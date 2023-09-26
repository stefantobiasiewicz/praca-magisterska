package pl.polsl;

import java.util.Random;

public final class Rng {

    static final Random RANDOM = new Random(0);

    public static double nextDouble() {
        double rng = RANDOM.nextDouble();
        System.out.println(rng);
        return rng;
    }
}
