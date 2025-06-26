package C_SimulationOutput;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import A_Agents.Responder;
import A_Agents.Responder_Ambulance;
import A_Agents.Responder_Fire;
import A_Agents.Responder_Police;
import A_Environment.Incident;
import A_Roles_Ambulance.Ambulance_Paramedic;
import B_Classes.Duration_info;
import D_Ontology.Ontology;
import D_Ontology.Ontology.Ambulance_ResponderRole;
import D_Ontology.Ontology.Fire_ResponderRole;
import D_Ontology.Ontology.Police_ResponderRole;
import D_Ontology.Ontology.RespondersActions;
import D_Ontology.Ontology.RespondersBehaviourTypes1;
import D_Ontology.Ontology.RespondersBehaviourTypes2;
import D_Ontology.Ontology.RespondersBehaviourTypes3;

public class ResponderOutput { // for each Responder separately 

	Responder AssignedResponder;

	Ambulance_ResponderRole  CuurentRole1;	
	Fire_ResponderRole  CuurentRole2;
	Police_ResponderRole  CuurentRole3;

	//public ArrayList<Action> ActionDetails_List = new ArrayList<Action>();
	//public ArrayList<Action> ActionSummery_List = new ArrayList<Action>();	

	public ArrayList<Action> BehSummery_List = new ArrayList<Action>();	


	public BehRec[] Beh_list_Amb =new BehRec[Ontology.ListTotal_Amb]  ;  
	public BehRec[] Beh_list_Fire =new BehRec[Ontology.ListTotal_Fire]  ;
	public BehRec[] Beh_list_Police =new BehRec[Ontology.ListTotal_Poice]  ;

	//************************************************************************************************************************
	public ResponderOutput (Responder _AssignedResponder)   //this is LogFile
	{
		AssignedResponder= _AssignedResponder;

		if ( _AssignedResponder  instanceof Responder_Ambulance )
			CreateBehlist_1();  

		if ( _AssignedResponder  instanceof Responder_Fire )
			CreateBehlist_2();  

		if ( _AssignedResponder  instanceof Responder_Police )	
			CreateBehlist_3();  
	}

	//************************************************************************************************************************
	private void CreateBehlist_1()
	{
		for ( int i=0 ; i<Ontology.ListTotal_Amb ; i++)
		{
			BehRec BR=new BehRec (Ambulance_ResponderRole.None , D_Ontology.Ontology.RespondersBehaviour_list1[i]); 
			Beh_list_Amb[i]=BR;
		}
	}

	private void CreateBehlist_2()
	{
		for ( int i=0 ; i<Ontology.ListTotal_Fire ; i++)
		{
			BehRec BR=new BehRec (Fire_ResponderRole.None , D_Ontology.Ontology.RespondersBehaviour_list2[i]); 
			Beh_list_Fire[i]=BR;
		}
	}

	private void CreateBehlist_3()
	{
		for ( int i=0 ; i<Ontology.ListTotal_Poice ; i++)
		{
			BehRec BR=new BehRec (Police_ResponderRole.None , D_Ontology.Ontology.RespondersBehaviour_list3[i]); 
			Beh_list_Police[i]=BR;
		}
	}	
	//*******************************************
	// this  one use nowwwwwww	
	public void  IncrmentBeh(  	Ambulance_ResponderRole  _CuurentRole1,	RespondersBehaviourTypes1 _BehaviourType1  ,
			Fire_ResponderRole  _CuurentRole2,  RespondersBehaviourTypes2 _BehaviourType2 ,
			Police_ResponderRole  _CuurentRole3 , RespondersBehaviourTypes3 _BehaviourType3 )
	{		
		if ( AssignedResponder  instanceof Responder_Ambulance )
		{
			for ( BehRec  B : Beh_list_Amb ) 
				if (B.BehaviourType1 == _BehaviourType1  ) 
				{  B.Role1=_CuurentRole1 ;  B.TotalTick++; break ;}	
		}

		if ( AssignedResponder  instanceof Responder_Fire )
		{
			for ( BehRec  B : Beh_list_Fire ) 
				if (B.BehaviourType2 == _BehaviourType2  ) 
				{  B.Role2=_CuurentRole2 ;  B.TotalTick++; break ;}	
		}

		if ( AssignedResponder  instanceof Responder_Police )	
		{
			for ( BehRec  B : Beh_list_Police ) 
				if (B.BehaviourType3 == _BehaviourType3  ) 
				{  B.Role3=_CuurentRole3 ;  B.TotalTick++; break ;}	
		}
	}

