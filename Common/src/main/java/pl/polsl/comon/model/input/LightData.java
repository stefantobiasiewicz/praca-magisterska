package pl.polsl.comon.model.input;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LightData {
    @JsonProperty("sector")
    private String sector;
    @JsonProperty("lux")
    private Double lux;
    @JsonProperty("timestamp")
    private LocalDateTime dateOfCreation;
}