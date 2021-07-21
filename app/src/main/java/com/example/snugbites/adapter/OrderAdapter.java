package com.example.snugbites.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;
import com.example.snugbites.R;
import com.example.snugbites.model.Order;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.Date;
import java.util.List;
import de.hdodenhof.circleimageview.CircleImageView;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {

    private Context context;
    private List<Order> orderList;
    private boolean placed;

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    public OrderAdapter(Context context, List<Order> orderList, boolean placed) {
        this.context = context;
        this.orderList = orderList;
        this.placed = placed;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.order_card, parent, false);
        return new OrderAdapter.ViewHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Order order = orderList.get(position);

        holder.orderIdTv.setText(order.getOrderId());
        String itemsCount = "ITEMS (" + order.getTotalCount() + ")";
        holder.itemsTextTv.setText(itemsCount);
        holder.orderItemsTv.setText(order.getItems());
        Date date = order.getDateTime().toDate();
        holder.orderTimerStampTv.setText(date.toString());
        holder.orderTotalAmountTv.setText("₹ " + order.getTotalPrice());
        holder.orderStatusTv.setText(order.getStatus());

        if (placed) {
            db.collection("users").document(order.getOrderTo())
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot != null && documentSnapshot.exists()) {
                                String dpUrl = (String) documentSnapshot.get("dpUrl");
                                if (dpUrl.equals("default")) {
                                    holder.userDpCiv.setImageResource(R.drawable.ic_profile_filled);
                                } else {
                                    Picasso.get().load(dpUrl).into(holder.userDpCiv);
                                }

                                holder.usernameOrderTv.setText(documentSnapshot.get("username").toString());
                                holder.fullNameOrderTv.setText(documentSnapshot.get("fullName").toString());
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, "Error! (Loading User Data)", Toast.LENGTH_SHORT).show();

                        }
                    });
        } else {
            db.collection("users").document(order.getOrderFrom())
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot != null && documentSnapshot.exists()) {
                                String dpUrl = (String) documentSnapshot.get("dpUrl");
                                if (dpUrl.equals("default")) {
                                    holder.userDpCiv.setImageResource(R.drawable.ic_profile_filled);
                                } else {
                                    Picasso.get().load(dpUrl).into(holder.userDpCiv);
                                }

                                holder.usernameOrderTv.setText(documentSnapshot.get("username").toString());
                                holder.fullNameOrderTv.setText(documentSnapshot.get("fullName").toString());
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, "Error! (Loading User Data)", Toast.LENGTH_SHORT).show();

                        }
                    });
        }

    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public CircleImageView userDpCiv;
        public TextView usernameOrderTv;
        public TextView fullNameOrderTv;
        public TextView orderIdTv;
        public TextView itemsTextTv;
        public TextView orderItemsTv;
        public TextView orderTimerStampTv;
        public TextView orderStatusTv;
        public TextView orderTotalAmountTv;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            userDpCiv = itemView.findViewById(R.id.userDpCiv);
            usernameOrderTv = itemView.findViewById(R.id.usernameOrderTv);
            fullNameOrderTv = itemView.findViewById(R.id.fullNameOrderTv);
            orderIdTv = itemView.findViewById(R.id.orderIdTv);
            itemsTextTv = itemView.findViewById(R.id.itemsTextTv);
            orderItemsTv = itemView.findViewById(R.id.orderItemsTv);
            orderTimerStampTv = itemView.findViewById(R.id.orderTimerStampTv);
            orderStatusTv = itemView.findViewById(R.id.orderStatusTv);
            orderTotalAmountTv = itemView.findViewById(R.id.orderTotalAmountTv);
        }
    }
}