	//====================================================================================================================================================================
	//     																	 Printing
	//====================================================================================================================================================================
	// This one used 	
	public int counterUsed=-1;
	public double RU=0 , CE=0   , com=0,Com_delay=0 ,idle=0  , base1=0 ,base2=0 ;	
	public double leaveStation1  , BacktoStation1 ,ArrivedScene1 ;	
	public Ambulance_ResponderRole  Rolle1=null;
	public Fire_ResponderRole  Rolle2=null;
	public Police_ResponderRole  Rolle3=null;

	//************************************************************************************************************************	
	public void ReportBeh_Amb(boolean PrintFile1 , PrintWriter d  ,boolean PrintFile2 , FileWriter fw ) throws IOException
	{
		//-----------------------
		if ( ! PrintFile1 && !PrintFile2 )
		{
			System.out.println(" ____________________________________________________ " );
			for ( BehRec  B: Beh_list_Amb ) 	
				System.out.println(AssignedResponder.Id +": "+ B.Role1+ "-"+ B.BehaviourType1  +"   "+ B.TotalTick  );	
		}


		//-----------------------
		if ( PrintFile1 )			
		{
			d.print(AssignedResponder.Id +"\t" );
			d.print( Beh_list_Amb [12].Role1+"\t");

			for ( int i=0 ; i<Ontology.ListTotal_Amb ; i++)
				d.print( Beh_list_Amb [i].TotalTick+"\t");

			d.println( );
		}

		//-----------------------
		if ( PrintFile2 )			
		{
			String Begin_R=null;
			switch( AssignedResponder.PrvRoleinprint1 )	{
			case Driver :
				Begin_R="Drv.";
				break;
			case Paramedic:	
				Begin_R="Res. ";
				break;
			case AmbulanceSectorCommander:
				Begin_R="ASC. ";
				break;
			case AmbulanceLoadingCommander :
				Begin_R="ALC ";
				break;
			case CasualtyClearingOfficer :
				Begin_R="CCO ";
				break;
			case AmbulanceCommunicationsOfficer:
				Begin_R="ACO ";
				break;
			case AmbulanceIncidentCommander :
				Begin_R="AIC ";
				break;

			}    


			RU=0 ; CE=0   ; com=0 ;idle=0 ; base1=0 ;base2=0 ;	
			fw.append(AssignedResponder.Id +"\t" );
			if ( counterUsed==0) fw.append( Begin_R +"\t");	else 	  fw.append( Begin_R +counterUsed+"\t");  // fw.append( Beh_list[12].Role+"\t"); 

			for ( int i=0 ; i< Ontology.ListTotal_Amb ; i++)
				fw.append( Beh_list_Amb[i].TotalTick+"\t");


			if (  Rolle1 == Ambulance_ResponderRole.Paramedic )
			{ 								
				double TasksEx=( Beh_list_Amb[5].TotalTick + Beh_list_Amb[6].TotalTick + Beh_list_Amb[7].TotalTick +  Beh_list_Amb[8].TotalTick +  Beh_list_Amb[10].TotalTick + Beh_list_Amb[11].TotalTick ) + Beh_list_Amb[9].TotalTick  ;
				double  TravlMov= (Beh_list_Amb[1].TotalTick  +  Beh_list_Amb[3].TotalTick) ;
				double  PerpareEnd=Beh_list_Amb[16].TotalTick ; //waiting end				
				double  Com=  Beh_list_Amb[14].TotalTick+Beh_list_Amb[15].TotalTick ;

				double  RoleTaskplan=Beh_list_Amb[2].TotalTick + Beh_list_Amb[4].TotalTick ;				
				double  wastTime= Beh_list_Amb[13].TotalTick  ; //noaction 

				double   Base=   BacktoStation1 - leaveStation1  ;  //(   LastcasualtytransferedfromSecen -this.AssignedResponder.ArrivedScene );

				RU=(  TravlMov + TasksEx + PerpareEnd  )/(  Base  )   ;								
				CE= (RoleTaskplan + Com  )/(  Base  )   ;
			}
			else if (  Rolle1 == Ambulance_ResponderRole.Driver )
			{				

				double TasksEx=(  Beh_list_Amb[5].TotalTick + Beh_list_Amb[10].TotalTick ) + Beh_list_Amb[9].TotalTick  ;  // yes there is delay 
				double  TravlMov= (Beh_list_Amb[1].TotalTick   + Beh_list_Amb[3].TotalTick) ;
				double  PerpareEnd=Beh_list_Amb[16].TotalTick ; //waiting end				
				double  Com=  Beh_list_Amb[14].TotalTick +  Beh_list_Amb[15].TotalTick ;// com delay

				double  RoleTaskplan=Beh_list_Amb[2].TotalTick  +Beh_list_Amb[4].TotalTick ;				
				double  wastTime= Beh_list_Amb[13].TotalTick  ; //noaction 

				double   Base1=   BacktoStation1 - leaveStation1  ;  //(   LastcasualtytransferedfromSecen -this.AssignedResponder.ArrivedScene );

				RU=(  TravlMov + TasksEx + PerpareEnd  )/(  Base1  )   ;								
				CE= (RoleTaskplan + Com  )/(  Base1  )   ;

			}

			//other role
			{

				com=  Beh_list_Amb[14].TotalTick  ;
				idle= Beh_list_Amb[13].TotalTick ;
				Com_delay=Beh_list_Amb[15].TotalTick ;

				base1=   BacktoStation1 - leaveStation1  ;  //(   LastcasualtytransferedfromSecen -this.AssignedResponder.ArrivedScene );
				base2=Beh_list_Amb[1].TotalTick +Beh_list_Amb[2].TotalTick +Beh_list_Amb[3].TotalTick +Beh_list_Amb[4].TotalTick +Beh_list_Amb[5].TotalTick 
						+Beh_list_Amb[6].TotalTick +Beh_list_Amb[7].TotalTick +Beh_list_Amb[8].TotalTick +Beh_list_Amb[9].TotalTick +Beh_list_Amb[10].TotalTick 
						+Beh_list_Amb[11].TotalTick +Beh_list_Amb[12].TotalTick +Beh_list_Amb[13].TotalTick +Beh_list_Amb[14].TotalTick+ Beh_list_Amb[15].TotalTick+ Beh_list_Amb[16].TotalTick ;



			}

			fw.append( RU+"\t");

			fw.append( CE+"\t");

			fw.append( base1+"\t");

			fw.append( base2+"\t");

			fw.append("\n" );

		}

	}

