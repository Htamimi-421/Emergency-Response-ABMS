package A_Roles_Police;
import java.util.ArrayList;
import A_Agents.Casualty;
import A_Agents.Responder;
import A_Agents.Responder_Ambulance;
import A_Agents.Responder_Police;
import B_Communication.ACL_Message;
import B_Communication.Command;
import B_Communication.Report;
import C_SimulationInput.InputFile;
import D_Ontology.BuildStaticFuction;
import D_Ontology.Ontology.ACLPerformative;
import D_Ontology.Ontology.CasualtyAction;
import D_Ontology.Ontology.CasualtyReportandTrackingMechanism;
import D_Ontology.Ontology.CasualtyStatus;
import D_Ontology.Ontology.CasualtyinfromationType;
import D_Ontology.Ontology.CommunicationMechanism;
import D_Ontology.Ontology.Level;
import D_Ontology.Ontology.MovmentCasualty;
import D_Ontology.Ontology.Police_ActivityType;
import D_Ontology.Ontology.Police_ResponderRole;
import D_Ontology.Ontology.Police_TaskType;
import D_Ontology.Ontology.RandomWalking_StrategyType;
import D_Ontology.Ontology.RespondersActions;
import D_Ontology.Ontology.RespondersBehaviourTypes2;
import D_Ontology.Ontology.RespondersBehaviourTypes3;
import D_Ontology.Ontology.RespondersTriggers;
import D_Ontology.Ontology.TypeMesg;


public class Police_Policeman  {

	Responder_Police Mybody;

	boolean IamNewResponder=true ;
	boolean IgetNewActivity =false ;

	// SA
	boolean ISDeadinInnerorCCS= true; //Default true in inner
	int Taskoption=0;
	
	ArrayList<Casualty> liaisonofficer_CaList =new ArrayList<Casualty>();
	int t=0;
	public CasualtyAction Temp , PreviousUnderAction;
	double Time_last_updated=0;	
	//##############################################################################################################################################################
	public Police_Policeman ( Responder_Police  _Mybody  ) 
	{
		Mybody=_Mybody;
		Mybody.EndofCurrentAction=0;
		Mybody.PrvRoleinprint3=Mybody.Role;
	}

	//##############################################################################################################################################################
	public void  Policeman_InterpretationMessage()
	{
		boolean  done= true;
		ACL_Message currentmsg =  Mybody.Message_inbox.get(Mybody.Lastmessagereaded);		 			
		Mybody.CurrentSender= (Responder)currentmsg.sender;
		Mybody.Lastmessagereaded++;
		
		Mybody.SendingReciving_External= false ; Mybody.SendingReciving_internal=false; 
		if (  currentmsg.Inernal) Mybody.SendingReciving_internal= true ;
		else if (  currentmsg.External) Mybody.SendingReciving_External=true ;


		switch( currentmsg.performative) {
		case Command :
			Mybody.CurrentCommandRequest=((Command) currentmsg.content);

			//++++++++++++++++++++++++++++++++++++++ Form PIC
			if ( Mybody.CurrentSender instanceof Responder_Police &&  Mybody.CurrentCommandRequest.commandActivityType3 == Police_ActivityType.SecureScene_outerCordons  )  
			{				
				Mybody.CommTrigger= RespondersTriggers.GetActivityControlentryaccessofcordon;		
				Mybody.AssignedCordon=Mybody.CurrentCommandRequest.TargetCordon ;
				Mybody.step_long =InputFile.step_long_ClearRoute ;

				IgetNewActivity= true ;
			}
			//++++++++++++++++++++++++++++++++++++++
			if ( Mybody.CurrentSender instanceof Responder_Police  &&   Mybody.CurrentCommandRequest.commandActivityType3 == Police_ActivityType.CollectInformation   )  
			{				
				Mybody.CommTrigger= RespondersTriggers.GetActivitycollectionofinformation;		
				Mybody.AssignedTA=Mybody.CurrentCommandRequest.TargetTA ;

				IgetNewActivity= true ;
			}
			//======================================================================================================
			//++++++++++++++++++++++++++++++++++++++  From BZC
			if ( Mybody.CurrentSender instanceof Responder_Police &&  Mybody.CurrentCommandRequest.commandType3 == Police_TaskType.SetupTacticalAreas  && Mybody.CurrentAssignedMajorActivity_pol!=null )  
			{				
				Mybody.CommTrigger= RespondersTriggers.GetcommandSetupTacticalAreas  ;	
			}
			//++++++++++++++++++++++++++++++++++++++	
			if ( Mybody.CurrentSender instanceof Responder_Police &&  Mybody.CurrentCommandRequest.commandType3 == Police_TaskType.ClearRouteTraffic&& Mybody.CurrentAssignedMajorActivity_pol==Police_ActivityType.SecureScene_outerCordons )  
			{				
				Mybody.CommTrigger= RespondersTriggers.GetCommandClearRoute ;
				Mybody.AssignedRoute=Mybody.CurrentCommandRequest.TargetRoad;
			}
			//++++++++++++++++++++++++++++++++++++++
			if ( Mybody.CurrentSender instanceof Responder_Police &&  Mybody.CurrentCommandRequest.commandType3 == Police_TaskType.SecureRoute && Mybody.CurrentAssignedMajorActivity_pol==Police_ActivityType.SecureScene_outerCordons  )  
			{				
				Mybody.CommTrigger= RespondersTriggers.GetCommandSecureRoute ;
				Mybody.AssignedRoute=Mybody.CurrentCommandRequest.TargetRoad;
			}
			//++++++++++++++++++++++++++++++++++++++
			if ( Mybody.CurrentSender instanceof Responder_Police  &&  Mybody.CurrentCommandRequest.commandType3 == Police_TaskType.collectInjuriedEvacuetedCasualty &&  Mybody.CurrentAssignedMajorActivity_pol==Police_ActivityType.CollectInformation)  
			{								
				Mybody.CommTrigger=RespondersTriggers.GetCommandCollectInjured_EvacuatedCasualty ;
				liaisonofficer_CaList=Mybody.CurrentCommandRequest.TargetCasualty_List ;

			}
			//++++++++++++++++++++++++++++++++++++++
			if ( Mybody.CurrentSender instanceof Responder_Police  &&  Mybody.CurrentCommandRequest.commandType3 == Police_TaskType.CollectUninjuriedCasualty  && Mybody.CurrentAssignedMajorActivity_pol==Police_ActivityType.CollectInformation)  
			{				
				Mybody.CommTrigger= RespondersTriggers.GetCommandCollectUninjuredCasualtyinRC  ; 
				Mybody.TargetCasualty=Mybody.CurrentCommandRequest.TargetCasualty;
				//System.out.println("                                                                                    "+ Mybody.Id  + "  GetCommandCollectUninjuredCasualtyinRC  " );
			}
			//++++++++++++++++++++++++++++++++++++++
			if ( Mybody.CurrentSender instanceof Responder_Police  &&  Mybody.CurrentCommandRequest.commandType3 == Police_TaskType.CollectDeceasedCasualty && Mybody.CurrentAssignedMajorActivity_pol==Police_ActivityType.CollectInformation)  
			{				
				Mybody.CommTrigger= RespondersTriggers.GetCommandCollectDeceasedCasualty  ;
				Mybody.TargetCasualty=Mybody.CurrentCommandRequest.TargetCasualty;	

				ISDeadinInnerorCCS=Mybody.CurrentCommandRequest.ISDeadinInnerorCCS;//not used
			}
			//++++++++++++++++++++++++++++++++++++++
			break;			
		case InformReallocation :
			Mybody.CommTrigger= RespondersTriggers.GetCommandReallocation  ;
			break;
		case InformRCOprationsEND :
			Mybody.CommTrigger= RespondersTriggers.ENDRCOprations;
			break;	
		case InformEndClearRouteTraffic :
			Mybody.CommTrigger= RespondersTriggers.ENDSceneOprations;
			break;		
		case InformERend :
			Mybody.CommTrigger= RespondersTriggers.ENDER;
			break;		
		default:
			done= true;
		} // end switch

	}

	//##############################################################################################################################################################	
	//													Actions 
	//##############################################################################################################################################################
	public void LogInformationActionS( boolean Leave  ){	


		if ( !Leave   )
		{
			PreviousUnderAction = Mybody.OnhandCasualty.UnderAction ;
			Mybody.OnhandCasualty.UnderAction=CasualtyAction.Collectinformation ;	
		}
		


	}

