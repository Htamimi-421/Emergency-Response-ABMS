package B_Communication;

import java.util.ArrayList;
import java.util.List;
import A_Agents.Casualty;
import A_Agents.Hospital;
import A_Agents.Responder;
import A_Agents.Responder_Ambulance;
import A_Agents.Responder_Fire;
import A_Agents.Responder_Police;
import A_Agents.Vehicle;
import A_Environment.Incident;
import A_Environment.PointDestination;
import A_Environment.RoadLink;
import A_Environment.Sector;
import C_SimulationInput.InputFile;
import D_Ontology.Ontology.GeolocationMechanism;
import D_Ontology.Ontology.RespondersTriggers;
import D_Ontology.Ontology.SurveyScene_SiteExploration_Mechanism;
import D_Ontology.Ontology.TaskAllocationMechanism;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.graph.Network;
import repast.simphony.space.graph.RepastEdge;

public class SliverMeetingRecord {

	ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule(); 
	double total_time= 0;
	//--------------------------------------------------------
	public String MeetingID;	
	Incident AssignedIncident ;
	PointDestination locationSilverMeeting ;

	// Member
	public  Responder_Ambulance  AssignedAIC; 
	public  Responder_Fire	 AssignedFIC; 
	public  Responder_Police AssignedPIC; 

	public int Thereisupdate_Amb=-1 ,Thereisupdate_fr=-1 ,Thereisupdate_Po=-1; 
	public boolean FirestM=false;
	
	//--------------------------------------------------------
	// Meeting List
	boolean GeolocationofTA=false,UpdateTOscenestructure=false , UpdateTOCausality=false , UpdateTOPriorityRoute=false , UpdateTORoute=false ;

	boolean cordons_outer_established=false, controlarea_established=false , CCS_established=false , loadingArea_established=false, RC_established=false ; //cordons_inner_established=false,
	boolean  Sector_established=false ;

	boolean NomorecasultyinInner=false , NomorecasultyinCCS=false , NomorecasultyinLA=false ;

	boolean EndER_Ambulance=false , EndER_Fire=false , EndER_Police=false; 

	boolean ConsiderSectorNeedinAssignedResponders ;
	//--------------------------------------------------------
	// 2- Causality
	
	ArrayList<Casualty> Trapped_List =new ArrayList<Casualty>();
	ArrayList<Casualty> Dead_List_inner =new ArrayList<Casualty>();
	ArrayList<Casualty> Dead_List_CCS =new ArrayList<Casualty>();
	ArrayList<Casualty> Cainfor_evacuation_List =new ArrayList<Casualty>();
	//ArrayList<Casualty> Injuried_list_CCS =new ArrayList<Casualty>();
	//--------------------------------------------------------
	//3- Route
	ArrayList<RoadLink> routeClearedwreckage_List =new ArrayList<RoadLink>();   // or CEAP
	ArrayList<RoadLink> routeClearedtraffic_List =new ArrayList<RoadLink>();	// or CEAP
	//--------------------------------------------------------
	//4-Priority 
	ArrayList<RoadLink> routeNEEDClearwreckage_List =new ArrayList<RoadLink>();
	ArrayList<RoadLink> routeNEEDCleartraffic_List =new ArrayList<RoadLink>();

	//--------------------------------------------------------
	//used by commanders to calculate the time of meeting
	public  double EstimatedTimeMeeting ; // in second
	
	public  double StartTime=0 ,FinishTime ;
	public  boolean Done=false;
	public  boolean  FirstTimeinmeeting= false;

	//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
	public SliverMeetingRecord (  String  _MeetingID ,PointDestination _locationSilverMeeting , Incident _AssignedIncident   )
	{
		MeetingID=_MeetingID  ;
		locationSilverMeeting=_locationSilverMeeting ;
		AssignedIncident = _AssignedIncident ;
	}

	//===================================================================================

