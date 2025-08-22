package cn.oyzh.easyshell.vnc;

import cn.oyzh.common.exception.ExceptionUtil;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.i18n.I18nHelper;
import org.jfxvnc.net.rfb.codec.ProtocolState;
import org.jfxvnc.ui.service.VncRenderService;

/**
 * @author oyzh
 * @since 2025-07-18
 */
public class ShellVNCRenderService extends VncRenderService {

    {
        // 监听是否认证成功
        this.protocolStateProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == ProtocolState.SECURITY_FAILED) {
                MessageBox.warn(I18nHelper.authFail() + "," + I18nHelper.pleaseCheckUserNameOrPassword());
            }
        });
        // 异常处理
        this.exceptionCaughtProperty().addListener((observable, oldValue, newValue) -> {
            // 认证失败的有单独的弹窗
            if (!ExceptionUtil.hasMessage(newValue, "Authentication failed")) {
                MessageBox.exception(newValue);
            }
        });
    }

    /**
     * 新实例
     *
     * @return 实例
     */
    public static ShellVNCRenderService newInstance() {
        return new ShellVNCRenderService();
    }
}
