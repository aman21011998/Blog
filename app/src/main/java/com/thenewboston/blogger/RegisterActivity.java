package com.thenewboston.blogger;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    EditText name,phone,password,email;
    Button register;
    android.support.v7.widget.Toolbar registertoolbar;
    FirebaseAuth firebaseAuth;
    DatabaseReference userdb,phonedb;
    ProgressDialog progressDialog;
    ImageButton show;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        firebaseAuth=FirebaseAuth.getInstance();
        userdb= FirebaseDatabase.getInstance().getReference().child("User");
        phonedb=FirebaseDatabase.getInstance().getReference().child("Phone");
        progressDialog=new ProgressDialog(this);

        registertoolbar=(android.support.v7.widget.Toolbar)findViewById(R.id.tb);
        setSupportActionBar(registertoolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Register");
        getSupportActionBar().show();

        name=(EditText)findViewById(R.id.name);
        phone=(EditText)findViewById(R.id.phone);
        password=(EditText)findViewById(R.id.password);
        email=(EditText)findViewById(R.id.email);
        register=(Button)findViewById(R.id.register);
        show=(ImageButton)findViewById(R.id.showpassword);

        show.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view)
            {
                if(!password.getText().toString().isEmpty())
            {
                password.setTransformationMethod(null);
            }

                return false;
            }
        });


       register.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               startregistering();
           }
       });
    }

    private void startregistering()
    {
        progressDialog.setMessage("Registering.......");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
        final String user_name=name.getText().toString();
        final String user_phone=phone.getText().toString();
        String user_password=password.getText().toString();
        final String user_email=email.getText().toString();

        if(!user_name.isEmpty() && !user_email.isEmpty() && !user_password.isEmpty() && !user_phone.isEmpty())
        {
            firebaseAuth.createUserWithEmailAndPassword(user_email,user_password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task)
                {
                        if(task.isSuccessful())
                        {
                            String id=firebaseAuth.getCurrentUser().getUid();
                            DatabaseReference currentuser=userdb.child(id);
                            HashMap<String,String> map=new HashMap<>();
                            map.put("name",user_name);
                            map.put("phone",user_phone);
                            map.put("email",user_email);
                            map.put("image","default");

                            HashMap<String,String> phonemap=new HashMap<>();
                            DatabaseReference db=phonedb.child(user_phone);
                            phonemap.put("email",user_email);
                            db.setValue(phonemap);

                            currentuser.setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful())
                                    {
                                        Toast.makeText(RegisterActivity.this, "Successfully registered", Toast.LENGTH_SHORT).show();


                                        progressDialog.dismiss();
                                        Intent mainactivity=new Intent(RegisterActivity.this,SetupActivity.class);
                                        mainactivity.putExtra("name",user_name);
                                        mainactivity.putExtra("phone",user_phone);
                                        mainactivity.putExtra("email",user_email);
                                        mainactivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(mainactivity);
                                    }
                                }
                            });
                        }

                }
            });
        }

        else
        {
            Toast.makeText(this, "All fields have to be filled", Toast.LENGTH_SHORT).show();
        }

    }
}
