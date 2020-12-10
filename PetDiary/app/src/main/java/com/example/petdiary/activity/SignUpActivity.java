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

import com.example.petdiary.info.MemberInfo;
import com.example.petdiary.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignUpActivity extends AppCompatActivity {

    private static final String TAG = "SignUpActivity";
    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;

    EditText emailEditText;
    EditText passwordEditText;
    EditText passwordCheckEditText;
    EditText nickNameEditText;

    ImageView emailCheckedImg;
    ImageView emailUnCheckImg;
    ImageView passwordCheckedImg;
    ImageView passwordUnCheckedImg;
    ImageView passwordCheckCheckedImg;
    ImageView passwordCheckUnCheckedImg;
    ImageView nickNameCheckedImg;
    ImageView nickNameUnCheckedImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setHomeAsUpIndicator(R.drawable.menu);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        findViewById(R.id.loginButton).setOnClickListener(onClickListener);
        findViewById(R.id.signUpButton).setOnClickListener(onClickListener);

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        passwordCheckEditText = findViewById(R.id.passwordCheckEditText);
        nickNameEditText = findViewById(R.id.nickNameEditText);

        emailEditText.addTextChangedListener(textWatcherEmail);
        passwordEditText.addTextChangedListener(textWatcherPassword);
        passwordCheckEditText.addTextChangedListener(textWatcherPasswordCheck);
        nickNameEditText.addTextChangedListener(textWatcherNickName);

        emailCheckedImg = findViewById(R.id.emailCheckedImg);
        emailUnCheckImg = findViewById(R.id.emailUnCheckedImg);
        passwordCheckedImg = findViewById(R.id.passwordCheckedImg);
        passwordUnCheckedImg = findViewById(R.id.passwordUnCheckedImg);
        passwordCheckCheckedImg = findViewById(R.id.passwordCheckCheckedImg);
        passwordCheckUnCheckedImg = findViewById(R.id.passwordCheckUnCheckedImg);
        nickNameCheckedImg = findViewById(R.id.nickNameCheckedImg);
        nickNameUnCheckedImg = findViewById(R.id.nickNameUnCheckedImg);
    }

    View.OnClickListener onClickListener = new View.OnClickListener(){

        @Override
        public void onClick(View v) {
            switch(v.getId()){
                case R.id.loginButton:
                    startLoginActivity();
                    finish();
                    break;
                case R.id.signUpButton:
                    String email = ((EditText)findViewById(R.id.emailEditText)).getText().toString();
                    String password = ((EditText)findViewById(R.id.passwordEditText)).getText().toString();
                    String passwordCheck = ((EditText)findViewById(R.id.passwordCheckEditText)).getText().toString();
                    final String nickName = ((EditText)findViewById(R.id.nickNameEditText)).getText().toString();
                    if(isValidEmail(email)){
                        if(isValidPassword(password)){
                            if(password.equals(passwordCheck) && isValidPassword(passwordCheck)){
                                if(isValidNickName(nickName)){
                                    final int[] check = {0};
                                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                                    db.collection("users")
                                            .get()
                                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                    if (task.isSuccessful()) {
                                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                                            //Log.d(TAG, document.getId() + " => " + document.getData());
                                                            if(nickName.equals(document.getData().get("nickName").toString())){
                                                                check[0]++;
                                                                break;
                                                            }
                                                        }
                                                    } else {
                                                        //Log.d(TAG, "Error getting documents: ", task.getException());
                                                    }
                                                    if(check[0] == 0){
                                                        signup();
                                                    } else {
                                                        startToast("중복된 닉네임 입니다.");
                                                    }
                                                }
                                            });
                                } else {
                                    startToast("닉네임을 확인해 주세요.");
                                }
                            } else {
                                startToast("비밀번호가 일치하지 않습니다.");
                            }
                        } else {
                            startToast("비밀번호를 확인해 주세요.(8~20 글자, 영문, 숫자, 특수문자 1자리 필수)");
                        }
                    } else {
                        startToast("이메일 주소를 확인해 주세요.");
                    }
                    break;
            }
        }
    };

    private void startLoginActivity(){
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public static boolean isValidEmail(String email) {
        boolean err = false;
        String regex = "^[_a-z0-9-]+(.[_a-z0-9-]+)*@(?:\\w+\\.)+\\w+$";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(email);
        if(m.matches()) {
            err = true;
        }
        return err;
    }

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

    public static boolean isValidNickName(final String nickName){
        boolean err = false;
        String regex = "^[a-zA-Z0-9]*$";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(nickName);
        if(m.matches()){
            if(nickName.length() > 1 && nickName.length() < 9){
                err = true;
            }
        }
        return err;
    }

    private void signup(){
        String email = ((EditText)findViewById(R.id.emailEditText)).getText().toString();
        String password = ((EditText)findViewById(R.id.passwordEditText)).getText().toString();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            //Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();

                            String email = ((EditText)findViewById(R.id.emailEditText)).getText().toString();
                            String password = ((EditText)findViewById(R.id.passwordEditText)).getText().toString();
                            String nickName = ((EditText)findViewById(R.id.nickNameEditText)).getText().toString();

                            //FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            FirebaseFirestore db = FirebaseFirestore.getInstance();

                            MemberInfo memberInfo = new MemberInfo(email, password, nickName, "", "");
                            db.collection("users").document(user.getUid()).set(memberInfo)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            startToast("회원가입에 성공하였습니다.");
                                            //startToast("회원정보등록을 성공하였습니다.");
                                            Log.d(TAG, "DocumentSnapshot successfully written!");
                                            FirebaseAuth.getInstance().signOut();
                                            startLoginActivity();
                                            finishAffinity();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            startToast("입력 정보를 확인해주세요.");
                                            Log.w(TAG, "Error writing document", e);
                                        }
                                    });

                        } else {
                            if(task.getException() != null){
                                startToast("이미 존재하는 이메일 입니다.");
                            }
                        }
                    }
                });
    }

    private void startToast(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    public final TextWatcher textWatcherEmail = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if(isValidEmail(emailEditText.getText().toString())){
                firebaseFirestore = FirebaseFirestore.getInstance();
                CollectionReference collectionReference = firebaseFirestore.collection("users");
                collectionReference.get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    int k = 0;
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        if(document.getData().get("email").toString().equals(emailEditText.getText().toString())){
                                            k++;
                                            break;
                                        }
                                    }
                                    if(k == 0){
                                        emailUnCheckImg.setVisibility(View.INVISIBLE);
                                        emailCheckedImg.setVisibility(View.VISIBLE);
                                    } else {
                                        emailCheckedImg.setVisibility(View.INVISIBLE);
                                        emailUnCheckImg.setVisibility(View.VISIBLE);
                                    }
                                } else {
                                    Log.d(TAG, "Error getting documents: ", task.getException());
                                }
                            }
                        });
            } else {
                emailCheckedImg.setVisibility(View.INVISIBLE);
                emailUnCheckImg.setVisibility(View.VISIBLE);
            }
        }
        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    public final TextWatcher textWatcherPassword = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if(isValidPassword(passwordEditText.getText().toString())){
                passwordUnCheckedImg.setVisibility(View.INVISIBLE);
                passwordCheckedImg.setVisibility(View.VISIBLE);
                if(passwordEditText.getText().toString().equals(passwordCheckEditText.getText().toString()) && isValidPassword(passwordCheckEditText.getText().toString())){
                    passwordCheckUnCheckedImg.setVisibility(View.INVISIBLE);
                    passwordCheckCheckedImg.setVisibility(View.VISIBLE);
                } else {
                    passwordCheckCheckedImg.setVisibility(View.INVISIBLE);
                    passwordCheckUnCheckedImg.setVisibility(View.VISIBLE);
                }
            } else {
                passwordCheckedImg.setVisibility(View.INVISIBLE);
                passwordUnCheckedImg.setVisibility(View.VISIBLE);
            }
        }
        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    public final TextWatcher textWatcherPasswordCheck = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if(passwordEditText.getText().toString().equals(passwordCheckEditText.getText().toString()) && isValidPassword(passwordCheckEditText.getText().toString())){
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

    public final TextWatcher textWatcherNickName = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if(isValidNickName(nickNameEditText.getText().toString())){
                firebaseFirestore = FirebaseFirestore.getInstance();
                CollectionReference collectionReference = firebaseFirestore.collection("users");
                collectionReference.get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    int k = 0;
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        if(document.getData().get("nickName").toString().equals(nickNameEditText.getText().toString())){
                                            k++;
                                            break;
                                        }
                                    }
                                    if(k == 0){
                                        nickNameUnCheckedImg.setVisibility(View.INVISIBLE);
                                        nickNameCheckedImg.setVisibility(View.VISIBLE);
                                    } else {
                                        nickNameCheckedImg.setVisibility(View.INVISIBLE);
                                        nickNameUnCheckedImg.setVisibility(View.VISIBLE);
                                    }
                                } else {
                                    Log.d(TAG, "Error getting documents: ", task.getException());
                                }
                            }
                        });
            } else {
                nickNameCheckedImg.setVisibility(View.INVISIBLE);
                nickNameUnCheckedImg.setVisibility(View.VISIBLE);
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
