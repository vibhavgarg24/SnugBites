package com.example.snugbites;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import com.example.snugbites.adapter.PhotosAdapter;
import com.example.snugbites.model.Post;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;

public class SavedPosts extends AppCompatActivity {

    private ImageView backIvSavedPosts;

    private RecyclerView savedPostsRv;
    private PhotosAdapter photosAdapter;
//    private List<Post> savedPostList;
    private List<String> savedPostList;

    private FirebaseUser firebaseUser;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_posts);
        getWindow().setStatusBarColor(getResources().getColor(R.color.main_color));
//        getWindow().setStatusBarColor(Color.BLACK);

        backIvSavedPosts = findViewById(R.id.backIvSavedPosts);
        savedPostsRv = findViewById(R.id.savedPostsRv);

        savedPostList = new ArrayList<>();
        photosAdapter = new PhotosAdapter(getApplicationContext(), savedPostList, "saved");
        savedPostsRv.setHasFixedSize(true);
        savedPostsRv.setLayoutManager(new GridLayoutManager(getApplicationContext(), 3));
        savedPostsRv.setAdapter(photosAdapter);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        setSavedPosts(firebaseUser);

        backIvSavedPosts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

    }

    private void setSavedPosts(FirebaseUser firebaseUser) {
        db.collection("users").document(firebaseUser.getUid()).collection("saves")
                .orderBy("savedTs", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
//                        if (error != null) {
//                            Toast.makeText(getApplicationContext(), "Error! (Loading Saved Posts)", Toast.LENGTH_SHORT).show();
//                            return;
//                        }
                        if (value != null) {
                            savedPostList.clear();
                            for (DocumentSnapshot documentSnapshot : value) {
                                if (documentSnapshot != null && documentSnapshot.exists()) {
//                                    getPost(documentSnapshot.getId());

                                    savedPostList.add(documentSnapshot.getId());
                                    photosAdapter.notifyDataSetChanged();
                                }
                            }
//                            photosAdapter.notifyDataSetChanged();
                        } else {
                            try {
                                Toast.makeText(getApplicationContext(), "Error! (Loading Saved Posts)", Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                Log.d("ErrorLog", "Saved Posts: setSavedPosts(): " + e.getMessage());
                            }
//                            Toast.makeText(getApplicationContext(), "Error! (Loading Saved Posts)", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

//                .get()
//                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//                    @Override
//                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
//                        if (queryDocumentSnapshots != null) {
//                            savedPostList.clear();
//                            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
//                                if (documentSnapshot != null && documentSnapshot.exists()) {
//                                    getPost(documentSnapshot.getId());
//                                }
//                            }
//                            photosAdapter.notifyDataSetChanged();
//                        }
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Toast.makeText(getApplicationContext(), "Error! (Loading Saved Posts)", Toast.LENGTH_SHORT).show();
//                    }
//                });
    }

    private void getPost(String id) {
        db.collection("posts").document(id)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot != null && documentSnapshot.exists()) {
                            Post post = documentSnapshot.toObject(Post.class);
                            if (post != null) {
                                post.setPostId(documentSnapshot.getId());
//                                savedPostList.add(post);
                                photosAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Error! (Loading Saved Post)", Toast.LENGTH_SHORT).show();
                    }
                });

//                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
//                    @Override
//                    public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
//                        if (error != null) {
//                            Toast.makeText(getApplicationContext(), "Error! (Loading Saved Post)", Toast.LENGTH_SHORT).show();
//                            return;
//                        }
//                        if (value != null && value.exists()) {
//                            Post post = value.toObject(Post.class);
//                            savedPostList.add(post);
//                            photosAdapter.notifyDataSetChanged();
//                        } else {
//                            Toast.makeText(getApplicationContext(), "Error! (Loading Saved Post)", Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                });
    }
}