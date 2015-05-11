package org.citydb.util;

import java.awt.Polygon;

public class test {

	/**
	 * @param args
	 */
	private Point[] points;
	public static void main(String[] args) {
		
		test instance = new test();
		System.out.println(instance.contains(new Point(2.0, 2.99)));
	}

	public test () {
		points = new Point[4];
		points[0] = new Point(1.0, 1.0);
		points[1] = new Point(1.0, 3.0);
		points[2] = new Point(3.0, 3.0);
		points[3] = new Point(3.0, 1.0);
	}
	
	public boolean contains(Point test) {
		int i;
		int j;
		boolean result = false;
		
		for (i = 0, j = points.length - 1; i < points.length; j = i++) {
			if ((points[i].y > test.y) != (points[j].y > test.y)
					&& (test.x < (points[j].x - points[i].x) * (test.y - points[i].y) / (points[j].y - points[i].y) + points[i].x)) {
				result = !result;
			}
		}
		return result;
	}

	
}
class Point {
	public Point(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public double x;
	public double y;
}