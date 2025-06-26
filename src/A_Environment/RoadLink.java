package A_Environment;

import java.util.ArrayList;
import com.vividsolutions.jts.geom.Coordinate;
import A_Agents.Responder;
import D_Ontology.Ontology.Level;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.gis.Geography;

public class RoadLink {

	ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule(); 

	public boolean Needschedule=false;
	//feature's Attributes
	public String fid;
	public String theme ;
	public String descript_1 ;
	public String natureOfRo ;
	public double length;
	public RoadNode source,target;
	public int ColorCode =0; //for visualization purpose	
	public int ColorCodeParts =0; //for visualization purpose	
	//----------------------------------------------------------------------

	public boolean ReporttoALCxxx = false; //Special used by ALC to repoert about traffic 

	//---------------------------------------------------------------------
	public Level WreckageLevel=Level.None;
	public Level TrafficLevel =Level.None;
	public  boolean  InsideCordon=false;
	
	public  boolean print_ReportW=false , print_ReportT=false;
	public int Wreckage_usedBefor=0,Wreckage_usedAfter=0 ;
	public int Traffic_usedBefor=0,Traffic_usedAfter =0;
	
	//----------------------------------------------------------------------
	//used by Firefighter
	public double EstimatedTimeWreckage; //no change 
	public  double EstimatedTimeClearW ; // in second
	public int currentworkersClearW=0 ;
	public ArrayList<Responder> CurrentworkersW_list=new ArrayList<Responder>();
	public  double StartTimeClearW=0 ,FinishTimeClearW ;
	public  boolean DoneW=false;

	//----------------------------------------------------------------------
	//used by Policeman	
	public Level TrafficduringOnset =Level.None;
	public boolean secured = false;  // Special used by policeman
	public double  lastTime_update=0  ;  // Special used by policeman
	public boolean  Needtosecured =false ;   // in cordon no need 

	//----------------------------------------------------------------------
	public double  EstimatedTimeTraffic; //no change 
	public  double EstimatedTimeClearT ; // in second
	public int currentworkersClearT=0 ;
	public ArrayList<Responder> CurrentworkersT_list=new ArrayList<Responder>();
	public  double StartTimeClearT=0 ,FinishTimeClearT ;
	public  boolean DoneT=false;

	public ArrayList<TopographicArea> Parts = new ArrayList<TopographicArea>();

	public PointDestination PointofClear;
	
	
	
	//-------------------------------------------------------------------------------------------------------------------------
	//clear
	public boolean WillbeusedinNW=false;
		
	//----------------------------------------------------------------------------------------------------------------------
	public RoadLink(String  fid1 , String theme1 , String descript_11 , String natureOfRo1 , double _length1 , RoadNode s, RoadNode t ) {

		fid=fid1 ;
		theme=theme1 ;
		descript_1=descript_11;
		natureOfRo=natureOfRo1; //Single Lane / Dual Lane
		length=_length1;
		source=s;
		target=t;	

	}
	public Coordinate getCurrentPosition(Geography<Object> _geography ) {		

		return  _geography.getGeometry(this).getCentroid().getCoordinate() ;
	}


	public boolean IsRoadLinkhavetheseNodes(RoadNode _node1, RoadNode _node2)
	{

		if ( (this.source== _node1 && this.target==_node2 ) || (this.source== _node2 && this.target==_node1 ) )
			return true;
		else
			return false;

	}

	public String getName() {
		return fid;
	}

	// speed saved in mph
	public double getRoadSpeed()  //mph
	{
		double speed=10;
		
		switch(descript_1)
		{
		case "Junction delay":speed=2.5;
		break;			
		case "Motorway":speed=35;       
		break;
		case "A Road":speed=29;             
		break;
		case "B Road":speed=24;    
		break;
		case "Minor Road":speed=19 ; 
		break;
		case "Local Street":speed=14;  
		break;
		case "Private Road- Restricted":speed=5;
		break;
		case "Private Road- Publicly":speed=5;
		break;
		case "Alley":speed=3;
		break;
		case "Pedestrianised Street":speed=2;
		break;
		default:speed= 40 ;  
		break;
		}

		
		double wL=0 ,  TL =0;
		if ( WreckageLevel==Level.High  )
		{
			wL=0.50 ;
		}
		else if (WreckageLevel==Level.Medium  )
		{
			wL=0.30 ;
		}
		else if (WreckageLevel==Level.Low)
		{
			wL=0.20 ;
		}


		if (  TrafficLevel==Level.High)
		{
			 TL=.40 ;
		}
		else if ( TrafficLevel==Level.Medium  )
		{
			 TL=.20 ;
		}
		else if (  TrafficLevel==Level.Low  )
		{
			 TL=0.10 ;
		}
		
		
		speed= speed - (speed *(wL +TL) ) ;
		
		
		
//		if ( WreckageLevel==Level.High  )
//		{
//			speed= speed - (speed *.60) ;
//		}
//		else if (WreckageLevel==Level.Medium  )
//		{
//			speed= speed - (speed *.40) ;
//		}
//		else if (WreckageLevel==Level.Low)
//		{
//			speed= speed - (speed *.20) ;
//		}
//
//
//		if (  TrafficLevel==Level.High)
//		{
//			speed= speed - (speed *.40) ;
//		}
//		else if ( TrafficLevel==Level.Medium  )
//		{
//			speed= speed - (speed *.20) ;
//		}
//		else if (  TrafficLevel==Level.Low  )
//		{
//			speed= speed - (speed *.10) ;
		
		//speed= 60 ; 		
		//speed= 300 ; 
		
		return speed;
	}

