package com.practice.textrecognitionkit;

import static androidx.core.content.ContextCompat.startActivity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.practice.textrecognitionkit.databinding.ActivityMainBinding;
import com.practice.textrecognitionkit.databinding.ItemBinding;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class RecycleAdapter extends RecyclerView.Adapter<RecycleAdapter.ViewHolder>{
    private LinkedList<HashMap<String,Object>> mList;
    private LinkedList<byte[]> mStreamList;
    private Context mContext;
    private RecyclerView recyclerView;
    private boolean mIsDeleting = false;

    //內部類別ViewHolder
    class ViewHolder extends RecyclerView.ViewHolder{
        ImageView iv_card;
        TextView tv_no, tv_name, tv_company;
        ImageButton ib_delete;
        public View mItemView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_no=itemView.findViewById(R.id.tv_no);
            iv_card=itemView.findViewById(R.id.iv_card);
            tv_name=itemView.findViewById(R.id.tv_name);
            tv_company=itemView.findViewById(R.id.tv_company);
//            ib_delete=itemView.findViewById(R.id.ib_delete);
            mItemView=itemView;
            getItemView(itemView);
        }
        public View getItemView(View itemView){
            mItemView=itemView;
            return mItemView;
        }
    }

    public RecycleAdapter(LinkedList<HashMap<String,Object>> data, LinkedList<byte[]> streamList, Context context, RecyclerView recyclerView){
        mList=data;
        mStreamList=streamList;
        mContext=context;
//        recyclerViewAction(RecycleAdapter.this, mList, mStreamList, recyclerView);
    }
    @NonNull
    @Override
    public RecycleAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecycleAdapter.ViewHolder holder, int position) {
        holder.tv_no.setText(mList.get(position).get("no").toString());
        Bitmap bitmap=null;
        if(mStreamList.get(position)!=null&&mStreamList.get(position).length>0 ) {
            bitmap = BitmapFactory.decodeByteArray(mStreamList.get(position), 0, mStreamList.get(position).length);
            holder.iv_card.setImageBitmap(bitmap);
        }
        //點擊recyclerview
        holder.tv_name.setText(mList.get(position).get("name").toString());
        holder.tv_company.setText(mList.get(position).get("company").toString());

        holder.itemView.setOnClickListener(v -> {
            Intent intent=new Intent(mContext, InfoActivity.class);
            intent.putExtra("position", position);
            mContext.startActivity(intent);
        });
        //長按刪除(用右滑刪除取代 暫時取消)
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                onItemClickListener.onItemLongClick(holder.itemView, holder.getAdapterPosition());
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public interface OnItemClickListener{
        void onItemLongClick(View view , int pos);
    }

    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    // 處理右滑手勢事件
    ItemTouchHelper itemTouchHelper=new ItemTouchHelper(new ItemTouchHelper.Callback() {
        @Override
        public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            return makeMovementFlags(0, ItemTouchHelper.LEFT );
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            int position=viewHolder.getAdapterPosition();
            Toast.makeText(mContext, "NO." + (position+1), Toast.LENGTH_SHORT).show();
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            int position = viewHolder.getAdapterPosition();
            switch(direction){
                case ItemTouchHelper.LEFT:
                    AlertDialog.Builder dialog=new AlertDialog.Builder(mContext);
                    dialog.setTitle("是否要永久刪除")
                            .setIcon(R.drawable.ic_baseline_delete_forever_24)
                            .setCancelable(false)
                            .setPositiveButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ((MainActivity)mContext).initDataBase();
                                    dialog.dismiss();
                                }
                            })
                            .setNegativeButton("確定", new DialogInterface.OnClickListener(){

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Toast.makeText(mContext, "已刪除", Toast.LENGTH_SHORT).show();
                                    DB db=new DB(mContext);
                                    db.deleteData(position+1);
                                    ((MainActivity)mContext).initDataBase();
                                }
                            }).show();
                    break;
            }
        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            new RecyclerViewSwipeDecorator.Builder(c,recyclerView,viewHolder,dX,dY,actionState,isCurrentlyActive)
                    .addActionIcon(R.drawable.ic_menu_delete)
                    .addBackgroundColor(ContextCompat.getColor(mContext,android.R.color.holo_red_dark))
                    .create()
                    .decorate();
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }

        @Override
        public void onChildDrawOver(@NonNull Canvas c, @NonNull RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            super.onChildDrawOver(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
    });
}
