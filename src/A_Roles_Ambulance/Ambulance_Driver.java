
package A_Roles_Ambulance;

import java.util.ArrayList;
import A_Agents.Casualty;
import A_Agents.Hospital;
import A_Agents.Responder;
import A_Agents.Responder_Ambulance;
import A_Agents.Vehicle_Ambulance;
import A_Environment.RoadLink;
import A_Environment.RoadNode;
import A_Environment.TopographicArea;
import B_Communication.ACL_Message;
import B_Communication.Command;
import B_Communication.ISRecord;
import B_Communication.Report;
import C_SimulationInput.InputFile;
import D_Ontology.Ontology.ACLPerformative;
import D_Ontology.Ontology.Ambulance_ActivityType;
import D_Ontology.Ontology.Ambulance_ResponderRole;
import D_Ontology.Ontology.Ambulance_TaskType;
import D_Ontology.Ontology.CasualtyAction;
import D_Ontology.Ontology.CasualtyReportandTrackingMechanism;
import D_Ontology.Ontology.CasualtyStatus;
import D_Ontology.Ontology.RespondersActions;
import D_Ontology.Ontology.RespondersBehaviourTypes1;
import D_Ontology.Ontology.RespondersTriggers;
import D_Ontology.Ontology.TaskAllocationApproach;
import D_Ontology.Ontology.TypeMesg;
import D_Ontology.Ontology.VehicleTriggers;

public class  Ambulance_Driver {

	Responder_Ambulance Mybody;   
	boolean ToAIC=false;	

	public Hospital AssignedhospitaltoCasualty=null ;
	Casualty  CurrentCasualtybySender=null;
	
	boolean IgetNewActivity =false ;
	//------------------------------------------------------------
	public boolean IsThereRoutes=false ;
	boolean Iaminbark=false;
	
	boolean FTXXXX1=true;
	int  FTXXXX2=500 ;
	
	// New to send to ALC
	ArrayList<RoadLink> routeNEEDClearwreckage_List =new ArrayList<RoadLink>();
	ArrayList<RoadLink> routeNEEDCleartraffic_List =new ArrayList<RoadLink>();

	// My memory 
	ArrayList<RoadLink> Mymemory_routeNEEDClearwreckage_List =new ArrayList<RoadLink>();
	ArrayList<RoadLink> Mymemory_routeNEEDCleartraffic_List =new ArrayList<RoadLink>();

	//##############################################################################################################################################################
	public  Ambulance_Driver ( Responder_Ambulance _Mybody) {

		Mybody=_Mybody;
		Mybody.EndofCurrentAction=0;
		Mybody.CurrentAssignedMajorActivity_amb= null ;
		Mybody.PrvRoleinprint1 =Mybody.Role;
	}

	//##############################################################################################################################################################
	public void Driver_InterpretationMessage()
	{
		boolean  done= true;
		ACL_Message currentmsg =  Mybody.Message_inbox.get(Mybody.Lastmessagereaded);		 			
		Mybody.CurrentSender= (Responder)currentmsg.sender;		
		Mybody.Lastmessagereaded++;
		
		Mybody.SendingReciving_External= false ; Mybody.SendingReciving_internal=false; 
		if (  currentmsg.Inernal) Mybody.SendingReciving_internal= true ;
		else if (  currentmsg.External) Mybody.SendingReciving_External=true ;

		//System.out.println("Driver: " + Mybody.Id + "  " + Mybody.CommTrigger + "  from  "+ Mybody.CurrentSender.Id  + "    Action:" + Mybody.Action);
		switch( currentmsg.performative) {
		case Command :
			Mybody.CurrentCommandRequest=((Command)currentmsg.content);

			//++++++++++++++++++++++++++++++++++++++ Form AIC
			if ( Mybody.CurrentSender instanceof Responder_Ambulance &&  Mybody.CurrentCommandRequest.commandActivityType1 == Ambulance_ActivityType.loadingandTnasporation  )  
			{				
				Mybody.CommTrigger= RespondersTriggers.GetActivityTransportation;						
				Mybody.AssignedTA=Mybody.CurrentCommandRequest.TargetTA ;

				IgetNewActivity= true ;

			}
			//++++++++++++++++++++++++++++++++++++++
			if ( Mybody.CurrentSender instanceof Responder_Ambulance &&  Mybody.CurrentCommandRequest.commandType1== Ambulance_TaskType.TransferCasualtytoHospital  )
			{
				AssignedhospitaltoCasualty =Mybody.CurrentCommandRequest.TargetHospital ;
				CurrentCasualtybySender=Mybody.CurrentCommandRequest.TargetCasualty ;
				Mybody.CommTrigger= RespondersTriggers.GetCommandTransferToHospital ;
				Mybody.OnhandCasualty=CurrentCasualtybySender;  //temp

			}
			//++++++++++++++++++++++++++++++++++++++  From BZC
			if ( Mybody.CurrentSender instanceof Responder_Ambulance &&  Mybody.CurrentCommandRequest.commandType1 ==  Ambulance_TaskType.SetupTacticalAreas   )  
			{				
				Mybody.CommTrigger= RespondersTriggers.GetcommandSetupTacticalAreas  ;
				Mybody.AssignedTA=Mybody.CurrentCommandRequest.TargetTA;
			}
			break;
		case	InfromCordonEstablished: //PEA
			;
			break;
		case InformERend :
			Mybody.CommTrigger= RespondersTriggers.ENDER;
			break;	
		} // end switch

	}

