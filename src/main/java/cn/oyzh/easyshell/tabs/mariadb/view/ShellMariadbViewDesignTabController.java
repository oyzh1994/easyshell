package cn.oyzh.easyshell.tabs.mariadb.view;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.event.mariadb.ShellMariadbEventUtil;
import cn.oyzh.easyshell.fx.mariadb.ShellMariadbSecurityTypeComboBox;
import cn.oyzh.easyshell.fx.mariadb.view.ShellMariadbViewAlgorithmComboBox;
import cn.oyzh.easyshell.fx.mariadb.view.ShellMariadbViewCheckOptionComboBox;
import cn.oyzh.easyshell.mariadb.generator.view.MariadbViewAlertSqlGenerator;
import cn.oyzh.easyshell.mariadb.generator.view.MariadbViewCreateSqlGenerator;
import cn.oyzh.easyshell.mariadb.view.MariadbAlertViewParam;
import cn.oyzh.easyshell.mariadb.view.MariadbCreateViewParam;
import cn.oyzh.easyshell.mariadb.view.MariadbView;
import cn.oyzh.easyshell.query.mariadb.ShellMariadbQueryEditor;
import cn.oyzh.easyshell.trees.mariadb.database.ShellMariadbDatabaseTreeItem;
import cn.oyzh.fx.db.listener.DBStatusListener;
import cn.oyzh.fx.db.listener.DBStatusListenerManager;
import cn.oyzh.fx.editor.incubator.Editor;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.plus.controls.tab.FXTabPane;
import cn.oyzh.fx.plus.controls.text.field.FXTextField;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.node.NodeGroupUtil;
import cn.oyzh.fx.plus.node.NodeUtil;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;

