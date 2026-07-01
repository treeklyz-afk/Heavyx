package com.drix;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;
import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    private TextView tv;
    private static final String PATCH_URL = "https://raw.githubusercontent.com/treeklyz-afk/Heavyx/main/DXs/libnative-lib.so";
    
    public native String stringFromJNI();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv = findViewById(R.id.sample_text);
        tv.setText("Connecting to patch server...");

        downloadAndLoadPatch();
    }

    private void downloadAndLoadPatch() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    File localLib = new File(getFilesDir(), "libnative-lib.so");
                    
                    URL url = new URL(PATCH_URL);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setConnectTimeout(15000);
                    connection.setReadTimeout(15000);
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

                        // Load the dynamic patch safely into internal data context
                        System.load(localLib.getAbsolutePath());

                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    tv.setText(stringFromJNI());
                                } catch (Throwable t) {
                                    tv.setText("JNI Linkage Failed: " + t.getMessage());
                                }
                            }
                        });
                    } else {
                        showError("Patch missing on Git (HTTP " + connection.getResponseCode() + ")");
                    }
                } catch (final Throwable t) {
                    Log.e("DriderX", "Execution catch caught", t);
                    showError("Sync status: " + t.getMessage());
                }
            }
        }).start();
    }

    private void showError(final String msg) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                tv.setText(msg);
            }
        });
    }
}
