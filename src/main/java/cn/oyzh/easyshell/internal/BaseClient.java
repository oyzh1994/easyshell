package cn.oyzh.easyshell.internal;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellConnect;

import java.nio.charset.Charset;

/**
 * 基础客户端
 *
 * @author oyzh
 * @since 2025-04-25
 */
public interface BaseClient extends AutoCloseable {

    /**
     * 连接
     */
    default void start() throws Exception {
        this.start(this.connectTimeout());
    }

    /**
     * 连接
     *
     * @param timeout 超时时间
     */
    void start(int timeout) throws Exception;

    /**
     * 获取连接
     *
     * @return 连接
     */
    ShellConnect getShellConnect();

    /**
     * 获取连接名称
     *
     * @return 名称
     */
    default String connectName() {
        return this.getShellConnect().getName();
    }

    /**
     * 获取连接超时
     *
     * @return 连接超时
     */
    default int connectTimeout() {
        return this.getShellConnect().connectTimeOutMs();
    }

    /**
     * 获取字符集
     *
     * @return 字符集
     */
    default Charset getCharset() {
        String charset = this.getShellConnect().getCharset();
        if (StringUtil.isBlank(charset)) {
            return Charset.defaultCharset();
        }
        return Charset.forName(charset);
    }

    /**
     * 是否已关闭
     *
     * @return 结果
     */
    default boolean isClosed() {
        return !this.isConnected();
    }

    /**
     * 是否已连接
     *
     * @return 结果
     */
    boolean isConnected();
}