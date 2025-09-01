package cn.oyzh.easyshell.tabs.redis.key;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.redis.RedisClient;
import cn.oyzh.easyshell.trees.redis.RedisKeyTreeItem;
import cn.oyzh.fx.gui.tabs.RichTab;

/**
 * @author oyzh
 * @since 2024-12-03
 */
public class RedisKeysTab extends RichTab {

    public RedisKeysTab(ShellConnect connect ) {
        super();
        this.controller().init(connect);
        super.flush();
    }

    public void flushData() {
        this.controller().initData();
    }

    @Override
    protected String getTabTitle() {
        // String name = this.redisConnect().getName();
        // Integer dbIndex = this.treeItem().getInnerDbIndex();
        // if (dbIndex != null) {
        //     name += "@" + dbIndex;
        // }
        // RedisKeyTreeItem keyItem = this.activeItem();
        // if (keyItem != null) {
        //     name += "#" + keyItem.key();
        // }
        // return name;
        return super.getTabTitle();
    }

    @Override
    public void flushGraphic() {
        // if (this.treeItem() == null) {
        //     return;
        // }
        // SVGGlyph graphic = this.treeItem().itemGraphic();
        // if (graphic == null) {
        //     return;
        // }
        // SVGGlyph glyph = (SVGGlyph) this.getGraphic();
        // if (glyph == null || !StringUtil.notEquals(glyph.getUrl(), graphic.getUrl())) {
        //     glyph = graphic.clone();
        //     glyph.disableTheme();
        //     this.setGraphic(glyph);
        // }
    }

    @Override
    public void flushGraphicColor() {
        // SVGGlyph graphic = this.treeItem().itemGraphic();
        // if (graphic == null) {
        //     return;
        // }
        // SVGGlyph glyph = (SVGGlyph) this.getGraphic();
        // if (glyph == null) {
        //     return;
        // }
        // if (graphic.getColor() != glyph.getColor()) {
        //     glyph.setColor(graphic.getColor());
        // }
    }

    /**
     * redis键节点
     */
    public RedisKeyTreeItem activeItem() {
        return this.controller().getActiveItem();
    }

    @Override
    protected String url() {
        return "/tabs/redis/key/redisKeysTab.fxml";
    }

    @Override
    protected RedisKeysTabController controller() {
        return (RedisKeysTabController) super.controller();
    }

    /**
     * ttl更新事件
     */
    public void flushTTL() {
        this.controller().flushTTL();
    }

    public int dbIndex() {
       return this.controller().dbIndex();
    }

    public RedisClient client() {
        return this.controller().getClient();
    }

    public ShellConnect redisConnect() {
        return this.client().redisConnect();
    }

}
