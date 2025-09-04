package cn.oyzh.easyshell.popups.zk;

import cn.oyzh.easyshell.filter.zk.ZKNodeFilterParam;
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
 * @since 2025/01/22
 */
@PopupAttribute(
        value = FXConst.POPUP_PATH + "zk/zkFilterSettingPopup.fxml"
)
public class ZKFilterSettingPopupController extends PopupController {

    /**
     * 搜索路径
     */
    @FXML
    private FXCheckBox searchPath;

    /**
     * 搜索数据
     */
    @FXML
    private FXCheckBox searchData;

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
            ZKNodeFilterParam filterParam = new ZKNodeFilterParam();
            filterParam.setMatchCase(this.matchCase.isSelected());
            filterParam.setMatchFull(this.matchFull.isSelected());
            filterParam.setSearchPath(this.searchPath.isSelected());
            filterParam.setSearchData(this.searchData.isSelected());
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
        ZKNodeFilterParam filterParam = this.getProp("filterParam");
        if (filterParam != null) {
            this.matchCase.setSelected(filterParam.isMatchCase());
            this.matchFull.setSelected(filterParam.isMatchFull());
            this.searchPath.setSelected(filterParam.isSearchPath());
            this.searchData.setSelected(filterParam.isSearchData());
        }
    }
}