	//----------------------------------------------------------------------------------------------------  //boolean ISsurvivorinRCorNot
	public void LogInformationActionE( int onRCCCS , boolean Leave  )    //1=n RC   2= CCS   3 scene  4 CCS
	{	


		
		if (onRCCCS ==1) // in RC
		{
			Mybody.OnhandCasualty.Status= CasualtyStatus.ConfirmedUninjuriedSurvivor;	


			Mybody.OnhandCasualty.PoliceCI= CasualtyinfromationType.SurvivorinRC  ;	
			Mybody.OnhandCasualty.DoneLog=true;

			Mybody.OnhandCasualty.ColorCode=6;				
			Mybody.OnhandCasualty.UnderAction=CasualtyAction.onRC;

		}
		

		
		if ( ! Leave && onRCCCS==2  ) 
		{
			if (  Mybody.OnhandCasualty.Triage_tage==5    ) 
			{
				Mybody.OnhandCasualty.Status= CasualtyStatus.ConfirmedDead ;

				//				if ( ISDeadinInnerorCCS)
				//				Mybody.OnhandCasualty.UnderAction=CasualtyAction.DeceasedonScene ;
				//				else
				//				Mybody.OnhandCasualty.UnderAction=CasualtyAction.DeceasedonCCS;	

				Mybody.OnhandCasualty.PoliceCI= CasualtyinfromationType.Deceased ;
				Mybody.OnhandCasualty.DoneLog=true;
				Mybody.OnhandCasualty.ColorCode=11;	

			}
			if (  Mybody.OnhandCasualty.Triage_tage!=5    )
			{
				//Mybody.OnhandCasualty.Status= CasualtyStatus.co ;

				Mybody.OnhandCasualty.PoliceCI= CasualtyinfromationType.Evacuated_KnowHosp ;
				Mybody.OnhandCasualty.DoneLog=true;
			}

			Mybody.OnhandCasualty.UnderAction = PreviousUnderAction ;

		}
		else if (  Leave && onRCCCS==2   ) 
		{
			Mybody.OnhandCasualty.PoliceCI= CasualtyinfromationType.Evacuated_NotKnowHosp;	
			Mybody.OnhandCasualty.DoneLog=true;
		}

		if (onRCCCS == 3  || onRCCCS ==  4 )  //  in CCS or scene
		{

			if (  Mybody.OnhandCasualty.Triage_tage==5    )
			{
				//Mybody.OnhandCasualty.Status= CasualtyStatus.co ;

				Mybody.OnhandCasualty.PoliceCI= CasualtyinfromationType.Deceased ;
				Mybody.OnhandCasualty.DoneLog=true;
				
				Mybody.OnhandCasualty.ColorCode=11;	
			}

			Mybody.OnhandCasualty.UnderAction = PreviousUnderAction ;


		}
	}

	//----------------------------------------------------------------------------------------------------
	public void GuideCasualtytoRCActionxxx( ) //not used
	{

		// do some thing to allow casualty  walk 
		Mybody.OnhandCasualty. SetMovement(MovmentCasualty.TORC ,null) ;
		Mybody.OnhandCasualty.UnderAction=CasualtyAction.DirectToRCorCCS ;
	}	

	//----------------------------------------------------------------------------------------------------
	public void InformResult(ACLPerformative xx   , Responder_Police BZC  ){	


		if (Mybody.CurrentAssignedMajorActivity_pol==Police_ActivityType.SecureScene_outerCordons )
		{
			Mybody.CurrentMessage  = new  ACL_Message( xx ,Mybody, BZC  ,Mybody.AssignedRoute   , Mybody.CurrentTick, Mybody.assignedIncident.ComMechanism_level_BtoRes,1  ,TypeMesg.Inernal) ;
			Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
		}
		else
		{
			Mybody.CurrentMessage  = new  ACL_Message( xx ,Mybody, BZC  ,Mybody.OnhandCasualty   , Mybody.CurrentTick, Mybody.assignedIncident.ComMechanism_level_BtoRes,1 ,TypeMesg.Inernal ) ;
			Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
		}

		Mybody.SendingReciving_External= false ;  Mybody.SendingReciving_internal= true ;
	}

	//##############################################################################################################################################################
	// Lission Officer
	//##############################################################################################################################################################

	public int ThereisFreeCasualtyinCCS_LO2()  // 1:free ; 2: Not free but here  ; -1: all leaved or loged
	{	
		int  Result= 0;

		for ( Casualty ca : liaisonofficer_CaList ) 
		{
			if (  !ca.DoneLog   && 
					(    ( ca.IsAssignedToResponder==false && ca.UnderAction==CasualtyAction.WaitingTreatment)  || (ca.IsAssignedToResponder==false && ca.UnderAction==CasualtyAction.WaitingTransfertoV) ) )
			{Result=1; break;}
			//System.out.println("                                                                                    "+ "$$$$$ LO: " +Mybody.Id + " ThereisFreeCasualtyinCCS ");
		}

		//		if ( Result==0 )
		//		{
		//			for ( Casualty ca : liaisonofficer_CaList ) 
		//				if (  !ca.DoneLog && Mybody.assignedIncident.CCStation.IsCasualtyinTA(ca)  )
		//					{Result=2; break;}
		//			//System.out.println("                                                                                    "+ "$$$$$ LO: " +Mybody.Id + " ThereisFreeCasualtyinCCS ");break;
		//		}


		if ( Result==0 )
		{
			int count=0;
			for ( Casualty ca : liaisonofficer_CaList ) 
				if ( ( ca.DoneLog  ) || (  ! Mybody.assignedIncident.CCStation.IsCasualtyinTA(ca)) )
					count++;

			if ( count== liaisonofficer_CaList.size())
				Result=3;
			//System.out.println("                                                                                    "+ "$$$$$ LO: " +Mybody.Id + " ThereisFreeCasualtyinCCS ");break;
		}

		return Result ;
	}

	//----------------------------------------------------------------------------------------------------
	public Casualty FreeCasualtyinCCS_LO2()
	{	
		boolean  Result=false;
		int max_prio=99;

		Casualty nominatedCasualty=null;

		for ( Casualty ca : liaisonofficer_CaList ) 
			if (  !ca.DoneLog   &&  ( ( ca.IsAssignedToResponder==false && ca.UnderAction==CasualtyAction.WaitingTreatment)  || (ca.IsAssignedToResponder==false && ca.UnderAction==CasualtyAction.WaitingTransfertoV) ) )
			{	
				if ( ca.Triage_tage < max_prio )
				{
					nominatedCasualty=ca;
					max_prio=ca.Triage_tage ;
				}
			}
		return nominatedCasualty;				
	}

	//----------------------------------------------------------------------------------------------------
	public int LogCasualtyEvacuated_LO2()
	{	
		int total=0;
		for (Casualty ca: this.liaisonofficer_CaList ) 
			if (  ! Mybody.assignedIncident.CCStation.IsCasualtyinTA(ca) && ! ca.DoneLog  )
			{
				ca.DoneLog=true ; ca.PoliceCI=CasualtyinfromationType.Evacuated_NotKnowHosp;
				total++;
			}

		return total;
	}


