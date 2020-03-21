package com.example.benjamin.googlefirebasetest;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SignInActivity extends AppCompatActivity {

    private EditText mEmailField;
    private EditText mPasswordField;
    private ProgressBar mProgressBar;
    public Button mLoginBtn;
    public TextView mSignUpActivity;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signin_activity);

        mSignUpActivity = (TextView) findViewById(R.id.textViewSignUp);
        mEmailField = (EditText) findViewById(R.id.emailEditText);
        mPasswordField = (EditText) findViewById(R.id.passwordEditText);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mLoginBtn = (Button) findViewById(R.id.signInBtn);

        mSignUpActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UserLogin();
            }
        });
        mAuth = FirebaseAuth.getInstance();
    }

    private void UserLogin(){

            String email = mEmailField.getText().toString().trim();
            String password = mPasswordField.getText().toString().trim();

            if (email.isEmpty()){
                mEmailField.setError("Email is Required");
                mEmailField.requestFocus();
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                mEmailField.setError("PLease enter a Valid Email Adress");
                mEmailField.requestFocus();
                return;
            }
            if (password.isEmpty()){
                mPasswordField.setError("Password is Required");
                mPasswordField.requestFocus();
                return;
            }

            if (password.length()<6){
                mPasswordField.setError("Password length too short");
                mPasswordField.requestFocus();
                return;
            }

            mProgressBar.setVisibility(View.VISIBLE);
            mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    mProgressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        Toast.makeText(getApplicationContext(), "Login Succesful", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getApplicationContext(), GameList.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);

                    }
                    else {
                        Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                    }
                }
            });
        }
    }

