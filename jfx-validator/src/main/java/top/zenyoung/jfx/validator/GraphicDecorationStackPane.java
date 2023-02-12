package top.zenyoung.jfx.validator;

import javafx.scene.layout.StackPane;

/** GraphicDecorationStackPane serves as stack for overlaying decoration nodes in GraphicDecoration.
 * @author r.lichtenberger@synedra.com
 */
public class GraphicDecorationStackPane extends StackPane {
	
	public GraphicDecorationStackPane() {
		super();
		setId("graphic validator overlay stack");
		setStyle("-fx-background-color: rgba(0, 0, 0, 0);");
	}	
}
