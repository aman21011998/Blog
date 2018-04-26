package com.amanachintyanikhil.blogapp;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;


public class ProfileView extends AppCompatActivity {

    Toolbar toolbar;
    ImageView timeline,profilepic;
    TextView uname,uprofession,udob,ugender,uphone,uemail;
    DatabaseReference blog,user,profile,follow;
    String key,userid,currentuserid;
    Button fllw;
    FirebaseAuth mauth;
    String currentuname;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_view);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        toolbar=(Toolbar)findViewById(R.id.profiletb);
        timeline=(ImageView)findViewById(R.id.timeline);
        profilepic=(ImageView)findViewById(R.id.circleImageView);
        uname=(TextView)findViewById(R.id.Name);
        uprofession=(TextView)findViewById(R.id.Profession);
        udob=(TextView)findViewById(R.id.dob);
        ugender=(TextView)findViewById(R.id.gender);
        uphone=(TextView)findViewById(R.id.phone);
        uemail=(TextView)findViewById(R.id.email);
        fllw=(Button)findViewById(R.id.follow);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");
        getSupportActionBar().show();
        toolbar.setNavigationIcon(R.drawable.navigationicon);
        toolbar.bringToFront();

        key=getIntent().getStringExtra("post_id");
        blog= FirebaseDatabase.getInstance().getReference("Vlog");
        user=FirebaseDatabase.getInstance().getReference().child("User");
        profile=FirebaseDatabase.getInstance().getReference().child("Profile");
        follow=FirebaseDatabase.getInstance().getReference().child("Follow");
        mauth=FirebaseAuth.getInstance();
        currentuserid=mauth.getCurrentUser().getUid();
        //fetching data from vlog

        user.child(currentuserid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                currentuname=dataSnapshot.child("name").getValue().toString();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        blog.child(key.toString()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                userid=dataSnapshot.child("uid").getValue().toString();
                Log.d("userid",userid);
                //Toast.makeText(ProfileView.this,"Userid= "+userid, Toast.LENGTH_SHORT).show();
                if(userid.equals(currentuserid))
                {
                   fllw.setVisibility(View.INVISIBLE);
                }
                else
                {
                    fllw.setVisibility(View.VISIBLE);
                    fllw.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String text=fllw.getText().toString();
                            if(text.equals("Follow"))
                            {
                                follow.child(userid).child(currentuserid).setValue(currentuname);
                                fllw.setText("Unfollow");
                            }
                            else
                            {
                                follow.child(userid).child(currentuserid).removeValue();
                                fllw.setText("Follow");
                            }

                        }
                    });
                }


                user.child(userid).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        uemail.setText(dataSnapshot.child("email").getValue().toString());
                        uphone.setText(dataSnapshot.child("phone").getValue().toString());
                        ugender.setText(dataSnapshot.child("gender").getValue().toString());
                        uname.setText(dataSnapshot.child("name").getValue().toString());
                        Picasso.with(getApplicationContext()).load(dataSnapshot.child("image").getValue().toString()).into(profilepic);

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


                //fetching data from Profile
                profile.child(userid).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        udob.setText(dataSnapshot.child("dob").getValue().toString());
                        uprofession.setText(dataSnapshot.child("profession").getValue().toString());
                        Picasso.with(getApplicationContext()).load(dataSnapshot.child("timelineimage").getValue().toString()).into(timeline);
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




    }
}
