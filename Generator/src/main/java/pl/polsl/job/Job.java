package pl.polsl.job;

import lombok.Data;
import pl.polsl.comon.model.MessageType;

@Data
public class Job implements Comparable<Job> {
    private MessageType type;
    private Double rng;
    private String sector;
    private Double interval;
    private Double value;
    private Long nextCall;
    @Override
    public int compareTo(Job o) {
        if (nextCall.equals(o.getNextCall())) {
            return rng.compareTo(o.getRng());
        }

        return nextCall.compareTo(o.nextCall);
    }
}
