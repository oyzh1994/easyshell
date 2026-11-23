package cn.oyzh.easyshell.ssh.process;

import org.junit.Test;

import java.util.List;
import java.util.Map;

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

    @Test
    public void parsesLinuxSsPorts() {
        String output = """
                Netid State  Recv-Q Send-Q Local Address:Port Peer Address:Port Process
                tcp   LISTEN 0      4096   0.0.0.0:22       0.0.0.0:*         users:(("sshd",pid=1188,fd=3))
                tcp   LISTEN 0      511    *:80             *:*               users:(("nginx",pid=1188,fd=6),("nginx",pid=1189,fd=6))
                udp   UNCONN 0      0      127.0.0.1:53     0.0.0.0:*         users:(("dnsmasq",pid=2000,fd=4))
                """;

        Map<Integer, List<Integer>> ports = ShellProcessParser.parseLinuxPorts(output);

        assertEquals(List.of(22, 80), ports.get(1188));
        assertEquals(List.of(80), ports.get(1189));
        assertEquals(List.of(53), ports.get(2000));
        assertEquals(3, ports.size());
    }

    @Test
    public void parsesUnixNetstatPortsOnlyForListeningTcp() {
        String output = """
                tcp        0      0 0.0.0.0:22              0.0.0.0:*               LISTEN      100/sshd
                udp        0      0 0.0.0.0:68              0.0.0.0:*                           101/dhclient
                tcp        0      0 10.0.0.1:22             10.0.0.2:54321          ESTABLISHED 100/sshd
                """;

        Map<Integer, List<Integer>> ports = ShellProcessParser.parseLinuxPorts(output);

        assertEquals(List.of(22), ports.get(100));
        assertEquals(List.of(68), ports.get(101));
        assertEquals(2, ports.size());
    }

    @Test
    public void parsesWindowsNetstatPorts() {
        String output = """
                  TCP    0.0.0.0:135            0.0.0.0:0              LISTENING       1234
                  TCP    [::]:445               [::]:0                 LISTENING       4
                  UDP    0.0.0.0:5353           *:*                                    5678
                  TCP    192.168.1.5:50000      1.1.1.1:443            ESTABLISHED     9999
                """;

        Map<Integer, List<Integer>> ports = ShellProcessParser.parseNetstatPorts(output);

        assertEquals(List.of(135), ports.get(1234));
        assertEquals(List.of(445), ports.get(4));
        assertEquals(List.of(5353), ports.get(5678));
        assertEquals(3, ports.size());
    }

    @Test
    public void parsesWindowsPowerShellPorts() {
        String output = """
                T,1234,135
                T,4,445
                U,5678,5353
                """;

        Map<Integer, List<Integer>> ports = ShellProcessParser.parseWindowsPorts(output);

        assertEquals(List.of(135), ports.get(1234));
        assertEquals(List.of(445), ports.get(4));
        assertEquals(List.of(5353), ports.get(5678));
        assertEquals(3, ports.size());
    }

    @Test
    public void parsesMacosLsofPorts() {
        String output = """
                p123
                n*:22
                n[::1]:631
                p456
                n127.0.0.1:5432
                """;

        Map<Integer, List<Integer>> ports = ShellProcessParser.parseLsofPorts(output);

        assertEquals(List.of(22, 631), ports.get(123));
        assertEquals(List.of(5432), ports.get(456));
        assertEquals(2, ports.size());
    }

    @Test
    public void parsesBsdSockstatPorts() {
        String output = """
                USER     COMMAND    PID   FD PROTO  LOCAL ADDRESS         FOREIGN ADDRESS
                root     sshd       1234  4  tcp4   *:22                  *:*
                root     named      2345  20 udp4   127.0.0.1:53          *:*
                """;

        Map<Integer, List<Integer>> ports = ShellProcessParser.parseSockstatPorts(output);

        assertEquals(List.of(22), ports.get(1234));
        assertEquals(List.of(53), ports.get(2345));
        assertEquals(2, ports.size());
    }

    @Test
    public void mergesPortsIntoProcesses() {
        ShellProcessInfo process = new ShellProcessInfo();
        process.setPid(1188);

        ShellProcessParser.parsePorts(List.of(process), Map.of(1188, List.of(22, 80)));

        assertEquals("22,80", process.getPortsData());
        assertEquals("-", new ShellProcessInfo().getPortsData());
    }

    @Test
    public void parsesMacosNettopCsv() {
        String output = """
                time,,interface,state,bytes_in,bytes_out,rx_dupe,rx_ooo,re-tx,rtt_avg,rcvsize,tx_win,tc_class,tc_mgt,cc_algo,P,C,R,W,arch,
                00:00:01,launchd.1,,,100,200,0,0,0,,,,,,,,,,,,
                00:00:02,Google H.19261,,,1000,2000,0,0,0,,,,,,,,,,,,
                """;

        Map<String, double[]> speeds = ShellProcessParser.parseNetworkSpeedOutputMacos(output);

        assertEquals(200, speeds.get("launchd.1")[0], 0.01);
        assertEquals(100, speeds.get("launchd.1")[1], 0.01);
        assertEquals(2000, speeds.get("Google H.19261")[0], 0.01);
        assertEquals(1000, speeds.get("Google H.19261")[1], 0.01);
    }

    @Test
    public void mergesMacosNettopPidFromLastSegment() {
        ShellProcessInfo process = new ShellProcessInfo();
        process.setPid(76554);

        ShellProcessParser.parseNetworkSpeed_macos(List.of(process),
                Map.of("com.docker.back.76554", new double[]{300, 100}));

        assertEquals(300, process.getNetworkSend(), 0.01);
        assertEquals(100, process.getNetworkRecv(), 0.01);
    }
}