	public void printxxx()
	{

		int Result=0  ;
		{
			int count=0;
			for ( Casualty ca : liaisonofficer_CaList ) 
				if ( ( ca.DoneLog  ) || (  ! Mybody.assignedIncident.CCStation.IsCasualtyinTA(ca)) )
					count++;

			if ( count== liaisonofficer_CaList.size())
				Result=3;


			System.out.println(Result +"                                                                                    "+ "$$$$$ LO: " + Mybody.Id + " get command Injured_Evacuated Casualty  " +liaisonofficer_CaList.size());
			for ( Casualty ca : liaisonofficer_CaList ) 
				System.out.println("                                                                                    "+ "$$$$$ LO: " + Mybody.Id + " "+ ca.ID  + " U: "  + ca.UnderAction  +" PO: "+ ca.PoliceCI  +" " + ca.IsAssignedToResponder  + "       in CCS:"  +  Mybody.assignedIncident.CCStation.IsCasualtyinTA(ca)  + "   "   + ca.DoneLog);
		}
	}
	//______________________________________________________________________________________________________________________________________________________________
	//##############################################################################################################################################################
	//                                                        Behavior
	//##############################################################################################################################################################
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	// Policeman - General 
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	public void PolicemanBehavior()	{	

		//********************* for Get Activity
		if (  Mybody.CommTrigger==RespondersTriggers.AssigendRolebyPIC )
		{
			Mybody.Action=RespondersActions.Noaction;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.Comunication; //RoleAssignment;
			Mybody.CommTrigger=null;
		}	
		if ( IgetNewActivity && (Mybody.CommTrigger==RespondersTriggers.GetActivityControlentryaccessofcordon ||Mybody.CommTrigger==RespondersTriggers.GetActivitycollectionofinformation) )													
		{																		
			Mybody.Action=RespondersActions.GetAssignedTask;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.Comunication; //RoleAssignment;	
			Mybody.EndofCurrentAction=  InputFile.GetCommand_duration  ;
			//reciving=true;
			IgetNewActivity= false;
		}	
		// ++++++ 2- +++++++
		else if (  Mybody.Action==RespondersActions.GetAssignedTask &&  Mybody.EndActionTrigger== RespondersTriggers.EndingAction) 												
		{						
			Mybody.CurrentSender.Acknowledg(Mybody) ;  //reciving=false;


			if ( Mybody.CommTrigger==RespondersTriggers.GetActivityControlentryaccessofcordon) 	Mybody.CurrentAssignedMajorActivity_pol=Police_ActivityType.SecureScene_outerCordons ;	 ;	
			if ( Mybody.CommTrigger==RespondersTriggers.GetActivitycollectionofinformation) 	Mybody.CurrentAssignedMajorActivity_pol=Police_ActivityType.CollectInformation ;	 ;

			//Mybody.CommTrigger=null;
			Mybody.EndActionTrigger=null ;
			//System.out.println(Mybody.Id +" ..... " +Mybody.Role+ "       " + Mybody.CurrentAssignedMajorActivity);
		}
		//***********************************1**************************************		
		// **  Control of cordon ***	
		if(   Mybody.CurrentAssignedMajorActivity_pol==Police_ActivityType.SecureScene_outerCordons )
		{
			// 1- initial response
			if(  Mybody.CommTrigger==RespondersTriggers.GetActivityControlentryaccessofcordon )
			{
				Mybody.CurrentAssignedActivity=Police_TaskType.GoTolocation;
				Mybody.step_long =InputFile.step_long_ClearRoute ;
			}
			// 2-No action
			else if ( ( Mybody.InterpretedTrigger== RespondersTriggers.FirstTimeonLocation  || Mybody.InterpretedTrigger == RespondersTriggers.DoneTask )	) 
			{
				Mybody.CurrentAssignedActivity=Police_TaskType.None;

				Mybody.Action=RespondersActions.Noaction;
				Mybody.BehaviourType3=RespondersBehaviourTypes3.WaitingDuring;

				Mybody.InterpretedTrigger=null;	
			}
			// 3-Setup
			else if (  Mybody.CommTrigger== RespondersTriggers.GetcommandSetupTacticalAreas	) 
			{
				Mybody.CurrentAssignedActivity=Police_TaskType.SetupTacticalAreas ;
			}
			// 4-Clear Route
			else if (  Mybody.CommTrigger== RespondersTriggers.GetCommandClearRoute	) 
			{
				Mybody.CurrentAssignedActivity=Police_TaskType.ClearRouteTraffic ;
			}	
			// 5-Secure Route
			else if ( Mybody.CommTrigger== RespondersTriggers.GetCommandSecureCordon 	) 
			{
				Mybody.CurrentAssignedActivity=Police_TaskType.SecureRoute ;
			}
			else if ( Mybody.CommTrigger== RespondersTriggers.ENDSceneOprations 	) 
			{

				Mybody.CurrentAssignedActivity=Police_TaskType.None;


				Mybody.Action=RespondersActions.Noaction;
				Mybody.BehaviourType3=RespondersBehaviourTypes3.WaitingEnd;
				Mybody.CommTrigger=null;

				Mybody.step_long =InputFile.step_long_regularewalk ;
			}

			//Mybody.CurrentAssignedActivity!=Police_TaskType.GoTolocation &&  Mybody.Action==RespondersActions.Noaction
			//==== Move with commander ======================			
			if (  ( (Mybody.CurrentAssignedActivity==Police_TaskType.GoTolocation && Mybody.Action==RespondersActions.NotifyArrival) || Mybody.CurrentAssignedActivity==Police_TaskType.None)  &&  ( Mybody.AssignedCordon.CCcommander.Action==RespondersActions.SearchRoute || Mybody.AssignedCordon.CCcommander.Action==RespondersActions.GoToRoute)  )
			{
				double dis1= BuildStaticFuction.DistanceC( Mybody.geography, Mybody.Return_CurrentLocation(), Mybody.AssignedCordon.CCcommander.Return_CurrentLocation());

				if (  dis1>2  )
				{
					Mybody.Assign_DestinationResponder(Mybody.AssignedCordon.CCcommander); 
					Mybody.Walk();
				}

				if ( Mybody.Action==RespondersActions.Noaction  )Mybody.BehaviourType3=RespondersBehaviourTypes3.TaskPlanning ;//serach
				if ( Mybody.Action==RespondersActions.GoToRoute  )Mybody.BehaviourType3=RespondersBehaviourTypes3.Movementonincidentsite ;
			}
			//==== Move=====================================

		}
		//************************************2*************************************	
		// ** collection of information  ***
		if ( Mybody.CurrentAssignedMajorActivity_pol==Police_ActivityType.CollectInformation)
		{
			// 1-initial response
			if(   Mybody.CommTrigger==RespondersTriggers.GetActivitycollectionofinformation || Mybody.InterpretedTrigger== RespondersTriggers.IamNOTinLocation)					
			{
				Mybody.CurrentAssignedActivity=Police_TaskType.GoTolocation;	
			}
			// 2-No action
			else if(   Mybody.InterpretedTrigger== RespondersTriggers.FirstTimeonLocation ||  Mybody.InterpretedTrigger== RespondersTriggers.ComeBack || 
					Mybody.InterpretedTrigger == RespondersTriggers.DoneTask || Mybody.InterpretedTrigger == RespondersTriggers.DoneActivity ||    Mybody.CommTrigger == RespondersTriggers.ENDRCOprations  )  
			{
				Mybody.CurrentAssignedActivity=Police_TaskType.None;
				Mybody.Action=RespondersActions.Noaction;
				Mybody.BehaviourType3=RespondersBehaviourTypes3.WaitingDuring;

				if ( Mybody.InterpretedTrigger == RespondersTriggers.DoneActivity ||    Mybody.CommTrigger == RespondersTriggers.ENDRCOprations )
					Mybody.BehaviourType3=RespondersBehaviourTypes3.WaitingEnd ;

				Mybody.InterpretedTrigger=null;
				Mybody.CommTrigger=null;
				//System.out.println(Mybody.Id + " No action  .." + Mybody.Sending  +  Mybody.CurrentMessage );
			}
			// 3-Setup
			else if ( Mybody.CommTrigger== RespondersTriggers.GetcommandSetupTacticalAreas) 
			{
				Mybody.CurrentAssignedActivity=Police_TaskType.SetupTacticalAreas ;
			}
			// 4- collection of information		
			else if (   Mybody.CommTrigger== RespondersTriggers.GetCommandCollectUninjuredCasualtyinRC  )
				Mybody.CurrentAssignedActivity=Police_TaskType.CollectUninjuriedCasualty ;
			else if ( Mybody.CommTrigger== RespondersTriggers.GetCommandCollectDeceasedCasualty	 )
				Mybody.CurrentAssignedActivity=Police_TaskType.CollectDeceasedCasualty ;
			else if ( Mybody.CommTrigger== RespondersTriggers.GetCommandCollectInjured_EvacuatedCasualty   )
				Mybody.CurrentAssignedActivity=Police_TaskType.collectInjuriedEvacuetedCasualty  ;

		}
		//************************************Final*************************************
		// Ending
		if ( Mybody.CommTrigger==RespondersTriggers.ENDER   )   
		{
			Mybody.PrvRoleinprint3=Mybody.Role ;
			Mybody.Role=Police_ResponderRole.None;
		}
		// Reallocation
		if ( Mybody.CommTrigger== RespondersTriggers.GetCommandReallocation   )   
		{
			//do some thing

			Mybody.CurrentAssignedActivity=Police_TaskType.None;
			Mybody.CurrentAssignedMajorActivity_pol =Police_ActivityType.None;

		}

		//*************************************************************************
		switch(Mybody.CurrentAssignedActivity) {
		case GoTolocation:
			PolicemanBehavior_GoTolocation()	;	
			break;	
		case SetupTacticalAreas :
			PolicemanBehavior_SetupTacticalAreas();
			break; 
		case ClearRouteTraffic :
			PolicemanBehavior_ClearRouteTraffic ();
			break;
		case SecureRoute :
			PolicemanBehavior_SecureRoute() ;
			break;			
			//case GuidUninjuriedCasualty :
			//	PolicemanBehavior_GuideUninjuredCasualtytoRC ();
			//	break;
		case CollectUninjuriedCasualty:
			PolicemanBehavior_CollectUninjuredCasualtyInformation  ();
			break;
		case CollectDeceasedCasualty :
			PolicemanBehavior_CollectDeceasedCasualtyInformation () ;
			break;
		case collectInjuriedEvacuetedCasualty  :			
			this.PolicemanBehavior_CollectInjured_CasualtyInformation_HEvacuation();
			break;	
		case None:
			;
			break;}

		//if ( Mybody.Action==RespondersActions.InformNomorecasualty  )
		//System.out.println(Mybody.Id  +  "  Action:" + Mybody.Action + " com:" + Mybody.CommTrigger + "   Nt:" + Mybody.InterpretedTrigger+ " en:" + Mybody.SensingeEnvironmentTrigger + " end:" + Mybody.EndActionTrigger +  Mybody.Acknowledged + "     " +  Mybody.Sending +"    "+ Mybody.CurrentMessage  );


	}// end ParamedicBehavior

	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	// Policeman- Go to location 
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^	
	private void PolicemanBehavior_GoTolocation()	
	{
		// ++++++ 1- +++++++
		if(  Mybody.CommTrigger==RespondersTriggers.GetActivityControlentryaccessofcordon 
				|| Mybody.CommTrigger==RespondersTriggers.GetActivitycollectionofinformation|| Mybody.InterpretedTrigger== RespondersTriggers.IamNOTinLocation  )
		{	

			// 1
			if ( Mybody.CommTrigger==RespondersTriggers.GetActivityControlentryaccessofcordon  )  
			{

				//Mybody.Assign_DestinationCordon(Mybody.AssignedCordon.EnteryPointAccess_Point); 	
				//Bronzecommander = Mybody.AssignedCordon.CCcommander ;
				//Mybody.Action=RespondersActions.GoToCordon;

				Mybody.Assign_DestinationResponder(Mybody.AssignedCordon.CCcommander );  //temp
				Mybody.Bronzecommander_pol =Mybody.AssignedCordon.CCcommander  ;

				Mybody.Action=RespondersActions.GoToResponders;
				Mybody.BehaviourType3=RespondersBehaviourTypes3.Movementonincidentsite;   	

				Mybody.CommTrigger=null; IamNewResponder=true; 
			}
			// 2
			else if ( Mybody.CommTrigger==RespondersTriggers.GetActivitycollectionofinformation)
			{
				Mybody.Assign_DestinationCordon(Mybody.AssignedTA.Location);  
				Mybody.Bronzecommander_pol = (Responder_Police) Mybody.AssignedTA.Bronzecommander ;  // or  Mybody.assignedIncident.RC

				Mybody.Action=RespondersActions.GoToRC;
				Mybody.BehaviourType3=RespondersBehaviourTypes3.Movementonincidentsite;   		

				Mybody.CommTrigger=null; IamNewResponder=true; 
			}
			// 3
			else if (Mybody.InterpretedTrigger== RespondersTriggers.IamNOTinLocation )
			{				
				if (  Mybody.CurrentAssignedMajorActivity_pol==Police_ActivityType.CollectInformation ) 
				{
					Mybody.Assign_DestinationCordon(Mybody.AssignedTA.Location); 
					Mybody.Bronzecommander_pol = (Responder_Police) Mybody.AssignedTA.Bronzecommander ; // Mybody.assignedIncident.RCO; 

					Mybody.Action=RespondersActions.GoToRC;
					Mybody.BehaviourType3=RespondersBehaviourTypes3.Movementonincidentsite;   //TaskPlanning;		
				}
				else if ( Mybody.CurrentAssignedMajorActivity_pol==Police_ActivityType.SecureScene_outerCordons)
				{
					Mybody.Assign_DestinationCordon(Mybody.AssignedCordon.EnteryPointAccess_Point); 	
					Mybody.Bronzecommander_pol = Mybody.AssignedCordon.CCcommander ;

					Mybody.Action=RespondersActions.GoToCordon;
					Mybody.BehaviourType3=RespondersBehaviourTypes3.Movementonincidentsite;   
				}
				Mybody.InterpretedTrigger= null;IamNewResponder=false;
			}

		}
		// ++++++ 2- +++++++
		else if ( ( Mybody.Action==RespondersActions.GoToTacticalArea|| Mybody.Action==RespondersActions.GoToCordon || Mybody.Action==RespondersActions.GoToRC || Mybody.Action==RespondersActions.GoToControlArea ) && Mybody.SensingeEnvironmentTrigger==null) 
		{						
			Mybody.Walk();
		}
		// ++++++ 3- +++++++
		//---------------------------------------------------------------------------------------
		// ++++++ 3- +++++++
		else if (   Mybody.Action==RespondersActions.GoToResponders   && Mybody.SensingeEnvironmentTrigger==null) 
		{						
			//update location of commander
			if ( ( Mybody.AssignedCordon.CCcommander .Action==RespondersActions.SearchRoute  || Mybody.AssignedCordon.CCcommander .Action==RespondersActions.GoToRoute)  ) 
				Mybody.Assign_DestinationResponder(Mybody.AssignedCordon.CCcommander );

			Mybody.Walk();
		}
		// ++++++ 5- +++++++
		else if ( (   Mybody.Action==RespondersActions.GoToResponders && Mybody.SensingeEnvironmentTrigger== RespondersTriggers.ArrivedResponder)  ||
				( ( Mybody.Action==RespondersActions.GoToTacticalArea|| Mybody.Action==RespondersActions.GoToCordon || Mybody.Action==RespondersActions.GoToRC || Mybody.Action==RespondersActions.GoToControlArea )&& Mybody.SensingeEnvironmentTrigger== RespondersTriggers.ArrivedTargetObject) ) 
		{ 			
			//Send message to Bronze commander
			if ( IamNewResponder )
			{
				Mybody.CurrentMessage  = new  ACL_Message( ACLPerformative.InformNewResponderArrival, Mybody, Mybody.Bronzecommander_pol ,null  ,  Mybody.CurrentTick ,Mybody.assignedIncident.ComMechanism_level_BtoRes,1 ,TypeMesg.Inernal ) ;
				Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
			}

			else
			{
				Mybody.CurrentMessage = new  ACL_Message( ACLPerformative.InformlocationArrival, Mybody, Mybody.Bronzecommander_pol ,null  ,  Mybody.CurrentTick,Mybody.assignedIncident.ComMechanism_level_BtoRes,1 ,TypeMesg.Inernal  ) ;				
				Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
			}

			//==================
			Mybody.Action=RespondersActions.NotifyArrival;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.Comunication;   //TaskPlanning ;
			Mybody.SensingeEnvironmentTrigger=null;			
		}	
		// ++++++ 4- +++++++
		else if (Mybody.Action==RespondersActions.NotifyArrival && Mybody.Acknowledged)
		{
			if ( IamNewResponder )
			{
				Mybody.InterpretedTrigger= RespondersTriggers.FirstTimeonLocation;
				IamNewResponder=false;
			}
			else
				Mybody.InterpretedTrigger= RespondersTriggers.ComeBack;

			Mybody.Acknowledged=false;Mybody.Sending=false; 
			//System.out.println("                                                                                    "+ Mybody.Id + "   arrived location" );
		}

	}	

	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	// Policeman- Setup TacticalAreas 
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^	
	private void PolicemanBehavior_SetupTacticalAreas()	
	{

		// ++++++ 1- +++++++
		if ( Mybody.CommTrigger==RespondersTriggers.GetcommandSetupTacticalAreas  ) 													
		{																		
			Mybody.Action=RespondersActions.GetcommandSetupTacticalAreas ;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.Comunication; //TaskPlanning;	
			Mybody.EndofCurrentAction=  InputFile.GetCommand_duration  ;
			Mybody.CommTrigger=null; //reciving=true;

		}	
		// ++++++ 2- +++++++
		else if (  Mybody.Action==RespondersActions.GetcommandSetupTacticalAreas  &&  Mybody.EndActionTrigger== RespondersTriggers.EndingAction) 												
		{		
			Mybody.CurrentSender.Acknowledg(Mybody) ;  //reciving=false;

			if (Mybody.CurrentAssignedMajorActivity_pol==Police_ActivityType.CollectInformation )
			{ Mybody.Action=RespondersActions.SetupRC ; Mybody.AssignedTA.IworkinSetup(Mybody);  Mybody.Bronzecommander_pol =  (Responder_Police) Mybody.AssignedTA.Bronzecommander ;}

			else if  ( Mybody.CurrentAssignedMajorActivity_pol==Police_ActivityType.SecureScene_outerCordons )
			{ Mybody.Action=RespondersActions.SetupCordons  ;Mybody.AssignedCordon.IworkinSetup(Mybody); Mybody.Bronzecommander_pol = Mybody.AssignedCordon.CCcommander ;}					

			else 
			{ Mybody.Action=RespondersActions.SetupControalArea ; ; Mybody.AssignedTA.IworkinSetup(Mybody) ; Mybody.Bronzecommander_pol =  Mybody.assignedIncident.PICcommander ;}


			Mybody.BehaviourType3=RespondersBehaviourTypes3.TaskExecution_setupTA;								
			Mybody.EndActionTrigger=null;


		}
		//-----------------------------------------cordon-----------------------------------------
		// ++++++ 3- +++++++
		else if ( Mybody.Action==RespondersActions.SetupCordons  && 	Mybody.AssignedCordon.installed )		// Mybody.SensingeEnvironmentTrigger==RespondersTriggers.Cordonestablished										
		{							
			Mybody.CurrentMessage  = new  ACL_Message( ACLPerformative.InfromResultSetupCordon ,Mybody, Mybody.Bronzecommander_pol   ,null   , Mybody.CurrentTick, Mybody.assignedIncident.ComMechanism_level_BtoRes ,1,TypeMesg.Inernal) ;
			Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);

			Mybody.Action=RespondersActions.InfromResultSetupCordons ;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.Comunication;    
			Mybody.SensingeEnvironmentTrigger= null;
			//System.out.println("                                                                                    "+ Mybody.Id + "   done SetupCordons" );
		}
		// ++++++ 4- +++++++
		else if (Mybody.Action==RespondersActions.InfromResultSetupCordons && Mybody.Acknowledged) 
		{
			Mybody.InterpretedTrigger= RespondersTriggers.DoneTask;

			Mybody.Action=RespondersActions.Noaction;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.WaitingDuring;
			Mybody.Acknowledged=false;Mybody.Sending=false; 
			//System.out.println("                                                                                    "+ Mybody.Id + "  done  infrom " );
		}
		//------------------------------------------ControalArea ----------------------------------------
		// ++++++ 5- +++++++   
		else if ( Mybody.Action==RespondersActions.SetupControalArea && Mybody.AssignedTA.installed	) 		// Mybody.SensingeEnvironmentTrigger==RespondersTriggers.TAestablished											
		{							
			InformResult(ACLPerformative.InfromResultSetupControalArea ,Mybody.Bronzecommander_pol  );

			Mybody.Action=RespondersActions.InfromResultSetupControalArea ;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.Comunication;   
			Mybody.SensingeEnvironmentTrigger= null;

		}
		// ++++++ 6- +++++++
		else if (Mybody.Action==RespondersActions.InfromResultSetupControalArea && Mybody.Acknowledged) 
		{
			Mybody.InterpretedTrigger= RespondersTriggers.DoneTask;

			Mybody.Action=RespondersActions.Noaction;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.WaitingDuring;
			Mybody.Acknowledged=false;Mybody.Sending=false; 

			//System.out.println("                                                                                    "+ Mybody.Id + "   arrived location" );
		}

