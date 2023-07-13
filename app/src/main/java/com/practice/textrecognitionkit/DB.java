package com.practice.textrecognitionkit;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.Nullable;

import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.HashMap;

public class DB {
    SQLiteDatabase sqLiteDatabase;
    private final String DATABASE="contact.db";
    private final String TABLE="contactlist";
    String CreateTable="CREATE TABLE `contactlist` (`no` INTEGER NOT NULL PRIMARY KEY, `name` VARCHAR(45) NOT NULL, `company` VARCHAR(45), `tel` VARCHAR(45) , `fax` VARCHAR(45), `phone` VARCHAR(45) , `address` VARCHAR(45) , `web` VARCHAR(45), `img` BLOB);";

    public DB(Context ctx){
        DatabaseHelper helper = new DatabaseHelper(ctx, DATABASE, null, 1);

        sqLiteDatabase = helper.getReadableDatabase();
    }
    private class DatabaseHelper extends SQLiteOpenHelper{

        public DatabaseHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CreateTable);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }

    public Cursor getAll(){
        return sqLiteDatabase.rawQuery("SELECT * FROM contactlist", null);
    }

    public void addImage(int row_position, Bitmap bitmap){

        ContentValues cv = new ContentValues();
        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 80, os);
        cv.put("img", os.toByteArray());
        sqLiteDatabase.update(TABLE, cv, "no="+row_position, null);
    }

    public void addData(HashMap<String, Object> map, Bitmap bitmap){

        ContentValues cv = new ContentValues();
        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 80, os);
        int length=os.toByteArray().length;
        cv.put("img", os.toByteArray());
        cv.put("no", map.get("no").toString());
        cv.put("name", map.get("name").toString());
        cv.put("company", map.get("company").toString());
        cv.put("tel", map.get("tel").toString());
        cv.put("fax", map.get("fax").toString());
        cv.put("phone", map.get("phone").toString());
        cv.put("address", map.get("address").toString());
        cv.put("web", map.get("web").toString());
        sqLiteDatabase.insert(TABLE, null, cv);
    }

    public void updateData(int row_position, String TitleStr, String ContentStr){
        ContentValues cv = new ContentValues();
        cv.put(TitleStr, ContentStr);
        sqLiteDatabase.update(TABLE, cv, "no="+row_position, null);
    }

    public void deleteData(int row_position){
        sqLiteDatabase.delete(TABLE, "no="+row_position, null);
        String upgrade_no="update "+TABLE+" SET no=no-1 where no>"+row_position;
        sqLiteDatabase.execSQL(upgrade_no);
    }
}


