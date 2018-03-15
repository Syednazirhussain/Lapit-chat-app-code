package com.example.supertec.myapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private Toolbar mToolbar;
    private TextInputLayout mEmail,mPassword;
    private Button mLoginBtn;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        mProgressDialog = new ProgressDialog(LoginActivity.this);
        mToolbar = (Toolbar)findViewById(R.id.register_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Login");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mEmail = (TextInputLayout)findViewById(R.id.login_email_text);
        mPassword = (TextInputLayout)findViewById(R.id.login_password_text);
        mLoginBtn = (Button)findViewById(R.id.login);

        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String email = mEmail.getEditText().getText().toString();
                String password = mPassword.getEditText().getText().toString();

                if (!email.isEmpty() && !password.isEmpty()){

                    mProgressDialog.setTitle("Logging In");
                    mProgressDialog.setMessage("Please wait while we check your credential.");
                    mProgressDialog.setCanceledOnTouchOutside(false);
                    mProgressDialog.show();
                    loginUser(email,password);

                }else{
                    Toast.makeText(LoginActivity.this,"Please fillOut missing fields",Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void loginUser(String email, String password) {

        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    mProgressDialog.dismiss();

                    String CurrentUID = mAuth.getCurrentUser().getUid();
                    String deviceToken  = FirebaseInstanceId.getInstance().getToken();

                    mDatabase.child(CurrentUID).child("device_token").setValue(deviceToken).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(LoginActivity.this,"Connectivity problem",Toast.LENGTH_SHORT).show();
                        }
                    });

                }else{
                    mProgressDialog.hide();
                    Toast.makeText(LoginActivity.this,"Cannot Login please check your email and password is correct",Toast.LENGTH_LONG).show();
                }
            }
        });

    }
}
