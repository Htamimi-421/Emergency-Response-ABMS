package B_Classes;

import java.util.ArrayList;
import java.util.List;
import A_Agents.Responder_Ambulance;
import A_Environment.Sector;
import A_Environment.TacticalArea;
import B_Communication.Report;
import D_Ontology.Ontology.Ambulance_ActivityType;
import D_Ontology.Ontology.Ambulance_ResponderRole;
import D_Ontology.Ontology.GeneralTaskStatus;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ISchedule;

public class Activity_ambulance {
	
	ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule();
	
	//---------------------------------------------------

	public Ambulance_ActivityType  Activity;  //SceneResponse , Communication, Tnasporation_loading ,HospitalSelection
	public Ambulance_ResponderRole  BronzRole;
	public Responder_Ambulance  BronzCommander;     
	public GeneralTaskStatus         ActivityStatus;         // Waiting or in-progress or Done or closed
	
	public boolean  SingleActivity;
	public boolean  Tempcommnader=false;
	
	public double  ActivityCreateTime, ActivityStartTime, ActivityEndTime;

	//-------------------------------------------------------
	public Sector inSector ; //special
	public Boolean informed_TA_sector_established=false ;
	public TacticalArea TA=null ;
	
	public Boolean Triage=false ,  Treatment=false;  //special
	//---------------------------------------------------

	public List<Responder_Ambulance> AllocatedResponderforThisActivity = new ArrayList<Responder_Ambulance>(); 
	List<Report> ActivityReportList = new ArrayList<Report>();	

	//***********************************************************************************************
	public Activity_ambulance (Ambulance_ActivityType _Activity,Ambulance_ResponderRole  _Role ,boolean  _SingleActivity ) {

		Activity =_Activity;		
		BronzRole=  _Role ;
		SingleActivity=_SingleActivity;
		inSector=null;

		ActivityStatus= GeneralTaskStatus.Waiting;
		ActivityCreateTime= schedule.getTickCount();
	}

	//---------------------------------------------------
	// Allocation commander
	public void ResourseAllocation_commander( Responder_Ambulance _BronzCommander) {

		BronzCommander=_BronzCommander;

	}

	public void ResourseAllocation_Responders( Responder_Ambulance Assignedresponders) {

		AllocatedResponderforThisActivity.add(Assignedresponders);

	}
	//---------------------------------------------------

	public void ActivityStart() {
		ActivityStartTime = schedule.getTickCount();
		ActivityStatus = GeneralTaskStatus.Inprogress;
	}

	public void ActivityClosed() {
		ActivityEndTime = schedule.getTickCount();
		ActivityStatus = GeneralTaskStatus.Done;
	}
	
	//---------------------------------------------------
	
	public void UpdateStatus( Report LastActivityReport )
	{
		 ActivityReportList.add(LastActivityReport);
	}
	
	public Report lastReport(  )
	{
		if (ActivityReportList.size() !=0 ) 
		{
			return  ActivityReportList.get( ActivityReportList.size()-1);
			
		}
		else
			return  null;
	}
	
}
