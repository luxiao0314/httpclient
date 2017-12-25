//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.haibin.httpnet.core.interceptor;

import com.haibin.httpnet.HttpNetClient;
import com.haibin.httpnet.builder.Request;
import com.haibin.httpnet.core.Response;

import java.io.IOException;

public final class ConnectInterceptor implements Interceptor {

    public final HttpNetClient client;

    public ConnectInterceptor(HttpNetClient client) {
        this.client = client;
    }

    public Response intercept(Chain chain) throws IOException {
        RealInterceptorChain realChain = (RealInterceptorChain)chain;
        Request request = realChain.request();

        return realChain.proceed(request);
    }
}
