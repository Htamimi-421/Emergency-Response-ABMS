package A_Roles_Police;
import java.util.ArrayList;
import java.util.List;
import A_Agents.Casualty;
import A_Agents.Responder;
import A_Agents.Responder_Police;
import A_Environment.PointDestination;
import A_Environment.RoadLink;
import B_Classes.Activity_Fire;
import B_Classes.Activity_Police;
import B_Classes.Activity_ambulance;
import B_Communication.ACL_Message;
import B_Communication.Command;
import B_Communication.ISRecord;
import B_Communication.Report;
import B_Communication.SliverMeetingRecord;
import C_SimulationInput.InputFile;
import D_Ontology.Ontology.ACLPerformative;
import D_Ontology.Ontology.Ambulance_ActivityType;
import D_Ontology.Ontology.CommunicationMechanism;
import D_Ontology.Ontology.Communication_Time;
import D_Ontology.Ontology.GeneralTaskStatus;
import D_Ontology.Ontology.Inter_Communication_Structure;
import D_Ontology.Ontology.Police_ActivityType;
import D_Ontology.Ontology.Police_ResponderRole;
import D_Ontology.Ontology.Police_TaskType;
import D_Ontology.Ontology.RandomWalking_StrategyType;
import D_Ontology.Ontology.ReportSummery;
import D_Ontology.Ontology.RespondersActions;
import D_Ontology.Ontology.RespondersBehaviourTypes1;
import D_Ontology.Ontology.RespondersBehaviourTypes2;
import D_Ontology.Ontology.RespondersBehaviourTypes3;
import D_Ontology.Ontology.RespondersTriggers;
import D_Ontology.Ontology.SurveyScene_SiteExploration_Mechanism;
import D_Ontology.Ontology.TypeMesg;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.engine.schedule.ScheduleParameters;

public class Police_Commander_PIC {

	Responder_Police Mybody;
	boolean TempPIC=true;

	//----------------------------------------------------
	Command    Allocation_Command ,  MajorActivity_Command;

	Responder_Police NewResponder=null,BackResponder=null ,CurrentResponder=null ;
	Responder_Police TempPCO , CurrentBZC ;

	Report CurrentReport=null;
	ArrayList<RoadLink> CurrentRouteListbySender=new ArrayList<RoadLink>();

	//ArrayList<Casualty> CurrentDead_CCSListbySender =new ArrayList<Casualty>();
	//ArrayList<Casualty> CurrentDead_innerListbySender =new ArrayList<Casualty>();
	//ArrayList<Casualty> CurrentCainf_evacuatedListbySender =new ArrayList<Casualty>();
	//----------------------------------------------------

	boolean InstrcuteTempPCOtoleave=false ;
	boolean SatartSetupTA=false ,  EndSetupTA=false  ,EndCollectioninformation=false , EndClearRouteTraffic=false	;	
	boolean IsNewResponder ;
	int NPolicemen=0 ;
	int option=0 ;
	int NEEDfromAESSMENT=0;
	int AssessSituation_casualtycount=0;
	boolean NeedFirstMeeting  , DoneFirstMeeting, DoneLastMeeting ;
	boolean SurveyScene ;
	//----------------------------------------------------
	boolean  GeolocationofTA_cordons , GeolocationofTA_controlarea , GeolocationofTA_RC ;
	//  The meaning of attribute content....  -1:No update   , 0: Get  , 1: realize or done  2: send to workers or sliver commander
	int cordons_outer_established=-1 , controlarea_established= -1  ,  RC_established=-1 ;
	int  TA_Sector_established=-1  ;// ???

	 int sartCInfo=-1 ; int startCC=-1;
	//----------------------------------------------------
	boolean IsCasualtyReport=false;
	public ArrayList<Casualty> Casualties_Uninjured=new ArrayList<Casualty>();
	//public ArrayList<Casualty> Casualties_Injured=new ArrayList<Casualty>();
	public ArrayList<Casualty> Casualties_Evacuated1=new ArrayList<Casualty>();
	public ArrayList<Casualty> Casualties_Deceased=new ArrayList<Casualty>();


	public ArrayList<ISRecord > DeadResultReport_RecList=new ArrayList<ISRecord >();
	public ArrayList<Casualty> DeadResultReport_CaList =new ArrayList<Casualty>();

	//----------------------------------------------------
	public List<Activity_Police> Tactical_Plan = new ArrayList<Activity_Police>();	
	public List<Responder_Police> MyResponder_Police = new ArrayList<Responder_Police>(); // all Responder of Police
	public List<Responder_Police> UnoccupiedResponders = new ArrayList<Responder_Police>(); //New Responders
	public List<Responder_Police> UnoccupiedDrivers = new ArrayList<Responder_Police>(); //New Driver

	//----------------------------------------------------
	double Time_last_updated=0;	
	double Time_last_SliverMeeting=0;
	int Meetingcounter=0 ;// temp
	SliverMeetingRecord Current_SliverMeeting ; 

	public List<SliverMeetingRecord> SliverMeetings= new ArrayList<SliverMeetingRecord>(); 

	//input silver meeting	
	boolean  FirstArrivalAssessment ;
	ArrayList<RoadLink> routeClearedtraffic_List =new ArrayList<RoadLink>(); 

	// output	
	int NomorecasultyinInner=-1 , NomorecasultyinCCS=-1 , NomorecasultyinLA=-1 ;
	int EndER_Ambulance=-1 , EndER_Fire=-1 , EndER_Police=-1; 
	ArrayList<Casualty> Dead_List_inner =new ArrayList<Casualty>();  
	ArrayList<Casualty> Dead_List_CCS =new ArrayList<Casualty>();
	//ArrayList<Casualty> injured_List =new ArrayList<Casualty>(); 
	ArrayList<Casualty> Cainfor_evacuation_List =new ArrayList<Casualty>(); // evacuated
	ArrayList<RoadLink> routeNEEDCleartraffic_List =new ArrayList<RoadLink>(); 

	//##############################################################################################################################################################	
	public Police_Commander_PIC ( Responder_Police _Mybody) 
	{
		Mybody=_Mybody;
		Mybody.ColorCode= 5;
		Mybody.assignedIncident.PICcommander=Mybody;

		Mybody.PrvRoleinprint3 =Mybody.Role;

		Mybody.assignedIncident.xxx=this.MyResponder_Police; //temp for trace
	}

	public void Commander_PIC_HandoverAction ( Responder_Police TempPIC)  //called by HRO to update
	{
		this.Tactical_Plan =TempPIC.CurrentCalssRole3.Tactical_Plan ;
		this.MyResponder_Police=	TempPIC.CurrentCalssRole3.MyResponder_Police;
		this.UnoccupiedResponders =TempPIC.CurrentCalssRole3.UnoccupiedResponders;
		this.UnoccupiedDrivers=TempPIC.CurrentCalssRole3.UnoccupiedDrivers ;
		this.DeadResultReport_RecList = TempPIC.CurrentCalssRole3.DeadResultReport_RecList; 
		this.DeadResultReport_CaList=TempPIC.CurrentCalssRole3.DeadResultReport_CaList ;


		int x= TempPIC.Message_inbox.size()-TempPIC.Lastmessagereaded ;  

		System.out.println(Mybody.Id + " number of message transferred" + x  );

		while (TempPIC.Lastmessagereaded < TempPIC.Message_inbox.size()  ) //Right now there is now system to check if there is unread message wich meaning waiting responders to comunicate
		{
			Mybody.Message_inbox.add(TempPIC.Message_inbox.get(TempPIC.Message_inbox.size()- x)) ;   
			x--;
		}

	}

