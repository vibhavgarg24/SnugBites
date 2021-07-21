package com.example.snugbites;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.Toast;
import com.example.snugbites.model.User;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceIdReceiver;
import com.google.firebase.installations.FirebaseInstallations;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Objects;

public class SignInActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 11;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;

    private ImageView iconIv;
    private ConstraintLayout signInLayout;
    private ExtendedFloatingActionButton googleSignInButton;

    private ProgressDialog progressDialog;

    FirebaseUser firebaseUser;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
//        getWindow().setStatusBarColor(Color.BLACK);

        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        iconIv = findViewById(R.id.iconIv);
        signInLayout = findViewById(R.id.signInLayout);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please Wait...");

        startupAnimations();

        mAuth = FirebaseAuth.getInstance();
        createRequest();

        googleSignInButton = findViewById(R.id.googleSignInButton);
        googleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.show();
                signIn();
            }
        });
    }

    private void startupAnimations() {
        TranslateAnimation translateAnimation = new TranslateAnimation(0, 0, 1500, 0);
        translateAnimation.setDuration(1000);
        translateAnimation.setFillAfter(false);
        iconIv.setAnimation(translateAnimation);

        AlphaAnimation alphaAnimation = new AlphaAnimation(0f, 1f);
        alphaAnimation.setDuration(2000);
        alphaAnimation.setFillAfter(true);
        signInLayout.startAnimation(alphaAnimation);
    }

    @Override
    protected void onStart() {
        super.onStart();

        firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null) {
            loadUser(firebaseUser, false);
            updateToken();
        }
    }

    private void createRequest() {

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                progressDialog.dismiss();
                Toast.makeText(this, "Google Sign-In Failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            firebaseUser = mAuth.getCurrentUser();
                            loadUser(firebaseUser, true);
                            progressDialog.dismiss();
                        } else {
                            // If sign in fails, display a message to the user.
                            progressDialog.dismiss();
                            Toast.makeText(SignInActivity.this, "Google Authentication Failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void addUserToDb(FirebaseUser firebaseUser) {
        User user = new User();
        user.setUserId(firebaseUser.getUid());
        user.setFullName(firebaseUser.getDisplayName());
        user.setSeller(false);
        user.setUsername("");
        user.setBio("");
        user.setDpUrl(firebaseUser.getPhotoUrl().toString());
        user.setDeviceToken("default");
        db.collection("users").document(firebaseUser.getUid())
                .set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        handleIntent("");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(SignInActivity.this, "Error! (Saving User Data)", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadUser(FirebaseUser firebaseUser, boolean addUser) {
        db.collection("users").document(firebaseUser.getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            User user = documentSnapshot.toObject(User.class);
                            if (user != null) {
                                handleIntent(user.getUsername());
                            }
                        } else {
                            if (addUser) {
                                addUserToDb(firebaseUser);
                            } else {
                                Toast.makeText(SignInActivity.this, "Error! (Recognising User)", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(SignInActivity.this, "Error! (Loading User Data)", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void handleIntent(String username) {
        updateToken();
        Intent intent;
        if (username.equals("")) {
            intent = new Intent(SignInActivity.this, UserNameActivity.class);
        } else {
            intent = new Intent(SignInActivity.this, MainActivity.class);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    public void updateToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (firebaseUser != null && task.getResult() != null) {
                            db.collection("users").document(firebaseUser.getUid())
                                    .update("deviceToken", task.getResult().toString());
                        }
                    }
                });

//        FirebaseInstanceId.getInstance().getInstanceId()
//                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
//                    @Override
//                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
//                        if (task.isSuccessful()) {
//                            String token = Objects.requireNonNull(task.getResult()).getToken();
//                        }
//                    }
//                });
    }

}