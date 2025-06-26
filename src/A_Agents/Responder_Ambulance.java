package A_Agents;

import java.util.ArrayList;
import java.util.List;
import com.vividsolutions.jts.geom.Coordinate;
import A_Environment.PointDestination;
import A_Roles_Ambulance.Ambulance_Commander_ACO;
import A_Roles_Ambulance.Ambulance_Commander_AIC;
import A_Roles_Ambulance.Ambulance_Commander_ALC;
import A_Roles_Ambulance.Ambulance_Commander_CCO;
import A_Roles_Ambulance.Ambulance_Commander_SC;
import A_Roles_Ambulance.Ambulance_Driver;
import A_Roles_Ambulance.Ambulance_Paramedic;
import B_Communication.ACL_Message;
import B_Communication.Command;
import B_Communication.ISRecord;
import C_SimulationInput.InputFile;
import C_SimulationOutput.BehRec;
import D_Ontology.BuildStaticFuction;
import D_Ontology.Ontology;
import D_Ontology.Ontology.ACLPerformative;
import D_Ontology.Ontology.Ambulance_ActivityType;
import D_Ontology.Ontology.Ambulance_ResponderRole;
import D_Ontology.Ontology.Ambulance_TaskType;
import D_Ontology.Ontology.CommunicationMechanism;
import D_Ontology.Ontology.Fire_ResponderRole;
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

public class Responder_Ambulance extends Responder {

	public static  int Counter=0;

	public Ambulance_ResponderRole  Role ;
	public boolean HighRankofficer=false;
	public  Ambulance_Driver  CurrentCalssRole1;
	public  Ambulance_Paramedic  CurrentCalssRole2;		
	public  Ambulance_Commander_AIC CurrentCalssRole3;
	public  Ambulance_Commander_SC CurrentCalssRole4;	
	public  Ambulance_Commander_ALC CurrentCalssRole5;
	public  Ambulance_Commander_ACO CurrentCalssRole6;	
	public  Ambulance_Commander_CCO CurrentCalssRole7;

	public Ambulance_TaskType  CurrentAssignedActivity ,    xxxx;

	//-----------------------------------------
	public boolean zzzz=false;
	public boolean OnScence=false ,IaminScenceSector=false , FiretTimeOnScence=true;
	public int  SectorNeedstoRespond=0;
	boolean Notyetinfrom ;
	//----------------------------------------- trace hang
	int counterofaction=0 ;
	public Ontology.RespondersActions Actionold;
	//-----------------------------------------
	public ArrayList<Responder> CurrentResponders_list = new ArrayList<Responder>() ;
	public  ArrayList<Integer> NumberofCasualtyinLRUD_list  = new ArrayList<>(); //Initiator

	public Responder_Ambulance CurrentResItalktohimxxxx= null;  //trace onlly

	//##############################################################################################################################################################	
	public Responder_Ambulance(Context<Object> _context, Geography<Object> _geography, Coordinate initialLocation, String ID, Vehicle_Ambulance vehicle)
	{
		super(_context, _geography, initialLocation, ID, vehicle);

		this.Role= Ambulance_ResponderRole.None;
		this.ColorCode= 0;
		this.step_long =InputFile.step_long_regularewalk;

		this.Action=RespondersActions.Idle;
		this.BehaviourType1=RespondersBehaviourTypes1.Idle;		
		CurrentAssignedActivity=Ambulance_TaskType.None;
		CurrentAssignedMajorActivity_amb =Ambulance_ActivityType.None;

	}

	public Ambulance_TaskType  ReturnCuurnttask() 
	{
		return this.CurrentAssignedActivity ;
	}

	public Ambulance_ResponderRole getRole() {
		return this.Role ;
	}

