

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Ellipse2D.Double;
import java.util.ArrayList;

import processing.core.PVector;

public class SuperOrca extends Orca {

	public SuperOrca(float x, float y, float size) {
		super(x, y, 20, 10, size);

		this.color = Util.randomColorBlue();
	}

	@Override
	protected void setShapeAttributes() {
		head = (Double) new Ellipse2D.Double(-dim.width /2, -dim.height / 2, dim.width, dim.height);
		eye = new Ellipse2D.Double(dim.width / 4, -dim.height / 4, dim.width / 20, dim.width / 20);

		int[] px = { (int) (dim.width / 4), (int) (-dim.width / 2), (int) (-dim.width / 5), (int) (-dim.width / 2) };
		int[] py = { 0, (int) (-dim.height / 2), 0, (int) (dim.height / 2) };
		tail = new Polygon(px, py, px.length);

	}

	@Override
	public void draw(Graphics2D g) {
		// transformation
		AffineTransform at = g.getTransform();
		g.translate(pos.x, pos.y);
		g.rotate(speed.heading());
		g.scale(size, size);
		if (speed.x < 0)
			g.scale(1, -1);

			if (state == SICK) {
							
				g.setColor(Color.white);
				g.fill(tail);
			
				// body
				g.setColor(color.darker().darker().darker());
				g.fill(head);


			} else {
				g.setColor(Color.white);
				g.fill(tail);
			
				// body
				g.setColor(color);
				g.fill(head);
			}
			
		if (state == HUNGRY) {
			g.setColor(Color.WHITE);
			eye.setFrame(dim.width / 4, -dim.height / 3, dim.width / 10, dim.width / 10);
		} else if (state == HALF_FULL) {
			g.setColor(Color.ORANGE);
			eye.setFrame(dim.width / 4, -dim.height / 3, dim.width / 15, dim.width / 15);
		} else {
			g.setColor(Color.green);
			eye.setFrame(dim.width / 4, -dim.height / 3, dim.width / 20, dim.width / 20);
		}
		
		g.fill(eye);

		g.setTransform(at);
	}

	@Override
	protected void traceBestFood(ArrayList<OceanObject> fList) {
		if (fList.size() > 0) {
			// find 1st target
			OceanObject target = fList.get(0);
			float distToTarget = PVector.dist(pos, target.getPos());

			// find the closer one
			for (int i = 0; i < fList.size(); i++) {
				OceanObject f = fList.get(i);
				if (PVector.dist(pos, f.getPos()) < distToTarget) {
					target = f;
					distToTarget = PVector.dist(pos, target.getPos());
				}
			}			

			// make animal follow this target
			this.approach(target);
		}
	}

	@Override
	protected void setOutline() {
		outline = new Area(head);
		outline.add(new Area(tail));

		FOVarea = new Area(FOV);
	}

	@Override
	protected Shape getOutline() {
		AffineTransform at = new AffineTransform();
		at.translate(pos.x, pos.y);
		at.rotate(speed.heading());
		at.scale(size, size);
		return at.createTransformedShape(outline);
	}

	@Override
	protected boolean eatable(OceanObject food) {
		return (food instanceof Fish);
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
		return energy;
	}

	@Override
	protected void setSpeed(PVector accel) {
		throw new UnsupportedOperationException("Unimplemented method 'setSpeed'");
	}

	@Override
    protected PVector getSpeed() {
        return speed;
    }

	@Override
	protected void fire() {
		throw new UnsupportedOperationException("Unimplemented method 'fire'");
	}

}
