package com.home.smart;

import androidx.appcompat.app.AppCompatActivity;

import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.UnsupportedEncodingException;

public class MainActivity extends AppCompatActivity {

    private Switch swLight1;
    private Switch swLight2;
    private TextView txtView1;
    private SeekBar dimmer1;
    private MqttAndroidClient client;
    private String clientId = MqttClient.generateClientId();
    private static final String SERVER_URI = "tcp://192.168.1.37:1883";
    private static final String TOPIC1 = "/home/ESP32/light/";
    private static final String TOPIC2 = "/home/ESP32/dimmer1/";
    private static final String TOPIC3 = "/home/ESP32/sensor1/temperature/";
    private static final String TOPIC4 = "/home/ESP32/sensor1/humidity/";
    private static final String TOPIC5 = "/home/ESP32/sensor1/heatindex/";
    private static final int QOS = 1;
    private static final String TAG = "Mqtt";
    private String message1;
    private IMqttToken subToken;
    private TextView tvTemperature;
    private TextView tvHeatIndex;
    private TextView tvHumidity;
    private TextView tvTimeRefresh;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        swLight1 = (Switch) findViewById(R.id.light1);
        swLight2 = (Switch) findViewById(R.id.light2);
        txtView1 =  (TextView) findViewById(R.id.txtView1);
        dimmer1 = (SeekBar) findViewById(R.id.dimmer1);
        tvTemperature = (TextView) findViewById(R.id.tvTemperature);
        tvHeatIndex = (TextView) findViewById(R.id.tvHeatIndex);
        tvHumidity = (TextView) findViewById(R.id.tvHumidity);
        tvTimeRefresh = (TextView) findViewById(R.id.tvTimeRefresh);

        client = new MqttAndroidClient(this.getApplicationContext(), SERVER_URI,
                        clientId);
        Log.d(TAG, "cliente creado");

        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                System.out.println("Connection was lost!");

            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                System.out.println("Message Arrived!: " + topic + ": " + new String(message.getPayload()));
                message1 = new String(message.getPayload());
                txtView1.setText("Topic:  " + topic + "    " + "\nMessage:  " + new String(message.getPayload()));

                if (topic.equals(TOPIC1)) {

                    if (message.toString().compareTo("on1") == 0) {
                        swLight1.setChecked(true);
                    }

                    if (message.toString().compareTo("off1") == 0) {
                        swLight1.setChecked(false);
                    }
                }

                if (topic.equals(TOPIC3)){
                    tvTemperature.setText(message1 + "°C");
                    Calendar c = Calendar.getInstance();
                    System.out.println("Current time => "+c.getTime());
                    SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                    String formattedDate = df.format(c.getTime());
                    tvTimeRefresh.setText("Ultima Actualización:  " + formattedDate);

                }
                if (topic.equals(TOPIC4)){
                    tvHumidity.setText("Humedad " + message1 + "%");
                }
                if (topic.equals(TOPIC5)){
                    tvHeatIndex.setText("ST " + message1 + "°C");
                }

            }


            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                System.out.println("Delivery Complete!");
            }
        });


        try {
            client.connect(this.getApplicationContext(), new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    System.out.println("Connection Success!");
                    try {
                        client.subscribe(TOPIC1, QOS);
                        System.out.println("Subscribed to " + TOPIC1);
                        System.out.println("Publishing message..");
                        client.publish(TOPIC1, new MqttMessage("Hello world testing..!".getBytes()));
                        client.subscribe(TOPIC3, QOS);
                        System.out.println("Subscribed to " + TOPIC3);
                        client.subscribe(TOPIC4, QOS);
                        System.out.println("Subscribed to " + TOPIC4);
                        client.subscribe(TOPIC5, QOS);
                        System.out.println("Subscribed to " + TOPIC5);
                    } catch (MqttException ex) {
                    }

                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    System.out.println("Connection Failure!");
                }
            });
        } catch (MqttException ex) {

        }


        swLight1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked == true){


                    String payload = "on1";
                    byte[] encodedPayload = new byte[0];
                    try {
                        encodedPayload = payload.getBytes("UTF-8");
                        MqttMessage message = new MqttMessage(encodedPayload);
                        client.publish(TOPIC1, message);
                    } catch (UnsupportedEncodingException | MqttException e) {
                        e.printStackTrace();
                    }

                }
                else {

                    String payload = "off1";
                    byte[] encodedPayload = new byte[0];
                    try {
                        encodedPayload = payload.getBytes("UTF-8");
                        MqttMessage message = new MqttMessage(encodedPayload);
                        client.publish(TOPIC1, message);
                    } catch (UnsupportedEncodingException | MqttException e) {
                        e.printStackTrace();
                    }

                }
            }
        });

        swLight2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked == true){
                    String payload = "on2";
                    byte[] encodedPayload = new byte[0];
                    try {
                        encodedPayload = payload.getBytes("UTF-8");
                        MqttMessage message = new MqttMessage(encodedPayload);
                        client.publish(TOPIC1, message);
                    } catch (UnsupportedEncodingException | MqttException e) {
                        e.printStackTrace();
                    }
                }

                else {
                    String payload = "off2";
                    byte[] encodedPayload = new byte[0];
                    try {
                        encodedPayload = payload.getBytes("UTF-8");
                        MqttMessage message = new MqttMessage(encodedPayload);
                        client.publish(TOPIC1, message);
                    } catch (UnsupportedEncodingException | MqttException e) {
                        e.printStackTrace();
                    }
                }
            }
        });



        dimmer1.setProgress(0);
        dimmer1.setMax(255);
        dimmer1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                try {
                    client.publish(TOPIC2, new MqttMessage(Integer.toString(progress).getBytes()));
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });










    }




}
