package com.example.petdiary.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.example.petdiary.Main_Expand_ImageView;
import com.example.petdiary.activity.UserPageActivity;
import com.example.petdiary.info.BlockFriendInfo;
import com.example.petdiary.info.BookmarkInfo;
import com.example.petdiary.Comment;
import com.example.petdiary.Data;
import com.example.petdiary.info.FriendInfo;
import com.example.petdiary.info.PostLikeInfo;
import com.example.petdiary.R;
import com.example.petdiary.activity.ContentEditActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.tbuonomo.viewpagerdotsindicator.WormDotsIndicator;

import java.util.ArrayList;
import java.util.Hashtable;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.CustomViewHolder> {

    private ArrayList<Data> arrayList;
    private Context context;
    private Button Comment_btn;
    private Button onPopupButton;
    FirebaseStorage storage;
    ViewPageAdapter viewPageAdapter;
    ViewPager viewPager;
    WormDotsIndicator wormDotsIndicator;

    private FirebaseDatabase firebaseDatabase;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    String uid = user.getUid();

    private boolean checkFriend = true;

    //어댑터에서 액티비티 액션을 가져올 때 context가 필요한데 어댑터에는 context가 없다.
    //선택한 액티비티에 대한 context를 가져올 때 필요하다.
    public CustomAdapter(ArrayList<Data> arrayList, Context context) {
        this.arrayList = arrayList;
        this.context = context;
    }

    public void clear() {
        arrayList.clear();
    }

    @NonNull
    @Override
    //실제 리스트뷰가 어댑터에 연결된 다음에 뷰 홀더를 최초로 만들어낸다.
    public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, final int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false);
        CustomViewHolder holder = new CustomViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final CustomViewHolder holder, final int position) {


        String empty_Url="https://firebasestorage.googleapis.com/v0/b/petdiary-794c6.appspot.com/o/images%2Fempty.png?alt=media&token=c41b1cc0-d610-4964-b00c-2638d4bfd8bd";
        String first_imageData = arrayList.get(position).getImageUrl1();
        View first_border = (View) holder.itemView.findViewById(R.id.first_Square);
        View second_border = (View) holder.itemView.findViewById(R.id.second_Square);
        View hidden_border = (View) holder.itemView.findViewById(R.id.hidden_Square);
        wormDotsIndicator = (WormDotsIndicator) holder.itemView.findViewById(R.id.worm_dots_indicator);

        viewPager = (ViewPager) holder.itemView.findViewById(R.id.main_image);
        viewPageAdapter = new ViewPageAdapter(arrayList.get(position), arrayList.get(position).getImageUrl1(), arrayList.get(position).getImageUrl2(),
                arrayList.get(position).getImageUrl3(), arrayList.get(position).getImageUrl4(), arrayList.get(position).getImageUrl5(), context);
        viewPager.setAdapter(viewPageAdapter);
        holder.content.setText(arrayList.get(position).getContent());
        holder.nickName.setText(arrayList.get(position).getNickName());
        holder.LikeText.setText(String.valueOf(arrayList.get(position).getFavoriteCount()));

        if (first_imageData.equals(empty_Url)) {
            viewPager.setVisibility(View.GONE);
            wormDotsIndicator.setViewPager(viewPager);
            wormDotsIndicator.setVisibility(View.INVISIBLE);

            first_border.setVisibility(View.GONE);
            second_border.setVisibility(View.GONE);
            hidden_border.setVisibility(View.VISIBLE);

        }
        else {

            viewPager.setVisibility(View.VISIBLE);
            wormDotsIndicator.setViewPager(viewPager);
            wormDotsIndicator.setVisibility(View.VISIBLE);

            first_border.setVisibility(View.VISIBLE);
            second_border.setVisibility(View.VISIBLE);
            hidden_border.setVisibility(View.GONE);

        }

        holder.nickName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, UserPageActivity.class);
                intent.putExtra("userID", arrayList.get(position).getUid().toString());
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });
        holder.profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, UserPageActivity.class);
                intent.putExtra("userID", arrayList.get(position).getUid().toString());
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });
        final String[] profileImg = new String[1];
        DocumentReference documentReference = FirebaseFirestore.getInstance().collection("users").document(arrayList.get(position).getUid());
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null) {
                        if (document.exists()) {
                            profileImg[0] = document.getData().get("profileImg").toString();
                            if(profileImg[0].length() > 0){
                                Glide.with(context).load(profileImg[0]).centerCrop().override(500).into(holder.profileImage);
                            } else {
                                holder.profileImage.setImageResource(R.drawable.icon_person);
                            }
                        } else {
                            //Log.d("###", "No such document");
                        }
                    }
                } else {
                    //Log.d("###", "get failed with ", task.getException());
                }
            }
        });

        if(arrayList.get(position).getBookmark()){
            holder.bookmark_button.setChecked(true);
        } else {
            holder.bookmark_button.setChecked(false);
        }
        if(arrayList.get(position).getLike()){
            holder.Like_button.setChecked(true);
        } else {
            holder.Like_button.setChecked(false);
        }

        Comment_btn = (Button) holder.itemView.findViewById(R.id.Comment_btn);
        Comment_btn.findViewById(R.id.Comment_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                Intent intent = new Intent(context, Comment.class);

                intent.putExtra("postID", arrayList.get(position).getPostID());
                intent.putExtra("nickName", arrayList.get(position).getNickName());
                intent.putExtra("uid", arrayList.get(position).getUid());
                intent.putExtra("content", arrayList.get(position).getContent());

                context.startActivity(intent);
            }
        });

        onPopupButton = (Button) holder.itemView.findViewById(R.id.onPopupButton);
        onPopupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                if (uid.equals(arrayList.get(position).getUid())) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                    CharSequence info[] = new CharSequence[]{"수정", "삭제"};
                    builder.setTitle("");
                    builder.setItems(info, new DialogInterface.OnClickListener() {
                        @Override

                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case 0:
                                    Intent intent = new Intent(context, ContentEditActivity.class);
                                    intent.putExtra("imageUrl1", arrayList.get(position).getImageUrl1());
                                    intent.putExtra("imageUrl2", arrayList.get(position).getImageUrl2());
                                    intent.putExtra("imageUrl3", arrayList.get(position).getImageUrl3());
                                    intent.putExtra("imageUrl4", arrayList.get(position).getImageUrl4());
                                    intent.putExtra("imageUrl5", arrayList.get(position).getImageUrl5());
                                    intent.putExtra("category", arrayList.get(position).getCategory());
                                    intent.putExtra("content", arrayList.get(position).getContent());
                                    intent.putExtra("postID", arrayList.get(position).getPostID());
                                    context.startActivity(intent);
                                    Toast.makeText(view.getContext(), "Edit", Toast.LENGTH_SHORT).show();
                                    break;
                                case 1:
                                    PostDelete(view,position);
                                    break;
                            }
                            dialog.dismiss();
                        }
                    });
                    builder.show();
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                    firebaseDatabase = FirebaseDatabase.getInstance();
                    final CharSequence[][] info = new CharSequence[1][1];
                    if(checkFriend){
                        info[0] = new CharSequence[]{"친구삭제", "사용자 차단"};
                    } else {
                        info[0] = new CharSequence[]{"친구추가", "사용자 차단"};
                    }
                    builder.setTitle("");
                    builder.setItems(info[0], new DialogInterface.OnClickListener() {
                        @Override

                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case 0:
                                    if(checkFriend){
                                        FriendsDelete(view,position);
                                    } else {
                                        DatabaseReference friend = firebaseDatabase.getReference("friend").child(user.getUid() + "/" + arrayList.get(position).getUid());
                                        Hashtable<String, String> numbers = new Hashtable<String, String>();
                                        numbers.put("message","없음");
                                        friend.setValue(numbers);
                                        checkFriend = true;
                                        Toast.makeText(context, "친구를 추가하였습니다.", Toast.LENGTH_SHORT).show();
                                    }
                                    break;
                                case 1:
                                    BlockFriendInfo blockFriendInfo = new BlockFriendInfo();
                                    blockFriendInfo.setFriendUid(arrayList.get(position).getUid());
                                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                                    db.collection("blockFriends/"+user.getUid()+"/friends").document(arrayList.get(position).getUid()).set(blockFriendInfo)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                }
                                            });
                                    Toast.makeText(view.getContext(), "사용자를 차단하였습니다.", Toast.LENGTH_SHORT).show();
                                    break;
                            }
                            dialog.dismiss();
                        }
                    });
                    builder.show();
                }
            }
        });
        wormDotsIndicator = (WormDotsIndicator) holder.itemView.findViewById(R.id.worm_dots_indicator);
        wormDotsIndicator.setViewPager(viewPager);
    }

    @Override
    public int getItemCount() {
        return (arrayList != null ? arrayList.size() : 0);
    }

    public class CustomViewHolder extends RecyclerView.ViewHolder {
        TextView content;
        TextView nickName;
        TextView LikeText;
        ImageView profileImage;
        CheckBox bookmark_button;
        CheckBox Like_button;

        public CustomViewHolder(@NonNull View itemView) {
            super(itemView);
            this.content = itemView.findViewById(R.id.main_textView);
            this.nickName = itemView.findViewById(R.id.Profile_Name);
            this.profileImage = itemView.findViewById(R.id.Profile_image);
            this.bookmark_button = itemView.findViewById(R.id.bookmark_button);
            this.Like_button = itemView.findViewById(R.id.Like_button);
            this.LikeText = itemView.findViewById(R.id.Like_button_text_Count);


            itemView.findViewById(R.id.bookmark_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int pos = getAdapterPosition();
                    final FirebaseFirestore db = FirebaseFirestore.getInstance();
                    BookmarkInfo bookmarkInfo = new BookmarkInfo();
                    if(((CheckBox)view).isChecked()){
                        bookmarkInfo.setPostID(arrayList.get(pos).getPostID());
                        db.collection("user-checked/"+user.getUid()+"/bookmark").document(arrayList.get(pos).getPostID()).set(bookmarkInfo)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                    }
                                });
                    } else {
                        db.collection("user-checked/"+user.getUid()+"/bookmark").document(arrayList.get(pos).getPostID())
                                .delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d("CustomAdapter", "DocumentSnapshot successfully deleted!");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w("CustomAdapter", "Error deleting document", e);
                                    }
                                });
                    }
                }
            });

            itemView.findViewById(R.id.Like_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final int pos = getAdapterPosition();
                    final FirebaseFirestore db = FirebaseFirestore.getInstance();
                    PostLikeInfo postLikeInfo = new PostLikeInfo();
                    if(((CheckBox)view).isChecked()){
                        postLikeInfo.setPostID(arrayList.get(pos).getPostID());
                        db.collection("user-checked/"+user.getUid()+"/like").document(arrayList.get(pos).getPostID()).set(postLikeInfo)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                                        DocumentReference washingtonRef = db.collection("post").document(arrayList.get(pos).getPostID());
                                        int favoritePlus = arrayList.get(pos).getFavoriteCount();
                                        favoritePlus =favoritePlus+1;
                                        arrayList.get(pos).setFavoriteCount(favoritePlus);
                                        LikeText.setText(String.valueOf(favoritePlus));
                                        washingtonRef
                                                .update("favoriteCount", favoritePlus)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Log.d("성공", "DocumentSnapshot successfully updated!");
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Log.w("실패", "Error updating document", e);
                                                    }
                                                });
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                    }
                                });
                    } else {
                        db.collection("user-checked/"+user.getUid()+"/like").document(arrayList.get(pos).getPostID())
                                .delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                                        DocumentReference washingtonRef = db.collection("post").document(arrayList.get(pos).getPostID());
                                        int favoriteMinos = arrayList.get(pos).getFavoriteCount();
                                        favoriteMinos = favoriteMinos - 1;
                                        arrayList.get(pos).setFavoriteCount(favoriteMinos);
                                        LikeText.setText(String.valueOf(favoriteMinos));
                                        washingtonRef
                                                .update("favoriteCount", favoriteMinos)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Log.d("성공", "DocumentSnapshot successfully updated!");
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Log.w("실패", "Error updating document", e);
                                                    }
                                                });
                                        Log.d("CustomAdapter", "DocumentSnapshot successfully deleted!");
                              //      }
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w("CustomAdapter", "Error deleting document", e);
                                    }
                                });
                    }
                }
            });
        }
    }

