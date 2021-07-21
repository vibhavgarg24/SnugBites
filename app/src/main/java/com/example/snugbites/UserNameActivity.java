package com.example.snugbites;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;
import java.util.HashMap;
import java.util.Map;
import de.hdodenhof.circleimageview.CircleImageView;

public class UserNameActivity extends AppCompatActivity {

    private MaterialEditText emailMet;
    private MaterialEditText nameMet;
    private MaterialEditText unMet;
    private CircleImageView dpCiv;
    private ExtendedFloatingActionButton setUnEfab;

    private FirebaseUser firebaseUser;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
//        getApplication().setTheme(R.style.Theme_SnugBites);
//        setTheme(R.style.Theme_SnugBites);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_name);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        getWindow().setStatusBarColor(Color.BLACK);

        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        emailMet = findViewById(R.id.emailMet);
        nameMet = findViewById(R.id.nameMet);
        unMet = findViewById(R.id.unMet);
        dpCiv = findViewById(R.id.dpCiv);
        setUnEfab = findViewById(R.id.setUnEfab);

        emailMet.setText(firebaseUser.getEmail());
        nameMet.setText(firebaseUser.getDisplayName());
        Picasso.get().load(firebaseUser.getPhotoUrl()).into(dpCiv);
        setUnEfab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (unMet.getText().toString().equals("")) {
                    unMet.setError("Username cannot be empty :(");
                } else {
                    usernameExists(unMet.getText().toString().trim().toLowerCase());
                }
            }
        });
    }

    private void usernameExists(String username) {
        db.collection("users")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            if (documentSnapshot.exists()) {
                                if (documentSnapshot.get("username").toString().equals(username)) {
                                    unMet.setError("Username already exists :(");
                                    return;
                                }
                            }
                        }
                        setUsername(username);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(UserNameActivity.this, "Error! (Checking Username)", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setUsername(String username) {
//        Map<String, Object> hashMap = new HashMap<>();
//        hashMap.put("username", username);
        db.collection("users").document(firebaseUser.getUid())
                .update("username", username)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Intent intent = new Intent(UserNameActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(UserNameActivity.this, "Error! (Setting Username)", Toast.LENGTH_SHORT).show();
                    }
                });
    }

}