	//##############################################################################################################################################################
	public void CommanderPIC_InterpretationMessage()
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
			if ( Mybody.CurrentSender instanceof Responder_Police && TempPIC && Mybody.CurrentCommandRequest.commandType1 == null  )  
			{	
				Mybody.CommTrigger= RespondersTriggers.GetinstructiontoHandover  ;
			}
			//++++++++++++++++++++++++++++++++++++++
			break;
		case InformNewResponderArrival:	
			NewResponder= (Responder_Police) Mybody.CurrentSender;			
			Mybody.CommTrigger= RespondersTriggers.GetNewResponderarrived;		
			break;
		case InformComebackResponderArrival:	
			BackResponder= (Responder_Police) Mybody.CurrentSender;			
			Mybody.CommTrigger= RespondersTriggers.GetbackResponderarrived	;		
			break;	
			//------------------------------- 
		case InformlocationArrival: //from bronze commander
			Mybody.CommTrigger= RespondersTriggers.GetBronzeCommanderstart ; //start 
			CurrentBZC=(Responder_Police) Mybody.CurrentSender;		 
			break;
		case  InfromCordonEstablished: 
			Mybody.CommTrigger= RespondersTriggers.ResultofSetupTA  ;
			CurrentBZC = (Responder_Police)currentmsg.sender;
			cordons_outer_established= 0 ; 
			break;
			//case  InfromControalAreaEstablished: 
			//Mybody.CommTrigger= RespondersTriggers.ResultofSetupTA;
			//CurrentBZC = (Responder_Police)currentmsg.sender;
			//controlarea_established= 0 ;
			//break;
		case  InfromRCEstablished: 
			Mybody.CommTrigger= RespondersTriggers.ResultofSetupTA;
			CurrentBZC = (Responder_Police)currentmsg.sender;
			RC_established=0 ;
			break;
			//------------------------------- 
		case  InformClearandSecurOfRouteReport  :  //here no end 
			Mybody.CommTrigger= RespondersTriggers.GetOPReport ;
			CurrentBZC = (Responder_Police)currentmsg.sender;
			CurrentReport=((Report) currentmsg.content);
			CurrentRouteListbySender =CurrentReport.routeClearedtraffic_List;
			//System.out.println(Mybody.Id + "  RT " );
			break;
		case  InformEndClearRouteTraffic  :  
			Mybody.CommTrigger= RespondersTriggers.GetOPReport ;
			CurrentBZC = (Responder_Police)currentmsg.sender;
			EndClearRouteTraffic=true;
			break;			
			//------------------------------- 
		case InformcasultyinfromationReport :
			Mybody.CommTrigger= RespondersTriggers.GetOPReport;
			CurrentBZC = (Responder_Police)currentmsg.sender;
			CurrentReport=((Report) currentmsg.content);
			IsCasualtyReport=true;
			break;
		case InformEndcasultyinfromation : 
			Mybody.CommTrigger= RespondersTriggers.GetOPReport;
			CurrentBZC = (Responder_Police)currentmsg.sender;
			IsCasualtyReport=true;
			EndCollectioninformation=true;

			//CurrentReport=((Report) currentmsg.content);			
			break;	
			//------------------------------- 	
		case   InformFirestSituationAssessmentReport:
			Mybody.CommTrigger= RespondersTriggers.GetReport;
			CurrentReport=((Report) currentmsg.content);
			break;
		case InformNewReceivingEOCReport:
			Mybody.CommTrigger= RespondersTriggers.GetReport; //from EOC
			CurrentReport=((Report) currentmsg.content);
			break;

			//=====================================
		case CallForSilverMeeting:
			Mybody.CommTrigger= RespondersTriggers.GetCallForSilverMeeting; //from other
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
	// Create Plan-(list of TActivity) for first time 
	public void Implmenting_plan() {

		// Communication , SecureScene_outerCordons ,CollectInformation 
		// ------------------------------------- Create Activity plan------------------------------------------ 				
		//1-
		Activity_Police A3 = new Activity_Police(Police_ActivityType.Communication ,Police_ResponderRole.PoliceCommunicationsOfficer ,true    ); //  SingleActivity  true , Status.waiting
		Tactical_Plan.add( A3);
		if (TempPIC)	{ A3.BronzCommander=(Responder_Police) Mybody.Myvehicle.AssignedDriver;A3.Tempcommnader=true ; A3.ActivityStart();	}

		//---------------------------------------------
		//2-
		Activity_Police  A2= new Activity_Police(Police_ActivityType.SecureScene_outerCordons,Police_ResponderRole.CordonsCommander,false ) ;
		Tactical_Plan.add( A2);
		A2.inCordon=Mybody.assignedIncident.Cordon_outer;

		//---------------------------------------------
		//3-
		Activity_Police  A4= new Activity_Police(Police_ActivityType.CollectInformation,Police_ResponderRole.ReceptionCenterOfficer,false ) ; 
		Tactical_Plan.add( A4);
		A4.TA=Mybody.assignedIncident.RC ;

	};

	//***************************************************************************************************
	private Activity_Police GetActivity(Police_ActivityType xx)
	{
		Activity_Police result=null;
		for ( Activity_Police A :Tactical_Plan) 
			if ( A.Activity == xx)
				result= A ;		
		return result ;		 
	}

	//***************************************************************************************************	
	// Resource Allocation for Activity
	public Command AssigneRoleandTasktoResponder(  Responder_Police _Responder  , Police_ActivityType SpecificActivity    ) {

		boolean implmnetActivity=false;
		Command CMD1 =new Command();
		MajorActivity_Command=null;

		//System.out.println(Mybody.Id + "Tactical_Plan.size()"+ Tactical_Plan.size() );

		//-------------------------------------- Implementation  allocate BZC
		for ( Activity_Police A :Tactical_Plan) 
		{		
			if ( A.ActivityStatus==GeneralTaskStatus.Inprogress &&  A.Activity==Police_ActivityType.Communication && A.Tempcommnader==true   ) 
			{
				TempPCO= A.BronzCommander;
				InstrcuteTempPCOtoleave=true;				
				A.Tempcommnader=false;

				A.ResourseAllocation_commander(_Responder);				
				CMD1.PoliceCommand( "0" , A.BronzRole   );				
				implmnetActivity=true;break;
			}
			else if ( A.ActivityStatus==GeneralTaskStatus.Waiting && A.BronzCommander==null && A.Activity==Police_ActivityType.SecureScene_outerCordons ) // Commander for setup task 
			{

				A.ResourseAllocation_commander(_Responder);
				CMD1.PoliceCommand( "0" , A.BronzRole  , A.inCordon ,null );
				A.ActivityStart(); // if you cindiser FA you have to delet this
				implmnetActivity=true;break;
			}
			else if ( A.ActivityStatus==GeneralTaskStatus.Waiting && A.BronzCommander==null && A.Activity==Police_ActivityType.CollectInformation     )// Commander for setup task
			{

				A.ResourseAllocation_commander(_Responder);
				CMD1.PoliceCommand( "0" , A.BronzRole ,null, A.TA  );
				A.ActivityStart();				
				implmnetActivity=true; break;
			}	
		}

		//------------------------------------- During response how to deploy responders to activity //equal responders between C information and secure cordon

		if ( !implmnetActivity  &&  SpecificActivity ==null  )   //no awareness about the activity  need
		{
			int MinimumResponders=99;
			Activity_Police  selectedA=null;

			for ( Activity_Police  A : Tactical_Plan ) 		
				if ( A.ActivityStatus==GeneralTaskStatus.Inprogress && ( A.Activity==Police_ActivityType.CollectInformation || A.Activity==Police_ActivityType.SecureScene_outerCordons)  )
				{
					if (A.AllocatedResponderforThisActivity.size()< MinimumResponders   )
					{selectedA=A;MinimumResponders=A.AllocatedResponderforThisActivity.size();}						
				}


			if (selectedA!=null )
			{

				selectedA.ResourseAllocation_Responders(_Responder);	
				CMD1.PoliceCommand( "0" , Police_ResponderRole.Policeman);

				if (selectedA.Activity ==Police_ActivityType.SecureScene_outerCordons )
				{
					MajorActivity_Command=new Command();
					MajorActivity_Command.PoliceCommand( "0" , Police_ActivityType.SecureScene_outerCordons,  selectedA.inCordon ,null  ) ;
				}
				else
				{
					MajorActivity_Command=new Command();
					MajorActivity_Command.PoliceCommand( "0" , Police_ActivityType.CollectInformation,  null ,selectedA.TA  ) ;				
				}
			}
			else
			{
				// need work
				System.out.println( Mybody.Id +" " +   " Police errroooorrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrr  new responders notask in assigned task  "); // if all of them  has max
			}

		}
		//-------------------------------------- 
		else if (!implmnetActivity  && SpecificActivity !=null)
		{

		}

		//-------------------------------------- 
		if ( this.UnoccupiedResponders.size()==0)
		{
			System.out.println("==============================Police==============================") ; 

			for ( Activity_Police A : Tactical_Plan ) 		

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
		Report1.Police_FRSituationAssessment( Mybody.assignedIncident ,ReportSummery.AdditionalResourceRequest , 0  );

		// send message
		Mybody.CurrentMessage  = new  ACL_Message( ACLPerformative.InformFirestSituationAssessmentReport ,Mybody ,Mybody.assignedIncident.PCOcommander ,Report1, Mybody.CurrentTick ,Mybody.assignedIncident.ComMechanism_inControlArea,1,TypeMesg.Inernal) ;
		Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
		//System.out.println( Mybody.assignedIncident.ID +" Police   AdditionalResourceRequest===============================================1========================================================================="  + NEEDfromAESSMENT );


	}

	//***************************************************************************************************	
	//update report 
	public void Reporting_URSituationAssessmentx()
	{
		//some thing

		Report Report1 =new Report();
		Report1.Police_URSituationAssessment( Mybody.assignedIncident ,ReportSummery.AdditionalResourceRequest  );

		// send message
		//Mybody.CurrentMessage  = new  ACL_Message( ACLPerformative.InformSituationReport ,Mybody ,Mybody.assignedIncident.PCOcommander ,Report1, Mybody.CurrentTick ,Mybody.assignedIncident.ComMechanism_inControlArea,1 ) ;
		Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
	}

	//***************************************************************************************************	
	//End Report
	public void Reporting_ENDSituationAssessment()
	{

		// Send Final message to its  ACO  nothings
		Report Report1 =new Report();
		Report1.Police_EndReport(Mybody.assignedIncident ,ReportSummery.EndER_Police  );

		Mybody.CurrentMessage = new  ACL_Message( ACLPerformative.InformERendReport , Mybody,Mybody.assignedIncident.PCOcommander ,Report1, Mybody.CurrentTick , Mybody.assignedIncident.ComMechanism_inControlArea ,1,TypeMesg.Inernal) ;
		Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
	}

	//***************************************************************************************************	
	//Broadcast
	public void BoradcastEndofERAction()
	{
		List<Responder_Police> Listofreceiver = new ArrayList<Responder_Police>();

		for ( Activity_Police  A : Tactical_Plan ) // for Bronze commander only
		{ 
			if ( A.BronzCommander !=Mybody)
			{	Listofreceiver.add(A.BronzCommander); A.ActivityStatus= GeneralTaskStatus.Done ;} ////System.out.println( A.BronzCommander.Id +" xxxxxxxxxListofreceiverxxxxxxx ")
		}  
		//System.out.println( Mybody.Id  +" xxxxxxxxxxxxxxxx ");
		// break; //one by one 

		Mybody.CurrentMessage  = new  ACL_Message( ACLPerformative.InformERend , Mybody,Listofreceiver ,null, Mybody.CurrentTick,CommunicationMechanism.RadioSystem_BoradCast,1,TypeMesg.Inernal ) ;
		Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
	}

	public void BoradcastEndofERAction2()
	{
		Mybody.CurrentMessage  = new  ACL_Message( ACLPerformative.InformERend , Mybody,this.UnoccupiedDrivers,null, Mybody.CurrentTick,CommunicationMechanism.RadioSystem_BoradCast,1,TypeMesg.Inernal ) ;
		Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
	}

	//***************************************************************************************************	
	//SilverMeeting
	public void BoradcastSilverMeetingAction(SliverMeetingRecord SR)
	{
		List<Responder> Listofreceiver = new ArrayList<Responder>();
		Listofreceiver.add(Mybody.assignedIncident.AICcommander ) ;
		Listofreceiver.add(Mybody.assignedIncident.FICcommander ) ;

		// locationSilverMeeting need to think 
		Mybody.CurrentMessage  = new  ACL_Message( ACLPerformative.CallForSilverMeeting, Mybody,Listofreceiver ,SR, Mybody.CurrentTick,CommunicationMechanism.FF_BoradCast,1,TypeMesg.External) ; // if there is sharing  chanle
		Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);

	}

