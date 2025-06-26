package A_Environment;
import java.util.ArrayList;
import java.util.List;
import com.vividsolutions.jts.geom.Coordinate;
import A_Agents.Responder;
import A_Agents.Responder_Fire;
import A_Agents.Responder_Police;
import A_Agents.Vehicle;
import C_SimulationInput.InputFile;
import D_Ontology.BuildStaticFuction;
import D_Ontology.Ontology.CordonType;
import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.space.gis.Geography;

public class Cordon {

	ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule(); 

	public int ColorCode =0; //for visualization purpose	

	//----------------------------------------------------------------------
	public CordonType CordonType;  //inner or outer
	public int NoofStand;
	public double circumference1 ;
	public PointDestination EnteryPointAccess_Point  ;
	public PointDestination Center_Point ;
	public RoadNode EnteryPointAccess_node ;
	public boolean installed= false;
	public Incident assignedIncident ;
	
	public  Responder_Police CCcommander ; //clear route
	public  Responder_Fire FSCcommander ; //clear route

	//----------------------------------------------------------------------

	public ArrayList<PointDestination> StandCordon = new ArrayList<PointDestination>();

	public List<Responder> EnteryQueueInner  = new ArrayList<Responder>(); //  not yet used to log all responders
	public List<Vehicle> EnteryQueueOuter  = new ArrayList<Vehicle>(); // not yet used to log all responders

	//----------------------------------------------------------------------
	//used by Policeman
	public  double EstimatedTimeSetup ; // in second
	public int currentworkersSetup=0 ;
	public ArrayList<Responder> Currentworkers_list=new ArrayList<Responder>();
	public  double StartTime=0 ,FinishTime ;
	public  boolean Done=false;

	//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
	public Cordon(Context<Object> _context, Geography<Object> _geography, Coordinate Centerlocation,RoadNode _EnteryPointAccess_node  , CordonType _CordonType ,  double radius_incidentCircle_innerorOuter ,Incident _assignedIncident )
	{
		CordonType=_CordonType ;

		EnteryPointAccess_node =_EnteryPointAccess_node;
		EnteryPointAccess_node.Name="PEA of " +_assignedIncident.ID;
		EnteryPointAccess_Point =new PointDestination ( _context, _geography, _EnteryPointAccess_node.getCurrentPosition());
		Center_Point =new PointDestination ( _context, _geography, Centerlocation );
		//Center_Point.ColorCode= 3;
	 assignedIncident=_assignedIncident ;
		if (  CordonType== CordonType.inner  )
		{
			circumference1= Math.PI * 2* radius_incidentCircle_innerorOuter ;
			NoofStand =  (int) (circumference1/15) ;  // every 10 meters set stand
			
			CordonsOfIncident_Innerboundery(_context, _geography ,Centerlocation  , radius_incidentCircle_innerorOuter  );
		}
		else 
		{
			circumference1= Math.PI * 2* radius_incidentCircle_innerorOuter ;
			NoofStand =  (int) (circumference1/20) ;  // every 20 meters set stand
			
			EstimatedTimeSetup= ( circumference1 * InputFile.EstimatedTimeSetup_Cordon ) +  30 ; // for EAP
			
			CordonsOfIncident_Outerboundery(_context, _geography ,Centerlocation  , radius_incidentCircle_innerorOuter  );
		}
		
	}		

	//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
	public void CordonsOfIncident_Outerboundery(Context<Object> _context, Geography<Object> _geography ,Coordinate location   ,double radius_incidentCircle_innerorOuter   )
	{
		double Angleincrment;
		double  distance= radius_incidentCircle_innerorOuter   ;

		Angleincrment= (2 * Math.PI) /NoofStand ;

		for(int i=0 ; i <NoofStand ; i++)
		{
			PointDestination Point =  new PointDestination  ( _context, _geography,location );			
			Point.ISvalidLocationPoint1(   location  ,Angleincrment * i ,distance,false) ;
			//Point.ColorCode=16;
			Point.id=i ;
			StandCordon.add( Point );
		}

	}

