package A_Environment;

import com.vividsolutions.jts.geom.Coordinate;
import Other.GruopResponders;
import repast.simphony.context.Context;
import repast.simphony.space.gis.Geography;


public class TopographicArea {

	// feature's Attributes
	public String fid;
	public double calculated;
	private String FeatureCod;

	private String theme;
	public String descriptiv;
	private int cat;
	private int ISAlreadyChecked=0;
	public int AffectedbyInciden=0;
	public boolean IsAssignedToResponder=false;
	public int ColorCode =0; //for visualization purpose
	public PointDestination EntranceBuld;

	public GruopResponders buildingdiscoverbythisgroup=null;

	public boolean used_byprogram=false ; //if not used removed
	public Context<Object> context;
	public Geography<Object> geography;

	//----------------------------------------------------------------------------------------------------------------------
	public TopographicArea(String  fid1 , double calculated1,String FeatureCod1, String theme1, String descriptiv1 , Context<Object> _context, Geography<Object> _geography ) {

		fid=fid1;
		calculated=calculated1;
		this.FeatureCod = FeatureCod1;
		this.theme = theme1;
		this.descriptiv = descriptiv1;

		context=_context ;
		geography=_geography ;


		if (descriptiv.equals((String) "Path")  ||descriptiv.equals((String) "Path,Structure ") )
		{ this.cat = 1;ColorCode = 3; }

		else if (descriptiv.equals((String) "Road Or Track") ||descriptiv.equals((String) "Structure,Road Or Track")  ||descriptiv.equals((String) "Structure,General Surface")) //
		{ this.cat = 2;ColorCode =2 ;}

		else if (  descriptiv.equals((String) "Roadside" )  ||  descriptiv.equals((String) "Roadside,Structure") ||descriptiv.equals((String) "Roadside,Path")||descriptiv.equals((String) "Structure,Roadside")  ) //
		{ this.cat = 3;ColorCode = 3;}	

		else if (descriptiv.equals((String) "Building")) //obstacle
		{this.cat = 4;ColorCode =4; }

		else if (descriptiv.equals((String) "General Surface")  || descriptiv.equals((String) "General Surface,Structure") )
		{this.cat = 5;ColorCode =  3 ;}

		else if (descriptiv.equals((String) "Landform"))
		{this.cat = 2;ColorCode =7;}

		else if (descriptiv.equals((String) "Rail"))
		{this.cat = 4;ColorCode =8;} //obstacle    

		else if (descriptiv.equals((String) "Structures,Roads Tracks And Paths,Land"))  
		{ this.cat = 0;ColorCode = 3;}			

		else if (descriptiv.equals((String) "Natural Environment")   ||    descriptiv.equals((String) "Rail,Natural Environment") ||  descriptiv.equals((String) "Natural Environment,Rail") )  
		{ this.cat = 0;ColorCode = 5;}	

		else
		{this.cat=7;ColorCode = 9;}






	}

	//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
	public int getISAlreadyChecked() {
		return this.ISAlreadyChecked;
	}

	public void setISAlreadyChecked(int checked) {
		this.ISAlreadyChecked = checked; // 0 or 1
		ColorCode =6;
	}

	public void setAffectedbyInciden() {
		AffectedbyInciden=1;
		ColorCode =5;
	}

	public boolean IsAffectedbyInciden() {
		if (AffectedbyInciden==1)
			return true;
		else
			return false;
	}

	public int getcode() {
		return this.cat;
	}

	public void setcode(int theme1) {
		this.cat = theme1;
	}

	public int getColorCode() {
		return this.ColorCode;
	}

	public void setColorCode(int ColorCode1) {
		this.ColorCode = ColorCode1;
	}


	public Coordinate getCurrentPosition() {		

		return  geography.getGeometry(this).getCentroid().getCentroid().getCoordinate();
	}
	
	//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
	
	

}
