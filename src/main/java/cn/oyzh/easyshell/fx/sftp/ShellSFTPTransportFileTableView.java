//package cn.oyzh.easyshell.fx.sftp;
//
//import cn.oyzh.easyshell.sftp.ShellSFTPFile;
//import cn.oyzh.fx.gui.menu.MenuItemHelper;
//import cn.oyzh.fx.plus.menu.FXMenuItem;
//import javafx.scene.control.MenuItem;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.function.Consumer;
//
///**
// * @author oyzh
// * @since 2025-03-05
// */
//public class ShellSFTPTransportFileTableView extends ShellSFTPFileBaseTableView {
//
////    @Override
////    public void loadFile() {
////        StageManager.showMask(() -> {
////            try {
////                super.loadFileInner();
////            } catch (Exception ex) {
////                ex.printStackTrace();
////                MessageBox.exception(ex);
////            }
////        });
////    }
//
//    private Consumer<List<ShellSFTPFile>> transportCallback;
//
//    public Consumer<List<ShellSFTPFile>> getTransportCallback() {
//        return transportCallback;
//    }
//
//    public void setTransportCallback(Consumer<List<ShellSFTPFile>> transportCallback) {
//        this.transportCallback = transportCallback;
//    }
//
//    @Override
//    public List<? extends MenuItem> getMenuItems() {
//        List<ShellSFTPFile> files = this.getSelectedItems();
//        // 检查是否包含无效文件
//        if (this.checkInvalid(files)) {
//            return super.getMenuItems();
//        }
//        List<MenuItem> menuItems = new ArrayList<>();
//        // 传输文件
//        FXMenuItem transportFile = MenuItemHelper.transportFile("12", () -> this.transportFile(files));
//        menuItems.add(transportFile);
//        menuItems.add(MenuItemHelper.separator());
//        // 添加父级菜单
//        menuItems.addAll(super.getMenuItems());
//        return menuItems;
//    }
//
//    /**
//     * 传输文件
//     *
//     * @param files 文件列表
//     */
//    private void transportFile(List<ShellSFTPFile> files) {
//        if (files == null || files.isEmpty()) {
//            return;
//        }
//        if (this.transportCallback == null) {
//            return;
//        }
//        this.transportCallback.accept(files);
//    }
//
//}
