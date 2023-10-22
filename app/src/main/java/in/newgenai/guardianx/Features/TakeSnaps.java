package in.newgenai.guardianx.Features;


import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;


import androidx.appcompat.app.AppCompatActivity;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;


public class TakeSnaps extends AppCompatActivity {

    ContentResolver contentResolver;
    private String pattern = "dd-MM-yyyy";
    String fileName;


    private String TAG = "Image save test: ";
    private String FAILURE = "Failed";
    private String SUCCESS = "Success";
    private final Handler handler = new Handler();


    public void startCapturingSnaps(Context context) {


        captureBackPhoto(context);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Do something after 5s = 5000ms
                captureFrontPhoto(context);
            }
        }, 2000);



    }

    public void captureFrontPhoto(Context context) {

        Log.d("Image capture in: ", "Preparing to take photo");
        Camera camera = null;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();

        int frontCamera = 1;
        //int backCamera=0;

        Camera.getCameraInfo(frontCamera, cameraInfo);

        try {
            camera = Camera.open(frontCamera);
            camera.enableShutterSound(false);
        } catch (RuntimeException e) {
            Log.d(TAG, "Camera not available: " + 1);
            camera = null;
            //e.printStackTrace();
        }

        try {
            if (null == camera) {
                Log.d(TAG, "Could not get camera instance");
            } else {
                Log.d(TAG, "Got the camera, creating the dummy surface texture");
                try {
                    camera.setPreviewTexture(new SurfaceTexture(0));
                    camera.startPreview();
                } catch (Exception e) {
                    Log.d(TAG, "Could not set the surface preview texture");
                    e.printStackTrace();
                }

                camera.takePicture(null, null, new Camera.PictureCallback() {

                    @Override
                    public void onPictureTaken(byte[] data, Camera camera) {

                        Log.d(TAG, "Taken from Front");


                        try {
                            Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);

                            // Rotate the Bitmap thanks to a rotated matrix. This seems to work.
                            Matrix matrix = new Matrix();
                            matrix.postRotate(270);
                            bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);

                            saveImage(bmp, context);

                        } catch (Exception e) {
                            Log.d(TAG, FAILURE);
                            System.out.println(e.getMessage());
                        }
                        camera.release();
                    }
                });
            }
        } catch (Exception e) {
            camera.release();
        }

    }

    public void captureBackPhoto(Context context) {

        Log.d(TAG, "Preparing to take photo");
        Camera camera = null;

        try {
            camera = Camera.open();
            camera.enableShutterSound(false);

        } catch (RuntimeException e) {
            Log.d(TAG, "Camera not available: " + 1);
            camera = null;
            //e.printStackTrace();
        }


        try {
            if (null == camera) {
                Log.d(TAG, "Could not get camera instance");
            } else {
                Log.d(TAG, "Got the camera, creating the dummy surface texture");
                try {
                    camera.setPreviewTexture(new SurfaceTexture(0));
                    camera.startPreview();
                } catch (Exception e) {
                    Log.d(TAG, "Could not set the surface preview texture");
                    e.printStackTrace();
                }

                camera.takePicture(null, null, new Camera.PictureCallback() {

                    @Override
                    public void onPictureTaken(byte[] data, Camera camera) {

                        Log.d(TAG, "Taken from Back");


                        try {
                            Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);

                            // Rotate the Bitmap thanks to a rotated matrix. This seems to work.
                            Matrix matrix = new Matrix();
                            matrix.postRotate(90);
                            bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);

                            saveImage(bmp, context);

                        } catch (Exception e) {
                            System.out.println(e.getMessage());
                        }
                        camera.release();
                    }
                });
            }
        } catch (Exception e) {
            camera.release();
        }
    }

    public void saveImage(Bitmap bitmap, Context context) {

        Log.d(TAG, "Start saving Image");

        Uri imageUri;

        Log.d("Image save test:", "Success");


        contentResolver = context.getContentResolver();


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            imageUri = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        } else {
            imageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        }

        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, System.currentTimeMillis() + ".jpg");
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "images/*");
        Uri uri = contentResolver.insert(imageUri, contentValues);

        //save image to package level directory
        try {
            storeInAppDirectory(bitmap, context);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            OutputStream outputStream = contentResolver.openOutputStream(Objects.requireNonNull(uri));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            Objects.requireNonNull(outputStream);

            Log.d(TAG, SUCCESS);

        } catch (Exception e) {
            Log.d(TAG, FAILURE);
            e.printStackTrace();
        }

    }

    private void storeInAppDirectory(Bitmap bitmapImage, Context context) throws IOException {

        Long timestamp = System.currentTimeMillis()/1000;

        Time time = new Time(System.currentTimeMillis());

        String timestampString = timestamp.toString();
        String dateInString =new SimpleDateFormat(pattern).format(new Date());

        Log.d("TimeStampToTime", time.toString());


        fileName = "gx_"+dateInString+"_"+time;

        ContextWrapper contextWrapper = new ContextWrapper(context);
        File image = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            image = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        }
        File file = new File(image, fileName+".jpg");

        FileOutputStream out = new FileOutputStream(file);
        bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, out);
        out.flush();
        out.close();

    }



}


