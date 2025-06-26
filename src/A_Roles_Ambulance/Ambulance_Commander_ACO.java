package A_Roles_Ambulance;

import java.util.ArrayList;
import java.util.List;
import A_Agents.EOC;
import A_Agents.Hospital;
import A_Agents.Responder;
import A_Agents.Responder_Ambulance;
import B_Classes.HospitalRecord;
import B_Communication.ACL_Message;
import B_Communication.Command;
import B_Communication.Report;
import C_SimulationInput.InputFile;
import D_Ontology.BuildStaticFuction;
import D_Ontology.Ontology.ACLPerformative;
import D_Ontology.Ontology.Allocation_Strategy;
import D_Ontology.Ontology.Ambulance_ResponderRole;
import D_Ontology.Ontology.Ambulance_TaskType;
import D_Ontology.Ontology.ReportSummery;
import D_Ontology.Ontology.RespondersActions;
import D_Ontology.Ontology.RespondersBehaviourTypes1;
import D_Ontology.Ontology.RespondersTriggers;
import D_Ontology.Ontology.TypeMesg;


public class Ambulance_Commander_ACO {

	Responder_Ambulance Mybody;
	boolean TempACO=true;
	EOC  EOC=null;  
	//----------------------------------------------------
	int CurrentLastNeedsend=0 ,CurrentLastReservedBedssend=0;
	HospitalRecord CurrentHospitalRecord=null;
	Report CurrentReport=null;
	Report EOCCurrentReport=null;
	Hospital CurrentHospitalSender=null;

	//----------------------------------------------------

	int TotalGetbed=0,TotalNOTgetbed=0, TotalNeedbed=0  , current_meet ; //indicator_meet  TotalGetbed- TotalNeedbed +ve good  -ve ....look for hospital
	boolean  newHospital=false ,LookforH=false;
	boolean FirstReport=false,UpdateReport=false,EndReport=false;
	//----------------------------------------------------
	List<HospitalRecord> RecivingHospital_List = new ArrayList<HospitalRecord>(); 

	//##############################################################################################################################################################	
	public Ambulance_Commander_ACO ( Responder_Ambulance _Mybody) 
	{
		Mybody=_Mybody;	
		//Mybody.ColorCode= 9;

		Mybody.assignedIncident.ACOcommander=Mybody;
		EOC= Mybody.assignedIncident.EOC1;

		Mybody.PrvRoleinprint1 =Mybody.Role;
	}

	public void TemplookforHAction()   // Temporary action ------------------------------------------
	{
		// they know about all hospitals in Area

		for (Hospital  Hos: EOC.Hospital_list )
		{
			HospitalRecord H= new HospitalRecord (Hos , Hos.getAvailableBedsCount());
			RecivingHospital_List.add(H);
			TotalGetbed=TotalGetbed + Hos.getAvailableBedsCount();		
		}


		//order there location to its incident		
		for (int i= 1 ; i<=RecivingHospital_List.size() ;i++ )  //  more neasrt  1, 2, then 3
		{

			//ProirityCreation				
			HospitalRecord NearestHosp =null;
			double MinDistance=99999999;

			for (HospitalRecord  Hosp:RecivingHospital_List)
			{
				double realDistance = BuildStaticFuction.Generate_Shortest_Path_byusingNode( Mybody.context, Mybody.geography ,Mybody.assignedIncident.BasicNode, Hosp.RecivingHospital.Node   ,true ) ;

				if( realDistance <= MinDistance && Hosp.OrderofNearsttomyLocation==-1 )
				{ MinDistance=realDistance;NearestHosp = Hosp; }			 
			}
			NearestHosp.OrderofNearsttomyLocation=i;

		}

		for (HospitalRecord  h :RecivingHospital_List) 			
			System.out.println("___________________________________________________ACO Hospital list: " + h.RecivingHospital.ID +"     " +h.OrderofNearsttomyLocation );
	}

