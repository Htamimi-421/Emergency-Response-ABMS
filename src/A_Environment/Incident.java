package A_Environment;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.measure.unit.SI;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;
import D_Ontology.BuildStaticFuction;
import A_Agents.Casualty;
import A_Agents.EOC;
import A_Agents.Responder;
import A_Agents.Responder_Ambulance;
import A_Agents.Responder_Fire;
import A_Agents.Responder_Police;
import A_Agents.Vehicle;
import A_Agents.Vehicle_Ambulance;
import B_Classes.Duration_info;
import B_Classes.HospitalRecord;
import B_Communication.ISRecord;
import B_Communication.chanle;
import C_SimulationInput.InputFile;
import D_Ontology.Ontology;
import D_Ontology.Ontology.AmbulanceTracking;
import D_Ontology.Ontology.Ambulance_ResponderRole;
import D_Ontology.Ontology.CasualtyReportandTrackingMechanism;
import D_Ontology.Ontology.CommunicationMechanism;
import D_Ontology.Ontology.Communication_Time;
import D_Ontology.Ontology.CordonType;
import D_Ontology.Ontology.GeolocationMechanism;
import D_Ontology.Ontology.Inter_Communication_Structure;
import D_Ontology.Ontology.Level;
import D_Ontology.Ontology.ResourceAllocation_Strategy;
import D_Ontology.Ontology.RespondersBehaviourTypes1;
import D_Ontology.Ontology.RespondersBehaviourTypes2;
import D_Ontology.Ontology.RespondersBehaviourTypes3;
import D_Ontology.Ontology.STartEvacuationtoHospital_Startgy;
import D_Ontology.Ontology.SurveyScene_SiteExploration_Mechanism;
import D_Ontology.Ontology.TacticalAreaType;
import D_Ontology.Ontology.TaskAllocationApproach;
import D_Ontology.Ontology.TaskAllocationMechanism;
import D_Ontology.Ontology.VehicleAction;
import bsh.This;
import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.gis.Geography;
import repast.simphony.space.graph.Network;
import repast.simphony.space.graph.RepastEdge;


public class Incident {

	ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule(); 	
	public double	 Time_ocurance ;

	//-----------------------------------------	
	static int CasualtyCounter  ; //used to give the casualty unique number
	static Random randomizer= new Random();

	//--------------------------------------------------------
	//Setup of scenario
	public boolean  Randomly_Location = false;	
	public boolean Randomly_Severity=false ;	
	public STartEvacuationtoHospital_Startgy  _STartEvacuationtoHospital_Startgy=null ;

	public	SurveyScene_SiteExploration_Mechanism Survey_scene_Mechanism_IN = null;	
	public	TaskAllocationApproach   TaskApproach_IN=null;
	public  TaskAllocationMechanism TaskMechanism_IN=null;
	public	int NOSector=0; //if there is sectorization	

	//Inter_Communication
	public  Inter_Communication_Structure Communication_Structure_uesd=null ;
	public	Communication_Time	Inter_Communication_Time_used=null;  // will be in trigger
	public  double SMEvery= 0;    // will be in trigger
	public  double UpdatOPEvery=0 ;// will be in trigger
	public 	CommunicationMechanism	Inter_Communication_Tool_used=null;
	public GeolocationMechanism   GeolocationTacticalAreas = null;
	public AmbulanceTracking AmbulanceTracking_Strategy=null ;

	//Intra_Communication	
	public	CommunicationMechanism  ComMechanism_level_BtoRes=null;
	public	CommunicationMechanism  ComMechanism_level_StoB=null;
	public	Communication_Time	Intra_Communication_Time_used=null;  // will be in trigger  StoB
	public	CommunicationMechanism  ComMechanism_inControlArea=null;
	public	CommunicationMechanism  ComMechanism_level_toEOC=null;
	public	CommunicationMechanism  ComMechanism_level_DrivertoALC=null;
	public	CasualtyReportandTrackingMechanism CasualtyReport_Mechanism_IN = null;	

	//-------------------------------------------------------
	// Used by EOC during response 
	public boolean	Active22=true;// used by EOC
	public boolean	ER_Police=false, ER_Fire=false , ER_Ambulance=false ; // used by EOC
	public double	Time_startResponde_Getcall=0 ,Time_endResponde_lastcsaualty=0 ;
	public double	Time_endResponde_FirstvehiclearrivedIncd=0 , Time_endResponde_lastvehicleLeaveIncd=0  , Time_endResponde_lastvehiclearrivedBS=0; ;    // used by EOC	
	public double    SB=0;
	public ArrayList<Duration_info> ComInts = new ArrayList<Duration_info>();

	public ResourceAllocation_Strategy Status=null ;  //used by EOC
	public int Amballocated=0,ARallocated=0 ;  //used by EOC
	public int Firallocated=0,FRallocated=0 ;  //used by EOC
	public int Polallocated=0,PRallocated=0 ;  //used by EOC

	//-----------------------------------------
	public String ID;	
	public double radius_incidentCircle;//Radius of the incident circle area in degree lat/lon	
	public double radius_incidentCircle_inner , radius_incidentCircle_outer, radius_incidentCircle_LRUD ;

	public int causaltiesCount , casualty_count_p1,casualty_count_p2,casualty_count_p3  ;
	public int causaltiesCountUN;
	int cusaltiesCountTrapped;
	public ArrayList<Casualty> casualties =new ArrayList<Casualty>();	

	// --------------------------------------------------------------
	public String Nodename_Location ;
	public RoadNode BasicNode ; //of parking
	public ArrayList<RoadNode > ParkingNodes =new ArrayList<RoadNode >();  // All node around incdent site
	public ArrayList<RoadNode >SerachNodes =new ArrayList<RoadNode >();  // All node around incdent site  used during serach

	Coordinate location;
	public Context<Object> context;
	public Geography<Object> geography;
	public TopographicArea TopographArea=null;

	//----------------------------------------------------------------
	// Structure of scene	
	public PointDestination PointCenter , PointL, PointR ,PointU ,PointD ; 
	public PointDestination  PointLL, PointRR ,PointUU ,PointDD ;
	public PointDestination  PointLU, PointLD ,PointRU ,PointRD; 
	TopographicArea buldingofcasualty=null; 

	public PointDestination bluelightflashing_Point=null; // for movement of responders
	public RoadNode bluelightflashing_Node=null;			// for movement of vehicle 
	//----------------------------------------------------------------
	//Tactical Areas ..... will be set after established  
	public Cordon Cordon_inner=null ,Cordon_outer=null ;
	public ArrayList<Sector> Sectorlist2 = new ArrayList<Sector>();
	public TacticalArea  RC=null , CCStation=null ,  loadingArea=null, ControlArea=null ,BHAxx=null  ;  
	public ArrayList<PointDestination > SafeLocations =new ArrayList<PointDestination >();
	public PointDestination  collection_point;
	//---------------------------------------------------------
	public boolean FirstArival_All=true;
	public boolean FirstArival_Ambulance=true;
	public boolean FirstArival_Fire=true;
	public boolean FirstArival_Police=true;
	public boolean StartAmbEvacuation=false;

	public Responder_Ambulance AICcommander=null , ALCcommander=null ,ACOcommander=null ,CCO_ambcommander=null  ;
	public Responder_Ambulance AmSCcommander1=null; public boolean AmSCcommander1_ENDSR_INNER=false;// temp
	public Responder_Fire FICcommander=null ,  FCOcommander=null,FRFSCcommander=null;// for route  
	public Responder_Fire FRSCcommander1=null ; // temp 
	public Responder_Police PICcommander=null , O_CCcommander=null,RCOcommander=null, PCOcommander=null;
	public boolean  BroadCast_CASU_InstalledRC=false ,BroadCast_CASU_InstalledCCS=false;

	public EOC EOC1;

	public List<Responder_Police> xxx  ; // temp for trace
	int counter2=0;

	//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
	//Temp soultion after Last meeting with Phill
	public ArrayList<RoadLink> nearObjects_RLwithTrandWR_inside=  new ArrayList<RoadLink>();
	public ArrayList<RoadLink> nearObjects_RLwithTrandWR_whole=  new ArrayList<RoadLink>();
	
	public ArrayList<TopographicArea> nearObjects_Build_small=  new ArrayList<TopographicArea>();
	public ArrayList<TopographicArea> nearObjects_Build_big=  new ArrayList<TopographicArea>();

	public void Building ()
	{
		//Temp soultion after Last meeting with Phill		
		@SuppressWarnings("unchecked") 
		List<TopographicArea> nearObjects1 = (List<TopographicArea>) BuildStaticFuction.GetObjectsWithinDistance(this,TopographicArea.class, 500);

		for (int i = 0; i < nearObjects1.size(); i++) {
			if (nearObjects1.get(i).getcode() == 4) {
				nearObjects_Build_big.add(nearObjects1.get(i));			
			}
		}
		
		nearObjects1.clear();

		@SuppressWarnings("unchecked") 
		List<TopographicArea> nearObjects2 = (List<TopographicArea>) BuildStaticFuction.GetObjectsWithinDistance(this,TopographicArea.class, 300);

		for (int i1 = 0; i1 < nearObjects2.size(); i1++) {
			if (nearObjects2.get(i1).getcode() == 4) {
				nearObjects_Build_small.add(nearObjects2.get(i1));			
			}
		}

		nearObjects2.clear();

		System.out.println( "xxxxxxxxxxxxxxxxxxxxsxxxxxxxxxxxxxxxxxxxx " + nearObjects_Build_small.size() +" xxxxxxxbxxxxxxx " + nearObjects_Build_big.size() );


	}
	//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
	public Incident(String _ID ,Context<Object> _context, Geography<Object> _geography,String _Nodename_Location, String TAname_Location ,int _causaltiesCount, int _causaltiesCountUN,int _cusaltiesCountTrapped, int _radius_incidentCircle)
	{
		context=_context;
		geography=_geography;

		ID=_ID;
		CasualtyCounter = 0 ;
		radius_incidentCircle= _radius_incidentCircle ;		
		Nodename_Location = _Nodename_Location;
		BasicNode = BuildStaticFuction.GetRoadNodeofthisLocation(geography,Nodename_Location);
		BasicNode.Name=ID;
		//BasicNode.ColorCode= 1;
		TopographArea= BuildStaticFuction.GetTAofthisLocation(geography,TAname_Location );

		location= TopographArea.getCurrentPosition();	
		causaltiesCount =_causaltiesCount;
		causaltiesCountUN=_causaltiesCountUN;
		cusaltiesCountTrapped=_cusaltiesCountTrapped;
		Time_ocurance= schedule .getTickCount();

		//----------------------------------
		GeometryFactory fac = new GeometryFactory();
		context.add(this);
		geography.move(this , fac.createPoint(location));	

		//-----------------------------------------------------------------------------------------
		radius_incidentCircle_LRUD=  this.radius_incidentCircle ;  //- 3
		radius_incidentCircle_inner= this.radius_incidentCircle  ;  //+ 5
		radius_incidentCircle_outer= this.radius_incidentCircle+ 35 ;	

		//----------------------------------
		//RadioSystem
		for( int i= 0 ; i< InputFile.RadioSystemchanleLimitation ;i++)				
		{
			chanle ch=new chanle("CH"+i) ;
			Communication_RadioSystem.add(ch); 
		}
		//-----------------------------------
		

	}

	//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
	// Create incident 
	public void Onset() {

		System.out.println("Create incident" );		

		LRUDofIncident( );

		IdentifyParkingNodes() ; // Or it could be selected as list before run from map QGIS   CenterNode.ColorCode= 1;

		//IdentifySafelocations() ;  //not used 

		OnsetWreckages( );

		OnsetTraffic( );

		SetColorWreckageandTraffic() ;
		
		 Create_Netcordon_setp1();

		OnsetCasualtyRPM(this.causaltiesCount ) ;

		OnsetTrappedCasualty(cusaltiesCountTrapped );

		OnsetUninjuredCasualty( causaltiesCountUN );

	}

	//****************************************************************************************************
	public RoadNode  ParkingNodesforIncdent()	 //or  PEA
	{
		//Generate random int value from 1 to 4
		//int  ind= randomizer.nextInt(ParkingNodes.size())  ;		
		return ParkingNodes.get(0); 

	}

	//****************************************************************************************************
	public void  UpdateParkingNodesforIncdent(RoadNode PEA)	 //or  PEA
	{		
		ParkingNodes.clear(); 
		ParkingNodes.add(PEA);					
	}

