package com.example.vigi.androiddownload;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.util.IOUtils;
import com.example.vigi.androiddownload.core.LogHelper;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by Vigi on 2016/1/18.
 */
public class TaskAccessor {
    public static final int WAIT = 0;    // ready for download and wait lock
    public static final int DOWNLOADING = 1;
    public static final int ERROR = 3;
    public static final int FINISH = 4;
    public static final int PROCESSING = 5;
    public static final int IDLE = 6;  // initial status
    public static final int DISABLED = 7;   // disable status to keep it atomic

    public TaskAccessor(File infoJsonFile) {
        this(infoJsonFile, 0);
    }

    public TaskAccessor(File infoJsonFile, int taskId) {
        mInfoJsonFile = infoJsonFile;
        if (infoJsonFile.exists()) {
            FileReader reader = null;
            try {
                reader = new FileReader(infoJsonFile);
                StringBuilder sb = new StringBuilder();
                char[] buffer = new char[512];
                int len;
                while ((len = reader.read(buffer)) != -1) {
                    sb.append(buffer, 0, len);
                }
                info = JSON.parseObject(sb.toString(), TaskInfoObject.class);
            } catch (IOException e) {
                LogHelper.logError("read failed", e);
            } finally {
                IOUtils.close(reader);
            }
//        } else {
//            try {
//                infoJsonFile.createNewFile();
//            } catch (IOException e) {
//                Log.e("vigi", "create new file failed", e);
//            }
        }
        if (info == null) {
            info = new TaskInfoObject();
            if (taskId != 0) {
                info.id = taskId;
            }
        }
        if (info.isCompleted) {
            status = FINISH;
        } else {
            status = IDLE;
        }
    }

    private final File mInfoJsonFile;
    public TaskInfoObject info;

    public int status;

    public String statusToString() {
        switch (status) {
            case WAIT:
                return "WAIT";
            case DOWNLOADING:
                return "DOWNLOADING";
            case ERROR:
                return "ERROR";
            case FINISH:
                return "FINISH";
            case PROCESSING:
                return "PROCESSING";
            case IDLE:
                return "IDLE";
            case DISABLED:
                return "DISABLED";
        }
        return "";
    }

    public void copyFrom(TaskAccessor source) {
        if (source == this) {
            return;
        }
        status = source.status;
        info.copyFrom(source.info);
    }

    public boolean validateInfoJson() {
        return info.id != 0;
    }

    public boolean syncInfoFile() {
        FileWriter writer = null;
        try {
            if (!mInfoJsonFile.exists()) {
                // TODO: 2016/3/1  cannot create file after we delete at file explorer and why?
                mInfoJsonFile.createNewFile();
            }
            writer = new FileWriter(mInfoJsonFile, false);
            writer.write(JSON.toJSONString(info));
            return true;
        } catch (IOException e) {
            LogHelper.logError("write failed", e);
        } finally {
            IOUtils.close(writer);
        }
        return false;
    }

    public void deleteInfoFile() {
        if (mInfoJsonFile.exists()) {
            File parentFile = mInfoJsonFile.getParentFile();
            Utils.deleteQuietly(parentFile);
        }
    }
}
