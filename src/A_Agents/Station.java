
package A_Agents;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import A_Environment.Incident;
import A_Environment.RoadNode;
import D_Ontology.Ontology.StationAction;
import D_Ontology.Ontology.StationTriggers;
import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.space.gis.Geography;


public class Station  {

	double  CurrentTick ,  EndofCurrentAction ;	
	ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule(); 
	
	public int ColorCode =0; //for visualization purpose	
	//-----------------------------------------------
	public EOC EOC1;
	StationAction Action  ;
	StationTriggers ActionEffectTrigger , CommTrigger, InterpretedTrigger ,EndActionTrigger ;
	//-----------------------------------------------

	public String ID ;
	public int AvailableVehiclesCount ;
	public int AvailableRespondersCount ;
	int EOC_allocatedRnum , EOC_allocatedVnum ;	
	public boolean Full=true;
	boolean EOC_double_crewed; 
	Incident newassignedIncident; //last one
	// --------------------------------------------------------------
	
	 Coordinate Location;
	public String Nodename_Location;
	public  RoadNode Node;
	public Context<Object> context;
	public Geography<Object> geography;

	//##############################################################################################################################################################

	public Station(String _ID, Context<Object> _context, Geography<Object> _geography ,RoadNode _Node ,String _Nodename_Location, int _availableVehiclesCount,int _availableRespondersCoun ) {

		context=_context;
		geography=_geography;   

		ID=_ID;				
		Nodename_Location=_Nodename_Location;
		Node= _Node;
		Node.Name=ID;
		Location= Node.getCurrentPosition();
		AvailableRespondersCount = _availableRespondersCoun ;
		AvailableVehiclesCount = _availableVehiclesCount ;
		Action=StationAction.Idle;

		//------------------------------------------------------

		GeometryFactory fac = new GeometryFactory();
		context.add(this);		
		geography.move(this, fac.createPoint(Location));

	}

	//*****************************************************************************************************	
	public void CommandSationtoResponse( Incident assignedIncident ,  int _EOC_allocatedVnum ,int _EOC_allocatedRnum , boolean _double_crewed )
	{		

		CommTrigger=StationTriggers.GetDispatchingcommand;		
		newassignedIncident=assignedIncident;

		EOC_allocatedRnum = _EOC_allocatedRnum ;
		EOC_allocatedVnum= _EOC_allocatedVnum;
		EOC_double_crewed = _double_crewed ;

		
//		if ( assignedIncident.RoadNode_ParkingofAmbulanceVehicle==null)	
//		{
//
//			assignedIncident.IdentifyAmbulanceParkingArea( assignedIncident.Node.getCurrentPosition() );
//			assignedIncident.IdentifyloadingArea( ) ; //temp
//			assignedIncident.IdentifyControlArea( assignedIncident.Node.getCurrentPosition()) ; //temp
//		} // new

	}
	
	public int getColorCode() {
		return this.ColorCode;
	}

	public void setColorCode(int ColorCode1) {
		this.ColorCode = ColorCode1;
	}


}


