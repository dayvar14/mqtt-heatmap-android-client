package com.example.heatmap

import android.content.Context
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.maps.android.heatmaps.WeightedLatLng
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import org.json.JSONArray
import java.util.*


class MqttConnector(context: Context) {
    private lateinit var mqttAndroidClient: MqttAndroidClient

    val mqttHost = "tcp://mqtt.eclipse.org"
    val mqttPort = 1883
    val mqttUri = "tcp://mqtt.eclipse.org:1883"

    val clientId = MqttClient.generateClientId()
    val subscriptionTopic = "testing/heatmap/coordinates"
    val publishTopic = "testing/heatmap/user"

    init{
        mqttAndroidClient = MqttAndroidClient(context,mqttUri,clientId)
        mqttAndroidClient.setCallback(object : MqttCallbackExtended {
            override fun connectComplete(b: Boolean, s: String) {
                Log.w("mqtt", s)
            }

            override fun connectionLost(throwable: Throwable) {}
            @Throws(Exception::class)

            override fun messageArrived(topic: String, mqttMessage: MqttMessage) {

                //Get jsonString from message
                val jsonString: String = mqttMessage.toString();

                val gson = Gson()
                val coordinateArray = gson.fromJson(jsonString, Array<Coordinate>::class.java)
                var list = mutableListOf<WeightedLatLng>()
                val iterator = coordinateArray.iterator();
                while(iterator.hasNext()){
                    val coordinate: Coordinate = iterator.next();
                    list.add(WeightedLatLng(LatLng(coordinate.lat, coordinate.lng),coordinate.count));
                }
                Log.w("MQTT Coords","Received ${list.size} coordinates...")
                Coordinates.coordinates.postValue(list)
            }

            override fun deliveryComplete(iMqttDeliveryToken: IMqttDeliveryToken) {}
        })

        connect(context)
    }

    fun connect(context: Context){
        val mqttConnectOptions = MqttConnectOptions()
        mqttConnectOptions.isAutomaticReconnect = true
        mqttConnectOptions.isCleanSession = false

        try {
            mqttAndroidClient.connect(mqttConnectOptions, context, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    val disconnectedBufferOptions = DisconnectedBufferOptions()
                    disconnectedBufferOptions.isBufferEnabled = true
                    disconnectedBufferOptions.bufferSize = 100
                    disconnectedBufferOptions.isPersistBuffer = false
                    disconnectedBufferOptions.isDeleteOldestMessages = false
                    mqttAndroidClient.setBufferOpts(disconnectedBufferOptions)
                    subscribe(context)
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.w("Mqtt", "Failed to connect to: ${mqttUri} ${exception.toString()}")
                }
            })
        } catch (ex:MqttException){
            ex.printStackTrace()
        }


    }

    fun subscribe(context: Context){
        try{
            mqttAndroidClient.subscribe(subscriptionTopic, 0, context, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.w("Mqtt","Subscribed!")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.w("Mqtt", "Subscribed fail!")
                }
            })
        } catch (ex : MqttException){
            print("Exception")
            ex.printStackTrace()
        }
    }



}