	public void  SectorPlanning( SurveyScene_SiteExploration_Mechanism   SitEM    , ArrayList<Casualty>  CasualtySeen_list  )
	{

		//=================1=============================== Organized_exploration
		if ( SitEM == SurveyScene_SiteExploration_Mechanism.Organized_exploration )
		{
			// Now, he know how many casualty in each sector	
			for ( Sector  S : AssignedIncident.Sectorlist2 )
			{
				List<Casualty> nearObjects_Casualty = (List<Casualty>) S.GetObjectsWithinSector(CasualtySeen_list );
				S.CasualityinThisSector=nearObjects_Casualty.size();
				System.out.println( " planing...  "+ S.CasualityinThisSector );
			}

			for ( int i=0;i<  AssignedIncident.NOSector  ;i++)   //  high priority 0, 1, then 2 
			{ 

				//ProirityCreation
				double Maxcasulity=-1 ;      Sector  NominatedS=null;  
				for ( Sector  S : AssignedIncident.Sectorlist2 )
				{
					if ( S.CasualityinThisSector >    Maxcasulity  && S.ProirityCreation ==-1 )
					{Maxcasulity= S.CasualityinThisSector  ; NominatedS= S; }
				}
				NominatedS.ProirityCreation= i;
			}

			ConsiderSectorNeedinAssignedResponders= true ;


			for ( int i=0;i<  AssignedIncident.NOSector  ;i++)   //  high priority 0, 1, then 2 
				System.out.println(" " + i +  " sector ...total   " +CasualtySeen_list.size() +" CasualityinThisSector :    "  + AssignedIncident.Sectorlist2.get(i).CasualityinThisSector   +"  ProirityCreation :   "  + AssignedIncident.Sectorlist2.get(i).ProirityCreation  +" NeedResponders:    "  + AssignedIncident.Sectorlist2.get(i).NeedResponders  +"  Meet :   "  + AssignedIncident.Sectorlist2.get(i).Meet  +"    CurrentResponderinSector : "  + AssignedIncident.Sectorlist2.get(i).CurrentPramdeicResponderinSector  + AssignedIncident.Sectorlist2.get(i).CurrentFireFighterResponderinSector );

		}
		//=================2=============================== Randomly_exploration
		else if ( SitEM == SurveyScene_SiteExploration_Mechanism.Randomly_exploration )
		{
			ConsiderSectorNeedinAssignedResponders= false ; // do no things
		}

	}

	//===================================================================================
	public void AIC_Attendance( Responder_Ambulance	AIC  ) 
	{
		AssignedAIC=AIC ;

		if ( AssignedPIC!=null && AssignedFIC!=null  && AssignedAIC!=null)
		{ 
			StartTime=schedule.getTickCount() ;
			if ( this.FirestM )
			{
				if ( AssignedIncident.GeolocationTacticalAreas==GeolocationMechanism.Papermap  )      EstimatedTimeMeeting= InputFile.Papermap_duration ;
				if ( AssignedIncident.GeolocationTacticalAreas==GeolocationMechanism.GPSmap  )      EstimatedTimeMeeting= InputFile.GISmap_duration ;

				
			}
			else
				EstimatedTimeMeeting= InputFile.EstimatedTimeinitiateSM ;

			//System.out.println( " AIC_Attendance"  );
			FirstTimeinmeeting= true;
		}
	}
	public void AIC_Addtomeeting_GeolocationTA( ) //after survey 
	{
		GeolocationofTA=true ;
	}
	public void AIC_Addtomeeting_EstablishedTA(boolean _CCS_established , boolean _loadingArea_established ) //after survey 
	{
		UpdateTOscenestructure=true;
		CCS_established=_CCS_established;
		loadingArea_established= _loadingArea_established ;
	}

	public void AIC_Addtomeeting_Casualty( ArrayList<Casualty> _Dead_List_CCS  ,  ArrayList<Casualty> _Cainfor_evacuation_List)
	{
		UpdateTOCausality=true;
		for( Casualty ca : _Dead_List_CCS   )   Dead_List_CCS.add(ca);
		//for( Casualty ca : _Injuried_list_CCS   )   Injuried_list_CCS.add(ca); ArrayList<Casualty> _Injuried_list_CCS  ,
		for( Casualty ca : _Cainfor_evacuation_List   )   Cainfor_evacuation_List.add(ca);

	}

	public void AIC_Addtomeeting_RouteW( ArrayList<RoadLink> _routeNEEDClearwreckage_List  )
	{
		UpdateTOPriorityRoute= true;

		for( RoadLink  R : _routeNEEDClearwreckage_List  )  routeNEEDClearwreckage_List.add(R);
	}

	public void AIC_Addtomeeting_RouteT( ArrayList<RoadLink> _routeNEEDCleartraffic_List  )
	{
		UpdateTOPriorityRoute= true;

		for( RoadLink  R : _routeNEEDCleartraffic_List   )  routeNEEDCleartraffic_List.add(R);
	}

