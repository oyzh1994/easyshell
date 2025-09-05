package cn.oyzh.easyshell.controller.tool;

import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.ParentStageController;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;

import java.util.List;


/**
 * shell工具箱业务
 *
 * @author oyzh
 * @since 2025/03/09
 */
@StageAttribute(
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "tool/shellTool.fxml"
)
public class ShellToolController extends ParentStageController {

    /**
     * 缓存
     */
    @FXML
    private ShellToolCacheTabController cacheTabController;

    /**
     * telnet
     */
    @FXML
    private ShellToolTelnetTabController telnetTabController;

    /**
     * 端口扫描
     */
    @FXML
    private ShellToolPortScanTabController portScanTabController;

    /**
     * 网络扫描
     */
    @FXML
    private ShellToolNetworkScanTabController networkScanTabController;

    /**
     * zookeeper
     */
    @FXML
    private ShellToolZookeeperTabController zookeeperTabController;

    /**
     * x11
     */
    @FXML
    private ShellToolX11TabController x11TabController;

    @Override
    public void onWindowShown(WindowEvent event) {
        super.onWindowShown(event);
        this.stage.switchOnTab();
        this.stage.hideOnEscape();
    }

    @Override
    public String getViewTitle() {
        return I18nHelper.tools();
    }

    @Override
    public List<? extends StageController> getSubControllers() {
        return List.of(
                this.cacheTabController,
                this.telnetTabController,
                this.portScanTabController,
                this.networkScanTabController,
                this.zookeeperTabController,
                this.x11TabController
        );
    }
}
