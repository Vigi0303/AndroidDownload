package com.example.vigi.androiddownload;

import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.util.IOUtils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by Vigi on 2016/1/18.
 */
public class TaskAccessor {
    public static final int WAIT = 0;
    public static final int DOWNLOADING = 1;
    public static final int PAUSE = 2;
    public static final int ERROR = 3;
    public static final int FINISH = 4;
    public static final int PROCESSING = 5;   // disable status to keep it atomic

    public TaskAccessor(File infoJsonFile) {
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
                Log.e("vigi", "read failed", e);
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
        }
        status = WAIT;
    }

    private final File mInfoJsonFile;
    public TaskInfoObject info;

    public int status;

    public boolean syncInfoFile() {
        FileWriter writer = null;
        try {
            writer = new FileWriter(mInfoJsonFile, false);
            writer.write(JSON.toJSONString(info));
            return true;
        } catch (IOException e) {
            Log.e("vigi", "write failed", e);
        } finally {
            IOUtils.close(writer);
        }
        return false;
    }
}
