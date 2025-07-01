package cn.oyzh.easyshell.ssh2;

import java.util.HashMap;
import java.util.Map;

/**
 * @author oyzh
 * @since 2025-07-01
 */
public class HostManager {

    private static Map<String, Host> map = new HashMap<>();

    public static Host getHost(String s) {
        return map.get(s);
    }

    public static void putHost(String s, Host host) {
        map.put(s, host);
    }
}
