package cn.oyzh.easyshell.controller.connect;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellGroup;
import cn.oyzh.easyshell.internal.ShellPrototype;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.easyshell.util.ShellViewFactory;
import cn.oyzh.fx.gui.button.NextStepButton;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.controls.box.FXVBox;
import cn.oyzh.fx.plus.controls.button.FXRadioButton;
import cn.oyzh.fx.plus.controls.label.FXLabel;
import cn.oyzh.fx.plus.controls.toggle.FXToggleGroup;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.FXStageStyle;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Labeled;
import javafx.scene.control.Toggle;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * 连接新增引导业务
 *
 * @author oyzh
 * @since 2025-04-24
 */
@StageAttribute(
        stageStyle = FXStageStyle.EXTENDED,
        cssUrls = "/css/connect-guide.css",
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "connect/shellAddConnectGuid.fxml"
)
public class ShellAddConnectGuidController extends StageController {

    /**
     * 全部分类标识
     */
    private static final String CATEGORY_ALL = "ALL";

    /**
     * 默认选中的连接类型
     */
    private static final String DEFAULT_TYPE_ID = "ssh";

    @FXML
    private FXLabel subtitleLabel;

    @FXML
    private ClearableTextField searchField;

    @FXML
    private FXLabel resultLabel;

    @FXML
    private FXLabel emptyLabel;

    @FXML
    private FXVBox sectionBox;

    @FXML
    private FXLabel selectionLabel;

    @FXML
    private NextStepButton nextButton;

    @FXML
    private FXToggleGroup categoryGroup;

    @FXML
    private FXToggleGroup cardGroup;

    /**
     * FXML 中声明的协议卡片，按界面顺序保存
     */
    private final Map<String, FXRadioButton> cards = new LinkedHashMap<>();

    /**
     * 卡片所属的分组标识
     */
    private final Map<String, String> cardCategories = new LinkedHashMap<>();

    /**
     * FXML 中声明的分组区块
     */
    private final Map<String, FXVBox> sections = new LinkedHashMap<>();

    /**
     * 当前分组
     */
    private ShellGroup group;

    /**
     * 搜索结果中的第一张可见卡片
     */
    private FXRadioButton firstCard;

    /**
     * 初始化页面
     */
    @FXML
    private void initialize() {
        this.subtitleLabel.text(ShellI18nHelper.connectGuidSubtitle());
        this.emptyLabel.text(ShellI18nHelper.connectGuidEmpty());
        this.collectFxmlNodes();
        this.cardGroup.selectedToggleProperty().addListener((observable, oldToggle, newToggle) -> this.updateSelection());
        this.searchField.textProperty().addListener((observable, oldText, newText) -> this.renderTypes());
        this.renderTypes();
        this.selectType(DEFAULT_TYPE_ID);
        this.updateSelection();
    }

    /**
     * 收集 FXML 中声明的分组与卡片节点
     */
    private void collectFxmlNodes() {
        for (Node node : this.sectionBox.lookupAll(".connect-guide-section")) {
            FXVBox section = (FXVBox) node;
            String category = (String) section.getUserData();
            this.sections.put(category, section);
            for (Node cardNode : section.lookupAll(".connect-type-card")) {
                FXRadioButton card = (FXRadioButton) cardNode;
                String id = (String) card.getUserData();
                this.cards.put(id, card);
                this.cardCategories.put(id, category);
            }
        }
    }

    /**
     * 按搜索关键字与当前分组刷新卡片可见性
     */
    @FXML
    private void renderTypes() {
        String keyword = this.searchField.getTextTrim().toLowerCase(Locale.ROOT);
        String category = this.selectedCategory();
        int visibleCount = 0;
        this.firstCard = null;

        for (Map.Entry<String, FXRadioButton> entry : this.cards.entrySet()) {
            FXRadioButton card = entry.getValue();
            boolean visible = this.matches(entry.getKey(), keyword, category);
            card.setVisible(visible);
            card.setManaged(visible);
            if (visible) {
                visibleCount++;
                if (this.firstCard == null) {
                    this.firstCard = card;
                }
            }
        }

        for (FXVBox section : this.sections.values()) {
            boolean hasVisibleCard = section.lookupAll(".connect-type-card").stream().anyMatch(Node::isManaged);
            section.setVisible(hasVisibleCard);
            section.setManaged(hasVisibleCard);
        }

        this.emptyLabel.setVisible(visibleCount == 0);
        this.emptyLabel.setManaged(visibleCount == 0);
        this.resultLabel.text(ShellI18nHelper.connectGuidResultCount(visibleCount));
        this.ensureSelectionVisible();
        this.updateSelection();
    }

