package A_Agents;

import java.util.ArrayList;
import java.util.List;
import A_Environment.Incident;
import A_Environment.RoadNode;
import A_Roles_Police.Police_Driver;
import C_SimulationInput.InputFile;
import D_Ontology.Ontology.Police_ResponderRole;
import D_Ontology.Ontology.RespondersTriggers;
import D_Ontology.Ontology.StationAction;
import D_Ontology.Ontology.StationTriggers;
import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.gis.Geography;


public class Station_Police  extends Station {

	static  int counterR = 1; //used to give the  unique number
	static  int counterV = 1; //used to give the  unique number
	
	// --------------------------------------------------------------
	List<Responder_Police> Responder_Police_list = new ArrayList<Responder_Police>();
	List<Vehicle_Police> Vehicle_Police_list = new ArrayList<Vehicle_Police>();
	List<Vehicle_Police> dispatchlist = new ArrayList<Vehicle_Police>();

	//##############################################################################################################################################################

	public Station_Police(String _ID, Context<Object> _context, Geography<Object> _geography, RoadNode _Node,String _Nodename_Location, int _availableVehiclesCount, int _availableRespondersCoun) {

		super(_ID, _context, _geography, _Node, _Nodename_Location, _availableVehiclesCount, _availableRespondersCoun);
		this.ColorCode= 3;
		
		InitiateResourse();
	}

	//*****************************************************************************************************
	public void InitiateResourse()
	{
		//int counterR = 0; //used to give the  unique number
		//int counterV = 0; //used to give the  unique number

		for( int i=0 ; i< AvailableVehiclesCount ; i++)
		{

			Vehicle_Police newVehicle = new Vehicle_Police(context, geography,this.Location,this.Nodename_Location,this.Node,ID+"VPol_"+counterV++ ,40,(Station_Police) this);	//ID+"V_Pl_"+counterV++
			Vehicle_Police_list.add(newVehicle);
			
			System.out.println("PRIORITY_execution =>  "+ InputFile.PRIORITY_execution +"   "  + newVehicle.Id );
			ScheduleParameters params = ScheduleParameters.createRepeating(schedule.getTickCount() + 1 , 1,InputFile.PRIORITY_execution ++ );
			schedule.schedule(params,newVehicle,"step");

		}
		for( int i=0 ; i< AvailableRespondersCount ; i++)
		{

			Responder_Police newResponder=new Responder_Police(context, geography,this.Location , "PolRes_"+counterR++ ,null);//ID+"Res_"+counterR++
			Responder_Police_list.add(newResponder );
			
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

			Vehicle_Police V= GetFreeVehicles();
			V.Busy=true;
			V.perpration_delay=  i * InputFile.perpration_delay_pol  ;
			dispatchlist.add(V);
			
			
			//-----------------	Driver

			Responder_Police R1=  GetFreeResponder();
			V.AssigenResponder(R1); // add to car
			R1.ActionEffectTrigger =RespondersTriggers.Alarm999 ;	
			R1.assignedIncident=assignedIncident;
			R1.Busy=true;

			V.AssignedDriver=R1;
			R1.Role= Police_ResponderRole.Driver;
			R1.CurrentCalssRole1= new Police_Driver (R1);

			//-----------------	Attendance

			if ( double_crewed )
			{
				Responder_Police R2=  GetFreeResponder();
				V.AssigenResponder(R2); // add to car
				R2.ActionEffectTrigger =RespondersTriggers.Alarm999 ;	
				R2.assignedIncident=assignedIncident;
				R2.Busy=true;
				
				

			}

			//-----------------	 Additional officers go with First car

			if ( DispatchAR  > 0)
			{
				while( DispatchAR !=0 )
				{
					Responder_Police R=  GetFreeResponder();
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
			Vehicle_Police V=dispatchlist.get(i) ;
			if ( ! V.Busy )
			{				

				if (V.Responders_list.size()!=0)
				{
					for (int j=0;  j<  V.Responders_list .size() ;j++ )
					{
						Responder_Police R=V.Responders_list.get(j);
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
	//@ScheduledMethod(start = 1, interval = 1 , priority= 26)
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
			System.out.println(this.ID + " Police station done  ................................................................. oh oh ohohohohohohoohohoh ");

			//send message to EOC
			//RunEnvironment.getInstance().endRun(); 
		}

	}// end Step *************

	//##############################################################################################################################################################

	public int getAvailableRespondersCount()
	{
		int count=0;
		for ( Responder_Police   R : Responder_Police_list   )
			if (R.Busy== false)
				count++;

		return count;

	}

	public Responder_Police  GetFreeResponder()
	{
		Responder_Police FR=null;
		for ( Responder_Police   R : Responder_Police_list   )
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
		for ( Vehicle_Police   R : Vehicle_Police_list   )
			if (R.Busy== false)
				count++;

		return count;
	}

	public Vehicle_Police GetFreeVehicles()
	{
		Vehicle_Police FV=null;
		for ( Vehicle_Police   R : Vehicle_Police_list   )
			if (R.Busy== false)
			{ FV= R; break;}

		return FV ;
	}

}








