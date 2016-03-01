package com.example.vigi.androiddownload;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
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
public class TaskListActivity extends AppCompatActivity implements ITaskListView {
    @Bind(R.id.list_view)
    RecyclerView mRecyclerView;

    @Bind(R.id.loading_view)
    ProgressBar mLoadingView;

    @Bind(R.id.empty_view)
    TextView mEmptyView;

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
        ItemTouchHelper.Callback callback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                TaskAccessor taskToRemove = mAccessorList.get(position);
                // remove on UI
                mAccessorList.remove(position);
                mListAdapter.notifyItemRemoved(position);
                // remove on Data(process in background)
                Bundle extra = new Bundle();
                extra.putInt(DownloadService.BUNDLE_ACTION, DownloadService.ACTION_DELETE_TASK);
                extra.putInt(DownloadService.BUNDLE_TASK_ID, taskToRemove.info.id);
                startService(new Intent(TaskListActivity.this, DownloadService.class).putExtras(extra));
            }
        };

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mListAdapter);
        mRecyclerView.addItemDecoration(new DefaultItemDecoration());
        new ItemTouchHelper(callback).attachToRecyclerView(mRecyclerView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getInstance().register(this);
        mPresenter.requestTaskList();
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
        refreshTask(event.taskId);
    }

    @Subscribe
    public void onReadLengthEvent(DownloadEvent.ReadLength event) {
        refreshTask(event.taskId);
    }

    @Subscribe
    public void onLoadingEvent(DownloadEvent.Loading event) {
        refreshTask(event.taskId);
    }

    @Subscribe
    public void onFinishEvent(DownloadEvent.Finish event) {
        refreshTask(event.taskId);
    }

    @Subscribe
    public void onCanceledEvent(DownloadEvent.Canceled event) {
        refreshTask(event.taskId);
    }

    private void refreshTask(int taskId) {
        TaskAccessor task = findTaskById(taskId);
        if (task != null) {
            task.copyFrom(TasksHolder.getInstance().getAccessor(taskId));
            mListAdapter.notifyItemChanged(mAccessorList.indexOf(task));
        }
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

    private TaskAccessor findTaskById(int taskId) {
        for (int i = 0; i < mAccessorList.size(); ++i) {
            TaskAccessor task = mAccessorList.get(i);
            if (task.info.id == taskId) {
                return task;
            }
        }
        return null;
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
            if (taskAccessor.status == TaskAccessor.PROCESSING) {
                holder.mItemProgress.setIndeterminate(true);
                holder.itemView.setClickable(true);
            } else if (taskAccessor.status == TaskAccessor.DISABLED) {
                holder.mItemProgress.setIndeterminate(true);
                holder.itemView.setClickable(false);
            } else {
                holder.mItemProgress.setIndeterminate(false);
                holder.itemView.setClickable(true);
                if (taskAccessor.info.totalSize != 0) {
                    holder.mItemProgress.setProgress((int) (100 * taskAccessor.info.downloadedSize / taskAccessor.info.totalSize));
                } else {
                    holder.mItemProgress.setProgress(0);
                }
            }
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
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            TaskAccessor task = mAccessorList.get(position);
            if (task.status == TaskAccessor.DOWNLOADING
                    || task.status == TaskAccessor.WAIT
                    || task.status == TaskAccessor.PROCESSING) {
                TasksHolder.getInstance().cancel(task.info.id);
                mListAdapter.notifyItemChanged(position);
            } else if (task.status == TaskAccessor.FINISH || task.status == TaskAccessor.DISABLED) {
                // do nothing
            } else {
                Bundle extra = new Bundle();
                extra.putInt(DownloadService.BUNDLE_ACTION, DownloadService.ACTION_RESUME_TASK);
                extra.putInt(DownloadService.BUNDLE_TASK_ID, task.info.id);
                startService(new Intent(TaskListActivity.this, DownloadService.class).putExtras(extra));
            }
        }
    }

    class DefaultItemDecoration extends RecyclerView.ItemDecoration {
        private Drawable mDividerDrawable;

        public DefaultItemDecoration() {
            mDividerDrawable = ResourcesCompat.getDrawable(getResources(), R.color.colorPrimary, getTheme());
        }

        @Override
        public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
            int count = parent.getChildCount();
            for (int i = 0; i < count; ++i) {
                View child = parent.getChildAt(i);
                mDividerDrawable.setBounds(0, child.getBottom(), parent.getWidth(), child.getBottom() + 2);
                mDividerDrawable.draw(c);
            }
        }
    }
}