	//##############################################################################################################################################################	
	//													Interpreted Triggers 
	//##############################################################################################################################################################	
	public boolean EndofER()
	{
		boolean done=true;

		if (Tactical_Plan.size()== 0 )
		{done=false;}

		for ( Activity_Police  A : Tactical_Plan ) 
			if (( A.Activity   ==Police_ActivityType.SecureScene_outerCordons  ||  A.Activity   == Police_ActivityType.CollectInformation ) && A.ActivityStatus!=GeneralTaskStatus.Done  )  //exept comunication 
			{done=false; break;}

		//if ( done==true ) System.out.println("p");
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
			NEEDfromAESSMENT=0;


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

		//2
		if ( cordons_outer_established==1  )
			Thereisupdate++;
		if (controlarea_established== 1 )
			Thereisupdate++;
		if (RC_established==1)
			Thereisupdate++;

		//if ( routeClearedtraffic_List.size() >0 )
		//{ Current_SliverMeeting.PIC_Addtomeeting_Route( routeClearedtraffic_List  );routeClearedtraffic_List.clear(); }

		//3
		if (  EndofER()  && EndER_Police==-1 && Tactical_Plan.size()!= 0 )
			Thereisupdate++;
		//-------------------

		if (Thereisupdate>0 )
			return true;
		else
			return false;
	}
	//----------------------------------------------------------------------------------------------------
	public void  SliverMeetingADD()
	{
		int Thereisupdate =0;

		boolean _controlarea_established=false ,  _cordons_outer_established=false  , _RC_established=false ;

		//set update in Meeting record 
		//1
		if (SliverMeetings.size()==1)
		{
			Current_SliverMeeting.AIC_Addtomeeting_GeolocationTA(); 
			if ( SurveyScene )Current_SliverMeeting.SectorPlanning(Mybody.assignedIncident.Survey_scene_Mechanism_IN, (ArrayList<Casualty>) Mybody.CasualtySeen_list); 
			Thereisupdate++;;
		}
		//2
		if ( cordons_outer_established==1  )
		{cordons_outer_established=2;_cordons_outer_established=true ;Thereisupdate++;}
		if (controlarea_established== 1 )
		{controlarea_established= 2;_controlarea_established=true ;Thereisupdate++;}
		if (RC_established==1)
		{RC_established=2 ;_RC_established=true;Thereisupdate++; }

		if ( _controlarea_established ||  _cordons_outer_established || _RC_established )
		{  Current_SliverMeeting.PIC_Addtomeeting_EstablishedTA(  _controlarea_established , _cordons_outer_established  ,  _RC_established);}

		//if ( routeClearedtraffic_List.size() >0 )
		//{ Current_SliverMeeting.PIC_Addtomeeting_Route( routeClearedtraffic_List  );routeClearedtraffic_List.clear(); }

		//3
		if (  EndofER()  && EndER_Police==-1 && Tactical_Plan.size()!= 0 )
		{Current_SliverMeeting.PIC_Addtomeeting_EndER_Police();EndER_Police=2;Thereisupdate++;  DoneLastMeeting =true ;}	


		//---------------------

		Current_SliverMeeting.Thereisupdate_Po=Thereisupdate++; ;


	}

