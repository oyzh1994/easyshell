package cn.oyzh.easyshell.store.llm;

import cn.oyzh.easyshell.llm.LLMSession;
import cn.oyzh.store.jdbc.JdbcStandardStore;
import cn.oyzh.store.jdbc.param.OrderByParam;
import cn.oyzh.store.jdbc.param.SelectParam;

import java.util.List;

/**
 * LLM会话存储
 *
 * @author oyzh
 * @since 2026-10-31
 */
public class LLMSessionStore extends JdbcStandardStore<LLMSession> {

    /**
     * 当前实例
     */
    public static final LLMSessionStore INSTANCE = new LLMSessionStore();

    /**
     * 加载会话历史，按更新时间倒序
     *
     * @return 会话列表
     */
    public List<LLMSession> loadRecent() {
        SelectParam param = new SelectParam();
        param.addOrderByParam(new OrderByParam("updateTime", "desc"));
        return super.selectList(param);
    }

    /**
     * 保存会话
     *
     * @param session 会话
     * @return 结果
     */
    public boolean save(LLMSession session) {
        if (session == null || session.getUid() == null) {
            return false;
        }
        if (super.exist(session.getUid())) {
            return super.update(session);
        }
        return super.insert(session);
    }

    @Override
    protected Class<LLMSession> modelClass() {
        return LLMSession.class;
    }
}
