package C_SimulationInput;

import com.vividsolutions.jts.geom.Coordinate;
import D_Ontology.Ontology.Allocation_Strategy;
import D_Ontology.Ontology.AmbulanceTracking;
import D_Ontology.Ontology.CasualtyReportandTrackingMechanism;
import D_Ontology.Ontology.CommunicationMechanism;
import D_Ontology.Ontology.GeolocationMechanism;
import D_Ontology.Ontology.SurveyScene_SiteExploration_Mechanism;
import D_Ontology.Ontology.TaskAllocationApproach;
import D_Ontology.Ontology.TaskAllocationMechanism;

public class InputFile {

	//-----------------------------------------------------
	// Details of Experiments
	public enum Exp{Exp1,Exp2,Exp3,Exp4,Exp5,Exp6,Exp7,Exp8,Exp9,Exp10,Exp11,Exp12,Exp13,Exp14,Exp15,Exp16,Exp17,Exp18,Exp19,Exp20,Exp21,Exp22,Exp23,Exp24 }
	public static final Exp  CurrentExp=Exp.Exp13; //from 15
	public static final int RunNO=10   ;//Repetition

	//-----------------------------------------------------
	public static  int PRIORITY_execution = 1 ;

	//-----------------------------------------------------
	//Setup
	public static final double EstimatedTimeExtrication_H=  60 * 10 ;
	public static final double EstimatedTimeExtrication_M=  60 * 7 ;
	public static final double EstimatedTimeExtrication_L=  60 * 3 ;
	public static final double EstimatedTimeWreckageTrafficPerMeter_H =  1 * 60 ;
	public static final double EstimatedTimeWreckageTrafficPerMeter_M =  .5 * 60 ;
	public static final double EstimatedTimeWreckageTrafficPerMeter_L = 0.25 * 60 ;

	public static final double EstimatedTimeSetup_Cordon=   ( 5)  ; // for EAP
	public static final double EstimatedTimeSetup_TA=   ( 10 )  ; //other type   
	public static final double EstimatedTimeSetup_sector=(7) ;	

	//----------------------------------------------------- 
	//EOC duration and Commander
	public static final double dispatch_EOC_duration = 120; //120s 2 m
	public static final double GetReport_EOC_duration = 120; //120s 2 m	
	public static final double AssessSituation_EOC_duration = 120; //120s 2 m
	public static final double GetReport_duration = 60; //120s 2 m  and

	//-----------------------------------------------------
	//Station duration
	public static final double PerparationforResponding_duration = 60 ;  // 120 s 2m

	//-----------------------------------------------------
	//Vehicle duration  or  rang 3 t0 1 m  in vhicle class
	public static final double perpration_duration_amb = 60 ; //2 m
	public static final double perpration_duration_fr =  120 ; // 3 m
	public static final double perpration_duration_pol =  10 ; // 1m

	public   static final double perpration_delay_amb=20 ;  // I used this temprory I like to put 1 menuts  btween vichels
	public   static final double perpration_delay_fr=30 ;
	public   static final double perpration_delay_pol=10 ;

	//-----------------------------------------------------
	//First responders ......Paramedic,Firefighter,policeman 
	public static final double Triage_duration = 45;   //45 s
	public static final double TriageEvery= 900 ; //   15 m
	public static final double Treatment_duration = 240;  //240 s 4m
	public static final double loadingToVehicle_duration = 60 ; //  2m
	public static final double DowenloadfromVehicle_duration =60 ; //   2m

	public static final double WalkingBeforSearch_duration = 4; // ----------------No need to discuss

	//writing
	public static final double CasualtyReportPaper_duration=10;  //20 s
	public static final double CasualtyReportElectronic_duration=5; //10 s	

	// new
	public static final double loginformation_duration = 60; // 60s  1m
	public static final double DirectCasualty_duration = 10;  

	//-----------------------------------------------------
	//Get message duration
	public static final double GetNotification_duration= 3;		// 5 s

	public static final double GetCommand_duration= 10;			//20 s 

	public static final double GetInfromation_Electronic_Duration=1; // 1s immediately
	public static final double GetInfromation_FtoForR_Duration_Data= 10;  // 30 s 
	public static final double GetInfromation_FtoForR_Duration_Notification= 3;  // 30 s 
	public static final double UploadingTime_Electronic = 50 ;

	public static final double EstimatedTimeinitiateSM=  10 ;  //intial 
	public static final double Boradcast_duration_FFcall= 5 ;
	//-----------------------------------------------------
	//Commander
	public static final double FormulatePlan_duration = 30 ;  //  at beginning 
	public static final double UdatePlan_duration= 3 ; 		// 5 s 
	public static final double Allocation_duration = 5 ; 		// 5 s

	public static final double Boradcast_duration= 45 ; //45 s   
	public static final double UpdatEOCEvery= 1000000000 ;  //  - m
	//public static final double SMEvery= 1200 ;  // 20 m
	//public static final double UpdatOPEvery=600 ;  // 10 m

	public static final double  Detroiration_Every = ( 30 * 60 ) ;
	public static final double  stabilization_R = ( 30 * 60 ) ;  //Assumption stabilization for some time  
	public static final double  stabilization_Y = ( 40 * 60 ) ;  //Assumption stabilization for some time  
	public static final double  stabilization_G = ( 50 * 60 ) ;  //Assumption stabilization for some time  

