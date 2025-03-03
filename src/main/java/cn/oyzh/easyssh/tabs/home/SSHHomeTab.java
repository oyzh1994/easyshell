package cn.oyzh.easyssh.tabs.home;

import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.ext.FXMLLoaderExt;
import javafx.scene.CacheHint;
import javafx.scene.Cursor;
import javafx.scene.Node;

/**
 * ssh主页tab
 *
 * @author oyzh
 * @since 2023/6/24
 */
public class SSHHomeTab extends RichTab {

    {
        this.setClosable(true);
        this.loadContent();
    }

    @Override
    protected void loadContent() {
        FXMLLoaderExt loaderExt = new FXMLLoaderExt();
        Node content = loaderExt.load("/tabs/home/sshHomeTabContent.fxml");
        content.setCache(true);
        content.setCacheHint(CacheHint.QUALITY);
        this.setContent(content);
        this.setText("主页");
        // 刷新图标
        this.flushGraphic();
    }

    @Override
    public void flushGraphic() {
        SVGGlyph graphic = (SVGGlyph) this.getGraphic();
        if (graphic == null) {
            graphic = new SVGGlyph("/font/home.svg", "13");
            graphic.setCursor(Cursor.DEFAULT);
            this.setGraphic(graphic);
        }
    }
}
