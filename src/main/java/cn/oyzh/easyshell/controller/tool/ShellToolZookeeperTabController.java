package cn.oyzh.easyshell.controller.tool;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.util.zk.ShellZKAuthUtil;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.plus.controller.SubStageController;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.validator.ValidatorUtil;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;


/**
 * shell工具箱 zookeeper业务
 *
 * @author oyzh
 * @since 2025/05/29
 */
public class ShellToolZookeeperTabController extends SubStageController {

    /**
     * 用户
     */
    @FXML
    private ClearableTextField user;

    /**
     * 密码
     */
    @FXML
    private ClearableTextField pwd;

    /**
     * 摘要
     */
    @FXML
    private TextField digest;

    /**
     * 生成摘要
     */
    @FXML
    private void genDigest() {
        try {
            if (StringUtil.isBlank(this.user.getText())) {
//                this.user.requestFocus();
                ValidatorUtil.validFail(this.user);
                return;
            }
            if (StringUtil.isBlank(this.pwd.getText())) {
//                this.pwd.requestFocus();
                ValidatorUtil.validFail(this.pwd);
                return;
            }
            String digest1 = ShellZKAuthUtil.digest(this.user.getText(), this.pwd.getText());
            this.digest.setText(digest1);
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    /**
     * 复制摘要
     */
    @FXML
    private void copyDigest() {
        this.digest.copy();
        MessageBox.info(I18nHelper.copySuccess());
    }

    @Override
    protected void bindListeners() {
        super.bindListeners();
        this.user.addTextChangeListener((observableValue, s, t1) -> {
            // 内容包含“:”，则直接切割字符为用户名密码
            if (t1 != null && t1.contains(":")) {
                this.user.setText(t1.split(":")[0]);
                this.pwd.setText(t1.split(":")[1]);
            } else {
                this.digest.clear();
            }
        });
        this.pwd.addTextChangeListener((observableValue, s, t1) -> this.digest.clear());
    }
}
