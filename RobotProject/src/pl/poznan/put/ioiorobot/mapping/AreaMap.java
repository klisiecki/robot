package pl.poznan.put.ioiorobot.mapping;

import java.util.ArrayList;
import java.util.List;

import pl.poznan.put.ioiorobot.positioning.Position;
import pl.poznan.put.ioiorobot.utils.Config;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.Log;

/**
 * Klasa reprezentująca mapę przestrzeni tworzoną przez robota
 */
public class AreaMap {
	private List<Pattern> patterns = new ArrayList<Pattern>();
	private List<Obstacle> obstacles = new ArrayList<Obstacle>();
	private Position robotPosition;
	
	
	public List<Pattern> getPatterns() {
		return patterns;
	}
	
	public List<Obstacle> getObstacles() {
		return obstacles;
	}
	
	public Position getRobotPosition() {
		return robotPosition;
	}

	public AreaMap(Position robotPosition) {
		this.robotPosition = robotPosition;
	}

	public void addPattern(Pattern pattern) {
		patterns.add(pattern);
	}

	public void addObstacle(Obstacle obstacle) {
		obstacles.add(obstacle);
	}
}
