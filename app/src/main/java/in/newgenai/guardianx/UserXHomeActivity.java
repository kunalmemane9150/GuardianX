package in.newgenai.guardianx;

import static android.location.LocationManager.GPS_PROVIDER;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import in.newgenai.guardianx.Fragment.CallFragment;
import in.newgenai.guardianx.Fragment.ChatFragment;
import in.newgenai.guardianx.Fragment.MapFragment;
import in.newgenai.guardianx.Fragment.ProfileFragment;
import in.newgenai.guardianx.Slider.SliderAdapter;
import in.newgenai.guardianx.Slider.SliderItem;
import in.newgenai.guardianx.databinding.ActivityMainBinding;
import in.newgenai.guardianx.databinding.ActivityUserXhomeBinding;

public class UserXHomeActivity extends AppCompatActivity implements LocationListener, SensorEventListener {

    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;
    private ActivityUserXhomeBinding binding;


    private double latitude, longitude;
    private LocationManager locationManager;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private String gPhone;

    //permissions
    ActivityResultLauncher<String[]> activityResultLauncher;
    private boolean isRecordPermissionGranted = false;
    private boolean isSMSPermissionGranted = false;
    private boolean isCallPermissionGranted = false;

    //AlertDialogBox
    private ImageButton cancelBtn;
    private Button okBtn;
    private AlertDialog dialog;

    //Shake Feature
    private SensorManager sensorManager;
    private Sensor sensor;
    private long lastUpdate, actualTime;
    //vibrate
    private Vibrator vibrator;

    //image slider
    private ViewPager2 viewPager2;
    private Handler sliderHandler = new Handler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserXhomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.bottomNavigationView.setBackground(null);

