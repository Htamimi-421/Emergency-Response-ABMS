
package A_Roles_Ambulance;
import java.util.ArrayList;
import java.util.List;
import A_Agents.Casualty;
import A_Agents.Hospital;
import A_Agents.Responder;
import A_Agents.Responder_Ambulance;
import A_Agents.Vehicle;
import A_Agents.Vehicle_Ambulance;
import A_Environment.TacticalArea;
import B_Classes.Task_ambulance;
import B_Communication.ACL_Message;
import B_Communication.Casualty_info;
import B_Communication.Command;
import B_Communication.ISRecord;
import B_Communication.Report;
import C_SimulationInput.InputFile;
import D_Ontology.BuildStaticFuction;
import D_Ontology.Ontology;
import D_Ontology.Ontology.ACLPerformative;
import D_Ontology.Ontology.Allocation_Strategy;
import D_Ontology.Ontology.Ambulance_ResponderRole;
import D_Ontology.Ontology.Ambulance_TaskType;
import D_Ontology.Ontology.CasualtyReportandTrackingMechanism;
import D_Ontology.Ontology.CommunicationMechanism;
import D_Ontology.Ontology.Communication_Time;
import D_Ontology.Ontology.GeneralTaskStatus;
import D_Ontology.Ontology.Inter_Communication_Structure;
import D_Ontology.Ontology.RespondersActions;
import D_Ontology.Ontology.RespondersBehaviourTypes1;
import D_Ontology.Ontology.RespondersTriggers;
import D_Ontology.Ontology.TaskAllocationApproach;
import D_Ontology.Ontology.TypeMesg;
import D_Ontology.Ontology.UpdatePlanType;

public class Ambulance_Commander_CCO  {

	Responder_Ambulance Mybody;
	Responder_Ambulance NewResponder=null,FinshedResponder=null, AllocatedRespondertoTask=null;
	Responder_Ambulance		CurrentSC=null ;
	Casualty_info CurrentCasualtybySender_info=null;
	ArrayList<Casualty> CurrentTrapped_ListbySender =new ArrayList<Casualty>();
	Casualty CurrentCasualtybySender=null;
	Hospital currentRecivingHospital=null;
	Vehicle_Ambulance CurrentAssignedAmbulancetoCasualty=null; 	
	Hospital CurrentAssignedHospitaltoCasualty=null; 

	ISRecord Record_CurrentCasualty=null; 
	//----------------------------------------------------
	Task_ambulance  AllocatedTask , CurrentCheckedTask=null;
	int TriageOfficercounter=0 ;
	//int NoMoreCasualtyinCCSandFinalReportsend=0 ;

	boolean PreviousRequestAcepted= true; //I used this to avoid pair to pair conflict communication during using paper
	int OptionTask= 0;
	boolean StartSetup=false;
	//int counter=0;
	//----------------------------------------------------	
	//  The meaning of attribute content....  -1:No update   , 0: Get  , 1: realize or done  2: send to workers or PIC
	int  EndCCSEstablished = -1   , End_nomorecinScene= -1 ; 	
	int End_CCS=-1;
	int LAEstablished=-1;
	//----------------------------------------------------
	int AllocatedResponderfroSetup=0;
	List<Task_ambulance> Operational_Plan = new ArrayList<Task_ambulance>();
	List<Responder_Ambulance> MyResponders = new ArrayList<Responder_Ambulance>(); //its worker
	List<Responder_Ambulance> UnoccupiedResponders = new ArrayList<Responder_Ambulance>(); //free worker //free worker

	//----------------------------------------------------

	double Time_last_updated=0;	

	//##############################################################################################################################################################	
	public Ambulance_Commander_CCO  ( Responder_Ambulance _Mybody , TacticalArea  _TargetTA ) 
	{
		Mybody=_Mybody;
		Mybody.EndofCurrentAction=0;	
		Mybody.ColorCode= 5;
		Mybody.AssignedTA=   _TargetTA ;

		Mybody.assignedIncident.CCO_ambcommander=Mybody;
		Mybody.AssignedTA.Bronzecommander= Mybody;

		Mybody.PrvRoleinprint1 =Mybody.Role;
	}

