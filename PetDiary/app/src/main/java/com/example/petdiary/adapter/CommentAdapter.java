package com.example.petdiary.adapter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.service.autofill.Dataset;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.petdiary.Chat;
import com.example.petdiary.R;
import com.example.petdiary.activity.MainActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.MyViewHolder> {

    private Context context;
    private ArrayList<Chat> mDataset;
    String stMyEmail = "";
    private TextView textView;
    FirebaseDatabase database;
    private String postID;
    private FirebaseAuth mAuth;
    private String stEmail;
    private String comment_email;


    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView textView;
        public TextView nickChat;
        public ImageView chatProfile;

        public MyViewHolder(View v) {
            super(v);
            textView = v.findViewById(R.id.tvChat);
            nickChat = v.findViewById(R.id.nickChat);
            chatProfile = v.findViewById(R.id.chatProfile);
        }

    }

    @Override
    public int getItemViewType(int position) {
//        return super.getItemViewType(position);
        return 0;
    }

    public CommentAdapter(ArrayList<Chat> myDataset, String stEmail, String postID,Context context) {
        mDataset = myDataset;
        this.stMyEmail = stEmail;
        this.context = context;
        this.postID = postID;
        notifyDataSetChanged();

    }

    @Override
    public CommentAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                          int viewType) {
        // create a new view
        View v =  LayoutInflater.from(parent.getContext())
                .inflate(R.layout.comment_text_view, parent, false);

        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }


    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        Log.e("###CommentAdapter", mDataset.get(position).getDate().toString());
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").whereEqualTo("email",mDataset.get(position).getEmail())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (final QueryDocumentSnapshot document : task.getResult()) {
                                holder.nickChat.setText(document.getData().get("nickName").toString());
                                if(document.getData().get("profileImg").toString().length() > 0){
                                    Glide.with(context).load(document.getData().get("profileImg").toString()).centerCrop().override(500).into(holder.chatProfile);
                                } else {
                                    holder.chatProfile.setImageResource(R.drawable.icon_person);
                                }

                                break;
                            }
                        } else {
                            Log.d("###", "Error getting documents: ", task.getException());
                        }
                    }
                });

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        stEmail = user.getEmail();
        comment_email = mDataset.get(position).getEmail();

        textView = (TextView) holder.itemView.findViewById(R.id.tvChat);
        textView.setOnLongClickListener(new View.OnLongClickListener() {

            //게시물 정보 uid
            @Override
            public boolean onLongClick(final View view) {
                if (stEmail.equals(comment_email)) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());

                CharSequence info[] = new CharSequence[]{"수정", "삭제"};
                builder.setTitle("");
                builder.setItems(info, new DialogInterface.OnClickListener() {
                    @Override

                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                // 내정보

                                Edit(view,position);
                                notifyItemRangeChanged(position, getItemCount());
                                notifyDataSetChanged();

                                break;
                            case 1:
                                // 로그아웃
                                PoPUP(view,position);

                                break;
                        }
                        dialog.dismiss();
                    }
                });

                builder.show();
            }
                    return false;
                }
        });



        holder.textView.setText(mDataset.get(position).getText());
    }
    @Override
    public int getItemCount() {
        return mDataset.size();
    }


    public void Edit(final View view, final int position){

        final AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());

        final EditText input = new EditText(view.getContext());
        input.setText(mDataset.get(position).getText());
        builder.setTitle("댓글을 수정해주세요");
        builder.setView(input);
        builder.setPositiveButton(context.getResources().getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String stText = input.getText().toString();
//                                    Log.d("input", "onClick: 인풋값"+input.getText().toString());
                        database = FirebaseDatabase.getInstance();

                        DatabaseReference myRef = database.getReference("comment/"+postID).child(mDataset.get(position).getDate());
                        Hashtable<String, Object> numbers
                                = new Hashtable<String, Object>();
                        numbers.put("text", stText);
                        myRef.updateChildren(numbers);


                        textView = (TextView) view.findViewById(R.id.tvChat);
                        textView.setText(stText);
                        mDataset.get(position).setText(stText);


                    }
                });
        builder.setNegativeButton(context.getResources().getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();

    }


    public void PoPUP(final View view, final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());


        builder.setTitle("정말 삭제하시겠습니까?")
                .setCancelable(false)
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {

                        database = FirebaseDatabase.getInstance();

                        DatabaseReference myRef = database.getReference("comment/" + postID).child(mDataset.get(position).getDate());
                        myRef.removeValue();

                        mDataset.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, getItemCount());

                    }
                })
                .setNegativeButton("취소", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {

                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }




}