	public void AIC_Addtomeeting_NomorecasultyinCCS(   )
	{		
		NomorecasultyinCCS=true;
	}

	public void AIC_Addtomeeting_NomorecasultyinLA(   )
	{		
		NomorecasultyinLA=true;
	}

	public void AIC_Addtomeeting_EndER_Ambulance(   )
	{		
		EndER_Ambulance=true;
	}

	//----------

	public boolean  AIC_Getmeeting_sectorplan(   ) 
	{
		if ( ConsiderSectorNeedinAssignedResponders )
			return true;
		else
			return false;	
	}
	public ArrayList<RoadLink>  AIC_Getmeeting_RouteW(   ) //?????
	{
		return routeClearedwreckage_List ;
	}
	public ArrayList<RoadLink>  AIC_Getmeeting_RouteT(   ) //?????
	{
		return routeClearedtraffic_List ;
	}
	//----------
	public boolean  AIC_Getmeeting_TA_controlarea(   )
	{
		if ( controlarea_established)
			return true;
		else
			return false;

	}
	public boolean  AIC_Getmeeting_TA_Sector(   )
	{
		if ( Sector_established)
			return true;
		else
			return false;

	}
	public boolean  AIC_Getmeeting_TA_cordons_outer(   ) // PEA very important  to update EOC
	{
		if ( cordons_outer_established)
			return true;
		else
			return false;	
	}

	public ArrayList<Casualty> AIC_Getmeeting_CasualtyTrapped(   )
	{
		return Trapped_List ;
	}

	
	public boolean AIC_Getmeeting_NomorecasultyinInner(   )
	{				
		if ( NomorecasultyinInner) // from FIC
			return true;
		else
			return false;
	}

	public boolean AIC_Getmeeting_EndER_Fire(   )
	{				
		if ( EndER_Fire)
			return true;
		else
			return false;
	}
	public boolean AIC_Getmeeting_EndER_Police(   )
	{				
		if ( EndER_Police)
			return true;
		else
			return false;
	}

	//===================================================================================
	public void FIC_Attendance( Responder_Fire	FIC  ) 
	{
		AssignedFIC=FIC ;
	

		if ( AssignedPIC!=null && AssignedFIC!=null  && AssignedAIC!=null)
		{ 
			StartTime=schedule.getTickCount() ;
			if ( this.FirestM )
			{
				if ( AssignedIncident.GeolocationTacticalAreas==GeolocationMechanism.Papermap  )      EstimatedTimeMeeting= InputFile.Papermap_duration ;
				if ( AssignedIncident.GeolocationTacticalAreas==GeolocationMechanism.GPSmap  )      EstimatedTimeMeeting= InputFile.GISmap_duration ;
				
			}
			else
				EstimatedTimeMeeting= InputFile.EstimatedTimeinitiateSM  ;

			//System.out.println( " FIC_Attendance" );
			FirstTimeinmeeting= true;
		}
	}
	//----------

	public void FIC_Addtomeeting_GeolocationTA( )
	{
		GeolocationofTA=true ;

	}

	public void FIC_Addtomeeting_EstablishedTA( )
	{
		UpdateTOscenestructure=true;
		Sector_established=true;
	}

	public void FIC_Addtomeeting_TrappedCasualty( ArrayList<Casualty> _Trapped_List )
	{
		UpdateTOCausality=true;
		for( Casualty ca : _Trapped_List  )   Trapped_List.add(ca);

	}
	
	public void FIC_Addtomeeting_Casualty( ArrayList<Casualty> _Dead_List_inner  )
	{
		UpdateTOCausality=true;
		for( Casualty ca : _Dead_List_inner  )   Dead_List_inner.add(ca);

	}

	public void FIC_Addtomeeting_Route( ArrayList<RoadLink> _routeClearedwreckage_List  )
	{		
		UpdateTORoute=true;

		for( RoadLink  R : _routeClearedwreckage_List   )  routeClearedwreckage_List.add(R);
	}

	public void FIC_Addtomeeting_NomorecasultyinInner(   )
	{		
		NomorecasultyinInner=true;
	}

	public void FIC_Addtomeeting_EndER_Fire(   )
	{		
		EndER_Fire=true;
	}
	//----------
	public boolean  FIC_Getmeeting_sectorplan(   ) 
	{
		if ( ConsiderSectorNeedinAssignedResponders )
			return true;
		else
			return false;	
	}

