package cn.oyzh.easyshell.tabs.valkey;

import cn.oyzh.common.util.UUIDUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.fx.ShellOsTypeComboBox;
import cn.oyzh.easyshell.llm.LLMTabCapability;
import cn.oyzh.easyshell.valkey.ShellValkeyClient;
import cn.oyzh.easyshell.valkey.ShellValkeyLLMTools;
import cn.oyzh.easyshell.tabs.ShellConnectTab;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import javafx.scene.Cursor;

import java.util.function.Supplier;

/**
 * redis标签页
 *
 * @author oyzh
 * @since 2025-09-01
 */
public class ShellValkeyTab extends ShellConnectTab implements LLMTabCapability {

    //public ShellValkeyTab(ShellConnect connect) {
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
        return "/tabs/valkey/shellValkeyTab.fxml";
    }

    @Override
    protected ShellValkeyTabController controller() {
        return (ShellValkeyTabController) super.controller();
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
    public ShellValkeyClient client() {
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
    public Supplier<ShellValkeyLLMTools> getToolsFunc() {
        return () -> {
            ShellValkeyLLMTools llmTools = new ShellValkeyLLMTools();
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
     * 创建redis标签页
     *
     * @param connect shell连接
     * @return redis标签页
     */
    public static ShellValkeyTab of(ShellConnect connect) {
        ShellValkeyTab tab = new ShellValkeyTab();
        tab.init(connect);
        return tab;
    }
}
