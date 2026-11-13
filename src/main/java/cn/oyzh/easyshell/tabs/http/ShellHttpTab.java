package cn.oyzh.easyshell.tabs.http;

import cn.oyzh.common.util.UUIDUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.fx.ShellOsTypeComboBox;
import cn.oyzh.easyshell.http.ShellHttpClient;
import cn.oyzh.easyshell.http.ShellHttpLLMTools;
import cn.oyzh.easyshell.llm.LLMTabCapability;
import cn.oyzh.easyshell.tabs.ShellConnectTab;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.util.FXUtil;
import javafx.scene.Cursor;

import java.util.function.Supplier;

/**
 * HTTP 工作区标签页。
 *
 * @author oyzh
 * @since 2026-11-07
 */
public class ShellHttpTab extends ShellConnectTab implements LLMTabCapability {

    private final String tabId = UUIDUtil.uuidSimple();

    @Override
    protected String getTabTitle() {
        return this.shellConnect().getName() + "(" + this.shellConnect().getType().toUpperCase() + ")";
    }

    public String getTabName() {
        return this.getTabTitle();
    }

    public String getTabId() {
        return tabId;
    }

    @Override
    protected String url() {
        return "/tabs/http/shellHttpTab.fxml";
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
            this.controller().init(connect);
            super.init(connect);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected ShellHttpTabController controller() {
        return (ShellHttpTabController) super.controller();
    }

    @Override
    public ShellHttpClient client() {
        return this.controller().getClient();
    }

    @Override
    public Supplier<ShellHttpLLMTools> getToolsFunc() {
        return () -> {
            ShellHttpLLMTools tools = new ShellHttpLLMTools();
            tools.setClient(this.client());
            tools.setWorkspaceSupplier(this.controller()::getWorkspace);
            tools.setWorkspaceChangeListener((type, id) ->
                    FXUtil.runLater(() -> this.controller().onWorkspaceChanged(type, id)));
            return tools;
        };
    }

    public static ShellHttpTab of(ShellConnect connect) {
        ShellHttpTab tab = new ShellHttpTab();
        tab.init(connect);
        return tab;
    }
}
