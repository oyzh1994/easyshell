package cn.oyzh.easyshell.store.llm;

import cn.oyzh.easyshell.llm.mcp.LLMMcpServer;
import cn.oyzh.store.jdbc.JdbcStandardStore;

import java.util.List;

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
        boolean previous = server.isEnabled();
        server.setEnabled(enabled);
        if (super.update(server)) {
            return true;
        }
        server.setEnabled(previous);
        return false;
    }

    @Override
    protected Class<LLMMcpServer> modelClass() {
        return LLMMcpServer.class;
    }

}