	//-----------------------------------------------------
	public static final double Papermap_duration= 60;  //2m 
	public static final double GISmap_duration= 30 ;    //1m 

	//-----------------------------------------------------   
	public static final int counterofaction=2500;
	//-----------------------------------------------------   

	//Communication
	public static final int RadioSystemchanleLimitation= 4  ;// 3 slots  1 amb  2 fire  3 police	4 chared
	public static final int FacetoFaceLimit= 3; // 3 meter   	
	//-----------------------------------------------------
	//Walking
	public static final double  step_long_casulaty=.25 ;  //casualty
	public static final double step_long_regularewalk=0.5 ;  //fr and amb responder
	public static final double step_long_ClearRoute=0.75 ; //Police responder
	//-----------------------------------------------------
	// Incident and  Casualty_location
	//new Coordinate (-1.5835959486843867,54.95925681014511),

	//new Coordinate (-1.5833303399361351,54.95940778028136),			
	//new Coordinate (-1.5831059506931264,54.95941318464246),

	//	public static Coordinate [] Casualty_location_GSM={ 
	//			new Coordinate (-1.583253873882093,54.9592219899684),
	//			new Coordinate (-1.5836293598231188,54.9594734026115),		
	//			new Coordinate (-1.5834817398327867,54.95956730600724),
	//			new Coordinate (-1.583375727827934,54.959307215708385),		
	//			new Coordinate (-1.5836046878347336,54.95945240214777),		
	//			new Coordinate (-1.5833515717792237,54.95947392249459),	
	//						
	//			new Coordinate (-1.583531808556974,54.95956176058076),
	//			new Coordinate (-1.5835694203624202,54.95947838466068),	
	//			new Coordinate (-1.5832939872223422,54.95944120468748),
	//			new Coordinate (-1.583047506084292,54.959366486698784),
	//			new Coordinate (-1.5829370742689453,54.95932761548049),
	//			new Coordinate (-1.5834013840604402,54.95930103271507),
	//			new Coordinate (-1.583425245555022,54.95947562646915),
	//			new Coordinate (-1.5831991702706028,54.95943007025909),
	//			new Coordinate (-1.5830846490946446,54.95960111206246),
	//			new Coordinate (-1.5834290568493432,54.95941986050115),		
	//			new Coordinate (-1.5830846490946446,54.95960111206246),
	//			new Coordinate (-1.5833623540828008,54.959482898261406),
	//			
	//			new Coordinate (-1.582995428773291,54.9594319007271),
	//			new Coordinate (-1.5831828329436803,54.959492983746934),
	//			new Coordinate (-1.5833767003444568,54.95953611261586),
	//			new Coordinate (-1.583600360067768,54.959412753178334),
	//			new Coordinate (-1.5833759127602876,54.95940937148868),
	//			new Coordinate (-1.5832626518035307,54.9593393808456),
	//			new Coordinate (-1.583102506584954,54.95959237150982),
	//			new Coordinate (-1.5834072324950996,54.959413150854154),
	//			new Coordinate (-1.5833210734047942,54.959332766211915),
	//			new Coordinate (-1.5831207410861712,54.959457515401475),
	//			new Coordinate (-1.5834467080339067,54.95930566574516),
	//			new Coordinate (-1.5831216635670056,54.95932698668339),
	//			new Coordinate (-1.5832661360253084,54.95926756554483),
	//			new Coordinate (-1.5834129111312178,54.959533145630594),
	//			new Coordinate (-1.5831850196059114,54.959335710071656),
	//			new Coordinate (-1.5835226068957797,54.95945080102309),
	//			new Coordinate (-1.583190326914232,54.959471150542655),
	//			new Coordinate (-1.583051656230084,54.95933859418126),
	//			new Coordinate (-1.583359095155363,54.95939179014029),
	//			new Coordinate (-1.583049334804548,54.95941319629833),
	//			new Coordinate (-1.5830307017768193,54.959480519898975),
	//			new Coordinate (-1.58309538715269,54.95933032946747),		
	//			new Coordinate (-1.5831922312719804,54.95964658495615),
	//			new Coordinate (-1.5830003348780652,54.95957213834876),
	//			new Coordinate (-1.5834018413579949,54.9595890067994),
	//			new Coordinate (-1.5836587682055183,54.959413275631675),
	//			new Coordinate (-1.5828567818203458,54.95925597410744),
	//			new Coordinate (-1.5829654902108266,54.959589383263584),
	//			new Coordinate (-1.582855916206481,54.95927966340088),
	//			new Coordinate (-1.5828323565841302,54.959300496878576),
	//			new Coordinate (-1.5834694068312785,54.959608349447244),			
	//			new Coordinate (-1.5832155015354383,54.95923868813086),			
	//	};			


	//			new Coordinate (-1.5836287671763303,54.95924869885375),
	//			new Coordinate (-1.5832006625703554,54.95947447518856),
	//			new Coordinate (-1.5829868216049194,54.95956048361546),
	//			new Coordinate (-1.5834203684482862,54.959512688733405),
	//			new Coordinate (-1.5830990033174612,54.959368032456915),
	//			new Coordinate (-1.5831608775193198,54.959435000929695),
	//			new Coordinate (-1.5833929420624357,54.959487507040045),
	//			new Coordinate (-1.5833759127602876,54.95940937148868),
	//			new Coordinate (-1.5828386686075737,54.95946514012825),	
	//			new Coordinate (-1.5828495481951455,54.95934736042488),
	//			new Coordinate (-1.5836224869154378,54.95947804369301),
	//			new Coordinate (-1.5836016099147823,54.959488436493515),
	//			new Coordinate (-1.5830846490946446,54.95960111206246),
	//			new Coordinate (-1.5833623540828008,54.959482898261406),

