package in.newgenai.guardianx.Fragment;

import static in.newgenai.guardianx.SignUpActivity.EMAIL_REGEX;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import in.newgenai.guardianx.LoginActivity;
import in.newgenai.guardianx.R;
import in.newgenai.guardianx.UserXHomeActivity;
import in.newgenai.guardianx.databinding.FragmentCallBinding;
import in.newgenai.guardianx.databinding.FragmentProfileBinding;

public class ProfileFragment extends Fragment implements LocationListener  {

    private FragmentProfileBinding binding;



    // permission constants
    private static final int STORAGE_REQUEST_CODE = 300;
    //image pick constants
    private static final int IMAGE_PICK_GALLERY_CODE = 400;

    //permission array
    private String[] storagePermissions;
    //image uri
    private Uri image_uri;
    private Bitmap bitmap;

    private double latitude = 0.0, longitude = 0.0;

    //firebase auth
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;
    private FirebaseStorage firebaseStorage;
    private FirebaseFirestore firebaseFirestore;
    private StorageReference storageReference;

    private LocationManager locationManager;
    private FusedLocationProviderClient fusedLocationProviderClient;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(inflater, container, false);

        init();
        onClickListner();
//        loadUserInfo();

        return binding.getRoot();
    }


    private void init(){
        user = FirebaseAuth.getInstance().getCurrentUser();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();

        binding.ccp.registerCarrierNumberEditText(binding.phoneEt);
        binding.ccpGPhone.registerCarrierNumberEditText(binding.guardianPhoneEt);

        //initialize permission array
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());


        firebaseAuth = FirebaseAuth.getInstance();
        checkUser();


    }

    private void onClickListner() {

        binding.getLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //detect location
               checkPermissions();
            }
        });

        binding.editProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermissionsGallery();
            }
        });

        binding.updateProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validate();
            }
        });

        binding.logoutTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firebaseAuth.signOut();
                startActivity(new Intent(getContext(), LoginActivity.class));
                getActivity().finish();
            }
        });


    }

    private String fName , lName, email, phoneNo, guardianPhoneNo, address;
    private void validate() {

        //input data
        fName = binding.fNameEt.getText().toString().trim();
        lName = binding.lNameEt.getText().toString().trim();
        email = binding.emailEt.getText().toString().trim();
        phoneNo = binding.ccp.getFullNumberWithPlus().replace("", "").trim();
        guardianPhoneNo = binding.ccpGPhone.getFullNumberWithPlus().replace("", "").trim();
        address = binding.locationEt.getText().toString().trim();

        if (fName.isEmpty() || fName.equals(" ")){
            binding.fNameEt.setError("Please enter first name");
            binding.fNameEt.requestFocus();
            return;
        }
        if (lName.isEmpty() || lName.equals(" ")){
            binding.lNameEt.setError("Please enter last name");
            binding.lNameEt.requestFocus();
            return;
        }
        if (phoneNo.isEmpty() || phoneNo.equals(" ")){
            binding.phoneEt.setError("Please enter phone number");
            binding.phoneEt.requestFocus();
            return;
        }
        if (binding.phoneEt.length() != 10){
            binding.phoneEt.setError("Please enter Valid phone no");
            binding.phoneEt.requestFocus();
            return;
        }
        if (guardianPhoneNo.isEmpty() || guardianPhoneNo.equals(" ") ){
            binding.guardianPhoneEt.setError("Please enter Guardian phone no");
            binding.guardianPhoneEt.requestFocus();
            return;
        }
        if (binding.guardianPhoneEt.length() != 10){
            binding.guardianPhoneEt.setError("Please enter Valid phone no");
            binding.guardianPhoneEt.requestFocus();
            return;
        }
        if (email.isEmpty() || !email.matches(EMAIL_REGEX)){
            binding.emailEt.setError("Please enter valid email");
            binding.emailEt.requestFocus();
            return;
        }


        updateProfile();
    }

    private void updateProfile() {

        if (image_uri == null){
            // update without image

            //setup data to update
            Map<String, Object> map = new HashMap<>();
            map.put("fName", fName);
            map.put("lName", lName);
            map.put("email", email);
            map.put("gPhone", guardianPhoneNo);
            map.put("phoneNo", phoneNo);
            map.put("address", address);

            //update to db
            FirebaseFirestore.getInstance()
                    .collection("Users")
                    .document(user.getUid())
                    .update(map)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(getContext(), "Profile Updated", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getContext(), "Something went wrong!", Toast.LENGTH_SHORT).show();
                        }
                    });

            //update email in authentication
            user.updateEmail(email)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(getContext(), "Email Updated", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getContext(), "Use your old email to sign in", Toast.LENGTH_SHORT).show();
                        }
                    });

        }
        else {
            //update with image
            /*----- Upload image first ------*/
            String filePathAndName = "profileImages/" + ""+ user.getUid();
            //get storage reference
            StorageReference storageReference = firebaseStorage.getReference(filePathAndName);

            storageReference.putFile(image_uri)
                    .addOnCompleteListener(task -> {

                        if (task.isSuccessful()) {

                            storageReference.getDownloadUrl()
                                    .addOnSuccessListener(uri1 -> {

                                        String imageURL = uri1.toString();

                                        //setup data to update
                                        Map<String, Object> map = new HashMap<>();

                                        map.put("fName", fName);
                                        map.put("lName", lName);
                                        map.put("email", email);
                                        map.put("gPhone", guardianPhoneNo);
                                        map.put("phoneNo", phoneNo);
                                        map.put("address", address);
                                        map.put("profileImage", imageURL);

                                        FirebaseFirestore.getInstance().collection("Users")
                                                .document(user.getUid())
                                                .update(map).addOnCompleteListener(task1 -> {

                                                    if (task1.isSuccessful()) {
                                                        Toast.makeText(getContext(), "Updated Successfully...", Toast.LENGTH_SHORT).show();
                                                    } else {
                                                        assert task1.getException() != null;
                                                        Toast.makeText(getContext(), "Error: " + task1.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                    }

                                                });
                                    });
                        } else {
                            assert task.getException() != null;
                            Toast.makeText(getContext(), "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

            //update email in authentication
            user.updateEmail(email)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(getContext(), "Email is updated", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getContext(), "Use your old email to sign in" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        }
    }

    private void loadUserInfo() {

        Log.d("UserUid", user.getUid());

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

                        String fName = value.getString("fName");
                        String lName = value.getString("lName");
                        String email = value.getString("email");
                        String phoneNo = value.getString("phoneNo");
                        String gPhone = value.getString("gPhone");
                        String address = value.getString("address");
                        String profileURL = value.getString("profileImage");

                        phoneNo = phoneNo.substring(3);
                        gPhone = gPhone.substring(3);

                        binding.fNameEt.setText(fName);
                        binding.lNameEt.setText(lName);
                        binding.emailEt.setText(email);
                        binding.phoneEt.setText(phoneNo);
                        binding.guardianPhoneEt.setText(gPhone);
                        binding.locationEt.setText(address);

                        try {
                            Glide.with(this)
                                    .load(profileURL)
                                    .placeholder(R.drawable.ic_person_filled_primary)
                                    .timeout(6500)
                                    .into(binding.profileImage);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }

                });

    }


   private void pickFromGallery() {
        //intent to pick image form gallery
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        launcher.launch(intent);

    }

    ActivityResultLauncher<Intent> launcher= registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK){
                    Intent data = result.getData();
                    if (data!=null && data.getData()!=null){
                        image_uri = data.getData();
                        try {
                            bitmap = MediaStore.Images.Media.getBitmap(
                                    getActivity().getContentResolver(), image_uri
                            );
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    if (image_uri!=null){
                        binding.profileImage.setImageBitmap(bitmap);
                    }

                }
            });


    private void checkPermissionsGallery(){
        Dexter.withContext(getContext())
                .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        pickFromGallery();

                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                        if (permissionDeniedResponse.isPermanentlyDenied()){
                            Toast.makeText(getContext(), "This app needs the Storage permission, please accept to use location functionality", Toast.LENGTH_SHORT).show();
                        }else {
                            Toast.makeText(getContext(), "Permissions denied", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).check();
    }

    private void checkPermissions(){
        Dexter.withContext(getContext())
                .withPermission(Manifest.permission.CALL_PHONE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        detectLocation();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                        if (permissionDeniedResponse.isPermanentlyDenied()){
                            Toast.makeText(getContext(), "This app needs the Calling permission, please accept to use location functionality", Toast.LENGTH_SHORT).show();
                        }else {
                            Toast.makeText(getContext(), "Permissions denied", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).check();
    }

    private void detectLocation() {

        LocationManager locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, (float) 0, (LocationListener) this);

        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();

                        LatLng latLng = new LatLng(latitude,longitude);
                        findAddress(latLng);
                    }
                });
    }

    private void findAddress(LatLng latLng) {
        //find address, country, state, city
        double latitude = latLng.latitude;
        double longitude = latLng.longitude;

//        String lat = String.valueOf(latitude);
//        String lon = String.valueOf(longitude);

        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(getContext(), Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1);
            String address = addresses.get(0).getAddressLine(0);

            //set addresses
            binding.locationEt.setText(address);

        }catch (Exception e){
            Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    private void checkUser() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            startActivity(new Intent(getContext(), LoginActivity.class));
        } else {
            loadUserInfo();
        }
    }


    @Override
    public void onLocationChanged(@NonNull Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        Toast.makeText(getContext(), "Location is disabled...", Toast.LENGTH_SHORT).show();
    }

    
}