package com.example.benjamin.googlefirebasetest;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Button mFirebaseBtn;
    private EditText nameEditText;
    private EditText emailEditText;
    private DatabaseReference mDataBase;
    private TextView mNameView;
    private ListView mUserList;
    private ArrayList<String> mUsername = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mDataBase = FirebaseDatabase.getInstance().getReference("Draws");

        mFirebaseBtn = (Button) findViewById(R.id.buttonNewGame);
       // mNameView = (TextView) findViewById(R.id.nameView);
        mUserList = (ListView) findViewById(R.id.listView);
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,mUsername);

        mUserList.setAdapter(arrayAdapter);
        mDataBase.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                FingerPath fp = dataSnapshot.getValue(FingerPath.class);
                mUsername.add("");
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mDataBase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
            //String name = dataSnapshot.getValue().toString();

           // mUserList.setText("Name: " + name);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mFirebaseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                // 1 - Create child in root object
                // 2 - Assign value to the child object
                String name = nameEditText.getText().toString().trim();
                String email = emailEditText.getText().toString().trim();

                //create hashmap to add complex  objects

               // HashMap<String, String> dataMap = new HashMap<String, String>();
/*                dataMap.put("Name", name);
                dataMap.put("Email", email);
                mDataBase.push().setValue(dataMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(MainActivity.this, "Stored...", Toast.LENGTH_LONG).show();
                        }
                        else{
                            Toast.makeText(MainActivity.this, "Error...", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });*/

    }
        });
    }
}
