package pl.polsl.proccesor;

import org.springframework.stereotype.Component;
import pl.polsl.comon.model.input.HumidityData;
import pl.polsl.comon.model.input.LightData;
import pl.polsl.comon.model.input.TempData;
import pl.polsl.model.DataProcess;

@Component
public class Handler {

    private final WindowBase window;

    public Handler(final WindowBase window) {
        this.window = window;
    }

    public void handle(final HumidityData data) {
        final DataProcess<HumidityData> dataProcess = new DataProcess<>();
        dataProcess.setData(data);
        dataProcess.setArrivedTime(System.currentTimeMillis());

        window.handleHumidityData(dataProcess);
    }

    public void handle(final LightData data) {
        final DataProcess<LightData> dataProcess = new DataProcess<>();
        dataProcess.setData(data);
        dataProcess.setArrivedTime(System.currentTimeMillis());

        window.handleLightData(dataProcess);
    }

    public void handle(final TempData data) {
        final DataProcess<TempData> dataProcess = new DataProcess<>();
        dataProcess.setData(data);
        dataProcess.setArrivedTime(System.currentTimeMillis());

        window.handleTempData(dataProcess);
    }
}
