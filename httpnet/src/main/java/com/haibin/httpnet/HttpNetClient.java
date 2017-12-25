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
package com.haibin.httpnet;

import com.haibin.httpnet.builder.Request;
import com.haibin.httpnet.core.Dispatcher;
import com.haibin.httpnet.core.call.Call;
import com.haibin.httpnet.core.call.RealCall;
import com.haibin.httpnet.core.connection.SSLManager;
import com.haibin.httpnet.core.interceptor.Authenticator;
import com.haibin.httpnet.core.interceptor.Interceptor;

import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLSocketFactory;

/**
 * Http客户端
 */
@SuppressWarnings("unused")
public final class HttpNetClient {
    private Proxy proxy;//为当前客户端开启全局代理
    private final int timeout;
    private Dispatcher dispatcher;
    private SSLManager sslManager;
    private List<Interceptor> interceptors;
    private Authenticator authenticator;

    public HttpNetClient() {
        this(new HttpNetClient.Builder());
    }

    HttpNetClient(Builder builder) {
        this.interceptors = builder.interceptors;
        this.authenticator = builder.authenticator;
        this.sslManager = builder.sslManager;
        this.dispatcher = builder.dispatcher;
        this.proxy = builder.proxy;
        this.timeout = builder.timeout;
    }

    public HttpNetClient.Builder newBuilder() {
        return new HttpNetClient.Builder(this);
    }

    public Call newCall(Request request) {
        return new RealCall(this, request);
    }

    public Call newCall(String url) {
        return new RealCall(this, getDefaultRequest(url));
    }

    public int timeout(){
        return timeout;
    }

    public Dispatcher dispatcher() {
        return dispatcher;
    }

    public Proxy getProxy() {
        return proxy;
    }

    public List<Interceptor> interceptors() {
        return this.interceptors;
    }

    public Authenticator authenticator() {
        return this.authenticator;
    }

    public SSLSocketFactory getSslSocketFactory() {
        return sslManager.getSslSocketFactory();
    }

    private Request getDefaultRequest(String url) {
        return new Request.Builder()
                .url(url)
                .build();
    }

    public static final class Builder {
        Proxy proxy;
        Dispatcher dispatcher;
        SSLManager sslManager;
        Authenticator authenticator;
        final List<Interceptor> interceptors = new ArrayList<>();
        int timeout;

        public Builder() {
            this.dispatcher = new Dispatcher();
            this.sslManager = new SSLManager();
            this.timeout = 13000;
        }

        Builder(HttpNetClient okHttpClient) {
            this.dispatcher = okHttpClient.dispatcher;
            this.proxy = okHttpClient.proxy;
        }

        public HttpNetClient.Builder timeout(int timeout) {
            this.timeout = timeout;
            if (timeout <= 0) this.timeout = 13000;
            return this;
        }

        /**
         * 开启全局代理
         */
        public HttpNetClient.Builder setProxy(Proxy proxy) {
            this.proxy = proxy;
            return this;
        }

        /**
         * 开启全局代理
         */
        public HttpNetClient.Builder setProxy(String host, int port) {
            this.proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port));
            return this;
        }

        /**
         * 导入证书
         */
        public HttpNetClient.Builder setSslSocketFactory(SSLSocketFactory sslSocketFactory) {
            this.sslManager.setSslSocketFactory(sslSocketFactory);
            return this;
        }

        /**
         * 导入证书
         */
        public HttpNetClient.Builder setSslSocketFactory(InputStream... cerInputStream) {
            this.sslManager.setSslSocketFactory(cerInputStream);
            return this;
        }

        public HttpNetClient.Builder setSslSocketFactory(String... cerPaths) {
            this.sslManager.setSslSocketFactory(cerPaths);
            return this;
        }

        public HttpNetClient.Builder setSslSocketFactoryAsString(String... cerValues) {
            this.sslManager.setSslSocketFactoryAsString(cerValues);
            return this;
        }

        public HttpNetClient.Builder authenticator(Authenticator authenticator) {
            if(authenticator == null) {
                throw new NullPointerException("authenticator == null");
            } else {
                this.authenticator = authenticator;
                return this;
            }
        }

        public List<Interceptor> interceptors() {
            return this.interceptors;
        }

        public HttpNetClient.Builder addInterceptor(Interceptor interceptor) {
            this.interceptors.add(interceptor);
            return this;
        }

        public HttpNetClient build() {
            return new HttpNetClient(this);
        }
    }
}
