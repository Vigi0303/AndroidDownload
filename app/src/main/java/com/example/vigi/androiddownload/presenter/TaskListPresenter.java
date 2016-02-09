package com.example.vigi.androiddownload.presenter;

import com.example.vigi.androiddownload.view.ITaskListView;

/**
 * Created by Vigi on 2016/2/6.
 */
public class TaskListPresenter {
    private ITaskListView mTaskListView;

    public TaskListPresenter(ITaskListView taskListView) {
        mTaskListView = taskListView;
    }

    public void requestTaskList() {
        mTaskListView.showLoading();
    }

    public void onDestroy() {
        mTaskListView = null;
    }
}
