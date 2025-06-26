package A_Agents;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.measure.unit.SI;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.util.GeometricShapeFactory;
import A_Environment.Cordon;
import A_Environment.Incident;
import A_Environment.Object_Zone;
import A_Environment.PointDestination;
import A_Environment.RoadLink;
import A_Environment.RoadNode;
import A_Environment.Sector;
import A_Environment.TacticalArea;
import A_Environment.TopographicArea;
import A_Environment.check_line;
import B_Communication.ACL_Message;
import B_Communication.Command;
import B_Communication.chanle;
import C_SimulationInput.InputFile;
import C_SimulationOutput.ResponderOutput;
import D_Ontology.BuildStaticFuction;
import D_Ontology.Ontology;
import D_Ontology.Ontology.ACLPerformative;
import D_Ontology.Ontology.Ambulance_ActivityType;
import D_Ontology.Ontology.Ambulance_ResponderRole;
import D_Ontology.Ontology.CasualtyStatus;
import D_Ontology.Ontology.CommunicationMechanism;
import D_Ontology.Ontology.CordonType;
import D_Ontology.Ontology.Fire_ActivityType;
import D_Ontology.Ontology.Fire_ResponderRole;
import D_Ontology.Ontology.Level;
import D_Ontology.Ontology.Police_ActivityType;
import D_Ontology.Ontology.Police_ResponderRole;
import D_Ontology.Ontology.RandomWalking_StrategyType;
import D_Ontology.Ontology.RespondersActions;
import D_Ontology.Ontology.RespondersBehaviourTypes1;
import D_Ontology.Ontology.RespondersBehaviourTypes2;
import D_Ontology.Ontology.RespondersBehaviourTypes3;
import D_Ontology.Ontology.RespondersTriggers;
import D_Ontology.Ontology.TaskAllocationMechanism;
import Other.GruopResponders;
import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.space.gis.Geography;
import repast.simphony.space.graph.Network;
import repast.simphony.space.graph.RepastEdge;

public class Responder {

	public boolean zzz=false;//trace
	boolean  ThereisCCCCasua=false; 

	public Ambulance_ResponderRole  PrvRoleinprint1;
	public Fire_ResponderRole  PrvRoleinprint2;
	public Police_ResponderRole  PrvRoleinprint3;

	public Police_ActivityType  CurrentAssignedMajorActivity_pol ;
	public Fire_ActivityType  CurrentAssignedMajorActivity_fr ;
	public Ambulance_ActivityType  CurrentAssignedMajorActivity_amb ;


	public Responder_Ambulance Bronzecommander_amb=null;
	public Responder_Fire Bronzecommander_fr=null;
	public Responder_Police Bronzecommander_pol=null;


	private int size;//for styling purposes
	public double  CurrentTick ,  EndofCurrentAction=0 , StartingTimeinscene2    , leaveStation  , BacktoStation ,ArrivedScene ;
	public ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule(); 
	public double  Last_update_Vofvision=0;
	//-----------------------------------------
	public String Id;				
	public Ontology.RespondersActions Action;
	public Ontology.RespondersBehaviourTypes1 BehaviourType1;
	public Ontology.RespondersBehaviourTypes2 BehaviourType2;
	public Ontology.RespondersBehaviourTypes3 BehaviourType3;
	public Ontology.RespondersTriggers SensingeEnvironmentTrigger,ActionEffectTrigger , CommTrigger=null , InterpretedTrigger ,EndActionTrigger ;
	public Ontology.RespondersTriggers TempSE=null;
	public ResponderOutput  LogFile ;
	public int AcceptRequest2=0;

	public  boolean NoMoreRouteinScene=false;

	public Sector  AssignedSector=null;	
	public Cordon   AssignedCordon=null;
	public TacticalArea AssignedTA=null ;
	public RoadLink AssignedRoute=null ;
	//public  AssignedentryAccessPoin ;
	//public SetupAreaType  SetupType =null ;
	public boolean Busy=false;
	//-----------------------------------------
	public Coordinate DestinationLocation;

	private Coordinate MiddleLocation;
	public double Total_distance ; // meter
	public Incident assignedIncident ;
	public double RadiusOfReponderVision;
	public GruopResponders  Mygroup ;
	public int ColorCode =0; //for visualization purpose

	//-----------------------------------------
	public PointDestination  _PointDestination ,   MyDirectiontowalk=null,VehicleOrStartDirection=null ,NextOppositVehicleOrStartDirection=null , StopDistnation=null; //like Ontology during moving
	//public PointDestination  CenterOfSearchArea=null;
	//-----------------------------------------

	public Ontology.TargetObject TargetKind;	
	public Casualty TargetCasualty, OnhandCasualty= null ;
	public Object TargetObjectt;
	public Responder TargetResponder ,ResponderArrivedme ;
	public TopographicArea TargetBuilding;
	public Vehicle Myvehicle, Targetvehicle;
	public RandomWalking_StrategyType walkingstrategy ;
	public RoadLink  TargetRoute ;
	public RoadNode  CurrentDirectionNode=null;
	
	//-----------------------------------------
	public boolean inAmbEvacuation=false;
	//-----------------------------------------
	public boolean FirstAriavall=true ;
	public boolean  CheckCasualtyStatusDuringSearch;
	public int Lastmessagereaded=0 ;
	//-----------------------------------------
	public boolean RandomllySearachCasualty=false, RandomllyCountCasualty =false, RandomllySearachBuld= false,RandomllySearachCordonPostion=false,RandomllySearachRoute=false ,RandomllySearachResponders=false;
	public boolean extrication= false, Firest_time=true, NOTyetReachBuilding= true, walkingOnScene=false ;		
	public boolean  DirL=false, DirR=false ,DirU=false ,DirD=false   ,ArrivedSTOP=false;  
	public boolean ExpandFieldofVision=false;
	public int  ClockDirection ;	
	boolean firstofRotation= true;
	public CordonType _CordonType;
	public static Random Randomizer = new Random();

	//-----------------------------------------
	public boolean Acknowledged=false, intialAcknowledged_FF=false , AcceptRequest=false;
	public boolean Sending=false ;
	public boolean ActivityAcceptCommunication=true;
	public ACL_Message  CurrentMessage=null;
	public List<ACL_Message> CurrentMessage_list= new ArrayList<ACL_Message>();    // it should be list  but I used this as atemp action 
	public CommunicationMechanism Currentusedcom=null;
	public Command CurrentCommandRequest=null ;
	public Responder CurrentSender=null  ;
	public Object AcknowledgedReciver , MyReceiver ;
	chanle  chanleused=null;
	public ACLPerformative MyPerformative; 
	public double    Mytimetosend;
	public boolean GO=true, back=false;
	public Casualty WorkingonCasualty= null ;
	boolean IwantTokeepPC=false;
	
	public int Comunication_External=0 , Comunication_internal =0;
	public int ComunicationDelay_External=0 , ComunicationDelay_internal=0;
	public boolean SendingReciving_internal=false , SendingReciving_External=false; 	
	//---------------------------
	//Pending Sending Action  Solution
	//public ACL_Message  PendingMessage=null;	
	public boolean wait_forPerviousReciver=false;
	public boolean PerviousAcknowledged=false;
	public List<Responder> PersonalContacts_List = new ArrayList<Responder>();
	public List<ACL_Message> PendingMessage_list = new ArrayList<ACL_Message>();
	public Ontology.RespondersActions PendingSendingAction;
	public Ontology.RespondersBehaviourTypes1 PendingBehaviourType1; //during communication
	public Ontology.RespondersBehaviourTypes2 PendingBehaviourType2;
	public Ontology.RespondersBehaviourTypes3 PendingBehaviourType3;
	//-----------------------------------------
	public ArrayList<PointDestination> Cordons_List = new ArrayList<PointDestination>();
	public List<ACL_Message> Message_inbox = new ArrayList<ACL_Message>();
	public List<ACL_Message> Message_inboxMeeting = new ArrayList<ACL_Message>();

	public List<Casualty> CasualtySeen_list = new ArrayList<Casualty>();
	public List<RoadLink> RouteSeen_list = new ArrayList<RoadLink>();
	public List<Responder_Fire > ResponderSeen_list = new ArrayList<Responder_Fire >();
	List<TopographicArea> CheckedBuildings = new ArrayList<TopographicArea>(); //like memory of responders

	// --------------------------------------------------------------
	public Context<Object> context;
	public Geography<Object> geography;

	// --------------------------------------------------------------
	double AssessRadius = 0.0002;   
	double AssessZoneRadius ;//in meter
	int numAssessZone = 8;
	List<Object_Zone> AssessZoneAgents; 				

	// --------------------------------------------------------------
	//double radius = 0.0001; 
	//double radius = 0.00012; 
	//double AssessRadius = 0.0002;    // Original Radius of the Direction circle area in degree lat/lon
	double radius = 0.00009;  
	public double  step_long=0;
	double DirectionZoneRadius ; //in meter
	int numDirectionZone = 200 ;// high search zones lead to accurate responder movement   250
	List<Object_Zone> DirectionZoneAgents; 				//Direction Zones
	Object_Zone safeZone;                               // Spatial space of the body of responder

	// --------------------------------------------------------------

	Object_Zone last_zone , PreviousbestZone=null , bestZone = null;
	List<Object_Zone> PreviousZones = new ArrayList<Object_Zone>();	
	double minDistance = 10000000; //in Km or M !!	
	double angle = 0;
	double CurrentZoneDistance;	

	int CounterSetep= 0;   // I used this when filrefirghter not found triage casulties can sart look for untiage aloso ??

	//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
	public  Responder(Context<Object> _context, Geography<Object> _geography, Coordinate _Location, String ID, Vehicle _vehicle) {

		context = _context;
		geography = _geography;
		context.add(this);
		GeometryFactory fac = new GeometryFactory();
		geography.move(this, fac.createPoint(_Location));

		//-------------------------------------------
		Id = ID;
		Myvehicle = _vehicle;
		DestinationLocation=null;
		CheckCasualtyStatusDuringSearch=false; 

		//-------------------------------------------
		LogFile= new ResponderOutput (this);
	}

	//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
	// *** 1 ****
	public void Assign_DestinationCasualty(Casualty _TargetCasualty) {

		TargetCasualty = _TargetCasualty;
		DestinationLocation = _TargetCasualty.getCurrentLocation();
		TargetObjectt=_TargetCasualty; ////////////////////////////////////////////////////////////////////////lock here 

		TargetKind=Ontology.TargetObject.Casualty;

		RandomllySearachCasualty=false ; RandomllySearachBuld= false;RandomllySearachCordonPostion=false;RandomllySearachRoute=false;RandomllyCountCasualty =false;
		NOTyetReachBuilding= true;extrication=false; PreviousbestZone=null;

	}

	//	public void Assign_DestinationRoute( RoadLink _TargetRL) {
	//
	//		TargetRoute = _TargetRL;
	//		DestinationLocation = _TargetRL.getCurrentPosition( this.geography);
	//		TargetObjectt= _TargetRL; ////////////////////////////////////////////////////////////////////////lock here 
	//
	//		TargetKind=Ontology.TargetObject.Targetobject ;
	//
	//		RandomllySearachCasualty=false ; RandomllySearachBuld= false;RandomllySearachCordonPostion=false;RandomllySearachRoute=false;RandomllyCountCasualty =false;
	//		NOTyetReachBuilding= true;extrication=false; PreviousbestZone=null;
	//
	//	}

	// *** 2 ****
	public void Assign_DestinationVehicle(Vehicle _Vehicle) {

		Targetvehicle= _Vehicle;
		DestinationLocation = _Vehicle.getCurrent_Location();
		TargetObjectt=_Vehicle ; ////////////////////////////////////////////////////////////////////////lock here 

		TargetKind=Ontology.TargetObject.Vehicle;

		RandomllySearachCasualty=false ; RandomllySearachBuld= false;RandomllySearachCordonPostion=false;RandomllySearachRoute=false;RandomllyCountCasualty =false;RandomllySearachResponders=false;
		NOTyetReachBuilding= true;extrication=false;PreviousbestZone=null;

	}

	// *** 3 ****
	public void Assign_DestinationResponder(Responder Res) {

		TargetResponder=Res;	
		DestinationLocation = geography.getGeometry(Res).getCoordinate();
		TargetObjectt = Res ;

		TargetKind=Ontology.TargetObject.Responder;
		RandomllySearachCasualty=false ; RandomllySearachBuld= false;RandomllySearachCordonPostion=false;RandomllySearachRoute=false;RandomllyCountCasualty =false;
		NOTyetReachBuilding= true;extrication=false;PreviousbestZone=null;

	}

	// *** 4 ****
	public void Assign_DestinationCordon(PointDestination cordon) {


		TargetObjectt = cordon;
		DestinationLocation = cordon.getCurrentPosition();


		TargetKind=Ontology.TargetObject.Targetobject;
		RandomllySearachCasualty=false ; RandomllySearachBuld= false;RandomllySearachCordonPostion=false;RandomllySearachRoute=false;RandomllyCountCasualty =false;RandomllySearachResponders=false;
		NOTyetReachBuilding=true;extrication=false;PreviousbestZone=null;
	}

	// *** 8 ****
	public void Assign_DestinationLocation_Serach() {

		TargetCasualty=null ; TargetObjectt=null ;TargetResponder =null ;TargetBuilding =null;Targetvehicle =null; OnhandCasualty= null ; TargetRoute=null ;ResponderArrivedme=null;

		//	if (_PointDestination == null && MyDirectiontowalk !=null )
		_PointDestination=MyDirectiontowalk ;
		if (_PointDestination == null && MyDirectiontowalk ==null )
			System.out.println(this.Id + "  error    Assign_DestinationLocation_Serach    ");

		DestinationLocation = _PointDestination.getCurrentPosition();
		TargetObjectt= _PointDestination;	
		TargetKind=Ontology.TargetObject.nothing;

		RandomllySearachCasualty=false ; RandomllySearachBuld= false;RandomllySearachCordonPostion=false;RandomllySearachRoute=false;RandomllyCountCasualty =false;RandomllySearachResponders=false;
		NOTyetReachBuilding=true;extrication=false;PreviousbestZone=null;


		if (Action== RespondersActions.Observeandcount)
			RandomllyCountCasualty =true;
		else if (Action== RespondersActions.SearchCasualty)
			RandomllySearachCasualty=true;
		else if (Action== RespondersActions.SearchRoute )
			RandomllySearachRoute=true;
		else if (Action== RespondersActions.SearchFireFighter )
			RandomllySearachResponders=true;

	}

	public void Assign_DestinationLocation_Serach_old() {

		TargetCasualty=null ; TargetObjectt=null ;TargetResponder =null ;TargetBuilding =null;Targetvehicle =null; OnhandCasualty= null ;

		if (_PointDestination == null && MyDirectiontowalk !=null )
			_PointDestination=MyDirectiontowalk ;
		else if (_PointDestination == null && MyDirectiontowalk ==null )
			System.out.println(this.Id + "  error    Assign_DestinationLocation_Serach    ");

		DestinationLocation = _PointDestination.getCurrentPosition();
		TargetObjectt= _PointDestination;	
		TargetKind=Ontology.TargetObject.nothing;


		RandomllySearachCasualty=false ; RandomllySearachBuld= false;RandomllySearachCordonPostion=false;RandomllySearachRoute=false;RandomllyCountCasualty =false;
		NOTyetReachBuilding=true;extrication=false;PreviousbestZone=null;

		ArrivedSTOP=false;

		//if ( this instanceof Responder_Ambulance )
		//	RandomllySearachCasualty=true ;

		//		else if ( this instanceof Responder_FireEngine )
		//			RandomllySearachBuld= true; 
		//
		//		else if ( this instanceof Responder_Police )
		//		{	
		//			Police_TaskType Task = ((Responder_Police)this).ReturnCuurnttask() ; 
		//
		//			if ( Task==Police_TaskType.Setcordon)
		//				RandomllySearachCordonPostion=true ;
		//			else if ( Task==Police_TaskType.CheckSecurity)
		//				RandomllySearachSuspectedObject=true ;
		//			else if ( Task ==Police_TaskType.SearchSurroundingAreaAboutCasualty)
		//				RandomllySearachCasualty=true ;
		//		}
	}