/**
 * MariaDB 视图设计标签页控制器
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class ShellMariadbViewDesignTabController extends RichTabController {

    /**
     * 视图对象
     */
    private MariadbView view;

    /**
     * 数据库树节点
     */
    private ShellMariadbDatabaseTreeItem dbItem;

    /**
     * 定义者
     */
    @FXML
    private FXTextField definer;

    /**
     * 算法
     */
    @FXML
    private ShellMariadbViewAlgorithmComboBox algorithm;

    /**
     * 安全性
     */
    @FXML
    private ShellMariadbSecurityTypeComboBox securityType;

    /**
     * 检查选项
     */
    @FXML
    private ShellMariadbViewCheckOptionComboBox checkOption;

    /**
     * 定义
     */
    @FXML
    private ShellMariadbQueryEditor definition;

    /**
     * 预览
     */
    @FXML
    private Editor preview;

    /**
     * 切换面板
     */
    @FXML
    private FXTabPane tabPane;

    /**
     * 数据监听器
     */
    private DBStatusListener listener;

    /**
     * 未保存标志位
     */
    private boolean unsaved;

    /**
     * 新数据标志位
     */
    private boolean newData;

    /**
     * 初始化中标志位
     */
    private boolean initiating;

    /**
     * 初始化信息
     */
    protected void initInfo() {
        // 更新初始化标志位
        this.initiating = true;

        // 如果是新数据，则默认触发变更
        if (this.newData) {
            this.unsaved = true;
            this.algorithm.selectFirst();
            this.checkOption.selectFirst();
            this.securityType.selectFirst();
            this.definer.setText("`root`@`%`");
            NodeGroupUtil.disappear(this.getTab(), "action3");
        } else {
            // 查询视图信息
            this.view = this.dbItem.selectView(this.view.getName());
            // 初始化数据
            this.definer.setText(this.view.getDefiner());
            this.algorithm.select(this.view.getAlgorithm());
            this.definition.setText(this.view.getDefinition());
            this.checkOption.select(this.view.getCheckOption());
            this.securityType.select(this.view.getSecurityType());
            this.definition.forgetHistory();
            NodeGroupUtil.display(this.getTab(), "action3");
        }

        // 标记为结束
        FXUtil.runPulse(() -> this.initiating = false);
    }

    /**
     * 执行初始化
     *
     * @param view   视图
     * @param dbItem 数据库树节点
     */
    public void init(MariadbView view, ShellMariadbDatabaseTreeItem dbItem) {
        this.dbItem = dbItem;
        this.view = view;
        // 更新新数据标志位
        this.newData = this.view.isNew();
        StageManager.showMask(this::doInit);
    }

    /**
     * 执行初始化
     */
    private void doInit() {
        // 初始化监听器
        this.initDBListener();

        // 初始化信息
        FXUtil.runWait(this::initInfo);

        // 监听组件
        DBStatusListenerManager.bindListener(this.definer, this.listener);
        DBStatusListenerManager.bindListener(this.algorithm, this.listener);
        DBStatusListenerManager.bindListener(this.definition, this.listener);
        DBStatusListenerManager.bindListener(this.checkOption, this.listener);
        DBStatusListenerManager.bindListener(this.securityType, this.listener);
    }

    /**
     * 初始化数据监听器
     */
    private void initDBListener() {
        // 销毁监听器
        if (this.listener != null) {
            this.listener.destroy();
            DBStatusListenerManager.unbindListener(this.definer, this.listener);
            DBStatusListenerManager.unbindListener(this.algorithm, this.listener);
            DBStatusListenerManager.unbindListener(this.definition, this.listener);
            DBStatusListenerManager.unbindListener(this.checkOption, this.listener);
            DBStatusListenerManager.unbindListener(this.securityType, this.listener);
        }
        // 初始化监听器
        this.listener = new DBStatusListener(this.view.getDbName() + ":" + this.view.getName()) {
            @Override
            public void changed(ObservableValue<?> observable, Object oldValue, Object newValue) {
                initChangedFlag();
            }
        };
        // 监听组件
        DBStatusListenerManager.bindListener(this.definer, this.listener);
        DBStatusListenerManager.bindListener(this.algorithm, this.listener);
        DBStatusListenerManager.bindListener(this.definition, this.listener);
        DBStatusListenerManager.bindListener(this.checkOption, this.listener);
        DBStatusListenerManager.bindListener(this.securityType, this.listener);
    }

    /**
     * 初始化变更标志
     */
    private void initChangedFlag() {
        if (!this.initiating) {
            this.unsaved = true;
            this.flushTab();
        }
    }

    /**
     * 刷新
     */
    @FXML
    private void refresh() {
        if (!MessageBox.confirm(I18nHelper.refreshData() + "?")) {
            return;
        }
        try {
            this.init(this.view, this.dbItem);
            this.flushTab();
        } catch (Exception ex) {
            MessageBox.exception(ex);
        }
    }

    /**
     * 保存
     */
    @FXML
    private void save() {
        StageManager.showMask(this::doSave);
    }

    /**
     * 视图名称
     */
    private String viewName;

    /**
     * 执行保存
     */
    private void doSave() {
        try {
            // 创建临时对象
            MariadbView tempView = this.tempData();

            // 视图名称
            if (this.newData) {
                this.viewName = MessageBox.prompt(I18nHelper.pleaseInputViewName(), viewName);
                if (this.viewName == null) {
                    return;
                }
                tempView.setName(this.viewName);
            } else {
                this.viewName = this.view.getName();
            }

            // 创建视图
            if (this.newData) {
                this.dbItem.createView(tempView);
                MariadbView view = this.dbItem.selectView(this.viewName);
                this.dbItem.getViewTypeChild().addView(view);
                // 初始化监听器
                this.initDBListener();
            } else {// 修改视图
                this.dbItem.alertView(tempView);
                ShellMariadbEventUtil.viewAlerted(this.viewName, this.dbItem);
            }
            // 重置保存标志位
            this.unsaved = false;
            // 更新新数据标志位
            this.newData = false;
            this.view = tempView;
            // 更新信息
            FXUtil.runWait(this::initInfo);
            // 初始化预览
            this.initPreview();
        } catch (Exception ex) {
            MessageBox.exception(ex);
        } finally {
            this.flushTab();
        }
    }

    /**
     * 获取临时数据
     *
     * @return 临时数据
     */
    private MariadbView tempData() {
        // 创建临时对象
        MariadbView tempView = new MariadbView();
        tempView.setName(this.view.getName());

        // 数据库
        tempView.setDbName(this.view.getDbName());
        tempView.setDefiner(this.definer.getTextTrim());
        tempView.setDefinition(this.definition.getTextTrim());
        tempView.setAlgorithm(this.algorithm.getSelectedItem());
        tempView.setCheckOption(this.checkOption.getSelectedItem());
        tempView.setSecurityType(this.securityType.getSelectedItem());

        return tempView;
    }

    @Override
    protected void bindListeners() {
        super.bindListeners();

        // 监听事件
        NodeUtil.nodeOnCtrlS(this.getTab(), this::save);
        NodeUtil.nodeOnCtrlS(this.definer, this::save);
        NodeUtil.nodeOnCtrlS(this.definition, this::save);
        NodeUtil.nodeOnCtrlS(this.preview, this::save);

        // 切换面板监听
        this.tabPane.selectedIndexChanged((observable, oldValue, newValue) -> {
            if (newValue.intValue() == 2) {
                this.initPreview();
            }
        });
    }

    /**
     * 获取数据库名称
     *
     * @return 数据库名称
     */
    public String dbName() {
        return this.dbItem.dbName();
    }

    /**
     * 获取视图名称
     *
     * @return 视图名称
     */
    public String viewName() {
        return this.view.getName();
    }

    /**
     * 获取数据库树节点
     *
     * @return 数据库树节点
     */
    public ShellMariadbDatabaseTreeItem getDbItem() {
        return dbItem;
    }

    /**
     * 是否未保存
     *
     * @return 是否未保存
     */
    public boolean isUnsaved() {
        return unsaved;
    }

    /**
     * 初始化预览
     */
    private void initPreview() {
        MariadbView temp = this.tempData();
        String sql;
        if (this.newData) {
            MariadbCreateViewParam param = new MariadbCreateViewParam();
            param.setView(temp);
            param.setDbName(this.dbName());
            if (StringUtil.isBlank(param.getViewName())) {
                param.setViewName("Unnamed_View");
            }
            sql = MariadbViewCreateSqlGenerator.generateSqlSingle(param);
        } else {
            MariadbAlertViewParam param = new MariadbAlertViewParam();
            param.setView(temp);
            param.setDbName(this.dbName());
            sql = MariadbViewAlertSqlGenerator.generateSqlSingle(param);
        }
        this.preview.text(sql);
    }
}
