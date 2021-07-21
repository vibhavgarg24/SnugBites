package com.example.snugbites;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.snugbites.model.User;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import java.util.HashMap;
import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfile extends AppCompatActivity {

    private ImageView closeIvEditProfile;
    private ImageView checkIvEditProfile;
    private CircleImageView dpEditProfileCiv;
    private TextView removeDpTv;
    private MaterialEditText fullNameMet;
    private MaterialEditText usernameMet;
    private MaterialEditText bioMet;
    private String fullNameText;
    private String usernameText;
    private String bioText;
    private String ogUsername;

    private Uri dpUri;
    private StorageTask storageTask;
    private StorageReference storageReference;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        getWindow().setStatusBarColor(getResources().getColor(R.color.main_color));
//        getWindow().setStatusBarColor(Color.BLACK);

        closeIvEditProfile = findViewById(R.id.closeIvEditProfile);
        checkIvEditProfile = findViewById(R.id.checkIvEditProfile);
        dpEditProfileCiv = findViewById(R.id.dpEditProfileCiv);
        removeDpTv = findViewById(R.id.removeDpTv);
        fullNameMet = findViewById(R.id.fullNameMet);
        usernameMet = findViewById(R.id.usernameMet);
        bioMet = findViewById(R.id.bioMet);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference().child("uploads");
        loadPreviousData(firebaseUser);
        loadImage(firebaseUser);

        closeIvEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        checkIvEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameMet.getText().toString().trim().toLowerCase();
                if (username.equals(ogUsername)) {
                    uploadProfileData(firebaseUser, ogUsername);
                } else if (username.equals("")) {
                    usernameMet.setError("Username cannot be empty :(");
                } else {
                    usernameExists(firebaseUser, username);
                }
            }
        });

        dpEditProfileCiv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fullNameText = fullNameMet.getText().toString();
                usernameText = usernameMet.getText().toString();
                bioText = bioMet.getText().toString();

                CropImage.activity().setCropShape(CropImageView.CropShape.OVAL).setAspectRatio(1, 1)
                        .start(EditProfile.this);
            }
        });

        removeDpTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeDp(firebaseUser);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            dpUri = result.getUri();
            uploadImage();

            fullNameMet.setText(fullNameText);
            usernameMet.setText(usernameText);
            bioMet.setText(bioText);
        } else {
            Toast.makeText(this, "Something went wrong :(", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadImage() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading");
        progressDialog.show();

        if (dpUri != null) {
            StorageReference fileRef = storageReference.child(System.currentTimeMillis() + ".jpeg");
            storageTask = fileRef.putFile(dpUri);
            storageTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return fileRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri dlUri = task.getResult();
                        String url = dlUri.toString();

                        db.collection("users").document(firebaseUser.getUid()).update("dpUrl", url);
                        progressDialog.dismiss();
                    } else {
                        Toast.makeText(EditProfile.this, "Error! (Uploading Image)", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void usernameExists(FirebaseUser firebaseUser, String username) {
        db.collection("users")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            if (documentSnapshot.exists()) {
                                if (documentSnapshot.get("username").toString().equals(username)) {
                                    usernameMet.setError("Username already exists :(");
                                    return;
                                }
                            }
                        }
                        uploadProfileData(firebaseUser, username);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(EditProfile.this, "Error! (Checking Username)", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void uploadProfileData(FirebaseUser firebaseUser, String username) {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("fullName", fullNameMet.getText().toString().trim());
        hashMap.put("username", username);
        hashMap.put("bio", bioMet.getText().toString().trim());

        db.collection("users").document(firebaseUser.getUid()).update(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(EditProfile.this, "Error! (Uploading User Data)", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadPreviousData(FirebaseUser firebaseUser) {
        db.collection("users").document(firebaseUser.getUid()).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot != null && documentSnapshot.exists()) {
                            User user = documentSnapshot.toObject(User.class);
                            if (user != null) {
                                fullNameMet.setText(user.getFullName());
                                ogUsername = user.getUsername();
                                usernameMet.setText(ogUsername);
                                bioMet.setText(user.getBio());
                            }
                        } else {
                            Toast.makeText(EditProfile.this, "Error! (Loading previous values)", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(EditProfile.this, "Error! (Loading previous values)", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadImage(FirebaseUser firebaseUser) {
        db.collection("users").document(firebaseUser.getUid())
//                .get()
//                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//                    @Override
//                    public void onSuccess(DocumentSnapshot documentSnapshot) {
//                        if (documentSnapshot != null && documentSnapshot.exists()) {
//                            String dpUrl = (String) documentSnapshot.get("dpUrl");
//                            if (dpUrl != null) {
//                                if (dpUrl.equals("default")) {
//                                    dpEditProfileCiv.setImageResource(R.drawable.ic_profile_filled);
//                                } else {
//                                    Picasso.get().load(dpUrl).into(dpEditProfileCiv);
//                                }
//                            }
//                        }
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Toast.makeText(EditProfile.this, "Error! (Loading display picture)", Toast.LENGTH_SHORT).show();
//                    }
//                });

                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
//                        if (error != null) {
//                            Toast.makeText(EditProfile.this, "Error! (Loading display picture)", Toast.LENGTH_SHORT).show();
//                            return;
//                        }
                        if (value != null && value.exists()) {
                            String dpUrl = (String) value.get("dpUrl");
                            if (dpUrl != null) {
                                if (dpUrl.equals("default")) {
                                    dpEditProfileCiv.setImageResource(R.drawable.ic_profile_filled);
                                } else {
                                    Picasso.get().load(dpUrl).into(dpEditProfileCiv);
                                }
                            }
                        } else {
                            Toast.makeText(EditProfile.this, "Error! (Loading display picture)", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void removeDp(FirebaseUser firebaseUser) {
        db.collection("users").document(firebaseUser.getUid())
                .update("dpUrl", "default")
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(EditProfile.this, "Display Picture Removed", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(EditProfile.this, "Error! (Removing display picture)", Toast.LENGTH_SHORT).show();
                    }
                });
    }

}