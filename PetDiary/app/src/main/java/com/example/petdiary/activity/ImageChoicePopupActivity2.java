package com.example.petdiary.activity;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.loader.content.CursorLoader;

import com.example.petdiary.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


public class ImageChoicePopupActivity2 extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //타이틀바 없애기
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_image_choice_popup);
    }


    public void goCamera(View v) {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                //startToast("권한을 허용하셨습니다.");
            } else {
                //startToast("권한을 허용해 주세요.");
            }
        } else {
            StartActivity(CameraAppActivity2.class);
        }
    }

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

    public void goGallery(View v) {

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)){
                //startToast("권한을 허용하셨습니다.");
            } else {
                //startToast("권한을 허용해 주세요.");
            }
        } else {
            myStartActivity();
        }

    }


    private String postImgPath;
    String[] sImg;
    int PICK_IMAGE_MULTIPLE = 1001;
    String[] name;
    String ca;
    ClipData clipData;


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Get a non-default Storage bucket
        FirebaseStorage storage = FirebaseStorage.getInstance("gs://petdiary-794c6.appspot.com");
        StorageReference storageRef = storage.getReference();
        Intent resultIntent2 = new Intent();
        ca = new String();
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_MULTIPLE && data != null) {

            clipData = data.getClipData();
            if (data.getClipData() == null) {
                Toast.makeText(this, "다중선택이 불가한 기기입니다", Toast.LENGTH_LONG).show();
            } else {
                if (clipData.getItemCount() > 9) {
                    Toast.makeText(this, "사진은 9장까지만 선택 가능합니다", Toast.LENGTH_LONG).show();
                } else {

                    postImgPath = data.getStringExtra("postImgPath");
                    sImg = new String[clipData.getItemCount()];
                    for (int i = 0; i < clipData.getItemCount(); i++) {
                        name = new String[clipData.getItemCount()];
                        name[i] = getImageNameToUri(clipData.getItemAt(i).getUri());

                        Uri file = Uri.fromFile(new File(getPath(clipData.getItemAt(i).getUri())));

                        //sImg[i] = clipData.getItemAt(i).getUri().toString();
                        sImg[i] = sendPicture(clipData.getItemAt(i).getUri());
                        Log.d("vcxz", name[i]);
                        Log.d("zxcv", sImg[i]);
                        resultIntent2.putExtra("postImgPath" + i + "", name[i]);
                        resultIntent2.putExtra("uri" + i + "", sImg[i]);

                        StorageReference riversRef = storageRef.child("chatImage/" + file.getLastPathSegment());

                        UploadTask uploadTask = riversRef.putFile(file);
                        uploadTask.addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {

                                Log.e("### ImageChoice2", exception.toString());

                            }
                        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {


                            }
                        });

                    }
                }
            }
            setResult(Activity.RESULT_OK, resultIntent2);
            finish();
        } else if (requestCode == 2 && data != null) {

            ca = data.getStringExtra("name");
            System.out.println(ca+"nulll");
            resultIntent2 = new Intent();
            resultIntent2.putExtra("camera", ca);
            setResult(2, resultIntent2);
            finish();

        }
    }

    private String sendPicture(Uri imgUri) {
        String imagePath = getRealPathFromURI(imgUri); // path 경로
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(imagePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        //int exifDegree = exifOrientationToDegrees(exifOrientation);
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);//경로를 통해 비트맵으로 전환
        return imagePath;
    }


    private String getRealPathFromURI(Uri contentUri) {
        int column_index = 0;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor.moveToFirst()) {
            column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        }
        return cursor.getString(column_index);
    }

    private void myStartActivity() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_MULTIPLE);
    }

    private void StartActivity(Class c) {
        Intent intent = new Intent(this, c);
        startActivityForResult(intent, 2);
    }

    private void startToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        //바깥레이어 클릭시 안닫히게
//        if(event.getAction()==MotionEvent.ACTION_OUTSIDE){
//            return false;
//        }
//        return true;
//    }
//
//    @Override
//    public void onBackPressed() {
//        //안드로이드 백버튼 막기
//        return;
//    }


    public String getPath(Uri uri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        CursorLoader cursorLoader = new CursorLoader(this, uri, proj, null, null, null);
        Cursor cursor = cursorLoader.loadInBackground();
        int index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

        cursor.moveToFirst();

        return cursor.getString(index);
    }

    public String getImageNameToUri(Uri data) {
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(data, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String imgPath = cursor.getString(column_index);
        String imgName = imgPath.substring(imgPath.lastIndexOf("/") + 1);
        return imgName;
    }
    private void tedPermission() {

        PermissionListener permissionListener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                // 권한 요청 성공

            }

            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                // 권한 요청 실패
            }
        };

        TedPermission.with(this)
                .setPermissionListener(permissionListener)
                .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .check();

    }


}
