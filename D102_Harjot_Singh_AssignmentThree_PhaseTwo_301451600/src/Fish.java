import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import processing.core.PVector;

public class Fish extends OceanObject {

    private GeneralPath foodShape; // geometric shape
    private Color foodColor; // shape color
    private PVector speed;
    private float currentAngle;
    protected float speedMag;
    private static final float MAX_SPEED = 5.0f; // Maximum speed limit

    protected float energy; // energy
    protected final float FULL_ENERGY = 300;
    protected float engGainRatio = 50; // Energy gained per food size unit
    protected float engLossRatio = FULL_ENERGY / (30 * 15); // Energy loss per frame
    protected float sizeGrowRatio = 0.0001f; // size growth ratio per extra energy unit

    protected int state;
    protected final int HUNGRY = 0;
    protected final int HALF_FULL = 1;
    protected final int FULL = 2;
    protected final int OVER_FULL = 3;
    protected final int SICK = 4;


    private Timer sickTimer;

	private boolean isAlive = true;

    public Fish(float x, float y, float size, float speedx, float speedy, Color color) {
        super(x, y, 10, 10, size);
        this.foodColor = color;
        this.speed = new PVector(speedx, speedy);
        this.currentAngle = speed.heading();

        speedMag = 3f;

        state = HUNGRY;

        this.energy = FULL_ENERGY;

        setShapeAttributes(); // Make sure to call this method to set shape attributes
        setOutline(); // Make sure to call this method to set outline
    }

    @Override
    public void draw(Graphics2D g) {
        AffineTransform at = g.getTransform();

        g.translate(pos.x, pos.y);
        g.scale(size, size);
        g.rotate(currentAngle);

        // draw food


        if (state == SICK) {
            g.setColor(Color.green);
            g.fill(foodShape);
        } else {
            g.setColor(foodColor);
            g.fill(foodShape);
        }

        g.setTransform(at);

        // g.setColor(Color.orange);
        // g.draw(getFOV());

    }

    @Override
    protected void setShapeAttributes() {
        double width = size * 2;
        double height = size;

        Ellipse2D.Double body = new Ellipse2D.Double(-width / 2, -height / 2, width, height);

        GeneralPath tail = new GeneralPath();
        tail.moveTo(-width / 2, 0);
        tail.lineTo(-width / 2 - width / 4, - height / 4);
        tail.lineTo(-width / 2 - width / 4, height / 4);
        tail.closePath();

        GeneralPath fish = new GeneralPath();
        fish.append(body, false);
        fish.append(tail, false);

        this.foodShape = fish;

		FOV = new Ellipse2D.Double(-45, -45, 90, 90);
    }

    @Override
    public void setOutline() {
        outline = new Area(foodShape);

        FOVarea = new Area(FOV);

    }

    @Override
    public Shape getOutline() {
        AffineTransform at = new AffineTransform();
        at.translate(pos.x, pos.y);
        at.scale(size, size);
        return at.createTransformedShape(outline);
    }

