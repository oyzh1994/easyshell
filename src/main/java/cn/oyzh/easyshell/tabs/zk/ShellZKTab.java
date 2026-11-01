package cn.oyzh.easyshell.tabs.zk;

import cn.oyzh.common.util.UUIDUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.fx.ShellOsTypeComboBox;
import cn.oyzh.easyshell.llm.LLMTabCapability;
import cn.oyzh.easyshell.tabs.ShellConnectTab;
import cn.oyzh.easyshell.zk.ShellZKClient;
import cn.oyzh.easyshell.zk.ShellZKLLMTools;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import javafx.scene.Cursor;

import java.util.function.Supplier;

/**
 * zk连接标签页
 *
 * @author oyzh
 * @since 2025-09-01
 */
public class ShellZKTab extends ShellConnectTab implements LLMTabCapability {

    //public ShellZKTab(ShellConnect connect) {
    //    super();
    //    this.init(connect);
    //    ObjectWatcherManager.watch(this);
    //}

    @Override
    public String getTabTitle() {
        return this.shellConnect().getName() + "(" + this.shellConnect().getType().toUpperCase() + ")";
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
    protected String url() {
        return "/tabs/zk/shellZKTab.fxml";
    }

    @Override
    protected ShellZKTabController controller() {
        return (ShellZKTabController) super.controller();
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
    public ShellZKClient client() {
        return this.controller().getClient();
    }

    private final String tabId = UUIDUtil.uuidSimple();

    @Override
    public String getTabId() {
        return this.tabId;
    }

    @Override
    public String getTabName() {
        return this.getTabTitle();
    }

    @Override
    public Supplier<ShellZKLLMTools> getToolsFunc() {
        return () -> {
            ShellZKLLMTools llmTools = new ShellZKLLMTools();
            llmTools.setClient(this.client());
            return llmTools;
        };
    }

    //    @Override
    //    protected void onTabClosed(Event event) {
    //        super.onTabClosed(event);
    //        this.destroy();
    //    }
    /**
     * 创建zk连接标签页
     *
     * @param connect 连接
     * @return zk连接标签页
     */
    public static ShellZKTab of(ShellConnect connect) {
        ShellZKTab tab = new ShellZKTab();
        tab.init(connect);
        return tab;
    }
}
