package cn.oyzh.easyshell.tabs.dameng;

import cn.oyzh.common.util.UUIDUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.fx.ShellOsTypeComboBox;
import cn.oyzh.easyshell.dameng.ShellDamengClient;
import cn.oyzh.easyshell.dameng.ShellDamengLLMTools;
import cn.oyzh.easyshell.llm.LLMTabCapability;
import cn.oyzh.easyshell.tabs.ShellConnectTab;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import javafx.scene.Cursor;

import java.util.function.Supplier;

/**
 * 达梦数据库连接标签页，负责承载达梦数据库的主界面与客户端
 *
 * @author oyzh
 * @since 2025-09-01
 */
public class ShellDamengTab extends ShellConnectTab implements LLMTabCapability {

    //public ShellDamengTab(ShellConnect connect) {
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
        return "/tabs/dameng/shellDamengTab.fxml";
    }

    @Override
    protected ShellDamengTabController controller() {
        return (ShellDamengTabController) super.controller();
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
    public ShellDamengClient client() {
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
    public Supplier<ShellDamengLLMTools> getToolsFunc() {
        return () -> {
            ShellDamengLLMTools llmTools = new ShellDamengLLMTools();
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
    public static ShellDamengTab of(ShellConnect connect) {
        ShellDamengTab tab = new ShellDamengTab();
        tab.init(connect);
        return tab;
    }
}
