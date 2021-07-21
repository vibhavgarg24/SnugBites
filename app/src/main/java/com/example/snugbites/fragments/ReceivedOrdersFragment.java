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
import com.example.snugbites.adapter.OrderAdapter;
import com.example.snugbites.model.Order;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ReceivedOrdersFragment extends Fragment {

    private String userUid;
    private List<Order> orderList;

    private RecyclerView receivedOrdersRv;
    private OrderAdapter orderAdapter;

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    public ReceivedOrdersFragment(String userUid) {
        this.userUid = userUid;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_received_orders, container, false);

        receivedOrdersRv = view.findViewById(R.id.receivedOrdersRv);
        receivedOrdersRv.setHasFixedSize(true);
        receivedOrdersRv.setLayoutManager(new LinearLayoutManager(getContext()));
        orderList = new ArrayList<>();
        orderAdapter = new OrderAdapter(getContext(), orderList, false);
        receivedOrdersRv.setAdapter(orderAdapter);

        loadOrders();

        return view;
    }

    private void loadOrders() {
        db.collection("users").document(userUid).collection("receivedOrders")
                .orderBy("dateTime", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (queryDocumentSnapshots != null) {
                            orderList.clear();
                            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                if (documentSnapshot != null && documentSnapshot.exists()) {
                                    Order order = documentSnapshot.toObject(Order.class);
                                    if (order != null) {
                                        orderList.add(order);
                                    }
                                }
                            }
                            orderAdapter.notifyDataSetChanged();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "Error! (Loading Received Orders)", Toast.LENGTH_SHORT).show();
                    }
                });

    }
}