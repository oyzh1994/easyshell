package cn.oyzh.easyshell.oracle;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;

/**
 * Oracle连接包装器，用于在连接关闭时同步释放本地代理隧道。
 */
public final class ShellOracleConnectionWrapper implements InvocationHandler {

    private final Connection connection;

    private final AutoCloseable tunnel;

    private Connection proxy;

    private ShellOracleConnectionWrapper(Connection connection, AutoCloseable tunnel) {
        this.connection = connection;
        this.tunnel = tunnel;
    }

    public static Connection wrap(Connection connection, AutoCloseable tunnel) {
        ShellOracleConnectionWrapper handler = new ShellOracleConnectionWrapper(connection, tunnel);
        handler.proxy = (Connection) Proxy.newProxyInstance(
                connection.getClass().getClassLoader(),
                new Class<?>[]{Connection.class},
                handler
        );
        return handler.proxy;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String name = method.getName();
        if (method.getDeclaringClass() == Object.class) {
            return switch (name) {
                case "equals" -> proxy == args[0];
                case "hashCode" -> System.identityHashCode(proxy);
                case "toString" -> "OracleConnectionProxy(" + this.connection + ")";
                default -> throw new UnsupportedOperationException(name);
            };
        }
        if (method.isDefault()) {
            return InvocationHandler.invokeDefault(proxy, method, args);
        }
        if ("unwrap".equals(name) && args != null && args.length == 1 && args[0] instanceof Class<?> type) {
            if (type.isInstance(proxy)) {
                return proxy;
            }
        }
        if ("isWrapperFor".equals(name) && args != null && args.length == 1 && args[0] instanceof Class<?> type) {
            return type.isInstance(proxy) || this.connection.isWrapperFor(type);
        }
        try {
            Object result = method.invoke(this.connection, args);
            if (("close".equals(name) || "abort".equals(name)) && (args == null || args.length <= 1)) {
                this.closeTunnel();
            }
            return result;
        } catch (InvocationTargetException ex) {
            if ("close".equals(name) || "abort".equals(name)) {
                this.closeTunnel();
            }
            throw ex.getCause();
        }
    }

    private void closeTunnel() {
        try {
            this.tunnel.close();
        } catch (Exception ignored) {
        }
    }
}
