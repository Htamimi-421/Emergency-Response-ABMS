package A_Roles_Fire;

import java.util.ArrayList;
import java.util.List;
import A_Agents.Casualty;
import A_Agents.Responder;
import A_Agents.Responder_Fire;
import A_Environment.PointDestination;
import A_Environment.RoadLink;
import A_Environment.Sector;
import B_Classes.Activity_Fire;
import B_Classes.Activity_ambulance;
import B_Classes.Task_Fire;
import B_Communication.ACL_Message;
import B_Communication.Command;
import B_Communication.ISRecord;
import B_Communication.Report;
import B_Communication.SliverMeetingRecord;
import C_SimulationInput.InputFile;
import D_Ontology.Ontology.ACLPerformative;
import D_Ontology.Ontology.Ambulance_ActivityType;
import D_Ontology.Ontology.Ambulance_ResponderRole;
import D_Ontology.Ontology.CommunicationMechanism;
import D_Ontology.Ontology.Communication_Time;
import D_Ontology.Ontology.Fire_ActivityType;
import D_Ontology.Ontology.Fire_ResponderRole;
import D_Ontology.Ontology.Fire_TaskType;
import D_Ontology.Ontology.GeneralTaskStatus;
import D_Ontology.Ontology.Inter_Communication_Structure;
import D_Ontology.Ontology.RandomWalking_StrategyType;
import D_Ontology.Ontology.ReportSummery;
import D_Ontology.Ontology.RespondersActions;
import D_Ontology.Ontology.RespondersBehaviourTypes1;
import D_Ontology.Ontology.RespondersBehaviourTypes2;
import D_Ontology.Ontology.RespondersBehaviourTypes3;
import D_Ontology.Ontology.RespondersTriggers;
import D_Ontology.Ontology.SurveyScene_SiteExploration_Mechanism;
import D_Ontology.Ontology.TaskAllocationMechanism;
import D_Ontology.Ontology.TypeMesg;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.engine.schedule.ScheduleParameters;

public class Fire_Commander_FIC {

	Responder_Fire Mybody;
	boolean TempFIC=true;
	//----------------------------------------------------
	Command    Allocation_Command ,  MajorActivity_Command;
	Task_Fire  AllocatedTaskforcasualtyevacuation;

	Responder_Fire NewResponder=null, BackResponder=null ,CurrentResponder=null ;
	Responder_Fire  CurrentBZC=null , TempFCO=null ;

	Report CurrentReport=null;
	ArrayList<RoadLink> CurrentRouteListbySender=new ArrayList<RoadLink>();	
	ArrayList<Casualty> CurrentDead_innerListbySender =new ArrayList<Casualty>();
	ArrayList<Casualty> CurrentTrapped_ListbySender =new ArrayList<Casualty>();

	//----------------------------------------------------
	boolean ConsiderSectorNeedinAssignedResponders =false; //sector	

	boolean InstrcuteTempFCOtoleave=false ;
	boolean EndSearchcasulties=false  , EndClearRouteWreckage=false	;	
	boolean IsNewResponder ;
	int NFireFighter=0 ;
	int option=0 ;
	int NEEDfromAESSMENT=0;
	int AssessSituation_casualtycount=0;
	boolean NeedFirstMeeting=true  , DoneFirstMeeting=false , DoneLastMeeting=false; ;
	boolean SurveyScene ;
	boolean respondrsBack=false;
	
	int startWRK=-1;
	int startSR=-1;
	//----------------------------------------------------
	boolean GeolocationofTA_sectors ;
	//  The meaning of attribute content....  -1:No update   , 0: Get  , 1: realize or done  2: send to workers or silver commander
	int Sector_established=-1 ;	 
	int  TA_cordon_established=-1 , TA_controlarea_established=-1 ,TA_CCS_established=-1 ,TA_RC_established=-1 ;

	//----------------------------------------------------
	ArrayList<ISRecord > DeadResultReport_RecList=new ArrayList<ISRecord >();
	ArrayList<Casualty> DeadResultReport_CaList =new ArrayList<Casualty>();

	//----------------------------------------------------
	List<Activity_Fire> Tactical_Plan = new ArrayList<Activity_Fire>();	
	public List<Responder_Fire> MyResponder_Fire = new ArrayList<Responder_Fire>(); // all Responder of Fire
	public List<Responder_Fire> UnoccupiedResponders = new ArrayList<Responder_Fire>(); //New Responders
	public List<Responder_Fire> UnoccupiedDrivers = new ArrayList<Responder_Fire>(); //New Driver

	List<Sector> SectorDoneSearch = new ArrayList<Sector>();
	//----------------------------------------------------
	double Time_last_updated=0;	
	SliverMeetingRecord Current_SliverMeeting ;
	public List<SliverMeetingRecord> SliverMeetings= new ArrayList<SliverMeetingRecord>(); 

	//input silver meeting	
	int NomorecasultyinInner=-1 ;
	boolean  FirstArrivalAssessment ;
	ArrayList<RoadLink> routeClearedwreckage_List =new ArrayList<RoadLink>();   // or CEAP
	ArrayList<Casualty> Dead_List_inner =new ArrayList<Casualty>();
	ArrayList<Casualty> Trapped_List =new ArrayList<Casualty>();
	//output
	int NomorecasultyinLA=-1 ;
	int EndER_Ambulance=-1 , EndER_Fire=-1 , EndER_Police=-1; 
	ArrayList<RoadLink> routeNEEDClearwreckage_List =new ArrayList<RoadLink>(); 

	//##############################################################################################################################################################	
	public Fire_Commander_FIC ( Responder_Fire _Mybody) 
	{
		Mybody=_Mybody;
		Mybody.ColorCode= 5;
		Mybody.assignedIncident.FICcommander=Mybody;
		Mybody.PrvRoleinprint2 =Mybody.Role;
	}

	public void Commander_FIC_HandoverAction ( Responder_Fire TempFIC)  //called by 
	{
		this.Tactical_Plan =TempFIC.CurrentCalssRole3.Tactical_Plan ;
		this.MyResponder_Fire=	TempFIC.CurrentCalssRole3.MyResponder_Fire;
		this.UnoccupiedResponders =TempFIC.CurrentCalssRole3.UnoccupiedResponders;
		//this.Casualty_Plan = TempAIC.CurrentCalssRole3.Casualty_Plan; 


		int x= TempFIC.Message_inbox.size()-TempFIC.Lastmessagereaded ;  

		System.out.println(Mybody.Id + " number of message transferred" + x  );

		while (TempFIC.Lastmessagereaded < TempFIC.Message_inbox.size()  ) //Right now there is now system to check if there is unread message
		{
			Mybody.Message_inbox.add(TempFIC.Message_inbox.get(TempFIC.Message_inbox.size()- x)) ;   
			x--;
		}

	}

	//##############################################################################################################################################################
	public void CommanderFIC_InterpretationMessage()
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
			if ( Mybody.CurrentSender instanceof Responder_Fire && TempFIC && Mybody.CurrentCommandRequest.commandType1 == null  )  
			{	
				Mybody.CommTrigger= RespondersTriggers.GetinstructiontoHandover  ;
			}
			//++++++++++++++++++++++++++++++++++++++
			break;
		case InformNewResponderArrival:	
			NewResponder= (Responder_Fire) Mybody.CurrentSender;			
			Mybody.CommTrigger= RespondersTriggers.GetNewResponderarrived;
			//System.out.println(Mybody.Id + " gettttttt " );
			break;
		case InformComebackResponderArrival:	
			BackResponder= (Responder_Fire) Mybody.CurrentSender;			
			Mybody.CommTrigger= RespondersTriggers.GetbackResponderarrived	;		
			break;	
			//------------------------------- ``
		case InformlocationArrival: //from bronze commander
			Mybody.CommTrigger= RespondersTriggers.GetBronzeCommanderstart ;  
			CurrentBZC=(Responder_Fire) Mybody.CurrentSender;	
			break;
		case  InfromSafetyBriefandSectorEstablished : 
			Mybody.CommTrigger= RespondersTriggers.ResultofSetupTA  ;
			CurrentBZC = (Responder_Fire)currentmsg.sender;
			Sector_established= 0;
			break;
			//------------------------------- 			
		case  InformClearOfRouteReport  : 
			Mybody.CommTrigger= RespondersTriggers.GetOPReport ;			
			CurrentBZC = (Responder_Fire)currentmsg.sender;
			CurrentReport=((Report) currentmsg.content);
			CurrentRouteListbySender =CurrentReport.routeClearedwreckage_List ;
			//System.out.println(Mybody.Id + "  RT " );
			break;	

