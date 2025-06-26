package B_Classes;

import A_Agents.Responder_Ambulance;
import A_Environment.Incident;
import D_Ontology.Ontology.GeneralTaskStatus;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ISchedule;


//used by strategic command group- array in its class
public class IncidentPlan {

	public String                    	IncidentPlanID;
	public Incident                         	Incident ;
	public GeneralTaskStatus         	IncidentPlanStatus;         // Waiting or inprogress or Done
	
	public Responder_Ambulance  	_Commander_Sliver_Ambulance ; 
	//public Commander_Sliver_FireEngine  	_Commander_Sliver_FireEngine ; 
	//public Commander_Sliver_Police   	    _Commander_Sliver_Police ; 
	
	public int     TotalResponderes_Ambulance , TotalResponderes_FireEngine  ,TotalResponderes_Police, TotalVehicles_Ambulance ,TotalVehicles_FireEngine  ,TotalVehicles_Police ; // Resource
	public int     NeedResponderes_Ambulance , NeedResponderes_FireEngine  ,NeedResponderes_Police, NeedVehicles_Ambulance ,NeedVehicles_FireEngine  ,NeedVehicles_Police ; // Need Resource
	
	public double  IncidentPlanCreateTime, IncidentPlanStartTime, IncidentPlanEndTime;
	
	ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule();

	//***********************************************************************************************
	public IncidentPlan (String _IncidentPlanID  ) {
		
		IncidentPlanID = _IncidentPlanID ;
	    IncidentPlanCreateTime = schedule.getTickCount();	
	}
	
	//---------------------------------------------------
	// Allocation Vehicles
	public void ResourseAllocation_Vehicles( ) {
		
		IncidentPlanStatus = GeneralTaskStatus.Inprogress;
	}
	//---------------------------------------------------
	// Allocation Responder
	public void ResourseAllocation_Responder( ) {
			
	}
		
	public void IncidentPlanIDClosed() {
		IncidentPlanEndTime = schedule.getTickCount();
		IncidentPlanStatus = GeneralTaskStatus.Done;
	}



}// end class


