package pl.polsl.comon.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TestPlan {
    @JsonProperty("window-size")
    public int windowSize;
    @JsonProperty("experiment-duration")
    public int experimentDuration;
    @JsonProperty("database-context")
    public String databaseContext;
    @JsonProperty("generator-config")
    public String generatorConfig;
    @JsonProperty("outputDir")
    public String outputDir;
}
