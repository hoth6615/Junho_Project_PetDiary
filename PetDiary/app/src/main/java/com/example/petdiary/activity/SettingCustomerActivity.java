package com.example.petdiary.activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.petdiary.info.CostomerCenterInfo;
import com.example.petdiary.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SettingCustomerActivity extends AppCompatActivity {

    Spinner spinner;

    private String category;
    private String email;
    private String contents;
    private String date;

    TextView contentsLengthTextView;
    EditText content;

    private FirebaseDatabase firebaseDatabase;
    RelativeLayout loaderLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_customer);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        contentsLengthTextView = (TextView) findViewById(R.id.contentsLengthTextView);

        content = (EditText) findViewById(R.id.contents);

        content.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String input = content.getText().toString();
                contentsLengthTextView.setText(input.length()+" / 100");
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        String[] items = {"회원정보", "게시글", "신고", "제안"};
        spinner = (Spinner) findViewById(R.id.contactTypesSpinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.spinner_item, items);
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

        findViewById(R.id.okButton).setOnClickListener(onClickListener);
        findViewById(R.id.cancelButton).setOnClickListener(onClickListener);


    }

    View.OnClickListener onClickListener = new View.OnClickListener(){

        @Override
        public void onClick(View v) {
            switch(v.getId()){
                case R.id.okButton:
                    loaderLayout = findViewById(R.id.loaderLayout);
                    loaderLayout.setVisibility(View.VISIBLE);
                    //startToast("등록 버튼 클릭");
                    register();
                    break;
                case R.id.cancelButton:
                    finish();
                    break;
            }
        }
    };

    public void register(){
        firebaseDatabase = FirebaseDatabase.getInstance();

        email = ((EditText) findViewById(R.id.email)).getText().toString();
        contents = ((EditText) findViewById(R.id.contents)).getText().toString();
        long now = System.currentTimeMillis();
        Date nowdate = new Date(now);
        SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy/MM/dd kk:mm:ss");
        date = sdfNow.format(nowdate);

        DatabaseReference customerCenter = firebaseDatabase.getReference().child("customerCenter").push();

        CostomerCenterInfo costomerCenterInfo = new CostomerCenterInfo();
        costomerCenterInfo.setEmail(email);
        costomerCenterInfo.setCategory(category);
        costomerCenterInfo.setContents(contents);
        costomerCenterInfo.setUid(FirebaseAuth.getInstance().getCurrentUser().getUid());
        costomerCenterInfo.setDate(date);

        //게시물을 데이터를 생성 및 엑티비티 종료
        customerCenter.setValue(costomerCenterInfo);

        setResult(RESULT_OK);
        startToast("게시글을 등록하였습니다.");
        loaderLayout.setVisibility(View.INVISIBLE);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home ){
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void startToast(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

}
