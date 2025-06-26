package A_Environment;


//used for visualization only 
public class Path {
			
		//feature's Attributes
	  //  Vehicle vechicle;
	    String  v="0" ;
		int num_link ;
		double length;
		
		public Path (  int num_link1 , double length1 ) {

			//this.vechicle=v1;
			this.num_link=num_link1;
			this.length=(double)length1;
		}
		
		public String getName() {
			return v;
		}
			
}