	//----------------------------------------------------------------------------------------------------
	public void  SliverMeetingGET()
	{
		//0-
		if ( SliverMeetings.size()==1)
		{
			//ScheduleParameters params = ScheduleParameters.createRepeating(Mybody.CurrentTick + 1 , 1,ScheduleParameters.FIRST_PRIORITY);
							
			Mybody.assignedIncident.CreateCordon() ;			
			System.out.println("PRIORITY_execution =>  "+ InputFile.PRIORITY_execution +"   "  +"cordon" );
			ScheduleParameters params1 = ScheduleParameters.createRepeating(Mybody.schedule.getTickCount() + 1 , 1,InputFile.PRIORITY_execution ++ );
			Mybody.schedule.schedule(params1,Mybody.assignedIncident.Cordon_outer,"step");
			
			Mybody.assignedIncident.CreateTA_RCandControlArea(10 ) ;		
			System.out.println("PRIORITY_execution =>  "+ InputFile.PRIORITY_execution +"   "  +"RC" );
			ScheduleParameters params2 = ScheduleParameters.createRepeating(Mybody.schedule.getTickCount() + 1 , 1,InputFile.PRIORITY_execution ++ );	
			Mybody.schedule.schedule(params2,Mybody.assignedIncident.RC,"step");
			
			
			DoneFirstMeeting=true;


			Mybody.UpdateComNet(Mybody.assignedIncident.FICcommander);  //Update No of messages
			Mybody.UpdateComNet(Mybody.assignedIncident.AICcommander);  //Update No of messages
			
			Mybody.CurrentMessage  = new  ACL_Message( ACLPerformative.CallForSilverMeeting, Mybody.assignedIncident.FICcommander,Mybody ,null, Mybody.CurrentTick,CommunicationMechanism.SilverMeeting,2,TypeMesg.External ) ;
			Mybody.Message_inboxMeeting.add(Mybody.CurrentMessage );  
			Mybody.CurrentMessage  = new  ACL_Message( ACLPerformative.CallForSilverMeeting, Mybody.assignedIncident.AICcommander,Mybody ,null, Mybody.CurrentTick,CommunicationMechanism.SilverMeeting,2,TypeMesg.External ) ;
			Mybody.Message_inboxMeeting.add(Mybody.CurrentMessage ); 

		}	

		//1-
		if (   Current_SliverMeeting.PIC_Getmeeting_TA_Sector(   ) ) 
		{
			TA_Sector_established=1 ;
			Mybody.UpdateComNet(Mybody.assignedIncident.FICcommander);  //Update No of messages
			Mybody.CurrentMessage  = new  ACL_Message( ACLPerformative.CallForSilverMeeting, Mybody.assignedIncident.FICcommander,Mybody ,null, Mybody.CurrentTick,CommunicationMechanism.SilverMeeting,1,TypeMesg.External ) ;
			Mybody.Message_inboxMeeting.add(Mybody.CurrentMessage ); 
		}

		//1-
		if ( Current_SliverMeeting.PIC_Getmeeting_Route(   ).size() >0 )
		{
			for( RoadLink RL :Current_SliverMeeting.PIC_Getmeeting_Route(   ) )
				routeNEEDCleartraffic_List.add(RL);

			Mybody.UpdateComNet(Mybody.assignedIncident.AICcommander);  //Update No of messages
			Mybody.CurrentMessage  = new  ACL_Message( ACLPerformative.CallForSilverMeeting, Mybody.assignedIncident.AICcommander,Mybody ,null, Mybody.CurrentTick,CommunicationMechanism.SilverMeeting,1 ,TypeMesg.External) ;
			Mybody.Message_inboxMeeting.add(Mybody.CurrentMessage ); 
		}
		//2
		if ( Current_SliverMeeting.PIC_Getmeeting_CasualtyDecasedCCS().size() >0 )
		{
			for( Casualty ca :Current_SliverMeeting.PIC_Getmeeting_CasualtyDecasedCCS() )
				Dead_List_CCS.add(ca);
			Mybody.UpdateComNet(Mybody.assignedIncident.AICcommander);  //Update No of messages
			Mybody.CurrentMessage  = new  ACL_Message( ACLPerformative.CallForSilverMeeting, Mybody.assignedIncident.AICcommander,Mybody ,null, Mybody.CurrentTick,CommunicationMechanism.SilverMeeting,Dead_List_CCS.size(),TypeMesg.External) ;
			Mybody.Message_inboxMeeting.add(Mybody.CurrentMessage ); 
		}
		//3
		if ( Current_SliverMeeting.PIC_Getmeeting_CasualtyDecasedinner().size() >0 )
		{
			for( Casualty ca :Current_SliverMeeting.PIC_Getmeeting_CasualtyDecasedinner() )
				Dead_List_inner.add(ca);
			Mybody.UpdateComNet(Mybody.assignedIncident.FICcommander);  //Update No of messages
			Mybody.CurrentMessage  = new  ACL_Message( ACLPerformative.CallForSilverMeeting, Mybody.assignedIncident.FICcommander,Mybody ,null, Mybody.CurrentTick,CommunicationMechanism.SilverMeeting,Dead_List_inner.size(),TypeMesg.External ) ;
			Mybody.Message_inboxMeeting.add(Mybody.CurrentMessage ); 
		}
		//4
		//5
		if ( Current_SliverMeeting.PIC_Getmeeting_Cainfor_evacuation_List().size() >0 )
		{
			for( Casualty ca :Current_SliverMeeting.PIC_Getmeeting_Cainfor_evacuation_List() )
				Cainfor_evacuation_List.add(ca);
			Mybody.UpdateComNet(Mybody.assignedIncident.AICcommander);  //Update No of messages
			Mybody.CurrentMessage  = new  ACL_Message( ACLPerformative.CallForSilverMeeting, Mybody.assignedIncident.AICcommander,Mybody ,null, Mybody.CurrentTick,CommunicationMechanism.SilverMeeting ,Cainfor_evacuation_List.size(),TypeMesg.External) ;
			Mybody.Message_inboxMeeting.add(Mybody.CurrentMessage );  
		}
		//6
		if ( Current_SliverMeeting.PIC_Getmeeting_NomorecasultyinInner()  )
		{
			NomorecasultyinInner=1 ;
			Mybody.UpdateComNet(Mybody.assignedIncident.FICcommander);  //Update No of messages
			Mybody.CurrentMessage  = new  ACL_Message( ACLPerformative.CallForSilverMeeting, Mybody.assignedIncident.FICcommander,Mybody ,null, Mybody.CurrentTick,CommunicationMechanism.SilverMeeting,1,TypeMesg.External ) ;
			Mybody.Message_inboxMeeting.add(Mybody.CurrentMessage ); 
		}
		//7
		if ( Current_SliverMeeting.PIC_Getmeeting_NomorecasultyinCCS()  )
		{
			NomorecasultyinCCS=1 ;
			Mybody.UpdateComNet(Mybody.assignedIncident.AICcommander);  //Update No of messages
			Mybody.CurrentMessage  = new  ACL_Message( ACLPerformative.CallForSilverMeeting, Mybody.assignedIncident.AICcommander,Mybody ,null, Mybody.CurrentTick,CommunicationMechanism.SilverMeeting,1 ,TypeMesg.External) ;
			Mybody.Message_inboxMeeting.add(Mybody.CurrentMessage ); 
		}
		//8
		if ( Current_SliverMeeting.PIC_Getmeeting_NomorecasultyinLA() )
		{
			NomorecasultyinLA=1 ;
			Mybody.UpdateComNet(Mybody.assignedIncident.AICcommander);  //Update No of messages
			Mybody.CurrentMessage  = new  ACL_Message( ACLPerformative.CallForSilverMeeting, Mybody.assignedIncident.AICcommander,Mybody ,null, Mybody.CurrentTick,CommunicationMechanism.SilverMeeting,1,TypeMesg.External ) ;
			Mybody.Message_inboxMeeting.add(Mybody.CurrentMessage ); 
		}
		//9
		if ( Current_SliverMeeting.PIC_Getmeeting_EndER_Ambulance())
		{
			EndER_Ambulance= 2;
			Mybody.UpdateComNet(Mybody.assignedIncident.AICcommander);  //Update No of messages
			Mybody.CurrentMessage  = new  ACL_Message( ACLPerformative.CallForSilverMeeting, Mybody.assignedIncident.AICcommander,Mybody ,null, Mybody.CurrentTick,CommunicationMechanism.SilverMeeting,1,TypeMesg.External ) ;
			Mybody.Message_inboxMeeting.add(Mybody.CurrentMessage ); 
		}
		//10
		if ( Current_SliverMeeting.PIC_Getmeeting_EndER_Fire())
		{
			EndER_Fire= 2 ; 
			Mybody.UpdateComNet(Mybody.assignedIncident.FICcommander);  //Update No of messages
			Mybody.CurrentMessage  = new  ACL_Message( ACLPerformative.CallForSilverMeeting, Mybody.assignedIncident.FICcommander,Mybody ,null, Mybody.CurrentTick,CommunicationMechanism.SilverMeeting,1,TypeMesg.External ) ;
			Mybody.Message_inboxMeeting.add(Mybody.CurrentMessage ); 
		}
	}
	//______________________________________________________________________________________________________________________________________________________________
	//##############################################################################################################################################################
	//                                                        Behavior
	//##############################################################################################################################################################
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	// PIC commander - General 
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	public void PICBehavior()	
	{		
		//*************************************************************************
		// 1-FirstArrival response
		if( Mybody.SensingeEnvironmentTrigger==RespondersTriggers.FirstArrival_NoResponder) 				
		{
			Mybody.CurrentAssignedActivity=Police_TaskType.CoordinateFirstArrival;		

			NeedFirstMeeting= true;
			DoneFirstMeeting=false;
			DoneLastMeeting=false;

//			if (Mybody.assignedIncident.FICcommander !=null && Mybody.assignedIncident.AICcommander !=null   )
//			{
//				Mybody.CurrentAssignedActivity=Police_TaskType.CoordinateER ;
//				Mybody.SensingeEnvironmentTrigger=null ;
//				Mybody.InterpretedTrigger= RespondersTriggers.TempPICofFirstArrival ;
//			}

		}
		// 2-CoordinateER
		else if ( Mybody.InterpretedTrigger== RespondersTriggers.TempPICofFirstArrival|| Mybody.CommTrigger==RespondersTriggers.PICRolehanded 	) 
		{
			Mybody.CurrentAssignedActivity=Police_TaskType.CoordinateER ;
		}
		// 3-Handover
		else if (Mybody.Action==RespondersActions.Noaction &&  Mybody.CommTrigger== RespondersTriggers.GetinstructiontoHandover 	) 
		{
			Mybody.CurrentAssignedActivity=Police_TaskType.HandovertoPIC;
		}
		// 4-Ending
		else if ( Mybody.InterpretedTrigger==RespondersTriggers.DoneEmergencyResponse  ) 
		{
			Mybody.CurrentAssignedActivity=Police_TaskType.CoordinateEndResponse;
		}	
		else  if (Mybody.InterpretedTrigger==RespondersTriggers.ENDER  || Mybody.InterpretedTrigger==RespondersTriggers.PICRolehanded ) 
		{
			Mybody.PrvRoleinprint3=Mybody.Role ;
			Mybody.Role=Police_ResponderRole.None;
		}

		//*************************************************************************
		switch(Mybody.CurrentAssignedActivity) {
		case CoordinateFirstArrival:  //Temp PIC
			CommanderBehavior_CoordinateFirstArrival();	
			break;
		case	HandovertoPIC : //Temp PIC
			CommanderBehavior_HandovertoPIC();	
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
	// Temp PIC - Coordinate FirstArrival
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^	
	private void CommanderBehavior_CoordinateFirstArrival()
	{
		// ++++++ 1- +++++++
		if( Mybody.SensingeEnvironmentTrigger==RespondersTriggers.FirstArrival_NoResponder )
		{						
			if ( ( Mybody.assignedIncident.AICcommander !=null || Mybody.assignedIncident.FICcommander !=null )   )
			{
				Mybody.ActivityAcceptCommunication=true;
				Mybody.SensingeEnvironmentTrigger=null;

				Mybody.InterpretedTrigger= RespondersTriggers.TempPICofFirstArrival;
				Mybody.Action=RespondersActions.Noaction;
				Mybody.BehaviourType3=RespondersBehaviourTypes3.WaitingDuring ;

				System.out.println(Mybody.Id + " AssessSituation Done1"  );
			}

			else
			{


				Mybody.Action=RespondersActions.Observeandcount;
				Mybody.BehaviourType3=RespondersBehaviourTypes3.Planformulation ; 			
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
			Mybody.BehaviourType3=RespondersBehaviourTypes3.Comunication;  
			Mybody.SensingeEnvironmentTrigger=null;	

			//System.out.println(Mybody.Id + " AssessSituation Done1"  );
		}

		// ++++++ 4- +++++++
		else if (Mybody.Action==RespondersActions.ReportAssessSituation && Mybody.Acknowledged)
		{		
			Mybody.ActivityAcceptCommunication=true;
			Mybody.Acknowledged=false;Mybody.Sending=false; 	
			Mybody.InterpretedTrigger= RespondersTriggers.TempPICofFirstArrival;

			Mybody.Action=RespondersActions.Noaction;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.WaitingDuring ;

			System.out.println(Mybody.Id + " AssessSituation Done2"  );

			//Silver Meeting
			NeedFirstMeeting= true;
			DoneFirstMeeting=false;

		}

	}

	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	// Temp PIC - HandovertoPIC
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^	
	private void  CommanderBehavior_HandovertoPIC()  //temp
	{
		// ++++++ 1- +++++++
		if( Mybody.CommTrigger== RespondersTriggers.GetinstructiontoHandover 	 )
		{						
			//her yiu have more work
			Mybody.Action=RespondersActions.GetcammandtoHandover ;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.RoleAssignment;
			Mybody.EndofCurrentAction=  InputFile.GetCommand_duration  ; 
			Mybody.CommTrigger=null;
		}
		// ++++++ 2- +++++++
		else if ( Mybody.Action==RespondersActions.GetcammandtoHandover  && Mybody.EndActionTrigger== RespondersTriggers.EndingAction 	) 			 	
		{							
			//Actually there is no report send but it will be done through Commander_AIC_HandoverAction 
			Mybody.CurrentMessage = new  ACL_Message( ACLPerformative.InformHandoverReport , Mybody, Mybody.CurrentSender , null, Mybody.CurrentTick ,Mybody.assignedIncident.ComMechanism_inControlArea,1,TypeMesg.Inernal ) ;
			Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);

			Mybody.Action=RespondersActions.HandovertoPIC;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.RoleAssignment;
			Mybody.EndActionTrigger=null;						
		}
		// ++++++ 3- +++++++
		else if ( Mybody.Action==RespondersActions.HandovertoPIC  &&     Mybody.Acknowledged 	) 			 	
		{		
			Mybody.InterpretedTrigger=RespondersTriggers.PICRolehanded ; //then he will get new commnd role

			Mybody.Role=Police_ResponderRole.None;				
			Mybody.Action=RespondersActions.Noaction;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.WaitingDuring ;
			Mybody.Acknowledged=false;Mybody.Sending=false; 	
		}
	}

	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	// PIC - Coordinate ER 
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	private void CommanderBehavior_CoordinateER()
	{								
		// ++++++ 1- +++++++
		if ( ( Mybody.CommTrigger==RespondersTriggers.PICRolehanded ||  Mybody.InterpretedTrigger== RespondersTriggers.TempPICofFirstArrival ) && Tactical_Plan.size()== 0 &&  ! NeedFirstMeeting  )   	
		{
			// Implmenting_plan(); diff version
			Mybody.Action=RespondersActions.FormulatePlan;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.Planformulation;  	
			Mybody.EndofCurrentAction=  InputFile.FormulatePlan_duration ; 

			if ( Mybody.InterpretedTrigger== RespondersTriggers.TempPICofFirstArrival)Mybody.InterpretedTrigger=null; 
			if ( Mybody.CommTrigger==RespondersTriggers.PICRolehanded  )Mybody.CommTrigger=null;
		}
		// ++++++ 2- +++++++
		else	if ( Mybody.Action==RespondersActions.Noaction  && Tactical_Plan.size()== 0 &&   NeedFirstMeeting && DoneFirstMeeting  )   	
		{
			Implmenting_plan();

			Mybody.Action=RespondersActions.FormulatePlan;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.Planformulation  ;  
			Mybody.EndofCurrentAction=  InputFile.FormulatePlan_duration ; 

			if ( Mybody.InterpretedTrigger== RespondersTriggers.TempPICofFirstArrival)Mybody.InterpretedTrigger=null; 
			if ( Mybody.CommTrigger==RespondersTriggers.PICRolehanded  )Mybody.CommTrigger=null;
		}
		// ++++++ 3- +++++++
		else if ( Mybody.Action==RespondersActions.FormulatePlan  && Mybody.EndActionTrigger== RespondersTriggers.EndingAction 	) 			 	
		{							
			Mybody.Action=RespondersActions.Noaction;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.WaitingDuring;
			Mybody.EndActionTrigger=null;	
			//System.out.println(Mybody.Id + " Waiting to command " );
		}

		//-------------------------------------------------------------------------------------------- (P1-called ) Silver Meeting  
		// ++++++ 4- +++++++
		else if (Mybody.Action==RespondersActions.Noaction  && Mybody.CommTrigger== RespondersTriggers.GetCallForSilverMeeting    )
		{
			// go to location need think
			Current_SliverMeeting.PIC_Attendance(Mybody); 
			SliverMeetings.add(Current_SliverMeeting);

			Mybody.Action=RespondersActions.GetCallForSilverMeeting;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.ComunicationDelay;
			Mybody.CommTrigger=null;
			
			Mybody.ActivityAcceptCommunication=false;
			Mybody.SendingReciving_internal= false; 
			Mybody.SendingReciving_External= true ; 
		}

		// ++++++ 6- +++++++
		else if (( Mybody.Action==RespondersActions.CallForSilverMeeting ||Mybody.Action==RespondersActions.GetCallForSilverMeeting)  && Mybody.InterpretedTrigger == RespondersTriggers.SliverMeetingStart  )
		{
			
			SliverMeetingADD();

			Mybody.Action=RespondersActions.Meeting ;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.Comunication;  
			Mybody.InterpretedTrigger=null;

			System.out.println("                                                                                                                                 " + "SM=======================" +Mybody.Id + " PIC in sliver meeting" );
		}
		// ++++++ 7- +++++++
		else if (Mybody.Action==RespondersActions.Meeting  && Mybody.InterpretedTrigger == RespondersTriggers.SliverMeetingEnd  )
		{


			Mybody.ActivityAcceptCommunication=true;			
			SliverMeetingGET(); //Get update from Meeting record
			Current_SliverMeeting=null;

			Mybody.Action=RespondersActions.UpdatePlan;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.Planformulation  ; 
			Mybody.EndofCurrentAction= InputFile.UdatePlan_duration ; 
			Mybody.InterpretedTrigger=null; 
			Time_last_SliverMeeting=Mybody.CurrentTick ;
			
			Mybody.SendingReciving_internal= false; 
			Mybody.SendingReciving_External= false ; 
			System.out.println("                                                                                                                                 " + "SM=======================" +Mybody.Id + " PIC end sliver meeting" );
		}
		//-------------------------------------------------------------------------------------------- (P1-need  or police) Silver Meeting  call
		// ++++++ 4- +++++++
		else  if ( Mybody.Action==RespondersActions.Noaction && Mybody.CommTrigger==null &&( Mybody.assignedIncident.FICcommander !=null && Mybody.assignedIncident.AICcommander !=null )  &&    
				(  
						(Mybody.InterpretedTrigger== RespondersTriggers.TempPICofFirstArrival  && NeedFirstMeeting && !DoneFirstMeeting) ||   					
						( Mybody.assignedIncident.Inter_Communication_Time_used==Communication_Time.Every_frequently &&  Mybody.assignedIncident.Communication_Structure_uesd==Inter_Communication_Structure.Struc1_SilverCommandersInteraction  &&   Mybody.CurrentTick >= Time_last_SliverMeeting + Mybody.assignedIncident.SMEvery   ) ||
						( Mybody.assignedIncident.Inter_Communication_Time_used==Communication_Time.When_need && Mybody.assignedIncident.Communication_Structure_uesd==Inter_Communication_Structure.Struc1_SilverCommandersInteraction  && ISthereupdateTosend( )  ) ||
						(    EndofER()  && EndER_Police==-1 && Tactical_Plan.size()!= 0  && !DoneLastMeeting &&  Mybody.assignedIncident.Communication_Structure_uesd==Inter_Communication_Structure.Struc2_BronzeCommandersInteraction ) 
						)
				) //m call  then regular call
		{			
			System.out.println("                                                                                                                                 " + "SM=======================" + Mybody.Id + " BoradcastcallForsliverMeeting  " + Meetingcounter + " " + Mybody.CurrentTick  );
			Meetingcounter++ ;

			// current location or control area 
			PointDestination locationSilverMeeting=null ; 

			if (Mybody.assignedIncident.ControlArea !=null && Mybody.assignedIncident.ControlArea.installed)
				locationSilverMeeting=Mybody.assignedIncident.ControlArea.Location ;
			else
				locationSilverMeeting=Mybody.assignedIncident.bluelightflashing_Point ;

			Current_SliverMeeting = new SliverMeetingRecord ("0",locationSilverMeeting , Mybody.assignedIncident) ;	
			if (!DoneFirstMeeting  ) Current_SliverMeeting.FirestM=true;
			

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

			if ( Mybody.InterpretedTrigger== RespondersTriggers.TempPICofFirstArrival)Mybody.InterpretedTrigger=null; 

		}
		// ++++++ 5- +++++++
		else if (Mybody.Action==RespondersActions.BoradcastcallForsliverMeeting && Mybody.EndActionTrigger== RespondersTriggers.EndingAction  )
		{
			Current_SliverMeeting.PIC_Attendance(Mybody  );  
			Mybody.Action=RespondersActions.CallForSilverMeeting ;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.ComunicationDelay;
			Mybody.EndActionTrigger=null;
			Mybody.Sending=false; 
		}


		//-------------------------------------------------------------------------------------------- (P1)
		//==========================New Responder==========================
		// ++++++ 8- +++++++
		else if (Mybody.Action==RespondersActions.Noaction  && ( Mybody.CommTrigger == RespondersTriggers.GetNewResponderarrived ||  Mybody.CommTrigger == RespondersTriggers. GetbackResponderarrived) )		  										
		{
			//UpdatePlan
			if (  Mybody.CommTrigger == RespondersTriggers.GetNewResponderarrived )
			{
				MyResponder_Police.add(NewResponder) ;
				if (NewResponder.Role ==Police_ResponderRole.None) //not driver
					UnoccupiedResponders.add(NewResponder);
				else	
					UnoccupiedDrivers.add(NewResponder);  
			}
			else
				UnoccupiedResponders.add(BackResponder);

			Mybody.Action=RespondersActions.GetNewArrivalNotification ;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.Comunication;   
			Mybody.EndofCurrentAction=  InputFile.GetNotification_duration  ; 
			Mybody.CommTrigger=null; //reciving=true;
			//System.out.println(Mybody.Id + " GetNewResponderarrived1" );
		}
		// ++++++ 9- +++++++
		else if ( Mybody.Action==RespondersActions.GetNewArrivalNotification  && Mybody.EndActionTrigger== RespondersTriggers.EndingAction 	) 			 	
		{				
			NewResponder.Acknowledg(Mybody);  //reciving=false; 	//Always F-F

			Mybody.Action=RespondersActions.UpdatePlan;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.Planformulation;   
			Mybody.EndofCurrentAction=   InputFile.UdatePlan_duration  ; 
			Mybody.EndActionTrigger=null; 

			//System.out.println(Mybody.Id + " GetNewResponderarrived2" );
		}
		//==========================Get OP Report from BZC ========================================= /OP report  or arrival location 
		// ++++++ 12- +++++++
		else if (Mybody.Action==RespondersActions.Noaction &&  (Mybody.CommTrigger== RespondersTriggers.GetOPReport|| Mybody.CommTrigger== RespondersTriggers.GetBronzeCommanderstart ||Mybody.CommTrigger== RespondersTriggers.ResultofSetupTA)    )		  										
		{	 	
			int Total=1;
			//1 -F A
			if (  Mybody.CommTrigger== RespondersTriggers.GetBronzeCommanderstart)
			{
				Total++; //Start activity
				
				for ( Activity_Police  A : Tactical_Plan ) 		
					if ( A.Activity== Police_ActivityType.SecureScene_outerCordons && A.BronzCommander == this.CurrentBZC )  
						this.startCC=1;
					else if ( A.Activity== Police_ActivityType.CollectInformation&& A.BronzCommander == this.CurrentBZC ) 
						this.sartCInfo=1;

			}
			// 2- Setup
			if (  Mybody.CommTrigger== RespondersTriggers.ResultofSetupTA )
			{
				if ( cordons_outer_established==0  )
					cordons_outer_established=1;

				else if (RC_established==0)
					RC_established=1 ;

				//else if (controlarea_established== 0 )
				//controlarea_established= 1;

				Total++;
			}
			// 2- Route
			if (  CurrentRouteListbySender!=null &&  CurrentRouteListbySender.size() >0  )
			{
				for( RoadLink RL :CurrentRouteListbySender )
				{routeClearedtraffic_List.add(RL);Total++;}
				CurrentRouteListbySender.clear();
			}

			//4- End casualty							
			if (EndCollectioninformation )
			{	for ( Activity_Police  A : Tactical_Plan ) 
				if ( A.BronzCommander == CurrentBZC  && A.Activity==Police_ActivityType.CollectInformation )
				{ A.ActivityStatus=GeneralTaskStatus.Done ;  EndCollectioninformation=false ;  break;}
			Total++;
			}

			//5- End Clear Route Traffic			
			if (EndClearRouteTraffic )
			{	for ( Activity_Police  A : Tactical_Plan ) 
				if ( A.BronzCommander == CurrentBZC  && A.Activity==Police_ActivityType.SecureScene_outerCordons )
				{ A.ActivityStatus=GeneralTaskStatus.Done ;  EndClearRouteTraffic=false;   break;}
			Total++;
			}

			Mybody.Action=RespondersActions.GetOPReport;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.Comunication; 
			Mybody.EndofCurrentAction= InputFile.GetInfromation_FtoForR_Duration_Data * Total ;
			Mybody.CommTrigger=null; //reciving=true;
		}
		// ++++++ 13- +++++++
		else if ( Mybody.Action==RespondersActions.GetOPReport  && Mybody.EndActionTrigger== RespondersTriggers.EndingAction 	) 			 	
		{
			CurrentBZC.Acknowledg(Mybody);	//reciving=false; 

			Mybody.Action=RespondersActions.UpdatePlan;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.Planformulation; 
			Mybody.EndofCurrentAction= InputFile.UdatePlan_duration ; 
			Mybody.EndActionTrigger=null; 
			System.out.println("                                                                                    " +"PIC: " + Mybody.Id +" GetOPReport from " +CurrentBZC.Role );

		}
		//============================================================ ALL
		// ++++++ 14- +++++++
		else if (Mybody.Action==RespondersActions.UpdatePlan && Mybody.EndActionTrigger== RespondersTriggers.EndingAction  )
		{						
			Mybody.Action=RespondersActions.Noaction;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.WaitingDuring;
			Mybody.EndActionTrigger=null;
		}
		//-------------------------------------------------------------------------------------------- (P2) EOC update
		// ++++++ 15- +++++++
		else  if (Mybody.Action==RespondersActions.Noaction && (Mybody.CurrentTick >= Time_last_updated + InputFile.UpdatEOCEvery) && false ) 
		{
			//Reporting_URSituationAssessment();
			Mybody.Action=RespondersActions.InfromReport;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.Comunication;  

		}
		// ++++++ 16- +++++++
		else if (Mybody.Action==RespondersActions.InfromReport && Mybody. Acknowledged )
		{
			Mybody.Action=RespondersActions.Noaction;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.WaitingDuring;
			Mybody.Acknowledged=false;Mybody.Sending=false; 
			Time_last_updated=Mybody.CurrentTick ;
			System.out.println("                                                                                    " +"PIC: "  + Mybody.Id + "  Infrom EOC Report;");	
		}

		//-------------------------------------------------------------------------------------------- (P4)
		// ++++++ 17- +++++++
		else if ( Mybody.Action==RespondersActions.Noaction  && UnoccupiedResponders.size()>0 && Tactical_Plan.size()!= 0	) 	 		 	
		{				
			CurrentResponder= UnoccupiedResponders.get(0);
			UnoccupiedResponders.remove(0);

			Allocation_Command = AssigneRoleandTasktoResponder(CurrentResponder , null);
			if ( CurrentResponder.Role == Police_ResponderRole.None ) 
				IsNewResponder= true ;
			else
				IsNewResponder= false ;

			Mybody.Action=RespondersActions.AllocatResponders;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.Planformulation; 
			Mybody.EndofCurrentAction=   InputFile.Allocation_duration ;
		}
		// ++++++ 18- +++++++
		else if (Mybody.Action==RespondersActions.AllocatResponders && Mybody.EndActionTrigger== RespondersTriggers.EndingAction  ) 
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
			Mybody.BehaviourType3=RespondersBehaviourTypes3.Comunication; //RoleAssignment;  
			Mybody.EndActionTrigger=null;						
		}
		// ++++++ 19- +++++++
		else if (Mybody.Action==RespondersActions.CommandAsignRole && Mybody.Acknowledged )
		{
			Mybody.Acknowledged=false;Mybody.Sending=false; 

			if (! InstrcuteTempPCOtoleave)
			{
				Mybody.Action=RespondersActions.Noaction;
				Mybody.BehaviourType3=RespondersBehaviourTypes3.WaitingDuring;

				if (  MajorActivity_Command !=null  && IsNewResponder )
				{
					Mybody.CurrentMessage = new  ACL_Message( ACLPerformative.Command , Mybody, CurrentResponder ,MajorActivity_Command , Mybody.CurrentTick,Mybody.assignedIncident.ComMechanism_inControlArea,1,TypeMesg.Inernal) ;
					Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
					MajorActivity_Command=null ;

					Mybody.Action=RespondersActions.CommandAsignRole;
					Mybody.BehaviourType3=RespondersBehaviourTypes3.Comunication; //RoleAssignment;   
					//System.out.println(Mybody.Id + " MA" );
				}
			}			
			else
			{
				// send message with command to driver to leave if there role PCO
				Mybody.CurrentMessage = new  ACL_Message( ACLPerformative.Instructiontoleave ,Mybody, TempPCO ,null, Mybody.CurrentTick,Mybody.assignedIncident.ComMechanism_inControlArea ,1,TypeMesg.Inernal) ;
				Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);

				Mybody.Action=RespondersActions.CommandAsignRole;
				Mybody.BehaviourType3=RespondersBehaviourTypes3.Comunication; //RoleAssignment; 

				InstrcuteTempPCOtoleave=false;
				UnoccupiedDrivers.add(TempPCO);  // not used
				this.MyResponder_Police.add(TempPCO);  // not used
			}			
		}
		//-------------------------------------------------------------------------------------------- (P5) Request or updates to bronze commanders
		//==========================Request for N responders==============================
		// ++++++ 20- +++++++		
		//		else if ( Mybody.Action==RespondersActions.Noaction  && NPolicemen > 0  ) 	 	 	
		//		{						
		//			//NPolicemen= ??
		//			Mybody.CurrentMessage = new  ACL_Message( ACLPerformative.RequsteReallocateNResponders, Mybody, Mybody.assignedIncident.RCOcommander ,NPolicemen, Mybody.CurrentTick,Mybody.assignedIncident.ComMechanism_level_StoB) ;
		//			Mybody.CurrentMessage1.add(Mybody.CurrentMessage);
		//			NPolicemen=0;
		//
		//			Mybody.Action=RespondersActions.RequstandupdatesinOP;
		//			Mybody.BehaviourType3=RespondersBehaviourTypes3.Comunication ; //Planexecution;
		//
		//
		//		}

		//==========================Update about  sector established   ==============================  //from FIC	
		// ++++++ 30- +++++++		
		else if ( Mybody.Action==RespondersActions.Noaction  && 	this.sartCInfo==1  && (TA_Sector_established==1 && this.RC_established>=1 )  && Mybody.assignedIncident.Communication_Structure_uesd==Inter_Communication_Structure.Struc1_SilverCommandersInteraction ) 	 	 	
		{													

			Mybody.CurrentMessage = new  ACL_Message( ACLPerformative.InfromSafetyBriefandSectorEstablished , Mybody, Mybody.assignedIncident.RCOcommander,null, Mybody.CurrentTick,Mybody.assignedIncident.ComMechanism_level_StoB,1,TypeMesg.External) ;					
			Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);

			TA_Sector_established=2 ;

			Mybody.Action=RespondersActions.RequstandupdatesinOP;  
			Mybody.BehaviourType3=RespondersBehaviourTypes3.Comunication; 
			//System.out.println("  "+ "AIC: " +Mybody.Id  +"    updateCCO Nomorecasulty  ");
		}	

