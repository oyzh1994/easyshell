package cn.oyzh.easyshell.fx.sqlite;

import cn.oyzh.fx.gui.text.field.ChooseTextField;
import cn.oyzh.fx.plus.chooser.FileChooserHelper;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nResourceBundle;

import java.io.File;

/**
 * SQLite 数据库文件路径选择框。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class SQLiteDatabaseFileField extends ChooseTextField {

    @Override
    public void initNode() {
        super.initNode();
        this.setEditable(true);
        this.setAction(this::selectDatabaseFile);
    }

    /**
     * 选择数据库文件。SQLite 文件可以使用任意扩展名或不使用扩展名。
     */
    public void selectDatabaseFile() {
        File file = FileChooserHelper.choose(I18nResourceBundle.i18nString("shell.sqlite.openFileTitle"), this.getWindow());
        if (file != null) {
            this.setText(file.getAbsolutePath());
            this.setTipText(file.getAbsolutePath());
        }
    }
}