		case  InformEndClearRouteWreckage :
			Mybody.CommTrigger= RespondersTriggers.GetOPReport;
			CurrentBZC = (Responder_Fire)currentmsg.sender;
			EndClearRouteWreckage=true;	
			break;
			//------------------------------- 	
		case InformSearchandRescueCasualtyReport :
			Mybody.CommTrigger= RespondersTriggers.GetOPReport;
			CurrentBZC = (Responder_Fire)currentmsg.sender;
			CurrentReport=((Report) currentmsg.content);
			CurrentDead_innerListbySender=CurrentReport.Dead_List_inner ;	
			break;
		case InformEndSearchandRescue_Nomorecasualty :
			Mybody.CommTrigger= RespondersTriggers.GetOPReport;
			CurrentBZC = (Responder_Fire)currentmsg.sender;
			//CurrentReport=((Report) currentmsg.content);
			//CurrentDead_innerListbySender=CurrentReport.Dead_List_inner ;		
			EndSearchcasulties=true ;
			break;	
			//------------------------------- 	
		case InformTrappedcasualty :
			Mybody.CommTrigger= RespondersTriggers.GetOPReport;
			CurrentBZC = (Responder_Fire)currentmsg.sender;
			CurrentReport=((Report) currentmsg.content);
			CurrentTrapped_ListbySender=CurrentReport.trapped_List;	
			break;
			//------------------------------- 
		case   InformFirestSituationAssessmentReport:
			Mybody.CommTrigger= RespondersTriggers.GetReport;
			CurrentReport=((Report) currentmsg.content);
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
	// Create Plan-(list of OP Activity) for first time
	public void Implmenting_plan() {


		// Communication  ,SearchandRescueCasualty ,ClearRoute 
		// ------------------------------------- Create Activity plan------------------------------------------ 
		//1-
		Activity_Fire A1 = new Activity_Fire(Fire_ActivityType.Communication ,Fire_ResponderRole.FireCommunicationsOfficer,true    ); // SingleActivity true
		Tactical_Plan.add( A1);
		if (TempFIC)	{ A1.BronzCommander=(Responder_Fire) Mybody.Myvehicle.AssignedDriver;A1.Tempcommnader=true ; A1.ActivityStart();	}
		//---------------------------------------------		
		//2- sectorization 
		if ( Mybody.assignedIncident.TaskMechanism_IN==TaskAllocationMechanism.Environmentsectorization )
		{

			for ( int i=0; i<Mybody.assignedIncident.NOSector  ;i++)
			{ 			
				Activity_Fire  A3= new Activity_Fire(Fire_ActivityType.SearchandRescueCasualty,Fire_ResponderRole.FireSectorCommander,false ) ; // SingleActivity false
				Tactical_Plan.add( A3);

				if ( ConsiderSectorNeedinAssignedResponders == false)
					A3.inSector=  Mybody.assignedIncident.Sectorlist2.get(i);   // create random 
				else
				{
					Sector  NominatedS=null;  
					for ( Sector  S : Mybody.assignedIncident.Sectorlist2 ) // create priority infromed by  survay sceeen
						if ( S.ProirityCreation== i  )
						{ NominatedS= S; break;}
					A3.inSector= NominatedS; 
				}
			}
		}
		else if ( Mybody.assignedIncident.TaskMechanism_IN==TaskAllocationMechanism.NoEnvironmentsectorization ) // one sector
		{
			Activity_Fire  A3= new Activity_Fire(Fire_ActivityType.SearchandRescueCasualty ,Fire_ResponderRole.FireSectorCommander,false ); 
			Tactical_Plan.add( A3);
			A3.inSector=  Mybody.assignedIncident.Sectorlist2.get(0); 
		}

		//---------------------------------------------
		//3-
		Activity_Fire  A4 = new Activity_Fire(Fire_ActivityType.ClearRoute,Fire_ResponderRole.FireSectorCommander,false )  ; 
		Tactical_Plan.add( A4);
		A4.inCordon=  Mybody.assignedIncident.Cordon_outer ;

	};

	//***************************************************************************************************	
	private Activity_Fire GetActivity1(Fire_ActivityType xx)
	{
		Activity_Fire result=null;
		for ( Activity_Fire A :Tactical_Plan) 
			if ( A.Activity == xx)
				result= A ;		
		return result ;		 
	}

	//***************************************************************************************************	
	// Resource Allocation for Activity   output command of role and task for  bronze commander or responders
	public Command AssigneRoleandActivitytoResponder(  Responder_Fire _Responder  , Fire_ActivityType SpecificActivity    ) {

		boolean implmnetActivity=false;
		Command CMD1 =new Command();
		MajorActivity_Command=null;

		//-------------------------------------- Implementation  allocate BZC
		for ( Activity_Fire A :Tactical_Plan) 
		{		
			if ( A.ActivityStatus==GeneralTaskStatus.Inprogress &&  A.Activity==Fire_ActivityType.Communication && A.Tempcommnader==true   ) 
			{				
				TempFCO= A.BronzCommander;
				InstrcuteTempFCOtoleave=true;				
				A.Tempcommnader=false;
				//System.out.println(Mybody.Id + "TempFCO "+TempFCO.Id ); 

				A.ResourseAllocation_commander(_Responder );				
				CMD1.FireCommand( "0" , A.BronzRole  , null,null  );				
				implmnetActivity=true;break;
			}
			else if ( A.ActivityStatus==GeneralTaskStatus.Waiting && A.BronzCommander==null && A.Activity==Fire_ActivityType.SearchandRescueCasualty ) // Commander for setup task
			{
				A.ResourseAllocation_commander(_Responder );
				CMD1.FireCommand( "0" , A.BronzRole  ,  A.inSector ,null );
				A.ActivityStart(); // if you consider location arrival you have to delete this
				implmnetActivity=true;break;				
			}	
			else if (   A.ActivityStatus==GeneralTaskStatus.Waiting && A.BronzCommander==null &&  A.Activity==Fire_ActivityType.ClearRoute  &&  NomorecasultyinInner>=1 )	
			{									 
				A.ResourseAllocation_commander(_Responder );
				CMD1.FireCommand( "0" , A.BronzRole  , null , A.inCordon  );
				A.ActivityStart();	
				//System.out.println(Mybody.Id + "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxClearRoute   "+ _Responder.Id ); 
				_Responder.Role = Fire_ResponderRole.None;  // temp solu
				implmnetActivity=true; break;

			}
		}

		//------------------------------------- During response how to adopt between  equal responders between C information and secure cordon

		if ( !implmnetActivity  &&  SpecificActivity ==null  )   //no awareness about the activity  need
		{
			//			int MinimumResponders=99;
			Activity_Fire  selectedA=null;
			//
			//			for ( Activity_Fire  A : Tactical_Plan ) 		
			//				if ( ( A.ActivityStatus==GeneralTaskStatus.Inprogress &&  A.Activity==Fire_ActivityType.SearchandRescueCasualty  && ! respondrsBack )|| ( A.ActivityStatus==GeneralTaskStatus.Inprogress && A.Activity==Fire_ActivityType.ClearRoute )  ) 
			//				{
			//					if (A.AllocatedResponderforThisActivity.size()< MinimumResponders   )
			//					{selectedA=A;   MinimumResponders=A.AllocatedResponderforThisActivity.size();}	
			//
			//					//System.out.println("                                       "  + Mybody.Id + A.Activity + "  "+ A.AllocatedResponderforThisActivity.size() ); 
			//				}

			if( NomorecasultyinInner==-1 )
				selectedA= GetActivity1(Fire_ActivityType.SearchandRescueCasualty );

			else if ( NomorecasultyinInner>=1 )
				selectedA=GetActivity1(Fire_ActivityType.ClearRoute );



			if (selectedA!=null )
			{
				selectedA.ResourseAllocation_Responders(_Responder);	

				if (  _Responder.Role==Fire_ResponderRole.None) 
					CMD1.FireCommand( "0" , Fire_ResponderRole.FireFighter,null, null);
				else
				{
					; //// for come back responders
				}


				if (selectedA.Activity ==Fire_ActivityType.SearchandRescueCasualty)
				{
					MajorActivity_Command=new Command();
					MajorActivity_Command.FireCommand( "0" , Fire_ActivityType.SearchandRescueCasualty,  selectedA.inSector ,null ) ;
				}
				else
				{
					MajorActivity_Command=new Command();
					MajorActivity_Command.FireCommand( "0" , Fire_ActivityType.ClearRoute ,null ,selectedA.inCordon) ;				
				}
			}
			else
			{
				// need work
				System.out.println( Mybody.Id +" " +   " Fire errroooorrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrr  new responders notask in assigned task  "); // if all of them  has max
			}

		}
		//-------------------------------------- 
		else if (!implmnetActivity  && SpecificActivity !=null)
		{

		}



		//-------------------------------------- During response   randomly or Least sector or FIFO more severity  // how to adopt

		if (!implmnetActivity  &&  ! ConsiderSectorNeedinAssignedResponders && SpecificActivity !=null )   // 1- Least sector number of responder ........no awareness about the sector need
		{

			int MinimumResponders=99;
			Activity_Fire  selectedA=null;

			for ( Activity_Fire  A : Tactical_Plan ) 		
				if ( A.ActivityStatus==GeneralTaskStatus.Inprogress &&  ! A.SingleActivity )
				{
					if (A.AllocatedResponderforThisActivity.size()<MinimumResponders)
					{selectedA=A;MinimumResponders=A.AllocatedResponderforThisActivity.size();}
				}

			if (selectedA!=null )
			{
				selectedA.ResourseAllocation_Responders(_Responder);	
				CMD1.FireCommand( "0" , Fire_ResponderRole.FireFighter  , selectedA.inSector ,null  );  selectedA.inSector.CurrentFireFighterResponderinSector ++;
			}
			else
			{
				System.out.println( Mybody.Id +" " +   " errroooorrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrr  new responders notask in assigned task  ");
			}

			//-------------------------------------
			//for ( int i=0;i<  InputFile.NOSector  ;i++)   //  high priority 0, 1, then 2 
			//System.out.println("Noconsider----"+ Mybody.Id +" " + i +  " sector ...total   " +Mybody.CasualtySeen_list.size() +" CasualityinThisSector :    "  + Mybody.assignedIncident.Sectorlist.get(i).CasualityinThisSector   +"  ProirityCreation :   "  + Mybody.assignedIncident.Sectorlist.get(i).ProirityCreation  +" NeedResponders:    "  + Mybody.assignedIncident.Sectorlist.get(i).NeedResponders  +"  Meet :   "  + Mybody.assignedIncident.Sectorlist.get(i).Meet  +"    CurrentResponderinSector : "  + Mybody.assignedIncident.Sectorlist.get(i).CurrentResponderinSector  );

		}
		else if (!implmnetActivity  && ConsiderSectorNeedinAssignedResponders && SpecificActivity !=null )   //2- send to specific sector
		{						
			int NumcasultyforParamdic=0;  
			int TotalEstimatedPramdic=0;

			for (Responder_Fire Res : MyResponder_Fire)
			{
				if ( Res.Role==Fire_ResponderRole.None ||Res.Role==Fire_ResponderRole.FireFighter)
					TotalEstimatedPramdic++;
			}

			NumcasultyforParamdic = ( AssessSituation_casualtycount / TotalEstimatedPramdic ) ; // minus sector commander 

			//1- who more need sector
			double LeastMeet=999;   Sector  NominatedS=null ;
			for ( Sector  S : Mybody.assignedIncident.Sectorlist2 )
			{

				S.NeedResponders =Math.floor (S.CasualityinThisSector/NumcasultyforParamdic) ;			
				S.Meet= S.CurrentFireFighterResponderinSector - S.NeedResponders  ;
				if ( S.Meet <    LeastMeet  )
				{LeastMeet= S.Meet ; NominatedS= S; }
				else if ( S.Meet == LeastMeet )
				{
					if ( S.CurrentFireFighterResponderinSector < NominatedS.CurrentFireFighterResponderinSector  )
					{ NominatedS= S; }
				}
			}


			Activity_Fire  selectedA=null;
			for ( Activity_Fire  A : Tactical_Plan ) 		
				if ( A.ActivityStatus==GeneralTaskStatus.Inprogress &&  A.Activity== Fire_ActivityType.SearchandRescueCasualty  )
				{
					if (A.inSector == NominatedS )
					{selectedA=A;}
				}

			selectedA.ResourseAllocation_Responders(_Responder);	
			//CMD1.FireceCommand( "0" , Fire_ResponderRole.FireFighter  , selectedA.inSector  );  selectedA.inSector.CurrentFireFighterResponderinSector ++;

			//-------------------------------------
			//for ( int i=0;i<  Mybody.assignedIncident.NOSector  ;i++)   //  high priority 0, 1, then 2 
			//	System.out.println("consider----"+ Mybody.Id +" " + i +  " sector ...total   " +Mybody.CasualtySeen_list.size() + " NumcasultyforParamdic : "+NumcasultyforParamdic + " CasualityinThisSector :    "  + Mybody.assignedIncident.Sectorlist.get(i).CasualityinThisSector   +"  ProirityCreation :   "  + Mybody.assignedIncident.Sectorlist.get(i).ProirityCreation  +" NeedResponders:    "  + Mybody.assignedIncident.Sectorlist.get(i).NeedResponders  +"  Meet :   "  + Mybody.assignedIncident.Sectorlist.get(i).Meet  +"    CurrentResponderinSector : "  + Mybody.assignedIncident.Sectorlist.get(i).CurrentResponderinSector  );

		}

		//-------------------------------------- 

		if ( this.UnoccupiedResponders.size()==0)
		{
			System.out.println("=============================Fire===============================") ; 

			for ( Activity_Fire  A : Tactical_Plan ) 		
			{
				System.out.println(A.Activity  + "       "  + A.AllocatedResponderforThisActivity.size()) ; 
			}
			System.out.println("============================================================") ; 
		}
		return CMD1;
	}

