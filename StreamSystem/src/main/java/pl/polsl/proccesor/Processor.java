package pl.polsl.proccesor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pl.polsl.comon.config.ConfigGenerator;
import pl.polsl.comon.config.Experiment;
import pl.polsl.comon.config.Sensor;
import pl.polsl.comon.config.TestPlan;
import pl.polsl.comon.entites.ReportEntity;
import pl.polsl.comon.model.input.HumidityData;
import pl.polsl.comon.model.input.LightData;
import pl.polsl.comon.model.input.TempData;
import pl.polsl.comon.repositories.ReportRepository;
import pl.polsl.comon.utils.FileNames;
import pl.polsl.comon.utils.JsonUtils;
import pl.polsl.model.DataProcess;
import pl.polsl.model.DataTimestamps;
import pl.polsl.model.PartialReport;
import pl.polsl.model.ProcessReport;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class Processor {
    private final String dbContext;
    private final ReportRepository reportRepository;


    public Processor(@Value("${testPlanDir}") final String testPlanDir, final ReportRepository reportRepository) throws IOException {
        this.reportRepository = reportRepository;
        final Experiment experiment = JsonUtils.MAPPER.readValue(new File(testPlanDir, FileNames.EXPERIMENT),
                Experiment.class);

        final TestPlan testPlan = JsonUtils.MAPPER.readValue(new File(new File(testPlanDir,
                        experiment.getTest()), FileNames.EXPERIMENT_CONFIG),
                TestPlan.class);

        this.dbContext = testPlan.getDatabaseContext();


        System.out.println(dbContext);

        reportRepository.deleteAllByContext(this.dbContext);
    }

    public static <T> long calculateAverageTimeInSystem(final List<DataProcess<T>> dataProcessList, final long currentTimeMillis) {
        if (dataProcessList == null || dataProcessList.isEmpty()) {
            return 0;
        }

        long totalTimeInSystem = 0;

        for (final DataProcess<T> dataProcess : dataProcessList) {
            final long timeInSystem = currentTimeMillis - dataProcess.getArrivedTime();
            totalTimeInSystem += timeInSystem;
        }

        return totalTimeInSystem / dataProcessList.size();
    }

    public ProcessReport calculate(final long windowId, final String sensor,
                                   final List<DataProcess<HumidityData>> sensorHumidityData,
                                   final List<DataProcess<LightData>> sensorLightData,
                                   final List<DataProcess<TempData>> sensorTempData) {
        final ProcessReport report = new ProcessReport();
        final ReportEntity entity = new ReportEntity();

        entity.setWindowId(windowId);
        entity.setSector(sensor);
        entity.setCatchTime(LocalDateTime.now());

        entity.setContext(dbContext);
        entity.setWasHum(!sensorHumidityData.isEmpty() ? Boolean.TRUE : Boolean.FALSE);
        entity.setWasLight(!sensorLightData.isEmpty() ? Boolean.TRUE : Boolean.FALSE);
        entity.setWasTemp(!sensorTempData.isEmpty() ? Boolean.TRUE : Boolean.FALSE);

        final long averageHumTime = calculateAverageTimeInSystem(sensorHumidityData, System.currentTimeMillis());
        final long averageLightTime = calculateAverageTimeInSystem(sensorTempData, System.currentTimeMillis());
        final long averageTempTime = calculateAverageTimeInSystem(sensorLightData, System.currentTimeMillis());

        final long mean = (averageTempTime + averageHumTime + averageLightTime) / 3;

        entity.setMeanAgeOfInfo(mean);
        report.setMeanAgeOfInformation(mean);

        reportRepository.save(entity);

        return report;
    }
}
