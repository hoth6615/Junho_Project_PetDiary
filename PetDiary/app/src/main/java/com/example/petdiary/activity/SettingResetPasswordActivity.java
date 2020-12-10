package com.example.petdiary.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.petdiary.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SettingResetPasswordActivity extends AppCompatActivity {

    private static final String TAG = "SettingResetPassword";
    private FirebaseAuth mAuth;

    private String email;
    private String nickName;
    private String password;


    EditText passwordEditText;
    EditText passwordCheckEditText;

    ImageView passwordCheckedImg;
    ImageView passwordUnCheckedImg;
    ImageView passwordCheckCheckedImg;
    ImageView passwordCheckUnCheckedImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_password);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        email = intent.getStringExtra("email");

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        findViewById(R.id.okButton).setOnClickListener(onClickListener);
        findViewById(R.id.cancelButton).setOnClickListener(onClickListener);

        passwordEditText = findViewById(R.id.passwordEditText);
        passwordCheckEditText = findViewById(R.id.passwordCheckEditText);

        passwordEditText.addTextChangedListener(textWatcherPassword);
        passwordCheckEditText.addTextChangedListener(textWatcherPasswordCheck);

        passwordCheckedImg = findViewById(R.id.passwordCheckedImg);
        passwordUnCheckedImg = findViewById(R.id.passwordUnCheckedImg);
        passwordCheckCheckedImg = findViewById(R.id.passwordCheckCheckedImg);
        passwordCheckUnCheckedImg = findViewById(R.id.passwordCheckUnCheckedImg);
    }

    View.OnClickListener onClickListener = new View.OnClickListener(){

        @Override
        public void onClick(View v) {
            switch(v.getId()){
                case R.id.okButton:
                    //setInfo();
                    setPassword();
                    break;
                case R.id.cancelButton:
                    //startToast("취소버튼 클릭");
                    finish();
                    break;
            }
        }
    };

    public static boolean isValidPassword(String password){
        boolean err = false;
        String regex = "^(?=.*[0-9])(?=.*[a-z])(?=.*\\W)(?=\\S+$).{8,20}$";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(password);
        if(m.matches()){
            err = true;
        }
        return err;
    }

//    private void setInfo(){
//        DocumentReference documentReference = FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid());
//        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                if (task.isSuccessful()) {
//                    DocumentSnapshot document = task.getResult();
//                    //Log.d("@@@", FirebaseAuth.getInstance().getCurrentUser().getUid());
//                    if (document != null) {
//                        if (document.exists()) {
//                            Log.d(TAG, "DocumentSnapshot data: " + document.getData());
//                            email = document.getData().get("email").toString();
//                            nickName = document.getData().get("nickName").toString();
//                        } else {
//                            Log.d(TAG, "No such document");
//                        }
//                    }
//                } else {
//                    Log.d(TAG, "get failed with ", task.getException());
//                }
//            }
//        });
//    }

    private void setPassword(){
        password = ((EditText)findViewById(R.id.passwordEditText)).getText().toString();
        String passwordCheck = ((EditText)findViewById(R.id.passwordCheckEditText)).getText().toString();

        if(isValidPassword(password)){
            if(password.equals(passwordCheck) && isValidPassword(passwordCheck)){

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                final String newPassword = password;


                user.updatePassword(newPassword)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    startToast("비밀번호가 변경되었습니다.");
                                    //Log.d(TAG, "User password updated.");

                                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                    FirebaseFirestore db = FirebaseFirestore.getInstance();

                                    login();

                                    DocumentReference washingtonRef = db.collection("users").document(user.getUid());
                                    washingtonRef
                                            .update("password", newPassword)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Log.d(TAG, "DocumentSnapshot successfully updated!");
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.w(TAG, "Error updating document", e);
                                                }
                                            });

                                    finish();

                                } else {
                                    startToast(task.getException().toString());
                                    //Log.d("@@@TAG", task.getException().toString());
                                }
                            }
                        });
            } else {
                startToast("비밀번호가 일치하지 않습니다.");
            }
        } else {
            startToast("비밀번호를 확인해 주세요.(8~20 글자, 영문, 숫자, 특수문자 1자리 필수)");
        }
    }

    private void login(){
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            startToast("로그인에 성공하였습니다.");
                        } else {
                            // If sign in fails, display a message to the user.
                            //Log.w(TAG, "signInWithEmail:failure", task.getException());
                            if(task.getException() != null){
                                Log.d(TAG, task.getException().toString());
                                startToast("로그인에 실패하였습니다.");
                            }
                        }
                    }
                });
    }

    private void startToast(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }



    private final TextWatcher textWatcherPassword = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if(isValidPassword(passwordEditText.getText().toString())){
                passwordUnCheckedImg.setVisibility(View.INVISIBLE);
                passwordCheckedImg.setVisibility(View.VISIBLE);
            } else {
                passwordCheckedImg.setVisibility(View.INVISIBLE);
                passwordUnCheckedImg.setVisibility(View.VISIBLE);
            }
        }
        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    private final TextWatcher textWatcherPasswordCheck = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if(passwordEditText.getText().toString().equals(passwordCheckEditText.getText().toString())){
                passwordCheckUnCheckedImg.setVisibility(View.INVISIBLE);
                passwordCheckCheckedImg.setVisibility(View.VISIBLE);
            } else {
                passwordCheckCheckedImg.setVisibility(View.INVISIBLE);
                passwordCheckUnCheckedImg.setVisibility(View.VISIBLE);
            }
        }
        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home ){
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
