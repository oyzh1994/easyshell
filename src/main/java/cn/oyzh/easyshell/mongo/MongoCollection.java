package cn.oyzh.easyshell.mongo;

import cn.oyzh.common.object.ObjectComparator;
import cn.oyzh.common.object.ObjectCopier;
import cn.oyzh.common.util.StringUtil;

import java.util.Comparator;

/**
 *
 * @author oyzh
 * @since 2026-06-01
 */
public class MongoCollection implements ObjectComparator<MongoCollection>, ObjectCopier<MongoCollection> {

    private String name;

    private String dbName;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    @Override
    public boolean compare(MongoCollection collection) {
        if (collection == null) {
            return false;
        }
        if (collection == this) {
            return true;
        }
        if (!StringUtil.equals(this.getName(), collection.getName())) {
            return false;
        }
        return StringUtil.equals(this.getDbName(), collection.getDbName());
    }

    @Override
    public void copy(MongoCollection collection) {
        if (collection != null) {
            this.setName(collection.getName());
            this.setDbName(collection.getDbName());
        }
    }
}
