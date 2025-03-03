package cn.oyzh.easyssh.tabs.home;

import cn.oyzh.common.dto.Project;
import cn.oyzh.easyssh.event.SSHEventUtil;
import cn.oyzh.fx.plus.controls.label.FXLabel;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * ssh主页tab内容组件
 *
 * @author oyzh
 * @since 2023/6/24
 */
public class SSHHomeTabContentController implements Initializable {

    /**
     * 软件信息
     */
    @FXML
    private FXLabel softInfo;

    /**
     * 环境信息
     */
    @FXML
    private FXLabel jdkInfo;

    /**
     * 项目对象
     */
    private Project project = Project.load();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.softInfo.setText("程序信息 v" + this.project.getVersion() + " Powered by oyzh.");
        String jdkInfo = "";
        if (System.getProperty("java.vm.name") != null) {
            jdkInfo += System.getProperty("java.vm.name");
        }
        if (System.getProperty("java.vm.version") != null) {
            jdkInfo += System.getProperty("java.vm.version");
        }
        this.jdkInfo.setText("环境信息 " + jdkInfo);
    }

    /**
     * 新增连接
     */
    @FXML
    private void addConnect() {
        SSHEventUtil.showAddConnect();
    }

    /**
     * 添加分组
     */
    @FXML
    private void addGroup() {
        SSHEventUtil.addGroup();
    }
}
