package cn.oyzh.easyshell.controller.tool;

import cn.oyzh.common.network.NetworkUtil;
import cn.oyzh.common.thread.DownLatch;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.NumberUtil;
import cn.oyzh.easyshell.ShellConst;
import cn.oyzh.easyshell.dto.ShellPortScanResult;
import cn.oyzh.easyshell.fx.ShellPortScanResultTableView;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.gui.text.field.NumberTextField;
import cn.oyzh.fx.gui.text.field.PortTextField;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.controls.button.FXButton;
import cn.oyzh.fx.plus.controls.label.FXLabel;
import cn.oyzh.fx.plus.controls.text.area.FXTextArea;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;
import org.apache.commons.net.telnet.TelnetClient;

import java.io.File;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;


/**
 * ssh工具箱业务
 *
 * @author oyzh
 * @since 2023/11/09
 */
@StageAttribute(
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "tool/shellTool.fxml"
)
public class ShellToolController extends StageController {

    /**
     * telnet地址
     */
    @FXML
    private ClearableTextField telnetHost;

    /**
     * telnet端口
     */
    @FXML
    private PortTextField telnetPort;

    /**
     * telnet超时
     */
    @FXML
    private NumberTextField telnetTimeout;

    /**
     * telnet文本域
     */
    @FXML
    private FXTextArea telnetArea;

    /**
     * 缓存文本域
     */
    @FXML
    private FXTextArea cacheArea;

    /**
     * 端口扫描消息
     */
    @FXML
    private FXLabel portScanMsg;

    /**
     * 端口扫描线程
     */
    private Thread portScanThread;

    /**
     * 端口扫描停止按钮
     */
    @FXML
    private FXButton portScanStopBtn;

    /**
     * 端口扫描开始按钮
     */
    @FXML
    private FXButton portScanStartBtn;


    /**
     * 端口扫描地址
     */
    @FXML
    private ClearableTextField portScanHost;

    /**
     * 端口扫描开始端口
     */
    @FXML
    private PortTextField portScanStartPort;

    /**
     * 端口扫描结束端口
     */
    @FXML
    private PortTextField portScanEndPort;

    /**
     * 端口扫描表
     */
    @FXML
    private ShellPortScanResultTableView portScanTable;

    @Override
    public void onWindowShown(WindowEvent event) {
        this.stage.switchOnTab();
        this.stage.hideOnEscape();
    }

    @Override
    public String getViewTitle() {
        return I18nHelper.tools();
    }

    /**
     * 计算缓存
     */
    @FXML
    private void calcCache() {
        StageManager.showMask(() -> {
            try {
                this.cacheArea.setText("calc cache start.");
                File dir = new File(ShellConst.getCachePath());
                this.doCalcCache(dir, new AtomicInteger(0), new LongAdder());
            } finally {
                this.cacheArea.appendLine("calc cache finish.");
            }
        });
    }

    /**
     * 计算缓存
     *
     * @param file      文件
     * @param fileCount 文件总数
     * @param fileSize  文件大小
     */
    private void doCalcCache(File file, AtomicInteger fileCount, LongAdder fileSize) {
        if (file.isFile()) {
            fileSize.add(file.length());
            fileCount.incrementAndGet();
            String sizeInfo = NumberUtil.formatSize(fileSize.longValue());
            FXUtil.runWait(() -> this.cacheArea.setText("find file: " + fileCount.get() + " total size: " + sizeInfo));
        } else {
            File[] files = file.listFiles();
            if (files != null) {
                for (File file1 : files) {
                    this.doCalcCache(file1, fileCount, fileSize);
                }
            }
        }
    }

    /**
     * 清理缓存
     */
    @FXML
    private void clearCache() {
        StageManager.showMask(() -> {
            try {
                this.cacheArea.setText("clear cache start.");
                File dir = new File(ShellConst.getCachePath());
                this.doClearCache(dir, new AtomicInteger(0), new LongAdder());
            } finally {
                this.cacheArea.appendLine("clear cache finish.");
            }
        });
    }

