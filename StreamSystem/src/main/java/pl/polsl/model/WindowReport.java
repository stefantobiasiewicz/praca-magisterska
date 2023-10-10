package pl.polsl.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WindowReport {
    private LocalDateTime WindowProcessStartTime;
    private long windowProcessTime = 0;
    private long windowBufferDataLen = 0;

    private long meanSingleProcessTime = 0;
    private long meanSingleProcessDataLen = 0;

    private long windowDataErrorCount = 0;
    private long sensorsCount = 0;
}
