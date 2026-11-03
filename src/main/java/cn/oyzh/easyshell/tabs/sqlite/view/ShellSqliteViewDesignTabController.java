package cn.oyzh.easyshell.tabs.sqlite.view;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.event.sqlite.ShellSqliteEventUtil;
import cn.oyzh.easyshell.fx.ShellDataEditor;
import cn.oyzh.easyshell.query.sqlite.ShellSqliteQueryEditor;
import cn.oyzh.easyshell.sqlite.generator.view.SQLiteViewCreateSqlGenerator;
import cn.oyzh.easyshell.sqlite.schema.SQLiteSchemaManager;
import cn.oyzh.easyshell.sqlite.view.SQLiteView;
import cn.oyzh.easyshell.sqlite.ShellSqliteClient;
import cn.oyzh.easyshell.trees.sqlite.root.ShellSqliteRootTreeItem;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.gui.tabs.RichTabPane;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.controls.tab.FixedTab;
import cn.oyzh.fx.plus.node.NodeUtil;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;

/**
 * SQLite 视图设计器控制器。
 *
 * @since 2026-11-02
 */
public class ShellSqliteViewDesignTabController extends RichTabController {

    @FXML
    private ShellSqliteQueryEditor definition;

    @FXML
    private ShellDataEditor preview;

    @FXML
    private RichTabPane tabPane;

    @FXML
    private FixedTab previewTab;

    private ShellSqliteClient client;

    private ShellSqliteRootTreeItem dbItem;

    private String originalName;

    private boolean unsaved;

    /**
     * 初始化
     *
     * @param view   视图对象
     * @param dbItem 数据库树节点
     */
    public void init(SQLiteView view, ShellSqliteRootTreeItem dbItem) {
        this.dbItem = dbItem;
        this.client = dbItem.client();
        this.originalName = view == null ? null : view.getName();
        this.definition.setText(view == null ? "" : StringUtil.blankToDefault(view.getDefinition(), ""));
        this.unsaved = view == null;
        this.definition.textProperty().addListener((observable, oldValue, newValue) -> this.unsaved = true);
        this.definition.forgetHistory();
    }

    private SQLiteView buildView(String name) {
        SQLiteView view = new SQLiteView();
        view.setName(name);
        view.setDefinition(this.definition.getText().trim());
        return view;
    }

    @FXML
    private void createPreview() {
        this.initPreview();
        this.previewTab.selectTab();
    }

    private void initPreview() {
        try {
            String name = StringUtil.blankToDefault(this.originalName, I18nHelper.unnamedView());
            this.preview.setText(SQLiteViewCreateSqlGenerator.createView(name, this.definition.getText().trim()));
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
        }
    }

    @FXML
    private void save() {
        try {
            if (StringUtil.isBlank(this.definition.getText())) {
                MessageBox.warn(I18nHelper.pleaseInputContent(), this.getWindow());
                return;
            }
            String targetName = this.originalName;
            if (StringUtil.isBlank(targetName)) {
                targetName = MessageBox.prompt(I18nHelper.pleaseInputViewName(), this.getWindow());
                if (targetName == null) {
                    return;
                }
                targetName = targetName.trim();
                if (StringUtil.isBlank(targetName)) {
                    MessageBox.warn(I18nHelper.pleaseInputViewName(), this.getWindow());
                    return;
                }
            }
            SQLiteView view = this.buildView(targetName);
            String previousName = this.originalName;
            new SQLiteSchemaManager(this.client).saveView(previousName, view);
            this.originalName = targetName;
            this.unsaved = false;
            ShellSqliteEventUtil.viewChanged(this.client, previousName, targetName);
            this.initPreview();
            this.flushTab();
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
        }
    }

    @FXML
    private void refresh() {
        if (this.originalName == null) {
            return;
        }
        if (!MessageBox.confirm(I18nHelper.refreshData() + "?", this.getWindow())) {
            return;
        }
        try {
            SQLiteView view = this.client.selectView(this.originalName);
            if (view == null) {
                MessageBox.warn(I18nHelper.operationFail(), this.getWindow());
                return;
            }
            this.definition.setText(StringUtil.blankToDefault(view.getDefinition(), ""));
            this.definition.forgetHistory();
            this.unsaved = false;
            this.flushTab();
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
        }
    }

    @Override
    protected void bindListeners() {
        super.bindListeners();
        NodeUtil.nodeOnCtrlS(this.getTab(), this::save);
        NodeUtil.nodeOnCtrlS(this.definition, this::save);
        NodeUtil.nodeOnCtrlS(this.preview, this::save);
        this.tabPane.selectedItemChanged((observable, oldTab, newTab) -> {
            if (newTab == this.previewTab) {
                this.initPreview();
            }
        });
    }

    /**
     * 获取视图名称
     *
     * @return 视图名称
     */
    public String viewName() {
        return this.originalName;
    }

    /**
     * 是否未保存
     *
     * @return 是否未保存
     */
    public boolean isUnsaved() {
        return this.unsaved;
    }

    /**
     * 获取数据库树节点
     *
     * @return 数据库树节点
     */
    public ShellSqliteRootTreeItem getDbItem() {
        return this.dbItem;
    }
}
