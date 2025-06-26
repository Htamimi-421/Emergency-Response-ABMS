package A_Roles_Ambulance;
import java.util.ArrayList;
import java.util.List;
import A_Agents.Casualty;
import A_Agents.Hospital;
import A_Agents.Responder;
import A_Agents.Responder_Ambulance;
import A_Agents.Vehicle_Ambulance;
import A_Environment.RoadLink;
import A_Environment.TacticalArea;
import A_Environment.TopographicArea;
import B_Classes.Task_ambulance;
import B_Communication.ACL_Message;
import B_Communication.Casualty_info;
import B_Communication.Command;
import B_Communication.ISRecord;
import B_Communication.Report;
import C_SimulationInput.InputFile;
import D_Ontology.Ontology.ACLPerformative;
import D_Ontology.Ontology.Allocation_Strategy;
import D_Ontology.Ontology.AmbulanceTracking;
import D_Ontology.Ontology.Ambulance_ResponderRole;
import D_Ontology.Ontology.Ambulance_TaskType;
import D_Ontology.Ontology.CasualtyReportandTrackingMechanism;
import D_Ontology.Ontology.CommunicationMechanism;
import D_Ontology.Ontology.Communication_Time;
import D_Ontology.Ontology.GeneralTaskStatus;
import D_Ontology.Ontology.Inter_Communication_Structure;
import D_Ontology.Ontology.Level;
import D_Ontology.Ontology.RespondersActions;
import D_Ontology.Ontology.RespondersBehaviourTypes1;
import D_Ontology.Ontology.RespondersTriggers;
import D_Ontology.Ontology.TypeMesg;
import D_Ontology.Ontology.UpdatePlanType;

public class Ambulance_Commander_ALC {

	Responder_Ambulance Mybody;

	Responder_Ambulance NewDriver=null,FinshedDriver=null , FinshedSoonDriver=null, AllocatedDrivertoTask=null;
	Responder_Ambulance		CurrentCCO=null ;

	Casualty_info CurrentCasualtybySender_info=null;
	Casualty CurrentCasualtybySender=null ;
	Hospital currentRecivingHospital=null;

	ISRecord Record_CurrentCasualty=null; 

	ArrayList<RoadLink> CurrentrouteNEEDClearwreckageSender  =new ArrayList<RoadLink>(); // by its driver
	ArrayList<RoadLink>  CurrentrouteNEEDCleartrafficSender =new ArrayList<RoadLink>();
	//----------------------------------------------------
	Task_ambulance  AllocatedTask , currntcheckedtask=null;

	boolean StartSetup=false;

	//----------------------------------------------------
	//  The meaning of attribute content....  -1:No update   , 0: Get  , 1: realize or done  2: send to workers or PIC
	int  EndLoading_AreaEstablished= -1 ,  cordons_outer_established= -1  , End_nomorec=-1  ; 
	int AllocatedDriverfroSetup=0;
	int  End_LA=-1 ;

	List<Task_ambulance> Operational_Plan = new ArrayList<Task_ambulance>(); //for each casualty 
	List<Responder_Ambulance> MyDrivers = new ArrayList<Responder_Ambulance>(); //its worker  or myAmbulance
	List<Responder_Ambulance> UnoccupiedDrivers = new ArrayList<Responder_Ambulance>(); //free worker

	List<Responder_Ambulance> UnoccupiedArrivingSoonDrivers = new ArrayList<Responder_Ambulance>(); //free worker
	List<Responder_Ambulance> OccupiedArrivingSoonDrivers = new ArrayList<Responder_Ambulance>(); //free worker
	//----------------------------------------------------
	// New to send to AIC
	ArrayList<RoadLink> routeNEEDClearwreckage_List =new ArrayList<RoadLink>();
	ArrayList<RoadLink> routeNEEDCleartraffic_List =new ArrayList<RoadLink>();

	// My memory 
	ArrayList<RoadLink> Mymemory_routeNEEDClearwreckage_List =new ArrayList<RoadLink>();
	ArrayList<RoadLink> Mymemory_routeNEEDCleartraffic_List =new ArrayList<RoadLink>();

	//----------------------------------------------------
	double Time_last_updated=0;	//itsAIC

	//##############################################################################################################################################################	
	public Ambulance_Commander_ALC ( Responder_Ambulance _Mybody  , TacticalArea  _TargetTA ,List<Responder_Ambulance>  Drivers ) 
	{
		Mybody=_Mybody;
		Mybody.EndofCurrentAction=0;	
		Mybody.ColorCode= 5;
		Mybody.AssignedTA=   _TargetTA ;

		Mybody.assignedIncident.ALCcommander=Mybody;
		Mybody.AssignedTA.Bronzecommander =Mybody;
		Mybody.PrvRoleinprint1 =Mybody.Role;

		//for(  Responder_Ambulance D:Drivers )
		//{MyDrivers.add(D);UnoccupiedDrivers.add(D);}
	}

	//##############################################################################################################################################################
	public void  CommanderALC_InterpretationMessage()
	{
		boolean  done= true;
		ACL_Message currentmsg =  Mybody.Message_inbox.get(Mybody.Lastmessagereaded);		 			
		Mybody.CurrentSender= (Responder)currentmsg.sender;
		Mybody.Lastmessagereaded++;
		
		Mybody.SendingReciving_External= false ; Mybody.SendingReciving_internal=false; 
		if (  currentmsg.Inernal) Mybody.SendingReciving_internal= true ;
		else if (  currentmsg.External) Mybody.SendingReciving_External=true ;

		switch( currentmsg.performative) {
		case Requste:
			Mybody.CurrentCommandRequest=((Command) currentmsg.content);

			//++++++++++++++++++++++++++++++++++++++
			if ( Mybody.CurrentSender instanceof Responder_Ambulance &&  Mybody.CurrentCommandRequest.commandType1 ==Ambulance_TaskType.TransferCasualtytoHospital  )  
			{				
				Mybody.CommTrigger= RespondersTriggers.GetRequstTransfer;
				CurrentCCO = (Responder_Ambulance)currentmsg.sender;

				CurrentCasualtybySender_info =Mybody.CurrentCommandRequest.TargetCasualty_info;
				currentRecivingHospital=Mybody.CurrentCommandRequest.TargetHospital;

				//System.out.println(Mybody.Id + "  xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx" + Mybody.CommTrigger );
			}
			//++++++++++++++++++++++++++++++++++++++
			break;
		case InformNewResponderArrival:	
			Mybody.CommTrigger= RespondersTriggers.GetNewDriverarrived ;
			NewDriver= (Responder_Ambulance) Mybody.CurrentSender;
			break;
		case InformDriverArrival:
			Mybody.CommTrigger= RespondersTriggers.GetDriverarrived;
			FinshedDriver= (Responder_Ambulance) Mybody.CurrentSender;
			break;
		case InformDriverArrivalSoon:
			Mybody.CommTrigger = RespondersTriggers.GetDriverSOONarrived ;
			FinshedSoonDriver= (Responder_Ambulance) Mybody.CurrentSender;
			break;	
		case InfromResultSetupLoadingArea:
			Mybody.CommTrigger= RespondersTriggers.GetresultofSetup;
			FinshedDriver= (Responder_Ambulance) Mybody.CurrentSender;
			break;
		case InformResultHTransfer:
			Mybody.CommTrigger= RespondersTriggers.GetresultofHTransfer;
			CurrentCasualtybySender = ((Casualty)currentmsg.content);
			FinshedDriver= null; //until arrived location
			break;			
		case InformRouteReport: 
			Mybody.CommTrigger= RespondersTriggers.GetRouteReport ;

			Report Report1=((Report) currentmsg.content);			
			CurrentrouteNEEDClearwreckageSender  = Report1.routeNEEDClearwreckage_List ;	
			CurrentrouteNEEDCleartrafficSender =Report1.routeNEEDCleartraffic_List ;		
			break;	
			//-------------------------------	
		case  InfromCordonEstablished: // form AIC  or OCC
			Mybody.CommTrigger= RespondersTriggers.GetSAupdatefromAIC ;			
			cordons_outer_established= 0 ; 
			break;		
		case InformNomorecasualty_CCS : // from AIC or  CCO
			Mybody.CommTrigger= RespondersTriggers.GetSAupdatefromAIC ;
			End_nomorec= 0 ; 
			break;	
		case InformERend :  // form AIC
			Mybody.CommTrigger= RespondersTriggers.ENDER;
			break;	
		default:
			done= true;
		} // end switch

		//System.out.println(Mybody.Id + "  " + Mybody.CommTrigger + "  from  "+ Mybody.CurrentSender.Id  + "    Action:" + Mybody.Action);
	}	

