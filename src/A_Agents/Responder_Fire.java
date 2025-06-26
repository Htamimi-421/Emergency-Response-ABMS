package A_Agents;

import java.util.ArrayList;
import java.util.List;
import com.vividsolutions.jts.geom.Coordinate;
import A_Environment.PointDestination;
import A_Roles_Fire.Fire_Commander_FCO;
import A_Roles_Fire.Fire_Commander_FIC;
import A_Roles_Fire.Fire_Commander_SC;
import A_Roles_Fire.Fire_Driver;
import A_Roles_Fire.Fire_FireFighter;
import B_Communication.ACL_Message;
import B_Communication.Command;
import C_SimulationInput.InputFile;
import D_Ontology.BuildStaticFuction;
import D_Ontology.Ontology;
import D_Ontology.Ontology.ACLPerformative;
import D_Ontology.Ontology.Ambulance_ResponderRole;
import D_Ontology.Ontology.Ambulance_TaskType;
import D_Ontology.Ontology.CommunicationMechanism;
import D_Ontology.Ontology.Fire_ActivityType;
import D_Ontology.Ontology.Fire_ResponderRole;
import D_Ontology.Ontology.Fire_TaskType;
import D_Ontology.Ontology.Police_ResponderRole;
import D_Ontology.Ontology.RespondersActions;
import D_Ontology.Ontology.RespondersBehaviourTypes1;
import D_Ontology.Ontology.RespondersBehaviourTypes2;
import D_Ontology.Ontology.RespondersTriggers;
import D_Ontology.Ontology.TypeMesg;
import D_Ontology.Ontology.VehicleAction;
import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.gis.Geography;

public class Responder_Fire extends Responder {
	public int ss=0;
	public static  int Counter=0;

	public Fire_ResponderRole  Role ;
	public boolean HighRankofficer=false;

	public  Fire_Driver  CurrentCalssRole1;
	public  Fire_FireFighter  CurrentCalssRole2;		
	public Fire_Commander_FIC CurrentCalssRole3;
	public Fire_Commander_SC CurrentCalssRole4;	
	public Fire_Commander_FCO CurrentCalssRole5;

	public Fire_TaskType  CurrentAssignedActivity ;
	

	//-----------------------------------------

	public boolean OnScence=false ,IaminScenceSector=false , FiretTimeOnScence=true;
	public int  SectorNeedstoRespond=0;
	//public RoadLink  AssignedRoad ;
	boolean Notyetinfrom ;
	//----------------------------------------- trace hang
	int counterofaction=0 ;
	public Ontology.RespondersActions Actionold;
	//-----------------------------------------

	public ArrayList<Responder> CurrentResponders_list = new ArrayList<Responder>() ;
	public  ArrayList<Integer> NumberofCasualtyinLRUD_list  = new ArrayList<>(); //Initiator

	//##############################################################################################################################################################	
	public Responder_Fire(Context<Object> _context, Geography<Object> _geography, Coordinate initialLocation, String ID, Vehicle_Fire vehicle)
	{
		super(_context, _geography, initialLocation, ID, vehicle);

		this.Role= Fire_ResponderRole.None;
		this.ColorCode= 0;
		this.step_long =InputFile.step_long_regularewalk;

		this.Action=RespondersActions.Idle;
		this.BehaviourType2=RespondersBehaviourTypes2.Idle;		
		CurrentAssignedActivity=Fire_TaskType.None;
		CurrentAssignedMajorActivity_fr =Fire_ActivityType.None;

	}

	public Fire_TaskType  ReturnCuurnttask() 
	{
		return this.CurrentAssignedActivity ;
	}

	public Fire_ResponderRole getRole() {
		return this.Role ;
	}

