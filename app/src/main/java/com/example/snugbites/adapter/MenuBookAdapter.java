package com.example.snugbites.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.snugbites.R;
import com.example.snugbites.model.MenuItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;
import java.util.List;

public class MenuBookAdapter extends RecyclerView.Adapter<MenuBookAdapter.ViewHolder>{

    private Context context;
    private List<MenuItem> menuItemList;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseUser firebaseUser;

    public MenuBookAdapter(Context context, List<MenuItem> menuItemList) {
        this.context = context;
        this.menuItemList = menuItemList;
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.menubook_item_card, parent, false);
        return new MenuBookAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        MenuItem menuItem = menuItemList.get(position);

        holder.menuBookCardName.setText(menuItem.getName());
        holder.menuBookCardDesc.setText(menuItem.getDesc());
        holder.menuBookCardPrice.setText("₹ " + menuItem.getPrice());
        holder.menuBookCardUnit.setText(menuItem.getUnit());

        if (menuItem.getImgUrl().equals("default")) {
            holder.menuBookCardIv.setPadding(90, 90, 90, 90);
            holder.menuBookCardIv.setColorFilter(Color.BLACK);
            holder.menuBookCardIv.setImageResource(R.drawable.ic_knife_spoon);
        } else {
            Picasso.get().load(menuItem.getImgUrl()).into(holder.menuBookCardIv);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(context, "MenuBook Item Clicked: " + position, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return menuItemList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public ImageView menuBookCardIv;
        public TextView menuBookCardName;
        public TextView menuBookCardDesc;
        public TextView menuBookCardPrice;
        public TextView menuBookCardUnit;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            menuBookCardIv = itemView.findViewById(R.id.menuBookCardIv);
            menuBookCardName = itemView.findViewById(R.id.menuBookCardName);
            menuBookCardDesc = itemView.findViewById(R.id.menuBookCardDesc);
            menuBookCardPrice = itemView.findViewById(R.id.menuBookCardPrice);
            menuBookCardUnit = itemView.findViewById(R.id.menuBookCardUnit);

        }
    }
}
