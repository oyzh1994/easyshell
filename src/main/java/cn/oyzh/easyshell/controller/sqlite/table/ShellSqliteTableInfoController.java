package cn.oyzh.easyshell.controller.sqlite.table;

import cn.oyzh.easyshell.sqlite.table.SQLiteTable;
import cn.oyzh.easyshell.trees.sqlite.table.ShellSqliteTableTreeItem;
import cn.oyzh.fx.editor.incubator.Editor;
import cn.oyzh.fx.gui.text.area.ReadOnlyTextArea;
import cn.oyzh.fx.gui.text.field.ReadOnlyTextField;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.FXStageStyle;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import cn.oyzh.i18n.I18nResourceBundle;
import javafx.fxml.FXML;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;

/**
 * sqlite表信息业务
 *
 * @author oyzh
 * @since 2026-11-02
 */
@StageAttribute(
        stageStyle = FXStageStyle.EXTENDED,
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "sqlite/table/shellSqliteTableInfo.fxml"
)
public class ShellSqliteTableInfoController extends StageController {

    /**
     * 名称
     */
    @FXML
    private ReadOnlyTextField name;

    /**
     * 类型
     */
    @FXML
    private ReadOnlyTextField type;

    /**
     * 无rowid
     */
    @FXML
    private ReadOnlyTextField withoutRowid;

    /**
     * 严格模式
     */
    @FXML
    private ReadOnlyTextField strict;

    /**
     * 注释
     */
    @FXML
    private ReadOnlyTextArea comment;

    /**
     * 定义
     */
    @FXML
    private Editor createDefinition;

    /**
     * 表节点
     */
    private ShellSqliteTableTreeItem treeItem;

    /**
     * 初始化信息
     */
    private void initInfo() {
        SQLiteTable table = this.treeItem.client().selectTable(this.treeItem.tableName());
        if (table == null) {
            MessageBox.warn(I18nResourceBundle.i18nString(I18nHelper.NOT_FOUND), this.getWindow());
            this.stage.close();
            return;
        }
        this.name.setText(table.getName());
        this.type.setText(table.getType());
        this.withoutRowid.setText(String.valueOf(table.isWithoutRowid()));
        this.strict.setText(String.valueOf(table.isStrict()));
        this.comment.setText(table.getComment());
        this.createDefinition.setText(this.treeItem.client().showCreate(table.getName()));
    }

    @Override
    public void onWindowShown(WindowEvent event) {
        super.onWindowShown(event);
        this.treeItem = this.getProp("item");
        StageManager.showMask(this.getWindow(), this::initInfo);
        this.stage.hideOnEscape();
    }

    @Override
    public String getViewTitle() {
        return I18nHelper.tableInfo();
    }

}
