package com.example.snugbites;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import com.example.snugbites.adapter.ViewPagerAdapter;
import com.example.snugbites.fragments.PlacedOrdersFragment;
import com.example.snugbites.fragments.ReceivedOrdersFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class MyOrdersActivity extends AppCompatActivity {

    private ImageView backIvMyOrders;
    private TabLayout tabLayoutMyOrders;
    private ViewPager viewPagerMyOrders;

    private FirebaseUser firebaseUser;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_orders);
        getWindow().setStatusBarColor(getResources().getColor(R.color.main_color));

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        backIvMyOrders = findViewById(R.id.backIvMyOrders);
        tabLayoutMyOrders = findViewById(R.id.tabLayoutMyOrders);
        viewPagerMyOrders = findViewById(R.id.viewPagerMyOrders);
        setupTabs(firebaseUser.getUid());

        backIvMyOrders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void setupTabs(String uid) {

        final ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());

        viewPagerAdapter.addFragment(new PlacedOrdersFragment(uid));
        viewPagerAdapter.addFragment(new ReceivedOrdersFragment(uid));

        viewPagerMyOrders.setAdapter(viewPagerAdapter);
        tabLayoutMyOrders.setupWithViewPager(viewPagerMyOrders);

        tabLayoutMyOrders.getTabAt(0).setIcon(R.drawable.tab_layout_placed_selector);
        tabLayoutMyOrders.getTabAt(0).setText("Placed");
        tabLayoutMyOrders.getTabAt(1).setIcon(R.drawable.tab_layout_received_selector);
        tabLayoutMyOrders.getTabAt(1).setText("Received");
    }
}