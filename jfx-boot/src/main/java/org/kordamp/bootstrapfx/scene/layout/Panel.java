package org.kordamp.bootstrapfx.scene.layout;

import javafx.beans.DefaultProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.util.Optional;

/**
 * Panel
 *
 * @author young
 */
@DefaultProperty("body")
public class Panel extends BorderPane {
    private ObjectProperty<Node> heading;
    private ObjectProperty<Node> body;
    private ObjectProperty<Node> footer;

    /**
     * 构造函数
     */
    public Panel() {
        getStyleClass().setAll("panel");
    }

    /**
     * 构造函数
     *
     * @param title 标题
     */
    public Panel(final String title) {
        this();
        setText(title);
    }

    public final ObjectProperty<Node> headingProperty() {
        if (this.heading == null) {
            this.heading = new SimpleObjectProperty<>(this, "heading");
            this.heading.addListener((v, o, n) -> {
                if (n != null) {
                    final GridPane box = new GridPane();
                    box.getStyleClass().setAll("panel-heading");
                    GridPane.setColumnIndex(n, 0);
                    GridPane.setRowIndex(n, 0);
                    GridPane.setHgrow(n, Priority.ALWAYS);
                    GridPane.setVgrow(n, Priority.ALWAYS);
                    box.getChildren().add(n);
                    setTop(box);
                }
            });
        }
        return this.heading;
    }

    public final void setHeading(final Node content) {
        headingProperty().set(content);
    }

    public final Node getHeading() {
        return Optional.ofNullable(heading)
                .map(ObjectProperty::get)
                .orElse(null);
    }

    public final ObjectProperty<Node> bodyProperty() {
        if (this.body == null) {
            this.body = new SimpleObjectProperty<>(this, "body");
            this.body.addListener((v, o, n) -> {
                if (n != null) {
                    final GridPane box = new GridPane();
                    box.getStyleClass().setAll("panel-body");
                    GridPane.setColumnIndex(n, 0);
                    GridPane.setRowIndex(n, 0);
                    GridPane.setHgrow(n, Priority.ALWAYS);
                    GridPane.setVgrow(n, Priority.ALWAYS);
                    box.getChildren().add(n);
                    setCenter(box);
                }
            });
        }
        return this.body;
    }

    public final void setBody(final Node body) {
        bodyProperty().set(body);
    }

    public final Node getBody() {
        return Optional.ofNullable(body)
                .map(ObjectProperty::get)
                .orElse(null);
    }

    public final ObjectProperty<Node> footerProperty() {
        if (this.footer == null) {
            this.footer = new SimpleObjectProperty<>(this, "footer");
            this.footer.addListener((v, o, n) -> {
                if (n != null) {
                    final GridPane box = new GridPane();
                    box.getStyleClass().setAll("panel-footer");
                    GridPane.setColumnIndex(n, 0);
                    GridPane.setRowIndex(n, 0);
                    GridPane.setHgrow(n, Priority.ALWAYS);
                    GridPane.setVgrow(n, Priority.ALWAYS);
                    box.getChildren().add(n);
                    setBottom(box);
                }
            });
        }
        return this.footer;
    }

    public final void setFooter(final Node content) {
        footerProperty().set(content);
    }

    public final Node getFooter() {
        return Optional.ofNullable(footer)
                .map(ObjectProperty::get)
                .orElse(null);
    }

    public void setText(final String title) {
        final Label label = new Label(title);
        label.getStyleClass().add("panel-title");
        headingProperty().set(label);
    }

    public String getText() {
        final Node node = headingProperty().get();
        if (node instanceof Labeled) {
            return ((Labeled) node).getText();
        }
        return null;
    }
}