	//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%		
	public void RandomDirectionSerach( )
	{
		//Generate random int value from 1 to 4
		int  Dir= Randomizer.nextInt(4)+ 1  ; 

		switch(Dir) {
		case 1:
			MyDirectiontowalk= this.assignedIncident.PointL;		
			break;
		case 2:
			MyDirectiontowalk= this.assignedIncident.PointR;	
			break;
		case 3:
			MyDirectiontowalk= this.assignedIncident.PointU	;
			break;
		case 4:
			MyDirectiontowalk= this.assignedIncident.PointD;
			break;
		}

		_PointDestination=MyDirectiontowalk;

	}

	public void RandomDirectionSerach_Small( )
	{
		//Generate random int value from 1 to 4
		int  Dir= Randomizer.nextInt(4)+ 1  ; 

		switch(Dir) {
		case 1:
			MyDirectiontowalk= this.assignedIncident.PointLL;		
			break;
		case 2:
			MyDirectiontowalk= this.assignedIncident.PointRR;	
			break;
		case 3:
			MyDirectiontowalk= this.assignedIncident.PointUU	;
			break;
		case 4:
			MyDirectiontowalk= this.assignedIncident.PointDD;
			break;
		}

		_PointDestination=MyDirectiontowalk;

	}

	public void NextOppositDirectionSerach_AtsameAxis( )
	{

		switch(_PointDestination.Name) {	
		case "Left":
			MyDirectiontowalk= this.assignedIncident.PointR;	
			break;			
		case "Right":
			MyDirectiontowalk= this.assignedIncident.PointL;		
			break;
		case "North":		
			MyDirectiontowalk= this.assignedIncident.PointD;	
			break;		
		case "South":		
			MyDirectiontowalk= this.assignedIncident.PointU	;
			break;
		}

		_PointDestination=MyDirectiontowalk;
	}

	public void NextOppositVehicleOrStartDirection( )
	{

		switch(VehicleOrStartDirection.Name) {	
		case "Left":
			MyDirectiontowalk= this.assignedIncident.PointR;

			break;			
		case "Right":
			MyDirectiontowalk= this.assignedIncident.PointL;		
			break;
		case "North":		
			MyDirectiontowalk= this.assignedIncident.PointD;	
			break;		
		case "South":		
			MyDirectiontowalk= this.assignedIncident.PointU	;
			break;
		}

		NextOppositVehicleOrStartDirection=MyDirectiontowalk;
		_PointDestination=MyDirectiontowalk;
	}

	//**************************************************************************************************	
	public void NextRotateDirectionSearch_bigRoute( int _ClockDirection) //Basic direction

	{
		//With the clockwise	

		if (_ClockDirection==1  || _ClockDirection==0)
		{
			switch(MyDirectiontowalk.Name) {	
			case "Left":	
				_PointDestination= this.assignedIncident.PointU;
				break;			
			case "Right":	
				_PointDestination= this.assignedIncident.PointD;
				break;			
			case "North":	
				_PointDestination= this.assignedIncident.PointR	;
				break;
			case "South":		
				_PointDestination= this.assignedIncident.PointL;
				break;	}
		}
		else if (_ClockDirection==-1)
		{
			switch(MyDirectiontowalk.Name) {	
			case "Left":	
				_PointDestination= this.assignedIncident.PointD;
				break;			
			case "Right":	
				_PointDestination= this.assignedIncident.PointU;
				break;			
			case "North":	
				_PointDestination= this.assignedIncident.PointL	;
				break;
			case "South":		
				_PointDestination= this.assignedIncident.PointR;
				break;	}
		}

		MyDirectiontowalk=_PointDestination;
	}

	public void NextRotateDirectionSearch_smallRoute( int _ClockDirection)
	{
		//With the clockwise	

		if (_ClockDirection==1  || _ClockDirection==0)
		{
			switch(MyDirectiontowalk.Name) {	
			case "Left":	
				_PointDestination= this.assignedIncident.PointUU;
				break;			
			case "Right":	
				_PointDestination= this.assignedIncident.PointDD;
				break;			
			case "North":	
				_PointDestination= this.assignedIncident.PointRR;
				break;
			case "South":		
				_PointDestination= this.assignedIncident.PointLL;
				break;	}
		}
		else if (_ClockDirection==-1)
		{
			switch(MyDirectiontowalk.Name) {	
			case "Left":	
				_PointDestination= this.assignedIncident.PointDD;
				break;			
			case "Right":	
				_PointDestination= this.assignedIncident.PointUU;
				break;			
			case "North":	
				_PointDestination= this.assignedIncident.PointLL;
				break;
			case "South":		
				_PointDestination= this.assignedIncident.PointRR;
				break;	}
		}

		MyDirectiontowalk=_PointDestination;
	}

	//**************************************************************************************************	
	public void walkingstrategy()	
	{
		switch (walkingstrategy) {	

		case OneDirection :
			//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
			//+++++++ 2- OneDirection ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
			//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
			if ( MyDirectiontowalk.Name == "Left" || MyDirectiontowalk.Name == "Right"  ||MyDirectiontowalk.Name == "North" || MyDirectiontowalk.Name == "South"   ) 
			{	
				MyDirectiontowalk=this.assignedIncident.PointCenter;
				_PointDestination= MyDirectiontowalk ;
				this.Assign_DestinationLocation_Serach( );		
				//CenterOfSearchArea =null;
			}
			else if ( MyDirectiontowalk.Name == "Center" )
			{
				MyDirectiontowalk=_PointDestination;

				SensingeEnvironmentTrigger=RespondersTriggers.Arrived_WalkedalongOneDirection;		
			}

			break;

		case TowDirections_AtsameAxis :
			//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
			//+++++++ 3- TowDirections++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
			//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
			switch (MyDirectiontowalk.Name ) {
			case "Left" :
				DirL=true;
				break;
			case "Right" : 
				DirR=true;
				break;
			case "North": 
				DirU=true; 
				break;	
			case "South": 
				DirD=true;
				break;
			}

			if ( (DirL && DirR)  || (DirU  && DirD )  )  //it means he walk in all direction 
				SensingeEnvironmentTrigger=RespondersTriggers.Arrived_WalkedinAllScene;
			else
			{
				this.NextOppositDirectionSerach_AtsameAxis();
				this.Assign_DestinationLocation_Serach( );	

			}
			break;
		case FourDirections_big :
			//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
			//+++++++ 4- FourDirections ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
			//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

			switch (MyDirectiontowalk.Name ) {
			case "Left" :
				DirL=true;
				break;
			case "Right" : 
				DirR=true;
				break;
			case "North": 
				DirU=true; 
				break;	
			case "South": 
				DirD=true;
				break;
			}

			if ( DirL && DirR  && DirU  && DirD   )  //it means he walk in all direction 	
			{					
				if ( this.StopDistnation == null  )	
					SensingeEnvironmentTrigger=RespondersTriggers.Arrived_WalkedinAllScene;
				else if ( this.StopDistnation != null   &&	 ! ArrivedSTOP)
				{				
					MyDirectiontowalk=this.StopDistnation;
					_PointDestination= this.StopDistnation;
					this.Assign_DestinationLocation_Serach( );	
					ArrivedSTOP=true;
					ExpandFieldofVision=true;
				}
				else if ( this.StopDistnation != null   &&	 ArrivedSTOP)
					SensingeEnvironmentTrigger=RespondersTriggers.Arrived_WalkedinAllScene;
			}

			else
			{
				this.NextRotateDirectionSearch_bigRoute(this.ClockDirection);
				this.Assign_DestinationLocation_Serach( );		
			}

			break;
		case FourDirections_small :
			//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
			//+++++++ 4- FourDirections ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
			//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

			//if (TargetCasualty !=null ) System.out.println( Id  +"errrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrwalkingstrategyrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrr" +TargetCasualty.ID);

			if (TargetCasualty ==null )
			{
				switch (MyDirectiontowalk.Name ) {
				case "Left" :
					DirL=true;
					break;
				case "Right" : 
					DirR=true;
					break;
				case "North": 
					DirU=true; 
					break;	
				case "South": 
					DirD=true;
					break;
				}

				if ( DirL && DirR  && DirU  && DirD   )  //it means he walk in all direction 
				{
					if ( this.StopDistnation == null  )	
						SensingeEnvironmentTrigger=RespondersTriggers.Arrived_WalkedinAllScene;
					else if ( this.StopDistnation != null   &&	 ! ArrivedSTOP)
					{				
						MyDirectiontowalk=this.StopDistnation;
						_PointDestination= this.StopDistnation;
						this.Assign_DestinationLocation_Serach( );	
						ArrivedSTOP=true;
						ExpandFieldofVision=true;

					}
					else if ( this.StopDistnation != null   &&	 ArrivedSTOP)
						SensingeEnvironmentTrigger=RespondersTriggers.Arrived_WalkedinAllScene;		
				}
				else
				{
					this.NextRotateDirectionSearch_smallRoute(this.ClockDirection);										
					this.Assign_DestinationLocation_Serach( );	
				}

			}

			//----------------------------------
			//Responder_Fire
			if (SensingeEnvironmentTrigger==RespondersTriggers.Arrived_WalkedinAllScene )

				if (this instanceof Responder_Fire || this instanceof Responder_Ambulance)
				{
					@SuppressWarnings("unchecked") 
					List<Casualty> nearObjects_Casualty = (List<Casualty>) BuildStaticFuction.GetObjectsWithinDistance(this,Casualty.class, 200 );
					nearObjects_Casualty = (List<Casualty>) this.AssignedSector.GetObjectsWithinSector(nearObjects_Casualty );

					this.ThereisCCCCasua =false;
					for ( Casualty ca  : nearObjects_Casualty  )  
					{			
						if (   ca.Triage_tage!=5  )
							ThereisCCCCasua=true;
					}

					if(  this.ThereisCCCCasua ==true )
					{	
						SensingeEnvironmentTrigger=null ;				
						Reset_DirectioninSearach( ); //its memory
						ClockDirection=1;
						MyDirectiontowalk=assignedIncident.IdentifyNearest_small(Return_CurrentLocation());
						_PointDestination= this.StopDistnation;
						NextRotateDirectionSearch_smallRoute(ClockDirection);							
						Assign_DestinationLocation_Serach();  //""""??????????????????????????????????????							
						ArrivedSTOP=false;
						//ExpandFieldofVision=true;
					}
				}	
			break;
		case OneDirection_sector :
			//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
			//+++++++      Sector      ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
			//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++	

			//if (TargetCasualty !=null ) System.out.println( Id  +"errrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrwalkingstrategyrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrr" +TargetCasualty.ID);

			if (TargetCasualty ==null )
			{
				if ( this.StopDistnation == null  )	
					SensingeEnvironmentTrigger=RespondersTriggers.Arrived_WalkedalongOneDirection;	
				else if ( this.StopDistnation != null   &&	 ! ArrivedSTOP)
				{				
					MyDirectiontowalk=this.StopDistnation;
					_PointDestination= this.StopDistnation;
					this.Assign_DestinationLocation_Serach( );	
					ArrivedSTOP=true;
					ExpandFieldofVision=true;
				}
				else if ( this.StopDistnation != null   &&	 ArrivedSTOP)
				{ 
					SensingeEnvironmentTrigger=RespondersTriggers.Arrived_WalkedalongOneDirection;	


					// to amke sure all casulties trasferd expec dead
					if (this instanceof Responder_Fire )
					{


						@SuppressWarnings("unchecked") 
						List<Casualty> nearObjects_Casualty = (List<Casualty>) BuildStaticFuction.GetObjectsWithinDistance(this,Casualty.class, 200 );
						nearObjects_Casualty = (List<Casualty>) this.AssignedSector.GetObjectsWithinSector(nearObjects_Casualty );

						this.ThereisCCCCasua =false;
						for ( Casualty ca  : nearObjects_Casualty  )  
						{			
							if (   ca.Triage_tage!=5  )
								ThereisCCCCasua=true;
						}


						if(  this.ThereisCCCCasua ==true )
						{	
							SensingeEnvironmentTrigger=null ;

							if (  this.StopDistnation == AssignedSector.Limitpoint  )
								this.StopDistnation = AssignedSector.Startpoint ;

							if (  this.StopDistnation == AssignedSector.Startpoint  )
								this.StopDistnation = AssignedSector.Limitpoint ;


							MyDirectiontowalk=this.StopDistnation;
							_PointDestination= this.StopDistnation;
							Assign_DestinationLocation_Serach();  //""""??????????????????????????????????????						
							ArrivedSTOP=false;
							ExpandFieldofVision=true;

						}	

					}
				}
			}


			break;
		case Nodes_Cordon :
			//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
			//+++++++ 4- FourDirections ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
			//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

			//if (TargetRoute !=null ) System.out.println( Id  +"errrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrwalkingstrategy route rrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrr");

			if (TargetRoute ==null )
			{
				if (this instanceof Responder_Police  &&  CurrentDirectionNode!=null  ) CurrentDirectionNode.checkedT=true;
				if (this instanceof Responder_Fire &&  CurrentDirectionNode!=null  ) CurrentDirectionNode.checkedW=true;

				RoadNode NearstNode =NearstSerachNode() ;
				CurrentDirectionNode=NearstNode;

				if ( NearstNode !=null )
				{						
					MyDirectiontowalk= NearstNode.PN ;
					_PointDestination= NearstNode.PN ;

					this.Assign_DestinationLocation_Serach( );	
					//NearstNode.ColorCode= 5;
				}
				else if ( NearstNode ==null && StopDistnation == null  )	
				{
					SensingeEnvironmentTrigger=RespondersTriggers.Arrived_WalkedinAllScene; //it means he walk in all direction 
				}
				else if ( NearstNode ==null && this.StopDistnation != null   &&	 ! ArrivedSTOP)
				{				

					MyDirectiontowalk=this.StopDistnation;
					_PointDestination= this.StopDistnation;
					this.Assign_DestinationLocation_Serach( );	
					ArrivedSTOP=true;
					//ExpandFieldofVision=true;
				}
				else if ( NearstNode ==null && this.StopDistnation != null   &&	 ArrivedSTOP)
				{
					SensingeEnvironmentTrigger=RespondersTriggers.Arrived_WalkedinAllScene; //it means he walk in all direction 

					//System.out.println( Id  +"errrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrr  done walkingstrategyrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrr");
				}
			}

			break;

		case Safelocations_Cordon :
			//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
			//+++++++ 4- FourDirections ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
			//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

			//if (TargetCasualty !=null ) System.out.println( Id  +"errrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrwalkingstrategyrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrr");

			if (TargetCasualty ==null )
			{

				PointDestination Nearstsafe =NearstSafelocation() ;

				if ( Nearstsafe !=null )
				{	
					MyDirectiontowalk= Nearstsafe;
					_PointDestination= Nearstsafe ;
					Nearstsafe .checkedbypolice=true;
					this.Assign_DestinationLocation_Serach( );	
					Nearstsafe .ColorCode= 1;
					//System.out.println( Id  +"errrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrwalkingstrategyrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrr");
				}
				else if (  Nearstsafe ==null && StopDistnation == null  )	
				{
					SensingeEnvironmentTrigger=RespondersTriggers.Arrived_WalkedinAllScene; //it means he walk in all direction 
				}
				else if (  Nearstsafe ==null && this.StopDistnation != null   &&	 ! ArrivedSTOP)
				{				
					MyDirectiontowalk=this.StopDistnation;
					_PointDestination= this.StopDistnation;
					this.Assign_DestinationLocation_Serach( );	
					ArrivedSTOP=true;
					//ExpandFieldofVision=true;
				}
				else if (Nearstsafe ==null && this.StopDistnation != null   &&	 ArrivedSTOP)
					SensingeEnvironmentTrigger=RespondersTriggers.Arrived_WalkedinAllScene; //it means he walk in all direction 

			}

			break;


		}//end switch
	}

