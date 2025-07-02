package cn.oyzh.easyshell.zmodem;

import cn.oyzh.common.file.FileUtil;
import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.NumberUtil;
import cn.oyzh.easyshell.terminal.ShellDefaultTtyConnector;
import cn.oyzh.fx.plus.chooser.DirChooserHelper;
import cn.oyzh.fx.plus.chooser.FXChooser;
import cn.oyzh.fx.plus.chooser.FileChooserHelper;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.i18n.I18nHelper;
import com.jediterm.terminal.Terminal;
import com.jediterm.terminal.TtyConnector;
import org.apache.commons.net.io.CopyStreamEvent;
import org.apache.commons.net.io.CopyStreamListener;
import zmodem.FileCopyStreamEvent;
import zmodem.ZModem;
import zmodem.util.CustomFile;
import zmodem.util.EmptyFileAdapter;
import zmodem.util.FileAdapter;
import zmodem.xfer.zm.util.ZModemCharacter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * zmodem协议tty连接器
 *
 * @author oyzh
 * @since 2025/06/24
 */
public class ZModemTtyConnector implements TtyConnector {

    /**
     * zmodem协议前缀
     */
    public static final char[] ZMODEM_PREFIX = new char[]{
            (char) ZModemCharacter.ZPAD.value(),
            (char) ZModemCharacter.ZPAD.value(),
            (char) ZModemCharacter.ZDLE.value()
    };

    /**
     * 终端容器
     */
    private Terminal terminal;

    /**
     * zmodem处理器
     */
    private volatile ZModemProcessor zmodem;

    /**
     * tty连接器
     */
    private ShellDefaultTtyConnector connector;

    public ShellDefaultTtyConnector getConnector() {
        return connector;
    }

    public ZModemTtyConnector(Terminal terminal, ShellDefaultTtyConnector connector) {
        this.terminal = terminal;
        this.connector = connector;
    }

    @Override
    public int read(char[] buffer, int offset, int length) throws IOException {
        // 处理zomdem
        if (this.zmodem != null) {
            this.zmodem.process();
            this.zmodem = null;
        }

        int i = this.connector.read(buffer, offset, length);
        if (i < 1) {
            return i;
        }

        char[] bufferSlice = Arrays.copyOfRange(buffer, 0, i);
        int e = indexOfZmodem(bufferSlice);
        if (e == -1) {
            return i;
        }
        char[] zmodemFrame = Arrays.copyOfRange(buffer, e, i);
        // 创建zmode处理器
        this.zmodem = new ZModemProcessor(
                // sz: * * 0x18 B 0 0
                // rz: * * 0x18 B 0 1
                zmodemFrame.length > 5 && zmodemFrame[5] == 48,
                new ZModemInputStream(this.connector.input(), new String(zmodemFrame).getBytes()),
                this.connector.output()
        );
        return e;
    }

    @Override
    public void write(byte[] bytes) throws IOException {
        // 取消zmodem
        if (this.zmodem != null && bytes.length > 0 && bytes[0] == 0x03) {
            this.zmodem.cancel();
            return;
        }
        this.connector.write(bytes);
    }

    @Override
    public void write(String string) throws IOException {
        this.connector.write(string);
    }

    @Override
    public boolean isConnected() {
        return this.connector != null && this.connector.isConnected();
    }

    @Override
    public int waitFor() throws InterruptedException {
        return this.connector.waitFor();
    }

    @Override
    public boolean ready() throws IOException {
        return this.connector.ready();
    }

    @Override
    public String getName() {
        return this.connector.getName();
    }

    @Override
    public void close() {
        if (this.connector != null) {
            this.zmodem = null;
            this.terminal = null;
            this.connector = null;
            if (JulLog.isInfoEnabled()) {
                JulLog.info("close zmodem tty");
            }
        }
    }

