package in.newgenai.guardianx;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import com.bumptech.glide.Glide;

import in.newgenai.guardianx.databinding.ActivityOpenSnapBinding;

public class OpenSnapActivity extends AppCompatActivity {

    private ActivityOpenSnapBinding binding;
    private String image = "";
    private ScaleGestureDetector scaleGestureDetector;
    private float scaleFactor = 1.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOpenSnapBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();
    }

    private void init(){

        Intent intent = getIntent();
        if (intent != null){
            image = intent.getStringExtra("parseData");
            Log.d("OpenSnapIntent: ", image);

            Glide.with(this).load(image).into(binding.snapOpened);
        }



        Glide.with(this)
                .load(image)
                .into(binding.snapOpened);

        scaleGestureDetector = new ScaleGestureDetector(this,
                new ScaleListener());
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