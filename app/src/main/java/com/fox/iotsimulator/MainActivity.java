package com.fox.iotsimulator;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.amplifyframework.auth.AuthSession;
import com.amplifyframework.auth.cognito.AWSCognitoAuthSession;
import com.amplifyframework.core.Amplify;
import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends AppCompatActivity {

    private EditText txtEmail, txtPassword;
    private Button btnLogin;
    private String errorMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtEmail = findViewById(R.id.editTextEmailAddress);
        txtPassword = findViewById(R.id.editTextPassword);
        btnLogin = findViewById(R.id.buttonLogin);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                errorMessage = "";
                String username = txtEmail.getText().toString();
                String password= txtPassword.getText().toString();

                if (username.trim().equals("") || password.trim().equals("")) {
                    errorMessage = "Email and password cannot be empty.";
                } else {
                    Amplify.Auth.signIn(
                            username,
                            password,
                            result -> Log.i("AuthQuickstart", result.isSignInComplete() ? "Sign in succeeded" : "Sign in not complete"),
                            error -> {Log.e("AuthQuickstart", error.toString()); errorMessage = "Email or password is incorrect.";}
                    );
                }
                if (errorMessage.equals("") == false) {
                    Snackbar warning = Snackbar.make(view, errorMessage, Snackbar.LENGTH_LONG);
                    warning.show();
                } else {
                    Intent intent = new Intent(getApplicationContext(), ListDeviceActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });



//        Amplify.Auth.fetchAuthSession(
//            session -> {
//                AWSCognitoAuthSession cognitoAuthSession = (AWSCognitoAuthSession) session;
//                switch(cognitoAuthSession.getUserPoolTokens().getType()) {
//                    case SUCCESS:
//                        Log.i("AuthQuickStart", "IDToken: " + cognitoAuthSession.getUserPoolTokens().getValue().getIdToken());
//                        break;
//                    case FAILURE:
//                        Log.i("AuthQuickStart123", "IdentityId not present because: " + cognitoAuthSession.getIdentityId().getError().toString());
//                }
//            },
//            error -> Log.e("AuthSession122", "Error +++++++" + error.getMessage())
//        );
//
//        Amplify.Auth.fetchUserAttributes(
//            attributes -> Log.i("AuthDemo", "User attributes = " + attributes.toString()),
//            error -> Log.e("AuthDemo", "Failed to fetch user attributes.", error)
//        );
    }
}