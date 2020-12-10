package com.example.petdiary.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

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
import java.util.Locale;

public class ContentEditActivity extends AppCompatActivity {
    private static final String TAG = "NewPost_Fragment";

    ImageView[] postImg = new ImageView[5];
    ImageView[] deletePostImg = new ImageView[5];
    int[] postImgCheck = new int[5];
    int choiceNum;
    RelativeLayout loaderLayout;
    Spinner spinner;
    TextView contentsLengthTextView;
    EditText contents;

    private String imageUrl1;
    private String imageUrl2;
    private String imageUrl3;
    private String imageUrl4;
    private String imageUrl5;
    private String Category;
    private String Content;
    private String PostID;

    ArrayList<String> items;
    ArrayList<String> petsID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content_edit);


        Intent intent = getIntent();
        imageUrl1 = intent.getStringExtra("imageUrl1");
        imageUrl2 = intent.getStringExtra("imageUrl2");
        imageUrl3 = intent.getStringExtra("imageUrl3");
        imageUrl4 = intent.getStringExtra("imageUrl4");
        imageUrl5 = intent.getStringExtra("imageUrl5");
        Category = intent.getStringExtra("category");
        Content = intent.getStringExtra("content");
        PostID = intent.getStringExtra("postID");

        Log.d("ddddddd", "onCreate: 포스트아이디"+PostID);

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
        spinner = (Spinner) findViewById(R.id.categorySpinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.spinner_item, items);
        //ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), R.layout.spinner_item, itemss);
        adapter.setDropDownViewResource(R.layout.spinner_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                category = spinner.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                category = "";
            }
        });

        //카테고리 정보 불러오기




        contentsLengthTextView = (TextView) findViewById(R.id.contentsLengthTextView);
        contents = (EditText) findViewById(R.id.contents);
        contents.setText(Content) ;
        contentsLengthTextView.setText(Content.length() + " / 100");

        contents.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String input = contents.getText().toString();
                contentsLengthTextView.setText(input.length() + " / 100");
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        findViewById(R.id.postButton).setOnClickListener(onClickListener);

        postImg[0] = findViewById(R.id.postImg1);
        postImg[0].setOnClickListener(onClickListener);
        postImg[1] = findViewById(R.id.postImg2);
        postImg[1].setOnClickListener(onClickListener);
        postImg[2] = findViewById(R.id.postImg3);
        postImg[2].setOnClickListener(onClickListener);
        postImg[3] = findViewById(R.id.postImg4);
        postImg[3].setOnClickListener(onClickListener);
        postImg[4] = findViewById(R.id.postImg5);
        postImg[4].setOnClickListener(onClickListener);

        deletePostImg[0] = findViewById(R.id.deletePostImg1);
        deletePostImg[0].setOnClickListener(onClickListener);
        deletePostImg[1] = findViewById(R.id.deletePostImg2);
        deletePostImg[1].setOnClickListener(onClickListener);
        deletePostImg[2] = findViewById(R.id.deletePostImg3);
        deletePostImg[2].setOnClickListener(onClickListener);
        deletePostImg[3] = findViewById(R.id.deletePostImg4);
        deletePostImg[3].setOnClickListener(onClickListener);
        deletePostImg[4] = findViewById(R.id.deletePostImg5);
        deletePostImg[4].setOnClickListener(onClickListener);

        deletePostImg[0].bringToFront();
        deletePostImg[1].bringToFront();
        deletePostImg[2].bringToFront();
        deletePostImg[3].bringToFront();
        deletePostImg[4].bringToFront();

        for (int i = 0; i < 5; i++) {
            imgUri[i] = "";
        }

        String postImgPath[] = {imageUrl1,imageUrl2,imageUrl3,imageUrl4,imageUrl5};
        if(!imageUrl1.equals("https://firebasestorage.googleapis.com/v0/b/petdiary-794c6.appspot.com/o/images%2Fempty.png?alt=media&token=c41b1cc0-d610-4964-b00c-2638d4bfd8bd")) {
                    Log.d("@@##", "onCreate: 이미지url"+ imageUrl1);
                    postImgCheck[0] = 0;
                    choiceNum = 0;
                    setPostImg(postImgPath[0]);
        }
        if(!imageUrl2.equals("")) {
            postImgCheck[1] = 0;
            choiceNum = 1;
            setPostImg(postImgPath[1]);
        }
        if(!imageUrl3.equals("")) {
            postImgCheck[2] = 0;
            choiceNum = 2;
            setPostImg(postImgPath[2]);
        }
        if(!imageUrl4.equals("")) {
            postImgCheck[3] = 0;
            choiceNum = 3;
            setPostImg(postImgPath[3]);
        }
        if(!imageUrl5.equals("")) {
            Log.d("@@##", "onCreate: 이미지url"+ imageUrl5);
            postImgCheck[4] = 0;
            choiceNum = 4;
            setPostImg(postImgPath[4]);
        }
    }






    View.OnClickListener onClickListener = new View.OnClickListener(){

        @Override
        public void onClick(View v) {
            switch(v.getId()){
                case R.id.postButton:
                    // postData();
                    loaderLayout = findViewById(R.id.loaderLayout);
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
                Glide.with(getApplicationContext()).load(img[i-1]).centerCrop().override(500).into(postImg[i-1]);
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

    //이미지 선택하는 창으로 이동
    private void startPopupActivity(){
        Intent intent = new Intent(getApplicationContext(), ImageChoicePopupActivity.class);
        startActivityForResult(intent, 0);
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

    //이미지 설정
    private void setPostImg(String postImgPath){
        if(postImgCheck[0] == 0){
            Glide.with(this).load(postImgPath).centerCrop().override(500).into(postImg[0]);
            img[0] = postImgPath;
            Log.d("@@@@", "setPostImg: 값은 "+img[0]);
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


    private int num;
    private String category;
    private String[] img = new String[5];
    private String[] imgUri = new String[5];
    private String content = "";
    private String date2;


    private ArrayList<String> hashTag;

    private void post(){

        Log.v("dsdsdsdsds", "포스트 탑니까");

        num = 0;
        //category = spinner.getSelectedItem().toString();
        content = ((EditText) findViewById(R.id.contents)).getText().toString();
        long now = System.currentTimeMillis();
        Date nowdate = new Date(now);
        SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy/MM/dd kk:mm:ss");
        SimpleDateFormat sdfNow2 = new SimpleDateFormat("yyyy-MM-dd_kk:mm:ss");
        date2 = sdfNow2.format(nowdate);
        Log.v("dsdsdsdsds", "데이트2값 뭔데?"+date2);

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


    int postNumCheck = 0;
    boolean imgCheck = true;



    private void startToast(String msg){
        Toast.makeText(this.getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }



    private void saveImage(){
        if(img[0]==imageUrl1&&imgUri[0].equals("")){
            imgUri[0]=imageUrl1;
            postImgCheck[0] = 0;
            postData();
        }
        else if(img[0]==imageUrl2&&imgUri[0].equals("")){
            imgUri[0]=imageUrl2;
            postImgCheck[0] = 0;
            postData();
        }
        else if(img[0]==imageUrl3&&imgUri[0].equals("")){
            imgUri[0]=imageUrl3;
            postImgCheck[0] = 0;
            postData();
        }
        else if(img[0]==imageUrl4&&imgUri[0].equals("")){
            imgUri[0]=imageUrl4;
            postImgCheck[0] = 0;
            postData();
        }
        else if(img[0]==imageUrl5&&imgUri[0].equals("")){
            imgUri[0]=imageUrl5;
            postImgCheck[0] = 0;
            postData();
        }

        if(img[1]==imageUrl1&&imgUri[1].equals("")){
            imgUri[1]=imageUrl1;
            postImgCheck[1] = 0;
            postData();
        }
        else if(img[1]==imageUrl2&&imgUri[1].equals("")){
            imgUri[1]=imageUrl2;
            postImgCheck[1] = 0;
            postData();
        }
        else if(img[1]==imageUrl3&&imgUri[1].equals("")){
            imgUri[1]=imageUrl3;
            postImgCheck[1] = 0;
            postData();
        }
        else if(img[1]==imageUrl4&&imgUri[1].equals("")){
            imgUri[1]=imageUrl4;
            postImgCheck[1] = 0;
            postData();
        }
        else if(img[1]==imageUrl5&&imgUri[1].equals("")){
            imgUri[1]=imageUrl5;
            postImgCheck[1] = 0;
            postData();
        }

        if(img[2]==imageUrl1&&imgUri[2].equals("")){
            imgUri[2]=imageUrl1;
            postImgCheck[2] = 0;
            postData();
        }
        else if(img[2]==imageUrl2&&imgUri[2].equals("")){
            imgUri[2]=imageUrl2;
            postImgCheck[2] = 0;
            postData();
        }
        else if(img[2]==imageUrl3&&imgUri[2].equals("")){
            imgUri[2]=imageUrl3;
            postImgCheck[2] = 0;
            postData();
        }
        else if(img[2]==imageUrl4&&imgUri[2].equals("")){
            imgUri[2]=imageUrl4;
            postImgCheck[2] = 0;
            postData();
        }
        else if(img[2]==imageUrl5&&imgUri[2].equals("")){
            imgUri[2]=imageUrl5;
            postImgCheck[2] = 0;
            postData();
        }

        if(img[3]==imageUrl1&&imgUri[3].equals("")){
            imgUri[3]=imageUrl1;
            postImgCheck[3] = 0;
            postData();
        }
        else if(img[3]==imageUrl2&&imgUri[3].equals("")){
            imgUri[3]=imageUrl2;
            postImgCheck[3] = 0;
            postData();
        }
        else if(img[3]==imageUrl3&&imgUri[3].equals("")){
            imgUri[3]=imageUrl3;
            postImgCheck[3] = 0;
            postData();
        }
        else if(img[3]==imageUrl4&&imgUri[3].equals("")){
            imgUri[3]=imageUrl4;
            postImgCheck[3] = 0;
            postData();
        }
        else if(img[3]==imageUrl5&&imgUri[3].equals("")){
            imgUri[3]=imageUrl5;
            postImgCheck[3] = 0;
            postData();
        }

        if(img[4]==imageUrl1&&imgUri[4].equals("")){
            imgUri[4]=imageUrl1;
            postImgCheck[4] = 0;
            postData();
        }
        else if(img[4]==imageUrl2&&imgUri[4].equals("")){
            imgUri[4]=imageUrl2;
            postImgCheck[4] = 0;
            postData();
        }
        else if(img[4]==imageUrl3&&imgUri[4].equals("")){
            imgUri[4]=imageUrl3;
            postImgCheck[4] = 0;
            postData();
        }
        else if(img[4]==imageUrl4&&imgUri[4].equals("")){
            imgUri[4]=imageUrl4;
            postImgCheck[4] = 0;
            postData();
        }
        else if(img[4]==imageUrl5&&imgUri[4].equals("")){
            imgUri[4]=imageUrl5;
            postImgCheck[4] = 0;
            postData();
        }

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


                                            postData();




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



    private void postData(){

        int i;
        for(i=0; i<items.size(); i++){
            if(items.get(i).equals(category)){
                break;
            }
        }

        content = ((EditText) findViewById(R.id.contents)).getText().toString();

        final String Data = imgUri[0];
        final String Data2 = imgUri[1];
        final String Data3 = imgUri[2];
        final String Data4 = imgUri[3];
        final String Data5 = imgUri[4];

        final String imgData = img[0];
        final String imgData2 = img[1];
        final String imgData3 = img[2];
        final String imgData4 = img[3];
        final String imgData5 = img[4];


        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference washingtonRef = db.collection("post").document(PostID);


        washingtonRef
                .update("category",category, "petsID", petsID.get(i), "content",content,"imageUrl1",imgUri[0],"imageUrl2",imgUri[1],"imageUrl3",imgUri[2],"imageUrl4",imgUri[3],"imageUrl5",imgUri[4],"hashTag",hashTag)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");

                        setDirEmpty();



                        if(imgData!=null&&imgData2==null){//&&imgData3.equals(null)&&imgData4.equals(null)&&imgData5.equals(null)) {
                            if(!Data.equals("")) {

                                Intent intent = new Intent();
                                intent.putExtra("content", content);

                                intent.putExtra("imageUrl1", Data);

                                intent.putExtra("imageUrl2", Data2);

                                intent.putExtra("imageUrl3", Data3);
                                intent.putExtra("imageUrl4", Data4);


                                intent.putExtra("imageUrl5", Data5);

                                setResult(RESULT_OK, intent);
                                loaderLayout.setVisibility(View.INVISIBLE);
                                startToast("게시글을 수정하였습니다.");

                                finish();
                            }
                        }

                   else if(imgData!=null&&imgData2!=null&&imgData3==null){//&&imgData3.equals(null)&&imgData4.equals(null)&&imgData5.equals(null)) {
                        if(!Data.equals("")&&!Data2.equals("")) {

                            Intent intent = new Intent();
                            intent.putExtra("content", content);

                            intent.putExtra("imageUrl1", Data);

                            intent.putExtra("imageUrl2", Data2);

                            intent.putExtra("imageUrl3", Data3);
                            intent.putExtra("imageUrl4", Data4);


                            intent.putExtra("imageUrl5", Data5);

                            setResult(RESULT_OK, intent);
                            loaderLayout.setVisibility(View.INVISIBLE);
                            startToast("게시글을 수정하였습니다.");

                            finish();
                        }
                    }

                       else if(imgData!=null&&imgData2!=null&&imgData3!=null&&imgData4==null){//&&imgData3.equals(null)&&imgData4.equals(null)&&imgData5.equals(null)) {
                            if(!Data.equals("")&&!Data2.equals("")&&!Data3.equals("")) {

                                Intent intent = new Intent();
                                intent.putExtra("content", content);

                                intent.putExtra("imageUrl1", Data);

                                intent.putExtra("imageUrl2", Data2);

                                intent.putExtra("imageUrl3", Data3);
                                intent.putExtra("imageUrl4", Data4);


                                intent.putExtra("imageUrl5", Data5);

                                setResult(RESULT_OK, intent);
                                loaderLayout.setVisibility(View.INVISIBLE);
                                startToast("게시글을 수정하였습니다.");

                                finish();
                            }
                        }

                     else if(imgData!=null&&imgData2!=null&&imgData3!=null&&imgData4!=null&&imgData5==null){//&&imgData3.equals(null)&&imgData4.equals(null)&&imgData5.equals(null)) {
                            if(!Data.equals("")&&!Data2.equals("")&&!Data3.equals("")&&!Data4.equals("")) {

                                Intent intent = new Intent();
                                intent.putExtra("content", content);

                                intent.putExtra("imageUrl1", Data);

                                intent.putExtra("imageUrl2", Data2);

                                intent.putExtra("imageUrl3", Data3);
                                intent.putExtra("imageUrl4", Data4);


                                intent.putExtra("imageUrl5", Data5);

                                setResult(RESULT_OK, intent);
                                loaderLayout.setVisibility(View.INVISIBLE);
                                startToast("게시글을 수정하였습니다.");

                                finish();
                            }
                        }
                    else if(imgData!=null&&imgData2!=null&&imgData3!=null&&imgData4!=null&&imgData5!=null){//&&imgData3.equals(null)&&imgData4.equals(null)&&imgData5.equals(null)) {
                        if(!Data.equals("")&&!Data2.equals("")&&!Data3.equals("")&&!Data4.equals("")&&!Data5.equals("")) {

                            Intent intent = new Intent();
                            intent.putExtra("content", content);

                            intent.putExtra("imageUrl1", Data);

                            intent.putExtra("imageUrl2", Data2);

                            intent.putExtra("imageUrl3", Data3);
                            intent.putExtra("imageUrl4", Data4);


                            intent.putExtra("imageUrl5", Data5);

                            setResult(RESULT_OK, intent);
                            loaderLayout.setVisibility(View.INVISIBLE);
                            startToast("게시글을 수정하였습니다.");


                            finish();
                        }
                    }

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




}
