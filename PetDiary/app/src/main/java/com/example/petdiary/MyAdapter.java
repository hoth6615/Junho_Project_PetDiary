package com.example.petdiary;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
    private ArrayList<Chat> mDataset;
    String stMyEmail = "";
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReference();
    StorageReference pathReference;
    String nick;


    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView textView;
        public ImageView imageView;
        public TextView nickView;
        public ImageView profile;

        public MyViewHolder(View v) {
            super(v);
            textView = v.findViewById(R.id.tvChat);
            imageView = v.findViewById(R.id.ivChat);
            nickView = v.findViewById(R.id.nickChat);
            profile = v.findViewById(R.id.chatProfile);
        }
    }

    @Override
    public int getItemViewType(int position) {
//        return super.getItemViewType(position);
        if (mDataset.get(position).email.equals(stMyEmail)) {
            if(mDataset.get(position).getImage() == null){
                return 1;
            }else if(mDataset.get(position).getText() == null)
                return 2;

        } else {
            if(mDataset.get(position).getImage() == null) {
                return 3;
            }else if(mDataset.get(position).getText() == null)
                return 4;
        }
        return 0;
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public MyAdapter(ArrayList<Chat> myDataset, String stEmail, String nick) {
        mDataset = myDataset;
        this.stMyEmail = stEmail;
        this.nick = nick;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public MyAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                     int viewType) {
        // create a new view
        View v =  LayoutInflater.from(parent.getContext())
                .inflate(R.layout.right_text_view, parent, false);
        if(viewType == 2){
            v =  LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.right_image_view, parent, false);
        }
        else if(viewType == 3){
            v =  LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.my_text_view, parent, false);
        }
        else if(viewType == 4){
            v =  LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.my_image_view, parent, false);
        }

        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }


    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (mDataset.get(position).getImage() == null) {

            if(holder.nickView != null){
                holder.nickView.setText(nick);

                db.collection("users")
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        String s = document.get("profileImg").toString();
                                        String ss = document.get("email").toString();
                                        if(ss.equals(mDataset.get(position).getEmail())) {
                                            if (s.length() > 0) {
                                                ImageView profileImage = (ImageView) holder.profile.findViewById(R.id.chatProfile);
                                                Glide.with(holder.profile.getContext()).load(s).centerCrop().override(500).into(profileImage);
                                                //Glide.with(holder.profile.getContext()).load(s).centerCrop().into(profileImage);
                                            }
                                        }
                                    }
                                } else {

                                }
                            }
                        });


            }
            holder.textView.setText(mDataset.get(position).getText());


        } else if (mDataset.get(position).getText() == null) {
            if(holder.nickView != null){
                holder.nickView.setText(nick);
                db.collection("users")
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        String s = document.get("profileImg").toString();
                                        String ss = document.get("email").toString();
                                        if(ss.equals(mDataset.get(position).getEmail())) {
                                            if (s.length() > 0) {
                                                ImageView profileImage = (ImageView) holder.profile.findViewById(R.id.chatProfile);
                                                Glide.with(holder.profile.getContext()).load(s).centerCrop().override(500).into(profileImage);
                                                //Glide.with(holder.profile.getContext()).load(s).centerCrop().into(profileImage);
                                            }
                                        }
                                    }
                                } else {

                                }
                            }
                        });

            }
            Uri i = Uri.parse(mDataset.get(position).getImage() + "");
            pathReference = storageRef.child("chatImage/" + mDataset.get(position).getImage()+"");
            //holder.imageView.setImageURI(i);

            File localFile = null;
            try {
                localFile = File.createTempFile("images", "jpeg");
            } catch (IOException e) {
                e.printStackTrace();
            }
            final File finalLocalFile = localFile;
            pathReference.getFile(localFile)
                    .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {

                            Glide.with(holder.imageView.getContext()).load(Uri.fromFile(finalLocalFile)).into(holder.imageView);

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {

                }
            });


        }
    }
    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
