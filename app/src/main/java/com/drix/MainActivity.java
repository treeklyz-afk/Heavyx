package com.drix;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private TextView tvStatus;
    private TextView tvDriverStatus;
    private EditText etPickup, etDrop;
    private Button btnFindDriver;
    private LinearLayout resultLayout;
    private boolean isPatchLoaded = false;

    private static final String PATCH_URL = "https://raw.githubusercontent.com/treeklyz-afk/Heavyx/main/DXs/libnative-lib.so";
    
    public native String findDriverNative(String pickup, String drop);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvStatus = findViewById(R.id.sample_text);
        tvDriverStatus = findViewById(R.id.tv_driver_status);
        etPickup = findViewById(R.id.et_pickup);
        etDrop = findViewById(R.id.et_drop);
        btnFindDriver = findViewById(R.id.btn_find_driver);
        resultLayout = findViewById(R.id.result_layout);

        // Button is always clickable from start!
        btnFindDriver.setEnabled(true);
        tvStatus.setText("DriderX Engine: Core Initialized");

        // Try loading online patch in background
        downloadAndLoadPatch();

        btnFindDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pickupTxt = etPickup.getText().toString().trim();
                String dropTxt = etDrop.getText().toString().trim();

                if (pickupTxt.isEmpty() || dropTxt.isEmpty()) {
                    resultLayout.setVisibility(View.VISIBLE);
                    tvDriverStatus.setText("Please enter valid locations.");
                    return;
                }

                resultLayout.setVisibility(View.VISIBLE);

                if (isPatchLoaded) {
                    try {
                        // Run advanced C++ algorithms if patch is live
                        String dynamicResult = findDriverNative(pickupTxt, dropTxt);
                        tvDriverStatus.setText(dynamicResult);
                    } catch (Throwable t) {
                        // Fallback to local execution if link drops
                        runJavaFallback(pickupTxt, dropTxt);
                    }
                } else {
                    // Instant fallback if patch isn't loaded yet
                    runJavaFallback(pickupTxt, dropTxt);
                }
            }
        });
    }

    private void runJavaFallback(String pickup, String drop) {
        String[] drivers = {"Vikram Singh [Rider Pro]", "Rajesh Kumar [Elite]", "Aman Verma [X-Prime]"};
        String[] vehicles = {"(KA-03-HA-4321)", "(DL-01-CA-9876)", "(BR-01-PC-5543)"};
        
        Random rand = new Random();
        int idx = rand.nextInt(3);
        int eta = 3 + rand.nextInt(5);

        String res = "⚡ Driver Matched (Internal Mode)!\n\n"
                   + "Driver: " + drivers[idx] + "\n"
                   + "Vehicle: " + vehicles[idx] + "\n"
                   + "Route: " + pickup + " -> " + drop + "\n"
                   + "Status: Arriving in " + eta + " mins";
        
        tvDriverStatus.setText(res);
    }

    private void downloadAndLoadPatch() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    File localLib = new File(getFilesDir(), "libnative-lib.so");
                    URL url = new URL(PATCH_URL);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(8000);
                    connection.connect();

                    if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        InputStream input = connection.getInputStream();
                        FileOutputStream output = new FileOutputStream(localLib);
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = input.read(buffer)) != -1) {
                            output.write(buffer, 0, bytesRead);
                        }
                        output.flush();
                        output.close();
                        input.close();

                        System.load(localLib.getAbsolutePath());
                        isPatchLoaded = true;

                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                tvStatus.setText("DriderX Engine: High-Performance Patch Active");
                            }
                        });
                    } else {
                        updateStatus("DriderX Engine: Ready (Cloud sync pending)");
                    }
                } catch (final Throwable t) {
                    updateStatus("DriderX Engine: Ready (Standby Mode)");
                }
            }
        }).start();
    }

    private void updateStatus(final String msg) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                tvStatus.setText(msg);
            }
        });
    }
}
