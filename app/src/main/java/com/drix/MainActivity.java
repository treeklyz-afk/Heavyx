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

public class MainActivity extends AppCompatActivity {
    private TextView tvStatus;
    private TextView tvDriverStatus;
    private EditText etPickup, etDrop;
    private Button btnFindDriver;
    private LinearLayout resultLayout;
    private boolean isPatchLoaded = false;

    // Direct automated reference string points to your secure folder path
    private static final String PATCH_URL = "https://raw.githubusercontent.com/treeklyz-afk/Heavyx/main/DXs/libnative-lib.so";
    
    // Core structural mapping declaration targeting native layer definitions
    public native String findDriverNative(String pickup, String drop);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Bind layouts elements cleanly
        tvStatus = findViewById(R.id.sample_text);
        tvDriverStatus = findViewById(R.id.tv_driver_status);
        etPickup = findViewById(findViewById(R.id.et_pickup).getId());
        etDrop = findViewById(findViewById(R.id.et_drop).getId());
        btnFindDriver = findViewById(R.id.btn_find_driver);
        resultLayout = findViewById(R.id.result_layout);

        btnFindDriver.setEnabled(false); // Disable interaction until dynamic patch binds
        downloadAndLoadPatch();

        btnFindDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pickupTxt = etPickup.getText().toString().trim();
                String dropTxt = etDrop.getText().toString().trim();

                if (pickupTxt.isEmpty() || dropTxt.isEmpty()) {
                    resultLayout.setVisibility(View.VISIBLE);
                    tvDriverStatus.setText("Please enter valid route coordinates.");
                    return;
                }

                if (isPatchLoaded) {
                    try {
                        // Forward telemetry string structures directly to online loaded C++ library
                        String dynamicResult = findDriverNative(pickupTxt, dropTxt);
                        resultLayout.setVisibility(View.VISIBLE);
                        tvDriverStatus.setText(dynamicResult);
                    } catch (Throwable t) {
                        tvDriverStatus.setText("Execution linkage error: " + t.getMessage());
                    }
                }
            }
        });
    }

    private void downloadAndLoadPatch() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    File localLib = new File(getFilesDir(), "libnative-lib.so");
                    URL url = new URL(PATCH_URL);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setConnectTimeout(10000);
                    connection.setReadTimeout(10000);
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

                        // Commit verification load context safely
                        System.load(localLib.getAbsolutePath());
                        isPatchLoaded = true;

                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                tvStatus.setText("Secure Patch Engine: Active Connected");
                                btnFindDriver.setEnabled(true);
                            }
                        });
                    } else {
                        updateStatus("Server status offline (HTTP " + connection.getResponseCode() + ")");
                    }
                } catch (final Throwable t) {
                    Log.e("DriderX", "Patch dynamic linking dropped", t);
                    updateStatus("Sync error: Verification infrastructure missing.");
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
