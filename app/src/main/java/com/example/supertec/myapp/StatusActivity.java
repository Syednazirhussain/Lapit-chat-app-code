package com.example.supertec.myapp;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ProgressDialog mProgressDialog;

    //Android layout
    private TextInputLayout mStatus;
    private Button mSaveBtn;

    //Firebase code
    private DatabaseReference mDatabase;
    private FirebaseUser mCurrentUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        final String user_id = mCurrentUser.getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);

        mToolbar = (Toolbar) findViewById(R.id.status_appBar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Account Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mStatus = (TextInputLayout) findViewById(R.id.status_input);
        mSaveBtn = (Button) findViewById(R.id.status_save_btn);

        String current_status = getIntent().getStringExtra("CurrentStatus");
        mStatus.getEditText().setText(current_status);

        mSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mProgressDialog = new ProgressDialog(StatusActivity.this);
                mProgressDialog.setTitle("Saving Changes");
                mProgressDialog.setMessage("Please wait while we save the changes");
                mProgressDialog.show();

                String status = mStatus.getEditText().getText().toString();
                mDatabase.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            mProgressDialog.dismiss();
                        }else{
                            Toast.makeText(getApplicationContext(),"There was some error while saving changes",Toast.LENGTH_LONG).show();
                        }
                    }
                });

            }
        });


    }
}
