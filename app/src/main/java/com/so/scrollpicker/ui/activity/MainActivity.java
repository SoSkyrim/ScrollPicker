package com.so.scrollpicker.ui.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.so.scrollpicker.R;
import com.so.scrollpicker.ui.view.SPView;
import com.so.scrollpicker.utils.UIUtil;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private SPView mSpHour;
    private SPView mSpMin;
    private String mHour;
    private String mMin;
    private TextView mTvSelectTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initUI();
        initSP();
    }

    private void initUI() {
        mTvSelectTime = (TextView) findViewById(R.id.tv_select_time);
    }

    /**
     * 初始化滚动选择器
     */
    private void initSP() {
        android.support.v7.app.AlertDialog.Builder builder
                = new android.support.v7.app.AlertDialog.Builder(MainActivity.this);

        builder.setIcon(R.mipmap.ic_launcher);
        builder.setTitle(R.string.scroll_picker);

        // 获取选择器
        View view = View.inflate(this, R.layout.pop_scroll_picker, null);
        mSpHour = (SPView) view.findViewById(R.id.sp_hour);
        mSpMin = (SPView) view.findViewById(R.id.sp_min);

        fillData();

        // 设置视图
        builder.setView(view);

        builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mTvSelectTime.setText(String.format(
                        UIUtil.getString(R.string.cur_time),
                        mHour, mMin));
            }
        });

        builder.setCancelable(false);
        android.support.v7.app.AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * 填充数据, 添加监听
     */
    private void fillData() {
        List<String> hour = new ArrayList<String>();
        List<String> min = new ArrayList<String>();
        for (int i = 0; i < 24; i++) {
            hour.add(i < 10 ? "0" + i : "" + i);
        }
        for (int i = 0; i < 60; i++) {
            min.add(i < 10 ? "0" + i : "" + i);
        }

        mHour = String.valueOf(hour.size() / 2);
        mMin = String.valueOf(min.size() / 2);

        mSpHour.setData(hour);
        mSpHour.setOnSelectListener(new SPView.onSelectListener() {
            @Override
            public void onSelect(String text) {
                Toast.makeText(UIUtil.getContext(), text + " 时",
                        Toast.LENGTH_SHORT).show();
                mHour = text;
            }
        });

        mSpMin.setData(min);
        mSpMin.setOnSelectListener(new SPView.onSelectListener() {
            @Override
            public void onSelect(String text) {
                Toast.makeText(UIUtil.getContext(), text + " 分",
                        Toast.LENGTH_SHORT).show();
                mMin = text;
            }
        });
    }
}
