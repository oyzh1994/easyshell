package cn.oyzh.easyshell.tabs.home;

import cn.oyzh.common.dto.Project;
import cn.oyzh.easyshell.event.ShellEventUtil;
import cn.oyzh.easyshell.util.ShellViewFactory;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import cn.oyzh.fx.plus.controls.text.FXText;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * shell主页tab内容组件
 *
 * @author oyzh
 * @since 2023/6/24
 */
public class ShellHomeTabController extends RichTabController {

    /**
     * 版本
     */
    @FXML
    private FXText version;

    /**
     * 更新日期
     */
    @FXML
    private FXText update;

    /**
     * 环境信息
     */
    @FXML
    private FXText jdkInfo;

    /**
     * 项目对象
     */
    private final Project project = Project.load();

    @Override
    public void initialize(URL url, ResourceBundle resource) {
        super.initialize(url, resource);
//        this.softInfo.setText(I18nHelper.soft() + ": v" + this.project.getVersion() + " Powered by oyzh.");
        this.version.setText(I18nHelper.version() + " : v" + this.project.getVersion());
        this.update.setText(I18nHelper.updateDate() + " : " + this.project.getUpdateDate());
        String jdkInfo = "";
        if (System.getProperty("java.vm.name") != null) {
            jdkInfo += System.getProperty("java.vm.name");
        }
        if (System.getProperty("java.vm.version") != null) {
            jdkInfo += System.getProperty("java.vm.version");
        }
        this.jdkInfo.setText(I18nHelper.env() + " : " + jdkInfo);
    }

    /**
     * 新增连接
     */
    @FXML
    private void addConnect() {
        //        ShellEventUtil.showAddConnect();
        ShellViewFactory.addConnectGuid(null);
    }

    /**
     * 添加分组
     */
    @FXML
    private void addGroup() {
        ShellEventUtil.addGroup();
    }

    /**
     * 打开终端
     */
    @FXML
    private void openTerminal() {
        ShellEventUtil.showTerminal();
    }

    /**
     * 更新日志
     */
    @FXML
    private void changelog() {
        ShellEventUtil.changelog();
    }

    /**
     * 打开终端
     */
    @FXML
    private void splitView() {
        ShellViewFactory.splitGuid();
    }

    @Override
    public void onTabInit(FXTab tab) {
        super.onTabInit(tab);
        tab.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
            if (t1 != null && t1) {
                ShellEventUtil.layout2();
            }
        });
    }
}
