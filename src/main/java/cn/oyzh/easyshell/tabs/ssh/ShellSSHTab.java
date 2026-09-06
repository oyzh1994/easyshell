package cn.oyzh.easyshell.tabs.ssh;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.fx.ShellOsTypeComboBox;
import cn.oyzh.easyshell.ssh2.ShellSSHClient;
import cn.oyzh.easyshell.tabs.ShellTermTab;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import javafx.scene.Cursor;

/**
 * ssh-组件tab
 *
 * @author oyzh
 * @since 2023/7/21
 */
public class ShellSSHTab extends ShellTermTab {

    //public ShellSSHTab(ShellConnect connect) {
    //    this.init(connect);
    //    ObjectWatcherManager.watch(this);
    //}

    @Override
    protected String url() {
//        ShellSetting setting = ShellSettingStore.SETTING;
        // 效率模式
//        if (setting.isEfficiencyMode()) {
            return "/tabs/ssh/shellSSHTab2.fxml";
//        }
//        return "/tabs/ssh/shellSSHTab.fxml";
    }

    @Override
    public void flushGraphic() {
        SVGGlyph graphic = (SVGGlyph) this.getGraphic();
        if (graphic == null) {
            graphic = ShellOsTypeComboBox.getGlyph(this.shellConnect().getOsType());
            graphic.setCursor(Cursor.DEFAULT);
            this.setGraphic(graphic);
        }
    }

    @Override
    public void init(ShellConnect connect) {
        try {
            // 初始化shell连接
            this.controller().init(connect);
            // 刷新图标
            super.init(connect);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected String getTabTitle() {
        return this.shellConnect().getName() + "(" + this.shellConnect().getType().toUpperCase() + ")";
    }

    @Override
    public ShellSSHTabController controller() {
        return (ShellSSHTabController) super.controller();
    }

    @Override
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
        this.controller().runSnippet(content);
    }

//    @Override
//    protected void onTabClosed(Event event) {
//        super.onTabClosed(event);
//        this.destroy();
//    }
}