	//##############################################################################################################################################################	
	//													Actions 
	//##############################################################################################################################################################	
	public void FirstArrivalAction1() {
		//First time
		//Mybody.assignedIncident.IdentifyloadingArea( ) ;
		//Mybody.assignedIncident.IdentifyControlArea( Mybody.assignedIncident.Node.getCurrentPosition()) ;
	}

	//----------------------------------------------------------------------------------------------------
	public void TansportCasualtyToHActionS()  //onLeaveAction
	{
		for( Casualty  ca : ((Vehicle_Ambulance )Mybody.Myvehicle).casualtiesinRoom )	
			ca.UnderAction=CasualtyAction.Travallingtohospital;		
	}

	//----------------------------------------------------------------------------------------------------
	public void DowenloadonHActionS() 
	{
		for( Casualty  ca : ((Vehicle_Ambulance )Mybody.Myvehicle).casualtiesinRoom )	
			ca.UnderAction=CasualtyAction.DownloadfromAmbulance;	
	}

	//----------------------------------------------------------------------------------------------------
	public void DowenloadonHActionE() 
	{
		for( Casualty  ca : ((Vehicle_Ambulance )Mybody.Myvehicle).casualtiesinRoom ) //temp it should be in hospital
		{
					
			AssignedhospitaltoCasualty.Dropping_off_Casualty( ca );	
			
		}
		
		Mybody.OnhandCasualty=null ;//temp
	}

	//----------------------------------------------------------------------------------------------------
	public void InformResult(ACLPerformative xx   , Responder_Ambulance SendTO  ){	

		if (xx ==ACLPerformative.InformResultHTransfer )
		{
			if ( Mybody.assignedIncident.CasualtyReport_Mechanism_IN== CasualtyReportandTrackingMechanism.Paper )
			{
				Casualty ca=((Vehicle_Ambulance )Mybody.Myvehicle).casualtiesinRoom.get(0) ; //  or CurrentCasualtybySender
				Mybody.CurrentMessage  = new  ACL_Message( ACLPerformative.InformResultHTransfer,Mybody,SendTO ,ca , Mybody.CurrentTick, Mybody.assignedIncident.ComMechanism_level_DrivertoALC,1 ,TypeMesg.Inernal) ;
				Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
			}
			else if ( Mybody.assignedIncident.CasualtyReport_Mechanism_IN == CasualtyReportandTrackingMechanism.Electronic )
			{	

				Casualty ca=((Vehicle_Ambulance )Mybody.Myvehicle).casualtiesinRoom.get(0) ;
				ISRecord currentISRecord = Mybody.assignedIncident.ReturnRecordcasualtyISSystem( ca );					
				currentISRecord.UpdateISRecord_HTransfer(Mybody ) ;		

				Mybody. Acknowledged=true; /////?????????
			}
		}		
		else
		{
			Mybody.CurrentMessage  = new  ACL_Message( xx ,Mybody,SendTO ,null , Mybody.CurrentTick, Mybody.assignedIncident.ComMechanism_level_DrivertoALC,1 ,TypeMesg.Inernal) ;
			Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
		}
		
		Mybody.SendingReciving_External= false ;  Mybody.SendingReciving_internal= true ;

	}

	//----------------------------------------------------------------------------------------------------
	private boolean Check_AllonVehicle()
	{
		boolean AllonVehicle= true; 

		for ( Responder_Ambulance Resp : ((Vehicle_Ambulance )Mybody.Myvehicle).Responders_list)
			if ( Resp!=Mybody & Resp.Action!=RespondersActions.OnVehicle)
			{ AllonVehicle= false;}

		return AllonVehicle;
	}

	//----------------------------------------------------------------------------------------------------
	public void IdentifyWTR_start()
	{

		Mybody.Myvehicle.RouteW_list.clear();
		Mybody.Myvehicle.RouteTr_list.clear();
	}

	public void IdentifyWTR_end()
	{

		ArrayList<RoadLink> Tempwreckage_List =Mybody.Myvehicle.RouteW_list ;
		ArrayList<RoadLink> Temptraffic_List=Mybody.Myvehicle.RouteTr_list ;

		for(RoadLink  RL :  Tempwreckage_List)
			if ( ! Mymemory_routeNEEDClearwreckage_List.contains(RL)  )
			{
				routeNEEDClearwreckage_List.add(RL);
				Mymemory_routeNEEDClearwreckage_List.add(RL);
				RL.ColorCode=2;
//				if (RL.Parts.size() !=0 )
//					for ( TopographicArea  P : RL.Parts )
//						P.ColorCode=7;
			}

		for(RoadLink  RL :  Temptraffic_List)
			if ( ! 	Mymemory_routeNEEDCleartraffic_List.contains(RL)  )
			{
				routeNEEDCleartraffic_List.add(RL);
				Mymemory_routeNEEDCleartraffic_List.add(RL);
				RL.ColorCode=2;
//				if (RL.Parts.size() !=0 )
//					for ( TopographicArea  P : RL.Parts )
//						P.ColorCode=7;
			}

		if ( routeNEEDClearwreckage_List.size()>0 || routeNEEDCleartraffic_List.size() >0 )
			IsThereRoutes= true ;
	}

	//----------------------------------------------------------------------------------------------------
	public boolean Check_FirstArrivalVehicle (  ) 
	{
		if (Mybody.Myvehicle.FirstArrivalofVehicleAll)				 
			return true;
		else
			return false;
	}

