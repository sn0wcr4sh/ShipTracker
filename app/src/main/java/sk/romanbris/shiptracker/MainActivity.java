package sk.romanbris.shiptracker;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SupportMapFragment mapFragment = (SupportMapFragment)getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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
}
