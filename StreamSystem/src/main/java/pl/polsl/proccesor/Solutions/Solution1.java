package pl.polsl.proccesor.Solutions;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;
import pl.polsl.comon.model.input.HumidityData;
import pl.polsl.comon.model.input.LightData;
import pl.polsl.comon.model.input.TempData;
import pl.polsl.comon.repositories.ReportRepository;
import pl.polsl.comon.repositories.WindowStatisticsRepository;
import pl.polsl.model.DataProcess;
import pl.polsl.proccesor.CalculationResults;
import pl.polsl.proccesor.Processor;
import pl.polsl.proccesor.WindowBase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

//@Component
public class Solution1 extends WindowBase {

    private static final int BUFFER_SIZE = 40000;
    private final List<DataProcess<HumidityData>> humidityData = new ArrayList<>();
    private final List<DataProcess<LightData>> lightData = new ArrayList<>();
    private final List<DataProcess<TempData>> tempData = new ArrayList<>();

    public Solution1(@Value("${testPlanDir}") final String testPlanDir, final ReportRepository reportRepository, final WindowStatisticsRepository windowStatisticsRepository, final TaskScheduler taskScheduler, final Processor processor) throws IOException {
        super(testPlanDir, reportRepository, windowStatisticsRepository, taskScheduler, processor);
    }

    @Override
    public void handleHumidityData(final DataProcess<HumidityData> data) {
        if (humidityData.size() > BUFFER_SIZE) {
            humidityDataLoss++;
            return;
        }

        humidityData.add(data);
    }

    @Override
    public void handleLightData(final DataProcess<LightData> data) {
        if (lightData.size() > BUFFER_SIZE) {
            lightDataLoss++;
            return;
        }

        lightData.add(data);
    }

    @Override
    public void handleTempData(final DataProcess<TempData> data) {
        if (tempData.size() > BUFFER_SIZE) {
            tempDataLoss++;
            return;
        }

        tempData.add(data);
    }


    @Override
    protected CalculationResults calculateAllWindow() {
        final List<DataProcess<HumidityData>> humidityData = new ArrayList<>(this.humidityData);
        final List<DataProcess<LightData>> lightData = new ArrayList<>(this.lightData);
        final List<DataProcess<TempData>> tempData = new ArrayList<>(this.tempData);

        final List<String> sensors = super.sectors.stream().toList();

        final Long bufferSize = (long) (this.humidityData.size() + this.lightData.size() + this.tempData.size());

        this.humidityData.clear();
        this.lightData.clear();
        this.tempData.clear();

        final CalculationResults results = new CalculationResults();
        results.setEntities(new ArrayList<>());
        results.setBufferSize(bufferSize);

        for (final String sector : sensors) {
            final List<DataProcess<HumidityData>> sensorHumidityData = humidityData.stream()
                    .filter(dataProcess -> dataProcess.getData().getSector().equals(sector))
                    .toList();
            final List<DataProcess<LightData>> sensorLightData = lightData.stream()
                    .filter(dataProcess -> dataProcess.getData().getSector().equals(sector))
                    .toList();
            final List<DataProcess<TempData>> sensorTempData = tempData.stream()
                    .filter(dataProcess -> dataProcess.getData().getSector().equals(sector))
                    .toList();

            results.getEntities().add(super.processor.calculate(windowId, sector, sensorHumidityData,
                    sensorLightData,
                    sensorTempData));
        }

        return results;
    }
}
