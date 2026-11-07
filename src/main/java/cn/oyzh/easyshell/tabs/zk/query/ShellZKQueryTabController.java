package cn.oyzh.easyshell.tabs.zk.query;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellQuery;
import cn.oyzh.easyshell.query.zk.ShellZKQueryEditor;
import cn.oyzh.easyshell.query.zk.ShellZKQueryParam;
import cn.oyzh.easyshell.query.zk.ShellZKQueryResult;
import cn.oyzh.easyshell.store.ShellQueryStore;
import cn.oyzh.easyshell.trees.zk.query.ShellZKQueriesTreeItem;
import cn.oyzh.easyshell.zk.ShellZKClient;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.plus.controls.pane.FXSplitPane;
import cn.oyzh.fx.plus.controls.tab.FXTabPane;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.keyboard.KeyboardUtil;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.scene.input.KeyEvent;

/**
 * zk查询tab内容组件
 *
 * @author oyzh
 * @since 2025-09-04
 */
public class ShellZKQueryTabController extends RichTabController {

    /**
     * 查询对象
     */
    private ShellQuery query;

    /**
     * 查询分类节点
     */
    private ShellZKQueriesTreeItem queriesItem;

    /**
     * zk客户端
     */
    private ShellZKClient zkClient;

    /**
     * 未保存标志位
     */
    private boolean unsaved;

    /**
     * 当前内容
     */
    @FXML
    private ShellZKQueryEditor content;

    /**
     * 分割面板
     */
    @FXML
    private FXSplitPane splitPane;

    /**
     * 结果面板
     */
    @FXML
    private FXTabPane resultTabPane;

    /**
     * 查询存储
     */
    private final ShellQueryStore queryStore = ShellQueryStore.INSTANCE;

    /**
     * 获取shell连接信息
     *
     * @return shell连接信息
     */
    public ShellConnect shellConnect() {
        return this.zkClient.getShellConnect();
    }

    /**
     * 获取查询对象
     *
     * @return 查询对象
     */
    public ShellQuery getQuery() {
        return query;
    }

    /**
     * 是否未保存
     *
     * @return 结果
     */
    public boolean isUnsaved() {
        return unsaved;
    }

    /**
     * 初始化
     *
     * @param query       查询对象
     * @param queriesItem 查询分类节点
     */
    public void init(ShellQuery query, ShellZKQueriesTreeItem queriesItem) {
        this.query = query;
        this.queriesItem = queriesItem;
        this.zkClient = queriesItem.client();
        this.content.setClient(this.zkClient);
        this.content.setText(query.getContent() == null ? "" : query.getContent());
        this.content.forgetHistory();
        // 监听内容变化
        this.content.addTextChangeListener((observable, oldValue, newValue) -> {
            if (StringUtil.notEquals(newValue, this.query.getContent())) {
                this.unsaved = true;
                this.flushTab();
            }
        });
        this.flushTab();
    }

    /**
     * 重命名查询
     *
     * @param name 查询名称
     */
    public void renameQuery(String name) {
        if (this.query != null) {
            this.query.setName(name);
            this.flushTab();
        }
    }

    /**
     * 保存查询
     */
    @FXML
    private void save() {
        try {
            String name = this.query.getName();
            if (StringUtil.isBlank(name)) {
                name = MessageBox.prompt(I18nHelper.pleaseInputName(), this.getWindow());
                if (StringUtil.isBlank(name)) {
                    return;
                }
                this.query.setName(name);
            }
            this.query.setContent(this.content.getText());
            this.query.setIid(this.zkClient.iid());
            boolean result;
            if (this.query.isNew()) {// 新增查询
                result = this.queryStore.insert(this.query);
                if (result && this.queriesItem != null) {
                    this.queriesItem.addQuery(this.query);
                }
            } else {// 修改查询
                result = this.queryStore.update(this.query);
            }
            if (result) {
                this.unsaved = false;
                this.flushTab();
            } else {
                MessageBox.warn(I18nHelper.operationFail(), this.getWindow());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, this.getWindow());
        }
    }

