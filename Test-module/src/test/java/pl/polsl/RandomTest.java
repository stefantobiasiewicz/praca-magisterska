package pl.polsl;


import org.junit.jupiter.api.Test;

import java.util.Random;

public class RandomTest {

    @Test
    void test() {
        Random random = new Random(123L);

        for (int i = 0; i< 23; i++) {
            System.out.println(random.nextInt(100));
        }
    }

}
