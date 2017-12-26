/*
 * Copyright (C) 2016 huanghaibin_dev <huanghaibin_dev@163.com>
 * WebSite https://github.com/MiracleTimes-Dev
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.haibin.httpnet.core.call;

import com.haibin.httpnet.HttpNetClient;
import com.haibin.httpnet.builder.Request;
import com.haibin.httpnet.core.Response;
import com.haibin.httpnet.core.connection.Connection;
import com.haibin.httpnet.core.connection.HttpConnection;
import com.haibin.httpnet.core.connection.HttpsConnection;
import com.haibin.httpnet.core.interceptor.CallServerInterceptor;
import com.haibin.httpnet.core.interceptor.ConnectInterceptor;
import com.haibin.httpnet.core.interceptor.Interceptor;
import com.haibin.httpnet.core.interceptor.RealInterceptorChain;
import com.haibin.httpnet.core.interceptor.RetryAndFollowUpInterceptor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 请求的封装执行
 */
public class AsyncCall implements Runnable {
    private Callback mCallBack;
    private Request mRequest;
    private Connection mConnection;
    private HttpNetClient mClient;

    AsyncCall(HttpNetClient client, Request request, Callback callBack, InterceptListener listener) {
        this.mCallBack = callBack;
        this.mRequest = request;
        this.mClient = client;
        mConnection = request.url().startsWith("https") ?
                new HttpsConnection(client, request, listener) :
                new HttpConnection(client, request, listener);
    }

    @Override
    public void run() {
        mConnection.connect(mCallBack);
    }

    public Response execute() throws IOException {
        return getResponseWithInterceptorChain();
//        return mConnection.connect();
    }

    private Response getResponseWithInterceptorChain() throws IOException {
        List<Interceptor> interceptors = new ArrayList<>();
        interceptors.addAll(this.mClient.interceptors());
        interceptors.add(new RetryAndFollowUpInterceptor(this.mClient));
        interceptors.add(new ConnectInterceptor(this.mClient));
        interceptors.add(new CallServerInterceptor());
        Interceptor.Chain chain = new RealInterceptorChain(interceptors, mConnection, 0, mRequest);
        return chain.proceed(this.mRequest);
    }

    public Request getRequest() {
        return mRequest;
    }

    Connection getConnection() {
        return mConnection;
    }

    @Override
    public boolean equals(Object o) {
        if (o != null && o instanceof AsyncCall) {
            return mRequest.url().equalsIgnoreCase(((AsyncCall) o).getRequest().url());
        }
        return super.equals(o);
    }
}