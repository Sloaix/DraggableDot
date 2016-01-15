package com.lsxiao.draggablereddot.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.lsxiao.draggablereddot.Model;
import com.lsxiao.draggablereddot.R;
import com.lsxiao.draggablereddot.base.BaseActivity;
import com.lsxiao.library.DotView;

import java.util.ArrayList;
import java.util.List;

/**
 * author:lsxiao
 * date:2016/01/04 18:29
 */
public class ListActivity extends BaseActivity {
    private RecyclerView mRecyclerView;
    private SimpleAdapter mSimpleAdapter;

    public static void start(Activity activity) {
        final Intent intent = new Intent(activity, ListActivity.class);
        activity.startActivity(intent);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_list;
    }

    @Override
    protected void afterCreate(Bundle savedInstanceState) {
        List<Model> modelList = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            Model model = new Model(false);
            modelList.add(model);
        }
        mSimpleAdapter = new SimpleAdapter(modelList);

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mSimpleAdapter);
    }

    class SimpleAdapter extends RecyclerView.Adapter<SimpleAdapter.SimpleViewHolder> implements DotView.onDotStateChangedListener {
        private List<Model> mModelList;

        public SimpleAdapter(List<Model> modelList) {
            mModelList = modelList;
        }

        @Override
        public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(ListActivity.this).inflate(R.layout.list_item, parent, false);
            return new SimpleViewHolder(view);
        }

        @Override
        public void onBindViewHolder(SimpleViewHolder holder, int position) {
            holder.mDotView.setOnDotStateChangedListener(this);
            holder.mDotView.setTag(position);
            holder.mDotView.setVisibility(mModelList.get(position).isRead() ? View.INVISIBLE : View.VISIBLE);
        }

        @Override
        public int getItemCount() {
            return mModelList.size();
        }

        class SimpleViewHolder extends RecyclerView.ViewHolder {
            DotView mDotView;

            public SimpleViewHolder(View itemView) {
                super(itemView);
                mDotView = (DotView) itemView.findViewById(R.id.dot);
            }
        }

        @Override
        public void onStretch(DotView dotView) {
            final int position = (int) dotView.getTag();
            Log.d("xls", "onStretch " + position);
            Toast.makeText(ListActivity.this, "onStretch " + position, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onDrag(DotView dotView) {
            final int position = (int) dotView.getTag();
            Log.d("xls", "onDrag " + position);
            Toast.makeText(ListActivity.this, "onDrag " + position, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onDismissed(DotView dotView) {
            final int position = (int) dotView.getTag();
            mModelList.get(position).setRead(true);
            Log.d("xls", "onDismissed " + position);
            Toast.makeText(ListActivity.this, "onDismissed " + position, Toast.LENGTH_SHORT).show();
        }
    }
}
