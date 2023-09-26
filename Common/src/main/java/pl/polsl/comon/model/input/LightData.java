package pl.polsl.comon.model.input;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class LightData {
    @JsonProperty("address")
    private String address;
    @JsonProperty("lux")
    private Double lux;
    @JsonProperty("timestamp")
    private OffsetDateTime dateOfCreation;
}