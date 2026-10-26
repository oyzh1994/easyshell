package cn.oyzh.easyshell.ssh.process;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Unix 进程列解析测试。
 */
public class ShellProcessParserTest {

    private static final String HEADER =
            "USER PID %CPU %MEM VSZ RSS TTY STAT START TIME COMMAND";

    @Test
    public void parsesLinuxTwoPartStartTimeAndCommandArguments() {
        String output = HEADER + "\n"
                + "root 1 0.0 0.3 23680 14256 ? Ss Oct 23 00:00:54 /sbin/init splash\n";

        List<ShellProcessInfo> processes = ShellProcessParser.psForUnix(output);

        assertEquals(1, processes.size());
        ShellProcessInfo process = processes.get(0);
        assertEquals("root", process.getUser());
        assertEquals(1, process.getPid());
        assertEquals("Ss", process.getStat());
        assertEquals("Oct 23", process.getStart());
        assertEquals("00:00:54", process.getTime());
        assertEquals("/sbin/init splash", process.getCommand());
        assertEquals(13.92, process.getRss(), 0.01);
    }

    @Test
    public void parsesBsdSinglePartStartTime() {
        String output = HEADER + "\n"
                + "root 1 0.0 0.1 12656 1052 - ILs Sat16 0:00.30 /sbin/init\n";

        List<ShellProcessInfo> processes = ShellProcessParser.psForUnix(output);

        assertEquals(1, processes.size());
        ShellProcessInfo process = processes.get(0);
        assertEquals("Sat16", process.getStart());
        assertEquals("0:00.30", process.getTime());
        assertEquals("/sbin/init", process.getCommand());
        assertEquals(1.03, process.getRss(), 0.01);
    }
}