    @Override
    public void update(ArrayList<OceanObject> objList, OceanPanel panel) {
        this.checkWalls(panel);
        this.move(panel);

        if (this.state == SICK) {
            this.speedMag = 1f;
        }

		this.escapeMode(objList);

        checkCollisionBetween(objList);

        if (energy > FULL_ENERGY) {
            state = OVER_FULL;
        } else if (energy == FULL_ENERGY) {
            state = FULL;
        } else if (energy > FULL_ENERGY / 2) {
            state = HALF_FULL;
        } else if (energy < FULL_ENERGY / 3) {
            if (state != SICK) {
                state = SICK;
                startSickTimer();
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
        System.out.println("Timer started");
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
        g.drawString("Fish", -metrics.stringWidth("Fish") / 2,
                -dim.height * size * .75f - margin - (textHeight + spacing) * 4f);
        g.setColor(Color.black);
        g.drawString(st1, -textWidth / 2, -dim.height * size * .75f - margin - (textHeight + spacing) * 2f);
        g.drawString(st2, -textWidth / 2, -dim.height * size * .75f - margin - (textHeight + spacing) * 1f);
        g.drawString(st3, -textWidth / 2, -dim.height * size * .75f - margin);

        g.setTransform(at);
    }

    private void move(OceanPanel panel) {
        PVector desiredVelocity = speed.copy();
        PVector steering = desiredVelocity.sub(speed).limit(0.1f);  // Steering force to limit the change in direction
        speed.add(steering);
        
        
        if (speed.mag() > MAX_SPEED) {
            speed.normalize().mult(MAX_SPEED);
        }
        
        
        this.pos.add(speed);

        energy -= engLossRatio;

        // Calculate the target angle based on the current speed
        float targetAngle = speed.heading();
        
        // Smoothly interpolate the current angle towards the target angle
        float angleDifference = targetAngle - currentAngle;
        if (angleDifference > Math.PI) angleDifference -= Math.PI * 2;
        if (angleDifference < -Math.PI) angleDifference += Math.PI * 2;
        currentAngle += angleDifference * 0.3;  // Adjust the smoothing factor (0.1) as needed
    }

    protected void checkWalls(OceanPanel panel) {
        if (this.pos.x < 100) {
            this.pos.x = 100;
            this.speed.x *= -1;
        } else if (this.pos.x > panel.getWidth() - 100) {
            this.pos.x = panel.getWidth() - 100;
            this.speed.x *= -1;
        }
        if (this.pos.y < 100) {
            this.pos.y = 100;
            this.speed.y *= -1;
        } else if (this.pos.y > panel.getHeight() - 100) {
            this.pos.y = panel.getHeight() - 100;
            this.speed.y *= -1;
        }
    }

    @Override
    protected Shape getFOV() {
        AffineTransform at = new AffineTransform();
		at.translate(pos.x, pos.y);
		at.rotate(speed.heading());
		at.scale(size, size);
		return at.createTransformedShape(FOVarea);
    }

    @Override
    protected float getEnergy() {
        throw new UnsupportedOperationException("Unimplemented method 'getEnergy'");
    }

    private void checkCollisionBetween(ArrayList<OceanObject> objList) {
        ArrayList<OceanObject> oList = Util.filteredFishList(objList);

        for (int i = 0; i < oList.size(); i++) {
            for (int j = i + 1; j < oList.size(); j++) {
                OceanObject first = oList.get(i);
                OceanObject second = oList.get(j);

                if ((first.getOutline().intersects(second.getBoundingBox()) &&
                        second.getOutline().intersects(first.getBoundingBox()))) {
                    if (first.getSize() < second.getSize()) {
                        float coef = 0.3f; // coefficient of acceleration relative to maxSpeed
                        PVector direction = PVector.sub(first.getPos(), second.getPos()).normalize(); // reverse the direction
                        PVector accel = PVector.mult(direction, speedMag * coef);
                        first.setSpeed(accel);
                    } else {
                        float coef = 0.3f; // coefficient of acceleration relative to maxSpeed
                        PVector direction = PVector.sub(second.getPos(), first.getPos()).normalize(); // reverse the direction
                        PVector accel = PVector.mult(direction, speedMag * coef);
                        second.setSpeed(accel);
                    }
                }
            }
        }
    }

    @Override
    protected void setSpeed(PVector accel) {
        this.speed.add(accel);
    }

    protected PVector getSpeed(){
        return speed;
    }

    private void escapeMode(ArrayList<OceanObject> objList) {
        ArrayList<OceanObject> oList = Util.filteredOrcaList(objList);
    
        for (int i = 0; i < oList.size(); i++) {
            SmartSuperOrca orca = (SmartSuperOrca) oList.get(i);
    
            if (this.getFOV().intersects(orca.getBoundingBox())) {
                this.speed.mult(2);
    
                PVector directionAway = PVector.sub(this.pos, orca.getPos()).normalize();
    
                if(state != SICK){
                    this.speedMag = 5f;
                }else if( state == SICK){
                    this.speedMag = 1f;
                }
                
                this.speed = directionAway.mult(this.speed.mag());
    
                this.currentAngle = this.speed.heading();
            }
        }
    }
    
    @Override
    protected void fire() {
        throw new UnsupportedOperationException("Unimplemented method 'fire'");
    }

    @Override
	protected boolean getIsAlive(){
		return isAlive;
	}
}
