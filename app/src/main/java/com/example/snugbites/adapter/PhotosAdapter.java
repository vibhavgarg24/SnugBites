package com.example.snugbites.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.snugbites.PostDetail;
import com.example.snugbites.R;
import com.example.snugbites.model.Post;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;
import java.util.List;

public class PhotosAdapter extends RecyclerView.Adapter<PhotosAdapter.ViewHolder>{

    private Context context;
//    private List<Post> postList;
    private List<String> postList;
    private String postListType;
    private FirebaseFirestore db;
    private Post post;

    public PhotosAdapter(Context context, List<String> postList, String postListType) {
        this.context = context;
        this.postList = postList;
        this.postListType = postListType;
        db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.photo_layout, parent, false);
        return new PhotosAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
//        Post post = postList.get(position);
        String postId = postList.get(position);

//        Picasso.get().load(post.getImageUrl()).fit().centerCrop().into(holder.userPostIv);
        setPostData(postId, holder);

        holder.userPostIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (post != null) {
                    Intent intent = new Intent(context, PostDetail.class);
                    intent.putExtra("postListType", postListType);
                    intent.putExtra("position", position);
                    intent.putExtra("publisherId", post.getPublisherId());
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    context.startActivity(intent);
                } else {
                    Toast.makeText(context, "Error! (Loading Post Data)", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView userPostIv;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            userPostIv = itemView.findViewById(R.id.userPostIv);
        }
    }

    private void setPostData(String id, ViewHolder holder) {
        db.collection("posts").document(id)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot != null && documentSnapshot.exists()) {
                            post = documentSnapshot.toObject(Post.class);
                            if (post != null) {
                                post.setPostId(documentSnapshot.getId());
                                String imageUrl = (String) documentSnapshot.get("imageUrl");
                                Picasso.get().load(imageUrl).fit().centerCrop().into(holder.userPostIv);
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "Error! (Loading Post)", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
