package cn.oyzh.easyshell.internal;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellConnect;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * @author oyzh
 * @since 2025-04-25
 */
public interface BaseClient extends AutoCloseable {

    /**
     * 开始连接客户端
     */
    default void start() throws IOException {
        this.start(this.getShellConnect().connectTimeOutMs());
    }

    /**
     * 开始连接客户端
     *
     * @param timeout 超时时间
     */
    void start(int timeout) throws IOException;

    /**
     * 获取连接
     *
     * @return 连接
     */
    ShellConnect getShellConnect();

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

}