package pl.polsl.proccesor.Solutions.help;

import lombok.AllArgsConstructor;
import lombok.Data;
import pl.polsl.comon.model.input.HumidityData;
import pl.polsl.comon.model.input.LightData;
import pl.polsl.comon.model.input.TempData;
import pl.polsl.model.DataProcess;

import java.util.ArrayList;
import java.util.List;


@Data
public class Tuple {
    private final List<DataProcess<HumidityData>> humidityData;
    private final List<DataProcess<LightData>> lightData;
    private final List<DataProcess<TempData>> tempData;

    public Tuple() {
        this.tempData = new ArrayList<>();
        this.lightData = new ArrayList<>();
        this.humidityData = new ArrayList<>();
    }

    public Tuple(final List<DataProcess<HumidityData>> humidityData, final List<DataProcess<LightData>> lightData, final List<DataProcess<TempData>> tempData) {
        this.humidityData = new ArrayList<>(humidityData);
        this.lightData = new ArrayList<>(lightData);
        this.tempData = new ArrayList<>(tempData);
    }
}