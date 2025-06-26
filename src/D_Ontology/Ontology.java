package D_Ontology;

import C_SimulationOutput.BehRec;
import D_Ontology.Ontology.RespondersTriggers;

//ontology
public class Ontology {

	public static int[][] DelphiDHC={
			{0,0,0,0,0,0,0,0,0,0,0,0 },
			{0,0,0,0,0,0,0,0,0,0,0,0 },
			{0,0,0,0,0,0,0,0,0,0,0,0 },
			{1,0,0,0,0,0,0,0,0,0,0,0 },
			{2,1,0,0,0,0,0,0,0,0,0,0 },
			{3,2,1,0,0,0,0,0,0,0,0,0 },
			{4,3,2,1,0,0,0,0,0,0,0,0 },
			{6,5,4,3,2,1,0,0,0,0,0,0 },
			{8,7,6,5,4,3,2,1,0,0,0,0 },
			{9,8,8,7,6,5,4,3,2,1,0,0 },
			{10 ,9,9,8,8,7,6,6,5,5,4,4},
			{11,11,10,10,9,8,8,7,7,6,6,5},
			{12,12,11,11,10,10,10,10,9,9,8,8}};

	public static double[][] SurvivalProbability={
			{0.052, 180},
			{0.089, 170},
			{0.15,160},
			{0.23,150},
			{0.35,140},
			{0.49 ,130},
			{0.63,120},
			{0.75,110},
			{0.84, 90},
			{0.90,60},
			{0.94, 50},
			{0.97,40},
			{0.98, 30}};

	//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

	//EOC
	public enum EOCAction{Idle,Noaction,DispatchingtoStation,AsessSituationofInciedent, GetReport}
	public enum EOCTriggers{Get999call ,delayinwork ,DoneActivity ,EndingAction,EndER ,GetFANotfication ,GeEndNotfication , GetFAReport ,GetReport }

	public enum Callinformatiom{NoInformation,ThereisInformation }
	//public enum DispatchTriggers{PublicCall_NoInformation ,PublicCall_ThereisInformation ,FirstArrivalReport,SituationReport } //Triggers

	//---------------------------------------------------------------------------------
	//Station
	public enum StationAction{Idle,Noaction,AllocateResources,DispatchingResources,done}
	public enum StationTriggers{GetDispatchingcommand ,delayinwork ,DoneActivity ,EndingAction }

	//---------------------------------------------------------------------------------
	//Vehicle
	public enum VehicleAction{Idle,WaitingRequest  ,Perprating ,TravelingtoIncident,TravelingtoHospital,TravelingtoBaseStation,done ,ColocationBLF , GotoSetupLA  }
	public enum VehicleTriggers{Alarm999 , Arrivedlocation ,EndingAction , InsertKey_incident ,InsertKey_BLF ,InsertKey_LAsetup ,InsertKey_LAleave,InsertKey_LABack,InsertKey_Hospital  ,InsertKey_IncidentLeave,InsertKey_Station }

	//---------------------------------------------------------------------------------
	// Casualty
	public enum CasualtyLife{Dead,Life } //update by PRM  //PreTreated ,
	public enum CasualtyStatus{Trapped,NonTrapped,PreTreatedTrapped, Extracted,Triaged,BLSTreated ,HospitalTreated ,ConfirmedDead , ConfirmedUninjuriedSurvivor }  // After Action , ConfirmedHospital 
	
	public enum CasualtyAction	{NoResponse, WaitingPre_Treatment, Pre_Treatment ,Extract,
		                                     FieldTriage,                                 
		                                     DirectToRCorCCS ,                                      
		                         			               WaitingTransferDelay ,
							                                                     TransferToCCS ,WaitingSecondTriage ,SecondTriage, WaitingTreatment,Treatment ,
							                                                     TransferToRC, WaitinginRC,
							                              Collectinformation,   
							                                                     WaitingTransfertoV ,TransferTovichel,LoadinginAmbulance,Travallingtohospital,DownloadfromAmbulance   ,
							               
							               onRC ,
							               DeceasedonScene ,
							               DeceasedonCCS,
							               DeceasedonHospital ,
							               OnHospital}  // TransferToSafe , TransferToCCS ,GuidedToSafe ,  GuidedToCCS ,WaitinginSafe
	
