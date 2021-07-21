package com.example.snugbites.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.snugbites.CartActivity;
import com.example.snugbites.R;
import com.example.snugbites.fragments.MenuFragment;
import com.example.snugbites.model.MenuItem;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.ViewHolder> {

    private Context context;
    private List<MenuItem> menuItemList;
    private String publisherId;

    private Map<String, Integer> cartMap;
    private int cartTotalCount = 0;
    private int cartTotalPrice = 0;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseUser firebaseUser;

    public MenuAdapter(Context context, List<MenuItem> menuItemList, String publisherId) {
        this.context = context;
        this.menuItemList = menuItemList;
        this.publisherId = publisherId;
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        cartMap = new HashMap<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.menu_item_card, parent, false);
        return new MenuAdapter.ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        MenuItem menuItem = menuItemList.get(position);

        holder.menuCardName.setText(menuItem.getName());
        holder.menuCardDesc.setText(menuItem.getDesc());
        holder.menuCardPrice.setText("₹ " + menuItem.getPrice());
        holder.menuCardUnit.setText(menuItem.getUnit());

        if (menuItem.getImgUrl().equals("default")) {
            holder.menuCardIv.setPadding(70, 70, 70, 70);
            holder.menuCardIv.setColorFilter(Color.BLACK);
            holder.menuCardIv.setImageResource(R.drawable.ic_knife_spoon);
        } else {
            Picasso.get().load(menuItem.getImgUrl()).into(holder.menuCardIv);
        }

        boolean selfUser = firebaseUser.getUid().equals(publisherId);
        if (!selfUser) {

            holder.addButton.setOnClickListener(new View.OnClickListener() {
                @SuppressLint("SetTextI18n")
                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void onClick(View v) {
                    holder.addButton.setVisibility(View.GONE);
                    holder.numberButtonLL.setVisibility(View.VISIBLE);
                    addItemToCart(menuItemList.get(position));
                    updateSnackBar(v);
                    holder.quantityTv.setText(Integer.toString(cartMap.get(menuItemList.get(position).getId())));
                    MenuFragment.snackbar.show();
                }
            });

            holder.plusButtonIv.setOnClickListener(new View.OnClickListener() {
                @SuppressLint("SetTextI18n")
                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void onClick(View v) {
                    addItemToCart(menuItemList.get(position));
                    updateSnackBar(v);
                    holder.quantityTv.setText(Integer.toString(cartMap.get(menuItemList.get(position).getId())));
                    MenuFragment.snackbar.show();
                }
            });

            holder.minusButtonIv.setOnClickListener(new View.OnClickListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onClick(View v) {
                    subtractItemFromCart(menuItemList.get(position));
                    if (cartMap.isEmpty()) {
                        MenuFragment.snackbar.dismiss();
                        MenuFragment.snackbar = null;
                        holder.numberButtonLL.setVisibility(View.GONE);
                        holder.addButton.setVisibility(View.VISIBLE);
                    } else if (!cartMap.containsKey(menuItemList.get(position).getId())) {
                        holder.numberButtonLL.setVisibility(View.GONE);
                        holder.addButton.setVisibility(View.VISIBLE);
                        updateSnackBar(v);
                        MenuFragment.snackbar.show();
                    } else {
                        updateSnackBar(v);
                        holder.quantityTv.setText(Integer.toString(cartMap.get(menuItemList.get(position).getId())));
                        MenuFragment.snackbar.show();
                    }
                }
            });

        } else {
            holder.addButtonCv.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return menuItemList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView menuCardIv;
        public TextView menuCardName;
        public TextView menuCardDesc;
        public TextView menuCardPrice;
        public TextView menuCardUnit;

        public CardView addButtonCv;
        public TextView addButton;
        public LinearLayout numberButtonLL;
        public ImageView plusButtonIv;
        public TextView quantityTv;
        public ImageView minusButtonIv;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            menuCardIv = itemView.findViewById(R.id.menuCardIv);
//            menuCardIv.setClipToOutline(true);
            menuCardName = itemView.findViewById(R.id.menuCardName);
            menuCardDesc = itemView.findViewById(R.id.menuCardDesc);
            menuCardPrice = itemView.findViewById(R.id.menuCardPrice);
            menuCardUnit = itemView.findViewById(R.id.menuCardUnit);

            addButtonCv = itemView.findViewById(R.id.addButtonCv);
            addButton = itemView.findViewById(R.id.addButton);
            numberButtonLL = itemView.findViewById(R.id.numberButtonLL);
            plusButtonIv = itemView.findViewById(R.id.plusButtonIv);
            quantityTv = itemView.findViewById(R.id.quantityTv);
            minusButtonIv = itemView.findViewById(R.id.minusButtonIv);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void addItemToCart(MenuItem menuItem) {
        cartMap.put(menuItem.getId(), cartMap.getOrDefault(menuItem.getId(), 0) + 1);
        cartTotalCount++;
        cartTotalPrice += menuItem.getPrice();
    }

    private void subtractItemFromCart(MenuItem menuItem) {
        int initQuantity = cartMap.get(menuItem.getId());
        if (initQuantity != 1) {
            cartMap.put(menuItem.getId(), initQuantity - 1);
        } else {
            cartMap.remove(menuItem.getId());
        }
        cartTotalCount--;
        cartTotalPrice -= menuItem.getPrice();
    }

    private void updateSnackBar(View v) {
        if (MenuFragment.snackbar == null) {
            initSnackBar(v);
        }

        String sbText = cartTotalCount + " Item(s)\n" + "₹ " + cartTotalPrice;
        MenuFragment.snackbar.setText(sbText);
    }

    private void initSnackBar(View v) {
        MenuFragment.snackbar = Snackbar.make(v, "", Snackbar.LENGTH_INDEFINITE);
        MenuFragment.snackbar.setBackgroundTint(0xFF1AA260);
        MenuFragment.snackbar.setActionTextColor(Color.WHITE);
        MenuFragment.snackbar.setAnchorView(v.getRootView().findViewById(R.id.bottomNavView));
        MenuFragment.snackbar.setAnchorViewLayoutListenerEnabled(true);
        MenuFragment.snackbar.setAction("View Cart >>", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MenuFragment.snackbar.dismiss();
                Intent intent = new Intent(context, CartActivity.class);
                intent.putExtra("publisherId", publisherId);
                intent.putExtra("cartMap", (Serializable) cartMap);
                intent.putExtra("cartTotalCount", cartTotalCount);
                intent.putExtra("cartTotalPrice", cartTotalPrice);
                context.startActivity(intent);
            }
        });
    }
}
