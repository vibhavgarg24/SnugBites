package com.example.snugbites;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.snugbites.adapter.CartAdapter;
import com.example.snugbites.model.MenuItem;
import com.example.snugbites.model.Order;
import com.example.snugbites.notifications.FcmNotificationsSender;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class CartActivity extends AppCompatActivity {

    private TextView totalCartTv;
    private ImageView backIvCart;

    private Map<String, Integer> cartMap;
    private List<MenuItem> cartList;
    private int cartTotalCount;
    private int cartTotalPrice;
    private final StringBuilder cartItemsString = new StringBuilder();;

    private RecyclerView cartRv;
    private CartAdapter cartAdapter;

    private ExtendedFloatingActionButton placeOrderButton;

    private String publisherId;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        getWindow().setStatusBarColor(getResources().getColor(R.color.main_color));
//        getWindow().setStatusBarColor(Color.BLACK);

        totalCartTv = findViewById(R.id.totalCartTv);
        backIvCart = findViewById(R.id.backIvCart);
        backIvCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        Intent intent = getIntent();
        publisherId = intent.getStringExtra("publisherId");
        cartMap = (Map<String, Integer>) intent.getSerializableExtra("cartMap");
        cartTotalCount = intent.getIntExtra("cartTotalCount", 0);
        cartTotalPrice = intent.getIntExtra("cartTotalPrice", 0);

        String totalCartText = cartTotalCount + " Item(s)\n" + "₹ " + cartTotalPrice;
        totalCartTv.setText(totalCartText);

        cartRv = findViewById(R.id.cartRv);
        cartRv.setHasFixedSize(true);
        cartRv.setLayoutManager(new LinearLayoutManager(CartActivity.this));
        cartList = new ArrayList<>();
        cartAdapter = new CartAdapter(CartActivity.this, cartList);
        cartRv.setAdapter(cartAdapter);

        loadCartList(publisherId);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        placeOrderButton = findViewById(R.id.placeOrderButton);
        placeOrderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addOrderPlaced(firebaseUser.getUid(), publisherId);
            }
        });

    }

    private void addOrderPlaced(String uid, String publisherId) {
        DocumentReference orderDoc = db.collection("users").document(uid).collection("placedOrders").document();
        Order order = new Order();
        order.setOrderId(orderDoc.getId());
        order.setOrderTo(publisherId);
        order.setOrderFrom(uid);
        order.setStatus("Placed (Yet to be Confirmed)");
        order.setItems(cartItemsString.toString());
        order.setTotalPrice(cartTotalPrice);
        order.setTotalCount(cartTotalCount);
        orderDoc.set(order)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        addOrderReceived(uid, publisherId, order);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(CartActivity.this, "Error! (Placing Order)", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void addOrderReceived(String uid, String publisherId, Order order) {
        db.collection("users").document(publisherId).collection("receivedOrders")
                .document().set(order)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(CartActivity.this, "Order Placed Successfully!", Toast.LENGTH_SHORT).show();
                        sendNotificationPlaced();
                        sendNotificationReceived();
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(CartActivity.this, "Error! (Placing Order)", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadCartList(String userId) {
        if (cartList == null) {
            cartList = new ArrayList<>();
        }
        cartList.clear();

        for (String menuItemId : cartMap.keySet()) {
            db.collection("users").document(userId).collection("menu").document(menuItemId)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.exists()) {
                                MenuItem menuItem = documentSnapshot.toObject(MenuItem.class);
                                if (menuItem != null) {
                                    menuItem.setId(documentSnapshot.getId());
                                    menuItem.setQuantity(cartMap.get(menuItemId));
                                    cartList.add(menuItem);
                                    cartAdapter.notifyDataSetChanged();

                                    cartItemsString.append(menuItem.getQuantity()).append(" x ").append(menuItem.getName()).append(" . ");
                                }
                            } else {
                                Toast.makeText(CartActivity.this, "Error! (Loading Cart Item)", Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(CartActivity.this, "Error! (Loading Cart Items)", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void sendNotificationPlaced() {
        db.collection("users").document(firebaseUser.getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot != null && documentSnapshot.exists()) {
                            String deviceToken = (String) documentSnapshot.get("deviceToken");
                            if (deviceToken != null && !deviceToken.equals("") && !deviceToken.equals("default")) {
                                FcmNotificationsSender fcmNotificationsSender = new FcmNotificationsSender(deviceToken,
                                        "Order Placed!", "Your order has been placed successfully! Further information is in My Orders section."
                                            , getApplicationContext(), CartActivity.this);

                                fcmNotificationsSender.SendNotifications();
                            }
                        }
                    }
                });
    }

    private void sendNotificationReceived() {
        db.collection("users").document(publisherId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot != null && documentSnapshot.exists()) {
                            String deviceToken = (String) documentSnapshot.get("deviceToken");
                            if (deviceToken != null && !deviceToken.equals("") && !deviceToken.equals("default")) {
                                FcmNotificationsSender fcmNotificationsSender = new FcmNotificationsSender(deviceToken,
                                        "Order Received!", "You have a new order! Further information is in My Orders section."
                                        , getApplicationContext(), CartActivity.this);

                                fcmNotificationsSender.SendNotifications();
                            }
                        }
                    }
                });
    }
}