package pl.polsl.adapter;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pl.polsl.comon.model.MessageType;
import pl.polsl.comon.model.input.HumidityData;
import pl.polsl.comon.model.input.LightData;
import pl.polsl.comon.model.input.TempData;
import pl.polsl.comon.utils.JsonUtils;
import pl.polsl.proccesor.Handler;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class MqttHandler implements MqttCallback {
    private final String broker;
    private final int port;
    private final String username;
    private final String password;
    private final String clientId;

    private final MqttAsyncClient client;

    String regex = "^stream-system/([0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12})/(TEMP|HUMIDITY|LIGHT)$";

    // Kompilacja wyrażenia regularnego
    Pattern pattern = Pattern.compile(regex);

    private final Handler handler;

    public MqttHandler(@Value("${system.mqtt.broker}") final String broker,
                       @Value("${system.mqtt.port}") final int port,
                       @Value("${system.mqtt.username}")  final String username,
                       @Value("${system.mqtt.password}") final String password,
                       @Value("${system.mqtt.clientId}") final String clientId, final Handler handler) {
        this.broker = broker;
        this.port = port;
        this.username = username;
        this.password = password;
        this.clientId = clientId;
        this.handler = handler;
        try {
            client = new MqttAsyncClient(String.format("tcp://%s:%d", this.broker, port), this.clientId,
                    new MemoryPersistence());
            MqttConnectOptions options = new MqttConnectOptions();
            options.setUserName(this.username);
            options.setPassword(this.password.toCharArray());
            options.setAutomaticReconnect(true);
            IMqttToken token = client.connect(options);
            token.waitForCompletion();

            client.subscribe("stream-system/#", 0);

            client.setCallback(this);
        } catch (final MqttException e) {
            final String message = "can 't create mqtt connection.";
            throw new RuntimeException(message);
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        System.out.println("CONNECTION LOST.... reconnecting");
        try {
            MqttConnectOptions options = new MqttConnectOptions();
            options.setUserName(this.username);
            options.setPassword(this.password.toCharArray());
            options.setAutomaticReconnect(true);

            IMqttToken token = client.connect(options);
            token.waitForCompletion();

            client.subscribe("stream-system/#", 0);

            client.setCallback(this);
        } catch (final MqttException e) {
            final String message = "can 't create mqtt connection.";
            throw new RuntimeException(message);
        }
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        Matcher matcher = pattern.matcher(topic);
        if(matcher.matches()) {
            final MessageType type = MessageType.valueOf(matcher.group(2));
            switch (type) {
                case LIGHT -> handler.handle(JsonUtils.MAPPER.readValue(message.getPayload(), LightData.class));
                case HUMIDITY ->  handler.handle(JsonUtils.MAPPER.readValue(message.getPayload(), HumidityData.class));
                case TEMP ->  handler.handle(JsonUtils.MAPPER.readValue(message.getPayload(), TempData.class));
                default -> {
                    System.out.println("mqtt message resolving error");
                }
            }
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }
}
