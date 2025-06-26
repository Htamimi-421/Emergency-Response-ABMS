package Other;

import java.util.List;

import A_Agents.Casualty;
import A_Agents.Responder_Ambulance;
import A_Environment.PointDestination;
import D_Ontology.Ontology;
import D_Ontology.Ontology.Ambulance_ResponderRole;
import D_Ontology.Ontology.RespondersActions;

public class other {
	
	//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
//	@Watch(watcheeClassName = "Agents.Vehicle_Ambulance", watcheeFieldNames = "InIncidentSite", query = "within 10", whenToTrigger = WatcherTriggerSchedule.IMMEDIATE)
//	public void SeeVehicleInScence() {
//
//		//System.out.println("              "+ this.Id +" see  _________________" + this.Role+"    "+ this.Status + "___ " + this.Message_inbox.size());
//		WaitingSeeVehicles=true;
//
//	}
//
//	@Watch(watcheeClassName = "Agents.Casualty", watcheeFieldNames = "SAExtrication", query = "within 10", whenToTrigger = WatcherTriggerSchedule.IMMEDIATE)
//	public void SeeExtractCasualty() {
//
//		System.out.println("              "+ this.Id +" see  _________________SAExtrication--------------------------------------oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo-------------------------------------------");
//		//SeeExtractCasualty=true;
//
//	}
	
	
	//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
//		public int AsessNumberofCasualtyinLRUDDirection (PointDestination  Dir , boolean chekSeverity) 
//		{
//			//1- Get All Near Responder Objects
//			@SuppressWarnings("unchecked") 
//			List<Casualty> nearObjects_Responder= (List<Casualty>) Ontology.GetObjectsWithinDistance(Dir,Casualty.class, this.assignedIncident.radius_incidentCircle_LRUD - 3);// responder of ambulance
//
//
//			if (! chekSeverity)
//			{
//				for (int i = 0; i < nearObjects_Responder.size(); i++) 
//					if (nearObjects_Responder.get(i).Outsidebulding == false  ||nearObjects_Responder.get(i).SurroundingArea == true  ) {
//						nearObjects_Responder.remove(i);
//						i--; 
//					}
//			}
//
//			else //red only
//			{
//				for (int i = 0; i < nearObjects_Responder.size(); i++) 
//					if (nearObjects_Responder.get(i).Outsidebulding == false  ||nearObjects_Responder.get(i).SurroundingArea == true || nearObjects_Responder.get(i).priority_level !=1 ) {
//						nearObjects_Responder.remove(i);
//						i--; 
//					}
//			}
//
//			return nearObjects_Responder.size();
//		}
//
//		//*****************************************
//		public int AssessSituationforTriageAction() 
//		{
//			int Neededgroup=0;
//
//			NumberofCasualtyinLRUD_list.clear();
//			NumberofCasualtyinLRUD_list.add(AsessNumberofCasualtyinLRUDDirection (assignedIncident.PointL,false ) ) ; // 0   LRUD
//			NumberofCasualtyinLRUD_list.add(AsessNumberofCasualtyinLRUDDirection (assignedIncident.PointR ,false) ) ; // 1   LRUD
//			NumberofCasualtyinLRUD_list.add(AsessNumberofCasualtyinLRUDDirection (assignedIncident.PointU ,false) ) ; // 2   LRUD
//			NumberofCasualtyinLRUD_list.add(AsessNumberofCasualtyinLRUDDirection (assignedIncident.PointD,false ) ) ; // 3   LRUD
//
//			Neededgroup=0;
//			for ( int num :NumberofCasualtyinLRUD_list)
//				if (num !=0)
//					Neededgroup++;
//
//			return Neededgroup ;
//		}
//
//		//*****************************************
//		public int AssessSituationforTreatmentAction() 
//		{
//			int Neededgroup=0;
//
//			NumberofCasualtyinLRUD_list.clear();
//			NumberofCasualtyinLRUD_list.add(AsessNumberofCasualtyinLRUDDirection (assignedIncident.PointL,true ) ) ; // 0   LRUD
//			NumberofCasualtyinLRUD_list.add(AsessNumberofCasualtyinLRUDDirection (assignedIncident.PointR,true ) ) ; // 1   LRUD
//			NumberofCasualtyinLRUD_list.add(AsessNumberofCasualtyinLRUDDirection (assignedIncident.PointU,true ) ) ; // 2   LRUD
//			NumberofCasualtyinLRUD_list.add(AsessNumberofCasualtyinLRUDDirection (assignedIncident.PointD,true ) ) ; // 3   LRUD
//
//			Neededgroup=0;
//			for ( int num :NumberofCasualtyinLRUD_list)
//				if (num !=0)
//					Neededgroup++;
//
//			return Neededgroup ;
//		}
//
//		//*****************************************
//		public PointDestination DirctionofHighestnumberofcasualty() 
//		{	
//			int DirctionofHighestnumberofcasualty=-1;
//			PointDestination SelectedDirction=null;
//			int  maxnumb=0;
//
//			for (int i=0 ; i < NumberofCasualtyinLRUD_list.size();i++ )
//			{
//				int numofcasualty=NumberofCasualtyinLRUD_list.get(i);
//
//				if(numofcasualty >= maxnumb )
//				{  DirctionofHighestnumberofcasualty=i;maxnumb=numofcasualty ;}			 
//			}
//
//			if (DirctionofHighestnumberofcasualty!=-1)
//				NumberofCasualtyinLRUD_list.set(DirctionofHighestnumberofcasualty, -1);
//
//			switch (DirctionofHighestnumberofcasualty ) {
//			case 0:
//				SelectedDirction= assignedIncident.PointL;
//				break;
//			case 1:
//				SelectedDirction=   assignedIncident.PointR;
//				break;
//			case 2:
//				SelectedDirction=  assignedIncident.PointU;
//				break;			
//			case 3:
//				SelectedDirction=  assignedIncident.PointD;
//				break;			
//			}
//
//			return SelectedDirction;
//
//		}
//
	
	
	
//	//*******************************************************************************************************	
//	public void CurrentDriverInScence ( ) 
//	{
//		//1- Get All Near Responder Objects
//		@SuppressWarnings("unchecked") 
//		List<Responder_Ambulance> nearObjects_Responder= (List<Responder_Ambulance>) Ontology.GetObjectsWithinDistance(this,Responder_Ambulance.class, 10);// responder of ambulance
//
//		// 2- Remove all non-building objects to get a list of Driver
//		for (int i = 0; i < nearObjects_Responder.size(); i++) 
//			if (nearObjects_Responder.get(i).Role != Ambulance_ResponderRole.Driver || !(nearObjects_Responder.get(i).Action==RespondersActions.Noaction)) {
//				nearObjects_Responder.remove(i);
//				i--; //Because if delete one object , next object not yet checked will take the previous number
//			}	
//
//		CurrentDriver_list.clear();
//
//		for (Responder_Ambulance  Dr :nearObjects_Responder)
//			CurrentDriver_list.add(Dr);
//
//	}
//
//	//*******************************************************************************************************	
//	public void CurrentParamdicInScence (RespondersActions checkStatus  ) 
//	{
//		//1- Get All Near Responder Objects
//		@SuppressWarnings("unchecked") 
//		List<Responder_Ambulance> nearObjects_Responder= (List<Responder_Ambulance>) Ontology.GetObjectsWithinDistance(this,Responder_Ambulance.class, 15);// responder of ambulance
//
//		// 2- Remove all non-building objects to get a list of Driver
//		if ( checkStatus ==null )
//		{
//			for (int i = 0; i < nearObjects_Responder.size(); i++) 
//				// if (nearObjects_Responder.get(i).Role != Ambulance_ResponderRole.Paramedic ||  ! ( nearObjects_Responder.get(i).Status==RespondersStatus.Walking  || nearObjects_Responder.get(i).Status==RespondersStatus.WaitingRequest) ) {
//				if (nearObjects_Responder.get(i).Role != Ambulance_ResponderRole.Paramedic ||   nearObjects_Responder.get(i).OnhandCasualty != null ) {
//					nearObjects_Responder.remove(i);
//					i--; //Because if delete one object , next object not yet checked will take the previous number
//				}	
//		}
//		else if ( checkStatus !=null )
//		{
//			for (int i = 0; i < nearObjects_Responder.size(); i++) 
//				if (nearObjects_Responder.get(i).Role != Ambulance_ResponderRole.Paramedic ||  nearObjects_Responder.get(i).Action!=checkStatus   ) {
//					nearObjects_Responder.remove(i);
//					i--; //Because if delete one object , next object not yet checked will take the previous number
//				}	
//		}
//
//		CurrentParamedictoCall_list.clear();
//
//		for (Responder_Ambulance  pr :nearObjects_Responder)
//			CurrentParamedictoCall_list.add(pr);
//
//		//System.out.println("              "+ this.Id +  " Ambulance CurrentParamdicInScence _list " + CurrentParamedictoCall_list.size());
//	}
	

//	//*******************************************************************************************************	
//	public Responder_Ambulance NearestParamdic ( ) 
//	{
//		Responder_Ambulance NearstResponder=null;
//		double dis, mindis=999999;
//
//		for (Responder_Ambulance  pr :CurrentParamedictoCall_list)
//		{
//
//			dis= Ontology.DistanceC(geography, this.Return_CurrentLocation(), pr.Return_CurrentLocation());
//
//			if(dis<mindis )
//			{ NearstResponder=pr;mindis =dis; }			 
//		}
//		return NearstResponder;
//	}
	
	//for (ACL_Message M:AllocatedTask.AssignedResponder.Message_inbox )
		//System.out.println(" "+ M.performative  +"   "+((Responder_Ambulance)M.sender).Id +"    " + M.time  +"   " + Mybody.CurrentTick);

}
