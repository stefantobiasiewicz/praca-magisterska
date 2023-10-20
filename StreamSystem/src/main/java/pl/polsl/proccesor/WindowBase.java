package pl.polsl.proccesor;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.transaction.annotation.Transactional;
import pl.polsl.comon.config.ConfigGenerator;
import pl.polsl.comon.config.Experiment;
import pl.polsl.comon.config.Sensor;
import pl.polsl.comon.config.TestPlan;
import pl.polsl.comon.entites.WindowStatisticsEntity;
import pl.polsl.comon.model.input.HumidityData;
import pl.polsl.comon.model.input.LightData;
import pl.polsl.comon.model.input.TempData;
import pl.polsl.comon.repositories.ReportRepository;
import pl.polsl.comon.repositories.WindowStatisticsRepository;
import pl.polsl.comon.utils.FileNames;
import pl.polsl.comon.utils.JsonUtils;
import pl.polsl.model.DataProcess;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

public abstract class WindowBase {
    protected static long windowId = 0;
    private final ReportRepository reportRepository;
    private final WindowStatisticsRepository windowStatisticsRepository;
    private final long windowSize;
    private final long experimentTime;
    protected final Set<String> sectors = new HashSet<>();
    private final TaskScheduler taskScheduler;
    protected final Processor processor;
    private final String dbContext;
    protected long humidityDataLoss;
    protected long lightDataLoss;
    protected long tempDataLoss;

    public WindowBase(@Value("${testPlanDir}") final String testPlanDir, final ReportRepository reportRepository, final WindowStatisticsRepository windowStatisticsRepository, final TaskScheduler taskScheduler, final Processor processor) throws IOException {
        this.reportRepository = reportRepository;
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
        }


        this.windowSize = testPlan.getWindowSize();
        this.experimentTime = testPlan.getExperimentDuration();
        this.dbContext = testPlan.getDatabaseContext();
        this.taskScheduler = taskScheduler;
        this.processor = processor;

        windowStatisticsRepository.deleteAllByContext(this.dbContext);

        System.out.println(String.format("solution name: '%s'.", this.getClass().getName()));
    }

    @PostConstruct
    @Transactional
    public void scheduleTasks() {
        System.out.printf("Start EXPERIMENT at: %s%n", LocalDateTime.now());

        final PeriodicTrigger trigger = new PeriodicTrigger(windowSize, TimeUnit.SECONDS);
        trigger.setFixedRate(true);

//        taskScheduler.schedule(this::calculate, trigger);
        taskScheduler.scheduleWithFixedDelay(this::end, Date.from(Instant.now().plusSeconds(experimentTime)), 1);
        reportRepository.deleteAllByContext(this.dbContext);
    }


    private void end() {
        System.out.printf("End EXPERIMENT at: %s%n", LocalDateTime.now());
        System.out.println("End of experiment!!!");
        System.exit(0);
    }


    protected abstract void handleHumidityData(final DataProcess<HumidityData> data);

    protected abstract void handleLightData(final DataProcess<LightData> data);

    protected abstract void handleTempData(final DataProcess<TempData> data);

    protected abstract CalculationResults calculateAllWindow();

    @Async
    @Scheduled(fixedRate = 45000)
    protected void calculate() {
        windowId++;

        final long windowProcessStartTime = System.currentTimeMillis();

        final long humiditySessionDataLoss = humidityDataLoss;
        final long lightSessionDataLoss = lightDataLoss;
        final long tempSessionDataLoss = tempDataLoss;

        final List<String> sensors = this.sectors.stream().toList();

        humidityDataLoss = 0;
        lightDataLoss = 0;
        tempDataLoss = 0;

        System.out.println("calculation START...");

        final CalculationResults results = calculateAllWindow();

        reportRepository.saveAll(results.getEntities());

        final WindowStatisticsEntity entity = new WindowStatisticsEntity();

        entity.setId(windowId);
        entity.setContext(dbContext);
        entity.setCatchTime(LocalDateTime.now());

        entity.setHumidityDataLost(humiditySessionDataLoss);
        entity.setLightDataLost(lightSessionDataLoss);
        entity.setTempDataLost(tempSessionDataLoss);

        entity.setBufferSize(results.getBufferSize());

        entity.setWindowProcessTime(System.currentTimeMillis() - windowProcessStartTime);

        windowStatisticsRepository.save(entity);


        try {
            System.out.println(JsonUtils.MAPPER.writeValueAsString(entity));
        } catch (final JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        System.out.println("calculation DONE...");
    }
}