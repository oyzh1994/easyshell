package cn.oyzh.easyshell.tabs.postgresql;

import cn.oyzh.common.util.UUIDUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.fx.ShellOsTypeComboBox;
import cn.oyzh.easyshell.postgresql.ShellPostgresqlClient;
import cn.oyzh.easyshell.postgresql.ShellPostgresqlLLMTools;
import cn.oyzh.easyshell.llm.LLMTabCapability;
import cn.oyzh.easyshell.tabs.ShellConnectTab;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import javafx.scene.Cursor;

import java.util.function.Supplier;

/**
 * PostgreSQL数据库连接标签页，负责承载PostgreSQL数据库的主界面与客户端
 *
 * @author oyzh
 * @since 2025-09-01
 */
public class ShellPostgresqlTab extends ShellConnectTab implements LLMTabCapability {

    //public ShellPostgresqlTab(ShellConnect connect) {
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
        return "/tabs/postgresql/shellPostgresqlTab.fxml";
    }

    @Override
    protected ShellPostgresqlTabController controller() {
        return (ShellPostgresqlTabController) super.controller();
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
    public ShellPostgresqlClient client() {
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
    public Supplier<ShellPostgresqlLLMTools> getToolsFunc() {
        return () -> {
            ShellPostgresqlLLMTools llmTools = new ShellPostgresqlLLMTools();
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
     * 创建实例
     *
     * @param connect 连接
     * @return 实例对象
     */
    public static ShellPostgresqlTab of(ShellConnect connect) {
        ShellPostgresqlTab tab = new ShellPostgresqlTab();
        tab.init(connect);
        return tab;
    }
}