	public enum CasualtyinfromationType {None, Uninjured,Injured ,Evacuated_KnowHosp,Deceased , SurvivorinRC  , Evacuated_NotKnowHosp  } //x,

	//order based on print 
	public static int ListTotal_Casualty= 25 ;
	public static CasualtyAction [] CasualtyAction_list_new={CasualtyAction.NoResponse,CasualtyAction.WaitingPre_Treatment,CasualtyAction.Pre_Treatment , CasualtyAction.Extract ,
			CasualtyAction.FieldTriage,
			CasualtyAction.DirectToRCorCCS , 
			CasualtyAction.WaitingTransferDelay, 
			CasualtyAction.TransferToCCS ,CasualtyAction.WaitingSecondTriage  ,CasualtyAction.SecondTriage,CasualtyAction.WaitingTreatment, CasualtyAction.Treatment,
			CasualtyAction.TransferToRC ,CasualtyAction.WaitinginRC ,
			CasualtyAction.Collectinformation , 
			CasualtyAction.WaitingTransfertoV, CasualtyAction.TransferTovichel, CasualtyAction.LoadinginAmbulance , CasualtyAction.Travallingtohospital, CasualtyAction.DownloadfromAmbulance,
			
			CasualtyAction.onRC,
			CasualtyAction.DeceasedonScene ,
			CasualtyAction.DeceasedonCCS ,
			CasualtyAction.DeceasedonHospital,
			CasualtyAction.OnHospital 
	};  //24
	
	

	public static CasualtyAction [] CasualtyAction_list_old={CasualtyAction.NoResponse,CasualtyAction.FieldTriage,CasualtyAction.WaitingTreatment,
			CasualtyAction.Treatment,CasualtyAction.WaitingTransfertoV, CasualtyAction.TransferTovichel, CasualtyAction.LoadinginAmbulance , 
			CasualtyAction.Travallingtohospital, CasualtyAction.DownloadfromAmbulance,CasualtyAction.OnHospital };  //10

	
	//---------------------------------------------------------------------------------
	//Roles used 

	public enum Ambulance_ResponderRole { None , Paramedic, Driver,AmbulanceIncidentCommander , AmbulanceCommunicationsOfficer , AmbulanceSectorCommander,AmbulanceLoadingCommander,CasualtyClearingOfficer}//Firstcrewonscene,ParkingOfficer ,TriageOfficer ,CasualtyClearingOfficer
	public enum Police_ResponderRole { None , Policeman, Driver,PoliceIncidentCommander , PoliceCommunicationsOfficer , CordonsCommander , ReceptionCenterOfficer}
	public enum Fire_ResponderRole { None , FireFighter, Driver,FireIncidentCommander , FireCommunicationsOfficer , FireSectorCommander}

	//---------------------------------------------------------------------------------	
	// Responder
	public enum RespondersActions{

		Idle, Noaction,OnVehicle, SearchPramdics ,
		TransferCasualtytoLA ,
		NotifyArrival,NotifyLeave, 
		CommandParamedicTriageorPre_treatment,
		GetcammandRole, GetcammandtoHandover ,GetHandoverReport,	GetcammandTriage,GetcammandTreatment,GetcammandTransfer,GetAssignedTask ,GetRouteReport  ,
		GetNewArrivalNotification ,GetArrivalNotification,GetResultTriage,GetResultTreatment,GetResultTransfer,GetRequstforTransfer ,GetRecivingHospitalResult,GetReport,GetOPReport,GetNotificationEndER,
		GetAllocatedHospital , GetAllocatedAmbulance,GetAllocatedHospitalAmbulanceBoth,GetNoMorecasualty , GetCommandLookforHospital ,GetcommandSetupTacticalAreas ,GetCallForSilverMeeting ,
		GetCommandPre_RescueTreatment ,GetupdatesFromFR ,
		BoradcastAICpdate,
		DrivingtoIncident,DrivingtoHospital,DrivingtoBaseStation , TravalingToIncident ,TravalingToBaseStation, 
		GoToControlArea,GoToSectorScene,GoToloadingArea ,GoToCasualty, SearchCasualty,GoToVehicle,GoToResponders,GOToRC , GoToTacticalArea ,GoToCCS ,

