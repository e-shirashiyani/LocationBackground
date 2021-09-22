package ir.hrk.mapproject;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
    private static final String TAG = "MainActivity";


    Button requestLocation, removeLocation;
    MyBackgroundService mService = null;
    boolean mBound = false;
    Context context=this;
    Activity activity=this;

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

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        removeLocation = findViewById(R.id.remove_location_updates_button);
        requestLocation = findViewById(R.id.request_location_updates_button);

        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_BACKGROUND_LOCATION)!=PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED ){

           if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
                    (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                            (context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 2);
                //startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q &&
                    (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                            (context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION}, 2);
                //startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
                    (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                            (context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 2);


                //startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                    (context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    & ActivityCompat.checkSelfPermission
                    (context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                PermissionDialog.showRequestLocationPermissionDialog(context, new PermissionDialog.ICallBackDialogPermission() {
                    @Override
                    public void onAccept() {
                        ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, 555);
                    }

                    @Override
                    public void onDecline() {
                        Toast.makeText(context, "دسترسی لازم داده نشد...", Toast.LENGTH_SHORT).show();
                    }
                });

        }else {
               Log.d(TAG, "onCreate2: ");

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

           }

            setButtonState(Common.requestingLocationUpdate(MainActivity.this));
            bindService(new Intent(MainActivity.this,
                            MyBackgroundService.class),
                    mServiceConnection,
                    Context.BIND_AUTO_CREATE);
        }
/*        Dexter.withActivity(this)
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
                }).check();*/


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

//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull @NotNull String[] permissions, @NonNull @NotNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == 1001 &&
//                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//
//
//    }

    private void gotoPermissionSetting() {
        context.startActivity(new Intent().setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                .setData(Uri.fromParts("package", activity.getPackageName(), null)));
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 || requestCode == 3 || requestCode==2) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                Toast.makeText(mService, "enterrrr", Toast.LENGTH_SHORT).show();
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
            else
                new AlertDialog.Builder(context)
                        .setTitle("عدم اجازه دسترسی")
                        .setMessage("دوباره سعی کنید")
                        .setPositiveButton("ورود به تنظیمات", (dialog, which) ->
                                gotoPermissionSetting()).create().show();
        }
    }