    /**
     * 获取zmodem协议前缀位置
     *
     * @param a 数组
     * @return 结果
     */
    private static int indexOfZmodem(char[] a) {
        if (a.length < ZMODEM_PREFIX.length) {
            return -1;
        }
        for (int i = 0; i <= a.length - ZMODEM_PREFIX.length; i++) {
            char[] range = Arrays.copyOfRange(a, i, i + ZMODEM_PREFIX.length);
            if (Arrays.equals(range, ZMODEM_PREFIX)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * zmodem输入流
     *
     * @author oyzh
     * @since 20025/06/24
     */
    private static class ZModemInputStream extends InputStream {
        private final InputStream input;
        private final byte[] buffer;
        private int index = 0;

        public ZModemInputStream(InputStream input, byte[] buffer) {
            this.input = input;
            this.buffer = buffer;
        }

        @Override
        public int read() throws IOException {
            if (this.index < buffer.length) {
                return this.buffer[index++];
            }
            return this.input.read();
        }
    }

    /**
     * zmodem处理器
     *
     * @author oyzh
     * @since 20025/06/24
     */
    private class ZModemProcessor implements CopyStreamListener {
        // 如果为 true 表示是接收（sz）文件
        private final boolean sz;
        private final ZModem zmodem;
        private long lastRefreshTime = 0L;

        public ZModemProcessor(boolean sz, InputStream input, OutputStream output) {
            this.sz = sz;
            this.zmodem = new ZModem(input, output, connector);
        }

        /**
         * 处理
         */
        public void process() {
            try {
                if (this.sz) {
                    this.receive();
                } else {
                    this.send();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                MessageBox.exception(ex);
            }
        }

        /**
         * 接收
         *
         * @throws IOException 异常
         */
        private void receive() throws IOException {
            this.zmodem.receive(() -> {
                File file = this.openDirDialog();
                if (file != null) {
                    FileUtil.forceMkdir(file);
                }
                return file == null ? EmptyFileAdapter.INSTANCE : new CustomFile(file);
            }, this);
        }

        /**
         * 发送
         *
         * @throws Exception 异常
         */
        private void send() throws Exception {
            this.zmodem.send(() -> {
                List<FileAdapter> files = new ArrayList<>();
                List<File> fileList = this.openFileDialog();
                for (File file : fileList) {
                    files.add(new CustomFile(file));
                }
                return files;
            }, this);
        }

        /**
         * 当前文件索引
         */
        private int curIndex = -1;

        /**
         * 刷新进度
         *
         * @param event 事件
         * @throws IOException 异常
         */
        private void refreshProgress(FileCopyStreamEvent event) throws IOException {
            // 文件索引变化，则换行
            if (this.curIndex != event.getIndex()) {
                this.curIndex = event.getIndex();
                terminal.nextLine();
            }
            // 是否跳过
            boolean skip = event.isSkip();
            // 文件总数
            long total = event.getRemaining() + event.getIndex() - 1;
            StringBuilder sb = new StringBuilder();

            // 文件索引
            sb.append(ControlCharacters.TAB);
            sb.append(event.getIndex());
            sb.append("/");
            sb.append(total);

            // 文件名
            sb.append(ControlCharacters.TAB);
            sb.append(event.getFilename());

            // 文件大小
            sb.append(ControlCharacters.TAB);
            sb.append(String.format("%d/%d", event.getBytesTransferred(), event.getTotalBytesTransferred()));
            sb.append(ControlCharacters.TAB);

            // 当前传输是否完成
            boolean completed = false;
            // 处理进度
            if (skip) {// 跳过
                sb.append(I18nHelper.skip()).append(ControlCharacters.CR);
            } else {// 进度
                completed = event.getBytesTransferred() >= event.getTotalBytesTransferred();
                double rate = (event.getBytesTransferred() * 1.0 / event.getTotalBytesTransferred()) * 100.0;
                sb.append(NumberUtil.scale(rate, 2)).append('%').append(ControlCharacters.CR);
            }
            
            // 刷新屏幕
            long now = System.currentTimeMillis();
            // 达到刷新阈值或当前文件传输完成或跳过，则执行刷新
            if (now - this.lastRefreshTime > 200 || completed || skip) {
                this.lastRefreshTime = now;
                terminal.saveCursor();
                terminal.writeCharacters(sb.toString());
                terminal.restoreCursor();
            }
        }

        /**
         * 文件选择器
         *
         * @return 文件
         */
        private List<File> openFileDialog() {
            CompletableFuture<List<File>> future = new CompletableFuture<>();
            try {
                List<File> files = FileChooserHelper.chooseMultiple(I18nHelper.pleaseChooseFile(), FXChooser.allExtensionFilter());
                future.complete(files);
                return future.get();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return Collections.emptyList();
        }

        /**
         * 文件夹选择器
         *
         * @return 文件夹
         */
        private File openDirDialog() {
            CompletableFuture<File> future = new CompletableFuture<>();
            try {
                File file = DirChooserHelper.chooseDownload(I18nHelper.pleaseChooseDir());
                future.complete(file);
                return future.get();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return null;
        }

        @Override
        public void bytesTransferred(CopyStreamEvent event) {
            if (event instanceof FileCopyStreamEvent) {
                try {
                    refreshProgress((FileCopyStreamEvent) event);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        @Override
        public void bytesTransferred(long totalBytesTransferred, int bytesTransferred, long streamSize) {
            // TODO("Not yet implemented")
        }

        public void cancel() throws IOException {
            zmodem.cancel();
        }
    }
}