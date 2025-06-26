package C_SimulationOutput;
import D_Ontology.Ontology.Ambulance_ResponderRole;
import D_Ontology.Ontology.RespondersActions;
import D_Ontology.Ontology.RespondersBehaviourTypes1;
import D_Ontology.Ontology.RespondersBehaviourTypes2;
import D_Ontology.Ontology.RespondersBehaviourTypes3;



public class Action {  //not used    !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

	double  CurrentTick ;
	int TotalTick;
	Ambulance_ResponderRole  Role;	
	RespondersActions Action;
	RespondersBehaviourTypes1 BehaviourType;
	String ChaPrint;

	//----------------------------------------------------
	public 	Action( Ambulance_ResponderRole  _Role ,RespondersActions _Action,RespondersBehaviourTypes1 _BehaviourType , double  _CurrentTick)
	{
		CurrentTick = _CurrentTick;
		TotalTick=1;
		Role=_Role ;
		Action=_Action;
		BehaviourType=_BehaviourType;

//		switch( BehaviourType)	{
//		case Idle:	
//			ChaPrint="Id";
//			break;
//		case Travlling :
//			ChaPrint="Tv";
//			break;
//		case RoleAssignment:
//			ChaPrint="RA";
//			break;
//		case WaitingBegin :
//			ChaPrint="WB";
//			break;
//		case TaskPlanning:
//			ChaPrint="TP";
//			break;
//		case TaskExecution_triage :
//			ChaPrint="Tg";
//			break;
//		case TaskExecution_treatment :
//			ChaPrint="Tr";
//			break;			
//		case CasualtyTransferDelay:
//			ChaPrint="TD";
//			break;
//		case TaskExecution_transfertoV:
//			ChaPrint="TV";
//			break;
//		case TaskExecution_transfertoH:
//			ChaPrint="TH";
//			break;
//		case Planformulation:
//			ChaPrint="PF";
//			break;
//		case Planexecution:
//			ChaPrint="PE";
//			break;
//		case WaitingDuring :
//			ChaPrint="WD";
//			break;
//		case Gatheringinfo:
//			ChaPrint="GI";
//			break;
//		case Sharinginfo:
//			ChaPrint="SI";
//			break;
//		case WaitingEnd:
//			ChaPrint="WE";
//			break;
//		case Done:
//			ChaPrint="Dn";
//			break;			
//		}    

	}


}