	//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
	//                                                                  casualties
	//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
	public void DistrbutionOfCasualty( int causaltiesCount1) {

		casualty_count_p1 =  (int) Math.round(causaltiesCount1 * 0.25);
		casualty_count_p2 =  (int) Math.round(causaltiesCount1 * 0.25);
		casualty_count_p3 =  (int) Math.round(causaltiesCount1 * 0.5);
		int sum = casualty_count_p1 + casualty_count_p2 + casualty_count_p3;

		if (sum > causaltiesCount1) 
			casualty_count_p3--;
		else if (sum < causaltiesCount1) 
			casualty_count_p3++;		
	}

	//****************************************************************************************************
	
	public void OnsetCasualtyRPM( int causaltiesCount1) {

		int  RPMscore ;
		//-----------------------------------------
		// create category 1
		for (int i = 0; i < causaltiesCount1; i++) {

	
		    RPMscore=InputFile.intialRPM [CasualtyCounter];

			Casualty newCasualty=new Casualty(context, geography, this.location,RPMscore,Ontology.CasualtyStatus.NonTrapped,CasualtyCounter , this);	
			casualties.add(newCasualty);
			
			System.out.println("PRIORITY_execution =>  "+ InputFile.PRIORITY_execution +"   "  + newCasualty.ID );
			ScheduleParameters params = ScheduleParameters.createRepeating(schedule.getTickCount() + 1 , 1,InputFile.PRIORITY_execution ++ );
			schedule.schedule(params,newCasualty,"step");
			
			

			if ( Randomly_Location )
			{

				do {
					newCasualty.setLocation_and_move( this.location );
					SetRandomLocationCasualty2(newCasualty);	
					//Coordinate cord =RandomLocationCasualty(this.radius_incidentCircle);						
					//newCasualty.setLocation_and_move(cord );
				} while( IsCasualtybhindBuilding(newCasualty) || ISDebrisLocationOfCasualty( newCasualty)  ) ; //|| (!Iaminsector(newCasualty))

			}
			else
			{
					newCasualty.setLocation_and_move(InputFile.Casualty_location_GSM_bigerarea[CasualtyCounter]);
			}

			if (  (newCasualty.InitialRPM >= 9 && newCasualty.InitialRPM <= 12)  ||  newCasualty.InitialRPM ==99 )  
				newCasualty.Generate_responder_zones() ;
			
			newCasualty.Setmovementdetails();
			CasualtyCounter++;
			
			
//			if( newCasualty.CurrentRPM >= 1 && newCasualty.CurrentRPM <= 4 )
//				newCasualty.ColorCode=1;
//			else if( newCasualty.CurrentRPM >= 5 && newCasualty.CurrentRPM <= 8 )
//				newCasualty.ColorCode=2;
//			else if( newCasualty.CurrentRPM >= 9 && newCasualty.CurrentRPM <= 12 )
//				newCasualty.ColorCode=3;
//			
			
		}

	};
	
	public void OnsetCasualtyRPM_old( int causaltiesCount1) {

//		DistrbutionOfCasualty(causaltiesCount1);
//		int counter1=0;
//
//		int  RPMscore ;
//		//-----------------------------------------
//		// create category 1
//		for (int i = 0; i < casualty_count_p1; i++) {
//
//			//Generate random int value from 1 to 4		4  1	
//			if ( Randomly_Severity)
//				RPMscore= randomizer.nextInt(4)+ 0 ; 
//			else
//				RPMscore=InputFile.intialRPM [counter1++];
//
//			Casualty newCasualty=new Casualty(context, geography, this.location,RPMscore,Ontology.CasualtyStatus.NonTrapped,CasualtyCounter++ , this);	
//			casualties.add(newCasualty);
//			
//			System.out.println("PRIORITY_execution =>  "+ InputFile.PRIORITY_execution +"   "  + newCasualty.ID );
//			ScheduleParameters params = ScheduleParameters.createRepeating(schedule.getTickCount() + 1 , 1,InputFile.PRIORITY_execution ++ );
//			schedule.schedule(params,newCasualty,"step");
//			
//			
//
//			if ( Randomly_Location )
//			{
//
//				do {
//					newCasualty.setLocation_and_move( this.location );
//					SetRandomLocationCasualty2(newCasualty);	
//					//Coordinate cord =RandomLocationCasualty(this.radius_incidentCircle);						
//					//newCasualty.setLocation_and_move(cord );
//				} while( IsCasualtybhindBuilding(newCasualty) || ISDebrisLocationOfCasualty( newCasualty)  ) ; //|| (!Iaminsector(newCasualty))
//
//			}
//			else
//			{
//
//				if (this.ID.equals("NCS_Incident"))
//					newCasualty.setLocation_and_move(InputFile.Casualty_location_NCS[counter2++]);
//				else if (ID.equals("GSS_Incident"))
//					newCasualty.setLocation_and_move(InputFile.Casualty_location_GSS[counter2++]);
//				else if (ID.equals("MyFrind_Incident"))
//					newCasualty.setLocation_and_move(InputFile.Casualty_location_GSM[counter2++]);
//
//
//			}
//
//			newCasualty.Setmovementdetails();
//		}
//
//		//-----------------------------------------
//		// create category 2
//		for (int i = 0; i < casualty_count_p2; i++) {
//
//			//Generate random int value from 5 to 8			
//			if ( Randomly_Severity)
//				RPMscore= randomizer.nextInt(4)+ 5  ;
//			else
//				RPMscore=InputFile.intialRPM [counter1++];
//			Casualty newCasualty=new Casualty(context, geography, this.location,RPMscore,Ontology.CasualtyStatus.NonTrapped ,CasualtyCounter++  , this);	
//			casualties.add(newCasualty);
//			
//			System.out.println("PRIORITY_execution =>  "+ InputFile.PRIORITY_execution +"   "  + newCasualty.ID );
//			ScheduleParameters params = ScheduleParameters.createRepeating(schedule.getTickCount() + 1 , 1,InputFile.PRIORITY_execution ++ );
//			schedule.schedule(params,newCasualty,"step");
//			
//
//			if ( Randomly_Location )
//			{
//				do {
//					newCasualty.setLocation_and_move( this.location );
//					SetRandomLocationCasualty2(newCasualty);	
//					//Coordinate cord =RandomLocationCasualty(this.radius_incidentCircle);						
//					//newCasualty.setLocation_and_move(cord );			
//				} while( IsCasualtybhindBuilding(newCasualty) || ISDebrisLocationOfCasualty( newCasualty) ) ; //|| (!Iaminsector(newCasualty))
//
//			}
//			else
//			{
//
//				if (this.ID.equals("NCS_Incident"))
//					newCasualty.setLocation_and_move(InputFile.Casualty_location_NCS[counter2++]);
//				else if (ID.equals("GSS_Incident"))
//					newCasualty.setLocation_and_move(InputFile.Casualty_location_GSS[counter2++]);
//				else if (ID.equals("MyFrind_Incident"))
//					newCasualty.setLocation_and_move(InputFile.Casualty_location_GSM[counter2++]);
//
//
//			}
//
//			newCasualty.Setmovementdetails();
//		}
//		//-----------------------------------------
//		// create category 3
//		for (int i = 0; i < casualty_count_p3; i++) {
//
//			//Generate random int value from 9 to 12  
//			if ( Randomly_Severity)
//				RPMscore= randomizer.nextInt(4)+ 9  ;	
//			else
//				RPMscore=InputFile.intialRPM [counter1++];
//
//			Casualty newCasualty=new Casualty(context, geography, this.location,RPMscore,Ontology.CasualtyStatus.NonTrapped,CasualtyCounter++ , this);	
//			casualties.add(newCasualty);
//			
//			System.out.println("PRIORITY_execution =>  "+ InputFile.PRIORITY_execution +"   "  + newCasualty.ID );
//			ScheduleParameters params = ScheduleParameters.createRepeating(schedule.getTickCount() + 1 , 1,InputFile.PRIORITY_execution ++ );
//			schedule.schedule(params,newCasualty,"step");
//			
//
//			if ( Randomly_Location )
//			{
//				do {
//					newCasualty.setLocation_and_move( this.location );
//					SetRandomLocationCasualty2(newCasualty);	
//					//Coordinate cord =RandomLocationCasualty(this.radius_incidentCircle);						
//					//newCasualty.setLocation_and_move(cord );
//				} while( IsCasualtybhindBuilding(newCasualty) || ISDebrisLocationOfCasualty( newCasualty) ) ; //|| (!Iaminsector(newCasualty))
//			}
//			else
//			{
//
//				if (this.ID.equals("NCS_Incident"))
//					newCasualty.setLocation_and_move(InputFile.Casualty_location_NCS[counter2++]);
//				else if (ID.equals("GSS_Incident"))
//					newCasualty.setLocation_and_move(InputFile.Casualty_location_GSS[counter2++]);
//				else if (ID.equals("MyFrind_Incident"))
//					newCasualty.setLocation_and_move(InputFile.Casualty_location_GSM[counter2++]);
//
//
//
//			}
//			
//			if (InitialRPM =99 >= 9 && InitialRPM =99 <= 12  ||  InitialRPM =99== )  
//				Generate_responder_zones() ;
//
//			newCasualty.Setmovementdetails();
//		}	
	};

	//****************************************************************************************************
	//Generate Random Location Within Circle Boundary
	private Coordinate RandomLocationCasualty(double radius )
	{
		double a=randomizer.nextDouble()* 2 * Math.PI;
		double r=(radius/100000) * Math.sqrt(randomizer.nextDouble());
		Coordinate coord= new Coordinate(location.x+r*Math.cos(a),location.y+r*Math.sin(a));	
		return coord;	
	}

	//****************************************************************************************************
	//Generate Random Location Within Circle Boundary
	private void SetRandomLocationCasualty2(Object newCasualty )
	{
		Coordinate  oldPosition= ((Casualty) newCasualty).Return_CurrentLocation();

		double a=randomizer.nextDouble()* 2 * Math.PI;
		//double r=randomizer.nextDouble() * (this.radius_incidentCircle+3) ;

		double maxSize= this.radius_incidentCircle - 3 ;  
		double minSize= 6;
		double r=((randomizer.nextDouble()*(maxSize-minSize))+minSize);	

		geography.moveByVector( newCasualty , r , SI.METER,a );

		// Update Search Zones Position As Well	
		((Casualty) newCasualty).Move_related_zones(  oldPosition , ((Casualty) newCasualty).Return_CurrentLocation());
	}

	//****************************************************************************************************
	//check
	public boolean IsCasualtybhindBuilding( Casualty  newCasualty  ) {

		boolean vaildloc= false;

		// 1- Get All Near Geographical Objects within incident Area
		@SuppressWarnings("unchecked") //@SuppressWarnings("unchecked") tells the compiler that the programmer believes the code to be safe and won't cause unexpected exceptions.
		List<TopographicArea> nearObjects = (List<TopographicArea>) BuildStaticFuction.GetObjectsWithinDistance(this,TopographicArea.class, this.radius_incidentCircle_outer );

		Coordinate[] coor = new Coordinate[2];
		coor[0]= this.getLocation();
		coor[1]=  newCasualty.getCurrentLocation();
		check_line chline= new check_line();
		GeometryFactory fac = new GeometryFactory();
		Geometry geomLine = fac.createLineString(coor) ;
		context.add(chline);
		geography.move( chline, geomLine);


		vaildloc=false;	
		for (TopographicArea obj : nearObjects) 
			if (obj.getcode()==4)// Is building  
			{
				Geometry Geombuilding = (Polygon) geography.getGeometry(obj);
				if  ( geomLine.intersects(Geombuilding))
				{vaildloc=true; break;}
			}// end for

		return vaildloc;
	}

	//****************************************************************************************************
	//check
	private boolean ISDebrisLocationOfCasualty(Object newCasualty) {

		boolean trapped= false;
		// 1- Get All Near Geographical Objects within incident Area
		@SuppressWarnings("unchecked") //@SuppressWarnings("unchecked") tells the compiler that the programmer believes the code to be safe and won't cause unexpected exceptions.
		List<TopographicArea> nearObjects = (List<TopographicArea>) BuildStaticFuction.GetObjectsWithinDistance(this,TopographicArea.class, 1000 );

		for (TopographicArea obj : nearObjects) {
			if (obj.getcode()==4)
			{
				Geometry Geombuilding = (Polygon) geography.getGeometry(obj);
				Geometry GeomCasualty = geography.getGeometry(newCasualty);
				if (GeomCasualty.intersects(Geombuilding)    )	// Is within building  	|| Geombuilding.within(GeomCasualty) 		 
				{trapped= true; buldingofcasualty=obj;break;}
			}
		}

		return trapped;
	};
	//****************************************************************************************************
	//check
	public boolean Iaminsector(Object newCasualty) {

		boolean r= false;

		for ( Sector S :Sectorlist2)
		{
			if (  S.AmIinSector(newCasualty ) )
			{	r=true;break;}
		}
		return r;
	}	

