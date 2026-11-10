package cn.oyzh.easyshell.domain;

import org.junit.Test;

import java.util.EnumSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ShellConnectTagTest {

    @Test
    public void parsesAndSerializesFixedTagOrder() {
        EnumSet<ShellConnectTag> tags = ShellConnectTag.parse("blue,red,unknown,red");

        assertEquals(EnumSet.of(ShellConnectTag.RED, ShellConnectTag.BLUE), tags);
        assertEquals("red,blue", ShellConnectTag.serialize(tags));
        assertEquals(2, ShellConnectTag.colors("red,blue").size());
        assertNull(ShellConnectTag.serialize(EnumSet.noneOf(ShellConnectTag.class)));
    }

    @Test
    public void copiesTagsWithConnectionsAndGroups() {
        ShellConnect connect = new ShellConnect();
        connect.setTags("orange,purple");
        ShellConnect connectCopy = new ShellConnect();
        connectCopy.copy(connect);

        ShellGroup group = new ShellGroup();
        group.setTags("green");
        ShellGroup groupCopy = new ShellGroup();
        groupCopy.copy(group);

        assertEquals("orange,purple", connectCopy.getTags());
        assertEquals("green", groupCopy.getTags());
        assertTrue(ShellConnectTag.colors(connectCopy.getTags()).size() == 2);
    }
}
