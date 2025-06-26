package A_EmergencyResponse;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import A_Environment.RoadLink;
import A_Environment.RoadNode;
import A_Environment.TopographicArea;
import D_Ontology.BuildStaticFuction;
import repast.simphony.context.Context;
import repast.simphony.context.space.gis.GeographyFactoryFinder;
import repast.simphony.context.space.graph.NetworkBuilder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.space.gis.Geography;
import repast.simphony.space.gis.GeographyParameters;
import repast.simphony.space.graph.Network;

public class ER_ContextBuilder implements ContextBuilder<Object> {

	@Override
	public Context build(Context<Object> context) {


		// Create the Geography projection that is used to store geographic locations of agents in the model.
		GeographyParameters geoParams = new GeographyParameters();			
		Geography<Object> geography = GeographyFactoryFinder.createGeographyFactory(null).createGeography("Geography", context,geoParams);		

		// Create the Network projection that is used to create the Road network.
		NetworkBuilder<Object> netBuilder = new NetworkBuilder<Object>("Road_network", context, false);
		Network Road_network = netBuilder.buildNetwork();
		
		NetworkBuilder<Object> netBuilder3 = new NetworkBuilder<Object>("cordon_network", context, false);  // for each incdent 
		Network cordon_network = netBuilder3.buildNetwork();
		
		// Create the Network projection that is used to create the organizational structure or Comunication_network
		NetworkBuilder<Object> netBuilder2 = new NetworkBuilder<Object>("Comunication_network", context, false);
		Network Comunication_network = netBuilder2.buildNetwork();

		// here or in run we have to build this network
		// ----------------------------------------Basic---------------------------------
		// Load Features from Shape Files 
//loadFeatures("data/G_RoadNode.shp", context, geography, Road_network);
//loadFeatures("data/G_RoadLink.shp", context, geography, Road_network);
		loadFeatures("data/TopograpgicArea.shp", context, geography,Road_network );	
		// System.out.println("CRS " + geography.getCRS());
		// ----------------------------------------Basic---------------------------------

		// ----------------------------------------Case study----------------------------   9148
loadFeatures("data2/NG_RoadNode.shp", context, geography, Road_network); //night
loadFeatures("data2/NG_RoadLink.shp", context, geography, Road_network);
		//loadFeatures("data2/NG_TopograpgicArea.shp", context, geography,Road_network );	

		// ----------------------------------------Case study---------------------------- 25548 this one used
//loadFeatures("data3/NZ26_RN.shp", context, geography, Road_network); //Morning
//loadFeatures("data3/NZ26_RL.shp", context, geography, Road_network);
		// loadFeatures("data3/NZ26_TA.shp", context, geography,Road_network );	

		// ----------------------------------------Case study---------------------------- 44000
		//loadFeatures("data5/NZ_big_RN.shp", context, geography, Road_network);
		//loadFeatures("data5/NZ_big_RL.shp", context, geography, Road_network); 
		//loadFeatures("data5/GST.shp", context, geography,Road_network );	
		//loadFeatures("data5/CST.shp", context, geography,Road_network );

		// ----------------------------------------Case study---------------------------
		
		BuildStaticFuction.clearExtraNode( context,geography   ) ;
		
		//Instantiate the first Scenario1 ------------------------------------------------		
		//Scenario_MyFrined newScenario=new Scenario_MyFrined(context,geography);			
		//context.add(newScenario);
							
		//Instantiate the Scenario2 -----------------------------------------------------
		//Scenario_MultipleIncidents newScenario=new Scenario_MultipleIncidents(context,geography);			
		//context.add(newScenario);
		
		//Instantiate the first Scenario3 ------------------------------------------------		
		Scenario_InformationalDependency newScenario=new Scenario_InformationalDependency(context,geography);			
		context.add(newScenario);

		return context;
	}

