// package cn.oyzh.easyshell.tabs.zk.server;
//
// import cn.oyzh.easyshell.domain.ShellConnect;
// import cn.oyzh.easyshell.tabs.zk.server.ShellZKServerTabController;
// import cn.oyzh.easyshell.zk.ShellZKClient;
// import cn.oyzh.fx.gui.svg.glyph.ServerSVGGlyph;
// import cn.oyzh.fx.gui.tabs.RichTab;
// import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
// import cn.oyzh.i18n.I18nHelper;
// import javafx.event.Event;
// import javafx.scene.Cursor;
//
// /**
//  * zk服务信息tab
//  *
//  * @author oyzh
//  * @since 2023/08/01
//  */
// public class ZKServerTab extends RichTab {
//
//     @Override
//     public ShellZKServerTabController controller() {
//         return (ShellZKServerTabController) super.controller();
//     }
//
//     @Override
//     protected String url() {
//         return "/tabs/server/shellZKServerTab.fxml";
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
//      * @param client zk客户端
//      */
//     public void init(ShellZKClient client) {
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
//      * zk信息
//      *
//      * @return zk信息
//      */
//     public ShellConnect zkConnect() {
//         return this.controller().getClient().zkConnect();
//     }
//
//     /**
//      * zk客户端
//      *
//      * @return zk客户端
//      */
//     public ShellZKClient client() {
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
