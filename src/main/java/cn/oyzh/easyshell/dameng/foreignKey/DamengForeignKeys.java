package cn.oyzh.easyshell.dameng.foreignKey;

import cn.oyzh.fx.db.DBObjectList;

import java.util.Collection;

/**
 * db外键列表
 *
 * @author oyzh
 * @since 2024/07/10
 */
public class DamengForeignKeys extends DBObjectList<DamengForeignKey> {

    public DamengForeignKeys() {

    }

    public DamengForeignKeys(Collection<DamengForeignKey> list) {
        super.addAll(list);
    }
}