	public static Coordinate [] Casualty_location_GSM_bigerarea={ 

					
			
			new Coordinate (-1.583587594882982,54.95922402203061),
			new Coordinate (-1.5834891478572095,54.95925555850106),
			new Coordinate (-1.583636928682722,54.95948078259254),
			new Coordinate (-1.5836046878347336,54.95945240214777),
			new Coordinate (-1.5834691184856626,54.95925964172845),
			
			new Coordinate (-1.583594398135697,54.95941939877465),
			new Coordinate (-1.5833653648615285,54.959221006991726),
			new Coordinate (-1.5833860800774242,54.959234101279556),
			new Coordinate (-1.5830760659396304,54.95922845942451),
			new Coordinate (-1.5840794157379912,54.95933270746036),
			
			new Coordinate (-1.583425245555022,54.95947562646915),	
			new Coordinate (-1.5833667094046433,54.959269249438705),
			new Coordinate (-1.5835639619050983,54.95933702319035),
			new Coordinate (-1.5838344822445674,54.95931504431779),
			new Coordinate (-1.5840936880971952,54.959231704729014),
			new Coordinate (-1.5835212942400978,54.95948808782795),
			new Coordinate (-1.584342064371865,54.95920986503675),
			new Coordinate (-1.5842711684395496,54.95919803218735),
			new Coordinate (-1.5833863920924327,54.95925349192962),
			new Coordinate (-1.5832996624135922,54.959576309192286),
			new Coordinate (-1.583389376721863,54.95947135122011),
			new Coordinate (-1.5838851510389536,54.95934218499479),
			new Coordinate (-1.5834089927109243,54.959358509753486),
			new Coordinate (-1.5840801673939169,54.95912396768629),
			new Coordinate (-1.5834379775470753,54.95949924738857),
			new Coordinate (-1.5842392708950779,54.95918139325446),
			new Coordinate (-1.5836817511933592,54.95922501661279),
			new Coordinate (-1.583393600399972,54.95945123771861),
			new Coordinate (-1.5830500269495191,54.95939079762568),
			new Coordinate (-1.5841982947696953,54.95905271993308),
			new Coordinate (-1.5839531175978516,54.95935010654702),
			new Coordinate (-1.5835540343040677,54.959316340423015),
			new Coordinate (-1.583924342548859,54.95928400310333),
			new Coordinate (-1.5835513428897572,54.959300491235624),
			new Coordinate (-1.583253873882093,54.9592219899684),
			new Coordinate (-1.5836293598231188,54.9594734026115),
			new Coordinate (-1.5831822427049331,54.95943730143933),
			new Coordinate (-1.584119844810002,54.95911407166611),
			new Coordinate (-1.5830778870944073,54.959442622615576),
			new Coordinate (-1.5834951722910207,54.95932163680162),
			new Coordinate (-1.5839356715835136,54.959315080405126),
			new Coordinate (-1.5835955455045665,54.95948060468547),
			new Coordinate (-1.5837976802416383,54.9593853604817),
			new Coordinate (-1.5836814782563582,54.95945730405219),
			new Coordinate (-1.5831605273319975,54.95959411285107),
			new Coordinate (-1.5835591897876233,54.95927963227623),
			new Coordinate (-1.5839942466697094,54.95918797556801),
			new Coordinate (-1.5842114572593988,54.95916175860823),
			new Coordinate (-1.5837145332322187,54.959454746649406),
			new Coordinate (-1.5829627235480555,54.95952046214538),
			new Coordinate (-1.5835597335600344,54.959281160670514),
			new Coordinate (-1.5836435750164746,54.95924045626553),
			new Coordinate (-1.5830272272766925,54.95953190980116),
			new Coordinate (-1.583865819011889,54.959412816553176),
			new Coordinate (-1.583454349543672,54.95923158993502),
			new Coordinate (-1.5840946725305296,54.959280822527546),
			new Coordinate (-1.5831390172384248,54.95936788310781),
			new Coordinate (-1.5839238927667056,54.959299795583036),
			new Coordinate (-1.5836085984117885,54.95946742117737),
			new Coordinate (-1.583772806584983,54.95931630093099),
			new Coordinate (-1.5832769417125252,54.95928081524084),
			new Coordinate (-1.5839966209989331,54.959214339688614),
			new Coordinate (-1.5831146012514234,54.95954776428159),
			new Coordinate (-1.583400023000939,54.9593355506539),
			new Coordinate (-1.583375727827934,54.959307215708385),
			new Coordinate (-1.5833515717792237,54.95947392249459),
			new Coordinate (-1.5832963900515504,54.95960024895009),
			new Coordinate (-1.5829718858585198,54.95936772079299),
			new Coordinate (-1.5831203904191953,54.959276134944105),
			new Coordinate (-1.583566943233277,54.95930555972469),		
			
			
			new Coordinate (-1.584297469232997,54.95921247706929),

	}	;

//3,3,3,3,3,     3,3,3,3,3,    4,4,4,4,4,
	//3,3,3,3,3,     4,4,4,4,4,    3,3,4,4,4,	
	public static int [] intialRPM={ 
			4,4,4,4,4,     3,3,3,3,3,    4,4,4,3,3,	
			8,8,8,8,8,     7,7,7,7,7, 	 6,6,6,6,6,
			9,9,9,9,9     ,10,10,10,10,10,  10,10,10,10,10,  11,11,11,11,11,  11,11,11,11,11,    12,12,12,12,12  
	};

