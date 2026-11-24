package cn.oyzh.easyshell.dameng;

import cn.oyzh.easyshell.dameng.table.DamengTable;
import cn.oyzh.easyshell.query.dameng.ShellDamengQueryPromptItem;
import cn.oyzh.easyshell.query.dameng.ShellDamengQueryToken;
import cn.oyzh.easyshell.query.dameng.ShellDamengQueryTokenAnalyzer;
import cn.oyzh.easyshell.query.dameng.ShellDamengQueryUtil;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ShellDamengQueryCompletionTest {

    private final ShellDamengQueryTokenAnalyzer analyzer = ShellDamengQueryTokenAnalyzer.INSTANCE;

    @Test
    public void parsesUppercaseIdentifierAndKeywords() {
        ShellDamengQueryToken token = this.analyzer.currentToken("SEL", 3);

        assertNotNull(token);
        assertEquals("SEL", token.getContent());
        assertTrue(token.isPossibilityKeyword());

        List<ShellDamengQueryPromptItem> prompts = this.analyzer.initPrompts(token, 0.5f);
        assertFalse(prompts.isEmpty());
        assertEquals("SELECT", prompts.getFirst().getContent());
    }

    @Test
    public void suggestsAfterWhitespaceWithoutTypedIdentifier() {
        ShellDamengQueryToken token = this.analyzer.currentToken("SELECT * FROM ", 14);

        assertNotNull(token);
        assertEquals("", token.getContent());
        assertEquals("FROM", token.getContext());
        assertTrue(token.isPossibilityTable());

        List<ShellDamengQueryPromptItem> prompts = this.analyzer.initPrompts(token, 0.5f);
        assertFalse(prompts.isEmpty());
    }

    @Test
    public void parsesQualifierBeforeDot() {
        ShellDamengQueryToken token = this.analyzer.currentToken("SELECT * FROM DMHR.", 19);

        assertNotNull(token);
        assertEquals("", token.getContent());
        assertEquals("DMHR", token.getQualifier());
        assertTrue(token.isPossibilityTable());
    }

    @Test
    public void parsesColumnContext() {
        ShellDamengQueryToken token = this.analyzer.currentToken("SELECT * FROM EMP WHERE ", 24);

        assertNotNull(token);
        assertEquals("WHERE", token.getContext());
    }

    @Test
    public void parsesQuotedQualifier() {
        ShellDamengQueryToken token = this.analyzer.currentToken("SELECT * FROM \"DMHR\".", 21);

        assertNotNull(token);
        assertEquals("DMHR", token.getQualifier());
    }

    @Test
    public void excludesShorterAndUnrelatedCandidates() {
        List<DamengTable> added = new ArrayList<>();
        try {
            added.add(this.table("C"));
            added.add(this.table("a2"));
            added.add(this.table("CD_TABLE"));
            ShellDamengQueryUtil.getTables().addAll(added);

            ShellDamengQueryToken token = this.analyzer.currentToken("SELECT * FROM cd", 16);
            List<ShellDamengQueryPromptItem> prompts = this.analyzer.initPrompts(token, 0.5f);
            List<String> contents = prompts.stream().map(ShellDamengQueryPromptItem::getContent).toList();

            assertTrue(contents.contains("CD_TABLE"));
            assertFalse(contents.contains("C"));
            assertFalse(contents.contains("a2"));
        } finally {
            ShellDamengQueryUtil.getTables().removeAll(added);
        }
    }

    @Test
    public void acceptsPartialSequentialMatches() {
        List<DamengTable> added = new ArrayList<>();
        try {
            added.add(this.table("CUSTOMER_ID"));
            added.add(this.table("a2"));
            ShellDamengQueryUtil.getTables().addAll(added);

            ShellDamengQueryToken token = this.analyzer.currentToken("SELECT * FROM cusid", 19);
            List<ShellDamengQueryPromptItem> prompts = this.analyzer.initPrompts(token, 0.5f);
            List<String> contents = prompts.stream().map(ShellDamengQueryPromptItem::getContent).toList();

            assertTrue(contents.contains("CUSTOMER_ID"));
            assertFalse(contents.contains("a2"));
        } finally {
            ShellDamengQueryUtil.getTables().removeAll(added);
        }
    }

    private DamengTable table(String name) {
        DamengTable table = new DamengTable();
        table.setSchema("TEST");
        table.setName(name);
        return table;
    }
}
