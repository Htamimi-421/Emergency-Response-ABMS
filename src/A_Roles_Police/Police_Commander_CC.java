package A_Roles_Police;
import java.util.ArrayList;
import java.util.List;
import A_Agents.Casualty;
import A_Agents.Responder;
import A_Agents.Responder_Ambulance;
import A_Agents.Responder_Police;
import A_Environment.Cordon;
import A_Environment.RoadLink;
import B_Classes.Task_Fire;
import B_Classes.Task_Police;
import B_Communication.ACL_Message;
import B_Communication.Command;
import B_Communication.Report;
import C_SimulationInput.InputFile;
import D_Ontology.BuildStaticFuction;
import D_Ontology.Ontology; 
import D_Ontology.Ontology.ACLPerformative;
import D_Ontology.Ontology.Allocation_Strategy_Route;
import D_Ontology.Ontology.CommunicationMechanism;
import D_Ontology.Ontology.Communication_Time;
import D_Ontology.Ontology.Fire_TaskType;
import D_Ontology.Ontology.GeneralTaskStatus;
import D_Ontology.Ontology.Inter_Communication_Structure;
import D_Ontology.Ontology.Police_ResponderRole;
import D_Ontology.Ontology.Police_TaskType;
import D_Ontology.Ontology.RandomWalking_StrategyType;
import D_Ontology.Ontology.RespondersActions;
import D_Ontology.Ontology.RespondersBehaviourTypes2;
import D_Ontology.Ontology.RespondersBehaviourTypes3;
import D_Ontology.Ontology.RespondersTriggers;
import D_Ontology.Ontology.TypeMesg;
import D_Ontology.Ontology.UpdatePlanType;


public class Police_Commander_CC { //outer cordon

	Responder_Police Mybody;

	//----------------------------------------------------
	Responder_Police NewResponder=null,FinshedResponder=null, AllocatedRespondertoTask=null;

	RoadLink   CurrentRoutebySender=null;
	ArrayList<RoadLink> CurrentRoutebySender_List=new ArrayList<RoadLink>();

	Task_Police  AllocatedTask , CurrentCheckedTask=null;

	int NoMoreRouteCounter=0;
	boolean StartSetup=false;
	
	boolean FirstTime=false;

	//----------------------------------------------------
	//  The meaning of attribute content....  -1:No update   , 0: Get  , 1: realize or done  2: send to workers or PIC
	int  EndCordonEstablished = -1 ; 
	int End_nomorec_LA= -1 ;
	int End_CC=-1;
	int  OptionTaskType=0   ;    // 1  SecureRoute  2  .ClearRouteTraffic
	//----------------------------------------------------
	int AllocatedResponderfroSetup=0 ;
	List<Task_Police> Operational_Plan = new ArrayList<Task_Police>(); // for each route 
	List<Responder_Police> MyResponders = new ArrayList<Responder_Police>(); //its worker
	List<Responder_Police> UnoccupiedResponders = new ArrayList<Responder_Police>(); //free worker

	//----------------------------------------------------
	double Time_last_updated=0;	

	//##############################################################################################################################################################	
	public Police_Commander_CC ( Responder_Police _Mybody ,Cordon  _AssignedCordon ) 
	{
		Mybody=_Mybody;
		Mybody.ColorCode= 6;

		Mybody.step_long =InputFile.step_long_ClearRoute;
		Mybody.EndofCurrentAction=0;	
		Mybody.AssignedCordon=_AssignedCordon ; // outer 

		Mybody.AssignedCordon.CCcommander=Mybody;
		Mybody.assignedIncident.O_CCcommander= Mybody ;

		//if (Mybody.AssignedCordon.CordonType== CordonType.outer )  { Mybody.ColorCode=1; }  
		//if (Mybody.AssignedCordon.CordonType== CordonType.inner) { Mybody.ColorCode=2  ; }  		
		Mybody.PrvRoleinprint3 =Mybody.Role;

	}

	//##############################################################################################################################################################
	public void  CommanderCC_InterpretationMessage()
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
			if ( Mybody.CurrentSender instanceof Responder_Police && Mybody.CurrentSender==Mybody.assignedIncident.PICcommander &&  Mybody.CurrentCommandRequest.commandType3 ==Police_TaskType.ClearRouteTraffic  )  // from PIC
			{				
				Mybody.CommTrigger= RespondersTriggers.GetRouteProirtytoclearFromPIC ; 				
				CurrentRoutebySender_List=Mybody.CurrentCommandRequest.TargetRoad_List ;				
			}
			//++++++++++++++++++++++++++++++++++++++
			break;


		case InformLoadingandTransportationReport:					
			Report CurrentReport=((Report) currentmsg.content);
			Mybody.CommTrigger= RespondersTriggers.GetRouteProirtytoclearFromPIC ; 				
			CurrentRoutebySender_List=CurrentReport.routeNEEDCleartraffic_List ;			
			break;

		case InformNewResponderArrivalforsetup:	
			Mybody.CommTrigger= RespondersTriggers.GetNewPolicemanarrivedforsetup ; 
			NewResponder= (Responder_Police) Mybody.CurrentSender;
			break;
		case InformNewResponderArrival:
			Mybody.CommTrigger= RespondersTriggers.GetNewPolicemanarrived;	
			NewResponder= (Responder_Police) Mybody.CurrentSender;			
			break;
		case InformlocationArrival:
			Mybody.CommTrigger= RespondersTriggers.GetPolicemanComeback ;
			FinshedResponder= (Responder_Police) Mybody.CurrentSender;
			break;
		case InfromResultSetupCordon:
			Mybody.CommTrigger= RespondersTriggers.GetresultofSetup;
			FinshedResponder= (Responder_Police) Mybody.CurrentSender;
			break;	
		case InfromResultRoute :
			Mybody.CommTrigger= RespondersTriggers.GetResultCleardRoute;
			CurrentRoutebySender  = ((RoadLink)currentmsg.content);
			FinshedResponder= (Responder_Police) Mybody.CurrentSender;
			break;

			//------------------------
		case InformNomorecasualty: // from PIC  or LAC
			Mybody.CommTrigger= RespondersTriggers.GetSAupdatefromPIC ;
			End_nomorec_LA= 0 ;  
			break;	
		case InformERend :
			Mybody.CommTrigger= RespondersTriggers.ENDER;

