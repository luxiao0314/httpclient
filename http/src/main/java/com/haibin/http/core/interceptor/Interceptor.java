//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.haibin.http.core.interceptor;

import com.haibin.http.builder.Request;
import com.haibin.http.core.Response;
import com.haibin.http.core.connection.Connection;

import java.io.IOException;

public interface Interceptor {

    Response intercept(Interceptor.Chain chain) throws IOException;

    interface Chain {

        Request request();

        Response proceed(Request request) throws IOException;

        Connection connection();
    }
}
