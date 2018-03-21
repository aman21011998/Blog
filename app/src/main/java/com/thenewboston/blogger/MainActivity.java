package com.thenewboston.blogger;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.multidex.MultiDex;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Comment;

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
                    Intent loginactivity=new Intent(MainActivity.this,FrontActivity.class);
                    loginactivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(loginactivity);
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

        mBlog= FirebaseDatabase.getInstance().getReference().child("Blog");
        mLikedatabase=FirebaseDatabase.getInstance().getReference().child("Likes");
        mUserdatabase=FirebaseDatabase.getInstance().getReference().child("User");

    }

    @Override
    protected void onStart() {
        super.onStart();

        firebaseAuth.addAuthStateListener(authStateListener);
        FirebaseRecyclerAdapter<Blog,BlogViewHolder> firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<Blog, BlogViewHolder>(
                Blog.class,R.layout.blog_row,BlogViewHolder.class,mBlog

        ) {
            @Override
            protected void populateViewHolder(BlogViewHolder viewHolder, Blog model, int position) {

                final String post_key=getRef(position).toString();
                viewHolder.setTitle(model.getTitle());
                viewHolder.setDesc(model.getDesc());
                viewHolder.setImage(model.getImage(),getApplicationContext());
                viewHolder.setUname(model.getUsername());
               // viewHolder.setLikeButton(post_key);
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
        public BlogViewHolder(View itemView) {
            super(itemView);
            mview=itemView;

            likebutton=(ImageButton)mview.findViewById(R.id.likebtn_grey);
            commentbutton=(ImageButton)mview.findViewById(R.id.comment);
            mLike=FirebaseDatabase.getInstance().getReference().child("Likes");
            mauth=FirebaseAuth.getInstance();

        }

      /*  public void setLikeButton(final String key)
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
        }  */
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

        if(item.getItemId()==R.id.addpost) {
            Intent postactivity = new Intent(MainActivity.this, PostActivity.class);
            startActivity(postactivity);
        }
        if(item.getItemId()==R.id.logout)
        {
            firebaseAuth.signOut();
        }
        if(item.getItemId()==R.id.profile)
        {
            Intent userprofile=new Intent(MainActivity.this,UserProfile.class);
            startActivity(userprofile);
        }
        if(item.getItemId()==R.id.myposts)
        {
            Intent myposts=new Intent(MainActivity.this,MyPosts.class);
            startActivity(myposts);
        }
        return super.onOptionsItemSelected(item);
    }
}


