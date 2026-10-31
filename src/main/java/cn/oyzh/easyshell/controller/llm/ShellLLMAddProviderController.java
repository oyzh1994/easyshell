package cn.oyzh.easyshell.controller.llm;

import cn.oyzh.easyshell.llm.ui.LLMApiTypeComboBox;
import cn.oyzh.easyshell.store.llm.LLMProviderStore;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.gui.text.field.SelectTextFiled;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.window.FXStageStyle;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.stage.Modality;

/**
 * 大模型提供商新增业务
 *
 * @author oyzh
 * @since 2025-04-15
 */
@StageAttribute(
        stageStyle = FXStageStyle.EXTENDED,
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "llm/shellLLMAddProvider.fxml"
)
public class ShellLLMAddProviderController extends StageController {

    /**
     * 跳板名称
     */
    @FXML
    private ClearableTextField name;

    /**
     * 主机
     */
    @FXML
    private ClearableTextField baseUrl;

    /**
     * 主机
     */
    @FXML
    private ClearableTextField sk;

    @FXML
    private LLMApiTypeComboBox apiType;

    @FXML
    private SelectTextFiled<String> model;


    /**
     * 密钥存储
     */
    private final LLMProviderStore providerStore = LLMProviderStore.INSTANCE;


    /**
     * 添加跳板信息
     */
    @FXML
    private void add() {
    }

    @Override
    public String getViewTitle() {
        return I18nHelper.addHost();
    }
}
