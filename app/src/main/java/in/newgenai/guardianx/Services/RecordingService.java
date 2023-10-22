package in.newgenai.guardianx.Services;

import android.app.Service;
import android.content.ContextWrapper;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import in.newgenai.guardianx.Database.DBRecordHelper;
import in.newgenai.guardianx.Model.RecordingItemModel;

public class RecordingService extends Service {

    MediaRecorder mediaRecorder;
    long startingTimeMillis = 0;
    long elapsedMillis = 0;

    File file;

    String fileName;
    private String pattern = "dd-MM-yyyy";

    DBRecordHelper dbRecordHelper;

    @Override
    public void onCreate() {
        super.onCreate();
        dbRecordHelper = new DBRecordHelper(getApplicationContext());

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        startRecording();

        return START_STICKY;
    }

    private void startRecording() {
        Long timestamp = System.currentTimeMillis()/1000;
        String timestampString = timestamp.toString();
        String dateInString =new SimpleDateFormat(pattern).format(new Date());

        fileName = "audio_"+timestampString+"_"+dateInString;

        ContextWrapper contextWrapper = new ContextWrapper(getApplicationContext());
        File music = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            music = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_RECORDINGS);
        }



        file = new File(music, fileName+".mp3");


        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setOutputFile(file.getAbsolutePath());
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mediaRecorder.setAudioChannels(1);

        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
            startingTimeMillis = System.currentTimeMillis();
        }catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void stopRecording(){
        mediaRecorder.stop();
        elapsedMillis = (System.currentTimeMillis()-startingTimeMillis);
        mediaRecorder.release();
        Toast.makeText(this, "Recording Saved! "+file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
        Log.d("Recording Services: ", file.getAbsolutePath());


        //add to database
        RecordingItemModel recordingItemModel = new
                RecordingItemModel(fileName, file.getAbsolutePath(),elapsedMillis, System.currentTimeMillis());
        dbRecordHelper.addRecording(recordingItemModel);
    }

    @Override
    public void onDestroy() {

        if (mediaRecorder !=null){
            stopRecording();
        }

        super.onDestroy();
    }


}