	public RoadNode NearstSerachNode()
	{
		RoadNode NearstNode=null ;
		double minDis=10000000;
		for (  RoadNode RN : this.assignedIncident.SerachNodes)

			if ( ( ! RN.checkedW && this instanceof Responder_Fire ) || ( ! RN.checkedT && this instanceof Responder_Police ) )  
			{
				double dis=BuildStaticFuction.DistanceC(geography, this.Return_CurrentLocation(), RN.getCurrentPosition());

				if ( dis <=  minDis )	
				{ minDis=dis; NearstNode=RN;}
			}

		return NearstNode ;
	}

	public PointDestination NearstSafelocation()
	{
		PointDestination Nearstsafe=null ;
		double minDis=10000000;
		for (  PointDestination SL : this.assignedIncident.SafeLocations)

			if (  ! SL.checkedbypolice  )  
			{
				double dis=BuildStaticFuction.DistanceC(geography, this.Return_CurrentLocation(), SL.getCurrentPosition());

				if ( dis <=  minDis )	
				{ minDis=dis; Nearstsafe=SL;}
			}

		return Nearstsafe ;
	}
	//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%	
	public void Walk() {

		if (RandomllySearachCasualty== true )
			//check for target
			Search_RandomlyCasualty_new2(this.CheckCasualtyStatusDuringSearch , false );

		if (RandomllyCountCasualty== true )
			Search_RandomlyCasualty_new2(this.CheckCasualtyStatusDuringSearch ,true );

		if (RandomllySearachRoute== true )
			Search_RandomlyRoute_new2();

		if (RandomllySearachResponders== true )
			Search_RandomlyResponders();

		//--------------------------------------------------------------------------------------
		//if ( TargetKind==Ontology.TargetObject.Responder  && this instanceof Responder_FireEngine  )
		//DestinationLocation=TargetResponder.Return_CurrentLocation();

		//--------------------------------------------------------------------------------------
		//  1- calculate real distance in meter
		Total_distance = BuildStaticFuction.DistanceC(geography, this.Return_CurrentLocation(), DestinationLocation);

		// 2- less than one meter before destination stop
		if (Total_distance > 1  ) {		
			walking_OneStep2(); // 0.25 M which means one step	
			walkingOnScene=true;
		} 
		else {
			walkingOnScene=false;
			PreviousbestZone=null;
			// we reached target, so we move there is less than one meter		
			switch (this.TargetKind) { // switch 1 
			case Casualty :
				SensingeEnvironmentTrigger=Ontology.RespondersTriggers.ArrivedCasualty;
				this.OnhandCasualty = (Casualty) TargetObjectt ;
				break;
			case Vehicle :				
				SensingeEnvironmentTrigger=Ontology.RespondersTriggers.ArrivedVehicle;
				break;
			case Responder:				
				SensingeEnvironmentTrigger=Ontology.RespondersTriggers.ArrivedResponder;
				((Responder) TargetObjectt).ResponderArrivedme= this;
				break;				
			case Targetobject:
				SensingeEnvironmentTrigger=Ontology.RespondersTriggers.ArrivedTargetObject;
				//if ( this instanceof Responder_Fire )System.out.println(" xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx "+Id + this.Action);			
				break;
			case nothing : 
				//Responder.Go back in different direction until he get some thing
				walkingstrategy();	
				break;
			} // switch 1
		}// we reached target


	}

	//****************************************************************************************************************	
	private void walking_OneStep2() {

		Coordinate OldPosition=this.Return_CurrentLocation();	

		if  (this.extrication)	
			MiddleLocation= this.DestinationLocation ;
		else
			SelectionZone(); 

		angle = BuildStaticFuction.AngleBetween2CartesianPoints(this.Return_CurrentLocation(), MiddleLocation);							
		geography.moveByVector(this,step_long , SI.METER, angle);

		// Update Search Zones Position As Well	
		Move_related_zones( OldPosition , this.Return_CurrentLocation());

	} 

	//****************************************************************************************************************	
	private  boolean CheckZone_SafeZoneCollision(List<TopographicArea>  nearObjects, double angle) 
	{
		boolean result=false;

		geography.moveByVector(safeZone, step_long * 1, SI.METER, angle);	

		if (safeZone.lookForObjects(nearObjects).size() == 0  ) 
			result=true;

		// return back to previous state (test ended)
		if (angle > Math.PI)
			geography.moveByVector(safeZone, step_long  * 1, SI.METER, angle - Math.PI);
		else
			geography.moveByVector(safeZone, step_long  * 1 , SI.METER, angle + Math.PI);


		return result;
	}

	//****************************************************************************************************************	
	private  boolean Check_SafeZoneInBuilding( Object  Build , double angle) 
	{
		boolean result=false;

		geography.moveByVector(safeZone, step_long  * 1, SI.METER, angle);

		if ( ( geography.getGeometry(safeZone)).intersects(( geography.getGeometry(Build )) ) || ( geography.getGeometry(Build )).intersects(( geography.getGeometry(safeZone)) ) ||  ( geography.getGeometry(Build )).contains(( geography.getGeometry(safeZone)))    )
			result=true;

		// return back to previous state (test ended)
		if (angle > Math.PI)
			geography.moveByVector(safeZone, step_long  * 1, SI.METER, angle - Math.PI);
		else
			geography.moveByVector(safeZone, step_long  * 1 , SI.METER, angle + Math.PI);


		return result;
	}

	//****************************************************************************************************************	
	private  boolean TestZone( Object_Zone Pre ,Object_Zone Cur )
	{ 
		boolean valid=false;

		if ( Pre ==null )
			valid= true;
		else
		{
			int zp= Pre .ID;
			int zt=Cur.ID;

			int  sz=zp-35;
			int  ez= zp + 35;

			//-----1-------
			if( ( sz>=1 && sz <=numDirectionZone) && (ez>=1 && ez <=numDirectionZone ))
			{
				//check1 within
				if (zt >=sz  && zt<= ez)
					valid=true;
				else
					valid=false;	
			}
			//-----2-------
			else if(ez > numDirectionZone )
			{
				//check2 out			
				ez=ez-numDirectionZone;
				if (zt >ez  && zt< sz)
					valid=false;
				else
					valid=true;	
			}
			//-----3-------
			else if( sz <=0 )
			{
				//check3 out
				sz=sz+numDirectionZone;
				if (zt >ez  && zt< sz)
					valid=false;
				else
					valid=true;	
			}
			//-------------

		}// end else

		return  valid;

	}

	//****************************************************************************************************************		
	private  void SelectionZone_old()
	{
		//xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx

		//1- Get All Near Geographical Objects
		@SuppressWarnings("unchecked") 
		List<TopographicArea> nearObjects = (List<TopographicArea>) BuildStaticFuction.GetObjectsWithinDistance(this,TopographicArea.class, 50);


		// 2- Remove all non-building objects to get a list of obstacles such as building 
		for (int i = 0; i < nearObjects.size(); i++) {
			if (nearObjects.get(i).getcode() != 4) {
				nearObjects.remove(i);
				i--; //Because if delete one object , next object not yet checked will take the previous number
			}
		}

		//xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
		bestZone = null; //reset
		MiddleLocation= null;
		minDistance = 10000000;	//reset
		///xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
		// 3- select the best Zone of DirectionZones   

		//++++ 1- +++++++++++++++++++++++++++++++++
		if (DirectionZoneRadius > Total_distance || this.TargetKind ==Ontology.TargetObject.Building)// this means that target is within search zone circle
		{
			//System.out.println("Selection 1");
			angle = BuildStaticFuction.AngleBetween2CartesianPoints(this.Return_CurrentLocation(), DestinationLocation);

			for (Object_Zone zone : DirectionZoneAgents) 
			{
				zone.setActive(false); 
				// check direct link
				if ( zone.lookForObject(TargetObjectt)  )  
				{
					if ( CheckZone_SafeZoneCollision( nearObjects,angle)  ) 
					{
						bestZone = zone;
						MiddleLocation= DestinationLocation;	
					}
				}
			} 
		}
		//++++ 2- +++++++++++++++++++++++++++++++++
		else
		{	
			//System.out.println("Selection 2");

			for (Object_Zone zone : DirectionZoneAgents) 
			{	
				zone.setActive(false); 
				if ( zone.lookForObjects(nearObjects).size() == 0 )
				{			
					CurrentZoneDistance =BuildStaticFuction.DistanceC(geography, zone.SidePoint, DestinationLocation); // calculate distance between zone end point which should be on the circumference of the search zone circle, and the target
					if ( CurrentZoneDistance <=  minDistance &&  TestZone( PreviousbestZone ,zone )  )
					{
						angle = BuildStaticFuction.AngleBetween2CartesianPoints(this.Return_CurrentLocation(), zone.SidePoint);// calculate direction  angle between zone end point and target	
						if ( CheckZone_SafeZoneCollision( nearObjects,angle) ) 
						{
							minDistance = CurrentZoneDistance;
							bestZone = zone;
							MiddleLocation=zone.SidePoint ;
						}
					}
				}
			}// for

			//System.out.println("Selection 2");
		}

		///xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
		// 4- check select the best Zone   
		if ( bestZone == null )
		{
			//++++ 3- +++++++++++++++++++++++++++++++++
			if (DirectionZoneRadius > Total_distance )
			{			
				angle = BuildStaticFuction.AngleBetween2CartesianPoints(this.Return_CurrentLocation(), DestinationLocation);// calculate direction  angle between zone end point and target	

				if ( CheckZone_SafeZoneCollision( nearObjects,angle) ) 
				{
					MiddleLocation= DestinationLocation;

				}
			}
			//++++ 4- +++++++++++++++++++++++++++++++++
			if (MiddleLocation==null )	
			{
				//System.out.println("selction 4");
				minDistance = 10000000;	//reset
				for (Object_Zone zone : DirectionZoneAgents) 
				{	
					zone.setActive(false); 
					if ( zone.lookForObjects(nearObjects).size() == 0 )
					{			

						CurrentZoneDistance = BuildStaticFuction.DistanceC(geography, zone.SidePoint, DestinationLocation); // calculate distance between zone end point which should be on the circumference of the search zone circle, and the target

						if ( CurrentZoneDistance <  minDistance   )  //&&  testZone( PreviousbestZone ,zone ) 
						{

							angle = BuildStaticFuction.AngleBetween2CartesianPoints(this.Return_CurrentLocation(), zone.SidePoint);// calculate direction  angle between zone end point and target	
							if ( CheckZone_SafeZoneCollision( nearObjects,angle)  ) 
							{
								minDistance = CurrentZoneDistance;
								bestZone = zone;
								MiddleLocation=zone.SidePoint ;
							}
						}
					}
				}// for

			}

			//System.out.println( this.Id + "  xxxxxxxxxxxxxxxxxxxxxxxxxxx   " +bestZone.ID + "     dis   " + minDistance  + "prev   "+ PreviousbestZone.ID  + "  total  " + Total_distance );

			//++++ 5- +++++++++++++++++++++++++++++++++
			if (this.TargetKind ==Ontology.TargetObject.Building)
			{	

				angle = BuildStaticFuction.AngleBetween2CartesianPoints(this.Return_CurrentLocation(), DestinationLocation);

				if ( Check_SafeZoneInBuilding(TargetObjectt,angle ) ) 
				{
					MiddleLocation= DestinationLocation;
					bestZone = null;
					NOTyetReachBuilding=false;

					//	System.out.println("dddssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssdd");
				}
			}


		} 
		///xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx		
		if (MiddleLocation==null )	
		{
			//System.out.println("selction 4");
			minDistance = 10000000;	//reset
			for (Object_Zone zone : DirectionZoneAgents) 
			{	
				zone.setActive(false); 
				if ( zone.lookForObjects(nearObjects).size() == 0 )
				{			

					CurrentZoneDistance = BuildStaticFuction.DistanceC(geography, zone.SidePoint, DestinationLocation); // calculate distance between zone end point which should be on the circumference of the search zone circle, and the target

					if ( CurrentZoneDistance <  minDistance )
					{

						angle = BuildStaticFuction.AngleBetween2CartesianPoints(this.Return_CurrentLocation(), zone.SidePoint);// calculate direction  angle between zone end point and target	
						if ( CheckZone_SafeZoneCollision( nearObjects,angle)  ) 
						{
							minDistance = CurrentZoneDistance;
							bestZone = zone;
							MiddleLocation=zone.SidePoint ;
						}
					}
				}
			}// for

		}
		///xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx	

		if (MiddleLocation==null )	
		{

			MiddleLocation= DestinationLocation;
		}

		if ( bestZone != null )
		{
			PreviousbestZone= bestZone;
			bestZone.setActive(true); // yellow shadow !!!! 

		}

		nearObjects.clear();
		nearObjects=null;

	}

	List<TopographicArea> nearObjects_AroundMe=null ;

