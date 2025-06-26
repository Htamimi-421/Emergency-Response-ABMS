package A_Agents;

import java.awt.Toolkit;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import A_Environment.Incident;
import A_Environment.RoadLink;
import A_Environment.RoadNode;
import B_Classes.Duration_info;
import B_Communication.ACL_Message;
import B_Communication.Report;
import C_SimulationInput.InputFile;
import C_SimulationOutput.Action;
import C_SimulationOutput.ChangeLineInFile;
import D_Ontology.BuildStaticFuction;
import D_Ontology.Ontology.Ambulance_ResponderRole;
import D_Ontology.Ontology.Callinformatiom;
import D_Ontology.Ontology.CasualtyAction;
import D_Ontology.Ontology.CasualtyLife;
import D_Ontology.Ontology.CasualtyinfromationType;
import D_Ontology.Ontology.CommunicationMechanism;
import D_Ontology.Ontology.EOCAction;
import D_Ontology.Ontology.EOCTriggers;
import D_Ontology.Ontology.Fire_ResponderRole;
import D_Ontology.Ontology.Police_ResponderRole;
import D_Ontology.Ontology.ReportSummery;
import D_Ontology.Ontology.ResourceAllocation_Strategy;
import D_Ontology.Ontology.StationAction;
import D_Ontology.Ontology.TacticalAreaType;
import D_Ontology.Ontology.TypeMesg;
import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.gis.Geography;
import repast.simphony.space.graph.Network;
import repast.simphony.space.graph.RepastEdge;

public class EOC {

	double  CurrentTick , EndofCurrentAction ;	
	ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule(); 


	// --------------------------------------------------------------
	EOCAction     Action; 
	public EOCTriggers ActionEffectTrigger , CommTrigger, InterpretedTrigger ,EndActionTrigger ;

	// --------------------------------------------------------------
	Incident  new_Incident, processing__Incident;
	Callinformatiom new_Incident_information;	
	Report CurrentReport ;
	int  NewneedofAmbulance =0;
	boolean double_crewed ;

	// --------------------------------------------------------------
	private Coordinate Location;
	private String Nodename_Location;
	public Context<Object> context;
	public Geography<Object> geography;

	//----------------------------------------------------------------
	//its Knowledge
	public List<Hospital> Hospital_list = new ArrayList<Hospital>();	
	List<Incident > Incident_list = new ArrayList<Incident >();

	public List<Station_Ambulance> Station_Ambulance_list = new ArrayList<Station_Ambulance>();	  
	public List<Station_Fire> Station_Fire_list = new ArrayList<Station_Fire>();
	public List<Station_Police> Station_Police_list = new ArrayList<Station_Police>();

	//----------------------------------------------------------------
	//Communication
	public boolean Acknowledged=false; 
	public int Lastmessagereaded=0 ;	
	public CommunicationMechanism Currentusedcom=null;
	public List<ACL_Message> Message_inbox = new ArrayList<ACL_Message>();
	private Responder CurrentSender;

	//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
	//                                                                           EOC behavior
	//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
	public EOC(Context<Object> _context, Geography<Object> _geography, Coordinate _Location,String _Nodename_Location) {

		Location= _Location;
		Nodename_Location=_Nodename_Location;
		Action=EOCAction.Noaction ;

		//------------------------------------------------------
		context=_context;
		geography=_geography;
		GeometryFactory fac = new GeometryFactory();
		context.add(this);		
		geography.move(this, fac.createPoint(Location));

	}
	public String getName() {
		return this. Nodename_Location;
	}
	//##############################################################################################################################################################

	public void  EOCInterpretationMessage()
	{
		boolean  done= true;
		ACL_Message currentmsg = Message_inbox.get(Lastmessagereaded);		 			
		CurrentSender= (Responder)currentmsg.sender;
		Lastmessagereaded++;

		switch( currentmsg.performative) {

		case InfromFirstArrival:
			CommTrigger= EOCTriggers.GetFANotfication ;
			CurrentReport =((Report ) currentmsg.content);
			break;
		case InformFirestSituationAssessmentReport:
			CommTrigger= EOCTriggers.GetFAReport;
			CurrentReport =((Report ) currentmsg.content);
			break;
		case InformSituationReport :
			CommTrigger= EOCTriggers.GetReport;
			CurrentReport =((Report ) currentmsg.content);
			break;
		case InformERendReport :
			CommTrigger= EOCTriggers.GeEndNotfication;			
			System.out.println("                                                                                                                                 " + "CommTrigger= EOCTriggers.GeEndNotfication;	" + CurrentSender.Id);
			CurrentReport =((Report ) currentmsg.content);

			break;}
	}
	//**********************************************************************************************************************
	//comunication
	public void call999(Incident _Incident ,  Callinformatiom  Callin )
	{

		this.new_Incident=_Incident;
		this.new_Incident_information=Callin ;
		this.new_Incident.Time_startResponde_Getcall= schedule.getTickCount();

		this.Incident_list.add(this.new_Incident );		
		this.CommTrigger=EOCTriggers.Get999call;

	}

	//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
	//                                                                            Allocation Action
	//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

	public void DispatchAmbulance(Incident _new_Incident , ResourceAllocation_Strategy RA , int Reqnum )
	{

		int  EOC_allocatedVnum=0 ,EOC_allocatedAdditionalRnum=0  ;

		//1- identify  how many resource to dispatch the incident information	
		switch(RA) {
		case NormalSituation:
			EOC_allocatedVnum=1 ;
			_new_Incident.Status=ResourceAllocation_Strategy.NormalSituation ;
			double_crewed=true;
			break;
		case StandBy:
			EOC_allocatedAdditionalRnum= 10 ; 
			EOC_allocatedVnum=6 ;
			_new_Incident.Status=ResourceAllocation_Strategy.StandBy;
			double_crewed=true;
			break;		
		case ConfirmedDeclared:
			EOC_allocatedAdditionalRnum= 3 ;
			EOC_allocatedVnum=3 ;
			_new_Incident.Status=ResourceAllocation_Strategy.ConfirmedDeclared;
			double_crewed=true;
			break;
		case FirstTimeDeclared:
			EOC_allocatedAdditionalRnum= 6  ;
			EOC_allocatedVnum=5 ;
			_new_Incident.Status=ResourceAllocation_Strategy.FirstTimeDeclared;
			double_crewed=true;
			break;
		case AdditionalResourceRequest:			
			EOC_allocatedVnum=1  * Reqnum ;
			_new_Incident.Status=ResourceAllocation_Strategy.AdditionalResourceRequest;
			double_crewed=false;
			break;
		}

		//2- identify the nearest station to dispatch the incident information	
		do
		{
			//1-----
			Station_Ambulance nominatedA=null;
			double MinDistance=999999999,dis ;
			for(Station_Ambulance AmbSt : Station_Ambulance_list)
			{

				if ( AmbSt.getAvailableVehiclesCount() >=1  && AmbSt.Full )
				{	
					dis= BuildStaticFuction.Generate_Shortest_Path_byusingNode(context,geography , _new_Incident.BasicNode    ,AmbSt.Node ,true);
					//dis=  BuildStaticFuction.Generate_Shortest_Path_byusingNode(context,geography , this.new_Incident.Node,AmbSt.Node ,true);

					if (dis <MinDistance )
					{
						nominatedA=AmbSt;
						MinDistance=dis;
					}
				}	

			} // each AmbStation 

			//2----- full or part
			if (  EOC_allocatedVnum <= nominatedA.getAvailableVehiclesCount() )
			{

				nominatedA.CommandSationtoResponse(_new_Incident , EOC_allocatedVnum , EOC_allocatedAdditionalRnum , double_crewed);
				System.out.println("EOC _____________________full___________________________ "+ nominatedA.ID +  "  V " + EOC_allocatedVnum +"...  Addtional R " + EOC_allocatedAdditionalRnum +" for : "+  _new_Incident.ID +  _new_Incident.Status);
				_new_Incident.Amballocated=_new_Incident.Amballocated + EOC_allocatedVnum ;	
				_new_Incident.ARallocated=_new_Incident.ARallocated + EOC_allocatedAdditionalRnum ;

				EOC_allocatedVnum=0 ; EOC_allocatedAdditionalRnum=0  ;
			}
			else
			{
				int sendV= nominatedA.getAvailableVehiclesCount() ; 
				int sendR=0 ;
				EOC_allocatedVnum =EOC_allocatedVnum-sendV ;

				if (EOC_allocatedAdditionalRnum > 0  ) {sendR=sendV ; EOC_allocatedAdditionalRnum= EOC_allocatedAdditionalRnum -sendR ;  }

				nominatedA.CommandSationtoResponse(_new_Incident ,sendV , sendR ,double_crewed );
				_new_Incident.Amballocated=_new_Incident.Amballocated + sendV ;
				_new_Incident.ARallocated=_new_Incident.ARallocated +  sendR ;		
				System.out.println("EOC ____________________Part____________________________ "+ nominatedA.ID +  "  V " + sendV +"...  Addtional R " + sendR +" for : "+  _new_Incident.ID +  _new_Incident.Status);
				nominatedA.Full=false;
			}


		} while ( EOC_allocatedVnum!=0 ) ;


		if ( EOC_allocatedAdditionalRnum!=0  )  System.out.println("EOC ____________________errrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrror __");
	}

	public void DispatchFire(Incident _new_Incident , ResourceAllocation_Strategy RA , int Reqnum )
	{

		int  EOC_allocatedVnum=0 ,EOC_allocatedAdditionalRnum=0  ;


		//1- identify  how many resource to dispatch the incident information	
		switch(RA) {
		case NormalSituation:
			EOC_allocatedVnum=1 ;
			_new_Incident.Status=ResourceAllocation_Strategy.NormalSituation ;
			double_crewed=true;
			break;
		case StandBy:
			EOC_allocatedAdditionalRnum= 0 ; //0
			EOC_allocatedVnum=5 ;//10
			_new_Incident.Status=ResourceAllocation_Strategy.StandBy;
			double_crewed=true;   //2
			break;		
		case ConfirmedDeclared:
			EOC_allocatedAdditionalRnum= 3 ;
			EOC_allocatedVnum=3 ;
			_new_Incident.Status=ResourceAllocation_Strategy.ConfirmedDeclared;
			double_crewed=true;
			break;
		case FirstTimeDeclared:
			EOC_allocatedAdditionalRnum= 6  ;
			EOC_allocatedVnum=5 ;
			_new_Incident.Status=ResourceAllocation_Strategy.FirstTimeDeclared;
			double_crewed=true;
			break;
		case AdditionalResourceRequest:			
			EOC_allocatedVnum=1  * Reqnum ;
			_new_Incident.Status=ResourceAllocation_Strategy.AdditionalResourceRequest;
			double_crewed=false;
			break;
		}

		//2- identify the nearest station to dispatch the incident information	
		do
		{
			//1-----
			Station_Fire nominatedA=null;
			double MinDistance=999999999,dis ;
			for(Station_Fire  FirSt : Station_Fire_list)
			{

				if (  FirSt.getAvailableVehiclesCount() >=1  &&  FirSt.Full )
				{	
					dis= BuildStaticFuction.Generate_Shortest_Path_byusingNode(context,geography , _new_Incident.BasicNode    , FirSt.Node ,true);
					//dis=  BuildStaticFuction.Generate_Shortest_Path_byusingNode(context,geography , this.new_Incident.Node,AmbSt.Node ,true);

					if (dis <MinDistance )
					{
						nominatedA= FirSt;
						MinDistance=dis;
					}
				}	

			} // each AmbStation 

			//2----- full or part
			if (  EOC_allocatedVnum <= nominatedA.getAvailableVehiclesCount() )
			{

				nominatedA.CommandSationtoResponse(_new_Incident , EOC_allocatedVnum , EOC_allocatedAdditionalRnum , double_crewed);
				System.out.println("Fire EOC _____________________full___________________________ "+ nominatedA.ID +  "  V " + EOC_allocatedVnum +"...  Addtional R " + EOC_allocatedAdditionalRnum +" for : "+  _new_Incident.ID +  _new_Incident.Status);
				_new_Incident.Firallocated=_new_Incident.Firallocated + EOC_allocatedVnum ;	
				_new_Incident.FRallocated=_new_Incident.FRallocated + EOC_allocatedAdditionalRnum ;

				EOC_allocatedVnum=0 ; EOC_allocatedAdditionalRnum=0  ;
			}
			else
			{
				int sendV= nominatedA.getAvailableVehiclesCount() ; 
				int sendR=0 ;
				EOC_allocatedVnum =EOC_allocatedVnum-sendV ;

				if (EOC_allocatedAdditionalRnum > 0  ) {sendR=sendV ; EOC_allocatedAdditionalRnum= EOC_allocatedAdditionalRnum -sendR ;  }

				nominatedA.CommandSationtoResponse(_new_Incident ,sendV , sendR ,double_crewed );
				_new_Incident.Firallocated=_new_Incident.Firallocated + sendV ;
				_new_Incident.FRallocated=_new_Incident.FRallocated +  sendR ;		
				System.out.println("Fire EOC ____________________Part____________________________ "+ nominatedA.ID +  "  V " + sendV +"...  Addtional R " + sendR +" for : "+  _new_Incident.ID +  _new_Incident.Status);
				nominatedA.Full=false;
			}


		} while ( EOC_allocatedVnum!=0 ) ;


		if ( EOC_allocatedAdditionalRnum!=0  )  System.out.println("Fire EOC ____________________errrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrror __");
	}

	public void DispatchPolice(Incident _new_Incident , ResourceAllocation_Strategy RA , int Reqnum )
	{

		int  EOC_allocatedVnum=0 ,EOC_allocatedAdditionalRnum=0  ;


		//1- identify  how many resource to dispatch the incident information	
		switch(RA) {
		case NormalSituation:
			EOC_allocatedVnum=1 ;
			_new_Incident.Status=ResourceAllocation_Strategy.NormalSituation ;
			double_crewed=true;
			break;
		case StandBy:
			EOC_allocatedAdditionalRnum= 10 ;//3
			EOC_allocatedVnum= 6 ;
			_new_Incident.Status=ResourceAllocation_Strategy.StandBy;
			double_crewed=true;
			break;		
		case ConfirmedDeclared:
			EOC_allocatedAdditionalRnum= 3 ;
			EOC_allocatedVnum=3 ;
			_new_Incident.Status=ResourceAllocation_Strategy.ConfirmedDeclared;
			double_crewed=true;
			break;
		case FirstTimeDeclared:
			EOC_allocatedAdditionalRnum= 6  ;
			EOC_allocatedVnum=5 ;
			_new_Incident.Status=ResourceAllocation_Strategy.FirstTimeDeclared;
			double_crewed=true;
			break;
		case AdditionalResourceRequest:			
			EOC_allocatedVnum=1  * Reqnum ;
			_new_Incident.Status=ResourceAllocation_Strategy.AdditionalResourceRequest;
			double_crewed=false;
			break;
		}

		//2- identify the nearest station to dispatch the incident information	
		do
		{
			//1-----
			Station_Police nominatedA=null;
			double MinDistance=999999999,dis ;
			for(Station_Police POLSt : Station_Police_list)
			{

				if ( POLSt.getAvailableVehiclesCount() >=1  && POLSt.Full )
				{	
					dis= BuildStaticFuction.Generate_Shortest_Path_byusingNode(context,geography , _new_Incident.BasicNode    ,POLSt.Node ,true);
					//dis=  BuildStaticFuction.Generate_Shortest_Path_byusingNode(context,geography , this.new_Incident.Node,AmbSt.Node ,true);

					if (dis <MinDistance )
					{
						nominatedA=POLSt;
						MinDistance=dis;
					}
				}	

			} // each Station 

			//2----- full or part
			if (  EOC_allocatedVnum <= nominatedA.getAvailableVehiclesCount() )
			{

				nominatedA.CommandSationtoResponse(_new_Incident , EOC_allocatedVnum , EOC_allocatedAdditionalRnum , double_crewed);
				System.out.println("Police EOC _____________________full___________________________ "+ nominatedA.ID +  "  V " + EOC_allocatedVnum +"...  Addtional R " + EOC_allocatedAdditionalRnum +" for : "+  _new_Incident.ID +  _new_Incident.Status);
				_new_Incident.Polallocated=_new_Incident.Polallocated + EOC_allocatedVnum ;	
				_new_Incident.PRallocated=_new_Incident.PRallocated + EOC_allocatedAdditionalRnum ;

				EOC_allocatedVnum=0 ; EOC_allocatedAdditionalRnum=0  ;
			}
			else
			{
				int sendV= nominatedA.getAvailableVehiclesCount() ; 
				int sendR=0 ;
				EOC_allocatedVnum =EOC_allocatedVnum-sendV ;

				if (EOC_allocatedAdditionalRnum > 0  ) {sendR=sendV ; EOC_allocatedAdditionalRnum= EOC_allocatedAdditionalRnum -sendR ;  }

				nominatedA.CommandSationtoResponse(_new_Incident ,sendV , sendR ,double_crewed );
				_new_Incident.Polallocated=_new_Incident.Polallocated + sendV ;
				_new_Incident.PRallocated=_new_Incident.PRallocated +  sendR ;		
				System.out.println("Police EOC ____________________Part____________________________ "+ nominatedA.ID +  "  V " + sendV +"...  Addtional R " + sendR +" for : "+  _new_Incident.ID +  _new_Incident.Status);
				nominatedA.Full=false;
			}

		} while ( EOC_allocatedVnum!=0 ) ;


		if ( EOC_allocatedAdditionalRnum!=0  )  System.out.println("Police EOC ____________________errrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrror __");
	}

