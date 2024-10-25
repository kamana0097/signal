package com.example.signal;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioTrack;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class SensorService extends Service implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor accelerometer, gyroscope, magnetometer;
    private FileWriter fileWriter;
    private AudioTrack audioTrack;

    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize sensors
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        // Register sensors
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_NORMAL);

        // Initialize CSV file for storing data
        try {
            File file = new File(getExternalFilesDir(null), "sensor_data.csv");
            fileWriter = new FileWriter(file);
            fileWriter.write("Timestamp,Accelerometer_X,Accelerometer_Y,Accelerometer_Z,Gyroscope_X,Gyroscope_Y,Gyroscope_Z,Magnetometer_X,Magnetometer_Y,Magnetometer_Z\n");
        } catch (IOException e) {
            Log.e("SensorService", "Error creating file", e);
        }

        // Start playing the acoustic signal (example: sine wave at 440Hz)
        startAcousticSignal();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    private void startAcousticSignal() {
        int sampleRate = 44100;
        int numSamples = sampleRate * 2;
        double freqOfTone = 440; // A4
        double[] sample = new double[numSamples];
        byte[] generatedSound = new byte[2 * numSamples];

        // Fill sample buffer
        for (int i = 0; i < numSamples; ++i) {
            sample[i] = Math.sin(2 * Math.PI * i / (sampleRate / freqOfTone));
        }

        // Convert to 16 bit pcm sound array
        int idx = 0;
        for (final double dVal : sample) {
            final short val = (short) ((dVal * 32767));
            generatedSound[idx++] = (byte) (val & 0x00ff);
            generatedSound[idx++] = (byte) ((val & 0xff00) >>> 8);
        }

        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, generatedSound.length, AudioTrack.MODE_STATIC);
        audioTrack.write(generatedSound, 0, generatedSound.length);
        audioTrack.play();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        long timestamp = System.currentTimeMillis();
        try {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                fileWriter.write(timestamp + "," + event.values[0] + "," + event.values[1] + "," + event.values[2] + ",,,,\n");
            } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                fileWriter.write(timestamp + ",,," + event.values[0] + "," + event.values[1] + "," + event.values[2] + ",,,\n");
            } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                fileWriter.write(timestamp + ",,,,," + event.values[0] + "," + event.values[1] + "," + event.values[2] + "\n");
            }
        } catch (IOException e) {
            Log.e("SensorService", "Error writing to file", e);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    public void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(this);
        try {
            fileWriter.close();
        } catch (IOException e) {
            Log.e("SensorService", "Error closing file", e);
        }
        if (audioTrack != null) {
            audioTrack.release();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