	private  void SelectionZone()
	{
		//xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx

		if (    this.CurrentTick  >= ( Last_update_Vofvision + 30) )
		{
			//1- Get All Near Geographical Objects
			if (nearObjects_AroundMe !=null )  nearObjects_AroundMe.clear();

			this.nearObjects_AroundMe  = (List<TopographicArea>) BuildStaticFuction.GetObjectsWithinDistance(this,TopographicArea.class, 80);

			// 2- Remove all non-building objects to get a list of obstacles such as building 
			for (int i = 0; i < nearObjects_AroundMe .size(); i++) {
				if (nearObjects_AroundMe .get(i).getcode() != 4) {
					nearObjects_AroundMe .remove(i);
					i--; //Because if delete one object , next object not yet checked will take the previous number
				}
			}

			Last_update_Vofvision = this.CurrentTick   ;
		}

		//xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
		bestZone = null; //reset
		MiddleLocation= null;
		minDistance = 10000000;	//reset
		///xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
		// 3- select the best Zone of DirectionZones   

		//++++ 1- +++++++++++++++++++++++++++++++++
		if (DirectionZoneRadius > Total_distance || this.TargetKind ==Ontology.TargetObject.Building)// this means that target is within search zone circle
		{
			//System.out.println("Selection 1");
			angle = BuildStaticFuction.AngleBetween2CartesianPoints(this.Return_CurrentLocation(), DestinationLocation);

			for (Object_Zone zone : DirectionZoneAgents) 
			{
				zone.setActive(false); 
				// check direct link
				if ( zone.lookForObject(TargetObjectt)  )  
				{
					if ( CheckZone_SafeZoneCollision( nearObjects_AroundMe ,angle)  ) 
					{
						bestZone = zone;
						MiddleLocation= DestinationLocation;	
					}
				}
			} 
		}
		//++++ 2- +++++++++++++++++++++++++++++++++
		else
		{	
			//System.out.println("Selection 2");

			for (Object_Zone zone : DirectionZoneAgents) 
			{	
				zone.setActive(false); 
				if ( zone.lookForObjects(nearObjects_AroundMe ).size() == 0 )
				{			
					CurrentZoneDistance =BuildStaticFuction.DistanceC(geography, zone.SidePoint, DestinationLocation); // calculate distance between zone end point which should be on the circumference of the search zone circle, and the target
					if ( CurrentZoneDistance <=  minDistance &&  TestZone( PreviousbestZone ,zone )  )
					{
						angle = BuildStaticFuction.AngleBetween2CartesianPoints(this.Return_CurrentLocation(), zone.SidePoint);// calculate direction  angle between zone end point and target	
						if ( CheckZone_SafeZoneCollision( nearObjects_AroundMe ,angle) ) 
						{
							minDistance = CurrentZoneDistance;
							bestZone = zone;
							MiddleLocation=zone.SidePoint ;
						}
					}
				}
			}// for

			//System.out.println("Selection 2");
		}

		///xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
		// 4- check select the best Zone   
		if ( bestZone == null )
		{
			//++++ 3- +++++++++++++++++++++++++++++++++
			if (DirectionZoneRadius > Total_distance )
			{			
				angle = BuildStaticFuction.AngleBetween2CartesianPoints(this.Return_CurrentLocation(), DestinationLocation);// calculate direction  angle between zone end point and target	

				if ( CheckZone_SafeZoneCollision( nearObjects_AroundMe ,angle) ) 
				{
					MiddleLocation= DestinationLocation;

				}
			}
			//++++ 4- +++++++++++++++++++++++++++++++++
			if (MiddleLocation==null )	
			{
				//System.out.println("selction 4");
				minDistance = 10000000;	//reset
				for (Object_Zone zone : DirectionZoneAgents) 
				{	
					zone.setActive(false); 
					if ( zone.lookForObjects(nearObjects_AroundMe ).size() == 0 )
					{			

						CurrentZoneDistance = BuildStaticFuction.DistanceC(geography, zone.SidePoint, DestinationLocation); // calculate distance between zone end point which should be on the circumference of the search zone circle, and the target

						if ( CurrentZoneDistance <  minDistance   )  //&&  testZone( PreviousbestZone ,zone ) 
						{

							angle = BuildStaticFuction.AngleBetween2CartesianPoints(this.Return_CurrentLocation(), zone.SidePoint);// calculate direction  angle between zone end point and target	
							if ( CheckZone_SafeZoneCollision( nearObjects_AroundMe ,angle)  ) 
							{
								minDistance = CurrentZoneDistance;
								bestZone = zone;
								MiddleLocation=zone.SidePoint ;
							}
						}
					}
				}// for

			}

			//System.out.println( this.Id + "  xxxxxxxxxxxxxxxxxxxxxxxxxxx   " +bestZone.ID + "     dis   " + minDistance  + "prev   "+ PreviousbestZone.ID  + "  total  " + Total_distance );

			//++++ 5- +++++++++++++++++++++++++++++++++
			if (this.TargetKind ==Ontology.TargetObject.Building)
			{	

				angle = BuildStaticFuction.AngleBetween2CartesianPoints(this.Return_CurrentLocation(), DestinationLocation);

				if ( Check_SafeZoneInBuilding(TargetObjectt,angle ) ) 
				{
					MiddleLocation= DestinationLocation;
					bestZone = null;
					NOTyetReachBuilding=false;

					//	System.out.println("dddssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssdd");
				}
			}


		} 
		///xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx		
		if (MiddleLocation==null )	
		{
			//System.out.println("selction 4");
			minDistance = 10000000;	//reset
			for (Object_Zone zone : DirectionZoneAgents) 
			{	
				zone.setActive(false); 
				if ( zone.lookForObjects(nearObjects_AroundMe ).size() == 0 )
				{			

					CurrentZoneDistance = BuildStaticFuction.DistanceC(geography, zone.SidePoint, DestinationLocation); // calculate distance between zone end point which should be on the circumference of the search zone circle, and the target

					if ( CurrentZoneDistance <  minDistance )
					{

						angle = BuildStaticFuction.AngleBetween2CartesianPoints(this.Return_CurrentLocation(), zone.SidePoint);// calculate direction  angle between zone end point and target	
						if ( CheckZone_SafeZoneCollision( nearObjects_AroundMe ,angle)  ) 
						{
							minDistance = CurrentZoneDistance;
							bestZone = zone;
							MiddleLocation=zone.SidePoint ;
						}
					}
				}
			}// for

		}
		///xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx	

		if (MiddleLocation==null )	
		{

			MiddleLocation= DestinationLocation;
		}

		if ( bestZone != null )
		{
			PreviousbestZone= bestZone;
			bestZone.setActive(true); // yellow shadow !!!! 

		}

	}
	//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%	
	Coordinate[] coor = new Coordinate[2];
	check_line chline= new check_line();
	GeometryFactory fac = new GeometryFactory();
	List<check_line> check_line_list = new ArrayList<check_line>();

	private void Search_RandomlyCasualty_old(  boolean Yesckeck_Severity ,boolean ckeckcount ) {

		//		TargetCasualty=null;
		//		minDistance = 10000000;
		//		int maxpriority_level = 4;
		//		List<check_line> check_line_list = new ArrayList<check_line>();
		//		//xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
		//		//1- Get All Near Geographical Objects
		//		@SuppressWarnings("unchecked") //@SuppressWarnings("unchecked") tells the compiler that the programmer believes the code to be safe and won't cause unexpected exceptions.
		//		List<TopographicArea> nearObjects = (List<TopographicArea>) BuildStaticFuction.GetObjectsWithinDistance(this,TopographicArea.class, 100);
		//
		//		//2- Remove all non-building objects to get a list of obstacles such as building 
		//		for (int i = 0; i < nearObjects.size(); i++) {
		//			if (nearObjects.get(i).getcode() != 4) {
		//				nearObjects.remove(i);
		//				i--; //Because if delete one object , next object not yet checked will take the previous number
		//			}
		//		}
		//		//xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
		//		//temp action 
		//		double filedofVision=200 ;
		//		//if (Prvusedinprint == Ambulance_ResponderRole.Paramedic && ! ExpandFieldofVision )  filedofVision= 20;
		//		if (  this.PrvRoleinprint1 == Ambulance_ResponderRole.AmbulanceSectorCommander  && ! ExpandFieldofVision) filedofVision= 20;
		//		if (  this.PrvRoleinprint2 == Fire_ResponderRole.FireSectorCommander  && ! ExpandFieldofVision) filedofVision= 20;
		//
		//		//xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
		//		//3- Get All Near casualty Objects		
		//		@SuppressWarnings("unchecked") 
		//		List<Casualty> nearObjects_Casualty = (List<Casualty>) BuildStaticFuction.GetObjectsWithinDistance(this,Casualty.class,  filedofVision );
		//
		//		if (assignedIncident.TaskMechanism_IN==TaskAllocationMechanism.Environmentsectorization  && !ckeckcount ) //	sector			
		//			nearObjects_Casualty = (List<Casualty>) this.AssignedSector.GetObjectsWithinSector(nearObjects_Casualty );
		//
		//		//xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
		//
		//		if (! Yesckeck_Severity ) //Triage
		//		{					
		//			if ( this instanceof Responder_Ambulance )
		//			{
		//				for (int i = 0; i < nearObjects_Casualty.size(); i++) {
		//					if (nearObjects_Casualty.get(i).Outsidebulding==false ||  CasualtySeen_list.contains(nearObjects_Casualty.get(i))   ||nearObjects_Casualty.get(i).IsAssignedToResponder== true  ||nearObjects_Casualty.get(i).Status==CasualtyStatus.Trapped || nearObjects_Casualty.get(i).Status==CasualtyStatus.Triaged ||  nearObjects_Casualty.get(i).ThisFatlity ){
		//						nearObjects_Casualty.remove(i);
		//						i--; 
		//					}
		//				}
		//			}
		//
		//
		//		}
		//		//---------------------------------------------------
		//		else  //treatment
		//		{
		//			if ( this instanceof Responder_Ambulance )
		//			{
		//				for (int i = 0; i < nearObjects_Casualty.size(); i++) {
		//					if (nearObjects_Casualty.get(i).Outsidebulding==false ||  CasualtySeen_list.contains(nearObjects_Casualty.get(i)) ||nearObjects_Casualty.get(i).IsAssignedToResponder== true ||nearObjects_Casualty.get(i).Status!=CasualtyStatus.Triaged   || nearObjects_Casualty.get(i).ThisFatlity  ){
		//						nearObjects_Casualty.remove(i);
		//						i--; 
		//					}
		//				}
		//			}
		//
		//		}
		//		//xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
		//		// 4- search the nearest casualty and there is no obstacle
		//
		//		if  (nearObjects_Casualty.size()> 0) //begin if ###############
		//		{							 	 
		//			for (Casualty ca :  nearObjects_Casualty)
		//			{		
		//
		//				//1- create line to check 
		//				Coordinate[] coor = new Coordinate[2];
		//				coor[0]= this.Return_CurrentLocation();
		//				coor[1]= ca.getCurrentLocation();
		//
		//				check_line chline= new check_line();
		//				GeometryFactory fac = new GeometryFactory();
		//				Geometry geom = fac.createLineString(coor) ;
		//				context.add(chline);
		//				geography.move( chline, geom);
		//				check_line_list.add(chline);
		//				//check if it intersect with building
		//				boolean validline=true;						
		//				for (TopographicArea buld :  nearObjects)
		//					if  ( geom.intersects(geography.getGeometry(buld)))
		//					{  	
		//						validline=false ; 
		//						break;
		//					}
		//
		//				//2- if valid line
		//				if( validline)
		//				{
		//
		//					//===========================================
		//					//Search Action
		//					if (!ckeckcount)
		//					{
		//
		//						double discas=BuildStaticFuction.DistanceC(geography, this.Return_CurrentLocation(), ca.getCurrentLocation());
		//
		//						if(	discas < minDistance   && !Yesckeck_Severity   )  
		//						{						
		//							TargetCasualty= ca;
		//							minDistance =discas;
		//							//System.out.println("responder " + this.Id + " targetCasualty : " + TargetCasualty.ID + " dis  : " + discas )  ;
		//						}
		//						//-----------------------
		//						else if (   ca. Triage_tage <= maxpriority_level    && Yesckeck_Severity   )
		//						{							
		//							if (ca. Triage_tage < maxpriority_level  )
		//
		//							{
		//								TargetCasualty= ca;
		//								maxpriority_level=ca. Triage_tage ;
		//								minDistance =discas;
		//							}
		//							else
		//							{	
		//								if(	discas < minDistance)
		//								{     
		//									TargetCasualty= ca;
		//									maxpriority_level=ca. Triage_tage ;
		//									minDistance =discas;
		//								}
		//
		//							}
		//
		//						}
		//						//-----------------------
		//					}
		//					//===========================================
		//					//Count Action
		//					if (ckeckcount) {
		//						//for (Object_Zone zone : AssessZoneAgents) 
		//						//if ( zone.lookForObject(ca)  )  
		//						//{zone.Addonecasulty(); Lastcoordinationcasualty_list.add(ca);}
		//
		//						if ( ! CasualtySeen_list.contains(ca))
		//							CasualtySeen_list.add(ca);
		//					}		 
		//
		//
		//				} //end validline
		//
		//
		//			}// end for
		//		} 
		//		else
		//		{
		//		}// end if ###############
		//
		//
		//		//xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
		//		// after selection of the bestZone , calculate result direction angle
		//		if (TargetCasualty != null && !ckeckcount  ) 
		//		{
		//
		//			RandomllySearachCasualty= false;
		//			SensingeEnvironmentTrigger=RespondersTriggers.SensedCasualty;
		//
		//			if ( ! CasualtySeen_list.contains(TargetCasualty)) //// !!!!!!!!!!!!!!!!!! without treatment 
		//				CasualtySeen_list.add(TargetCasualty);
		//
		//
		//			check_line_list.clear();
		//
		//			//TargetCasualty.ColorCode=6;	
		//			//System.out.println(  this.Id + " targetCasualty : " + TargetCasualty.ID )  ;
		//
		//		}	
	}

