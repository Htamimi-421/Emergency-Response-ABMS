package A_Agents;
import java.util.ArrayList;
import java.util.List;
import com.vividsolutions.jts.geom.Coordinate;
import A_Environment.Incident;
import A_Environment.PointDestination;
import A_Environment.RoadNode;
import B_Classes.HospitalRecord;
import C_SimulationInput.InputFile;
import D_Ontology.BuildStaticFuction;
import D_Ontology.Ontology.AmbulanceTracking;
import D_Ontology.Ontology.CasualtyinfromationType;
import D_Ontology.Ontology.RespondersTriggers;
import D_Ontology.Ontology.VehicleAction;
import D_Ontology.Ontology.VehicleTriggers;
import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.gis.Geography;

public class Vehicle_Ambulance extends Vehicle {

	double  CurrentTick , EndofCurrentAction ;	
	ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule(); 

	//-------------------------------------
	public Station_Ambulance AssignedStation=null;
	public boolean FirstTime;
	public boolean onBarkofLA1=false; ;
	public boolean BackorLeave=false;   // back = true  level=false
	public boolean Move_responderOrDriver=true;
	public boolean ALLCasualitiesInRoom=false; //changed by paramedic during loading
	
	int ComeBackoption= 0 ;
	public PointDestination bark ;  //Used only in loading Area

	
	public boolean AcompanywithCasualty1=false;
	public Responder_Ambulance ResponderacompanywithCasualty1= null;
	//--------------------------------------

	public ArrayList<Responder_Ambulance> Responders_list=new ArrayList<Responder_Ambulance>();	
	public ArrayList<Casualty> casualtiesinRoom = new ArrayList<Casualty>();

	//----------------------------------------------------------------------------------------------
	public Vehicle_Ambulance(Context<Object> context, Geography<Object> geography, Coordinate Location,String Nodename_initialLocation,RoadNode _Node , String ID, int speed ,Station_Ambulance _AssignedStation) {

		super.vehicle(context, geography, Location,Nodename_initialLocation,_Node, ID, speed);	
		this.ColorCode= 1;

		Action= VehicleAction.Idle;
		AssignedStation= _AssignedStation;
		FirstTime=true;

		EndActionTrigger=null;
		Rest(true);
	}

	//********************************************************************************
	public void AssigenResponder(Responder_Ambulance Resp  ){

		Responders_list.add(Resp);	
		//Resp.SensingeEnvironmentTrigger=RespondersTriggers.OnVehicle;
		Resp.Myvehicle= this;		
	}

	public void AssigenIncident2x(Incident assignedIncident ){
		this.AssignedIncident=assignedIncident;
	}

	public void AssigenCasualty(Casualty Ca){			
		casualtiesinRoom.add(Ca);
		
	}

