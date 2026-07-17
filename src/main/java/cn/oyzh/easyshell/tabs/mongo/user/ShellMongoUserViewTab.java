package cn.oyzh.easyshell.tabs.mongo.user;

import cn.oyzh.easyshell.mongo.user.MongoUser;
import cn.oyzh.easyshell.tabs.mongo.ShellMongoBaseTab;
import cn.oyzh.easyshell.trees.mongo.database.ShellMongoDatabaseTreeItem;
import cn.oyzh.fx.gui.svg.glyph.UserSVGGlyph;
import cn.oyzh.fx.plus.FXConst;
import javafx.scene.Cursor;

/**
 * mongodb用户tab
 *
 * @author oyzh
 * @since 2024/02/18
 */
public class ShellMongoUserViewTab extends ShellMongoBaseTab {

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "mongo/user/shellMongoUserViewTab.fxml";
    }

    @Override
    public void flushGraphic() {
        UserSVGGlyph graphic = (UserSVGGlyph) this.getGraphic();
        if (graphic == null) {
            graphic = new UserSVGGlyph();
            graphic.setCursor(Cursor.DEFAULT);
            this.setGraphic(graphic);
        }
    }

    @Override
    public void flushTitle() {
        String name = this.userName();
        this.setText(name + "@" + this.dbName() + "(" + this.connectName() + ")");
    }

    public String dbName() {
        return this.dbItem().dbName();
    }

    public String connectName() {
        return this.dbItem().connectName();
    }

    public String userName() {
        return this.controller().getMongoUser().getUser();
    }

    @Override
    public ShellMongoDatabaseTreeItem dbItem() {
        return this.controller().getDbItem();
    }

    /**
     * 初始化
     *
     * @param user 用户
     * @param dbItem     db库树节点
     */
    public void init(MongoUser user, ShellMongoDatabaseTreeItem dbItem) {
        this.controller().init(user, dbItem);
        // 刷新tab
        this.flush();
    }

    @Override
    public ShellMongoUserViewTabController controller() {
        return (ShellMongoUserViewTabController) super.controller();
    }
}
