package com.example.vigi.androiddownload;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.vigi.androiddownload.presenter.TaskListPresenter;
import com.example.vigi.androiddownload.view.ITaskListView;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Vigi on 2016/1/15.
 */
public class TaskListActivity extends Activity implements ITaskListView {
    @Bind(R.id.list_view)
    RecyclerView mRecyclerView;

    @Bind(R.id.loading_view)
    ProgressBar mLoadingView;

    @Bind(R.id.empty_view)
    ProgressBar mEmptyView;

    TaskListPresenter mPresenter;
    DownloadListAdapter mListAdapter;
    List<TaskAccessor> mAccessorList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_manager);
        ButterKnife.bind(this);

        mPresenter = new TaskListPresenter(this);
        mListAdapter = new DownloadListAdapter();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mListAdapter);

        mPresenter.requestTaskList();
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getInstance().register(this);
    }

    @Override
    protected void onPause() {
        EventBus.getInstance().unregister(this);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mPresenter.onDestroy();
        super.onDestroy();
    }

    @Subscribe
    public void onDisPatchedEvent(DownloadEvent.DisPatched event) {

    }

    @Subscribe
    public void onReadLengthEvent(DownloadEvent.ReadLength event) {

    }

    @Subscribe
    public void onLoadingEvent(DownloadEvent.Loading event) {

    }

    @Subscribe
    public void onFinishEvent(DownloadEvent.Finish event) {

    }

    @Subscribe
    public void onCanceledEvent(DownloadEvent.Canceled event) {

    }

    @Override
    public void showLoading() {
        mRecyclerView.setVisibility(View.GONE);
        mLoadingView.setVisibility(View.VISIBLE);
        mEmptyView.setVisibility(View.GONE);
    }

    @Override
    public void showEmpty() {
        mRecyclerView.setVisibility(View.GONE);
        mLoadingView.setVisibility(View.GONE);
        mEmptyView.setVisibility(View.VISIBLE);
    }

    @Override
    public void showContent(List<TaskAccessor> accessorList) {
        mRecyclerView.setVisibility(View.VISIBLE);
        mLoadingView.setVisibility(View.GONE);
        mEmptyView.setVisibility(View.GONE);

        mAccessorList.clear();
        mAccessorList.addAll(accessorList);
        mListAdapter.notifyDataSetChanged();
    }

    class DownloadListAdapter extends RecyclerView.Adapter<DownloadItemHolder> {

        @Override
        public DownloadItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new DownloadItemHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_download, parent, false));
        }

        @Override
        public void onBindViewHolder(DownloadItemHolder holder, int position) {
            TaskAccessor taskAccessor = mAccessorList.get(position);
            holder.mItemTitle.setText(taskAccessor.info.title);
            holder.mItemProgress.setProgress((int) (100 * taskAccessor.info.downloadedSize / taskAccessor.info.totalSize));
            holder.mItemStatus.setText(taskAccessor.statusToString());
            holder.mItemSpeed.setText(""); // TODO: 2016/2/8 add speed support
        }

        @Override
        public int getItemCount() {
            return mAccessorList.size();
        }
    }

    class DownloadItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @Bind(R.id.item_title)
        TextView mItemTitle;

        @Bind(R.id.item_progress)
        ProgressBar mItemProgress;

        @Bind(R.id.item_status)
        TextView mItemStatus;

        @Bind(R.id.item_download_speed)
        TextView mItemSpeed;

        public DownloadItemHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            TaskAccessor task = mAccessorList.get(getAdapterPosition());

        }
    }
}
