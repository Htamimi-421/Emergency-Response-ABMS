package A_EmergencyResponse;

import com.vividsolutions.jts.geom.Coordinate;
import A_Agents.EOC;
import A_Environment.Incident;
import A_Environment.RoadNode;
import A_Environment.TacticalArea;
import C_SimulationInput.InputFile;
import D_Ontology.BuildStaticFuction;
import D_Ontology.Ontology.AmbulanceTracking;
import D_Ontology.Ontology.Callinformatiom;
import D_Ontology.Ontology.CasualtyReportandTrackingMechanism;
import D_Ontology.Ontology.CommunicationMechanism;
import D_Ontology.Ontology.Communication_Time;
import D_Ontology.Ontology.GeolocationMechanism;
import D_Ontology.Ontology.Inter_Communication_Structure;
import D_Ontology.Ontology.STartEvacuationtoHospital_Startgy;
import D_Ontology.Ontology.SurveyScene_SiteExploration_Mechanism;
import D_Ontology.Ontology.TaskAllocationApproach;
import D_Ontology.Ontology.TaskAllocationMechanism;
import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.gis.Geography;

public class Scenario_InformationalDependency {

	Context<Object> context;
	Geography<Object> geography;		
	static int Incidentcounter=1;
	boolean Incident1_active=false;
	double RightNow_NoneedforTA=0;
	//---------------------------------------

	//EOC Instantiation
	Coordinate EOCCoordinate=new Coordinate(-1.58772788672538701, 54.95606439799289689);     
	EOC EOC1;
	Incident Incident1 ;

	//---------------------------------------------------------------------

	double  CurrentTick , CurrentTask_duration, EndofCurrentTask ;		
	ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule(); 	

	//*************************************************************************************************************************************

	//Instantiate the  Scenario3 in which  one responder  will go to the rescue the causality and then back to the ambulance vehicle and then hospital

