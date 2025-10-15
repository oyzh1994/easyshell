//package cn.oyzh.easyshell.fx;
//
//import cn.oyzh.easyshell.domain.ShellSetting;
//import cn.oyzh.easyshell.store.ShellSettingStore;
//import cn.oyzh.fx.editor.incubator.JsonEditor;
//import cn.oyzh.fx.plus.font.FontManager;
//import javafx.scene.text.Font;
//
///**
// * @author oyzh
// * @since 2025-03-26
// */
//public class ShellJsonEditor extends JsonEditor {
//
////     @Override
////     protected Font initFont() {
//// //        // 禁用字体管理
//// //        super.disableFont();
////         // 初始化字体
////         ShellSetting setting = ShellSettingStore.SETTING;
////         return FontManager.toFont(setting.editorFontConfig());
////     }
//
//    @Override
//    protected Font getEditorFont() {
//        if (super.getEditorFont() == null) {
//            ShellSetting setting = ShellSettingStore.SETTING;
//            Font font = FontManager.toFont(setting.editorFontConfig());
//            super.setEditorFont(font);
//        }
//        return super.getEditorFont();
//    }
//
//    //@Override
//    //public void changeFont(Font font) {
//    //    ShellSetting setting = ShellSettingStore.SETTING;
//    //    Font font1 = FontManager.toFont(setting.editorFontConfig());
//    //    super.changeFont(font1);
//    //}
//}