	//##############################################################################################################################################################
	public void  CommanderCCO_InterpretationMessage()
	{
		boolean  done= true;
		ACL_Message currentmsg =  Mybody.Message_inbox.get(Mybody.Lastmessagereaded);		 			
		Mybody.CurrentSender= (Responder)currentmsg.sender;
		Mybody.Lastmessagereaded++;

		Mybody.SendingReciving_External= false ; Mybody.SendingReciving_internal=false; 
		if (  currentmsg.Inernal) Mybody.SendingReciving_internal= true ;
		else if (  currentmsg.External) Mybody.SendingReciving_External=true ;

		switch( currentmsg.performative) {

		case InformHospitalAllocationResult: // from AIC

			Mybody.CurrentCommandRequest=((Command) currentmsg.content);
			if ( Mybody.CurrentSender instanceof Responder_Ambulance && Mybody.CurrentSender==Mybody.assignedIncident.AICcommander &&  Mybody.CurrentCommandRequest.commandType1 ==Ambulance_TaskType.TransferCasualtytoHospital  )  
			{	
				Mybody.CommTrigger= RespondersTriggers.GetAllocatedHospital ; 
				CurrentCasualtybySender =Mybody.CurrentCommandRequest.TargetCasualty;
				CurrentAssignedHospitaltoCasualty = Mybody.CurrentCommandRequest.TargetHospital ;
			}
			break;

		case InformAmbulanceAllocationResult: //from ALC

			Mybody.CurrentCommandRequest=((Command) currentmsg.content);
			if ( Mybody.CurrentSender instanceof Responder_Ambulance && Mybody.CurrentSender==Mybody.assignedIncident.ALCcommander &&  Mybody.CurrentCommandRequest.commandType1 ==Ambulance_TaskType.TransferCasualtytoHospital  )  
			{				
				Mybody.CommTrigger= RespondersTriggers.GetAllocatedAmbulance;
				CurrentCasualtybySender =Mybody.CurrentCommandRequest.TargetCasualty;
				CurrentAssignedAmbulancetoCasualty= (Vehicle_Ambulance) Mybody.CurrentCommandRequest.AssignedAmbulance;
			}

			break;
			//++++++++++++++++++++++++++++++++++++++				
		case InformNewResponderArrival:
			Mybody.CommTrigger= RespondersTriggers.GetNewParamedicarrived;	
			NewResponder= (Responder_Ambulance) Mybody.CurrentSender;			
			break;
		case InformlocationArrival:
			Mybody.CommTrigger= RespondersTriggers.GetParamedicComeback ;
			FinshedResponder= (Responder_Ambulance) Mybody.CurrentSender;
			break;
		case InfromResultSetupCCS:
			Mybody.CommTrigger= RespondersTriggers.GetresultofSetup;
			FinshedResponder= (Responder_Ambulance) Mybody.CurrentSender;
			break;	
		case InformResultSecondTriage :
			Mybody.CommTrigger= RespondersTriggers.GetresultofTriage;
			CurrentCasualtybySender_info= ((Casualty_info)currentmsg.content);  //we need triafe lvel
			FinshedResponder= (Responder_Ambulance) Mybody.CurrentSender;
			break;
		case InformResultTreatment:
			Mybody.CommTrigger= RespondersTriggers.GetresultofTreatment;
			CurrentCasualtybySender_info = ((Casualty_info)currentmsg.content);
			FinshedResponder= (Responder_Ambulance) Mybody.CurrentSender; //wait with casualty or go if he dead
			break;
		case  InformResultPre_RescueTreatment:  
			Mybody.CommTrigger= RespondersTriggers.GetresultofPre_RescueTreatment;
			CurrentCasualtybySender_info = ((Casualty_info)currentmsg.content);
			FinshedResponder= (Responder_Ambulance) Mybody.CurrentSender;
			break;				
		case InformResultVTransfer:
			Mybody.CommTrigger= RespondersTriggers.GetresultofVTransfer;
			CurrentCasualtybySender_info = ((Casualty_info)currentmsg.content);
			FinshedResponder= null; //until arrived location
			break;
			//-------------------------------	
		case InformNomorecasualty_impact : // in inner From AIC or FRSC			
			Mybody.CommTrigger= RespondersTriggers.GetSAupdatefromAIC ;
			End_nomorecinScene= 0;
			//System.out.println(Mybody.Id + "  " + Mybody.CommTrigger + "  from  "+ Mybody.CurrentSender.Id  + "    Action:" + Mybody.Action);
			break;
		case InfromLoadingAreaEstablished : 			
			Mybody.CommTrigger= RespondersTriggers.GetSAupdatefromAIC ;
			LAEstablished= 0;
			break;	

		case InformTrappedcasualty :
			Mybody.CommTrigger= RespondersTriggers.GetSAupdatefromAIC ;
			Report CurrentReport=((Report) currentmsg.content);
			CurrentTrapped_ListbySender=CurrentReport.trapped_List;	
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
	// Create Plan-list of tasks- Action for first time
	public void Implmenting_plan() {

	};

	//***************************************************************************************************
	// Update Plan - add Casualty/Task
	public void UpdatePlan( UpdatePlanType UpdateType, Casualty _Casualty, Ambulance_TaskType _TaskType ,Responder_Ambulance _Responder , Hospital  _AssignedHospital , Vehicle  _AssignedAmbulance ,int Prio_Level , boolean _Decased) 
	{

		switch( UpdateType) {
		case NewRes :
			MyResponders.add(_Responder);
			UnoccupiedResponders.add(_Responder);
			_Responder.ColorCode= 0 ;	
			break;
		case FreeRes:
			UnoccupiedResponders.add(_Responder);
			break;
		case RemoveRes:
			MyResponders.remove(_Responder);
			break;

			//------------------------------------------------	
		case NewTask:  //triage
			Task_ambulance Newtask = new Task_ambulance( _Casualty, _TaskType ); //TaskStatus.waiting
			Operational_Plan.add(Newtask);
			Newtask.priority_level= Prio_Level ;
			break;
		case UpdateTask_triage: // new triage cat
			for (Task_ambulance T : Operational_Plan) 
				if (T.TargetCasualty == _Casualty )  //&& T.TaskType==_TaskType
				{
					if (   T.priority_level!=Prio_Level   )
					{	
						T.priority_level=Prio_Level ; 
						T.priority_leveldgetbad= true ;
					}

					if(	T.TaskStatus==GeneralTaskStatus.waiting_tobeinCCS ) //the ca are now inCCS
					{
						T.Pre_Treated=true; 
						T.TaskStatus=GeneralTaskStatus.Stop; 
					}



				}
			break;
		case AssignTask:  //treatment
			for (Task_ambulance T : Operational_Plan) 
				if (T.TargetCasualty == _Casualty && T.TaskType==_TaskType && T.TaskStatus==GeneralTaskStatus.Waiting) 
				{
					T.TaskStatus=GeneralTaskStatus.Inprogress ;
					T.TaskStartTime = Mybody.CurrentTick; //Task.AssignedResponder in allocation
					UnoccupiedResponders.remove(T.AssignedResponder );
				}						
			break;
		case UpdateTask_stop: //result treatment
			for (Task_ambulance T : Operational_Plan) 
				if (T.TargetCasualty == _Casualty && T.TaskType==_TaskType && T.TaskStatus==GeneralTaskStatus.Inprogress ) 
				{
					T.Treated=true; 
					T.TaskStatus=GeneralTaskStatus.Stop; 
					T.TimeofdoneTreatment= Mybody.CurrentTick;

					if (   T.priority_level!=Prio_Level   )
					{	
						T.priority_level=Prio_Level ; 
						T.priority_leveldgetbad= true ;
					}				
				}
		case UpdateTask_stop2: //result treatment
			for (Task_ambulance T : Operational_Plan) 
				if (T.TargetCasualty == _Casualty ) 
				{
					T.Pre_Treated=true; 
					T.TaskStatus=GeneralTaskStatus.Stop; 
					T.TimeofdoneTreatment= Mybody.CurrentTick;

				}
			break;
			//------------------------------------------------	
		case UpdateTask_waiting_tobeinCCS: //convert from pre_treatment to  treatment
			for (Task_ambulance T : Operational_Plan) 
				if (T.TargetCasualty == _Casualty &&  T.TaskStatus==GeneralTaskStatus.Inprogress) 
				{
					T.TaskType=Ambulance_TaskType.TreatmentandTransfertoVehicle ;

					T.priority_level=Prio_Level ; 
					T.TaskStatus=GeneralTaskStatus.waiting_tobeinCCS; 
					T.TimeofdoneTreatment= Mybody.CurrentTick;
				}
			break;
			//------------------------------------------------
		case UpdateTask_assignH :
			for (Task_ambulance T : Operational_Plan) 
				if (T.TargetCasualty == _Casualty ) //&& T.TaskType==_TaskType
				{
					if ( T.AssignedHospital!=null )
						T.NewupdateHosTOALC=true;
					T.AssignedHospital=_AssignedHospital ;
				}
			break;
		case UpdateTask_assignAmb :
			for (Task_ambulance T : Operational_Plan) 
				if (T.TargetCasualty == _Casualty )   //&& T.TaskType==_TaskType
				{ T.AssignedAmbulance=_AssignedAmbulance; }
			break;
		case UpdateTask_sendtoALC:
			for (Task_ambulance T : Operational_Plan) 
				if (T.TargetCasualty == _Casualty )   //&& T.TaskType==_TaskType
				{T.SendrequestoALC=true;

				T.NewupdateHosTOALC=false;  }	
			break;
		case UpdateTask_inprogress: //to amb
			for (Task_ambulance T : Operational_Plan) 
				if (T.TargetCasualty == _Casualty && T.TaskType==_TaskType) 
				{
					T.TaskStatus=GeneralTaskStatus.Inprogress ;
					T.TaskStartTime = Mybody.CurrentTick; //Task.AssignedResponder is waiting
					UnoccupiedResponders.remove(T.AssignedResponder );
				}
			break;		
		case  CloseTask:  // dead or done
			for (Task_ambulance T : Operational_Plan) 
				if (T.TargetCasualty == _Casualty ) 
				{
					T.TaskStatus=GeneralTaskStatus.Done;	
					T.TaskEndTime = Mybody.CurrentTick;
					if ( _Decased== true)
						T.Decased=true;
				}
			break;}
	}

	//***************************************************************************************************	
	//Execution_plane - Task Allocation		
	public Task_ambulance Allocate_paramedic( Ambulance_TaskType  Tasktypeofinterst ,  Allocation_Strategy Strategy) 
	{
		Task_ambulance nominatedTask=null;
		Responder Resp , nominatedResp=null ;

		switch(Strategy) {		
		case FIFO:		//Pre=treat	
			//FIFO casualty
			for (Task_ambulance Task : Operational_Plan) 
				if (Task.TaskStatus == Ontology.GeneralTaskStatus.Waiting && Task.TaskType== Tasktypeofinterst)
				{	
					Resp=UnoccupiedResponders.get(0);
					nominatedTask=Task;
					nominatedResp=Resp;	
					break;
				}
			break;

		case Severity_RYGPriorty: //treatment
			// identify more priority task-casualty- to assign nearest free responder
			double More_priorityRYG=4 ; 
			for (Task_ambulance Task : Operational_Plan)
				if (Task.TaskStatus == GeneralTaskStatus.Waiting && Task.TaskType== Tasktypeofinterst)
				{	
					if (Task.priority_level < More_priorityRYG)
					{ More_priorityRYG=Task.priority_level  ;   nominatedTask=Task;}

				}

			Resp=nominatedTask.ckeck_Distance_befor_TaskAssignment(UnoccupiedResponders);
			nominatedResp=Resp ;
			break;

		case Severity_RYGPriorty_trans :  
			// identify more priority task-casualty- to assign nearest free responder
			double More_priorityRYG1=4 ; 
			for (Task_ambulance Task : Operational_Plan)
				if (Task.TaskStatus == GeneralTaskStatus.Stop && Task.TaskType== Tasktypeofinterst &&  Task.AssignedHospital!=null && Task.AssignedAmbulance!=null)
				{	
					if (Task.priority_level < More_priorityRYG1)
					{ More_priorityRYG1=Task.priority_level  ;   nominatedTask=Task;}

				}

			Resp=nominatedTask.ckeck_Distance_befor_TaskAssignment(UnoccupiedResponders);
			nominatedResp=Resp ;
			break;	}

		nominatedTask.AssignedResponder= nominatedResp; 
		return nominatedTask ;

	}

	//##############################################################################################################################################################	
	//													Reporting
	//##############################################################################################################################################################	
	// CCS established 
	public void Reporting_plane1(ACLPerformative xx  ) 
	{
		// send message		
		if ( xx==ACLPerformative.InfromCCSEstablished  && Mybody.assignedIncident.Communication_Structure_uesd== Inter_Communication_Structure.Struc1_SilverCommandersInteraction  )
		{
			Mybody.CurrentMessage = new  ACL_Message( xx ,Mybody ,Mybody.assignedIncident.AICcommander,null, Mybody.CurrentTick , Mybody.assignedIncident.ComMechanism_level_StoB,1 ,TypeMesg.External) ;
			Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
		}		
		else if (xx==ACLPerformative.InfromCCSEstablished  &&  Mybody.assignedIncident.Communication_Structure_uesd== Inter_Communication_Structure.Struc2_BronzeCommandersInteraction  )
		{			
			Mybody.CurrentMessage = new  ACL_Message( xx ,Mybody ,Mybody.assignedIncident.FRSCcommander1,null, Mybody.CurrentTick , Mybody.assignedIncident.Inter_Communication_Tool_used,1  ,TypeMesg.External) ;
			Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
		}




		if (xx==ACLPerformative.InformNomorecasualty_CCS  &&  Mybody.assignedIncident.Communication_Structure_uesd== Inter_Communication_Structure.Struc1_SilverCommandersInteraction  )
		{
			Mybody.CurrentMessage = new  ACL_Message( xx ,Mybody ,Mybody.assignedIncident.AICcommander,null, Mybody.CurrentTick , Mybody.assignedIncident.ComMechanism_level_StoB,1 ,TypeMesg.Inernal) ;
			Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
		}		
		else if (xx==ACLPerformative.InformNomorecasualty_CCS &&  Mybody.assignedIncident.Communication_Structure_uesd== Inter_Communication_Structure.Struc2_BronzeCommandersInteraction  )
		{			
			Mybody.CurrentMessage = new  ACL_Message( xx ,Mybody ,Mybody.assignedIncident.RCOcommander,null, Mybody.CurrentTick , Mybody.assignedIncident.Inter_Communication_Tool_used,1 ,TypeMesg.External) ;
			Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);

			Mybody.CurrentMessage = new  ACL_Message( xx ,Mybody ,Mybody.assignedIncident.AICcommander,null, Mybody.CurrentTick , Mybody.assignedIncident.ComMechanism_level_StoB,1 ,TypeMesg.Inernal) ;
			Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
		}


	};
	//***************************************************************************************************  
	public boolean  IsthereReporting_TriageReport( ) //report
	{	
		boolean Send =false;
		for (Task_ambulance T :Operational_Plan ) 
			if ( T.TaskStatus != GeneralTaskStatus.Done && T.TaskType == Ambulance_TaskType.TreatmentandTransfertoVehicle &&  ! T.Decased  && (T.SendTriageReporttoAIC==false ||( T.SendTriageReporttoAIC==true && T.priority_leveldgetbad == true && T.AssignedAmbulance ==null ) ))  
			{				
				Send =true;break;
			}



		return Send ;
	}
	//--------------------------
	public boolean  IsthereReporting_DecasedReport( ) 
	{	
		boolean Send =false;
		for (Task_ambulance T :Operational_Plan ) 
			if (T.TaskStatus == GeneralTaskStatus.Done && T.TaskType == Ambulance_TaskType.TreatmentandTransfertoVehicle  && T.SendDecasedReporttoAIC==false  &&   T.Decased ) 
			{
				Send =true;break;
			}

		return Send ;
	}
	//--------------------------
	public boolean  IsthereReporting_casToLogInfoReport( ) //report
	{	
		boolean Send =false;
		for (Task_ambulance T :Operational_Plan ) 
			if (  T.TaskStatus != GeneralTaskStatus.Done && T.TaskType == Ambulance_TaskType.TreatmentandTransfertoVehicle && T.SendfinalReporttoAIC==false && T.AssignedHospital!=null &&  ! T.Decased ) 
			{				

				Send =true;break;
			}

		return Send ;
	}
	//--------------------------
	public boolean  Reporting_plane_Report_triage(ACLPerformative xx ) //report
	{	

		boolean Send1 =false;

		ArrayList<Casualty_info> casualtiesneedtoHospital_List =new ArrayList<Casualty_info>();
		casualtiesneedtoHospital_List.clear();

		for (Task_ambulance T :Operational_Plan ) 
			if ( T.TaskStatus != GeneralTaskStatus.Done && T.TaskType == Ambulance_TaskType.TreatmentandTransfertoVehicle &&  ! T.Decased  && (T.SendTriageReporttoAIC==false ||( T.SendTriageReporttoAIC==true && T.priority_leveldgetbad == true && T.AssignedAmbulance ==null ) ))  
			{				

				Casualty_info ca_inf=new Casualty_info ( T.TargetCasualty , T.priority_level ) ;

				casualtiesneedtoHospital_List.add(ca_inf );

				T.priority_leveldgetbad= false;
				if ( T.SendTriageReporttoAIC==false ) T.SendTriageReporttoAIC=true;

				Send1 =true;
			}


		if (  (Send1 ==true )   )
		{
			Report Report1 =new Report();			
			Report1. Ambulance_OPReport_CCS(casualtiesneedtoHospital_List , null ,null);

			Mybody.CurrentMessage  = new  ACL_Message( xx ,Mybody ,Mybody.assignedIncident.AICcommander,Report1, Mybody.CurrentTick ,Mybody.assignedIncident.ComMechanism_level_StoB,casualtiesneedtoHospital_List.size(),TypeMesg.Inernal) ;
			Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
		}


		return true ;
	}
	//--------------------------
	public boolean  Reporting_plane_Report_Deandinfo(ACLPerformative xx ) //report
	{	
		boolean Send2=false,Send3=false;

		ArrayList<Casualty> Decasedcasualties_List =new ArrayList<Casualty>();
		Decasedcasualties_List.clear();

		ArrayList<Casualty> Casualtiesinfor_List =new ArrayList<Casualty>();
		Casualtiesinfor_List.clear();

		for (Task_ambulance T :Operational_Plan ) 
			if (T.TaskStatus == GeneralTaskStatus.Done && T.TaskType == Ambulance_TaskType.TreatmentandTransfertoVehicle  && T.SendDecasedReporttoAIC==false  &&   T.Decased ) 
			{
				Decasedcasualties_List.add(T.TargetCasualty);
				T.SendDecasedReporttoAIC=true;
				Send2 =true;
			}

		for (Task_ambulance T :Operational_Plan ) 
			if (  T.TaskStatus != GeneralTaskStatus.Done && T.TaskType == Ambulance_TaskType.TreatmentandTransfertoVehicle && T.SendfinalReporttoAIC==false && T.AssignedHospital!=null &&  ! T.Decased ) 
			{						
				Casualtiesinfor_List.add(T.TargetCasualty);
				T.SendfinalReporttoAIC=true;
				Send3 =true;
				//System.out.println(Mybody.Id +" ***********************************  " + T.TargetCasualty.ID + "  "  + T.TargetCasualty.PoliceCI  + " " + T.TargetCasualty.Triage_tage);
			}


		if (   Mybody.assignedIncident.Communication_Structure_uesd== Inter_Communication_Structure.Struc1_SilverCommandersInteraction  )
		{
			Report Report1 =new Report();			
			Report1. Ambulance_OPReport_CCS(null , Decasedcasualties_List ,Casualtiesinfor_List);

			Mybody.CurrentMessage  = new  ACL_Message( xx ,Mybody ,Mybody.assignedIncident.AICcommander,Report1, Mybody.CurrentTick ,Mybody.assignedIncident.ComMechanism_level_StoB,( Decasedcasualties_List.size() + Casualtiesinfor_List.size()),TypeMesg.External) ;
			Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
		}

		else if (  Mybody.assignedIncident.Communication_Structure_uesd== Inter_Communication_Structure.Struc2_BronzeCommandersInteraction  )
		{				
			if (Send3 ==true) {
				Report Report2 =new Report();			
				Report2. Ambulance_OPReport_CCS(null, null ,Casualtiesinfor_List);

				Mybody.CurrentMessage  = new  ACL_Message( ACLPerformative.InformCCSReport_casTolog ,Mybody ,Mybody.assignedIncident.RCOcommander,Report2, Mybody.CurrentTick ,Mybody.assignedIncident.Inter_Communication_Tool_used,Casualtiesinfor_List.size(),TypeMesg.External) ;
				Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
			}

			if (Send2 ==true) {
				Report Report2 =new Report();			
				Report2. Ambulance_OPReport_CCS(null, Decasedcasualties_List ,null);

				Mybody.CurrentMessage  = new  ACL_Message( ACLPerformative.InformCCSReport ,Mybody ,Mybody.assignedIncident.RCOcommander,Report2, Mybody.CurrentTick ,Mybody.assignedIncident.Inter_Communication_Tool_used,Decasedcasualties_List.size(),TypeMesg.External) ;
				Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
			}
		}

		return true ;
	}

	//***************************************************************************************************
	//Execution_plane  - commanding	
	public void Command_paramedic(Task_ambulance _Task , Responder_Ambulance _Responder ,Ambulance_TaskType xx  ) {

		Command CMD1 =new Command();

		if ( _Task==null ) // responders
		{
			CMD1.AmbulanceCommand("0",xx);
			Mybody.CurrentMessage = new  ACL_Message( ACLPerformative.Command , Mybody, _Responder ,CMD1, Mybody.CurrentTick,Mybody.assignedIncident.ComMechanism_level_BtoRes,1  ,TypeMesg.Inernal ) ;
			Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
		}
		else if ( _Task.AssignedAmbulance  ==null ) //treatment
		{
			CMD1.AmbulanceCommand(_Task.TaskID ,_Task.TaskType ,_Task.TargetCasualty );
			Mybody.CurrentMessage = new  ACL_Message( ACLPerformative.Command , Mybody, _Task.AssignedResponder ,CMD1, Mybody.CurrentTick,Mybody.assignedIncident.ComMechanism_level_BtoRes ,1  ,TypeMesg.Inernal) ;
			Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
		}
		else
		{
			CMD1.AmbulanceCommand(_Task.TaskID ,_Task.TaskType ,_Task.TargetCasualty,_Task.AssignedAmbulance );  //transfer
			Mybody.CurrentMessage = new  ACL_Message( ACLPerformative.Command , Mybody, _Task.AssignedResponder ,CMD1, Mybody.CurrentTick,Mybody.assignedIncident.ComMechanism_level_BtoRes,1 ,TypeMesg.Inernal  ) ;
			Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
		}

		// send message with command

	} 
	//***************************************************************************************************
	//Broadcast
	public void BoradcastAction(ACLPerformative xx)
	{		
		Mybody.CurrentMessage  = new  ACL_Message( xx, Mybody, MyResponders ,null, Mybody.CurrentTick ,CommunicationMechanism.FF_BoradCast,1 ,TypeMesg.Inernal) ;
		Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
		// break; //one by one 
	}

	//##############################################################################################################################################################	
	//													Interpreted Triggers 
	//##############################################################################################################################################################
	public boolean IsNewCasualty( Casualty _Casualty )
	{	
		boolean  Result=true;

		for (Task_ambulance T : Operational_Plan) 
			if (T.TargetCasualty  == _Casualty  ) 
			{
				Result=false;
				break;
			}

		return Result;
	}
	//----------------------------------------------------------------------------------------------------
	//1  Treatment or Pre-
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
	//2-   Done treatment ...  need amb
	public boolean IsThereCasualtyneedtoAmbulance()
	{	
		boolean  Result=false;
		double MaxWaitTime=0;
		double More_priorityRYG=4 ; 
		CurrentCheckedTask=null;

		for (Task_ambulance T :Operational_Plan ) 
			if (T.TaskStatus == GeneralTaskStatus.Stop && T.TaskType == Ambulance_TaskType.TreatmentandTransfertoVehicle && (T.Treated ||T.Pre_Treated)  &&  T.AssignedHospital!=null && ( T.SendrequestoALC==false || T.NewupdateHosTOALC==true   ) &&T.AssignedAmbulance==null ) 
			{
				if (T.priority_level < More_priorityRYG)
				{ 
					More_priorityRYG=T.priority_level; 

					CurrentCheckedTask=T;Result=true;
				}
			}	


		//	//MaxWaitTime=Mybody.CurrentTick- T.TimeofdoneTreatment  ;			
		//				else if (T.priority_level == More_priorityRYG )					
		//				{
		//					double HowLongWaitTime= Mybody.CurrentTick- T.TimeofdoneTreatment  ;
		//					if (  HowLongWaitTime > MaxWaitTime)
		//					{	
		//						MaxWaitTime=HowLongWaitTime;
		//						CurrentCheckedTask=T;Result=true;
		//					}



		return Result;
	}

	//----------------------------------------------------------------------------------------------------
	//3- evacuation Severity then FIFO
	public boolean IsThereCasualtyreadyTotansfertoAmbulance()  
	{	
		boolean  Result=false;

		for (Task_ambulance T :Operational_Plan ) 
			if (T.TaskStatus == GeneralTaskStatus.Stop && T.TaskType == Ambulance_TaskType.TreatmentandTransfertoVehicle &&    T.AssignedHospital!=null && T.AssignedAmbulance!=null  ) //&& T.TargetCasualty.DoneLog
			{
				Result=true;

			}

		//				else if (T.priority_level == More_priorityRYG )					
		//				{
		//					double HowLongWaitTime= Mybody.CurrentTick- T.TimeofdoneTreatment  ;
		//					if (  HowLongWaitTime > MaxWaitTime)
		//					{	
		//						MaxWaitTime=HowLongWaitTime;
		//						CurrentCheckedTask=T;Result=true;
		//					}


		return Result;
	}

	//----------------------------------------------------------------------------------------------------
	//4
	public boolean IsAllTaskclosed()
	{	
		boolean  Result=true;

		for (Task_ambulance Task : Operational_Plan) 
			if (Task.TaskStatus != GeneralTaskStatus.Done  ) 
			{
				Result=false;
				break;
			}


		return Result;
	}

	//----------------------------------------------------------------------------------------------------		
	public boolean ISthereupdateTosend( )
	{	
		boolean  Result= false;

		if ( EndCCSEstablished==1  )
		{    Result=true; }

		if  ( Mybody.assignedIncident.CasualtyReport_Mechanism_IN==CasualtyReportandTrackingMechanism.Paper  )	  //  IsthereReporting_TriageReport( )  ||
		{   
			if ( (  IsthereReporting_DecasedReport( ) || IsthereReporting_casToLogInfoReport( )  ) &&      
					! (  IsThereCasualtyreadyTotansfertoAmbulance() && UnoccupiedResponders.size() >0)  &&
					!(   ThereisWaitingCasualty(Ambulance_TaskType.TreatmentandTransfertoVehicle) && UnoccupiedResponders.size() >0) &&
					!(    ThereisWaitingCasualty (Ambulance_TaskType.Pre_RescueTreatment) && UnoccupiedResponders.size() >0) &&			 
					! IsThereCasualtyneedtoAmbulance() )
				Result=true; 
		}
		else if  ( Mybody.assignedIncident.CasualtyReport_Mechanism_IN==CasualtyReportandTrackingMechanism.Electronic   )	
		{
			if ( (  IsthereReporting_DecasedReport( ) || IsthereReporting_casToLogInfoReport( )  )&&
					! (  IsThereCasualtyreadyTotansfertoAmbulance() && UnoccupiedResponders.size() >0)  &&
					!(   ThereisWaitingCasualty(Ambulance_TaskType.TreatmentandTransfertoVehicle) && UnoccupiedResponders.size() >0) &&
					!(    ThereisWaitingCasualty (Ambulance_TaskType.Pre_RescueTreatment) && UnoccupiedResponders.size() >0) 	 )
				Result=true; 
		}

		if  ( End_CCS!= 2 &&   End_nomorecinScene==1  &&  IsAllTaskclosed()   && UnoccupiedResponders.size()==( this.MyResponders.size()-1)  ) // Mybody.assignedIncident.CCStation. NOMorecasualtyinTAexceptFatality()
		{Result=true; End_CCS= 1;}

		return Result;
	}	

	//______________________________________________________________________________________________________________________________________________________________
	//##############################################################################################################################################################
	//                                                        Behavior
	//##############################################################################################################################################################
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	// CCO commander - General 
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	public void CCOBehavior()	
	{		
		//*************************************************************************
		// 1- initial response
		if( Mybody.CommTrigger==RespondersTriggers.AssigendRolebyAIC) 				
		{
			Mybody.CurrentAssignedActivity=Ambulance_TaskType.GoTolocation;	
		}
		// 2- Coordinate CCS
		else if (Mybody.CurrentAssignedActivity==Ambulance_TaskType.GoTolocation &&  Mybody.InterpretedTrigger== RespondersTriggers.FirstTimeonLocation	) 
		{
			Mybody.CurrentAssignedActivity=Ambulance_TaskType.CoordinateCCS ;
		}			
		// 3-No action
		else if ( Mybody.InterpretedTrigger==RespondersTriggers.DoneActivity)  
		{
			Mybody.CurrentAssignedActivity=Ambulance_TaskType.None;	
			Mybody.Action=RespondersActions.Noaction ;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.WaitingEnd;

			Mybody.InterpretedTrigger=null;
			//System.out.println( Mybody.Id  +" done CCS  "  +  Mybody.CommTrigger );
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
			CCOCommanderBehavior_GoToCCS()	;	
			break;
		case CoordinateCCS :
			CCOCommanderBehavior_CoordinateCasualtyTriageandTreatment();
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
	// CCO - Go to location   
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^	
	private void CCOCommanderBehavior_GoToCCS()	
	{
		// ++++++ 1- +++++++
		if( Mybody.CommTrigger==RespondersTriggers.AssigendRolebyAIC)
		{	

			Mybody.Assign_DestinationCordon(Mybody.assignedIncident.CCStation.Location); 

			Mybody.Action=RespondersActions.GoToCCS;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Movementonincidentsite ; 	
			Mybody.CommTrigger=null;
		}
		// ++++++ 2- +++++++
		else if (Mybody.Action==RespondersActions.GoToCCS && Mybody.SensingeEnvironmentTrigger==null)
		{			
			Mybody.Walk();
		}	
		// ++++++ 3- +++++++
		else if (Mybody.Action==RespondersActions.GoToCCS && Mybody.SensingeEnvironmentTrigger== RespondersTriggers.ArrivedTargetObject  ) 
		{ 					
			//System.out.println(Mybody.Id +" arived ");
			//send message to AIC				
			Mybody.CurrentMessage  = new  ACL_Message( ACLPerformative.InformlocationArrival , Mybody , Mybody.assignedIncident.AICcommander ,null ,  Mybody.CurrentTick,Mybody.assignedIncident.ComMechanism_level_StoB,1 ,TypeMesg.Inernal ) ;
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

			System.out.println("  "+ "CCS: " + Mybody.Id +" arrived ");
		}

	}		

	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	// CCO -  CoordinateCCS (Centralized  )
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	private void  CCOCommanderBehavior_CoordinateCasualtyTriageandTreatment()
	{					
		//--------------------------------------------------------------------------------------------	
		// ++++++ 1- +++++++
		if (Mybody.InterpretedTrigger== RespondersTriggers.FirstTimeonLocation)
		{		
			// Implmenting_plan(); 
			StartSetup=true; 
			TriageOfficercounter= 0 ;

			Mybody.Action=RespondersActions.FormulatePlan;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Planformulation  ;   
			Mybody.EndofCurrentAction=  InputFile.FormulatePlan_duration  ; 
			Mybody.InterpretedTrigger=null; 
		}
		// ++++++ 2- +++++++
		else if ( Mybody.Action==RespondersActions.FormulatePlan  && Mybody.EndActionTrigger== RespondersTriggers.EndingAction 	) 			 	
		{
			Mybody.Action=RespondersActions.Noaction;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.WaitingDuring;	
			Mybody.EndActionTrigger=null;
		}			
		//-------------------------------------------------------------------------------------------- (P1)
		//==========================New/Back Responder========================== Both  N/A
		// ++++++ 3- +++++++
		if ( Mybody.Action==RespondersActions.Noaction && ( Mybody.CommTrigger == RespondersTriggers.GetNewParamedicarrived ||Mybody.CommTrigger == RespondersTriggers.GetParamedicComeback  ) )	//come back from transfer	  										
		{	 	
			if (Mybody.CommTrigger == RespondersTriggers.GetNewParamedicarrived )
				UpdatePlan( UpdatePlanType.NewRes, null, null ,NewResponder,null,null,-1,false) ;
			else if ( Mybody.CommTrigger == RespondersTriggers.GetParamedicComeback  ) 
			{UpdatePlan( UpdatePlanType.FreeRes, null, null ,FinshedResponder,null,null,-1,false) ;  }

			Mybody.Action=RespondersActions.GetArrivalNotification ;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Comunication; 
			Mybody.EndofCurrentAction=  InputFile.GetNotification_duration ; 
			//reciving=true;
		}
		// ++++++ 4- +++++++
		else if ( Mybody.Action==RespondersActions.GetArrivalNotification  && Mybody.EndActionTrigger== RespondersTriggers.EndingAction ) 			 	
		{
			//System.out.println("  "+ "CCS: " + Mybody.Id +" GetArrivalNotification " + NewResponder.Id );		
			if (Mybody.CommTrigger == RespondersTriggers.GetNewParamedicarrived )
				NewResponder.Acknowledg(Mybody);

			else if ( Mybody.CommTrigger == RespondersTriggers.GetParamedicComeback  ) 
				FinshedResponder.Acknowledg(Mybody);	

			Mybody.CommTrigger=null; //reciving=false;

			Mybody.Action=RespondersActions.UpdatePlan;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Planformulation ; 
			Mybody.EndofCurrentAction=  InputFile.UdatePlan_duration  ; 
			Mybody.EndActionTrigger=null; 
		}
		//==========================Result of Setup sector ==========================  Both N/A
		// ++++++ 5- +++++++
		else if (Mybody.Action==RespondersActions.Noaction &&  Mybody.CommTrigger== RespondersTriggers.GetresultofSetup )	  								
		{	 	
			//Update plan
			UpdatePlan( UpdatePlanType.FreeRes, null, null ,FinshedResponder,null,null,-1,false) ;
			EndCCSEstablished = 0 ;
			AllocatedResponderfroSetup --;

			if  ( AllocatedResponderfroSetup == 0)
			{
				EndCCSEstablished = 1 ;
				StartSetup=false;
				System.out.println("  "+"CCS: "+  Mybody.Id  +"  Done Setup   " + +MyResponders.size()  );
			}

			Mybody.Action=RespondersActions.GetResultofSetupTA ;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Comunication;  
			Mybody.EndofCurrentAction=  InputFile.GetNotification_duration; 

		}
		// ++++++ 6- +++++++
		else if ( Mybody.Action==RespondersActions.GetResultofSetupTA  && Mybody.EndActionTrigger== RespondersTriggers.EndingAction 	) 			 	
		{
			Mybody.CurrentSender.Acknowledg(Mybody);	//reciving=false;

			Mybody.Action=RespondersActions.UpdatePlan;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Planformulation  ;  
			Mybody.EndofCurrentAction=  InputFile.UdatePlan_duration  ; 
			Mybody.EndActionTrigger=null; 
			Mybody.CommTrigger=null; 
		}
		//==========================Result of second Triage========================== Both Paper/Electronic
		// ++++++ 7- +++++++
		else if ( Mybody.Action==RespondersActions.Noaction && ( Mybody.CommTrigger== RespondersTriggers.GetresultofTriage||  Mybody.CommTrigger== RespondersTriggers.GetresultofPre_RescueTreatment ||
				( Mybody.CommTrigger== null && Mybody.assignedIncident.NewRecordcasualtyadded_ISSystem(Mybody.Role,null , 1)>0	)  )   )		  										
		{	 				
			Mybody.Action=RespondersActions.GetResultTriage; 
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Comunication; 

			if ( Mybody.CommTrigger== RespondersTriggers.GetresultofTriage ||  Mybody.CommTrigger== RespondersTriggers.GetresultofPre_RescueTreatment)   
				Mybody.EndofCurrentAction= InputFile.GetInfromation_FtoForR_Duration_Data; 
			//reciving=true;

			else 
			{
				Mybody.EndofCurrentAction= InputFile.GetInfromation_Electronic_Duration ;
				Record_CurrentCasualty=Mybody.assignedIncident.GeRecordcasualtyISSystem(Mybody.Role ,null,1);//FIFO  
				Mybody.SendingReciving_External= false ; Mybody.SendingReciving_internal= true ;
			}			
		}
		// ++++++ 8- +++++++
		else if ( Mybody.Action==RespondersActions.GetResultTriage  && Mybody.EndActionTrigger== RespondersTriggers.EndingAction 	) 			 	
		{		
			Casualty ca;
			int prio_level;
			boolean ThisFatlity ;
			boolean Pre_tre=false;
			boolean thisTrapp=false;

			if ( Mybody.CommTrigger== RespondersTriggers.GetresultofTriage ||  Mybody.CommTrigger== RespondersTriggers.GetresultofPre_RescueTreatment)
			{
				Mybody.CurrentSender.Acknowledg(Mybody);	//reciving=false;			


				ca=CurrentCasualtybySender_info.ca ;
				prio_level=CurrentCasualtybySender_info.priority_level;
				ThisFatlity=CurrentCasualtybySender_info.ThisFatlity;
				if (Mybody.CommTrigger== RespondersTriggers.GetresultofPre_RescueTreatment ) Pre_tre=true;

				thisTrapp=CurrentCasualtybySender_info.IwillupdateaboutTrapparrivedCCS ; //from scendry triage

			}
			else 
			{						

				ca=Record_CurrentCasualty.CasualtyinRec;
				prio_level=Record_CurrentCasualty.priority_levelinRec ;
				ThisFatlity=Record_CurrentCasualty.FatlityRecord;
				if ( Record_CurrentCasualty.Pre_treatment== true && Record_CurrentCasualty.IssuedByAmbCCO==true   && Record_CurrentCasualty.SecondTriaged== false ) Pre_tre=true;
				if ( Record_CurrentCasualty.Pre_treatment== true  && Record_CurrentCasualty.SecondTriaged== true ) Pre_tre=false ;
				thisTrapp=Record_CurrentCasualty.IwillupdateaboutTrapparrivedCCS ;
				Record_CurrentCasualty.IwillupdateaboutTrapparrivedCCS= false;
			}

			//UpdatePlan 
			if(Mybody.CommTrigger== RespondersTriggers.GetresultofTriage  || Pre_tre==false)
			{
				if ( IsNewCasualty( ca ))
					UpdatePlan( UpdatePlanType.NewTask,ca , Ambulance_TaskType.TreatmentandTransfertoVehicle , null,null,null,prio_level ,false) ;
				else 
				{			
					//getbad
					UpdatePlan( UpdatePlanType.UpdateTask_triage, ca , Ambulance_TaskType.TreatmentandTransfertoVehicle , null,null,null,prio_level ,false) ;							
				}

				if ( thisTrapp  )				
				{
					UpdatePlan( UpdatePlanType.UpdateTask_stop2,ca ,null , null,null,null,prio_level,false) ;// ready to send to AIC 
				}

				if (  ThisFatlity   )
				{ 
					UpdatePlan( UpdatePlanType.CloseTask, ca , Ambulance_TaskType.TreatmentandTransfertoVehicle, null,null,null,prio_level ,true) ;
				}
			}
			else if (  Mybody.CommTrigger== RespondersTriggers.GetresultofPre_RescueTreatment || Pre_tre==true)	
			{				
				UpdatePlan( UpdatePlanType.UpdateTask_waiting_tobeinCCS , ca , null, null,null,null,prio_level ,false) ; // if Isssue

				if (  ThisFatlity   )
				{ 
					UpdatePlan( UpdatePlanType.CloseTask, ca , null, null,null,null,prio_level ,true) ;
				}
			}

			Mybody.CommTrigger=null; 
			Mybody.Action=RespondersActions.UpdatePlan;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Planformulation  ; 
			Mybody.EndofCurrentAction= InputFile.UdatePlan_duration ; 
			Mybody.EndActionTrigger=null; 
		}
		//==========================Result of Treatment:ready ========================== Both Paper/Electronic
		// ++++++ 9- +++++++
		else if ( Mybody.Action==RespondersActions.Noaction && ( Mybody.CommTrigger== RespondersTriggers.GetresultofTreatment  || 
				(Mybody.CommTrigger== null && Mybody.assignedIncident.NewRecordcasualtyadded_ISSystem(Mybody.Role,Mybody.AssignedSector ,2)>0)	 )  )
		{		
			Mybody.Action=RespondersActions.GetResultTreatment ;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Comunication;  

			if ( Mybody.CommTrigger== RespondersTriggers.GetresultofTreatment )
				Mybody.EndofCurrentAction= InputFile.GetInfromation_FtoForR_Duration_Notification ; 
			else 
			{
				Mybody.EndofCurrentAction= InputFile.GetInfromation_Electronic_Duration ;
				Record_CurrentCasualty=Mybody.assignedIncident.GeRecordcasualtyISSystem(Mybody.Role ,null,2);//FIFO  inside Record.checkedSC=true;
				Mybody.SendingReciving_External= false ; Mybody.SendingReciving_internal= true ;
			}	
			//System.out.println("  "+"CCS: "+ Mybody.Id  +"GetresultofTreatment");
		}
		// ++++++ 10- +++++++
		else if ( Mybody.Action==RespondersActions.GetResultTreatment  && Mybody.EndActionTrigger== RespondersTriggers.EndingAction 	) 			 	
		{					
			Casualty ca;
			int prio_level;
			boolean ThisFatlity ;
			Responder_Ambulance Res=null;

			//============ 1 ============ 
			if ( Mybody.CommTrigger== RespondersTriggers.GetresultofTreatment  ) // paper
			{
				Mybody.CurrentSender.Acknowledg(Mybody);	
				Mybody.CommTrigger=null; 

				ca=CurrentCasualtybySender_info.ca ;
				prio_level=CurrentCasualtybySender_info.priority_level;
				ThisFatlity=CurrentCasualtybySender_info.ThisFatlity;
				Res=(Responder_Ambulance) Mybody.CurrentSender;		

			}
			else  // Device
			{													
				ca=Record_CurrentCasualty.CasualtyinRec;
				prio_level=Record_CurrentCasualty.priority_levelinRec ;
				ThisFatlity=Record_CurrentCasualty.FatlityRecord;
				Res=Record_CurrentCasualty.AssignedParamdicinRec;				
			}
			//============ 2 ============ 
			//update plan
			if ( ! ThisFatlity  )			
			{
				UpdatePlan( UpdatePlanType.UpdateTask_stop,ca , Ambulance_TaskType.TreatmentandTransfertoVehicle , null,null,null,prio_level,false) ;// ready to send to AIC 
				UpdatePlan( UpdatePlanType.FreeRes, null, null ,Res,null,null,-1,false) ;
			}
			else 
			{
				UpdatePlan( UpdatePlanType.CloseTask, ca , Ambulance_TaskType.TreatmentandTransfertoVehicle, null,null,null,prio_level,true) ;
				UpdatePlan( UpdatePlanType.FreeRes, null, null ,Res,null,null,-1,false) ;
			}

			Mybody.Action=RespondersActions.UpdatePlan;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Planformulation  ; 
			Mybody.EndofCurrentAction= InputFile.UdatePlan_duration ; 
			Mybody.EndActionTrigger=null; 

		}
		//==========================Result of Allocated Hospital  ==========================Both  Paper only
		// ++++++ 11- +++++++
		else if ( Mybody.Action==RespondersActions.Noaction  && ( Mybody.CommTrigger== RespondersTriggers.GetAllocatedHospital   )   )												
		{	 				
			Mybody.Action=RespondersActions.GetAllocatedHospital;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Comunication ; 
			Mybody.EndofCurrentAction= InputFile.GetInfromation_FtoForR_Duration_Data ; 
			Mybody.CommTrigger=null; 	
		}
		// ++++++ 12- +++++++
		else if ( Mybody.Action==RespondersActions.GetAllocatedHospital && Mybody.EndActionTrigger== RespondersTriggers.EndingAction 	) 			 	
		{					
			Mybody.CurrentSender.Acknowledg(Mybody);  //reciving=false;	
			//UpdatePlan		
			UpdatePlan( UpdatePlanType.UpdateTask_assignH, CurrentCasualtybySender , null ,null,CurrentAssignedHospitaltoCasualty,null,-1 ,false) ;				

			Mybody.Action=RespondersActions.UpdatePlan;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Planformulation  ; 
			Mybody.EndofCurrentAction= InputFile.UdatePlan_duration ; 
			Mybody.EndActionTrigger=null; 
		}
		//==========================Result of Allocated ambulance ==========================Both  Paper only
		// ++++++ 13- +++++++
		else if ( Mybody.Action==RespondersActions.Noaction  && ( Mybody.CommTrigger== RespondersTriggers.GetAllocatedAmbulance   )   )		 										
		{	 				
			Mybody.Action=RespondersActions.GetAllocatedAmbulance ;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Comunication ; 
			Mybody.EndofCurrentAction= InputFile.GetInfromation_FtoForR_Duration_Data ; 			
			Mybody.CommTrigger=null; //reciving=true;

			//System.out.println("  "+"CCS: "+ Mybody.Id +"GetAllocatedAmbulance for "+ CurrentCasualtybySender.ID);
		}
		// ++++++ 14- +++++++
		else if ( Mybody.Action==RespondersActions.GetAllocatedAmbulance && Mybody.EndActionTrigger== RespondersTriggers.EndingAction 	) 		//PreviousRequestAcepted= true;	 	
		{		
			Mybody.CurrentSender.Acknowledg(Mybody);   //reciving=false;	
			//UpdatePlan			
			UpdatePlan( UpdatePlanType.UpdateTask_assignAmb, CurrentCasualtybySender , Ambulance_TaskType.TreatmentandTransfertoVehicle ,null,null,CurrentAssignedAmbulancetoCasualty,-1 ,false) ;	

			Mybody.Action=RespondersActions.UpdatePlan;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Planformulation ; 
			Mybody.EndofCurrentAction= InputFile.UdatePlan_duration ; 
			Mybody.EndActionTrigger=null; 
			PreviousRequestAcepted= true;
		}
		//==========================Result of H Am one time========================== Both 	Electronic only
		// ++++++ 15- +++++++
		else if ( Mybody.Action==RespondersActions.Noaction && ( Mybody.CommTrigger== null && Mybody.assignedIncident.NewRecordcasualtyadded_ISSystem(Mybody.Role,null,3)>0)	)
		{
			Mybody.Action=RespondersActions.GetAllocatedHospitalAmbulanceBoth;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Comunication; 
			Mybody.EndofCurrentAction= InputFile.GetInfromation_Electronic_Duration ; 			
			Record_CurrentCasualty= Mybody.assignedIncident.GeRecordcasualtyISSystem(Mybody.Role,null ,3);//FIFO  inside Record.checkedSC=true;	
			Mybody.SendingReciving_External= false ; Mybody.SendingReciving_internal= true ;
			//System.out.println("  "+"CCS: "+ Mybody.Id +"GetAllocated Am H ........");
		}
		// ++++++ 16- +++++++
		else if ( Mybody.Action==RespondersActions.GetAllocatedHospitalAmbulanceBoth && Mybody.EndActionTrigger== RespondersTriggers.EndingAction 	) 			 	
		{		

			UpdatePlan( UpdatePlanType.UpdateTask_assignH, Record_CurrentCasualty.CasualtyinRec , Ambulance_TaskType.TreatmentandTransfertoVehicle, null,Record_CurrentCasualty.AssignedHospitalinRec,Record_CurrentCasualty.AssignedAmbulanceinRec,-1,false) ;
			UpdatePlan( UpdatePlanType.UpdateTask_assignAmb, Record_CurrentCasualty.CasualtyinRec , Ambulance_TaskType.TreatmentandTransfertoVehicle, null,Record_CurrentCasualty.AssignedHospitalinRec,Record_CurrentCasualty.AssignedAmbulanceinRec,-1,false) ;

			Mybody.Action=RespondersActions.UpdatePlan;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Planformulation  ; 
			Mybody.EndofCurrentAction= InputFile.UdatePlan_duration ; 
			Mybody.EndActionTrigger=null; 
		}
		//==========================Result of Transfer to V ==========================	Both	Paper/Electronic
		// ++++++ 17- +++++++
		else if ( Mybody.Action==RespondersActions.Noaction && ( Mybody.CommTrigger== RespondersTriggers.GetresultofVTransfer || ( Mybody.CommTrigger== null && Mybody.assignedIncident.NewRecordcasualtyadded_ISSystem(Mybody.Role,null,4)>0)	  )   )		  										
		{	 				
			Mybody.Action=RespondersActions.GetResultTransfer; 
			if ( Mybody.CommTrigger== RespondersTriggers.GetresultofVTransfer )
				Mybody.EndofCurrentAction= InputFile.GetInfromation_FtoForR_Duration_Notification ; 
			else 
			{
				Mybody.EndofCurrentAction= InputFile.GetInfromation_Electronic_Duration ;	
				Record_CurrentCasualty= Mybody.assignedIncident.GeRecordcasualtyISSystem(Mybody.Role,null ,4);//FIFO  inside Record.checkedSC=true;
				Mybody.SendingReciving_External= false ; Mybody.SendingReciving_internal= true ;
			}
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Comunication; 				
		}
		// ++++++ 18- +++++++
		else if ( Mybody.Action==RespondersActions.GetResultTransfer  && Mybody.EndActionTrigger== RespondersTriggers.EndingAction 	) 			 	
		{		
			Casualty ca;
			Responder_Ambulance Res=null;

			if ( Mybody.CommTrigger== RespondersTriggers.GetresultofVTransfer)
			{
				Mybody.CurrentSender.Acknowledg(Mybody);   //reciving=false;	
				Mybody.CommTrigger=null; 
				ca=CurrentCasualtybySender_info.ca ;
			}
			else 
			{						

				ca=Record_CurrentCasualty.CasualtyinRec;
			}

			//UpdatePlan	
			UpdatePlan( UpdatePlanType.CloseTask, ca , Ambulance_TaskType.TreatmentandTransfertoVehicle, null,null,null,-1,false) ;	// Will be free when he comeback

			Mybody.Action=RespondersActions.UpdatePlan;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Planformulation ; 
			Mybody.EndofCurrentAction= InputFile.UdatePlan_duration ; 
			Mybody.EndActionTrigger=null; 

		}
		//==========================No more casualty in scene , Updates from AIC or FrSC ==========================		
		// ++++++ 19- +++++++
		else if (  Mybody.Action==RespondersActions.Noaction   &&  Mybody.CommTrigger== RespondersTriggers.GetSAupdatefromAIC     )											
		{	 							 
			int total=0 ;
			//Updates about SA				
			if (   End_nomorecinScene==0 )  
			{	End_nomorecinScene=1;total++;}

			if(  LAEstablished== 0)
			{	LAEstablished= 2;total++;}

			if ( CurrentTrapped_ListbySender.size() >0 )
			{
				for( Casualty ca : CurrentTrapped_ListbySender )
				{
					total++;
					UpdatePlan( UpdatePlanType.NewTask,ca , Ambulance_TaskType.Pre_RescueTreatment , null,null,null,99 ,false) ;				
				}
				CurrentTrapped_ListbySender.clear();
				System.out.println("  "+"CCS: " + Mybody.Id +"Gettrraped "  + total );
			}

			System.out.println("  "+"CCS: " + Mybody.Id  +" GetSAupdate  from " + Mybody.CurrentSender.Id);


			Mybody.Action=RespondersActions.GetSAupdatefromAIC  ;		
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Comunication;  
			Mybody.EndofCurrentAction= InputFile.GetInfromation_FtoForR_Duration_Data * total ; 	
			Mybody.CommTrigger=null; 
		}
		// ++++++ 20- +++++++
		if (Mybody.Action==RespondersActions.GetSAupdatefromAIC  && Mybody.EndActionTrigger== RespondersTriggers.EndingAction 	  )	
		{
			Mybody.CurrentSender.Acknowledg(Mybody);   //reciving=false;
			Mybody.Action=RespondersActions.UpdatePlan;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Planformulation  ; 
			Mybody.EndofCurrentAction= InputFile.UdatePlan_duration ; 
			Mybody.EndActionTrigger=null; 


		}
		//============================================================ ALL
		// ++++++ 21- +++++++
		else if (Mybody.Action==RespondersActions.UpdatePlan && Mybody.EndActionTrigger== RespondersTriggers.EndingAction  )
		{						
			Mybody.Action=RespondersActions.Noaction;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.WaitingDuring;
			Mybody.EndActionTrigger=null;
		}


		//-------------------------------------------------------------------------------------------------
		// ++++++ 22- +++++++
		else  if ( Mybody.Action==RespondersActions.Noaction  && 
				(		
						(  Mybody.assignedIncident.Communication_Structure_uesd==Inter_Communication_Structure.Struc1_SilverCommandersInteraction  && ( ( ISthereupdateTosend( ) &&  Mybody.assignedIncident.Intra_Communication_Time_used==Communication_Time.When_need)   )) 
						||
						(  Mybody.assignedIncident.Communication_Structure_uesd==Inter_Communication_Structure.Struc2_BronzeCommandersInteraction  && (  ( Mybody.CurrentTick >= (Time_last_updated +  Mybody.assignedIncident.UpdatOPEvery) &&  Mybody.assignedIncident.Inter_Communication_Time_used==Communication_Time.Every_frequently )  ||  ( ISthereupdateTosend( ) &&  Mybody.assignedIncident.Inter_Communication_Time_used==Communication_Time.When_need)   )) 
						)				
				)

		{
			//updates to silver commander  ( CCS established ,Triage report, Dead casualties, casu information !! )												
			//--------------
			if (  ISthereupdateTosend( )  )
			{ 
				if ( EndCCSEstablished==1  )
				{ Reporting_plane1(ACLPerformative.InfromCCSEstablished) ; EndCCSEstablished= 2;  }	


				if  ( Mybody.assignedIncident.CasualtyReport_Mechanism_IN==CasualtyReportandTrackingMechanism.Paper   )	
				{   
					//Mybody.zzz=true;

					if (	Mybody.assignedIncident.Communication_Structure_uesd==Inter_Communication_Structure.Struc1_SilverCommandersInteraction)
					{
						//if ( IsthereReporting_TriageReport( )    )
						//	this.Reporting_plane_Report_triage(ACLPerformative.InformCCSReport);
						//else
						if  (  IsthereReporting_DecasedReport( ) || IsthereReporting_casToLogInfoReport( )  )
							this.Reporting_plane_Report_Deandinfo(ACLPerformative.InformCCSReport)  ;
					}
					else
					{
						//						if ( IsthereReporting_TriageReport( )    )
						//							this.Reporting_plane_Report_triage(ACLPerformative.InformCCSReport);
						if  (  IsthereReporting_DecasedReport( ) || IsthereReporting_casToLogInfoReport( )  )
							this.Reporting_plane_Report_Deandinfo(ACLPerformative.InformCCSReport)  ;
					}


				}
				else if  ( Mybody.assignedIncident.CasualtyReport_Mechanism_IN==CasualtyReportandTrackingMechanism.Electronic )
				{
					if  (  IsthereReporting_DecasedReport( ) || IsthereReporting_casToLogInfoReport( )  )
						this.Reporting_plane_Report_Deandinfo(ACLPerformative.InformCCSReport)  ;
				}

				if ( End_CCS==1 )
				{ Reporting_plane1(ACLPerformative.InformNomorecasualty_CCS  );   End_CCS=2  ;} 


				Mybody.Action=RespondersActions.InfromOPReport;
				Mybody.BehaviourType1=RespondersBehaviourTypes1.Comunication;  


				System.out.println("  "+"CCS: "+ Mybody.Id  +"  InfromOPReport  " + Mybody.CurrentMessage_list.size()   );
				for ( ACL_Message  A:Mybody.CurrentMessage_list )
					System.out.println("  "+"CCS: "+ Mybody.Id  +"  InfromOPReport  receiver:" + ( (Responder )A.receiver).Id  +"  " + A.performative   +"  sending " + ( (Responder )A.receiver).Sending  ); //+ "    to"+  ( (Responder )A.receiver).MyReceiver  + "in "  + ( (Responder )A.receiver).Mytimetosend);
			}
			else
			{
				Mybody.Action=RespondersActions.UpdatePlan;
				Mybody.BehaviourType1=RespondersBehaviourTypes1.Planformulation ; 
				Mybody.EndofCurrentAction=  InputFile.UdatePlan_duration  ; 
				Time_last_updated=Mybody.CurrentTick;

			}
		}
		// ++++++ 23- +++++++
		else if (Mybody.Action==RespondersActions.InfromOPReport && Mybody. Acknowledged )
		{
			Mybody.Action=RespondersActions.Noaction;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.WaitingDuring;
			Mybody.Acknowledged=false;Mybody.Sending=false; 
			Time_last_updated=Mybody.CurrentTick;

			//for(ISRecord     Rec: Mybody.assignedIncident.ISSystem) 
			//	System.out.println( Rec.CasualtyinRec.ID +" --------   "+ Rec.priority_levelinRec + " FieldTriaged   "+ Rec.FieldTriaged + " SecondTriaged  "+ Rec.SecondTriaged+ " Treated  "+ Rec.Treated + " TransferdV "+ Rec.TransferdV+ " TransferdH "+ Rec.TransferdH    );

		}
		//-------------------------------------------------------------------------------------------- (P3)  N/A
		//============= setup ======================
		// ++++++ 24- +++++++
		else if ( Mybody.Action==RespondersActions.Noaction  && ( StartSetup && EndCCSEstablished== -1 && ! Mybody.AssignedTA.installed )  && UnoccupiedResponders.size() >0  )		  										
		{

			AllocatedRespondertoTask=UnoccupiedResponders.get(0)  ;
			UnoccupiedResponders.remove(0);
			AllocatedResponderfroSetup ++;
			OptionTask= 1;
			Mybody.Action=RespondersActions.AllocatResponders;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Planformulation ;
			Mybody.EndofCurrentAction=   InputFile.Allocation_duration ;
			//System.out.println("  "+"CCS: "+ Mybody.Id  +"  allocate Setupe  " +AllocatedRespondertoTask.Id   );

		}
		// ++++++ 25- +++++++
		else if (Mybody.Action==RespondersActions.AllocatResponders &&   OptionTask== 1 &&  Mybody.EndActionTrigger== RespondersTriggers.EndingAction  )
		{				
			Command_paramedic(null ,AllocatedRespondertoTask ,Ambulance_TaskType.SetupTacticalAreas );

			Mybody.Action=RespondersActions.CommandSetupTA ;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Comunication; //TaskPlanning;			
			Mybody.EndActionTrigger=null;
			OptionTask= 0;
		}
		// ++++++ 26- +++++++
		else if (Mybody.Action==RespondersActions.CommandSetupTA && Mybody. Acknowledged )
		{
			//System.out.println("  "+"CCS: "+  Mybody.Id  + " Get Acknowledg "  +"  "+ Mybody.CurrentTick );
			Mybody.Action=RespondersActions.Noaction;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.WaitingDuring;

			Mybody.Acknowledged=false;Mybody.Sending=false; 		
		}
		//============= Triage Officer ======================
		// ++++++ 27- +++++++
		else if (  Mybody.Action==RespondersActions.Noaction && TriageOfficercounter < 1 && UnoccupiedResponders.size() >0  && Mybody.AssignedTA.installed )	
		{			
			AllocatedRespondertoTask =UnoccupiedResponders.get(0);
			UnoccupiedResponders.remove(0);
			TriageOfficercounter ++ ;
			OptionTask= 2;

			Mybody.Action=RespondersActions.AllocatResponders;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Planformulation ;
			Mybody.EndofCurrentAction=   InputFile.Allocation_duration ;
		}
		// ++++++ 28- +++++++
		else if (Mybody.Action==RespondersActions.AllocatResponders && OptionTask==2  && Mybody.EndActionTrigger== RespondersTriggers.EndingAction  )
		{									

			Command_paramedic(null ,AllocatedRespondertoTask ,Ambulance_TaskType.SecondryTriage );

			Mybody.Action=RespondersActions.CommandParamedicTriage;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Comunication; //TaskPlanning;			
			Mybody.EndActionTrigger=null;	
			OptionTask= 0;

			//System.out.println("  "+"CCS: "+ Mybody.Id  +"  allocate SecondryTriage  "   );
		}
		// ++++++ 29- +++++++
		else if (Mybody.Action==RespondersActions.CommandParamedicTriage && Mybody. Acknowledged )
		{
			Mybody.Action=RespondersActions.Noaction;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.WaitingDuring;
			Mybody.Acknowledged=false;Mybody.Sending=false; 	
		}
		//-------------------------------------------------------------------------------------------- (P4) Trapped 
		// ++++++ 15- +++++++
		else if (  Mybody.Action==RespondersActions.Noaction && ThereisWaitingCasualty (Ambulance_TaskType.Pre_RescueTreatment)   && UnoccupiedResponders.size() >0 )							
		{															

			AllocatedTask = Allocate_paramedic(Ambulance_TaskType.Pre_RescueTreatment , Allocation_Strategy.FIFO) ;	
			OptionTask= 111;

			Mybody.Action=RespondersActions.AllocatResponders;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Planformulation ;
			Mybody.EndofCurrentAction=   InputFile.Allocation_duration ;
			System.out.println("  "+"CCS: "+  Mybody.Id+ "  command to go Amb  PRE ZZZZZZZZZZZZZZZZZZZZZZZZZ trapped" + AllocatedTask .TargetCasualty.ID +"   " + AllocatedTask .AssignedResponder.Id  );
		}
		// ++++++ 13- +++++++
		else if (Mybody.Action==RespondersActions.AllocatResponders &&  OptionTask==111 && Mybody.EndActionTrigger== RespondersTriggers.EndingAction  )
		{												
			Command_paramedic(AllocatedTask , null ,null ); 

			UpdatePlan( UpdatePlanType.UpdateTask_inprogress, AllocatedTask.TargetCasualty , Ambulance_TaskType.Pre_RescueTreatment , null,null ,null,-1,false) ;	

			Mybody.Action=RespondersActions.CommandParamedicTriageorPre_treatment;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Comunication; //TaskPlanning ;			
			Mybody.EndActionTrigger=null;	
			OptionTask= 0;	
		}
		// ++++++ 14- +++++++
		else if (Mybody.Action==RespondersActions.CommandParamedicTriageorPre_treatment  && Mybody. Acknowledged )
		{			
			Mybody.Action=RespondersActions.Noaction;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.WaitingDuring;
			Mybody.Acknowledged=false;Mybody.Sending=false; 		
		}
		//-------------------------------------------------------------------------------------------- (P5) Both  TtoV 
		// ++++++ 30- +++++++
		else if ( Mybody.Action==RespondersActions.Noaction  &&  LAEstablished==2 && IsThereCasualtyreadyTotansfertoAmbulance()   && UnoccupiedResponders.size() >0   )		  										
		{
			AllocatedTask = Allocate_paramedic(Ambulance_TaskType.TreatmentandTransfertoVehicle , Allocation_Strategy.Severity_RYGPriorty_trans) ;	
			OptionTask= 11;

			Mybody.Action=RespondersActions.AllocatResponders;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Planformulation ;
			Mybody.EndofCurrentAction=   InputFile.Allocation_duration ;
			//System.out.println("  "+"CCS: "+  Mybody.Id+ "  command to go Amb  " + AllocatedTask .TargetCasualty.ID +"   " + AllocatedTask .AssignedResponder.Id  );
		}
		// ++++++ 33- +++++++
		else if (Mybody.Action==RespondersActions.AllocatResponders && OptionTask==11  && Mybody.EndActionTrigger== RespondersTriggers.EndingAction  )
		{									

			Command_paramedic(AllocatedTask , null ,null ); //No Allocation because there is paramedic waiting with casualty

			UpdatePlan( UpdatePlanType.UpdateTask_inprogress, AllocatedTask.TargetCasualty , Ambulance_TaskType.TreatmentandTransfertoVehicle , null,null ,null,-1,false) ;	

			Mybody.Action=RespondersActions.CommandParamedicTransfer ;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Comunication; //TaskPlanning;			
			Mybody.EndActionTrigger=null;	
			OptionTask= 0;					

		}
		// ++++++ 31- +++++++
		else if (Mybody.Action==RespondersActions.CommandParamedicTransfer && Mybody. Acknowledged )
		{
			Mybody.Action=RespondersActions.Noaction;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.WaitingDuring;
			Mybody.Acknowledged=false;Mybody.Sending=false; 		
		}
		//-------------------------------------------------------------------------------------------- (P6) Both  Treatment
		// ++++++ 32- +++++++
		else if (  Mybody.Action==RespondersActions.Noaction && ThereisWaitingCasualty(Ambulance_TaskType.TreatmentandTransfertoVehicle) && UnoccupiedResponders.size() >0 )	
		{			
			AllocatedTask = Allocate_paramedic(Ambulance_TaskType.TreatmentandTransfertoVehicle , Allocation_Strategy.Severity_RYGPriorty) ;	
			OptionTask= 3;

			Mybody.Action=RespondersActions.AllocatResponders;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Planformulation ;
			Mybody.EndofCurrentAction=   InputFile.Allocation_duration ;

			//System.out.println("  "+"CCS: "+  Mybody.Id + "  Tasked treatment " + AllocatedTask.TargetCasualty.ID +"   " +AllocatedTask.AssignedResponder.Id  );
		}
		// ++++++ 33- +++++++
		else if (Mybody.Action==RespondersActions.AllocatResponders && OptionTask==3  && Mybody.EndActionTrigger== RespondersTriggers.EndingAction  )
		{									
			Command_paramedic(AllocatedTask,null,null);
			UpdatePlan( UpdatePlanType.AssignTask, AllocatedTask.TargetCasualty , Ambulance_TaskType.TreatmentandTransfertoVehicle , null,null ,null,-1,false) ;		// responder assigned inside  allocation

			Mybody.Action=RespondersActions.CommandParamedicTreatment ;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Comunication; //TaskPlanning;			
			Mybody.EndActionTrigger=null;
			OptionTask= 0;
		}
		// ++++++ 34- +++++++
		else if (Mybody.Action==RespondersActions.CommandParamedicTreatment && Mybody. Acknowledged )
		{
			Mybody.Action=RespondersActions.Noaction;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.WaitingDuring;
			Mybody.Acknowledged=false;Mybody.Sending=false; 		
		}

		//-------------------------------------------------------------------------------------------- (P7) Paper hospital allocated by AIC  && PreviousRequestAcepted && 
		// ++++++ 35- +++++++
		else if ( Mybody.Action==RespondersActions.Noaction &&  Mybody.assignedIncident.CasualtyReport_Mechanism_IN== CasualtyReportandTrackingMechanism.Paper  && IsThereCasualtyneedtoAmbulance()  )	 										
		{
			Casualty_info ca_inf=new Casualty_info ( CurrentCheckedTask.TargetCasualty , CurrentCheckedTask.priority_level  ) ;

			//send message to ALC				
			Command CMD1 =new Command();
			CMD1.AmbulanceCommand("0" ,Ambulance_TaskType.TransferCasualtytoHospital  ,ca_inf , CurrentCheckedTask.AssignedHospital );
			// send message with command
			Mybody.CurrentMessage  = new  ACL_Message( ACLPerformative.Requste  , Mybody,Mybody.assignedIncident.ALCcommander  ,CMD1, Mybody.CurrentTick ,Mybody.assignedIncident.ComMechanism_level_StoB,1 ,TypeMesg.Inernal) ;
			Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);

			//updateplane
			UpdatePlan( UpdatePlanType.UpdateTask_sendtoALC, CurrentCheckedTask.TargetCasualty , Ambulance_TaskType.TreatmentandTransfertoVehicle , null,null ,null,-1,false) ;	

			Mybody.Action=RespondersActions.RequestAmbulance; //coordinate
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Comunication ;	

			//System.out.println("  "+"CCS: "+ Mybody.Id  +" sendtoALC" + Mybody.assignedIncident.ALCcommander.Id  + "  " + CurrentCheckedTask.TargetCasualty.ID +CurrentCheckedTask.TargetCasualty.ID ); 
		}
		// ++++++ 36- +++++++
		else if (Mybody.Action==RespondersActions.RequestAmbulance && Mybody. Acknowledged )
		{
			Mybody.Action=RespondersActions.Noaction;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.WaitingDuring;
			Mybody.Acknowledged=false;Mybody.Sending=false; 
			PreviousRequestAcepted= false;
		}
		//-------------------------------------------------------------------------------------------- (P2)  
		else  if ( Mybody.Action==RespondersActions.Noaction  && IsthereReporting_TriageReport( )  &&  Mybody.assignedIncident.CasualtyReport_Mechanism_IN==CasualtyReportandTrackingMechanism.Paper  )
		{

			this.Reporting_plane_Report_triage(ACLPerformative.InformCCSReport);

			Mybody.Action=RespondersActions.InfromOPReport;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Comunication;  


		}
		//-----------------------------------------------End --------------------------------------------- (P8) 
		// ++++++ 37- +++++++	
		else if  (  Mybody.Action==RespondersActions.Noaction &&  End_CCS==2 )
		{

			End_nomorecinScene=2;
			End_CCS=3;
			System.out.println("  "+"CCS: "+ Mybody.Id  +" done CCS*****************1*****************" +  IsAllTaskclosed() ); 

			BoradcastAction(ACLPerformative.InformCCSOprationsEND);

			Mybody.Action=RespondersActions.BoradcastEndScene ;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Comunication ; 
			Mybody.EndofCurrentAction=   InputFile.Boradcast_duration ;

			//for(ISRecord     Rec: Mybody.assignedIncident.ISSystem) 
			//	System.out.println( Rec.CasualtyinRec.ID +" -------- "  + Rec.AssignedParamdicinRec.Id  +"  "  + Rec.AssignedHospitalinRec +"  "  +  Rec.Triaged+ "  "  +  Rec.Treated+"  "  +Rec.checkedSC1);
		}
		// ++++++ 38- +++++++
		else if (Mybody.Action==RespondersActions.BoradcastEndScene && Mybody.EndActionTrigger== RespondersTriggers.EndingAction  )
		{				
			Mybody.InterpretedTrigger=RespondersTriggers.DoneActivity;
			Mybody.EndActionTrigger= null;
			Mybody.Sending=false; 

			System.out.println("  "+"CCS: "+ Mybody.Id  +" done CCS*****************2*****************" ); 
		}
		// ++++++ 38- +++++++
		else if (Mybody.Action==RespondersActions.BoradcastEndScene && Mybody.EndActionTrigger== RespondersTriggers.EndingAction  )
		{				
			Mybody.InterpretedTrigger=RespondersTriggers.DoneActivity;
		}

		if (  Mybody.zzz== true)
		{
			System.out.println("  "+"CCS: "+ Mybody.Id  +" -----------------------------------------------------------" ); 
			for (Task_ambulance T : Operational_Plan) 
				System.out.println(     "  " + T.TargetCasualty.ID +" -------- "  + T.TaskStatus +"   " + T.TaskType    +"    " + T.priority_level  +"    " +    T.AssignedHospital +"    "  + T.AssignedAmbulance +"    "  + T.AssignedResponder);


			System.out.println("  "+"CCS: "+ Mybody.Id  +" -----------------------------------------------------------" ); 
			for(ISRecord     Rec: Mybody.assignedIncident.ISSystem) 
				System.out.println( Rec.CasualtyinRec.ID +" -------- " + Rec.priority_levelinRec +"   " + Rec.AssignedAmbulanceinRec  +"  "  + Rec.AssignedHospitalinRec +"  "  +  Rec.SecondTriaged+ "  "  +  Rec.Treated+ "     "   + Rec.Pre_treatment 
						+"   DFT:" +Rec.checkedSC_DoneFieldTriage+"   DST:"  +Rec.checkedCCO_DoneSecondTriage +"   AIC_DST:"  +Rec.checkedAIC_DoneSecondTriage+" DT: "+ Rec.checkedCCO_DoneTreatment  +"  ALC:"  +Rec.checkedALC_AllocateAmb  + Rec.checkedCCO_DoneHosandAmb  );

			Mybody.zzz=false;
		}

	}

	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	// CCO -  CoordinateEndResponse  
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
			BoradcastAction(ACLPerformative.InformERend);

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
			System.out.println( "  "+Mybody.Id  +" GO back to Vehicle  " +Mybody.Role  + " " + this.MyResponders.size());

		}
	}

}//end class


