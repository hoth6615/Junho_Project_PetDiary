package com.example.petdiary.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;
import com.example.petdiary.Data;
import com.example.petdiary.Expand_ImageView;
import com.example.petdiary.info.FriendInfo;
import com.example.petdiary.OnSingleClickListener;
import com.example.petdiary.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class ViewPageAdapterSub extends PagerAdapter {

    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    String uid = user.getUid();
    private ArrayList<String> images = new ArrayList<String>();
    private LayoutInflater inflater;
    private Context context;
    private Data arrayList;

    private DatabaseReference mReference;
    private DatabaseReference mDatabase;
    private FirebaseDatabase firebaseDatabase;

    public ViewPageAdapterSub(Data arrayList, String uri1, Context context) {
        if (uri1.length() > 0) {
            images.add(uri1);
        }
        this.context = context;
        this.arrayList = arrayList;
    }

    @Override
    public int getCount() {
        //return images.length;
        return images.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((LinearLayout) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        inflater = (LayoutInflater) context.getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.slider_sub, container, false);
        ImageView imageView = (ImageView) v.findViewById(R.id.imageView);
        Glide.with(context).load(images.get(position)).centerCrop().override(500).into(imageView);

        imageView.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View view) {
                goPost(arrayList);
            }
        });

        container.addView(v);
        return v;
    }

    private void goPost(final Data arrayList) {
        final Intent intent = new Intent(context, Expand_ImageView.class);
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();

        final ArrayList<String> mainSource = new ArrayList<>();

        mainSource.clear();

        mDatabase = FirebaseDatabase.getInstance().getReference("friend/"+uid);
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot postSnapshot: snapshot.getChildren()) {
                    mainSource.add(postSnapshot.getKey());
                }
                db.collection("user-checked/"+uid+"/bookmark")
                        .whereEqualTo("postID", arrayList.getPostID())
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    intent.putExtra("bookmark", "unchecked");
                                    for (final QueryDocumentSnapshot document : task.getResult()) {
                                        if(arrayList.getPostID().equals(document.getData().get("postID").toString())){
                                            intent.putExtra("bookmark", "checked");
                                            break;
                                        }
                                    }
                                    db.collection("user-checked/"+uid+"/like")
                                            .whereEqualTo("postID", arrayList.getPostID())
                                            .get()
                                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                    if (task.isSuccessful()) {
                                                        intent.putExtra("postLike", "unchecked");
                                                        for (final QueryDocumentSnapshot document : task.getResult()) {
                                                            if(arrayList.getPostID().equals(document.getData().get("postID").toString())){
                                                                intent.putExtra("postLike", "checked");
                                                                break;
                                                            }
                                                        }
                                                        boolean chkFriend = false;
                                                        for (int i=0; i<mainSource.size(); i++) {
                                                            if (arrayList.getUid().equals(mainSource.get(i))) {
                                                                chkFriend = true;
                                                                break;
                                                            }
                                                        }
                                                        if (chkFriend) {
                                                            intent.putExtra("friend", "checked");
                                                        } else {
                                                            intent.putExtra("friend", "unchecked");
                                                        }
                                                        intent.putExtra("postID", arrayList.getPostID());
                                                        intent.putExtra("nickName", arrayList.getNickName());
                                                        intent.putExtra("uid", arrayList.getUid());
                                                        intent.putExtra("imageUrl1", arrayList.getImageUrl1());
                                                        intent.putExtra("imageUrl2", arrayList.getImageUrl2());
                                                        intent.putExtra("imageUrl3", arrayList.getImageUrl3());
                                                        intent.putExtra("imageUrl4", arrayList.getImageUrl4());
                                                        intent.putExtra("imageUrl5", arrayList.getImageUrl5());
                                                        intent.putExtra("favoriteCount", arrayList.getFavoriteCount());
                                                        intent.putExtra("date", arrayList.getDate());
                                                        intent.putExtra("content", arrayList.getContent());
                                                        intent.putExtra("postID", arrayList.getPostID());
                                                        intent.putExtra("category", arrayList.getCategory());
                                                        intent.putExtra("favoriteCount", arrayList.getFavoriteCount());
                                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                        context.startActivity(intent);
                                                    } else {
                                                        Log.d("###", "Error getting documents: ", task.getException());
                                                    }
                                                }
                                            });
                                } else {
                                    Log.d("###", "Error getting documents: ", task.getException());
                                }
                            }
                        });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.invalidate();
    }
}


