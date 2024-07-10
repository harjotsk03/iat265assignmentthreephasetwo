
import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;

import processing.core.PVector;

public class Util {

	public static float random(double min, double max) {
		return (float) (Math.random()*(max-min)+min);
	}
	
	public static float random(double max) {
		return (float) (Math.random()*max);
	}
	
	public static Color randomColor() {
		int r = (int) random(255);
		int g = (int) random(255);
		int b = (int) random(255);
		
		return new Color(r,g,b);
	}

	public static Color randomColorBlue() {
		int r = (int) random(0, 50);
		int g = (int) random(0, 50);
		int b = (int) random(170, 255);
		
		return new Color(r, g, b);
	}
	
	
	
	public static PVector randomPVector(int maxX, int maxY) {		
		return new PVector((float)random(maxX), (float)random(maxY));	
	}
	
	public static PVector randomPVector(float magnitude) {
		return PVector.random2D().mult(magnitude);
	}
	
	public static Fish randomFish(OceanPanel pane) {
		return new Fish(	Util.random(60, 1100), 
						Util.random(60, 600),
						Util.random(2.5,3.5),
						Util.random(-2, 2),
						Util.random(-2, 2),
						Util.randomColor());
	}
	

	
	public static int countFood(ArrayList<OceanObject> objList) {
		int i = 0;
		for (OceanObject obj:objList) if (obj instanceof Fish) i++;
		return i;
	}
	
	public static int countOrca(ArrayList<OceanObject> objList) {
		int i = 0;
		for (OceanObject obj:objList) if (obj instanceof SmartSuperOrca) i++;
		return i;
	}

	public static ArrayList<OceanObject> filteredOrcaList(ArrayList<OceanObject> oList) {
		ArrayList<OceanObject> list = new ArrayList<>();
		for (int i = 0; i < oList.size(); i++) {
			OceanObject f = oList.get(i);
			if (f instanceof SmartSuperOrca) {
				list.add(f);
			}
		}
		return list;
	}

	public static ArrayList<OceanObject> filteredFishList(ArrayList<OceanObject> oList) {
		ArrayList<OceanObject> list = new ArrayList<>();
		for (int i = 0; i < oList.size(); i++) {
			OceanObject f = oList.get(i);
			if (f instanceof Fish) {
				list.add(f);
			}
		}
		return list;
	}
	
	
}
