package cn.oyzh.jeditermfx.terminal.ui;

import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.i18n.I18nHelper;
import cn.oyzh.jeditermfx.terminal.SubstringFinder;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class JediTermFindComponent {

    private final HBox pane = new HBox();

    private final ClearableTextField textField = new ClearableTextField();
//    private final TextField textField = new TextField();

    private final Label label = new Label();

    private final CheckBox ignoreCaseCheckBox = new CheckBox(I18nHelper.ignoreCase());
//    private final CheckBox ignoreCaseCheckBox = new CheckBox("Ignore Case");

    public JediTermFindComponent(JediTermFxWidget jediTermWidget) {
        this.ignoreCaseCheckBox.setSelected(true);
        Button next = new Button("\u25BC");
        next.setOnAction(e -> onResultUpdated(jediTermWidget.getTerminalPanel().selectNextFindResultItem()));
        Button prev = new Button("\u25B2");
        prev.setOnAction(e -> onResultUpdated(jediTermWidget.getTerminalPanel().selectPrevFindResultItem()));
        Button close = new Button("\u2715");
        close.setOnAction(e -> jediTermWidget.hideFindComponent());
        var charSize = jediTermWidget.myTerminalPanel.myCharSize;
        this.textField.setPromptText(I18nHelper.pleaseInputContent());
        HBox.setHgrow(textField, Priority.ALWAYS);
        pane.setMaxSize(charSize.getWidth() * 60, 30);
//        pane.setMaxSize(
//                charSize.getWidth() * 60,
//                charSize.getHeight() + 14);
        pane.setAlignment(Pos.CENTER_LEFT);
//        pane.setPadding(new Insets(0, charSize.getWidth() / 2, 0, charSize.getWidth() / 2));
        pane.setStyle("-fx-background-color: -fx-background");
//        textField.setEditable(true);
        updateLabel(null);
        this.pane.getChildren().add(textField);
        this.pane.getChildren().add(ignoreCaseCheckBox);
//        HBox.setMargin(ignoreCaseCheckBox, new Insets(0, charSize.getWidth(), 0, charSize.getWidth()));
        HBox.setMargin(ignoreCaseCheckBox, new Insets(0, 5, 0, 5));
        HBox.setMargin(label, new Insets(0, 5, 0, 5));
        HBox.setMargin(next, new Insets(0, 0, 0, 5));
        HBox.setMargin(prev, new Insets(0, 0, 0, 3));
        HBox.setMargin(close, new Insets(0, 5, 0, 3));
        this.pane.getChildren().add(label);
        this.pane.getChildren().add(next);
        this.pane.getChildren().add(prev);
        this.pane.getChildren().add(close);
        this.pane.focusedProperty().addListener((ov, oldV, newV) -> {
            if (newV) {
                this.textField.requestFocus();
            }
        });
    }

    TextField getTextField() {
        return textField;
    }

    CheckBox getIgnoreCaseCheckBox() {
        return ignoreCaseCheckBox;
    }

    private void updateLabel(@Nullable SubstringFinder.FindResult result) {
        if (result == null) {
            label.setText("");
        } else if (!result.getItems().isEmpty()) {
            SubstringFinder.FindResult.FindItem selectedItem = result.selectedItem();
            label.setText(selectedItem.getIndex() + " of " + result.getItems().size());
        }
    }

    void onResultUpdated(SubstringFinder.FindResult results) {
        updateLabel(results);
    }

    @NotNull Pane getPane() {
        return this.pane;
    }

    void requestFocus() {
        textField.requestFocus();
    }
}
