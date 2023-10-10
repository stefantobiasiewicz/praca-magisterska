package pl.polsl.comon.model.input;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;



@Data
public class TempData {
    @JsonProperty("sector")
    private String sector;
    @JsonProperty("temperature")
    private Double temperature;
    @JsonProperty("timestamp")
    private LocalDateTime dateOfCreation;
}