		Triage,Treatment,TransferCasualtytoVehicle,loadCasualtyToVehicle,TransferCasualtytoHospital,DownloadCasualtyFromeVehicle,TransferCasualtytoTratmentArea,Extrication, Observeandcount ,

		InfromResult,InfromResultTriage,InfromResultTreatment,InfromResultTransferToVehicle ,InfromResultTransferToHospital ,InformNomorecasualty ,BoradcastFICpdate ,
		InfromReport,InfromAllocationResult, RequestAdditionalResources,BoradcastEndER,SendReortToEOC,InformRouteReport ,
		InformTriageReport ,InformEndTriageReport,InformTreatmentandTransferReport,InformEndTreatmentandTransferReport,InformNewReceivingHospital,InformLoadingandTransportationReport,InformEndLoadingandTransportationReport,
		InfromResultSetupLoadingArea ,InfromResultSetupCCS ,InformEndCCSReport  ,GetSAupdatefromAIC  ,BoradcastEndScene ,
		
		ReportResult,ReportPlan, ReportAssessSituation,RequestAmbulance,SetuploadingArea , SetupCCS ,

		AssessSituation,FormulatePlan,UpdatePlan,AllocatResponders,AllocatHospital,AllocatAmbulance, CommandAsignRole,  instructTemoAICtohandover ,CommandParamedicTriage,CommandParamedicTreatment,CommandParamedicTransfer ,CommandDriverTransfer,HandovertoAIC ,
		SearchRoute ,
		
		GetRequestCarryCasualtytoCCS ,
		HandovertoFIC,CommandPolicemanClearRoute , InformResultDirectingCasualty ,DirectingCasualtyTOSafety ,CommandFirefighterClearRoute ,
		GetCommandSetupSector ,GetCommandClearRoute ,GetCommandCarryCasualtytoCCS,GetresultofSearchandRescue ,CommandFireFighterCarry , GetSAupdatefromPIC ,CommandFireFightertostop ,
		
		GoToRoute, 
		Extract,CarryCasualtytoCCS ,SetupSector ,ClearRoute , DownloadCasualty ,GetresultofCasualtyDeadorGuid ,GetresultofCarryCasualtytoCCS ,FreeRallocatResponders ,GuidUninjuriedCasualtyTORC ,
		
		InfromResultSetupSector , InfromResultRoute , InfromResultExtract , InformResultCarryCasualtytoCCS ,InformResultCasualtyDead ,InfromOPReport ,InformEndCarryCasulatyrReport, 
		InformTrappedcasualty,
		
		GetRequstecollectInjuredcasualty ,CarryCasualtytoCP , SearchFireFighter ,RequestFireFighter ,
		HandovertoPIC,RequstandupdatesinOP,GetSAupdatefromFIC,GetRouteProirtytoclearFromFIC ,
		SetupCordons , SetupRC ,SetupBHA ,BoradcastcallForsliverMeeting ,CallForSilverMeeting ,Meeting , SetupControalArea , 
		GoToCordon ,GetRequestcollectDeceasedcasualty ,Getresultcasultyinfromation ,GoToRC ,GetCommandCollectUninjuredCasualtyinRC ,GetCommandGuideUninjuredCasualtytoRC ,
		GetCommandManageTraffic , GetCommandSetupCordons,GetCommandSetupSRC , GetCommandSetupBHA  , GetCommand,GetCommandGuidCasualtytoCCS ,GetResultofSetupTA , CommandPolicemanInformation ,GetCommandCollectDeceasedCasualty ,
		CommandPolicemanSecure, loginformation ,GetCommandSecureRoute ,
		CordonsCreated, RouteTrafficCleared  , GetActivitySetupCordons ,GetActivityManageTraffic ,GetActivityCasualty ,  ControlEntryAccess ,
		GetResultCleardRoute,GetResultSecureRoute ,GetRouteProirtytoclearFromPIC ,SecureRoute ,
		InfromResultSetupCordons , InfromResultRoute1 , InfromResultExtract1 , InformResultTransferCasualtytoCCS1,InformcasultyinfromationReport ,
		InformClearandSecurOfRouteReport , InformResultcollectioninformation  ,InformEndcasultyinfromationReport ,InfromResultSetupControalArea ,
		InformNewReceivingEOCReport , InfromResultSetupRC ,CommandSetupTA,GetCommandCollectInjured_EvacuatedCasualty ,BoradcastEndRC ,
		
