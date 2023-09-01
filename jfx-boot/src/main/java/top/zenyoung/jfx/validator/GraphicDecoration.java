package top.zenyoung.jfx.validator;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.transform.Transform;

import javax.annotation.Nonnull;

/**
 * GraphicDecoration provides decoration of nodes by overlaying them with another node.
 *
 * @author r.lichtenberger@synedra.com
 */
public class GraphicDecoration implements Decoration {
    private Node decoratedNode;
    private final Node decorationNode;
    private final Pos pos;
    private final double xOffset;
    private final double yOffset;

    private final ChangeListener<Boolean> layoutListener;
    private final ChangeListener<Transform> transformListener;

    private ChangeListener<Scene> sceneChangedListener;
    private GraphicDecorationStackPane stack;

    /**
     * Create GraphicDecoration that will be overlayed in the top-left corner
     *
     * @param decorationNode The node to overlay over the decorated node
     */
    public GraphicDecoration(@Nonnull final Node decorationNode) {
        this(decorationNode, Pos.TOP_LEFT);
    }

    /**
     * Create GraphicDecoration
     *
     * @param decorationNode The node to overlay over the decorated node
     * @param position       The location of the overlay
     */
    public GraphicDecoration(@Nonnull final Node decorationNode, @Nonnull final Pos position) {
        this(decorationNode, position, 0, 0);
    }

    /**
     * Create GraphicDecoration
     *
     * @param decorationNode The node to overlay over the decorated node
     * @param position       The location of the overlay
     * @param xOffset        Horizontal offset of overlay (with respect to position)
     * @param yOffset        Vertical offset of overlay (with respect to position)
     */
    public GraphicDecoration(@Nonnull final Node decorationNode, @Nonnull final Pos position, final double xOffset, final double yOffset) {
        this.decorationNode = decorationNode;
        this.decorationNode.setManaged(false);
        this.pos = position;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        layoutListener = (observable, oldValue, newValue) -> layoutGraphic();
        transformListener = (observable, oldValue, newValue) -> layoutGraphic();
    }

    /**
     * Manually trigger an update for decorations in scene.
     * <p>
     * You should not need to call this method, because ValidatorFX should detect the need to update graphic validations.
     * If you do need to call this method, consider filing an issue at https://github.com/effad/ValidatorFX/issues
     *
     * @param node A node in the scene to update decorations for
     */
    public static void updateDecorations(@Nonnull final Node node) {
        final GraphicDecorationStackPane decorationPane = findDecorationPane(node);
        if (decorationPane != null) {
            decorationPane.requestLayout(); // will trigger the layoutListener for all graphic decorations in scene
        }
    }

    @Override
    public void add(@Nonnull final Node target) {
        decoratedNode = target;
        withStack(() -> {
            if (decoratedNode != null) {    // #10: could have been removed again already ...
                setListener();
                layoutGraphic();
            }
        }, target);
    }


    @Override
    public void remove(@Nonnull final Node target) {
        if (stack != null) {
            stack.getChildren().remove(decorationNode);
            stack.needsLayoutProperty().removeListener(layoutListener);
        }
        this.decoratedNode = null;
        target.localToSceneTransformProperty().removeListener(transformListener);
    }

    private void withStack(@Nonnull final Runnable code, @Nonnull final Node target) {
        stack = findDecorationPane(target);
        if (stack == null) {
            if (target.getScene() == null) {
                sceneChangedListener = (observable, oldValue, newValue) -> {
                    if (oldValue == null && newValue != null) {
                        target.sceneProperty().removeListener(sceneChangedListener);
                        setupStack(target);
                        code.run();
                    }
                };
                target.sceneProperty().addListener(sceneChangedListener);
            } else {
                setupStack(target);
                code.run();
            }
        } else {
            code.run();
        }
    }

    private static GraphicDecorationStackPane findDecorationPane(@Nonnull final Node node) {
        Node parent = node.getParent();
        while (parent != null && !(parent instanceof GraphicDecorationStackPane)) {
            parent = parent.getParent();
        }
        return (GraphicDecorationStackPane) parent;
    }

    private void setupStack(@Nonnull final Node target) {
        stack = new GraphicDecorationStackPane();
        final Parent oldRoot = target.getScene().getRoot();
        target.getScene().setRoot(stack);
        stack.getChildren().add(oldRoot);
    }

    private void setListener() {
        stack.needsLayoutProperty().removeListener(layoutListener);
        stack.needsLayoutProperty().addListener(layoutListener);
        decoratedNode.localToSceneTransformProperty().removeListener(transformListener);
        decoratedNode.localToSceneTransformProperty().addListener(transformListener);
    }


    private void layoutGraphic() {
        // Because we made decorationNode unmanaged, we are responsible for sizing it:
        decorationNode.autosize();
        // Now get decorationNode's layout Bounds and use for its position computations: 
        final Bounds decorationNodeLayoutBounds = decorationNode.getLayoutBounds();
        final double decorationNodeWidth = decorationNodeLayoutBounds.getWidth();
        final double decorationNodeHeight = decorationNodeLayoutBounds.getHeight();

        final Bounds targetBounds = decoratedNode.getLayoutBounds();
        double x = targetBounds.getMinX(), y = targetBounds.getMinY(),
                targetWidth = targetBounds.getWidth(), targetHeight = targetBounds.getHeight();
        if (targetWidth <= 0) {
            targetWidth = decoratedNode.prefWidth(-1);
        }
        if (targetHeight <= 0) {
            targetHeight = decoratedNode.prefHeight(-1);
        }
        switch (pos.getHpos()) {
            case CENTER:
                x += targetWidth / 2 - decorationNodeWidth / 2.0;
                break;
            case LEFT:
                x -= decorationNodeWidth / 2.0;
                break;
            case RIGHT:
                x += targetWidth - decorationNodeWidth / 2.0;
                break;
            default:
                break;
        }
        switch (pos.getVpos()) {
            case CENTER:
                y += targetHeight / 2 - decorationNodeHeight / 2.0;
                break;
            case TOP:
                y -= decorationNodeHeight / 2.0;
                break;
            case BOTTOM:
                y += targetHeight - decorationNodeHeight / 2.0;
                break;
            case BASELINE:
                y += decoratedNode.getBaselineOffset() - decorationNode.getBaselineOffset() - decorationNodeHeight / 2.0;
                break;
            default:
                break;
        }
        final Bounds sceneBounds = decoratedNode.localToScene(targetBounds), stackBounds = stack.sceneToLocal(sceneBounds);
        decorationNode.setLayoutX(Math.round(x + xOffset + stackBounds.getMinX()));
        decorationNode.setLayoutY(Math.round(y + yOffset + stackBounds.getMinY()));
        addOrRemoveDecorationNodeToStack();
    }

    private void addOrRemoveDecorationNodeToStack() {
        if (stack != null) {
            final boolean shouldBeThere = decoratedNode.getScene() != null && targetVisible();
            final boolean isThere = stack.getChildren().contains(decorationNode);
            if (shouldBeThere != isThere) {
                if (shouldBeThere) {
                    stack.getChildren().add(decorationNode);
                } else {
                    Platform.runLater(() -> stack.getChildren().remove(decorationNode));
                }
            }
        }
    }

    private boolean targetVisible() {
        Node node = decoratedNode;
        boolean visible = true;
        while (visible && node != null) {
            visible = node.isVisible();
            node = node.getParent();
        }
        return visible;
    }
}
