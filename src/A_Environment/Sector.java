package A_Environment;

import java.util.ArrayList;
import java.util.List;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import A_Agents.Responder;
import A_Agents.Responder_Ambulance;
import A_Agents.Responder_Fire;
import C_SimulationInput.InputFile;
import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.space.gis.Geography;


public class Sector {

	ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule(); 

	public int ColorCode =0; //for visualization purpose	

	public  int ID;
	public Coordinate  cor1,cor2;
	public PointDestination         Startpoint, centerofsectorPoint ,Limitpoint ;	
	public Responder                AssignedSC;	

	public  Responder_Ambulance SCcommander ;
	public  Responder_Fire FSCcommander ;
	//----------------------------------------------------------------------
	public Context<Object> context;
	public Geography<Object> geography;
	public boolean installed=false ;
	//----------------------------------------------------------------------

	public int CasualityinThisSector=0 ;  // updated and used by AIC  when walk in organized_way  , these parameters should be in TP
	public  int  CurrentPramdeicResponderinSector=0 ;
	public  int  CurrentFireFighterResponderinSector=0 ;
	//public  int  CurrentFireFighterforSetupinSector=0 ;
	
	public double NeedResponders;
	public double Meet;
	public int ProirityCreation=-1; //based on number of casualties in 

	//----------------------------------------------------------------------
	//used by Firefighter
	public  double EstimatedTimeSetup ; // in second
	public int currentworkersSetup=0 ;
	public ArrayList<Responder> Currentworkers_list=new ArrayList<Responder>();
	public  double StartTime=0 ,FinishTime ;
	public  boolean Done=false;

	//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
	public Sector(int _ID ,Context<Object> _context, Geography<Object> _geography,PointDestination  _Startpoint ,PointDestination _Limitpoint ,  PointDestination B1, PointDestination B2 ,PointDestination _centerPoint , Incident incd ) {

		ID= _ID;		
		Startpoint=_Startpoint;
		Limitpoint =_Limitpoint ;
		centerofsectorPoint=_centerPoint;

		double circumference1= Math.PI * 2* incd.radius_incidentCircle_inner;

		EstimatedTimeSetup= ( circumference1 * InputFile.EstimatedTimeSetup_sector) ;
		
		//--------------------------------------
		context = _context;
		geography = _geography;


		//--------------------------------------		
		GeometryFactory fac = new GeometryFactory();
		// Generate a polygon geometry for each circle pie slice		
		Coordinate[] coords = new Coordinate[5];

		coords[0] =  _Startpoint.getCurrentPosition();
		coords[1] = B1.getCurrentPosition();
		coords[2] =_Limitpoint.getCurrentPosition();
		coords[3] = B2.getCurrentPosition();
		coords[4] =  _Startpoint.getCurrentPosition();

		context.add( this);
		geography.move(this, fac.createPolygon(coords));	

		//		int colorcoode= (_ID-1) * 3 + 1; 
		//		Startpoint.ColorCode=colorcoode;
		//		Limitpoint.ColorCode=colorcoode+1;
		//		centerPoint.ColorCode=colorcoode+2;

		//--------------------------------------

		//	@SuppressWarnings("unchecked") 
		//	List<Casualty> nearObjects_Casualty = (List<Casualty>) BuildStaticFuction.GetObjectsWithinDistance(_Startpoint,Casualty.class, 40 );
		//	GetObjectsWithinSector(nearObjects_Casualty);
	}

