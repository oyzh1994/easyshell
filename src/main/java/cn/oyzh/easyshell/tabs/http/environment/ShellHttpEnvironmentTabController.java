package cn.oyzh.easyshell.tabs.http.environment;

import cn.oyzh.common.thread.TaskManager;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.fx.http.ShellHttpKeyValueTableView;
import cn.oyzh.easyshell.domain.http.ShellHttpEnvironment;
import cn.oyzh.easyshell.domain.http.ShellHttpRequest;
import cn.oyzh.easyshell.tabs.http.ShellHttpTabPane;
import cn.oyzh.easyshell.store.http.ShellHttpEnvironmentStore;
import cn.oyzh.easyshell.trees.http.environment.ShellHttpEnvironmentTreeItem;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.plus.keyboard.KeyHandler;
import cn.oyzh.fx.plus.keyboard.KeyListener;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.i18n.I18nHelper;
import javafx.collections.FXCollections;
import cn.oyzh.fx.plus.util.FXUtil;
import javafx.fxml.FXML;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.util.concurrent.Future;

/**
 * HTTP 环境标签页控制器。
 *
 * @author oyzh
 * @since 2026-11-07
 */
public class ShellHttpEnvironmentTabController extends RichTabController {

    /**
     * 环境名称
     */
    @FXML
    private ClearableTextField environmentName;

    /**
     * 环境变量表格
     */
    @FXML
    private ShellHttpKeyValueTableView valuesTable;

    /**
     * 环境节点
     */
    private ShellHttpEnvironmentTreeItem item;

    /**
     * 环境
     */
    private ShellHttpEnvironment environment;

    /**
     * 标签页面板
     */
    private ShellHttpTabPane tabPane;

    /**
     * 是否正在加载
     */
    private boolean loading;

    /**
     * 延迟保存任务
     */
    private Future<?> saveFuture;

    /**
     * 初始化
     *
     * @param item    环境节点
     * @param tabPane 标签页面板
     */
    public void init(ShellHttpEnvironmentTreeItem item, ShellHttpTabPane tabPane) {
        this.item = item;
        this.environment = item.environment();
        this.tabPane = tabPane;
        this.loading = true;
        this.environmentName.setText(this.environment.getName());
        this.valuesTable.setChangeAction(this::onValuesChanged);
        this.valuesTable.setItems(FXCollections.observableArrayList(this.environment.getValues()));
        this.loading = false;
        this.environmentName.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!this.loading) {
                this.apply();
                this.scheduleSave();
            }
        });
        this.bindSaveKey();
    }

    /**
     * 绑定保存快捷键
     */
    private void bindSaveKey() {
        KeyHandler saveHandler = new KeyHandler();
        saveHandler.setHandler(e -> this.save());
        saveHandler.setKeyCode(KeyCode.S);
        saveHandler.setMainModifierDown(true);
        saveHandler.setKeyType(KeyEvent.KEY_RELEASED);
        KeyListener.addHandler(this.getTabContent(), saveHandler);
    }

    /**
     * 获取环境
     *
     * @return 环境
     */
    public ShellHttpEnvironment environment() {
        return environment;
    }

    /**
     * 重命名环境
     *
     * @param name 名称
     */
    public void rename(String name) {
        if (this.environment == null) {
            return;
        }
        this.environment.setName(name);
        this.loading = true;
        this.environmentName.setText(name);
        this.loading = false;
    }

    /**
     * 应用数据
     */
    private void apply() {
        if (this.environment == null) {
            return;
        }
        String name = this.environmentName.getTextTrim();
        if (StringUtil.isNotBlank(name) && !name.equals(this.environment.getName())) {
            this.environment.setName(name);
            if (this.item != null) {
                this.item.refreshEnvironment();
            }
            this.tabPane.renameEnvironmentTab(this.environment.getId(), name);
            this.tabPane.refreshEnvironmentCombo();
        }
        this.environment.setValues(ShellHttpRequest.copyRows(this.valuesTable.getItems()));
        this.tabPane.refreshHomeTab();
    }

    /**
     * 延迟保存
     */
    private void onValuesChanged() {
        if (this.loading || this.environment == null) {
            return;
        }
        this.apply();
        this.scheduleSave();
    }

    /**
     * 延迟保存环境。
     */
    private void scheduleSave() {
        if (this.loading || this.environment == null) {
            return;
        }
        if (this.saveFuture != null) {
            this.saveFuture.cancel(false);
        }
        this.saveFuture = TaskManager.startDelay(() -> FXUtil.runLater(
                () -> ShellHttpEnvironmentStore.INSTANCE.update(this.environment)), 300);
    }

    /**
     * 保存
     */
    @FXML
    private void save() {
        this.flush();
        MessageBox.okToast(I18nHelper.operationSuccess(), this.getWindow());
    }

    /**
     * 刷新数据
     */
    public void flush() {
        if (this.saveFuture != null) {
            this.saveFuture.cancel(false);
            this.saveFuture = null;
        }
        this.apply();
        ShellHttpEnvironmentStore.INSTANCE.update(this.environment);
    }

    @FXML
    private void addValue() {
        this.valuesTable.addRow();
    }

    @FXML
    private void deleteValue() {
        this.valuesTable.deleteSelectedRow();
    }
}
