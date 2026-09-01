package cn.oyzh.easyshell.data.mysql.ui;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.fx.db.dto.DBTransportObject;
import cn.oyzh.fx.db.data.ui.DBDataTransportObjectListView;
import cn.oyzh.easyshell.mysql.procedure.MysqlProcedure;

import java.util.List;

/**
 * @author oyzh
 * @since 2024/09/05
 */
public class ShellMysqlDataTransportProcedureListView extends DBDataTransportObjectListView {

    public void of(List<MysqlProcedure> procedures) {
        List<DBTransportObject> list = CollectionUtil.newArrayList();
        for (MysqlProcedure procedure : procedures) {
            DBTransportObject obj = new DBTransportObject();
            obj.setName(procedure.getName());
            list.add(obj);
        }
        this.init(list);
    }

}
