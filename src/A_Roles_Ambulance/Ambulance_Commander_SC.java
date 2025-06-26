package A_Roles_Ambulance;

import java.util.ArrayList;
import java.util.List;
import A_Agents.Casualty;
import A_Agents.Hospital;
import A_Agents.Responder;
import A_Agents.Responder_Ambulance;
import A_Agents.Responder_Fire;
import A_Agents.Vehicle;
import A_Environment.Sector;
import B_Classes.Task_ambulance;
import B_Communication.ACL_Message;
import B_Communication.Casualty_info;
import B_Communication.Command;
import B_Communication.ISRecord;
import B_Communication.Report;
import C_SimulationInput.InputFile;
import D_Ontology.Ontology; 
import D_Ontology.BuildStaticFuction;
import D_Ontology.Ontology.ACLPerformative;
import D_Ontology.Ontology.Allocation_Strategy;
import D_Ontology.Ontology.Ambulance_ResponderRole;
import D_Ontology.Ontology.Ambulance_TaskType;
import D_Ontology.Ontology.CasualtyReportandTrackingMechanism;
import D_Ontology.Ontology.CommunicationMechanism;
import D_Ontology.Ontology.GeneralTaskStatus;
import D_Ontology.Ontology.Inter_Communication_Structure;
import D_Ontology.Ontology.RandomWalking_StrategyType;
import D_Ontology.Ontology.RespondersActions;
import D_Ontology.Ontology.RespondersBehaviourTypes1;
import D_Ontology.Ontology.RespondersBehaviourTypes2;
import D_Ontology.Ontology.RespondersTriggers;
import D_Ontology.Ontology.TaskAllocationApproach;
import D_Ontology.Ontology.TaskAllocationMechanism;
import D_Ontology.Ontology.TypeMesg;
import D_Ontology.Ontology.UpdatePlanType;

public class Ambulance_Commander_SC {

	Responder_Ambulance Mybody; //

	//----------------------------------------------------
	Responder_Ambulance NewResponder=null,FinshedResponder=null, AllocatedRespondertoTask=null;

	Casualty_info CurrentCasualtybySender_info=null;
	ArrayList<Casualty> CurrentTrapped_ListbySender =new ArrayList<Casualty>();
	ArrayList<Casualty> Trapped_List =new ArrayList<Casualty>();

	ISRecord Record_CurrentCasualty=null; 
	
	Task_ambulance  AllocatedTask , CurrentCheckedTask=null;
	int NoMoreCasualtyCounter=0;
	boolean FirstTime=true;
	//----------------------------------------------------
	//  The meaning of attribute content....  -1:No update   , 0: Get  , 1: realize or done  2: send to workers or PIC
	int  SafetyBriefandSectorEstablished = -1 ; 
	int End_nomorecinScene= -1 ;
	int Taskoption=0;

	boolean Me_NomoreinInner=false;
	//----------------------------------------------------

	List<Task_ambulance> Operational_Plan = new ArrayList<Task_ambulance>();
	List<Responder_Ambulance> MyResponders = new ArrayList<Responder_Ambulance>(); //its worker
	List<Responder_Ambulance> UnoccupiedResponders = new ArrayList<Responder_Ambulance>(); //free worker
	List<Responder_Ambulance> NewRespondrsNotinfromedSB = new ArrayList<Responder_Ambulance>(); //its worker
	//----------------------------------------------------

	double Time_last_updated=0;	

	//##############################################################################################################################################################	
	public Ambulance_Commander_SC ( Responder_Ambulance _Mybody ,Sector  _AssignedSector ) 
	{
		Mybody=_Mybody;

		Mybody.EndofCurrentAction=0;	
		Mybody.AssignedSector=_AssignedSector;
		Mybody.AssignedSector.SCcommander=Mybody;

		if (Mybody.AssignedSector.ID==1 )  { Mybody.ColorCode=1 ; }  // Mybody.AssignedSector.ColorCode= 1;
		if (Mybody.AssignedSector.ID==2 ) { Mybody.ColorCode=2  ; }  
		if (Mybody.AssignedSector.ID==3 ) { Mybody.ColorCode=3 ; }  
		if (Mybody.AssignedSector.ID==4 ) { Mybody.ColorCode= 4;  }  

		Mybody.PrvRoleinprint1 =Mybody.Role;
		Mybody.ColorCode=5 ;
		//if (Mybody.assignedIncident.TaskMechanism_IN==TaskAllocationMechanism.NoEnvironmentsectorization)  //no need to color
		//Mybody.AssignedSector.ColorCode= 0;

		Mybody.assignedIncident.AmSCcommander1=Mybody;   // temp because there are four  but I put this one becasue I have one right now

	}

	//##############################################################################################################################################################
	public void  CommanderSC_InterpretationMessage()
	{
		boolean  done= true;
		ACL_Message currentmsg =  Mybody.Message_inbox.get(Mybody.Lastmessagereaded);		 			
		Mybody.CurrentSender= (Responder)currentmsg.sender;
		Mybody.Lastmessagereaded++;
		
		Mybody.SendingReciving_External= false ; Mybody.SendingReciving_internal=false; 
		if (  currentmsg.Inernal) Mybody.SendingReciving_internal= true ;
		else if (  currentmsg.External) Mybody.SendingReciving_External=true ;

		switch( currentmsg.performative) {

		case InformNewResponderArrival:
			Mybody.CommTrigger= RespondersTriggers.GetNewParamedicarrived;	
			NewResponder= (Responder_Ambulance) Mybody.CurrentSender;
			break;
		case  InformResultFieldTriage:
			Mybody.CommTrigger= RespondersTriggers.GetresultofTriage;
			CurrentCasualtybySender_info = ((Casualty_info)currentmsg.content);
			FinshedResponder= (Responder_Ambulance) Mybody.CurrentSender;
			break;
		case  InformResultPre_RescueTreatment:  
			Mybody.CommTrigger= RespondersTriggers.GetresultofPre_RescueTreatment;
			CurrentCasualtybySender_info = ((Casualty_info)currentmsg.content);
			FinshedResponder= (Responder_Ambulance) Mybody.CurrentSender;
			break;		
		case InformNomorecasualtyScene : //Decentralized  
			Mybody.CommTrigger= RespondersTriggers.NoMorecasualty; 
			break;
			//-------------------------
		case InfromSafetyBriefandSectorEstablished : // form AIC
			Mybody.CommTrigger= RespondersTriggers.GetSAupdatefromAIC ;	
			SafetyBriefandSectorEstablished=0;
			break;
			//------------------------------- 	
		case InformNomorecasualty_impact : // in inner From AIC or FRSC		
			Mybody.CommTrigger= RespondersTriggers.GetSAupdatefromAIC ;
			End_nomorecinScene= 0;
			break;
		case InformTrappedcasualty :
			Mybody.CommTrigger= RespondersTriggers.GetSAupdatefromAIC ;
			Report CurrentReport=((Report) currentmsg.content);
			CurrentTrapped_ListbySender=CurrentReport.trapped_List;	
			break;			
		case InformERend :
			Mybody.CommTrigger= RespondersTriggers.ENDER;
			//System.out.println(Mybody.Id + "   Sector"+Mybody.AssignedSector.ID  +"===================================================================" + Mybody.assignedIncident.ID );

			break;	
		} // end switch

		//System.out.println(Mybody.Id + "  " + Mybody.CommTrigger + "  from  "+ Mybody.CurrentSender.Id  + "    Action:" + Mybody.Action);
	}	

