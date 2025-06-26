package B_Communication;

import java.util.ArrayList;

import A_Agents.Casualty;
import A_Environment.Incident;
import A_Environment.RoadLink;
import D_Ontology.Ontology.Ambulance_TaskType;
import D_Ontology.Ontology.ReportSummery;

public class Report {

	// Ambulance
	Ambulance_TaskType ReportType;

	public ReportSummery  RS = ReportSummery.None;  //{None, ConfirmedorDeclared ,AdditionalResourceRequest}
	public Incident fromIncident ;
	//public int  Totalneedamb ;

	public int TotalCasualties;
	public int TotalCasualties_waiting;
	public int TotalCasualties_inprogress ;
	public int TotalCasualties_done ;

	public int TotalCasualtiesRed_nottransfer ; //used by EOC
	public int TotalCasualtiesRed_transfer ; //used by AIC

	//used by PIC
	public ArrayList<Casualty> Casualties_Uninjured=new ArrayList<Casualty>();
	public ArrayList<Casualty> Casualties_Injured=new ArrayList<Casualty>();
	public ArrayList<Casualty> Casualties_Evacuated=new ArrayList<Casualty>();
	public ArrayList<Casualty> Casualties_Deceased=new ArrayList<Casualty>();

	int TotalResponders=0 ,RespondersFree=0 ,RepondersBusy=0;

	public ArrayList<Casualty_info> cas_triage_List_CCS=new ArrayList<Casualty_info>();
	public ArrayList<Casualty> Dead_List_inner =new ArrayList<Casualty>();
	public ArrayList<Casualty> trapped_List =new ArrayList<Casualty>();
	public ArrayList<Casualty> Dead_List_CCS =new ArrayList<Casualty>();
	public ArrayList<Casualty> Cas_evacuated_List =new ArrayList<Casualty>();

	public ArrayList<RoadLink> routeNEEDClearwreckage_List =new ArrayList<RoadLink>();
	public ArrayList<RoadLink> routeNEEDCleartraffic_List =new ArrayList<RoadLink>();

	public ArrayList<RoadLink> routeClearedwreckage_List =new ArrayList<RoadLink>();
	public ArrayList<RoadLink> routeClearedtraffic_List =new ArrayList<RoadLink>();

	//==============================================================================================================================
	//not used
	public void Ambulance_ReportCasualties_Bronze(int  _TotalCasualties_done ,Ambulance_TaskType _ReportType )
	{
		ReportType=_ReportType;
		TotalCasualties_done= _TotalCasualties_done ;		
	}

	public void Ambulance_OPReport_CCS(ArrayList<Casualty_info> _cas_triage_List_CCS , ArrayList<Casualty> _Dead_List_CCS ,ArrayList<Casualty> _Cas_evacuated_List   ) //from CCO to AIC
	{	
		if (  _cas_triage_List_CCS !=null)
			for(  Casualty_info  Ca: _cas_triage_List_CCS)
				cas_triage_List_CCS.add(Ca);

		if (  _Dead_List_CCS !=null)
			for(  Casualty  Ca: _Dead_List_CCS)
				Dead_List_CCS.add(Ca);

		if (  _Cas_evacuated_List !=null)
			for(  Casualty  Ca: _Cas_evacuated_List)
				Cas_evacuated_List.add(Ca);	
	}

	public void Ambulance_OPReport_loading(ArrayList<RoadLink> _routeNEEDClearwreckage_List   ,ArrayList<RoadLink> _routeNEEDCleartraffic_List ,ArrayList<Casualty> _Cas_evacuated_List   )// from ALC to AIC
	{	
		if (_Cas_evacuated_List !=null)
			for(  Casualty  Ca: _Cas_evacuated_List)
				Cas_evacuated_List.add(Ca);

		if ( _routeNEEDClearwreckage_List !=null )
			for(   RoadLink RL: _routeNEEDClearwreckage_List)
				routeNEEDClearwreckage_List.add(RL);

		if (_routeNEEDCleartraffic_List !=null )
			for(    RoadLink RL: _routeNEEDCleartraffic_List)
				routeNEEDCleartraffic_List.add(RL);	  		
	}

	public void Ambulance_ReportRoutes(ArrayList<RoadLink> _routeNEEDClearwreckage_List   ,ArrayList<RoadLink> _routeNEEDCleartraffic_List  )  // from driver to ALC 
	{
		if ( _routeNEEDClearwreckage_List !=null )
		for(   RoadLink RL: _routeNEEDClearwreckage_List)
			routeNEEDClearwreckage_List.add(RL);

		
		if (_routeNEEDCleartraffic_List !=null )
		for(    RoadLink RL: _routeNEEDCleartraffic_List)
			routeNEEDCleartraffic_List.add(RL);			
	}

	//-------------------------------------------------------------------------------------
	//  To EOC
	//-------------------------------------------------------------------------------------
	public void Ambulance_FRSituationAssessment( Incident _fromIncident ,ReportSummery  _RS , int _TotalREDCasualties   )
	{
		TotalCasualtiesRed_nottransfer =_TotalREDCasualties ;
		fromIncident=_fromIncident ;
		RS = _RS ;

	}
	public void Ambulance_URSituationAssessment( Incident _fromIncident ,ReportSummery  _RS , int _TotalCasualtiesRed_nottransfer  )
	{
		TotalCasualtiesRed_nottransfer  =_TotalCasualtiesRed_nottransfer; 
		fromIncident=_fromIncident ;
		RS=_RS;
	}

