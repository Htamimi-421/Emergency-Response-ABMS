package A_Agents;

import java.util.ArrayList;
import java.util.List;
import javax.measure.unit.SI;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.util.GeometricShapeFactory;
import A_Environment.Incident;
import A_Environment.Object_Zone;
import A_Environment.PointDestination;
import A_Environment.TacticalArea;
import A_Environment.TopographicArea;
import C_SimulationInput.InputFile;
import C_SimulationOutput.CasualtyOutput;
import D_Ontology.Ontology;
import D_Ontology.BuildStaticFuction;
import D_Ontology.Ontology.CasualtyAction;
import D_Ontology.Ontology.CasualtyLife;
import D_Ontology.Ontology.CasualtyStatus;
import D_Ontology.Ontology.CasualtyinfromationType;
import D_Ontology.Ontology.Level;
import D_Ontology.Ontology.MovmentCasualty;
import D_Ontology.Ontology.RespondersTriggers;
import D_Ontology.Ontology.TacticalAreaType;
import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.gis.Geography;

public class Casualty {

	public Responder addedbyxxxxx=null;    //temp used inTA
	public Responder  DirectedBy=null ; //temp 
	public double DirectedTime=0 ; //temp
	public double  CurrentTickaddedxxxx=0; //temp used inTA
	public boolean error=false;
	public boolean Giuded=true;
	int counterwalk=0;
	int countergiude=0;
	
	
	//-----------------------------------------	
	public double  CurrentTick ,  EndofCurrentAction  , Nextcheck= 0 , TimeLeaveScene;
	double Last_update_Vofvision=0 ;
	ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule(); 	

	public int ColorCode =0; //for visualization purpose

	//-----------------------------------------	
	public String ID;	
	public int Triage_tage= 99  ;   // visible for every one priority_leve: 1, 2, 3, 5 dead ,  6 Uninjured Casualty
	public int Triage_tageBeforHosp= 99  ;
	public CasualtyStatus Status;   
	public CasualtyAction UnderAction   , tempaction=null , Dead_duringAction=null;
	public boolean IsAssignedToResponder=false;	
	public int UnInjured_NOtYetStartowalk=-1 ;  //    0 not wak   1 walk
	public Ontology.RespondersTriggers SensingeEnvironmentTrigger=null ;
	//-----------------------------------------
	//SurvivalProbability
	public int InitialRPM =-1, CurrentRPM=-1, NextTimeinterval=1;   // 99 Uninjured Casualty
	public double Current_SurvivalProbability=-1 ;
	public CasualtyLife Life=CasualtyLife.Life;  //update by RPM
	public double  deadTime=0;
	
	//-----------------------------------------
	//Location
	public Level mobility_dpeathLevel= Level.None ;
	//public Sector   CasualtySector;	

	// Movement
	//public boolean IamOnScence= false;
	public boolean IcanMove=false ,  IGetInstactionToWalk_and_walking= false ,  WaitingToWalkCCS=false ,WaitingToWalkRC=false ;
	public MovmentCasualty		MoveTo=MovmentCasualty.Nomovment  , FinalMoveTO=null ;
	CasualtyAction FinalAction ;

	// --------------------------------------------------------------
	public Context<Object> context;
	public Geography<Object> geography;
	public A_Environment.check_line checkline; // if the responder can see this casualty there is direct line between responder and causality 

	// --------------------------------------------------------------	
	//used by Hospital
	public double TimeAdmissioninHospital ;
	public double waitStriage=0,  wait_treatment=0,  wait_Vtransfer=0 ;
	public double TimeinCCSorRC=0 ;
	public TacticalAreaType TA =null ; 

	//Used only by second triage officer  it SA
	public  boolean  newaddetoCCS= true ; 
	public  double  LastTime_Triage=0 ; 

	//Used only by RC officer  its SA
	public  boolean newaddetoRC= true ; 

	//Used only inCCS and RC
	public PointDestination bedOrchair ;  

	// used by Lission office police
	public boolean DoneLog=false;

	//used by firefighter
	public boolean DeadByFire=false;

