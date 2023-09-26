package pl.polsl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.log4j.Log4j2;
import pl.polsl.config.GeneratorConfig;
import pl.polsl.config.GeneratorType;
import pl.polsl.config.MqttConnection;
import pl.polsl.config.Sector;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

@Log4j2
public class ConfigGenerate {
    public static void main(String[] args) throws IOException {
        log.info("START CREATING GENERATOR CONFIG.");

        final ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);;


        final GeneratorConfig generatorConfig = new GeneratorConfig();

        generatorConfig.setType(GeneratorType.TEMP);
        generatorConfig.setExperimentTime(60L);

        final MqttConnection mqttConnection = new MqttConnection();
        mqttConnection.setPort(1883);
        mqttConnection.setBrokerAddress("192.168.31.112");
        mqttConnection.setClientId("generator-v1");
        mqttConnection.setUsername("service");
        mqttConnection.setPassword("master");

        generatorConfig.setMqtt(mqttConnection);
        generatorConfig.setSectors(new ArrayList<>());

        for (int i = 1; i <= 40; i++ ){
            final Sector sector = new Sector();
            sector.setName(String.format("sector-%02d", i));
            int addresses = new Random().nextInt(100);

            Random rand = new Random();
            sector.setInterval(rand.nextInt(2) + rand.nextDouble());
            sector.setNoise(rand.nextInt(2) + rand.nextDouble());


            sector.setAddresses(new ArrayList<>());
            for (int a = 0; a < addresses; a++) {
                sector.getAddresses().add(UUID.randomUUID().toString());
            }

            generatorConfig.getSectors().add(sector);
        }

        final File output = new File("Generator/src/test/resources/config.json");

        if(output.exists()) {
            output.delete();
        }

        output.createNewFile();
        log.info("writing into {}.", output.getAbsolutePath());

        objectMapper.writeValue(output, generatorConfig);

        log.info("GENERATION DONE");
    }
}
