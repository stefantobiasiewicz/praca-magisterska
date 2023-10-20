package pl.polsl.proccesor.Solutions;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.TaskScheduler;
import pl.polsl.comon.model.input.HumidityData;
import pl.polsl.comon.model.input.LightData;
import pl.polsl.comon.model.input.TempData;
import pl.polsl.comon.repositories.ReportRepository;
import pl.polsl.comon.repositories.WindowStatisticsRepository;
import pl.polsl.model.DataProcess;
import pl.polsl.proccesor.CalculationResults;
import pl.polsl.proccesor.Processor;
import pl.polsl.proccesor.WindowBase;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;

//@Component
public class Solution2 extends WindowBase {
    private static final int TOTAL_BUFFER_SIZE = 30000;
    private int bufferSize = 0;
    private final Map<String, List<DataProcess<HumidityData>>> humidityData = new HashMap<>();
    private final Map<String, List<DataProcess<LightData>>> lightData = new HashMap<>();
    private final Map<String, List<DataProcess<TempData>>> tempData = new HashMap<>();

    public Solution2(@Value("${testPlanDir}") final String testPlanDir, final ReportRepository reportRepository, final WindowStatisticsRepository windowStatisticsRepository, final TaskScheduler taskScheduler, final Processor processor) throws IOException {
        super(testPlanDir, reportRepository, windowStatisticsRepository, taskScheduler, processor);
    }

    @PostConstruct
    protected void prefillBuffers() {
        for (final String sector : sectors) {
            humidityData.put(sector, new ArrayList<>());
            lightData.put(sector, new ArrayList<>());
            tempData.put(sector, new ArrayList<>());
        }
    }

    @Override
    public void handleHumidityData(final DataProcess<HumidityData> data) {
        final List<DataProcess<HumidityData>> list = humidityData.get(data.getData().getSector());

        if (bufferSize > TOTAL_BUFFER_SIZE) {
            return;
        }

        list.add(data);
        bufferSize++;
    }

    @Override
    public void handleLightData(final DataProcess<LightData> data) {
        final List<DataProcess<LightData>> list = lightData.get(data.getData().getSector());

        if (bufferSize > TOTAL_BUFFER_SIZE) {
            return;
        }

        list.add(data);
        bufferSize++;
    }

    @Override
    public void handleTempData(final DataProcess<TempData> data) {
        final List<DataProcess<TempData>> list = tempData.get(data.getData().getSector());

        if (bufferSize > TOTAL_BUFFER_SIZE) {
            return;
        }

        list.add(data);
        bufferSize++;
    }


    @Override
    protected CalculationResults calculateAllWindow() {
        final List<String> sensors = super.sectors.stream().toList();

        long bufferSize = 0L;

        final CalculationResults results = new CalculationResults();
        results.setEntities(new ArrayList<>());

        for (int i = 0; i < sensors.size(); i++) {
            final String sector = sensors.get(i);

            final List<DataProcess<HumidityData>> sensorHumidityData = this.humidityData.get(sector);
            final List<DataProcess<LightData>> sensorLightData = this.lightData.get(sector);
            final List<DataProcess<TempData>> sensorTempData = this.tempData.get(sector);

            results.getEntities().add(super.processor.calculate(windowId, sector, sensorHumidityData,
                    sensorLightData, sensorTempData));
            bufferSize += sensorHumidityData.size() + sensorLightData.size() + sensorTempData.size();

            this.bufferSize -= bufferSize;

            sensorHumidityData.clear();
            sensorLightData.clear();
            sensorTempData.clear();
        }

        results.setBufferSize(bufferSize);
        return results;
    }
}
