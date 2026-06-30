package cn.oyzh.easyshell.mongo.bucket;

import cn.oyzh.common.object.ObjectComparator;
import cn.oyzh.common.object.ObjectCopier;
import cn.oyzh.common.util.StringUtil;

/**
 *
 * @author oyzh
 * @since 2026-06-01
 */
public class MongoBucket implements ObjectComparator<MongoBucket>, ObjectCopier<MongoBucket> {

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
    public boolean compare(MongoBucket table) {
        if (table == null) {
            return false;
        }
        if (table == this) {
            return true;
        }
        if (!StringUtil.equals(this.getName(), table.getName())) {
            return false;
        }
        return StringUtil.equals(this.getDbName(), table.getDbName());
    }

    @Override
    public void copy(MongoBucket gridFS) {
        if (gridFS != null) {
            this.setName(gridFS.getName());
            this.setDbName(gridFS.getDbName());
        }
    }
}