	public Scenario_InformationalDependency(Context<Object> _context, Geography<Object> _geography)
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
	//  it represents the sequence of incident's events
	@ScheduledMethod(start = 0, interval = 1 , priority= ScheduleParameters.FIRST_PRIORITY)
	public void step() {	

		CurrentTick=schedule.getTickCount() ;

		if (CurrentTick==1)	
		{				
			// Clear extra link of Road Network to reduce the burden on memory
			RoadNode   incNode= BuildStaticFuction.GetRoadNodeofthisLocation( geography, "osgb4000000007932484");
			RoadNode PEANode = BuildStaticFuction.GetRoadNodeofthisLocation(geography, "osgb4000000007592232");  
			
			
			BuildStaticFuction.clearExtraNW( context ,geography  , incNode , PEANode ,EOC1.Hospital_list ,EOC1.Station_Ambulance_list,EOC1.Station_Fire_list ,EOC1.Station_Police_list);
			
			// Incident creation 
			Incident1 = new Incident("MyFrind_Incident",context, geography,"osgb4000000007932484","osgb1000002310250872" , 60 , 10, 10 ,60); //40   35  "osgb1000002310066393"
			
			System.out.println("PRIORITY_execution =>  "+ InputFile.PRIORITY_execution +"   "  + Incident1.ID );
			ScheduleParameters params = ScheduleParameters.createRepeating(CurrentTick + 1 , 1,InputFile.PRIORITY_execution ++);
			schedule.schedule(params,Incident1,"Step");
			
			Incident1.EOC1= EOC1 ;	
			Incident1.Randomly_Location= false ;
			Incident1.Randomly_Severity= false ;

			// Coordination Mechanism static for Exps
			Incident1.TaskMechanism_IN=TaskAllocationMechanism.NoEnvironmentsectorization; Incident1.NOSector=1; 
			Incident1.Survey_scene_Mechanism_IN= SurveyScene_SiteExploration_Mechanism.Randomly_exploration; 
			Incident1.TaskApproach_IN=TaskAllocationApproach.Decentralized_Autonomous ;
			Incident1.AmbulanceTracking_Strategy=AmbulanceTracking.Radio ;
			Incident1.GeolocationTacticalAreas = GeolocationMechanism.Papermap ;

			// Assumptions
			Incident1._STartEvacuationtoHospital_Startgy=STartEvacuationtoHospital_Startgy.CCS_MorepriorityinCSS ;

			//Intra-Communication	
			Incident1.ComMechanism_inControlArea=CommunicationMechanism.FaceToFace; // Role Assignment 
			Incident1.ComMechanism_level_BtoRes=CommunicationMechanism.FaceToFace;// Between bronze commander to its responders
			Incident1.ComMechanism_level_toEOC=CommunicationMechanism.Phone ;       // AOC
			Incident1.ComMechanism_level_DrivertoALC=CommunicationMechanism.Phone ;//Driver with ALC

			Incident1.ComMechanism_level_StoB=CommunicationMechanism.RadioSystem ; // Between silver commander to its responders	
			Incident1.Intra_Communication_Time_used=Communication_Time.When_need ;
			
			

			switch ( InputFile.CurrentExp ) {	
			//==================================================================Struc 1========================================================================
			case Exp1 :					
				//Inter-Communication										
				Incident1.Communication_Structure_uesd=Inter_Communication_Structure.Struc1_SilverCommandersInteraction ;
				Incident1.Inter_Communication_Tool_used=CommunicationMechanism.SilverMeeting ;					
				Incident1.Inter_Communication_Time_used=Communication_Time.Every_frequently; Incident1.SMEvery= 30  * 60 ;  // 20 m    // Incident1.SMEvery=400;  
				//Incident1.Inter_Communication_Time_used=Communication_Time.When_need ;

				Incident1.CasualtyReport_Mechanism_IN=CasualtyReportandTrackingMechanism.Paper ;							

				break;
			case Exp2 :							
				//Inter-Communication										
				Incident1.Communication_Structure_uesd=Inter_Communication_Structure.Struc1_SilverCommandersInteraction ;
				Incident1.Inter_Communication_Tool_used=CommunicationMechanism.SilverMeeting ;					
				Incident1.Inter_Communication_Time_used=Communication_Time.Every_frequently; Incident1.SMEvery=20  * 60 ;  // 15 m

				Incident1.CasualtyReport_Mechanism_IN=CasualtyReportandTrackingMechanism.Paper;

				break;				
			case Exp3 :			
				//Inter-Communication										
				Incident1.Communication_Structure_uesd=Inter_Communication_Structure.Struc1_SilverCommandersInteraction ;
				Incident1.Inter_Communication_Tool_used=CommunicationMechanism.SilverMeeting ;					
				Incident1.Inter_Communication_Time_used=Communication_Time.Every_frequently; Incident1.SMEvery=10  * 60 ;  // 10 m

				Incident1.CasualtyReport_Mechanism_IN=CasualtyReportandTrackingMechanism.Paper;

				break;				
			case Exp4 :
				//Inter-Communication										
				Incident1.Communication_Structure_uesd=Inter_Communication_Structure.Struc1_SilverCommandersInteraction ;
				Incident1.Inter_Communication_Tool_used=CommunicationMechanism.SilverMeeting ;					
				Incident1.Inter_Communication_Time_used=Communication_Time.When_need;

				Incident1.CasualtyReport_Mechanism_IN=CasualtyReportandTrackingMechanism.Paper;	

				break;
				//===============================Electronic=========================================
			case Exp5 :
				//Inter-Communication										
				Incident1.Communication_Structure_uesd=Inter_Communication_Structure.Struc1_SilverCommandersInteraction ;
				Incident1.Inter_Communication_Tool_used=CommunicationMechanism.SilverMeeting ;					
				Incident1.Inter_Communication_Time_used=Communication_Time.Every_frequently; Incident1.SMEvery=30  * 60 ;  // 20 m																															

				Incident1.CasualtyReport_Mechanism_IN=CasualtyReportandTrackingMechanism.Electronic;	

				break;
			case Exp6 :
				//Inter-Communication										
				Incident1.Communication_Structure_uesd=Inter_Communication_Structure.Struc1_SilverCommandersInteraction ;
				Incident1.Inter_Communication_Tool_used=CommunicationMechanism.SilverMeeting ;					
				Incident1.Inter_Communication_Time_used=Communication_Time.Every_frequently; Incident1.SMEvery=20  * 60 ;  // 15 m

				Incident1.CasualtyReport_Mechanism_IN=CasualtyReportandTrackingMechanism.Electronic;	

				break;
			case Exp7 :
				//Inter-Communication										
				Incident1.Communication_Structure_uesd=Inter_Communication_Structure.Struc1_SilverCommandersInteraction ;
				Incident1.Inter_Communication_Tool_used=CommunicationMechanism.SilverMeeting ;					
				Incident1.Inter_Communication_Time_used=Communication_Time.Every_frequently; Incident1.SMEvery=10  * 60 ;  // 10 m

				Incident1.CasualtyReport_Mechanism_IN=CasualtyReportandTrackingMechanism.Electronic;	

				break;
			case Exp8 :
				//Inter-Communication										
				Incident1.Communication_Structure_uesd=Inter_Communication_Structure.Struc1_SilverCommandersInteraction ;
				Incident1.Inter_Communication_Tool_used=CommunicationMechanism.SilverMeeting ;					
				Incident1.Inter_Communication_Time_used=Communication_Time.When_need;  																															

				Incident1.CasualtyReport_Mechanism_IN=CasualtyReportandTrackingMechanism.Electronic;	

				break;
				//==================================================================Struc 2========================================================================
			case Exp9 :
				//Inter-Communication										
				Incident1.Communication_Structure_uesd=Inter_Communication_Structure.Struc2_BronzeCommandersInteraction ;
				Incident1.Inter_Communication_Tool_used=CommunicationMechanism.FaceToFace_and_Phone ;				
				Incident1.Inter_Communication_Time_used=Communication_Time.Every_frequently ; Incident1.UpdatOPEvery=30 * 60  ;// 20 m	 400 ; 
				//Incident1.Inter_Communication_Time_used=Communication_Time.When_need;

				Incident1.CasualtyReport_Mechanism_IN=CasualtyReportandTrackingMechanism.Paper;

				break;
			case Exp10 :
				//Inter-Communication										
				Incident1.Communication_Structure_uesd=Inter_Communication_Structure.Struc2_BronzeCommandersInteraction ;
				Incident1.Inter_Communication_Tool_used=CommunicationMechanism.FaceToFace_and_Phone ;						
				Incident1.Inter_Communication_Time_used=Communication_Time.Every_frequently ; Incident1.UpdatOPEvery=20 * 60  ;// 15 m	

				Incident1.CasualtyReport_Mechanism_IN=CasualtyReportandTrackingMechanism.Paper;

				break;
			case Exp11 :
				//Inter-Communication										
				Incident1.Communication_Structure_uesd=Inter_Communication_Structure.Struc2_BronzeCommandersInteraction ;
				Incident1.Inter_Communication_Tool_used=CommunicationMechanism.FaceToFace_and_Phone ;					
				Incident1.Inter_Communication_Time_used=Communication_Time.Every_frequently ; Incident1.UpdatOPEvery= 10 * 60  ;// 10 m	

				Incident1.CasualtyReport_Mechanism_IN=CasualtyReportandTrackingMechanism.Paper;

				break;
			case Exp12 :
				//Inter-Communication										
				Incident1.Communication_Structure_uesd=Inter_Communication_Structure.Struc2_BronzeCommandersInteraction ;
				Incident1.Inter_Communication_Tool_used=CommunicationMechanism.FaceToFace_and_Phone ;						
				Incident1.Inter_Communication_Time_used=Communication_Time.When_need ;

				Incident1.CasualtyReport_Mechanism_IN=CasualtyReportandTrackingMechanism.Paper;

				break;
				//=====================Radio========================
			case Exp13 :		
				//Inter-Communication		error								
				Incident1.Communication_Structure_uesd=Inter_Communication_Structure.Struc2_BronzeCommandersInteraction ;
				Incident1.Inter_Communication_Tool_used=CommunicationMechanism.SharedTactical_RadioSystem ;						
				Incident1.Inter_Communication_Time_used=Communication_Time.Every_frequently ; Incident1.UpdatOPEvery=30 * 60  ;// 20 m	

				Incident1.CasualtyReport_Mechanism_IN=CasualtyReportandTrackingMechanism.Paper;

				break;
			case Exp14 :
				//Inter-Communication										
				Incident1.Communication_Structure_uesd=Inter_Communication_Structure.Struc2_BronzeCommandersInteraction ;
				Incident1.Inter_Communication_Tool_used=CommunicationMechanism.SharedTactical_RadioSystem ;					
				Incident1.Inter_Communication_Time_used=Communication_Time.Every_frequently ; Incident1.UpdatOPEvery=20 * 60  ;// 15 m	

				Incident1.CasualtyReport_Mechanism_IN=CasualtyReportandTrackingMechanism.Paper;

				break;
			case Exp15 :
				//Inter-Communication										
				Incident1.Communication_Structure_uesd=Inter_Communication_Structure.Struc2_BronzeCommandersInteraction ;
				Incident1.Inter_Communication_Tool_used=CommunicationMechanism.SharedTactical_RadioSystem ;						
				Incident1.Inter_Communication_Time_used=Communication_Time.Every_frequently ; Incident1.UpdatOPEvery=10 * 60  ;// 10 m	

				Incident1.CasualtyReport_Mechanism_IN=CasualtyReportandTrackingMechanism.Paper;

				break;
			case Exp16 :
				//Inter-Communication										
				Incident1.Communication_Structure_uesd=Inter_Communication_Structure.Struc2_BronzeCommandersInteraction ;
				Incident1.Inter_Communication_Tool_used=CommunicationMechanism.SharedTactical_RadioSystem ;				
				Incident1.Inter_Communication_Time_used=Communication_Time.When_need ;

				Incident1.CasualtyReport_Mechanism_IN=CasualtyReportandTrackingMechanism.Paper;

				break;
				//===============================Electronic=========================================
			case Exp17 :
				//Inter-Communication										
				Incident1.Communication_Structure_uesd=Inter_Communication_Structure.Struc2_BronzeCommandersInteraction ;
				Incident1.Inter_Communication_Tool_used=CommunicationMechanism.FaceToFace_and_Phone ;			
				Incident1.Inter_Communication_Time_used=Communication_Time.Every_frequently ; Incident1.UpdatOPEvery=30 * 60  ;// 20 m	

				Incident1.CasualtyReport_Mechanism_IN=CasualtyReportandTrackingMechanism.Electronic;

				break;
			case Exp18 :
				//Inter-Communication										
				Incident1.Communication_Structure_uesd=Inter_Communication_Structure.Struc2_BronzeCommandersInteraction ;
				Incident1.Inter_Communication_Tool_used=CommunicationMechanism.FaceToFace_and_Phone ;				
				Incident1.Inter_Communication_Time_used=Communication_Time.Every_frequently ; Incident1.UpdatOPEvery=20 * 60  ;// 15 m	

				Incident1.CasualtyReport_Mechanism_IN=CasualtyReportandTrackingMechanism.Electronic;

				break;
			case Exp19 :
				//Inter-Communication										
				Incident1.Communication_Structure_uesd=Inter_Communication_Structure.Struc2_BronzeCommandersInteraction ;
				Incident1.Inter_Communication_Tool_used=CommunicationMechanism.FaceToFace_and_Phone ;			
				Incident1.Inter_Communication_Time_used=Communication_Time.Every_frequently ; Incident1.UpdatOPEvery=10 * 60  ;// 10 m	

				Incident1.CasualtyReport_Mechanism_IN=CasualtyReportandTrackingMechanism.Electronic;

				break;
			case Exp20 :
				//Inter-Communication										
				Incident1.Communication_Structure_uesd=Inter_Communication_Structure.Struc2_BronzeCommandersInteraction ;
				Incident1.Inter_Communication_Tool_used=CommunicationMechanism.FaceToFace_and_Phone ;			
				Incident1.Inter_Communication_Time_used=Communication_Time.When_need ;

				Incident1.CasualtyReport_Mechanism_IN=CasualtyReportandTrackingMechanism.Electronic;

				break;
				//=====================Radio========================
			case Exp21 :
				//Inter-Communication										
				Incident1.Communication_Structure_uesd=Inter_Communication_Structure.Struc2_BronzeCommandersInteraction ;
				Incident1.Inter_Communication_Tool_used=CommunicationMechanism.SharedTactical_RadioSystem;					
				Incident1.Inter_Communication_Time_used=Communication_Time.Every_frequently ; Incident1.UpdatOPEvery=30 * 60  ;// 20 m	

				Incident1.CasualtyReport_Mechanism_IN=CasualtyReportandTrackingMechanism.Electronic;

				break;
			case Exp22 :
				//Inter-Communication										
				Incident1.Communication_Structure_uesd=Inter_Communication_Structure.Struc2_BronzeCommandersInteraction ;
				Incident1.Inter_Communication_Tool_used=CommunicationMechanism.SharedTactical_RadioSystem;					
				Incident1.Inter_Communication_Time_used=Communication_Time.Every_frequently ; Incident1.UpdatOPEvery=20 * 60  ;// 15 m	

				Incident1.CasualtyReport_Mechanism_IN=CasualtyReportandTrackingMechanism.Electronic;

				break;
			case Exp23 :
				//Inter-Communication										
				Incident1.Communication_Structure_uesd=Inter_Communication_Structure.Struc2_BronzeCommandersInteraction ;
				Incident1.Inter_Communication_Tool_used=CommunicationMechanism.SharedTactical_RadioSystem;				
				Incident1.Inter_Communication_Time_used=Communication_Time.Every_frequently ; Incident1.UpdatOPEvery=10 * 60  ;// 10 m	

				Incident1.CasualtyReport_Mechanism_IN=CasualtyReportandTrackingMechanism.Electronic;

				break;
			case Exp24 :
				//Inter-Communication										
				Incident1.Communication_Structure_uesd=Inter_Communication_Structure.Struc2_BronzeCommandersInteraction ;
				Incident1.Inter_Communication_Tool_used=CommunicationMechanism.SharedTactical_RadioSystem;					
				Incident1.Inter_Communication_Time_used=Communication_Time.When_need ;

				Incident1.CasualtyReport_Mechanism_IN=CasualtyReportandTrackingMechanism.Electronic;

				break;
				//==================================================================End ========================================================================
			}


			Incident1.Onset();				
			this.EndofCurrentTask= CurrentTick +30 ; //120 2m
			Incident1_active= true;

			//			//print casualty
			//			int x=0;
			//			for (int i = 0; i < 49 ; i++) 
			//			{
			//				double discas=BuildStaticFuction.DistanceC(Incident1.geography, Incident1.getLocation(), Incident1.casualties.get(i).getCurrentLocation());
			//				if (discas >= 25  && discas <= 30 )
			//					{
			//					Incident1.casualties.get(i).ColorCode= x ++;	
			//					System.out.println( "new Coordinate ("+ Incident1.casualties.get(i).getCurrentLocation().x+ ","+ Incident1.casualties.get(i).getCurrentLocation().y + ")," );	
			//					}
			//			}

		}

		else if ( Incident1_active && EndofCurrentTask== CurrentTick )
		{
			EOC1.call999(Incident1,Callinformatiom.ThereisInformation); // information 
			System.out.println("EOC get call from Incident  1 ");
			boolean Incident1_active=false;

		}

		//Temp soultion after Last meeting with Phill	
		if ( CurrentTick >= 120 &&  RightNow_NoneedforTA==0 &&  Incident1.RC!=null && Incident1.CCStation!=null &&  Incident1.loadingArea!=null   )
		{
			RightNow_NoneedforTA=CurrentTick;	
		}

		if (  this.CurrentTick==(RightNow_NoneedforTA+10)   && Incident1.RC!=null && Incident1.CCStation!=null &&  Incident1.loadingArea!=null   )
		{
			BuildStaticFuction.IdentifyNear_Building( Incident1 , context, geography);
			BuildStaticFuction.clearExtraTArea(context, geography);
			Incident1.Building ();
			

			BuildStaticFuction.clearExtraNW( context ,geography  , Incident1.ParkingNodesforIncdent(), Incident1.Cordon_outer.EnteryPointAccess_node  ,EOC1.Hospital_list ,EOC1.Station_Ambulance_list,EOC1.Station_Fire_list ,EOC1.Station_Police_list);
		}

	}// step


}