	//______________________________________________________________________________________________________________________________________________________________
	//##############################################################################################################################################################
	//                                                        Behavior
	//##############################################################################################################################################################
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	// Driver- General  
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	public void DriverBehavior()
	{	
		//********************* for Get Activity
		if ( IgetNewActivity &&  Mybody.CommTrigger==RespondersTriggers.GetActivityTransportation  )													
		{																		
			Mybody.Action=RespondersActions.GetAssignedTask;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Comunication; //RoleAssignment;	
			Mybody.EndofCurrentAction=  InputFile.GetCommand_duration  ;

			IgetNewActivity= false;
		}	
		// ++++++ 2- +++++++
		else if (  Mybody.Action==RespondersActions.GetAssignedTask &&  Mybody.EndActionTrigger== RespondersTriggers.EndingAction) 												
		{						
			Mybody.CurrentSender.Acknowledg(Mybody) ;	
			Mybody.CurrentAssignedMajorActivity_amb=Ambulance_ActivityType.loadingandTnasporation ;	
			Mybody.EndActionTrigger=null ;

		}

		//*************************************************************************
		// ** Go to 
		// 1- Driver to scene
		if ( Mybody.CurrentAssignedMajorActivity_amb== null)
		{
			if ( 	Mybody.ActionEffectTrigger == RespondersTriggers.Alarm999  )
			{			
				Mybody.CurrentAssignedActivity=Ambulance_TaskType.TravaltoIncident ;		
			}
			// 2- FirstArrival
			else if ( Mybody.SensingeEnvironmentTrigger== RespondersTriggers.FirstArrival_NoResponder   )
			{
				FirstArrivalAction1() ; //as assumption
				Mybody.Role=Ambulance_ResponderRole.AmbulanceCommunicationsOfficer;
				Mybody.CurrentCalssRole6= new Ambulance_Commander_ACO (Mybody);
				System.out.println(Mybody.Id +" ..... " +Mybody.Role);
			}

			// 3- control area
			else if (Mybody.SensingeEnvironmentTrigger== RespondersTriggers.NotFirstArrival_ThereisResponder  ) 
			{
				Mybody.CurrentAssignedActivity=Ambulance_TaskType.InitialResponse ;	

				//
				//if ( Mybody.CommTrigger==RespondersTriggers.CORolehanded ) { Mybody.CommTrigger=null;Mybody.FiretTimeOnScence=false;	}  //System.out.println("driver"+ Mybody.Id +"  handing " );
			}

		}

		//*************************************************************************
		// ** Trasportation
		else if ( Mybody.CurrentAssignedMajorActivity_amb==Ambulance_ActivityType.loadingandTnasporation )		
		{ 	
			// 1- Go to Location
			if(   Mybody.CommTrigger==RespondersTriggers.GetActivityTransportation   )			
			{
				Mybody.CurrentAssignedActivity=Ambulance_TaskType.GoTolocation;
			}
			// 2-No Action 
			else if ( Mybody.InterpretedTrigger == RespondersTriggers.FirstTimeonLocation || Mybody.InterpretedTrigger== RespondersTriggers.DoneTask ||Mybody.InterpretedTrigger== RespondersTriggers.ComeBack  ) 
			{		
				
				Mybody.CurrentAssignedActivity=Ambulance_TaskType.None;
				Mybody.Action=RespondersActions.Noaction;
				Mybody.BehaviourType1=RespondersBehaviourTypes1.WaitingDuring;

				Mybody.InterpretedTrigger=null;
				
			}
			// 3-Setup
			else if (   Mybody.CommTrigger==RespondersTriggers.GetcommandSetupTacticalAreas 	) 
			{
				Mybody.CurrentAssignedActivity=Ambulance_TaskType.SetupTacticalAreas  ;
			}
			// 4- TransferCasualtytoHospital 
			else if (Mybody.CommTrigger== RespondersTriggers.GetCommandTransferToHospital  && this.Iaminbark==true ) 
			{
				Mybody.CurrentAssignedActivity=Ambulance_TaskType.TransferCasualtytoHospital ;					
			}
		
			
			
			
			
			//=======================
			if ( Mybody.CurrentAssignedActivity==Ambulance_TaskType.None  && Mybody.AssignedTA.installed && Iaminbark==false   )
			{
				if ( Mybody.AssignedTA.AssigenAmbtoTA( (Vehicle_Ambulance) Mybody.Myvehicle)  )
				{		
					Iaminbark=true;
					//System.out.println("Driver: " + Mybody.Id + "  in bark   in ***************************************************,,,,,,, " + ( (Vehicle_Ambulance) Mybody.Myvehicle).bark.id );

				}
			}
			
	
			//-------------------------------------------------------------------------------------------- ENDER
			// 5- Driver to Base
			//++++++ 1- +++++++
			else  if (Mybody.CurrentAssignedActivity==Ambulance_TaskType.None && Mybody.CommTrigger==RespondersTriggers.ENDER ) 
			{
				Mybody.Action=RespondersActions.GetNotificationEndER ;
				Mybody.BehaviourType1=RespondersBehaviourTypes1.Comunication;                 
				Mybody.EndofCurrentAction= InputFile.GetInfromation_FtoForR_Duration_Notification ;
				Mybody.CommTrigger=null; 
			}
			// ++++++ 2- +++++++
			else if (Mybody.Action==RespondersActions.GetNotificationEndER  && Mybody.EndActionTrigger== RespondersTriggers.EndingAction   )
			{
				Mybody.InterpretedTrigger=RespondersTriggers.ENDER; //make sure no still task

				Mybody.Action=RespondersActions.Noaction;
				Mybody.BehaviourType1=RespondersBehaviourTypes1.WaitingEnd ;
				Mybody.EndActionTrigger=null;
			}
			else if ( 	Mybody.InterpretedTrigger == RespondersTriggers.ENDER  )
			{			
				Mybody.CurrentAssignedMajorActivity_amb=null;
				Mybody.CurrentAssignedActivity=Ambulance_TaskType.TravaltoBaseStation ;

			}
		}
		//*************************************************************************
		switch(Mybody.CurrentAssignedActivity) {
		case TravaltoIncident :
			DriverBehavior_GotoIncident()	;	
			break;
		case InitialResponse :
			DriverBehavior_InitialResponse(); 
			break;			
		case GoTolocation :
			DriverBehavior_GoTolocation(); 
			break;		
		case SetupTacticalAreas :
			DriverBehavior_SetupTacticalAreas() ; 
			break;
		case TransferCasualtytoHospital :
			DriverBehavior_TransferCasualtytoHospital();
			break;
		case TravaltoBaseStation :
			DriverBehavior_GotoBaseStation()	;
			break;
		case None:
			;
			break;}
	}

	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	// Driver-  TravaltoIncident
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^	
	public void DriverBehavior_GotoIncident()	
	{		
		// ++++++ 1- +++++++
		if ( 	Mybody.ActionEffectTrigger == RespondersTriggers.Alarm999  ) 
		{			
			RoadNode  Node1 = Mybody.assignedIncident.ParkingNodesforIncdent() ;			
			Mybody.Myvehicle.AssignDestination2(  Node1,false ,false);	 //	Mybody.Myvehicle.AssignDestination2( Mybody.assignedIncident.Node,false);
			((Vehicle_Ambulance) Mybody.Myvehicle).ActionEffectTrigger = VehicleTriggers.InsertKey_incident;
			Mybody.ActionEffectTrigger=null;
		}
		// ++++++ 2- +++++++
		else if (  Mybody.SensingeEnvironmentTrigger==RespondersTriggers.Engineisrunning  && ! Mybody.OnScence )
		{		
			Mybody.Action=RespondersActions.DrivingtoIncident ;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Travlling;
			Mybody.SensingeEnvironmentTrigger=null;	

			Mybody. leaveStation =Mybody.CurrentTick;
		}
		// ++++++ 3- +++++++
		else if (Mybody.Action==RespondersActions.DrivingtoIncident  && Mybody.SensingeEnvironmentTrigger == RespondersTriggers.ArrivedIncident  && Check_FirstArrivalVehicle (  )  )
		{
			Mybody.OnScence=true;
			Mybody.ArrivedScene=Mybody.CurrentTick;

			((Vehicle_Ambulance) Mybody.Myvehicle).Swich_on_bluelightflashing(  ) ;

			Mybody.Check_FirstArrival(); //its agency only					 
			//System.out.println(Mybody.Id + " Check_FirstArrival " + Mybody.SensingeEnvironmentTrigger);
		}
		// ++++++ 4- +++++++
		else if (Mybody.Action==RespondersActions.DrivingtoIncident  && Mybody.SensingeEnvironmentTrigger == RespondersTriggers.ArrivedIncident && ! Check_FirstArrivalVehicle (  )  )
		{
			Mybody.OnScence=true;
			Mybody.ArrivedScene=Mybody.CurrentTick;

			Mybody.Myvehicle.AssignDestination2(  Mybody.assignedIncident.bluelightflashing_Node,false ,false);	
			((Vehicle_Ambulance) Mybody.Myvehicle).ActionEffectTrigger = VehicleTriggers.InsertKey_BLF ;
			Mybody.SensingeEnvironmentTrigger=null;

		}
		// ++++++ 5- +++++++
		else if (  Mybody.SensingeEnvironmentTrigger==RespondersTriggers.Engineisrunning  &&  Mybody.OnScence )
		{		
			Mybody.Action=RespondersActions.DrivingtoIncident ;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Travlling;		
		}
		// ++++++ 6- +++++++
		else if (Mybody.Action==RespondersActions.DrivingtoIncident  && Mybody.SensingeEnvironmentTrigger == RespondersTriggers.Arrivedbluelightflashing    )
		{
			Mybody.Check_FirstArrival();
			
			if (  Mybody.assignedIncident.Time_endResponde_FirstvehiclearrivedIncd==0 )
				 Mybody.assignedIncident.Time_endResponde_FirstvehiclearrivedIncd= Mybody.CurrentTick ; //its will be update by all until fist one
			
			//System.out.println(Mybody.Id + " Check_FirstArrival " + Mybody.SensingeEnvironmentTrigger);
		}
	}

	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	// Driver-  TravaltoBaseStation
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^	
	public void DriverBehavior_GotoBaseStation()	
	{		
		// ++++++ 1- +++++++
		if (Mybody.InterpretedTrigger == RespondersTriggers.ENDER    ) {


			Mybody.Action=RespondersActions.DrivingtoBaseStation;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Travlling;

			
			if (Check_AllonVehicle()  )
			{
				Mybody.Myvehicle.AssignDestination2(Mybody.assignedIncident.Cordon_outer.EnteryPointAccess_node,false,true);
				Mybody.Myvehicle.ActionEffectTrigger = VehicleTriggers.InsertKey_IncidentLeave;
				Mybody.InterpretedTrigger=null;  
				
				Mybody.assignedIncident.Time_endResponde_lastvehicleLeaveIncd= Mybody.CurrentTick ; //its will be update by all until last one
				
			}
			
//			if (FTXXXX1 && FTXXXX2==0  && Mybody.InterpretedTrigger !=null)  //after time check
//			{ FTXXXX1=false ;
//			
//						for( Responder_Ambulance Resp : ((Vehicle_Ambulance) Mybody.Myvehicle).Responders_list ) 
//						{
//							System.out.println(" erro  not invichel &&&&&&&&&&&&&&&&&&&&&&   " + Resp.Role + Resp.CurrentAssignedMajorActivity +"    "  + Resp.CurrentAssignedActivity );
//						System.out.println(Resp.Id  +  "  Action: " + Resp.Action + " com:" + Resp.CommTrigger + "   Nt:" + Resp.InterpretedTrigger+ " en:" +Resp.SensingeEnvironmentTrigger + " end:" + Resp.EndActionTrigger +"   " + Resp.Acknowledged   );
//						}
//			}
//			else
//				FTXXXX2--;
			
					
		}
		// ++++++ 2- +++++++
		else if (  Mybody.Action==RespondersActions.DrivingtoBaseStation && Mybody.SensingeEnvironmentTrigger==RespondersTriggers.ArrivedPEA  ) 	
		{			
			Mybody.Myvehicle.AssignDestination2( ((Vehicle_Ambulance) Mybody.Myvehicle).AssignedStation.Node,false,false); 

			((Vehicle_Ambulance) Mybody.Myvehicle).ActionEffectTrigger = VehicleTriggers.InsertKey_Station  ;
			Mybody.SensingeEnvironmentTrigger=null;		


		}
		// ++++++ 3- +++++++
		else if (Mybody.Action==RespondersActions.DrivingtoBaseStation && Mybody.SensingeEnvironmentTrigger == RespondersTriggers.ArrivedBaseStation  )
		{
			Mybody.Action=RespondersActions.Idle;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Done;
			Mybody.SensingeEnvironmentTrigger=null;	
			Mybody. Busy=false;
			System.out.println("Amb Driver" + Mybody.Id + "   ......bey bey "  );
			Mybody.BacktoStation =Mybody.CurrentTick;
			Mybody.assignedIncident.Time_endResponde_lastvehiclearrivedBS= Mybody.CurrentTick ; //its will be update by all until last one
						
		}
	}

	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	// Driver- InitialResponse
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^	
	public void DriverBehavior_InitialResponse()
	{		
		//-------------------------------------------------------------------------------------------- inform AIC
		// ++++++ 1- +++++++
		if ( Mybody.SensingeEnvironmentTrigger== RespondersTriggers.NotFirstArrival_ThereisResponder ||

				(  Mybody.FiretTimeOnScence && ( Mybody.Action==RespondersActions.DrivingtoIncident ||Mybody.Action==RespondersActions.Noaction) && Mybody.Check_CommnderIsinContralArea()   ))			
		{ 			

			if ( Mybody.Check_CommnderIsinContralArea()  )
			{			
				//send message to AIC  
				Mybody.CurrentMessage  = new  ACL_Message( ACLPerformative.InformNewResponderArrival , Mybody, Mybody.assignedIncident.AICcommander ,null  ,  Mybody.CurrentTick,Mybody.assignedIncident.ComMechanism_level_DrivertoALC,1,TypeMesg.Inernal) ;
				Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
				
				//==================
				Mybody.Action=RespondersActions.NotifyArrival;
				Mybody.BehaviourType1=RespondersBehaviourTypes1.Comunication; //RoleAssignment;
				Mybody.SensingeEnvironmentTrigger=null;	
				Mybody.FiretTimeOnScence=false;
				//System.out.println("driver"+ Mybody.Id + " NotifyArrival " );
			}
			else
			{
				//waiting AIC
				Mybody.Action=RespondersActions.Noaction;
				Mybody.BehaviourType1=RespondersBehaviourTypes1.RoleAssignment;
				Mybody.SensingeEnvironmentTrigger=null;
				//	System.out.println("driver"+ Mybody.Id +"  Waiting  No NotifyArrival " );
			}
		}

		// ++++++ 2- +++++++
		else if (Mybody.Action==RespondersActions.NotifyArrival && Mybody.Acknowledged )
		{			
			Mybody.Action=RespondersActions.Noaction;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.WaitingDuring ;
			Mybody.Acknowledged=false;Mybody.Sending=false; 
			//System.out.println("Driver: " + Mybody.Id + " ready 1" );

		} 
	}

	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	// Driver-  GoTolocation LA
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^		
	private void DriverBehavior_GoTolocation()
	{	
		// ++++++ 1- +++++++
		if(  Mybody.CommTrigger==RespondersTriggers.GetActivityTransportation   )
		{

			if ( Mybody.assignedIncident.Cordon_outer.installed== true )
				Mybody.Myvehicle.AssignDestination2(  Mybody.AssignedTA.LoadingNode,false , true);
			else
				Mybody.Myvehicle.AssignDestination2(  Mybody.AssignedTA.LoadingNode,false , false);	
			((Vehicle_Ambulance) Mybody.Myvehicle).ActionEffectTrigger = VehicleTriggers.InsertKey_LAsetup ;

			Mybody.Action=RespondersActions.GoToTacticalArea;  
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Movementonincidentsite ;			
			Mybody.CommTrigger=null;
		}
		// ++++++ 2- +++++++
		else if (Mybody.Action==RespondersActions.GoToTacticalArea && Mybody.SensingeEnvironmentTrigger == RespondersTriggers.ArrivedLA   )
		{
			
					
			//send message to ALC  
			if ( Mybody.Check_CommnderIsinAL(  )   )
			{
				
				Mybody.Bronzecommander_amb=Mybody.assignedIncident.ALCcommander ;
				Mybody.CurrentMessage  = new  ACL_Message( ACLPerformative.InformNewResponderArrival , Mybody, Mybody.assignedIncident.ALCcommander,null  ,  Mybody.CurrentTick,Mybody.assignedIncident.ComMechanism_level_DrivertoALC,1,TypeMesg.Inernal) ;
				Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
			//==================
			Mybody.Action=RespondersActions.NotifyArrival;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Comunication;
			Mybody.SensingeEnvironmentTrigger=null;			
			//System.out.println("Driver: " + Mybody.Id + " ready 1" );
			}
		}

		// ++++++ 3- +++++++
		else if (Mybody.Action==RespondersActions.NotifyArrival && Mybody.Acknowledged )
		{			
					
			//System.out.println("Driver: " + Mybody.Id + "  Acknowledged ");
			
			if (  Mybody.AssignedTA.installed   )
			{
				if ( Mybody.AssignedTA.AssigenAmbtoTA( (Vehicle_Ambulance) Mybody.Myvehicle)  )
				{		
					Mybody.InterpretedTrigger= RespondersTriggers.FirstTimeonLocation;

					Mybody.Action=RespondersActions.Noaction;
					Mybody.BehaviourType1=RespondersBehaviourTypes1.WaitingDuring;
					Mybody.Acknowledged=false;Mybody.Sending=false; 
					//System.out.println("Driver: " + Mybody.Id + "  in bark " + ( (Vehicle_Ambulance) Mybody.Myvehicle).bark.id );
					
					Iaminbark=true;
				}
				else
				{
					
					System.out.println("Driver: " + Mybody.Id + "  there is  errrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrror in barking" );
				}
				
			}
			else
			{
				Mybody.InterpretedTrigger= RespondersTriggers.FirstTimeonLocation;

				Mybody.Action=RespondersActions.Noaction;
				Mybody.BehaviourType1=RespondersBehaviourTypes1.WaitingDuring;
				Mybody.Acknowledged=false;Mybody.Sending=false; 
			}

		}
	}

	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	// Driver-  Setup
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^		
	private void DriverBehavior_SetupTacticalAreas()
	{
		//-------------------------------------------------------------------------------------------- Setup
		// ++++++ 1- +++++++
		if ( Mybody.CommTrigger== RespondersTriggers.GetcommandSetupTacticalAreas)
		{
			Mybody.Action=RespondersActions.GetcommandSetupTacticalAreas ;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Comunication; //TaskPlanning;				
			Mybody.EndofCurrentAction=  InputFile.GetCommand_duration  ; 	
			Mybody.CommTrigger=null;	

			//System.out.println("  "+"Driver: " + Mybody.Id + "GetcommandSetupTacticalAreas");
		}	
		// ++++++ 2- +++++++
		else if (  Mybody.Action==RespondersActions.GetcommandSetupTacticalAreas &&  Mybody.EndActionTrigger== RespondersTriggers.EndingAction )													
		{		
			Mybody.CurrentSender.Acknowledg(Mybody);

			Mybody.AssignedTA.IworkinSetup(Mybody);
			Mybody.Action=RespondersActions.SetuploadingArea ;  
			Mybody.BehaviourType1=RespondersBehaviourTypes1.TaskExecution_SetupTA ;			
			Mybody.EndActionTrigger=null;

		}		
		//------------------------------------------LoadingArea ----------------------------------------
		// ++++++ 3- +++++++   
		else if ( Mybody.Action==RespondersActions.SetuploadingArea   && 	 Mybody.AssignedTA.installed ) 													
		{							
			if ( Mybody.AssignedTA.AssigenAmbtoTA( (Vehicle_Ambulance) Mybody.Myvehicle)  )
			{		
				InformResult(ACLPerformative.InfromResultSetupLoadingArea, (Responder_Ambulance) Mybody.AssignedTA.Bronzecommander );
				//System.out.println("  "+"Driver: " + Mybody.Id + " inform ");
				Mybody.Action=RespondersActions.InfromResultSetupLoadingArea ;
				Mybody.BehaviourType1=RespondersBehaviourTypes1.Comunication ;                   //Sharinginfo;
				Mybody.SensingeEnvironmentTrigger= null;
				this.Iaminbark=true;
				//System.out.println("Driver: " + Mybody.Id + " ater setup  in bark  " + ( (Vehicle_Ambulance) Mybody.Myvehicle).bark.id  );
			}
		}
		// ++++++ 4- +++++++
		else if (Mybody.Action==RespondersActions.InfromResultSetupLoadingArea && Mybody.Acknowledged) 
		{			
			Mybody.InterpretedTrigger= RespondersTriggers.DoneTask;

			Mybody.Action=RespondersActions.Noaction;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.WaitingDuring;
			Mybody.Acknowledged=false;Mybody.Sending=false; 

			//System.out.println("  "+"Driver: " + Mybody.Id + "done setup "  );
		}		
	}

	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	// Driver-  TransferCasualtytoHospital
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^	
	public void DriverBehavior_TransferCasualtytoHospital()
	{		
		//-------------------------------------------------------------------------------------------- Get command/Task
		// ++++++ 1- +++++++
		if ( Mybody.CommTrigger== RespondersTriggers.GetCommandTransferToHospital)
		{
			Mybody.Action=RespondersActions.GetcammandTransfer ;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Comunication; //TaskPlanning;				
			Mybody.EndofCurrentAction=  InputFile.GetCommand_duration  ; 	
			Mybody.CommTrigger=null;	
			Mybody.inAmbEvacuation=true;
			
		}	
		// ++++++ 2- +++++++
		else if (  Mybody.Action==RespondersActions.GetcammandTransfer && 	  Mybody.EndActionTrigger== RespondersTriggers.EndingAction )													
		{		
			Mybody.CurrentSender.Acknowledg(Mybody);

			Mybody.Action=RespondersActions.loadCasualtyToVehicle ;  // ?? not in flowchart
			Mybody.BehaviourType1=RespondersBehaviourTypes1.CasualtyTransferDelay;		
			Mybody.EndActionTrigger=null;					
		}
		//-------------------------------------------------------------------------------------------- PEA
		// ++++++ 3- +++++++	
		else if (  Mybody.Action==RespondersActions.loadCasualtyToVehicle && ((Vehicle_Ambulance) Mybody.Myvehicle).ALLCasualitiesInRoom  ) 												
		{		

			IdentifyWTR_start();
			Mybody.Myvehicle.AssignDestination2(Mybody.assignedIncident.Cordon_outer.EnteryPointAccess_node,false,true);
			Mybody.Myvehicle.ActionEffectTrigger = VehicleTriggers.InsertKey_LAleave;
			Mybody.AssignedTA.RemoveAmbfromTA(((Vehicle_Ambulance) Mybody.Myvehicle)) ;
			TansportCasualtyToHActionS() ;
			this.Iaminbark=false;
			Mybody.OnScence=false;

			Mybody.Action=RespondersActions.DrivingtoHospital;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.TaskExecution_transfertoH;			

			//System.out.println("Driver: " + Mybody.Id +  "  done loading   "  + Mybody.assignedIncident.Cordon_outer.EnteryPointAccess_node.fid);
		}
		//-------------------------------------------------------------------------------------------- Hospital
		// ++++++ 4- +++++++	
		else if (  Mybody.Action==RespondersActions.DrivingtoHospital && Mybody.SensingeEnvironmentTrigger==RespondersTriggers.ArrivedPEA  ) 												
		{		
			Mybody.Myvehicle.AssignDestination2(AssignedhospitaltoCasualty.Node,false,false);
			Mybody.Myvehicle.ActionEffectTrigger = VehicleTriggers.InsertKey_Hospital ;

			IdentifyWTR_end();
			Mybody.OnScence=false;

			Mybody.Action=RespondersActions.DrivingtoHospital;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.TaskExecution_transfertoH;			
			Mybody.SensingeEnvironmentTrigger=null;	
			// System.out.println("Driver: " + Mybody.Id +  "  ArrivedPEA"  +  Mybody.Myvehicle.Node_of_current_Location.fid    + "  " + Mybody.Myvehicle.Node_of_Target_Location.fid );
		}

		// ++++++ 5- +++++++	
		else if (Mybody.Action==RespondersActions.DrivingtoHospital && Mybody.SensingeEnvironmentTrigger==RespondersTriggers.ArrivedHospital  )
		{		

			DowenloadonHActionS() ;
			Mybody.Action= RespondersActions.DownloadCasualtyFromeVehicle;
			Mybody.EndofCurrentAction= (  InputFile.DowenloadfromVehicle_duration * ((Vehicle_Ambulance) Mybody.Myvehicle).casualtiesinRoom.size() )   ; 
			Mybody.SensingeEnvironmentTrigger=null;	
			//System.out.println("Driver: " + Mybody.Id +  "    ArrivedHospital  oh  oh  oh ");	
		}
		// ++++++ 6- +++++++
		else if (Mybody.Action== RespondersActions.DownloadCasualtyFromeVehicle &&   Mybody.EndActionTrigger== RespondersTriggers.EndingAction )
		{		
			DowenloadonHActionE() ;			
			InformResult(ACLPerformative.InformResultHTransfer ,Mybody.assignedIncident.ALCcommander) ; 
			((Vehicle_Ambulance )Mybody.Myvehicle).casualtiesinRoom.clear();
			((Vehicle_Ambulance )Mybody.Myvehicle).ALLCasualitiesInRoom=false;

			Mybody.Action=RespondersActions.InfromResultTransferToHospital;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Comunication ;                      //Sharinginfo;			
			Mybody.EndActionTrigger=null;
			//System.out.println("Driver: " + Mybody.Id +  "   InformResultHTransfer");	
		}			
		// ++++++ 7- +++++++
		else if (Mybody.Action==RespondersActions.InfromResultTransferToHospital && Mybody.Acknowledged ) 	
		{									

			if ( IsThereRoutes )
			{
				//System.out.println("Driver: " + Mybody.Id +  "  Inform route xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx  W" + routeNEEDClearwreckage_List.size() + " T " + routeNEEDCleartraffic_List.size()  ) ;
				Report Report1=new Report() ;
				Report1.Ambulance_ReportRoutes( routeNEEDClearwreckage_List ,routeNEEDCleartraffic_List  ) ;

				Mybody.CurrentMessage  = new  ACL_Message( ACLPerformative.InformRouteReport , Mybody, Mybody.assignedIncident.ALCcommander , Report1 ,  Mybody.CurrentTick ,Mybody.assignedIncident.ComMechanism_level_DrivertoALC,( routeNEEDClearwreckage_List.size() + routeNEEDCleartraffic_List.size()), TypeMesg.Inernal ) ;
				Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
				
				routeNEEDClearwreckage_List.clear();   
				routeNEEDCleartraffic_List.clear();
				IsThereRoutes= false ;

				Mybody.Action=RespondersActions.InformRouteReport;
				Mybody.BehaviourType1=RespondersBehaviourTypes1.Comunication ;               //Sharinginfo;			
				Mybody.Acknowledged=false;Mybody.Sending=false;

			}			
			else
			{
				RoadNode  Node1 = Mybody.assignedIncident.ParkingNodesforIncdent() ;
				Node1 = Mybody.assignedIncident.Cordon_outer.EnteryPointAccess_node ;
				
				
				Mybody.Myvehicle.AssignDestination2( Node1,false, false);
				Mybody.Myvehicle.ActionEffectTrigger = VehicleTriggers.InsertKey_incident;

				Mybody.Action=RespondersActions.DrivingtoIncident;
				Mybody.BehaviourType1=RespondersBehaviourTypes1.TaskExecution_transfertoH;			
				Mybody.Acknowledged=false;Mybody.Sending=false; 			
			}
		}
		// ++++++ 8- +++++++
		else if ( Mybody.Action==RespondersActions.InformRouteReport && Mybody.Acknowledged ) 	
		{
			RoadNode  Node1 = Mybody.assignedIncident.ParkingNodesforIncdent() ;
			Node1 = Mybody.assignedIncident.Cordon_outer.EnteryPointAccess_node ;
			Mybody.Myvehicle.AssignDestination2( Node1,false,false);
			Mybody.Myvehicle.ActionEffectTrigger = VehicleTriggers.InsertKey_incident;

			Mybody.Action=RespondersActions.DrivingtoIncident;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.TaskExecution_transfertoH;			
			Mybody.Acknowledged=false;Mybody.Sending=false; 
			//System.out.println("Driver: " + Mybody.Id +  "    Action:" + Mybody.Action);	
		}
		//-------------------------------------------------------------------------------------------- Come back
		// ++++++ 9- +++++++	
		else if (Mybody.Action==RespondersActions.DrivingtoIncident && Mybody.SensingeEnvironmentTrigger==RespondersTriggers.ArrivedIncident  )
		{		

			Mybody.Myvehicle.AssignDestination2(  Mybody.AssignedTA.LoadingNode,false , true);	
			Mybody.Myvehicle.ActionEffectTrigger = VehicleTriggers.InsertKey_LABack ;

			Mybody.Action=RespondersActions.DrivingtoIncident ;  
			Mybody.BehaviourType1=RespondersBehaviourTypes1.TaskExecution_transfertoH ;			
			Mybody.SensingeEnvironmentTrigger=null;

			//System.out.println("Driver: " + Mybody.Id +  "   arived PEA  gate  back" );	
		}
		// ++++++ 10- +++++++
		else if (Mybody.Action==RespondersActions.DrivingtoIncident && Mybody.SensingeEnvironmentTrigger==RespondersTriggers.ArrivedLA )
		{
			if ( Mybody.AssignedTA.AssigenAmbtoTA( (Vehicle_Ambulance) Mybody.Myvehicle)  )
			{
				Mybody.CurrentMessage  = new  ACL_Message( ACLPerformative.InformDriverArrival , Mybody, Mybody.assignedIncident.ALCcommander ,null  ,  Mybody.CurrentTick ,Mybody.assignedIncident.ComMechanism_level_DrivertoALC ,1,TypeMesg.Inernal) ;
				Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
				
				Mybody.Action=RespondersActions.NotifyArrival;
				Mybody.BehaviourType1=RespondersBehaviourTypes1.Comunication ;                      //Sharinginfo;
				Mybody.SensingeEnvironmentTrigger=null;	
				
				this.Iaminbark=true;
			}

			//System.out.println("Driver: " + Mybody.Id +  "    Action:" + Mybody.Action +".............................................................." + Mybody.Acknowledged);
		}
		//++++++ 11- +++++++
		else if (Mybody.Action==RespondersActions.NotifyArrival && Mybody.Acknowledged  )
		{			
			Mybody.InterpretedTrigger= RespondersTriggers.ComeBack;
			Mybody.Action=RespondersActions.Noaction;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.WaitingDuring;
			Mybody.Acknowledged=false;Mybody.Sending=false; 
			//System.out.println("Driver: " + Mybody.Id + " ready  to next................................................................................... " );	
			Mybody.inAmbEvacuation=false;
		}	
		
		
		if ( Mybody.zzz==true )
		{
			Mybody.zzz=false;
			System.out.println("Driver: " + Mybody.Id + "   "+ CurrentCasualtybySender.ID +"  " + AssignedhospitaltoCasualty.ID + CurrentCasualtybySender.Status  + CurrentCasualtybySender.Triage_tage       );
		
		}


	}// end DriverBehavior

}//end class


