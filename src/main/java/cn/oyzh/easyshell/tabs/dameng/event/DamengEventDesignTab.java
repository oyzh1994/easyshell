package cn.oyzh.easyshell.tabs.dameng.event;//package cn.oyzh.easyshell.tabs.dameng.event;
//
//import cn.oyzh.common.util.StringUtil;
//import cn.oyzh.easyshell.dameng.event.DamengEvent;
//import cn.oyzh.easyshell.tabs.dameng.DamengTab;
//import cn.oyzh.easyshell.trees.dameng.database.DamengSchemaTreeItem;
//import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
//import cn.oyzh.fx.plus.information.MessageBox;
//import cn.oyzh.i18n.I18nHelper;
//import javafx.event.Event;
//import javafx.scene.Cursor;
//
///**
// * @author oyzh
// * @since 2024/09/09
// */
//public class DamengEventDesignTab extends DamengTab {
//
//    @Override
//    protected String url() {
//        return super.getBasePath() + "event/damengEventDesignTab.fxml";
//    }
//
//    @Override
//    public void flushGraphic() {
//        SVGGlyph graphic = (SVGGlyph) this.getGraphic();
//        if (graphic == null) {
//            graphic = new SVGGlyph("/font/event.svg");
//            graphic.setCursor(Cursor.DEFAULT);
//            this.setGraphic(graphic);
//        }
//    }
//
//    @Override
//    public void flushTitle() {
//        String name = this.eventName();
//        if (StringUtil.isBlank(name)) {
//            name = I18nHelper.unnamedEvent();
//        }
//        // 设置提示文本
//        if (this.isUnsaved()) {
//            this.setText("* " + this.dbItem().dbName() + "-" + name);
//        } else {
//            this.setText(this.dbItem().dbName() + "-" + name);
//        }
//    }
//
//    public DamengEvent event() {
//        return this.controller().getEvent();
//    }
//
//    public String eventName() {
//        return this.event().getName();
//    }
//
//    @Override
//    public DamengSchemaTreeItem dbItem() {
//        return this.controller().getDbItem();
//    }
//
//    /**
//     * 初始化
//     *
//     * @param event 事件对象
//     * @param item  db库树节点
//     */
//    public void init(DamengEvent event, DamengSchemaTreeItem item) {
//        this.controller().init(event, item);
//        // 刷新tab
//        this.flush();
//    }
//
//    @Override
//    public DamengEventDesignTabController controller() {
//        return (DamengEventDesignTabController) super.controller();
//    }
//
//    public boolean isUnsaved() {
//        return this.controller().isUnsaved();
//    }
//
//    @Override
//    protected void onTabCloseRequest(Event event) {
//        if (this.isUnsaved() && !MessageBox.confirm(I18nHelper.unsavedAndContinue())) {
//            event.consume();
//        } else {
//            this.closeTab();
//        }
//    }
//}
