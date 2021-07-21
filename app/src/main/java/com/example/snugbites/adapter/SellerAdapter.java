package com.example.snugbites.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.snugbites.MainActivity;
import com.example.snugbites.ProfileDetail;
import com.example.snugbites.R;
import com.example.snugbites.model.Seller;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;
import java.util.List;
import de.hdodenhof.circleimageview.CircleImageView;

public class SellerAdapter extends RecyclerView.Adapter<SellerAdapter.ViewHolder>{

    private Context context;
    private List<Seller> sellerList;
    private FirebaseUser firebaseUser;

    public SellerAdapter(Context context, List<Seller> sellerList) {
        this.context = context;
        this.sellerList = sellerList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.seller_card, parent, false);
        return new SellerAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        Seller seller = sellerList.get(position);

        holder.usernameSellerCard.setText(seller.getUsername());
        holder.fullNameSellerCard.setText(seller.getFullName());

        String dpUrl = seller.getDpUrl();
        if (dpUrl.equals("default")) {
            holder.dpCivSellerCard.setImageResource(R.drawable.ic_profile_filled);
        } else {
            Picasso.get().load(dpUrl).into(holder.dpCivSellerCard);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String publisherId = seller.getSellerId();
                if (publisherId.equals(firebaseUser.getUid())) {
                    MainActivity.bottomNavView.setSelectedItemId(R.id.nav_profile);
                } else {
                    Intent intent = new Intent(context, ProfileDetail.class);
                    intent.putExtra("publisherId", publisherId);
                    context.startActivity(intent);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return sellerList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public CircleImageView dpCivSellerCard;
        public TextView usernameSellerCard;
        public TextView fullNameSellerCard;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            dpCivSellerCard = itemView.findViewById(R.id.dpCivSellerCard);
            usernameSellerCard = itemView.findViewById(R.id.usernameSellerCard);
            fullNameSellerCard = itemView.findViewById(R.id.fullNameSellerCard);
        }
    }
}