	public ArrayList<RoadLink>  FIC_Getmeeting_Route(   )
	{
		return routeNEEDClearwreckage_List ;
	}
	//----------
	public boolean  FIC_Getmeeting_TA_controlarea(   )
	{
		if ( controlarea_established)
			return true;
		else
			return false;	
	}	
	public boolean  FIC_Getmeeting_TA_CCS(   )
	{
		if ( CCS_established)
			return true;
		else
			return false;
	}
	public boolean  FIC_Getmeeting_TA_RC(   )
	{
		if ( RC_established)
			return true;
		else
			return false;	
	}
	public boolean  FIC_Getmeeting_TA_cordons_outer(   ) // PEA very important  to update EOC
	{
		if ( cordons_outer_established)
			return true;
		else
			return false;	
	}

	public boolean FIC_Getmeeting_NomorecasultyinLA(   )
	{				
		if ( NomorecasultyinLA)
			return true;
		else
			return false;
	}
	public boolean FIC_Getmeeting_EndER_Ambulance(   )
	{				
		if ( EndER_Ambulance)
			return true;
		else
			return false;
	}
	public boolean FIC_Getmeeting_EndER_Police(   )
	{				
		if ( EndER_Police)
			return true;
		else
			return false;
	}
	//===================================================================================
	public void PIC_Attendance( Responder_Police	PIC )
	{
		AssignedPIC=PIC ;
	

		if ( AssignedPIC!=null && AssignedFIC!=null  && AssignedAIC!=null)
		{ 
			StartTime=schedule.getTickCount() ;
			if ( this.FirestM )
			{
				if ( AssignedIncident.GeolocationTacticalAreas==GeolocationMechanism.Papermap  )      EstimatedTimeMeeting= InputFile.Papermap_duration ;
				if ( AssignedIncident.GeolocationTacticalAreas==GeolocationMechanism.GPSmap  )      EstimatedTimeMeeting= InputFile.GISmap_duration ;

				
			}
			else
				EstimatedTimeMeeting= InputFile.EstimatedTimeinitiateSM ;

			//System.out.println( " PIC_Attendance" );
			FirstTimeinmeeting= true;
		}
	}
	//----------
	public void PIC_Addtomeeting_GeolocationTA(  ) 
	{
		GeolocationofTA=true ;


	}
	public void PIC_Addtomeeting_EstablishedTA(  boolean _controlarea_established , boolean _cordons_outer_established  , boolean _RC_established) //boolean  _cordons_inner_established 
	{
		UpdateTOscenestructure=true;
		controlarea_established=_controlarea_established; 	
		cordons_outer_established=_cordons_outer_established; 	
		RC_established=_RC_established;

		//cordons_inner_established=_cordons_inner_established; 
	}

	public void PIC_Addtomeeting_Route( ArrayList<RoadLink> _routeClearedtraffic_List )
	{
		UpdateTORoute=true;		
		for( RoadLink  R : _routeClearedtraffic_List  )  routeClearedtraffic_List.add(R);
	}

	public void PIC_Addtomeeting_EndER_Police(   )
	{		
		EndER_Police=true;
	}

	//----------
	public boolean  PIC_Getmeeting_TA_Sector(   )
	{
		if ( Sector_established)
			return true;
		else
			return false;

	}
	public ArrayList<RoadLink>  PIC_Getmeeting_Route(   )
	{
		return routeNEEDCleartraffic_List;
	}

	public ArrayList<Casualty> PIC_Getmeeting_CasualtyDecasedCCS(   )
	{
		return Dead_List_CCS  ;
	}

	public ArrayList<Casualty> PIC_Getmeeting_CasualtyDecasedinner(   )
	{
		return Dead_List_inner ;
	}

//	public ArrayList<Casualty> PIC_Getmeeting_CasualtyinjuredCCS(   )
//	{
//		return this.Injuried_list_CCS;
//	}
	public ArrayList<Casualty> PIC_Getmeeting_Cainfor_evacuation_List(   )
	{
		return Cainfor_evacuation_List ;
	}

	public boolean PIC_Getmeeting_NomorecasultyinInner(   )
	{				
		if ( NomorecasultyinInner)
			return true;
		else
			return false;
	}

