package A_Agents;

import java.util.ArrayList;
import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;

import A_Environment.RoadNode;
import B_Classes.HospitalRecord;
import B_Communication.ACL_Message;
import B_Communication.Command;
import D_Ontology.BuildStaticFuction;
import D_Ontology.Ontology;
import D_Ontology.Ontology.Ambulance_TaskType;
import D_Ontology.Ontology.CasualtyAction;
import D_Ontology.Ontology.CasualtyStatus;
import D_Ontology.Ontology.CasualtyinfromationType;
import D_Ontology.Ontology.CommunicationMechanism;
import D_Ontology.Ontology.RespondersTriggers;
import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.space.gis.Geography;

public class Hospital {


	ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule(); 

	//-----------------------------------------------
	public String  ID;
	private int availableBedsCount=3;	
	public int Lastmessagereaded=0 ;	
	public CommunicationMechanism Currentusedcom=null;
	public Command CurrentCommandRequest=null ;	
	int Neededcapacity=0;

	// --------------------------------------------------------------
	private Coordinate Location;
	public String Nodename_Location;
	public RoadNode Node;
	public Context<Object> context;
	public Geography<Object> geography;

	// --------------------------------------------------------------	
	public ArrayList<Casualty> casualtiesinHospital = new ArrayList<Casualty>();
	public List<ACL_Message> Message_inbox = new ArrayList<ACL_Message>();

	//##############################################################################################################################################################

	public Hospital(Context<Object> _context, Geography<Object> _geography,String _ID, RoadNode _Node ,String _Nodename_Location, int _availableBedsCount) {

		context=_context;
		geography=_geography;


		ID = _ID ;
		Nodename_Location=_Nodename_Location;
		Node= _Node ;
		Node.Name=ID;
		Location= Node.getCurrentPosition();
		availableBedsCount= _availableBedsCount ;		

		//------------------------------------------------------		
		GeometryFactory fac = new GeometryFactory();
		context.add(this);		
		geography.move(this, fac.createPoint(Location));

	}

	//##############################################################################################################################################################
	public void  Commander_ACO_InterpretationMessage()  // not yet implemented
	{
		boolean  done= true;
		ACL_Message currentmsg = Message_inbox.get(Lastmessagereaded);		 			
		Lastmessagereaded++;
		Responder CurrentSender=null;

		if ( currentmsg.sender instanceof Responder )
			CurrentSender= (Responder)currentmsg.sender;
		else if ( currentmsg.sender instanceof EOC )
			;



		switch( currentmsg.performative) {
		case Requste :
			CurrentCommandRequest=((Command) currentmsg.content);

			//++++++++++++++++++++++++++++++++++++++
			if (CurrentSender instanceof Responder_Ambulance &&  CurrentCommandRequest.commandType1 ==Ambulance_TaskType.LookforHospital )  
			{		
				Neededcapacity= Neededcapacity + CurrentCommandRequest.need;


			}
			//++++++++++++++++++++++++++++++++++++++
			break;
		default:
			done= true;
		} // end switch

	}	

	public void Acknowledg() //in Sender 
	{

	}

	//##############################################################################################################################################################	
	//													Actions 
	//##############################################################################################################################################################
	public void Dropping_off_Casualty( Casualty ca )
	{
		casualtiesinHospital.add(ca);
		ca.TimeAdmissioninHospital=schedule.getTickCount() ;
		ca.Status = CasualtyStatus.HospitalTreated;	
		ca.UnderAction=CasualtyAction.OnHospital; 
		ca.Triage_tageBeforHosp=ca.Triage_tage ;
		
		if( ca.CurrentRPM >= 1 && ca.CurrentRPM <= 4 )
			ca.Triage_tage=1;
		else if(ca.CurrentRPM >= 5 && ca.CurrentRPM <= 8 )
			ca.Triage_tage=2;
		else if( ca.CurrentRPM >= 9 && ca.CurrentRPM <= 12 )
			ca.Triage_tage=3;
		else  if( ca.CurrentRPM == 0)
		{ ca.Triage_tage=5; ca.UnderAction=CasualtyAction.DeceasedonHospital ; }
		
		
		ca.AcurrateCI= CasualtyinfromationType.Evacuated_KnowHosp ;
		
	}

	public int getAvailableBedsCount()
	{
		return availableBedsCount;
	}

	public void setAvailableBedsCount(int bedsCount)
	{
		availableBedsCount = bedsCount;
	}

	public String getName() 
	{
		return this. Nodename_Location;
	}
}
