package pl.polsl;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import pl.polsl.comon.config.ConfigGenerator;
import pl.polsl.comon.config.Experiment;
import pl.polsl.comon.config.Sensor;
import pl.polsl.comon.config.TestPlan;
import pl.polsl.comon.entites.ReportEntity;
import pl.polsl.comon.entites.WindowStatisticsEntity;
import pl.polsl.comon.repositories.ReportRepository;
import pl.polsl.comon.repositories.WindowStatisticsRepository;
import pl.polsl.comon.utils.FileNames;
import pl.polsl.comon.utils.JsonUtils;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SpringBootApplication(scanBasePackages = {"pl.polsl.**", "pl.polsl.comon.repositories"})
@EntityScan(basePackages = {"pl.polsl.comon.entites"})
public class Main implements CommandLineRunner {

    @Autowired
    WindowStatisticsRepository windowStatisticsRepository;
    @Autowired
    ReportRepository reportRepository;

    public static void main(final String[] args) {
        SpringApplication.run(Main.class, args);
    }

    private String escapeSpecialCharacters(String data) {
        String escapedData = data.replaceAll("\\R", " ");
        if (data.contains(",") || data.contains("\"") || data.contains("'")) {
            data = data.replace("\"", "\"\"");
            escapedData = "\"" + data + "\"";
        }
        return escapedData;
    }

    private String convertToCSV(final String[] data) {
        return Stream.of(data)
                .map(this::escapeSpecialCharacters)
                .collect(Collectors.joining(","));
    }


