package pl.polsl.proccesor.Solutions.help;

import lombok.Data;
import pl.polsl.comon.model.input.HumidityData;
import pl.polsl.comon.model.input.LightData;
import pl.polsl.comon.model.input.TempData;
import pl.polsl.model.DataProcess;

import java.util.ArrayList;
import java.util.List;


@Data
public class Tuple {
    private final List<DataProcess<HumidityData>> humidityData = new ArrayList<>();
    private final List<DataProcess<LightData>> lightData = new ArrayList<>();
    private final List<DataProcess<TempData>> tempData = new ArrayList<>();
}