	public int getColorCode() {
		return this.ColorCode;
	}

	public void setColorCode(int ColorCode1) {
		this.ColorCode = ColorCode1;
	}

	//****************************************************************************************
	public void IworkinRouteW(Responder workers ){		

		CurrentworkersW_list.add(workers);		
		currentworkersClearW ++ ;
		if ( StartTimeClearW== 0 )
		{StartTimeClearW=schedule.getTickCount() ; EstimatedTimeClearW =EstimatedTimeWreckage;}
	}

	public void INOTworkinRouteW(Responder workers ){		

		CurrentworkersW_list.remove(workers);		

	}
	//****************************************************************************************
	public void IworkinRouteT(Responder workers ){		

		CurrentworkersT_list.add(workers);		
		currentworkersClearT ++ ;
		if ( StartTimeClearT== 0 )
		{StartTimeClearT=schedule.getTickCount() ;EstimatedTimeClearT=EstimatedTimeTraffic ;}
	}

	public void INOTworkinRouteT(Responder workers ){		

		CurrentworkersT_list.remove(workers);		

	}
	//****************************************************************************************
	public void  SetColorWreckageandTraffic()
	{	

		if ( WreckageLevel==Level.High  &&  TrafficLevel==Level.High )
		{		
			ColorCodeParts=10 ;  //Dark Red
		}

		else if ( WreckageLevel==Level.High || TrafficLevel==Level.High )
		{
			ColorCodeParts=11 ; // Light Red
		}
		else if (WreckageLevel==Level.Medium &&  TrafficLevel==Level.Medium  )
		{
			ColorCodeParts=12 ; // Dark orange
		}
		else if (WreckageLevel==Level.Medium ||  TrafficLevel==Level.Medium  )
		{
			ColorCodeParts=13 ; // Light orange
		}
		else if (WreckageLevel==Level.Low &&  TrafficLevel==Level.Low  )
		{
			ColorCodeParts=14 ; // Dark Yellow
		}
		else if (WreckageLevel==Level.Low ||  TrafficLevel==Level.Low  )
		{
			ColorCodeParts=15 ; // Light Yellow
		}
		else if (WreckageLevel==Level.None &&  TrafficLevel==Level.None )
		{
			ColorCodeParts=2 ; // Gray
		}

		for ( TopographicArea P :Parts )
			P.ColorCode=ColorCodeParts ;

	}
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	// Road Link-  General
	//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	//@ScheduledMethod(start = 1, interval = 1)  in incident 
	public void step() {

		//******************
		if (StartTimeClearW >0 && !DoneW )
		{
			EstimatedTimeClearW = EstimatedTimeClearW - currentworkersClearW   ; 
			if ( EstimatedTimeClearW <=0  )
			{
				DoneW=true ;
				WreckageLevel=Level.None;
				SetColorWreckageandTraffic() ;		
			}
		}
		//******************
		if (StartTimeClearT >0 && !DoneT )
		{
			EstimatedTimeClearT = EstimatedTimeClearT - currentworkersClearT   ; 
			if ( EstimatedTimeClearT <=0  )
			{
				DoneT=true ;
				TrafficLevel=Level.None ;
				SetColorWreckageandTraffic() ;

				//lastTime_update=schedule.getTickCount() ;
				//if ( TrafficLevel==Level.None )   // temp action 
				//{
				//ColorCodeParts=2 ; // Gray
				//}
				//
				//for ( TopographicArea P :Parts )
				//P.ColorCode=ColorCodeParts ;
			}

		}
		//******************
		//dynamic of go back to traffic if there is no scured 
		if ( ! secured && Needtosecured  &&  lastTime_update > 0     )
		{

			if (  ( schedule.getTickCount()  > ( lastTime_update + EstimatedTimeTraffic ) ) )
			{
				if (TrafficLevel==Level.None )
					TrafficLevel=Level.Low ;
				else if (TrafficLevel==Level.Low  && TrafficduringOnset != Level.Low)
					TrafficLevel=Level.Medium ;
				else if (TrafficLevel==Level.Medium && TrafficduringOnset != Level.Low &&TrafficduringOnset != Level.Medium)
					TrafficLevel=Level.High;

				SetColorWreckageandTraffic() ;

				lastTime_update=schedule.getTickCount() ;
			}
		}

	}// end Step **

}

//for (Responder   Resp :CurrentworkersW_list)	
//	Resp.SensingeEnvironmentTrigger=RespondersTriggers.RouteCleared;
