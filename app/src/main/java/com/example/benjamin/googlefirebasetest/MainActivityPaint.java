package com.example.benjamin.googlefirebasetest;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Layout;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.benjamin.googlefirebasetest.R;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

public class MainActivityPaint extends AppCompatActivity {

    private PaintView paintView;
    private DatabaseReference mDataBase;
    private ValueEventListener mConnectedListener;
    private String canvas_name;
    private String temp_key;
    private Toolbar toolbar;
    private int mBoardWidth;
    private int mBoardHeight;
    Button fab;
    EditText input;
    public static int SIGN_IN_REQUEST_CODE = 10;
    private FirebaseListAdapter<ChatMessage> adapter;
    public String wordToFind;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paint);
        mDataBase = FirebaseDatabase.getInstance().getReference();
        canvas_name = getIntent().getExtras().get("canvas_name").toString();
        toolbar = findViewById(R.id.toolbar2);
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        paintView = (PaintView) findViewById(R.id.paintView);
        ViewGroup.LayoutParams params = paintView.getLayoutParams();
        fab = (Button) findViewById(R.id.fab);
        paintView.getViewTreeObserver().addOnGlobalLayoutListener(new MyGlobalListenerClass());
        paintView.addListener(mDataBase.child("Draws").child(canvas_name),metrics,params.height,getIntent().getExtras().get("player").toString());
        toolbar.setTitle(getIntent().getExtras().get("pictionnary_word").toString());
        System.out.println("canvas Name: " + canvas_name);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Draws").child(canvas_name).child("pictionnaryWord");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                wordToFind = (String) dataSnapshot.getValue();
                System.out.println("word to find " + wordToFind);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                input = (EditText) findViewById(R.id.input);

                // Read the input field and push a new instance
                // of ChatMessage to the Firebase database
                FirebaseDatabase.getInstance()
                        .getReference("Messages")
                        .child(canvas_name)
                        .push()
                        .setValue(new ChatMessage(input.getText().toString(),
                                FirebaseAuth.getInstance()
                                        .getCurrentUser()
                                        .getEmail())
                        );

                if (input.getText().toString().toLowerCase().equals(wordToFind.toLowerCase())) {
                    FirebaseDatabase.getInstance()
                            .getReference("Messages")
                            .child(canvas_name)
                            .push()
                            .setValue(new ChatMessage(FirebaseAuth.getInstance().getCurrentUser().getEmail() + "win the game", "IA"));
                }
                // Clear the input
                input.setText("");
            }
        });


        displayChatMessages();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Set up a notification to let us know when we're connected or disconnected from the Firebase servers
        mConnectedListener = FirebaseDatabase.getInstance().getReference("Draws").getRoot().child(".info/connected").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean connected = (Boolean) dataSnapshot.getValue();
                if (connected) {
                    Toast.makeText(MainActivityPaint.this, "Connected to Firebase", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivityPaint.this, "Disconnected from Firebase", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);

        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.normal:
                paintView.normal();
                return true;
            case R.id.emboss:
                paintView.emboss();
                return true;
            case R.id.blur:
                paintView.blur();
                return true;
            case R.id.clear:
                paintView.clear();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    private void displayChatMessages() {
        ListView listOfMessages = findViewById(R.id.list_of_messages);

        adapter = new FirebaseListAdapter<ChatMessage>(this, ChatMessage.class,
                R.layout.messages, FirebaseDatabase.getInstance().getReference("Messages").child(canvas_name)) {
            @Override
            protected void populateView(View v, ChatMessage model, int position) {
                // Get references to the views of message.xml
                TextView messageText = v.findViewById(R.id.message_text);
                TextView messageUser = v.findViewById(R.id.message_user);

                // Set their text
                messageText.setText(model.getMessageText());
                messageUser.setText(model.getMessageUser());

                if (model.getMessageUser().equals("IA")) {
                    FirebaseDatabase.getInstance().getReference("Messages").child(canvas_name).removeValue();
                    FirebaseDatabase.getInstance().getReference("Draws").child(canvas_name).removeValue();
                    Intent intent = new Intent(getApplicationContext(), GameList.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
            }
        };

        listOfMessages.setAdapter(adapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SIGN_IN_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this,
                        "Successfully signed in. Welcome!",
                        Toast.LENGTH_LONG)
                        .show();
                displayChatMessages();
            } else {
                Toast.makeText(this,
                        "We couldn't sign you in. Please try again later.",
                        Toast.LENGTH_LONG)
                        .show();

                // Close the app
                finish();
            }
        }

    }

    //Declare the layout listener
    class MyGlobalListenerClass implements ViewTreeObserver.OnGlobalLayoutListener {
        @Override
        public void onGlobalLayout() {
            PaintView v = (PaintView) findViewById(R.id.paintView);

            mBoardWidth = v.getWidth();
            mBoardHeight = v.getHeight();
            //show ImageView width and height

        }
    }
}
