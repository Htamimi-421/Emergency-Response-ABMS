package A_Roles_Ambulance;

import A_Agents.Casualty;
import A_Agents.Responder;
import A_Agents.Responder_Ambulance;
import A_Agents.Vehicle_Ambulance;
import B_Communication.ACL_Message;
import B_Communication.Casualty_info;
import B_Communication.Command;
import B_Communication.ISRecord;
import C_SimulationInput.InputFile;
import D_Ontology.BuildStaticFuction;
import D_Ontology.Ontology.ACLPerformative;
import D_Ontology.Ontology.Ambulance_ActivityType;
import D_Ontology.Ontology.Ambulance_ResponderRole;
import D_Ontology.Ontology.Ambulance_TaskType;
import D_Ontology.Ontology.CasualtyAction;
import D_Ontology.Ontology.CasualtyReportandTrackingMechanism;
import D_Ontology.Ontology.CasualtyStatus;
import D_Ontology.Ontology.CasualtyinfromationType;
import D_Ontology.Ontology.CommunicationMechanism;
import D_Ontology.Ontology.Fire_TaskType;
import D_Ontology.Ontology.RandomWalking_StrategyType;
import D_Ontology.Ontology.RespondersActions;
import D_Ontology.Ontology.RespondersBehaviourTypes1;
import D_Ontology.Ontology.RespondersTriggers;
import D_Ontology.Ontology.TaskAllocationApproach;
import D_Ontology.Ontology.TaskAllocationMechanism;
import D_Ontology.Ontology.TypeMesg;

public class Ambulance_Paramedic {

	Responder_Ambulance Mybody;
	
	Casualty TrappedCasualty=null;
	boolean IwillupdateaboutTrapp=false;
	boolean IamNewResponder=true ;
	boolean IgetNewActivity =false ;
	int counter=0 ;
	public  Vehicle_Ambulance AssignedVehicletoCasualty ;
	boolean  SafetyBriefandSectorEstablished= false ;
	Casualty  LastCasualty=null;
	boolean NEEDTOupdatetoCCO=false;
	
	
	boolean Ialready_Arrived_WalkedinAllScene=false;

	//##############################################################################################################################################################
	public Ambulance_Paramedic ( Responder_Ambulance _Mybody  ) 
	{
		Mybody=_Mybody;
		Mybody.EndofCurrentAction=0;
		Mybody.PrvRoleinprint1=Mybody.Role;
		
		
		if ( _Mybody.Id.equals("AmbRes_34"))
			_Mybody.ColorCode= 3;
	}

	//##############################################################################################################################################################
	public void  Paramedic_InterpretationMessage()
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

			//++++++++++++++++++++++++++++++++++++++ Form AIC
			if ( Mybody.CurrentSender instanceof Responder_Ambulance &&  Mybody.CurrentCommandRequest.commandActivityType1 == Ambulance_ActivityType.SceneResponse  )  
			{				
				Mybody.CommTrigger= RespondersTriggers.GetActivitySceneResponse;						
				Mybody.AssignedSector=Mybody.CurrentCommandRequest.TargetSector;

				IgetNewActivity= true ;

			}
			//++++++++++++++++++++++++++++++++++++++
			else if ( Mybody.CurrentSender instanceof Responder_Ambulance  &&  Mybody.CurrentCommandRequest.commandActivityType1 == Ambulance_ActivityType.CCSResponse   )  
			{				
				Mybody.CommTrigger= RespondersTriggers.GetActivityCCSResponse;  
				Mybody.AssignedTA=Mybody.CurrentCommandRequest.TargetTA ;

				IgetNewActivity= true ;
			}
			//======================================================================================================
			//++++++++++++++++++++++++++++++++++++++  From BZC
			else if ( Mybody.CurrentSender instanceof Responder_Ambulance &&  Mybody.CurrentCommandRequest.commandType1 ==  Ambulance_TaskType.SetupTacticalAreas && Mybody.CurrentAssignedMajorActivity_amb!=null  )  
			{				
				Mybody.CommTrigger= RespondersTriggers.GetcommandSetupTacticalAreas  ;
			}
			//++++++++++++++++++++++++++++++++++++++
			else if ( Mybody.CurrentSender instanceof Responder_Ambulance &&  Mybody.CurrentCommandRequest.commandType1 == Ambulance_TaskType.FieldTriage  )  
			{				
				Mybody.CommTrigger= RespondersTriggers.GetCommandTriage ;
				Mybody.TargetCasualty=Mybody.CurrentCommandRequest.TargetCasualty;

				//System.out.println("  "+Mybody.Id + " get" );

			}
			//++++++++++++++++++++++++++++++++++++++
			else if ( Mybody.CurrentSender instanceof Responder_Ambulance &&  Mybody.CurrentCommandRequest.commandType1 ==Ambulance_TaskType.Pre_RescueTreatment )  
			{				
				Mybody.CommTrigger= RespondersTriggers.GetCommandPre_RescueTreatment ;
				TrappedCasualty=Mybody.CurrentCommandRequest.TargetCasualty;
			}

			//++++++++++++++++++++++++++++++++++++++
			else if ( Mybody.CurrentSender instanceof Responder_Ambulance &&  Mybody.CurrentCommandRequest.commandType1 == Ambulance_TaskType.SecondryTriage  )  
			{				
				Mybody.CommTrigger= RespondersTriggers.GetCommandTriage ;

			}

			//++++++++++++++++++++++++++++++++++++++
			else if ( Mybody.CurrentSender instanceof Responder_Ambulance &&  Mybody.CurrentCommandRequest.commandType1 ==Ambulance_TaskType.TreatmentandTransfertoVehicle  && Mybody.CurrentCommandRequest.AssignedAmbulance ==null )  
			{				
				Mybody.CommTrigger= RespondersTriggers.GetCommandTreatment ;
				Mybody.TargetCasualty=Mybody.CurrentCommandRequest.TargetCasualty;
			}
			//++++++++++++++++++++++++++++++++++++++
			else if ( Mybody.CurrentSender instanceof Responder_Ambulance &&  Mybody.CurrentCommandRequest.commandType1 ==Ambulance_TaskType.TreatmentandTransfertoVehicle && Mybody.CurrentCommandRequest.AssignedAmbulance!=null  )  
			{				
				Mybody.CommTrigger= RespondersTriggers.GetCommandTransferToVehicle;
				AssignedVehicletoCasualty=(Vehicle_Ambulance) Mybody.CurrentCommandRequest.AssignedAmbulance ;
				Mybody.TargetCasualty=Mybody.CurrentCommandRequest.TargetCasualty; //used if not wait until finish this casualty

				//System.out.println("  "+Mybody.Id + " get" );
			}
			//++++++++++++++++++++++++++++++++++++++
			break;

			//		case AcceptRequest  :
			//			Mybody.CommTrigger= RespondersTriggers.AcceptRequest;
			//			break;
			//		case RejectRequest :
			//			Mybody.CommTrigger= RespondersTriggers.RejectRequest;
			//			break;	
			//++++++++++++++++++++++++++++++++++++++
		case InfromSafetyBriefandSectorEstablished : //SC
			SafetyBriefandSectorEstablished= true ;
			break;
		case InformNomorecasualty  :
			Mybody.CommTrigger= RespondersTriggers.NoMorecasualty;
			break;
		case InformSceneOprationsEND  :
			Mybody.CommTrigger= RespondersTriggers.ENDSceneOprations;
			//System.out.println("  ----------------------------------"+Mybody.Id + " get   RespondersTriggers.ENDSceneOprations   " +Mybody.Action  );
			break;	
		case InformReallocation :
			Mybody.CommTrigger= RespondersTriggers.GetCommandReallocation  ;
			break;
		case InformCCSOprationsEND  :
			Mybody.CommTrigger= RespondersTriggers.ENDCCSOprations;
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
	public void OnFieldTriageActionS(){	

		if (Mybody.OnhandCasualty.Status== CasualtyStatus.Triaged ) System.out.println( Mybody.Id  +"errrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrOnTriageActionSrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrr");


		if( Mybody.OnhandCasualty.CurrentRPM >= 1 && Mybody.OnhandCasualty.CurrentRPM <= 4 )
			Mybody.OnhandCasualty.Triage_tage=1;
		else if( Mybody.OnhandCasualty.CurrentRPM >= 5 && Mybody.OnhandCasualty.CurrentRPM <= 8 )
			Mybody.OnhandCasualty.Triage_tage=2;
		else if( Mybody.OnhandCasualty.CurrentRPM >= 9 && Mybody.OnhandCasualty.CurrentRPM <= 12 )
			Mybody.OnhandCasualty.Triage_tage=3;
		else  if( Mybody.OnhandCasualty.CurrentRPM == 0)
		{ Mybody.OnhandCasualty.Triage_tage=5; }

		//System.out.println( Mybody.Id + " +++++++++++++++++++++++++++++++++++++++ this dead         in triage                   " + Mybody.OnhandCasualty.ID  );

		Mybody.OnhandCasualty.Status= CasualtyStatus.Triaged;
		Mybody.OnhandCasualty.UnderAction=CasualtyAction.FieldTriage ;

		//color
		if (Mybody.OnhandCasualty.Triage_tage==1  || Mybody.OnhandCasualty.Triage_tage==2 || Mybody.OnhandCasualty.Triage_tage==3 )
			Mybody.OnhandCasualty.ColorCode=Mybody.OnhandCasualty.Triage_tage;
		else if (Mybody.OnhandCasualty.Triage_tage==5)
			Mybody.OnhandCasualty.ColorCode=5;

	}

	//----------------------------------------------------------------------------------------------------
	public void OnFieldTriageActionE(){	

		if (  Mybody.OnhandCasualty.Triage_tage==5 )
			Mybody.OnhandCasualty.UnderAction=CasualtyAction.DeceasedonScene ;
		else
			Mybody.OnhandCasualty.UnderAction=CasualtyAction.WaitingTransferDelay ;

		Mybody.WorkingonCasualty=null;

	}

	//----------------------------------------------------------------------------------------------------
	public boolean OnCCSTriageActionS(){	

		boolean  sendupdatetoCCO=false; 
		int new_triage=0;

		if( Mybody.OnhandCasualty.CurrentRPM >= 1 && Mybody.OnhandCasualty.CurrentRPM <= 4 )
			new_triage=1;
		else if( Mybody.OnhandCasualty.CurrentRPM >= 5 && Mybody.OnhandCasualty.CurrentRPM <= 8 )
			new_triage=2;
		else if( Mybody.OnhandCasualty.CurrentRPM >= 9 && Mybody.OnhandCasualty.CurrentRPM <= 12 )
			new_triage=3;
		else  if( Mybody.OnhandCasualty.CurrentRPM == 0)
		{ new_triage=5;   }

		if (  Mybody.OnhandCasualty.Triage_tage==99   ||  new_triage != Mybody.OnhandCasualty.Triage_tage   || Mybody.OnhandCasualty.LastTime_Triage==0 )
			sendupdatetoCCO=true;

		Mybody.OnhandCasualty.Triage_tage =new_triage ;

		//System.out.println( Mybody.Id + " +++++++++++++++++++++++++++++++++++++++ this dead         in triage                   " + Mybody.OnhandCasualty.ID  );

		if (Mybody.OnhandCasualty.Status== CasualtyStatus.Extracted)
			{ this.TrappedCasualty=Mybody.OnhandCasualty;
			IwillupdateaboutTrapp=true;	
			}

		Mybody.OnhandCasualty.Status= CasualtyStatus.Triaged;
		Mybody.OnhandCasualty.UnderAction=CasualtyAction.SecondTriage;

		if (Mybody.OnhandCasualty.Triage_tage==1  || Mybody.OnhandCasualty.Triage_tage==2 || Mybody.OnhandCasualty.Triage_tage==3 )
			Mybody.OnhandCasualty.ColorCode=Mybody.OnhandCasualty.Triage_tage;
		else if (Mybody.OnhandCasualty.Triage_tage==5)
			Mybody.OnhandCasualty.ColorCode=5;

		Mybody.OnhandCasualty.LastTime_Triage= Mybody.CurrentTick ;


		return sendupdatetoCCO ;
	}

