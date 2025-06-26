package A_Roles_Fire;
import java.util.Random;
import A_Agents.Casualty;
import A_Agents.Responder;
import A_Agents.Responder_Ambulance;
import A_Agents.Responder_Fire;
import B_Communication.ACL_Message;
import B_Communication.Command;
import C_SimulationInput.InputFile;
import D_Ontology.BuildStaticFuction;
import D_Ontology.Ontology;
import D_Ontology.Ontology.ACLPerformative;
import D_Ontology.Ontology.Ambulance_TaskType;
import D_Ontology.Ontology.CasualtyAction;
import D_Ontology.Ontology.CasualtyReportandTrackingMechanism;
import D_Ontology.Ontology.CasualtyStatus;
import D_Ontology.Ontology.Fire_ActivityType;
import D_Ontology.Ontology.Fire_ResponderRole;
import D_Ontology.Ontology.Fire_TaskType;
import D_Ontology.Ontology.Level;
import D_Ontology.Ontology.MovmentCasualty;
import D_Ontology.Ontology.RandomWalking_StrategyType;
import D_Ontology.Ontology.RespondersActions;
import D_Ontology.Ontology.RespondersBehaviourTypes2;
import D_Ontology.Ontology.RespondersTriggers;
import D_Ontology.Ontology.TaskAllocationApproach;
import D_Ontology.Ontology.TaskAllocationMechanism;
import D_Ontology.Ontology.TypeMesg;

public class Fire_FireFighter {

	Responder_Fire Mybody;

	boolean IamNewResponder=true ;
	boolean IgetNewActivity ;
	boolean DirectToCasualty=false ;

	// SA
	public boolean ISRCestablished= false ;  
	public boolean ISCCSestablished=false;
	int Taskoption=0 ;

	double parallelactionTimmer=0;
	Casualty NewTargetCasualty=null;
	
	
	public CasualtyAction  PreviousUnderAction;

	//##############################################################################################################################################################
	public Fire_FireFighter ( Responder_Fire _Mybody  ) 
	{
		Mybody=_Mybody;
		Mybody.EndofCurrentAction=0;
		Mybody.PrvRoleinprint2=Mybody.Role;
	}

	//##############################################################################################################################################################
	public void  FireFighter_InterpretationMessage()
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

			//++++++++++++++++++++++++++++++++++++++ from FIC
			if ( Mybody.CurrentSender instanceof Responder_Fire &&  Mybody.CurrentCommandRequest.commandActivityType2== Fire_ActivityType.ClearRoute  )  
			{				
				Mybody.CommTrigger= RespondersTriggers.GetActivityClearRoute;
				//System.out.println(Mybody.Id + "GetActivityClearRoute" );
				//System.out.println("3                                     GetActivityClearRoute  "  + Mybody.Id  +  "  Action:" + Mybody.Action + " com:" + Mybody.CommTrigger + "   Nt:" + Mybody.InterpretedTrigger+ " en:" + Mybody.SensingeEnvironmentTrigger + " end:" + Mybody.EndActionTrigger +  Mybody.Acknowledged + "     " +  Mybody.Sending +"    "+ Mybody.CurrentMessage  );

				IgetNewActivity= true ;
			}
			//++++++++++++++++++++++++++++++++++++++
			if ( Mybody.CurrentSender instanceof Responder_Fire &&  Mybody.CurrentCommandRequest.commandActivityType2 == Fire_ActivityType.SearchandRescueCasualty  )  
			{				
				Mybody.CommTrigger= RespondersTriggers.GetActivitySearchCasualty;
				Mybody.AssignedSector=Mybody.CurrentCommandRequest.TargetSector;
				//System.out.println(Mybody.Id + "GetActivitySearchCasualty" );
				IgetNewActivity= true ;
			}
			//======================================================================================================
			//++++++++++++++++++++++++++++++++++++++  From BZC
			if ( Mybody.CurrentSender instanceof Responder_Fire  &&  Mybody.CurrentCommandRequest.commandType2 == Fire_TaskType.SetupSector  && Mybody.CurrentAssignedMajorActivity_fr!=null )  
			{				
				Mybody.CommTrigger= RespondersTriggers.GetcommandSetupTacticalAreas  ;	
				//System.out.println("                                       "  +Mybody.Id + "GetcommandSetupTacticalAreas " );
			}
			//++++++++++++++++++++++++++++++++++++++
			if ( Mybody.CurrentSender instanceof Responder_Fire &&  Mybody.CurrentCommandRequest.commandType2 == Fire_TaskType.ClearRouteWreckage && Mybody.CurrentAssignedMajorActivity_fr==Fire_ActivityType.ClearRoute )  
			{				
				Mybody.CommTrigger= RespondersTriggers.GetCommandClearRoute ;
				Mybody.AssignedRoute=Mybody.CurrentCommandRequest.TargetRoad;
				//System.out.println(Mybody.Id + "GetCommandClearRoute" );
			}
			//++++++++++++++++++++++++++++++++++++++
			else if ( Mybody.CurrentSender instanceof Responder_Fire &&  Mybody.CurrentCommandRequest.commandType2 == Fire_TaskType.CarryCasualtytoCCS  && Mybody.CurrentAssignedMajorActivity_fr==Fire_ActivityType.SearchandRescueCasualty)  
			{				
				Mybody.CommTrigger=RespondersTriggers.GetCommandCarryCasualtytoCCS;
				Mybody.TargetCasualty=Mybody.CurrentCommandRequest.TargetCasualty;
				//System.out.println(Mybody.Id + "Gxxxxxxxxxxxxxxxxxxx1xxxxxxxxxxxxxxxxxxx" );
			}
		case Requste :
			Mybody.CurrentCommandRequest=((Command) currentmsg.content);

			//++++++++++++++++++++++++++++++++++++++ //  Mutual Adjustment tasked by paramedic
			if ( Mybody.CurrentSender instanceof Responder_Ambulance &&  Mybody.CurrentCommandRequest.commandType2 == Fire_TaskType.CarryCasualtytoCCS  && Mybody.CurrentAssignedMajorActivity_fr==Fire_ActivityType.SearchandRescueCasualty)  
			{				
				Mybody.CommTrigger= RespondersTriggers.GetRequestCarryCasualtytoCCS ;
				NewTargetCasualty=Mybody.CurrentCommandRequest.TargetCasualty ;
				//System.out.println(Mybody.Id + "Gxxxxxxxxxxxxxxxxxxx2xxxxxxxxxxxxxxxxxxx" );
			}
			//++++++++++++++++++++++++++++++++++++++
			break;
		case InfromCCSEstablished :
			ISCCSestablished= true; // no trigger because it is broadcast
			break;
		case InfromRCEstablished : // no trigger because it is broadcast
			ISRCestablished= true;	
			break;
		case InformNomorecasualty  :
			Mybody.CommTrigger= RespondersTriggers.NoMorecasualty;
			break;
		case InformSceneOprationsEND  :
			Mybody.CommTrigger= RespondersTriggers.ENDSceneOprations;
			break;
		case InformEndClearRouteWreckage :
			Mybody.CommTrigger= RespondersTriggers.ENDSceneOprations;
			break;

		case InformReallocation :
			Mybody.CommTrigger= RespondersTriggers.GetCommandReallocation  ;
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
	public void ExtractActionS(){	
		
		PreviousUnderAction=Mybody.OnhandCasualty.UnderAction ;
		Mybody.OnhandCasualty.UnderAction=CasualtyAction.Extract;
		Mybody.OnhandCasualty.IworkinExtrication(Mybody);	
	}

	//----------------------------------------------------------------------------------------------------
	public void ExtractActionE(){	
		Mybody.OnhandCasualty.ColorCode=0;	
		Mybody.OnhandCasualty.Status= CasualtyStatus.Extracted;	
		
		//Mybody.OnhandCasualty.UnderAction=PreviousUnderAction ;	
		
		if (  Mybody.OnhandCasualty.Triage_tage==5 )
		Mybody.OnhandCasualty.UnderAction=CasualtyAction.DeceasedonScene ;
		else
			Mybody.OnhandCasualty.UnderAction=CasualtyAction.WaitingTransferDelay ;
		
		
	}

	//----------------------------------------------------------------------------------------------------
	public void GuideCasualtytosafeOrCCSorRCActionS(  ) {

		Mybody.OnhandCasualty.UnderAction=CasualtyAction.DirectToRCorCCS ;

	}

	public void GuideCasualtytosafeOrCCSorRCActionE(MovmentCasualty xx ,MovmentCasualty xxFinal   ) {

		// do some thing to allow casualty  walk 
		Mybody.OnhandCasualty. SetMovement(xx ,xxFinal ) ;

		if (xx==MovmentCasualty.TOCCS || ( xxFinal!=null  && xxFinal==MovmentCasualty.TOCCS )  )  Mybody.OnhandCasualty.UnderAction=CasualtyAction.TransferToCCS ;  	
		if (xx==MovmentCasualty.TORC || ( xxFinal!=null  && xxFinal==MovmentCasualty.TORC )  )Mybody.OnhandCasualty.UnderAction=CasualtyAction.TransferToRC ;
		//if (xx==MovmentCasualty.TORandomsafelocation )Mybody.OnhandCasualty.UnderAction=CasualtyAction.GuidedToSafe ;

		//if ( Mybody.OnhandCasualty.CurrentRPM == 99 )  System.out.println( "                                                                *******   GUid to wak   "
		//  + Mybody.OnhandCasualty.ID +"     "+ Mybody.CurrentTick+ "   " +  Mybody.OnhandCasualty.CurrentRPM  +"   " + 
		//		Mybody.OnhandCasualty.Triage_tage +" " + Mybody.OnhandCasualty.MoveTo +" " + Mybody.OnhandCasualty.PoliceCI   +"   " + Mybody.OnhandCasualty.Status  +"   " + Mybody.OnhandCasualty.UnderAction    +"   " + Mybody.Id );

	}

	//----------------------------------------------------------------------------------------------------
	public void InformResult(ACLPerformative xx , Responder_Fire BZC ){	

		if (  Mybody.CurrentAssignedMajorActivity_fr==Fire_ActivityType.SearchandRescueCasualty  ) 
		{

			if(  xx == ACLPerformative.InformTrappedcasualty )	
			Mybody.CurrentMessage  = new  ACL_Message( xx ,Mybody, BZC ,Mybody.OnhandCasualty   , Mybody.CurrentTick, Mybody.assignedIncident.ComMechanism_level_BtoRes,1,TypeMesg.Inernal ) ;
			else
				Mybody.CurrentMessage  = new  ACL_Message( xx ,Mybody, BZC ,Mybody.OnhandCasualty   , Mybody.CurrentTick, Mybody.assignedIncident.ComMechanism_level_BtoRes,1,TypeMesg.Inernal ) ;
			Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
		}

		else
		{

			Mybody.CurrentMessage  = new  ACL_Message( xx ,Mybody, BZC ,Mybody.AssignedRoute  , Mybody.CurrentTick, Mybody.assignedIncident.ComMechanism_level_BtoRes ,1,TypeMesg.Inernal) ;
			Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);

		}
		
