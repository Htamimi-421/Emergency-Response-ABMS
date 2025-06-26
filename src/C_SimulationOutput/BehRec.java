package C_SimulationOutput;

import D_Ontology.Ontology.Ambulance_ResponderRole;
import D_Ontology.Ontology.Fire_ResponderRole;
import D_Ontology.Ontology.Police_ResponderRole;
import D_Ontology.Ontology.RespondersBehaviourTypes1;
import D_Ontology.Ontology.RespondersBehaviourTypes2;
import D_Ontology.Ontology.RespondersBehaviourTypes3;

public class BehRec {
	//1
	Ambulance_ResponderRole  Role1;	
	Fire_ResponderRole  Role2;
	Police_ResponderRole  Role3;
	//2
	RespondersBehaviourTypes1 BehaviourType1;
	RespondersBehaviourTypes2 BehaviourType2;
	RespondersBehaviourTypes3 BehaviourType3;
	//3
	double  TotalTick=0 ;
	
	
	public 	BehRec( Ambulance_ResponderRole  _Role ,RespondersBehaviourTypes1 _BehaviourType )
	{
		Role1=_Role ;
		BehaviourType1=_BehaviourType;
					
	}
	
	public 	BehRec( Fire_ResponderRole  _Role ,RespondersBehaviourTypes2 _BehaviourType )
	{
		Role2=_Role ;
		BehaviourType2=_BehaviourType;
					
	}
	
	public 	BehRec( Police_ResponderRole  _Role ,RespondersBehaviourTypes3 _BehaviourType )
	{
		Role3=_Role ;
		BehaviourType3=_BehaviourType;
					
	}
	
}