	//##############################################################################################################################################################
	public void  Commander_ACO_InterpretationMessage()
	{
		boolean  done= true;
		ACL_Message currentmsg =  Mybody.Message_inbox.get(Mybody.Lastmessagereaded);		 			
		Mybody.Lastmessagereaded++;
		
		Mybody.SendingReciving_External= false ; Mybody.SendingReciving_internal=false; 
		if (  currentmsg.Inernal) Mybody.SendingReciving_internal= true ;
		else if (  currentmsg.External) Mybody.SendingReciving_External=true ;

		if ( currentmsg.sender instanceof Responder )
			Mybody.CurrentSender= (Responder)currentmsg.sender;

		else if ( currentmsg.sender instanceof Hospital  )
			CurrentHospitalSender=(Hospital)currentmsg.sender;

		switch( currentmsg.performative) {
		case Command :
			Mybody.CurrentCommandRequest=((Command) currentmsg.content);

			//++++++++++++++++++++++++++++++++++++++
			if ( Mybody.CurrentSender instanceof Responder_Ambulance &&  Mybody.CurrentCommandRequest.commandType1 ==Ambulance_TaskType.LookforHospital )  
			{						
				CurrentLastNeedsend	 =Mybody.CurrentCommandRequest.need;
				Mybody.CommTrigger= RespondersTriggers.GetCommandLookforHospital ;
				//Mybody.CurrentSender= (Responder)currentmsg.sender;
			}

			//++++++++++++++++++++++++++++++++++++++
			break;
		case AcceptRequest:
			Mybody.CommTrigger= RespondersTriggers.GetNewRecivingHospital;
			CurrentHospitalRecord =((HospitalRecord ) currentmsg.content);
			//from hospital;
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

	//##############################################################################################################################################################	
	// Create Plan-thinking for first time
	public void Implmenting_plan() {

	}
	// Resource Allocation 		
	public Hospital LookforHospitalforCasualty(Allocation_Strategy Strategy) 
	{
		Hospital  selectedHospital=null ;
		switch(Strategy) {
		case FIFO:	

			break;
		case Severity_RYGPriorty:

			break;

		case Severity_RPMPriorty:

			break; }

		return selectedHospital;

	}

	public HospitalRecord IsThereNewRecivingHospital()
	{	
		HospitalRecord HH=null;
		for (HospitalRecord H: RecivingHospital_List ) 
			if ( ! H.checkedACO)	
			{HH=H; H.checkedACO=true; break;}
		return HH;

	}
	//______________________________________________________________________________________________________________________________________________________________
	//##############################################################################################################################################################
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	// ACO commander - General 
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	public void ACOBehavior()	
	{		
		//*************************************************************************
		// 1- FirstArrival respond
		if( Mybody.SensingeEnvironmentTrigger== RespondersTriggers.FirstArrival_NoResponder ) 				
		{
			TempACO=true;
			Mybody.CurrentAssignedActivity=Ambulance_TaskType.CoordinateFirstArrival;
		}
		// 2- Coordinate Communication
		else if (Mybody.CommTrigger==RespondersTriggers.AssigendRolebyAIC	) 
		{
			TempACO=false;
			Mybody.CurrentAssignedActivity=Ambulance_TaskType.CoordinateCommunication ;
			//System.out.println( "  " +Mybody.Id  +" AssigendRolebyAIC " +Mybody.Role + "88888888888888888888888888888888" );
		}	
		// 3-Ending
		else if (Mybody.CommTrigger==RespondersTriggers.ENDER )   
		{			
			System.out.println( "  "+ Mybody.Id  +" GO back to Vehicle " +Mybody.Role );		
			Mybody.Role=Ambulance_ResponderRole.None;
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
	//  TempACO - Coordinate FirstArrival
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^	
	private void CommanderBehavior_CoordinateFirstArrival()
	{
		// ++++++ 1- +++++++
		if( Mybody.SensingeEnvironmentTrigger== RespondersTriggers.FirstArrival_NoResponder)
		{									
			Report Report1 =new Report();
			Report1.Ambulance_FRSituationAssessment( Mybody.assignedIncident ,ReportSummery.ConfirmedorDeclared , 0  );

			Mybody.CurrentMessage = new  ACL_Message( ACLPerformative.InfromFirstArrival,  Mybody,EOC ,Report1,  Mybody.CurrentTick,Mybody.assignedIncident.ComMechanism_level_toEOC ,1 ,TypeMesg.External) ;
			Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
			
			Mybody.Action=RespondersActions.NotifyArrival;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Comunication;   			
			Mybody.SensingeEnvironmentTrigger=null;

			//System.out.println(Mybody.Id + " " + "sending11");
		}
		// ++++++ 2- +++++++
		else if (Mybody.Action==RespondersActions.NotifyArrival && Mybody. Acknowledged)
		{
			Mybody.Action=RespondersActions.Noaction;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.WaitingDuring ;
			Mybody.Acknowledged=false;Mybody.Sending=false; 
			System.out.println(Mybody.Id + " " + "done EOC sending");
		}
		//----------------------------------------------------------------------------------------------------
		// ++++++ 3- +++++++
		else if (Mybody.Action==RespondersActions.Noaction && Mybody.CommTrigger==RespondersTriggers.GetReport)
		{
			Mybody.Action=RespondersActions.GetReport;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Comunication; //Gatheringinfo;
			Mybody.EndofCurrentAction= InputFile.GetInfromation_FtoForR_Duration_Data ;
			Mybody.CommTrigger=null;
			//reciving=true;
		}
		// ++++++ 4- +++++++
		else if ( Mybody.Action==RespondersActions.GetReport && Mybody.EndActionTrigger== RespondersTriggers.EndingAction 	) 	
		{
			Mybody.CurrentSender.Acknowledg(Mybody); //reciving=true;	
			//System.out.println(Mybody.Id + " " + "GetReport  from AIC ");

			if (FirstReport)
			{
				Mybody.CurrentMessage  = new  ACL_Message( ACLPerformative.InformFirestSituationAssessmentReport,  Mybody,EOC  ,CurrentReport,  Mybody.CurrentTick ,Mybody.assignedIncident.ComMechanism_level_toEOC,1,TypeMesg.External ) ;
				Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
				FirstReport=false;
			}
			else
			{
				Mybody.CurrentMessage  = new  ACL_Message( ACLPerformative.InformSituationReport,  Mybody,EOC  ,CurrentReport,  Mybody.CurrentTick,Mybody.assignedIncident.ComMechanism_level_toEOC,1 ,TypeMesg.External ) ;
				Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
			}

			Mybody.Action=RespondersActions.InfromReport;  //SendReortToEOC
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Comunication ;  
			Mybody.EndActionTrigger= null;

		}		
		// ++++++ 5- +++++++
		else if (Mybody.Action==RespondersActions.InfromReport && Mybody. Acknowledged )
		{
			Mybody.Action=RespondersActions.Noaction;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.WaitingDuring;
			Mybody.Acknowledged=false;Mybody.Sending=false; 	
			//System.out.println(Mybody.Id + " Done Send Reort To EOC" );

			//Mybody.InterpretedTrigger= RespondersTriggers.TempACOofFirstArrival;	//no need for this like AIC
		}
		//-----------------------------------------------instruct to Leave--------------------------------------------------

		// ++++++ 6- +++++++
		else if (Mybody.Action==RespondersActions.Noaction && Mybody.CommTrigger==RespondersTriggers.Getinstructiontoleave)
		{
			Mybody.Action=RespondersActions.GetcammandRole;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Comunication; //RoleAssignment;
			Mybody.EndofCurrentAction= InputFile.GetInfromation_FtoForR_Duration_Notification ;
			Mybody.CommTrigger=null;  //reciving=true;			
		}	
		// ++++++ 7- +++++++
		else if ( Mybody.Action==RespondersActions.GetcammandRole &&  Mybody.EndActionTrigger==RespondersTriggers.EndingAction) 												
		{		

			Mybody.CurrentSender.Acknowledg(Mybody) ;		
			Mybody.EndActionTrigger=null;		
			//System.out.println(Mybody.Id +" ..... ACORolehanded " +Mybody.Role +Mybody.Message_inbox.size()  +"  " +Mybody.Lastmessagereaded );
			Mybody.ActivityAcceptCommunication=true;Mybody.Acknowledged=false;Mybody.Sending=false; 
			Mybody.Role=Ambulance_ResponderRole.Driver;	
			Mybody.PrvRoleinprint1=Mybody.Role ;
			Mybody.ColorCode=0;

			Mybody.Action=RespondersActions.Noaction ;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.RoleAssignment;
			//System.out.println(Mybody.Id +" .....****************************************************** " +Mybody.Role  );
		}		
	}

	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	// ACO - CoordinateCommunication
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	private void CommanderBehavior_CoordinateCommunication()
	{								
		// ++++++ 1- +++++++
		if (  Mybody.CommTrigger==RespondersTriggers.AssigendRolebyAIC )  
		{
			//do some thing
			Implmenting_plan();//nothing
			Mybody.Action=RespondersActions.FormulatePlan;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Planformulation ;
			Mybody.EndofCurrentAction=  InputFile.FormulatePlan_duration  ; 
			Mybody.CommTrigger=null; 

			//System.out.println(Mybody.Id + " com " );
		}
		// ++++++ 2- +++++++
		else if ( Mybody.Action==RespondersActions.FormulatePlan  && Mybody.EndActionTrigger== RespondersTriggers.EndingAction 	) 			 	
		{							
			Mybody.Action=RespondersActions.Noaction;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.WaitingDuring;
			Mybody.EndActionTrigger=null;						
		}

		//-------------------------------------------------------------------------------------------- (P1)

		//==========================Hospital need Reported==========================
		// ++++++ 3- +++++++
		else if (Mybody.Action==RespondersActions.Noaction &&  Mybody.CommTrigger== RespondersTriggers.GetCommandLookforHospital   )	//report	  										
		{	 												
			Mybody.Action=RespondersActions.GetCommandLookforHospital;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Comunication ;  
			Mybody.EndofCurrentAction= InputFile.GetInfromation_FtoForR_Duration_Data ; 
			Mybody.CommTrigger=null; //reciving=true;

			//System.out.println(Mybody.Id + "GetCommandLookforHospital " );
		}
		// ++++++ 4- +++++++
		else if ( Mybody.Action==RespondersActions.GetCommandLookforHospital  && Mybody.EndActionTrigger== RespondersTriggers.EndingAction 	) 			 	
		{			
			//UpdatePlan						
			TotalNeedbed=TotalNeedbed+CurrentLastNeedsend;
			LookforH= true;

			Mybody.CurrentSender.Acknowledg(Mybody);  //reciving=true;	// F-F or radio

			Mybody.Action=RespondersActions.UpdatePlan;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Planformulation ;
			Mybody.EndofCurrentAction= InputFile.UdatePlan_duration ; 
			Mybody.EndActionTrigger=null; 
		}
		//==========================Hospital capacity Reported==========================
		// ++++++ 5- +++++++
		else if (Mybody.Action==RespondersActions.Noaction &&  Mybody.CommTrigger== RespondersTriggers.GetNewRecivingHospital   )	//report	  										
		{	 												
			//UpdatePlan
			TotalGetbed=TotalGetbed + CurrentHospitalRecord.CurrentReservedBedsCount ;
			RecivingHospital_List.add(CurrentHospitalRecord) ;

			Mybody.Action=RespondersActions.GetRecivingHospitalResult;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Comunication ;  
			Mybody.EndofCurrentAction= InputFile.GetInfromation_FtoForR_Duration_Data ; 
			Mybody.CommTrigger=null; 	
		}
		// ++++++ 6- +++++++
		else if ( Mybody.Action==RespondersActions.GetRecivingHospitalResult  && Mybody.EndActionTrigger== RespondersTriggers.EndingAction 	) 			 	
		{			

			CurrentHospitalSender.Acknowledg();	 

			Mybody.Action=RespondersActions.UpdatePlan;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Planformulation ;
			Mybody.EndofCurrentAction= InputFile.UdatePlan_duration ; 
			Mybody.EndActionTrigger=null; 
		}
		//============================================================ ALL
		// ++++++ 7- +++++++
		else if (Mybody.Action==RespondersActions.UpdatePlan && Mybody.EndActionTrigger== RespondersTriggers.EndingAction  )
		{						
			Mybody.Action=RespondersActions.Noaction;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.WaitingDuring;
			Mybody.EndActionTrigger=null;
		}
		//-------------------------------------------------------------------------------------------- (P2)
		// ++++++ 8- +++++++
		else if (Mybody.Action==RespondersActions.Noaction && Mybody.CommTrigger==RespondersTriggers.GetReport)
		{
			Mybody.Action=RespondersActions.GetReport;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Comunication ; 
			Mybody.EndofCurrentAction= InputFile.GetReport_duration ;
			Mybody.CommTrigger=null;   //reciving=true;
		}
		// ++++++ 9- +++++++
		else if ( Mybody.Action==RespondersActions.GetReport && Mybody.EndActionTrigger== RespondersTriggers.EndingAction 	) 	
		{
			Mybody.CurrentSender.Acknowledg(Mybody);   //reciving=true;	

			if (FirstReport)
				{
				Mybody.CurrentMessage  = new  ACL_Message( ACLPerformative.InformFirestSituationAssessmentReport,  Mybody,EOC ,CurrentReport,  Mybody.CurrentTick ,Mybody.assignedIncident.ComMechanism_level_toEOC,1 ,TypeMesg.External) ;
				Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
				}
			else if ( UpdateReport)
				{
				Mybody.CurrentMessage  = new  ACL_Message( ACLPerformative.InformSituationReport,  Mybody,EOC  ,CurrentReport,  Mybody.CurrentTick,Mybody.assignedIncident.ComMechanism_level_toEOC,1 ,TypeMesg.External  ) ;
				Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
				}
			else if ( EndReport)
				{
				Mybody.CurrentMessage  = new  ACL_Message( ACLPerformative.InformERendReport,  Mybody,EOC  ,CurrentReport,  Mybody.CurrentTick,Mybody.assignedIncident.ComMechanism_level_toEOC,1  ,TypeMesg.External ) ;
				Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
				}

			FirstReport=false;UpdateReport=false;EndReport=false;

			Mybody.Action=RespondersActions.InfromReport;  //SendReortToEOC
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Comunication; 
			Mybody.EndActionTrigger= null;
		}		
		// ++++++ 10- +++++++
		else if (Mybody.Action==RespondersActions.InfromReport && Mybody. Acknowledged )
		{
			Mybody.Action=RespondersActions.Noaction;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.WaitingDuring;
			Mybody.Acknowledged=false;Mybody.Sending=false; 	

			//System.out.println(Mybody.Id  +  "  Action:" + Mybody.Action + " com:" + Mybody.CommTrigger + "   Nt:" + Mybody.InterpretedTrigger+ " en:" + Mybody.SensingeEnvironmentTrigger + " end:" + Mybody.EndActionTrigger);

		}
		//-------------------------------------------------------------------------------------------- (P3)

		// ++++++ 11- +++++++
		else if (  Mybody.Action==RespondersActions.Noaction  && LookforH )	//look for hospital
		{			

			//Hospital  selectedHospital=LookforHospitalforCasualty(InputFile.SelectionHospital_Strategy) ;
			//send to hosital or do some thing Mybody.CurrentMessage  = new  ACL_Message( ACLPerformative.Requste,  Mybody,EOC ,CurrentReport,  Mybody.CurrentTick,CommunicationMechanism.Phone  ) ;	

			TemplookforHAction(); // Temporary action 
			newHospital=true;
			LookforH=false;

			//System.out.println(Mybody.Id + "L H" );

			Mybody.Action=RespondersActions.RequestAdditionalResources;  //temp
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Comunication ;  	//temp
			Mybody.EndofCurrentAction= InputFile.GetInfromation_FtoForR_Duration_Data ; //temp
		}

		// ++++++ 12- +++++++
		else if (Mybody.Action==RespondersActions.RequestAdditionalResources && Mybody.EndActionTrigger== RespondersTriggers.EndingAction  )  //&& Mybody. Acknowledged 
		{
			Mybody.Action=RespondersActions.Noaction;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.WaitingDuring;
			//Mybody.Acknowledged=false;Mybody.Sending=false; 	
			Mybody.EndActionTrigger=null;
		}
		//-------------------------------------------------------------------------------------------- (P4)

		// ++++++ 11- +++++++
		else if (  Mybody.Action==RespondersActions.Noaction &&   newHospital  )	
		{			
			//send to AIC
			Mybody.CurrentMessage  = new  ACL_Message( ACLPerformative.InformNewReceivingHospital,  Mybody,Mybody.assignedIncident.AICcommander ,RecivingHospital_List,  Mybody.CurrentTick,Mybody.assignedIncident.ComMechanism_inControlArea,RecivingHospital_List.size(),TypeMesg.Inernal ) ;			
			Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
			newHospital=false;
			Mybody.Action=RespondersActions.InformNewReceivingHospital;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Comunication;
			newHospital=false;

			//System.out.println(Mybody.Id + "InformNewReceivingHospital" );
		}

		// ++++++ 12- +++++++
		else if (Mybody.Action==RespondersActions.InformNewReceivingHospital && Mybody. Acknowledged )
		{
			Mybody.Action=RespondersActions.Noaction;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.WaitingDuring;
			Mybody.Acknowledged=false;Mybody.Sending=false; 	
		}

	}

}


