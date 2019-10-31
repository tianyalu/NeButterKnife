package com.sty.ne.butterknife;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.sty.ne.butterknife.annotations.BindView;
import com.sty.ne.butterknife.annotations.OnClick;
import com.sty.ne.butterknife.library.ButterKnife;


public class MainActivity extends AppCompatActivity {
    @BindView(R.id.tv)
    TextView tv;
    @BindView(R.id.tv2)
    TextView tv2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //相当于new MainActivity$ViewBinder(this)
        ButterKnife.bind(this);

        tv.setText("修改后的文字");
    }

    @OnClick(R.id.tv)
    public void click() {
        Toast.makeText(this, tv.getText().toString(), Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.tv2)
    public void click2() {
        Toast.makeText(this, tv2.getText().toString(), Toast.LENGTH_SHORT).show();
    }
}
