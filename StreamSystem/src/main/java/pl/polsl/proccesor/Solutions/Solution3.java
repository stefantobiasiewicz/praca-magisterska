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
import pl.polsl.proccesor.Solutions.help.Tuple;
import pl.polsl.proccesor.WindowBase;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class Solution3 extends WindowBase {
    private static final int TOTAL_BUFFER_SIZE = 50000;
    private final HashMap<String, Tuple> data = new HashMap<>();
    private int bufferSize = 0;


    public Solution3(@Value("${testPlanDir}") final String testPlanDir,
                     final ReportRepository reportRepository, final WindowStatisticsRepository windowStatisticsRepository, final TaskScheduler taskScheduler, final Processor processor) throws IOException {
        super(testPlanDir, reportRepository, windowStatisticsRepository, taskScheduler, processor);
    }

    @PostConstruct
    protected void prefillBuffers() {
        for (final String sector : sectors) {
            data.put(sector, new Tuple());
        }
    }

    @Override
    public synchronized void handleHumidityData(final DataProcess<HumidityData> data) {
        final List<DataProcess<HumidityData>> list = this.data.get(data.getData().getSector()).getHumidityData();

        if (bufferSize > TOTAL_BUFFER_SIZE) {
            humidityDataLoss++;
            return;
        }

        list.add(data);
        bufferSize++;
    }

    @Override
    public synchronized void handleLightData(final DataProcess<LightData> data) {
        final List<DataProcess<LightData>> list = this.data.get(data.getData().getSector()).getLightData();

        if (bufferSize > TOTAL_BUFFER_SIZE) {
            lightDataLoss++;
            return;
        }

        list.add(data);
        bufferSize++;
    }

    @Override
    public synchronized void handleTempData(final DataProcess<TempData> data) {
        final List<DataProcess<TempData>> list = this.data.get(data.getData().getSector()).getTempData();

        if (bufferSize > TOTAL_BUFFER_SIZE) {
            tempDataLoss++;
            return;
        }

        list.add(data);
        bufferSize++;
    }


    @Override
    protected CalculationResults calculateAllWindow() {
        final List<String> sensors = super.sectors.stream().toList();


        final Map<String, Tuple> data = (Map<String, Tuple>) this.data.clone();

        long bufferSize = 0;
        for (final Map.Entry<String, Tuple> tuple : this.data.entrySet()) {
            bufferSize += tuple.getValue().getHumidityData().size();
            bufferSize += tuple.getValue().getLightData().size();
            bufferSize += tuple.getValue().getTempData().size();
        }

        this.bufferSize = 0;

        final CalculationResults results = new CalculationResults();
        results.setEntities(new ArrayList<>());

        for (int i = 0; i < sensors.size(); i++) {
            final String sector = sensors.get(i);

            final List<DataProcess<HumidityData>> sensorHumidityData = data.get(sector).getHumidityData();
            final List<DataProcess<LightData>> sensorLightData = data.get(sector).getLightData();
            final List<DataProcess<TempData>> sensorTempData = data.get(sector).getTempData();

            results.getEntities().add(super.processor.calculate(windowId, sector, sensorHumidityData, sensorLightData,
                    sensorTempData));


            sensorHumidityData.clear();
            sensorLightData.clear();
            sensorTempData.clear();
        }

        results.setBufferSize(bufferSize);
        return results;
    }
}