	public void Ambulance_EndReport( Incident _fromIncident , ReportSummery _RS   )
	{	
		fromIncident=_fromIncident ;
		RS=_RS  ;  //ReportSummery.EndER_Ambulance ;
	}

	//	public void Ambulance_AddtionalResouserRequest( Incident _fromIncident ,ReportSummery  _RS , int _Totalneedamb   )
	//	{
	//		Totalneedamb =_Totalneedamb ;
	//		fromIncident=_fromIncident ;
	//		RS=ReportSummery.AdditionalResourceRequest ;
	//	}

	//==============================================================================================================================
	public void Fire_OPReport_SC( ArrayList<Casualty> _Dead_List_inner , ArrayList<Casualty> _trapped_List   ) //from SC to FIC
	{	

		if (_Dead_List_inner !=null  )
			for(  Casualty  Ca: _Dead_List_inner)
				Dead_List_inner.add(Ca);

		if (_trapped_List  !=null  )
			for(  Casualty  Ca: _trapped_List )
				trapped_List.add(Ca);		
	}

	public void Fire_ReportRoutes(ArrayList<RoadLink> _routeClearedwreckage_List    )  // from FSC to FLC 
	{

		for(   RoadLink RL: _routeClearedwreckage_List)
			routeClearedwreckage_List.add(RL);		
	}

	//-------------------------------------------------------------------------------------
	//  To EOC  Fire
	//-------------------------------------------------------------------------------------
	public void Fire_FRSituationAssessment( Incident _fromIncident ,ReportSummery  _RS , int _TotalREDCasualties   )
	{

		TotalCasualtiesRed_nottransfer =_TotalREDCasualties ;
		fromIncident=_fromIncident ;
		RS = _RS ;
	}
	public void Fire_URSituationAssessment( Incident _fromIncident ,ReportSummery  _RS , int _TotalCasualtiesRed_nottransfer  )
	{
		TotalCasualtiesRed_nottransfer  =_TotalCasualtiesRed_nottransfer; 
		fromIncident=_fromIncident ;
		RS=_RS;
	}
	public void Fire_EndReport( Incident _fromIncident  ,ReportSummery _RS   )
	{	
		fromIncident=_fromIncident ;
		RS=_RS  ;  //ReportSummery.EndER_Fire ;
	}

	//==============================================================================================================================

	public void Police_ReportCasualties_informatiom(ArrayList<Casualty> _Csualties_Uninjured , ArrayList<Casualty> _Casualties_Injured , ArrayList<Casualty> _Casualties_Evacuated ,ArrayList<Casualty> _Casualties_Deceased )//from RCO to PIC
	{

		for(  Casualty  Ca: _Csualties_Uninjured)
			Casualties_Uninjured.add(Ca);
		for(  Casualty  Ca: _Casualties_Injured)
			Casualties_Injured.add(Ca);
		for(  Casualty  Ca: _Casualties_Evacuated)
			Casualties_Evacuated.add(Ca);
		for(  Casualty  Ca: _Casualties_Deceased)
			Casualties_Deceased.add(Ca);
	}

	public void Police_ReportRoutes( ArrayList<RoadLink> _routeClearedtraffic_List  )  // from driver to ALC 
	{

		for(    RoadLink RL: _routeClearedtraffic_List)
			routeClearedtraffic_List.add(RL);			
	}

	public void Police_caReport_LO( ArrayList<Casualty> _Casualties_Injured  ) //fromLO to PIC
	{	

		for(  Casualty  Ca: _Casualties_Injured)
			Casualties_Injured.add(Ca);

	}
	//-------------------------------------------------------------------------------------
	//  To EOC  Police
	//-------------------------------------------------------------------------------------
	public void Police_FRSituationAssessment( Incident _fromIncident ,ReportSummery  _RS  , int nothing   )
	{
		//nothing
		fromIncident=_fromIncident ;
		RS = _RS ;
	}

	public void Police_URSituationAssessment( Incident _fromIncident ,ReportSummery  _RS   )
	{
		////nothing
		fromIncident=_fromIncident ;
		RS=_RS;
	}

	public void Police_EndReport( Incident _fromIncident ,ReportSummery _RS   )
	{	
		fromIncident=_fromIncident ;
		RS=_RS  ; // ReportSummery.EndER_Police ;
	}

	//==============================================================================================================================

	//	//===================================================================================================================================
	//	public void Ambulance_ReportCasualties_Bronze(int _TotalResponders, int _RespondersFree ,int _RepondersBusy,
	//			int _TotalCasualties ,int _TotalCasualties_waiting ,int _TotalCasualties_inprogress ,int  _TotalCasualties_done ,Ambulance_TaskType _ReportType )
	//	{
	//
	//		TotalResponders=_TotalResponders;
	//		RespondersFree=_RespondersFree;
	//		RepondersBusy=_RepondersBusy;
	//
	//
	//		TotalCasualties =_TotalCasualties ;
	//		TotalCasualties_waiting= _TotalCasualties_waiting;
	//		TotalCasualties_inprogress = _TotalCasualties_inprogress;
	//		TotalCasualties_done= _TotalCasualties_done ;	
	//
	//		ReportType=_ReportType;
	//	}
	//
	//
	//
	//	//-------------------------------------------------------------------------------------
	//	public void Ambulance_Report_Sliver(int _currenttasks_total , int _currenttasks_waiting , int _currenttasks_inprogress ,  int _currenttasks_done  )
	//	{
	//
	//	}



}
