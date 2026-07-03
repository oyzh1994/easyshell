package cn.oyzh.easyshell.test.mosh;

import com.jediterm.terminal.TtyConnector;
import org.mosh4j.core.MoshTerminalFrontend;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class MoshTtyConnector implements TtyConnector {

    private final MoshTerminalFrontend frontend;
    // 用于线程间安全地传递从 mosh 收到的数据
    private final BlockingQueue<byte[]> dataQueue = new ArrayBlockingQueue<>(1000);
    private volatile boolean isRunning = true;

    public MoshTtyConnector(MoshTerminalFrontend frontend) {
        this.frontend = frontend;
        // 启动一个后台线程，持续从 mosh4j 读取数据并放入队列
        startReaderThread();
    }

    private void startReaderThread() {
        Thread reader = new Thread(() -> {
            try {
                while (isRunning && frontend.isRunning()) {
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

    @Override
    public int read(char[] buf, int offset, int length) throws IOException {
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

    @Override
    public void write(byte[] bytes) throws IOException {
        // 用户输入：将键盘输入通过 mosh4j 发送给服务器
        frontend.sendUserInput(bytes);
        //frontend.sendHeartbeat();
    }

    @Override
    public void write(String string) throws IOException {
        this.write(string.getBytes());
    }

    @Override
    public boolean isConnected() {
        return false;
    }

    @Override
    public int waitFor() throws InterruptedException {
        return 0;
    }

    @Override
    public boolean ready() throws IOException {
        return false;
    }

    @Override
    public void close() {
        isRunning = false;
        frontend.close();
    }

    @Override
    public String getName() {
        return "Mosh Connection";
    }

}