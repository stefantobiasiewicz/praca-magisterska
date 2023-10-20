package pl.polsl;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"pl.polsl.**", "pl.polsl.comon.repositories"})
@EnableScheduling
@EnableAsync
@EntityScan(basePackages = {"pl.polsl.comon.entites"})
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}