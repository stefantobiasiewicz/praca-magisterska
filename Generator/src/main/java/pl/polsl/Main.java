package pl.polsl;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import pl.polsl.comon.model.input.HumidityData;
import pl.polsl.comon.model.input.LightData;
import pl.polsl.comon.model.input.TempData;
import pl.polsl.config.GeneratorConfig;
import pl.polsl.config.GeneratorType;
import pl.polsl.config.Sector;
import pl.polsl.job.Executor;
import pl.polsl.job.Job;

import java.io.File;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.Random;

public class Main {
    static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        MAPPER.registerModule(new JavaTimeModule());
        MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }



    public static void main(final String[] args) throws IOException, MqttException, InterruptedException {
        final Args arguments = new Args();
        JCommander.newBuilder()
                .addObject(arguments)
                .build()
                .parse(args);

        final ObjectMapper objectMapper = new ObjectMapper();

        final GeneratorConfig generatorConfig = objectMapper.readValue(
                new File(arguments.getConfigFile()),
                GeneratorConfig.class
        );

        sort(generatorConfig);

        final String brokerAddress = generatorConfig.getMqtt().getBrokerAddress();
        final int port = generatorConfig.getMqtt().getPort();
        final IMqttClient client = new MqttClient(String.format("tcp://%s:%s", brokerAddress, port), "generator");

        final MqttConnectOptions options = new MqttConnectOptions();
        final String username = generatorConfig.getMqtt().getUsername();
        final String passwd = generatorConfig.getMqtt().getPassword();
        options.setUserName(username);
        options.setPassword(passwd.toCharArray());

        client.connect(options);

        final Executor executor = new Executor(generatorConfig.getSectors(), job -> {
            System.out.println(job.getName());

            final String address = job.getAddresses();

            final String topic = String.format("stream-system/%s", address);

            final Object data = getData(job, generatorConfig.getType());

            try {
                final String payload = MAPPER.writeValueAsString(data);
                client.publish(topic, payload.getBytes(), 0, false);
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }

            final long interval = (long) (System.currentTimeMillis() + (job.getInterval() * 1000));
            final long nextCll = (long) (interval + (Rng.nextDouble() * (job.getNoise() * 1000)));

            job.setNextCall(nextCll);
            return job;
        });

        System.out.println("GENERATOR-START-WORK.");

        executor.start(generatorConfig.getExperimentTime());

        System.out.println("GENERATOR-END-WORK.");


        client.disconnect();
        client.close();
    }

    private static void sort(final GeneratorConfig generatorConfig) {
        generatorConfig.getSectors().sort(Comparator.comparing(Sector::getName));
        generatorConfig.getSectors().forEach(sector -> sector.getAddresses().sort(String::compareTo));
    }

    private static Object getData(final Job job, final GeneratorType type) {
        final Object data;
        switch (type) {
            case TEMP: {
                final TempData tempData = new TempData();
                tempData.setAddress(job.getAddresses());
                tempData.setTemperature(job.getValue());
                tempData.setDateOfCreation(OffsetDateTime.now());

                data = tempData;
                break;
            }
            case LIGHT: {
                final LightData lightData = new LightData();
                lightData.setAddress(job.getAddresses());
                lightData.setLux(job.getValue());
                lightData.setDateOfCreation(OffsetDateTime.now());

                data = lightData;
                break;
            }
            case HUMIDITY: {
                final HumidityData humidityData = new HumidityData();
                humidityData.setAddress(job.getAddresses());
                humidityData.setHumidity(job.getValue());
                humidityData.setDateOfCreation(OffsetDateTime.now());

                data = humidityData;
                break;
            }
            default:
                throw new RuntimeException("invalid generator type");
        }

        job.setValue(job.getValue() + (Rng.nextDouble() - Rng.nextDouble()) * 10);
        return data;
    }

    @NoArgsConstructor
    public static class Args {
        @Getter
        @Parameter(names = {"-file", "--config-file"}, description = "Path to JSON config file", required = true)
        private String configFile;
    }
}