package cn.oyzh.easyshell.fx.h2;

import cn.oyzh.fx.gui.text.field.ChooseTextField;
import cn.oyzh.fx.plus.chooser.FileChooserHelper;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nResourceBundle;

import java.io.File;

/**
 * H2 数据库文件路径选择框。
 *
 * @author oyzh
 * @since 2026-11-28
 */
public class H2DatabaseFileField extends ChooseTextField {

    @Override
    public void initNode() {
        super.initNode();
        this.setEditable(true);
        this.setAction(this::selectDatabaseFile);
    }

    /**
     * 选择 H2 MVStore 主文件。实际文件名必须以 {@code .mv.db} 结尾。
     */
    public void selectDatabaseFile() {
        File file = FileChooserHelper.choose(I18nResourceBundle.i18nString("shell.h2.openFileTitle"), this.getWindow());
        if (file != null) {
            this.setText(file.getAbsolutePath());
            this.setTipText(file.getAbsolutePath());
        }
    }
}