//public void Reporting_plane_CCS(ACLPerformative xx , Ambulance_TaskType  Typeeee  ) {
//
//
//	int TotalCasualties=0 ,TotalCasualties_waiting=0 ,TotalCasualties_inprogress=0 , TotalCasualties_done=0 ;
//	int TotalResponders=0 ,RespondersFree=0 ,RepondersBusy=0;
//
//	TotalResponders=MyResponders .size() ;
//	RespondersFree=UnoccupiedResponders .size();
//	RepondersBusy=MyResponders .size()- UnoccupiedResponders .size();
//
//	//----------------------
//	TotalCasualties= 0;
//	TotalCasualties_waiting=0 ;
//	TotalCasualties_inprogress=0 ;
//	TotalCasualties_done=0 ;
//
//
//	for (Task_ambulance Task : Operational_Plan) {
//
//		if (  Task.TaskType == Typeeee   &&  Task.TaskStatus == D_Ontology.Ontology.GeneralTaskStatus.Done) {
//
//			TotalCasualties_done ++ ;
//		}
//		if (  Task.TaskType == Typeeee   &&  Task.TaskStatus == D_Ontology.Ontology.GeneralTaskStatus.Waiting) {
//
//			TotalCasualties_waiting ++;
//		}
//		if (  Task.TaskType == Typeeee   && Task.TaskStatus == D_Ontology.Ontology.GeneralTaskStatus.Inprogress) {
//
//			TotalCasualties_inprogress++; 
//		}
//
//	}
//
//	TotalCasualties= TotalCasualties_waiting +TotalCasualties_inprogress + TotalCasualties_done ;
//
//	// create Report that contains all details to update the Silver commander about current Operational_Plan .
//	Report Report1 =new Report();
//	//Report1.Ambulance_ReportCasualties_Bronze( TotalResponders,RespondersFree ,RepondersBusy , TotalCasualties ,TotalCasualties_waiting ,TotalCasualties_inprogress , TotalCasualties_done ,Typeeee  );
//
//	// send message
//	Mybody.CurrentMessage = new  ACL_Message( xx ,Mybody ,Mybody.assignedIncident.AICcommander,Report1, Mybody.CurrentTick ,InputFile.ComMechanism_level_TtoO) ;
//
//};









