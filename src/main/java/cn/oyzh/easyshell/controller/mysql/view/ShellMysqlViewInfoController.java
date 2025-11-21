package cn.oyzh.easyshell.controller.mysql.view;

import cn.oyzh.easyshell.mysql.view.MysqlView;
import cn.oyzh.easyshell.trees.mysql.database.ShellMysqlDatabaseTreeItem;
import cn.oyzh.easyshell.trees.mysql.view.ShellMysqlViewTreeItem;
import cn.oyzh.fx.editor.incubator.Editor;
import cn.oyzh.fx.gui.text.area.ReadOnlyTextArea;
import cn.oyzh.fx.gui.text.field.ReadOnlyTextField;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;

/**
 * mysql视图信息业务
 *
 * @author oyzh
 * @since 2024/01/30
 */
@StageAttribute(
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "mysql/view/shellMysqlViewInfo.fxml"
)
public class ShellMysqlViewInfoController extends StageController {

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
     * 视图节点
     */
    private ShellMysqlViewTreeItem treeItem;

    /**
     * 初始化信息
     */
    private void initInfo() {
        ShellMysqlDatabaseTreeItem dbItem = this.treeItem.dbItem();
        MysqlView view = dbItem.selectView(this.treeItem.viewName());
        this.name.setText(view.getName());
        this.comment.setText(view.getComment());
        this.definition.setText(view.getDefinition());
        this.createDefinition.setText(view.getCreateDefinition());
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
        return I18nHelper.viewInfo();
    }
}
