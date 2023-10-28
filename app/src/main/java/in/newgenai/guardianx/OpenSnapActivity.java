package in.newgenai.guardianx;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ShareCompat;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.util.Arrays;

import in.newgenai.guardianx.databinding.ActivityOpenSnapBinding;

public class OpenSnapActivity extends AppCompatActivity {

    private ActivityOpenSnapBinding binding;
    private String finalPath = "";
    private String fileName = "";
    private ScaleGestureDetector scaleGestureDetector;
    private float scaleFactor = 1.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOpenSnapBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();
//        onClickListener();
    }


    private void init(){

        Intent intent = getIntent();
        if (intent != null){
            finalPath = intent.getStringExtra("parseData");
            fileName = intent.getStringExtra("fileName");
            Log.d("OpenSnapIntent: ", finalPath);
            Glide.with(this)
                    .load(finalPath)
                    .placeholder(R.drawable.ic_image_placeholder)
                    .into(binding.snapOpened);
        }

        scaleGestureDetector = new ScaleGestureDetector(this,
                new ScaleListener());

        Log.d("FileSnapName", finalPath);

        binding.fileNameTv.setText(fileName);
    }

    private void onClickListener() {

        binding.shareBtn.setOnClickListener(v -> new ShareCompat.IntentBuilder(
                OpenSnapActivity.this)
                .setStream(Uri.parse(finalPath))
                .setType("image/*")
                .setChooserTitle("Share Image")
                .startChooser());


        binding.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(OpenSnapActivity.this);
                alertDialogBuilder.setMessage("Are you sure you want to delete this image ?");
                alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String[] projection = new String[]{MediaStore.Images.Media._ID};
                        String selection = MediaStore.Images.Media.DATA + " = ?";
                        String[] selectionArgs = new String[]{new File(finalPath).getAbsolutePath()};
                        Uri queryUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

                        ContentResolver contentResolver = getContentResolver();
                        Cursor cursor = contentResolver.query(queryUri, projection, selection,
                                selectionArgs, null);

                        Log.e("DeleteUri", cursor.toString() + "");

                        if (cursor.moveToFirst()) {
                            long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
                            Uri deleteUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
                            try {
                                contentResolver.delete(deleteUri, null, null);
                                boolean delete1 = new File(finalPath).delete();
                                Log.e("TAG", delete1 + "");
                                Toast.makeText(OpenSnapActivity.this, "Deleted Successfully", Toast.LENGTH_SHORT).show();
                                finish();
                            } catch (Exception e) {
                                e.printStackTrace();
                                Toast.makeText(OpenSnapActivity.this, "Error Deleting Video", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(OpenSnapActivity.this, "File Not Find", Toast.LENGTH_SHORT).show();
                        }
                        cursor.close();
                    }
                });
                alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                alertDialogBuilder.show();
            }

        });

    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleGestureDetector.onTouchEvent(event);
        return true;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scaleFactor *= detector.getScaleFactor();
            scaleFactor = Math.max(0.1f,Math.min(scaleFactor,10.0f));

            binding.snapOpened.setScaleX(scaleFactor);
            binding.snapOpened.setScaleY(scaleFactor);

            return true;
        }
    }

}