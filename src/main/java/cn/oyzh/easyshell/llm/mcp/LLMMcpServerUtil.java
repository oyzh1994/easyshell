package cn.oyzh.easyshell.llm.mcp;

import cn.oyzh.easyshell.store.llm.LLMMcpServerStore;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * MCP Server 工具类。
 *
 * @author oyzh
 * @since 2026-11-20
 */
public final class LLMMcpServerUtil {

    private LLMMcpServerUtil() {
    }

    /**
     * 按名称导入 MCP Server，已存在时更新原记录。
     *
     * @param servers Server 配置
     * @return 导入结果
     */
    public static synchronized ImportResult importServers(List<LLMMcpServer> servers) {
        if (servers == null || servers.isEmpty()) {
            return new ImportResult(0, 0, 0);
        }
        LLMMcpServerStore store = LLMMcpServerStore.INSTANCE;
        Map<String, LLMMcpServer> existing = new LinkedHashMap<>();
        for (LLMMcpServer server : store.selectList()) {
            existing.put(normalizedName(server.getName()), server);
        }
        int inserted = 0;
        int updated = 0;
        int failed = 0;
        for (LLMMcpServer server : servers) {
            LLMMcpServer current = existing.get(normalizedName(server.getName()));
            boolean success;
            if (current == null) {
                success = store.insert(server);
                if (success) {
                    inserted++;
                }
            } else {
                server.setUid(current.getUid());
                success = store.update(server);
                if (success) {
                    updated++;
                }
            }
            if (success) {
                existing.put(normalizedName(server.getName()), server);
            } else {
                failed++;
            }
        }
        return new ImportResult(inserted, updated, failed);
    }

    private static String normalizedName(String name) {
        return name == null ? "" : name.trim().toLowerCase(Locale.ROOT);
    }

    public record ImportResult(int inserted, int updated, int failed) {

        public int total() {
            return this.inserted + this.updated;
        }
    }
}