        init();
        onClickListener();
//        checkPermissions();
        imageSlider();
        requestPermissions();
        replaceFragment(new MapFragment());

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.location) {
                replaceFragment(new MapFragment());
                binding.fragmentTitle.setText("Track Me");
                binding.fragmentTitleDesc.setText("Share live location with your Guardian");
                binding.topView2.setVisibility(View.GONE);
                binding.topView.setVisibility(View.VISIBLE);
                binding.viewPagerImageSlider.setVisibility(View.VISIBLE);

            } else if (itemId == R.id.call) {
                replaceFragment(new CallFragment());
                binding.fragmentTitle.setText("Call For Help");
                binding.fragmentTitleDesc.setText("In case of an emergency, call an appropriate number for help.");
                binding.topView2.setVisibility(View.GONE);
                binding.topView.setVisibility(View.VISIBLE);
                binding.viewPagerImageSlider.setVisibility(View.GONE);

            } else if (itemId == R.id.chat) {
                replaceFragment(new ChatFragment());
                binding.fragmentTitle.setText("Chat With Guardian");
                binding.fragmentTitleDesc.setText("Start a one-to-one chat with your guardian.");
                binding.topView2.setVisibility(View.VISIBLE);
                binding.topView.setVisibility(View.VISIBLE);
                binding.viewPagerImageSlider.setVisibility(View.GONE);

            } else if (itemId == R.id.profile) {
                replaceFragment(new ProfileFragment());
                binding.fragmentTitle.setText("Your Profile");
                binding.fragmentTitleDesc.setText("Take a look at your profile, be updated.");
                binding.topView2.setVisibility(View.GONE);
                binding.topView.setVisibility(View.GONE);
                binding.viewPagerImageSlider.setVisibility(View.GONE);
            }
            return true;
        });

    }


    private void init(){
        user = FirebaseAuth.getInstance().getCurrentUser();
        firebaseAuth = FirebaseAuth.getInstance();
        getPhoneNumberFromFirebase();

        viewPager2 = binding.viewPagerImageSlider;


        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
            @Override
            public void onActivityResult(Map<String, Boolean> result) {
                if (result.get(Manifest.permission.SEND_SMS)!=null){
                    isSMSPermissionGranted = result.get(Manifest.permission.SEND_SMS);
                }if (result.get(Manifest.permission.CALL_PHONE)!=null){
                    isSMSPermissionGranted = result.get(Manifest.permission.CALL_PHONE);
                }if (result.get(Manifest.permission.RECORD_AUDIO)!=null){
                    isSMSPermissionGranted = result.get(Manifest.permission.RECORD_AUDIO);
                }
            }
        });
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        //AlertDialogBox
        View alertDialogBox = LayoutInflater.from(this).inflate(R.layout.custom_dialog_layout,null);
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setView(alertDialogBox);

        cancelBtn = (ImageButton)alertDialogBox.findViewById(R.id.cancelBtn);
        okBtn = (Button) alertDialogBox.findViewById(R.id.okBtn);
        dialog = alertDialog.create();

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
            }
        });
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
            }
        });


        //Shake Feature
        lastUpdate = System.currentTimeMillis();
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (sensor==null){
            Toast.makeText(this, "No Accelerometer detected in device!", Toast.LENGTH_SHORT).show();
            finish();
        }else {
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        }

        //Vibrate
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

    }

    //Vibration Generation methods
    private void generateVibration(){
        if (!vibrator.hasVibrator()){
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            vibrator.vibrate(VibrationEffect.createOneShot(1000,VibrationEffect.DEFAULT_AMPLITUDE));
        }else {
            long[] vibrationPattern = {0,200,0,500};
            vibrator.vibrate(vibrationPattern,-1);
        }
    }

    private void policeCall(){
        String ambCall = "9146911950";
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:"+ambCall));
        startActivity(intent);
    }


    private void onClickListener() {
        
        binding.floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                detectLocation();
                generateVibration();
                showDialogBox();
            }
        });

        binding.hamIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(UserXHomeActivity.this, MenuBarActivity.class));

            }
        });

        binding.notificationIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialogBox();
                generateVibration();
            }
        });


    }

    private void showDialogBox(){
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }

    private void replaceFragment(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, fragment);
        fragmentTransaction.commit();
    }


    private void detectLocation() {

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
//        locationManager.requestLocationUpdates(GPS_PROVIDER, 0L, (float) 0, (LocationListener) this);

        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();

                LatLng latLng = new LatLng(latitude,longitude);
//                Toast.makeText(UserXHomeActivity.this, ""+latLng, Toast.LENGTH_SHORT).show();

                findAddress(latLng);
            }
        });
    }

    private void findAddress(LatLng latLng) {
        // find address, country, state, city
        // find address, country, state, city
        double latitude = latLng.latitude;
        double longitude = latLng.longitude;

        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());

        String lat = String.valueOf(latitude);
        String lon = String.valueOf(longitude);

        Log.d("Latlong", "findAddress: latitude " + latitude);
        Log.d("Latlong", "findAddress: longitude " + longitude);

//        Toast.makeText(this, ""+gPhone, Toast.LENGTH_SHORT).show();

        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1);