	//****************************************************************************************************
	public void OnsetTrappedCasualty( int TrappedCount) {

		Casualty ca=null ;	

		for (int i = 0; i < TrappedCount; i++) {		

			if ( Randomly_Location )
			{
				Random Randomizer = new Random();
				int  randomloc= Randomizer.nextInt(this.casualties.size())  ; 
				ca=casualties.get(randomloc) ;
			}
			else
			{
				if (this.ID.equals("NCS_Incident"))
				{ ca=casualties.get(  InputFile.CaualtyNum_Trapped[i]  );  }				
				else if (ID.equals("GSS_Incident"))
				{ ca=casualties.get(  InputFile.CaualtyNum_Trapped[i]  );  }
				else if (ID.equals("MyFrind_Incident"))
				{ ca=casualties.get(  InputFile.CaualtyNum_Trapped[i]  );   }
			}

			ca.mobility_dpeathLevel=Level.Medium ;
			ca.setTrapped();
			ca.ColorCode= 4 ;	
			
		
			
			

		}

	}

	//****************************************************************************************************
	public void OnsetUninjuredCasualty( int UninjuredcausaltiesCount) {



		int  RPMscore=99 ;

		for (int i = 0; i < UninjuredcausaltiesCount; i++) {		

			Casualty newCasualty=new Casualty(context, geography, this.location ,Ontology.CasualtyStatus.NonTrapped,CasualtyCounter , this);			
			casualties.add(newCasualty);
			//newCasualty.ColorCode = 6 ;
			
			System.out.println("PRIORITY_execution =>  "+ InputFile.PRIORITY_execution +"   "  + newCasualty.ID );
			ScheduleParameters params = ScheduleParameters.createRepeating(schedule.getTickCount() + 1 , 1,InputFile.PRIORITY_execution ++ );
			schedule.schedule(params,newCasualty,"step");
			

			if ( Randomly_Location )
			{

				do {
					newCasualty.setLocation_and_move( this.location );
					SetRandomLocationCasualty2(newCasualty);	
					//Coordinate cord =RandomLocationCasualty(this.radius_incidentCircle);						
					//newCasualty.setLocation_and_move(cord );
				} while( IsCasualtybhindBuilding(newCasualty) || ISDebrisLocationOfCasualty( newCasualty)  ) ; //|| (!Iaminsector(newCasualty))

			}
			else
			{
				if (this.ID.equals("NCS_Incident"))
					newCasualty.setLocation_and_move(InputFile.Casualty_location_NCS[counter2++]);
				else if (ID.equals("GSS_Incident"))
					newCasualty.setLocation_and_move(InputFile.Casualty_location_GSS[counter2++]);
				else if (ID.equals("MyFrind_Incident"))
					newCasualty.setLocation_and_move(InputFile.Casualty_location_GSM_bigerarea[CasualtyCounter]);
			}
			
			if (newCasualty.InitialRPM >= 9 && newCasualty.InitialRPM <= 12  ||  newCasualty.InitialRPM ==99 )  
				newCasualty.Generate_responder_zones() ;
			
			newCasualty.Setmovementdetails();
			CasualtyCounter++;	
			
			
			
		}

	}

	//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
	//                                                                 Wreckage and Traffic
	//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
	public void OnsetWreckages() {

		ArrayList<TopographicArea> Partslist = new ArrayList<TopographicArea>();

		//1- Get All Near Geographical Objects
		@SuppressWarnings("unchecked") 
		List<TopographicArea> nearObjects_RoadObjects = (List<TopographicArea>) BuildStaticFuction.GetObjectsWithinDistance( this,TopographicArea.class, this.radius_incidentCircle+ 500 );

		// High ---------------------------	  + 20 for make sure take route completely	
		//1- Get All Near Road link
		@SuppressWarnings("unchecked") 
		List<RoadLink> nearObjects_RoadLink1 = (List<RoadLink>) BuildStaticFuction.GetObjectsWithinDistance( this.BasicNode ,RoadLink.class, this.radius_incidentCircle+ 20 + 20 );

		for (RoadLink RL: nearObjects_RoadLink1  )
		{
			RL.WreckageLevel=Level.High ;						  
			RL.EstimatedTimeWreckage= RL.length    *   InputFile.EstimatedTimeWreckageTrafficPerMeter_H  ;	
			RL.PointofClear=new PointDestination  ( context, geography, RL.getCurrentPosition(geography));
			//RL.PointofClear.ColorCode= 25 ;

			Geometry geom = geography.getGeometry(RL);	

			for(TopographicArea   RO: nearObjects_RoadObjects )
			{
				if ( geom.intersects(geography.getGeometry(RO)) )
				{  Partslist.add(RO);  RL.Parts.add(RO); RO.used_byprogram=true;}
			}

			RL.Needschedule=true;
			//System.out.println("PRIORITY_execution =>  "+ InputFile.PRIORITY_execution +"   "  + RL.fid );
			ScheduleParameters params = ScheduleParameters.createRepeating(schedule.getTickCount() + 1 , 1,InputFile.PRIORITY_execution ++);	
			schedule.schedule(params,RL,"step");

		}

		// Medium ---------------------------			
		//1- Get All Near Road link
		@SuppressWarnings("unchecked") 
		List<RoadLink> nearObjects_RoadLink2 = (List<RoadLink>) BuildStaticFuction.GetObjectsWithinDistance( this.BasicNode,RoadLink.class, this.radius_incidentCircle +40 + 20  );

		for (RoadLink RL: nearObjects_RoadLink2  )
		{

			if ( RL.WreckageLevel!=Level.High )
			{
				RL.WreckageLevel=Level.Medium ;
				RL.EstimatedTimeWreckage= RL.length * InputFile.EstimatedTimeWreckageTrafficPerMeter_M ;
				RL.PointofClear=new PointDestination  ( context, geography, RL.getCurrentPosition(geography));
				//RL.PointofClear.ColorCode= 25 ;

				Geometry geom = geography.getGeometry(RL);	

				for(TopographicArea   RO: nearObjects_RoadObjects )
				{
					if ( geom.intersects(geography.getGeometry(RO))  )  //&& !Partslist.contains(RO)
					{ Partslist.add(RO);  RL.Parts.add(RO);RO.used_byprogram=true;}
				}

				RL.Needschedule=true;			
				//System.out.println("PRIORITY_execution =>  "+ InputFile.PRIORITY_execution +"   "  + RL.fid );
				ScheduleParameters params = ScheduleParameters.createRepeating(schedule.getTickCount() + 1 , 1,InputFile.PRIORITY_execution ++);	
				schedule.schedule(params,RL,"step");
			}
		}

		// Low ---------------------------
		//1- Get All Near Road link
		@SuppressWarnings("unchecked") 
		List<RoadLink> nearObjects_RoadLink3 = (List<RoadLink>) BuildStaticFuction.GetObjectsWithinDistance( this.BasicNode,RoadLink.class, this.radius_incidentCircle + 60 +20 );


		for (RoadLink RL: nearObjects_RoadLink3  )
		{
			if ( RL.WreckageLevel!=Level.High  && RL.WreckageLevel!=Level.Medium )
			{
				RL.WreckageLevel=Level.Low ;
				RL.EstimatedTimeWreckage=   RL.length * InputFile.EstimatedTimeWreckageTrafficPerMeter_L  ;	
				RL.PointofClear=new PointDestination  ( context, geography, RL.getCurrentPosition(geography));
				//RL.PointofClear.ColorCode= 25 ;


				Geometry geom = geography.getGeometry(RL);	

				for(TopographicArea   RO: nearObjects_RoadObjects )
				{
					if ( geom.intersects(geography.getGeometry(RO))   )  //&& !Partslist.contains(RO)
					{  RL.Parts.add(RO);RO.used_byprogram=true;}
				}

				RL.Needschedule=true;			
				//System.out.println("PRIORITY_execution =>  "+ InputFile.PRIORITY_execution +"   "  + RL.fid );
				ScheduleParameters params = ScheduleParameters.createRepeating(schedule.getTickCount() + 1 , 1,InputFile.PRIORITY_execution ++);	
				schedule.schedule(params,RL,"step");
			}
		}
						
		nearObjects_RoadObjects.clear();
		nearObjects_RoadLink1.clear(); 
		nearObjects_RoadLink2.clear(); 
		nearObjects_RoadLink3.clear();
		
		System.gc() ;
		
	}

	//****************************************************************************************************
	public void OnsetTraffic () {

		ArrayList<TopographicArea> Partslist = new ArrayList<TopographicArea>();



		//1- Get All Near Geographical Objects
		@SuppressWarnings("unchecked") 
		List<TopographicArea> nearObjects_RoadObjects = (List<TopographicArea>) BuildStaticFuction.GetObjectsWithinDistance( this,TopographicArea.class, this.radius_incidentCircle+ 1100 );

		// High ---------------------------		
		//1- Get All Near Road link
		@SuppressWarnings("unchecked") 
		List<RoadLink> nearObjects_RoadLink1 = (List<RoadLink>) BuildStaticFuction.GetObjectsWithinDistance( this.BasicNode,RoadLink.class, this.radius_incidentCircle+ 200  );

		for (RoadLink RL: nearObjects_RoadLink1  )
		{
			if ( RL.descript_1.equals("Motorway")  || RL.descript_1.equals("A Road") || RL.descript_1.equals("B Road"))
			{		
				RL.TrafficLevel =Level.High ;						  
				RL.EstimatedTimeTraffic=  ( RL.length * InputFile.EstimatedTimeWreckageTrafficPerMeter_H  );	
				RL.PointofClear=new PointDestination  ( context, geography, RL.getCurrentPosition(geography));
				//RL.PointofClear.ColorCode= 25 ;

				Geometry geom = geography.getGeometry(RL);	

				for(TopographicArea   RO: nearObjects_RoadObjects )
					if ( geom.intersects(geography.getGeometry(RO)) )
					{  Partslist.add(RO);  RL.Parts.add(RO);RO.used_byprogram=true;}

				RL.Needschedule=true;			
				//System.out.println("PRIORITY_execution =>  "+ InputFile.PRIORITY_execution +"   "  + RL.fid );
				ScheduleParameters params = ScheduleParameters.createRepeating(schedule.getTickCount() + 1 , 1,InputFile.PRIORITY_execution ++);	
				schedule.schedule(params,RL,"step");

			}

		}

		// Medium ---------------------------			
		//1- Get All Near Road link
		@SuppressWarnings("unchecked") 
		List<RoadLink> nearObjects_RoadLink2 = (List<RoadLink>) BuildStaticFuction.GetObjectsWithinDistance( this.BasicNode,RoadLink.class, this.radius_incidentCircle +300  );

		for (RoadLink RL: nearObjects_RoadLink2  )
		{

			if ( RL.descript_1.equals("Motorway")  || RL.descript_1.equals("A Road") || RL.descript_1.equals("B Road"))
			{	
				if ( RL.TrafficLevel!=Level.High )
				{
					RL.TrafficLevel=Level.Medium ;
					RL.EstimatedTimeTraffic= ( RL.length * InputFile.EstimatedTimeWreckageTrafficPerMeter_M );
					RL.PointofClear=new PointDestination  ( context, geography, RL.getCurrentPosition(geography));
					//RL.PointofClear.ColorCode= 25 ;

					Geometry geom = geography.getGeometry(RL);	

					for(TopographicArea   RO: nearObjects_RoadObjects )
						if ( geom.intersects(geography.getGeometry(RO))  )  //&& !Partslist.contains(RO)
						{ Partslist.add(RO);  RL.Parts.add(RO);RO.used_byprogram=true; }

					RL.Needschedule=true;			
					//System.out.println("PRIORITY_execution =>  "+ InputFile.PRIORITY_execution +"   "  + RL.fid );
					ScheduleParameters params = ScheduleParameters.createRepeating(schedule.getTickCount() + 1 , 1,InputFile.PRIORITY_execution ++);	
					schedule.schedule(params,RL,"step");
				}

			}
		}

		// Low ---------------------------
		//1- Get All Near Road link
		@SuppressWarnings("unchecked") 
		List<RoadLink> nearObjects_RoadLink3 = (List<RoadLink>) BuildStaticFuction.GetObjectsWithinDistance( this.BasicNode,RoadLink.class, this.radius_incidentCircle + 400   );


		for (RoadLink RL: nearObjects_RoadLink3  )
		{
			if ( RL.descript_1.equals("Motorway")  || RL.descript_1.equals("A Road") || RL.descript_1.equals("B Road"))
			{	
				if ( RL.TrafficLevel!=Level.High  && RL.TrafficLevel!=Level.Medium )
				{

					RL.TrafficLevel=Level.Low ;
					RL.EstimatedTimeTraffic= ( RL.length * InputFile.EstimatedTimeWreckageTrafficPerMeter_L);	
					RL.PointofClear=new PointDestination  ( context, geography, RL.getCurrentPosition(geography));
					//RL.PointofClear.ColorCode= 25 ;

					Geometry geom = geography.getGeometry(RL);	

					for(TopographicArea   RO: nearObjects_RoadObjects )
						if ( geom.intersects(geography.getGeometry(RO))   )  //&& !Partslist.contains(RO)
						{  RL.Parts.add(RO);RO.used_byprogram=true; }

					RL.Needschedule=true;			
					//System.out.println("PRIORITY_execution =>  "+ InputFile.PRIORITY_execution +"   "  + RL.fid );
					ScheduleParameters params = ScheduleParameters.createRepeating(schedule.getTickCount() + 1 , 1,InputFile.PRIORITY_execution ++);	
					schedule.schedule(params,RL,"step");
				}

			}
		}

		// Low --------------------------- inside
		
		@SuppressWarnings("unchecked") 
		List<RoadLink> nearObjects_RoadLink4 = (List<RoadLink>) BuildStaticFuction.GetObjectsWithinDistance( this.BasicNode,RoadLink.class,  this.radius_incidentCircle+ 50   ); //this.radius_incidentCircle_outer


		for (RoadLink RL: nearObjects_RoadLink4  )
		{
			if ( RL.TrafficLevel!=Level.High  && RL.TrafficLevel!=Level.Medium )
			{
				RL.TrafficLevel=Level.Low ;
				RL.EstimatedTimeTraffic=  ( RL.length * InputFile.EstimatedTimeWreckageTrafficPerMeter_L );	
				RL.PointofClear=new PointDestination  ( context, geography, RL.getCurrentPosition(geography));
				//RL.PointofClear.ColorCode= 25 ;

				Geometry geom = geography.getGeometry(RL);	

				for(TopographicArea   RO: nearObjects_RoadObjects )
					if ( geom.intersects(geography.getGeometry(RO))  && !Partslist.contains(RO) )
					{  RL.Parts.add(RO);RO.used_byprogram=true; }

				RL.Needschedule=true;			
				//System.out.println("PRIORITY_execution =>  "+ InputFile.PRIORITY_execution +"   "  + RL.fid );
				ScheduleParameters params = ScheduleParameters.createRepeating(schedule.getTickCount() + 1 , 1,InputFile.PRIORITY_execution ++);	
				schedule.schedule(params,RL,"step");
			}
		}

		
		
		nearObjects_RoadLink1.clear();
		nearObjects_RoadLink2.clear();
		nearObjects_RoadLink3.clear();
		nearObjects_RoadLink4.clear();
		nearObjects_RoadObjects.clear();
		System.gc() ;

	}
	
