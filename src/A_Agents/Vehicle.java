package A_Agents;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import A_Environment.Incident;
import A_Environment.Object_Zone;
import A_Environment.Path;
import A_Environment.RoadLink;
import A_Environment.RoadNode;
import D_Ontology.BuildStaticFuction;
import D_Ontology.Ontology.Level;
import D_Ontology.Ontology.VehicleAction;
import D_Ontology.Ontology.VehicleTriggers;
import repast.simphony.context.Context;
import repast.simphony.space.gis.Geography;
import repast.simphony.space.graph.Network;
import repast.simphony.space.graph.RepastEdge;
import repast.simphony.space.graph.ShortestPath;

public class Vehicle {

	public String Id;
	double VehicleSpeed=0;// in KM
	double org_vehicle_speed = 0;

	public VehicleAction Action;
	public VehicleTriggers SensingeEnvironmentTrigger,ActionEffectTrigger ,InterpretedTrigger ,EndActionTrigger ;

	public Incident AssignedIncident ;
	public Responder  AssignedDriver ;
	public boolean FirstArrivalofVehicleAll=false;
	public boolean FirstArrivalofVehicleAgency=false;
	boolean Busy=false;
	boolean insideCordon=false;
	public int ColorCode =0; //for visualization purpose	
	public  double perpration_delay= 0 ;  // I used this temprory I like to put 1 menuts  btween vichels
	// --------------------------------------------------------------

	public RoadNode Node_of_current_Location=null;
	public RoadNode Node_of_Target_Location=null;
	public RoadNode Node_of_incident_location =null;
	public RoadNode Node_of_current_GPSLocation=null;
	//public boolean StartGPS=false;
	
	public String Nodename_of_current_Location=null;
	public String Nodename_of_Target_Location=null;
	public String Nodename_of_incident_location =null;

	static Random xxxrandomizer= new Random();

	// --------------------------------------------------------------
	List<RepastEdge<RoadNode>> sequenced_path; // of link and its nodes
	List<RoadLink> listpath_link; //links
	Path PathAgent; // for visualization only  green path
	RoadNode source_node = null, target_node = null, x;
	public double Total_distance, total_time; // meters and minutes
	double remaining_of_road__segment;
	double shift_amount = 0;	
	double road_speed = -1;
	int current_edge_index = 0;
	int current_edge_point_index = -1;
	double point_to_point_progress = 0;
	List<Object_Zone> searchZoneAgents;

	// --------------------------------------------------------------

	int DelayofNode=0 ;	
	public ArrayList<RoadLink>RouteW_list  = new ArrayList<RoadLink>() ; //links
	public ArrayList<RoadLink> RouteTr_list = new ArrayList<RoadLink>(); //links
	// --------------------------------------------------------------
	Context<Object> context;
	Geography<Object> geography;

	// ***********************************************************************************************
	public void vehicle(Context<Object> _context, Geography<Object> _geography,Coordinate Location, String Nodename_initialLocation,RoadNode _Node, String ID, int speed) {

		context = _context;
		geography = _geography;			
		context.add(this);
		GeometryFactory fac = new GeometryFactory();
		geography.move(this, fac.createPoint(Location));

		//-------------------------------------------------
		Id = ID;
		Nodename_of_current_Location = Nodename_initialLocation;
		Node_of_current_Location=_Node;
		org_vehicle_speed = speed;
		Action = D_Ontology.Ontology.VehicleAction.Idle;
	}

	public int getColorCode() {
		return this.ColorCode;
	}

	public void setColorCode(int ColorCode1) {
		this.ColorCode = ColorCode1;
	}

	//===========================================================================================2=====================================================================================================
	public void AssignDestination2(RoadNode Node_Location , boolean IsCheckOnly ,  boolean insideCordon2) {

		Rest(true);
		// System.out.println( Node_of_current_Location.fid +" " + Node_Location.fid);
		Total_distance = Generate_Shortest_Path2(Node_of_current_Location, Node_Location,IsCheckOnly ,insideCordon2);		
		//System.out.println("Total dis to drive " + Total_distance);

		if (!IsCheckOnly)
		{
			//Nodename_of_Target_Location=Nodename_Location;	
			Node_of_Target_Location=Node_Location;
		}

	}

