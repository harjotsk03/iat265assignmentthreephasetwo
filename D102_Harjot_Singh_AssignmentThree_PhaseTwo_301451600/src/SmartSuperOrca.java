import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;


import processing.core.PVector;

public class SmartSuperOrca extends SuperOrca{
	
	public SmartSuperOrca(float x, float y, float size) {
		super(x, y, size);
	}

	@Override
	public void update(ArrayList<OceanObject> objList, OceanPanel panel){
		super.update(objList, panel);

		this.chaseMode(objList);

		checkCollisionBetween(objList);
	}
	
	@Override
	protected void setShapeAttributes() {
		super.setShapeAttributes();

		FOV = new Ellipse2D.Double(-20, -20, 40, 40);
	}
	
	@Override
	public void draw(Graphics2D g) {
		super.draw(g);
		
		AffineTransform at = g.getTransform();		
		g.translate(pos.x, pos.y);
		g.rotate(speed.heading());
		g.scale(size, size);
		if (speed.x < 0) g.scale(1, -1);
		
			
		g.setTransform(at);

		// g.setColor(Color.orange);
		// g.draw(getOutline());

		// g.setColor(Color.green);
		// g.draw(getBoundingBox());

	}
	
	@Override
	protected void traceBestFood(ArrayList<OceanObject> fList) {	
		if (fList.size()>0) {	
			OceanObject target = fList.get(0);
			float targetAttraction = this.getAttraction(target);
			
			for (OceanObject f:fList) if (this.getAttraction(f) > targetAttraction) {
				target = f;
				targetAttraction = this.getAttraction(target);
			}
			
			this.approach(target);
		}	
	}
	
	protected float getAttraction(OceanObject target) {
		return (target.getSize()*engGainRatio)/(PVector.dist(pos, target.getPos())/this.speed.mag() * engLossRatio);
	}

	private void checkCollisionBetween(ArrayList<OceanObject> objList){
		ArrayList<OceanObject> oList = Util.filteredOrcaList(objList);

		for(int i = 0 ; i < oList.size() ; i++){
			for(int j = i+1 ; j < oList.size() ; j++){
				OceanObject first = oList.get(i);
				OceanObject second = oList.get(j);

				if((first.getFOV().intersects(second.getBoundingBox()) &&
				second.getFOV().intersects(first.getBoundingBox()) )){
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
	protected void setSpeed(PVector accel){
		this.speed.add(accel);
	}

	private void chaseMode(ArrayList<OceanObject> objList){
		ArrayList<OceanObject> oList = Util.filteredFishList(objList);

		for(int i = 0 ; i < oList.size() ; i++){
			if((this.getFOV().intersects(oList.get(i).getBoundingBox()) &&
			oList.get(i).getFOV().intersects(this.getBoundingBox()))) {
				this.speed.mult(2);
			}else{
				this.speed.mult(1);
			}
		}
	}


}
