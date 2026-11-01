package cn.oyzh.easyshell.tabs.mongo;

import cn.oyzh.common.util.UUIDUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.fx.ShellOsTypeComboBox;
import cn.oyzh.easyshell.llm.LLMTabCapability;
import cn.oyzh.easyshell.mongo.ShellMongoClient;
import cn.oyzh.easyshell.mongo.ShellMongoLLMTools;
import cn.oyzh.easyshell.tabs.ShellConnectTab;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import javafx.scene.Cursor;

import java.util.function.Supplier;

/**
 * MongoDB 数据库连接标签页
 *
 * @author oyzh
 * @since 2025-09-01
 */
public class ShellMongoTab extends ShellConnectTab implements LLMTabCapability {

    //public ShellMongoTab(ShellConnect connect) {
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
        return "/tabs/mongo/shellMongoTab.fxml";
    }

    @Override
    protected ShellMongoTabController controller() {
        return (ShellMongoTabController) super.controller();
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
    public ShellMongoClient client() {
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
    public Supplier<ShellMongoLLMTools> getToolsFunc() {
        return () -> {
            ShellMongoLLMTools llmTools = new ShellMongoLLMTools();
            llmTools.setClient(this.client());
            return llmTools;
        };
    }

    /**
     * 创建实例
     *
     * @param connect 连接
     * @return 实例对象
     */
    public static ShellMongoTab of(ShellConnect connect) {
        ShellMongoTab tab = new ShellMongoTab();
        tab.init(connect);
        return tab;
    }
}
