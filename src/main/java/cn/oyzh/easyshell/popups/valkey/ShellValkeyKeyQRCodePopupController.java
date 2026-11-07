package cn.oyzh.easyshell.popups.valkey;

import cn.oyzh.common.exception.ExceptionUtil;
import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.qrcode.QRCodeUtil;
import cn.oyzh.easyshell.valkey.key.ShellValkeyKey;
import cn.oyzh.easyshell.util.ShellI18nHelper;
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
 * @since 2025-09-01
 */
@PopupAttribute(
        value = FXConst.POPUP_PATH + "valkey/shellValkeyKeyQRCodePopup.fxml"
)
public class ShellValkeyKeyQRCodePopupController extends PopupController {

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
            ShellValkeyKey key = this.getProp("key");
            String keyData = this.getProp("keyData");
            StringBuilder builder = new StringBuilder();
            builder.append(ShellI18nHelper.label(I18nHelper.key(), key.getKey()))
                    .append("\n")
                    .append(ShellI18nHelper.label(I18nHelper.database(), key.getDbIndex()))
                    .append("\n")
                    .append(ShellI18nHelper.label(I18nHelper.data(), keyData));
            JulLog.info("generate qrcode begin.");
            int codeW = (int) NodeUtil.getWidth(this.qrcode);
            int codeH = (int) NodeUtil.getHeight(this.qrcode);
            BufferedImage source = QRCodeUtil.createImage(builder.toString(), "utf-8", codeW, codeH);
            this.qrcode.setImage(FXUtil.toImage(source));
        } catch (Exception ex) {
            this.closeWindow();
            if (ExceptionUtil.hasMessage(ex, "Data too big")) {
                MessageBox.warn(I18nHelper.dataTooLarge(), this.getWindow());
            } else {
                ex.printStackTrace();
                JulLog.warn("initQRCode error, ex:{}", ex.getMessage());
                MessageBox.warn(I18nHelper.operationFail(), this.getWindow());
            }
        }
    }
}