	//##############################################################################################################################################################	
	//													Plan
	//##############################################################################################################################################################	
	// Create Plan-list of tasks- Action for first time
	public void Implmenting_plan() {

		MyDrivers=Mybody.CurrentAmbulanceInScence ( ) ;

		for(  Responder_Ambulance D:MyDrivers )
			UnoccupiedDrivers.add(D);

	};

	//***************************************************************************************************
	// Update Plan - add Casualty/Task
	public void UpdatePlan( UpdatePlanType UpdateType, Casualty _Casualty, Ambulance_TaskType _TaskType ,Responder_Ambulance _Responder , Hospital _RecivingHospital  , int _prio_level) 
	{
		switch( UpdateType) {
		case NewRes :
			if ( ! MyDrivers.contains(_Responder))
			{MyDrivers.add(_Responder);
			UnoccupiedDrivers.add(_Responder);}
			else
				System.out.println( Mybody.Id + "  Iam ALC  eroooorrrrr in newr driver  I aready count hem " );
			break;
		case FreeRes:
			UnoccupiedDrivers.add(_Responder);
			break;
		case RemoveRes:
			MyDrivers.remove(_Responder);
			break;
		case NewTask:
			Task_ambulance Newtask = new Task_ambulance( _Casualty, _TaskType , _Responder , _RecivingHospital ); // CCO commander
			Operational_Plan.add(Newtask);
			Newtask.TimeofsendRequest= Mybody.CurrentTick;
			Newtask.priority_level= _prio_level ;
			break;

		case UpdateTask_triage_or_hospital:
			for (Task_ambulance Task : Operational_Plan) 
				if (Task.TargetCasualty == _Casualty  ) 
				{ 
					Task.priority_level= _prio_level ;
					Task.AssignedHospital=_RecivingHospital;
					//if ( _prio_level == 5)
					//Task.Decased=true;
				}	
			break;
		case AssignTask:
			for (Task_ambulance Task : Operational_Plan) 
				if (Task.TargetCasualty == _Casualty && Task.TaskType==_TaskType) 
				{ 
					Task.TaskStatus=GeneralTaskStatus.Inprogress ;
					Task.TaskStartTime = Mybody.CurrentTick ;
					// UnoccupiedDrivers.remove(Task.AssignedResponder); done in allocation
				}			
			break;
		case  CloseTask:
			for (Task_ambulance Task : Operational_Plan) 
				if (Task.TargetCasualty == _Casualty   && Task.TaskType==_TaskType ) 
				{
					Task.TaskStatus=GeneralTaskStatus.Done;	
					Task.TaskEndTime =Mybody.CurrentTick ;		
				}

			break;}
	}

	//***************************************************************************************************	
	// Task Allocation		
	public Task_ambulance Allocate_Ambulance(Allocation_Strategy Strategy) 
	{
		Task_ambulance nominatedTask=null;

		switch(Strategy) {
		case Severity_RYGPriorty:
			// identify more priority task-casualty- to assign ambulance
			double More_priorityRYG=4 ; 
			double MaxWaitTime=0; 
			for (Task_ambulance Task : Operational_Plan)
				if (Task.TaskStatus == GeneralTaskStatus.Waiting  && Task.AssignedResponder==null )
				{
					if (Task.priority_level < More_priorityRYG)   //priority_level_AfterTreatment
					{ 
						More_priorityRYG=Task.priority_level ; 
						nominatedTask=Task;
						MaxWaitTime= Mybody.CurrentTick- Task. TimeofsendRequest;

					}
					else if (Task.priority_level == More_priorityRYG)
					{
						double HowLongWaitTime= Mybody.CurrentTick- Task. TimeofsendRequest  ; 

						if (  HowLongWaitTime > MaxWaitTime)
						{	
							MaxWaitTime=HowLongWaitTime; 
							nominatedTask=Task;
						}	

					}

				}

			break;}


		Responder_Ambulance SelectedDriver=null ;

		if (  UnoccupiedDrivers.size() > 0 )	
		{
			SelectedDriver=UnoccupiedDrivers.get(0);
			UnoccupiedDrivers.remove(0);
		}
		else if (  this.UnoccupiedArrivingSoonDrivers.size() > 0 )
		{

			SelectedDriver=UnoccupiedArrivingSoonDrivers.get(0);
			UnoccupiedArrivingSoonDrivers.remove(0);
			OccupiedArrivingSoonDrivers.add(SelectedDriver) ;
		}

		nominatedTask.AssignedResponder= SelectedDriver;
		nominatedTask.AssignedAmbulance=SelectedDriver.Myvehicle ;


		return nominatedTask;
	}

	//##############################################################################################################################################################	
	//													Reporting
	//##############################################################################################################################################################	
	// Loading Area established or End	
	public void Reporting_plane(ACLPerformative xx  ) {

		// send message		
		if (  xx== ACLPerformative.InfromLoadingAreaEstablished  )
		{
			Mybody.CurrentMessage = new  ACL_Message( xx ,Mybody ,Mybody.assignedIncident.AICcommander,null, Mybody.CurrentTick ,Mybody.assignedIncident.ComMechanism_level_StoB,1,TypeMesg.Inernal) ;
			Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
		}		
//		else if ( xx== ACLPerformative.InfromLoadingAreaEstablished && Mybody.assignedIncident.Communication_Structure_uesd== Inter_Communication_Structure.Struc2_BronzeCommandersInteraction  )
//		{			
//			Mybody.CurrentMessage = new  ACL_Message( xx ,Mybody ,Mybody.assignedIncident.CCO_ambcommander,null, Mybody.CurrentTick ,Mybody.assignedIncident.ComMechanism_level_StoB,1,TypeMesg.External) ;
//			Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
//		}


		if (  xx== ACLPerformative.InformEndLoadingandTransportation_Nomorecasualty && Mybody.assignedIncident.Communication_Structure_uesd== Inter_Communication_Structure.Struc1_SilverCommandersInteraction )
		{
			Mybody.CurrentMessage = new  ACL_Message( xx ,Mybody ,Mybody.assignedIncident.AICcommander,null, Mybody.CurrentTick ,Mybody.assignedIncident.ComMechanism_level_StoB,1,TypeMesg.Inernal) ;
			Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
		}
		else if ( xx== ACLPerformative.InformEndLoadingandTransportation_Nomorecasualty  && Mybody.assignedIncident.Communication_Structure_uesd== Inter_Communication_Structure.Struc2_BronzeCommandersInteraction  )
		{			

			Mybody.CurrentMessage = new  ACL_Message( xx ,Mybody ,Mybody.assignedIncident.AICcommander,null, Mybody.CurrentTick ,Mybody.assignedIncident.ComMechanism_level_StoB,1,TypeMesg.Inernal) ;
			Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);

			Mybody.CurrentMessage = new  ACL_Message( ACLPerformative.InformNomorecasualty ,Mybody ,Mybody.assignedIncident.FRFSCcommander,null, Mybody.CurrentTick ,Mybody.assignedIncident.Inter_Communication_Tool_used,1,TypeMesg.External) ;
			Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);

