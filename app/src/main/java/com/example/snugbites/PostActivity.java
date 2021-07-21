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
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.Toast;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.hendraanggrian.appcompat.widget.SocialAutoCompleteTextView;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import java.util.HashMap;

public class PostActivity extends AppCompatActivity {

    private ImageView closeIvPostActivity;
    private ImageView checkIvPostActivity;
    private ImageView postImg;
    private SocialAutoCompleteTextView postCaption;

    private Uri imgUri;
    private StorageTask storageTask;
    private StorageReference storageReference;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        getWindow().setStatusBarColor(getResources().getColor(R.color.main_color));
//        getWindow().setStatusBarColor(Color.BLACK);

        closeIvPostActivity = findViewById(R.id.closeIvPostActivity);
        checkIvPostActivity = findViewById(R.id.checkIvPostActivity);
        postImg = findViewById(R.id.postImg);
        postCaption = findViewById(R.id.postCaption);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference().child("posts");

        closeIvPostActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        checkIvPostActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
//                onBackPressed();
            }
        });

        CropImage.activity().setCropShape(CropImageView.CropShape.RECTANGLE).setAspectRatio(1,1)
                .start(PostActivity.this);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

//            if (imgUri != null && result != null) {
                imgUri = result.getUri();
                postImg.setImageURI(imgUri);
//            }
        } else {
            onBackPressed();
        }
    }

    private void uploadImage() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading");
        progressDialog.show();

        if (imgUri != null) {
            StorageReference fileRef = storageReference.child(System.currentTimeMillis() + "." + getFileExtension(imgUri));
            storageTask = fileRef.putFile(imgUri);
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
//                        if (dlUri != null) {
                            String url = dlUri.toString();

                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("description", postCaption.getText().toString());
                            hashMap.put("imageUrl", url);
                            hashMap.put("publisherId", firebaseUser.getUid());
                            hashMap.put("timestamp", System.currentTimeMillis());
                            db.collection("posts").document().set(hashMap);
//                                    .addOnFailureListener(new OnFailureListener() {
//                                        @Override
//                                        public void onFailure(@NonNull Exception e) {
//                                            Toast.makeText(PostActivity.this, "Error! (Uploading Image)", Toast.LENGTH_SHORT).show();
//                                        }
//                                    });
//                        }

                        progressDialog.dismiss();

                        Intent intent = new Intent(PostActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(PostActivity.this, "Error! (Uploading Image)", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(PostActivity.this, "Error! (Uploading Image)", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
        }
    }

    private String getFileExtension(Uri imgUri) {
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(this.getContentResolver().getType(imgUri));
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(PostActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        super.onBackPressed();
    }
}