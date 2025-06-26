package A_Environment;

import java.util.List;
import javax.measure.unit.SI;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;
import D_Ontology.BuildStaticFuction;
import D_Ontology.Ontology.CordonType;
import repast.simphony.context.Context;
import repast.simphony.space.gis.Geography;

public class PointDestination {

	Coordinate loc; //not uesd
	public String Name="";
	public int id=-1;	//Used for index of inner and  outer cordons only
	public int ColorCode =0; //for visualization purpose	

	public CordonType type ;

	//----------------------
	public boolean Isoccupied=false;  // Spatial for TA bed or bark or chair
	public boolean behindbulding=false;	// for police	
	public boolean checkedbypolice= false;
	
	//----------------------
	public Context<Object> context;
	public Geography<Object> geography;

	public PointDestination ( Context<Object> _context, Geography<Object> _geography,Coordinate _Location )

	{
		loc = _Location ;

		//----------------------------------------------------------------------
		context = _context;
		geography = _geography;
		GeometryFactory fac = new GeometryFactory();		
		context.add(this);
		geography.move(this, fac.createPoint(_Location));			
	}

	//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
	//check
	public void ISvalidLocationPoint1( Coordinate  fromCurrentLocation  , double Angle  , double dis , boolean ckeckline) {

		boolean vaildBulding= false;
		boolean validline=false;	
		Geometry geomLine=null;

		this.setlocation(fromCurrentLocation );	
		geography.moveByVector( this , dis , SI.METER,Angle );

		// 1- Get All Near Geographical Objects within incident Area
		@SuppressWarnings("unchecked") //@SuppressWarnings("unchecked") tells the compiler that the programmer believes the code to be safe and won't cause unexpected exceptions.
		List<TopographicArea> nearObjects = (List<TopographicArea>) BuildStaticFuction.GetObjectsWithinDistance(this,TopographicArea.class, 1000 );


		while ( ! vaildBulding )
		{
			if (ckeckline)
			{
				Coordinate[] coor = new Coordinate[2];
				coor[0]= this.getCurrentPosition();
				coor[1]= fromCurrentLocation ;
				check_line chline= new check_line();
				GeometryFactory fac = new GeometryFactory();
				geomLine = fac.createLineString(coor) ;
				context.add(chline);
				geography.move( chline, geomLine);
			}

			vaildBulding= true;
			validline=true;	
			for (TopographicArea obj : nearObjects) 
				if (obj.getcode()==4)// Is building  
				{
					Geometry Geombuilding = (Polygon) geography.getGeometry(obj);
					Geometry GeomPoint = geography.getGeometry(this);
					if ( GeomPoint.within(Geombuilding)) // Is within building  
						vaildBulding=false; 					

					if (ckeckline)
						if  ( geomLine.intersects(Geombuilding))
							validline=false;
				}// end for



			if ( ! vaildBulding )
			{	
				dis= dis- 1 ;
				this.setlocation(fromCurrentLocation );	
				geography.moveByVector( this , dis , SI.METER,Angle );
			}
			else
			{	
				if (ckeckline)
					if ( ! validline )
					{
						vaildBulding=false;
						dis= dis- 1 ;
						this.setlocation(fromCurrentLocation );	
						geography.moveByVector( this , dis , SI.METER,Angle );
					}
			}



		} // end while
		
		nearObjects.clear();
		nearObjects=null ;
	}

	//*****************************************************************
	//check
	public void ISvalidLocationPoint2( Coordinate  fromCurrentLocation  , double Angle  , double dis , boolean ckeckline) {

		boolean vaildBulding= false;
		boolean validline=false;	
		boolean cordonfar=true, firestcheck=true;
		Geometry geomLine=null;

		this.setlocation(fromCurrentLocation );	
		geography.moveByVector( this , dis , SI.METER,Angle );

		// 1- Get All Near Geographical Objects within incident Area
		@SuppressWarnings("unchecked") //@SuppressWarnings("unchecked") tells the compiler that the programmer believes the code to be safe and won't cause unexpected exceptions.
		List<TopographicArea> nearObjects = (List<TopographicArea>) BuildStaticFuction.GetObjectsWithinDistance(this,TopographicArea.class, 1000 );


		while ( ! vaildBulding )
		{
			if (ckeckline)
			{
				Coordinate[] coor = new Coordinate[2];
				coor[0]= this.getCurrentPosition();
				coor[1]= fromCurrentLocation ;
				check_line chline= new check_line();
				GeometryFactory fac = new GeometryFactory();
				geomLine = fac.createLineString(coor) ;
				context.add(chline);
				geography.move( chline, geomLine);
			}

			vaildBulding= true;
			validline=true;	
			for (TopographicArea obj : nearObjects) 
				if (obj.getcode()==4)// Is building  
				{
					Geometry Geombuilding = (Polygon) geography.getGeometry(obj);
					Geometry GeomPoint = geography.getGeometry(this);
					if ( GeomPoint.within(Geombuilding)) // Is within building  
					{ vaildBulding=false; 
					if (obj.IsAffectedbyInciden() && firestcheck)
					{cordonfar= true;firestcheck=false;}
					if (! obj.IsAffectedbyInciden() && firestcheck)
					{cordonfar=false;firestcheck=true;}
					}

					if (ckeckline)
						if  ( geomLine.intersects(Geombuilding))
							validline=false;
				}// end for



			if ( ! vaildBulding )
			{	
				if(cordonfar) 
				{dis= dis + 1 ; this.behindbulding=true;}
				else 
					dis= dis - 1 ;
				this.setlocation(fromCurrentLocation );	
				geography.moveByVector( this , dis , SI.METER,Angle );
			}
			else
			{	
				if (ckeckline)
					if ( ! validline )
					{
						vaildBulding=false;
						if(cordonfar) dis= dis + 1 ; else dis= dis - 1 ;
						this.setlocation(fromCurrentLocation );	
						geography.moveByVector( this , dis , SI.METER,Angle );
					}
			}



		} // end while

		
		nearObjects.clear();
		nearObjects=null ;
	}

