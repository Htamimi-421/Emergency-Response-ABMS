package A_EmergencyResponse;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import A_Agents.EOC;
import A_Environment.Incident;
import C_SimulationInput.InputFile;
import D_Ontology.Ontology.Callinformatiom;
import D_Ontology.Ontology.CasualtyReportandTrackingMechanism;
import D_Ontology.Ontology.SurveyScene_SiteExploration_Mechanism;
import D_Ontology.Ontology.TaskAllocationApproach;
import D_Ontology.Ontology.TaskAllocationMechanism;
import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.gis.Geography;

public class Scenario_MultipleIncidents {


	Context<Object> context;
	Geography<Object> geography;		
	public static Envelope areaOfConcern=new Envelope(-1.588,-1.5754,54.9563,54.9614); //Project Map Envelope
	public static double areaSize=0.0003;//Size of area in which we look for a random location near a center coordinate
	static int Incidentcounter=1;
	boolean Incident1_active=false;
	boolean Incident2_active=false;
	boolean Incident3_active=false;
	//---------------------------------------

	//EOC Instantiation
	Coordinate EOCCoordinate=new Coordinate(-1.58772788672538701, 54.95606439799289689);     //osgb4000000007592876
	EOC EOC1;
	Incident Incident1 ,Incident2, Incident3;

	//---------------------------------------------------------------------

	double  CurrentTick ,  EndofCurrentTask1 ,  EndofCurrentTask2 ;		
	ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule(); 	

	//*************************************************************************************************************************************

	//Instantiate the  Scenario3 in which  one responder  will go to the rescue the causality and then back to the ambulance vehicle and then hospital

	public Scenario_MultipleIncidents(Context<Object> _context, Geography<Object> _geography)
	{		
		this.context=_context;
		this.geography=_geography;
		EOC1=new EOC(context,geography, EOCCoordinate,"osgb4000000007592876");

		// creation of environment
	//	EOC1.Senario1_1H1SP1SA1F();
		EOC1.EnvironmentofSenario2_MS();

		//----------------------------------------------------------------------------------------
		Incidentcounter=1;

	}