	private void loadFeatures(String filename, Context context, Geography geography, Network Road_network) {

		List<SimpleFeature> features = loadFeaturesFromShapefile(filename);
		int i = 0;

		
		// For each feature in the file
		for (SimpleFeature feature : features) {
			Geometry geom = (Geometry) feature.getDefaultGeometry();
			Object agent = null;

			if (!geom.isValid()) {
				System.out.println("Invalid geometry: " + feature.getID());
			}
			//==================================1=======================================
			// For Polygons, create Area
			if (geom instanceof MultiPolygon) {
				MultiPolygon mp = (MultiPolygon) feature.getDefaultGeometry();
				geom = (Polygon) mp.getGeometryN(0);

				// Read the feature attributes and assign to the TopograpgicArea class				
				String descriptiv1 = (String) feature.getAttribute("descriptiv");

				// Exclude other features during the environment creation, or we can removed them before from Shapefile in QGIS
			
//				if (  descriptiv1.equals((String) "Building") || descriptiv1.equals((String) "Rail")||
//						descriptiv1.equals((String) "Road Or Track") ||descriptiv1.equals((String) "Structure,Road Or Track")|| descriptiv1.equals((String) "Structure,General Surface")||   
//						descriptiv1.equals((String) "Path") || descriptiv1.equals((String) "Path,Structure ") ||
//						descriptiv1.equals((String) "Roadside") || descriptiv1.equals((String) "Roadside,Structure") ||descriptiv1.equals((String) "Roadside,Path")||descriptiv1.equals((String) "Structure,Roadside")||
//						descriptiv1.equals((String) "Structures,Roads Tracks And Paths,Land") ||
//						descriptiv1.equals((String) "Natural Environment") || descriptiv1.equals((String) "Rail,Natural Environment")  ||  descriptiv1.equals((String) "Natural Environment,Rail")  ||
//						descriptiv1.equals((String) "General Surface")
//						 )
				
				
				
				{
					String fid1 = (String) feature.getAttribute("fid");
					String FeatureCod1 = (String) feature.getAttribute("FeatureCod");
					String theme1 = (String) feature.getAttribute("theme");
					//double calculated1 = (double) feature.getAttribute("calculated");		
					double calculated1=0;					
					agent = new TopographicArea(fid1,calculated1,FeatureCod1, theme1, descriptiv1,context,geography);
						
					//System.out.println("done  "+ fid1 + " - "); 
				}
			}
			
			//=====================================2====================================
			// For Points, create RoadNode
			else if (geom instanceof Point) {
				geom = (Point) feature.getDefaultGeometry();

				// Read the feature attributes and assign to RoadNode class
				String fid1 = (String) feature.getAttribute("fid");

				agent = new RoadNode(fid1, context,geography );
				// System.out.println(" Point/ " + geom.getNumPoints() + " "+ geom.getCoordinate().x+" "+ geom.getCoordinate().y);

			}
			//====================================3=====================================
			// For Lines, create RoadLine
			else if (geom instanceof MultiLineString) {
				MultiLineString line = (MultiLineString) feature.getDefaultGeometry();
				geom = (LineString) line.getGeometryN(0);

				// Read the feature attributes and assign to the RoadLine class
				String fid1 = (String) feature.getAttribute("fid");
				String theme1 = (String) feature.getAttribute("theme");
				String descript_11 = (String) feature.getAttribute("descript_1");
				String natureOfRo1 = (String) feature.getAttribute("natureOfRo");
				double length1 = (double) feature.getAttribute("length");
		
				// build Road Network 
				boolean first_time = true;
				RoadNode source = null, target = null;
				Iterable<?> nearObjects = geography.getAllObjects();
				for (Object obj : nearObjects) {
					if (obj instanceof RoadNode) {
						Geometry geomp = (Point) geography.getGeometry(obj);
						if (geom.intersects(geomp)) {
							//							System.out.println("   line: " + i + " " + geom.getNumPoints() + "  "
							//									+ geom.getCoordinates()[0].x + "   " + geom.getCoordinates()[0].y);
							//							System.out.println("   line: " + i + "  " + geom.getCoordinates()[geom.getNumPoints() - 1].x
							//									+ "   " + geom.getCoordinates()[geom.getNumPoints() - 1].y);
							//							System.out.println("point" + geomo.getCoordinate().x + " " + geomo.getCoordinate().y);

							if (first_time) {
								source = (RoadNode) obj;
								first_time = false;
							} else {
								target = (RoadNode) obj;
							}
						}
					}
				}
				
				if ((target != null) && (source != null )) {	
					agent = new RoadLink(fid1,theme1, descript_11, natureOfRo1, length1,source,target);
					Road_network.addEdge(source, target, length1);					
					//System.out.println("done  "+ i + " - 44377  or 25548 " + length1); 
				} else  agent = null;
			}
			//=========================================================================	
			
			if (agent != null) 
			{
				context.add(agent);
				geography.move(agent, geom);
			} else 
			{
				// System.out.println("Error creating agent for " + geom);
			}
			
			
			i++;
		} // end  for 
	}

	private List<SimpleFeature> loadFeaturesFromShapefile(String filename) {
		URL url = null;
		try {
			url = new File(filename).toURL();
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		}

		List<SimpleFeature> features = new ArrayList<SimpleFeature>();

		// Try to load the Shape File
		SimpleFeatureIterator fiter = null;
		ShapefileDataStore store = null;
		store = new ShapefileDataStore(url);

		try {
			fiter = store.getFeatureSource().getFeatures().features();

			while (fiter.hasNext()) {
				features.add(fiter.next());
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			fiter.close();
			store.dispose();
		}
		return features;
	}
}
