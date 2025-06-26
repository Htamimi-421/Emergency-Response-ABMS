package A_Roles_Fire;

import java.util.ArrayList;
import java.util.List;
import A_Agents.EOC;
import A_Agents.Responder;
import A_Agents.Responder_Fire;
import B_Classes.HospitalRecord;
import B_Communication.ACL_Message;
import B_Communication.Report;
import C_SimulationInput.InputFile;
import D_Ontology.Ontology.ACLPerformative;
import D_Ontology.Ontology.Fire_ResponderRole;
import D_Ontology.Ontology.Fire_TaskType;
import D_Ontology.Ontology.ReportSummery;
import D_Ontology.Ontology.RespondersActions;
import D_Ontology.Ontology.RespondersBehaviourTypes1;
import D_Ontology.Ontology.RespondersBehaviourTypes2;
import D_Ontology.Ontology.RespondersTriggers;
import D_Ontology.Ontology.TypeMesg;

public class Fire_Commander_FCO  {

	Responder_Fire Mybody;
	boolean TempFCO=true;
	EOC  EOC=null;  

	//----------------------------------------------------
	Report CurrentReport=null;
	Report EOCCurrentReport=null;
	boolean FirstReport=false,UpdateReport=false,EndReport=false;
	boolean  NewReportfromEOC =false ;
	//----------------------------------------------------
	List<HospitalRecord> RecivingHospital_List = new ArrayList<HospitalRecord>(); 

	//##############################################################################################################################################################	
	public Fire_Commander_FCO ( Responder_Fire  _Mybody) 
	{
		Mybody=_Mybody;	
		//Mybody.ColorCode= 5;

		Mybody.assignedIncident.FCOcommander=Mybody;
		EOC= Mybody.assignedIncident.EOC1;

		Mybody.PrvRoleinprint2 =Mybody.Role;
	}

	//##############################################################################################################################################################
	public void  Commander_PCO_InterpretationMessage()
	{
		boolean  done= true;
		ACL_Message currentmsg =  Mybody.Message_inbox.get(Mybody.Lastmessagereaded);		 			
		Mybody.Lastmessagereaded++;
		
		Mybody.SendingReciving_External= false ; Mybody.SendingReciving_internal=false; 
		if (  currentmsg.Inernal) Mybody.SendingReciving_internal= true ;
		else if (  currentmsg.External) Mybody.SendingReciving_External=true ;

		if ( currentmsg.sender instanceof Responder )
			Mybody.CurrentSender= (Responder)currentmsg.sender;
		else
			; //

		switch( currentmsg.performative) {
		case EOCReport :
			Mybody.CommTrigger= RespondersTriggers.GetEOCReport;
			NewReportfromEOC=true;
			EOCCurrentReport =((Report ) currentmsg.content);
			break;
		case Instructiontoleave :
			Mybody.CommTrigger= RespondersTriggers.Getinstructiontoleave;
			break;	
		case InformFirestSituationAssessmentReport :
			Mybody.CommTrigger= RespondersTriggers.GetReport;
			FirstReport=true;
			CurrentReport =((Report ) currentmsg.content);
			//System.out.println("ACO geting");
			break;
		case InformSituationReport :
			Mybody.CommTrigger= RespondersTriggers.GetReport;
			FirstReport=false;
			UpdateReport=true;
			CurrentReport =((Report ) currentmsg.content);
			break;
		case InformERendReport :
			Mybody.CommTrigger= RespondersTriggers.GetReport;
			EndReport=true;
			CurrentReport =((Report ) currentmsg.content);
			break;
		case InformERend :
			Mybody.CommTrigger= RespondersTriggers.ENDER;

			break;	
		default:
			done= true;
		} // end switch

		//System.out.println(Mybody.Id + "  " + Mybody.CommTrigger + "  from  "+ Mybody.CurrentSender.Id  + "    Action:" + Mybody.Action);
	}	

