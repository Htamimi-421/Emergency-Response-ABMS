package A_Environment;

import java.util.ArrayList;
import javax.measure.unit.SI;
import A_Agents.Casualty;
import A_Agents.Responder;
import A_Agents.Vehicle_Ambulance;
import C_SimulationInput.InputFile;
import D_Ontology.Ontology.TacticalAreaType;
import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.space.gis.Geography;

public class TacticalArea {  // RC, CCS ,LoadingArea // BHA ,  dealing with casualties or Ambulance   ControlArea

	ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule(); 
	public int ColorCode =0; //for visualization purpose	

	public  TacticalAreaType    AreaType ;
	public PointDestination  Location ;
	public TopographicArea TopA ;
	public RoadNode LoadingNode; //used in loading area
	public int capacity= 10;
	public boolean installed= false;
	int incmental;

	public Responder Bronzecommander ;

	//---------------------------------------------------------------
	public ArrayList<PointDestination> TALocations = new ArrayList<PointDestination>();
	public ArrayList<Casualty> casualtiesinTA = new ArrayList<Casualty>();
	public ArrayList<Vehicle_Ambulance> AmbulanceinTA = new ArrayList<Vehicle_Ambulance>(); // not used

	//----------------------------------------------------------------------
	public  int  CurrentPramdeicforSetupinTA=0 ;

	//----------------------------------------------------------------------	
	//used by Responders 
	public  double EstimatedTimeSetup ; // in second
	public int currentworkersSetup=0 ;
	public ArrayList<Responder> Currentworkers_list=new ArrayList<Responder>();
	public  double StartTime=0 ,FinishTime ;
	public  boolean Done=false;

	public Context<Object> context;
	public Geography<Object> geography;


	//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%	

	public TacticalArea ( Context<Object> _context, Geography<Object> _geography ,TopographicArea _TopA ,RoadNode _LoadingNode  ,TacticalAreaType  _AreaType, int _capacity )  // Coordinate _Location 
	{		
		context=_context;
		geography=_geography;

		if (_TopA !=null )  
			Location =  new PointDestination ( _context, _geography, _TopA.getCurrentPosition());	
		else
			Location =  new PointDestination ( _context, _geography, _LoadingNode.getCurrentPosition());	

		AreaType= _AreaType ;
		capacity=  _capacity ;
		TopA=_TopA;
		if(TopA !=null)  TopA.used_byprogram=true;
		if (  _LoadingNode !=null)
		{LoadingNode=_LoadingNode ;  LoadingNode.Name="Loading_A "  ; }  // +assignedIncident.ID;

		if ( AreaType ==    TacticalAreaType.ControlArea )
			EstimatedTimeSetup= 3 * InputFile.EstimatedTimeSetup_TA ;
		else
			EstimatedTimeSetup=  capacity * InputFile.EstimatedTimeSetup_TA ; //other type   

	}
	//****************************************************************************************
	public boolean AssigenCasualtytoTA(Casualty Ca)
	{			
		boolean result= false;

		for(PointDestination bedOrchair :TALocations   )
			if (  bedOrchair.Isoccupied==false )
			{


				if (casualtiesinTA.contains(Ca) )
				{
					
					System.out.println("                                                                                                                                 errror TacticalArea  in adding  casulties    == >>>> "
							+ Ca.ID +"  to "   + AreaType  + "  "+  Ca.addedbyxxxxx.Id + "  "+ Ca.CurrentTickaddedxxxx + "  "+ Ca.Triage_tage + "  "+  Ca.Status + "  "+  Ca.IcanMove );   
					Ca.error=true;
									
				}				
				

					bedOrchair.Isoccupied=true;
					casualtiesinTA.add(Ca);	
					Ca.setLocation_and_move(bedOrchair.getCurrentPosition());
					Ca.bedOrchair=bedOrchair ;
					
					result= true;
					Ca.TimeinCCSorRC=schedule .getTickCount() ;
					Ca.TA=this.AreaType ;
					//System.out.println("                                                                                                                            == >>>> "+
					//Ca.ID  + " in TA   " + AreaType   +"   " +  Ca.TimeinCCSorRC );
				
				
				
				break;
			}	


		return result ;
	}

	public void RemoveCasualtyfromTA(Casualty Ca)
	{			
		casualtiesinTA.remove(Ca);
		Ca.bedOrchair.Isoccupied=false;
		Ca.bedOrchair=null;

		if ( casualtiesinTA.contains(Ca)  )
			System.out.println("errrrrrrrrrrrrrrr in TA rrrrrrrrrrrrrrror TacticalArea  in remving  casulties"+ Ca.ID +"  from "   + AreaType  );                     
	}

	public boolean IsCasualtyinTA(Casualty Ca)
	{			
		boolean result=false;
		for ( Casualty ca : casualtiesinTA) 
			if ( ca==Ca)
			{result=true; break;}		
		return result ;
	}

	public boolean NOMorecasualtyinTAexceptFatality()// No casualty except Fatality
	{			
		boolean result=true;
		for ( Casualty ca : casualtiesinTA) 
			if (  ca.Triage_tage !=5 )
			{result=false; break;}		
		return result ;
	}

