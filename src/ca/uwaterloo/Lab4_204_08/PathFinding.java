package ca.uwaterloo.Lab4_204_08;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import mapper.MapView;
import mapper.NavigationalMap;
import mapper.PositionListener;
import mapper.VectorUtils;
import android.graphics.PointF;
import android.widget.TextView;
import android.widget.Toast;

public class PathFinding implements PositionListener {

	public static List<PointF> pathList;
	public static MapView mapView;
	public static NavigationalMap map;
	private List<PointF> wayPoints = new ArrayList<PointF>();
	private List<PointF> userDirectWayPoints = new ArrayList<PointF>();
	private List<PointF> destDirectWayPoints = new ArrayList<PointF>();
	private static TextView pathInfo;
	private static Toast wallHit;

	public PathFinding(List<PointF> pathList, MapView mapView,
			NavigationalMap map, TextView pathInfo, Toast wallHit) {
		PathFinding.pathList = pathList;
		PathFinding.mapView = mapView;
		mapView.addListener(this);
		PathFinding.map = map;
		PathFinding.pathInfo = pathInfo;
		PathFinding.wallHit = wallHit;
		wayPoints.add(mapView.getWayPoint1());
		wayPoints.add(mapView.getWayPoint2());
		wayPoints.add(mapView.getWayPoint3());
		wayPoints.add(mapView.getWayPoint4());
		wayPoints.add(mapView.getWayPoint5());
		wayPoints.add(mapView.getWayPoint6());
		wayPoints.add(mapView.getWayPoint7());
		wayPoints.add(mapView.getWayPoint8());
	}