		//==========================Request for clear routes ==============================
		// ++++++ 21- +++++++		
		else if ( Mybody.Action==RespondersActions.Noaction &&  startCC==1  && (routeNEEDCleartraffic_List.size()>0 && this.cordons_outer_established>=1)   && Mybody.assignedIncident.Communication_Structure_uesd==Inter_Communication_Structure.Struc1_SilverCommandersInteraction) 	 	 	
		{						

			Command CMD1 =new Command();
			CMD1.PoliceCommand( "0" , Police_TaskType.ClearRouteTraffic , routeNEEDCleartraffic_List ); // from PIC to CC
			Mybody.CurrentMessage = new  ACL_Message( ACLPerformative.Requste, Mybody, Mybody.assignedIncident.O_CCcommander,CMD1, Mybody.CurrentTick,Mybody.assignedIncident.ComMechanism_level_StoB,routeNEEDCleartraffic_List.size(),TypeMesg.External) ;
			Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
			routeNEEDCleartraffic_List.clear();

			Mybody.Action=RespondersActions.RequstandupdatesinOP;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.Comunication ; 


			System.out.println("                                                                                    " +"PIC: "  + Mybody.Id + "  routeNEEDCleartraffic_List");	
		}
		//==========================information of injured_casulties  ==============================
		// ++++++ 24- +++++++  
		else if ( Mybody.Action==RespondersActions.Noaction && this.sartCInfo==1  && (Cainfor_evacuation_List.size() >0&& this.RC_established>=1 )  && Mybody.assignedIncident.Communication_Structure_uesd==Inter_Communication_Structure.Struc1_SilverCommandersInteraction ) //&& Mybody.assignedIncident.RadioSystem_CheckChanleFree(3,Mybody)
		{
			Command CMD1 =new Command();
			CMD1.PoliceCommand( "0" , Police_TaskType.collectInjuriedEvacuetedCasualty ,Cainfor_evacuation_List , false  ) ;// from PIC to RCO
			Mybody.CurrentMessage = new  ACL_Message( ACLPerformative.Requste, Mybody, Mybody.assignedIncident.RCOcommander ,CMD1, Mybody.CurrentTick,Mybody.assignedIncident.ComMechanism_level_StoB,Cainfor_evacuation_List.size(),TypeMesg.External) ;
			Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
			Cainfor_evacuation_List.clear();

			Mybody.Action=RespondersActions.RequstandupdatesinOP;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.Comunication ; 

			System.out.println("                                                                                    " +"PIC: "  + Mybody.Id + "   injured_List ");


		}
		//==========================Request for Deceased  inner==============================
		// ++++++ 22- +++++++
		else if ( Mybody.Action==RespondersActions.Noaction && this.sartCInfo==1  && ( Dead_List_inner.size() >0 && this.RC_established>=1 ) && Mybody.assignedIncident.Communication_Structure_uesd==Inter_Communication_Structure.Struc1_SilverCommandersInteraction) 
		{
			Command CMD1 =new Command();
			CMD1.PoliceCommand( "0" , Police_TaskType.CollectDeceasedCasualty ,Dead_List_inner  , true  ) ;// from PIC to RCO
			Mybody.CurrentMessage = new  ACL_Message( ACLPerformative.Requste, Mybody, Mybody.assignedIncident.RCOcommander ,CMD1, Mybody.CurrentTick,Mybody.assignedIncident.ComMechanism_level_StoB,Dead_List_inner.size(),TypeMesg.External) ;
			Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
			Dead_List_inner.clear();

			Mybody.Action=RespondersActions.RequstandupdatesinOP;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.Comunication ; 

			System.out.println("                                                                                    " +"PIC: "  + Mybody.Id + "  Dead_List_inner ");	
		}
		//==========================Request for Deceased  CCS==============================
		// ++++++ 23- +++++++
		else if ( Mybody.Action==RespondersActions.Noaction && this.sartCInfo==1   && ( Dead_List_CCS.size() >0 && this.RC_established>=1 ) && Mybody.assignedIncident.Communication_Structure_uesd==Inter_Communication_Structure.Struc1_SilverCommandersInteraction) 
		{
			Command CMD1 =new Command();
			CMD1.PoliceCommand( "0" , Police_TaskType.CollectDeceasedCasualty ,Dead_List_CCS  , false  ) ;// from PIC to RCO
			Mybody.CurrentMessage = new  ACL_Message( ACLPerformative.Requste, Mybody, Mybody.assignedIncident.RCOcommander ,CMD1, Mybody.CurrentTick,Mybody.assignedIncident.ComMechanism_level_StoB,Dead_List_CCS .size(),TypeMesg.External) ;
			Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
			Dead_List_CCS.clear();

			Mybody.Action=RespondersActions.RequstandupdatesinOP;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.Comunication ; 

			System.out.println("                                                                                    " +"PIC: "  + Mybody.Id + "   Dead_List_CCS ");
		}

