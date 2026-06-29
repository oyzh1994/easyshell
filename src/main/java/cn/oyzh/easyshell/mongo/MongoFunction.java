package cn.oyzh.easyshell.mongo;

import cn.oyzh.common.object.ObjectComparator;
import cn.oyzh.common.object.ObjectCopier;
import cn.oyzh.common.util.StringUtil;

/**
 *
 * @author oyzh
 * @since 2026-06-11
 */
public class MongoFunction implements ObjectCopier<MongoFunction>, ObjectComparator<MongoFunction> {

    private String name;

    private String code;

    private String dbName;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public boolean isNew() {
        return StringUtil.isBlank(this.getCode());
    }

    @Override
    public void copy(MongoFunction function) {
        if (function != null) {
            this.setName(function.getName());
            this.setCode(function.getCode());
            this.setDbName(function.getDbName());
        }
    }

    @Override
    public boolean compare(MongoFunction t1) {
        if (t1 == null) {
            return false;
        }
        if (t1 == this) {
            return true;
        }
        if (!StringUtil.equals(this.getName(), t1.getName())) {
            return false;
        }
        return StringUtil.equals(this.getDbName(), t1.getDbName());
    }
}
