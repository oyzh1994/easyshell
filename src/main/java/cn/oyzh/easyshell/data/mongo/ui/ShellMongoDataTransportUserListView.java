//package cn.oyzh.easyshell.data.mongo.ui;
//
//import cn.oyzh.common.util.CollectionUtil;
//import cn.oyzh.easyshell.data.mongo.dto.ShellMongoDataTransportUser;
//import cn.oyzh.easyshell.mongo.user.MongoUser;
//import cn.oyzh.fx.plus.controls.button.FXCheckBox;
//import cn.oyzh.fx.plus.controls.list.FXListView;
//import cn.oyzh.fx.plus.util.ListViewUtil;
//
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * @author oyzh
// * @since 2024/09/05
// */
//public class ShellMongoDataTransportUserListView extends FXListView<FXCheckBox> {
//
//    private Runnable selectedChanged;
//
//    public Runnable getSelectedChanged() {
//        return selectedChanged;
//    }
//
//    public void setSelectedChanged(Runnable selectedChanged) {
//        this.selectedChanged = selectedChanged;
//    }
//
//    public void of(List<MongoUser> users) {
//        List<ShellMongoDataTransportUser> list = CollectionUtil.newArrayList();
//        for (MongoUser user : users) {
//            ShellMongoDataTransportUser obj = new ShellMongoDataTransportUser();
//            obj.setUser(user.getUser());
//            obj.setPassword(user.getPassword());
//            list.add(obj);
//        }
//        this.init(list);
//    }
//
//    public void init(List<ShellMongoDataTransportUser> users) {
//        this.clearItems();
//        if (CollectionUtil.isNotEmpty(users)) {
//            for (ShellMongoDataTransportUser user : users) {
//                FXCheckBox checkBox = new FXCheckBox();
//                checkBox.setText(user.getName());
//                checkBox.setSelected(user.isSelected());
//                checkBox.setProp("data", user);
//                checkBox.selectedChanged((observable, oldValue, newValue) -> {
//                    user.setSelected(newValue);
//                    if (this.selectedChanged != null) {
//                        this.selectedChanged.run();
//                    }
//                });
//                ListViewUtil.selectRowOnMouseClicked(checkBox);
//                this.addItem(checkBox);
//            }
//        }
//        if (this.selectedChanged != null) {
//            this.selectedChanged.run();
//        }
//    }
//
//    public List<ShellMongoDataTransportUser> getSelectedUsers() {
//        List<ShellMongoDataTransportUser> list = new ArrayList<>();
//        for (FXCheckBox item : this.getItems()) {
//            if (item.isSelected()) {
//                list.add(item.getProp("data"));
//            }
//        }
//        return list;
//    }
//
//    public int getSelectedSize() {
//        int size = 0;
//        for (FXCheckBox item : this.getItems()) {
//            if (item.isSelected()) {
//                size++;
//            }
//        }
//        return size;
//    }
//}
