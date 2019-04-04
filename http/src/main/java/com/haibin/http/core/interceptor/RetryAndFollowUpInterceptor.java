//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.haibin.http.core.interceptor;

import com.haibin.http.HttpNetClient;
import com.haibin.http.builder.Request;
import com.haibin.http.core.Response;

import java.io.IOException;

public final class RetryAndFollowUpInterceptor implements Interceptor {

    private final HttpNetClient client;

    public RetryAndFollowUpInterceptor(HttpNetClient client) {
        this.client = client;
    }

    public HttpNetClient client() {
        return this.client;
    }

    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Response response = chain.proceed(request);
        this.followUpRequest(response);
        return response;
    }

    private void followUpRequest(Response userResponse) throws IOException {
        if(userResponse == null) {
            throw new IllegalStateException();
        } else {
            int responseCode = userResponse.getCode();
            switch(responseCode) {
            case 307:
            case 308:
            case 300:
            case 301:
            case 302:
            case 303:
            case 401:
                this.client.authenticator().authenticate(userResponse);
                return;
            case 407:
            case 408:
            default:
            }
        }
    }
}
