package com.example.supertec.myapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private TextInputLayout mFullName;
    private  TextInputLayout mEmail;
    private  TextInputLayout mPassword;
    private Button mRegBtn;
    private Toolbar mToolbar;
    private DatabaseReference mDatabase;

    private ProgressDialog mRegProgrss;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        mFullName = (TextInputLayout)findViewById(R.id.reg_display_name);
        mEmail = (TextInputLayout)findViewById(R.id.reg_email);
        mPassword = (TextInputLayout)findViewById(R.id.reg_password);
        mRegBtn = (Button)findViewById(R.id.reg_account_btn);

        mToolbar = (Toolbar)findViewById(R.id.register_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mRegProgrss = new ProgressDialog(this);

        mRegBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String display_name = mFullName.getEditText().getText().toString();
                String email = mEmail.getEditText().getText().toString();
                String password = mPassword.getEditText().getText().toString();

                if (!display_name.isEmpty() && !email.isEmpty() && !password.isEmpty()){

                    mRegProgrss.setTitle("Registering User");
                    mRegProgrss.setMessage("Please wait while we create your account !");
                    mRegProgrss.setCanceledOnTouchOutside(false);
                    mRegProgrss.show();
                    register_user(display_name,email,password);

                }else{
                    Toast.makeText(getApplicationContext(),"Please fill out empty field",Toast.LENGTH_LONG).show();
                }


            }
        });

    }

    private void register_user(final String display_name, String email, String password) {

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){

                    FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();
                    String uid = current_user.getUid();
                    String deviceToken = FirebaseInstanceId.getInstance().getToken();
                    mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

                    HashMap<String,String> userMap = new HashMap<>();
                    userMap.put("device_token",deviceToken);
                    userMap.put("name",display_name);
                    userMap.put("status","Hi there , i m using Office App");
                    userMap.put("image","https://firebasestorage.googleapis.com/v0/b/myapp-16956.appspot.com/o/profile_images%2Fthumbs%2Fdefault.jpg?alt=media&token=d5c97830-3ec2-4940-9ef8-a8ea7c246cea");
                    userMap.put("thumb_image","default");
                    mDatabase.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){

                                mRegProgrss.dismiss();
                                Intent mainIntent = new Intent(getApplicationContext(),MainActivity.class);
                                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(mainIntent);
                                finish();

                            }
                        }
                    });

                }else {

                    mRegProgrss.hide();
                    Toast.makeText(getApplicationContext(),"Cannot sign in. Please check the form and try again",Toast.LENGTH_LONG).show();

                }
            }
        });

    }
}