	//****************************************************************************************************
	public void  SetColorWreckageandTraffic()

	{
		//1- Get All Near Road link
		@SuppressWarnings("unchecked") 
		List<RoadLink> nearObjects_RoadLink = (List<RoadLink>) BuildStaticFuction.GetObjectsWithinDistance( this,RoadLink.class, this.radius_incidentCircle + 1200  );

		for (RoadLink RL: nearObjects_RoadLink   )
		{
			RL.SetColorWreckageandTraffic();
			if ( RL.WreckageLevel!=Level.None|| RL.TrafficLevel!=Level.None)
				nearObjects_RLwithTrandWR_whole.add(RL);  // this only for inside and outside cordon
		}

		nearObjects_RoadLink.clear();
	}
	
	//****************************************************************************************************
	public void  Create_Netcordon_setp1()
	{		

		Network<RoadNode> cordon_network = (Network<RoadNode>) context.getProjection("cordon_network");
		List<RoadLink>  RoadLinkList  =new ArrayList<RoadLink>();
		// way 1
		//		//Get All Near Road link
		//		@SuppressWarnings("unchecked") 
		//		List<RoadLink> nearObjects_RoadLink5 = (List<RoadLink>) BuildStaticFuction.GetObjectsWithinDistance(this.Cordon_outer.Center_Point,RoadLink.class, this.radius_incidentCircle_outer + 20  );
		//		for (RoadLink RL: nearObjects_RoadLink5  )
		//		{				
		//			cordon_network.addEdge( RL.source,  RL.target,  RL.length);	
		//			RL.ColorCode=2;
		//		}



		// way 2
		// 1- by programmer
		RoadLink  Rlink=null;
		if (this.ID.equals("NCS_Incident"))
		{;}
		else if (ID.equals("GSS_Incident"))
		{;}
		else if (ID.equals("MyFrind_Incident"))
		{	
			Rlink = BuildStaticFuction.GetRoadLinkeofthisLocation(geography, "osgb4000000007933373");  
			Rlink.InsideCordon=true;
			cordon_network.addEdge( Rlink.source,  Rlink.target,  Rlink.length);			
			RoadLinkList.add(Rlink);	
			Rlink.WillbeusedinNW= true;
			//Rlink.ColorCode=2;
			
			Rlink = BuildStaticFuction.GetRoadLinkeofthisLocation(geography, "osgb4000000007990194"); 
			Rlink.InsideCordon=true;
			cordon_network.addEdge( Rlink.source,  Rlink.target,  Rlink.length);
			RoadLinkList.add(Rlink);
			Rlink.WillbeusedinNW= true;
			//Rlink.ColorCode=2;
			
			Rlink = BuildStaticFuction.GetRoadLinkeofthisLocation(geography, "osgb4000000007751005");  	
			Rlink.InsideCordon=true;
			cordon_network.addEdge( Rlink.source,  Rlink.target,  Rlink.length);
			RoadLinkList.add(Rlink);
			Rlink.WillbeusedinNW= true;
			//Rlink.ColorCode=2;
		}

		//2- SerachNodes
		for (RoadLink RL: RoadLinkList	  )
		{	
			if (  ! SerachNodes.contains(RL.source)    )
			{  
				SerachNodes.add(RL.source);
				//RL.source.ColorCode=3;
				PointDestination Point=new PointDestination  ( context, geography, RL.source.getCurrentPosition());
				RL.source.PN=Point ;
			}
			if (   ! SerachNodes.contains(RL.target)    )
			{  
				SerachNodes.add(RL.target);
				//RL.target.ColorCode=3;
				PointDestination Point=new PointDestination  ( context, geography, RL.target.getCurrentPosition());
				RL.target.PN=Point ;
			}
			nearObjects_RLwithTrandWR_inside.add(RL); // this only for inside cordon

	}
		
//		Network<RoadNode> cnetwork = (Network<RoadNode>) context.getProjection("cordon_network");
//		Iterable<?> Edges_list = cordon_network.getEdges();
//
//		for ( Object edg1 : Edges_list) 
//		{
//
//			RepastEdge edgxx=(RepastEdge)edg1;
//					System.out.println("Cordon NW " +  ((RoadNode )edgxx.getSource()).fid +"   "+ ((RoadNode )edgxx.getSource()).Name  
//							+"   "  +((RoadNode )edgxx.getTarget()).fid  +"   "  +((RoadNode )edgxx.getTarget()).Name  +"   " +edgxx.getWeight());
//		}

	
	
	}

	public void  Cut_Netcordon_setp2()
	{		
		int I=0;
		Network<RoadNode> net1 = (Network<RoadNode>) context.getProjection("Road_network");
		Network<RoadNode> net2 = (Network<RoadNode>) context.getProjection("cordon_network");

		Iterable<?> Edges_list = net2.getEdges();

		for ( Object edg : Edges_list) 
		{ 

			RepastEdge edg1 = net1.getEdge(((RepastEdge<RoadNode>) edg).getSource(), ((RepastEdge<RoadNode>) edg).getTarget()) ;
			net1.removeEdge(edg1);
			//System.out.println( I++ );
		}
	}
	
	//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
	//                                                                  Structure of Incident - customization 
	//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%	
	// Parking Nodes	
	void IdentifyParkingNodes()
	{

		// 1st way
		//1- Get All Near Road Nodes
		//		@SuppressWarnings("unchecked") 
		//		List<RoadNode> nearObjects_inside = (List<RoadNode>) BuildStaticFuction.GetObjectsWithinDistance( this,RoadNode.class, this.radius_incidentCircle );
		//
		//		@SuppressWarnings("unchecked") 
		//		List<RoadNode> nearObjects_boundery = (List<RoadNode>) BuildStaticFuction.GetObjectsWithinDistance( this,RoadNode.class, this.radius_incidentCircle+ 30 );
		//
		//		for (RoadNode RN: nearObjects_boundery   )
		//		{
		//			if (  ! nearObjects_inside.contains(RN)  )
		//			{  ParkingNodes.add(RN); RN.ColorCode= 2 ;}			
		//		}

		// 2nd way
		int colorN=0; //2
		RoadNode Node1 = BuildStaticFuction.GetRoadNodeofthisLocation(geography,"osgb4000000007932484");
		//RoadNode Node2 = BuildStaticFuction.GetRoadNodeofthisLocation(geography,"osgb4000000007932477");
		//RoadNode Node3 = BuildStaticFuction.GetRoadNodeofthisLocation(geography,"osgb4000000008000278");  //geography,"osgb5000005210831394"
		ParkingNodes.add(Node1); Node1.ColorCode= colorN ;
		//ParkingNodes.add(Node2); Node2.ColorCode= colorN;
		//ParkingNodes.add(Node3); Node3.ColorCode=  colorN ;

		System.out.println(" Number of parking Nodes  ________________________________________ " + ParkingNodes.size() );		
	}

	// Search Nodes in cordon   not used	
	void IdentifySerachRouteNodes2xx()
	{
		//way 1
		//1- Get All Near Road Nodes
		@SuppressWarnings("unchecked") 
		List<RoadNode> nearObjects_inside = (List<RoadNode>) BuildStaticFuction.GetObjectsWithinDistance( this.Cordon_outer.Center_Point ,RoadNode.class, this.radius_incidentCircle );

		@SuppressWarnings("unchecked") 
		List<RoadNode> nearObjects_boundery = (List<RoadNode>) BuildStaticFuction.GetObjectsWithinDistance( this.Cordon_outer.Center_Point,RoadNode.class, this.radius_incidentCircle_outer );

		for (RoadNode RN: nearObjects_boundery   )
		{
			if (  ! nearObjects_inside.contains(RN)  )
			{  
				SerachNodes.add(RN);
				//RN.ColorCode=3;
				PointDestination Point=new PointDestination  ( context, geography, RN.getCurrentPosition());
				RN.PN=Point ;
			}			
		}

		System.out.println(" Number of SerachRouteNodes  ________________________________________  "+ SerachNodes.size());


		nearObjects_inside.clear();
		nearObjects_boundery.clear();

	}
	//****************************************************************************************************
	// Safe locations	
	void IdentifySafelocations()
	{
		PointDestination Point=null ;
		double Angle;
		double  distance= radius_incidentCircle_inner + 5 ;
		boolean cloockDir=true;  //changed based on run result

		int colorsafe=0;  //4

		Angle=6.2831;	//e	  
		Point =  new PointDestination  ( context, geography, this.location );	
		Point.type=CordonType.Area;
		Point.ColorCode=colorsafe;
		SafeLocations.add(Point);

		if ( this.ID.equals("MyFrind_Incident") ) Point.ISvalidLocationPoint1(   this.location  , Angle ,distance,false) ;
		if ( this.ID.equals("NCS_Incident") ) 	Point.ISvalidLocationPoint2(   this.location  , Angle ,distance,false) ;
		if ( this.ID.equals("GSS_Incident") ) Point.ISvalidLocationPoint2(   this.location  , Angle ,distance,true) ;			

		Angle=3.14159;	//w	   
		Point =  new PointDestination  ( context, geography, this.location );	
		Point.type=CordonType.Area;
		Point.ColorCode=colorsafe;	
		SafeLocations.add(Point);

		if ( this.ID.equals("MyFrind_Incident") )Point.ISvalidLocationPoint1(   this.location  , Angle ,distance,false) ;
		if ( this.ID.equals("NCS_Incident") ) Point.ISvalidLocationPoint2(   this.location  , Angle ,distance,false) ;	
		if ( this.ID.equals("GSS_Incident") ) Point.ISvalidLocationPoint2(   this.location  , Angle ,distance,true) ;	

		Angle=1.5708; //n 
		Point =new PointDestination  ( context, geography, this.location );
		Point.type=CordonType.Area ;
		Point.ColorCode=colorsafe;	
		SafeLocations.add(Point);

		if ( this.ID.equals("MyFrind_Incident") ) Point.ISvalidLocationPoint1(   this.location  , Angle ,distance,false) ;		
		if ( this.ID.equals("NCS_Incident") ) Point.ISvalidLocationPoint2(   this.location  , Angle ,distance,false) ;
		if ( this.ID.equals("GSS_Incident") ) Point.ISvalidLocationPoint2(   this.location  , Angle ,distance,true) ;

		Angle=4.71239; //s 
		Point=new PointDestination  ( context, geography, this.location );
		Point.type=CordonType.Area;		
		Point.ColorCode=colorsafe;
		SafeLocations.add(Point);

		if ( this.ID.equals("MyFrind_Incident") ) Point.ISvalidLocationPoint1(   this.location  , Angle ,distance,false) ;			
		if ( this.ID.equals("NCS_Incident") ) Point.ISvalidLocationPoint2(   this.location  , Angle ,distance,false) ;	
		if ( this.ID.equals("GSS_Incident") )  	Point.ISvalidLocationPoint2(   this.location  , Angle ,distance,true) ;

	}

