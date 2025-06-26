package A_Roles_Police;
import java.util.ArrayList;
import java.util.List;
import A_Agents.Casualty;
import A_Agents.Responder;
import A_Agents.Responder_Ambulance;
import A_Agents.Responder_Fire;
import A_Agents.Responder_Police;
import A_Environment.TacticalArea;
import B_Classes.Task_Police;
import B_Classes.Task_ambulance;
import B_Communication.ACL_Message;
import B_Communication.Command;
import B_Communication.Report;
import C_SimulationInput.InputFile;
import D_Ontology.BuildStaticFuction;
import D_Ontology.Ontology;
import D_Ontology.Ontology.ACLPerformative;
import D_Ontology.Ontology.Allocation_Strategy;
import D_Ontology.Ontology.CasualtyinfromationType;
import D_Ontology.Ontology.CommunicationMechanism;
import D_Ontology.Ontology.Communication_Time;
import D_Ontology.Ontology.GeneralTaskStatus;
import D_Ontology.Ontology.Inter_Communication_Structure;
import D_Ontology.Ontology.Police_ResponderRole;
import D_Ontology.Ontology.Police_TaskType;
import D_Ontology.Ontology.RespondersActions;
import D_Ontology.Ontology.RespondersBehaviourTypes3;
import D_Ontology.Ontology.RespondersTriggers;
import D_Ontology.Ontology.TypeMesg;
import D_Ontology.Ontology.UpdatePlanType;

public class Police_Commander_RCO {

	Responder_Police Mybody;

	Responder_Police NewResponder=null,FinshedResponder=null, AllocatedRespondertoTask=null;	
	Task_Police  AllocatedTask;

	Report CurrentReport=null;
	Casualty  CurrentCasualtybySender=null;
	ArrayList<Casualty> CurrentCasualtyListbySender=new ArrayList<Casualty>();

	//ArrayList<Casualty> Dead_List_innerxx =new ArrayList<Casualty>();
	ArrayList<Casualty> liaisonofficer_CaList=new ArrayList<Casualty>();
	ArrayList<Casualty> evaccasualty_list=new ArrayList<Casualty>();
	
	//----------------------------------------------------	
	int  TaskOtion=0;
	boolean InInnerorCCS;    /// inner: true , CCS:false 
	int counter=0;
	boolean StartSetup=false;

	boolean NoMoreCasualtyinCordon1=false; //used when we allow them to serach in safe location
	int liaisonofficerCounter=0, liaisonofficerMax=0;
	boolean liaisonofficerNeed=false;

	//----------------------------------------------------
	//  The meaning of attribute content....  -1:No update   , 0: Get  , 1: realize or done  2: send to workers or PIC or other
	int  EndRCEstablished = -1  , End_nomorec_impact=-1 , End_nomorec_CCS =-1 ,End_nomorec=-1 , canEnterScene=-1 ; // in scene
	int End_RC=-1;
	//----------------------------------------------------
	int AllocatedResponderfroSetup=0 ;

	List<Task_Police> Operational_Plan = new ArrayList<Task_Police>(); //for each casualty 
	List<Responder_Police> MyResponders = new ArrayList<Responder_Police>(); //its worker  
	List<Responder_Police> UnoccupiedResponders = new ArrayList<Responder_Police>(); //free worker

	//----------------------------------------------------
	double Time_last_updated=0;	

	//##############################################################################################################################################################	
	public Police_Commander_RCO ( Responder_Police _Mybody  , TacticalArea  _TargetTA ) 
	{
		Mybody=_Mybody;
		Mybody.EndofCurrentAction=0;	
		Mybody.ColorCode= 5;
		Mybody.AssignedTA=_TargetTA ;

		Mybody.assignedIncident.RCOcommander=Mybody;
		Mybody.AssignedTA.Bronzecommander=Mybody;

		Mybody.PrvRoleinprint3 =Mybody.Role;
	}

	//##############################################################################################################################################################
	public void  Commander_RCO_InterpretationMessage()
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
			if ( Mybody.CurrentSender instanceof Responder_Police &&  Mybody.CurrentCommandRequest.commandType3 ==Police_TaskType.CollectDeceasedCasualty  )  // from PIC  
			{				
				Mybody.CommTrigger= RespondersTriggers.GetRequstecollectDeceasedcasualty ;
				CurrentCasualtyListbySender =Mybody.CurrentCommandRequest.TargetCasualty_List ;
				InInnerorCCS= Mybody.CurrentCommandRequest.ISDeadinInnerorCCS ; //InInner= true or CCS=0;

			}
			//++++++++++++++++++++++++++++++++++++++
			if ( Mybody.CurrentSender instanceof Responder_Police &&  Mybody.CurrentCommandRequest.commandType3 ==Police_TaskType.collectInjuriedEvacuetedCasualty  )  // from PIC   or   CCO_Amb 
			{				
				Mybody.CommTrigger= RespondersTriggers.GetRequstecollectInjuredcasualty ;
				CurrentCasualtyListbySender =Mybody.CurrentCommandRequest.TargetCasualty_List ;
			}
			//++++++++++++++++++++++++++++++++++++++
			break;

		case InformSearchandRescueCasualtyReport :  // from SC_Fire (inner)
			Mybody.CommTrigger= RespondersTriggers.GetRequstecollectDeceasedcasualty ;
			CurrentReport=((Report) currentmsg.content);
			CurrentCasualtyListbySender=CurrentReport.Dead_List_inner ;	
			InInnerorCCS= true;
			break;		

			//------------------------------- 	
		case 	InformCCSReport :  // from CCO (CCS)
			Mybody.CommTrigger= RespondersTriggers.GetRequstecollectDeceasedcasualty ;

			CurrentReport=((Report) currentmsg.content);						
			CurrentCasualtyListbySender =CurrentReport.Dead_List_CCS;				
			InInnerorCCS= false;
			break;	
			
		case 	InformCCSReport_casTolog :  // from CCO (CCS)
			Mybody.CommTrigger= RespondersTriggers.GetRequstecollectInjuredcasualty ;

			CurrentReport=((Report) currentmsg.content);							
			CurrentCasualtyListbySender =CurrentReport.Cas_evacuated_List ;
			
			break;	 
			
