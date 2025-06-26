package B_Communication;

import java.util.ArrayList;

import A_Agents.Casualty;
import A_Agents.Hospital;
import A_Agents.Responder;
import A_Agents.Responder_Ambulance;
import A_Agents.Vehicle;
import A_Environment.Cordon;
import A_Environment.PointDestination;
import A_Environment.RoadLink;
import A_Environment.Sector;
import A_Environment.TacticalArea;
import D_Ontology.Ontology.Ambulance_ActivityType;
import D_Ontology.Ontology.Ambulance_ResponderRole;
import D_Ontology.Ontology.Ambulance_TaskType;
import D_Ontology.Ontology.Fire_ActivityType;
import D_Ontology.Ontology.Fire_ResponderRole;
import D_Ontology.Ontology.Fire_TaskType;
import D_Ontology.Ontology.Police_ActivityType;
import D_Ontology.Ontology.Police_ResponderRole;
import D_Ontology.Ontology.Police_TaskType;
import D_Ontology.Ontology.RandomWalking_StrategyType;
import D_Ontology.Ontology.TaskAllocationApproach;
import Other.GruopResponders;

public class Command {

	public String commandID ;

	public  Ambulance_ActivityType commandActivityType1 ;
	public  Fire_ActivityType commandActivityType2 ;
	public  Police_ActivityType commandActivityType3 ;

	public  Ambulance_TaskType commandType1 ;
	public  Fire_TaskType commandType2 ;
	public  Police_TaskType commandType3 ;

	public Ambulance_ResponderRole  AssignedRole1; 
	public Fire_ResponderRole  AssignedRole2; 
	public Police_ResponderRole  AssignedRole3; 

	public Casualty TargetCasualty;
	public Casualty_info TargetCasualty_info ;
	public ArrayList<Casualty> TargetCasualty_List=new ArrayList<Casualty>();
	public  Hospital TargetHospital ; 
	public  Vehicle AssignedAmbulance ; 

	public Sector  TargetSector ;
	public Cordon TargetCordon ;
	public TacticalArea  TargetTA ;
	public RoadLink TargetRoad;
	public ArrayList<RoadLink> TargetRoad_List=new ArrayList<RoadLink>();
	public PointDestination  TargetDirectionDistination ;

	public boolean ISDeadinInnerorCCS ;  // First true ..  true:inner ,RC  false:CCS ,NOT;

	public int need;	
	public ArrayList<Responder_Ambulance>  DriverList ;

	//=======================================================================================================================================

	public void AmbulanceCommand( String _commandID , Ambulance_ResponderRole  _AssignedRole   )   //not all parameter used
	{
		commandID=_commandID ;	
		commandType1= null;
		AssignedRole1= _AssignedRole ;		
	}

	public void AmbulanceCommand( String _commandID , Ambulance_ResponderRole  _AssignedRole  , TacticalArea  _TargetTA  ,Sector  _TargetSector , ArrayList<Responder_Ambulance>  _DriverList  )   //not all parameter used
	{
		commandID=_commandID ;	
		commandType1= null;
		AssignedRole1= _AssignedRole ;
		TargetTA=_TargetTA ;
		TargetSector =_TargetSector ;
		DriverList =_DriverList ; 
	}

	//------------------------------------------------------------ Activity 
	public void AmbulanceCommand( String _commandID , Ambulance_ActivityType   _commandActivityType ,  TacticalArea  _TargetTA  , Sector  _TargetSector  ) // From AIC 
	{
		commandID=_commandID ;		
		commandActivityType1= _commandActivityType ;		
		TargetTA=_TargetTA ;
		TargetSector=_TargetSector ;

	}

	public void  AmbulanceCommand( String _commandID ,Ambulance_TaskType  _commandType )
	{
		commandID=_commandID ;	
		commandType1= _commandType ;

	}

