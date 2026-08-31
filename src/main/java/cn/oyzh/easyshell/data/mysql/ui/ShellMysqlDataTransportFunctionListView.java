package cn.oyzh.easyshell.data.mysql.ui;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.data.db.dto.DBDataTransportObject;
import cn.oyzh.easyshell.data.db.ui.DBDataTransportObjectListView;
import cn.oyzh.easyshell.mysql.function.MysqlFunction;

import java.util.List;

/**
 * @author oyzh
 * @since 2024/09/05
 */
public class ShellMysqlDataTransportFunctionListView extends DBDataTransportObjectListView {

    public void of(List<MysqlFunction> functions) {
        List<DBDataTransportObject> list = CollectionUtil.newArrayList();
        for (MysqlFunction function : functions) {
            DBDataTransportObject obj = new DBDataTransportObject();
            obj.setName(function.getName());
            list.add(obj);
        }
        this.init(list);
    }
}
