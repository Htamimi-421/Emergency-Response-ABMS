
package A_Agents;

import java.util.ArrayList;
import java.util.List;
import com.vividsolutions.jts.geom.Coordinate;
import A_Environment.PointDestination;
import A_Roles_Police.Police_Commander_CC;
import A_Roles_Police.Police_Commander_PCO;
import A_Roles_Police.Police_Commander_PIC;
import A_Roles_Police.Police_Commander_RCO;
import A_Roles_Police.Police_Driver;
import A_Roles_Police.Police_Policeman;
import B_Communication.ACL_Message;
import B_Communication.Command;
import C_SimulationInput.InputFile;
import D_Ontology.BuildStaticFuction;
import D_Ontology.Ontology;
import D_Ontology.Ontology.ACLPerformative;
import D_Ontology.Ontology.Ambulance_ResponderRole;
import D_Ontology.Ontology.CommunicationMechanism;
import D_Ontology.Ontology.Fire_ResponderRole;
import D_Ontology.Ontology.Police_ActivityType;
import D_Ontology.Ontology.Police_ResponderRole;
import D_Ontology.Ontology.Police_TaskType;
import D_Ontology.Ontology.RespondersActions;
import D_Ontology.Ontology.RespondersBehaviourTypes1;
import D_Ontology.Ontology.RespondersBehaviourTypes2;
import D_Ontology.Ontology.RespondersBehaviourTypes3;
import D_Ontology.Ontology.RespondersTriggers;
import D_Ontology.Ontology.TypeMesg;
import D_Ontology.Ontology.VehicleAction;
import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.gis.Geography;

public class Responder_Police extends Responder {

	public static  int Counter=0;

	public Police_ResponderRole  Role ;
	public boolean HighRankofficer=false;
	public Police_Driver  CurrentCalssRole1;
	public Police_Policeman  CurrentCalssRole2;		
	public Police_Commander_PIC CurrentCalssRole3;
	public Police_Commander_CC CurrentCalssRole4;	
	public Police_Commander_RCO CurrentCalssRole5;
	public Police_Commander_PCO CurrentCalssRole6;

	public Police_TaskType  CurrentAssignedActivity ;
	

	//-----------------------------------------

	public boolean OnScence=false ,IaminScenceSector=false , FiretTimeOnScence=true;
	public int  SectorNeedstoRespond=0;
	//----------------------------------------- trace hang
	int counterofaction=0 ;
	public Ontology.RespondersActions Actionold;
	//-----------------------------------------

	public ArrayList<Responder> CurrentResponders_list = new ArrayList<Responder>() ;
	public  ArrayList<Integer> NumberofCasualtyinLRUD_list  = new ArrayList<>(); //Initiator

	//##############################################################################################################################################################	
	public Responder_Police(Context<Object> _context, Geography<Object> _geography, Coordinate initialLocation, String ID, Vehicle_Police vehicle)
	{
		super(_context, _geography, initialLocation, ID, vehicle);

		this.Role= Police_ResponderRole.None;
		this.ColorCode= 0;
		this.step_long =InputFile.step_long_regularewalk;

		this.Action=RespondersActions.Idle;
		this.BehaviourType3=RespondersBehaviourTypes3.Idle;		
		CurrentAssignedActivity=Police_TaskType.None;
		CurrentAssignedMajorActivity_pol =Police_ActivityType.None;

	}

	public Police_TaskType  ReturnCuurnttask() 
	{
		return this.CurrentAssignedActivity ;
	}

	public Police_ResponderRole getRole() {
		return this.Role ;
	}

	public void setRole(Police_ResponderRole R) {
		this.Role = R;
	}

