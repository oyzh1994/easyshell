package cn.oyzh.easyshell.controller.connect.http;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.event.ShellEventUtil;
import cn.oyzh.easyshell.internal.ShellPrototype;
import cn.oyzh.easyshell.store.ShellConnectStore;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.gui.text.field.NumberTextField;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.controls.text.area.FXTextArea;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.FXStageStyle;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;

/**
 * 修改 HTTP 工作区。
 *
 * @author oyzh
 * @since 2026-11-07
 */
@StageAttribute(
        stageStyle = FXStageStyle.EXTENDED,
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "connect/http/shellUpdateHttpConnect.fxml"
)
public class ShellUpdateHttpConnectController extends StageController {

    /**
     * 名称
     */
    @FXML
    private ClearableTextField name;

    /**
     * 连接超时
     */
    @FXML
    private NumberTextField connectTimeOut;

    /**
     * 备注
     */
    @FXML
    private FXTextArea remark;

    /**
     * 连接
     */
    private ShellConnect shellConnect;

    /**
     * 连接存储
     */
    private final ShellConnectStore connectStore = ShellConnectStore.INSTANCE;

    @Override
    public void onWindowShown(WindowEvent event) {
        super.onWindowShown(event);
        this.shellConnect = this.getProp("shellConnect");
        this.name.setText(this.shellConnect.getName());
        this.connectTimeOut.setValue(this.shellConnect.getConnectTimeOut());
        this.remark.setText(this.shellConnect.getRemark());
        this.stage.hideOnEscape();
    }

    @FXML
    private void update() {
        String nameText = this.name.getTextTrim();
        if (StringUtil.isBlank(nameText)) {
            MessageBox.warn(I18nHelper.contentCanNotEmpty(), this.getWindow());
            return;
        }
        this.shellConnect.setName(nameText);
        // HTTP 连接不再配置基础地址，请求使用完整 URL 或环境变量
        this.shellConnect.setHost(null);
        this.shellConnect.setConnectTimeOut(this.connectTimeOut.getIntValue());
        this.shellConnect.setRemark(this.remark.getTextTrim());
        this.shellConnect.setType(ShellPrototype.HTTP);
        this.shellConnect.setOsType(ShellPrototype.HTTP);
        if (this.connectStore.replace(this.shellConnect)) {
            ShellEventUtil.connectUpdated(this.shellConnect);
            this.closeWindow();
        } else {
            MessageBox.warn(I18nHelper.operationFail(), this.getWindow());
        }
    }

    @Override
    public String getViewTitle() {
        return I18nHelper.connectUpdateTitle();
    }
}
