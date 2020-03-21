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
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText mEmailField;
    private EditText mPasswordField;
    private ProgressBar mProgressBar;
    Button mRegisterBtn;
    TextView mSignInActivity;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup_activity);

        System.out.println("on Create");

        mEmailField = (EditText) findViewById(R.id.emailEditText);
        mPasswordField = (EditText) findViewById(R.id.passwordEditText);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mRegisterBtn = (Button) findViewById(R.id.signUpBtn);
        mSignInActivity = (TextView) findViewById(R.id.textViewSignIn);

        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerUser();
            }
        });
        mAuth = FirebaseAuth.getInstance();
        mSignInActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() != null) {
                    //Intent User Account
                }
            }
        };
    }

    private void registerUser(){
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

            mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    mProgressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()){
                        finish();
                        startActivity(new Intent(SignUpActivity.this, GameList.class));
                        Toast.makeText(getApplicationContext(),"User Registration Successful", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        if(task.getException()instanceof FirebaseAuthUserCollisionException)
                            Toast.makeText(getApplicationContext(),"User Already Registered", Toast.LENGTH_SHORT).show();
                        else{
                            Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
        }

    @Override
    public void onClick(View view) {
        System.out.println("on Click Sign Up");
        switch (view.getId()){
            case R.id.textViewSignIn:
                finish();
                startActivity(new Intent(this, SignInActivity.class));
                break;
            //case R.id.buttonLogin:

        }
    }
}
