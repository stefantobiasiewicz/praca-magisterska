package pl.polsl.comon.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class ConfigSectors {
    @JsonProperty("sectors")
    private List<String> sectors;
}