	//****************************************************************************************
	public boolean AssigenAmbtoTA(Vehicle_Ambulance Amb)
	{
		boolean result= false;

		for(PointDestination bark :TALocations   )
			if (  bark.Isoccupied==false )
			{
				AmbulanceinTA.add(Amb);
				bark.Isoccupied=true;
				Amb.Move_responderOrDriver=false;
				Amb.update_postion(bark.getCurrentPosition());
				Amb. bark= bark ;
				result= true;
				break;
			}	

		return result ;

	}

	public void RemoveAmbfromTA(Vehicle_Ambulance Amb)
	{			
		AmbulanceinTA.remove(Amb);
		Amb.bark.Isoccupied=false;
		Amb.bark=null;
	}

	//****************************************************************************************
	public void CreatTAlocs(  int ColorCodex )
	{				

		int numcol= (int) Math.floor( this.capacity/8 );

		int disx =0 ,disy =0   ;
		for (  int i=0 ; i< 2; i++) 
		{			
			PointDestination  Point1 =  new PointDestination  ( context, geography, Location.getCurrentPosition() );
			PointDestination  Point2 =  new PointDestination  ( context, geography, Location.getCurrentPosition());
			TALocations.add(Point1);
			TALocations.add(Point2);
			Point1.ColorCode=ColorCodex; 
			Point2.ColorCode=ColorCodex; 
			disx = disx +incmental  ;
			disy =0 ;
			geography.moveByVector(Point1 , disx , SI.METER, 6.2831 ); //e	
			geography.moveByVector(Point2 , disx , SI.METER, 3.14159 );//w


			for (  int j=0 ; j< numcol ; j++) 
			{
				PointDestination  Point1_1=  new PointDestination  ( context, geography, Point1.getCurrentPosition() );
				PointDestination  Point1_2=  new PointDestination  ( context, geography, Point1.getCurrentPosition() );
				PointDestination  Point2_1 =  new PointDestination  ( context, geography, Point2.getCurrentPosition() );
				PointDestination  Point2_2 =  new PointDestination  ( context, geography, Point2.getCurrentPosition() );
				TALocations.add(Point1_1); TALocations.add(Point1_2);
				TALocations.add(Point2_1); TALocations.add(Point2_2);
				Point1_1.ColorCode=ColorCodex; 	Point1_2.ColorCode=ColorCodex; 
				Point2_1.ColorCode=ColorCodex; Point2_2.ColorCode=ColorCodex; 
				disy = disy + incmental  ;

				geography.moveByVector(Point1_1 , disy , SI.METER,  1.5708 ); //n	
				geography.moveByVector(Point1_2 , disy , SI.METER, 4.71239);//s

				geography.moveByVector(Point2_1 , disy , SI.METER,  1.5708 ); //n	
				geography.moveByVector(Point2_2 , disy , SI.METER, 4.71239);//s
			}
		}
		// delete in bulding
		int counter=0 ;
		for ( PointDestination Loc : TALocations)		
			if (  ! Loc.ISvalidLocation_bed_chair_bark() )
				TALocations.remove(Loc) ;
			else
				Loc.id=counter ++;



		while ( TALocations.size() > this.capacity )
		{
			TALocations.remove(TALocations.size()-1) ;
		}

		//System.out.println("******LA*********** "+ TALocations.size()+ "TALocations " );
	}

	//****************************************************************************************
	public void IworkinSetup(Responder workers ){		

		Currentworkers_list.add(workers);		
		currentworkersSetup ++ ;
		if ( StartTime== 0 )
			StartTime=schedule.getTickCount() ;

		//System.out.println("***************** "+ workers.Id + " IworkinSetups  " );
	}

	public void INOTworkinSetup(Responder workers ){		

		Currentworkers_list.remove(workers);		

	}

	//****************************************************************************************
	public int getColorCode() {
		return this.ColorCode;
	}

	public void setColorCode(int ColorCode1) {
		this.ColorCode = ColorCode1;
	}

	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	// TA-  General
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	//@ScheduledMethod(start = 1, interval = 1)  in Silver commander
	public void step() {

		if (StartTime>0 && !Done )
		{
			EstimatedTimeSetup = EstimatedTimeSetup - currentworkersSetup   ; 

			if ( EstimatedTimeSetup<=0  )
			{
				Done=true ;
				installed =true ;

				//for (Responder   Resp :Currentworkers_list)	
				//	Resp.SensingeEnvironmentTrigger=RespondersTriggers.TAestablished;

				switch( AreaType) {
				case ControlArea :
					Location.ColorCode=22 ;
					break;
				case RC :
					incmental= 1;
					CreatTAlocs( 24 ) ;
					//Location.ColorCode=23 ;
					break;
				case CCS :
					incmental= 2;
					CreatTAlocs( 24 ) ;
					//Location.ColorCode=17 ;
					break;
				case  LoadingArea :
					incmental= 4 ;
					CreatTAlocs(25) ;  // cpacity ????? wrong
					//Location.ColorCode=18 ;
					LoadingNode.ColorCode=3 ;
					this.Location.Name=" LoadingArea hhh" ;
					break;}
			}
		}


		if ( Location.loc  != Location.getCurrentPosition()  )  System.out.println("uuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuu " + " error  " );     

	}// end Step *************	


}
