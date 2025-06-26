package B_Classes;
import java.util.ArrayList;
import java.util.List;
import A_Agents.Casualty;
import A_Agents.Responder;
import A_Agents.Responder_Fire;
import A_Agents.Responder_Police;
import A_Environment.Cordon;
import A_Environment.PointDestination;
import A_Environment.RoadLink;
import D_Ontology.Ontology.Police_TaskType;
import D_Ontology.BuildStaticFuction;
import D_Ontology.Ontology.CasualtyinfromationType;
import D_Ontology.Ontology.GeneralTaskStatus;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ISchedule;

//used by Bronze operational commander- array in its class
public class Task_Police {

	public String                   TaskID;
	public Police_TaskType          TaskType;
	public GeneralTaskStatus        TaskStatus; // Waiting or Inprogress or Done 	
	
	public RoadLink                 TargetRoute;
	public    PointDestination      TargetPEA;
	public Casualty TargetCasualty;
	
	public Responder_Police  AssignedResponder ;
	public List<Responder_Police> AssignedResponderslist = new ArrayList<Responder_Police>(); //its worker

	public Responder                CCcommnader;
	public Cordon                   InCordon=null;	
	public int Priority_clearRoute=0 ;
	
	public double TaskCreateTime, TaskStartTime, TaskEndTime;   
	
	//-----------------------------------------
	// casualty
	public CasualtyinfromationType infromationType= CasualtyinfromationType.None;

	//-----------------------------------------
	//Route
	public boolean SendReporttoPIC=false; //Spatial .......in secure send to PIC /used by CC or RCO
	
	ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule();

	//***********************************************************************************************
	public Task_Police( RoadLink  _TargetRoute, Police_TaskType _TaskType  ) {
		TaskID = "0";		
		TaskType = _TaskType;

		TargetRoute = _TargetRoute;

		TaskStatus = GeneralTaskStatus.Waiting;	
		TaskCreateTime = schedule.getTickCount();
	}
	
	public Task_Police( Casualty _TargetCasualty, Police_TaskType _TaskType  ) {
		TaskID = "0";		
		TaskType = _TaskType;

		TargetCasualty=_TargetCasualty;

		TaskStatus = GeneralTaskStatus.Waiting;	
		TaskCreateTime = schedule.getTickCount();
	}

	//---------------------------------------------------
	public double Caculate_TaskWaitingDuration() {

		if (TaskStatus == TaskStatus.Waiting)
			return schedule.getTickCount() - TaskCreateTime;
		else
			return -1;

	}

	//---------------------------------------------------
	public Responder_Police ckeck_Distance_befor_TaskAssignment(List<Responder_Police> NominatedResponders) {

		Responder_Police NearestRes =null;
		double MinDistance=999999;

		for (Responder_Police  Res:NominatedResponders)
		{
			double realDistance = BuildStaticFuction.DistanceC(TargetCasualty.geography, Res.Return_CurrentLocation(),TargetCasualty.getCurrentLocation());

			if( realDistance <= MinDistance   )
			{ MinDistance=realDistance;NearestRes=Res; }			 
		}


		return NearestRes;
	}
	
	//---------------------------------------------------
	public Responder_Police ckeck_Distance_befor_TaskAssignment_Route(List<Responder_Police> NominatedResponders) {

		Responder_Police NearestRes =null;
		double MinDistance=999999;

		for (Responder_Police  Res:NominatedResponders)
		{
			double realDistance = BuildStaticFuction.DistanceC(Res.geography, Res.Return_CurrentLocation(),TargetRoute.PointofClear.getCurrentPosition());

			if( realDistance <= MinDistance )
			{ MinDistance=realDistance;NearestRes=Res; }			 
		}


		return NearestRes;
	}

}// end class
