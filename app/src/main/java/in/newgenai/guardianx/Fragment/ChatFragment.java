package in.newgenai.guardianx.Fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.File;
import java.util.List;

import in.newgenai.guardianx.MainActivity;
import in.newgenai.guardianx.R;
import in.newgenai.guardianx.RecordingsAndSnapsActivity;
import in.newgenai.guardianx.Services.RecordingService;
import in.newgenai.guardianx.databinding.FragmentChatBinding;


public class ChatFragment extends Fragment {

    private FragmentChatBinding binding;

    private boolean startRecording = true;
    private boolean stopRecording = true;
    long timeWhenPaused = 0;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentChatBinding.inflate(inflater, container, false);


        init();
        onClickListener();

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    private void init() {
    }

    private void onClickListener() {

        binding.recordFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                checkRecordingPermissions();

            }
        });

        binding.recordingsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), RecordingsAndSnapsActivity.class);
                startActivity(intent);
            }
        });

    }

    private void checkRecordingPermissions() {

        Dexter.withContext(getActivity())
                .withPermissions(Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            startRecording();
                        }

                        // check for permanent denial of any permission
                        if (report.isAnyPermissionPermanentlyDenied()) {

                            // permission is denied permenantly, navigate user to app settings
                            showSettingsDialog();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                })
                .onSameThread()
                .check();

    }

    private void showSettingsDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Need Permissions");
        builder.setMessage("This app needs permission to use this feature. You can grant them in app settings.");
        builder.setPositiveButton("GOTO SETTINGS", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                openSettings();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    private void openSettings() {

        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);

        Uri uri = Uri.fromParts("package", MainActivity.PACKAGE_NAME, null);
        intent.setData(uri);
        startActivityForResult(intent, 101);
    }



    private void startRecording() {

        onRecord(startRecording);
        startRecording = !startRecording;
    }

    @SuppressLint("ResourceAsColor")
    private void onRecord(boolean startRecording) {

        Intent intent = new Intent(getContext(), RecordingService.class);

        if(startRecording){

            binding.recordFab.setBackgroundTintList(ContextCompat.getColorStateList(getContext(), R.color.colorRed));
            binding.recordFab.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_mic, 0, 0, 0);
            binding.recordFab.setText("Stop Recording");

            Toast.makeText(getContext(), "Recording started...", Toast.LENGTH_SHORT).show();

            //Recordings Folder Access
            ContextWrapper contextWrapper = new ContextWrapper(getContext());
            File music = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                music = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_RECORDINGS);
            }


            binding.chronometerView.setBase(SystemClock.elapsedRealtime());
            binding.chronometerView.start();

            Log.d("Recording Logs: ", "");

            getActivity().startService(intent);
            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            binding.recordingStatusTv.setText("Recording in Progress");

        }else {
            binding.recordFab.setBackgroundTintList(ContextCompat.getColorStateList(getContext(), R.color.black));
            binding.recordFab.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_mic_filled, 0, 0, 0);
            binding.recordFab.setText("Start Recording");

            Toast.makeText(getContext(), "Recording Stopped...", Toast.LENGTH_SHORT).show();
            binding.chronometerView.stop();
            binding.chronometerView.setBase(SystemClock.elapsedRealtime());
            timeWhenPaused = 0;
            binding.recordingStatusTv.setText("Tap the button to start Recording");

            getActivity().stopService(intent);

        }
    }



}