	//----------------------------------------------------------------------------------------
	// it represent sequence of events
	@ScheduledMethod(start = 1, interval = 1)
	public void step() {	

		CurrentTick=schedule.getTickCount() ;

		if (CurrentTick==2)	
		{
			//Incident Instantiation	
			Incident1 = new Incident("NCS_Incident",context, geography, "osgb5000005157645330","osgb1000002310318115",40, 10,3,20);   // parking and location    osgb5000005157645334    osgb1000000060906649
			Incident1.EOC1= EOC1 ;
			Incident1.Randomly_Location=true ;
			Incident1.Randomly_Severity=true ;		
			
			Incident1.TaskMechanism_IN=TaskAllocationMechanism.Environmentsectorization; Incident1.NOSector=4; 	
			Incident1.TaskApproach_IN=TaskAllocationApproach.CentralizedDirectSupervision;				
			
								
			switch ( InputFile.CurrentExp ) {			
			case Exp1 :
				Incident1.Survey_scene_Mechanism_IN= SurveyScene_SiteExploration_Mechanism.Randomly_exploration;
				Incident1.CasualtyReport_Mechanism_IN=CasualtyReportandTrackingMechanism.Paper;
				break;
			case Exp2 :
				Incident1.Survey_scene_Mechanism_IN= SurveyScene_SiteExploration_Mechanism.Randomly_exploration;
				Incident1.CasualtyReport_Mechanism_IN=CasualtyReportandTrackingMechanism.Electronic;
				break;
			case Exp3 :
				Incident1.Survey_scene_Mechanism_IN= SurveyScene_SiteExploration_Mechanism.Organized_exploration;
				Incident1.CasualtyReport_Mechanism_IN=CasualtyReportandTrackingMechanism.Paper;
				break;
			case Exp4 :
				Incident1.Survey_scene_Mechanism_IN= SurveyScene_SiteExploration_Mechanism.Organized_exploration;
				Incident1.CasualtyReport_Mechanism_IN=CasualtyReportandTrackingMechanism.Electronic;
				break;}
//				break;
//			case Exp5 :
//				Incident1.Survey_scene_Mechanism_IN= SurveyScene_SiteExploration_Mechanism.Randomly_exploration;
//				Incident1.CasualtyReport_Mechanism_IN=CasualtyReportandTrackingMechanism.Paper;
//				break;
//			case Exp6 :
//				Incident1.Survey_scene_Mechanism_IN= SurveyScene_SiteExploration_Mechanism.Organized_exploration;
//				Incident1.CasualtyReport_Mechanism_IN=CasualtyReportandTrackingMechanism.Paper;
//				break;
//			case Exp7 :
//				Incident1.Survey_scene_Mechanism_IN= SurveyScene_SiteExploration_Mechanism.Randomly_exploration;
//				Incident1.CasualtyReport_Mechanism_IN=CasualtyReportandTrackingMechanism.Electronic;
//				break;
//			case Exp8 :
//				Incident1.Survey_scene_Mechanism_IN= SurveyScene_SiteExploration_Mechanism.Organized_exploration;
//				Incident1.CasualtyReport_Mechanism_IN=CasualtyReportandTrackingMechanism.Electronic;
				
				
			
			Incident1.Onset();				
			this.EndofCurrentTask1= CurrentTick + 120  ; //120 2m
			Incident1_active=true;
			
		//print casualty
			//System.out.println( "NCS");
		//for (int i = 0; i < 10 ; i++) 
			//System.out.println( "new Coordinate ("+ Incident1.casualties.get(i).getCurrentLocation().x+ ","+ Incident1.casualties.get(i).getCurrentLocation().y + ")," );	
		
		}
		 if ( Incident1_active && EndofCurrentTask1== CurrentTick )
		{
			EOC1.call999(Incident1 ,Callinformatiom.NoInformation ); // information 
			System.out.println("EOC get call from Incident  1 ");
			Incident1_active=false;
			
		}
		 if (CurrentTick==3)	
		{
			//Incident Instantiation	
			Incident2 = new Incident("GSS_Incident",context, geography, "osgb4000000007593236","osgb1000002310173121",40,10,0,20);  //     osgb1000000061149839
			Incident2.EOC1= EOC1 ;	
			Incident2.Randomly_Location=true ;
			Incident2.Randomly_Severity=true;
			
			Incident2.TaskMechanism_IN=TaskAllocationMechanism.Environmentsectorization; Incident2.NOSector=4; 	
			Incident2.TaskApproach_IN=TaskAllocationApproach.CentralizedDirectSupervision;	
			
			switch ( InputFile.CurrentExp ) {			
			case Exp1 :
				Incident2.Survey_scene_Mechanism_IN= SurveyScene_SiteExploration_Mechanism.Randomly_exploration;
				Incident2.CasualtyReport_Mechanism_IN=CasualtyReportandTrackingMechanism.Paper;
				break;
			case Exp2 :
				Incident2.Survey_scene_Mechanism_IN= SurveyScene_SiteExploration_Mechanism.Randomly_exploration;
				Incident2.CasualtyReport_Mechanism_IN=CasualtyReportandTrackingMechanism.Electronic;
				break;
			case Exp3 :
				Incident2.Survey_scene_Mechanism_IN= SurveyScene_SiteExploration_Mechanism.Organized_exploration;
				Incident2.CasualtyReport_Mechanism_IN=CasualtyReportandTrackingMechanism.Paper;
				break;
			case Exp4 :
				Incident2.Survey_scene_Mechanism_IN= SurveyScene_SiteExploration_Mechanism.Organized_exploration;
				Incident2.CasualtyReport_Mechanism_IN=CasualtyReportandTrackingMechanism.Electronic;
				break;}
//				break;
//			case Exp5 :
//				Incident2.Survey_scene_Mechanism_IN= SurveyScene_SiteExploration_Mechanism.Randomly_exploration;
//				Incident2.CasualtyReport_Mechanism_IN=CasualtyReportandTrackingMechanism.Paper;
//				break;
//			case Exp6 :
//				Incident2.Survey_scene_Mechanism_IN= SurveyScene_SiteExploration_Mechanism.Randomly_exploration;
//				Incident2.CasualtyReport_Mechanism_IN=CasualtyReportandTrackingMechanism.Paper;
//				break;
//			case Exp7 :
//				Incident2.Survey_scene_Mechanism_IN= SurveyScene_SiteExploration_Mechanism.Organized_exploration;
//				Incident2.CasualtyReport_Mechanism_IN=CasualtyReportandTrackingMechanism.Electronic;
//				break;
//			case Exp8 :
//				Incident2.Survey_scene_Mechanism_IN= SurveyScene_SiteExploration_Mechanism.Organized_exploration;
//				Incident2.CasualtyReport_Mechanism_IN=CasualtyReportandTrackingMechanism.Electronic;
			
			
			
			
			Incident2.Onset();				
			this.EndofCurrentTask2= CurrentTick + 120 ; //120 2m
			Incident2_active=true;	
			
		//System.out.println( "GG");
			//print casualty
			//for (int i = 0; i < 10 ; i++) 
			//System.out.println( "new Coordinate ("+ Incident2.casualties.get(i).getCurrentLocation().x+ ","+ Incident2.casualties.get(i).getCurrentLocation().y + ")," );	
			
		}
		 if ( Incident2_active	 && EndofCurrentTask2== CurrentTick )
		{
			EOC1.call999(Incident2 ,Callinformatiom.NoInformation ); // information 
			System.out.println("IEOC get call from Incident  2");
			Incident2_active=false;	

		}
		
//		else if (CurrentTick==20)	
//		{
//			//Incident Instantiation							
//			Incident3 = new Incident("Incident_" +Incidentcounter ,context, geography, "osgb4000000007593531",20,10);
//			Incident3.EOC1= EOC1 ;			
//			Incident3.Onset();				
//			this.EndofCurrentTask= CurrentTick + 3 ; 
//			Incident3_active=true;	
//		}
//		else if ( Incident3_active && EndofCurrentTask== CurrentTick )
//		{
//			EOC1.call999(Incident3);
//			System.out.println("Incident  3");
//			Incident3_active=false;	
//		}

	}// step

}





