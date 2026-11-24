package cn.oyzh.easyshell.util;

import cn.oyzh.easyshell.domain.ShellQuery;
import cn.oyzh.fx.db.util.DBUtil;

/**
 * 查询工具类
 *
 * @author oyzh
 * @since 2026-11-24
 */
public final class ShellQueryUtil {

    private ShellQueryUtil() {
    }

    /**
     * 创建查询副本
     *
     * @param source 源查询
     * @return 查询副本
     */
    public static ShellQuery cloneQuery(ShellQuery source) {
        if (source == null) {
            return null;
        }
        ShellQuery query = new ShellQuery();
        query.setIid(source.getIid());
        query.setName((source.getName() == null ? "" : source.getName()) + DBUtil.genCloneName());
        query.setContent(source.getContent());
        query.setDbIndex(source.getDbIndex());
        query.setDbName(source.getDbName());
        return query;
    }
}
