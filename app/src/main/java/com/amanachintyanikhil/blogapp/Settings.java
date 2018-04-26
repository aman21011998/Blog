package com.amanachintyanikhil.blogapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Settings extends AppCompatActivity {

    Toolbar toolbar;
    FirebaseAuth mauth;
    TextView da;
    DatabaseReference profile,user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        toolbar=(Toolbar) findViewById(R.id.settingstb);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Settings");
        getSupportActionBar().show();
        toolbar.setNavigationIcon(R.drawable.navigationicon);

        mauth= FirebaseAuth.getInstance();
        da=(TextView)findViewById(R.id.deactivate);
        profile=FirebaseDatabase.getInstance().getReference().child("Profile");
        user=FirebaseDatabase.getInstance().getReference().child("User");

        da.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               // Toast.makeText(Settings.this, "Clicked", Toast.LENGTH_SHORT).show();
                final AlertDialog.Builder builder=new AlertDialog.Builder(Settings.this);
                builder.setTitle("Confirm Deactivation....");
                builder.setIcon(R.drawable.deactivate);
                builder.setMessage("Are you sure you want to deactivate your account?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        deactivateaccount();
                        dialogInterface.cancel();
                    }
                });
                 builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialogInterface, int i) {
                         dialogInterface.cancel();
                     }
                 });

                builder.show();
            }
        });

    }

    public void deactivateaccount()
    {
      final String id=mauth.getCurrentUser().getUid();

      mauth.getCurrentUser().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
          @Override
          public void onComplete(@NonNull Task<Void> task) {

              if(task.isSuccessful())
              {
                profile.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        dataSnapshot.getRef().removeValue();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                user.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        dataSnapshot.getRef().removeValue();
                        Toast.makeText(getApplicationContext(),"Account deactivated succesfully",Toast.LENGTH_LONG).show();

                        Intent frontactivity=new Intent(Settings.this,FrontActivity.class);
                        frontactivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(frontactivity);
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
