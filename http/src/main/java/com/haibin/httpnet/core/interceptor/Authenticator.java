//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.haibin.httpnet.core.interceptor;

import com.haibin.httpnet.builder.Request;
import com.haibin.httpnet.core.Response;

import java.io.IOException;

public interface Authenticator {
    Authenticator NONE = new Authenticator() {
        public Request authenticate(Response response) {
            return null;
        }
    };

    Request authenticate(Response var2) throws IOException;
}