	// --------------------------------------------------------------
//information
	public CasualtyinfromationType  AcurrateCI= CasualtyinfromationType.None ; // Update dynamic by casualty 
	public CasualtyinfromationType  PoliceCI= CasualtyinfromationType.None ; //  collect by Policeman .... Uninjured( in RC- log )  or Deceased( in scene - log )  // Injured( in CCS after- ST  by police )  => Deceased( in CCS Log )  ,Evacuated (  in CCS after- loading by police  )
	public  CasualtyinfromationType  EOCCI= CasualtyinfromationType.None ; //used by EOC 

	// --------------------------------------------------------------
	// used by FireFighter
	public  double EstimatedTimeExtrication ; //  in second 
	public int currentworkersExtrication=0 ;
	public ArrayList<Responder> Currentworkers_list=new ArrayList<Responder>();
	public  double StartTimeExtrication=0 ,FinishTimeExtrication ;
	public  boolean Done=false;

	// --------------------------------------------------------------
	public CasualtyOutput  LogFile ;

	//##############################################################################################################################################################	
	//Injured Casualty
	public Casualty(Context<Object> _context, Geography<Object> _geography,Coordinate _Location , int _InitialRPM, CasualtyStatus _Status , int _Casualtynum ,Incident _Incident) {

		context = _context;
		geography = _geography;
		GeometryFactory fac = new GeometryFactory();		
		context.add(this);
		geography.move(this, fac.createPoint(_Location));

		//-----------------------------
		InitialRPM =_InitialRPM ;
		CurrentRPM=InitialRPM ;
		Current_SurvivalProbability=Ontology.SurvivalProbability[InitialRPM][0];	
		Status=_Status;
		ID="Ca"+ _Casualtynum; 
		Nextcheck= ( InputFile.Detroiration_Every ) + schedule.getTickCount() ;	
		UnderAction=CasualtyAction.NoResponse;


		LogFile= new CasualtyOutput ( this , InitialRPM ); //this,schedule.getTickCount()

		AcurrateCI= CasualtyinfromationType.Injured ;
		this.assignedIncident=_Incident ;

	}

	//----------------------------------------------------------------------------------------------------
	//Uninjured Casualty
	public Casualty(Context<Object> _context, Geography<Object> _geography,Coordinate _Location  ,CasualtyStatus _Status , int _Casualtynum ,Incident _Incident) {

		context = _context;
		geography = _geography;
		GeometryFactory fac = new GeometryFactory();		
		context.add(this);
		geography.move(this, fac.createPoint(_Location));

		//-----------------------------
		InitialRPM =99;
		CurrentRPM=99;
		Current_SurvivalProbability=100 ;	

		Status=_Status;
		ID="Ca"+ _Casualtynum; 
		UnderAction=CasualtyAction.NoResponse;
		IcanMove= true;
		UnInjured_NOtYetStartowalk= 0 ;
		//ColorCode=priority_level;
		//IamOnScence= true;
		LogFile= new CasualtyOutput ( this , InitialRPM);  //this,schedule.getTickCount()
		AcurrateCI= CasualtyinfromationType.Uninjured ;
		this.assignedIncident=_Incident ;
	}

	//##############################################################################################################################################################	
	public double DistansTocasuality(Responder Res){
		return BuildStaticFuction.DistanceC(geography, this.getCurrentLocation(), Res.Return_CurrentLocation());
	}

	//----------------------------------------------------------------------------------------------------
	public Coordinate getCurrentLocation(){
		return (geography.getGeometry(this)).getCoordinate();
	}

	//----------------------------------------------------------------------------------------------------
	public String getName() {
		return this.ID;
	}

	//----------------------------------------------------------------------------------------------------
	public int getPriority() {
		return Triage_tage ;
	}

	//----------------------------------------------------------------------------------------------------
	public void setPriority(int Priority) {
		//during triage
		Triage_tage = Priority;
	}

	//----------------------------------------------------------------------------------------------------
	public int getTrapped() {
		if (this.Status==CasualtyStatus.Trapped)
			return 1;
		else
			return 0;
	}

