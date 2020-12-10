package com.example.petdiary.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.petdiary.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class FindPasswordActivity extends AppCompatActivity {

    private static final String TAG = "FindPasswordActivity";
    private FirebaseFirestore firebaseFirestore;

    EditText emailEditText;
    EditText nickNameEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_findpassword);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        emailEditText = findViewById(R.id.emailEditText);
        nickNameEditText = findViewById(R.id.nickNameEditText);

        findViewById(R.id.findPasswordButton).setOnClickListener(onClickListener);
        findViewById(R.id.cancelButton).setOnClickListener(onClickListener);

    }

    View.OnClickListener onClickListener = new View.OnClickListener(){

        @Override
        public void onClick(View v) {
            switch(v.getId()){
                case R.id.findPasswordButton:
                    firebaseFirestore = FirebaseFirestore.getInstance();
                    CollectionReference collectionReference = firebaseFirestore.collection("users");
                    collectionReference.get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        int k=0;
                                        int j=0;
                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                            if(document.getData().get("email").toString().equals(emailEditText.getText().toString())){
                                                k = 1;
                                                if(document.getData().get("nickName").toString().equals(nickNameEditText.getText().toString())){
                                                    j = 1;
                                                }
                                            }
                                        }
                                        if(k == 1 && j == 1){
                                            FirebaseAuth auth = FirebaseAuth.getInstance();
                                            String emailAddress = emailEditText.getText().toString();

                                            auth.sendPasswordResetEmail(emailAddress)
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                Log.d(TAG, "Email sent.");
                                                            }
                                                        }
                                                    });
                                            Log.d("@@@", "성공");
                                            startToast("비밀번호 재설정 이메일을 발송하였습니다.");
                                            finish();
                                        } else if(k == 0 && j == 0){
                                            startToast("일치하는 Email이 없습니다.");
                                        } else if(k == 1 && j == 0){
                                            startToast("일치하는 닉네임이 없습니다.");
                                        } else {
                                            startToast("비밀번호 재설정 이메일 발송에 실패하였습니다.");
                                            Log.d("@@@", "실패");
                                        }
                                    } else {
                                        Log.d(TAG, "Error getting documents: ", task.getException());
                                    }
                                }
                            });
                    break;
                case R.id.cancelButton:
                    finish();
                    break;
            }
        }
    };

    private void startToast(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

}
