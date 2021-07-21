package com.example.snugbites.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import com.example.snugbites.MainActivity;
import com.example.snugbites.ProfileDetail;
import com.example.snugbites.R;
import com.example.snugbites.model.Post;
import com.example.snugbites.model.Seller;
import com.example.snugbites.model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.hendraanggrian.appcompat.widget.SocialTextView;
import com.squareup.picasso.Picasso;
import java.util.HashMap;
import java.util.List;
import de.hdodenhof.circleimageview.CircleImageView;
import eightbitlab.com.blurview.BlurView;
import eightbitlab.com.blurview.RenderScriptBlur;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {

    private Context context;
//    private List<Post> postList;
    private List<String> postList;

    private FirebaseUser firebaseUser;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public PostAdapter(Context context, List<String> postList) {
        this.context = context;
        this.postList = postList;
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.post_layout, parent, false);
//        setupBlurView(view);
        return new PostAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {



//        Post post = postList.get(position);
        String postId = postList.get(position);

        getPostData(postId, holder);


//        CollectionReference likedByColRef = db.collection("posts").document(post.getPostId()).collection("likedBy");
        CollectionReference likedByColRef = db.collection("posts").document(postId).collection("likedBy");
        isLiked(likedByColRef, holder.likePostIv);
        setLikesCounter(likedByColRef, holder.likesCounterTv);
        holder.likePostIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.likePostIv.getTag().equals("notLiked")) {
                    likedByColRef.document(firebaseUser.getUid()).set(new HashMap<String, String>());
                } else {
                    likedByColRef.document(firebaseUser.getUid()).delete();
                }
            }
        });

        CollectionReference savesColRef = db.collection("users").document(firebaseUser.getUid()).collection("saves");
