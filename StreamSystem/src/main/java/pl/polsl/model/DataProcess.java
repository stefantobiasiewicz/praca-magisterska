package pl.polsl.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DataProcess<T>{
    private long arrivedTime;

    private T data;
}