	//----------------------------------------------------------------------------------------------------
	public void OnCCSTriageActionE(){	

		if (  Mybody.OnhandCasualty.Triage_tage==5)
			Mybody.OnhandCasualty.UnderAction=CasualtyAction.DeceasedonCCS ;
		else		
		{
			Mybody.OnhandCasualty.UnderAction=CasualtyAction.WaitingTreatment;

			if ( this.TrappedCasualty!=null)
			{
				Mybody.OnhandCasualty.UnderAction=CasualtyAction.WaitingTransfertoV ;
				this.TrappedCasualty=null;
				Mybody.OnhandCasualty.Status= CasualtyStatus.BLSTreated;
				
				
				System.out.println( " zzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz" + Mybody.OnhandCasualty.ID);                                                                                         
			}

		}

		Mybody.WorkingonCasualty=null;
	}

	//----------------------------------------------------------------------------------------------------
	////write  on card
	public void ReportResult(Ambulance_TaskType  Type)  {

		if ( Mybody.assignedIncident.CasualtyReport_Mechanism_IN == CasualtyReportandTrackingMechanism.Paper )
		{	
			Mybody.EndofCurrentAction=  InputFile.CasualtyReportPaper_duration;
		}
		//---------------------------------------------
		else if ( Mybody.assignedIncident.CasualtyReport_Mechanism_IN == CasualtyReportandTrackingMechanism.Electronic )
		{

			if (Type == Ambulance_TaskType.FieldTriage || Type == Ambulance_TaskType.Pre_RescueTreatment  )
			{
				ISRecord currentISRecord=null ;

				if (   Mybody.CurrentAssignedMajorActivity_amb==Ambulance_ActivityType.SceneResponse )
					currentISRecord = new ISRecord(Mybody.OnhandCasualty , Mybody ,Mybody.AssignedSector.SCcommander , Mybody.AssignedSector );
				else if (  Mybody.CurrentAssignedMajorActivity_amb==Ambulance_ActivityType.CCSResponse )
					currentISRecord = new ISRecord(Mybody.OnhandCasualty , Mybody , null , null );

				Mybody.assignedIncident.ISSystem.add(currentISRecord  ); 
			}
			else if (Type == Ambulance_TaskType.SecondryTriage )
			{
				ISRecord currentISRecord ;
				currentISRecord = Mybody.assignedIncident.ReturnRecordcasualtyISSystem( Mybody.OnhandCasualty  );

				if (currentISRecord == null)
				{
					currentISRecord = new ISRecord(Mybody.OnhandCasualty , Mybody ,null , null );
					Mybody.assignedIncident.ISSystem.add(currentISRecord  ); 
				}

			}		

			Mybody.EndofCurrentAction= InputFile.CasualtyReportElectronic_duration;
			
		}
	}

	//----------------------------------------------------------------------------------------------------
	public void InformResult(ACLPerformative xx   , Responder_Ambulance SendTO  ){	

		if ( xx == ACLPerformative.InfromResultSetupCCS )
		{

			Mybody.CurrentMessage  = new  ACL_Message( xx ,Mybody, SendTO ,null  , Mybody.CurrentTick, Mybody.assignedIncident.ComMechanism_level_BtoRes,1,TypeMesg.Inernal ) ;
			Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
		}

		else
		{
			if ( Mybody.assignedIncident.CasualtyReport_Mechanism_IN== CasualtyReportandTrackingMechanism.Paper )
			{
				Casualty_info ca_inf=new Casualty_info ( Mybody.OnhandCasualty  , Mybody.OnhandCasualty.Triage_tage  ) ;
				if ( Mybody.OnhandCasualty.Triage_tage==5  ) ca_inf.ThisFatlity=true; 
				if ( IwillupdateaboutTrapp )  ca_inf.IwillupdateaboutTrapparrivedCCS=true;

				Mybody.CurrentMessage  = new  ACL_Message( xx ,Mybody, SendTO , ca_inf , Mybody.CurrentTick, Mybody.assignedIncident.ComMechanism_level_BtoRes,1 ,TypeMesg.Inernal) ;
				Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);

			}
			else if ( Mybody.assignedIncident.CasualtyReport_Mechanism_IN == CasualtyReportandTrackingMechanism.Electronic )
			{				
				ISRecord currentISRecord = Mybody.assignedIncident.ReturnRecordcasualtyISSystem( Mybody.OnhandCasualty );					
				currentISRecord.LastupdateTime= Mybody.CurrentTick ;
				currentISRecord.Visible=false;

				if (xx == ACLPerformative.InformResultFieldTriage )	
					currentISRecord.UpdateISRecord_FieldTriage(Mybody,Mybody.OnhandCasualty.Triage_tage );
				if (xx == ACLPerformative.InformResultPre_RescueTreatment )	//???????????????????????????????????????????????????????????????
				{
					currentISRecord.UpdateISRecord_FieldTriageandPre_teatment(Mybody,Mybody.OnhandCasualty.Triage_tage );

					if (   Mybody.CurrentAssignedMajorActivity_amb==Ambulance_ActivityType.SceneResponse )
						currentISRecord.IssuedByAmbSC=true;	
					else if (  Mybody.CurrentAssignedMajorActivity_amb==Ambulance_ActivityType.CCSResponse )
						currentISRecord.IssuedByAmbCCO=true;	
				}

				if (xx == ACLPerformative.InformResultSecondTriage )	
				{	currentISRecord.UpdateISRecord_SecondTriage(Mybody,Mybody.OnhandCasualty.Triage_tage );
					if ( IwillupdateaboutTrapp ) currentISRecord.IwillupdateaboutTrapparrivedCCS=true;
				}
				if (xx == ACLPerformative.InformResultTreatment )	   
					currentISRecord.UpdateISRecord_Teatment(Mybody,Mybody.OnhandCasualty.Triage_tage);	
				if (xx == ACLPerformative.InformResultVTransfer )		
					currentISRecord.UpdateISRecord_VTransfer(Mybody ) ;		

				Mybody. Acknowledged=true; /////?????????  update Network
			}
		}
		
