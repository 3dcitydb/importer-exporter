package org.citydb.database.connection;

import org.apache.tomcat.jdbc.pool.interceptor.StatementFinalizer;

import java.lang.reflect.Method;

public class ConcurrentStatementFinalizer extends StatementFinalizer {

    @Override
    public synchronized Object createStatement(Object proxy, Method method, Object[] args, Object statement, long time) {
        return super.createStatement(proxy, method, args, statement, time);
    }
}
