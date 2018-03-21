package com.thenewboston.blogger;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

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
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageActivity;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserProfile extends AppCompatActivity {

    Toolbar profiletoolbar;
    ImageView timeline;
    CircleImageView profilepic;
    TextView name,gender,phone,email;
    EditText profession,dob,address;
    DatabaseReference mUser,mProfile;
    FirebaseAuth mauth;
    String uname,ugender,uphone,uemail,uprofile;
    private static final int PICK=1;
    Uri mImageuri;
    StorageReference mTimeLineStorage;
    ProgressDialog pd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        profiletoolbar=(Toolbar)findViewById(R.id.tb);
        setSupportActionBar(profiletoolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");
        profiletoolbar.setNavigationIcon(R.drawable.navigationicon);
        getSupportActionBar().show();
        profiletoolbar.bringToFront();
        pd=new ProgressDialog(this);

        timeline=(ImageView)findViewById(R.id.timeline);
        profilepic=(CircleImageView)findViewById(R.id.circleImageView);
        name=(TextView)findViewById(R.id.Name);
        gender=(TextView)findViewById(R.id.gender);
        phone=(TextView)findViewById(R.id.phone);
        email=(TextView)findViewById(R.id.email);
        profession=(EditText)findViewById(R.id.Profession);
        dob=(EditText)findViewById(R.id.dob);
        //address=(EditText)findViewById(R.id.address);

        mUser= FirebaseDatabase.getInstance().getReference().child("User");
        mauth=FirebaseAuth.getInstance();
        mProfile=FirebaseDatabase.getInstance().getReference().child("Profile");
        mTimeLineStorage= FirebaseStorage.getInstance().getReference().child("Timeline");

        timeline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent gallery=new Intent(Intent.ACTION_GET_CONTENT);
                gallery.setType("image/+");
                startActivityForResult(gallery,PICK);

            }
        });

        mProfile.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild(mauth.getCurrentUser().getUid()))
                {
                    String id=mauth.getCurrentUser().getUid();
                    String prfs = dataSnapshot.child(id).child("profession").getValue().toString();
                    String date_of_birth = dataSnapshot.child(id).child("dob").getValue().toString();
                    String tmlnimg = dataSnapshot.child(id).child("timelineimage").getValue().toString();

                    profession.setText(prfs);
                    dob.setText(date_of_birth);
                    Picasso.with(UserProfile.this).load(tmlnimg).into(timeline);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        mUser.child(mauth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                uname=dataSnapshot.child("name").getValue().toString();
                ugender=dataSnapshot.child("gender").getValue().toString();
                uphone=dataSnapshot.child("phone").getValue().toString();
                uemail=dataSnapshot.child("email").getValue().toString();
                uprofile=dataSnapshot.child("image").getValue().toString();

                name.setText(uname);
                gender.setText(ugender);
                phone.setText(uphone);
                email.setText(uemail);
                Picasso.with(UserProfile.this).load(uprofile).into(profilepic);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==PICK && resultCode==RESULT_OK)
        {
            Uri uri=data.getData();
            CropImage.activity(uri).setAspectRatio(1,1).start(this);
        }

        if(requestCode== CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mImageuri = result.getUri();
                timeline.setImageURI(mImageuri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        pd.setMessage("Saving details....");
        pd.setCanceledOnTouchOutside(false);
        pd.show();
        StorageReference filepath=mTimeLineStorage.child(mImageuri.getLastPathSegment());
        filepath.putFile(mImageuri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                Uri downloadUri=taskSnapshot.getDownloadUrl();
                DatabaseReference local=mProfile.child(mauth.getCurrentUser().getUid());
                String uProffesion=profession.getText().toString();
                String uDob=dob.getText().toString();

                local.child("profession").setValue(uProffesion);
                local.child("timelineimage").setValue(downloadUri.toString());
                local.child("dob").setValue(uDob);

                pd.dismiss();
                Intent main=new Intent(UserProfile.this,MainActivity.class);
                startActivity(main);

            }
        });
    }
}
