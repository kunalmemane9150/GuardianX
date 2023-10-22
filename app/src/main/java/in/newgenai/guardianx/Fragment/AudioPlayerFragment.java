package in.newgenai.guardianx.Fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import in.newgenai.guardianx.Model.RecordingItemModel;
import in.newgenai.guardianx.R;

public class AudioPlayerFragment extends DialogFragment {

    private RecordingItemModel itemModel;
    private Handler handle = new Handler();
    private MediaPlayer mediaPlayer;

    private boolean isPlaying = false;
    long minutes = 0;
    long seconds = 0;

    private ImageButton playFab;
    private TextView fileLengthTv, currentProgressTv , fileNameTv;
    private SeekBar seekBar;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        itemModel = (RecordingItemModel) getArguments().getSerializable("item");
        minutes = TimeUnit.MICROSECONDS.toMinutes(itemModel.getLength());
        seconds = TimeUnit.MINUTES.toSeconds(itemModel.getLength()) - TimeUnit.MINUTES.toSeconds(minutes);



    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_audio_player, null);


        fileNameTv = view.findViewById(R.id.fileNameTv);
        currentProgressTv = view.findViewById(R.id.currentProgressTv);
        fileLengthTv = view.findViewById(R.id.fileLengthTv);
        playFab = view.findViewById(R.id.playFab);
        playFab = view.findViewById(R.id.playFab);
        seekBar = view.findViewById(R.id.seekBar);

        fileNameTv.setText(itemModel.getName());

        long minutes = TimeUnit.MILLISECONDS.toMinutes(itemModel.getLength());
        long seconds = TimeUnit.MILLISECONDS.toSeconds(itemModel.getLength()) -
                TimeUnit.MINUTES.toSeconds(minutes);

        fileLengthTv.setText(String.format("%02d:%02d", minutes, seconds));

        playFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                    onPlay(isPlaying);
                    isPlaying = !isPlaying;
            }
        });

        setSeekbarValues();

        builder.setView(view);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        return builder.create();
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {

        if (isPlaying){
            stopPlaying();
        }

        super.onDismiss(dialog);
    }

    private void onPlay(boolean isPlaying) {
        if (!isPlaying){
            if (mediaPlayer == null){
                try {
                    startPlaying();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }else {
            pausePlaying();
        }
    }

    private void pausePlaying() {

        playFab.setImageResource(R.drawable.ic_play);
        handle.removeCallbacks(mRunnable);
        mediaPlayer.pause();
    }

    private void startPlaying() throws IOException {
        playFab.setImageResource(R.drawable.ic_pause);
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setDataSource(itemModel.getPath());
        mediaPlayer.prepare();
        seekBar.setMax(mediaPlayer.getDuration());

        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mediaPlayer.start();
            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                stopPlaying();
            }
        });

        updateSeekbar();
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    }

    private void setSeekbarValues() {
        ColorFilter colorFilter = new LightingColorFilter(getResources().getColor(R.color.black),
                getResources().getColor(R.color.black));

        seekBar.getProgressDrawable().setColorFilter(colorFilter);
        seekBar.getThumb().setColorFilter(colorFilter);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (mediaPlayer != null && b){
                    mediaPlayer.seekTo(i);
                    handle.removeCallbacks(mRunnable);

                    long minute = TimeUnit.MILLISECONDS.toMinutes(mediaPlayer.getCurrentPosition());
                    long seconds = TimeUnit.MILLISECONDS.toSeconds(mediaPlayer.getCurrentPosition()) -
                            TimeUnit.MILLISECONDS.toSeconds(minute);

                    currentProgressTv.setText(String.format("%02d:%02d", minute, seconds));

                    updateSeekbar();

                }else if (mediaPlayer == null && b){
                    try {
                        prepareMediaPlayerFromPoint(i);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    updateSeekbar();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }

    private void prepareMediaPlayerFromPoint(int i) throws IOException {

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setDataSource(itemModel.getPath());
        mediaPlayer.prepare();
        seekBar.setMax(mediaPlayer.getDuration());
        mediaPlayer.seekTo(i);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                stopPlaying();
            }
        });

    }

    private void stopPlaying() {
        playFab.setImageResource(R.drawable.ic_pause);
        handle.removeCallbacks(mRunnable);
        mediaPlayer.stop();
        mediaPlayer.reset();
        mediaPlayer.release();
        mediaPlayer = null;

        seekBar.setProgress(seekBar.getMax());
        isPlaying = !isPlaying;

        currentProgressTv.setText(fileLengthTv.getText());
        seekBar.setProgress(seekBar.getMax());

    }


    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer != null){
                int currentPosition = mediaPlayer.getCurrentPosition();
                seekBar.setProgress(currentPosition);

                long minute = TimeUnit.MILLISECONDS.toMinutes(mediaPlayer.getCurrentPosition());
                long seconds = TimeUnit.MILLISECONDS.toSeconds(mediaPlayer.getCurrentPosition()) -
                        TimeUnit.MILLISECONDS.toSeconds(minute);

                currentProgressTv.setText(String.format("%02d:%02d", minute, seconds));
                updateSeekbar();
            }
        }
    };

    private void updateSeekbar() {
        handle.postDelayed(mRunnable,1000);
    }
}
