package A_EmergencyResponse;

import com.vividsolutions.jts.geom.Coordinate;
import A_Agents.EOC;
import A_Agents.Hospital;
import A_Agents.Station_Ambulance;
import A_Agents.Station_Fire;
import A_Agents.Station_Police;
import A_Environment.Incident;
import A_Environment.RoadNode;
import C_SimulationInput.InputFile;
import D_Ontology.BuildStaticFuction;
import D_Ontology.Ontology.AmbulanceTracking;
import D_Ontology.Ontology.Callinformatiom;
import D_Ontology.Ontology.CasualtyReportandTrackingMechanism;
import D_Ontology.Ontology.GeolocationMechanism;
import D_Ontology.Ontology.STartEvacuationtoHospital_Startgy;
import D_Ontology.Ontology.SurveyScene_SiteExploration_Mechanism;
import D_Ontology.Ontology.TaskAllocationApproach;
import D_Ontology.Ontology.TaskAllocationMechanism;
import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.gis.Geography;

public class Scenario_MyFrined {
	
	Context<Object> context;
	Geography<Object> geography;		
	static int Incidentcounter=1;
	boolean Incident1_active=false;
	//---------------------------------------

	//EOC Instantiation
	Coordinate EOCCoordinate=new Coordinate(-1.58772788672538701, 54.95606439799289689);     //osgb4000000007592876
	EOC EOC1;
	Incident Incident1 ;

	//---------------------------------------------------------------------

	double  CurrentTick , CurrentTask_duration, EndofCurrentTask ;		
	ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule(); 	

	//*************************************************************************************************************************************

	//Instantiate the  Scenario3 in which  one responder  will go to the rescue the causality and then back to the ambulance vehicle and then hospital

	public Scenario_MyFrined(Context<Object> _context, Geography<Object> _geography)
	{		

		this.context=_context;
		this.geography=_geography;
		EOC1=new EOC(context,geography, EOCCoordinate,"osgb4000000007592876");

		// creation of Resources
		//EOC1.Senario1_1H1SP1SA1F(); 
		//EOC1.EnvironmentofSenario2_MS();
		
		EOC1.EnvironmentofSenario2_acurate();
		
		
		
		//----------------------------------------------------------------------------------------
		Incidentcounter=1;
	}

