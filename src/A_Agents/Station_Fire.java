package A_Agents;

import java.util.ArrayList;
import java.util.List;
import A_Environment.Incident;
import A_Environment.RoadNode;
import A_Roles_Fire.Fire_Driver;
import C_SimulationInput.InputFile;
import D_Ontology.Ontology.Fire_ResponderRole;
import D_Ontology.Ontology.RespondersTriggers;
import D_Ontology.Ontology.StationAction;
import D_Ontology.Ontology.StationTriggers;
import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.gis.Geography;

public class Station_Fire extends Station {

	static  int counterR = 1; //used to give the  unique number
	static  int counterV = 1; //used to give the  unique number
	
	List<Responder_Fire> Responder_Fire_list = new ArrayList<Responder_Fire>();
	List<Vehicle_Fire> Vehicle_Fire_list = new ArrayList<Vehicle_Fire>();
	List<Vehicle_Fire> dispatchlist = new ArrayList<Vehicle_Fire>();

	//##############################################################################################################################################################

	public Station_Fire(String _ID, Context<Object> _context, Geography<Object> _geography, RoadNode _Node,
			String _Nodename_Location, int _availableVehiclesCount, int _availableRespondersCoun) {
		super(_ID, _context, _geography, _Node, _Nodename_Location, _availableVehiclesCount, _availableRespondersCoun);
		this.ColorCode= 2;
		
		InitiateResourse();
	}

	//*****************************************************************************************************
	public void InitiateResourse()
	{
		//int counterR = 0; //used to give the  unique number
		//int counterV = 0; //used to give the  unique number

		for( int i=0 ; i< AvailableVehiclesCount ; i++)
		{


			Vehicle_Fire newVehicle = new Vehicle_Fire(context, geography,this.Location,this.Nodename_Location,this.Node,"VFr_"+counterV++ ,40,(Station_Fire) this);	//ID+"V_Fr_"+counterV++
			Vehicle_Fire_list.add(newVehicle);
			
			System.out.println("PRIORITY_execution =>  "+ InputFile.PRIORITY_execution +"   "  + newVehicle.Id );
			ScheduleParameters params = ScheduleParameters.createRepeating(schedule.getTickCount() + 1 , 1,InputFile.PRIORITY_execution ++ );
			schedule.schedule(params,newVehicle,"step");

		}
		for( int i=0 ; i< AvailableRespondersCount ; i++)
		{


			Responder_Fire newResponder=new Responder_Fire(context, geography,this.Location , "FrRes_"+counterR++ ,null);//ID+"Res_"+counterR++
			Responder_Fire_list.add(newResponder );
			
			System.out.println("PRIORITY_execution =>  "+ InputFile.PRIORITY_execution +"   "  + newResponder.Id );
			ScheduleParameters params = ScheduleParameters.createRepeating(schedule.getTickCount() + 1 , 1,InputFile.PRIORITY_execution ++ );
			schedule.schedule(params,newResponder,"step");

		}
	}



	//##############################################################################################################################################################	
	//													Plan
	//##############################################################################################################################################################	
	public void DispatchResourese( Incident assignedIncident , int DispatchV ,int DispatchAR , boolean double_crewed )
	{

		//	System.out.println("Ambulance : assigen responders to vichels and  intiate responde behavoir of v and re  ");				

		//1- Distribute Responders among Vehicles
		for (int i=1 ; i <= DispatchV ; i++ )
		{	

			Vehicle_Fire V= GetFreeVehicles();
			V.Busy=true;
			V.perpration_delay=  i * InputFile.perpration_delay_fr  ;
			dispatchlist.add(V);

			//-----------------	Driver

			Responder_Fire R1=  GetFreeResponder();
			V.AssigenResponder(R1); // add to car
			R1.ActionEffectTrigger =RespondersTriggers.Alarm999 ;	
			R1.assignedIncident=assignedIncident;
			R1.Busy=true;

			V.AssignedDriver=R1;
			R1.Role= Fire_ResponderRole.Driver;
			R1.CurrentCalssRole1= new Fire_Driver (R1);

			//-----------------	Attendance

			if ( double_crewed )
			{
				Responder_Fire R2=  GetFreeResponder();
				V.AssigenResponder(R2); // add to car
				R2.ActionEffectTrigger =RespondersTriggers.Alarm999 ;	
				R2.assignedIncident=assignedIncident;
				R2.Busy=true;
								
				Responder_Fire R3=  GetFreeResponder();
				V.AssigenResponder(R3); // add to car
				R3.ActionEffectTrigger =RespondersTriggers.Alarm999 ;	
				R3.assignedIncident=assignedIncident;
				R3.Busy=true;
				
				
				Responder_Fire R4=  GetFreeResponder();
				V.AssigenResponder(R4); // add to car
				R4.ActionEffectTrigger =RespondersTriggers.Alarm999 ;	
				R4.assignedIncident=assignedIncident;
				R4.Busy=true;
				
				
				

			}

			//-----------------	 Additional officers go with First car

			if ( DispatchAR  > 0)
			{
				while( DispatchAR !=0 )
				{
					Responder_Fire R=  GetFreeResponder();
					V.AssigenResponder(R); // add to car
					R.ActionEffectTrigger =RespondersTriggers.Alarm999 ;	
					R.assignedIncident=assignedIncident;
					R.Busy=true;
					DispatchAR=DispatchAR-1;
				}

			}

		}// end for

	}