	// ***********************************************************************************************
	// identify short path based on distance
	public double Generate_Shortest_Path2(RoadNode sourcenode_Location, RoadNode targetnode_Location, boolean IsCheckOnly , boolean insideCordon2 ) {

		Network<RoadNode> Road_network ;

		if ( insideCordon2 ) 
			Road_network = (Network<RoadNode>) context.getProjection("cordon_network");
		else			
			Road_network = (Network<RoadNode>) context.getProjection("Road_network");
		

		Iterable<?> nearObjects = geography.getAllObjects();
		DelayofNode=0;
		source_node = sourcenode_Location;
		target_node = targetnode_Location;
		source_node.ColorCode=2;
		target_node.ColorCode=2;
		//--------------------------------------
		// 2- get a ShortestPath by using dijkstra's algorithm
		ShortestPath<RoadNode> p = new ShortestPath<RoadNode>(Road_network);
		double L = p.getPathLength(source_node, target_node);


		if ( L ==  Double.POSITIVE_INFINITY )  System.out.println(  this.Id +"Eerrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrror in Path" + L  +"  "+ source_node.Name + "  "+target_node.Name);
		//--------------------------------------
		// 3- search about line geometry of selected nodes path and list(array) of Roadlink
		RoadNode node_temp = source_node;
		RepastEdge<RoadNode> edg_temp;
		sequenced_path = new ArrayList<RepastEdge<RoadNode>>();
		listpath_link = new ArrayList<RoadLink>();
		List<Geometry> listpath_geom = new ArrayList<Geometry>();

		for (RepastEdge edg : p.getPath(source_node, target_node)) {
			for (Object o : nearObjects) {
				if (o instanceof RoadLink) {
					Geometry geom = geography.getGeometry(o);
					if (geom.intersects(geography.getGeometry(edg.getSource()))
							&& geom.intersects(geography.getGeometry(edg.getTarget()))) {
						listpath_link.add((RoadLink) o);
						listpath_geom.add(geom);
						break;
					}
				}
			}
			if (node_temp.equals(edg.getSource())) {
				sequenced_path.add(edg);
				node_temp = (RoadNode) edg.getTarget();
			} else {
				edg_temp = new RepastEdge<RoadNode>((RoadNode) edg.getTarget(), (RoadNode) edg.getSource(), false, (double) edg.getWeight());
				sequenced_path.add(edg_temp);
				node_temp = (RoadNode) edg.getSource();
			}

			DelayofNode++;
		}

		DelayofNode= DelayofNode * 2;
		//--------------------------------------------
		// used to track w and t in path by driver

		for ( RoadLink  RL : listpath_link)
		{
			if ( RL.WreckageLevel !=Level.None  &&  AssignedDriver.inAmbEvacuation )
				{RouteW_list.add(RL); RL.print_ReportW=true; RL.Wreckage_usedBefor++;}
			
			if ( RL.TrafficLevel !=Level.None &&  AssignedDriver.inAmbEvacuation )
				{RouteTr_list.add(RL);RL.print_ReportT=true;RL.Traffic_usedBefor++;}	
			
			if ( RL.WreckageLevel ==Level.None && RL.print_ReportW==true && AssignedDriver.inAmbEvacuation )
				RL.Wreckage_usedAfter++;
			if ( RL.TrafficLevel ==Level.None && RL.print_ReportT==true &&  AssignedDriver.inAmbEvacuation )
				RL.Traffic_usedAfter++;
		}
		
		
		
		
		if ( DelayofNode >= 350 )  System.out.println(  " ############################################################################  Ambulance  ***  DelayofNode " + this.Id  +"     " + DelayofNode );
		
				
		//--------------------------------------
		//4- testing exam : return without building path agent nor search zone, since this is only a test call only
		GeometryFactory fac = new GeometryFactory();
		Geometry geom_path = fac.buildGeometry(listpath_geom);
		
		
		if (geom_path instanceof MultiLineString) {

			
			nearObjects=null;
			
			if (IsCheckOnly)// return without building path agent nor search zone, since this is only a test call
				return L;
			else {

				// Add PathAgent in context to visualization
				MultiLineString SHP = (MultiLineString) geom_path;
				PathAgent = new Path( listpath_geom.size(), L);
				context.add(PathAgent);
				geography.move(PathAgent, SHP);
				//generate_vehicle_search_zone();

				return L;
			}
		}
		return -1; // Otherwise if there is no path 
	};

