package D_Ontology;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.geotools.geometry.jts.JTS;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.util.GeometricShapeFactory;

import A_Agents.Hospital;
import A_Agents.Station_Ambulance;
import A_Agents.Station_Fire;
import A_Agents.Station_Police;
import A_Environment.Incident;
import A_Environment.Object_Zone;
import A_Environment.Path;
import A_Environment.RoadLink;
import A_Environment.RoadNode;
import A_Environment.Sector;
import A_Environment.TopographicArea;
import repast.simphony.context.Context;
import repast.simphony.gis.util.GeometryUtil;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.gis.Geography;
import repast.simphony.space.graph.Network;
import repast.simphony.space.graph.RepastEdge;
import repast.simphony.space.graph.ShortestPath;
import repast.simphony.util.ContextUtils;
import repast.simphony.util.SimUtilities;

public class BuildStaticFuction {

	//---------------------------------------------------------------------------------
	public static void clearExtraNW(  Context<Object> context ,Geography<Object> geography  ,  RoadNode incNode ,  RoadNode PEANode ,
			List<Hospital> Hospital_list ,  List<Station_Ambulance> Station_Ambulance_list  , List<Station_Fire> Station_Fire_list , List<Station_Police> Station_Police_list )
	{

		for (Hospital H :Hospital_list )
		{
			BuildStaticFuction.Generate_Shortest_Path_Clearing(context, geography , incNode , H.Node , true);
			BuildStaticFuction.Generate_Shortest_Path_Clearing(context, geography , PEANode , H.Node , true);	

			BuildStaticFuction.Generate_Shortest_Path_Clearing(context, geography ,  H.Node,incNode , true);
			BuildStaticFuction.Generate_Shortest_Path_Clearing(context, geography ,  H.Node ,PEANode , true);
		}


		for (Station_Ambulance  A :Station_Ambulance_list)
		{
			BuildStaticFuction.Generate_Shortest_Path_Clearing(context, geography , incNode , A.Node , true);
			BuildStaticFuction.Generate_Shortest_Path_Clearing(context, geography , PEANode , A.Node , true);	

			BuildStaticFuction.Generate_Shortest_Path_Clearing(context, geography ,  A.Node ,PEANode  , true);
			BuildStaticFuction.Generate_Shortest_Path_Clearing(context, geography ,  A.Node,PEANode  , true);

		}

		for (Station_Fire  F :Station_Fire_list)
		{
			BuildStaticFuction.Generate_Shortest_Path_Clearing(context, geography , incNode , F.Node , true);
			BuildStaticFuction.Generate_Shortest_Path_Clearing(context, geography , PEANode , F.Node , true);	

			BuildStaticFuction.Generate_Shortest_Path_Clearing(context, geography ,F.Node,incNode , true);
			BuildStaticFuction.Generate_Shortest_Path_Clearing(context, geography , F.Node, PEANode , true);
		}

		for (Station_Police  P :Station_Police_list)
		{
			BuildStaticFuction.Generate_Shortest_Path_Clearing(context, geography , incNode , P.Node , true);
			BuildStaticFuction.Generate_Shortest_Path_Clearing(context, geography , PEANode , P.Node , true);	

			BuildStaticFuction.Generate_Shortest_Path_Clearing(context, geography , P.Node ,incNode ,  true);
			BuildStaticFuction.Generate_Shortest_Path_Clearing(context, geography , P.Node ,PEANode , true);
		}


		@SuppressWarnings("unchecked") 
		List<RoadNode> nearObjects_RoadNode  = (List<RoadNode>) GetObjectsWithinDistance(incNode  , RoadNode.class , 1000 );
		@SuppressWarnings("unchecked") 
		List<RoadLink> nearObjects_RoadLink  = (List<RoadLink>) GetObjectsWithinDistance(incNode  , RoadLink.class , 1000 );

		for (RoadNode RN : nearObjects_RoadNode ) 
			RN.WillbeusedinNW=true;

		for (RoadLink RL : nearObjects_RoadLink ) 
			RL.WillbeusedinNW=true;


		Iterable<?> nearObjects = geography.getAllObjects();
		ArrayList<Object > notused =new ArrayList<Object >();  

		for (Object obj : nearObjects) 
		{
			boolean result=true;

			//node
			if (obj instanceof RoadNode) 
			{			
				if (     (( RoadNode) obj).WillbeusedinNW ==true )
					result=false;

				if (result)
					notused.add(obj);

			}
			
			if (obj instanceof RoadLink) 
			{
				if (     ((RoadLink) obj).WillbeusedinNW ==true )				
					result=false;

				if (result)
					notused.add(obj);

			}

		}

		System.out.println( "clear:" + notused.size());

		for ( Object obj : notused   )
			context.remove(obj);

		nearObjects=null;
		nearObjects_RoadNode.clear();
		nearObjects_RoadNode =null;
		nearObjects_RoadLink.clear();
		nearObjects_RoadLink =null;
		notused.clear();
		System.gc() ;

	}

