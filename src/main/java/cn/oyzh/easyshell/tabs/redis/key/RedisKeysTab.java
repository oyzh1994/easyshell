// package cn.oyzh.easyshell.tabs.redis.key;
//
// import cn.oyzh.easyshell.domain.ShellConnect;
// import cn.oyzh.easyshell.fx.ShellOsTypeComboBox;
// import cn.oyzh.easyshell.redis.ShellRedisClient;
// import cn.oyzh.easyshell.trees.redis.ShellRedisKeyTreeItem;
// import cn.oyzh.fx.gui.tabs.RichTab;
// import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
// import javafx.scene.Cursor;
//
// /**
//  * @author oyzh
//  * @since 2024-12-03
//  */
// public class RedisKeysTab extends RichTab {
//
//     public RedisKeysTab(  ) {
//         super();
//     }
//
//     public RedisKeysTab(ShellConnect connect ) {
//         super();
//         this.controller().init(connect);
//         super.flush();
//     }
//
//     public void flushData() {
//         this.controller().initData();
//     }
//
//     @Override
//     public String getTabTitle() {
//         return this.shellConnect().getName() + "(" + this.shellConnect().getType().toUpperCase() + ")";
//     }
//
//     @Override
//     public void flushGraphic() {
//         SVGGlyph graphic = (SVGGlyph) this.getGraphic();
//         if (graphic == null) {
//             graphic = ShellOsTypeComboBox.getGlyph(this.shellConnect().getOsType());
//             graphic.setSizeStr("13");
//             graphic.setCursor(Cursor.DEFAULT);
//             this.setGraphic(graphic);
//         }
//     }
//
//     /**
//      * redis键节点
//      */
//     public ShellRedisKeyTreeItem activeItem() {
//         return this.controller().getActiveItem();
//     }
//
//     @Override
//     protected String url() {
//         return "/tabs/redis/key/shellRedisKeysTab.fxml";
//     }
//
//     @Override
//     protected ShellRedisKeysTabController controller() {
//         return (ShellRedisKeysTabController) super.controller();
//     }
//
//     /**
//      * ttl更新事件
//      */
//     public void flushTTL() {
//         this.controller().flushTTL();
//     }
//
//     // public int dbIndex() {
//     //    return this.controller().dbIndex();
//     // }
//
//     public ShellRedisClient client() {
//         return this.controller().getClient();
//     }
//
//     public ShellConnect shellConnect() {
//         return this.client().shellConnect();
//     }
//
// }
