package in.newgenai.guardianx;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private ImageView splashBtn;
    FirebaseFirestore firebaseFirestore;
    private FirebaseAuth auth;

    public static String PACKAGE_NAME;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PACKAGE_NAME = getApplicationContext().getPackageName();


        auth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        splashBtn = findViewById(R.id.splashBtn);

        FirebaseUser user = auth.getCurrentUser();
        if(user == null){
            splashBtn.setVisibility(View.VISIBLE);
            onClickListener();
        }else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    checkUserType(user);
                }
            }, 500);
        }

    }

    void onClickListener(){

        splashBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
            }
        });
    }

    private void checkUserType(FirebaseUser user){
        //if user is child, start child main screen
        //if user is Parent , start Parent main screen
        //if user is admin , start Admin main screen

        Task<DocumentSnapshot> ref = FirebaseFirestore.getInstance().collection("Users")
                .document(user.getUid()).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        String accountType = ""+documentSnapshot.getString("accountType");

                        if(accountType.equals("UserX")){
                            //user is Child
                            startActivity(new Intent(MainActivity.this, LocationPermissionActivity.class));
                            finish();
                        }else if(accountType.equals("Parent")){
                            //user is Parent
                            startActivity(new Intent(MainActivity.this, ParentHomeActivity.class));
                            finish();
                        }else {
                            //user is Admin
                            Toast.makeText(MainActivity.this, "Seems like you're an Admin Please", Toast.LENGTH_SHORT).show();
                        }
                        finish();
                    }
                });
    }


}