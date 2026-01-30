package cn.oyzh.easyshell.controller.mysql.event;

import cn.oyzh.easyshell.mysql.event.MysqlEvent;
import cn.oyzh.easyshell.trees.mysql.database.ShellMysqlDatabaseTreeItem;
import cn.oyzh.easyshell.trees.mysql.event.ShellMysqlEventTreeItem;
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
 * mysql事件信息业务
 *
 * @author oyzh
 * @since 2024/01/30
 */
@StageAttribute(
        stageStyle = FXStageStyle.EXTENDED,
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "mysql/event/shellMysqlEventInfo.fxml"
)
public class ShellMysqlEventInfoController extends StageController {

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
    private ShellMysqlEventTreeItem treeItem;

    /**
     * 初始化信息
     */
    private void initInfo() {
        ShellMysqlDatabaseTreeItem dbItem = this.treeItem.dbItem();
        MysqlEvent event = dbItem.selectEvent(this.treeItem.eventName());
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
}
