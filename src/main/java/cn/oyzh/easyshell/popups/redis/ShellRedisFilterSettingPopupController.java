package cn.oyzh.easyshell.popups.redis;

import cn.oyzh.easyshell.filter.redis.ShellRedisKeyFilterParam;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.PopupController;
import cn.oyzh.fx.plus.controls.button.FXCheckBox;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.PopupAttribute;
import javafx.fxml.FXML;
import javafx.stage.WindowEvent;

/**
 * 过滤设置弹窗
 *
 * @author oyzh
 * @since 2025/01/24
 */
@PopupAttribute(
        value = FXConst.POPUP_PATH + "redis/shellRedisFilterSettingPopup.fxml"
)
public class ShellRedisFilterSettingPopupController extends PopupController {

//    /**
//     * 搜索键
//     */
//    @FXML
//    private FXCheckBox searchKey;

//    /**
//     * 搜索数据
//     */
//    @FXML
//    private FXCheckBox searchData;

    /**
     * 匹配大小写
     */
    @FXML
    private FXCheckBox matchCase;

    /**
     * 匹配全文
     */
    @FXML
    private FXCheckBox matchFull;

    /**
     * 应用
     */
    @FXML
    private void apply() {
        try {
            ShellRedisKeyFilterParam filterParam = new ShellRedisKeyFilterParam();
            filterParam.setMatchCase(this.matchCase.isSelected());
            filterParam.setMatchFull(this.matchFull.isSelected());
//            filterParam.setSearchKey(this.searchKey.isSelected());
//            filterParam.setSearchData(this.searchData.isSelected());
            this.submit(filterParam);
            this.closeWindow();
        } catch (Exception ex) {
            MessageBox.exception(ex);
        }
    }

    /**
     * 关闭
     */
    @FXML
    private void close() {
        this.closeWindow();
    }

    @Override
    public void onWindowShowing(WindowEvent event) {
        super.onWindowShowing(event);
        // 初始化
        ShellRedisKeyFilterParam filterParam = this.getProp("filterParam");
        if (filterParam != null) {
            this.matchCase.setSelected(filterParam.isMatchCase());
            this.matchFull.setSelected(filterParam.isMatchFull());
//            this.searchKey.setSelected(filterParam.isSearchKey());
//            this.searchData.setSelected(filterParam.isSearchData());
        }
    }
}