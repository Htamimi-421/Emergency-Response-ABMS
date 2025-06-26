package B_Classes;

public class Duration_info {
	
	
	public  String Rep= ""  ;
	String hour="";
	public String M="";
	public  int MinNum=0;
	public  int Range_S=0;
	public  int RangeE=0;
	
	
	public  int countMesg =0;
	public  int countMesg_Moredetails =0;
	
	public  int countMesg_External =0;
	public  int countMesg_MoredetailExternal =0;
	
	public  int countca=0;
	public  int countcared_BeforHosp=0;
	public  int countdeadca=0;
	public  int countlifeca=0;
	

	public   Duration_info(int _Range_S  , int _Range_E , int _MinNum )
	{
		Range_S=_Range_S;
		RangeE=_Range_E;
		MinNum=_MinNum;
		
//		if ( 	Range_S < 3600  )  hour="12"; 
//		if ( Range_S >=3600 &&	Range_S < 7200  )   hour="01"; 
//		if ( Range_S >=7200 &&	Range_S <10800  )  hour="02";
//		if ( Range_S >=10800 &&	Range_S < 14400  )  hour="03";
//		if ( Range_S >=14400&&	Range_S < 18000  )  hour="04";
//		if ( Range_S >=18000 && 	Range_S < 21600  )  hour="05";
//		if ( Range_S >=21600 && 	Range_S < 25200  )  hour="06";
//		if ( Range_S >=25200  && 	Range_S < 28800  )  hour="07";
//		
//		M=Mi ;
//		Rep=""+hour +":" + M+ ":00 AM";
		
		}		
	
		
	


}