package cn.oyzh.easyshell.store.llm;

import cn.oyzh.easyshell.llm.LLMTokenUsageStats;
import cn.oyzh.easyshell.llm.capability.LLMModelCapability;
import cn.oyzh.easyshell.llm.capability.LLMModelCapabilityStore;
import cn.oyzh.easyshell.llm.session.LLMSession;
import cn.oyzh.store.jdbc.JdbcConst;
import cn.oyzh.store.jdbc.JdbcDialect;
import cn.oyzh.store.jdbc.JdbcManager;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class LLMSessionStoreTest {

    private static Path workDirectory;

    @BeforeClass
    public static void beforeClass() throws Exception {
        workDirectory = Files.createTempDirectory("easyshell-llm-session-");
        JdbcManager.destroy();
        JdbcConst.dbFile(workDirectory.resolve("db").toString());
        JdbcConst.dbDialect(JdbcDialect.SQLITE);
        JdbcManager.dialect = JdbcDialect.SQLITE;
    }

    @AfterClass
    public static void afterClass() throws Exception {
        JdbcManager.destroy();
        if (workDirectory != null) {
            try (Stream<Path> paths = Files.walk(workDirectory)) {
                paths.sorted(Comparator.reverseOrder()).forEach(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (Exception ignored) {
                    }
                });
            }
        }
    }

    @Test
    public void persistsAndUpdatesTokenUsagePerSession() {
        LLMSessionStore store = new LLMSessionStore();
        LLMSession session = new LLMSession();
        session.setUid("llm-session-usage");
        session.setTitle("usage");
        session.setMessages("[]");
        session.setTokenUsageStats(new LLMTokenUsageStats(
                1_200, 300, 1_500,
                2_400, 600, 3_000,
                true, 2_000));

        assertTrue(store.save(session));
        LLMSession stored = store.selectOne(session.getUid());
        assertNotNull(stored);
        assertEquals(1_500, stored.getTokenUsageStats().lastTotalTokens());
        assertEquals(3_000, stored.getTokenUsageStats().sessionTotalTokens());
        assertEquals(2_000, stored.getTokenUsageStats().lastDurationMillis());

        stored.setTokenUsageStats(new LLMTokenUsageStats(
                1_800, 400, 2_200,
                4_200, 1_000, 5_200,
                true, 2_500));
        assertTrue(store.save(stored));

        LLMSession updated = store.selectOne(session.getUid());
        assertNotNull(updated);
        assertEquals(2_200, updated.getTokenUsageStats().lastTotalTokens());
        assertEquals(5_200, updated.getTokenUsageStats().sessionTotalTokens());
        assertEquals(2_500, updated.getTokenUsageStats().lastDurationMillis());
    }

    @Test
    public void persistsPerModelCapabilityOverrides() {
        LLMModelCapabilityStore store = new LLMModelCapabilityStore();
        LLMModelCapability capability = new LLMModelCapability();
        capability.setProviderUid("provider-1");
        capability.setModel("custom-model");
        capability.setContextWindowTokens(128 * 1024);
        capability.setTools(false);
        capability.setImage(true);
        capability.setThinking(true);

        assertTrue(store.save(capability));
        LLMModelCapability stored = store.find("provider-1", "custom-model");
        assertNotNull(stored);
        assertEquals(128 * 1024, stored.getContextWindowTokens());
        assertTrue(stored.isImage());
        assertTrue(stored.isThinking());
        org.junit.Assert.assertFalse(stored.isTools());

        stored.setContextWindowTokens(1024 * 1024);
        assertTrue(store.save(stored));
        assertEquals(1024 * 1024, store.find(
                "provider-1", "custom-model").getContextWindowTokens());
    }
}
