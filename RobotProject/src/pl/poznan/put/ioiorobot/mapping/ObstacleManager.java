package pl.poznan.put.ioiorobot.mapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import pl.poznan.put.ioiorobot.utils.Config;
import android.graphics.Point;

public class ObstacleManager {
	// TODO "wypalanie" punktów wokół robota po jakimś czasie

	private ObstacleHashMap obstacleHashMap;
	private List<Obstacle> rawObstacleList;
	
	public List<Obstacle> getRawObstacleList() {
		return rawObstacleList;
	}

	public interface ObstacleAcceptedListener {
		void onObstacleAccepted(Obstacle obstacle);
	}

	private ObstacleAcceptedListener obstacleAcceptedListener;

	public ObstacleManager() {
		obstacleHashMap = new ObstacleHashMap();
		rawObstacleList = new ArrayList<Obstacle>();
	}
	
	public void setObstacleAcceptedListener(ObstacleAcceptedListener obstacleAcceptedListener) {
		this.obstacleAcceptedListener = obstacleAcceptedListener;
	}

	public void addObstacle(Obstacle obstacle) {
		obstacleHashMap.addPoint(obstacle.getPoint());
		if (obstacleAcceptedListener != null) {
			obstacleAcceptedListener.onObstacleAccepted(obstacle);
		}
	}
	

	private class ObstacleHashMap extends HashMap<Point, Obstacle> {

		public void addPoint(Point p) {
			p.x = p.x / Config.obstacleCellSize * Config.obstacleCellSize;
			p.y = p.y / Config.obstacleCellSize * Config.obstacleCellSize;
			int xStart = p.x - Config.obstacleRange * Config.obstacleCellSize;
			int yStart = p.y - Config.obstacleRange * Config.obstacleCellSize;
			int xEnd = p.x + Config.obstacleRange * Config.obstacleCellSize;
			int yEnd = p.y + Config.obstacleRange * Config.obstacleCellSize;

			for (int i = xStart; i <= xEnd; i += Config.obstacleCellSize) {
				for (int j = yStart; j <= yEnd; j += Config.obstacleCellSize) {
					put(new Point(i, j));
				}
			}
		}

		private void put(Point point) {
			Obstacle o;
			if (containsKey(point)) {
				o = get(point);
				o.increment();
			} else {
				put(point, o =  new Obstacle(new Point(point)));
			}
			if (o.getCount() >= Config.minObstacleCount && !o.isAccepted()) {
				if (obstacleAcceptedListener != null) {
					o.accept();
					obstacleAcceptedListener.onObstacleAccepted(o);
				}
			}
		}
	}

}
