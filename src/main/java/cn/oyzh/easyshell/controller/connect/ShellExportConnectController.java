package cn.oyzh.easyshell.controller.connect;

import cn.oyzh.common.file.FileUtil;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.dto.ShellConnectExport;
import cn.oyzh.easyshell.store.ShellConnectStore;
import cn.oyzh.easyshell.store.ShellGroupStore;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.chooser.FXChooser;
import cn.oyzh.fx.plus.chooser.FileChooserHelper;
import cn.oyzh.fx.plus.chooser.FileExtensionFilter;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.controls.button.FXCheckBox;
import cn.oyzh.fx.plus.controls.text.FXText;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;

import java.io.File;
import java.util.List;


/**
 * 连接导出业务
 *
 * @author oyzh
 * @since 2025/02/21
 */
@StageAttribute(
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "connect/shellExportConnect.fxml"
)
public class ShellExportConnectController extends StageController {

    /**
     * 导出文件
     */
    private File exportFile;

    /**
     * 文件名
     */
    @FXML
    private FXText fileName;

    /**
     * 包含分组
     */
    @FXML
    private FXCheckBox includeGroup;

    /**
     * 分组存储
     */
    private final ShellGroupStore groupStore = ShellGroupStore.INSTANCE;

    /**
     * 连接存储
     */
    private final ShellConnectStore connectStore = ShellConnectStore.INSTANCE;

    /**
     * 执行导出
     */
    @FXML
    private void doExport() {
        List<ShellConnect> connects = this.connectStore.loadFull();
        if (CollectionUtil.isEmpty(connects)) {
            MessageBox.warn(I18nHelper.connectionIsEmpty());
            return;
        }
        if (this.exportFile == null) {
            MessageBox.warn(I18nHelper.pleaseSelectFile());
            return;
        }
        ShellConnectExport export = ShellConnectExport.fromConnects(connects);
        // 分组
        if (this.includeGroup.isSelected()) {
            export.setGroups(this.groupStore.load());
        }
        try {
            FileUtil.writeUtf8String(export.toJSONString(), this.exportFile);
            this.closeWindow();
            MessageBox.okToast(I18nHelper.exportConnectionSuccess());
        } catch (Exception ex) {
            MessageBox.exception(ex, I18nHelper.exportConnectionFail());
        }
    }

    @Override
    public void onWindowShown(WindowEvent event) {
        super.onWindowShown(event);
        this.stage.hideOnEscape();
    }

    @Override
    public String getViewTitle() {
        return I18nHelper.exportConnect();
    }

    /**
     * 选择文件
     */
    @FXML
    private void selectFile() {
        FileExtensionFilter filter = FXChooser.jsonExtensionFilter();
        String fileName = "Shell-" + I18nHelper.connect() + ".json";
        this.exportFile = FileChooserHelper.save(fileName, fileName, filter);
        if (this.exportFile != null) {
            this.fileName.setText(this.exportFile.getPath());
        } else {
            this.fileName.clear();
        }
    }
}