	//********************************************************************************
	@ Override
	public void update_postion(Coordinate new_position) {

		Coordinate lastPosition = geography.getGeometry(this).getCoordinate();
		double x = (new_position.x - lastPosition.x);
		double y = (new_position.y - lastPosition.y);
		geography.moveByDisplacement(this, x, y);

		if ( Move_responderOrDriver )
			for( Responder_Ambulance Resp : Responders_list)
				geography.moveByDisplacement(Resp, x, y);
		else
			geography.moveByDisplacement(this.AssignedDriver, x, y);

		if (casualtiesinRoom.size()>0 )
			for( Casualty  ca : casualtiesinRoom)
				geography.moveByDisplacement(ca, x, y);
		//ca.setLocation_and_move(new_position);	
		
		if ( AcompanywithCasualty1  )
			{
				Coordinate  OldPosition= ResponderacompanywithCasualty1.Return_CurrentLocation();
				geography.moveByDisplacement(ResponderacompanywithCasualty1, x, y);
				
				// Update Search Zones Position As Well	
				ResponderacompanywithCasualty1.Move_related_zones( OldPosition , ResponderacompanywithCasualty1.Return_CurrentLocation());
			}
		
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

	public boolean Check_FirstArrivalAmbulance (  ) 
	{
		boolean result=false;

		if (this.AssignedDriver.assignedIncident.FirstArival_Ambulance )
		{result=true;this.AssignedDriver.assignedIncident.FirstArival_Ambulance =false;}	

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
	//@ScheduledMethod(start = 1, interval = 1 , priority= 25)
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
			EndofCurrentAction=  InputFile.perpration_duration_amb + this.perpration_delay   ;   
			ActionEffectTrigger=null;

			//int result = r.nextInt(high-low) + low; This gives you a random number in between low (inclusive) and high (exclusive)
			//EndofCurrentAction= randomizer.nextInt(180 - 60 ) +  60 ; 
			
		
		}
		// ++++++ 2- +++++++
		if (Action== VehicleAction.Perprating && EndActionTrigger== VehicleTriggers.EndingAction )	
		{									
			FirstTime=true;
			Move_responderOrDriver=true;

			AssignedDriver.SensingeEnvironmentTrigger=RespondersTriggers.Engineisrunning;
			for (Responder_Ambulance   Resp :Responders_list)	
				Resp.SensingeEnvironmentTrigger=RespondersTriggers.Engineisrunning;			

			Action= VehicleAction.TravelingtoIncident;
			EndActionTrigger=null;	
			ComeBackoption = 1 ;
		}
		// ++++++ 3- +++++++
		else if (Action== VehicleAction.TravelingtoIncident && SensingeEnvironmentTrigger==null  && ComeBackoption == 1)
		{	
			Drive();
		}
		// ++++++ 4- +++++++
		else if (Action== VehicleAction.TravelingtoIncident && SensingeEnvironmentTrigger==VehicleTriggers.Arrivedlocation  && ComeBackoption == 1) 
		{ 			
			//InIncidentSite=true;
			FirstArrivalofVehicleAll= this.Check_FirstArrivalAll() ;
			FirstArrivalofVehicleAgency= this.Check_FirstArrivalAmbulance() ;

			AssignedDriver.SensingeEnvironmentTrigger=RespondersTriggers.ArrivedIncident;

			if (FirstTime && FirstArrivalofVehicleAll )
			{
				for (Responder_Ambulance   Resp :Responders_list)	
					Resp.SensingeEnvironmentTrigger=RespondersTriggers.ArrivedIncident;
				FirstTime=false;
			}
			//second time to collect casualties

			Action= VehicleAction.WaitingRequest;
			SensingeEnvironmentTrigger=null;
			ComeBackoption = 0 ;
		}
		//-------------------------------------------------------------------------------------------- 
		// ++++++ 5- +++++++
		else if (Action== VehicleAction.WaitingRequest && ActionEffectTrigger == VehicleTriggers.InsertKey_BLF )  
		{
			Move_responderOrDriver=true;

			Action= VehicleAction.ColocationBLF;
			ActionEffectTrigger=null;
			ComeBackoption = 2 ;
		}	
		// ++++++ 6- +++++++
		else if (Action== VehicleAction.ColocationBLF  && SensingeEnvironmentTrigger==null  && ComeBackoption == 2)
		{
			Drive();
		}
		// ++++++ 7- +++++++
		else if (Action== VehicleAction.ColocationBLF && SensingeEnvironmentTrigger==VehicleTriggers.Arrivedlocation && ComeBackoption == 2) 
		{ 
			AssignedDriver.SensingeEnvironmentTrigger=RespondersTriggers.Arrivedbluelightflashing;	

			if (FirstTime  )
			{
				for (Responder_Ambulance   Resp :Responders_list)	
					Resp.SensingeEnvironmentTrigger=RespondersTriggers.ArrivedIncident;
				FirstTime=false;
			}


			Action= VehicleAction.WaitingRequest;
			SensingeEnvironmentTrigger=null;
			ComeBackoption = 0 ;
		}
		//-------------------------------------------------------------------------------------------- 
		// ++++++ 8- +++++++
		else if (Action== VehicleAction.WaitingRequest && ActionEffectTrigger == VehicleTriggers.InsertKey_LAsetup ) 
		{
			Move_responderOrDriver=false;

			Action= VehicleAction.ColocationBLF;
			ActionEffectTrigger=null;
			ComeBackoption = 3 ;
		}	
		// ++++++ 9- +++++++
		else if (Action== VehicleAction.ColocationBLF  && SensingeEnvironmentTrigger==null && ComeBackoption == 3  )
		{
			Drive();
		}
		// ++++++ 10- +++++++
		else if (Action== VehicleAction.ColocationBLF && SensingeEnvironmentTrigger==VehicleTriggers.Arrivedlocation && ComeBackoption ==3 ) 
		{ 
			AssignedDriver.SensingeEnvironmentTrigger=RespondersTriggers.ArrivedLA;	

			Action= VehicleAction.WaitingRequest;
			SensingeEnvironmentTrigger=null;
			ComeBackoption = 0 ;
			onBarkofLA1=true;
		}

		//-------------------------------------------------------------------------------------------- 
		// ++++++ 11- +++++++
		else if (Action== VehicleAction.WaitingRequest && ActionEffectTrigger == VehicleTriggers.InsertKey_LAleave ) // GoHospital updated by driver
		{
			Move_responderOrDriver=false;

			Action= VehicleAction.TravelingtoHospital;
			ActionEffectTrigger=null;
			ComeBackoption = 4 ;
			onBarkofLA1=false;
			BackorLeave=false;
		}	
		// ++++++ 12- +++++++
		else if (Action== VehicleAction.TravelingtoHospital  && SensingeEnvironmentTrigger==null && ComeBackoption ==4 )
		{
			Drive();
		}
		// ++++++ 13- +++++++
		else if (Action== VehicleAction.TravelingtoHospital && SensingeEnvironmentTrigger==VehicleTriggers.Arrivedlocation && ComeBackoption ==4 ) 
		{ 
			AssignedDriver.SensingeEnvironmentTrigger=RespondersTriggers.ArrivedPEA ;	

			Action= VehicleAction.TravelingtoHospital;
			SensingeEnvironmentTrigger=null;
			ComeBackoption = 0 ;
		}

		// ++++++ 11- +++++++
		else if (Action== VehicleAction.TravelingtoHospital && ActionEffectTrigger == VehicleTriggers.InsertKey_Hospital ) // GoHospital updated by driver
		{
			Move_responderOrDriver=false;

			Action= VehicleAction.TravelingtoHospital;
			ActionEffectTrigger=null;
			ComeBackoption = 5 ;
		}	
		// ++++++ 12- +++++++
		else if (Action== VehicleAction.TravelingtoHospital  && SensingeEnvironmentTrigger==null && ComeBackoption ==5 )
		{
			Drive();
		}
		// ++++++ 13- +++++++
		else if (Action== VehicleAction.TravelingtoHospital && SensingeEnvironmentTrigger==VehicleTriggers.Arrivedlocation && ComeBackoption ==5 ) 
		{ 
			AssignedDriver.SensingeEnvironmentTrigger=RespondersTriggers.ArrivedHospital;	

			Action= VehicleAction.WaitingRequest;
			SensingeEnvironmentTrigger=null;
			ComeBackoption = 0 ;
		}
		//-------------------------------------------------------------------------------------------- 
		// ++++++ 14- +++++++
		else if (Action== VehicleAction.WaitingRequest    && ActionEffectTrigger==  VehicleTriggers.InsertKey_incident ) // go back to incident from hospital 	
		{	

			Move_responderOrDriver=false;

			Action= VehicleAction.TravelingtoIncident;
			ActionEffectTrigger=null;
			ComeBackoption = 6 ;
			BackorLeave=true;

		}
		// ++++++ 15- +++++++
		else if (Action== VehicleAction.TravelingtoIncident  && SensingeEnvironmentTrigger==null && ComeBackoption == 6 )
		{
			Drive();
		}
		// ++++++ 16- +++++++
		else if (Action== VehicleAction.TravelingtoIncident && SensingeEnvironmentTrigger==VehicleTriggers.Arrivedlocation && ComeBackoption == 6 ) 
		{ 
			AssignedDriver.SensingeEnvironmentTrigger=RespondersTriggers.ArrivedIncident ;	

			Action= VehicleAction.TravelingtoIncident;
			SensingeEnvironmentTrigger=null;
			ComeBackoption = 0 ;
		}

		// ++++++ 17- +++++++
		else if (Action== VehicleAction.TravelingtoIncident && ActionEffectTrigger == VehicleTriggers.InsertKey_LABack ) 
		{
			Move_responderOrDriver=false;

			Action= VehicleAction.TravelingtoIncident;
			ActionEffectTrigger=null;
			ComeBackoption = 7 ;
		}	
		// ++++++ 18- +++++++
		else if (Action== VehicleAction.TravelingtoIncident && SensingeEnvironmentTrigger==null && ComeBackoption ==7 )
		{
			Drive();
		}
		// ++++++ 19- +++++++
		else if (Action== VehicleAction.TravelingtoIncident && SensingeEnvironmentTrigger==VehicleTriggers.Arrivedlocation && ComeBackoption ==7) 
		{ 
			AssignedDriver.SensingeEnvironmentTrigger=RespondersTriggers.ArrivedLA ;	

			Action= VehicleAction.WaitingRequest;
			SensingeEnvironmentTrigger=null;
			ComeBackoption = 0 ;
			
			onBarkofLA1=true;
			
			
			//Here
			AcompanywithCasualty1=false;
			ResponderacompanywithCasualty1.SensingeEnvironmentTrigger=RespondersTriggers.ArrivedIncidentBackfromHosp ;
			ResponderacompanywithCasualty1=null;
	
		}
		//-------------------------------------------------------------------------------------------- 
		// ++++++ 11- +++++++
		else if (Action== VehicleAction.WaitingRequest && ActionEffectTrigger == VehicleTriggers.InsertKey_IncidentLeave ) 
		{
			Move_responderOrDriver=true;
			for (Responder_Ambulance   Resp :Responders_list)	
				Resp.SensingeEnvironmentTrigger=RespondersTriggers.Engineisrunning;

			//go back to station	
			Action= VehicleAction.TravelingtoBaseStation;
			ActionEffectTrigger=null;
			ComeBackoption = 8 ;
			onBarkofLA1=false;
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
			for (Responder_Ambulance   Resp :Responders_list)	
				Resp.SensingeEnvironmentTrigger=RespondersTriggers.ArrivedBaseStation ;

			Action= VehicleAction.done;
			SensingeEnvironmentTrigger=null;
			Busy=false;
			System.out.println(this.Id + " on station bey bey -------------------------------------------------------------------------------------------------------------------------"  );			
		}

	}// end Step *************

}
