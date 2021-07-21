package com.example.snugbites;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.snugbites.adapter.ViewPagerAdapter;
import com.example.snugbites.fragments.MenuFragment;
import com.example.snugbites.fragments.PostsFragment;
import com.example.snugbites.model.Post;
import com.example.snugbites.model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.hendraanggrian.appcompat.widget.SocialTextView;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileDetail extends AppCompatActivity {

    private TextView usernameProfileDetail;
    private TextView postCounterProfileDetail;
    private TextView fullNameProfileDetail;
    private CircleImageView dpProfileCivProfileDetail;
    private SocialTextView bioProfileDetail;
    private Button followButton;
    private ImageView backIvProfileDetail;

    private TabLayout tabLayoutProfileDetail;
    private ViewPager viewPagerProfileDetail;

    private String publisherId;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_detail);
        getWindow().setStatusBarColor(getResources().getColor(R.color.main_color));
//        getWindow().setStatusBarColor(Color.BLACK);

        Intent intent = getIntent();
        publisherId = intent.getStringExtra("publisherId");

        usernameProfileDetail = findViewById(R.id.usernameProfileDetail);
        postCounterProfileDetail = findViewById(R.id.postCounterProfileDetail);
        fullNameProfileDetail = findViewById(R.id.fullNameProfileDetail);
        dpProfileCivProfileDetail = findViewById(R.id.dpProfileCivProfileDetail);
        bioProfileDetail = findViewById(R.id.bioProfileDetail);
        followButton = findViewById(R.id.followButton);
        backIvProfileDetail = findViewById(R.id.backIvProfileDetail);

        tabLayoutProfileDetail = findViewById(R.id.tabLayoutProfileDetail);
        viewPagerProfileDetail = findViewById(R.id.viewPagerProfileDetail);
        setUserInfo(publisherId);
        setPostsCount(publisherId);
        setupTabs(publisherId);

        backIvProfileDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

    }

    private void setupTabs(String publisherId) {

        final ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());

        viewPagerAdapter.addFragment(new MenuFragment(publisherId));
        viewPagerAdapter.addFragment(new PostsFragment(publisherId));

        viewPagerProfileDetail.setAdapter(viewPagerAdapter);
        tabLayoutProfileDetail.setupWithViewPager(viewPagerProfileDetail);

        tabLayoutProfileDetail.getTabAt(0).setIcon(R.drawable.tab_layout_food_selector);
        tabLayoutProfileDetail.getTabAt(0).setText("Menu");
        tabLayoutProfileDetail.getTabAt(1).setIcon(R.drawable.tab_layout_grid_selector);
        tabLayoutProfileDetail.getTabAt(1).setText("Posts");
    }

    private void setUserInfo(String publisherId) {
        db.collection("users").document(publisherId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot != null && documentSnapshot.exists()) {
                            User user = documentSnapshot.toObject(User.class);
                            if (user != null) {
                                usernameProfileDetail.setText(user.getUsername());
                                fullNameProfileDetail.setText(user.getFullName());
                                bioProfileDetail.setText(user.getBio());

                                String dpUrl = user.getDpUrl();
                                if (dpUrl.equals("default")) {
                                    dpProfileCivProfileDetail.setImageResource(R.drawable.ic_profile_filled);
                                } else {
                                    Picasso.get().load(dpUrl).into(dpProfileCivProfileDetail);
                                }
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ProfileDetail.this, "Error! (Loading Seller Data)", Toast.LENGTH_SHORT).show();
                    }
                });

//                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
//                    @Override
//                    public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
////                        if (error != null) {
////                            Toast.makeText(ProfileDetail.this, "Error! (Loading Seller Data)", Toast.LENGTH_SHORT).show();
////                            return;
////                        }
//                        if (value != null && value.exists()) {
//                            User user = value.toObject(User.class);
//                            if (user != null) {
//                                usernameProfileDetail.setText(user.getUsername());
//                                fullNameProfileDetail.setText(user.getFullName());
//                                bioProfileDetail.setText(user.getBio());
//
//                                String dpUrl = user.getDpUrl();
//                                if (dpUrl.equals("default")) {
//                                    dpProfileCivProfileDetail.setImageResource(R.drawable.ic_profile_filled);
//                                } else {
//                                    Picasso.get().load(dpUrl).into(dpProfileCivProfileDetail);
//                                }
//                            }
//                        } else {
//                            Toast.makeText(ProfileDetail.this, "Error! (Loading Seller Data)", Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                });
    }

    private void setPostsCount(String publisherId) {
        db.collection("posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (queryDocumentSnapshots != null) {
                            int postCounter = 0;
                            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                if (documentSnapshot != null && documentSnapshot.exists()) {
                                    Post post = documentSnapshot.toObject(Post.class);
                                    if (post != null) {
                                        if (post.getPublisherId().equals(publisherId)) {
                                            post.setPostId(documentSnapshot.getId());
                                            postCounter++;
                                        }
                                    }
                                }
                            }
                            postCounterProfileDetail.setText(String.valueOf(postCounter));
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ProfileDetail.this, "Error! (Loading No. of Posts)", Toast.LENGTH_SHORT).show();
                    }
                });

//                .addSnapshotListener(new EventListener<QuerySnapshot>() {
//                    @Override
//                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
//                        if (error != null) {
//                            Toast.makeText(ProfileDetail.this, "Error! (Loading No. of Posts)", Toast.LENGTH_SHORT).show();
//                            return;
//                        }
//                        if (value != null) {
//                            int postCounter = 0;
//                            for (DocumentSnapshot documentSnapshot : value) {
//                                Post post = documentSnapshot.toObject(Post.class);
//                                if (post.getPublisherId().equals(publisherId)) {
//                                    post.setPostId(documentSnapshot.getId());
//                                    postCounter++;
//                                }
//                            }
//                            postCounterProfileDetail.setText(String.valueOf(postCounter));
//                        } else {
//                            Toast.makeText(ProfileDetail.this, "Error! (Loading No. of Posts)", Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                });
    }
}