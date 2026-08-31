package cn.oyzh.easyshell.data.mysql.ui;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.data.db.dto.DBDataTransportObject;
import cn.oyzh.easyshell.data.db.ui.DBDataTransportObjectListView;
import cn.oyzh.easyshell.mysql.trigger.MysqlTrigger;

import java.util.List;

/**
 * @author oyzh
 * @since 2024/09/05
 */
public class ShellMysqlDataTransportTriggerListView extends DBDataTransportObjectListView {

    public void of(List<MysqlTrigger> triggers) {
        List<DBDataTransportObject> list = CollectionUtil.newArrayList();
        for (MysqlTrigger trigger : triggers) {
            DBDataTransportObject obj = new DBDataTransportObject();
            obj.setName(trigger.getName());
            list.add(obj);
        }
        this.init(list);
    }

}
