package cn.oyzh.easyshell.tabs.valkey.key;

import cn.oyzh.easyshell.tabs.valkey.ShellValkeyBaseTab;
import cn.oyzh.easyshell.trees.valkey.database.ShellValkeyDatabaseTreeItem;
import cn.oyzh.easyshell.trees.valkey.key.ShellValkeyKeyTreeItem;
import cn.oyzh.fx.gui.svg.glyph.key.KeySVGGlyph;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.i18n.I18nHelper;
import javafx.event.Event;
import javafx.scene.Cursor;

/**
 * redis键标签页
 *
 * @author oyzh
 * @since 2026-11-07
 */
public class ShellValkeyKeyTab extends ShellValkeyBaseTab {

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "valkey/key/shellValkeyKeyTab.fxml";
    }

    @Override
    public void flushGraphic() {
        SVGGlyph graphic = (SVGGlyph) this.getGraphic();
        if (graphic == null) {
            graphic = new KeySVGGlyph();
            graphic.setCursor(Cursor.DEFAULT);
            this.setGraphic(graphic);
        }
    }

    @Override
    public void flushTitle() {
        ShellValkeyKeyTreeItem item = this.item();
        if (item != null) {
            this.setText(item.key() + "@" + this.dbName() + "(" + this.connectName() + ")");
        }
    }

    /**
     * 初始化
     *
     * @param item 键节点
     */
    public void init(ShellValkeyKeyTreeItem item) {
        this.controller().init(item);
        this.flush();
    }

    @Override
    public ShellValkeyKeyTabController controller() {
        return (ShellValkeyKeyTabController) super.controller();
    }

    @Override
    public void reload() {
        this.controller().reloadKey();
    }

    /**
     * 获取键节点
     *
     * @return 键节点
     */
    public ShellValkeyKeyTreeItem item() {
        ShellValkeyKeyTabController controller = this.controller();
        return controller == null ? null : controller.item();
    }

    /**
     * 获取键名称
     *
     * @return 键名称
     */
    public String key() {
        ShellValkeyKeyTreeItem item = this.item();
        return item == null ? null : item.key();
    }

    @Override
    public ShellValkeyDatabaseTreeItem dbItem() {
        ShellValkeyKeyTreeItem item = this.item();
        return item == null ? null : item.dbItem();
    }

    /**
     * 是否有未保存数据
     *
     * @return 结果
     */
    public boolean hasUnsavedData() {
        ShellValkeyKeyTreeItem item = this.item();
        return item != null && item.isDataUnsaved();
    }

    @Override
    protected void onTabCloseRequest(Event event) {
        if (this.hasUnsavedData() && !MessageBox.confirm(I18nHelper.unsavedAndContinue(), this.getWindow())) {
            event.consume();
        } else {
            this.closeTab();
        }
    }
}
