package com.practice.textrecognitionkit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.practice.textrecognitionkit.databinding.ActivityClassificationBinding;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class classification extends AppCompatActivity {
    ActivityClassificationBinding binding;
    ArrayList<String> strList=new ArrayList<>();
    String company, name, tel, fax, phone, address, web;
    Bitmap bitmap;
    Uri uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityClassificationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setToolbar();

        Intent intent =getIntent();
        String strUri=intent.getParcelableExtra("uri").toString();
        uri=Uri.parse(strUri);
        name=intent.getStringExtra("name");
        company=intent.getStringExtra("company");
        tel=intent.getStringExtra("tel");
        fax=intent.getStringExtra("fax");
        phone=intent.getStringExtra("phone");
        address=intent.getStringExtra("address");
        web=intent.getStringExtra("web");

        binding.ivBusinessCard.setImageURI(uri);
        binding.etName.setText(name);
        binding.etCompany.setText(company);
        binding.etTel.setText(tel);
        binding.etFax.setText(fax);
        binding.etPhone.setText(phone);
        binding.etAddress.setText(address);
        binding.etWeb.setText(web);

        binding.btSave.setOnClickListener(v -> {
            btSaveListener();
        });
    }

    private void setToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("建立聯絡人");
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        toolbar.setContentInsetStartWithNavigation(0);
        toolbar.setNavigationIcon(getDrawable(android.R.drawable.ic_menu_revert));
        toolbar.setNavigationOnClickListener(v -> {
            finish();
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.info_menu, menu);
        return true;
    }

    private void dialogAbout() {
        new AlertDialog.Builder(this)
                .setTitle("App Developers")
                .setMessage("1. Hsu HanHung ")
                .setPositiveButton("CLOSE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.saveInfo:
                btSaveListener();
                break;
            case R.id.about:
                dialogAbout();
                break;
        }
        return true;
    }

    private void btSaveListener(){
        if(!binding.etName.getText().toString().equals("")) {

            //URI Transfer to Bitmap
            InputStream inputStream = null;
            try {
                inputStream = getContentResolver().openInputStream(uri);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            BitmapDrawable bitmapDrawable = (BitmapDrawable) Drawable.createFromStream(inputStream, uri.toString() );

            int img_h=bitmapDrawable.getBitmap().getHeight();
            int img_w=bitmapDrawable.getBitmap().getWidth();
            bitmap=Bitmap.createScaledBitmap(bitmapDrawable.getBitmap(), 512, 384, false);

            HashMap<String, Object> map = new HashMap<>();
            map.put("no", new DB(this).getAll().getCount()+1);
            map.put("name", binding.etName.getText());
            map.put("company", binding.etCompany.getText());
            map.put("tel", binding.etTel.getText());
            map.put("fax", binding.etFax.getText());
            map.put("phone", binding.etPhone.getText());
            map.put("address", binding.etAddress.getText());
            map.put("web", binding.etWeb.getText());
            DB db = new DB(this);
            db.addData(map, bitmap);
            Intent intentToMain = new Intent(this, MainActivity.class);
            startActivity(intentToMain);
        }
        else{
            Toast.makeText(this, "姓名不可空白", Toast.LENGTH_SHORT).show();
        }
    }
}