			Mybody.CurrentMessage = new  ACL_Message( ACLPerformative.InformNomorecasualty ,Mybody ,Mybody.assignedIncident.O_CCcommander,null, Mybody.CurrentTick ,Mybody.assignedIncident.Inter_Communication_Tool_used,1,TypeMesg.External) ;
			Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
		}

	};

	//***************************************************************************************************  
	//Routes 
	public void Reporting_plane_LoadingandTransportationReport(ACLPerformative xx) {

		//		ArrayList<Casualty> Casualties_List =new ArrayList<Casualty>();
		//		for (Task_ambulance T : Operational_Plan) 
		//			if (( T.TaskStatus == GeneralTaskStatus.Done)   ) 
		//				Casualties_List.add(T.TargetCasualty);


		// send message
		if (  Mybody.assignedIncident.Communication_Structure_uesd== Inter_Communication_Structure.Struc1_SilverCommandersInteraction  )
		{
			Report Report1 =new Report();
			Report1.Ambulance_OPReport_loading( routeNEEDClearwreckage_List, routeNEEDCleartraffic_List ,null); //Casualties_List

			Mybody.CurrentMessage  = new  ACL_Message( xx ,Mybody ,Mybody.assignedIncident.AICcommander ,Report1, Mybody.CurrentTick ,Mybody.assignedIncident.ComMechanism_level_StoB,(routeNEEDClearwreckage_List.size() +routeNEEDCleartraffic_List.size() ),TypeMesg.External) ;
			Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
			routeNEEDClearwreckage_List.clear();
			routeNEEDCleartraffic_List.clear();
		}

		if (  routeNEEDClearwreckage_List.size() !=0  && Mybody.assignedIncident.FRFSCcommander!=null && Mybody.assignedIncident.Communication_Structure_uesd== Inter_Communication_Structure.Struc2_BronzeCommandersInteraction  )
		{
			Report Report2 =new Report();
			Report2.Ambulance_OPReport_loading( routeNEEDClearwreckage_List, null ,null);


			Mybody.CurrentMessage  = new  ACL_Message( xx ,Mybody ,Mybody.assignedIncident.FRFSCcommander ,Report2, Mybody.CurrentTick ,Mybody.assignedIncident.Inter_Communication_Tool_used,routeNEEDClearwreckage_List.size(),TypeMesg.External) ;
			Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);

			System.out.println("Loading:   " + Mybody.Id +" routeNEEDClearwreckage"+routeNEEDClearwreckage_List.size()+ "<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<" );
			routeNEEDClearwreckage_List.clear();
		}

		if ( routeNEEDCleartraffic_List.size() !=0 && Mybody.assignedIncident.Communication_Structure_uesd== Inter_Communication_Structure.Struc2_BronzeCommandersInteraction  )
		{
			Report Report3 =new Report();
			Report3.Ambulance_OPReport_loading( null, routeNEEDCleartraffic_List ,null);


			Mybody.CurrentMessage  = new  ACL_Message( xx ,Mybody ,Mybody.assignedIncident.O_CCcommander ,Report3, Mybody.CurrentTick ,Mybody.assignedIncident.Inter_Communication_Tool_used,routeNEEDCleartraffic_List.size(),TypeMesg.External) ;
			Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);

			System.out.println("Loading:   " + Mybody.Id +" routeNEEDtraffice"+routeNEEDCleartraffic_List.size()+ "<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<" );
			routeNEEDCleartraffic_List.clear();
		}

	};	

	//***************************************************************************************************
	// Commanding	
	public void Command_Driver(Task_ambulance _Task , Responder_Ambulance _Responder ) {

		Command CMD1 =new Command();

		if ( _Task==null ) // responders
		{
			CMD1.AmbulanceCommand(" " ,Ambulance_TaskType.SetupTacticalAreas ,Mybody.AssignedTA );
			// send message with command
			Mybody.CurrentMessage = new  ACL_Message( ACLPerformative.Command , Mybody,  _Responder  ,CMD1, Mybody.CurrentTick,Mybody.assignedIncident.ComMechanism_level_DrivertoALC ,1,TypeMesg.Inernal) ;
			Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
		}
		else // driver
		{
			CMD1.AmbulanceCommand(_Task.TaskID ,_Task.TaskType ,_Task.TargetCasualty ,_Task.AssignedHospital );
			Mybody.CurrentMessage = new  ACL_Message( ACLPerformative.Command , Mybody, _Task.AssignedResponder ,CMD1, Mybody.CurrentTick,Mybody.assignedIncident.ComMechanism_level_DrivertoALC,1 ,TypeMesg.Inernal) ;
			Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
		}

		//System.out.println("command: " + Mybody.Id + "  " + _Task.TaskType + "   "+ _Task.TargetCasualty.ID  + "    " + _Task.AssignedResponder.Id + "  " + _Task.AssignedHospital.ID );

	} 

	//***************************************************************************************************
	//Broadcast
	public void BoradcastAction(ACLPerformative   xx )
	{			
		Mybody.CurrentMessage  = new  ACL_Message( xx, Mybody, MyDrivers ,null, Mybody.CurrentTick,CommunicationMechanism.FF_BoradCast,1 ,TypeMesg.Inernal) ;
		Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
		// break; //one by one 	
	}

	//##############################################################################################################################################################	
	//													Interpreted Triggers 
	//##############################################################################################################################################################	

	public boolean IsNewCasualty( Casualty _Casualty )
	{	
		boolean  Result=true;

		for (Task_ambulance T : Operational_Plan ) 
			if (T.TargetCasualty  == _Casualty  ) 
			{
				Result=false;
				break;
			}

		return Result;
	}

	//----------------------------------------------------------------------------------------------------
	public boolean ThereisWaitingCasualty()
	{	
		boolean  Result=false;

		for (Task_ambulance Task : Operational_Plan) 
			if (Task.TaskStatus == D_Ontology.Ontology.GeneralTaskStatus.Waiting && Task.AssignedResponder==null   ) 
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

		for (Task_ambulance Task : Operational_Plan) 
			if (Task.TaskStatus == GeneralTaskStatus.Inprogress   && Task.Send_Am_AllocationTOCCS==false ) 
			{
				Result=true;
				break;
			}

		return Result;
	}

	//----------------------------------------------------------------------------------------------------	
	public Task_ambulance  GETThereCasualtyneedtoSendAllocation()
	{	
		Task_ambulance  Result=null;
		int Maxp=4;

		for (Task_ambulance Task : Operational_Plan) 
			if (Task.TaskStatus == D_Ontology.Ontology.GeneralTaskStatus.Inprogress   && Task.Send_Am_AllocationTOCCS==false ) 
			{
				if ( Task.priority_level < Maxp )
				{ Result=Task;}
			}

		return Result;
	}

	//----------------------------------------------------------------------------------------------------
	public boolean ThereisWaitingTocommandDriver()
	{	
		boolean  Result=false;

		for (Task_ambulance Task : Operational_Plan) 
			if (Task.TaskStatus == GeneralTaskStatus.Waiting && Task.AssignedResponder!=null ) 
			{

				if ( ((Vehicle_Ambulance)Task.AssignedAmbulance).onBarkofLA1 ==true  )	//&& ! OccupiedArrivingSoonDrivers.contains(Task.AssignedResponder)  && ! UnoccupiedArrivingSoonDrivers.contains(Task.AssignedResponder) // mening he send notfication		
				{
					Result=true;				
					currntcheckedtask= Task ;
					break;
				}
			}
		return Result;
	} 

	//----------------------------------------------------------------------------------------------------
	public boolean IsAllTaskclosed()
	{	
		boolean  Result=true;

		if (Operational_Plan.size() ==0 )
			Result=false;
		//or	
		for (Task_ambulance Task : Operational_Plan) 
			if (Task.TaskStatus != D_Ontology.Ontology.GeneralTaskStatus.Done    ) 
			{
				Result=false;
				break;
			}

		return Result;
	}

	//----------------------------------------------------------------------------------------------------
	public boolean IsAllDriverComeback()
	{	
		boolean  Result=true;		
		if (MyDrivers.size() != UnoccupiedDrivers.size()  ) 					
		{
			Result=false;				
		}

		return Result;
	}

	//----------------------------------------------------------------------------------------------------		
	public boolean ISthereupdateTosend( )
	{	
		boolean  Result= false;

		//updates to silver commander 

		if ( ( this.routeNEEDCleartraffic_List.size() >0 )|| (this.routeNEEDClearwreckage_List.size() >0  &&  Mybody.assignedIncident.FRFSCcommander!=null ))
		{  Result=true; }

		if  ( End_LA!=2 && IsAllTaskclosed() && IsAllDriverComeback() &&  End_nomorec==1) 
		{	Result=true; End_LA=1;	}	

		return Result;
	}
	//______________________________________________________________________________________________________________________________________________________________
	//##############################################################################################################################################################
	//                                                        Behavior
	//##############################################################################################################################################################
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	// ALC commander - General 
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	public void ALCBehavior()	
	{		
		//*************************************************************************
		// 1- initial response
		if( Mybody.CommTrigger==RespondersTriggers.AssigendRolebyAIC) 				
		{
			Mybody.CurrentAssignedActivity=Ambulance_TaskType.GoTolocation;	
		}
		// 2- Transfer to hospital
		else if (Mybody.CurrentAssignedActivity==Ambulance_TaskType.GoTolocation &&  Mybody.InterpretedTrigger== RespondersTriggers.FirstTimeonLocation) 
		{
			Mybody.CurrentAssignedActivity=Ambulance_TaskType.CoordinateCasualtyTransferToHospital ;
		}
		// 3-No action
		else if (Mybody.CurrentAssignedActivity==Ambulance_TaskType.CoordinateCasualtyTransferToHospital &&  Mybody.InterpretedTrigger==RespondersTriggers.DoneLoadingActivity) 
		{					
			Mybody.CurrentAssignedActivity=Ambulance_TaskType.None;	
			Mybody.InterpretedTrigger=null;
		}				
		// 4-Ending
		else if (Mybody.CurrentAssignedActivity==Ambulance_TaskType.None && Mybody.CommTrigger==RespondersTriggers.ENDER )  
		{
			Mybody.CurrentAssignedActivity=Ambulance_TaskType.CoordinateEndResponse;  //inform driver
		}
		else if(Mybody.InterpretedTrigger==RespondersTriggers.ENDER )
		{
			Mybody.Role=Ambulance_ResponderRole.None; 
		}
		//*************************************************************************
		switch(Mybody.CurrentAssignedActivity) {
		case GoTolocation:
			CommanderBehavior_GoToloadingArea()	;	
			break;
		case CoordinateCasualtyTransferToHospital  :			
			CommanderBehavior_CoordinateCasualtyTransferToHospital();
			break;			
		case CoordinateEndResponse :
			CommanderBehavior_EndER() ;
			break;}

		//System.out.println(Mybody.Id+" "  +  "  Action:" + Mybody.Action + " com:" + Mybody.CommTrigger + "   Nt:" + Mybody.InterpretedTrigger+ " en:" + Mybody.SensingeEnvironmentTrigger + " end:" + Mybody.EndActionTrigger +  "Acknowledged: "+ Mybody.Acknowledged +"s ending: "+  Mybody.Sending  + Mybody.CurrentMessage +"  "+ IsAllTaskclosed()+"  "+  IsAllDriverComeback() +"  "+ End_nomorec );

	}// end ALC Behavior

	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	// ALC - Go to location   
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^	
	private void CommanderBehavior_GoToloadingArea()	
	{
		// ++++++ 1- +++++++
		if( Mybody.CommTrigger==RespondersTriggers.AssigendRolebyAIC)
		{	

			Mybody.Assign_DestinationCordon(Mybody.assignedIncident.loadingArea.Location); 

			Mybody.Action=RespondersActions.GoToloadingArea;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Movementonincidentsite ; 	
			Mybody.CommTrigger=null;
		}
		// ++++++ 2- +++++++
		else if (Mybody.Action==RespondersActions.GoToloadingArea && Mybody.SensingeEnvironmentTrigger==null)
		{			
			Mybody.Walk();
		}	
		// ++++++ 3- +++++++
		else if (Mybody.Action==RespondersActions.GoToloadingArea && Mybody.SensingeEnvironmentTrigger== RespondersTriggers.ArrivedTargetObject ) 
		{ 					
			//send message to AIC				
			Mybody.CurrentMessage  = new  ACL_Message( ACLPerformative.InformlocationArrival , Mybody , Mybody.assignedIncident.AICcommander ,null ,  Mybody.CurrentTick,Mybody.assignedIncident.ComMechanism_level_StoB ,1,TypeMesg.Inernal) ;
			Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);

			Time_last_updated=Mybody.CurrentTick;

			Mybody.Action=RespondersActions.NotifyArrival;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Comunication ;  
			Mybody.SensingeEnvironmentTrigger=null;			
		}	
		// ++++++ 4- +++++++
		else if (Mybody.Action==RespondersActions.NotifyArrival && Mybody. Acknowledged)
		{
			Mybody.InterpretedTrigger= RespondersTriggers.FirstTimeonLocation;
			Mybody.Acknowledged=false;Mybody.Sending=false; 	

			System.out.println("  "+"Loading: " + Mybody.Id +" arrived Loading");
		}

	}		

	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	// ALC -  CoordinateCasualtyTransfer
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	private void CommanderBehavior_CoordinateCasualtyTransferToHospital()
	{						
		// ++++++ 1- +++++++
		if (Mybody.InterpretedTrigger == RespondersTriggers.FirstTimeonLocation)
		{
			//Implmenting_plan();//count ambulance
			StartSetup=true;
			End_nomorec=-1;

			Mybody.Action=RespondersActions.FormulatePlan;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Planformulation ; 	
			Mybody.EndofCurrentAction=  InputFile.FormulatePlan_duration  ; 
			Mybody.InterpretedTrigger=null; 
		}
		// ++++++ 2- +++++++
		else if ( Mybody.Action==RespondersActions.FormulatePlan  && Mybody.EndActionTrigger== RespondersTriggers.EndingAction 	) 			 	
		{				
			Mybody.Action=RespondersActions.Noaction;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.WaitingDuring;
			Mybody.EndActionTrigger=null;				

			System.out.println("  "+"Loading: " + Mybody.Id +" My ambulance " + +MyDrivers.size() );
			//for(  Responder_Ambulance Am :UnoccupiedDrivers)
			//System.out.println("Loading:   " + Am.Id +" My ambulance "+Am.Action );
		}

		//-------------------------------------------------------------------------------------------- (P1)
		//==========================New/Back Driver==========================
		// ++++++ 3- +++++++
		else if (Mybody.Action==RespondersActions.Noaction && (Mybody.CommTrigger == RespondersTriggers.GetNewDriverarrived ||  Mybody.CommTrigger == RespondersTriggers.GetDriverarrived ) )  //||Mybody.CommTrigger == RespondersTriggers.GetDriverSOONarrived  )	  										
		{	 	
			//Update plan
			if ( Mybody.CommTrigger == RespondersTriggers.GetNewDriverarrived )
			{UpdatePlan( UpdatePlanType.NewRes, null, null ,NewDriver,null ,0) ;	 } 			
			else if ( Mybody.CommTrigger == RespondersTriggers.GetDriverarrived )		
			{

				if (Mybody.assignedIncident.AmbulanceTracking_Strategy==AmbulanceTracking.Radio)  //&& 
				{
					UpdatePlan( UpdatePlanType.FreeRes, null, null ,FinshedDriver,null ,0) ; 							
				}
				else if (Mybody.assignedIncident.AmbulanceTracking_Strategy==AmbulanceTracking.GPS)
				{
					if ( UnoccupiedArrivingSoonDrivers.contains(FinshedDriver) )
					{
						UnoccupiedArrivingSoonDrivers.remove(FinshedDriver);
						UpdatePlan( UpdatePlanType.FreeRes, null, null ,FinshedDriver,null ,0) ; 
					}

					else if (  OccupiedArrivingSoonDrivers.contains(FinshedDriver) )
					{							
						OccupiedArrivingSoonDrivers.remove(FinshedDriver);		//he already sllocated  just wait to comand		
					}
					else
					{
						UpdatePlan( UpdatePlanType.FreeRes, null, null ,FinshedDriver,null ,0) ; 
					}
				}
			}
			//	else if ( Mybody.CommTrigger == RespondersTriggers.GetDriverSOONarrived)   //not used
			//	{ ArrivingSoonDrivers.add(FinshedSoonDriver) ; }

			Mybody.Action=RespondersActions.GetArrivalNotification ;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Comunication ; 
			Mybody.EndofCurrentAction=  InputFile.GetNotification_duration ;
			//reciving=true;
		}
		// ++++++ 4- +++++++
		else if ( Mybody.Action==RespondersActions.GetArrivalNotification  && Mybody.EndActionTrigger== RespondersTriggers.EndingAction 	) 			 	
		{			

			if ( Mybody.CommTrigger == RespondersTriggers.GetNewDriverarrived )
				NewDriver.Acknowledg(Mybody);  // System.out.println("  "+"Loading: " + Mybody.Id +"  ......  "  +   NewDriver.Id +"  "       + MyDrivers.size() );}
			else if ( Mybody.CommTrigger == RespondersTriggers.GetDriverarrived  )		
				FinshedDriver.Acknowledg(Mybody);
			//	else if ( Mybody.CommTrigger == RespondersTriggers.GetDriverSOONarrived)
			//		FinshedSoonDriver.Acknowledg(Mybody);
			//reciving=false;
			Mybody.Action=RespondersActions.UpdatePlan;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Planformulation ;  
			Mybody.EndofCurrentAction=   InputFile.UdatePlan_duration  ; 
			Mybody.EndActionTrigger=null; 
			Mybody.CommTrigger=null; 
		}		
		//==========================Result of Setup LA ==========================  Both N/A
		// ++++++ 5- +++++++
		else if (Mybody.Action==RespondersActions.Noaction &&  Mybody.CommTrigger== RespondersTriggers.GetresultofSetup )	  										
		{	 	
			//Update plan
			UpdatePlan( UpdatePlanType.FreeRes, null, null ,FinshedDriver,null,0) ; 
			EndLoading_AreaEstablished = 0 ;
			AllocatedDriverfroSetup --;

			if  ( AllocatedDriverfroSetup == 0)
			{
				EndLoading_AreaEstablished = 1 ;
				StartSetup=false;
				System.out.println("  "+"Loading: " + Mybody.Id +"  Done Setup   "  + MyDrivers.size() );
			}

			Mybody.Action=RespondersActions.GetResultofSetupTA ;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Comunication ; 
			Mybody.EndofCurrentAction=  InputFile.GetNotification_duration ; 
			//reciving=true;
		}
		// ++++++ 6- +++++++
		else if ( Mybody.Action==RespondersActions.GetResultofSetupTA  && Mybody.EndActionTrigger== RespondersTriggers.EndingAction 	) 			 	
		{
			Mybody.CurrentSender.Acknowledg(Mybody);  //reciving=false;	

			Mybody.Action=RespondersActions.UpdatePlan;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Planformulation ;  
			Mybody.EndofCurrentAction=  InputFile.UdatePlan_duration  ; 
			Mybody.EndActionTrigger=null;
			Mybody.CommTrigger=null;
		}
		//==========================Request for transfer :Ready ========================== from CCO
		// ++++++ 7- +++++++
		else if (Mybody.Action==RespondersActions.Noaction && ( Mybody.CommTrigger== RespondersTriggers.GetRequstTransfer|| (Mybody.CommTrigger== null && Mybody.assignedIncident.NewRecordcasualtyadded_ISSystem(Mybody.Role,null,1)>0 ))   )		  										
		{	 	
			//System.out.println("  "+"Loading:   " + Mybody.Id +" GetRequstTransfer from CCS "+ UnoccupiedDrivers.size());
			Mybody.Action=RespondersActions.GetRequstforTransfer;

			if ( Mybody.CommTrigger== RespondersTriggers.GetRequstTransfer )
				Mybody.EndofCurrentAction= InputFile.GetInfromation_FtoForR_Duration_Data ; 
			else 
			{
				Mybody.EndofCurrentAction= InputFile.GetInfromation_Electronic_Duration ;
				Record_CurrentCasualty=Mybody.assignedIncident.GeRecordcasualtyISSystem(Mybody.Role,null,1);
				Mybody.SendingReciving_External= false ; Mybody.SendingReciving_internal= true ;
			}

			Mybody.BehaviourType1=RespondersBehaviourTypes1.Comunication; //reciving=true; 			

		}
		// ++++++ 8- +++++++
		else if ( Mybody.Action==RespondersActions.GetRequstforTransfer && Mybody.EndActionTrigger== RespondersTriggers.EndingAction 	) 			 	
		{
			Casualty ca;
			int prio_level;

			if ( Mybody.CommTrigger== RespondersTriggers.GetRequstTransfer )
			{
				CurrentCCO.Acknowledg(Mybody);	//reciving=false;
				Mybody.CommTrigger=null; 

				ca=CurrentCasualtybySender_info.ca ;
				prio_level=CurrentCasualtybySender_info.priority_level;
				// currentRecivingHospital  in up  

			}
			else 
			{	

				ca=Record_CurrentCasualty.CasualtyinRec;
				prio_level=Record_CurrentCasualty.priority_levelinRec;	
				currentRecivingHospital=Record_CurrentCasualty.AssignedHospitalinRec;
			}

			//update plan
			if (IsNewCasualty( ca ))
			{
				UpdatePlan( UpdatePlanType.NewTask, ca , Ambulance_TaskType.TransferCasualtytoHospital , CurrentCCO , currentRecivingHospital ,prio_level );
				//	System.out.println("Loading:   " + Mybody.Id +" GetRequstTransfer "+ CurrentCasualtybySender.ID);
			}

			else
			{
				UpdatePlan( UpdatePlanType.UpdateTask_triage_or_hospital , ca , Ambulance_TaskType.TransferCasualtytoHospital , CurrentCCO , currentRecivingHospital ,prio_level );
				//	System.out.println("Loading:   " + Mybody.Id +" GetRequstTransfer "+ CurrentCasualtybySender.ID);

			}


			Mybody.Action=RespondersActions.UpdatePlan;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Planformulation  ;
			Mybody.EndofCurrentAction= InputFile.UdatePlan_duration  ; 
			Mybody.EndActionTrigger=null;
		}
		//==========================Result of Transfer to H ==========================	Both	Paper/Electronic
		// ++++++ 9- +++++++
		else if ( Mybody.Action==RespondersActions.Noaction && ( Mybody.CommTrigger== RespondersTriggers.GetresultofHTransfer || ( Mybody.CommTrigger== null && Mybody.assignedIncident.NewRecordcasualtyadded_ISSystem(Mybody.Role,null,2)>0	)  )   )		  										
		{	 				
			//System.out.println("Loading:   " +Mybody.Id + " GetresultofHTransfer  " );
			Mybody.Action=RespondersActions.GetResultTransfer; 
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Comunication ; 

			if ( Mybody.CommTrigger== RespondersTriggers.GetresultofHTransfer )
				Mybody.EndofCurrentAction= InputFile.GetInfromation_FtoForR_Duration_Notification ; //reciving=true;
			else 
			{
				Mybody.EndofCurrentAction= InputFile.GetInfromation_Electronic_Duration ;
				Record_CurrentCasualty=Mybody.assignedIncident.GeRecordcasualtyISSystem(Mybody.Role,null ,2);//FIFO  inside Record.checkedSC=true;
				Mybody.SendingReciving_External= false ; Mybody.SendingReciving_internal= true ;
			}			

		}
		// ++++++ 10- +++++++
		else if ( Mybody.Action==RespondersActions.GetResultTransfer  && Mybody.EndActionTrigger== RespondersTriggers.EndingAction 	) 			 	
		{		
			if ( Mybody.CommTrigger== RespondersTriggers.GetresultofHTransfer)
			{
				Mybody.CurrentSender.Acknowledg(Mybody);    //reciving=false;	
				//UpdatePlan		
				UpdatePlan( UpdatePlanType.CloseTask, CurrentCasualtybySender, Ambulance_TaskType.TransferCasualtytoHospital ,null,null,0);
				Mybody.CommTrigger=null; 	
			}
			else 
			{						

				UpdatePlan( UpdatePlanType.CloseTask, Record_CurrentCasualty.CasualtyinRec, Ambulance_TaskType.TransferCasualtytoHospital ,null,null,0);
			}

			Mybody.Action=RespondersActions.UpdatePlan;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Planformulation  ; 
			Mybody.EndofCurrentAction= InputFile.UdatePlan_duration ; 
			Mybody.EndActionTrigger=null; 

		}
		//==========================Result of Route ==========================	Both	
		// ++++++ 11- +++++++
		else if ( Mybody.Action==RespondersActions.Noaction && ( Mybody.CommTrigger== RespondersTriggers.GetRouteReport   )   )		  										
		{	 				
			//System.out.println("  "+"Loading:   " +Mybody.Id + " Getresultofrout xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx 1" );

			int total=1;
			//UpdatePlan									
			for(RoadLink  RL :  CurrentrouteNEEDClearwreckageSender)
				if ( ! Mymemory_routeNEEDClearwreckage_List.contains(RL)  )
				{
					routeNEEDClearwreckage_List.add(RL);
					Mymemory_routeNEEDClearwreckage_List.add(RL);
					total++;
				}

			for(RoadLink  RL :  CurrentrouteNEEDCleartrafficSender )
				if ( ! 	Mymemory_routeNEEDCleartraffic_List.contains(RL)  )
				{
					routeNEEDCleartraffic_List.add(RL);
					Mymemory_routeNEEDCleartraffic_List.add(RL);
					total++;
				}			

			Mybody.Action=RespondersActions.GetRouteReport; 
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Comunication; 
			Mybody.EndofCurrentAction= InputFile.GetInfromation_FtoForR_Duration_Data * total ; 
			Mybody.CommTrigger=null; //reciving=true;
		}
		// ++++++ 12- +++++++
		else if ( Mybody.Action==RespondersActions.GetRouteReport  && Mybody.EndActionTrigger== RespondersTriggers.EndingAction 	) 			 	
		{				
			Mybody.CurrentSender.Acknowledg(Mybody);	//reciving=false;
			//System.out.println("  "+"Loading:   " + Mybody.Id + " get route xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx ") ;

			Mybody.Action=RespondersActions.UpdatePlan;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Planformulation   ;  
			Mybody.EndofCurrentAction= InputFile.UdatePlan_duration ; 
			Mybody.EndActionTrigger=null; 

		}
		//==========================cordon established , Updates from AIC  ==========================		
		// ++++++ 13- +++++++
		else if (  Mybody.Action==RespondersActions.Noaction   &&  Mybody.CommTrigger== RespondersTriggers.GetSAupdatefromAIC     )											
		{	 							 
			//Updates about SA
			if ( this.cordons_outer_established == 0   )  
				cordons_outer_established =1 ;	
			else if (   End_nomorec==0)
			{	End_nomorec=1;System.out.println("  " + "Loading:   " + Mybody.Id +"GetNoMorecasualty" );}

			Mybody.Action=RespondersActions.GetSAupdatefromAIC  ;		
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Comunication ; 
			Mybody.EndofCurrentAction= InputFile.GetInfromation_FtoForR_Duration_Data ; 	

			Mybody.CommTrigger=null; //reciving=true;
		}
		// ++++++ 14- +++++++
		if (Mybody.Action==RespondersActions.GetSAupdatefromAIC  && Mybody.EndActionTrigger== RespondersTriggers.EndingAction 	  )//InputFile.CasualtyReport_Mechanism == CasualtyReportandTrackingMechanism.Paper		
		{
			Mybody.CurrentSender.Acknowledg(Mybody);  //reciving=false;

			Mybody.Action=RespondersActions.UpdatePlan;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Planformulation  ; 
			Mybody.EndofCurrentAction= InputFile.UdatePlan_duration ; 
			Mybody.EndActionTrigger=null; 
		}
		//============================================================ ALL
		// ++++++ 15- +++++++
		else if (Mybody.Action==RespondersActions.UpdatePlan && Mybody.EndActionTrigger== RespondersTriggers.EndingAction  )
		{						
			Mybody.Action=RespondersActions.Noaction;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.WaitingDuring;
			Mybody.EndActionTrigger=null;
		}

		//-------------------------------------------------------------------------------------------- (P2)
		else  if (Mybody.Action==RespondersActions.Noaction && EndLoading_AreaEstablished==1 )
		{
			Reporting_plane(ACLPerformative.InfromLoadingAreaEstablished ) ; EndLoading_AreaEstablished= 2;
			
			Mybody.Action=RespondersActions.InfromOPReport2;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Comunication  ; 
			
			System.out.println("  "+"Loading: " +  Mybody.Id  +"  InfromOPReport to CCS  "  + this.Mybody.CurrentMessage_list.size()  +  this.Mybody.Sending);

		}
		// ++++++ 17- +++++++
				else if (Mybody.Action==RespondersActions.InfromOPReport2 && Mybody.Acknowledged )
				{
					Mybody.Action=RespondersActions.Noaction;
					Mybody.BehaviourType1=RespondersBehaviourTypes1.WaitingDuring;
					Mybody.Acknowledged=false;Mybody.Sending=false;

				
					//System.out.println("  "+"Loading: " +  Mybody.Id  +"  InfromOPReport2  "  +  this.Mybody.Sending   );
				}
		
		//-------------------------------------------------------------------------------------------- (P2)
		// ++++++ 16- +++++++
		else  if (Mybody.Action==RespondersActions.Noaction &&
				(		
						(  Mybody.assignedIncident.Communication_Structure_uesd==Inter_Communication_Structure.Struc1_SilverCommandersInteraction  && ( ( ISthereupdateTosend( ) &&  Mybody.assignedIncident.Intra_Communication_Time_used==Communication_Time.When_need)   )) 
						||
						(  Mybody.assignedIncident.Communication_Structure_uesd==Inter_Communication_Structure.Struc2_BronzeCommandersInteraction  && (  ( Mybody.CurrentTick >= (Time_last_updated +  Mybody.assignedIncident.UpdatOPEvery) &&  Mybody.assignedIncident.Inter_Communication_Time_used==Communication_Time.Every_frequently )  ||  ( ISthereupdateTosend( ) &&  Mybody.assignedIncident.Inter_Communication_Time_used==Communication_Time.When_need)   )) 
						)				
				)
		{

			if ( ISthereupdateTosend( )   )
			{ 				
				
				if ( (this.routeNEEDCleartraffic_List.size() >0) || (this.routeNEEDClearwreckage_List.size() >0  &&  Mybody.assignedIncident.FRFSCcommander!=null ))
				{ Reporting_plane_LoadingandTransportationReport(ACLPerformative.InformLoadingandTransportationReport ) ;}

				if (End_LA==1 )	
				{ Reporting_plane(ACLPerformative.InformEndLoadingandTransportation_Nomorecasualty ) ;	End_LA=2 ; }

				Mybody.Action=RespondersActions.InfromOPReport;
				Mybody.BehaviourType1=RespondersBehaviourTypes1.Comunication  ; 

				System.out.println("  "+"Loading: " +  Mybody.Id  +"  InfromOPReport1  "  + this.Mybody.CurrentMessage_list.size()  +  this.Mybody.Sending);

			}
			else
			{
				Mybody.Action=RespondersActions.UpdatePlan;
				Mybody.BehaviourType1=RespondersBehaviourTypes1.Planformulation  ; 
				Mybody.EndofCurrentAction=  InputFile.UdatePlan_duration  ; 
				Time_last_updated=Mybody.CurrentTick;
			}
		}
		// ++++++ 17- +++++++
		else if (Mybody.Action==RespondersActions.InfromOPReport && Mybody.Acknowledged )
		{
			Mybody.Action=RespondersActions.Noaction;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.WaitingDuring;
			Mybody.Acknowledged=false;Mybody.Sending=false;

			Time_last_updated=Mybody.CurrentTick ;
			//System.out.println("  "+"Loading: " +  Mybody.Id  +"  InfromOPReport2  "  +  this.Mybody.Sending   );
		}
		//-------------------------------------------------------------------------------------------- (P3) update its responders 
		// ++++++ 18- +++++++
		else if (  Mybody.Action==RespondersActions.Noaction  && cordons_outer_established==1 )  										
		{
			cordons_outer_established=2; 

			//send message to now can move					
			BoradcastAction(ACLPerformative.InfromCordonEstablished);

			Mybody.Action=RespondersActions.BoradcastFICpdate ;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Comunication  ; 
			Mybody.EndofCurrentAction=   InputFile.Boradcast_duration ;
			//System.out.println("  "+"Loading: " +  Mybody.Id  +"  Boradcastupdate to responders  "   );		
		}
		// ++++++ 19- +++++++
		else if (Mybody.Action==RespondersActions.BoradcastFICpdate && Mybody.EndActionTrigger== RespondersTriggers.EndingAction  )
		{				
			Mybody.Action=RespondersActions.Noaction;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.WaitingDuring;
			Mybody.Sending=false; 
			Mybody.EndActionTrigger=null;
		}
		//-------------------------------------------------------------------------------------------- (P4)
		//============= setup ======================
		// ++++++ 20- +++++++
		else if (  Mybody.Action==RespondersActions.Noaction  && ( StartSetup && EndLoading_AreaEstablished == -1 && ! Mybody.AssignedTA.installed)  && UnoccupiedDrivers.size() >0 )		  										
		{
			AllocatedDrivertoTask=UnoccupiedDrivers.get(0)  ;
			UnoccupiedDrivers.remove(0);
			AllocatedDriverfroSetup ++;

			Mybody.Action=RespondersActions.AllocatResponders;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Planformulation ;
			Mybody.EndofCurrentAction=   InputFile.Allocation_duration ;

			//System.out.println("  "+"Loading: " +  Mybody.Id  +"  allocate   "   );
		}
		// ++++++ 21- +++++++
		else if (Mybody.Action==RespondersActions.AllocatResponders &&  StartSetup &&  Mybody.EndActionTrigger== RespondersTriggers.EndingAction  )
		{				
			Command_Driver(null ,AllocatedDrivertoTask );

			Mybody.Action=RespondersActions.CommandSetupTA ;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Comunication; //TaskPlanning;			
			Mybody.EndActionTrigger=null;					
		}
		// ++++++ 22- +++++++
		else if (Mybody.Action==RespondersActions.CommandSetupTA && Mybody. Acknowledged )
		{
			Mybody.Action=RespondersActions.Noaction;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.WaitingDuring;
			Mybody.Acknowledged=false;Mybody.Sending=false; 		
		}
		//============= comand driver ======================
		// ++++++ 23- +++++++
		else if (  Mybody.Action==RespondersActions.Noaction   && cordons_outer_established ==2 && ThereisWaitingTocommandDriver()  )//InterpretedTrigger
		{	
			Command_Driver(currntcheckedtask, null);
			//System.out.println("Loading:   " +Mybody.Id +" commad driver " +currntcheckedtask.AssignedResponder.Id  +"command "  + currntcheckedtask.TargetCasualty.ID  );
			UpdatePlan( UpdatePlanType.AssignTask, currntcheckedtask.TargetCasualty , Ambulance_TaskType.TransferCasualtytoHospital, null, null ,0);	
			
			Mybody.assignedIncident.StartAmbEvacuation= true; //to start caculate W T
			
			Mybody.Action=RespondersActions.CommandDriverTransfer;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Comunication; //TaskPlanning ;			

		}
		// ++++++ 26- +++++++
		else if (Mybody.Action==RespondersActions.CommandDriverTransfer && Mybody.Acknowledged )
		{				
			Mybody.Action=RespondersActions.Noaction;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.WaitingDuring;
			Mybody.Acknowledged=false;Mybody.Sending=false;

			//System.out.println("Loading:   " +Mybody.Id +" commad driver");
		}
		//============= Casualty ======================	

		else if (  Mybody.Action==RespondersActions.Noaction &&  Mybody.assignedIncident.CasualtyReport_Mechanism_IN== CasualtyReportandTrackingMechanism.Paper && this.IsThereCasualtyneedtoSendAllocation()     )
		{

			AllocatedTask= GETThereCasualtyneedtoSendAllocation() ;
			//send message to CCO	
			Command CMD1 =new Command();
			CMD1.AmbulanceCommand("0", AllocatedTask.TaskType ,AllocatedTask.TargetCasualty,  AllocatedTask.AssignedResponder.Myvehicle );
			Mybody.CurrentMessage  = new  ACL_Message( ACLPerformative.InformAmbulanceAllocationResult, Mybody , Mybody.assignedIncident.CCO_ambcommander ,CMD1 ,  Mybody.CurrentTick , Mybody.assignedIncident.ComMechanism_level_StoB,1,TypeMesg.Inernal ) ;
			Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);

			AllocatedTask.Send_Am_AllocationTOCCS=true;

			//System.out.println("  " + "Loading: " + Mybody.Id +" send allocation "  + AllocatedTask.AssignedResponder.Id  + "   " + AllocatedTask.TargetCasualty.ID);

			Mybody.Action=RespondersActions.InfromAllocationResult;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Comunication ;		

		}
		// ++++++ 25- +++++++
		else if (Mybody.Action==RespondersActions.InfromAllocationResult &&   Mybody. Acknowledged    )
		{
			Mybody.Action=RespondersActions.Noaction;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.WaitingDuring;
			Mybody.Acknowledged=false;Mybody.Sending=false;
		}
		//============= Ambulance ======================	
		// ++++++ 23- +++++++
		else if (  Mybody.Action==RespondersActions.Noaction && cordons_outer_established ==2 && ThereisWaitingCasualty() && (UnoccupiedDrivers.size() >0 || UnoccupiedArrivingSoonDrivers.size() >0) )//InterpretedTrigger
		{			
			AllocatedTask = Allocate_Ambulance(Allocation_Strategy.Severity_RYGPriorty) ;	// iside 

			//System.out.println("  " + "Loading: " + Mybody.Id +" allocate  Driver  "  + AllocatedTask.AssignedResponder.Id  + "   " + AllocatedTask.TargetCasualty.ID);

			Mybody.Action=RespondersActions.AllocatResponders;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Planformulation  ; 
			Mybody.EndofCurrentAction=   InputFile.Allocation_duration ;
		}
		// ++++++ 24- +++++++
		else if (Mybody.Action==RespondersActions.AllocatResponders &&  Mybody.EndActionTrigger== RespondersTriggers.EndingAction  )
		{	
			if ( Mybody.assignedIncident.CasualtyReport_Mechanism_IN == CasualtyReportandTrackingMechanism.Electronic )
			{	
				AllocatedTask.Send_Am_AllocationTOCCS=true;
				ISRecord currentISRecord = Mybody.assignedIncident.ReturnRecordcasualtyISSystem( AllocatedTask.TargetCasualty );			
				currentISRecord.UpdateISRecord_Ambulance( AllocatedTask.AssignedResponder.Myvehicle );
			}

			AllocatedTask=null;
			Mybody.Action=RespondersActions.Noaction;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.WaitingDuring;
			Mybody.EndActionTrigger=null;
		}		
		//-------------------------------------------------------------------------------------------- (P5)
		// ++++++ 27- +++++++	
		else if (Mybody.Action==RespondersActions.Noaction  &&  Mybody.assignedIncident.AmbulanceTracking_Strategy==AmbulanceTracking.GPS  && Mybody.assignedIncident.ckeck_GPS_ArrivingSoonAmbulance(MyDrivers , UnoccupiedDrivers ,  UnoccupiedArrivingSoonDrivers, OccupiedArrivingSoonDrivers  , 3000 )  )	
		{							
			//Updateplan			
			UnoccupiedArrivingSoonDrivers.add( Mybody.assignedIncident.GPS_ArrivingSoonDriver);

			Mybody.Action=RespondersActions.UpdatePlan;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Planformulation;
			Mybody.EndofCurrentAction=  InputFile.UdatePlan_duration  ; 

			System.out.println("Loading:   " +Mybody.Id +" ArrivingSoonDrivers GPS ...... GPS GPS GPS GPS GPS GPS GPS GPS GPS GPS GPS GPS GPS GPS GPS GPS GPS GPS GPS GPS GPS GPS " +Mybody.assignedIncident.GPS_ArrivingSoonDriver.Id );
		}				
		//-----------------------------------------------End ----------------------------------------- (P5)
		// ++++++ 27- +++++++	
		else if  (  Mybody.Action==RespondersActions.Noaction && End_LA==2) 
		{			
			End_nomorec= 2 ;


			for (RoadLink   RL: Mymemory_routeNEEDCleartraffic_List  )
			if (   RL.TrafficLevel !=Level.None   )
				System.out.println("  "+"Loading: TR " + RL.fid +"  " + RL.descript_1 + " "  + RL.TrafficLevel);
			

			for (RoadLink   RL: Mymemory_routeNEEDClearwreckage_List  )
				if (  RL.WreckageLevel != Level.None   )
				System.out.println("  "+"Loading: WR " + RL.fid +"  " + RL.descript_1 + " "  + RL.WreckageLevel);
			

//			for (RoadLink   RL: Mybody.assignedIncident.nearObjects_RLwithTrandWR_whole  )
//			{
//				if (  RL.WreckageLevel != Level.None ||  RL.TrafficLevel !=Level.None   )
//				{
//					System.out.println("  "+"Loading: City " + RL.fid +"  " + RL.descript_1 + " TR  "  + RL.TrafficLevel + " WR  "  + RL.WreckageLevel);
//					RL.ColorCode=2;
//					if (RL.Parts.size() !=0 )
//						for ( TopographicArea  P : RL.Parts )
//							P.ColorCode=6;
//				}
//
//			}


			System.out.println("  "+"Loading: "  +" done loading*****************1*****************" ); 

			Mybody.InterpretedTrigger=RespondersTriggers.DoneLoadingActivity;
			Mybody.Action=RespondersActions.Noaction;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.WaitingEnd;

			System.out.println("  "+"Loading: " + " done loadingr*****************2*****************" ); 
		}


		//if(this.Mybody.Sending)   System.out.println("  "+"Loading: " +  Mybody.Id  +"  xxxxxx  "  +  Mybody.Action );
	}

	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	// ALC -  CoordinateEndResponse  
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	private void CommanderBehavior_EndER()
	{
		// ++++++ 1- +++++++
		if (Mybody.Action==RespondersActions.Noaction && Mybody.CommTrigger==RespondersTriggers.ENDER ) 
		{
			Mybody.Action=RespondersActions.GetNotificationEndER ;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Comunication ; 
			Mybody.EndofCurrentAction= InputFile.GetNotification_duration ;
			Mybody.CommTrigger=null; 

		}
		// ++++++ 2- +++++++
		else if (Mybody.Action==RespondersActions.GetNotificationEndER  && Mybody.EndActionTrigger== RespondersTriggers.EndingAction   )
		{
			// Send message to its driver
			BoradcastAction(ACLPerformative.InformERend );

			Mybody.Action=RespondersActions.BoradcastEndER ;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Comunication ;
			Mybody.EndofCurrentAction=   InputFile.Boradcast_duration ;
			Mybody.EndActionTrigger=null;					
		}
		// ++++++ 3- +++++++
		else if (Mybody.Action==RespondersActions.BoradcastEndER && Mybody.EndActionTrigger== RespondersTriggers.EndingAction  )
		{		
			Mybody.InterpretedTrigger=RespondersTriggers.ENDER; //make sure no still task

			Mybody.Action=RespondersActions.Noaction;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.WaitingEnd;
			Mybody.Sending=false; 
			Mybody.EndActionTrigger= null;
			System.out.println( "  "+Mybody.Id  +" GO back to Vehicle  " +Mybody.Role );
		}
	}

}//end class


