package com.example.snugbites;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.snugbites.adapter.MenuBookAdapter;
import com.example.snugbites.model.MenuItem;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;

public class EditMenu extends AppCompatActivity {

    private ImageView backIvMenuBook;
    private RecyclerView menuBookRv;
    private MenuBookAdapter menuBookAdapter;
    private List<MenuItem> menuItemList;
    private ExtendedFloatingActionButton addMenuItemEfab;

    FirebaseUser firebaseUser;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_menu);
        getWindow().setStatusBarColor(getResources().getColor(R.color.main_color));
//        getWindow().setStatusBarColor(Color.BLACK);

        backIvMenuBook = findViewById(R.id.backIvMenuBook);
        backIvMenuBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        menuBookRv = findViewById(R.id.menuBookRv);
        menuBookRv.setHasFixedSize(true);
        menuBookRv.setLayoutManager(new LinearLayoutManager(EditMenu.this));
        menuItemList = new ArrayList<>();
        menuBookAdapter = new MenuBookAdapter(EditMenu.this, menuItemList);
        menuBookRv.setAdapter(menuBookAdapter);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        loadMenuItems(firebaseUser.getUid());

        addMenuItemEfab = findViewById(R.id.addMenuItemEfab);
        addMenuItemEfab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(EditMenu.this, AddMenuItem.class));
            }
        });

//        BlurView blurView = findViewById(R.id.blurView);
//        float radius = 8f;
//
//        View decorView = getWindow().getDecorView();
//        //ViewGroup you want to start blur from. Choose root as close to BlurView in hierarchy as possible.
//        ViewGroup rootView = (ViewGroup) decorView.findViewById(android.R.id.content);
//        //Set drawable to draw in the beginning of each blurred frame (Optional).
//        //Can be used in case your layout has a lot of transparent space and your content
//        //gets kinda lost after after blur is applied.
//        Drawable windowBackground = decorView.getBackground();
//
//        blurView.setupWith(rootView)
//                .setFrameClearDrawable(windowBackground)
//                .setBlurAlgorithm(new RenderScriptBlur(this))
//                .setBlurRadius(radius)
//                .setBlurAutoUpdate(true);
    }

    private void loadMenuItems(String uid) {
        db.collection("users").document(uid).collection("menu")
                .orderBy("name")
//                .get()
//                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//                    @Override
//                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
//                        if (queryDocumentSnapshots != null) {
//                            menuItemList.clear();
//                            for (DocumentSnapshot documentSnapshot: queryDocumentSnapshots) {
//                                if (documentSnapshot != null && documentSnapshot.exists()) {
//                                    MenuItem menuItem = documentSnapshot.toObject(MenuItem.class);
//                                    if (menuItem != null) {
//                                        menuItem.setId(documentSnapshot.getId());
//                                        menuItemList.add(menuItem);
//                                    }
//                                }
//                            }
//                            menuBookAdapter.notifyDataSetChanged();
//                        }
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Toast.makeText(EditMenu.this, "Error! (Loading Menu)", Toast.LENGTH_SHORT).show();
//                    }
//                });

                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
//                        if (error != null) {
//                            Toast.makeText(EditMenu.this, "Error! (Loading Menu)", Toast.LENGTH_SHORT).show();
//                            return;
//                        }
                        if (value != null) {
                            menuItemList.clear();
                            for (DocumentSnapshot documentSnapshot: value) {
                                MenuItem menuItem = documentSnapshot.toObject(MenuItem.class);
                                if (menuItem != null) {
                                    menuItem.setId(documentSnapshot.getId());
                                    menuItemList.add(menuItem);
                                }
                            }
                            menuBookAdapter.notifyDataSetChanged();
                        } else {
                            try {
                                Toast.makeText(EditMenu.this, "Error! (Loading Menu)", Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                Log.d("ErrorLog", "Edit Menu: loadMenuItems(): " + e.getMessage());
                            }
                        }
                    }
                });
    }

}