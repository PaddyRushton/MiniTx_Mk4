package mini.tx.mk4;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkSpecifier;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import android.widget.ToggleButton;

public class WifiManagerActivity extends AppCompatActivity {
    Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_manager);

        ToggleButton wifiToggleButton = findViewById(R.id.wifiToggleButton);
        wifiToggleButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // The toggle is enabled
                wifiConnect(1);
            } else {
                // The toggle is disabled
                wifiConnect(0);
            }
        });
    }

    public void wifiConnect(int toggleStatus) {
        Log.d("myTag", "Connect button pressed");

// Connect to wifi
        WifiNetworkSpecifier specifier = new WifiNetworkSpecifier.Builder()
                .setSsid("minirx")
                .setWpa2Passphrase("123456789")
                .build();

        NetworkRequest request = new NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI) // we want WiFi
                .setNetworkSpecifier(specifier) // we want _our_ network
                .build();

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        ConnectivityManager.NetworkCallback networkCallback = new
                ConnectivityManager.NetworkCallback() {
                    @Override
                    public void onAvailable(Network miniRxNetwork) {
                        super.onAvailable(miniRxNetwork);
                        Log.d("myTag", "onAvailable:" + miniRxNetwork);
                        connectivityManager.bindProcessToNetwork(miniRxNetwork);
                        LinkProperties linkProperties = connectivityManager.getLinkProperties(miniRxNetwork);
                        Log.d("myTag", "Link Properties: " + linkProperties);

                    }
                };

        if (toggleStatus == 1) {
            connectivityManager.requestNetwork(request, networkCallback);
            Log.d("myTag", "Connect button toggled on");
        }

        if (toggleStatus == 0) {
            Log.d("myTag", "Connect button toggled off");
            try {
                connectivityManager.unregisterNetworkCallback(networkCallback);
                Log.d("myTag", "unregister network called");
            } catch (Exception exception) {
                Log.d("could not unregister network callback", String.valueOf(exception));
            }
        }

    }

/*
    //deprecated unfortunately but I can't figure out how to do it properly for Android API 33
    //public void wifiCheck() {
        // on below line we are creating a variable for connectivity manager and initializing it.
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        // on below line we are getting network info to get wifi network info.
        NetworkInfo wifiConnection = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        // on below line displaying toast message when wi-fi is connected when wi-fi is disconnected
        if (wifiConnection.isConnected()) {

            WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            int ipAddr = wifiInfo.getIpAddress();
            Toast.makeText(WifiManagerActivity.this, "Wi-Fi is connected... AP IP Address is:" + ipAddr, Toast.LENGTH_SHORT).show();

        } else {
            Toast.makeText(WifiManagerActivity.this, "Wi-Fi is disconnected..", Toast.LENGTH_SHORT).show();
        }

    }
 */
}