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
import android.widget.Toast;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import java.util.HashMap;

public class AddMenuItem extends AppCompatActivity {

    private ImageView backIvAddMenuItem;
    private ImageView addMenuItemImg;
    private MaterialEditText menuItemNameMet;
    private MaterialEditText menuItemDescMet;
    private MaterialEditText menuItemPriceMet;
    private MaterialEditText menuItemUnitMet;
    private String nameText;
    private String descText;
    private String priceText;
    private String unitText;
    private ExtendedFloatingActionButton saveMenuItemEfab;

    private Uri imgUri;
    private String dlUrl;
    private StorageTask storageTask;
    private StorageReference storageReference;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_menu_item);
        getWindow().setStatusBarColor(getResources().getColor(R.color.main_color));
//        getWindow().setStatusBarColor(Color.BLACK);

        backIvAddMenuItem = findViewById(R.id.backIvAddMenuItem);
        addMenuItemImg = findViewById(R.id.addMenuItemImg);
        menuItemNameMet = findViewById(R.id.menuItemNameMet);
        menuItemDescMet = findViewById(R.id.menuItemDescMet);
        menuItemPriceMet = findViewById(R.id.menuItemPriceMet);
        menuItemUnitMet = findViewById(R.id.menuItemUnitMet);
        saveMenuItemEfab = findViewById(R.id.saveMenuItemEfab);

        initMets();

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference().child("uploads");

        backIvAddMenuItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        addMenuItemImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nameText = menuItemNameMet.getText().toString();
                descText = menuItemDescMet.getText().toString();
                priceText = menuItemPriceMet.getText().toString();
                unitText = menuItemUnitMet.getText().toString();

                CropImage.activity().setCropShape(CropImageView.CropShape.RECTANGLE).setAspectRatio(1, 1)
                        .start(AddMenuItem.this);
            }
        });

        saveMenuItemEfab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (menuItemNameMet.getText().toString().equals("")) {
                    menuItemNameMet.setError("Name cannot be empty :(");
                } else if (menuItemPriceMet.getText().toString().equals("")){
                    menuItemPriceMet.setError("Price cannot be empty :(");
                } else {
//                    MenuItem menuItem = new MenuItem();
//                    menuItem.setName(menuItemNameMet.getText().toString());
//                    menuItem.setDesc(menuItemDescMet.getText().toString());
//                    menuItem.setPrice(Integer.parseInt(menuItemPriceMet.getText().toString()));
//                    menuItem.setUnit(menuItemUnitMet.getText().toString());
                    addMenuItem();
                    finish();
                }
            }
        });
    }

    private void addMenuItem() {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("name", menuItemNameMet.getText().toString());
        hashMap.put("desc", menuItemDescMet.getText().toString());
        hashMap.put("price", Integer.parseInt(menuItemPriceMet.getText().toString()));
        hashMap.put("unit", menuItemUnitMet.getText().toString());
        if (dlUrl == null || dlUrl.equals("")) {
            hashMap.put("imgUrl", "default");
        } else {
            hashMap.put("imgUrl", dlUrl);
        }

        db.collection("users").document(firebaseUser.getUid())
                .collection("menu").document().set(hashMap)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AddMenuItem.this, "Error! (Adding Menu Item)", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            imgUri = result.getUri();
            uploadImage();

            menuItemNameMet.setText(nameText);
            menuItemDescMet.setText(descText);
            menuItemPriceMet.setText(priceText);
            menuItemUnitMet.setText(unitText);
        } else {
            Toast.makeText(this, "Something went wrong :(", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadImage() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading");
        progressDialog.show();

        if (imgUri != null) {
            StorageReference fileRef = storageReference.child(System.currentTimeMillis() + ".jpeg");
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
                        dlUrl = task.getResult().toString();

                        progressDialog.dismiss();

                        if (dlUrl != null && !dlUrl.equals("")) {
                            Picasso.get().load(dlUrl).into(addMenuItemImg);
                        }
                    } else {
                        Toast.makeText(AddMenuItem.this, "Error! (Uploading Image)", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void initMets() {
        menuItemNameMet.setMaxLines(1);
        menuItemNameMet.setMinCharacters(1);
        menuItemNameMet.setMaxCharacters(20);
        menuItemDescMet.setMaxLines(3);
        menuItemPriceMet.setMaxLines(1);
        menuItemPriceMet.setMinCharacters(1);
        menuItemUnitMet.setMaxLines(1);
    }
}