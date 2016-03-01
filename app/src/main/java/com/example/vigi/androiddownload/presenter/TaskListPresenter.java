package com.example.vigi.androiddownload.presenter;

import com.example.vigi.androiddownload.TaskAccessor;
import com.example.vigi.androiddownload.TasksHolder;
import com.example.vigi.androiddownload.view.ITaskListView;

import java.util.List;

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
        List<TaskAccessor> allTask = TasksHolder.getInstance().getAllTaskAccessor();
        if (allTask.isEmpty()) {
            mTaskListView.showEmpty();
        } else {
            mTaskListView.showContent(allTask);
        }
    }

    public void onDestroy() {
        mTaskListView = null;
    }
}