	//****************************************************************************************
	//not used
	public void CordonsOfIncident_Innerboundery(Context<Object> _context, Geography<Object> _geography ,Coordinate location   ,double radius_incidentCircle_innerorOuter   )
	{
		double Angleincrment;
		double  distance= radius_incidentCircle_innerorOuter   ;

		Angleincrment= (2 * Math.PI) /NoofStand ;

		for(int i=0 ; i <NoofStand ; i++)
		{
			PointDestination Point =  new PointDestination  ( _context, _geography,location );			
			Point.ISvalidLocationPoint2(   location  ,Angleincrment * i ,distance,true) ;
			Point.ColorCode=16;
			Point.id=i ;
			StandCordon.add( Point );
		}

	}

	//****************************************************************************************
	public PointDestination NearstCordon_from_Outercordons (Geography<Object> geography , Coordinate fromthislocation) 
	{
		PointDestination Nearstcordon=null;
		double dis, mindis=999999;

		for (PointDestination co :StandCordon)
		{
			dis= BuildStaticFuction.DistanceC(geography, co.getCurrentPosition(),  fromthislocation);

			if(dis<mindis )
			{ Nearstcordon=co;mindis =dis; }			 
		}
		return Nearstcordon;
	}

	//****************************************************************************************
	public void ArrivedEnteryPointAccessofCordon(Responder Rs )
	{			
		EnteryQueueInner.add(Rs);
	}

	public void leaveEnteryPointAccessofCordon(Responder Rs)
	{			
		EnteryQueueInner.remove(Rs);
	}

	public void MangeEnteryQueueInner()
	{
		//if (EnteryQueueInner.get(0)= null)
		;
	}

	//****************************************************************************************
	public void IworkinSetup(Responder workers ){		

		Currentworkers_list.add(workers);		
		currentworkersSetup ++ ;
		if ( StartTime== 0 )
			StartTime=schedule.getTickCount() ;
	}

	public void INOTworkinSetup(Responder workers ){		

		Currentworkers_list.remove(workers);		
	}

	//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%	
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	// Cordon-  General
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	//	@ScheduledMethod(start = 1, interval = 1)  in Silver commander
	public void step() {

		if (StartTime>0 && !Done )
		{
			EstimatedTimeSetup = EstimatedTimeSetup - currentworkersSetup   ; 

			if ( EstimatedTimeSetup<=0  )
			{
				Done=true ;
				installed =true ;
						
				assignedIncident.Cut_Netcordon_setp2(); // to avoild any acess expcept from PEA
				
				for (PointDestination Stand :  StandCordon)
					Stand .ColorCode=16;
				EnteryPointAccess_Point.ColorCode= 20;
			}

		}

	}// end Step *************

}

//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%	


//public void CordonsOfIncident_bounderyOfOuterCordon_old(Context<Object> _context, Geography<Object> _geography ,Coordinate location  ,int radius_incidentCircle_innerorOuter   )
//{
//	double Angleincrment;
//	double  distance= radius_incidentCircle_innerorOuter  + 5 ;
//
//	NoofStand=8 ;
//	boolean cloockDir=true;
//
//	Angleincrment= (2 * Math.PI) /NoofStand ;
//
//	for(int i=0 ; i < NoofStand ; i++)
//	{
//		PointDestination Point =  new PointDestination  ( _context, _geography, location );			
//
//		if (i==0) 
//		{
//			Point.Name= "Right";	
//			Point.ISvalidLocationPointLRUDatStreet(   location  , Angleincrment * i  ,distance,cloockDir) ;
//		}
//		else if (i==2)
//		{
//			Point.Name= "North";
//			Point.ISvalidLocationPointLRUDatStreet(   location  , Angleincrment * i  ,distance,cloockDir) ;
//		}
//		else if (i==4)
//		{
//			Point.Name= "Left";
//			Point.ISvalidLocationPointLRUDatStreet(   location  , Angleincrment * i  ,distance - 5 ,cloockDir) ;
//			//Point.ISvalidLocationPoint2(   this.location  ,Angleincrment * i ,distance,false) ; // not in building 
//
//		}
//		else if(i==6)
//		{
//			Point.Name= "South";
//			Point.ISvalidLocationPointLRUDatStreet(   location  , Angleincrment * i  ,distance,cloockDir) ;
//		}
//		else
//		{
//			Point.ISvalidLocationPoint2(   location  ,Angleincrment * i ,distance,false) ; // not in building 
//		}
//
//		//Point.IsAssignedToResponder=false;
//		//Point.InstalledCordon=false;
//		Point.ColorCode=1;
//		Point.ColorCode=0;
//		Point.type=CordonType.outer;
//		Point.id=i;
//		StandCordon.add( Point );
//	}	
//
//}

