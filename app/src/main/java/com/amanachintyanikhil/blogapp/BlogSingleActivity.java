package com.amanachintyanikhil.blogapp;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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


public class BlogSingleActivity extends AppCompatActivity {

    android.support.v7.widget.Toolbar toolbar;
    ImageView blogimage;
    TextView title,description;
    DatabaseReference mBlog;
    String mPostkey=null;
    Button removePost;
    FirebaseAuth mauth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blog_single);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        toolbar=(android.support.v7.widget.Toolbar)findViewById(R.id.tb);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");
        toolbar.setNavigationIcon(R.drawable.navigationicon);
        getSupportActionBar().show();
        mPostkey=getIntent().getStringExtra("post_id");

        removePost=(Button)findViewById(R.id.removepost);

        blogimage=(ImageView)findViewById(R.id.blogimage);
        title=(TextView)findViewById(R.id.title);
        description=(TextView)findViewById(R.id.description);

        mBlog= FirebaseDatabase.getInstance().getReference().child("Vlog");
        mauth=FirebaseAuth.getInstance();

        Log.d("Firebase Key", mPostkey);

        mBlog.child(mPostkey.toString()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
               String blog_tit=dataSnapshot.child("title").getValue().toString();
               String blog_description=dataSnapshot.child("desc").getValue().toString();
               String blog_image=dataSnapshot.child("image").getValue().toString();
               String uid=dataSnapshot.child("uid").getValue().toString();

               title.setText(blog_tit);
               description.setText(blog_description);
                Picasso.with(BlogSingleActivity.this).load(blog_image).into(blogimage);

                if(mauth.getCurrentUser().getUid().equals(uid))
                {
                    removePost.setVisibility(View.VISIBLE);

                    removePost.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view)
                        {
                            mBlog.child(mPostkey).removeValue();

                            Intent mainactivity=new Intent(BlogSingleActivity.this,MainActivity.class);
                            mainactivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(mainactivity);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });




    }
}
