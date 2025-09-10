package cn.oyzh.easyshell.zmodem;

import cn.oyzh.common.file.FileUtil;
import cn.oyzh.common.util.NumberUtil;
import cn.oyzh.fx.plus.chooser.DirChooserHelper;
import cn.oyzh.fx.plus.chooser.FXChooser;
import cn.oyzh.fx.plus.chooser.FileChooserHelper;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import com.jediterm.terminal.Terminal;
import org.apache.commons.net.io.CopyStreamEvent;
import org.apache.commons.net.io.CopyStreamListener;
import zmodem.FileCopyStreamEvent;
import zmodem.ZModem;
import zmodem.util.CustomFile;
import zmodem.util.EmptyFileAdapter;
import zmodem.util.FileAdapter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * ZModem处理器
 *
 * @author oyzh
 * @since 2025/06/24
 */
public class ShellZModemProcessor implements CopyStreamListener {
    // 如果为 true 表示是接收（sz）文件
    private final boolean sz;
    private final ZModem zmodem;
    private long lastRefreshTime;
    private final Terminal terminal;

    public ShellZModemProcessor(char[] frame, InputStream input, OutputStream output, Terminal terminal) {
        // sz: * * 0x18 B 0 0
        // rz: * * 0x18 B 0 1
        this.sz = frame[5] == 48;
        this.terminal = terminal;
        ShellZModemInputStream in = new ShellZModemInputStream(input, new String(frame).getBytes());
        this.zmodem = new ZModem(in, output);
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
            this.terminal.nextLine();
        }
        // 是否跳过
        boolean skip = event.isSkip();
        // 文件总数
        long total = event.getRemaining() + event.getIndex() - 1;
        StringBuilder sb = new StringBuilder();

        // 文件索引
        sb.append("\t");
        sb.append(event.getIndex());
        sb.append("/");
        sb.append(total);

        // 文件名
        sb.append("\t");
        sb.append(event.getFilename());

        // 文件大小
        sb.append("\t");
        sb.append(event.getBytesTransferred()).append("/").append(event.getTotalBytesTransferred());
        // 当前传输是否完成
        boolean completed = false;
        // 是否全部结束了
        boolean allFinished;
        // 处理进度
        sb.append("\t");
        if (skip) {// 跳过
            sb.append(I18nHelper.skip()).append("\r");
            allFinished = event.getIndex() == total;
        } else {// 进度
            completed = event.getBytesTransferred() >= event.getTotalBytesTransferred();
            double rate = (event.getBytesTransferred() * 1.0 / event.getTotalBytesTransferred()) * 100.0;
            sb.append(NumberUtil.scale(rate, 2)).append('%').append("\r");
            allFinished = completed && event.getIndex() == total;
        }

        // 全部结束了，则跳过
        if (allFinished) {
            return;
        }

        // 刷新屏幕
        long now = System.currentTimeMillis();
        // 达到刷新阈值或当前文件传输完成或跳过，则执行刷新
        if (now - this.lastRefreshTime > 200 || completed || skip) {
            this.lastRefreshTime = now;
            this.terminal.saveCursor();
            this.terminal.writeCharacters(sb.toString());
            this.terminal.restoreCursor();
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
            List<File> files = FileChooserHelper.chooseMultiple(
                    I18nHelper.pleaseChooseFile(),
                    FXChooser.allExtensionFilter(),
                    StageManager.getFrontWindow()
            );
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
                this.refreshProgress((FileCopyStreamEvent) event);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void bytesTransferred(long totalBytesTransferred, int bytesTransferred, long streamSize) {
    }

    public void cancel() throws IOException {
        this.zmodem.cancel();
    }
}