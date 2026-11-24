package cn.oyzh.easyshell.util;

import cn.oyzh.easyshell.domain.ShellQuery;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ShellQueryUtilTest {

    @Test
    public void clonesQueryWithNewNameAndIdentity() {
        ShellQuery source = new ShellQuery();
        source.setUid("source-id");
        source.setIid("connect-id");
        source.setName("demo");
        source.setContent("select * from demo");
        source.setDbName("test");
        source.setDbIndex(3);

        ShellQuery clone = ShellQueryUtil.cloneQuery(source);

        assertEquals("connect-id", clone.getIid());
        assertEquals("select * from demo", clone.getContent());
        assertEquals("test", clone.getDbName());
        assertEquals(3, clone.getDbIndex());
        assertTrue(clone.getName(), clone.getName().startsWith("demo_clone_"));
        assertNull(clone.getUid());
    }
}
