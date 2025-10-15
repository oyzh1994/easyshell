// package cn.oyzh.easyshell.fx.zk;
//
// import cn.oyzh.easyshell.domain.zk.ZKSetting;
// import cn.oyzh.easyshell.store.zk.ZKSettingStore;
// import cn.oyzh.fx.editor.incubator.Editor;
// import cn.oyzh.fx.plus.font.FontManager;
// import javafx.scene.text.Font;
//
// /**
//  * @author oyzh
//  * @since 2024/12/29
//  */
// public class ZKDataEditor extends Editor {
//
//     // @Override
//     // public void initNode() {
//     //     Editor textArea = super.getContent();
//     //     // 200k
//     //     textArea.setStyleBound(RichDataType.HEX, 200 * 1024 * 1024);
//     //     // 500k
//     //     textArea.setStyleBound(RichDataType.JSON, 500 * 1024 * 1024);
//     //     // 100k
//     //     textArea.setStyleBound(RichDataType.BINARY, 100 * 1024 * 1024);
//     //     super.initNode();
//     // }
// //
// //     @Override
// //     protected Font initFont() {
// // //        // 禁用字体管理
// // //        super.disableFont();
// //         // 初始化字体
// //         ZKSetting setting = ZKSettingStore.SETTING;
// // //        this.setFontSize(setting.getEditorFontSize());
// // //        this.setFontFamily(setting.getEditorFontFamily());
// // //        this.setFontWeight2(setting.getEditorFontWeight());
// //         return FontManager.toFont(setting.editorFontConfig());
// //     }
//
//     @Override
//     protected Font getEditorFont() {
//         if (super.getEditorFont() == null) {
//             ZKSetting setting = ZKSettingStore.SETTING;
//             Font font = FontManager.toFont(setting.editorFontConfig());
//             super.setEditorFont(font);
//         }
//         return super.getEditorFont();
//     }
//
//     //@Override
//     //public void changeFont(Font font) {
//     //    ZKSetting setting = ZKSettingStore.SETTING;
//     //    Font font1 = FontManager.toFont(setting.editorFontConfig());
//     //    super.changeFont(font1);
//     //}
// }
