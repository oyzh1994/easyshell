package cn.oyzh.easyshell.rlogin;

import javax.net.SocketFactory;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * @author oyzh
 * @since 2025-05-27
 */
public class ShellRLoginSocketFactory extends SocketFactory {

    @Override
    public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
        Socket socket = new Socket(host, port);
        socket.setReuseAddress(Boolean.TRUE);
        return socket;
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localAddress, int localPort) throws IOException, UnknownHostException {
        Socket socket = new Socket(host, port, localAddress, localPort);
        socket.setReuseAddress(Boolean.TRUE);
        return socket;
    }

    @Override
    public Socket createSocket(InetAddress inetAddress, int port) throws IOException {
        Socket socket = new Socket(inetAddress, port);
        socket.setReuseAddress(Boolean.TRUE);
        return socket;
    }

    @Override
    public Socket createSocket(InetAddress inetAddress, int port, InetAddress localAddress, int localPort) throws IOException {
        Socket socket = new Socket(inetAddress, port, localAddress, localPort);
        socket.setReuseAddress(Boolean.TRUE);
        return socket;
    }
}
