package com.amanachintyanikhil.blogapp;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;


import de.hdodenhof.circleimageview.CircleImageView;

public class CommentActivity extends AppCompatActivity {

    android.support.v7.widget.Toolbar toolbar;
    FirebaseAuth mauth;
    String postid;
    String currentuserid;
    DatabaseReference mComment,mUser;
    String username;
    EditText commentbox;
    ImageButton send;
    RecyclerView commentlist;
     String name,photo;
     String cmmnt;
    DatabaseReference blogid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        toolbar=(android.support.v7.widget.Toolbar)findViewById(R.id.tb);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Comments");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().show();
        toolbar.setNavigationIcon(R.drawable.navigationicon);

        commentbox=(EditText)findViewById(R.id.editText);
        send=(ImageButton)findViewById(R.id.imageButton3);
        commentlist=(RecyclerView)findViewById(R.id.commentview);

        postid=getIntent().getStringExtra("postkey");
        currentuserid=FirebaseAuth.getInstance().getCurrentUser().getUid();
        mComment= FirebaseDatabase.getInstance().getReference().child("Comment");
        mUser=FirebaseDatabase.getInstance().getReference().child("User");

        commentlist.setHasFixedSize(true);
        commentlist.setLayoutManager(new LinearLayoutManager(this));

        mUser.child(currentuserid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                name=dataSnapshot.child("name").getValue().toString();
                photo=dataSnapshot.child("image").getValue().toString();

                blogid=mComment.child(postid);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cmmnt=commentbox.getText().toString();
                blogid.child("comment").setValue(cmmnt);
                blogid.child("username").setValue(name);
                blogid.child("image").setValue(photo);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Comment,BlogViewHolder> firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<Comment, BlogViewHolder>(
                Comment.class,R.layout.comment_row,BlogViewHolder.class,mComment
        ) {
            @Override
            protected void populateViewHolder(BlogViewHolder viewHolder, Comment model, int position)
            {
               viewHolder.setUsername(model.getUsername());
               viewHolder.setComment(model.getComment());
               viewHolder.setImage(model.getImage(),getApplicationContext());
            }
        };

        commentlist.setAdapter(firebaseRecyclerAdapter);
    }

    public static class BlogViewHolder extends RecyclerView.ViewHolder
    {
        View mview;

        public BlogViewHolder(View itemView) {
            super(itemView);
            mview=itemView;

        }

        public void setUsername(String uname)
        {
            TextView unamefield=(TextView)mview.findViewById(R.id.username);
            unamefield.setText(uname);
        }

        public void setComment(String cmmt)
        {
            TextView cmmntfield=(TextView)mview.findViewById(R.id.comment);
            cmmntfield.setText(cmmt);
        }

        public void setImage(String image,Context context)
        {
            CircleImageView imgv=(CircleImageView)mview.findViewById(R.id.civ);
            Picasso.with(context).load(image).placeholder(R.drawable.person).into(imgv);
        }
    }
}