	//----------------------------------------------------------------------------------------------------
	public void setTrapped() {

		this.Status=CasualtyStatus.Trapped;

		if ( mobility_dpeathLevel== Level.High)
			EstimatedTimeExtrication= InputFile.EstimatedTimeExtrication_H;  //10  ;   
		else if ( mobility_dpeathLevel== Level.Medium)
			EstimatedTimeExtrication=  InputFile.EstimatedTimeExtrication_M ; 
		else if ( mobility_dpeathLevel== Level.Low)
			EstimatedTimeExtrication= InputFile.EstimatedTimeExtrication_L;  //30;  //

		Setmovementdetails();
	}

	public void Setmovementdetails() {


		if (CurrentRPM >= 9 && CurrentRPM <= 12  )  
		{
			IcanMove=true; 
			if ( Status== CasualtyStatus.Trapped || Status== CasualtyStatus.PreTreatedTrapped ) IcanMove=false; 
		}

		else if (CurrentRPM < 9)
		{

//			if (IcanMove==true )
//			{
//				System.out.println("                                                                                                                                                 ******* not move   now     "  
//						+  " " + this.ID +"     "+  this.CurrentRPM  +"   " + this.Triage_tage +  this.PoliceCI   +"   " + this.Status  +"   " + this.UnderAction   
//						+"   " +  this.TimeinCCSorRC  +"  " + this.MoveTo);
//			}

			if ( IcanMove==true && ( IGetInstactionToWalk_and_walking==true || WaitingToWalkRC==true  || WaitingToWalkCCS==true )   ) //temprery solution is wrong
			{   
				System.out.println("                                                                                                                                                 I will continue ******* not move   errrororrr/Attention I will not stop "+ this.ID ) ;
			}
			else
			{
				IcanMove=false;
			}


			//				else 	if ( ( IGetInstactionToWalk_and_walking==true  ) &&  (this.assignedIncident.Sectorlist2.get(0) ).AmIinSector( this)   ) 
			//				{
			//					IcanMove=false;IGetInstactionToWalk_and_walking=false;
			//					System.out.println("                                                                                                                                                 I will stop **" + this.ID );
			//				}



		}


	}

	//----------------------------------------------------------------------------------------------------
	public int getColorCode() {
		return this.ColorCode;
	}

	//----------------------------------------------------------------------------------------------------
	public void setColorCode(int ColorCode1) {
		this.ColorCode = ColorCode1;
	}

	//##############################################################################################################################################################	
	public void IworkinExtrication(Responder workers ){		

		Currentworkers_list.add(workers);		
		currentworkersExtrication ++ ;
		if ( StartTimeExtrication== 0 && ! Done)
			StartTimeExtrication=schedule.getTickCount() ;
	}

	public void INOTworkinSetup(Responder workers ){		

		Currentworkers_list.remove(workers);		

	}

