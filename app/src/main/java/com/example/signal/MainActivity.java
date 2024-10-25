package com.example.signal;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager; // Import for PackageManager
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Request Permissions
        requestPermissions();

        Button startButton = findViewById(R.id.startButton);
        Button stopButton = findViewById(R.id.stopButton);
        Button changeSignalButton = findViewById(R.id.changeSignalButton);

        // Access EditText for signal input
        EditText signalInput = findViewById(R.id.signalInput);

        startButton.setOnClickListener(v -> {
            Intent serviceIntent = new Intent(MainActivity.this, SensorService.class);
            startService(serviceIntent);
        });

        stopButton.setOnClickListener(v -> {
            Intent serviceIntent = new Intent(MainActivity.this, SensorService.class);
            stopService(serviceIntent);
        });

        changeSignalButton.setOnClickListener(v -> {
            // Get user input (e.g., frequency) from EditText
            String frequencyString = signalInput.getText().toString();

            if (!frequencyString.isEmpty()) {
                int frequency = Integer.parseInt(frequencyString);

                // Add logic to change acoustic signal
                changeAcousticSignal(frequency);  // Define this method to handle signal change
                Toast.makeText(this, "Signal changed to frequency: " + frequency, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Please enter a valid frequency", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
            }
            if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSION_REQUEST_CODE);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (!Environment.isExternalStorageManager()) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                    startActivity(intent);
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permissions Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permissions Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Method to change the acoustic signal frequency (implementation depends on your use case)
    private void changeAcousticSignal(int frequency) {
        // Logic to change the acoustic signal based on frequency
        // For example, you could pass the frequency to a service or audio manager to change the signal
    }
}
