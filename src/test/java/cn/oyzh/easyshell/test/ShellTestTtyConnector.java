package cn.oyzh.easyshell.test;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.IOUtil;
import cn.oyzh.easyshell.ssh2.ShellSSHClient;
import cn.oyzh.fx.tty.TtyDefaultTtyConnector;
import com.jcraft.jsch.ChannelShell;
import com.pty4j.PtyProcess;
import net.schmizz.sshj.connection.channel.direct.Session;
import org.mosh4j.core.MoshTerminalFrontend;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * @author oyzh
 * @since 2025-03-04
 */
public class ShellTestTtyConnector extends TtyDefaultTtyConnector {

    /**
     * ssh客户端
     */
    private ShellSSHClient client;

    private InputStreamReader shellReader;

    private OutputStreamWriter shellWriter;

    private Session.Shell shell;

    public void init(Session.Shell shell) throws IOException {
        this.shell = shell;
        this.shellReader = new InputStreamReader(shell.getInputStream(), this.myCharset);
        this.shellWriter = new OutputStreamWriter(shell.getOutputStream(), this.myCharset);
    }

    private MoshTerminalFrontend frontend;

    private final BlockingQueue<byte[]> dataQueue = new ArrayBlockingQueue<>(1000);

    public void init(MoshTerminalFrontend frontend) throws IOException {
        this.frontend = frontend;
        Thread reader = new Thread(() -> {
            try {
                while (frontend.isRunning()) {
                    // 从 mosh4j 获取数据 (等待最多 100ms)
                    byte[] data = frontend.takeHostBytes(100);
                    if (data != null && data.length > 0) {
                        // 将数据放入队列，供 JediTerm 主线程消费
                        dataQueue.offer(data);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        reader.setDaemon(true);
        reader.start();

    }

    private InputStream in;
    private OutputStream out;

    public void init(OutputStream out, InputStream in) throws IOException {
        this.in = in;
        this.out = out;
        this.shellReader = new InputStreamReader(in, this.myCharset);
        this.shellWriter = new OutputStreamWriter(out, this.myCharset);
    }

    public void init(OutputStream out) throws IOException {
        this.out = out;
        this.shellWriter = new OutputStreamWriter(out, this.myCharset);
    }

    public void init( InputStream in) throws IOException {
        this.in = in;
        this.shellReader = new InputStreamReader(in, this.myCharset);
        this.shellWriter = new OutputStreamWriter(out, this.myCharset);
    }

    private ChannelShell shell1;

    public void init(ChannelShell shell) throws IOException {
        this.shell1 = shell;
        this.shellReader = new InputStreamReader(shell.getInputStream(), this.myCharset);
        this.shellWriter = new OutputStreamWriter(shell.getOutputStream(), this.myCharset);
    }

    private org.apache.sshd.client.channel.ChannelShell shell2;

    public void init(org.apache.sshd.client.channel.ChannelShell shell) throws IOException {
        this.shell2 = shell;
        this.shellReader = new InputStreamReader(shell.getInvertedOut(), this.myCharset);
        this.shellWriter = new OutputStreamWriter(shell.getInvertedIn(), this.myCharset);
    }

    public ShellTestTtyConnector(PtyProcess process, Charset charset, List<String> commandLines) {
        super(process, charset, commandLines);
    }

    @Override
    public int read(char[] buf, int offset, int length) throws IOException {
        if (frontend == null) {
            int len;
            if (this.shellReader == null) {
                len = super.read(buf, offset, length);
            } else {
                len = this.shellReader.read(buf, offset, length);
            }
            if (len > 0) {
                return this.doRead(buf, offset, len);
            }
            return len;
        } else {
            // 这个方法会被 JediTerm 的渲染循环持续调用，以获取数据
            try {
                // 尝试从队列中取出一批数据 (等待最多 50ms)
                byte[] data = dataQueue.poll();
                if (data == null) {
                    return 0; // 没有新数据
                }

                // 将字节数据转换为字符，并填入 buf 数组
                String chunk = new String(data, StandardCharsets.UTF_8);
                int charsToCopy = Math.min(chunk.length(), length);
                chunk.getChars(0, charsToCopy, buf, offset);
                return charsToCopy;
            } catch (Exception e) {
                throw new IOException("Error reading from Mosh", e);
            }
        }
    }

    // private Runnable reset;
    //
    // public Runnable getReset() {
    //     return reset;
    // }
    //
    // public void setReset(Runnable reset) {
    //     this.reset = reset;
    // }

    @Override
    public void write(String str) throws IOException {
        // if (str.equals("reset--1")) {
        //     // reset.run();
        //
        //     try {
        //         // shell1.resetPty("xterm");
        //     } catch (Exception e) {
        //         throw new RuntimeException(e);
        //     }
        //     return;
        // }
        JulLog.warn("shell write : {}", str);
        // super.write(str);

        if (frontend != null) {
            frontend.sendUserInput(str.getBytes(this.myCharset));
        } else {
            this.shellWriter.write(str);
            this.shellWriter.flush();
        }
    }

    @Override
    public void write(byte[] bytes) throws IOException {
        if (frontend != null) {
            frontend.sendUserInput(bytes);
        } else {
            String str = new String(bytes, this.myCharset);
            this.write(str);
        }
    }

    @Override
    public void close() {
        super.close();
        this.client = null;
        if (this.shellReader != null) {
            IOUtil.close(this.shellReader);
            this.shellReader = null;
        }
        if (this.shellWriter != null) {
            IOUtil.close(this.shellWriter);
            this.shellWriter = null;
        }
    }

    @Override
    protected int doRead(char[] buf, int offset, int len) throws IOException {
        super.doRead(buf, offset, len);
        String str = new String(buf, offset, len);
        if (this.client != null) {
            ThreadUtil.start(() -> this.client.resolveWorkerDir(str));
        }
        return len;
    }

    @Override
    public InputStream input() {
        try {
            if (this.shell1 != null) {
                return this.shell1.getInputStream();
            }
            if (this.shell2 != null) {
                return this.shell2.getInvertedOut();
            }
            if (this.shell != null) {
                return this.shell.getInputStream();
            }
            return this.in;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public OutputStream output() {
        try {
            if (this.shell1 != null) {
                return this.shell1.getOutputStream();
            }
            if (this.shell2 != null) {
                return this.shell2.getInvertedIn();
            }
            if (this.shell != null) {
                return this.shell.getOutputStream();
            }
            return this.out;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }

}