package pl.polsl.job;

import lombok.Data;

@Data
public class Job implements Comparable<Job> {
    private Double rng;
    private String name;
    private String addresses;
    private Double interval;
    private Double noise;
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
