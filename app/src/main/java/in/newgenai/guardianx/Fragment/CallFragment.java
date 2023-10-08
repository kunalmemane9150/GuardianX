package in.newgenai.guardianx.Fragment;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import in.newgenai.guardianx.databinding.FragmentCallBinding;

public class CallFragment extends Fragment {

    private FragmentCallBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCallBinding.inflate(inflater, container, false);

        init();
        checkPermissions();
        return binding.getRoot();

    }

    private void init(){

    }

    private void checkPermissions(){
        Dexter.withContext(getContext())
                .withPermission(Manifest.permission.CALL_PHONE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        callRespective();
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

    private void callRespective() {

        ambulanceCall();
        nationalHLCall();
        policeCall();
        childHelpCall();
        womenCall();
        fireCall();
        railCall();


    }

    private void ambulanceCall(){
        binding.ambulanceLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String ambCall = "108";
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:"+ambCall));
                startActivity(intent);
            }
        });

        binding.ambulanceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String ambCall = "108";
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:"+ambCall));
                startActivity(intent);
            }
        });

    }

    private void nationalHLCall(){
        binding.callNHLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String ambCall = "112";
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:"+ambCall));
                startActivity(intent);
            }
        });

        binding.callNHBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String ambCall = "112";
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:"+ambCall));
                startActivity(intent);
            }
        });

    }

    private void policeCall(){
        binding.policeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String ambCall = "100";
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:"+ambCall));
                startActivity(intent);
            }
        });

        binding.policeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String ambCall = "100";
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:"+ambCall));
                startActivity(intent);
            }
        });

    }

    private void childHelpCall(){
        binding.childLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String ambCall = "1098";
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:"+ambCall));
                startActivity(intent);
            }
        });

        binding.childBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String ambCall = "1098";
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:"+ambCall));
                startActivity(intent);
            }
        });

    }

    private void womenCall(){
        binding.womenLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String ambCall = "1091";
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:"+ambCall));
                startActivity(intent);
            }
        });

        binding.womenBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String ambCall = "1091";
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:"+ambCall));
                startActivity(intent);
            }
        });

    }

    private void fireCall(){
        binding.fireLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String ambCall = "101";
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:"+ambCall));
                startActivity(intent);
            }
        });

        binding.fireBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String ambCall = "101";
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:"+ambCall));
                startActivity(intent);
            }
        });

    }

    private void railCall(){
        binding.railLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String ambCall = "182";
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:"+ambCall));
                startActivity(intent);
            }
        });

        binding.railBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String ambCall = "182";
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:"+ambCall));
                startActivity(intent);
            }
        });

    }

}