			//------------------------------- 		
		case InformNewResponderArrival:	
			Mybody.CommTrigger= RespondersTriggers.GetNewPolicemanarrived ;
			NewResponder= (Responder_Police) Mybody.CurrentSender;
			break;
		case InformlocationArrival:
			Mybody.CommTrigger= RespondersTriggers.ComeBack;  //decased + guid
			FinshedResponder= (Responder_Police) Mybody.CurrentSender;
			break;			
		case InfromResultSetupRC:
			Mybody.CommTrigger= RespondersTriggers.GetresultofSetup;
			FinshedResponder= (Responder_Police) Mybody.CurrentSender;
			break;
			//----------------------------------
		case InformResultcasultyinfromation_Decased:
			Mybody.CommTrigger= RespondersTriggers.Getresultcasultyinfromation_Decased;
			CurrentCasualtybySender = ((Casualty)currentmsg.content);
			FinshedResponder= null; //not used
			break;
		case InformResultcasultyinfromation_uninjured:
			Mybody.CommTrigger= RespondersTriggers.Getresultcasultyinfromation_uninjured;
			CurrentCasualtybySender = ((Casualty)currentmsg.content);
			FinshedResponder= (Responder_Police) Mybody.CurrentSender; 
			break;
		case InformResultcasultyinfromation_injured:
			Mybody.CommTrigger= RespondersTriggers.Getresultcasultyinfromation_injured ;
			CurrentReport= ((Report)currentmsg.content);
			CurrentCasualtyListbySender=CurrentReport.Casualties_Injured; //or evacuated
			FinshedResponder= (Responder_Police) Mybody.CurrentSender; 
			break;
			//----------------------------------
					