	public Sector(int _ID ,Context<Object> _context, Geography<Object> _geography,PointDestination  _Startpoint ,PointDestination _Limitpoint ,PointDestination _centerPoint , Incident incd) {

		ID= _ID;		
		Startpoint=_Startpoint;   //Startpoint.ColorCode= 7 ;
		Limitpoint =_Limitpoint ;  //Limitpoint.ColorCode= 15;
		centerofsectorPoint=_centerPoint; //centerofsectorPoint.ColorCode=13 ;

		double circumference1= Math.PI * 2* incd.radius_incidentCircle_inner;

		EstimatedTimeSetup= ( (circumference1/4) + incd.radius_incidentCircle_inner + incd.radius_incidentCircle_inner ) * InputFile.EstimatedTimeSetup_sector ;
		
		//--------------------------------------
		context = _context;
		geography = _geography;

		//--------------------------------------		
		GeometryFactory fac = new GeometryFactory();	
		Coordinate[] coords = new Coordinate[9];

		coords[0] =  incd.PointL.getCurrentPosition();
		coords[1] = incd.PointLU.getCurrentPosition();
		coords[2] =incd.PointU.getCurrentPosition();
		coords[3] = incd.PointRU.getCurrentPosition();
		coords[4] =  incd.PointR.getCurrentPosition();
		coords[5] =  incd.PointRD.getCurrentPosition();
		coords[6] = incd.PointD.getCurrentPosition();
		coords[7] =incd.PointLD.getCurrentPosition();
		coords[8] = incd.PointL.getCurrentPosition();

		context.add( this);
		geography.move(this, fac.createPolygon(coords));	


		//PointDestination Point =  new PointDestination  ( context, geography, geography.getGeometry(this).getCoordinate() );
		//centerofsectorPoint= Point ;
		//centerofsectorPoint.ColorCode=11;

	}

	//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
	public List<?> GetObjectsWithinSector(List<?> nearObjects){		

		List<Object> objectList = new ArrayList<Object>();		

		// Find all features that intersect the sector feature
		Geometry thisGeom = geography.getGeometry(this);

		for (Object o : nearObjects){
			if (thisGeom.intersects(geography.getGeometry(o))){
				objectList.add(o);
			}
		}

		//System.out.println( "   "+ ID +" " +objectList.size());
		return objectList;
	}

	//****************************************************************************************
	public boolean AmIinSector(Object nearObject){		

		// Find all features that intersect the zone feature
		Geometry thisGeom = geography.getGeometry(this);

		if (thisGeom!=null && nearObject!=null && (     thisGeom.intersects(geography.getGeometry(nearObject)) ||  thisGeom.contains(geography.getGeometry(nearObject)) )       )
		{
			return true;
		}		
		return false;
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

	//****************************************************************************************
	public void setName(int _nothing) {
		ID=_nothing;

	}

	public int getName() {
		return  this.ID ;
	}

	public int getColorCode() {
		return this.ColorCode;
	}

	public void setColorCode(int ColorCode1) {
		this.ColorCode = ColorCode1;
	}

	//****************************************************************************************
	/*
	 * //by using evelop
	 * 
	 * public Envelope Env; Env= new Envelope ( Startpoint.getCurrentPosition()
	 * ,Limitpoint.getCurrentPosition()); public List<?> GetObjectsWithinSector2(
	 * Class clazz){
	 * 
	 * Iterable<?> nearObjects = geography.getObjectsWithin(Env, clazz); List
	 * nearObjectList = new ArrayList();
	 * 
	 * for (Object o : nearObjects){ nearObjectList.add(o);
	 * //System.out.println("dddd" ); }
	 * 
	 * return nearObjectList; }
	 */


	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	// Sector-  General
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	//@ScheduledMethod(start = 1, interval = 1) in Silver commander
	public void step() {

		if (StartTime>0 && !Done )
		{
			EstimatedTimeSetup = EstimatedTimeSetup - currentworkersSetup   ; 

			if ( EstimatedTimeSetup<=0  )
			{
				Done=true ;
				installed =true ;
				//for (Responder   Resp :Currentworkers_list)	
					//Resp.SensingeEnvironmentTrigger=RespondersTriggers.SectorCreated;
												
				//if (ID==1 )  ColorCode=1 ;   
				if (ID==2 ) ColorCode=2  ;  
				if (ID==3 ) ColorCode=3 ;   
				if (ID==4 )ColorCode= 4;   
				

				
			}
		}

	}// end Step *************

}
