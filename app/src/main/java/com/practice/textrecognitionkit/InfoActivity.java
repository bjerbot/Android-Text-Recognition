package com.practice.textrecognitionkit;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.practice.textrecognitionkit.databinding.ActivityInfoBinding;

import java.util.ArrayList;
import java.util.HashMap;

public class InfoActivity extends AppCompatActivity {
    ActivityInfoBinding binding;
    private DB db;
    static int row_position;
    SimpleAdapter simpleAdapter;
    private ArrayList<HashMap<String, Object>> mData2;
    private String[] from;
    private int[] to;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setToolbar();

        db=new DB(this);
        Cursor cursor=db.getAll();
        Intent intentPos =getIntent();
        row_position=intentPos.getIntExtra("position", -1);
        cursor.moveToPosition(row_position);
        InitListView(cursor);

        int img_col=cursor.getColumnIndex("img");
        binding.ivBusinessCardEdit.setOnClickListener(v->{
            Intent intent=new Intent(InfoActivity.this, ShowImageActivity.class);
            intent.putExtra("img", cursor.getBlob(img_col));
            intent.putExtra("position", row_position+1);
            startActivity(intent);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        DB db =new DB(this);
        Cursor cursor=db.getAll();
        cursor.moveToPosition(row_position);
        InitImage(cursor);
    }

    private void setToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("聯絡人");
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        toolbar.setContentInsetStartWithNavigation(0);
        toolbar.setNavigationIcon(getDrawable(android.R.drawable.ic_menu_revert));
        toolbar.setNavigationOnClickListener(v -> {
            finish();
        });
    }

    private void InitImage(Cursor cursor){
        int img_col=cursor.getColumnIndex("img");
        Bitmap bitmap = BitmapFactory.decodeByteArray(cursor.getBlob(img_col), 0, cursor.getBlob(img_col).length);
        binding.ivBusinessCardEdit.setImageBitmap(bitmap);
    }

    public Cursor InitListView(Cursor cursor){
        ListViewAdapter listViewAdapter=new ListViewAdapter(this, db);
        binding.lvContactInfo.setAdapter(listViewAdapter);
        return cursor;
    }
}