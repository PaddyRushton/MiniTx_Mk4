package mini.tx.mk4;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkSpecifier;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class WifiManagerActivity extends AppCompatActivity {

    private WifiManager wifiManager;
    private TextView wifiStatusTextView;
    private Button scanButton;
    private String scanButtonText = "Press to Rescan";

    private Handler handler;
    private Runnable revertButtonTask;

    private boolean openWifiSettingsOnNextPress = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_manager);
        wifiStatusTextView = findViewById(R.id.wifiStatusTextView);
        scanButton = findViewById(R.id.scanButton);
        scanButton.setText(scanButtonText);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (openWifiSettingsOnNextPress) {
                    openWifiSettings();
                    openWifiSettingsOnNextPress = false;
                } else {
                    scanWifiNetworks();
                }
            }
        });

        handler = new Handler();
        revertButtonTask = new Runnable() {
            @Override
            public void run() {
                scanButtonText = "Press to Rescan";
                scanButton.setText(scanButtonText);
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(wifiScanReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        scanWifiNetworks();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(wifiScanReceiver);
    }

    private void scanWifiNetworks() {
        boolean success = wifiManager.startScan();
        if (success) {
            scanButtonText = "Scanning Wi-Fi networks...";
            scanButton.setText(scanButtonText);
            handler.removeCallbacks(revertButtonTask); // Remove any pending callbacks
            // Delay in milliseconds
            int REVERT_DELAY = 1000;
            handler.postDelayed(revertButtonTask, REVERT_DELAY);
        } else {
            scanButtonText = "Press again to force refresh.";
            scanButton.setText(scanButtonText);
            openWifiSettingsOnNextPress = true;
        }
    }

    // Open Android OS WiFi Settings to force a cheeky refresh..
    private void openWifiSettings() {
        Intent wifiIntent = new Intent(Settings.ACTION_WIFI_SETTINGS);
        startActivity(wifiIntent);
    }

    private void connectToMinirxNetwork(String password) {
        WifiNetworkSpecifier specifier = new WifiNetworkSpecifier.Builder()
                .setSsid("minirx")
                .setWpa2Passphrase(password)
                .build();

        NetworkRequest networkRequest = new NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .setNetworkSpecifier(specifier)
                .build();

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                super.onAvailable(network);
                // Network is available, do your work here
                wifiStatusTextView.setText("Wi-Fi network 'minirx' connected!");
            }

            @Override
            public void onUnavailable() {
                super.onUnavailable();
                // Network is unavailable or connection failed
                wifiStatusTextView.setText("Wi-Fi network 'minirx' not found");
                // Unregister the network callback
                connectivityManager.unregisterNetworkCallback(this);
            }
        };

        connectivityManager.requestNetwork(networkRequest, networkCallback);
    }

    private final BroadcastReceiver wifiScanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            List<ScanResult> scanResults = wifiManager.getScanResults();
            boolean minirxAvailable = false;

            for (ScanResult scanResult : scanResults) {
                if (scanResult.SSID.equals("minirx")) {
                    minirxAvailable = true;
                    break;
                }
            }

            if (minirxAvailable) {
                // Connect to minirx network
                connectToMinirxNetwork("123456789");
                wifiStatusTextView.setText("Wi-Fi network 'minirx' available!");



            } else {
                wifiStatusTextView.setText("Wi-Fi network 'minirx' not found.");
            }
        }
    };
}