//        isSaved(savesColRef, post.getPostId(), holder.savePostIv);
        isSaved(savesColRef, postId, holder.savePostIv);
        holder.savePostIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.savePostIv.getTag().equals("notSaved")) {
                    HashMap<String, Long> hashMap = new HashMap<>();
                    hashMap.put("savedTs", System.currentTimeMillis());
//                    savesColRef.document(post.getPostId()).set(hashMap);
                    savesColRef.document(postId).set(hashMap);
                } else {
//                    savesColRef.document(post.getPostId()).delete();
                    savesColRef.document(postId).delete();
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public CircleImageView dpPostCiv;
        public TextView topUsernamePostTv;
        public ImageView imgPostIv;
        public ImageView likePostIv;
        public ImageView savePostIv;
        public TextView likesCounterTv;
//        public TextView bottomUsernamePostTv;
        public SocialTextView descriptionPostStv;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            dpPostCiv = itemView.findViewById(R.id.dpPostCiv);
            topUsernamePostTv = itemView.findViewById(R.id.topUsernamePostTv);
            imgPostIv = itemView.findViewById(R.id.imgPostIv);
            likePostIv = itemView.findViewById(R.id.likePostIv);
            savePostIv = itemView.findViewById(R.id.savePostIv);
            likesCounterTv = itemView.findViewById(R.id.likesCounterTv);
//            bottomUsernamePostTv = itemView.findViewById(R.id.bottomUsernamePostTv);
            descriptionPostStv = itemView.findViewById(R.id.descriptionPostStv);
        }
    }

    private void isLiked(CollectionReference likedBy, ImageView likePostIv) {
        likedBy.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
//                if (error != null) {
//                    Toast.makeText(context, "Error! (Loading Likes)", Toast.LENGTH_SHORT).show();
//                    return;
//                }

                if (value != null && value.size() != 0) {
                    likedBy.document(firebaseUser.getUid()).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                            if (error != null) {
                                Toast.makeText(context, "Error! (Loading Like)", Toast.LENGTH_SHORT).show();
                            } else if (value != null && value.exists()) {
                                likePostIv.setImageResource(R.drawable.ic_like_filled);
                                likePostIv.setTag("liked");
                            } else {
                                likePostIv.setImageResource(R.drawable.ic_like_outline);
                                likePostIv.setTag("notLiked");
                            }
                        }
                    });

                } else {
                    likePostIv.setImageResource(R.drawable.ic_like_outline);
                    likePostIv.setTag("notLiked");
                }
            }
        });
    }

    private void setLikesCounter(CollectionReference likedBy, TextView likesCounterTv) {
        likedBy.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
//                if (error != null) {
//                    Toast.makeText(context, "Error! (Loading No. of Likes)", Toast.LENGTH_SHORT).show();
//                    return;
//                }

                if (value != null ) {
                    int likesCount = value.size();
                    likesCounterTv.setText(String.valueOf(likesCount));         // + "likes"
                } else {
                    Toast.makeText(context, "Error! (Loading No. of Likes)", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void isSaved(CollectionReference savesColRef,String postId, ImageView savePostIv) {
        savesColRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
//                if (error != null) {
//                    Toast.makeText(context, "Error! (Loading Saved Posts)", Toast.LENGTH_SHORT).show();
//                    return;
//                }

                if (value != null && value.size() != 0) {
                    savesColRef.document(postId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                            if (error != null) {
                                Toast.makeText(context, "Error! (Loading Save)", Toast.LENGTH_SHORT).show();
                            } else if (value != null && value.exists()) {
                                savePostIv.setImageResource(R.drawable.ic_save_filled);
                                savePostIv.setTag("saved");
                            } else {
                                savePostIv.setImageResource(R.drawable.ic_save_outline);
                                savePostIv.setTag("notSaved");
                            }
                        }
                    });

                } else {
                    savePostIv.setImageResource(R.drawable.ic_save_outline);
                    savePostIv.setTag("notSaved");
                }
            }
        });
    }

    private void getPostData(String postId, PostAdapter.ViewHolder holder) {
        db.collection("posts").document(postId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot != null && documentSnapshot.exists()) {
                            String imageUrl = (String) documentSnapshot.get("imageUrl");
                            Picasso.get().load(imageUrl).into(holder.imgPostIv);
                            holder.descriptionPostStv.setText((String) documentSnapshot.get("description"));

                            String publisherId = (String) documentSnapshot.get("publisherId");
                            setPostPublisherData(publisherId, holder);
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

    private void setPostPublisherData(String publisherId, ViewHolder holder) {

        db.collection("users").document(publisherId)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
//                        if (error != null) {
//                            Toast.makeText(context, "Error! (Loading publisher data)", Toast.LENGTH_SHORT).show();
//                            return;
//                        }
                        if (value != null && value.exists()) {
                            String dpUrl = (String) value.get("dpUrl");
                            String username = (String) value.get("username");

                            if (dpUrl.equals("default")) {
                                holder.dpPostCiv.setImageResource(R.drawable.ic_profile_filled);
                            } else {
                                Picasso.get().load(dpUrl).into(holder.dpPostCiv);
                            }
                            holder.topUsernamePostTv.setText(username);
//                            holder.bottomUsernamePostTv.setText(username);
                        } else {
                            try {
                                Toast.makeText(context, "Error! (Loading publisher data)", Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                Log.d("ErrorLog", "Post Adapter: onBindViewHolder(): " + e.getMessage());
                            }
//                            Toast.makeText(context, "Error! (Loading publisher data)", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        holder.topUsernamePostTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (publisherId.equals(firebaseUser.getUid())) {
                    Intent intent = new Intent(context, MainActivity.class);
                    intent.putExtra("selfProfile", true);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    context.startActivity(intent);
                } else {
                    Intent intent = new Intent(context, ProfileDetail.class);
                    intent.putExtra("publisherId", publisherId);
                    context.startActivity(intent);
                }
            }
        });

        holder.dpPostCiv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (publisherId.equals(firebaseUser.getUid())) {
                    Intent intent = new Intent(context, MainActivity.class);
                    intent.putExtra("selfProfile", true);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    context.startActivity(intent);
                } else {
                    Intent intent = new Intent(context, ProfileDetail.class);
                    intent.putExtra("publisherId", publisherId);
                    context.startActivity(intent);
                }
            }
        });
    }

//    private void setupBlurView(View view) {
//        BlurView blurView = view.findViewById(R.id.blurView);
//        ConstraintLayout layoutContainer = view.findViewById(R.id.layoutContainer);
//        final float radius = 5f;
//
//        //set background, if your root layout doesn't have one
////        final Drawable windowBackground = getWindow().getDecorView().getBackground();
//
//        blurView.setupWith(layoutContainer)
//
//                .setBlurAlgorithm(new RenderScriptBlur(context))
//                .setBlurRadius(radius)
//                .setHasFixedTransformationMatrix(true);
//    }
}