    /**
     * 运行查询
     */
    @FXML
    private void run() {
        StageManager.showMask(this.getWindow(), () -> {
            try {
                ShellZKQueryParam param = new ShellZKQueryParam();
                param.setContent(this.content.getText());
                ShellZKQueryResult result = this.zkClient.query(param);
                if (!this.splitPane.isShowDivider()) {
                    this.splitPane.setShowDivider(true);
                    this.splitPane.setDividerPositions(0.3, 0.7);
                }
                this.resultTabPane.setVisible(true);
                this.resultTabPane.clearChild();
                this.resultTabPane.addTab(ShellZKQueryMsgTab.of(param, result));
                if (param.isGet()) {
                    if (result.isSuccess()) {
                        this.resultTabPane.addTab(ShellZKQueryDataTab.of(param.getPath(), result.asData(), this.zkClient));
                        if (param.hasParamStat()) {
                            this.resultTabPane.addTab(ShellZKQueryStatTab.of(result.getStat()));
                        }
                        this.resultTabPane.select(1);
                    } else {
                        this.resultTabPane.select(0);
                    }
                } else if (param.isLs() || param.isLs2()) {
                    if (result.isSuccess()) {
                        this.resultTabPane.addTab(ShellZKQueryNodeTab.of(param.getPath(), result.asNode()));
                        if (param.hasParamStat()) {
                            this.resultTabPane.addTab(ShellZKQueryStatTab.of(result.getStat()));
                        }
                        this.resultTabPane.select(1);
                    } else {
                        this.resultTabPane.select(0);
                    }
                } else if (param.isGetEphemerals()) {
                    if (result.isSuccess()) {
                        String path = param.getPath() == null ? "/" : param.getPath();
                        this.resultTabPane.addTab(ShellZKQueryNodeTab.of(path, result.asNode()));
                        this.resultTabPane.select(1);
                    } else {
                        this.resultTabPane.select(0);
                    }
                } else if (param.isWhoami()) {
                    if (result.isSuccess()) {
                        this.resultTabPane.addTab(ShellZKQueryWhoamiTab.of(result.asClientInfo()));
                        this.resultTabPane.select(1);
                    } else {
                        this.resultTabPane.select(0);
                    }
                } else if (param.isSrvr() || param.isEnvi() || param.isMntr() || param.isConf() || param.isStat4()) {
                    if (result.isSuccess()) {
                        this.resultTabPane.addTab(ShellZKQueryEnvTab.of(result.asEnvInfo()));
                        this.resultTabPane.select(1);
                    } else {
                        this.resultTabPane.select(0);
                    }
                } else if (param.isSet() || param.isSetACL()) {
                    if (result.isSuccess() && param.hasParamStat()) {
                        this.resultTabPane.addTab(ShellZKQueryStatTab.of(result.getStat()));
                    }
                    this.resultTabPane.select(0);
                } else if (param.isGetACL()) {
                    if (result.isSuccess()) {
                        this.resultTabPane.addTab(ShellZKQueryACLTab.of(result.asACL()));
                        if (param.hasParamStat()) {
                            this.resultTabPane.addTab(ShellZKQueryStatTab.of(result.getStat()));
                        }
                        this.resultTabPane.select(1);
                    } else {
                        this.resultTabPane.select(0);
                    }
                } else if (param.isCreate() || param.isSync() || param.isSetQuota() || param.isRmr() || param.isDeleteall()
                        || param.isDelete()) {
                    this.resultTabPane.select(0);
                } else if (param.isStat()) {
                    if (result.isSuccess()) {
                        this.resultTabPane.addTab(ShellZKQueryStatTab.of(result.getStat()));
                        this.resultTabPane.select(1);
                    } else {
                        this.resultTabPane.select(0);
                    }
                } else if (param.isListquota()) {
                    if (result.isSuccess()) {
                        this.resultTabPane.addTab(ShellZKQueryQuotaTab.of(result.asQuota()));
                        this.resultTabPane.select(1);
                    } else {
                        this.resultTabPane.select(0);
                    }
                }
                this.content.parentAutosize();
            } catch (Exception ex) {
                ex.printStackTrace();
                MessageBox.exception(ex, this.getWindow());
            }
        });
    }

    /**
     * 内容按键事件
     *
     * @param event 事件
     */
    @FXML
    private void onContentKeyPressed(KeyEvent event) {
        if (KeyboardUtil.isCtrlS(event)) {
            this.save();
        } else if (KeyboardUtil.isCtrlR(event)) {
            this.run();
        }
    }
}
