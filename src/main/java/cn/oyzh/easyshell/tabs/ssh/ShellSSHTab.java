package cn.oyzh.easyshell.tabs.ssh;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellSetting;
import cn.oyzh.easyshell.fx.ShellOsTypeComboBox;
import cn.oyzh.easyshell.ssh.ShellSSHClient;
import cn.oyzh.easyshell.store.ShellSettingStore;
import cn.oyzh.easyshell.tabs.ShellTermTab;
import cn.oyzh.easyshell.trees.connect.ShellConnectTreeItem;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import javafx.scene.Cursor;

/**
 * ssh-组件tab
 *
 * @author oyzh
 * @since 2023/7/21
 */
public class ShellSSHTab extends ShellTermTab {

    public ShellSSHTab(ShellConnectTreeItem item) {
        this.init(item);
    }

    @Override
    protected String url() {
        ShellSetting setting = ShellSettingStore.SETTING;
        // 效率模式
        if (setting.isEfficiencyMode()) {
            return "/tabs/ssh/shellSSHTab2.fxml";
        }
        return "/tabs/ssh/shellSSHTab.fxml";
    }

    @Override
    public void flushGraphic() {
        SVGGlyph graphic = (SVGGlyph) this.getGraphic();
        if (graphic == null) {
            graphic = ShellOsTypeComboBox.getGlyph(this.shellConnect().getOsType());
            graphic.setSizeStr("13");
            graphic.setCursor(Cursor.DEFAULT);
            this.setGraphic(graphic);
        }
    }

    /**
     * 初始化
     *
     * @param treeItem shell信息
     */
    public void init(ShellConnectTreeItem treeItem) {
        try {
            // 初始化shell连接
            this.controller().init(treeItem);
            // 刷新图标
            this.flush();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected String getTabTitle() {
        return this.controller().shellConnect().getName() + "(" + this.shellConnect().getType().toUpperCase() + ")";
    }

    @Override
    public ShellSSHTabController controller() {
        return (ShellSSHTabController) super.controller();
    }

    /**
     * shell信息
     *
     * @return 当前shell信息
     */
    public ShellConnect shellConnect() {
        return this.controller().shellConnect();
    }

    /**
     * 获取shell客户端
     *
     * @return shell客户端
     */
    public ShellSSHClient client() {
        return this.controller().getClient();
    }

    // @Override
    // public void onNodeInitialize() {
    //     NodeLifeCycle.super.onNodeInitialize();
    //     // 更新一下内容
    //     StageAdapter adapter = StageManager.getAdapter(this.window());
    //     if (adapter != null) {
    //         adapter.updateContentLater();
    //     }
    // }

    @Override
    public void runSnippet(String content) throws Exception {
        super.runSnippet(content);
        this.controller().runSnippet(content);
    }
}
