package com.example.petdiary.activity;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static android.os.Environment.DIRECTORY_PICTURES;

public class CameraAppActivity2 extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private static final int REQUEST_IMAGE_CAPTURE = 672;
    private String imageFilePath;
    private Uri photoUri;
    private String imageFileName;
    File image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException e) {

            }
            if (photoFile != null) {
                photoUri = FileProvider.getUriForFile(getApplicationContext(), getPackageName(), photoFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    String aaa;
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_kkmmss").format(new Date());
        imageFileName = "PetDiary" + timeStamp + "_";
        File storageDir = getExternalFilesDir(DIRECTORY_PICTURES);
        image = File.createTempFile(imageFileName, ".jpg", storageDir);
        imageFilePath = image.getAbsolutePath();
        return image;
    }

    String name;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            Intent resultIntent = new Intent();
            resultIntent.putExtra("name",imageFileName);

            mAuth = FirebaseAuth.getInstance();
            FirebaseStorage storage = FirebaseStorage.getInstance("gs://petdiary-794c6.appspot.com");
            final StorageReference storageRef = storage.getReference();

            StorageReference riversRef = storageRef.child("chatImage/" + imageFileName);

            Uri file = Uri.fromFile(new File(imageFilePath));

            UploadTask uploadTask = riversRef.putFile(file);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    System.out.println("tlfvo");
                    Log.e("### ImageChoice2", exception.toString());
                    // Handle unsuccessful uploads
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    System.out.println("tjdrhd");
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                    // ...
                }
            });

            setResult(2, resultIntent);

            if (Build.VERSION.SDK_INT >= 29) {
                // Sdk 21버전부터 실행할 코드
                AlbumAdd(imageFilePath);
            }

            finish();
        } else if (resultCode == RESULT_CANCELED) {
            finish();
        }
    }


    //앨범에 추가
    public void AlbumAdd( String cacheFilePath ) {
        if ( cacheFilePath == null ) return;
        BitmapFactory.Options options = new BitmapFactory.Options( );
        ExifInterface exifInterface = null;
        try {
            exifInterface = new ExifInterface( cacheFilePath );
        } catch ( Exception e ) {
            e.printStackTrace( );
        }
        int exifOrientation;
        int exifDegree = 0;
        //사진 회전값 구하기
        if ( exifInterface != null ) {
            exifOrientation = exifInterface.getAttributeInt( ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL );

            if ( exifOrientation == ExifInterface.ORIENTATION_ROTATE_90 ) {
                exifDegree = 90;
            } else if ( exifOrientation == ExifInterface.ORIENTATION_ROTATE_180 ) {
                exifDegree = 180;
            } else if ( exifOrientation == ExifInterface.ORIENTATION_ROTATE_270 ) {
                exifDegree = 270;
            }
        }
        Bitmap bitmap = BitmapFactory.decodeFile( cacheFilePath, options );
        Matrix matrix = new Matrix( );
        matrix.postRotate( exifDegree );
        Bitmap exifBit = Bitmap.createBitmap( bitmap, 0, 0, bitmap.getWidth( ), bitmap.getHeight( ), matrix, true );
        ContentValues values = new ContentValues( );
        //실제 앨범에 저장될 이미지이름
        values.put( MediaStore.Images.Media.DISPLAY_NAME, "PetDiary_" + new SimpleDateFormat( "yyyyMMdd_kkmmss", Locale.US ).format( new Date( ) ) + ".jpg" );
        values.put( MediaStore.Images.Media.MIME_TYPE, "image/*" );
        //저장될 경로
        values.put( MediaStore.Images.Media.RELATIVE_PATH, "DCIM/PetDiary" );
        values.put( MediaStore.Images.Media.ORIENTATION, exifDegree );
        values.put( MediaStore.Images.Media.IS_PENDING, 1 );

        Uri u = MediaStore.Images.Media.getContentUri( MediaStore.VOLUME_EXTERNAL );
        Uri uri = getContentResolver( ).insert( u, values );

        try {
            ParcelFileDescriptor parcelFileDescriptor = getContentResolver( ).openFileDescriptor( uri, "w", null );
            if ( parcelFileDescriptor == null ) return;

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream( );
            exifBit.compress( Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream );
            byte[] b = byteArrayOutputStream.toByteArray( );
            InputStream inputStream = new ByteArrayInputStream( b );

            ByteArrayOutputStream buffer = new ByteArrayOutputStream( );
            int bufferSize = 1024;
            byte[] buffers = new byte[ bufferSize ];

            int len = 0;
            while ( ( len = inputStream.read( buffers ) ) != -1 ) {
                buffer.write( buffers, 0, len );
            }

            byte[] bs = buffer.toByteArray( );
            FileOutputStream fileOutputStream = new FileOutputStream( parcelFileDescriptor.getFileDescriptor( ) );
            fileOutputStream.write( bs );
            fileOutputStream.close( );
            inputStream.close( );
            parcelFileDescriptor.close( );

            getContentResolver( ).update( uri, values, null, null );

        } catch ( Exception e ) {
            e.printStackTrace( );
        }

        values.clear( );
        values.put( MediaStore.Images.Media.IS_PENDING, 0 );
        getContentResolver( ).update( uri, values, null, null );
    }

}