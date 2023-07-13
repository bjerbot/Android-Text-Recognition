package com.practice.textrecognitionkit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import com.practice.textrecognitionkit.databinding.ActivityMainBinding;
import com.practice.textrecognitionkit.databinding.NavHeaderBinding;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    Toolbar toolbar;

    //select language(spinner)
    private Spinner spLanguage;
    private int Select_language;
    ArrayAdapter<CharSequence> spinnerAdapter;

    //Permission Code
    //Request Code
    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 400;
    //Result Code
    private static final int IMAGE_PICK_GALLERY_CODE = 1000;
    private static final int IMAGE_PICK_CAMERA_CODE = 2001;

    String cameraPermission[];
    String storagePermission[];

    //image crop & text recognition
    Uri image_uri;
    Uri intentUri;
    BitmapDrawable bitmapDrawable;
    TextRecognizer recognizer;
    ArrayList<String> strList;
    String company, name, tel, fax, phone, address, web;
    int beginIndex, endIndex;
    boolean isFinish=false;

    //database declare
    private DB db;
    LinkedList<HashMap<String, Object>> contactList;
    LinkedList<byte[]> streamList;

    //recycleView declare
    private RecyclerView recyclerView;
    RecycleAdapter recycleAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //set toolbar & get DrawerLayout & get Navigation view
        setToolbar();
        DrawerLayout drawerLayout=binding.drawerlayout;
        NavigationView navigationView=binding.navView;

        //get Navigation header from Navigation view
        View nav_header=navigationView.getHeaderView(0);
        //get text form Navigation header
        TextView userName=nav_header.findViewById(R.id.user_name);
        userName.setText(GoogleSignInClientSingleton.account.getDisplayName());
        TextView userEmail=nav_header.findViewById(R.id.user_email);
        userEmail.setText(GoogleSignInClientSingleton.account.getEmail());
        ImageView userPhoto=nav_header.findViewById(R.id.user_photo);
        try {
            String StringUrl=GoogleSignInClientSingleton.account.getPhotoUrl().toString();
            URL url=new URL(StringUrl);
            HttpURLConnection connection=(HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStream inputStream = connection.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                userPhoto.setImageBitmap(bitmap);
            }
            else{
                userName.setText(responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        ActionBarDrawerToggle actionBarDrawerToggle=new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId()){
                    case R.id.sign_out:
                        GoogleSignInClientSingleton.getFirebaseAuth().signOut();
                        GoogleSignInClientSingleton.getGoogleSignInClient(MainActivity.this).signOut()
                                .addOnCompleteListener(MainActivity.this, new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        GoogleSignInClientSingleton.account= GoogleSignIn.getLastSignedInAccount(MainActivity.this);
                                        if(GoogleSignInClientSingleton.account==null){
                                            finish();
                                        }
                                    }
                                });
                     return  true;
                }
                return false;
            }
        });

        //camera permission
        cameraPermission = new String[] {Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE};
        //storage permission
        storagePermission = new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE};

        //select language(spinner)
        spLanguage =findViewById(R.id.language);
        spinnerAdapter=ArrayAdapter.createFromResource(this, R.array.language_array, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_item);
        spLanguage.setAdapter(spinnerAdapter);
        spLanguage.setOnItemSelectedListener(spinnerelectedlistener);
    }

    @Override
    protected void onStart() {
        super.onStart();
        initDataBase();
    }

    @Override
    public void finish() {
        if(GoogleSignInClientSingleton.account==null) {
            super.finish();
        }
    }

    private void setToolbar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("名片掃描工具");
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        toolbar.setContentInsetStartWithNavigation(0);

