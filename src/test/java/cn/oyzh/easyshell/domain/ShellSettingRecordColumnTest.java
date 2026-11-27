package cn.oyzh.easyshell.domain;

import cn.oyzh.easyshell.util.ShellI18nHelper;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * 记录表格列头设置测试。
 *
 * @since 2026-11-27
 */
public class ShellSettingRecordColumnTest {

    @Test
    public void testShowColumnTypeDefaultsToTrue() {
        ShellSetting setting = new ShellSetting();
        assertTrue(setting.isShowColumnType());
        assertTrue(setting.isShowColumnComment());

        setting.setShowColumnType(false);
        setting.setShowColumnComment(false);
        assertFalse(setting.isShowColumnType());
        assertFalse(setting.isShowColumnComment());
    }

    @Test
    public void testCopyKeepsRecordColumnSetting() {
        ShellSetting source = new ShellSetting();
        source.setShowColumnType(false);
        source.setShowColumnComment(false);

        ShellSetting target = new ShellSetting();
        target.copy(source);
        assertFalse(target.isShowColumnType());
        assertFalse(target.isShowColumnComment());
    }

    @Test
    public void testRecordColumnI18nExists() {
        String type = ShellI18nHelper.recordShowColumnType();
        String comment = ShellI18nHelper.recordShowColumnComment();
        assertNotNull(type);
        assertNotNull(comment);
        assertFalse(type.isEmpty());
        assertFalse(comment.isEmpty());
        assertFalse(type.equals("shell.record.showColumnType"));
        assertFalse(comment.equals("shell.record.showColumnComment"));
        assertEquals(type, ShellI18nHelper.recordShowColumnType());
    }
}
