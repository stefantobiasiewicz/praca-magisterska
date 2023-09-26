package pl.polsl.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class MqttConnection {
    @JsonProperty("broker")
    private String brokerAddress;
    @JsonProperty("port")
    private int port;
    @JsonProperty("username")
    private String username;
    @JsonProperty("password")
    private String password;
    @JsonProperty("clientId")
    private String clientId;
}
