package B_Communication;

import A_Agents.Responder;
import D_Ontology.Ontology.ACLPerformative;

public class chanle {
	
	public String ChanelID ;
	public boolean   used=false;
	public Responder user_currentsender=null;
	public Responder TowhomIcall_Reciver=null;
	
	public chanle ( String ID)
	{
		ChanelID=ID;
		
	}

}