		InformEndClearRouteTraffic , InformEndClearRouteWreckage ,InfromOPReport2,
	}


	public enum RespondersTriggers{

		OnVehicle,Engineisrunning, ArrivedIncident, ArrivedBaseStation,  Arrivedbluelightflashing ,	ArrivedPEA,	ArrivedLA ,	ENDSceneOprations ,SensedRoute ,
		ArrivedCasualty,ArrivedTargetObject,ArrivedResponder,ArrivedHospital ,ArrivedVehicle,ArrivedDistination,ArrivedBuilding,Arrived_WalkedinAllScene,Arrived_WalkedalongOneDirection,			
		FirstArrival_NoResponder,NotFirstArrival_ThereisResponder, SensedCasualty,CasualtyHasanotherParamedic,	GetDriverSOONarrived ,	ENDCCSOprations ,
		AllCasualtyTriaged,IaminMySectorScene,IamNOTinMySectorScene,ComeBack,CommnderHere,SensedFireFighterResponder ,ENDRCOprations ,
		ThereisTrappedcasualty,
		GetCommandRole,AssigendRolebyAIC ,ICRolehanded,CORolehanded, GetHandoverReport , GetinstructiontoHandover ,GetCallForSilverMeeting ,GetParamedicarrivedforsetup ,
		GetCommandGotoSector,GetCommandGotoScene, GetCommandTriage,GetCommandTreatment ,GetCommandTransferToVehicle ,GetCommandTransferToHospital,GetresultofCasualtyDead ,GetresultofCarryCasualtytoCCS ,
		GetNewResponderarrived , GetNewParamedicarrived,GetParamedicComeback ,GetNewDriverarrived ,GetDriverarrived,GetDriverleaved ,GetresultofTriage,GetresultofTreatment,GetresultofVTransfer,GetresultofHTransfer,
		GetresultofTriageReport, ENDER, AllocatedAmbulance,GetReport,GetOPReport,GetRouteReport ,GetNewParamedicarrivedforsetup , GetSAupdatefromAIC  ,GetActivityTransportation ,
		GetRequstTransfer,GetAllocatedHospital , GetAllocatedAmbulance,  GetNewRecivingHospital, GetCommandLookforHospital,Getinstructiontoleave ,
		GetCommandPre_RescueTreatment ,GetresultofPre_RescueTreatment ,

		delayinwork, NoMorecasualty,UpdatedPlanTrigger, TempAICofFirstArrival ,TempACOofFirstArrival ,ThridAttandant,CasualtyDead,NoMorecasualtyNeedEvacuation,
		FirstTimeonLocation, FirstTimeOnActivity , DoneTriageActivity,DoneTreatmentActivity,DoneLoadingActivity,DoneEmergencyResponse ,Alarm999 ,DoneTask ,ThereisneedforHospital ,EndTriage,EndTreatment , GetEndTriage,GetEndTreatmentandtransfer,GetEndLoading,
		LateNewResponderArrived,DoneFieldTriageActivity , SliverMeetingStart ,  SliverMeetingEnd , 
		
		EndingAction, GetSAupdatefromFICBC  ,Casualtyleaved ,

		GetRequestCarryCasualtytoCCS ,DirectToCasualty ,
		FICRolehanded , GetresultofSearchandRescueReport ,ResultofClearRoutesfromW ,GetReallocateNFirefighter ,GetRouteProirtytoclearFromFIC , 
		AssigendRolebyFIC, TempFICofFirstArrival , GetSAupdatefromFIC  ,
		 
		 GetActivityClearRoute ,GetActivitySearchCasualty ,GetFirefighterarrivedforsetup , GetNewFirefighterarrived ,GetFirefighterComeback ,GetresultofDirectingCasualty ,
		GetCommandReallocation ,GetCommandClearRoute , GetCommandSetupSector , GetCommandExtract,GetCommandCarryCasualtytoCCS ,CasualtyHasanotherFireFighter ,	EndCarryCasualtytoCCS ,	
		waitingInstalling,
		
