package pl.polsl.job;

import pl.polsl.Rng;
import pl.polsl.comon.config.Sensor;
import pl.polsl.comon.model.MessageType;

import java.util.List;
import java.util.function.Function;

public class Executor {
    private final TimeQueue queue = new TimeQueue();
    private final Function<Job, Job> task;

    public Executor(final MessageType type, final List<Sensor> sensors, final Function<Job, Job> task) {
        this.task = task;

        for (final Sensor sensor : sensors) {
            final Job job = new Job();

            job.setType(type);
            job.setSector(sensor.getSector());
            switch (type) {
                case TEMP:
                    job.setInterval(sensor.getTemperatureInterval());
                    break;
                case LIGHT:
                    job.setInterval(sensor.getLightInterval());
                    break;
                case HUMIDITY:
                    job.setInterval(sensor.getHumidityInterval());
                    break;
                default:
                    throw new RuntimeException("invalid sensor type");
            }

            job.setValue(0.0);
            job.setRng(Rng.nextDouble());

            job.setNextCall((long) (System.currentTimeMillis() + (Rng.nextDouble() * job.getInterval() * 1000)));
            queue.add(job);
        }

        queue.print();
    }

    public void start(final long seconds) throws InterruptedException {
        final long stopTime = seconds * 1000 + System.currentTimeMillis();
        while (true) {
            final Job job = queue.takeirst();

            if (job.getNextCall() > System.currentTimeMillis()) {
                Thread.sleep(job.getNextCall() - System.currentTimeMillis());
            }

            queue.add(task.apply(job));

            if (System.currentTimeMillis() >= stopTime) {
                break;
            }
        }
    }
}
