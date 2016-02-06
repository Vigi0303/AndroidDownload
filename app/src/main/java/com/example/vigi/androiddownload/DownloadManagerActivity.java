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

import com.squareup.otto.Subscribe;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Vigi on 2016/1/15.
 */
public class DownloadManagerActivity extends Activity {
    @Bind(R.id.list_view)
    RecyclerView mRecyclerView;

    DownloadListAdapter mListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_manager);
        ButterKnife.bind(this);

        mListAdapter = new DownloadListAdapter();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mListAdapter);
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


    class DownloadListAdapter extends RecyclerView.Adapter<DownloadItemHolder> {
        @Override
        public DownloadItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new DownloadItemHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_download, parent, false));
        }

        @Override
        public void onBindViewHolder(DownloadItemHolder holder, int position) {

        }

        @Override
        public int getItemCount() {
            return 0;
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

        TaskAccessor mTaskAccessor = null;

        public DownloadItemHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

        }
    }
}