//case SmallestDistance:
////Smallest distance
//double smallest_dis=999999 ; ; 
//
//for (Task_ambulance Task : Operational_Plan)
//	if (Task.TaskStatus == Ontology.GeneralTaskStatus.Waiting && Task.TaskType== Tasktypeofinterst )
//	{
//		Resp=Task.ckeck_Distance_befor_TaskAssignment(UnoccupiedResponders);
//		double dis = BuildStaticFuction.DistanceC(Task.TargetCasualty.geography, Resp.Return_CurrentLocation(),Task.TargetCasualty.getCurrentLocation());
//
//		if (dis<smallest_dis)
//		{ smallest_dis= dis; nominatedResp=Resp ;  nominatedTask=Task;}
//	}
//
//break;

//case Severity_RPMPriorty:
//// identify more priority task-casualty- to assign nearest free responder
//double More_priorityRPM=13 ; 
//for (Task_ambulance Task : Operational_Plan)
//if (Task.TaskStatus == D_Ontology.Ontology.GeneralTaskStatus.Waiting && Task.TaskType== Tasktypeofinterst )
//{
//	if (Task.TargetCasualty.CurrentRPM < More_priorityRPM)
//	{ More_priorityRPM=Task.TargetCasualty.CurrentRPM  ;  nominatedTask=Task;}
//
//	nominatedResp=nominatedTask.ckeck_Distance_befor_TaskAssignment(UnoccupiedResponders);
//}
//
//break;}	
























