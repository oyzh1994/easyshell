package cn.oyzh.easyshell.tabs.sqlite;

import cn.oyzh.common.util.UUIDUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.fx.ShellOsTypeComboBox;
import cn.oyzh.easyshell.llm.LLMTabCapability;
import cn.oyzh.easyshell.sqlite.ShellSqliteClient;
import cn.oyzh.easyshell.sqlite.ShellSqliteLLMTools;
import cn.oyzh.easyshell.tabs.ShellConnectTab;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import javafx.scene.Cursor;

import java.util.function.Supplier;

/**
 * SQLite 连接标签页。
 *
 * @since 2026-11-02
 */
public class ShellSqliteTab extends ShellConnectTab implements LLMTabCapability {

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
        return "/tabs/sqlite/shellSqliteTab.fxml";
    }

    @Override
    protected ShellSqliteTabController controller() {
        return (ShellSqliteTabController) super.controller();
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

    public ShellSqliteClient client() {
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
    public Supplier<ShellSqliteLLMTools> getToolsFunc() {
        return () -> {
            ShellSqliteLLMTools tools = new ShellSqliteLLMTools();
            tools.setClient(this.client());
            return tools;
        };
    }

    public static ShellSqliteTab of(ShellConnect connect) {
        ShellSqliteTab tab = new ShellSqliteTab();
        tab.init(connect);
        return tab;
    }
}
