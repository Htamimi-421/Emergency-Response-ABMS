package C_SimulationOutput;

import D_Ontology.Ontology.CasualtyAction;

public class CasualtyStatus {

	double  CurrentTick ;
	int TotalTick;

	CasualtyAction Action;
	public int  CurrentRPM;
	public double Current_SurvivalProbability;

	public 	CasualtyStatus( CasualtyAction _Action, int  _CurrentRPM , double  _CurrentTick)
	{
		CurrentTick = _CurrentTick;
		TotalTick=1;

		Action=_Action;
		CurrentRPM= _CurrentRPM;
	}	
	
	public 	CasualtyStatus( CasualtyAction _Action)  //used
	{
		TotalTick=0;
		Action=_Action;
	}
	
	public 	void CasualtyStatusupdate(  double _Current_SurvivalProbability ,int  _CurrentRPM , double  _CurrentTick)  // at begining of action only
	{		
		TotalTick=1;
		
		CurrentRPM= _CurrentRPM;
		Current_SurvivalProbability= _Current_SurvivalProbability;	
	}
	

}
