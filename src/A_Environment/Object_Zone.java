package A_Environment;

import java.util.ArrayList;
import java.util.List;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import repast.simphony.context.Context;
import repast.simphony.space.gis.Geography;
import repast.simphony.util.ContextUtils;

public class Object_Zone {

/**
 * Zone agents are search areas around vehicle agents that are used to detect agent types nearby. 
 * @author Eric Tatara
 */

	protected boolean visible = true;
	public int active = -1;
	public Coordinate SidePoint;
	public int ID;
	public int safezone=0;
	public Object parent;
	public int Totalcount=0;
		
	
	public Object_Zone(Object _parent)
	{
		parent=_parent;
	}
	
	/**
	 * Returns a list objects that intersect this zone's geometry from the list of  near objects provided. 
	 */
	public boolean lookForObject(Object nearObject){					
		
		Context context = ContextUtils.getContext(this);
		
	
		Geography geography = (Geography)context.getProjection("Geography");
		
		
		// Find all features that intersect the zone feature
		Geometry thisGeom = geography.getGeometry(this);
		
		if (thisGeom!=null && nearObject!=null && (     thisGeom.intersects(geography.getGeometry(nearObject)) ||  (geography.getGeometry(nearObject)).contains(thisGeom) )       )
				{
			return true;
		}		
		return false;
	}
	
	public List<?> lookForObjects(List<?> nearObjects){		
		
		List<Object> objectList = new ArrayList<Object>();		
		
		Context context = ContextUtils.getContext(this);
		Geography geography = (Geography)context.getProjection("Geography");
		
		// Find all features that intersect the zone feature
		Geometry thisGeom = geography.getGeometry(this);
		
		for (Object o : nearObjects){
			if (thisGeom.intersects(geography.getGeometry(o))){
				objectList.add(o);
			}
		}
		
		return objectList;
	}
	
	public String getVehicleKind()
	{
		
		return "";
	}
	
	public int getsafezone()
	{
		return safezone;
	}
	
	public void setsafezone(int safe)
	{
		safezone=safe;
	}
	
	public int getActive() {
		return active;
	}

	public void setActive(boolean _active) {
		if(_active)
			active =0;  //1
		else
			active= 0; //-1
	}
	
	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}
	
	public String getName() {
		return  Integer.toString(active);
	}
	
	//****************************************************************************
	
	public void Addonecasulty()
	{
		Totalcount++;
	}
	
	//****************************************************************************
}

