package pl.polsl.comon.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import pl.polsl.comon.model.MessageType;

import java.util.List;

@Data
public class ConfigGenerator {
    @JsonProperty("experiment-time")
    private Long experimentTime;
    @JsonProperty("mqtt")
    private MqttConnection mqtt;
    @JsonProperty("sensors")
    private List<Sensor> sensors;
}