	public static int [] CaualtyNum_Trapped={0, 1, 2, 3 ,4 ,5,6,7,8,9} ;    //TrappedCasualty
	//public static int [] CaualtyNum_Trapped={ 1, 5, 8} ;    //TrappedCasualty

	//-----------------------------------------------------   	//not used  begin
	// Incident and  Casualty_location	

	public static Coordinate [] Casualty_location_GSM1={ 
			new Coordinate (-1.5828495481951455,54.95934736042488),
			new Coordinate (-1.5834817398327867,54.95956730600724),
			new Coordinate (-1.5835694203624202,54.95947838466068),
			new Coordinate (-1.5836046878347336,54.95945240214777),
			new Coordinate (-1.583531808556974,54.95956176058076),
			new Coordinate (-1.5832939872223422,54.95944120468748),
			new Coordinate (-1.583047506084292,54.959366486698784),
			new Coordinate (-1.5829370742689453,54.95932761548049),
			new Coordinate (-1.5834013840604402,54.95930103271507),
			new Coordinate (-1.583425245555022,54.95947562646915),
			new Coordinate (-1.5831991702706028,54.95943007025909),
			new Coordinate (-1.5836016099147823,54.959488436493515),
			new Coordinate (-1.5830846490946446,54.95960111206246),
			new Coordinate (-1.5834290568493432,54.95941986050115),
			new Coordinate (-1.5836224869154378,54.95947804369301),
			new Coordinate (-1.5836016099147823,54.959488436493515),
			new Coordinate (-1.5830846490946446,54.95960111206246),
			new Coordinate (-1.5833623540828008,54.959482898261406),
			new Coordinate (-1.583253873882093,54.9592219899684),	
			new Coordinate (-1.582995428773291,54.9594319007271),
			new Coordinate (-1.5831828329436803,54.959492983746934),
			new Coordinate (-1.5833767003444568,54.95953611261586),
			new Coordinate (-1.583600360067768,54.959412753178334),
			new Coordinate (-1.5833778597534212,54.9592656871848),
			new Coordinate (-1.5833759127602876,54.95940937148868),
			new Coordinate (-1.5832626518035307,54.9593393808456),
			new Coordinate (-1.583102506584954,54.95959237150982),
			new Coordinate (-1.5834072324950996,54.959413150854154),
			new Coordinate (-1.5833210734047942,54.959332766211915),
			new Coordinate (-1.5831207410861712,54.959457515401475),
			new Coordinate (-1.5834467080339067,54.95930566574516),
			new Coordinate (-1.5831216635670056,54.95932698668339),
			new Coordinate (-1.5832661360253084,54.95926756554483),
			new Coordinate (-1.5834129111312178,54.959533145630594),
			new Coordinate (-1.5831850196059114,54.959335710071656),
			new Coordinate (-1.5835226068957797,54.95945080102309),
			new Coordinate (-1.583190326914232,54.959471150542655),
			new Coordinate (-1.583051656230084,54.95933859418126),
			new Coordinate (-1.583359095155363,54.95939179014029),
			new Coordinate (-1.583049334804548,54.95941319629833),
			new Coordinate (-1.5830307017768193,54.959480519898975),
			new Coordinate (-1.58309538715269,54.95933032946747),
			new Coordinate (-1.583382471036079,54.95922633507368),
			new Coordinate (-1.5832155015354383,54.95923868813086),
			new Coordinate (-1.5833303399361351,54.95940778028136),			
			new Coordinate (-1.5831059506931264,54.95941318464246),
			new Coordinate (-1.5833515717792237,54.95947392249459),	
			new Coordinate (-1.583375727827934,54.959307215708385),
			new Coordinate (-1.5828768129273245,54.959399005842926),
			new Coordinate (-1.5828863623254434,54.95930595395138),
			new Coordinate (-1.5830496111062118,54.95923238899219),
			new Coordinate (-1.5833813520320026,54.95922133049603),			
			new Coordinate (-1.5829588053137758,54.959360918628505),
			new Coordinate (-1.583278996605404,54.9594913193797),
			new Coordinate (-1.5829972560616623,54.959398888413055),
			new Coordinate (-1.5832599627751163,54.9592626478284),
			new Coordinate (-1.5832632396978885,54.959459796163536),
			new Coordinate (-1.582976815285664,54.95924610688693),
			new Coordinate (-1.5834827279203447,54.95928757370359),
			new Coordinate (-1.5835655588201476,54.9594054980819),
			new Coordinate (-1.5833929420624357,54.959487507040045),
			new Coordinate (-1.5833195335040935,54.95940122782156),
			new Coordinate (-1.5835430802318882,54.959320153082274),
			new Coordinate (-1.5832006625703554,54.95947447518856),
			new Coordinate (-1.5829868216049194,54.95956048361546),
			new Coordinate (-1.5834203684482862,54.959512688733405),
			new Coordinate (-1.5830990033174612,54.959368032456915),
			new Coordinate (-1.5831608775193198,54.959435000929695),
			new Coordinate (-1.5833778597534212,54.9592656871848),
			new Coordinate (-1.5833759127602876,54.95940937148868),
			new Coordinate (-1.583137703283792,54.95927048858292),
			new Coordinate (-1.5829843170640625,54.9593480630857),
			new Coordinate (-1.5832617956089317,54.95956440571225),
			new Coordinate (-1.5833904078805006,54.959210477559786),
			new Coordinate (-1.5828942811444804,54.95943931339623),
			new Coordinate (-1.5831984008217845,54.95926271934444),
			new Coordinate (-1.5834642224461473,54.95947917180936),
			new Coordinate (-1.5832766462849985,54.9595106179948),
			new Coordinate (-1.5829638282780087,54.959380488618116),
			new Coordinate (-1.5829912527321612,54.95929673505702),
			new Coordinate (-1.5832011241390622,54.95929410016082),
			new Coordinate (-1.5832400140170302,54.959319102166795),
			new Coordinate (-1.5835138492203977,54.95935532081154),
			new Coordinate (-1.5834065540265363,54.95946304386407),
			new Coordinate (-1.5833858596411576,54.959354084428924),
			new Coordinate (-1.5831296174674492,54.959354883135724),
			new Coordinate (-1.583494818528225,54.959299467051004),
			new Coordinate (-1.5833336527518729,54.9590413587273),
			new Coordinate (-1.583250223588424,54.95955820313135),
			new Coordinate (-1.5831963773024518,54.959497737912315),
			new Coordinate (-1.5830277121887157,54.95935534023766),
			new Coordinate (-1.5833000626023375,54.95945734832129),
			new Coordinate (-1.5833503174518773,54.95927639781032),
			new Coordinate (-1.583398553786299,54.959545712277965),
			new Coordinate (-1.5833005581074755,54.95922005444506),	
			new Coordinate (-1.5831179879870307,54.95935761428959),
			new Coordinate (-1.5830527388282278,54.95943682035958),
			new Coordinate (-1.5833012428637279,54.95936354606633),
			new Coordinate (-1.5831905591669573,54.95949517294229),
			new Coordinate (-1.5833370968543639,54.95936655848404),
			new Coordinate (-1.583304354317974,54.95931631796473),
			new Coordinate (-1.5833210734047942,54.959332766211915),
			new Coordinate (-1.5831207410861712,54.959457515401475),
			new Coordinate (-1.5834467080339067,54.95930566574516),
			new Coordinate (-1.5831216635670056,54.95932698668339),
			new Coordinate (-1.5832661360253084,54.95926756554483),			
			new Coordinate (-1.5832988744438132,54.95926831522667),
			new Coordinate (-1.5831883399935447,54.95953624167175),
			new Coordinate (-1.5834892384934514,54.95944553419026),
			new Coordinate (-1.583457392587273,54.959481545007876),
			new Coordinate (-1.5830708995952238,54.959276553586975),
			new Coordinate (-1.5833647729969729,54.959232043634636),
			new Coordinate (-1.583367785384362,54.95923071518782),
			new Coordinate (-1.583408878862058,54.95922434169611),
			new Coordinate (-1.5831223896650872,54.95935969539856),	
			new Coordinate (-1.5835441612891012,54.95940765123054),
			new Coordinate (-1.5830135439788824,54.959590565907966),
			new Coordinate (-1.5833657422857554,54.9595958066397),
			new Coordinate (-1.5835735740998758,54.959278290138265),
			new Coordinate (-1.5835963199194565,54.95931796964939),
			new Coordinate (-1.5828495481951455,54.95934736042488),
			new Coordinate (-1.5834817398327867,54.95956730600724),
			new Coordinate (-1.5835694203624202,54.95947838466068),
			new Coordinate (-1.5836046878347336,54.95945240214777),
			new Coordinate (-1.583531808556974,54.95956176058076),
			new Coordinate (-1.583259535708292,54.959606999741105),
			new Coordinate (-1.582917696790888,54.959217684492614),
			new Coordinate (-1.5836076021622274,54.95931690202302),
			new Coordinate (-1.5833011650527327,54.95960384499488),
			new Coordinate (-1.5828955957513686,54.95923722220897),
			new Coordinate (-1.5829505665387251,54.95957786970799),
			new Coordinate (-1.5836224869154378,54.95947804369301),
			new Coordinate (-1.5836016099147823,54.959488436493515),
			new Coordinate (-1.5830846490946446,54.95960111206246),
			new Coordinate (-1.5836216336612499,54.95942943250307),
			new Coordinate (-1.583555452798775,54.959521482469505)};

