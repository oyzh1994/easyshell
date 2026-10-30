package cn.oyzh.easyshell.llm.ui;

import cn.oyzh.easyshell.llm.LLMMessage;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;

import java.io.File;

public final class LLMMessageCell extends ListCell<LLMMessage> {

    private final HBox row = new HBox();

    private final VBox bubbleStack = new VBox(4);

    private final VBox bubble = new VBox(5);

    private final Label text = new Label();

    private final Label attachment = new Label();

    private final Label meta = new Label();

    private LLMMessageCell() {
        this.getStyleClass().add("llm-message-cell");
        this.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        this.setPadding(Insets.EMPTY);

        this.row.getStyleClass().add("llm-message-row");
        this.row.setAlignment(Pos.CENTER_LEFT);
        this.row.setMinWidth(0);
        this.row.prefWidthProperty().bind(Bindings.createDoubleBinding(
                () -> Math.max(0, this.getWidth() - this.getInsets().getLeft() - this.getInsets().getRight()),
                this.widthProperty(), this.insetsProperty()));
        this.row.maxWidthProperty().bind(this.row.prefWidthProperty());

        this.bubbleStack.getStyleClass().add("llm-bubble-stack");
        this.bubbleStack.setFillWidth(true);

        this.bubble.getStyleClass().add("llm-bubble");
        this.bubble.setFillWidth(true);
        this.bubble.maxWidthProperty().bind(this.row.widthProperty().multiply(0.78));

        this.text.getStyleClass().add("llm-bubble-text");
        this.text.setWrapText(true);
        this.text.setMaxWidth(Double.MAX_VALUE);
        this.text.setMinWidth(0);
        this.text.maxWidthProperty().bind(Bindings.max(40, this.bubble.maxWidthProperty().subtract(28)));
        this.text.setTextAlignment(TextAlignment.LEFT);

        this.attachment.getStyleClass().add("llm-bubble-attachment");
        this.attachment.setWrapText(true);
        this.attachment.setMaxWidth(Double.MAX_VALUE);
        this.attachment.setMinWidth(0);
        this.attachment.maxWidthProperty().bind(Bindings.max(40, this.bubble.maxWidthProperty().subtract(28)));

        this.meta.getStyleClass().add("llm-meta");
        this.meta.setMaxWidth(Double.MAX_VALUE);

        this.bubble.getChildren().addAll(this.text, this.attachment);
        this.bubbleStack.getChildren().addAll(this.bubble, this.meta);
        this.row.getChildren().add(this.bubbleStack);
        this.setGraphic(this.row);
    }

    @Override
    protected void updateItem(LLMMessage item, boolean empty) {
        super.updateItem(item, empty);
        if (this.text.textProperty().isBound()) {
            this.text.textProperty().unbind();
        }
        if (empty || item == null) {
            this.setGraphic(null);
            return;
        }

        this.setGraphic(this.row);
        boolean user = item.isUser();
        this.row.setAlignment(user ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        this.row.getStyleClass().removeAll("llm-message-row-user", "llm-message-row-llm");
        this.row.getStyleClass().add(user ? "llm-message-row-user" : "llm-message-row-llm");

        this.bubble.getStyleClass().removeAll("llm-bubble-user", "llm-bubble-assistant");
        this.bubble.getStyleClass().add(user ? "llm-bubble-user" : "llm-bubble-assistant");

        this.text.getStyleClass().removeAll("llm-bubble-text-user", "llm-bubble-text-assistant");
        this.text.getStyleClass().add(user ? "llm-bubble-text-user" : "llm-bubble-text-assistant");
        this.text.textProperty().bind(item.messageProperty());

        File file = item.getFile();
        boolean hasFile = file != null;
        this.attachment.setVisible(hasFile);
        this.attachment.setManaged(hasFile);
        this.attachment.getStyleClass().removeAll("llm-bubble-attachment-user", "llm-bubble-attachment-assistant");
        if (hasFile) {
            this.attachment.setText("附件: " + file.getName());
            this.attachment.getStyleClass().add(user ? "llm-bubble-attachment-user" : "llm-bubble-attachment-assistant");
        }

        String model = item.getModel();
        String prefix = user ? "" : (model == null || model.isBlank() ? "AI" : model) + " · ";
        this.meta.setText(prefix + item.getTimeText());
        this.meta.setAlignment(user ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
    }
}