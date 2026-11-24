package cn.oyzh.easyshell.util.llm;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.llm.prompt.LLMPromptUtil;
import cn.oyzh.easyshell.store.llm.LLMPromptStore;
import cn.oyzh.fx.editor.incubator.control.MarkdownEditor;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.i18n.I18nResourceBundle;
import javafx.stage.Window;

/**
 * 提示词编辑界面工具。
 *
 * @author oyzh
 * @since 2026-11-21
 */
public final class ShellLLMPromptControllerUtil {

    private ShellLLMPromptControllerUtil() {
    }

    public static void prepareEditor(MarkdownEditor editor) {
        editor.setWrapText(true);
        editor.setHighlightCurrentParagraph(false);
        editor.setAutoPairEnabled(false);
    }

    public static boolean validate(ClearableTextField name,
                            MarkdownEditor content,
                            LLMPromptStore store,
                            String excludeUid,
                            Window owner) {
        if (!name.validate()) {
            return false;
        }
        String normalizedName = LLMPromptUtil.normalizeName(name.getTextTrim());
        if (LLMPromptUtil.exceedsNameLimit(normalizedName)) {
            MessageBox.warn(I18nResourceBundle.i18nString("shell.llm.prompt.nameTooLong"), owner);
            return false;
        }
        if (store.nameExists(normalizedName, excludeUid)) {
            MessageBox.warn(I18nResourceBundle.i18nString("shell.llm.prompt.nameDuplicate"), owner);
            return false;
        }
        String normalizedContent = LLMPromptUtil.normalizeContent(content.getTextTrim());
        if (StringUtil.isBlank(normalizedContent)) {
            MessageBox.warn(I18nResourceBundle.i18nString("shell.llm.prompt.contentRequired"), owner);
            return false;
        }
        if (LLMPromptUtil.exceedsContentLimit(normalizedContent)) {
            MessageBox.warn(I18nResourceBundle.i18nString("shell.llm.prompt.contentTooLong"), owner);
            return false;
        }
        return true;
    }
}
