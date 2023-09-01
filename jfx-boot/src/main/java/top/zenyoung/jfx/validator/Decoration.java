package top.zenyoung.jfx.validator;

import javafx.scene.Node;

/** Decoration is the common interface of the various decoration strategies.
 * @author r.lichtenberger@synedra.com
 */
public interface Decoration {

	/** Add a decoration to the given target node
	 * @param target The node to decorate
	 */
	void add(final Node target);

	/** Remove a decoration from the given target node
	 * @param target The node to remove decoration from
	 */
	void remove(final Node target);
}
