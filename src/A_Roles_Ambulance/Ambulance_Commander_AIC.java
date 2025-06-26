package A_Roles_Ambulance;

import java.util.ArrayList;
import java.util.List;
import A_Agents.Casualty;
import A_Agents.Responder;
import A_Agents.Responder_Ambulance;
import A_Environment.PointDestination;
import A_Environment.RoadLink;
import A_Environment.Sector;
import B_Classes.Activity_Fire;
import B_Classes.Activity_ambulance;
import B_Classes.HospitalRecord;
import B_Classes.Task_ambulance;
import B_Communication.ACL_Message;
import B_Communication.Casualty_info;
import B_Communication.Command;
import B_Communication.ISRecord;
import B_Communication.Report;
import B_Communication.SliverMeetingRecord;
import C_SimulationInput.InputFile;
import D_Ontology.Ontology.ACLPerformative;
import D_Ontology.Ontology.Allocation_Strategy;
import D_Ontology.Ontology.Ambulance_ActivityType;
import D_Ontology.Ontology.Ambulance_ResponderRole;
import D_Ontology.Ontology.Ambulance_TaskType;
import D_Ontology.Ontology.CasualtyReportandTrackingMechanism;
import D_Ontology.Ontology.CommunicationMechanism;
import D_Ontology.Ontology.Communication_Time;
import D_Ontology.Ontology.GeneralTaskStatus;
import D_Ontology.Ontology.Inter_Communication_Structure;
import D_Ontology.Ontology.RandomWalking_StrategyType;
import D_Ontology.Ontology.ReportSummery;
import D_Ontology.Ontology.RespondersActions;
import D_Ontology.Ontology.RespondersBehaviourTypes1;
import D_Ontology.Ontology.RespondersBehaviourTypes2;
import D_Ontology.Ontology.RespondersBehaviourTypes3;
import D_Ontology.Ontology.RespondersTriggers;
import D_Ontology.Ontology.STartEvacuationtoHospital_Startgy;
import D_Ontology.Ontology.SurveyScene_SiteExploration_Mechanism;
import D_Ontology.Ontology.TaskAllocationMechanism;
import D_Ontology.Ontology.TypeMesg;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ISchedulableAction;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.engine.schedule.ScheduleParameters;

public class Ambulance_Commander_AIC {

	Responder_Ambulance Mybody;
	boolean TempAIC=true;

	//----------------------------------------------------
	Command    Allocation_Command , MajorActivity_Command; 
	Task_ambulance  AllocatedTaskforcasualtyevacuation;

	Responder_Ambulance NewResponder=null, BackResponder=null ,CurrentResponder=null,CurrentDriver=null ; 
	Responder_Ambulance  CurrentBZC  , TempACO=null ;

	HospitalRecord CurrentHospitalRecord=null;
	Report CurrentReport=null;
	int Taskoption=0;
	ArrayList<Casualty_info> CurrentTriage_CCSListbySender =new ArrayList<Casualty_info>();
	ArrayList<Casualty> CurrentDead_CCSListbySender =new ArrayList<Casualty>();	
	ArrayList<Casualty> Currentevacuated_CCSListbySender =new ArrayList<Casualty>();

	ArrayList<RoadLink> CurrentrouteNEEDClearwreckageSender;
	ArrayList<RoadLink> CurrentrouteNEEDCleartrafficSender ;

	//----------------------------------------------------
	boolean ConsiderSectorNeedinAssignedResponders =false; //sector	
	boolean InstrcuteTempACOtoleave=false ;
	boolean EndSceneResponse=false,EndCCSResponse=false , EndLoadingandTransportation=false ;	
	boolean IsNewResponder ;
	boolean  updateALCNomorecasulty=false ;
	int NPramdics=0 ;
	int option=0 ;
	boolean ThereisneedforHospital=false;


	boolean  doneARRequest=true;
	int NEEDfromAESSMENT=0;
	int CountedCasualties=0 , TotalCasualtiesRed_transfer = 0;
	int AssessSituation_casualtycount=0;
	int countCa=0;
	boolean NeedFirstMeeting=true  , DoneFirstMeeting=false ,DoneLastMeeting=false ;
	boolean SurveyScene ;
	boolean respondrsBack=false;
	//----------------------------------------------------
	boolean GeolocationofTA_CCS , GeolocationofTA_loadingArea ;

	//  The meaning of attribute content....  -1:No update   , 0: Get  , 1: realize or done  2: send to workers or sliver commander
	int CCS_established=-1 , LoadingArea_established= -1  ;
	int  TA_cordon_established=-1 , TA_controlarea_established=-1 ,TA_Sector_established=-1 ,TA_RC_established=-1 ;

	int startSR=-1;
	int startLA=-1;
	int startCCS=-1;

	//----------------------------------------------------
	int ThereIsneedformoreHospitalcapacity=0;
	int Totalbedavilibeuntilnow=0;

	//----------------------------------------------------
	List<HospitalRecord> CurrentHospitalRecord_list  ; //temp action
	ArrayList<ISRecord > TriageResultReport_RecList=new ArrayList<ISRecord >();

	//----------------------------------------------------
	List<Activity_ambulance> Tactical_Plan = new ArrayList<Activity_ambulance>();	
	public List<Responder_Ambulance> MyResponder_Ambulance = new ArrayList<Responder_Ambulance>(); // all Responder of Ambulance
	public List<Responder_Ambulance> UnoccupiedResponders = new ArrayList<Responder_Ambulance>(); //New Responders
	public List<Responder_Ambulance> UnoccupiedDrivers = new ArrayList<Responder_Ambulance>(); //New Driver

	List<Task_ambulance> Casualty_Plan = new ArrayList<Task_ambulance>();// Task for each casualty

	List<HospitalRecord> RecivingHospital_list = new ArrayList<HospitalRecord>(); 
	List<Sector> SectorDoneFieldTraige = new ArrayList<Sector>();
	//----------------------------------------------------
	double Time_last_updated=0;	

	SliverMeetingRecord Current_SliverMeeting ; 

	public List<SliverMeetingRecord> SliverMeetings= new ArrayList<SliverMeetingRecord>(); 

	// input silver meeting	
	ArrayList<RoadLink> routeNEEDCleartraffic_List =new ArrayList<RoadLink>(); 
	ArrayList<RoadLink> routeNEEDClearwreckage_List =new ArrayList<RoadLink>(); 

	ArrayList<Casualty> Dead_List_CCS =new ArrayList<Casualty>();
	ArrayList<Casualty> Trapped_List =new ArrayList<Casualty>();
	ArrayList<Casualty> Cainfor_evacuation_List =new ArrayList<Casualty>();


	//output
	int NomorecasultyinInner=-1 , NomorecasultyinCCS=-1 ,  NomorecasultyinAL ;
	int EndER_Ambulance=-1 , EndER_Fire=-1 , EndER_Police=-1; 
	ArrayList<RoadLink> routeClearedwreckage_List =new ArrayList<RoadLink>();   // or CEAP
	ArrayList<RoadLink> routeClearedtraffic_List =new ArrayList<RoadLink>();

	//##############################################################################################################################################################	
	public Ambulance_Commander_AIC ( Responder_Ambulance _Mybody) 
	{
		Mybody=_Mybody;
		Mybody.ColorCode= 5;

		Mybody.assignedIncident.AICcommander=Mybody;
		Mybody.PrvRoleinprint1 =Mybody.Role;
	}

	public void Commander_AIC_HandoverAction ( Responder_Ambulance TempAIC)  //called by 
	{
		this.Tactical_Plan =TempAIC.CurrentCalssRole3.Tactical_Plan ;
		this.MyResponder_Ambulance=	TempAIC.CurrentCalssRole3.MyResponder_Ambulance;
		this.UnoccupiedResponders =TempAIC.CurrentCalssRole3.UnoccupiedResponders;
		this.Casualty_Plan = TempAIC.CurrentCalssRole3.Casualty_Plan; 
		this.RecivingHospital_list= TempAIC.CurrentCalssRole3.RecivingHospital_list ;

		int x= TempAIC.Message_inbox.size()-TempAIC.Lastmessagereaded ;  

		System.out.println(Mybody.Id + " number of message transferred" + x  );

		while (TempAIC.Lastmessagereaded < TempAIC.Message_inbox.size()  ) //Right now there is now system to check if there is unread message
		{
			Mybody.Message_inbox.add(TempAIC.Message_inbox.get(TempAIC.Message_inbox.size()- x)) ;   
			x--;
		}

	}

	//##############################################################################################################################################################
	public void CommanderAIC_InterpretationMessage()
	{
		boolean  done= true;
		ACL_Message currentmsg =  Mybody.Message_inbox.get(Mybody.Lastmessagereaded);		 			
		Mybody.CurrentSender= (Responder)currentmsg.sender;
		Mybody.Lastmessagereaded++;
		
		Mybody.SendingReciving_External= false ; Mybody.SendingReciving_internal=false; 
		if (  currentmsg.Inernal) Mybody.SendingReciving_internal= true ;
		else if (  currentmsg.External) Mybody.SendingReciving_External=true ;

		switch( currentmsg.performative) {
		case Command :
			Mybody.CurrentCommandRequest=((Command) currentmsg.content);
			//++++++++++++++++++++++++++++++++++++++
			if ( Mybody.CurrentSender instanceof Responder_Ambulance && TempAIC && Mybody.CurrentCommandRequest.commandType1 == null  )  
			{	
				Mybody.CommTrigger= RespondersTriggers.GetinstructiontoHandover  ;
			}
			//++++++++++++++++++++++++++++++++++++++
			break;
		case InformNewResponderArrival:	
			NewResponder= (Responder_Ambulance) Mybody.CurrentSender;			
			Mybody.CommTrigger= RespondersTriggers.GetNewResponderarrived;
			//System.out.println(Mybody.Id + "  " );
			break;
		case InformComebackResponderArrival:	
			BackResponder= (Responder_Ambulance) Mybody.CurrentSender;			
			Mybody.CommTrigger= RespondersTriggers.GetbackResponderarrived	;		
			break;	
			//------------------------------- 
		case InformlocationArrival: //from BZC
			Mybody.CommTrigger= RespondersTriggers.GetBronzeCommanderstart ; //start 
			CurrentBZC=(Responder_Ambulance) Mybody.CurrentSender;		 
			// do some;
			break;
		case  InfromCCSEstablished: 
			Mybody.CommTrigger= RespondersTriggers.ResultofSetupTA  ;
			CurrentBZC = (Responder_Ambulance)Mybody.CurrentSender;	
			CCS_established= 0;
			break;			
		case  InfromLoadingAreaEstablished: 
			Mybody.CommTrigger= RespondersTriggers.ResultofSetupTA  ;
			CurrentBZC = (Responder_Ambulance)Mybody.CurrentSender;	
			LoadingArea_established= 0;
			break;	
			//------------------------------- 	
		case  InformEndFieldTriage : // no details no  op in sector
			Mybody.CommTrigger= RespondersTriggers.GetOPReport;			
			CurrentBZC = (Responder_Ambulance)Mybody.CurrentSender;
			EndSceneResponse=true ;	

			break;	
			//------------------------------- 	
		case 	InformCCSReport :
			Mybody.CommTrigger= RespondersTriggers.GetOPReport;
			CurrentBZC = (Responder_Ambulance)Mybody.CurrentSender;	

			CurrentReport=((Report) currentmsg.content);
			CurrentTriage_CCSListbySender =CurrentReport.cas_triage_List_CCS ;							
			CurrentDead_CCSListbySender =CurrentReport.Dead_List_CCS;				
			Currentevacuated_CCSListbySender=CurrentReport.Cas_evacuated_List ;
			break;
		case 	InformNomorecasualty_CCS  : 
			Mybody.CommTrigger= RespondersTriggers.GetOPReport;
			CurrentBZC = (Responder_Ambulance)Mybody.CurrentSender;	
			EndCCSResponse=true ;
			//			CurrentReport=((Report) currentmsg.content);
			//			CurrentTriage_CCSListbySender =CurrentReport.cas_triage_List_CCS ;							
			//			CurrentDead_CCSListbySender =CurrentReport.Dead_List_CCS;				
			//			//Currentevacuated_CCSListbySender=CurrentReport.Cas_evacuated_List;

			break;
			//------------------------------- 	
		case InformLoadingandTransportationReport:
			Mybody.CommTrigger= RespondersTriggers.GetOPReport;
			CurrentBZC = (Responder_Ambulance)currentmsg.sender;
			CurrentReport=((Report) currentmsg.content);

			//Currentevacuated_CCSListbySender=CurrentReport.Cas_evacuated_List ;				
			CurrentrouteNEEDClearwreckageSender  = CurrentReport.routeNEEDClearwreckage_List ;	
			CurrentrouteNEEDCleartrafficSender =CurrentReport.routeNEEDCleartraffic_List ;
			break;
		case InformEndLoadingandTransportation_Nomorecasualty :
			Mybody.CommTrigger= RespondersTriggers.GetOPReport;
			CurrentBZC = (Responder_Ambulance)Mybody.CurrentSender;	
			EndLoadingandTransportation=true ;

			//			CurrentReport=((Report) currentmsg.content);
			//			//Currentevacuated_CCSListbySender=CurrentReport.Cas_evacuated_List ;					
			//			CurrentrouteNEEDClearwreckageSender  = CurrentReport.routeNEEDClearwreckage_List ;	
			//			CurrentrouteNEEDCleartrafficSender =CurrentReport.routeNEEDCleartraffic_List ;

			break;
			//------------------------------- 
		case InformNewReceivingHospital:
			Mybody.CommTrigger= RespondersTriggers.GetNewRecivingHospital;
			//CurrentHospitalRecord =((HospitalRecord ) currentmsg.content);
			CurrentHospitalRecord_list = (List<HospitalRecord>) currentmsg.content;
			break;
		case   InformFirestSituationAssessmentReport: //from 
			Mybody.CommTrigger= RespondersTriggers.GetReport;
			CurrentReport=((Report) currentmsg.content);
			System.out.println(Mybody.Id + " 77777777777777777777777777777777777777777777777777777777777error 77777777777777777777777777777777777777777777777777777777777777777777777777777777 " );
			break;
		case InformNewReceivingEOCReport:
			Mybody.CommTrigger= RespondersTriggers.GetEOCReport; //from EOC
			CurrentReport=((Report) currentmsg.content);
			break;
			//=====================================
		case CallForSilverMeeting:
			Mybody.CommTrigger= RespondersTriggers.GetCallForSilverMeeting; //from Police
			Current_SliverMeeting  =((SliverMeetingRecord) currentmsg.content);
			break;
		default:
			done= true;
		} // end switch

		//System.out.println(Mybody.Id + "  " + Mybody.CommTrigger + "  from  "+ Mybody.CurrentSender.Id  + "  Action:  " + Mybody.Action);
	}	