	//---------------------------------------------------------------------------------
	public static void clearExtraNode(  Context context ,Geography<Object> _geography)
	{

		Iterable<?> nearObjects = _geography.getAllObjects();
		ArrayList<Object > notused =new ArrayList<Object >();  



		for (Object objN : nearObjects) 
		{

			if (objN instanceof RoadNode) 
			{
				boolean result=true;
				for (Object objL : nearObjects)				
				{
					if (objL instanceof RoadLink) 
						if (     ((RoadLink) objL).source==objN  ||   ((RoadLink) objL).target==objN )
							result=false;
				}

				if (result)
					notused.add(objN);

			}
		}

		System.out.println( "clear:" + notused.size());

		for ( Object obj : notused   )
			context.remove(obj);

		nearObjects=null;
		notused.clear();
		System.gc() ;
	}

	//---------------------------------------------------------------------------------
	public static void clearExtraTArea(  Context context ,Geography<Object> _geography)
	{
		Iterable<?> nearObjects = _geography.getAllObjects();
		ArrayList<Object > notused =new ArrayList<Object >();  

		for (Object objTA : nearObjects) 
		{
			if (objTA instanceof TopographicArea) 
			{
				boolean result=true;
				if (((TopographicArea)objTA).used_byprogram == true ) 
					result=false;

				if (result)
					notused.add(objTA);
			}
		}

		for ( Object obj : notused   )
			context.remove(obj);

		System.out.println( "clear:" + notused.size());

		nearObjects=null;
		notused.clear();
		System.gc() ;
	}

	//---------------------------------------------------------------------------------
	public static void IdentifyNear_Building( Incident incd , Context context ,Geography<Object> _geography)
	{

		@SuppressWarnings("unchecked") 
		List<TopographicArea> nearObjects = (List<TopographicArea>) BuildStaticFuction.GetObjectsWithinDistance(incd,TopographicArea.class, 300 );


		// 2- Remove all non-building objects to get a list of obstacles such as building 
		for (int i = 0; i < nearObjects.size(); i++) {
			if (nearObjects.get(i).getcode() == 4) {
				nearObjects.get(i).used_byprogram=true;			
			}
		}

		nearObjects.clear();
		nearObjects=null;
		System.gc() ;
	}

	//---------------------------------------------------------------------------------
	public static  RoadNode GetRoadNodeofthisLocation( Geography<Object> _geography, String Node_name)
	{
		RoadNode RN=null;

		Iterable<?> nearObjects =  _geography.getAllObjects();

		for (Object o : nearObjects) 
		{
			if (o instanceof RoadNode)
			{
				RoadNode x = (RoadNode) o;
				if (x.fid.equals(Node_name)) {
					RN=  x;
					break;
				}

			}
		}

		nearObjects=null;
		return RN ;
	}

	//---------------------------------------------------------------------------------
	public static  TopographicArea GetTAofthisLocation( Geography<Object> _geography, String Topicgraphic_name)
	{
		TopographicArea TA=null;
		Iterable<?> nearObjects =  _geography.getAllObjects();

		for (Object o : nearObjects) 
		{
			if (o instanceof TopographicArea )
			{		
				String n=((TopographicArea) o).fid;
				if (n.equals(Topicgraphic_name) )
				{	TA=  (TopographicArea) o;break;}

			}
		}

		nearObjects=null;
		((TopographicArea)TA).used_byprogram = true;
		return TA ;
	}

