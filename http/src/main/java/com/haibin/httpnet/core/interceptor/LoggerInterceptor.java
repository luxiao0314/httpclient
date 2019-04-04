package com.haibin.httpnet.core.interceptor;

import android.util.Log;

import com.haibin.httpnet.builder.Request;
import com.haibin.httpnet.core.Response;

import java.io.IOException;

/**
 * @Description
 * @Author lucio
 * @Email xiao.lu@magicwindow.cn
 * @Date 25/12/2017 4:08 PM
 * @Version
 */
public class LoggerInterceptor implements Interceptor{

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Response response = chain.proceed(request);
        Log.e("loggerInterceptor", " onComplete " + response.getBody());
        return response;
    }
}