		DoneActivity , DoneSetupSectorActivity , DoneClearRouteActivity , DoneSearchCasualtyActivity , DoneSceneResponseActivity  ,SectorCreated, RouteCleared , CasualtyExtracted ,
		ArrivedIncidentBackfromHosp,
		
		
		PICRolehanded, DoneSetupActivity  ,GetNewPolicemanarrivedforsetup,GetresultofSetup ,Getresultcasultyinfromation_uninjured,Getresultcasultyinfromation_injured  ,Getresultcasultyinfromation_Decased ,GetSAupdatefromPIC ,
		AssigendRolebyPIC, TempPICofFirstArrival ,ResultofClearRoutesfromtraffic ,ResultofSetupTA , IamNOTinLocation , GetRequstecollectDeceasedcasualty ,
		GetCommandSetupCordons,GetCommandSetupSRC , GetCommandSetupBHA ,GetCommandCollectUninjuredCasualtyinRC ,GetCommandGuideUninjuredCasualtytoRC ,
		GetCommandManageTraffic , GetCommand,GetCommandGuidCasualtytoCCS ,GetCommandSecureCordon  ,GetCommandSecureRoute ,
		Cordonestablished  ,TAestablished , RCestablished  ,RouteTrafficCleared   ,GetActivityManageTraffic ,GetActivityCasualty ,  ControlEntryAccess ,
		GetNewPolicemanarrived,GetPolicemanComeback , GetBronzeCommanderstart ,GetResultCleardSecuredRouteReport , GetbackResponderarrived ,
		GetResultCleardRoute,GetResultSecureRoute ,GetRouteProirtytoclearFromPIC ,GetCommandCollectDeceasedCasualty  ,GetActivityCCSResponse ,GetActivitySceneResponse ,
		DoneSetupCordonsActivity , DoneClearTrafficRouteActivity , DonecollectinformationCasualtyActivity ,
		PCORolehanded ,CasualtyHasanotherPoliceman,GetCommandCollectInjured_EvacuatedCasualty ,
		GetEOCReport , 
		GetcommandSetupTacticalAreas ,GetRequstecollectInjuredcasualty ,
		
		GetActivityControlentryaccessofcordon ,GetActivitycollectionofinformation,
		
		AcceptRequest ,RejectRequest,
		
		GetCaualty_listReadyTocollectinformation 

	}

	//---------------------------------------------------------------------------------	

	public static int ListTotal_Amb= 18 ;
	public static int ListTotal_Fire= 17 ;
	public static int ListTotal_Poice= 14 ;
	
	
	////WaitingBeging , Gatheringinfo,Sharinginfo , SliverMeeting  , Planformulation,Planexecution
	
	//Ambulance 
	public enum RespondersBehaviourTypes1{Idle,Travlling, RoleAssignment,Movementonincidentsite, TaskPlanning ,

		TaskExecution_SetupTA ,TaskExecution_fieldtriage,TaskExecution_secondtriage ,TaskExecution_treatment,CasualtyTransferDelay,TaskExecution_transfertoV,TaskExecution_transfertoH, 
				
		Planformulation ,WaitingDuring ,Comunication , ComunicationDelay  ,WaitingEnd ,Done 

	} //18  
	
	public static RespondersBehaviourTypes1[] RespondersBehaviour_list1={RespondersBehaviourTypes1.Idle,RespondersBehaviourTypes1.Travlling, RespondersBehaviourTypes1.RoleAssignment ,  RespondersBehaviourTypes1.Movementonincidentsite ,RespondersBehaviourTypes1.TaskPlanning,

			RespondersBehaviourTypes1.TaskExecution_SetupTA, RespondersBehaviourTypes1.TaskExecution_fieldtriage , RespondersBehaviourTypes1.TaskExecution_secondtriage  ,RespondersBehaviourTypes1.TaskExecution_treatment,RespondersBehaviourTypes1.CasualtyTransferDelay, RespondersBehaviourTypes1.TaskExecution_transfertoV,RespondersBehaviourTypes1.TaskExecution_transfertoH,

		RespondersBehaviourTypes1.Planformulation,RespondersBehaviourTypes1.WaitingDuring ,RespondersBehaviourTypes1.Comunication ,RespondersBehaviourTypes1.ComunicationDelay , RespondersBehaviourTypes1.WaitingEnd , RespondersBehaviourTypes1.Done};
	
	//---------------------------------------------------------------------------------		
	//Fire      
	public enum RespondersBehaviourTypes2{Idle,Travlling, RoleAssignment,Movementonincidentsite, TaskPlanning ,
	 		