    /**
     * 卡片是否匹配搜索关键字与分组
     */
    private boolean matches(String id, String keyword, String category) {
        if (!CATEGORY_ALL.equals(category) && !category.equals(this.cardCategories.get(id))) {
            return false;
        }
        if (keyword.isEmpty()) {
            return true;
        }
        return StringUtil.containsIgnoreCase(id, keyword);
    }

    /**
     * 获取当前分组标识
     */
    private String selectedCategory() {
        String category = this.categoryGroup.selectedUserData();
        return category == null ? CATEGORY_ALL : category;
    }

    /**
     * 选中指定协议
     */
    private void selectType(String id) {
        FXRadioButton card = this.cards.get(id);
        if (card != null && card.isManaged()) {
            this.cardGroup.selectToggle(card);
        }
    }

    /**
     * 搜索或切换分组后，若选中项已不可见则取消选中
     */
    private void ensureSelectionVisible() {
        Toggle selected = this.cardGroup.getSelectedToggle();
        if (selected instanceof Node node && !node.isManaged()) {
            this.cardGroup.selectToggle(null);
        }
    }

    /**
     * 更新已选协议提示与下一步按钮状态
     */
    private void updateSelection() {
        String id = this.cardGroup.selectedUserData();
        if (id == null) {
            this.selectionLabel.clear();
            this.nextButton.disable();
        } else {
            this.selectionLabel.text(ShellI18nHelper.connectGuidSelected(this.cardName(id)));
            this.nextButton.enable();
        }
    }

    /**
     * 读取 FXML 卡片中声明的协议名称（含 %key 解析后的 i18n 文本）
     */
    private String cardName(String id) {
        FXRadioButton card = this.cards.get(id);
        if (card != null && card.getGraphic() instanceof Parent graphic) {
            for (Node node : graphic.lookupAll(".connect-card-name")) {
                if (node instanceof Labeled labeled) {
                    return labeled.getText();
                }
            }
        }
        return id;
    }

    /**
     * 协议卡片双击直接创建连接
     */
    @FXML
    private void onCardClicked(MouseEvent event) {
        if (event.getClickCount() == 2 && event.getSource() instanceof FXRadioButton card) {
            this.cardGroup.selectToggle(card);
            this.toAdd();
            event.consume();
        }
    }

