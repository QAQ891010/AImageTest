package com.example.imagetest;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

public class MainActivity extends Activity {

    private ImageView ivImg;
    private Button btnDownload;
    private Bitmap img;
    private ImageHandler imgHandler = new ImageHandler();
    //测试Url
    private String url = "http://47.93.51.80:8090/familyCare/api/fileDownload";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ivImg = (ImageView) findViewById(R.id.ivImg);
        btnDownload = (Button) findViewById(R.id.btnDownload);
        //点击按钮开始下载
        btnDownload.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadImg();
            }
        });
    }

    /**
     * 异步从服务器加载图片数据
     */
    private void downloadImg(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Bitmap img =  getImg();
                Message msg = imgHandler.obtainMessage();
                msg.what = 0;
                msg.obj = img;
                imgHandler.sendMessage(msg);
            }
        }).start();
    }

    /**
     * 异步线程请求到的图片数据，利用Handler，在主线程中显示
     */
    class ImageHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case 0:
                    img = (Bitmap)msg.obj;
                    if(img != null){
                        ivImg.setImageBitmap(img);
                    }
                    break;

                default:
                    break;
            }
        }
    }

    /**
     * 从服务器读取图片流数据，并转换为Bitmap格式
     * @return Bitmap
     */
    private Bitmap getImg(){
        Bitmap img = null;

        try {
            URL imgUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) imgUrl.openConnection();

            conn.setRequestMethod("POST");
            conn.setConnectTimeout(1000 * 6);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setUseCaches(false);

            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Charset", "UTF-8");
            conn.connect();

            //输出流写参数
            DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
            String param = getParam();
            dos.writeBytes(param);
            dos.flush();
            dos.close();

            int resultCode = conn.getResponseCode();

            if(HttpURLConnection.HTTP_OK == resultCode){
                InputStream is = conn.getInputStream();
                img = BitmapFactory.decodeStream(is);
                is.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return img;
    }

    /**
     * 测试参数
     * @return
     */
    private String getParam(){
        JSONObject jsObj = new JSONObject();
        try {
            jsObj.put("picFormat", "jpg");
            jsObj.put("testParam", "9527");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsObj.toString();
    }
}