	private void Search_RandomlyCasualty_new1(  boolean Yesckeck_Severity ,boolean ckeckcount ) {


		CounterSetep++;   // I used this when filrefirghter not found triage casulties can sart look for untiage aloso ??

		TargetCasualty=null;
		minDistance = 10000000;
		int maxpriority_level = 7;
		List<Casualty> Casualtyun = new ArrayList<Casualty>() ;
		List<Casualty> Casualtytrap = new ArrayList<Casualty>() ;
		List<check_line> check_line_list = new ArrayList<check_line>();

		//xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
		//1- Get All Near Geographical Objects
		@SuppressWarnings("unchecked") //@SuppressWarnings("unchecked") tells the compiler that the programmer believes the code to be safe and won't cause unexpected exceptions.
		List<TopographicArea> nearObjects = (List<TopographicArea>) BuildStaticFuction.GetObjectsWithinDistance(this,TopographicArea.class, 100);

		//2- Remove all non-building objects to get a list of obstacles such as building 
		for (int i = 0; i < nearObjects.size(); i++) {
			if (nearObjects.get(i).getcode() != 4) {
				nearObjects.remove(i);
				i--; //Because if delete one object , next object not yet checked will take the previous number
			}
		}
		//xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
		//temp action 
		double filedofVision=100 ;
		//if (Prvusedinprint == Ambulance_ResponderRole.Paramedic && ! ExpandFieldofVision )  filedofVision= 20;

		if (  this.PrvRoleinprint1 == Ambulance_ResponderRole.AmbulanceSectorCommander  && ! ExpandFieldofVision) filedofVision= 30;
		if (  this.PrvRoleinprint2 == Fire_ResponderRole.FireSectorCommander  && ! ExpandFieldofVision) filedofVision= 30;

		//xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx 
		//3- Get All Near casualty Objects		
		@SuppressWarnings("unchecked") 
		List<Casualty> nearObjects_Casualty = (List<Casualty>) BuildStaticFuction.GetObjectsWithinDistance(this,Casualty.class,filedofVision );

		//Temp soultion after Last meeting with Phill
		//List<Casualty> nearObjects_Casualty = this.assignedIncident.casualties ;

		if ( !ckeckcount ) //	sector	  assignedIncident.TaskMechanism_IN==TaskAllocationMechanism.Environmentsectorization  &&		
			nearObjects_Casualty = (List<Casualty>) this.AssignedSector.GetObjectsWithinSector(nearObjects_Casualty );

		//xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx

		if (! Yesckeck_Severity  &&  !ckeckcount ) 
		{					
			// 1=== 
			if ( this instanceof Responder_Ambulance )   //Field Triage  used
			{
				for (int i = 0; i < nearObjects_Casualty.size(); i++) {
					if (  CasualtySeen_list.contains(nearObjects_Casualty.get(i))   ||nearObjects_Casualty.get(i).IsAssignedToResponder== true  ||nearObjects_Casualty.get(i).Status==CasualtyStatus.Trapped || nearObjects_Casualty.get(i).Status==CasualtyStatus.PreTreatedTrapped||nearObjects_Casualty.get(i).Status==CasualtyStatus.Extracted  ||   nearObjects_Casualty.get(i).Status==CasualtyStatus.Triaged ||  nearObjects_Casualty.get(i).UnInjured_NOtYetStartowalk>=0 )  //nearObjects_Casualty.get(i).Status==CasualtyStatus.PreTreated ||
					{
						{ nearObjects_Casualty.remove(i); i--; }
					}
				}
			}

			// 2=== 
			if ( this instanceof Responder_Police )  //Not used
			{
				for (int i = 0; i < nearObjects_Casualty.size(); i++) {
					if (  CasualtySeen_list.contains(nearObjects_Casualty.get(i))   ||nearObjects_Casualty.get(i).IsAssignedToResponder== true ||  nearObjects_Casualty.get(i).UnInjured_NOtYetStartowalk==0  || nearObjects_Casualty.get(i).IGetInstactionToWalk_and_walking==true    ){
						{ nearObjects_Casualty.remove(i); i--; }
					}
				}

				// not in sctors
				for (int i = 0; i < nearObjects_Casualty.size(); i++) {
					for (Sector  S : this.assignedIncident.Sectorlist2 )

						if ( S.AmIinSector(nearObjects_Casualty.get(i)  ) )	
						{ nearObjects_Casualty.remove(i); i--; }
				}

				// not in RC
				for (int i = 0; i < nearObjects_Casualty.size(); i++) {
					if ( AssignedTA.casualtiesinTA.contains(nearObjects_Casualty.get(i)))
					{ nearObjects_Casualty.remove(i); i--; }
				}
			}

		}
		//---------------------------------------------------
		else  if ( Yesckeck_Severity  &&  !ckeckcount ) 
		{
			// 3===  
			//			if ( this instanceof Responder_Ambulance ) // Treatment  old
			//			{
			//				for (int i = 0; i < nearObjects_Casualty.size(); i++) {
			//					if (  CasualtySeen_list.contains(nearObjects_Casualty.get(i)) ||nearObjects_Casualty.get(i).IsAssignedToResponder== true ||nearObjects_Casualty.get(i).Status!=CasualtyStatus.Triaged   || nearObjects_Casualty.get(i).ThisFatlity  ){
			//						{ nearObjects_Casualty.remove(i); i--; }
			//					}
			//				}
			//			}

			//4====
			if ( this instanceof Responder_Fire ) // Carry   used
			{
				List<Casualty> nearObjects_Casualty2= new ArrayList<Casualty>();

				for (int j = 0; j < nearObjects_Casualty.size(); j++) {
					if ( ! CasualtySeen_list.contains(nearObjects_Casualty.get(j))&& nearObjects_Casualty.get(j).IsAssignedToResponder== false && ! nearObjects_Casualty.get(j).DeadByFire  &&  ! nearObjects_Casualty.get(j).IGetInstactionToWalk_and_walking  && ( nearObjects_Casualty.get(j).Status==CasualtyStatus.Triaged || nearObjects_Casualty.get(j).Status==CasualtyStatus.Trapped ||  nearObjects_Casualty.get(j).UnInjured_NOtYetStartowalk==0 )  )
					{ nearObjects_Casualty2.add(nearObjects_Casualty.get(j)); }
				}

				nearObjects_Casualty=nearObjects_Casualty2 ;

				//System.out.println(nearObjects_Casualty.size() );
				// if (nearObjects_Casualty.size()==0  )
				// {
				// nearObjects_Casualty = (List<Casualty>) this.AssignedSector.GetObjectsWithinSector(nearObjects_Casualty );

				//	 for (int i = 0; i < nearObjects_Casualty.size(); i++) 
				//		if (nearObjects_Casualty.get(i).Outsidebulding==false ||  CasualtySeen_list.contains(nearObjects_Casualty.get(i)) ||nearObjects_Casualty.get(i).IsAssignedToResponder== true      ){
				//		{ nearObjects_Casualty.remove(i); i--; }
				//		 }

			}


		}
		//xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
		// 4- search the nearest casualty and there is no obstacle

		if  (nearObjects_Casualty.size()> 0) //begin if ###############
		{							 	 
			for (Casualty ca :  nearObjects_Casualty)
			{		

				//1- create line to check 				
				coor[0]= this.Return_CurrentLocation();
				coor[1]= ca.getCurrentLocation();
				Geometry geom = fac.createLineString(coor) ;
				geography.move( chline, geom);
				check_line_list.add(chline);

				//check if it intersect with building
				boolean validline=true;						
				for (TopographicArea buld :  nearObjects)
					if  ( geom.intersects(geography.getGeometry(buld)))
					{  	
						validline=false ; 
						break;
					}				



				//				//1- create line to check 
				//				Coordinate[] coor = new Coordinate[2];
				//				coor[0]= this.Return_CurrentLocation();
				//				coor[1]= ca.getCurrentLocation();
				//
				//				check_line chline= new check_line();
				//				GeometryFactory fac = new GeometryFactory();
				//				Geometry geom = fac.createLineString(coor) ;
				//				context.add(chline);
				//				geography.move( chline, geom);
				//				check_line_list.add(chline);
				//				//check if it intersect with building
				//				boolean validline=true;						
				//				for (TopographicArea buld :  nearObjects)
				//					if  ( geom.intersects(geography.getGeometry(buld)))
				//					{  	
				//						validline=false ; 
				//						break;
				//					}

				//2- if valid line
				if( validline)
				{

					//===========================================
					//Search Action
					if (!ckeckcount)
					{

						double discas=BuildStaticFuction.DistanceC(geography, this.Return_CurrentLocation(), ca.getCurrentLocation());

						if(	discas < minDistance   && !Yesckeck_Severity   )  
						{						
							TargetCasualty= ca;
							minDistance =discas;
							//System.out.println("responder " + this.Id + " targetCasualty : " + TargetCasualty.ID + " dis  : " + discas )  ;
						}

						//-----------------------
						else if (   ca.Triage_tage <= maxpriority_level  && Yesckeck_Severity   )
						{							
							if (ca.Triage_tage < maxpriority_level  )

							{
								TargetCasualty= ca;
								maxpriority_level=ca.Triage_tage ;
								minDistance =discas;
							}
							else
							{	
								if(	discas < minDistance)
								{     
									TargetCasualty= ca;
									maxpriority_level=ca.Triage_tage ;
									minDistance =discas;
								}

							}

						}
						//-----------------------
					}
					//===========================================
					//Count Action
					if (ckeckcount) {
						//for (Object_Zone zone : AssessZoneAgents) 
						//if ( zone.lookForObject(ca)  )  
						//{zone.Addonecasulty(); Lastcoordinationcasualty_list.add(ca);}

						if ( ! CasualtySeen_list.contains(ca))
							CasualtySeen_list.add(ca);
					}
					//===========================================
					// direct casulties
					// unjuried

					if (  ca.UnInjured_NOtYetStartowalk==0  && !ckeckcount ) 
					{
						Casualtyun.add(ca) ;
					}

					if (  ca.Status==CasualtyStatus.Trapped   && !ckeckcount ) 
					{
						Casualtytrap.add(ca) ;
					}

				} //end validline


			}// end for
		} 


		// direct casulties
		if ( this instanceof Responder_Fire && !ckeckcount ) // Carry   used
		{

			for (Casualty ca:  Casualtyun )
			{
				double discas=BuildStaticFuction.DistanceC(geography, this.Return_CurrentLocation(), ca.getCurrentLocation());

				if(	discas < minDistance     )  
				{						
					TargetCasualty= ca;
					minDistance =discas;
					//System.out.println("responder " + this.Id + " targetCasualty : " + TargetCasualty.ID + " dis  : " + discas )  ;
				}
			}

			for (Casualty ca:  Casualtytrap )
			{
				double discas=BuildStaticFuction.DistanceC(geography, this.Return_CurrentLocation(), ca.getCurrentLocation());

				if(	discas < minDistance     )  
				{						
					TargetCasualty= ca;
					minDistance =discas;
					//System.out.println("responder " + this.Id + " targetCasualty : " + TargetCasualty.ID + " dis  : " + discas )  ;
				}
			}

		}

		//xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
		// after selection of the bestZone , calculate result direction angle
		if (TargetCasualty != null && !ckeckcount  ) 
		{

			RandomllySearachCasualty= false;
			SensingeEnvironmentTrigger=RespondersTriggers.SensedCasualty;

			if ( this.PrvRoleinprint1 == Ambulance_ResponderRole.AmbulanceSectorCommander ||  PrvRoleinprint2 == Fire_ResponderRole.FireSectorCommander)
			{
				// do nothing
			}
			else
			{
				if ( ! CasualtySeen_list.contains(TargetCasualty)    ) CasualtySeen_list.add(TargetCasualty);
			}

			//check_line_list.clear();

			//TargetCasualty.ColorCode=6;	
			//System.out.println(  this.Id + " targetCasualty : " + TargetCasualty.ID )  ;

		}	

		nearObjects.clear();
		nearObjects_Casualty.clear();
		nearObjects=null;
		nearObjects_Casualty=null;

		for (check_line cl:  check_line_list )
			this.context.remove(cl);

		check_line_list.clear();
		check_line_list=null;

	}

	private void Search_RandomlyCasualty_new2(  boolean Yesckeck_Severity ,boolean ckeckcount ) {


		CounterSetep++;   // I used this when filrefirghter not found triage casulties can sart look for untiage aloso ??

		TargetCasualty=null;
		minDistance = 10000000;
		int maxpriority_level = 7;
		List<Casualty> Casualtyun = new ArrayList<Casualty>() ;
		List<Casualty> Casualtytrap = new ArrayList<Casualty>() ;

		//Temp soultion after Last meeting with Phill
		List<TopographicArea> nearObjects = this.assignedIncident.nearObjects_Build_small ;		
		List<Casualty> nearObjects_Casualty =  null;

		if ( !ckeckcount ) //	sector	  assignedIncident.TaskMechanism_IN==TaskAllocationMechanism.Environmentsectorization  &&		
			nearObjects_Casualty = (List<Casualty>) this.AssignedSector.GetObjectsWithinSector(this.assignedIncident.casualties  );
		else
		{
			nearObjects_Casualty = new ArrayList<Casualty>() ;
			for (  Casualty  ca:  this.assignedIncident.casualties )
				nearObjects_Casualty.add(ca);
		}

		//xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx

		if (! Yesckeck_Severity  &&  !ckeckcount ) 
		{					
			// 1=== 
			if ( this instanceof Responder_Ambulance )   //Field Triage  used
			{
				for (int i = 0; i < nearObjects_Casualty.size(); i++) {
					if (  CasualtySeen_list.contains(nearObjects_Casualty.get(i))   ||nearObjects_Casualty.get(i).IsAssignedToResponder== true  ||nearObjects_Casualty.get(i).Status==CasualtyStatus.Trapped || nearObjects_Casualty.get(i).Status==CasualtyStatus.PreTreatedTrapped||nearObjects_Casualty.get(i).Status==CasualtyStatus.Extracted  ||   nearObjects_Casualty.get(i).Status==CasualtyStatus.Triaged ||  nearObjects_Casualty.get(i).UnInjured_NOtYetStartowalk>=0 )  //nearObjects_Casualty.get(i).Status==CasualtyStatus.PreTreated ||
					{
						{ nearObjects_Casualty.remove(i); i--; }
					}
				}
			}

			// 2=== 
			if ( this instanceof Responder_Police )  //Not used
			{
				for (int i = 0; i < nearObjects_Casualty.size(); i++) {
					if (  CasualtySeen_list.contains(nearObjects_Casualty.get(i))   ||nearObjects_Casualty.get(i).IsAssignedToResponder== true ||  nearObjects_Casualty.get(i).UnInjured_NOtYetStartowalk==0  || nearObjects_Casualty.get(i).IGetInstactionToWalk_and_walking==true    ){
						{ nearObjects_Casualty.remove(i); i--; }
					}
				}

				// not in sctors
				for (int i = 0; i < nearObjects_Casualty.size(); i++) {
					for (Sector  S : this.assignedIncident.Sectorlist2 )

						if ( S.AmIinSector(nearObjects_Casualty.get(i)  ) )	
						{ nearObjects_Casualty.remove(i); i--; }
				}

				// not in RC
				for (int i = 0; i < nearObjects_Casualty.size(); i++) {
					if ( AssignedTA.casualtiesinTA.contains(nearObjects_Casualty.get(i)))
					{ nearObjects_Casualty.remove(i); i--; }
				}
			}

		}
		//---------------------------------------------------
		else  if ( Yesckeck_Severity  &&  !ckeckcount ) 
		{

			if ( this instanceof Responder_Fire ) // Carry   used
			{
				List<Casualty> nearObjects_Casualty2= new ArrayList<Casualty>();

				for (int j = 0; j < nearObjects_Casualty.size(); j++) {
					if ( ! CasualtySeen_list.contains(nearObjects_Casualty.get(j))&& nearObjects_Casualty.get(j).IsAssignedToResponder== false && ! nearObjects_Casualty.get(j).DeadByFire  &&  ! nearObjects_Casualty.get(j).IGetInstactionToWalk_and_walking  && ( nearObjects_Casualty.get(j).Status==CasualtyStatus.Triaged || nearObjects_Casualty.get(j).Status==CasualtyStatus.Trapped ||  nearObjects_Casualty.get(j).UnInjured_NOtYetStartowalk==0 )  )
					{ nearObjects_Casualty2.add(nearObjects_Casualty.get(j)); }
				}

				nearObjects_Casualty=nearObjects_Casualty2 ;

			}


		}
		//xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
		// 4- search the nearest casualty and there is no obstacle

		if  (nearObjects_Casualty.size()> 0) //begin if ###############
		{							 	 
			for (Casualty ca :  nearObjects_Casualty)
			{		

				//				//1- create line to check 				
				//				coor[0]= this.Return_CurrentLocation();
				//				coor[1]= ca.getCurrentLocation();
				//				Geometry geom = fac.createLineString(coor) ;
				//				geography.move( chline, geom);
				//				check_line_list.add(chline);
				//
				//				//check if it intersect with building
				//				boolean validline=true;						
				//				for (TopographicArea buld :  nearObjects)
				//					if  ( geom.intersects(geography.getGeometry(buld)))
				//					{  	
				//						validline=false ; 
				//						break;
				//					}

				boolean validline=true;	
				//2- if valid line
				if( validline)
				{

					//===========================================
					//Search Action
					if (!ckeckcount)
					{

						double discas=BuildStaticFuction.DistanceC(geography, this.Return_CurrentLocation(), ca.getCurrentLocation());

						if(	discas < minDistance   && !Yesckeck_Severity   )  
						{						
							TargetCasualty= ca;
							minDistance =discas;
							//System.out.println("responder " + this.Id + " targetCasualty : " + TargetCasualty.ID + " dis  : " + discas )  ;
						}

						//-----------------------
						else if (   ca.Triage_tage <= maxpriority_level  && Yesckeck_Severity   )
						{							
							if (ca.Triage_tage < maxpriority_level  )

							{
								TargetCasualty= ca;
								maxpriority_level=ca.Triage_tage ;
								minDistance =discas;
							}
							else
							{	
								if(	discas < minDistance)
								{     
									TargetCasualty= ca;
									maxpriority_level=ca.Triage_tage ;
									minDistance =discas;
								}

							}

						}
						//-----------------------
					}
					//===========================================
					//Count Action
					if (ckeckcount) {
						//for (Object_Zone zone : AssessZoneAgents) 
						//if ( zone.lookForObject(ca)  )  
						//{zone.Addonecasulty(); Lastcoordinationcasualty_list.add(ca);}

						if ( ! CasualtySeen_list.contains(ca))
							CasualtySeen_list.add(ca);
					}
					//===========================================
					// direct casulties
					// unjuried

					if (  ca.UnInjured_NOtYetStartowalk==0  && !ckeckcount ) 
					{
						Casualtyun.add(ca) ;
					}

					if (  ca.Status==CasualtyStatus.Trapped   && !ckeckcount ) 
					{
						Casualtytrap.add(ca) ;
					}

				} //end validline


			}// end for
		} 


		// direct casulties
		if ( this instanceof Responder_Fire && !ckeckcount ) // Carry   used
		{

			for (Casualty ca:  Casualtyun )
			{
				double discas=BuildStaticFuction.DistanceC(geography, this.Return_CurrentLocation(), ca.getCurrentLocation());

				if(	discas < minDistance     )  
				{						
					TargetCasualty= ca;
					minDistance =discas;
					//System.out.println("responder " + this.Id + " targetCasualty : " + TargetCasualty.ID + " dis  : " + discas )  ;
				}
			}

			for (Casualty ca:  Casualtytrap )
			{
				double discas=BuildStaticFuction.DistanceC(geography, this.Return_CurrentLocation(), ca.getCurrentLocation());

				if(	discas < minDistance     )  
				{						
					TargetCasualty= ca;
					minDistance =discas;
					//System.out.println("responder " + this.Id + " targetCasualty : " + TargetCasualty.ID + " dis  : " + discas )  ;
				}
			}

		}

		//xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
		// after selection of the bestZone , calculate result direction angle
		if (TargetCasualty != null && !ckeckcount  ) 
		{

			RandomllySearachCasualty= false;
			SensingeEnvironmentTrigger=RespondersTriggers.SensedCasualty;

			if ( this.PrvRoleinprint1 == Ambulance_ResponderRole.AmbulanceSectorCommander ||  PrvRoleinprint2 == Fire_ResponderRole.FireSectorCommander)
			{
				// do nothing
			}
			else
			{
				if ( ! CasualtySeen_list.contains(TargetCasualty)    ) CasualtySeen_list.add(TargetCasualty);
			}



			//check_line_list.clear();

			//TargetCasualty.ColorCode=6;	
			//System.out.println(  this.Id + " targetCasualty : " + TargetCasualty.ID )  ;

		}	

		//System.out.println( "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx " + nearObjects_Casualty.size() +" xxxxxxxxxxxxxx " + nearObjects.size()+"  " + check_line_list.size());

		nearObjects_Casualty.clear();
		nearObjects_Casualty=null;

		for (check_line cl:  check_line_list )
			this.context.remove(cl);
		check_line_list.clear();

	}


