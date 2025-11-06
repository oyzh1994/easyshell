package cn.oyzh.easyshell.tabs.mysql.table;// package cn.oyzh.easymysql.tabs.table;
//
// import cn.hutool.core.util.StrUtil;
// import cn.oyzh.easymysql.tabs.MysqlTab;
// import cn.oyzh.easymysql.trees.database.MysqlDatabaseTreeItem;
// import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
// import cn.oyzh.fx.plus.information.MessageBox;
// import cn.oyzh.fx.plus.window.StageManager;
// import cn.oyzh.i18n.I18nHelper;
// import javafx.event.Event;
// import javafx.scene.Cursor;
//
// /**
//  * db表设计tab
//  *
//  * @author oyzh
//  * @since 2024/08/07
//  */
// public class MysqlTableUpdateTab extends MysqlTab {
//
//     {
//         this.setClosable(true);
//     }
//
//     @Override
//     protected String url() {
//         return super.getBasePath() + "table/mysqlTableUpdateTab.fxml";
//     }
//
//     @Override
//     public void flushGraphic() {
//         SVGGlyph graphic = (SVGGlyph) this.getGraphic();
//         if (graphic == null) {
//             graphic = new SVGGlyph("/font/table.svg", "13");
//             graphic.setCursor(Cursor.DEFAULT);
//             this.setGraphic(graphic);
//         }
//     }
//
//     @Override
//     public void flushTitle() {
//         String name = this.tableName();
//         if (StrUtil.isBlank(name)) {
//             name = I18nHelper.unnamedTable();
//         }
//         // 设置提示文本
//         if (this.isUnsaved()) {
//             this.setText("* " + this.dbName() + "-" + name);
//         } else {
//             this.setText(this.dbName() + "-" + name);
//         }
//     }
//
//     public String tableName() {
//         return this.controller().tableName();
//     }
//
//     public String dbName() {
//         return this.controller().dbName();
//     }
//
//     /**
//      * 初始化
//      *
//      * @param tableName db表
//      * @param dbItem    db数据库树节点
//      */
//     public void init(String tableName, MysqlDatabaseTreeItem dbItem) throws Exception {
//         StageManager.showMask(() -> {
//             try {
//                 this.controller().init(tableName, dbItem);
//                 this.flush();
//             } catch (Exception ex) {
//                 MessageBox.exception(ex);
//             }
//         });
//     }
//
//     @Override
//     public MysqlTableDesignTabController controller() {
//         return (MysqlTableDesignTabController) super.controller();
//     }
//
//     public boolean isUnsaved() {
//         return this.controller().isUnsaved();
//     }
//
//     @Override
//     protected void onTabCloseRequest(Event event) {
//         if (this.isUnsaved() && !MessageBox.confirm(I18nHelper.unsavedAndContinue())) {
//             event.consume();
//         } else {
//             this.closeTab();
//         }
//     }
//
//     @Override
//     public MysqlDatabaseTreeItem dbItem() {
//         return this.controller().getDbItem();
//     }
// }