	public void findPath() {
		pathList.clear();
		if (map.calculateIntersections(mapView.getUserPoint(),
				mapView.getDestinationPoint()).size() == 0) {
			pathList.add(mapView.getUserPoint());// direct path from user to
													// dest
			pathList.add(mapView.getDestinationPoint());
			mapView.setUserPath(pathList);

			if (VectorUtils.distance(mapView.getUserPoint(),
					mapView.getDestinationPoint()) < 1) {
				wallHit.setText("You have arrived the destination");
				wallHit.show();
			}
		} else {
			userDirectWayPoints.clear();
			destDirectWayPoints.clear();
			for (PointF wayPoint : this.closestToUser(wayPoints)) {// find
																	// closest
																	// waypoint
																	// for user
				if (map.calculateIntersections(mapView.getUserPoint(), wayPoint)
						.size() == 0) {
					userDirectWayPoints.add(wayPoint);
				}
			}

			for (PointF wayPoint : this.closestToDestination(wayPoints)) {// find
																			// closest
																			// waypoint
																			// for
																			// dest
				if (map.calculateIntersections(mapView.getDestinationPoint(),
						wayPoint).size() == 0) {
					destDirectWayPoints.add(wayPoint);
				}
			}
			if (!userDirectWayPoints.isEmpty()
					&& !destDirectWayPoints.isEmpty()) {
				if (userDirectWayPoints.size() == 1
						&& destDirectWayPoints.size() == 1) {
					if (map.calculateIntersections(userDirectWayPoints.get(0),
							destDirectWayPoints.get(0)).size() == 0) {
						pathList.add(mapView.getUserPoint());// direct path from
																// user's only
																// direct
																// waypoint
																// to dest's
																// only
																// direct
																// waypoint
						pathList.add(userDirectWayPoints.get(0));
						pathList.add(destDirectWayPoints.get(0));
						pathList.add(mapView.getDestinationPoint());
					}
				} else if (destDirectWayPoints.size() == 1) { // user has
																// multiple
																// direct
																// waypoints,
																// dest only has
																// one
					if (map.calculateIntersections(mapView.getUserPoint(),
							destDirectWayPoints.get(0)).size() == 0) {
						pathList.add(mapView.getUserPoint()); // direct path
																// from
																// user to
																// dest's
																// only direct
																// waypoint
						pathList.add(destDirectWayPoints.get(0));
						pathList.add(mapView.getDestinationPoint());
					} else {
						if (map.calculateIntersections(
								userDirectWayPoints.get(0),
								destDirectWayPoints.get(0)).size() == 0) {
							pathList.add(mapView.getUserPoint());
							pathList.add(userDirectWayPoints.get(0));
							pathList.add(destDirectWayPoints.get(0));
							pathList.add(mapView.getDestinationPoint());
						}
					}
				} else if (userDirectWayPoints.size() == 1) { // user has one
					// direct waypoints,
					// dest has multiple
					if (map.calculateIntersections(
							mapView.getDestinationPoint(),
							userDirectWayPoints.get(0)).size() == 0) {
						pathList.add(mapView.getUserPoint()); // direct path
																// from
						// dest to user's
						// only direct
						// waypoint
						pathList.add(userDirectWayPoints.get(0));
						pathList.add(mapView.getDestinationPoint());
					} else {
						if (map.calculateIntersections(
								destDirectWayPoints.get(0),
								userDirectWayPoints.get(0)).size() == 0) {
							pathList.add(mapView.getUserPoint());
							pathList.add(userDirectWayPoints.get(0));
							pathList.add(destDirectWayPoints.get(0));
							pathList.add(mapView.getDestinationPoint());
						}
					}
				} else { // both user and dest have multiple direct waypoints
					if (map.calculateIntersections(destDirectWayPoints.get(0),
							userDirectWayPoints.get(0)).size() == 0) {
						pathList.add(mapView.getUserPoint()); // direct path
																// from
						// dest to user's
						// only direct
						// waypoint
						pathList.add(userDirectWayPoints.get(0));
						pathList.add(destDirectWayPoints.get(0));
						pathList.add(mapView.getDestinationPoint());
					} else if (map.calculateIntersections(
							destDirectWayPoints.get(0),
							userDirectWayPoints.get(0)).size() != 0) {
						if (map.calculateIntersections(
								mapView.getDestinationPoint(),
								userDirectWayPoints.get(0)).size() == 0) {
							pathList.add(mapView.getUserPoint()); // direct path
																	// from
							// dest to user's
							// only direct
							// waypoint
							pathList.add(userDirectWayPoints.get(0));
							pathList.add(mapView.getDestinationPoint());
						} else if (map.calculateIntersections(mapView.getUserPoint(),
								destDirectWayPoints.get(0)).size() == 0) {
							pathList.add(mapView.getUserPoint()); // direct path
																	// from
																	// user to
																	// dest's
																	// only
																	// direct
																	// waypoint
							pathList.add(destDirectWayPoints.get(0));
							pathList.add(mapView.getDestinationPoint());
						}
						for (PointF userWayPoint : userDirectWayPoints) {
							for (PointF destWayPoint : destDirectWayPoints) {
								if (map.calculateIntersections(destWayPoint,
										userWayPoint).size() == 0 && pathList.isEmpty()) {
									pathList.add(mapView.getUserPoint());
									pathList.add(userWayPoint);
									pathList.add(destWayPoint);
									pathList.add(mapView.getDestinationPoint());
								}
								break;
							}
							if (!pathList.isEmpty()) {
								break;
							}
						}
					}
				}
			}
			mapView.setUserPath(pathList);
		}
		pathInfo.setText(String.format("User: %d  Dest : %d",
				userDirectWayPoints.size(), destDirectWayPoints.size()));
	}

	public List<PointF> closestToUser(List<PointF> ret) {
		Collections.sort(ret, new Comparator<PointF>() {
			@Override
			public int compare(PointF arg0, PointF arg1) {
				float distStart0 = VectorUtils.distance(mapView.getUserPoint(),
						arg0);
				float distStart1 = VectorUtils.distance(mapView.getUserPoint(),
						arg1);
				if (VectorUtils.isZero(distStart0 - distStart1))
					return 0;
				else if (distStart0 < distStart1)
					return -1;
				else
					return 1;
			}
		});
		return ret;
	}

	public List<PointF> closestToDestination(List<PointF> ret) {
		Collections.sort(ret, new Comparator<PointF>() {
			@Override
			public int compare(PointF arg0, PointF arg1) {
				float distStart0 = VectorUtils.distance(
						mapView.getDestinationPoint(), arg0);
				float distStart1 = VectorUtils.distance(
						mapView.getDestinationPoint(), arg1);
				if (VectorUtils.isZero(distStart0 - distStart1))
					return 0;
				else if (distStart0 < distStart1)
					return -1;
				else
					return 1;
			}
		});
		return ret;
	}

	@Override
	public void originChanged(MapView source, PointF loc) {
		source.setUserPoint(loc);
		this.findPath();

	}

	@Override
	public void destinationChanged(MapView source, PointF dest) {
		this.findPath();
	}

}
