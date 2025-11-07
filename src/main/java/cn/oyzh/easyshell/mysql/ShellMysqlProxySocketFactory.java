package cn.oyzh.easyshell.mysql;

import cn.oyzh.easyshell.domain.ShellProxyConfig;
import cn.oyzh.easyshell.util.ShellProxyUtil;
import com.mysql.cj.conf.PropertySet;
import com.mysql.cj.protocol.StandardSocketFactory;

import java.io.IOException;
import java.net.Socket;
import java.util.Properties;

/**
 * mysql代理连接工厂
 *
 * @author oyzh
 * @since 2025/11/07
 */
public class ShellMysqlProxySocketFactory extends StandardSocketFactory {

    /**
     * 连接超时
     */
    private int connectTimeout;

    @Override
    public Socket connect(String host, int portNumber, PropertySet props, int loginTimeout) throws IOException {
        this.host = host;
        this.port = portNumber;
        this.connectTimeout = loginTimeout <= 0 ? 5000 : loginTimeout * 1000;
        Socket socket = this.createSocket(props);
        super.configureSocket(socket, props);
        this.rawSocket = socket;
        this.sslSocket = socket;
        return this.rawSocket;
    }

    @Override
    protected Socket createSocket(PropertySet props) {
        Socket socket;
        try {
            Properties properties = props.exposeAsProperties();
            // 从属性中获取代理配置
            String proxyHost = properties.getProperty("_proxyHost");
            String proxyType = properties.getProperty("_proxyType");
            String proxyUser = properties.getProperty("_proxyUser");
            String proxyPortVal = properties.getProperty("_proxyPort");
            String proxyPassword = properties.getProperty("_proxyPassword");
            ShellProxyConfig proxyConfig = new ShellProxyConfig();
            proxyConfig.setHost(proxyHost);
            proxyConfig.setUser(proxyUser);
            proxyConfig.setProtocol(proxyType);
            proxyConfig.setPassword(proxyPassword);
            proxyConfig.setPort(Integer.parseInt(proxyPortVal));
            socket = ShellProxyUtil.createSocket(proxyConfig, this.host, this.port, this.connectTimeout);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        return socket;
    }
}