	//##############################################################################################################################################################	
	//													Reporting
	//##############################################################################################################################################################	
	//First report
	public void Reporting_FRSituationAssessment()
	{
		Report Report1 =new Report();
		Report1.Fire_FRSituationAssessment( Mybody.assignedIncident ,ReportSummery.AdditionalResourceRequest , NEEDfromAESSMENT  );

		// send message
		Mybody.CurrentMessage  = new  ACL_Message( ACLPerformative.InformFirestSituationAssessmentReport ,Mybody ,Mybody.assignedIncident.FCOcommander ,Report1, Mybody.CurrentTick ,Mybody.assignedIncident.ComMechanism_inControlArea ,1,TypeMesg.Inernal) ;
		Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
		//System.out.println( Mybody.assignedIncident.ID +" Fire   AdditionalResourceRequest===============================================1========================================================================="  + NEEDfromAESSMENT );
	}

	//***************************************************************************************************	
	//update report 
	public void Reporting_URSituationAssessmentx()
	{

		Report Report1 =new Report();
		Report1.Fire_URSituationAssessment( Mybody.assignedIncident ,ReportSummery.AdditionalResourceRequest , 0 );

		// send message
		//Mybody.CurrentMessage  = new  ACL_Message( ACLPerformative.InformSituationReport ,Mybody ,Mybody.assignedIncident.FCOcommander ,Report1, Mybody.CurrentTick ,Mybody.assignedIncident.ComMechanism_inControlArea,1 ) ;
		Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
	}
	//***************************************************************************************************	
	//End Report
	public void Reporting_ENDSituationAssessment()
	{

		// Send Final message to its  ACO  nothings
		Report Report1 =new Report();
		Report1.Fire_EndReport(Mybody.assignedIncident ,ReportSummery.EndER_Fire );

		Mybody.CurrentMessage = new  ACL_Message( ACLPerformative.InformERendReport , Mybody,Mybody.assignedIncident.FCOcommander ,Report1, Mybody.CurrentTick , Mybody.assignedIncident.ComMechanism_inControlArea,1 ,TypeMesg.Inernal) ;
		Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
	}

	//***************************************************************************************************	
	public void BoradcastEndofERAction()
	{

		List<Responder_Fire> Listofreceiver = new ArrayList<Responder_Fire>();

		for ( Activity_Fire  A : Tactical_Plan ) 
		{ if ( A.BronzCommander !=Mybody)
		{	Listofreceiver.add(A.BronzCommander); A.ActivityStatus= GeneralTaskStatus.Done ;} }  //System.out.println( A.BronzCommander.Id +" xxxxxxxxxListofreceiverxxxxxxx ")

		Mybody.CurrentMessage  = new  ACL_Message( ACLPerformative.InformERend , Mybody,Listofreceiver ,null, Mybody.CurrentTick,CommunicationMechanism.RadioSystem_BoradCast ,1,TypeMesg.Inernal) ;
		Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
	}

	public void BoradcastEndofERAction2()
	{
		Mybody.CurrentMessage  = new  ACL_Message( ACLPerformative.InformERend , Mybody,this.UnoccupiedDrivers,null, Mybody.CurrentTick,CommunicationMechanism.RadioSystem_BoradCast,1 ,TypeMesg.Inernal) ;
		Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
	}

	public void BoradcastsectorAction(ACLPerformative  xx)
	{

		List<Responder_Fire> Listofreceiver = new ArrayList<Responder_Fire>();

		for ( Activity_Fire  A : Tactical_Plan ) 
		{ 
			if ( A.BronzCommander !=Mybody &&  A.ActivityStatus!= GeneralTaskStatus.Done  &&  A.Activity== Fire_ActivityType.SearchandRescueCasualty  )
			{	Listofreceiver.add(A.BronzCommander); } 
		}  

		Mybody.CurrentMessage  = new  ACL_Message( xx , Mybody,Listofreceiver ,null, Mybody.CurrentTick,CommunicationMechanism.RadioSystem_BoradCast,1 ,TypeMesg.Inernal) ;
		Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
	}

