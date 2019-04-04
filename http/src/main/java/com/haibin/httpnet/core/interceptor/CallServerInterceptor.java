//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.haibin.httpnet.core.interceptor;

import com.haibin.httpnet.core.Response;
import com.haibin.httpnet.core.connection.Connection;

import java.io.IOException;
import java.net.ProtocolException;

public final class CallServerInterceptor implements Interceptor {

    public CallServerInterceptor(){
    }

    public Response intercept(Chain chain) throws IOException {
        Connection connection = chain.connection();
        Response response = connection.connect();
        int code = response.getCode();
        if ((code == 204 || code == 205) && response.getContentLength() > 0L) {
            throw new ProtocolException("HTTP " + code + " had non-zero Content-Length: " +  response.getContentLength());
        } else {
            return response;
        }
    }
}