	//______________________________________________________________________________________________________________________________________________________________
	//##############################################################################################################################################################
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	// FCO commander - General 
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	public void FCOBehavior()	
	{		
		//*************************************************************************
		// 1- FirstArrival respond
		if( Mybody.SensingeEnvironmentTrigger== RespondersTriggers.FirstArrival_NoResponder ) 				
		{
			TempFCO=true;
			Mybody.CurrentAssignedActivity=Fire_TaskType.CoordinateFirstArrival;
		}
		// 2- Coordinate Communication
		else if (Mybody.CommTrigger==RespondersTriggers.AssigendRolebyFIC	) 
		{
			TempFCO=false;
			Mybody.CurrentAssignedActivity=Fire_TaskType.CoordinateCommunication ;
			
			//System.out.println( "                                       "  +Mybody.Id  +" AssigendRolebyFIC	 " +Mybody.Role + "88888888888888888888888888888888" );
		}	
		// 3-Ending
		else if (Mybody.CommTrigger==RespondersTriggers.ENDER )   
		{
			System.out.println( "                                       "  +Mybody.Id  +" GO back to station  " +Mybody.Role );
			Mybody.PrvRoleinprint2=Mybody.Role ;
			Mybody.Role=Fire_ResponderRole.None;
		}
		//*************************************************************************
		switch(Mybody.CurrentAssignedActivity) {
		case CoordinateFirstArrival:
			CommanderBehavior_CoordinateFirstArrival();	
			break;
		case CoordinateCommunication :			
			CommanderBehavior_CoordinateCommunication();
			break;			
		case None:
			;
			break;}

		//System.out.println(Mybody.Id  +  "  Action:" + Mybody.Action + " com:" + Mybody.CommTrigger + "   Nt:" + Mybody.InterpretedTrigger+ " en:" + Mybody.SensingeEnvironmentTrigger + " end:" + Mybody.EndActionTrigger);
	}// end Behavior

	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	//  TempFCO - Coordinate FirstArrival
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^	
	private void CommanderBehavior_CoordinateFirstArrival()
	{
		// ++++++ 1- +++++++
		if( Mybody.SensingeEnvironmentTrigger== RespondersTriggers.FirstArrival_NoResponder)
		{						

			Report Report1 =new Report();
			Report1.Fire_FRSituationAssessment( Mybody.assignedIncident ,ReportSummery.ConfirmedorDeclared , 0  );

			Mybody.CurrentMessage = new  ACL_Message( ACLPerformative.InfromFirstArrival,  Mybody,EOC ,Report1,  Mybody.CurrentTick,Mybody.assignedIncident.ComMechanism_level_toEOC ,1,TypeMesg.External ) ;			
			Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
			
			Mybody.Action=RespondersActions.NotifyArrival;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.Comunication ; 			
			Mybody.SensingeEnvironmentTrigger=null;

			//System.out.println(Mybody.Id + " " + "sending");
		}
		// ++++++ 2- +++++++
		else if (Mybody.Action==RespondersActions.NotifyArrival && Mybody. Acknowledged)
		{
			Mybody.Action=RespondersActions.Noaction;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.WaitingDuring ;
			Mybody.Acknowledged=false;Mybody.Sending=false; 
			System.out.println(Mybody.Id + " " + "done EOC sending");
		}
		//----------------------------------------------------------------------------------------------------
		// ++++++ 3- +++++++
		else if (Mybody.Action==RespondersActions.Noaction && Mybody.CommTrigger==RespondersTriggers.GetReport)
		{
			Mybody.Action=RespondersActions.GetReport;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.Comunication ; 
			Mybody.EndofCurrentAction= InputFile.GetInfromation_FtoForR_Duration_Data ;
			Mybody.CommTrigger=null;	
			
			System.out.println("                                       "  + Mybody.Id  +" GetReport  " +Mybody.Role );
		}
		// ++++++ 4- +++++++
		else if ( Mybody.Action==RespondersActions.GetReport && Mybody.EndActionTrigger== RespondersTriggers.EndingAction 	) 	
		{
			Mybody.CurrentSender.Acknowledg(Mybody);	


			if (FirstReport)
			{
				Mybody.CurrentMessage  = new  ACL_Message( ACLPerformative.InformFirestSituationAssessmentReport,  Mybody,EOC  ,CurrentReport,  Mybody.CurrentTick ,Mybody.assignedIncident.ComMechanism_level_toEOC,1 ,TypeMesg.External ) ;
				Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
				FirstReport=false;
			}
			else
			{
				Mybody.CurrentMessage  = new  ACL_Message( ACLPerformative.InformSituationReport,  Mybody,EOC  ,CurrentReport,  Mybody.CurrentTick,Mybody.assignedIncident.ComMechanism_level_toEOC,1 ,TypeMesg.External ) ;
				Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
			}

			Mybody.Action=RespondersActions.InfromReport;  //SendReortToEOC
			Mybody.BehaviourType2=RespondersBehaviourTypes2.Comunication ; 
			Mybody.EndActionTrigger= null;
			System.out.println("                                       "  + Mybody.Id  +" InfromReport  " +Mybody.Role );
		}		
		// ++++++ 5- +++++++
		else if (Mybody.Action==RespondersActions.InfromReport && Mybody. Acknowledged )
		{
			Mybody.Action=RespondersActions.Noaction;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.WaitingDuring;
			Mybody.Acknowledged=false;Mybody.Sending=false; 	
			System.out.println("                                       "  +Mybody.Id + " Done Send Reort To EOC" );
			
			//Mybody.InterpretedTrigger= RespondersTriggers.TempACOofFirstArrival;	//no need for this like AIC
		}
		//-----------------------------------------------instruct to Leave--------------------------------------------------

		// ++++++ 6- +++++++
		else if (Mybody.Action==RespondersActions.Noaction && Mybody.CommTrigger==RespondersTriggers.Getinstructiontoleave)
		{
			Mybody.Action=RespondersActions.GetcammandRole;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.Comunication; //RoleAssignment;
			Mybody.EndofCurrentAction= InputFile.GetInfromation_FtoForR_Duration_Notification ;
			Mybody.CommTrigger=null;			
		}	
		// ++++++ 7- +++++++
		else if ( Mybody.Action==RespondersActions.GetcammandRole &&  Mybody.EndActionTrigger==RespondersTriggers.EndingAction) 												
		{		

			Mybody.CurrentSender.Acknowledg(Mybody) ;			
			Mybody.CommTrigger=RespondersTriggers.CORolehanded ;
			Mybody.EndActionTrigger=null;		
			
			Mybody.ActivityAcceptCommunication=true;Mybody.Acknowledged=false;Mybody.Sending=false; 
			Mybody.Role=Fire_ResponderRole.Driver;	
			Mybody.PrvRoleinprint2=Mybody.Role ;
			//System.out.println(Mybody.Id +" .....****************************************************** " +Mybody.Role  );
			
			Mybody.Action=RespondersActions.Noaction ;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.RoleAssignment;
		}		
	}

	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	// ACO - CoordinateCommunication
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	private void CommanderBehavior_CoordinateCommunication()
	{								
		// ++++++ 1- +++++++
		if (  Mybody.CommTrigger==RespondersTriggers.AssigendRolebyFIC )  
		{
			//do some thing
			Mybody.Action=RespondersActions.Noaction;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.WaitingDuring;
			Mybody.CommTrigger=null;		
		}
		//-------------------------------------------------------------------------------------------- (P1)
		// ++++++ 2- +++++++
		else if (Mybody.Action==RespondersActions.Noaction && Mybody.CommTrigger==RespondersTriggers.GetReport)
		{
			Mybody.Action=RespondersActions.GetReport;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.Comunication ; 
			Mybody.EndofCurrentAction= InputFile.GetReport_duration ;
			Mybody.CommTrigger=null;
		}
		// ++++++ 3- +++++++
		else if ( Mybody.Action==RespondersActions.GetReport && Mybody.EndActionTrigger== RespondersTriggers.EndingAction 	) 	
		{
			Mybody.CurrentSender.Acknowledg(Mybody);	

			if (FirstReport)
			{
				Mybody.CurrentMessage  = new  ACL_Message( ACLPerformative.InformFirestSituationAssessmentReport,  Mybody,EOC ,CurrentReport,  Mybody.CurrentTick ,Mybody.assignedIncident.ComMechanism_level_toEOC,1 ,TypeMesg.External ) ;
				Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
			}
				else if ( UpdateReport)
			{
				Mybody.CurrentMessage  = new  ACL_Message( ACLPerformative.InformSituationReport,  Mybody,EOC  ,CurrentReport,  Mybody.CurrentTick,Mybody.assignedIncident.ComMechanism_level_toEOC,1 ,TypeMesg.External ) ;
				Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
		}
			else if ( EndReport)
			{
				Mybody.CurrentMessage  = new  ACL_Message( ACLPerformative.InformERendReport,  Mybody,EOC  ,CurrentReport,  Mybody.CurrentTick,Mybody.assignedIncident.ComMechanism_level_toEOC ,1 ,TypeMesg.External ) ;
				Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
			}
			FirstReport=false;UpdateReport=false;EndReport=false;

			Mybody.Action=RespondersActions.InfromReport;  //SendReortToEOC
			Mybody.BehaviourType2=RespondersBehaviourTypes2.Comunication ; 
			Mybody.EndActionTrigger= null;
		}		
		// ++++++ 4- +++++++
		else if (Mybody.Action==RespondersActions.InfromReport && Mybody. Acknowledged )
		{
			Mybody.Action=RespondersActions.Noaction;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.WaitingDuring;
			Mybody.Acknowledged=false;Mybody.Sending=false; 	

			//System.out.println(Mybody.Id  +  "  Action:" + Mybody.Action + " com:" + Mybody.CommTrigger + "   Nt:" + Mybody.InterpretedTrigger+ " en:" + Mybody.SensingeEnvironmentTrigger + " end:" + Mybody.EndActionTrigger);

		}

		//-------------------------------------------------------------------------------------------- (P1)
		// ++++++ 5- +++++++
		else if (  Mybody.Action==RespondersActions.Noaction &&  Mybody.CommTrigger== RespondersTriggers.GetEOCReport &&  NewReportfromEOC  )	
		{			
			//send to PIC
			Mybody.CurrentMessage  = new  ACL_Message( ACLPerformative.InformNewReceivingEOCReport,  Mybody,Mybody.assignedIncident.PICcommander ,EOCCurrentReport,  Mybody.CurrentTick,Mybody.assignedIncident.ComMechanism_inControlArea ,1,TypeMesg.Inernal) ;			
			Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
			
			NewReportfromEOC=false;
			Mybody.Action=RespondersActions.InformNewReceivingEOCReport;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.Comunication ; 
			//System.out.println(Mybody.Id + "InformNewReceivingHospital" );
		}
		// ++++++ 6- +++++++
		else if (Mybody.Action==RespondersActions.InformNewReceivingEOCReport && Mybody. Acknowledged )
		{
			Mybody.Action=RespondersActions.Noaction;
			Mybody.BehaviourType2=RespondersBehaviourTypes2.WaitingDuring;
			Mybody.Acknowledged=false;Mybody.Sending=false; 	
		}


	}

}