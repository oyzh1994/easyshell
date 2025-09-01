package cn.oyzh.easyshell.tabs.redis.query;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.redis.RedisQuery;
import cn.oyzh.easyshell.redis.RedisClient;
import cn.oyzh.fx.gui.svg.glyph.QuerySVGGlyph;
import cn.oyzh.fx.gui.tabs.RichTab;
import javafx.scene.Cursor;

/**
 * @author oyzh
 * @since 2025/02/06
 */
public class RedisQueryTab extends RichTab {

    public RedisQueryTab(RedisClient client, RedisQuery query) {
        super();
        this.init(client, query);
        super.flush();
    }

    @Override
    public void flushGraphic() {
        QuerySVGGlyph glyph = (QuerySVGGlyph) this.getGraphic();
        if (glyph == null) {
            glyph = new QuerySVGGlyph("12");
            glyph.setCursor(Cursor.DEFAULT);
            this.graphic(glyph);
        }
    }

    @Override
    protected String url() {
        return "/tabs/query/redisQueryTab.fxml";
    }

    @Override
    protected RedisQueryTabController controller() {
        return (RedisQueryTabController) super.controller();
    }

    @Override
    public String getTabTitle() {
        if (this.controller().isUnsaved()) {
            return this.query().getName() + " *";
        }
        return this.query().getName();
    }

    public RedisQuery query() {
        return this.controller().getQuery();
    }

    public ShellConnect shellConnect() {
        return this.controller().shellConnect();
    }

    public void init(RedisClient client) {
        this.controller().init(client, null);
    }

    public void init(RedisClient client, RedisQuery query) {
        this.controller().init(client, query);
    }
}
