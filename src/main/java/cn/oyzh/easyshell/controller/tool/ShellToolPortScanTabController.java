package cn.oyzh.easyshell.controller.tool;

import cn.oyzh.common.network.NetworkUtil;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.easyshell.dto.ShellPortScanResult;
import cn.oyzh.easyshell.fx.tool.ShellPortScanResultTableView;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.gui.text.field.PortTextField;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.SubStageController;
import cn.oyzh.fx.plus.controls.button.FXButton;
import cn.oyzh.fx.plus.controls.label.FXLabel;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.stage.Modality;

import java.util.concurrent.atomic.AtomicInteger;


/**
 * shell工具箱 端口扫描业务
 *
 * @author oyzh
 * @since 2025/05/27
 */
@StageAttribute(
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "tool/shellToolPortScanTab.fxml"
)
public class ShellToolPortScanTabController extends SubStageController {

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
        this.portScanThread = NetworkUtil.scanMultiple(startPort, endPort, 500, 10, host, (port, success) -> {
            totalCount.incrementAndGet();
            if (success) {
                successCount.incrementAndGet();
                ShellPortScanResult result = new ShellPortScanResult();
                result.setPort(port);
                result.setDesc(NetworkUtil.detectDesc(port));
                this.portScanTable.addItem(result);
                this.portScanTable.doSort();
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
        this.portScanStopBtn.disable();
    }
}
