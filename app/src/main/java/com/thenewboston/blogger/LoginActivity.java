package com.thenewboston.blogger;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

public class LoginActivity extends AppCompatActivity {

    Toolbar toolbar;
    EditText email,password;
    Button login;
    FirebaseAuth mauth;
    ProgressDialog progressDialog;
    DatabaseReference userdb,phonedb;
    ImageButton show;
    TwitterLoginButton loginbutton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Twitter.initialize(this);
        setContentView(R.layout.activity_login);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mauth=FirebaseAuth.getInstance();

        toolbar=(Toolbar)findViewById(R.id.tb);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Login");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().show();
        loginbutton=(TwitterLoginButton)findViewById(R.id.tlogin_button);
        progressDialog=new ProgressDialog(this);

        userdb= FirebaseDatabase.getInstance().getReference().child("User");
        phonedb=FirebaseDatabase.getInstance().getReference().child("Phone");
        email=(EditText)findViewById(R.id.email);
        password=(EditText)findViewById(R.id.editText3);
        login=(Button)findViewById(R.id.login);
        show=(ImageButton)findViewById(R.id.imageButton2);
        show.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if(!password.getText().toString().isEmpty())
                {
                    password.setTransformationMethod(null);
                }
                return false;
            }
        });

        loginbutton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result)
            {
                TwitterSession twitterSession= TwitterCore.getInstance().getSessionManager().getActiveSession();
                TwitterAuthToken authToken=twitterSession.getAuthToken();
                String token=authToken.token;
                String secret=authToken.secret;

                loginusingtwitter(twitterSession);
            }

            @Override
            public void failure(TwitterException exception) {

                Toast.makeText(LoginActivity.this, "Authentication Failed", Toast.LENGTH_SHORT).show();
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                startloggingin();
            }
        });
    }

    private void loginusingtwitter(TwitterSession twitterSession)
    {
        String username=twitterSession.getUserName();
        Intent i=new Intent(LoginActivity.this,Homepage.class);
        i.putExtra("username",username);
        startActivity(i);
    }

    private void startloggingin()
    {
        progressDialog.setMessage("Logging in....");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
        final String user_email=email.getText().toString();
        String user_password=password.getText().toString();

        if(!user_email.isEmpty() && !user_password.isEmpty())
        {
                mauth.signInWithEmailAndPassword(user_email,user_password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful())
                        {
                            userexists(user_email);
                        }
                    }
                });
        }
        else
        {
            progressDialog.dismiss();
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Pass the activity result to the Twitter login button.
        loginbutton.onActivityResult(requestCode, resultCode, data);
    }

    private void userexists(final String user_email)
    {
                final String id=mauth.getCurrentUser().getUid();
                userdb.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        if(dataSnapshot.hasChild(id))
                        {
                            progressDialog.dismiss();
                            Toast.makeText(LoginActivity.this, "Successfully signed in", Toast.LENGTH_SHORT).show();
                            userdb.child(id).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    String img=dataSnapshot.child("image").getValue().toString();
                                    String name=dataSnapshot.child("name").getValue().toString();
                                    String phone=dataSnapshot.child("phone").getValue().toString();
                                    if(img.equals("default"))
                                    {
                                        Intent setupactivity=new Intent(LoginActivity.this,SetupActivity.class);
                                        setupactivity.putExtra("name",name);
                                        setupactivity.putExtra("email",user_email);
                                        setupactivity.putExtra("phone",phone);
                                        setupactivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(setupactivity);
                                    }
                                    else
                                    {
                                        Intent mainactivity=new Intent(LoginActivity.this,MainActivity.class);
                                        mainactivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(mainactivity);
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                        }
                        else
                        {
                            Toast.makeText(LoginActivity.this, "Account does not exist", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    public void register(View view)
    {
        Intent registeractivity=new Intent(LoginActivity.this,RegisterActivity.class);
        startActivity(registeractivity);
    }

    public void forgetpassword(View view)
    {
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        View mview=getLayoutInflater().inflate(R.layout.forgotpassword,null);
        builder.setView(mview);
        final EditText phoneno=(EditText)mview.findViewById(R.id.phone);
        Button submit=(Button)mview.findViewById(R.id.submit);
        builder.show();
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog.setMessage("Sending reset email....");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();
                final String p=phoneno.getText().toString();
                if(p.length()==10 && !p.isEmpty())
                {
                    phonedb.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot)
                        {
                           if(dataSnapshot.hasChild(p))
                           {
                               String mail=dataSnapshot.child(p).child("email").getValue().toString();
                               mauth.sendPasswordResetEmail(mail).addOnCompleteListener(new OnCompleteListener<Void>() {
                                   @Override
                                   public void onComplete(@NonNull Task<Void> task) {

                                       if(task.isSuccessful())
                                       {
                                           Toast.makeText(getApplicationContext(),"Link sent to your mail",Toast.LENGTH_LONG).show();
                                           progressDialog.dismiss();
                                       }
                                   }
                               });
                           }
                           else
                           {
                               Toast.makeText(LoginActivity.this, "This phone number is not registered", Toast.LENGTH_SHORT).show();
                               progressDialog.dismiss();
                           }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }
        });

    }
}
