// package cn.oyzh.easyshell.tabs.redis.server;
//
// import cn.oyzh.easyshell.domain.ShellConnect;
// import cn.oyzh.easyshell.redis.ShellRedisClient;
// import cn.oyzh.fx.gui.svg.glyph.ServerSVGGlyph;
// import cn.oyzh.fx.gui.tabs.RichTab;
// import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
// import cn.oyzh.i18n.I18nHelper;
// import javafx.event.Event;
// import javafx.scene.Cursor;
//
// /**
//  * redis服务信息tab
//  *
//  * @author oyzh
//  * @since 2023/08/01
//  */
// public class RedisServerTab extends RichTab {
//
//     @Override
//     public ShellRedisServerTabController controller() {
//         return (ShellRedisServerTabController) super.controller();
//     }
//
//     @Override
//     protected String url() {
//         return "/tabs/server/shellRedisServerTab.fxml";
//     }
//
//     @Override
//     public void flushGraphic() {
//         SVGGlyph graphic = (SVGGlyph) this.getGraphic();
//         if (graphic == null) {
//             graphic = new ServerSVGGlyph("13");
//             graphic.setCursor(Cursor.DEFAULT);
//             this.setGraphic(graphic);
//         }
//     }
//
//     /**
//      * 初始化
//      *
//      * @param client redis客户端
//      */
//     public void init(ShellRedisClient client) {
//         try {
//             // 设置文本
//             this.setText(I18nHelper.serverInfo() + "-" + client.connectName());
//             // 刷新图标
//             this.flushGraphic();
//             // 初始化
//             this.controller().init(client);
//         } catch (Exception ex) {
//             ex.printStackTrace();
//         }
//     }
//
//     /**
//      * 关闭刷新任务
//      */
//     public void closeRefreshTask() {
//         this.controller().closeRefreshTask();
//     }
//
//     /**
//      * redis信息
//      *
//      * @return redis信息
//      */
//     public ShellConnect shellConnect() {
//         return this.controller().getClient().shellConnect();
//     }
//
//     /**
//      * redis客户端
//      *
//      * @return redis客户端
//      */
//     public ShellRedisClient client() {
//         return this.controller().getClient();
//     }
//
//     @Override
//     protected void onTabClosed(Event event) {
//         this.closeRefreshTask();
//         super.onTabClosed(event);
//     }
//
//     @Override
//     protected void onTabCloseRequest(Event event) {
//         this.closeRefreshTask();
//         super.onTabCloseRequest(event);
//     }
//
//     @Override
//     public void closeTab() {
//         this.closeRefreshTask();
//         super.closeTab();
//     }
// }
