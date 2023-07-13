package com.practice.textrecognitionkit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {
    private SignInButton googleSignIn;
    private GoogleSignInClient mGoogleSignInClient;
    Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //check last google account
        GoogleSignInClientSingleton.account= GoogleSignIn.getLastSignedInAccount(LoginActivity.this);
        if(GoogleSignInClientSingleton.account!=null){
            startActivity(new Intent(this, MainActivity.class));
        }

        //setToolbar
        setToolbar();

        // Initialize Firebase Auth
        GoogleSignInClientSingleton.getFirebaseAuth();

        //Initialize Sign in button
        googleSignIn=findViewById(R.id.btSign_in);
        googleSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
    }

    private void setToolbar() {
        toolbar = findViewById(R.id.toolbar_login);
        setSupportActionBar(toolbar);
    }

    //右上方選單顯示
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.login_menu, menu);
        return true;
    }

    //右上方選單功能
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.about:
                dialogAbout();
                break;
        }
        return true;
    }

    //右上角 Developers 資訊
    private void dialogAbout() {
        new AlertDialog.Builder(this)
                .setTitle("App Developers")
                .setMessage("1. Hsu HanHung ")
                .setPositiveButton("CLOSE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    //The method to be executed when the login button is clicked
    private void signIn() {
        mGoogleSignInClient= GoogleSignInClientSingleton.getGoogleSignInClient(this);
        startActivityForResult(mGoogleSignInClient.getSignInIntent(),200);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 200) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInClientSingleton.account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(GoogleSignInClientSingleton.account);
                Intent intent =new Intent(this, MainActivity.class);
                startActivity(intent);
            } catch (ApiException e) {
                Log.w("TAG", "Google sign in failed", e);
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        GoogleSignInClientSingleton.getFirebaseAuth().signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = GoogleSignInClientSingleton.getFirebaseAuth().getCurrentUser();
//                            updateUI(user);
                        } else {
//                            updateUI(null);
                        }
                    }
                });
    }
}