	//----------------------------------------------------------------------------------------
	// it represent sequence of events
	@ScheduledMethod(start = 1, interval = 1)
	public void step() {	

		CurrentTick=schedule.getTickCount() ;

		if (CurrentTick==3)	
		{	
			
			// clear extra link
			RoadNode   incNode= BuildStaticFuction.GetRoadNodeofthisLocation( geography, "osgb4000000007932484");
			RoadNode PEANode = BuildStaticFuction.GetRoadNodeofthisLocation(geography, "osgb4000000007592232");  	
			BuildStaticFuction.clearExtraNW( context ,geography  , incNode , PEANode ,EOC1.Hospital_list ,EOC1.Station_Ambulance_list,EOC1.Station_Fire_list ,EOC1.Station_Police_list);
			
			
			//"osgb4000000007593323" my friend
			Incident1 = new Incident("MyFrind_Incident",context, geography,"osgb4000000007932484","osgb1000002310066393" , 40 , 10,3 ,28); //40
			Incident1.EOC1= EOC1 ;	
			Incident1.Randomly_Location= false ;
			Incident1.Randomly_Severity= false ;
						
			switch ( InputFile.CurrentExp ) {			
			case Exp1 :				
				Incident1.TaskMechanism_IN=TaskAllocationMechanism.NoEnvironmentsectorization; Incident1.NOSector=1; 
				Incident1.Survey_scene_Mechanism_IN= SurveyScene_SiteExploration_Mechanism.Randomly_exploration; 
				
				Incident1.TaskApproach_IN=TaskAllocationApproach.CentralizedDirectSupervision;		
				Incident1.CasualtyReport_Mechanism_IN=CasualtyReportandTrackingMechanism.Paper;  
				
				Incident1.AmbulanceTracking_Strategy=AmbulanceTracking.Radio ;					
				Incident1.GeolocationTacticalAreas = GeolocationMechanism.Papermap ;
				
				
				Incident1._STartEvacuationtoHospital_Startgy=STartEvacuationtoHospital_Startgy.CCS_MorepriorityinCSS ;
								
				break;
			case Exp2 :
				Incident1.TaskMechanism_IN=TaskAllocationMechanism.NoEnvironmentsectorization; Incident1.NOSector=1; 	
				Incident1.Survey_scene_Mechanism_IN= SurveyScene_SiteExploration_Mechanism.Randomly_exploration; 
				
				Incident1.TaskApproach_IN=TaskAllocationApproach.CentralizedDirectSupervision;				
				Incident1.CasualtyReport_Mechanism_IN=CasualtyReportandTrackingMechanism.Electronic;
				
				Incident1.AmbulanceTracking_Strategy=AmbulanceTracking.Radio ;				
				Incident1.GeolocationTacticalAreas = GeolocationMechanism.Papermap ;
				
				Incident1._STartEvacuationtoHospital_Startgy=STartEvacuationtoHospital_Startgy.CCS_MorepriorityinCSS ;
				
				break;
			case Exp3 :
				Incident1.TaskMechanism_IN=TaskAllocationMechanism.NoEnvironmentsectorization; Incident1.NOSector=1; 	
				Incident1.Survey_scene_Mechanism_IN= SurveyScene_SiteExploration_Mechanism.Randomly_exploration;
				
				Incident1.TaskApproach_IN=TaskAllocationApproach.Decentralized_Autonomous;				
				Incident1.CasualtyReport_Mechanism_IN=CasualtyReportandTrackingMechanism.Paper;
				
				Incident1.AmbulanceTracking_Strategy=AmbulanceTracking.Radio ;					
				Incident1.GeolocationTacticalAreas = GeolocationMechanism.Papermap ;
				
				Incident1._STartEvacuationtoHospital_Startgy=STartEvacuationtoHospital_Startgy.CCS_MorepriorityinCSS ;
				
				break;
			case Exp4 :
				Incident1.TaskMechanism_IN=TaskAllocationMechanism.NoEnvironmentsectorization; Incident1.NOSector=1; 	
				Incident1.Survey_scene_Mechanism_IN= SurveyScene_SiteExploration_Mechanism.Randomly_exploration;
				
				Incident1.TaskApproach_IN=TaskAllocationApproach.Decentralized_Autonomous;			
				Incident1.CasualtyReport_Mechanism_IN=CasualtyReportandTrackingMechanism.Electronic;
				
				Incident1.AmbulanceTracking_Strategy=AmbulanceTracking.Radio ;				
				Incident1.GeolocationTacticalAreas = GeolocationMechanism.Papermap ;
				
				Incident1._STartEvacuationtoHospital_Startgy=STartEvacuationtoHospital_Startgy.CCS_MorepriorityinCSS ;
				
				break;
			case Exp5 :
				Incident1.TaskMechanism_IN=TaskAllocationMechanism.Environmentsectorization; Incident1.NOSector=4; 	
				Incident1.Survey_scene_Mechanism_IN= SurveyScene_SiteExploration_Mechanism.Randomly_exploration;
				
				Incident1.TaskApproach_IN=TaskAllocationApproach.CentralizedDirectSupervision;				
				Incident1.CasualtyReport_Mechanism_IN=CasualtyReportandTrackingMechanism.Paper;
				
				Incident1.AmbulanceTracking_Strategy=AmbulanceTracking.GPS;				
				Incident1.GeolocationTacticalAreas = GeolocationMechanism.GPSmap ;
				
				Incident1._STartEvacuationtoHospital_Startgy=STartEvacuationtoHospital_Startgy.CCS_MorepriorityinCSS ;
				
				break;
			case Exp6 :
				Incident1.TaskMechanism_IN=TaskAllocationMechanism.Environmentsectorization; Incident1.NOSector=4; 	
				Incident1.Survey_scene_Mechanism_IN= SurveyScene_SiteExploration_Mechanism.Randomly_exploration;
				
				Incident1.TaskApproach_IN=TaskAllocationApproach.CentralizedDirectSupervision;				
				Incident1.CasualtyReport_Mechanism_IN=CasualtyReportandTrackingMechanism.Electronic;
				
				Incident1.AmbulanceTracking_Strategy=AmbulanceTracking.GPS;				
				Incident1.GeolocationTacticalAreas = GeolocationMechanism.GPSmap ;
				
				Incident1._STartEvacuationtoHospital_Startgy=STartEvacuationtoHospital_Startgy.CCS_MorepriorityinCSS ;
				
				break;
			case Exp7 :
				Incident1.TaskMechanism_IN=TaskAllocationMechanism.Environmentsectorization; Incident1.NOSector=4; 	
				Incident1.Survey_scene_Mechanism_IN= SurveyScene_SiteExploration_Mechanism.Randomly_exploration;
				
				Incident1.TaskApproach_IN=TaskAllocationApproach.Decentralized_Autonomous;				
				Incident1.CasualtyReport_Mechanism_IN=CasualtyReportandTrackingMechanism.Paper;
				
				Incident1.AmbulanceTracking_Strategy=AmbulanceTracking.GPS;				
				Incident1.GeolocationTacticalAreas = GeolocationMechanism.GPSmap ;
				
				Incident1._STartEvacuationtoHospital_Startgy=STartEvacuationtoHospital_Startgy.CCS_MorepriorityinCSS ;
				
				break;
			case Exp8 :
				Incident1.TaskMechanism_IN=TaskAllocationMechanism.Environmentsectorization; Incident1.NOSector=4; 	
				Incident1.Survey_scene_Mechanism_IN= SurveyScene_SiteExploration_Mechanism.Randomly_exploration;
				
				Incident1.TaskApproach_IN=TaskAllocationApproach.Decentralized_Autonomous;			
				Incident1.CasualtyReport_Mechanism_IN=CasualtyReportandTrackingMechanism.Electronic;
				
				Incident1.AmbulanceTracking_Strategy=AmbulanceTracking.GPS;				
				Incident1.GeolocationTacticalAreas = GeolocationMechanism.GPSmap ;
				
				Incident1._STartEvacuationtoHospital_Startgy=STartEvacuationtoHospital_Startgy.CCS_MorepriorityinCSS;
				
				break;}
			
							
			Incident1.Onset();				
			this.EndofCurrentTask= CurrentTick + 120 ; //120 2m
			Incident1_active= true;
			
			//print casualty
			//for (int i = 0; i < 6 ; i++) 
			//System.out.println( "new Coordinate ("+ Incident1.casualties.get(i).getCurrentLocation().x+ ","+ Incident1.casualties.get(i).getCurrentLocation().y + ")," );	
						
		}

		else if ( Incident1_active && EndofCurrentTask== CurrentTick )
		{
			EOC1.call999(Incident1,Callinformatiom.ThereisInformation); // information 
			System.out.println("EOC get call from Incident  1 ");
			boolean Incident1_active=false;
			
		}
	
	}// step


}
