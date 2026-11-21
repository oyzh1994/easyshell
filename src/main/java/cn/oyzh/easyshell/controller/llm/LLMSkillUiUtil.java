package cn.oyzh.easyshell.controller.llm;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.llm.skill.LLMSkillArchiveUtil;
import cn.oyzh.easyshell.store.llm.LLMSkillStore;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.plus.chooser.FileChooserHelper;
import cn.oyzh.fx.plus.chooser.FileExtensionFilter;
import cn.oyzh.fx.plus.chooser.FXDirChooser;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.i18n.I18nResourceBundle;
import javafx.stage.Window;

import java.io.File;
import java.util.Locale;

/**
 * Skill 编辑界面工具。
 *
 * @author oyzh
 * @since 2026-11-21
 */
final class LLMSkillUiUtil {

    private LLMSkillUiUtil() {
    }

    static File chooseFile(Window owner) {
        FileExtensionFilter filter = new FileExtensionFilter(
                I18nResourceBundle.i18nString("shell.llm.skill.fileFilter"),
                "*.zip", "*.md");
        return FileChooserHelper.choose(
                I18nResourceBundle.i18nString("shell.llm.skill.selectFile"), filter, owner);
    }

    static File chooseFolder(Window owner) {
        FXDirChooser chooser = new FXDirChooser();
        chooser.setTitle(I18nResourceBundle.i18nString("shell.llm.skill.selectFolder"));
        return chooser.showDialog(owner);
    }

    static boolean validateSource(File source, Window owner) {
        if (source == null || !source.exists()) {
            MessageBox.warn(I18nResourceBundle.i18nString("shell.llm.skill.fileRequired"), owner);
            return false;
        }
        if (source.isFile()) {
            String fileName = source.getName().toLowerCase(Locale.ROOT);
            if (!fileName.endsWith(".zip") && !fileName.endsWith(".md")) {
                MessageBox.warn(I18nResourceBundle.i18nString("shell.llm.skill.fileTypeInvalid"), owner);
                return false;
            }
            if (source.length() > LLMSkillArchiveUtil.MAX_FILE_BYTES) {
                MessageBox.warn(I18nResourceBundle.i18nString("shell.llm.skill.fileTooLarge"), owner);
                return false;
            }
        }
        return true;
    }

    static boolean validateName(String name, String excludeUid, LLMSkillStore store, Window owner) {
        if (StringUtil.isBlank(name)) {
            MessageBox.warn(I18nResourceBundle.i18nString("shell.llm.skill.nameRequired"), owner);
            return false;
        }
        if (store.nameExists(name, excludeUid)) {
            MessageBox.warn(I18nResourceBundle.i18nString("shell.llm.skill.nameDuplicate"), owner);
            return false;
        }
        return true;
    }

    static String suggestedName(File source) {
        if (source == null) {
            return "";
        }
        String name;
        if (source.isDirectory()) {
            name = source.getName();
        } else if (source.getName().toLowerCase(Locale.ROOT).endsWith(".md")) {
            name = source.getName().replaceFirst("(?i)\\.md$", "");
        } else {
            name = source.getName().replaceFirst("(?i)\\.zip$", "");
        }
        return name.length() > 64 ? name.substring(0, 64) : name;
    }

    static void showSource(ClearableTextField field, File source, boolean keepCurrent) {
        if (source != null) {
            field.setText(source.getAbsolutePath());
        } else if (!keepCurrent) {
            field.clear();
        }
    }

}
