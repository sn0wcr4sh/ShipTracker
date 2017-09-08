package sk.romanbris.shiptracker;

import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;

    private final IntentFilter mIntentFilter = new IntentFilter();
    private WifiBroadcastReceiver mWifiReceiver;

    private TextView mTvWifiState;
    private Button mBtConnect;

    public void setWifiState(boolean state) {
        mTvWifiState.setText(state
            ? R.string.wifi_on
            : R.string.wifi_off);
    }

    public void startConnecting(View v) {
        final Context context = this;

        mBtConnect.setEnabled(false);

        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                mBtConnect.setEnabled(true);
                Toast.makeText(context, "Wifi P2P peer discovery success", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int reason) {
                mBtConnect.setEnabled(true);
                Toast.makeText(context, "Wifi P2P peer discovery failed", Toast.LENGTH_LONG).show();
            }
        });
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

        GetReferences();

        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        mManager = (WifiP2pManager)getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);

        mWifiReceiver = new WifiBroadcastReceiver(this, mManager, mChannel);

//        SupportMapFragment mapFragment = (SupportMapFragment)getSupportFragmentManager()
//                .findFragmentById(R.id.map);
//        mapFragment.getMapAsync(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mWifiReceiver, mIntentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mWifiReceiver);
    }

    private void GetReferences() {
        mTvWifiState = (TextView) findViewById(R.id.tvWifiState);
        mBtConnect = (Button)findViewById(R.id.btConnect);
    }
}