		//------------------------------------------RC  ----------------------------------------
		// ++++++ 7- +++++++   
		else if ( Mybody.Action==RespondersActions.SetupRC && 	 Mybody.AssignedTA.installed	) 													
		{							

			Mybody.CurrentMessage  = new  ACL_Message( ACLPerformative.InfromResultSetupRC ,Mybody, Mybody.Bronzecommander_pol   ,null   , Mybody.CurrentTick, Mybody.assignedIncident.ComMechanism_level_BtoRes,1 ,TypeMesg.Inernal) ;
			Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);

			Mybody.Action=RespondersActions.InfromResultSetupRC ;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.Comunication;   
			Mybody.SensingeEnvironmentTrigger= null;

		}
		// ++++++ 8- +++++++
		else if (Mybody.Action==RespondersActions.InfromResultSetupRC && Mybody.Acknowledged) 
		{
			Mybody.InterpretedTrigger= RespondersTriggers.DoneTask;

			Mybody.Action=RespondersActions.Noaction;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.WaitingDuring;
			Mybody.Acknowledged=false;Mybody.Sending=false; 


		}
	}

	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	// Policeman- ClearRoute (Centralized )
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^	
	private void PolicemanBehavior_ClearRouteTraffic()	
	{
		// ++++++ 1- +++++++
		if ( Mybody.CommTrigger==RespondersTriggers.GetCommandClearRoute ) 													
		{																		
			Mybody.Action=RespondersActions.GetCommandClearRoute ;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.Comunication; //TaskPlanning;	
			Mybody.EndofCurrentAction=  InputFile.GetCommand_duration  ;
			Mybody.CommTrigger=null;  	
		}	
		// ++++++ 2- +++++++
		else if (  Mybody.Action==RespondersActions.GetCommandClearRoute &&  Mybody.EndActionTrigger== RespondersTriggers.EndingAction) 												
		{						
			Mybody.CurrentSender.Acknowledg(Mybody) ; 

			//System.out.println(Mybody.CurrentTick + "  " +  Mybody.Id + " TriageS "+ Mybody.OnhandCasualty.ID);
			Mybody.Assign_DestinationCordon(Mybody.AssignedRoute.PointofClear);

			Mybody.Action=RespondersActions.GoToRoute ;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.Movementonincidentsite; //TaskPlanning?							
			Mybody.EndActionTrigger=null;

		}
		// ++++++ 3- +++++++
		else if (  Mybody.Action==RespondersActions.GoToRoute &&  Mybody.SensingeEnvironmentTrigger==null) 												
		{	
			Mybody.Walk();
		}
		// ++++++ 4- +++++++
		else if (  Mybody.Action==RespondersActions.GoToRoute &&  Mybody.SensingeEnvironmentTrigger==RespondersTriggers.ArrivedTargetObject) 												
		{						

			//System.out.println(Mybody.CurrentTick + "  " +  Mybody.Id + " TriageS "+ Mybody.OnhandCasualty.ID);
			Mybody.AssignedRoute.IworkinRouteT(Mybody);
			Mybody.Action=RespondersActions.ClearRoute ;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.TaskExecution_cleareRoute ;							
			//Mybody.EndActionTrigger=null;
			Mybody.SensingeEnvironmentTrigger=null;

		}
		// ++++++ 5- +++++++
		else if ( Mybody.Action==RespondersActions.ClearRoute && 	Mybody.AssignedRoute.TrafficLevel==Level.None  ) 	 //Mybody.SensingeEnvironmentTrigger==RespondersTriggers.RouteCleared												
		{							
			InformResult(ACLPerformative.InfromResultRoute , Mybody.assignedIncident.O_CCcommander);

			Mybody.Action=RespondersActions.InfromResultRoute ;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.Comunication;  //Sharinginfo;
			Mybody.SensingeEnvironmentTrigger= null;

		}
		// ++++++ 6- +++++++
		else if (Mybody.Action==RespondersActions.InfromResultRoute && Mybody.Acknowledged) 
		{
			Mybody.InterpretedTrigger= RespondersTriggers.DoneTask;
			Mybody.Acknowledged=false;Mybody.Sending=false; 

			//System.out.println("                                                                                    "+  Mybody.Id + " Route done ");

		}

	}

	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	// Policeman- Secure Route 
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^	
	private void PolicemanBehavior_SecureRoute()	
	{
		// ++++++ 1- +++++++
		if ( Mybody.CommTrigger==RespondersTriggers.GetCommandSecureRoute ) 													
		{																		
			Mybody.Action=RespondersActions.GetCommandSecureRoute ;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.Comunication; //TaskPlanning;	
			Mybody.EndofCurrentAction=  InputFile.GetCommand_duration  ;
			Mybody.CommTrigger=null;  //reciving=true;	
		}	
		// ++++++ 2- +++++++
		else if (  Mybody.Action==RespondersActions.GetCommandClearRoute &&  Mybody.EndActionTrigger== RespondersTriggers.EndingAction) 												
		{						
			Mybody.CurrentSender.Acknowledg(Mybody) ;  //reciving=false;

			//System.out.println(Mybody.CurrentTick + "  " +  Mybody.Id + " TriageS "+ Mybody.OnhandCasualty.ID);
			Mybody.Assign_DestinationCordon(Mybody.AssignedRoute.PointofClear);

			Mybody.Action=RespondersActions.GoToRoute ;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.Movementonincidentsite;  //TaskPlanning;								
			Mybody.EndActionTrigger=null;

		}
		// ++++++ 3- +++++++
		else if (  Mybody.Action==RespondersActions.GoToRoute &&  Mybody.SensingeEnvironmentTrigger==null) 												
		{	
			Mybody.Walk();
		}
		// ++++++ 4- +++++++
		else if (  Mybody.Action==RespondersActions.GoToRoute &&  Mybody.SensingeEnvironmentTrigger==RespondersTriggers.ArrivedTargetObject) 												
		{						

			//System.out.println(Mybody.CurrentTick + "  " +  Mybody.Id + " TriageS "+ Mybody.OnhandCasualty.ID);
			Mybody.TargetRoute.secured= true ;

			Mybody.Action=RespondersActions.SecureRoute ;
			//Mybody.BehaviourType3=RespondersBehaviourTypes3.TaskExecution_SecureRoute ;							
			Mybody.SensingeEnvironmentTrigger=null;

		}
	}

	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	// Policeman- collection of information in RC (Uninjured )
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^	
	private void PolicemanBehavior_CollectUninjuredCasualtyInformation ()	
	{
		// ++++++ 1- +++++++
		if ( Mybody.CommTrigger==RespondersTriggers.GetCommandCollectUninjuredCasualtyinRC     ) 													
		{																		
			Mybody.Action=RespondersActions.GetCommandCollectUninjuredCasualtyinRC   ;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.Comunication; //TaskPlanning;	
			Mybody.EndofCurrentAction=  InputFile.GetCommand_duration  ; 	
			Mybody.CommTrigger=null;   //reciving=true;	
			//System.out.println("                                                                                    "+ Mybody.Id + " get command Collectsurvivor in RC ");
		}
		// ++++++ 2- +++++++
		else if (   Mybody.Action==RespondersActions.GetCommandCollectUninjuredCasualtyinRC   && 	  Mybody.EndActionTrigger== RespondersTriggers.EndingAction) 													
		{		
			Mybody.CurrentSender.Acknowledg(Mybody) ;   //reciving=false;

			Mybody.Assign_DestinationCasualty(Mybody.TargetCasualty);

			Mybody.Action=RespondersActions.GoToCasualty ;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.Movementonincidentsite; //TaskPlanning;	?
			Mybody.EndActionTrigger=null;
		}

		// ++++++ 3- +++++++
		else if (Mybody.Action==RespondersActions.GoToCasualty && Mybody.SensingeEnvironmentTrigger==null)
		{			
			Mybody.Walk();
		}	
		// ++++++ 4- +++++++
		else if ( Mybody.Action==RespondersActions.GoToCasualty    && Mybody.SensingeEnvironmentTrigger==RespondersTriggers.ArrivedCasualty)
		{	
			//System.out.println("                                                                                    "+ Mybody.Id + "  arived  "+ Mybody.OnhandCasualty.ID);

			LogInformationActionS(false);
			Mybody.Action=RespondersActions.loginformation;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.TaskExecution_loginformation ;
			Mybody.EndofCurrentAction=  InputFile.loginformation_duration  ;
			Mybody.SensingeEnvironmentTrigger=null;  

		}
		// ++++++ 5- +++++++
		else if (Mybody.Action==RespondersActions.loginformation &&    Mybody.EndActionTrigger== RespondersTriggers.EndingAction ) 													
		{	

			LogInformationActionE(1 , false) ;
			InformResult(ACLPerformative.InformResultcasultyinfromation_uninjured , Mybody.assignedIncident.RCOcommander );  	//inside message to My RCO
			//System.out.println("                                                                                    "+ Mybody.Id + " done and send   "+ Mybody.OnhandCasualty.ID);
			Mybody.OnhandCasualty.IsAssignedToResponder = false ;
			Mybody.OnhandCasualty = null;

			Mybody.Action=RespondersActions.InformResultcollectioninformation ;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.Comunication;   			
			Mybody.EndActionTrigger=null;
		}			
		// ++++++ 6- +++++++
		else if (Mybody.Action==RespondersActions.InformResultcollectioninformation  && Mybody.Acknowledged ) 
		{						
			Mybody.InterpretedTrigger= RespondersTriggers.DoneTask ;	
			Mybody.Acknowledged=false;Mybody.Sending=false; 
			//System.out.println("                                                                                    "+ Mybody.Id + "done Collectsurvivor in RC ");
		}

	}

	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	// Policeman- collection of information (Dead )
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^	
	private void PolicemanBehavior_CollectDeceasedCasualtyInformation ()	
	{
		// ++++++ 1- +++++++
		if ( Mybody.CommTrigger==RespondersTriggers.GetCommandCollectDeceasedCasualty    ) 													
		{																		
			Mybody.Action=RespondersActions.GetCommandCollectDeceasedCasualty ;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.Comunication; //TaskPlanning;	
			Mybody.EndofCurrentAction=  InputFile.GetCommand_duration  ; 	
			Mybody.CommTrigger=null;  	
			//System.out.println("                                                                                    "+ Mybody.Id + " get command DeceasedCasualty  " + ISDeadinInnerorCCS);
		}
		// ++++++ 2- +++++++
		else if (  Mybody.Action==RespondersActions.GetCommandCollectDeceasedCasualty &&  Mybody.EndActionTrigger== RespondersTriggers.EndingAction) 													
		{		
			Mybody.CurrentSender.Acknowledg(Mybody) ; //reciving=false;

			Mybody.Assign_DestinationCasualty(Mybody.TargetCasualty) ;	

			Mybody.Action=RespondersActions.GoToCasualty;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.Movementonincidentsite;  //TaskPlanning ; 
			Mybody.EndActionTrigger=null;
		}
		// ++++++ 3- +++++++
		else if (Mybody.Action==RespondersActions.GoToCasualty && Mybody.SensingeEnvironmentTrigger==null)
		{			
			Mybody.Walk();
		}	
		// ++++++ 4- +++++++
		else if (  Mybody.Action==RespondersActions.GoToCasualty   && Mybody.SensingeEnvironmentTrigger==RespondersTriggers.ArrivedCasualty)
		{	
			if ( !ISDeadinInnerorCCS)  
				System.out.println("                                                                                    "+ Mybody.Id + "inCCS  for"+ Mybody.OnhandCasualty.ID);

			LogInformationActionS(false);					
			Mybody.Action=RespondersActions.loginformation;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.TaskExecution_loginformation ;
			Mybody.EndofCurrentAction=  InputFile.loginformation_duration  ;
			Mybody.SensingeEnvironmentTrigger=null; 
			Mybody.EndActionTrigger=null;

		}
		// ++++++ 5- +++++++
		else if (  Mybody.Action==RespondersActions.loginformation  && Mybody.EndActionTrigger== RespondersTriggers.EndingAction ) 													
		{						
			
			LogInformationActionE( 3 ,false) ;
			Mybody.OnhandCasualty.IsAssignedToResponder = false ;
			
			InformResult(ACLPerformative.InformResultcasultyinfromation_Decased, Mybody.assignedIncident.RCOcommander  );  	//inside message to My RCO
			
			
			Mybody.OnhandCasualty = null;

			Mybody.Action=RespondersActions.InformResultcollectioninformation ;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.Comunication;   		
			Mybody.EndActionTrigger=null;
		}			
		// ++++++ 6- +++++++
		else if (Mybody.Action==RespondersActions.InformResultcollectioninformation  && Mybody.Acknowledged ) 
		{						
			Mybody.InterpretedTrigger= RespondersTriggers.IamNOTinLocation ;
			Mybody.Acknowledged=false;Mybody.Sending=false; 
			//System.out.println("                                                                                    "+ Mybody.Id + " Done DeceasedCasualty  ");
		}
	}

	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	// Policeman- collection of information (Injured_Evacuated ) - liaison officer
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^	
	private void PolicemanBehavior_CollectInjured_CasualtyInformation_HEvacuation ()	
	{		
		// ++++++ 1- +++++++
		if ( Mybody.CommTrigger==RespondersTriggers.GetCommandCollectInjured_EvacuatedCasualty  ) //in CCS													
		{																		
			Mybody.Action=RespondersActions.GetCommandCollectInjured_EvacuatedCasualty ; this.Taskoption=11;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.Comunication; //TaskPlanning;	
			Mybody.EndofCurrentAction=  InputFile.GetCommand_duration  ; 	
			Mybody.CommTrigger=null; //reciving=true;	

		}
		// ++++++ 2- +++++++
		else if (  Mybody.Action==RespondersActions.GetCommandCollectInjured_EvacuatedCasualty && Taskoption==11 &&  Mybody.EndActionTrigger== RespondersTriggers.EndingAction) 													
		{		
			Mybody.CurrentSender.Acknowledg(Mybody) ;  //reciving=false;

			Mybody.Assign_DestinationCordon(Mybody.assignedIncident.CCStation.Location);

			Mybody.Action=RespondersActions.GoToCCS ;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.Movementonincidentsite  ;   
			Mybody.EndActionTrigger=null;
			Taskoption=0;
		}
		// ++++++ 3- +++++++
		else if (Mybody.Action==RespondersActions.GoToCCS && Mybody.SensingeEnvironmentTrigger==null)
		{			
			Mybody.Walk();
		}	
		// ++++++ 4- +++++++
		else if (   Mybody.Action==RespondersActions.GoToCCS && Mybody.SensingeEnvironmentTrigger==RespondersTriggers.ArrivedTargetObject) 				
		{
			Mybody.Action=RespondersActions.SearchCasualty;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.TaskPlanning ; 
			Mybody.SensingeEnvironmentTrigger=null;

			System.out.println("                                                                                    "+ "$$$$$ LO: " + Mybody.Id + " get command Injured_Evacuated Casualty  " +liaisonofficer_CaList.size());
			//for ( Casualty ca : liaisonofficer_CaList ) 
			//System.out.println("                                                                                    "+ "$$$$$ LO: " + Mybody.Id + " "+ ca.ID  + " U: "  + ca.UnderAction  +" PO: "+ ca.PoliceCI  +" " + ca.IsAssignedToResponder  + ""  +  Mybody.assignedIncident.CCStation.IsCasualtyinTA(ca)  + "   "   + ca.DoneLog);
		}
		//-----------------------------------------------------------------------------
		// ++++++ 5- +++++++
		else if (  Mybody.Action==RespondersActions.SearchCasualty &&  ThereisFreeCasualtyinCCS_LO2()==1 )  // 1:free ; 2: Not free but here  ; -1: all leaved or loged			  
		{																			
			Mybody.TargetCasualty=FreeCasualtyinCCS_LO2() ;

			Mybody.Assign_DestinationCasualty(Mybody.TargetCasualty) ;
			Mybody.Action=RespondersActions.GoToCasualty;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.Movementonincidentsite; //TaskPlanning?

			//System.out.println("                                                                                    "+ "$$$$$ LO: " +Mybody.Id + " go to  "+ Mybody.TargetCasualty.ID);
		}
		// ++++++ 6- +++++++
		else if (Mybody.Action==RespondersActions.GoToCasualty && Mybody.SensingeEnvironmentTrigger==null  )
		{								
			Mybody.Walk();

			if  ( ! Mybody.assignedIncident.CCStation.IsCasualtyinTA(Mybody.TargetCasualty)    )
			{ 
				Mybody.SensingeEnvironmentTrigger= RespondersTriggers.Casualtyleaved ; 
			}
			else if ( Mybody.TargetCasualty.IsAssignedToResponder==true  )  					
			{   
				Mybody.SensingeEnvironmentTrigger= RespondersTriggers.CasualtyHasanotherParamedic;
				Mybody.TargetCasualty=null;

				Mybody.Action=RespondersActions.SearchCasualty;
				Mybody.BehaviourType3=RespondersBehaviourTypes3.TaskPlanning ; 
				Mybody.SensingeEnvironmentTrigger=null;

			}
		}	
		// ++++++ 7- +++++++
		else if (  Mybody.Action==RespondersActions.GoToCasualty   && 
				( Mybody.SensingeEnvironmentTrigger==RespondersTriggers.ArrivedCasualty || Mybody.SensingeEnvironmentTrigger==RespondersTriggers.Casualtyleaved))
		{	
			if ( Mybody.TargetCasualty.IsAssignedToResponder==true && Mybody.SensingeEnvironmentTrigger==RespondersTriggers.ArrivedCasualty )  
			{
				Mybody.SensingeEnvironmentTrigger= RespondersTriggers.CasualtyHasanotherParamedic;
				Mybody.TargetCasualty=null;

				Mybody.Action=RespondersActions.SearchCasualty;
				Mybody.BehaviourType3=RespondersBehaviourTypes3.TaskPlanning ; 
				Mybody.SensingeEnvironmentTrigger=null;
			}
			else
			{

				Mybody.TargetCasualty.IsAssignedToResponder=true ;
				Mybody.OnhandCasualty= Mybody.TargetCasualty;

				if ( Mybody.SensingeEnvironmentTrigger== RespondersTriggers.Casualtyleaved ) 
				   LogInformationActionS(true);
				else if (Mybody.SensingeEnvironmentTrigger==RespondersTriggers.ArrivedCasualty )
					LogInformationActionS(false);

				Mybody.Action=RespondersActions.loginformation;
				Mybody.BehaviourType3=RespondersBehaviourTypes3.TaskExecution_loginformation ;
				Mybody.EndofCurrentAction=  InputFile.loginformation_duration  ;				
				this.Taskoption=22;
				//System.out.println("                                                                                    "+ "$$$$$ LO: " +Mybody.Id + " log  "+ Mybody.OnhandCasualty.ID);
			}
		}

		// ++++++ 8- +++++++
		else if (  Mybody.Action==RespondersActions.loginformation  && this.Taskoption==22 &&  Mybody.EndActionTrigger== RespondersTriggers.EndingAction  ) 													
		{						

			if ( Mybody.SensingeEnvironmentTrigger== RespondersTriggers.Casualtyleaved ) 
			{  Mybody.OnhandCasualty= Mybody.TargetCasualty;  LogInformationActionE(2,true);}
			else if (Mybody.SensingeEnvironmentTrigger==RespondersTriggers.ArrivedCasualty )
				LogInformationActionE(2 ,false);

			//System.out.println("                                                                                    "+ "$$$$$ LO: " +Mybody.Id + " done log  "+ Mybody.OnhandCasualty.ID);
			Mybody.OnhandCasualty.IsAssignedToResponder = false ;
			Mybody.OnhandCasualty = null;

			Mybody.Action=RespondersActions.SearchCasualty;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.TaskPlanning ; 
			Mybody.SensingeEnvironmentTrigger=null; 
			Mybody.EndActionTrigger=null;
			this.Taskoption=0;
		}			
		//-----------------------------------------------------------------------------   // 1:free ; 2: Not freebuthere  ; -1: all leaved oe loged
		// ++++++ 9- +++++++		
		else  if ( Mybody.Action==RespondersActions.SearchCasualty &&   ThereisFreeCasualtyinCCS_LO2()== 3 && this.Taskoption!=44   ) 
		{		
			int  total=0;
			total=LogCasualtyEvacuated_LO2();

			if (  total>0 )
			{
				Mybody.Action=RespondersActions.loginformation;
				Mybody.BehaviourType3=RespondersBehaviourTypes3.TaskExecution_loginformation ;
				Mybody.EndofCurrentAction=  InputFile.loginformation_duration * total ;
				this.Taskoption= 33; 
				//System.out.println("                                                                                    "+ "$$$$$ LO: " +Mybody.Id + " log  "+ Mybody.OnhandCasualty.ID);
			}
			else
			{
				this.Taskoption= 44; 
				//System.out.println("                                                                                    "+ "$$$$$ LO: " +Mybody.Id + " 44");
			}
		}
		// ++++++ 8- +++++++
		else if (  ( Mybody.Action==RespondersActions.loginformation  &&  this.Taskoption==33 && Mybody.EndActionTrigger== RespondersTriggers.EndingAction )||
				(	this.Taskoption== 44    )
				) 
		{

			Report Report1 =new Report();
			Report1. Police_caReport_LO (liaisonofficer_CaList ) ;
			Mybody.CurrentMessage = new  ACL_Message( ACLPerformative.InformResultcasultyinfromation_injured ,Mybody ,Mybody.assignedIncident.RCOcommander,Report1, Mybody.CurrentTick ,Mybody.assignedIncident.ComMechanism_level_BtoRes,liaisonofficer_CaList.size()  ,TypeMesg.Inernal ) ;
			Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);

			liaisonofficer_CaList.clear();

			Mybody.Action=RespondersActions.InformResultcollectioninformation ;
			Mybody.BehaviourType3=RespondersBehaviourTypes3.Comunication;        			
			this.Taskoption=0;

		}			
		// ++++++ 10- +++++++
		else if (Mybody.Action==RespondersActions.InformResultcollectioninformation  && Mybody.Acknowledged  ) 
		{						

			Mybody.InterpretedTrigger= RespondersTriggers.IamNOTinLocation ;
			Mybody.Acknowledged=false;Mybody.Sending=false; 
			//System.out.println("                                                                                    "+ "$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$ LO: " +Mybody.Id + "liaison officer done    ");

		}

		// ++++++ 11- +++++++
		else if (Mybody.Action==RespondersActions.SearchCasualty  && Mybody.CommTrigger== RespondersTriggers.ENDRCOprations ) 
		{

			Mybody.InterpretedTrigger = RespondersTriggers.DoneActivity ;
			Mybody.CommTrigger=null;
			System.out.println("                                                                                    "+ "$$$$$ LO: " +Mybody.Id + "liaison officer  DoneActivity   ");

		}
		// System.out.println(ThereisFreeCasualtyinCCS()   + " $$$$$ LO: " + Mybody.Id  +  "  Action:" + Mybody.Action + " com:" + Mybody.CommTrigger + "   Nt:" + Mybody.InterpretedTrigger+ " en:" + Mybody.SensingeEnvironmentTrigger + " end:" + Mybody.EndActionTrigger +  Mybody.Acknowledged + "     " +  Mybody.Sending +"    "+ Mybody.CurrentMessage  );
	}

} //end class





