	//______________________________________________________________________________________________________________________________________________________________
	//##############################################################################################################################################################	
	//													Behavior
	//##############################################################################################################################################################	
	@ScheduledMethod(start = 1, interval = 1 ,priority= ScheduleParameters.LAST_PRIORITY)
	public void SimulationResult( ) {

		//LogFile.ActionSummery( UnderAction ,CurrentRPM,  CurrentTick);

		//LogFile.IncrmentCasultyAction( UnderAction ,CurrentRPM,  CurrentTick, this.Current_SurvivalProbability);
		
		LogFile.IncrmentCasultyAction2( UnderAction ,CurrentRPM ); //,CurrentRPM,  CurrentTick 
		
		//if ( this.Status==CasualtyStatus.PreTreatedTrapped )  
	}

	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	// Casualty-  General
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	//@ScheduledMethod(start = 1, interval = 1 , priority= ScheduleParameters.FIRST_PRIORITY)
	public void step() {

		CurrentTick=schedule.getTickCount() ;

		// ++++++ 1- +++++++
		//if (( Status== CasualtyStatus.Trapped ||Status== CasualtyStatus.NonTrapped ||Status== CasualtyStatus.Triaged ) && CurrentRPM!=99 )  //CurrentRPM!=99  means uninjured
		//	Deterioration();



		if ( Status!= CasualtyStatus.HospitalTreated && CurrentRPM!=99 && CurrentRPM!=0)  //CurrentRPM!=99  means uninjured
			Deterioration();

		// ++++++ 2- +++++++
		if (( Status== CasualtyStatus.PreTreatedTrapped)   && StartTimeExtrication>0 && !Done  )  //||Status== CasualtyStatus.PreTreated) 
		{
			EstimatedTimeExtrication = EstimatedTimeExtrication - currentworkersExtrication   ; 

			if ( EstimatedTimeExtrication<=0  )
			{
				Done=true ;
				for (Responder   Resp :Currentworkers_list)	
					Resp.SensingeEnvironmentTrigger=RespondersTriggers.CasualtyExtracted;

				this.ColorCode= 7 ;
				Status= CasualtyStatus.Extracted ;
			}
		}

		// ++++++ 3- +++++++  //IcanMove==true && becasue  it could be updated in walk it shoud metion as asmuption 
		if ( ( IGetInstactionToWalk_and_walking  && SensingeEnvironmentTrigger==null)  ||
				(  WaitingToWalkCCS && assignedIncident.BroadCast_CASU_InstalledCCS  &&  SensingeEnvironmentTrigger==null  )  || 
				(  WaitingToWalkRC && assignedIncident.BroadCast_CASU_InstalledRC &&  SensingeEnvironmentTrigger==null  ) )  
		{
			Walk();
			if ( UnderAction==CasualtyAction.WaitingTransferDelay  && tempaction!=null ) {  UnderAction= tempaction;tempaction=null ;}
			
		}
		else	if ( ( IGetInstactionToWalk_and_walking  && SensingeEnvironmentTrigger==RespondersTriggers.ArrivedTargetObject)  ||
				(  WaitingToWalkCCS && assignedIncident.BroadCast_CASU_InstalledCCS  &&  SensingeEnvironmentTrigger==RespondersTriggers.ArrivedTargetObject  ) 
				|| (  WaitingToWalkRC && assignedIncident.BroadCast_CASU_InstalledRC  &&  SensingeEnvironmentTrigger==RespondersTriggers.ArrivedTargetObject  ) )  
		{
			//System.out.println( "                                                                                                                             *******      " + CurrentTick  + " " + this.ID +"  arived     " );				
			switch( MoveTo) {
			case TOCP :
				IGetInstactionToWalk_and_walking=false ;
				SensingeEnvironmentTrigger=null ;	

				MoveTo=FinalMoveTO;
				if ( FinalMoveTO== MovmentCasualty.TORC)  
				{				
					WaitingToWalkRC=true  ;	
					tempaction=UnderAction;
					UnderAction=CasualtyAction.WaitingTransferDelay;
					Assign_DestinationCordon( this.assignedIncident.RC.Location) ;
					FinalAction=CasualtyAction.WaitinginRC ;
				}
				else if ( FinalMoveTO== MovmentCasualty.TOCCS)
				{
					WaitingToWalkCCS=true  ;
					tempaction=UnderAction;
					UnderAction=CasualtyAction.WaitingTransferDelay;
					Assign_DestinationCordon( this.assignedIncident.CCStation.Location) ;
					FinalAction= CasualtyAction.WaitingSecondTriage;
				}

				break;
			case  TORC:
				if ( assignedIncident.RC.AssigenCasualtytoTA(this))
				{
					this.addedbyxxxxx=this.assignedIncident.AICcommander ;  this.CurrentTickaddedxxxx=this.CurrentTick ;
					UnderAction=FinalAction ;
					IGetInstactionToWalk_and_walking=false ;
					WaitingToWalkRC=false;
					SensingeEnvironmentTrigger=null ;	
					this.IcanMove=false;
					Destroy_responder_Zones();
					AcurrateCI= CasualtyinfromationType.SurvivorinRC ;
					//System.out.println( "                                                                                                                             *******      " + CurrentTick  + " " + this.ID +"  in RC     " );
				}
				break;	
			case TOCCS :
				if (assignedIncident.CCStation.AssigenCasualtytoTA(this))
				{
					this.addedbyxxxxx=this.assignedIncident.AICcommander ;  this.CurrentTickaddedxxxx=this.CurrentTick ;

					if (this.error)
						System.out.println(countergiude + "errrrrrrrrrrrrrrr in TA rrrrrrrrrrrrrrror TacticalArea  in adding  casulties walk"+ this.ID +"  to "      + "  "+  this.addedbyxxxxx.Id + "  "+ this.CurrentTickaddedxxxx + "  "+ this.Triage_tage + "  "+  this.Status+ "  "+  this.IcanMove );   
					this.error=false;

					UnderAction=FinalAction ;
					IGetInstactionToWalk_and_walking=false ;
					WaitingToWalkCCS=false;
					SensingeEnvironmentTrigger=null ;
					this.IcanMove=false;
					Destroy_responder_Zones();
					//System.out.println( "                                                                                                                              *******      " + CurrentTick  + " " + this.ID +"  in CCS     " );
				}
				break;	
			}
		}

		// ++++++ 4- +++++++
		if ( this.UnderAction== CasualtyAction.WaitingSecondTriage ) waitStriage ++ ;  
		if ( this.UnderAction== CasualtyAction.WaitingTreatment ) wait_treatment++;  
		if ( this.UnderAction== CasualtyAction.WaitingTransfertoV )  wait_Vtransfer++ ;

		// System.out.println( "    *******      " +  " " + this.ID +"     "+  this.CurrentRPM  +"   " + this.Triage_tage +  this.PoliceCI   +"   " + this.Status  +"   " + this.UnderAction    +"   " + this.UnInjured_NOtYetStartowalk);
		//		TimeinCCSorRC=1; 
		//		if ( CurrentTick==2300 && TimeinCCSorRC==0  )
		//		{ 
		//			System.out.println( "    *******      " +  " " + this.ID +"     "+  this.CurrentRPM  +"   " + this.Triage_tage +  this.PoliceCI   +"   " + this.Status  +"   " + this.UnderAction    +"   " + this.UnInjured_NOtYetStartowalk);
		//			TimeinCCSorRC=1; 
		//		}
	}// end Step *************

	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	// Casualty-  Deterioration
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^	
	private void Deterioration() {			

		if ( CurrentTick == Nextcheck )
		{ 
			CurrentRPM=Ontology.DelphiDHC[this.InitialRPM][NextTimeinterval-1];
			Current_SurvivalProbability=Ontology.SurvivalProbability[CurrentRPM][0];

			if (this.NextTimeinterval!= 11)
				this.NextTimeinterval++;


			if ( CurrentRPM==0 ) { this.Life=CasualtyLife.Dead;  this.Dead_duringAction= this.UnderAction ;   deadTime=CurrentTick;}
			
			if (CurrentRPM==0)
			{ AcurrateCI= CasualtyinfromationType.Deceased ;}

			if ( this.TimeinCCSorRC==0  || this.Life==CasualtyLife.Dead )  Setmovementdetails(); 

			Nextcheck=  CurrentTick + InputFile.Detroiration_Every  ;  //( InputFile.Detroiration_Every  )  ;  public static final double  Detroiration_Every = ( 30 * 60 )
			// Nextcheck = (this.NextTimeinterval * 30* 60) + starfrom 			
			//System.out.println( "       " + CurrentTick  + " " + this.ID +"      " + this.CurrentRPM  + "  " + this.priority_level   +"    " +Nextcheck +"  " + this.Life);
		}
	}

	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	// Casualty-  Move 
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^	
	public void SetMovement(MovmentCasualty _MoveTo  , MovmentCasualty _FinalMoveTO ) {	

		MoveTo=_MoveTo;
		switch( MoveTo) {
		case TORandomsafelocation :
			//Assign_DestinationCordon(assignedIncident.IdentifyNearest_safelocation(this.Return_CurrentLocation())) ;	//
			//FinalAction= CasualtyAction.WaitinginSafe;
			break;
		case TOCP :
			Assign_DestinationCordon( this.assignedIncident.collection_point) ;
			FinalMoveTO=_FinalMoveTO ;
			break;
		case  TORC:
			Assign_DestinationCordon( this.assignedIncident.RC.Location) ;
			FinalAction=CasualtyAction.WaitinginRC ;
			break;	
		case TOCCS :
			Assign_DestinationCordon( this.assignedIncident.CCStation.Location) ;
			FinalAction= CasualtyAction.WaitingSecondTriage;
			break;	
		}

		IGetInstactionToWalk_and_walking=true ;
		SensingeEnvironmentTrigger=null ;	
		counterwalk=0;
		countergiude++;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//                                           walk
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	// --------------------------------------------------------------
	double radius = 0.00009;  
	double DirectionZoneRadius ; //in meter
	int numDirectionZone = 200;// high search zones lead to accurate responder movement   250
	List<Object_Zone> DirectionZoneAgents; 				//Direction Zones
	Object_Zone safeZone;                               // Spatial space of the body of responder

	// --------------------------------------------------------------

	Object_Zone last_zone , PreviousbestZone=null , bestZone = null;
	List<Object_Zone> PreviousZones = new ArrayList<Object_Zone>();	
	double minDistance = 10000000; //in Km or M !!	
	double angle = 0;
	double CurrentZoneDistance;	

	//-----------------------------------------
	private Coordinate DestinationLocation,  MiddleLocation;
	public double Total_distance ; // meter
	public Incident assignedIncident ;
	public double RadiusOfReponderVision;
	public Object TargetObject;

	public Ontology.TargetObject TargetKind;	

	public void Assign_DestinationCordon(PointDestination cordon) {

		DestinationLocation = cordon.getCurrentPosition();
		TargetObject = cordon;
		TargetKind=Ontology.TargetObject.Targetobject;

		PreviousbestZone=null;
	}	

	//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
	//                                                             Walk
	//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%	
	public void Walk() {

		//  1- calculate real distance in meter
		Total_distance = BuildStaticFuction.DistanceC(geography, this.Return_CurrentLocation(), DestinationLocation);
		//System.out.println( this.ID+ "   "+ DestinationLocation.x +"  " + DestinationLocation.y );
		// 2- less than one meter before destination stop
		if (Total_distance > 1  ) {		
			walking_OneStep2(); // 0.25 M which means one step	

		} 
		else {
			PreviousbestZone=null;
			// we reached target, so we move there is less than one meter		
			switch (this.TargetKind) { // switch 1 

			case Targetobject:
				SensingeEnvironmentTrigger=Ontology.RespondersTriggers.ArrivedTargetObject;
				break;
			} // switch 1
		}// we reached target

		counterwalk ++;
		if (  counterwalk== 500 ) 
		{
			//System.out.println(this.countergiude + "errrrrrrrrrrrrrrr walk rrrrrrrrrrrrrrror  casulties  "+ this.ID + "  "+ this.Triage_tage + "  "+  this.Status+ "  "+  this.IcanMove + "IGetInstactionToWalk" + IGetInstactionToWalk_and_walking);   
			System.out.println("                                                                                                                                                    errrrrrrrrrrrrrrr walk rrrrrrrrrrrrrrror  casulties  "+ this.ID + "  "+ this.MoveTo + "  "+  this.FinalMoveTO + "  "+ WaitingToWalkCCS + 	WaitingToWalkRC +  this.UnderAction + SensingeEnvironmentTrigger);   

			counterwalk=0;
			this.ColorCode=4;
		}

	}

	//****************************************************************************************************************	
	private void walking_OneStep2() {

		Coordinate OldPosition=this.Return_CurrentLocation();	

		SelectionZone(); 

		angle = BuildStaticFuction.AngleBetween2CartesianPoints(this.Return_CurrentLocation(), MiddleLocation);							
		geography.moveByVector(this, 0.25, SI.METER, angle);

		// Update Search Zones Position As Well	
		Move_related_zones( OldPosition , this.Return_CurrentLocation());

	} 

	//****************************************************************************************************************	
	private  boolean CheckZone_SafeZoneCollision(List<TopographicArea>  nearObjects, double angle) 
	{
		boolean result=false;

		geography.moveByVector(safeZone, 0.25 * 1, SI.METER, angle);	

		if (safeZone.lookForObjects(nearObjects).size() == 0  ) 
			result=true;

		// return back to previous state (test ended)
		if (angle > Math.PI)
			geography.moveByVector(safeZone, 0.25 * 1, SI.METER, angle - Math.PI);
		else
			geography.moveByVector(safeZone, 0.25 * 1 , SI.METER, angle + Math.PI);


		return result;
	}

	//****************************************************************************************************************	
	private  boolean Check_SafeZoneInBuilding( Object  Build , double angle) 
	{
		boolean result=false;

		geography.moveByVector(safeZone, 0.25 * 1, SI.METER, angle);

		if ( ( geography.getGeometry(safeZone)).intersects(( geography.getGeometry(Build )) ) || ( geography.getGeometry(Build )).intersects(( geography.getGeometry(safeZone)) ) ||  ( geography.getGeometry(Build )).contains(( geography.getGeometry(safeZone)))    )
			result=true;

		// return back to previous state (test ended)
		if (angle > Math.PI)
			geography.moveByVector(safeZone, 0.25 * 1, SI.METER, angle - Math.PI);
		else
			geography.moveByVector(safeZone, 0.25* 1 , SI.METER, angle + Math.PI);


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

	List<TopographicArea> nearObjects =null;

	private  void SelectionZone()
	{
		//		//xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
		//		//1- Get All Near Geographical Objects
		//		@SuppressWarnings("unchecked") 
		//		List<TopographicArea> nearObjects = (List<TopographicArea>) BuildStaticFuction.GetObjectsWithinDistance(this,TopographicArea.class, 100 );
		//
		//
		//		// 2- Remove all non-building objects to get a list of obstacles such as building 
		//		for (int i = 0; i < nearObjects.size(); i++) {
		//			if (nearObjects.get(i).getcode() != 4) {
		//				nearObjects.remove(i);
		//				i--; //Because if delete one object , next object not yet checked will take the previous number
		//			}
		//		}


		if (    this.CurrentTick  >= ( Last_update_Vofvision + 30) )
		{
			//1- Get All Near Geographical Objects
			if (nearObjects !=null )  nearObjects.clear();

			this.nearObjects = (List<TopographicArea>) BuildStaticFuction.GetObjectsWithinDistance(this,TopographicArea.class, 100);

			// 2- Remove all non-building objects to get a list of obstacles such as building 
			for (int i = 0; i < nearObjects .size(); i++) {
				if (nearObjects .get(i).getcode() != 4) {
					nearObjects .remove(i);
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
				if ( zone.lookForObject(TargetObject)  )  
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

				if ( Check_SafeZoneInBuilding(TargetObject,angle ) ) 
				{
					MiddleLocation= DestinationLocation;
					bestZone = null;

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
	}

	//****************************************************************************************************************	
	public Coordinate Return_CurrentLocation()
	{
		return (geography.getGeometry(this)).getCoordinate();		
	}

	//****************************************************************************************************************	
	public void setLocation_and_move(Coordinate newPosition)
	{

		Coordinate  oldPosition= this.Return_CurrentLocation();

		// so we move agent 
		double x = (newPosition.x - oldPosition.x);
		double y = (newPosition.y - oldPosition.y);

		geography.moveByDisplacement(this, x, y);


		// Update Search Zones Position As Well	
		Move_related_zones(  oldPosition , this.Return_CurrentLocation());


	}

	//****************************************************************************************************************	
	public void Move_related_zones(Coordinate oldPosition ,Coordinate newPosition )
	{
		// so we move agent and all search zones and safe zone to exact position
		double x = (newPosition.x - oldPosition.x);
		double y = (newPosition.y - oldPosition.y);

		if ( DirectionZoneAgents != null)
		{


			for (Object_Zone zone : DirectionZoneAgents) {
				zone.SidePoint.x += x;
				zone.SidePoint.y += y;
				geography.moveByDisplacement(zone, x, y);
			}

			geography.moveByDisplacement(safeZone, x, y);
		}


	}

	//****************************************************************************************************************	
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

	//****************************************************************************************************************	
	public void Destroy_responder_Zones() {

		context.remove(safeZone);

		for (Object_Zone zone : DirectionZoneAgents) {
			context.remove(zone);
		}
		DirectionZoneAgents=null;
	}

	//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

}		