	//##############################################################################################################################################################	
	//													Plan
	//##############################################################################################################################################################	
	// Create Plan-(list of OPActivity) for first time 
	public void Implmenting_plan() {

		//   Communication, SceneResponse,  CCSResponse ,loadingandTnasporation ,HospitalSelection  
		// ------------------------------------- Create Activity plan------------------------------------------ 
		//1-
		Activity_ambulance A1 = new Activity_ambulance(Ambulance_ActivityType.Communication ,Ambulance_ResponderRole.AmbulanceCommunicationsOfficer ,true    ); // SingleActivity true
		Tactical_Plan.add( A1);
		if (TempAIC)	{ A1.BronzCommander= (Responder_Ambulance) Mybody.Myvehicle.AssignedDriver;A1.Tempcommnader=true ; A1.ActivityStart();	}	
		//---------------------------------------------
		//2-
		Activity_ambulance  A3 = new Activity_ambulance(Ambulance_ActivityType.CCSResponse,Ambulance_ResponderRole.CasualtyClearingOfficer,false )  ; 
		Tactical_Plan.add( A3);
		A3.TA=Mybody.assignedIncident.CCStation ;
		//---------------------------------------------
		//3-
		if ( Mybody.assignedIncident.TaskMechanism_IN==TaskAllocationMechanism.NoEnvironmentsectorization ) //one sector
		{
			Activity_ambulance  A2= new Activity_ambulance(Ambulance_ActivityType.SceneResponse ,Ambulance_ResponderRole.AmbulanceSectorCommander,false ) ; 
			Tactical_Plan.add( A2);
			A2.inSector=Mybody.assignedIncident.Sectorlist2.get(0); 
		}
		else
		{			
			for ( int i=0;i<   Mybody.assignedIncident.NOSector  ;i++)
			{ 
				Activity_ambulance  A2= new Activity_ambulance(Ambulance_ActivityType.SceneResponse ,Ambulance_ResponderRole.AmbulanceSectorCommander,false ); 
				Tactical_Plan.add( A2);

				if ( ConsiderSectorNeedinAssignedResponders == false)
					A2.inSector=  Mybody.assignedIncident.Sectorlist2.get(i);   // create random 
				else
				{
					Sector  NominatedS=null;  
					for ( Sector  S : Mybody.assignedIncident.Sectorlist2 ) // create priority
						if ( S.ProirityCreation== i  )
						{ NominatedS= S; break;}
					A2.inSector= NominatedS; 
				}
			}
		}
		//---------------------------------------------				
		//4-
		Activity_ambulance  A5 = new Activity_ambulance(Ambulance_ActivityType.loadingandTnasporation,Ambulance_ResponderRole.AmbulanceLoadingCommander,true )  ; 
		Tactical_Plan.add( A5);
		A5.TA=Mybody.assignedIncident.loadingArea;

		//---------------------------------------------
		//5-
		Activity_ambulance  A6 = new Activity_ambulance(Ambulance_ActivityType.HospitalSelection , Ambulance_ResponderRole.AmbulanceIncidentCommander ,true   );
		Tactical_Plan.add( A6);
		A6.BronzCommander=Mybody;
		A6.ActivityStart();
	};

	//***************************************************************************************************	
	private Activity_ambulance GetActivity(Ambulance_ActivityType xx)
	{
		Activity_ambulance result=null;
		for ( Activity_ambulance A :Tactical_Plan) 
			if ( A.Activity == xx)
				result= A ;		
		return result ;		 
	}

	//***************************************************************************************************	
	// Resource Allocation for Activity   output command of role and task for  bronze commander or responders
	public Command AssigneRoleandActiviytoResponder(  Responder_Ambulance _Responder  , Ambulance_ActivityType SpecificActivity    ) {

		boolean implmnetActivity=false;
		Command CMD1 =new Command();
		MajorActivity_Command=null ;

		//System.out.println(Mybody.Id + "Tactical_Plan.size()"+ Tactical_Plan.size() );

		//-------------------------------------- Implementation  allocate BZC
		for ( Activity_ambulance A :Tactical_Plan) 
		{		
			if ( A.ActivityStatus==GeneralTaskStatus.Inprogress &&  A.Activity==Ambulance_ActivityType.Communication && A.Tempcommnader==true   ) 
			{				
				TempACO= A.BronzCommander;
				InstrcuteTempACOtoleave=true; 
				A.Tempcommnader=false;

				//System.out.println(Mybody.Id + "TempACO "+TempACO.Id );
				A.ResourseAllocation_commander(_Responder );				
				CMD1.AmbulanceCommand( "0" , A.BronzRole  );				
				implmnetActivity=true;break;
			}					
			else if ( A.ActivityStatus==GeneralTaskStatus.Waiting && A.BronzCommander==null && A.Activity==Ambulance_ActivityType.SceneResponse  ) 
			{
				A.ResourseAllocation_commander(_Responder );
				CMD1.AmbulanceCommand( "0" , A.BronzRole  ,null, A.inSector ,null );
				A.ActivityStart(); // if you consider location arrival you have to delete this
				implmnetActivity=true;break;				
			}
			else if ( A.ActivityStatus==GeneralTaskStatus.Waiting && A.BronzCommander==null && A.Activity==Ambulance_ActivityType.CCSResponse ) 
			{
				A.ResourseAllocation_commander(_Responder );
				CMD1.AmbulanceCommand( "0" , A.BronzRole  , A.TA , null ,null);
				A.ActivityStart(); 
				implmnetActivity=true;break;				
			}	

			else if ( A.ActivityStatus==GeneralTaskStatus.Waiting && A.BronzCommander==null && A.Activity==Ambulance_ActivityType.loadingandTnasporation ) 
			{
				A.ResourseAllocation_commander(_Responder );
				CMD1.AmbulanceCommand( "0" , A.BronzRole  , A.TA , null, (ArrayList<Responder_Ambulance>) UnoccupiedDrivers);
				A.ActivityStart(); 
				implmnetActivity=true;break;				
			}

		}

		//------------------------------------- During response how to deploy responders to activity //equal responders 

		if ( !implmnetActivity  &&  SpecificActivity ==null  )   //no awareness about the activity  need
		{
			int MinimumResponders=99;
			Activity_ambulance  selectedA=null;

			for ( Activity_ambulance  A : Tactical_Plan ) 		
				if ( ( A.ActivityStatus==GeneralTaskStatus.Inprogress &&  A.Activity==Ambulance_ActivityType.SceneResponse  && ! respondrsBack )|| ( A.ActivityStatus==GeneralTaskStatus.Inprogress && A.Activity==Ambulance_ActivityType.CCSResponse)  )
				{
					if (A.AllocatedResponderforThisActivity.size()< MinimumResponders   )
					{selectedA=A;MinimumResponders=A.AllocatedResponderforThisActivity.size();}						
				}

			if (selectedA!=null )
			{

				selectedA.ResourseAllocation_Responders(_Responder);

				if (  _Responder.Role==Ambulance_ResponderRole.None) 
					CMD1.AmbulanceCommand( "0" , Ambulance_ResponderRole.Paramedic);
				else
				{
					; //// for come back responders
				}

				if (selectedA.Activity ==Ambulance_ActivityType.CCSResponse)			
				{
					MajorActivity_Command=new Command();
					MajorActivity_Command.AmbulanceCommand( "0" , Ambulance_ActivityType.CCSResponse,  selectedA.TA ,null ) ;
				}
				else
				{	
					MajorActivity_Command=new Command(); 
					MajorActivity_Command.AmbulanceCommand( "0" , Ambulance_ActivityType.SceneResponse  ,null, selectedA.inSector ) ;	
					selectedA.inSector.CurrentPramdeicResponderinSector ++; 
					//System.out.println( Mybody.Id + "  " + selectedA.inSector.ID  ) ;
				}		
			}
			else
			{
				// need work
				System.out.println( Mybody.Id +  " Ambulance errroooorrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrr  new responders notask in assigned task  "); // if all of them  has max
			}

			//-------------------------------------
			//for ( int i=0;i<  InputFile.NOSector  ;i++)   //  high priority 0, 1, then 2 
			//System.out.println("Noconsider----"+ Mybody.Id +" " + i +  " sector ...total   " +Mybody.CasualtySeen_list.size() +" CasualityinThisSector :    "  + Mybody.assignedIncident.Sectorlist.get(i).CasualityinThisSector   +"  ProirityCreation :   "  + Mybody.assignedIncident.Sectorlist.get(i).ProirityCreation  +" NeedResponders:    "  + Mybody.assignedIncident.Sectorlist.get(i).NeedResponders  +"  Meet :   "  + Mybody.assignedIncident.Sectorlist.get(i).Meet  +"    CurrentResponderinSector : "  + Mybody.assignedIncident.Sectorlist.get(i).CurrentResponderinSector  );


		}
		//-------------------------------------- 
		else if (!implmnetActivity  && SpecificActivity !=null)
		{


		}


		//-------------------------------------- During response   randomly or Least sector or FIFO more severity  how to adopt

		else if (!implmnetActivity  && ConsiderSectorNeedinAssignedResponders )   //2- send to specific sector
		{						
			int NumcasultyforParamdic=0;  
			int TotalEstimatedPramdic=0;

			for (Responder_Ambulance Res : MyResponder_Ambulance)
			{
				if ( Res.Role==Ambulance_ResponderRole.None ||Res.Role==Ambulance_ResponderRole.Paramedic )
					TotalEstimatedPramdic++;
			}

			NumcasultyforParamdic = ( AssessSituation_casualtycount / TotalEstimatedPramdic ) ; // minus sector commander 

			//1- who more need sector
			double LeastMeet=999;   Sector  NominatedS=null ;
			for ( Sector  S : Mybody.assignedIncident.Sectorlist2 )
			{

				S.NeedResponders =Math.floor (S.CasualityinThisSector/NumcasultyforParamdic) ;			
				S.Meet= S.CurrentPramdeicResponderinSector - S.NeedResponders  ;
				if ( S.Meet <    LeastMeet  )
				{LeastMeet= S.Meet ; NominatedS= S; }
				else if ( S.Meet == LeastMeet )
				{
					if ( S.CurrentPramdeicResponderinSector < NominatedS.CurrentPramdeicResponderinSector  )
					{ NominatedS= S; }
				}
			}


			Activity_ambulance  selectedA=null;
			for ( Activity_ambulance  A : Tactical_Plan ) 		
				if ( A.ActivityStatus==GeneralTaskStatus.Inprogress &&  A.Activity== Ambulance_ActivityType.SceneResponse  )
				{
					if (A.inSector == NominatedS )
					{selectedA=A;}
				}

			selectedA.ResourseAllocation_Responders(_Responder);	
			//CMD1.AmbulanceCommand( "0" , Ambulance_ResponderRole.Paramedic  , selectedA.inSector  );  selectedA.inSector.CurrentPramdeicResponderinSector ++;

			//-------------------------------------
			for ( int i=0;i<  Mybody.assignedIncident.NOSector  ;i++)   //  high priority 0, 1, then 2 
				System.out.println("consider----"+ Mybody.Id +" " + i +  " sector ...total   " +Mybody.CasualtySeen_list.size() + " NumcasultyforParamdic : "+NumcasultyforParamdic + " CasualityinThisSector :    "  + Mybody.assignedIncident.Sectorlist2.get(i).CasualityinThisSector   +"  ProirityCreation :   "  + Mybody.assignedIncident.Sectorlist2.get(i).ProirityCreation  +" NeedResponders:    "  + Mybody.assignedIncident.Sectorlist2.get(i).NeedResponders  +"  Meet :   "  + Mybody.assignedIncident.Sectorlist2.get(i).Meet  +"    CurrentResponderinSector : "  + Mybody.assignedIncident.Sectorlist2.get(i).CurrentPramdeicResponderinSector );

		}

		//-------------------------------------- 
		if ( this.UnoccupiedResponders.size()== 0)
		{
			System.out.println("============================Ambulance================================") ; 

			for ( Activity_ambulance  A : Tactical_Plan ) 		

			{
				System.out.println(A.Activity  + "       "  + A.AllocatedResponderforThisActivity.size()) ; 
			}
			System.out.println("============================================================") ; 
		}

		return CMD1;
	}

	//***************************************************************************************************
	// Resource Allocation 		
	public Task_ambulance Allocate_HospitalforCasualty(Allocation_Strategy Strategy) 
	{
		Task_ambulance nominatedTask=null; //Casualty
		HospitalRecord nominatedHospital=null ;

		switch(Strategy) {
		case FIFO:	
			//FIFO casualty to assign ambulance
			double longer_starttime=999999999 ;  
			for (Task_ambulance Task : Casualty_Plan) 
				if (Task.TaskStatus == GeneralTaskStatus.Waiting)
				{	
					if (Task.TaskCreateTime <longer_starttime)
					{ longer_starttime= Task.TaskCreateTime ;  nominatedTask=Task;}
				}

			break;
		case Severity_RYGPriorty:
			// identify more priority task-casualty- to assign ambulance
			double More_priorityRYG=4 ; 

			for (Task_ambulance Task : Casualty_Plan)
			{
				if    (Task.TaskStatus == D_Ontology.Ontology.GeneralTaskStatus.Waiting   && Task.priority_level!=5   )
				{
					if (Task.priority_level < More_priorityRYG   ) //&& SectorDoneFieldTraige.contains(Task.InSector)
						More_priorityRYG=Task.priority_level ;  nominatedTask=Task;					
				}
			}

			for (Task_ambulance Task : Casualty_Plan)
			{
				if (  Task.priority_leveldgetbad== true && Task.priority_level!=5  )  	
				{
					if (Task.priority_level <= More_priorityRYG   ) //&& SectorDoneFieldTraige.contains(Task.InSector)
						More_priorityRYG=Task.priority_level ;  nominatedTask=Task;					
				}
			}

			break; }


		//for (HospitalRecord  h :RecivingHospital_list) 			
		//System.out.println( Mybody.Id + "___________________________________________________AIC Hospital list: " + h.RecivingHospital.ID +"     " +h.OrderofNearsttomyLocation );


		//for(Task_ambulance ca:   Casualty_Plan    )
		//	if (  ca.priority_leveldgetbad== true  )  
		//		System.out.println( Mybody.Id + "___________________________________________________" +ca.TaskStatus +"   "+ ca.TargetCasualty.ID   +"     " +ca.priority_level+ "  " +  ca.priority_leveldgetbad +"   "+ca.AssignedHospital.ID );

		// allocation hospital  basd on nearst to location  and triage 
		for (HospitalRecord  Hosp:RecivingHospital_list)
		{
			if ( nominatedTask.priority_level== Hosp.OrderofNearsttomyLocation)
			{
				nominatedHospital=Hosp; 
				Hosp.CurrentReservedBedsCount --;
				break;
			}	
		}

		nominatedTask.AssignedHospital=  nominatedHospital.RecivingHospital ;
		return nominatedTask;

	}

