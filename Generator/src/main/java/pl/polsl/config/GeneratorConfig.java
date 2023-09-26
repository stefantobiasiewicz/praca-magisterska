package pl.polsl.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class GeneratorConfig {
    @JsonProperty("type")
    private GeneratorType type;
    @JsonProperty("experiment-time")
    private Long experimentTime;
    @JsonProperty("mqtt")
    private MqttConnection mqtt;
    @JsonProperty("sectors")
    private List<Sector> sectors;
}