	//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
	//                                                                           Interpret Action
	//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
	public void EndIncidentAction(Incident incd ,ReportSummery _RS )
	{

		//---------------------------- update
		if (   _RS ==ReportSummery.EndER_Ambulance) incd.ER_Ambulance= true;
		if (   _RS ==ReportSummery.EndER_Fire) incd.ER_Fire= true;
		if (   _RS ==ReportSummery.EndER_Police) incd.ER_Police= true;

		if (  incd.ER_Police && incd.ER_Fire&& incd.ER_Ambulance )		
			incd .Active22= false;

		//-------------------------- ckeck 
		boolean NomoreIncednt= true;
		for (  Incident _incd : Incident_list) 
			if (_incd.Active22==true )
			{NomoreIncednt=false ;break;}

		if (NomoreIncednt )
			InterpretedTrigger =EOCTriggers.EndER ;

	}

	//**********************************************************************************************************************
	private boolean Check_AllResoursesBacktoStation()
	{
		boolean AllResoursesBack= true;

		//Ambulance
		for ( Station_Ambulance St : Station_Ambulance_list )
			if ( ( St.Action != StationAction.Idle ) )
			{ AllResoursesBack= false;};

			//Fire 
			for ( Station_Fire St : Station_Fire_list )
				if ( ( St.Action != StationAction.Idle ) )
				{ AllResoursesBack= false;};


				//Police
				for ( Station_Police St : Station_Police_list )
					if ( ( St.Action != StationAction.Idle ) )
					{ AllResoursesBack= false;};

					return AllResoursesBack;
	}

	//**********************************************************************************************************************
	private boolean Check_AssessSituation(Report   CR)
	{
		boolean NeedApdopt= false;

		if ( CR.TotalCasualtiesRed_nottransfer>0   &&  CR.fromIncident.Amballocated!=20 )  //20   && CR.TotalCasualtiesRed_nottransfer>  CR.fromIncident.Amballocated
		{

			NewneedofAmbulance=CR.TotalCasualtiesRed_nottransfer;

			if ((CurrentReport.fromIncident.Amballocated +  NewneedofAmbulance) <=20)   //<=20
			{NeedApdopt=true; }
			else
			{ NewneedofAmbulance =20-CurrentReport.fromIncident.Amballocated ;NeedApdopt=true; }
		}

		NeedApdopt= false ;
		return NeedApdopt;
	}

	//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
	//                                                                           EOC behavior
	//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
	// it like the brain(behavior) of EOC
	@ScheduledMethod(start = 0, interval = 1 , priority= ScheduleParameters.LAST_PRIORITY)
	public void step() {	

		CurrentTick=schedule.getTickCount() ;

		if(EndofCurrentAction !=0)
		{

			EndofCurrentAction--;
			if (EndofCurrentAction == 0 )
				EndActionTrigger= EOCTriggers.EndingAction ;	

		}
		else if (  Lastmessagereaded < Message_inbox.size()  &&  this.Action== EOCAction.Noaction ) // Communication Trigger priority 1	
		{
			EOCInterpretationMessage();
		}

		//*************************************************************************
		// ++++++ 1- +++++++
		if( this.Action== EOCAction.Noaction  && (CommTrigger==EOCTriggers.Get999call )  )
		{	
			processing__Incident=this.new_Incident;

			Action= EOCAction.DispatchingtoStation;			
			EndofCurrentAction=  InputFile.dispatch_EOC_duration ;   
			CommTrigger=null; 

		}
		// ++++++ 2- +++++++
		else if (this.Action== EOCAction.DispatchingtoStation    &&  EndActionTrigger== EOCTriggers.EndingAction  )
		{	
			processing__Incident.Active22=true;			
			if (this.new_Incident_information== Callinformatiom.NoInformation)
			{
				this.DispatchAmbulance(processing__Incident , ResourceAllocation_Strategy.NormalSituation ,0); 
				this.DispatchFire(processing__Incident , ResourceAllocation_Strategy.NormalSituation ,0); 
				this.DispatchPolice(processing__Incident , ResourceAllocation_Strategy.NormalSituation ,0); 
			}
			else if (this.new_Incident_information== Callinformatiom.ThereisInformation)
			{
				this.DispatchAmbulance(processing__Incident ,ResourceAllocation_Strategy.StandBy,0); 	
				this.DispatchFire(processing__Incident , ResourceAllocation_Strategy.StandBy ,0); 
				this.DispatchPolice(processing__Incident , ResourceAllocation_Strategy.StandBy ,0); 
			}

			Action= EOCAction.Noaction;
			EndActionTrigger=null;						
		}
		// ++++++ 3- +++++++
		else if (this.Action== EOCAction.Noaction   && ( CommTrigger== EOCTriggers.GetFANotfication || CommTrigger== EOCTriggers.GetFAReport  || CommTrigger== EOCTriggers.GetReport ||  CommTrigger== EOCTriggers.GeEndNotfication)  )
		{	
			//System.out.println("EOC geting");
			Action=EOCAction.GetReport ;			
			EndofCurrentAction=  InputFile.GetReport_EOC_duration  ; 	

		}	
		// ++++++ 4- +++++++
		else if (Action==EOCAction.GetReport  &&  ( CommTrigger== EOCTriggers.GetFANotfication || CommTrigger== EOCTriggers.GeEndNotfication ) && EndActionTrigger== EOCTriggers.EndingAction  ) //1
		{	

			CurrentSender.Acknowledg(this);

			//do some thing
			if (CommTrigger== EOCTriggers.GetFANotfication )
			{
				if ( CurrentReport.RS== ReportSummery.ConfirmedorDeclared && CurrentReport.fromIncident.Status==ResourceAllocation_Strategy.NormalSituation )
				{
					//this.DispatchAmbulance(CurrentReport.fromIncident ,ResourceAllocation_Strategy.FirstTimeDeclared ,0);
					//this.DispatchFire(CurrentReport.fromIncident ,ResourceAllocation_Strategy.FirstTimeDeclared ,0);
					//this.DispatchPolice(CurrentReport.fromIncident ,ResourceAllocation_Strategy.FirstTimeDeclared ,0);
				}
				else
				{
					//this.DispatchAmbulance(CurrentReport.fromIncident ,ResourceAllocation_Strategy.ConfirmedDeclared ,0);
					//this.DispatchFire(CurrentReport.fromIncident ,ResourceAllocation_Strategy.ConfirmedDeclared ,0);
					//this.DispatchPolice(CurrentReport.fromIncident ,ResourceAllocation_Strategy.ConfirmedDeclared ,0);
				}
			}
			else if (CommTrigger== EOCTriggers.GeEndNotfication)
				EndIncidentAction(CurrentReport.fromIncident , CurrentReport.RS );

			CurrentReport=null;			
			Action= EOCAction.Noaction;
			EndActionTrigger=null;	
			CommTrigger=null;			
		}	
		// ++++++ 5- +++++++
		else if (  Action==EOCAction.GetReport  && 	(CommTrigger== EOCTriggers.GetFAReport|| CommTrigger== EOCTriggers.GetReport) && EndActionTrigger== EOCTriggers.EndingAction  )												
		{		

			CurrentSender.Acknowledg(this);

			Action= EOCAction.AsessSituationofInciedent;
			EndofCurrentAction=  InputFile.AssessSituation_EOC_duration  ; 
			EndActionTrigger=null;	

		}
		// ++++++ 6- +++++++
		else if (  Action==EOCAction.AsessSituationofInciedent  && 	EndActionTrigger== EOCTriggers.EndingAction  )													
		{		

			if ( Check_AssessSituation( CurrentReport)   )
			{
				//this.DispatchAmbulance(CurrentReport.fromIncident ,ResourceAllocation_Strategy.AdditionalResourceRequest , NewneedofAmbulance);
				//this.DispatchFire(CurrentReport.fromIncident ,ResourceAllocation_Strategy.AdditionalResourceRequest , NewneedofAmbulance);
				//this.DispatchPolice(CurrentReport.fromIncident ,ResourceAllocation_Strategy.AdditionalResourceRequest , NewneedofAmbulance);
			}

			else
			{
				//do some thing
			}

			CurrentReport=null;	
			Action= EOCAction.Noaction;
			EndActionTrigger=null;	
			CommTrigger=null;
		}

		// ++++++ 7- +++++++	
		else if(   this.Action== EOCAction.Noaction   && InterpretedTrigger == EOCTriggers.EndER   )//InterpretedTrigger
		{


			if (Check_AllResoursesBacktoStation()   )
			{ 
				System.out.println("EOC done"  );
				System.out.println("==================================================================================================");
				System.out.println("                                         Final Report                                             ");
				System.out.println("==================================================================================================");

				for (Incident incd  :Incident_list  )
				{											
					Performance_Metrics( incd ) ; 
					Coordinationcost_numberofMesages(incd) ;

					ReportCasualty_V_time(   incd  ) ;
					ReportCasualtydead_V_time(incd) ;

					Responders_Report_Amb(incd ,true);
					Responders_Report_fire(incd ,true);
					Responders_Report_Police(incd ,true);

					//Casualties_Report( incd );	//print inscreen	
					Casualty_Report(incd ,true ); 	
					Print_Result( incd ) ;
					ReportComInts(   incd  ) ;
				}

				System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$" + InputFile.CurrentExp +" $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$") ;
				System.out.println("Ibrahim   & Hanan  $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$") ;
				Toolkit.getDefaultToolkit().beep();  

				RunEnvironment.getInstance().endRun();			
			}
		}

	}

	//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
	//                                       Communication
	//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
	public boolean SendMessagex( ACL_Message msg )  
	{
		boolean flag=false ;

		//---------------------------------------
		// 1- check availability of current CommunicationMechanism
		if ( msg.receiver instanceof Responder  ) 
		{						
			switch (msg.ComMech) {	
			case Phone: 
				flag=((Responder) msg.receiver).RecivedMessage(msg );
				Currentusedcom=msg.ComMech ;	
				if (flag ) UpdateComNet(msg.receiver );						
				break;
				//			case ISApplication: // Electronic
				//				flag=((Responder) msg.receiver).RecivedMessage(msg );
				//				Currentusedcom=msg.ComMech ;	
				//				if (flag ) UpdateComNet(msg.receiver );
				//				break;

			}			
		}
		//---------------------------------------
		return flag;	
	}

	private void UpdateComNet(Object receiver )
	{
		Network net = (Network)context.getProjection("Comunication_network");
		RepastEdge<Object> edg_temp;		
		edg_temp = net.getEdge(this,receiver );
		if(edg_temp!=null)
			edg_temp.setWeight(edg_temp.getWeight() + 1) ;
		else
			net.addEdge(this,receiver , 1);
	}

	public boolean RecivedMessage(ACL_Message msg ) //in Receiver
	{		
		boolean   Acknowledged =true;	
		switch ( msg.ComMech) {	
		case Phone : 
			Message_inbox.add(msg);	
			msg.time= this.CurrentTick ;
			break;
			//		case ISApplication: 				
			//			//added directly in system				
			//			((Responder) msg.sender). Acknowledged=true;
			//			break;
		}

		return  Acknowledged ;
	}

	public void Acknowledg() //in Sender  called
	{
		//	if (Currentusedcom==CommunicationMechanism.Phone ) 
		//	this.assignedIncident.Off_RadioSystem();

		//  Acknowledged=true;
	}

	//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
	//                                       output
	//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

	int ResponseTime=0;
	double Mrate=0; int TotalDead=0;
	int TotalKnow=0; int Totallog=0;
	int FirstcasualtytransferedtoCCSorRC=0 , LastcasualtytransferedtoCCSorRC=0,FirstcasualtytransferedtoH=0, LastcasualtytransferedtoH=0 ; 
	double AveragewaitingtimeinCCS=0 ;
	int MinTimeAdmissionHospital_Red=0,MaxTimeAdmissionHospital_Red=0 ,MaxTimeAdmissionHospital_allTriage=0 ;
	int MinTimeAdmissionHospital_Red_beforeHos=0, MaxTimeAdmissionHospital_Red_beforeHos=0 ;
	int EvacuationTime_ImpactArea=0 ;
	//----------------------
	//dead
	int NumberDeadwaitingPre=0;
	int NumberDeadEvacuation=0;
	int NumberDeadinCCS=0;
	int NumberDeadinOther=0;
	int NumberDeadinScene=0;

	//----------------------
	//traffic- wrakge
	double total_Wreckage_usedBefor=0,total_Wreckage_usedAfter=0 ;
	double total_Traffic_usedBefor=0,total_Traffic_usedAfter =0;

	double Percentage_of_use_beforecleaningT=0 ; 
	double Percentage_of_use_beforecleaningW=0 ; 
	double Percentage_of_use_aftercleaningT=0 ;
	double Percentage_of_use_aftercleaningW=0 ;
	//----------------------
	int NofMesg=0;
	double Com_Amb=0  , Com_Fir=0 ,Com_Pol=0  ;
	double idle_Amb=0  , idle_Fir=0 ,idle_Pol=0  ;
	double delay_Amb=0  , delay_Fir=0 ,delay_Pol=0  ;
	//----------------------

	double delay_Amb_External=0  , delay_Fir_External=0 ,delay_Pol_External=0  ;
	double delay_Amb_internal=0  , delay_Fir_internal=0 ,delay_Pol_internal=0  ;
	double Com_Amb_External=0  , Com_Fir_External=0 ,Com_Pol_External=0  ;
	double Com_Amb_internal=0  , Com_Fir_internal=0 ,Com_Pol_internal=0  ;
	//----------------------
	double  RU_Driver=0 ,RU_paramdic=0 ,   RU_FireFighter=0 ,RU_Police=0  ;
	double  CE_Driver=0 ,CE_paramdic=0 ,   CE_FireFighter=0 ,CE_Police=0  ;
	int total_Driver=0, total_paramdic=0,  total_FireFighter=0  , total_Police=0 ;