	// ***********************************************************************************************
	// Driving to destination one second
	public void Drive() {

		//------------------
		if ( DelayofNode   !=0 )
		{
			DelayofNode --;

			// System.out.println(  " Drive ***  " + this.Id  +"     " + DelayofNode ); 
		}
		//------------------
		else
		{

			if (Total_distance > 0) {

				// Ready steady Gooo .....change its position

				// pick road speed if available
				if (road_speed > 0)
					VehicleSpeed = road_speed;
				else
					VehicleSpeed = org_vehicle_speed;

				double distance_meter_per_second = (VehicleSpeed/ 2.237 ); // Convert speed from mph to meter per second  see Internet  

				if (Total_distance < distance_meter_per_second)
					distance_meter_per_second = Total_distance;

				update_postion(drive_distance(distance_meter_per_second));
				Total_distance -= distance_meter_per_second;

			} else  
			{

				//  Vehicle has arrived to destination 
				shift_amount = 0;
				update_postion(target_node.getCurrentPosition());
				if (PathAgent != null) {
					context.remove(PathAgent);
					PathAgent = null;
				}

				//update location with Current_Location
				Nodename_of_current_Location=target_node.fid;	
				Node_of_current_Location=target_node;
				Nodename_of_Target_Location=null;
				Node_of_Target_Location=null;
				SensingeEnvironmentTrigger=VehicleTriggers.Arrivedlocation ;
				//	System.out.println("Vehicle" + Id + " has arrived to destination  ");
				
				System.gc() ;
			}
		}

	};

	// --------------------------------------------------------------
	// Here we calculate next vehicle position based on required distance( required distance that calculated based on speed)
	// vehicle advances on a track we calculated on incident assigning
	private Coordinate drive_distance(double distance) {

		// cache current edge and edge's point indices
		int temp_edge_index = current_edge_index;
		int temp_edge_point_index = current_edge_point_index;
		Coordinate position_coordinate = geography.getGeometry(this).getCoordinate();



		if (current_edge_index >= sequenced_path.size())// we reached target node
			return target_node.getCurrentPosition();
		// select current edge
		RepastEdge<RoadNode> current_edge = sequenced_path.get(current_edge_index);
		double current_edge_length = current_edge.getWeight();
		shift_amount = 0;
		double edge_remaining = 0;
		LineString LS = (LineString) geography.getGeometry(listpath_link.get(current_edge_index));
		//Road speed
		road_speed = listpath_link.get(current_edge_index).getRoadSpeed();
		boolean isReversed = false;

		// Check for the correct road direction by matching current edge with current road link
		if (BuildStaticFuction.DistanceC(geography, current_edge.getSource().getCurrentPosition(),
				LS.getPointN(0).getCoordinate()) > BuildStaticFuction.DistanceC(geography,
						current_edge.getTarget().getCurrentPosition(), LS.getPointN(0).getCoordinate()))
			isReversed = true;

		if (current_edge_point_index == -1)// new edge
			edge_remaining = current_edge_length;
		else
			edge_remaining = BuildStaticFuction.DistanceC(geography, position_coordinate,
					current_edge.getTarget().getCurrentPosition());
		while (distance > edge_remaining) {
			current_edge_index++;// advance edge index to the next one
			distance -= edge_remaining;
			if (current_edge_index >= sequenced_path.size())
				return target_node.getCurrentPosition();// target reached
			current_edge = sequenced_path.get(current_edge_index);
			edge_remaining = current_edge_length = current_edge.getWeight();
			current_edge_point_index = -1;
			point_to_point_progress = 0;
		}
		//======================
		Node_of_current_GPSLocation=current_edge.getTarget();
		//=====================
		LS = (LineString) geography.getGeometry(listpath_link.get(current_edge_index));
		//Road speed
		road_speed = listpath_link.get(current_edge_index).getRoadSpeed();
		isReversed = false;
		// Check for the correct road direction by matching the new edge with corresponding road link
		if (BuildStaticFuction.DistanceC(geography, current_edge.getSource().getCurrentPosition(),
				LS.getPointN(0).getCoordinate()) > BuildStaticFuction.DistanceC(geography,
						current_edge.getTarget().getCurrentPosition(), LS.getPointN(0).getCoordinate()))
			isReversed = true;
		if (isReversed) {
			if (current_edge_point_index == -1) {
				current_edge_point_index = LS.getNumPoints() - 1;
				point_to_point_progress = 0;
			}
			for (int i = current_edge_point_index; i > 0; i--) {
				// calculate distance between two consecutive points subtracting already achieved progress
				double inPoints_Distance = BuildStaticFuction.DistanceP(geography, LS.getPointN(i), LS.getPointN(i - 1))
						- point_to_point_progress;
				if (distance - inPoints_Distance <= 0) {
					current_edge_point_index = i;
					shift_amount = distance;
					point_to_point_progress += distance;
					if (temp_edge_index != current_edge_index || temp_edge_point_index != current_edge_point_index) {
						position_coordinate = LS.getPointN(i).getCoordinate();
					}
					double normalizedShift = distance / inPoints_Distance;
					// calculate position on a line between two points with specific shifting
					position_coordinate = new Coordinate(
							position_coordinate.x
							+ (LS.getPointN(i - 1).getCoordinate().x - position_coordinate.x) * normalizedShift,
							position_coordinate.y + (LS.getPointN(i - 1).getCoordinate().y - position_coordinate.y)
							* normalizedShift);
					break;
				} else {
					distance -= inPoints_Distance;
					point_to_point_progress = 0;
				}
			}
		} else {
			if (current_edge_point_index == -1) {
				current_edge_point_index = 0;
				point_to_point_progress = 0;
			}
			for (int i = current_edge_point_index; i < LS.getNumPoints() - 1; i++) {
				// calculate distance between two consecutive points subtracting already achieved progress
				double inPoints_Distance = BuildStaticFuction.DistanceP(geography, LS.getPointN(i), LS.getPointN(i + 1))
						- point_to_point_progress;

				if (distance - inPoints_Distance <= 0) {
					current_edge_point_index = i;
					shift_amount = distance;
					point_to_point_progress += distance;
					if (temp_edge_index != current_edge_index || temp_edge_point_index != current_edge_point_index) {
						position_coordinate = LS.getPointN(i).getCoordinate();
					}
					double normalizedShift = distance / inPoints_Distance;
					// calculate position on a line between two points with specific shifting
					position_coordinate = new Coordinate( position_coordinate.x + (LS.getPointN(i + 1).getCoordinate().x - position_coordinate.x) * normalizedShift, position_coordinate.y + (LS.getPointN(i + 1).getCoordinate().y - position_coordinate.y) * normalizedShift);
					break;
				} else 
				{
					distance -= inPoints_Distance;
					point_to_point_progress = 0;
				}
			}
		}
		return position_coordinate;
	}

