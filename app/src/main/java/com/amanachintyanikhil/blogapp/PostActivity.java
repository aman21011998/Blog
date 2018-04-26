package com.amanachintyanikhil.blogapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.transition.Explode;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;


public class PostActivity extends AppCompatActivity {

    android.support.v7.widget.Toolbar toolbar;
    ImageButton imageButton;
    EditText title,description;
    Button submit;
    public static final int GALLERY_REQUEST=1;
    Uri mImageuri,downloaduri;
    StorageReference mstorage;
    DatabaseReference mBlog,mUser;
    ProgressDialog progressDialog;
    FirebaseAuth mauth;
    String id;
    MyStringRandomGen  rn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        initAnimation();
        rn=new MyStringRandomGen();
        mauth=FirebaseAuth.getInstance();
        id=mauth.getCurrentUser().getUid();
        mUser=FirebaseDatabase.getInstance().getReference().child("User").child(id);
        toolbar= (android.support.v7.widget.Toolbar) findViewById(R.id.tb);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Post New Blog");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        toolbar.setNavigationIcon(R.drawable.navigationicon);
        getSupportActionBar().show();


        mBlog= FirebaseDatabase.getInstance().getReference().child("Vlog");
        progressDialog=new ProgressDialog(this);
        imageButton=(ImageButton)findViewById(R.id.imageButton);
        title=(EditText)findViewById(R.id.title);
        description=(EditText)findViewById(R.id.description);
        submit=(Button)findViewById(R.id.submit);

        mstorage= FirebaseStorage.getInstance().getReference();
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startposting();
            }
        });

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryintent=new Intent(Intent.ACTION_GET_CONTENT);
                galleryintent.setType("image/+");
                startActivityForResult(galleryintent,GALLERY_REQUEST);
            }
        });

    }

    @Override
    public boolean onSupportNavigateUp()
    {
        finishAfterTransition();
        return super.onSupportNavigateUp();
    }

    private void initAnimation()
    {
        Explode entertransition=new Explode();
        entertransition.setDuration(getResources().getInteger(R.integer.anim_duration_long));
        getWindow().setEnterTransition(entertransition);
    }

    private void startposting()
    {
        final String blog_title=title.getText().toString();
        final String blog_description=description.getText().toString();


        progressDialog.setMessage("Posting blog.....");
        progressDialog.show();
        if(!blog_title.isEmpty() && !blog_description.isEmpty() && mImageuri!=null)
        {
            StorageReference filepath=mstorage.child("Blog_images").child(mImageuri.getLastPathSegment());
            filepath.putFile(mImageuri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    downloaduri=taskSnapshot.getDownloadUrl();
                    String blogkey=rn.generateRandomString();
                    final DatabaseReference newpost=mBlog.push();
                    //final DatabaseReference newpost=mBlog.push();
                    mUser.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot)
                        {
                            String uname=dataSnapshot.child("name").getValue().toString();
                            newpost.child("title").setValue(blog_title);
                            newpost.child("desc").setValue(blog_description);
                            newpost.child("image").setValue(downloaduri.toString());
                            newpost.child("uid").setValue(id);
                            newpost.child("username").setValue(uname);

                            progressDialog.dismiss();
                            Toast.makeText(PostActivity.this, "Posted Successfully", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                    Intent mainactivity=new Intent(PostActivity.this,MainActivity.class);
                    mainactivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(mainactivity);
                }

            });
        }
        else
        {
            Toast.makeText(this, "Fields can not be left blank", Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==GALLERY_REQUEST && resultCode==RESULT_OK)
        {
            Uri imageuri=data.getData();
            CropImage.activity(imageuri).setAspectRatio(1,1).start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mImageuri = result.getUri();
                imageButton.setImageURI(mImageuri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}
