//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.haibin.http.core.interceptor;

import com.haibin.http.builder.Request;
import com.haibin.http.core.Response;
import com.haibin.http.core.connection.Connection;

import java.io.IOException;
import java.util.List;

public final class RealInterceptorChain implements Interceptor.Chain {

    private List<Interceptor> interceptors;
    private final Connection connection;
    private final Request request;
    private int index;
    private int calls;

    public RealInterceptorChain(List<Interceptor> interceptors, Connection connection, int index, Request request) {
        this.interceptors = interceptors;
        this.connection = connection;
        this.request = request;
        this.index = index;
    }

    @Override
    public Request request() {
        return request;
    }

    @Override
    public Connection connection() {
        return connection;
    }

    @Override
    public Response proceed(Request request) throws IOException {
        if (this.index >= this.interceptors.size()) {
            throw new AssertionError();
        } else {
            ++this.calls;
            if (this.calls > 1) {
                throw new IllegalStateException("network interceptor " + this.interceptors.get(this.index - 1) + " must call proceed() exactly once");
            } else {
                RealInterceptorChain next = new RealInterceptorChain(this.interceptors, connection, this.index + 1, request);
                Interceptor interceptor = this.interceptors.get(this.index);
                Response response = interceptor.intercept(next);
                if (this.index + 1 < this.interceptors.size() && next.calls != 1) {
                    throw new IllegalStateException("network interceptor " + interceptor + " must call proceed() exactly once");
                } else if (response == null) {
                    throw new NullPointerException("interceptor " + interceptor + " returned null");
                } else {
                    return response;
                }
            }
        }
    }
}