//        toolbar.setNavigationOnClickListener(v -> {
//            Toast.makeText(this, "結束", Toast.LENGTH_SHORT).show();
//        });
//        toolbar.setNavigationIcon(getDrawable(R.drawable.ic_baseline_dehaze_24));
    }

    protected void initDataBase(){
        //導入自建的DB類，並藉由DB類裡的方法去取得Cursor
        db=new DB(this);
        Cursor cursor =db.getAll();

        contactList=new LinkedList<>();
        streamList=new LinkedList<>();
        if(cursor.getCount()>=0) {
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                int no_col=cursor.getColumnIndex("no");
                int name_col = cursor.getColumnIndex("name");
                int company_col = cursor.getColumnIndex("company");
                int img_col = cursor.getColumnIndex("img");
                if (name_col < 0 && company_col < 0 && img_col < 0) {
                    name_col = 0;
                    company_col = 0;
                    img_col = 0;
                }

                HashMap<String, Object> map = new HashMap<>();
                map.put("no", cursor.getString(no_col));
                map.put("name", cursor.getString(name_col));
                map.put("company", cursor.getString(company_col));
                contactList.add(map);
                streamList.add(cursor.getBlob(img_col));
            }

            //create recyclerView
            recyclerView=binding.recyclerView;

            //prepare LayoutManager & RecycleAdapter
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
            recycleAdapter=new RecycleAdapter(contactList, streamList, MainActivity.this, recyclerView);
            recycleAdapter.itemTouchHelper.attachToRecyclerView(recyclerView);
            recyclerView.setAdapter(recycleAdapter);

            //長按刪除(用右滑刪除取代 暫時取消)
            recycleAdapter.setOnItemClickListener(new RecycleAdapter.OnItemClickListener() {
                @Override
                public void onItemLongClick(final View view, final int row_position) {
                    PopupMenu popupMenu = new PopupMenu(MainActivity.this,view);
                    popupMenu.getMenuInflater().inflate(R.menu.delete_menu,popupMenu.getMenu());

                    //Popup menu click event
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            Toast.makeText(MainActivity.this, "已刪除", Toast.LENGTH_SHORT).show();
                            db.deleteData(row_position+1);
                            initDataBase();
                            return false;
                        }
                    });
                    popupMenu.show();
                }
            });
        }
    }

    //右上方選單顯示
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    //右上方選單功能
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.addImage:
                showImageImportDialog();
                break;
            case R.id.about:
                dialogAbout();
                break;
        }
        return true;
    }

    //右上角 Developers 資訊
    private void dialogAbout() {
        new AlertDialog.Builder(this)
                .setTitle("App Developers")
                .setMessage("1. Hsu HanHung ")
                .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    //選擇插入圖片的方式 利用dialog視窗
    private void showImageImportDialog() {
        String[] items = {"相機", "相簿"};
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);

        dialog.setTitle("選擇圖片");
        dialog.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    if (!checkCameraPermission()) {
                        //camera permission not allowed, request it
                        requestCameraPermission();
                    } else {
                        //permission allowed, take picture
                        pickCamera();
                    }
                }

                if (which == 1) {
                    if (!checkStoragePermission()) {
                        //storage permission not allowed, request it
                        requestStoragePermission();
                    } else {
                        //permission allowed, take picture
                        pickGallery();
                    }
                }
            }
        });
        dialog.create().show();
    }

    //spinner listener
    AdapterView.OnItemSelectedListener spinnerelectedlistener=new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            Select_language=position;
        }
        @Override
        public void onNothingSelected(AdapterView<?> parent) {}
    };

    private void pickGallery() {
        //intent to pick image from gallery (Implicit intent)
        Intent intent = new Intent(Intent.ACTION_PICK);
        //set intent type to image
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
    }

    private void pickCamera() {
        //intent to take image from camera, it will also be save to storage to get high quality image
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "NewPick"); //title of the picture
        values.put(MediaStore.Images.Media.DESCRIPTION, "Image To Text"); //title of the picture
        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE);
    }

    //請求儲存權限
    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, storagePermission, STORAGE_REQUEST_CODE);
    }
    //檢查儲存權限
    private boolean checkStoragePermission() {
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    //請求相機權限
    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, cameraPermission, CAMERA_REQUEST_CODE);
    }
    //檢查相機權限
    private boolean checkCameraPermission() {
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);

        boolean result1 = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    //handle permission result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_REQUEST_CODE:
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] ==
                            PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted = grantResults[0] ==
                            PackageManager.PERMISSION_GRANTED;

                    if (cameraAccepted && writeStorageAccepted) {
                        pickCamera();
                    } else {
                        Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                    }
                }
                break;

            case STORAGE_REQUEST_CODE:
                if (grantResults.length > 0) {
                    boolean writeStorageAccepted = grantResults[0] ==
                            PackageManager.PERMISSION_GRANTED;

                    if (writeStorageAccepted) {
                        pickGallery();
                    } else {
                        Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }

    //handle image result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                //got image from gallery now crop it
                intentUri=data.getData();
                InputStream inputStream = null;
                try {
                    inputStream = getContentResolver().openInputStream(data.getData());
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                bitmapDrawable = (BitmapDrawable) Drawable.createFromStream(inputStream, data.getData().toString() );
            }

            if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                //got image from camera now crop it
                intentUri=image_uri;
                InputStream inputStream = null;
                try {
                    inputStream = getContentResolver().openInputStream(image_uri);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                bitmapDrawable = (BitmapDrawable) Drawable.createFromStream(inputStream, image_uri.toString() );
            }
        }

        new ClassifyAsyncTask().execute();


    }

    /*以下是AsyncTask的運行流程：
    1.在UI線程上創建一個AsyncTask對象，並調用execute()方法。
    2.execute()方法調用onPreExecute()方法，執行UI初始化操作。
    3.execute()方法調用doInBackground()方法，在非UI線程上執行耗時的操作。
    4.doInBackground()方法調用publishProgress()方法。
    5.publishProgress()方法調用onProgressUpdate()方法，更新UI上的進度條。
    6.doInBackground()方法運行完畢，返回結果。
    7.execute()方法調用onPostExecute()方法，更新UI，例如顯示下載完成的圖像。*/

    class ClassifyAsyncTask extends AsyncTask<String, Integer, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.progressBackground.setBackgroundColor(getResources().getColor(R.color.progress));
        }

        @Override
        protected String doInBackground(String... strings) {
            if(bitmapDrawable!=null) {
                //recognition text
                Bitmap bitmap = bitmapDrawable.getBitmap();

                if (Select_language == 0) {
                    recognizer = TextRecognition.getClient(new ChineseTextRecognizerOptions.Builder().build());
                } else if (Select_language == 1) {
                    recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
                }


                InputImage image = InputImage.fromBitmap(bitmap, 0);
                Task<Text> items = recognizer.process(image);
                items.addOnSuccessListener(new OnSuccessListener<Text>() {
                    @Override
                    public void onSuccess(Text text) {
                        strList = new ArrayList<>();
                        for (Text.TextBlock block : text.getTextBlocks()) {
                            for (Text.Line line : block.getLines()) {
                                strList.add(line.getText());
                                //classification text
                                for(int i=0;i<strList.size();i++){
                                    for(int j=0;j<strList.get(i).length();j++){

                                        if(strList.get(i).contains("公司")||strList.get(i).contains("有限")||strList.get(i).contains("科技")
                                                ||strList.get(i).contains("製造")||strList.get(i).contains("工作室")){
                                            if(company==null) {
                                                if(strList.get(i).contains("公司")) {
                                                    endIndex = strList.get(i).lastIndexOf("公司") + 1;
                                                    company = strList.get(i).substring(0, endIndex + 1);
                                                }
                                                else{
                                                    company = strList.get(i);
                                                }
                                            }
                                        }
                                        if(strList.get(i).contains("陳")||strList.get(i).contains("林")||strList.get(i).contains("王")
                                                ||strList.get(i).contains("張")||strList.get(i).contains("李")
                                                ||strList.get(i).contains("吳")||strList.get(i).contains("謝")){
                                            if(name==null) {
                                                name = strList.get(i).substring(0, 3);
                                            }
                                        }
                                        if(strList.get(i).contains("電話")||strList.get(i).contains("TEL")){
                                            if(tel==null) {
                                                beginIndex = strList.get(i).indexOf("電話");
                                                tel = strList.get(i).substring(beginIndex+3);
                                                //                    tel=strList.get(i);
                                            }
                                        }
                                        if(strList.get(i).contains("傳")||strList.get(i).contains("真")||strList.get(i).contains("FAX")){
                                            if(fax==null) {
                                                //                    strList.get(i).charCodeAt(j) > 0x4E00 && strList.get(i).charCodeAt(j) < 0x9FA5
                                                beginIndex = strList.get(i).indexOf("真");
                                                fax = strList.get(i).substring(beginIndex+2);
                                                //                    fax=strList.get(i);
                                            }
                                        }
                                        if(strList.get(i).contains("09")){
                                            if(phone==null) {
                                                beginIndex=strList.get(i).indexOf("09");
                                                phone = strList.get(i).substring(beginIndex);
                                            }
                                        }
                                        if(strList.get(i).contains("台北")||strList.get(i).contains("桃園")||strList.get(i).contains("新竹")||strList.get(i).contains("苗栗")||strList.get(i).contains("台中")
                                                ||strList.get(i).contains("彰化")||strList.get(i).contains("雲林")||strList.get(i).contains("嘉義")||strList.get(i).contains("台南")||strList.get(i).contains("高雄")
                                                ||strList.get(i).contains("屏東")||strList.get(i).contains("南投")||strList.get(i).contains("宜蘭")||strList.get(i).contains("花蓮")||strList.get(i).contains("台東")){
                                            if(address==null) {
                                                if(strList.get(i).contains("地址")) {
                                                    beginIndex = strList.get(i).indexOf("地址");
                                                    address = strList.get(i).substring(beginIndex + 3);
                                                }
                                                else{
                                                    address = strList.get(i);
                                                }
                                            }
                                        }
                                        if(strList.get(i).contains("http")) {
                                            if(web==null) {
                                                beginIndex = strList.get(i).indexOf("http");
                                                endIndex = strList.get(i).lastIndexOf("com") + 2;
                                                web = strList.get(i).substring(beginIndex);
                                            }
                                        }
//                                try {
//                                    Thread.sleep(100);
//                                } catch (InterruptedException e) {
//                                    e.printStackTrace();
//                                }
                                    }
                                }
                            }
                        }
                        isFinish=true;
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });

            }
            while(true) {
                if (isFinish == true) {
                    return null;
                }
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            binding.progressBar.setVisibility(View.GONE);
            binding.progressBackground.setBackgroundColor(0);
            Intent intent = new Intent(MainActivity.this, classification.class);
//            intent.putStringArrayListExtra("info", strList);
            intent.putExtra("name", name);
            intent.putExtra("company", company);
            intent.putExtra("tel", tel);
            intent.putExtra("fax", fax);
            intent.putExtra("phone", phone);
            intent.putExtra("address", address);
            intent.putExtra("web", web);
            intent.putExtra("uri", intentUri);
            startActivity(intent);
        }
    }
}