	//************************************************************************************************************************
	public void ReportBeh_Fire(boolean PrintFile1 , PrintWriter d  ,boolean PrintFile2 , FileWriter fw ) throws IOException
	{
		//-----------------------
		if ( ! PrintFile1 && !PrintFile2 )
		{
			System.out.println(" ____________________________________________________ " );
			for ( BehRec  B: Beh_list_Fire ) 	
				System.out.println(AssignedResponder.Id +": "+ B.Role2+ "-"+ B.BehaviourType2  +"   "+ B.TotalTick  );	
		}


		//-----------------------
		if ( PrintFile1 )			
		{
			d.print(AssignedResponder.Id +"\t" );
			d.print( Beh_list_Fire [12].Role2+"\t");

			for ( int i=0 ; i<=16 ; i++)
				d.print( Beh_list_Fire [i].TotalTick+"\t");

			d.println( );
		}

		//-----------------------
		if ( PrintFile2 )			
		{
			String Begin_R=null;

			switch (AssignedResponder.PrvRoleinprint2 ) {		
			case Driver :  
				Begin_R="Drv.";
				break;
			case FireFighter:	
				Begin_R="Res. ";
				break;
			case FireIncidentCommander:
				Begin_R="FIC. ";
				break;
			case FireSectorCommander:
				Begin_R="FSC. ";
				break;
			case FireCommunicationsOfficer:
				Begin_R="FCO.";
				break;		}


			RU=0 ; CE=0   ; com=0 ;idle=0 ; base1=0 ;base2=0 ;	
			fw.append(AssignedResponder.Id +"\t" );
			if ( counterUsed==0) fw.append( Begin_R +"\t");	else 	  fw.append( Begin_R +counterUsed+"\t");  // fw.append( Beh_list[12].Role+"\t"); 

			for ( int i=0 ; i<Ontology.ListTotal_Fire ; i++)
				fw.append( Beh_list_Fire[i].TotalTick+"\t");


			if (  Rolle2 == Fire_ResponderRole.FireFighter)
			{ 
				double TasksEx=( Beh_list_Fire[5].TotalTick + Beh_list_Fire[6].TotalTick + Beh_list_Fire[7].TotalTick +  Beh_list_Fire[8].TotalTick +  Beh_list_Fire[10].TotalTick  ) + Beh_list_Fire[9].TotalTick  ;
				double  TravlMov= (Beh_list_Fire[1].TotalTick  +  Beh_list_Fire[3].TotalTick) ;			
				double  PerpareEnd=Beh_list_Fire[15].TotalTick ; //waiting end				
				double  Com=  Beh_list_Fire[13].TotalTick +  Beh_list_Fire[14].TotalTick ; //+ com delay

				double  RoleTaskplan=Beh_list_Fire[2].TotalTick +Beh_list_Fire[4].TotalTick ;				
				double  wastTime= Beh_list_Fire[12].TotalTick  ; //noaction 

				double   Base=   BacktoStation1 - leaveStation1  ;  //(   LastcasualtytransferedfromSecen -this.AssignedResponder.ArrivedScene );

				RU=(  TravlMov + TasksEx + PerpareEnd  )/(  Base  )   ;								
				CE= (RoleTaskplan + Com  )/(  Base  )   ;				

			}


			//other role
			{

				com=  Beh_list_Fire[13].TotalTick  ;
				idle= Beh_list_Fire[12].TotalTick  ; 
				this.Com_delay =  Beh_list_Fire[14].TotalTick ;

				base1=   BacktoStation1 - leaveStation1  ;  //(   LastcasualtytransferedfromSecen -this.AssignedResponder.ArrivedScene );
				base2=Beh_list_Fire[1].TotalTick +Beh_list_Fire[2].TotalTick+Beh_list_Fire[3].TotalTick+Beh_list_Fire[4].TotalTick +Beh_list_Fire[5].TotalTick
						+Beh_list_Fire[6].TotalTick+Beh_list_Fire[7].TotalTick+Beh_list_Fire[8].TotalTick+Beh_list_Fire[9].TotalTick+Beh_list_Fire[10].TotalTick+
						+Beh_list_Fire[11].TotalTick+Beh_list_Fire[12].TotalTick+Beh_list_Fire[13].TotalTick+Beh_list_Fire[14].TotalTick+Beh_list_Fire[15].TotalTick;

			}

			fw.append( RU+"\t");

			fw.append( CE+"\t");

			fw.append( base1+"\t");

			fw.append( base2+"\t");

			fw.append("\n" );

		}

	}

