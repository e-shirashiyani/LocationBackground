package ir.hrk.mapproject;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.jetbrains.annotations.NotNull;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {


    LocationManager locationManager;

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        if (supportMapFragment != null) {
            supportMapFragment.getMapAsync(this);
        }

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1001);
        } else {
            startService(new Intent(this,LocationService.class));
        }


       /* if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
            Toast.makeText(this,"on",Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this,"off",Toast.LENGTH_SHORT).show();
        }*/




    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull @NotNull String[] permissions, @NonNull @NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1001 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                grantResults[1] == PackageManager.PERMISSION_GRANTED){
            startService(new Intent(this,LocationService.class));
        }
    }

    @Override
    public void onMapReady(@NonNull @org.jetbrains.annotations.NotNull GoogleMap googleMap) {

        LatLng myLocation = new LatLng(35.681156, 51.385098);
        googleMap.addMarker(new MarkerOptions().position(myLocation).title("kamali")).setSnippet("roberooye bashgah farhangian");
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 15.5f));
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setZoomGesturesEnabled(true);
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        /*Toast.makeText(this, "lat : " + location.getLatitude() + "lag : " + location.getLongitude(), Toast.LENGTH_SHORT).show();*/
        Log.i("loc",location.getLatitude()+"");
    }

    @Override
    public void onFlushComplete(int requestCode) {
        /*Toast.makeText(this,"flush : " + requestCode,Toast.LENGTH_SHORT).show();*/
        Log.i("flush",requestCode+"");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.i("status",provider + " : " + status);
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        Log.i("provider_enable",provider );

    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        Log.i("provider_disable",provider );
    }
}