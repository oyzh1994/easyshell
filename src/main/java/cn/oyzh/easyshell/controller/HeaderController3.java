// package cn.oyzh.easyshell.controller;
//
// import cn.oyzh.common.SysConst;
// import cn.oyzh.easyshell.event.ShellEventUtil;
// import cn.oyzh.easyshell.util.ShellViewFactory;
// import cn.oyzh.fx.plus.controller.StageController;
// import cn.oyzh.fx.plus.information.MessageBox;
// import cn.oyzh.fx.plus.window.StageManager;
// import cn.oyzh.i18n.I18nHelper;
// import javafx.fxml.FXML;
//
// /**
//  * 主页头部业务
//  *
//  * @author oyzh
//  * @since 2022/1/26
//  */
// public class HeaderController3 extends StageController {
//
//     /**
//      * 设置
//      */
//     @FXML
//     private void setting() {
//         ShellViewFactory.setting();
//     }
//
//     /**
//      * 关于
//      */
//     @FXML
//     private void about() {
//         ShellViewFactory.about();
//     }
//
//     /**
//      * 退出
//      */
//     @FXML
//     private void quit() {
//         if (MessageBox.confirm(I18nHelper.quit() + " " + SysConst.projectName())) {
//             StageManager.exit();
//         }
//     }
//
//     /**
//      * 传输数据
//      */
//     @FXML
//     private void transport() {
//         ShellViewFactory.fileTransport(null);
//     }
//
//     /**
//      * 密钥
//      */
//     @FXML
//     private void key() {
//         ShellEventUtil.showKey();
//     }
//
//     /**
//      * 片段
//      */
//     @FXML
//     private void snippet() {
//         ShellViewFactory.snippet();
//     }
//
//     /**
//      * 消息
//      */
//     @FXML
//     private void message() {
//         ShellEventUtil.showMessage();
//     }
//
//     /**
//      * 工具箱
//      */
//     @FXML
//     private void tool() {
//         ShellViewFactory.tool();
//     }
// }
