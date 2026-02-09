//package cn.oyzh.easyshell.tabs.main;
//
//import cn.oyzh.fx.gui.svg.glyph.HomeSVGGlyph;
//import cn.oyzh.fx.gui.tabs.RichTab;
//import cn.oyzh.i18n.I18nHelper;
//import javafx.scene.Cursor;
//
///**
// * shell主页tab
// *
// * @author oyzh
// * @since 2023/6/24
// */
//public class ShellMainTab extends RichTab {
//
//    public ShellMainTab() {
//        super();
//        super.flush();
//        this.setClosable(false);
//    }
//
//    @Override
//    protected String url() {
//        return "/tabs/main/shellMainTab.fxml";
//    }
//
//    @Override
//    public void flushGraphic() {
//        HomeSVGGlyph glyph = (HomeSVGGlyph) this.getGraphic();
//        if (glyph == null) {
//            glyph = new HomeSVGGlyph("12");
//            glyph.setCursor(Cursor.DEFAULT);
//            this.graphic(glyph);
//        }
//    }
//
//    @Override
//    public String getTabTitle() {
//        return I18nHelper.homeTitle();
//    }
//
//}
