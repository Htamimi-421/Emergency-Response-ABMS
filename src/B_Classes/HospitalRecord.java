package B_Classes;
import A_Agents.Hospital;


public class HospitalRecord {
	
	public  int CurrentReservedBedsCount=3;
	public   Hospital   RecivingHospital;

	
	public  boolean checkedAIC=false ; //Spatial ........
	public boolean checkedACO=false; //Spatial ........
	
	public int OrderofNearsttomyLocation =-1; //used by AIC and ACO to order hospital
		
	//---------------------------------------------------------
	
	public 	HospitalRecord(Hospital   _RecivingHospital ,int _ReserveddBedsCount ) {
		
		 RecivingHospital=_RecivingHospital;
		 CurrentReservedBedsCount=_ReserveddBedsCount;		
	}
	
	//---------------------------------------------------------
	public void ReservedBed()
	{
		CurrentReservedBedsCount--;
	}
	
	//---------------------------------------------------------
	public boolean ISThereBed ()
	{
		if (CurrentReservedBedsCount >0)
			return true;
		else
			return false;		
	}
	
}
