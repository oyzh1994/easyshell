package cn.oyzh.easyshell.tabs.h2.view;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.event.h2.ShellH2EventUtil;
import cn.oyzh.easyshell.fx.ShellDataEditor;
import cn.oyzh.easyshell.query.h2.ShellH2QueryEditor;
import cn.oyzh.easyshell.h2.generator.view.H2ViewCreateSqlGenerator;
import cn.oyzh.easyshell.h2.schema.H2SchemaManager;
import cn.oyzh.easyshell.h2.view.H2View;
import cn.oyzh.easyshell.h2.ShellH2Client;
import cn.oyzh.easyshell.trees.h2.root.ShellH2RootTreeItem;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.gui.tabs.RichTabPane;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.node.NodeGroupUtil;
import cn.oyzh.fx.plus.controls.tab.FixedTab;
import cn.oyzh.fx.plus.node.NodeUtil;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;

/**
 * H2 视图设计器控制器。
 *
 * @since 2026-11-02
 */
public class ShellH2ViewDesignTabController extends RichTabController {

    @FXML
    private ShellH2QueryEditor definition;

    @FXML
    private ShellDataEditor preview;

    @FXML
    private RichTabPane tabPane;

    @FXML
    private FixedTab previewTab;

    private ShellH2Client client;

    private ShellH2RootTreeItem dbItem;

    private String originalName;

    private String pendingName;

    private String schema;

    private boolean unsaved;

    /**
     * 初始化
     *
     * @param view   视图对象
     * @param dbItem 数据库树节点
     */
    public void init(H2View view, ShellH2RootTreeItem dbItem) {
        this.dbItem = dbItem;
        this.client = dbItem.client();
        this.schema = view == null || StringUtil.isBlank(view.getDbName())
                ? this.client.schema() : view.getDbName();
        this.originalName = view == null ? null : view.getName();
        this.pendingName = this.originalName;
        this.definition.text(view == null ? "" : StringUtil.blankToDefault(view.getDefinition(), ""));
        this.unsaved = view == null;
        if (view == null) {
            NodeGroupUtil.disappear(this.getTab(), "action3");
        } else {
            NodeGroupUtil.display(this.getTab(), "action3");
        }
        this.definition.textProperty().addListener((observable, oldValue, newValue) -> this.unsaved = true);
        this.definition.forgetHistory();
    }

    private H2View buildView(String name) {
        H2View view = new H2View();
        view.setName(name);
        view.setDbName(this.schema);
        view.setDefinition(this.definition.getText().trim());
        return view;
    }

    private void initPreview() {
        String name = StringUtil.blankToDefault(
                StringUtil.blankToDefault(this.originalName, this.pendingName),
                I18nHelper.unnamedView());
        this.preview.text(H2ViewCreateSqlGenerator.createViewPreview(name, this.definition.getText()));
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
                targetName = MessageBox.prompt(I18nHelper.pleaseInputViewName(),
                        this.pendingName, this.getWindow());
                if (targetName == null) {
                    return;
                }
                targetName = targetName.trim();
                if (StringUtil.isBlank(targetName)) {
                    MessageBox.warn(I18nHelper.pleaseInputViewName(), this.getWindow());
                    return;
                }
                this.pendingName = targetName;
            }
            H2View view = this.buildView(targetName);
            String previousName = this.originalName;
            new H2SchemaManager(this.client).saveView(this.schema, previousName, view);
            this.originalName = targetName;
            this.pendingName = targetName;
            this.unsaved = false;
            NodeGroupUtil.display(this.getTab(), "action3");
            ShellH2EventUtil.viewChanged(this.client, this.schema, previousName, targetName);
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
        if (!MessageBox.confirm(ShellI18nHelper.confirmRefreshData(), this.getWindow())) {
            return;
        }
        try {
            H2View view = this.client.selectView(this.schema, this.originalName);
            if (view == null) {
                MessageBox.warn(I18nHelper.operationFail(), this.getWindow());
                return;
            }
            this.definition.text(StringUtil.blankToDefault(view.getDefinition(), ""));
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
        NodeUtil.nodeOnCtrlS(this.getTab() == null ? null : this.getTab().getContent(), this::save);
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
        return StringUtil.blankToDefault(this.originalName, this.pendingName);
    }

    /**
     * 获取视图所属模式。
     *
     * @return 模式名称
     */
    public String schema() {
        return this.schema;
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
    public ShellH2RootTreeItem getDbItem() {
        return this.dbItem;
    }
}
