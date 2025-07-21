// package cn.oyzh.easyshell.fx.file;
//
// import cn.oyzh.fx.plus.controls.label.FXLabel;
// import cn.oyzh.fx.plus.controls.pane.FXFlowPane;
// import javafx.scene.Cursor;
//
// /**
//  * @author oyzh
//  * @since 2025-07-21
//  */
// public class ShellFileLocationLabels extends FXFlowPane {
//
//     public void parseLocation(String location) {
//         this.clearChild();
//         String[] arr = location.split("/");
//         for (String s : arr) {
//             FXLabel label = new FXLabel(s+" >");
//             label.setSnapToPixel(true);
//             label.setCursor(Cursor.HAND);
//             this.addChild(label);
//         }
//     }
// }
