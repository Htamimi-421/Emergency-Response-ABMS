package B_Classes;
import java.util.ArrayList;
import java.util.List;
import A_Agents.Responder_Police;
import A_Environment.Cordon;
import A_Environment.TacticalArea;
import B_Communication.Report;
import D_Ontology.Ontology.GeneralTaskStatus;
import D_Ontology.Ontology.Police_ActivityType;
import D_Ontology.Ontology.Police_ResponderRole;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ISchedule;

public class Activity_Police {
	
	ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule();
	
	//---------------------------------------------------

	public Police_ActivityType  Activity;  // SetupTacticalAreas ,Communication , SecureScene_outerCordons ,CollectInformation 
	public Police_ResponderRole  BronzRole;
	public Responder_Police  BronzCommander;     
	public GeneralTaskStatus         ActivityStatus;         // Waiting or in-progress or Done or closed
	
	public boolean  SingleActivity;
	public boolean  Tempcommnader=false;
	
	public double  ActivityCreateTime, ActivityStartTime, ActivityEndTime;

	//-------------------------------------------------------
	public Cordon inCordon=null ; 
	public TacticalArea TA=null ; 
	
	public int NRespondersOC= 0;  //special setup TA
	public int NRespondersCA= 0;  
	public int NRespondersRC= 0;  
	
	public boolean ClosedOC=false ; //special setup TA
	public boolean ClosedCA=false ;
	public boolean ClosedRC=false ;
		
	//------------------------------------------------------	
	boolean ThereisupdatetoSM=false;  // there is update to silver meeting from this activity 


		
	//---------------------------------------------------
	public int MaxResponders= 0; //to stop allocate more responders for this activity 
	public List<Responder_Police> AllocatedResponderforThisActivity = new ArrayList<Responder_Police>(); 
	List<Report> ActivityReportList = new ArrayList<Report>();	

	//***********************************************************************************************
	public Activity_Police (Police_ActivityType _Activity,Police_ResponderRole  _Role ,boolean  _SingleActivity ) {

		Activity =_Activity;		
		BronzRole=  _Role ;
		SingleActivity=_SingleActivity;

		ActivityStatus= GeneralTaskStatus.Waiting;
		ActivityCreateTime= schedule.getTickCount();
	}

	//---------------------------------------------------
	// Allocation commander
	public void ResourseAllocation_commander( Responder_Police _BronzCommander) {

		BronzCommander=_BronzCommander;
	}

	public void ResourseAllocation_Responders( Responder_Police Assignedresponders) {

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
