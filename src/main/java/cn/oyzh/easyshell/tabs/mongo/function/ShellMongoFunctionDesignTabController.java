package cn.oyzh.easyshell.tabs.mongo.function;

import cn.oyzh.common.cache.CacheHelper;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.data.db.listener.DBStatusListener;
import cn.oyzh.easyshell.data.db.listener.DBStatusListenerManager;
import cn.oyzh.easyshell.mongo.function.MongoFunction;
import cn.oyzh.easyshell.trees.mongo.database.MongoDatabaseTreeItem;
import cn.oyzh.easyshell.util.mongo.MongoDataUtil;
import cn.oyzh.fx.editor.incubator.control.SqlEditor;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.plus.controls.tab.FXTabPane;
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
 * db函数内容组件
 *
 * @author oyzh
 * @since 2024/07/08
 */
public class ShellMongoFunctionDesignTabController extends RichTabController {

    /**
     * 函数
     */
    private MongoFunction function;

    public MongoFunction getFunction() {
        return function;
    }

    /**
     * db数据库树节点
     */
    private MongoDatabaseTreeItem dbItem;

    /**
     * 定义
     */
    @FXML
    private SqlEditor definition;

    /**
     * 预览
     */
    @FXML
    private SqlEditor preview;

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
     * 执行初始化
     *
     * @param function 查询对象
     * @param dbItem   db库树节点
     */
    public void init(MongoFunction function, MongoDatabaseTreeItem dbItem) {
        this.dbItem = dbItem;
        this.function = function;
        // 更新新数据标志位
        this.newData = this.function.isNew();
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
        CacheHelper.set("dbClient", this.dbItem.client());
    }

    /**
     * 初始化数据监听器
     */
    private void initDBListener() {
        // 销毁监听器
        if (this.listener != null) {
            this.listener.destroy();
            DBStatusListenerManager.unbindListener(this.definition, this.listener);
        }
        // 初始化监听器
        this.listener = new DBStatusListener(this.function.getDbName() + ":" + this.function.getName()) {
            @Override
            public void changed(ObservableValue<?> observable, Object oldValue, Object newValue) {
                initChangedFlag();
            }
        };
        DBStatusListenerManager.bindListener(this.definition, this.listener);
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
     * 初始化信息
     */
    protected void initInfo() {
        // 更新初始化标志位
        this.initiating = true;

        // 如果是新数据，则默认触发变更
        if (this.newData) {
            this.unsaved = true;
            String defDefinition = """
                    function () {
                        //Routine body goes here...
                    
                    }
                    """;
            this.definition.setText(defDefinition);
        } else {
            // 查询函数信息
            this.function = this.dbItem.selectFunction(this.function.getName());
            // 初始化数据
            this.definition.setText(this.function.getCode());
            this.definition.forgetHistory();
        }

        // 标记为结束
        FXUtil.runPulse(() -> this.initiating = false);
    }

    /**
     * 保存
     */
    @FXML
    private void save() {
        StageManager.showMask(this::doSave);
    }

    /**
     * 函数名称
     */
    private String functionName;

    /**
     * 执行保存
     */
    private void doSave() {
        try {
            // 创建临时对象
            MongoFunction tempFunction = this.tempData();

            // 函数名称
            if (this.newData) {
                functionName = MessageBox.prompt(I18nHelper.pleaseInputFunctionName(), functionName);
                if (functionName == null) {
                    return;
                }
                tempFunction.setName(functionName);
            } else {
                functionName = tempFunction.getName();
            }

            // 创建函数
            if (this.newData) {
                this.dbItem.createFunction(tempFunction);
                MongoFunction function = this.dbItem.selectFunction(functionName);
                this.dbItem.getFunctionTypeChild().addFunction(function);
                this.initDBListener();
            } else {// 修改过程
                this.dbItem.alertFunction(tempFunction);
            }
            // 更新保存标志位
            this.unsaved = false;
            // 更新新数据标志位
            this.newData = false;
            this.function = tempFunction;
            // 刷新tab
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
    private MongoFunction tempData() {
        // 创建临时对象
        MongoFunction tempFunction = new MongoFunction();
        tempFunction.setName(this.function.getName());

        // 基本信息处理
        tempFunction.setDbName(this.function.getDbName());
        tempFunction.setCode(this.definition.getTextTrim());
        return tempFunction;
    }

    @Override
    public void initialize(URL location, ResourceBundle resourceBundle) {
        super.initialize(location, resourceBundle);
        // 监听事件
        NodeUtil.nodeOnCtrlS(this.getTab(), this::save);
        NodeUtil.nodeOnCtrlS(this.definition, this::save);

        // 切换面板监听
        this.tabPane.selectedIndexChanged((observable, oldValue, newValue) -> {
            if (newValue.intValue() == 1) {
                this.initPreview();
            }
        });
    }

    /**
     * 初始化预览
     */
    private void initPreview() {
        MongoFunction temp = this.tempData();
        if (StringUtil.isBlank(temp.getName())) {
            temp.setName("Unnamed_Function");
        }
        String sql = MongoDataUtil.toReplaceScript(temp);
        this.preview.text(sql);
    }

    public boolean isUnsaved() {
        return unsaved;
    }

    public void setUnsaved(boolean unsaved) {
        this.unsaved = unsaved;
    }

    public MongoDatabaseTreeItem getDbItem() {
        return dbItem;
    }

    public void setDbItem(MongoDatabaseTreeItem dbItem) {
        this.dbItem = dbItem;
    }

    @Override
    public void destroy() {
        this.preview.destroy();
        this.definition.destroy();
        super.destroy();
    }
}