//==========================================================================================================


//switch ( InputFile.CurrentExp ) {			
//case Exp1 :
//	Incident1.Survey_scene_Mechanism_IN= SurveyScene_SiteExploration_Mechanism.Randomly_exploration;
//	Incident1.CasualtyReport_Mechanism_IN=CasualtyReportandTrackingMechanism.Electronic;
//	break;
//case Exp2 :
//	Incident1.Survey_scene_Mechanism_IN= SurveyScene_SiteExploration_Mechanism.Organized_exploration;
//	Incident1.CasualtyReport_Mechanism_IN=CasualtyReportandTrackingMechanism.Electronic;
//	break;
//case Exp3 :
//	Incident1.Survey_scene_Mechanism_IN= SurveyScene_SiteExploration_Mechanism.Randomly_exploration;
//	Incident1.CasualtyReport_Mechanism_IN=CasualtyReportandTrackingMechanism.Electronic;
//	break;
//case Exp4 :
//	Incident1.Survey_scene_Mechanism_IN= SurveyScene_SiteExploration_Mechanism.Organized_exploration;
//	Incident1.CasualtyReport_Mechanism_IN=CasualtyReportandTrackingMechanism.Electronic;
//	break;
//case Exp5 :
//	Incident1.Survey_scene_Mechanism_IN= SurveyScene_SiteExploration_Mechanism.Randomly_exploration;
//	Incident1.CasualtyReport_Mechanism_IN=CasualtyReportandTrackingMechanism.Paper;
//	break;
//case Exp6 :
//	Incident1.Survey_scene_Mechanism_IN= SurveyScene_SiteExploration_Mechanism.Organized_exploration;
//	Incident1.CasualtyReport_Mechanism_IN=CasualtyReportandTrackingMechanism.Paper;
//	break;
//case Exp7 :
//	Incident1.Survey_scene_Mechanism_IN= SurveyScene_SiteExploration_Mechanism.Randomly_exploration;
//	Incident1.CasualtyReport_Mechanism_IN=CasualtyReportandTrackingMechanism.Paper;
//	break;
//case Exp8 :
//	Incident1.Survey_scene_Mechanism_IN= SurveyScene_SiteExploration_Mechanism.Organized_exploration;
//	Incident1.CasualtyReport_Mechanism_IN=CasualtyReportandTrackingMechanism.Paper;
//	break;}
//
//
//switch ( InputFile.CurrentExp ) {			
//case Exp1 :
//	Incident2.Survey_scene_Mechanism_IN= SurveyScene_SiteExploration_Mechanism.Randomly_exploration;
//	Incident2.CasualtyReport_Mechanism_IN=CasualtyReportandTrackingMechanism.Paper;
//	break;
//case Exp2 :
//	Incident2.Survey_scene_Mechanism_IN= SurveyScene_SiteExploration_Mechanism.Randomly_exploration;
//	Incident2.CasualtyReport_Mechanism_IN=CasualtyReportandTrackingMechanism.Paper;
//	break;
//case Exp3 :
//	Incident2.Survey_scene_Mechanism_IN= SurveyScene_SiteExploration_Mechanism.Organized_exploration;
//	Incident2.CasualtyReport_Mechanism_IN=CasualtyReportandTrackingMechanism.Paper;
//	break;
//case Exp4 :
//	Incident2.Survey_scene_Mechanism_IN= SurveyScene_SiteExploration_Mechanism.Organized_exploration;
//	Incident2.CasualtyReport_Mechanism_IN=CasualtyReportandTrackingMechanism.Paper;
//	break;
//case Exp5 :
//	Incident2.Survey_scene_Mechanism_IN= SurveyScene_SiteExploration_Mechanism.Randomly_exploration;
//	Incident2.CasualtyReport_Mechanism_IN=CasualtyReportandTrackingMechanism.Electronic;
//	break;
//case Exp6 :
//	Incident2.Survey_scene_Mechanism_IN= SurveyScene_SiteExploration_Mechanism.Randomly_exploration;
//	Incident2.CasualtyReport_Mechanism_IN=CasualtyReportandTrackingMechanism.Electronic;
//	break;
//case Exp7 :
//	Incident2.Survey_scene_Mechanism_IN= SurveyScene_SiteExploration_Mechanism.Organized_exploration;
//	Incident2.CasualtyReport_Mechanism_IN=CasualtyReportandTrackingMechanism.Electronic;
//	break;
//case Exp8 :
//	Incident2.Survey_scene_Mechanism_IN= SurveyScene_SiteExploration_Mechanism.Organized_exploration;
//	Incident2.CasualtyReport_Mechanism_IN=CasualtyReportandTrackingMechanism.Electronic;
//	break;}



