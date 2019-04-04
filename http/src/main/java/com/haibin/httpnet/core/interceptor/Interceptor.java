//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.haibin.httpnet.core.interceptor;

import com.haibin.httpnet.builder.Request;
import com.haibin.httpnet.core.Response;
import com.haibin.httpnet.core.connection.Connection;

import java.io.IOException;

public interface Interceptor {

    Response intercept(Interceptor.Chain chain) throws IOException;

    interface Chain {

        Request request();

        Response proceed(Request request) throws IOException;

        Connection connection();
    }
}
