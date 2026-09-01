package cn.oyzh.easyshell.data.mysql.ui;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.fx.db.dto.DBTransportObject;
import cn.oyzh.fx.db.ui.DBDataTransportObjectListView;
import cn.oyzh.easyshell.mysql.trigger.MysqlTrigger;

import java.util.List;

/**
 * @author oyzh
 * @since 2024/09/05
 */
public class ShellMysqlDataTransportTriggerListView extends DBDataTransportObjectListView {

    public void of(List<MysqlTrigger> triggers) {
        List<DBTransportObject> list = CollectionUtil.newArrayList();
        for (MysqlTrigger trigger : triggers) {
            DBTransportObject obj = new DBTransportObject();
            obj.setName(trigger.getName());
            list.add(obj);
        }
        this.init(list);
    }

}
