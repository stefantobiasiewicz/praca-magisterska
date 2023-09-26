package pl.polsl.job;

import pl.polsl.Rng;
import pl.polsl.config.Sector;

import java.util.List;
import java.util.function.Function;

public class Executor {
    private final TimeQueue queue = new TimeQueue();
    private final Function<Job, Job> task;
    public Executor(List<Sector> sectors, Function<Job, Job> task) {
        this.task = task;

        for (final Sector sector : sectors) {
            for (final String address: sector.getAddresses()) {
                final Job job = new Job();

                job.setName(String.format("%s-%s", sector.getName(), address));
                job.setAddresses(address);
                job.setInterval(sector.getInterval());
                job.setNoise(sector.getNoise());
                job.setValue(10.0);
                job.setRng(Rng.nextDouble());
                job.setNextCall((long) (System.currentTimeMillis() + (Rng.nextDouble() * job.getInterval() * 1000) + (Rng.nextDouble() * job.getNoise() * 1000)));
                queue.add(job);
            }
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