	//##############################################################################################################################################################	
	public void  ResponderInterpretationMessage()
	{
		boolean  done= true;
		ACL_Message currentmsg = Message_inbox.get(Lastmessagereaded);		 			
		CurrentSender= (Responder)currentmsg.sender;
		Lastmessagereaded++;
		
		SendingReciving_External= false ; SendingReciving_internal=false; 
		if (  currentmsg.Inernal) SendingReciving_internal= true ;
		else if (  currentmsg.External) SendingReciving_External=true ;

		switch( currentmsg.performative) {
		case Command :

			CurrentCommandRequest=((Command) currentmsg.content);

			if ( CurrentSender instanceof Responder_Police &&  CurrentCommandRequest.commandType3 == null && CurrentCommandRequest.AssignedRole3!=null )  				
			{ 
				CommTrigger= RespondersTriggers.GetCommandRole;

			}


			break;
		case InformHandoverReport:			
			CommTrigger= RespondersTriggers.GetHandoverReport;	
			//by using Commander_AIC_HandoverAction
			break;

		}
	}

	//##############################################################################################################################################################	
	//													Actions 
	//##############################################################################################################################################################	
	public void loadingResponderinVichelAction() 
	{
		this.setLocation_and_move(this.Myvehicle.getCurrent_Location());
	}

	//----------------------------------------------------------------------------------------------------
	public ArrayList<Responder_Police> CurrentPoliceInScence ( ) 
	{
		ArrayList<Responder_Police> CurrentDriver_list = new ArrayList<Responder_Police>() ;


		@SuppressWarnings("unchecked") 
		List<Vehicle_Police> nearObjects_vehicle= (List<Vehicle_Police>) BuildStaticFuction.GetObjectsWithinDistance(this,Vehicle_Police.class, 15 );


		for (int i = 0; i < nearObjects_vehicle.size(); i++) 
			if (nearObjects_vehicle.get(i).Action!= VehicleAction.WaitingRequest ||    ((Responder_Ambulance) nearObjects_vehicle.get(i).AssignedDriver).Role != Ambulance_ResponderRole.Driver ) {
				nearObjects_vehicle.remove(i);
				i--; 
			}	

		CurrentDriver_list.clear();

		for (Vehicle  V :nearObjects_vehicle)
			CurrentDriver_list.add((Responder_Police) V.AssignedDriver);

		return  CurrentDriver_list;
	}

	//----------------------------------------------------------------------------------------------------
	public Responder_Police  CurrentCommanderInScence ( Police_ResponderRole  Role  ) 
	{
		Responder_Police Commander=null ;

		//1- Get All Near Responder Objects
		@SuppressWarnings("unchecked") 
		List<Responder_Police> nearObjects_Responder= (List<Responder_Police>) BuildStaticFuction.GetObjectsWithinDistance(this,Responder_Police.class, 5);// responder 

		for (int i = 0; i < nearObjects_Responder.size(); i++) 
		{
			if (nearObjects_Responder.get(i).Role == Role ) {
				Commander=nearObjects_Responder.get(i);
				break;
			}
		}

		return Commander;
	}

	//----------------------------------------------------------------------------------------------------
	public void Check_FirstArrival (  ) 
	{
		if (Myvehicle.FirstArrivalofVehicleAgency)				 
			SensingeEnvironmentTrigger=RespondersTriggers.FirstArrival_NoResponder;
		else
			SensingeEnvironmentTrigger=RespondersTriggers.NotFirstArrival_ThereisResponder;	

	}
	//----------------------------------------------------------------------------------------------------
	public boolean Check_CommnderIsinContralArea(  ) 
	{		
		PointDestination loc=null; 
		if (  assignedIncident.ControlArea== null)
			loc=assignedIncident.bluelightflashing_Point ;
		else 
			loc=assignedIncident.ControlArea.Location;

		@SuppressWarnings("unchecked") 
		List<Responder_Police> nearObjects_Responder= (List<Responder_Police>) BuildStaticFuction.GetObjectsWithinDistance(loc,Responder_Police.class, 5); //3

		boolean result=false;

		for (int i = 0; i < nearObjects_Responder.size(); i++) 
			if (nearObjects_Responder.get(i).Role== Police_ResponderRole.PoliceIncidentCommander && nearObjects_Responder.get(i).ActivityAcceptCommunication==true) 
			{result=true;break ;}

		nearObjects_Responder.clear();
		nearObjects_Responder=null;

		return result;
	}