	//not used
	double RU_commander=0,CE_commander;
	int total_commander=0 ;
	int TotalRespondersDispached=0;
	//********************************************************************************************************************************************************
	public void Performance_Metrics(   Incident incd1   ) 
	{

		System.out.println(" _________________________Performance Metrics___________________________ " );		
		System.out.println(incd1.ID );

		int Casu_totalinCCS=0;
		double TotalLifeatBegining=incd1.casualties.size() ;

		ResponseTime=0; Mrate=0;  TotalDead=0;		 
		TotalKnow=0;TotalDead=0;
		FirstcasualtytransferedtoCCSorRC=999999999 ; LastcasualtytransferedtoCCSorRC=0;FirstcasualtytransferedtoH=999999999; LastcasualtytransferedtoH=0;
		AveragewaitingtimeinCCS=0 ; 
		MinTimeAdmissionHospital_Red=999999999 ; MaxTimeAdmissionHospital_Red=0 ; MaxTimeAdmissionHospital_allTriage=0 ;   
		MinTimeAdmissionHospital_Red_beforeHos=999999999 ;MaxTimeAdmissionHospital_Red_beforeHos=0;

		//-----------------
		NumberDeadinScene=0;
		NumberDeadwaitingPre=0;
		NumberDeadinCCS=0;
		NumberDeadEvacuation=0;	   
		NumberDeadinOther=0;

		for (   Casualty ca : incd1.casualties) 				
		{ 				
			if (ca.Life== CasualtyLife.Dead) 
			{
				TotalDead++ ;

				if ( ca.Dead_duringAction== CasualtyAction.NoResponse || ca.Dead_duringAction== CasualtyAction.FieldTriage  || ca.Dead_duringAction== CasualtyAction.WaitingTransferDelay    || ca.Dead_duringAction== CasualtyAction.TransferToCCS  )
					NumberDeadinScene++;

				else if ( ca.Dead_duringAction== CasualtyAction.WaitingPre_Treatment  ||  ca.Dead_duringAction== CasualtyAction.Pre_Treatment   )
					NumberDeadwaitingPre++;			

				else if ( ca.Dead_duringAction== CasualtyAction.WaitingSecondTriage || ca.Dead_duringAction== CasualtyAction.SecondTriage  || ca.Dead_duringAction== CasualtyAction.WaitingTreatment|| ca.Dead_duringAction== CasualtyAction.Treatment )
					NumberDeadinCCS++;

				else if ( ca.Dead_duringAction== CasualtyAction.WaitingTransfertoV ||  ca.Dead_duringAction== CasualtyAction.TransferTovichel ||  ca.Dead_duringAction== CasualtyAction.LoadinginAmbulance  
						|| ca.Dead_duringAction== CasualtyAction.Travallingtohospital ||  ca.Dead_duringAction== CasualtyAction.DownloadfromAmbulance  )
					NumberDeadEvacuation++;
				else
					NumberDeadinOther++;
				
			}

			if (ca.DoneLog== true) Totallog++ ;

			if (ca.PoliceCI== CasualtyinfromationType.Evacuated_KnowHosp  || ca.PoliceCI== CasualtyinfromationType.Deceased  || ca.PoliceCI== CasualtyinfromationType.SurvivorinRC  ) TotalKnow++ ;



			if (    ca.TA !=null && ca.TA == TacticalAreaType.CCS && ca.TimeinCCSorRC != 0  && ( ca.TimeinCCSorRC  < FirstcasualtytransferedtoCCSorRC) )
				FirstcasualtytransferedtoCCSorRC  = (int) ca.TimeinCCSorRC  ;

			if ( ca.TA !=null && ca.TA == TacticalAreaType.CCS && ca.TA !=null   &&  ca.TimeinCCSorRC  > LastcasualtytransferedtoCCSorRC     )
				LastcasualtytransferedtoCCSorRC = (int) ca.TimeinCCSorRC  ;


			if (ca.Current_SurvivalProbability !=100 )  // for Injured only
			{								
				if (ca.TimeLeaveScene!=0  && ca.TimeLeaveScene < FirstcasualtytransferedtoH)
					FirstcasualtytransferedtoH=(int) ca.TimeLeaveScene  ;

				if (ca.TimeLeaveScene > LastcasualtytransferedtoH)
					LastcasualtytransferedtoH=(int) ca.TimeLeaveScene  ;

				if (   ca.TimeAdmissioninHospital  > MaxTimeAdmissionHospital_allTriage )
					MaxTimeAdmissionHospital_allTriage= (int) ca.TimeAdmissioninHospital ;




				if ( ca.Triage_tage==1 )
				{
					if (   ca.TimeAdmissioninHospital  > MaxTimeAdmissionHospital_Red  )
						MaxTimeAdmissionHospital_Red= (int) ca.TimeAdmissioninHospital ;					

					if (ca.TimeAdmissioninHospital !=0  && ca.TimeAdmissioninHospital < MinTimeAdmissionHospital_Red )
						MinTimeAdmissionHospital_Red=(int) ca.TimeAdmissioninHospital ;
				}

				if ( ca.Triage_tageBeforHosp==1 )
				{
					if (   ca.TimeAdmissioninHospital  > MaxTimeAdmissionHospital_Red_beforeHos )
						MaxTimeAdmissionHospital_Red_beforeHos= (int) ca.TimeAdmissioninHospital ;	


					if (ca.TimeAdmissioninHospital !=0  && ca.TimeAdmissioninHospital < MinTimeAdmissionHospital_Red_beforeHos )
						MinTimeAdmissionHospital_Red_beforeHos=(int) ca.TimeAdmissioninHospital ;


				}



				//				if ( ca.Triage_tage==2 )
				//				{
				//					if (   ca.TimeAdmissioninHospital  > MaxTimeAdmissionHospital_Red  )
				//						MaxTimeAdmissionHospital_Red= (int) ca.TimeAdmissioninHospital ;					
				//
				//					if (ca.TimeAdmissioninHospital !=0  && ca.TimeAdmissioninHospital < MinTimeAdmissionHospital_Red )
				//						MinTimeAdmissionHospital_Red=(int) ca.TimeAdmissioninHospital ;
				//				}
				//				
				//				if ( ca.Triage_tage==3 )
				//				{
				//					if (   ca.TimeAdmissioninHospital  > MaxTimeAdmissionHospital_Red  )
				//						MaxTimeAdmissionHospital_Red= (int) ca.TimeAdmissioninHospital ;					
				//
				//					if (ca.TimeAdmissioninHospital !=0  && ca.TimeAdmissioninHospital < MinTimeAdmissionHospital_Red )
				//						MinTimeAdmissionHospital_Red=(int) ca.TimeAdmissioninHospital ;
				//				}


				if (ca.TimeinCCSorRC !=0  ) //means they are trasferd to CC
				{
					AveragewaitingtimeinCCS= ca.waitStriage+ca.wait_treatment+ca.wait_Vtransfer ;
					Casu_totalinCCS++;	
				}


			}

		}

		Mrate = (TotalDead/ TotalLifeatBegining);		 
		EvacuationTime_ImpactArea=  (int) (LastcasualtytransferedtoCCSorRC - incd1.SB) ;	
		AveragewaitingtimeinCCS= AveragewaitingtimeinCCS/Casu_totalinCCS ;

		//way 1
		//incd1.Time_endResponde_lastcsaualty= MaxTimeAdmissionHospital_allTriage ;
		//ResponseTime= (int) (incd1.Time_endResponde_lastcsaualty - incd1.Time_startResponde_Getcall);

		//way 2	
		ResponseTime= (int) (incd1.Time_endResponde_lastvehiclearrivedBS- incd1.Time_startResponde_Getcall);		
		ResponseTime=ResponseTime ;

		//		TotalRespondersDispached= incd1.AICcommander.CurrentCalssRole3.MyResponder_Ambulance.size() + 1;
		//
		//		if ( TotalRespondersDispached != ( incd1.Amballocated  * 2 +incd1.ARallocated  ) )
		//		{
		//			System.out.println(" ___________________________________________________  errrrrrror in Resouser allocation  ___________________________________________________ " );	
		//			System.out.println( " tottal   " + TotalRespondersDispached + " Am  " +  incd1.Amballocated  + " AR   " + incd1.ARallocated   );	
		//
		//		}

		total_Wreckage_usedBefor=0;total_Wreckage_usedAfter=0 ;
		total_Traffic_usedBefor=0;total_Traffic_usedAfter =0;

		for (RoadLink RL: incd1.nearObjects_RLwithTrandWR_whole  )
		{
			if ( RL.print_ReportT==true )
			{
				total_Traffic_usedBefor=total_Traffic_usedBefor + RL.Traffic_usedBefor ;
				total_Traffic_usedAfter=total_Traffic_usedAfter+ RL.Traffic_usedAfter ;
			}

			if ( RL.print_ReportW==true )
			{
				total_Wreckage_usedBefor=total_Wreckage_usedBefor + RL.Wreckage_usedBefor ;
				total_Wreckage_usedAfter=total_Wreckage_usedAfter + RL.Wreckage_usedAfter ;
			}
		}

		Percentage_of_use_beforecleaningT= (total_Traffic_usedBefor /   ( total_Traffic_usedBefor + total_Traffic_usedAfter));
		Percentage_of_use_beforecleaningW=(total_Wreckage_usedBefor / ( total_Wreckage_usedBefor +  total_Wreckage_usedAfter)) ;
		Percentage_of_use_aftercleaningT= (total_Traffic_usedAfter /   ( total_Traffic_usedBefor + total_Traffic_usedAfter));
		Percentage_of_use_aftercleaningW=(total_Wreckage_usedAfter / ( total_Wreckage_usedBefor +  total_Wreckage_usedAfter)) ;

	}

