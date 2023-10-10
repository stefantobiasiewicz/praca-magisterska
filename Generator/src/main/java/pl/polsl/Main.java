package pl.polsl;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import pl.polsl.comon.config.ConfigGenerator;
import pl.polsl.comon.config.Experiment;
import pl.polsl.comon.config.TestPlan;
import pl.polsl.comon.model.input.HumidityData;
import pl.polsl.comon.model.input.LightData;
import pl.polsl.comon.model.input.TempData;
import pl.polsl.comon.utils.FileNames;
import pl.polsl.comon.utils.JsonUtils;
import pl.polsl.comon.model.MessageType;
import pl.polsl.job.Executor;
import pl.polsl.job.Job;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Comparator;

public class Main {
     public static void main(final String[] args) throws IOException, MqttException, InterruptedException {
        final Args arguments = new Args();
        JCommander.newBuilder()
                .addObject(arguments)
                .build()
                .parse(args);

        final ObjectMapper objectMapper = new ObjectMapper();


         final Experiment experiment = JsonUtils.MAPPER.readValue(new File(arguments.getTestPlanDir(), FileNames.EXPERIMENT),
                 Experiment.class);

         final TestPlan testPlan = JsonUtils.MAPPER.readValue(new File(new File(arguments.getTestPlanDir(),
                         experiment.getTest()), FileNames.EXPERIMENT_CONFIG),
                 TestPlan.class);

         final ConfigGenerator configGenerator = JsonUtils.MAPPER.readValue(new File(new File(arguments.getTestPlanDir(),
                         experiment.getTest()), testPlan.getGeneratorConfig()),
                 ConfigGenerator.class);


        final String brokerAddress = configGenerator.getMqtt().getBrokerAddress();
        final int port = configGenerator.getMqtt().getPort();
        final IMqttClient client = new MqttClient(String.format("tcp://%s:%s", brokerAddress, port),
                String.format("generator-%s", arguments.getType()));

        final MqttConnectOptions options = new MqttConnectOptions();
        final String username = configGenerator.getMqtt().getUsername();
        final String passwd = configGenerator.getMqtt().getPassword();
        options.setUserName(username);
        options.setPassword(passwd.toCharArray());

        client.connect(options);

        final Executor executor = new Executor(arguments.getType(), configGenerator.getSensors(), job -> {
            System.out.println(job.getSector());

            final String address = job.getSector();

            final String topic = String.format("stream-system/%s/%s", address, arguments.getType());

            final Object data = getData(job, arguments.getType());

            try {
                final String payload = JsonUtils.MAPPER.writeValueAsString(data);
                client.publish(topic, payload.getBytes(), 0, false);
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }

            final long interval = (long) (System.currentTimeMillis() + (job.getInterval() * 1000));

            job.setNextCall(interval);
            return job;
        });

        System.out.println("GENERATOR-START-WORK.");

        executor.start(configGenerator.getExperimentTime());

        System.out.println("GENERATOR-END-WORK.");


        client.disconnect();
        client.close();
    }

    private static Object getData(final Job job, final MessageType type) {
        final Object data;
        switch (type) {
            case TEMP: {
                final TempData tempData = new TempData();
                tempData.setSector(job.getSector());
                tempData.setTemperature(job.getValue());
                tempData.setDateOfCreation(LocalDateTime.now());

                data = tempData;
                break;
            }
            case LIGHT: {
                final LightData lightData = new LightData();
                lightData.setSector(job.getSector());
                lightData.setLux(job.getValue());
                lightData.setDateOfCreation(LocalDateTime.now());

                data = lightData;
                break;
            }
            case HUMIDITY: {
                final HumidityData humidityData = new HumidityData();
                humidityData.setSector(job.getSector());
                humidityData.setHumidity(job.getValue());
                humidityData.setDateOfCreation(LocalDateTime.now());

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
        @Parameter(names = {"-dir", "--config-dir"}, description = "Path to test plan dir", required = true)
        private File testPlanDir;
        @Getter
        @Parameter(names = {"-type", "--type"}, description = "type of generator [TEMP, HUMIDITY, LIGHT]", required =
                true)
        private MessageType type;
    }
}