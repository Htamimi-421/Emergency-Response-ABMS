
package Other;

import java.util.ArrayList;
import java.util.List;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;

import A_Agents.Casualty;
import A_Agents.Hospital;
import A_Agents.Responder;
import A_Agents.Station_Ambulance;
import A_Agents.Vehicle;
import A_Environment.Incident;
import B_Classes.IncidentPlan;
import B_Classes.Task_ambulance;
import B_Communication.ACL_Message;
import D_Ontology.Ontology;
import D_Ontology.Ontology.ACLPerformative;
import D_Ontology.Ontology.Ambulance_TaskType;
import D_Ontology.Ontology.GeneralTaskStatus;
import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.gis.Geography;
import repast.simphony.space.graph.Network;
import repast.simphony.space.graph.RepastEdge;


public class Commander_Gold_Group  {
	
//	protected int counter = 0; //used to give the plan unique number
//	
//	int Ambulance_Station_total = 0 ,  FireEngine_Station_total =0 ,  Police_Station_total=0 ,  Hospital_total=0 ; // number of stations in city
//	int currentIncident_total ,	currentIncident_inprogress = 0 , currentIncident_wating = 0 , currentIncident_done = 0;
//	
//	GeneralTaskStatus Status; // if done close the simulation
//
//	
//	List<IncidentPlan> Strategic_Plan = new ArrayList<IncidentPlan>();  // give information about the incident 
//	
//	List<Station_Ambulance> Ambulance_Resource  = new ArrayList<Station_Ambulance>();  
//	List<Station_FireEngine> FireEngine_Resource = new ArrayList<Station_FireEngine>();
//	List<Station_Police> Police_Resource = new ArrayList<Station_Police>();	
//	List<Hospital> Hospital_Resource = new ArrayList<Hospital>();
//	
//	ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule();
//	
//	// --------------------------------------------------------------
//		public Context<Object> context;
//		public Geography<Object> geography;
//			
//	//**************************************************************************************************************** 
//	
//	public Commander_Gold_Group (Context<Object> _context, Geography<Object> _geography, Coordinate initialLocation ) {
//		
//		context = _context;
//		geography = _geography;
//		context.add(this);
//		GeometryFactory fac = new GeometryFactory();
//		geography.move(this, fac.createPoint(initialLocation));
//		          
//      //-------------------------------------------
//		Status = Ontology.GeneralTaskStatus.Waiting;
//	
//	}
//
//	// --------------------------------------------------------------------------
//	// Create the plan(list of tasks) for first time
//	public void Implmenting_plane(Incident Current_Incident, Ambulance_TaskType _TaskType) {
//
//		for (Casualty casul : Current_Incident.casualties)
//			this.addTask_plane(casul, _TaskType);
//
//	};
//
//	public void addTask_plane(Casualty casul, Ambulance_TaskType _TaskType) {
//		IncidentPlan  NewIncidentPlan  = new IncidentPlan ("Incid_" + ++counter, casul, _TaskType);
//		Strategic_Plan.add(Newtask);
//	};
//
//
//	// --------------------------------------------------------------------------
//	// commanding all time and thinking
//	@ScheduledMethod(start = 1, interval = 1) //it should be programming
//	public void Managing_incdent() {
//
//		for (IncidentPlan Incplan : Strategic_Plan )
//			
//				
//				// send command
//				// send message
//
//			}
//	}
//	
//	// --------------------------------------------------------------------------
//	// Reporting
//	@ScheduledMethod(start = 1, interval = 900) // every 15 mints
//	public void Reporting_about_plan() {
//
//		currentIncident_total = Strategic_Plan.size();		
//		currentIncident_inprogress = 0;
//		currentIncident_wating = 0;
//		currentIncident_done = 0;
//
//
//		for (IncidentPlan  Incident : Strategic_Plan) 
//			
//
//
//		}
//		// send inform
//		// send message to silver commander
//	};	
//	
//	
//	// --------------------------------------------------------------------------
//	// this is called  by other responder to
//	@ Override
//	public void RecivedMessage(ACL_Message msg) {
//		  
//		// System.out.println(this.Id   + "   " + msg.content  + "   " + msg.sender_ID +"     "+ msg.time);
//		
//		 //interpretation of Messages
//		
//		if (msg.setPerformative =ACLPerformative.Missioncommand)
//	}	
//		
//	
//	
//	public boolean SendMessage( ACL_Message msg ) {
//		
//		RepastEdge<Vehicle> edg_temp;
//		Network net = (Network)context.getProjection("Comunication_network");  
//
//		((Responder) Reciver).RecivedMessage(msg);
//		edg_temp = net.getEdge(this,Reciver);
//		edg_temp.setWeight(edg_temp.getWeight() + 1) ;
//		
//		return true;
//	    
//		}
//	
//	
//		
//				

}


