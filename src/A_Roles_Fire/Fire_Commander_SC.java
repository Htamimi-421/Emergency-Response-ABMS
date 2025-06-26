package A_Roles_Fire;
import java.util.ArrayList;
import java.util.List;
import A_Agents.Casualty;
import A_Agents.Responder;
import A_Agents.Responder_Ambulance;
import A_Agents.Responder_Fire;
import A_Environment.Cordon;
import A_Environment.RoadLink;
import A_Environment.Sector;
import A_Environment.TopographicArea;
import B_Classes.Task_Fire;
import B_Communication.ACL_Message;
import B_Communication.Command;
import B_Communication.Report;
import C_SimulationInput.InputFile;
import D_Ontology.Ontology; 
import D_Ontology.BuildStaticFuction;
import D_Ontology.Ontology.ACLPerformative;
import D_Ontology.Ontology.Allocation_Strategy;
import D_Ontology.Ontology.Allocation_Strategy_Route;
import D_Ontology.Ontology.Ambulance_ResponderRole;
import D_Ontology.Ontology.Ambulance_TaskType;
import D_Ontology.Ontology.CasualtyStatus;
import D_Ontology.Ontology.Fire_ResponderRole;
import D_Ontology.Ontology.Fire_TaskType;
import D_Ontology.Ontology.CommunicationMechanism;
import D_Ontology.Ontology.Communication_Time;
import D_Ontology.Ontology.Fire_ActivityType;
import D_Ontology.Ontology.GeneralTaskStatus;
import D_Ontology.Ontology.Inter_Communication_Structure;
import D_Ontology.Ontology.Police_TaskType;
import D_Ontology.Ontology.RandomWalking_StrategyType;
import D_Ontology.Ontology.RespondersActions;
import D_Ontology.Ontology.RespondersBehaviourTypes2;
import D_Ontology.Ontology.RespondersBehaviourTypes3;
import D_Ontology.Ontology.RespondersTriggers;
import D_Ontology.Ontology.TaskAllocationApproach;
import D_Ontology.Ontology.TaskAllocationMechanism;
import D_Ontology.Ontology.TypeMesg;
import D_Ontology.Ontology.UpdatePlanType;

public class Fire_Commander_SC {

	Responder_Fire Mybody;

	//----------------------------------------------------
	Responder_Fire NewResponder=null,FinshedResponder=null, AllocatedRespondertoTask=null;
	Casualty  CurrentCasualtybySender=null;
	RoadLink   CurrentRoutebySender=null;
	ArrayList<RoadLink> CurrentRoutebySender_List=new ArrayList<RoadLink>();

	Task_Fire  AllocatedTask ;
	int NoMoreCasualtyCounter=0;
	int NFirefighter=0 ,  SendNFirefighter=0 ;
	boolean StartSetup=false;
	boolean nowork= false;

	boolean FirstTime ;

	//----------------------------------------------------
	//  The meaning of attribute content....  -1:No update   , 0: Get  , 1: realize or done  2: send to workers or PIC
	int  EndSectorEstablished = -1 ; 	
	int CCSestablished = -1 ,RCestablished = -1 ;   // SA from other agency 
	int End_nomorec_LA= -1 ;
	int End_nomorec_inner=-1 ;
	int End_SR=-1 , ENDambSC=-1;
	int End_Wr=-1 ;
	int IsThereTrapped= -1;
	//----------------------------------------------------

	ArrayList<Casualty> Dead_List_inner =new ArrayList<Casualty>();
	ArrayList<Casualty> Trapped_List =new ArrayList<Casualty>();
	int AllocatedResponderfroSetup=0 ;
	List<Task_Fire> Operational_Plan = new ArrayList<Task_Fire>();
	List<Responder_Fire> MyResponders = new ArrayList<Responder_Fire>(); //its worker
	public List<Responder_Fire> UnoccupiedResponders = new ArrayList<Responder_Fire>(); //free worker

	List<Responder_Fire> NewRespondrsNotinfromedCCS = new ArrayList<Responder_Fire>(); //its worker
	List<Responder_Fire> NewRespondrsNotinfromedRC = new ArrayList<Responder_Fire>(); //its worker
	//----------------------------------------------------
	int xx=0;
	double Time_last_updated=0;	

	//##############################################################################################################################################################	
	public Fire_Commander_SC( Responder_Fire _Mybody  ) 
	{
		Mybody=_Mybody;
		Mybody.EndofCurrentAction=0;

		Mybody.CurrentAssignedMajorActivity_fr=Fire_ActivityType.None ;

		Mybody.PrvRoleinprint2 =Mybody.Role;
		
	}

	public void AssignSector(Sector  _AssignedSector )  // for sector commander only
	{
		Mybody.AssignedSector=_AssignedSector;
		Mybody.AssignedSector.FSCcommander=Mybody;

		if (Mybody.AssignedSector.ID==1 ) { Mybody.ColorCode=1 ; }   
		if (Mybody.AssignedSector.ID==2 ) { Mybody.ColorCode=2  ; }  
		if (Mybody.AssignedSector.ID==3 ) { Mybody.ColorCode=3 ;  }  
		if (Mybody.AssignedSector.ID==4 ) { Mybody.ColorCode= 4;  }  

		//if (Mybody.assignedIncident.TaskMechanism_IN==TaskAllocationMechanism.NoEnvironmentsectorization)  //no need to color
		//Mybody.AssignedSector.ColorCode= 0;
		Mybody.ColorCode=5;		
		Mybody.CurrentAssignedMajorActivity_fr=Fire_ActivityType.SearchandRescueCasualty ;
		Mybody.assignedIncident.FRSCcommander1=Mybody;   // temp because there are four  but I put this one becasue I have one right now
	}

	public void AssignCordon(Cordon  _AssignedCordon )  // for route  commander only
	{
		Mybody.AssignedCordon =_AssignedCordon ;
		Mybody.AssignedCordon.FSCcommander=Mybody;
		Mybody.assignedIncident.FRFSCcommander=Mybody;
		Mybody.ColorCode=6;
		Mybody.step_long =InputFile.step_long_ClearRoute;
		Mybody.CurrentAssignedMajorActivity_fr=Fire_ActivityType.ClearRoute ;
	}

	//##############################################################################################################################################################
	public void  Fire_Commander_SC_InterpretationMessage()
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
			if ( Mybody.CurrentSender instanceof Responder_Fire &&  Mybody.CurrentCommandRequest.commandType2 == Fire_TaskType.ClearRouteWreckage  )  // from from FIC-AIC
			{				
				Mybody.CommTrigger= RespondersTriggers.GetRouteProirtytoclearFromFIC ;
				CurrentRoutebySender_List =Mybody.CurrentCommandRequest.TargetRoad_List ;				
			}
			break;	

			//------------------------------- 	
		case InformLoadingandTransportationReport: // from... LAC
			// Clear Route
			Mybody.CommTrigger= RespondersTriggers.GetRouteProirtytoclearFromFIC ;
			Report CurrentReport=((Report) currentmsg.content);			
			CurrentRoutebySender_List  = CurrentReport.routeNEEDClearwreckage_List ;	
			break;

		case InformNewResponderArrival:
			Mybody.CommTrigger= RespondersTriggers.GetNewFirefighterarrived;	
			NewResponder= (Responder_Fire) Mybody.CurrentSender;	
			//System.out.println("                                       "  + Mybody.Id + "gettttttttttttttttttttttttttttt");
			break;
		case InformlocationArrival:
			Mybody.CommTrigger= RespondersTriggers.GetFirefighterComeback  ;
			FinshedResponder= (Responder_Fire) Mybody.CurrentSender;
			break;	
		case RequsteReallocateNResponders:
			Mybody.CommTrigger= RespondersTriggers.GetReallocateNFirefighter  ;
			NFirefighter = ((int)currentmsg.content);
			break;			
		case InfromCCSEstablished:	
			//Mybody.CommTrigger= RespondersTriggers.GetSAupdatefromFICBC  ; //broad cast
			Mybody.CommTrigger= RespondersTriggers.GetSAupdatefromFIC;
			CCSestablished =0;
			break;	
		case InfromRCEstablished:	// from FIC or RCO  
			//	Mybody.CommTrigger= RespondersTriggers.GetSAupdatefromFICBC ;  //broad cast
			Mybody.CommTrigger= RespondersTriggers.GetSAupdatefromFIC;
			RCestablished = 0;
			break;
		case InfromResultSetupSector:
			Mybody.CommTrigger= RespondersTriggers.GetresultofSetup;
			FinshedResponder= (Responder_Fire) Mybody.CurrentSender;
			break;
			//--------------------------------------
			// Search and Rescue 
		case InformResultCasualtyDead:
			Mybody.CommTrigger= RespondersTriggers.GetresultofCasualtyDead ;
			CurrentCasualtybySender = ((Casualty)currentmsg.content);
			FinshedResponder= (Responder_Fire) Mybody.CurrentSender; //wait with casualty or go if he dead
			break;
		case InformResultDirectingCasualty :
			Mybody.CommTrigger= RespondersTriggers.GetresultofDirectingCasualty ;
			CurrentCasualtybySender = ((Casualty)currentmsg.content);
			FinshedResponder= (Responder_Fire) Mybody.CurrentSender; //wait with casualty or go if he dead
			break;
		case InformResultCarryCasualtytoCCS:
			Mybody.CommTrigger= RespondersTriggers.GetresultofCarryCasualtytoCCS  ;
			CurrentCasualtybySender = ((Casualty)currentmsg.content);
			FinshedResponder= null; //until arrived location
			break;
			//--------------------------------------

