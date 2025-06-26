/*
 * package Other;
 * 
 * 
 * 
 * import java.util.ArrayList; import java.util.List; import java.util.Random;
 * import javax.measure.unit.SI; import com.vividsolutions.jts.geom.Coordinate;
 * import com.vividsolutions.jts.geom.Geometry; import
 * com.vividsolutions.jts.geom.GeometryFactory; import
 * com.vividsolutions.jts.geom.Polygon; import
 * com.vividsolutions.jts.util.GeometricShapeFactory;
 * 
 * import A_Environment.Incident; import A_Environment.Object_Zone; import
 * A_Environment.PointDestination; import A_Environment.TopographicArea; import
 * A_Environment.check_line; import B_Communication.ACL_Message; import
 * B_Communication.Command; import B_Communication.ISRecord; import
 * C_SimulationInput.InputFile; import C_SimulationOutput.ResponderActions;
 * import D_Ontology.Ontology; import D_Ontology.Ontology.CasualtyStatus; import
 * D_Ontology.Ontology.CommunicationMechanism; import
 * D_Ontology.Ontology.CordonType; import
 * D_Ontology.Ontology.RandomWalking_StrategyType; import
 * D_Ontology.Ontology.RespondersActions; import
 * D_Ontology.Ontology.RespondersBehaviourTypes; import
 * D_Ontology.Ontology.RespondersTriggers; import
 * D_Ontology.Ontology.TaskAllocationApproach; import
 * D_Ontology.Ontology.TaskAllocationMechanism; import Other.GruopResponders;
 * import repast.simphony.context.Context; import
 * repast.simphony.engine.environment.RunEnvironment; import
 * repast.simphony.engine.schedule.ISchedule; import
 * repast.simphony.space.gis.Geography; import
 * repast.simphony.space.graph.Network; import
 * repast.simphony.space.graph.RepastEdge;
 * 
 * public class oldresponders{
 * 
 * private int size;//for styling purposes public double CurrentTick ,
 * EndofCurrentAction=0 ; public ISchedule schedule =
 * RunEnvironment.getInstance().getCurrentSchedule();
 * 
 * //----------------------------------------- public String Id; public
 * Ontology.RespondersActions Action; public Ontology.RespondersBehaviourTypes
 * BehaviourType; public Ontology.RespondersTriggers
 * SensingeEnvironmentTrigger,ActionEffectTrigger , CommTrigger,
 * InterpretedTrigger ,EndActionTrigger ; public ResponderActions RespFile ;
 * //----------------------------------------- private Coordinate
 * DestinationLocation, MiddleLocation; public double Total_distance ; // meter
 * public Incident assignedIncident ; public double RadiusOfReponderVision;
 * public GruopResponders Mygroup ; public int ColorCode =0; //for visualization
 * purpose
 * 
 * //----------------------------------------- public PointDestination
 * _PointDestination , MyDirectiontowalk=null,VehicleOrStartDirection=null
 * ,NextOppositVehicleOrStartDirection=null ; //like Ontology during moving
 * public PointDestination CenterOfSearchArea=null;
 * //-----------------------------------------
 * 
 * public Ontology.TargetObject TargetKind; public Casualty TargetCasualty,
 * OnhandCasualty= null ; public Object TargetObject; public Responder
 * TargetResponder ,ResponderArrivedme ; public TopographicArea TargetBuilding;
 * public Vehicle Myvehicle, Targetvehicle; public RandomWalking_StrategyType
 * walkingstrategy ;
 * 
 * //----------------------------------------- public boolean FirstAriavall=true
 * ; public boolean CheckCasualtyStatusDuringSearch; public int
 * Lastmessagereaded=0 ; //----------------------------------------- public
 * boolean RandomllySearachCasualty=false, RandomllyCountCasualty =false,
 * RandomllySearachBuld=
 * false,RandomllySearachCordonPostion=false,RandomllySearachSuspectedObject=
 * false ; public boolean extrication= false, Firest_time=true,
 * NOTyetReachBuilding= true, walkingOnScene=false ; public boolean DirL=false,
 * DirR=false ,DirU=false ,DirD=false ; public int ClockDirection ; boolean
 * firstofRotation= true; public CordonType _CordonType; public static Random
 * Randomizer = new Random();
 * 
 * //-----------------------------------------
 * 
 * public CommunicationMechanism Currentusedcom=null; public Command
 * CurrentCommandRequest=null ;
 * 
 * //----------------------------------------- public
 * ArrayList<PointDestination> Cordons_List = new ArrayList<PointDestination>();
 * public List<ACL_Message> Message_inbox = new ArrayList<ACL_Message>(); public
 * List<Casualty> Lastcoordinationcasualty_list = new ArrayList<Casualty>();
 * List<TopographicArea> CheckedBuildings = new ArrayList<TopographicArea>();
 * //like memory of responders List<check_line> check_line_list = new
 * ArrayList<check_line>(); //
 * -------------------------------------------------------------- public
 * Context<Object> context; public Geography<Object> geography;
 * 
 * 
 * // --------------------------------------------------------------
 * 
 * double AssessRadius = 0.0002; double AssessZoneRadius ;//in meter int
 * numAssessZone = 8; List<Object_Zone> AssessZoneAgents;
 * 
 * // --------------------------------------------------------------
 * 
 * 
 * //double radius = 0.0001; //double radius = 0.00012; //double AssessRadius =
 * 0.0002; // Original Radius of the Direction circle area in degree lat/lon
 * double radius = 0.00009; double DirectionZoneRadius ; //in meter int
 * numDirectionZone = 200;// high search zones lead to accurate responder
 * movement 250 List<Object_Zone> DirectionZoneAgents; //Direction Zones
 * Object_Zone safeZone; // Spatial space of the body of responder
 * 
 * // --------------------------------------------------------------
 * 
 * Object_Zone last_zone , PreviousbestZone=null , bestZone = null;
 * List<Object_Zone> PreviousZones = new ArrayList<Object_Zone>(); double
 * minDistance = 10000000; //in Km or M !! double angle = 0; double
 * CurrentZoneDistance;
 * 
 * 
 * //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
 * %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
 * %%%% public Responder(Context<Object> _context, Geography<Object> _geography,
 * Coordinate _Location, String ID, Vehicle _vehicle) {
 * 
 * context = _context; geography = _geography; context.add(this);
 * GeometryFactory fac = new GeometryFactory(); geography.move(this,
 * fac.createPoint(_Location));
 * 
 * //------------------------------------------- Id = ID; Myvehicle = _vehicle;
 * DestinationLocation=null; CheckCasualtyStatusDuringSearch=false;
 * 
 * //------------------------------------------- RespFile= new ResponderActions
 * (this);
 * 
 * }
 * 
 * //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
 * %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
 * %%%% // *** 1 **** public void Assign_DestinationCasualty(Casualty
 * _TargetCasualty) {
 * 
 * TargetCasualty = _TargetCasualty; DestinationLocation =
 * _TargetCasualty.getCurrentLocation(); TargetObject=_TargetCasualty;
 * ////////////////////////////////////////////////////////////////////////lock
 * here
 * 
 * TargetKind=Ontology.TargetObject.Casualty;
 * 
 * RandomllySearachCasualty=false ; RandomllySearachBuld=
 * false;RandomllySearachCordonPostion=false;RandomllySearachSuspectedObject=
 * false; NOTyetReachBuilding= true;extrication=false; PreviousbestZone=null;
 * 
 * }
 * 
 * // *** 2 **** public void Assign_DestinationVehicle(Vehicle _Vehicle) {
 * 
 * Targetvehicle= _Vehicle; DestinationLocation =
 * _Vehicle.getCurrent_Location(); TargetObject=_Vehicle ;
 * ////////////////////////////////////////////////////////////////////////lock
 * here
 * 
 * TargetKind=Ontology.TargetObject.Vehicle;
 * 
 * RandomllySearachCasualty=false ; RandomllySearachBuld=
 * false;RandomllySearachCordonPostion=false;RandomllySearachSuspectedObject=
 * false; NOTyetReachBuilding= true;extrication=false;PreviousbestZone=null;
 * 
 * }
 * 
 * // *** 3 **** public void Assign_DestinationResponder(Responder Res) {
 * 
 * TargetResponder=Res; DestinationLocation =
 * geography.getGeometry(Res).getCoordinate(); TargetObject = Res ;
 * 
 * TargetKind=Ontology.TargetObject.Responder; RandomllySearachCasualty=false ;
 * RandomllySearachBuld=
 * false;RandomllySearachCordonPostion=false;RandomllySearachSuspectedObject=
 * false; NOTyetReachBuilding= true;extrication=false;PreviousbestZone=null;
 * 
 * }
 * 
 * // *** 4 ****
 * 
 * public void Assign_DestinationCordon(PointDestination cordon) {
 * 
 * DestinationLocation = cordon.getCurrentPosition(); TargetObject = cordon;
 * 
 * TargetKind=Ontology.TargetObject.TargetObject; RandomllySearachCasualty=false
 * ; RandomllySearachBuld=
 * false;RandomllySearachCordonPostion=false;RandomllySearachSuspectedObject=
 * false; NOTyetReachBuilding=true;extrication=false;PreviousbestZone=null; }
 * 
 * // *** 8 **** public void Assign_DestinationLocation_Serach() {
 * 
 * TargetCasualty=null ; TargetObject=null ; TargetResponder =null ;
 * TargetBuilding =null; Targetvehicle =null; OnhandCasualty= null ;
 * 
 * if (_PointDestination == null && MyDirectiontowalk !=null )
 * _PointDestination=MyDirectiontowalk ; else if (_PointDestination == null &&
 * MyDirectiontowalk ==null ) System.out.println(this.Id +
 * "  error    Assign_DestinationLocation_Serach    ");
 * 
 * DestinationLocation = _PointDestination.getCurrentPosition(); TargetObject=
 * _PointDestination; TargetKind=Ontology.TargetObject.nothing;
 * 
 * 
 * RandomllySearachCasualty=false ; RandomllySearachBuld=
 * false;RandomllySearachCordonPostion=false;RandomllySearachSuspectedObject=
 * false; NOTyetReachBuilding=true;extrication=false;PreviousbestZone=null;
 * 
 * if ( this instanceof Responder_Ambulance ) RandomllySearachCasualty=true ;
 * 
 * 
 * 
 * // else if ( this instanceof Responder_FireEngine ) // RandomllySearachBuld=
 * true; // // else if ( this instanceof Responder_Police ) // { //
 * Police_TaskType Task = ((Responder_Police)this).ReturnCuurnttask() ; // // if
 * ( Task==Police_TaskType.Setcordon) // RandomllySearachCordonPostion=true ; //
 * else if ( Task==Police_TaskType.CheckSecurity) //
 * RandomllySearachSuspectedObject=true ; // else if ( Task
 * ==Police_TaskType.SearchSurroundingAreaAboutCasualty) //
 * RandomllySearachCasualty=true ; // } }
 * 
 * //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
 * %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
 * %%%% public void RandomDirectionSerach( ) { //Generate random int value from
 * 1 to 4 int Dir= Randomizer.nextInt(4)+ 1 ;
 * 
 * switch(Dir) { case 1: MyDirectiontowalk= this.assignedIncident.PointL; break;
 * case 2: MyDirectiontowalk= this.assignedIncident.PointR; break; case 3:
 * MyDirectiontowalk= this.assignedIncident.PointU ; break; case 4:
 * MyDirectiontowalk= this.assignedIncident.PointD; break; }
 * 
 * _PointDestination=MyDirectiontowalk;
 * 
 * }
 * 
 * public void NextOppositDirectionSerach_AtsameAxis( ) {
 * 
 * switch(_PointDestination.Name) { case "Left": MyDirectiontowalk=
 * this.assignedIncident.PointR; break; case "Right": MyDirectiontowalk=
 * this.assignedIncident.PointL; break; case "North": MyDirectiontowalk=
 * this.assignedIncident.PointD; break; case "South": MyDirectiontowalk=
 * this.assignedIncident.PointU ; break; }
 * 
 * _PointDestination=MyDirectiontowalk; }
 * 
 * public void NextOppositVehicleOrStartDirection( ) {
 * 
 * switch(VehicleOrStartDirection.Name) { case "Left": MyDirectiontowalk=
 * this.assignedIncident.PointR; break; case "Right": MyDirectiontowalk=
 * this.assignedIncident.PointL; break; case "North": MyDirectiontowalk=
 * this.assignedIncident.PointD; break; case "South": MyDirectiontowalk=
 * this.assignedIncident.PointU ; break; }
 * 
 * NextOppositVehicleOrStartDirection=MyDirectiontowalk;
 * _PointDestination=MyDirectiontowalk; }
 * 
 * //***************************************************************************
 * *********************** public void NextRotateDirectionSearch_bigRoute( int
 * _ClockDirection) //Basic direction
 * 
 * { //With the clockwise
 * 
 * if (_ClockDirection==1 || _ClockDirection==0) {
 * switch(MyDirectiontowalk.Name) { case "Left": _PointDestination=
 * this.assignedIncident.PointU; break; case "Right": _PointDestination=
 * this.assignedIncident.PointD; break; case "North": _PointDestination=
 * this.assignedIncident.PointR ; break; case "South": _PointDestination=
 * this.assignedIncident.PointL; break; } } else if (_ClockDirection==-1) {
 * switch(MyDirectiontowalk.Name) { case "Left": _PointDestination=
 * this.assignedIncident.PointD; break; case "Right": _PointDestination=
 * this.assignedIncident.PointU; break; case "North": _PointDestination=
 * this.assignedIncident.PointL ; break; case "South": _PointDestination=
 * this.assignedIncident.PointR; break; } }
 * 
 * MyDirectiontowalk=_PointDestination; }
 * 
 * public void NextRotateDirectionSearch_smallRoute( int _ClockDirection) {
 * //With the clockwise
 * 
 * if (_ClockDirection==1 || _ClockDirection==0) {
 * switch(MyDirectiontowalk.Name) { case "Left": _PointDestination=
 * this.assignedIncident.PointUU; break; case "Right": _PointDestination=
 * this.assignedIncident.PointDD; break; case "North": _PointDestination=
 * this.assignedIncident.PointRR; break; case "South": _PointDestination=
 * this.assignedIncident.PointLL; break; } } else if (_ClockDirection==-1) {
 * switch(MyDirectiontowalk.Name) { case "Left": _PointDestination=
 * this.assignedIncident.PointDD; break; case "Right": _PointDestination=
 * this.assignedIncident.PointUU; break; case "North": _PointDestination=
 * this.assignedIncident.PointLL; break; case "South": _PointDestination=
 * this.assignedIncident.PointRR; break; } }
 * 
 * MyDirectiontowalk=_PointDestination; }
 * 
 * public PointDestination NextSecondStartofsmallRoute( PointDestination curr) {
 * PointDestination SecondStart=null; if ( curr== assignedIncident.PointD )
 * SecondStart=assignedIncident.PointDD ; else if (
 * curr==assignedIncident.PointU ) SecondStart=assignedIncident.PointUU ; else
 * if (curr==assignedIncident.PointL ) SecondStart=assignedIncident.PointLL ;
 * else if ( curr==assignedIncident.PointR )
 * SecondStart=assignedIncident.PointRR ;
 * 
 * return SecondStart; }
 * 
 * //***************************************************************************
 * *********************** public void walkingstrategy() { switch
 * (walkingstrategy) { case StreetsCenter :
 * //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 * ++++++++++ //+++++++ 1- StreetsCenter
 * ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 * //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 * ++++++++++ if ( MyDirectiontowalk.Name == "Left" || MyDirectiontowalk.Name ==
 * "Right" ||MyDirectiontowalk.Name == "North" || MyDirectiontowalk.Name ==
 * "South" ) { switch (MyDirectiontowalk.Name ) { case "Left" : DirL=true;
 * break; case "Right" : DirR=true; break; case "North": DirU=true; break; case
 * "South": DirD=true; break; }
 * 
 * MyDirectiontowalk=this.assignedIncident.PointCenter; _PointDestination=
 * MyDirectiontowalk ; this.Assign_DestinationLocation_Serach( ); } else if (
 * MyDirectiontowalk.Name == "Center" ) { if ( DirL && DirR && DirU && DirD )
 * //it means he walk in all direction
 * SensingeEnvironmentTrigger=RespondersTriggers.Arrived_WalkedinAllScene; else
 * { this.RandomDirectionSerach(); while( MyDirectiontowalk.Name == "Left" &&
 * DirL==true || MyDirectiontowalk.Name == "Right" && DirR==true ||
 * MyDirectiontowalk.Name == "North" && DirU==true || MyDirectiontowalk.Name ==
 * "South" && DirD==true) this.RandomDirectionSerach();
 * this.Assign_DestinationLocation_Serach( ); } }
 * 
 * break; case OneDirection :
 * //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 * ++++++++++ //+++++++ 2- OneDirection
 * ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 * //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 * ++++++++++ if ( MyDirectiontowalk.Name == "Left" || MyDirectiontowalk.Name ==
 * "Right" ||MyDirectiontowalk.Name == "North" || MyDirectiontowalk.Name ==
 * "South" ) { MyDirectiontowalk=this.assignedIncident.PointCenter;
 * _PointDestination= MyDirectiontowalk ;
 * this.Assign_DestinationLocation_Serach( ); //CenterOfSearchArea =null; } else
 * if ( MyDirectiontowalk.Name == "Center" ) {
 * SensingeEnvironmentTrigger=RespondersTriggers.
 * Arrived_WalkedalongOneDirection; }
 * 
 * break;
 * 
 * case TowDirections_AtsameAxis :
 * //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 * ++++++++++ //+++++++ 3-
 * TowDirections++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 * //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 * ++++++++++ switch (MyDirectiontowalk.Name ) { case "Left" : DirL=true; break;
 * case "Right" : DirR=true; break; case "North": DirU=true; break; case
 * "South": DirD=true; break; }
 * 
 * if ( (DirL && DirR) || (DirU && DirD ) ) //it means he walk in all direction
 * SensingeEnvironmentTrigger=RespondersTriggers.Arrived_WalkedinAllScene; else
 * { this.NextOppositDirectionSerach_AtsameAxis();
 * this.Assign_DestinationLocation_Serach( ); CenterOfSearchArea =null; } break;
 * case FourDirections :
 * //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 * ++++++++++ //+++++++ 4- FourDirections
 * ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 * //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 * ++++++++++
 * 
 * switch (MyDirectiontowalk.Name ) { case "Left" : DirL=true; break; case
 * "Right" : DirR=true; break; case "North": DirU=true; break; case "South":
 * DirD=true; break; }
 * 
 * if ( DirL && DirR && DirU && DirD ) //it means he walk in all direction
 * SensingeEnvironmentTrigger=RespondersTriggers.Arrived_WalkedinAllScene; else
 * { System.out.println(Id + "xxxxx");
 * this.NextRotateDirectionSearch_bigRoute(this.ClockDirection);
 * this.Assign_DestinationLocation_Serach( ); }
 * 
 * break; case FourDirections_police1 :
 * //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 * ++++++++++ //+++++++ 4- FourDirections
 * ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 * //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 * ++++++++++ switch (MyDirectiontowalk.Name ) { case "Left" : DirL=true; break;
 * case "Right" : DirR=true; break; case "North": DirU=true; break; case
 * "South": DirD=true; break; }
 * 
 * if ( DirL && DirR && DirU && DirD ) //it means he walk in all direction { if
 * ( Mygroup.Groupwithus==null ) { this.Reset_DirectioninSearach();
 * this.walkingstrategy=RandomWalking_StrategyType.FourDirections_police2 ;
 * this.ClockDirection =this.ClockDirection * -1 ;
 * MyDirectiontowalk=this.NextSecondStartofsmallRoute( _PointDestination );
 * _PointDestination=MyDirectiontowalk; this.Assign_DestinationLocation_Serach(
 * );
 * 
 * Mygroup.Groupwithus=Mygroup ;
 * 
 * } else { if ( _PointDestination== assignedIncident.ControlArea ) {
 * SensingeEnvironmentTrigger=RespondersTriggers.Arrived_WalkedinAllScene;
 * //System.out.println("     +++ " + Id +"Arrived_WalkedinAllScene "); } else {
 * MyDirectiontowalk=this.assignedIncident.ControlArea;
 * _PointDestination=MyDirectiontowalk; this.Assign_DestinationLocation_Serach(
 * ); } } } else { if (FirstAriavall) { EndofCurrentAction= CurrentTick +20 + 1
 * ;FirstAriavall= false;}
 * 
 * this.NextRotateDirectionSearch_bigRoute(this.ClockDirection);
 * this.Assign_DestinationLocation_Serach( ); }
 * 
 * break;
 * 
 * case FourDirections_police2 :
 * //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 * ++++++++++ //+++++++ 4- FourDirections
 * ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 * //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 * ++++++++++
 * 
 * switch (MyDirectiontowalk.Name ) { case "Left" : DirL=true; break; case
 * "Right" : DirR=true; break; case "North": DirU=true; break; case "South":
 * DirD=true; break; }
 * 
 * if ( DirL && DirR && DirU && DirD ) //it means he walk in all direction { if
 * ( _PointDestination== assignedIncident.ControlArea ) {
 * SensingeEnvironmentTrigger=RespondersTriggers.Arrived_WalkedinAllScene;
 * //System.out.println("     +++ " + Id +"Arrived_WalkedinAllScene "); } else {
 * MyDirectiontowalk=assignedIncident.ControlArea ;
 * _PointDestination=MyDirectiontowalk; this.Assign_DestinationLocation_Serach(
 * ); }
 * 
 * } else { if (FirstAriavall) { EndofCurrentAction= CurrentTick +20 + 1
 * ;FirstAriavall= false;}
 * 
 * this.NextRotateDirectionSearch_smallRoute(this.ClockDirection);
 * this.Assign_DestinationLocation_Serach( ); }
 * 
 * break; case RotationInnerCordon :
 * //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 * ++++++++++ //+++++++ 5- RotationInnerCordon
 * ++++++++++++++++++++++++++++++++++++++++++++++++++++++
 * //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 * ++++++++++ if ( firstofRotation) { PointDestination Nearstcordon=null; double
 * dis, mindis=999999;
 * 
 * firstofRotation= false;
 * 
 * Nearstcordon= this.assignedIncident.NearstCordon_from_Innercordons(this.
 * Return_CurrentLocation(),false,false);
 * 
 * //clockDir= Randomizer.nextInt(2) ; ClockDirection=1;
 * //System.out.println("dir     " + clockDir );
 * //Cordons_List.add(Nearstcordon); _PointDestination=Nearstcordon;
 * this.Assign_DestinationLocation_Serach( );
 * 
 * } else { if (Cordons_List.size() >
 * this.assignedIncident.CordonsOfIncident_inner.size() )
 * {SensingeEnvironmentTrigger=RespondersTriggers.Arrived_WalkedinAllScene;
 * System.out.println(this.Id + " Arrived_WalkedinAllScene");} else {
 * 
 * int lastindex=Cordons_List.size()-1 ; int z=
 * this.assignedIncident.GetnextCordons_Inner( ClockDirection
 * ,Cordons_List.get(lastindex).id,false,0 ); //System.out.println("ssss" +
 * Cordons_List.get(lastindex).id + "    " + z); PointDestination
 * currentCordon=this.assignedIncident.CordonsOfIncident_inner.get(z);
 * //Cordons_List.add(currentCordon); _PointDestination=currentCordon;
 * this.Assign_DestinationLocation_Serach( ); //return back to this }
 * 
 * } break;
 * 
 * case TowDirections_Fire :
 * //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 * ++++++++++ //+++++++ 9- TowDirections
 * ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 * //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 * ++++++++++ switch (MyDirectiontowalk.Name ) { case "Left" : DirL=true; break;
 * case "Right" : DirR=true; break; case "North": DirU=true; break; case
 * "South": DirD=true; break; }
 * 
 * if ( (DirR && DirU ) || ( DirR && DirD ) || ( DirD && DirL ) || ( DirU &&
 * DirL ) ) //it means he walk in all direction { if
 * (MyDirectiontowalk!=this.VehicleOrStartDirection ) { CenterOfSearchArea=null;
 * MyDirectiontowalk=this.VehicleOrStartDirection ;
 * _PointDestination=MyDirectiontowalk; this.Assign_DestinationLocation_Serach(
 * ); } else
 * SensingeEnvironmentTrigger=RespondersTriggers.Arrived_WalkedinAllScene; }
 * else { this.NextOppositVehicleOrStartDirection( );
 * this.Assign_DestinationLocation_Serach( ); }
 * 
 * break;}//end switch }
 * 
 * //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
 * %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
 * %%%% public void Walk() {
 * 
 * if (RandomllySearachCasualty== true ) //check for target
 * Search_RandomlyCasualty(this.CheckCasualtyStatusDuringSearch , false );
 * 
 * if (RandomllyCountCasualty== true )
 * Search_RandomlyCasualty(this.CheckCasualtyStatusDuringSearch ,true );
 * 
 * //---------------------------------------------------------------------------
 * -----------
 * 
 * //if ( TargetKind==Ontology.TargetObject.Responder && this instanceof
 * Responder_FireEngine )
 * //DestinationLocation=TargetResponder.Return_CurrentLocation();
 * 
 * //---------------------------------------------------------------------------
 * ----------- // 1- calculate real distance in meter Total_distance =
 * Ontology.DistanceC(geography, this.Return_CurrentLocation(),
 * DestinationLocation);
 * 
 * // 2- less than one meter before destination stop if (Total_distance > 1 &&
 * NOTyetReachBuilding ) { walking_OneStep2(); // 0.25 M which means one step
 * walkingOnScene=true; } else { walkingOnScene=false; PreviousbestZone=null; //
 * we reached target, so we move there is less than one meter switch
 * (this.TargetKind) { // switch 1 case Casualty :
 * SensingeEnvironmentTrigger=Ontology.RespondersTriggers.ArrivedCasualty;
 * this.OnhandCasualty = (Casualty) TargetObject ; if ( this instanceof
 * Responder_Ambulance ) this.OnhandCasualty.IsAssignedToResponder=true; //need
 * think !!!!!!!
 * 
 * //if ( this instanceof Responder_Ambulance &&
 * this.OnhandCasualty.IsAssignedToResponder== false ) //
 * this.OnhandCasualty.IsAssignedToResponder=true; //need think !!!!!!! //else
 * // System.out.println( this.Id +"  I ahve problem  with " +
 * this.OnhandCasualty.ID); break; case Vehicle :
 * SensingeEnvironmentTrigger=Ontology.RespondersTriggers.ArrivedVehicle; break;
 * case Responder:
 * SensingeEnvironmentTrigger=Ontology.RespondersTriggers.ArrivedResponder;
 * ((Responder) TargetObject).ResponderArrivedme= this; break; case
 * TargetObject:
 * SensingeEnvironmentTrigger=Ontology.RespondersTriggers.ArrivedTargetObject;
 * 
 * break; case nothing : //Responder.Go back in different direction until he get
 * some thing walkingstrategy(); break; } // switch 1 }// we reached target }
 * 
 * //***************************************************************************
 * ************************************* private void walking_OneStep2() {
 * 
 * Coordinate OldPosition=this.Return_CurrentLocation();
 * 
 * if (this.extrication) MiddleLocation= this.DestinationLocation ; else
 * SelectionZone();
 * 
 * angle = Ontology.AngleBetween2CartesianPoints(this.Return_CurrentLocation(),
 * MiddleLocation); geography.moveByVector(this, 0.5, SI.METER, angle);
 * 
 * // Update Search Zones Position As Well Move_related_zones( OldPosition ,
 * this.Return_CurrentLocation());
 * 
 * }
 * 
 * //***************************************************************************
 * ************************************* private boolean
 * CheckZone_SafeZoneCollision(List<TopographicArea> nearObjects, double angle)
 * { boolean result=false;
 * 
 * geography.moveByVector(safeZone, 0.5 * 1, SI.METER, angle);
 * 
 * if (safeZone.lookForObjects(nearObjects).size() == 0 ) result=true;
 * 
 * // return back to previous state (test ended) if (angle > Math.PI)
 * geography.moveByVector(safeZone, 0.5 * 1, SI.METER, angle - Math.PI); else
 * geography.moveByVector(safeZone, 0.5* 1 , SI.METER, angle + Math.PI);
 * 
 * 
 * return result; }
 * 
 * //***************************************************************************
 * ************************************* private boolean
 * Check_SafeZoneInBuilding( Object Build , double angle) { boolean
 * result=false;
 * 
 * geography.moveByVector(safeZone, 0.5 * 1, SI.METER, angle);
 * 
 * if ( ( geography.getGeometry(safeZone)).intersects((
 * geography.getGeometry(Build )) ) || ( geography.getGeometry(Build
 * )).intersects(( geography.getGeometry(safeZone)) ) || (
 * geography.getGeometry(Build )).contains(( geography.getGeometry(safeZone))) )
 * result=true;
 * 
 * // return back to previous state (test ended) if (angle > Math.PI)
 * geography.moveByVector(safeZone, 0.5 * 1, SI.METER, angle - Math.PI); else
 * geography.moveByVector(safeZone, 0.5* 1 , SI.METER, angle + Math.PI);
 * 
 * 
 * return result; }
 * 
 * //***************************************************************************
 * ************************************* private boolean TestZone( Object_Zone
 * Pre ,Object_Zone Cur ) { boolean valid=false;
 * 
 * if ( Pre ==null ) valid= true; else { int zp= Pre .ID; int zt=Cur.ID;
 * 
 * int sz=zp-35; int ez= zp + 35;
 * 
 * //-----1------- if( ( sz>=1 && sz <=numDirectionZone) && (ez>=1 && ez
 * <=numDirectionZone )) { //check1 within if (zt >=sz && zt<= ez) valid=true;
 * else valid=false; } //-----2------- else if(ez > numDirectionZone ) {
 * //check2 out ez=ez-numDirectionZone; if (zt >ez && zt< sz) valid=false; else
 * valid=true; } //-----3------- else if( sz <=0 ) { //check3 out
 * sz=sz+numDirectionZone; if (zt >ez && zt< sz) valid=false; else valid=true; }
 * //-------------
 * 
 * }// end else
 * 
 * return valid;
 * 
 * }
 * 
 * //***************************************************************************
 * ************************************* private void SelectionZone() {
 * //xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx //1- Get All
 * Near Geographical Objects
 * 
 * @SuppressWarnings("unchecked") List<TopographicArea> nearObjects =
 * (List<TopographicArea>)
 * Ontology.GetObjectsWithinDistance(this,TopographicArea.class, 30);
 * 
 * 
 * // 2- Remove all non-building objects to get a list of obstacles such as
 * building for (int i = 0; i < nearObjects.size(); i++) { if
 * (nearObjects.get(i).getcode() != 4) { nearObjects.remove(i); i--; //Because
 * if delete one object , next object not yet checked will take the previous
 * number } }
 * 
 * //xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx bestZone =
 * null; //reset MiddleLocation= null; minDistance = 10000000; //reset
 * ///xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx // 3- select
 * the best Zone of DirectionZones
 * 
 * //++++ 1- +++++++++++++++++++++++++++++++++ if (DirectionZoneRadius >
 * Total_distance || this.TargetKind ==Ontology.TargetObject.Building)// this
 * means that target is within search zone circle {
 * //System.out.println("Selection 1"); angle =
 * Ontology.AngleBetween2CartesianPoints(this.Return_CurrentLocation(),
 * DestinationLocation);
 * 
 * for (Object_Zone zone : DirectionZoneAgents) { zone.setActive(false); //
 * check direct link if ( zone.lookForObject(TargetObject) ) { if (
 * CheckZone_SafeZoneCollision( nearObjects,angle) ) { bestZone = zone;
 * MiddleLocation= DestinationLocation; } } } } //++++ 2-
 * +++++++++++++++++++++++++++++++++ else { //System.out.println("Selection 2");
 * 
 * for (Object_Zone zone : DirectionZoneAgents) { zone.setActive(false); if (
 * zone.lookForObjects(nearObjects).size() == 0 ) { CurrentZoneDistance =
 * Ontology.DistanceC(geography, zone.SidePoint, DestinationLocation); //
 * calculate distance between zone end point which should be on the
 * circumference of the search zone circle, and the target if (
 * CurrentZoneDistance <= minDistance && TestZone( PreviousbestZone ,zone ) ) {
 * angle = Ontology.AngleBetween2CartesianPoints(this.Return_CurrentLocation(),
 * zone.SidePoint);// calculate direction angle between zone end point and
 * target if ( CheckZone_SafeZoneCollision( nearObjects,angle) ) { minDistance =
 * CurrentZoneDistance; bestZone = zone; MiddleLocation=zone.SidePoint ; } } }
 * }// for
 * 
 * //System.out.println("Selection 2"); }
 * 
 * ///xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx // 4- check
 * select the best Zone if ( bestZone == null ) { //++++ 3-
 * +++++++++++++++++++++++++++++++++ if (DirectionZoneRadius > Total_distance )
 * { angle =
 * Ontology.AngleBetween2CartesianPoints(this.Return_CurrentLocation(),
 * DestinationLocation);// calculate direction angle between zone end point and
 * target
 * 
 * if ( CheckZone_SafeZoneCollision( nearObjects,angle) ) { MiddleLocation=
 * DestinationLocation;
 * 
 * } } //++++ 4- +++++++++++++++++++++++++++++++++ if (MiddleLocation==null ) {
 * //System.out.println("selction 4"); minDistance = 10000000; //reset for
 * (Object_Zone zone : DirectionZoneAgents) { zone.setActive(false); if (
 * zone.lookForObjects(nearObjects).size() == 0 ) {
 * 
 * CurrentZoneDistance = Ontology.DistanceC(geography, zone.SidePoint,
 * DestinationLocation); // calculate distance between zone end point which
 * should be on the circumference of the search zone circle, and the target
 * 
 * if ( CurrentZoneDistance < minDistance ) //&& testZone( PreviousbestZone
 * ,zone ) {
 * 
 * angle = Ontology.AngleBetween2CartesianPoints(this.Return_CurrentLocation(),
 * zone.SidePoint);// calculate direction angle between zone end point and
 * target if ( CheckZone_SafeZoneCollision( nearObjects,angle) ) { minDistance =
 * CurrentZoneDistance; bestZone = zone; MiddleLocation=zone.SidePoint ; } } }
 * }// for
 * 
 * }
 * 
 * //System.out.println( this.Id + "  xxxxxxxxxxxxxxxxxxxxxxxxxxx   "
 * +bestZone.ID + "     dis   " + minDistance + "prev   "+ PreviousbestZone.ID +
 * "  total  " + Total_distance );
 * 
 * //++++ 5- +++++++++++++++++++++++++++++++++ if (this.TargetKind
 * ==Ontology.TargetObject.Building) {
 * 
 * angle = Ontology.AngleBetween2CartesianPoints(this.Return_CurrentLocation(),
 * DestinationLocation);
 * 
 * if ( Check_SafeZoneInBuilding(TargetObject,angle ) ) { MiddleLocation=
 * DestinationLocation; bestZone = null; NOTyetReachBuilding=false;
 * 
 * // System.out.println(
 * "dddssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssdd"); } }
 * 
 * 
 * } ///xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx if
 * (MiddleLocation==null ) { //System.out.println("selction 4"); minDistance =
 * 10000000; //reset for (Object_Zone zone : DirectionZoneAgents) {
 * zone.setActive(false); if ( zone.lookForObjects(nearObjects).size() == 0 ) {
 * 
 * CurrentZoneDistance = Ontology.DistanceC(geography, zone.SidePoint,
 * DestinationLocation); // calculate distance between zone end point which
 * should be on the circumference of the search zone circle, and the target
 * 
 * if ( CurrentZoneDistance < minDistance ) {
 * 
 * angle = Ontology.AngleBetween2CartesianPoints(this.Return_CurrentLocation(),
 * zone.SidePoint);// calculate direction angle between zone end point and
 * target if ( CheckZone_SafeZoneCollision( nearObjects,angle) ) { minDistance =
 * CurrentZoneDistance; bestZone = zone; MiddleLocation=zone.SidePoint ; } } }
 * }// for
 * 
 * } ///xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
 * 
 * if (MiddleLocation==null ) {
 * 
 * MiddleLocation= DestinationLocation; }
 * 
 * 
 * if ( bestZone != null ) { PreviousbestZone= bestZone;
 * bestZone.setActive(true); // yellow shadow !!!!
 * 
 * }
 * 
 * }
 * 
 * //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
 * %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
 * %%%% private void Search_RandomlyCasualty( boolean Yesckeck_Severity ,boolean
 * ckeckcount ) {
 * 
 * 
 * TargetCasualty=null; minDistance = 10000000; int maxpriority_level = 4;
 * //xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx //1- Get All
 * Near Geographical Objects
 * 
 * @SuppressWarnings("unchecked") //@SuppressWarnings("unchecked") tells the
 * compiler that the programmer believes the code to be safe and won't cause
 * unexpected exceptions. List<TopographicArea> nearObjects =
 * (List<TopographicArea>)
 * Ontology.GetObjectsWithinDistance(this,TopographicArea.class, 100);
 * 
 * //2- Remove all non-building objects to get a list of obstacles such as
 * building for (int i = 0; i < nearObjects.size(); i++) { if
 * (nearObjects.get(i).getcode() != 4) { nearObjects.remove(i); i--; //Because
 * if delete one object , next object not yet checked will take the previous
 * number } }
 * 
 * //xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
 * 
 * Object Source=null; if (CenterOfSearchArea!=null) // sector Source=
 * CenterOfSearchArea; else Source=this;
 * //xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx //3- Get All
 * Near casualty Objects
 * 
 * @SuppressWarnings("unchecked") List<Casualty> nearObjects_Casualty =
 * (List<Casualty>) Ontology.GetObjectsWithinDistance(Source,Casualty.class,
 * this.assignedIncident.radius_incidentCircle_LRUD );
 * 
 * 
 * if (! Yesckeck_Severity ) //triage { if ( this instanceof Responder_Ambulance
 * ) { for (int i = 0; i < nearObjects_Casualty.size(); i++) { if
 * (nearObjects_Casualty.get(i).Outsidebulding==false ||
 * Lastcoordinationcasualty_list.contains(nearObjects_Casualty.get(i))
 * ||nearObjects_Casualty.get(i).IsAssignedToResponder== true
 * ||nearObjects_Casualty.get(i).Status!=CasualtyStatus.NonTrapped ){
 * nearObjects_Casualty.remove(i); i--; } } }
 * 
 * 
 * } //--------------------------------------------------- else //treatment { if
 * ( this instanceof Responder_Ambulance ) { for (int i = 0; i <
 * nearObjects_Casualty.size(); i++) { if
 * (nearObjects_Casualty.get(i).Outsidebulding==false ||
 * Lastcoordinationcasualty_list.contains(nearObjects_Casualty.get(i))
 * ||nearObjects_Casualty.get(i).IsAssignedToResponder== true
 * ||nearObjects_Casualty.get(i).Status!=CasualtyStatus.Triaged){
 * nearObjects_Casualty.remove(i); i--; } } }
 * 
 * } //xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx // 4- search
 * the nearest casualty and there is no obstacle
 * 
 * if (nearObjects_Casualty.size()> 0) //begin if ############### { for
 * (Casualty ca : nearObjects_Casualty) {
 * 
 * //1- create line to check Coordinate[] coor = new Coordinate[2]; coor[0]=
 * this.Return_CurrentLocation(); coor[1]= ca.getCurrentLocation();
 * 
 * check_line chline= new check_line(); GeometryFactory fac = new
 * GeometryFactory(); Geometry geom = fac.createLineString(coor) ;
 * context.add(chline); geography.move( chline, geom);
 * check_line_list.add(chline); //check if it intersect with building boolean
 * validline=true; for (TopographicArea buld : nearObjects) if (
 * geom.intersects(geography.getGeometry(buld))) { validline=false ; break; }
 * 
 * //2- if valid line if( validline) {
 * 
 * //=========================================== //Search Action if
 * (!ckeckcount) {
 * 
 * 
 * double discas=Ontology.DistanceC(geography, this.Return_CurrentLocation(),
 * ca.getCurrentLocation());
 * 
 * if( discas < minDistance && !Yesckeck_Severity ) { TargetCasualty= ca;
 * minDistance =discas; //System.out.println("responder " + this.Id +
 * " targetCasualty : " + TargetCasualty.ID + " dis  : " + discas ) ; }
 * //----------------------- else if ( ca.priority_level <= maxpriority_level &&
 * Yesckeck_Severity ) { if (ca.priority_level < maxpriority_level )
 * 
 * { TargetCasualty= ca; maxpriority_level=ca.priority_level ; minDistance
 * =discas; } else { if( discas < minDistance) { TargetCasualty= ca;
 * maxpriority_level=ca.priority_level ; minDistance =discas; }
 * 
 * }
 * 
 * } //----------------------- } //===========================================
 * //Count Action if (ckeckcount) { for (Object_Zone zone : AssessZoneAgents) if
 * ( zone.lookForObject(ca) ) {zone.Addonecasulty();
 * Lastcoordinationcasualty_list.add(ca);break;} }
 * 
 * 
 * } //end validline
 * 
 * 
 * }// end for } else { }// end if ###############
 * 
 * 
 * //xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx // after
 * selection of the bestZone , calculate result direction angle if
 * (TargetCasualty != null && !ckeckcount ) {
 * 
 * RandomllySearachCasualty= false;
 * SensingeEnvironmentTrigger=RespondersTriggers.SensedCasualty;
 * 
 * check_line_list.clear();
 * 
 * //TargetCasualty.ColorCode=7; //System.out.println( this.Id +
 * " targetCasualty : " + TargetCasualty.ID ) ;
 * 
 * } }
 * 
 * //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
 * %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
 * %%%% public void Setup_ConsiderSeverityofcasualtyinSearach(boolean
 * _CheckCasualtyStatus ) {
 * this.CheckCasualtyStatusDuringSearch=_CheckCasualtyStatus; }
 * 
 * public void Reset_DirectioninSearach( ) { DirL=false; DirR=false ; DirU=false
 * ; DirD=false ; }
 * 
 * public void Set_DirectioninSearach( ) { DirL=true; DirR=true; DirU=true;
 * DirD=true; }
 * 
 * public Coordinate getDestinationLocation() { return DestinationLocation; }
 * 
 * public Coordinate Return_CurrentLocation() { return
 * (geography.getGeometry(this)).getCoordinate(); }
 * 
 * public void setLocation_and_move(Coordinate newPosition) {
 * 
 * Coordinate oldPosition= this.Return_CurrentLocation();
 * 
 * // so we move agent double x = (newPosition.x - oldPosition.x); double y =
 * (newPosition.y - oldPosition.y);
 * 
 * geography.moveByDisplacement(this, x, y);
 * 
 * }
 * 
 * private void Move_related_zones(Coordinate oldPosition ,Coordinate
 * newPosition ) { // so we move agent and all search zones and safe zone to
 * exact position double x = (newPosition.x - oldPosition.x); double y =
 * (newPosition.y - oldPosition.y);
 * 
 * for (Object_Zone zone : DirectionZoneAgents) { zone.SidePoint.x += x;
 * zone.SidePoint.y += y; geography.moveByDisplacement(zone, x, y); }
 * 
 * geography.moveByDisplacement(safeZone, x, y);
 * 
 * if ( AssessZoneAgents!=null ) for (Object_Zone zone : AssessZoneAgents) {
 * zone.SidePoint.x += x; zone.SidePoint.y += y;
 * geography.moveByDisplacement(zone, x, y); }
 * 
 * }
 * 
 * public void Generate_responder_zones() {
 * 
 * //-------------------------------------------------- // 1- Spatial space of
 * the body of responder safeZone = new Object_Zone(this);
 * safeZone.setVisible(true); safeZone.setsafezone(1);// for visualization
 * purposes Coordinate center = geography.getGeometry(this).getCoordinate();
 * GeometricShapeFactory gsf = new GeometricShapeFactory();
 * gsf.setCentre(center); //gsf.setSize(radius / 22); //orginal
 * //gsf.setSize(radius /40); //35 uesd befor gsf.setSize(radius /60);
 * gsf.setNumPoints(20); Polygon p = gsf.createCircle(); GeometryFactory fac =
 * new GeometryFactory(); context.add(safeZone); geography.move(safeZone,
 * fac.createPolygon(p.getCoordinates()));
 * 
 * //-------------------------------------------------- // 2- Generate a list of
 * direction search zone agents that will be used by the responder agent to
 * detect nearby objects.
 * 
 * DirectionZoneAgents = Ontology.GenerateSearchZones(geography, context,
 * center, numDirectionZone, radius, this); DirectionZoneRadius =
 * Ontology.DistanceC(geography, center, DirectionZoneAgents.get(0).SidePoint);
 * //System.out.println(this.Id + "     DirectionZoneRadius in meter : " +
 * DirectionZoneRadius);
 * 
 * }
 * 
 * public void Destroy_responder_Zones() {
 * 
 * context.remove(safeZone);
 * 
 * for (Object_Zone zone : DirectionZoneAgents) { context.remove(zone); }
 * 
 * }
 * 
 * //***************************************************************************
 * ************************************* public void Generate_AssessZones() { //
 * Generate a list of assess search zone agents that will be used by the
 * responder agent to detect nearby objects. Coordinate center =
 * geography.getGeometry(this).getCoordinate(); AssessZoneAgents=
 * Ontology.GenerateSearchZones(geography, context, center, numAssessZone,
 * AssessRadius, this); AssessZoneRadius = Ontology.DistanceC(geography, center,
 * DirectionZoneAgents.get(0).SidePoint); System.out.println(this.Id +
 * "     AssessZoneRadius in meter : " + AssessZoneRadius); }
 * 
 * public void Destroy_AssessZones() {
 * 
 * for (Object_Zone zone : AssessZoneAgents) context.remove(zone); }
 * 
 * public Object_Zone Count_MaximumZonewithCasualty() {
 * 
 * int max=-1; Object_Zone zonemax=null; for (Object_Zone zone :
 * AssessZoneAgents) if (zone.Totalcount >= max ) zonemax=zone ;
 * 
 * return zonemax; }
 * 
 * //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
 * %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
 * %%%%
 * 
 * public boolean SendMessage2( ACL_Message msg , CommunicationMechanism
 * ComMech) //in Sender FaceToFace, RadioSystem,ISApplication { boolean
 * flag=false, YoucanSend=false;
 * 
 * //--------------------------------------- // 1- check availability of current
 * CommunicationMechanism if ( msg.receiver instanceof Responder ) { switch
 * (ComMech) { case FaceToFace : double dis= Ontology.DistanceC(geography,
 * this.Return_CurrentLocation(), ((Responder)
 * msg.receiver).Return_CurrentLocation()); if ( dis >=
 * InputFile.FacetoFaceLimit ) // I can add no obstacle {
 * this.Assign_DestinationResponder((Responder) msg.receiver);
 * this.Action=RespondersActions.GoToResponders;
 * this.BehaviourType=RespondersBehaviourTypes.Sharinginfo; this.Walk(); } else
 * { YoucanSend=true;
 * 
 * } break; case RadioSystem: //Paper
 * 
 * if ( this.assignedIncident.On_RadioSystem()) //Reserve unused channel to
 * communicate { YoucanSend=true; } break; case ISApplication: // Electronic
 * YoucanSend=true; break;} }
 * 
 * //--------------------------------------- //2- if (YoucanSend==true) {
 * flag=((Responder) msg.receiver).RecivedMessage2(msg,ComMech );
 * Currentusedcom=ComMech ; }
 * 
 * 
 * //--------------------------------------- // 3- if sent it if (flag ) {
 * Network net = (Network)context.getProjection("Comunication_network");
 * RepastEdge<Object> edg_temp; edg_temp = net.getEdge(this,msg.receiver);
 * if(edg_temp!=null) edg_temp.setWeight(edg_temp.getWeight() + 1) ; else
 * net.addEdge(this,msg.receiver, 1); }
 * 
 * return flag; }
 * 
 * public boolean RecivedMessage2(ACL_Message msg , CommunicationMechanism
 * ComMech) //in Receiver { boolean Acknowledged =true; switch (ComMech) { case
 * FaceToFace : //Paper Message_inbox.add(msg); break; case RadioSystem: //Paper
 * Message_inbox.add(msg); break; case ISApplication: // Electronic //added
 * directly in system ((Responder) msg.sender).CommTrigger=RespondersTriggers.
 * Acknowledged; break; }
 * 
 * 
 * return Acknowledged ; }
 * 
 * public void Acknowledg() //in Sender { if
 * (Currentusedcom==CommunicationMechanism.RadioSystem )
 * this.assignedIncident.Off_RadioSystem();
 * 
 * CommTrigger=RespondersTriggers. Acknowledged; }
 * 
 * //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
 * %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
 * %%%% public void setRandomllymove( boolean flage) {
 * 
 * this.RandomllySearachBuld=flage; }
 * 
 * public boolean getRandomllymove () { return this.RandomllySearachBuld; }
 * 
 * public String getId() { return this.Id; }
 * 
 * public int getColorCode() { return this.ColorCode; }
 * 
 * public void setColorCode(int ColorCode1) { this.ColorCode = ColorCode1; }
 * 
 * //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
 * %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
 * %%%%
 * 
 * ArrayList<Integer> NumberRespondersPersector = new ArrayList<>();
 * 
 * public int AllocationGroup_Minimum(int NeededNumofGroups ,int TotalResponders
 * ,int Minimum) // at least Minimum member for group { int ActualNumofGroup1=
 * 0;
 * 
 * NumberRespondersPersector .clear(); //1- Identify how many group
 * 
 * if ( TotalResponders<= (NeededNumofGroups * Minimum ) ) { ActualNumofGroup1=
 * (int) Math.floor( TotalResponders/Minimum );
 * 
 * 
 * for(int i=0 ;i < ActualNumofGroup1;i++ ) { NumberRespondersPersector
 * .add(Minimum) ; }
 * 
 * 
 * if (( ActualNumofGroup1 * Minimum) < TotalResponders) { int x=
 * NumberRespondersPersector .get(NumberRespondersPersector .size()-1); x=x+1;
 * NumberRespondersPersector .set(NumberRespondersPersector .size()-1, x); }
 * 
 * }
 * 
 * else { boolean Ftime=true; int K=0,i=0; while(i < TotalResponders) { if (K<
 * NeededNumofGroups && Ftime ) { if (i + Minimum <= TotalResponders) {
 * NumberRespondersPersector.add(Minimum); i=i+ Minimum ; } else { int x=
 * NumberRespondersPersector.get(NumberRespondersPersector.size()-1); x=x+1;
 * NumberRespondersPersector.set(NumberRespondersPersector.size()-1, x); i=i+ 1
 * ; }
 * 
 * K++; } else if (K< NeededNumofGroups && ! Ftime ) { int x=
 * NumberRespondersPersector.get(K); x=x+1; NumberRespondersPersector.set(K, x);
 * i=i+ 1 ; } else { K=0; Ftime = false; }
 * 
 * }// end while
 * 
 * }
 * 
 * // System.out.println("grouping"); // for ( int num :GroupResponders ) //
 * System.out.println(num);
 * 
 * ActualNumofGroup1=NumberRespondersPersector.size(); return ActualNumofGroup1;
 * }
 * 
 * //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
 * %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
 * %%%%
 * 
 * }// end of class
 */