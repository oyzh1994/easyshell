package cn.oyzh.easyshell.controller.mysql.database;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.dto.mysql.MysqlDatabase;
import cn.oyzh.easyshell.event.mysql.ShellMysqlEventUtil;
import cn.oyzh.easyshell.fx.mysql.ShellMysqlCharsetComboBox;
import cn.oyzh.easyshell.fx.mysql.ShellMysqlCollationComboBox;
import cn.oyzh.easyshell.trees.mysql.root.MysqlRootTreeItem;
import cn.oyzh.fx.gui.text.field.ReadOnlyTextField;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;

/**
 * 编辑db库业务
 *
 * @author oyzh
 * @since 2024/01/30
 */
@StageAttribute(
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "mysql/database/shellMysqlDatabaseUpdate.fxml"
)
public class ShellMysqlDatabaseUpdateController extends StageController {

    /**
     * 名称
     */
    @FXML
    private ReadOnlyTextField name;

    /**
     * 字符集
     */
    @FXML
    private ShellMysqlCharsetComboBox charset;

    /**
     * 排序方式
     */
    @FXML
    private ShellMysqlCollationComboBox collation;

    /**
     * db库对象
     */
    private MysqlDatabase database;

    /**
     * db连接节点
     */
    private MysqlRootTreeItem connectItem;

    /**
     * 编辑db库
     */
    @FXML
    private void save() {
        try {
            MysqlDatabase database = new MysqlDatabase();
            database.setName(this.name.getText());
            // 字符集
            String charset = this.charset.getSelectedItem();
            if (!StringUtil.equalsIgnoreCase(charset, this.database.getCharset())) {
                database.setCharset(charset);
            }
            // 排序规则
            String collation = this.collation.getSelectedItem();
            if (!StringUtil.equalsIgnoreCase(charset, this.database.getCollation())) {
                database.setCollation(collation);
            }
            // 修改数据库
            if (this.connectItem.alterDatabase(database)) {
                ShellMysqlEventUtil.databaseUpdated(this.connectItem, database);
                // 更新字符集和排序规则
                this.database.setCharset(charset);
                this.database.setCollation(collation);
                this.closeWindow();
            } else {
                MessageBox.warn(I18nHelper.operationFail());
            }
        } catch (Exception ex) {
            MessageBox.exception(ex);
        }
    }

    @Override
    protected void bindListeners() {
        super.bindListeners();
        // 字符集选中事件
        this.charset.selectedItemChanged((observable, oldValue, newValue) -> {
            this.collation.init(newValue, this.connectItem.client());
            this.collation.select(0);
        });
    }

    @Override
    public void onWindowShown(WindowEvent event) {
        this.database = this.getProp("database");
        this.connectItem = this.getProp("connectItem");

        // 数据库名
        this.name.setText(this.database.getName());

        // 初始化字符集和排序
        this.charset.init(this.connectItem.client());
        this.charset.select(this.database.getCharset());
        this.collation.init(this.database.getCharset(), this.connectItem.client());
        this.collation.select(this.database.getCollation());

        super.onWindowShown(event);
        this.stage.switchOnTab();
        this.stage.hideOnEscape();
    }

    @Override
    public String getViewTitle() {
        return I18nHelper.updateDatabase();
    }
}