////-------------------------------------------------------------------------------------------------
//		// ++++++ 22- +++++++
//		else  if ( Mybody.Action==RespondersActions.Noaction  && 
//				(		
//						(  Mybody.assignedIncident.Communication_Structure_uesd==Inter_Communication_Structure.Struc1_SilverCommandersInteraction  && ( ( ISthereupdateTosend( ) &&  Mybody.assignedIncident.Intra_Communication_Time_used==Communication_Time.When_need)   )) 
//						||
//						(  Mybody.assignedIncident.Communication_Structure_uesd==Inter_Communication_Structure.Struc2_BronzeCommandersInteraction  && (  ( Mybody.CurrentTick >= (Time_last_updated +  Mybody.assignedIncident.UpdatOPEvery) &&  Mybody.assignedIncident.Inter_Communication_Time_used==Communication_Time.Every_frequently )  ||  ( ISthereupdateTosend( ) &&  Mybody.assignedIncident.Inter_Communication_Time_used==Communication_Time.When_need)   )) 
//						)				
//				)
//
//		{
//			//updates to silver commander  ( CCS established ,Triage report, Dead casualties, casu information !! )												
//			//--------------
//			if (  ISthereupdateTosend( )  )
//			{ 
//				if ( EndCCSEstablished==1  )
//				{ Reporting_plane1(ACLPerformative.InfromCCSEstablished) ; EndCCSEstablished= 2;  }	
//
//
//				if  ( Mybody.assignedIncident.CasualtyReport_Mechanism_IN==CasualtyReportandTrackingMechanism.Paper   )	
//				{   
//					//Mybody.zzz=true;
//
//					if (	Mybody.assignedIncident.Communication_Structure_uesd==Inter_Communication_Structure.Struc1_SilverCommandersInteraction)
//					{
//						if ( IsthereReporting_TriageReport( )    )
//							this.Reporting_plane_Report_triage(ACLPerformative.InformCCSReport);
//						else if  (  IsthereReporting_DecasedReport( ) || IsthereReporting_casToLogInfoReport( )  )
//							this.Reporting_plane_Report_Deandinfo(ACLPerformative.InformCCSReport)  ;
//					}
//					else
//					{
//						if ( IsthereReporting_TriageReport( )    )
//							this.Reporting_plane_Report_triage(ACLPerformative.InformCCSReport);
//						if  (  IsthereReporting_DecasedReport( ) || IsthereReporting_casToLogInfoReport( )  )
//							this.Reporting_plane_Report_Deandinfo(ACLPerformative.InformCCSReport)  ;
//					}
//
//
//				}
//				else if  ( Mybody.assignedIncident.CasualtyReport_Mechanism_IN==CasualtyReportandTrackingMechanism.Electronic )
//				{
//					if  (  IsthereReporting_DecasedReport( ) || IsthereReporting_casToLogInfoReport( )  )
//						this.Reporting_plane_Report_Deandinfo(ACLPerformative.InformCCSReport)  ;
//				}
//
//				if ( End_CCS==1 )
//				{ Reporting_plane1(ACLPerformative.InformNomorecasualty_CCS  );   End_CCS=2  ;} 
//
//
//				Mybody.Action=RespondersActions.InfromOPReport;
//				Mybody.BehaviourType1=RespondersBehaviourTypes1.Comunication;  
//
//
//				System.out.println("  "+"CCS: "+ Mybody.Id  +"  InfromOPReport  " + Mybody.CurrentMessage_list.size()   );
//				for ( ACL_Message  A:Mybody.CurrentMessage_list )
//					System.out.println("  "+"CCS: "+ Mybody.Id  +"  InfromOPReport  receiver:" + ( (Responder )A.receiver).Id  +"  " + A.performative   +"  sending " + ( (Responder )A.receiver).Sending  ); //+ "    to"+  ( (Responder )A.receiver).MyReceiver  + "in "  + ( (Responder )A.receiver).Mytimetosend);
//			}
//			else
//			{
//				Mybody.Action=RespondersActions.UpdatePlan;
//				Mybody.BehaviourType1=RespondersBehaviourTypes1.Planformulation ; 
//				Mybody.EndofCurrentAction=  InputFile.UdatePlan_duration  ; 
//				Time_last_updated=Mybody.CurrentTick;
//
//			}
//		}
