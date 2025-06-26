package A_Environment;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import repast.simphony.context.Context;
import repast.simphony.space.gis.Geography;

public class RoadNode {
	
	public String fid ;	//Name
	public int ColorCode =0; //for visualization purpose	
	public String Name=" " ;
		
	public boolean aded= false; 
	public Incident Assignedncident=null; 
	
	public Context<Object> context;
	public Geography<Object> geography;
	
	//-------------------------
	public PointDestination PN; //used in walk
	public boolean checkedW= false; //used by BZC Police and BZCFire in search routes
	public boolean checkedT= false;
	
	//-------------------------------------------------------------------------------------------------------------------------
		//clear
		public boolean WillbeusedinNW=false;
		

	//----------------------------------------------------------------------
	public RoadNode (String fid1 , Context<Object> _context, Geography<Object> _geography ) {
		this.fid=fid1 ;
		
		context=_context ;
		geography=_geography ;
	}	

	public Coordinate getCurrentPosition() {		
			
		return  geography.getGeometry(this).getCoordinate();
	}
	
	public Geometry getGeometry() {		
		
		return  geography.getGeometry(this);
	}
	
	public String getFID() {
		return this.fid;
	}
	
	public int getColorCode() {
		return this.ColorCode;
	}

	public void setColorCode(int ColorCode1) {
		this.ColorCode = ColorCode1;
	}
	
}