//친구 삭제
    public void FriendsDelete(final View view, final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
        builder.setTitle("정말 삭제하시겠습니까?")
                .setCancelable(false)
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        DatabaseReference friend = firebaseDatabase.getReference("friend").child(user.getUid() + "/" + arrayList.get(position).getUid());
                        FriendInfo friendInfo = new FriendInfo();
                        friend.setValue(friendInfo);
                        checkFriend = false;
                        arrayList.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, getItemCount());
                        Toast.makeText(context, "친구를 삭제하였습니다.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    //게시글 삭제
    public void PostDelete(final View view, final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
        builder.setTitle("정말 삭제하시겠습니까?")
                .setCancelable(false)
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        FirebaseStorage storage = FirebaseStorage.getInstance();
                        final StorageReference storageRef = storage.getReference();
                        String[] splitText =  arrayList.get(position).getPostID().split("_");
                        String image[] = new String[5];
                        image[0] = arrayList.get(position).getImageUrl1();
                        image[1] = arrayList.get(position).getImageUrl1();
                        image[2] = arrayList.get(position).getImageUrl1();
                        image[3] = arrayList.get(position).getImageUrl1();
                        image[4] = arrayList.get(position).getImageUrl1();
                        for(int i=0; i<5; i++) {
                            if (image[i]!=null) {
                                StorageReference desertRef = storageRef.child("images/" + splitText[0] + "_" + splitText[1] + "_postImg_"+i);
                                // Delete the file
                                desertRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        // File deleted successfully
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {
                                        // Uh-oh, an error occurred!
                                    }
                                });
                            }
                        }

                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        db.collection("post").document(arrayList.get(position).getPostID())
                                .delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {

                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        arrayList.remove(position);
                                        notifyItemRemoved(position);
                                        //this line below gives you the animation and also updates the
                                        //list items after the deleted item
                                        notifyItemRangeChanged(position, getItemCount());
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                    }
                                });

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