	//***************************************************************************************************	
	//SilverMeeting
	public void BoradcastSilverMeetingAction(SliverMeetingRecord SR)
	{
		List<Responder> Listofreceiver = new ArrayList<Responder>();
		Listofreceiver.add(Mybody.assignedIncident.AICcommander ) ;
		Listofreceiver.add(Mybody.assignedIncident.PICcommander ) ;

		// locationSilverMeeting need to think 
		Mybody.CurrentMessage  = new  ACL_Message( ACLPerformative.CallForSilverMeeting, Mybody,Listofreceiver ,SR, Mybody.CurrentTick,CommunicationMechanism.FF_BoradCast,1,TypeMesg.External ) ; // if there is sharing  chanle
		Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);

	}

	//##############################################################################################################################################################	
	//													Interpreted Triggers 
	//##############################################################################################################################################################	
	public void AssessStatus(  )
	{		
		int MaximumWaiting=-1;
		Activity_Fire  selectedA=null;

		for ( Activity_Fire  A : Tactical_Plan ) 		
			if ( A.Activity== Fire_ActivityType.SearchandRescueCasualty && A.ActivityStatus==GeneralTaskStatus.Inprogress  )
			{
				int waiting= A.lastReport().TotalCasualties_waiting;
				if (waiting >MaximumWaiting  )
				{selectedA=A ; MaximumWaiting=waiting;}
			}

		//based on report
	}

	//	//----------------------------------------------------------------------------------------------------
	//	public Activity_Fire IsAllSectorInfromed_CCS(  )
	//	{		
	//		Activity_Fire Asector=null ;
	//
	//		for ( Activity_Fire  A : Tactical_Plan ) 		
	//			if ( A.Activity== Fire_ActivityType.SearchandRescueCasualty && A.ActivityStatus!=GeneralTaskStatus.Done   && A.informed_TA_CCS_established==false )  //,   informed_TA_RC_established=false ; 
	//			{
	//				Asector=A;
	//
	//				break;
	//			}
	//
	//		return Asector;
	//
	//	}
	//
	//	//----------------------------------------------------------------------------------------------------
	//	public Activity_Fire IsAllSectorInfromed_RC(  )
	//	{		
	//		Activity_Fire Asector=null ;
	//
	//		for ( Activity_Fire  A : Tactical_Plan ) 		
	//			if ( A.Activity== Fire_ActivityType.SearchandRescueCasualty && A.ActivityStatus!=GeneralTaskStatus.Done   && A.informed_TA_RC_established==false )  //,   informed_TA_RC_established=false ; 
	//			{
	//				Asector=A;				
	//				break;
	//			}
	//
	//		return Asector ;
	//
	//	}

	//----------------------------------------------------------------------------------------------------
	public boolean IsAllSectorDone(  )
	{		
		boolean result=true;

		for ( Activity_Fire  A : Tactical_Plan ) 		
			if ( A.Activity== Fire_ActivityType.SearchandRescueCasualty && A.ActivityStatus!=GeneralTaskStatus.Done )  
			{
				result=false;				
				break;
			}

		return result ;

	}
	//----------------------------------------------------------------------------------------------------
	public boolean EndofER()
	{
		boolean done=true;

		if (Tactical_Plan.size()== 0 )
		{done=false;}

		for ( Activity_Fire  A : Tactical_Plan ) 
			if (( A.Activity   ==Fire_ActivityType.SearchandRescueCasualty ||  A.Activity   == Fire_ActivityType.ClearRoute ) && A.ActivityStatus!=GeneralTaskStatus.Done  )  //exept comunication and hospital 
			{done=false; break;}

		//if ( done==true ) System.out.println("f");
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

		if ( Mybody.assignedIncident.Survey_scene_Mechanism_IN== SurveyScene_SiteExploration_Mechanism.Organized_exploration )
		{
			NEEDfromAESSMENT=0; // need 

		}
		else if ( Mybody.assignedIncident.Survey_scene_Mechanism_IN== SurveyScene_SiteExploration_Mechanism.Randomly_exploration )
		{
			NEEDfromAESSMENT=0 ; //Do nothings
		}	

		return count;
	}

	//----------------------------------------------------------------------------------------------------
	public boolean  ISthereupdateTosend( )
	{
		int Thereisupdate =0;

		if (Sector_established==1  )
			Thereisupdate++;
		//if ( routeClearedwreckage_List .size() >0 )
		//{ Current_SliverMeeting.FIC_Addtomeeting_Route( routeClearedwreckage_List  );routeClearedwreckage_List .clear(); }
		//3
		if (Trapped_List.size() >0)
			Thereisupdate++;

		if (Dead_List_inner.size() >0)
			Thereisupdate++;	
		//4
		if ( NomorecasultyinInner==1  && Mybody.assignedIncident.Communication_Structure_uesd==Inter_Communication_Structure.Struc1_SilverCommandersInteraction )
			Thereisupdate++;
		//5
		if (  EndofER()  && EndER_Fire==-1 && Tactical_Plan.size()!= 0 )
			Thereisupdate++;

		if (Thereisupdate>0 )
			return true;
		else
			return false;
	}
	
	//----------------------------------------------------------------------------------------------------
	public void  SliverMeetingADD()
	{
		int Thereisupdate =0;

		//set update in Meeting record 	
		//1
		if (SliverMeetings.size()==1)
		{
			Current_SliverMeeting.AIC_Addtomeeting_GeolocationTA(); 
			if ( SurveyScene )Current_SliverMeeting.SectorPlanning(Mybody.assignedIncident.Survey_scene_Mechanism_IN, (ArrayList<Casualty>) Mybody.CasualtySeen_list); 

			Thereisupdate++;
		}
		//2
		if (Sector_established==1  )
		{Current_SliverMeeting.FIC_Addtomeeting_EstablishedTA( );Sector_established=2;Thereisupdate++;}

		//if ( routeClearedwreckage_List .size() >0 )
		//{ Current_SliverMeeting.FIC_Addtomeeting_Route( routeClearedwreckage_List  );routeClearedwreckage_List .clear(); }
		//3
		if (Trapped_List.size() >0)
		{ Thereisupdate=Thereisupdate + Trapped_List.size() ;  Current_SliverMeeting.FIC_Addtomeeting_TrappedCasualty( Trapped_List  );Trapped_List.clear(); }

		if (Dead_List_inner.size() >0)
		{ Thereisupdate=Thereisupdate + Dead_List_inner.size() ; Current_SliverMeeting.FIC_Addtomeeting_Casualty( Dead_List_inner  );Dead_List_inner.clear(); }		
		//4
		if ( NomorecasultyinInner==1  && Mybody.assignedIncident.Communication_Structure_uesd==Inter_Communication_Structure.Struc1_SilverCommandersInteraction)
		{Current_SliverMeeting.FIC_Addtomeeting_NomorecasultyinInner();NomorecasultyinInner=2;Thereisupdate++;}
		//5
		if (  EndofER()  && EndER_Fire==-1 && Tactical_Plan.size()!= 0 )
		{Current_SliverMeeting.FIC_Addtomeeting_EndER_Fire();EndER_Fire=2;Thereisupdate++;DoneLastMeeting=true;}	

		Current_SliverMeeting.Thereisupdate_fr=Thereisupdate++; ;

	}

	//----------------------------------------------------------------------------------------------------
	public void  SliverMeetingGET()
	{
		//Get update from Meeting record
		//0-
		if (SliverMeetings.size()==1)
		{			
			
					
			System.out.println("PRIORITY_execution =>  "+ InputFile.PRIORITY_execution +"   "  +"sector" );
			ScheduleParameters params = ScheduleParameters.createRepeating(Mybody.schedule.getTickCount() + 1 , 1,InputFile.PRIORITY_execution ++ );
			//ScheduleParameters params = ScheduleParameters.createRepeating(Mybody.CurrentTick + 1 , 1,ScheduleParameters.FIRST_PRIORITY);
			
			Mybody.assignedIncident.CreateSector2( ) ;	
			for ( Sector  S : Mybody.assignedIncident.Sectorlist2 )
				Mybody.schedule.schedule(params,S,"step");


			for (Casualty  Ca:  Mybody.assignedIncident.casualties )
			{
				if (!Mybody.assignedIncident.Iaminsector(Ca) )

					System.out.println(Ca.ID+" " + Ca.getCurrentLocation().x  + "  " + Ca.getCurrentLocation().y );
			}

			// Arrange the sector points  temp action
			//for  ( Sector S:  Mybody.assignedIncident. Sectorlist )
			//	if (S.Limitpoint==Mybody.VehicleOrStartDirection )  //swap for front sector only
			//	{
			//		S.Limitpoint=S.Startpoint ;
			//		S.Startpoint=Mybody.VehicleOrStartDirection;							 
			//	}

			this.ConsiderSectorNeedinAssignedResponders= Current_SliverMeeting.FIC_Getmeeting_sectorplan() ;
			DoneFirstMeeting=true;
			Mybody.UpdateComNet(Mybody.assignedIncident.PICcommander);  //Update No of messages
			Mybody.UpdateComNet(Mybody.assignedIncident.AICcommander);  //Update No of messages
			
			Mybody.CurrentMessage  = new  ACL_Message( ACLPerformative.CallForSilverMeeting, Mybody.assignedIncident.PICcommander,Mybody ,null, Mybody.CurrentTick,CommunicationMechanism.SilverMeeting,1,TypeMesg.External ) ;
			Mybody.Message_inboxMeeting.add(Mybody.CurrentMessage ); 
			Mybody.CurrentMessage  = new  ACL_Message( ACLPerformative.CallForSilverMeeting, Mybody.assignedIncident.AICcommander,Mybody ,null, Mybody.CurrentTick,CommunicationMechanism.SilverMeeting,1,TypeMesg.External ) ;
			Mybody.Message_inboxMeeting.add(Mybody.CurrentMessage ); 
			

		}			
		//1-
		if ( Current_SliverMeeting.FIC_Getmeeting_Route(   ).size() >0 )
		{
			for( RoadLink RL :Current_SliverMeeting.FIC_Getmeeting_Route(   ) )
				routeNEEDClearwreckage_List.add(RL);

			Mybody.UpdateComNet(Mybody.assignedIncident.AICcommander);  //Update No of messages
			Mybody.CurrentMessage  = new  ACL_Message( ACLPerformative.CallForSilverMeeting, Mybody.assignedIncident.AICcommander,Mybody ,null, Mybody.CurrentTick,CommunicationMechanism.SilverMeeting,routeNEEDClearwreckage_List.size() ,TypeMesg.External) ;
			Mybody.Message_inboxMeeting.add(Mybody.CurrentMessage );  
			
		}
		//2-
		if (   Current_SliverMeeting.FIC_Getmeeting_TA_controlarea()  )  // For FIC  only
		{
			TA_controlarea_established=1 ;

			Mybody.UpdateComNet(Mybody.assignedIncident.PICcommander);  //Update No of messages
			Mybody.CurrentMessage  = new  ACL_Message( ACLPerformative.CallForSilverMeeting, Mybody.assignedIncident.PICcommander,Mybody ,null, Mybody.CurrentTick,CommunicationMechanism.SilverMeeting,1 ,TypeMesg.External) ;
			Mybody.Message_inboxMeeting.add(Mybody.CurrentMessage ); 
			
		}
		//3-
		if (   Current_SliverMeeting.FIC_Getmeeting_TA_CCS()  )  // For SC
		{
			TA_CCS_established=1 ;
			Mybody.UpdateComNet(Mybody.assignedIncident.AICcommander);  //Update No of messages
			Mybody.CurrentMessage  = new  ACL_Message( ACLPerformative.CallForSilverMeeting, Mybody.assignedIncident.AICcommander,Mybody ,null, Mybody.CurrentTick,CommunicationMechanism.SilverMeeting,1,TypeMesg.External ) ;
			Mybody.Message_inboxMeeting.add(Mybody.CurrentMessage );  
			
		}
		//4-
		if (   Current_SliverMeeting.FIC_Getmeeting_TA_RC()  )  // For SC
		{
			TA_RC_established=1 ;
			Mybody.UpdateComNet(Mybody.assignedIncident.PICcommander);  //Update No of messages
			Mybody.CurrentMessage  = new  ACL_Message( ACLPerformative.CallForSilverMeeting, Mybody.assignedIncident.PICcommander,Mybody ,null, Mybody.CurrentTick,CommunicationMechanism.SilverMeeting,1,TypeMesg.External ) ;
			Mybody.Message_inboxMeeting.add(Mybody.CurrentMessage ); 
			
		}

		if (Current_SliverMeeting.FIC_Getmeeting_NomorecasultyinLA() )
		{
			NomorecasultyinLA=1 ;
			Mybody.UpdateComNet(Mybody.assignedIncident.AICcommander);  //Update No of messages
			Mybody.CurrentMessage  = new  ACL_Message( ACLPerformative.CallForSilverMeeting, Mybody.assignedIncident.AICcommander,Mybody ,null, Mybody.CurrentTick,CommunicationMechanism.SilverMeeting,1,TypeMesg.External ) ;
			Mybody.Message_inboxMeeting.add(Mybody.CurrentMessage ); 
			
		}

		if ( Current_SliverMeeting.FIC_Getmeeting_EndER_Ambulance())
		{		EndER_Ambulance= 2;
		Mybody.UpdateComNet(Mybody.assignedIncident.AICcommander);  //Update No of messages
		Mybody.CurrentMessage  = new  ACL_Message( ACLPerformative.CallForSilverMeeting, Mybody.assignedIncident.AICcommander,Mybody ,null, Mybody.CurrentTick,CommunicationMechanism.SilverMeeting,1 ,TypeMesg.External) ;
		Mybody.Message_inboxMeeting.add(Mybody.CurrentMessage ); 
		
		}

		if ( Current_SliverMeeting.FIC_Getmeeting_EndER_Police())
		{	EndER_Police= 2;
		Mybody.UpdateComNet(Mybody.assignedIncident.PICcommander);  //Update No of messages
		Mybody.CurrentMessage  = new  ACL_Message( ACLPerformative.CallForSilverMeeting, Mybody.assignedIncident.PICcommander,Mybody ,null, Mybody.CurrentTick,CommunicationMechanism.SilverMeeting,1,TypeMesg.External ) ;
		Mybody.Message_inboxMeeting.add(Mybody.CurrentMessage );  
		

		}

		//5-
		//if (   Current_SliverMeeting.FIC_Getmeeting_TA_cordons_outer()  )  // For FSC start and infrom its comming responders
		//{
		//	TA_cordon_established=1 ;
		//}
	}
	//______________________________________________________________________________________________________________________________________________________________
	//##############################################################################################################################################################
	//                                                        Behavior
	//##############################################################################################################################################################
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	// FIC commander - General 
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	public void FICBehavior()	
	{		
		//*************************************************************************
		// 1-FirstArrival response
		if( Mybody.SensingeEnvironmentTrigger==RespondersTriggers.FirstArrival_NoResponder) 				
		{
			Mybody.CurrentAssignedActivity=Fire_TaskType.CoordinateFirstArrival;

			NeedFirstMeeting= true;
			DoneFirstMeeting=false;
			DoneLastMeeting=false;
			
//			Mybody.CurrentAssignedActivity=Fire_TaskType.CoordinateER ;
//			Mybody.InterpretedTrigger= RespondersTriggers.TempFICofFirstArrival ;
//			Mybody.SensingeEnvironmentTrigger= null;
		}
		// 2-CoordinateER
		else if ( Mybody.InterpretedTrigger== RespondersTriggers.TempFICofFirstArrival|| Mybody.CommTrigger==RespondersTriggers.FICRolehanded 	) 
		{
			Mybody.CurrentAssignedActivity=Fire_TaskType.CoordinateER ;
		}
		// 3-Handover
		else if (Mybody.Action==RespondersActions.Noaction &&  Mybody.CommTrigger== RespondersTriggers.GetinstructiontoHandover 	) 
		{
			Mybody.CurrentAssignedActivity=Fire_TaskType.HandovertoFIC;
		}
		// 4-Ending
		else if ( Mybody.InterpretedTrigger==RespondersTriggers.DoneEmergencyResponse  )  
		{
			Mybody.CurrentAssignedActivity=Fire_TaskType.CoordinateEndResponse;
		}	
		else  if (Mybody.InterpretedTrigger==RespondersTriggers.ENDER  || Mybody.InterpretedTrigger==RespondersTriggers.FICRolehanded ) 
		{
			Mybody.PrvRoleinprint2=Mybody.Role ;
			Mybody.Role=Fire_ResponderRole.None;
		}

		//*************************************************************************
		switch(Mybody.CurrentAssignedActivity) {
		case CoordinateFirstArrival:  //Temp FIC
			CommanderBehavior_CoordinateFirstArrival();	
			break;
		case	HandovertoFIC : //Temp FIC
			CommanderBehavior_HandovertoFIC();	
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
	// Temp FIC - Coordinate FirstArrival
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^	
	private void CommanderBehavior_CoordinateFirstArrival()
	{
		// ++++++ 1- +++++++
		if( Mybody.SensingeEnvironmentTrigger==RespondersTriggers.FirstArrival_NoResponder )
		{						


			if ( ( Mybody.assignedIncident.AICcommander !=null ||Mybody.assignedIncident.PICcommander !=null )   )
			{
				Mybody.ActivityAcceptCommunication=true;
				Mybody.SensingeEnvironmentTrigger=null;

				Mybody.InterpretedTrigger= RespondersTriggers.TempFICofFirstArrival;
				Mybody.Action=RespondersActions.Noaction;
				Mybody.BehaviourType2=RespondersBehaviourTypes2.WaitingDuring ;

				System.out.println(Mybody.Id + " AssessSituation Done1"  );
			}

			else
			{

				Mybody.Action=RespondersActions.Observeandcount;
				Mybody.BehaviourType2=RespondersBehaviourTypes2.Planformulation;			
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
			Mybody.BehaviourType2=RespondersBehaviourTypes2.Comunication; 
			Mybody.SensingeEnvironmentTrigger=null;	

			//System.out.println(Mybody.Id + " AssessSituation Done1"  );
		}	
		// ++++++ 4- +++++++
		else if (Mybody.Action==RespondersActions.ReportAssessSituation && Mybody.Acknowledged)
		{		
			Mybody.ActivityAcceptCommunication=true;
			Mybody.Acknowledged=false;Mybody.Sending=false; 	
			Mybody.InterpretedTrigger= RespondersTriggers.TempFICofFirstArrival;

			Mybody.Action=RespondersActions.Noaction;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.WaitingDuring ;

			System.out.println(Mybody.Id + " AssessSituation Done2"  );
		}	
	}

	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	// Temp FIC - HandovertoFIC
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^	
	private void  CommanderBehavior_HandovertoFIC()  //temp
	{
		// ++++++ 1- +++++++
		if( Mybody.CommTrigger== RespondersTriggers.GetinstructiontoHandover 	 )
		{						
			//her yiu have more work
			Mybody.Action=RespondersActions.GetcammandtoHandover ;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.RoleAssignment;
			Mybody.EndofCurrentAction=  InputFile.GetCommand_duration  ; 
			Mybody.CommTrigger=null;
		}
		// ++++++ 2- +++++++
		else if ( Mybody.Action==RespondersActions.GetcammandtoHandover  && Mybody.EndActionTrigger== RespondersTriggers.EndingAction 	) 			 	
		{							
			//Actually there is no report send but it will be done through Commander_FIC_HandoverAction 
			Mybody.CurrentMessage = new  ACL_Message( ACLPerformative.InformHandoverReport , Mybody, Mybody.CurrentSender , null, Mybody.CurrentTick ,Mybody.assignedIncident.ComMechanism_inControlArea,1 ,TypeMesg.Inernal) ;
			Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);

			Mybody.Action=RespondersActions.HandovertoFIC;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.RoleAssignment;
			Mybody.EndActionTrigger=null;						
		}
		// ++++++ 3- +++++++
		else if ( Mybody.Action==RespondersActions.HandovertoFIC  &&     Mybody.Acknowledged 	) 			 	
		{		
			Mybody.InterpretedTrigger=RespondersTriggers.FICRolehanded ; //then he will get new commnd role

			Mybody.Role=Fire_ResponderRole.None;				
			Mybody.Action=RespondersActions.Noaction;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.WaitingDuring ;
			Mybody.Acknowledged=false;Mybody.Sending=false; 	
		}
	}

	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	// FIC - Coordinate   ER 
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	private void CommanderBehavior_CoordinateER()
	{								
		// ++++++ 1- +++++++
		if ( ( Mybody.CommTrigger==RespondersTriggers.FICRolehanded ||  Mybody.InterpretedTrigger== RespondersTriggers.TempFICofFirstArrival ) && Tactical_Plan.size()== 0  &&  ! NeedFirstMeeting  )   	
		{
			// Implmenting_plan(); diffrent vision

			Mybody.Action=RespondersActions.FormulatePlan;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.Planformulation;	
			Mybody.EndofCurrentAction=  InputFile.FormulatePlan_duration ; 

			if ( Mybody.InterpretedTrigger== RespondersTriggers.TempFICofFirstArrival)Mybody.InterpretedTrigger=null; 
			if ( Mybody.CommTrigger==RespondersTriggers.ICRolehanded  )Mybody.CommTrigger=null;
		}
		// ++++++ 2- +++++++
		if ( Mybody.Action==RespondersActions.Noaction && Tactical_Plan.size()== 0 && NeedFirstMeeting  &&  DoneFirstMeeting  )   	
		{

			Implmenting_plan();  // gelocation + sectorplan
			DoneFirstMeeting=false;

			Mybody.Action=RespondersActions.FormulatePlan;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.Planformulation;
			Mybody.EndofCurrentAction=  InputFile.FormulatePlan_duration ; 

			if ( Mybody.InterpretedTrigger== RespondersTriggers.TempFICofFirstArrival)Mybody.InterpretedTrigger=null; 
			if ( Mybody.CommTrigger==RespondersTriggers.ICRolehanded  )Mybody.CommTrigger=null;
		}
		// ++++++ 3- +++++++
		else if ( Mybody.Action==RespondersActions.FormulatePlan  && Mybody.EndActionTrigger== RespondersTriggers.EndingAction 	) 			 	
		{							
			Mybody.Action=RespondersActions.Noaction;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.WaitingDuring;
			Mybody.EndActionTrigger=null;	

		}
		//-------------------------------------------------------------------------------------------- (P1-called) Silver meeting  
		// ++++++ 4- +++++++
		else if (Mybody.Action==RespondersActions.Noaction  && Mybody.CommTrigger== RespondersTriggers.GetCallForSilverMeeting    )
		{
			// go to location need think
			Current_SliverMeeting.FIC_Attendance(Mybody ) ;
			SliverMeetings.add(Current_SliverMeeting);

			Mybody.Action=RespondersActions.GetCallForSilverMeeting;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.ComunicationDelay;
			Mybody.CommTrigger=null;
			
			Mybody.ActivityAcceptCommunication=false;
			Mybody.SendingReciving_internal= false; 
			Mybody.SendingReciving_External= true ; 

		}
		// ++++++ 5- +++++++
		else if (( Mybody.Action==RespondersActions.GetCallForSilverMeeting || Mybody.Action==RespondersActions.CallForSilverMeeting )&&  Mybody.InterpretedTrigger== RespondersTriggers.SliverMeetingStart  )
		{

			SliverMeetingADD();

			Mybody.Action=RespondersActions.Meeting ;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.Comunication;   
			Mybody.InterpretedTrigger=null;

			System.out.println("                                                                                                                                 " + "SM=======================" + Mybody.Id + " FIC in sliver meeting" );
		}
		// ++++++ 6- +++++++
		else if (Mybody.Action==RespondersActions.Meeting  && Mybody.InterpretedTrigger == RespondersTriggers.SliverMeetingEnd  )
		{
			Mybody.ActivityAcceptCommunication=true;
			SliverMeetingGET();
			Current_SliverMeeting=null;

			Mybody.Action=RespondersActions.UpdatePlan;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.Planformulation;
			Mybody.EndofCurrentAction= InputFile.UdatePlan_duration ; 
			Mybody.InterpretedTrigger=null; 

			Mybody.SendingReciving_internal= false; 
			Mybody.SendingReciving_External= false ; 
			
			System.out.println("                                                                                                                                 " + "SM=======================" + Mybody.Id + " FIC end sliver meeting" );
		}

		//-------------------------------------------------------------------------------------------- (P1-need) Silver Meeting  call
		else  if ( Mybody.Action==RespondersActions.Noaction && Mybody.CommTrigger==null && ( Mybody.assignedIncident.PICcommander !=null && Mybody.assignedIncident.AICcommander !=null ) &&     
				( 
						(   Mybody.assignedIncident.Inter_Communication_Time_used==Communication_Time.When_need && Mybody.assignedIncident.Communication_Structure_uesd==Inter_Communication_Structure.Struc1_SilverCommandersInteraction  && ISthereupdateTosend( )  ) ||
						(    EndofER()  && EndER_Fire==-1 && Tactical_Plan.size()!= 0  && !DoneLastMeeting &&  Mybody.assignedIncident.Communication_Structure_uesd==Inter_Communication_Structure.Struc2_BronzeCommandersInteraction ) 
						)
				)

		{			
			System.out.println("                                                                                                                                 " + "SM=======================" + Mybody.Id + " BoradcastcallForsliverMeeting  " +  " " + Mybody.CurrentTick  );

			// current location or control area 
			PointDestination locationSilverMeeting=null ; 

			if (Mybody.assignedIncident.ControlArea !=null && Mybody.assignedIncident.ControlArea.installed)
				locationSilverMeeting=Mybody.assignedIncident.ControlArea.Location ;
			else
				locationSilverMeeting=Mybody.assignedIncident.bluelightflashing_Point ;

			Current_SliverMeeting = new SliverMeetingRecord ("0",locationSilverMeeting , Mybody.assignedIncident) ;	
			
			System.out.println("PRIORITY_execution =>  "+ InputFile.PRIORITY_execution +"   "  +Current_SliverMeeting.MeetingID);
			ScheduleParameters params = ScheduleParameters.createRepeating(Mybody.schedule.getTickCount() + 1 , 1,InputFile.PRIORITY_execution ++ );
			Mybody.schedule.schedule(params,Current_SliverMeeting,"step");
			
			

			SliverMeetings.add(Current_SliverMeeting);
			BoradcastSilverMeetingAction(Current_SliverMeeting );	//call 

			Mybody.Action=RespondersActions.BoradcastcallForsliverMeeting;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.ComunicationDelay;
			Mybody.EndofCurrentAction=   InputFile.Boradcast_duration_FFcall ;
			
			Mybody.ActivityAcceptCommunication=false;
			Mybody.SendingReciving_internal= false; 
			Mybody.SendingReciving_External= true ; 

		}
		// ++++++ 5- +++++++
		else if (Mybody.Action==RespondersActions.BoradcastcallForsliverMeeting && Mybody.EndActionTrigger== RespondersTriggers.EndingAction  )
		{
			Current_SliverMeeting.FIC_Attendance(Mybody ) ;
			Mybody.Action=RespondersActions.CallForSilverMeeting ;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.ComunicationDelay; 
			Mybody.EndActionTrigger=null;
			Mybody.Sending=false; 
		}

		//-------------------------------------------------------------------------------------------- (P1)
		//==========================New Responder==========================
		// ++++++ 7- +++++++
		else if (Mybody.Action==RespondersActions.Noaction  && ( Mybody.CommTrigger == RespondersTriggers.GetNewResponderarrived ||  Mybody.CommTrigger == RespondersTriggers. GetbackResponderarrived))		  										
		{

			//UpdatePlan			
			if (  Mybody.CommTrigger == RespondersTriggers.GetNewResponderarrived )
			{
				MyResponder_Fire.add(NewResponder) ;
				if (NewResponder.Role ==Fire_ResponderRole.None) //not driver
					UnoccupiedResponders.add(NewResponder);
				else	
					UnoccupiedDrivers.add(NewResponder);  // not used in fire
			}
			else if (Mybody.CommTrigger == RespondersTriggers. GetbackResponderarrived)
			{
				UnoccupiedResponders.add(BackResponder);
				respondrsBack=true;
			}

			Mybody.Action=RespondersActions.GetNewArrivalNotification ;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.Comunication;  
			Mybody.EndofCurrentAction=  InputFile.GetNotification_duration  ; 

			//System.out.println(Mybody.Id + " GetNewResponderarrived1" );
		}
		// ++++++ 8- +++++++
		else if ( Mybody.Action==RespondersActions.GetNewArrivalNotification  && Mybody.EndActionTrigger== RespondersTriggers.EndingAction 	) 			 	
		{				
			if (  Mybody.CommTrigger == RespondersTriggers.GetNewResponderarrived )
				NewResponder.Acknowledg(Mybody);	//Always F-F
			else
				BackResponder.Acknowledg(Mybody);

			Mybody.Action=RespondersActions.UpdatePlan;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.Planformulation;
			Mybody.EndofCurrentAction=   InputFile.UdatePlan_duration  ; 
			Mybody.EndActionTrigger=null; 
			Mybody.CommTrigger=null; 
			//System.out.println(Mybody.Id + " GetNewResponderarrived2" );
		}

		//==========================Get OP Report from BZC ========================================= //OP Report  or arrival location 
		// ++++++ 11- +++++++
		else if (Mybody.Action==RespondersActions.Noaction && ( Mybody.CommTrigger== RespondersTriggers.GetOPReport || Mybody.CommTrigger== RespondersTriggers.GetBronzeCommanderstart ||Mybody.CommTrigger== RespondersTriggers.ResultofSetupTA)   )		  										
		{	 	
			int Total=1;
			//1 -F A
			if (  Mybody.CommTrigger== RespondersTriggers.GetBronzeCommanderstart)
			{
				Total++; //Start activity
						
				for ( Activity_Fire  A : Tactical_Plan ) 		
					if ( A.Activity== Fire_ActivityType.SearchandRescueCasualty && A.BronzCommander == this.CurrentBZC )  
					startSR=1;
					else if ( A.Activity== Fire_ActivityType.ClearRoute  && A.BronzCommander == this.CurrentBZC ) 
						startWRK=1;	
				
			}
			// 2- Setup
			if (  Mybody.CommTrigger== RespondersTriggers.ResultofSetupTA )
			{
				if ( Sector_established==0  )  // if there is 4 sctors   if ( IsAllSectorDone(  ) ) NomorecasultyinInner=1;	
					Sector_established =1;
				Total++;
			}
			// 2- Route
			if (  CurrentRouteListbySender !=null && CurrentRouteListbySender.size() >0  )
			{
				for( RoadLink RL :CurrentRouteListbySender )
				{routeClearedwreckage_List.add(RL);Total++;}
				CurrentRouteListbySender.clear();
			}
			//3- Trapped casualty					
			if (  CurrentTrapped_ListbySender !=null && CurrentTrapped_ListbySender.size() >0  )
			{
				for( Casualty ca :CurrentTrapped_ListbySender )
				{Trapped_List.add(ca);Total++;}
				CurrentTrapped_ListbySender.clear();

			}
			//3- Dead casualty					
			if (  CurrentDead_innerListbySender !=null &&  CurrentDead_innerListbySender.size() >0  )
			{
				for( Casualty ca :CurrentDead_innerListbySender )
				{Dead_List_inner.add(ca);Total++;}
				CurrentDead_innerListbySender.clear();
			}	
			//4- End casualty							
			if (EndSearchcasulties)
			{
				for ( Activity_Fire  A : Tactical_Plan ) 				
					if ( A.BronzCommander == CurrentBZC  && A.Activity==Fire_ActivityType.SearchandRescueCasualty )
					{ A.ActivityStatus=GeneralTaskStatus.Done ;   EndSearchcasulties=false;  break;}  
				Total++;
				if ( IsAllSectorDone(  ) ) NomorecasultyinInner=1;		
			}

			//4- End ClearRouteWreckage							
			if (EndClearRouteWreckage	)
			{
				for ( Activity_Fire  A : Tactical_Plan ) 				
					if ( A.BronzCommander == CurrentBZC  && A.Activity==Fire_ActivityType.ClearRoute )
					{ A.ActivityStatus=GeneralTaskStatus.Done ; EndClearRouteWreckage=false;   break;}
				Total++;
			}


			Mybody.Action=RespondersActions.GetOPReport;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.Comunication;  
			Mybody.EndofCurrentAction= InputFile.GetInfromation_FtoForR_Duration_Data * Total ; //InputFile.GetReport_duration ;
			Mybody.CommTrigger=null; 
			//System.out.println("                                       "   +"FIC: "  + Mybody.Id + " GetOPReport " + CurrentBZC.Role);
		}
		// ++++++ 12- +++++++
		else if ( Mybody.Action==RespondersActions.GetOPReport  && Mybody.EndActionTrigger== RespondersTriggers.EndingAction 	) 			 	
		{
			CurrentBZC.Acknowledg(Mybody);	
			System.out.println("                                       "   +"FIC: "  + Mybody.Id + " GetOPReport  from " + CurrentBZC.Role);
			Mybody.Action=RespondersActions.UpdatePlan;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.Planformulation;
			Mybody.EndofCurrentAction= InputFile.UdatePlan_duration ; 
			Mybody.EndActionTrigger=null; 

		}		
		//============================================================ ALL
		// ++++++ 13- +++++++
		else if (Mybody.Action==RespondersActions.UpdatePlan && Mybody.EndActionTrigger== RespondersTriggers.EndingAction  )
		{						
			Mybody.Action=RespondersActions.Noaction;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.WaitingDuring;
			Mybody.EndActionTrigger=null;
		}
		//-------------------------------------------------------------------------------------------- (P2)
		// ++++++ 14- +++++++
		else  if (Mybody.Action==RespondersActions.Noaction && (Mybody.CurrentTick >= Time_last_updated + InputFile.UpdatEOCEvery)&& false  ) 
		{
			//Reporting_URSituationAssessment();
			Mybody.Action=RespondersActions.InfromReport;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.Comunication;  

		}
		// ++++++ 15- +++++++
		else if (Mybody.Action==RespondersActions.InfromReport && Mybody. Acknowledged )
		{
			Mybody.Action=RespondersActions.Noaction;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.WaitingDuring;
			Mybody.Acknowledged=false;Mybody.Sending=false; 
			Time_last_updated=Mybody.CurrentTick ;
			//System.out.println(Mybody.Id + " don inform report" +Mybody.assignedIncident.RadioSystem_ChanleFree() +"    "+Mybody.CurrentTick );
			System.out.println("                                       "   +"FIC: "  + Mybody.Id + " Infrom EOC Report");	
		}		
		//-------------------------------------------------------------------------------------------- (P4)
		// ++++++ 16- +++++++
		else if ( Mybody.Action==RespondersActions.Noaction  && UnoccupiedResponders.size()>0 && Tactical_Plan.size()!= 0	) 		 	
		{				
			CurrentResponder= UnoccupiedResponders.get(0);
			UnoccupiedResponders.remove(0);

			Allocation_Command = AssigneRoleandActivitytoResponder(CurrentResponder , null);
			if ( CurrentResponder.Role == Fire_ResponderRole.None ) 
				IsNewResponder= true ;
			else
				IsNewResponder= false ;

			Mybody.Action=RespondersActions.AllocatResponders;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.Planformulation;
			Mybody.EndofCurrentAction=   InputFile.Allocation_duration ;
		}
		// ++++++ 17- +++++++
		else if (Mybody.Action==RespondersActions.AllocatResponders && Mybody.EndActionTrigger== RespondersTriggers.EndingAction  ) 
		{	
			// send message with command F-F			
			if (IsNewResponder)
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
			Mybody.BehaviourType2=RespondersBehaviourTypes2.Comunication; //RoleAssignment; 
			Mybody.EndActionTrigger=null;						
		}
		// ++++++ 18- +++++++
		else if (Mybody.Action==RespondersActions.CommandAsignRole && Mybody.Acknowledged )
		{
			Mybody.Acknowledged=false;Mybody.Sending=false; 
			if (! InstrcuteTempFCOtoleave)
			{
				Mybody.Action=RespondersActions.Noaction;
				Mybody.BehaviourType2=RespondersBehaviourTypes2.WaitingDuring;

				if (  MajorActivity_Command !=null  && IsNewResponder )
				{
					Mybody.CurrentMessage = new  ACL_Message( ACLPerformative.Command , Mybody, CurrentResponder ,MajorActivity_Command , Mybody.CurrentTick,Mybody.assignedIncident.ComMechanism_inControlArea,1,TypeMesg.Inernal) ;
					Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
					MajorActivity_Command=null ;

					Mybody.Action=RespondersActions.CommandAsignRole;
					Mybody.BehaviourType2=RespondersBehaviourTypes2.Comunication; //RoleAssignment; 
					//System.out.println(Mybody.Id + " MA" );
				}
			}			
			else
			{
				// send message with command to driver to leave if there role PCO
				Mybody.CurrentMessage = new  ACL_Message( ACLPerformative.Instructiontoleave ,Mybody, TempFCO ,null, Mybody.CurrentTick,Mybody.assignedIncident.ComMechanism_inControlArea,1 ,TypeMesg.Inernal) ;
				Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
				Mybody.Action=RespondersActions.CommandAsignRole;
				Mybody.BehaviourType2=RespondersBehaviourTypes2.Comunication; //RoleAssignment; 

				InstrcuteTempFCOtoleave=false;
				UnoccupiedDrivers.add(TempFCO);  // not used
				this.MyResponder_Fire.add(TempFCO);  // not used
			}			
		}
		//-------------------------------------------------------------------------------------------- (P5) Request or updates to bronze commanders
		//==========================Request for N responders==============================
		// ++++++ 19- +++++++		
		//		else if ( Mybody.Action==RespondersActions.Noaction  && NFireFighter > 0   ) 	 	 	
		//		{						
		//			option=99 ;
		//			//which sector  NFireFighter ??????  Mybody.assignedIncident.FSCcommander
		//			Mybody.CurrentMessage = new  ACL_Message( ACLPerformative.RequsteReallocateNResponders, Mybody, null ,NFireFighter, Mybody.CurrentTick,InputFile.ComMechanism_level_TtoO) ;
		//			Mybody.CurrentMessage1.add(Mybody.CurrentMessage);
		//			NFireFighter=0;
		//
		//			Mybody.Action=RespondersActions.RequstandupdatesinOP;
		//			Mybody.BehaviourType2=RespondersBehaviourTypes2.Comunication ; //Planexecution;
		//
		//		}
		//==========================Request for clear routes ============================== // from  AIC
		// ++++++ 20- +++++++		
		else if ( Mybody.Action==RespondersActions.Noaction  && startWRK==1 && routeNEEDClearwreckage_List.size()>0 && Mybody.assignedIncident.FRFSCcommander!=null && Mybody.assignedIncident.Communication_Structure_uesd==Inter_Communication_Structure.Struc1_SilverCommandersInteraction ) 	 	 	
		{						
			option=99 ;
			Command CMD1 =new Command();
			CMD1.FireCommand( "0" , Fire_TaskType.ClearRouteWreckage, routeNEEDClearwreckage_List ); // from FIC to FSC
			Mybody.CurrentMessage = new  ACL_Message( ACLPerformative.Requste, Mybody, Mybody.assignedIncident.FRFSCcommander,CMD1, Mybody.CurrentTick,Mybody.assignedIncident.ComMechanism_level_StoB,routeNEEDClearwreckage_List.size() ,TypeMesg.External ) ;
			Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);

			routeNEEDClearwreckage_List.clear();

			Mybody.Action=RespondersActions.RequstandupdatesinOP;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.Comunication ;
			System.out.println("                                       "   +"FIC: "  + Mybody.Id + " RouteNEEDClearwreckage ");

		}
		//==========================Update about CCS established   ============================== // from  AIC
		// ++++++ 21- +++++++		
		else if ( Mybody.Action==RespondersActions.Noaction  && startSR==1 && ( TA_CCS_established==1 && this.Sector_established >=1)  && Mybody.assignedIncident.Communication_Structure_uesd==Inter_Communication_Structure.Struc1_SilverCommandersInteraction  ) 	 	 	
		{						
			//option=98 ;
			//BoradcastsectorAction(ACLPerformative.InfromCCSEstablished );
			//Mybody.EndofCurrentAction=   InputFile.Boradcast_duration ;

			option=99 ;
			for ( Activity_Fire  A : Tactical_Plan ) 		
				if ( A.Activity== Fire_ActivityType.SearchandRescueCasualty  && A.informed_TA_CCS_established==false )  
				{
					Mybody.CurrentMessage = new  ACL_Message( ACLPerformative.InfromCCSEstablished , Mybody, A.BronzCommander,null, Mybody.CurrentTick,Mybody.assignedIncident.ComMechanism_level_StoB,1 , TypeMesg.External) ;
					A.informed_TA_CCS_established= true;
					Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
				}
			TA_CCS_established=2 ;

			Mybody.Action=RespondersActions.RequstandupdatesinOP;  
			Mybody.BehaviourType2=RespondersBehaviourTypes2.Comunication ; 



			System.out.println("                                       "   +"FIC: "  + Mybody.Id + " TA_CCS_established  " );
		}
		//==========================Update about   RC  established   ============================== // from  PIC
		// ++++++ 22- +++++++		
		else if ( Mybody.Action==RespondersActions.Noaction  && 	startSR==1 && ( TA_RC_established==1 && this.Sector_established >=1)  && Mybody.assignedIncident.Communication_Structure_uesd==Inter_Communication_Structure.Struc1_SilverCommandersInteraction   ) 	 	 	
		{						
			//option=98 ;
			//BoradcastsectorAction(ACLPerformative.InfromRCEstablished );
			//Mybody.EndofCurrentAction=   InputFile.Boradcast_duration ;

			option=99 ;
			for ( Activity_Fire  A : Tactical_Plan ) 		
				if ( A.Activity== Fire_ActivityType.SearchandRescueCasualty  && A.informed_TA_RC_established==false )  
				{
					Mybody.CurrentMessage = new  ACL_Message( ACLPerformative.InfromRCEstablished , Mybody, A.BronzCommander,null, Mybody.CurrentTick,Mybody.assignedIncident.ComMechanism_level_StoB,1,TypeMesg.External) ;
					Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
					A.informed_TA_RC_established= true;

				}

			TA_RC_established=2 ;

			Mybody.Action=RespondersActions.RequstandupdatesinOP;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.Comunication ; 

			//System.out.println(Mybody.Id+" "  +Mybody.CurrentTick +  "  Action:" + Mybody.Action + " com:" + Mybody.CommTrigger + "   Nt:" + Mybody.InterpretedTrigger+ " en:" + Mybody.SensingeEnvironmentTrigger + " end:" + Mybody.EndActionTrigger +  "Acknowledged: "+ Mybody.Acknowledged+"sending: "+  Mybody.Sending  + "CurrentMessage: " + Mybody.CurrentMessage.performative +" " + this.MyResponder_Fire.size()  +"  " + this.UnoccupiedResponders.size());
			System.out.println("                                       "   +"FIC: "  + Mybody.Id + " TA_RC_established  "  );

		}
		//==========================Update about  Nomorecasultyin LA  FSC ==============================  // from  AIC
		// ++++++ 33- +++++++		
		else if ( Mybody.Action==RespondersActions.Noaction  && 	 startWRK==1 && NomorecasultyinLA==1  && Mybody.assignedIncident.Communication_Structure_uesd==Inter_Communication_Structure.Struc1_SilverCommandersInteraction ) 	 // from AIC	 	
		{						
			option=99 ;
			Mybody.CurrentMessage = new  ACL_Message( ACLPerformative.InformNomorecasualty ,Mybody , Mybody.assignedIncident.FRFSCcommander,null, Mybody.CurrentTick,Mybody.assignedIncident.ComMechanism_level_StoB,1,TypeMesg.External) ;		
			Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
			NomorecasultyinLA=2 ;

			Mybody.Action=RespondersActions.RequstandupdatesinOP;  
			Mybody.BehaviourType2=RespondersBehaviourTypes2.Comunication ; 

			System.out.println("                                       "   +"FIC: "  + Mybody.Id  +"    update WRK Nomorecasulty for evacuation  ");

		}
		//============================================================ ALL
		// ++++++ 23- +++++++
		else if ( (Mybody.Action==RespondersActions.RequstandupdatesinOP && Mybody.Acknowledged && option==99)  
				|| (   Mybody.Action==RespondersActions.RequstandupdatesinOP && Mybody.EndActionTrigger== RespondersTriggers.EndingAction && option==98 ) )
		{
			Mybody.Action=RespondersActions.Noaction;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.WaitingDuring;
			Mybody.Acknowledged=false;Mybody.Sending=false; 
			Mybody.EndActionTrigger=null;
			option=0 ;
			System.out.println("                                       "   +"FIC: "  + Mybody.Id + " Get Acknowledged  after RequstandupdatesinOP  " );
		}
		//-------------------------------------------------------------------------------------------- (P6)
		// ++++++ 24- +++++++
		else if ( Mybody.Action==RespondersActions.Noaction && EndofER() && Tactical_Plan.size()!= 0 && EndER_Ambulance==2  && EndER_Police==2 && this.EndER_Fire==2 )  
		{
			Mybody.InterpretedTrigger=RespondersTriggers.DoneEmergencyResponse ;
			System.out.println("                                       "   +"FIC: "  + Mybody.Id + " End ER $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");	
			//System.gc() ;
		}
	}

	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	// FIC -  CoordinateEndResponse  
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	private void CommanderBehavior_EndER()
	{
		// ++++++ 1- +++++++
		if ( Mybody.InterpretedTrigger==RespondersTriggers.DoneEmergencyResponse ) 
		{

			Reporting_ENDSituationAssessment();

			Mybody.Action=RespondersActions.InfromReport;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.Comunication; 
			Mybody.InterpretedTrigger=null;

		}
		// ++++++ 2- +++++++
		else if (Mybody.Action==RespondersActions.InfromReport && Mybody. Acknowledged  )
		{
			// Send message to its commanders
			BoradcastEndofERAction();
			option=1 ;

			Mybody.Action=RespondersActions.BoradcastEndER ;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.Comunication ; 
			Mybody.EndofCurrentAction=   InputFile.Boradcast_duration ;
			Mybody.Acknowledged=false;Mybody.Sending=false; 	

		}
		// ++++++ 2- +++++++
		else if (Mybody.Action==RespondersActions.BoradcastEndER  && Mybody.EndActionTrigger== RespondersTriggers.EndingAction && option==1 )
		{

			// Send message to its driver
			BoradcastEndofERAction2();
			option=2 ;
			Mybody.Action=RespondersActions.BoradcastEndER ;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.Comunication ; //Sharinginfo;
			Mybody.EndofCurrentAction=   InputFile.Boradcast_duration ;
			Mybody.EndActionTrigger= null;	

		}

		// ++++++ 3- +++++++
		else if (Mybody.Action==RespondersActions.BoradcastEndER && Mybody.EndActionTrigger== RespondersTriggers.EndingAction && option==2 )
		{
			Mybody.InterpretedTrigger=RespondersTriggers.ENDER;

			Mybody.Action=RespondersActions.Noaction ;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.WaitingEnd;

			Mybody.Sending=false; 
			Mybody.EndActionTrigger= null;
			System.out.println( "                                       "  + Mybody.Id  +" GO back to Vehicle  " +Mybody.Role );

			//for ( Responder_Fire RA : MyResponder_Fire )
			//	System.out.println(RA.Id + "-----" + RA.Action );

		}
	}

}//end class

