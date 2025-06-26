package B_Classes;
import java.util.ArrayList;
import java.util.List;
import A_Agents.Casualty;
import A_Agents.Responder;
import A_Agents.Responder_Fire;
import A_Environment.RoadLink;
import A_Environment.Sector;
import D_Ontology.BuildStaticFuction;
import D_Ontology.Ontology.Fire_TaskType;
import D_Ontology.Ontology.GeneralTaskStatus;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ISchedule;

//used by Bronze operational commander- array in its class
public class Task_Fire {

	public String                   TaskID;
	public Fire_TaskType       		TaskType;
	public GeneralTaskStatus        TaskStatus; // Waiting or In progress or Done 	
	
	public Casualty                 TargetCasualty;
	public RoadLink                 TargetRoute;
	
	public Responder_Fire               AssignedResponder;
	public List<Responder_Fire> AssignedResponderslist = new ArrayList<Responder_Fire>(); //its worker
	
	public Responder                FSCcommnader;
	public Sector                   InSector=null;	

	public double TaskCreateTime, TaskStartTime, TaskEndTime;   

	public double  TimeofdoneTreatment= 0 ;  //used by SC to order the cas for transfer 
	public double  TimeofsendRequest= 0 ;  //used by ALC to order the cas for transfer 
	public int Priority_clearRoute=0 ;
	//-----------------------------------------
	public boolean Decased=false  ;
	public boolean SendDecasedReporttoFIC=false; //Spatial dead........after send to FIC /used by FSC
	public boolean SendRouteReporttoFIC=false; //Spatial dead........after send to FIC /used by FSC
	
	ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule();

	//***********************************************************************************************
	public Task_Fire( Casualty _TargetCasualty, Fire_TaskType _TaskType  ) // Centralized 
	{
		TaskID = "0";		
		TaskType = _TaskType;

		TargetCasualty = _TargetCasualty;

		TaskStatus = GeneralTaskStatus.Waiting;	
		TaskCreateTime = schedule.getTickCount();
		//InSector=_InSector;
	}
	//---------------------------------------------------
	public Task_Fire( Casualty _TargetCasualty, Fire_TaskType _TaskType , Responder_Fire   Res ) //TaskApproach=TaskAllocationApproach.Decentralized_Autonomous
	{
		TaskID = "0";		
		TaskType = _TaskType;

		TargetCasualty = _TargetCasualty;
		AssignedResponder=Res;
		TaskStatus = GeneralTaskStatus.Done;	
		TaskCreateTime = schedule.getTickCount();
		TaskEndTime = schedule.getTickCount();

	}
	//---------------------------------------------------
	public Task_Fire( RoadLink  _TargetRoute, Fire_TaskType _TaskType  ) //  Route              
	{
		TaskID = "0";		
		TaskType = _TaskType;

		TargetRoute = _TargetRoute;

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
	public Responder_Fire ckeck_Distance_befor_TaskAssignment(List<Responder_Fire> NominatedResponders) {

		Responder_Fire NearestRes =null;
		double MinDistance=999999;

		for (Responder_Fire  Res:NominatedResponders)
		{
			double realDistance = BuildStaticFuction.DistanceC(TargetCasualty.geography, Res.Return_CurrentLocation(),TargetCasualty.getCurrentLocation());

			if( realDistance <= MinDistance )
			{ MinDistance=realDistance;NearestRes=Res; }			 
		}


		return NearestRes;

	}
	
	//---------------------------------------------------
		public Responder_Fire ckeck_Distance_befor_TaskAssignment_Route(List<Responder_Fire> NominatedResponders) {

			Responder_Fire NearestRes =null;
			double MinDistance=999999;

			for (Responder_Fire  Res:NominatedResponders)
			{
				double realDistance = BuildStaticFuction.DistanceC(Res.geography, Res.Return_CurrentLocation(),TargetRoute.PointofClear.getCurrentPosition());

				if( realDistance <= MinDistance )
				{ MinDistance=realDistance;NearestRes=Res; }			 
			}


			return NearestRes;

		}

}// end class