	//************************************************************************************************************************
	public void ReportBeh_Police(boolean PrintFile1 , PrintWriter d  ,boolean PrintFile2 , FileWriter fw ) throws IOException
	{
		//-----------------------
		if ( ! PrintFile1 && !PrintFile2 )
		{
			System.out.println(" ____________________________________________________ " );
			for ( BehRec  B: Beh_list_Police ) 	
				System.out.println(AssignedResponder.Id +": "+ B.Role3+ "-"+ B.BehaviourType3  +"   "+ B.TotalTick  );	
		}


		//-----------------------
		if ( PrintFile1 )			
		{
			d.print(AssignedResponder.Id +"\t" );
			d.print( Beh_list_Police [12].Role3+"\t");

			for ( int i=0 ; i<=16 ; i++)
				d.print( Beh_list_Police [i].TotalTick+"\t");

			d.println( );
		}

		//-----------------------
		if ( PrintFile2 )			
		{
			String Begin_R=null;

			switch (AssignedResponder.PrvRoleinprint3) {		
			case Driver :  
				Begin_R="Drv.";
				break;
			case Policeman:	
				Begin_R="Res.";
				break;
			case PoliceIncidentCommander:
				Begin_R="PIC.";
				break;
			case CordonsCommander:
				Begin_R="CC.";
				break;
			case ReceptionCenterOfficer:
				Begin_R="RCO.";
				break;
			case PoliceCommunicationsOfficer:
				Begin_R="PCO.";
				break;		}


			RU=0 ; CE=0   ; com=0 ;idle=0 ; base1=0 ;base2=0 ;	
			fw.append(AssignedResponder.Id +"\t" );
			if ( counterUsed==0) fw.append( Begin_R +"\t");	else 	  fw.append( Begin_R +counterUsed+"\t");  // fw.append( Beh_list[12].Role+"\t"); 

			for ( int i=0 ; i< Ontology.ListTotal_Poice ; i++)
				fw.append( Beh_list_Police[i].TotalTick+"\t");


			if (  Rolle3 == Police_ResponderRole.Policeman)
			{ 
				double TasksEx=( Beh_list_Police[5].TotalTick + Beh_list_Police[6].TotalTick + Beh_list_Police[7].TotalTick )  ;
				double  TravlMov= (Beh_list_Police[1].TotalTick  +  Beh_list_Police[3].TotalTick) ;
				double  PerpareEnd=Beh_list_Police[12].TotalTick ; //waiting end				
				double  Com=  Beh_list_Police[10].TotalTick + Beh_list_Police[11].TotalTick ;

				double  RoleTaskplan=Beh_list_Police[2].TotalTick  + Beh_list_Police[4].TotalTick ;				
				double  wastTime= Beh_list_Police[9].TotalTick  ; //noaction 				
				double   Base=   BacktoStation1 - leaveStation1  ;  //(   LastcasualtytransferedfromSecen -this.AssignedResponder.ArrivedScene );

				RU=(  TravlMov + TasksEx + PerpareEnd  )/(  Base  )   ;								
				CE= (RoleTaskplan + Com  )/(  Base  )   ;


			}

			//other role
			{

				com=  Beh_list_Police[10].TotalTick ;		
				idle= Beh_list_Police[9].TotalTick  ; 
				Com_delay=    Beh_list_Police[11].TotalTick ;

				base1=   BacktoStation1 - leaveStation1  ;  //(   LastcasualtytransferedfromSecen -this.AssignedResponder.ArrivedScene );
				base2=Beh_list_Police[1].TotalTick +  Beh_list_Police[2].TotalTick +Beh_list_Police[3].TotalTick +  Beh_list_Police[4].TotalTick +
						Beh_list_Police[5].TotalTick +  Beh_list_Police[6].TotalTick +Beh_list_Police[7].TotalTick +  Beh_list_Police[8].TotalTick +
						Beh_list_Police[9].TotalTick +  Beh_list_Police[10].TotalTick +Beh_list_Police[11].TotalTick +  Beh_list_Police[12].TotalTick ;
			}


			fw.append( RU+"\t");

			fw.append( CE+"\t");

			fw.append( base1+"\t");

			fw.append( base2+"\t");

			fw.append("\n" );

		}

	}