	public void  AmbulanceCommand( String _commandID ,Ambulance_TaskType  _commandType ,  TacticalArea  _TargetTA )
	{
		commandID=_commandID ;	
		commandType1= _commandType ;
		TargetTA=_TargetTA ;
	}

	public void AmbulanceCommand( String _commandID , Ambulance_TaskType  _commandType , Casualty _TargetCasualty )
	{
		commandID=_commandID ;		
		commandType1= _commandType ;
		TargetCasualty= _TargetCasualty;

	}
	
	public void AmbulanceCommand( String _commandID , Ambulance_TaskType  _commandType   , Casualty _TargetCasualty  ,Vehicle _AssignedAmbulance  )
	{
		commandID=_commandID ;	
		commandType1= _commandType ;
		TargetCasualty= _TargetCasualty;
		AssignedAmbulance=_AssignedAmbulance; 

	}

	public void AmbulanceCommand( String _commandID , Ambulance_TaskType  _commandType  ,Hospital _TargetHospital )
	{
		commandID=_commandID ;		
		commandType1= _commandType ;
		TargetHospital=_TargetHospital;
	}

	public void AmbulanceCommand( String _commandID , Ambulance_TaskType  _commandType  ,int _need )
	{
		commandID=_commandID ;		
		commandType1= _commandType ;
		need=_need;
	}

	public void AmbulanceCommand( String _commandID , Ambulance_TaskType  _commandType ,  Casualty_info _TargetCasualty_info  ,Hospital _TargetHospital    )
	{
		commandID=_commandID ;		
		commandType1= _commandType ;
		TargetCasualty_info= _TargetCasualty_info ;
		TargetHospital=_TargetHospital;
		
	}
	public void AmbulanceCommand( String _commandID , Ambulance_TaskType  _commandType , Casualty _TargetCasualty ,Hospital _TargetHospital    )
	{
		commandID=_commandID ;		
		commandType1= _commandType ;
		TargetCasualty= _TargetCasualty;
		TargetHospital=_TargetHospital;
	}

	//=======================================================================================================================================
	public void FireCommand( String _commandID , Fire_ResponderRole  _AssignedRole  ,  Sector  _TargetSector ,Cordon _TargetCordon   )   //not all parameter used
	{
		commandID=_commandID ;	
		commandType1= null;
		AssignedRole2= _AssignedRole ;
		TargetSector=_TargetSector ;
		TargetCordon  =_TargetCordon  ;
	}
	//------------------------------------------------------------ Activity 
	public void FireCommand( String _commandID , Fire_ActivityType   _commandActivityType ,   Sector  _TargetSector ,Cordon _TargetCordon     ) // From FIC 
	{
		commandID=_commandID ;		
		commandActivityType2= _commandActivityType ;					
		TargetSector=_TargetSector ;	
		TargetCordon  =_TargetCordon  ;
	}
	//------------------------------------------------------------
	public void  FireCommand( String _commandID , Fire_TaskType  _commandType ,Casualty _TargetCasualty )
	{
		commandID=_commandID ;	
		commandType2= _commandType ;
		TargetCasualty=_TargetCasualty ;

	}
	//------------------------------------------------------------
	public void FireCommand( String _commandID , Fire_TaskType  _commandType  , Sector  _TargetSector   )   
	{
		commandID=_commandID ;	
		commandType2= _commandType ;
		TargetSector =_TargetSector ;
	}	

	//------------------------------------------------------------
	public void FireCommand( String _commandID , Fire_TaskType  _commandType )   
	{
		commandID=_commandID ;	
		commandType2= _commandType ;
	}	
	//------------------------------------------------------------
	public void FireCommand( String _commandID , Fire_TaskType  _commandType , ArrayList<RoadLink> _TargetRoad_List ) // from FIC to FSC
	{
		commandID=_commandID ;		
		commandType2= _commandType ;

		for(  RoadLink  RL: _TargetRoad_List)
			TargetRoad_List.add(RL);

	}	
	//------------------------------------------------------------
	public void  FireCommand( String _commandID , Fire_TaskType  _commandType ,RoadLink _TargetRoad )  //from FSC to firefighter
	{
		commandID=_commandID ;	
		commandType2= _commandType ;
		TargetRoad=_TargetRoad ;

	}

