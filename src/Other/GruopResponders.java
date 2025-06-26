package Other;

import java.util.ArrayList;

import A_Agents.Responder;
import A_Environment.PointDestination;
import D_Ontology.Ontology;
import D_Ontology.Ontology.RandomWalking_StrategyType;


public class GruopResponders {

	static  int Groupcounter=0 ,Paircounter=0 ;		
	static  public ArrayList<Responder> INITIATOR_list = new ArrayList<Responder>() ; 
	
	//----------------------------------------------------
	public int GroupID ;
	public Responder leder=null;
	public ArrayList<Responder> Group_list = new ArrayList<Responder>() ; 

	//----------------------------------------------------
	public PointDestination DirectionOfSetcordons ;
	public PointDestination DirectionOfSearch ;
	public int SetCordonClockDirection ,SearchClockDirection;	
	public RandomWalking_StrategyType WS;
	public double WalkingBeforSearch_duration;
	
	
	//----------------------------------------------------
	public GruopResponders Groupwithus = null ,GroupOppsitus= null ;
	//----------------------------------------------------

	public  GruopResponders()
	{
		GroupID=Groupcounter++;		

		INITIATOR_list.add(GroupID, null);

		//System.out.println(" no error "+ GroupID );
	}


	public boolean Set_INITIATOR( Responder Res )
	{
		boolean done=false;

		if (INITIATOR_list.get(GroupID)==null)
		{
			INITIATOR_list.set(GroupID, Res);
			done=true; 
			System.out.println(Res.Id + " no error "+ GroupID );
		}

		return done;
	}


	public boolean Reset_INITIATOR(Responder Res  )
	{
		boolean done=false;

		if (INITIATOR_list.get(GroupID)==Res )
		{
			INITIATOR_list.set(GroupID, null);
			done=true; 
		}

		return done;
	}

	//***************************************************************************************************
	//Police
	public  void Set_details(PointDestination _DirectionOfSetcordons ,int _SetCordonClockDirection  ,PointDestination _DirectionOfSearch ,int _SearchClockDirection ,ArrayList<Responder> _Group_list ,GruopResponders _Groupwithus ,GruopResponders _GroupOppsitus )
	{			
		DirectionOfSetcordons=_DirectionOfSetcordons ;
		DirectionOfSearch=_DirectionOfSearch;
		SetCordonClockDirection =_SetCordonClockDirection ;
		SearchClockDirection=_SearchClockDirection;			
		GroupOppsitus=_GroupOppsitus ;
		Groupwithus=_Groupwithus ;
		Group_list=_Group_list;
	}

	//***************************************************************************************************
	//Police
	public  void Set_details(PointDestination _DirectionOfSearch ,int _SearchClockDirection, RandomWalking_StrategyType _WS,ArrayList<Responder> _Group_list ,GruopResponders _Groupwithus ,GruopResponders _GroupOppsitus )
	{					
		DirectionOfSearch=_DirectionOfSearch;
		SearchClockDirection=_SearchClockDirection;	
		WS=_WS;
		GroupOppsitus=_GroupOppsitus ;
		Groupwithus=_Groupwithus ;
		Group_list=_Group_list;
	}

	//***************************************************************************************************
	//FireFighter
	public  void Set_details(PointDestination _DirectionOfSearchforcasuality,double _WalkingBeforSearch_duration ,RandomWalking_StrategyType _WS,ArrayList<Responder> _Group_list  )
	{				
		DirectionOfSearch=_DirectionOfSearchforcasuality ;
		WS=_WS;
		Group_list=_Group_list;
		WalkingBeforSearch_duration=_WalkingBeforSearch_duration;
	}
	//***************************************************************************************************
		//Ambulance 
	public  void Set_details(PointDestination _DirectionOfSearchforcasuality ,RandomWalking_StrategyType _WS,ArrayList<Responder> _Group_list  )
	{				
		DirectionOfSearch=_DirectionOfSearchforcasuality ;
		WS=_WS;
		Group_list=_Group_list;

	}




}