	//****************************************************************************************************
	// create sector by FIC 
	public void CreateSector2( ) 
	{
		Sector  Sector1,Sector2,Sector3,Sector4 ;

		if ( this.TaskMechanism_IN==TaskAllocationMechanism.Environmentsectorization ) 
		{		
			Sector1 = new Sector (1, context, geography,this.PointCenter, this.PointL ,this.PointLD,this.PointLU, this.PointLL,this );
			Sector2 = new Sector (2,context, geography,this.PointCenter, this.PointR ,this.PointRU,this.PointRD,this.PointRR ,this );
			Sector3 = new Sector (3, context, geography,this.PointCenter, this.PointU ,this.PointLU,this.PointRU,this.PointUU ,this);
			Sector4 = new Sector (4, context, geography, this.PointCenter, this.PointD ,this.PointRD,this.PointLD,this.PointDD,this);
			Sectorlist2.add(Sector1);			
			Sectorlist2.add(Sector2);
			Sectorlist2.add(Sector3);
			Sectorlist2.add(Sector4);

			//Sector1.ColorCode=1;
			//Sector2.ColorCode=2;
			//Sector3.ColorCode=3;
			//Sector4.ColorCode=4;
		}
		else //one sector
		{		
			//PointDestination x= IdentfyMyStartDirection (this. CenterNode.getCurrentPosition()  ) ;  //wrong !!!!!!!!!!!!!!!!!!!!!!!! bec CenterNode.
			//PointDestination xx= NextOppositVehicleOrStartDirection(x );  
			//Sector1 = new Sector (1 ,context, geography,x, xx ,this.PointCenter ,this );
			//Sector1 = new Sector (1 ,context, geography,this.PointCenter, this.PointCenter ,this.PointCenter ,this );
			Sector1 = new Sector (1 ,context, geography, this.PointR ,this.PointL ,this.PointCenter , this );
			Sectorlist2.add(Sector1);
			//Sector1.ColorCode=1;

			//System.out.println(" one sector" );
		}
	}

	//****************************************************************************************************
	// create  Cordon by PIC 
	public void CreateCordon() 
	{			
		RoadNode PEANode=null;	
		TopographicArea  TA=null;

		if (this.ID.equals("NCS_Incident"))
		{;}
		else if (ID.equals("GSS_Incident"))
		{;}
		else if (ID.equals("MyFrind_Incident"))
		{	
			PEANode = BuildStaticFuction.GetRoadNodeofthisLocation(geography, "osgb4000000007592232");  
			TA = BuildStaticFuction.GetTAofthisLocation(geography, "osgb5000005238946117");
			TA.used_byprogram=true;
		}

		Cordon_outer = new Cordon(this.context , this.geography  ,TA.getCurrentPosition() ,PEANode , CordonType.outer , radius_incidentCircle_outer , this) ;  //this.location

		this.Create_Netcordon_setp1();

	}

	//****************************************************************************************************
	// create Cordon by PIC 
	public void CreateTA_RCandControlArea(  int RCcapacity   ) 
	{
		TopographicArea TopA1=null, TopA2=null ;

		// RC
		if (this.ID.equals("NCS_Incident"))
			TopA1 = BuildStaticFuction.GetTAofthisLocation(geography,"" );
		else if (ID.equals("GSS_Incident"))
			TopA1 = BuildStaticFuction.GetTAofthisLocation(geography,"" );
		else if (ID.equals("MyFrind_Incident"))
			TopA1 = BuildStaticFuction.GetTAofthisLocation(geography,"osgb1000002310184780" );  //"osgb1000002310358822"  osgb1000002310184780

		RC= new TacticalArea ( context ,geography, TopA1 ,null,TacticalAreaType.RC,  RCcapacity  ) ;

		//CA  
		if (this.ID.equals("NCS_Incident"))
			TopA2 = BuildStaticFuction.GetTAofthisLocation(geography,"" );
		else if (ID.equals("GSS_Incident"))
			TopA2 = BuildStaticFuction.GetTAofthisLocation(geography,"" );
		else if (ID.equals("MyFrind_Incident"))
			TopA2 = BuildStaticFuction.GetTAofthisLocation(geography,"osgb1000002310066363" ); 

		// ControlArea=new TacticalArea ( context ,geography, TopA2 ,null,TacticalAreaType.ControlArea, 3 ) ;
	}

	//****************************************************************************************************
	// create CCS and LA  by AIC 
	public void CreateTA_CCSandloadingArea(int CCScapcity , int Loadingcapacity ) 
	{		
		TopographicArea TopA1=null, TopA2=null  ,TopA3=null  ;
		RoadNode loadingNode=null , RN1=null;

		// CCS +++++++++++++++++++++++++
		if (this.ID.equals("NCS_Incident"))
			TopA1 = BuildStaticFuction.GetTAofthisLocation(geography,"" );
		else if (ID.equals("GSS_Incident"))
			TopA1 = BuildStaticFuction.GetTAofthisLocation(geography,"" );
		else if (ID.equals("MyFrind_Incident"))
			TopA1 = BuildStaticFuction.GetTAofthisLocation(geography,"osgb1000002310184777"  );//  "osgb1000002310184786"   osgb1000002310184777  

		CCStation= new TacticalArea ( context ,geography, TopA1 ,null, TacticalAreaType.CCS , CCScapcity) ;

		//LA +++++++++++++++++++++++++	
		if (this.ID.equals("NCS_Incident"))
			TopA2 = BuildStaticFuction.GetTAofthisLocation(geography,"" );
		else if (ID.equals("GSS_Incident"))
			TopA2 = BuildStaticFuction.GetTAofthisLocation(geography,"" );
		else if (ID.equals("MyFrind_Incident"))
		{

			loadingNode = BuildStaticFuction.GetRoadNodeofthisLocation(geography, "osgb4000000007932477");  //osgb4000000007932477

			//			Network<RoadNode> Road_network = (Network<RoadNode>) context.getProjection("Road_network");	
			//			
			//			TopA2 = BuildStaticFuction.GetTAofthisLocation(geography,"osgb1000002310184804" );
			//			GeometryFactory fac = new GeometryFactory();
			//			loadingNode = new RoadNode(this.ID +"load_Ambulance",this.context ,this.geography );
			//			loadingNode.aded=true;
			//			context.add(loadingNode);
			//			geography.move(loadingNode, fac.createPoint(TopA2.getCurrentPosition()));
			//
			//
			//			RoadNode RN2 = BuildStaticFuction.GetRoadNodeofthisLocation(geography, "osgb4000000007932484");
			//			double Len=BuildStaticFuction.DistanceC(geography, loadingNode.getCurrentPosition(),RN2.getCurrentPosition()) ;
			//			GeometryFactory fac2 = new GeometryFactory();
			//			RoadLink	NewRoadLink = new RoadLink("0" , " ", " " , " " , Len , loadingNode, RN2 );
			//			Coordinate[] coor = new Coordinate[2];
			//			coor[0]= loadingNode.getCurrentPosition();
			//			coor[1]= RN2.getCurrentPosition();
			//			Geometry geom = fac2.createLineString(coor) ;
			//			context.add(NewRoadLink);
			//			geography.move( NewRoadLink, geom);
			//			Road_network.addEdge(loadingNode,RN2, Len);

		}

		this.loadingArea=new TacticalArea ( context ,geography, null, loadingNode ,TacticalAreaType.LoadingArea, Loadingcapacity ) ;


		// CP +++++++++++++++++++++++++
		if (this.ID.equals("NCS_Incident"))
			TopA3 = BuildStaticFuction.GetTAofthisLocation(geography,"" );
		else if (ID.equals("GSS_Incident"))
			TopA3 = BuildStaticFuction.GetTAofthisLocation(geography,"" );
		else if (ID.equals("MyFrind_Incident"))
			TopA3 = BuildStaticFuction.GetTAofthisLocation(geography,"osgb1000002310184813"  );    //   osgb1000002310184804   osgb1000002310184781

		collection_point=new PointDestination  ( context, geography, TopA3.getCurrentPosition() );
		//collection_point.ColorCode= 20;
		collection_point.ColorCode=18 ;

	}

