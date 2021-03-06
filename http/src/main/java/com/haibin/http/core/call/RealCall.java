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
package com.haibin.http.core.call;

import com.haibin.http.HttpNetClient;
import com.haibin.http.builder.Request;
import com.haibin.http.core.Response;
import com.haibin.http.core.connection.HttpConnection;
import com.haibin.http.core.connection.HttpsConnection;
import com.haibin.http.core.interceptor.ConnectInterceptor;
import com.haibin.http.core.connection.Connection;
import com.haibin.http.core.interceptor.CallServerInterceptor;
import com.haibin.http.core.interceptor.Interceptor;
import com.haibin.http.core.interceptor.RealInterceptorChain;
import com.haibin.http.core.interceptor.RetryAndFollowUpInterceptor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 请求的真正代理实现
 */
public class RealCall implements Call {
    private HttpNetClient mClient;
    private Request mRequest;
    private AsyncCall mAsyncCall;
    private Connection mConnection;
    private InterceptListener mListener;
    private boolean executed;

    public RealCall(HttpNetClient client, Request request) {
        this.mClient = client;
        this.mRequest = request;
        mConnection = request.url().startsWith("https") ?
                new HttpsConnection(client, request, mListener) :
                new HttpConnection(client, request, mListener);
    }

    @Override
    public Call intercept(InterceptListener listener) {
        this.mListener = listener;
        return this;
    }

    /**
     * 异步
     * @param callBack 回调
     */
    @Override
    public void enqueue(Callback callBack) {
        if (mAsyncCall == null)
            mAsyncCall = new AsyncCall(mRequest, callBack);
        mClient.dispatcher().execute(mAsyncCall);
    }

    /**
     * 同步
     * @return
     * @throws IOException
     */
    @Override
    public Response execute() throws IOException {
        //第一步：判断同一Http是否请求过
        synchronized (this) {
            if (executed) throw new IllegalStateException("Already Executed");
            executed = true;
        }
        try {
            Response result = getResponseWithInterceptorChain();
            if (result == null) throw new IOException("Canceled");
            return result;
        } catch (IOException e) {
            throw e;
        } finally {
            mConnection.finish();
        }
    }

    @Override
    public void cancel() {
        mConnection.disconnect();
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    public class AsyncCall implements Runnable {
        private Callback mCallBack;
        private Request mRequest;

        AsyncCall(Request request, Callback callBack) {
            this.mCallBack = callBack;
            this.mRequest = request;
        }

        /**
         * 异步请求
         */
        @Override
        public void run() {
            try {
                Response response = getResponseWithInterceptorChain();
                mCallBack.onResponse(response);
            } catch (IOException e) {
                e.printStackTrace();
                mCallBack.onFailure(e);
            } finally {
                mConnection.finish();
            }
        }

        public Request getRequest() {
            return mRequest;
        }

        @Override
        public boolean equals(Object o) {
            if (o != null && o instanceof AsyncCall) {
                return mRequest.url().equalsIgnoreCase(((AsyncCall) o).getRequest().url());
            }
            return super.equals(o);
        }
    }

    private Response getResponseWithInterceptorChain() throws IOException {
        List<Interceptor> interceptors = new ArrayList<>(this.mClient.interceptors());
        interceptors.add(new RetryAndFollowUpInterceptor(this.mClient));
        interceptors.add(new ConnectInterceptor(this.mClient));
        interceptors.add(new CallServerInterceptor());
        Interceptor.Chain chain = new RealInterceptorChain(interceptors, mConnection, 0, mRequest);
        return chain.proceed(this.mRequest);
    }

}
