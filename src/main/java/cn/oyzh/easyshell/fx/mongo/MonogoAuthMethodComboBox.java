//package cn.oyzh.easyshell.fx.mongo;
//
//import cn.oyzh.fx.plus.controls.combo.FXComboBox;
//import cn.oyzh.i18n.I18nHelper;
//
///**
// *
// * @author oyzh
// * @since 2026-06-01
// */
//public class MonogoAuthMethodComboBox extends FXComboBox<String> {
//
//    public String getType() {
//        if (this.getSelectedIndex() == 0) {
//            return "none";
//        }
//        if (this.getSelectedIndex() == 1) {
//            return "password";
//        }
//        return "unknown";
//    }
//
//    @Override
//    public void select(String obj) {
//        if ("none".equalsIgnoreCase(obj)) {
//            this.select(0);
//        } else if ("password".equalsIgnoreCase(obj)) {
//            this.select(1);
//        }
//    }
//
//    @Override
//    public void initNode() {
//        this.addItem(I18nHelper.none());
//        this.addItem(I18nHelper.password());
//        super.initNode();
//    }
//}
