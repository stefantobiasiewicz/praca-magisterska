package pl.polsl;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.logging.Logger;
import org.eclipse.paho.client.mqttv3.logging.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import pl.polsl.comon.config.ConfigGenerator;
import pl.polsl.comon.config.Experiment;
import pl.polsl.comon.config.Sensor;
import pl.polsl.comon.config.TestPlan;
import pl.polsl.comon.entites.ReportEntity;
import pl.polsl.comon.repositories.ReportRepository;
import pl.polsl.comon.utils.FileNames;
import pl.polsl.comon.utils.JsonUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootApplication(scanBasePackages = {"pl.polsl.**", "pl.polsl.comon.repositories"})
@EntityScan(basePackages = {"pl.polsl.comon.entites"})
public class Main implements CommandLineRunner {

    @Autowired
    ReportRepository reportRepository;

    public static void main(final String[] args) {
        SpringApplication.run(Main.class, args);
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


//        map of addres -> liczba oczekiwanych wystapien w eksperymencie


        final int window = testPlan.getWindowSize();
        final int testTime = testPlan.getExperimentDuration();

        double totalLightQoD = 0;
        double totalHumidityQoD = 0;
        double totalTemperatureQoD = 0;

        System.out.println("Start Liczenia wyników");
        int totalSensors = configGenerator.getSensors().size();
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
            }

            final double realCountOfLightInSingleWindow =  (double) countOfLightDataPresent / entities.size();
            final double realCountOfHumidityInSingleWindow = (double) countOfHumidityDataPresent / entities.size();
            final double realCountOfTemperatureInSingleWindow = (double) countOfTemperatureDataPresent / entities.size();

            final double lightQoD = realCountOfLightInSingleWindow / expectedCountOfLightInSingleWindow;
            final double humidityQoD = realCountOfHumidityInSingleWindow / expectedCountOfHumidityInSingleWindow;
            final double temperatureQoD = realCountOfTemperatureInSingleWindow / expectedCountOfTemperatureInSingleWindow;

            totalLightQoD += lightQoD;
            totalHumidityQoD += humidityQoD;
            totalTemperatureQoD += temperatureQoD;


            currentSensor++;
            double progress = (double) currentSensor / totalSensors;

            // Wyświetl pasek postępu
            int progressBarWidth = 50; // Szerokość paska postępu
            int progressValue = (int) (progress * progressBarWidth);
            String progressBar = "[" + "*".repeat(progressValue) + " ".repeat(progressBarWidth - progressValue) + "]";

            // Wypisz postęp na konsoli
            System.out.print("\r");

            // Wypisz postęp na konsoli bez nowej linii
            System.out.printf("Postęp: %.2f%% %s", progress * 100, progressBar);

            // Wymuś natychmiastowe wyświetlenie danych w konsoli
            System.out.flush();
        }

        final double averageLightQoD = totalLightQoD / configGenerator.getSensors().size();
        final double averageHumidityQoD = totalHumidityQoD / configGenerator.getSensors().size();
        final double averageTemperatureQoD = totalTemperatureQoD / configGenerator.getSensors().size();

        System.out.println("");
        System.out.println("");
        System.out.println("Czas testu: " + testTime);
        System.out.println("Nazwa: " + testPlan.getDatabaseContext());
        System.out.println("Liczba sektorów: " + configGenerator.getSensors().size());
        System.out.println("Długość okna: " + testPlan.getWindowSize());
        System.out.println("************ WYNIKI ************");
        System.out.println("Średnia LightQoD: " + averageLightQoD);
        System.out.println("Średnia HumidityQoD: " + averageHumidityQoD);
        System.out.println("Średnia TemperatureQoD: " + averageTemperatureQoD);
    }

    @NoArgsConstructor
    public static class Args {
        @Getter
        @Parameter(names = {"-dir", "--config-dir"}, description = "Path to test plan dir", required = true)
        private File testPlanDir;
    }
}