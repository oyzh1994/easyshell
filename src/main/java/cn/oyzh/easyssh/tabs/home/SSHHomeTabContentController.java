package cn.oyzh.easyssh.tabs.home;

import cn.oyzh.common.dto.Project;
import cn.oyzh.easyfx.controls.FXLabel;
import cn.oyzh.easyfx.event.EventUtil;
import cn.oyzh.easyssh.ssh.SSHEvents;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * ssh主页tab内容组件
 *
 * @author oyzh
 * @since 2023/6/24
 */
@Lazy
@Component
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
    @Autowired
    private Project project;

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
        EventUtil.fire(SSHEvents.SSH_ADD_CONNECT);
    }

    /**
     * 添加分组
     */
    @FXML
    private void addGroup() {
        EventUtil.fire(SSHEvents.SSH_ADD_GROUP);
    }

    /**
     * 打开终端
     */
    @FXML
    private void openTerminal() {
        EventUtil.fire(SSHEvents.SSH_OPEN_TERMINAL);
    }

}