	//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
	public void LRUDofIncident_old( )
	{
		double Angle;
		double  distance= radius_incidentCircle_inner; //this.radius_incidentCircle_LRUD;
		boolean cloockDir=true;  //changed based on run result


		int colorLRUD= 1;   
		int colorcorner= 2;  

		PointCenter=new PointDestination  ( context, geography, this.location );
		PointCenter.type=CordonType.direction;
		PointCenter.Name="Center";
		PointCenter.ColorCode=colorLRUD ;

		Angle=6.2831;	//e	  
		PointR =  new PointDestination  ( context, geography, this.location );	
		PointR.type=CordonType.direction;
		PointR.id=2;
		PointR.Name="Right";
		PointR.ColorCode=colorLRUD;

		//if ( this.ID.equals("MyFrind_Incident") ) PointR.ISvalidLocationPointLRUDatStreet(   this.location  , Angle ,distance ,cloockDir) ;
		if ( this.ID.equals("MyFrind_Incident") ) PointR.ISvalidLocationPoint1(   this.location  , Angle ,distance,false) ;
		if ( this.ID.equals("NCS_Incident") ) 	PointR.ISvalidLocationPoint2(   this.location  , Angle ,distance,false) ;
		if ( this.ID.equals("GSS_Incident") ) PointR.ISvalidLocationPoint2(   this.location  , Angle ,distance,true) ;			


		Angle=3.14159;	//w	   
		PointL =  new PointDestination  ( context, geography, this.location );	
		PointL.type=CordonType.direction;
		PointL.id=1;
		PointL.Name="Left";
		PointL.ColorCode=colorLRUD;	

		//if ( this.ID.equals("MyFrind_Incident") ) PointL.ISvalidLocationPointLRUDatStreet(   this.location  , Angle ,distance,cloockDir) ;
		if ( this.ID.equals("MyFrind_Incident") ) PointL.ISvalidLocationPoint1(   this.location  , Angle ,distance,false) ;
		if ( this.ID.equals("NCS_Incident") ) PointL.ISvalidLocationPoint2(   this.location  , Angle ,distance,false) ;	
		if ( this.ID.equals("GSS_Incident") ) PointL.ISvalidLocationPoint2(   this.location  , Angle ,distance,true) ;	



		Angle=1.5708; //n 
		PointU =new PointDestination  ( context, geography, this.location );
		PointU.type=CordonType.direction;
		PointU.id=3;
		PointU.Name="North";
		PointU.ColorCode=colorLRUD;

		//if ( this.ID.equals("MyFrind_Incident") )  PointU.ISvalidLocationPointLRUDatStreet(   this.location  , Angle ,distance,cloockDir) ;
		if ( this.ID.equals("MyFrind_Incident") ) PointU.ISvalidLocationPoint1(   this.location  , Angle ,distance,false) ;		
		if ( this.ID.equals("NCS_Incident") ) PointU.ISvalidLocationPoint2(   this.location  , Angle ,distance,false) ;
		if ( this.ID.equals("GSS_Incident") ) PointU.ISvalidLocationPoint2(   this.location  , Angle ,distance,true) ;

		Angle=4.71239; //s 
		PointD=new PointDestination  ( context, geography, this.location );
		PointD.type=CordonType.direction;
		PointD.id=4;
		PointD.Name="South";		
		PointD.ColorCode=colorLRUD;

		//if ( this.ID.equals("MyFrind_Incident") ) PointD.ISvalidLocationPointLRUDatStreet(   this.location  , Angle ,distance,cloockDir) ;
		if ( this.ID.equals("MyFrind_Incident") ) PointD.ISvalidLocationPoint1(   this.location  , Angle ,distance,false) ;			
		if ( this.ID.equals("NCS_Incident") ) PointD.ISvalidLocationPoint2(   this.location  , Angle ,distance,false) ;	
		if ( this.ID.equals("GSS_Incident") )  	PointD.ISvalidLocationPoint2(   this.location  , Angle ,distance,true) ;



		//*****************************************	
		Angle=6.2831;	//e	  
		PointRR =  new PointDestination  ( context, geography, this.location );
		PointRR.type=CordonType.direction;
		PointRR.id=2;
		PointRR.Name="Right";
		PointRR.ColorCode=colorLRUD;

		//if ( this.ID.equals("MyFrind_Incident") ) PointRR.ISvalidLocationPointLRUDatStreet(   this.location  , Angle ,distance/2,cloockDir) ;	
		if ( this.ID.equals("MyFrind_Incident") ) PointRR.ISvalidLocationPoint1(   this.location  , Angle ,distance/2,false) ;		
		if ( this.ID.equals("NCS_Incident") ) PointRR.ISvalidLocationPoint2(   this.location  , Angle ,distance/2,false) ;
		if ( this.ID.equals("GSS_Incident") )  PointRR.ISvalidLocationPoint2(   this.location  , Angle ,distance/2,true); 



		Angle=3.14159;	//w	 
		PointLL =  new PointDestination  ( context, geography, this.location );		
		PointLL.type=CordonType.direction;
		PointLL.id=1;
		PointLL.Name="Left";
		PointLL.ColorCode=colorLRUD;
		//if ( this.ID.equals("MyFrind_Incident") )  PointLL.ISvalidLocationPointLRUDatStreet(   this.location  , Angle ,distance/2,cloockDir) ;
		if ( this.ID.equals("MyFrind_Incident") ) PointLL.ISvalidLocationPoint1(   this.location  , Angle ,distance/2,false) ;		
		if ( this.ID.equals("NCS_Incident") ) PointLL.ISvalidLocationPoint2(   this.location  , Angle ,distance/2,false) ;
		if ( this.ID.equals("GSS_Incident") )  PointLL.ISvalidLocationPoint2(   this.location  , Angle ,distance/2,true) ;



		Angle=1.5708; //n 
		PointUU =new PointDestination  ( context, geography, this.location );
		PointUU.type=CordonType.direction;
		PointUU.id=3;
		PointUU.Name="North";
		PointUU.ColorCode=colorLRUD;
		//if ( this.ID.equals("MyFrind_Incident") ) PointUU.ISvalidLocationPointLRUDatStreet(   this.location  , Angle ,distance/2,cloockDir) ;
		if ( this.ID.equals("MyFrind_Incident") ) PointUU.ISvalidLocationPoint1(   this.location  , Angle ,distance/2,false) ;		
		if ( this.ID.equals("NCS_Incident") ) PointUU.ISvalidLocationPoint2(   this.location  , Angle ,distance/2,false) ;
		if ( this.ID.equals("GSS_Incident") )  PointUU.ISvalidLocationPoint2(   this.location  , Angle ,distance/2,true) ;



		Angle=4.71239; //s 
		PointDD=new PointDestination  ( context, geography, this.location );
		PointDD.type=CordonType.direction;
		PointDD.id=4;
		PointDD.Name="South";		
		PointDD.ColorCode=colorLRUD;
		//if ( this.ID.equals("MyFrind_Incident") ) 	PointDD.ISvalidLocationPointLRUDatStreet(   this.location  , Angle ,distance/2,cloockDir) ;
		if ( this.ID.equals("MyFrind_Incident") ) PointDD.ISvalidLocationPoint1(   this.location  , Angle ,distance/2,false) ;		
		if ( this.ID.equals("NCS_Incident") ) PointDD.ISvalidLocationPoint2(   this.location  , Angle ,distance/2,false) ;
		if ( this.ID.equals("GSS_Incident") )    PointDD.ISvalidLocationPoint2(   this.location  , Angle ,distance/2,true) ;	



		//*****************************************	

		Angle=0.785398 ;	// 45    
		PointRU =  new PointDestination  ( context, geography, this.location );		
		PointRU.type=CordonType.direction;
		PointRU.Name="EastNorth";
		PointRU.ColorCode=colorcorner;

		//if ( this.ID.equals("MyFrind_Incident") )  PointRU.ISvalidLocationPointLRUDatnotStreet(   this.location  , Angle ,distance,cloockDir) ;
		if ( this.ID.equals("MyFrind_Incident") ) PointRU.ISvalidLocationPoint1(   this.location  , Angle ,distance,false) ;		
		if ( this.ID.equals("NCS_Incident") ) PointRU.ISvalidLocationPoint2(   this.location  , Angle ,distance,false) ;	
		if ( this.ID.equals("GSS_Incident") )  	PointRU.ISvalidLocationPoint2(   this.location  , Angle ,distance,true) ;	




		Angle=2.35619 ;	//135	 
		PointLU =  new PointDestination  ( context, geography, this.location );			
		PointLU.type=CordonType.direction;
		PointLU.Name="WestNorth";
		PointLU.ColorCode=colorcorner;

		//if ( this.ID.equals("MyFrind_Incident") )  PointLU.ISvalidLocationPointLRUDatnotStreet(   this.location  , Angle ,distance,cloockDir) ;
		if ( this.ID.equals("MyFrind_Incident") ) PointLU.ISvalidLocationPoint1(   this.location  , Angle ,distance,false) ;		
		if ( this.ID.equals("NCS_Incident") )	PointLU.ISvalidLocationPoint2(   this.location  , Angle ,distance,false) ;
		if ( this.ID.equals("GSS_Incident") ) PointLU.ISvalidLocationPoint2(   this.location  , Angle ,distance,true) ;




		Angle=3.92699; //225  
		PointLD =new PointDestination  ( context, geography, this.location );
		PointLD.type=CordonType.direction;
		PointLD.Name="WestSouth";
		PointLD.ColorCode=colorcorner;

		//if ( this.ID.equals("MyFrind_Incident") )  PointLD.ISvalidLocationPointLRUDatnotStreet(   this.location  , Angle ,distance,cloockDir) ;
		if ( this.ID.equals("MyFrind_Incident") ) PointLD.ISvalidLocationPoint1(   this.location  , Angle ,distance,false) ;		
		if ( this.ID.equals("NCS_Incident") )  PointLD.ISvalidLocationPoint2(   this.location  , Angle ,distance,false) ;
		if ( this.ID.equals("GSS_Incident") )  PointLD.ISvalidLocationPoint2(   this.location  , Angle ,distance,true) ;




		Angle=5.49779; //315 
		PointRD=new PointDestination  ( context, geography, this.location );
		PointRD.type=CordonType.direction;
		PointRD.Name="EastSouth";		
		PointRD.ColorCode=colorcorner;

		//if ( this.ID.equals("MyFrind_Incident") ) PointRD.ISvalidLocationPointLRUDatnotStreet(   this.location  , Angle ,distance,cloockDir) ;
		if ( this.ID.equals("MyFrind_Incident") ) PointRD.ISvalidLocationPoint1(   this.location  , Angle ,distance,false) ;		
		if ( this.ID.equals("NCS_Incident") ) PointRD.ISvalidLocationPoint2(   this.location  , Angle ,distance,false) ;
		if ( this.ID.equals("GSS_Incident") )  PointRD.ISvalidLocationPoint2(   this.location  , Angle ,distance,true) ;

	}
		
	public void LRUDofIncident( )
	{
		double Angle;
		double  distance= radius_incidentCircle_inner; //this.radius_incidentCircle_LRUD;
		boolean cloockDir=true;  //changed based on run result


		int colorLRUD= 0;   
		int colorcorner= 0;  

		PointCenter=new PointDestination  ( context, geography, this.location );
		PointCenter.type=CordonType.direction;
		PointCenter.Name="Center";
		PointCenter.ColorCode=0;

		Angle=6.2831;	//e	  g
		PointR =  new PointDestination  ( context, geography, this.location );	
		PointR.type=CordonType.direction;
		PointR.id=2;
		PointR.Name="Right";
		PointR.ColorCode=colorLRUD;
		if ( this.ID.equals("MyFrind_Incident") ) PointR.ISvalidLocationPoint1(   this.location  , Angle ,distance,true) ;
		
		Angle=5.79779; //315 
		PointRD=new PointDestination  ( context, geography, this.location );
		PointRD.type=CordonType.direction;
		PointRD.Name="EastSouth";		
		PointRD.ColorCode=colorcorner;
		if ( this.ID.equals("MyFrind_Incident") ) PointRD.ISvalidLocationPoint1(   this.location  , Angle ,distance,true) ;		
				
		Angle=4.71239; //s 
		PointD=new PointDestination  ( context, geography, this.location );
		PointD.type=CordonType.direction;
		PointD.id=4;
		PointD.Name="South";		
		PointD.ColorCode=colorLRUD;	
		if ( this.ID.equals("MyFrind_Incident") ) PointD.ISvalidLocationPoint1(   this.location  , Angle ,distance,true) ;			
		
		Angle=3.92699; //225  
		PointLD =new PointDestination  ( context, geography, this.location );
		PointLD.type=CordonType.direction;
		PointLD.Name="WestSouth";
		PointLD.ColorCode=colorcorner;
		if ( this.ID.equals("MyFrind_Incident") ) PointLD.ISvalidLocationPoint1(   this.location  , Angle ,distance,true) ;		
		
		Angle=3.4159;  //w	   
		PointL =  new PointDestination  ( context, geography, this.location );	
		PointL.type=CordonType.direction;
		PointL.id=1;
		PointL.Name="Left";
		PointL.ColorCode=colorLRUD;	
		if ( this.ID.equals("MyFrind_Incident") ) PointL.ISvalidLocationPoint1(   this.location  , Angle ,distance,true) ;
			
	
		Angle=2.95619 ;	//135	 
		PointLU =  new PointDestination  ( context, geography, this.location );			
		PointLU.type=CordonType.direction;
		PointLU.Name="WestNorth";
		PointLU.ColorCode=colorcorner;
		if ( this.ID.equals("MyFrind_Incident") ) PointLU.ISvalidLocationPoint1(   this.location  , Angle ,distance,true) ;		
		

		Angle=1.5708; //n 
		PointU =new PointDestination  ( context, geography, this.location );
		PointU.type=CordonType.direction;
		PointU.id=3;
		PointU.Name="North";
		PointU.ColorCode=colorLRUD;
		if ( this.ID.equals("MyFrind_Incident") ) PointU.ISvalidLocationPoint1(   this.location  , Angle ,distance,true) ;		
		
		
		Angle=0.785398 ;	// 45    
		PointRU =  new PointDestination  ( context, geography, this.location );		
		PointRU.type=CordonType.direction;
		PointRU.Name="EastNorth";
		PointRU.ColorCode=colorcorner;
		if ( this.ID.equals("MyFrind_Incident") ) PointRU.ISvalidLocationPoint1(   this.location  , Angle ,distance,true) ;		

	
	}

	//****************************************************************************************************
	public PointDestination IdentfyMyStartDirection (Coordinate fromthislocation  ) 
	{
		PointDestination NearstDirection=null;
		double  mindis=999999;

		double disL= BuildStaticFuction.DistanceC(geography,   fromthislocation ,PointL.getCurrentPosition());
		if(disL<mindis )
		{ NearstDirection=PointL ;mindis =disL; }	

		double disR= BuildStaticFuction.DistanceC(geography,   fromthislocation ,PointR.getCurrentPosition());
		if(disR<mindis )
		{ NearstDirection=PointR ;mindis =disR; }	

		double disU= BuildStaticFuction.DistanceC(geography,   fromthislocation ,PointU.getCurrentPosition());
		if(disU<mindis )
		{ NearstDirection=PointU ;mindis =disU; }

		double disD= BuildStaticFuction.DistanceC(geography,   fromthislocation ,PointD.getCurrentPosition());
		if(disD<mindis )
		{ NearstDirection=PointD ;mindis =disD; }	

		return NearstDirection;
	}

	//****************************************************************************************************
	public  PointDestination NextOppositVehicleOrStartDirection(PointDestination   ofthis )
	{
		PointDestination opisDirection=null;
		switch(ofthis.Name) {	
		case "Left":
			opisDirection= this.PointR;

			break;			
		case "Right":
			opisDirection= this.PointL;		
			break;
		case "North":		
			opisDirection= this.PointD;	
			break;		
		case "South":		
			opisDirection =this.PointU	;
			break;
		}

		return opisDirection ;
	}

