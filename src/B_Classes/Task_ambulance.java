package B_Classes;
import java.util.List;
import A_Agents.Casualty;
import A_Agents.Hospital;
import A_Agents.Responder;
import A_Agents.Responder_Ambulance;
import A_Agents.Vehicle;
import A_Environment.Sector;
import D_Ontology.BuildStaticFuction;
import D_Ontology.Ontology.Ambulance_TaskType;
import D_Ontology.Ontology.GeneralTaskStatus;
import D_Ontology.Ontology.RespondersActions;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ISchedule;


//used by Bronze operational commander- array in its class
public class Task_ambulance {

	public String                   TaskID;
	public Ambulance_TaskType       TaskType;
	public GeneralTaskStatus        TaskStatus; // Waiting or Inprogress or Done 	
	public Casualty                 TargetCasualty;
	public Responder                AssignedResponder;

	public Responder                BZCcommnader;
	public Sector                   InSector=null;	

	public  Vehicle  AssignedAmbulance;         
	public  Hospital  AssignedHospital;

	public double TaskCreateTime, TaskStartTime, TaskEndTime;   


	public double  TimeofdoneTreatment= 0 ;  //used by SC to order the cas for transfer 
	public double  TimeofsendRequest= 0 ;  //used by ALC to order the cas for transfer 

	//-----------------------------------------
	public  int priority_level=99; 
	
	public boolean priority_leveldgetbad=false;
	
	
	public boolean Treated=false; //Spatial ..........to identify ready to send to ALC/ used by CCO 
	public boolean Pre_Treated=false;
	public boolean Decased=false  ;//Spatial ..........to identify deceased/ used by CCO 
	public boolean  Transferd=false; //Spatial ..........to identify used by CCO 
	public boolean NewupdateHosTOALC=false;
	public boolean SendrequestoALC=false; //Spatial ........after send to ALC  /used by SC
	public boolean SendTriageReporttoAIC=false; //Spatial ........after send to AIC /used by CCO
	public boolean SendDecasedReporttoAIC=false; //Spatial ........after send to AIC /used by CCO
	public boolean SendfinalReporttoAIC=false; //Spatial ........after send to AIC /used by CCO
	
	
	public boolean CountinHospNeed=false; //Spatial ........after get result triage /used by AIC
	
	
	public boolean Send_H_AllocationTOCCS=false;  //used by AIC
	public boolean Send_Am_AllocationTOCCS=false;  //used by ALC

	//-----------------------------------------

	ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule();

	//***********************************************************************************************
	public Task_ambulance( Casualty _TargetCasualty, Ambulance_TaskType _TaskType  ) {
		TaskID = "0";		
		TaskType = _TaskType;

		TargetCasualty = _TargetCasualty;

		TaskStatus = GeneralTaskStatus.Waiting;	
		TaskCreateTime = schedule.getTickCount();
		//InSector=_InSector;
	}

	public Task_ambulance(Casualty _TargetCasualty, Ambulance_TaskType _TaskType ,Responder   _BZCcommnader , Hospital _RecivingHospital ) //uesd by AIC
	{

		TaskID = "0";
		TaskType = _TaskType;

		TargetCasualty = _TargetCasualty;
		 BZCcommnader=_BZCcommnader;
		AssignedHospital =_RecivingHospital ;
		//InSector=_SCcommnader.AssignedSector ;

		TaskStatus = GeneralTaskStatus.Waiting;	
		TaskCreateTime = schedule.getTickCount();
	}

	public Task_ambulance( Casualty _TargetCasualty, Ambulance_TaskType _TaskType , Responder   Res ) //TaskApproach=TaskAllocationApproach.Decentralized_Autonomous
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
	public double Caculate_TaskWaitingDuration() {

		if (TaskStatus == TaskStatus.Waiting)
			return schedule.getTickCount() - TaskCreateTime;
		else
			return -1;

	}

	//---------------------------------------------------
	public Responder_Ambulance ckeck_Distance_befor_TaskAssignment(List<Responder_Ambulance> NominatedResponders) {

		Responder_Ambulance NearestRes =null;
		double MinDistance=999999;

		for (Responder_Ambulance  Res:NominatedResponders)
		{
			//System.out.println("  "+ Res +"   "+ TargetCasualty );
			double realDistance = BuildStaticFuction.DistanceC(TargetCasualty.geography, Res.Return_CurrentLocation(),TargetCasualty.getCurrentLocation());

			if( realDistance <= MinDistance )
			{ MinDistance=realDistance;NearestRes=Res; }			 
		}

		return NearestRes;

	}	
	//---------------------------------------------------  pre rescue Decentralized
		public Responder_Ambulance ckeck_Distance_befor_TaskAssignment_pre(Responder_Ambulance SC , List<Responder_Ambulance> NominatedResponders) {

			Responder_Ambulance NearestRes =null;
			double MinDistance=999999;

			for (Responder_Ambulance  Res:NominatedResponders)
			{
					if( Res.OnhandCasualty==null && (  Res.Action ==RespondersActions.SearchCasualty  || Res.Action ==RespondersActions.Noaction  ||Res.Action ==RespondersActions.GoToCasualty  ) )
				{
					double realDistance = BuildStaticFuction.DistanceC(TargetCasualty.geography, Res.Return_CurrentLocation(), SC.Return_CurrentLocation());
				
				if( realDistance <= MinDistance )
				{ MinDistance=realDistance;NearestRes=Res; }	
				
				}
			}

			return NearestRes;

		} 
}// end class
