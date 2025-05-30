package cn.oyzh.easyshell.controller.tool;

import cn.oyzh.common.network.NetworkUtil;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.easyshell.dto.ShellNetworkScanResult;
import cn.oyzh.easyshell.fx.tool.ShellNetworkScanResultTableView;
import cn.oyzh.fx.gui.text.field.NumberTextField;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.SubStageController;
import cn.oyzh.fx.plus.controls.button.FXButton;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.stage.Modality;

import java.util.ArrayList;
import java.util.List;


/**
 * shell工具箱 网络扫描业务
 *
 * @author oyzh
 * @since 2025/05/29
 */
@StageAttribute(
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "tool/shellToolNetworkScanTab.fxml"
)
public class ShellToolNetworkScanTabController extends SubStageController {

    /**
     * 网络扫描线程
     */
    private Thread scanThread;

    /**
     * 网络扫描停止按钮
     */
    @FXML
    private FXButton scanStopBtn;

    /**
     * 端口扫描开始按钮
     */
    @FXML
    private FXButton scanStartBtn;

    /**
     * 网络扫描网段1
     */
    @FXML
    private NumberTextField scanSegment1;

    /**
     * 网络扫描网段2
     */
    @FXML
    private NumberTextField scanSegment2;

    /**
     * 网络扫描网段3
     */
    @FXML
    private NumberTextField scanSegment3;

    /**
     * 网络扫描开始地址
     */
    @FXML
    private NumberTextField scanStart;

    /**
     * 网络扫描结束地址
     */
    @FXML
    private NumberTextField scanEnd;

    /**
     * 网络扫描表
     */
    @FXML
    private ShellNetworkScanResultTableView scanTable;

    /**
     * 执行网络扫描
     */
    @FXML
    private void execNetworkScan() {
        if (!this.scanSegment1.validate()) {
            return;
        }
        if (!this.scanSegment2.validate()) {
            return;
        }
        if (!this.scanSegment3.validate()) {
            return;
        }
        if (!this.scanStart.validate()) {
            return;
        }
        if (!this.scanEnd.validate()) {
            return;
        }
        if (this.scanStart.getIntValue() > this.scanEnd.getIntValue()) {
            MessageBox.warn(I18nHelper.invalidData());
            this.scanStart.requestFocus();
            return;
        }
        this.scanStartBtn.disable();
        this.scanStopBtn.enable();
        this.scanTable.clearItems();

        String hostBase = this.scanSegment1.getIntValue() + "." + this.scanSegment2.getIntValue() + "." + this.scanSegment3.getIntValue();
        int ipEnd = this.scanEnd.getIntValue();
        int ipStart = this.scanStart.getIntValue();

        this.scanThread = ThreadUtil.start(() -> {
            try {
                for (int i = ipStart; i <= ipEnd; i++) {
                    if (ThreadUtil.isInterrupted()) {
                        break;
                    }
                    String host = hostBase + "." + i;
                    ShellNetworkScanResult result = new ShellNetworkScanResult();
                    result.setHost(host);
                    List<Runnable> tasks = new ArrayList<>();
                    tasks.add(() -> {
                        boolean ssh = NetworkUtil.reachable(host, NetworkUtil.SSH_PORT, 500);
                        result.setSshAvailable(ssh);
                    });
                    tasks.add(() -> {
                        boolean ftp = NetworkUtil.reachable(host, NetworkUtil.FTP_PORT, 500);
                        result.setFtpAvailable(ftp);
                    });
                    tasks.add(() -> {
                        boolean vnc = NetworkUtil.reachable(host, NetworkUtil.VNC_PORT, 500);
                        result.setVncAvailable(vnc);
                    });
                    tasks.add(() -> {
                        boolean rdp = NetworkUtil.reachable(host, NetworkUtil.RDP_PORT, 500);
                        result.setRdpAvailable(rdp);
                    });
                    tasks.add(() -> {
                        boolean http = NetworkUtil.reachable(host, NetworkUtil.HTTP_PORT, 500);
                        result.setHttpAvailable(http);
                    });
                    tasks.add(() -> {
                        boolean https = NetworkUtil.reachable(host, NetworkUtil.HTTPS_PORT, 500);
                        result.setHttpsAvailable(https);
                    });
                    tasks.add(() -> {
                        boolean telnet = NetworkUtil.reachable(host, NetworkUtil.TELNET_PORT, 500);
                        result.setTelnetAvailable(telnet);
                    });
                    tasks.add(() -> {
                        boolean rlogin = NetworkUtil.reachable(host, NetworkUtil.RLOGIN_PORT, 500);
                        result.setRloginAvailable(rlogin);
                    });
                    tasks.add(() -> {
                        boolean mysql = NetworkUtil.reachable(host, NetworkUtil.Mysql_PORT, 500);
                        result.setMysqlAvailable(mysql);
                    });
                    tasks.add(() -> {
                        boolean redis = NetworkUtil.reachable(host, NetworkUtil.Redis_PORT, 500);
                        result.setRedisAvailable(redis);
                    });
                    tasks.add(() -> {
                        boolean zookeeper = NetworkUtil.reachable(host, NetworkUtil.Zookeeper_PORT, 500);
                        result.setZookeeperAvailable(zookeeper);
                    });
                    tasks.add(() -> {
                        boolean oracle = NetworkUtil.reachable(host, NetworkUtil.Oracle_PORT, 500);
                        result.setOracleAvailable(oracle);
                    });
                    tasks.add(() -> {
                        boolean mongo = NetworkUtil.reachable(host, NetworkUtil.MongoDB_PORT, 500);
                        result.setMongoDBAvailable(mongo);
                    });
                    tasks.add(() -> {
                        boolean postgreSQL = NetworkUtil.reachable(host, NetworkUtil.PostgreSQL_PORT, 500);
                        result.setPostgreSQLAvailable(postgreSQL);
                    });
                    tasks.add(() -> {
                        boolean memcached = NetworkUtil.reachable(host, NetworkUtil.Memcached_PORT, 500);
                        result.setMemcachedAvailable(memcached);
                    });
                    tasks.add(() -> {
                        boolean elasticsearch = NetworkUtil.reachable(host, NetworkUtil.Elasticsearch_PORT, 500);
                        result.setElasticsearchAvailable(elasticsearch);
                    });
                    tasks.add(() -> {
                        boolean sqlServer = NetworkUtil.reachable(host, NetworkUtil.SQLServer_PORT, 500);
                        result.setSqlServerAvailable(sqlServer);
                    });
                    ThreadUtil.submitVirtual(tasks);
                    this.scanTable.addItem(result);
                }
            } finally {
                this.scanStartBtn.enable();
                this.scanStopBtn.disable();
            }
        });
    }

    /**
     * 停止网络扫描
     */
    @FXML
    private void stopNetworkScan() {
        ThreadUtil.interrupt(this.scanThread);
        this.scanThread = null;
        this.scanStart.enable();
        this.scanStopBtn.disable();
    }
}
