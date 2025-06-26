package B_Communication;

import java.util.ArrayList;
import java.util.List;

import A_Agents.Responder;
import A_Agents.Responder_Ambulance;
import D_Ontology.Ontology;
import D_Ontology.Ontology.ACLPerformative;
import D_Ontology.Ontology.CommunicationMechanism;
import D_Ontology.Ontology.TypeMesg;

public class ACL_Message {

	public ACLPerformative performative;  
	public Object sender;  // Object
	public Object receiver ; 
	public List<Object> Listofreceiver= new ArrayList<Object>();
	public Object content;  // command-coordinate   or Report
	public double    time; // no ticks;

	public boolean Inernal=false;
	public boolean  External=false;

	public TypeMesg TypeMesg1= TypeMesg.Inernal;
	public int NoofMesg=0;

	public CommunicationMechanism ComMech;
	//public boolean changed_com=false;

	public ACL_Message( ACLPerformative _performative, Object _sender,  Object _receiver ,Object _content,double _time , CommunicationMechanism _ComMech , int _NoofMesg ,TypeMesg _TypeMesg1 )
	{
		performative=_performative;
		time=_time;
		content=_content;
		sender =_sender;

		

		if( _ComMech == CommunicationMechanism.RadioSystem_BoradCast  || _ComMech == CommunicationMechanism.FF_BoradCast)
		{
			for (Object  OB :(List<Object>) _receiver)
				Listofreceiver.add(OB)  ;
		}
		else
			receiver =_receiver;

		ComMech=_ComMech;
		NoofMesg=  _NoofMesg;
		TypeMesg1 = _TypeMesg1 ;


		if (ComMech == CommunicationMechanism.FaceToFace_and_Phone   || 
				ComMech == CommunicationMechanism.SharedTactical_RadioSystem || 
				ComMech == CommunicationMechanism.SilverMeeting   || 
				performative== ACLPerformative.CallForSilverMeeting )
			External=true;
		else
			Inernal=true;







	}

	//	public ACL_Message( ACLPerformative _performative, Object _sender, List<Object> _Listofreceiver ,Object _content,double _time , CommunicationMechanism _ComMech)
	//	{
	//		performative=_performative;
	//		time=_time;
	//		content=_content;
	//		sender =_sender;
	//		Listofreceiver =_Listofreceiver;
	//		 System.out.println( " >>>>>>>>>>>>>>>>>>>>>>>" +Listofreceiver.size()   );
	//		ComMech=_ComMech;
	//	}
}
