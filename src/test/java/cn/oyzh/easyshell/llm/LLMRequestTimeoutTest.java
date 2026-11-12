package cn.oyzh.easyshell.llm;

import cn.oyzh.easyshell.domain.ShellSetting;
import cn.oyzh.store.jdbc.Column;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class LLMRequestTimeoutTest {

    @Test
    public void exposesRequestedOptions() {
        List<Integer> minutes = Arrays.stream(LLMRequestTimeout.values())
                .map(LLMRequestTimeout::getMinutes)
                .toList();
        assertEquals(List.of(10, 20, 30, 60, 0), minutes);
    }

    @Test
    public void defaultsToThirtyMinutes() {
        assertEquals(30, new ShellSetting().getLlmTimeoutMinutes());
        assertEquals(30, LLMRequestTimeout.fromMinutes(null).getMinutes());
        assertEquals(30, LLMRequestTimeout.fromMinutes(45).getMinutes());
    }

    @Test
    public void unlimitedTimeoutHasNoDuration() {
        ShellSetting setting = new ShellSetting();
        setting.setLlmTimeoutMinutes(0);

        assertEquals(0, setting.getLlmTimeoutMinutes());
        assertNull(LLMRequestTimeout.UNLIMITED.getDuration());
    }

    @Test
    public void timeoutSettingIsPersisted() throws Exception {
        Field field = ShellSetting.class.getDeclaredField("llmTimeoutMinutes");
        assertNotNull(field.getAnnotation(Column.class));
    }

    @Test
    public void copyKeepsTimeoutSetting() {
        ShellSetting source = new ShellSetting();
        source.setLlmTimeoutMinutes(60);

        ShellSetting copy = new ShellSetting();
        copy.copy(source);

        assertEquals(60, copy.getLlmTimeoutMinutes());
    }
}
