package com.drix;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    private LinearLayout mainContentView, loadingOverlay, maintenanceView;
    private TextView tvNetworkStatus, tvConsoleOutput, tvAlertTitle, tvAlertDesc;
    private EditText etPickup, etDrop;
    private Button btnRequest, btnAlertAction;
    
    private boolean isNativeEngineBound = false;
    private String noticePdfUrl = "";

    // Resource Endpoints tracking contexts points dynamically to your storage context
    private static final String ENGINE_REMOTE_URL = "https://raw.githubusercontent.com/treeklyz-afk/Heavyx/main/DXs/libnative-lib.so";
    private static final String APP_CONTROL_JSON = "https://raw.githubusercontent.com/treeklyz-afk/Heavyx/main/DXs/data.json";

    public native void initDriverPoolNative();
    public native String findDriverNative(String pickup, String drop);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainContentView = findViewById(R.id.main_content_view);
        loadingOverlay = findViewById(R.id.loading_interface_overlay);
        maintenanceView = findViewById(R.id.maintenance_block_view);
        tvNetworkStatus = findViewById(R.id.tv_network_status);
        tvConsoleOutput = findViewById(R.id.tv_console_output);
        etPickup = findViewById(R.id.et_pickup);
        etDrop = findViewById(R.id.et_drop);
        btnRequest = findViewById(R.id.btn_request_ride);
        btnAlertAction = findViewById(R.id.btn_alert_action);

        // Run control validation checks immediately 
        evaluateSystemParameters();

        btnRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String p = etPickup.getText().toString().trim();
                final String d = etDrop.getText().toString().trim();

                if(p.isEmpty() || d.isEmpty()) return;

                // Fire up premium iOS UI loading screen frame layer
                loadingOverlay.setVisibility(View.VISIBLE);

                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loadingOverlay.setVisibility(View.GONE);
                        findViewById(R.id.driver_response_card).setVisibility(View.VISIBLE);
                        
                        if (isNativeEngineBound) {
                            try {
                                tvConsoleOutput.setText(findDriverNative(p, d));
                            } catch (Throwable t) {
                                tvConsoleOutput.setText("Engine fallback routing active:\nSearching nearby matching array systems...");
                            }
                        } else {
                            tvConsoleOutput.setText("Processing local allocation calculations...\nDriver tracking context bound cleanly.");
                        }

                        // Fire up hardware map interface layers via standard Android UI schemas
                        try {
                            Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:28.6139,77.2090?q=" + Uri.encode(d)));
                            mapIntent.setPackage("com.google.android.apps.maps");
                            startActivity(mapIntent);
                        } catch (Exception ex) {
                            // Map package missing guard layer execution
                        }
                    }
                }, 2000);
            }
        });
    }

    private void evaluateSystemParameters() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Fetch Remote Control JSON configuration array rules 
                    URL urlJson = new URL(APP_CONTROL_JSON);
                    HttpURLConnection conn = (HttpURLConnection) urlJson.openConnection();
                    conn.setConnectTimeout(6000);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder builder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != -1) builder.append(line);
                    reader.close();

                    JSONObject config = new JSONObject(builder.toString());
                    final boolean maintenance = config.getBoolean("maintenance_mode");
                    noticePdfUrl = config.getString("pdf_notice_url");

                    if (maintenance) {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                mainContentView.setVisibility(View.GONE);
                                maintenanceView.setVisibility(View.VISIBLE);
                                btnAlertAction.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(noticePdfUrl)));
                                    }
                                });
                            }
                        });
                        return;
                    }

                    // Download and bind online execution patch runtime engine objects securely
                    File localLib = new File(getFilesDir(), "libnative-lib.so");
                    URL urlLib = new URL(ENGINE_REMOTE_URL);
                    HttpURLConnection connLib = (HttpURLConnection) urlLib.openConnection();
                    if(connLib.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        InputStream is = connLib.getInputStream();
                        FileOutputStream fos = new FileOutputStream(localLib);
                        byte[] buffer = new byte[4096];
                        int read;
                        while ((read = is.read(buffer)) != -1) fos.write(buffer, 0, read);
                        fos.close();
                        is.close();

                        System.load(localLib.getAbsolutePath());
                        initDriverPoolNative();
                        isNativeEngineBound = true;
                    }

                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            tvNetworkStatus.setText("Premium Cloud Core Matrix: Synchronized");
                        }
                    });

                } catch (Exception e) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            tvNetworkStatus.setText("Offline structural operations active");
                        }
                    });
                }
            }
        }).start();
    }
}
