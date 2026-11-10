package cn.oyzh.easyshell.domain;

import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * 主窗口背景图片设置测试
 *
 * @author oyzh
 * @since 2026-11-10
 */
public class ShellSettingWindowBackgroundTest {

    @Test
    public void defaultsToSeventyFivePercentOpacity() {
        ShellSetting setting = new ShellSetting();
        assertEquals(75, setting.getWindowBackgroundOpacity());
        assertFalse(setting.isEnableBackgroundImage());
    }

    @Test
    public void blankImageIsInvalid() {
        ShellSetting setting = new ShellSetting();
        assertTrue(setting.isWindowBackgroundImageInvalid());

        setting.setWindowBackgroundImage("   ");
        assertTrue(setting.isWindowBackgroundImageInvalid());
        assertEquals("   ", setting.getWindowBackgroundImageUrl());
    }

    @Test
    public void remoteImageIsAcceptedAsIs() {
        ShellSetting setting = new ShellSetting();
        setting.setWindowBackgroundImage("https://example.com/background.png");

        assertFalse(setting.isWindowBackgroundImageInvalid());
        assertEquals("https://example.com/background.png", setting.getWindowBackgroundImageUrl());
    }

    @Test
    public void localImageIsConvertedToFileUrl() throws Exception {
        File file = File.createTempFile("fx-shell-background", ".png");
        file.deleteOnExit();
        ShellSetting setting = new ShellSetting();
        setting.setWindowBackgroundImage(file.getAbsolutePath());

        assertFalse(setting.isWindowBackgroundImageInvalid());
        assertTrue(setting.getWindowBackgroundImageUrl().startsWith("file:"));
    }

    @Test
    public void missingLocalImageIsInvalid() {
        ShellSetting setting = new ShellSetting();
        setting.setWindowBackgroundImage("/path/to/not/exist/background.png");

        assertTrue(setting.isWindowBackgroundImageInvalid());
    }

    @Test
    public void copyKeepsWindowBackgroundSettings() {
        ShellSetting source = new ShellSetting();
        source.setWindowBackgroundImage("/tmp/background.png");
        source.setWindowBackgroundOpacity(42);
        source.setEnableBackgroundImage(true);

        ShellSetting copy = new ShellSetting();
        copy.copy(source);

        assertEquals("/tmp/background.png", copy.getWindowBackgroundImage());
        assertEquals(42, copy.getWindowBackgroundOpacity());
        assertTrue(copy.isEnableBackgroundImage());
    }
}
