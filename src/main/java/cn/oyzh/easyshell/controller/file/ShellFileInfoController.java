package cn.oyzh.easyshell.controller.file;

import cn.oyzh.common.util.NumberUtil;
import cn.oyzh.easyshell.file.ShellFile;
import cn.oyzh.fx.gui.text.field.ReadOnlyTextField;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.window.FXStageStyle;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;

/**
 * 文件信息业务
 *
 * @author oyzh
 * @since 2025/03/16
 */
@StageAttribute(
        stageStyle = FXStageStyle.EXTENDED,
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "file/shellFileInfo.fxml"
)
public class ShellFileInfoController extends StageController {

    /**
     * 分组
     */
    @FXML
    private ReadOnlyTextField group;

    /**
     * 拥有者
     */
    @FXML
    private ReadOnlyTextField owner;

    /**
     * 名称
     */
    @FXML
    private ReadOnlyTextField name;

    /**
     * 大小
     */
    @FXML
    private ReadOnlyTextField size;

    /**
     * 权限
     */
    @FXML
    private ReadOnlyTextField permissions;

    @Override
    public void onWindowShown(WindowEvent event) {
        super.onWindowShown(event);
        ShellFile file = this.getProp("file");
        this.group.setText(file.getGroup());
        this.owner.setText(file.getOwner());
        this.name.setText(file.getFileName());
        this.permissions.setText(file.getPermissions());
        if (file.isDirectory()) {
            // NodeGroupUtil.disappear(this.getStage(), "size");
            this.size.setText("-");
        } else {
            this.size.setText(NumberUtil.formatSize(file.getFileSize(), 2));
        }
        this.stage.switchOnTab();
        this.stage.hideOnEscape();
    }

    @Override
    public String getViewTitle() {
        return I18nHelper.fileInfo();
    }
}
