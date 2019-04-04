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
package com.haibin.http.core.connection;


import com.haibin.http.HttpNetClient;
import com.haibin.http.builder.Request;
import com.haibin.http.core.call.InterceptListener;
import com.haibin.http.core.Response;
import com.haibin.http.core.io.HttpContent;
import com.haibin.http.core.io.IO;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URLConnection;

/**
 * http连接
 */
@SuppressWarnings("unused")
public class HttpConnection extends Connection {
    private HttpURLConnection mHttpUrlConnection;
    private InterceptListener mListener;

    public HttpConnection(HttpNetClient client, Request request) {
        super(client, request);
    }

    public HttpConnection(HttpNetClient client, Request request, InterceptListener listener) {
        super(client, request);
        this.mListener = listener;
    }

    @Override
    void connect(URLConnection connection, String method) throws IOException {
        mHttpUrlConnection = (HttpURLConnection) connection;
        mHttpUrlConnection.setRequestMethod(method);
        mHttpUrlConnection.setUseCaches(true);
        mHttpUrlConnection.setConnectTimeout(mClient.timeout());
        mHttpUrlConnection.setChunkedStreamingMode(1024);
        mHttpUrlConnection.setRequestProperty("Accept-Language", "zh-CN");
        mHttpUrlConnection.setRequestProperty("Charset", mRequest.encode());
        mHttpUrlConnection.setRequestProperty("Connection", "Keep-Alive");
    }

    @Override
    void post() throws IOException {
        mHttpUrlConnection.setDoOutput(true);
        mHttpUrlConnection.setRequestProperty("Content-Type", getContentType(mRequest.params()));
        mOutputStream = new DataOutputStream(mHttpUrlConnection.getOutputStream());
        HttpContent body = mRequest.content();
        if (body != null) {
            body.setOutputStream(mOutputStream);
            body.doOutput(mListener);
        }
    }

    @Override
    void get() throws IOException {

    }

    @Override
    void put() throws IOException {
        post();
    }

    @Override
    void delete() throws IOException {

    }

    @Override
    void patch() throws IOException {

    }

    @Override
    Response getResponse() throws IOException {
        return new Response(mHttpUrlConnection.getResponseCode(),
                mInputStream,
                mHttpUrlConnection.getHeaderFields(),
                mRequest.encode(), mHttpUrlConnection.getContentLength());
    }

    @Override
    public void disconnect() {
        if (mHttpUrlConnection != null)
            mHttpUrlConnection.disconnect();
    }

    @Override
    public void finish() {
        IO.close(mOutputStream, mInputStream);
    }
}