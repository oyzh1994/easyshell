package cn.oyzh.easyssh.domain;


import cn.oyzh.fx.plus.domain.AppSetting;
import cn.oyzh.store.jdbc.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * ssh设置
 *
 * @author oyzh
 * @since 2023/6/16
 */
@Data
@Table("t_setting")
@EqualsAndHashCode(callSuper = true)
public class SSHSetting extends AppSetting {
}
