package com.app.workstamper;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity
{
    private EditText
            companyTextField,
            userIDTextField;
    private Button
            loginBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        companyTextField = findViewById(R.id.companyIdBox);
        userIDTextField = findViewById(R.id.userIdBox);
        loginBtn = findViewById(R.id.loginButton);

        userIDTextField.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                loginBtn.setVisibility((count > 0 && companyTextField.getText().length() > 0) ? View.VISIBLE : View.INVISIBLE);
            }

            @Override
            public void afterTextChanged(Editable editable) { }
        });
    }

    public void onClickLogin(View view)
    {
        // TODO: Add database connection and check if user exists in the company

        // TODO: If user valid, continue here:

        Intent i = new Intent(LoginActivity.this, MainActivity.class);
        i.putExtra("CompanyID", companyTextField.getText());
        i.putExtra("UserID", userIDTextField.getText());
        startActivity(i);
    }
}
