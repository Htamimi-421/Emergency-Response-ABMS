package B_Communication;

import com.vividsolutions.jts.geom.Coordinate;

import A_Agents.Casualty;
import A_Environment.Incident;
import D_Ontology.Ontology.CasualtyStatus;
import repast.simphony.context.Context;
import repast.simphony.space.gis.Geography;

public class Casualty_info {   // used in sending
	
	
	public Casualty     ca ;
	public int priority_level;
	public boolean IsSeverityGetbad=false;
	public boolean  ThisFatlity =false;
	
	public boolean IwillupdateaboutTrapparrivedCCS=false;



public  Casualty_info(	 Casualty     _ca , int _priority_level   ) 
	{
	     ca=_ca ;
		 priority_level=_priority_level ;
	
	}

}