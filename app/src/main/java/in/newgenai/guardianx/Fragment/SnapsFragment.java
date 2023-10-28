package in.newgenai.guardianx.Fragment;

import android.Manifest;
import android.content.ContentUris;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.database.Cursor;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.File;
import java.sql.Struct;
import java.util.ArrayList;
import java.util.Collections;

import in.newgenai.guardianx.Adapter.FileViewerAdapter;
import in.newgenai.guardianx.Adapter.SnapAdapter;
import in.newgenai.guardianx.Model.SnapModel;
import in.newgenai.guardianx.OpenSnapActivity;
import in.newgenai.guardianx.databinding.FragmentSnapsBinding;

public class SnapsFragment extends Fragment {

    private FragmentSnapsBinding binding;
    ArrayList<SnapModel> arrayList;
    SnapAdapter snapAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSnapsBinding.inflate(inflater, container, false);

        init();
        checkPermissions();

        return binding.getRoot();
    }

    private void init() {

        arrayList = new ArrayList<>();


        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        binding.recyclerView.setLayoutManager(layoutManager);

        snapAdapter = new SnapAdapter(arrayList, getContext());
        binding.recyclerView.setAdapter(snapAdapter);

    }


    private void checkPermissions() {
        Dexter.withContext(getContext())
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        readSnaps(getContext());
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).check();
    }
    @Override
    public void onResume() {
        super.onResume();
        readSnaps(getContext());
    }

    private void readSnaps(Context context) {

        arrayList.clear();

        //Custom Path
//        String filePath = "/storage/emulated/0/Pictures";

        //App package Path
        String filePath = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath();

        Log.d("SnapPath", filePath);


        File file = new File(filePath);
        File[] files = file.listFiles();

        if (files != null) {
            for (File file1 : files) {
                if (file1.getPath().endsWith(".png") || file1.getPath().endsWith(".jpg")) {
                    arrayList.add(new SnapModel(file1.getName(), file1.getPath(), file1.length()));
                }
            }
        }

        Collections.reverse(arrayList);

        SnapAdapter adapter = new SnapAdapter(arrayList, context);
        binding.recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener((view, path) -> startActivity(
                new Intent(context, OpenSnapActivity.class)
                        .putExtra("parseData", path))
        );

    }


}