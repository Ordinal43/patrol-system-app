package com.patrolsystemapp.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

import static android.support.constraint.Constraints.TAG;


public class MqttHelper {
    public MqttAndroidClient mqttAndroidClient;
    final String serverUri = "tcp://broker.shiftr.io:1883";
    final String clientId;
    final ArrayList<String> topics;
    //    final String username = "d893e7e1";
//    final String password = "5d4c1dda9c5e6f90";
    final String username = "patrol_system";
    final String password = "patrol_system";

    public MqttHelper(Context context) throws JSONException {
        SharedPreferences sharedPrefs = context.getSharedPreferences("patrol_app", Context.MODE_PRIVATE);
        clientId = new JSONObject(sharedPrefs.getString("user_object", "")).getString("id");

        Log.d(TAG, "MqttHelper: " + clientId);

        //        list topics
        topics = new ArrayList<>();
        topics.addAll(Arrays.asList(
                "scan", "tes", "wow"
        ));

        mqttAndroidClient = new MqttAndroidClient(context, serverUri, clientId);
        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean b, String s) {
                Log.w("mqtt", s);
            }

            @Override
            public void connectionLost(Throwable throwable) {
                Log.w("error", "connectionLost");
            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                Log.w("Mqtt", mqttMessage.toString());
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        });
        connect();
    }

    public void setCallback(MqttCallbackExtended callback) {
        mqttAndroidClient.setCallback(callback);
    }

    private void connect() {
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(false);
        mqttConnectOptions.setUserName(username);
        mqttConnectOptions.setPassword(password.toCharArray());

        try {

            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {

                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(100);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
                    subscribeToTopics();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.w("Mqtt", "Failed to connect to: " + serverUri + exception.toString());
                }
            });


        } catch (MqttException ex) {
            ex.printStackTrace();
        }
    }

    public void publish(String topic, String message) {
        try {
            mqttAndroidClient.publish(topic, new MqttMessage(message.getBytes()));
        } catch (MqttException ex) {
            System.err.println("Exceptionst publishing");
            ex.printStackTrace();
        }
    }

    private void subscribeToTopics() {
        for (final String topic : topics) {
            try {
                mqttAndroidClient.subscribe(topic, 0, null, new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        Log.w("Mqtt", "Subscribed to " + topic);
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        Log.w("Mqtt", "Subscribed fail!");
                    }
                });

            } catch (MqttException ex) {
                System.err.println("Exceptionst subscribing");
                ex.printStackTrace();
            }
        }
    }

    public void destroy() {
        mqttAndroidClient.unregisterResources();
        mqttAndroidClient.close();
    }
}