	//##############################################################################################################################################################	
	//													Plan
	//##############################################################################################################################################################		
	// Create Plan-thinking for first time
	public void Implmenting_plan() {

		//called in CentralizedDirectSupervision only

		Mybody.Reset_DirectioninSearach( ); //its memory
		Mybody.ClockDirection=1;
		Mybody.CasualtySeen_list.clear();

		if (Mybody.assignedIncident.TaskMechanism_IN==TaskAllocationMechanism.NoEnvironmentsectorization)
		{								
			//Mybody.MyDirectiontowalk=Mybody.AssignedSector.Startpoint;
			//Mybody.NextRotateDirectionSearch_bigRoute(Mybody.ClockDirection);
			//Mybody.walkingstrategy=RandomWalking_StrategyType.FourDirections_big;	

			Mybody.Reset_DirectioninSearach( ); //its memory
			if (Mybody.Randomizer.nextInt(2)==0 ) Mybody.ClockDirection= 1; else Mybody.ClockDirection= -1; 
			Mybody.MyDirectiontowalk=Mybody.assignedIncident.IdentifyNearest_small(Mybody.Return_CurrentLocation());
			Mybody.NextRotateDirectionSearch_smallRoute(Mybody.ClockDirection);
			Mybody.walkingstrategy=RandomWalking_StrategyType.FourDirections_small;


			//Mybody.MyDirectiontowalk=Mybody.AssignedSector.Limitpoint;
			//Mybody.walkingstrategy=RandomWalking_StrategyType.OneDirection_sector;				
		}
		else if (Mybody.assignedIncident.TaskMechanism_IN==TaskAllocationMechanism.Environmentsectorization)
		{		
			Mybody.MyDirectiontowalk=Mybody.AssignedSector.Limitpoint;
			Mybody.walkingstrategy=RandomWalking_StrategyType.OneDirection_sector;									
		}

		Mybody.Setup_ConsiderSeverityofcasualtyinSearach(false ); // No Severity
		Mybody.Assign_DestinationLocation_Serach();  //""""??????????????????????????????????????						
		Mybody.StopDistnation=Mybody.AssignedSector.centerofsectorPoint;
		Mybody.ArrivedSTOP=false;
		Mybody.ExpandFieldofVision=false;

	};

	//***************************************************************************************************
	// Update Plan - add Casualty/Task
	public void UpdatePlan( UpdatePlanType UpdateType, Casualty _Casualty, Ambulance_TaskType _TaskType ,Responder_Ambulance _Responder , Hospital  _AssignedHospital , Vehicle  _AssignedAmbulance  ) 
	{

		switch( UpdateType) {
		case NewRes :
			MyResponders.add(_Responder);
			UnoccupiedResponders.add(_Responder);
			//_Responder.ColorCode=Mybody.ColorCode ;
			// _Responder.ColorCode=Mybody.AssignedSector.ID;
			break;
		case FreeRes:
			UnoccupiedResponders.add(_Responder);
			break;
		case RemoveRes:
			MyResponders.remove(_Responder);
			break;
		case NewTask:
			Task_ambulance Newtask = new Task_ambulance( _Casualty, _TaskType ); //TaskStatus.waiting
			Operational_Plan.add(Newtask);
			//Mybody.Lastcoordinationcasualty_list.add(_Casualty );
			break;
		case AssignTask:
			for (Task_ambulance T : Operational_Plan) 
				if (T.TargetCasualty == _Casualty && T.TaskType==_TaskType && T.TaskStatus==GeneralTaskStatus.Waiting) 
				{
					T.TaskStatus=GeneralTaskStatus.Inprogress ;
					T.TaskStartTime = Mybody.CurrentTick; //Task.AssignedResponder in allocation
					UnoccupiedResponders.remove(T.AssignedResponder );
				}						
			break;
		case  CloseTask: 
			for (Task_ambulance T : Operational_Plan) 
				if (T.TargetCasualty == _Casualty ) //&& T.TaskType==_TaskType
				{
					T.TaskStatus=GeneralTaskStatus.Done;	
					T.TaskEndTime = Mybody.CurrentTick;
				}
			break;
			//=====================================
		case  TrackTask:	 //  Decentralized
			Task_ambulance Newtask1 = new Task_ambulance( _Casualty, _TaskType ,_Responder ); //done
			Operational_Plan.add(Newtask1);
			break;		
		case TrackandUpdateTask_stop:   //  Decentralized
			for (Task_ambulance T : Operational_Plan) 
				if (T.TargetCasualty == _Casualty && T.TaskType==_TaskType ) 
				{ T.TaskStatus=GeneralTaskStatus.Stop; T.AssignedResponder=_Responder ; T.TimeofdoneTreatment= Mybody.CurrentTick; }
			break;}
	}

	//***************************************************************************************************	
	//Execution_plane - Task Allocation		
	public Task_ambulance Allocate_paramedic( Ambulance_TaskType  Tasktypeofinterst ,  Allocation_Strategy Strategy) 
	{

		Task_ambulance nominatedTask=null;
		Responder nominatedResp=null ;

		switch(Strategy) {
		case FIFO:			
			//FIFO casualty
			for (Task_ambulance Task : Operational_Plan) 
				if (Task.TaskStatus == Ontology.GeneralTaskStatus.Waiting && Task.TaskType== Tasktypeofinterst)
				{
					Responder Resp =Task.ckeck_Distance_befor_TaskAssignment(UnoccupiedResponders);		
					nominatedTask=Task;
					nominatedResp=Resp;
					break;
				}

			break;
		case FIFO_Pre :	//for prerescue_decentralized		temp solution
			//FIFO casualty
			for (Task_ambulance Task : Operational_Plan) 
				if (Task.TaskStatus == Ontology.GeneralTaskStatus.Waiting && Task.TaskType== Tasktypeofinterst)
				{
					Responder Resp =Task.ckeck_Distance_befor_TaskAssignment_pre(Mybody, this.UnoccupiedResponders);		
					nominatedTask=Task;
					nominatedResp=Resp;
					//nominatedResp.ColorCode= 6;
					break;
				}
			break;	
		case SmallestDistance:
			//Smallest distance
			double smallest_dis=999999 ; 

			for (Task_ambulance Task : Operational_Plan)
				if (Task.TaskStatus == Ontology.GeneralTaskStatus.Waiting && Task.TaskType== Tasktypeofinterst )
				{
					Responder Resp=Task.ckeck_Distance_befor_TaskAssignment(UnoccupiedResponders);
					double dis = BuildStaticFuction.DistanceC(Task.TargetCasualty.geography, Resp.Return_CurrentLocation(),Task.TargetCasualty.getCurrentLocation());

					if (dis<smallest_dis)
					{ smallest_dis= dis; nominatedResp=Resp ;  nominatedTask=Task;}

				}
			break;}

		if ( nominatedTask !=null ) nominatedTask.AssignedResponder= nominatedResp; 
		return nominatedTask ;

	}