	// ***********************************************************************************************************************************
	// Generate a list of search zone agents that will be used by the vehicle agent  to detect nearby objects.
	private void generate_vehicle_search_zone() {

		int numSearchZones = 10;
		double radius = 0.0004; // Radius of the search circle area in degree lat/lon
		Coordinate center = geography.getGeometry(this).getCoordinate();
		searchZoneAgents = BuildStaticFuction.GenerateSearchZones(geography, context, center, numSearchZones, radius, this);
	}

	// --------------------------------------------------------------
	// update_postion of vehicle each second
	public void update_postion(Coordinate new_position) {

		//		Coordinate lastPosition = geography.getGeometry(this).getCoordinate();
		//		double x = (new_position.x - lastPosition.x);
		//		double y = (new_position.y - lastPosition.y);
		//		geography.moveByDisplacement(this, x, y);

		// for (Object_Zone zone : searchZoneAgents) {
		//	geography.moveByDisplacement(zone, x, y);
		//}
	}
	//----------------------------------------------------------------
	// reset all vehicle information
	public void Rest(boolean isResetIncidentInfo) {

		sequenced_path = new ArrayList<RepastEdge<RoadNode>>();

		if (searchZoneAgents != null) {
			for (Object_Zone obj : searchZoneAgents) {
				context.remove(obj);
			}
		}
		searchZoneAgents = new ArrayList<Object_Zone>();
		current_edge_point_index = -1;
		current_edge_index = 0;
		point_to_point_progress = 0;
		remaining_of_road__segment = 0;
		Nodename_of_incident_location = "";
		Nodename_of_Target_Location=null;
		Node_of_Target_Location=null;
		target_node = null;
		source_node = null;
		if (isResetIncidentInfo)
			AssignedIncident = null;
		if (PathAgent != null) {
			context.remove(PathAgent);
			PathAgent = null;
		}
		VehicleSpeed = org_vehicle_speed;
	}

	// --------------------------------------------------------------
	public VehicleAction getStatus() {
		return this.Action;
	}

	public void setStatus(VehicleAction _Status) {
		this.Action = _Status;
	}

	public Coordinate getCurrent_Location() {
		return geography.getGeometry(this).getCoordinate();
	}

	public String getId() {
		return this.Id;
	}








}
