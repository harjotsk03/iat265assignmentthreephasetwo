import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Ellipse2D.Double;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import processing.core.PVector;

public abstract class Orca extends OceanObject {

    protected PVector speed; // speed
    protected float speedMag; // speed limit
    protected float energy; // energy
    protected final float FULL_ENERGY = 800;
    protected float engGainRatio = 50; // Energy gained per food size unit
    protected float engLossRatio = FULL_ENERGY / (30 * 15); // Energy loss per frame
    protected float sizeGrowRatio = 0.0001f; // size growth ratio per extra energy unit

    protected Color color; // featured color
    protected Double head; // the original body
    protected Ellipse2D.Double eye; // the eye
    protected Polygon tail; // tail

    // FSM states
    protected int state;
    protected final int HUNGRY = 0;
    protected final int HALF_FULL = 1;
    protected final int FULL = 2;
    protected final int OVER_FULL = 3;
    protected final int SICK = 4;

    private Timer sickTimer; // Timer for the sick state

	private boolean isAlive = true;

    public Orca(float x, float y, int w, int h, float size) {
        super(x, y, w, h, size);
        speedMag = 5f;
        speed = Util.randomPVector(speedMag);
        state = HUNGRY;

        this.energy = FULL_ENERGY;
    }

    public void move() {
        if (state == SICK) {
            this.speed.div(2);
        }

        speed.normalize().mult(speedMag);

        // apply speed to position
        pos.add(speed);

        // lose energy
        energy -= engLossRatio;
    }

    public void approach(OceanObject target) {
        float coef = .3f;
        PVector direction = PVector.sub(target.getPos(), pos).normalize();
        PVector accel = PVector.mult(direction, speedMag * coef);
        speed.add(accel);
    }

    @Override
    public void update(ArrayList<OceanObject> objList, OceanPanel panel) {
        ArrayList<OceanObject> fList =filterTargetList(objList);
        traceBestFood(fList);
        checkCollision();
        move();

        if (this.state == SICK) {
            this.speedMag = 2f;
        }

        for (int i = 0; i < fList.size(); i++) {
            if (isColliding(fList.get(i))) {
                float foodSize = fList.get(i).getSize();
                energy += foodSize * engGainRatio;
                @SuppressWarnings("unused")
				String st = String.format("%s gains energy by %.2f units to %.2f", animalType(), foodSize * 100,
                        energy);

                if (state == OVER_FULL && size < 5) {
                    float extra = energy - FULL_ENERGY;
                    energy = FULL_ENERGY;
                    size += extra * sizeGrowRatio * size;
                    // st = String.format("%s grows by %.1f%% to %.2f%n", animalType(), energy * .01, size);
                    // panel.setStatus(st);
                    // System.out.println(st);
                }
                objList.remove(fList.get(i));
            }
        }
        if (energy > FULL_ENERGY) {
            state = OVER_FULL;
        } else if (energy == FULL_ENERGY) {
            state = FULL;
        } else if (energy > FULL_ENERGY / 2) {
            state = HALF_FULL;
        } else if (energy < FULL_ENERGY / 3) {
            if (state != SICK) {
                state = SICK;
                startSickTimer(); // Start the timer when entering the SICK state
            }
        } else {
            state = HUNGRY;
        }
    }