	public void Search_RandomlyResponders(   ) {

		TargetResponder=null;
		minDistance = 10000000;
		List<check_line> check_line_list = new ArrayList<check_line>();

		//xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
		//1- Get All Near Geographical Objects
		@SuppressWarnings("unchecked") //@SuppressWarnings("unchecked") tells the compiler that the programmer believes the code to be safe and won't cause unexpected exceptions.
		List<TopographicArea> nearObjects = (List<TopographicArea>) BuildStaticFuction.GetObjectsWithinDistance(this,TopographicArea.class, 50);

		//2- Remove all non-building objects to get a list of obstacles such as building 
		for (int i = 0; i < nearObjects.size(); i++) {
			if (nearObjects.get(i).getcode() != 4) 
			{nearObjects.remove(i);i--; }
		}
		//xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
		//temp action 
		double filedofVision=10 ;
		//if (Prvusedinprint1 == Ambulance_ResponderRole.Paramedic && ! ExpandFieldofVision )  filedofVision= 20;

		//xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
		//3- Get All Near casualty Objects		
		@SuppressWarnings("unchecked") 
		List<Responder_Fire> nearObjects_Responder = (List<Responder_Fire>) BuildStaticFuction.GetObjectsWithinDistance(this,Responder_Fire.class,  filedofVision );

		if (assignedIncident.TaskMechanism_IN==TaskAllocationMechanism.Environmentsectorization  ) //	sector			
			nearObjects_Responder = (List<Responder_Fire>) this.AssignedSector.GetObjectsWithinSector(nearObjects_Responder );

		//xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx

		if ( this instanceof Responder_Ambulance )   
		{
			for (int i = 0; i < nearObjects_Responder.size(); i++) {
				if (nearObjects_Responder.get(i).OnhandCasualty != null ||  ResponderSeen_list.contains(nearObjects_Responder.get(i))   || nearObjects_Responder.get(i).Role!= Fire_ResponderRole.FireFighter  )		
				{ nearObjects_Responder.remove(i); i--; }

			}
		}


		//xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
		// 4- search the nearest casualty and there is no obstacle

		if  (nearObjects_Responder.size()> 0) //begin if ###############
		{							 	 
			for (Responder_Fire Res :  nearObjects_Responder)
			{		

				//1- create line to check 
				Coordinate[] coor = new Coordinate[2];
				coor[0]= this.Return_CurrentLocation();
				coor[1]= Res.Return_CurrentLocation() ;

				check_line chline= new check_line();
				GeometryFactory fac = new GeometryFactory();
				Geometry geom = fac.createLineString(coor) ;
				context.add(chline);
				geography.move( chline, geom);
				check_line_list.add(chline);
				//check if it intersect with building
				boolean validline=true;						
				for (TopographicArea buld :  nearObjects)
					if  ( geom.intersects(geography.getGeometry(buld)))
					{  	
						validline=false ; 
						break;
					}

				//2- if valid line
				if( validline)
				{
					double discas=BuildStaticFuction.DistanceC(geography, this.Return_CurrentLocation(), Res.Return_CurrentLocation());

					if(	discas < minDistance  &&   discas <= 5  )  
					{						
						TargetResponder= Res ;
						minDistance =discas;
						//System.out.println("responder " + this.Id + " targetCasualty : " + TargetCasualty.ID + " dis  : " + discas )  ;
					}
				} //end validline
			}// end for
		} 

		//xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
		// after selection of the bestZone , calculate result direction angle
		if (TargetResponder != null   ) 
		{
			RandomllySearachResponders= false;
			SensingeEnvironmentTrigger=RespondersTriggers.SensedFireFighterResponder ;

			if ( !  this.ResponderSeen_list.contains(TargetResponder)) //// !!!!!!!!!!!!!!!!!! 
				ResponderSeen_list.add((Responder_Fire) TargetResponder) ;

			check_line_list.clear();
			TargetResponder.ColorCode=6;
			//TargetCasualty.ColorCode=6;	
			//System.out.println(  this.Id + " targetCasualty : " + TargetCasualty.ID )  ;
		}	
	}

	//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
	private void Search_RandomlyRoute_old() {

		this.TargetRoute =null;
		minDistance = 10000000;
		List<check_line> check_line_list = new ArrayList<check_line>();

		//xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
		//1- Get All Near Geographical Objects
		//@SuppressWarnings("unchecked") 
		//List<TopographicArea> nearObjects = (List<TopographicArea>) BuildStaticFuction.GetObjectsWithinDistance(this,TopographicArea.class, 300);
		//2- Remove all non-building objects to get a list of obstacles such as building 
		//for (int i = 0; i < nearObjects.size(); i++) {
		//	if (nearObjects.get(i).getcode() != 4) {
		//		nearObjects.remove(i);
		//		i--; //Because if delete one object , next object not yet checked will take the previous number
		//	}
		// }
		//xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
		//2- Get All Near RL Objects
		//@SuppressWarnings("unchecked") 
		//double filedofVision=500 ;
		//List<RoadLink> nearObjects_RL = (List<RoadLink>) BuildStaticFuction.GetObjectsWithinDistance(this,RoadLink.class, filedofVision);

		//Temp soultion after Last meeting with Phill
		List<RoadLink> nearObjects_RL=  this.assignedIncident.nearObjects_RLwithTrandWR_inside ;
		List<TopographicArea> nearObjects =  this.assignedIncident.nearObjects_Build_big;

		System.out.println( "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx " + nearObjects_RL.size() +" xxxxxxxxxxxxxx " + nearObjects.size());

		//xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
		//3-	

		if ( this instanceof Responder_Fire ) 
		{
			for (int i = 0; i < nearObjects_RL.size(); i++) {
				if (nearObjects_RL.get(i).WreckageLevel ==Level.None )  //|| RouteSeen_list.contains(nearObjects_RL.get(i)
				{ nearObjects_RL.remove(i); i--; }

			}
		}

		if ( this instanceof Responder_Police )  
		{
			for (int i = 0; i < nearObjects_RL.size(); i++) {
				if (nearObjects_RL.get(i).TrafficLevel ==Level.None   ) // RouteSeen_list.contains(nearObjects_RL.get(i)
				{ nearObjects_RL.remove(i); i--; }

			}
		}	

		//System.out.println(  this.Id + " size " + nearObjects_RL.size())  ;
		//xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
		// 4- search the nearest RL and there is no obstacle

		if  (nearObjects_RL.size()> 0) //begin if ###############
		{							 	 
			for (RoadLink RL :  nearObjects_RL)
			{		
				//1- create line to check 
				//				Coordinate[] coor = new Coordinate[2];
				//				coor[0]= this.Return_CurrentLocation();
				//				coor[1]= RL.PointofClear.getCurrentPosition();
				//
				//				check_line chline= new check_line();
				//				GeometryFactory fac = new GeometryFactory();
				//				Geometry geom = fac.createLineString(coor) ;
				//				context.add(chline);
				//				geography.move( chline, geom);
				//				check_line_list.add(chline);
				//				//check if it intersect with building
				//				boolean validline=true;						
				//				for (TopographicArea buld :  nearObjects)
				//					if  ( geom.intersects(geography.getGeometry(buld)))
				//					{  	
				//						validline=false ; 
				//						break;
				//					}

				coor[0]= this.Return_CurrentLocation();
				coor[1]= RL.PointofClear.getCurrentPosition();
				Geometry geom = fac.createLineString(coor) ;
				geography.move( chline, geom);
				check_line_list.add(chline);

				boolean validline=true;						
				for (TopographicArea buld :  nearObjects)
					if  ( geom.intersects(geography.getGeometry(buld)))
					{  	
						validline=false ; 
						break;
					}

				//2- if valid line
				if( validline)
				{

					double dis=BuildStaticFuction.DistanceC(geography, this.Return_CurrentLocation(), RL.PointofClear.getCurrentPosition());

					if ( TargetRoute !=null )
					{

						if ( TargetRoute.InsideCordon==false  && RL.InsideCordon==true  )
						{
							TargetRoute= RL;
							minDistance =dis;
						}
						else if ( TargetRoute.InsideCordon==true  && RL.InsideCordon==false  )
						{
							; //nothing
						}
						else if ( TargetRoute.InsideCordon==true  && RL.InsideCordon==true )
						{
							if(	dis < minDistance   )  
							{						
								TargetRoute= RL;
								minDistance =dis;
							}	
						}
						else if ( TargetRoute.InsideCordon==false && RL.InsideCordon==false )
						{

							if(	dis < minDistance   )  
							{						
								TargetRoute= RL;
								minDistance =dis;
							}	
						}


					}
					else
					{
						if(	dis < minDistance   )  
						{						
							TargetRoute= RL;
							minDistance =dis;
						}	

					}


				} //end validline


			}// end for
		} 

		//xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx

		if (TargetRoute != null  ) 
		{
			RandomllySearachRoute= false;
			SensingeEnvironmentTrigger=RespondersTriggers.SensedRoute;
			//RouteSeen_list.add(TargetRoute);


			if ( this instanceof Responder_Police )  System.out.println( "                                                                                     "+"Cordon: " + this.Id + " targetroute : " + TargetRoute.TrafficLevel   +TargetRoute.EstimatedTimeTraffic)  ;
			if ( this instanceof Responder_Fire )   System.out.println( "                                       "  +"WrK: " + this.Id + " targetroute : " + TargetRoute.WreckageLevel  +TargetRoute.EstimatedTimeWreckage)  ;
			//TargetRoute.ColorCode=2;
		}




		nearObjects.clear();
		nearObjects_RL.clear();		
		nearObjects=null;
		nearObjects_RL=null;		
		for (check_line cl:  check_line_list )
			this.context.remove(cl);		
		check_line_list.clear();
		check_line_list=null;

	}

	private void Search_RandomlyRoute_new2() {

		this.TargetRoute =null;
		minDistance = 10000000;


		//Temp soultion after Last meeting with Phill
		List<RoadLink> nearObjects_RL=  this.assignedIncident.nearObjects_RLwithTrandWR_inside ;
		List<TopographicArea> nearObjects =  this.assignedIncident.nearObjects_Build_big;


		//xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
		// 4- search the nearest RL and there is no obstacle
		if  (nearObjects_RL.size()> 0) //begin if ###############
		{							 	 
			for (RoadLink RL :  nearObjects_RL)
			{
				if ( (this instanceof Responder_Fire &&  RL.WreckageLevel !=Level.None )  || (this instanceof Responder_Police && RL.TrafficLevel !=Level.None   ) )					
				{		
					coor[0]= this.Return_CurrentLocation();
					coor[1]= RL.PointofClear.getCurrentPosition();

					Geometry geom = fac.createLineString(coor) ;
					geography.move( chline, geom);
					check_line_list.add(chline);

					boolean validline=true;						
					for (TopographicArea buld :  nearObjects)
						if  ( geom.intersects(geography.getGeometry(buld)))
						{  	
							validline=false ; 
							break;
						}

					//2- if valid line
					if( validline)
					{

						double dis=BuildStaticFuction.DistanceC(geography, this.Return_CurrentLocation(), RL.PointofClear.getCurrentPosition());

						if ( TargetRoute !=null )
						{

							if ( TargetRoute.InsideCordon==false  && RL.InsideCordon==true  )
							{
								TargetRoute= RL;
								minDistance =dis;
							}
							else if ( TargetRoute.InsideCordon==true  && RL.InsideCordon==false  )
							{
								; //nothing
							}
							else if ( TargetRoute.InsideCordon==true  && RL.InsideCordon==true )
							{
								if(	dis < minDistance   )  
								{						
									TargetRoute= RL;
									minDistance =dis;
								}	
							}
							else if ( TargetRoute.InsideCordon==false && RL.InsideCordon==false )
							{

								if(	dis < minDistance   )  
								{						
									TargetRoute= RL;
									minDistance =dis;
								}	
							}


						}
						else
						{
							if(	dis < minDistance   )  
							{						
								TargetRoute= RL;
								minDistance =dis;
							}	

						}


					} //end validline

				}// if
			}// end for
		} 

		//xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx

		if (TargetRoute != null  ) 
		{
			RandomllySearachRoute= false;
			SensingeEnvironmentTrigger=RespondersTriggers.SensedRoute;
			//RouteSeen_list.add(TargetRoute);


			//	if ( this instanceof Responder_Police )  System.out.println( "                                                                                     "+"Cordon: " + this.Id + " targetroute : " + TargetRoute.TrafficLevel   +TargetRoute.EstimatedTimeTraffic)  ;
			//	if ( this instanceof Responder_Fire )   System.out.println( "                                       "  +"WrK: " + this.Id + " targetroute : " + TargetRoute.WreckageLevel  +TargetRoute.EstimatedTimeWreckage)  ;
			//TargetRoute.ColorCode=2;
		}

		//System.out.println( "xxxxxxxxxxxxxxxxxxxxRxxxxxxxxxxxxxxxxxxxx " + nearObjects_RL.size() +" xxxxxxxxxxxxxx " + nearObjects.size() + "   " + check_line_list.size());

		for (check_line cl:  check_line_list )
			this.context.remove(cl);

		check_line_list.clear();

	}

	//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
	public void Setup_ConsiderSeverityofcasualtyinSearach(boolean  _CheckCasualtyStatus )
	{
		this.CheckCasualtyStatusDuringSearch=_CheckCasualtyStatus;
	}

	public void Reset_DirectioninSearach( )
	{
		DirL=false; 
		DirR=false ;
		DirU=false ;
		DirD=false ;
	}

	public void Set_DirectioninSearach( )
	{
		DirL=true; 
		DirR=true;
		DirU=true;
		DirD=true;
	}

	public Coordinate getDestinationLocation() {
		return DestinationLocation;
	}

	public Coordinate Return_CurrentLocation()
	{
		return (geography.getGeometry(this)).getCoordinate();		
	}