	//----------------------------------------------------------------------------------------------------
	public boolean Check_CommnderIsHere2 (  ) 
	{
		if ( this.assignedIncident.PICcommander!=null || this.assignedIncident.PICcommander.ActivityAcceptCommunication==false)
		{
			double dis= BuildStaticFuction.DistanceC(geography, this.Return_CurrentLocation(), this.assignedIncident.PICcommander.Return_CurrentLocation());

			if ( dis >= InputFile.FacetoFaceLimit  )  // I can add no obstacle
				return false;
			else
				return true;
		}
		else
			return false;
	}

	//______________________________________________________________________________________________________________________________________________________________
	//##############################################################################################################################################################	
	//													Responders Behavior
	//##############################################################################################################################################################	
	@ScheduledMethod(start = 1, interval = 1 ,priority= ScheduleParameters.LAST_PRIORITY)
	public void SimulationResult() {

		LogFile.IncrmentBeh(null ,null,null, null, Role, BehaviourType3);
		
		
		if (  BehaviourType3==RespondersBehaviourTypes3.Comunication &&  SendingReciving_internal==true && SendingReciving_External==false)
			Comunication_internal ++;
		else if (  BehaviourType3==RespondersBehaviourTypes3.Comunication && SendingReciving_External== true && SendingReciving_internal==false )
			Comunication_External++;
		else if (  BehaviourType3==RespondersBehaviourTypes3.Comunication )
				System.out.println("______________________________________________________________________________________________________________________________________________ "+this.Id  + this.Role+ this.Action);


		if (  BehaviourType3==RespondersBehaviourTypes3.ComunicationDelay  &&  SendingReciving_internal==true && SendingReciving_External==false)
			ComunicationDelay_internal++;
		else if (  BehaviourType3==RespondersBehaviourTypes3.ComunicationDelay  && SendingReciving_External== true && SendingReciving_internal==false )
			ComunicationDelay_External++;
		else if (  BehaviourType3==RespondersBehaviourTypes3.ComunicationDelay )
				System.out.println("______________________________________________________________________________________________________________________________________________ "+this.Id  + this.Role + this.Action);

		

	}

	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	// Responder - General 
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	//@ScheduledMethod(start = 1, interval = 1 , priority= 20)
	public void step() {

		CurrentTick=schedule.getTickCount() ;

		//System.out.println("         1     "+ this.Role );
		//*************************************************************************
		//Part one
		if(EndofCurrentAction !=0)
		{
			EndofCurrentAction--;

			if (EndofCurrentAction == 0 )
				EndActionTrigger= RespondersTriggers.EndingAction ;					
		}
		else if (  Lastmessagereaded < Message_inbox.size()  && CommTrigger==null  &&   !Sending  && ActivityAcceptCommunication && !Acknowledged  ) // Communication Trigger priority 1	
		{
			switch (this.Role) {		
			case Driver :  
				CurrentCalssRole1.Driver_InterpretationMessage();
				break;
			case Policeman:	
				CurrentCalssRole2.Policeman_InterpretationMessage();
				break;
			case PoliceIncidentCommander:
				CurrentCalssRole3.CommanderPIC_InterpretationMessage();
				break;
			case CordonsCommander:
				CurrentCalssRole4.CommanderCC_InterpretationMessage();
				break;
			case ReceptionCenterOfficer:
				CurrentCalssRole5.Commander_RCO_InterpretationMessage();
				break;
			case PoliceCommunicationsOfficer:
				CurrentCalssRole6.Commander_PCO_InterpretationMessage();
				break;			
			case None:
				ResponderInterpretationMessage(); 	
				break;}
		}

		//*************************************************************************	
		//Part Tow
		switch (this.Role) {
		case Driver : 
			CurrentCalssRole1.DriverBehavior(); //created in station
			break;
		case Policeman:	
			CurrentCalssRole2.PolicemanBehavior(); //during assign role
			break;
		case PoliceIncidentCommander:
			CurrentCalssRole3.PICBehavior();// tow times 1- by itself
			break;
		case CordonsCommander:
			CurrentCalssRole4.CCBehavior(); //during assign role
			break;
		case ReceptionCenterOfficer: 
			CurrentCalssRole5.RCOBehavior(); //during assign role
			break;
		case PoliceCommunicationsOfficer: 
			CurrentCalssRole6.PCOBehavior()	;  // ?????????????????????????
			break;			
		case None:
			//=================================================		
			if (ActionEffectTrigger == RespondersTriggers.Alarm999)
			{
				CurrentAssignedActivity=Police_TaskType.TravaltoIncident;
			}
			else if (CurrentAssignedActivity==Police_TaskType.TravaltoIncident  && SensingeEnvironmentTrigger== RespondersTriggers.FirstArrival_NoResponder )
			{				
				if (this.assignedIncident.PICcommander==null)
				{
					Role=Police_ResponderRole.PoliceIncidentCommander;
					CurrentCalssRole3= new Police_Commander_PIC (this);
					System.out.println(Id +" ..... " +Role);
				}
				else //Third Attendant
				{
					CurrentAssignedActivity=Police_TaskType.InitialResponse;
					InterpretedTrigger= RespondersTriggers.ThridAttandant ;
					SensingeEnvironmentTrigger= null;
				}

			}
			else if ( CurrentAssignedActivity==Police_TaskType.TravaltoIncident  && SensingeEnvironmentTrigger== RespondersTriggers.NotFirstArrival_ThereisResponder )
			{
				CurrentAssignedActivity=Police_TaskType.InitialResponse;
			}
			else if (CommTrigger==RespondersTriggers.ENDER || InterpretedTrigger == RespondersTriggers.ENDER ) 
			{
				CurrentAssignedActivity=Police_TaskType.TravaltoBaseStation;

			}
			//=================================================	
			switch(CurrentAssignedActivity) {
			//Mybody.VehicleOrStartDirection= Mybody.assignedIncident.IdentfyMyStartDirection (Mybody.Return_CurrentLocation()  )  ;	
			case  TravaltoIncident :
				GeneralResponderBehavior_GoToIncident()	;	
				break;
			case InitialResponse: 
				GeneralResponderBehavior_GoToControlArea();	
				break;
			case TravaltoBaseStation :
				GeneralResponderBehavior_GoToStation()	;	
				break;}
			break;
			//=================================================	
		}
		//*************************************************************************	
		//Part Three

		if (PendingMessage_list.size() !=0  && Action==RespondersActions.Noaction )
		{
			for (  ACL_Message  Msg : PendingMessage_list  )
				CurrentMessage_list.add(Msg);
			this.Action=PendingSendingAction ;
			this.BehaviourType3 =PendingBehaviourType3 ;	
			PendingMessage_list.clear(); 
			
			System.out.println("                                                                                              "+this.Id + "  3 )...I will send pending.........================================================================..........Radio " );
			
			
		}

		if (  CurrentMessage_list.size() !=0   )  //until send
		{								
			int  SMResult=0;

			if ( ! wait_forPerviousReciver  ) 
			{
				Sending=true; MyReceiver = CurrentMessage_list.get(0).receiver ; // used to check
				
				SendingReciving_External= false ; SendingReciving_internal=false; 
				if (  CurrentMessage_list.get(0).Inernal) SendingReciving_internal= true ;
				else if (  CurrentMessage_list.get(0).External) SendingReciving_External=true ;
				MyPerformative= CurrentMessage_list.get(0).performative ; Mytimetosend= CurrentMessage_list.get(0).time ; //used for output only		
				//SEND>>>>>>>>>				
				SMResult=SendMessage( CurrentMessage_list.get(0)  ) ; 

			}
			else 
			{
				if ( PerviousAcknowledged== true  )
				{ 
					PerviousAcknowledged= false; wait_forPerviousReciver=false ;

					Sending=true; MyReceiver = CurrentMessage_list.get(0).receiver ; // used to check
					
					SendingReciving_External= false ; SendingReciving_internal=false; 
					if (  CurrentMessage_list.get(0).Inernal) SendingReciving_internal= true ;
					else if (  CurrentMessage_list.get(0).External) SendingReciving_External=true ;
					MyPerformative= CurrentMessage_list.get(0).performative ; Mytimetosend= CurrentMessage_list.get(0).time ; //used for output only		
					//SEND>>>>>>>>>
					SMResult=SendMessage( CurrentMessage_list.get(0));
					//if ( this.zzz )System.out.println(Id +" ................................................done1");
				}
			}

			if ( SMResult==1 )		
			{
				if (CurrentMessage_list.size()>1  )
				{
					if ( CurrentMessage_list.get(0).ComMech==CommunicationMechanism.FaceToFace)
					{
						CurrentMessage_list.remove(0) ;	
						wait_forPerviousReciver=true ;

						PerviousAcknowledged=true ;
						Acknowledged=false;
					}
					else
					{
						CurrentMessage_list.remove(0) ;	
						wait_forPerviousReciver=true ;
					}

					//if ( this.zzz ) System.out.println(Id +" ................................................wait_forPerviousReciver"); 
				}
				else if ( CurrentMessage_list.size()==1   )
				{
					CurrentMessage_list.remove(0) ;	
					wait_forPerviousReciver=false; 
					//if ( this.zzz )System.out.println(Id +" ................................................done  all ");
				}

			}  
			else if ( SMResult==-1 )	
			{
				for (  ACL_Message  Msg : CurrentMessage_list  )
					PendingMessage_list.add(Msg);
				PendingSendingAction = this.Action;
				PendingBehaviourType3=this.BehaviourType3 ;
				CurrentMessage_list.clear();
				Sending=false;
				SendingReciving_External= false ; SendingReciving_internal=false; 
				
				
				System.out.println("                                                                                              "+this.Id + "2 )...I will save for  pending.........================================================================..........Radio " );
				

				switch (this.Role) {		
				case PoliceIncidentCommander:
					Action=RespondersActions.Noaction;
					BehaviourType3=RespondersBehaviourTypes3.WaitingDuring;
					break;
				case CordonsCommander:
					CurrentCalssRole4.TaskApproach_way();
					break;
				case ReceptionCenterOfficer: 
					Action=RespondersActions.Noaction;
					BehaviourType3=RespondersBehaviourTypes3.WaitingDuring;
					break;
				case PoliceCommunicationsOfficer:
					Action=RespondersActions.Noaction;
					BehaviourType3=RespondersBehaviourTypes3.WaitingDuring;
					break;			
				case None:
					//Action=RespondersActions.Noaction;
					//BehaviourType=RespondersBehaviourTypes.WaitingDuring;
					break;}
			}

		}
		//*************************************************************************	
		//Trace		
		if (Actionold ==Action  && Actionold != RespondersActions.Noaction && Role!= Police_ResponderRole.None  && Role!= Police_ResponderRole.Driver &&  Role!= Police_ResponderRole.PoliceCommunicationsOfficer  ) //&&  Role!= Police_ResponderRole.Policeman
			this.counterofaction ++;
		else
		{
			counterofaction=0;
			Actionold =Action ;
		}

		if ( counterofaction== InputFile.counterofaction && Counter<10) 
		{
			Counter++;
			System.out.println(" " );
			System.out.println(" &&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&  Attention/Error &&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&     "+ this.Role  );
			System.out.println(Id  +  "  Action: " + Action + " com:" + CommTrigger + "   Nt:" + InterpretedTrigger+ " SensingeEnvir:" +SensingeEnvironmentTrigger + " end:" + EndActionTrigger +"  Acknowledged: " + Acknowledged + "   Sending:" + this.Sending +"  "+ ActivityAcceptCommunication );
			System.out.println(" " );
			counterofaction=0;


			//if (   Role== Police_ResponderRole.Policeman )
			//	this.CurrentCalssRole2.printxxx();


		}
	}// end Step *************

	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	// Responders- Travel to Incident site
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	private void GeneralResponderBehavior_GoToIncident()	
	{
		// ++++++ 1- +++++++
		if ( ActionEffectTrigger == RespondersTriggers.Alarm999 && SensingeEnvironmentTrigger==RespondersTriggers.Engineisrunning )  
		{
			Action=RespondersActions.TravalingToIncident ;
			BehaviourType3=RespondersBehaviourTypes3.Travlling;
			ActionEffectTrigger =null;
			SensingeEnvironmentTrigger=null;

			leaveStation=this.CurrentTick;
		}
		// ++++++ 2- +++++++
		else if (Action== RespondersActions.TravalingToIncident  && SensingeEnvironmentTrigger== RespondersTriggers.ArrivedIncident)
		{			

			Action=RespondersActions.Noaction;
			BehaviourType3=RespondersBehaviourTypes3.WaitingDuring ;   //WaitingDuring 

			ArrivedScene= CurrentTick;
			Generate_responder_zones() ;	
			RadiusOfReponderVision=assignedIncident.radius_incidentCircle ; // not yet used in search for casualty 
			VehicleOrStartDirection= assignedIncident.IdentfyMyStartDirection (Return_CurrentLocation()  )  ;	//form LRUD
			NextOppositVehicleOrStartDirection( );

			Check_FirstArrival (  ) ;

			//System.out.println(Id + " Check_FirstArrival   " + SensingeEnvironmentTrigger);

		}
	}

	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	// Responders- GoToControlArea
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	private void  GeneralResponderBehavior_GoToControlArea()	
	{		
		// ++++++ 1- +++++++
		if (SensingeEnvironmentTrigger== RespondersTriggers.NotFirstArrival_ThereisResponder  || InterpretedTrigger== RespondersTriggers.ThridAttandant ) 
		{													
			if (  assignedIncident.ControlArea != null)
			{
				Assign_DestinationCordon(assignedIncident.ControlArea.Location);
				Action=RespondersActions.GoToControlArea;							
				BehaviourType3=RespondersBehaviourTypes3.RoleAssignment;
			}
			else
			{
				Assign_DestinationCordon(assignedIncident.bluelightflashing_Point);
				Action=RespondersActions.GoToControlArea;							
				BehaviourType3=RespondersBehaviourTypes3.RoleAssignment;
			}

			if (SensingeEnvironmentTrigger== RespondersTriggers.NotFirstArrival_ThereisResponder) SensingeEnvironmentTrigger=null;
			if (InterpretedTrigger== RespondersTriggers.ThridAttandant ) InterpretedTrigger= null;
		}
		// ++++++ 2- +++++++
		else if (Action==RespondersActions.GoToControlArea  && SensingeEnvironmentTrigger==null)// Action
		{	
			Walk();
		}
		//-------------------------------------------------------------------------------------------- Not HighRankofficer
		// ++++++ 3- +++++++
		else if ((( Action==RespondersActions.GoToControlArea && SensingeEnvironmentTrigger== RespondersTriggers.ArrivedTargetObject)||
				(  FiretTimeOnScence && Action==RespondersActions.Noaction && Check_CommnderIsinContralArea(  )  ))
				&& HighRankofficer== false) 
		{ 			
			//send message to AIC				
			//Responder_Ambulance Comder= CurrentCommanderInScence ( Ambulance_ResponderRole.AmbulanceIncidentCommander ) ;
			if ( Check_CommnderIsinContralArea(  )   )
			{
				CurrentMessage = new  ACL_Message( ACLPerformative.InformNewResponderArrival ,this , this.assignedIncident.PICcommander ,null , CurrentTick,this.assignedIncident.ComMechanism_inControlArea,1,TypeMesg.Inernal) ;
				CurrentMessage_list.add(CurrentMessage);
				//==================
				Action=RespondersActions.NotifyArrival;
				BehaviourType3=RespondersBehaviourTypes3.Comunication; //RoleAssignment;
				SensingeEnvironmentTrigger=null;
				FiretTimeOnScence=false;
				//System.out.println(Id + " NotifyArrival " );
			}
			else
			{
				Action=RespondersActions.Noaction;
				BehaviourType3=RespondersBehaviourTypes3.RoleAssignment;
				SensingeEnvironmentTrigger=null;
				//System.out.println(Id + "  Waiting  No NotifyArrival " );
			}
		}	
		// ++++++ 4- +++++++
		else if (Action==RespondersActions.NotifyArrival &&  Acknowledged) 
		{
			Action=RespondersActions.Noaction;
			BehaviourType3=RespondersBehaviourTypes3.RoleAssignment;
			Acknowledged=false;Sending=false; 
		}
		// ++++++ 5- +++++++
		else if ( CommTrigger==RespondersTriggers.GetCommandRole  ) 												
		{															
			Action=RespondersActions.GetcammandRole;
			BehaviourType3=RespondersBehaviourTypes3.Comunication; //RoleAssignment;
			EndofCurrentAction=  InputFile.GetCommand_duration  ; 
			CommTrigger=null;
			//System.out.println(Id + " GetCommandRole" );
		}	
		// ++++++ 6- +++++++
		else if ( Action==RespondersActions.GetcammandRole &&  EndActionTrigger==RespondersTriggers.EndingAction) 												
		{		
			CurrentSender.Acknowledg(this) ;	

			Role=CurrentCommandRequest.AssignedRole3;

			switch( Role)	{
			case Policeman:	
				CurrentCalssRole2= new Police_Policeman (this);	
				break;
			case CordonsCommander:
				CurrentCalssRole4= new Police_Commander_CC (this,CurrentCommandRequest.TargetCordon );
				break;
			case ReceptionCenterOfficer: 
				CurrentCalssRole5= new Police_Commander_RCO (this ,CurrentCommandRequest.TargetTA );
				break;
			case PoliceCommunicationsOfficer:
				CurrentCalssRole6= new Police_Commander_PCO (this);
				break;
			}

			CommTrigger=RespondersTriggers.AssigendRolebyPIC;
			EndActionTrigger=null;

			if ( Role!=  Police_ResponderRole.Policeman)  System.out.println(Id +" ..... " +Role);
		}
		//--------------------------------------------------------------------------------------------  HighRankofficer
		// ++++++ 7- +++++++
		else if (Action==RespondersActions.GoToControlArea && SensingeEnvironmentTrigger== RespondersTriggers.ArrivedTargetObject && HighRankofficer== true) 
		{ 			
			//send message to Temp AIC				
			//Responder_Ambulance Comder= CurrentCommanderInScence ( Ambulance_ResponderRole.AmbulanceIncidentCommander ) ;

			CurrentMessage  = new  ACL_Message( ACLPerformative.Command , this, this.assignedIncident.AICcommander ,null , this.CurrentTick,this.assignedIncident.ComMechanism_inControlArea,1 ,TypeMesg.Inernal) ;
			CurrentMessage_list.add(CurrentMessage);
			//==================
			Action=RespondersActions.instructTemoAICtohandover ;
			BehaviourType3=RespondersBehaviourTypes3.Comunication ;   //Planformulation
			SensingeEnvironmentTrigger=null;

		}
		// ++++++ 8- +++++++
		else if (Action==RespondersActions.instructTemoAICtohandover  &&  Acknowledged) 
		{
			Action=RespondersActions.Noaction;
			BehaviourType3=RespondersBehaviourTypes3.WaitingDuring ;
			Acknowledged=false;Sending=false; 
		}
		// ++++++ 9- +++++++
		else if ( CommTrigger==RespondersTriggers.GetHandoverReport	  ) 	//Actual no content send 											
		{															
			Action=RespondersActions.GetHandoverReport;
			BehaviourType3=RespondersBehaviourTypes3.Comunication   ;   
			EndofCurrentAction=  InputFile.GetInfromation_FtoForR_Duration_Data  ; 
			CommTrigger=null;
		}	

		// ++++++ 10- +++++++
		else if ( Action==RespondersActions.GetHandoverReport &&  EndActionTrigger==RespondersTriggers.EndingAction) 												
		{		

			CurrentSender.Acknowledg(this) ;	
			Role=Police_ResponderRole.PoliceIncidentCommander;
			CurrentCalssRole3= new Police_Commander_PIC (this);
			CurrentCalssRole3.Commander_PIC_HandoverAction ( (Responder_Police) CurrentSender )  ;	 //from TempAIC	
			CurrentCalssRole3.UnoccupiedResponders.add((Responder_Police) CurrentSender );

			CommTrigger=RespondersTriggers.GetHandoverReport;
			EndActionTrigger=null;

			System.out.println(Id +" HRP " +Role);
		}
		// +++++++++++++

	}//end 

	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	// Responders- Go To Vehicle after finish 
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	private void GeneralResponderBehavior_GoToStation()	
	{

		// ++++++ 1- +++++++
		if (CommTrigger==RespondersTriggers.ENDER   )
		{	
			Action=RespondersActions.GetNotificationEndER;
			BehaviourType3=RespondersBehaviourTypes3.Comunication ;   //Gatheringinfo
			EndofCurrentAction=  InputFile.GetInfromation_FtoForR_Duration_Notification ;
			CommTrigger=null;	
		}
		// ++++++ 2- +++++++
		else if (( Action==RespondersActions.GetNotificationEndER && EndActionTrigger==RespondersTriggers.EndingAction)  ||  InterpretedTrigger == RespondersTriggers.ENDER )
		{	
			Assign_DestinationVehicle(Myvehicle);

			Action=RespondersActions.GoToVehicle;
			BehaviourType3=RespondersBehaviourTypes3.Travlling;
			EndActionTrigger=null;
			InterpretedTrigger =null;
			//NO CurrentSender.Acknowledg() ; because it is broadcast
		}
		// ++++++ 3- +++++++
		else if (Action==RespondersActions.GoToVehicle   && SensingeEnvironmentTrigger!=RespondersTriggers.ArrivedVehicle)
		{	
			Walk();
		}
		// ++++++ 4- +++++++
		else if (Action==RespondersActions.GoToVehicle   && SensingeEnvironmentTrigger==RespondersTriggers.ArrivedVehicle)
		{ 
			loadingResponderinVichelAction();
			Destroy_responder_Zones();

			Action=RespondersActions.OnVehicle;
			BehaviourType3=RespondersBehaviourTypes3.Travlling;
			SensingeEnvironmentTrigger=null;

		}
		// ++++++ 5- +++++++
		else if (Action==RespondersActions.OnVehicle  && SensingeEnvironmentTrigger==RespondersTriggers.Engineisrunning)
		{ 
			Action=RespondersActions.TravalingToBaseStation;
			BehaviourType3=RespondersBehaviourTypes3.Travlling;
			SensingeEnvironmentTrigger=null;

		}
		// ++++++ 6- +++++++
		else if (Action==RespondersActions.TravalingToBaseStation  && SensingeEnvironmentTrigger==RespondersTriggers.ArrivedBaseStation  )
		{
			Action=RespondersActions.Idle;
			BehaviourType3=RespondersBehaviourTypes3.Done;
			SensingeEnvironmentTrigger=null;
			Busy=false;
			BacktoStation= CurrentTick;

			System.out.println("Police Responder" + Id + "   ......bey bey "  );

		}
	}


}//end class

