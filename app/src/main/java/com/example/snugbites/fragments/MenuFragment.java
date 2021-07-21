package com.example.snugbites.fragments;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.snugbites.EditMenu;
import com.example.snugbites.R;
import com.example.snugbites.adapter.MenuAdapter;
import com.example.snugbites.model.MenuItem;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;

public class MenuFragment extends Fragment {

    private RecyclerView menuRv;
    private MenuAdapter menuAdapter;
    private List<MenuItem> menuItemList;

    public static Snackbar snackbar = null;

    private String publisherId;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public MenuFragment(String publisherId) {
        this.publisherId = publisherId;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_menu, container, false);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        menuRv = view.findViewById(R.id.menuRv);
        menuRv.setHasFixedSize(true);
        menuRv.setLayoutManager(new LinearLayoutManager(getContext()));
        menuItemList = new ArrayList<>();
        menuAdapter = new MenuAdapter(getContext(), menuItemList, publisherId);
        menuRv.setAdapter(menuAdapter);

        loadMenuItems(publisherId);

        return view;
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
//                            menuAdapter.notifyDataSetChanged();
//                        }
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Toast.makeText(getContext(), "Error! (Loading Menu)", Toast.LENGTH_SHORT).show();
//                    }
//                });

                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
//                        if (error != null) {
//                            Toast.makeText(getContext(), "Error! (Loading Menu)", Toast.LENGTH_SHORT).show();
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
                            menuAdapter.notifyDataSetChanged();
                        } else {
                            try {
                                Toast.makeText(getContext(), "Error! (Loading Menu)", Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                Log.d("ErrorLog", "Menu Fragment: loadMenuItems(): " + e.getMessage());
                            }
                        }
                    }
                });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (snackbar != null) {
            if (snackbar.isShown()) {
                snackbar.dismiss();
            }
            snackbar = null;
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        
        if (snackbar != null) {
            if (!snackbar.isShown()) {
                snackbar.show();
            }
        }
    }
}