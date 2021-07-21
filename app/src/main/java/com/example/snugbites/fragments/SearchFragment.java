package com.example.snugbites.fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.example.snugbites.R;
import com.example.snugbites.adapter.SellerAdapter;
import com.example.snugbites.model.Seller;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.hendraanggrian.appcompat.widget.SocialAutoCompleteTextView;
import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    private SocialAutoCompleteTextView searchBar;
    private RecyclerView searchResultsRv;
    private SellerAdapter sellerAdapter;
    private List<Seller> sellerList;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        searchBar = view.findViewById(R.id.searchBar);
        searchResultsRv = view.findViewById(R.id.searchResultsRv);
        searchResultsRv.setHasFixedSize(true);
        searchResultsRv.setLayoutManager(new LinearLayoutManager(getContext()));
        sellerList = new ArrayList<>();
        sellerAdapter = new SellerAdapter(getContext(), sellerList);
        searchResultsRv.setAdapter(sellerAdapter);

        loadSellers();
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchSellers(s.toString().trim().toLowerCase());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        return view;
    }

    private void loadSellers() {
        db.collection("users")
                .orderBy("username")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (queryDocumentSnapshots != null) {
                            sellerList.clear();
                            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                if (documentSnapshot != null && documentSnapshot.exists()) {
                                    Seller seller = documentSnapshot.toObject(Seller.class);
                                    if (seller != null) {
                                        seller.setSellerId(documentSnapshot.getId());
                                        sellerList.add(seller);
                                    }
                                }
                            }
                            sellerAdapter.notifyDataSetChanged();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "Error! (Loading Searched Seller)", Toast.LENGTH_SHORT).show();
                    }
                });

//                .addSnapshotListener(new EventListener<QuerySnapshot>() {
//                    @Override
//                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
//                        if (error != null) {
//                            Toast.makeText(getContext(), "Error! (Loading Searched Seller)", Toast.LENGTH_SHORT).show();
//                            return;
//                        }
//                        if (value != null) {
//                            sellerList.clear();
//                            for (DocumentSnapshot documentSnapshot : value) {
//                                Seller seller = documentSnapshot.toObject(Seller.class);
//                                sellerList.add(seller);
//                                if (seller != null) {
//                                    seller.setSellerId(documentSnapshot.getId());
//                                }
//                            }
//                            sellerAdapter.notifyDataSetChanged();
//                        } else {
//                            Toast.makeText(getContext(), "Error! (Loading Searched Seller)", Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                });
    }

    private void searchSellers(String searchString) {
        Query query = db.collection("users")
                .orderBy("username").startAt(searchString).endAt(searchString + "\uf8ff");
        query.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (queryDocumentSnapshots != null) {
                            sellerList.clear();
                            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                if (documentSnapshot != null && documentSnapshot.exists()) {
                                    Seller seller = documentSnapshot.toObject(Seller.class);
                                    if (seller != null) {
                                        sellerList.add(seller);
                                        seller.setSellerId(documentSnapshot.getId());
                                    }
                                }
                            }
                            sellerAdapter.notifyDataSetChanged();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "Error! (Loading Searched Seller)", Toast.LENGTH_SHORT).show();
                    }
                });

//        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
//            @Override
//            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
//                if (error != null) {
//                    Toast.makeText(getContext(), "Error! (Loading Searched Seller)", Toast.LENGTH_SHORT).show();
//                    return;
//                }
//                if (value != null) {
//                    sellerList.clear();
//                    for (DocumentSnapshot documentSnapshot : value) {
//                        Seller seller = documentSnapshot.toObject(Seller.class);
//                        sellerList.add(seller);
//                        if (seller != null) {
//                            seller.setSellerId(documentSnapshot.getId());
//                        }
//                    }
//                    sellerAdapter.notifyDataSetChanged();
//                } else {
//                    Toast.makeText(getContext(), "Error! (Loading Searched Seller)", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
    }
}