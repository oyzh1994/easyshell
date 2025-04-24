package cn.oyzh.easyshell.telnet;

import cn.oyzh.easyshell.domain.ShellConnect;

import java.io.IOException;

/**
 * @author oyzh
 * @since 2025-04-24
 */
public class TelnetClient extends org.apache.commons.net.telnet.TelnetClient implements AutoCloseable {

    private final ShellConnect shellConnect;

//    /**
//     * 数据队列
//     */
//    private final Queue<Character> characters = new ArrayDeque<>();

    public TelnetClient(ShellConnect shellConnect) {
        this.shellConnect = shellConnect;
    }

    public void start() throws IOException {
        super.connect(this.shellConnect.hostIp(), this.shellConnect.hostPort());

        // 处理登录
//        handleLogin2(this.getInputStream(), new PrintStream(this.getOutputStream()), this.shellConnect.getUser(), shellConnect.getPassword());

//        startxx();
    }

//    private void handleLogin(InputStream in, PrintStream out, String username, String password) throws IOException {
//        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
//        String line;
//        while ((line = reader.readLine()) != null) {
//            if (line.contains("login:")) {
//                out.println(username);
//                out.flush();
//            } else if (line.contains("Password:")) {
//                out.println(password);
//                out.flush();
//            } else if (line.contains("$") || line.contains("#")) {
//                // 登录成功
//                break;
//            }
//        }
//    }
//
//    private void handleLogin2(InputStream in, PrintStream out, String username, String password) throws IOException {
//        try {
//
//            byte[] buffer = new byte[1024];
//            int bytesRead;
//            StringBuilder response = new StringBuilder();
//            boolean inputName = false;
//            boolean inputPwd = false;
//            while ((bytesRead = in.read(buffer)) != -1) {
//                for (int i = 0; i < bytesRead; i++) {
//                    char c = (char) buffer[i];
//                    response.append(c);
//                    if (c == '\n') {
//                        String line = response.toString();
//                        System.out.println("server response: " + line);
//                        if (!inputName) {
//                            inputName = true;
//                            System.out.println("输入用户名: " + username);
//                            out.println(username);
//                            out.flush();
//                            continue;
//                        }
//                        if (!inputPwd) {
//                            inputPwd = true;
//                            System.out.println("输入密码: " + password);
//                            out.println(password);
//                            out.flush();
//                            continue;
//                        }
//                        if (line.contains("$") || line.contains("#")) {
//                            // 登录成功
//                            System.out.println("登录成功");
//                            return;
//                        }
//                        response.setLength(0);
//                    }
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            startxx();
//        }
//    }

//
//    public void startxx() {
//        // 启动线程将 Telnet 输入复制到 pty4j 输出
//        new Thread(() -> {
//            try {
//                byte[] buffer = new byte[4096];
//                while (true) {
//                    int bytesRead = this.getInputStream().read(buffer);
//                    if (bytesRead == -1) {
//                        break;
//                    }
//                    ThreadUtil.sleep(100);
//                    String str = new String(buffer, 0, bytesRead);
//                    for (char aChar : str.toCharArray()) {
//                        characters.add(aChar);
//                    }
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            } finally {
//                System.out.println("----");
//            }
//        }).start();
//    }

    @Override
    public void close() {
        try {
            this.disconnect();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public ShellConnect getShellConnect() {
        return shellConnect;
    }

//    public boolean isEmpty() {
//        return this.characters.isEmpty();
//    }
//
//    public Character takeChar() {
//        return this.characters.poll();
//    }
}
