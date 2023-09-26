package pl.polsl.job;

import java.util.PriorityQueue;

public class TimeQueue {
    private final PriorityQueue<Job> data;
    public TimeQueue() {
        data = new PriorityQueue<>();
    }
    public void add(Job element) {
        data.add(element);
    }
    public Job takeirst() {
        return data.poll();
    }

    public void print() {
        System.out.println(data.toString());
    }
}