		//==========================Update about  Nomorecasulty in CCS  RCO ==============================  
		// ++++++ 33- +++++++		
		else if ( Mybody.Action==RespondersActions.Noaction  && this.sartCInfo==1  &&	( NomorecasultyinInner==1  && NomorecasultyinCCS==1)  && Mybody.assignedIncident.Communication_Structure_uesd==Inter_Communication_Structure.Struc1_SilverCommandersInteraction) 	 // from FIC and AIC	 	
		{						

			Mybody.CurrentMessage = new  ACL_Message( ACLPerformative.InformNomorecasualty ,Mybody , Mybody.assignedIncident.RCOcommander,null, Mybody.CurrentTick,Mybody.assignedIncident.ComMechanism_level_StoB,1,TypeMesg.External) ;	
			Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
			NomorecasultyinInner=2  ;
			NomorecasultyinCCS=2 ;

			Mybody.Action=RespondersActions.RequstandupdatesinOP;  
			Mybody.BehaviourType3=RespondersBehaviourTypes3.Comunication ;

			System.out.println("                                                                                    " + "PIC: " +Mybody.Id  +"    updateRC Nomorecasulty  ");

		}


		//==========================Update about  Nomorecasulty in LA CC ==============================  
		// ++++++ 33- +++++++		
		else if ( Mybody.Action==RespondersActions.Noaction  && 	 NomorecasultyinLA==1   && Mybody.assignedIncident.Communication_Structure_uesd==Inter_Communication_Structure.Struc1_SilverCommandersInteraction  ) 	 // from AIC	 	
		{						

			Mybody.CurrentMessage = new  ACL_Message( ACLPerformative.InformNomorecasualty ,Mybody , Mybody.assignedIncident.O_CCcommander,null, Mybody.CurrentTick,Mybody.assignedIncident.ComMechanism_level_StoB,1,TypeMesg.External) ;		
			Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
			NomorecasultyinLA=2 ;

			Mybody.Action=RespondersActions.RequstandupdatesinOP;  
			Mybody.BehaviourType3=RespondersBehaviourTypes3.Comunication ; 

			System.out.println("                                                                                    " + "PIC: " +Mybody.Id  +"    updateCC Nomorecasulty for evacuation  ");

		}
		//============================================================ ALL
		// ++++++ 25- +++++++
		else if (Mybody.Action==RespondersActions.RequstandupdatesinOP && Mybody.Acknowledged )
		{
			Mybody.Action=RespondersActions.Noaction;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.WaitingDuring;
			Mybody.Acknowledged=false;Mybody.Sending=false; 
			System.out.println("                                                                                    " +"PIC: "  + Mybody.Id + " Done RequstandupdatesinOP ");
		}
		//-------------------------------------------------------------------------------------------- (P6)
		// ++++++ 26- +++++++
		else if ( Mybody.Action==RespondersActions.Noaction && EndofER()  && EndER_Ambulance==2 && EndER_Fire==2  && EndER_Police==2 )   
		{
			Mybody.InterpretedTrigger=RespondersTriggers.DoneEmergencyResponse ;
			System.gc() ;
			System.out.println("                                                                                    " +"PIC: "  + Mybody.Id + " End ER  $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");	
		}
	}

	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	// PIC -  CoordinateEndResponse  
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	private void CommanderBehavior_EndER()
	{
		// ++++++ 1- +++++++
		if ( Mybody.InterpretedTrigger==RespondersTriggers.DoneEmergencyResponse ) 
		{

			Reporting_ENDSituationAssessment();

			Mybody.Action=RespondersActions.InfromReport;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.Comunication;  
			Mybody.InterpretedTrigger=null;
			//System.out.println("                                                                                    " + Mybody.Id  +"......  " + Mybody.assignedIncident.PCOcommander.Action );

		}
		// ++++++ 2- +++++++
		else if (Mybody.Action==RespondersActions.InfromReport && Mybody. Acknowledged  )
		{
			// Send message to its commanders
			BoradcastEndofERAction();
			option=1 ;

			Mybody.Action=RespondersActions.BoradcastEndER ;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.Comunication;  
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
			Mybody.BehaviourType3=RespondersBehaviourTypes3.Comunication ; 
			Mybody.EndofCurrentAction=   InputFile.Boradcast_duration ;
			Mybody.EndActionTrigger= null;	
		}

		// ++++++ 3- +++++++
		else if (Mybody.Action==RespondersActions.BoradcastEndER && Mybody.EndActionTrigger== RespondersTriggers.EndingAction&& option==2   )
		{
			Mybody.InterpretedTrigger=RespondersTriggers.ENDER;

			Mybody.Action=RespondersActions.Noaction ;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.WaitingEnd;

			Mybody.Sending=false; 
			Mybody.EndActionTrigger= null;
			System.out.println("                                                                                    " + Mybody.Id  +" GO back to Vehicle  " +Mybody.Role );

			//for ( Responder_Police RA : MyResponder_Police)
			//	System.out.println(RA.Id + "-----" + RA.Action );
		}
	}

}//end class