		TaskExecution_SetupTA , TaskExecution_Extrication ,TaskExecution_Directcasulty ,TaskExecution_CarrytoCCS , CasualtyTransferDelay , TaskExecution_cleareRoute ,	
		
		Planformulation ,WaitingDuring , Comunication , ComunicationDelay ,WaitingEnd ,Done 

	} //17
	
	public static RespondersBehaviourTypes2[] RespondersBehaviour_list2={RespondersBehaviourTypes2.Idle,RespondersBehaviourTypes2.Travlling, RespondersBehaviourTypes2.RoleAssignment ,  RespondersBehaviourTypes2.Movementonincidentsite ,RespondersBehaviourTypes2.TaskPlanning,

			RespondersBehaviourTypes2.TaskExecution_SetupTA, RespondersBehaviourTypes2.TaskExecution_Extrication ,RespondersBehaviourTypes2.TaskExecution_Directcasulty , RespondersBehaviourTypes2.TaskExecution_CarrytoCCS , RespondersBehaviourTypes2.CasualtyTransferDelay  ,RespondersBehaviourTypes2.TaskExecution_cleareRoute ,

			RespondersBehaviourTypes2.Planformulation,RespondersBehaviourTypes2.WaitingDuring ,RespondersBehaviourTypes2.Comunication,RespondersBehaviourTypes2.ComunicationDelay , RespondersBehaviourTypes2.WaitingEnd , RespondersBehaviourTypes2.Done};
	
	//---------------------------------------------------------------------------------	
	//Police  /TaskExecution_SecureRoute   
	public enum RespondersBehaviourTypes3{Idle,Travlling,  RoleAssignment, Movementonincidentsite, TaskPlanning ,
			
		TaskExecution_setupTA ,TaskExecution_loginformation, TaskExecution_cleareRoute ,	
		
		Planformulation, WaitingDuring ,Comunication , ComunicationDelay ,WaitingEnd ,Done 
	
	} //14 

	public static RespondersBehaviourTypes3[] RespondersBehaviour_list3={RespondersBehaviourTypes3.Idle,RespondersBehaviourTypes3.Travlling, RespondersBehaviourTypes3.RoleAssignment ,  RespondersBehaviourTypes3.Movementonincidentsite ,RespondersBehaviourTypes3.TaskPlanning,

			RespondersBehaviourTypes3.TaskExecution_setupTA, RespondersBehaviourTypes3.TaskExecution_loginformation, RespondersBehaviourTypes3.TaskExecution_cleareRoute ,

			RespondersBehaviourTypes3.Planformulation,RespondersBehaviourTypes3.WaitingDuring ,RespondersBehaviourTypes3.Comunication ,RespondersBehaviourTypes3.ComunicationDelay , RespondersBehaviourTypes3.WaitingEnd , RespondersBehaviourTypes3.Done};
	
	//---------------------------------------------------------------------------------	
	// 2- Task and command  
	
	public enum Ambulance_ActivityType { None,Communication, SceneResponse,  CCSResponse ,loadingandTnasporation ,HospitalSelection    }  //used by Silver commander
	
	public enum Fire_ActivityType  { None,Communication  ,SearchandRescueCasualty ,ClearRoute } //used by Silver commander
	
	public enum Police_ActivityType  { None, Communication , SecureScene_outerCordons ,CollectInformation  } //used by Silver commander  ... SecureScene_innerCordons,
	
	
	//---------------------------------------------------------------------------------	
	public enum Ambulance_TaskType  {

		None,TravaltoIncident ,TravaltoBaseStation , InitialResponse, GoTolocation, GoBackToControlArea ,

		SetupTacticalAreas  , SceneResponse , CCSResponse , SearchCasualty ,FieldTriage,Pre_RescueTreatment , SecondryTriage ,Treatment,TransferCasualtytoVehicle, TransferCasualtytoHospital,TreatmentandTransfertoVehicle ,LookforHospital,

		CoordinateFirstArrival,HandovertoAIC,CoordinateInitialResponse,CoordinateEndResponse,

		CoordinateCasualtyTriage,CoordinateCasualtyTreatmentandTransfer, CoordinateCasualtyTransferToHospital, CoordinateCommunication ,CoordinateER ,CoordinateCCS,

	}  