	public static Coordinate [] Casualty_location_NCS1={ 	
			new Coordinate (-1.6148465310763245,54.96968936725039),
			new Coordinate (-1.6150028738883393,54.969550807339246),
			new Coordinate (-1.6150655283856397,54.96946503125283),
			new Coordinate (-1.6146347541378334,54.969779437697895),
			new Coordinate (-1.6148568820152547,54.96974859528893),
			new Coordinate (-1.6146771920222704,54.96954251011147),
			new Coordinate (-1.6145583108793566,54.969540066283145),
			new Coordinate (-1.614793827512906,54.96951927377316),
			new Coordinate (-1.6146765066687854,54.969682749214506),
			new Coordinate (-1.6149019050780589,54.96969464576018),
			new Coordinate (-1.614587831128982,54.96955360974161),
			new Coordinate (-1.6148832729591465,54.96970024442892),
			new Coordinate (-1.6150396466960624,54.96959346931478),
			new Coordinate (-1.6149509947496623,54.96956221000317),
			new Coordinate (-1.6149529415676274,54.96975633301612),
			new Coordinate (-1.615122158448673,54.969492153874235),
			new Coordinate (-1.6150794158978712,54.96974027035491),
			new Coordinate (-1.6149322448114154,54.969665243522925),
			new Coordinate (-1.6149422142466865,54.9696611805193),
			new Coordinate (-1.6147358862164325,54.969638831379775),
			new Coordinate (-1.6146589814751315,54.969666013758236),
			new Coordinate (-1.6151384667377886,54.96956633526037),
			new Coordinate (-1.6148597594438718,54.96968165975845),
			new Coordinate (-1.6148460962166522,54.96954458422295),
			new Coordinate (-1.6147334078302158,54.96961599687117),
			new Coordinate (-1.6149043892406985,54.9696752028336),
			new Coordinate (-1.6147950651942233,54.969569751004734),
			new Coordinate (-1.6150962035291014,54.96954863673215),
			new Coordinate (-1.6148627949563188,54.969482084072474),
			new Coordinate (-1.6145308899202546,54.969527670166244),
			new Coordinate (-1.6146462654382034,54.969530044145095),
			new Coordinate (-1.6150743186897059,54.96964286828069),
			new Coordinate (-1.6148992701550642,54.96958767983911),
			new Coordinate (-1.6149248394955977,54.96965238429658),
			new Coordinate (-1.6148979851278296,54.96959378356263),
			new Coordinate (-1.6148516124734607,54.96978282068366),
			new Coordinate (-1.6148380209421376,54.96953284973674),
			new Coordinate (-1.6148640337013955,54.9694879667852),
			new Coordinate (-1.6148468334503048,54.96942025454763),
			new Coordinate (-1.6148974693325646,54.969501256288815)};