		case InfromSafetyBriefandSectorEstablished: // from PIC  or SC_Fire
			Mybody.CommTrigger= RespondersTriggers.GetSAupdatefromPIC ;
			canEnterScene= 0 ; 
			//System.out.println(Mybody.Id + "..................................................................." + Mybody.CommTrigger + "  from  "+ Mybody.CurrentSender.Id  + "    Action:" + Mybody.Action);
			break;
		case InformNomorecasualty: // from PIC  both  imapct and CCS
			Mybody.CommTrigger= RespondersTriggers.GetSAupdatefromPIC ;
			End_nomorec= 0 ; 
			break;	
		case InformNomorecasualty_impact: // from  SC_Fire   
			Mybody.CommTrigger= RespondersTriggers.GetSAupdatefromPIC ;
			End_nomorec_impact=0 ;
			break;	
		case InformNomorecasualty_CCS: // from CCO
			Mybody.CommTrigger= RespondersTriggers.GetSAupdatefromPIC ;
			End_nomorec_CCS =0; 
			break;
		case InformERend :  // from PIC
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

	}
	//***************************************************************************************************
	// Update Plan - add Casualty/Task
	public void UpdatePlan( UpdatePlanType UpdateType, Casualty _Casualty, Police_TaskType _TaskType ,Responder_Police _Responder ,ArrayList<Casualty> _liaisonofficer_CaList ,CasualtyinfromationType xx ) 
	{
		switch( UpdateType) {

		case NewRes :
			MyResponders.add(_Responder);
			UnoccupiedResponders.add(_Responder);	
			_Responder.ColorCode=7;
			break;
		case FreeRes:
			UnoccupiedResponders.add(_Responder);
			break;
		case RemoveRes:
			MyResponders.remove(_Responder);
			break;
		case NewTask:
			Task_Police Newtask = new Task_Police( _Casualty, _TaskType  ); 
			Operational_Plan.add(Newtask);
			break;
		case AssignTask:
			for (Task_Police Task : Operational_Plan) 
				if (Task.TargetCasualty == _Casualty  && Task.TaskStatus==GeneralTaskStatus.Waiting ) 
				{ 
					Task.TaskStatus=GeneralTaskStatus.Inprogress ;
					Task.TaskStartTime = Mybody.CurrentTick ;
					UnoccupiedResponders.remove(Task.AssignedResponder); // asign done in allocation
				}
			break;
		case AssignTask_list:
			for ( Casualty ca: _liaisonofficer_CaList)
			{
				for (Task_Police Task : Operational_Plan) 
					if (Task.TargetCasualty == ca ) 
					{ 
						Task.TaskStatus=GeneralTaskStatus.Inprogress ;
						Task.TaskStartTime = Mybody.CurrentTick ;
						Task.AssignedResponder=_Responder ;	//liaisonofficer		
					}	
			}
			break;
		case  CloseTask:
			for (Task_Police Task : Operational_Plan) 
				if (Task.TargetCasualty == _Casualty    ) 
				{
					Task.TaskStatus=GeneralTaskStatus.Done;	
					Task.TaskEndTime =Mybody.CurrentTick ;
					Task.infromationType= xx ;
				}
			break;
			//=====================================
		case  TrackTask:	 //  in CCS

			Task_Police Newtask1 = new Task_Police( _Casualty, _TaskType  );  //done
			Newtask1.TaskStatus=GeneralTaskStatus.Done;	
			Newtask1.TaskEndTime =Mybody.CurrentTick ;

			Newtask1.infromationType= xx ;
			Operational_Plan.add(Newtask1);
			break;}

	}

	//***************************************************************************************************	
	//Execution_plane - Task Allocation		
	public Task_Police Allocate_Policeman(  Allocation_Strategy Strategy ,Police_TaskType inters) 
	{

		Task_Police nominatedTask=null;
		Responder nominatedResp=null ;

		switch(Strategy) {
		case FIFO:			
			//FIFO seen casualty
			for (Task_Police Task : Operational_Plan) 
				if (Task.TaskStatus == Ontology.GeneralTaskStatus.Waiting && Task.TaskType  ==inters )
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

			for (Task_Police Task : Operational_Plan)
				if (Task.TaskStatus == Ontology.GeneralTaskStatus.Waiting  ) //&& Task.TaskType== Tasktypeofinterst
				{
					Responder Resp=Task.ckeck_Distance_befor_TaskAssignment(UnoccupiedResponders);
					double dis = BuildStaticFuction.DistanceC(Task.TargetCasualty.geography, Resp.Return_CurrentLocation(),Task.TargetCasualty.getCurrentLocation());

					if (dis<smallest_dis)
					{ smallest_dis= dis; nominatedResp=Resp ;  nominatedTask=Task;}
				}

			break;}

		nominatedTask.AssignedResponder= (Responder_Police) nominatedResp; 
		return nominatedTask ;
	}

	//##############################################################################################################################################################	
	//													Reporting
	//##############################################################################################################################################################	
	//  RC established 	
	public void Reporting_plane1(ACLPerformative xx  ) {


		//send message to PIC
		if (xx==ACLPerformative.InfromRCEstablished  &&  Mybody.assignedIncident.Communication_Structure_uesd== Inter_Communication_Structure.Struc1_SilverCommandersInteraction  )
		{
			Mybody.CurrentMessage  = new  ACL_Message( xx , Mybody , Mybody.assignedIncident.PICcommander ,null ,  Mybody.CurrentTick,Mybody.assignedIncident.ComMechanism_level_StoB,1,TypeMesg.External ) ;	
			Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
		}
		//send message to FR SC
		else if (xx==ACLPerformative.InfromRCEstablished  &&  Mybody.assignedIncident.Communication_Structure_uesd== Inter_Communication_Structure.Struc2_BronzeCommandersInteraction  )
		{
			Mybody.CurrentMessage  = new  ACL_Message( xx , Mybody , Mybody.assignedIncident.FRSCcommander1 ,null ,  Mybody.CurrentTick,Mybody.assignedIncident.Inter_Communication_Tool_used ,1,TypeMesg.External) ;
			Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
		}


		if (xx==ACLPerformative.InformEndcasultyinfromation )
		{
			Mybody.CurrentMessage  = new  ACL_Message( xx , Mybody , Mybody.assignedIncident.PICcommander ,null ,  Mybody.CurrentTick,Mybody.assignedIncident.ComMechanism_level_StoB,1,TypeMesg.Inernal ) ;	
			Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
		}


	};

	//***************************************************************************************************
	public void  Reporting_plane_casultiesInfromationxxxxxx(ACLPerformative xx) {

//		boolean send=false;
//
//		ArrayList<Casualty> Casualties_Uninjured=new ArrayList<Casualty>();
//		ArrayList<Casualty> Casualties_Injured=new ArrayList<Casualty>();
//		ArrayList<Casualty> Casualties_Evacuated=new ArrayList<Casualty>();
//		ArrayList<Casualty> Casualties_Deceased=new ArrayList<Casualty>();
//
//		for (Task_Police Task : Operational_Plan) 
//			if ( Task.TaskStatus == GeneralTaskStatus.Done && Task.SendReporttoPIC != true  )  
//			{
//				Task.SendReporttoPIC= true ;
//				switch(Task.infromationType) {
//				case Uninjured:
//					Casualties_Uninjured.add(Task .TargetCasualty);
//					break;
//				case Injured:
//					Casualties_Injured.add(Task .TargetCasualty);  //???
//					break;
//				case Evacuated:
//					Casualties_Evacuated.add(Task .TargetCasualty); //???
//					break;
//				case Deceased:
//					Casualties_Deceased.add(Task .TargetCasualty);
//					break;}	
//
//			}
//
//		Report Report1 =new Report();
//		Report1.Police_ReportCasualties_informatiom( Casualties_Uninjured , Casualties_Injured  , Casualties_Evacuated , Casualties_Deceased );
//
//		// send message
//		Mybody.CurrentMessage  = new  ACL_Message( xx ,Mybody ,Mybody.assignedIncident.PICcommander ,Report1, Mybody.CurrentTick ,Mybody.assignedIncident.ComMechanism_level_StoB) ;
//		Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
	};	

	//***************************************************************************************************
	// Commanding	
	public void Command_Policeman(Task_Police _Task ,   Police_TaskType _Police_TaskType ,  Responder_Police _Responder    ) {

		Command CMD1 =new Command();

		if (_Police_TaskType== Police_TaskType.SetupTacticalAreas )	//setup
		{
			CMD1.PoliceCommand("0" , _Police_TaskType );
			Mybody.CurrentMessage = new  ACL_Message( ACLPerformative.Command , Mybody,  _Responder ,CMD1, Mybody.CurrentTick,Mybody.assignedIncident.ComMechanism_level_BtoRes,1 ,TypeMesg.Inernal ) ;
			Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
		}			
		else if (   _Police_TaskType==  Police_TaskType.collectInjuriedEvacuetedCasualty )
		{
			CMD1.PoliceCommand2("0" ,_Police_TaskType ,evaccasualty_list);  // lission 
			Mybody.CurrentMessage = new  ACL_Message( ACLPerformative.Command , Mybody,   _Responder,CMD1, Mybody.CurrentTick,Mybody.assignedIncident.ComMechanism_level_BtoRes,1  ,TypeMesg.Inernal) ;
			Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
		}
		else if ( _Police_TaskType== Police_TaskType.CollectUninjuriedCasualty || _Police_TaskType== Police_TaskType.CollectDeceasedCasualty  ) 
		{
			CMD1.PoliceCommand(_Task.TaskID ,_Task.TaskType ,_Task.TargetCasualty , false);  // right now I will not used boolean need to think
			Mybody.CurrentMessage = new  ACL_Message( ACLPerformative.Command , Mybody, _Task.AssignedResponder ,CMD1, Mybody.CurrentTick,Mybody.assignedIncident.ComMechanism_level_BtoRes ,1 ,TypeMesg.Inernal) ;
			Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
			//System.out.println("                                                                                    "+"RC: "+  Mybody.Id  +" xxxxxxxxxxxxxxxxxx "   );
		}

	} 

	//***************************************************************************************************
	//Broadcast
	public void BoradcastAction(ACLPerformative xx)
	{			
		Mybody.CurrentMessage  = new  ACL_Message(xx , Mybody, MyResponders,null, Mybody.CurrentTick,CommunicationMechanism.FF_BoradCast,1 ,TypeMesg.Inernal ) ;
		Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
		// break; //one by one 	
	}

	//##############################################################################################################################################################	
	//													Interpreted Triggers 
	//##############################################################################################################################################################	
	public boolean ThereisWaitingCasualty(Police_TaskType Taskofinterset)
	{	
		boolean  Result=false;

		for (Task_Police Task : Operational_Plan) 
			if (Task.TaskStatus == D_Ontology.Ontology.GeneralTaskStatus.Waiting  &&  Task.TaskType== Taskofinterset) 
			{
				Result=true;
				break;
			}

		return Result;
	}

	//----------------------------------------------------------------------------------------------------
	public boolean  xxIsThereReporting_casultiesInfromation() {

		boolean send=false;

		for (Task_Police Task : Operational_Plan) 
			if ( Task.TaskStatus == GeneralTaskStatus.Done   && ! Task.SendReporttoPIC )  				
			{	send=true ; break; }

		return send ;
	}

	//----------------------------------------------------------------------------------------------------
	public boolean IsAllTaskclosed()
	{	
		boolean  Result=true;

		if (Operational_Plan.size() ==0 )
			Result=false;
		//or	
		for (Task_Police Task : Operational_Plan) 
			if (Task.TaskStatus != D_Ontology.Ontology.GeneralTaskStatus.Done    ) 
			{
				Result=false;
				break;
			}

		return Result;
	}

	//----------------------------------------------------------------------------------------------------
	public boolean ThereisNewCasualtyinRC()
	{	
		boolean  Result=false;
		for (Casualty ca: Mybody.AssignedTA.casualtiesinTA ) 
			if (ca.newaddetoRC  ) 
			{
				Result=true;				
				break;
			}
		return Result;
	}

	//----------------------------------------------------------------------------------------------------
	public Casualty NearstNewCasualtyinRC()
	{	
		//Smallest distance
		Casualty nominatedCasualty=null;
		double smallest_dis=999999 ; ; 

		for (Casualty ca: Mybody.AssignedTA.casualtiesinTA ) 
			if ( ca.newaddetoRC )
			{
				double dis = BuildStaticFuction.DistanceC(Mybody.geography, Mybody.Return_CurrentLocation(),ca.getCurrentLocation());

				if (dis<=smallest_dis)
				{ smallest_dis= dis; nominatedCasualty= ca ; }
			}

		if (nominatedCasualty !=null)  nominatedCasualty.newaddetoRC=false;
		return  nominatedCasualty ;

	}
	//----------------------------------------------------------------------------------------------------		
	public boolean ISthereupdateTosend( )
	{	
		boolean  Result= false;

		//Is there updates 
		if ( EndRCEstablished ==1  )
		{  Result=true; }
		
		//if ( IsThereReporting_casultiesInfromation() )
		//{  Result=true; }

		if ( End_RC!= 2  & ( UnoccupiedResponders.size()== this.MyResponders.size())  &&   End_nomorec==1 ) // No More casualty in both CCS and inner this.MyResponders.size()-liaisonofficerCounter 
		{  Result=true;  End_RC=1 ;}
		return Result;
	}	

	//______________________________________________________________________________________________________________________________________________________________
	//##############################################################################################################################################################
	//                                                        Behavior
	//##############################################################################################################################################################
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	// RCO commander - General 
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	public void RCOBehavior()	
	{		
		//*************************************************************************
		// 1- initial response
		if( Mybody.CommTrigger==RespondersTriggers.AssigendRolebyPIC) 				
		{
			Mybody.CurrentAssignedActivity=Police_TaskType.GoTolocation;	
		}
		// 2- Collect information 
		else if (Mybody.CurrentAssignedActivity==Police_TaskType.GoTolocation &&  Mybody.InterpretedTrigger== RespondersTriggers.FirstTimeonLocation	) 
		{
			Mybody.CurrentAssignedActivity=Police_TaskType.CoordinateInformationCollectionofCasualty ;
		}
		// 3-NoAction
		else if (  Mybody.InterpretedTrigger== RespondersTriggers.DonecollectinformationCasualtyActivity)
		{		
			Mybody.CurrentAssignedActivity=Police_TaskType.None;
			Mybody.InterpretedTrigger=null;
			System.out.println( Mybody.Id  +" done   "  +  Mybody.CommTrigger );
		}
		// 4-Ending
		else if (Mybody.CurrentAssignedActivity==Police_TaskType.None && Mybody.CommTrigger==RespondersTriggers.ENDER )
		{
			Mybody.CurrentAssignedActivity=  Police_TaskType.CoordinateEndResponse ;
		}
		// 5-Ending leave
		else if ( Mybody.InterpretedTrigger==RespondersTriggers.ENDER   )   
		{
			Mybody.PrvRoleinprint3=Mybody.Role ;
			Mybody.Role=Police_ResponderRole.None;
		}

		//*************************************************************************
		switch(Mybody.CurrentAssignedActivity) {
		case GoTolocation:
			RCOCommanderBehavior_GoToRC()	;	
			break;
		case CoordinateInformationCollectionofCasualty :			
			RCOCommanderBehavior_CoordinateCasualtyinfromation();
			break;			
		case CoordinateEndResponse :
			CommanderBehavior_EndER() ;
			break;
		case None:
			;
			break;}

		//System.out.println(Mybody.Id+"    Action:" + Mybody.Action + " com:" + Mybody.CommTrigger + "   Nt:" + Mybody.InterpretedTrigger+ " en:" + Mybody.SensingeEnvironmentTrigger + " end:" + Mybody.EndActionTrigger +  "Acknowledged: "+ Mybody.Acknowledged+"sending: "+  Mybody.Sending  + "CurrentMessage: ");

	}// end Behavior

	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	// RCO - Go to location   
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^	
	private void RCOCommanderBehavior_GoToRC()	
	{
		// ++++++ 1- +++++++
		if( Mybody.CommTrigger==RespondersTriggers.AssigendRolebyPIC)
		{	

			Mybody.Assign_DestinationCordon(Mybody.assignedIncident.RC.Location); 

			Mybody.Action=RespondersActions.GOToRC;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.Movementonincidentsite ; 	
			Mybody.CommTrigger=null;
		}
		// ++++++ 2- +++++++
		else if (Mybody.Action==RespondersActions.GOToRC && Mybody.SensingeEnvironmentTrigger==null)
		{			
			Mybody.Walk();
		}	
		// ++++++ 3- +++++++
		else if (Mybody.Action==RespondersActions.GOToRC && Mybody.SensingeEnvironmentTrigger== RespondersTriggers.ArrivedTargetObject   ) 
		{ 					
			//send message to PIC
			Mybody.CurrentMessage  = new  ACL_Message( ACLPerformative.InformlocationArrival , Mybody , Mybody.assignedIncident.PICcommander ,null ,  Mybody.CurrentTick,Mybody.assignedIncident.ComMechanism_level_StoB ,1 ,TypeMesg.Inernal) ;
			Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
			Time_last_updated=Mybody.CurrentTick;

			Mybody.Action=RespondersActions.NotifyArrival;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.Comunication; 
			Mybody.SensingeEnvironmentTrigger=null;		

		}	
		// ++++++ 4- +++++++
		else if (Mybody.Action==RespondersActions.NotifyArrival && Mybody. Acknowledged)
		{
			Mybody.InterpretedTrigger= RespondersTriggers.FirstTimeonLocation;
			Mybody.Acknowledged=false;Mybody.Sending=false; 	

			System.out.println("                                                                                    "+"RC : "+ Mybody.Id +" arrived RC");
		}

	}		

	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	// RCO -  CoordinateInformationCollectionofCasualty ( Decentralized -Centralized  )
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	private void  RCOCommanderBehavior_CoordinateCasualtyinfromation()
	{		
		//--------------------------------------------------------------------------------------------	
		// ++++++ 1- +++++++
		if (Mybody.InterpretedTrigger== RespondersTriggers.FirstTimeonLocation)
		{
			// Implmenting_plan(); 
			StartSetup=true;

			Mybody.Action=RespondersActions.FormulatePlan;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.Planformulation;  	
			Mybody.EndofCurrentAction=  InputFile.FormulatePlan_duration  ; 
			Mybody.InterpretedTrigger=null; 
		}
		// ++++++ 2- +++++++
		else if ( Mybody.Action==RespondersActions.FormulatePlan  && Mybody.EndActionTrigger== RespondersTriggers.EndingAction 	) 			 	
		{
			Mybody.Action=RespondersActions.Noaction;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.WaitingDuring;	
			Mybody.EndActionTrigger=null;
		}
		//-------------------------------------------------------------------------------------------- (P1)
		//==========================New/Back Responder========================== Both N/A
		// ++++++ 3- +++++++
		else if (Mybody.Action==RespondersActions.Noaction  && ( Mybody.CommTrigger == RespondersTriggers.GetNewPolicemanarrived || Mybody.CommTrigger == RespondersTriggers.ComeBack  ) )		  										
		{	 	
			if (Mybody.CommTrigger == RespondersTriggers.GetNewPolicemanarrived )
				UpdatePlan( UpdatePlanType.NewRes, null, null ,NewResponder,null ,null) ;
			else if ( Mybody.CommTrigger == RespondersTriggers.ComeBack ) 
				UpdatePlan( UpdatePlanType.FreeRes, null, null ,FinshedResponder,null,null) ;

			Mybody.Action=RespondersActions.GetArrivalNotification ;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.Comunication ; 
			Mybody.EndofCurrentAction=  InputFile.GetNotification_duration ; 
			Mybody.CommTrigger=null; //reciving=true;
			//System.out.println("                                                                                    "+"RC: "+  Mybody.Id  +"  GetNewPolicemanarrived  "   );
		}
		// ++++++ 4- +++++++
		else if ( Mybody.Action==RespondersActions.GetArrivalNotification  && Mybody.EndActionTrigger== RespondersTriggers.EndingAction 	) 			 	
		{
			Mybody.CurrentSender.Acknowledg(Mybody);  //reciving=false; 	

			Mybody.Action=RespondersActions.UpdatePlan;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.Planformulation;  
			Mybody.EndofCurrentAction=  InputFile.UdatePlan_duration  ; 
			Mybody.EndActionTrigger=null; 

		}
		//==========================Result of Setup RC ==========================  N/A 
		// ++++++ 5- +++++++
		else if ( Mybody.Action==RespondersActions.Noaction  &&  Mybody.CommTrigger== RespondersTriggers.GetresultofSetup )	  										
		{	 	
			//Update plan
			UpdatePlan( UpdatePlanType.FreeRes,null, null ,FinshedResponder ,null,null) ;
			EndRCEstablished=0;
			AllocatedResponderfroSetup --;

			if  ( AllocatedResponderfroSetup == 0)
			{
				EndRCEstablished = 1 ;
				StartSetup=false;
				System.out.println("                                                                                    "+"RC: "+  Mybody.Id  +"  Done Setup   "  +this.MyResponders.size()  );
			}

			Mybody.Action=RespondersActions.GetResultofSetupTA ;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.Comunication;  
			Mybody.EndofCurrentAction=  InputFile.GetNotification_duration  ; 
			//reciving=true;
		}
		// ++++++ 6- +++++++
		else if ( Mybody.Action==RespondersActions.GetResultofSetupTA  && Mybody.EndActionTrigger== RespondersTriggers.EndingAction 	) 			 	
		{
			Mybody.CurrentSender.Acknowledg(Mybody);  //reciving=false; 	

			Mybody.Action=RespondersActions.UpdatePlan;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.Planformulation;  
			Mybody.EndofCurrentAction=  InputFile.UdatePlan_duration  ; 
			Mybody.EndActionTrigger=null; 
			Mybody.CommTrigger=null; 

			//System.out.println("                                                                                    "+"RC: "+  Mybody.Id  +"  send  Acknowledg to "  +Mybody.CurrentSender.Id  );
		}
		//==========================Result of casualty information deacased/un  ========================== Both Paper
		// ++++++ 7- +++++++
		else if ( Mybody.Action==RespondersActions.Noaction && ( Mybody.CommTrigger== RespondersTriggers.Getresultcasultyinfromation_uninjured || Mybody.CommTrigger== RespondersTriggers. Getresultcasultyinfromation_Decased )   )		  										
		{	 				
			Mybody.Action=RespondersActions.Getresultcasultyinfromation; 
			Mybody.BehaviourType3=RespondersBehaviourTypes3.Comunication; 
			this.TaskOtion= 7 ;
			Mybody.EndofCurrentAction= InputFile.GetInfromation_FtoForR_Duration_Notification; 	
			//reciving=true;
		}
		// ++++++ 8- +++++++
		else if ( Mybody.Action==RespondersActions.Getresultcasultyinfromation && TaskOtion== 7  && Mybody.EndActionTrigger== RespondersTriggers.EndingAction 	) 			 	
		{		
			Casualty ca=null;

			Mybody.CurrentSender.Acknowledg(Mybody);  //reciving=false; 				
			ca=CurrentCasualtybySender ;
			//UpdatePlan	
			UpdatePlan( UpdatePlanType.CloseTask, ca , null , null ,null ,ca.PoliceCI ); 

			if ( Mybody.CommTrigger== RespondersTriggers.Getresultcasultyinfromation_uninjured )
				UpdatePlan( UpdatePlanType.FreeRes, null, null ,FinshedResponder,null,null) ;

			Mybody.CommTrigger=null; 

			Mybody.Action=RespondersActions.UpdatePlan;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.Planformulation; 
			Mybody.EndofCurrentAction= InputFile.UdatePlan_duration ; 
			Mybody.EndActionTrigger=null; 
			this.TaskOtion= 0 ;
		}
		//==========================Result of casualty information inj  ========================== Both Paper/Electronic
		// ++++++ 7- +++++++
		else if ( Mybody.Action==RespondersActions.Noaction && ( Mybody.CommTrigger== RespondersTriggers.Getresultcasultyinfromation_injured )   )	// || Mybody.assignedIncident.NewRecordcasualtyadded_ISSystem(Mybody.Role,Mybody.AssignedSector , 1)>0		  										
		{	 				
			Mybody.Action=RespondersActions.Getresultcasultyinfromation; 
			Mybody.BehaviourType3=RespondersBehaviourTypes3.Comunication;  
			TaskOtion= 8 ; 
			Mybody.EndofCurrentAction= InputFile.GetInfromation_FtoForR_Duration_Data  * CurrentCasualtyListbySender.size()  ; 
			//reciving=true;
		}
		// ++++++ 8- +++++++
		else if ( Mybody.Action==RespondersActions.Getresultcasultyinfromation  && TaskOtion== 8  && Mybody.EndActionTrigger== RespondersTriggers.EndingAction 	) 			 	
		{		

			Mybody.CurrentSender.Acknowledg(Mybody);  //reciving=false; 				
			Mybody.CommTrigger=null; 

			for ( Casualty ca : CurrentCasualtyListbySender  )
			//	UpdatePlan( UpdatePlanType.TrackTask, ca , null , null ,null ,ca.PoliceCI);
			UpdatePlan( UpdatePlanType.CloseTask, ca , null , null ,null ,ca.PoliceCI ); 

			Mybody.Action=RespondersActions.UpdatePlan;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.Planformulation;  
			Mybody.EndofCurrentAction= InputFile.UdatePlan_duration ; 
			Mybody.EndActionTrigger=null; 
			TaskOtion= 0 ;
		}
		//==========================Request for CollectDeceasedcasualty from PIC (AIC or FIC) ==========================   N/A 
		// ++++++ 9- +++++++
		else if ( Mybody.Action==RespondersActions.Noaction  &&  Mybody.CommTrigger== RespondersTriggers.GetRequstecollectDeceasedcasualty  )	  										
		{	 				
					
			//UpdatePlan
			for(Casualty    Ca:CurrentCasualtyListbySender) {
				UpdatePlan( UpdatePlanType.NewTask,  Ca, Police_TaskType.CollectDeceasedCasualty ,null ,null ,null); 
				
			}
			
			Mybody.Action=RespondersActions.GetRequestcollectDeceasedcasualty ;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.Comunication; 
			Mybody.EndofCurrentAction= (InputFile.GetInfromation_FtoForR_Duration_Data * CurrentCasualtyListbySender.size() ); 
			Mybody.CommTrigger=null;  //reciving=true;
			System.out.println("                                                                                    "+"RC: " + Mybody.Id +"Get  Deceasedcasualty   from  "+ Mybody.CurrentSender.Id + CurrentCasualtyListbySender.size()  +" " + InInnerorCCS );
		}
		// ++++++ 10- +++++++
		else if ( Mybody.Action==RespondersActions.GetRequestcollectDeceasedcasualty && Mybody.EndActionTrigger== RespondersTriggers.EndingAction 	) 			 	
		{
			Mybody.CurrentSender.Acknowledg(Mybody);  //reciving=false;	

			Mybody.Action=RespondersActions.UpdatePlan;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.Planformulation; 
			Mybody.EndofCurrentAction=  InputFile.UdatePlan_duration  ; 
			Mybody.EndActionTrigger=null; 
		}

		//==========================Request for liaison officer from PIC ==========================   N/A 
		// ++++++ 9- +++++++
		else if ( Mybody.Action==RespondersActions.Noaction  &&  Mybody.CommTrigger== RespondersTriggers.GetRequstecollectInjuredcasualty  )	  										
		{	 	
			Mybody.Action=RespondersActions.GetRequstecollectInjuredcasualty ;
			
			//UpdatePlan
			for(Casualty    Ca:CurrentCasualtyListbySender) 
				evaccasualty_list.add(Ca);

			for(Casualty    Ca:CurrentCasualtyListbySender) {
				UpdatePlan( UpdatePlanType.NewTask,  Ca, Police_TaskType.collectInjuriedEvacuetedCasualty ,null ,null ,null); 
				
			}
			
			
			Mybody.BehaviourType3=RespondersBehaviourTypes3.Comunication;  
			Mybody.EndofCurrentAction= (InputFile.GetInfromation_FtoForR_Duration_Data * CurrentCasualtyListbySender.size() ); 
			Mybody.CommTrigger=null;  //reciving=true;

			//System.out.println("                                                                                    "+"RC: " + Mybody.Id +"Get   Injuredcasualty   from PIC" );
		}
		// ++++++ 10- +++++++
		else if ( Mybody.Action==RespondersActions.GetRequstecollectInjuredcasualty && Mybody.EndActionTrigger== RespondersTriggers.EndingAction 	) 			 	
		{
			Mybody.CurrentSender.Acknowledg(Mybody);//reciving=false;	

			Mybody.Action=RespondersActions.UpdatePlan;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.Planformulation;  
			Mybody.EndofCurrentAction=  InputFile.UdatePlan_duration  ; 
			Mybody.EndActionTrigger=null; 
		}

		//==========================End_NOmoreCasualty , Updates from PIC  ==========================		
		// ++++++ 11- +++++++
		else if (  Mybody.Action==RespondersActions.Noaction   &&  Mybody.CommTrigger== RespondersTriggers.GetSAupdatefromPIC     )											
		{	 							 
			int total=1;
			//Updates about SA
			if (  canEnterScene== 0)  //.InfromSafetyBriefandSectorEstablished
				{canEnterScene=1 ;total++;}

			if (   End_nomorec==0)
			{	End_nomorec=1;total++;}

			if (End_nomorec_impact==0 && End_nomorec_CCS ==0 )
				End_nomorec=1 ;

			Mybody.Action=RespondersActions.GetSAupdatefromPIC  ;		
			Mybody.BehaviourType3=RespondersBehaviourTypes3.Comunication;
			Mybody.EndofCurrentAction= InputFile.GetInfromation_FtoForR_Duration_Data * total ; 	
			Mybody.CommTrigger=null; //reciving=true;
		}
		// ++++++ 12- +++++++
		if (Mybody.Action==RespondersActions.GetSAupdatefromPIC  && Mybody.EndActionTrigger== RespondersTriggers.EndingAction 	  )
		{
			Mybody.CurrentSender.Acknowledg(Mybody);  //reciving=false; 

			Mybody.Action=RespondersActions.UpdatePlan;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.Planformulation;  
			Mybody.EndofCurrentAction= InputFile.UdatePlan_duration ; 
			Mybody.EndActionTrigger=null; 

			System.out.println("                                                                                    "+"RC: " + Mybody.Id +" GetSAupdate  from " + Mybody.CurrentSender.Id );
		}

		//============================================================ ALL
		// ++++++ 13- +++++++
		else if (Mybody.Action==RespondersActions.UpdatePlan && Mybody.EndActionTrigger== RespondersTriggers.EndingAction  )
		{						
			Mybody.Action=RespondersActions.Noaction;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.WaitingDuring;	
			Mybody.EndActionTrigger=null;
		}
		//----------------------------------------------------------------------------------------------- (P2) 
		// ++++++ 14- +++++++
		else  if ( Mybody.Action==RespondersActions.Noaction  &&
				(		
						(  Mybody.assignedIncident.Communication_Structure_uesd==Inter_Communication_Structure.Struc1_SilverCommandersInteraction  && ( ( ISthereupdateTosend( ) &&  Mybody.assignedIncident.Intra_Communication_Time_used==Communication_Time.When_need)   )) 
						||
						(  Mybody.assignedIncident.Communication_Structure_uesd==Inter_Communication_Structure.Struc2_BronzeCommandersInteraction  && (  ( Mybody.CurrentTick >= (Time_last_updated +  Mybody.assignedIncident.UpdatOPEvery) &&  Mybody.assignedIncident.Inter_Communication_Time_used==Communication_Time.Every_frequently )  ||  ( ISthereupdateTosend( ) &&  Mybody.assignedIncident.Inter_Communication_Time_used==Communication_Time.When_need)   )) 
						)				
				)
		{		

			if ( ISthereupdateTosend( ) )
			{ 
				//updates to silver commander
				if ( EndRCEstablished ==1  )
				{ Reporting_plane1(ACLPerformative.InfromRCEstablished) ; EndRCEstablished= 2; }
				
				//if ( IsThereReporting_casultiesInfromation() )
				//{	Reporting_plane_casultiesInfromation(ACLPerformative.InformcasultyinfromationReport) ;  }

				if ( End_RC==1  ) 
				{Reporting_plane1(ACLPerformative.InformEndcasultyinfromation ) ;End_RC=2;} 

				Mybody.Action=RespondersActions.InfromOPReport;
				Mybody.BehaviourType3=RespondersBehaviourTypes3.Comunication; 
				System.out.println("                                                                                    "+"RC: "+  Mybody.Id  +" update PIC  "   );
				for ( ACL_Message  A:Mybody.CurrentMessage_list )
					System.out.println("                                                                                    "+"RC: "+   Mybody.Id  +"  InfromOPReport    receiver:" + ( (Responder )A.receiver).Id  +"  " + A.performative   +"  sending " + ( (Responder )A.receiver).Sending  );
			
			}
			else
			{
				Mybody.Action=RespondersActions.UpdatePlan;
				Mybody.BehaviourType3=RespondersBehaviourTypes3.Planformulation;  
				Mybody.EndofCurrentAction=  InputFile.UdatePlan_duration  ; 
				Time_last_updated=Mybody.CurrentTick;

			}			
		}
		// ++++++ 15- +++++++
		else if (Mybody.Action==RespondersActions.InfromOPReport  && Mybody. Acknowledged )
		{
			Mybody.Action=RespondersActions.Noaction;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.WaitingDuring;	
			Mybody.Acknowledged=false;Mybody.Sending=false; 
			Time_last_updated=Mybody.CurrentTick ;
		}
		//-------------------------------------------------------------------------------------------- (P3) 
		//============= setup ======================
		// ++++++ 16- +++++++
		else if (  Mybody.Action==RespondersActions.Noaction && ( StartSetup && EndRCEstablished== -1 && ! Mybody.AssignedTA.installed)  && UnoccupiedResponders.size() >0 )		  										
		{
			AllocatedRespondertoTask=UnoccupiedResponders.get(0)  ;
			UnoccupiedResponders.remove(0);
			AllocatedResponderfroSetup ++;
			TaskOtion = 1 ;
			Mybody.Action=RespondersActions.AllocatResponders;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.Planformulation;
			Mybody.EndofCurrentAction=   InputFile.Allocation_duration ;

			//System.out.println("                                                                                    "+"RC: "+  Mybody.Id  +" Allocate for Setup   "   );
		}
		// ++++++ 17- +++++++
		else if (Mybody.Action==RespondersActions.AllocatResponders &&  TaskOtion == 1 &&   Mybody.EndActionTrigger== RespondersTriggers.EndingAction  )
		{				
			Command_Policeman(null ,Police_TaskType.SetupTacticalAreas , AllocatedRespondertoTask  );

			Mybody.Action=RespondersActions.CommandSetupTA ;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.Comunication; //TaskPlanning; 	
			Mybody.EndActionTrigger=null;	
			TaskOtion = 0;
		}
		// ++++++ 18- +++++++
		else if (Mybody.Action==RespondersActions.CommandSetupTA && Mybody.Acknowledged )
		{
			Mybody.Action=RespondersActions.Noaction;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.WaitingDuring;
			Mybody.Acknowledged=false;Mybody.Sending=false; 	
			//System.out.println("                                                                                    "+"RC: "+  Mybody.Id  +" gt Acknowledged setup"   );
		}
		//============= RC casualty ======================
		// ++++++ 19- +++++++
		else if (   Mybody.Action==RespondersActions.Noaction  && ThereisWaitingCasualty(Police_TaskType.CollectUninjuriedCasualty) && UnoccupiedResponders.size() >0 )	
		{			
			AllocatedTask = Allocate_Policeman( Allocation_Strategy.FIFO , Police_TaskType.CollectUninjuriedCasualty) ;	
			TaskOtion= 2;
			Mybody.Action=RespondersActions.AllocatResponders;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.Planformulation;
			Mybody.EndofCurrentAction=   InputFile.Allocation_duration ;
			//System.out.println("                                                                                    "+"RC: "+Mybody.Id  + "  Command un Casualty " );
		}
		// ++++++ 20- +++++++
		else if (Mybody.Action==RespondersActions.AllocatResponders && TaskOtion == 2 && Mybody.EndActionTrigger== RespondersTriggers.EndingAction  )
		{									
			Command_Policeman(AllocatedTask ,Police_TaskType.CollectUninjuriedCasualty, null);
			UpdatePlan( UpdatePlanType.AssignTask, AllocatedTask.TargetCasualty , null , null,null ,null);		

			Mybody.Action=RespondersActions.CommandPolicemanInformation ;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.Comunication; //TaskPlanning;  		
			Mybody.EndActionTrigger=null;
			TaskOtion=0;

		}
		//-------------------------------------------------------------------------------------------- (P5) 

		else if (   Mybody.Action==RespondersActions.Noaction   && evaccasualty_list.size()>0 && UnoccupiedResponders.size() >0 )	//ThereisWaitingCasualty(Police_TaskType.collectInjuriedEvacuetedCasualty)
		{			
			AllocatedRespondertoTask=UnoccupiedResponders.get(0)  ;
			UnoccupiedResponders.remove(0);
			TaskOtion= 3;
			
//			liaisonofficerCounter ++;liaisonofficerCounter != liaisonofficerMax
//			if ( liaisonofficerCounter == liaisonofficerMax )
//				liaisonofficerNeed=false;
			Mybody.Action=RespondersActions.AllocatResponders;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.Planformulation;
			Mybody.EndofCurrentAction=   InputFile.Allocation_duration ;

		}
		// ++++++ 20- +++++++
		else if (Mybody.Action==RespondersActions.AllocatResponders && TaskOtion == 3 && Mybody.EndActionTrigger== RespondersTriggers.EndingAction  )
		{											
			//for (Task_Police Task : Operational_Plan) 
			//if (Task.TaskStatus == D_Ontology.Ontology.GeneralTaskStatus.Waiting  &&  Task.TaskType== Police_TaskType.collectInjuriedEvacuetedCasualty) 
			//	this.liaisonofficer_CaList.add(Task.TargetCasualty);

			//UpdatePlan( UpdatePlanType.AssignTask_list, null , null , AllocatedRespondertoTask , liaisonofficer_CaList,null);	
			
			Command_Policeman(null ,Police_TaskType.collectInjuriedEvacuetedCasualty, AllocatedRespondertoTask );
			evaccasualty_list.clear();
			Mybody.Action=RespondersActions.CommandPolicemanInformation ;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.Comunication; //TaskPlanning;  		
			Mybody.EndActionTrigger=null;	
			TaskOtion=0;
			//System.out.println("                                                                                    "+"RC: "+Mybody.Id  + " Command  liaisonofficer " );
		}
		//-------------------------------------------------------------------------------------------- (P4) 

		else if (   Mybody.Action==RespondersActions.Noaction  && ThereisWaitingCasualty(Police_TaskType.CollectDeceasedCasualty) && UnoccupiedResponders.size() > 0 )	// && canEnterScene=1
		{			

			AllocatedTask = Allocate_Policeman( Allocation_Strategy.FIFO ,Police_TaskType.CollectDeceasedCasualty) ;	
			TaskOtion= 4;
			Mybody.Action=RespondersActions.AllocatResponders;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.Planformulation;
			Mybody.EndofCurrentAction=   InputFile.Allocation_duration ;
			//System.out.println("                                                                                    "+"RC: "+Mybody.Id  + "  Command CollectDeceasedCasualty " );
		}
		// ++++++ 20- +++++++
		else if (Mybody.Action==RespondersActions.AllocatResponders && TaskOtion == 4 && Mybody.EndActionTrigger== RespondersTriggers.EndingAction  )
		{									
			Command_Policeman(AllocatedTask ,Police_TaskType.CollectDeceasedCasualty, null );
			UpdatePlan( UpdatePlanType.AssignTask, AllocatedTask.TargetCasualty , null , null,null,null);		

			Mybody.Action=RespondersActions.CommandPolicemanInformation ;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.Comunication; //TaskPlanning;  		
			Mybody.EndActionTrigger=null;	
			TaskOtion=0;
		}
		//=================== All
		// ++++++ 21- +++++++
		else if (Mybody.Action==RespondersActions.CommandPolicemanInformation && Mybody.Acknowledged )
		{
			Mybody.Action=RespondersActions.Noaction;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.WaitingDuring;	
			Mybody.Acknowledged=false;Mybody.Sending=false; 	
			//System.out.println("                                                                                    "+"RC: "+Mybody.Id  + " done Command" );
		}
		//-------------------------------------------------------------------------------------------- (P5) 
		// ++++++ 22- +++++++
		else if ( Mybody.Action==RespondersActions.Noaction &&  ThereisNewCasualtyinRC()  && Mybody.AssignedTA.installed )
		{									
			Mybody.TargetCasualty  =NearstNewCasualtyinRC() ;  
			UpdatePlan( UpdatePlanType.NewTask, Mybody.TargetCasualty , Police_TaskType.CollectUninjuriedCasualty , null,null,null);
			//System.out.println("                                                                                    "+"RC: "+Mybody.Id  + " added" + Mybody.TargetCasualty.ID);

			Mybody.Action=RespondersActions.UpdatePlan;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.Planformulation;  
			Mybody.EndofCurrentAction=  InputFile.UdatePlan_duration  ; 

		}
		//------------------------------------------End-------------------------------------------------- (P6)  //IsAllTaskclosed()
		// ++++++ 23- +++++++	
		else if  (  Mybody.Action==RespondersActions.Noaction   &&  End_RC==2  && IsAllTaskclosed()) // No More casualty in both CCS and inner
		{				
			End_nomorec=2;

			System.out.println( "                                                                                    " +"RC: "+Mybody.Id  +" done collection******************1****************" );

			BoradcastAction(ACLPerformative.InformRCOprationsEND);

			Mybody.Action=RespondersActions.BoradcastEndRC ;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.Comunication;  
			Mybody.EndofCurrentAction=   InputFile.Boradcast_duration ;
			Mybody.Acknowledged=false;Mybody.Sending=false; 

		}
		// ++++++ 25- +++++++
		else if (Mybody.Action==RespondersActions.BoradcastEndRC && Mybody.EndActionTrigger== RespondersTriggers.EndingAction  )
		{				
			Mybody.InterpretedTrigger=RespondersTriggers.DonecollectinformationCasualtyActivity; //DoneActivity

			Mybody.Action=RespondersActions.Noaction;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.WaitingEnd;
			Mybody.EndActionTrigger= null;
			Mybody.Sending=false;

			System.out.println( "                                                                                    " +"RC: "+Mybody.Id  +" done collection*******************2***************" ); 

			System.out.println("................................................" );
			for (   Casualty ca : Mybody.assignedIncident.casualties)
				System.out.println(ca.ID + "  "+ ca.AcurrateCI + "  " + ca.PoliceCI  + "  "  + ca.DoneLog);
			System.out.println("................................................" );
		}
	}

	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	// RCO -  CoordinateEndResponse  
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	private void CommanderBehavior_EndER()
	{
		// ++++++ 1- +++++++
		if ( Mybody.CommTrigger==RespondersTriggers.ENDER ) 
		{
			Mybody.Action=RespondersActions.GetNotificationEndER ;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.Comunication;  
			Mybody.EndofCurrentAction= InputFile.GetNotification_duration ;
			Mybody.CommTrigger=null; 

		}
		// ++++++ 2- +++++++
		else if (Mybody.Action==RespondersActions.GetNotificationEndER  && Mybody.EndActionTrigger== RespondersTriggers.EndingAction   )
		{
			// Send message to its responders	
			BoradcastAction(ACLPerformative.InformERend);

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
			System.out.println( "                                                                                    " +Mybody.Id  +" GO back to Vehicle  " +Mybody.Role );

		}
	}

}//end class

