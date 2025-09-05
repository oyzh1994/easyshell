package cn.oyzh.easyshell.popups.zk;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.qrcode.QRCodeUtil;
import cn.oyzh.easyshell.zk.ZKNode;
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
 * zk数据二维码业务
 *
 * @author oyzh
 * @since 2024/02/21
 */
@PopupAttribute(
        value = FXConst.POPUP_PATH + "zk/zkNodeQRCodePopup.fxml"
)
public class ShellZKNodeQRCodePopupController extends PopupController {

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
            ZKNode zkNode = this.getProp("zkNode");
            String nodeData = this.getProp("nodeData");
            StringBuilder builder = new StringBuilder();
            builder.append(I18nHelper.nodePath()).append(": ")
                    .append(zkNode.decodeNodePath()).append("\n")
                    .append(I18nHelper.nodeData()).append(": ")
                    .append(nodeData);
            JulLog.info("generate qrcode begin.");
            int codeW = (int) NodeUtil.getWidth(this.qrcode);
            int codeH = (int) NodeUtil.getHeight(this.qrcode);
            BufferedImage source = QRCodeUtil.createImage(builder.toString(), "utf-8", codeW, codeH);
            this.qrcode.setImage(FXUtil.toImage(source));
        } catch (Exception ex) {
            this.closeWindow();
            ex.printStackTrace();
            JulLog.warn("initQRCode error, ex:{}", ex.getMessage());
            MessageBox.warn(I18nHelper.operationFail());
        }
    }
}
