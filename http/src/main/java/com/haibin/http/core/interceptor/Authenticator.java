//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.haibin.http.core.interceptor;

import com.haibin.http.builder.Request;
import com.haibin.http.core.Response;

import java.io.IOException;

public interface Authenticator {
    Authenticator NONE = new Authenticator() {
        public Request authenticate(Response response) {
            return null;
        }
    };

    Request authenticate(Response var2) throws IOException;
}
