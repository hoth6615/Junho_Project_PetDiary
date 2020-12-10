package com.example.petdiary.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.LogoutResponseCallback;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KakaoSignUpActivity extends AppCompatActivity {

    private static final String TAG = "KakaoLoginActivity";
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

    private String kakaoNickName;
    private String kakaoProfile;
    private String kakaoEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kakao_sign_up);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        Intent intent = getIntent();
        if(intent != null) {
            if(intent.getStringExtra("nickName") != null){ // 카카오톡 로그인 시 정보 받아오기
                kakaoNickName = intent.getStringExtra("nickName");
                kakaoProfile = intent.getStringExtra("profile");
                kakaoEmail = intent.getStringExtra("email");
            }
        }

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

        if(isValidEmail(kakaoEmail)){
            emailEditText.setText(kakaoEmail);
            emailUnCheckImg.setVisibility(View.INVISIBLE);
            emailCheckedImg.setVisibility(View.VISIBLE);
        }
        if(isValidNickName(kakaoNickName)){
            nickNameEditText.setText(kakaoNickName);
            nickNameUnCheckedImg.setVisibility(View.INVISIBLE);
            nickNameCheckedImg.setVisibility(View.VISIBLE);
        }

        emailWatcher(emailEditText.getText().toString());
        nickNameWatcher(nickNameEditText.getText().toString());

        findViewById(R.id.signUpButton).setOnClickListener(onClickListener);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
    }

    View.OnClickListener onClickListener = new View.OnClickListener(){

        @Override
        public void onClick(View v) {
            switch(v.getId()){
                case R.id.signUpButton:
                    signUp();
                    break;
            }
        }
    };

    private void anonymityLogin(final String email, final String password, final String nickName){
        mAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInAnonymously:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            singUpGo(email, password, nickName);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInAnonymously:failure", task.getException());
                            Toast.makeText(getApplicationContext(), "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    public void signUp(){
        final String email = ((EditText)findViewById(R.id.emailEditText)).getText().toString();
        final String password = ((EditText)findViewById(R.id.passwordEditText)).getText().toString();
        String passwordCheck = ((EditText)findViewById(R.id.passwordCheckEditText)).getText().toString();
        final String nickName = ((EditText)findViewById(R.id.nickNameEditText)).getText().toString();

        if(isValidEmail(email)){
            if(isValidPassword(password)){
                if(isValidPassword(passwordCheck)){
                    if(password.equals(passwordCheck)){
                        if(isValidNickName(nickName)){
                            final int[] check = {0};
                            Log.e("###", "여기까지왔다");
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
                                                anonymityLogin(email, password, nickName);
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
                    startToast("비밀번호 확인란을 확인해 주세요.(8~20 글자, 영문, 숫자, 특수문자 1자리 필수)");
                }
            } else {
                startToast("비밀번호를 확인해 주세요.(8~20 글자, 영문, 숫자, 특수문자 1자리 필수)");
            }
        } else {
            startToast("이메일 주소를 확인해 주세요.");
        }
    }

    private void singUpGo(final String email, final String password, final String nickName){
        AuthCredential credential = EmailAuthProvider.getCredential(email, password);

        mAuth.getCurrentUser().linkWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "linkWithCredential:success");
                            FirebaseUser user = task.getResult().getUser();

                            FirebaseFirestore db = FirebaseFirestore.getInstance();

                            MemberInfo memberInfo = new MemberInfo(email, password, nickName, kakaoProfile, "");
                            db.collection("users").document(user.getUid()).set(memberInfo)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            startToast("계정 등록을 성공하였습니다.");
                                            //startToast("회원정보등록을 성공하였습니다.");
                                            Log.d(TAG, "DocumentSnapshot successfully written!");
                                            //FirebaseAuth.getInstance().signOut();
                                            UserManagement.getInstance().requestLogout(new LogoutResponseCallback() { //로그아웃 실행
                                                @Override
                                                public void onCompleteLogout() {
                                                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                    startActivity(intent);
                                                }
                                            });
                                            startMainActivity();
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
                            Log.w(TAG, "linkWithCredential:failure", task.getException());
                            Toast.makeText(getApplicationContext(), "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });
    }

    private void startToast(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void startMainActivity(){
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public void onBackPressed(){
        UserManagement.getInstance().requestLogout(new LogoutResponseCallback() { //로그아웃 실행
            @Override
            public void onCompleteLogout() {
                finish();
            }
        });
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


    public final TextWatcher textWatcherEmail = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            emailWatcher(emailEditText.getText().toString());
        }
        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    private void emailWatcher(final String email){
        if(isValidEmail(email)){
            firebaseFirestore = FirebaseFirestore.getInstance();
            CollectionReference collectionReference = firebaseFirestore.collection("users");
            collectionReference.get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                int k = 0;
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    if(document.getData().get("email").toString().equals(email)){
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
            nickNameWatcher(nickNameEditText.getText().toString());
        }
        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    private void nickNameWatcher(final String nickName){
        if(isValidNickName(nickName)){
            firebaseFirestore = FirebaseFirestore.getInstance();
            CollectionReference collectionReference = firebaseFirestore.collection("users");
            collectionReference.get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                int k = 0;
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    if(document.getData().get("nickName").toString().equals(nickName)){
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
}
