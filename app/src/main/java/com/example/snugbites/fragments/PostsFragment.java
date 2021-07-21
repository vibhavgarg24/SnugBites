package com.example.snugbites.fragments;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.example.snugbites.R;
import com.example.snugbites.adapter.PhotosAdapter;
import com.example.snugbites.model.Post;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;

public class PostsFragment extends Fragment {

    private RecyclerView userPostsRv;
//    private List<Post> postList;
    private List<String> postList;
    private PhotosAdapter photosAdapter;

    private String publisherId;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public PostsFragment(String publisherId) {
        this.publisherId = publisherId;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_posts, container, false);

        userPostsRv = view.findViewById(R.id.userPostsRv);
//        userPostsRv.setNestedScrollingEnabled(false);
        postList = new ArrayList<>();
        photosAdapter = new PhotosAdapter(getContext(), postList, "posted");
        userPostsRv.setHasFixedSize(true);
        userPostsRv.setLayoutManager(new GridLayoutManager(getContext(), 3));
        userPostsRv.setAdapter(photosAdapter);

        setPostsPhotos(publisherId);

        return view;
    }

    private void setPostsPhotos(String publisherId) {
        db.collection("posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (queryDocumentSnapshots != null) {
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
                            photosAdapter.notifyDataSetChanged();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "Error! (Loading No. of Posts)", Toast.LENGTH_SHORT).show();
                    }
                });

//                .addSnapshotListener(new EventListener<QuerySnapshot>() {
//                    @Override
//                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
//                        if (error != null) {
//                            Toast.makeText(getContext(), "Error! (Loading No. of Posts)", Toast.LENGTH_SHORT).show();
//                            return;
//                        }
//                        if (value != null) {
//                            postList.clear();
//                            for (DocumentSnapshot documentSnapshot : value) {
//                                Post post = documentSnapshot.toObject(Post.class);
//                                if (post != null) {
//                                    if (post.getPublisherId().equals(publisherId)) {
//                                        post.setPostId(documentSnapshot.getId());
//                                        postList.add(post);
//                                    }
//                                }
//                            }
//                            photosAdapter.notifyDataSetChanged();
//                        } else {
//                            Toast.makeText(getContext(), "Error! (Loading No. of Posts)", Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                });
    }
}