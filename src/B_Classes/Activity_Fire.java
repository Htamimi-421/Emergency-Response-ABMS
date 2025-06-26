
package B_Classes;
import java.util.ArrayList;
import java.util.List;
import A_Agents.Responder_Fire;
import A_Environment.Cordon;
import A_Environment.Sector;
import B_Communication.Report;
import D_Ontology.Ontology.Fire_ActivityType;
import D_Ontology.Ontology.Fire_ResponderRole;
import D_Ontology.Ontology.GeneralTaskStatus;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ISchedule;

public class Activity_Fire {
	
	ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule();
	
	//---------------------------------------------------

	public Fire_ActivityType  Activity;  //SceneResponse , Communication, Tnasporation_loading ,HospitalSelection
	public Fire_ResponderRole  BronzRole;
	public Responder_Fire  BronzCommander;     
	public GeneralTaskStatus         ActivityStatus;         // Waiting or in-progress or Done or closed
	
	public boolean  SingleActivity;
	public boolean  Tempcommnader=false;
	
	public double  ActivityCreateTime, ActivityStartTime, ActivityEndTime;

	//-------------------------------------------------------
	public Sector inSector ; //special
	public Cordon inCordon=null ;  
	
	public Boolean informed_TA_CCS_established=false ,   informed_TA_RC_established=false ; //special
		
	//---------------------------------------------------

	public List<Responder_Fire> AllocatedResponderforThisActivity = new ArrayList<Responder_Fire>(); 
	List<Report> ActivityReportList = new ArrayList<Report>();	

	//***********************************************************************************************
	public Activity_Fire (Fire_ActivityType _Activity,Fire_ResponderRole  _Role ,boolean  _SingleActivity ) {

		Activity =_Activity;		
		BronzRole=  _Role ;
		SingleActivity=_SingleActivity;
		inSector=null;

		ActivityStatus= GeneralTaskStatus.Waiting;
		ActivityCreateTime= schedule.getTickCount();
	}

	//---------------------------------------------------
	// Allocation commander
	public void ResourseAllocation_commander( Responder_Fire _BronzCommander) {

		BronzCommander=_BronzCommander;

	}

	public void ResourseAllocation_Responders( Responder_Fire Assignedresponders) {

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