	public void setRole(Ambulance_ResponderRole R) {
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

			if ( CurrentSender instanceof Responder_Ambulance &&  CurrentCommandRequest.commandType1 == null && CurrentCommandRequest.AssignedRole1!=null )  				
				CommTrigger= RespondersTriggers.GetCommandRole;
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
	public ArrayList<Responder_Ambulance> CurrentAmbulanceInScence ( ) 
	{
		ArrayList<Responder_Ambulance> CurrentDriver_list = new ArrayList<Responder_Ambulance>() ;


		@SuppressWarnings("unchecked") 
		List<Vehicle_Ambulance> nearObjects_vehicle= (List<Vehicle_Ambulance>) BuildStaticFuction.GetObjectsWithinDistance(this,Vehicle_Ambulance.class, 15 );


		for (int i = 0; i < nearObjects_vehicle.size(); i++) 
			if (nearObjects_vehicle.get(i).Action!= VehicleAction.WaitingRequest ||    ((Responder_Ambulance) nearObjects_vehicle.get(i).AssignedDriver).Role != Ambulance_ResponderRole.Driver ) {
				nearObjects_vehicle.remove(i);
				i--; 
			}	

		CurrentDriver_list.clear();

		for (Vehicle  V :nearObjects_vehicle)
			CurrentDriver_list.add((Responder_Ambulance) V.AssignedDriver);

		return  CurrentDriver_list;
	}

	//----------------------------------------------------------------------------------------------------
	public Responder_Ambulance  CurrentCommanderInScence ( Ambulance_ResponderRole  Role  ) 
	{
		Responder_Ambulance Commander=null ;

		//1- Get All Near Responder Objects
		@SuppressWarnings("unchecked") 
		List<Responder_Ambulance> nearObjects_Responder= (List<Responder_Ambulance>) BuildStaticFuction.GetObjectsWithinDistance(this,Responder_Ambulance.class, 5);// responder of ambulance

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
		List<Responder_Ambulance> nearObjects_Responder= (List<Responder_Ambulance>) BuildStaticFuction.GetObjectsWithinDistance(loc,Responder_Ambulance.class, 10); //3

		boolean result=false;

		for (int i = 0; i < nearObjects_Responder.size(); i++) 
			if (nearObjects_Responder.get(i).Role== Ambulance_ResponderRole.AmbulanceIncidentCommander  && nearObjects_Responder.get(i).ActivityAcceptCommunication==true) 
			{result=true;break ;}


		nearObjects_Responder.clear();
		nearObjects_Responder=null;

		return result;
	}

	//----------------------------------------------------------------------------------------------------
	public boolean Check_CommnderIsinAL(  ) 
	{

		double dis= BuildStaticFuction.DistanceC(geography, this.Return_CurrentLocation(), this.assignedIncident.ALCcommander.Return_CurrentLocation());

		if ( dis >= InputFile.FacetoFaceLimit  )  // I can add no obstacle
			return false;
		else
			return true;

	}

	//______________________________________________________________________________________________________________________________________________________________
	//##############################################################################################################################################################	
	//													Responders Behavior
	//##############################################################################################################################################################	
	@ScheduledMethod(start = 1, interval = 1 ,priority= ScheduleParameters.LAST_PRIORITY)
	public void SimulationResult() {

		//LogFile.AddAction(Role, Action, BehaviourType, CurrentTick);   //each tick
		//LogFile.AddActionSummery(Role, Action, BehaviourType, CurrentTick);   // each action
		//LogFile.AddBehSummery_Amb(Role, Action, BehaviourType1, CurrentTick);   //each Behaviour   this one used 

		LogFile.IncrmentBeh( Role, BehaviourType1 ,null, null,null ,null);

		if (  BehaviourType1==RespondersBehaviourTypes1.Comunication &&  SendingReciving_internal==true && SendingReciving_External==false)
			Comunication_internal ++;
		else if (  BehaviourType1==RespondersBehaviourTypes1.Comunication && SendingReciving_External== true && SendingReciving_internal==false )
			Comunication_External++;
		else if (  BehaviourType1==RespondersBehaviourTypes1.Comunication )
				System.out.println("______________________________________________________________________________________________________________________________________________ "+this.Id  + this.Role+ this.Action);


		if (  BehaviourType1==RespondersBehaviourTypes1.ComunicationDelay  &&  SendingReciving_internal==true && SendingReciving_External==false)
			ComunicationDelay_internal++;
		else if (  BehaviourType1==RespondersBehaviourTypes1.ComunicationDelay  && SendingReciving_External== true && SendingReciving_internal==false )
			ComunicationDelay_External++;
		else if (  BehaviourType1==RespondersBehaviourTypes1.ComunicationDelay )
				System.out.println("______________________________________________________________________________________________________________________________________________ "+this.Id  + this.Role + this.Action);

	}


	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	// Responder - General 
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	//@ScheduledMethod(start = 1, interval = 1 , priority= 22)
	public void step() {

		CurrentTick=schedule.getTickCount() ;

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
			case Paramedic:	
				CurrentCalssRole2.Paramedic_InterpretationMessage();
				break;
			case AmbulanceIncidentCommander:
				CurrentCalssRole3.CommanderAIC_InterpretationMessage();
				break;
			case AmbulanceSectorCommander:
				CurrentCalssRole4.CommanderSC_InterpretationMessage();
				break;
			case AmbulanceLoadingCommander:
				CurrentCalssRole5.CommanderALC_InterpretationMessage();
				break;
			case AmbulanceCommunicationsOfficer:
				CurrentCalssRole6.Commander_ACO_InterpretationMessage();
				break;	
			case CasualtyClearingOfficer:
				CurrentCalssRole7.CommanderCCO_InterpretationMessage();
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
		case Paramedic:	
			CurrentCalssRole2.ParamedicBehavior(); //during assign role
			break;
		case AmbulanceIncidentCommander:
			CurrentCalssRole3.AICBehavior();// tow times 1- by itself
			break;
		case AmbulanceSectorCommander:
			CurrentCalssRole4.SCBehavior(); //during assign role
			break;
		case AmbulanceLoadingCommander: 
			CurrentCalssRole5.ALCBehavior(); //during assign role
			break;
		case AmbulanceCommunicationsOfficer: 
			CurrentCalssRole6.ACOBehavior()	;  // ?????????????????????????
			break;
		case CasualtyClearingOfficer:
			CurrentCalssRole7.CCOBehavior()	;
			break;
		case None:
			//=================================================		
			if (ActionEffectTrigger == RespondersTriggers.Alarm999)
			{
				CurrentAssignedActivity=Ambulance_TaskType.TravaltoIncident;
			}
			else if (CurrentAssignedActivity==Ambulance_TaskType.TravaltoIncident  && SensingeEnvironmentTrigger== RespondersTriggers.FirstArrival_NoResponder )
			{				
				if (this.assignedIncident.AICcommander==null)
				{
					Role=Ambulance_ResponderRole.AmbulanceIncidentCommander;
					CurrentCalssRole3= new  Ambulance_Commander_AIC (this);
					System.out.println(Id +" ..... " +Role);
				}
				else //Third Attendant
				{
					CurrentAssignedActivity=Ambulance_TaskType.InitialResponse;
					InterpretedTrigger= RespondersTriggers.ThridAttandant ;
					SensingeEnvironmentTrigger= null;
				}

			}
			else if ( CurrentAssignedActivity==Ambulance_TaskType.TravaltoIncident  && SensingeEnvironmentTrigger== RespondersTriggers.NotFirstArrival_ThereisResponder )
			{
				CurrentAssignedActivity=Ambulance_TaskType.InitialResponse;
			}
			else if ( InterpretedTrigger == RespondersTriggers.DoneActivity  ) // for commander when they become none
			{
				CurrentAssignedActivity=Ambulance_TaskType.GoBackToControlArea ;
			}
			else if (CommTrigger==RespondersTriggers.ENDER || InterpretedTrigger == RespondersTriggers.ENDER ) 
			{
				CurrentAssignedActivity=Ambulance_TaskType.TravaltoBaseStation;

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
				ParamedicBehavior_GoBackToControlArea() ; 	
				break;
			case TravaltoBaseStation :
				GeneralResponderBehavior_GoToStation()	;	
				break;}
			break;
			//=================================================	
		}
		//*************************************************************************	
		//Part Three

		if (PendingMessage_list.size()!=0  && Action==RespondersActions.Noaction)  //
		{
			for (  ACL_Message  Msg : PendingMessage_list  )
				CurrentMessage_list.add(Msg);
			this.Action=PendingSendingAction ;
			this.BehaviourType1 =PendingBehaviourType1 ;	
			PendingMessage_list.clear();


			System.out.println("                                                                                              "+this.Id + "  3 )...I will send pending.........================================================================..........Radio " );

		}


		if (  CurrentMessage_list.size()!=0 ) //until send
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
				PendingBehaviourType1=this.BehaviourType1 ;
				CurrentMessage_list.clear();
				Sending=false;
				SendingReciving_External= false ; SendingReciving_internal=false; 

				System.out.println("                                                                                              "+this.Id + "  2 )...I will save for  pending.........================================================================..........Radio " );


				switch (this.Role) {		
				case AmbulanceIncidentCommander:
					Action=RespondersActions.Noaction;
					BehaviourType1=RespondersBehaviourTypes1.WaitingDuring;
					break;
				case AmbulanceSectorCommander:
					CurrentCalssRole4.TaskApproach_way() ;
					break;
				case AmbulanceLoadingCommander:
					Action=RespondersActions.Noaction;
					BehaviourType1=RespondersBehaviourTypes1.WaitingDuring;
					break;
				case AmbulanceCommunicationsOfficer:
					Action=RespondersActions.Noaction;
					BehaviourType1=RespondersBehaviourTypes1.WaitingDuring;
					break;
				case CasualtyClearingOfficer:
					Action=RespondersActions.Noaction;
					BehaviourType1=RespondersBehaviourTypes1.WaitingDuring;
					break;
				case None:
					//Action=RespondersActions.Noaction;
					//BehaviourType=RespondersBehaviourTypes.WaitingDuring;
					break;}
			}


		}
		//*************************************************************************	
		//Trace		
		if ((  Actionold ==Action && Actionold != RespondersActions.Noaction  && Role!= Ambulance_ResponderRole.None &&  Action!= RespondersActions.Idle &&
				Role!= Ambulance_ResponderRole.AmbulanceCommunicationsOfficer ) || 
				(  Actionold ==Action  && Role== Ambulance_ResponderRole.CasualtyClearingOfficer  ) || 
				(  Actionold ==Action  && Role== Ambulance_ResponderRole.AmbulanceSectorCommander )  )//&& ( this.AssignedSector !=null) &&  Role!= Ambulance_ResponderRole.Paramedic  &&  Role!= Ambulance_ResponderRole.Paramedic
			this.counterofaction ++;
		else
		{
			counterofaction=0;
			Actionold =Action ;			
			xxxx= CurrentAssignedActivity ;    
		}

		if ( counterofaction== InputFile.counterofaction  && Counter<10)  //1500
		{
			Counter ++;
			System.out.println(" " );
			System.out.println(" &&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&& Attention/Error &&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&     " + this.Role + this.CurrentAssignedMajorActivity_amb +"    "  + this.CurrentAssignedActivity );
			System.out.println(Id  +  "  Action: " + Action + " com:" + CommTrigger + "   Nt:" + InterpretedTrigger+ " SensingeEnvir:" +SensingeEnvironmentTrigger + " end:" + EndActionTrigger +"  Acknowledged: " + Acknowledged + "   Sending:" + this.Sending +"  "+ ActivityAcceptCommunication +"  "+  this.Message_inbox.get(this.Message_inbox.size()-1 ).performative );

			//if ( CurrentResItalktohimxxxx !=null)
			//	System.out.println(CurrentResItalktohimxxxx.Id  +  "  Action: " + CurrentResItalktohimxxxx.Action + " com:" + CurrentResItalktohimxxxx.CommTrigger + "   Nt:" + CurrentResItalktohimxxxx.InterpretedTrigger+ " en:" +CurrentResItalktohimxxxx.SensingeEnvironmentTrigger + " end:" + CurrentResItalktohimxxxx.EndActionTrigger +"   " + CurrentResItalktohimxxxx.Acknowledged +CurrentResItalktohimxxxx.Role + CurrentResItalktohimxxxx.CurrentAssignedMajorActivity +"    "  + CurrentResItalktohimxxxx.CurrentAssignedActivity  );

			if (Role== Ambulance_ResponderRole.CasualtyClearingOfficer ) this.zzz=true;

			if (Role== Ambulance_ResponderRole.Driver )zzz=true ;

			if ( ( this.Role==Ambulance_ResponderRole.Paramedic  && Action == RespondersActions.GoToCasualty)   || ( this.Role==Ambulance_ResponderRole.Driver  && Action == RespondersActions.loadCasualtyToVehicle)   )
			{
				System.out.println("  "+  "  move: " + this.OnhandCasualty.IcanMove +  " " + OnhandCasualty.ID +"     "+  OnhandCasualty.CurrentRPM  +"   " + OnhandCasualty.Triage_tage   +"   " + OnhandCasualty.Status  +"   " + OnhandCasualty.UnderAction  ); 


				for(ISRecord     Rec: assignedIncident.ISSystem) 
					System.out.println( Rec.CasualtyinRec.ID +" -------- " + Rec.priority_levelinRec +"   " + Rec.AssignedAmbulanceinRec  +"  "  + Rec.AssignedHospitalinRec +"  "  +  Rec.SecondTriaged+ " treat"  +  Rec.Treated+ "     "   + Rec.Pre_treatment 
							+"   DFT:" +Rec.checkedSC_DoneFieldTriage+"   DST:"  +Rec.checkedCCO_DoneSecondTriage +"   AIC_DST:"  +Rec.checkedAIC_DoneSecondTriage+" DT: "+ Rec.checkedCCO_DoneTreatment  +"  ALC:"  +Rec.checkedALC_AllocateAmb  + Rec.checkedCCO_DoneHosandAmb  );



			}

			if (this.OnhandCasualty!=null  )System.out.println( this.Id + "OnhandCasualty  " + this.OnhandCasualty.ID +"   " );
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
			BehaviourType1=RespondersBehaviourTypes1.Travlling;
			ActionEffectTrigger =null;
			SensingeEnvironmentTrigger=null;

			leaveStation=this.CurrentTick;
		}
		// ++++++ 2- +++++++
		else if (Action== RespondersActions.TravalingToIncident  && SensingeEnvironmentTrigger== RespondersTriggers.ArrivedIncident)
		{			

			Action=RespondersActions.Noaction;
			BehaviourType1=RespondersBehaviourTypes1.WaitingDuring ;

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
				BehaviourType1=RespondersBehaviourTypes1.RoleAssignment;
			}
			else
			{
				Assign_DestinationCordon(assignedIncident.bluelightflashing_Point);
				Action=RespondersActions.GoToControlArea;							
				BehaviourType1=RespondersBehaviourTypes1.RoleAssignment;
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
				CurrentMessage = new  ACL_Message( ACLPerformative.InformNewResponderArrival ,this , this.assignedIncident.AICcommander ,null , CurrentTick, this.assignedIncident.ComMechanism_inControlArea,1,TypeMesg.Inernal) ;
				CurrentMessage_list.add(CurrentMessage);
				//==================
				Action=RespondersActions.NotifyArrival;
				BehaviourType1=RespondersBehaviourTypes1.Comunication; //RoleAssignment;
				SensingeEnvironmentTrigger=null;
				FiretTimeOnScence=false;
				//System.out.println(Id + " NotifyArrival " );
			}
			else
			{
				Action=RespondersActions.Noaction;
				BehaviourType1=RespondersBehaviourTypes1.RoleAssignment;
				SensingeEnvironmentTrigger=null;

			}
		}	
		// ++++++ 4- +++++++
		else if (Action==RespondersActions.NotifyArrival &&  Acknowledged) 
		{
			Action=RespondersActions.Noaction;
			BehaviourType1=RespondersBehaviourTypes1.RoleAssignment;
			Acknowledged=false;Sending=false; 

			//System.out.println(Id + "  done NotifyArrival " );
		}
		// ++++++ 5- +++++++
		else if ( CommTrigger==RespondersTriggers.GetCommandRole  ) 												
		{															
			Action=RespondersActions.GetcammandRole;
			BehaviourType1=RespondersBehaviourTypes1.Comunication; //RoleAssignment;
			EndofCurrentAction=  InputFile.GetCommand_duration  ; 
			CommTrigger=null;
			//System.out.println(Id + " GetCommandRole" );
		}	
		// ++++++ 6- +++++++
		else if ( Action==RespondersActions.GetcammandRole &&  EndActionTrigger==RespondersTriggers.EndingAction) 												
		{		
			CurrentSender.Acknowledg(this) ;	

			Role=CurrentCommandRequest.AssignedRole1;

			switch( Role)	{
			case Paramedic:	
				CurrentCalssRole2= new Ambulance_Paramedic (this);	
				break;
			case AmbulanceSectorCommander:
				CurrentCalssRole4= new  Ambulance_Commander_SC (this,CurrentCommandRequest.TargetSector );
				break;
			case AmbulanceLoadingCommander :
				CurrentCalssRole5= new  Ambulance_Commander_ALC (this,CurrentCommandRequest.TargetTA, CurrentCommandRequest.DriverList);
				break;
			case AmbulanceCommunicationsOfficer:
				CurrentCalssRole6= new  Ambulance_Commander_ACO (this);
				break;
			case CasualtyClearingOfficer:
				CurrentCalssRole7= new  Ambulance_Commander_CCO (this,CurrentCommandRequest.TargetTA);
				break;
			}

			CommTrigger=RespondersTriggers.AssigendRolebyAIC;
			EndActionTrigger=null;

			if ( Role!=  Ambulance_ResponderRole.Paramedic ) System.out.println(Id +" ..... " +Role);
		}
		//--------------------------------------------------------------------------------------------  HighRankofficer
		// ++++++ 7- +++++++
		else if (Action==RespondersActions.GoToControlArea && SensingeEnvironmentTrigger== RespondersTriggers.ArrivedTargetObject && HighRankofficer== true) 
		{ 			
			//send message to Temp AIC				
			//Responder_Ambulance Comder= CurrentCommanderInScence ( Ambulance_ResponderRole.AmbulanceIncidentCommander ) ;

			CurrentMessage  = new  ACL_Message( ACLPerformative.Command , this, this.assignedIncident.AICcommander ,null , this.CurrentTick,this.assignedIncident.ComMechanism_inControlArea,1,TypeMesg.Inernal ) ;
			CurrentMessage_list.add(CurrentMessage);
			//==================
			Action=RespondersActions.instructTemoAICtohandover ;
			BehaviourType1=RespondersBehaviourTypes1.Comunication ;   
			SensingeEnvironmentTrigger=null;

		}
		// ++++++ 8- +++++++
		else if (Action==RespondersActions.instructTemoAICtohandover  &&  Acknowledged) 
		{
			Action=RespondersActions.Noaction;
			BehaviourType1=RespondersBehaviourTypes1.WaitingDuring;
			Acknowledged=false;Sending=false; 
		}
		// ++++++ 9- +++++++
		else if ( CommTrigger==RespondersTriggers.GetHandoverReport	  ) 	//Actual no content send 											
		{															
			Action=RespondersActions.GetHandoverReport;
			BehaviourType1=RespondersBehaviourTypes1.Comunication;   
			EndofCurrentAction=  InputFile.GetInfromation_FtoForR_Duration_Data  ; 
			CommTrigger=null;
		}	

		// ++++++ 10- +++++++
		else if ( Action==RespondersActions.GetHandoverReport &&  EndActionTrigger==RespondersTriggers.EndingAction) 												
		{		

			CurrentSender.Acknowledg(this) ;	
			Role=Ambulance_ResponderRole.AmbulanceIncidentCommander;
			CurrentCalssRole3= new  Ambulance_Commander_AIC (this);
			CurrentCalssRole3.Commander_AIC_HandoverAction ( (Responder_Ambulance) CurrentSender )  ;	 //from TempAIC	
			CurrentCalssRole3.UnoccupiedResponders.add((Responder_Ambulance) CurrentSender );

			CommTrigger=RespondersTriggers.GetHandoverReport;
			EndActionTrigger=null;

			System.out.println(Id +" HRP " +Role);
		}
		// +++++++++++++

	}//end 

	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	// Policeman- Go back to CA 
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^	
	public void  ParamedicBehavior_GoBackToControlArea()	
	{		
		// ++++++ 1- +++++++
		if( InterpretedTrigger == RespondersTriggers.DoneActivity  )  // or done activity
		{	
			if (  assignedIncident.ControlArea!= null)
			{
				Assign_DestinationCordon(assignedIncident.ControlArea.Location);
				Action=RespondersActions.GoToControlArea;							
				BehaviourType1=RespondersBehaviourTypes1.RoleAssignment;
			}
			else
			{
				Assign_DestinationCordon(assignedIncident.bluelightflashing_Point);
				Action=RespondersActions.GoToControlArea;							
				BehaviourType1=RespondersBehaviourTypes1.RoleAssignment;
			}

			InterpretedTrigger= null; Notyetinfrom=true;
		}
		// ++++++ 2- +++++++
		else if (Action==RespondersActions.GoToControlArea  && SensingeEnvironmentTrigger==null)
		{	
			Walk();
		}
		// ++++++ 3- +++++++
		else if (( Action==RespondersActions.GoToControlArea && SensingeEnvironmentTrigger== RespondersTriggers.ArrivedTargetObject)||
				(  Action==RespondersActions.Noaction && Check_CommnderIsinContralArea(  )  && Notyetinfrom == true )  )				 
		{ 			
			//send message to AIC				
			if ( Check_CommnderIsinContralArea(  )   )
			{
				CurrentMessage = new  ACL_Message( ACLPerformative.InformComebackResponderArrival  , this, assignedIncident.AICcommander ,null , CurrentTick,this.assignedIncident.ComMechanism_inControlArea,1,TypeMesg.Inernal) ;				
				CurrentMessage_list.add(CurrentMessage);

				//==================
				Action=RespondersActions.NotifyArrival;
				BehaviourType1=RespondersBehaviourTypes1.Comunication; //RoleAssignment;
				SensingeEnvironmentTrigger=null;
				Notyetinfrom= false;
				//System.out.println(Id + " NotifyArrival " );
			}
			else
			{
				Action=RespondersActions.Noaction;
				BehaviourType1=RespondersBehaviourTypes1.RoleAssignment;
				SensingeEnvironmentTrigger=null;
				//System.out.println(Id + "  Waiting  No NotifyArrival =================================================================" );
			}
		}	
		// ++++++ 4- +++++++
		else if (Action==RespondersActions.NotifyArrival &&  Acknowledged) 
		{

			//System.out.println(Id + "  NotifyArrival Go back %%%%%%%%%%%%%%%%%%%%1%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%" );

			Action=RespondersActions.Noaction;
			BehaviourType1=RespondersBehaviourTypes1.RoleAssignment;
			Acknowledged=false;Sending=false; 
		}
		// ++++++ 5- +++++++
		else if ( Action==RespondersActions.Noaction  && CommTrigger==RespondersTriggers.GetCommandRole  ) 												
		{															
			Action=RespondersActions.GetcammandRole;
			BehaviourType1=RespondersBehaviourTypes1.Comunication; //RoleAssignment;
			EndofCurrentAction=  InputFile.GetCommand_duration  ; 
			CommTrigger=null;
			//System.out.println(Id + " GetCommandRole %%%%%%%%%%%%%%%%%%%%%%%2%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%\"" );
		}	
		// ++++++ 6- +++++++
		else if ( Action==RespondersActions.GetcammandRole &&  EndActionTrigger==RespondersTriggers.EndingAction) 												
		{		
			CurrentSender.Acknowledg(this) ;	

			Role=CurrentCommandRequest.AssignedRole1;

			switch( Role)	{
			case Paramedic:	
				CurrentCalssRole2= new Ambulance_Paramedic (this);	
				break;
			case AmbulanceSectorCommander:
				CurrentCalssRole4= new  Ambulance_Commander_SC (this,CurrentCommandRequest.TargetSector );
				break;
			case AmbulanceLoadingCommander :
				CurrentCalssRole5= new  Ambulance_Commander_ALC (this,CurrentCommandRequest.TargetTA, CurrentCommandRequest.DriverList);
				break;
			case AmbulanceCommunicationsOfficer:
				CurrentCalssRole6= new  Ambulance_Commander_ACO (this);
				break;
			case CasualtyClearingOfficer:
				CurrentCalssRole7= new  Ambulance_Commander_CCO (this,CurrentCommandRequest.TargetTA);
				break;
			}

			CommTrigger=RespondersTriggers.AssigendRolebyAIC;
			EndActionTrigger=null;

			if ( Role!=  Ambulance_ResponderRole.Paramedic ) System.out.println(Id +" ..... " +Role);

			//System.out.println(Id + "  Acknowledg %%%%%%%%%%%%%%%%%%%%%%%3%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%"+ CurrentSender.Id );
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
			BehaviourType1=RespondersBehaviourTypes1.Comunication;
			EndofCurrentAction=  InputFile.GetInfromation_FtoForR_Duration_Notification ;
			CommTrigger=null;	
		}
		// ++++++ 2- +++++++
		else if (( Action==RespondersActions.GetNotificationEndER && EndActionTrigger==RespondersTriggers.EndingAction)  ||  InterpretedTrigger == RespondersTriggers.ENDER )//
		{	
			Assign_DestinationVehicle(Myvehicle);

			Action=RespondersActions.GoToVehicle;
			BehaviourType1=RespondersBehaviourTypes1.Travlling;
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
			BehaviourType1=RespondersBehaviourTypes1.Travlling;
			SensingeEnvironmentTrigger=null;

		}
		// ++++++ 5- +++++++
		else if (Action==RespondersActions.OnVehicle  && SensingeEnvironmentTrigger==RespondersTriggers.Engineisrunning)
		{ 
			Action=RespondersActions.TravalingToBaseStation;
			BehaviourType1=RespondersBehaviourTypes1.Travlling;
			SensingeEnvironmentTrigger=null;

		}
		// ++++++ 6- +++++++
		else if (Action==RespondersActions.TravalingToBaseStation  && SensingeEnvironmentTrigger==RespondersTriggers.ArrivedBaseStation  )
		{
			Action=RespondersActions.Idle;
			BehaviourType1=RespondersBehaviourTypes1.Done;
			SensingeEnvironmentTrigger=null;
			Busy=false;
			BacktoStation= CurrentTick;

			System.out.println("Ambulance Responder" + Id + "   ......bey bey "  );
		}
	}


}//end class

//Part Three
//		if (  CurrentMessage!=null  )  //until send
//		{			
//			Sending=true; SynReceiver = CurrentMessage.receiver ; SynPerformative= CurrentMessage.performative ; Syntime= CurrentMessage.time ;
//			if ( SendMessage( CurrentMessage  ) )   
//			{CurrentMessage=null;}
//		}




//int  SMResult=0;
//
//SMResult=SendMessage( CurrentMessage_list.get(0)  )  ;
//
//if ( SMResult==1 )		
//{CurrentMessage_list.remove(0) ;   }  