		Mybody.SendingReciving_External= false ;  Mybody.SendingReciving_internal= true ;
		
		
		IwillupdateaboutTrapp=false;

	}

	//----------------------------------------------------------------------------------------------------
	public void OnPre_TreatmentActionS( )
	{		
		//Assumption stabilization for some time 

		if( Mybody.OnhandCasualty.CurrentRPM >= 1 && Mybody.OnhandCasualty.CurrentRPM <= 4 )
		{ Mybody.OnhandCasualty.Triage_tage=1; Mybody.OnhandCasualty.Nextcheck=  Mybody.OnhandCasualty.Nextcheck + InputFile.stabilization_R  ;}
		else if( Mybody.OnhandCasualty.CurrentRPM >= 5 && Mybody.OnhandCasualty.CurrentRPM <= 8 )
		{ Mybody.OnhandCasualty.Triage_tage=2; Mybody.OnhandCasualty.Nextcheck=  Mybody.OnhandCasualty.Nextcheck + InputFile.stabilization_Y  ;}
		else if( Mybody.OnhandCasualty.CurrentRPM >= 9 && Mybody.OnhandCasualty.CurrentRPM < 12 )
		{ Mybody.OnhandCasualty.Triage_tage=3; Mybody.OnhandCasualty.Nextcheck=  Mybody.OnhandCasualty.Nextcheck + InputFile.stabilization_G  ;}
		else if(  Mybody.OnhandCasualty.CurrentRPM == 12)
		{ Mybody.OnhandCasualty.Triage_tage=3;}
		else  if( Mybody.OnhandCasualty.CurrentRPM == 0)
		{ Mybody.OnhandCasualty.Triage_tage=5;  }

		Mybody.OnhandCasualty.UnderAction=CasualtyAction.Pre_Treatment;


		//System.out.println( Mybody.Id + " +++++++++++++++++++++++++++++++++++++++ this dead    in treatment                      " + Mybody.OnhandCasualty.ID  );

	}
	
	//----------------------------------------------------------------------------------------------------
	public void OnPre_TreatmentActionE( )
	{
//		if (  Mybody.OnhandCasualty.Triage_tage==5 )
//			Mybody.OnhandCasualty.UnderAction=CasualtyAction.DeceasedonScene ;
//			else
//				Mybody.OnhandCasualty.UnderAction=CasualtyAction.WaitingTransferDelay ;

		Mybody.OnhandCasualty.Status= CasualtyStatus.PreTreatedTrapped ;		
		Mybody.OnhandCasualty.ColorCode=10;			
		Mybody.WorkingonCasualty=null;

	}

	//----------------------------------------------------------------------------------------------------
	public void OnTreatmentActionS( )
	{		

		//Assumption stabilization for some time

		if( Mybody.OnhandCasualty.CurrentRPM >= 1 && Mybody.OnhandCasualty.CurrentRPM <= 4 )
		{ Mybody.OnhandCasualty.Triage_tage=1; Mybody.OnhandCasualty.Nextcheck=  Mybody.OnhandCasualty.Nextcheck + InputFile.stabilization_R  ;}
		else if( Mybody.OnhandCasualty.CurrentRPM >= 5 && Mybody.OnhandCasualty.CurrentRPM <= 8 )
		{ Mybody.OnhandCasualty.Triage_tage=2; Mybody.OnhandCasualty.Nextcheck=  Mybody.OnhandCasualty.Nextcheck + InputFile.stabilization_Y  ;}
		else if( Mybody.OnhandCasualty.CurrentRPM >= 9 && Mybody.OnhandCasualty.CurrentRPM < 12 )
		{ Mybody.OnhandCasualty.Triage_tage=3; Mybody.OnhandCasualty.Nextcheck=  Mybody.OnhandCasualty.Nextcheck + InputFile.stabilization_G  ;}
		else if(  Mybody.OnhandCasualty.CurrentRPM == 12)
		{ Mybody.OnhandCasualty.Triage_tage=3;}
		else  if( Mybody.OnhandCasualty.CurrentRPM == 0)
		{ Mybody.OnhandCasualty.Triage_tage=5;  }

		Mybody.OnhandCasualty.Status= CasualtyStatus.BLSTreated;
		Mybody.OnhandCasualty.UnderAction=CasualtyAction.Treatment;

		//System.out.println( Mybody.Id + " +++++++++++++++++++++++++++++++++++++++ this dead    in treatment                      " + Mybody.OnhandCasualty.ID  );

		if (Mybody.OnhandCasualty.Triage_tage==1  || Mybody.OnhandCasualty.Triage_tage==2 || Mybody.OnhandCasualty.Triage_tage==3 )
			Mybody.OnhandCasualty.ColorCode=Mybody.OnhandCasualty.Triage_tage;
		else if (Mybody.OnhandCasualty.Triage_tage==5)
			Mybody.OnhandCasualty.ColorCode=5;
	}

	//----------------------------------------------------------------------------------------------------
	public void OnTreatmentActionE( )
	{
		if (  Mybody.OnhandCasualty.Triage_tage==5 )
			Mybody.OnhandCasualty.UnderAction=CasualtyAction.DeceasedonCCS ;
		else			
		{
			Mybody.OnhandCasualty.UnderAction=CasualtyAction.WaitingTransfertoV ;
			//Mybody.WorkingonCasualty=Mybody.OnhandCasualty ; //used only in Face to face communication  beacuse he free
		}

		Mybody.WorkingonCasualty=null;

	}

	//----------------------------------------------------------------------------------------------------
	public void OnTransferCasualtoVichelActionS( ) {

		Mybody.OnhandCasualty.UnderAction=CasualtyAction.TransferTovichel;
		Mybody.AssignedTA.RemoveCasualtyfromTA( Mybody.OnhandCasualty);
	}	

	//----------------------------------------------------------------------------------------------------
	public void loadingCasualtyinVichelActionS() 
	{
		Mybody.OnhandCasualty.UnderAction=CasualtyAction.LoadinginAmbulance;

	}

	//----------------------------------------------------------------------------------------------------
	public void loadingCasualtyinVichelActionE1() 
	{
		Mybody.OnhandCasualty.UnderAction=CasualtyAction.Travallingtohospital;
		Mybody.OnhandCasualty.TimeLeaveScene=Mybody.CurrentTick ;

		Mybody.OnhandCasualty.setLocation_and_move(AssignedVehicletoCasualty.getCurrent_Location());
		AssignedVehicletoCasualty.AssigenCasualty(Mybody.OnhandCasualty);  // add to room of car				
		AssignedVehicletoCasualty.ALLCasualitiesInRoom= true; //here we assume one casualty	

		AssignedVehicletoCasualty.AcompanywithCasualty1= true;
		AssignedVehicletoCasualty.ResponderacompanywithCasualty1= Mybody ;

	}

	//##############################################################################################################################################################	
	//													Interpreted Triggers 
	//##############################################################################################################################################################
	public boolean ThereisNewCasualtyinCCS()
	{	
		boolean  Result=false;

		for (Casualty ca: Mybody.AssignedTA.casualtiesinTA ) 
			if (ca.newaddetoCCS  ) 
			{
				Result=true;				
				break;
			}
		return Result;
	}

	//----------------------------------------------------------------------------------------------------
	public Casualty NearstNewCasualtyinCCS()
	{	

		//Smallest distance
		Casualty nominatedCasualty=null;
		double smallest_dis=999999 ; 
		int Hightriage=4 ;

		for (Casualty ca: Mybody.AssignedTA.casualtiesinTA ) 
			if ( ca.newaddetoCCS  )
			{
				double dis = BuildStaticFuction.DistanceC(Mybody.geography, Mybody.Return_CurrentLocation(),ca.getCurrentLocation());

				if (  ca.Triage_tage < Hightriage )
				{
					nominatedCasualty= ca ;
					Hightriage=ca.Triage_tage ;
					smallest_dis= dis ;
				}

				else if ( ca.Triage_tage == Hightriage )
				{

					if (dis<=smallest_dis  )
					{  
						nominatedCasualty= ca ;
						Hightriage=ca.Triage_tage ;
						smallest_dis= dis;
					}					
				}

			}

		
		
		double smallest_dis2=999999 ; 
		for (Casualty ca: Mybody.AssignedTA.casualtiesinTA ) 
			if ( ca.newaddetoCCS && ca.Triage_tage == 99 )
			{

				double dis = BuildStaticFuction.DistanceC(Mybody.geography, Mybody.Return_CurrentLocation(),ca.getCurrentLocation());

				if (dis<=smallest_dis  )
				{  nominatedCasualty= ca ;
				smallest_dis= dis;
				}			

			}
		


		if (nominatedCasualty ==null)  System.out.println( Mybody.Id + " ++++++++++++++++++++++++ second triage +++++++++++++++error"  );

		if (nominatedCasualty !=null)  nominatedCasualty.newaddetoCCS=false;
		return  nominatedCasualty ;

	}

	//----------------------------------------------------------------------------------------------------
	public boolean ThereisTrigedCasualtyinCCS()
	{	
		boolean  Result=false;
		for (Casualty ca: Mybody.AssignedTA.casualtiesinTA ) 
			if (  ca.Status==CasualtyStatus.Triaged && ca.Triage_tage !=5   && ca.IsAssignedToResponder== false )
			{
				if ( ( ca.LastTime_Triage + InputFile.TriageEvery ) <=  Mybody.CurrentTick    )
				{Result=true;break;}
			}
		return Result;
	}

	//----------------------------------------------------------------------------------------------------
	public Casualty NearstTrigedCasualtyinCCS()
	{	

		//Smallest distance
		Casualty nominatedCasualty=null;
		double smallest_dis=999999 ; ; 

		for (Casualty ca: Mybody.AssignedTA.casualtiesinTA ) 
			if (  ca.Status==CasualtyStatus.Triaged &&  ca.Triage_tage!=5   && ca.IsAssignedToResponder== false )
			{
				if ( ( ca.LastTime_Triage + InputFile.TriageEvery ) <=  Mybody.CurrentTick  )
				{
					double dis = BuildStaticFuction.DistanceC(Mybody.geography, Mybody.Return_CurrentLocation(),ca.getCurrentLocation());

					if (dis<smallest_dis)
					{ smallest_dis= dis; nominatedCasualty= ca ; }
				}
			}


		return  nominatedCasualty ;

	}
	//______________________________________________________________________________________________________________________________________________________________
	//##############################################################################################################################################################
	//                                                        Behavior
	//##############################################################################################################################################################
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	// Paramedic - General 
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	public void ParamedicBehavior()	{	

		//********************* for Get Activity
		if (  Mybody.CommTrigger==RespondersTriggers.AssigendRolebyAIC )
		{
			Mybody.Action=RespondersActions.Noaction;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Comunication; // RoleAssignment;
			Mybody.CommTrigger=null;
		}		
		else if ( IgetNewActivity && ( Mybody.CommTrigger==RespondersTriggers.GetActivitySceneResponse ||Mybody.CommTrigger==RespondersTriggers.GetActivityCCSResponse) )													
		{																		
			Mybody.Action=RespondersActions.GetAssignedTask;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Comunication; // RoleAssignment;	
			Mybody.EndofCurrentAction=  InputFile.GetCommand_duration  ;

			IgetNewActivity= false;
		}	
		// ++++++ 2- +++++++
		else if (  Mybody.Action==RespondersActions.GetAssignedTask &&  Mybody.EndActionTrigger== RespondersTriggers.EndingAction) 												
		{						
			Mybody.CurrentSender.Acknowledg(Mybody) ;	

			if ( Mybody.CommTrigger==RespondersTriggers.GetActivitySceneResponse) Mybody.CurrentAssignedMajorActivity_amb=Ambulance_ActivityType.SceneResponse ;	
			if ( Mybody.CommTrigger==RespondersTriggers.GetActivityCCSResponse) Mybody.CurrentAssignedMajorActivity_amb=Ambulance_ActivityType.CCSResponse ;

			//Mybody.CommTrigger=null;
			Mybody.EndActionTrigger=null ;
			//System.out.println(Mybody.Id +" ..... " +Mybody.Role+ "      " + Mybody.CurrentAssignedMajorActivity);
		}
		//************************************1*************************************	
		// ** Scene Operation ***
		// Decentralized			
		if (( Mybody.assignedIncident.TaskApproach_IN ==TaskAllocationApproach.Decentralized_Autonomous ||  Mybody.assignedIncident.TaskApproach_IN ==TaskAllocationApproach.Decentralized_Mutualadjustment)
				&& Mybody.CurrentAssignedMajorActivity_amb==Ambulance_ActivityType.SceneResponse )		
		{ 	

			//=====================================================RescueTreatment
			if (   Mybody.CommTrigger==RespondersTriggers.GetCommandPre_RescueTreatment  ) 	
			{
				//System.out.println(Mybody.Id + " get GetCommandPre_RescueTreatment  xxxxxxx "  + TrappedCasualty.ID +"  "+  Mybody.Action );

				if (Mybody.OnhandCasualty==null &&( Mybody.Action==RespondersActions.Noaction || Mybody.Action==RespondersActions.SearchCasualty  || Mybody.Action==RespondersActions.GoToCasualty ))
				{
					Mybody.CurrentAssignedActivity=Ambulance_TaskType.Pre_RescueTreatment ;

					System.out.println(Mybody.Id + " get GetCommandPre_RescueTreatment      yes  for  "  + TrappedCasualty.ID );

					Mybody.SensingeEnvironmentTrigger=null ;
					if ( Mybody.TargetCasualty !=null  &&  Mybody.CasualtySeen_list.contains(TrappedCasualty)  )
						Mybody.CasualtySeen_list.remove(TrappedCasualty);
				}
				else 
				{
					Mybody.CurrentSender.Acknowledg(Mybody , false ) ;
					Mybody.CommTrigger=null;

					System.out.println(Mybody.Id + " get GetCommandPre_RescueTreatment  NO  for "  + TrappedCasualty.ID +"  "+  Mybody.Action );
				}
			}
			//=====================================================RescueTreatment

			// 1- initial response
			if(   Mybody.CommTrigger==RespondersTriggers.GetActivitySceneResponse   )			
			{
				Mybody.CurrentAssignedActivity=Ambulance_TaskType.GoTolocation;
			}
			// 2- field triage
			else if ( Mybody.InterpretedTrigger== RespondersTriggers.FirstTimeonLocation && Mybody.AssignedSector.installed  && SafetyBriefandSectorEstablished )	 
			{
				Mybody.CurrentAssignedActivity=Ambulance_TaskType.FieldTriage ;
			}
			// 3-No Action  wait until installed
			else if (Mybody.InterpretedTrigger== RespondersTriggers.FirstTimeonLocation  && ! Mybody.AssignedSector.installed && SafetyBriefandSectorEstablished)
			{
				Mybody.CurrentAssignedActivity=Ambulance_TaskType.None;
				Mybody.Action=RespondersActions.Noaction;
				Mybody.BehaviourType1=RespondersBehaviourTypes1.WaitingDuring;
			}

			else if ( Mybody.CurrentAssignedActivity==Ambulance_TaskType.Pre_RescueTreatment && Mybody.InterpretedTrigger== RespondersTriggers.DoneTask  && ! Ialready_Arrived_WalkedinAllScene )	 //fr
			{
				Mybody.CurrentAssignedActivity=Ambulance_TaskType.FieldTriage ;
								
			}			
			// 3-No Action 
			else if ( Mybody.InterpretedTrigger == RespondersTriggers.NoMorecasualty || (   Mybody.CurrentAssignedActivity==Ambulance_TaskType.Pre_RescueTreatment && Mybody.InterpretedTrigger== RespondersTriggers.DoneTask  &&  Ialready_Arrived_WalkedinAllScene  ) ) 
			{	
				Mybody.CurrentAssignedActivity=Ambulance_TaskType.None;
				Mybody.Action=RespondersActions.Noaction;
				Mybody.BehaviourType1=RespondersBehaviourTypes1.WaitingDuring;
				Mybody.InterpretedTrigger=null;
				if ( Mybody.SensingeEnvironmentTrigger != null )Mybody.SensingeEnvironmentTrigger=null;
			}
			// 4- GoBackToControlArea
			else if ( Mybody.CommTrigger== RespondersTriggers.ENDSceneOprations  )  /// Confirmation from bronze commander
			{		
				Mybody.CurrentAssignedMajorActivity_amb=null;
				Mybody.CurrentAssignedActivity=Ambulance_TaskType.GoBackToControlArea ;
				Mybody.InterpretedTrigger= RespondersTriggers.DoneActivity ;
				Mybody.CommTrigger=null;
				Mybody.SensingeEnvironmentTrigger=null;
				Mybody.Bronzecommander_amb=null;

				//System.out.println("  ----------"+Mybody.Id + " I will  GoBackToControlArea  .."  );
			}	

		}
		// Centralized
		else if (Mybody.assignedIncident.TaskApproach_IN == TaskAllocationApproach.CentralizedDirectSupervision   && Mybody.CurrentAssignedMajorActivity_amb==Ambulance_ActivityType.SceneResponse )	
		{
			// 1-initial response
			if(  Mybody.CommTrigger==RespondersTriggers.GetActivitySceneResponse )		//come back		 GetActivitySceneResponse
			{
				Mybody.CurrentAssignedActivity=Ambulance_TaskType.GoTolocation;
			}
			// 2-No action
			else if ( Mybody.InterpretedTrigger== RespondersTriggers.FirstTimeonLocation ||  Mybody.InterpretedTrigger== RespondersTriggers.DoneTask )  
			{
				Mybody.CurrentAssignedActivity=Ambulance_TaskType.None;
				Mybody.Action=RespondersActions.Noaction;
				Mybody.BehaviourType1=RespondersBehaviourTypes1.WaitingDuring;

				Mybody.InterpretedTrigger=null;

				//System.out.println("  "+Mybody.Id + " No action  .."  );
			}		
			// 3-filed Triage 
			else if ( Mybody.CommTrigger==RespondersTriggers.GetCommandTriage  ) 	
			{
				Mybody.CurrentAssignedActivity=Ambulance_TaskType.FieldTriage;
			}
			else if ( Mybody.CommTrigger==RespondersTriggers.GetCommandPre_RescueTreatment ) 	
			{
				Mybody.CurrentAssignedActivity=Ambulance_TaskType.Pre_RescueTreatment ;
			}
			// 4- GoBackToControlArea
			else if ( Mybody.CommTrigger== RespondersTriggers.ENDSceneOprations  ) 
			{		
				Mybody.CurrentAssignedMajorActivity_amb=null;
				Mybody.CurrentAssignedActivity=Ambulance_TaskType.GoBackToControlArea;
				Mybody.InterpretedTrigger= RespondersTriggers.DoneActivity ;
				Mybody.CommTrigger=null;

			}	

			//==== Move with commander ======================			
			if ( (( Mybody.CurrentAssignedActivity==Ambulance_TaskType.GoTolocation && Mybody.Action==RespondersActions.NotifyArrival) || Mybody.CurrentAssignedActivity==Ambulance_TaskType.None )  &&  ( Mybody.AssignedSector.SCcommander.Action==RespondersActions.SearchCasualty  )  )
			{
				double dis1= BuildStaticFuction.DistanceC( Mybody.geography, Mybody.Return_CurrentLocation(), Mybody.AssignedSector.SCcommander.Return_CurrentLocation());

				if (  dis1>3  )
				{
					Mybody.Assign_DestinationResponder(Mybody.AssignedSector.SCcommander); 
					Mybody.Walk();
				}

			}
			//==== Move=====================================
		}

		//***********************************2**************************************		
		// ** CCS  ***
		// 1- initial response
		if (  Mybody.CurrentAssignedMajorActivity_amb==Ambulance_ActivityType.CCSResponse )
		{

			if(   Mybody.CommTrigger==RespondersTriggers.GetActivityCCSResponse || Mybody.InterpretedTrigger== RespondersTriggers.IamNOTinLocation) 		
			{
				Mybody.CurrentAssignedActivity=Ambulance_TaskType.GoTolocation;	

			}
			// 2-No action
			else if (  Mybody.InterpretedTrigger== RespondersTriggers.FirstTimeonLocation || Mybody.InterpretedTrigger == RespondersTriggers.ComeBack || 
					Mybody.InterpretedTrigger == RespondersTriggers.DoneTask || Mybody.InterpretedTrigger== RespondersTriggers.CasualtyDead ||
					Mybody.InterpretedTrigger == RespondersTriggers.DoneActivity || Mybody.CommTrigger== RespondersTriggers.ENDCCSOprations )
			{

				Mybody.CurrentAssignedActivity=Ambulance_TaskType.None;
				Mybody.Action=RespondersActions.Noaction;
				Mybody.BehaviourType1=RespondersBehaviourTypes1.WaitingDuring ;

				if (Mybody.InterpretedTrigger == RespondersTriggers.DoneTask )					
				{
					//Mybody.BehaviourType1=RespondersBehaviourTypes1.CasualtyTransferDelay ;
					Mybody.BehaviourType1=RespondersBehaviourTypes1.WaitingDuring ;
				}


				if ( Mybody.InterpretedTrigger == RespondersTriggers.DoneActivity || Mybody.CommTrigger== RespondersTriggers.ENDCCSOprations  )/// ?????
					Mybody.BehaviourType1=RespondersBehaviourTypes1.WaitingEnd;

				Mybody.InterpretedTrigger=null;	
				Mybody.CommTrigger=null;
			}	
			// 3-Setup
			else if (   Mybody.CommTrigger==RespondersTriggers.GetcommandSetupTacticalAreas 	) 
			{
				Mybody.CurrentAssignedActivity=Ambulance_TaskType.SetupTacticalAreas  ;
			}
			// 4-Second Triage 
			else if ( Mybody.CommTrigger==RespondersTriggers.GetCommandTriage  ) 	
			{
				Mybody.CurrentAssignedActivity=Ambulance_TaskType.SecondryTriage;
			}
			// 5-Treatment
			else if (Mybody.CommTrigger==RespondersTriggers.GetCommandTreatment	) 
			{
				Mybody.CurrentAssignedActivity=Ambulance_TaskType.Treatment;
			}
			// 5-RescueTreatment
			else if ( Mybody.CommTrigger==RespondersTriggers.GetCommandPre_RescueTreatment ) 	
			{
				Mybody.CurrentAssignedActivity=Ambulance_TaskType.Pre_RescueTreatment ;
			}
			// 6-Transfer
			else if (Mybody.CommTrigger==RespondersTriggers.GetCommandTransferToVehicle	) 
			{
				Mybody.CurrentAssignedActivity=Ambulance_TaskType.TransferCasualtytoVehicle;
			}
		}
		//************************************Final*************************************
		// Ending
		if ( Mybody.CommTrigger==RespondersTriggers.ENDER    )   
		{
			Mybody.CurrentAssignedActivity=Ambulance_TaskType.None;
			Mybody.CurrentAssignedMajorActivity_amb =Ambulance_ActivityType.None;

			Mybody.PrvRoleinprint1=Mybody.Role ;
			Mybody.Role=Ambulance_ResponderRole.None;
		}
		// Reallocation
		if ( Mybody.CommTrigger== RespondersTriggers.GetCommandReallocation   )   
		{
			//do some thing

			Mybody.CurrentAssignedActivity=Ambulance_TaskType.None;
			Mybody.CurrentAssignedMajorActivity_amb =Ambulance_ActivityType.None;

		}

		//*************************************************************************
		switch(Mybody.CurrentAssignedActivity) {
		case GoTolocation:
			ParamedicBehavior_GoTolocation() ; 	
			break;	
		case GoBackToControlArea :
			Mybody.ParamedicBehavior_GoBackToControlArea() ; 	
			break;
		case SetupTacticalAreas :
			ParamedicBehavior_SetupTacticalAreas();
			break;
		case FieldTriage :
			if (Mybody.assignedIncident.TaskApproach_IN ==TaskAllocationApproach.Decentralized_Autonomous ||  Mybody.assignedIncident.TaskApproach_IN ==TaskAllocationApproach.Decentralized_Mutualadjustment)	
				ParamedicBehavior_SearchandFieldTriage();
			else
				ParamedicBehavior_FieldTriage();
			break;	
		case Pre_RescueTreatment:
			ParamedicBehavior_Pre_RescueTreatment()	;
			break;					
		case SecondryTriage :
			ParamedicBehavior_SecondTriage();
			break;			
		case Treatment :			
			ParamedicBehavior_Treatment();
			break;	
		case TransferCasualtytoVehicle :
			ParamedicBehavior_TransferCasualtytoVehicle();
			break;		
		case None:
			;
			break;}

		//if ( Mybody.Action==RespondersActions.InformNomorecasualty  )
		//System.out.println(Mybody.Id  +  "  Action:" + Mybody.Action + " com:" + Mybody.CommTrigger + "   Nt:" + Mybody.InterpretedTrigger+ " en:" + Mybody.SensingeEnvironmentTrigger + " end:" + Mybody.EndActionTrigger +  Mybody.Acknowledged + "     " +  Mybody.Sending +"    "+ Mybody.CurrentMessage  );


	}// end ParamedicBehavior

	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	// Paramedic- Go to location 
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^	
	private void ParamedicBehavior_GoTolocation()	
	{
		// ++++++ 1- +++++++
		if(  Mybody.CommTrigger==RespondersTriggers.GetActivitySceneResponse 
				|| Mybody.CommTrigger==RespondersTriggers.GetActivityCCSResponse || Mybody.InterpretedTrigger== RespondersTriggers.IamNOTinLocation  )
		{	

			// 1
			if ( Mybody.CommTrigger==RespondersTriggers.GetActivitySceneResponse )  
			{
				Mybody.Assign_DestinationCordon(Mybody.AssignedSector.centerofsectorPoint);  
				Mybody.Bronzecommander_amb = Mybody.AssignedSector.SCcommander ;  

				Mybody.Action=RespondersActions.GoToSectorScene;
				Mybody.BehaviourType1=RespondersBehaviourTypes1.Movementonincidentsite ;  

				Mybody.CommTrigger=null; IamNewResponder=true; 
			}
			// 2
			else if ( Mybody.CommTrigger==RespondersTriggers.GetActivityCCSResponse)
			{
				Mybody.Assign_DestinationCordon(Mybody.AssignedTA.Location); 
				Mybody.Bronzecommander_amb =  (Responder_Ambulance) Mybody.AssignedTA.Bronzecommander ;  // or  Mybody.assignedIncident.RC

				Mybody.Action=RespondersActions.GoToCCS;
				Mybody.BehaviourType1=RespondersBehaviourTypes1.Movementonincidentsite ;  

				Mybody.CommTrigger=null; IamNewResponder=true; 
			}
			// 3
			else if (Mybody.InterpretedTrigger== RespondersTriggers.IamNOTinLocation )
			{				
				if (  Mybody.CurrentAssignedMajorActivity_amb==Ambulance_ActivityType.CCSResponse) 
				{
					Mybody.Assign_DestinationCordon(Mybody.AssignedTA.Location); Mybody.Bronzecommander_amb = (Responder_Ambulance) Mybody.AssignedTA.Bronzecommander ; // Mybody.assignedIncident.RCO; 

					Mybody.Action=RespondersActions.GoToCCS;
					Mybody.BehaviourType1=RespondersBehaviourTypes1.Movementonincidentsite ;  
					Mybody.InterpretedTrigger=null ; IamNewResponder=false;
					//System.out.println(Mybody.Id + " Go location ====================================================================" +Bronzecommander.Role );
				}
			}

			Mybody.SensingeEnvironmentTrigger=null ;

		}
		// ++++++ 3- +++++++
		else if (Mybody.Action==RespondersActions.GoToSectorScene && Mybody.SensingeEnvironmentTrigger==null) 
		{						
			if ( Mybody.AssignedSector.AmIinSector(Mybody))
				Mybody.SensingeEnvironmentTrigger= RespondersTriggers.ArrivedTargetObject;

			Mybody.Walk();
		}
		// ++++++ 3- +++++++
		else if ( ( Mybody.Action==RespondersActions.GoToTacticalArea||  Mybody.Action==RespondersActions.GoToCCS ) && Mybody.SensingeEnvironmentTrigger==null) 
		{						
			Mybody.Walk();
		}
		// ++++++ 4- +++++++
		else if ( ( Mybody.Action==RespondersActions.GoToTacticalArea||  Mybody.Action==RespondersActions.GoToCCS || Mybody.Action==RespondersActions.GoToSectorScene )&& Mybody.SensingeEnvironmentTrigger== RespondersTriggers.ArrivedTargetObject) 
		{ 			
			//Send message to Bronze commander			
			if ( IamNewResponder )
			{
				Mybody.CurrentMessage  = new  ACL_Message( ACLPerformative.InformNewResponderArrival, Mybody, Mybody.Bronzecommander_amb ,null  ,  Mybody.CurrentTick ,Mybody.assignedIncident.ComMechanism_level_BtoRes ,1,TypeMesg.Inernal ) ;
				Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
				//System.out.println(Mybody.Id + " send arrived location" + Bronzecommander.Id);Mybody.ColorCode=8;
			}

			else
			{ Mybody.CurrentMessage = new  ACL_Message( ACLPerformative.InformlocationArrival, Mybody, Mybody.Bronzecommander_amb ,null  ,  Mybody.CurrentTick,Mybody.assignedIncident.ComMechanism_level_BtoRes ,1,TypeMesg.Inernal  ) ;
			Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);
			}			

			//==================
			Mybody.Action=RespondersActions.NotifyArrival;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Comunication ;                   //TaskPlanning;
			Mybody.SensingeEnvironmentTrigger=null;			
		}	
		// ++++++ 5- +++++++
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
			//System.out.println(Mybody.Id + " arrived location" );

		}

	}	

	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	// Policeman- Setup TacticalAreas 
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^	
	private void ParamedicBehavior_SetupTacticalAreas()	
	{
		// ++++++ 1- +++++++
		if ( Mybody.CommTrigger==RespondersTriggers.GetcommandSetupTacticalAreas  ) 													
		{																		
			Mybody.Action=RespondersActions.GetcommandSetupTacticalAreas ;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Comunication; //TaskPlanning;	
			Mybody.EndofCurrentAction=  InputFile.GetCommand_duration  ;
			Mybody.CommTrigger=null;	
		}	
		// ++++++ 2- +++++++
		else if (  Mybody.Action==RespondersActions.GetcommandSetupTacticalAreas  &&  Mybody.EndActionTrigger== RespondersTriggers.EndingAction) 												
		{		
			Mybody.CurrentSender.Acknowledg(Mybody) ;
			//System.out.println("  "+"CCS: "+  Mybody.Id  + " Acknowledg " + Mybody.CurrentSender.Id +"  "+ Mybody.CurrentTick );

			if ( Mybody.CurrentAssignedMajorActivity_amb ==Ambulance_ActivityType.CCSResponse )
			{ Mybody.Action=RespondersActions.SetupCCS ; Mybody.AssignedTA.IworkinSetup(Mybody);  Mybody.Bronzecommander_amb = (Responder_Ambulance) Mybody.AssignedTA.Bronzecommander ;}			
			else 
			{ Mybody.Action=RespondersActions.SetuploadingArea ; Mybody.AssignedTA.IworkinSetup(Mybody) ; Mybody.Bronzecommander_amb =  (Responder_Ambulance) Mybody.AssignedTA.Bronzecommander  ;}


			Mybody.BehaviourType1=RespondersBehaviourTypes1.TaskExecution_SetupTA ;							
			Mybody.EndActionTrigger=null;
		}
		//------------------------------------------LoadingArea ----------------------------------------
		// ++++++ 3- +++++++   
		else if ( Mybody.Action==RespondersActions.SetuploadingArea   && 	 Mybody.AssignedTA.installed ) 													
		{							
			InformResult(ACLPerformative.InfromResultSetupLoadingArea ,Mybody.Bronzecommander_amb );
			//System.out.println(Mybody.CurrentTick + "  " +  Mybody.Id + " ");
			Mybody.Action=RespondersActions.InfromResultSetupLoadingArea ;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Comunication ;   
			Mybody.SensingeEnvironmentTrigger= null;

		}
		// ++++++ 4- +++++++
		else if (Mybody.Action==RespondersActions.InfromResultSetupLoadingArea && Mybody.Acknowledged) 
		{
			Mybody.InterpretedTrigger= RespondersTriggers.DoneTask;
			Mybody.Acknowledged=false;Mybody.Sending=false; 

		}

		//------------------------------------------CCS  ----------------------------------------
		// ++++++ 5- +++++++   
		else if ( Mybody.Action==RespondersActions.SetupCCS && 	 Mybody.AssignedTA.installed ) 													
		{							
			InformResult(ACLPerformative.InfromResultSetupCCS  ,Mybody.Bronzecommander_amb );
			Mybody.Action=RespondersActions.InfromResultSetupCCS;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Comunication ;  
			Mybody.SensingeEnvironmentTrigger= null;

			//System.out.println("  "+"CCS: "+  Mybody.Id  + " InfromResultSetupCCS  " + Mybody.CurrentSender.Id +"  "+ Mybody.CurrentTick );

		}
		// ++++++ 6- +++++++
		else if (Mybody.Action==RespondersActions.InfromResultSetupCCS && Mybody.Acknowledged) 
		{
			Mybody.InterpretedTrigger= RespondersTriggers.DoneTask;
			Mybody.Acknowledged=false;Mybody.Sending=false; 

		}
	}

	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	// Paramedic- Field Triage (Centralized )
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^	
	private void ParamedicBehavior_FieldTriage()	
	{
		// ++++++ 1- +++++++
		if ( Mybody.CommTrigger==RespondersTriggers.GetCommandTriage  ) 													
		{																		
			Mybody.Action=RespondersActions.GetcammandTriage;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Comunication; //TaskPlanning;	
			Mybody.EndofCurrentAction=  InputFile.GetCommand_duration  ;
			Mybody.CommTrigger=null;

			//System.out.println(Mybody.Id + " GetCommandTriage  "  + Mybody.TargetCasualty.ID );
		}	
		// ++++++ 2- +++++++
		else if (  Mybody.Action==RespondersActions.GetcammandTriage &&  Mybody.EndActionTrigger== RespondersTriggers.EndingAction) 												
		{		
			Mybody.CurrentSender.Acknowledg(Mybody) ;

			Mybody.Assign_DestinationCasualty(Mybody.TargetCasualty) ;					
			Mybody.Action=RespondersActions.GoToCasualty;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Movementonincidentsite; //TaskPlanning?
			Mybody.EndActionTrigger=null;

		}
		// ++++++ 3- +++++++
		else if (Mybody.Action==RespondersActions.GoToCasualty && Mybody.SensingeEnvironmentTrigger==null)
		{			
			Mybody.Walk();
		}	
		// ++++++ 4- +++++++
		else if (Mybody.Action==RespondersActions.GoToCasualty  && Mybody.SensingeEnvironmentTrigger==RespondersTriggers.ArrivedCasualty)// Action
		{				
			OnFieldTriageActionS();
			Mybody.OnhandCasualty.IsAssignedToResponder = true;

			Mybody.Action=RespondersActions.Triage;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.TaskExecution_fieldtriage ;
			Mybody.EndofCurrentAction=  InputFile. Triage_duration  ; 				
			Mybody.SensingeEnvironmentTrigger=null;
		}	
		// ++++++ 5- +++++++
		else if ( Mybody.Action==RespondersActions.Triage && 	 Mybody.EndActionTrigger== RespondersTriggers.EndingAction) 													
		{				
			OnFieldTriageActionE();
			ReportResult(Ambulance_TaskType.FieldTriage) ;

			Mybody.Action=RespondersActions.ReportResult;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.TaskExecution_fieldtriage ;
			Mybody.EndActionTrigger= null;
		}
		// ++++++ 7- +++++++
		else if (Mybody.Action==RespondersActions.ReportResult &&  Mybody.EndActionTrigger== RespondersTriggers.EndingAction) 
		{
			InformResult(ACLPerformative.InformResultFieldTriage,Mybody.Bronzecommander_amb);
			//System.out.println("  "+  Mybody.Id + " done field  "+ Mybody.OnhandCasualty.ID);

			Mybody.OnhandCasualty.IsAssignedToResponder = false ;  // to used by firefighter

			Mybody.Action=RespondersActions.InfromResultTriage;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Comunication ; 
			Mybody.EndActionTrigger= null; 
		}
		// ++++++ 7- +++++++
		else if (Mybody.Action==RespondersActions.InfromResultTriage && Mybody.Acknowledged) 
		{

			Mybody.OnhandCasualty=null;
			Mybody.InterpretedTrigger= RespondersTriggers.DoneTask;
			Mybody.Acknowledged=false;Mybody.Sending=false; 
		}

	}

	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	// Paramedic-  Field Triage  ( Decentralized )
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	private void ParamedicBehavior_SearchandFieldTriage()
	{
		// ++++++ 1- +++++++
		if (Mybody.InterpretedTrigger== RespondersTriggers.FirstTimeonLocation   )  
		{
			if (Mybody.assignedIncident.TaskMechanism_IN==TaskAllocationMechanism.NoEnvironmentsectorization)
			{
				//Mybody.Reset_DirectioninSearach( ); //its memory
				//if (Mybody.Randomizer.nextInt(2)==0 ) Mybody.ClockDirection= 1; else Mybody.ClockDirection= -1; 
				//Mybody.MyDirectiontowalk=Mybody.assignedIncident.IdentifyNearest_small(Mybody.Return_CurrentLocation());
				//Mybody.NextRotateDirectionSearch_smallRoute(Mybody.ClockDirection);
				//Mybody.walkingstrategy=RandomWalking_StrategyType.FourDirections_small;/

				//Mybody.RandomDirectionSerach(); //Mybody.MyDirectiontowalk	
				//while (Mybody.MyDirectiontowalk== Mybody.AssignedSector.Startpoint		)
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
//
//				Mybody.walkingstrategy=RandomWalking_StrategyType.OneDirection_sector;
			}


			Mybody.CasualtySeen_list.clear();				
			Mybody.StopDistnation=Mybody.AssignedSector.centerofsectorPoint; 			
			Mybody.ArrivedSTOP=false;
			Mybody.ExpandFieldofVision=false;	

			Mybody.Setup_ConsiderSeverityofcasualtyinSearach(false ); // No Severity
			Mybody.Assign_DestinationLocation_Serach();
			Mybody.EndofCurrentAction=  InputFile.WalkingBeforSearch_duration ; 
			Mybody.RandomllySearachCasualty= false;

			Mybody.Action=RespondersActions.SearchCasualty;	
			Mybody.BehaviourType1=RespondersBehaviourTypes1.TaskPlanning;
			Mybody.InterpretedTrigger= null;

		}
		// ++++++ 1- ++++++

		else if   (( Mybody.Action==RespondersActions.GoToCasualty && Mybody.SensingeEnvironmentTrigger== RespondersTriggers.CasualtyHasanotherParamedic)||
				(Mybody.InterpretedTrigger== RespondersTriggers.DoneTask  )  //FROM TRAPPED
				)  	 
		{	
			Mybody.Action=RespondersActions.SearchCasualty;	
			Mybody.BehaviourType1=RespondersBehaviourTypes1.TaskPlanning;
			Mybody.Assign_DestinationLocation_Serach();
			Mybody.InterpretedTrigger= null;

			//=============
			if( Mybody.InterpretedTrigger== RespondersTriggers.FirstTimeonLocation) Mybody.InterpretedTrigger=null ;
			if ( Mybody.SensingeEnvironmentTrigger== RespondersTriggers.CasualtyHasanotherParamedic)  Mybody.SensingeEnvironmentTrigger=null;
			//if (  Mybody.Acknowledged)  Mybody.Acknowledged=false;Mybody.Sending=false; 

		}
		// ++++++ 2- +++++++
		else if (Mybody.Action==RespondersActions.SearchCasualty && Mybody.SensingeEnvironmentTrigger==null)
		{			

			if ( Mybody.EndActionTrigger== RespondersTriggers.EndingAction  )
			{Mybody.RandomllySearachCasualty= true; Mybody.MyDirectiontowalk= Mybody.AssignedSector.Limitpoint;Mybody.EndActionTrigger=null;	}//for first time now start search

			Mybody.Walk();
		}
		// ++++++ 3- +++++++
		else if (Mybody.Action==RespondersActions.SearchCasualty && Mybody.SensingeEnvironmentTrigger==RespondersTriggers.SensedCasualty)
		{			
			Mybody.Assign_DestinationCasualty(Mybody.TargetCasualty) ;	

			Mybody.Action=RespondersActions.GoToCasualty;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Movementonincidentsite; //TaskPlanning?
			Mybody.SensingeEnvironmentTrigger=null;
		}
		// ++++++ 4- +++++++
		else if (Mybody.Action==RespondersActions.GoToCasualty && Mybody.SensingeEnvironmentTrigger==null)
		{			
			if ( Mybody.TargetCasualty.IsAssignedToResponder==true  )
				Mybody.SensingeEnvironmentTrigger= RespondersTriggers.CasualtyHasanotherParamedic; 
			else
				Mybody.Walk();
		}	
		// ++++++ 5- +++++++		
		else if (Mybody.Action==RespondersActions.GoToCasualty  && Mybody.SensingeEnvironmentTrigger==RespondersTriggers.ArrivedCasualty)
		{	
			if ( Mybody.TargetCasualty.IsAssignedToResponder==true  )
				Mybody.SensingeEnvironmentTrigger= RespondersTriggers.CasualtyHasanotherParamedic; 
			else
			{
				Mybody.OnhandCasualty.IsAssignedToResponder = true;
				OnFieldTriageActionS();

				Mybody.Action=RespondersActions.Triage;
				Mybody.BehaviourType1=RespondersBehaviourTypes1.TaskExecution_fieldtriage ;
				Mybody.EndofCurrentAction=  InputFile. Triage_duration  ; 
				Mybody.SensingeEnvironmentTrigger=null;
			}
		}	
		// ++++++ 6- +++++++
		else if(  Mybody.Action==RespondersActions.Triage && Mybody.EndActionTrigger== RespondersTriggers.EndingAction  )
		{
			OnFieldTriageActionE();			
			ReportResult(Ambulance_TaskType.FieldTriage) ;

			Mybody.Action=RespondersActions.ReportResult;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.TaskExecution_fieldtriage ;

			Mybody.EndActionTrigger= null;
		}
		// ++++++ 7- +++++++
		else if (Mybody.Action==RespondersActions.ReportResult &&  Mybody.EndActionTrigger== RespondersTriggers.EndingAction) 
		{
			InformResult(ACLPerformative.InformResultFieldTriage ,Mybody.Bronzecommander_amb);
			Mybody.OnhandCasualty.IsAssignedToResponder = false ;

			Mybody.Action=RespondersActions.InfromResultTriage;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Comunication ;  
			Mybody.EndActionTrigger= null;
		}	

		// ++++++ 8- +++++++
		else if(  ( (Mybody.Action==RespondersActions.InfromResultTriage && Mybody.Acknowledged) 	  && Mybody.assignedIncident.TaskApproach_IN ==TaskAllocationApproach.Decentralized_Autonomous )||
				( (Mybody.Action==RespondersActions.InfromResultTriage && Mybody.Acknowledged) 	 && Mybody.OnhandCasualty.Triage_tage!=3  && Mybody.assignedIncident.TaskApproach_IN ==TaskAllocationApproach.Decentralized_Mutualadjustment ) ) 	 													
		{		

			Mybody.OnhandCasualty=null;
			Mybody.ResponderSeen_list.clear(); 
			Mybody.Action=RespondersActions.SearchCasualty;	
			Mybody.BehaviourType1=RespondersBehaviourTypes1.TaskPlanning;
			Mybody.Assign_DestinationLocation_Serach();
			Mybody.Acknowledged=false;Mybody.Sending=false; 
		}
		//================================================================================================================		
		// ++++++ 9- +++++++
		else if  ( (Mybody.Action==RespondersActions.InfromResultTriage && Mybody.Acknowledged)  && Mybody.OnhandCasualty.Triage_tage==3  &&  Mybody.assignedIncident.TaskApproach_IN ==TaskAllocationApproach.Decentralized_Mutualadjustment   ) 	 
		{						
			Mybody.Action=RespondersActions.SearchFireFighter;	
			Mybody.BehaviourType1=RespondersBehaviourTypes1.TaskPlanning;			
			counter=60 ; // Search for only 60 second 
			Mybody.Acknowledged=false;Mybody.Sending=false; 
			//System.out.println("  "+  Mybody.Id + " witing with   "+ Mybody.OnhandCasualty.ID);
		}	
		// ++++++ 10- +++++++
		else if (Mybody.Action==RespondersActions.SearchFireFighter && counter!=0 &&  Mybody.SensingeEnvironmentTrigger==null)
		{			
			counter--;
			Mybody.Search_RandomlyResponders();  //not move only search from its current location
		}
		// ++++++ 11- +++++++
		else if (Mybody.Action==RespondersActions.SearchFireFighter && counter==0 &&  Mybody.SensingeEnvironmentTrigger==null)
		{			
			//System.out.println("  "+  Mybody.Id + " No body I will go   "+ Mybody.OnhandCasualty.ID);
			Mybody.ResponderSeen_list.clear(); 
			Mybody.Action=RespondersActions.SearchCasualty;	
			Mybody.BehaviourType1=RespondersBehaviourTypes1.TaskPlanning;
			Mybody.Assign_DestinationLocation_Serach();
			Mybody.Acknowledged=false;Mybody.Sending=false; 

		}
		// ++++++ 12- +++++++
		else if (Mybody.Action==RespondersActions.SearchFireFighter && Mybody.SensingeEnvironmentTrigger==RespondersTriggers.SensedFireFighterResponder)
		{			
			Command CMD1 =new Command();
			CMD1.FireCommand("" ,Fire_TaskType.CarryCasualtytoCCS ,Mybody.OnhandCasualty );
			Mybody.CurrentMessage = new  ACL_Message( ACLPerformative.Requste , Mybody, Mybody.TargetResponder ,CMD1, Mybody.CurrentTick , CommunicationMechanism.FaceToFace ,1,TypeMesg.Inernal) ;
			Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);

			Mybody.Action=RespondersActions.RequestFireFighter ;  //  or by SC commander !!
			Mybody.BehaviourType1=RespondersBehaviourTypes1.TaskPlanning ;
			Mybody.SensingeEnvironmentTrigger=null;

			System.out.println("  ---------------------"+  Mybody.Id + " send to    "+ Mybody.TargetResponder.Id);

		}
		// ++++++ 13- +++++++
		else if (Mybody.Action==RespondersActions.RequestFireFighter &&  Mybody.Acknowledged && Mybody.AcceptRequest  )
		{			
			System.out.println("  ---------------------"+  Mybody.Id + " .AcceptRequest  "+ Mybody.TargetResponder.Id);
			Mybody.ResponderSeen_list.clear();

			Mybody.Action=RespondersActions.SearchCasualty;	
			Mybody.BehaviourType1=RespondersBehaviourTypes1.TaskPlanning;
			Mybody.Assign_DestinationLocation_Serach();
			Mybody.Acknowledged=false;Mybody.Sending=false; 

		}	
		// ++++++ 14- +++++++
		else if (Mybody.Action==RespondersActions.RequestFireFighter &&  Mybody.Acknowledged && !Mybody.AcceptRequest  )
		{			
			System.out.println("  ---------------------"+  Mybody.Id + " reject   "+ Mybody.TargetResponder.Id);
			Mybody.Action=RespondersActions.SearchFireFighter;	
			Mybody.BehaviourType1=RespondersBehaviourTypes1.TaskPlanning;			
			Mybody.Acknowledged=false;Mybody.Sending=false; 

		}	
		//================================================================================================================

		// ++++++ 16- +++++++
		else if (( Mybody.Action==RespondersActions.InformNomorecasualty && Mybody.Acknowledged)  || ( ( Mybody.Action==RespondersActions.SearchCasualty ||Mybody.Action==RespondersActions.Noaction ) && Ialready_Arrived_WalkedinAllScene==true) )  //affter trapped
		{
			Ialready_Arrived_WalkedinAllScene=true;	
			if ( Mybody.SensingeEnvironmentTrigger != null )Mybody.SensingeEnvironmentTrigger=null;
			Mybody.InterpretedTrigger= RespondersTriggers.NoMorecasualty;
			Mybody.Acknowledged=false;Mybody.Sending=false; 

			System.out.println( Mybody.Id  +" done" );

			//for(ISRecord     Rec: Mybody.assignedIncident.ISSystem) 
			//System.out.println( Rec.CasualtyinRec.ID +" -------- "  + Rec.AssignedParamdicinRec.Id +"  "  + Rec.Triaged +"  "  +  Rec.Treated + "  "  +  Rec.TransferdV);

		}
		// ++++++ 15- +++++++
		else if (Mybody.Action==RespondersActions.SearchCasualty && ( Mybody.SensingeEnvironmentTrigger== RespondersTriggers.Arrived_WalkedinAllScene || Mybody.SensingeEnvironmentTrigger == RespondersTriggers.Arrived_WalkedalongOneDirection) )
		{ 
			System.out.println( Mybody.Id  +" Arrived_Walkedin /////////////////////////////////" +Mybody.OnhandCasualty + Mybody.WorkingonCasualty);
			
			//inform SC
			Mybody.CurrentMessage  = new  ACL_Message( ACLPerformative.InformNomorecasualtyScene, Mybody, Mybody.assignedIncident.AmSCcommander1 ,null  ,  Mybody.CurrentTick,Mybody.assignedIncident.ComMechanism_level_BtoRes,1,TypeMesg.Inernal ) ;
			Mybody.CurrentMessage_list.add(Mybody.CurrentMessage);	

			Mybody.Action=RespondersActions.InformNomorecasualty;  //  or by SC commander !!
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Comunication ;   
			Mybody.SensingeEnvironmentTrigger=null;

		}
	}

	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	// Paramedic-  Second Triage 
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	private void  ParamedicBehavior_SecondTriage()
	{
		// ++++++ 1- +++++++
		if ( Mybody.CommTrigger==RespondersTriggers.GetCommandTriage)  
		{
			Mybody.Action=RespondersActions.GetcammandTriage;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Comunication; //TaskPlanning;	
			Mybody.EndofCurrentAction=  InputFile.GetCommand_duration  ;
			Mybody.CommTrigger=null;
		}	
		// ++++++ 2- +++++++
		else if (  Mybody.Action==RespondersActions.GetcammandTriage &&  Mybody.EndActionTrigger== RespondersTriggers.EndingAction) 												
		{		
			Mybody.CurrentSender.Acknowledg(Mybody) ;

			Mybody.Action=RespondersActions.SearchCasualty;	
			Mybody.BehaviourType1=RespondersBehaviourTypes1.TaskPlanning;
			Mybody.EndActionTrigger= null;

			System.out.println("  "+"ST: "+  Mybody.Id  + " Iam  second Triage from " + Mybody.CurrentSender.Id);
		}
		//-------------------------------------------------------------------------------------------- (P1)
		// ++++++ 2- +++++++
		else if(  Mybody.Action==RespondersActions.SearchCasualty &&   ThereisNewCasualtyinCCS()  )
		{									
			Mybody.TargetCasualty  =this.NearstNewCasualtyinCCS() ;  

			Mybody.Assign_DestinationCasualty(Mybody.TargetCasualty ) ;			
			Mybody.Action=RespondersActions.GoToCasualty;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Movementonincidentsite; //TaskPlanning?

		}
		//-------------------------------------------------------------------------------------------- (P2)
		// ++++++ 3- +++++++
		else if(  Mybody.Action==RespondersActions.SearchCasualty &&  ! ThereisNewCasualtyinCCS() && ThereisTrigedCasualtyinCCS()  )
		{									
			Mybody.TargetCasualty  =this.NearstTrigedCasualtyinCCS() ;  //herrrrrrrr	

			Mybody.Assign_DestinationCasualty(Mybody.TargetCasualty ) ;				
			Mybody.Action=RespondersActions.GoToCasualty;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Movementonincidentsite; //TaskPlanning?

		}
		// ++++++ 4- +++++++
		else if (Mybody.Action==RespondersActions.GoToCasualty && Mybody.SensingeEnvironmentTrigger==null)
		{			
			if ( Mybody.TargetCasualty.IsAssignedToResponder==true  )
				Mybody.SensingeEnvironmentTrigger= RespondersTriggers.CasualtyHasanotherParamedic; 
			else
				Mybody.Walk();
		}
		// ++++++ 5- +++++++
		else if   ( Mybody.Action==RespondersActions.GoToCasualty && Mybody.SensingeEnvironmentTrigger== RespondersTriggers.CasualtyHasanotherParamedic) 
		{
			Mybody.Action=RespondersActions.SearchCasualty;	
			Mybody.BehaviourType1=RespondersBehaviourTypes1.TaskPlanning;
			Mybody.TargetCasualty=null;
			Mybody.SensingeEnvironmentTrigger=null;
		}
		// ++++++ 6- +++++++		
		else if (Mybody.Action==RespondersActions.GoToCasualty  && Mybody.SensingeEnvironmentTrigger==RespondersTriggers.ArrivedCasualty)
		{	
			if ( Mybody.TargetCasualty.IsAssignedToResponder==true  )
				Mybody.SensingeEnvironmentTrigger= RespondersTriggers.CasualtyHasanotherParamedic; 
			else
			{
				Mybody.OnhandCasualty.IsAssignedToResponder = true;		
				this.NEEDTOupdatetoCCO=false;
				NEEDTOupdatetoCCO=OnCCSTriageActionS();

				Mybody.Action=RespondersActions.Triage;
				Mybody.BehaviourType1=RespondersBehaviourTypes1.TaskExecution_secondtriage ;
				Mybody.EndofCurrentAction=  InputFile. Triage_duration  ; 
				Mybody.SensingeEnvironmentTrigger=null;
			}
		}	
		// ++++++ 7- +++++++
		else if ( Mybody.Action==RespondersActions.Triage && Mybody.EndActionTrigger== RespondersTriggers.EndingAction) 													
		{				
			OnCCSTriageActionE();

			if (   this.NEEDTOupdatetoCCO== true  )
			{
				ReportResult(Ambulance_TaskType.SecondryTriage) ;
				//System.out.println("  "+"ST: "+  Mybody.Id  + " second Triage "+ Mybody.OnhandCasualty.ID);

				Mybody.Action=RespondersActions.ReportResult;
				Mybody.BehaviourType1=RespondersBehaviourTypes1.TaskExecution_secondtriage ;
				Mybody.EndActionTrigger= null;
			}
			else
			{

				Mybody.OnhandCasualty.IsAssignedToResponder = false ;
				Mybody.OnhandCasualty=null;

				Mybody.Action=RespondersActions.SearchCasualty;	
				Mybody.BehaviourType1=RespondersBehaviourTypes1.TaskPlanning;
				Mybody.TargetCasualty=null;
				Mybody.EndActionTrigger= null;
				Mybody.SensingeEnvironmentTrigger=null;
			}		
		}
		// ++++++ 7- +++++++
		else if (Mybody.Action==RespondersActions.ReportResult &&  Mybody.EndActionTrigger== RespondersTriggers.EndingAction) 
		{
			InformResult(ACLPerformative.InformResultSecondTriage ,(Responder_Ambulance) Mybody.AssignedTA.Bronzecommander);

			Mybody.OnhandCasualty.IsAssignedToResponder = false ;
			Mybody.OnhandCasualty=null;

			Mybody.Action=RespondersActions.InfromResultTriage;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Comunication;             
			Mybody.EndActionTrigger= null;
			//onte there is erro aknolge no need for IMS
		}	
		// ++++++ 8- +++++++
		else if (Mybody.Action==RespondersActions.InfromResultTriage && Mybody.Acknowledged) 
		{
			Mybody.Action=RespondersActions.SearchCasualty;	
			Mybody.BehaviourType1=RespondersBehaviourTypes1.TaskPlanning;
			Mybody.Acknowledged=false;Mybody.Sending=false; 
			//System.out.println("  "+"ST: "+  Mybody.Id  + " infrom second Triage ");
		}

		// ++++++ 9- +++++++
		else if (Mybody.Action==RespondersActions.SearchCasualty  && Mybody.CommTrigger== RespondersTriggers.ENDCCSOprations ) 
		{
			Mybody.InterpretedTrigger = RespondersTriggers.DoneActivity ;

			Mybody.CommTrigger=null;
			//System.out.println("  "+"ST: "+  Mybody.Id  + " infrom second Triage ");
		}

	}

	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	// Paramedic- Pre_Treatment (Centralized )
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^	
	private void ParamedicBehavior_Pre_RescueTreatment()	
	{
		// ++++++ 1- +++++++
		if ( Mybody.CommTrigger==RespondersTriggers.GetCommandPre_RescueTreatment  ) 													
		{																		
			Mybody.Action=RespondersActions.GetCommandPre_RescueTreatment  ;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Comunication; // TaskPlanning;	
			Mybody.EndofCurrentAction=  InputFile.GetCommand_duration  ; 	
			Mybody.CommTrigger=null;	
			//System.out.println(Mybody.Id + " get GetCommandPre_RescueTreatment  "  + TrappedCasualty.ID );
		}	
		// ++++++ 2- +++++++
		else if (  Mybody.Action==RespondersActions.GetCommandPre_RescueTreatment   && 	  Mybody.EndActionTrigger== RespondersTriggers.EndingAction) 													
		{		
			if ( Mybody.assignedIncident.TaskApproach_IN ==TaskAllocationApproach.CentralizedDirectSupervision  &&  Mybody.CurrentAssignedMajorActivity_amb==Ambulance_ActivityType.SceneResponse )
				Mybody.CurrentSender.Acknowledg(Mybody) ;
			else if ( Mybody.assignedIncident.TaskApproach_IN ==TaskAllocationApproach.Decentralized_Autonomous  &&  Mybody.CurrentAssignedMajorActivity_amb==Ambulance_ActivityType.SceneResponse )
				Mybody.CurrentSender.Acknowledg(Mybody,true) ;
			else if (  Mybody.CurrentAssignedMajorActivity_amb==Ambulance_ActivityType.CCSResponse )
				Mybody.CurrentSender.Acknowledg(Mybody) ; //CCS

			Mybody.TargetCasualty=TrappedCasualty;
			Mybody.Assign_DestinationCasualty(Mybody.TargetCasualty) ;	

			Mybody.Action=RespondersActions.GoToCasualty;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Movementonincidentsite; //TaskPlanning? 
			Mybody.EndActionTrigger=null;
		}
		// ++++++ 3- +++++++
		else if (Mybody.Action==RespondersActions.GoToCasualty && Mybody.SensingeEnvironmentTrigger==null)// Action
		{			
			Mybody.Walk();
		}	
		// ++++++ 4- +++++++
		else if (Mybody.Action==RespondersActions.GoToCasualty  && Mybody.SensingeEnvironmentTrigger==RespondersTriggers.ArrivedCasualty   )//Trigger Arrived
		{	
			Mybody.OnhandCasualty.IsAssignedToResponder = true ;
			OnPre_TreatmentActionS();

			System.out.println(Mybody.Id + " Per_Treatment for"+ Mybody.OnhandCasualty.ID);

			Mybody.Action=RespondersActions.Treatment;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.TaskExecution_treatment;
			Mybody.EndofCurrentAction= InputFile.Treatment_duration ; 	//Because it is static but it could be changed based on type of severity			
			Mybody.SensingeEnvironmentTrigger=null;
		}
		// ++++++ 6- +++++++
//		else if ( Mybody.Action==RespondersActions.Treatment && 	Mybody.EndofCurrentAction==1) 													
//		{				
//			Mybody.WorkingonCasualty.Status=CasualtyStatus.PreTreatedTrapped ;
//		}
		// ++++++ 6- +++++++
				else if ( Mybody.Action==RespondersActions.Treatment && 	Mybody.EndActionTrigger== RespondersTriggers.EndingAction) 													
				{				
					OnPre_TreatmentActionE();
					Mybody.WorkingonCasualty= null ; 

					ReportResult(Ambulance_TaskType.Pre_RescueTreatment);	
					//System.out.println(Mybody.Id + " Treatment for"+ Mybody.OnhandCasualty.ID);

					Mybody.Action=RespondersActions.ReportResult;
					Mybody.BehaviourType1=RespondersBehaviourTypes1.TaskExecution_treatment ;
					Mybody.EndActionTrigger= null;
				}
		
		// ++++++ 7- +++++++
		else if (Mybody.Action==RespondersActions.ReportResult &&  Mybody.EndActionTrigger== RespondersTriggers.EndingAction) 
		{
			InformResult(ACLPerformative.InformResultPre_RescueTreatment,Mybody.Bronzecommander_amb);		 
			//Mybody.OnhandCasualty.IsAssignedToResponder = false ;  //beacuse it with firefgiter
			Mybody.OnhandCasualty=null;

			Mybody.Action=RespondersActions.InfromResultTreatment;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Comunication;   
			Mybody.EndActionTrigger= null;

		}	
		// ++++++ 8- +++++++
		else if (Mybody.Action==RespondersActions.InfromResultTreatment && Mybody.Acknowledged) //Trigger end of action
		{

			if (   Mybody.CurrentAssignedMajorActivity_amb==Ambulance_ActivityType.SceneResponse )
				Mybody.InterpretedTrigger= RespondersTriggers.DoneTask;			
			else if (  Mybody.CurrentAssignedMajorActivity_amb==Ambulance_ActivityType.CCSResponse )
				Mybody.InterpretedTrigger= RespondersTriggers.IamNOTinLocation;

			Mybody.Action=RespondersActions.Noaction;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.WaitingDuring ;			
			Mybody.Acknowledged=false;Mybody.Sending=false; 
			
			
			System.out.println(Mybody.Id + " Pre_RescueTreatment ");

		}
	}

	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	// Paramedic- Treatment (Centralized )
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^	
	private void ParamedicBehavior_Treatment()	
	{
		// ++++++ 1- +++++++
		if ( Mybody.CommTrigger==RespondersTriggers.GetCommandTreatment  ) 													
		{																		
			Mybody.Action=RespondersActions.GetcammandTreatment ;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Comunication ;    //TaskPlanning;	
			Mybody.EndofCurrentAction=  InputFile.GetCommand_duration  ; 	
			Mybody.CommTrigger=null;	
			//System.out.println(Mybody.Id + " get command Treatment ");
		}	
		// ++++++ 2- +++++++
		else if (  Mybody.Action==RespondersActions.GetcammandTreatment && 	  Mybody.EndActionTrigger== RespondersTriggers.EndingAction) 													
		{		

			Mybody.CurrentSender.Acknowledg(Mybody) ;
			Mybody.Assign_DestinationCasualty(Mybody.TargetCasualty) ;	

			Mybody.Action=RespondersActions.GoToCasualty;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Movementonincidentsite; //TaskPlanning?
			Mybody.EndActionTrigger=null;
		}
		// ++++++ 3- +++++++
		else if (Mybody.Action==RespondersActions.GoToCasualty && Mybody.SensingeEnvironmentTrigger==null)// Action
		{			
			Mybody.Walk();
		}	
		// ++++++ 4- +++++++
		else if (Mybody.Action==RespondersActions.GoToCasualty  && Mybody.SensingeEnvironmentTrigger==RespondersTriggers.ArrivedCasualty  && ! Mybody.OnhandCasualty.IsAssignedToResponder  )//Trigger Arrived
		{	
			Mybody.OnhandCasualty.IsAssignedToResponder = true ;
			OnTreatmentActionS();

			//System.out.println(Mybody.Id + " Treatment for"+ Mybody.OnhandCasualty.ID);

			Mybody.Action=RespondersActions.Treatment;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.TaskExecution_treatment;
			Mybody.EndofCurrentAction= InputFile.Treatment_duration ; 	//Because it is static but it could be changed based on type of severity			
			Mybody.SensingeEnvironmentTrigger=null;
		}
		// ++++++ 6- +++++++
		else if ( Mybody.Action==RespondersActions.Treatment && 	Mybody.EndActionTrigger== RespondersTriggers.EndingAction) 													
		{				
			OnTreatmentActionE();
			ReportResult(Ambulance_TaskType.Treatment);	
			//System.out.println(Mybody.Id + " Treatment for"+ Mybody.OnhandCasualty.ID);

			Mybody.Action=RespondersActions.ReportResult;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.TaskExecution_treatment ;
			Mybody.EndActionTrigger= null;
		}
		// ++++++ 7- +++++++
		else if (Mybody.Action==RespondersActions.ReportResult &&  Mybody.EndActionTrigger== RespondersTriggers.EndingAction) 
		{
			InformResult(ACLPerformative.InformResultTreatment,Mybody.Bronzecommander_amb);
			//Mybody.OnhandCasualty.IsAssignedToResponder = false ;  wrong will waite 
			//Mybody.OnhandCasualty=null;

			Mybody.Action=RespondersActions.InfromResultTreatment;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Comunication;   
			Mybody.EndActionTrigger= null;

		}	
		// ++++++ 8- +++++++
		else if (Mybody.Action==RespondersActions.InfromResultTreatment && Mybody.Acknowledged) //Trigger end of action
		{
			Mybody.Acknowledged=false;Mybody.Sending=false; 
			//System.out.println(Mybody.Id + "back to Treatment "+ Mybody.OnhandCasualty.ID);
			if ( Mybody.OnhandCasualty.Triage_tage== 5 )
			{
				Mybody.InterpretedTrigger= RespondersTriggers.CasualtyDead;						
				Mybody.WorkingonCasualty= null ;  
				Mybody.OnhandCasualty.IsAssignedToResponder = false ;
				Mybody.OnhandCasualty=null;
			}
			else
			{
				Mybody.InterpretedTrigger= RespondersTriggers.DoneTask;	

				Mybody.WorkingonCasualty= null ;  
				Mybody.OnhandCasualty.IsAssignedToResponder = false ;
				Mybody.OnhandCasualty=null;

			}

		}
	}	

	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	// Paramedic- TransferCasualtytoVehicle ( Decentralized - Centralized )
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^	
	private void  ParamedicBehavior_TransferCasualtytoVehicle()
	{
		// ++++++ 7- +++++++
		if ( Mybody.CommTrigger==RespondersTriggers.GetCommandTransferToVehicle ) //Trigger communicate													
		{																		
			Mybody.Action=RespondersActions.GetcammandTransfer ;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Comunication; // TaskPlanning;				
			Mybody.EndofCurrentAction=  InputFile.GetCommand_duration  ; 	
			Mybody.CommTrigger=null;

			//System.out.println( Mybody.Id + "+++++++++++1++++++++++++" +Mybody.TargetCasualty.ID+" " +Mybody.TargetCasualty.IsAssignedToResponder  +" "+ Mybody.TargetCasualty.UnderAction);
		}	
		// ++++++8- +++++++
		else if (  Mybody.Action==RespondersActions.GetcammandTransfer &&  Mybody.EndActionTrigger== RespondersTriggers.EndingAction  ) //Trigger end of action													
		{	
			Mybody.CurrentSender.Acknowledg(Mybody) ;

			Mybody.Assign_DestinationCasualty(Mybody.TargetCasualty) ;
			Mybody.SensingeEnvironmentTrigger=null;
			Mybody.Action=RespondersActions.GoToCasualty;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Movementonincidentsite; //TaskPlanning?
			Mybody.EndActionTrigger=null;
		}
		// ++++++ 3- +++++++

		else if (Mybody.Action==RespondersActions.GoToCasualty && Mybody.SensingeEnvironmentTrigger==null)// Action
		{			
			Mybody.Walk();
		}	
		// ++++++ 4- +++++++
		else if (Mybody.Action==RespondersActions.GoToCasualty  && Mybody.SensingeEnvironmentTrigger==RespondersTriggers.ArrivedCasualty  && ! Mybody.OnhandCasualty.IsAssignedToResponder  )//Trigger Arrived
		{	
			Mybody.OnhandCasualty.IsAssignedToResponder = true ;

			OnTransferCasualtoVichelActionS( );
			//System.out.println( Mybody.Id + "+++++++++++++2++++++++++" +Mybody.TargetCasualty.ID);

			Mybody.Assign_DestinationCordon(Mybody.assignedIncident.loadingArea.Location); 
			Mybody.Action=RespondersActions.TransferCasualtytoLA ;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.TaskExecution_transfertoV;			
			Mybody.SensingeEnvironmentTrigger=null ;

		}
		// ++++++ 9- +++++++
		else if (Mybody.Action==RespondersActions.TransferCasualtytoLA && Mybody.SensingeEnvironmentTrigger==null  && Mybody.OnhandCasualty.UnderAction!=CasualtyAction.Collectinformation)// Action
		{			
			Mybody.Walk();
			Mybody.OnhandCasualty.setLocation_and_move(Mybody.Return_CurrentLocation());	
		}
		// ++++++ 10- +++++++
		else if (Mybody.Action==RespondersActions.TransferCasualtytoLA  && Mybody.SensingeEnvironmentTrigger==RespondersTriggers.ArrivedTargetObject  )
		{	

			//System.out.println( Mybody.Id + "+++++++++++++3++++++++++" +Mybody.TargetCasualty.ID);

			if (  AssignedVehicletoCasualty.onBarkofLA1==true  )
			{
				Mybody.Assign_DestinationVehicle(AssignedVehicletoCasualty);
				Mybody.Action=RespondersActions.TransferCasualtytoVehicle ;
				Mybody.BehaviourType1=RespondersBehaviourTypes1.TaskExecution_transfertoV;
				Mybody.SensingeEnvironmentTrigger=null;			
			}
		}		
		else if (Mybody.Action==RespondersActions.TransferCasualtytoVehicle && Mybody.SensingeEnvironmentTrigger==null )// Action
		{			
			Mybody.Walk();
			Mybody.OnhandCasualty.setLocation_and_move(Mybody.Return_CurrentLocation());	
		}
		// ++++++ 10- +++++++
		else if (Mybody.Action==RespondersActions.TransferCasualtytoVehicle && Mybody.SensingeEnvironmentTrigger==RespondersTriggers.ArrivedVehicle)
		{	
			loadingCasualtyinVichelActionS();

			Mybody.Action=RespondersActions.loadCasualtyToVehicle;	
			Mybody.BehaviourType1=RespondersBehaviourTypes1.TaskExecution_transfertoV;
			Mybody.EndofCurrentAction=   InputFile.loadingToVehicle_duration ; 	//Because it is static but it could be changed based on type of severity			
			Mybody.SensingeEnvironmentTrigger=null;

		}
		// ++++++ 11- +++++++
		else if ( Mybody.Action==RespondersActions.loadCasualtyToVehicle &&  Mybody.EndActionTrigger== RespondersTriggers.EndingAction ) //Trigger end of action													
		{						
			loadingCasualtyinVichelActionE1();
			Mybody.WorkingonCasualty= null ;  //used only in Face to face communication
			Mybody.OnhandCasualty.IsAssignedToResponder = false ;

			Mybody.Action=RespondersActions.TransferCasualtytoHospital;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.TaskExecution_transfertoH ;		
			Mybody.EndActionTrigger=null;
		}	
		// ++++++ 11- +++++++
		else if ( Mybody.Action==RespondersActions.TransferCasualtytoHospital &&  Mybody.SensingeEnvironmentTrigger==RespondersTriggers.ArrivedIncidentBackfromHosp ) 													
		{									

			Mybody.SensingeEnvironmentTrigger=null;

			InformResult(ACLPerformative.InformResultVTransfer , Mybody.Bronzecommander_amb);  	//inside message to My CCO

			Mybody.OnhandCasualty = null;
			AssignedVehicletoCasualty=null;	

			Mybody.Action=RespondersActions.InfromResultTransferToVehicle;
			Mybody.BehaviourType1=RespondersBehaviourTypes1.Comunication;         				
		}	
		// ++++++ 12- +++++++
		else if (Mybody.Action==RespondersActions.InfromResultTransferToVehicle && Mybody.Acknowledged ) 
		{						
			//Mybody.InterpretedTrigger= RespondersTriggers.DoneTask;
			Mybody.InterpretedTrigger= RespondersTriggers.IamNOTinLocation;
			Mybody.Acknowledged=false;Mybody.Sending=false; 
			//System.out.println( Mybody.Id + "++++++++++++++++++++++++2+++++++++++++++++++++++ +"  +  " inform result  " + Mybody.assignedIncident.CCO_ambcommander.Id );
		}

	}

} //end class
