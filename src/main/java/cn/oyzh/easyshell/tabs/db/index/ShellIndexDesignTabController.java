package cn.oyzh.easyshell.tabs.db.index;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.fx.ShellDataEditor;
import cn.oyzh.easyshell.db.index.ShellIndexHelper;
import cn.oyzh.easyshell.db.index.ShellIndexTarget;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.DBIndex;
import cn.oyzh.fx.db.sql.DBSqlParser;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.node.NodeGroupUtil;
import cn.oyzh.fx.plus.node.NodeUtil;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;

import java.util.List;

/**
 * 索引设计标签内容组件
 * <p>
 * 索引直接以“定义”（DDL语句）的形式编辑，保存时执行编辑器中的SQL，各数据库共用此实现。
 *
 * @author oyzh
 * @since 2026-11-28
 */
public class ShellIndexDesignTabController extends RichTabController {

    /**
     * 索引对象
     */
    private DBIndex index;

    /**
     * 索引设计目标
     */
    private ShellIndexTarget target;

    /**
     * 索引定义
     */
    @FXML
    private ShellDataEditor definition;

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
     * 组件监听是否已绑定
     */
    private boolean listenersBound;

    /**
     * 初始化
     *
     * @param index  索引对象
     * @param target 索引设计目标
     */
    public void init(DBIndex index, ShellIndexTarget target) {
        this.target = target;
        this.index = index;
        // 更新新数据标志位（名称为空视为新建索引）
        this.newData = index == null || StringUtil.isBlank(index.getName());
        StageManager.showMask(this.getWindow(), this::doInit);
    }

    /**
     * 执行初始化
     */
    private void doInit() {
        // 初始化组件监听
        this.initListeners();
        // 初始化信息
        FXUtil.runWait(this::initInfo);
    }

    /**
     * 初始化组件监听
     */
    private void initListeners() {
        // 监听只绑定一次，刷新时复用
        if (this.listenersBound) {
            return;
        }
        this.listenersBound = true;
        this.definition.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!this.initiating) {
                this.unsaved = true;
                this.flushTab();
            }
        });
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
            this.definition.text(this.newDefinition());
            this.definition.forgetHistory();
            NodeGroupUtil.disappear(this.getTab(), "action3");
        } else {// 查询索引定义
            String definition = this.target.selectIndexDefinition(this.index.getName());
            // 查询不到定义时保留编辑器内容，避免用户编辑的内容丢失
            if (StringUtil.isNotBlank(definition)) {
                this.definition.text(definition);
            }
            this.definition.forgetHistory();
            NodeGroupUtil.display(this.getTab(), "action3");
        }
        // 标记为结束
        FXUtil.runPulse(() -> this.initiating = false);
    }

    /**
     * 新建索引的默认定义
     *
     * @return 定义
     */
    private String newDefinition() {
        String scope = this.target.indexScope();
        String prefix = StringUtil.isBlank(scope) ? "" : scope + ".";
        return "-- " + ShellI18nHelper.indexDefinitionTip() + "\n"
                + "-- CREATE INDEX INDEX_NAME ON " + prefix + "TABLE_NAME (COLUMN_NAME)";
    }

    /**
     * 刷新
     */
    @FXML
    private void refresh() {
        if (!MessageBox.confirm(ShellI18nHelper.confirmRefreshData(), this.getWindow())) {
            return;
        }
        try {
            this.init(this.index, this.target);
            this.flushTab();
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
        }
    }

    /**
     * 保存
     */
    @FXML
    private void save() {
        StageManager.showMask(this.getWindow(), this::doSave);
    }

    /**
     * 执行保存
     */
    private void doSave() {
        try {
            String definition = this.definition.getTextTrim();
            // 解析为多条语句，注释内容不参与执行
            List<String> sqlList = DBSqlParser.getParser(definition, this.dialect()).parseSql()
                    .stream().filter(StringUtil::isNotBlank).toList();
            if (CollectionUtil.isEmpty(sqlList)) {
                MessageBox.warn(I18nHelper.pleaseInputContent(), this.getWindow());
                return;
            }
            // 修改索引时，如果提交的是创建语句，则先删除原索引（多数数据库不支持直接修改索引的字段）
            if (!this.newData && this.needDropBeforeSave(sqlList)) {
                this.target.dropIndex(this.index.getName(), this.index.getTableName());
            }
            // 执行索引定义
            this.target.executeIndexSql(sqlList);
            // 更新保存标志位
            this.unsaved = false;
            // 更新新数据标志位
            this.newData = false;
            // 更新索引名称与所属表
            String indexName = this.parseIndexName(sqlList);
            if (StringUtil.isNotBlank(indexName)) {
                this.refreshIndexInfo(indexName);
            }
            // 刷新树节点
            this.target.reloadIndexes();
            // 重新读取索引定义
            FXUtil.runWait(this::initInfo);
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
        } finally {
            this.flushTab();
        }
    }

    /**
     * 更新索引名称与所属表
     *
     * @param indexName 索引名称
     */
    private void refreshIndexInfo(String indexName) {
        this.index.setName(indexName);
        try {
            for (DBIndex index : this.target.selectIndexes()) {
                if (StringUtil.equalsIgnoreCase(index.getName(), indexName)) {
                    this.index.setTableName(index.getTableName());
                    break;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 解析索引名称
     *
     * @param sqlList SQL列表
     * @return 索引名称，未解析到返回null
     */
    private String parseIndexName(List<String> sqlList) {
        for (String sql : sqlList) {
            String indexName = ShellIndexHelper.parseIndexName(sql);
            if (StringUtil.isNotBlank(indexName)) {
                return indexName;
            }
        }
        return null;
    }

    /**
     * 保存前是否需要删除原索引
     *
     * @param sqlList SQL列表
     * @return 是否需要删除
     */
    private boolean needDropBeforeSave(List<String> sqlList) {
        // 索引名称不存在，或者未提交创建语句，无需删除
        if (StringUtil.isBlank(this.index.getName()) || sqlList.stream().noneMatch(ShellIndexHelper::isCreateIndexSql)) {
            return false;
        }
        // 已经包含删除语句，无需重复删除
        if (sqlList.stream().anyMatch(ShellIndexHelper::isDropIndexSql)) {
            return false;
        }
        return this.target.existIndex(this.index.getName());
    }

    /**
     * 获取数据库方言
     *
     * @return 数据库方言
     */
    private DBDialect dialect() {
        return this.target.dialect();
    }

    /**
     * 获取索引设计目标
     *
     * @return 索引设计目标
     */
    public ShellIndexTarget getTarget() {
        return target;
    }

    /**
     * 获取索引对象
     *
     * @return 索引对象
     */
    public DBIndex getIndex() {
        return index;
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
     * 是否新索引
     *
     * @return 是否新索引
     */
    public boolean isNew() {
        return newData;
    }

    @Override
    protected void bindListeners() {
        super.bindListeners();
        // 监听事件
        NodeUtil.nodeOnCtrlS(this.getTab() == null ? null : this.getTab().getContent(), this::save);
        NodeUtil.nodeOnCtrlS(this.definition, this::save);
    }
}