	//****************************************************************************************************
	public PointDestination IdentifyNearest_small (Coordinate fromthislocation  ) 
	{
		PointDestination NearstDirection=null;
		double  mindis=999999;

		double disL= BuildStaticFuction.DistanceC(geography,   fromthislocation ,PointLL.getCurrentPosition());
		if(disL<mindis )
		{ NearstDirection=PointLL ;mindis =disL; }	

		double disR= BuildStaticFuction.DistanceC(geography,   fromthislocation ,PointRR.getCurrentPosition());
		if(disR<mindis )
		{ NearstDirection=PointRR ;mindis =disR; }	

		double disU= BuildStaticFuction.DistanceC(geography,   fromthislocation ,PointUU.getCurrentPosition());
		if(disU<mindis )
		{ NearstDirection=PointUU ;mindis =disU; }

		double disD= BuildStaticFuction.DistanceC(geography,   fromthislocation ,PointDD.getCurrentPosition());
		if(disD<mindis )
		{ NearstDirection=PointDD ;mindis =disD; }	


		return NearstDirection;
	}

	//****************************************************************************************************
	public PointDestination IdentifyNearest_safelocation(Coordinate fromthislocation  ) 
	{
		PointDestination NearstSL=null;
		double   dis ,mindis=999999;

		for ( PointDestination P :SafeLocations   )
		{
			dis= BuildStaticFuction.DistanceC(geography,   fromthislocation ,P.getCurrentPosition());
			if (dis <= mindis )
			{  NearstSL= P ;  mindis=dis ;}
		}

		return NearstSL;
	}

	//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
	public double getSize() {

		return  radius_incidentCircle ;  //return  radius_incidentCircle *1000000; ///!by ammar
	}

	public void setSize(double size) {
		radius_incidentCircle = size;
	}

	public String getLocationNode()
	{
		return Nodename_Location;
	}

	public Coordinate getLocation()
	{
		return location;
	}

	public String getName() {
		return this. Nodename_Location;
	}

	//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
	//														Communication Tools
	//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

	public ArrayList<ISRecord > ISSystem =new ArrayList<ISRecord >();
	public   ArrayList<chanle> Communication_RadioSystem  = new ArrayList<chanle>();

	//****************************************************************************************************
	//                                                 Radio System
	//****************************************************************************************************
	public int  RadioSystem_CheckChanleFree(int x, Responder Res , Responder  Iwillsendtothis )
	{
		chanle ch, Rch =null;
		int result=0;     //  +1  yes free    0//  bus with other  -1//Pending

		switch( x) {
		case 1:
			ch= Communication_RadioSystem.get(0);		
			if ( ch.used == false    )
			{ result =1 ; Rch=ch;}
			else if ( ch.used ==true  &&  ch.TowhomIcall_Reciver ==Res && ch.user_currentsender==Iwillsendtothis)  
			{ result = 1 ; Rch=ch;
			
			if (ch.TowhomIcall_Reciver != null ) System.out.println("  "+"||||||||||||  Amb " + Res.Id+ "   Chanle some one  waiting me ||||||||||||" +ch.user_currentsender.Id+ " " + this.schedule.getTickCount()  );
						
			}
			else if ( ch.used ==true  &&  ch.TowhomIcall_Reciver ==Res && ch.user_currentsender!=Iwillsendtothis )
			{
				result = -1 ;
			
			if (ch.TowhomIcall_Reciver != null ) System.out.println("  "+"||||||||||||  Amb " + Res.Id+ "   Check Free  other ||||||||||||"  +ch.user_currentsender.Id+ " send to  " +ch.TowhomIcall_Reciver.Id + " " + this.schedule.getTickCount()  );
			else
				System.out.println("  "+"||||||||||||  Amb " + Res.Id+ "   Chanle called Broadcast ||||||||||||" +ch.user_currentsender.Id+ " " + this.schedule.getTickCount()  );
				
			} // IhavetoPending 
			else
			{	result=0;
				Res.BehaviourType1=RespondersBehaviourTypes1.ComunicationDelay ;  
				
			}
			break;
		case 2:
			ch= Communication_RadioSystem.get(1);		
			if ( ch.used == false)
			{ result = 1 ; Rch=ch;}
			else if ( ch.used ==true  &&  ch.TowhomIcall_Reciver ==Res&& ch.user_currentsender==Iwillsendtothis )
			{ result = 1 ; Rch=ch;}
			else if ( ch.used ==true  &&  ch.TowhomIcall_Reciver ==Res && ch.user_currentsender!=Iwillsendtothis )
			{result = -1 ;
			} // IhavetoPending 
			else
			{	result=0;
				Res.BehaviourType2=RespondersBehaviourTypes2.ComunicationDelay ; 
			//System.out.println("                                       "+"||||||||||||  Fire " + Res.Id+ " Check Free ||||||||||||"+ this.schedule.getTickCount()); 
			}
			break;	 
		case 3:
			ch= Communication_RadioSystem.get(2);		
			if ( ch.used == false)
			{ result = 1 ; Rch=ch;}
			else if ( ch.used ==true  &&  ch.TowhomIcall_Reciver ==Res && ch.user_currentsender==Iwillsendtothis)
			{ result = 1 ; Rch=ch;}
			else if ( ch.used ==true  &&  ch.TowhomIcall_Reciver ==Res && ch.user_currentsender!=Iwillsendtothis )
			{result = -1 ; } // IhavetoPending 
			else
			{	result=0 ;
				Res.BehaviourType3=RespondersBehaviourTypes3.ComunicationDelay ;
			//System.out.println("                                                                                    "+"|||||||||||| Police " + Res.Id+ " Check Free ||||||||||||"+ this.schedule.getTickCount()); 
			}
			break;

		case 4:  //shared channel
			ch= Communication_RadioSystem.get(3);		
			if ( ch.used == false)
			{ result = 1; Rch=ch;}
			else if ( ch.used ==true  &&  ch.TowhomIcall_Reciver ==Res && ch.user_currentsender==Iwillsendtothis )
			{ result = 1 ; Rch=ch;
			if (ch.TowhomIcall_Reciver != null )  System.out.println("                                                                                                                                                      "+"||||||||||||  SharedChanle "		
					+ Res.Id+ "   Chanle some one  waiting me ||||||||||||" +ch.user_currentsender.Id+ " " + this.schedule.getTickCount()  );
			
			
			}
			else if ( ch.used ==true  &&  ch.TowhomIcall_Reciver ==Res && ch.user_currentsender!=Iwillsendtothis )
			{result = -1 ; 
			
			if (ch.TowhomIcall_Reciver != null )  System.out.println("                                                                                                                                                      "+"||||||||||||  SharedChanle "		
					+ Res.Id+ "   Check Free  other ||||||||||||"  +ch.user_currentsender.Id+ " send to  " +ch.TowhomIcall_Reciver.Id + " " + this.schedule.getTickCount()  );
			else
				System.out.println("  "+"||||||||||||  Amb " + Res.Id+ "   Chanle called Broadcast ||||||||||||" +ch.user_currentsender.Id+ " " + this.schedule.getTickCount()  );
				
			
			
			
			} // IhavetoPending 
			else
			{	
				result = 0 ; 
				if ( Res instanceof Responder_Ambulance )  Res.BehaviourType1=RespondersBehaviourTypes1.ComunicationDelay ;
				if ( Res instanceof Responder_Fire )  Res.BehaviourType2=RespondersBehaviourTypes2.ComunicationDelay ;
				if ( Res instanceof Responder_Police ) Res.BehaviourType3=RespondersBehaviourTypes3.ComunicationDelay ;

				//System.out.println("                                                                                                                                                      "+"||||||||||||  SharedChanle "+ Res.Id+ " Check Free ||||||||||||"+ this.schedule.getTickCount()); 
			}
			break;
		case 5:  //shared channel  just check  not used in beging 
			ch= Communication_RadioSystem.get(3);		
			if ( ch.used == false)
			{ result = 1 ;}	
			else if ( ch.used ==true  &&  ch.TowhomIcall_Reciver ==Res && ch.user_currentsender==Iwillsendtothis )
			{ result = 1 ;}	
			break;
		default:
			System.out.println("C erorrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrr in Radio System" );	
			break;

		}		

		return result; 

	}

	public chanle On_RadioSystem( int x , Responder Res , Responder _TowhomIcall_Reciver )  //  0 amb  1 fire   p 2
	{
		chanle ch, Rch =null;	

		switch( x) {
		case 1:
			ch= Communication_RadioSystem.get(0);		
			if ( ch.used == false)
			{ 
				ch.used = true; Rch=ch; 
				ch.TowhomIcall_Reciver= _TowhomIcall_Reciver;
				ch.user_currentsender= Res;
				Res.BehaviourType1=RespondersBehaviourTypes1.Comunication ;
				//if (ch.TowhomIcall_Reciver !=null)  
				//	System.out.println("  "+"||||||||||||  Amb " + Res.Id+ "   ON ||||||||||||" + ch.TowhomIcall_Reciver.Id +" " + this.schedule.getTickCount()  );
			}
			break;
		case 2:
			ch= Communication_RadioSystem.get(1);		
			if ( ch.used == false)
			{ 
				ch.used = true; Rch=ch; 
				ch.TowhomIcall_Reciver= _TowhomIcall_Reciver;
				ch.user_currentsender= Res;
				Res.BehaviourType2=RespondersBehaviourTypes2.Comunication ;
				//System.out.println("                                       "+"||||||||||||  Fire " + Res.Id+ " ON ||||||||||||"+ this.schedule.getTickCount()); 
			}
			break;	 
		case 3:
			ch= Communication_RadioSystem.get(2);		
			if ( ch.used == false)
			{ 
				ch.used = true; 
				ch.TowhomIcall_Reciver= _TowhomIcall_Reciver;
				ch.user_currentsender= Res;
				Res.BehaviourType3=RespondersBehaviourTypes3.Comunication ;
				Rch=ch;
				//System.out.println("                                                                                    "+"|||||||||||| Police " + Res.Id+ " ON ||||||||||||"+ this.schedule.getTickCount()); 
			}
			break;
		case 4:
			ch= Communication_RadioSystem.get(3);		
			if ( ch.used == false)
			{ 
				ch.used = true; 
				ch.TowhomIcall_Reciver= _TowhomIcall_Reciver;
				ch.user_currentsender= Res;
				if ( Res instanceof Responder_Ambulance )  Res.BehaviourType1=RespondersBehaviourTypes1.Comunication ;
				if ( Res instanceof Responder_Fire )  Res.BehaviourType2=RespondersBehaviourTypes2.Comunication ;
				if ( Res instanceof Responder_Police ) Res.BehaviourType3=RespondersBehaviourTypes3.Comunication ;
				Rch=ch;
				System.out.println("                                                                                                                                                      "+"||||||||||||  SharedChanle " + Res.Id+ " ON ||||||||||||"+ ch.TowhomIcall_Reciver.Id +" " + this.schedule.getTickCount()  );
			
			}
			break;
		default:
			System.out.println("C erorrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrr in Radio System" );	
			break;
		}

		return Rch;






	}

	public void Off_RadioSystem(chanle Rch , int x , Responder Res )
	{		
		switch( x) {
		case 1:
			Rch.used=false;	
			Rch.TowhomIcall_Reciver=null;
			Rch.user_currentsender=null;
			//System.out.println("  "+"||||||||||||  Amb " + Res.Id+ "  OFF ||||||||||||"+ this.schedule.getTickCount()); 
			break;
		case 2:
			Rch.used=false;	
			Rch.TowhomIcall_Reciver=null;
			Rch.user_currentsender=null;
			// System.out.println("                                       "+"||||||||||||  Fire " + Res.Id+ "  OFF ||||||||||||"+ this.schedule.getTickCount() ); 
			break;	
		case 3:
			Rch.used=false;	
			Rch.TowhomIcall_Reciver=null;
			Rch.user_currentsender=null;
			//System.out.println("                                                                                    "+"|||||||||||| Police " + Res.Id+ "  OFF ||||||||||||"+ this.schedule.getTickCount()); 
			break;
		case 4:
			Rch.used=false;
			Rch.TowhomIcall_Reciver=null;
			Rch.user_currentsender=null;
			System.out.println("                                                                                                                                                      "+"||||||||||||  SharedChanle " + Res.Id+ "  OFF ||||||||||||"+ this.schedule.getTickCount()); 
			break;
		default:
			System.out.println("C erorrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrr in Radio System" );	
			break;
		}


	}

	//****************************************************************************************************
	//                                                 IS System
	//****************************************************************************************************
	//update visibility  for upload it will apear after one miunuts
	//@ScheduledMethod(start = 1, interval = 1 ,priority= ScheduleParameters.FIRST_PRIORITY)
	public void Step()      //Updatevisibility_RecordcasualtyISSystem(  )
	{

		for(  ISRecord Rec : ISSystem )				
			if ( Rec.Visible==false &&  this.schedule.getTickCount() >= ( Rec.LastupdateTime + InputFile.UploadingTime_Electronic ))
			{ Rec.Visible=true; }

	}

