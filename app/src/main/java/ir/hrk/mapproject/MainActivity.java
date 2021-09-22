package ir.hrk.mapproject;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {



    Button requestLocation, removeLocation;
    MyBackgroundService mService = null;
    boolean mBound = false;

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MyBackgroundService.LocalBinder binder = (MyBackgroundService.LocalBinder) service;
            mService = binder.getSrvice();
            mBound = true;

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mBound = false;

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Dexter.withActivity(this)
                .withPermissions(Arrays.asList(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                ))
                .withListener(new MultiplePermissionsListener() {

                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                        removeLocation = findViewById(R.id.remove_location_updates_button);
                        requestLocation = findViewById(R.id.request_location_updates_button);

                        requestLocation.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mService.requestLocationUpdate();
                            }
                        });

                        removeLocation.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                               mService.removeLocationUpdate();
                            }
                        });

                        setButtonState(Common.requestingLocationUpdate(MainActivity.this));
                        bindService(new Intent(MainActivity.this,
                                        MyBackgroundService.class),
                                mServiceConnection,
                                Context.BIND_AUTO_CREATE);

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {

                    }
                }).check();


    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(Common.KEY_REQUEST_LOCATION_UPDATE)) {
            setButtonState(sharedPreferences.getBoolean(Common.KEY_REQUEST_LOCATION_UPDATE, false));
        }
    }

    private void setButtonState(boolean isRequestEnable) {
        if (isRequestEnable) {
            requestLocation.setEnabled(false);
            removeLocation.setEnabled(true);
        } else {
            requestLocation.setEnabled(true);
            removeLocation.setEnabled(false);
        }


    }

    @Override
    protected void onStart() {
        super.onStart();
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this::onSharedPreferenceChanged);
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        if (mBound) {
            unbindService(mServiceConnection);
            mBound = false;

        }
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this::onSharedPreferenceChanged);
        EventBus.getDefault().unregister(this);

        super.onStop();

    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onListenLocation(SendLocationToActivity event) {

        if (event!=null)
        {
            String data=new StringBuilder()
                    .append(event.getLocation().getLatitude())
                    .append("/")
                    .append(event.getLocation().getLongitude())
                    .toString();
            Toast.makeText(mService, data, Toast.LENGTH_SHORT).show();
        }
    }
}


