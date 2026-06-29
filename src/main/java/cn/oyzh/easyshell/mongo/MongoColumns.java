package cn.oyzh.easyshell.mongo;

import cn.oyzh.common.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * db外键列表
 *
 * @author oyzh
 * @since 2024/07/10
 */
public class MongoColumns extends DBObjectList<MongoColumn> {

    public MongoColumns() {

    }

    public MongoColumns(List<MongoColumn> list) {
        super.addAll(list);
    }

    public boolean exists(String name) {
        return this.column(name) != null;
    }

    public MongoColumn column(String name) {
        if (!this.isEmpty()) {
            for (MongoColumn dbColumn : this) {
                if (StringUtil.equalsAnyIgnoreCase(dbColumn.getName(), name)) {
                    return dbColumn;
                }
            }
        }
        return null;
    }

    public int index(String name) {
        int index = 0;
        for (MongoColumn dbColumn : this) {
            if (dbColumn.getName().equals(name)) {
                break;
            }
            index++;
        }
        return index;
    }

    public String collectionName() {
        for (MongoColumn dbColumn : this) {
            return dbColumn.getCollectionName();
        }
        return null;
    }

    public String dbName() {
        for (MongoColumn dbColumn : this) {
            return dbColumn.getDbName();
        }
        return null;
    }

    public List<String> columnNames() {
        List<String> list = new ArrayList<>();
        for (MongoColumn dbColumn : this) {
            list.add(dbColumn.getName());
        }
        return list;
    }
}
