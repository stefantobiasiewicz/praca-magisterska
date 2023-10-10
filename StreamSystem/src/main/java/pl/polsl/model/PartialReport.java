package pl.polsl.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PartialReport {
    private Long windowDataLen;
    private List<DataTimestamps> timestamps = new ArrayList<>();
}
