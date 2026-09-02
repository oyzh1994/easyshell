package cn.oyzh.easyshell.data.mongo.ui;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.mongo.function.MongoFunction;
import cn.oyzh.fx.db.data.dto.DBDataTransportObject;
import cn.oyzh.fx.db.data.ui.DBDataTransportObjectListView;
import cn.oyzh.fx.plus.controls.button.FXCheckBox;
import cn.oyzh.fx.plus.controls.list.FXListView;
import cn.oyzh.fx.plus.util.ListViewUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author oyzh
 * @since 2024/09/05
 */
public class ShellMongoDataTransportFunctionListView extends DBDataTransportObjectListView {

    public void of(List<MongoFunction> functions) {
        List<DBDataTransportObject> list = CollectionUtil.newArrayList();
        for (MongoFunction function : functions) {
            DBDataTransportObject obj = new DBDataTransportObject();
            obj.setName(function.getName());
            list.add(obj);
        }
        this.init(list);
    }
}
