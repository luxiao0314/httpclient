package com.haibin.httpnetproject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.haibin.http.HttpNetClient;
import com.haibin.http.builder.Headers;
import com.haibin.http.builder.Request;
import com.haibin.http.builder.RequestParams;
import com.haibin.http.core.Response;
import com.haibin.http.core.call.Call;
import com.haibin.http.core.call.Callback;
import com.haibin.http.core.call.InterceptListener;
import com.haibin.http.core.interceptor.LoggerInterceptor;
import com.haibin.http.core.io.JsonContent;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    private ProgressBar progressBar;
    private ImageView iv;
    private TextView text;
    private Handler handler = new Handler();
    private Call callExe;

    private Call callDownload;
    HttpNetClient client = new HttpNetClient();
    {
        client.newBuilder().addInterceptor(new LoggerInterceptor()).build();
    }

    public static void show(Activity activity) {
        Intent intent = new Intent(activity, MainActivity.class);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        iv = (ImageView) findViewById(R.id.iv);
        text = (TextView) findViewById(R.id.text);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_execute:
                upload();
                break;
            case R.id.btn_execute1:
                httpGetCommon();
                break;
            case R.id.btn_execute11:
                httpGet();
                break;
            case R.id.btn_execute111:
                httpPostJson();
                break;
            case R.id.btn_cancel:
                if (callExe != null) {
                    callExe.cancel();
                }
                if (callDownload != null) {
                    callDownload.cancel();
                }
                break;
            case R.id.btn_download:
                //rxDownload();
                //rangeDownload();
                rangeDownload();
                break;
        }
    }

    /**
     * RxJava方式断点下载
     */
    private void rxRangeDownload() {
        final File rangeFile = new File(Environment.getExternalStorageDirectory().getPath() + "/cnblogs.apk");
        final long readySize = rangeFile.exists() ? rangeFile.length() : 0;

        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(final ObservableEmitter<String> e) throws Exception {

                        client.newCall(new Request.Builder()
                                .url("http://f1.market.xiaomi.com/download/AppStore/0117653278abecee8762883a940e129e9d242ae7d/com.huanghaibin_dev.cnblogs.apk")
                                .headers(new Headers.Builder().addHeader("Range", "bytes=" + readySize + "-"))
                                .build())
                        .intercept(new InterceptListener() {
                            @Override
                            public void onProgress(final int index, final long currentLength, final long totalLength) {
                                Log.e("当前进度", "  --  " + ((float) currentLength / totalLength) * 100);
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        text.setText("第 " + (index + 1) + " 个文件上传进度" + ((float) currentLength / totalLength) * 100);
                                    }
                                });
                            }
                        })
                        .enqueue(new Callback() {
                            @Override
                            public void onResponse(Response response) {
                                try {
                                    InputStream is = response.toStream();
                                    RandomAccessFile randomAccessFile = new RandomAccessFile(rangeFile, "rw");
                                    randomAccessFile.seek(readySize);
                                    int length = response.getContentLength();
                                    length += readySize;
                                    int p = (int) readySize;
                                    int bytes;
                                    byte[] buffer = new byte[1024];
                                    while ((bytes = is.read(buffer)) != -1) {
                                        randomAccessFile.write(buffer, 0, bytes);
                                        p += bytes;
                                        e.onNext(String.valueOf((p / (float) length) * 100));
                                    }
                                    response.close();
                                } catch (Exception error) {
                                    error.printStackTrace();
                                    e.onError(error);
                                }
                            }

                            @Override
                            public void onFailure(Exception e) {
                                Log.e("onFailure", e.getMessage());
                            }
                        });

            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(String value) {
                        text.setText(value);
                        float f = Float.parseFloat(value);
                        progressBar.setProgress((int) f);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    /**
     * 断点下载案例
     */
    private void rangeDownload() {
        final File rangeFile = new File(Environment.getExternalStorageDirectory().getPath() + "/cnblogs.apk");
        final long readySize = rangeFile.exists() ? rangeFile.length() : 0;
        Log.e("已经下载长度--- ", "  --   " + readySize);
        Headers.Builder headers = new Headers.Builder()
                .addHeader("Range", "bytes=" + readySize + "-");

        Request request = new Request.Builder()
                .url("http://f1.market.xiaomi.com/download/AppStore/0117653278abecee8762883a940e129e9d242ae7d/com.huanghaibin_dev.cnblogs.apk")
                .headers(headers)
                .build();
        callDownload = client.newCall(request);
        callDownload.enqueue(new Callback() {
            @Override
            public void onResponse(Response response) {
                try {
                    InputStream is = response.toStream();
                    RandomAccessFile randomAccessFile = new RandomAccessFile(rangeFile, "rw");
                    randomAccessFile.seek(readySize);
                    int length = response.getContentLength();
                    Log.e("服务器数据长度为--- ", "  --   " + length);
                    length += readySize;
                    int p = (int) readySize;
                    int bytes;
                    byte[] buffer = new byte[1024];
                    while ((bytes = is.read(buffer)) != -1) {
                        randomAccessFile.write(buffer, 0, bytes);
                        p += bytes;
                        Log.e("下载进度：", String.valueOf((p / (float) length) * 100));
                    }
                    response.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Exception e) {

            }
        });
    }

    private void rxExecute() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Request request = new Request.Builder()
                        .url("http://v2.api.dmzj.com/old/comment/0/0/33461/0.json")
                        .method("GET")
                        .build();
                try {
                    client.newCall(request)
                            .execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void httpPostJson() {

        Headers.Builder header = new Headers.Builder()
                .addHeader("Cookie", "_ga=GA1.2.2128538109.1473746167; pgv_pvi=9544373248; SERVERID=d0849c852e6ab8cf0cebe3fa386ea513|1477908232|1477908230")
                .addHeader("X-Requested-With", "XMLHttpRequest")
                .addHeader("Content-Type", "application/json; charset=UTF-8");

        Request request = new Request.Builder()
                .url("https://passport.cnblogs.com/user/signin")
                .content(new JsonContent("{\"input1\":\"kV/jhKdvBcnLjutqHSIG2KbTchONKXgGX3EgSKKo6z1Xb0Jk6x04+4xeu1VPONYJTazlUTuxpsyNSOAv08uOlxBDcO988rMDO/6b/B5Ozjw9WJqK73h3FnY4kbz0qCT9jCyC0X1mSNoRLh88yE1XtiQoJSLvAWfX/PZ3zYTmAT4=\",\"input2\":\"DTQ2iCphxVXZ/gYT6SMeJ0w/NaH/U+zWb9CRX8CbriAr3GLGp+StPSZw+cWK01DktC/b7XlZasQvji9NCOWYcz2Z3ppSnXmSJbKqgwHHOqI2Pezte15A/qx5Qfr3KlwdbE8Pr6v2uZSLOo/lLrurxxoQnRsyTjeQz1z8sgfqhms=\",\"remember\":false}"))
                .method("POST")
                .headers(header)
                .build();
        client.newCall(request)
                .enqueue(new Callback() {
                    @Override
                    public void onResponse(Response response) {
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.e("onFailure", e.getMessage());
                    }
                });
    }

    public void upload() {
        Request request = new Request.Builder()
                .url("http://upload.cnblogs.com/ImageUploader/TemporaryAvatarUpload")
                .method("POST")
                .params(new RequestParams().putFile("qqfile", "/storage/emulated/0/DCIM/Camera/IMG_20160909_080844.jpg"))
                .headers(new Headers.Builder().addHeader("Cookie", "pgv_pvi=9544373248; .CNBlogsCookie=CA5152A644BF0710FB4CFFE2D1634FEE921CB1201E01962ACCEAEE2417BC6AE649E30F5C6DD63FC40ED6B064E4709B1656F8273AE2050DE1FAC47CE884FDFE6D430BAA80271DF15ADAD159FCDF0F37C7AC3B987FFA9ED210939E0650C08D42F84C0FC029; _ga=GA1.2.2128538109.1473746167; _gat=1"))
                .build();
        client.newCall(request)
                .intercept(new InterceptListener() {
                    @Override
                    public void onProgress(final int index, final long currentLength, final long totalLength) {
                        Log.e("当前进度", "  --  " + ((float) currentLength / totalLength) * 100);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                text.setText("第 " + (index + 1) + " 个文件上传进度" + ((float) currentLength / totalLength) * 100);
                            }
                        });
                    }
                })
                .enqueue(new Callback() {
                    @Override
                    public void onResponse(Response response) {
                        Log.e("res", response.getBody());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, "上传完成", Toast.LENGTH_LONG).show();
                            }
                        });
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.e("onFailure", " onFailure " + e.getMessage());
                    }
                });
    }

    public void httpGet() {
        Request request = new Request.Builder()
                .url("http://v2.api.dmzj.com/old/comment/0/0/33461/0.json")
                .method("GET")
                .build();
        client.newCall(request)
                .enqueue(new Callback() {
                    @Override
                    public void onResponse(Response response) {
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.e("onFailure", " onFailure " + e.getMessage());
                    }
                });
    }

    public void httpGetCommon() {
        try {
            Response response = client.newCall("http://img.pconline.com.cn/images/upload/upc/tx/wallpaper/1306/27/c4/22626360_1372304637240_800x800.jpg").execute();
            Bitmap bitmap = BitmapFactory.decodeStream(response.toStream());
            iv.setImageBitmap(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * RxJava下载
     */
    private void rxDownload() {
        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> e) throws Exception {
                callDownload = client.newCall("http://f3.market.xiaomi.com/download/AppStore/0b3f6b4e06ff14b61065972a96149da822c86ad40/com.eg.android.AlipayGphone.apk");
                Response response = callDownload.execute();
                InputStream is = response.toStream();
                File file = new File(Environment.getExternalStorageDirectory().getPath() + "/alipay.apk");
                FileOutputStream os = new FileOutputStream(file);
                int length = response.getContentLength();
                int p = 0;
                int bytes;
                byte[] buffer = new byte[1024];
                while ((bytes = is.read(buffer)) != -1) {
                    os.write(buffer, 0, bytes);
                    p += bytes;
                    e.onNext(String.valueOf((p / (float) length) * 100));
                }
                response.close();
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        text.setText(s);
                        float f = Float.parseFloat(s);
                        progressBar.setProgress((int) f);
                    }
                });
    }
}
