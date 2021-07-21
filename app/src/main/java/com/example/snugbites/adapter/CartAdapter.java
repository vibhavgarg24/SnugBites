package com.example.snugbites.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.snugbites.R;
import com.example.snugbites.model.MenuItem;
import com.squareup.picasso.Picasso;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder>{

    private Context context;
    private List<MenuItem> cartList;

    public CartAdapter(Context context, List<MenuItem> cartList) {
        this.context = context;
        this.cartList = cartList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cart_item_card, parent, false);
        return new CartAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        MenuItem menuItem = cartList.get(position);

        if (menuItem.getImgUrl().equals("default")) {
            holder.cartCardIv.setPadding(70, 70, 70, 70);
            holder.cartCardIv.setColorFilter(Color.BLACK);
            holder.cartCardIv.setImageResource(R.drawable.ic_knife_spoon);
        } else {
            Picasso.get().load(menuItem.getImgUrl()).into(holder.cartCardIv);
        }

        holder.cartCardName.setText(menuItem.getName());
        holder.cartCardDesc.setText(menuItem.getDesc());
        holder.cartCardPrice.setText("₹ " + menuItem.getPrice());
        holder.cartCardUnit.setText(menuItem.getUnit());
        holder.cartCardQuantity.setText("x" + menuItem.getQuantity());
    }

    @Override
    public int getItemCount() {
        return cartList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView cartCardIv;
        public TextView cartCardName;
        public TextView cartCardDesc;
        public TextView cartCardPrice;
        public TextView cartCardUnit;
        public TextView cartCardQuantity;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            cartCardIv = itemView.findViewById(R.id.cartCardIv);
            cartCardName = itemView.findViewById(R.id.cartCardName);
            cartCardDesc = itemView.findViewById(R.id.cartCardDesc);
            cartCardPrice = itemView.findViewById(R.id.cartCardPrice);
            cartCardUnit = itemView.findViewById(R.id.cartCardUnit);
            cartCardQuantity = itemView.findViewById(R.id.cartCardQuantity);
        }
    }
}
