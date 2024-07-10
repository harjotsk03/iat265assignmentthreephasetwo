import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.geom.RoundRectangle2D.Double;
import java.util.ArrayList;

import processing.core.PVector;

public class Hunter extends OceanObject{

    protected PVector speed;
    protected Ellipse2D.Double head;
    protected Double body;
    protected Double legs;
    protected Double feet;
    protected Double airTank;

    private float currentAngle;
    public ArrayList<Bullets> bullets;
    private long lastFiredTime; // Time of the last shot fired
    private static final long FIRE_INTERVAL = 1000000000L;
    public boolean active;
    public int health = 100;

    private Color skincolor = new Color(224,172,105);

    public Hunter(float x, float y, int w, int h, float size) {
        super(x, y, w, h, size);
        this.speed = new PVector(Util.random(0, 0), Util.random(2, 3));
        this.currentAngle = speed.heading();
        this.bullets = new ArrayList<>();
        this.lastFiredTime = 0; // Initialize last fired time
    }

    @Override
    public void draw(Graphics2D g2) {
        AffineTransform at = g2.getTransform();
        g2.translate(pos.x, pos.y);
        g2.scale(size, size);
        g2.setColor(skincolor);
        g2.fill(head);
        g2.setColor(Color.blue.darker().darker());
        g2.fill(body);
        g2.setColor(Color.orange);
        g2.fill(airTank);
        g2.setColor(Color.orange);
        g2.fill(feet);
        g2.setColor(Color.blue.darker().darker());
        g2.fill(legs);
        
        g2.setTransform(at);

        if (this.bullets.size() > 0) {
            for (int i = 0; i < bullets.size(); i++) {
                Bullets bullet = bullets.get(i);
                bullet.draw(g2);
            }            
        }
    }

    private void move() {
        this.pos.add(speed);
    }

    @Override
    public void drawInfo(Graphics2D g) {
        AffineTransform at = g.getTransform();
        g.translate(pos.x, pos.y);
    
        String st1 = "Health     : " + String.format("%d", health);
    
        Font f = new Font("Courier", Font.PLAIN, 12);
        FontMetrics metrics = g.getFontMetrics(f);
    
        float textWidth = metrics.stringWidth(st1);
        float textHeight = metrics.getHeight();
        float margin = 6, spacing = 6;
    
        float rectHeight = textHeight * 3 + spacing * 3 + margin * 2; // Adjust height calculation here
    
        g.setColor(new Color(255, 255, 255, 60));
        g.fillRect((int) (-textWidth / 2 - margin),
                (int) (-dim.height * size * .75f - rectHeight),
                (int) (textWidth + margin * 2f), 
                (int) (rectHeight));
    
        g.setColor(Color.blue.darker());
        g.setColor(Color.black);
        g.drawString(st1, -textWidth / 2, -dim.height * size * .75f - margin - (textHeight + spacing) * 2f);
    
        g.setTransform(at);
    }
    

    @Override
    public void update(ArrayList<OceanObject> objList, OceanPanel panel) {
        this.checkWalls();
        this.move();

        for (int i = bullets.size() - 1; i >= 0; i--) {
            bullets.get(i).update(objList, panel);

            // Check for collisions with SmartSuperOrca
            for (int j = objList.size() - 1; j >= 0; j--) {
                OceanObject obj = objList.get(j);
                if (obj instanceof SmartSuperOrca && bullets.get(i).getOutline().intersects(obj.getOutline().getBounds2D())) {
                    bullets.remove(i);
                    objList.remove(j);
                    break; // Exit the loop after a hit
                }
            }

            if (i < bullets.size() && (bullets.get(i).checkWalls(panel) || bullets.get(i).didHit)) {
                bullets.remove(i);
            }
        }

        checkCollisionOthers(objList);

    }

    // private void checkCollisionOthers(ArrayList<OceanObject> objList){
    //     ArrayList<OceanObject> oList = Util.filteredOrcaList(objList);
    //     ArrayList<OceanObject> fList = Util.filteredFishList(objList);

