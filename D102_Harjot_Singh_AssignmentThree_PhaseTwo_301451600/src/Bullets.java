import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.geom.RoundRectangle2D.Double;
import java.util.ArrayList;

import processing.core.PVector;

public class Bullets extends OceanObject {

    private static final float MAX_SPEED = 10.0f;
    private PVector speed;
    private float currentAngle;
    protected Double body;
    public boolean didHit = false;
    @SuppressWarnings("unused")
    private int targetIndexReturn;
    private Color gold = new Color(255,215,0);

    public Bullets(float x, float y, int w, int h, float size, PVector speed) {
        super(x, y, w, h, size);
        this.speed = speed;
        this.currentAngle = speed.heading();
    }

    @Override
    public void update(ArrayList<OceanObject> objList, OceanPanel panel) {
        this.move();
        ArrayList<OceanObject> fList = Util.filteredOrcaList(objList);
        traceBestFood(fList);

    }

    public boolean checkWalls(OceanPanel panel) {
        return this.pos.x > panel.getWidth() - 50 || this.pos.y > panel.getHeight() - 70 || this.pos.x < 50 || this.pos.y < 50;
    }
  
    private void move() {
        this.pos.add(speed);
        float targetAngle = speed.heading();
        float angleDifference = targetAngle - currentAngle;
        if (angleDifference > Math.PI) angleDifference -= Math.PI * 2;
        if (angleDifference < -Math.PI) angleDifference += Math.PI * 2;
        currentAngle += angleDifference * 0.3;
    }

    @Override
    public void draw(Graphics2D g2) {
        AffineTransform at = g2.getTransform();
        g2.translate(pos.x, pos.y);
        g2.scale(size, size);
        g2.rotate(currentAngle);
        g2.setColor(gold);
        g2.fill(body);
        g2.setTransform(at);
    }

    @Override
    public void drawInfo(Graphics2D g2) {
        // Not implemented
    }

    @Override
    protected void setShapeAttributes() {
        body = new RoundRectangle2D.Double(-5, -5, 15, 7, 20, 20);
    }

    @Override
    protected void setOutline() {
        outline = new Area(body);
    }

    @Override
    protected Shape getOutline() {
        AffineTransform at = new AffineTransform();
        at.translate(pos.x, pos.y);
        at.scale(size, size);
        return at.createTransformedShape(outline);
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
        return 0;
    }

    @Override
    protected void setSpeed(PVector accel) {
        // No implementation needed for now
    }

    @Override
    protected void fire() {
        // No implementation needed for now
    }

    protected void traceBestFood(ArrayList<OceanObject> fList) {
        if (fList.size() > 0) {
            OceanObject target = fList.get(0);
            float targetAttraction = this.getAttraction(target);
           
            for (int i = 0; i < fList.size(); i++) {
                OceanObject f = fList.get(i);
                if (this.getAttraction(f) > targetAttraction) {
                    target = f;
                    targetAttraction = this.getAttraction(target);
                }
            }
    
            this.setDirectionTowards(target);
    
            if (getOutline().intersects(target.getBoundingBox()) &&
                target.getOutline().intersects(getBoundingBox())) {
                this.didHit = true;
            }
        }
    }
    

    private float getAttraction(OceanObject target) {
        return (target.getEnergy());
    }

    private void setDirectionTowards(OceanObject target) {
        PVector direction = PVector.sub(target.getPos(), this.pos).normalize().mult(MAX_SPEED);
        this.speed = direction;
        this.currentAngle = this.speed.heading();
    }

    @Override
    protected PVector getSpeed() {
        return speed;
    }

    @Override
    protected boolean getIsAlive() {
        throw new UnsupportedOperationException("method 'getIsAlive' not needed");
    }
}