	public static Coordinate [] Casualty_location_GSS1={ 

			new Coordinate (-1.589157857779768,54.95793820613669),
			new Coordinate (-1.5891128524141114,54.95791894952329),
			new Coordinate (-1.589439711903307,54.957886016234355),
			new Coordinate (-1.5894178120660944,54.957873394498364),
			new Coordinate (-1.5891486796023229,54.95780869503462),
			new Coordinate (-1.5890124281749674,54.957800761001245),
			new Coordinate (-1.5894424048704778,54.95780667921792),
			new Coordinate (-1.5891572801390703,54.95798675936051),
			new Coordinate (-1.5894371350347911,54.95793942543583),
			new Coordinate (-1.5890801936667918,54.95781771773429),
			new Coordinate (-1.589329993978727,54.957884278209704),
			new Coordinate (-1.5893378444780673,54.95780962648567),
			new Coordinate (-1.5892625124341988,54.95802050458905),
			new Coordinate (-1.5893172683018084,54.95796758330147),
			new Coordinate (-1.5893271213402647,54.95790795586523),
			new Coordinate (-1.589383471392812,54.9578115512078),
			new Coordinate (-1.589158607300798,54.957755325095555),
			new Coordinate (-1.5893639346913795,54.95786211178714),
			new Coordinate (-1.5893834703834635,54.95789150254117),
			new Coordinate (-1.5893841778611877,54.95793372659587),
			new Coordinate (-1.5893453528510524,54.95787403919397),
			new Coordinate (-1.589071140195498,54.95775201596077),
			new Coordinate (-1.5889840407345326,54.95781930995646),
			new Coordinate (-1.5892687200517914,54.95800741711889),
			new Coordinate (-1.5894428219032908,54.957815993742585),
			new Coordinate (-1.589164884007332,54.95790713421755),
			new Coordinate (-1.589110303406815,54.9577369361505),
			new Coordinate (-1.5893966797492474,54.95792303968559),
			new Coordinate (-1.5892104096329371,54.9578851615541),
			new Coordinate (-1.5891314731605253,54.95769697884078),
			new Coordinate (-1.5892331321965059,54.95773404346162),
			new Coordinate (-1.5890520583314462,54.95775634781908),
			new Coordinate (-1.5889137109368707,54.95784099633336),
			new Coordinate (-1.5892543769097605,54.95795058838809),
			new Coordinate (-1.589259363046404,54.95797289509619),
			new Coordinate (-1.589246208933658,54.958005405953934),
			new Coordinate (-1.5891266084838023,54.957956536067414),
			new Coordinate (-1.5895224879506267,54.95791360714037),
			new Coordinate (-1.5891069781645157,54.95775161927422),
			new Coordinate (-1.5892342130184731,54.95797514360873)};


	public static int [] intialRPM2x={ 
			3,2,4,1,3,
			5,6,7,8,5,
			9,10,11,12,9,10,

			4,3,
			6,8,
			9,10,11,12,9,10,

			4,3,
			5,8,
			9,10,11,12,

			3,
			7,
			9,10,11,12};

	public static int [] intialRPM1x={ 3,4,2,4,1,3,4,3,3,4,
			5,6,7,8,5,6,7,8,5,6,
			9,10,11,12,9,10,11,12,9,10,11,12,
			9,10,11,12,9,10,11,12,9};
	


