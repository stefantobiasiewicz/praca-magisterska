package pl.polsl.comon.model;

import lombok.Getter;

public enum MessageType {
    TEMP("TEMP"),
    HUMIDITY("HUMIDITY"),
    LIGHT("LIGHT")
    ;

    @Getter
    private String name;

    MessageType(String name) {
        this.name = name;
    }
}
