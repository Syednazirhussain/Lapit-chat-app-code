package com.example.supertec.myapp;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity {

    private ImageView mProfileImage;
    private TextView mDisplayName,mStatus,mTotalFriend;
    private Button mSendFriendRequestBtn,mDeclineBtn;

    private ProgressDialog mProgressDialog;
    private String mCurrent_state;

    private DatabaseReference mDatabase;
    private DatabaseReference mFriendRequestDatabase;
    private DatabaseReference mFriendsDatabase;
    private DatabaseReference mNotificationDatabase;

    private FirebaseUser mCurrentUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mProfileImage = (ImageView) findViewById(R.id.view_person_profilePic);
        mDisplayName = (TextView) findViewById(R.id.view_person_name);
        mStatus = (TextView) findViewById(R.id.view_person_status);
        mTotalFriend = (TextView) findViewById(R.id.view_person_totalFrd);
        mSendFriendRequestBtn = (Button) findViewById(R.id.send_friend_requestBtn);
        mDeclineBtn = (Button) findViewById(R.id.decline_friend_request);

        mCurrent_state = "not_friends";

        mProgressDialog = new ProgressDialog(ProfileActivity.this);
        mProgressDialog.setTitle("Loading user data");
        mProgressDialog.setMessage("Please wait a while");
        mProgressDialog.setCanceledOnTouchOutside(false);

        mCurrentUsers = FirebaseAuth.getInstance().getCurrentUser();

        mFriendRequestDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req");
        mFriendsDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        mNotificationDatabase = FirebaseDatabase.getInstance().getReference().child("notifications");

        final String userId = getIntent().getStringExtra("Key");
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mDisplayName.setText(dataSnapshot.child("name").getValue().toString());
                mStatus.setText(dataSnapshot.child("status").getValue().toString());
                String image = dataSnapshot.child("image").getValue().toString();
                Picasso.with(ProfileActivity.this).load(image).into(mProfileImage);

                // Friend List / Request feature

                mFriendRequestDatabase.child(mCurrentUsers.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(userId)){
                            String req_type = dataSnapshot.child(userId).child("request_type").getValue().toString();
                            if (req_type.equals("received")){
                                mCurrent_state = "req_received";
                                mSendFriendRequestBtn.setText("ACCEPT FRIEND REQUEST");
                                mDeclineBtn.setVisibility(View.VISIBLE);
                                mDeclineBtn.setEnabled(true);

                            }else if(req_type.equals("sent")){

                                mCurrent_state = "req_sent";
                                mSendFriendRequestBtn.setText("CANCEL FRIEND REQUEST");

                                mDeclineBtn.setVisibility(View.INVISIBLE);
                                mDeclineBtn.setEnabled(false);

                            }
                            mProgressDialog.dismiss();
                        }else{

                            mDeclineBtn.setVisibility(View.INVISIBLE);
                            mDeclineBtn.setEnabled(false);

                            mFriendsDatabase.child(mCurrentUsers.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChild(userId)){
                                        mCurrent_state = "friends";
                                        mSendFriendRequestBtn.setText("Un friend");
                                    }
                                    mProgressDialog.dismiss();
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    mProgressDialog.dismiss();
                                }
                            });

                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mSendFriendRequestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSendFriendRequestBtn.setEnabled(false);

                // ------------------ NOT FRIEND STATE ---------------- //
                if (mCurrent_state.equals("not_friends")){
                    mFriendRequestDatabase.child(mCurrentUsers.getUid()).child(userId).child("request_type")
                            .setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){

                                mFriendRequestDatabase.child(userId).child(mCurrentUsers.getUid()).child("request_type")
                                        .setValue("received").addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        HashMap<String,String> notify = new HashMap<>();
                                        notify.put("from",mCurrentUsers.getUid());
                                        notify.put("type","request");

                                        mNotificationDatabase.child(userId).push().setValue(notify).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {

                                                mCurrent_state = "req_sent";
                                                mSendFriendRequestBtn.setText("CANCEL FRIEND REQUEST");
                                                mDeclineBtn.setVisibility(View.INVISIBLE);
                                                mDeclineBtn.setEnabled(false);

                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {

                                            }
                                        });

                                        Toast.makeText(ProfileActivity.this,"Request Sent Successfull",Toast.LENGTH_LONG).show();

                                    }
                                });

                            }else {
                                Toast.makeText(ProfileActivity.this,"Failed sending request",Toast.LENGTH_LONG).show();
                            }
                            mSendFriendRequestBtn.setEnabled(true);
                        }
                    });
                }
                // ------------- FRIEND STATE ---------------
                if(mCurrent_state.equals("friends")){
                    mFriendsDatabase.child(mCurrentUsers.getUid()).child(userId).removeValue()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                     mFriendsDatabase.child(userId).child(mCurrentUsers.getUid()).removeValue()
                                             .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                 @Override
                                                 public void onSuccess(Void aVoid) {
                                                     mCurrent_state = "not_friends";
                                                     mSendFriendRequestBtn.setText("Send Friend Request");
                                                 }
                                             });
                                }
                            });
                }
                // -------------- CANCEL REQUEST STATE ----------------
                if(mCurrent_state.equals("req_sent")){

                    mFriendRequestDatabase.child(mCurrentUsers.getUid()).child(userId).removeValue()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mFriendRequestDatabase.child(userId).child(mCurrentUsers.getUid()).removeValue()
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {

                                                    mNotificationDatabase.child(userId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            mCurrent_state = "not_friends";
                                                            mSendFriendRequestBtn.setText("Send Friend Request");
                                                            mDeclineBtn.setVisibility(View.INVISIBLE);
                                                            mDeclineBtn.setEnabled(false);
                                                        }
                                                    });

                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            mSendFriendRequestBtn.setEnabled(true);
                                        }
                                    });
                                }
                            });

                }
                // ------------ REQUEST RECIEVED STATE -----------------------
                if(mCurrent_state.equals("req_received")){

                    final String currentDate = DateFormat.getDateTimeInstance().format(new Date());

                    mFriendsDatabase.child(mCurrentUsers.getUid()).child(userId).setValue(currentDate)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    mFriendsDatabase.child(userId).child(mCurrentUsers.getUid()).setValue(currentDate)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {

                                                    mFriendRequestDatabase.child(mCurrentUsers.getUid()).child(userId).removeValue()
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    mFriendRequestDatabase.child(userId).child(mCurrentUsers.getUid()).removeValue()
                                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                @Override
                                                                                public void onSuccess(Void aVoid) {

                                                                                    mNotificationDatabase.child(mCurrentUsers.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                        @Override
                                                                                        public void onSuccess(Void aVoid) {
                                                                                            mSendFriendRequestBtn.setEnabled(true);
                                                                                            mCurrent_state = "friends";
                                                                                            mSendFriendRequestBtn.setText("Un friend");
                                                                                            mDeclineBtn.setVisibility(View.INVISIBLE);
                                                                                            mDeclineBtn.setEnabled(false);
                                                                                        }
                                                                                    });
                                                                                }
                                                                            }).addOnFailureListener(new OnFailureListener() {
                                                                        @Override
                                                                        public void onFailure(@NonNull Exception e) {
                                                                            mSendFriendRequestBtn.setEnabled(true);
                                                                        }
                                                                    });
                                                                }
                                                    });
                                                }
                                            });
                                }
                            });
                }

            }
        });
    }
}
