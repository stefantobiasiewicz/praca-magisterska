package pl.polsl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.log4j.Log4j2;
import pl.polsl.comon.config.ConfigGenerator;
import pl.polsl.comon.config.ConfigSectors;
import pl.polsl.comon.config.MqttConnection;
import pl.polsl.comon.config.Sensor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Log4j2
public class ConfigGenerate {
    public static void main(final String[] args) throws IOException {
        log.info("START CREATING GENERATOR CONFIG.");

        final ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);


        final ConfigGenerator configGenerator = new ConfigGenerator();

        configGenerator.setExperimentTime(2000L);


        final MqttConnection mqttConnection = new MqttConnection();
        mqttConnection.setPort(1883);
        mqttConnection.setBrokerAddress("192.168.31.112");
        mqttConnection.setUsername("service");
        mqttConnection.setPassword("master");
        configGenerator.setMqtt(mqttConnection);

        final List<String> sectors = new ArrayList<>();
        for (int i = 1; i <= 1000; i++) {
            sectors.add(UUID.randomUUID().toString());
        }

        double totalInteval = 0, intervalCounts = 0;
        double minOverallInterval = Double.MAX_VALUE;
        double maxOverallInterval = Double.MIN_VALUE;


        configGenerator.setSensors(new ArrayList<>());
        for (final String sector : sectors) {
            final Sensor sensor = new Sensor();

            sensor.setSector(sector);
            final double lightInterval = new Random().nextDouble() * 10 + 5;
            final double humidityInterval = new Random().nextDouble() * 10 + 5;
            final double temperatureInterval = new Random().nextDouble() * 10 + 5;

            minOverallInterval = Math.min(minOverallInterval, lightInterval);
            minOverallInterval = Math.min(minOverallInterval, humidityInterval);
            minOverallInterval = Math.min(minOverallInterval, temperatureInterval);

            maxOverallInterval = Math.max(maxOverallInterval, lightInterval);
            maxOverallInterval = Math.max(maxOverallInterval, humidityInterval);
            maxOverallInterval = Math.max(maxOverallInterval, temperatureInterval);

            sensor.setLightInterval(lightInterval);
            sensor.setHumidityInterval(humidityInterval);
            sensor.setTemperatureInterval(temperatureInterval);


            totalInteval += sensor.getLightInterval() + sensor.getTemperatureInterval() + sensor.getHumidityInterval();
            intervalCounts += 3;

            configGenerator.getSensors().add(sensor);
        }


        final double meanInterval = totalInteval / intervalCounts;

        final File configFile = new File(String.format("Generator/src/test/resources/config-%d.json", sectors.size()));

        if (configFile.exists()) {
            configFile.delete();
        }

        configFile.createNewFile();
        log.info("writing into {}.", configFile.getAbsolutePath());

        objectMapper.writeValue(configFile, configGenerator);


        // save list of sectors
        final ConfigSectors configSectors = new ConfigSectors();
        configSectors.setSectors(sectors);
        configSectors.setMeanInterval(meanInterval);
        configSectors.setMaxInterval(maxOverallInterval);
        configSectors.setMinInterval(minOverallInterval);

        final File sectorFile = new File(String.format("Generator/src/test/resources/sectors-%d.json", sectors.size()));

        if (sectorFile.exists()) {
            sectorFile.delete();
        }

        sectorFile.createNewFile();
        log.info("writing into {}.", sectorFile.getAbsolutePath());

        objectMapper.writeValue(sectorFile, configSectors);

        log.info("GENERATION DONE");
    }
}
