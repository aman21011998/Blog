package com.amanachintyanikhil.blogapp;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.Fade;
import android.transition.Slide;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AnticipateOvershootInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;


public class MyPosts extends AppCompatActivity {

    android.support.v7.widget.Toolbar toolbar;
    RecyclerView mBlogList;
    DatabaseReference mBlogCurrentUser;
    FirebaseAuth mauth;
    Query currentuseridquery;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_posts);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        toolbar=(android.support.v7.widget.Toolbar)findViewById(R.id.tb);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("MyPosts");
        toolbar.setNavigationIcon(R.drawable.navigationicon);
        getSupportActionBar().show();

        mBlogList=(RecyclerView)findViewById(R.id.posts);
        mBlogList.setHasFixedSize(true);
        mBlogList.setLayoutManager(new LinearLayoutManager(this));

        mBlogCurrentUser= FirebaseDatabase.getInstance().getReference().child("Vlog");
        mauth=FirebaseAuth.getInstance();
        String id=mauth.getCurrentUser().getUid();
        currentuseridquery=mBlogCurrentUser.orderByChild("uid").equalTo(id);


    }

    @Override
    public boolean onSupportNavigateUp()
    {
        finishAfterTransition();
        return true;
    }




    @Override
    protected void onStart() {
        super.onStart();

      //  firebaseAuth.addAuthStateListener(authStateListener);
        FirebaseRecyclerAdapter<Blog,MainActivity.BlogViewHolder> firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<Blog, MainActivity.BlogViewHolder>(
                Blog.class,R.layout.blog_row,MainActivity.BlogViewHolder.class,currentuseridquery

        ) {
            @Override
            protected void populateViewHolder(MainActivity.BlogViewHolder viewHolder, Blog model, int position) {

                final String post_key=getRef(position).toString();
                viewHolder.setTitle(model.getTitle());
                viewHolder.setDesc(model.getDesc());
                viewHolder.setImage(model.getImage(),getApplicationContext());
               // viewHolder.setUname(model.getUsername());
                // viewHolder.setLikeButton(post_key);
                viewHolder.mview.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //Toast.makeText(MainActivity.this, post_key, Toast.LENGTH_SHORT).show();
                        Intent blogsinglactivity=new Intent(MyPosts.this,BlogSingleActivity.class);
                        blogsinglactivity.putExtra("post_id",post_key);
                        startActivity(blogsinglactivity);
                    }
                });
                viewHolder.likebutton.setVisibility(View.INVISIBLE);
                viewHolder.commentbutton.setVisibility(View.INVISIBLE);


            }
        };
        mBlogList.setAdapter(firebaseRecyclerAdapter);

    }
    public static class BlogViewHolder extends RecyclerView.ViewHolder
    {
        View mview;
        ImageButton likebutton,commentbutton;
        DatabaseReference mLike;
        //TextView usrname;
        FirebaseAuth mauth;
        public BlogViewHolder(View itemView) {
            super(itemView);
            mview=itemView;
            likebutton=(ImageButton)mview.findViewById(R.id.likebtn_grey);
            commentbutton=(ImageButton)mview.findViewById(R.id.comment);
          // usrname=(TextView)mview.findViewById(R.id.username);
           // mLike=FirebaseDatabase.getInstance().getReference().child("Likes");
            mauth=FirebaseAuth.getInstance();

        }


        public void setTitle(String title)
        {
            TextView posttitle=(TextView)mview.findViewById(R.id.posttitle);
            posttitle.setText(title);
        }
        public void setDesc(String desc)
        {
            TextView postdesc=(TextView)mview.findViewById(R.id.postdesc);
            postdesc.setText(desc);
        }
        public void setImage(String image,Context context)
        {
            ImageView imageView=(ImageView)mview.findViewById(R.id.postimage);
            Picasso.with(context).load(image).into(imageView);
        }


    }
}