	//*****************************************************************
	//check
	public void ISvalidLocationPointLRUDatStreet( Coordinate  fromCurrentLocation  , double Angle  , double dis , boolean cloockDir )
	{
		boolean validStreet= false;

		@SuppressWarnings("unchecked") 
		List<TopographicArea> nearObjects = (List<TopographicArea>) BuildStaticFuction.GetObjectsWithinDistance(this,TopographicArea.class, 200 );

		for (int i = 0; i < nearObjects.size(); i++) 
			if (nearObjects .get(i).getcode()!=2) // Is not street 
			{nearObjects.remove(i);i--; }

		this.setlocation(fromCurrentLocation );	
		geography.moveByVector( this , dis , SI.METER,Angle );

		while ( ! validStreet )
		{						
			Geometry GeomPoint = geography.getGeometry(this);
			for (TopographicArea obj : nearObjects) 
			{
				Geometry Geomstreet = (Polygon) geography.getGeometry(obj);					
				if ( GeomPoint.intersects(Geomstreet)) // Is within street 
					validStreet=true; 					
			}// end

			if ( ! validStreet )
			{	
				if (cloockDir ) Angle= Angle- .1;    else Angle= Angle+ .1;

				if ( Angle >6.2831) Angle= Angle-6.2831 ;
				if ( Angle <0) Angle= 6.2831 + Angle ;

				this.setlocation(fromCurrentLocation );	
				geography.moveByVector( this , dis , SI.METER,Angle );
			}

		} // end while
		
		nearObjects.clear();
		nearObjects=null ;

	}

	//*****************************************************************
	//check
	public void ISvalidLocationPointLRUDatnotStreet( Coordinate  fromCurrentLocation  , double Angle  , double dis , boolean cloockDir )
	{
		boolean Street= true;

		@SuppressWarnings("unchecked") 
		List<TopographicArea> nearObjects = (List<TopographicArea>) BuildStaticFuction.GetObjectsWithinDistance(this,TopographicArea.class, 200 );

		for (int i = 0; i < nearObjects.size(); i++) 
			if ( ! ( nearObjects .get(i).getcode()==2 ||nearObjects .get(i).getcode()==3)  ) // Is not street  Roadside
			{nearObjects.remove(i);i--; }

		this.setlocation(fromCurrentLocation );	
		geography.moveByVector( this , dis , SI.METER,Angle );

		while ( Street )
		{						
			Street= false;
			Geometry GeomPoint = geography.getGeometry(this);
			for (TopographicArea obj : nearObjects) 
			{
				Geometry Geomstreet = (Polygon) geography.getGeometry(obj);					
				if ( GeomPoint.intersects(Geomstreet)) // Is within street 
					Street=true; 					
			}// end

			if ( Street )
			{	
				if (cloockDir ) Angle= Angle- .1;    else Angle= Angle+ .1;

				if ( Angle >6.2831) Angle= Angle-6.2831 ;
				if ( Angle <0) Angle= 6.2831 + Angle ;

				this.setlocation(fromCurrentLocation );	
				geography.moveByVector( this , dis , SI.METER,Angle );
			}

		} // end while

		nearObjects.clear();
		nearObjects=null ;
	}
	
	
	public boolean ISvalidLocation_bed_chair_bark() {
	
		boolean r= true;
		// 1- Get All Near Geographical Objects within incident Area
		@SuppressWarnings("unchecked") //@SuppressWarnings("unchecked") tells the compiler that the programmer believes the code to be safe and won't cause unexpected exceptions.
		List<TopographicArea> nearObjects = (List<TopographicArea>) BuildStaticFuction.GetObjectsWithinDistance(this,TopographicArea.class, 100 );

		for (TopographicArea obj : nearObjects) 
			if (obj.getcode()==4)
			{
				Geometry Geombuilding = (Polygon) geography.getGeometry(obj);
				Geometry Geom = geography.getGeometry(this);
				if (Geom.intersects(Geombuilding)    )	// Is within building  	|| Geombuilding.within(GeomCasualty) 		 
				{r= false;break;}
			}
		
		nearObjects.clear();
		nearObjects=null ;

return r ;
	}

	//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

	public void setlocation(Coordinate newPosition) {		

		Coordinate oldPosition = getCurrentPosition();

		double x = (newPosition.x - oldPosition.x);
		double y = (newPosition.y - oldPosition.y);

		geography.moveByDisplacement(this, x, y);
	}

	public Coordinate getCurrentPosition() {		

		return  geography.getGeometry(this).getCoordinate();
	}

	public Geometry getGeometry() {		

		return  geography.getGeometry(this);
	}



	public void setName(int _nothing) {
		id=_nothing;

	}

	public int getName() {
		return  this.id ;
	}

	public int getColorCode() {
		return this.ColorCode;
	}

	public void setColorCode(int ColorCode1) {
		this.ColorCode = ColorCode1;
	}


}
