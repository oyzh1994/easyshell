package cn.oyzh.easyshell.tabs.mysql.view;

import cn.oyzh.easyshell.event.mysql.MysqlEventUtil;
import cn.oyzh.easyshell.fx.mysql.DBEditor;
import cn.oyzh.easyshell.fx.mysql.DBSecurityTypeComboBox;
import cn.oyzh.easyshell.fx.mysql.view.MysqlViewAlgorithmComboBox;
import cn.oyzh.easyshell.fx.mysql.view.MysqlViewCheckOptionComboBox;
import cn.oyzh.easyshell.db.listener.DBStatusListener;
import cn.oyzh.easyshell.db.listener.DBStatusListenerManager;
import cn.oyzh.easyshell.mysql.view.MysqlView;
import cn.oyzh.easyshell.trees.mysql.database.MysqlDatabaseTreeItem;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.plus.controls.text.field.FXTextField;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.node.NodeUtil;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * db视图tab内容组件
 *
 * @author oyzh
 * @since 2024/06/28
 */
public class MysqlViewDesignTabController extends RichTabController {

    /**
     * 视图对象
     */
    private MysqlView dbView;

    /**
     * db数据库树节点
     */
    private MysqlDatabaseTreeItem dbItem;

    /**
     * 定义者
     */
    @FXML
    private FXTextField definer;

    /**
     * 算法
     */
    @FXML
    private MysqlViewAlgorithmComboBox algorithm;

    /**
     * 安全性
     */
    @FXML
    private DBSecurityTypeComboBox securityType;

    /**
     * 检查选项
     */
    @FXML
    private MysqlViewCheckOptionComboBox checkOption;

    /**
     * 定义
     */
    @FXML
    private DBEditor definition;

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

        // 更新新表标志位
        this.newData = this.dbView.isNew();

        // 初始化数据
        this.definer.setText(this.dbView.getDefiner());
        this.algorithm.select(this.dbView.getAlgorithm());
        this.checkOption.select(this.dbView.getCheckOption());
        this.securityType.select(this.dbView.getSecurityType());
        this.definition.setText(this.dbView.getDefinition());
        this.definition.forgetHistory();
        this.definition.setDialect(this.dbItem.dialect());

        // 如果是新数据，则默认触发变更
        if (this.newData) {
            this.unsaved = true;
            this.algorithm.selectFirst();
            this.checkOption.selectFirst();
            this.securityType.selectFirst();
            this.definer.setText("`root`@`%`");
        }

        // 标记为结束
        FXUtil.runPulse(() -> this.initiating = false);
    }

    /**
     * 执行初始化
     *
     * @param view   视图
     * @param dbItem db库树节点
     */
    public void init(MysqlView view, MysqlDatabaseTreeItem dbItem) {
        this.dbView = view;
        this.dbItem = dbItem;

        // 初始化监听器
        this.initDBListener();

        // 初始化信息
        this.initInfo();

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
        // 初始化监听器
        this.listener = new DBStatusListener(this.dbView.getDbName() + ":" + this.dbView.getName()) {
            @Override
            public void changed(ObservableValue<?> observable, Object oldValue, Object newValue) {
                initChangedFlag();
            }
        };
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
     * 保存
     */
    @FXML
    private void save() {
        StageManager.showMask(this::doSave);
    }

    /**
     * 执行保存
     */
    private void doSave() {
        try {
            // 创建临时对象
            MysqlView tempView = new MysqlView();

            String viewName;
            // 视图名称
            if (this.dbView.getName() == null) {
                viewName = MessageBox.prompt(I18nHelper.pleaseInputViewName());
                if (viewName == null) {
                    return;
                }
                tempView.setName(viewName);
            } else {
                viewName = this.dbView.getName();
                tempView.setName(viewName);
            }

            // 数据库
            tempView.setDbName(this.dbView.getDbName());
            tempView.setDefiner(this.definer.getTextTrim());
            tempView.setDefinition(this.definition.getTextTrim());
            tempView.setAlgorithm(this.algorithm.getSelectedItem());
            tempView.setCheckOption(this.checkOption.getSelectedItem());
            tempView.setSecurityType(this.securityType.getSelectedItem());

            // this.disableTab();

            // 创建视图
            if (this.newData) {
                this.dbItem.createView(tempView);
                MysqlView view = this.dbItem.selectView(viewName);
                this.dbItem.getViewTypeChild().addView(view);
                // MysqlEventUtil.viewAdded(this.dbItem);
                // 初始化监听器
                this.initDBListener();
            } else {// 修改视图
                this.dbItem.alertView(tempView);
                MysqlEventUtil.viewAlerted(viewName, this.dbItem);
            }
            // // 刷新数据
            // this.dbItem.getViewTypeChild().reloadChild();
            // 重置保存标志位
            this.unsaved = false;
            // 查询视图信息
            this.dbView = this.dbItem.selectView(viewName);
            // 更新信息
            this.initInfo();
        } catch (Exception ex) {
            MessageBox.exception(ex);
        } finally {
            // this.enableTab();
            this.flushTab();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resourceBundle) {
        super.initialize(location, resourceBundle);

        // 监听事件
        NodeUtil.nodeOnCtrlS(this.getTab(), this::save);
        NodeUtil.nodeOnCtrlS(this.definer, this::save);
        NodeUtil.nodeOnCtrlS(this.definition, this::save);
    }

    public String dbName() {
        return this.dbItem.dbName();
    }

    public String viewName() {
        return this.dbView.getName();
    }

    public MysqlDatabaseTreeItem getDbItem() {
        return dbItem;
    }

    // public void setDbItem(MysqlDatabaseTreeItem dbItem) {
    //     this.dbItem = dbItem;
    // }

    public boolean isUnsaved() {
        return unsaved;
    }

    // public void setUnsaved(boolean unsaved) {
    //     this.unsaved = unsaved;
    // }
}
