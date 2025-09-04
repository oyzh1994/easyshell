package cn.oyzh.easyshell.tabs.zk.query;

import cn.oyzh.easyshell.domain.zk.ZKConnect;
import cn.oyzh.easyshell.domain.zk.ZKQuery;
import cn.oyzh.easyshell.zk.ZKClient;
import cn.oyzh.fx.gui.svg.glyph.QuerySVGGlyph;
import cn.oyzh.fx.gui.tabs.RichTab;
import javafx.scene.Cursor;

/**
 * @author oyzh
 * @since 2025/01/20
 */
public class ZKQueryTab extends RichTab {

    public ZKQueryTab(ZKClient client, ZKQuery query) {
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
        return "/tabs/query/zkQueryTab.fxml";
    }

    @Override
    protected ZKQueryTabController controller() {
        return (ZKQueryTabController) super.controller();
    }

    @Override
    public String getTabTitle() {
        if (this.controller().isUnsaved()) {
            return this.query().getName() + " *";
        }
        return this.query().getName();
    }

    public ZKQuery query() {
        return this.controller().getQuery();
    }

    public ZKConnect zkConnect() {
        return this.controller().zkConnect();
    }

    public void init(ZKClient client) {
        this.controller().init(client, null);
    }

    public void init(ZKClient client, ZKQuery query) {
        this.controller().init(client, query);
    }
}
