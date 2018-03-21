package com.thenewboston.blogger;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.HashMap;

public class SetupActivity extends AppCompatActivity {

    Toolbar accountoolbar;
    ImageButton imageButton;
    EditText name;
    Button submit;
    Uri mImageuri;
    DatabaseReference userdb;
    FirebaseAuth mauth;
    Spinner gender;
    ProgressDialog pd;
    String gend[]={"Male", "Female"};
    String g; //gender selection
    StorageReference storageReference;
    Uri downloaduri=null;
    String uname,uphone,uemail;
    private static final int PICK=1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        accountoolbar=(Toolbar)findViewById(R.id.tb);
        setSupportActionBar(accountoolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Account Setup");
        getSupportActionBar().show();

        uname=getIntent().getStringExtra("name");
        uemail=getIntent().getStringExtra("email");
        uphone=getIntent().getStringExtra("phone");

        gender=(Spinner)findViewById(R.id.gender);
        imageButton=(ImageButton)findViewById(R.id.profilepicture);
        name=(EditText) findViewById(R.id.name);
        submit=(Button)findViewById(R.id.button2);
        userdb= FirebaseDatabase.getInstance().getReference().child("User");
        mauth=FirebaseAuth.getInstance();
        storageReference= FirebaseStorage.getInstance().getReference().child("Profile_images");
        pd=new ProgressDialog(this);

        ArrayAdapter aa=new ArrayAdapter(this,R.layout.support_simple_spinner_dropdown_item,gend);
        aa.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        gender.setAdapter(aa);

        gender.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                 g=adapterView.getItemAtPosition(i).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                startsettingaccount();
            }
        });
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryintent=new Intent(Intent.ACTION_GET_CONTENT);
                galleryintent.setType("image/+");
                startActivityForResult(galleryintent,PICK);
            }
        });

    }

    private void startsettingaccount()
    {
        pd.setMessage("Saving ur details....");
        pd.setCanceledOnTouchOutside(false);
        pd.show();
        final String id=mauth.getCurrentUser().getUid();
        final String nm=name.getText().toString();
        StorageReference filepath=storageReference.child(id);
        filepath.putFile(mImageuri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                downloaduri=taskSnapshot.getDownloadUrl();

                if(mImageuri!=null && !nm.isEmpty())
                {

                    String desc=name.getText().toString();
                    HashMap<String,String> map=new HashMap<>();
                    map.put("image",downloaduri.toString());
                    map.put("gender",g);
                    map.put("description",desc);
                    map.put("name",uname);
                    map.put("phone",uphone);
                    map.put("email",uemail);
                    userdb.child(id).setValue(map);
                    pd.dismiss();
                    Toast.makeText(SetupActivity.this, "Details succesfully saved", Toast.LENGTH_SHORT).show();
                    Intent mainactivity=new Intent(SetupActivity.this,MainActivity.class);
                    mainactivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(mainactivity);
                }
                else
                {
                    Toast.makeText(SetupActivity.this, "Fields can not be empty", Toast.LENGTH_SHORT).show();
                    pd.dismiss();
                }

            }
        });
        /*    if(mImageuri!=null && !nm.isEmpty())
            {

                String desc=name.getText().toString();
                HashMap<String,String> map=new HashMap<>();
                map.put("image",downloaduri.toString());
                map.put("gender",g);
                map.put("description",desc);
                userdb.child(id).setValue(map);
                pd.dismiss();
                Toast.makeText(this, "Details succesfully saved", Toast.LENGTH_SHORT).show();
                Intent mainactivity=new Intent(SetupActivity.this,MainActivity.class);
                mainactivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(mainactivity);
            }
            else
            {
                Toast.makeText(this, "Fields can not be empty", Toast.LENGTH_SHORT).show();
                pd.dismiss();
            }  */
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==PICK && resultCode==RESULT_OK)
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
