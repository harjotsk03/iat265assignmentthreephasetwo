import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;

public class Ocean {
    private Dimension panelSize;
    private Color bgColor = new Color(0,150,187);
    private Color sandColor = new Color(236,204,162);

    public Ocean(Dimension panel){
        this.panelSize = panel;
    }

    public void drawMe(Graphics2D g2){
        g2.setColor(bgColor);
        g2.fillRect(50, 50, (int) panelSize.getWidth() - 100, (int) panelSize.getHeight() - 120);
        
        g2.setColor(sandColor);
        g2.fillRect(50, (int) panelSize.getHeight() - 140, (int) panelSize.getWidth() - 100, (int) panelSize.getHeight() - 120);

        g2.setColor(Color.white);
        g2.fillRect(0, 0, 50, (int) panelSize.getHeight());
        g2.fillRect((int) panelSize.getWidth() - 50, 0, 50, (int) panelSize.getHeight());
        g2.fillRect(0, 0, (int) panelSize.getWidth(), 50);
        g2.fillRect(0, (int) panelSize.getHeight() - 70, (int) panelSize.getWidth(), 50);
    }
}
