package cn.oyzh.easyshell.tabs.redis.key;

import cn.oyzh.common.dto.Paging;
import cn.oyzh.common.thread.TaskManager;
import cn.oyzh.easyshell.domain.ShellSetting;
import cn.oyzh.easyshell.fx.redis.RedisKeyRowTableView;
import cn.oyzh.easyshell.popups.redis.ShellRedisPageSettingPopupController;
import cn.oyzh.easyshell.redis.key.ShellRedisKeyRow;
import cn.oyzh.easyshell.store.ShellSettingStore;
import cn.oyzh.easyshell.trees.redis.key.RedisRowKeyTreeItem;
import cn.oyzh.fx.gui.page.PageBox;
import cn.oyzh.fx.gui.page.PageEvent;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.plus.controls.box.FXHBox;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.plus.window.PopupAdapter;
import cn.oyzh.fx.plus.window.PopupManager;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;

import java.util.List;

/**
 * redis键tab内容组件，支持行显示
 *
 * @author oyzh
 * @since 2023/06/30
 */
public abstract class RedisRowKeyController<T extends RedisRowKeyTreeItem<R>, R extends ShellRedisKeyRow> extends RedisKeyController<T> {

    /**
     * 分页数据
     */
    protected Paging<R> pageData;

    /**
     * 分页面板
     */
    @FXML
    protected PageBox<R> pagePane;

    /**
     * 数据过滤组件
     */
    @FXML
    protected ClearableTextField filter;

    /**
     * 数据列表
     */
    @FXML
    protected RedisKeyRowTableView<R> listTable;

    /**
     * 数据操作面板
     */
    @FXML
    protected FXHBox dataAction;

    /**
     * 设置
     */
    private final ShellSetting setting = ShellSettingStore.SETTING;

    @Override
    public boolean init(T treeItem) {
        if (super.init(treeItem)) {
            // 过滤处理
            this.filter.addTextChangeListener((t3, t2, t1) -> TaskManager.startDelay(this::firstPage, 50));
            // 设置操作
            this.listTable.setAddAction(this::addRow);
            this.listTable.setCopyAction(this::copyRow);
            this.listTable.setDeleteAction(this::deleteRow);
            return true;
        }
        return false;
    }

    /**
     * 上一页
     */
    @FXML
    protected void prevPage() {
        this.initPage(this.pageData.currentPage() - 1);
    }

    /**
     * 下一页
     */
    @FXML
    protected void nextPage() {
        this.initPage(this.pageData.currentPage() + 1);
    }

    /**
     * 首页
     */
    @FXML
    public void firstPage() {
        this.initPage(0);
    }

    /**
     * 尾页
     */
    @FXML
    protected void lastPage() {
        this.initPage(Integer.MAX_VALUE);
    }

    @Override
    public void reloadKey() {
        // 放弃保存
        if (this.treeItem.isDataUnsaved() && !MessageBox.confirm(I18nHelper.unsavedAndContinue())) {
            return;
        }
        StageManager.showMask(() -> {
            try {
                // 刷新数据
                this.treeItem.refreshKeyValue();
                // 跳转到首页
                this.firstPage();
                // 刷新内存占用
                this.treeItem.flushMemoryUsage();
            } catch (Exception ex) {
                ex.printStackTrace();
                MessageBox.exception(ex);
            }
        });
    }

    /**
     * 添加行
     */
    protected abstract void addRow();

    /**
     * 删除行
     */
    protected abstract void deleteRow();

    /**
     * 获取行列表
     *
     * @return 行列表
     */
    protected abstract List<R> getRows();

    /**
     * 初始化分页
     *
     * @param pageNo 页码
     */
    protected void initPage(long pageNo) {
        StageManager.showMask(() -> {
            try {
                List<R> rows = this.getRows();
                this.pageData = new Paging<>(rows, this.setting.getRowPageLimit());
                List<R> pageRows = this.pageData.page(pageNo);
                this.listTable.setItem(pageRows);
                this.pagePane.setPaging(this.pageData);
            } catch (Exception ex) {
                MessageBox.exception(ex);
            }
        });
    }

    /**
     * 初始化列表控件
     */
    protected void initTable() {
        // 监听列表点击事件
        this.listTable.selectedItemChanged((observable, oldValue, newValue) -> this.initRow(newValue));
    }

    /**
     * 初始化行
     *
     * @param row 当前行
     */
    protected void initRow(R row) {
        this.disableTab();
        FXUtil.runLater(() -> {
            try {
                this.treeItem.currentRow(row);
                this.treeItem.clearData();
                if (row == null) {
                    this.clearRow();
                } else {
                    this.firstShowData();
                }
            } finally {
                this.enableTab();
            }
        }, 20);
//        if (this.dataAction != null) {
//            this.dataAction.setDisable(row == null);
//        }
    }

    /**
     * 复制行
     */
    protected abstract void copyRow();

    /**
     * 清除行
     */
    protected abstract void clearRow();

    /**
     * 页码设置
     */
    @FXML
    private void pageSetting() {
        PopupAdapter popup = PopupManager.parsePopup(ShellRedisPageSettingPopupController.class);
        popup.showPopup(this.pagePane.getSettingBtn());
        int limit = this.setting.getRowPageLimit();
        popup.setSubmitHandler(o -> {
            if (o instanceof Integer l && l != limit) {
                this.firstPage();
            }
        });
    }

    /**
     * 页码跳页
     */
    @FXML
    private void pageJump(PageEvent.PageJumpEvent event) {
        this.initPage(event.getPage());
    }
}
