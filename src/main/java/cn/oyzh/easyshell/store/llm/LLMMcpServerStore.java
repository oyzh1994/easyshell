package cn.oyzh.easyshell.store.llm;

import cn.oyzh.easyshell.llm.LLMMcpServer;
import cn.oyzh.store.jdbc.JdbcStandardStore;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * MCP Server 存储。
 *
 * @author oyzh
 * @since 2026-11-20
 */
public class LLMMcpServerStore extends JdbcStandardStore<LLMMcpServer> {

    public static final LLMMcpServerStore INSTANCE = new LLMMcpServerStore();

    public List<LLMMcpServer> selectEnabled() {
        return super.selectList().stream().filter(LLMMcpServer::isEnabled).toList();
    }

    public synchronized boolean setEnabled(LLMMcpServer server, boolean enabled) {
        if (server == null || server.getUid() == null) {
            return false;
        }
        server.setEnabled(enabled);
        return super.update(server);
    }

    /**
     * 按名称导入 MCP Server，已存在时更新原记录。
     *
     * @param servers Server 配置
     * @return 导入结果
     */
    public synchronized ImportResult importServers(List<LLMMcpServer> servers) {
        if (servers == null || servers.isEmpty()) {
            return new ImportResult(0, 0, 0);
        }
        Map<String, LLMMcpServer> existing = new LinkedHashMap<>();
        for (LLMMcpServer server : super.selectList()) {
            existing.put(normalizedName(server.getName()), server);
        }
        int inserted = 0;
        int updated = 0;
        int failed = 0;
        for (LLMMcpServer server : servers) {
            LLMMcpServer current = existing.get(normalizedName(server.getName()));
            boolean success;
            if (current == null) {
                success = super.insert(server);
                if (success) {
                    inserted++;
                }
            } else {
                server.setUid(current.getUid());
                success = super.update(server);
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

    @Override
    protected Class<LLMMcpServer> modelClass() {
        return LLMMcpServer.class;
    }

    public record ImportResult(int inserted, int updated, int failed) {

        public int total() {
            return this.inserted + this.updated;
        }
    }
}
