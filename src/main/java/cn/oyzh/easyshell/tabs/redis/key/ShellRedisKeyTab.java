package cn.oyzh.easyshell.tabs.redis.key;

import cn.oyzh.easyshell.tabs.redis.ShellRedisBaseTab;
import cn.oyzh.easyshell.trees.redis.database.ShellRedisDatabaseTreeItem;
import cn.oyzh.easyshell.trees.redis.key.ShellRedisKeyTreeItem;
import cn.oyzh.fx.gui.svg.glyph.key.KeySVGGlyph;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import javafx.scene.Cursor;

/**
 * redis键标签页
 *
 * @author oyzh
 * @since 2026-11-07
 */
public class ShellRedisKeyTab extends ShellRedisBaseTab {

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "redis/key/shellRedisKeyTab.fxml";
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
        ShellRedisKeyTreeItem item = this.item();
        if (item != null) {
            this.setText(item.key() + "@" + this.dbName() + "(" + this.connectName() + ")");
        }
    }

    /**
     * 初始化
     *
     * @param item 键节点
     */
    public void init(ShellRedisKeyTreeItem item) {
        this.controller().init(item);
        this.flush();
    }

    @Override
    public ShellRedisKeyTabController controller() {
        return (ShellRedisKeyTabController) super.controller();
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
    public ShellRedisKeyTreeItem item() {
        ShellRedisKeyTabController controller = this.controller();
        return controller == null ? null : controller.item();
    }

    /**
     * 获取键名称
     *
     * @return 键名称
     */
    public String key() {
        ShellRedisKeyTreeItem item = this.item();
        return item == null ? null : item.key();
    }

    @Override
    public ShellRedisDatabaseTreeItem dbItem() {
        ShellRedisKeyTreeItem item = this.item();
        return item == null ? null : item.dbItem();
    }
}