	public boolean PIC_Getmeeting_NomorecasultyinCCS(   )
	{				
		if ( NomorecasultyinCCS)
			return true;
		else
			return false;
	}
	public boolean PIC_Getmeeting_NomorecasultyinLA(   )
	{				
		if ( NomorecasultyinLA)
			return true;
		else
			return false;
	}

	public boolean PIC_Getmeeting_EndER_Ambulance(   )
	{				
		if ( EndER_Ambulance)
			return true;
		else
			return false;
	}
	public boolean PIC_Getmeeting_EndER_Fire(   )
	{				
		if ( EndER_Fire)
			return true;
		else
			return false;
	}

	//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
	
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	// Meeting-  General
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	//@ScheduledMethod( interval = 1)
	public void step() {

		if (StartTime>0 && !Done )
		{
			EstimatedTimeMeeting--; 

			if (FirstTimeinmeeting )
			{
				FirstTimeinmeeting= false;
				AssignedPIC.InterpretedTrigger=RespondersTriggers.SliverMeetingStart; 
				AssignedFIC.InterpretedTrigger=RespondersTriggers.SliverMeetingStart; 
				AssignedAIC.InterpretedTrigger=RespondersTriggers.SliverMeetingStart; 

			}


			if (    Thereisupdate_Amb>-1 && Thereisupdate_fr>-1 && Thereisupdate_Po>-1   && !this.FirestM  )
			{


				total_time= ( Thereisupdate_Amb + Thereisupdate_fr + Thereisupdate_Po ) *  InputFile.GetInfromation_FtoForR_Duration_Data ;
				
				EstimatedTimeMeeting=EstimatedTimeMeeting + total_time ;

				Thereisupdate_Amb= -2 ;  Thereisupdate_fr=-2 ;Thereisupdate_Po=-2 ;
			}

			if ( EstimatedTimeMeeting<=0  )
			{
				Done=true ;
				AssignedPIC.InterpretedTrigger=RespondersTriggers.SliverMeetingEnd; 
				AssignedFIC.InterpretedTrigger=RespondersTriggers.SliverMeetingEnd;
				AssignedAIC.InterpretedTrigger=RespondersTriggers.SliverMeetingEnd;	// after get 

				printupdate();
				
				
				
			}
		}


	}// end Step *************

	void printupdate()
	{

		System.out.println("                                                                                                                                 " + total_time +  " <= Time SM=======================");
		if (GeolocationofTA )
			System.out.println("                                                                                                                                 " +  "GeolocationofTA "+ GeolocationofTA );
		if (UpdateTOscenestructure)
		{	
			System.out.println( "                                                                                                                                 " + " Sector_established   "+ Sector_established +" cordons_outer_established   "+ cordons_outer_established +  " CCS_established " + CCS_established + " RC_established "+RC_established   );   //" loadingArea_established  " +loadingArea_established +
		}
		if ( UpdateTOCausality)
		{
			System.out.println("                                                                                                                                 " +  " Trapped_List "+  Trapped_List.size() +  " Dead_List_inner "+ Dead_List_inner.size()  + " Dead_List_CCS " +Dead_List_CCS .size() + " Cainfor_evacuation_List " + Cainfor_evacuation_List.size()   );   
		}
		if( UpdateTOPriorityRoute)
		{			
			System.out.println( "                                                                                                                                 " + " routeNEEDCleartraffic_List "+ routeNEEDCleartraffic_List .size()  +" routeNEEDClearwreckage_List "+ routeNEEDClearwreckage_List.size() );
		}
		if ( UpdateTORoute)
		{
			System.out.println("                                                                                                                                 " +  " routeClearedwreckage_List "+ routeClearedwreckage_List.size()  +" routeClearedtraffic_List"+ routeClearedtraffic_List.size());  
		}
		if (NomorecasultyinInner || NomorecasultyinCCS || NomorecasultyinLA )
		{
			System.out.println( "                                                                                                                                 " + " NomorecasultyinInner   "+ NomorecasultyinInner + " NomorecasultyinCCS  " + NomorecasultyinCCS + "NomorecasultyinLA "  + NomorecasultyinLA);
		}
		if (EndER_Ambulance ||EndER_Fire || EndER_Police )
		{
			System.out.println( "                                                                                                                                 " + " EndER_Ambulance   "+ EndER_Ambulance + " EndER_Fire  " + EndER_Fire +" EndER_Police  "+ EndER_Police );
		}

		System.out.println("                                                                                                                                 " +  "SM=======================");
	}

}