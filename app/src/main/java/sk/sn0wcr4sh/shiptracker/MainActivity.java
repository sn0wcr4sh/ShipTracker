package sk.sn0wcr4sh.shiptracker;

import android.content.Intent;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private final String TAG = "ShipTracker";

    private P2pManager mP2p;

    private WifiBroadcastReceiver mWifiReceiver;

    private TextView mTvWifiState;
    private TextView mTvShipAddress;

    private Button mBtConnect;

    public void startDiscovery(View v) {
        mP2p.startServiceDiscovery();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        LatLng ship = new LatLng(48.615152, 18.305411);
        googleMap.addMarker(new MarkerOptions().position(ship)
                .title("Ship")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.boat)));

        LatLng phone = new LatLng(48.615347, 18.306976);
        googleMap.addMarker(new MarkerOptions().position(phone)
                .title("Phone")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.phone)));

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ship, 15));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupReferences();

        mP2p = new P2pManager(this, new P2pManager.Listener() {
            @Override
            public void onConnected(WifiP2pInfo info) {
                if (info != null)
                    mTvShipAddress.setText(info.groupOwnerAddress.getHostAddress());
            }
        });

        mWifiReceiver = new WifiBroadcastReceiver(new WifiListener() {
            @Override
            public void onWifiP2pStateChanged(int state) {
                setWifiState(state == WifiP2pManager.WIFI_P2P_STATE_ENABLED);
            }

            @Override
            public void onPeersChanged() {
            }

            @Override
            public void onConnectionChanged(Intent intent) {
                mP2p.getConnectionInfo(intent);
            }
        });

        setWifiState(false);

//        SupportMapFragment mapFragment = (SupportMapFragment)getSupportFragmentManager()
//                .findFragmentById(R.id.map);
//        mapFragment.getMapAsync(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mWifiReceiver, mWifiReceiver.getFilter());
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mWifiReceiver);
    }

    private void setWifiState(boolean state) {
        mTvWifiState.setText(state
                ? R.string.wifi_on
                : R.string.wifi_off);

        mBtConnect.setEnabled(state);
    }

    private void setupReferences() {
        mTvWifiState = (TextView) findViewById(R.id.tvWifiState);
        mTvShipAddress = (TextView) findViewById(R.id.tvShipAddress);
        mBtConnect = (Button)findViewById(R.id.btConnect);
    }
}
