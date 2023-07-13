package com.practice.textrecognitionkit;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import java.util.ArrayList;
import java.util.HashMap;

public class ListViewAdapter extends BaseAdapter {
    private Context mContext;
    private DB db;
    private Cursor cursor;
    private String[] title=new String[]{"姓名", "公司", "電話", "傳真", "手機", "地址", "網站"};
    private int[] imgId=new int[]{0, 0, R.drawable.ic_baseline_call_24, 0, R.drawable.ic_baseline_call_24,
            R.drawable.ic_baseline_location_on_24, R.drawable.ic_baseline_corporate_fare_24};
    private ArrayList<ArrayList<Object>> mData;
    private static class ViewHolder{
        private TextView title, content;
        private ImageView icon;
    }

    public ListViewAdapter(Context context, DB db){
        this.mContext=context;
        this.db=db;
        this.cursor=db.getAll();
        cursor.moveToPosition(InfoActivity.row_position);
    }

    @Override
    public int getCount() {
        return cursor.getColumnCount()-2;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if(convertView==null){
            //LayoutInflater 是一個Android類，用於將布局XML文件轉換為Java對象，也就是將XML文件中描述的佈局層次轉換為視圖層次，從而創建一個視圖元素。
            LayoutInflater layoutInflater=LayoutInflater.from(mContext);
            //使用 inflate() 方法將佈局文件（R.layout.info_item）轉換為一個視圖對象，即 View。
            convertView = layoutInflater.inflate(R.layout.info_item, parent, false);
            viewHolder=new ViewHolder();
            viewHolder.title=convertView.findViewById(R.id.tvTitle_info);
            viewHolder.content=convertView.findViewById(R.id.tvContent_info);
            viewHolder.icon=convertView.findViewById(R.id.ivIcon_info);
            // convertView.setTag(holder) 的作用是將 ViewHolder 對象 holder 設置到 convertView 的標籤中，以便在後續使用時可以方便地獲取到 ViewHolder 對象。
            // 即是保存holder的參考到convertView的標籤中
            // 這裡的標籤是一個 Object 型別的變量，可以存儲任何對象。通過這種方式，我們可以將一個向上轉型成 Object 的 ViewHolder 存儲到 convertView 中。
            convertView.setTag(viewHolder);
        }
        else{
            viewHolder=(ViewHolder) convertView.getTag();
        }

        cursor= db.getAll();
        cursor.moveToPosition(InfoActivity.row_position);

        viewHolder.title.setText(title[position]);
        viewHolder.content.setText(cursor.getString(position+1));
        viewHolder.icon.setImageResource(imgId[position]);

        viewHolder.content.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext, "content", Toast.LENGTH_SHORT).show();
                EditeContent(position);
            }
        });

        viewHolder.icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch(imgId[position]){
                    case 0:
                        EditeContent(position);
                        break;
                    case R.drawable.ic_baseline_call_24:
                        Toast.makeText(mContext, "call", Toast.LENGTH_SHORT).show();
                        if(viewHolder.content.getText().toString()!=null && !viewHolder.content.getText().toString().equals("")) {
                            String phoneNumber = viewHolder.content.getText().toString();  // 設定電話號碼
                            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNumber));
                            mContext.startActivity(intent);
                        }
                        break;
                    case R.drawable.ic_baseline_location_on_24:
                        Toast.makeText(mContext, "location", Toast.LENGTH_SHORT).show();
                        if(viewHolder.content.getText().toString()!=null && !viewHolder.content.getText().toString().equals("")) {
                            Uri uri = Uri.parse("geo:0,0?q=" + viewHolder.content.getText().toString());
                            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                            mContext.startActivity(intent);
                        }
                        break;
                    case R.drawable.ic_baseline_corporate_fare_24:
                        Toast.makeText(mContext, "corporation", Toast.LENGTH_SHORT).show();
                        if(viewHolder.content.getText().toString()!=null && !viewHolder.content.getText().toString().equals("")) {
                            Uri uri=Uri.parse(viewHolder.content.getText().toString());
                            Intent intent=new Intent(Intent.ACTION_VIEW, uri);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.setPackage(null);
                                mContext.startActivity(Intent.createChooser(intent, "Choose Browser"));
                        }
                        break;

                }
            }
        });


        return convertView;
    }

    private void EditeContent(int position){
        EditText etEdit = new EditText(mContext);
        etEdit.setSingleLine();
//        Cursor cursor = db.getAll();
        cursor.moveToPosition(InfoActivity.row_position);
        int col_index = cursor.getColumnIndex(cursor.getColumnName(position + 1));
        etEdit.setText(cursor.getString(col_index));
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("修改" + title[position]);
        builder.setView(etEdit);
        builder.setPositiveButton("修改", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String ContentStr = etEdit.getText().toString();
                db.updateData(InfoActivity.row_position + 1, cursor.getColumnName(position + 1), ContentStr);
                cursor.moveToPosition(InfoActivity.row_position);
                notifyDataSetChanged();
            }
        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();

    }
}