	//---------------------------------------------------------------------------------	
	public enum Fire_TaskType {

		None,TravaltoIncident ,TravaltoBaseStation , InitialResponse, GoTolocation, GoBackToControlArea ,

		SetupSector, SearchCasualty, Extract, CarryCasualtytoCCS,  ClearRouteWreckage,

		CoordinateFirstArrival,HandovertoFIC,CoordinateInitialResponse,CoordinateEndResponse,

		CoordinateClearRoute,CoordinateSetupSector, CoordinateSceneOperation, CoordinateCommunication ,CoordinateER }

	
	//---------------------------------------------------------------------------------	 GuidUninjuriedCasualty ,
	public enum Police_TaskType     {
		
		None,TravaltoIncident ,TravaltoBaseStation , InitialResponse, GoTolocation, GoBackToControlArea ,

		SetupTacticalAreas ,ClearRouteTraffic ,SecureRoute , collectionofinformation , collectInjuriedEvacuetedCasualty   ,CollectUninjuriedCasualty  , CollectDeceasedCasualty  , 
		
		CoordinateFirstArrival,HandovertoPIC,CoordinateInitialResponse,CoordinateEndResponse,  

		CoordinateSetupcordon,CoordinateSetupRC ,CoordinateOutercordon ,CoordinateInformationCollectionofCasualty   ,  CoordinateCommunication ,CoordinateER ,
		
	}  // ControlRouteandAccessofcordon ,

	
	//--------------------------------------------------------------------------------- 
	// Communication 
	public enum ACLPerformative{

		Command, Requste ,AcceptRequest ,RejectRequest ,Instructiontoleave , CallForSilverMeeting ,InformNomorecasualtyScene,InformDriverArrivalSoon ,

		InformNewResponderArrivalforsetup ,InformNewResponderArrival,InformAICArrival ,InformlocationArrival,InformNewDriverArrival ,InformDriverArrival ,InformDriverleave,

		InformResultFieldTriage,InformResultSecondTriage,InformResultTreatment,InformResultVTransfer,InformResultHTransfer,InformCCSOprationsEND ,InformRCOprationsEND ,

		InformNewReceivingHospital,InformHospitalAllocationResult,InformAmbulanceAllocationResult,InformDecasedCasualtyResport ,

		InformHandoverReport , InfromFirstArrival , InformFirestSituationAssessmentReport,InformSituationReport ,InformERendReport,InformDoneSceneResponseActivity ,InfromResultSetupLA ,
		InformResultPre_RescueTreatment ,
		InformTriageReport,InformEndTriageReport ,InformTreatmentandTransferReport,InformEndTreatmentandTransfer, InformLoadingandTransportationReport,
		InformOPlanReport , InfromCCSEstablished ,InfromLoadingAreaEstablished ,InfromResultSetupCCS,InfromResultSetupLoadingArea ,
		
		InformERend ,InformNomorecasualty , InformSceneOprationsEND ,
				
		 InformSearchandRescueCasualtyReport ,InformEndSearchandRescueCasualty , InformEndRouteReport  ,InfromRCEstablished ,
		InfromResultSetupSector , InfromResultRoute , InfromResultExtract , InformResultCarryCasualtytoCCS,InformResultCasualtyDead ,InformEndCarryCasulatyrReport ,
		InformRouteReport ,InformReallocation ,RequsteReallocateNResponders ,InformClearOfRouteReport ,InformResultDirectingCasualty ,
		
		
		InformComebackResponderArrival , InfromCordonEstablished ,InfromControalAreaEstablished ,
		InfromResultSetupCordon,InfromResultSetupRC, InformRouteNeedclrear ,InformClearandSecurOfRouteReport,InformcasultyinfromationReport ,InformEndcasultyinfromation ,	
		EOCReport ,InformNewReceivingEOCReport  ,InformResultcollectioninformation ,InfromResultSetupControalArea ,
		InformResultcasultyinfromation_Decased ,InformResultcasultyinfromation_uninjured ,InformResultcasultyinfromation_injured
		