	//---------------------------------------------------------------------------------
	public static  RoadLink GetRoadLinkeofthisLocation( Geography<Object> _geography, String RL_name)
	{
		RoadLink RL=null;

		Iterable<?> nearObjects =  _geography.getAllObjects();

		for (Object o : nearObjects) 
		{
			if (o instanceof RoadLink)
			{
				RoadLink x = (RoadLink) o;
				if (x.fid.equals(RL_name)) {
					RL=  x;
					break;
				}

			}
		}



		nearObjects=null;

		return RL ;
	}

	//---------------------------------------------------------------------------------
	///Calculates angle in radians between two points in Coordinate
	public static double AngleBetween2CartesianPoints(Coordinate start, Coordinate end) {

		double angle = Math.atan2((end.y - start.y), (end.x - start.x));

		if (angle <= 0) {
			return (2 * Math.PI + angle);
		} else {
			return (angle);
		}
	};

	//---------------------------------------------------------------------------------
	public static double DistanceC(Geography geography,Coordinate c1, Coordinate c2) {
		double d = -1;
		try {
			d = JTS.orthodromicDistance(c1, c2, geography.getCRS());
		} catch (TransformException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return d;
	};

	//---------------------------------------------------------------------------------
	public static double DistanceP(Geography geography, Point p1, Point p2) {
		double d = -1;
		try {
			d = JTS.orthodromicDistance(p1.getCoordinate(), p2.getCoordinate(), geography.getCRS());
		} catch (TransformException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return d;
	};	

	//---------------------------------------------------------------------------------
	/** // @author Eric Tatara
	 * Generates a list of polygon geometries that defines the search area around vehicle This implementation creates a set of "pie slices" within a 
	 * circle around the vehicle .  center the current vehicle  location coordinate a list of polygon geometries
	 */							
	public static List<Object_Zone> GenerateSearchZones(Geography geography,Context context, Coordinate center, 
			int numZones,double radius,Object parent){
		// Search zone agent list that defines the search radius area for this Vehicle
		List<Object_Zone> searchZoneAgents = new ArrayList<Object_Zone>();
		GeometricShapeFactory gsf = new GeometricShapeFactory();

		// The radian interval used to divide the search circle area
		double interval = 2 * Math.PI / numZones;

		int numArcPoints = 10; // Num points in the arc (precision)
		gsf = new GeometricShapeFactory();
		gsf.setCentre(center);
		gsf.setSize(radius); 
		gsf.setNumPoints(numArcPoints); 

		int ZoneID=1;
		GeometryFactory fac = new GeometryFactory();
		// Generate a polygon geometry for each circle pie slice
		for (int i=0; i < numZones; i++){
			LineString arc = gsf.createArc(i*interval, interval);

			Coordinate[] coords = new Coordinate[numArcPoints + 2];

			coords[0] = center;
			coords [numArcPoints + 2 - 1] = center;

			for (int j=0; j<numArcPoints; j++){
				coords[j+1] = arc.getCoordinateN(j);
			}

			Object_Zone zoneAgent = new Object_Zone(parent);
			zoneAgent.ID=ZoneID;			
			zoneAgent.SidePoint=coords[coords.length/2];
			if (coords[coords.length/2]== null)
				System.out.println("helllo 11111111111111111111111111111111111111111111111111111");
			// Don't initially show search zones in display for Vehicle agents
			zoneAgent.setVisible(false);
			searchZoneAgents.add(zoneAgent);
			context.add(zoneAgent);
			geography.move(zoneAgent, fac.createPolygon(coords));
			ZoneID++;
		}	
		return searchZoneAgents;
	}

	//---------------------------------------------------------------------------------
	/**
	 * Returns a list of objects in the geography based on the source object, class
	 * type of objects to search for, and distance.  This approach is faster than
	 * using the Repast GeograpyWithin query since it uses the geography's internal
	 * spatial index to limit search results based on distance.  The GeoraphyWithin
	 * query in contrast compares the distance of all objects in the geography which
	 * will be slow when there are lots of objects.
	 * 
	 * This approach uses the reference envelope around a distance buffer around
	 * the source object's geometry, which for a single agent should be a point
	 * source with a circular buffer.  The envelope around the buffer is 
	 * rectangular so this should be used as a rough within distance result and
	 * refined further to check if the list of return objects fall within a more
	 * specific region contained within the reference envelope.
	 */
	public static List<?> GetObjectsWithinDistance(Object source, Class clazz, double searchDistance){

		Context context = ContextUtils.getContext(source);		
		Geography geography = (Geography)context.getProjection("Geography"); 		
		Geometry searchArea =  GeometryUtil.generateBuffer(geography, geography.getGeometry(source), searchDistance);

		Envelope searchEnvelope = searchArea.getEnvelopeInternal();
		Iterable<?> nearObjects = geography.getObjectsWithin(searchEnvelope, clazz);	
		List nearObjectList = new ArrayList();

		for (Object o : nearObjects){
			nearObjectList.add(o);

			//System.out.println("dddd");
		}

		nearObjects=null;
		return nearObjectList;
	}

	//---------------------------------------------------------------------------------
	//get random node around center coordinate and within squared area
	public static RoadNode GetRandomLocation(Geography geography,double _long,double _lat,double areaSize)
	{				
		Coordinate newPosition = new Coordinate(_long,_lat);
		String ambulance_location="";
		Iterable<RoadNode> Objects =  geography.queryInexact(new Envelope(newPosition.x-areaSize,newPosition.x+areaSize,newPosition.y-areaSize,newPosition.y+areaSize),RoadNode.class);
		List<RoadNode> list=StreamSupport.stream(Objects.spliterator(), false)
				.collect(Collectors.toList());
		if(list.size()>0)
		{
			SimUtilities.shuffle(list,RandomHelper.getUniform());
			return list.get(0);
		}
		return null;
	}

	//---------------------------------------------------------------------------------
	// identify short path based on distance using Node
	public static double Generate_Shortest_Path_byusingNode(Context<Object> context, Geography<Object> geography ,RoadNode source_node, RoadNode  target_node, boolean IsCheckOnly) {

		List<RepastEdge<RoadNode>> sequenced_path; // of link and its nodes
		List<RoadLink> listpath_link; //links
		Path PathAgent; // for visualization only  green path
		RoadNode  x;


		Network<RoadNode> Road_network = (Network<RoadNode>) context.getProjection("Road_network");

		//--------------------------------------
		// 1- get a near Node object to locations search based on near node not coordinate  of location because the given is field not objects

		Iterable<?> nearObjects = geography.getAllObjects();

		//--------------------------------------
		// 2- get a ShortestPath by using dijkstra's algorithm
		ShortestPath<RoadNode> p = new ShortestPath<RoadNode>(Road_network);
		double L = p.getPathLength(source_node, target_node);

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
				edg_temp = new RepastEdge<RoadNode>((RoadNode) edg.getTarget(), (RoadNode) edg.getSource(), false,
						(double) edg.getWeight());
				sequenced_path.add(edg_temp);
				node_temp = (RoadNode) edg.getSource();
			}
		}

		nearObjects=null;
		sequenced_path.clear();
		sequenced_path=null;
		listpath_link.clear();
		listpath_link=null;
		//--------------------------------------
		//4- testing exam : return without building path agent nor search zone, since this is only a test call onlly
		GeometryFactory fac = new GeometryFactory();
		Geometry geom_path = fac.buildGeometry(listpath_geom);


		if (geom_path instanceof MultiLineString) 
		{
			nearObjects=null;

			if (IsCheckOnly)// return without building path since this is only a test call
				return L;
			else {

				// Add PathAgent in context to visualization
				MultiLineString SHP = (MultiLineString) geom_path;
				PathAgent = new Path( listpath_geom.size(), L);
				context.add(PathAgent);
				geography.move(PathAgent, SHP);

				return L;
			}
		}
		return -1; // Otherwise if there is no path 
	};

