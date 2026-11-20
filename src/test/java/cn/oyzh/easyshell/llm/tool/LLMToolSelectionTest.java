package cn.oyzh.easyshell.llm.tool;

import cn.oyzh.easyshell.llm.LLMExecutionMode;
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
