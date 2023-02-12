package top.zenyoung.jfx.validator;

import com.google.common.collect.Sets;
import javafx.scene.Node;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * StyleClassDecoration provides decoration of nodes by setting / removing style classes.
 *
 * @author r.lichtenberger@synedra.com
 */
public class StyleClassDecoration implements Decoration {
    private final Set<String> styleClasses;

    /**
     * Create new StyleClassDecoration
     *
     * @param styleClasses The style classes to apply to the target node, if check fails
     * @throws IllegalArgumentException if styleClasses is null or empty.
     */
    public StyleClassDecoration(final String... styleClasses) {
        if (styleClasses == null || styleClasses.length == 0) {
            throw new IllegalArgumentException("At least one style class is required");
        }
        this.styleClasses = Sets.newHashSet(Arrays.asList(styleClasses));
    }

    @Override
    public void add(Node targetNode) {
        final List<String> styleClassList = targetNode.getStyleClass();
        final Set<String> toAdd = Sets.newHashSet(styleClasses);
        // don't add a style class that is already added.
        styleClassList.forEach(toAdd::remove);
        styleClassList.addAll(toAdd);
    }

    @Override
    public void remove(@Nonnull final Node targetNode) {
        targetNode.getStyleClass().removeAll(styleClasses);
    }
}
