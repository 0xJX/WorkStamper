package com.app.workstamper;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;



public class LoginActivity extends AppCompatActivity
{
    private static final String TAG = "LoginPassword";

    private EditText
            emailTextField,
            passwordTextField;
    private Button
            loginBtn;

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        mAuth = FirebaseAuth.getInstance();

        emailTextField = findViewById(R.id.emailBox);
        passwordTextField = findViewById(R.id.passwordBox);
        loginBtn = findViewById(R.id.loginButton);



        passwordTextField.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                loginBtn.setVisibility((count > 0 && emailTextField.getText().length() > 0) ? View.VISIBLE : View.INVISIBLE);
            }

            @Override
            public void afterTextChanged(Editable editable) { }
        });

        loginBtn.setOnClickListener(view -> {
            loginUser();
        });
    }

    //protected void onStart();

    private void loginUser(){
        String email = emailTextField.getText().toString();
        String password = passwordTextField.getText().toString();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed",
                                    Toast.LENGTH_LONG).show();
                            updateUI(null);
                        }
                    }
                });
    }

    public void onClickLogin(View view)
    {
        // TODO: Add database connection and check if user exists in the company

        // TODO: If user valid, continue here:

        Intent i = new Intent(LoginActivity.this, MainActivity.class);
        i.putExtra("CompanyID", emailTextField.getText());
        //i.putExtra("UserID", passwordTextField.getText());
        startActivity(i);
    }

    private void reload() { }

    private void updateUI(FirebaseUser user) {

    }
}
