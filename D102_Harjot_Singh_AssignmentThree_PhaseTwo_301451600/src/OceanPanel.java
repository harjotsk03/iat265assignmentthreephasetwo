import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.JPanel;

public class OceanPanel extends JPanel implements ActionListener {

    private static final long serialVersionUID = 1L;
    private ArrayList<OceanObject> objList;
    private Hunter hunter;
	private Ocean ocean;
    private javax.swing.Timer t;
    private int orcaNum = 6;
    private int MAX_FOOD = orcaNum * 2;
    private String status = "status...";
    private boolean showInfo = false;

    private Timer timer;
    private TimerTask timerTask;
    private boolean timerRunning = false;

    public final static Dimension PAN_SIZE = new Dimension(1200, 700);

    public OceanPanel() {
        super();
        this.setPreferredSize(PAN_SIZE);

        this.objList = new ArrayList<>();

        for (int i = 0; i < orcaNum; i++) {
            this.addSmartSuperOrca();
        }

        for (int i = 0; i < MAX_FOOD; i++)
            objList.add(Util.randomFish(this));

        hunter = new Hunter(100, 100, 5, 3, 1);
        hunter.setActive(false);

		ocean = new Ocean(PAN_SIZE);

        t = new javax.swing.Timer(33, this);
        t.start();

        addKeyListener(new MyKeyAdapter());
        setFocusable(true);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        setBackground(Color.darkGray);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		ocean.drawMe(g2);
        // Draw objects
        for (int i = 0; i < objList.size(); i++) {
            OceanObject obj = objList.get(i);
            obj.draw(g2);
            if (showInfo) {
                obj.drawInfo(g2);
            }
        }        

        // Draw hunter and its info
        if (hunter.active) {
            hunter.draw(g2);
            if (showInfo) hunter.drawInfo(g2);
        }


        drawStatusBar(g2);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        for (int i = 0; i < objList.size(); i++)
            objList.get(i).update(objList, this);

        int numFish = Util.filteredFishList(objList).size() / 2;

		String st = String.format("%d fish must die before hunter appears", numFish);
		setStatus(st);

        int numOrca = Util.filteredOrcaList(objList).size() / 2;

        if (Util.countFood(objList) < MAX_FOOD / 2) {
            hunter.setActive(true);
			st = String.format("Hunter Activated, press space to shoot! You must kill %d more Orcas", numOrca);
			setStatus(st);
        }

        if (hunter.active) {
            hunter.update(objList, this);
        }

        if (Util.countOrca(objList) <= orcaNum / 2) {
            hunter.setActive(false);
			st = String.format("Hunter Deactivated");
			setStatus(st);
        }

        if (Util.countOrca(objList) <= orcaNum / 2 && !timerRunning) {
            startTimer();
        }

        for( int i = 0 ; i < objList.size() ; i++){
            OceanObject current = objList.get(i);

            if(current instanceof SmartSuperOrca && !current.getIsAlive()){
                objList.remove(i);
            }
        }

        for( int i = 0 ; i < objList.size() ; i++){
            OceanObject current = objList.get(i);

            if(current instanceof Fish && !current.getIsAlive()){
                objList.remove(i);
            }
        }

        repaint();
    }

    private void startTimer() {
        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                restoreOcean();
                timerRunning = false;
            }
        };
        timer.schedule(timerTask, 5000);  // Schedule the task for 5 seconds later
        timerRunning = true;  // Set the timerRunning flag to true
    }

    private class MyKeyAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_SPACE && hunter != null) {
                hunter.fire();
            }

            if (e.getKeyCode() == KeyEvent.VK_UP)
                t.setDelay(t.getDelay() / 2);
            if (e.getKeyCode() == KeyEvent.VK_DOWN)
                t.setDelay(t.getDelay() * 2);

            if (e.getKeyCode() == KeyEvent.VK_D) {
                showInfo = !showInfo;
            }
        }
    }

    private void drawStatusBar(Graphics2D g) {
        Font f = new Font("Arial", Font.BOLD, 12);
        g.setFont(f);
        g.setColor(Color.LIGHT_GRAY);
        g.fillRect(0, getSize().height - 24, getSize().width, 24);
        g.setColor(Color.BLACK);
        g.drawString(status, 12, getSize().height - 8);
    }

    public void setStatus(String st) {
        this.status = st;
    }

    public void addSmartSuperOrca() {
        float x = Util.random(PAN_SIZE.width);
        float y = Util.random(PAN_SIZE.height);
        float size = Util.random(2f, 3.5f);
        this.objList.add(new SmartSuperOrca(x, y, size));
    }

	private void restoreOcean(){
		ArrayList<OceanObject> orcaList = Util.filteredOrcaList(objList);

		int neededOrcas = orcaList.size();
		

        for (int i = 0; i < orcaNum - neededOrcas; i++) {
            this.addSmartSuperOrca();
        }

		ArrayList<OceanObject> oList = Util.filteredFishList(objList);

		int neededFish = oList.size();

        for (int i = 0; i < MAX_FOOD - neededFish  ; i++)
            objList.add(Util.randomFish(this));
	}
}