	public void setRole(Fire_ResponderRole R) {
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

			if ( CurrentSender instanceof Responder_Fire &&  CurrentCommandRequest.commandType2 == null && CurrentCommandRequest.AssignedRole2!=null )  				
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
	public void Check_FirstArrival (  ) 
	{
		if (Myvehicle.FirstArrivalofVehicleAgency)				 
			SensingeEnvironmentTrigger=RespondersTriggers.FirstArrival_NoResponder;
		else
			SensingeEnvironmentTrigger=RespondersTriggers.NotFirstArrival_ThereisResponder;	

	}

	//----------------------------------------------------------------------------------------------------
	public ArrayList<Responder_Fire> CurrentFireInScence ( ) 
	{
		ArrayList<Responder_Fire> CurrentDriver_list = new ArrayList<Responder_Fire>() ;


		@SuppressWarnings("unchecked") 
		List<Vehicle_Fire> nearObjects_vehicle= (List<Vehicle_Fire>) BuildStaticFuction.GetObjectsWithinDistance(this,Vehicle_Fire.class, 15 );


		for (int i = 0; i < nearObjects_vehicle.size(); i++) 
			if (nearObjects_vehicle.get(i).Action!= VehicleAction.WaitingRequest ||    ((Responder_Fire) nearObjects_vehicle.get(i).AssignedDriver).Role != Fire_ResponderRole.Driver ) {
				nearObjects_vehicle.remove(i);
				i--; 
			}	

		CurrentDriver_list.clear();

		for (Vehicle  V :nearObjects_vehicle)
			CurrentDriver_list.add((Responder_Fire) V.AssignedDriver);



		return  CurrentDriver_list;
	}

	//----------------------------------------------------------------------------------------------------
	public Responder_Fire  CurrentCommanderInScence ( Fire_ResponderRole  Role  ) 
	{
		Responder_Fire Commander=null ;

		//1- Get All Near Responder Objects
		@SuppressWarnings("unchecked") 
		List<Responder_Fire> nearObjects_Responder= (List<Responder_Fire>) BuildStaticFuction.GetObjectsWithinDistance(this,Responder_Fire.class, 5);// responder 

		for (int i = 0; i < nearObjects_Responder.size(); i++) 
		{
			if (nearObjects_Responder.get(i).Role == Role ) {
				Commander=nearObjects_Responder.get(i);
				break;
			}
		}

		nearObjects_Responder.clear();

		return Commander;
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
		List<Responder_Fire> nearObjects_Responder= (List<Responder_Fire>) BuildStaticFuction.GetObjectsWithinDistance(loc,Responder_Fire.class, 10); //3

		boolean result=false;

		for (int i = 0; i < nearObjects_Responder.size(); i++) 
			if (nearObjects_Responder.get(i).Role== Fire_ResponderRole.FireIncidentCommander  && nearObjects_Responder.get(i).ActivityAcceptCommunication==true) 
			{result=true;break ;}


		nearObjects_Responder.clear();
		nearObjects_Responder=null;


		return result;
	}

	//----------------------------------------------------------------------------------------------------
	public boolean Check_CommnderIsHere2 (  ) 
	{
		if ( this.assignedIncident.FICcommander!=null || this.assignedIncident.FICcommander.ActivityAcceptCommunication==false)
		{
			double dis= BuildStaticFuction.DistanceC(geography, this.Return_CurrentLocation(), this.assignedIncident.FICcommander.Return_CurrentLocation());

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


		LogFile.IncrmentBeh(null ,null, Role, BehaviourType2 ,null, null);
		
		if (  BehaviourType2==RespondersBehaviourTypes2.Comunication &&  SendingReciving_internal==true && SendingReciving_External==false)
			Comunication_internal ++;
		else if (  BehaviourType2==RespondersBehaviourTypes2.Comunication && SendingReciving_External== true && SendingReciving_internal==false )
			Comunication_External++;
		else if (  BehaviourType2==RespondersBehaviourTypes2.Comunication )
				System.out.println("______________________________________________________________________________________________________________________________________________ "+this.Id  + this.Role+ this.Action);


		if (  BehaviourType2==RespondersBehaviourTypes2.ComunicationDelay  &&  SendingReciving_internal==true && SendingReciving_External==false)
			ComunicationDelay_internal++;
		else if (  BehaviourType2==RespondersBehaviourTypes2.ComunicationDelay  && SendingReciving_External== true && SendingReciving_internal==false )
			ComunicationDelay_External++;
		else if (  BehaviourType2==RespondersBehaviourTypes2.ComunicationDelay )
				System.out.println("______________________________________________________________________________________________________________________________________________ "+this.Id  + this.Role + this.Action);

		


	}

	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	// Responder - General 
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	//@ScheduledMethod(start = 1, interval = 1,priority= 21)
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
			case FireFighter:	
				CurrentCalssRole2.FireFighter_InterpretationMessage();
				break;
			case FireIncidentCommander:
				CurrentCalssRole3.CommanderFIC_InterpretationMessage();
				break;
			case FireSectorCommander:
				CurrentCalssRole4.Fire_Commander_SC_InterpretationMessage();
				break;
			case FireCommunicationsOfficer:
				CurrentCalssRole5.Commander_PCO_InterpretationMessage();
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
		case FireFighter:	
			CurrentCalssRole2.FireFighterBehavior(); //during assign role
			break;
		case FireIncidentCommander:
			CurrentCalssRole3.FICBehavior();// tow times 1- by itself
			break;
		case FireSectorCommander:
			CurrentCalssRole4.FSCBehavior(); //during assign role
			break;
		case FireCommunicationsOfficer: 
			CurrentCalssRole5.FCOBehavior()	;  // ?????????????????????????
			break;			
		case None:
			//=================================================		
			if (ActionEffectTrigger == RespondersTriggers.Alarm999)
			{
				CurrentAssignedActivity=Fire_TaskType.TravaltoIncident;
			}
			else if (CurrentAssignedActivity==Fire_TaskType.TravaltoIncident  && SensingeEnvironmentTrigger== RespondersTriggers.FirstArrival_NoResponder )
			{				
				if (this.assignedIncident.FICcommander==null)
				{
					Role=Fire_ResponderRole.FireIncidentCommander;
					CurrentCalssRole3= new Fire_Commander_FIC (this);
					System.out.println(Id +" ..... " +Role);
				}
				else //Third Attendant
				{
					CurrentAssignedActivity=Fire_TaskType.InitialResponse;
					InterpretedTrigger= RespondersTriggers.ThridAttandant ;
					SensingeEnvironmentTrigger= null;
				}

			}
			else if ( CurrentAssignedActivity==Fire_TaskType.TravaltoIncident  && SensingeEnvironmentTrigger== RespondersTriggers.NotFirstArrival_ThereisResponder )
			{
				CurrentAssignedActivity=Fire_TaskType.InitialResponse;
			}
			else if ( InterpretedTrigger == RespondersTriggers.DoneActivity  )
			{
				CurrentAssignedActivity=Fire_TaskType.GoBackToControlArea ;
			}
			else if (CommTrigger==RespondersTriggers.ENDER || InterpretedTrigger == RespondersTriggers.ENDER ) 
			{
				CurrentAssignedActivity=Fire_TaskType.TravaltoBaseStation;

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
			case GoBackToControlArea :
				FireFighterBehavior_GoBackToControlArea() ; 	
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
			this.BehaviourType2 =PendingBehaviourType2 ;	
			PendingMessage_list.clear();
			
			System.out.println("                                                                                              "+this.Id + "  3 )...I will send pending.........================================================================..........Radio " );
			
		}

		if (  CurrentMessage_list.size() !=0  )   //until send
		{			
			int  SMResult=0;

			if ( ! wait_forPerviousReciver  ) // First time  or last time
			{
				Sending=true; MyReceiver = CurrentMessage_list.get(0).receiver ; // used to check
				
				SendingReciving_External= false ; SendingReciving_internal=false; 
				if (  CurrentMessage_list.get(0).Inernal) SendingReciving_internal= true ;
				else if (  CurrentMessage_list.get(0).External) SendingReciving_External=true ;
				MyPerformative= CurrentMessage_list.get(0).performative ; Mytimetosend= CurrentMessage_list.get(0).time ; //used for output only		
				//SEND>>>>>>>>>
				SMResult=SendMessage( CurrentMessage_list.get(0)  ) ; 
			}
			else if (  wait_forPerviousReciver  )    // waiting per to send next
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
				PendingBehaviourType2=this.BehaviourType2 ;
				CurrentMessage_list.clear();
				//CurrentMessage=null;
				Sending=false;
				SendingReciving_External= false ; SendingReciving_internal=false; 
				
				
				System.out.println("                                                                                              "+this.Id + "2 )...I will save for  pending.........================================================================..........Radio " );
				

				switch (this.Role) {		
				case FireIncidentCommander:
					Action=RespondersActions.Noaction;
					BehaviourType2=RespondersBehaviourTypes2.WaitingDuring;
					break;
				case FireSectorCommander:
					if (CurrentAssignedMajorActivity_fr==Fire_ActivityType.SearchandRescueCasualty)
						CurrentCalssRole4.TaskApproach_way1() ;
					else
						CurrentCalssRole4.TaskApproach_way2() ;
					break;
				case FireCommunicationsOfficer:
					Action=RespondersActions.Noaction;
					BehaviourType2=RespondersBehaviourTypes2.WaitingDuring;
					break;			
				case None:
					//Action=RespondersActions.Noaction;
					//BehaviourType=RespondersBehaviourTypes.WaitingDuring;
					break;}
			}

		}
		//*************************************************************************	
		//Trace		
		if (( Actionold ==Action  && Actionold != RespondersActions.Noaction && Role!= Fire_ResponderRole.None && Role!= Fire_ResponderRole.Driver &&  Role!= Fire_ResponderRole.FireCommunicationsOfficer) 
				|| 
				(  Actionold ==Action  && Role== Fire_ResponderRole.FireSectorCommander  )

				) //( Actionold ==Action  && Actionold != RespondersActions.Noaction && Role== Fire_ResponderRole.FireFighter   )

			this.counterofaction ++;
		else
		{
			counterofaction=0;
			Actionold =Action ;
		}

		if ( counterofaction== InputFile.counterofaction  && Counter< 10) 
		{
			Counter ++;
			System.out.println(" " );
			System.out.println(" &&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&  Attention/Error &&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&   "  + this.Role  );
			System.out.println(Id +"  "+  "  Action: " + Action + " com:" + CommTrigger + "   Nt:" + InterpretedTrigger+ " SensingeEnvir:" +SensingeEnvironmentTrigger + " end:" + EndActionTrigger +"  Acknowledged: " + Acknowledged + "   Sending:" + this.Sending +"  "+ ActivityAcceptCommunication );

			if ( this.Role==Fire_ResponderRole.FireSectorCommander )
			{
				System.out.println(this.CurrentCalssRole4.UnoccupiedResponders.size() +"    "  + this.CurrentCalssRole4.IsAllTaskclosed1(Fire_TaskType.CarryCasualtytoCCS) );

				if ( this.CurrentCalssRole4.IsAllTaskclosed1(Fire_TaskType.CarryCasualtytoCCS)  ) this.CurrentCalssRole4.AllTaskclosedprint();

				zzz= true;
			}
			
			if ( this.Role==Fire_ResponderRole.FireSectorCommander && Action==RespondersActions.CommandFirefighterClearRoute )
			{
				
				zzz= true;
			}
			
			
			if ( this.Role==Fire_ResponderRole.FireFighter  && Action == RespondersActions.GoToCasualty)
			{
				System.out.println("  "+  "  Action: " + this.OnhandCasualty.IcanMove +  " " + OnhandCasualty.ID +"     "+  OnhandCasualty.CurrentRPM  +"   " + OnhandCasualty.Triage_tage   +"   " + OnhandCasualty.Status  +"   " + OnhandCasualty.UnderAction  ); 
			}


			//			if ( waittttt && Actionold == RespondersActions.Noaction)  
			//				{
			//					System.out.println(Id  + "ISRCestablished " + this.CurrentCalssRole2.ISRCestablished+"  ISCCSestablished"+ this.CurrentCalssRole2.ISCCSestablished);
			//					System.out.println(Id  +  " "+ this.Message_inbox.get(this.Message_inbox.size()-1).performative);
			//					System.out.println(Id  +  " "+ this.Message_inbox.get(this.Message_inbox.size()-2).performative);
			//					System.out.println(Id  +  " "+ this.Message_inbox.get(this.Message_inbox.size()-3).performative);
			//				}
			System.out.println(" " );
			counterofaction=0;
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
			BehaviourType2=RespondersBehaviourTypes2.Travlling;
			ActionEffectTrigger =null;
			SensingeEnvironmentTrigger=null;

			leaveStation=this.CurrentTick;
		}
		// ++++++ 2- +++++++
		else if (Action== RespondersActions.TravalingToIncident  && SensingeEnvironmentTrigger== RespondersTriggers.ArrivedIncident)
		{			

			Action=RespondersActions.Noaction;
			BehaviourType2=RespondersBehaviourTypes2.WaitingDuring ;

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
			if (  assignedIncident.ControlArea!= null)
			{
				Assign_DestinationCordon(assignedIncident.ControlArea.Location);
				Action=RespondersActions.GoToControlArea;							
				BehaviourType2=RespondersBehaviourTypes2.RoleAssignment;
			}
			else
			{
				Assign_DestinationCordon(assignedIncident.bluelightflashing_Point);
				Action=RespondersActions.GoToControlArea;							
				BehaviourType2=RespondersBehaviourTypes2.RoleAssignment;
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
				CurrentMessage = new  ACL_Message( ACLPerformative.InformNewResponderArrival ,this , this.assignedIncident.FICcommander ,null , CurrentTick,this.assignedIncident.ComMechanism_inControlArea,1,TypeMesg.Inernal) ;
				CurrentMessage_list.add(CurrentMessage);
				//==================
				Action=RespondersActions.NotifyArrival;
				BehaviourType2=RespondersBehaviourTypes2.Comunication; //RoleAssignment;
				SensingeEnvironmentTrigger=null;
				FiretTimeOnScence=false;
				//System.out.println(Id + " NotifyArrival +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++" );
			}
			else
			{
				Action=RespondersActions.Noaction;
				BehaviourType2=RespondersBehaviourTypes2.RoleAssignment;
				SensingeEnvironmentTrigger=null;
				//System.out.println(Id + "  Waiting  No NotifyArrival " );
			}
		}	
		// ++++++ 4- +++++++
		else if (Action==RespondersActions.NotifyArrival &&  Acknowledged) 
		{
			Action=RespondersActions.Noaction;
			BehaviourType2=RespondersBehaviourTypes2.RoleAssignment;
			Acknowledged=false;Sending=false; 
		}
		// ++++++ 5- +++++++
		else if ( CommTrigger==RespondersTriggers.GetCommandRole  ) 												
		{															
			Action=RespondersActions.GetcammandRole;
			BehaviourType2=RespondersBehaviourTypes2.Comunication; //RoleAssignment;
			EndofCurrentAction=  InputFile.GetCommand_duration  ; 
			CommTrigger=null;
			//System.out.println(Id + " GetCommandRole" );
		}	
		// ++++++ 6- +++++++
		else if ( Action==RespondersActions.GetcammandRole &&  EndActionTrigger==RespondersTriggers.EndingAction) 												
		{		
			CurrentSender.Acknowledg(this) ;	

			Role=CurrentCommandRequest.AssignedRole2;

			switch( Role)	{
			case FireFighter:	
				CurrentCalssRole2= new Fire_FireFighter (this);				
				break;
			case FireSectorCommander:
				CurrentCalssRole4= new Fire_Commander_SC(this );
				if (CurrentCommandRequest.TargetSector!=null )
				{					
					CurrentAssignedMajorActivity_fr=Fire_ActivityType.SearchandRescueCasualty ;
					CurrentCalssRole4.AssignSector(CurrentCommandRequest.TargetSector );
				}
				else if (CurrentCommandRequest.TargetCordon!=null )
				{	
					CurrentAssignedMajorActivity_fr=Fire_ActivityType.ClearRoute ;
					CurrentCalssRole4.AssignCordon(CurrentCommandRequest.TargetCordon ) ;
				}
				break;
			case FireCommunicationsOfficer:
				CurrentCalssRole5= new Fire_Commander_FCO (this);
				break;
			}
			CommTrigger=RespondersTriggers.AssigendRolebyFIC;
			EndActionTrigger=null;

			if ( Role!=  Fire_ResponderRole.FireFighter)  System.out.println(Id +" ..... " +Role);
		}
		//--------------------------------------------------------------------------------------------  HighRankofficer
		// ++++++ 7- +++++++
		else if (Action==RespondersActions.GoToControlArea && SensingeEnvironmentTrigger== RespondersTriggers.ArrivedTargetObject && HighRankofficer== true) 
		{ 			
			//send message to Temp AIC				
			//Responder_Ambulance Comder= CurrentCommanderInScence ( Ambulance_ResponderRole.AmbulanceIncidentCommander ) ;

			CurrentMessage  = new  ACL_Message( ACLPerformative.Command , this, this.assignedIncident.AICcommander ,null , this.CurrentTick,this.assignedIncident.ComMechanism_inControlArea ,1,TypeMesg.Inernal) ;
			CurrentMessage_list.add(CurrentMessage);
			//==================
			Action=RespondersActions.instructTemoAICtohandover ;
			BehaviourType2=RespondersBehaviourTypes2.Comunication;
			SensingeEnvironmentTrigger=null;

		}
		// ++++++ 8- +++++++
		else if (Action==RespondersActions.instructTemoAICtohandover  &&  Acknowledged) 
		{
			Action=RespondersActions.Noaction;
			BehaviourType2=RespondersBehaviourTypes2.WaitingDuring ;
			Acknowledged=false;Sending=false; 
		}
		// ++++++ 9- +++++++
		else if ( CommTrigger==RespondersTriggers.GetHandoverReport	  ) 	//Actual no content send 											
		{															
			Action=RespondersActions.GetHandoverReport;
			BehaviourType2=RespondersBehaviourTypes2.Comunication;
			EndofCurrentAction=  InputFile.GetInfromation_FtoForR_Duration_Data  ; 
			CommTrigger=null;
		}	

		// ++++++ 10- +++++++
		else if ( Action==RespondersActions.GetHandoverReport &&  EndActionTrigger==RespondersTriggers.EndingAction) 												
		{		

			CurrentSender.Acknowledg(this) ;	
			Role=Fire_ResponderRole.FireIncidentCommander;
			CurrentCalssRole3= new Fire_Commander_FIC (this);
			//CurrentCalssRole3.Commander_AIC_HandoverAction ( (Responder_Fire) CurrentSender )  ;	 //from TempAIC	
			CurrentCalssRole3.UnoccupiedResponders.add((Responder_Fire) CurrentSender );

			CommTrigger=RespondersTriggers.GetHandoverReport;
			EndActionTrigger=null;

			System.out.println(Id +" HRP " +Role);
		}
		// +++++++++++++

	}//end 

	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	// FireFighter- Go back to CA 
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^	
	public void  FireFighterBehavior_GoBackToControlArea()	
	{		

		// ++++++ 1- +++++++
		if( InterpretedTrigger== RespondersTriggers.DoneActivity  )
		{	

			if ( assignedIncident.ControlArea!= null)
			{
				Assign_DestinationCordon(assignedIncident.ControlArea.Location);
				Action=RespondersActions.GoToControlArea;							
				BehaviourType2=RespondersBehaviourTypes2.RoleAssignment;
			}
			else
			{
				Assign_DestinationCordon(assignedIncident.bluelightflashing_Point);
				Action=RespondersActions.GoToControlArea;							
				BehaviourType2=RespondersBehaviourTypes2.RoleAssignment;
			}

			InterpretedTrigger= null;Notyetinfrom=true;

			//System.out.println(Id + "  Waiting  No NotifyArrival Go back aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa ");
		}
		// ++++++ 2- +++++++
		else if (Action==RespondersActions.GoToControlArea  && SensingeEnvironmentTrigger==null)
		{	
			Walk();
		}
		// ++++++ 3- +++++++
		else if (( Action==RespondersActions.GoToControlArea && SensingeEnvironmentTrigger== RespondersTriggers.ArrivedTargetObject)||
				(  Action==RespondersActions.Noaction && Check_CommnderIsinContralArea(  )  && Notyetinfrom == true  )  )				 
		{ 			
			//send message to FIC				
			if ( Check_CommnderIsinContralArea(  )   )
			{
				CurrentMessage = new  ACL_Message( ACLPerformative.InformComebackResponderArrival  ,this , assignedIncident.FICcommander ,null , CurrentTick,this.assignedIncident.ComMechanism_inControlArea,1,TypeMesg.Inernal) ;
				CurrentMessage_list.add(CurrentMessage);
				//==================
				Action=RespondersActions.NotifyArrival;
				BehaviourType2=RespondersBehaviourTypes2.Comunication; //RoleAssignment;
				SensingeEnvironmentTrigger=null;
				Notyetinfrom= false;
				//System.out.println(Id + " NotifyArrival " );
			}
			else
			{
				Action=RespondersActions.Noaction;
				BehaviourType2=RespondersBehaviourTypes2.RoleAssignment;
				SensingeEnvironmentTrigger=null;
				//System.out.println(Id + "  Waiting  No NotifyArrival " );
			}
		}	
		// ++++++ 4- +++++++
		else if (Action==RespondersActions.NotifyArrival && Acknowledged) 
		{			

			//System.out.println(Id + "   NotifyArrival Go back %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%" );

			Action=RespondersActions.Noaction;
			BehaviourType2=RespondersBehaviourTypes2.RoleAssignment;
			Acknowledged=false;Sending=false; 
		}
		// ++++++ 5- +++++++
		else if ( CommTrigger==RespondersTriggers.GetCommandRole  ) 												
		{															
			Action=RespondersActions.GetcammandRole;
			BehaviourType2=RespondersBehaviourTypes2.Comunication; //RoleAssignment;
			EndofCurrentAction=  InputFile.GetCommand_duration  ; 
			CommTrigger=null;
			//System.out.println(Id + " GetCommandRole" );
		}	
		// ++++++ 6- +++++++
		else if ( Action==RespondersActions.GetcammandRole &&  EndActionTrigger==RespondersTriggers.EndingAction) 												
		{		
			CurrentSender.Acknowledg(this) ;	

			Role=CurrentCommandRequest.AssignedRole2;

			switch( Role)	{
			case FireFighter:	
				CurrentCalssRole2= new Fire_FireFighter (this);				
				break;
			case FireSectorCommander:
				CurrentCalssRole4= new Fire_Commander_SC(this );
				if (CurrentCommandRequest.TargetSector!=null )
				{					
					CurrentAssignedMajorActivity_fr=Fire_ActivityType.SearchandRescueCasualty ;
					CurrentCalssRole4.AssignSector(CurrentCommandRequest.TargetSector );
				}
				else if (CurrentCommandRequest.TargetCordon!=null )
				{	
					CurrentAssignedMajorActivity_fr=Fire_ActivityType.ClearRoute ;
					CurrentCalssRole4.AssignCordon(CurrentCommandRequest.TargetCordon ) ;
				}
				break;
			case FireCommunicationsOfficer:
				CurrentCalssRole5= new Fire_Commander_FCO (this);
				break;
			}
			CommTrigger=RespondersTriggers.AssigendRolebyFIC;
			EndActionTrigger=null;

			if ( Role!=  Fire_ResponderRole.FireFighter)  System.out.println(Id +" ..... " +Role);
		}
	}

	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	// Responders- Go To Vehicle after finish 
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	private void GeneralResponderBehavior_GoToStation()	
	{

		// ++++++ 1- +++++++
		if (CommTrigger==RespondersTriggers.ENDER   )
		{	
			Action=RespondersActions.GetNotificationEndER;
			BehaviourType2=RespondersBehaviourTypes2.Comunication ;   
			EndofCurrentAction=  InputFile.GetInfromation_FtoForR_Duration_Notification ;
			CommTrigger=null;	
		}
		// ++++++ 2- +++++++
		else if (( Action==RespondersActions.GetNotificationEndER && EndActionTrigger==RespondersTriggers.EndingAction)  ||  InterpretedTrigger == RespondersTriggers.ENDER )//
		{	
			Assign_DestinationVehicle(Myvehicle);

			Action=RespondersActions.GoToVehicle;
			BehaviourType2=RespondersBehaviourTypes2.Travlling;
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
			BehaviourType2=RespondersBehaviourTypes2.Travlling;
			SensingeEnvironmentTrigger=null;

		}
		// ++++++ 5- +++++++
		else if (Action==RespondersActions.OnVehicle  && SensingeEnvironmentTrigger==RespondersTriggers.Engineisrunning)
		{ 
			Action=RespondersActions.TravalingToBaseStation;
			BehaviourType2=RespondersBehaviourTypes2.Travlling;
			SensingeEnvironmentTrigger=null;

		}
		// ++++++ 6- +++++++
		else if (Action==RespondersActions.TravalingToBaseStation  && SensingeEnvironmentTrigger==RespondersTriggers.ArrivedBaseStation  )
		{
			Action=RespondersActions.Idle;
			BehaviourType2=RespondersBehaviourTypes2.Done;
			SensingeEnvironmentTrigger=null;
			Busy=false;
			BacktoStation= CurrentTick;

			System.out.println("Fire Responder" + Id + "   ......bey bey "  );
		}
	}


}//end class


