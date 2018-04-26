package com.amanachintyanikhil.blogapp;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AllFollowers extends AppCompatActivity {

    FirebaseAuth mauth;
    DatabaseReference follow;
    String currentid;
    Toolbar toolbar;
    ListView listView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_followers);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        toolbar=(Toolbar)findViewById(R.id.toolbar);
        listView=(ListView)findViewById(R.id.lv);
        mauth=FirebaseAuth.getInstance();
        follow= FirebaseDatabase.getInstance().getReference().child("Follow");
        currentid=mauth.getCurrentUser().getUid();
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Your Followers");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().show();
        toolbar.setNavigationIcon(R.drawable.navigationicon);
        follow.child(currentid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                long number=dataSnapshot.getChildrenCount();
                Toast.makeText(AllFollowers.this, "Number of followers= "+number, Toast.LENGTH_SHORT).show();
                Iterable<DataSnapshot> names=dataSnapshot.getChildren();

                ArrayList<String> arrayList=new ArrayList<>();
                for(DataSnapshot nm:names)
                {
                    //Toast.makeText(AllFollowers.this,"Name="+nm.getValue().toString(), Toast.LENGTH_SHORT).show();
                    arrayList.add(nm.getValue().toString());
                }

                ArrayAdapter<String> arrayAdapter=new ArrayAdapter<String>(AllFollowers.this,R.layout.followerlistview,R.id.followername,arrayList);
                listView.setAdapter(arrayAdapter);

            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }


}
