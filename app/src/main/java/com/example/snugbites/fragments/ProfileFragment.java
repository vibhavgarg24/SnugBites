package com.example.snugbites.fragments;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.snugbites.EditMenu;
import com.example.snugbites.EditProfile;
import com.example.snugbites.IOnBackPressed;
import com.example.snugbites.MyOrdersActivity;
import com.example.snugbites.R;
import com.example.snugbites.SavedPosts;
import com.example.snugbites.SignInActivity;
import com.example.snugbites.adapter.ViewPagerAdapter;
import com.example.snugbites.model.Post;
import com.example.snugbites.model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.hendraanggrian.appcompat.widget.SocialTextView;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment implements NavigationView.OnNavigationItemSelectedListener, IOnBackPressed {

    private TextView usernameProfile;
    private TextView postCounterProfile;
    private TextView fullNameProfile;
    private ImageView menuIvProfile;
    private CircleImageView dpProfileCiv;
    private SocialTextView bioProfile;
    private Button editProfileButton;

    private DrawerLayout drawerLayout;
    private NavigationView optionsNavView;
    private View headerView;
    private ImageView backIvOptions;

    private TabLayout tabLayoutProfile;
    private ViewPager viewPagerProfile;

    private FirebaseUser firebaseUser;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        usernameProfile = view.findViewById(R.id.usernameProfile);
        postCounterProfile = view.findViewById(R.id.postCounterProfile);
        fullNameProfile = view.findViewById(R.id.fullNameProfile);
        menuIvProfile = view.findViewById(R.id.menuIvProfile);
        dpProfileCiv = view.findViewById(R.id.dpProfileCiv);
        bioProfile = view.findViewById(R.id.bioProfile);
        editProfileButton = view.findViewById(R.id.editProfileButton);

        drawerLayout = view.findViewById(R.id.drawerLayoutProfile);
        optionsNavView = view.findViewById(R.id.optionsNavView);
        headerView = optionsNavView.getHeaderView(0);
        backIvOptions = headerView.findViewById(R.id.backIvOptions);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        setUserInfo(firebaseUser.getUid());
        setPostsCount(firebaseUser.getUid());

        tabLayoutProfile = view.findViewById(R.id.tabLayoutProfile);
        viewPagerProfile = view.findViewById(R.id.viewPagerProfile);
        setupTabs(firebaseUser.getUid());

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(getActivity(), drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        optionsNavView.setNavigationItemSelectedListener(this);

        menuIvProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.END);
            }
        });

        backIvOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeDrawer();
            }
        });

        editProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), EditProfile.class);
                startActivity(intent);
            }
        });

        return view;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_saved_posts:
                startActivity(new Intent(getContext(), SavedPosts.class));
                break;

            case R.id.nav_my_orders:
                startActivity(new Intent(getContext(), MyOrdersActivity.class));
                break;

            case R.id.nav_edit_menu:
                startActivity(new Intent(getContext(), EditMenu.class));
                break;

            case R.id.nav_edit_profile:
                startActivity(new Intent(getContext(), EditProfile.class));
                break;

            case R.id.nav_logout:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getContext(), SignInActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK));
                Objects.requireNonNull(getActivity()).finish();
                break;
        }

        closeDrawer();
        return true;
    }

    @Override
    public boolean onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawer(GravityCompat.END);
            return true;
        } else {
            return false;
        }
    }

    private void setupTabs(String publisherId) {

        final ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getChildFragmentManager());

        viewPagerAdapter.addFragment(new MenuFragment(publisherId));
        viewPagerAdapter.addFragment(new PostsFragment(publisherId));

        viewPagerProfile.setAdapter(viewPagerAdapter);
        tabLayoutProfile.setupWithViewPager(viewPagerProfile);

        tabLayoutProfile.getTabAt(0).setIcon(R.drawable.tab_layout_food_selector);
        tabLayoutProfile.getTabAt(0).setText("Menu");
        tabLayoutProfile.getTabAt(1).setIcon(R.drawable.tab_layout_grid_selector);
        tabLayoutProfile.getTabAt(1).setText("Posts");
    }

    private void setUserInfo(String publisherId) {
        db.collection("users").document(publisherId)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (value != null && value.exists()) {
                            User user = value.toObject(User.class);
                            if (user != null) {
                                user.setUserId(value.getId());
                                usernameProfile.setText(user.getUsername());
                                fullNameProfile.setText(user.getFullName());
                                bioProfile.setText(user.getBio());

                                String dpUrl = user.getDpUrl();
                                if (dpUrl.equals("default")) {
                                    dpProfileCiv.setImageResource(R.drawable.ic_profile_filled);
                                } else {
                                    Picasso.get().load(user.getDpUrl()).into(dpProfileCiv);
                                }
                            }
                        }
                        else {
//                            Snackbar sb = Snackbar.make(getActivity().findViewById(R.id.content),
//                                    "Error! (Loading User Data)", Snackbar.LENGTH_SHORT);
//                            sb.show();
                            try {
                                Toast.makeText(getContext(), "Error! (Loading User Data)", Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                Log.d("ErrorLog", "Profile Fragment: setUserInfo(): " + e.getMessage());
                            }
                        }
                    }
                });

//                .get()
//                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//                    @Override
//                    public void onSuccess(DocumentSnapshot documentSnapshot) {
//                        if (documentSnapshot != null && documentSnapshot.exists()) {
//                            User user = documentSnapshot.toObject(User.class);
//                            if (user != null) {
//                                user.setUserId(documentSnapshot.getId());
//                                usernameProfile.setText(user.getUsername());
//                                fullNameProfile.setText(user.getFullName());
//                                bioProfile.setText(user.getBio());
//
//                                String dpUrl = user.getDpUrl();
//                                if (dpUrl.equals("default")) {
//                                    dpProfileCiv.setImageResource(R.drawable.ic_profile_filled);
//                                } else {
//                                    Picasso.get().load(user.getDpUrl()).into(dpProfileCiv);
//                                }
//                            }
//                        }
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Toast.makeText(getContext(), "Error! (Loading User Data)", Toast.LENGTH_SHORT).show();
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
                            postCounterProfile.setText(String.valueOf(postCounter));
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
//                            int postCounter = 0;
//                            for (DocumentSnapshot documentSnapshot : value) {
//                                Post post = documentSnapshot.toObject(Post.class);
//                                if (post != null) {
//                                    if (post.getPublisherId().equals(publisherId)) {
//                                        post.setPostId(documentSnapshot.getId());
//                                        postCounter++;
//                                    }
//                                }
//                            }
//                            postCounterProfile.setText(String.valueOf(postCounter));
//                        } else {
//                            Toast.makeText(getContext(), "Error! (Loading No. of Posts)", Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                });
    }

    private void closeDrawer() {
        if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawer(GravityCompat.END);
        }
    }

}