    private void startSickTimer() {
        // If the timer is already running, cancel it
        if (sickTimer != null) {
            sickTimer.cancel();
            sickTimer.purge();
        }

        // Create a new timer
        sickTimer = new Timer();
        sickTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                isAlive = false;
            }
        }, 3000);

    }

    private String animalType() {
        String type = "unknown animal";
        if (this instanceof SmartSuperOrca)
            type = "Smart Orca";
        else if (this instanceof SuperOrca)
            type = "Regular Orca";
        return type;
    }

	protected ArrayList<OceanObject> filterTargetList(ArrayList<OceanObject> fList) {
		ArrayList<OceanObject> list = new ArrayList<>();
		for (int i = 0; i < fList.size(); i++) {
			OceanObject f = fList.get(i);
			if (eatable(f)) {
				list.add(f);
			}
		}
		return list;
	}
	

    public void drawInfo(Graphics2D g) {
        AffineTransform at = g.getTransform();
        g.translate(pos.x, pos.y);

        String st1 = "Size     : " + String.format("%.2f", size);
        String st2 = "Speed  : " + String.format("%.2f", speed.mag());
        String st3 = "Energy : " + String.format("%.2f", energy);

        Font f = new Font("Courier", Font.PLAIN, 12);
        FontMetrics metrics = g.getFontMetrics(f);

        float textWidth = metrics.stringWidth(st3);
        float textHeight = metrics.getHeight();
        float margin = 12, spacing = 6;

        g.setColor(new Color(255, 255, 255, 60));
        g.fillRect((int) (-textWidth / 2 - margin),
                (int) (-dim.height * size * .75f - textHeight * 5f - spacing * 4f - margin * 2f),
                (int) (textWidth + margin * 2f), (int) (textHeight * 5f + spacing * 4f + margin * 2f));

        g.setColor(Color.blue.darker());
        g.drawString(this.animalType(), -metrics.stringWidth(this.animalType()) / 2,
                -dim.height * size * .75f - margin - (textHeight + spacing) * 4f);
        g.setColor(Color.black);
        g.drawString(st1, -textWidth / 2, -dim.height * size * .75f - margin - (textHeight + spacing) * 2f);
        g.drawString(st2, -textWidth / 2, -dim.height * size * .75f - margin - (textHeight + spacing) * 1f);
        g.drawString(st3, -textWidth / 2, -dim.height * size * .75f - margin);

        g.setTransform(at);
    }

    public void checkCollision() {
        float coef = 50f;

        Line2D.Double TOP_LINE = new Line2D.Double(0, 50, OceanPanel.PAN_SIZE.width, 50);
        Line2D.Double BTM_LINE = new Line2D.Double(0, OceanPanel.PAN_SIZE.height - 70, OceanPanel.PAN_SIZE.width,
                OceanPanel.PAN_SIZE.height - 70);
        Line2D.Double LFT_LINE = new Line2D.Double(50, 0, 50, OceanPanel.PAN_SIZE.height);
        Line2D.Double RGT_LINE = new Line2D.Double(OceanPanel.PAN_SIZE.width - 50, 0, OceanPanel.PAN_SIZE.width - 50,
                OceanPanel.PAN_SIZE.height);

        double top_dist = TOP_LINE.ptLineDist(pos.x, pos.y) - getBoundingBox().getHeight() / 2;
        double btm_dist = BTM_LINE.ptLineDist(pos.x, pos.y) - getBoundingBox().getHeight() / 2;
        double lft_dist = LFT_LINE.ptLineDist(pos.x, pos.y) - getBoundingBox().getWidth() / 2;
        double rgt_dist = RGT_LINE.ptLineDist(pos.x, pos.y) - getBoundingBox().getWidth() / 2;

        PVector top_f = new PVector(0, 1).mult((float) (coef / Math.pow(top_dist, 2f)));
        PVector btm_f = new PVector(0, -1).mult((float) (coef / Math.pow(btm_dist, 2f)));
        PVector lft_f = new PVector(1, 0).mult((float) (coef / Math.pow(lft_dist, 2f)));
        PVector rgt_f = new PVector(-1, 0).mult((float) (coef / Math.pow(rgt_dist, 2f)));

        speed.add(top_f).add(btm_f).add(lft_f).add(rgt_f);
    }

	public boolean getIsAlive(){
		return isAlive;
	}

    protected abstract void traceBestFood(ArrayList<OceanObject> fList);

    protected abstract boolean eatable(OceanObject food);
}
