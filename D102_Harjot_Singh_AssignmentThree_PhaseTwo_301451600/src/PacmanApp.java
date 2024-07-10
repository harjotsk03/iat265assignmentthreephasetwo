
import javax.swing.JFrame;

public class PacmanApp extends JFrame {

	private static final long serialVersionUID = 6457792220456140992L;

	public PacmanApp(String title) {
		super(title);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		// instantiating our BallPanel
		OceanPanel panel = new OceanPanel();
		
		// adding it to the current frame
		this.add(panel);
		this.pack();
		
		// displaying the frame
		this.setVisible(true);
	}

	public static void main(String[] args) {
		new PacmanApp("My Interactive Pacman App");
	}

}
