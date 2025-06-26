package cn.oyzh.easyshell.tabs.s3;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.fx.ShellOsTypeComboBox;
import cn.oyzh.easyshell.s3.ShellS3Client;
import cn.oyzh.easyshell.tabs.ShellConnectTab;
import cn.oyzh.easyshell.trees.connect.ShellConnectTreeItem;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.node.NodeLifeCycle;
import cn.oyzh.fx.plus.window.StageAdapter;
import cn.oyzh.fx.plus.window.StageManager;
import javafx.scene.Cursor;

/**
 * ftp tab
 *
 * @author oyzh
 * @since 2023/7/21
 */
public class ShellS3Tab extends ShellConnectTab implements NodeLifeCycle {

    public ShellS3Tab(ShellConnect connect) {
        this.init(connect);
    }

    @Override
    protected String url() {
        return "/tabs/s3/shellS3Tab.fxml";
    }

    @Override
    public void flushGraphic() {
        SVGGlyph graphic = (SVGGlyph) this.getGraphic();
        if (graphic == null) {
            graphic = ShellOsTypeComboBox.getGlyph(this.shellConnect().getOsType());
            graphic.setSizeStr("13");
            graphic.setCursor(Cursor.DEFAULT);
            this.setGraphic(graphic);
        }
    }

    /**
     * 初始化
     *
     * @param connect 连接
     */
    public void init(ShellConnect connect) {
        try {
            // 初始化shell连接
            this.controller().init(connect);
            // 刷新图标
            this.flush();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected String getTabTitle() {
        return this.controller().shellConnect().getName() + "(" + this.shellConnect().getType().toUpperCase() + ")";
    }

    @Override
    public ShellS3TabController controller() {
        return (ShellS3TabController) super.controller();
    }

   @Override
    public ShellConnect shellConnect() {
        return this.controller().shellConnect();
    }

    /**
     * 获取shell客户端
     *
     * @return shell客户端
     */
    public ShellS3Client client() {
        return this.controller().client();
    }

    @Override
    public void onNodeInitialize() {
        NodeLifeCycle.super.onNodeInitialize();
        // 更新一下内容
        StageAdapter adapter = StageManager.getAdapter(this.window());
        if (adapter != null) {
            adapter.updateContentLater();
        }
    }
}