			break;	
		} // end switch

		//System.out.println(Mybody.Id + "  " + Mybody.CommTrigger + "  from  "+ Mybody.CurrentSender.Id  + "    Action:" + Mybody.Action);
	}	

	//##############################################################################################################################################################	
	//													Plan
	//##############################################################################################################################################################		
	// Create Plan-thinking for first time
	public void Implmenting_plan() {

	};

	//***************************************************************************************************
	// Update Plan - add Task
	public void UpdatePlan( UpdatePlanType UpdateType,RoadLink  _Route,  Police_TaskType _TaskType ,Responder_Police _Responder  , int _Priority ) 
	{
		switch( UpdateType) {
		case NewRes :
			MyResponders.add(_Responder);
			UnoccupiedResponders.add(_Responder); 
			_Responder.ColorCode= 6 ; // outer inner			
			break;
		case FreeRes:
			UnoccupiedResponders.add(_Responder);
			break;
		case RemoveRes:
			MyResponders.remove(_Responder);
			break;
		case NewTask:

			Task_Police foundTask=null ;

			for (Task_Police T : Operational_Plan) 
				if (T.TargetRoute == _Route && T.TaskType==_TaskType ) 
				{
					foundTask=T;
					break;
				}

			if ( foundTask==null  )
			{
				Task_Police Newtask = new Task_Police( _Route, _TaskType ); // clear or secure //TaskStatus.waiting
				Operational_Plan.add(Newtask);	
				Newtask.Priority_clearRoute=_Priority ;

			}
			else
			{
				foundTask.Priority_clearRoute= _Priority;	
			}

			break;
		case AssignTask: // to responders clear
			for (Task_Police T : Operational_Plan) 
				if ( T.TargetRoute == _Route && ( T.TaskStatus==GeneralTaskStatus.Waiting || T.TaskStatus==GeneralTaskStatus.Inprogress ) ) 
				{
					T.TaskStatus=GeneralTaskStatus.Inprogress ;
					T.TaskStartTime = Mybody.CurrentTick; 

					//UnoccupiedResponders.remove(Res ); 	// fill in allocation 	
					if ( T.TaskType==Police_TaskType.ClearRouteTraffic )
					{
						T.AssignedResponderslist.add(_Responder   );
						UnoccupiedResponders.remove(_Responder  );
					}
					else
					{
						T.AssignedResponder=_Responder   ;
						UnoccupiedResponders.remove(_Responder  );
					}


				}						
			break;	
		case  CloseTask: 
			for (Task_Police T : Operational_Plan) 
				if (T.TargetRoute == _Route && T.TaskType==_TaskType) 
				{																			
					if (T.TaskType  ==Police_TaskType.ClearRouteTraffic)
					{
						T.AssignedResponderslist.remove(_Responder);

						T.TaskStatus=GeneralTaskStatus.Done ;
						T.TaskEndTime = Mybody.CurrentTick;

					}
					else if ( T.TaskType  ==Police_TaskType.SecureRoute)
					{
						T.TaskStatus=GeneralTaskStatus.Done ;
						T.TaskEndTime = Mybody.CurrentTick;
					}					
				}

			break;}
	}

	//***************************************************************************************************	
	//Execution_plane - Task allocation	of Route ( clear 	or secure)
	public Task_Police Allocate_Policeman( Police_TaskType  Tasktypeofinterst ,  Allocation_Strategy_Route Strategy  ) 
	{
		Task_Police nominatedTask=null;

		switch(Strategy) {
		case FIFO:			
			//1- FIFO Route in list waiting
			for (Task_Police Task : Operational_Plan) 
				if (Task.TaskStatus == Ontology.GeneralTaskStatus.Waiting && Task.TaskType== Tasktypeofinterst)
				{	
					nominatedTask=Task;					
					break;
				}
			// 2- then , in progress
			if ( nominatedTask== null ) 
			{
				int Min_resp=99 ;
				for (Task_Police Task : Operational_Plan) 
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
			for (Task_Police Task : Operational_Plan) 
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

		return nominatedTask ;
	}

	//##############################################################################################################################################################	
	//													Reporting
	//##############################################################################################################################################################		
	// Cordon established or End	
	public void Reporting_plane1(ACLPerformative xx  ) {

		// send message

		if (  xx==ACLPerformative.InfromCordonEstablished  && Mybody.assignedIncident.Communication_Structure_uesd== Inter_Communication_Structure.Struc1_SilverCommandersInteraction  )
		{
			Mybody.CurrentMessage = new  ACL_Message( xx ,Mybody ,Mybody.assignedIncident.PICcommander,null, Mybody.CurrentTick ,Mybody.assignedIncident.ComMechanism_level_StoB,1 ,TypeMesg.External) ;
			Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
		}

		else if (xx==ACLPerformative.InfromCordonEstablished  &&  Mybody.assignedIncident.Communication_Structure_uesd== Inter_Communication_Structure.Struc2_BronzeCommandersInteraction  )
		{
			Mybody.CurrentMessage = new  ACL_Message( xx ,Mybody ,Mybody.assignedIncident.ALCcommander,null, Mybody.CurrentTick ,Mybody.assignedIncident.Inter_Communication_Tool_used,1,TypeMesg.External) ;
			Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
		}

		
		
		

		if (xx==ACLPerformative.InformEndClearRouteTraffic    )
		{
			Mybody.CurrentMessage = new  ACL_Message( xx ,Mybody ,Mybody.assignedIncident.PICcommander,null, Mybody.CurrentTick ,Mybody.assignedIncident.ComMechanism_level_StoB,1 ,TypeMesg.Inernal) ;
			Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
		}


	};

	//***************************************************************************************************
	// ClearedandScuredRouteReport  done
	public void Reporting_plane_ClearedandScuredRouteReportxxxxxxx(ACLPerformative xx ) 
	{	
		ArrayList<RoadLink> ClearedRoadLink_List =new ArrayList<RoadLink>();
		ClearedRoadLink_List.clear();

		for (Task_Police T :Operational_Plan ) 
			if (T.TaskStatus == GeneralTaskStatus.Inprogress && T.TaskType ==Police_TaskType.SecureRoute && T.SendReporttoPIC==false  ) 
			{
				T.SendReporttoPIC=true;
			}
		Report Report1 =new Report();
		Report1.Police_ReportRoutes( ClearedRoadLink_List) ;

		//System.out.println(Mybody.Id + "Send triage report " +casualtiesneedtoHospital_List.size() );

		if (Mybody.assignedIncident.Communication_Structure_uesd== Inter_Communication_Structure.Struc1_SilverCommandersInteraction  )
		{
			//Mybody.CurrentMessage = new  ACL_Message( xx ,Mybody ,Mybody.assignedIncident.PICcommander,Report1 , Mybody.CurrentTick ,Mybody.assignedIncident.ComMechanism_level_StoB,ClearedRoadLink_List.size()) ;
			Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
		}
		else if ( Mybody.assignedIncident.Communication_Structure_uesd== Inter_Communication_Structure.Struc2_BronzeCommandersInteraction  )
		{
			//Mybody.CurrentMessage = new  ACL_Message( xx ,Mybody ,Mybody.assignedIncident.ALCcommander,Report1 , Mybody.CurrentTick ,Mybody.assignedIncident.ComMechanism_level_StoB,ClearedRoadLink_List.size());
			Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
		}


	}

	//***************************************************************************************************
	//Execution_plane  - commanding	clear or secure 
	public void Command_Policeman(Task_Police _Task ,Responder_Police _Responder ) {

		Command CMD1 =new Command();

		if (_Task== null)
		{
			CMD1.PoliceCommand("0" ,Police_TaskType.SetupTacticalAreas);
			// send message with command
			Mybody.CurrentMessage = new  ACL_Message( ACLPerformative.Command , Mybody, _Responder ,CMD1,Mybody.CurrentTick,Mybody.assignedIncident.ComMechanism_level_BtoRes,1 ,TypeMesg.Inernal  ) ;
			Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
		}
		else if ( _Task.TaskType  ==Police_TaskType.ClearRouteTraffic)
		{
			CMD1.PoliceCommand(_Task.TaskID ,_Task.TaskType ,_Task.TargetRoute );			
			Mybody.CurrentMessage = new  ACL_Message( ACLPerformative.Command , Mybody, _Responder ,CMD1,Mybody.CurrentTick,Mybody.assignedIncident.ComMechanism_level_BtoRes ,1 ,TypeMesg.Inernal ) ;
			Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
		}	
		else if ( _Task.TaskType  ==Police_TaskType.SecureRoute)
		{	
			CMD1.PoliceCommand(_Task.TaskID ,_Task.TaskType ,_Task.TargetRoute,_Task.TargetPEA );		
			Mybody.CurrentMessage = new  ACL_Message( ACLPerformative.Command , Mybody, _Responder  ,CMD1, Mybody.CurrentTick,Mybody.assignedIncident.ComMechanism_level_BtoRes,1  ,TypeMesg.Inernal) ;
			Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
		}

	} 

	//***************************************************************************************************
	//Broadcast
	public void BoradcastAction(ACLPerformative xx)
	{		
		Mybody.CurrentMessage  = new  ACL_Message( xx , Mybody, MyResponders ,null, Mybody.CurrentTick ,CommunicationMechanism.FF_BoradCast,1 ,TypeMesg.Inernal) ;
		Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
		// break; //one by one 
	}

	//##############################################################################################################################################################	
	//													Interpreted Triggers 
	//##############################################################################################################################################################
	//1 Traffic Route
	public boolean IsThereTrafficRoute_ToAssign()
	{	
		boolean  Result=false;

		for (Task_Police T : Operational_Plan) 
			if ((T.TaskStatus == GeneralTaskStatus.Waiting || T.TaskStatus == GeneralTaskStatus.Inprogress)  && T.TaskType== Police_TaskType.ClearRouteTraffic   ) 
			{
				Result=true;

				//System.out.println(Mybody.Id + " ++++++++++++++++++++++ "  +T.TaskType + "   " + T.TargetCasualty.ID + "   " + T.AssignedResponder );
				break;
			}

		return Result;
	}

	public boolean IsCurrentRouteClosed( RoadLink  RL)
	{	
		boolean  Result= false;

		for (Task_Police T : Operational_Plan) 
			if (T.TaskStatus == GeneralTaskStatus.Done && RL==T.TargetRoute && T.TaskType== Police_TaskType.ClearRouteTraffic  &&  T.AssignedResponderslist.size() == 0  ) 
			{
				Result=true;
				//System.out.println(Mybody.Id + " ++++++++++++++++++++++ "  +T.TaskType + "   " + T.TargetCasualty.ID + "   " + T.AssignedResponder );
			}

		return Result;
	}

	//----------------------------------------------------------------------------------------------------
	//2- create Secure 
	public boolean NotYetsecure(RoadLink  _Route )
	{	
		boolean  Result=true;

		for (Task_Police T : Operational_Plan) 
			if (T.TargetRoute== _Route  &&  T.TaskType== Police_TaskType.SecureRoute   ) 
			{
				Result=false;
				//System.out.println(Mybody.Id + " ++++++++++++++++++++++ "  +T.TaskType + "   " + T.TargetCasualty.ID + "   " + T.AssignedResponder );
				break;
			}

		return Result;
	}

	//----------------------------------------------------------------------------------------------------
	//2- Done Traffic and need Secure 
	public boolean IsThereRouteneedtoSecure()
	{	
		boolean  Result=false;

		for (Task_Police T :Operational_Plan ) 
			if (T.TaskStatus == GeneralTaskStatus.Waiting &&  T.TaskType== Police_TaskType.SecureRoute  ) 
			{
				Result=true;
				break;
			}

		return Result;
	}

	//----------------------------------------------------------------------------------------------------
	//3-
	public boolean IsAllTaskclosed(Police_TaskType  Tasktypeofinterst)
	{	
		boolean  Result=true;

		for (Task_Police Task : Operational_Plan) 
			if (Task.TaskStatus != GeneralTaskStatus.Done && Task.TaskType== Tasktypeofinterst   ) 
			{
				Result=false;
				break;
			}

		return Result;
	}

	//----------------------------------------------------------------------------------------------------
	//4-
	public int Isthere_SecureRouteReport() //report
	{	
		int result=0;

		for (Task_Police T :Operational_Plan ) 
			if (T.TaskStatus == GeneralTaskStatus.Inprogress && T.TaskType == Police_TaskType.SecureRoute && T.SendReporttoPIC==false  ) 
			{
				result++;
			}
		return result;
	}

	//----------------------------------------------------------------------------------------------------		
	public boolean ISthereupdateTosend( )
	{	
		boolean  Result= false;

		if ( EndCordonEstablished==1  )
		{  Result=true; }
		//else if ( Isthere_SecureRouteReport()  > 0 )
		//{  Result=true; }

		if (  End_CC!=2  && End_nomorec_LA== 1  && IsAllTaskclosed(Police_TaskType.ClearRouteTraffic) )
		{  Result=true;End_CC=1; }


		return Result;
	}	

	//##############################################################################################################################################################
	public void TaskApproach_way(  )
	{
		if ( EndCordonEstablished>=1 && ! IsThereTrafficRoute_ToAssign()  && ! Mybody.NoMoreRouteinScene  && ( Mybody.AssignedRoute==null ) )   //Mybody.AssignedRoute==null
		{
			Mybody.Action=RespondersActions.SearchRoute;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.TaskPlanning ; //Planformulation;

			Mybody.Assign_DestinationLocation_Serach(); 
			Mybody.SensingeEnvironmentTrigger=null;
		}
		else
		{
			Mybody.Action=RespondersActions.Noaction;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.WaitingDuring;			
		}
	}

	//______________________________________________________________________________________________________________________________________________________________
	//##############################################################################################################################################################
	//                                                        Behavior
	//##############################################################################################################################################################
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	// Cordon commander - General 
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	public void CCBehavior()	
	{		
		//*************************************************************************
		// 1- initial response
		if( Mybody.CommTrigger==RespondersTriggers.AssigendRolebyPIC) 				
		{
			Mybody.CurrentAssignedActivity=Police_TaskType.GoTolocation;	
			Mybody.step_long =InputFile.step_long_ClearRoute;
		}
		// 2-Clear Route and secure
		else if (Mybody.CurrentAssignedActivity==Police_TaskType.GoTolocation && Mybody.InterpretedTrigger== RespondersTriggers.FirstTimeonLocation) 
		{
			Mybody.CurrentAssignedActivity=Police_TaskType.CoordinateOutercordon ;
		}
		// 3-Ending
		else if ( Mybody.CommTrigger==RespondersTriggers.ENDER )
		{
			Mybody.CurrentAssignedActivity=  Police_TaskType.CoordinateEndResponse ;
		}
		else if ( Mybody.InterpretedTrigger==RespondersTriggers.ENDER   )  
		{
			Mybody.PrvRoleinprint3=Mybody.Role ;
			Mybody.Role=Police_ResponderRole.None;
			Mybody.step_long =InputFile.step_long_regularewalk;

			//System.out.println(Mybody.Id+"  zzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz  Action:" );
		}

		//*************************************************************************
		switch(Mybody.CurrentAssignedActivity) {
		case GoTolocation:
			CCCommanderBehavior_GoToCordon()	;	
			break;
		case CoordinateOutercordon :			
			CCCommanderBehavior_CoordinateClearRouteTrafficandScure();
			break;			
		case CoordinateEndResponse :
			CommanderBehavior_EndER() ;
			break;
		case None:
			;
			break;}


		//System.out.println(Mybody.Id+"    Action:" + Mybody.Action + " com:" + Mybody.CommTrigger + "   Nt:" + Mybody.InterpretedTrigger+ " en:" + Mybody.SensingeEnvironmentTrigger + " end:" + Mybody.EndActionTrigger +  "Acknowledged: "+ Mybody.Acknowledged+"sending: "+  Mybody.Sending  + "CurrentMessage: ");

	}// end CC Behavior

	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	// CC - Go to location   
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^	
	private void CCCommanderBehavior_GoToCordon()	
	{
		// ++++++ 1- +++++++
		if( Mybody.CommTrigger==RespondersTriggers.AssigendRolebyPIC )
		{	
			Mybody.Assign_DestinationCordon(Mybody.AssignedCordon.EnteryPointAccess_Point);	

			Mybody.Action=RespondersActions.GoToCordon ;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.Movementonincidentsite;  
			Mybody.CommTrigger=null;

		}
		// ++++++ 2- +++++++
		else if (Mybody.Action==RespondersActions.GoToCordon && Mybody.SensingeEnvironmentTrigger==null)
		{			

			Mybody.Walk();
		}	
		// ++++++ 3- +++++++
		else if (Mybody.Action==RespondersActions.GoToCordon && Mybody.SensingeEnvironmentTrigger== RespondersTriggers.ArrivedTargetObject  ) 
		{ 					

			//send message to PIC	
			Mybody.CurrentMessage  = new  ACL_Message( ACLPerformative.InformlocationArrival , Mybody, Mybody.assignedIncident.PICcommander ,null ,  Mybody.CurrentTick ,Mybody.assignedIncident.ComMechanism_level_StoB,1 ,TypeMesg.Inernal ) ;
			Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);

			Time_last_updated=Mybody.CurrentTick;

			Mybody.Action=RespondersActions.NotifyArrival;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.Comunication ;
			Mybody.SensingeEnvironmentTrigger=null;		

		}	
		// ++++++ 4- +++++++
		else if (Mybody.Action==RespondersActions.NotifyArrival && Mybody. Acknowledged)
		{
			FirstTime= true;
			Mybody.InterpretedTrigger= RespondersTriggers.FirstTimeonLocation;
			Mybody.Acknowledged=false;Mybody.Sending=false; 	
			//System.out.println("***************** " +"Cordon: "+ Mybody.Id + " Arrived  my cordon" );
		}

	}		

	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	// CC - CoordinateClearRouteTrafficandScure
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	private void  CCCommanderBehavior_CoordinateClearRouteTrafficandScure()
	{
		// ++++++ Move +++++++	
		if (  Mybody.Action==RespondersActions.SearchRoute && Mybody.RandomllySearachRoute==true && Mybody.SensingeEnvironmentTrigger ==null   &&  Mybody.CommTrigger ==null && Mybody.InterpretedTrigger==null && Mybody.EndActionTrigger==null && Mybody.EndofCurrentAction == 0 && Mybody.Sending==false  )
		{
			Mybody.Walk();	
			//System.out.println("                                                                                     "+"Cordon: "+Mybody.Id + "  walk " + this.MyResponders.size());
		}	
		if ( Mybody.Action==RespondersActions.Noaction  &&  EndCordonEstablished>=1 && FirstTime 	) 	//&& this.UnoccupiedResponders.size() >1		 	
		{
			FirstTime=false ;

			//Mybody.RouteSeen_list.clear();
			Mybody.StopDistnation=Mybody.AssignedCordon.EnteryPointAccess_Point ;
			Mybody.ArrivedSTOP=false ;

			Mybody.CurrentDirectionNode=Mybody.NearstSerachNode() ;
			Mybody.MyDirectiontowalk=Mybody.CurrentDirectionNode.PN;
			Mybody._PointDestination= Mybody.CurrentDirectionNode.PN;

			Mybody.walkingstrategy=RandomWalking_StrategyType.Nodes_Cordon ;
			Mybody.Assign_DestinationLocation_Serach(); 


			TaskApproach_way(  );
			System.out.println("                                                                                    "+"Cordon: "+Mybody.Id + " Now star serach " +"  "+  Mybody.StopDistnation );
		}
		// ++++++ Move +++++++

		//--------------------------------------------------------------------------------------------	
		// ++++++ 1- +++++++
		if (Mybody.InterpretedTrigger== RespondersTriggers.FirstTimeonLocation)
		{
			//implent Plan
			StartSetup=true;
			FirstTime=true;
			Mybody.AssignedRoute=null ;
			Mybody.NoMoreRouteinScene=false;


			Mybody.Action=RespondersActions.FormulatePlan;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.Planformulation;  
			Mybody.EndofCurrentAction=  InputFile.FormulatePlan_duration  ; 
			Mybody.InterpretedTrigger=null; 
		}
		// ++++++ 2- +++++++
		else if ( Mybody.Action==RespondersActions.FormulatePlan  && Mybody.EndActionTrigger== RespondersTriggers.EndingAction 	) 			 	
		{
			TaskApproach_way(  ) ;

			Mybody.EndActionTrigger=null;
			//System.out.println("***************** " +"Cordon: "+ Mybody.Id + " CC Implmenting_plan" );
		}
		//-------------------------------------------------------------------------------------------- (P1)
		//==========================New / Back Responder ========================== Both  N/A( face to face )
		// ++++++ 3- +++++++
		else if ( ( Mybody.Action==RespondersActions.Noaction || Mybody.Action==RespondersActions.SearchRoute) && ( Mybody.CommTrigger == RespondersTriggers.GetNewPolicemanarrived ||Mybody.CommTrigger == RespondersTriggers.GetPolicemanComeback  ) )	  										
		{	 	
			if (Mybody.CommTrigger == RespondersTriggers.GetNewPolicemanarrived )
				UpdatePlan( UpdatePlanType.NewRes, null, null ,NewResponder ,0) ;

			else if (Mybody.CommTrigger == RespondersTriggers.GetPolicemanComeback  )
				UpdatePlan( UpdatePlanType.FreeRes,null, null ,FinshedResponder,0 ) ;

			Mybody.Action=RespondersActions.GetArrivalNotification ;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.Comunication ; 
			Mybody.EndofCurrentAction=  InputFile.GetNotification_duration ; 

		}
		// ++++++ 4- +++++++
		else if ( Mybody.Action==RespondersActions.GetArrivalNotification  && Mybody.EndActionTrigger== RespondersTriggers.EndingAction ) 			 	
		{
			Mybody.CurrentSender.Acknowledg(Mybody);	

			Mybody.Action=RespondersActions.UpdatePlan;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.Planformulation ; 
			Mybody.EndofCurrentAction=  InputFile.UdatePlan_duration  ; 
			Mybody.EndActionTrigger=null; 
			Mybody.CommTrigger=null;

			//System.out.println("                                                                                    "+"Cordon: "+  Mybody.Id  +".GetNewPolicemanarrived  "   );
		}
		//==========================Result of Setup Cordon ==========================  Both N/A
		// ++++++ 5- +++++++
		else if (( Mybody.Action==RespondersActions.Noaction || Mybody.Action==RespondersActions.SearchRoute) &&  Mybody.CommTrigger== RespondersTriggers.GetresultofSetup )	  										
		{	 	
			//Update plan
			UpdatePlan( UpdatePlanType.FreeRes,null, null ,FinshedResponder ,0 ) ;
			EndCordonEstablished=0;

			AllocatedResponderfroSetup --;
			if  ( AllocatedResponderfroSetup == 0)
			{
				EndCordonEstablished = 1 ;
				StartSetup=false;

				System.out.println("                                                                                    "+"Cordon: "+  Mybody.Id  +" Done Setup   " +this.MyResponders.size()  );
			}

			Mybody.Action=RespondersActions.GetResultofSetupTA ;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.Comunication ; 
			Mybody.EndofCurrentAction=  InputFile.GetNotification_duration  ; 
			Mybody.CommTrigger=null; 			
		}
		// ++++++ 6- +++++++
		else if ( Mybody.Action==RespondersActions.GetResultofSetupTA  && Mybody.EndActionTrigger== RespondersTriggers.EndingAction 	) 			 	
		{
			Mybody.CurrentSender.Acknowledg(Mybody);	

			Mybody.Action=RespondersActions.UpdatePlan;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.Planformulation;  
			Mybody.EndofCurrentAction=  InputFile.UdatePlan_duration  ; 
			Mybody.EndActionTrigger=null; 
			//System.out.println( "                                                                                    "+"Cordon: " +  Mybody.Id  +" Send Acknowledged  "+Mybody.CurrentSender.Id   );
		}

		//==========================Result of cleared route ==========================  Paper/Electronic   
		// ++++++ 7- +++++++
		else if ( ( Mybody.Action==RespondersActions.Noaction || Mybody.Action==RespondersActions.SearchRoute) && ( Mybody.CommTrigger== RespondersTriggers.GetResultCleardRoute 	 )  ) 
		{		
			//System.out.println("                                                                                    "+"Cordon: "+ Mybody.Id + " GetResult  route  "  );  

			Mybody.Action=RespondersActions.GetResultCleardRoute ;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.Comunication;  
			Mybody.EndofCurrentAction= InputFile.GetInfromation_FtoForR_Duration_Notification ; 
			Mybody.CommTrigger=null; 
		}
		// ++++++ 8- +++++++
		else if ( Mybody.Action==RespondersActions.GetResultCleardRoute  && Mybody.EndActionTrigger== RespondersTriggers.EndingAction 	) 			 	
		{					
			RoadLink  RL=null;
			Responder_Police Res=null;

			//============ 1 ============ 

			Mybody.CurrentSender.Acknowledg(Mybody);				
			RL= CurrentRoutebySender ;
			Res=(Responder_Police) Mybody.CurrentSender;		


			//============ 2 ============ 
			//update plan
			UpdatePlan( UpdatePlanType.CloseTask, RL ,Police_TaskType.ClearRouteTraffic,Res ,0);	// ready to send to PIC 
			UpdatePlan( UpdatePlanType.FreeRes, null,null,Res,0) ;
			//if ( NotYetsecure (RL))
			//UpdatePlan( UpdatePlanType.NewTask, RL ,Police_TaskType.SecureRoute,null );

			if (this.IsCurrentRouteClosed(RL))
			{
				System.out.println( "                                                                                    "+"Cordon: "+ Mybody.Id + " donnnnnnnnnnnnnnnnne   route:  "+ Mybody.AssignedRoute.fid+ "  " + Mybody.AssignedRoute.TrafficLevel +"  " + Mybody.AssignedRoute.descript_1 + " ResNum:"  + this.UnoccupiedResponders.size());				
				Mybody.AssignedRoute=null ;
			}

			Mybody.Action=RespondersActions.UpdatePlan;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.Planformulation; 
			Mybody.EndofCurrentAction= InputFile.UdatePlan_duration ; 
			Mybody.EndActionTrigger=null; 

		}

		//==========================GetRouteProirtytoclear From PIC==========================   N/A 
		// ++++++ 9- +++++++
		else if ( ( Mybody.Action==RespondersActions.Noaction || Mybody.Action==RespondersActions.SearchRoute) && ( Mybody.CommTrigger== RespondersTriggers.GetRouteProirtytoclearFromPIC  ))	  										
		{	 	
			int total=0;

			Mybody.CurrentSender.Acknowledg(Mybody);	
			//UpdatePlan	
			for ( RoadLink  RL :  CurrentRoutebySender_List )
			{
				UpdatePlan( UpdatePlanType.NewTask, RL ,Police_TaskType.ClearRouteTraffic,null,1 );  //we can set proirity
				total++;
			}
			CurrentRoutebySender_List.clear();

			Mybody.Action=RespondersActions.GetRouteProirtytoclearFromPIC;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.Comunication; 
			Mybody.EndofCurrentAction=  InputFile.GetInfromation_FtoForR_Duration_Data * total ; 
			Mybody.CommTrigger=null; 

			System.out.println( "                                                                                    "+"Cordon: "+ Mybody.Id +" GetRouteProirtytoclear " + total  + "<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<" );
		}
		// ++++++ 10- +++++++
		else if ( Mybody.Action==RespondersActions.GetRouteProirtytoclearFromPIC && Mybody.EndActionTrigger== RespondersTriggers.EndingAction 	) 			 	
		{

			for (Task_Police Task : Operational_Plan) 
				System.out.println( "                                                                                    "+"Cordon: "+ Mybody.Id +" Route: "+ Task.TargetRoute.fid+ "  " + Task.TargetRoute.TrafficLevel+"  " + Task.TargetRoute.descript_1 + " " + Task.TaskStatus + "  " + Task.Priority_clearRoute);

			Mybody.Action=RespondersActions.UpdatePlan;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.Planformulation; 
			Mybody.EndofCurrentAction= InputFile.UdatePlan_duration ; 
			Mybody.EndActionTrigger=null; 
		}
		//========================== Updates from PIC  ==========================		
		// ++++++ 13- +++++++
		else if ( ( Mybody.Action==RespondersActions.Noaction || Mybody.Action==RespondersActions.SearchRoute)   &&  Mybody.CommTrigger== RespondersTriggers.GetSAupdatefromPIC     )											
		{	 							 
			//Updates about SA

			if (   End_nomorec_LA==0)
			{	End_nomorec_LA=1;Mybody.NoMoreRouteinScene= true;   System.out.println( "                                                                                    "+"Cordon: " + Mybody.Id +" LA GetNoMorecasualty" );}

			Mybody.Action=RespondersActions.GetSAupdatefromPIC  ;		
			Mybody.BehaviourType3=RespondersBehaviourTypes3.Comunication;  
			Mybody.EndofCurrentAction= InputFile.GetInfromation_FtoForR_Duration_Data ; 			
		}
		// ++++++ 14- +++++++
		if (Mybody.Action==RespondersActions.GetSAupdatefromPIC  && Mybody.EndActionTrigger== RespondersTriggers.EndingAction 	  )//InputFile.CasualtyReport_Mechanism == CasualtyReportandTrackingMechanism.Paper		
		{
			Mybody.CurrentSender.Acknowledg(Mybody);

			Mybody.Action=RespondersActions.UpdatePlan;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.Planformulation; 
			Mybody.EndofCurrentAction= InputFile.UdatePlan_duration ; 
			Mybody.EndActionTrigger=null; 
			Mybody.CommTrigger=null; 
		}
		//============================================================ ALL
		// ++++++ 11- +++++++
		else if (Mybody.Action==RespondersActions.UpdatePlan && Mybody.EndActionTrigger== RespondersTriggers.EndingAction  )
		{						
			TaskApproach_way();
			Mybody.EndActionTrigger=null;
		}

		//-------------------------------------------------------------------------------------------- (P2)  Paper
		// ++++++ 12- +++++++
		else  if ( ( Mybody.Action==RespondersActions.Noaction || Mybody.Action==RespondersActions.SearchRoute)   && 
				(		
						(  Mybody.assignedIncident.Communication_Structure_uesd==Inter_Communication_Structure.Struc1_SilverCommandersInteraction  && ( ( ISthereupdateTosend( ) &&  Mybody.assignedIncident.Intra_Communication_Time_used==Communication_Time.When_need)   )) 
						||
						(  Mybody.assignedIncident.Communication_Structure_uesd==Inter_Communication_Structure.Struc2_BronzeCommandersInteraction  && (  ( Mybody.CurrentTick >= (Time_last_updated +  Mybody.assignedIncident.UpdatOPEvery) &&  Mybody.assignedIncident.Inter_Communication_Time_used==Communication_Time.Every_frequently )  ||  ( ISthereupdateTosend( ) &&  Mybody.assignedIncident.Inter_Communication_Time_used==Communication_Time.When_need)   )) 
						)				
				)
		{			

			if (ISthereupdateTosend( ) ) 				
			{ 
				if ( EndCordonEstablished==1  )
				{ Reporting_plane1(ACLPerformative.InfromCordonEstablished ) ; EndCordonEstablished= 2; }

				//if ( Isthere_SecureRouteReport()  > 0 )
				//{ Reporting_plane_ClearedandScuredRouteReport(ACLPerformative.InformClearandSecurOfRouteReport ) ; }

				if (End_CC==1 )
				{this.Reporting_plane1(ACLPerformative.InformEndClearRouteTraffic); End_CC=2;}

				Mybody.Action=RespondersActions.InfromOPReport;
				Mybody.BehaviourType3=RespondersBehaviourTypes3.Comunication;  

				System.out.println( "                                                                                    "+"Cordon: "+  Mybody.Id  +" update PIC  "   );		
				for ( ACL_Message  A:Mybody.CurrentMessage_list )
					System.out.println( "                                                                                    "+"Cordon: "+   Mybody.Id  +"  InfromOPReport    receiver:" + ( (Responder )A.receiver).Id  +"  " + A.performative   +"  sending " + ( (Responder )A.receiver).Sending  );

			}
			else
			{
				Mybody.Action=RespondersActions.UpdatePlan;
				Mybody.BehaviourType3=RespondersBehaviourTypes3.Planformulation; 
				Mybody.EndofCurrentAction=  InputFile.UdatePlan_duration  ; 				
				Time_last_updated=Mybody.CurrentTick;
			}	
		}
		else if (Mybody.Action==RespondersActions.InfromOPReport && Mybody. Acknowledged )
		{
			Mybody.Action=RespondersActions.Noaction;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.WaitingDuring;
			Mybody.Acknowledged=false;Mybody.Sending=false; 
			Time_last_updated=Mybody.CurrentTick;

		}
		//-------------------------------------------------------------------------------------------- (P3) 
		//============= setup ======================
		// ++++++ 13- +++++++
		else if ( Mybody.Action==RespondersActions.Noaction && ( StartSetup && EndCordonEstablished== -1 && ! Mybody.AssignedCordon.installed)  && UnoccupiedResponders.size() >0 )		  										
		{

			AllocatedRespondertoTask=UnoccupiedResponders.get(0)  ;
			UnoccupiedResponders.remove(0);
			AllocatedResponderfroSetup ++;

			Mybody.Action=RespondersActions.AllocatResponders;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.Planformulation;
			Mybody.EndofCurrentAction=   InputFile.Allocation_duration ;

			//System.out.println( "                                                                                    "+"Cordon: " +  Mybody.Id  +" allcate setup  "   );
		}
		// ++++++ 14- +++++++
		else if (Mybody.Action==RespondersActions.AllocatResponders &&  StartSetup &&  Mybody.EndActionTrigger== RespondersTriggers.EndingAction  )
		{				
			Command_Policeman(null ,AllocatedRespondertoTask );

			Mybody.Action=RespondersActions.CommandSetupTA ;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.Comunication; //TaskPlanning ; 		
			Mybody.EndActionTrigger=null;					
		}

		// ++++++ 15- +++++++
		else if (Mybody.Action==RespondersActions.CommandSetupTA && Mybody. Acknowledged )
		{
			Mybody.Action=RespondersActions.Noaction;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.WaitingDuring;
			Mybody.Acknowledged=false;Mybody.Sending=false; 
			//System.out.println( "                                                                                    "+"Cordon: " +  Mybody.Id  +" Get Acknowledged  "   );
		}
		//============= Secure ====================== //not uesd
		// ++++++ 16- +++++++
		else if (  ( Mybody.Action==RespondersActions.Noaction || Mybody.Action==RespondersActions.SearchRoute )  &&  IsThereRouteneedtoSecure() && UnoccupiedResponders.size() >0 )		 //   Mybody.AssignedRoute!=null ;  										
		{
			//check for different Priority
			AllocatedTask = Allocate_Policeman( Police_TaskType.SecureRoute ,  Allocation_Strategy_Route.FIFO   ) ;
			AllocatedRespondertoTask=UnoccupiedResponders.get(0)  ;
			OptionTaskType= 1; 

			Mybody.Action=RespondersActions.AllocatResponders;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.Planformulation;
			Mybody.EndofCurrentAction=   InputFile.Allocation_duration ; 
		}
		// ++++++ 17- +++++++
		else if (Mybody.Action==RespondersActions.AllocatResponders &&  OptionTaskType== 1 &&  Mybody.EndActionTrigger== RespondersTriggers.EndingAction  )
		{				
			Command_Policeman(AllocatedTask ,AllocatedRespondertoTask );
			UpdatePlan( UpdatePlanType.AssignTask, AllocatedTask.TargetRoute , Police_TaskType.SecureRoute , AllocatedRespondertoTask ,0 );	

			Mybody.Action=RespondersActions.CommandPolicemanSecure ;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.Comunication; //TaskPlanning ;  		
			Mybody.EndActionTrigger=null;					
		}

		// ++++++ 18- +++++++
		else if (Mybody.Action==RespondersActions.CommandPolicemanSecure && Mybody. Acknowledged )
		{
			Mybody.Action=RespondersActions.Noaction;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.WaitingDuring;
			Mybody.Acknowledged=false;Mybody.Sending=false; 		
		}
		//============= clear traffic ======================
		// ++++++ 19- +++++++
		else if (  Mybody.Action==RespondersActions.Noaction  &&  Mybody.AssignedRoute==null && IsThereTrafficRoute_ToAssign() && UnoccupiedResponders.size() >0 )	
		{	
			AllocatedTask = Allocate_Policeman(Police_TaskType.ClearRouteTraffic,Allocation_Strategy_Route.SliverPriorty ) ;	//InputFile.Allocation_Strategy_ 	//one time					
			Mybody.AssignedRoute = AllocatedTask.TargetRoute ;

			if (  Mybody.AssignedRoute.InsideCordon==true )
			{
				Mybody.Assign_DestinationCordon(Mybody.AssignedRoute.PointofClear);
				Mybody.Action=RespondersActions.GoToRoute ;
				Mybody.BehaviourType3=RespondersBehaviourTypes3.Movementonincidentsite;  //TaskPlanning; 
				System.out.println("                                                                                    "+"Cordon: "+Mybody.Id + "GOOOOOOOOOOOOOOo to " + Mybody.AssignedRoute.descript_1);
			}
			else if (  Mybody.AssignedRoute.InsideCordon==false )
			{
				Mybody.Assign_DestinationCordon(Mybody.AssignedCordon.EnteryPointAccess_Point);
				Mybody.Action=RespondersActions.GoToRoute ;
				Mybody.BehaviourType3=RespondersBehaviourTypes3.Movementonincidentsite;  //TaskPlanning; 
				System.out.println("                                                                                    "+"Cordon: "+Mybody.Id + "PEAAAAAAAAAAAAAAAAAAAAAAAAA " + Mybody.AssignedRoute.descript_1);
			}
//			else if (  Mybody.AssignedRoute.InsideCordon==false   )
//			{
//				Mybody.Action=RespondersActions.UpdatePlan;
//				Mybody.BehaviourType3=RespondersBehaviourTypes3.Planformulation;  
//				Mybody.EndofCurrentAction=  InputFile.UdatePlan_duration  ;
//				System.out.println("                                                                                    "+"Cordon: "+Mybody.Id + "StopPPPPPPPPPPPP and send them to go  " + Mybody.AssignedRoute.descript_1);
//			}
			
		}
		// ++++++ 20- +++++++
		else if (  Mybody.Action==RespondersActions.GoToRoute &&  Mybody.SensingeEnvironmentTrigger==null) 												
		{	
			Mybody.Walk();
		}
		// ++++++ 21- +++++++
		else if (  (Mybody.Action==RespondersActions.GoToRoute  && Mybody.SensingeEnvironmentTrigger== RespondersTriggers.ArrivedTargetObject ) ||
				( Mybody.Action==RespondersActions.Noaction && Mybody.AssignedRoute!=null &&  IsThereTrafficRoute_ToAssign()  && UnoccupiedResponders.size() >0 )	)
		{			

			AllocatedRespondertoTask=AllocatedTask.ckeck_Distance_befor_TaskAssignment_Route(UnoccupiedResponders) ;

			Mybody.Action=RespondersActions.AllocatResponders;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.Planformulation;
			Mybody.EndofCurrentAction=   InputFile.Allocation_duration ;

			if ( Mybody.SensingeEnvironmentTrigger != null )Mybody.SensingeEnvironmentTrigger=null;

			System.out.println( "                                                                                    "+"Cordon: " + Mybody.Id + "  Tasked" + AllocatedRespondertoTask.Id  );
		}
		// ++++++ 22- +++++++
		else if (Mybody.Action==RespondersActions.AllocatResponders && Mybody.EndActionTrigger== RespondersTriggers.EndingAction  )
		{																
			Command_Policeman(AllocatedTask, AllocatedRespondertoTask);
			UpdatePlan( UpdatePlanType.AssignTask, AllocatedTask.TargetRoute, Police_TaskType.ClearRouteTraffic , AllocatedRespondertoTask ,0);		// responder assigned inside  allocation

			Mybody.Action=RespondersActions.CommandPolicemanClearRoute ;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.Comunication; //TaskPlanning ; 			
			Mybody.EndActionTrigger=null;
		}
		// ++++++ 23- +++++++
		else if (Mybody.Action==RespondersActions.CommandPolicemanClearRoute && Mybody. Acknowledged )
		{
			TaskApproach_way();
			Mybody.Acknowledged=false;Mybody.Sending=false; 		
			//System.out.println("                                                                                    "+"Cordon: "+ Mybody.Id + "  done Tasked" + AllocatedRespondertoTask.Id  );
		}
		//-------------------------------------------------------------------------------------------- (P4)  N/A
		// ++++++ 24- +++++++
		else if (  Mybody.Action==RespondersActions.SearchRoute &&  Mybody.SensingeEnvironmentTrigger== RespondersTriggers.SensedRoute )
		{									
			UpdatePlan( UpdatePlanType.NewTask,Mybody.TargetRoute ,Police_TaskType.ClearRouteTraffic,null ,0);
			System.out.println( "                                                                                    "+"Cordon: "+Mybody.Id + " added :"+ Mybody.TargetRoute.fid+"  " + Mybody.TargetRoute.descript_1);

			Mybody.Action=RespondersActions.UpdatePlan;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.Planformulation;  
			Mybody.EndofCurrentAction=  InputFile.UdatePlan_duration  ; 
			Mybody.SensingeEnvironmentTrigger=null; 							
		}		
		// ++++++ 25- +++++++
		else if (  Mybody.Action==RespondersActions.SearchRoute &&  Mybody.SensingeEnvironmentTrigger== RespondersTriggers.Arrived_WalkedinAllScene)
		{									
			//update plan
			Mybody.NoMoreRouteinScene= true;

			Mybody.Action=RespondersActions.UpdatePlan;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.Planformulation;  
			Mybody.EndofCurrentAction=  InputFile.UdatePlan_duration  ; 
			Mybody.SensingeEnvironmentTrigger=null; 

			System.out.println( "                                                                                    "+"Cordon: "+ Mybody.Id  +" Arrived_WalkedinAllScene" ); 
		}
		//------------------------------------------End-------------------------------------------------- (P5) 


		// ++++++ 26- +++++++	
		else if (  ( Mybody.Action==RespondersActions.Noaction ||Mybody.Action==RespondersActions.SearchRoute )   && End_CC==2  && this.IsAllTaskclosed(Police_TaskType.ClearRouteTraffic) )
		{		
			End_nomorec_LA=2 ;
			End_CC=3;		
			BoradcastAction(ACLPerformative.InformEndClearRouteTraffic);

			Mybody.Action=RespondersActions.BoradcastEndScene  ;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.Comunication;  
			Mybody.EndofCurrentAction=   InputFile.Boradcast_duration ;
			Mybody.Acknowledged=false;Mybody.Sending=false; 	

		}
		// ++++++ 27- +++++++
		else if (Mybody.Action==RespondersActions.BoradcastEndScene  && Mybody.EndActionTrigger== RespondersTriggers.EndingAction  )			
		{				

			Mybody.Action=RespondersActions.Noaction;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.WaitingEnd;
			Mybody.Sending=false; 
			Mybody.EndActionTrigger=null;


			for (Task_Police Task : Operational_Plan) 
				System.out.println( "                                                                                    "+"Cordon: "+  Task.TaskStatus +" Route: "+ Task.TargetRoute.fid+"  " + Task.TargetRoute.descript_1 + " "  + Task.TargetRoute.TrafficLevel);

			System.out.println( "                                                                                    "+"Cordon: "+ Mybody.Id  +" done **************1***2*****************" ); 
		}

	}

	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	// CC -  CoordinateEndResponse  
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	private void CommanderBehavior_EndER()
	{
		// ++++++ 1- +++++++
		if ( Mybody.CommTrigger==RespondersTriggers.ENDER ) 
		{
			Mybody.Action=RespondersActions.GetNotificationEndER ;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.Comunication ;  
			Mybody.EndofCurrentAction= InputFile.GetNotification_duration ;
			Mybody.CommTrigger=null; 

		}
		// ++++++ 2- +++++++
		else if (Mybody.Action==RespondersActions.GetNotificationEndER  && Mybody.EndActionTrigger== RespondersTriggers.EndingAction   )
		{
			// Send message to its Policmen	
			BoradcastAction(ACLPerformative.InformERend );

			Mybody.Action=RespondersActions.BoradcastEndER ;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.Comunication; 
			Mybody.EndofCurrentAction=   InputFile.Boradcast_duration ;
			Mybody.EndActionTrigger=null;					
		}
		// ++++++ 3- +++++++
		else if (Mybody.Action==RespondersActions.BoradcastEndER && Mybody.EndActionTrigger== RespondersTriggers.EndingAction  )
		{		
			Mybody.InterpretedTrigger=RespondersTriggers.ENDER; //make sure no still task

			Mybody.Action=RespondersActions.Noaction;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.WaitingEnd;

			Mybody.Sending=false; 
			Mybody.EndActionTrigger= null;

			System.out.println("                                                                                    " + Mybody.Id  +" GO back to Vehicle  " +Mybody.Role );
		}
	}

}//end class