	//##############################################################################################################################################################	
	//													Reporting
	//##############################################################################################################################################################	
	// Reporting ........not used
	public void Reporting_planexxxxxxxx(  ) {

		int TotalCasualties=0 ,TotalCasualties_waiting=0 ,TotalCasualties_inprogress=0 , TotalCasualties_done=0 ;
		int TotalResponders=0 ,RespondersFree=0 ,RepondersBusy=0;

		TotalResponders=MyResponder_Ambulance .size() ;
		RespondersFree=UnoccupiedResponders .size();
		RepondersBusy=MyResponder_Ambulance .size()- UnoccupiedResponders .size();

		//----------------------
		TotalCasualties= 0;
		TotalCasualties_waiting=0 ;
		TotalCasualties_inprogress=0 ;
		TotalCasualties_done=0 ;


		for ( Task_ambulance Task : Casualty_Plan ) 	 {

			if (   Task.TaskStatus == D_Ontology.Ontology.GeneralTaskStatus.Done) {

				TotalCasualties_done ++ ;
			}
			if (  Task.TaskStatus == D_Ontology.Ontology.GeneralTaskStatus.Waiting) {

				TotalCasualties_waiting ++;
			}
			if ( Task.TaskStatus == D_Ontology.Ontology.GeneralTaskStatus.Inprogress) {

				TotalCasualties_inprogress++; 
			}

		}
		//Evacuation
		TotalCasualties= TotalCasualties_waiting +TotalCasualties_inprogress + TotalCasualties_done ;

		// create Report that contains all details to update the Silver commander about current Operational_Plan .
		Report Report1 =new Report();
		Report1.RS= ReportSummery.None;

		//	Report1.Ambulance_ReportCasualties_Bronze( TotalResponders,RespondersFree ,RepondersBusy , TotalCasualties ,TotalCasualties_waiting ,TotalCasualties_inprogress , TotalCasualties_done ,Ambulance_TaskType.TransferCasualtytoHospital );


		// send message
		//Mybody.CurrentMessage  = new  ACL_Message( ACLPerformative.InformSituationReport ,Mybody ,Mybody.assignedIncident.ACOcommander,Report1, Mybody.CurrentTick ,Mybody.assignedIncident.ComMechanism_inControlArea,1 ) ;
		Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);

	};	

	//***************************************************************************************************	
	//First report
	public void Reporting_FRSituationAssessment()
	{
		Report Report1 =new Report();
		Report1.Ambulance_FRSituationAssessment( Mybody.assignedIncident ,ReportSummery.AdditionalResourceRequest , NEEDfromAESSMENT  );

		// send message
		Mybody.CurrentMessage  = new  ACL_Message( ACLPerformative.InformFirestSituationAssessmentReport ,Mybody ,Mybody.assignedIncident.ACOcommander ,Report1, Mybody.CurrentTick ,Mybody.assignedIncident.ComMechanism_inControlArea,1,TypeMesg.Inernal ) ;
		Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
		//System.out.println( Mybody.assignedIncident.ID +" Amambulance   AdditionalResourceRequest===============================================1========================================================================="  + NEEDfromAESSMENT );
	}

	//***************************************************************************************************	
	//update report 
	public void Reporting_URSituationAssessmentx()
	{


		int counterRed=0;

		//(counterRed -TotalCasualtiesRed_transfer )

		boolean dontriage=true;
		for ( Activity_ambulance  A : Tactical_Plan ) 
			if (  A.Activity==Ambulance_ActivityType.SceneResponse && A.Triage!=true  && A.ActivityStatus==GeneralTaskStatus.Inprogress)
			{dontriage=false;  break;} //not done


		if ( dontriage  && doneARRequest)	//one time
		{
			for ( Task_ambulance TofCa : Casualty_Plan)
			{

				//if ( TofCa.TargetCasualty.priority_level_AfterTriage== 1)
				//counterRed++;
			}

			doneARRequest=false;
			counterRed= counterRed - this.UnoccupiedDrivers.size() ;
			System.out.println( Mybody.assignedIncident.ID +  "this.UnoccupiedDrivers.size()" + this.UnoccupiedDrivers.size());
		}

		Report Report1 =new Report();
		Report1.Ambulance_URSituationAssessment( Mybody.assignedIncident ,ReportSummery.AdditionalResourceRequest , counterRed  );

		// send message
		//Mybody.CurrentMessage  = new  ACL_Message( ACLPerformative.InformSituationReport ,Mybody ,Mybody.assignedIncident.ACOcommander ,Report1, Mybody.CurrentTick ,Mybody.assignedIncident.ComMechanism_inControlArea,1 ) ;
		Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);

		if (counterRed !=0  ) System.out.println( Mybody.assignedIncident.ID +"   AdditionalResourceRequest========================================2================================================================================"  + counterRed );

	}

	//***************************************************************************************************	
	//End Report
	public void Reporting_ENDSituationAssessment()
	{

		// Send Final message to its  ACO  nothings
		Report Report1 =new Report();
		Report1.Ambulance_EndReport(Mybody.assignedIncident ,ReportSummery.EndER_Ambulance );

		Mybody.CurrentMessage = new  ACL_Message( ACLPerformative.InformERendReport , Mybody,Mybody.assignedIncident.ACOcommander ,Report1, Mybody.CurrentTick , Mybody.assignedIncident.ComMechanism_inControlArea ,1,TypeMesg.Inernal) ;
		Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);

	}

	//***************************************************************************************************	
	public void BoradcastEndofERAction()
	{

		List<Responder_Ambulance> Listofreceiver = new ArrayList<Responder_Ambulance>();

		for ( Activity_ambulance  A : Tactical_Plan ) 
		{ if ( A.BronzCommander !=Mybody)
		{	Listofreceiver.add(A.BronzCommander); A.ActivityStatus= GeneralTaskStatus.Done ;} }  //System.out.println( A.BronzCommander.Id +" xxxxxxxxxListofreceiverxxxxxxx ")

		Mybody.CurrentMessage  = new  ACL_Message( ACLPerformative.InformERend , Mybody,Listofreceiver ,null, Mybody.CurrentTick,CommunicationMechanism.RadioSystem_BoradCast ,1,TypeMesg.Inernal) ;
		Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
	}
	//***************************************************************************************************	
	//SilverMeeting
	public void BoradcastSilverMeetingAction(SliverMeetingRecord SR)
	{
		List<Responder> Listofreceiver = new ArrayList<Responder>();
		Listofreceiver.add(Mybody.assignedIncident.PICcommander ) ;
		Listofreceiver.add(Mybody.assignedIncident.FICcommander ) ;

		// locationSilverMeeting need to think 
		Mybody.CurrentMessage  = new  ACL_Message( ACLPerformative.CallForSilverMeeting, Mybody,Listofreceiver ,SR, Mybody.CurrentTick,CommunicationMechanism.FF_BoradCast,1,TypeMesg.External ) ; // if there is sharing  chanle
		Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);

	}
	//##############################################################################################################################################################	
	//													Interpreted Triggers 
	//##############################################################################################################################################################	
	//not used
	public void AssessStatus(  )
	{		
		int MaximumWaiting=-1;
		Activity_ambulance  selectedA=null;

		for ( Activity_ambulance  A : Tactical_Plan ) 		
			if ( A.Activity== Ambulance_ActivityType.SceneResponse && A.ActivityStatus==GeneralTaskStatus.Inprogress  )
			{
				int waiting= A.lastReport().TotalCasualties_waiting;
				if (waiting >MaximumWaiting  )
				{selectedA=A ; MaximumWaiting=waiting;}
			}

		//based on report
	}

	//----------------------------------------------------------------------------------------------------
	public boolean IsThereRecivingHospital()
	{	
		boolean  Result=false;

		for (HospitalRecord H: RecivingHospital_list) 
			if (H.ISThereBed ()) 
			{
				Result=true;
				break;
			}

		return Result;
	}

	//----------------------------------------------------------------------------------------------------
	public int EstimatedNeedforHospital()
	{	
		int need=0 ,Totalcaualty=0 ;

		//1- check update
		//System.out.println(Mybody.Id + "RecivingHospital_list  "+RecivingHospital_list.size());
		for (HospitalRecord H: RecivingHospital_list) 
			if ( ! H.checkedAIC)	
			{Totalbedavilibeuntilnow =Totalbedavilibeuntilnow + H.CurrentReservedBedsCount ;H.checkedAIC=true;}

		//--------------------------
		//2-check new cas
		for (Task_ambulance Task : Casualty_Plan) 
			if (Task.TaskStatus == D_Ontology.Ontology.GeneralTaskStatus.Waiting  && Task.CountinHospNeed==false)	
			{ Totalcaualty++;Task.CountinHospNeed=true ;}

		//--------------------------
		if (  Totalcaualty >0 && Totalbedavilibeuntilnow >= Totalcaualty )
		{ 
			need=0;
			Totalbedavilibeuntilnow=Totalbedavilibeuntilnow-Totalcaualty;
		}
		else if (  Totalcaualty > 0 && Totalcaualty >Totalbedavilibeuntilnow   )
		{ 			

			need=Totalcaualty- Totalbedavilibeuntilnow ;
			Totalbedavilibeuntilnow= Totalbedavilibeuntilnow-Totalcaualty;

		}  

		//--------------------------
		if (Mybody.InterpretedTrigger==RespondersTriggers.ThereisneedforHospital && need>0)
		{
			ThereIsneedformoreHospitalcapacity=ThereIsneedformoreHospitalcapacity + need;
		}
		else if ( Mybody.InterpretedTrigger==null && need>0 )
		{
			Mybody.InterpretedTrigger=RespondersTriggers.ThereisneedforHospital;
			ThereIsneedformoreHospitalcapacity=need;
		}


		//System.out.println(Mybody.Id + "   total need  "+ThereIsneedformoreHospitalcapacity + " need "+ need  + " Totalbedavilibeuntilnow  " + Totalbedavilibeuntilnow );
		System.out.println(Mybody.Id + "  xxxxxxxxxxxxxxxxAICxxxxxxxxxxxxxxxx" +  Mybody.InterpretedTrigger  );
		return need ;
	}

	//----------------------------------------------------------------------------------------------------
	public boolean EndofER()
	{
		boolean done=true;

		if (Tactical_Plan.size()== 0 )
		{done=false;}

		for ( Activity_ambulance  A : Tactical_Plan ) 
			if (( A.Activity   ==Ambulance_ActivityType.SceneResponse || A.Activity   ==Ambulance_ActivityType.CCSResponse ||  A.Activity   == Ambulance_ActivityType.loadingandTnasporation ) && A.ActivityStatus!=GeneralTaskStatus.Done  )  //exept comunication and hospital 
			{done=false; break;}

		//if ( done==true ) System.out.println("amb");
		return done ;
	}

	//##############################################################################################################################################################	
	//												Evacuation && Hospital Interpreted Triggers 
	//##############################################################################################################################################################	
	//----------------------------------------------------------------------------------------------------
	public boolean IsNewCasualty( Casualty _Casualty )
	{	
		boolean  Result=true;

		for (Task_ambulance T : this.Casualty_Plan) 
			if (T.TargetCasualty  == _Casualty  ) 
			{
				Result=false;
				break;
			}

		return Result;
	}

	//----------------------------------------------------------------------------------------------------
	public boolean IsThereCasualtyneedtoEvacuation()
	{	
		boolean  Result=false;

		for (Task_ambulance Task : Casualty_Plan) 
			if ((Task.TaskStatus == D_Ontology.Ontology.GeneralTaskStatus.Waiting   && Task.priority_level!=5   ) ||  (  Task.priority_leveldgetbad== true  && Task.priority_level!=5  ))
			{
				Result=true;
				break;
			}

		return Result;
	}

	//----------------------------------------------------------------------------------------------------
	public boolean IsThereCasualtyneedtoSendAllocation()
	{	
		boolean  Result=false;

		for (Task_ambulance Task : Casualty_Plan) 
			if (Task.TaskStatus == D_Ontology.Ontology.GeneralTaskStatus.Inprogress   && Task.Send_H_AllocationTOCCS==false ) 
			{
				Result=true;
				break;
			}

		return Result;
	}

	//----------------------------------------------------------------------------------------------------	
	public Task_ambulance CasualtyneedtoSendAllocation_GET()
	{	
		Task_ambulance  Result=null;
		int Maxp=4;

		for (Task_ambulance Task : Casualty_Plan) 
			if (Task.TaskStatus == D_Ontology.Ontology.GeneralTaskStatus.Inprogress   && Task.Send_H_AllocationTOCCS==false ) 
			{
				if ( Task.priority_level < Maxp )
				{ Result=Task;}
			}

		return Result;
	}

	//----------------------------------------------------------------------------------------------------
	public boolean STartPrioirytoHospital(  STartEvacuationtoHospital_Startgy ST )
	{
		boolean done=false;

		// Strategy  1----------------------------
		if (  ST==STartEvacuationtoHospital_Startgy.Field_EndTrigaeAllCasualty ) 
		{
			done=true;
			for ( Activity_ambulance  A : Tactical_Plan ) 
				if ( A.Activity   ==Ambulance_ActivityType.SceneResponse  &&  !A.Triage )  //exept comunication and hospital 
				{done=false; break;}
		}

		// Strategy  2----------------------------
		else if (  ST==STartEvacuationtoHospital_Startgy.Field_REDCasultyOnllyuntilEndTrigaeAllCasualty) 
		{
			//red only			
			done=false;			
			for (Sector S  :SectorDoneFieldTraige)
			{
				for (Task_ambulance T : Casualty_Plan) 
					if (T.TaskStatus == D_Ontology.Ontology.GeneralTaskStatus.Waiting && T.InSector==S  && T.priority_level==1  ) 
					{done=true; break;}
			}

			//no more red then check all sector done to consider anothe proirity
			if (done==false )
			{
				done=true;
				for ( Activity_ambulance  A : Tactical_Plan ) 
					if ( A.Activity   ==Ambulance_ActivityType.SceneResponse  &&  !A.Triage )  //exept comunication and hospital 
					{done=false; break;}
			}			
		}
		// Strategy  3---------------------------- used  HERRRRRRRRRRRRRRRRR
		else if (  ST==STartEvacuationtoHospital_Startgy.CCS_REDCasultyOnllyuntilEndSceneOpration ) 
		{
			//red only			
			done=false;			
			for (Task_ambulance T : Casualty_Plan) 
				if (T.TaskStatus == D_Ontology.Ontology.GeneralTaskStatus.Waiting &&  T.priority_level==1  ) 
				{done=true; break;}

			//no more red then check all sector done to consider anothe proirity
			if (done==false )
			{
				if (  NomorecasultyinInner >= 1)
					done=true ;
			}		
		}
		// Strategy  4----------------------------
		else if (  ST==STartEvacuationtoHospital_Startgy.CCS_MorepriorityinCSS)
		{
			done=true ;
		}

		return done ;
	}

	//##############################################################################################################################################################	
	//													Actions 
	//##############################################################################################################################################################	
	public void AssessSituation_begincountAction() 
	{			
		//Mybody.Generate_AssessZones() ;

		Mybody.Setup_ConsiderSeverityofcasualtyinSearach(false );
		Mybody.ClockDirection=1;						
		Mybody.CasualtySeen_list.clear();		
		Mybody.StopDistnation=Mybody.assignedIncident.bluelightflashing_Point ;		
		Mybody.ArrivedSTOP=false;


		if ( Mybody.assignedIncident.Survey_scene_Mechanism_IN== SurveyScene_SiteExploration_Mechanism.Organized_exploration )
		{
			//Mybody.MyDirectiontowalk=Mybody.assignedIncident.IdentfyMyStartDirection(Mybody.Return_CurrentLocation());	
			//Mybody.NextRotateDirectionSearch_bigRoute(Mybody.ClockDirection);	
			//Mybody.walkingstrategy=RandomWalking_StrategyType.FourDirections_big;		
			//Mybody.Assign_DestinationLocation_Serach();  //inside Mybody.RandomllyCountCasualty=true;	

			Mybody.MyDirectiontowalk=Mybody.assignedIncident.IdentfyMyStartDirection(Mybody.Return_CurrentLocation());	
			Mybody.NextRotateDirectionSearch_smallRoute(Mybody.ClockDirection);
			Mybody.walkingstrategy=RandomWalking_StrategyType.FourDirections_small;		
			Mybody.Assign_DestinationLocation_Serach();  //inside Mybody.RandomllyCountCasualty=true;	

		}
		else if ( Mybody.assignedIncident.Survey_scene_Mechanism_IN== SurveyScene_SiteExploration_Mechanism.Randomly_exploration )
		{
			//Mybody.Reset_DirectioninSearach( ); //its memory  uesd in MI
			Mybody.MyDirectiontowalk=Mybody.assignedIncident.IdentfyMyStartDirection(Mybody.Return_CurrentLocation());	
			Mybody.walkingstrategy=RandomWalking_StrategyType.OneDirection_sector;
			Mybody.Assign_DestinationLocation_Serach();
		}	

		SurveyScene =true;
	}

	//----------------------------------------------------------------------------------------------------
	public int AssessSituation_endcountAction() 
	{
		int count=0;	
		count = Mybody.CasualtySeen_list.size();		
		Mybody.RandomllyCountCasualty=false;

		//Object_Zone Count_MaximumZonewithCasualty()	
		//Mybody.Destroy_AssessZones();	

		if ( Mybody.assignedIncident.Survey_scene_Mechanism_IN== SurveyScene_SiteExploration_Mechanism.Organized_exploration )
		{
			NEEDfromAESSMENT=0;

			//for ( Casualty  ca: Mybody.CasualtySeen_list )
			//	if ( ca.priority_level_AfterTriage==1    )   //approximate numbers of priority 1   
			//		NEEDfromAESSMENT++;
		}
		else if ( Mybody.assignedIncident.Survey_scene_Mechanism_IN== SurveyScene_SiteExploration_Mechanism.Randomly_exploration )
		{
			NEEDfromAESSMENT=0 ; //do notings
		}	

		return count;
	}

	//----------------------------------------------------------------------------------------------------
	public boolean  ISthereupdateTosend( )
	{
		int Thereisupdate =0;

		if ( CCS_established==1  )
			Thereisupdate++;
		//if (LoadingArea_established== 1 )
		//Thereisupdate++;

		if ( routeNEEDClearwreckage_List.size() >0 )
			Thereisupdate++;
		//4
		if ( routeNEEDCleartraffic_List.size() >0 )
			Thereisupdate++;					
		//5
		if (Dead_List_CCS.size() >0 || Cainfor_evacuation_List.size() >0  )
			Thereisupdate++;
		//6
		if ( NomorecasultyinCCS== 1)
			Thereisupdate++;
		//7
		if ( NomorecasultyinAL== 1)
			Thereisupdate++;
		//8
		if (  EndofER()  && EndER_Ambulance==-1 && Tactical_Plan.size()!= 0  )
			Thereisupdate++;
		//-------------------

		if (Thereisupdate>0 )
			return true;
		else
			return false;
	}

	//----------------------------------------------------------------------------------------------------
	public void   SliverMeetingADD()
	{

		int Thereisupdate=0;

		boolean _CCS_established=false ,  _LoadingArea_established=false ;
		//set update in Meeting record 
		//1
		if (SliverMeetings.size()==1)
		{
			Current_SliverMeeting.AIC_Addtomeeting_GeolocationTA(); 
			if ( SurveyScene )Current_SliverMeeting.SectorPlanning(Mybody.assignedIncident.Survey_scene_Mechanism_IN, (ArrayList<Casualty>) Mybody.CasualtySeen_list); 

			Thereisupdate++;
		}
		//2
		if ( CCS_established==1  )
		{CCS_established=2;_CCS_established=true ;Thereisupdate++;}

		//if (LoadingArea_established== 1 )
		//{LoadingArea_established= 2;_LoadingArea_established=true ;Thereisupdate++;}

		if ( _CCS_established  )
		{  Current_SliverMeeting.AIC_Addtomeeting_EstablishedTA(  _CCS_established  , _LoadingArea_established);}
		//3
		if ( routeNEEDClearwreckage_List.size() >0 )
		{ Thereisupdate=Thereisupdate+ routeNEEDClearwreckage_List.size() ;  Current_SliverMeeting.AIC_Addtomeeting_RouteW( routeNEEDClearwreckage_List  ); routeNEEDClearwreckage_List .clear(); }
		//4
		if ( routeNEEDCleartraffic_List.size() >0 )
		{ Thereisupdate=Thereisupdate+  routeNEEDCleartraffic_List.size()  ; Current_SliverMeeting.AIC_Addtomeeting_RouteT( routeNEEDCleartraffic_List );routeNEEDCleartraffic_List .clear(); }					
		//5
		if (Dead_List_CCS.size() >0 || Cainfor_evacuation_List.size() >0  )
		{ 
			Thereisupdate=Thereisupdate+ Dead_List_CCS.size() +Cainfor_evacuation_List.size() ;
			Current_SliverMeeting.AIC_Addtomeeting_Casualty( Dead_List_CCS, Cainfor_evacuation_List);
			Dead_List_CCS.clear(); Cainfor_evacuation_List.clear();	
		}
		//6
		if ( NomorecasultyinCCS== 1 && Mybody.assignedIncident.Communication_Structure_uesd==Inter_Communication_Structure.Struc1_SilverCommandersInteraction )
		{ Current_SliverMeeting.AIC_Addtomeeting_NomorecasultyinCCS();NomorecasultyinCCS=2 ;Thereisupdate++;}
		//7
		if ( NomorecasultyinAL== 1  && Mybody.assignedIncident.Communication_Structure_uesd==Inter_Communication_Structure.Struc1_SilverCommandersInteraction )
		{ Current_SliverMeeting.AIC_Addtomeeting_NomorecasultyinLA();NomorecasultyinAL=2 ;Thereisupdate++;}
		//8
		if (  EndofER()  && EndER_Ambulance==-1 && Tactical_Plan.size()!= 0  )
		{Current_SliverMeeting.AIC_Addtomeeting_EndER_Ambulance();EndER_Ambulance=2;Thereisupdate++;DoneLastMeeting=true;}
		//-------------------

		Current_SliverMeeting.Thereisupdate_Amb=Thereisupdate++; ;

	}

	//----------------------------------------------------------------------------------------------------
	public void  SliverMeetingGET()
	{
		//0-
		if (SliverMeetings.size()==1)
		{
			//ScheduleParameters params = ScheduleParameters.createRepeating(Mybody.CurrentTick + 1 , 1,ScheduleParameters.FIRST_PRIORITY);

			//ISchedulableAction xx1 = schedule.createAction(params,Mybody.assignedIncident.loadingArea222,"step");
			// ISchedulableAction xx2 = schedule.createAction(params,Mybody.assignedIncident.CCStation,"step");

			Mybody.assignedIncident.CreateTA_CCSandloadingArea( 60 , 10 ) ;	

			System.out.println("PRIORITY_execution =>  "+ InputFile.PRIORITY_execution +"   "  +"CCS" );	
			ScheduleParameters params1 = ScheduleParameters.createRepeating(Mybody.schedule.getTickCount() + 1 , 1,InputFile.PRIORITY_execution ++ );
			Mybody.schedule.schedule(params1,Mybody.assignedIncident.CCStation,"step");

			System.out.println("PRIORITY_execution =>  "+ InputFile.PRIORITY_execution +"   "  +"LA" );
			ScheduleParameters params2 = ScheduleParameters.createRepeating(Mybody.schedule.getTickCount() + 1 , 1,InputFile.PRIORITY_execution ++ );
			Mybody.schedule.schedule(params2,Mybody.assignedIncident.loadingArea,"step");

			DoneFirstMeeting=true;

			Mybody.UpdateComNet(Mybody.assignedIncident.PICcommander);  //Update No of messages
			Mybody.UpdateComNet(Mybody.assignedIncident.FICcommander);  //Update No of messages
			Mybody.CurrentMessage  = new  ACL_Message( ACLPerformative.CallForSilverMeeting, Mybody.assignedIncident.PICcommander,Mybody ,null, Mybody.CurrentTick,CommunicationMechanism.SilverMeeting,2 ,TypeMesg.External) ;
			Mybody.Message_inboxMeeting.add(Mybody.CurrentMessage ); 
			Mybody.CurrentMessage  = new  ACL_Message( ACLPerformative.CallForSilverMeeting, Mybody.assignedIncident.FICcommander,Mybody ,null, Mybody.CurrentTick,CommunicationMechanism.SilverMeeting ,2,TypeMesg.External) ;
			Mybody.Message_inboxMeeting.add(Mybody.CurrentMessage ); 
			

			this.ConsiderSectorNeedinAssignedResponders= Current_SliverMeeting.AIC_Getmeeting_sectorplan() ;
		}			
		//1-
		//		if ( Current_SliverMeeting.AIC_Getmeeting_RouteW(   ).size() >0 )
		//		{
		//			for( RoadLink RL :Current_SliverMeeting.AIC_Getmeeting_RouteW(   ) )
		//				routeClearedwreckage_List.add(RL);
		//		}
		//		//2-
		//		if ( Current_SliverMeeting.AIC_Getmeeting_RouteT(   ).size() >0 )
		//		{
		//			for( RoadLink RL :Current_SliverMeeting.AIC_Getmeeting_RouteT(   ) )
		//				routeClearedtraffic_List.add(RL);
		//		}
		//3-
		//if (   Current_SliverMeeting.AIC_Getmeeting_TA_controlarea()  )  // For FIC  only
		//{
		//	TA_controlarea_established=1 ;
		//}

		//4-
		if (   Current_SliverMeeting.AIC_Getmeeting_TA_Sector(   ) ) // For SC
		{
			TA_Sector_established=1 ;
			Mybody.UpdateComNet(Mybody.assignedIncident.FICcommander);  //Update No of messages
			Mybody.CurrentMessage  = new  ACL_Message( ACLPerformative.CallForSilverMeeting, Mybody.assignedIncident.FICcommander,Mybody ,null, Mybody.CurrentTick,CommunicationMechanism.SilverMeeting,1,TypeMesg.External ) ;
			Mybody.Message_inboxMeeting.add(Mybody.CurrentMessage ); 
		}
		//5-
		if (   Current_SliverMeeting.AIC_Getmeeting_TA_cordons_outer()  )  // For ALC start and infrom its comming responders
		{
			TA_cordon_established=1 ;
			Mybody.UpdateComNet(Mybody.assignedIncident.PICcommander);  //Update No of messages
			Mybody.CurrentMessage  = new  ACL_Message( ACLPerformative.CallForSilverMeeting, Mybody.assignedIncident.PICcommander,Mybody ,null, Mybody.CurrentTick,CommunicationMechanism.SilverMeeting,1,TypeMesg.External ) ;
			Mybody.Message_inboxMeeting.add(Mybody.CurrentMessage ); 
		}

		//3
		if ( Current_SliverMeeting.AIC_Getmeeting_CasualtyTrapped(   ).size() >0 )
		{
			for( Casualty ca :Current_SliverMeeting.AIC_Getmeeting_CasualtyTrapped() )
				Trapped_List.add(ca);

			Mybody.UpdateComNet(Mybody.assignedIncident.FICcommander);  //Update No of messages
			Mybody.CurrentMessage  = new  ACL_Message( ACLPerformative.CallForSilverMeeting, Mybody.assignedIncident.FICcommander,Mybody ,null, Mybody.CurrentTick,CommunicationMechanism.SilverMeeting,Trapped_List.size() ,TypeMesg.External) ;
			Mybody.Message_inboxMeeting.add(Mybody.CurrentMessage ); 
		}

		if ( Current_SliverMeeting.AIC_Getmeeting_NomorecasultyinInner()  )
		{
			NomorecasultyinInner=1 ;
			Mybody.UpdateComNet(Mybody.assignedIncident.FICcommander);  //Update No of messages
			Mybody.CurrentMessage  = new  ACL_Message( ACLPerformative.CallForSilverMeeting, Mybody.assignedIncident.FICcommander,Mybody ,null, Mybody.CurrentTick,CommunicationMechanism.SilverMeeting,1 ,TypeMesg.External ) ;
			Mybody.Message_inboxMeeting.add(Mybody.CurrentMessage ); 
		}

		if ( Current_SliverMeeting.AIC_Getmeeting_EndER_Fire())
		{	EndER_Fire= 2 ; 
		Mybody.UpdateComNet(Mybody.assignedIncident.FICcommander);  //Update No of messages
		Mybody.CurrentMessage  = new  ACL_Message( ACLPerformative.CallForSilverMeeting, Mybody.assignedIncident.FICcommander,Mybody ,null, Mybody.CurrentTick,CommunicationMechanism.SilverMeeting ,1,TypeMesg.External) ;
		Mybody.Message_inboxMeeting.add(Mybody.CurrentMessage ); 
		}

		if ( Current_SliverMeeting.AIC_Getmeeting_EndER_Police())
		{	EndER_Police=2;

		Mybody.UpdateComNet(Mybody.assignedIncident.PICcommander);  //Update No of messages
		Mybody.CurrentMessage  = new  ACL_Message( ACLPerformative.CallForSilverMeeting, Mybody.assignedIncident.PICcommander,Mybody ,null, Mybody.CurrentTick,CommunicationMechanism.SilverMeeting ,1,TypeMesg.External) ;
		Mybody.Message_inboxMeeting.add(Mybody.CurrentMessage ); 
		}
	} 
	//______________________________________________________________________________________________________________________________________________________________
	//##############################################################################################################################################################
	//                                                        Behavior
	//##############################################################################################################################################################
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	// AIC commander - General 
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	public void AICBehavior()	
	{		
		//*************************************************************************
		// 1-FirstArrival response
		if( Mybody.SensingeEnvironmentTrigger==RespondersTriggers.FirstArrival_NoResponder ) 				
		{
			Mybody.CurrentAssignedActivity=Ambulance_TaskType.CoordinateFirstArrival;

			NeedFirstMeeting= true;
			DoneFirstMeeting=false;
			DoneLastMeeting=false;

			//			Mybody.CurrentAssignedActivity=Ambulance_TaskType.CoordinateER ;
			//			Mybody.InterpretedTrigger= RespondersTriggers.TempAICofFirstArrival ;
			//			Mybody.SensingeEnvironmentTrigger= null;
		}
		// 2-CoordinateER
		else if ( Mybody.InterpretedTrigger== RespondersTriggers.TempAICofFirstArrival|| Mybody.CommTrigger==RespondersTriggers.ICRolehanded 	) 
		{
			Mybody.CurrentAssignedActivity=Ambulance_TaskType.CoordinateER ;
		}
		// 3-Handover
		else if (Mybody.Action==RespondersActions.Noaction &&  Mybody.CommTrigger== RespondersTriggers.GetinstructiontoHandover 	) 
		{
			Mybody.CurrentAssignedActivity=Ambulance_TaskType.HandovertoAIC;
		}
		// 4-Ending
		else if ( Mybody.InterpretedTrigger==RespondersTriggers.DoneEmergencyResponse  )   
		{
			Mybody.CurrentAssignedActivity=Ambulance_TaskType.CoordinateEndResponse;
		}	
		else  if (Mybody.InterpretedTrigger==RespondersTriggers.ENDER  || Mybody.InterpretedTrigger==RespondersTriggers.ICRolehanded ) 
		{
			Mybody.PrvRoleinprint1=Mybody.Role ;
			Mybody.Role=Ambulance_ResponderRole.None;
		}

		//*************************************************************************
		switch(Mybody.CurrentAssignedActivity) {
		case CoordinateFirstArrival:  //Temp AIC
			CommanderBehavior_CoordinateFirstArrival();	
			break;
		case	HandovertoAIC : //Temp AIC
			CommanderBehavior_HandovertoAIC();	
			break;
		case CoordinateER :			
			CommanderBehavior_CoordinateER();
			break;			
		case CoordinateEndResponse:
			CommanderBehavior_EndER();
			break;}

		//System.out.println(Mybody.Id+" "  +Mybody.CurrentTick +  "  Action:" + Mybody.Action + " com:" + Mybody.CommTrigger + "   Nt:" + Mybody.InterpretedTrigger+ " en:" + Mybody.SensingeEnvironmentTrigger + " end:" + Mybody.EndActionTrigger +  "Acknowledged: "+ Mybody.Acknowledged+"sending: "+  Mybody.Sending  + "CurrentMessage: ");

	}// end ParamedicBehavior

	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	// Temp AIC - Coordinate FirstArrival
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^	
	private void CommanderBehavior_CoordinateFirstArrival()
	{
		// ++++++ 1- +++++++
		if( Mybody.SensingeEnvironmentTrigger==RespondersTriggers.FirstArrival_NoResponder )
		{						

			if ( ( Mybody.assignedIncident.FICcommander !=null || Mybody.assignedIncident.PICcommander !=null )   )
			{
				Mybody.ActivityAcceptCommunication=true;
				Mybody.SensingeEnvironmentTrigger=null;

				Mybody.InterpretedTrigger= RespondersTriggers.TempAICofFirstArrival;
				Mybody.Action=RespondersActions.Noaction;
				Mybody.BehaviourType1=RespondersBehaviourTypes1.WaitingDuring ;

				System.out.println(Mybody.Id + " AssessSituation Done1"  );
			}

			else
			{
				Mybody.Action=RespondersActions.Observeandcount;
				Mybody.BehaviourType1=RespondersBehaviourTypes1.Planformulation ;			
				Mybody.SensingeEnvironmentTrigger=null;

				Mybody.ActivityAcceptCommunication=false;
				AssessSituation_begincountAction() ;  // Here concurrent Action
			}
		}
		// ++++++ 2- +++++++
		else if (Mybody.Action==RespondersActions.Observeandcount && ( Mybody.SensingeEnvironmentTrigger!= RespondersTriggers.Arrived_WalkedinAllScene && Mybody.SensingeEnvironmentTrigger !=RespondersTriggers.Arrived_WalkedalongOneDirection))
		{			
			Mybody.Walk(); //with AssessSituation
		}	
		// ++++++ 3- +++++++
		else if (Mybody.Action==RespondersActions.Observeandcount && (Mybody.SensingeEnvironmentTrigger== RespondersTriggers.Arrived_WalkedinAllScene || Mybody.SensingeEnvironmentTrigger==RespondersTriggers.Arrived_WalkedalongOneDirection)) 
		{ 					

			AssessSituation_casualtycount=AssessSituation_endcountAction() ;  // Here concurrent Action			
			this.Reporting_FRSituationAssessment();
			Time_last_updated=Mybody.CurrentTick;

			Mybody.Action=RespondersActions.ReportAssessSituation ;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Comunication ; 
			Mybody.SensingeEnvironmentTrigger=null;	

			//System.out.println(Mybody.Id + " AssessSituation Done2 --------1"  );
			//	System.out.println(Mybody.Id  +  "  Action: " + Mybody.Action + " com:" + Mybody.CommTrigger + "   Nt:" + Mybody.InterpretedTrigger+ " en:" +Mybody.SensingeEnvironmentTrigger + " end:" + Mybody.EndActionTrigger +"   " + Mybody.Acknowledged   );
			//System.out.println(Mybody.assignedIncident.ACOcommander.Id  +  "  Action: " + Mybody.assignedIncident.ACOcommander.Action + " com:" + Mybody.assignedIncident.ACOcommander.CommTrigger + "   Nt:" + Mybody.assignedIncident.ACOcommander.InterpretedTrigger+ " en:" +Mybody.assignedIncident.ACOcommander.SensingeEnvironmentTrigger + " end:" + Mybody.assignedIncident.ACOcommander.EndActionTrigger +"   " + Mybody.assignedIncident.ACOcommander.Acknowledged   );

		}	
		// ++++++ 4- +++++++
		else if (Mybody.Action==RespondersActions.ReportAssessSituation && Mybody.Acknowledged)
		{		
			Mybody.ActivityAcceptCommunication=true;
			Mybody.Acknowledged=false;Mybody.Sending=false; 	
			Mybody.InterpretedTrigger= RespondersTriggers.TempAICofFirstArrival;

			Mybody.Action=RespondersActions.Noaction;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.WaitingDuring ;

			System.out.println(Mybody.Id + " AssessSituation Done2----------2"  );
		}	
	}

	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	// Temp AIC - HandovertoAIC
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^	
	private void  CommanderBehavior_HandovertoAIC()  //temp
	{
		// ++++++ 1- +++++++
		if( Mybody.CommTrigger== RespondersTriggers.GetinstructiontoHandover 	 )
		{						
			//her yiu have more work
			Mybody.Action=RespondersActions.GetcammandtoHandover ;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.RoleAssignment;
			Mybody.EndofCurrentAction=  InputFile.GetCommand_duration  ; 
			Mybody.CommTrigger=null;
		}
		// ++++++ 2- +++++++
		else if ( Mybody.Action==RespondersActions.GetcammandtoHandover  && Mybody.EndActionTrigger== RespondersTriggers.EndingAction 	) 			 	
		{							
			//Actually there is no report send but it will be done through Commander_AIC_HandoverAction 
			Mybody.CurrentMessage = new  ACL_Message( ACLPerformative.InformHandoverReport , Mybody, Mybody.CurrentSender , null, Mybody.CurrentTick ,Mybody.assignedIncident.ComMechanism_inControlArea,1,TypeMesg.Inernal ) ;
			Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);

			Mybody.Action=RespondersActions.HandovertoAIC;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.RoleAssignment;
			Mybody.EndActionTrigger=null;						
		}
		// ++++++ 3- +++++++
		else if ( Mybody.Action==RespondersActions.HandovertoAIC  &&     Mybody.Acknowledged 	) 			 	
		{		
			Mybody.InterpretedTrigger=RespondersTriggers.ICRolehanded ; //then he will get new commnd role

			Mybody.Role=Ambulance_ResponderRole.None;				
			Mybody.Action=RespondersActions.Noaction;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.WaitingDuring ;
			Mybody.Acknowledged=false;Mybody.Sending=false; 	
		}
	}

	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	// AIC - Coordinate   ER 
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	private void CommanderBehavior_CoordinateER()
	{								
		// ++++++ 1- +++++++
		if ( ( Mybody.CommTrigger==RespondersTriggers.ICRolehanded ||  Mybody.InterpretedTrigger== RespondersTriggers.TempAICofFirstArrival ) && Tactical_Plan.size()== 0 &&  !NeedFirstMeeting )   	
		{
			//Implmenting_plan(); dif viresion 
			Mybody.Action=RespondersActions.FormulatePlan;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Planformulation ;
			Mybody.EndofCurrentAction=  InputFile.FormulatePlan_duration ; 

			if ( Mybody.InterpretedTrigger== RespondersTriggers.TempAICofFirstArrival)Mybody.InterpretedTrigger=null; 
			if ( Mybody.CommTrigger==RespondersTriggers.ICRolehanded  )Mybody.CommTrigger=null;
		}
		// ++++++ 2- +++++++
		else if ( Mybody.Action==RespondersActions.Noaction  && Tactical_Plan.size()== 0 &&  NeedFirstMeeting && DoneFirstMeeting  )   	
		{
			Implmenting_plan();
			DoneFirstMeeting=false ;

			Mybody.Action=RespondersActions.FormulatePlan;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Planformulation ;
			Mybody.EndofCurrentAction=  InputFile.FormulatePlan_duration ; 
		}
		// ++++++ 3- +++++++
		else if ( Mybody.Action==RespondersActions.FormulatePlan  && Mybody.EndActionTrigger== RespondersTriggers.EndingAction 	) 			 	
		{							
			Mybody.Action=RespondersActions.Noaction;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.WaitingDuring;
			Mybody.EndActionTrigger=null;	
		}
		//-------------------------------------------------------------------------------------------- (P1-called) Silver meeting
		// ++++++ 4- +++++++
		else if (Mybody.Action==RespondersActions.Noaction  && Mybody.CommTrigger== RespondersTriggers.GetCallForSilverMeeting    )
		{
			// go to location 
			Mybody.ActivityAcceptCommunication=false;
			
			Current_SliverMeeting.AIC_Attendance(Mybody ) ;
			SliverMeetings.add(Current_SliverMeeting);
			
			Mybody.Action=RespondersActions.GetCallForSilverMeeting;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.ComunicationDelay;
			Mybody.CommTrigger=null;
			
			Mybody.ActivityAcceptCommunication=false;		
			Mybody.SendingReciving_internal= false; 
			Mybody.SendingReciving_External= true ; 
		}
		// ++++++ 5- +++++++
		else if ( (Mybody.Action==RespondersActions.GetCallForSilverMeeting || Mybody.Action==RespondersActions.CallForSilverMeeting ) &&  Mybody.InterpretedTrigger== RespondersTriggers.SliverMeetingStart  )
		{


				
			SliverMeetingADD();

			Mybody.Action=RespondersActions.Meeting ;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Comunication; 
			Mybody.InterpretedTrigger=null;

			System.out.println("                                                                                                                                 " +  "SM=======================" +Mybody.Id + " AIC in sliver meeting" );
		}
		// ++++++ 6- +++++++
		else if (Mybody.Action==RespondersActions.Meeting  && Mybody.InterpretedTrigger == RespondersTriggers.SliverMeetingEnd  )
		{
			Mybody.ActivityAcceptCommunication=true;
			SliverMeetingGET();
			Current_SliverMeeting=null;

			Mybody.Action=RespondersActions.UpdatePlan;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Planformulation ;
			Mybody.EndofCurrentAction= InputFile.UdatePlan_duration ; 
			Mybody.InterpretedTrigger=null; 
			
			Mybody.SendingReciving_internal= false; 
			Mybody.SendingReciving_External= false; 

			System.out.println("                                                                                                                                 " + "SM=======================" +Mybody.Id + " AIC end sliver meeting" );
		}

		//-------------------------------------------------------------------------------------------- (P1-need) Silver Meeting  call
		else  if ( Mybody.Action==RespondersActions.Noaction && Mybody.CommTrigger==null && ( Mybody.assignedIncident.FICcommander !=null && Mybody.assignedIncident.PICcommander !=null ) &&     
				(  
						( Mybody.assignedIncident.Inter_Communication_Time_used==Communication_Time.When_need && Mybody.assignedIncident.Communication_Structure_uesd==Inter_Communication_Structure.Struc1_SilverCommandersInteraction  && ISthereupdateTosend( )  ) ||				
						(    EndofER()  && EndER_Ambulance==-1 && Tactical_Plan.size()!= 0  && !DoneLastMeeting &&  Mybody.assignedIncident.Communication_Structure_uesd==Inter_Communication_Structure.Struc2_BronzeCommandersInteraction ) 
						))
		{			
			System.out.println("                                                                                                                                 " + "SM=======================" + Mybody.Id + " BoradcastcallForsliverMeeting  " +  " " + Mybody.CurrentTick  );

			// current location or control area 
			PointDestination locationSilverMeeting=null ; 

			if (Mybody.assignedIncident.ControlArea !=null && Mybody.assignedIncident.ControlArea.installed)
				locationSilverMeeting=Mybody.assignedIncident.ControlArea.Location ;
			else
				locationSilverMeeting=Mybody.assignedIncident.bluelightflashing_Point ;

			Current_SliverMeeting = new SliverMeetingRecord ("0",locationSilverMeeting , Mybody.assignedIncident) ;	

			//ScheduleParameters params = ScheduleParameters.createRepeating(Mybody.CurrentTick + 1 , 1,ScheduleParameters.FIRST_PRIORITY);

			System.out.println("PRIORITY_execution =>  "+ InputFile.PRIORITY_execution +"   "  +Current_SliverMeeting.MeetingID);
			ScheduleParameters params = ScheduleParameters.createRepeating(Mybody.schedule.getTickCount() + 1 , 1,InputFile.PRIORITY_execution ++ );
			Mybody.schedule.schedule(params,Current_SliverMeeting,"step");



			SliverMeetings.add(Current_SliverMeeting);
			BoradcastSilverMeetingAction(Current_SliverMeeting );	//call 

			Mybody.Action=RespondersActions.BoradcastcallForsliverMeeting;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.ComunicationDelay ; 
			Mybody.EndofCurrentAction=   InputFile.Boradcast_duration_FFcall ;
			
			Mybody.ActivityAcceptCommunication=false;
			Mybody.SendingReciving_internal= false; 
			Mybody.SendingReciving_External= true ; 
		}
		// ++++++ 5- +++++++
		else if (Mybody.Action==RespondersActions.BoradcastcallForsliverMeeting && Mybody.EndActionTrigger== RespondersTriggers.EndingAction  )
		{
			Current_SliverMeeting.AIC_Attendance(Mybody );
			Mybody.Action=RespondersActions.CallForSilverMeeting ;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.ComunicationDelay; 
			Mybody.EndActionTrigger=null;
			Mybody.Sending=false; 
		}


		//-------------------------------------------------------------------------------------------- (P2)
		//==========================New Responder==========================
		// ++++++ 7- +++++++
		else if (Mybody.Action==RespondersActions.Noaction &&  ( Mybody.CommTrigger == RespondersTriggers.GetNewResponderarrived ||  Mybody.CommTrigger == RespondersTriggers. GetbackResponderarrived))			  										
		{
			//UpdatePlan
			if (  Mybody.CommTrigger == RespondersTriggers.GetNewResponderarrived )
			{
				MyResponder_Ambulance.add(NewResponder) ;

				if (NewResponder.Role ==Ambulance_ResponderRole.None) //not driver
				{	UnoccupiedResponders.add(NewResponder);
				}
				else	
				{	UnoccupiedDrivers.add(NewResponder);  
				// System.out.println(Mybody.Id + " GetDriver"+ NewResponder.Id );
				}
			}
			else if (  Mybody.CommTrigger == RespondersTriggers.GetbackResponderarrived )
			{
				UnoccupiedResponders.add(BackResponder);
				respondrsBack=true;

				//System.out.println("  "+ "AIC: " +Mybody.Id + " GetNewResponderarrived1" );

			}

			Mybody.Action=RespondersActions.GetNewArrivalNotification ;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Comunication;  
			Mybody.EndofCurrentAction=  InputFile.GetNotification_duration  ;
			//reciving=true;
		}
		// ++++++ 8- +++++++
		else if ( Mybody.Action==RespondersActions.GetNewArrivalNotification  && Mybody.EndActionTrigger== RespondersTriggers.EndingAction 	) 			 	
		{				
			if (  Mybody.CommTrigger == RespondersTriggers.GetNewResponderarrived )
				NewResponder.Acknowledg(Mybody);	//Always F-F
			else
				BackResponder.Acknowledg(Mybody); 
			//reciving=true;
			Mybody.Action=RespondersActions.UpdatePlan;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Planformulation ;
			Mybody.EndofCurrentAction=   InputFile.UdatePlan_duration  ; 
			Mybody.EndActionTrigger=null; 
			Mybody.CommTrigger=null; 
			//System.out.println(Mybody.Id + " GetNewResponderarrived2" );
		}
		//==========================Result of Triage============================== Electronic only
		// ++++++ 9- +++++++
		else if (Mybody.Action==RespondersActions.Noaction &&   Mybody.assignedIncident.CasualtyReport_Mechanism_IN== CasualtyReportandTrackingMechanism.Electronic   
				&&  ( Mybody.CommTrigger== null && Mybody.assignedIncident.NewRecordcasualtyadded_ISSystem(Mybody.Role,null,0)>0 )  )	//after SecondTriaged	  										
		{	 	

			Mybody.Action=RespondersActions.GetResultTriage ;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Comunication; 

			TriageResultReport_RecList=Mybody.assignedIncident.GetTriageResults_ISSystem(Mybody.Role);
			Mybody.SendingReciving_External= false ;  Mybody.SendingReciving_internal= true ;
			Mybody.EndofCurrentAction= (InputFile.GetInfromation_Electronic_Duration * TriageResultReport_RecList.size()) ;			
		}
		// ++++++ 10- +++++++
		else if ( Mybody.Action==RespondersActions.GetResultTriage && Mybody.EndActionTrigger== RespondersTriggers.EndingAction 	) 			 	
		{
			//UpdatePlan
			for(ISRecord    Rec:TriageResultReport_RecList) {

				if (  IsNewCasualty( Rec.CasualtyinRec)   )
				{
					Task_ambulance Newtask = new Task_ambulance( Rec.CasualtyinRec, Ambulance_TaskType.TransferCasualtytoHospital ,Rec.AssignedSC ,null); 
					Newtask.priority_level=Rec.priority_levelinRec ;
					Casualty_Plan.add(Newtask);	

				}
				else
				{
					for (Task_ambulance T : this.Casualty_Plan) 
						if (    T.TargetCasualty  == Rec.CasualtyinRec) 
						{
							T.priority_level= Rec.priority_levelinRec  ;

							if (T.priority_level!=5)							
								T.priority_leveldgetbad= true;
							else
							{
								T.TaskStatus=GeneralTaskStatus.Done;	
								T.Decased=true ;
							}
						}

				}

			}

			//ThereIsneedformoreHospitalcapacity=ThereIsneedformoreHospitalcapacity +
			//if ( !Iamrequest )EstimatedNeedforHospital();  // ???????????????????????????????????????????

			Mybody.Action=RespondersActions.UpdatePlan;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Planformulation ;
			Mybody.EndofCurrentAction= InputFile.UdatePlan_duration ; 
			Mybody.EndActionTrigger=null; 

			//System.out.println(Mybody.Id + " GetresultofTriageReport " );
		}
		//==========================Hospital Capacity Reported==========================
		// ++++++ 11- +++++++
		else if (Mybody.Action==RespondersActions.Noaction &&  Mybody.CommTrigger== RespondersTriggers.GetNewRecivingHospital   )		  										
		{	 														
			//UpdatePlan
			//RecivingHospital_list.add(CurrentHospitalRecord) ;	
			RecivingHospital_list=CurrentHospitalRecord_list;

			Mybody.Action=RespondersActions.GetRecivingHospitalResult;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Comunication; 	
			Mybody.EndofCurrentAction= InputFile.GetInfromation_FtoForR_Duration_Data * RecivingHospital_list.size() ; 
			Mybody.CommTrigger=null; 	//reciving=true;

			//System.out.println("  "+ "AIC: " +Mybody.Id+" GetNewRecivingHospital 777777777777777777777777777777777777 ");
		}
		// ++++++ 12- +++++++
		else if ( Mybody.Action==RespondersActions.GetRecivingHospitalResult  && Mybody.EndActionTrigger== RespondersTriggers.EndingAction 	) 			 	
		{						
			Mybody.CurrentSender.Acknowledg(Mybody);	//reciving=true;// F-F or radio

			Mybody.Action=RespondersActions.UpdatePlan;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Planformulation ;
			Mybody.EndofCurrentAction= InputFile.UdatePlan_duration ; 
			Mybody.EndActionTrigger=null; 
		}

		//==========================Get OP Report from BZC=========================================
		// ++++++ 15- +++++++
		else if (Mybody.Action==RespondersActions.Noaction && 
				( Mybody.CommTrigger== RespondersTriggers.GetOPReport  || Mybody.CommTrigger== RespondersTriggers.GetBronzeCommanderstart ||  Mybody.CommTrigger== RespondersTriggers.ResultofSetupTA)  )		  										
		{	 	

			int total=1 ;
			// update plan 								
			//1 -F A
			if (  Mybody.CommTrigger== RespondersTriggers.GetBronzeCommanderstart)
			{
				; //Start activity
				total++;

				for ( Activity_ambulance  A : Tactical_Plan ) 		
					if ( A.Activity== Ambulance_ActivityType.SceneResponse && A.BronzCommander == this.CurrentBZC )  
						startSR=1;
					else if ( A.Activity== Ambulance_ActivityType.loadingandTnasporation&& A.BronzCommander == this.CurrentBZC ) 
						startLA=1;
					else if ( A.Activity== Ambulance_ActivityType.CCSResponse && A.BronzCommander == this.CurrentBZC )  
						startCCS=1;


			}
			// 2- Setup
			if (  Mybody.CommTrigger== RespondersTriggers.ResultofSetupTA )
			{
				if ( CCS_established==0  )
					CCS_established =1;
				if ( LoadingArea_established==0  )
					LoadingArea_established= 1;
				total++;
			}			
			// 2- Route from ALC
			if (    CurrentrouteNEEDClearwreckageSender!=null && CurrentrouteNEEDClearwreckageSender.size() >0  )
			{
				for( RoadLink RL :CurrentrouteNEEDClearwreckageSender )
				{routeNEEDClearwreckage_List.add(RL);total++;}

				CurrentrouteNEEDClearwreckageSender.clear();
			}
			if (   CurrentrouteNEEDCleartrafficSender!=null && CurrentrouteNEEDCleartrafficSender.size() >0  )
			{

				for( RoadLink RL : CurrentrouteNEEDCleartrafficSender )
				{routeNEEDCleartraffic_List.add(RL);	total++;}

				CurrentrouteNEEDCleartrafficSender.clear();
			}

			//3- casualty			
			if (   CurrentTriage_CCSListbySender!=null &&  CurrentTriage_CCSListbySender.size()>0  )
			{

				//UpdatePlan
				for(Casualty_info    Ca_inf:CurrentTriage_CCSListbySender)
				{
					if (  IsNewCasualty(Ca_inf.ca)   )
					{
						Task_ambulance Newtask = new Task_ambulance( Ca_inf.ca, Ambulance_TaskType.TransferCasualtytoHospital ,CurrentBZC ,null  ); 
						Newtask.priority_level=Ca_inf.priority_level ;

						Casualty_Plan.add(Newtask);	

					}
					else //not new 
					{
						for (Task_ambulance T : this.Casualty_Plan) 
							if (    T.TargetCasualty  == Ca_inf.ca ) 
							{
								T.priority_level = Ca_inf.priority_level ;	

								if (T.priority_level!=5)							
									T.priority_leveldgetbad= true;
								else
									T.TaskStatus=GeneralTaskStatus.Done;	
							}

					}
					total++;
				}
				CurrentTriage_CCSListbySender.clear();
				//System.out.println("  "+ "AIC: " +Mybody.Id+"TriageResultReport_CaList ");
				//ThereIsneedformoreHospitalcapacity=ThereIsneedformoreHospitalcapacity +
				//if ( !Iamrequest )EstimatedNeedforHospital();  // ???????????????????????????????????????????


			}

			if (   CurrentDead_CCSListbySender!=null &&  CurrentDead_CCSListbySender.size() >0  )
			{
				for( Casualty ca : CurrentDead_CCSListbySender )
				{ 	
					Dead_List_CCS.add(ca);
					//delet its postion from hospital    
					for (Task_ambulance Task : Casualty_Plan) 
						if (Task.TaskStatus == GeneralTaskStatus.Waiting   && Task.TargetCasualty== ca  )
							Task.TaskStatus = GeneralTaskStatus.Done;
						else if (Task.TaskStatus == GeneralTaskStatus.Inprogress   && Task.TargetCasualty== ca  )
						{
							Task.TaskStatus = GeneralTaskStatus.Done;

							for (HospitalRecord  Hosp:RecivingHospital_list)
								if ( Hosp.RecivingHospital    == Task.AssignedHospital)
									Hosp.CurrentReservedBedsCount ++ ;
						}
					total++;	
				}	

				CurrentDead_CCSListbySender.clear();
			}

			if (  this.Currentevacuated_CCSListbySender !=null && Currentevacuated_CCSListbySender.size() >0  )
			{
				for( Casualty ca : Currentevacuated_CCSListbySender )
				{Cainfor_evacuation_List.add(ca);total++;}

				Currentevacuated_CCSListbySender.clear();
			}

			//4- End work							
			if ( EndSceneResponse  ) 
			{
				//UpdatePlan
				for ( Activity_ambulance  A : Tactical_Plan ) 
					if ( A.BronzCommander ==CurrentBZC  && A.Activity==Ambulance_ActivityType.SceneResponse )
					{A.ActivityStatus=GeneralTaskStatus.Done  ;  EndSceneResponse=false;  SectorDoneFieldTraige.add(A.inSector); break;} //not done

				//if ( SectorDoneFieldTraige.size() == Mybody.assignedIncident.NOSector)
				//	updateCCONomorecasulty=true;

				System.out.println("  "+ "AIC: " +Mybody.Id+"  SectorDoneffTraige:  "  + SectorDoneFieldTraige.size());
				total++;

				if (NomorecasultyinInner==-1 ) NomorecasultyinInner=2; 

			}
			if (   EndCCSResponse )
			{
				//UpdatePlan
				for ( Activity_ambulance  A : Tactical_Plan ) 
					if ( A.BronzCommander == CurrentBZC  && A.Activity==Ambulance_ActivityType.CCSResponse)
					{A.Treatment=true; A.ActivityStatus=GeneralTaskStatus.Done ;  EndCCSResponse=false ;updateALCNomorecasulty=true ;NomorecasultyinCCS=1 ;   break;}
				total++;
			}
			if (EndLoadingandTransportation )
			{
				//UpdatePlan
				for ( Activity_ambulance  A : Tactical_Plan ) 
					if ( A.BronzCommander ==CurrentBZC && A.Activity==Ambulance_ActivityType.loadingandTnasporation )
					{A.ActivityStatus=GeneralTaskStatus.Done ;  EndLoadingandTransportation=false ;NomorecasultyinAL=1;  break;}
				total++;
			}

			Mybody.Action=RespondersActions.GetOPReport;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Comunication ; 			
			Mybody.EndofCurrentAction= InputFile.GetInfromation_FtoForR_Duration_Data * total ;	//Mybody.EndofCurrentAction= InputFile.GetReport_duration ;
			Mybody.CommTrigger=null; //reciving=true;

		}
		// ++++++ 16- +++++++
		else if ( Mybody.Action==RespondersActions.GetOPReport  && Mybody.EndActionTrigger== RespondersTriggers.EndingAction 	) 			 	
		{
			CurrentBZC.Acknowledg(Mybody);   //reciving=true;	

			Mybody.Action=RespondersActions.UpdatePlan;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Planformulation ;
			Mybody.EndofCurrentAction= InputFile.UdatePlan_duration ; 
			Mybody.EndActionTrigger=null; 
			System.out.println("  "+ "AIC: " +Mybody.Id+" GetOPReport from " +CurrentBZC.Role );
		}
		//==========================Get EOC Report=========================================
		// ++++++ 17- +++++++
		else if (Mybody.Action==RespondersActions.Noaction &&  Mybody.CommTrigger== RespondersTriggers.GetEOCReport && false    )		  										
		{	 	
			//do some thing
			//OP report  or arrival location 
			// update plan 						
			Mybody.Action=RespondersActions.GetReport;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Comunication ; 
			Mybody.EndofCurrentAction= InputFile.GetReport_duration ;
			Mybody.CommTrigger=null; //reciving=true;
		}
		// ++++++ 18- +++++++
		else if ( Mybody.Action==RespondersActions.GetReport  && Mybody.EndActionTrigger== RespondersTriggers.EndingAction 	) 			 	
		{
			Mybody.CurrentSender.Acknowledg(Mybody);  //reciving=true;	

			Mybody.Action=RespondersActions.UpdatePlan;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Planformulation ;
			Mybody.EndofCurrentAction= InputFile.UdatePlan_duration ; 
			Mybody.EndActionTrigger=null; 
			System.out.println("  "+ "AIC: " +Mybody.Id  +"  Get  EOC Report ");
		}
		//============================================================ ALL
		// ++++++ 19- +++++++
		else if (Mybody.Action==RespondersActions.UpdatePlan && Mybody.EndActionTrigger== RespondersTriggers.EndingAction  )
		{						
			Mybody.Action=RespondersActions.Noaction;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.WaitingDuring;
			Mybody.EndActionTrigger=null;
		}
		//-------------------------------------------------------------------------------------------- (P3)
		// ++++++ 20- +++++++
		else  if (Mybody.Action==RespondersActions.Noaction && (Mybody.CurrentTick >= Time_last_updated + InputFile.UpdatEOCEvery) && false ) 
		{
			//Reporting_URSituationAssessment(); // face-face with its ACO
			Mybody.Action=RespondersActions.InfromReport;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Comunication ;

		}
		// ++++++ 21- +++++++
		else if (Mybody.Action==RespondersActions.InfromReport && Mybody. Acknowledged )
		{
			Mybody.Action=RespondersActions.Noaction;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.WaitingDuring;
			Mybody.Acknowledged=false;Mybody.Sending=false; 
			Time_last_updated=Mybody.CurrentTick ;
			//System.out.println(Mybody.Id + " don inform report" +Mybody.assignedIncident.RadioSystem_ChanleFree() +"    "+Mybody.CurrentTick );
			//System.out.println("  "+ "AIC: " +Mybody.Id  +"  Infrom EOCReport ");
		}
		//-------------------------------------------------------------------------------------------- (P4)  AllocatResponders
		//================== Responders
		// ++++++ 22- +++++++
		else if ( Mybody.Action==RespondersActions.Noaction && Tactical_Plan.size()!= 0 && UnoccupiedResponders.size()>0	) 		 	
		{				
			CurrentResponder= UnoccupiedResponders.get(0);
			UnoccupiedResponders.remove(0);
			Taskoption=1;

			Allocation_Command = AssigneRoleandActiviytoResponder(CurrentResponder , null);
			if ( CurrentResponder.Role == Ambulance_ResponderRole.None ) 
				IsNewResponder= true ;
			else
				IsNewResponder= false ;

			Mybody.Action=RespondersActions.AllocatResponders;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Planformulation ;
			Mybody.EndofCurrentAction=   InputFile.Allocation_duration ;

			//System.out.println("  "+ "AIC: " +Mybody.Id  +"  assign role ");
		}
		// ++++++ 23- +++++++
		else if (Mybody.Action==RespondersActions.AllocatResponders && Taskoption==1 && Mybody.EndActionTrigger== RespondersTriggers.EndingAction  ) 
		{	
			// send message with command F-F			
			if (IsNewResponder )
			{
				Mybody.CurrentMessage = new  ACL_Message( ACLPerformative.Command , Mybody, CurrentResponder ,Allocation_Command, Mybody.CurrentTick,Mybody.assignedIncident.ComMechanism_inControlArea,1,TypeMesg.Inernal) ;
				Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
			}
			else 
			{
				Mybody.CurrentMessage = new  ACL_Message( ACLPerformative.Command , Mybody, CurrentResponder ,MajorActivity_Command , Mybody.CurrentTick,Mybody.assignedIncident.ComMechanism_inControlArea,1,TypeMesg.Inernal) ;
				Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
			}
			Mybody.Action=RespondersActions.CommandAsignRole;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Comunication; // RoleAssignment; 
			Mybody.EndActionTrigger=null;	
			Mybody.CurrentResItalktohimxxxx=CurrentResponder ;
			//System.out.println("  "+ "AIC: " +Mybody.Id  +"  assign role " + CurrentResponder.Id );
		}
		// ++++++ 24- +++++++
		else if (Mybody.Action==RespondersActions.CommandAsignRole && Taskoption==1 && Mybody.Acknowledged )
		{
			Mybody.Acknowledged=false;Mybody.Sending=false; 


			if (! InstrcuteTempACOtoleave)
			{

				if (  MajorActivity_Command !=null  && IsNewResponder )
				{
					Mybody.CurrentMessage = new  ACL_Message( ACLPerformative.Command , Mybody, CurrentResponder ,MajorActivity_Command , Mybody.CurrentTick,Mybody.assignedIncident.ComMechanism_inControlArea,1,TypeMesg.Inernal) ;
					Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
					MajorActivity_Command=null ;

					Mybody.Action=RespondersActions.CommandAsignRole;
					Mybody.BehaviourType1=RespondersBehaviourTypes1.Comunication; // RoleAssignment; 

				}
				else
				{
					Mybody.Action=RespondersActions.Noaction;
					Mybody.BehaviourType1=RespondersBehaviourTypes1.WaitingDuring;
					Taskoption= 0;
				}
			}			
			else
			{
				// send message with command to driver to leave if there role PCO
				Mybody.CurrentMessage = new  ACL_Message( ACLPerformative.Instructiontoleave ,Mybody, TempACO ,null, Mybody.CurrentTick,Mybody.assignedIncident.ComMechanism_inControlArea,1 ,TypeMesg.Inernal) ;
				Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
				Mybody.Action=RespondersActions.CommandAsignRole;
				Mybody.BehaviourType1=RespondersBehaviourTypes1.Comunication; // RoleAssignment; 

				InstrcuteTempACOtoleave=false;
				UnoccupiedDrivers.add(TempACO);  // not used
				this.MyResponder_Ambulance.add(TempACO);  // not used
				ThereisneedforHospital=true;
			}			
		}
		//================== Drivers
		// ++++++ 22- +++++++
		else if ( Mybody.Action==RespondersActions.Noaction && Tactical_Plan.size()!= 0 && UnoccupiedDrivers.size() >0	) 		 	
		{				
			CurrentDriver= UnoccupiedDrivers.get(0);
			UnoccupiedDrivers.remove(0);
			Taskoption= 2;  // 2 for driver
			Mybody.Action=RespondersActions.AllocatResponders;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Planformulation ;
			Mybody.EndofCurrentAction=   InputFile.Allocation_duration ;

			//System.out.println("  "+ "AIC: " +Mybody.Id  +"  assign driver role ");
		}
		// ++++++ 23- +++++++
		else if (Mybody.Action==RespondersActions.AllocatResponders && Taskoption==2 && Mybody.EndActionTrigger== RespondersTriggers.EndingAction  ) 
		{	
			Activity_ambulance  selectedA= GetActivity(Ambulance_ActivityType.loadingandTnasporation);
			selectedA.ResourseAllocation_Responders(CurrentDriver);	
			MajorActivity_Command=new Command();
			MajorActivity_Command.AmbulanceCommand( "0" , Ambulance_ActivityType.loadingandTnasporation,  selectedA.TA ,null ) ;

			Mybody.CurrentMessage = new  ACL_Message( ACLPerformative.Command , Mybody, CurrentDriver ,MajorActivity_Command , Mybody.CurrentTick,Mybody.assignedIncident.ComMechanism_inControlArea,1,TypeMesg.Inernal) ;
			Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
			MajorActivity_Command=null ;

			Mybody.Action=RespondersActions.CommandAsignRole;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Comunication; // RoleAssignment; 
			Mybody.EndActionTrigger=null;	
		}
		// ++++++ 24- +++++++
		else if (Mybody.Action==RespondersActions.CommandAsignRole&& Taskoption==2  && Mybody.Acknowledged )
		{

			Mybody.Action=RespondersActions.Noaction;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.WaitingDuring;
			Mybody.Acknowledged=false;Mybody.Sending=false; 
			Taskoption= 0 ;
		}
		//-------------------------------------------------------------------------------------------- (P5) Allocate  Hospital
		// ++++++ 25- +++++++
		else if (  Mybody.Action==RespondersActions.Noaction &&  Mybody.assignedIncident.CasualtyReport_Mechanism_IN== CasualtyReportandTrackingMechanism.Paper  && this.IsThereCasualtyneedtoSendAllocation()  )    
		{	

			AllocatedTaskforcasualtyevacuation=CasualtyneedtoSendAllocation_GET() ;

			//send message to CCO

			Command CMD1 =new Command();
			CMD1.AmbulanceCommand("0", AllocatedTaskforcasualtyevacuation.TaskType ,AllocatedTaskforcasualtyevacuation.TargetCasualty,  AllocatedTaskforcasualtyevacuation.AssignedHospital);

			Mybody.CurrentMessage = new  ACL_Message( ACLPerformative.InformHospitalAllocationResult,Mybody , Mybody.assignedIncident.CCO_ambcommander ,CMD1 ,  Mybody.CurrentTick,Mybody.assignedIncident.ComMechanism_level_StoB,1,TypeMesg.Inernal ) ;
			Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);

			AllocatedTaskforcasualtyevacuation.Send_H_AllocationTOCCS=true;
			Mybody.Action=RespondersActions.InfromAllocationResult;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Comunication;			

			//System.out.println("  "+ "AIC: " +Mybody.Id  +"     send  AllocationHospital  ");
		}
		// ++++++ 27- +++++++
		else if (Mybody.Action==RespondersActions.InfromAllocationResult && Mybody.Acknowledged  )
		{				
			Mybody.Action=RespondersActions.Noaction;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.WaitingDuring;
			Mybody.Acknowledged=false;Mybody.Sending=false; 
			//System.out.println("  "+ "AIC: " +Mybody.Id  +"   get Acknowledged after send  ");
		}
		// ++++++ 25- +++++++
		else if (  Mybody.Action==RespondersActions.Noaction && IsThereRecivingHospital()  && IsThereCasualtyneedtoEvacuation()  && this.STartPrioirytoHospital(Mybody.assignedIncident._STartEvacuationtoHospital_Startgy) )
		{			
			AllocatedTaskforcasualtyevacuation = Allocate_HospitalforCasualty( Allocation_Strategy.Severity_RYGPriorty) ;	// get bad or new

			Mybody.Action=RespondersActions.AllocatHospital;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Planformulation ;
			Mybody.EndofCurrentAction=   InputFile.Allocation_duration ;

			//System.out.println("  "+ "AIC: " +Mybody.Id  +"    AllocationHospital  ");
		}
		// ++++++ 26- +++++++
		else if (Mybody.Action==RespondersActions.AllocatHospital &&  Mybody.EndActionTrigger== RespondersTriggers.EndingAction  )
		{	
			//update plan
			if (AllocatedTaskforcasualtyevacuation.TaskStatus==GeneralTaskStatus.Waiting) AllocatedTaskforcasualtyevacuation.TaskStatus=GeneralTaskStatus.Inprogress ;
			if (AllocatedTaskforcasualtyevacuation.TaskStatus==GeneralTaskStatus.Inprogress ) { AllocatedTaskforcasualtyevacuation.Send_H_AllocationTOCCS=false; AllocatedTaskforcasualtyevacuation.priority_leveldgetbad=false;}
			AllocatedTaskforcasualtyevacuation.TaskStartTime = Mybody.CurrentTick ;

			if ( Mybody.assignedIncident.CasualtyReport_Mechanism_IN== CasualtyReportandTrackingMechanism.Electronic )
			{

				AllocatedTaskforcasualtyevacuation.Send_H_AllocationTOCCS=true;

				ISRecord Record=Mybody.assignedIncident.ReturnRecordcasualtyISSystem( AllocatedTaskforcasualtyevacuation.TargetCasualty );
				Record.UpdateISRecord_Hospital( AllocatedTaskforcasualtyevacuation.AssignedHospital);

				//for(ISRecord     Rec: Mybody.assignedIncident.ISSystem) 
				//	System.out.println( Rec.CasualtyinRec.ID +" --------   "+ Rec.priority_levelinRec + "  "+ Rec.FieldTriaged + "  "+ Rec.SecondTriaged+ "  "+ Rec.Treated + "  "+ Rec.TransferdV+ "  "+ Rec.TransferdH  +" checkedALC_AllocateAmb  " + Rec.checkedALC_AllocateAmb +" checkedCCO_DoneHosandAmb " + Rec.checkedCCO_DoneHosandAmb +" checkedCCO_DoneonAmb  " + Rec.checkedCCO_DoneonAmb  + "checkedAIC" +Rec.checkedAIC_DoneSecondTriage );
			}

			AllocatedTaskforcasualtyevacuation=null;

			Mybody.Action=RespondersActions.Noaction;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.WaitingDuring;
			Mybody.EndActionTrigger=null;
		}
		//-------------------------------------------------------------------------------------------- (P6) Request or updates to bronze commanders
		//==========================Request for Hospital   ==============================  from ACO
		// ++++++ 32- +++++++
		else if (  Mybody.Action==RespondersActions.Noaction &&  ThereisneedforHospital    )	//InterpretedTrigger after each time recvie triage result
		{			
			ThereIsneedformoreHospitalcapacity = 40;
			option=99 ;
			Command CMD1 =new Command();
			CMD1.AmbulanceCommand("0", Ambulance_TaskType.LookforHospital, ThereIsneedformoreHospitalcapacity  );
			Mybody.CurrentMessage = new  ACL_Message( ACLPerformative.Command, Mybody , Mybody.assignedIncident.ACOcommander ,CMD1  ,  Mybody.CurrentTick,Mybody.assignedIncident.ComMechanism_inControlArea,1,TypeMesg.Inernal) ;
			Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);

			ThereIsneedformoreHospitalcapacity =0;

			Mybody.Action=RespondersActions.RequstandupdatesinOP;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Comunication; 
			ThereisneedforHospital= false;

			//System.out.println("  "+ "AIC: " +Mybody.Id  +"    RequestAdditional  h xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
		}

		//==========================Request for N responders==============================
		// ++++++ 28- +++++++		
		//		else if ( Mybody.Action==RespondersActions.Noaction  && NPramdics > 0) 	 	 	
		//		{						
		//			//which sector 
		//			//Mybody.CurrentMessage = new  ACL_Message( ACLPerformative.RequsteReallocateNResponders, Mybody, Mybody.assignedIncident.,NPramdics, Mybody.CurrentTick,InputFile.ComMechanism_level_TtoO) ;
		//			//NPramdics=0;
		//
		//			Mybody.Action=RespondersActions.RequstandupdatesinOP;
		//			Mybody.BehaviourType1=RespondersBehaviourTypes1.Comunication; //Planexecution;
		//
		//		}
		//==========================Routes cleared  ==============================  he will  sense
		// ++++++ 29- +++++++		
		//else if ( Mybody.Action==RespondersActions.Noaction  && (routeClearedtraffic_List.size()>0 ||  routeClearedwreckage_List.size()>0)  ) 	 	 	
		//{						
		//	Report Report1=new Report();
		//	// addd
		//	Mybody.CurrentMessage = new  ACL_Message( ACLPerformative.Requste, Mybody, Mybody.assignedIncident.ALCcommander,Report1, Mybody.CurrentTick,InputFile.ComMechanism_level_TtoO) ;
		//	routeNEEDClearwreckage_List.clear();

		//	Mybody.Action=RespondersActions.RequstandupdatesinOP;
		//	Mybody.BehaviourType=RespondersBehaviourTypes.Planexecution;
		//	
		//	System.out.println("  "+ "AIC: " +Mybody.Id  +"    update about route  ");
		//}

		//==========================Update about  sector established   ==============================  //from FIC	
		// ++++++ 30- +++++++		
		else if ( Mybody.Action==RespondersActions.Noaction  && startSR==1 &&	TA_Sector_established==1 && Mybody.assignedIncident.Communication_Structure_uesd==Inter_Communication_Structure.Struc1_SilverCommandersInteraction ) 	 	 	
		{													
			//option=98 ;
			//BoradcastsectorAction(ACLPerformative.InfromRCEstablished );
			//Mybody.EndofCurrentAction=   InputFile.Boradcast_duration ;

			option=99 ;
			for ( Activity_ambulance  A : Tactical_Plan ) 		
				if ( A.Activity== Ambulance_ActivityType.SceneResponse && A.informed_TA_sector_established==false )  
				{
					Mybody.CurrentMessage = new  ACL_Message( ACLPerformative.InfromSafetyBriefandSectorEstablished , Mybody, A.BronzCommander,null, Mybody.CurrentTick,Mybody.assignedIncident.ComMechanism_level_StoB,1,TypeMesg.External) ;
					A.informed_TA_sector_established= true;
					Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
				}			
			TA_Sector_established=2 ;

			Mybody.Action=RespondersActions.RequstandupdatesinOP;  
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Comunication; 
			System.out.println("  "+ "AIC: " +Mybody.Id  +"    update SB ");
		}
		//==========================Update about    TA_cordon_established  ==============================  
		// ++++++ 34- +++++++		
		else if ( Mybody.Action==RespondersActions.Noaction &&  this.startLA==1   && ( this.TA_cordon_established==1 && this.LoadingArea_established >=1) && Mybody.assignedIncident.Communication_Structure_uesd==Inter_Communication_Structure.Struc1_SilverCommandersInteraction ) 	 	 //from PIC	
		{						
			option=99 ;
			Mybody.CurrentMessage = new  ACL_Message( ACLPerformative.InfromCordonEstablished ,Mybody , Mybody.assignedIncident.ALCcommander,null, Mybody.CurrentTick,Mybody.assignedIncident.ComMechanism_level_StoB,1,TypeMesg.External) ;		
			Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
			this.TA_cordon_established= 2;

			Mybody.Action=RespondersActions.RequstandupdatesinOP;  
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Comunication; 
			System.out.println("  "+ "AIC: " +Mybody.Id  +"    cordon_established  ");
		}

		//==========================Update about    TA_LA_established  ==============================  
		// ++++++ 34- +++++++		
		else if ( Mybody.Action==RespondersActions.Noaction && startCCS==1  && ( this.LoadingArea_established ==1)  ) 	 	
		{						
			option=99 ;
			Mybody.CurrentMessage = new  ACL_Message( ACLPerformative.InfromLoadingAreaEstablished,Mybody , Mybody.assignedIncident.CCO_ambcommander,null, Mybody.CurrentTick,Mybody.assignedIncident.ComMechanism_level_StoB,1,TypeMesg.Inernal) ;		
			Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
			this.LoadingArea_established= 2;

			Mybody.Action=RespondersActions.RequstandupdatesinOP;  
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Comunication; 
			System.out.println("  "+ "AIC: " +Mybody.Id  +"    inform CCS LoadingArea_establish ");
		}
		//==========================Update about  trappd SC ==============================  
		// ++++++ 34- +++++++		
		else if ( Mybody.Action==RespondersActions.Noaction  && startSR==1  && 	this.Trapped_List.size()>0  && Mybody.assignedIncident.Communication_Structure_uesd==Inter_Communication_Structure.Struc1_SilverCommandersInteraction   ) 	 	 //from FIC	
		{						
			Report Report1=new Report() ;
			Report1.Fire_OPReport_SC( null , this.Trapped_List ) ;
			

			option=99 ;

			if ( Mybody.assignedIncident.AmSCcommander1_ENDSR_INNER!=true   )  //temp solution   Mybody.assignedIncident.AmSCcommander1 !=null
			{	
				Mybody.CurrentMessage = new  ACL_Message( ACLPerformative.InformTrappedcasualty ,Mybody , Mybody.assignedIncident.AmSCcommander1,Report1, Mybody.CurrentTick,Mybody.assignedIncident.ComMechanism_level_StoB,this.Trapped_List.size(),TypeMesg.External) ;		
				Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
			}
			else  //&& this.startCCS==1 
			{
				Mybody.CurrentMessage = new  ACL_Message( ACLPerformative.InformTrappedcasualty ,Mybody , Mybody.assignedIncident.CCO_ambcommander,Report1, Mybody.CurrentTick,Mybody.assignedIncident.ComMechanism_level_StoB,this.Trapped_List.size(),TypeMesg.External) ;	
				Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
			}
			
			this.Trapped_List.clear();

			Mybody.Action=RespondersActions.RequstandupdatesinOP;  
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Comunication ;   
			System.out.println("  "+ "AIC: " +Mybody.Id  +"    Trapped  ");
		}

		//==========================Update about  Nomorecasulty   CCO ==============================  
		// ++++++ 34- +++++++		
		else if ( Mybody.Action==RespondersActions.Noaction  && this.startCCS==1 &&	NomorecasultyinInner==1   && Mybody.assignedIncident.Communication_Structure_uesd==Inter_Communication_Structure.Struc1_SilverCommandersInteraction   ) 	 	 //from FIC	
		{						
			option=99 ;

			Mybody.CurrentMessage = new  ACL_Message( ACLPerformative.InformNomorecasualty_impact ,Mybody , Mybody.assignedIncident.CCO_ambcommander,null, Mybody.CurrentTick,Mybody.assignedIncident.ComMechanism_level_StoB,1,TypeMesg.External) ;		
			Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);

			//Mybody.CurrentMessage = new  ACL_Message( ACLPerformative.InformNomorecasualty_impact ,Mybody , Mybody.assignedIncident.AmSCcommander1,null, Mybody.CurrentTick,Mybody.assignedIncident.ComMechanism_level_StoB) ;		
			//Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);

			NomorecasultyinInner=2;

			Mybody.Action=RespondersActions.RequstandupdatesinOP;  
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Comunication;   
			System.out.println("  "+ "AIC: " +Mybody.Id  +"    updateCCO/SC Nomorecasulty  ");
		}
		//==========================Update about  Nomorecasulty inCCS  ALC ==============================  
		// ++++++ 33- +++++++		
		else if ( Mybody.Action==RespondersActions.Noaction  && this.startLA==1 &&	updateALCNomorecasulty   ) 	 // from CCO	 	
		{						
			option=99 ;
			Mybody.CurrentMessage = new  ACL_Message( ACLPerformative.InformNomorecasualty_CCS ,Mybody , Mybody.assignedIncident.ALCcommander,null, Mybody.CurrentTick,Mybody.assignedIncident.ComMechanism_level_StoB,1,TypeMesg.Inernal) ;		
			Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
			updateALCNomorecasulty=false ;

			Mybody.Action=RespondersActions.RequstandupdatesinOP;  
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Comunication;
			System.out.println("  "+ "AIC: " +Mybody.Id  +"    updateALC Nomorecasulty  ");

		}

		//============================================================ ALL
		// ++++++ 35- +++++++
		else if ( (Mybody.Action==RespondersActions.RequstandupdatesinOP && Mybody.Acknowledged && option==99)  
				|| (   Mybody.Action==RespondersActions.RequstandupdatesinOP && Mybody.EndActionTrigger== RespondersTriggers.EndingAction && option==98 ) )
		{
			Mybody.Action=RespondersActions.Noaction;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.WaitingDuring;
			Mybody.Acknowledged=false;Mybody.Sending=false; 
			Mybody.EndActionTrigger=null;
			option=0 ;
			//System.out.println("  "+ "AIC: " +Mybody.Id  +"   get  Acknowledged after RequstandupdatesinOP ");
		}

		//-------------------------------------------------------------------------------------------- (P7)
		// ++++++ 36- +++++++
		else if ( Mybody.Action==RespondersActions.Noaction && EndofER() &&   EndER_Fire==2 && EndER_Police==2  && this.EndER_Ambulance==2  )  
		{
			Mybody.InterpretedTrigger=RespondersTriggers.DoneEmergencyResponse ;
			System.out.println("  "+ "AIC: " +Mybody.Id  +"    EndofER $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$ ");
			//System.gc() ;
		}
	}

	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	// AIC -  CoordinateEndResponse  
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	private void CommanderBehavior_EndER()
	{
		// ++++++ 1- +++++++
		if ( Mybody.InterpretedTrigger==RespondersTriggers.DoneEmergencyResponse ) 
		{
			Reporting_ENDSituationAssessment();

			Mybody.Action=RespondersActions.InfromReport;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Comunication ; 
			Mybody.InterpretedTrigger=null;
		}
		// ++++++ 2- +++++++
		else if (Mybody.Action==RespondersActions.InfromReport && Mybody. Acknowledged  )
		{
			// Send message to its commanders
			BoradcastEndofERAction();

			Mybody.Action=RespondersActions.BoradcastEndER ;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Comunication;  
			Mybody.EndofCurrentAction=   InputFile.Boradcast_duration ;
			Mybody.Acknowledged=false;Mybody.Sending=false; 	
		}
		// ++++++ 3- +++++++
		else if (Mybody.Action==RespondersActions.BoradcastEndER && Mybody.EndActionTrigger== RespondersTriggers.EndingAction  )
		{
			Mybody.InterpretedTrigger=RespondersTriggers.ENDER;

			Mybody.Action=RespondersActions.Noaction ;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.WaitingEnd;
			Mybody.Sending=false; 
			Mybody.EndActionTrigger= null;
			System.out.println( "  "+Mybody.Id  +" GO back to Vehicle  " +Mybody.Role );

			//for ( Responder_Ambulance RA : MyResponder_Ambulance )
			//	System.out.println(RA.Id + "-----" + RA.Action );

		}
	}

}//end class























