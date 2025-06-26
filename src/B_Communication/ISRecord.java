package B_Communication;

import A_Agents.Casualty;
import A_Agents.Hospital;
import A_Agents.Responder_Ambulance;
import A_Agents.Vehicle;
import A_Agents.Vehicle_Ambulance;
import A_Environment.Sector;
import D_Ontology.Ontology;
import D_Ontology.Ontology.Ambulance_ResponderRole;

public class ISRecord {

	public double LastupdateTime=0 ;
	public  boolean Visible=false;

	public Casualty CasualtyinRec;	
	public  Responder_Ambulance  AssignedSC;
	public  Responder_Ambulance  AssignedCCO;
	public Sector inSector=null;

	public  Responder_Ambulance  AssignedParamdicinRec; //last responder work in casualty LastUpdatedBy;
	public  Vehicle  AssignedAmbulanceinRec;      //updated by ALC ......AssignedDriverinRec
	public  Hospital  AssignedHospitalinRec;	//updated by AIC ......AssignedDriverinRec

	public boolean  Hospitalupdated_ALC=false ;
	//----------------------------------------------------------

	public  int priority_levelinRec=99 ;
	public  boolean FieldTriaged=false;
	public  boolean Pre_treatment=false ,  IssuedByAmbSC=false, IssuedByAmbCCO=false ;	
	public  boolean SecondTriaged=false;	


	public  boolean SivirtyGetbad_CCO=false;  // for CCO only
	public  boolean SivirtyGetbad_AIC=false;  // for AIC only
	public  boolean SivirtyGetbad_ALC=false;  // for ALC only

	public  boolean Treated=false;
	public  boolean TransferdV=false; 
	public  boolean TransferdH=false;
	//----------------------------------------------------------
	public  boolean checkedSC_DoneFieldTriage=false ; // After triage to use responder   Decentralized only  and update plan
	public  boolean checkedCCO_DoneSecondTriage=false ; // update plan  for ccs

	public  boolean checkedCCO_DoneTreatment=false ; // After treatment to use responder if it will not wait with casualty  ,right now not useful and update plan 
	public  boolean checkedCCO_DoneHosandAmb=false ;  /// after treatment and assigned hosp and assigned ambulance  to command
	public  boolean checkedCCO_DoneonAmb=false ;   // after transfer to close task

	public  boolean checkedAIC_DoneSecondTriage=false ; // After triage to allocate hosp

	public  boolean checkedALC_AllocateAmb=false ; // After treatment and assigned hospital to allocate amb
	public  boolean checkedALC_inAmb=false ;  // after transfer to close task

	public Boolean FatlityRecord= false;
	public Boolean  CLOSED=false;	

	public Boolean  CLOSED_ALC=false;	
	
	
	public boolean IwillupdateaboutTrapparrivedCCS=false;

	//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%	

	public ISRecord(Casualty _Casualty ,Responder_Ambulance  _Res ,  Responder_Ambulance  _AssignedSC , Sector _inSector )
	{
		CasualtyinRec=_Casualty;
		AssignedParamdicinRec =_Res;
		AssignedSC=_AssignedSC;
		inSector=_inSector;

	}

	public void UpdateISRecord_FieldTriage(Responder_Ambulance  _AssignedParamdicinRec ,int _priority_levelinRec )
	{
		AssignedParamdicinRec=_AssignedParamdicinRec;

		FieldTriaged= true;
		priority_levelinRec=_priority_levelinRec ;

		if (_priority_levelinRec==5  )			
			FatlityRecord= true;
	}

	public void UpdateISRecord_FieldTriageandPre_teatment(Responder_Ambulance  _AssignedParamdicinRec ,int _priority_levelinRec )
	{
		AssignedParamdicinRec=_AssignedParamdicinRec;

		FieldTriaged= true;
		Pre_treatment=true;

		priority_levelinRec=_priority_levelinRec ;

		if (_priority_levelinRec==5  )			
			FatlityRecord= true;
	}

	public void UpdateISRecord_SecondTriage(Responder_Ambulance  _AssignedParamdicinRec ,int _priority_levelinRec)
	{
		AssignedParamdicinRec=_AssignedParamdicinRec;		
		SecondTriaged= true;

		if (_priority_levelinRec==5  )			
			FatlityRecord= true;

		if (priority_levelinRec!=99 && priority_levelinRec!=_priority_levelinRec  )
		{SivirtyGetbad_CCO=true;	  SivirtyGetbad_AIC=true;   } // SivirtyGetbad_ALC=true; not used


		priority_levelinRec=_priority_levelinRec ;
	}

	public void UpdateISRecord_Teatment(Responder_Ambulance  _AssignedParamdicinRec ,int _priority_levelinRec )
	{
		AssignedParamdicinRec=_AssignedParamdicinRec;
		Treated=true;


		//new added 
		if (_priority_levelinRec==5  )			
			FatlityRecord= true;

		if ( priority_levelinRec!=_priority_levelinRec  )
		{  SivirtyGetbad_AIC=true; }	//SivirtyGetbad_CCO=true and SivirtyGetbad_ALC=true;   not used	


		priority_levelinRec=_priority_levelinRec ;

	}		
	public void UpdateISRecord_Hospital(Hospital  _AssignedHospitalinRec ) // by AIC
	{

		if (   AssignedHospitalinRec!= null )
		{ Hospitalupdated_ALC=true;}

		AssignedHospitalinRec=_AssignedHospitalinRec;

	}

	public void UpdateISRecord_Ambulance(Vehicle  _AssignedAmbulanceinRec ) // ALC
	{
		AssignedAmbulanceinRec =_AssignedAmbulanceinRec ;

	}
	public void UpdateISRecord_VTransfer(Responder_Ambulance  _AssignedParamdicinRec )
	{
		AssignedParamdicinRec=_AssignedParamdicinRec;
		TransferdV=true;
	}

	public void UpdateISRecord_HTransfer(Responder_Ambulance  _AssignedParamdicinRec )//by driver
	{
		AssignedParamdicinRec=_AssignedParamdicinRec;
		TransferdH=true;
	}

}
