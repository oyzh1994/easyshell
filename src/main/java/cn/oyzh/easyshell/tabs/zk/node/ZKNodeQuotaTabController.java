package cn.oyzh.easyshell.tabs.zk.node;

import cn.oyzh.easyshell.trees.zk.ShellZKNodeTreeItem;
import cn.oyzh.fx.gui.tabs.SubTabController;
import cn.oyzh.fx.gui.text.field.NumberTextField;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.util.ClipboardUtil;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import org.apache.zookeeper.StatsTrack;

/**
 * zk节点quota组件
 *
 * @author oyzh
 * @since 2025/04/11
 */
public class ZKNodeQuotaTabController extends SubTabController {

    /**
     * 配额tab
     */
    @FXML
    protected FXTab quotaTab;

    /**
     * 子节点数量配额
     */
    @FXML
    protected NumberTextField quotaCount;

    /**
     * 节点数据大小配额
     */
    @FXML
    protected NumberTextField quotaBytes;

    /**
     * 重载配额
     */
    @FXML
    private void reloadQuota() {
        try {
            this.activeItem().refreshQuota();
            this.initQuota();
            MessageBox.okToast(I18nHelper.operationSuccess());
        } catch (Exception ex) {
            MessageBox.exception(ex);
        }
    }

    /**
     * 复制配额
     */
    @FXML
    private void copyQuota() {
        try {
            if (this.activeItem() == null) {
                return;
            }
            StatsTrack quota = this.activeItem().quota();
            String builder;
            if (quota == null) {
                builder = I18nHelper.count() + " -1" + System.lineSeparator() + I18nHelper.bytes() + " -1";
            } else {
                builder = I18nHelper.count() + " " + quota.getCount() + System.lineSeparator() + I18nHelper.bytes() + " " + quota.getBytes();
            }
            ClipboardUtil.setStringAndTip(builder);
            MessageBox.okToast(I18nHelper.operationSuccess());
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    /**
     * 初始化配额
     */
    public void initQuota() throws Exception {
        // if (this.treeItem() == null) {
        //     return;
        // }
        if (this.activeItem().isRootNode()) {
            this.quotaTab.getContent().setDisable(true);
        } else {
            this.quotaTab.getContent().setDisable(false);
            StatsTrack quota = this.activeItem().quota();
            if (quota != null) {
                this.quotaCount.setValue(quota.getCount());
                this.quotaBytes.setValue(quota.getBytes());
            } else {
                this.quotaCount.setValue(-1);
                this.quotaBytes.setValue(-1);
            }
        }
    }

    /**
     * 保存配额
     */
    @FXML
    private void saveQuota() {
        try {
            this.activeItem().saveQuota(this.quotaBytes.getLongValue(), this.quotaCount.getIntValue());
            MessageBox.okToast(I18nHelper.operationSuccess());
        } catch (Exception ex) {
            MessageBox.exception(ex);
        }
    }

    /**
     * 清除子节点数量配额
     */
    @FXML
    private void clearQuotaCount() {
        this.quotaCount.setValue(-1);
    }

    /**
     * 清除数据大小配额
     */
    @FXML
    private void clearQuotaBytes() {
        this.quotaBytes.setValue(-1);
    }

    // private ZKConnectTreeItem treeItem() {
    //     return this.parent().getTreeItem();
    // }

    private ShellZKNodeTreeItem activeItem() {
        return this.parent().getActiveItem();
    }

    @Override
    public ZKNodeTabController parent() {
        return (ZKNodeTabController) super.parent();
    }
}