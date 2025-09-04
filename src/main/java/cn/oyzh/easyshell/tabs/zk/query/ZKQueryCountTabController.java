package cn.oyzh.easyshell.tabs.zk.query;//package cn.oyzh.easyshell.tabs.zk.query;
//
//import cn.oyzh.fx.gui.tabs.RichTabController;
//import cn.oyzh.fx.plus.controls.table.FXTableView;
//import cn.oyzh.fx.plus.property.KeyValueProperty;
//import cn.oyzh.fx.plus.property.Param1Property;
//import javafx.fxml.FXML;
//import org.apache.zookeeper.StatsTrack;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Objects;
//
///**
// * @author oyzh
// * @since 2025/02/26
// */
//public class ZKQueryCountTabController extends RichTabController {
//
//    @FXML
//    private FXTableView<Param1Property<Object>> countTable;
//
//    public void init(Integer count) {
//        List<Param1Property<Object>> data = new ArrayList<>();
//        data.add(Param1Property.of(Objects.requireNonNullElse(count, 0)));
//        this.countTable.setItem(data);
//    }
//
//}