    /**
     * 搜索框回车直接创建首个结果，Esc 清空搜索
     */
    @FXML
    private void onSearchKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER && this.firstCard != null) {
            this.cardGroup.selectToggle(this.firstCard);
            this.toAdd();
            event.consume();
        }
    }

    /**
     * 添加连接
     */
    @FXML
    private void toAdd() {
        try {
            String userData = this.cardGroup.selectedUserData();
            if (ShellPrototype.SSH.equalsIgnoreCase(userData)) {
                ShellViewFactory.addSSHConnect(group);
            } else if (ShellPrototype.LOCAL.equalsIgnoreCase(userData)) {
                ShellViewFactory.addLocalConnect(group);
            } else if (ShellPrototype.TELNET.equalsIgnoreCase(userData)) {
                ShellViewFactory.addTelnetConnect(group);
            } else if (ShellPrototype.SFTP.equalsIgnoreCase(userData)) {
                ShellViewFactory.addSFTPConnect(group);
            } else if (ShellPrototype.FTP.equalsIgnoreCase(userData)) {
                ShellViewFactory.addFTPConnect(group);
            } else if (ShellPrototype.S3.equalsIgnoreCase(userData)) {
                ShellViewFactory.addS3Connect(group, null);
            } else if ("s3_cos".equalsIgnoreCase(userData)) {
                ShellViewFactory.addS3Connect(group, "cos");
            } else if ("s3_obs".equalsIgnoreCase(userData)) {
                ShellViewFactory.addS3Connect(group, "obs");
            } else if ("s3_oss".equalsIgnoreCase(userData)) {
                ShellViewFactory.addS3Connect(group, "oss");
            } else if ("s3_minio".equalsIgnoreCase(userData)) {
                ShellViewFactory.addS3Connect(group, "minio");
            } else if (ShellPrototype.SERIAL.equalsIgnoreCase(userData)) {
                ShellViewFactory.addSerialConnect(group);
            } else if (ShellPrototype.VNC.equalsIgnoreCase(userData)) {
                ShellViewFactory.addVNCConnect(group);
            } else if (ShellPrototype.RLOGIN.equalsIgnoreCase(userData)) {
                ShellViewFactory.addRLoginConnect(group);
            } else if (ShellPrototype.RSH.equalsIgnoreCase(userData)) {
                ShellViewFactory.addRshConnect(group);
            } else if (ShellPrototype.SMB.equalsIgnoreCase(userData)) {
                ShellViewFactory.addSMBConnect(group);
            } else if (ShellPrototype.REDIS.equalsIgnoreCase(userData)) {
                ShellViewFactory.addRedisConnect(group);
            } else if (ShellPrototype.VALKEY.equalsIgnoreCase(userData)) {
                ShellViewFactory.addValkeyConnect(group);
            } else if (ShellPrototype.ZOOKEEPER.equalsIgnoreCase(userData)) {
                ShellViewFactory.addZKConnect(group);
            } else if (ShellPrototype.RDP.equalsIgnoreCase(userData)) {
                ShellViewFactory.addRDPConnect(group);
            } else if (ShellPrototype.WEBDAV.equalsIgnoreCase(userData)) {
                ShellViewFactory.addWebdavConnect(group);
            } else if (ShellPrototype.MYSQL.equalsIgnoreCase(userData)) {
                ShellViewFactory.addMysqlConnect(group);
            } else if (ShellPrototype.MARIADB.equalsIgnoreCase(userData)) {
                ShellViewFactory.addMariadbConnect(group);
            } else if (ShellPrototype.SQLITE.equalsIgnoreCase(userData)) {
                ShellViewFactory.addSqliteConnect(group);
            } else if (ShellPrototype.H2.equalsIgnoreCase(userData)) {
                ShellViewFactory.addH2Connect(group);
            } else if (ShellPrototype.DAMENG.equalsIgnoreCase(userData)) {
                ShellViewFactory.addDamengConnect(group);
            } else if (ShellPrototype.POSTGRESQL.equalsIgnoreCase(userData)) {
                ShellViewFactory.addPostgresqlConnect(group);
            } else if (ShellPrototype.SQLSERVER.equalsIgnoreCase(userData)) {
                ShellViewFactory.addSqlServerConnect(group);
            } else if (ShellPrototype.ORACLE.equalsIgnoreCase(userData)) {
                ShellViewFactory.addOracleConnect(group);
            } else if (ShellPrototype.MONGO.equalsIgnoreCase(userData)) {
                ShellViewFactory.addMongoConnect(group);
            } else if (ShellPrototype.MOSH.equalsIgnoreCase(userData)) {
                ShellViewFactory.addMoshConnect(group);
            } else if (ShellPrototype.HTTP.equalsIgnoreCase(userData)) {
                ShellViewFactory.addHttpConnect(group);
            }
            this.closeWindow();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, this.getWindow());
        }
    }

    @Override
    public void onWindowShown(WindowEvent event) {
        super.onWindowShown(event);
        this.group = this.getProp("group");
        this.stage.hideOnEscape();
        this.searchField.requestFocus();
    }

    @Override
    public String getViewTitle() {
        return I18nHelper.addConnect();
    }
}
