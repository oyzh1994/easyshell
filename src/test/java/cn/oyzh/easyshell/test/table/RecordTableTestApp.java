package cn.oyzh.easyshell.test.table;

import cn.oyzh.easyshell.fx.mysql.record.ShellMysqlRecordTableView;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import cn.oyzh.fx.plus.controls.tab.FXTabPane;
import cn.oyzh.fx.plus.controls.table.FXTableColumn;
import cn.oyzh.fx.plus.ext.FXApplication;
import cn.oyzh.fx.plus.theme.ThemeManager;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * @author oyzh
 * @since 2023/11/21
 */
public class RecordTableTestApp extends FXApplication {

    public static void main(String[] args) {
        launch(RecordTableTestApp.class, args);
    }

    @Override
    public void start(Stage primaryStage) {
        super.start(primaryStage);

        ThemeManager.apply(ThemeManager.defaultTheme);

        FXTabPane tabPane1 = new FXTabPane();
        FXTab tab1 = new FXTab("测试");

        tabPane1.getTabs().add(tab1);

        FXTabPane tabPane2 = new FXTabPane();
        tab1.setContent(tabPane2);

        ShellMysqlRecordTableView tableView = new ShellMysqlRecordTableView();

        tableView.getColumns().add(new FXTableColumn<>("列1"));
        tableView.getColumns().add(new FXTableColumn<>("列2"));
        tableView.getColumns().add(new FXTableColumn<>("列3"));
        tableView.getColumns().add(new FXTableColumn<>("列4"));
        tableView.getColumns().add(new FXTableColumn<>("列5"));
        tableView.getColumns().add(new FXTableColumn<>("列6"));

        tabPane2.getTabs().add(new FXTab("内容1"));
        tabPane2.getTabs().add(new FXTab("内容", tableView));


        primaryStage.setScene(new Scene(tabPane1));
        primaryStage.show();

    }

    @Override
    protected void initSystemTray() {

    }

    public static class RecordTableTestAppStarter {

        public static void main(String[] args) {
            RecordTableTestApp.main(args);
        }

    }


}
