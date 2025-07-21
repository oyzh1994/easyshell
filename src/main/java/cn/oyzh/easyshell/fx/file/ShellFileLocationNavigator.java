// package cn.oyzh.easyshell.fx.file;
//
// import cn.oyzh.easyshell.domain.ShellFileCollect;
// import cn.oyzh.fx.plus.controls.pane.FXPane;
//
// import java.util.List;
// import java.util.function.Consumer;
// import java.util.function.Supplier;
//
// /**
//  * @author oyzh
//  * @since 2025-07-21
//  */
// public class ShellFileLocationNavigator extends FXPane {
//
//     private ShellFileLocationLabels locationLabels;
//
//     private ShellFileLocationTextField locationTextField;
//
//     {
//         this.locationLabels = new ShellFileLocationLabels();
//         this.locationLabels.managedBindVisible();
//         this.locationLabels.setVisible(false);
//
//         this.locationTextField = new ShellFileLocationTextField();
//         this.locationTextField.managedBindVisible();
//         this.locationTextField.setVisible(true);
//
//         this.locationTextField.addTextChangeListener((observableValue, s, t1) -> {
//             this.locationLabels.parseLocation(t1);
//         });
//
//         this.locationTextField.focusedProperty().addListener((observable, oldValue, newValue) -> {
//             if (newValue) {
//                 this.locationLabels.setVisible(false);
//             }else{
//                 this.locationTextField.setVisible(false);
//
//                 this.locationLabels.setVisible(true);
//             }
//         });
//
//         this.addChild(locationTextField);
//         this.addChild(locationLabels);
//     }
//
//     public void setFileCollectSupplier(Supplier<List<ShellFileCollect>> fileCollectSupplier) {
//         this.locationTextField.setFileCollectSupplier(fileCollectSupplier);
//     }
//
//     public void clear() {
//         this.locationTextField.clear();
//     }
//
//     public void text(String t1) {
//         this.locationTextField.text(t1);
//     }
//
//     public void setOnJumpLocation(Consumer<String> onJumpLocation) {
//         this.locationTextField.setOnJumpLocation(onJumpLocation);
//     }
// }