		Mybody.SendingReciving_External= false ;  Mybody.SendingReciving_internal= true ;

	}

	//----------------------------------------------------------------------------------------------------
	public void TransferCasualtytoSafeOrCCSActionS( ) {

		Mybody.OnhandCasualty.UnderAction=CasualtyAction.TransferToCCS ;
	}	

	//----------------------------------------------------------------------------------------------------
	public void TransferCasualtytoSafeOrCCSActionE() 
	{
		//Mybody.OnhandCasualty.UnderAction=CasualtyAction.

		Mybody.OnhandCasualty.UnderAction=CasualtyAction.WaitingSecondTriage;


	}

	//______________________________________________________________________________________________________________________________________________________________
	//##############################################################################################################################################################
	//                                                        Behavior
	//##############################################################################################################################################################
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	// FireFighter - General 
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	public void FireFighterBehavior()	{	



		//********************* for Get Activity
		if (  Mybody.CommTrigger==RespondersTriggers.AssigendRolebyFIC )
		{
			Mybody.Action=RespondersActions.Noaction;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.Comunication; //RoleAssignment;
			Mybody.CommTrigger=null;
		}	
		else if (IgetNewActivity && (Mybody.CommTrigger==RespondersTriggers.GetActivityClearRoute ||Mybody.CommTrigger==RespondersTriggers.GetActivitySearchCasualty ) 	)												
		{																		
			Mybody.Action=RespondersActions.GetAssignedTask;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.Comunication; //RoleAssignment;	
			Mybody.EndofCurrentAction=  InputFile.GetCommand_duration  ;

			IgetNewActivity=false ;
		}	
		// ++++++ 2- +++++++
		else if (  Mybody.Action==RespondersActions.GetAssignedTask &&  Mybody.EndActionTrigger== RespondersTriggers.EndingAction) 												
		{						
			Mybody.CurrentSender.Acknowledg(Mybody) ;

			if ( Mybody.CommTrigger==RespondersTriggers.GetActivityClearRoute)  Mybody.CurrentAssignedMajorActivity_fr=Fire_ActivityType.ClearRoute ;
			if ( Mybody.CommTrigger==RespondersTriggers.GetActivitySearchCasualty )  Mybody.CurrentAssignedMajorActivity_fr=Fire_ActivityType.SearchandRescueCasualty ;

			//Mybody.CommTrigger=null;
			Mybody.EndActionTrigger=null ;

			//System.out.println("                                       "  +Mybody.Id +" ..... " +Mybody.Role+ "     " + Mybody.CurrentAssignedMajorActivity);
		}
		//************************************1*************************************	
		// ** Scene Operation ***
		// Decentralized			
		if (( Mybody.assignedIncident.TaskApproach_IN ==TaskAllocationApproach.Decentralized_Autonomous ||  Mybody.assignedIncident.TaskApproach_IN ==TaskAllocationApproach.Decentralized_Mutualadjustment)   
				&& Mybody.CurrentAssignedMajorActivity_fr==Fire_ActivityType.SearchandRescueCasualty)		
		{ 	
			// 1- initial response
			if(    Mybody.CommTrigger==RespondersTriggers.GetActivitySearchCasualty  || Mybody.InterpretedTrigger== RespondersTriggers.IamNOTinMySectorScene  )			
			{
				Mybody.CurrentAssignedActivity=Fire_TaskType.GoTolocation;		
			}		
			// 2- No action 
			else if (   (Mybody.InterpretedTrigger== RespondersTriggers.FirstTimeonLocation &&  ! Mybody.AssignedSector.installed )    )  //sector not install 
			{
				Mybody.CurrentAssignedActivity=Fire_TaskType.None;			
				Mybody.Action=RespondersActions.Noaction;		
				Mybody.BehaviourType2=RespondersBehaviourTypes2.WaitingDuring ;

				Mybody.InterpretedTrigger=RespondersTriggers.waitingInstalling ;
			}
			// 3-Setup
			else if (   Mybody.CommTrigger==RespondersTriggers.GetcommandSetupTacticalAreas 	) 
			{
				Mybody.InterpretedTrigger=null;
				Mybody.CurrentAssignedActivity=Fire_TaskType.SetupSector  ;
			}
			// 4- Carry to safe location or CCS
			else if (( Mybody.InterpretedTrigger==RespondersTriggers.waitingInstalling && Mybody.AssignedSector.installed) || (Mybody.InterpretedTrigger== RespondersTriggers.FirstTimeonLocation &&  Mybody.AssignedSector.installed ) ||  Mybody.InterpretedTrigger== RespondersTriggers.ComeBack 
					|| Mybody.InterpretedTrigger== RespondersTriggers.DoneTask  ) 	 
			{
				Mybody.CurrentAssignedActivity=Fire_TaskType.CarryCasualtytoCCS;
			}	
			// 5- No action 
			else if (   Mybody.InterpretedTrigger == RespondersTriggers.NoMorecasualty   )   
			{
				Mybody.CurrentAssignedActivity=Fire_TaskType.None;

				Mybody.Action=RespondersActions.Noaction;
				Mybody.BehaviourType2=RespondersBehaviourTypes2.WaitingDuring ;
				Mybody.InterpretedTrigger=null;	

				//System.out.println(" 1                                      "  + Mybody.Id  +  "  Action:" + Mybody.Action + " com:" + Mybody.CommTrigger + "   Nt:" + Mybody.InterpretedTrigger+ " en:" + Mybody.SensingeEnvironmentTrigger + " end:" + Mybody.EndActionTrigger +  Mybody.Acknowledged + "     " +  Mybody.Sending +"    " );


			}
			// 6- GoBackToControlArea
			else if (    Mybody.CommTrigger==RespondersTriggers.ENDSceneOprations )  /// Confirmation from bronze commander
			{

				Mybody.CurrentAssignedMajorActivity_fr=null;
				Mybody.CurrentAssignedActivity=Fire_TaskType.GoBackToControlArea ;
				Mybody.InterpretedTrigger= RespondersTriggers.DoneActivity ;
				Mybody.CommTrigger=null;

				//System.out.println("2                                       "  + Mybody.Id  +  "  Action:" + Mybody.Action + " com:" + Mybody.CommTrigger + "   Nt:" + Mybody.InterpretedTrigger+ " en:" + Mybody.SensingeEnvironmentTrigger + " end:" + Mybody.EndActionTrigger +  Mybody.Acknowledged + "     " +  Mybody.Sending +"    " );

			}

		}
		// Centralized
		else if (Mybody.assignedIncident.TaskApproach_IN == TaskAllocationApproach.CentralizedDirectSupervision   && Mybody.CurrentAssignedMajorActivity_fr==Fire_ActivityType.SearchandRescueCasualty )	
		{
			// 1-initial response
			if(  Mybody.CommTrigger==RespondersTriggers.GetActivitySearchCasualty  || Mybody.InterpretedTrigger== RespondersTriggers.IamNOTinMySectorScene)		//come back		
			{
				Mybody.CurrentAssignedActivity=Fire_TaskType.GoTolocation;	

			}// 2- No action 
			else if ( Mybody.InterpretedTrigger== RespondersTriggers.FirstTimeonLocation  || Mybody.InterpretedTrigger== RespondersTriggers.DoneTask ||
					Mybody.InterpretedTrigger== RespondersTriggers.ComeBack   ||  Mybody.InterpretedTrigger== RespondersTriggers.CasualtyDead)  
			{
				Mybody.CurrentAssignedActivity=Fire_TaskType.None;
				Mybody.Action=RespondersActions.Noaction;
				Mybody.BehaviourType2=RespondersBehaviourTypes2.WaitingDuring;

				Mybody.InterpretedTrigger=null;	
				Mybody.CommTrigger=null;
				//System.out.println("                                       "  + Mybody.Id + " No action"  );
			}
			// 3-Setup
			else if (   Mybody.CommTrigger==RespondersTriggers.GetcommandSetupTacticalAreas 	) 
			{
				Mybody.CurrentAssignedActivity=Fire_TaskType.SetupSector ;
			}
			// 4-Carry to safe location or CCS
			else if (Mybody.CommTrigger==RespondersTriggers.GetCommandCarryCasualtytoCCS) 
			{
				Mybody.CurrentAssignedActivity=Fire_TaskType.CarryCasualtytoCCS;

			}
			// 5- GoBackToControlArea
			else if (    Mybody.CommTrigger==RespondersTriggers.ENDSceneOprations )  /// Confirmation from bronze commander
			{

				Mybody.CurrentAssignedMajorActivity_fr=null;
				Mybody.CurrentAssignedActivity=Fire_TaskType.GoBackToControlArea ;
				Mybody.InterpretedTrigger= RespondersTriggers.DoneActivity ;
				Mybody.CommTrigger=null;
				Mybody.Bronzecommander_fr=null;

			}


			//==== Move with commander ======================		
			if ( ( ( Mybody.CurrentAssignedActivity== Fire_TaskType.GoTolocation && Mybody.Action==RespondersActions.NotifyArrival) || Mybody.CurrentAssignedActivity==Fire_TaskType.None ) &&  (  Mybody.AssignedSector.FSCcommander.Action==RespondersActions.SearchCasualty || Mybody.AssignedSector.FSCcommander.Action==RespondersActions.Noaction )  )
			{
				double dis1= BuildStaticFuction.DistanceC( Mybody.geography, Mybody.Return_CurrentLocation(), Mybody.AssignedSector.FSCcommander.Return_CurrentLocation());

				if (  dis1> 3  )
				{
					Mybody.Assign_DestinationResponder(Mybody.AssignedSector.FSCcommander); 
					Mybody.Walk();
				}

			}
			//==== Move=====================================

		}
		//***********************************1**************************************	
		// ** Clear Route ***
		// 1- initial response
		if(  Mybody.CurrentAssignedMajorActivity_fr==Fire_ActivityType.ClearRoute )	
		{
			if ( (Mybody.CommTrigger==RespondersTriggers.GetActivityClearRoute || Mybody.InterpretedTrigger== RespondersTriggers.IamNOTinLocation) )
			{
				Mybody.CurrentAssignedActivity=Fire_TaskType.GoTolocation;	
				Mybody.step_long =InputFile.step_long_ClearRoute ;
			}
			// 2-No action
			else if ( Mybody.InterpretedTrigger== RespondersTriggers.FirstTimeonLocation || Mybody.InterpretedTrigger == RespondersTriggers.DoneTask || Mybody.InterpretedTrigger == RespondersTriggers.ComeBack ) 
			{
				Mybody.CurrentAssignedActivity=Fire_TaskType.None;				
				Mybody.Action=RespondersActions.Noaction;
				Mybody.BehaviourType2=RespondersBehaviourTypes2.WaitingDuring;

				Mybody.InterpretedTrigger=null;	
			}
			// 3-Clear Route
			else if ( Mybody.CommTrigger==RespondersTriggers.GetCommandClearRoute  )
			{
				Mybody.CurrentAssignedActivity=Fire_TaskType.ClearRouteWreckage ;
			}
			// 3-END 
			else if ( Mybody.CommTrigger== RespondersTriggers.ENDSceneOprations  )
			{
				Mybody.CurrentAssignedActivity=Fire_TaskType.None;				
				Mybody.Action=RespondersActions.Noaction;
				Mybody.BehaviourType2=RespondersBehaviourTypes2.WaitingEnd;

				Mybody.CommTrigger=null;	
				Mybody.step_long =InputFile.step_long_regularewalk ;
			}


			//==== Move with commander ======================			
			if (   ( Mybody.Action==RespondersActions.NotifyArrival || Mybody.Action==RespondersActions.Noaction ) && ( Mybody.assignedIncident.FRFSCcommander.Action==RespondersActions.SearchRoute  || Mybody.assignedIncident.FRFSCcommander.Action==RespondersActions.GoToRoute)  )
			{
				double dis1= BuildStaticFuction.DistanceC( Mybody.geography, Mybody.Return_CurrentLocation(), Mybody.assignedIncident.FRFSCcommander.Return_CurrentLocation());

				if (  dis1>2  )
				{					
					Mybody.Assign_DestinationResponder(Mybody.assignedIncident.FRFSCcommander); 
					Mybody.Walk();
				}	
				if ( Mybody.Action==RespondersActions.Noaction  )Mybody.BehaviourType2=RespondersBehaviourTypes2.TaskPlanning ;//serach
				if ( Mybody.Action==RespondersActions.GoToRoute  )Mybody.BehaviourType2=RespondersBehaviourTypes2.Movementonincidentsite ;
			}
			//==== Move=====================================
		}
		//************************************Final*************************************
		// Ending
		if ( Mybody.CommTrigger==RespondersTriggers.ENDER   )   
		{
			Mybody.CurrentAssignedActivity=Fire_TaskType.None;
			Mybody.CurrentAssignedMajorActivity_fr =Fire_ActivityType.None;

			Mybody.PrvRoleinprint2=Mybody.Role ;
			Mybody.Role=Fire_ResponderRole.None;
		}		
		// Reallocation
		if ( Mybody.CommTrigger== RespondersTriggers.GetCommandReallocation   )   
		{
			//do some thing
			Mybody.CurrentAssignedActivity=Fire_TaskType.None;
			Mybody.CurrentAssignedMajorActivity_fr =Fire_ActivityType.None;
		}
		//*************************************************************************
		switch(Mybody.CurrentAssignedActivity) {
		case GoTolocation:
			FireFighterBehavior_GoTolocation() ; //  GoTosectorSceneorRoute 	;	
			break;
		case GoBackToControlArea :
			Mybody.FireFighterBehavior_GoBackToControlArea() ; //  GoTosectorSceneorRoute 	;	
			break;	
		case SetupSector :
			FireFighterBehavior_SetupSector();
			break;
		case ClearRouteWreckage :
			FireFighterBehavior_ClearRouteWreckage ();
			break;
		case CarryCasualtytoCCS:			
			if (Mybody.assignedIncident.TaskApproach_IN ==TaskAllocationApproach.Decentralized_Autonomous ||  Mybody.assignedIncident.TaskApproach_IN ==TaskAllocationApproach.Decentralized_Mutualadjustment )	
				FireFighterBehavior_SearchandCarryCasualty();
			else
				FireFighterBehavior_CarryCasualty();
			break;		
		case None:
			;
			break;}

		//if ( Mybody.Action==RespondersActions.InformNomorecasualty  )
		//System.out.println(Mybody.Id  +  "  Action:" + Mybody.Action + " com:" + Mybody.CommTrigger + "   Nt:" + Mybody.InterpretedTrigger+ " en:" + Mybody.SensingeEnvironmentTrigger + " end:" + Mybody.EndActionTrigger +  Mybody.Acknowledged + "     " +  Mybody.Sending +"    "+ Mybody.CurrentMessage  );


	}// end ParamedicBehavior

	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	// FireFighter- Go to location 
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^	
	private void FireFighterBehavior_GoTolocation()	
	{		
		// ++++++ 1- +++++++
		if(  Mybody.CommTrigger==RespondersTriggers.GetActivityClearRoute|| Mybody.CommTrigger==RespondersTriggers.GetActivitySearchCasualty
				|| Mybody.InterpretedTrigger== RespondersTriggers.IamNOTinMySectorScene  || Mybody.InterpretedTrigger== RespondersTriggers.IamNOTinLocation )
		{	


			// 1
			if ( Mybody.CommTrigger==RespondersTriggers.GetActivityClearRoute  )  
			{
				Mybody.Assign_DestinationResponder(Mybody.assignedIncident.FRFSCcommander);  //temp
				Mybody.Bronzecommander_fr = Mybody.assignedIncident.FRFSCcommander ;

				Mybody.Action=RespondersActions.GoToResponders;
				Mybody.BehaviourType2=RespondersBehaviourTypes2.Movementonincidentsite;                 
				Mybody.CommTrigger=null; IamNewResponder=true; 

				//System.out.println("                                       "  + Mybody.Id + " Go to  location" );
			}
			// 2
			else if ( Mybody.CommTrigger==RespondersTriggers.GetActivitySearchCasualty )
			{
				Mybody.Assign_DestinationCordon(Mybody.AssignedSector.centerofsectorPoint);  
				Mybody.Bronzecommander_fr = Mybody.AssignedSector.FSCcommander ;  

				Mybody.Action=RespondersActions.GoToSectorScene;
				Mybody.BehaviourType2=RespondersBehaviourTypes2.Movementonincidentsite;                 

				Mybody.CommTrigger=null; IamNewResponder=true; 
			}
			// 3
			else if (Mybody.InterpretedTrigger== RespondersTriggers.IamNOTinMySectorScene )  //(  Mybody.CurrentAssignedMajorActivity==Fire_ActivityType.SearchandRescueCasualty ) 
			{					
				Mybody.Assign_DestinationCordon(Mybody.AssignedSector.centerofsectorPoint); 
				Mybody.Bronzecommander_fr = Mybody.AssignedSector.FSCcommander ;  

				Mybody.Action=RespondersActions.GoToSectorScene;
				Mybody.BehaviourType2=RespondersBehaviourTypes2.Movementonincidentsite;                  
				Mybody.InterpretedTrigger= null;
			}
			else if (Mybody.InterpretedTrigger== RespondersTriggers.IamNOTinLocation  )
			{	
				//				else if ( Mybody.CurrentAssignedMajorActivity==Fire_TaskType.ClearRoute)
				//				{
				//					Mybody.Assign_DestinationCordon(Mybody.AssignedCordon.EnteryPointAccess); 	
				//					Bronzecommander = Mybody.AssignedCordon.FSCcommander ;
				//
				//					Mybody.Action=RespondersActions.GoToCordon;
				//					Mybody.BehaviourType=RespondersBehaviourTypes.TaskPlanning;	
				//				}
				//				Mybody.InterpretedTrigger= null;IamNewResponder=false;IamSetupResponder=false;
			}

		}
		//---------------------------------------------------------------------------------------
		// ++++++ 3- +++++++
		else if (Mybody.Action==RespondersActions.GoToSectorScene && Mybody.SensingeEnvironmentTrigger==null) 
		{						
			if ( Mybody.AssignedSector.AmIinSector(Mybody))
				Mybody.SensingeEnvironmentTrigger= RespondersTriggers.ArrivedTargetObject;

			Mybody.Walk();
		}		
		//---------------------------------------------------------------------------------------
		// ++++++ 3- +++++++
		else if (   Mybody.Action==RespondersActions.GoToResponders   && Mybody.SensingeEnvironmentTrigger==null) 
		{						
			//update location of commander
			if ( ( Mybody.assignedIncident.FRFSCcommander.Action==RespondersActions.SearchRoute  || Mybody.assignedIncident.FRFSCcommander.Action==RespondersActions.GoToRoute)  ) 
				Mybody.Assign_DestinationResponder(Mybody.assignedIncident.FRFSCcommander);

			Mybody.Walk();
		}
		// ++++++ 5- +++++++
		else if ( (   Mybody.Action==RespondersActions.GoToResponders && Mybody.SensingeEnvironmentTrigger== RespondersTriggers.ArrivedResponder)  ||
				(  Mybody.Action==RespondersActions.GoToSectorScene  && Mybody.SensingeEnvironmentTrigger== RespondersTriggers.ArrivedTargetObject) )
		{ 			
			//Send message to Bronze commander
			if ( IamNewResponder )
			{
				Mybody.CurrentMessage  = new  ACL_Message( ACLPerformative.InformNewResponderArrival, Mybody, Mybody.Bronzecommander_fr ,null  ,  Mybody.CurrentTick ,Mybody.assignedIncident.ComMechanism_level_BtoRes,1  ,TypeMesg.Inernal) ;
				Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
				//System.out.println(Mybody.Id + " send arrived location" + Bronzecommander.Id);
			}

			else
			{
				Mybody.CurrentMessage = new  ACL_Message( ACLPerformative.InformlocationArrival, Mybody, Mybody.Bronzecommander_fr ,null  ,  Mybody.CurrentTick,Mybody.assignedIncident.ComMechanism_level_BtoRes ,1,TypeMesg.Inernal ) ;	
				Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
			}
			//System.out.println("                                       "  + Mybody.Id + " arrived responders  " + Bronzecommander .Id + "zzzzzzzzzzzzzzzzzzzzzzzzzz");
			//==================	
			Mybody.Action=RespondersActions.NotifyArrival;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.Comunication ;    ///TaskPlanning ;
			Mybody.SensingeEnvironmentTrigger=null;			
		}	
		// ++++++ 6- +++++++
		else if (Mybody.Action==RespondersActions.NotifyArrival && Mybody.Acknowledged)
		{
			if ( IamNewResponder  )
			{
				Mybody.InterpretedTrigger= RespondersTriggers.FirstTimeonLocation;
				IamNewResponder=false;				
			}
			else
				Mybody.InterpretedTrigger= RespondersTriggers.ComeBack;



			Mybody.Acknowledged=false;Mybody.Sending=false; 
			//System.out.println("                                       "  + Mybody.Id + " arrived location" );

		}

	}	

	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	// FireFighter- SetupSector 
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^	
	private void FireFighterBehavior_SetupSector()	
	{
		// ++++++ 1- +++++++
		if ( Mybody.CommTrigger==RespondersTriggers.GetcommandSetupTacticalAreas  ) 													
		{																		
			Mybody.Action=RespondersActions.GetcommandSetupTacticalAreas ;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.Comunication; //TaskPlanning;	
			Mybody.EndofCurrentAction=  InputFile.GetCommand_duration  ;
			Mybody.CommTrigger=null;

		}	
		// ++++++ 2- +++++++
		else if (  Mybody.Action==RespondersActions.GetcommandSetupTacticalAreas  &&  Mybody.EndActionTrigger== RespondersTriggers.EndingAction) 												
		{		
			Mybody.CurrentSender.Acknowledg(Mybody) ;

			Mybody.AssignedSector.IworkinSetup(Mybody);

			Mybody.Action=RespondersActions.SetupSector ;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.TaskExecution_SetupTA ;						
			Mybody.EndActionTrigger=null;

		}	
		// ++++++ 3- +++++++   
		else if ( Mybody.Action==RespondersActions.SetupSector  && Mybody.AssignedSector.installed	 ) 	 		//Mybody.SensingeEnvironmentTrigger==RespondersTriggers.SectorCreated										
		{							
			InformResult(ACLPerformative.InfromResultSetupSector ,Mybody.AssignedSector.FSCcommander );

			Mybody.Action=RespondersActions.InfromResultSetupSector ;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.Comunication;                  
			Mybody.SensingeEnvironmentTrigger= null;

		}
		// ++++++ 4- +++++++
		else if (Mybody.Action==RespondersActions.InfromResultSetupSector && Mybody.Acknowledged) 
		{
			Mybody.InterpretedTrigger= RespondersTriggers.DoneTask;
			Mybody.Acknowledged=false;Mybody.Sending=false;

			//System.out.println("                                       "  + Mybody.Id + " done setup" );
		}

	}

	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	// FireFighter- ClearRoute 
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^	
	private void FireFighterBehavior_ClearRouteWreckage()	
	{

		// ++++++ 1- +++++++
		if ( Mybody.CommTrigger==RespondersTriggers.GetCommandClearRoute ) 													
		{																		
			Mybody.Action=RespondersActions.GetCommandClearRoute ;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.Comunication; //TaskPlanning;	
			Mybody.EndofCurrentAction=  InputFile.GetCommand_duration  ;
			Mybody.CommTrigger=null;	
			//System.out.println("                                       "  +Mybody.Id + " GetCommandClearRoute" );
		}	
		// ++++++ 2- +++++++
		else if (  Mybody.Action==RespondersActions.GetCommandClearRoute &&  Mybody.EndActionTrigger== RespondersTriggers.EndingAction) 												
		{						
			Mybody.CurrentSender.Acknowledg(Mybody) ;

			//System.out.println(Mybody.CurrentTick + "  " +  Mybody.Id + " TriageS "+ Mybody.OnhandCasualty.ID);
			Mybody.Assign_DestinationCordon(Mybody.AssignedRoute.PointofClear);

			Mybody.Action=RespondersActions.GoToRoute ;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.Movementonincidentsite; //TaskPlanning?					
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

			Mybody.AssignedRoute.IworkinRouteW(Mybody);
			Mybody.Action=RespondersActions.ClearRoute ;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.TaskExecution_cleareRoute;								
			//Mybody.EndActionTrigger=null;
			Mybody.SensingeEnvironmentTrigger=null;

		}
		// ++++++ 5- +++++++
		else if ( Mybody.Action==RespondersActions.ClearRoute && 	Mybody.AssignedRoute.WreckageLevel==Level.None  ) //Mybody.SensingeEnvironmentTrigger==RespondersTriggers.RouteCleared													
		{							
			InformResult(ACLPerformative.InfromResultRoute , Mybody.assignedIncident.FRFSCcommander);

			Mybody.Action=RespondersActions.InfromResultRoute ;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.Comunication;    
			Mybody.SensingeEnvironmentTrigger= null;
			//System.out.println("                                       "  +Mybody.Id + " done route" );
		}
		// ++++++ 6- +++++++
		else if (Mybody.Action==RespondersActions.InfromResultRoute && Mybody.Acknowledged) 
		{
			Mybody.InterpretedTrigger= RespondersTriggers.DoneTask;
			Mybody.Acknowledged=false;Mybody.Sending=false; 

			//System.out.println("                                       "  +Mybody.Id + " done infrom " );

		}

	}

	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	// FireFighter- Search and carry casualties to safe location (CP)  until CCS established  (Centralized )
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^	
	private void FireFighterBehavior_CarryCasualty ()	
	{
		// ++++++ 1- +++++++
		if ( Mybody.CommTrigger==RespondersTriggers.GetCommandCarryCasualtytoCCS ) 													
		{																		
			Mybody.Action=RespondersActions.GetCommandCarryCasualtytoCCS  ;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.Comunication; //TaskPlanning;	
			Mybody.EndofCurrentAction=  InputFile.GetCommand_duration  ; 	
			Mybody.CommTrigger=null;	
			//System.out.println("                                       "  + Mybody.Id + " GetCommandCarryCasualtytoCCS" );
		}	

		// ++++++ 2- +++++++
		else if (  Mybody.Action==RespondersActions.GetCommandCarryCasualtytoCCS  && Mybody.EndActionTrigger== RespondersTriggers.EndingAction) 													
		{		
			Mybody.CurrentSender.Acknowledg(Mybody) ;

			Mybody.Assign_DestinationCasualty(Mybody.TargetCasualty) ;	
			Mybody.Action=RespondersActions.GoToCasualty;
			//			Mybody.BehaviourType2=RespondersBehaviourTypes2.Movementonincidentsite ; //TaskPlanning ;?
			Mybody.EndActionTrigger=null;
			//System.out.println("                                       "  + Mybody.Id + " GoToCasualty"  + Mybody.TargetCasualty.ID );
		}
		// ++++++ 3- +++++++
		else if (Mybody.Action==RespondersActions.GoToCasualty && Mybody.SensingeEnvironmentTrigger==null)
		{			
			Mybody.Walk();
		}
		//-----------------------------------------------------------1
		// ++++++ 4- +++++++		
		else if (Mybody.Action==RespondersActions.GoToCasualty  && Mybody.SensingeEnvironmentTrigger==RespondersTriggers.ArrivedCasualty 
				&& (  Mybody.OnhandCasualty.Status==CasualtyStatus.Trapped || Mybody.OnhandCasualty.Status==CasualtyStatus.PreTreatedTrapped )  )  //Mybody.OnhandCasualty.Status==CasualtyStatus.PreTreated
		{	
			//System.out.println("                                       "  + Mybody.Id + " ArrivedCasualty1 ..Casualty trapped " );
			//if ( Mybody.TargetCasualty.IsAssignedToResponder==true )
			//	{Mybody.SensingeEnvironmentTrigger= RespondersTriggers.CasualtyHasanotherFireFighter; System.out.println("                                       "  + Mybody.Id + " erorrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrr " );}
			//	else
			{
				//Casualty trapped
				Mybody.OnhandCasualty.IsAssignedToResponder = true;

				Mybody.Action=RespondersActions.Noaction ;
				Mybody.BehaviourType2=RespondersBehaviourTypes2.CasualtyTransferDelay ; //???????????????????????????????????????????????????????????
				Taskoption=  44;			
				Mybody.Acknowledged=false;Mybody.Sending=false; 
				System.out.println("                                       "  + Mybody.Id +  " Extrication   wait" + Mybody.OnhandCasualty.ID );
			}
		}
		// ++++++ 4- +++++++
		else if ( Mybody.Action==RespondersActions.Noaction &&  Taskoption==44 && Mybody.OnhandCasualty.Status==CasualtyStatus.PreTreatedTrapped)   
		{									
			ExtractActionS() ;
			Mybody.Action=RespondersActions.Extrication;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.TaskExecution_Extrication ;
			Taskoption=0;
			//System.out.println("                                       "  + Mybody.Id +  " Extrication   sart "  + Mybody.OnhandCasualty.ID );
		}		
		//-----------------------------------------------------------2
		// ++++++ 5- +++++++		
		else if (Mybody.Action==RespondersActions.GoToCasualty  && Mybody.SensingeEnvironmentTrigger==RespondersTriggers.ArrivedCasualty 
				&& Mybody.OnhandCasualty.Status!=CasualtyStatus.Trapped  && ! Mybody.OnhandCasualty.DeadByFire &&  Mybody.OnhandCasualty.Triage_tage==5 ) // after confirmed from ambulance 
		{

			//System.out.println("                                       "  + Mybody.Id + " ArrivedCasualty2 ... Casualty Dead	 " );
			if ( Mybody.TargetCasualty.IsAssignedToResponder==true )
				Mybody.SensingeEnvironmentTrigger= RespondersTriggers.CasualtyHasanotherFireFighter;
			else
			{
				//Casualty Dead	
				Mybody.OnhandCasualty.DeadByFire=true;  // make like sign  to avoid another fire fighter come back
				Mybody.OnhandCasualty.IsAssignedToResponder = true ;

				InformResult(ACLPerformative.InformResultCasualtyDead ,Mybody.AssignedSector.FSCcommander );  
				DirectToCasualty=false ;

				Mybody.Action=RespondersActions.InformResultCasualtyDead;	
				Mybody.BehaviourType2=RespondersBehaviourTypes2.Comunication;   		
				Mybody.SensingeEnvironmentTrigger=null;
			}
		}

		//-----------------------------------------------------------3
		// ++++++ 6- +++++++		
		else if (Mybody.Action==RespondersActions.GoToCasualty  && Mybody.SensingeEnvironmentTrigger==RespondersTriggers.ArrivedCasualty 
				&& Mybody.OnhandCasualty.Status!=CasualtyStatus.Trapped  &&  Mybody.OnhandCasualty.Triage_tage!=5  && ! Mybody.OnhandCasualty.IcanMove & Mybody.OnhandCasualty.Triage_tage<= 3  )
		{
			//System.out.println("                                       "  + Mybody.Id + " ArrivedCasualty3 ... Casualty injured  can not Move " );
			if ( Mybody.TargetCasualty.IsAssignedToResponder==true )
				Mybody.SensingeEnvironmentTrigger= RespondersTriggers.CasualtyHasanotherFireFighter;
			else
			{
				// Casualty injured 	
				Mybody.OnhandCasualty.IsAssignedToResponder = true;

				if ( ISCCSestablished  )
				{
					Mybody.Assign_DestinationCordon(Mybody.assignedIncident.CCStation.Location);
					TransferCasualtytoSafeOrCCSActionS();
					Mybody.Action=RespondersActions.CarryCasualtytoCCS ;
					Mybody.BehaviourType2=RespondersBehaviourTypes2.TaskExecution_CarrytoCCS ;
					Mybody.SensingeEnvironmentTrigger=null;
				}
				else
				{	//Mybody.Assign_DestinationCordon( Mybody.assignedIncident.IdentifyNearest_safelocation(Mybody.Return_CurrentLocation()) ) ;								

					//Mybody.Action=RespondersActions.Noaction ;
					//Mybody.BehaviourType=RespondersBehaviourTypes.WaitingDuring;	
					//Mybody.SensingeEnvironmentTrigger=null;

					Taskoption= 1;
					Mybody.Assign_DestinationCordon(Mybody.assignedIncident.collection_point);
					TransferCasualtytoSafeOrCCSActionS();
					Mybody.Action=RespondersActions.CarryCasualtytoCP ;
					Mybody.BehaviourType2=RespondersBehaviourTypes2.TaskExecution_CarrytoCCS ;
					Mybody.SensingeEnvironmentTrigger=null;  
				}
			}
		}
		//-----------------------------------------------------------4
		// ++++++ 7- +++++++		
		else if (Mybody.Action==RespondersActions.GoToCasualty  && Mybody.SensingeEnvironmentTrigger==RespondersTriggers.ArrivedCasualty 
				&& Mybody.OnhandCasualty.Status!=CasualtyStatus.Trapped  &&  Mybody.OnhandCasualty.Triage_tage!=5  && Mybody.OnhandCasualty.IcanMove &&Mybody.OnhandCasualty.IGetInstactionToWalk_and_walking==false && Mybody.OnhandCasualty.Triage_tage<= 3 ) //injured Casualty  
		{
			//System.out.println("                                       "  + Mybody.Id + " ArrivedCasualty4 ... Casualty injured  can  Move ******************************************** " );
			if ( Mybody.TargetCasualty.IsAssignedToResponder==true )
				Mybody.SensingeEnvironmentTrigger= RespondersTriggers.CasualtyHasanotherFireFighter;
			else
			{
				// Casualty injured 	
				Mybody.OnhandCasualty.IsAssignedToResponder = true;

				if ( ISCCSestablished  )									
				{ 
					//GuideCasualtytosafeOrCCSorRCAction(MovmentCasualty.TOCCS ,null  )	;	

					Mybody.Action=RespondersActions.DirectingCasualtyTOSafety ;
					Mybody.BehaviourType2=RespondersBehaviourTypes2.TaskExecution_Directcasulty ;
					Mybody.EndofCurrentAction=  InputFile.DirectCasualty_duration  ;
					Mybody.SensingeEnvironmentTrigger=null;
				}

				else
				{	//GuideCasualtytosafeOrCCSorRCAction(MovmentCasualty.TORandomsafelocation   );								

					//Mybody.Action=RespondersActions.Noaction ;
					//Mybody.BehaviourType=RespondersBehaviourTypes.WaitingDuring;	
					//Mybody.SensingeEnvironmentTrigger=null;
					//Taskoption=  2;

					//GuideCasualtytosafeOrCCSorRCAction(MovmentCasualty.TOCP ,MovmentCasualty.TOCCS  );
					Mybody.Action=RespondersActions.DirectingCasualtyTOSafety ;
					Mybody.BehaviourType2=RespondersBehaviourTypes2.TaskExecution_Directcasulty ;
					Mybody.EndofCurrentAction=  InputFile.DirectCasualty_duration  ;
					Mybody.SensingeEnvironmentTrigger=null;
				}
			}

		}
		//-----------------------------------------------------------5
		// ++++++ 8- +++++++		
		else if (Mybody.Action==RespondersActions.GoToCasualty  && Mybody.SensingeEnvironmentTrigger==RespondersTriggers.ArrivedCasualty 
				&& Mybody.OnhandCasualty.UnInjured_NOtYetStartowalk== 0 ) // Uninjured Casualty  
		{
			//System.out.println("                                       "  + Mybody.Id + " ArrivedCasualty5 ... Casualty Uninjured  can  Move " );
			if ( Mybody.TargetCasualty.IsAssignedToResponder==true )
				Mybody.SensingeEnvironmentTrigger= RespondersTriggers.CasualtyHasanotherFireFighter;
			else
			{
				// Casualty Uninjured 	
				Mybody.OnhandCasualty.IsAssignedToResponder = true;
				Mybody.OnhandCasualty.UnInjured_NOtYetStartowalk= 1;

				if ( ISRCestablished  )									
				{ 
					//GuideCasualtytosafeOrCCSorRCAction(MovmentCasualty.TORC ,null )	;
					Mybody.Action=RespondersActions.GuidUninjuriedCasualtyTORC ;
					Mybody.BehaviourType2=RespondersBehaviourTypes2.TaskExecution_Directcasulty ;
					Mybody.EndofCurrentAction=  InputFile.DirectCasualty_duration  ;
					Mybody.SensingeEnvironmentTrigger=null;

				}
				else						
				{
					//GuideCasualtytosafeOrCCSorRCAction(MovmentCasualty.TORandomsafelocation   );
					//Mybody.Action=RespondersActions.Noaction ;
					//Mybody.BehaviourType=RespondersBehaviourTypes.WaitingDuring;	
					//Mybody.SensingeEnvironmentTrigger=null;
					//Taskoption=  3;

					//GuideCasualtytosafeOrCCSorRCAction(MovmentCasualty.TOCP , MovmentCasualty.TORC );
					Mybody.Action=RespondersActions.GuidUninjuriedCasualtyTORC ;
					Mybody.BehaviourType2=RespondersBehaviourTypes2.TaskExecution_Directcasulty ;
					Mybody.EndofCurrentAction=  InputFile.DirectCasualty_duration  ;
					Mybody.SensingeEnvironmentTrigger=null;
				}			
			}
		}
		// ++++++ 9- +++++++
		else if ( Mybody.Action==RespondersActions.Extrication && 	Mybody.OnhandCasualty.Status==CasualtyStatus.Extracted) 												
		{				
			//System.out.println("                                       "  + Mybody.Id + " Extrication  " );
			ExtractActionE();

			if (Mybody.OnhandCasualty.Triage_tage==5 )

			{
				{
					//Casualty Dead	
					Mybody.OnhandCasualty.DeadByFire=true;  // make like sign  to avoid another fire fighter come back
					Mybody.WorkingonCasualty= null;

					InformResult(ACLPerformative.InformResultCasualtyDead ,Mybody.AssignedSector.FSCcommander );  

					Mybody.Action=RespondersActions.InformResultCasualtyDead;	
					Mybody.BehaviourType2=RespondersBehaviourTypes2.Comunication;   ///Sharinginfo;			
					Mybody.SensingeEnvironmentTrigger=null;
				}
			}
			else
			{

				if ( ISCCSestablished  )
				{
					Mybody.Assign_DestinationCordon(Mybody.assignedIncident.CCStation.Location);
					TransferCasualtytoSafeOrCCSActionS();
					Mybody.Action=RespondersActions.CarryCasualtytoCCS ;
					Mybody.BehaviourType2=RespondersBehaviourTypes2.TaskExecution_CarrytoCCS ;
					Mybody.SensingeEnvironmentTrigger=null;
				}
				else		
				{
					//Mybody.Assign_DestinationCordon( Mybody.assignedIncident.IdentifyNearest_safelocation(Mybody.Return_CurrentLocation()) ) ;	// wait
					//Mybody.Action=RespondersActions.Noaction ;
					//Mybody.BehaviourType=RespondersBehaviourTypes.WaitingDuring;	
					//Mybody.SensingeEnvironmentTrigger=null;

					Taskoption= 1;
					Mybody.Assign_DestinationCordon(Mybody.assignedIncident.collection_point);
					TransferCasualtytoSafeOrCCSActionS();
					Mybody.Action=RespondersActions.CarryCasualtytoCP ;
					Mybody.BehaviourType2=RespondersBehaviourTypes2.TaskExecution_CarrytoCCS ;
					Mybody.SensingeEnvironmentTrigger=null;
				}
			}
		}
		// ++++++ 10- +++++++
		else if (Mybody.Action==RespondersActions.Noaction &&   Mybody.OnhandCasualty!=null &&  ( ISCCSestablished )    && Taskoption==1 ) //( ISCCSestablished || Mybody.assignedIncident.CCStation.installed) 
		{
			Mybody.Assign_DestinationCordon(Mybody.assignedIncident.CCStation.Location);
			TransferCasualtytoSafeOrCCSActionS();
			Mybody.Action=RespondersActions.CarryCasualtytoCCS ;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.TaskExecution_CarrytoCCS ;	
			Taskoption= 0 ;
		}	
		//		// ++++++ 11- +++++++
		//		else if (Mybody.Action==RespondersActions.Noaction &&   Mybody.OnhandCasualty!=null &&  ( ISCCSestablished )    && Taskoption==2 )  // ( ISCCSestablished || Mybody.assignedIncident.CCStation.installed) 
		//		{
		//			GuideCasualtytosafeOrCCSorRCAction(MovmentCasualty.TOCCS ,null  )	;	
		//
		//			Mybody.Action=RespondersActions.DirectingCasualtyTOSafety ;
		//			Mybody.BehaviourType2=RespondersBehaviourTypes2.TaskExecution_Directcasulty ;
		//			Mybody.EndofCurrentAction=  InputFile.DirectCasualty_duration  ;
		//			Taskoption= 0 ;Mybody.waittttt=false;
		//		}	
		//		// ++++++ 12- +++++++
		//		else if (Mybody.Action==RespondersActions.Noaction &&   Mybody.OnhandCasualty!=null && (ISRCestablished )  && Taskoption==3 )  // (ISRCestablished || Mybody.assignedIncident.RC.installed)
		//		{
		//			GuideCasualtytosafeOrCCSorRCAction(MovmentCasualty.TORC ,null )	;
		//			Mybody.Action=RespondersActions.GuidUninjuriedCasualtyTORC ;
		//			Mybody.BehaviourType2=RespondersBehaviourTypes2.TaskExecution_Directcasulty ;
		//			Mybody.EndofCurrentAction=  InputFile.DirectCasualty_duration  ;
		//			Taskoption= 0 ;
		//		}	
		// ++++++ 13- +++++++
		else if (Mybody.Action==RespondersActions.CarryCasualtytoCCS && Mybody.SensingeEnvironmentTrigger==null    )
		{			
			Mybody.Walk();
			Mybody.OnhandCasualty.setLocation_and_move(Mybody.Return_CurrentLocation());	
		}
		// ++++++ 14- +++++++
		else if (Mybody.Action==RespondersActions.CarryCasualtytoCCS && Mybody.SensingeEnvironmentTrigger==RespondersTriggers.ArrivedTargetObject)
		{	
			if ( Mybody.assignedIncident.CCStation.AssigenCasualtytoTA(Mybody.OnhandCasualty))
			{
				TransferCasualtytoSafeOrCCSActionE() ;	
				Mybody.OnhandCasualty.IsAssignedToResponder = false ;
				Mybody.WorkingonCasualty= null;


				InformResult(ACLPerformative.InformResultCarryCasualtytoCCS ,Mybody.AssignedSector.FSCcommander );  	//inside message to My SC

				Mybody.OnhandCasualty=null;

				Mybody.Action=RespondersActions.InformResultCarryCasualtytoCCS;
				Mybody.BehaviourType2=RespondersBehaviourTypes2.Comunication ;               //Sharinginfo;			
				Mybody.SensingeEnvironmentTrigger=null;
				//System.out.println("                                       "  + Mybody.Id +  "add to CCS " + Mybody.OnhandCasualty.ID );
			}
		}
		// ++++++ 13- +++++++
		else if (Mybody.Action==RespondersActions.CarryCasualtytoCP && Mybody.SensingeEnvironmentTrigger==null    )
		{			
			Mybody.Walk();
			Mybody.OnhandCasualty.setLocation_and_move(Mybody.Return_CurrentLocation());	
		}
		// ++++++ 14- +++++++
		else if (Mybody.Action==RespondersActions.CarryCasualtytoCP && Mybody.SensingeEnvironmentTrigger==RespondersTriggers.ArrivedTargetObject)
		{	
			Mybody.Action=RespondersActions.Noaction ;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.CasualtyTransferDelay ;
			Mybody.SensingeEnvironmentTrigger=null;
			Taskoption=  1;
			//System.out.println("                                       "  + Mybody.Id +  "add to CCS " + Mybody.OnhandCasualty.ID );
		}	
		// ++++++ 15- +++++++
		else if ( (Mybody.Action==RespondersActions.DirectingCasualtyTOSafety  || Mybody.Action==RespondersActions.GuidUninjuriedCasualtyTORC)  &&  Mybody.EndActionTrigger== RespondersTriggers.EndingAction ) 													
		{	
			InformResult(ACLPerformative.InformResultDirectingCasualty ,Mybody.AssignedSector.FSCcommander );

			Mybody.OnhandCasualty.IsAssignedToResponder = false ;
			Mybody.OnhandCasualty=null;Mybody.WorkingonCasualty=null;

			Mybody.Action=RespondersActions.InformResultDirectingCasualty ;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.Comunication;  //Sharinginfo;			
			Mybody.EndActionTrigger=null;
		}			
		// ++++++ 16- +++++++
		else if (( Mybody.Action==RespondersActions.InformResultCarryCasualtytoCCS || Mybody.Action==RespondersActions.InformResultCasualtyDead || Mybody.Action==RespondersActions.InformResultDirectingCasualty ) && Mybody.Acknowledged ) 
		{						
			if (Mybody.Action==RespondersActions.InformResultCasualtyDead  ) {Mybody.OnhandCasualty.IsAssignedToResponder = false ;Mybody.OnhandCasualty=null;  Mybody.WorkingonCasualty=null; }

			if (Mybody.Action==RespondersActions.InformResultCarryCasualtytoCCS) Mybody.InterpretedTrigger= RespondersTriggers.IamNOTinMySectorScene;
			else if ( Mybody.Action==RespondersActions.InformResultCasualtyDead )		Mybody.InterpretedTrigger= RespondersTriggers.CasualtyDead;
			else if (  Mybody.Action==RespondersActions.InformResultDirectingCasualty) Mybody.InterpretedTrigger= RespondersTriggers.DoneTask ;

			Mybody.Acknowledged=false;Mybody.Sending=false; 
			//System.out.println("                                       "  + Mybody.Id +  " " + Mybody.InterpretedTrigger );
		}
	}
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	// FireFighter- Search and carry casualties to safe location (CP)  until CCS established  (Centralized )
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^		
	private void  FireFighterBehavior_SearchandCarryCasualty ()
	{
		//================================================================================================================ // from pramedic		 //parallel action		
		// ++++++ 1- +++++++
		parallelactionTimmer--;

		//		if ( Mybody.CommTrigger==RespondersTriggers.GetRequestCarryCasualtytoCCS   &&   Mybody.assignedIncident.TaskApproach_IN ==TaskAllocationApproach.Decentralized_Mutualadjustment ) 													
		//		{																		
		//			Mybody.InterpretedTrigger = RespondersTriggers.GetRequestCarryCasualtytoCCS ;
		//			parallelactionTimmer =  InputFile.GetCommand_duration  ; 	
		//			Mybody.CommTrigger=null;	
		//
		//			System.out.println("                                       ---------------------"  + Mybody.Id + " GetRequestCarryCasualtytoCCS  from pramedic" + Mybody.CurrentSender.Id + "  "  + NewTargetCasualty.ID  );					
		//		}	
		//		// ++++++ 2- +++++++
		//		else if (  Mybody.InterpretedTrigger == RespondersTriggers.GetRequestCarryCasualtytoCCS && parallelactionTimmer==0   ) 													
		//		{		
		//
		//			if ( ( Mybody.Action==RespondersActions.SearchCasualty || Mybody.Action==RespondersActions.GoToCasualty ) && Mybody.OnhandCasualty==null  && !DirectToCasualty )
		//			{
		//				Mybody.CurrentSender.Acknowledg(Mybody , true ) ;
		//				Mybody.TargetCasualty=NewTargetCasualty ;
		//				DirectToCasualty=true;	
		//
		//				Mybody.Assign_DestinationCasualty(Mybody.TargetCasualty) ;	
		//				Mybody.Action=RespondersActions.GoToCasualty;
		//				Mybody.BehaviourType2=RespondersBehaviourTypes2.Movementonincidentsite;  //TaskPlanning;
		//
		//				System.out.println("                                       ---------------------"   + Mybody.Id + " Yes " + Mybody.CurrentSender.Id + "  "  + NewTargetCasualty.ID  );	
		//			}
		//			else
		//			{
		//				Mybody.CurrentSender.Acknowledg(Mybody , false ) ;
		//				System.out.println("                                       ---------------------"   + Mybody.Id + " NO " + Mybody.CurrentSender.Id + "  "  + NewTargetCasualty.ID );	
		//			}
		//
		//			Mybody.EndActionTrigger=null;
		//			Mybody.InterpretedTrigger=null;
		//
		//		}				
		//================================================================================================================

		// ++++++ 1- +++++++
		if (Mybody.InterpretedTrigger==RespondersTriggers.waitingInstalling  || Mybody.InterpretedTrigger== RespondersTriggers.FirstTimeonLocation ||  Mybody.InterpretedTrigger== RespondersTriggers.DoneTask  )  //means DoneTask  of sector installed
		{
			if (Mybody.assignedIncident.TaskMechanism_IN==TaskAllocationMechanism.NoEnvironmentsectorization)
			{
				//Mybody.RandomDirectionSerach(); //Mybody.MyDirectiontowalk	
				//while (Mybody.MyDirectiontowalk== Mybody.AssignedSector.Startpoint)
				//	Mybody.RandomDirectionSerach();	

				Mybody.MyDirectiontowalk= Mybody.AssignedSector.Limitpoint ;
				Mybody.walkingstrategy=RandomWalking_StrategyType.OneDirection_sector;	
			}
			else if (Mybody.assignedIncident.TaskMechanism_IN==TaskAllocationMechanism.Environmentsectorization)
			{				
				//				if (Mybody.Randomizer.nextInt(2)==0 ) 
				//					Mybody.MyDirectiontowalk=Mybody.AssignedSector.centerofsectorPoint ;
				//				else 
				//					Mybody.MyDirectiontowalk=Mybody.AssignedSector.Limitpoint;	

				Mybody.walkingstrategy=RandomWalking_StrategyType.OneDirection_sector;	
			}

			Mybody.CasualtySeen_list.clear();			
			Mybody.StopDistnation=Mybody.AssignedSector.centerofsectorPoint; 			
			Mybody.ArrivedSTOP=false;

			Mybody.ExpandFieldofVision=false;
			Mybody.Setup_ConsiderSeverityofcasualtyinSearach(true ); // No Severity
			Mybody.Assign_DestinationLocation_Serach();
			Mybody.EndofCurrentAction=  InputFile.WalkingBeforSearch_duration ; 
			Mybody.RandomllySearachCasualty= false;


			Mybody.Action=RespondersActions.SearchCasualty;	
			Mybody.BehaviourType2=RespondersBehaviourTypes2.TaskPlanning;
			Mybody.InterpretedTrigger= null;


			//System.out.println("                                       "  + Mybody.Id + " zzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz  start walk " + Mybody.SensingeEnvironmentTrigger );
		}
		//===============================================================================================================

		// ++++++ 1- +++++++
		else if (  ( Mybody.Action==RespondersActions.GoToCasualty && Mybody.SensingeEnvironmentTrigger== RespondersTriggers.CasualtyHasanotherFireFighter)
				||Mybody.InterpretedTrigger== RespondersTriggers.ComeBack ) 
		{			 	
			Mybody.Action=RespondersActions.SearchCasualty;	
			Mybody.BehaviourType2=RespondersBehaviourTypes2.TaskPlanning;
			Mybody.Assign_DestinationLocation_Serach();	

			//=============			
			if ( Mybody.SensingeEnvironmentTrigger== RespondersTriggers.CasualtyHasanotherFireFighter)  Mybody.SensingeEnvironmentTrigger=null;
			if (  Mybody.InterpretedTrigger== RespondersTriggers.ComeBack)  Mybody.InterpretedTrigger=null ;

		}
		// ++++++ 2- +++++++
		else if (Mybody.Action==RespondersActions.SearchCasualty && Mybody.SensingeEnvironmentTrigger==null)
		{			
			if ( Mybody.EndActionTrigger== RespondersTriggers.EndingAction  )
			{Mybody.RandomllySearachCasualty= true; Mybody.MyDirectiontowalk= Mybody.AssignedSector.Limitpoint;	Mybody.EndActionTrigger=null; }
			Mybody.Walk();
		}
		// ++++++ 3- +++++++
		else if ( Mybody.Action==RespondersActions.SearchCasualty &&  Mybody.SensingeEnvironmentTrigger==RespondersTriggers.SensedCasualty  )
		{			
			Mybody.Assign_DestinationCasualty(Mybody.TargetCasualty) ;	

			Mybody.Action=RespondersActions.GoToCasualty;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.Movementonincidentsite; //TaskPlanning;?
			Mybody.SensingeEnvironmentTrigger=null;
		}
		// ++++++ 4- +++++++
		else if (Mybody.Action==RespondersActions.GoToCasualty && Mybody.SensingeEnvironmentTrigger==null)
		{			
			if ( Mybody.TargetCasualty.IsAssignedToResponder==true )
				Mybody.SensingeEnvironmentTrigger= RespondersTriggers.CasualtyHasanotherFireFighter;
			else
				Mybody.Walk();
		}	
		//================================================================================================================
		//-----------------------------------------------------------1
		// ++++++ 4- +++++++		
		else if (Mybody.Action==RespondersActions.GoToCasualty  && Mybody.SensingeEnvironmentTrigger==RespondersTriggers.ArrivedCasualty 
				&& Mybody.OnhandCasualty.Status==CasualtyStatus.Trapped )		
		{	
			//System.out.println("                                       "  + Mybody.Id + " ArrivedCasualty1 ..Casualty trapped " );
			if (Mybody.OnhandCasualty.IsAssignedToResponder==true ||  Mybody.TargetCasualty.IsAssignedToResponder==true )
			{Mybody.SensingeEnvironmentTrigger= RespondersTriggers.CasualtyHasanotherFireFighter; System.out.println("                                       "  + Mybody.Id + Mybody.OnhandCasualty.ID + "  CasualtyHasanotherFireFighter" );}
			else
			{
				//Casualty trapped
				Mybody.OnhandCasualty.IsAssignedToResponder = true;
				Mybody.WorkingonCasualty=Mybody.OnhandCasualty;
				Mybody.OnhandCasualty.UnderAction=CasualtyAction.WaitingPre_Treatment ;

				InformResult(ACLPerformative.InformTrappedcasualty,Mybody.AssignedSector.FSCcommander );		
				Mybody.Action=RespondersActions.InformTrappedcasualty ;
				Mybody.BehaviourType2=RespondersBehaviourTypes2.Comunication;  			
				Mybody.SensingeEnvironmentTrigger=null;
			}
		}			
		// ++++++ 4- +++++++
		else if ( Mybody.Action==RespondersActions.InformTrappedcasualty  && Mybody.Acknowledged  ) 
		{						
			Mybody.Action=RespondersActions.Noaction ;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.CasualtyTransferDelay ; //???????????????????????????????????????????????????????????
			Taskoption=  44;			
			Mybody.Acknowledged=false;Mybody.Sending=false; 
			//System.out.println("                                       "  + Mybody.Id +  " " + Mybody.InterpretedTrigger );
		}
		// ++++++ 4- +++++++
		else if ( Mybody.Action==RespondersActions.Noaction &&  Taskoption==44 && Mybody.OnhandCasualty.Status==CasualtyStatus.PreTreatedTrapped) // Mybody.OnhandCasualty.Status==CasualtyStatus.PreTreated
		{									
			ExtractActionS() ;
			Mybody.Action=RespondersActions.Extrication;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.TaskExecution_Extrication ;
			Taskoption=0;
			//System.out.println("                                       "  + Mybody.Id +  " " + Mybody.InterpretedTrigger );
		}			
		//-----------------------------------------------------------2
		// ++++++ 5- +++++++		
		else if (Mybody.Action==RespondersActions.GoToCasualty  && Mybody.SensingeEnvironmentTrigger==RespondersTriggers.ArrivedCasualty 
				&& Mybody.OnhandCasualty.Status!=CasualtyStatus.Trapped  &&  ! Mybody.OnhandCasualty.DeadByFire &&  Mybody.OnhandCasualty.Triage_tage==5 ) // after confirmed from ambulance 
		{
			//System.out.println("                                       "  + Mybody.Id + " ArrivedCasualty2 ... Casualty Dead	 " );
			if (Mybody.OnhandCasualty.IsAssignedToResponder==true ||  Mybody.TargetCasualty.IsAssignedToResponder==true )
				Mybody.SensingeEnvironmentTrigger= RespondersTriggers.CasualtyHasanotherFireFighter;
			else
			{
				//Casualty Dead	
				Mybody.OnhandCasualty.IsAssignedToResponder = true;
				Mybody.OnhandCasualty.DeadByFire=true;

				InformResult(ACLPerformative.InformResultCasualtyDead ,Mybody.AssignedSector.FSCcommander );  
				DirectToCasualty=false ;

				Mybody.Action=RespondersActions.InformResultCasualtyDead;	
				Mybody.BehaviourType2=RespondersBehaviourTypes2.Comunication ;   	
				Mybody.SensingeEnvironmentTrigger=null;
			}

		}
		//-----------------------------------------------------------3
		// ++++++ 6- +++++++		
		else if (Mybody.Action==RespondersActions.GoToCasualty  && Mybody.SensingeEnvironmentTrigger==RespondersTriggers.ArrivedCasualty 
				&& Mybody.OnhandCasualty.Status!=CasualtyStatus.Trapped  && ! Mybody.OnhandCasualty.IcanMove & Mybody.OnhandCasualty.Triage_tage<= 3  )
		{
			//System.out.println("                                       "  + Mybody.Id + " ArrivedCasualty3 ... Casualty injured  can not Move " );
			if (Mybody.OnhandCasualty.IsAssignedToResponder==true ||  Mybody.TargetCasualty.IsAssignedToResponder==true )
				Mybody.SensingeEnvironmentTrigger= RespondersTriggers.CasualtyHasanotherFireFighter;
			else
			{
				// Casualty cant move injured 	
				Mybody.OnhandCasualty.IsAssignedToResponder = true;

				if ( ISCCSestablished  )
				{
					Mybody.Assign_DestinationCordon(Mybody.assignedIncident.CCStation.Location);
					TransferCasualtytoSafeOrCCSActionS();
					Mybody.Action=RespondersActions.CarryCasualtytoCCS ;
					Mybody.BehaviourType2=RespondersBehaviourTypes2.TaskExecution_CarrytoCCS ;
					Mybody.SensingeEnvironmentTrigger=null;
				}
				else
				{	
					//Mybody.Assign_DestinationCordon( Mybody.assignedIncident.IdentifyNearest_safelocation(Mybody.Return_CurrentLocation()) ) ;								
					//Mybody.Action=RespondersActions.Noaction ;
					//Mybody.BehaviourType=RespondersBehaviourTypes.WaitingDuring;	
					//Mybody.SensingeEnvironmentTrigger=null;

					Taskoption= 1;
					Mybody.Assign_DestinationCordon(Mybody.assignedIncident.collection_point);
					TransferCasualtytoSafeOrCCSActionS();
					Mybody.Action=RespondersActions.CarryCasualtytoCP ;
					Mybody.BehaviourType2=RespondersBehaviourTypes2.TaskExecution_CarrytoCCS ;
					Mybody.SensingeEnvironmentTrigger=null;
				}
			}
		}
		//-----------------------------------------------------------4
		// ++++++ 7- +++++++		
		else if (Mybody.Action==RespondersActions.GoToCasualty  && Mybody.SensingeEnvironmentTrigger==RespondersTriggers.ArrivedCasualty 
				&& Mybody.OnhandCasualty.Status!=CasualtyStatus.Trapped   
				&& Mybody.OnhandCasualty.IcanMove&&  Mybody.OnhandCasualty.IGetInstactionToWalk_and_walking==false && Mybody.OnhandCasualty.Triage_tage<= 3 ) //injured Casualty  
		{
			//System.out.println("                                       "  + Mybody.Id + " ArrivedCasualty4 ... Casualty injured  can  Move ******************************************** " );
			if (Mybody.OnhandCasualty.IsAssignedToResponder==true ||  Mybody.TargetCasualty.IsAssignedToResponder==true )
				Mybody.SensingeEnvironmentTrigger= RespondersTriggers.CasualtyHasanotherFireFighter;
			else
			{
				// Casualty can move injured 	
				Mybody.OnhandCasualty.IsAssignedToResponder = true;

				if ( ISCCSestablished  )									
				{ 
					//GuideCasualtytosafeOrCCSorRCAction(MovmentCasualty.TOCCS ,null  )	;	
					GuideCasualtytosafeOrCCSorRCActionS();

					Mybody.Action=RespondersActions.DirectingCasualtyTOSafety ;
					Mybody.BehaviourType2=RespondersBehaviourTypes2.TaskExecution_Directcasulty ;
					Mybody.EndofCurrentAction=  InputFile.DirectCasualty_duration  ;
					Mybody.SensingeEnvironmentTrigger=null;
				}

				else
				{	//GuideCasualtytosafeOrCCSorRCAction(MovmentCasualty.TORandomsafelocation   );								

					//Mybody.Action=RespondersActions.Noaction ;
					//Mybody.BehaviourType=RespondersBehaviourTypes.WaitingDuring;	
					//Mybody.SensingeEnvironmentTrigger=null;
					//Taskoption=  2;

					//GuideCasualtytosafeOrCCSorRCAction(MovmentCasualty.TOCP ,MovmentCasualty.TOCCS );
					GuideCasualtytosafeOrCCSorRCActionS();
					Mybody.Action=RespondersActions.DirectingCasualtyTOSafety ;
					Mybody.BehaviourType2=RespondersBehaviourTypes2.TaskExecution_Directcasulty ;
					Mybody.EndofCurrentAction=  InputFile.DirectCasualty_duration  ;
					Mybody.SensingeEnvironmentTrigger=null;
				}				

			}

		}
		//-----------------------------------------------------------5
		// ++++++ 8- +++++++		
		else if (Mybody.Action==RespondersActions.GoToCasualty  && Mybody.SensingeEnvironmentTrigger==RespondersTriggers.ArrivedCasualty 
				&&  Mybody.OnhandCasualty.UnInjured_NOtYetStartowalk== 0 ) // Uninjured Casualty  
		{

			if (Mybody.OnhandCasualty.IsAssignedToResponder==true ||  Mybody.TargetCasualty.IsAssignedToResponder==true )
				Mybody.SensingeEnvironmentTrigger= RespondersTriggers.CasualtyHasanotherFireFighter;
			else
			{
				//System.out.println("                                       "  + Mybody.Id + " ArrivedCasualty5 ... Casualty Uninjured  can  Move " );
				// Casualty Uninjured 	
				Mybody.OnhandCasualty.IsAssignedToResponder = true;
				Mybody.OnhandCasualty.UnInjured_NOtYetStartowalk= 1;
				Mybody.OnhandCasualty.DirectedBy= Mybody;  //temp
				Mybody.OnhandCasualty.DirectedTime=Mybody.CurrentTick;   //temp

				if ( ISRCestablished  )									
				{ 
					//GuideCasualtytosafeOrCCSorRCAction(MovmentCasualty.TORC ,null )	;
					GuideCasualtytosafeOrCCSorRCActionS();
					Mybody.Action=RespondersActions.GuidUninjuriedCasualtyTORC ;
					Mybody.BehaviourType2=RespondersBehaviourTypes2.TaskExecution_Directcasulty ;
					Mybody.EndofCurrentAction=  InputFile.DirectCasualty_duration  ;
					Mybody.SensingeEnvironmentTrigger=null;

				}
				else						
				{
					//GuideCasualtytosafeOrCCSorRCAction(MovmentCasualty.TORandomsafelocation   );
					//Mybody.Action=RespondersActions.Noaction ;
					//Mybody.BehaviourType=RespondersBehaviourTypes.WaitingDuring;	
					//Mybody.SensingeEnvironmentTrigger=null;
					//Taskoption=  3;

					if (  Mybody.assignedIncident.collection_point == null)
						System.out.println("    error   Mybody.assignedIncident.collection_point                                "  + Mybody.OnhandCasualty.ID );

					//GuideCasualtytosafeOrCCSorRCAction(MovmentCasualty.TOCP ,MovmentCasualty.TORC    );
					GuideCasualtytosafeOrCCSorRCActionS();
					Mybody.Action=RespondersActions.GuidUninjuriedCasualtyTORC ;
					Mybody.BehaviourType2=RespondersBehaviourTypes2.TaskExecution_Directcasulty ;
					Mybody.EndofCurrentAction=  InputFile.DirectCasualty_duration  ;
					Mybody.SensingeEnvironmentTrigger=null;
				}



			}

		}
		//================================================================================================================
		// non of above
		else if (Mybody.Action==RespondersActions.GoToCasualty  && Mybody.SensingeEnvironmentTrigger==RespondersTriggers.ArrivedCasualty )
		{
			if (Mybody.OnhandCasualty.IsAssignedToResponder==true ||  Mybody.TargetCasualty.IsAssignedToResponder==true )
			{
				Mybody.SensingeEnvironmentTrigger= RespondersTriggers.CasualtyHasanotherFireFighter;
			}
			else
			{
				Mybody.SensingeEnvironmentTrigger= RespondersTriggers.CasualtyHasanotherFireFighter;
				System.out.println("                                       "  + Mybody.Id + " CasualtyHasanotherFireFighter--eroor-------------------------------------------------------------------------------------------------------------------------------=====================xxxxxxxxx============================= " );
				System.out.println(  Mybody.CurrentTick +"  " + Mybody.OnhandCasualty.UnInjured_NOtYetStartowalk +"  " + Mybody.OnhandCasualty.DirectedBy.Id +"  "+ Mybody.OnhandCasualty.ID+ "  " + Mybody.OnhandCasualty.DirectedTime  + Mybody.OnhandCasualty.IsAssignedToResponder +"  " + Mybody.OnhandCasualty.IcanMove +  " " +Mybody.OnhandCasualty.ID +"     "+  Mybody.OnhandCasualty.CurrentRPM  +"   " + Mybody.OnhandCasualty.Triage_tage   +"   " + Mybody.OnhandCasualty.Status  +"   " + Mybody.OnhandCasualty.UnderAction  ); 
			}

		}	
		//================================================================================================================
		// ++++++ 9- +++++++
		else if ( Mybody.Action==RespondersActions.Extrication && 	Mybody.OnhandCasualty.Status==CasualtyStatus.Extracted) 	// Mybody.SensingeEnvironmentTrigger==RespondersTriggers.CasualtyExtracted												
		{				
			//System.out.println("                                       "  + Mybody.Id + " Extrication  " );
			ExtractActionE();

			if(Mybody.OnhandCasualty.Triage_tage==5)
			{
				//Casualty Dead	
				Mybody.OnhandCasualty.DeadByFire=true;
				Mybody.WorkingonCasualty=null;

				InformResult(ACLPerformative.InformResultCasualtyDead ,Mybody.AssignedSector.FSCcommander );  

				DirectToCasualty=false ;
				Mybody.Action=RespondersActions.InformResultCasualtyDead;	
				Mybody.BehaviourType2=RespondersBehaviourTypes2.Comunication ;  	
				Mybody.SensingeEnvironmentTrigger=null;
			}
			else
			{
				if ( ISCCSestablished  )
				{
					Mybody.Assign_DestinationCordon(Mybody.assignedIncident.CCStation.Location);
					TransferCasualtytoSafeOrCCSActionS();
					Mybody.Action=RespondersActions.CarryCasualtytoCCS ;
					Mybody.BehaviourType2=RespondersBehaviourTypes2.TaskExecution_CarrytoCCS ;
					Mybody.SensingeEnvironmentTrigger=null;
				}
				else		
				{
					//Mybody.Assign_DestinationCordon( Mybody.assignedIncident.IdentifyNearest_safelocation(Mybody.Return_CurrentLocation()) ) ;	// wait
					//Mybody.Action=RespondersActions.Noaction ;
					//Mybody.BehaviourType=RespondersBehaviourTypes.WaitingDuring;	
					//Mybody.SensingeEnvironmentTrigger=null;

					Taskoption= 1;
					Mybody.Assign_DestinationCordon(Mybody.assignedIncident.collection_point);
					TransferCasualtytoSafeOrCCSActionS();
					Mybody.Action=RespondersActions.CarryCasualtytoCP ;
					Mybody.BehaviourType2=RespondersBehaviourTypes2.TaskExecution_CarrytoCCS ;
					Mybody.SensingeEnvironmentTrigger=null;
				}
			}
		}
		// ++++++ 10- +++++++
		else if (Mybody.Action==RespondersActions.Noaction &&   Mybody.OnhandCasualty!=null &&  ( ISCCSestablished )    && Taskoption==1 )  //( ISCCSestablished || Mybody.assignedIncident.CCStation.installed) 
		{
			Mybody.Assign_DestinationCordon(Mybody.assignedIncident.CCStation.Location);
			TransferCasualtytoSafeOrCCSActionS();
			Mybody.Action=RespondersActions.CarryCasualtytoCCS ;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.TaskExecution_CarrytoCCS ;
			Taskoption= 0 ;
		}	
		// ++++++ 11- +++++++
		//		else if (Mybody.Action==RespondersActions.Noaction &&   Mybody.OnhandCasualty!=null && ( ISCCSestablished )   && Taskoption==2 )  //( ISCCSestablished || Mybody.assignedIncident.CCStation.installed)
		//		{
		//			GuideCasualtytosafeOrCCSorRCAction(MovmentCasualty.TOCCS ,null  )	;	
		//
		//			Mybody.Action=RespondersActions.DirectingCasualtyTOSafety ;
		//			Mybody.BehaviourType2=RespondersBehaviourTypes2.TaskExecution_Directcasulty ;
		//			Mybody.EndofCurrentAction=  InputFile.DirectCasualty_duration  ;
		//			Taskoption= 0 ;
		//		}	
		//		// ++++++ 12- +++++++
		//		else if (Mybody.Action==RespondersActions.Noaction &&   Mybody.OnhandCasualty!=null && ( ISRCestablished )   && Taskoption==3 )  // ( ISRCestablished || Mybody.assignedIncident.RC.installed)
		//		{
		//			GuideCasualtytosafeOrCCSorRCAction(MovmentCasualty.TORC ,null )	;
		//			Mybody.Action=RespondersActions.GuidUninjuriedCasualtyTORC ;
		//			Mybody.BehaviourType2=RespondersBehaviourTypes2.TaskExecution_Directcasulty;
		//			Mybody.EndofCurrentAction=  InputFile.DirectCasualty_duration  ;
		//			Taskoption= 0 ;
		//		}	
		// ++++++ 13- +++++++
		else if (Mybody.Action==RespondersActions.CarryCasualtytoCCS && Mybody.SensingeEnvironmentTrigger==null    )
		{			
			Mybody.Walk();
			Mybody.OnhandCasualty.setLocation_and_move(Mybody.Return_CurrentLocation());	
		}
		// ++++++ 14- +++++++
		else if (Mybody.Action==RespondersActions.CarryCasualtytoCCS && Mybody.SensingeEnvironmentTrigger==RespondersTriggers.ArrivedTargetObject)
		{	
			if ( Mybody.assignedIncident.CCStation.AssigenCasualtytoTA(Mybody.OnhandCasualty))
			{				
				Mybody.OnhandCasualty.addedbyxxxxx=Mybody;  Mybody.OnhandCasualty.CurrentTickaddedxxxx=Mybody.CurrentTick ;
				if (Mybody.OnhandCasualty.error)
				{
					System.out.println("errrrrrrrrrrrrrrr in TA rrrrrrrrrrrrrrror TacticalArea  in adding  casulties"+ Mybody.OnhandCasualty.ID +"  to "    + "  "+  Mybody.OnhandCasualty.addedbyxxxxx.Id + "  "+ Mybody.OnhandCasualty.CurrentTickaddedxxxx + "  "+ Mybody.OnhandCasualty.Triage_tage + "  "+  Mybody.OnhandCasualty.Status + "  "+ Mybody.OnhandCasualty.IcanMove);   
					Mybody.OnhandCasualty.error=false;
				}

				TransferCasualtytoSafeOrCCSActionE() ;	
				Mybody.OnhandCasualty.IsAssignedToResponder = false ;
				Mybody.WorkingonCasualty= null;

				InformResult(ACLPerformative.InformResultCarryCasualtytoCCS ,Mybody.AssignedSector.FSCcommander );  	//inside message to My SC
				Mybody.ss++;
				Mybody.OnhandCasualty=null;
				DirectToCasualty=false ;

				Mybody.Action=RespondersActions.InformResultCarryCasualtytoCCS;
				Mybody.BehaviourType2=RespondersBehaviourTypes2.Comunication;    		
				Mybody.SensingeEnvironmentTrigger=null;
				//System.out.println("                                       "  + Mybody.Id +  "add to CCS " + Mybody.OnhandCasualty.ID );
			}
			else
				System.out.println("                                       "  + Mybody.Id +  "erooooorrrrrrrrrrrrrrrrrr FULL CCS " + Mybody.OnhandCasualty.ID );
		}
		// ++++++ 13- +++++++
		else if (Mybody.Action==RespondersActions.CarryCasualtytoCP && Mybody.SensingeEnvironmentTrigger==null    )
		{			
			Mybody.Walk();
			Mybody.OnhandCasualty.setLocation_and_move(Mybody.Return_CurrentLocation());	
		}
		// ++++++ 14- +++++++
		else if (Mybody.Action==RespondersActions.CarryCasualtytoCP && Mybody.SensingeEnvironmentTrigger==RespondersTriggers.ArrivedTargetObject)
		{	
			Mybody.Action=RespondersActions.Noaction ;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.CasualtyTransferDelay ;
			Mybody.SensingeEnvironmentTrigger=null;
			Taskoption=  1;
			//System.out.println("                                       "  + Mybody.Id +  "add to CCS " + Mybody.OnhandCasualty.ID );
		}	
		// ++++++ 15- +++++++
		else if ( (Mybody.Action==RespondersActions.DirectingCasualtyTOSafety  || Mybody.Action==RespondersActions.GuidUninjuriedCasualtyTORC) 
				&&  Mybody.EndActionTrigger== RespondersTriggers.EndingAction ) 													
		{	

			if (  Mybody.Action==RespondersActions.DirectingCasualtyTOSafety ) 
			{
				if ( ISCCSestablished  )									 
					GuideCasualtytosafeOrCCSorRCActionE(MovmentCasualty.TOCCS ,null  )	;	
				else	
					GuideCasualtytosafeOrCCSorRCActionE(MovmentCasualty.TOCP ,MovmentCasualty.TOCCS );

			}

			else if ( Mybody.Action==RespondersActions.GuidUninjuriedCasualtyTORC )
			{
				if ( ISRCestablished  )									
					GuideCasualtytosafeOrCCSorRCActionE(MovmentCasualty.TORC ,null )	;
				else						
					GuideCasualtytosafeOrCCSorRCActionE(MovmentCasualty.TOCP ,MovmentCasualty.TORC    );
			}

			InformResult(ACLPerformative.InformResultDirectingCasualty ,Mybody.AssignedSector.FSCcommander );
			Mybody.ss++;
			Mybody.OnhandCasualty.IsAssignedToResponder = false ;
			Mybody.OnhandCasualty=null;
			DirectToCasualty=false ;

			Mybody.Action=RespondersActions.InformResultDirectingCasualty ;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.Comunication;  		
			Mybody.EndActionTrigger=null;
		}			
		// ++++++ 16- +++++++
		else if ( Mybody.Action==RespondersActions.InformResultCarryCasualtytoCCS  && Mybody.Acknowledged ) 
		{
			//Mybody.InterpretedTrigger= RespondersTriggers.IamNOTinMySectorScene;
			Mybody.Action=RespondersActions.SearchCasualty;	
			Mybody.BehaviourType2=RespondersBehaviourTypes2.TaskPlanning;
			Mybody.Assign_DestinationLocation_Serach();

			Mybody.Acknowledged=false;Mybody.Sending=false; 
		}
		else if ((  Mybody.Action==RespondersActions.InformResultCasualtyDead || Mybody.Action==RespondersActions.InformResultDirectingCasualty ) && Mybody.Acknowledged ) 
		{						
			if (Mybody.Action==RespondersActions.InformResultCasualtyDead  ) {Mybody.OnhandCasualty.IsAssignedToResponder = false ;Mybody.OnhandCasualty=null;}
			Mybody.ss++;
			Mybody.Action=RespondersActions.SearchCasualty;	
			Mybody.BehaviourType2=RespondersBehaviourTypes2.TaskPlanning;
			Mybody.Assign_DestinationLocation_Serach();
			Mybody.Acknowledged=false;Mybody.Sending=false; 

			//System.out.println("                                       "  + Mybody.Id +  " " + Mybody.InterpretedTrigger );
		}		
		//================================================================================================================
		// ++++++ 9- +++++++
		else if (Mybody.Action==RespondersActions.SearchCasualty && (Mybody.SensingeEnvironmentTrigger== RespondersTriggers.Arrived_WalkedinAllScene || Mybody.SensingeEnvironmentTrigger == RespondersTriggers.Arrived_WalkedalongOneDirection ))
		{ 
			System.out.println("                                       "  + Mybody.Id  +" Arrived_Walked in//////////////////////////////////////////" +Mybody.OnhandCasualty + Mybody.WorkingonCasualty  ); 
			// Inform FSC
			Mybody.CurrentMessage  = new  ACL_Message( ACLPerformative.InformNomorecasualtyScene , Mybody, Mybody.assignedIncident.FRSCcommander1,null  ,  Mybody.CurrentTick,Mybody.assignedIncident.ComMechanism_level_BtoRes,1,TypeMesg.Inernal ) ;
			Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);

			Mybody.Action=RespondersActions.InformNomorecasualty;  //  or by SC commander !!
			Mybody.BehaviourType2=RespondersBehaviourTypes2.Comunication;    
			Mybody.SensingeEnvironmentTrigger=null;
			Mybody.Acknowledged=false;
		}
		// ++++++ 9- +++++++
		else if (Mybody.Action==RespondersActions.InformNomorecasualty && Mybody.Acknowledged) 
		{
			Mybody.InterpretedTrigger= RespondersTriggers.NoMorecasualty;
			Mybody.Acknowledged=false;Mybody.Sending=false; 
			Mybody.OnhandCasualty=null;

			//			Mybody.CurrentAssignedActivity=Fire_TaskType.None;
			//
			//			Mybody.Action=RespondersActions.Noaction;
			//			Mybody.BehaviourType2=RespondersBehaviourTypes2.WaitingDuring ;
			//			Mybody.InterpretedTrigger=null;	
			//			

			//System.out.println("                                       "  + Mybody.Id  +" done Searchecasulty**********************************" ); 
		}





	}

} //end class
