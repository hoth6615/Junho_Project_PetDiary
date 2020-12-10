package com.example.petdiary;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.example.petdiary.activity.ContentEditActivity;
import com.example.petdiary.adapter.ViewPageAdapterDetail;
import com.example.petdiary.info.BookmarkInfo;
import com.example.petdiary.info.FriendInfo;
import com.example.petdiary.info.PostLikeInfo;
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
import com.tbuonomo.viewpagerdotsindicator.WormDotsIndicator;

import java.util.ArrayList;
import java.util.Hashtable;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class Expand_ImageView2 extends AppCompatActivity {

    private String friendChecked;
    private String bookmarkChecked;
    private String likeChecked;
    private String postID;
    private String uid;
    private String imageUrl1;
    private String imageUrl2;
    private String imageUrl3;
    private String imageUrl4;
    private String imageUrl5;
    private String imageUrl11;
    private String imageUrl22;
    private String imageUrl33;
    private String imageUrl44;
    private String imageUrl55;
    private String content;
    private ArrayList<String> hashTag = new ArrayList<String>();
    private String date;
    private String nickName;
    private String Category;
    private int favoriteCount;

    ViewPageAdapterDetail viewPageAdapter;
    ViewPager viewPager;
    WormDotsIndicator wormDotsIndicator;

    TextView post_nickName;
    TextView post_content;
    private Button Comment_btn;
    //파이어베이스에서 내 uid 가져오기
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    String myuid_server = user.getUid();


    private CheckBox bookmark_button;
    private CheckBox Like_button;
    private FirebaseDatabase firebaseDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expand__image_view);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        friendChecked = intent.getStringExtra("friend");
        likeChecked = intent.getStringExtra("postLike");
        bookmarkChecked = intent.getStringExtra("bookmark");
        postID = intent.getStringExtra("postID");
        uid = intent.getStringExtra("uid");
        nickName = intent.getStringExtra("nickName");
        date = intent.getStringExtra("date");
        content = intent.getStringExtra("content");
        imageUrl1 = intent.getStringExtra("imageUrl1");
        imageUrl2 = intent.getStringExtra("imageUrl2");
        imageUrl3 = intent.getStringExtra("imageUrl3");
        imageUrl4 = intent.getStringExtra("imageUrl4");
        imageUrl5 = intent.getStringExtra("imageUrl5");
        favoriteCount = intent.getIntExtra("favoriteCount", 0);
        Category = intent.getStringExtra("category");
        final FirebaseFirestore db = FirebaseFirestore.getInstance();

        Comment_btn = (Button)findViewById(R.id.Comment_btn);

        Comment_btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(getApplicationContext(), Comment.class);

                intent.putExtra("postID", postID);
                intent.putExtra("nickName", nickName);
                intent.putExtra("uid", uid);
                intent.putExtra("content", content);

                getApplicationContext().startActivity(intent.addFlags(FLAG_ACTIVITY_NEW_TASK));

            }
        });


        final String[] profileImg = new String[1];
        DocumentReference documentReference = FirebaseFirestore.getInstance().collection("users").document(uid);
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null) {
                        if (document.exists()) {
                            profileImg[0] = document.getData().get("profileImg").toString();
                            if(profileImg[0].length() > 0){
                                document.getLong("favoriteCount");
                                ImageView profileImage = (ImageView) findViewById(R.id.Profile_image);
                                Glide.with(getApplicationContext()).load(profileImg[0]).centerCrop().override(500).into(profileImage);
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

        viewPager = (ViewPager) findViewById(R.id.main_image);
        viewPageAdapter = new ViewPageAdapterDetail(true, imageUrl1, imageUrl2, imageUrl3, imageUrl4, imageUrl5, getApplicationContext());
        viewPager.setAdapter(viewPageAdapter);

        wormDotsIndicator  = (WormDotsIndicator) findViewById(R.id.worm_dots_indicator);
        wormDotsIndicator .setViewPager(viewPager);

        post_nickName = findViewById(R.id.Profile_Name);
        post_content = findViewById(R.id.main_textView);

        post_nickName.setText(nickName);
        post_content.setText(content);
        if(content.length() == 0){
            post_content.setVisibility(View.INVISIBLE);
        }

        final String[] nn = new String[1];
        db.collection("users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("asdf", document.getId() + " => " + document.getData());

                                if(document.get("email").toString().equals(user.getEmail())){
                                    nn[0] = document.get("nickName").toString();
                                }

                            }
                        } else {

                        }
                    }
                });

        findViewById(R.id.onPopupButton).setOnClickListener(new View.OnClickListener(){

            //내 uid
            String myuid = myuid_server;
            //게시글 uid
            String content_uid = uid;
            @Override

            public void onClick(final View view) {
                Log.d("@@@@", "onClick: 포스트아이디가 뭐야?"+postID);

                if (myuid.equals(content_uid)) {
                    CharSequence info[] = new CharSequence[]{"Edit", "Delete", "Share"};

                    final AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());

                    builder.setTitle("");

                    builder.setItems(info, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case 0:

                                    Intent intent = new Intent(getApplicationContext(), ContentEditActivity.class);

                                    intent.putExtra("imageUrl1", imageUrl1);
                                    intent.putExtra("imageUrl2", imageUrl2);
                                    intent.putExtra("imageUrl3", imageUrl3);
                                    intent.putExtra("imageUrl4", imageUrl4);
                                    intent.putExtra("imageUrl5", imageUrl5);
                                    intent.putExtra("postID", postID);
                                    intent.putExtra("content", content);
                                    intent.putExtra("category", Category);

                                    //getApplicationContext().startActivity(intent.addFlags(FLAG_ACTIVITY_NEW_TASK));
                                    startActivityForResult(intent,0);

                                    // 내정보
                                    Toast.makeText(view.getContext(), "Edit", Toast.LENGTH_SHORT).show();
                                    break;
                                case 1:
                                    // 로그아웃
                                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                                    db.collection("post").document(postID)
                                            .delete()
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {

                                                @Override
                                                public void onSuccess(Void aVoid) {

                                                    onBackPressed();
                                                    Log.d("@@@", "오류가나는이유가뭐야?!");

                                                }
//                                                    finish();
//                                                    overridePendingTransition(0, 0);
//                                                    startActivity(getIntent());
//                                                    overridePendingTransition(0, 0);

//                                                arrayList.remove(position);
//                                                notifyItemRemoved(position);
//                                                //this line below gives you the animation and also updates the
//                                                //list items after the deleted item
//                                                notifyItemRangeChanged(position, getItemCount());
                                               // }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.w("@@@", "Error deleting document", e);
                                                }
                                            });

                                    Toast.makeText(view.getContext(), "Delete", Toast.LENGTH_SHORT).show();
                                    break;
                                case 2:
                                    Intent msg = new Intent(Intent.ACTION_SEND);
                                    msg.addCategory(Intent.CATEGORY_DEFAULT);
                                    msg.putExtra(Intent.EXTRA_SUBJECT, "주제");
                                    msg.putExtra(Intent.EXTRA_TEXT, "내용");
                                    msg.putExtra(Intent.EXTRA_TITLE, "제목");
                                    msg.setType("text/plain");
                                    view.getContext().startActivity(Intent.createChooser(msg, "공유"));

                                    break;
                            }
                            dialog.dismiss();
                        }
                    });

                    builder.show();

                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());

                    if(friendChecked.equals("checked")){
                        final CharSequence info[] = new CharSequence[]{"친구삭제", "신고하기", "사용자 차단", "게시물 숨기기"};
                        builder.setTitle("");
                        builder.setItems(info, new DialogInterface.OnClickListener() {
                            @Override

                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0:
                                        Toast.makeText(view.getContext(), "deleteFriend", Toast.LENGTH_SHORT).show();
                                        friendChecked = "unchecked";
                                        firebaseDatabase = FirebaseDatabase.getInstance();
                                        DatabaseReference friend = firebaseDatabase.getReference("friend").child(nn[0]+"/"+nickName);
                                        FriendInfo friendInfo = new FriendInfo();
                                        friend.setValue(friendInfo);
                                        Toast.makeText(getApplicationContext(), "친구를 삭제하였습니다.", Toast.LENGTH_SHORT).show();
                                        break;
                                    case 1:
                                        // 내정보
                                        Toast.makeText(view.getContext(), "신고하기", Toast.LENGTH_SHORT).show();
                                        break;
                                    case 2:
                                        // 로그아웃

                                        Toast.makeText(view.getContext(), "사용자 차단", Toast.LENGTH_SHORT).show();
                                        break;
                                    case 3:

                                        Toast.makeText(view.getContext(), "게시물 숨기기", Toast.LENGTH_SHORT).show();

                                        break;
                                }
                                dialog.dismiss();
                            }
                        });
                    } else {
                        final CharSequence info[] = new CharSequence[]{"친구추가", "신고하기", "사용자 차단", "게시물 숨기기"};
                        builder.setTitle("");
                        builder.setItems(info, new DialogInterface.OnClickListener() {
                            @Override

                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0:
                                        Toast.makeText(view.getContext(), "addFriend", Toast.LENGTH_SHORT).show();
                                        friendChecked = "checked";
                                        firebaseDatabase = FirebaseDatabase.getInstance();
                                        System.out.println(nn[0]+"fffff");
                                        DatabaseReference friend = firebaseDatabase.getReference("friend").child(nn[0]+"/"+nickName);

                                        Hashtable<String, String> numbers = new Hashtable<String, String>();
                                        numbers.put("message","없음");
                                        friend.setValue(numbers);

                                        Toast.makeText(getApplicationContext(), "친구를 추가하였습니다.", Toast.LENGTH_SHORT).show();
                                        break;
                                    case 1:
                                        // 내정보
                                        Toast.makeText(view.getContext(), "신고하기", Toast.LENGTH_SHORT).show();
                                        break;
                                    case 2:
                                        // 로그아웃

                                        Toast.makeText(view.getContext(), "사용자 차단", Toast.LENGTH_SHORT).show();
                                        break;
                                    case 3:

                                        Toast.makeText(view.getContext(), "게시물 숨기기", Toast.LENGTH_SHORT).show();

                                        break;
                                }
                                dialog.dismiss();
                            }
                        });
                    }
                    builder.show();
                }
            }
        });



        bookmark_button = (CheckBox)findViewById(R.id.bookmark_button);
        if(bookmarkChecked.equals("checked")){
            bookmark_button.setChecked(true);
        }
        firebaseDatabase = FirebaseDatabase.getInstance();
        bookmark_button.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                DatabaseReference bookmark = firebaseDatabase.getReference("bookmark").child(user.getUid()+"/"+postID);
                BookmarkInfo bookmarkInfo = new BookmarkInfo();
                if(b){
                    bookmarkInfo.setPostID(postID);
                    db.collection("user-checked/"+user.getUid()+"/bookmark").document(postID).set(bookmarkInfo)
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
                    db.collection("user-checked/"+user.getUid()+"/bookmark").document(postID)
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

        Like_button = (CheckBox)findViewById(R.id.Like_button);
        if(likeChecked.equals("checked")){

            Like_button.setChecked(true);
        }
        Like_button.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                PostLikeInfo postLikeInfo = new PostLikeInfo();
                if (b) {
                    postLikeInfo.setPostID(postID);
                    db.collection("user-checked/"+user.getUid()+"/like").document(postID).set(postLikeInfo)
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
                    db.collection("user-checked/"+user.getUid()+"/like").document(postID)
                            .delete()
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
                }
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home ){
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 0:
                if (resultCode == RESULT_OK) {
                    content = data.getStringExtra("content");
                    imageUrl1 = data.getStringExtra("imageUrl1");
                    imageUrl2 = data.getStringExtra("imageUrl2");
                    Log.d("ㅍ", "onActivityResult: 값은?"+imageUrl2);
                    imageUrl3= data.getStringExtra("imageUrl3");
                    Log.d("ㅍ", "onActivityResult: 값은?"+imageUrl3);
                    imageUrl4 = data.getStringExtra("imageUrl4");
                    Log.d("ㅍ", "onActivityResult: 값은?"+imageUrl4);
                    imageUrl5 = data.getStringExtra("imageUrl5");
                    Log.d("ㅍ", "onActivityResult: 값은?"+imageUrl5);


                    post_content = findViewById(R.id.main_textView);
                    post_content.setText(content);

                    viewPager = (ViewPager) findViewById(R.id.main_image);
                    viewPageAdapter = new ViewPageAdapterDetail(true, imageUrl1, imageUrl2, imageUrl3, imageUrl4, imageUrl5, getApplicationContext());
                    viewPager.setAdapter(viewPageAdapter);

                    wormDotsIndicator  = (WormDotsIndicator) findViewById(R.id.worm_dots_indicator);
                    wormDotsIndicator .setViewPager(viewPager);

                } else {
                }
                break;
        }
    }

}