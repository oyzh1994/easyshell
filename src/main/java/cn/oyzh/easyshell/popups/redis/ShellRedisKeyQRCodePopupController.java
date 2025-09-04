package cn.oyzh.easyshell.popups.redis;

import cn.oyzh.common.exception.ExceptionUtil;
import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.qrcode.QRCodeUtil;
import cn.oyzh.easyshell.redis.key.ShellRedisKey;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.PopupController;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.node.NodeUtil;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.plus.window.PopupAttribute;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.stage.WindowEvent;

import java.awt.image.BufferedImage;


/**
 * redis键值二维码业务
 *
 * @author oyzh
 * @since 2025/02/51
 */
@PopupAttribute(
        value = FXConst.POPUP_PATH + "redis/shellRedisKeyQRCodePopup.fxml"
)
public class ShellRedisKeyQRCodePopupController extends PopupController {

    /**
     * 二维码图片
     */
    @FXML
    private ImageView qrcode;

    @Override
    public void onWindowShowing(WindowEvent event) {
        super.onWindowShowing(event);
        this.initQRCode();
    }

    /**
     * 初始化二维码
     */
    private void initQRCode() {
        try {
            ShellRedisKey key = this.getProp("key");
            String keyData = this.getProp("keyData");
            StringBuilder builder = new StringBuilder();
            builder.append(I18nHelper.key()).append(": ")
                    .append(key.getKey()).append("\n")
                    .append(I18nHelper.database()).append(": ")
                    .append(key.getDbIndex()).append("\n")
                    .append(I18nHelper.data()).append(": ")
                    .append(keyData);
            JulLog.info("generate qrcode begin.");
            int codeW = (int) NodeUtil.getWidth(this.qrcode);
            int codeH = (int) NodeUtil.getHeight(this.qrcode);
            BufferedImage source = QRCodeUtil.createImage(builder.toString(), "utf-8", codeW, codeH);
            this.qrcode.setImage(FXUtil.toImage(source));
        } catch (Exception ex) {
            this.closeWindow();
            if (ExceptionUtil.hasMessage(ex, "Data too big")) {
                MessageBox.warn(I18nHelper.dataTooLarge());
            } else {
                ex.printStackTrace();
                JulLog.warn("initQRCode error, ex:{}", ex.getMessage());
                MessageBox.warn(I18nHelper.operationFail());
            }
        }
    }
}
