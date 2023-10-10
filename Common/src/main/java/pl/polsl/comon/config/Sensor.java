package pl.polsl.comon.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Sensor {
    @JsonProperty("sector")
    private String sector;
    @JsonProperty("temperature-interval")
    private Double temperatureInterval;
    @JsonProperty("light-interval")
    private Double lightInterval;
    @JsonProperty("humidity-interval")
    private Double humidityInterval;
}