	public static Coordinate [] Casualty_location_NCS={ 
			
			
			
			
			
			
			new Coordinate (-1.615079538342752,54.96960059656991),																			
			new Coordinate (-1.6149299739271843,54.9696156068744),						
			new Coordinate (-1.6151524330497624,54.96972529008506),				
			new Coordinate (-1.6149688799243669,54.96945978761635),				
			new Coordinate (-1.614673966923941,54.969507862745296),
				
					
			
			new Coordinate (-1.6149609110830114,54.96945556340677),
			new Coordinate (-1.6148427443655822,54.969536624975454),
			new Coordinate (-1.6147691271327735,54.969564228016765),
			new Coordinate (-1.6148363495073377,54.969563735025034),
			new Coordinate (-1.6148394417142633,54.96956059250608),


			new Coordinate (-1.614671036406318,54.9697159219302),
			new Coordinate (-1.6144994405666604,54.969584668127816),
			new Coordinate (-1.6145901622168357,54.96959639155962),
			new Coordinate (-1.6147351878619722,54.96958649375129),
			new Coordinate (-1.614734326813642,54.96957612577401),
			new Coordinate (-1.614720021895675,54.96962742645528),
			new Coordinate (-1.6147630399370738,54.9696031727067),
			new Coordinate (-1.6146899379595696,54.9695443909757),


			new Coordinate (-1.6147586077256793,54.96970429126852),
			new Coordinate (-1.6149340198433777,54.96970128651464),
			new Coordinate (-1.6149785795829856,54.96973444219333),
			new Coordinate (-1.614868529528997,54.969779429484674),
			new Coordinate (-1.614676909673154,54.96974077322286),
			new Coordinate (-1.6147977959676223,54.9697947209511),
			
			new Coordinate (-1.6150008377152085,54.96954742806693),
			new Coordinate (-1.614983824845902,54.9696529938448),
			new Coordinate (-1.6151044555744996,54.9696929046549),
			new Coordinate (-1.6150590485116958,54.96963218861352),
			new Coordinate (-1.6149831488070863,54.96970056619334),
			
			new Coordinate (-1.6151260049032792,54.969611579773755),
			new Coordinate (-1.6149749053641402,54.96966393522349),	
			new Coordinate (-1.6149998850385456,54.96959836871606),
			new Coordinate (-1.6151759894430222,54.96965088425921),
			new Coordinate (-1.615042344518415,54.96965051467858),
			new Coordinate (-1.6151780379441993,54.969585878676135),
			
			new Coordinate (-1.6149280170045788,54.96966702145039),	
			new Coordinate (-1.6150587432041685,54.969633683099445),
			new Coordinate (-1.6148928024479916,54.96957607255233),
			new Coordinate (-1.6147191336840065,54.96944225487476),
			new Coordinate (-1.6148138950819046,54.9694436594151),
			
			
			
	
	
	};


	public static Coordinate [] Casualty_location_GSS={ 


			//			new Coordinate (-1.589584225576616,54.95793886375351),
			//			new Coordinate (-1.589423284117967,54.95791153098268),
			//			new Coordinate (-1.5895359519704824,54.95791105155192),
			//			new Coordinate (-1.5894675256574113,54.95781882321351),
			//			new Coordinate (-1.5893758895164842,54.95784516745556),
			//			new Coordinate (-1.5893763274593886,54.95784475487823),
			//			new Coordinate (-1.589428576058127,54.957833757346755),
			//			new Coordinate (-1.5894206163087312,54.95787688408895),
			new Coordinate (-1.5895252625029004,54.95794489046957),
			new Coordinate (-1.5895919448450362,54.95790733562551),
			new Coordinate (-1.5894254622055055,54.95787206618903),
			new Coordinate (-1.5894346394350753,54.95784309643883),
			new Coordinate (-1.5894565948504809,54.95790488330585),
			new Coordinate (-1.5893841305432914,54.957885502902),
			new Coordinate (-1.5895144444110505,54.95779645609669),
			new Coordinate (-1.5895474481463638,54.95780451216088),



			new Coordinate (-1.5892770018173705,54.957785758222634),
			new Coordinate (-1.5892315320765138,54.957778373039964),
			new Coordinate (-1.5893710543393458,54.95777211502659),
			new Coordinate (-1.5890437340924344,54.957700802028434),
			new Coordinate (-1.5892240086972829,54.95779284647672),
			new Coordinate (-1.5894022641741694,54.9577546156155),
			new Coordinate (-1.5893001299520013,54.95773281826857),
			new Coordinate (-1.5893777988164692,54.95773421796726),
			new Coordinate (-1.589375978437076,54.95777050156254),
			new Coordinate (-1.5891893779488049,54.957709164952895),


			new Coordinate (-1.589013472933334,54.95774316669025),
			new Coordinate (-1.589014392704034,54.95784601733127),
			new Coordinate (-1.589104547115284,54.95787055761674),
			new Coordinate (-1.5890989629294865,54.95793696437292),
			new Coordinate (-1.5890916730689926,54.95794420978008),
			new Coordinate (-1.5889794518467741,54.95781784458414),
			new Coordinate (-1.5890568570293075,54.957801179249984),
			new Coordinate (-1.58893465961044,54.957816551820336),

			new Coordinate (-1.5892432319792327,54.95793426423333),
			new Coordinate (-1.5892462542977064,54.957903265652604),
			new Coordinate (-1.5892826558689637,54.95789858295877),
			new Coordinate (-1.5893137436478602,54.95790102327959),
			new Coordinate (-1.589262279890821,54.95796689817302),
			new Coordinate (-1.5893337706925372,54.95799371050222),};