////------------------------------------- Sector Planning----------------------------------------
//		if ( Mybody.assignedIncident.TaskMechanism_IN==TaskAllocationMechanism.NoEnvironmentsectorization ) //one sector
//		{
//								
//			System.out.println(Mybody.Id + " AssessSituation ...total   " +Mybody.CasualtySeen_list.size() );
//		}
//		else
//		{
//			//=================1=============================== Organized_exploration
//			if ( Mybody.assignedIncident.Survey_scene_Mechanism_IN== SurveyScene_SiteExploration_Mechanism.Organized_exploration )
//			{
//				// Now, he know how many casualty in each sector	
//				for ( Sector  S : Mybody.assignedIncident.Sectorlist )
//				{
//					List<Casualty> nearObjects_Casualty = (List<Casualty>) S.GetObjectsWithinSector(Mybody.CasualtySeen_list );
//					S.CasualityinThisSector=nearObjects_Casualty.size();
//					System.out.println(Mybody.Id + " planing...  "+ S.CasualityinThisSector );
//				}
//
//
//				for ( int i=0;i<  Mybody.assignedIncident.NOSector  ;i++)   //  high priority 0, 1, then 2 
//				{ 
//
//					//ProirityCreation
//					double Maxcasulity=-1 ;      Sector  NominatedS=null;  
//					for ( Sector  S : Mybody.assignedIncident.Sectorlist )
//					{
//						if ( S.CasualityinThisSector >    Maxcasulity  && S.ProirityCreation ==-1 )
//						{Maxcasulity= S.CasualityinThisSector  ; NominatedS= S; }
//					}
//					NominatedS.ProirityCreation= i;
//				}
//
//				ConsiderSectorNeedinAssignedResponders= true ;
//
//
//				for ( int i=0;i<  Mybody.assignedIncident.NOSector  ;i++)   //  high priority 0, 1, then 2 
//					System.out.println(Mybody.Id +" " + i +  " sector ...total   " +Mybody.CasualtySeen_list.size() +" CasualityinThisSector :    "  + Mybody.assignedIncident.Sectorlist.get(i).CasualityinThisSector   +"  ProirityCreation :   "  + Mybody.assignedIncident.Sectorlist.get(i).ProirityCreation  +" NeedResponders:    "  + Mybody.assignedIncident.Sectorlist.get(i).NeedResponders  +"  Meet :   "  + Mybody.assignedIncident.Sectorlist.get(i).Meet  +"    CurrentResponderinSector : "  + Mybody.assignedIncident.Sectorlist.get(i).CurrentPramdeicResponderinSector );
//
//			}
//			//=================2=============================== Randomly_exploration
//			else if ( Mybody.assignedIncident.Survey_scene_Mechanism_IN== SurveyScene_SiteExploration_Mechanism.Randomly_exploration )
//			{
//				ConsiderSectorNeedinAssignedResponders= false ; // do no things
//			}