	//---------------------------------------------------------------------------------
	// identify short path sequenced_pathbased on distance using Node
	public static double Generate_Shortest_Path_Clearing(Context<Object> context, Geography<Object> geography ,RoadNode source_node, RoadNode  target_node, boolean IsCheckOnly) {

		List<RepastEdge<RoadNode>> sequenced_path; // of link and its nodes
		List<RoadLink> listpath_link; //links
		Path PathAgent; // for visualization only  green path
		RoadNode  x;


		Network<RoadNode> Road_network = (Network<RoadNode>) context.getProjection("Road_network");

		//--------------------------------------
		// 1- get a near Node object to locations search based on near node not coordinate  of location because the given is field not objects

		Iterable<?> nearObjects = geography.getAllObjects();

		//--------------------------------------
		// 2- get a ShortestPath by using dijkstra's algorithm
		ShortestPath<RoadNode> p = new ShortestPath<RoadNode>(Road_network);
		double L = p.getPathLength(source_node, target_node);

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
				edg_temp = new RepastEdge<RoadNode>((RoadNode) edg.getTarget(), (RoadNode) edg.getSource(), false,
						(double) edg.getWeight());
				sequenced_path.add(edg_temp);
				node_temp = (RoadNode) edg.getSource();
			}
		}



		//------------------------------------------------

		for ( RepastEdge edg_used   :sequenced_path )
		{
			( (RoadNode) edg_used.getSource()).WillbeusedinNW=true; 
			( (RoadNode) edg_used.getTarget()).WillbeusedinNW=true;  
		}

		for(RoadLink   RL_used : listpath_link )
		{
			RL_used .WillbeusedinNW=true;
			//RL_used.ColorCode= 2 ;
		}

		nearObjects=null;
		sequenced_path.clear();
		sequenced_path=null;
		listpath_link.clear();
		listpath_link=null;

		//------------------------------------------------
		//4- testing exam : return without building path agent nor search zone, since this is only a test call onlly
		GeometryFactory fac = new GeometryFactory();
		Geometry geom_path = fac.buildGeometry(listpath_geom);


		if (geom_path instanceof MultiLineString) 
		{
			nearObjects=null;

			if (IsCheckOnly)// return without building path since this is only a test call
				return L;
			else {

				// Add PathAgent in context to visualization
				MultiLineString SHP = (MultiLineString) geom_path;
				PathAgent = new Path( listpath_geom.size(), L);
				context.add(PathAgent);
				geography.move(PathAgent, SHP);

				return L;
			}
		}
		return -1; // Otherwise if there is no path 
	};

	/*
	 * public static List<Sector> GenerateSectorss(Geography geography,Context
	 * context, Coordinate center, int numSectors,double radiusincident,Object
	 * parent){
	 * 
	 * 
	 * List<Sector> SearchSectorList = new ArrayList<Sector>();
	 * GeometricShapeFactory gsf = new GeometricShapeFactory();
	 * 
	 * // The radian interval used to divide the search circle area double interval
	 * = 2 * Math.PI / numSectors;
	 * 
	 * int numArcPoints = 10; // Num points in the arc (precision) gsf = new
	 * GeometricShapeFactory(); gsf.setCentre(center); gsf.setSize(radiusincident);
	 * gsf.setNumPoints(numArcPoints);
	 * 
	 * int SectorID=1; GeometryFactory fac = new GeometryFactory(); // Generate a
	 * polygon geometry for each circle pie slice for (int i=0; i < numSectors;
	 * i++){ LineString arc = gsf.createArc(i*interval, interval);
	 * 
	 * Coordinate[] coords = new Coordinate[numArcPoints + 2];
	 * 
	 * coords[0] = center; coords [numArcPoints + 2 - 1] = center;
	 * 
	 * for (int j=0; j<numArcPoints; j++){ coords[j+1] = arc.getCoordinateN(j); }
	 * 
	 * Sector SectorsAgent = new Sectors(); SectorsAgent.ID=SectorID;
	 * 
	 * SearchSectorList.add(SectorsAgent); context.add(SectorsAgent);
	 * geography.move(SectorsAgent, fac.createPolygon(coords)); SectorID++; } return
	 * SearchSectorList ; }
	 */

}
