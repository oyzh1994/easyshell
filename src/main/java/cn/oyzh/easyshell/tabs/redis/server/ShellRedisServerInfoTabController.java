package cn.oyzh.easyshell.tabs.redis.server;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.dto.redis.RedisInfoProp;
import cn.oyzh.easyshell.dto.redis.RedisInfoPropItem;
import cn.oyzh.fx.gui.tabs.SubTabController;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import cn.oyzh.fx.plus.controls.tab.FXTabPane;
import cn.oyzh.fx.plus.controls.table.FXTableColumn;
import cn.oyzh.fx.plus.controls.table.FXTableView;
import cn.oyzh.fx.plus.tableview.TableViewUtil;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.i18n.I18nHelper;
import com.alibaba.fastjson2.JSONObject;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;
import java.util.Optional;

/**
 * redis服务信息tab内容组件
 *
 * @author oyzh
 * @since 2023/08/01
 */
public class ShellRedisServerInfoTabController extends SubTabController {

    /**
     * tab面板
     */
    @FXML
    private FXTabPane tabPane;

    /**
     * 执行初始化
     *
     * @param prop 属性对象
     */
    public void init(RedisInfoProp prop) {
        this.initPropPane(prop);
    }

    /**
     * 初始化属性面板
     */
    private void initPropPane(RedisInfoProp prop) {
        List<String> groups = prop.groups().stream().sorted().toList();
        for (String group : groups) {
            this.initPropTab(prop, group);
        }
    }

    /**
     * 初始化属性tab
     *
     * @param group 属性组
     */
    private void initPropTab(RedisInfoProp prop, String group) {
        JSONObject object = prop.getProps(group);
        if (object == null || object.isEmpty()) {
            return;
        }
        Optional<Tab> tabOptional = this.tabPane.getTabs().stream().filter(t -> StringUtil.equals(t.getId(), "prop-" + group)).findFirst();
        FXTableView<RedisInfoPropItem> tableView;
        if (tabOptional.isEmpty()) {
            FXTab fxTab = new FXTab();
            fxTab.setText(group);
            fxTab.setId("prop-" + group);
            tableView = new FXTableView<>();
            tableView.setFlexWidth("100%");
            tableView.setFlexHeight("100%");

            FXTableColumn<RedisInfoPropItem, String> name = new FXTableColumn<>();
            name.setText(I18nHelper.name());
            name.setFlexWidth("30%");
            name.setCellValueFactory(new PropertyValueFactory<>("name"));

            FXTableColumn<RedisInfoPropItem, String> value = new FXTableColumn<>();
            value.setText(I18nHelper.value());
            value.setFlexWidth("70% - 20");
            value.setCellValueFactory(new PropertyValueFactory<>("value"));

            FXUtil.runWait(() -> {
                tableView.getColumns().add(name);
                tableView.getColumns().add(value);
            });

            // 双击时，复制列数据
            TableViewUtil.copyCellDataOnDoubleClicked(tableView);

            fxTab.setContent(tableView);
            this.tabPane.addTab(fxTab);
        } else {
            tableView = (FXTableView<RedisInfoPropItem>) tabOptional.get().getContent();
        }
        for (String key : object.keySet()) {
            this.initPropItem(tableView, key, object.getString(key));
        }
    }

    /**
     * 初始化属性内容
     *
     * @param tableView 表单组件
     * @param name      名称
     * @param value     值
     */
    private void initPropItem(TableView<RedisInfoPropItem> tableView, String name, String value) {
        ObservableList<RedisInfoPropItem> items = tableView.getItems();
        Optional<RedisInfoPropItem> optional = items.parallelStream().filter(i -> StringUtil.equals(i.getName(), name)).findFirst();
        if (optional.isEmpty()) {
            tableView.getItems().add(new RedisInfoPropItem(name, value));
        } else if (!StringUtil.equals(optional.get().getValue(), value)) {
            optional.get().setValue(value);
        }
    }
}
