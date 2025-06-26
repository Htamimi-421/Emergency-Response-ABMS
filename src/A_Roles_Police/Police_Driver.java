package A_Roles_Police;
import A_Agents.Responder;
import A_Agents.Responder_Police;
import A_Agents.Vehicle_Ambulance;
import A_Agents.Vehicle_Fire;
import A_Agents.Vehicle_Police;
import A_Environment.RoadNode;
import B_Communication.ACL_Message;
import C_SimulationInput.InputFile;
import D_Ontology.Ontology.ACLPerformative;
import D_Ontology.Ontology.Fire_TaskType;
import D_Ontology.Ontology.Police_ResponderRole;
import D_Ontology.Ontology.Police_TaskType;
import D_Ontology.Ontology.RespondersActions;
import D_Ontology.Ontology.RespondersBehaviourTypes3;
import D_Ontology.Ontology.RespondersTriggers;
import D_Ontology.Ontology.TypeMesg;
import D_Ontology.Ontology.VehicleTriggers;

public class Police_Driver{

	Responder_Police Mybody;   
	boolean ToFIC=false;	
	boolean xxxbegin=true;

	//##############################################################################################################################################################
	public  Police_Driver ( Responder_Police _Mybody) {

		Mybody=_Mybody;
		Mybody.EndofCurrentAction=0;

		//Mybody.PrvRoleinprint =Mybody.Role;
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
		case InformERend : //from PIC
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
	public void InformResult( ){	


	}
	//----------------------------------------------------------------------------------------------------
	private boolean Check_AllonVehicle()
	{
		boolean AllonVehicle= true; 

		for ( Responder_Police Resp : ( (Vehicle_Police ) Mybody.Myvehicle).Responders_list)
			if ( Resp!=Mybody & Resp.Action!=RespondersActions.OnVehicle)
			{ AllonVehicle= false;}

		return AllonVehicle;
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
		//*************************************************************************
		// 1- Driver to scene
		if ( 	Mybody.ActionEffectTrigger == RespondersTriggers.Alarm999  )
		{			
			Mybody.CurrentAssignedActivity=Police_TaskType.TravaltoIncident ;		
		}
		// 2- FirstArrival
		else if ( Mybody.SensingeEnvironmentTrigger== RespondersTriggers.FirstArrival_NoResponder   )
		{
			FirstArrivalAction1() ; //as assumption
			Mybody.Role=Police_ResponderRole.PoliceCommunicationsOfficer;
			Mybody.CurrentCalssRole6= new Police_Commander_PCO (Mybody);
			System.out.println(Mybody.Id +" ..... " +Mybody.Role);
		}
		// 3- control area
		else if (Mybody.SensingeEnvironmentTrigger== RespondersTriggers.NotFirstArrival_ThereisResponder  ) 
		{
			Mybody.CurrentAssignedActivity=Police_TaskType.InitialResponse ;	
		}	
		//4- No action 			
		else if ( Mybody.CommTrigger==RespondersTriggers.PCORolehanded   )
		{
			Mybody.CurrentAssignedActivity=Police_TaskType.None ;
			Mybody.Action=RespondersActions.Noaction;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.WaitingDuring;

			Mybody.SensingeEnvironmentTrigger=null;
			Mybody.CommTrigger=null;
		}
		// 5- Driver to Base
		else if ( 	Mybody.CommTrigger == RespondersTriggers.ENDER  )
		{			
			Mybody.CurrentAssignedActivity=Police_TaskType.TravaltoBaseStation ;
			Mybody.PrvRoleinprint3=Mybody.Role ;

		}	
		//*************************************************************************
		switch(Mybody.CurrentAssignedActivity) {
		case TravaltoIncident :
			DriverBehavior_GotoIncident()	;	
			break;
		case TravaltoBaseStation :
			DriverBehavior_GotoBaseStation()	;
			break;
		case InitialResponse :
			DriverBehavior_InitialResponse(); 
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
		if ( 	Mybody.ActionEffectTrigger == RespondersTriggers.Alarm999  )  // and AssignedIncident updated
		{			
			RoadNode  Node1 = Mybody.assignedIncident.ParkingNodesforIncdent() ;
			Mybody.Myvehicle.AssignDestination2( Node1,false,false);	 // need to think how to consider more than location 
			((Vehicle_Police ) Mybody.Myvehicle).ActionEffectTrigger = VehicleTriggers.InsertKey_incident;
			Mybody.ActionEffectTrigger=null;
		}
		// ++++++ 2- +++++++
		else if (  Mybody.SensingeEnvironmentTrigger==RespondersTriggers.Engineisrunning  && ! Mybody.OnScence )
		{		
			Mybody.Action=RespondersActions.DrivingtoIncident ;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.Travlling;
			Mybody.SensingeEnvironmentTrigger=null;	

			Mybody. leaveStation =Mybody.CurrentTick;
		}
		//---------------------------------------------------------------------------------------------------
		// ++++++ 3- +++++++
		else if (Mybody.Action==RespondersActions.DrivingtoIncident  && Mybody.SensingeEnvironmentTrigger == RespondersTriggers.ArrivedIncident  && Check_FirstArrivalVehicle (  )  )
		{
			Mybody.OnScence=true;
			Mybody.ArrivedScene=Mybody.CurrentTick;

			((Vehicle_Police) Mybody.Myvehicle).Swich_on_bluelightflashing(  ) ;

			Mybody.Check_FirstArrival(); //its agency only					 
			//System.out.println(Mybody.Id + " Check_FirstArrival " + Mybody.SensingeEnvironmentTrigger);
		}
		// ++++++ 4- +++++++
		else if (Mybody.Action==RespondersActions.DrivingtoIncident  && Mybody.SensingeEnvironmentTrigger == RespondersTriggers.ArrivedIncident && ! Check_FirstArrivalVehicle (  )  )
		{
			Mybody.OnScence=true;
			Mybody.ArrivedScene=Mybody.CurrentTick;

			Mybody.Myvehicle.AssignDestination2(  Mybody.assignedIncident.bluelightflashing_Node,false,false);	
			((Vehicle_Police) Mybody.Myvehicle).ActionEffectTrigger = VehicleTriggers.InsertKey_BLF ;
			Mybody.SensingeEnvironmentTrigger=null;

		}
		// ++++++ 5- +++++++
		else if (  Mybody.SensingeEnvironmentTrigger==RespondersTriggers.Engineisrunning  &&  Mybody.OnScence )
		{		
			Mybody.Action=RespondersActions.DrivingtoIncident ;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.Travlling;		
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
		if (Mybody.CommTrigger == RespondersTriggers.ENDER      ) {


			Mybody.Action=RespondersActions.DrivingtoBaseStation;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.Travlling;

			if (Check_AllonVehicle()  )
			{
				Mybody.Myvehicle.AssignDestination2(Mybody.assignedIncident.Cordon_outer.EnteryPointAccess_node,false,true);
				Mybody.Myvehicle.ActionEffectTrigger = VehicleTriggers.InsertKey_IncidentLeave;
				Mybody.CommTrigger=null; 
				Mybody.assignedIncident.Time_endResponde_lastvehicleLeaveIncd= Mybody.CurrentTick ; //its will be update by all until last one
			}
		}
		// ++++++ 2- +++++++
		else if (  Mybody.Action==RespondersActions.DrivingtoBaseStation && Mybody.SensingeEnvironmentTrigger==RespondersTriggers.ArrivedPEA  ) 	
		{			
			Mybody.Myvehicle.AssignDestination2( ((Vehicle_Police) Mybody.Myvehicle).AssignedStation.Node,false,false); 
			Mybody.Myvehicle.ActionEffectTrigger = VehicleTriggers.InsertKey_Station  ;
			Mybody.SensingeEnvironmentTrigger=null;	
			//System.out.println(Mybody.Id + " start zzzzzzzzzzzzzzzzzzzzzzzzzz2zzzzzzzzzzzzzzzzzzzzzzzz" +((Vehicle_Police) Mybody.Myvehicle).AssignedStation.ID   );
		}
		// ++++++ 3- +++++++
		else if (Mybody.Action==RespondersActions.DrivingtoBaseStation && Mybody.SensingeEnvironmentTrigger == RespondersTriggers.ArrivedBaseStation  )
		{
			Mybody.Action=RespondersActions.Idle;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.Done;
			Mybody.SensingeEnvironmentTrigger=null;	
			Mybody. Busy=false;
			System.out.println("Police Driver" + Mybody.Id + "   ......bey bey "  );
			Mybody.BacktoStation =Mybody.CurrentTick;
			Mybody.assignedIncident.Time_endResponde_lastvehiclearrivedBS= Mybody.CurrentTick ; //its will be update by all until last one
			//for (Responder_Police r: Mybody.assignedIncident.xxx )
			//	System.out.println(r.Id  +  "  Action: " + r.Action + " com:" + r.CommTrigger + "   Nt:" + r.InterpretedTrigger+ " en:" +r.SensingeEnvironmentTrigger + " end:" + r.EndActionTrigger +"   " + r.Acknowledged );
		}
	}

	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	// Driver- InitialResponse
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^	
	public void DriverBehavior_InitialResponse()
	{		
		//-------------------------------------------------------------------------------------------- inform FIC
		// ++++++ 1- +++++++
		if ( Mybody.SensingeEnvironmentTrigger== RespondersTriggers.NotFirstArrival_ThereisResponder ||

				(  Mybody.FiretTimeOnScence && ( Mybody.Action==RespondersActions.DrivingtoIncident ||Mybody.Action==RespondersActions.Noaction) && Mybody.Check_CommnderIsinContralArea()   ))			
		{ 			

			if ( Mybody.Check_CommnderIsinContralArea()  )
			{			
				//send message to FIC  
				Mybody.CurrentMessage  = new  ACL_Message( ACLPerformative.InformNewResponderArrival , Mybody, Mybody.assignedIncident.PICcommander ,null  ,  Mybody.CurrentTick,Mybody.assignedIncident.ComMechanism_level_DrivertoALC,1 ,TypeMesg.Inernal) ;
				Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
				//==================
				Mybody.Action=RespondersActions.NotifyArrival;
				Mybody.BehaviourType3=RespondersBehaviourTypes3.Comunication; //RoleAssignment;
				Mybody.SensingeEnvironmentTrigger=null;	
				Mybody.FiretTimeOnScence=false;
				//System.out.println("driver"+ Mybody.Id + " NotifyArrival " );
			}
			else
			{
				//waiting AIC
				Mybody.Action=RespondersActions.Noaction;
				Mybody.BehaviourType3=RespondersBehaviourTypes3.RoleAssignment;
				Mybody.SensingeEnvironmentTrigger=null;
				//	System.out.println("driver"+ Mybody.Id +"  Waiting  No NotifyArrival " );
			}
		}

		// ++++++ 2- +++++++
		else if (Mybody.Action==RespondersActions.NotifyArrival && Mybody.Acknowledged )
		{			
			Mybody.Action=RespondersActions.Noaction;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.WaitingDuring ;
			Mybody.Acknowledged=false;Mybody.Sending=false; 
			//System.out.println("Driver: " + Mybody.Id + " ready Police " );
			
			

		} 
	}

}//end class



