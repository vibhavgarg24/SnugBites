package com.example.snugbites;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import com.example.snugbites.adapter.PostAdapter;
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

public class PostDetail extends AppCompatActivity {

    private ImageView backIvPostDetail;

    private RecyclerView postDetailRv;
    private PostAdapter postAdapter;
//    private List<Post> postList;
    private List<String> postList;
    private String postListType;
    private int position;

    private String publisherId;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);
        getWindow().setStatusBarColor(getResources().getColor(R.color.main_color));
//        getWindow().setStatusBarColor(Color.BLACK);

        postListType = getIntent().getStringExtra("postListType");
        position = getIntent().getIntExtra("position", 0);
        publisherId = getIntent().getStringExtra("publisherId");

        backIvPostDetail = findViewById(R.id.backIvPostDetail);
        postDetailRv = findViewById(R.id.postDetailRv);

        postList = new ArrayList<>();
        postAdapter = new PostAdapter(PostDetail.this, postList);
        postDetailRv.setHasFixedSize(true);
        postDetailRv.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        postDetailRv.setAdapter(postAdapter);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (postListType.equals("saved")) {
            setSavedPosts(firebaseUser.getUid());
        } else if (postListType.equals("posted")){
            setPostedPosts(publisherId);
        }

        backIvPostDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void setSavedPosts(String publisherId) {
        db.collection("users").document(publisherId).collection("saves")
                .orderBy("savedTs", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (queryDocumentSnapshots != null) {
                            postList.clear();
                            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                if (documentSnapshot != null && documentSnapshot.exists()) {
//                                    getPost(documentSnapshot.getId());
                                    postList.add(documentSnapshot.getId());
                                    postAdapter.notifyDataSetChanged();
                                    postDetailRv.scrollToPosition(position);
                                }
                            }
                            postAdapter.notifyDataSetChanged();
                            postDetailRv.scrollToPosition(position);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Error! (Loading Saved Posts)", Toast.LENGTH_SHORT).show();
                    }
                });

//                .addSnapshotListener(new EventListener<QuerySnapshot>() {
//                    @Override
//                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
////                        if (error != null) {
////                            Toast.makeText(getApplicationContext(), "Error! (Loading Saved Posts)", Toast.LENGTH_SHORT).show();
////                            return;
////                        }
//                        if (value != null) {
//                            postList.clear();
//                            for (DocumentSnapshot documentSnapshot : value) {
//                                getPost(documentSnapshot.getId());
//                                postAdapter.notifyDataSetChanged();
//                            }
//                        } else {
//                            try {
//                                Toast.makeText(getApplicationContext(), "Error! (Loading Saved Posts)", Toast.LENGTH_SHORT).show();
//                            } catch (Exception e) {
//                                Log.d("ErrorLog", "PostDetail: setSavedPosts(): " + e.getMessage());
//                            }
////                            Toast.makeText(getApplicationContext(), "Error! (Loading Saved Posts)", Toast.LENGTH_SHORT).show();
//                        }
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
//                                postList.add(post);
                                postAdapter.notifyDataSetChanged();
                                postDetailRv.scrollToPosition(position);
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
//                            post.setPostId(value.getId());
//                            postList.add(post);
//                            postAdapter.notifyDataSetChanged();
//                            postDetailRv.scrollToPosition(position);
//                        } else {
//                            Toast.makeText(getApplicationContext(), "Error! (Loading Saved Post)", Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                });
    }

    private void setPostedPosts(String publisherId) {
        db.collection("posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (queryDocumentSnapshots != null) {
                            postList.clear();
                            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                if (documentSnapshot != null && documentSnapshot.exists()) {
                                    Post post = documentSnapshot.toObject(Post.class);
                                    if (post != null) {
                                        if (post.getPublisherId().equals(publisherId)) {
                                            post.setPostId(documentSnapshot.getId());
//                                            postList.add(post);
                                            postList.add(post.getPostId());
                                        }
                                    }
                                }
                            }
                            postAdapter.notifyDataSetChanged();
                            postDetailRv.scrollToPosition(position);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Error! (Loading No. of Posts)", Toast.LENGTH_SHORT).show();
                    }
                });

//                .addSnapshotListener(new EventListener<QuerySnapshot>() {
//                    @Override
//                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
//                        if (error != null) {
//                            Toast.makeText(getApplicationContext(), "Error! (Loading No. of Posts)", Toast.LENGTH_SHORT).show();
//                            return;
//                        }
//                        if (value != null) {
//                            postList.clear();
//                            for (DocumentSnapshot documentSnapshot : value) {
//                                Post post = documentSnapshot.toObject(Post.class);
//                                if (post.getPublisherId().equals(publisherId)) {
//                                    post.setPostId(documentSnapshot.getId());
//                                    postList.add(post);
//                                }
//                            }
//                            postAdapter.notifyDataSetChanged();
//                            postDetailRv.scrollToPosition(position);
//                        } else {
//                            Toast.makeText(getApplicationContext(), "Error! (Loading No. of Posts)", Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                });
    }

}