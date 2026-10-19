package cn.oyzh.easyshell.controller.mariadb.event;

import cn.oyzh.easyshell.mariadb.event.MariadbEvent;
import cn.oyzh.easyshell.trees.mariadb.database.ShellMariadbDatabaseTreeItem;
import cn.oyzh.easyshell.trees.mariadb.event.ShellMariadbEventTreeItem;
import cn.oyzh.fx.editor.incubator.Editor;
import cn.oyzh.fx.gui.text.area.ReadOnlyTextArea;
import cn.oyzh.fx.gui.text.field.ReadOnlyTextField;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.window.FXStageStyle;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;

/**
 * MariaDB事件信息业务
 *
 * @author oyzh
 * @since 2026-10-09
 */
@StageAttribute(
        stageStyle = FXStageStyle.EXTENDED,
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "mariadb/event/shellMariadbEventInfo.fxml"
)
public class ShellMariadbEventInfoController extends StageController {

    /**
     * 名称
     */
    @FXML
    private ReadOnlyTextField name;

    /**
     * 注释
     */
    @FXML
    private ReadOnlyTextArea comment;

    /**
     * 定义
     */
    @FXML
    private Editor definition;

    /**
     * ddl
     */
    @FXML
    private Editor createDefinition;

    /**
     * 事件节点
     */
    private ShellMariadbEventTreeItem treeItem;

    /**
     * 初始化信息
     */
    private void initInfo() {
        ShellMariadbDatabaseTreeItem dbItem = this.treeItem.dbItem();
        MariadbEvent event = dbItem.selectEvent(this.treeItem.eventName());
        this.name.setText(event.getName());
        this.comment.setText(event.getComment());
        this.definition.setText(event.getDefinition());
        this.createDefinition.setText(event.getCreateDefinition());
    }

    @Override
    public void onWindowShown(WindowEvent event) {
        super.onWindowShown(event);
        this.treeItem = this.getProp("item");
        StageManager.showMask(this::initInfo);
        this.stage.hideOnEscape();
    }

    @Override
    public String getViewTitle() {
        return I18nHelper.eventInfo();
    }

    @Override
    public void destroy() {
        this.definition.destroy();
        this.createDefinition.destroy();
        super.destroy();
    }
}
