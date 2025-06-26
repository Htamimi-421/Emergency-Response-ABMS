package A_Roles_Fire;
import A_Agents.Responder;
import A_Agents.Responder_Fire;
import A_Agents.Vehicle_Ambulance;
import A_Agents.Vehicle_Fire;
import A_Environment.RoadNode;
import B_Communication.ACL_Message;
import C_SimulationInput.InputFile;
import D_Ontology.Ontology.ACLPerformative;
import D_Ontology.Ontology.Ambulance_TaskType;
import D_Ontology.Ontology.Fire_ResponderRole;
import D_Ontology.Ontology.Fire_TaskType;
import D_Ontology.Ontology.RespondersActions;
import D_Ontology.Ontology.RespondersBehaviourTypes2;
import D_Ontology.Ontology.RespondersTriggers;
import D_Ontology.Ontology.TypeMesg;
import D_Ontology.Ontology.VehicleTriggers;

public class Fire_Driver {

	Responder_Fire Mybody;   
	boolean ToAIC=false;	
	boolean xxxbegin=true;

	//##############################################################################################################################################################
	public  Fire_Driver ( Responder_Fire _Mybody) {

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
		case InformERend : //from  FIC
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


	//----------------------------------------------------------------------------------------------------
	public void InformResult( ){	


	}
	//----------------------------------------------------------------------------------------------------
	private boolean Check_AllonVehicle()
	{
		boolean AllonVehicle= true; 

		for ( Responder_Fire Resp : ((Vehicle_Fire)  Mybody.Myvehicle).Responders_list)
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
			Mybody.CurrentAssignedActivity=Fire_TaskType.TravaltoIncident ;		
		}
		// 2- FirstArrival
		else if ( Mybody.SensingeEnvironmentTrigger== RespondersTriggers.FirstArrival_NoResponder   )
		{
			FirstArrivalAction1() ; //as assumption
			Mybody.Role=Fire_ResponderRole.FireCommunicationsOfficer;
			Mybody.CurrentCalssRole5= new Fire_Commander_FCO (Mybody);
			System.out.println(Mybody.Id +" ..... " +Mybody.Role);
		}

		// 3- control area
		else if (Mybody.SensingeEnvironmentTrigger== RespondersTriggers.NotFirstArrival_ThereisResponder  ) 
		{
			Mybody.CurrentAssignedActivity=Fire_TaskType.InitialResponse ;	
		}	

		//4- No action 			
		else if ( Mybody.CommTrigger==RespondersTriggers.CORolehanded   )
		{
			Mybody.CurrentAssignedActivity=Fire_TaskType.None ;
			Mybody.Action=RespondersActions.Noaction;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.WaitingDuring;

			Mybody.SensingeEnvironmentTrigger=null;
			Mybody.CommTrigger=null;
		}

		// 5- Driver to Base
		else if ( 	Mybody.CommTrigger == RespondersTriggers.ENDER  )
		{			
			Mybody.CurrentAssignedActivity=Fire_TaskType.TravaltoBaseStation ;
			Mybody.PrvRoleinprint2=Mybody.Role ;

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
			Mybody.Myvehicle.AssignDestination2(  Node1,false,false);	 
			((Vehicle_Fire) Mybody.Myvehicle).ActionEffectTrigger = VehicleTriggers.InsertKey_incident;
			Mybody.ActionEffectTrigger=null;
		}
		// ++++++ 2- +++++++
		else if (  Mybody.SensingeEnvironmentTrigger==RespondersTriggers.Engineisrunning  && ! Mybody.OnScence )
		{		
			Mybody.Action=RespondersActions.DrivingtoIncident ;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.Travlling;
			Mybody.SensingeEnvironmentTrigger=null;	

			Mybody. leaveStation =Mybody.CurrentTick;
		}
		// ++++++ 3- +++++++
		else if (Mybody.Action==RespondersActions.DrivingtoIncident  && Mybody.SensingeEnvironmentTrigger == RespondersTriggers.ArrivedIncident  && Check_FirstArrivalVehicle (  )  )
		{
			Mybody.OnScence=true;
			Mybody.ArrivedScene=Mybody.CurrentTick;

			((Vehicle_Fire) Mybody.Myvehicle).Swich_on_bluelightflashing(  ) ;

			Mybody.Check_FirstArrival(); //its agency only					 
			//System.out.println(Mybody.Id + " Check_FirstArrival " + Mybody.SensingeEnvironmentTrigger);
		}
		// ++++++ 4- +++++++
		else if (Mybody.Action==RespondersActions.DrivingtoIncident  && Mybody.SensingeEnvironmentTrigger == RespondersTriggers.ArrivedIncident && ! Check_FirstArrivalVehicle (  )  )
		{
			Mybody.OnScence=true;
			Mybody.ArrivedScene=Mybody.CurrentTick;

			Mybody.Myvehicle.AssignDestination2(  Mybody.assignedIncident.bluelightflashing_Node,false,false);	
			((Vehicle_Fire) Mybody.Myvehicle).ActionEffectTrigger = VehicleTriggers.InsertKey_BLF ;
			Mybody.SensingeEnvironmentTrigger=null;

		}
		// ++++++ 5- +++++++
		else if (  Mybody.SensingeEnvironmentTrigger==RespondersTriggers.Engineisrunning  &&  Mybody.OnScence )
		{		
			Mybody.Action=RespondersActions.DrivingtoIncident ;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.Travlling;		
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
		if (Mybody.CommTrigger == RespondersTriggers.ENDER    ) {


			Mybody.Action=RespondersActions.DrivingtoBaseStation;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.Travlling;

			if (Check_AllonVehicle()  )
			{
				Mybody.Myvehicle.AssignDestination2(Mybody.assignedIncident.Cordon_outer.EnteryPointAccess_node,false,true);
				((Vehicle_Fire) Mybody.Myvehicle).ActionEffectTrigger = VehicleTriggers.InsertKey_IncidentLeave;
				Mybody.CommTrigger=null;
				Mybody.assignedIncident.Time_endResponde_lastvehicleLeaveIncd= Mybody.CurrentTick ; //its will be update by all until last one
			}
			
		}
		// ++++++ 2- +++++++
		else if (  Mybody.Action==RespondersActions.DrivingtoBaseStation && Mybody.SensingeEnvironmentTrigger==RespondersTriggers.ArrivedPEA  ) 	
		{			
			Mybody.Myvehicle.AssignDestination2( ((Vehicle_Fire) Mybody.Myvehicle).AssignedStation.Node,false,false); 
		 Mybody.Myvehicle.ActionEffectTrigger = VehicleTriggers.InsertKey_Station  ;
			Mybody.Myvehicle.ActionEffectTrigger= VehicleTriggers.InsertKey_Station ;
			Mybody.SensingeEnvironmentTrigger=null;		
			//System.out.println(Mybody.Id + " start zzzzzzzzzzzzzzzzzzzzzzzzzz2zzzzzzzzzzzzzzzzzzzzzzzz" +((Vehicle_Fire) Mybody.Myvehicle).AssignedStation.ID   );
		}
		// ++++++ 3- +++++++
		else if (Mybody.Action==RespondersActions.DrivingtoBaseStation && Mybody.SensingeEnvironmentTrigger == RespondersTriggers.ArrivedBaseStation  )
		{
			Mybody.Action=RespondersActions.Idle;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.Done;
			Mybody.SensingeEnvironmentTrigger=null;	
			Mybody. Busy=false;
			System.out.println("Fire Driver" + Mybody.Id + "   ......bey bey "  );
			Mybody.BacktoStation =Mybody.CurrentTick;
			Mybody.assignedIncident.Time_endResponde_lastvehiclearrivedBS= Mybody.CurrentTick ; //its will be update by all until last one
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
				Mybody.CurrentMessage  = new  ACL_Message( ACLPerformative.InformNewResponderArrival , Mybody, Mybody.assignedIncident.FICcommander ,null  ,  Mybody.CurrentTick,Mybody.assignedIncident.ComMechanism_level_DrivertoALC,1, TypeMesg.Inernal) ;
				Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
				
				//==================
				Mybody.Action=RespondersActions.NotifyArrival;
				Mybody.BehaviourType2=RespondersBehaviourTypes2.Comunication; //RoleAssignment;
				Mybody.SensingeEnvironmentTrigger=null;	
				Mybody.FiretTimeOnScence=false;
				//System.out.println("driver"+ Mybody.Id + " NotifyArrival " );
			}
			else
			{
				//waiting AIC
				Mybody.Action=RespondersActions.Noaction;
				Mybody.BehaviourType2=RespondersBehaviourTypes2.RoleAssignment;
				Mybody.SensingeEnvironmentTrigger=null;
				//	System.out.println("driver"+ Mybody.Id +"  Waiting  No NotifyArrival " );
			}
		}

		// ++++++ 2- +++++++
		else if (Mybody.Action==RespondersActions.NotifyArrival && Mybody.Acknowledged )
		{			
			Mybody.Action=RespondersActions.Noaction;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.WaitingDuring ;
			Mybody.Acknowledged=false;Mybody.Sending=false; 
			//System.out.println("Driver: " + Mybody.Id + " ready Fire " );

		} 
	}

}//end class


