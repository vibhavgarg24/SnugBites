package com.example.snugbites.fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.example.snugbites.R;
import com.example.snugbites.adapter.PostAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView postsRecyclerView;
    private PostAdapter postAdapter;
//    private List<Post> postList;
    private List<String> postList;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);
//        setupBlurView(view);

        postsRecyclerView = view.findViewById(R.id.postsRecyclerView);
        postsRecyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
//        linearLayoutManager.setStackFromEnd(true);
//        linearLayoutManager.setReverseLayout(true);
        postsRecyclerView.setLayoutManager(linearLayoutManager);

        postList = new ArrayList<>();
        postAdapter = new PostAdapter(getContext(), postList);
        postsRecyclerView.setAdapter(postAdapter);

        loadPosts();

        return view;
    }

    private void loadPosts() {
        db.collection("posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            if (documentSnapshot != null && documentSnapshot.exists()) {
//                                Post post = documentSnapshot.toObject(Post.class);
//                                if (post != null) {
//                                    post.setPostId(documentSnapshot.getId());
                                    postList.add(documentSnapshot.getId());
//                                }
                            }
                        }
                        postAdapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "Error! (Loading Posts)", Toast.LENGTH_SHORT).show();
                    }
                });
    }

//    private void setupBlurView(View view) {
//        BlurView blurView = view.findViewById(R.id.blurView);
////        FrameLayout frameLayout = view.findViewById(R.id.fragmentContainer);
//        FrameLayout layoutContainer = view.findViewById(R.id.layoutContainer);
//        final float radius = 10f;
//
//        //set background, if your root layout doesn't have one
//        final Drawable windowBackground = getActivity().getWindow().getDecorView().getBackground();
//
//        blurView.setupWith(layoutContainer)
//                .setFrameClearDrawable(windowBackground)
//                .setBlurAlgorithm(new RenderScriptBlur(getContext()))
//                .setBlurRadius(radius)
//                .setHasFixedTransformationMatrix(true);
//    }
}