//
//public boolean ThereisFreeCasualtyinCCS_LO()
//{	
//	boolean  Result=false;
//
//	for (Casualty ca: Mybody.assignedIncident.CCStation.casualtiesinTA ) 
//		if (  !ca.DoneLog   &&  ( ( ca.IsAssignedToResponder==false && ca.UnderAction==CasualtyAction.WaitingTreatment)  || (ca.IsAssignedToResponder==true && ca.UnderAction==CasualtyAction.WaitingTransfertoV) ))
//		{
//			Result=true; 
//			//System.out.println("                                                                                    "+ "$$$$$ LO: " +Mybody.Id + " ThereisFreeCasualtyinCCS ");break;
//		}
//
//	return Result ;
//}
//
////----------------------------------------------------------------------------------------------------
//public Casualty FreeCasualtyinCCS_LO()
//{	
//	boolean  Result=false;
//	int max_prio=99;
//
//	Casualty nominatedCasualty=null;
//
//	for (Casualty ca:  Mybody.assignedIncident.CCStation.casualtiesinTA ) 
//		if  ( ! ca.DoneLog   &&( ( ca.IsAssignedToResponder==false && ca.UnderAction==CasualtyAction.WaitingTreatment)  || (ca.IsAssignedToResponder==true && ca.UnderAction==CasualtyAction.WaitingTransfertoV) ))
//		{
//			if (  ca.Triage_tage < max_prio )
//			{
//				nominatedCasualty=ca;
//				max_prio=ca.Triage_tage ;
//			}
//		}
//	return nominatedCasualty;
//}
//
////----------------------------------------------------------------------------------------------------
//public boolean DoneCasualtyinCCS_LO()
//{	
//	boolean  Result=true;
//
//	for (Casualty ca: Mybody.assignedIncident.CCStation.casualtiesinTA ) 
//		if (    ( ! ca.DoneLog && Mybody.assignedIncident.CCStation.IsCasualtyinTA(ca) )   )
//		{
//			Result=false ;break;
//		}
//	return Result;
//}
//
////----------------------------------------------------------------------------------------------------
//public void CasualtyEvacuated_LO()
//{	
//	for (Casualty ca: this.liaisonofficer_CaList ) 
//		if (  ! Mybody.assignedIncident.CCStation.IsCasualtyinTA(ca) && ! ca.DoneLog  )
//		{
//			ca.DoneLog=true ; ca.PoliceCI=CasualtyinfromationType.Evacuated;
//		}
//}
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//

