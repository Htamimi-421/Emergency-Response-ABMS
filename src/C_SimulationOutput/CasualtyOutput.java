package C_SimulationOutput;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import A_Agents.Casualty;
import A_Agents.Responder;
import D_Ontology.Ontology;
import D_Ontology.Ontology.Ambulance_ResponderRole;
import D_Ontology.Ontology.CasualtyAction;
import D_Ontology.Ontology.RespondersActions;


public class CasualtyOutput {  

	Casualty  casualty;    // record for each casualty 
	
	int initialRPM , endRPM  ;
	
	

	public ArrayList<CasualtyStatus> ActionDetails_List = new ArrayList<CasualtyStatus>(); //not used
	public ArrayList<CasualtyStatus> ActionSummery_List = new ArrayList<CasualtyStatus>(); //not used	

	public CasualtyStatus[] ActiononCasualty_list =new CasualtyStatus[Ontology.ListTotal_Casualty]  ;


	//************************************************************************************************************************
	public CasualtyOutput ( Casualty  _casualty  , int  _CurrentRPM  )
	{
		casualty=_casualty;
		initialRPM = _CurrentRPM  ;
		for ( int i=0 ; i<Ontology.ListTotal_Casualty ; i++)
		{
			CasualtyStatus CA=new CasualtyStatus ( D_Ontology.Ontology.CasualtyAction_list_new[i] ); 
			ActiononCasualty_list[i]=CA;
		}	

	}

	//==========================================================================================================================
	//                                    used
	//==========================================================================================================================
	// this one used used used here
	public void IncrmentCasultyAction2(CasualtyAction _Action  , int  _CurrentRPM  )
	{		
		for ( CasualtyStatus  CA  : ActiononCasualty_list  ) 
			if (CA.Action == _Action  ) 
			{  CA.TotalTick++  ; break ;}	
		
		endRPM = _CurrentRPM  ;

	}

	public void ReportaboutAction1(boolean PrintFile2 , FileWriter fw ) throws IOException
	{

		if ( ! PrintFile2 )	
		{
			//System.out.println(" ____________________________________________________ " );
			for (CasualtyStatus A : ActionSummery_List ) 
			{			
				System.out.println(casualty.ID +": "+  A.Action + "             Duration : "+    A.TotalTick  +"   Time: "     + A.CurrentTick);
			}
			System.out.println(casualty.ID +": " + casualty.Life    + " Timeinhospital: " + casualty.TimeAdmissioninHospital );

		}

		//-----------------------
		if ( PrintFile2 )			
		{
			fw.append(casualty.ID  +"\t"+ this.initialRPM +"\t"+this.endRPM +"\t"  );

			for ( int i=0 ; i< Ontology.ListTotal_Casualty ; i++)
				fw.append( ActiononCasualty_list[i].TotalTick+"\t");

			if ( casualty.Dead_duringAction !=null )  fw.append(casualty.Dead_duringAction+"  " + casualty.deadTime );
			else
				fw.append(" "  );
			
			
			fw.append("\n" );

		}



	}


	//==========================================================================================================================
	//                                 not used
	//==========================================================================================================================
	//1- called each tick  option one
	public void  AddAction( CasualtyAction _Action,int  _CurrentRPM, double  _CurrentTick)
	{

		ActionDetails_List.add( new CasualtyStatus (_Action,  _CurrentRPM , _CurrentTick));  // not used

	}

	//==========================================================================================================================
	//2- called each tick  not used
	public void ActionSummery( CasualtyAction _Action,int  _CurrentRPM, double  _CurrentTick)
	{
		int LastActionindex=0;

		if (ActionSummery_List.size()!=0 )
		{
			LastActionindex = ActionSummery_List.size() -1;
			CasualtyStatus LastAction= ActionSummery_List.get(LastActionindex) ;

			//------------------
			if (LastAction.Action == _Action  )
				LastAction.TotalTick ++;
			else		 
				ActionSummery_List.add( new CasualtyStatus (_Action,  _CurrentRPM , _CurrentTick));
		}
		else
		{
			ActionSummery_List.add( new CasualtyStatus (_Action,  _CurrentRPM , _CurrentTick));
		}
	}

	//==========================================================================================================================
	// this not used
	public void IncrmentCasultyActionold(CasualtyAction _Action,int  _CurrentRPM, double  _CurrentTick ,double _Current_SurvivalProbability )
	{		
		for ( CasualtyStatus  CA  : ActiononCasualty_list  ) 
			if (CA.Action == _Action  &&  CA.TotalTick== 0) 
			{  
				CA.CasualtyStatusupdate(  _Current_SurvivalProbability  ,  _CurrentRPM ,   _CurrentTick) ;
				break ;}	
			else if (CA.Action == _Action  &&  CA.TotalTick != 0  ) 
			{  CA.TotalTick++  ; break ;}			

	}



	//==========================================================================================================================
	//2- called each tick   not used 
	public void ActionSummery222( CasualtyAction _Action,int  _CurrentRPM, double  _CurrentTick)
	{
		int LastActionindex=0;

		if (ActionSummery_List.size()!=0 )
		{
			LastActionindex = ActionSummery_List.size() -1;
			CasualtyStatus LastAction= ActionSummery_List.get(LastActionindex) ;

			LastAction.TotalTick= (int) (_CurrentTick- LastAction.CurrentTick) ;

			ActionSummery_List.add( new CasualtyStatus (_Action,  _CurrentRPM , _CurrentTick));
		}
		else
		{
			ActionSummery_List.add( new CasualtyStatus (_Action,  _CurrentRPM , _CurrentTick));
		}




	}


	//************************************************************************************************************************	


	public void ReportaboutSurvivalProbability(boolean PrintFile2 , FileWriter fw ) throws IOException
	{


		//-----------------------
		if ( PrintFile2 )			
		{
			fw.append(casualty.ID  +"\t" );

			for ( int i=0 ; i<=21; i++)
				fw.append( ActiononCasualty_list[i].Current_SurvivalProbability+"\t");

			fw.append("\n" );

		}

	}


	public void ReportaboutAction2()
	{
		System.out.println(casualty.ID +":  " + ActionSummery_List.size() + " - " +casualty.Life );	
	}


}
