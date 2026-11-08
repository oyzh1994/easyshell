package cn.oyzh.easyshell.controller.connect.http;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellGroup;
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
 * 新增 HTTP 工作区。
 *
 * @author oyzh
 * @since 2026-11-07
 */
@StageAttribute(
        stageStyle = FXStageStyle.EXTENDED,
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "connect/http/shellAddHttpConnect.fxml"
)
public class ShellAddHttpConnectController extends StageController {

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
     * 分组
     */
    private ShellGroup group;

    /**
     * 连接存储
     */
    private final ShellConnectStore connectStore = ShellConnectStore.INSTANCE;

    @Override
    public void onWindowShown(WindowEvent event) {
        super.onWindowShown(event);
        this.group = this.getProp("group");
        this.connectTimeOut.setValue(30);
        this.stage.hideOnEscape();
    }

    @FXML
    private void add() {
        ShellConnect connect = this.buildConnect();
        if (connect == null) {
            return;
        }
        connect.setGroupId(this.group == null ? null : this.group.getGid());
        if (this.connectStore.insert(connect)) {
            ShellEventUtil.connectAdded(connect);
            this.setProp("connect", connect);
            this.closeWindow();
        } else {
            MessageBox.warn(I18nHelper.operationFail(), this.getWindow());
        }
    }

    /**
     * 构建连接
     *
     * @return 连接
     */
    private ShellConnect buildConnect() {
        String nameText = this.name.getTextTrim();
        if (StringUtil.isBlank(nameText)) {
            MessageBox.warn(I18nHelper.contentCanNotEmpty(), this.getWindow());
            return null;
        }
        ShellConnect connect = new ShellConnect();
        connect.setType(ShellPrototype.HTTP);
        connect.setOsType(ShellPrototype.HTTP);
        connect.setName(nameText);
        connect.setConnectTimeOut(this.connectTimeOut.getIntValue());
        connect.setRemark(this.remark.getTextTrim());
        return connect;
    }

    @Override
    public String getViewTitle() {
        return I18nHelper.connectAddTitle();
    }
}
