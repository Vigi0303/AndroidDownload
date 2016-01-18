package com.example.vigi.androiddownload;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

// AsyncTask<Params, Progress, Result>
public class Async extends AsyncTask<String, Integer, String> {

    /* 用于查询数据库 */
    private DBHelper dbHelper;

    // 下载的文件的map，也可以是实体Bean 
    private HashMap<String, String> dataMap = null;
    private Context context;

    private boolean finished = false;
    private boolean paused = false;

    private int curSize = 0;

    private int length = 0;

    private Async startTask = null;
    private boolean isFirst = true;

    private String strUrl;

    @Override
    protected String doInBackground(String... Params) {

        dbHelper = new DBHelper(context);

        strUrl = Params[0];
        String name = dataMap.get("name");
        String appid = dataMap.get("appid");
        int startPosition = 0;

        URL url = null;
        HttpURLConnection httpURLConnection = null;
        InputStream inputStream = null;
        RandomAccessFile outputStream = null;
        // 文件保存路径 
        String path = Environment.getExternalStorageDirectory().getPath();
        // 文件名 
        String fileName = strUrl.substring(strUrl.lastIndexOf('/'));
        try {
            length = getContentLength(strUrl);
            startPosition = getDownloadedLength(strUrl, name);

            /** 判断是否是第一次启动任务，true则保存数据到数据库并下载， 
             *  false则更新数据库中的数据 start  
             */
            boolean isHas = false;
            for (String urlString : AppConstants.listUrl) {
                if (strUrl.equalsIgnoreCase(urlString)) {
                    isHas = true;
                    break;
                }
            }
            if (false == isHas) {
                saveDownloading(name, appid, strUrl, path, fileName, startPosition, length, 1);
            } else if (true == isHas) {
                updateDownloading(curSize, name, strUrl);
            }
            /** 判断是否是第一次启动任务，true则保存数据到数据库并下载， 
             *  false则更新数据库中的数据 end  
             */

            // 设置断点续传的开始位置 
            url = new URL(strUrl);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setAllowUserInteraction(true);
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setReadTimeout(5000);
            httpURLConnection.setRequestProperty("User-Agent", "NetFox");
            httpURLConnection.setRequestProperty("Range", "bytes=" + startPosition + "-");
            inputStream = httpURLConnection.getInputStream();

            File outFile = new File(path + fileName);
            // 使用java中的RandomAccessFile 对文件进行随机读写操作 
            outputStream = new RandomAccessFile(outFile, "rw");
            // 设置开始写文件的位置 
            outputStream.seek(startPosition);

            byte[] buf = new byte[1024 * 100];
            int read = 0;
            curSize = startPosition;
            while (false == finished) {
                while (true == paused) {
                    // 暂停下载 
                    Thread.sleep(500);
                }
                read = inputStream.read(buf);
                if (read == -1) {
                    break;
                }
                outputStream.write(buf, 0, read);
                curSize = curSize + read;
                // 当调用这个方法的时候会自动去调用onProgressUpdate方法，传递下载进度 
                publishProgress((int) (curSize * 100.0f / length));
                if (curSize == length) {
                    break;
                }
                Thread.sleep(500);
                updateDownloading(curSize, name, strUrl);
            }
            if (false == finished) {
                finished = true;
                deleteDownloading(strUrl, name);
            }
            inputStream.close();
            outputStream.close();
            httpURLConnection.disconnect();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            finished = true;
            deleteDownloading(strUrl, name);
            if (inputStream != null) {
                try {
                    inputStream.close();
                    if (outputStream != null) {
                        outputStream.close();
                    }
                    if (httpURLConnection != null) {
                        httpURLConnection.disconnect();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        // 这里的返回值将会被作为onPostExecute方法的传入参数 
        return strUrl;
    }

    /**
     * 暂停下载
     */
    public void pause() {
        paused = true;
    }

    /**
     * 继续下载
     */
    public void continued() {
        paused = false;
    }

    /**
     * 停止下载
     */
    @Override
    protected void onCancelled() {
        finished = true;
        deleteDownloading(dataMap.get("url"), dataMap.get("name"));
        super.onCancelled();
    }

    /**
     * 当一个下载任务成功下载完成的时候回来调用这个方法，
     * 这里的result参数就是doInBackground方法的返回值
     */
    @Override
    protected void onPostExecute(String result) {
        try {
            String name = dataMap.get("name");
            System.out.println("name====" + name);
            // 判断当前结束的这个任务在任务列表中是否还存在，如果存在就移除 
            if (AppConstants.mapTask.containsKey(result)) {
                if (AppConstants.mapTask.get(result) != null) {
                    finished = true;
                    deleteDownloading(result, name);
                }
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        super.onPostExecute(result);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    /**
     * 更新下载进度，当publishProgress方法被调用的时候就会自动来调用这个方法
     */
    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
    }


    /**
     * 获取要下载内容的长度
     *
     * @param urlString
     * @return
     */
    private int getContentLength(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            return connection.getContentLength();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 从数据库获取已经下载的长度
     *
     * @param url
     * @param name
     * @return
     */
    private int getDownloadedLength(String url, String name) {
        int downloadedLength = 0;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT downloadBytes FROM fileDownloading WHERE downloadUrl=? AND name=?";
        Cursor cursor = db.rawQuery(sql, new String[]{url, name});
        while (cursor.moveToNext()) {
            downloadedLength = cursor.getInt(0);
        }
        db.close();
        return downloadedLength;
    }

    /**
     * 保存下载的数据
     *
     * @param name
     * @param appid
     * @param url
     */
    private void saveDownloading(String name, String appid, String url, String savePath, String fileName, int downloadBytes, int totalBytes, int status) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            db.beginTransaction();
            String sql = "INSERT INTO fileDownloading(name, appid, downloadUrl, savePath, fileName, downloadBytes, totalBytes, downloadStatus) " +
                    "values(?,?,?,?,?,?,?,?)";
            db.execSQL(sql, new Object[]{name, appid, url, savePath, fileName, downloadBytes, totalBytes, status});
            db.setTransactionSuccessful();
            boolean isHas = false;
            // 判断当前要下载的这个连接是否已经正在进行，如果正在进行就阻止在此启动一个下载任务 
            for (String urlString : AppConstants.listUrl) {
                if (url.equalsIgnoreCase(urlString)) {
                    isHas = true;
                    break;
                }
            }
            if (false == isHas) {
                AppConstants.listUrl.add(url);
            }
            if (false == isFirst) {
                AppConstants.mapTask.put(url, startTask);
            }
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    /**
     * 更新下载数据
     *
     * @param cursize
     * @param name
     * @param url
     */
    private void updateDownloading(int cursize, String name, String url) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            db.beginTransaction();
            String sql = "UPDATE fileDownloading SET downloadBytes=? WHERE name=? AND downloadUrl=?";
            db.execSQL(sql, new String[]{cursize + "", name, url});
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    /**
     * 删除下载数据
     *
     * @param url
     * @param name
     */
    private void deleteDownloading(String url, String name) {
        if (true == finished) {
            // 删除保存的URL。这个listurl主要是为了在列表中按添加下载任务的顺序进行显示 
            for (int i = 0; i < AppConstants.listUrl.size(); i++) {
                if (url.equalsIgnoreCase(AppConstants.listUrl.get(i))) {
                    AppConstants.listUrl.remove(i);
                }
            }
            // 删除已经完成的下载任务 
            if (AppConstants.mapTask.containsKey(url)) {
                AppConstants.mapTask.remove(url);
            }
        }
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String sql = "DELETE FROM fileDownloading WHERE downloadUrl=? AND name=?";
        db.execSQL(sql, new Object[]{url, name});
        db.close();
    }

    public void setDataMap(HashMap<String, String> dataMap) {
        this.dataMap = dataMap;
    }

    public HashMap<String, String> getDataMap() {
        return dataMap;
    }

    public boolean isPaused() {
        return paused;
    }

    public int getCurSize() {
        return curSize;
    }

    public int getLength() {
        return length;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public Context getContext() {
        return context;
    }
} 