//private void PolicemanBehavior_CollectInjured_EvacuatedCasualtyInformation ()	
//{
//	// ++++++ 1- +++++++
//	if ( Mybody.CommTrigger==RespondersTriggers.GetCommandCollectInjured_EvacuatedCasualty    ) //in CCS													
//	{																		
//		Mybody.Action=RespondersActions.GetCommandCollectInjured_EvacuatedCasualty ;
//		Mybody.BehaviourType3=RespondersBehaviourTypes3.TaskPlanning;	
//		Mybody.EndofCurrentAction=  InputFile.GetCommand_duration  ; 	
//		Mybody.CommTrigger=null;	
//		//System.out.println("                                                                                    "+ "$$$$$ LO: " + Mybody.Id + " get command Injured_Evacuated Casualty  " +liaisonofficer_CaList.size());
//	}
//	// ++++++ 2- +++++++
//	else if (  Mybody.Action==RespondersActions.GetCommandCollectInjured_EvacuatedCasualty &&  Mybody.EndActionTrigger== RespondersTriggers.EndingAction) 													
//	{		
//		Mybody.CurrentSender.Acknowledg(Mybody) ;
//
//		Mybody.Assign_DestinationCordon(Mybody.assignedIncident.CCStation.Location);
//
//		Mybody.Action=RespondersActions.GoToCCS ;
//		Mybody.BehaviourType3=RespondersBehaviourTypes3.Movementonincidentsite  ;   
//		Mybody.EndActionTrigger=null;
//	}
//	// ++++++ 3- +++++++
//	else if (Mybody.Action==RespondersActions.GoToCCS && Mybody.SensingeEnvironmentTrigger==null)
//	{			
//		Mybody.Walk();
//	}	
//	// ++++++ 4- +++++++
//	else if (   Mybody.Action==RespondersActions.GoToCCS && Mybody.SensingeEnvironmentTrigger==RespondersTriggers.ArrivedTargetObject) 				
//	{
//		Mybody.Action=RespondersActions.SearchCasualty;
//		Mybody.BehaviourType3=RespondersBehaviourTypes3.TaskPlanning ; 
//		Mybody.SensingeEnvironmentTrigger=null;
//		Time_last_updated=Mybody.CurrentTick ;
//		
//		//System.out.println("                                                                                    "+ "$$$$$ LO: " +Mybody.Id + " arrived CCS  ");			
//		//for (Casualty ca: liaisonofficer_CaList ) 
//		//	System.out.println("                                                                                    "+ "$$$$$ LO: "  +Mybody.Id +ca.DoneLog +" "+ ca.ID  + "  "  + ca.priority_level  + " "  + ca.UnderAction+"    "  + ca.Status  + " IsAssignedToResponder   " + ca.IsAssignedToResponder + ""+ Mybody.assignedIncident.CCStation.IsCasualtyinTA(ca) ) ; 
//	}	
//	//-----------------------------------------------------------------------------
//	// ++++++ 5- +++++++
//	else if (  Mybody.Action==RespondersActions.SearchCasualty &&  ThereisFreeCasualtyinCCS_LO() )  				  
//	{	
//
//		Mybody.TargetCasualty=FreeCasualtyinCCS_LO() ;
//
//		Mybody.Assign_DestinationCasualty(Mybody.TargetCasualty) ;
//		Mybody.Action=RespondersActions.GoToCasualty;
//		Mybody.BehaviourType3=RespondersBehaviourTypes3.TaskPlanning ; //Movementonincidentsite;  
//		Mybody.SensingeEnvironmentTrigger=null;
//		//System.out.println("                                                                                    "+ "$$$$$ LO: " +Mybody.Id + " go to  "+ Mybody.TargetCasualty.ID);
//	}		
//	// ++++++ 6- +++++++
//	else if (Mybody.Action==RespondersActions.GoToCasualty && Mybody.SensingeEnvironmentTrigger==null  )
//	{								
//		Mybody.Walk();
//		
//		if ( ! Mybody.assignedIncident.CCStation.IsCasualtyinTA(Mybody.TargetCasualty)    )					
//		{   
//			Mybody.SensingeEnvironmentTrigger= RespondersTriggers.CasualtyHasanotherParamedic ;
//			Mybody.TargetCasualty=null;
//
//			Mybody.Action=RespondersActions.SearchCasualty;
//			Mybody.BehaviourType3=RespondersBehaviourTypes3.TaskPlanning ; 
//			Mybody.SensingeEnvironmentTrigger=null;
//
//		}
//	}	
//	// ++++++ 7- +++++++
//	else if (  Mybody.Action==RespondersActions.GoToCasualty   && Mybody.SensingeEnvironmentTrigger==RespondersTriggers.ArrivedCasualty)
//	{	
//
//		if ( Mybody.TargetCasualty.IsAssignedToResponder==true && Mybody.TargetCasualty.UnderAction==CasualtyAction.WaitingTreatment )  
//		{Mybody.SensingeEnvironmentTrigger= RespondersTriggers.CasualtyHasanotherParamedic ;Mybody.TargetCasualty=null;}
//		else
//		{
//			LogInformationActionS();
//			Mybody.TargetCasualty.IsAssignedToResponder=true ;
//			Taskoption=1;
//			Mybody.Action=RespondersActions.loginformation;
//			Mybody.BehaviourType3=RespondersBehaviourTypes3.TaskExecution_loginformation ;
//			Mybody.EndofCurrentAction=  InputFile.loginformation_duration  ;
//			Mybody.SensingeEnvironmentTrigger=null;  
//			//System.out.println("                                                                                    "+ "$$$$$ LO: " +Mybody.Id + " log  "+ Mybody.OnhandCasualty.ID);
//		}
//	}
//	// ++++++ 8- +++++++
//	else if (  Mybody.Action==RespondersActions.loginformation  && Taskoption==1 && Mybody.EndActionTrigger== RespondersTriggers.EndingAction  ) 													
//	{						
//
//		LogInformationActionE(false) ;
//		liaisonofficer_CaList2.add(Mybody.OnhandCasualty);
//		//System.out.println("                                                                                    "+ "$$$$$ LO: " +Mybody.Id + " done log  "+ Mybody.OnhandCasualty.ID);
//		Mybody.OnhandCasualty.IsAssignedToResponder = false ;
//		Mybody.OnhandCasualty = null;
//
//		Mybody.Action=RespondersActions.SearchCasualty;
//		Mybody.BehaviourType3=RespondersBehaviourTypes3.TaskPlanning ; 
//		Mybody.EndActionTrigger=null;
//		Taskoption=0;
//	}			
//	//-----------------------------------------------------------------------------
//	// ++++++ 10- +++++++		
//	else  if ( Mybody.Action==RespondersActions.SearchCasualty && liaisonofficer_CaList2.size() != 0 ) //&& (  Mybody.CurrentTick >= (Time_last_updated +  InputFile.UpdatOPEvery  )
//	{		
//
//			Report Report1 =new Report();
//			Report1. Police_caReport_LO (liaisonofficer_CaList2 ) ;
//			Mybody.CurrentMessage = new  ACL_Message( ACLPerformative.InformResultcasultyinfromation_injured ,Mybody ,Mybody.assignedIncident.RCOcommander,Report1, Mybody.CurrentTick ,CommunicationMechanism.RadioSystem ) ;
//			Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
//
//			liaisonofficer_CaList2.clear();
//
//			Mybody.Action=RespondersActions.InformResultcollectioninformation ;
//			Mybody.BehaviourType3=RespondersBehaviourTypes3.Comunication;         //Sharinginfo;			
//			Time_last_updated=Mybody.CurrentTick ;
//	
//		//System.out.println("                                                                                    "+ "$$$$$ LO: " +Mybody.Id + "liaison officer send    ");
//	}			
//	// ++++++ 11- +++++++
//	else if (Mybody.Action==RespondersActions.InformResultcollectioninformation  && Mybody.Acknowledged  ) 
//	{						
//		Mybody.Action=RespondersActions.SearchCasualty;
//		Mybody.BehaviourType3=RespondersBehaviourTypes3.TaskPlanning ; 
//		Mybody.Acknowledged=false;Mybody.Sending=false; 
//		//System.out.println("                                                                                    "+ "$$$$$ LO: " +Mybody.Id + "liaison officer  Done   ");
//	}
//
//	// ++++++ 9- +++++++
//	else if (Mybody.Action==RespondersActions.SearchCasualty  && Mybody.CommTrigger== RespondersTriggers.ENDRCOprations ) 
//	{
//
//		Mybody.InterpretedTrigger = RespondersTriggers.DoneActivity ;
//		Mybody.CommTrigger=null;
//		System.out.println("                                                                                    "+ "$$$$$ LO: " +Mybody.Id + "liaison officer  DoneActivity   ");
//
//	}
//	// System.out.println(ThereisFreeCasualtyinCCS()   + " $$$$$ LO: " + Mybody.Id  +  "  Action:" + Mybody.Action + " com:" + Mybody.CommTrigger + "   Nt:" + Mybody.InterpretedTrigger+ " en:" + Mybody.SensingeEnvironmentTrigger + " end:" + Mybody.EndActionTrigger +  Mybody.Acknowledged + "     " +  Mybody.Sending +"    "+ Mybody.CurrentMessage  );
//}