		, InformEndClearRouteTraffic , InformEndClearRouteWreckage,InformTrappedcasualty ,   InformCCSReport ,InformCCSReport_casTolog ,
		
		
		InformNomorecasualty_impact ,InformNomorecasualty_CCS ,
		InformEndFieldTriage,InformEndLoadingandTransportation_Nomorecasualty ,InfromSafetyBriefandSectorEstablished ,InformEndSearchandRescue_Nomorecasualty
	}
	
	//--------------------------------------------------------------------------------- 

	public enum UpdatePlanType{Setupadd ,Setupremove , NewRes, FreeRes,RemoveRes , NewTask ,AssignTask,UpdateTask_stop,UpdateTask_inprogress, CloseTask ,TrackTask , TrackandUpdateTask_stop , 
		UpdateTask_assignH,UpdateTask_assignAmb,UpdateTask_sendtoALC ,UpdateTask_triage ,UpdateTask_decased ,UpdateTask_triage_or_hospital,UpdateTask_stop2 ,
		 UpdateTask_secure ,AssignTask_list ,UpdateTask_waiting_tobeinCCS} 
	public enum CordonType{inner , outer , enteranc ,direction ,casualtysign ,Area }
	public enum TacticalAreaType{  RC , BHA , CCS ,LoadingArea ,ControlArea}
	public enum RandomWalking_StrategyType{  StreetsCenter ,OneDirection , TowDirections_AtsameAxis,FourDirections_big, FourDirections_small,OneDirection_sector ,Nodes_Cordon ,Safelocations_Cordon}
	public enum TargetObject{Distination,Vehicle,Targetobject,Responder,Casualty,Building,Route,nothing} //walking target for responder		
	public enum GeneralTaskStatus{Waiting,Inprogress,Stop,Done, waiting_tobeinCCS }// New  ,Closed
	public enum STartEvacuationtoHospital_Startgy{Field_EndTrigaeAllCasualty ,Field_REDCasultyOnllyuntilEndTrigaeAllCasualty, CCS_REDCasultyOnllyuntilEndSceneOpration , CCS_MorepriorityinCSS}
	public enum Level {High, Medium, Low ,None}	
	public enum MovmentCasualty {Nomovment ,TORandomsafelocation ,TOCP, TORC,TOCCS } // RandomlyInscene,
	
	public enum ReportSummery{None, ConfirmedorDeclared ,AdditionalResourceRequest ,EndER_Fire ,EndER_Police , EndER_Ambulance }
	//--------------------------------------------------------------------------------- 
	public enum TaskAllocationApproach {Decentralized_Autonomous, Decentralized_Mutualadjustment ,CentralizedDirectSupervision , Stigmergy }
	public enum TaskAllocationMechanism {Environmentsectorization ,NoEnvironmentsectorization }

	public enum Allocation_Strategy{FIFO,FIFO_Pre , SmallestDistance,Severity_RYGPriorty,Severity_RPMPriorty ,Severity_RYGPriorty_trans ,Severity_RYGPriorty_treat }
	public enum Allocation_Strategy_Route{FIFO,SmallestDistance,SliverPriorty}
	public enum ResourceAllocation_Strategy{NormalSituation , StandBy ,ConfirmedDeclared , FirstTimeDeclared ,AdditionalResourceRequest }
	public enum SurveyScene_SiteExploration_Mechanism {Randomly_exploration,Organized_exploration }
	public enum	GeolocationMechanism { Papermap , GPSmap } 
	public enum AmbulanceTracking  { Radio , GPS } 
	
	public enum Inter_Communication_Structure {Struc1_SilverCommandersInteraction ,Struc2_BronzeCommandersInteraction}
	public enum Communication_Time {Every_frequently ,When_need} //Every_10m,Every_20m,Every_30m,Every_40m
	//public enum	Inter_Communication_Tool{ SilverMeeting,  SharedTactical_Radio ,FaceToFace , Phone }
	
	public enum CommunicationMechanism{FaceToFace,SilverMeeting, RadioSystem, SharedTactical_RadioSystem  ,RadioSystem_BoradCast ,Phone ,ElectronicDevice  ,FF_BoradCast ,
		SharedTactical_RadioSystem_and_FaceToFace ,SharedTactical_RadioSystem_and_Phone ,FaceToFace_and_Phone  }
	public enum CasualtyReportandTrackingMechanism {Paper, Electronic }	
	
	public enum TypeMesg {External ,Inernal};
	
	
	//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%


}







