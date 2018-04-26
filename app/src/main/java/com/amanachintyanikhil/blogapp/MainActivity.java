package com.amanachintyanikhil.blogapp;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.Slide;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;


public class MainActivity extends AppCompatActivity {

    android.support.v7.widget.Toolbar maintoolbar;
    RecyclerView mBlogList;
    DatabaseReference mBlog,mLikedatabase,mUserdatabase;
    FirebaseAuth firebaseAuth;
    FirebaseAuth.AuthStateListener authStateListener;
    boolean mProcessLikebutton=false;
    RelativeLayout relativeLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        //MultiDex.install(this);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        firebaseAuth=FirebaseAuth.getInstance();
        //below function is used to check if a user is logged in or not
        authStateListener=new FirebaseAuth.AuthStateListener()
        {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth)
            {
                if(firebaseAuth.getCurrentUser()==null)
                {
                    ActivityOptions activityOptions=ActivityOptions.makeSceneTransitionAnimation(MainActivity.this);
                    Intent loginactivity=new Intent(MainActivity.this,FrontActivity.class);
                    loginactivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(loginactivity,activityOptions.toBundle());
                }
            }
        };
        maintoolbar= (android.support.v7.widget.Toolbar) findViewById(R.id.tb);
        relativeLayout=(RelativeLayout)findViewById(R.id.relativelayout);
        setSupportActionBar(maintoolbar);
        getSupportActionBar().setTitle("Blogger");
        maintoolbar.setTitleTextColor(getResources().getColor(R.color.white));
        //maintoolbar.setOverflowIcon(getDrawable(R.drawable.logo));

        getSupportActionBar().show();

        mBlogList=(RecyclerView)findViewById(R.id.recyclerview);
        mBlogList.setHasFixedSize(true);
        mBlogList.setLayoutManager(new LinearLayoutManager(this));

        mBlog= FirebaseDatabase.getInstance().getReference().child("Vlog");
        mLikedatabase=FirebaseDatabase.getInstance().getReference().child("Likes");
        mUserdatabase=FirebaseDatabase.getInstance().getReference().child("User");



    }

    private void setUpWindowAnimations()
    {
        Slide slidetransition=new Slide();
        slidetransition.setSlideEdge(Gravity.LEFT);
        slidetransition.setDuration(1000);

        getWindow().setReenterTransition(slidetransition); //when we return to the main activity than animation occurs
        getWindow().setAllowReturnTransitionOverlap(false); //prevents overlapping of transition


        getWindow().setExitTransition(slidetransition);  //while exiting the main activity

    }

    @Override
    protected void onStart() {
        super.onStart();
        setUpWindowAnimations();
        firebaseAuth.addAuthStateListener(authStateListener);
        FirebaseRecyclerAdapter<Blog,BlogViewHolder> firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<Blog, BlogViewHolder>(
                Blog.class,R.layout.blog_row,BlogViewHolder.class,mBlog

        ) {
            @Override
            protected void populateViewHolder(BlogViewHolder viewHolder, Blog model, int position) {

                final String post_key=getRef(position).getKey();
                viewHolder.setTitle(model.getTitle());
                viewHolder.setDesc(model.getDesc());
                viewHolder.setImage(model.getImage(),getApplicationContext());
                viewHolder.setUname(model.getUsername());
                viewHolder.setLikeButton(post_key);
                viewHolder.mview.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //Toast.makeText(MainActivity.this, post_key, Toast.LENGTH_SHORT).show();
                        Intent blogsinglactivity=new Intent(MainActivity.this,BlogSingleActivity.class);
                        blogsinglactivity.putExtra("post_id",post_key);
                        startActivity(blogsinglactivity);
                    }
                });

                viewHolder.commentbutton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent commentactivity=new Intent(MainActivity.this, CommentActivity.class);
                        commentactivity.putExtra("postkey",post_key);
                        startActivity(commentactivity);
                    }
                });
                viewHolder.user.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent profileview=new Intent(MainActivity.this,ProfileView.class);
                        profileview.putExtra("post_id",post_key);
                        startActivity(profileview);
                    }
                });
                viewHolder.likebutton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        mProcessLikebutton=true;


                        mLikedatabase.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if(mProcessLikebutton)
                                {
                                    //if (dataSnapshot.child(post_key).exists()) {
                                        if (dataSnapshot.child(post_key).hasChild(firebaseAuth.getCurrentUser().getUid())) {
                                            mLikedatabase.child(post_key).child(firebaseAuth.getCurrentUser().getUid()).removeValue();
                                            mProcessLikebutton = false;
                                        } else {
                                            final String id = firebaseAuth.getCurrentUser().getUid();
                                            mUserdatabase.child(id).addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {

                                                    String name = dataSnapshot.child("name").getValue().toString();
                                                    mLikedatabase.child(post_key).child(id).setValue(name);
                                                    mProcessLikebutton = false;
                                                    //Toast.makeText(MainActivity.this, "Liked", Toast.LENGTH_SHORT).show();
                                                    Snackbar.make(relativeLayout, "Liked", Snackbar.LENGTH_LONG).show();
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });

                                        }
                                    //}
                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                    }
                });
            }
        };
        mBlogList.setAdapter(firebaseRecyclerAdapter);

    }

    public static class BlogViewHolder extends RecyclerView.ViewHolder
    {
        View mview;
        ImageButton likebutton,commentbutton;
        DatabaseReference mLike;
        FirebaseAuth mauth;
        TextView user;
        public BlogViewHolder(View itemView) {
            super(itemView);
            mview=itemView;

            likebutton=(ImageButton)mview.findViewById(R.id.likebtn_grey);
            commentbutton=(ImageButton)mview.findViewById(R.id.comment);
            user=(TextView)mview.findViewById(R.id.username);
            mLike=FirebaseDatabase.getInstance().getReference().child("Likes");
            mauth=FirebaseAuth.getInstance();

        }

        public void setLikeButton(final String key)
        {
            mLike.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                        if(dataSnapshot.child(key).hasChild(mauth.getCurrentUser().getUid()))
                        {
                            likebutton.setImageResource(R.drawable.thumbs_red);
                        }
                        else
                        {
                            likebutton.setImageResource(R.drawable.thums_grey);
                        }
                    }


                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
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

        public void setUname(String nm)
        {
            TextView postuname=(TextView)mview.findViewById(R.id.username);
            postuname.setText("~ "+nm);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return super.onCreateOptionsMenu(menu);
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {

        if(item.getItemId()==R.id.followers)
        {
            Intent allfollowers=new Intent(MainActivity.this,AllFollowers.class);
            startActivity(allfollowers);
        }
        if(item.getItemId()==R.id.settings)
        {
            Intent settings=new Intent(MainActivity.this,Settings.class);
            startActivity(settings);
        }
        if(item.getItemId()==R.id.addpost)
        {
            ActivityOptions activityOptions=ActivityOptions.makeSceneTransitionAnimation(this);
            Intent postactivity = new Intent(MainActivity.this, PostActivity.class);
            startActivity(postactivity,activityOptions.toBundle());
        }
        if(item.getItemId()==R.id.logout)
        {
            firebaseAuth.signOut();
        }
        if(item.getItemId()==R.id.profile)
        {
            ActivityOptions activityOptions=ActivityOptions.makeSceneTransitionAnimation(this);
            Intent userprofile=new Intent(MainActivity.this,UserProfile.class);
            startActivity(userprofile,activityOptions.toBundle());
        }
        if(item.getItemId()==R.id.myposts)
        {

            Intent myposts=new Intent(MainActivity.this,MyPosts.class);
            startActivity(myposts);
        }
        return super.onOptionsItemSelected(item);
    }
}


