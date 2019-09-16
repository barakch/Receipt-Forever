package com.receipt.forever.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.receipt.forever.R;
import com.receipt.forever.utils.Validator;
import com.yarolegovich.lovelydialog.LovelyProgressDialog;
import com.yarolegovich.lovelydialog.LovelyStandardDialog;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    public static String TAG = "LoginActivity";
    private static final int RC_SIGN_IN = 9001;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private EditText edEmail, edPassword;
    private LovelyProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null)
            openMainActivity();

        setContentView(R.layout.activity_login);

        //Init views and UI listeners
        edEmail = findViewById(R.id.ed_signin_email);
        edPassword = findViewById(R.id.ed_signin_password);
        findViewById(R.id.btn_login_email).setOnClickListener(this);
        findViewById(R.id.btn_login_google).setOnClickListener(this);
        findViewById(R.id.tv_forgot_pass).setOnClickListener(this);
        findViewById(R.id.tv_signup).setOnClickListener(this);

        // Configure Google Login
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.btn_login_email:
                String emailAddress = edEmail.getText().toString();
                String password = edPassword.getText().toString();

                if (!Validator.validateEmail(emailAddress)) {
                    Toast.makeText(LoginActivity.this, "Email address is not valid", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!Validator.validatePassword(password)) {
                    Toast.makeText(LoginActivity.this, "Invalid password", Toast.LENGTH_SHORT).show();
                    return;
                }

                loginWithEmail(emailAddress, password);
                break;

            case R.id.btn_login_google:
                loginWithGoogle();
                break;

            case R.id.tv_forgot_pass:
                sendPasswordResetEmail();

                break;
            case R.id.tv_signup:
                openEmailSignupActivity();
                break;
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            try {
                // Google Sign In was successful, authenticate with Firebase
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                e.printStackTrace();
                Log.d(TAG, "Google sign in failed: "+e.getMessage());
                Toast.makeText(getApplicationContext(), "Google sign in failed", Toast.LENGTH_SHORT).show();
            }
        }
    }


    /**
     * Login with Email
     */
    private void loginWithEmail(String email, String password) {
        showProgressDialog();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            hideProgressDialog();
                            createDataBaseReference("user/password");
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            hideProgressDialog();
                            new LovelyStandardDialog(LoginActivity.this)
                                    .setTitle("We have an error")
                                    .setMessage(task.getException().getMessage()).setPositiveButtonText("OK")
                                    .show();
                        }
                    }
                });

    }

    private void sendPasswordResetEmail() {
        final String emailAddress = edEmail.getText().toString();
        if (!Validator.validateEmail(emailAddress)) {
            Toast.makeText(LoginActivity.this, "Email address is not valid", Toast.LENGTH_SHORT).show();
            return;
        }

        new LovelyStandardDialog(LoginActivity.this)
                .setTitle("Reset password")
                .setMessage("Would you like us to send you an email in order to reset your account password?").setPositiveButton("Yes", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.sendPasswordResetEmail(emailAddress)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "Email sent.");
                                    Toast.makeText(LoginActivity.this, "An email was sent to you", Toast.LENGTH_SHORT).show();
                                } else {
                                    new LovelyStandardDialog(LoginActivity.this)
                                            .setTitle("We have an error")
                                            .setMessage(task.getException().getMessage()).setPositiveButtonText("OK")
                                            .show();
                                }
                            }
                        });
            }
        }).setNegativeButtonText("No").show();
    }


    /**
     * Login with Google
     */
    private void loginWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        showProgressDialog();

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            createDataBaseReference("google");
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            new LovelyStandardDialog(LoginActivity.this)
                                    .setTitle("We have an error")
                                    .setMessage(task.getException().getMessage()).setPositiveButtonText("OK")
                                    .show();
                        }

                        hideProgressDialog();
                    }
                });
    }

    /**
     * Helpers
     */
    public void showProgressDialog() {
        if (progressDialog != null) {
            return;
        }

        progressDialog = new LovelyProgressDialog(this)
                .setTitle("Connecting...");
        progressDialog.show();
    }

    public void hideProgressDialog() {
        if (progressDialog != null)
            progressDialog.dismiss();
        progressDialog = null;
    }

    public void createDataBaseReference(String method) {
        createNewUser(method);
        openMainActivity();
    }

    public void openMainActivity() {
        Intent intent = new Intent(getApplicationContext(), MainScreenActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
        LoginActivity.this.finish();
    }

    public void openEmailSignupActivity() {
        Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
        startActivity(intent);
        finish();
    }

    private void createNewUser(String method) {
        FirebaseUser fbUser = mAuth.getCurrentUser();

        if (fbUser == null) {
            Log.e(TAG, "createNewUser() << Error user is null");
            return;
        }
    }
}