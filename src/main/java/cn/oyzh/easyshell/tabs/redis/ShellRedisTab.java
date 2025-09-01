package cn.oyzh.easyshell.tabs.redis;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.fx.ShellOsTypeComboBox;
import cn.oyzh.easyshell.redis.RedisClient;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.Cursor;

/**
 * @author oyzh
 * @since 2024-12-03
 */
public class ShellRedisTab extends RichTab {

    public ShellRedisTab(ShellConnect connect) {
        super();
        this.init(connect);
        super.flush();
    }

    @Override
    public String getTabTitle() {
        return this.shellConnect().getName() + "(" + this.shellConnect().getType().toUpperCase() + ")";
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

    @Override
    protected String url() {
        return "/tabs/redis/shellRedisTab.fxml";
    }

    @Override
    protected ShellRedisTabController controller() {
        return (ShellRedisTabController) super.controller();
    }

    private RedisClient client;

    public RedisClient client() {
        return this.client;
    }

    public ShellConnect shellConnect() {
        return this.client().shellConnect();
    }

    public void init(ShellConnect connect) {
        this.client = new RedisClient(connect);
        // 加载根节点
        StageManager.showMask(() -> {
            try {
                client.start();
                if (!client.isConnected()) {
                    MessageBox.warn(I18nHelper.connectFail());
                    return;
                }
                this.controller().init(client);
            } catch (Exception ex) {
                ex.printStackTrace();
                MessageBox.exception(ex);
            }
        });
    }

}
