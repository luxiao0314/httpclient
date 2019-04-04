package com.haibin.httpnetproject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.haibin.http.HttpNetClient;
import com.haibin.http.builder.Headers;
import com.haibin.http.builder.Request;
import com.haibin.http.builder.RequestParams;
import com.haibin.http.core.Response;
import com.haibin.http.core.call.Call;
import com.haibin.http.core.call.InterceptListener;

import java.io.File;
import java.io.FileOutputStream;
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

/**
 * Created by haibin
 * on 2016/9/24.
 */

public class TestActivity extends AppCompatActivity {

    HttpNetClient client = new HttpNetClient();
    private Call callDownload;

    public static void show(Activity activity) {
        Intent intent = new Intent(activity, TestActivity.class);
        activity.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button:
                upload();
                break;
            case R.id.button2:
                download();
                break;
            case R.id.button3:
                download2();
                break;
            case R.id.button4:
                break;
            case R.id.button5:
                break;
        }
    }

    private void upload() {
        final Request request = new Request.Builder()
                .url("http://upload.cnblogs.com/ImageUploader/TemporaryAvatarUpload")
                .method("POST")
                .params(new RequestParams()
                        .putFile("qqfile", "/storage/emulated/0/DCIM/Camera/339718150.jpeg"))
                .headers(new Headers.Builder().addHeader("Cookie", "CNZZDATA1259029673=2072545293-1479795067-null%7C1479795067; lhb_smart_1=1; __utma=226521935.1789795872.1480996255.1480996255.1480996255.1; __utmz=226521935.1480996255.1.1.utmcsr=baidu|utmccn=(organic)|utmcmd=organic; .CNBlogsCookie=A6783E37E1040979421EC4A57A2FEFBB74B65BB51C7345AC99B64A7065293F59A79C6830C60D71629E8D28A332436E23CD40968EB58AA830CBD0F0733438F9A7627C074DB0462C2576D206D3752E640871E8CB23D1A50B0A9962C158466EE81425B1E516; _gat=1; _ga=GA1.2.1789795872.1480996255"))
                .build();
        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(final ObservableEmitter<String> e) throws Exception {
                client.newCall(request)
                        .intercept(new InterceptListener() {
                            @Override
                            public void onProgress(int index, long currentLength, long totalLength) {
                                Log.e("file", index + " -- " + " -- " + currentLength + " -- " + totalLength);
                                e.onNext(index + " -- " + " -- " + currentLength + " -- " + totalLength);
                            }
                        })
                        .execute();
            }
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String response) throws Exception {
                    }

                });
    }

    private void download2() {
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
                    }
                });
    }

    private void download() {
        final File rangeFile = new File(Environment.getExternalStorageDirectory().getPath() + "/cnblogs.apk");
        final long readySize = rangeFile.exists() ? rangeFile.length() : 0;
        Headers.Builder headers = new Headers.Builder()
                .addHeader("Range", "bytes=" + readySize + "-");
        Request request = new Request.Builder()
                .url("http://f1.market.xiaomi.com/download/AppStore/0117653278abecee8762883a940e129e9d242ae7d/com.huanghaibin_dev.cnblogs.apk")
                                .headers(headers)
                                .build();
        callDownload = client.newCall(request);//调用callDownload.cancel();取消请求实现暂停，再次请求即可从断点位置继续下载
        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> e) throws Exception {
                try {
                    Response response = callDownload.execute();
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
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(String value) {
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }
}
