package cn.oyzh.easyshell.mosh;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.thread.TaskManager;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.IOUtil;
import cn.oyzh.fx.tty.TtyDefaultTtyConnector;
import com.pty4j.PtyProcess;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.Future;

/**
 * @author oyzh
 * @since 2025-03-04
 */
public class ShellMoshTtyConnector extends TtyDefaultTtyConnector {

    /**
     * mosh客户端
     */
    private ShellMoshClient client;

    /**
     * 读取器
     */
    private InputStreamReader shellReader;

    public ShellMoshClient getClient() {
        return client;
    }

    private InputStream input;
    private OutputStream output;
    private Future<?> heartbeat;

    public void init(ShellMoshClient client) throws Exception {
        this.client = client;

        final int pipeCapacity = 65536;

        // Pipe: render thread → terminal display
        PipedOutputStream hostOutputPipe = new PipedOutputStream();
        PipedInputStream hostInputPipe = new PipedInputStream(hostOutputPipe, pipeCapacity);

        // Render thread: 驱动 UDP 接收 + 消费 StatefulAnsiRenderer 渲染帧（含颜色）
        Thread outputThread = new Thread(() -> {
            while (this.client != null && this.client.isConnected()) {
                byte[] bytes = this.client.pollHostBytes();
                if (bytes != null) {
                    try {
                        hostOutputPipe.write(bytes);
                        hostOutputPipe.flush();
                    } catch (IOException e) {
                        break;
                    }
                } else {
                    ThreadUtil.sleep(40);
                }
            }
        }, "mosh-ouput");
        outputThread.setDaemon(true);
        outputThread.start();

        //        // Pipe: terminal keystrokes → Mosh frontend
        //        PipedOutputStream keyOutputPipe = new PipedOutputStream();
        //        PipedInputStream keyInputPipe = new PipedInputStream(keyOutputPipe, pipeCapacity);
        //        // Input thread: read terminal keyboard input → send to Mosh frontend
        //        Thread inputThread = new Thread(() -> {
        //            byte[] buffer = new byte[4096];
        //            while (this.client != null && this.client.isConnected()) {
        //                try {
        //                    int len = keyInputPipe.read(buffer);
        //                    if (len > 0) {
        //                        byte[] data = new byte[len];
        //                        System.arraycopy(buffer, 0, data, 0, len);
        //                        this.client.sendUserInput(data);
        //                        System.out.println(new String(data));
        //                    } else {
        //                        ThreadUtil.sleep(40);
        //                    }
        //                } catch (IOException e) {
        //                    break;
        //                }
        //            }
        //        }, "mosh-input");
        //        inputThread.setDaemon(true);
        //        inputThread.start();

        // 初始化
        this.input = hostInputPipe;
        this.output = hostOutputPipe;
        this.shellReader = new InputStreamReader(hostInputPipe, this.myCharset);

        // 定时发送心跳
        this.heartbeat = TaskManager.startInterval(this.client::sendHeartbeat, 15_000);
    }

    public ShellMoshTtyConnector(PtyProcess process, Charset charset, List<String> commandLines) {
        super(process, charset, commandLines);
    }

    @Override
    public int read(char[] buf, int offset, int length) throws IOException {
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
    }

    @Override
    public void write(String str) throws IOException {
        if (JulLog.isDebugEnabled()) {
            JulLog.debug("shell write : {}", str);
        }
        if (this.client != null) {
            this.client.sendUserInput(str.getBytes());
        }
    }

    @Override
    public void write(byte[] bytes) throws IOException {
        if (this.client != null) {
            this.client.sendUserInput(bytes);
        }
    }

    @Override
    public void close() {
        super.close();
        this.client = null;
        if (this.heartbeat != null) {
            TaskManager.cancel(this.heartbeat);
            this.heartbeat = null;
        }
        if (this.shellReader != null) {
            IOUtil.close(this.shellReader);
            this.shellReader = null;
        }
    }

    @Override
    public InputStream input() {
        return this.input;
    }

    @Override
    public OutputStream output() {
        return this.output;
    }
}