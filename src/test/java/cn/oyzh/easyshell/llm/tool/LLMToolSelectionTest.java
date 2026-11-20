package cn.oyzh.easyshell.llm.tool;

import cn.oyzh.easyshell.llm.LLMExecutionMode;
import cn.oyzh.easyshell.domain.ShellSetting;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class LLMToolSelectionTest {

    @Test
    public void sessionSelectionContainsSessionAndLocalExecutionOnly() {
        FakeTools session = new FakeTools(LLMToolScope.SESSION);
        FakeTools local = new FakeTools(LLMToolScope.LOCAL_EXECUTION);

        LLMToolSelection selection = new LLMToolSelection(
                session, null, local, LLMExecutionMode.APPROVE);

        assertTrue(selection.hasSession());
        assertEquals(2, selection.tools().size());
        assertSame(session, selection.tools().get(0));
        assertSame(local, selection.tools().get(1));
        assertEquals("SESSION\n\nLOCAL_EXECUTION", selection.systemMessage());
    }

    @Test
    public void localTabUsesTheSameSessionPolicy() {
        FakeTools localTab = new FakeTools(LLMToolScope.SESSION);
        FakeTools localExecution = new FakeTools(LLMToolScope.LOCAL_EXECUTION);

        LLMToolSelection selection = new LLMToolSelection(
                localTab, null, localExecution, LLMExecutionMode.APPROVE);

        assertTrue(selection.hasSession());
        assertEquals(List.of(localTab, localExecution), selection.tools());
    }

    @Test
    public void noSessionSelectionContainsInternalAndLocalExecutionOnly() {
        FakeTools internal = new FakeTools(LLMToolScope.INTERNAL);
        FakeTools local = new FakeTools(LLMToolScope.LOCAL_EXECUTION);

        LLMToolSelection selection = new LLMToolSelection(
                null, internal, local, LLMExecutionMode.APPROVE);

        assertFalse(selection.hasSession());
        assertEquals(2, selection.tools().size());
        assertSame(internal, selection.tools().get(0));
        assertSame(local, selection.tools().get(1));
        assertEquals("INTERNAL\n\nLOCAL_EXECUTION", selection.systemMessage());
    }

    @Test
    public void sessionOnlyModeDoesNotExposeLocalOrInternalTools() {
        FakeTools session = new FakeTools(LLMToolScope.SESSION);
        FakeTools local = new FakeTools(LLMToolScope.LOCAL_EXECUTION);

        LLMToolSelection selection = new LLMToolSelection(
                session, null, local, LLMExecutionMode.APPROVE,
                LLMToolAccessMode.SESSION_ONLY);

        assertEquals(List.of(session), selection.tools());
        assertEquals(LLMToolAccessMode.SESSION_ONLY, selection.accessMode());
        assertEquals("SESSION", selection.systemMessage());
    }

    @Test
    public void completeModeCombinesCurrentSessionLocalAndInternalTools() {
        FakeTools session = new FakeTools(LLMToolScope.SESSION);
        FakeTools internal = new FakeTools(LLMToolScope.INTERNAL);
        FakeTools local = new FakeTools(LLMToolScope.LOCAL_EXECUTION);

        LLMToolSelection selection = new LLMToolSelection(
                session, internal, local, LLMExecutionMode.APPROVE,
                LLMToolAccessMode.COMPLETE);

        assertEquals(List.of(session, local, internal), selection.tools());
        assertTrue(selection.systemMessage().contains("SESSION"));
        assertTrue(selection.systemMessage().contains("LOCAL_EXECUTION"));
        assertTrue(selection.systemMessage().contains("INTERNAL"));
    }

    @Test
    public void completeModeWithoutSessionUsesInternalAndLocalTools() {
        FakeTools internal = new FakeTools(LLMToolScope.INTERNAL);
        FakeTools local = new FakeTools(LLMToolScope.LOCAL_EXECUTION);

        LLMToolSelection selection = new LLMToolSelection(
                null, internal, local, LLMExecutionMode.APPROVE,
                LLMToolAccessMode.COMPLETE);

        assertEquals(List.of(internal, local), selection.tools());
        assertFalse(selection.hasSession());
    }

    @Test
    public void sessionOnlyModeWithoutSessionUsesInternalToolsOnly() {
        FakeTools internal = new FakeTools(LLMToolScope.INTERNAL);
        FakeTools local = new FakeTools(LLMToolScope.LOCAL_EXECUTION);

        LLMToolSelection selection = new LLMToolSelection(
                null, internal, local, LLMExecutionMode.APPROVE,
                LLMToolAccessMode.SESSION_ONLY);

        assertEquals(List.of(internal), selection.tools());
        assertFalse(selection.hasSession());
    }

    @Test
    public void rejectsInternalAndSessionTogether() {
        try {
            new LLMToolSelection(
                    new FakeTools(LLMToolScope.SESSION),
                    new FakeTools(LLMToolScope.INTERNAL),
                    new FakeTools(LLMToolScope.LOCAL_EXECUTION),
                    LLMExecutionMode.APPROVE);
            fail("SESSION and INTERNAL must not be exposed together");
        } catch (IllegalArgumentException ex) {
            assertTrue(ex.getMessage().contains("SESSION"));
        }
    }

    @Test
    public void toolAccessModeIsPersistedAndInvalidValuesUseDefault() {
        ShellSetting setting = new ShellSetting();
        assertEquals(LLMToolAccessMode.SESSION_WITH_LOCAL.ordinal(),
                setting.getLlmToolAccessMode());

        setting.setLlmToolAccessMode(LLMToolAccessMode.COMPLETE.ordinal());
        ShellSetting copy = new ShellSetting();
        copy.copy(setting);
        assertEquals(LLMToolAccessMode.COMPLETE.ordinal(), copy.getLlmToolAccessMode());
        assertEquals(LLMToolAccessMode.SESSION_WITH_LOCAL,
                LLMToolAccessMode.fromOrdinal(999));
    }

    private static final class FakeTools implements LLMTools {

        private final LLMToolScope scope;

        private FakeTools(LLMToolScope scope) {
            this.scope = scope;
        }

        @Override
        public LLMToolScope scope() {
            return this.scope;
        }

        @Override
        public String systemMessage(LLMExecutionMode executionMode) {
            return this.scope.name();
        }
    }
}