		case InformEndFieldTriage:
			Mybody.CommTrigger= RespondersTriggers.GetSAupdatefromFIC ;
			ENDambSC=0;
			break;
		case	InformTrappedcasualty :  
			Mybody.CommTrigger= RespondersTriggers.ThereisTrappedcasualty ;
			CurrentCasualtybySender = ((Casualty)currentmsg.content);
			FinshedResponder= (Responder_Fire) Mybody.CurrentSender;
			//System.out.println(Mybody.Id + "  " + Mybody.CommTrigger + "  from  "+ Mybody.CurrentSender.Id  + "    Action:" + Mybody.Action);
			break;			
		case InformNomorecasualtyScene :  //Decentralized  firefighter 
			Mybody.CommTrigger= RespondersTriggers.NoMorecasualty; //Decentralized
			FinshedResponder= (Responder_Fire) Mybody.CurrentSender;
			xx++;
			break;
			//--------------------------------------	
			// Clear Route
		case InfromResultRoute :
			Mybody.CommTrigger= RespondersTriggers.GetResultCleardRoute;
			CurrentRoutebySender  = ((RoadLink)currentmsg.content);
			FinshedResponder= (Responder_Fire) Mybody.CurrentSender;
			break;		
			//--------------------------------------
			// Clear Route
		case InformNomorecasualty: // in LA..... from FIC-AIC  or LAC
			Mybody.CommTrigger= RespondersTriggers.GetSAupdatefromFIC ;
			End_nomorec_LA= 0 ; 
			//CurrentCCO = (Responder_Ambulance)currentmsg.sender; old 
			break;			
		case InformERend :
			Mybody.CommTrigger= RespondersTriggers.ENDER;
			//System.out.println("                                       "  + Mybody.Id + "   Fire Sector"+Mybody.AssignedSector.ID  +"===================================================================" + Mybody.assignedIncident.ID );
			break;	
		} // end switch

		//System.out.println(Mybody.Id + "  " + Mybody.CommTrigger + "  from  "+ Mybody.CurrentSender.Id  + "    Action:" + Mybody.Action);
	}	

	//##############################################################################################################################################################	
	//													Plan
	//##############################################################################################################################################################		
	// Create Plan-thinking for first time
	public void Implmenting_plan() {

		//used only CentralizedDirectSupervision only

		Mybody.Reset_DirectioninSearach( ); //its memory
		Mybody.ClockDirection=1;
		Mybody.CasualtySeen_list.clear();

		if (Mybody.assignedIncident.TaskMechanism_IN==TaskAllocationMechanism.NoEnvironmentsectorization)
		{								
			Mybody.Reset_DirectioninSearach( ); //its memory
			if (Mybody.Randomizer.nextInt(2)==0 ) Mybody.ClockDirection= 1; else Mybody.ClockDirection= -1; 
			Mybody.MyDirectiontowalk=Mybody.assignedIncident.IdentifyNearest_small(Mybody.Return_CurrentLocation());
			Mybody.NextRotateDirectionSearch_smallRoute(Mybody.ClockDirection);
			Mybody.walkingstrategy=RandomWalking_StrategyType.FourDirections_small;

		}
		else if (Mybody.assignedIncident.TaskMechanism_IN==TaskAllocationMechanism.Environmentsectorization)
		{		
			Mybody.MyDirectiontowalk=Mybody.AssignedSector.Limitpoint;
			Mybody.walkingstrategy=RandomWalking_StrategyType.OneDirection_sector;									
		}

		Mybody.Setup_ConsiderSeverityofcasualtyinSearach(true ); //  Severity
		Mybody.Assign_DestinationLocation_Serach();  					
		Mybody.StopDistnation=Mybody.AssignedSector.centerofsectorPoint;
		Mybody.ArrivedSTOP=false;
		Mybody.ExpandFieldofVision=false;

	};

	//***************************************************************************************************
	// Update Plan - add Casualty/Task
	public void UpdatePlan( UpdatePlanType UpdateType, Casualty _Casualty,RoadLink  _TargetRoute , Fire_TaskType _TaskType ,Responder_Fire _Responder ,boolean _Decased   , int _Priority ) 
	{
		switch( UpdateType) {
		case NewRes :
			MyResponders.add(_Responder);
			UnoccupiedResponders.add(_Responder);			
			if( Mybody.CurrentAssignedMajorActivity_fr==Fire_ActivityType.ClearRoute)   _Responder.ColorCode= 6 ;	
			//else
			//	_Responder.ColorCode=Mybody.AssignedSector.ID; //_Responder.ColorCode=Mybody.ColorCode ;
			break;
		case FreeRes:
			UnoccupiedResponders.add(_Responder);
			break;
		case RemoveRes:
			MyResponders.remove(_Responder);
			break;
		case NewTask:
			if (Mybody.CurrentAssignedMajorActivity_fr==Fire_ActivityType.SearchandRescueCasualty )
			{
				Task_Fire Newtask = new Task_Fire( _Casualty, _TaskType ); //TaskStatus.waiting
				Operational_Plan.add(Newtask);
			}
			else if (Mybody.CurrentAssignedMajorActivity_fr==Fire_ActivityType.ClearRoute )
			{				
				Task_Fire foundTask=null ;
				for (Task_Fire T : Operational_Plan) 
					if (T.TargetRoute == _TargetRoute && T.TaskType==_TaskType ) 
					{
						foundTask=T;
						break;
					}

				if ( foundTask==null  )
				{
					Task_Fire Newtask = new Task_Fire(  _TargetRoute , _TaskType ); //TaskStatus.waiting
					Operational_Plan.add(Newtask);
					Newtask.Priority_clearRoute= _Priority;								
				}
				else
				{
					foundTask.Priority_clearRoute= _Priority;	
				}



			}
			break;
		case AssignTask:			
			if (Mybody.CurrentAssignedMajorActivity_fr==Fire_ActivityType.SearchandRescueCasualty )
			{
				for (Task_Fire T : Operational_Plan) 
					if (T.TargetCasualty == _Casualty && T.TaskType==_TaskType && T.TaskStatus==GeneralTaskStatus.Waiting) 
					{
						T.TaskStatus=GeneralTaskStatus.Inprogress ;
						T.TaskStartTime = Mybody.CurrentTick; //Task.AssignedResponder in allocation
						UnoccupiedResponders.remove(T.AssignedResponder );
					}
			}
			else
			{
				for (Task_Fire T : Operational_Plan) 
					if (T.TargetRoute == _TargetRoute &&  ( T.TaskStatus==GeneralTaskStatus.Waiting || T.TaskStatus==GeneralTaskStatus.Inprogress )) 
					{
						T.TaskStatus=GeneralTaskStatus.Inprogress ;
						T.TaskStartTime = Mybody.CurrentTick; //Task.AssignedResponder in allocation

						T.AssignedResponderslist.add(_Responder);
						UnoccupiedResponders.remove(_Responder);
					}
			}

			break;
		case  CloseTask: 
			if (Mybody.CurrentAssignedMajorActivity_fr==Fire_ActivityType.SearchandRescueCasualty )
			{
				for (Task_Fire T : Operational_Plan) 
					if (T.TargetCasualty == _Casualty && T.TaskType==_TaskType) 
					{
						T.TaskStatus=GeneralTaskStatus.Done;	
						T.TaskEndTime = Mybody.CurrentTick;
						if ( _Decased== true)
							T.Decased=true;
					}
			}
			else
			{
				for (Task_Fire T : Operational_Plan) 
					if (T.TargetRoute == _TargetRoute && T.TaskType==_TaskType ) 
					{
						T.AssignedResponderslist.remove(_Responder);

						T.TaskStatus=GeneralTaskStatus.Done;	
						T.TaskEndTime = Mybody.CurrentTick;
					}
			}

			break;
			//=====================================
		case  TrackTask:	 //  Decentralized
			Task_Fire Newtask1 = new Task_Fire( _Casualty, _TaskType ,_Responder ); //done
			Operational_Plan.add(Newtask1);

			Newtask1.TaskStatus=GeneralTaskStatus.Done;	
			Newtask1.TaskEndTime = Mybody.CurrentTick;

			if ( _Decased== true)
				Newtask1.Decased=true;

			break;}
	}

	//***************************************************************************************************	
	//Execution_plane - Task Allocation	
	//casualty
	public Task_Fire Allocate_FireFighter( Fire_TaskType  Tasktypeofinterst ,  Allocation_Strategy Strategy) 
	{

		Task_Fire nominatedTask=null;
		Responder nominatedResp=null ;

		switch(Strategy) {

		case FIFO:			
			//FIFO casualty
			for (Task_Fire Task : Operational_Plan) 
				if (Task.TaskStatus == Ontology.GeneralTaskStatus.Waiting && Task.TaskType== Tasktypeofinterst)
				{
					Responder Resp =Task.ckeck_Distance_befor_TaskAssignment(UnoccupiedResponders);		
					nominatedTask=Task;
					nominatedResp=Resp;

					break;
				}

			break;
		case SmallestDistance:
			//Smallest distance
			double smallest_dis=999999 ; ; 

			for (Task_Fire Task : Operational_Plan)
				if (Task.TaskStatus == Ontology.GeneralTaskStatus.Waiting && Task.TaskType== Tasktypeofinterst )
				{
					Responder Resp=Task.ckeck_Distance_befor_TaskAssignment(UnoccupiedResponders);
					double dis = BuildStaticFuction.DistanceC(Task.TargetCasualty.geography, Resp.Return_CurrentLocation(),Task.TargetCasualty.getCurrentLocation());

					if (dis<smallest_dis)
					{ smallest_dis= dis; nominatedResp=Resp ;  nominatedTask=Task;}
				}

			break;
		case Severity_RYGPriorty:
			// identify more priority task-casualty- to assign nearest free responder
			double More_priorityRYG=100; 
			for (Task_Fire Task : Operational_Plan)
				if (Task.TaskStatus == GeneralTaskStatus.Waiting && Task.TaskType== Tasktypeofinterst)
				{	
					if (Task.TargetCasualty.Triage_tage < More_priorityRYG)
					{ More_priorityRYG=Task.TargetCasualty.Triage_tage  ;   nominatedTask=Task;}

					//nominatedResp=nominatedTask.ckeck_Distance_befor_TaskAssignment(UnoccupiedResponders);
				}

			Responder Resp=nominatedTask.ckeck_Distance_befor_TaskAssignment(UnoccupiedResponders);
			nominatedResp=Resp ;
			break;

		case Severity_RPMPriorty:
			// identify more priority task-casualty- to assign nearest free responder
			double More_priorityRPM=13 ; //100
			for (Task_Fire Task : Operational_Plan)
				if (Task.TaskStatus == D_Ontology.Ontology.GeneralTaskStatus.Waiting && Task.TaskType== Tasktypeofinterst )
				{
					if (Task.TargetCasualty.CurrentRPM < More_priorityRPM)
					{ More_priorityRPM=Task.TargetCasualty.CurrentRPM  ;  nominatedTask=Task;}

					nominatedResp=nominatedTask.ckeck_Distance_befor_TaskAssignment(UnoccupiedResponders);
				}

			break;}

		nominatedTask.AssignedResponder= (Responder_Fire) nominatedResp; 
		return nominatedTask ;

	}

	//Route
	public Task_Fire Allocate_FireFighter2( Fire_TaskType  Tasktypeofinterst ,  Allocation_Strategy_Route Strategy) 
	{

		Task_Fire nominatedTask=null;
		Responder nominatedResp=null ;

		switch(Strategy) {


		case FIFO:			
			//1- FIFO Route in list waiting
			for (Task_Fire Task : Operational_Plan) 
				if (Task.TaskStatus == Ontology.GeneralTaskStatus.Waiting && Task.TaskType== Tasktypeofinterst)
				{	
					nominatedTask=Task;					
					break;
				}
			// 2- then , in progress
			if ( nominatedTask== null ) 
			{
				int Min_resp=99 ;
				for (Task_Fire Task : Operational_Plan) 
					if (Task.TaskStatus == Ontology.GeneralTaskStatus.Inprogress && Task.TaskType== Tasktypeofinterst)
					{							
						if ( Task.AssignedResponderslist.size() <= Min_resp  )    // in this we consider the number of responders we can consider the lenght of route
						{ Min_resp=Task.AssignedResponderslist.size() ; nominatedTask=Task;	}				
					}
			}
			break;
		case SliverPriorty:			
			//1- look in waiting
			int MaxPriority = - 1; 
			double minDistance=99999999;
			for (Task_Fire Task : Operational_Plan) 
				if (Task.TaskStatus == Ontology.GeneralTaskStatus.Waiting && Task.TaskType== Tasktypeofinterst  &&  Task.Priority_clearRoute>=MaxPriority  )
				{	

					if( Task.Priority_clearRoute>MaxPriority )
					{
						nominatedTask=Task;	
						MaxPriority=Task.Priority_clearRoute ;
						minDistance=BuildStaticFuction.DistanceC(Mybody.geography, Mybody.Return_CurrentLocation(), Task.TargetRoute.PointofClear.getCurrentPosition());
					}

					if( Task.Priority_clearRoute==MaxPriority )
					{

						double discas=BuildStaticFuction.DistanceC(Mybody.geography, Mybody.Return_CurrentLocation(), Task.TargetRoute.PointofClear.getCurrentPosition());

						if(	discas < minDistance   )  
						{						
							nominatedTask=Task;	
							MaxPriority=Task.Priority_clearRoute ;					
							minDistance=discas ;
						}
					}

				}

			break;}
		return nominatedTask;
	}

	//##############################################################################################################################################################	
	//													Reporting
	//##############################################################################################################################################################	
	// sectorestablished/Safetybrifing  or End
	public void Reporting_plane1(ACLPerformative xx ) //report
	{	

		// Search and Rescue 
		//send message to FIC
		if ( xx ==ACLPerformative.InfromSafetyBriefandSectorEstablished   && Mybody.assignedIncident.Communication_Structure_uesd== Inter_Communication_Structure.Struc1_SilverCommandersInteraction  )
		{
			Mybody.CurrentMessage = new  ACL_Message( xx ,Mybody ,Mybody.assignedIncident.FICcommander,null, Mybody.CurrentTick ,Mybody.assignedIncident.ComMechanism_level_StoB,1,TypeMesg.External) ;
			Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
		}

		//send message to RCO  and AmSCs
		else if (xx ==ACLPerformative.InfromSafetyBriefandSectorEstablished  && Mybody.assignedIncident.Communication_Structure_uesd== Inter_Communication_Structure.Struc2_BronzeCommandersInteraction  )
		{			
			//Mybody.zzz=true ;
			Mybody.CurrentMessage  = new  ACL_Message( xx , Mybody , Mybody.assignedIncident.AmSCcommander1 ,null ,  Mybody.CurrentTick,Mybody.assignedIncident.Inter_Communication_Tool_used,1,TypeMesg.External) ;
			Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);

			Mybody.CurrentMessage  = new  ACL_Message( xx , Mybody , Mybody.assignedIncident.RCOcommander ,null ,  Mybody.CurrentTick,Mybody.assignedIncident.Inter_Communication_Tool_used,1 ,TypeMesg.External) ;
			Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);

		}



		if ( xx ==ACLPerformative.InformEndSearchandRescue_Nomorecasualty  && Mybody.assignedIncident.Communication_Structure_uesd== Inter_Communication_Structure.Struc1_SilverCommandersInteraction  )
		{
			Mybody.CurrentMessage = new  ACL_Message( xx ,Mybody ,Mybody.assignedIncident.FICcommander,null, Mybody.CurrentTick ,Mybody.assignedIncident.ComMechanism_level_StoB,1,TypeMesg.Inernal) ;
			Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
		}
		else if (xx ==ACLPerformative.InformEndSearchandRescue_Nomorecasualty && Mybody.assignedIncident.Communication_Structure_uesd== Inter_Communication_Structure.Struc2_BronzeCommandersInteraction  )
		{

			Mybody.CurrentMessage = new  ACL_Message( xx ,Mybody ,Mybody.assignedIncident.FICcommander,null, Mybody.CurrentTick ,Mybody.assignedIncident.ComMechanism_level_StoB,1,TypeMesg.Inernal) ;
			Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);

			Mybody.CurrentMessage  = new  ACL_Message( ACLPerformative.InformNomorecasualty_impact , Mybody , Mybody.assignedIncident.RCOcommander ,null ,  Mybody.CurrentTick,Mybody.assignedIncident.Inter_Communication_Tool_used,1,TypeMesg.External ) ;
			Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);

			Mybody.CurrentMessage  = new  ACL_Message( ACLPerformative.InformNomorecasualty_impact, Mybody , Mybody.assignedIncident.CCO_ambcommander ,null ,  Mybody.CurrentTick,Mybody.assignedIncident.Inter_Communication_Tool_used,1,TypeMesg.External ) ;
			Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);

			//			if  (Mybody.assignedIncident.AmSCcommander1 !=null  ) 
			//			{
			//				Mybody.CurrentMessage  = new  ACL_Message( ACLPerformative.InformNomorecasualty_impact , Mybody , Mybody.assignedIncident.AmSCcommander1 ,null ,  Mybody.CurrentTick,Mybody.assignedIncident.Inter_Communication_Tool_used ) ;				
			//				Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
			//			}
		}

		//--------------------------------------------------------------------------
		// Clear Route
		if (xx ==ACLPerformative.InformEndClearRouteWreckage    )
		{
			Mybody.CurrentMessage = new  ACL_Message( xx ,Mybody ,Mybody.assignedIncident.FICcommander,null, Mybody.CurrentTick ,Mybody.assignedIncident.ComMechanism_level_StoB,1,TypeMesg.Inernal) ;
			Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
		}

	}

	//***************************************************************************************************
	// List of trapped 
	public void Reporting_plane_TrappedReport(ACLPerformative xx ) //report  InformTrappedcasualty
	{	

		Report Report1=new Report() ;
		Report1.Fire_OPReport_SC( null , this.Trapped_List ) ;

		if (Mybody.assignedIncident.Communication_Structure_uesd== Inter_Communication_Structure.Struc1_SilverCommandersInteraction  )
		{
			Mybody.CurrentMessage = new  ACL_Message( xx ,Mybody ,Mybody.assignedIncident.FICcommander,Report1, Mybody.CurrentTick ,Mybody.assignedIncident.ComMechanism_level_StoB,Trapped_List.size(),TypeMesg.External) ;
			Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
		}
		else if ( Mybody.assignedIncident.Communication_Structure_uesd== Inter_Communication_Structure.Struc2_BronzeCommandersInteraction  )
		{

			if (  Mybody.assignedIncident.AmSCcommander1_ENDSR_INNER!=true  )  // ENDambSC != 1  //temp solution
			{	
				Mybody.CurrentMessage = new  ACL_Message( xx ,Mybody ,Mybody.assignedIncident.AmSCcommander1,Report1, Mybody.CurrentTick,Mybody.assignedIncident.Inter_Communication_Tool_used,Trapped_List.size(),TypeMesg.External) ;
				Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
			}
			else
			{
				Mybody.CurrentMessage = new  ACL_Message( xx ,Mybody ,Mybody.assignedIncident.CCO_ambcommander,Report1, Mybody.CurrentTick,Mybody.assignedIncident.Inter_Communication_Tool_used,Trapped_List.size(),TypeMesg.External) ;
				Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
			}

		}

	}

	//***************************************************************************************************
	// List of deceased  
	public void Reporting_plane_DecasedReport(ACLPerformative xx ) //report
	{	
		ArrayList<Casualty> casualties_Decased_List =new ArrayList<Casualty>();
		casualties_Decased_List.clear();

		for (Task_Fire T :Operational_Plan ) 
			if (T.TaskStatus == GeneralTaskStatus.Done && T.TaskType == Fire_TaskType.CarryCasualtytoCCS && T.SendDecasedReporttoFIC==false  &&   T.Decased  ) 
			{
				casualties_Decased_List.add(T.TargetCasualty);
				T.SendDecasedReporttoFIC=true;
			}

		Report Report1=new Report() ;
		Report1.Fire_OPReport_SC( casualties_Decased_List , null ) ;

		//System.out.println(Mybody.Id + "xxxxxxxxxxxxxxxxxxxxxxx Send Decased report " +casualties_Decased_List.size());


		if (Mybody.assignedIncident.Communication_Structure_uesd== Inter_Communication_Structure.Struc1_SilverCommandersInteraction  )
		{
			Mybody.CurrentMessage = new  ACL_Message( xx ,Mybody ,Mybody.assignedIncident.FICcommander,Report1, Mybody.CurrentTick ,Mybody.assignedIncident.ComMechanism_level_StoB,casualties_Decased_List.size(),TypeMesg.External) ;
			Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
		}
		else if ( Mybody.assignedIncident.Communication_Structure_uesd== Inter_Communication_Structure.Struc2_BronzeCommandersInteraction  )
		{
			Mybody.CurrentMessage = new  ACL_Message( xx ,Mybody ,Mybody.assignedIncident.RCOcommander,Report1, Mybody.CurrentTick,Mybody.assignedIncident.Inter_Communication_Tool_used,casualties_Decased_List.size(),TypeMesg.External) ;
			Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
		}


	}

	//***************************************************************************************************
	// List of Routes done
	public void Reporting_plane_RouteReportxxxxxxxxxxxxxx(ACLPerformative xx ) //report
	{	
		ArrayList<RoadLink> ClearedRoute_List =new ArrayList<RoadLink>();
		ClearedRoute_List.clear();

		for (Task_Fire T :Operational_Plan ) 
			if (T.TaskStatus == GeneralTaskStatus.Done && T.TaskType == Fire_TaskType.ClearRouteWreckage && T. SendRouteReporttoFIC==false  &&  T.AssignedResponderslist.size() ==0 ) 
			{
				ClearedRoute_List.add(T.TargetRoute);
				T. SendRouteReporttoFIC=true;
			}
		Report Report1 =new Report();
		Report1.Police_ReportRoutes( ClearedRoute_List) ;
		//System.out.println(Mybody.Id + "Send Deceased report " +casualties_Decased_List.);


		if (Mybody.assignedIncident.Communication_Structure_uesd== Inter_Communication_Structure.Struc1_SilverCommandersInteraction  )
		{
			//Mybody.CurrentMessage = new  ACL_Message( xx ,Mybody ,Mybody.assignedIncident.FICcommander,Report1 , Mybody.CurrentTick ,Mybody.assignedIncident.ComMechanism_level_StoB,ClearedRoute_List.size()) ;
			Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
		}
		else if ( Mybody.assignedIncident.Communication_Structure_uesd== Inter_Communication_Structure.Struc2_BronzeCommandersInteraction  )
		{
			//Mybody.CurrentMessage = new  ACL_Message( xx ,Mybody ,Mybody.assignedIncident.ALCcommander,Report1 , Mybody.CurrentTick ,Mybody.assignedIncident.ComMechanism_level_StoB,ClearedRoute_List.size()) ;
			Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
		}


	}

	//***************************************************************************************************
	//Execution_plane  - commanding	
	public void Command_FireFighter(Task_Fire _Task , Responder_Fire  _Responder ) {

		Command CMD1 =new Command();

		if (_Task== null )
		{	// send message with command
			CMD1.FireCommand("0" ,Fire_TaskType.SetupSector   );
			Mybody.CurrentMessage = new  ACL_Message( ACLPerformative.Command , Mybody, _Responder ,CMD1, Mybody.CurrentTick,Mybody.assignedIncident.ComMechanism_level_BtoRes,1  ,TypeMesg.Inernal ) ;
			Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
		}
		else if ( _Task.TaskType == Fire_TaskType.CarryCasualtytoCCS )
		{	
			CMD1.FireCommand(_Task.TaskID ,_Task.TaskType ,_Task.TargetCasualty );
			Mybody.CurrentMessage = new  ACL_Message( ACLPerformative.Command , Mybody, _Task.AssignedResponder ,CMD1, Mybody.CurrentTick,Mybody.assignedIncident.ComMechanism_level_BtoRes,1  ,TypeMesg.Inernal ) ;
			Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
		}
		else if ( _Task.TaskType == Fire_TaskType.ClearRouteWreckage )
		{
			CMD1.FireCommand(_Task.TaskID ,_Task.TaskType ,_Task.TargetRoute );
			Mybody.CurrentMessage = new  ACL_Message( ACLPerformative.Command , Mybody,_Responder ,CMD1, Mybody.CurrentTick,Mybody.assignedIncident.ComMechanism_level_BtoRes,1  ,TypeMesg.Inernal ) ;
			Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
		}

	} 

	//***************************************************************************************************
	//Broadcast  FIC updates or End
	public void BoradcastAction( ACLPerformative xx  , List<Responder_Fire> _Responders )
	{		
		Mybody.CurrentMessage  = new  ACL_Message( xx, Mybody, _Responders ,null, Mybody.CurrentTick ,CommunicationMechanism.FF_BoradCast,1 ,TypeMesg.Inernal) ;
		Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
		// break; //one by one 

		//		System.out.println(Mybody.Id + "xxxxxxxxxxxxxxxxxxxx " +_Responders .size() );
		//		for (  Responder_Fire  R: _Responders )
		//		System.out.println(Mybody.Id + "==========> " +R.Id);	
	}

	//##############################################################################################################################################################	
	//													Interpreted Triggers 
	//##############################################################################################################################################################
	//1 Casualty 
	public boolean ThereisWaitingTask(Fire_TaskType  Tasktypeofinterst)
	{	
		boolean  Result=false;

		for (Task_Fire T : Operational_Plan) 
			if (T.TaskStatus == GeneralTaskStatus.Waiting && T.TaskType== Tasktypeofinterst  ) 
			{
				Result=true;
				//System.out.println(Mybody.Id + " ++++++++++++++++++++++ "  +T.TaskType + "   " + T.TargetCasualty.ID + "   " + T.AssignedResponder );
				break;
			}

		return Result;
	}

	//----------------------------------------------------------------------------------------------------
	//2
	public boolean IsAllTaskclosed1(Fire_TaskType  Tasktypeofinterst)
	{	
		boolean  Result=true;

		if ( Operational_Plan.size() ==0)
			Result=false;

		for (Task_Fire Task : Operational_Plan) 
			if (Task.TaskStatus != GeneralTaskStatus.Done && Task.TaskType== Tasktypeofinterst   ) 
			{
				Result=false;
				break;
			}

		return Result;
	}

	public boolean AllTaskclosedprint()
	{	
		boolean  Result=true;

		if ( Operational_Plan.size() ==0)
			Result=false;

		for (Task_Fire Task : Operational_Plan) 
			if (Task.TaskStatus != GeneralTaskStatus.Done && Task.TaskType == Fire_TaskType.CarryCasualtytoCCS  ) 
			{
				System.out.println(Mybody.Id + " ^^^^^^^^  "  +Task.TaskType + "   " + Task.TargetCasualty.ID + "   " + Task.AssignedResponder );
				break;
			}

		return Result;
	}

	//----------------------------------------------------------------------------------------------------
	//3
	public boolean Isthere_DecasedReport() //for report
	{	
		boolean result=false;

		for (Task_Fire T :Operational_Plan ) 
			if (T.TaskStatus == GeneralTaskStatus.Done && T.TaskType == Fire_TaskType.CarryCasualtytoCCS && T.SendDecasedReporttoFIC==false  &&   T.Decased ) 

			{
				result=true; break;
			}
		return result;
	}

	//----------------------------------------------------------------------------------------------------
	//4
	public boolean Isthere_RouteReport() //for report
	{	
		boolean result=false;

		for (Task_Fire T :Operational_Plan ) 
			if (T.TaskStatus == GeneralTaskStatus.Done && T.TaskType == Fire_TaskType.ClearRouteWreckage && T. SendRouteReporttoFIC==false &&  T.AssignedResponderslist.size() ==0 ) 
			{
				result=true; break;
			}
		return result;
	}

	//----------------------------------------------------------------------------------------------------

	//6 Traffic Route
	public boolean IsThereWreckageRoute_ToAssign()
	{	
		boolean  Result=false;

		for (Task_Fire T : Operational_Plan) 
			if ((T.TaskStatus == GeneralTaskStatus.Waiting || T.TaskStatus == GeneralTaskStatus.Inprogress)  && T.TaskType== Fire_TaskType.ClearRouteWreckage ) 
			{
				Result=true;
				break;
			}

		return Result;
	}

	//----------------------------------------------------------------------------------------------------
	public boolean IsCurrentRouteClosed( RoadLink  RL)
	{	
		boolean  Result= false;

		for (Task_Fire T : Operational_Plan) 
			if (T.TaskStatus == GeneralTaskStatus.Done && RL==T.TargetRoute &&  T.AssignedResponderslist.size() == 0  ) 
			{
				Result=true;
				//System.out.println(Mybody.Id + " ++++++++++++++++++++++ "  +T.TaskType + "   " + T.TargetCasualty.ID + "   " + T.AssignedResponder );
				break;
			}

		return Result;
	}

	//----------------------------------------------------------------------------------------------------		
	public boolean ISthereupdateTosend_SR( )
	{	
		boolean  Result= false;

		if ( EndSectorEstablished==1  )
		{  Result=true; }
		else if ( Isthere_DecasedReport()  )
		{  Result=true; }
		else if ( IsThereTrapped== 1  )
		{  Result=true; }

		if ( End_SR< 2 &&
				(
						(  IsAllTaskclosed1(Fire_TaskType.CarryCasualtytoCCS) && UnoccupiedResponders.size()== this.MyResponders.size()  &&  Mybody.assignedIncident.TaskApproach_IN ==TaskAllocationApproach.CentralizedDirectSupervision &&  ( Mybody.SensingeEnvironmentTrigger== RespondersTriggers.Arrived_WalkedinAllScene || Mybody.SensingeEnvironmentTrigger == RespondersTriggers.Arrived_WalkedalongOneDirection))
						|| ( End_nomorec_inner== 1   && ( Mybody.assignedIncident.TaskApproach_IN ==TaskAllocationApproach.Decentralized_Autonomous ||Mybody.assignedIncident.TaskApproach_IN ==TaskAllocationApproach.Decentralized_Mutualadjustment)) 
						)
				) 
		{  Result=true;  End_SR=1 ;}


		return Result;
	}

	//----------------------------------------------------------------------------------------------------		
	public boolean ISthereupdateTosend_Wr( )
	{	
		boolean  Result= false;

		//if ( Isthere_RouteReport()  )
		//{  Result=true; }


		if (  End_Wr!=2 && End_nomorec_LA==1   && IsAllTaskclosed1(Fire_TaskType.ClearRouteWreckage) )
		{  Result=true;  End_Wr=1 ;}		

		this.Reporting_plane1(ACLPerformative.InformEndClearRouteWreckage);
		return Result;
	}	

	//##############################################################################################################################################################
	public void TaskApproach_way1(  )
	{
		if ( Mybody.assignedIncident.TaskApproach_IN ==TaskAllocationApproach.CentralizedDirectSupervision &&  Mybody.AssignedSector.installed    )  //  EndSectorEstablished>=1 it means it is established and update the worker
		{
			Mybody.RandomllySearachCasualty= true;
			Mybody.Action=RespondersActions.SearchCasualty;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.TaskPlanning ; //Planformulation;
		}
		else
		{
			Mybody.Action=RespondersActions.Noaction;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.WaitingDuring;			
		}
	}

	public void TaskApproach_way2(  )
	{
		if (   ! Mybody.NoMoreRouteinScene && ! IsThereWreckageRoute_ToAssign() &&   ( Mybody.AssignedRoute==null )    )   // &&  Mybody.SensingeEnvironmentTrigger!= RespondersTriggers.SensedRoute
		{
			Mybody.Action=RespondersActions.SearchRoute;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.TaskPlanning ;  //Planformulation;

			Mybody.Assign_DestinationLocation_Serach(); 
			Mybody.SensingeEnvironmentTrigger=null;
		}
		else
		{
			Mybody.Action=RespondersActions.Noaction;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.WaitingDuring;			
		}
	}
	//______________________________________________________________________________________________________________________________________________________________
	//##############################################################################################################################################################
	//                                                        Behavior
	//##############################################################################################################################################################
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	// Fire Sector Commander - General 
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	public void FSCBehavior()	
	{		
		//***********************************1**************************************	
		// ** Scene Operation ***

		if( Mybody.CurrentAssignedMajorActivity_fr==Fire_ActivityType.SearchandRescueCasualty ) 				
		{
			// 1- initial response
			if( Mybody.CommTrigger==RespondersTriggers.AssigendRolebyFIC)
			{
				Mybody.CurrentAssignedActivity=Fire_TaskType.GoTolocation;	
			}
			// 2- EXtrication and  carry
			else if (Mybody.CurrentAssignedActivity==Fire_TaskType.GoTolocation &&  Mybody.InterpretedTrigger== RespondersTriggers.FirstTimeonLocation	) 
			{
				Mybody.CurrentAssignedActivity=Fire_TaskType.CoordinateSceneOperation;
			}

			// 3- GoBackToControlArea
			else if ( Mybody.InterpretedTrigger==RespondersTriggers.DoneSearchCasualtyActivity )  
			{
				Mybody.CurrentAssignedActivity=Fire_TaskType.None;
				Mybody.CurrentAssignedMajorActivity_fr=null;

				Mybody.InterpretedTrigger=RespondersTriggers.DoneActivity ;				
				Mybody.Role=Fire_ResponderRole.None;
				//System.out.println( Mybody.Id  +" done scene "     );
			}


		}
		//***********************************2**************************************	
		// ** Clear Route ***

		if ( Mybody.CurrentAssignedMajorActivity_fr==Fire_ActivityType.ClearRoute  ) 				
		{
			// 1- initial response
			if( Mybody.CommTrigger==RespondersTriggers.AssigendRolebyFIC )
			{
				Mybody.CurrentAssignedActivity=Fire_TaskType.GoTolocation;
				Mybody.step_long =InputFile.step_long_ClearRoute;
			}
			// 2- Clear Route
			else if (Mybody.CurrentAssignedActivity==Fire_TaskType.GoTolocation &&  Mybody.InterpretedTrigger== RespondersTriggers.FirstTimeonLocation) 
			{
				Mybody.CurrentAssignedActivity=Fire_TaskType.CoordinateClearRoute;
			}
			// 3-No action
			else if (Mybody.InterpretedTrigger==RespondersTriggers.DoneClearRouteActivity )  
			{	
				Mybody.CurrentAssignedActivity=Fire_TaskType.None;
				Mybody.CurrentAssignedMajorActivity_fr=Fire_ActivityType.None ;

				Mybody.Action=RespondersActions.Noaction;
				Mybody.InterpretedTrigger=null;


				//System.out.println( Mybody.Id  +" done route zzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz "   );
			}
		}
		//************************************Final*************************************
		// Ending
		if ( Mybody.CommTrigger==RespondersTriggers.ENDER  && Mybody.CurrentAssignedActivity==Fire_TaskType.None  )   
		{
			Mybody.CurrentAssignedActivity=  Fire_TaskType.CoordinateEndResponse ;
			Mybody.PrvRoleinprint2=Mybody.Role ;
		}
		else if ( Mybody.InterpretedTrigger==RespondersTriggers.ENDER   )   
		{
			Mybody.PrvRoleinprint2=Mybody.Role ;
			Mybody.Role=Fire_ResponderRole.None;
			Mybody.step_long =InputFile.step_long_regularewalk;
		}

		//*************************************************************************
		switch(Mybody.CurrentAssignedActivity) {
		case GoTolocation:
			FSCCommanderBehavior_GoTosectorScene()	;	
			break;
		case CoordinateClearRoute :			
			FSCCommanderBehavior_CoordinateClearRouteWreckage();
			break;	
		case CoordinateSceneOperation :			
			FSCCommanderBehavior_CoordinateSceneOperation();
			break;	
		case CoordinateEndResponse :
			CommanderBehavior_EndER() ;
			break;
		case None:
			;
			break;}

		//if ( zzz== true ) System.out.println(Mybody.Id+"    Action:" + Mybody.Action + " com:" + Mybody.CommTrigger + "   Nt:" + Mybody.InterpretedTrigger+ " en:" + Mybody.SensingeEnvironmentTrigger + " end:" + Mybody.EndActionTrigger +  "Acknowledged: "+ Mybody.Acknowledged+"sending: "+  Mybody.Sending  + "CurrentMessage: ");

	}// end ParamedicBehavior

	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	// FSC - Go to location   both( Decentralized -Centralized )
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^	
	private void FSCCommanderBehavior_GoTosectorScene()	
	{
		// ++++++ 1- +++++++
		if( Mybody.CommTrigger==RespondersTriggers.AssigendRolebyFIC   )
		{	
			if ( Mybody.CurrentAssignedMajorActivity_fr==Fire_ActivityType.SearchandRescueCasualty )
			{
				Mybody.Assign_DestinationCordon(Mybody.AssignedSector.centerofsectorPoint);	
				Mybody.Action=RespondersActions.GoToSectorScene;
			}
			else if ( Mybody.CurrentAssignedMajorActivity_fr==Fire_ActivityType.ClearRoute )
			{
				Mybody.Assign_DestinationCordon(Mybody.AssignedCordon.EnteryPointAccess_Point);	
				Mybody.Action=RespondersActions.GoToCordon ;
			}

			Mybody.BehaviourType2=RespondersBehaviourTypes2.Movementonincidentsite ;  
			Mybody.CommTrigger=null;

		}
		// ++++++ 2- +++++++
		else if ( Mybody.Action==RespondersActions.GoToSectorScene && Mybody.SensingeEnvironmentTrigger==null)
		{			
			if ( Mybody.AssignedSector.AmIinSector(Mybody) && Mybody.assignedIncident.TaskApproach_IN==TaskAllocationApproach.CentralizedDirectSupervision )
				Mybody.SensingeEnvironmentTrigger= RespondersTriggers.ArrivedTargetObject;

			Mybody.Walk();
		}
		// ++++++ 3- +++++++
		else if ( Mybody.Action==RespondersActions.GoToCordon && Mybody.SensingeEnvironmentTrigger==null)
		{			
			Mybody.Walk();
		}
		// ++++++ 4- +++++++
		else if (( Mybody.Action==RespondersActions.GoToSectorScene || Mybody.Action==RespondersActions.GoToCordon )
				&& Mybody.SensingeEnvironmentTrigger== RespondersTriggers.ArrivedTargetObject  ) 
		{ 					
			//send message to FIC				
			Mybody.CurrentMessage  = new  ACL_Message( ACLPerformative.InformlocationArrival , Mybody, Mybody.assignedIncident.FICcommander ,null ,  Mybody.CurrentTick ,Mybody.assignedIncident.ComMechanism_level_StoB,1 ,TypeMesg.Inernal ) ;

			Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
			Time_last_updated=Mybody.CurrentTick;

			Mybody.Action=RespondersActions.NotifyArrival;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.Comunication ;  
			Mybody.SensingeEnvironmentTrigger=null;			

		}	
		// ++++++ 5- +++++++
		else if (Mybody.Action==RespondersActions.NotifyArrival && Mybody.Acknowledged)
		{
			Mybody.InterpretedTrigger= RespondersTriggers.FirstTimeonLocation;
			Mybody.Acknowledged=false;Mybody.Sending=false; 

			if ( Mybody.CurrentAssignedMajorActivity_fr==Fire_ActivityType.SearchandRescueCasualty ) 
				System.out.println("                                       "  + "Sector: " + Mybody.Id + " Arrived  my sector" );
			else 
			{
				System.out.println("                                       "  + "WrK: " + Mybody.Id + " Arrived  my route" );
				FirstTime=true;
			}
		}
	}		

	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	// FSC -  CoordinateCoordinateSceneOperation ( Decentralized -Centralized  )
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	private void  FSCCommanderBehavior_CoordinateSceneOperation()
	{		
		// ++++++ Move +++++++	
		if ( ! ThereisWaitingTask(Fire_TaskType.CarryCasualtytoCCS) && Mybody.Action==RespondersActions.SearchCasualty && Mybody.RandomllySearachCasualty==true && Mybody.SensingeEnvironmentTrigger ==null   &&  Mybody.CommTrigger ==null && Mybody.InterpretedTrigger==null && Mybody.EndActionTrigger==null && Mybody.EndofCurrentAction == 0 && Mybody.Sending==false  )
		{
			Mybody.Walk();	
			//System.out.println("                                       "  +Mybody.Id + "  walk "+ this.MyResponders.size());
		}
		// ++++++ Move +++++++

		//--------------------------------------------------------------------------------------------	
		// ++++++ 1- +++++++
		if (Mybody.InterpretedTrigger== RespondersTriggers.FirstTimeonLocation)
		{

			if (Mybody.assignedIncident.TaskApproach_IN==TaskAllocationApproach.CentralizedDirectSupervision)
				Implmenting_plan(); //how to walk
			else
				NoMoreCasualtyCounter=0; ; //nothing Stop in center of sector


				StartSetup=true;
				Mybody.Action=RespondersActions.FormulatePlan;
				Mybody.BehaviourType2=RespondersBehaviourTypes2.Planformulation  ;  
				Mybody.EndofCurrentAction=  InputFile.FormulatePlan_duration  ; 
				Mybody.InterpretedTrigger=null; 
		}
		// ++++++ 2- +++++++
		else if ( Mybody.Action==RespondersActions.FormulatePlan  && Mybody.EndActionTrigger== RespondersTriggers.EndingAction 	) 			 	
		{
			TaskApproach_way1(  );
			Mybody.EndActionTrigger=null;

		}
		//-------------------------------------------------------------------------------------------- (P1)
		//==========================New / Back Responder ========================== Both  N/A   face to face
		// ++++++ 3- +++++++
		else if ( ( Mybody.Action==RespondersActions.Noaction  ||  Mybody.Action==RespondersActions.SearchCasualty )&& ( Mybody.CommTrigger == RespondersTriggers.GetNewFirefighterarrived ||Mybody.CommTrigger == RespondersTriggers.GetFirefighterComeback  ) )	//come back from transfer	  										
		{	 	
			if (Mybody.CommTrigger == RespondersTriggers.GetNewFirefighterarrived )
			{ UpdatePlan( UpdatePlanType.NewRes, null, null, null ,NewResponder , false ,0) ; NewRespondrsNotinfromedCCS.add(NewResponder);  NewRespondrsNotinfromedRC.add(NewResponder);
			//System.out.println("                                       "  +"Sector: "+  NewResponder.Id  +"     " +MyResponders.size()  );

			}

			else if (Mybody.CommTrigger == RespondersTriggers.GetFirefighterComeback )
				UpdatePlan( UpdatePlanType.FreeRes,null, null, null ,FinshedResponder ,false,0) ;

			Mybody.Action=RespondersActions.GetArrivalNotification ;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.Comunication ;  
			Mybody.EndofCurrentAction=  InputFile.GetNotification_duration ; 
			Mybody.CommTrigger=null; 	

		}
		// ++++++ 4- +++++++
		else if ( Mybody.Action==RespondersActions.GetArrivalNotification  && Mybody.EndActionTrigger== RespondersTriggers.EndingAction ) 			 	
		{
			Mybody.CurrentSender.Acknowledg(Mybody);	

			Mybody.Action=RespondersActions.UpdatePlan;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.Planformulation  ;  
			Mybody.EndofCurrentAction=  InputFile.UdatePlan_duration  ; 
			Mybody.EndActionTrigger=null; 
		}
		//==========================Result of Setup sector ==========================  Both N/A
		// ++++++ 5- +++++++
		else if (( Mybody.Action==RespondersActions.Noaction  ||  Mybody.Action==RespondersActions.SearchCasualty )&& Mybody.CommTrigger== RespondersTriggers.GetresultofSetup )	  										
		{	 	
			//Update plan
			UpdatePlan( UpdatePlanType.FreeRes, null,null, null ,FinshedResponder,false,0); 
			EndSectorEstablished = 0 ;
			Mybody.AssignedSector.installed=true ;
			AllocatedResponderfroSetup --;

			if  ( AllocatedResponderfroSetup == 0)
			{
				EndSectorEstablished = 1 ;
				Mybody.assignedIncident.SB=Mybody.CurrentTick ;
				StartSetup=false;
				System.out.println("                                       "  +"Sector: "+  Mybody.Id  +"  Done Setup   " +MyResponders.size()  );

//				// temp	for print			
//				List<Casualty> nearObjects_Casualty  = (List<Casualty>) Mybody.AssignedSector.GetObjectsWithinSector(Mybody.assignedIncident.casualties);
//				for (int i = 0; i < nearObjects_Casualty.size(); i++) 
//				{
//					nearObjects_Casualty.get(i).ColorCode= 7 ;	
//					System.out.println( "new Coordinate ("+ nearObjects_Casualty.get(i).getCurrentLocation().x+ ","+ nearObjects_Casualty.get(i).getCurrentLocation().y + ")," );	
//				}
			}

			Mybody.Action=RespondersActions.GetResultofSetupTA ;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.Comunication  ;  
			Mybody.EndofCurrentAction=  InputFile.GetNotification_duration ; 
			Mybody.CommTrigger=null; 

		}
		// ++++++ 6- +++++++
		else if ( Mybody.Action==RespondersActions.GetResultofSetupTA  && Mybody.EndActionTrigger== RespondersTriggers.EndingAction 	) 			 	
		{
			Mybody.CurrentSender.Acknowledg(Mybody);	

			Mybody.Action=RespondersActions.UpdatePlan;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.Planformulation  ;  
			Mybody.EndofCurrentAction=  InputFile.UdatePlan_duration  ; 
			Mybody.EndActionTrigger=null; 
		}

		//==========================Result of Dead or Guide ========================== Both 
		// ++++++ 7- +++++++
		else if ( ( Mybody.Action==RespondersActions.Noaction  ||  Mybody.Action==RespondersActions.SearchCasualty ) &&  ( Mybody.CommTrigger== RespondersTriggers.GetresultofCasualtyDead || Mybody.CommTrigger== RespondersTriggers.GetresultofDirectingCasualty) ) //|| Mybody.assignedIncident.NewRecordcasualtyadded_ISSystem(Mybody.Role,Mybody.AssignedSector ,2)>0	 ) 
		{		

			Mybody.Action=RespondersActions.GetresultofCasualtyDeadorGuid ;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.Comunication;   

			if ( Mybody.CommTrigger== RespondersTriggers.GetresultofCasualtyDead  || Mybody.CommTrigger== RespondersTriggers.GetresultofDirectingCasualty )
				Mybody.EndofCurrentAction= InputFile.GetInfromation_FtoForR_Duration_Notification ; 

		}
		// ++++++ 8- +++++++
		else if ( Mybody.Action==RespondersActions.GetresultofCasualtyDeadorGuid   && Mybody.EndActionTrigger== RespondersTriggers.EndingAction 	) 			 	
		{					
			Casualty ca=null;
			Responder_Fire Res=null;

			//============ 1 ============ 
			Mybody.CurrentSender.Acknowledg(Mybody);				
			ca= CurrentCasualtybySender ;
			Res=(Responder_Fire) Mybody.CurrentSender;		

			//============ 2 ============ 
			//UpdatePlan	
			if ( Mybody.assignedIncident.TaskApproach_IN ==TaskAllocationApproach.CentralizedDirectSupervision)
			{
				if ( Mybody.CommTrigger== RespondersTriggers.GetresultofCasualtyDead )
					UpdatePlan( UpdatePlanType.CloseTask, ca ,null, Fire_TaskType.CarryCasualtytoCCS, null ,true ,0);	
				if ( Mybody.CommTrigger== RespondersTriggers.GetresultofDirectingCasualty )
					UpdatePlan( UpdatePlanType.CloseTask, ca ,null, Fire_TaskType.CarryCasualtytoCCS, null ,false,0 );

				UpdatePlan( UpdatePlanType.FreeRes, null, null,null ,Res,false ,0) ;

			}
			else
			{				
				if ( Mybody.CommTrigger== RespondersTriggers.GetresultofCasualtyDead )
					UpdatePlan( UpdatePlanType.TrackTask, ca ,null, Fire_TaskType.CarryCasualtytoCCS, null ,true ,0);	
				if ( Mybody.CommTrigger== RespondersTriggers.GetresultofDirectingCasualty )
					UpdatePlan( UpdatePlanType.TrackTask, ca ,null, Fire_TaskType.CarryCasualtytoCCS, null ,false,0 );

				UpdatePlan( UpdatePlanType.FreeRes, null, null,null ,Res,false ,0) ;
			}

			Mybody.Action=RespondersActions.UpdatePlan;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.Planformulation ; 
			Mybody.EndofCurrentAction= InputFile.UdatePlan_duration ; 
			Mybody.EndActionTrigger=null; 
			Mybody.CommTrigger=null; 

		}
		//==========================Result of carry casualty ==========================	Both	
		// ++++++ 9- +++++++
		else if ( ( Mybody.Action==RespondersActions.Noaction  ||  Mybody.Action==RespondersActions.SearchCasualty ) &&  Mybody.CommTrigger== RespondersTriggers.GetresultofCarryCasualtytoCCS    )	//|| Mybody.assignedIncident.NewRecordcasualtyadded_ISSystem(Mybody.Role,Mybody.AssignedSector,4)>0	  )	  										
		{	 				
			//System.out.println("                                       "  +"Sector: "+  Mybody.Id  +"get transferd "   );
			Mybody.Action=RespondersActions.GetresultofCarryCasualtytoCCS; 
			if ( Mybody.CommTrigger== RespondersTriggers.GetresultofCarryCasualtytoCCS)
				Mybody.EndofCurrentAction= InputFile.GetInfromation_FtoForR_Duration_Notification ; 

			Mybody.BehaviourType2=RespondersBehaviourTypes2.Comunication;   		
		}
		// ++++++ 10- +++++++
		else if ( Mybody.Action==RespondersActions.GetresultofCarryCasualtytoCCS  && Mybody.EndActionTrigger== RespondersTriggers.EndingAction 	) 			 	
		{		
			Casualty ca=null;
			Responder_Fire Res=null;

			//============ 1 ============ 		
			Mybody.CurrentSender.Acknowledg(Mybody);	
			//System.out.println(Mybody.Id + " get Transfer result of D or C   P " + CurrentCasualtybySender.ID);
			ca= CurrentCasualtybySender ;
			Res=(Responder_Fire) Mybody.CurrentSender;		
			Mybody.CommTrigger=null; 	
			//============ 2 ============ 
			//UpdatePlan	
			if ( Mybody.assignedIncident.TaskApproach_IN ==TaskAllocationApproach.CentralizedDirectSupervision)
			{
				UpdatePlan( UpdatePlanType.CloseTask, CurrentCasualtybySender , null,Fire_TaskType.CarryCasualtytoCCS, null,false ,0);	// Will be free when he come back
			}
			else
			{
				UpdatePlan( UpdatePlanType.TrackTask, CurrentCasualtybySender , null,Fire_TaskType.CarryCasualtytoCCS, null,false ,0);	// Will be free when he come back
			}

			Mybody.Action=RespondersActions.UpdatePlan;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.Planformulation ;  
			Mybody.EndofCurrentAction= InputFile.UdatePlan_duration ; 
			Mybody.EndActionTrigger=null; 

		}
		//==========================Updates from FIC ==========================		
		// ++++++ 11- +++++++
		else if ( ( Mybody.Action==RespondersActions.Noaction  ||  Mybody.Action==RespondersActions.SearchCasualty ) &&  Mybody.CommTrigger== RespondersTriggers.GetSAupdatefromFIC    )											
		{	 							 
			//Updates about SA
			if ( CCSestablished == 0   )  
				CCSestablished = 1 ;	
			else if ( RCestablished == 0   ) 
				RCestablished=1 ;
			if ( ENDambSC==0 )
				ENDambSC=1;
			Mybody.Action=RespondersActions.GetSAupdatefromFIC  ;		
			Mybody.BehaviourType2=RespondersBehaviourTypes2.Comunication  ;  
			Mybody.EndofCurrentAction= InputFile.GetInfromation_FtoForR_Duration_Data ; 	

			//System.out.println("                                       "  +"Sector: "+  Mybody.Id  +" GetSAupdatefromFIC   "   );
		}
		//==========================Get request to Reallocate N Firefighter ========================== Both  N/A 
		// ++++++ 12- +++++++
		else if (( Mybody.Action==RespondersActions.Noaction  ||  Mybody.Action==RespondersActions.SearchCasualty )&&  Mybody.CommTrigger== RespondersTriggers.GetReallocateNFirefighter )	  										
		{	 	
			//Update plan
			SendNFirefighter= NFirefighter ;

			Mybody.Action=RespondersActions.GetSAupdatefromFIC;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.Comunication ;  
			Mybody.EndofCurrentAction= InputFile.GetInfromation_FtoForR_Duration_Data ; 	

			//System.out.println( Mybody.Id  +"NFirefighter  " +  NFirefighter +"  " +MyResponders.size() );
		}
		// ++++++ 13- +++++++
		else if ( Mybody.Action==RespondersActions.GetSAupdatefromFIC   && Mybody.EndActionTrigger== RespondersTriggers.EndingAction 	) 			 	
		{		
			if ( Mybody.CommTrigger== RespondersTriggers.GetSAupdatefromFIC)
			{
				Mybody.CurrentSender.Acknowledg(Mybody);	
				Mybody.CommTrigger=null; 
			}
			else if (Mybody.CommTrigger== RespondersTriggers.GetSAupdatefromFICBC ) //boradcast
			{
				//boradcast
				Mybody.CommTrigger=null; 
			}

			Mybody.Action=RespondersActions.UpdatePlan;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.Planformulation ; 
			Mybody.EndofCurrentAction= InputFile.UdatePlan_duration ; 
			Mybody.EndActionTrigger=null; 

		}
		//==========================No more casualty  or Trapped ========================== from FireFighter   
		// ++++++ 14- +++++++
		else if ( ( Mybody.Action==RespondersActions.Noaction  ||  Mybody.Action==RespondersActions.SearchCasualty ) &&  ( Mybody.CommTrigger== RespondersTriggers.ThereisTrappedcasualty || Mybody.CommTrigger== RespondersTriggers.NoMorecasualty )   ) 										
		{	 	
			//Update plan
			if ( Mybody.CommTrigger== RespondersTriggers.NoMorecasualty )
			{
				NoMoreCasualtyCounter++;
				Mybody.EndofCurrentAction=  InputFile.GetInfromation_FtoForR_Duration_Notification ; 

				if  (NoMoreCasualtyCounter == MyResponders.size() )
				{
					End_nomorec_inner= 1;  
					System.out.println( Mybody.Id  +" " + NoMoreCasualtyCounter +"  " +MyResponders.size() + " " + xx );
				}

			}

			else if (Mybody.CommTrigger== RespondersTriggers.ThereisTrappedcasualty )
			{	
				IsThereTrapped= 1;  this.Trapped_List.add( CurrentCasualtybySender) ;
				Mybody.EndofCurrentAction=  InputFile.GetInfromation_FtoForR_Duration_Data ; 
				//System.out.println("                                       "  +"Sector: "+  "    ThereisTrappedcasualty");
			}

			Mybody.Action=RespondersActions.GetupdatesFromFR;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.Comunication ; 
			Mybody.CommTrigger=null; 


		}
		// ++++++ 15- +++++++
		else if ( Mybody.Action==RespondersActions.GetupdatesFromFR && Mybody.EndActionTrigger== RespondersTriggers.EndingAction 	) 			 	
		{
			FinshedResponder.Acknowledg(Mybody);	

			Mybody.Action=RespondersActions.UpdatePlan;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.Planformulation ;  
			Mybody.EndofCurrentAction=  InputFile.UdatePlan_duration  ; 
			Mybody.EndActionTrigger=null; 
		}

		//============================================================ ALL
		// ++++++ 16- +++++++
		else if (Mybody.Action==RespondersActions.UpdatePlan && Mybody.EndActionTrigger== RespondersTriggers.EndingAction  )
		{						
			TaskApproach_way1(  );
			Mybody.EndActionTrigger=null;
		}

		//-------------------------------------------------------------------------------------------- (P2)Both  
		// ++++++ 17- +++++++
		else  if ( ( Mybody.Action==RespondersActions.Noaction  ||  Mybody.Action==RespondersActions.SearchCasualty )  && 
				(		
						(  Mybody.assignedIncident.Communication_Structure_uesd==Inter_Communication_Structure.Struc1_SilverCommandersInteraction  && ( ( ISthereupdateTosend_SR( ) &&  Mybody.assignedIncident.Intra_Communication_Time_used==Communication_Time.When_need)   )) 
						||
						(  Mybody.assignedIncident.Communication_Structure_uesd==Inter_Communication_Structure.Struc2_BronzeCommandersInteraction  && (  ( Mybody.CurrentTick >= (Time_last_updated +  Mybody.assignedIncident.UpdatOPEvery) &&  Mybody.assignedIncident.Inter_Communication_Time_used==Communication_Time.Every_frequently )  ||  ( ISthereupdateTosend_SR( ) &&  Mybody.assignedIncident.Inter_Communication_Time_used==Communication_Time.When_need)   )) 
						)				
				)			
		{

			if (  ISthereupdateTosend_SR( )  )
			{ 
				//updates to silver commander SendupdatetoFICorRCOorSC_amb
				if ( EndSectorEstablished==1  )
				{ Reporting_plane1(ACLPerformative.InfromSafetyBriefandSectorEstablished ) ; EndSectorEstablished= 2;  }				

				if ( IsThereTrapped== 1  )
				{	Reporting_plane_TrappedReport(ACLPerformative.InformTrappedcasualty )  ; this.Trapped_List.clear(); ; IsThereTrapped= -1; }

				if ( Isthere_DecasedReport()  )
				{ Reporting_plane_DecasedReport(ACLPerformative.InformSearchandRescueCasualtyReport  );  }


				if (End_SR==1)
				{
					Reporting_plane1(ACLPerformative.InformEndSearchandRescue_Nomorecasualty  );End_SR=2;
					if ( Mybody.InterpretedTrigger==RespondersTriggers.NoMorecasualty) Mybody.InterpretedTrigger=null;
					if ( Mybody.SensingeEnvironmentTrigger !=null ) Mybody.SensingeEnvironmentTrigger =null;			
				}


				Mybody.Action=RespondersActions.InfromOPReport;
				Mybody.BehaviourType2=RespondersBehaviourTypes2.Comunication ; 

				System.out.println("                                       "  +"Sector: "+  Mybody.Id  +" update FIC  " + Mybody.CurrentMessage_list.size()   );				
				for ( ACL_Message  A:Mybody.CurrentMessage_list )
					System.out.println("                                       "  +"Sector: "+   Mybody.Id  +"  InfromOPReport    receiver:" + ( (Responder )A.receiver).Id  +"  " + A.performative   +"  sending " + ( (Responder )A.receiver).Sending  );
			}
			else
			{
				Mybody.Action=RespondersActions.UpdatePlan;
				Mybody.BehaviourType2=RespondersBehaviourTypes2.Planformulation  ; 
				Mybody.EndofCurrentAction=  InputFile.UdatePlan_duration  ; 
				Time_last_updated=Mybody.CurrentTick;
			}
		}
		// ++++++ 18- +++++++
		else if (Mybody.Action==RespondersActions.InfromOPReport && Mybody. Acknowledged )
		{
			TaskApproach_way1(  );
			Mybody.Acknowledged=false;Mybody.Sending=false; 
			Time_last_updated=Mybody.CurrentTick;
			//System.out.println("                                       "  +"Sector: "+  Mybody.Id  +" I didd update FIC  "   );

			//for(ISRecord     Rec: Mybody.assignedIncident.ISSystem) {
			//System.out.println( Rec.CasualtyinRec.ID +" -------- "  + Rec.AssignedParamdicinRec.Id +"  "  + Rec.AssignedHospitalinRec +"  "  + Rec.AssignedAmbulanceinRec +"  "  +  Rec.Treated+ "  "  +Rec.checkedSC3  + "  "  +Rec.inSector.ID  );}
			// System.out.println(Mybody.Id +"       " + Mybody.Role+ "   " +Mybody.AssignedSector + "   "+  Mybody.assignedIncident.NewRecordcasualtyadded_ISSystem(Mybody.Role,Mybody.AssignedSector,3));

		}

		//-------------------------------------------------------------------------------------------- (P3)Centralized   N/A	
		// ++++++ 19- +++++++
		else if (  Mybody.Action==RespondersActions.Noaction && SendNFirefighter!=0 && UnoccupiedResponders.size() >0  )	
		{			

			Responder_Fire FreeRes=UnoccupiedResponders.get(0);
			UpdatePlan( UpdatePlanType.RemoveRes,null, null , null , FreeRes ,false ,0 );

			Mybody.Action=RespondersActions.FreeRallocatResponders;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.Planformulation;
			Mybody.EndofCurrentAction=   InputFile.Allocation_duration ;
		}
		// ++++++ 20- +++++++
		else if (Mybody.Action==RespondersActions.FreeRallocatResponders &&  Mybody.EndActionTrigger== RespondersTriggers.EndingAction  )
		{									
			//Command_FireFighter(AllocatedTask);			

			Mybody.Action=RespondersActions.CommandFireFightertostop;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.Comunication;			
			Mybody.EndActionTrigger=null;					
		}
		// ++++++ 21- +++++++
		else if (Mybody.Action==RespondersActions.CommandFireFightertostop && Mybody. Acknowledged )
		{
			Mybody.Action=RespondersActions.Noaction;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.WaitingDuring;
			Mybody.Acknowledged=false;Mybody.Sending=false; 		
		}	
		//-------------------------------------------------------------------------------------------- (P4) both  N/A
		//============= setup ======================
		// ++++++ 22- +++++++
		else if (  Mybody.Action==RespondersActions.Noaction  && ( StartSetup && EndSectorEstablished== -1 && ! Mybody.AssignedSector.installed)  && UnoccupiedResponders.size() >0 )		  										
		{

			AllocatedRespondertoTask=UnoccupiedResponders.get(0)  ;
			UnoccupiedResponders.remove(0);
			AllocatedResponderfroSetup ++;

			Mybody.Action=RespondersActions.AllocatResponders;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.Planformulation ;
			Mybody.EndofCurrentAction=   InputFile.Allocation_duration ;

			//System.out.println("                                       "  +"Sector: "+  Mybody.Id  +"  allocate   "   );

		}
		// ++++++ 23- +++++++
		else if (Mybody.Action==RespondersActions.AllocatResponders &&  StartSetup &&  Mybody.EndActionTrigger== RespondersTriggers.EndingAction  )
		{				
			Command_FireFighter(null ,AllocatedRespondertoTask );

			Mybody.Action=RespondersActions.CommandSetupTA ;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.Comunication; //TaskPlanning;		
			Mybody.EndActionTrigger=null;					
		}
		// ++++++ 24- +++++++
		else if (Mybody.Action==RespondersActions.CommandSetupTA && Mybody. Acknowledged )
		{
			TaskApproach_way1(  );
			Mybody.Acknowledged=false;Mybody.Sending=false; 		
		}
		//============= Casualty ====================== Centralized
		// ++++++ 25- +++++++
		else if (  Mybody.Action==RespondersActions.SearchCasualty && ThereisWaitingTask(Fire_TaskType.CarryCasualtytoCCS) && UnoccupiedResponders.size() >0  &&  Mybody.assignedIncident.TaskApproach_IN ==TaskAllocationApproach.CentralizedDirectSupervision)	
		{			
			AllocatedTask = Allocate_FireFighter(Fire_TaskType.CarryCasualtytoCCS, Allocation_Strategy.Severity_RYGPriorty) ;	
			//System.out.println("                                       " + "Sector: "+ Mybody.Id + " Tasked  " + AllocatedTask.TargetCasualty.ID +"   " + AllocatedTask.AssignedResponder.Id );

			Mybody.Action=RespondersActions.AllocatResponders;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.Planformulation ;
			Mybody.EndofCurrentAction=   InputFile.Allocation_duration ;
		}
		// ++++++ 26- +++++++
		else if (Mybody.Action==RespondersActions.AllocatResponders &&  Mybody.EndActionTrigger== RespondersTriggers.EndingAction  )
		{									
			Command_FireFighter(AllocatedTask,null);
			UpdatePlan( UpdatePlanType.AssignTask, AllocatedTask.TargetCasualty ,null, Fire_TaskType.CarryCasualtytoCCS , null ,false ,0);		// responder assigned inside  allocation

			Mybody.Action=RespondersActions.CommandFireFighterCarry;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.Comunication; //TaskPlanning ; 		
			Mybody.EndActionTrigger=null;					
		}
		// ++++++ 27- +++++++
		else if (Mybody.Action==RespondersActions.CommandFireFighterCarry && Mybody. Acknowledged )
		{
			TaskApproach_way1(  );
			Mybody.Acknowledged=false;Mybody.Sending=false; 		
		}

		//-------------------------------------------------------------------------------------------- (P4) 
		// ++++++ 28- +++++++


		// ++++++ 28- +++++++
		else if ( ( Mybody.Action==RespondersActions.Noaction  ||  Mybody.Action==RespondersActions.SearchCasualty ) &&   ( CCSestablished ==1 || ( CCSestablished ==2 && NewRespondrsNotinfromedCCS.size()>0)  ) )  										
		{

			//System.out.println("                                       "  +"Sector: "+  Mybody.Id  +"  Boradcastupdate to responders  "   );

			//send message to Firefighters						
			if (CCSestablished ==1 ) { BoradcastAction(ACLPerformative.InfromCCSEstablished , this.MyResponders );CCSestablished =2; NewRespondrsNotinfromedCCS.clear(); }
			else if (CCSestablished ==2 ) { BoradcastAction(ACLPerformative.InfromCCSEstablished , NewRespondrsNotinfromedCCS ); NewRespondrsNotinfromedCCS.clear(); }
			Mybody.Action=RespondersActions.BoradcastFICpdate ;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.Comunication; 
			Mybody.EndofCurrentAction=   InputFile.Boradcast_duration ;

		}
		else if ( ( Mybody.Action==RespondersActions.Noaction  ||  Mybody.Action==RespondersActions.SearchCasualty ) &&  ( RCestablished == 1 || ( RCestablished ==2 && NewRespondrsNotinfromedRC.size()>0)  ) )   										
		{

			//System.out.println("                                       "  +"Sector: "+  Mybody.Id  +"  Boradcastupdate to responders  "   );

			//send message to Firefighters						
			if ( RCestablished ==1  ) { BoradcastAction(ACLPerformative.InfromRCEstablished, this.MyResponders );RCestablished=2; NewRespondrsNotinfromedRC.clear(); }
			else if (RCestablished ==2 ) { BoradcastAction(ACLPerformative.InfromRCEstablished , NewRespondrsNotinfromedRC ); NewRespondrsNotinfromedRC.clear(); }
			Mybody.Action=RespondersActions.BoradcastFICpdate ;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.Comunication;  
			Mybody.EndofCurrentAction=   InputFile.Boradcast_duration ;

		}
		// ++++++ 29- +++++++
		else if (Mybody.Action==RespondersActions.BoradcastFICpdate && Mybody.EndActionTrigger== RespondersTriggers.EndingAction  )
		{				
			//for casualty
			if (CCSestablished ==2 ) Mybody.assignedIncident.BroadCast_CASU_InstalledCCS=true;
			if (RCestablished ==2 )Mybody.assignedIncident.BroadCast_CASU_InstalledRC=true;

			TaskApproach_way1(  );
			Mybody.Sending=false; 
			Mybody.EndActionTrigger=null;
		}
		//-------------------------------------------------------------------------------------------- (P5) Centralized N/A
		// ++++++ 30- +++++++
		else if (! ThereisWaitingTask(Fire_TaskType.CarryCasualtytoCCS) && Mybody.Action==RespondersActions.SearchCasualty &&  Mybody.SensingeEnvironmentTrigger== RespondersTriggers.SensedCasualty && Mybody.assignedIncident.TaskApproach_IN ==TaskAllocationApproach.CentralizedDirectSupervision )
		{									
			UpdatePlan( UpdatePlanType.NewTask, Mybody.TargetCasualty ,null, Fire_TaskType.CarryCasualtytoCCS,  null,false ,0);
			//System.out.println("                                       "  + "Sector: "+Mybody.Id + " added" + Mybody.TargetCasualty.ID +" "+ UnoccupiedResponders.size());

			if ( Mybody.TargetCasualty .Status == CasualtyStatus.Trapped   )
			{	IsThereTrapped= 1;  this.Trapped_List.add(Mybody.TargetCasualty) ; }			
			if ( ! Mybody.CasualtySeen_list.contains(Mybody.TargetCasualty)) Mybody.CasualtySeen_list.add(Mybody.TargetCasualty); 

			Mybody.Action=RespondersActions.UpdatePlan;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.Planformulation;
			Mybody.EndofCurrentAction=  InputFile.UdatePlan_duration  ; 
			Mybody.SensingeEnvironmentTrigger=null; 
		}
		//------------------------------------------End--------------------------------------------------(P6)  Both 		
		// ++++++ 31- +++++++	
		else if  (  ( Mybody.Action==RespondersActions.Noaction  ||  Mybody.Action==RespondersActions.SearchCasualty ) && End_SR==2  )
		{		

			//	( Mybody.Action==RespondersActions.SearchCasualty && IsAllTaskclosed1(Fire_TaskType.CarryCasualtytoCCS)&& UnoccupiedResponders.size()== this.MyResponders.size()  &&  Mybody.assignedIncident.TaskApproach_IN ==TaskAllocationApproach.CentralizedDirectSupervision &&  ( Mybody.SensingeEnvironmentTrigger== RespondersTriggers.Arrived_WalkedinAllScene || Mybody.SensingeEnvironmentTrigger == RespondersTriggers.Arrived_WalkedalongOneDirection))
			//	|| (Mybody.Action==RespondersActions.Noaction && Mybody.InterpretedTrigger== RespondersTriggers.NoMorecasualty  &&  ( Mybody.assignedIncident.TaskApproach_IN ==TaskAllocationApproach.Decentralized_Autonomous ||Mybody.assignedIncident.TaskApproach_IN ==TaskAllocationApproach.Decentralized_Mutualadjustment))  ) 

			End_SR=3;
			System.out.println( "                                       "   + "Sector: "+Mybody.Id   +" done carry *****************1*****************" ); 
			if (  Mybody.SensingeEnvironmentTrigger!=null ) Mybody.SensingeEnvironmentTrigger= null;

			BoradcastAction(ACLPerformative.InformSceneOprationsEND, this.MyResponders);

			Mybody.Action=RespondersActions.BoradcastEndScene  ;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.Comunication ;  
			Mybody.EndofCurrentAction=   InputFile.Boradcast_duration ;
			//Mybody.Acknowledged=false;Mybody.Sending=false; 	

		}
		// ++++++ 32- +++++++
		else if (Mybody.Action==RespondersActions.BoradcastEndScene  && Mybody.EndActionTrigger== RespondersTriggers.EndingAction  )
		{		
			Mybody.InterpretedTrigger=RespondersTriggers.DoneSearchCasualtyActivity;

			Mybody.Action=RespondersActions.Noaction;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.WaitingDuring;
			Mybody.EndActionTrigger= null;
			Mybody.Sending=false; 
			//System.out.println( "  "+"Sector: "+Mybody.Id   +" BoradcastEndER ");

			System.out.println("                                       "   + "Sector: "+Mybody.Id  +" done carry*****************2*****************" ); 

			//for(ISRecord     Rec: Mybody.assignedIncident.ISSystem) {
			//System.out.println( Rec.CasualtyinRec.ID +" -------- "  + Rec.AssignedParamdicinRec.Id +"  "  + Rec.AssignedHospitalinRec +"  "  +  Rec.TransferdV+ "  "  +  Rec.TransferdH+"  "  +Rec.checkedALC2);}

		}


		//		if (Mybody.zzz== true)
		//		{for (   Responder_Fire f : MyResponders )
		//			System.out.println( f.CurrentAssignedActivity +"  "+ f.ss +"  " + f.Id +"  "+ f.Role + " "+ "  Action: " + f.Action + " com:" + f.CommTrigger + "   Nt:" + f.InterpretedTrigger+ " SensingeEnvir:" +f.SensingeEnvironmentTrigger + " end:" + f.EndActionTrigger +"  Acknowledged: " + f.Acknowledged + "   Sending:" + f.Sending +"  "+ f.ActivityAcceptCommunication );
		//		Mybody.zzz= false;
		//		}

		//if ( Mybody.SensingeEnvironmentTrigger == RespondersTriggers.Arrived_WalkedalongOneDirection  )
		//System.out.println("                                       "   + "Sector: "+Mybody.Id  +" done carry*********************************" + IsAllTaskclosed(Fire_TaskType.CarryCasualtytoCCS)+"  "+ UnoccupiedResponders.size()+ "  " + this.MyResponders.size()  ); 
	}

	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	// FSC -  CoordinateClearRoute sector
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	private void  FSCCommanderBehavior_CoordinateClearRouteWreckage()
	{		
		// ++++++ Move +++++++	
		if (  Mybody.Action==RespondersActions.SearchRoute && Mybody.RandomllySearachRoute==true && Mybody.SensingeEnvironmentTrigger ==null   &&  Mybody.CommTrigger ==null && Mybody.InterpretedTrigger==null && Mybody.EndActionTrigger==null && Mybody.EndofCurrentAction == 0 && Mybody.Sending==false  )
		{
			Mybody.Walk();	
			//System.out.println("                                       "  +"WrK: "+Mybody.Id + "  walk " + this.MyResponders.size());
		}	
		// ++++++ Move +++++++

		//--------------------------------------------------------------------------------------------	
		// ++++++ 1- +++++++
		if (Mybody.InterpretedTrigger== RespondersTriggers.FirstTimeonLocation)
		{
			//implent Plan
			Mybody.AssignedRoute=null ;
			Mybody.NoMoreRouteinScene=false;

			//	Mybody.RouteSeen_list.clear();
			Mybody.StopDistnation=Mybody.AssignedCordon.EnteryPointAccess_Point ;
			Mybody.ArrivedSTOP=false ;

			Mybody.CurrentDirectionNode=Mybody.NearstSerachNode() ;
			Mybody.MyDirectiontowalk=Mybody.CurrentDirectionNode.PN;
			Mybody._PointDestination= Mybody.CurrentDirectionNode.PN;

			Mybody.walkingstrategy=RandomWalking_StrategyType.Nodes_Cordon ;
			Mybody.Assign_DestinationLocation_Serach(); 



			Mybody.Action=RespondersActions.FormulatePlan;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.Planformulation;	
			Mybody.EndofCurrentAction=  InputFile.FormulatePlan_duration  ; 
			Mybody.InterpretedTrigger=null; 
		}
		// ++++++ 2- +++++++
		else if ( Mybody.Action==RespondersActions.FormulatePlan  && Mybody.EndActionTrigger== RespondersTriggers.EndingAction 	) 			 	
		{				
			TaskApproach_way2(  );
			Mybody.EndActionTrigger=null;

			System.out.println("                                       "  +"WrK: "+Mybody.Id + " star serach " );			

		}
		//-------------------------------------------------------------------------------------------- (P1)		
		//==========================New / Back Responder ========================== Both  N/A   face to face
		// ++++++ 3- +++++++
		else if ( ( Mybody.Action==RespondersActions.Noaction  ||  Mybody.Action==RespondersActions.SearchRoute)&& ( Mybody.CommTrigger == RespondersTriggers.GetNewFirefighterarrived ||Mybody.CommTrigger == RespondersTriggers.GetFirefighterComeback  ) )	//come back from transfer	  										
		{	 	
			if (Mybody.CommTrigger == RespondersTriggers.GetNewFirefighterarrived )
				UpdatePlan( UpdatePlanType.NewRes, null, null, null ,NewResponder , false ,0) ;

			else if (Mybody.CommTrigger == RespondersTriggers.GetFirefighterComeback )
				UpdatePlan( UpdatePlanType.FreeRes,null, null, null ,FinshedResponder ,false,0) ;

			Mybody.Action=RespondersActions.GetArrivalNotification ;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.Comunication ; 
			Mybody.EndofCurrentAction=  InputFile.GetNotification_duration ; 

			//System.out.println("                                       "  +"WrK: "+ Mybody.Id +" total  " + this.MyResponders.size() + "zzzzzzzzzzzzz");
		}
		// ++++++ 4- +++++++
		else if ( Mybody.Action==RespondersActions.GetArrivalNotification  && Mybody.EndActionTrigger== RespondersTriggers.EndingAction ) 			 	
		{


			if (Mybody.CommTrigger == RespondersTriggers.GetNewFirefighterarrived )
				NewResponder .Acknowledg(Mybody);

			else if (Mybody.CommTrigger == RespondersTriggers.GetFirefighterComeback )
				FinshedResponder .Acknowledg(Mybody);


			Mybody.CommTrigger=null; 


			Mybody.Action=RespondersActions.UpdatePlan;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.Planformulation;
			Mybody.EndofCurrentAction=  InputFile.UdatePlan_duration  ; 
			Mybody.EndActionTrigger=null; 
		}

		//==========================Result of cleared route ==========================  Paper/Electronic   
		// ++++++ 7- +++++++
		else if (  ( Mybody.Action==RespondersActions.Noaction || Mybody.Action==RespondersActions.SearchRoute) && ( Mybody.CommTrigger== RespondersTriggers.GetResultCleardRoute 	 )  )  
		{		
			

			Mybody.Action=RespondersActions.GetResultCleardRoute ;			
			Mybody.BehaviourType2=RespondersBehaviourTypes2.Comunication;  
			Mybody.EndofCurrentAction= InputFile.GetInfromation_FtoForR_Duration_Notification ; 
			Mybody.CommTrigger=null; 

		}
		// ++++++ 8- +++++++
		else if ( Mybody.Action==RespondersActions.GetResultCleardRoute  && Mybody.EndActionTrigger== RespondersTriggers.EndingAction 	) 			 	
		{					
			RoadLink  RL=null;
			Responder_Fire Res=null;

			//============ 1 ============ 
			Mybody.CurrentSender.Acknowledg(Mybody);				
			RL= CurrentRoutebySender ;
			Res=(Responder_Fire) Mybody.CurrentSender;	
			System.out.println("                                       "  +"WrK: "+ Res.Id + " GetResult  route  from" + Res.Id );  

			//============ 2 ============ 
			//update plan
			UpdatePlan( UpdatePlanType.CloseTask,null, RL ,Fire_TaskType.ClearRouteWreckage,Res ,false ,0);	// ready to send to PIC 
			UpdatePlan( UpdatePlanType.FreeRes, null,null,null,Res,false,0) ;

			if (this.IsCurrentRouteClosed(RL))
			{			
				System.out.println("                                       "  +"WrK: "+ Mybody.Id + " donnnnnnnnnnnnnnnnnnnne   route:  "+ Mybody.AssignedRoute.fid +Mybody.AssignedRoute.WreckageLevel  +"  " + Mybody.AssignedRoute.descript_1 + " ResNum:"  + this.UnoccupiedResponders.size());
				Mybody.AssignedRoute=null ;  	
			}

			Mybody.Action=RespondersActions.UpdatePlan;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.Planformulation;
			Mybody.EndofCurrentAction= InputFile.UdatePlan_duration ; 
			Mybody.EndActionTrigger=null; 

		}

		//==========================Get RouteProirtyto clear From FIC==========================  N/A 
		// ++++++ 9- +++++++
		else if ( ( Mybody.Action==RespondersActions.Noaction || Mybody.Action==RespondersActions.SearchRoute) && ( Mybody.CommTrigger== RespondersTriggers.GetRouteProirtytoclearFromFIC  ))	  										
		{	 	
			int total=0;
			//UpdatePlan	
			for ( RoadLink  RL :  CurrentRoutebySender_List  )
			{
				UpdatePlan( UpdatePlanType.NewTask, null, RL ,Fire_TaskType.ClearRouteWreckage,null ,false , 1 );  //we can set Priority
				total++;
			}
			CurrentRoutebySender_List .clear();

			Mybody.Action=RespondersActions.GetRouteProirtytoclearFromFIC;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.Comunication;
			Mybody.EndofCurrentAction=  InputFile.GetInfromation_FtoForR_Duration_Data * total ; 
			Mybody.CommTrigger=null; 

			System.out.println("                                       "  +"WrK: "+ Mybody.Id  +" GetRouteProirtytoclear " + total  + "<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<" );
		}
		// ++++++ 10- +++++++
		else if ( Mybody.Action==RespondersActions.GetRouteProirtytoclearFromFIC && Mybody.EndActionTrigger== RespondersTriggers.EndingAction 	) 			 	
		{

			Mybody.CurrentSender.Acknowledg(Mybody);	

			for (Task_Fire Task : Operational_Plan) 
				System.out.println("                                       "  +"WrK: "+ Mybody.Id +" Route: "+ Task.TargetRoute.fid+"  " + "  " + Task.TargetRoute.WreckageLevel+ Task.TargetRoute.descript_1+ Task.TargetRoute.WreckageLevel  + " " + Task.TaskStatus + "  " + Task.Priority_clearRoute);

			Mybody.Action=RespondersActions.UpdatePlan;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.Planformulation;
			Mybody.EndofCurrentAction= InputFile.UdatePlan_duration ; 
			Mybody.EndActionTrigger=null; 
		}

		//========================== Updates from FIC  ==========================		
		// ++++++ 13- +++++++
		else if (  ( Mybody.Action==RespondersActions.Noaction || Mybody.Action==RespondersActions.SearchRoute)   &&  Mybody.CommTrigger==RespondersTriggers.GetSAupdatefromFIC   )											
		{	 							 
			//Updates about SA

			if (   End_nomorec_LA==0)
			{	End_nomorec_LA=1;  Mybody.NoMoreRouteinScene= true;   System.out.println("                                       "  +"WrK: " + Mybody.Id +"GetNoMorecasualty" );}  //NoMoreRouteinScene= true;  ??


			Mybody.Action=RespondersActions.GetSAupdatefromFIC  ;		
			Mybody.BehaviourType2=RespondersBehaviourTypes2.Comunication;  
			Mybody.EndofCurrentAction= InputFile.GetInfromation_FtoForR_Duration_Notification ; 	

			Mybody.CommTrigger=null; 
		}
		// ++++++ 14- +++++++
		if (Mybody.Action==RespondersActions.GetSAupdatefromFIC  && Mybody.EndActionTrigger== RespondersTriggers.EndingAction 	  )//InputFile.CasualtyReport_Mechanism == CasualtyReportandTrackingMechanism.Paper		
		{
			Mybody.CurrentSender.Acknowledg(Mybody);

			Mybody.Action=RespondersActions.UpdatePlan;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.Planformulation;
			Mybody.EndofCurrentAction= InputFile.UdatePlan_duration ; 
			Mybody.EndActionTrigger=null; 
		}
		//============================================================ ALL
		// ++++++ 11- +++++++
		else if (Mybody.Action==RespondersActions.UpdatePlan && Mybody.EndActionTrigger== RespondersTriggers.EndingAction  )
		{						
			TaskApproach_way2();
			Mybody.EndActionTrigger=null;
		}
		//-------------------------------------------------------------------------------------------- (P2)  Paper
		// ++++++ 12- +++++++
		//		else  if ( ( Mybody.Action==RespondersActions.Noaction || Mybody.Action==RespondersActions.SearchRoute) &&				
		//			(		
		//					(  Mybody.assignedIncident.Communication_Structure_uesd==Inter_Communication_Structure.Struc1_SilverCommandersInteraction  && ( ( ISthereupdateTosend_Wr( ) &&  Mybody.assignedIncident.Intra_Communication_Time_used==Communication_Time.When_need)   )) 
		//					||
		//					(  Mybody.assignedIncident.Communication_Structure_uesd==Inter_Communication_Structure.Struc2_BronzeCommandersInteraction  && (  ( Mybody.CurrentTick >= (Time_last_updated +  Mybody.assignedIncident.UpdatOPEvery) &&  Mybody.assignedIncident.Inter_Communication_Time_used==Communication_Time.Every_frequently )  ||  ( ISthereupdateTosend_Wr( ) &&  Mybody.assignedIncident.Inter_Communication_Time_used==Communication_Time.When_need)   )) 
		//					)				
		//			)	
		//			
		//		{		
		//
		//			if (  ISthereupdateTosend_Wr( )  )
		//			{ 
		//
		//				//Reporting_plane_RouteReport(ACLPerformative.InformClearOfRouteReport ) ;
		//				//SendupdatetoFICorRCOorSC_amb=false;
		//
		//				Mybody.Action=RespondersActions.InfromOPReport;
		//				Mybody.BehaviourType2=RespondersBehaviourTypes2.Comunication;  //Sharinginfo;
		//				System.out.println( "                                       "+"WrK: "+  Mybody.Id  +" update FIC  "   );
		//				//System.out.println(  Mybody.Lastmessagereaded  +"  " + Mybody.Message_inbox.size()  + " "+ Mybody.CommTrigger   +"   " + Mybody.Sending + "  "+ Mybody.ActivityAcceptCommunication +" "+ Mybody.Acknowledged);
		//			}
		//			else
		//			{
		//				Mybody.Action=RespondersActions.UpdatePlan;
		//				Mybody.BehaviourType2=RespondersBehaviourTypes2.Planexecution;  //Planformulation;
		//				Mybody.EndofCurrentAction=  InputFile.UdatePlan_duration  ; 
		//
		//				Time_last_updated=Mybody.CurrentTick;
		//				//System.out.println( "                                       "+"WrK: "+  Mybody.Id  +" nothings  "   );
		//				// System.out.println(Mybody.Id+"    Action:" + Mybody.Action + " com:" + Mybody.CommTrigger + "   Nt:" + Mybody.InterpretedTrigger+ " en:" + Mybody.SensingeEnvironmentTrigger + " end:" + Mybody.EndActionTrigger +  "Acknowledged: "+ Mybody.Acknowledged+"sending: "+  Mybody.Sending  + "CurrentMessage: ");
		//
		//			}	
		//		}
		// ++++++ 13- +++++++
		else if (Mybody.Action==RespondersActions.InfromOPReport && Mybody. Acknowledged )
		{
			TaskApproach_way2();
			Mybody.Acknowledged=false;Mybody.Sending=false; 
			Time_last_updated=Mybody.CurrentTick;

		}
		//-------------------------------------------------------------------------------------------- (P3) 
		//============= clear Wreckage ======================	
		// ++++++ 14- +++++++
		else if (  ( Mybody.Action==RespondersActions.Noaction || Mybody.Action==RespondersActions.SearchRoute )  &&  Mybody.AssignedRoute==null &&   IsThereWreckageRoute_ToAssign() && UnoccupiedResponders.size() >0 )	
		{	

			AllocatedTask = Allocate_FireFighter2(Fire_TaskType.ClearRouteWreckage,Allocation_Strategy_Route.SliverPriorty ) ;	//return route	mean route 				
			Mybody.AssignedRoute = AllocatedTask.TargetRoute ;

			
			if (  Mybody.AssignedRoute.InsideCordon==true )
			{
				Mybody.Assign_DestinationCordon(Mybody.AssignedRoute.PointofClear);			
				Mybody.Action=RespondersActions.GoToRoute ;
				Mybody.BehaviourType2=RespondersBehaviourTypes2.Movementonincidentsite ; //TaskPlanning;

				System.out.println("                                       "  +"WrK: "+Mybody.Id + "GOOOOOOOOOOOOOOo to " + Mybody.AssignedRoute.fid +"  " +Mybody.AssignedRoute.descript_1);
				
			}
			else if (  Mybody.AssignedRoute.InsideCordon==false )
			{
				Mybody.Assign_DestinationCordon(Mybody.AssignedCordon.EnteryPointAccess_Point);		
				Mybody.Action=RespondersActions.GoToRoute ;
				Mybody.BehaviourType2=RespondersBehaviourTypes2.Movementonincidentsite ; //TaskPlanning;

				System.out.println("                                       "  +"WrK: "+Mybody.Id + "PEAAAAAAAAAAAAAAAAAAAAAAAAA " + Mybody.AssignedRoute.fid +"  " + Mybody.AssignedRoute.descript_1);
				
			}
				
		}
		// ++++++ 15- +++++++
		else if (  Mybody.Action==RespondersActions.GoToRoute &&  Mybody.SensingeEnvironmentTrigger==null) 												
		{	
			Mybody.Walk();
		}
		// ++++++ 16- +++++++
		else if (  (Mybody.Action==RespondersActions.GoToRoute  && Mybody.SensingeEnvironmentTrigger== RespondersTriggers.ArrivedTargetObject ) ||
				( Mybody.Action==RespondersActions.Noaction && Mybody.AssignedRoute!=null && IsThereWreckageRoute_ToAssign() && UnoccupiedResponders.size() >0 )	)  
		{			
			AllocatedRespondertoTask=AllocatedTask.ckeck_Distance_befor_TaskAssignment_Route(UnoccupiedResponders) ;

			Mybody.Action=RespondersActions.AllocatResponders;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.Planformulation;
			Mybody.EndofCurrentAction=   InputFile.Allocation_duration ;

			if ( Mybody.SensingeEnvironmentTrigger != null ) { Mybody.SensingeEnvironmentTrigger=null;}

			System.out.println("                                       "  +"WrK: "+ Mybody.Id + "  Tasked" + AllocatedRespondertoTask.Id  );
		}
		// ++++++ 17- +++++++
		else if (Mybody.Action==RespondersActions.AllocatResponders && Mybody.EndActionTrigger== RespondersTriggers.EndingAction  )
		{												
			UpdatePlan( UpdatePlanType.AssignTask, null,AllocatedTask.TargetRoute, Fire_TaskType.ClearRouteWreckage , AllocatedRespondertoTask , false ,0);		// responder assigned inside  allocation
			Command_FireFighter(AllocatedTask, AllocatedRespondertoTask);

			Mybody.Action=RespondersActions.CommandFirefighterClearRoute ;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.Comunication; //TaskPlanning;		
			Mybody.EndActionTrigger=null;

		}
		// ++++++ 18- +++++++
		else if (Mybody.Action==RespondersActions.CommandFirefighterClearRoute && Mybody. Acknowledged )
		{
			TaskApproach_way2();
			Mybody.Acknowledged=false;Mybody.Sending=false; 		
			//System.out.println("                                       "  +"WrK: "+ Mybody.Id + "  done Tasked" + AllocatedRespondertoTask.Id  );
		}
		//-------------------------------------------------------------------------------------------- (P4)  N/A
		// ++++++ 19- +++++++
		else if (  Mybody.Action==RespondersActions.SearchRoute &&  Mybody.SensingeEnvironmentTrigger== RespondersTriggers.SensedRoute )
		{												
			UpdatePlan( UpdatePlanType.NewTask,null,Mybody.TargetRoute ,Fire_TaskType.ClearRouteWreckage,null,false ,0 );

			System.out.println("                                       "  +"WrK: "+Mybody.Id + " added:"+ Mybody.TargetRoute.fid+"  "+ Mybody.TargetRoute.descript_1);

			Mybody.Action=RespondersActions.UpdatePlan;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.Planformulation;
			Mybody.EndofCurrentAction=  InputFile.UdatePlan_duration  ; 
			Mybody.SensingeEnvironmentTrigger=null; 							

		}
		// ++++++ 20- +++++++
		else if (   Mybody.Action==RespondersActions.SearchRoute  &&  Mybody.SensingeEnvironmentTrigger== RespondersTriggers.Arrived_WalkedinAllScene)
		{									
			//update plan
			Mybody.NoMoreRouteinScene= true;

			Mybody.Action=RespondersActions.UpdatePlan;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.Planformulation;
			Mybody.EndofCurrentAction=  InputFile.UdatePlan_duration  ; 
			Mybody.SensingeEnvironmentTrigger=null; 

			System.out.println("                                       "  +"WrK: "+ Mybody.Id  +" Arrived_WalkedinAllScene" ); 
		}
		//------------------------------------------End-------------------------------------------------- (P5) //&& IsAllTaskclosed(Fire_TaskType.ClearRouteWreckage)
		// ++++++ 21- +++++++	
		else if (  (Mybody.Action==RespondersActions.Noaction ||Mybody.Action==RespondersActions.SearchRoute )  && End_nomorec_LA==1   && IsAllTaskclosed1(Fire_TaskType.ClearRouteWreckage) )
		{			
			End_nomorec_LA= 2;
			this.Reporting_plane1(ACLPerformative.InformEndClearRouteWreckage);

			Mybody.Action=RespondersActions.InformEndClearRouteWreckage;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.Comunication ; 
		}
		// ++++++ 22- +++++++
		else if (Mybody.Action==RespondersActions.InformEndClearRouteWreckage && Mybody. Acknowledged )
		{

			BoradcastAction(ACLPerformative.InformEndClearRouteWreckage, this.MyResponders);

			Mybody.Action=RespondersActions.BoradcastEndScene  ;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.Comunication;  
			Mybody.EndofCurrentAction=   InputFile.Boradcast_duration ;
			Mybody.Acknowledged=false;Mybody.Sending=false; 	

		}
		// ++++++ 33- +++++++
		else if (Mybody.Action==RespondersActions.BoradcastEndScene  && Mybody.EndActionTrigger== RespondersTriggers.EndingAction  )			
		{				
			Mybody.InterpretedTrigger=RespondersTriggers.DoneClearRouteActivity;

			Mybody.Action=RespondersActions.Noaction;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.WaitingEnd;
			Mybody.Sending=false; 
			Mybody.EndActionTrigger=null;

			for (Task_Fire Task : Operational_Plan) 
				System.out.println("                                       "  +"WrK: "+ Task.TaskStatus  +" Route: "+ Task.TargetRoute.fid+"  " + Task.TargetRoute.descript_1 + " " + Task.TargetRoute.WreckageLevel);


			System.out.println("                                       "  +"WrK: "+ Mybody.Id  +" done ClearRouteWreckage ************1******2****************" ); 

		}
		
		if (Mybody.zzz )
			{
				System.out.println("              (((((((((((((((((((((((((((((((((((((((((((((((((((((((             "  +"WrK: "+ AllocatedRespondertoTask.Id  +"   " + AllocatedRespondertoTask.Action); 
				Mybody.zzz = false;
			}

	}

	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	// FSC -  CoordinateEndResponse  
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	private void CommanderBehavior_EndER()
	{
		// ++++++ 1- +++++++
		if ( Mybody.CommTrigger==RespondersTriggers.ENDER ) 
		{
			Mybody.Action=RespondersActions.GetNotificationEndER ;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.Comunication; 
			Mybody.EndofCurrentAction= InputFile.GetNotification_duration ;
			Mybody.CommTrigger=null; 

		}
		// ++++++ 2- +++++++
		else if (Mybody.Action==RespondersActions.GetNotificationEndER  && Mybody.EndActionTrigger== RespondersTriggers.EndingAction   )
		{
			// Send message to its workers	
			BoradcastAction(ACLPerformative.InformERend, this.MyResponders);

			Mybody.Action=RespondersActions.BoradcastEndER ;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.Comunication;  
			Mybody.EndofCurrentAction=   InputFile.Boradcast_duration ;
			Mybody.EndActionTrigger=null;					
		}
		// ++++++ 3- +++++++
		else if (Mybody.Action==RespondersActions.BoradcastEndER && Mybody.EndActionTrigger== RespondersTriggers.EndingAction  )
		{		
			Mybody.InterpretedTrigger=RespondersTriggers.ENDER; //make sure no still task

			Mybody.Action=RespondersActions.Noaction;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.WaitingEnd;
			Mybody.Sending=false; 
			Mybody.EndActionTrigger= null;
			System.out.println( "                                       "  +Mybody.Id  +" GO back to Vehicle  " +Mybody.Role );

		}
	}

}//end class

