package com.example.benjamin.googlefirebasetest;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class ProfileActivity extends AppCompatActivity {

    ImageView profileImageView;
    EditText editText;
    private static final int CHOOSE_IMAGE = 101;
    Uri uriProfileImage;
    ProgressBar progressBar;
    String profileImageUrl;
    Button saveBtn;
    FirebaseAuth mAuth;
    Button addCanvasBtn;
    ListView listView;
    private DatabaseReference root = FirebaseDatabase.getInstance().getReference("Draws");
    private ArrayList<String> listCanvas;
    private ArrayAdapter<String> arrayAdapter;
    EditText canvas_name;
    private String name;
    TextView canvas_num;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        editText = (EditText) findViewById(R.id.editText);
        mAuth = FirebaseAuth.getInstance();
        addCanvasBtn = (Button) findViewById(R.id.buttonNewGame);
        listView = (ListView) findViewById(R.id.canvasListView);
        canvas_name = (EditText) findViewById(R.id.canvas_name);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        listCanvas = new ArrayList<>();
        root.child("path").removeValue();
        arrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,listCanvas);
        listView.setAdapter(arrayAdapter);
        canvas_num = (TextView) findViewById(R.id.textViewNumGames);
        final Calendar rightNow = Calendar.getInstance();
        canvas_num.setText(String.valueOf(listCanvas.size()));
        addCanvasBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Map<String, Object> map = new HashMap<>();
                String canvasName = "Game created at "+String.valueOf(rightNow.get(Calendar.HOUR_OF_DAY))+":"+String.valueOf(rightNow.get(Calendar.MINUTE));
                map.put(canvasName,"");
                root.updateChildren(map);
                finish();
                Intent intent = new Intent(getApplicationContext(),MainActivityPaint.class);
                intent.putExtra("canvas_name",canvasName);
                intent.putExtra("user_name",name);
                startActivity(intent);
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getApplicationContext(),MainActivityPaint.class);
                intent.putExtra("canvas_name",((TextView)view).getText().toString());
                intent.putExtra("user_name",name);
                startActivity(intent);
            }
        });

        root.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Set<String> set = new HashSet<String>();
                Iterator i = dataSnapshot.getChildren().iterator();
                while (i.hasNext()){
                    set.add(((DataSnapshot)i.next()).getKey());
                }
                listCanvas.clear();
                listCanvas.addAll(set);
                canvas_num.setText(String.valueOf(listCanvas.size()));
                arrayAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onStart(){
        super.onStart();
        if(mAuth.getCurrentUser() == null){
            finish();
            startActivity(new Intent(this,SignInActivity.class));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);


        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

       // switch(item.getItemId()){
            //case R.id.menuLogout:
               // FirebaseAuth.getInstance().signOut();
               // finish();
             //   startActivity(new Intent(this, SignInActivity.class));
              //  break;
        //}
        return true;
    }

    private void loadUserInformation() {

        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            if (user.getPhotoUrl() != null) {
                Glide.with(this).load(user.getPhotoUrl().toString()).into(profileImageView);
            }

            if (user.getDisplayName() != null) {
                editText.setText(user.getDisplayName());
            }
        }
    }

    public void saveUserInformation(){
        String displayName = editText.getText().toString();

        if (displayName.isEmpty()){
            editText.setError("Name Required");
            editText.requestFocus();
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null && profileImageUrl != null){
            UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder().
                    setDisplayName(displayName).
                    setPhotoUri(Uri.parse(profileImageUrl)).
                    build();
            user.updateProfile(profile)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Toast.makeText(ProfileActivity.this,"Profile Updated", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
            }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CHOOSE_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null){

            uriProfileImage = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uriProfileImage);
                profileImageView.setImageBitmap(bitmap);

                uploadImageToFirebaseStorage();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private void uploadImageToFirebaseStorage() {
        StorageReference profileImageReference =
                FirebaseStorage.getInstance().getReference("profilepics/" + System.currentTimeMillis() + ".jpg");
        if (uriProfileImage != null){
            progressBar.setVisibility(View.VISIBLE);
            profileImageReference.putFile(uriProfileImage).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    progressBar.setVisibility(View.GONE);
                    profileImageUrl = taskSnapshot.getDownloadUrl().toString();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_SHORT).show();
                }
            });
        }

    }

    private void showImageChooser(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Profile Image"), CHOOSE_IMAGE);
    }
}
