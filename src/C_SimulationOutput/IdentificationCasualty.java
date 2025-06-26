package C_SimulationOutput;

import com.jidesoft.awt.geom.Insets2D.Double;

import A_Agents.Casualty;
import A_Environment.Incident;
import D_Ontology.Ontology.CasualtyinfromationType;

public class IdentificationCasualty {


	public double  situationalawareness_casualtiesidentification ;



	public void update_SWCI( Incident  icnd)
	{
		int Totalacurat=0  ;
		for (Casualty ca:  icnd.casualties )
			if ( ca.AcurrateCI == ca.EOCCI)
				Totalacurat++ ;

		situationalawareness_casualtiesidentification= Totalacurat/icnd.casualties.size()  ;
	}

}