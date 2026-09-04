package cn.oyzh.easyshell.tabs.dameng.view;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.dameng.generator.view.DamengViewAlertSqlGenerator;
import cn.oyzh.easyshell.dameng.generator.view.DamengViewCreateSqlGenerator;
import cn.oyzh.easyshell.dameng.view.DamengAlertViewParam;
import cn.oyzh.easyshell.dameng.view.DamengCreateViewParam;
import cn.oyzh.easyshell.dameng.view.DamengView;
import cn.oyzh.easyshell.event.dameng.ShellDamengEventUtil;
import cn.oyzh.easyshell.fx.dameng.ShellDamengSecurityTypeComboBox;
import cn.oyzh.easyshell.trees.dameng.schema.ShellDamengSchemaTreeItem;
import cn.oyzh.fx.db.listener.DBStatusListener;
import cn.oyzh.fx.db.listener.DBStatusListenerManager;
import cn.oyzh.fx.editor.incubator.Editor;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.plus.controls.button.FXCheckBox;
import cn.oyzh.fx.plus.controls.tab.FXTabPane;
import cn.oyzh.fx.plus.controls.text.area.FXTextArea;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.node.NodeGroupUtil;
import cn.oyzh.fx.plus.node.NodeUtil;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;

/**
 * db视图tab内容组件
 *
 * @author oyzh
 * @since 2024/06/28
 */
public class ShellDamengViewDesignTabController extends RichTabController {

    /**
     * 视图对象
     */
    private DamengView view;

    /**
     * db数据库树节点
     */
    private ShellDamengSchemaTreeItem dbItem;

    //    /**
    //     * 定义者
    //     */
    //    @FXML
    //    private FXTextField definer;

    /**
     * 只读模式
     */
    @FXML
    private FXCheckBox readonly;

    /**
     * 注释
     */
    @FXML
    private FXTextArea comment;

    /**
     * 安全性
     */
    @FXML
    private ShellDamengSecurityTypeComboBox securityType;
    //
    //    /**
    //     * 检查选项
    //     */
    //    @FXML
    //    private DamengViewCheckOptionComboBox checkOption;

    /**
     * 定义
     */
    @FXML
    private Editor definition;

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
            //this.securityType.selectFirst();
            NodeGroupUtil.disappear(this.getTab(), "action3");
        } else {
            // 查询视图信息
            this.view = this.dbItem.selectView(this.view.getName());
            // 初始化数据
            this.comment.text(this.view.getComment());
            this.definition.text(this.view.getDefinition());
            this.definition.forgetHistory();
            this.readonly.setSelected(!this.view.isUpdatable());
            this.securityType.select(this.view.getSecurityType());
            NodeGroupUtil.display(this.getTab(), "action3");
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
    public void init(DamengView view, ShellDamengSchemaTreeItem dbItem) {
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
        //        DBStatusListenerManager.bindListener(this.definer, this.listener);
        DBStatusListenerManager.bindListener(this.comment, this.listener);
        DBStatusListenerManager.bindListener(this.readonly, this.listener);
        DBStatusListenerManager.bindListener(this.definition, this.listener);
        //        DBStatusListenerManager.bindListener(this.checkOption, this.listener);
        DBStatusListenerManager.bindListener(this.securityType, this.listener);
    }

    /**
     * 初始化数据监听器
     */
    private void initDBListener() {
        // 销毁监听器
        if (this.listener != null) {
            this.listener.destroy();
            //            DBStatusListenerManager.unbindListener(this.definer, this.listener);
            //            DBStatusListenerManager.unbindListener(this.algorithm, this.listener);
            DBStatusListenerManager.unbindListener(this.comment, this.listener);
            DBStatusListenerManager.unbindListener(this.readonly, this.listener);
            DBStatusListenerManager.unbindListener(this.definition, this.listener);
            //            DBStatusListenerManager.unbindListener(this.checkOption, this.listener);
            DBStatusListenerManager.unbindListener(this.securityType, this.listener);
        }
        // 初始化监听器
        this.listener = new DBStatusListener(this.view.getSchema() + ":" + this.view.getName()) {
            @Override
            public void changed(ObservableValue<?> observable, Object oldValue, Object newValue) {
                initChangedFlag();
            }
        };
        // 监听组件
        //        DBStatusListenerManager.bindListener(this.definer, this.listener);
        //        DBStatusListenerManager.bindListener(this.algorithm, this.listener);
        DBStatusListenerManager.bindListener(this.comment, this.listener);
        DBStatusListenerManager.bindListener(this.readonly, this.listener);
        DBStatusListenerManager.bindListener(this.definition, this.listener);
        //        DBStatusListenerManager.bindListener(this.checkOption, this.listener);
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
            DamengView tempView = this.tempData();

            // 视图名称
            if (this.newData) {
                this.viewName = MessageBox.prompt(I18nHelper.pleaseInputViewName(), this.viewName);
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
                DamengView view = this.dbItem.selectView(this.viewName);
                this.dbItem.getViewTypeChild().addView(view);
                // 初始化监听器
                this.initDBListener();
            } else {// 修改视图
                this.dbItem.alertView(tempView);
                ShellDamengEventUtil.viewAlerted(viewName, this.dbItem);
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
    private DamengView tempData() {
        // 创建临时对象
        DamengView tempView = new DamengView();
        tempView.setName(this.view.getName());

        // 数据库
        tempView.setSchema(this.view.getSchema());
        tempView.setComment(this.comment.getTextTrim());
        tempView.setUpdatable(!this.readonly.isSelected());
        tempView.setDefinition(this.definition.getTextTrim());
        //        tempView.setAlgorithm(this.algorithm.getSelectedItem());
        //        tempView.setCheckOption(this.checkOption.getSelectedItem());
        tempView.setSecurityType(this.securityType.getSelectedItem());

        return tempView;
    }

    @Override
    protected void bindListeners() {
        super.bindListeners();
        // 监听事件
        NodeUtil.nodeOnCtrlS(this.getTab(), this::save);
        NodeUtil.nodeOnCtrlS(this.comment, this::save);
        NodeUtil.nodeOnCtrlS(this.definition, this::save);
        NodeUtil.nodeOnCtrlS(this.preview, this::save);

        // 切换面板监听
        this.tabPane.selectedIndexChanged((observable, oldValue, newValue) -> {
            if (newValue.intValue() == 2) {
                this.initPreview();
            }
        });
    }

    public String schema() {
        return this.dbItem.schema();
    }

    public String viewName() {
        return this.view.getName();
    }

    public ShellDamengSchemaTreeItem getDbItem() {
        return dbItem;
    }

    public boolean isUnsaved() {
        return unsaved;
    }

    /**
     * 初始化预览
     */
    private void initPreview() {
        DamengView temp = this.tempData();
        String sql;
        if (this.newData) {
            DamengCreateViewParam param = new DamengCreateViewParam();
            param.setView(temp);
            param.setSchema(this.schema());
            if (StringUtil.isBlank(param.getViewName())) {
                param.setViewName("Unnamed_View");
            }
            sql = DamengViewCreateSqlGenerator.generateSqlSingle(param);
        } else {
            DamengAlertViewParam param = new DamengAlertViewParam();
            param.setView(temp);
            param.setSchema(this.schema());
            sql = DamengViewAlertSqlGenerator.generateSqlSingle(param);
        }
        this.preview.text(sql);
    }
}
