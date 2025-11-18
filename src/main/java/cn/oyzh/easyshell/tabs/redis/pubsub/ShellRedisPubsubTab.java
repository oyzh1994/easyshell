// package cn.oyzh.easyshell.tabs.redis.pubsub;
//
// import cn.oyzh.easyshell.dto.redis.ShellRedisPubsubItem;
// import cn.oyzh.easyshell.fx.svg.glyph.SubscribeSVGGlyph;
// import cn.oyzh.easyshell.redis.ShellRedisClient;
// import cn.oyzh.fx.gui.tabs.RichTab;
// import javafx.event.Event;
// import javafx.scene.Cursor;
//
// /**
//  * redis发布订阅tab
//  *
//  * @author oyzh
//  * @since 2023/08/02
//  */
// public class ShellRedisPubsubTab extends RichTab {
//
//     {
//         this.setClosable(true);
//         // this.setOnCloseRequest(event -> this.unsubscribe());
//         this.loadContent();
//     }
//
//     public ShellRedisPubsubItem getItem() {
//         return item;
//     }
//
//     public void setItem(ShellRedisPubsubItem item) {
//         this.item = item;
//     }
//
//     /**
//      * redis发布及订阅节点
//      */
//     private ShellRedisPubsubItem item;
//
//     @Override
//     public ShellRedisPubsubTabController controller() {
//         return (ShellRedisPubsubTabController) super.controller();
//     }
//
//     @Override
//     protected String url() {
//         return "/tabs/redis/pubsub/shellRedisPubsubTab.fxml";
//     }
//
//     @Override
//     public void flushGraphic() {
//         SubscribeSVGGlyph graphic = (SubscribeSVGGlyph) this.getGraphic();
//         if (graphic == null) {
//             graphic = new SubscribeSVGGlyph("13");
//             graphic.setCursor(Cursor.DEFAULT);
//             this.setGraphic(graphic);
//         }
//     }
//
//     /**
//      * 初始化
//      *
//      * @param item redis发布及订阅节点
//      */
//     public void init(ShellRedisPubsubItem item) {
//         try {
//             this.item = item;
//             // 设置文本
//             this.setText(item.getClient().connectName() + "-" + item.getChannel());
//             // 刷新图标
//             this.flushGraphic();
//             // 初始化
//             this.controller().init(item);
//         } catch (Exception ex) {
//             ex.printStackTrace();
//         }
//     }
//
//     /**
//      * 取消订阅
//      */
//     public void unsubscribe() {
//         this.controller().unsubscribe();
//     }
//
//     @Override
//     protected void onTabClosed(Event event) {
//         this.unsubscribe();
//         super.onTabClosed(event);
//     }
//
//     @Override
//     protected void onTabCloseRequest(Event event) {
//         super.onTabCloseRequest(event);
//         this.unsubscribe();
//     }
//
//     public ShellRedisClient client() {
//         return this.item.getClient();
//     }
//
// }
