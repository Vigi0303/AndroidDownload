package com.example.vigi.androiddownload;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Vigi on 2016/1/18.
 */
public class MainActivity extends Activity {
    private static final String[] URLS = {
            "http://vplay.aixifan.com/des/20160113/3080405_mp4/3080405_720p.mp4?k=318eb25f0a1c7cd22ccd5e0b7e8689e1&t=1452764991"
            , "http://down.mumayi.com/41052/mbaidu"
    };

    @Bind(R.id.download_url)
    TextView mUrlText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mUrlText.setText(URLS[0]);
    }

    @OnClick(R.id.add_to_download)
    void onAddClick() {
        String url = mUrlText.getText().toString();
        // TODO: 2016/1/18 add to download
    }

    @OnClick(R.id.show_list)
    void onShowListClick() {
        startActivity(new Intent(this, DownloadManagerActivity.class));
    }
}
