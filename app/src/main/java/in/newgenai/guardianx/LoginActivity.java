package in.newgenai.guardianx;

import static in.newgenai.guardianx.SignUpActivity.EMAIL_REGEX;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEt, passEt;
    private ImageView googleSignInBtn;
    private TextView signupTv;
    private Button loginBtn;

    private FirebaseAuth auth;
    private FirebaseUser user;
    private GoogleSignInClient googleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        init();
        onClickListener();

    }

    void init(){
        emailEt = findViewById(R.id.emailEt);
        passEt = findViewById(R.id.passEt);
        googleSignInBtn = findViewById(R.id.googleSignIn);
        signupTv = findViewById(R.id.signupTv);
        loginBtn = findViewById(R.id.loginBtn);

        //Google signin
        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("926533714151-hbpmke2ufe4b0km9757tooc5n69uecm5.apps.googleusercontent.com")
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, options);

        user = FirebaseAuth.getInstance().getCurrentUser();
        auth = FirebaseAuth.getInstance();
    }

    void onClickListener(){
        signupTv.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(intent);
//            finish();
        });

        loginBtn.setOnClickListener(view -> loginUser());

        googleSignInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = googleSignInClient.getSignInIntent();
                startActivityForResult(intent, 1234);
            }
        });
    }

    void loginUser(){
        String email = emailEt.getText().toString().trim();
        String password = passEt.getText().toString().trim();

        if (email.isEmpty()) {
            emailEt.setError("Email is required!");
            emailEt.requestFocus();
            return;
        }
        if (!email.matches(EMAIL_REGEX)) {
            emailEt.setError("Enter valid email");
            emailEt.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            passEt.setError("Password is required!");
            passEt.requestFocus();
            return;
        }
        if (password.length() < 6) {
            passEt.setError("Enter valid password");
            passEt.requestFocus();
            return;
        }


        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        FirebaseUser user = auth.getCurrentUser();
                        checkUserType(user);

                        if (!user.isEmailVerified()) {
                            Toast.makeText(LoginActivity.this, "Please verify your email", Toast.LENGTH_SHORT).show();
                        }
//                            sendUserToMainActivity();
                    } else {
                        String exception = "Error: " + task.getException().getMessage();
                        Toast.makeText(LoginActivity.this, exception, Toast.LENGTH_SHORT).show();

                    }
                });
    }

    private void checkUserType(FirebaseUser user) {

        Task<DocumentSnapshot> ref = FirebaseFirestore.getInstance().collection("Users").document(user.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {

                    String accountType = documentSnapshot.getString("accountType");

                    if (accountType.equals("UserX")) {
                        startActivity(new Intent(LoginActivity.this, LocationPermissionActivity.class));
                        finish();
                    }else if(accountType.equals("Parent")) {
                        startActivity(new Intent(LoginActivity.this, ParentHomeActivity.class));
                        finish();
                    }else {
                        Toast.makeText(LoginActivity.this, "Seems like you're an Admin.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    //Google SignIn methods
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1234){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);

                AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                FirebaseAuth.getInstance().signInWithCredential(credential)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()){
                                    FirebaseUser user = auth.getCurrentUser();
                                    updateUI(user);
                                }else {
                                    Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

            } catch (ApiException e) {
                throw new RuntimeException(e);
            }
        }
    }

    void updateUI(FirebaseUser user){
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(LoginActivity.this);

      //  uploadUser(account);
        Intent intent = new Intent(LoginActivity.this, GoogleSignInActivity.class);
        intent.putExtra("fName", account.getDisplayName());
        intent.putExtra("email", account.getEmail());
        intent.putExtra("profileImage", String.valueOf(account.getPhotoUrl()));
        intent.putExtra("uid", user.getUid());
        startActivity(intent);
        finish();

    }

    void uploadUser(GoogleSignInAccount account){
        Map<String, Object> map = new HashMap<>();

        map.put("fName", account.getDisplayName());
        map.put("lName", "");
        map.put("email", account.getEmail());
        map.put("phoneNo", "");
        map.put("state", "");
        map.put("city", "");
        map.put("address", "");
        map.put("accountType", "");
        map.put("profileImage", String.valueOf(account.getPhotoUrl()));
        map.put("uid", user.getUid());

        FirebaseFirestore.getInstance().collection("Users").document(user.getUid())
                .set(map)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()){
                            Toast.makeText(LoginActivity.this, "Document added", Toast.LENGTH_SHORT).show();
                        }else {
                            Toast.makeText(LoginActivity.this, "Error: "+ task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            return;
                        }
                    }
                });
    }

}