	//	//-----------------------------------------------------   	//not used end
	//		//EOC duration and Commander
	//		public static final double dispatch_EOC_duration = 4; 
	//		public static final double GetReport_EOC_duration = 4; 
	//		public static final double AssessSituation_EOC_duration = 4; 
	//	
	//		//Station duration
	//		public static final double PerparationforResponding_duration = 1;  
	//	
	//		//-----------------------------------------------------
	//		//Vehicle duration
	//		public static final double perpration_duration_amb =  70 ;
	//		public static final double perpration_duration_fr =  12 ;
	//		public static final double perpration_duration_pol =  5 ;
	//	
	//		public  static final double perpration_delay_amb=3 ;  // I used this temprory I like to put 1 menuts  btween vichels
	//		public  static final double perpration_delay_fr=2 ;
	//		public  static final double perpration_delay_pol=4 ;
	//		//-----------------------------------------------------
	//		//Paramedic 
	//		public static final double Triage_duration = 4;   
	//		public static final double Treatment_duration = 5;  
	//		public static final double loadingToVehicle_duration = 2; 
	//		public static final double DowenloadfromVehicle_duration = 2 ; 
	//	
	//		public static final double WalkingBeforSearch_duration = 10; 
	//	
	//		public static final double CasualtyReportPaper_duration=2;  
	//		public static final double CasualtyReportElectronic_duration=1; 
	//		public static final double UploadingTime_Electronic = 8 ;
	//	
	//		public static final double loginformation_duration = 3 ;
	//		public static final double DirectCasualty_duration = 3 ;
	//	
	//		//-----------------------------------------------------
	//		//Get message duration
	//		public static final double GetNotification_duration=2;		
	//		public static final double GetReport_duration = 4; 
	//		public static final double GetCommand_duration=3;			
	//	
	//		public static final double GetInfromation_Electronic_Duration=1; 
	//		//public static final double GetInfromation_FtoForR_Duration= 3;
	//		public static final double GetInfromation_FtoForR_Duration_Data= 4;   
	//		public static final double GetInfromation_FtoForR_Duration_Notification=2;
	//		public static final double EstimatedTimeinitiateSM= 20 ;
	//		public static final double Boradcast_duration_FFcall= 5 ;
	//	
	//		//-----------------------------------------------------
	//		//Commander
	//		public static final double FormulatePlan_duration = 2; 
	//		public static final double UdatePlan_duration=2; 
	//		public static final double Allocation_duration = 2; 
	//	
	//		public static final double UpdatEOCEvery=555500 ;
	//		//public static final double SMEvery=500 ;
	//		//public static final double UpdatOPEvery=200 ;
	//		public static final double TriageEvery=120 ; 
	//		public static final double Boradcast_duration=2; 
	//	
	//		
	//	
	//		public static final double EstimatedTimeExtrication_H=   15 ;
	//		public static final double EstimatedTimeExtrication_M=   10 ;
	//		public static final double EstimatedTimeExtrication_L=   5 ;
	//		public static final double EstimatedTimeWreckageTrafficPerMeter_H =  5 ;
	//		public static final double EstimatedTimeWreckageTrafficPerMeter_M =  3  ;
	//		public static final double EstimatedTimeWreckageTrafficPerMeter_L =  2  ;
	//	
	//		public static final double EstimatedTimeSetup_Cordon=   1 ; // for EAP
	//		public static final double EstimatedTimeSetup_TA=   3  ; //other type   
	//		public static final double EstimatedTimeSetup_sector= 1 ;	
	//	
	//	
	//		public static final double  Detroiration_Every =  500 ;
	//		public static final double  stabilization_R = (300 ) ;  //Assumption stabilization for some time  
	//		public static final double  stabilization_Y = ( 400) ;  //Assumption stabilization for some time  
	//		public static final double  stabilization_G = ( 500 ) ;  //Assumption stabilization for some time  
	//	
	//	
	//		public static final double Papermap_duration= 20;  
	//		public static final double GISmap_duration= 10;    
	//		public static final int counterofaction=500;


}




//public static final  Allocation_Strategy AllocationPramedicforCasuslty_Strategy_Triage= Allocation_Strategy.FIFO; //First seen 
//public static final  Allocation_Strategy AllocationPramedicforCasuslty_Strategy_Treatment= Allocation_Strategy.Severity_RYGPriorty;
//public static final  Allocation_Strategy AllocationAmbulanceforCasuslty_Strategy= Allocation_Strategy.Severity_RYGPriorty;
//public static final  Allocation_Strategy AllocationHospitalforCasuslty_Strategy= Allocation_Strategy.Severity_RYGPriorty;
//public static final  Allocation_Strategy SelectionHospital_Strategy1= Allocation_Strategy.FIFO;  // spesfic
//public static final CommunicationMechanism  ComMechanism_level_OtoRes=CommunicationMechanism.FaceToFace;  // Between bronze commander to its responders
//public static final CommunicationMechanism  ComMechanism_level_TtoO=CommunicationMechanism.RadioSystem ; // Between silver commander to its responders
//public static final CommunicationMechanism  ComMechanism_ControlArea=CommunicationMechanism.FaceToFace;  // Role Assignment 
//public static final CommunicationMechanism  ComMechanism_level_TtoEOC=CommunicationMechanism.Phone ;  // AOC
//public static final CommunicationMechanism  ComMechanism_level_DrivertoALC=CommunicationMechanism.Phone ;