	//************************************************************************************************************************	
	//	// NOT used
	//	public void ReportAction(  FileWriter fw ) throws IOException
	//	{
	//
	//
	//		String R=null;
	//		switch( AssignedResponder.PrvRoleinprint1 )	{
	//		case Paramedic:	
	//			R="Res. ";
	//			break;
	//		case AmbulanceSectorCommander:
	//			R="ASC. ";
	//			break;
	//		case AmbulanceLoadingCommander :
	//			R="ALC ";
	//			break;
	//		case AmbulanceCommunicationsOfficer:
	//			R="ACO ";
	//			break;
	//		case AmbulanceIncidentCommander :
	//			R="AIC ";
	//			break;
	//		case Driver :
	//			R="Drv.";
	//			break;}    
	//
	//
	//		fw.append(AssignedResponder.Id +"\t" );
	//		if ( counter==0) fw.append( R+"\t");	else 	  fw.append( R+counter+"\t");  
	//
	//
	//		for ( Action  A : ActionDetails_List ) 
	//		{			
	//			fw.append(  A.ChaPrint +"\t" );				
	//		}
	//
	//
	//		fw.append("\n" );
	//		fw.append("\n" );
	//
	//	}
	//	// NOT used
	//	public void ReportaboutAction3()
	//	{
	//		System.out.println(AssignedResponder.Id +": " + BehSummery_List.size() );	
	//	}




}



































