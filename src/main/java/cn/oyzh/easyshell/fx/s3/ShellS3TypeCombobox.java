package cn.oyzh.easyshell.fx.s3;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;
import cn.oyzh.i18n.I18nHelper;
import software.amazon.awssdk.regions.Region;

/**
 * s3协议类型
 *
 * @author oyzh
 * @since 2025-07-15
 */
public class ShellS3TypeCombobox extends FXComboBox<Region> {

    {
        this.addItem("Minio");
        this.addItem(I18nHelper.aliyun() + " oss");
        this.addItem(I18nHelper.tencent() + " cos");
        this.addItem(I18nHelper.standard() + " s3");
    }

    public String getType() {
        return switch (this.getSelectedIndex()) {
            case 0 -> "Minio";
            case 1 -> "Alibaba";
            case 2 -> "Tencent";
            default -> "S3";
        };
    }

    public void select(String type) {
        if (StringUtil.isBlank(type) || StringUtil.equalsIgnoreCase(type, "S3")) {
            this.select(3);
        } else if (StringUtil.equalsIgnoreCase(type, "Minio")) {
            this.select(0);
        } else if (StringUtil.equalsIgnoreCase(type, "Alibaba")) {
            this.select(1);
        } else if (StringUtil.equalsIgnoreCase(type, "Tencent")) {
            this.select(2);
        }
    }
}