//            String address = addresses.get(0).getAddressLine(0);  //complete address
            String address = addresses.get(0).getFeatureName()+", "+addresses.get(0).getSubLocality()+", "+addresses.get(0).getLocality() +", " +addresses.get(0).getAdminArea()
                    +", "+addresses.get(0).getPostalCode()
                    +", "+ addresses.get(0).getCountryName() ;  //complete address

            sendSMS(lat, lon, address, gPhone);

        }
        catch (Exception e){
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.d("Exceptlatlong", "findAddress: longitude " + e.getMessage());

        }
    }

    private void sendSMS(String latitude, String longitude, String address, String number) {

        String geoUri = "I'm in Emergency!\n"+"http://maps.google.com/maps?q=loc:" + latitude + "," + longitude+" ("+address+")";

        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(number, null, geoUri, null, null);

    }

    private void getPhoneNumberFromFirebase(){
        FirebaseFirestore.getInstance().collection("Users")
                .document(user.getUid())
                .addSnapshotListener((value, error) -> {

                    if (error != null) {
                        return;
                    }
                    if (value == null) {
                        return;
                    }
                    if (!value.exists()) {
                        return;
                    }

                    if (value.exists()){

                        gPhone = value.getString("gPhone");

                    }

                });

    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        // location detected
        latitude = location.getLatitude();
        longitude = location.getLongitude();

        LatLng latLng = new LatLng(latitude,longitude);

        Log.d("Latlang", latLng.toString());

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {

    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        // gps/location disabled
        Toast.makeText(this, "Please turn on location...", Toast.LENGTH_SHORT).show();
    }

    private void checkPermissions() {
        Dexter.withContext(this)
                .withPermission(Manifest.permission.SEND_SMS)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {

                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                        if (permissionDeniedResponse.isPermanentlyDenied()){
                            Toast.makeText(UserXHomeActivity.this, "This app needs the SMS permission, please accept to use location functionality", Toast.LENGTH_SHORT).show();
                        }else {
                            Toast.makeText(UserXHomeActivity.this, "Permissions denied", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).check();
    }

    private void requestPermissions(){
        isSMSPermissionGranted = ContextCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED;
        isCallPermissionGranted = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CALL_PHONE)
                == PackageManager.PERMISSION_GRANTED;
        isRecordPermissionGranted = ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED;

        List<String> permissionRequest = new ArrayList<String>();

        if (!isSMSPermissionGranted){
            permissionRequest.add(Manifest.permission.SEND_SMS);
        }if (!isCallPermissionGranted){
            permissionRequest.add(Manifest.permission.CALL_PHONE);
        }if (!isRecordPermissionGranted){
            permissionRequest.add(Manifest.permission.RECORD_AUDIO);
        }

        if (!permissionRequest.isEmpty()){
            activityResultLauncher.launch(permissionRequest.toArray(new String[0]));
        }
    }


    //shake feature
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType()==Sensor.TYPE_ACCELEROMETER){
            float[] values = sensorEvent.values;
            float x= values[0];
            float y= values[1];
            float z= values[2];

            float EG = SensorManager.GRAVITY_EARTH;
            float devAccel = (x*x+y*y+z*z)/(EG*EG);

            if (devAccel>=150){
                actualTime=System.currentTimeMillis();
                if ((actualTime-lastUpdate)>5000){
                    lastUpdate=actualTime;

                    Toast.makeText(this, "Shake Detected!", Toast.LENGTH_SHORT).show();
                    showDialogBox();
                    generateVibration();
                    detectLocation();
                    policeCall();

                }
            }

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }


    //Image Slider
    private void imageSlider(){
        List<SliderItem> sliderItemList = new ArrayList<>();
        sliderItemList.add(new SliderItem(R.drawable.ic_child));
        sliderItemList.add(new SliderItem(R.drawable.ic_women));
        sliderItemList.add(new SliderItem(R.drawable.ic_police));
        sliderItemList.add(new SliderItem(R.drawable.ic_ambulance));

        viewPager2.setAdapter(new SliderAdapter(sliderItemList,viewPager2));
        viewPager2.setClipToPadding(false);
        viewPager2.setClipChildren(false);
        viewPager2.setOffscreenPageLimit(3);
        viewPager2.getChildAt(0).setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);

        CompositePageTransformer compositePageTransformer = new CompositePageTransformer();
        compositePageTransformer.addTransformer(new MarginPageTransformer(40));
        compositePageTransformer.addTransformer(new ViewPager2.PageTransformer() {
            @Override
            public void transformPage(@NonNull View page, float position) {
                float r = 1-Math.abs(position);
                page.setScaleY(0.85f+r*0.15f);
            }
        });
        viewPager2.setPageTransformer(compositePageTransformer);
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                sliderHandler.removeCallbacks(sliderRunnable);
                sliderHandler.postDelayed(sliderRunnable,3500);
            }
        });
    }

    private Runnable sliderRunnable = new Runnable() {
        @Override
        public void run() {
            viewPager2.setCurrentItem(viewPager2.getCurrentItem()+1);
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        sliderHandler.removeCallbacks(sliderRunnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sliderHandler.postDelayed(sliderRunnable,3500);
    }
}