//=================================================

////==========================================================================================================================
//	//1- for each behave
//	public void AddBehSummery_Amb(Ambulance_ResponderRole  _Role ,RespondersActions _Action,RespondersBehaviourTypes1 _BehaviourType ,double  _CurrentTick )
//	{
//		int Lastindex = 0;
//
//		if ( BehSummery_List.size() !=0 )
//		{
//			Lastindex=BehSummery_List.size() -1;
//			Action LastAction= BehSummery_List.get(Lastindex) ;
//
//			if (LastAction.BehaviourType ==  _BehaviourType  )
//				LastAction.TotalTick ++;
//			else		 
//				BehSummery_List.add( new Action (_Role,_Action,_BehaviourType,_CurrentTick ));
//
//		}
//		else
//			BehSummery_List.add( new Action (_Role,_Action,_BehaviourType,_CurrentTick ));
//
//		IncrmentBeh(_Role ,_BehaviourType );
//	}
//
//	//==========================================================================================================================
//	//1- for each tick
//	public void  AddAction( Ambulance_ResponderRole  _Role ,RespondersActions _Action,RespondersBehaviourTypes _BehaviourType , double  _CurrentTick)
//	{
//
//		ActionDetails_List.add( new Action (_Role,_Action,_BehaviourType,_CurrentTick ));
//
//		IncrmentBeh(_Role ,_BehaviourType );
//
//	}
//	
//	//2- called each tick option tow ------ used
//	public void AddActionSummery(Ambulance_ResponderRole  _Role ,RespondersActions _Action,RespondersBehaviourTypes _BehaviourType ,double  _CurrentTick )
//	{
//		int LastActionindex = 0;
//
//		if ( ActionSummery_List.size() !=0 )
//		{
//			LastActionindex=ActionSummery_List.size() -1;
//			Action LastAction= ActionSummery_List.get(LastActionindex) ;
//
//			if (LastAction.Action == _Action  )
//				LastAction.TotalTick ++;
//			else		 
//				ActionSummery_List.add( new Action (_Role,_Action,_BehaviourType,_CurrentTick ));
//
//		}
//		else
//			ActionSummery_List.add( new Action (_Role,_Action,_BehaviourType,_CurrentTick ));
//
//		IncrmentBeh(_Role ,_BehaviourType );
//	}

