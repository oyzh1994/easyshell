package cn.oyzh.easyshell.tabs.h2;

import cn.oyzh.common.util.UUIDUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.fx.ShellOsTypeComboBox;
import cn.oyzh.easyshell.llm.LLMTabCapability;
import cn.oyzh.easyshell.h2.ShellH2Client;
import cn.oyzh.easyshell.h2.ShellH2LLMTools;
import cn.oyzh.easyshell.tabs.ShellConnectTab;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import javafx.scene.Cursor;

import java.util.function.Supplier;

/**
 * H2 连接标签页。
 *
 * @since 2026-11-02
 */
public class ShellH2Tab extends ShellConnectTab implements LLMTabCapability {

    private final String tabId = UUIDUtil.uuidSimple();

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
        return "/tabs/h2/shellH2Tab.fxml";
    }

    @Override
    protected ShellH2TabController controller() {
        return (ShellH2TabController) super.controller();
    }

    @Override
    public void init(ShellConnect connect) {
        try {
            this.controller().init(connect);
            super.init(connect);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public ShellH2Client client() {
        return this.controller().getClient();
    }

    @Override
    public String getTabId() {
        return this.tabId;
    }

    @Override
    public String getTabName() {
        return this.getTabTitle();
    }

    @Override
    public Supplier<ShellH2LLMTools> getToolsFunc() {
        return () -> {
            ShellH2LLMTools tools = new ShellH2LLMTools();
            tools.setClient(this.client());
            return tools;
        };
    }

    public static ShellH2Tab of(ShellConnect connect) {
        ShellH2Tab tab = new ShellH2Tab();
        tab.init(connect);
        return tab;
    }
}
