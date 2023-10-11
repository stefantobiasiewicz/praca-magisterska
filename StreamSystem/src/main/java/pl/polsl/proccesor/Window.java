package pl.polsl.proccesor;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.stereotype.Component;
import pl.polsl.comon.config.ConfigGenerator;
import pl.polsl.comon.config.Experiment;
import pl.polsl.comon.config.Sensor;
import pl.polsl.comon.config.TestPlan;
import pl.polsl.comon.entites.WindowStatisticsEntity;
import pl.polsl.comon.model.input.HumidityData;
import pl.polsl.comon.model.input.LightData;
import pl.polsl.comon.model.input.TempData;
import pl.polsl.comon.repositories.WindowStatisticsRepository;
import pl.polsl.comon.utils.FileNames;
import pl.polsl.comon.utils.JsonUtils;
import pl.polsl.model.DataProcess;
import pl.polsl.model.ProcessReport;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Component
public class Window {

    private static final int BUFFER_SIZE = 1;
    private static long windowId = 0;
    private final WindowStatisticsRepository windowStatisticsRepository;
    private final long windowSize;
    private final long experimentTime;
    //    private final List<DataProcess<HumidityData>> humidityData = new ArrayList<>();
//    private final List<DataProcess<LightData>> lightData = new ArrayList<>();
//    private final List<DataProcess<TempData>> tempData = new ArrayList<>();
    private final Map<String, List<DataProcess<HumidityData>>> humidityData = new LinkedHashMap<>();
    private final Map<String, List<DataProcess<LightData>>> lightData = new LinkedHashMap<>();
    private final Map<String, List<DataProcess<TempData>>> tempData = new LinkedHashMap<>();
    private final Set<String> sectors = new HashSet<>();
    private final TaskScheduler taskScheduler;
    private final Processor processor;
    private final String dbContext;
    private long humidityDataLoss;
    private long lightDataLoss;
    private long tempDataLoss;

    public Window(@Value("${testPlanDir}") final String testPlanDir, final WindowStatisticsRepository windowStatisticsRepository, final TaskScheduler taskScheduler, final Processor processor) throws IOException {
        this.windowStatisticsRepository = windowStatisticsRepository;
        final Experiment experiment = JsonUtils.MAPPER.readValue(new File(testPlanDir, FileNames.EXPERIMENT),
                Experiment.class);

        final TestPlan testPlan = JsonUtils.MAPPER.readValue(new File(new File(testPlanDir,
                        experiment.getTest()), FileNames.EXPERIMENT_CONFIG),
                TestPlan.class);

        final ConfigGenerator configGenerator = JsonUtils.MAPPER.readValue(new File(new File(testPlanDir,
                        experiment.getTest()), testPlan.getGeneratorConfig()),
                ConfigGenerator.class);

        for (final Sensor sensor : configGenerator.getSensors()) {
            sectors.add(sensor.getSector());

            humidityData.put(sensor.getSector(), new ArrayList<>());
            lightData.put(sensor.getSector(), new ArrayList<>());
            tempData.put(sensor.getSector(), new ArrayList<>());
        }

        this.windowSize = testPlan.getWindowSize();
        this.experimentTime = testPlan.getExperimentDuration();
        this.dbContext = testPlan.getDatabaseContext();
        this.taskScheduler = taskScheduler;
        this.processor = processor;

        windowStatisticsRepository.deleteAllByContext(this.dbContext);
    }

    @PostConstruct
    public void scheduleTasks() {
        System.out.printf("Start EXPERIMENT at: %s%n", LocalDateTime.now());
        taskScheduler.schedule(this::calculate, new PeriodicTrigger(windowSize, TimeUnit.SECONDS));
        taskScheduler.scheduleWithFixedDelay(this::end, Date.from(Instant.now().plusSeconds(experimentTime)), 1);
    }

    private void end() {
        System.out.printf("End EXPERIMENT at: %s%n", LocalDateTime.now());
        System.out.println("End of experimetn!!!");
        System.exit(0);
    }

