package com.ibm.notepad;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import static com.ibm.notepad.DatabaseHelper.DB_NAME;//引入包名要正确！
import static com.ibm.notepad.DatabaseHelper.TABLE_NAME;
import static com.ibm.notepad.DatabaseHelper.VERSION;

public class MainActivity extends AppCompatActivity {//设置沉浸式状态栏和标题栏父类是Activity？

    public static DatabaseHelper dbHelper;
    private SQLiteDatabase db;
    private List<String> diary=new ArrayList<>();
    public static final int TAG_INSERT=1;
    public static final int TAG_UPDATE=0;
    private String select_item;
    private int Id;
    ListView listView;
    ArrayAdapter<String> adapter;

    private SwipeRefreshLayout swipeRefresh;

    public static DatabaseHelper getDbHelper(){
        return dbHelper;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button add=(Button)findViewById(R.id.add);
        swipeRefresh=(SwipeRefreshLayout)findViewById(R.id.swipe_refresh);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout
                .OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });
        dbHelper=new DatabaseHelper(MainActivity.this,DB_NAME,null,VERSION);
        dbHelper.getWritableDatabase();
        init();
        //设置点击按钮事件，从而添加笔记
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this,DetailActivity.class);
                intent.putExtra("TAG",TAG_INSERT);
                startActivity(intent);
            }
        });

        //设置列表项目点击事件
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Intent intent=new Intent(MainActivity.this,DetailActivity.class);
                Id=getDiaryId(position);
                intent.putExtra("ID",Id);
                intent.putExtra("TAG",TAG_UPDATE);
                startActivity(intent);
            }
        });
    }

    private void init(){
        db=dbHelper.getWritableDatabase();
        diary.clear();
        //查询数据库，将title一列添加到列表项目中
        Cursor cursor=db.query(TABLE_NAME,null,null,null,null,null,null);
        if(cursor.moveToFirst()){
            String diary_item;
            do{
                diary_item=cursor.getString(cursor.getColumnIndex("title"));
                diary.add(diary_item);
            }while(cursor.moveToNext());
        }
        cursor.close();
        adapter=new ArrayAdapter<String>(
                MainActivity.this,android.R.layout.simple_list_item_1,diary);
        listView=(ListView)findViewById(R.id.list_item);
        listView.setAdapter(adapter);
    }

    private void refresh(){
        new Thread(new Runnable(){
            @Override
            public void run() {
                try{
                    Thread.sleep(1000);
                }catch(InterruptedException e){
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        init();
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        }).start();
    }

    private int getDiaryId(int position){
        //获取所点击的日记的title
        int Id;
        select_item=diary.get(position);
        //获取id
        db=dbHelper.getWritableDatabase();
        Cursor cursor=db.query(TABLE_NAME,new String[]{"id"},"title=?",
                new String[]{select_item},null,null,null);
        cursor.moveToFirst();
        Id=cursor.getInt(cursor.getColumnIndex("id"));
        return Id;
    }

    //实现再按一次就可退出程序
    private long exitTime = 0;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){
            if((System.currentTimeMillis()-exitTime) > 2000){
                Toast.makeText(getApplicationContext(), "再按一次退出记事本", Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                finish();
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}