	//=======================================================================================================================================

	public void PoliceCommand( String _commandID , Police_ResponderRole  _AssignedRole   )   
	{
		commandID=_commandID ;	
		commandType1= null;
		AssignedRole3= _AssignedRole ;

	}
	//------------------------------------------------------------
	public void PoliceCommand( String _commandID , Police_ResponderRole  _AssignedRole ,Cordon _TargetCordon ,TacticalArea  _TargetTA   )  
	{
		commandID=_commandID ;	
		commandType1= null;
		AssignedRole3= _AssignedRole ;
		TargetCordon=_TargetCordon ;
		TargetTA=_TargetTA ;
	}
	//------------------------------------------------------------ Activity 
	public void PoliceCommand( String _commandID , Police_ActivityType   _commandActivityType , 	 Cordon _TargetCordon , TacticalArea  _TargetTA  ) // From PIC  
	{
		commandID=_commandID ;		
		commandActivityType3= _commandActivityType ;
		TargetCordon =_TargetCordon ; 
		TargetTA=_TargetTA ;
	}

	//------------------------------------------------------------
	public void PoliceCommand( String _commandID , Police_TaskType  _commandType  ) // from CC to policeman
	{
		commandID=_commandID ;		
		commandType3= _commandType ;

	}
	//------------------------------------------------------------
	public void PoliceCommand( String _commandID , Police_TaskType  _commandType , ArrayList<RoadLink> _TargetRoad_List ) // from PIC to CC
	{
		commandID=_commandID ;		
		commandType3= _commandType ;

		for(  RoadLink  RL: _TargetRoad_List)
			TargetRoad_List.add(RL);

	}	
	//------------------------------------------------------------
	public void PoliceCommand( String _commandID , Police_TaskType  _commandType , RoadLink _TargetRoad ) //from CC to policeman
	{
		commandID=_commandID ;		
		commandType3= _commandType ;
		TargetRoad= _TargetRoad;
	}
	//------------------------------------------------------------
	public void PoliceCommand( String _commandID , Police_TaskType  _commandType   , RoadLink _TargetRoad , PointDestination  _AssignedEA  ) // one of them will be null
	{
		commandID=_commandID ;	
		commandType3= _commandType ;
		TargetRoad= _TargetRoad;
		//=_AssignedEA; 
	}
	

	//------------------------------------------------------------
	public void PoliceCommand( String _commandID , Police_TaskType  _commandType , ArrayList<Casualty> _TargetCasualty_List ,  boolean _ISDCSeadinInnerorCCS ) // from PIC to RCO
	{
		commandID=_commandID ;		
		commandType3= _commandType ;		
		for(  Casualty  Ca: _TargetCasualty_List)
			TargetCasualty_List.add(Ca);
		ISDeadinInnerorCCS = _ISDCSeadinInnerorCCS;
	}
	//------------------------------------------------------------
	public void PoliceCommand( String _commandID , Police_TaskType  _commandType , Casualty _TargetCasualty , boolean _ISDCSeadinInnerorCCS) //   from RCO to policeman
	{
		commandID=_commandID ;		
		commandType3= _commandType ;
		TargetCasualty= _TargetCasualty;
		ISDeadinInnerorCCS= _ISDCSeadinInnerorCCS ;
	}
	//------------------------------------------------------------
	public void PoliceCommand2( String _commandID , Police_TaskType  _commandType , ArrayList<Casualty> _TargetCasualty_List) //   from RCO to policeman lission 
	{
		commandID=_commandID ;		
		commandType3= _commandType ;
		for(  Casualty  Ca: _TargetCasualty_List)
			TargetCasualty_List.add(Ca);
	}
}