//case  InformTriageReport  :
//	Mybody.CommTrigger= RespondersTriggers.GetresultofTriageReport;
//	CurrentBZC = (Responder_Ambulance)currentmsg.sender;
//	TriageResultReport_CaList =(ArrayList<Casualty>) currentmsg.content;
//
//	break;
//	
//case  InformEndTriageReport :
//	Mybody.CommTrigger= RespondersTriggers.GetOPReport;			
//	CurrentBZC = (Responder_Ambulance)currentmsg.sender;
//	if ( Mybody.assignedIncident.CasualtyReport_Mechanism_IN==CasualtyReportandTrackingMechanism.Paper )
//		TriageResultReport_CaList =(ArrayList<Casualty>) currentmsg.content;
//	EndSceneResponse=false ;				
//	break;
//	
//	//==========================Result of Triage==============================
//			// ++++++ 5- +++++++
//			else if (Mybody.Action==RespondersActions.Noaction && ( Mybody.CommTrigger== RespondersTriggers.GetresultofTriageReport || Mybody.assignedIncident.NewRecordcasualtyadded_ISSystem(Mybody.Role,null,0)>0 )   )		  										
//			{	 	
//
//				Mybody.Action=RespondersActions.GetResultTriage ;
//				Mybody.BehaviourType=RespondersBehaviourTypes.Gatheringinfo;
//
//				if ( Mybody.CommTrigger== RespondersTriggers.GetresultofTriageReport  )		
//					Mybody.EndofCurrentAction= (InputFile.GetInfromation_FtoForR_Duration * TriageResultReport_CaList.size() ); 
//				else 
//				{
//					TriageResultReport_RecList=Mybody.assignedIncident.GetTriageResults_ISSystem(Mybody.Role);
//					Mybody.EndofCurrentAction= (InputFile.GetInfromation_Electronic_Duration * TriageResultReport_RecList.size()) ;			
//				}
//
//			}
//			// ++++++ 6- +++++++
//			else if ( Mybody.Action==RespondersActions.GetResultTriage && Mybody.EndActionTrigger== RespondersTriggers.EndingAction 	) 			 	
//			{
//				Casualty ca;
//
//
//				if ( Mybody.CommTrigger== RespondersTriggers.GetresultofTriageReport  )
//				{
//					CurrentBZC.Acknowledg(Mybody);	
//					//UpdatePlan
//					for(Casualty    Ca:TriageResultReport_CaList) {
//						Task_ambulance Newtask = new Task_ambulance( Ca, Ambulance_TaskType.TransferCasualtytoHospital ,CurrentBZC ,null  ); 
//
//						Casualty_Plan.add(Newtask);	
//					}
//					//System.out.println(Mybody.Id + " get from "+CurrentSC.Id );
//					Mybody.CommTrigger=null;
//				}
//				else 
//				{	
//					//UpdatePlan
//					for(ISRecord    Rec:TriageResultReport_RecList) {
//						Task_ambulance Newtask = new Task_ambulance( Rec.CasualtyinRec, Ambulance_TaskType.TransferCasualtytoHospital ,Rec.AssignedSC ,null); 
//						Casualty_Plan.add(Newtask);	
//					}
//				}
//
//				//ThereIsneedformoreHospitalcapacity=ThereIsneedformoreHospitalcapacity +
//				if ( !Iamrequest )EstimatedNeedforHospital();  // ???????????????????????????????????????????
//
//				Mybody.Action=RespondersActions.UpdatePlan;
//				Mybody.BehaviourType=RespondersBehaviourTypes.Planformulation;
//				Mybody.EndofCurrentAction= InputFile.UdatePlan_duration ; 
//				Mybody.EndActionTrigger=null; 
//
//				//System.out.println(Mybody.Id + " GetresultofTriageReport " );
//			}