    //     for(int i = 0 ; i < oList.size() ; i++){
    //         OceanObject currentOrca = oList.get(i);

    //         if(currentOrca.getFOV().intersects(this.getBoundingBox())){
    //             currentOrca.setSpeed(new PVector(-currentOrca.getSpeed().x, -currentOrca.getSpeed().y));
    //         }
    //     }
        
    //     for(int i = 0 ; i < fList.size() ; i++){
    //         OceanObject currentFish = fList.get(i);

    //         if(currentFish.getFOV().intersects(this.getBoundingBox())){
    //             currentFish.setSpeed(new PVector(-currentFish.getSpeed().x, -currentFish.getSpeed().y));
    //         }
    //     }
    // }

    private void checkCollisionOthers(ArrayList<OceanObject> objList) {
        ArrayList<OceanObject> oList = Util.filteredOrcaList(objList);
        ArrayList<OceanObject> fList = Util.filteredFishList(objList);
    
        // Check collision with orcas
        for (int i = 0; i < oList.size(); i++) {
            OceanObject currentOrca = oList.get(i);
    
            if (currentOrca.getFOV().intersects(this.getBoundingBox())) {
                float coef = 0.8f; // coefficient of acceleration relative to maxSpeed
                PVector direction = PVector.sub(currentOrca.getPos(), this.getPos()).normalize(); // reverse the direction
                PVector accel = PVector.mult(direction, 5.0f * coef);
                currentOrca.setSpeed(accel);
            }
        }
    
        // Check collision with fish
        for (int i = 0; i < fList.size(); i++) {
            OceanObject currentFish = fList.get(i);
    
            if (currentFish.getFOV().intersects(this.getBoundingBox())) {
                float coef = 0.9f; // coefficient of acceleration relative to maxSpeed
                PVector direction = PVector.sub(currentFish.getPos(), this.getPos()).normalize(); // reverse the direction
                PVector accel = PVector.mult(direction, 5.0f * coef);
                currentFish.setSpeed(accel);
            }
        }
    }
    

    private void checkWalls() {
        if (this.pos.x < 80) {
            this.pos.x = 80;
            this.speed.x *= -1;
        } else if (this.pos.x > 120) {
            this.pos.x = 120;
            this.speed.x *= -1;
        }
        if (this.pos.y < 70) {
            this.pos.y = 70;
            this.speed.y *= -1;
        } else if (this.pos.y > 450) {
            this.pos.y = 450;
            this.speed.y *= -1;
        }
    }
    

    @Override
    protected void setShapeAttributes() {
        head = new Ellipse2D.Double(-15, -15, 30, 30);
        body = new RoundRectangle2D.Double(-20, 15, 35, 70, 40, 40);
        airTank = new RoundRectangle2D.Double(-35, 25, 25, 50, 20, 20);
        legs = new RoundRectangle2D.Double(-12, 85, 20, 60, 20, 20);
        feet = new RoundRectangle2D.Double(-10, 130, 30, 15, 20, 20);
    }

    @Override
    protected void setOutline() {
        outline = new Area(body);
        outline.add(new Area(head));
        outline.add(new Area(airTank));
        outline.add(new Area(legs));
        outline.add(new Area(feet));
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
        long currentTime = System.nanoTime();
        if (currentTime - lastFiredTime >= FIRE_INTERVAL) {
            float bulletSpeed = 5.0f; // Define bullet speed
            PVector bulletVelocity = PVector.fromAngle(currentAngle).mult(bulletSpeed);
            bullets.add(new Bullets(this.pos.x, this.pos.y, 1, 1, 1f, bulletVelocity));
            lastFiredTime = currentTime; // Update last fired time
        }
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    protected PVector getSpeed() {
        return speed;
    }

    @Override
    protected boolean getIsAlive() {
        throw new UnsupportedOperationException("Unimplemented method 'getIsAlive'");
    }
}
