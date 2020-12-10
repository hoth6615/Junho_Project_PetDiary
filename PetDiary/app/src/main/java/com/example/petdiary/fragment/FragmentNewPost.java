package com.example.petdiary.fragment;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.petdiary.info.PostInfo;
import com.example.petdiary.activity.*;
import com.example.petdiary.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

public class FragmentNewPost extends Fragment {

    private static final String TAG = "NewPost_Fragment";

    ImageView[] postImg = new ImageView[5];
    ImageView[] deletePostImg = new ImageView[5];
    int[] postImgCheck = new int[5];
    int choiceNum;
    RelativeLayout loaderLayout;
    Spinner spinner;
    TextView contentsLengthTextView;
    EditText contents;

    ViewGroup viewGroup;

    ArrayList<String> items;
    ArrayList<String> petsID;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_newpost, container, false);

        items = new ArrayList<String>();
        petsID = new ArrayList<String>();
        items.add("전체");
        petsID.add("ALL");
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("pets/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/pets")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                items.add(document.getData().get("petName").toString());
                                petsID.add(document.getId());
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
        spinner = (Spinner)viewGroup.findViewById(R.id.categorySpinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), R.layout.spinner_item, items);
        adapter.setDropDownViewResource(R.layout.spinner_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                category = spinner.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                category = "";
            }
        });

        setEmail();


        contentsLengthTextView = (TextView) viewGroup.findViewById(R.id.contentsLengthTextView);
        contents = (EditText) viewGroup.findViewById(R.id.contents);

        contents.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String input = contents.getText().toString();
                contentsLengthTextView.setText(input.length()+" / 100");
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        viewGroup.findViewById(R.id.postButton).setOnClickListener(onClickListener);

        postImg[0] = viewGroup.findViewById(R.id.postImg1);
        postImg[0].setOnClickListener(onClickListener);
        postImg[1] = viewGroup.findViewById(R.id.postImg2);
        postImg[1].setOnClickListener(onClickListener);
        postImg[2] = viewGroup.findViewById(R.id.postImg3);
        postImg[2].setOnClickListener(onClickListener);
        postImg[3] = viewGroup.findViewById(R.id.postImg4);
        postImg[3].setOnClickListener(onClickListener);
        postImg[4] = viewGroup.findViewById(R.id.postImg5);
        postImg[4].setOnClickListener(onClickListener);

        deletePostImg[0] = viewGroup.findViewById(R.id.deletePostImg1);
        deletePostImg[0].setOnClickListener(onClickListener);
        deletePostImg[1] = viewGroup.findViewById(R.id.deletePostImg2);
        deletePostImg[1].setOnClickListener(onClickListener);
        deletePostImg[2] = viewGroup.findViewById(R.id.deletePostImg3);
        deletePostImg[2].setOnClickListener(onClickListener);
        deletePostImg[3] = viewGroup.findViewById(R.id.deletePostImg4);
        deletePostImg[3].setOnClickListener(onClickListener);
        deletePostImg[4] = viewGroup.findViewById(R.id.deletePostImg5);
        deletePostImg[4].setOnClickListener(onClickListener);

        deletePostImg[0].bringToFront();
        deletePostImg[1].bringToFront();
        deletePostImg[2].bringToFront();
        deletePostImg[3].bringToFront();
        deletePostImg[4].bringToFront();

        for(int i=0; i<5; i++){
            imgUri[i] = "";
        }

        return viewGroup;
    }

    View.OnClickListener onClickListener = new View.OnClickListener(){

        @Override
        public void onClick(View v) {
            switch(v.getId()){
                case R.id.postButton:
                    loaderLayout = viewGroup.findViewById(R.id.loaderLayout);
                    loaderLayout.setVisibility(View.VISIBLE);
                    post();
                    break;
                case R.id.postImg1:
                    startPopupActivity();
                    choiceNum = 0;
                    break;
                case R.id.postImg2:
                    startPopupActivity();
                    choiceNum = 1;
                    break;
                case R.id.postImg3:
                    startPopupActivity();
                    choiceNum = 2;
                    break;
                case R.id.postImg4:
                    startPopupActivity();
                    choiceNum = 3;
                    break;
                case R.id.postImg5:
                    startPopupActivity();
                    choiceNum = 4;
                    break;
                case R.id.deletePostImg1:
                    cancelImg(1);
                    break;
                case R.id.deletePostImg2:
                    cancelImg(2);
                    break;
                case R.id.deletePostImg3:
                    cancelImg(3);
                    break;
                case R.id.deletePostImg4:
                    cancelImg(4);
                    break;
                case R.id.deletePostImg5:
                    cancelImg(5);
                    break;
            }
        }
    };

    private void startPopupActivity(){
        Intent intent = new Intent(getContext(), ImageChoicePopupActivity.class);
        startActivityForResult(intent, 0);
    }

    private void cancelImg(int a){
        int count = 0;
        for(int i=0; i<5; i++){
            if(postImgCheck[i] == 1){
                count++;
            }
        }
        if(count == 1){
            postImgCheck[0] = 0;
            postImg[0].setImageResource(R.drawable.ic_baseline_add_24);
            deletePostImg[0].setVisibility(View.INVISIBLE);
            img[0] = null;
        } else {
            for(int i=a; i<count; i++){
                img[i-1] = img[i];
                Glide.with(getContext()).load(img[i-1]).centerCrop().override(500).into(postImg[i-1]);
            }
            postImgCheck[count-1] = 0;
            postImg[count-1].setImageResource(R.drawable.ic_baseline_add_24);
            deletePostImg[count-1].setVisibility(View.INVISIBLE);
            img[count-1] = null;
        }
        postNumCheck--;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults){
        switch(requestCode){
            case 1:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    //myStartActivity(GalleryActivity.class);
                } else {
                    startToast("권한을 허용해 주세요.");
                }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        switch(requestCode){
            case 0:
                if(resultCode == RESULT_OK){
                    String postImgPath = data.getStringExtra("postImgPath");
                    setPostImg(postImgPath);
                } else if(resultCode == RESULT_CANCELED) {
                    Log.e("postImgPath", "실패!");
                }
                break;
        }
    }

    private void setPostImg(String postImgPath){
        if(postImgCheck[0] == 0){
            Glide.with(this).load(postImgPath).centerCrop().override(500).into(postImg[0]);
            img[0] = postImgPath;
            postImgCheck[0] = 1;
            postNumCheck = 1;
            deletePostImg[0].setVisibility(View.VISIBLE);
        } else if(postImgCheck[1] == 0){
            Glide.with(this).load(postImgPath).centerCrop().override(500).into(postImg[1]);
            img[1] = postImgPath;
            postImgCheck[1] = 1;
            postNumCheck = 2;
            deletePostImg[1].setVisibility(View.VISIBLE);
        } else if(postImgCheck[2] == 0){
            Glide.with(this).load(postImgPath).centerCrop().override(500).into(postImg[2]);
            img[2] = postImgPath;
            postImgCheck[2] = 1;
            postNumCheck = 3;
            deletePostImg[2].setVisibility(View.VISIBLE);
        } else if(postImgCheck[3] == 0){
            Glide.with(this).load(postImgPath).centerCrop().override(500).into(postImg[3]);
            img[3] = postImgPath;
            postImgCheck[3] = 1;
            postNumCheck = 4;
            deletePostImg[3].setVisibility(View.VISIBLE);
        } else if(postImgCheck[4] == 0){
            Glide.with(this).load(postImgPath).centerCrop().override(500).into(postImg[4]);
            img[4] = postImgPath;
            postImgCheck[4] = 1;
            postNumCheck = 5;
            deletePostImg[4].setVisibility(View.VISIBLE);
        } else if(postImgCheck[0] == 1 && postImgCheck[1] == 1 && postImgCheck[2] == 1 && postImgCheck[3] == 1 && postImgCheck[4] == 1){
            Glide.with(this).load(postImgPath).centerCrop().override(500).into(postImg[choiceNum]);
            img[choiceNum] = postImgPath;
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home ){
            getActivity().finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private String email;
    private String category;
    private String[] img = new String[5];
    private String[] imgUri = new String[5];
    private String content = "";
    private String tag;
    private String date;
    private String date2;
    private String nickName;
    private int favoriteCount = 0;

    private ArrayList<String> hashTag;

    private void post(){
        //category = spinner.getSelectedItem().toString();
        content = ((EditText) viewGroup.findViewById(R.id.contents)).getText().toString();
        tag = "";
        long now = System.currentTimeMillis();
        Date nowdate = new Date(now);
        SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy/MM/dd kk:mm:ss");
        SimpleDateFormat sdfNow2 = new SimpleDateFormat("yyyy-MM-dd_kk:mm:ss");
        date = sdfNow.format(nowdate);
        date2 = sdfNow2.format(nowdate);

        hashTag = new ArrayList<String>();
        if(content.length() > 0){
            String[] word = content.split(" |\\n");

            for(int i=0; i<word.length; i++){
                if(word[i].charAt(0) == '#'){
                    hashTag.add(word[i]);
                }
            }
        }
        if(content.length() > 0 || img[0] != null){
            saveImage();
        } else {
            loaderLayout.setVisibility(View.INVISIBLE);
            startToast("사진과 내용 중 하나는 입력해주세요.");
        }

    }

    private void setEmail(){
        DocumentReference documentReference = FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid());
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null) {
                        if (document.exists()) {
                            Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                            email = document.getData().get("email").toString();
                            nickName = document.getData().get("nickName").toString();
                        } else {
                            Log.d(TAG, "No such document");
                        }
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    int postNum = 0;
    int postNumCheck = 0;

    private void saveImage(){

        FirebaseStorage storage = FirebaseStorage.getInstance();
        final StorageReference storageRef = storage.getReference();
        final UploadTask[] uploadTask = new UploadTask[1];

        if(postNumCheck == 0){
            imgUri[0] = "https://firebasestorage.googleapis.com/v0/b/petdiary-794c6.appspot.com/o/images%2Fempty.png?alt=media&token=c41b1cc0-d610-4964-b00c-2638d4bfd8bd";
            postData();
        } else {
            for(int i=0; i<5; i++){
                if(postImgCheck[i] == 1){
                    final Uri file;
                    file = Uri.fromFile(new File(img[i]));
                    StorageReference riversRef = storageRef.child("images/"+date2+"_postImg_"+i);
                    uploadTask[0] = riversRef.putFile(file);

                    final int finalI = i;
                    final int finalI1 = i;
                    uploadTask[0].addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            final StorageReference ref = storageRef.child("images/"+date2+"_postImg_"+ finalI);
                            uploadTask[0] = ref.putFile(file);

                            Task<Uri> urlTask = uploadTask[0].continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                                @Override
                                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                    if (!task.isSuccessful()) {
                                        throw task.getException();
                                    }
                                    return ref.getDownloadUrl();
                                }
                            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    if (task.isSuccessful()) {
                                        Uri downloadUri = task.getResult();
                                        imgUri[finalI1] = downloadUri.toString();
                                        postNum++;
                                        if(postNum == postNumCheck){
                                            postData();
                                        }
                                    } else {
                                    }
                                }
                            });
                        }
                    });
                }
            }
        }
    }

    BottomNavigationView bottomNavigationView;
    Menu menu;

    private void postData(){

        PostInfo postInfo = new PostInfo();

        int i;
        for(i=0; i<items.size(); i++){
            if(items.get(i).equals(category)){
                break;
            }
        }

        postInfo.setImageUrl1(imgUri[0]);
        postInfo.setImageUrl2(imgUri[1]);
        postInfo.setImageUrl3(imgUri[2]);
        postInfo.setImageUrl4(imgUri[3]);
        postInfo.setImageUrl5(imgUri[4]);
        postInfo.setUid(FirebaseAuth.getInstance().getCurrentUser().getUid());
        postInfo.setContent(content);
        postInfo.setEmail(email);
        postInfo.setCategory(category);
        postInfo.setPetsID(petsID.get(i));
        postInfo.setDate(date);
        postInfo.setNickName(nickName);
        postInfo.setHashTag(hashTag);
        postInfo.setFavoriteCount(favoriteCount);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String doucmentPath = date2 + "_" + user.getUid();

        db.collection("post").document(doucmentPath).set(postInfo)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        startToast("게시글을 등록하였습니다.");
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                        getActivity().setResult(RESULT_OK);
                        loaderLayout.setVisibility(View.INVISIBLE);

                        bottomNavigationView = getActivity().findViewById(R.id.bottomNavigationView);
                        menu = bottomNavigationView.getMenu();
                        menu.findItem(R.id.tab3).setChecked(false);
                        menu.findItem(R.id.tab1).setChecked(true);

                        setDirEmpty();

                        ((MainActivity)getActivity()).replaceFragment();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        startToast("입력 정보를 확인해주세요.");
                        Log.w(TAG, "Error writing document", e);
                    }
                });
    }

    public void setDirEmpty(){
        String path = "/storage/emulated/0/Android/data/com.example.petdiary/files/Pictures";
        File dir = new File(path);
        File[] childFileList = dir.listFiles();
        if (dir.exists()) {
            for (File childFile : childFileList) {
                if (childFile.isDirectory()) {
                    //setDirEmpty(childFile.getAbsolutePath());
                    //하위 디렉토리
                } else {
                    childFile.delete();
                    //하위 파일
                }
            }
            dir.delete();
        }
    }

    private void startToast(String msg){
        Toast.makeText(getActivity().getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

}

