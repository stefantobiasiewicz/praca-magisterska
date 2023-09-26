package pl.polsl.config;

public enum GeneratorType {
    TEMP("TEMP"),
    HUMIDITY("HUMIDITY"),
    LIGHT("LIGHT")
    ;

    private String name;

    GeneratorType(String name) {
        this.name = name;
    }
}
