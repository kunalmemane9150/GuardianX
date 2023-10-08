package in.newgenai.guardianx;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import in.newgenai.guardianx.databinding.ActivitySignUpBinding;

public class SignUpActivity extends AppCompatActivity {

    private ActivitySignUpBinding binding;
    public static final String EMAIL_REGEX = "[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}";   //"^(.+)@(.+)$"   '^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,6}$'
    private FirebaseAuth auth;
    FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();
        onClickListener();

    }

    void init(){

        auth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        binding.ccp.registerCarrierNumberEditText(binding.phoneEt);
        binding.ccpGPhone.registerCarrierNumberEditText(binding.guardianPhoneEt);


    }

    void onClickListener(){
        binding.signupTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext().getApplicationContext(), LoginActivity.class));
                finish();
            }
        });

        binding.UserXSignUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validate();
            }
        });
    }


    void validate(){
        String fName = binding.fNameEt.getText().toString().trim();
        String lName = binding.lNameEt.getText().toString().trim();
        String email = binding.emailEt.getText().toString().trim();
        String phoneNo = binding.ccp.getFullNumberWithPlus().replace("", "").trim();
        String guardianPhoneNo = binding.ccpGPhone.getFullNumberWithPlus().replace("", "").trim();
        String password = binding.passEt.getText().toString().trim();
        String confirmPass = binding.confirmPassEt.getText().toString().trim();

        if (fName.isEmpty() || fName.equals(" ")){
            binding.fNameEt.setError("Please enter first name");
            binding.fNameEt.requestFocus();
            return;
        }
        if (lName.isEmpty() || lName.equals(" ")){
            binding.lNameEt.setError("Please enter last name");
            binding.lNameEt.requestFocus();
            return;
        }if (phoneNo.isEmpty() || phoneNo.equals(" ")){
            binding.phoneEt.setError("Please enter phone number");
            binding.phoneEt.requestFocus();
            return;
        }if (binding.phoneEt.length() != 10){
            binding.phoneEt.setError("Please enter Valid phone no");
            binding.phoneEt.requestFocus();
            return;
        }
        if (guardianPhoneNo.isEmpty() || guardianPhoneNo.equals(" ") ){
            binding.guardianPhoneEt.setError("Please enter Guardian phone no");
            binding.guardianPhoneEt.requestFocus();
            return;
        }
        if (binding.guardianPhoneEt.length() != 10){
            binding.guardianPhoneEt.setError("Please enter Valid phone no");
            binding.guardianPhoneEt.requestFocus();
            return;
        }
        if (email.isEmpty() || !email.matches(EMAIL_REGEX)){
            binding.emailEt.setError("Please enter valid email");
            binding.emailEt.requestFocus();
            return;
        }if (password.isEmpty() || password.equals(" ")){
            binding.passEt.setError("Enter password");
            binding.passEt.requestFocus();
            return;
        }
        if(password.length() < 6){
            binding.passEt.setError("Enter valid password");
            binding.passEt.requestFocus();
            return;
        }if (confirmPass.isEmpty() || !confirmPass.equals(password)){
            binding.confirmPassEt.setError("Password doesn't match");
            binding.confirmPassEt.requestFocus();
            return;
        }

        //Create account with email and password
        createAccount(email, password, fName, lName, guardianPhoneNo, phoneNo);

    }

    void createAccount(String email, String password, String fName, String lName, String gPhone, String phoneNo){
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            FirebaseUser user =  auth.getCurrentUser();
                            user.sendEmailVerification()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                Toast.makeText(getApplicationContext(), "Account created successfully...", Toast.LENGTH_SHORT).show();
                                            }else{
                                                Toast.makeText(getApplicationContext(), "Oops! something went wrong..", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                            uploadUser(user, fName, lName, email, gPhone, phoneNo);

                        }else {
                            String exception = task.getException().getMessage();
                            Toast.makeText(getApplicationContext().getApplicationContext(), "Error: "+ exception, Toast.LENGTH_SHORT).show();
                        }
                    }
                });


    }

    private void uploadUser(@NonNull FirebaseUser user, String fName, String lName, String email, String gPhone, String phoneNo) {
        Map<String, Object> map = new HashMap<>();

        map.put("fName", fName);
        map.put("lName", lName);
        map.put("email", email);
        map.put("gPhone", gPhone);
        map.put("phoneNo", phoneNo);
        map.put("address", "");
        map.put("accountType", "UserX");
        map.put("profileImage", "");
        map.put("uid", user.getUid());

        FirebaseFirestore.getInstance().collection("Users").document(user.getUid())
                .set(map)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {
                            startActivity(new Intent(getApplicationContext().getApplicationContext(), UserXHomeActivity.class));
                            Toast.makeText(getApplicationContext(), "Welcome to GuardianX", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(getApplicationContext(), "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            binding.emailEt.setError("Email is already registered, Please Sign In!");
                            return;
                        }
                    }
                });
    }


}