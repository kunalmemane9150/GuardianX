package in.newgenai.guardianx;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class GoogleSignInActivity extends AppCompatActivity {

    private TextView welcomeUser;
    private EditText gEmailEt;
    private Button loginBtn;
    private LinearLayout gEmailLL;
    private String fName, uid, profileImage, email;
    private MaterialButtonToggleGroup toggleGroup;

    private FirebaseAuth auth;
    private FirebaseFirestore firebaseFirestore;
    
    private String accountType = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_sign_in);

        init();
        onClickListener();




    }

    void init(){
        welcomeUser = findViewById(R.id.welcomeUser);
        toggleGroup = findViewById(R.id.toggleButton);
        gEmailEt = findViewById(R.id.gEmailEt);
        loginBtn = findViewById(R.id.loginBtn);
        gEmailLL = findViewById(R.id.gEmailLL);

        Intent intent = getIntent();
        fName = intent.getStringExtra("fName");
        uid = intent.getStringExtra("uid");
        profileImage = intent.getStringExtra("profileImage");
        email = intent.getStringExtra("email");

        welcomeUser.setText(fName);

    }

    void onClickListener(){

        toggleGroup.addOnButtonCheckedListener(new MaterialButtonToggleGroup.OnButtonCheckedListener() {
            @Override
            public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {

                if (isChecked){
                    
                    if (checkedId == R.id.childBtn){
                        accountType = "UserX";
                        gEmailLL.setVisibility(View.VISIBLE);
                    } else if (checkedId == R.id.parentBtn) {
                        accountType = "Parent";
                        gEmailLL.setVisibility(View.GONE);
                    }

                }else{
                    if (toggleGroup.getCheckedButtonId() == View.NO_ID){
                        accountType = "";
                        Toast.makeText(GoogleSignInActivity.this, "Please select your role", Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                uploadUser();

            }
        });

    }


    void uploadUser(){
        
        if (accountType.equals("UserX")){

            String guardEmail = gEmailEt.getText().toString().trim();
            uploadUserAsChild(guardEmail);
            
        }else if(accountType.equals("Parent")){
            uploadUserAsParent();
        }else {
            Toast.makeText(this, "Please select your role", Toast.LENGTH_SHORT).show();
        }
    }

    void uploadUserAsChild(String guardEmail){

        Map<String, Object> map = new HashMap<>();

        map.put("fName", fName);
        map.put("lName", "");
        map.put("email", email);
        map.put("phoneNo", "");
        map.put("state", "");
        map.put("gEmail", guardEmail);
        map.put("city", "");
        map.put("address", "");
        map.put("accountType", accountType);
        map.put("profileImage", profileImage);
        map.put("uid",uid);

        FirebaseFirestore.getInstance().collection("Users").document(uid)
                .set(map)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()){
                            startActivity(new Intent(GoogleSignInActivity.this, UserXHomeActivity.class));
                            finish();
                        }else {
                            Toast.makeText(GoogleSignInActivity.this, "Error: "+ task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            return;
                        }
                    }
                });

    }

    void uploadUserAsParent(){

        Map<String, Object> map = new HashMap<>();

        map.put("fName", fName);
        map.put("lName", "");
        map.put("email", email);
        map.put("phoneNo", "");
        map.put("state", "");
        map.put("city", "");
        map.put("address", "");
        map.put("accountType", accountType);
        map.put("profileImage", profileImage);
        map.put("uid",uid);

        FirebaseFirestore.getInstance().collection("Users").document(uid)
                .set(map)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()){
                            startActivity(new Intent(GoogleSignInActivity.this, ParentHomeActivity.class));
                            finish();
                        }else {
                            Toast.makeText(GoogleSignInActivity.this, "Error: "+ task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            return;
                        }
                    }
                });

    }

}