	//##############################################################################################################################################################	
	//													Reporting
	//##############################################################################################################################################################	
	// End  Filed triage
	public void Reporting_plane1(ACLPerformative xx ) //report
	{	

		//			if (  Mybody.assignedIncident.Communication_Structure_uesd== Inter_Communication_Structure.Struc1_SilverCommandersInteraction  )
		//			{
		//				//send message to AIC		
		//				Mybody.CurrentMessage = new  ACL_Message( xx ,Mybody ,Mybody.assignedIncident.FRSCcommander1,null, Mybody.CurrentTick ,Mybody.assignedIncident.Inter_Communication_Tool_used ) ; 
		//				Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);	
		//			}		
		//			else if (  Mybody.assignedIncident.Communication_Structure_uesd== Inter_Communication_Structure.Struc2_BronzeCommandersInteraction  )
		//			{			
		Mybody.CurrentMessage = new  ACL_Message( xx ,Mybody ,Mybody.assignedIncident.AICcommander,null, Mybody.CurrentTick ,Mybody.assignedIncident.ComMechanism_level_StoB,1,TypeMesg.Inernal) ; 
		Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);	

	}

	// not used
	public void Reporting_plane_TriageReport(ACLPerformative xx ) //report
	{	
		ArrayList<Casualty> casualtiesneedtoHospital_List =new ArrayList<Casualty>();
		casualtiesneedtoHospital_List.clear();

		for (Task_ambulance T :Operational_Plan ) 
			if (T.TaskStatus == GeneralTaskStatus.Done && T.TaskType == Ambulance_TaskType.FieldTriage&& T.SendTriageReporttoAIC==false  &&    T.TargetCasualty.Triage_tage !=5  ) 
			{
				casualtiesneedtoHospital_List.add(T.TargetCasualty);
				T.SendTriageReporttoAIC=true;
			}

		//System.out.println(Mybody.Id + "Send triage report " +casualtiesneedtoHospital_List.size() );
		Mybody.CurrentMessage = new  ACL_Message( xx ,Mybody ,Mybody.assignedIncident.AICcommander,casualtiesneedtoHospital_List, Mybody.CurrentTick , Mybody.assignedIncident.ComMechanism_level_StoB,casualtiesneedtoHospital_List.size(),TypeMesg.Inernal) ;
		Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
	}

	//***************************************************************************************************
	//Execution_plane  - commanding	
	public void Command_paramedic(Task_ambulance _Task  ) {

		Command CMD1 =new Command();

		if ( _Task.AssignedAmbulance  ==null )
			CMD1.AmbulanceCommand(_Task.TaskID ,_Task.TaskType ,_Task.TargetCasualty );
		else
			CMD1.AmbulanceCommand(_Task.TaskID ,_Task.TaskType ,_Task.TargetCasualty,_Task.AssignedAmbulance );

		// send message with command
		Mybody.CurrentMessage = new  ACL_Message( ACLPerformative.Command , Mybody, _Task.AssignedResponder ,CMD1, Mybody.CurrentTick,Mybody.assignedIncident.ComMechanism_level_BtoRes ,1 ,TypeMesg.Inernal ) ;
		Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
	} 

	//***************************************************************************************************
	//Broadcast
	public void BoradcastAction( ACLPerformative xx ,List<Responder_Ambulance>  _Responders  )
	{		
		Mybody.CurrentMessage  = new  ACL_Message( xx , Mybody, _Responders ,null, Mybody.CurrentTick ,CommunicationMechanism.FF_BoradCast,1 ,TypeMesg.Inernal) ;
		Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
		// break; //one by one 

		//System.out.println(Mybody.Id + "xxxxxxxxxxxxxxxxxxxx " +_Responders .size() );
	}

	//##############################################################################################################################################################	
	//													Interpreted Triggers 
	//##############################################################################################################################################################
	//1 Triage 
	public boolean ThereisWaitingCasualty(Ambulance_TaskType  Tasktypeofinterst)
	{	
		boolean  Result=false;

		for (Task_ambulance T : Operational_Plan) 
			if (T.TaskStatus == GeneralTaskStatus.Waiting && T.TaskType== Tasktypeofinterst  ) 
			{
				Result=true;
				//System.out.println(Mybody.Id + " ++++++++++++++++++++++ "  +T.TaskType + "   " + T.TargetCasualty.ID + "   " + T.AssignedResponder );
				break;
			}

		return Result;
	}

	//----------------------------------------------------------------------------------------------------
	public boolean There_is_free_respondr()
	{
		boolean result=false;
		for (Responder_Ambulance  Res:this.UnoccupiedResponders )   //|| Res.Action ==RespondersActions.Noaction
		{				
			if (    Res.OnhandCasualty==null && (  Res.Action ==RespondersActions.SearchCasualty  || Res.Action ==RespondersActions.Noaction   ) )
				result=true;		
		}			
		return result ;
	}

	//----------------------------------------------------------------------------------------------------
	//4
	int Total_Pre=0;
	public boolean IsAllTaskclosed(Ambulance_TaskType  Tasktypeofinterst)
	{	
		boolean  Result=true;
		Total_Pre=0;
		if (Mybody.assignedIncident.TaskApproach_IN ==TaskAllocationApproach.CentralizedDirectSupervision    )
			if ( Operational_Plan.size() ==0)
				Result=false;

		for (Task_ambulance Task : Operational_Plan) 
			if (Task.TaskStatus != GeneralTaskStatus.Done && Task.TaskType== Tasktypeofinterst   ) 
			{
				Result=false; Total_Pre++  ;
				//break;
			}

		return Result;
	}

	//----------------------------------------------------------------------------------------------------
	//5
	public int Isthere_TriageReport() //report
	{	
		int result=0;

		for (Task_ambulance T :Operational_Plan ) 
			if (T.TaskStatus == GeneralTaskStatus.Done && T.TaskType == Ambulance_TaskType.FieldTriage && T.SendTriageReporttoAIC==false  &&    T.TargetCasualty.Triage_tage!=5 ) 
			{
				result++;
			}
		return result;
	}

	//##############################################################################################################################################################
	public void TaskApproach_way(  )
	{
		if (Mybody.assignedIncident.TaskApproach_IN ==TaskAllocationApproach.CentralizedDirectSupervision  && Mybody.AssignedSector.installed && SafetyBriefandSectorEstablished>=1 )  
		{

			Mybody.RandomllySearachCasualty= true;
			Mybody.Action=RespondersActions.SearchCasualty;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.TaskPlanning ;  //Planformulation;
		}
		else
		{
			Mybody.Action=RespondersActions.Noaction;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.WaitingDuring;			
		}
	}

	//______________________________________________________________________________________________________________________________________________________________
	//##############################################################################################################################################################
	//                                                        Behavior
	//##############################################################################################################################################################
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	// Sector commander - General 
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	public void SCBehavior()	
	{		
		//*************************************************************************
		// 1- initial response
		if( Mybody.CommTrigger==RespondersTriggers.AssigendRolebyAIC) 				
		{
			Mybody.CurrentAssignedActivity=Ambulance_TaskType.GoTolocation;	
		}
		// 2- Triage
		else if (Mybody.CurrentAssignedActivity==Ambulance_TaskType.GoTolocation &&  Mybody.InterpretedTrigger== RespondersTriggers.FirstTimeonLocation	) 
		{
			Mybody.CurrentAssignedActivity=Ambulance_TaskType.CoordinateCasualtyTriage;
		}
		// 3- GoBackToControlArea
		else if (Mybody.InterpretedTrigger== RespondersTriggers.DoneActivity) 
		{
			Mybody.CurrentAssignedMajorActivity_amb=null;
			Mybody.CurrentAssignedActivity=Ambulance_TaskType.None;	

			Mybody.InterpretedTrigger= RespondersTriggers.DoneActivity ;			
			Mybody.Role=Ambulance_ResponderRole.None;
			//System.out.println( Mybody.Id  +" done scene  " );
		}
		// 4-Ending
		else if (Mybody.CurrentAssignedActivity==Ambulance_TaskType.None && Mybody.CommTrigger==RespondersTriggers.ENDER )
		{
			Mybody.CurrentAssignedActivity=  Ambulance_TaskType.CoordinateEndResponse ;
		}
		else if ( Mybody.InterpretedTrigger==RespondersTriggers.ENDER   )   
		{
			Mybody.Role=Ambulance_ResponderRole.None;
		}

		//*************************************************************************
		switch(Mybody.CurrentAssignedActivity) {
		case GoTolocation:
			SCCommanderBehavior_GoTosectorScene()	;	
			break;
		case CoordinateCasualtyTriage :
			SCCommanderBehavior_CoordinateCasualtyTriage();
			break;		
		case CoordinateEndResponse :
			CommanderBehavior_EndER() ;
			break;
		case None:
			;
			break;}


		//System.out.println(Mybody.Id+"    Action:" + Mybody.Action + " com:" + Mybody.CommTrigger + "   Nt:" + Mybody.InterpretedTrigger+ " en:" + Mybody.SensingeEnvironmentTrigger + " end:" + Mybody.EndActionTrigger +  "Acknowledged: "+ Mybody.Acknowledged+"sending: "+  Mybody.Sending  + "CurrentMessage: ");

	}// end ParamedicBehavior

	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	// SC - Go to location   both( Decentralized -Centralized )
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^	
	private void SCCommanderBehavior_GoTosectorScene()	
	{
		// ++++++ 1- +++++++
		if( Mybody.CommTrigger==RespondersTriggers.AssigendRolebyAIC )
		{	
			Mybody.Assign_DestinationCordon(Mybody.AssignedSector.centerofsectorPoint);	

			Mybody.Action=RespondersActions.GoToSectorScene;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Movementonincidentsite; 	
			Mybody.CommTrigger=null;

		}
		else if (Mybody.Action==RespondersActions.GoToSectorScene&& Mybody.SensingeEnvironmentTrigger==null)
		{			
			if ( Mybody.AssignedSector.AmIinSector(Mybody)) // && Mybody.assignedIncident.TaskApproach_IN==TaskAllocationApproach.CentralizedDirectSupervision
				Mybody.SensingeEnvironmentTrigger= RespondersTriggers.ArrivedTargetObject;

			Mybody.Walk();
		}	
		// ++++++ 2- +++++++
		else if (Mybody.Action==RespondersActions.GoToSectorScene&& Mybody.SensingeEnvironmentTrigger== RespondersTriggers.ArrivedTargetObject ) //Trigger
		{ 					
			//send message to AIC				
			Mybody.CurrentMessage  = new  ACL_Message( ACLPerformative.InformlocationArrival , Mybody, Mybody.assignedIncident.AICcommander ,null ,  Mybody.CurrentTick ,Mybody.assignedIncident.ComMechanism_level_StoB,1 ,TypeMesg.Inernal ) ;
			Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);

			Time_last_updated=Mybody.CurrentTick;

			Mybody.Action=RespondersActions.NotifyArrival;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Comunication;  
			Mybody.SensingeEnvironmentTrigger=null;			

		}	
		// ++++++ 3- +++++++
		else if (Mybody.Action==RespondersActions.NotifyArrival && Mybody. Acknowledged)
		{
			Mybody.InterpretedTrigger= RespondersTriggers.FirstTimeonLocation;
			Mybody.Acknowledged=false;Mybody.Sending=false; 	
			System.out.println("  "+"Sector: "+Mybody.Id + " Arrived  my sector" );
		}

	}		

	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	// SC -  CoordinateCasualtyTriage ( Decentralized -Centralized  )
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	private void  SCCommanderBehavior_CoordinateCasualtyTriage()
	{		
		// ++++++ Move +++++++	
		if ( ! ThereisWaitingCasualty(Ambulance_TaskType.FieldTriage) && ! ThereisWaitingCasualty(Ambulance_TaskType.Pre_RescueTreatment)  && Mybody.Action==RespondersActions.SearchCasualty && Mybody.RandomllySearachCasualty==true && Mybody.SensingeEnvironmentTrigger ==null   &&  Mybody.CommTrigger ==null && Mybody.InterpretedTrigger==null && Mybody.EndActionTrigger==null && Mybody.EndofCurrentAction == 0 && Mybody.Sending==false  )
		{
			Mybody.Walk();	
			//System.out.println("  "+"Sector: "+Mybody.Id + "  walk " + this.MyResponders.size());
		}	
		if ( Mybody.Action==RespondersActions.Noaction  &&  Mybody.AssignedSector.installed && SafetyBriefandSectorEstablished>=1 && FirstTime 	) 			 	
		{
			FirstTime=false ;	
			TaskApproach_way(  );
			System.out.println("  "+"Sector: "+Mybody.Id + " star serach " ); //neeeeeed broadcast if its Desntralizedc
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


				Mybody.Action=RespondersActions.FormulatePlan;
				Mybody.BehaviourType1=RespondersBehaviourTypes1.Planformulation ;  
				Mybody.EndofCurrentAction=  InputFile.FormulatePlan_duration  ; 
				Mybody.InterpretedTrigger=null; 
		}

		// ++++++ 2- +++++++
		else if ( Mybody.Action==RespondersActions.FormulatePlan  && Mybody.EndActionTrigger== RespondersTriggers.EndingAction 	) 			 	
		{
			TaskApproach_way(  );
			Mybody.EndActionTrigger=null;
			//System.out.println("  "+"Sector: "+Mybody.Id + " SC Implmenting_plan" );
		}			
		//-------------------------------------------C & D------------------------------------------------- (P1)
		//==========================New/Back Responder========================== Both N/A
		// ++++++ 3- +++++++
		else if (( Mybody.Action==RespondersActions.Noaction ||Mybody.Action==RespondersActions.SearchCasualty ) &&  Mybody.CommTrigger == RespondersTriggers.GetNewParamedicarrived  )		  										
		{	 	
			if (Mybody.CommTrigger == RespondersTriggers.GetNewParamedicarrived )
			{UpdatePlan( UpdatePlanType.NewRes, null, null ,NewResponder,null,null) ;NewRespondrsNotinfromedSB.add(NewResponder);}

			Mybody.Action=RespondersActions.GetArrivalNotification ;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Comunication;  
			Mybody.EndofCurrentAction=  InputFile.GetNotification_duration ; 

		}
		// ++++++ 4- +++++++
		else if ( Mybody.Action==RespondersActions.GetArrivalNotification  && Mybody.EndActionTrigger== RespondersTriggers.EndingAction 	) 			 	
		{
			Mybody.CurrentSender.Acknowledg(Mybody);	

			Mybody.Action=RespondersActions.UpdatePlan;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Planformulation  ; 
			Mybody.EndofCurrentAction=  InputFile.UdatePlan_duration  ; 
			Mybody.EndActionTrigger=null; 
			Mybody.CommTrigger=null; 	
		}
		//==========================Result of Triage========================== Both Paper/Electronic
		// ++++++ 5- +++++++
		else if (( Mybody.Action==RespondersActions.Noaction ||Mybody.Action==RespondersActions.SearchCasualty )&& 
				( Mybody.CommTrigger== RespondersTriggers.GetresultofTriage || Mybody.CommTrigger== RespondersTriggers.GetresultofPre_RescueTreatment || 
				( Mybody.CommTrigger== null && Mybody.assignedIncident.NewRecordcasualtyadded_ISSystem(Mybody.Role,Mybody.AssignedSector , 0)>0)	  )   )		  										
		{	 				
			Mybody.Action=RespondersActions.GetResultTriage; 
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Comunication ; 

			if ( Mybody.CommTrigger== RespondersTriggers.GetresultofTriage || Mybody.CommTrigger== RespondersTriggers.GetresultofPre_RescueTreatment )   
				Mybody.EndofCurrentAction= InputFile.GetInfromation_FtoForR_Duration_Data; 
			else 
			{
				Mybody.EndofCurrentAction= InputFile.GetInfromation_Electronic_Duration ;
				Record_CurrentCasualty=Mybody.assignedIncident.GeRecordcasualtyISSystem(Mybody.Role ,Mybody.AssignedSector,0);//FIFO  inside Record.checkedSC=true;
				Mybody.SendingReciving_External= false ; Mybody.SendingReciving_internal= true ;
			}		
		}
		// ++++++ 6- +++++++
		else if ( Mybody.Action==RespondersActions.GetResultTriage  && Mybody.EndActionTrigger== RespondersTriggers.EndingAction 	) 			 	
		{		
			Casualty ca;
			Responder_Ambulance Res=null;
			boolean Pre_treatment=false;
			boolean FieldTriaged=false;
			//============ 1 ============ 
			if ( Mybody.CommTrigger== RespondersTriggers.GetresultofTriage || Mybody.CommTrigger== RespondersTriggers.GetresultofPre_RescueTreatment)
			{
				Mybody.CurrentSender.Acknowledg(Mybody);				

				ca=CurrentCasualtybySender_info.ca ;
				Res=(Responder_Ambulance) Mybody.CurrentSender ;

			}
			else 
			{						
								
				ca=Record_CurrentCasualty.CasualtyinRec;
				Res=Record_CurrentCasualty.AssignedParamdicinRec ;	
				if ( Record_CurrentCasualty.Pre_treatment==true && Record_CurrentCasualty.IssuedByAmbSC==true ) 
					Pre_treatment=true;
				else		
					FieldTriaged=true;

			}
			//============ 2 ============ 
			//UpdatePlan	
			if ( Mybody.assignedIncident.TaskApproach_IN ==TaskAllocationApproach.CentralizedDirectSupervision)
			{
				UpdatePlan( UpdatePlanType.CloseTask, ca , null , null,null,null);
				UpdatePlan( UpdatePlanType.FreeRes, null, null ,Res,null,null) ;
			}
			else 	if ( Mybody.assignedIncident.TaskApproach_IN ==TaskAllocationApproach.Decentralized_Autonomous)
			{
				if ( Mybody.CommTrigger== RespondersTriggers.GetresultofTriage  || FieldTriaged )				
					UpdatePlan( UpdatePlanType.TrackTask, ca , Ambulance_TaskType.FieldTriage  ,Res,null,null);	
				else if (Mybody.CommTrigger== RespondersTriggers.GetresultofPre_RescueTreatment || Pre_treatment)
				{
					UpdatePlan( UpdatePlanType.CloseTask, ca , null , null,null,null);
					UpdatePlan( UpdatePlanType.FreeRes, null, null ,Res,null,null) ;
					System.out.println("  "+ "Sector: "+Mybody.Id +" closed    Pre_treatment for "+ca.ID  +"    "  + Res.Id);

				}

			}

			Mybody.Action=RespondersActions.UpdatePlan;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Planformulation ;  
			Mybody.EndofCurrentAction= InputFile.UdatePlan_duration ; 
			Mybody.EndActionTrigger=null; 
			Mybody.CommTrigger=null;
		}
		//==========================No more casualty========================== Decentralized  N/A 
		// ++++++ 7- +++++++
		else if ( Mybody.Action==RespondersActions.Noaction &&  Mybody.CommTrigger== RespondersTriggers.NoMorecasualty  &&
				(Mybody.assignedIncident.TaskApproach_IN ==TaskAllocationApproach.Decentralized_Autonomous ||Mybody.assignedIncident.TaskApproach_IN ==TaskAllocationApproach.Decentralized_Mutualadjustment) )	  										
		{	 	
			//Update plan
			NoMoreCasualtyCounter++;
			if  (NoMoreCasualtyCounter== MyResponders.size() )
			{  Me_NomoreinInner=true;}  //Mybody.InterpretedTrigger=RespondersTriggers.NoMorecasualty ;
			else if (NoMoreCasualtyCounter > MyResponders.size() )
				System.out.println("  "+ "Sector: "+Mybody.Id +" " + NoMoreCasualtyCounter +"  " +MyResponders.size()+"  " +Me_NomoreinInner +"*************************error***************************" );

			Mybody.Action=RespondersActions.GetNoMorecasualty;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Comunication;  
			Mybody.EndofCurrentAction=  InputFile.GetInfromation_FtoForR_Duration_Notification  ; 
			Mybody.CommTrigger=null; 

			
		}
		// ++++++ 8- +++++++
		else if ( Mybody.Action==RespondersActions.GetNoMorecasualty && Mybody.EndActionTrigger== RespondersTriggers.EndingAction 	) 			 	
		{
			Mybody.CurrentSender.Acknowledg(Mybody);	

			Mybody.Action=RespondersActions.UpdatePlan;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Planformulation ;  
			Mybody.EndofCurrentAction=  InputFile.UdatePlan_duration  ; 
			Mybody.EndActionTrigger=null; 
		}

		//========================== SafetyBriefandSectorEstablished,Trapped , Updates from AIC  ==========================		
		// ++++++ 13- +++++++
		else if (  ( Mybody.Action==RespondersActions.SearchCasualty  || Mybody.Action==RespondersActions.Noaction )  &&  Mybody.CommTrigger== RespondersTriggers.GetSAupdatefromAIC     )											
		{	 							 

			int total=0;
			//Updates about SA
			if ( SafetyBriefandSectorEstablished == 0   )  
			{SafetyBriefandSectorEstablished  =1 ; total++;}	

			if (   End_nomorecinScene==0)  // from FS
			{	End_nomorecinScene=1; total++;}

			if ( CurrentTrapped_ListbySender.size() >0 )
			{
				for( Casualty ca : CurrentTrapped_ListbySender )
				{
					total++;
					UpdatePlan( UpdatePlanType.NewTask, ca , Ambulance_TaskType.Pre_RescueTreatment, null,null,null);

				}   
				CurrentTrapped_ListbySender.clear();							
			}

			System.out.println("  "+ "Sector: "+Mybody.Id + " GetSAupdate  from " + Mybody.CurrentSender.Id +"  "+  total);

			Mybody.Action=RespondersActions.GetSAupdatefromAIC  ;		
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Comunication ; 
			Mybody.EndofCurrentAction= InputFile.GetInfromation_FtoForR_Duration_Data * total ; 	

			Mybody.CommTrigger=null; 
		}
		// ++++++ 14- +++++++
		if (Mybody.Action==RespondersActions.GetSAupdatefromAIC  && Mybody.EndActionTrigger== RespondersTriggers.EndingAction 	  )
		{
			Mybody.CurrentSender.Acknowledg(Mybody);

			Mybody.Action=RespondersActions.UpdatePlan;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Planformulation  ; 
			Mybody.EndofCurrentAction= InputFile.UdatePlan_duration ; 
			Mybody.EndActionTrigger=null; 

			//System.out.println("  "+ "Sector: "+Mybody.Id + " GetSAupdate" );
		}

		//============================================================ ALL
		// ++++++ 9- +++++++
		else if (Mybody.Action==RespondersActions.UpdatePlan && Mybody.EndActionTrigger== RespondersTriggers.EndingAction  )
		{						
			TaskApproach_way(  );
			Mybody.EndActionTrigger=null;
		}

		//-------------------------------------------------------------------------------------------- (P2) 
		// ++++++ 28- +++++++
		else if ( ( Mybody.Action==RespondersActions.Noaction  ||  Mybody.Action==RespondersActions.SearchCasualty ) &&   ( this.SafetyBriefandSectorEstablished ==1  || (this.SafetyBriefandSectorEstablished ==2  && NewRespondrsNotinfromedSB.size()>0)) )  										
		{

			System.out.println("  "+"Sector: "+  Mybody.Id  +"  Boradcastupdate to responders  "   );

			//send message to pramdics						
			if ( this.SafetyBriefandSectorEstablished ==1 ) { BoradcastAction( ACLPerformative.InfromSafetyBriefandSectorEstablished , this.MyResponders ); SafetyBriefandSectorEstablished=2;NewRespondrsNotinfromedSB.clear();}
			else if ( this.SafetyBriefandSectorEstablished ==2 ) { BoradcastAction( ACLPerformative.InfromSafetyBriefandSectorEstablished , NewRespondrsNotinfromedSB ); NewRespondrsNotinfromedSB.clear();}

			Mybody.Action=RespondersActions.BoradcastAICpdate ;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Comunication;  
			Mybody.EndofCurrentAction=   InputFile.Boradcast_duration ;

		}
		// ++++++ 29- +++++++
		else if (Mybody.Action==RespondersActions.BoradcastAICpdate && Mybody.EndActionTrigger== RespondersTriggers.EndingAction  )
		{				
			TaskApproach_way(  );
			Mybody.Sending=false; 
			Mybody.EndActionTrigger=null;
		}


		//-------------------------------------------------------------------------------------------- (P3) Trapped Centralized or Decentralized 
		// ++++++ 15- +++++++
		else if (   ThereisWaitingCasualty (Ambulance_TaskType.Pre_RescueTreatment)  && (
				( (Mybody.Action==RespondersActions.SearchCasualty ||Mybody.Action==RespondersActions.Noaction) && UnoccupiedResponders.size() >0 &&  Mybody.assignedIncident.TaskApproach_IN ==TaskAllocationApproach.CentralizedDirectSupervision)	
				|| (Mybody.Action==RespondersActions.Noaction &&  There_is_free_respondr() &&  this.UnoccupiedResponders.size() >0 && Mybody.assignedIncident.TaskApproach_IN ==TaskAllocationApproach.Decentralized_Autonomous)
				) )				
		{															
			System.out.println("  "+ "Sector: "+ Mybody.Id + " look for Pramdics for Tarraped   Res#: "+  this.UnoccupiedResponders.size() + "  Trapp:" + IsAllTaskclosed(Ambulance_TaskType.Pre_RescueTreatment) +"  "  + this.Total_Pre  );

			if (Mybody.assignedIncident.TaskApproach_IN ==TaskAllocationApproach.CentralizedDirectSupervision )
			{

				AllocatedTask = Allocate_paramedic(Ambulance_TaskType.Pre_RescueTreatment, Allocation_Strategy.FIFO) ;Mybody.AcceptRequest2=1;
				System.out.println("  "+ "Sector: "+ Mybody.Id + " ddddddoooooo   missing code like triage");
			}
			else
			{
				AllocatedTask = Allocate_paramedic(Ambulance_TaskType.Pre_RescueTreatment, Allocation_Strategy.FIFO_Pre) ;Mybody.AcceptRequest2=0;

				if ( AllocatedTask.AssignedResponder !=null   )
				{
					double dis1= BuildStaticFuction.DistanceC(Mybody.geography, Mybody.Return_CurrentLocation(), AllocatedTask.AssignedResponder.Return_CurrentLocation());
					if ( dis1 > InputFile.FacetoFaceLimit )  
					{ 						
						Taskoption=333;
						Mybody.DestinationLocation = AllocatedTask.AssignedResponder.Return_CurrentLocation()	;
						Mybody.Walk();

						System.out.println("  "+ "Sector: "+ Mybody.Id + " I will  walk to  Pramdics for Tarraped " + AllocatedTask.AssignedResponder.Id  +"   " + AllocatedTask.AssignedResponder.Action );

						Mybody.Action=RespondersActions.SearchPramdics ;
						Mybody.BehaviourType1=RespondersBehaviourTypes1.TaskPlanning ;				
					}
					else
					{
						Taskoption=111;									
						Command_paramedic(AllocatedTask);			
						System.out.println("  "+ "Sector: "+ Mybody.Id + " I will  send to  Pramdics for Tarraped " + AllocatedTask.AssignedResponder.Id  +"   " + AllocatedTask.AssignedResponder.Action );

						Mybody.Action=RespondersActions.CommandParamedicTriageorPre_treatment;
						Mybody.BehaviourType1=RespondersBehaviourTypes1.Comunication; //TaskPlanning ;			
						//Mybody.EndActionTrigger=null;	
					}
				}  //no responders
				else if (  AllocatedTask.AssignedResponder ==null)
				{
					System.out.println("  "+ "Sector: "+ Mybody.Id + " No responders there look for Pramdics for Tarraped .....   I have to wailt some time " );
					TaskApproach_way(  );
				}
			}
		}
		// ++++++ 15- +++++++	
		else if (Mybody.Action==RespondersActions.SearchPramdics && Taskoption==333  )
		{

			double dis1= BuildStaticFuction.DistanceC(Mybody.geography, Mybody.Return_CurrentLocation(), AllocatedTask.AssignedResponder.Return_CurrentLocation());
			if ( dis1 > InputFile.FacetoFaceLimit )  // I can add no obstacle
			{ 						
				Taskoption=333;
				Mybody.DestinationLocation = AllocatedTask.AssignedResponder.Return_CurrentLocation()	;
				Mybody.Walk();

				Mybody.Action=RespondersActions.SearchPramdics ;
				Mybody.BehaviourType1=RespondersBehaviourTypes1.TaskPlanning ;				
			}
			else
			{
				TaskApproach_way(  );
				Taskoption=0;
			}
		}
		// ++++++ 14- +++++++
		else if (Mybody.Action==RespondersActions.CommandParamedicTriageorPre_treatment &&  Taskoption==111 && Mybody. Acknowledged )
		{			
			if ( Mybody.AcceptRequest2==1 )
				UpdatePlan( UpdatePlanType.AssignTask, AllocatedTask.TargetCasualty , AllocatedTask.TaskType , null,null,null);	

			System.out.println("  "+ "Sector: "+ Mybody.Id + " Tarraped   Mybody.AcceptRequest2 " +  Mybody.AcceptRequest2 );

			TaskApproach_way(  );
			Mybody.Acknowledged=false;Mybody.Sending=false; 
			Taskoption=0;
		}

		//-------------------------------------------------------------------------------------------- (P4) Centralized N/A
		// ++++++ 12- +++++++
		else if (   ThereisWaitingCasualty(Ambulance_TaskType.FieldTriage) && Mybody.Action==RespondersActions.SearchCasualty   && UnoccupiedResponders.size() >0 &&  Mybody.assignedIncident.TaskApproach_IN ==TaskAllocationApproach.CentralizedDirectSupervision)	
		{			
			//System.out.println("  ur "+UnoccupiedResponders.size() );
			AllocatedTask = Allocate_paramedic(Ambulance_TaskType.FieldTriage, Allocation_Strategy.FIFO) ;	
			Taskoption=222;

			Mybody.Action=RespondersActions.AllocatResponders;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Planformulation ;
			Mybody.EndofCurrentAction=   InputFile.Allocation_duration ;
		}
		// ++++++ 13- +++++++
		else if (Mybody.Action==RespondersActions.AllocatResponders && Taskoption==222 && Mybody.EndActionTrigger== RespondersTriggers.EndingAction  )
		{									
			Command_paramedic(AllocatedTask);
			UpdatePlan( UpdatePlanType.AssignTask, AllocatedTask.TargetCasualty , AllocatedTask.TaskType , null,null,null);		//??????

			//System.out.println("  "+ "Sector: "+ Mybody.Id + " Tasked  " + AllocatedTask.TaskType +"  "+  AllocatedTask.TargetCasualty.ID +"   " + AllocatedTask.AssignedResponder.Id );

			Mybody.Action=RespondersActions.CommandParamedicTriageorPre_treatment;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.TaskPlanning ;			
			Mybody.EndActionTrigger=null;					
		}
		// ++++++ 14- +++++++
		else if (Mybody.Action==RespondersActions.CommandParamedicTriageorPre_treatment  && Taskoption==222 && Mybody. Acknowledged )
		{
			TaskApproach_way(  );
			Mybody.Acknowledged=false;Mybody.Sending=false; 	
			Taskoption=0;
		}


		//-------------------------------------------------------------------------------------------- (P5) Centralized   search
		// ++++++ 15- +++++++
		else if ( ! ThereisWaitingCasualty(Ambulance_TaskType.FieldTriage) && ! ThereisWaitingCasualty(Ambulance_TaskType.Pre_RescueTreatment)  && Mybody.Action==RespondersActions.SearchCasualty &&  Mybody.SensingeEnvironmentTrigger== RespondersTriggers.SensedCasualty && Mybody.assignedIncident.TaskApproach_IN ==TaskAllocationApproach.CentralizedDirectSupervision )
		{									
			UpdatePlan( UpdatePlanType.NewTask, Mybody.TargetCasualty , Ambulance_TaskType.FieldTriage , null,null,null);
			//System.out.println("  "+ "Sector: "+Mybody.Id + " added" + Mybody.TargetCasualty.ID);
			if ( ! Mybody.CasualtySeen_list.contains(Mybody.TargetCasualty)) Mybody.CasualtySeen_list.add(Mybody.TargetCasualty); 

			Mybody.Action=RespondersActions.UpdatePlan;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Planformulation ; 
			Mybody.EndofCurrentAction=  InputFile.UdatePlan_duration  ; 
			Mybody.SensingeEnvironmentTrigger=null; 
		}
		//-----------------------------------------------End ---------------------------------------------(P5) Both Paper/Electronic  End_nomorecinScene==1 &&
		// ++++++ 16- +++++++	
		else if (    (  Mybody.Action==RespondersActions.SearchCasualty && IsAllTaskclosed(Ambulance_TaskType.FieldTriage) && IsAllTaskclosed(Ambulance_TaskType.Pre_RescueTreatment) &&   Mybody.assignedIncident.TaskApproach_IN ==TaskAllocationApproach.CentralizedDirectSupervision  &&  ( Mybody.SensingeEnvironmentTrigger== RespondersTriggers.Arrived_WalkedinAllScene || Mybody.SensingeEnvironmentTrigger == RespondersTriggers.Arrived_WalkedalongOneDirection))
				|| (  Mybody.Action==RespondersActions.Noaction && Me_NomoreinInner==true && IsAllTaskclosed(Ambulance_TaskType.Pre_RescueTreatment)   &&   ( Mybody.assignedIncident.TaskApproach_IN ==TaskAllocationApproach.Decentralized_Autonomous ||Mybody.assignedIncident.TaskApproach_IN ==TaskAllocationApproach.Decentralized_Mutualadjustment) ))
		{	
			Mybody.assignedIncident.AmSCcommander1_ENDSR_INNER=true ; //temp solution
			Me_NomoreinInner=false;
			//if(Mybody.InterpretedTrigger==RespondersTriggers.NoMorecasualty ) Mybody.InterpretedTrigger=null;
			if (  Mybody.SensingeEnvironmentTrigger!=null ) Mybody.SensingeEnvironmentTrigger= null;

			//send message to AIC
			Reporting_plane1(ACLPerformative.InformEndFieldTriage)  ; //Triage 

			Mybody.Action=RespondersActions.InformEndTriageReport ;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Comunication ; 

			System.out.println("  "+ "Sector: "+Mybody.Id  +" done triage****************1******************" + " Trapp: " + IsAllTaskclosed(Ambulance_TaskType.Pre_RescueTreatment) +"  "  + this.Total_Pre  );
		}
		// ++++++ 17- +++++++
		else if (Mybody.Action==RespondersActions.InformEndTriageReport && Mybody. Acknowledged )
		{

			BoradcastAction(ACLPerformative.InformSceneOprationsEND, this.MyResponders);

			Mybody.Action=RespondersActions.BoradcastEndScene ;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Comunication;  
			Mybody.EndofCurrentAction=   InputFile.Boradcast_duration ;
			Mybody.Acknowledged=false;Mybody.Sending=false; 

			//for(ISRecord     Rec: Mybody.assignedIncident.ISSystem) 
			//System.out.println( Rec.CasualtyinRec.ID +" --------   "+ Rec.priority_levelinRec + "  "+ Rec.FieldTriaged + "  "+ Rec.SecondTriaged+ "  "+ Rec.Treated + "  "+ Rec.TransferdV+ "  "+ Rec.TransferdH  +" checkedSC_DoneFieldTriage  " + Rec.checkedSC_DoneFieldTriage );
		}
		// ++++++ 18- +++++++
		else if (Mybody.Action==RespondersActions.BoradcastEndScene && Mybody.EndActionTrigger== RespondersTriggers.EndingAction  )
		{		
			Mybody.InterpretedTrigger= RespondersTriggers.DoneActivity;

			Mybody.Action=RespondersActions.Noaction;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.WaitingEnd;
			Mybody.EndActionTrigger= null;
			Mybody.Sending=false; 
			System.out.println( "  "+"Sector: "+Mybody.Id   +" done triage*****************2*****************" );

			Mybody.assignedIncident.AmSCcommander1 =null ;


//			System.out.println(    "  Action: " + Mybody.Action + " com:" + Mybody.CommTrigger + "   Nt:" + Mybody.InterpretedTrigger+ " SensingeEnvir:" 
//					+Mybody.SensingeEnvironmentTrigger + " end:" + Mybody.EndActionTrigger +"  Acknowledged: " + Mybody.Acknowledged + "   Sending:" + Mybody.Sending +"  "+ Mybody.ActivityAcceptCommunication +"  " );
//			System.out.println( Mybody.Message_inbox.get(Mybody.Message_inbox.size()-1 ).performative );
//			System.out.println( Mybody.Message_inbox.get(Mybody.Message_inbox.size()-2 ).performative );
//			System.out.println( Mybody.Message_inbox.get(Mybody.Message_inbox.size()-3 ).performative );
//			System.out.println( Mybody.Message_inbox.get(Mybody.Message_inbox.size()-4 ).performative );
//			System.out.println( Mybody.Message_inbox.get(Mybody.Message_inbox.size()-5 ).performative );
//			System.out.println( Mybody.Message_inbox.get(Mybody.Message_inbox.size()-6 ).performative );
//			System.out.println( Mybody.Message_inbox.get(Mybody.Message_inbox.size()-7 ).performative );
//			System.out.println( Mybody.Message_inbox.get(Mybody.Message_inbox.size()-8 ).performative );
//			System.out.println( Mybody.Message_inbox.get(Mybody.Message_inbox.size()-9 ).performative );
//			System.out.println( Mybody.Message_inbox.get(Mybody.Message_inbox.size()-10 ).performative );
//			System.out.println( Mybody.Message_inbox.get(Mybody.Message_inbox.size()-11 ).performative );

		}


		//		if ( Mybody.CurrentTick==2300  )
		//		{
		//				System.out.println("  "+ "----------------------------------");							
		//				for (Task_ambulance Task : Operational_Plan) 
		//					System.out.println("  "+ Mybody.Id  +" " + Task.TaskType +"  " +Task.TargetCasualty.ID   +""  + Task.TaskStatus);
		//				System.out.println("  "+ "----------------------------------");
		//				
		//		 for (Casualty  ca : Mybody.CasualtySeen_list  ) 
		//				System.out.println("  "+ Mybody.Id  +" " + ca.ID);
		//				
		//		}



	}

	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	// SC -  CoordinateEndResponse  
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	private void CommanderBehavior_EndER()
	{
		// ++++++ 1- +++++++
		if ( Mybody.CommTrigger==RespondersTriggers.ENDER ) 
		{
			Mybody.Action=RespondersActions.GetNotificationEndER ;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Comunication ; 
			Mybody.EndofCurrentAction= InputFile.GetNotification_duration ;
			Mybody.CommTrigger=null; 

		}
		// ++++++ 2- +++++++
		else if (Mybody.Action==RespondersActions.GetNotificationEndER  && Mybody.EndActionTrigger== RespondersTriggers.EndingAction   )
		{
			// Send message to its pramedics	
			BoradcastAction(ACLPerformative.InformERend, this.MyResponders);

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



////----------------------------------------------------------------------------------------------- (P2) Both Paper only
//		// ++++++ 10- +++++++
//		else  if (( Mybody.Action==RespondersActions.Noaction ||Mybody.Action==RespondersActions.SearchCasualty ) && (Mybody.CurrentTick >= (Time_last_updated +  InputFile.UpdatOPEvery   )) && Mybody.assignedIncident.CasualtyReport_Mechanism_IN == CasualtyReportandTrackingMechanism.Paper) 
//		{			 	
//			boolean Send=false;
//			//updates to silver commander
//			if (  Isthere_TriageReport()  >0 )
//			{ Reporting_plane_TriageReport(ACLPerformative.InformTriageReport)   ; Send=true; }
//
//			if ( Send )
//			{ 
//				Mybody.Action=RespondersActions.InformTriageReport ;  //Mybody.Action=RespondersActions.InfromOPReport;
//				Mybody.BehaviourType=RespondersBehaviourTypes.Sharinginfo;	
//			}
//			else
//			{
//				Mybody.Action=RespondersActions.UpdatePlan;
//				Mybody.BehaviourType=RespondersBehaviourTypes.Planformulation;
//				Mybody.EndofCurrentAction=  InputFile.UdatePlan_duration  ; 
//				Time_last_updated=Mybody.CurrentTick;
//			}
//		}
//		// ++++++ 11- +++++++
//		else if (Mybody.Action==RespondersActions.InformTriageReport  && Mybody. Acknowledged )
//		{
//			TaskApproach_way(  );
//
//			Mybody.Acknowledged=false;Mybody.Sending=false; 
//			Time_last_updated=Mybody.CurrentTick ;
//
//		}