    @Override
    public void run(final String... args) throws Exception {
        final Args arguments = new Args();
        JCommander.newBuilder()
                .addObject(arguments)
                .build()
                .parse(args);


        final Experiment experiment = JsonUtils.MAPPER.readValue(new File(arguments.getTestPlanDir(), FileNames.EXPERIMENT),
                Experiment.class);

        final TestPlan testPlan = JsonUtils.MAPPER.readValue(new File(new File(arguments.getTestPlanDir(),
                        experiment.getTest()), FileNames.EXPERIMENT_CONFIG),
                TestPlan.class);

        final ConfigGenerator configGenerator = JsonUtils.MAPPER.readValue(new File(new File(arguments.getTestPlanDir(),
                        experiment.getTest()), testPlan.getGeneratorConfig()),
                ConfigGenerator.class);


        final File outputDir = new File(new File(arguments.getTestPlanDir(),
                experiment.getTest()), testPlan.getOutputDir());
        if (!outputDir.exists()) {
            outputDir.mkdir();
        } else {
            outputDir.delete();
            outputDir.mkdir();
        }


//        map of addres -> liczba oczekiwanych wystapien w eksperymencie


        final int window = testPlan.getWindowSize();
        final int testTime = testPlan.getExperimentDuration();

        //        ************************* QoD *************************
        final File ageOfInfoOutputFile = new File(outputDir, FileNames.AGE_OF_INFO_FILE_NAME);
        final PrintWriter ageOfInfoOutputFilePw = new PrintWriter(ageOfInfoOutputFile);




        double totalLightQoD = 0;
        double totalHumidityQoD = 0;
        double totalTemperatureQoD = 0;

        System.out.println("Start Liczenia wyników");
        final int totalSensors = configGenerator.getSensors().size();
        int currentSensor = 0;

        for (final Sensor sensor : configGenerator.getSensors()) {
            double expectedCountOfLightInSingleWindow = window / sensor.getLightInterval();
            double expectedCountOfHumidityInSingleWindow = window / sensor.getHumidityInterval();
            double expectedCountOfTemperatureInSingleWindow = window / sensor.getTemperatureInterval();

            /**
             * jeśli ilość wysłanych wiadomości w oknie jest więszka niż 1 oznacza to że w każdym oknie będzie
             * informacja
             */
            if (expectedCountOfLightInSingleWindow > 1) {
                expectedCountOfLightInSingleWindow = 1;
            }

            if (expectedCountOfHumidityInSingleWindow > 1) {
                expectedCountOfHumidityInSingleWindow = 1;
            }

            if (expectedCountOfTemperatureInSingleWindow > 1) {
                expectedCountOfTemperatureInSingleWindow = 1;
            }

            final List<ReportEntity> entities = reportRepository.getAllByContextAndSector(testPlan.getDatabaseContext(),
                    sensor.getSector());

            int countOfLightDataPresent = 0;
            int countOfHumidityDataPresent = 0;
            int countOfTemperatureDataPresent = 0;

            for (final ReportEntity entity : entities) {
                countOfLightDataPresent += entity.getWasLight() == Boolean.TRUE ? 1 : 0;
                countOfHumidityDataPresent += entity.getWasHum() == Boolean.TRUE ? 1 : 0;
                countOfTemperatureDataPresent += entity.getWasTemp() == Boolean.TRUE ? 1 : 0;

                ageOfInfoOutputFilePw.println(entity.getMeanAgeOfInfo());
            }

            final double realCountOfLightInSingleWindow = (double) countOfLightDataPresent / entities.size();
            final double realCountOfHumidityInSingleWindow = (double) countOfHumidityDataPresent / entities.size();
            final double realCountOfTemperatureInSingleWindow = (double) countOfTemperatureDataPresent / entities.size();

            final double lightQoD = realCountOfLightInSingleWindow / expectedCountOfLightInSingleWindow;
            final double humidityQoD = realCountOfHumidityInSingleWindow / expectedCountOfHumidityInSingleWindow;
            final double temperatureQoD = realCountOfTemperatureInSingleWindow / expectedCountOfTemperatureInSingleWindow;

            totalLightQoD += lightQoD;
            totalHumidityQoD += humidityQoD;
            totalTemperatureQoD += temperatureQoD;

            currentSensor++;
            final double progress = (double) currentSensor / totalSensors;

            // Wyświetl pasek postępu
            final int progressBarWidth = 50; // Szerokość paska postępu
            final int progressValue = (int) (progress * progressBarWidth);
            final String progressBar = "[" + "*".repeat(progressValue) + " ".repeat(progressBarWidth - progressValue) + "]";

            // Wypisz postęp na konsoli
            System.out.print("\r");

            // Wypisz postęp na konsoli bez nowej linii
            System.out.printf("Postęp: %.2f%% %s", progress * 100, progressBar);

            // Wymuś natychmiastowe wyświetlenie danych w konsoli
            System.out.flush();
        }

        ageOfInfoOutputFilePw.close();

        final double averageLightQoD = totalLightQoD / configGenerator.getSensors().size();
        final double averageHumidityQoD = totalHumidityQoD / configGenerator.getSensors().size();
        final double averageTemperatureQoD = totalTemperatureQoD / configGenerator.getSensors().size();


        //        ************************* QoS *************************
        double totalQoS = 0;
        int qosSamples = 0;


        final List<String[]> dataLines = new ArrayList<>();
        dataLines.add(new String[]{"window id", "buffer size", "humidity_lost", "light_lost", "temp_lost", "procces time"});

        final List<WindowStatisticsEntity> entities =
                windowStatisticsRepository.getAllByContext(testPlan.getDatabaseContext());

        for (final WindowStatisticsEntity entity : entities) {
            qosSamples++;
//                if (entity.getWindowProcessTime() > window ) {
//                    totalQoS += 0;
//                    continue;
//                }

            dataLines.add(new String[]{entity.getId().toString(), entity.getBufferSize().toString(),
                    entity.getHumidityDataLost().toString(),
                    entity.getLightDataLost().toString(),
                    entity.getTempDataLost().toString(),
                    entity.getWindowProcessTime().toString()});


            totalQoS += entity.getWindowProcessTime();
        }
        final double qos = totalQoS / qosSamples;

//        window results
        final File csvOutputFile = new File(outputDir, FileNames.WINDOW_CSV_FILE_NAME);
        try (final PrintWriter pw = new PrintWriter(csvOutputFile)) {
            dataLines.stream()
                    .map(this::convertToCSV)
                    .forEach(pw::println);
        }

        final File qodOutputFile = new File(outputDir, FileNames.STATS_FILE_NAME);
        try (final PrintWriter pw = new PrintWriter(qodOutputFile)) {
            pw.println("Czas testu: " + testTime);
            pw.println("Nazwa: " + testPlan.getDatabaseContext());
            pw.println("Liczba sektorów: " + configGenerator.getSensors().size());
            pw.println("Długość okna: " + testPlan.getWindowSize());
            pw.println("************ WYNIKI ************");
            pw.println("Średnia LightQoD: " + averageLightQoD);
            pw.println("Średnia HumidityQoD: " + averageHumidityQoD);
            pw.println("Średnia TemperatureQoD: " + averageTemperatureQoD);
            pw.println("QoS: " + qos);
        }


        System.out.println();
        System.out.println();
        System.out.println("Czas testu: " + testTime);
        System.out.println("Nazwa: " + testPlan.getDatabaseContext());
        System.out.println("Liczba sektorów: " + configGenerator.getSensors().size());
        System.out.println("Długość okna: " + testPlan.getWindowSize());
        System.out.println("************ WYNIKI ************");
        System.out.println("Średnia LightQoD: " + averageLightQoD);
        System.out.println("Średnia HumidityQoD: " + averageHumidityQoD);
        System.out.println("Średnia TemperatureQoD: " + averageTemperatureQoD);
        System.out.println("QoS: " + qos);


    }

    @NoArgsConstructor
    public static class Args {
        @Getter
        @Parameter(names = {"-dir", "--config-dir"}, description = "Path to test plan dir", required = true)
        private File testPlanDir;
    }
}