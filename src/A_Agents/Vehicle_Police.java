
package A_Agents;

import java.util.ArrayList;
import com.vividsolutions.jts.geom.Coordinate;
import A_Environment.Incident;
import A_Environment.PointDestination;
import A_Environment.RoadNode;
import C_SimulationInput.InputFile;
import D_Ontology.Ontology.RespondersTriggers;
import D_Ontology.Ontology.VehicleAction;
import D_Ontology.Ontology.VehicleTriggers;
import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.gis.Geography;

public class Vehicle_Police  extends Vehicle {

	double  CurrentTick , EndofCurrentAction ;	
	ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule(); 

	//-------------------------------------
	public Station_Police AssignedStation=null;
	public boolean FirstTime;
	public boolean InIncidentSite=false;
	boolean Move_responderOrDriver=true;
	int ComeBackoption=0;
	//--------------------------------------

	public ArrayList<Responder_Police> Responders_list=new ArrayList<Responder_Police>();	


	//----------------------------------------------------------------------------------------------
	public Vehicle_Police(Context<Object> context, Geography<Object> geography, Coordinate Location,String Nodename_initialLocation,RoadNode _Node , String ID, int speed ,Station_Police _AssignedStation) {

		super.vehicle(context, geography, Location,Nodename_initialLocation,_Node, ID, speed);	
		this.ColorCode= 3;

		Action= VehicleAction.Idle;
		AssignedStation= _AssignedStation;
		FirstTime=true;
		InIncidentSite=false;
		EndActionTrigger=null;
		Rest(true);
	}

	//********************************************************************************
	public void AssigenResponder(Responder_Police Resp  ){

		Responders_list.add(Resp);	
		//Resp.SensingeEnvironmentTrigger=RespondersTriggers.OnVehicle;
		Resp.Myvehicle= this;		
	}

	public void AssigenIncident2x(Incident assignedIncident ){
		this.AssignedIncident=assignedIncident;
	}



	//********************************************************************************
	@ Override
	public void update_postion(Coordinate new_position) {

		Coordinate lastPosition = geography.getGeometry(this).getCoordinate();
		double x = (new_position.x - lastPosition.x);
		double y = (new_position.y - lastPosition.y);
		geography.moveByDisplacement(this, x, y);

		if ( Move_responderOrDriver )
			for( Responder_Police Resp : Responders_list)
				geography.moveByDisplacement(Resp, x, y);
		else
			geography.moveByDisplacement(this.AssignedDriver, x, y);

	}


	//********************************************************************************
	public int getIsAvailable()
	{
		if(this.Action== VehicleAction.Idle)
			return 1;
		else
			return 0;
	}

	//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

	public boolean Check_FirstArrivalPolice (  ) 
	{
		boolean result=false;

		if (this.AssignedDriver.assignedIncident.FirstArival_Police ) 
		{result=true;this.AssignedDriver.assignedIncident.FirstArival_Police =false;}	

		return result;
	}

	public boolean Check_FirstArrivalAll (  ) 
	{
		boolean result=false;

		if (this.AssignedDriver.assignedIncident.FirstArival_All  ) 
		{result=true;this.AssignedDriver.assignedIncident.FirstArival_All =false;}	

		return result;
	}