	//=================================================
	public ISRecord ReturnRecordcasualtyISSystem( Casualty CasualtyinRec )
	{
		ISRecord RC=null;

		for(  ISRecord Rec : ISSystem )				
			if ( Rec.CasualtyinRec== CasualtyinRec  )
			{ RC=Rec ; break;}

		return RC;
	}

	//=================================================
	//check
	public int NewRecordcasualtyadded_ISSystem( Ambulance_ResponderRole  _Role,Sector _inSector ,int x )
	{
		int total=0;

		if (_Role==Ambulance_ResponderRole.AmbulanceSectorCommander)  
		{

			for(  ISRecord Rec : ISSystem )				
				if ( Rec.Visible==true &&  Rec.checkedSC_DoneFieldTriage== false && (Rec.FieldTriaged==true||( Rec.Pre_treatment==true && Rec.IssuedByAmbSC==true   ) )  && Rec.inSector== _inSector )  // after triage: to free responders
					total++;

		}
		else  if (_Role==Ambulance_ResponderRole.CasualtyClearingOfficer)  
		{
			switch( x) {
			case 1:				
				for(  ISRecord Rec : ISSystem )				
					if ( Rec.Visible==true &&  Rec.checkedSC_DoneFieldTriage== false && ( Rec.Pre_treatment==true && Rec.IssuedByAmbCCO==true   )  )  
						total++;

				for(  ISRecord Rec : ISSystem )				
					if ( Rec.Visible==true && Rec.checkedCCO_DoneSecondTriage == false &&  Rec.SecondTriaged==true     )  // after secod triage: to free responders  //&& Rec.inSector== _inSector
						total++;

				for(  ISRecord Rec : ISSystem )				
					if ( Rec.Visible==true && Rec.checkedCCO_DoneSecondTriage == true &&  Rec.SivirtyGetbad_CCO==true && Rec.SecondTriaged==true   )  // after secod triage: to free responders  //&& Rec.inSector== _inSector
						total++;

				break;
			case 2:
				for(  ISRecord Rec : ISSystem )				
					if ( Rec.Visible==true && Rec.checkedCCO_DoneTreatment == false && Rec.Treated==true  )  // after treatment to update plan only
						total++;
				break;
			case 3:
				for(  ISRecord Rec : ISSystem )				
					if ( Rec.Visible==true && Rec.checkedCCO_DoneHosandAmb == false && ( Rec.Treated==true ||(Rec.SecondTriaged==true && Rec.Pre_treatment== true) ) && Rec.AssignedHospitalinRec !=null && Rec.AssignedAmbulanceinRec!=null    )  //to command Responder
						total++;
				break;
			case 4:
				for(  ISRecord Rec : ISSystem )				
					if ( Rec.Visible==true && Rec.checkedCCO_DoneonAmb == false && Rec.TransferdV==true   )  //to close task
						total++;
				break;
			}		
		}


		else if (_Role==Ambulance_ResponderRole.AmbulanceIncidentCommander)  // after triage: allocation hospital
		{

			for(  ISRecord Rec : ISSystem )				
				if (Rec.Visible==true &&  Rec.checkedAIC_DoneSecondTriage == false && Rec.SecondTriaged==true && ! Rec.FatlityRecord)
					total++;

			for(  ISRecord Rec : ISSystem )				
				if ( Rec.Visible==true && Rec.checkedAIC_DoneSecondTriage== true &&  Rec.SecondTriaged==true && ( Rec.SivirtyGetbad_AIC==true && Rec.AssignedAmbulanceinRec==null ) )  // after secod triage: to free responders  //&& Rec.inSector== _inSector
					total++;
		}

		else if (_Role==Ambulance_ResponderRole.AmbulanceLoadingCommander) //after treatment and H: allocation ambulance or update triage
		{
			switch( x) {
			case 1:

				for(  ISRecord Rec : ISSystem )				
					if ( Rec.Visible==true && Rec.checkedALC_AllocateAmb == false && ( Rec.Treated==true ||( Rec.SecondTriaged==true && Rec.Pre_treatment== true ) ) && Rec.AssignedHospitalinRec !=null && ! Rec.FatlityRecord ) // to allocate driver
						total++;

				for(  ISRecord Rec : ISSystem )				
					if ( Rec.Visible==true && Rec.checkedALC_AllocateAmb == true && (   Rec.Hospitalupdated_ALC==true  && Rec.AssignedAmbulanceinRec==null ) && ! Rec.FatlityRecord ) // to allocate driver
						total++;
				
				

				break;			
			case 2:
				for(  ISRecord Rec : ISSystem )				
					if (  Rec.Visible==true &&  Rec.checkedALC_inAmb == false && Rec.TransferdH==true  ) //to close task
						total++;
				break;}
		}

		return total;
	}

	//=================================================
	public ISRecord GeRecordcasualtyISSystem(Ambulance_ResponderRole  _Role ,Sector _inSector , int x ) //  one record FIFO
	{
		ISRecord RC=null;
		
		
		if (_Role==Ambulance_ResponderRole.AmbulanceSectorCommander)
		{
			for(  ISRecord Rec : ISSystem )				
				if ( Rec.Visible==true &&  Rec.checkedSC_DoneFieldTriage== false && ( Rec.Pre_treatment==true && Rec.IssuedByAmbSC==true)  && Rec.inSector== _inSector ) 
				{ RC=Rec ; RC.checkedSC_DoneFieldTriage=true ; break;}

			if (RC==null  )
				for(  ISRecord Rec : ISSystem )				
					if ( Rec.Visible==true &&  Rec.checkedSC_DoneFieldTriage== false && Rec.FieldTriaged==true   && Rec.inSector== _inSector ) 
					{ RC=Rec ; RC.checkedSC_DoneFieldTriage=true ; break;}

		}
		else if (_Role==Ambulance_ResponderRole.CasualtyClearingOfficer)
		{
			switch( x) {
			case 1:

				for(  ISRecord Rec : ISSystem )				
					if ( Rec.Visible==true &&  Rec.checkedSC_DoneFieldTriage== false && ( Rec.Pre_treatment==true && Rec.IssuedByAmbCCO==true)  ) 
					{ RC=Rec ; RC.checkedSC_DoneFieldTriage=true ; break;}


				if ( RC==null)
				{
					for(  ISRecord Rec : ISSystem )				
						if ( Rec.Visible==true && Rec.checkedCCO_DoneSecondTriage == true && Rec.SivirtyGetbad_CCO==true && Rec.SecondTriaged==true   )  // after secod triage: to free responders  //&& Rec.inSector== _inSector
						{ RC=Rec ; Rec.SivirtyGetbad_CCO=false ; break;}
				}
				if ( RC==null)
				{
					for(  ISRecord Rec : ISSystem )				
						if ( Rec.Visible==true &&  Rec.checkedCCO_DoneSecondTriage == false && Rec.SecondTriaged==true ) 
						{ RC=Rec ; RC.checkedCCO_DoneSecondTriage=true;Rec.SivirtyGetbad_CCO=false ; break;}
				}	

				break;
			case 2:
				for(  ISRecord Rec : ISSystem )				
					if ( Rec.Visible==true && Rec.checkedCCO_DoneTreatment == false && Rec.Treated==true ) 
					{ RC=Rec ; RC.checkedCCO_DoneTreatment=true  ; break;}
				break;
			case 3:
				for(  ISRecord Rec : ISSystem )				
					if ( Rec.Visible==true && Rec.checkedCCO_DoneHosandAmb == false && ( Rec.Treated==true ||(Rec.SecondTriaged==true && Rec.Pre_treatment== true) ) && Rec.AssignedHospitalinRec !=null && Rec.AssignedAmbulanceinRec!=null  ) 
					{ RC=Rec ; RC.checkedCCO_DoneHosandAmb=true ; break;}
				break;
			case 4:
				for(  ISRecord Rec : ISSystem )				
					if ( Rec.Visible==true && Rec.checkedCCO_DoneonAmb == false && Rec.TransferdV==true  ) 
					{ RC=Rec ; RC.checkedCCO_DoneonAmb =true ; break;}
				break;}

		}
		else if (_Role==Ambulance_ResponderRole.AmbulanceLoadingCommander)
		{
			switch( x) {
			case 1:	
							
				for(  ISRecord Rec : ISSystem )				
					if ( Rec.Visible==true && Rec.checkedALC_AllocateAmb == true && (   Rec.Hospitalupdated_ALC==true && Rec.AssignedAmbulanceinRec==null ) && ! Rec.FatlityRecord )
					{ RC=Rec ;Rec.Hospitalupdated_ALC=false  ; break;}
				
				if ( RC==null)
				{
					for(  ISRecord Rec : ISSystem )				
						if ( Rec.Visible==true && Rec.checkedALC_AllocateAmb == false && ( Rec.Treated==true ||( Rec.SecondTriaged==true && Rec.Pre_treatment== true ) )&& Rec.AssignedHospitalinRec !=null && ! Rec.FatlityRecord )
						{ RC=Rec ;RC.checkedALC_AllocateAmb=true ; break;}
				}

				break;			
			case 2:			
				for(  ISRecord Rec : ISSystem )				
					if (  Rec.Visible==true &&  Rec.checkedALC_inAmb  == false && Rec.TransferdH==true  ) //to close task
					{ RC=Rec ;RC.checkedALC_inAmb=true ; break;}
				break;}
		}	

		return RC;
	}

	//=================================================
	public ArrayList<ISRecord > GetTriageResults_ISSystem(Ambulance_ResponderRole  _Role  ) //block   by AIC
	{
		ArrayList<ISRecord > TriageResult=new ArrayList<ISRecord >();

		if (_Role==Ambulance_ResponderRole.AmbulanceIncidentCommander)
		{
			for(  ISRecord Rec : ISSystem )				
				if ( Rec.Visible==true && Rec.checkedAIC_DoneSecondTriage == false && Rec.SecondTriaged==true &&  ! Rec.FatlityRecord)
				{ 
					Rec.checkedAIC_DoneSecondTriage=true ;
					TriageResult.add(Rec);
				}

			for(  ISRecord Rec : ISSystem )				
				if ( Rec.Visible==true && Rec.checkedAIC_DoneSecondTriage== true &&   Rec.SecondTriaged==true && ( Rec.SivirtyGetbad_AIC==true && Rec.AssignedAmbulanceinRec==null )  )  // after secod triage: to free responders  //&& Rec.inSector== _inSector
				{ 
					Rec.SivirtyGetbad_AIC=false ;
					TriageResult.add(Rec);
				}		
		}

		return TriageResult;
	}

	//****************************************************************************************************
	//                                                 Hospital
	//****************************************************************************************************
	public HospitalRecord ckeck_NearestHospital( List<HospitalRecord> RecivingHospital) {

		HospitalRecord NearestHosp =null;
		double MinDistance=99999999;

		for (HospitalRecord  Hosp:RecivingHospital)
		{
			double realDistance = BuildStaticFuction.Generate_Shortest_Path_byusingNode( context, geography ,this.BasicNode, Hosp.RecivingHospital.Node   ,true ) ;

			if( realDistance <= MinDistance )
			{ MinDistance=realDistance;NearestHosp = Hosp; }			 
		}


		return NearestHosp;

	}

	//****************************************************************************************************
	//                                               Tracking Ambulances 
	//****************************************************************************************************

	public Responder_Ambulance GPS_ArrivingSoonDriver  =null; 

	public  boolean ckeck_GPS_ArrivingSoonAmbulance(List<Responder_Ambulance> Drivers , List<Responder_Ambulance> UnoccupiedDrivers ,List<Responder_Ambulance> UnoccupiedArrivingSoonDrivers ,List<Responder_Ambulance> OccupiedArrivingSoonDrivers    ,double tragetDis ) 
	{
		boolean result=false;
		Responder_Ambulance  nominatedDriver=null;
		double MinDiss= 999999 ;
		for ( Responder_Ambulance  D  : Drivers )
		{

			Vehicle_Ambulance V=   (Vehicle_Ambulance) D.Myvehicle ;
			if (  ! UnoccupiedDrivers.contains(D) && ! UnoccupiedArrivingSoonDrivers.contains(D) && ! OccupiedArrivingSoonDrivers.contains(D)  &&  V.BackorLeave == true )
			{			

				double realDistance = BuildStaticFuction.Generate_Shortest_Path_byusingNode( context, geography , D.Myvehicle.Node_of_current_GPSLocation, this.Cordon_outer.EnteryPointAccess_node  ,true ) ;

				if( realDistance <= tragetDis &&  realDistance < MinDiss  )
				{ 
					nominatedDriver=D;
					MinDiss= realDistance; 
					result=true;
				}	
			}
		}

		GPS_ArrivingSoonDriver= nominatedDriver;
		return result;
	}

	//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

}


