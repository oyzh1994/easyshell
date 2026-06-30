package cn.oyzh.easyshell.mongo.script;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.util.mongo.MongoUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author oyzh
 * @since 2024/2/26
 */
public class MongoScriptParser {

    private final String scriptContent;

    public MongoScriptParser(String scriptContent) {
        this.scriptContent = scriptContent;
    }

    public String removeComment() {
        return MongoUtil.removeComment(this.scriptContent);
    }
    //
    //    private Boolean single;
    //
    //    private Boolean select;
    //
    //    @Override
    //    public boolean isSingle() {
    //        if (this.single != null) {
    //            return this.single;
    //        }
    //        return false;
    //    }

    //    @Override
    //    public boolean isSelect() {
    //        if (this.select != null) {
    //            return this.select;
    //        }
    //        return false;
    //    }
    //
    //    @Override
    //    public boolean isFullColumn() {
    //        return false;
    //    }

    public List<String> parseScript() {
        String sqlContent = this.removeComment();
        List<String> sqlList = new ArrayList<>();
        // druid无法解析这些语句，直接返回
        if (StringUtil.startWithAnyIgnoreCase(sqlContent,
                "show dbs",
                "show collections"
        )) {
            sqlList.add(sqlContent);
            return sqlList;
        }
        AtomicBoolean startFlag = new AtomicBoolean();
        StringBuilder sql = new StringBuilder();
        sqlContent.lines().forEach(l -> {
            if (l.startsWith("db.")) {
                startFlag.set(true);
            }
            if (startFlag.get()) {
                sql.append(l);
            }
            if (startFlag.get() && l.stripTrailing().endsWith(";")) {
                sqlList.add(sql.toString());
                sql.delete(0, sql.length());
                startFlag.set(false);
            }
        });
        if (!sql.isEmpty()) {
            sqlList.add(sql.toString());
        }
        //        this.single = null;
        //        this.select = null;
        return sqlList;
    }

    //    @Override
    //    public String parseSingleSql() throws Exception {
    //        String sql = this.removeComment();
    //        sql = sql.replace("\n", " ");
    //        return sql;
    //    }

    //    @Override
    //    public String prettySql() {
    //        return this.sqlContent;
    //    }

    public static MongoScriptParser getParser(String script) {
        return new MongoScriptParser(script);
    }
}
