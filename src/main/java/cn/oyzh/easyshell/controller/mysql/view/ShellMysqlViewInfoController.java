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
    private ReadOnlyTextField viewName;

    /**
     * 注释
     */
    @FXML
    private ReadOnlyTextArea viewComment;

    /**
     * 定义
     */
    @FXML
    private Editor viewDefinition;

    /**
     * ddl
     */
    @FXML
    private Editor createDefinition;

    /**
     * 表节点
     */
    private ShellMysqlViewTreeItem viewItem;

    /**
     * 初始化信息
     */
    private void initInfo() {
        ShellMysqlDatabaseTreeItem dbItem = this.viewItem.dbItem();
        MysqlView view = dbItem.selectFullView(this.viewItem.viewName());
        this.viewName.setText(view.getName());
        this.viewComment.setText(view.getComment());
        this.viewDefinition.setText(view.getDefinition());
        this.createDefinition.setText(view.getCreateDefinition());
    }

    @Override
    public void onWindowShown(WindowEvent event) {
        super.onWindowShown(event);
        this.viewItem = this.getProp("item");
        StageManager.showMask(this::initInfo);
        this.stage.hideOnEscape();
    }

    @Override
    public String getViewTitle() {
        return I18nHelper.viewInfo();
    }
}
