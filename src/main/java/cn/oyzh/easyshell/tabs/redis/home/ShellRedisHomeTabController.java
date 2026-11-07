package cn.oyzh.easyshell.tabs.redis.home;

import cn.oyzh.easyshell.dto.redis.ShellRedisInfoProp;
import cn.oyzh.easyshell.redis.ShellRedisClient;
import cn.oyzh.easyshell.tabs.redis.ShellRedisTabPane;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.plus.controls.label.FXLabel;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import javafx.fxml.FXML;

/**
 * redis主页标签页控制器
 *
 * @author oyzh
 * @since 2026-11-07
 */
public class ShellRedisHomeTabController extends RichTabController {

    /**
     * 客户端
     */
    private ShellRedisClient client;

    @FXML
    private FXLabel version;

    @FXML
    private FXLabel mode;

    @FXML
    private FXLabel role;

    @FXML
    private FXLabel databases;

    @FXML
    private FXLabel keyCount;

    @FXML
    private FXLabel usedMemory;

    @FXML
    private FXLabel clients;

    @FXML
    private FXLabel uptime;

    @FXML
    private FXLabel commands;

    @Override
    public void onTabInit(FXTab tab) {
        super.onTabInit(tab);
        tab.tabPaneProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue instanceof ShellRedisTabPane tabPane) {
                if (tabPane.getClient() != null) {
                    this.client = tabPane.getClient();
                    this.initInfo();
                } else {
                    tabPane.clientProperty().addListener((observable1, oldValue1, newValue1) -> {
                        if (newValue1 != null) {
                            this.client = newValue1;
                            this.initInfo();
                        }
                    });
                }
            }
        });
        super.flushTab();
    }

    /**
     * 初始化信息
     */
    private void initInfo() {
        try {
            if (this.client == null || this.client.isClosed()) {
                return;
            }
            ShellRedisInfoProp prop = new ShellRedisInfoProp();
            prop.parse(this.client.info(null));
            this.version.text(prop.getRedisVersion());
            this.mode.text(prop.getRedisMode());
            try {
                this.role.text(String.valueOf(this.client.role().getFirst()));
            } catch (Exception ex) {
                this.role.text("N/A");
            }
            Integer dbCount = this.client.databases();
            this.databases.text(dbCount == null ? "1" : String.valueOf(dbCount));
            Long keyCount = prop.keyCount();
            this.keyCount.text(keyCount == null ? "N/A" : String.valueOf(keyCount));
            this.usedMemory.text(prop.getUsedMemoryHuman());
            this.clients.text(String.valueOf(prop.getConnectedClients()));
            this.uptime.text(ShellI18nHelper.uptimeDays(prop.getUptimeInSeconds() / 86400));
            this.commands.text(String.valueOf(prop.getTotalCommandsProcessed()));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