	public void setLocation_and_move(Coordinate newPosition)
	{

		Coordinate  oldPosition= this.Return_CurrentLocation();

		// so we move agent 
		double x = (newPosition.x - oldPosition.x);
		double y = (newPosition.y - oldPosition.y);

		geography.moveByDisplacement(this, x, y);		

	}

	public void Move_related_zones(Coordinate oldPosition ,Coordinate newPosition )
	{
		// so we move agent and all search zones and safe zone to exact position
		double x = (newPosition.x - oldPosition.x);
		double y = (newPosition.y - oldPosition.y);

		for (Object_Zone zone : DirectionZoneAgents) {
			zone.SidePoint.x += x;
			zone.SidePoint.y += y;
			geography.moveByDisplacement(zone, x, y);
		}

		geography.moveByDisplacement(safeZone, x, y);

		if ( AssessZoneAgents!=null )
			for (Object_Zone zone : AssessZoneAgents) {
				zone.SidePoint.x += x;
				zone.SidePoint.y += y;
				geography.moveByDisplacement(zone, x, y);
			}

	}

	public void Generate_responder_zones() {

		//--------------------------------------------------
		// 1- Spatial space of the body of responder
		safeZone = new Object_Zone(this);
		safeZone.setVisible(true);
		safeZone.setsafezone(1);// for visualization purposes
		Coordinate center = geography.getGeometry(this).getCoordinate();
		GeometricShapeFactory gsf = new GeometricShapeFactory();
		gsf.setCentre(center);
		//gsf.setSize(radius / 22);  //orginal
		//gsf.setSize(radius /40); //35 uesd befor
		gsf.setSize(radius /60); 
		gsf.setNumPoints(20);
		Polygon p = gsf.createCircle();		
		GeometryFactory fac = new GeometryFactory();
		context.add(safeZone);
		geography.move(safeZone, fac.createPolygon(p.getCoordinates()));

		//--------------------------------------------------
		// 2- Generate a list of direction search zone agents that will be used by the responder agent to detect nearby objects.

		DirectionZoneAgents = BuildStaticFuction.GenerateSearchZones(geography, context, center, numDirectionZone, radius, this);
		DirectionZoneRadius = BuildStaticFuction.DistanceC(geography, center, DirectionZoneAgents.get(0).SidePoint);
		//System.out.println(this.Id + "     DirectionZoneRadius in meter : " + DirectionZoneRadius);

	}

	public void Destroy_responder_Zones() {

		context.remove(safeZone);

		for (Object_Zone zone : DirectionZoneAgents) {
			context.remove(zone);
		}

	}

	//****************************************************************************************************************
	public void Generate_AssessZones() 
	{
		// Generate a list of assess search zone agents that will be used by the responder agent to detect nearby objects.
		Coordinate center = geography.getGeometry(this).getCoordinate();
		AssessZoneAgents= BuildStaticFuction.GenerateSearchZones(geography, context, center,  numAssessZone, AssessRadius, this);
		AssessZoneRadius = BuildStaticFuction.DistanceC(geography, center, DirectionZoneAgents.get(0).SidePoint);
		System.out.println(this.Id + "     AssessZoneRadius in meter : " + AssessZoneRadius);
	}

	public void Destroy_AssessZones() {

		for (Object_Zone zone : AssessZoneAgents) 
			context.remove(zone);
	}

	public Object_Zone Count_MaximumZonewithCasualty() {

		int max=-1;
		Object_Zone zonemax=null;
		for (Object_Zone zone : AssessZoneAgents) 
			if (zone.Totalcount >= max )
				zonemax=zone ;

		return zonemax;
	}

	//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
	boolean changedMechanism=false;
	boolean Commander_leaveLocation=false;

	public int SendMessage( ACL_Message msg  )  //in Sender   FaceToFace, RadioSystem,ISApplication 
	{
		boolean flag=false ;  
		int answer =0 ; // 1 send -1 pending  0 no answer

		//xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxInter Com xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
		if ( msg.ComMech== CommunicationMechanism.SharedTactical_RadioSystem_and_FaceToFace )
		{
			if ( (   this.PrvRoleinprint2 == Fire_ResponderRole.FireSectorCommander &&  ((Responder )msg.receiver) instanceof Responder_Ambulance  &&  ((Responder )msg.receiver).PrvRoleinprint1 == Ambulance_ResponderRole.AmbulanceSectorCommander   )||
					(   this.PrvRoleinprint1 == Ambulance_ResponderRole.AmbulanceSectorCommander  &&  ((Responder )msg.receiver) instanceof Responder_Fire  &&  ((Responder )msg.receiver).PrvRoleinprint2 == Fire_ResponderRole.FireSectorCommander   )||
					(   this.PrvRoleinprint3 == Police_ResponderRole.ReceptionCenterOfficer &&  ((Responder )msg.receiver) instanceof Responder_Ambulance  &&  ((Responder )msg.receiver).PrvRoleinprint1 == Ambulance_ResponderRole.CasualtyClearingOfficer   )||
					(   this.PrvRoleinprint1 == Ambulance_ResponderRole.CasualtyClearingOfficer &&  ((Responder )msg.receiver) instanceof Responder_Police  &&  ((Responder )msg.receiver).PrvRoleinprint3 == Police_ResponderRole.ReceptionCenterOfficer   ))
			{			
				if (this.assignedIncident.RadioSystem_CheckChanleFree(5,this ,(Responder) msg.receiver )==1 )
				{
					msg.ComMech=CommunicationMechanism.SharedTactical_RadioSystem;	
				}
				else if (this.assignedIncident.RadioSystem_CheckChanleFree(5,this,(Responder) msg.receiver)==0 )
				{ 
					msg.ComMech= CommunicationMechanism.FaceToFace;   
					changedMechanism=true ; 
				}
				//System.out.println(this.Id + ".........................................................................................................0..............start face to face " + wait_forPerviousReciver);		
			}
			else  //other commander
			{
				msg.ComMech=CommunicationMechanism.SharedTactical_RadioSystem;	
				//System.out.println(this.Id + ".........................................................................................................0..............start phone " );	
			}
		}
		//xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
		if (msg.ComMech== CommunicationMechanism.SharedTactical_RadioSystem_and_Phone )
		{

			if (this.assignedIncident.RadioSystem_CheckChanleFree(5,this,(Responder) msg.receiver)==1 )
				msg.ComMech=CommunicationMechanism.SharedTactical_RadioSystem;
			else if (this.assignedIncident.RadioSystem_CheckChanleFree(5,this,(Responder) msg.receiver)==0 )
			{
				//1- look for PC
				if ( PersonalContacts_List.contains(((Responder) msg.receiver))) 	 
					msg.ComMech=CommunicationMechanism.Phone;		
				else
				{ msg.ComMech=CommunicationMechanism.SharedTactical_RadioSystem;	IwantTokeepPC= true; }		
			}
		}


		//xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
		if (msg.ComMech== CommunicationMechanism.FaceToFace_and_Phone )
		{	
			//1- look for PC
			//			if ( PersonalContacts_List.contains(((Responder) msg.receiver))) 	 
			//				msg.ComMech=CommunicationMechanism.Phone;		
			//			else
			msg.ComMech=CommunicationMechanism.FaceToFace;
			changedMechanism=true ; 
			Commander_leaveLocation=true; 	//IwantTokeepPC= true;	
			if  (this.PrvRoleinprint2 == Fire_ResponderRole.FireSectorCommander && !(msg.receiver instanceof Responder_Ambulance && ((Responder )msg.receiver).PrvRoleinprint1 == Ambulance_ResponderRole.AmbulanceSectorCommander ) && CurrentAssignedMajorActivity_fr==Fire_ActivityType.SearchandRescueCasualty  ) 
				Commander_leaveLocation=false;
			if  (this.PrvRoleinprint1 == Ambulance_ResponderRole.AmbulanceSectorCommander&& !(msg.receiver instanceof Responder_Fire &&  ((Responder )msg.receiver).PrvRoleinprint2 == Fire_ResponderRole.FireSectorCommander )  )
				Commander_leaveLocation=false;

		}
		//xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxInter Com xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx

		//---------------------------------------
		// 1- check availability of current CommunicationMechanism
		switch (msg.ComMech) {	
		//========================================================
		case FaceToFace :
			double dis1= 0, dis2=0 ;

			if  ( ( this instanceof Responder_Ambulance && !changedMechanism && Bronzecommander_amb !=null && Bronzecommander_amb.Commander_leaveLocation &&  (CurrentAssignedMajorActivity_amb==Ambulance_ActivityType.CCSResponse || CurrentAssignedMajorActivity_amb==Ambulance_ActivityType.SceneResponse) )  ||				
					( this instanceof Responder_Fire && !changedMechanism && Bronzecommander_fr !=null   && Bronzecommander_fr.Commander_leaveLocation &&  CurrentAssignedMajorActivity_fr==Fire_ActivityType.SearchandRescueCasualty )  ||			
					( this instanceof Responder_Police && !changedMechanism  && Bronzecommander_pol !=null   && Bronzecommander_pol.Commander_leaveLocation &&  CurrentAssignedMajorActivity_pol==Police_ActivityType.CollectInformation  ) 
					)
				; //do nothing	
			else
			{

				dis1= BuildStaticFuction.DistanceC(geography, this.Return_CurrentLocation(), ((Responder) msg.receiver).Return_CurrentLocation());

				if  ( this.WorkingonCasualty!=null ) 
					dis2= BuildStaticFuction.DistanceC(geography, this.Return_CurrentLocation(), this.WorkingonCasualty.getCurrentLocation());

				if  ( this.PrvRoleinprint1 == Ambulance_ResponderRole.CasualtyClearingOfficer && changedMechanism )  
					dis2= BuildStaticFuction.DistanceC(geography, this.Return_CurrentLocation(), this.assignedIncident.CCStation.Location.getCurrentPosition());
				if  ( this.PrvRoleinprint1 == Ambulance_ResponderRole.AmbulanceLoadingCommander && changedMechanism )  
					dis2= BuildStaticFuction.DistanceC(geography, this.Return_CurrentLocation(), this.assignedIncident.loadingArea.Location.getCurrentPosition()) ;
				if  (this.PrvRoleinprint3 == Police_ResponderRole.ReceptionCenterOfficer && changedMechanism )  
					dis2= BuildStaticFuction.DistanceC(geography, this.Return_CurrentLocation(), this.assignedIncident.RC.Location.getCurrentPosition()) ;
				if  (this.PrvRoleinprint3 == Police_ResponderRole.CordonsCommander && NoMoreRouteinScene && changedMechanism )  
					dis2= BuildStaticFuction.DistanceC(geography, this.Return_CurrentLocation(), this.assignedIncident.Cordon_outer.EnteryPointAccess_Point.getCurrentPosition()) ;
				if  (this.PrvRoleinprint2 == Fire_ResponderRole.FireSectorCommander&& NoMoreRouteinScene && CurrentAssignedMajorActivity_fr==Fire_ActivityType.ClearRoute && changedMechanism ) 
					dis2= BuildStaticFuction.DistanceC(geography, this.Return_CurrentLocation(), this.assignedIncident.Cordon_outer.EnteryPointAccess_Point.getCurrentPosition()) ;			

				if  (this.PrvRoleinprint2 == Fire_ResponderRole.FireSectorCommander  && CurrentAssignedMajorActivity_fr==Fire_ActivityType.SearchandRescueCasualty && changedMechanism )  //&& !(msg.receiver instanceof Responder_Ambulance && ((Responder )msg.receiver).PrvRoleinprint1 == Ambulance_ResponderRole.AmbulanceSectorCommander )
				{
					dis2= BuildStaticFuction.DistanceC(geography, this.Return_CurrentLocation(),  this.AssignedSector.Startpoint.getCurrentPosition()) ;
					if ( this.AssignedSector.AmIinSector(this)) dis2= .5 ;
				}
				if  (this.PrvRoleinprint1 == Ambulance_ResponderRole.AmbulanceSectorCommander  && changedMechanism )  //!(msg.receiver instanceof Responder_Fire &&  ((Responder )msg.receiver).PrvRoleinprint2 == Fire_ResponderRole.FireSectorCommander )
				{
					dis2= BuildStaticFuction.DistanceC(geography, this.Return_CurrentLocation(), this.AssignedSector.Startpoint.getCurrentPosition()) ;
					if ( this.AssignedSector.AmIinSector(this)) dis2= .5 ;
				}



				if ( dis1 >= InputFile.FacetoFaceLimit && GO )  // I can add no obstacle
				{ 							
					DestinationLocation = ((Responder) msg.receiver).Return_CurrentLocation();						
					this.Walk();
					if ( this instanceof Responder_Ambulance )  this.BehaviourType1=RespondersBehaviourTypes1.ComunicationDelay ;
					if ( this instanceof Responder_Fire ) this.BehaviourType2=RespondersBehaviourTypes2.ComunicationDelay ;
					if ( this instanceof Responder_Police ) this.BehaviourType3=RespondersBehaviourTypes3.ComunicationDelay ;

				}
				else if ( dis1 < InputFile.FacetoFaceLimit && GO )
				{
					if ( SensingeEnvironmentTrigger==Ontology.RespondersTriggers.ArrivedResponder)
					{ Assign_DestinationLocation_Serach() ;SensingeEnvironmentTrigger=null; System.out.println(this.Id + "erooor=============================================================================face to face===========================================================================ArrivedResponder");}

					intialAcknowledged_FF= false;

					if ( this instanceof Responder_Ambulance )  this.BehaviourType1=RespondersBehaviourTypes1.Comunication ;
					if ( this instanceof Responder_Fire ) this.BehaviourType2=RespondersBehaviourTypes2.Comunication ;
					if ( this instanceof Responder_Police ) this.BehaviourType3=RespondersBehaviourTypes3.Comunication ;

					flag=((Responder) msg.receiver).RecivedMessage(msg );
					Currentusedcom=msg.ComMech ;
					if (flag ) 
					{ 
						UpdateComNet(msg.receiver );	GO=false;answer=0; 

						//System.out.println(this.Id + ".........................................................................................................1..............give msage  " + wait_forPerviousReciver +"  "+ this.Sending);	
					}   			
					else  //pending or waiting
					{
						answer=-1;
						//System.out.println(this.Id + ".........................................................................................................2..............wait msage " + wait_forPerviousReciver+"  "+ this.Sending);	

					} 

				}
				else if ( intialAcknowledged_FF  )   // get Acknowledged 
				{
					//System.out.println(this.Id + ".........................................................................................................3.............intialAcknowledged - " + wait_forPerviousReciver+"  "+ this.Sending);	
					back =false;
					intialAcknowledged_FF= false;

					if (this.WorkingonCasualty!=null    || 
							(this.PrvRoleinprint1 == Ambulance_ResponderRole.CasualtyClearingOfficer&& changedMechanism) ||
							( this.PrvRoleinprint1 == Ambulance_ResponderRole.AmbulanceLoadingCommander && changedMechanism )||
							(this.PrvRoleinprint3 == Police_ResponderRole.ReceptionCenterOfficer && changedMechanism ) ||
							(this.PrvRoleinprint3 == Police_ResponderRole.CordonsCommander&& NoMoreRouteinScene  && changedMechanism ) ||
							(this.PrvRoleinprint2 == Fire_ResponderRole.FireSectorCommander&& NoMoreRouteinScene && CurrentAssignedMajorActivity_fr==Fire_ActivityType.ClearRoute && changedMechanism )||

							(this.PrvRoleinprint2 == Fire_ResponderRole.FireSectorCommander && ! ( this.AssignedSector.AmIinSector(this)) && CurrentAssignedMajorActivity_fr==Fire_ActivityType.SearchandRescueCasualty && changedMechanism ) ||
							(this.PrvRoleinprint1 == Ambulance_ResponderRole.AmbulanceSectorCommander && ! ( this.AssignedSector.AmIinSector(this))  && changedMechanism ) )


					{ 
						answer=0 ; back =true ;GO=false; 
					} // not yet  
					else
					{
						answer=1; back=false;GO=true;

						Acknowledged=true;
						Currentusedcom=msg.ComMech ;
						changedMechanism=false;
						Commander_leaveLocation=false;
						//System.out.println(this.Id + ".........................................................................................................4.............done -no need to back " +"  "+ this.Sending);

					}  //done    

				}
				else if ( dis2  >=1   && back )
				{
					if  ( this.WorkingonCasualty!=null ) 
						Assign_DestinationCasualty(this.WorkingonCasualty) ;

					if  ( this.PrvRoleinprint1 == Ambulance_ResponderRole.CasualtyClearingOfficer && changedMechanism )  
						this.Assign_DestinationCordon(this.assignedIncident.CCStation.Location);
					if  ( this.PrvRoleinprint1 == Ambulance_ResponderRole.AmbulanceLoadingCommander && changedMechanism ) 
						this.Assign_DestinationCordon(this.assignedIncident.loadingArea.Location) ;
					if  (this.PrvRoleinprint3 == Police_ResponderRole.ReceptionCenterOfficer && changedMechanism ) 
						this.Assign_DestinationCordon(this.assignedIncident.RC.Location);
					if  (this.PrvRoleinprint3 == Police_ResponderRole.CordonsCommander && NoMoreRouteinScene && changedMechanism )  
						this.Assign_DestinationCordon(this.AssignedCordon.EnteryPointAccess_Point) ;
					if  (this.PrvRoleinprint2 == Fire_ResponderRole.FireSectorCommander&& NoMoreRouteinScene && CurrentAssignedMajorActivity_fr==Fire_ActivityType.ClearRoute && changedMechanism ) 
						this.Assign_DestinationCordon(this.AssignedCordon.EnteryPointAccess_Point) ;		

					if  (this.PrvRoleinprint2 == Fire_ResponderRole.FireSectorCommander &&  ! ( this.AssignedSector.AmIinSector(this)) && CurrentAssignedMajorActivity_fr==Fire_ActivityType.SearchandRescueCasualty && changedMechanism ) 
						this.Assign_DestinationCordon(this.AssignedSector.Startpoint) ;
					if  (this.PrvRoleinprint1 == Ambulance_ResponderRole.AmbulanceSectorCommander&& ! ( this.AssignedSector.AmIinSector(this)) && changedMechanism ) 
						this.Assign_DestinationCordon(this.AssignedSector.Startpoint) ;



					this.Walk();

					if ( this instanceof Responder_Ambulance )  this.BehaviourType1=RespondersBehaviourTypes1.ComunicationDelay ;
					if ( this instanceof Responder_Fire ) this.BehaviourType2=RespondersBehaviourTypes2.ComunicationDelay ;
					if ( this instanceof Responder_Police ) this.BehaviourType3=RespondersBehaviourTypes3.ComunicationDelay ;

				}
				else if (dis2  <1  && back  )
				{
					if ( SensingeEnvironmentTrigger==Ontology.RespondersTriggers.ArrivedCasualty || SensingeEnvironmentTrigger==Ontology.RespondersTriggers.ArrivedTargetObject)
					{ SensingeEnvironmentTrigger=null; System.out.println(this.Id + "............-------------------------------------------------------------............ArrivedCasualty/.ArrivedTargetObject");}

					answer=1; back=false;GO=true;
					Acknowledged=true;
					Currentusedcom=msg.ComMech ;
					changedMechanism=false;
					Commander_leaveLocation=false;
					if ( this instanceof Responder_Ambulance )  BehaviourType1=RespondersBehaviourTypes1.Comunication ;
					if ( this instanceof Responder_Fire )  BehaviourType2=RespondersBehaviourTypes2.Comunication ;
					if ( this instanceof Responder_Police ) BehaviourType3=RespondersBehaviourTypes3.Comunication ;
					//System.out.println(this.Id + ".........................................................................................................4.............done -end " + wait_forPerviousReciver+"  "+ this.Sending);
				}

			}

			break;	
			//========================================================
		case Phone : 

			if ( this instanceof Responder_Ambulance && !changedMechanism && Bronzecommander_amb !=null && Bronzecommander_amb.Commander_leaveLocation &&  (CurrentAssignedMajorActivity_amb==Ambulance_ActivityType.loadingandTnasporation) )  
				;  //do nothings  for driver
			else
			{

				if (  msg.receiver instanceof Responder ) 
				{
					answer=1;
					flag=((Responder) msg.receiver).RecivedMessage(msg );
					Currentusedcom=msg.ComMech ;
					if (flag ) UpdateComNet(msg.receiver );else  answer=-1;
				}
				else if ( msg.receiver instanceof EOC  ) 
				{		
					answer=1;	
					flag=((EOC) msg.receiver).RecivedMessage(msg );
					Currentusedcom=msg.ComMech ;
					if (flag ) UpdateComNet(msg.receiver );	else  answer=-1;
				}

			}
			break;
			//========================================================
		case RadioSystem: 
			int x=-1 ;
			if ( this instanceof Responder_Ambulance )  x=1;
			if ( this instanceof Responder_Fire )  x=2;
			if ( this instanceof Responder_Police ) x=3 ;

			if (this.assignedIncident.RadioSystem_CheckChanleFree(x,this,(Responder) msg.receiver) ==1 )
			{
				chanleused=this.assignedIncident.On_RadioSystem(x , this ,(Responder) msg.receiver );	
				if ( chanleused!=null) //Reserve unused channel to communicate
				{
					answer=1;
					flag=((Responder) msg.receiver).RecivedMessage(msg );
					Currentusedcom=msg.ComMech ;

					if (flag ) UpdateComNet(msg.receiver ); 
					else {  answer=-1; this.assignedIncident.Off_RadioSystem(chanleused ,x,this);}
				}

			}
			else if (this.assignedIncident.RadioSystem_CheckChanleFree(x,this,(Responder) msg.receiver) ==-1 )
				answer=-1 ;//pending

			if ( answer==-1) System.out.println("                                                                                              "+ this.Id + "  1 )...pending.........================================================================..........Radio " );
			break;
			//========================================================			
		case SharedTactical_RadioSystem: 			


			if (this.assignedIncident.RadioSystem_CheckChanleFree(4,this,(Responder) msg.receiver)==1 )
			{
				chanleused=this.assignedIncident.On_RadioSystem(4 , this,(Responder) msg.receiver);	
				if ( chanleused!=null) //Reserve unused channel to communicate
				{
					answer=1;
					flag=((Responder) msg.receiver).RecivedMessage(msg );
					Currentusedcom=msg.ComMech ;

					if (flag ) UpdateComNet(msg.receiver ); 
					else {  answer=-1; this.assignedIncident.Off_RadioSystem(chanleused ,4,this);}
				}
			}
			else if (this.assignedIncident.RadioSystem_CheckChanleFree(4,this,(Responder) msg.receiver) ==-1 )
				answer=-1 ;//pending


			if (answer==-1) System.out.println("                                                                                              "+this.Id + " 1)...pending.........================================================================..........Radio " );
			break;
			//========================================================

		case RadioSystem_BoradCast: //Paper
			int xxx=-1 ;
			if ( this instanceof Responder_Ambulance )  xxx=1;
			if ( this instanceof Responder_Fire )  xxx=2;
			if ( this instanceof Responder_Police ) xxx=3 ;

			if (this.assignedIncident.RadioSystem_CheckChanleFree(xxx,this,null)==1 )
			{
				chanleused=this.assignedIncident.On_RadioSystem(xxx , this, null);	
				if ( chanleused!=null) //Reserve unused channel to communicate
				{

					answer=1;
					for(Object Res : msg.Listofreceiver    )
					{
						flag=((Responder) Res).RecivedMessage(msg );
						if (flag ) UpdateComNet(Res );else  { System.out.println(this.Id + "...error.........================================================================..........Radio BoradCast " );}
					}

					Currentusedcom=msg.ComMech ;	
					this.assignedIncident.Off_RadioSystem(chanleused ,xxx,this);}
			}

			break;
			//========================================================
		case ElectronicDevice: // Electronic
			answer=1;
			Currentusedcom=msg.ComMech ;	
			//UpdateComNet(msg.receiver );
			break;
		case FF_BoradCast : 
			answer=1;
			for(Object Res : msg.Listofreceiver    )
			{
				flag=((Responder) Res).RecivedMessage(msg );
				if (flag ) UpdateComNet(Res );else  { System.out.println(this.Id + "...error.........================================================================..........FF BoradCast " );}
			}

			Currentusedcom=msg.ComMech ;			
			break;}	

		return answer;	
	}

