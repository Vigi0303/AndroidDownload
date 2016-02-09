package com.example.vigi.androiddownload.view;

import com.example.vigi.androiddownload.TaskAccessor;

import java.util.List;

/**
 * Created by Vigi on 2016/2/6.
 */
public interface ITaskListView {
    void showLoading();

    void showEmpty();

    void showContent(List<TaskAccessor> accessorList);

}
