package cn.oyzh.easyshell.test.terminal.zk_v2;

import cn.oyzh.easyshell.terminal.zk2.ZKCommandDescriptor;
import cn.oyzh.easyshell.terminal.zk2.ZKCommandExecutor;
import cn.oyzh.easyshell.terminal.zk2.ZKCommandRegistry;
import org.junit.Test;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ZKCommandRegistryTest {

    private static final Set<String> LEGACY_COMMANDS = Set.of(
            "help", "clear", "connect", "version",
            "addauth", "close", "create", "delquota", "delete", "deleteall",
            "getAcl", "getAllChildrenNumber", "config", "getEphemerals", "get",
            "listquota", "ls2", "ls", "reconfig", "removewatches", "rmr",
            "setAcl", "setquota", "set", "stat", "sync", "whoami",
            "conf", "cons", "crst", "dirs", "dump", "envi", "kill", "mntr",
            "reqs", "ruok", "srst", "srvr", "wchc", "wchp", "wchs"
    );

    @Test
    public void preservesLegacyCommandSurface() {
        Set<String> actual = ZKCommandRegistry.commandNames().stream()
                .map(command -> command.toLowerCase(Locale.ROOT))
                .collect(java.util.stream.Collectors.toSet());
        actual.add("help");
        actual.add("clear");
        for (String command : LEGACY_COMMANDS) {
            assertTrue("Missing legacy command: " + command,
                    actual.contains(command.toLowerCase(Locale.ROOT)));
        }
    }

    @Test
    public void discoversCliCommandsAndSupportsVersionWithoutConnection() throws Exception {
        assertTrue(ZKCommandRegistry.contains("create"));
        ZKCommandDescriptor descriptor = ZKCommandRegistry.find("version").orElseThrow();
        String output = new ZKCommandExecutor().execute(descriptor, List.of("version"), null, "", 0);
        assertTrue(output.contains("ZooKeeper CLI version:"));
    }

    @Test
    public void discoversDeprecatedCliCommands() {
        assertTrue(ZKCommandRegistry.find("ls").isPresent());
        assertTrue(ZKCommandRegistry.find("ls2").isPresent());
        assertTrue(ZKCommandRegistry.find("deleteall").isPresent());
        assertTrue(ZKCommandRegistry.find("rmr").isPresent());
    }

    @Test
    public void resolvesStatCommandByArgumentShape() {
        ZKCommandDescriptor cliCommand = ZKCommandRegistry.find(List.of("stat", "/root")).orElseThrow();
        assertEquals("stat", cliCommand.name());
        assertEquals("path", cliCommand.usage());
        assertEquals(ZKCommandDescriptor.Type.CLI, cliCommand.type());

        ZKCommandDescriptor fourLetterWord = ZKCommandRegistry.find(List.of("stat")).orElseThrow();
        assertEquals("stat", fourLetterWord.name());
        assertEquals("", fourLetterWord.usage());
        assertEquals(ZKCommandDescriptor.Type.FOUR_LETTER_WORD, fourLetterWord.type());

        assertEquals(2, ZKCommandRegistry.findAll("stat").size());
        assertFalse(ZKCommandRegistry.contains("stat4"));
    }
}