	//*****************************************************************************************************	
	public void CommandSationtoResponse( Incident assignedIncident ,  int _EOC_allocatedVnum ,int _EOC_allocatedRnum , boolean _double_crewed )
	{		

		CommTrigger=StationTriggers.GetDispatchingcommand;		
		newassignedIncident=assignedIncident;

		EOC_allocatedRnum = _EOC_allocatedRnum ;
		EOC_allocatedVnum= _EOC_allocatedVnum;
		EOC_double_crewed = _double_crewed ;



	}

	//##############################################################################################################################################################	
	//													Interpreted Triggers 
	//##############################################################################################################################################################
	private boolean Check_AllResoursesBacktoStation()
	{

		for (int i=0;  i<  this.dispatchlist.size() ;i++ )					
		{	
			Vehicle_Fire V=dispatchlist.get(i) ;
			if ( ! V.Busy )
			{				

				if (V.Responders_list.size()!=0)
				{
					for (int j=0;  j<  V.Responders_list .size() ;j++ )
					{
						Responder_Fire R=V.Responders_list.get(j);
						if ( ! R.Busy )
						{ V.Responders_list.remove(j); j--;}
					}
				}
				else if (V.Responders_list.size()==0)					
				{ dispatchlist.remove(i); i--; }
			}	

		}

		if (dispatchlist.size()==0)
		{System.out.println(this.ID +" thank you god   "); return true;}
		else
		{ return false;}


		//			for(Responder_Ambulance r: v.Responders_list )		
		//				if (r..Busy== false)
		//				{AllResoursesBack= false; }	//break;	

		//			// extra
		//			if ( this.EOC1.InterpretedTrigger==EOCTriggers.EndER    )
		//			{
		//				if(v.Action!=VehicleAction.done)
		//				for(Responder_Ambulance r: v.Responders_list     )
		//						System.out.println(v.Id +"    " +v.Action +"   Errorrrrrrrrrrrrr" + r.Id +" ---  "+ r.Action + " " + r.Role +"   " +r.Prv );
		//			}		


	}

	//______________________________________________________________________________________________________________________________________________________________
	//##############################################################################################################################################################
	//                                                  Behavior
	//##############################################################################################################################################################
	//@ScheduledMethod(start = 1, interval = 1 ,priority= 27)
	public void step() {


		CurrentTick=schedule.getTickCount() ;

		if(EndofCurrentAction !=0)
		{
			EndofCurrentAction--;
			if (EndofCurrentAction == 0 )
				EndActionTrigger= StationTriggers.EndingAction ;					
		}
		//*************************************************************************
		// ++++++ 1- +++++++
		if (CommTrigger==StationTriggers.GetDispatchingcommand	 )
		{

			Action= StationAction.DispatchingResources;		
			EndofCurrentAction=  InputFile.PerparationforResponding_duration ; 
			CommTrigger=null;
			//int result = r.nextInt(high-low) + low; This gives you a random number in between low (inclusive) and high (exclusive)
			//( randomizer.nextInt(11-4)+ 4 )  + 1 ; 
		}
		// ++++++ 2- +++++++
		if (Action== StationAction.DispatchingResources	 && EndActionTrigger== StationTriggers.EndingAction )	
		{			
			DispatchResourese( this.newassignedIncident,  EOC_allocatedVnum, EOC_allocatedRnum ,EOC_double_crewed    );
			//DispatchResourese( this.newassignedIncident, 3 ,  3 ,true);
			//System.out.println(this.ID + " DispatchResourese  to"+  this.newassignedIncident.ID  +" v " + 2  +" r " + 1 );

			Action= StationAction.Noaction;
			EndActionTrigger=null;			
		}
		// ++++++ 3- +++++++
		else if (Action== StationAction.Noaction && Check_AllResoursesBacktoStation()  )
		{

			Action= StationAction.Idle;
			System.out.println(this.ID + " Fire station done  ................................................................. oh oh ohohohohohohoohohoh ");

			//send message to EOC
			//RunEnvironment.getInstance().endRun(); 
		}

	}// end Step *************

	//##############################################################################################################################################################

	public int getAvailableRespondersCount()
	{
		int count=0;
		for ( Responder_Fire   R : Responder_Fire_list   )
			if (R.Busy== false)
				count++;

		return count;

	}

	public Responder_Fire  GetFreeResponder()
	{
		Responder_Fire FR=null;
		for ( Responder_Fire   R : Responder_Fire_list   )
			if (R.Busy== false)
			{ FR= R; break;}

		return FR;

	}

	public void setAvailableRespondersCount(int Count)
	{
		;
	}

	public String getName() {
		return this. Nodename_Location;
	}

	public int getAvailableVehiclesCount()
	{
		int count=0;
		for ( Vehicle_Fire   R : Vehicle_Fire_list   )
			if (R.Busy== false)
				count++;

		return count;
	}

	public Vehicle_Fire GetFreeVehicles()
	{
		Vehicle_Fire FV=null;
		for ( Vehicle_Fire   R : Vehicle_Fire_list   )
			if (R.Busy== false)
			{ FV= R; break;}

		return FV ;
	}

}