    public void handleHumidityData(final DataProcess<HumidityData> data) {
//        if (humidityData.size() > BUFFER_SIZE) {
//            humidityDataLoss++;
//            return;
//        }

//        humidityData.add(data);

        final List<DataProcess<HumidityData>> list = humidityData.get(data.getData().getSector());
        if (list.size() > BUFFER_SIZE) {
            humidityDataLoss++;
            return;
        }
        list.add(data);
    }

    public void handleLightData(final DataProcess<LightData> data) {
//        if (lightData.size() > BUFFER_SIZE) {
//            lightDataLoss++;
//            return;
//        }

//        lightData.add(data);

        final List<DataProcess<LightData>> list = lightData.get(data.getData().getSector());
        if (list.size() > BUFFER_SIZE) {
            lightDataLoss++;
            return;
        }
        list.add(data);
    }

    public void handleTempData(final DataProcess<TempData> data) {
//        if (tempData.size() > BUFFER_SIZE) {
//            tempDataLoss++;
//            return;
//        }

//        tempData.add(data);

        final List<DataProcess<TempData>> list = tempData.get(data.getData().getSector());
        if (list.size() > BUFFER_SIZE) {
            tempDataLoss++;
            return;
        }
        list.add(data);
    }

    private void calculate() {
        if (windowId == 0) {
            windowId++;
            return;
        }

        final long windowProcessStartTime = System.currentTimeMillis();

//        final List<DataProcess<HumidityData>> humidityData = new ArrayList<>(this.humidityData);
        final long humiditySessionDataLoss = humidityDataLoss;
//        final List<DataProcess<LightData>> lightData = new ArrayList<>(this.lightData);
        final long lightSessionDataLoss = lightDataLoss;
//        final List<DataProcess<TempData>> tempData = new ArrayList<>(this.tempData);
        final long tempSessionDataLoss = tempDataLoss;

        final List<String> sensors = this.sectors.stream().toList();

//        this.humidityData.clear();
        humidityDataLoss = 0;
//        this.lightData.clear();
        lightDataLoss = 0;
//        this.tempData.clear();
        tempDataLoss = 0;

        System.out.println("calculation START...");

        long sumOfMeanAgeOfInfo = 0;

        for (final String sector : sensors) {
//            final List<DataProcess<HumidityData>> sensorHumidityData = humidityData.stream()
//                    .filter(dataProcess -> dataProcess.getData().getSector().equals(sector))
//                    .toList();
//            final List<DataProcess<LightData>> sensorLightData = lightData.stream()
//                    .filter(dataProcess -> dataProcess.getData().getSector().equals(sector))
//                    .toList();
//            final List<DataProcess<TempData>> sensorTempData = tempData.stream()
//                    .filter(dataProcess -> dataProcess.getData().getSector().equals(sector))
//                    .toList();

            final List<DataProcess<HumidityData>> sensorHumidityData = this.humidityData.get(sector);
            final List<DataProcess<LightData>> sensorLightData = this.lightData.get(sector);
            final List<DataProcess<TempData>> sensorTempData = this.tempData.get(sector);

            final ProcessReport report = processor.calculate(windowId, sector, sensorHumidityData, sensorLightData,
                    sensorTempData);

            sensorHumidityData.clear();
            sensorLightData.clear();
            sensorTempData.clear();

            sumOfMeanAgeOfInfo += report.getMeanAgeOfInformation();
        }


        final WindowStatisticsEntity entity = new WindowStatisticsEntity();

        entity.setId(windowId);
        entity.setContext(dbContext);
        entity.setCatchTime(LocalDateTime.now());

        entity.setMeanAgeOfInfo(sumOfMeanAgeOfInfo / sensors.size());
        entity.setHumidityDataLost(humiditySessionDataLoss);
        entity.setLightDataLost(lightSessionDataLoss);
        entity.setTempDataLost(tempSessionDataLoss);

        entity.setHumidityBuffSize((long) humidityData.size());
        entity.setLightBuffSize((long) lightData.size());
        entity.setTempBuffSize((long) tempData.size());

        entity.setWindowProcessTime(System.currentTimeMillis() - windowProcessStartTime);

        windowStatisticsRepository.save(entity);

        windowId++;
        try {
            System.out.println(JsonUtils.MAPPER.writeValueAsString(entity));
        } catch (final JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        System.out.println("calculation DONE...");
    }
}