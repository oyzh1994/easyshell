package cn.oyzh.easyshell.util;

import cn.oyzh.easyshell.domain.ShellSetting;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

class ShellVersionCheckerTest {

    @Test
    void acceptsOnlyStableThreePartVersions() {
        assertTrue(ShellVersionChecker.isStableVersion("v1.2.4"));
        assertTrue(ShellVersionChecker.isStableVersion("1.2.4"));
        assertTrue(ShellVersionChecker.isStableVersion("V10.0.12"));
        assertFalse(ShellVersionChecker.isStableVersion("v1.2.4-beta"));
        assertFalse(ShellVersionChecker.isStableVersion("v1.2.4-rc1"));
        assertFalse(ShellVersionChecker.isStableVersion("v1.2.4-preview"));
        assertFalse(ShellVersionChecker.isStableVersion("v1.2"));
        assertFalse(ShellVersionChecker.isStableVersion("v1.2.4.1"));
        assertFalse(ShellVersionChecker.isStableVersion(null));
    }

    @Test
    void comparesVersionsNumerically() {
        assertEquals(0, ShellVersionChecker.compareVersions("v1.2.4", "1.2.4"));
        assertTrue(ShellVersionChecker.compareVersions("v1.2.5", "1.2.4") > 0);
        assertTrue(ShellVersionChecker.compareVersions("v1.10.0", "1.9.9") > 0);
        assertTrue(ShellVersionChecker.compareVersions("v1.2.3", "1.2.4") < 0);
        assertEquals(0, ShellVersionChecker.compareVersions("bad", "1.2.4"));
    }

    @Test
    void loadsReleaseUrlFromProjectProperties() {
        assertEquals("https://github.com/oyzh1994/easyshell/releases",
                cn.oyzh.common.dto.Project.load().getReleaseUrl());
    }

    @Test
    void appliesReminderFrequency() {
        ShellSetting setting = new ShellSetting();
        long now = System.currentTimeMillis();
        assertEquals("off", setting.getVersionCheckReminder());
        assertFalse(ShellVersionChecker.shouldRemind(setting));

        setting.setVersionCheckReminder("");
        assertFalse(ShellVersionChecker.shouldRemind(setting));

        setting.setVersionCheckReminder("always");
        setting.setVersionCheckLastRemindTime(now);
        assertTrue(ShellVersionChecker.shouldRemind(setting));

        setting.setVersionCheckReminder("startup");
        assertFalse(ShellVersionChecker.shouldRemind(setting));

        setting.setVersionCheckReminder("off");
        setting.setVersionCheckLastRemindTime(null);
        assertFalse(ShellVersionChecker.shouldRemind(setting));

        setting.setVersionCheckReminder("day");
        setting.setVersionCheckLastRemindTime(null);
        assertTrue(ShellVersionChecker.shouldRemind(setting));
        setting.setVersionCheckLastRemindTime(now);
        assertFalse(ShellVersionChecker.shouldRemind(setting));
        setting.setVersionCheckLastRemindTime(now - TimeUnit.DAYS.toMillis(1) - 1000);
        assertTrue(ShellVersionChecker.shouldRemind(setting));

        setting.setVersionCheckReminder("week");
        setting.setVersionCheckLastRemindTime(now - TimeUnit.DAYS.toMillis(6));
        assertFalse(ShellVersionChecker.shouldRemind(setting));
        setting.setVersionCheckLastRemindTime(now - TimeUnit.DAYS.toMillis(7) - 1000);
        assertTrue(ShellVersionChecker.shouldRemind(setting));

        setting.setVersionCheckReminder("month");
        setting.setVersionCheckLastRemindTime(now - TimeUnit.DAYS.toMillis(29));
        assertFalse(ShellVersionChecker.shouldRemind(setting));
        setting.setVersionCheckLastRemindTime(now - TimeUnit.DAYS.toMillis(30) - 1000);
        assertTrue(ShellVersionChecker.shouldRemind(setting));
    }

    @Test
    void checksOnStartupOnlyWhenReminderEnabled() {
        ShellSetting setting = new ShellSetting();
        assertFalse(ShellVersionChecker.isReminderEnabled(setting));

        setting.setVersionCheckReminder("");
        assertFalse(ShellVersionChecker.isReminderEnabled(setting));

        setting.setVersionCheckReminder("off");
        assertFalse(ShellVersionChecker.isReminderEnabled(setting));

        setting.setVersionCheckReminder("unknown");
        assertFalse(ShellVersionChecker.isReminderEnabled(setting));

        setting.setVersionCheckReminder("startup");
        assertFalse(ShellVersionChecker.isReminderEnabled(setting));

        for (String reminder : new String[]{"always", "day", "week", "month"}) {
            setting.setVersionCheckReminder(reminder);
            assertTrue(ShellVersionChecker.isReminderEnabled(setting));
        }
    }

    @Test
    void derivesGitHubReleasesApiUrl() {
        assertEquals("https://api.github.com/repos/oyzh1994/easyshell/releases?per_page=100",
                ShellVersionChecker.releasesApiUrl("https://github.com/oyzh1994/easyshell/releases"));
        assertEquals("https://api.github.com/repos/oyzh1994/easyshell/releases?per_page=100",
                ShellVersionChecker.releasesApiUrl("https://github.com/oyzh1994/easyshell/releases/"));
        assertEquals("1.2.4", ShellVersionChecker.normalizeVersion("v1.2.4"));
    }
}