    /**
     * 清理缓存
     *
     * @param file      文件
     * @param fileCount 文件总数
     * @param fileSize  文件大小
     */
    private void doClearCache(File file, AtomicInteger fileCount, LongAdder fileSize) {
        if (file.isFile()) {
            fileSize.add(file.length());
            fileCount.incrementAndGet();
            String sizeInfo = NumberUtil.formatSize(fileSize.longValue());
            file.delete();
            FXUtil.runWait(() -> this.cacheArea.setText("delete file: " + fileCount.get() + " total size: " + sizeInfo));
        } else {
            File[] files = file.listFiles();
            if (files != null) {
                for (File file1 : files) {
                    this.doClearCache(file1, fileCount, fileSize);
                }
            }
        }
    }

    /**
     * 执行telnet
     */
    @FXML
    private void execTelnet() {
        if (!this.telnetHost.validate()) {
            return;
        }
        StageManager.showMask(() -> {
            try {
                // 清除记录
                this.telnetArea.clear();
                // 超时时间
                int timeout = this.telnetTimeout.getIntValue();
                // 创建客户端
                TelnetClient client = new TelnetClient();
                // 设置超时
                client.setConnectTimeout(timeout * 1000);
                // 执行连接
                client.connect(this.telnetHost.getTextTrim(), this.telnetPort.getIntValue());
                // 连接成功
                if (client.isConnected()) {
                    DownLatch latch = DownLatch.of();
                    Thread thread = ThreadUtil.start(() -> {
                        try {
                            // 读取数据
                            InputStreamReader reader = new InputStreamReader(client.getInputStream());
                            int len;
                            char[] buffer = new char[1024];
                            while (true) {
                                len = reader.read(buffer, 0, buffer.length);
                                if (len == -1) {
                                    break;
                                }
                                String str = new String(buffer, 0, len);
                                this.telnetArea.appendText(str);
                                ThreadUtil.sleep(5);
                            }
                        } catch (Exception ex) {
                            if (!(ex instanceof InterruptedIOException)) {
                                ex.printStackTrace();
                                this.telnetArea.appendLine(ex.getMessage());
                            }
                        } finally {
                            latch.countDown();
                        }
                    });
                    // 设置等待超时
                    if (!latch.await(timeout, TimeUnit.SECONDS)) {
                        thread.interrupt();
                    }
                    // 断开连接
                    client.disconnect();
                } else {// 连接失败
                    // 断开连接
                    client.disconnect();
                    MessageBox.warn(I18nHelper.connectFail());
                }
            } catch (Exception ex) {
                this.telnetArea.appendLine(ex.getMessage());
                MessageBox.warn(I18nHelper.connectFail());
            }
        });
    }

    /**
     * 执行端口扫描
     */
    @FXML
    private void execPortScan() {
        if (!this.portScanHost.validate()) {
            return;
        }
        int startPort = this.portScanStartPort.getIntValue();
        int endPort = this.portScanEndPort.getIntValue();
        if (startPort >= endPort) {
            this.portScanStartPort.requestFocus();
            MessageBox.warn(I18nHelper.invalid());
            return;
        }
        this.portScanStartBtn.disable();
        this.portScanStopBtn.enable();
        this.portScanTable.clearItems();
        String host = this.portScanHost.getTextTrim();
        AtomicInteger totalCount = new AtomicInteger(0);
        AtomicInteger successCount = new AtomicInteger(0);
        this.portScanThread = NetworkUtil.scanAsync(startPort, endPort, host, (port, success) -> {
            totalCount.incrementAndGet();
            if (success) {
                successCount.incrementAndGet();
                ShellPortScanResult result = new ShellPortScanResult();
                result.setPort(port);
                result.setDesc(NetworkUtil.detectDesc(port));
                this.portScanTable.addItem(result);
            }
            this.portScanMsg.text(I18nHelper.scan() + ":" + totalCount.get() + " " + I18nHelper.available() + ":" + successCount.get());
        }, () -> {
            this.portScanStartBtn.enable();
            this.portScanStopBtn.disable();
        });
    }

    /**
     * 停止端口扫描
     */
    @FXML
    private void stopPortScan() {
        ThreadUtil.interrupt(this.portScanThread);
        this.portScanThread = null;
        this.portScanStartBtn.enable();
    }
}