	//********************************************************************************************************************************************************
	public void Casualties_Report(Incident incd1 ) //print in screen
	{
		System.out.println(" _________________________Casualties_Report___________________________ " );
		System.out.println(incd1.ID );

		for (   Casualty ca : incd1.casualties) 				
		{ 				
			try {
				ca.LogFile.ReportaboutAction1(false,null);
				;
				;
				;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}  	

	//********************************************************************************************************************************************************
	public void Coordinationcost_numberofMesages( Incident incd1  ) 
	{
		//--------------------------------------		
		//ComunicationIntensity			
		ArrayList<Responder> Responder_done = new ArrayList<Responder>();
		int S= 0 ;
		//int E= 21600 ;  //6 houres
		int E= (int) incd1.Time_endResponde_lastvehiclearrivedBS ;
		int Min=0;
		while (S <= E )
		{		
			Duration_info  DF= new Duration_info(  S  ,  S+ 59 ,Min );

			incd1.ComInts.add(DF);
			S=S+ 60 ;
			Min++;
		}
		int Totalbox=0;
		//--------------------------------------		
		// # of Mesg
		//System.out.println(" _________________________Coordinationcost___________________________ " );
		System.out.println(incd1.ID );

		for (  ACL_Message  M :  incd1.PICcommander.Message_inboxMeeting )	
			incd1.PICcommander.Message_inbox.add(M);
		for (  ACL_Message  M :  incd1.AICcommander.Message_inboxMeeting )	
			incd1.AICcommander.Message_inbox.add(M);
		for (  ACL_Message  M :  incd1.FICcommander.Message_inboxMeeting )	
			incd1.FICcommander.Message_inbox.add(M);


		//--------------------------------------		
		int count=0,total=0;
		Network net = (Network)context.getProjection("Comunication_network");	
		Iterable<?> Edges_list = net.getEdges();

		for ( Object edg1 : Edges_list) 
		{

			if (  ((RepastEdge)edg1).getSource() instanceof Responder && ((RepastEdge)edg1).getTarget() instanceof Responder   )
			{

				Responder Res1=  (Responder)((RepastEdge)edg1).getSource();
				Responder Res2=  (Responder)((RepastEdge)edg1).getTarget();

				if ( Res1.assignedIncident == incd1  && Res2.assignedIncident == incd1)
				{
					int cost =(int) ((RepastEdge)edg1).getWeight();
					total=total+ cost;

					int x1=0 ,x2=0 ;
					for (  ACL_Message  M :  Res2.Message_inbox )		
						if ( (Responder) M.sender == Res1 )
							x1++;

					for (  ACL_Message  M :  Res1.Message_inbox )		
						if ( (Responder) M.sender == Res2 )
							x2++;


					if (cost != ( x1  + x2 ))
						System.out.println(++count + "-"+ Res1.Id  +"  to  "+ Res2.Id +"  is: cost="+cost + "  from Res1  " +      x1 + "  from Res2  " +  x2  );


					if (  ! Responder_done.contains( Res1 ) )
					{
						for (  ACL_Message  M :  Res1.Message_inbox )
						{
							for ( Duration_info  D :incd1.ComInts  )
								if (  M.time >= D.Range_S &&  M.time <= D.RangeE  )
								{ 
									D.countMesg++ ; D.countMesg_Moredetails=D.countMesg_Moredetails+M.NoofMesg; 
									if(M.TypeMesg1==TypeMesg.External)
									{
										D.countMesg_MoredetailExternal=D.countMesg_MoredetailExternal+M.NoofMesg ; 
										D.countMesg_External++; 
									}

									break; 
								}
						}

						Responder_done.add(Res1);Totalbox=  Totalbox+ Res1.Message_inbox.size();

						//						for (  ACL_Message  M :  Res1.Message_inbox )		
						//							if ( (Responder) M.sender ==null   )  //||  (Responder) M.receiver==null 
						//								System.out.println(++count + "-"+ Res1.Id  +" " + M.performative  );
					}

					if (  ! Responder_done.contains( Res2 ) )
					{
						for (  ACL_Message  M :  Res2.Message_inbox )
						{
							for ( Duration_info  D :incd1.ComInts  )
								if (  M.time >= D.Range_S &&  M.time <= D.RangeE  )
								{ 
									D.countMesg++ ;D.countMesg_Moredetails=D.countMesg_Moredetails+M.NoofMesg; 
									if(M.TypeMesg1==TypeMesg.External)
									{
										D.countMesg_MoredetailExternal=D.countMesg_MoredetailExternal+M.NoofMesg ; 
										D.countMesg_External++; 
									}
									break; 
								}
						}

						Responder_done.add(Res2);Totalbox=  Totalbox+ Res2.Message_inbox.size();

						//						for (  ACL_Message  M :  Res2.Message_inbox )		
						//							if ( (Responder) M.sender ==null   )  //||  (Responder) M.receiver==null 
						//								System.out.println(++count + "-"+ Res2.Id  +" " + M.performative  );
					}


				}
			}

			if (  ((RepastEdge)edg1).getSource() instanceof EOC || ((RepastEdge)edg1).getTarget() instanceof EOC   )
			{


			}



		} // end link loop

		NofMesg= total;  //Here
		System.out.println("  ------------------------------------------------------ " );
		System.out.println(" 1-The total = "+total );
		System.out.println(" 1-The Totalbox = "+Totalbox );




	}  

	//********************************************************************************************************************************************************
	public void Print_Result( Incident incd1 )
	{		
		try {

			String fileName= "SOutput/test.txt" ;
			String line1,line2,line3,line4,line5,line6,line7,line8,line9,line10,line11,line12,line13;
			String newLineContent;
			int lineToBeEdited=0 ;
			int lineToBeEdited2=0;

			if (incd1.ID.equals("NCS_Incident"))
				fileName = "SOutput/Summery_NCS.txt";
			else if (incd1.ID.equals("GSS_Incident"))
				fileName = "SOutput/Summery_GSS.txt";
			else if (incd1.ID.equals("MyFrind_Incident"))
				fileName = "SOutput/Summery_GMarket.txt";

			//one Line  One RUN only
			switch ( InputFile.CurrentExp ) {			
			case Exp1 :
				lineToBeEdited =   0 ; 
				lineToBeEdited2= 25 ;
				break;
			case Exp2 :
				lineToBeEdited =  1 ; 
				lineToBeEdited2= 26 ;
				break;
			case Exp3 :
				lineToBeEdited =  2 ;
				lineToBeEdited2= 27 ;
				break;
			case Exp4 :
				lineToBeEdited = 3 ;
				lineToBeEdited2= 28 ;
				break;
			case Exp5 :
				lineToBeEdited =  4 ;
				lineToBeEdited2= 29 ;
				break;
			case Exp6 :
				lineToBeEdited =  5 ;
				lineToBeEdited2= 30 ;
				break;
			case Exp7 :
				lineToBeEdited =  6 ;
				lineToBeEdited2= 31 ;
				break;
			case Exp8 :
				lineToBeEdited =  7 ;
				lineToBeEdited2=32  ;
				break;
			case Exp9 :
				lineToBeEdited =  8 ;
				lineToBeEdited2=33  ;
				break;
			case Exp10 :
				lineToBeEdited =  9 ;
				lineToBeEdited2=34  ;
				break;
			case Exp11 :
				lineToBeEdited =  10 ;
				lineToBeEdited2=35  ;
				break;
			case Exp12 :
				lineToBeEdited =  11 ;
				lineToBeEdited2= 36 ;
				break;
			case Exp13 :
				lineToBeEdited =  12 ;
				lineToBeEdited2= 37 ;
				break;
			case Exp14 :
				lineToBeEdited =  13 ;
				lineToBeEdited2=38  ;
				break;
			case Exp15 :
				lineToBeEdited =  14 ;
				lineToBeEdited2= 39 ;
				break;
			case Exp16 :
				lineToBeEdited =  15 ;
				lineToBeEdited2= 40 ;
				break;
			case Exp17 :
				lineToBeEdited =  16 ;
				lineToBeEdited2= 41 ;
				break;
			case Exp18 :
				lineToBeEdited =  17 ;
				lineToBeEdited2=42  ;
				break;
			case Exp19 :
				lineToBeEdited =  18 ;
				lineToBeEdited2= 43;
				break;
			case Exp20 :
				lineToBeEdited =  19 ;
				lineToBeEdited2= 44 ;
				break;
			case Exp21 :
				lineToBeEdited =  20 ;
				lineToBeEdited2=45 ;
				break;
			case Exp22 :
				lineToBeEdited =  21 ;
				lineToBeEdited2= 46 ;
				break;
			case Exp23 :
				lineToBeEdited =  22 ;
				lineToBeEdited2= 47 ;
				break;
			case Exp24 :
				lineToBeEdited =  23 ;
				lineToBeEdited2= 48 ;
				break;

			}		

			try (Stream<String> lines = Files.lines(Paths.get(fileName))) {
				line1 = lines.skip(lineToBeEdited ).findFirst().get();
			}

			//			newLineContent =  line1+ "\t" + "Run"+ InputFile.RunNO  +"\t" + ResponseTime  +"\t" + Mrate +"\t" + TotalDead +"\t" +  this.FirstcasualtytransferedtoCCSorRC +"\t" + LastcasualtytransferedtoCCSorRC   +"\t" + EvacuationTime_ImpactArea   +"\t" +FirstcasualtytransferedtoH +"\t" + LastcasualtytransferedtoH +"\t" + AveragewaitingtimeinCCS + "\t" + MaxTimeAdmissionHospital_Red    +"\t" +  MaxTimeAdmissionHospital_allTriage  +"\t" +
			//					NofMesg +"\t" +  Com_Amb +"\t" +  Com_Fir  +"\t" + Com_Pol +"\t" + idle_Amb+"\t" +idle_Fir+"\t" + idle_Pol+"\t" + RU_Driver+"\t" +RU_paramdic+"\t" +   RU_FireFighter+"\t" +RU_Police+"\t" +  CE_Driver+"\t" +CE_paramdic+"\t" +  CE_FireFighter +"\t" +CE_Police; 	

			newLineContent =  line1 ; //+ "\t" + "Run"+ InputFile.RunNO ; 					
			newLineContent=newLineContent +"\t" + ResponseTime  +"\t" +  MaxTimeAdmissionHospital_allTriage +"\t" + Mrate + "\t" + TotalKnow+"\t"+  this.Totallog +"\t" + TotalDead  ;
			newLineContent=newLineContent +"\t" + this.MinTimeAdmissionHospital_Red    +"\t" +  MaxTimeAdmissionHospital_Red +"\t" +MinTimeAdmissionHospital_Red_beforeHos +"\t" + MaxTimeAdmissionHospital_Red_beforeHos ;
			newLineContent= newLineContent+"\t" + EvacuationTime_ImpactArea  +"\t" + FirstcasualtytransferedtoCCSorRC  +"\t" + LastcasualtytransferedtoCCSorRC ;
			
			ChangeLineInFile changeFile1 = new ChangeLineInFile();
			changeFile1.changeALineInATextFile(fileName, newLineContent, lineToBeEdited +1 );


		
					
			
			try (Stream<String> lines = Files.lines(Paths.get(fileName))) {
				line2 = lines.skip(lineToBeEdited2 ).findFirst().get();
			}

			newLineContent =  line2 ; //+ "\t" + "Run"+ InputFile.RunNO ; 		
			newLineContent= newLineContent+"\t" +  total_Wreckage_usedBefor +"\t" + total_Wreckage_usedAfter  +"\t" + total_Traffic_usedBefor +"\t" +  total_Traffic_usedAfter ;
			newLineContent= newLineContent+"\t" +  Percentage_of_use_beforecleaningW +"\t" + Percentage_of_use_aftercleaningW  +"\t" + Percentage_of_use_aftercleaningT +"\t" +   Percentage_of_use_beforecleaningT ;


			ChangeLineInFile changeFile2 = new ChangeLineInFile();
			changeFile2.changeALineInATextFile(fileName, newLineContent, lineToBeEdited2 +1 );


			System.out.println(incd1.ID +"  Successfully wrote to the file 1  : " +fileName ); 
			

		}
		catch (IOException e) {
			System.out.println("An error occurred......." + e.getMessage());
			e.printStackTrace();
		}

	}

	//********************************************************************************************************************************************************
	public void Responders_Report_Amb(Incident incd1 , boolean PrintFile ) 
	{

		int count=0,total=0;

		System.out.println(" _________________________Responders_Report___________________________ " );


		System.out.println(incd1.ID );

		//		try {
		//			//incd.AICcommander.LogFile.ReportaboutAction1();
		//			incd1.AICcommander.LogFile.ReportBeh2(false,null, false,null );
		//
		//			for (   Responder Res : incd1.AICcommander.CurrentCalssRole3.MyResponder_Ambulance) 				
		//			{
		//				//Res.LogFile.ReportaboutAction1();
		//				//Res.LogFile.ReportBeh2(false,null,false,null );
		//				//System.out.println(" ____________________________________________________ " );
		//			}
		//		}
		//		catch (IOException e) {
		//			System.out.println("An error occurred......." + e.getMessage());
		//			e.printStackTrace();
		//		}




		if (PrintFile)
		{
			try {
				//------------ way 1
				//PrintWriter d  = new PrintWriter("SOutput/"+incd.ID +"output.txt");
				//incd.AICcommander.LogFile.ReportBeh2(true,d,false,null);
				//for (   Responder Res : incd.AICcommander.CurrentCalssRole3.MyResponder_Ambulance) 				
				//Res.LogFile.ReportBeh2( true,  d,false,null );		
				//d.close();
				//System.out.println("Successfully wrote to the file."+incd.ID );    

				//--------------- end way 1

				//--------------- way 2
				String fileName1= "SOutput/test.txt";
				String fileName2= "SOutput/test.txt";

				if (incd1.ID.equals("NCS_Incident"))
				{ fileName1 = "SOutput/Summery_NCS_Details_Amb.txt";  fileName2="SOutput/Summery_NCS_TimelineActions.txt";  }
				else if (incd1.ID.equals("GSS_Incident"))
				{fileName1 = "SOutput/Summery_GSS_Details_Amb.txt";fileName2="SOutput/Summery__GSS_TimelineActions.txt";  }
				else if (incd1.ID.equals("MyFrind_Incident"))
				{	fileName1 = "SOutput/Summery_GMarket_Details_Amb.txt";fileName2="SOutput/Summery_GMarket_TimelineActions.txt";  }

				int counterDr=1;
				int counterPR=1;
				int counterSC=1;

				FileWriter fw1 = new FileWriter(fileName1,true); //the true will append the new data...fw.write("add a line\n");//appends the string to the file
				//	FileWriter fw2 = new FileWriter(fileName2,true); //the true will append the new data...fw.write("add a line\n");//appends the string to the file

				fw1.append("--------------------------------------Run " + InputFile.RunNO+ "  " +InputFile.CurrentExp +"\n" );
				//	fw2.append("--------------------------------------Run " + InputFile.RunNO+ "  " +InputFile.CurrentExp +"\n" );


				RU_Driver=0 ;RU_paramdic=0;RU_commander=0  ;
				CE_Driver=0 ;CE_paramdic=0 ;CE_commander=0  ;
				total_Driver=0; total_paramdic=0; total_commander=0;
				Com_Amb= 0 ; idle_Amb= 0; this.delay_Amb=0;
				delay_Amb_External=0 ; delay_Amb_internal=0  ;Com_Amb_External=0  ;Com_Amb_internal=0  ;

				//commander
				incd1.AICcommander.LogFile.counterUsed= 0;		
				//incd1.AICcommander.LogFile.LastcasualtytransferedfromSecen= Lastcasualtytransfered;
				//incd1.AICcommander.LogFile.LastcasualtyAdim=MaxTimeAdmissionHospital;
				incd1.AICcommander.LogFile.Rolle1=incd1.AICcommander.PrvRoleinprint1;	
				incd1.AICcommander.LogFile.leaveStation1=incd1.AICcommander.leaveStation ;
				incd1.AICcommander.LogFile.ArrivedScene1=incd1.AICcommander.ArrivedScene;
				incd1.AICcommander.LogFile.BacktoStation1=incd1.AICcommander.BacktoStation;
				incd1.AICcommander.LogFile.ReportBeh_Amb(false,null,true,fw1);
				total_commander++;
				RU_commander =RU_commander+incd1.AICcommander.LogFile.RU;
				CE_commander=CE_commander+incd1.AICcommander.LogFile.CE; 


				// AIC
				Com_Amb=  Com_Amb + incd1.AICcommander.LogFile.com  ;
				idle_Amb= idle_Amb  + incd1.AICcommander.LogFile.idle;
				delay_Amb=this.delay_Amb + incd1.AICcommander.LogFile.Com_delay ;

				delay_Amb_External= delay_Amb_External + incd1.AICcommander.ComunicationDelay_External ; 
				delay_Amb_internal= delay_Amb_internal + incd1.AICcommander.ComunicationDelay_internal  ;
				Com_Amb_External=Com_Amb_External + incd1.AICcommander.Comunication_External ;
				Com_Amb_internal= Com_Amb_internal + incd1.AICcommander.Comunication_internal  ;


				for (   Responder Res : incd1.AICcommander.CurrentCalssRole3.MyResponder_Ambulance) 				
				{
					int counter=0;
					if (  Res.PrvRoleinprint1 == Ambulance_ResponderRole.Driver)  counter=counterDr ++;
					if (  Res.PrvRoleinprint1 == Ambulance_ResponderRole.Paramedic)  counter=counterPR ++;	
					if (  Res.PrvRoleinprint1 == Ambulance_ResponderRole.AmbulanceSectorCommander)  counter=counterSC ++;	

					Res.LogFile.counterUsed= counter;	
					//Res.LogFile.LastcasualtytransferedfromSecen= Lastcasualtytransfered;
					//Res.LogFile.LastcasualtyAdim=MaxTimeAdmissionHospital;
					Res.LogFile.Rolle1=Res.PrvRoleinprint1;
					Res.LogFile.leaveStation1=Res.leaveStation ;
					Res.LogFile.ArrivedScene1=Res.ArrivedScene;
					Res.LogFile.BacktoStation1=Res.BacktoStation;
					Res.LogFile.ReportBeh_Amb( false, null,true,fw1  );	


					if (  Res.PrvRoleinprint1 == Ambulance_ResponderRole.Driver)  
					{
						total_Driver ++; 
						RU_Driver =RU_Driver+Res.LogFile.RU;
						CE_Driver=CE_Driver+Res.LogFile.CE; 
					}
					else if (  Res.PrvRoleinprint1 == Ambulance_ResponderRole.Paramedic)  
					{	
						total_paramdic++;
						RU_paramdic =RU_paramdic+Res.LogFile.RU;
						CE_paramdic=CE_paramdic+Res.LogFile.CE; 
					}
					else 
					{
						total_commander++; 
						RU_commander =RU_commander+Res.LogFile.RU;
						CE_commander=CE_commander+Res.LogFile.CE; 	   
					}

					// all
					Com_Amb=  Com_Amb + Res.LogFile.com  ;
					idle_Amb= idle_Amb  + Res.LogFile.idle;
					delay_Amb=this.delay_Amb + Res.LogFile.Com_delay ;

					delay_Amb_External= delay_Amb_External + Res.ComunicationDelay_External ; 
					delay_Amb_internal= delay_Amb_internal + Res.ComunicationDelay_internal  ;
					Com_Amb_External=Com_Amb_External + Res.Comunication_External ;
					Com_Amb_internal= Com_Amb_internal + Res.Comunication_internal  ;


					//--------------------
					//Res.LogFile.ReportAction( fw2  );	
					//Res.LogFile.ReportaboutAction3();


				}// end for





				fw1.append("\n" );
				fw1.close();

				//fw2.close();


				RU_Driver =RU_Driver/total_Driver  ;
				CE_Driver=CE_Driver/total_Driver ; 

				RU_paramdic =RU_paramdic/total_paramdic;
				CE_paramdic=CE_paramdic/total_paramdic; 


				//RU_commander =RU_commander/total_commander ;
				//CE_commander=CE_commander/total_commander ;


				System.out.println(incd1.ID +"  Successfully wrote to the file 2  : " +fileName1 ); 
				//	System.out.println(incd1.ID +"  Successfully wrote to the file 4  : " +fileName2 );

				//--------------- end way 2

			}
			catch (IOException e) {
				System.out.println("An error occurred......." + e.getMessage());
				e.printStackTrace();
			}
		}


	}  

	public void Responders_Report_fire(Incident incd1 , boolean PrintFile ) 
	{

		int count=0,total=0;

		System.out.println(" _________________________Responders_Report___________________________ " );


		System.out.println(incd1.ID );


		if (PrintFile)
		{
			try {

				//--------------- way 2
				String fileName1= "SOutput/test.txt";
				String fileName2= "SOutput/test.txt";

				if (incd1.ID.equals("NCS_Incident"))
				{ fileName1 = "SOutput/Summery_NCS_Details_fire.txt";  fileName2="SOutput/Summery_NCS_TimelineActions.txt";  }
				else if (incd1.ID.equals("GSS_Incident"))
				{fileName1 = "SOutput/Summery_GSS_Details_fire.txt";fileName2="SOutput/Summery__GSS_TimelineActions.txt";  }
				else if (incd1.ID.equals("MyFrind_Incident"))
				{	fileName1 = "SOutput/Summery_GMarket_Details_fire.txt";fileName2="SOutput/Summery_GMarket_TimelineActions.txt";  }

				int counterDr=1;
				int counterFR=1;
				int counterSC=1;

				FileWriter fw1 = new FileWriter(fileName1,true); //the true will append the new data...fw.write("add a line\n");//appends the string to the file
				//FileWriter fw2 = new FileWriter(fileName2,true); //the true will append the new data...fw.write("add a line\n");//appends the string to the file

				fw1.append("--------------------------------------Run " + InputFile.RunNO+ "  " +InputFile.CurrentExp +"\n" );
				//fw2.append("--------------------------------------Run " + InputFile.RunNO+ "  " +InputFile.CurrentExp +"\n" );


				RU_FireFighter=0;
				CE_FireFighter=0  ;
				total_FireFighter=0; 
				Com_Fir=0 ; idle_Fir= 0;  this.delay_Fir=0;
				delay_Fir_External=0 ; delay_Fir_internal=0 ;	Com_Fir_External=0 ;Com_Fir_internal=0 ;

				//FIC commander
				incd1.FICcommander.LogFile.counterUsed= 0;		
				//incd1.FICcommander.LogFile.LastcasualtytransferedfromSecen= Lastcasualtytransfered;
				//incd1.FICcommander.LogFile.LastcasualtyAdim=MaxTimeAdmissionHospital;
				incd1.FICcommander.LogFile.Rolle2=incd1.FICcommander.PrvRoleinprint2;
				incd1.FICcommander.LogFile.leaveStation1=incd1.FICcommander.leaveStation;
				incd1.FICcommander.LogFile.ArrivedScene1=incd1.FICcommander.ArrivedScene;
				incd1.FICcommander.LogFile.BacktoStation1=incd1.FICcommander.BacktoStation;
				incd1.FICcommander.LogFile.ReportBeh_Fire(false,null,true,fw1);

				total_commander++;
				RU_commander =RU_commander+incd1.FICcommander.LogFile.RU; //not used
				CE_commander=CE_commander+incd1.FICcommander.LogFile.CE;  //not used
				Com_Fir= 0 ; idle_Fir= 0; this.delay_Fir=0;

				// FIC
				Com_Fir=  Com_Fir + incd1.FICcommander.LogFile.com  ;
				idle_Fir= idle_Fir  + incd1.FICcommander.LogFile.idle;
				delay_Fir=this.delay_Fir + incd1.FICcommander.LogFile.Com_delay ;

				delay_Fir_External= delay_Fir_External + incd1.FICcommander.ComunicationDelay_External ; 
				delay_Fir_internal= delay_Fir_internal + incd1.FICcommander.ComunicationDelay_internal  ;
				Com_Fir_External=Com_Fir_External + incd1.FICcommander.Comunication_External ;
				Com_Fir_internal= Com_Fir_internal + incd1.FICcommander.Comunication_internal  ;


				for (   Responder Res : incd1.FICcommander.CurrentCalssRole3.MyResponder_Fire) 				
				{
					int counter=0;
					if (  Res.PrvRoleinprint2 == Fire_ResponderRole.Driver)  counter=counterDr ++;
					if (  Res.PrvRoleinprint2 == Fire_ResponderRole.FireFighter)  counter=counterFR ++;	
					if (  Res.PrvRoleinprint2 == Fire_ResponderRole.FireSectorCommander)  counter=counterSC ++;	

					Res.LogFile.counterUsed= counter;	
					//Res.LogFile.LastcasualtytransferedfromSecen= Lastcasualtytransfered;
					//Res.LogFile.LastcasualtyAdim=MaxTimeAdmissionHospital;
					Res.LogFile.Rolle2=Res.PrvRoleinprint2;
					Res.LogFile.leaveStation1=Res.leaveStation ;
					Res.LogFile.ArrivedScene1=Res.ArrivedScene;
					Res.LogFile.BacktoStation1=Res.BacktoStation;
					Res.LogFile.ReportBeh_Fire( false, null,true,fw1  );	

					if (  Res.PrvRoleinprint2 == Fire_ResponderRole.FireFighter)  
					{	
						total_FireFighter++;
						RU_FireFighter =RU_FireFighter+Res.LogFile.RU;
						CE_FireFighter=CE_FireFighter+Res.LogFile.CE; 
					}
					//all

					Com_Fir=  Com_Fir + Res.LogFile.com  ;
					idle_Fir= idle_Fir  + Res.LogFile.idle;
					delay_Fir=this.delay_Fir + Res.LogFile.Com_delay ;

					delay_Fir_External= delay_Fir_External + Res.ComunicationDelay_External ; 
					delay_Fir_internal= delay_Fir_internal + Res.ComunicationDelay_internal  ;
					Com_Fir_External=Com_Fir_External + Res.Comunication_External ;
					Com_Fir_internal= Com_Fir_internal + Res.Comunication_internal  ;


				}// end for

				fw1.append("\n" );
				fw1.close();
				//fw2.close();



				RU_FireFighter =RU_FireFighter/total_FireFighter;
				CE_FireFighter=CE_FireFighter/total_FireFighter; 



				System.out.println(incd1.ID +"  Successfully wrote to the file 2  : " +fileName1 ); 
				//System.out.println(incd1.ID +"  Successfully wrote to the file 4  : " +fileName2 );

				//--------------- end way 2

			}
			catch (IOException e) {
				System.out.println("An error occurred......." + e.getMessage());
				e.printStackTrace();
			}
		}


	}  

	public void Responders_Report_Police(Incident incd1 , boolean PrintFile ) 
	{

		int count=0,total=0;

		System.out.println(" _________________________Responders_Report___________________________ " );


		System.out.println(incd1.ID );


		if (PrintFile)
		{
			try {

				//--------------- way 2
				String fileName1= "SOutput/test.txt";
				String fileName2= "SOutput/test.txt";

				if (incd1.ID.equals("NCS_Incident"))
				{ fileName1 = "SOutput/Summery_NCS_Details_police.txt";  fileName2="SOutput/Summery_NCS_TimelineActions.txt";  }
				else if (incd1.ID.equals("GSS_Incident"))
				{fileName1 = "SOutput/Summery_GSS_Details_police.txt";fileName2="SOutput/Summery__GSS_TimelineActions.txt";  }
				else if (incd1.ID.equals("MyFrind_Incident"))
				{	fileName1 = "SOutput/Summery_GMarket_Details_police.txt";fileName2="SOutput/Summery_GMarket_TimelineActions.txt";  }

				int counterDr=1;
				int counterPoR=1;
				int counterSC=1;

				FileWriter fw1 = new FileWriter(fileName1,true); //the true will append the new data...fw.write("add a line\n");//appends the string to the file
				//FileWriter fw2 = new FileWriter(fileName2,true); //the true will append the new data...fw.write("add a line\n");//appends the string to the file

				fw1.append("--------------------------------------Run " + InputFile.RunNO+ "  " +InputFile.CurrentExp +"\n" );
				//fw2.append("--------------------------------------Run " + InputFile.RunNO+ "  " +InputFile.CurrentExp +"\n" );


				RU_Police=0; 
				CE_Police=0 ;
				total_Police=0;
				Com_Pol= 0 ; idle_Pol= 0;  this.delay_Pol=0;
				delay_Pol_External=0  ; delay_Pol_internal=0  ;Com_Pol_External=0  ;Com_Pol_internal=0  ;


				//commander
				incd1.PICcommander.LogFile.counterUsed= 0;		
				//incd1.PICcommander.LogFile.LastcasualtytransferedfromSecen= Lastcasualtytransfered;
				//incd1.PICcommander.LogFile.LastcasualtyAdim=MaxTimeAdmissionHospital;
				incd1.PICcommander.LogFile.Rolle1=incd1.PICcommander.PrvRoleinprint1;
				incd1.PICcommander.LogFile.leaveStation1=incd1.PICcommander.leaveStation ;
				incd1.PICcommander.LogFile.ArrivedScene1=incd1.PICcommander.ArrivedScene;
				incd1.PICcommander.LogFile.BacktoStation1=incd1.PICcommander.BacktoStation;
				incd1.PICcommander.LogFile.ReportBeh_Police(false,null,true,fw1);
				total_commander++;
				RU_commander =RU_commander+incd1.PICcommander.LogFile.RU;
				CE_commander=CE_commander+incd1.PICcommander.LogFile.CE; 

				//PIC
				Com_Pol=  Com_Pol + incd1.PICcommander.LogFile.com  ;
				idle_Pol= idle_Pol  + incd1.PICcommander.LogFile.idle;
				delay_Pol=this.delay_Pol + incd1.PICcommander.LogFile.Com_delay ;


				delay_Pol_External= delay_Pol_External + incd1.PICcommander.ComunicationDelay_External ; 
				delay_Pol_internal= delay_Pol_internal + incd1.PICcommander.ComunicationDelay_internal  ;
				Com_Pol_External=Com_Pol_External + incd1.PICcommander.Comunication_External ;
				Com_Pol_internal= Com_Pol_internal + incd1.PICcommander.Comunication_internal  ;


				for (   Responder Res : incd1.PICcommander.CurrentCalssRole3.MyResponder_Police) 				
				{
					int counter=0;
					if (  Res.PrvRoleinprint3 == Police_ResponderRole.Driver)  counter=counterDr ++;
					if (  Res.PrvRoleinprint3 == Police_ResponderRole.Policeman)  counter=counterPoR ++;	

					Res.LogFile.counterUsed= counter;	
					//Res.LogFile.LastcasualtytransferedfromSecen= Lastcasualtytransfered;
					//Res.LogFile.LastcasualtyAdim=MaxTimeAdmissionHospital;
					Res.LogFile.Rolle3=Res.PrvRoleinprint3;
					Res.LogFile.leaveStation1=Res.leaveStation ;
					Res.LogFile.ArrivedScene1=Res.ArrivedScene;
					Res.LogFile.BacktoStation1=Res.BacktoStation;
					Res.LogFile.ReportBeh_Police( false, null,true,fw1  );	



					if (  Res.PrvRoleinprint3 == Police_ResponderRole.Policeman)  
					{	
						total_Police++;
						RU_Police =RU_Police+Res.LogFile.RU;
						CE_Police=CE_Police+Res.LogFile.CE; 
					}

					//all
					Com_Pol=  Com_Pol + Res.LogFile.com  ;
					idle_Pol= idle_Pol  + Res.LogFile.idle;
					delay_Pol=this.delay_Pol + Res.LogFile.Com_delay ;

					delay_Pol_External= delay_Pol_External + Res.ComunicationDelay_External ; 
					delay_Pol_internal= delay_Pol_internal + Res.ComunicationDelay_internal  ;
					Com_Pol_External=Com_Pol_External + Res.Comunication_External ;
					Com_Pol_internal= Com_Pol_internal + Res.Comunication_internal  ;

				}// end for

				fw1.append("\n" );
				fw1.close();
				//fw2.close();

				RU_Police =RU_Police/total_Police;
				CE_Police=CE_Police/total_Police; 


				System.out.println(incd1.ID +"  Successfully wrote to the file 2  : " +fileName1 ); 
				//System.out.println(incd1.ID +"  Successfully wrote to the file 4  : " +fileName2 );

				//--------------- end way 2

			}
			catch (IOException e) {
				System.out.println("An error occurred......." + e.getMessage());
				e.printStackTrace();
			}
		}


	}  

	//********************************************************************************************************************************************************
	public void Casualty_Report(Incident incd1 , boolean PrintFile ) 
	{

		try {

			//--------------- way 2
			String fileName= "SOutput/test.txt";

			if (incd1.ID.equals("NCS_Incident"))
				fileName = "SOutput/Summery_NCS_Casualty.txt";
			else if (incd1.ID.equals("GSS_Incident"))
				fileName = "SOutput/Summery_GSS_Casualty.txt";
			else if (incd1.ID.equals("MyFrind_Incident"))
				fileName = "SOutput/Summery_GMarket_Casualty.txt";

			FileWriter fw = new FileWriter(fileName,true); //the true will append the new data...fw.write("add a line\n");//appends the string to the file
			fw.append("-------------------------------------Run "+ InputFile.RunNO+ "  " +InputFile.CurrentExp +"    " + TotalDead +"\n" );

			for (   Casualty ca : incd1.casualties) 							
				ca.LogFile.ReportaboutAction1(true,fw);


			//			fw.append("------------------SP--------------------Run "+ InputFile.RunNO+ "  "  +InputFile.CurrentExp  +"\n" );
			//			for (   Casualty ca : incd1.casualties) 				
			//			{ 				
			//				ca.LogFile.ReportaboutSurvivalProbability(true,fw);
			//			}

			fw.append("\n" );
			fw.close();				
			System.out.println(incd1.ID +"  Successfully wrote to the file 3  : " +fileName ); 
			//--------------- end way 2
		}

		catch (IOException e) {
			System.out.println("An error occurred......." + e.getMessage());
			e.printStackTrace();
		}


	} 

	//********************************************************************************************************************************************************
	public void ReportComInts(  Incident incd1  ) 
	{

		try {

			String fileName= "SOutput/test.txt" ;
			String line1,line2,line3,line4,line5 ,newLineContent ;
			int lineToBeEdited=0 ;
			int lineToBeEdited_2=0 ;
			int lineToBeEdited_3=0 ;
			int lineToBeEdited_4=0 ;
			int lineToBeEdited_5 =0;
			
			if (incd1.ID.equals("NCS_Incident"))
				fileName = "SOutput/Summery_NCS_ComInst.txt";
			else if (incd1.ID.equals("GSS_Incident"))
				fileName = "SOutput/Summery_GSS_ComInst.txt";
			else if (incd1.ID.equals("MyFrind_Incident"))
				fileName = "SOutput/com.txt";

			FileWriter fw = new FileWriter(fileName,true); //the true will append the new data...fw.write("add a line\n");//appends the string to the file


			switch ( InputFile.CurrentExp ) {			
			case Exp1 :
				lineToBeEdited =   0 ; 
				lineToBeEdited_2=25 ;
				lineToBeEdited_3=50 ;
				lineToBeEdited_4=75 ;
				lineToBeEdited_5=100 ;
				break;
			case Exp2 :
				lineToBeEdited =  1 ; 
				lineToBeEdited_2=26 ;
				lineToBeEdited_3=51 ;
				lineToBeEdited_4=76 ;
				lineToBeEdited_5=101 ;
				break;
			case Exp3 :
				lineToBeEdited =  2 ;
				lineToBeEdited_2=27 ;
				lineToBeEdited_3=52 ;
				lineToBeEdited_4=77 ;
				lineToBeEdited_5=102 ;
				break;
			case Exp4 :
				lineToBeEdited = 3 ;
				lineToBeEdited_2=28 ;
				lineToBeEdited_3=53 ;
				lineToBeEdited_4=78 ;
				lineToBeEdited_5=103 ;
				break;
			case Exp5 :
				lineToBeEdited =  4 ;
				lineToBeEdited_2=29 ;
				lineToBeEdited_3=54 ;
				lineToBeEdited_4=79 ;
				lineToBeEdited_5=104 ;
				break;
			case Exp6 :
				lineToBeEdited =  5 ;
				lineToBeEdited_2=30 ;
				lineToBeEdited_3=55 ;
				lineToBeEdited_4=80 ;
				lineToBeEdited_5=105 ;
				break;
			case Exp7 :
				lineToBeEdited =  6 ;
				lineToBeEdited_2=31 ;
				lineToBeEdited_3=56 ;
				lineToBeEdited_4=81 ;
				lineToBeEdited_5=106 ;
				break;
			case Exp8 :
				lineToBeEdited =  7 ;
				lineToBeEdited_2=32 ;
				lineToBeEdited_3=57 ;
				lineToBeEdited_4=82 ;
				lineToBeEdited_5=107 ;
				break;
			case Exp9 :
				lineToBeEdited =  8 ;
				lineToBeEdited_2=33 ;
				lineToBeEdited_3=58 ;
				lineToBeEdited_4=83 ;
				lineToBeEdited_5=108 ;
				break;
			case Exp10 :
				lineToBeEdited =  9 ;
				lineToBeEdited_2=34 ;
				lineToBeEdited_3=59 ;
				lineToBeEdited_4=84 ;
				lineToBeEdited_5=109 ;
				break;
			case Exp11 :
				lineToBeEdited =  10 ;
				lineToBeEdited_2=35 ;
				lineToBeEdited_3=60 ;
				lineToBeEdited_4=85 ;
				lineToBeEdited_5=110 ;
				break;
			case Exp12 :
				lineToBeEdited =  11 ;
				lineToBeEdited_2=36 ;
				lineToBeEdited_3=61 ;
				lineToBeEdited_4=86 ;
				lineToBeEdited_5=111 ;
				break;
			case Exp13 :
				lineToBeEdited =  12 ;
				lineToBeEdited_2=37 ;
				lineToBeEdited_3=62 ;
				lineToBeEdited_4=87 ;
				lineToBeEdited_5=112 ;
				break;
			case Exp14 :
				lineToBeEdited =  13 ;
				lineToBeEdited_2=38 ;
				lineToBeEdited_3=63 ;
				lineToBeEdited_4=88 ;
				lineToBeEdited_5=113 ;
				break;
			case Exp15 :
				lineToBeEdited =  14 ;
				lineToBeEdited_2=39 ;
				lineToBeEdited_3=64 ;
				lineToBeEdited_4=89 ;
				lineToBeEdited_5=114 ;
				break;
			case Exp16 :
				lineToBeEdited =  15 ;
				lineToBeEdited_2=40 ;
				lineToBeEdited_3=65 ;
				lineToBeEdited_4=90 ;
				lineToBeEdited_5=115 ;
				break;
			case Exp17 :
				lineToBeEdited =  16 ;
				lineToBeEdited_2=41 ;
				lineToBeEdited_3=66 ;
				lineToBeEdited_4=91 ;
				lineToBeEdited_5=116 ;
				break;
			case Exp18 :
				lineToBeEdited =  17 ;
				lineToBeEdited_2=42 ;
				lineToBeEdited_3=67 ;
				lineToBeEdited_4=92 ;
				lineToBeEdited_5=117 ;
				break;
			case Exp19 :
				lineToBeEdited =  18 ;
				lineToBeEdited_2=43 ;
				lineToBeEdited_3=68 ;
				lineToBeEdited_4=93 ;
				lineToBeEdited_5=118 ;
				break;
			case Exp20 :
				lineToBeEdited =  19 ;
				lineToBeEdited_2=44 ;
				lineToBeEdited_3=69 ;
				lineToBeEdited_4=94 ;
				lineToBeEdited_5=119;
				break;
			case Exp21 :
				lineToBeEdited =  20 ;
				lineToBeEdited_2=45 ;
				lineToBeEdited_3=70 ;
				lineToBeEdited_4=95 ;
				lineToBeEdited_5=120 ;
				break;
			case Exp22 :
				lineToBeEdited =  21 ;
				lineToBeEdited_2=46 ;
				lineToBeEdited_3=71 ;
				lineToBeEdited_4=96 ;
				lineToBeEdited_5=121 ;
				break;
			case Exp23 :
				lineToBeEdited =  22 ;
				lineToBeEdited_2=47 ;
				lineToBeEdited_3=72 ;
				lineToBeEdited_4=97 ;
				lineToBeEdited_5=122 ;
				break;
			case Exp24 :
				lineToBeEdited =  23 ;
				lineToBeEdited_2=48 ;
				lineToBeEdited_3=73 ;
				lineToBeEdited_4=98 ;
				lineToBeEdited_5=123 ;
				break;
			}

			// 1------------ mesg only
		//	try (Stream<String> lines = Files.lines(Paths.get(fileName))) {
		//		line1 = lines.skip(lineToBeEdited ).findFirst().get();
		//	}

		//	newLineContent =  line1; //+ "\t"; // + "Run"+InputFile.RunNO   ;

			//Content
			int total_comulative1=0;
			int total_Detail=0;
			int total_external=0;
			int total_MoredetailExternal=0;
			
			for ( Duration_info  D :incd1.ComInts  )
			{			

				total_comulative1=total_comulative1 +  D.countMesg ;
				total_Detail=total_Detail+  D.countMesg_Moredetails;

				total_external=total_external+ D.countMesg_External;
				total_MoredetailExternal=total_MoredetailExternal+ D.countMesg_MoredetailExternal;


				//if (  total_comulative1!=0 )
				//	newLineContent =  newLineContent   +"\t" +  total_comulative1  ;	
			//	else
				//	newLineContent =  newLineContent   +"\t"  ;	

			//	if ( total_comulative1==NofMesg ) break;
			}

		//	ChangeLineInFile changeFile1 = new ChangeLineInFile();
		//	changeFile1.changeALineInATextFile(fileName, newLineContent, lineToBeEdited +1 );

			// 2------------
			try (Stream<String> lines = Files.lines(Paths.get(fileName))) {
				line2 = lines.skip(lineToBeEdited_2 ).findFirst().get();
			}

			newLineContent =  line2 ;  //+ "\t" + "Run"+ InputFile.RunNO ;
			newLineContent =  newLineContent   +"\t" + NofMesg +"\t" + Com_Amb +"\t" +  Com_Fir  +"\t" + Com_Pol +"\t" + delay_Amb+"\t" +delay_Fir+"\t" + delay_Pol ;


			newLineContent =  newLineContent   +"\t" + (Com_Amb_internal +  Com_Amb_External ) +"\t" + (Com_Fir_internal + Com_Fir_External) +"\t" + (Com_Pol_internal+Com_Pol_External) ;
			newLineContent =  newLineContent   +"\t" + ( delay_Amb_internal  + delay_Amb_External ) +"\t" + (delay_Fir_internal + delay_Fir_External) +"\t" + (delay_Pol_internal +delay_Pol_External) ;

			newLineContent =  newLineContent   +"\t" + Com_Amb_internal +"\t" +  Com_Amb_External +"\t" + Com_Fir_internal +"\t" + Com_Fir_External +"\t" + Com_Pol_internal +"\t" + Com_Pol_External ;
			newLineContent =  newLineContent   +"\t" + delay_Amb_internal +"\t" + delay_Amb_External  +"\t" + delay_Fir_internal +"\t" + delay_Fir_External +"\t" + delay_Pol_internal +"\t" +delay_Pol_External ;
			newLineContent =  newLineContent   +"\t" + total_comulative1 +"\t" + total_Detail  +"\t" +total_external +"\t" + total_MoredetailExternal ;


			ChangeLineInFile changeFile2 = new ChangeLineInFile();
			changeFile2.changeALineInATextFile(fileName, newLineContent, lineToBeEdited_2 +1 );


			//==================================================================================================================================


			// 3------------  all
			try (Stream<String> lines = Files.lines(Paths.get(fileName))) {
				line3 = lines.skip(lineToBeEdited_3 ).findFirst().get();
			}

			newLineContent =  line3; //+ "\t"; // + "Run"+InputFile.RunNO   ;

			//Content
			int total_comulative3=0;
			for ( Duration_info  D :incd1.ComInts  )
			{			

				total_comulative3=total_comulative3 +  D.countMesg_Moredetails;

				if (  total_comulative3!=0 )
					newLineContent =  newLineContent   +"\t" +  total_comulative3  ;	
				else
					newLineContent =  newLineContent   +"\t" + "" ;	

				if ( total_comulative3==total_Detail ) break;
			}

			ChangeLineInFile changeFile3 = new ChangeLineInFile();
			changeFile3.changeALineInATextFile(fileName, newLineContent, lineToBeEdited_3 +1 );


			// 4------------ only external
//			try (Stream<String> lines = Files.lines(Paths.get(fileName))) {
//				line4 = lines.skip(lineToBeEdited_4 ).findFirst().get();
//			}
//
//			newLineContent =  line4; //+ "\t"; // + "Run"+InputFile.RunNO   ;
//
//			//Content
//			int total_comulative4=0;
//			for ( Duration_info  D :incd1.ComInts  )
//			{			
//
//				total_comulative4=total_comulative4 +  D.countMesg_External;
//
//				if (  total_comulative4!=0 )
//					newLineContent =  newLineContent   +"\t" +  total_comulative4  ;	
//				else
//					newLineContent =  newLineContent   +"\t" + "" ;	
//
//				if ( total_comulative4==total_external ) break;
//			}
//
//			ChangeLineInFile changeFile4 = new ChangeLineInFile();
//			changeFile4.changeALineInATextFile(fileName, newLineContent, lineToBeEdited_4 +1 );


			// 5------------ only external details
			try (Stream<String> lines = Files.lines(Paths.get(fileName))) {
				line5 = lines.skip(lineToBeEdited_5 ).findFirst().get();
			}

			newLineContent =  line5; //+ "\t"; // + "Run"+InputFile.RunNO   ;

			//Content
			int total_comulative5=0;
			for ( Duration_info  D :incd1.ComInts  )
			{			

				total_comulative5=total_comulative5 +  D.countMesg_MoredetailExternal;

				if (  total_comulative5!=0 )
					newLineContent =  newLineContent   +"\t" +  total_comulative5  ;	
				else
					newLineContent =  newLineContent   +"\t" + "" ;	

				if ( total_comulative5==total_MoredetailExternal ) break;
			}

			ChangeLineInFile changeFile5 = new ChangeLineInFile();
			changeFile5.changeALineInATextFile(fileName, newLineContent, lineToBeEdited_5 +1 );

			System.out.println(incd1.ID +"  Successfully wrote to the file 1  : " +fileName ); 

		}
		catch (IOException e) {
			System.out.println("An error occurred......." + e.getMessage());
			e.printStackTrace();
		}









		// way 2
		//			try {
		//
		//				//--------------- 
		//				String fileName= "SOutput/test.txt";
		//
		//				if (incd1.ID.equals("NCS_Incident"))
		//					fileName = "SOutput/Summery_NCS_ComInst.txt";
		//				else if (incd1.ID.equals("GSS_Incident"))
		//					fileName = "SOutput/Summery_GSS_ComInst.txt";
		//				else if (incd1.ID.equals("MyFrind_Incident"))
		//					fileName = "SOutput/Summery_GMarket_ComInst.txt";
		//
		//				FileWriter fw = new FileWriter(fileName,true); //the true will append the new data...fw.write("add a line\n");//appends the string to the file
		//
		//				//Header
		//				for ( Duration_info  D :incd1.ComInts  )
		//				{			
		//					fw.append(  D.Range_S +"\t" );				
		//				}
		//
		//				fw.append("\n" );
		//				
		//				//Content
		//				for ( Duration_info  D :incd1.ComInts  )
		//				{			
		//					fw.append(  D.count +"\t" );				
		//				}
		//
		//				fw.append("\n" );
		//
		//				fw.close();				
		//				System.out.println(incd1.ID +"  Successfully wrote to the file   : " +fileName ); 
		//
		//				//--------------- 
		//
		//			}
		//			catch (IOException e) {
		//				System.out.println("An error occurred......." + e.getMessage());
		//				e.printStackTrace();
		//			}

		//			newLineContent =  newLineContent  + "\t" + "Run"+InputFile.RunNO   ;
		//			//Content
		//			for ( Duration_info  D :incd1.ComInts  )
		//			{			
		//				newLineContent =  newLineContent   +"\t" + D.Rep   ;				
		//			}

	}

	public void ReportCasualty_V_time(  Incident incd1  ) 
	{

		try {

			String fileName= "SOutput/test.txt" ;
			String line1,line2;
			String newLineContent;
			int lineToBeEdited=0 ;
			int lineToBeEdited2=0 ;

			if (incd1.ID.equals("NCS_Incident"))
				fileName = "SOutput/Summery_NCS_xx.txt";
			else if (incd1.ID.equals("GSS_Incident"))
				fileName = "SOutput/Summery_GSS_xx.txt";
			else if (incd1.ID.equals("MyFrind_Incident"))
				fileName = "SOutput/Summery_GMarket_Casualty_v_time.txt";

			FileWriter fw = new FileWriter(fileName,true); //the true will append the new data...fw.write("add a line\n");//appends the string to the file


			switch ( InputFile.CurrentExp ) {			
			case Exp1 :
				lineToBeEdited =   0 ; 
				lineToBeEdited2=25 ;
				break;
			case Exp2 :
				lineToBeEdited =  1 ; 
				lineToBeEdited2= 26;
				break;
			case Exp3 :
				lineToBeEdited =  2 ;
				lineToBeEdited2=27 ;
				break;
			case Exp4 :
				lineToBeEdited = 3 ;
				lineToBeEdited2=28 ;
				break;
			case Exp5 :
				lineToBeEdited =  4 ;
				lineToBeEdited2=29 ;
				break;
			case Exp6 :
				lineToBeEdited =  5 ;
				lineToBeEdited2=30 ;
				break;
			case Exp7 :
				lineToBeEdited =  6 ;
				lineToBeEdited2=31 ;
				break;
			case Exp8 :
				lineToBeEdited =  7 ;
				lineToBeEdited2=32 ;
				break;
			case Exp9 :
				lineToBeEdited =  8 ;
				lineToBeEdited2=33 ;
				break;
			case Exp10 :
				lineToBeEdited =  9 ;
				lineToBeEdited2=34 ;
				break;
			case Exp11 :
				lineToBeEdited =  10 ;
				lineToBeEdited2=35 ;
				break;
			case Exp12 :
				lineToBeEdited =  11 ;
				lineToBeEdited2=36 ;
				break;
			case Exp13 :
				lineToBeEdited =  12 ;
				lineToBeEdited2=37 ;
				break;
			case Exp14 :
				lineToBeEdited =  13 ;
				lineToBeEdited2= 38;
				break;
			case Exp15 :
				lineToBeEdited =  14 ;
				lineToBeEdited2=39 ;
				break;
			case Exp16 :
				lineToBeEdited =  15 ;
				lineToBeEdited2=40 ;
				break;
			case Exp17 :
				lineToBeEdited =  16 ;
				lineToBeEdited2=41 ;
				break;
			case Exp18 :
				lineToBeEdited =  17 ;
				lineToBeEdited2=42 ;
				break;
			case Exp19 :
				lineToBeEdited =  18 ;
				lineToBeEdited2=43 ;
				break;
			case Exp20 :
				lineToBeEdited =  19 ;
				lineToBeEdited2=44 ;
				break;
			case Exp21 :
				lineToBeEdited =  20 ;
				lineToBeEdited2=45 ;
				break;
			case Exp22 :
				lineToBeEdited =  21 ;
				lineToBeEdited2=46 ;
				break;
			case Exp23 :
				lineToBeEdited =  22 ;
				lineToBeEdited2= 47;
				break;
			case Exp24 :
				lineToBeEdited =  23 ;
				lineToBeEdited2=48 ;
				break;
			}		


			int total_red=0;
			int total_redbefore=0;
			//Content
			for (   Casualty ca : incd1.casualties) 				
			{ 				

				if ( ca.Triage_tage==1 )
				{	
					for ( Duration_info  D :incd1.ComInts  )
						if (  ca.TimeAdmissioninHospital >= D.Range_S &&  ca.TimeAdmissioninHospital <= D.RangeE  )
						{ 
							D.countca++ ;
							System.out.println( ca.ID + " _______________________1_____________________________ "+ D.MinNum );
							break;
						}

					total_red++;

				}

				if ( ca.Triage_tageBeforHosp==1 )
				{	
					for ( Duration_info  D :incd1.ComInts  )
						if (  ca.TimeAdmissioninHospital >= D.Range_S &&  ca.TimeAdmissioninHospital <= D.RangeE  )
						{ 
							D.countcared_BeforHosp++ ;
							System.out.println( ca.ID + " _________________________2___________________________ "+ D.MinNum );
							break;
						}

					total_redbefore++;

				}
			}

//			//..............1
//			try (Stream<String> lines = Files.lines(Paths.get(fileName))) {
//				line1 = lines.skip(lineToBeEdited ).findFirst().get();
//			}
//
//
//			newLineContent =  line1; //+ "\t"; // + "Run"+InputFile.RunNO   ;
//			int total_comulative=0;
//
//			// print
//			for ( Duration_info  D :incd1.ComInts  )
//			{			
//				total_comulative=total_comulative +  D.countca ;
//
//				if (  total_comulative==0    )
//
//					newLineContent =  newLineContent   +"\t" + "" ;
//				else
//					newLineContent =  newLineContent   +"\t" +  total_comulative  ;	
//
//
//				if ( total_comulative==total_red ) {    break;}
//
//			}
//
//			ChangeLineInFile changeFile = new ChangeLineInFile();
//			changeFile.changeALineInATextFile(fileName, newLineContent, lineToBeEdited +1 );


			//..............2
			try (Stream<String> lines = Files.lines(Paths.get(fileName))) {
				line2 = lines.skip(lineToBeEdited2 ).findFirst().get();
			}
			newLineContent =  line2; //+ "\t"; // + "Run"+InputFile.RunNO   ;
			int total_comulative2=0;

			// print
			for ( Duration_info  D :incd1.ComInts  )
			{			
				total_comulative2=total_comulative2 +  D.countcared_BeforHosp ;

				if (  total_comulative2==0    )

					newLineContent =  newLineContent   +"\t" + "" ;
				else
					newLineContent =  newLineContent   +"\t" +  total_comulative2  ;	


				if ( total_comulative2==total_redbefore ) {    break;}

			}

			ChangeLineInFile changeFile2 = new ChangeLineInFile();
			changeFile2.changeALineInATextFile(fileName, newLineContent, lineToBeEdited2 +1 );



			System.out.println(incd1.ID +"  Successfully wrote to the file 1  : " +fileName ); 

		}	
		catch (IOException e) {
			System.out.println("An error occurred......." + e.getMessage());
			e.printStackTrace();
		}

	}

	public void ReportCasualtydead_V_time(  Incident incd1  ) 
	{

		try {

			String fileName= "SOutput/test.txt" ;
			String line1,line2,line3,line4,line5,line6,line7,line8,line9,line10,line11,line12,line13;
			String newLineContent;
			int lineToBeEdited=0 ;
			int lineToBeEdited2=0;

			if (incd1.ID.equals("NCS_Incident"))
				fileName = "SOutput/Summery_NCS_xx.txt";
			else if (incd1.ID.equals("GSS_Incident"))
				fileName = "SOutput/Summery_GSS_xx.txt";
			else if (incd1.ID.equals("MyFrind_Incident"))
				fileName = "SOutput/Summery_GMarket_Casualtydead_v_time.txt";

			FileWriter fw = new FileWriter(fileName,true); //the true will append the new data...fw.write("add a line\n");//appends the string to the file


			switch ( InputFile.CurrentExp ) {			
			case Exp1 :
				lineToBeEdited =   0 ; 
				lineToBeEdited2=25;
				break;
			case Exp2 :
				lineToBeEdited =  1 ; 
				lineToBeEdited2=26 ;
				break;
			case Exp3 :
				lineToBeEdited =  2 ;
				lineToBeEdited2= 27;
				break;
			case Exp4 :
				lineToBeEdited = 3 ;
				lineToBeEdited2=28 ;
				break;
			case Exp5 :
				lineToBeEdited =  4 ;
				lineToBeEdited2=29 ;
				break;
			case Exp6 :
				lineToBeEdited =  5 ;
				lineToBeEdited2=30 ;
				break;
			case Exp7 :
				lineToBeEdited =  6 ;
				lineToBeEdited2= 31;
				break;
			case Exp8 :
				lineToBeEdited =  7 ;
				lineToBeEdited2=32 ;
				break;
			case Exp9 :
				lineToBeEdited =  8 ;
				lineToBeEdited2=33 ;
				break;
			case Exp10 :
				lineToBeEdited =  9 ;
				lineToBeEdited2=34 ;
				break;
			case Exp11 :
				lineToBeEdited =  10 ;
				lineToBeEdited2=35 ;
				break;
			case Exp12 :
				lineToBeEdited =  11 ;
				lineToBeEdited2=36 ;
				break;
			case Exp13 :
				lineToBeEdited =  12 ;
				lineToBeEdited2= 37;
				break;
			case Exp14 :
				lineToBeEdited =  13 ;
				lineToBeEdited2= 38;
				break;
			case Exp15 :
				lineToBeEdited =  14 ;
				lineToBeEdited2=39 ;
				break;
			case Exp16 :
				lineToBeEdited =  15 ;
				lineToBeEdited2=40 ;
				break;
			case Exp17 :
				lineToBeEdited =  16 ;
				lineToBeEdited2=41 ;
				break;
			case Exp18 :
				lineToBeEdited =  17 ;
				lineToBeEdited2=42 ;
				break;
			case Exp19 :
				lineToBeEdited =  18 ;
				lineToBeEdited2= 43;
				break;
			case Exp20 :
				lineToBeEdited =  19 ;
				lineToBeEdited2=44 ;
				break;
			case Exp21 :
				lineToBeEdited =  20 ;
				lineToBeEdited2=45 ;
				break;
			case Exp22 :
				lineToBeEdited =  21 ;
				lineToBeEdited2=46 ;
				break;
			case Exp23 :
				lineToBeEdited =  22 ;
				lineToBeEdited2= 47;
				break;
			case Exp24 :
				lineToBeEdited =  23 ;
				lineToBeEdited2= 48;
				break;
			}		


			int total_dead=0;
			//Content
			for (   Casualty ca : incd1.casualties) 				
			{ 				

				if (ca.Life== CasualtyLife.Dead) 
				{	
					for ( Duration_info  D :incd1.ComInts  )
						if (  ca.deadTime >= D.Range_S &&  ca.deadTime <= D.RangeE  )
						{ 
							D.countdeadca++ ;
							System.out.println( ca.ID + " ___________________________dead_________________________ "+ D.MinNum );
							break; }
					total_dead++;

				}
			}


			//			try (Stream<String> lines = Files.lines(Paths.get(fileName))) {
			//				line1 = lines.skip(lineToBeEdited ).findFirst().get();
			//			}
			//
			//						newLineContent =  line1; //+ "\t" ; //+ "Run"+InputFile.RunNO   ;
			//						int total_comulative=0;
			//			
			//						// print
			//						for ( Duration_info  D :incd1.ComInts  )
			//						{			
			//							total_comulative=total_comulative +  D.countdeadca ;
			//			
			//							if (  total_comulative!=0    )
			//								newLineContent =  newLineContent   +"\t" +  total_comulative  ;	
			//							else
			//								newLineContent =  newLineContent   +"\t" + "\t" ;
			//			
			//							if ( total_comulative==total_dead ) break;
			//			
			//						}



			try (Stream<String> lines = Files.lines(Paths.get(fileName))) {
				line1 = lines.skip(lineToBeEdited ).findFirst().get();
			}

			newLineContent =  line1; //+ "\t" ; //+ "Run"+InputFile.RunNO   ;
			int total_comulative=incd1.causaltiesCount +incd1.causaltiesCountUN ;
			int totalcas=incd1.causaltiesCount +incd1.causaltiesCountUN ;
			// print
			for ( Duration_info  D :incd1.ComInts  )
			{			
				total_comulative=total_comulative -  D.countdeadca ;
				newLineContent =  newLineContent   +"\t" +  total_comulative  ;	
			}


			ChangeLineInFile changeFile1 = new ChangeLineInFile();
			changeFile1.changeALineInATextFile(fileName, newLineContent, lineToBeEdited +1 );


			
			
			
			
			try (Stream<String> lines = Files.lines(Paths.get(fileName))) {
				line2 = lines.skip(lineToBeEdited2 ).findFirst().get();
			}


			newLineContent =  line2 ; //+ "\t" + "Run"+ InputFile.RunNO ; 					
			newLineContent=newLineContent +"\t" + TotalDead +"\t" + NumberDeadinScene   +"\t" +  NumberDeadwaitingPre+"\t" +NumberDeadinCCS +"\t" + NumberDeadEvacuation +"\t"+  NumberDeadinOther ;

			ChangeLineInFile changeFile2 = new ChangeLineInFile();
			changeFile2.changeALineInATextFile(fileName, newLineContent, lineToBeEdited2 +1 );



			System.out.println(incd1.ID +"  Successfully wrote to the file 1  : " +fileName ); 

		}	
		catch (IOException e) {
			System.out.println("An error occurred......." + e.getMessage());
			e.printStackTrace();
		}

	}

	//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
	//                                       Environment
	//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
	public void Senario1_1H1SP1SA1F()
	{

		RoadNode Node;
		Hospital Hospital1  ;
		Station_Ambulance Station_Ambulance1 ;
		Station_Fire Station_Fire1 ;
		Station_Police Station_Police1 ;

		//-------------------------------------------------------------
		Node=BuildStaticFuction.GetRoadNodeofthisLocation(geography,"osgb4000000007997008");
		if (   Node!=null )
		{
			Hospital1 = new Hospital ( context , geography ,"TestH1",Node,"osgb4000000007997008", 100);
			Hospital_list.add(Hospital1); 
			System.out.println("Hospital ______________________________________ "+ Hospital1.ID );
		}

		//-------------------------------------------------------------
		Node=BuildStaticFuction.GetRoadNodeofthisLocation(geography,"osgb4000000007594320");
		if (   Node!=null )
		{
			Hospital1 = new Hospital ( context , geography ,"TestH2",Node,"osgb4000000007594320", 100);
			Hospital_list.add(Hospital1); 
			System.out.println("Hospital ______________________________________ "+ Hospital1.ID );
		}
		//-------------------------------------------------------------
		Node=BuildStaticFuction.GetRoadNodeofthisLocation(geography,"osgb4000000008044945");
		if (   Node!=null )
		{
			Hospital1 = new Hospital ( context , geography ,"TestH3",Node,"osgb4000000008044945", 100);
			Hospital_list.add(Hospital1); 
			System.out.println("Hospital ______________________________________ "+ Hospital1.ID );
		}

		//-------------------------------------------------------------

		Node=BuildStaticFuction.GetRoadNodeofthisLocation(geography,"osgb4000000007594160");
		if (   Node!=null )
		{
			Station_Ambulance1 = new Station_Ambulance ( "Am" , context , geography , Node,"osgb4000000007594160",100,300);
			Station_Ambulance1.EOC1 =this ;
			Station_Ambulance_list.add(Station_Ambulance1);
			System.out.println("Station_Ambulance ______________________________________ "+ Station_Ambulance1.ID);
		}
		//-------------------------------------------------------------
		Node=BuildStaticFuction.GetRoadNodeofthisLocation(geography,"osgb4000000007594320");
		if (   Node!=null )
		{
			Station_Fire1 = new Station_Fire ( "Fi" , context , geography , Node,"osgb4000000007594320",100,300);
			Station_Fire1.EOC1 =this ;
			Station_Fire_list.add(Station_Fire1);
			System.out.println("Station_Fire ______________________________________ "+ Station_Fire1.ID);
		}
		//-------------------------------------------------------------
		Node=BuildStaticFuction.GetRoadNodeofthisLocation(geography,"osgb4000000008044945");
		if (   Node!=null )
		{
			Station_Police1 = new Station_Police ( "Po" , context , geography , Node,"osgb4000000008044945",100,300);
			Station_Police1.EOC1 =this ;
			Station_Police_list.add(Station_Police1);
			System.out.println("Station_Police ______________________________________ "+ Station_Police1.ID);
		}
	}

	//**********************************************************************************************************************
	public void  EnvironmentofSenario2_MS()
	{
		RoadNode Node;
		Hospital Hospital1  ;
		Station_Ambulance Station_Ambulance1 ;  ///osgb5000005126598465   osgb4000000008009535
		Station_Fire Station_Fire1 ;
		Station_Police Station_Police1 ;

		//-------------------------------------------------------------
		System.out.println(" set up Environment ______________________________________ " );

		Node=BuildStaticFuction.GetRoadNodeofthisLocation(geography,"osgb4000000007594290");
		if (   Node!=null )
		{
			Hospital1 = new Hospital ( context , geography ,"H_RH",Node,"osgb4000000007594290", 100);  //osgb4000000007600548
			Hospital_list.add(Hospital1); 
			System.out.println("Hospital ______________________________________ "+ Hospital1.ID );
		}

		Node=BuildStaticFuction.GetRoadNodeofthisLocation(geography,"osgb4000000008016210"); //osgb5000005116842148
		if (  Node!=null )
		{
			Hospital1 = new Hospital ( context , geography ,"H_WH", Node,"osgb4000000008016210", 100);
			Hospital_list.add(Hospital1);
			System.out.println("Hospital ______________________________________ "+ Hospital1.ID );
		}


		//		Node=BuildStaticFuction.GetRoadNodeofthisLocation(geography,"osgb4000000007999440");
		//		if (   Node!=null)
		//		{
		//			Hospital1 = new Hospital ( context , geography ,"H_FH", Node,"osgb4000000007999440", 100);
		//			Hospital_list.add(Hospital1);
		//			System.out.println("Hospital ______________________________________ "+ Hospital1.ID );
		//		}

		//		Node=BuildStaticFuction.GetRoadNodeofthisLocation(geography,"osgb5000005166214944");
		//		if (  Node!=null )
		//		{
		//			Hospital1 = new Hospital ( context , geography ,"H_QH", Node,"osgb5000005166214944", 100);
		//			Hospital_list.add(Hospital1);
		//			System.out.println("Hospital ______________________________________ "+ Hospital1.ID );
		//		}
		//
		//		Node=BuildStaticFuction.GetRoadNodeofthisLocation(geography,"osgb5000005207562117");
		//
		//		if ( Node!=null )
		//		{
		//			Hospital1 = new Hospital ( context , geography ,"H_DH",Node,"osgb5000005207562117", 100);
		//			Hospital_list.add(Hospital1);
		//			System.out.println("Hospital ______________________________________ "+ Hospital1.ID );
		//		}
		//
		//
		//		Node=BuildStaticFuction.GetRoadNodeofthisLocation(geography,"osgb4000000007606551");
		//		if (   Node!=null )
		//		{
		//			Hospital1 = new Hospital ( context , geography ,"H_PH", Node,"osgb4000000007606551", 100);
		//			Hospital_list.add(Hospital1);
		//			System.out.println("Hospital ______________________________________ "+ Hospital1.ID );
		//		}

		Node=BuildStaticFuction.GetRoadNodeofthisLocation(geography,"osgb4000000007596154");
		if (   Node!=null )
		{
			Hospital1 = new Hospital ( context , geography ,"H_GH", Node,"osgb4000000007596154", 100);
			Hospital_list.add(Hospital1);
			System.out.println("Hospital ___________________g Hospital___________________ "+ Hospital1.ID );
		}






		//-------------------------------------------------------------



		//		Node=BuildStaticFuction.GetRoadNodeofthisLocation(geography,"osgb5000005205157696");
		//		if (   Node!=null )
		//		{
		//			Station_Ambulance1 = new Station_Ambulance ( "Amb_te" , context , geography , Node,"osgb5000005205157696",5,20);
		//			Station_Ambulance1.EOC1 =this ;
		//			Station_Ambulance_list.add(Station_Ambulance1);
		//			System.out.println("Station_Ambulance __________________te___________________ "+ Station_Ambulance1.ID);
		//		}

		Node=BuildStaticFuction.GetRoadNodeofthisLocation(geography,"osgb4000000007594671");
		if (   Node!=null )
		{
			Station_Ambulance1 = new Station_Ambulance ( "Amb_GS" , context , geography , Node,"osgb4000000007594671",5,20);
			Station_Ambulance1.EOC1 =this ;
			Station_Ambulance_list.add(Station_Ambulance1);
			System.out.println("Station_Ambulance ___________________GS___________________ "+ Station_Ambulance1.ID);
		}



		Node=BuildStaticFuction.GetRoadNodeofthisLocation(geography,"osgb5000005205281022");
		if (   Node!=null )
		{
			Station_Ambulance1 = new Station_Ambulance ( "Amb_SRS" , context , geography , Node,"osgb5000005205281022",5,20);
			Station_Ambulance1.EOC1 =this ;
			Station_Ambulance_list.add(Station_Ambulance1);
			System.out.println("Station_Ambulance ______________________________________ "+ Station_Ambulance1.ID);
		}

		Node=BuildStaticFuction.GetRoadNodeofthisLocation(geography,"osgb4000000007604915");
		if (  Node!=null )
		{
			Station_Ambulance1 = new Station_Ambulance ( "Amb_DGS" , context , geography ,Node, "osgb4000000007604915",5,20);
			Station_Ambulance1.EOC1 =this ;
			Station_Ambulance_list.add(Station_Ambulance1);
			System.out.println("Station_Ambulance ______________________________________ "+ Station_Ambulance1.ID);
		}


		//		Node=BuildStaticFuction.GetRoadNodeofthisLocation(geography,"osgb5000005207121742");
		//		if (   Node!=null )
		//		{
		//			Station_Ambulance1 = new Station_Ambulance ( "Amb_NES" , context , geography , Node,"osgb5000005207121742",5,20);
		//			Station_Ambulance1.EOC1 =this ;
		//			Station_Ambulance_list.add(Station_Ambulance1);
		//			System.out.println("Station_Ambulance ______________________________________ "+ Station_Ambulance1.ID);
		//		}

		Node=BuildStaticFuction.GetRoadNodeofthisLocation(geography,"osgb4000000007588714");
		if (   Node!=null )
		{
			Station_Ambulance1 = new Station_Ambulance ( "Amb_GS" , context , geography ,Node, "osgb4000000007588714",5,20);
			Station_Ambulance1.EOC1 =this ;
			Station_Ambulance_list.add(Station_Ambulance1);
			System.out.println("Station_Ambulance ______________________________________ "+ Station_Ambulance1.ID);
		}

		Node=BuildStaticFuction.GetRoadNodeofthisLocation(geography,"osgb4000000007597701");
		if (   Node!=null )
		{
			Station_Ambulance1 = new Station_Ambulance ( "MS" , context , geography , Node,"osgb4000000007597701",5,20);
			Station_Ambulance1.EOC1 =this ;
			Station_Ambulance_list.add(Station_Ambulance1);
			System.out.println("Station_Ambulance ______________________________________ "+ Station_Ambulance1.ID);
		}

		Node=BuildStaticFuction.GetRoadNodeofthisLocation(geography,"osgb4000000007582576") ;
		if (  Node!=null )
		{
			Station_Ambulance1 = new Station_Ambulance ( "Amb_MLS" , context , geography ,Node, "osgb4000000007582576",5,20);
			Station_Ambulance1.EOC1 =this ;
			Station_Ambulance_list.add(Station_Ambulance1);
			System.out.println("Station_Ambulance ______________________________________ "+ Station_Ambulance1.ID);
		}
		//-------------------------------------------------------------

		Node=BuildStaticFuction.GetRoadNodeofthisLocation(geography,"osgb5000005238685489");
		if (   Node!=null )
		{
			Station_Fire1 = new Station_Fire ( "Fr_MLF" , context , geography , Node,"osgb5000005238685489",5,25);
			Station_Fire1.EOC1 =this ;
			Station_Fire_list.add(Station_Fire1);
			System.out.println("Station_Fire ______________________________________ "+ Station_Fire1.ID);
		}

		Node=BuildStaticFuction.GetRoadNodeofthisLocation(geography,"osgb4000000007591120");
		if (   Node!=null )
		{
			Station_Fire1 = new Station_Fire ( "Fr_ENC" , context , geography , Node,"osgb4000000007591120",5,25);
			Station_Fire1.EOC1 =this ;
			Station_Fire_list.add(Station_Fire1);
			System.out.println("Station_Fire ______________________________________ "+ Station_Fire1.ID);
		}

		Node=BuildStaticFuction.GetRoadNodeofthisLocation(geography,"osgb4000000008001081");
		if (   Node!=null )
		{
			Station_Fire1 = new Station_Fire ( "Fr_DR" , context , geography , Node,"osgb4000000008001081",5,25);
			Station_Fire1.EOC1 =this ;
			Station_Fire_list.add(Station_Fire1);
			System.out.println("Station_Fire ______________________________________ "+ Station_Fire1.ID);
		}

		Node=BuildStaticFuction.GetRoadNodeofthisLocation(geography,"osgb1000000061417175");
		if (   Node!=null )
		{
			Station_Fire1 = new Station_Fire ( "Fr_BR" , context , geography , Node,"osgb1000000061417175",5,25);
			Station_Fire1.EOC1 =this ;
			Station_Fire_list.add(Station_Fire1);
			System.out.println("Station_Fire ______________________________________ "+ Station_Fire1.ID);
		}

		Node=BuildStaticFuction.GetRoadNodeofthisLocation(geography,"osgb4000000007592850");
		if (   Node!=null )
		{
			Station_Fire1 = new Station_Fire ( "Fr_UR" , context , geography , Node,"osgb4000000007592850",5,25);
			Station_Fire1.EOC1 =this ;
			Station_Fire_list.add(Station_Fire1);
			System.out.println("Station_Fire ______________________________________ "+ Station_Fire1.ID);
		}

		//-------------------------------------------------------------

		Node=BuildStaticFuction.GetRoadNodeofthisLocation(geography,"osgb4000000007593782");
		if (   Node!=null )
		{
			Station_Police1 = new Station_Police ( "Po_HW" , context , geography , Node,"osgb4000000007593782",5,20);
			Station_Police1.EOC1 =this ;
			Station_Police_list.add(Station_Police1);
			System.out.println("Station_Police ______________________________________ "+ Station_Police1.ID);
		}

		Node=BuildStaticFuction.GetRoadNodeofthisLocation(geography,"osgb4000000007593008");
		if (   Node!=null )
		{
			Station_Police1 = new Station_Police ( "Po_FB" , context , geography , Node,"osgb4000000007593008",5,20);
			Station_Police1.EOC1 =this ;
			Station_Police_list.add(Station_Police1);
			System.out.println("Station_Police ______________________________________ "+ Station_Police1.ID);
		}



	}

	//**********************************************************************************************************************
	public void  EnvironmentofSenario2_acurate()
	{

		RoadNode Node;
		Hospital Hospital1  ;
		Station_Ambulance Station_Ambulance1 ;  
		Station_Fire Station_Fire1 ;
		Station_Police Station_Police1 ;

		System.out.println("Environment ______________________________________ ");
		Node=BuildStaticFuction.GetRoadNodeofthisLocation(geography,"osgb5000005126598465");
		if (   Node!=null )
		{
			Hospital1 = new Hospital ( context , geography ,"RH",Node,"osgb5000005126598465", 100);
			Hospital_list.add(Hospital1);


			System.out.println("Hospital ______________________________________ "+ Hospital1.ID );
		}

		Node=BuildStaticFuction.GetRoadNodeofthisLocation(geography,"osgb5000005116842148");
		if (  Node!=null )
		{
			Hospital1 = new Hospital ( context , geography ,"WH", Node,"osgb5000005116842148", 100);
			Hospital_list.add(Hospital1);
			System.out.println("Hospital ______________________________________ "+ Hospital1.ID );
		}

		Node=BuildStaticFuction.GetRoadNodeofthisLocation(geography,"osgb4000000007999440");
		if (   Node!=null)
		{
			Hospital1 = new Hospital ( context , geography ,"FH", Node,"osgb4000000007999440", 100);
			Hospital_list.add(Hospital1);
			System.out.println("Hospital ______________________________________ "+ Hospital1.ID );
		}

		Node=BuildStaticFuction.GetRoadNodeofthisLocation(geography,"osgb4000000007595457");
		if (  Node!=null )
		{
			Hospital1 = new Hospital ( context , geography ,"QH", Node,"osgb4000000007595457"  , 100);  //"osgb5000005166214944"
			Hospital_list.add(Hospital1);
			System.out.println("Hospital ______________________________________ "+ Hospital1.ID );
		}

		Node=BuildStaticFuction.GetRoadNodeofthisLocation(geography,"osgb5000005166971790");

		if ( Node!=null )
		{

			Hospital1 = new Hospital ( context , geography ,"DH",Node,"osgb5000005166971790", 100);
			Hospital_list.add(Hospital1);
			System.out.println("Hospital ______________________________________ "+ Hospital1.ID );
		}

		Node=BuildStaticFuction.GetRoadNodeofthisLocation(geography,"osgb4000000007606551");
		if (   Node!=null )
		{
			Hospital1 = new Hospital ( context , geography ,"PH", Node,"osgb4000000007606551", 100);
			Hospital_list.add(Hospital1);
			System.out.println("Hospital ______________________________________ "+ Hospital1.ID );
		}


		//-------------------------------------------------------------
		Node=BuildStaticFuction.GetRoadNodeofthisLocation(geography,"osgb5000005238685489");
		if (   Node!=null )
		{
			Station_Fire1 = new Station_Fire ( "Fr_MLF" , context , geography , Node,"osgb5000005238685489",2,10);
			Station_Fire1.EOC1 =this ;
			Station_Fire_list.add(Station_Fire1);
			System.out.println("Station_Fire ______________________________________ "+ Station_Fire1.ID);
		}

		Node=BuildStaticFuction.GetRoadNodeofthisLocation(geography,"osgb4000000007591120");  
		if (   Node!=null )
		{
			Station_Fire1 = new Station_Fire ( "Fr_ENC" , context , geography , Node,"osgb4000000007591120",2,10);
			Station_Fire1.EOC1 =this ;
			Station_Fire_list.add(Station_Fire1);
			System.out.println("Station_Fire ______________________________________ "+ Station_Fire1.ID);
		}

		Node=BuildStaticFuction.GetRoadNodeofthisLocation(geography,"osgb4000000008001081");
		if (   Node!=null )
		{
			Station_Fire1 = new Station_Fire ( "Fr_DR" , context , geography , Node,"osgb4000000008001081",2,10);
			Station_Fire1.EOC1 =this ;
			Station_Fire_list.add(Station_Fire1);
			System.out.println("Station_Fire ______________________________________ "+ Station_Fire1.ID);
		}

		Node=BuildStaticFuction.GetRoadNodeofthisLocation(geography,"osgb1000000061417175");
		if (   Node!=null )
		{
			Station_Fire1 = new Station_Fire ( "Fr_BR" , context , geography , Node,"osgb1000000061417175",2,10);
			Station_Fire1.EOC1 =this ;
			Station_Fire_list.add(Station_Fire1);
			System.out.println("Station_Fire ______________________________________ "+ Station_Fire1.ID);
		}

		Node=BuildStaticFuction.GetRoadNodeofthisLocation(geography,"osgb4000000007592850");
		if (   Node!=null )
		{
			Station_Fire1 = new Station_Fire ( "Fr_UR" , context , geography , Node,"osgb4000000007592850",2,10);
			Station_Fire1.EOC1 =this ;
			Station_Fire_list.add(Station_Fire1);
			System.out.println("Station_Fire ______________________________________ "+ Station_Fire1.ID);
		}

		//-------------------------------------------------------------

		Node=BuildStaticFuction.GetRoadNodeofthisLocation(geography,"osgb5000005205281022");
		if (   Node!=null )
		{
			Station_Ambulance1 = new Station_Ambulance ( "Amb_SRS" , context , geography , Node,"osgb5000005205281022",3,15);
			Station_Ambulance1.EOC1 =this ;
			Station_Ambulance_list.add(Station_Ambulance1);
			System.out.println("Station_Ambulance ______________________________________ "+ Station_Ambulance1.ID);
		}

		Node=BuildStaticFuction.GetRoadNodeofthisLocation(geography,"osgb4000000007604915");
		if (  Node!=null )
		{
			Station_Ambulance1 = new Station_Ambulance ( "Amb_DGS" , context , geography ,Node, "osgb4000000007604915",3,15);
			Station_Ambulance1.EOC1 =this ;
			Station_Ambulance_list.add(Station_Ambulance1);
			System.out.println("Station_Ambulance ______________________________________ "+ Station_Ambulance1.ID);
		}


		Node=BuildStaticFuction.GetRoadNodeofthisLocation(geography,"osgb5000005207121742");
		if (   Node!=null )
		{
			Station_Ambulance1 = new Station_Ambulance ( "Amb_NES" , context , geography , Node,"osgb5000005207121742",2,10);
			Station_Ambulance1.EOC1 =this ;
			Station_Ambulance_list.add(Station_Ambulance1);
			System.out.println("Station_Ambulance ______________________________________ "+ Station_Ambulance1.ID);
		}

		Node=BuildStaticFuction.GetRoadNodeofthisLocation(geography,"osgb4000000007595457");
		if (   Node!=null )
		{
			Station_Ambulance1 = new Station_Ambulance ( "Amb_GS" , context , geography ,Node, "osgb4000000007595457" ,2,10);      //"osgb4000000007588714"
			Station_Ambulance1.EOC1 =this ;
			Station_Ambulance_list.add(Station_Ambulance1);
			System.out.println("Station_Ambulance ______________________________________ "+ Station_Ambulance1.ID);
		}

		Node=BuildStaticFuction.GetRoadNodeofthisLocation(geography,"osgb4000000007597701");
		if (   Node!=null )
		{
			Station_Ambulance1 = new Station_Ambulance ( "Amb_MS" , context , geography , Node,"osgb4000000007597701",2,10);
			Station_Ambulance1.EOC1 =this ;
			Station_Ambulance_list.add(Station_Ambulance1);
			System.out.println("Station_Ambulance ______________________________________ "+ Station_Ambulance1.ID);
		}

		Node=BuildStaticFuction.GetRoadNodeofthisLocation(geography,"osgb4000000007582576") ;
		if (  Node!=null )
		{
			Station_Ambulance1 = new Station_Ambulance ( "Amb_MLS" , context , geography ,Node, "osgb4000000007582576",2,10);
			Station_Ambulance1.EOC1 =this ;
			Station_Ambulance_list.add(Station_Ambulance1);
			System.out.println("Station_Ambulance ______________________________________ "+ Station_Ambulance1.ID);
		}











		//-------------------------------------------------------------

		Node=BuildStaticFuction.GetRoadNodeofthisLocation(geography,"osgb4000000007593782");
		if (   Node!=null )
		{
			Station_Police1 = new Station_Police ( "Po_HW" , context , geography , Node,"osgb4000000007593782",3,15);
			Station_Police1.EOC1 =this ;
			Station_Police_list.add(Station_Police1);
			System.out.println("Station_Police ______________________________________ "+ Station_Police1.ID);
		}

		Node=BuildStaticFuction.GetRoadNodeofthisLocation(geography,"osgb4000000007593008");
		if (   Node!=null )
		{
			Station_Police1 = new Station_Police ( "Po_FB" , context , geography , Node,"osgb4000000007593008",3,15);
			Station_Police1.EOC1 =this ;
			Station_Police_list.add(Station_Police1);
			System.out.println("Station_Police ______________________________________ "+ Station_Police1.ID);
		}



		for (Station_Ambulance  A :Station_Ambulance_list)
		{
			System.out.println("PRIORITY_execution =>  "+ InputFile.PRIORITY_execution +"   "  + A.ID );
			ScheduleParameters params = ScheduleParameters.createRepeating(schedule.getTickCount() + 1 , 1,InputFile.PRIORITY_execution ++ );
			schedule.schedule(params,A,"step");
		}

		for (Station_Fire  F :Station_Fire_list)
		{
			System.out.println("PRIORITY_execution =>  "+ InputFile.PRIORITY_execution +"   "  + F.ID );
			ScheduleParameters params = ScheduleParameters.createRepeating(schedule.getTickCount() + 1 , 1,InputFile.PRIORITY_execution ++ );
			schedule.schedule(params,F,"step");
		}

		for (Station_Police  P :Station_Police_list)
		{
			System.out.println("PRIORITY_execution =>  "+ InputFile.PRIORITY_execution +"   "  + P.ID );
			ScheduleParameters params = ScheduleParameters.createRepeating(schedule.getTickCount() + 1 , 1,InputFile.PRIORITY_execution ++ );
			schedule.schedule(params,P,"step");
		}
















	}

	//**********************************************************************************************************************



}