	public void Swich_on_bluelightflashing(  ) 
	{
		this.AssignedDriver.assignedIncident.bluelightflashing_Point =  new PointDestination  ( context, geography ,this. getCurrent_Location() );
		this.AssignedDriver.assignedIncident.bluelightflashing_Node = this.target_node ;	
		this.AssignedDriver.assignedIncident.bluelightflashing_Point.ColorCode= 19 ;
	} 
	//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
	//
	//@ScheduledMethod(start = 1, interval = 1 , priority= 23)
	public void step() {

		CurrentTick=schedule.getTickCount() ;

		if(EndofCurrentAction !=0)
		{
			EndofCurrentAction--;
			if (EndofCurrentAction == 0 )
				EndActionTrigger= VehicleTriggers.EndingAction ;					
		}

		//*************************************************************************
		// ++++++ 1- +++++++
		else if (this.Action== VehicleAction.Idle && ActionEffectTrigger==  VehicleTriggers.InsertKey_incident  )	 //  insert key   Whenever there is a target destination then drive toward  
		{		 

			Action= VehicleAction.Perprating;			
			EndofCurrentAction=  InputFile.perpration_duration_pol + this.perpration_delay;  // I used this temprory I like to put 1 menuts  btween vichels
			ActionEffectTrigger=null;

			//int result = r.nextInt(high-low) + low; This gives you a random number in between low (inclusive) and high (exclusive)
			//EndofCurrentAction= randomizer.nextInt(180 - 120 ) +  120 ; 
		}
		// ++++++ 2- +++++++
		if (Action== VehicleAction.Perprating && EndActionTrigger== VehicleTriggers.EndingAction )	
		{									
			FirstTime=true;
			Move_responderOrDriver=true;
			AssignedDriver.SensingeEnvironmentTrigger=RespondersTriggers.Engineisrunning;
			for (Responder_Police   Resp :Responders_list)	
				Resp.SensingeEnvironmentTrigger=RespondersTriggers.Engineisrunning;			

			Action= VehicleAction.TravelingtoIncident;
			EndActionTrigger=null;			
		}
		//-------------------------------------------------------------------------------------------- 
		// ++++++ 3- +++++++
		else if (Action== VehicleAction.TravelingtoIncident && SensingeEnvironmentTrigger==null )
		{	
			Drive();
		}
		// ++++++ 4- +++++++
		else if (Action== VehicleAction.TravelingtoIncident && SensingeEnvironmentTrigger==VehicleTriggers.Arrivedlocation) 
		{ 			
			InIncidentSite=true;
			FirstArrivalofVehicleAll= this.Check_FirstArrivalAll() ;
			FirstArrivalofVehicleAgency= this.Check_FirstArrivalPolice() ;

			AssignedDriver.SensingeEnvironmentTrigger=RespondersTriggers.ArrivedIncident;
			if (FirstTime && FirstArrivalofVehicleAll)
			{
				for (Responder_Police  Resp :Responders_list)	
					Resp.SensingeEnvironmentTrigger=RespondersTriggers.ArrivedIncident;
				FirstTime=false;
			}


			Action= VehicleAction.WaitingRequest;
			SensingeEnvironmentTrigger=null;					
		}
		//-------------------------------------------------------------------------------------------- 
		// ++++++ 5- +++++++
		else if (Action== VehicleAction.WaitingRequest && ActionEffectTrigger == VehicleTriggers.InsertKey_BLF ) 
		{
			Move_responderOrDriver=true;
			Action= VehicleAction.ColocationBLF;
			ActionEffectTrigger=null;
		}	
		// ++++++ 6- +++++++
		else if (Action== VehicleAction.ColocationBLF  && SensingeEnvironmentTrigger==null )
		{
			Drive();
		}
		// ++++++ 7- +++++++
		else if (Action== VehicleAction.ColocationBLF && SensingeEnvironmentTrigger==VehicleTriggers.Arrivedlocation) 
		{ 
			AssignedDriver.SensingeEnvironmentTrigger=RespondersTriggers.Arrivedbluelightflashing;	

			if (FirstTime )
			{
				for (Responder_Police  Resp :Responders_list)	
					Resp.SensingeEnvironmentTrigger=RespondersTriggers.ArrivedIncident;
				FirstTime=false;
			}

			Action= VehicleAction.WaitingRequest;
			SensingeEnvironmentTrigger=null;
		}
		//-------------------------------------------------------------------------------------------- 
			
		// ++++++ 11- +++++++
		else if (Action== VehicleAction.WaitingRequest && ActionEffectTrigger == VehicleTriggers.InsertKey_IncidentLeave ) 
		{
				
			InIncidentSite=false;
			Move_responderOrDriver=true;
			for (Responder_Police   Resp :Responders_list)	
				Resp.SensingeEnvironmentTrigger=RespondersTriggers.Engineisrunning;

			//go back to station	
			Action= VehicleAction.TravelingtoBaseStation;
			ActionEffectTrigger=null;			
			ComeBackoption = 8 ;
			
			
		}	
		// ++++++ 12- +++++++
		else if (Action== VehicleAction.TravelingtoBaseStation  && SensingeEnvironmentTrigger==null && ComeBackoption ==8 )
		{
			Drive();
		}
		// ++++++ 13- +++++++
		else if (Action== VehicleAction.TravelingtoBaseStation && SensingeEnvironmentTrigger==VehicleTriggers.Arrivedlocation && ComeBackoption ==8 ) 
		{ 
			AssignedDriver.SensingeEnvironmentTrigger=RespondersTriggers.ArrivedPEA ;	

			SensingeEnvironmentTrigger=null;
			ComeBackoption = 0 ;
			//System.out.println(this.Id + " start zzzzzzzzzzzzzzzzzzzzzzzzz1zzzzzzzzzzzzzzzzzzzzzzzzz");
		}

		// ++++++ 20- +++++++
		else if (Action== VehicleAction.TravelingtoBaseStation && ActionEffectTrigger==  VehicleTriggers.InsertKey_Station    )
		{			
			ActionEffectTrigger=null;
			ComeBackoption = 9 ;

		}
		// ++++++ 21- +++++++
		else if (Action== VehicleAction.TravelingtoBaseStation && SensingeEnvironmentTrigger==null && ComeBackoption == 9 ) 
		{
			Drive();
		}
		// ++++++ 22- +++++++
		else if (Action== VehicleAction.TravelingtoBaseStation && SensingeEnvironmentTrigger==VehicleTriggers.Arrivedlocation  && ComeBackoption == 9 ) 
		{ 
			AssignedDriver.SensingeEnvironmentTrigger=RespondersTriggers.ArrivedBaseStation ;
			for (Responder_Police   Resp :Responders_list)	
				Resp.SensingeEnvironmentTrigger=RespondersTriggers.ArrivedBaseStation ;

			Action= VehicleAction.done;
			SensingeEnvironmentTrigger=null;
			Busy=false;
			
			System.out.println(this.Id + " on station bey bey -------------------------------------------------------------------------------------------------------------------------"  );			
			
		}

		
		

	}// end Step *************

}