	public void UpdateComNet(Object receiver )
	{
		Network net = (Network)context.getProjection("Comunication_network");
		RepastEdge<Object> edg_temp;		
		edg_temp = net.getEdge(this,receiver );
		if(edg_temp!=null)
			edg_temp.setWeight(edg_temp.getWeight() + 1) ;
		else
			net.addEdge(this,receiver , 1);
	}

	public boolean RecivedMessage(ACL_Message msg ) //in Receiver agent
	{		

		//		if  (Sending == true  && MyReceiver== msg.sender   && !back  && !( msg.ComMech==CommunicationMechanism.RadioSystem_BoradCast || msg.ComMech==CommunicationMechanism.FF_BoradCast)   && MyPerformative != ACLPerformative.Command && msg.performative!= ACLPerformative.Command )  ///&& this.CurrentMessage == null
		//		{	
		//			System.out.println( "  --------------------------------------------- Sending Issue ------------------------------------------------------------------in agent " + this.Id  + "called by" + ((Responder) msg.sender).Id);
		//			System.out.println( this.CurrentTick +" Mesg content  Sender:" +  ((Responder) msg.sender).Id +"   "+ msg.performative  + "..... Reciver :"+ ((Responder) msg.receiver).Id  );
		//
		//			ACL_Message SM= (((Responder) msg.sender).Message_inbox.get( ((Responder) msg.sender).Message_inbox.size()-1));
		//			System.out.println( "Sender :" +  SM.time   );
		//
		//			System.out.println( this.CurrentTick + "But me I was want to send him .......Receiver " + ((Responder)MyReceiver).Id +"   " + MyPerformative + " in " +Mytimetosend   );
		//
		//
		//			return false;
		//		}
		
		
		if  (Sending == true  && msg.sender== MyReceiver   && !back  && !( msg.ComMech==CommunicationMechanism.RadioSystem_BoradCast || msg.ComMech==CommunicationMechanism.FF_BoradCast)   && MyPerformative != ACLPerformative.Command && msg.performative!= ACLPerformative.Command )  ///&& this.CurrentMessage == null
		{	
			System.out.println( "  --------------------------------------------- Sending Issue ------------------------------------------------------------------in agent " + this.Id  + "called by" + ((Responder) msg.sender).Id);
			System.out.println( this.CurrentTick +" Mesg content  Sender:" +  ((Responder) msg.sender).Id +"   "+ msg.performative  + "..... Reciver :"+ ((Responder) msg.receiver).Id  );

			ACL_Message SM= (((Responder) msg.sender).Message_inbox.get( ((Responder) msg.sender).Message_inbox.size()-1));
			System.out.println( "Sender :" +  SM.time   );

			System.out.println( this.CurrentTick + "But me I was want to send him .......Receiver " + ((Responder)MyReceiver).Id +"   " + MyPerformative + " in " +Mytimetosend   );


			return false;
		}	


		else
		{
			switch (msg.ComMech) {	
			case FaceToFace : //Paper
				Message_inbox.add(msg);		
				msg.time= this.CurrentTick ;
				break;
			case Phone : //Paper
				Message_inbox.add(msg);	
				msg.time= this.CurrentTick ;
				break;
			case RadioSystem_BoradCast : 
				Message_inbox.add(msg);	
				msg.time= this.CurrentTick ;
				break;	
			case RadioSystem: 
				Message_inbox.add(msg);	
				msg.time= this.CurrentTick ;
				break;
			case SharedTactical_RadioSystem: 
				Message_inbox.add(msg);	
				msg.time= this.CurrentTick ;				
				break;
			case ElectronicDevice: // Electronic				
				//added directly in system				
				;
				break;
			case FF_BoradCast : 
				Message_inbox.add(msg);	
				msg.time= this.CurrentTick ;

				break;}

			return  true ;
		}
	}

	public void Acknowledg( Object receiver ) //in Sender  and called by receiver to confirm he get messages
	{
		AcknowledgedReciver=receiver;

		if (wait_forPerviousReciver)
			PerviousAcknowledged =true ;
		else
			Acknowledged=true;

		//if ( this.zzz==true  )  System.out.println(this.Id + " >>>>>>>>>>>>>>>>>>>>>>>. Acknowledged     "+ ((Responder )receiver).Id );

		if (Currentusedcom==CommunicationMechanism.RadioSystem  ) 
		{				
			int x=-1 ;
			if ( this instanceof Responder_Ambulance )  x=1;
			if ( this instanceof Responder_Fire )  x=2;
			if ( this instanceof Responder_Police ) x=3 ; 

			this.assignedIncident.Off_RadioSystem(chanleused , x , this);
		}
		else if (Currentusedcom==CommunicationMechanism.SharedTactical_RadioSystem ) 
			this.assignedIncident.Off_RadioSystem(chanleused ,4 , this);
		else if (Currentusedcom==CommunicationMechanism.FaceToFace  ) 
		{
			//have to back 
			intialAcknowledged_FF =true ; 
			Acknowledged=false; 
			PerviousAcknowledged =false ;
		} 



		if (IwantTokeepPC== true   ) // with phone
		{
			//in sender
			if ( ! this.PersonalContacts_List.contains(((Responder) receiver))) 	
				this.PersonalContacts_List.add(((Responder) receiver));

			//in resciver			
			if ( ! ((Responder) receiver).PersonalContacts_List.contains(this)    )
				((Responder) receiver).PersonalContacts_List.add( this);

			IwantTokeepPC=false;

			System.out.println("                                                                                                                                                      "+"||||||||||||  Save Phone "+ this.Id  +" "+ ((Responder) receiver).Id); 
		}

		Currentusedcom=null;
		//System.out.println(this.Id + " >>>>>>>>>>>>>>>>>>>>>>>. Acknowledged"+ Acknowledged + Action );
	}

	public void Acknowledg( Object receiver , boolean _AcceptRequest ) //in Sender just F-F
	{
		AcknowledgedReciver=receiver;
		Acknowledged=true;
		if (_AcceptRequest ==true)  
			AcceptRequest2= 1 ;
		else
			AcceptRequest2= -1 ;

		if (Currentusedcom==CommunicationMechanism.FaceToFace  ) 
		{ intialAcknowledged_FF =true ; Acknowledged=false;}  
		Currentusedcom=null;
		//System.out.println(this.Id + " >>>>>>>>>>>>>>>>>>>>>>>. Acknowledged"+ Acknowledged + Action );
	}

	//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
	public void setRandomllymove( boolean  flage)
	{

		this.RandomllySearachBuld=flage;
	}

	public  boolean  getRandomllymove ()
	{
		return this.RandomllySearachBuld;	
	}

	public String getId() {
		return this.Id;
	}

	public int getColorCode() {
		return this.ColorCode;
	}

	public void setColorCode(int ColorCode1) {
		this.ColorCode = ColorCode1;
	}

	public RespondersActions getAction() {
		return this.Action ;
	}

	public void setAction(RespondersActions A) {
		this.Action = A;
	}

	//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
}// end of class
