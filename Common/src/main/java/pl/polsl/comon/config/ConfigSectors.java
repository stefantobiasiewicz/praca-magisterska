package pl.polsl.comon.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class ConfigSectors {
    @JsonProperty("meanInterval")
    private double meanInterval;
    @JsonProperty("maxInterval")
    private double maxInterval;
    @JsonProperty("minInterval")
    private double minInterval;
    @JsonProperty("sectors")
    private List<String> sectors;
}
