package com.sb.gb3pick;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.sb.widget.EventViewer;
import com.sb.widget.TeamInfoView;
//import com.sb.widget.TeamInfoView;
import com.sb.gb3pick.Prediction3Goal;
import com.sb.gb3pick.R;

import com.sb.DataFeed;
import com.sb.FeedEntry;
import com.sb.GameServerConnection;
import com.sb.Goal;
import com.sb.Jackpot;
import com.sb.Match;
import com.sb.MatchState;
import com.sb.MatchStatus;
import com.sb.MatchTime;
import com.sb.MatchTimerState;
import com.sb.Prediction;
import com.sb.SBTime;
import com.sb.Team;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ViewAnimator;
import android.widget.AdapterView.OnItemSelectedListener;

public class GB3PickMinimalistActivity extends Activity
{
	public int liveBetAdvanceFactor=15;
	public int clockAdvanceFactor=15;
	public Boolean liveBetAdvanceMinimum=false;
	
	protected GB3PredictionFactory predictionFactory = new GB3PredictionFactory();
	
	public Prediction3Goal currentBet=new Prediction3Goal();
//	public Bet[] selectedBets=new Array();
	
	protected Boolean waitingForTheHalfHack=false;
	protected Boolean bettingSelectedSlots=false;
	protected Boolean testMode=true;
	public Boolean freePlayMode=false;
	protected int selectedFutureMatchId=-1;
	protected String gameConfig="1";
	public int biaModeMatchId=0;
	
	public float stakeMultiple=2;
	public float stakeMinimum=2;
	public float stakeMaximum=99;

	public int potUpdateRate=300;
	public int clockUpdateRate=60;
	public int statusTimeout=20;
	public int matchStatusUpdateRate=300;
	public int gameFeedUpdateRate=60;
	
	protected GameServerConnection server = null;
	protected boolean initialConfigReceived = false;
	protected boolean initialMatchListReceived = false;
	
	protected View viewStatusBar = null;
	protected View infoBar = null;
	
	protected TextView viewStatus = null;
	protected TextView viewAccount = null;
	protected TextView viewPot=null;

	protected ProgressBar viewProgress = null;
	protected Animation animSlideIn = null;
	protected Animation animSlideOut = null;
	protected EventFeedPanel eventFeedPanel = null;
	protected TeamInfoView team1Details = null;
	protected TeamInfoView team2Details = null;
	
	public Match currentMatch = null;
	public MatchTime currentBetTime=new MatchTime();
	public MatchTime time=new MatchTime();
	public Boolean predictionCheckLooper=false;
	
	public boolean showingFinalWhistle=false;
	protected boolean liveViewEnabled=true;
	protected int matchStatusUpdateTimerDelay=0;

	protected int currentBetSlotNo=-1;
	
	public static final int BET_INTERVAL_LEN_MINS=5;
	
	protected Timer gameFeedTimer = null;
	protected Timer lookForLiveTimer = null;
	protected Timer clockUpdateTimer = null;
	protected Timer initialConnectionTimer = null;
	protected Timer statusTimeoutTimer = null;
	protected Timer potUpdateTimer = null;
	protected Timer matchStatusUpdateTimer = null;
	protected Timer postResultMatchListUpdateTimer = null;
	public boolean timerStopped=false;
	
	protected ArrayList<Prediction> selectedBets=null;

	protected AlertDialog selectMatchDialog = null;
	protected ViewAnimator mainContent = null;
	protected View loginView = null;
	protected View placeBetView = null;
	protected View confirmBetView = null;
	protected View trackerView = null;
	protected View resultsView=null;
	protected View fireworksView=null;
	
	TextView confirmTitleView = null;
	TextView confirmPredictionView = null;	
	
	protected TextView placeBetTitle=null;
	protected Spinner mainSpinner = null;
	protected Spinner jackpot1Spinner = null;
	protected Spinner jackpot2Spinner = null;
	protected Button placeBetButton = null;
	protected Button luckyDipButton = null;
		
	protected Button resultsSelectEarlier = null;
	protected Spinner resultsSelectSpinner = null;
	protected Button resultsSelectLater = null;
	protected TextView resultsSelectedName = null;
	protected TextView resultsViewTitle = null;
	
	private boolean initialLoginAttempt;
	private boolean needGameFeed=false;
	protected ArrayAdapter<SlotSpinnerData> betTimeAdapter;
	protected ArrayAdapter<MatchSpinnerData> allMatchAdapter;

	protected TextView trackerTeam1Name=null;
	protected TextView trackerTeam1Score=null;
	protected TextView trackerTeam2Name=null;
	protected TextView trackerTeam2Score=null;
	protected View trackerScoreView = null;

	protected ResultsViewPanel resultsViewPanel=null;
	protected ResultsDetailPanel resultsDetailPanel=null;
	protected ViewAnimator resultsContent = null;
	
	private String fullVersionString="";
	
	protected String currentName = null;
	protected String currentPassword = null;

	public enum ErrorLevel {
		NONE, WARNING, ERROR, FATAL
	}
	class MatchSpinnerData {
		public MatchSpinnerData(String spinnerText, int matchId)
		{
			this.spinnerText = spinnerText;
			this.matchId = matchId;
		}
		
		public int getMatch() {
			return matchId;
		}

		public String toString() {
			return spinnerText;
		}

		String	spinnerText;
		int		matchId;
	}
	
	class SlotSpinnerData {
		public SlotSpinnerData( String spinnerText, int matchId, int teamId, int betSlotId )
		{
			this.spinnerText = spinnerText;
			this.matchId = matchId;
			this.teamId = teamId;
			this.betSlotId = betSlotId;
		}

		public String getSpinnerText() {
			return spinnerText;
		}

		public int getTeamId() {
			return teamId;
		}

		public int getBetSlotId() {
			return teamId;
		}

		public int getMatch() {
			return matchId;
		}

		public String toString() {
			return spinnerText;
		}

		String	spinnerText;
		int		betSlotId;
		int		teamId;
		int		matchId;
	};
	
	protected boolean forcedResultSpinner = false;
	
	public void forceResultSpinner(int ind)
	{
		Log.d("GB3P", "frs "+Integer.toString(ind));
		forcedResultSpinner = true;
		resultsSelectSpinner.setSelection(ind);
	}
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		Log.d("GB3Pick", "GoalBuster::onCreate()");
		super.onCreate(savedInstanceState);
		
// set up server connection
		server = new GameServerConnection(
			this, gameServerHandler,
			R.string.template_user_agent,
			R.string.url_gameapi,
			R.string.url_wnsapi,
			freePlayMode,
			clockAdvanceFactor,
			R.raw.mykeystore,
			getResources().getString(R.string.keystore_pass)
		);

// create the main interface
		setContentView(R.layout.main);
		
//		gameClock = new DigitalGameClock(this);
//		team2Details = new TeamInfoView(this);
//		team1Details = new TeamInfoView(this);
		
		animSlideIn = AnimationUtils.loadAnimation(this, R.anim.slide_in);
		animSlideOut = AnimationUtils.loadAnimation(this, R.anim.slide_out);
		viewStatusBar = findViewById(R.id.status_bar);
		viewProgress = (ProgressBar) findViewById(R.id.progress);
		viewStatus = (TextView) findViewById(R.id.status_view);
		viewAccount = (TextView) findViewById(R.id.account_view);
		viewPot = (TextView) findViewById(R.id.pot_view);
		
		infoBar = findViewById(R.id.info_bar);
		
		fullVersionString = getString(R.string.gb_version_name) + ", "+getString(R.string.gb_version_number);
		
// set up main content
		mainContent = (ViewAnimator)findViewById(R.id.main_content);
    	Animation mainContentInAnim = new AlphaAnimation(0, 1);
    	mainContentInAnim.setDuration(1000);
    	Animation mainContentOutAnim = new AlphaAnimation(1, 0);
    	mainContentOutAnim.setDuration(1000);

    	mainContent.setInAnimation(mainContentInAnim);
    	mainContent.setOutAnimation(mainContentOutAnim);
    	
// set up login screen
    	loginView = mainContent.findViewById(R.id.login_view);
    	
    	final EditText nameView = (EditText) mainContent.findViewById(R.id.input_username);
		final EditText passwdView = (EditText) mainContent.findViewById(R.id.input_password);
//		final TextView loginStatusView = (TextView) mainContent.findViewById(R.id.login_error);
//		loginStatusView.setText("");
		Button	loginButton = (Button) mainContent.findViewById(R.id.login_button);
		loginButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v)
			{
				String nm = nameView.getText().toString();
				String pw = passwdView.getText().toString();
				
				if (nm.length() > 0) {
//					loginStatusView.setText("logging in ...");
					showConnecting("logging in ...");
					currentName = nm;
					currentPassword = pw;
//					getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
					InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(nameView.getWindowToken(), 0);
					imm.hideSoftInputFromWindow(passwdView.getWindowToken(), 0);
					server.login(nm, pw);
				} else {
					showError(999, "user name must be non-null");
//					loginStatusView.setText("user name must be non-null");
				}
			}
		});

// set up bet screen
	   	placeBetView = mainContent.findViewById(R.id.place_bet_view);
	   	placeBetTitle = (TextView) mainContent.findViewById(R.id.place_bet_title);
		// Create an ArrayAdapter using the string array and a default spinner layout
	   	
	   	
	   	betTimeAdapter = 
            new ArrayAdapter<SlotSpinnerData>( 
                this,
                R.layout.slot_spinner_item);
		betTimeAdapter.setDropDownViewResource(R.layout.slot_spinner_dropdown_item);
		
		mainSpinner = (Spinner) mainContent.findViewById(R.id.main_bet_selector);
		jackpot1Spinner = (Spinner) mainContent.findViewById(R.id.jackpot1_selector);
		jackpot2Spinner = (Spinner) mainContent.findViewById(R.id.jackpot2_selector);
		placeBetButton = (Button) mainContent.findViewById(R.id.place_bet_button);
		luckyDipButton = (Button) mainContent.findViewById(R.id.lucky_dip_button);
		placeBetButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v)
			{
				displayConfirmSingleBet(currentBet);
			}
		});
		luckyDipButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v)
			{
				generateLuckyDip(currentBet);
			}
		});
		
		// set up results screen

	   	resultsView = mainContent.findViewById(R.id.results_view);
	   	resultsDetailPanel = (ResultsDetailPanel) mainContent.findViewById(R.id.results_detail_panel);
		resultsViewPanel = (ResultsViewPanel) resultsView.findViewById(R.id.results_view_panel);
		resultsSelectEarlier = (Button) mainContent.findViewById(R.id.results_select_earlier);
		resultsSelectSpinner = (Spinner) mainContent.findViewById(R.id.results_select_spinner);
		resultsSelectLater = (Button) mainContent.findViewById(R.id.results_select_later);
		resultsSelectedName = (TextView) mainContent.findViewById(R.id.results_selected_name);
		resultsViewTitle = (TextView) mainContent.findViewById(R.id.results_view_title);
		resultsContent = (ViewAnimator) mainContent.findViewById(R.id.results_view_content);
    	resultsContent.setInAnimation(mainContentInAnim);
    	resultsContent.setOutAnimation(mainContentOutAnim);

	   	allMatchAdapter = 
            new ArrayAdapter<MatchSpinnerData>( 
                this,
                R.layout.results_spinner_item);
	   	allMatchAdapter.setDropDownViewResource(R.layout.results_spinner_dropdown_item);
		resultsSelectSpinner.setAdapter(allMatchAdapter);
		
		resultsSelectSpinner.setOnItemSelectedListener(new OnItemSelectedListener() 
		{
		    @Override
		    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) 
		    {
		    	if (forcedResultSpinner) {
		    		forcedResultSpinner = false;
		    		return;
		    	}
		    	Log.d("GB3P", "Item selected");
		    	int npos = rvCheck(position);
				if (npos != position) {
					forceResultSpinner(npos);
				}
		    }
	
		    @Override
		    public void onNothingSelected(AdapterView<?> parentView)
		    {
		    	;
		    }
		});
		
		resultsSelectEarlier.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v)
			{
		    	Log.d("GB3P", "onclick -");
				int position = resultsSelectSpinner.getSelectedItemPosition();
				int npos = rvCheck(position+1);
				if (npos != position) {
					forceResultSpinner(npos);
				}
			}
		});
		
		resultsSelectLater.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v)
			{
		    	Log.d("GB3P", "onclick +");
				int position = resultsSelectSpinner.getSelectedItemPosition();
				int npos = rvCheck(position-1);
				if (npos != position) {
					forceResultSpinner(npos);
				}
			}
		});
		
		mainSpinner.setAdapter(betTimeAdapter);
		jackpot1Spinner.setAdapter(betTimeAdapter);
		jackpot2Spinner.setAdapter(betTimeAdapter);
		
		mainSpinner.setOnItemSelectedListener(new OnItemSelectedListener() 
		{
			void onBetSlot1Select()
			{
				SlotSpinnerData sd1 = (SlotSpinnerData) mainSpinner.getSelectedItem();
				SlotSpinnerData sd2 = (SlotSpinnerData) jackpot1Spinner.getSelectedItem();
				SlotSpinnerData sd3 = (SlotSpinnerData) jackpot2Spinner.getSelectedItem();
				int sid1 = (sd1 != null)?sd1.betSlotId:0;
				int sid2 = (sd2 != null)?sd2.betSlotId:0;
				int sid3 = (sd3 != null)?sd3.betSlotId:0;
				if (sid1 <= 0) {
					placeBetButton.setEnabled(false);
					return;
				}
				if (sid1 == sid2) {
					jackpot1Spinner.setSelection(0);
					sid2 = 0;
				}
				if (sid1 == sid3) {
					jackpot2Spinner.setSelection(0);
					sid3 = 0;
				}
				Log.d("GB3P", Integer.toString(sid1)+","+Integer.toString(sid2)+","+Integer.toString(sid3)+",1");
				if (sid1 > 0 && sid2 > 0 && sid3 > 0) {
					placeBetButton.setEnabled(true);
				} else {
					placeBetButton.setEnabled(false);
				}
				onBetSlotSelect(sid1, sid2, sid3);
			}
			
		    @Override
		    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) 
		    {
		    	SlotSpinnerData d = betTimeAdapter.getItem(position);
		    	if (d != null) {
		    		onBetSlot1Select();
		    	}
                //.setText( d.getValue() );
		    }

		    @Override
		    public void onNothingSelected(AdapterView<?> parentView) 
		    {
		    }
		});
	   	
		jackpot1Spinner.setOnItemSelectedListener(new OnItemSelectedListener() 
		{
			void onBetSlot2Select()
			{
				SlotSpinnerData sd1 = (SlotSpinnerData) mainSpinner.getSelectedItem();
				SlotSpinnerData sd2 = (SlotSpinnerData) jackpot1Spinner.getSelectedItem();
				SlotSpinnerData sd3 = (SlotSpinnerData) jackpot2Spinner.getSelectedItem();
				int sid1 = (sd1 != null)?sd1.betSlotId:0;
				int sid2 = (sd2 != null)?sd2.betSlotId:0;
				int sid3 = (sd3 != null)?sd3.betSlotId:0;
				if (sid2 <= 0) {
					placeBetButton.setEnabled(false);
					return;
				}
				if (sid2 == sid3) {
					sid3 = 0;
					jackpot2Spinner.setSelection(0);
				}
				if (sid1 == sid2) {
					jackpot1Spinner.setSelection(0);
					sid2 = 0;
				}
				Log.d("GB3P", Integer.toString(sid1)+","+Integer.toString(sid2)+","+Integer.toString(sid3)+",2");
				if (sid1 > 0 && sid2 > 0 && sid3 > 0) {
					placeBetButton.setEnabled(true);
				} else {
					placeBetButton.setEnabled(false);
				}
				onBetSlotSelect(sid1, sid2, sid3);
			}
			
		    @Override
		    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) 
		    {
		    	SlotSpinnerData d = betTimeAdapter.getItem(position);
		    	if (d != null) {
		    		onBetSlot2Select();
		    	}
                //.setText( d.getValue() );
		    }

		    @Override
		    public void onNothingSelected(AdapterView<?> parentView) 
		    {
		    }
		});
	   	
		jackpot2Spinner.setOnItemSelectedListener(new OnItemSelectedListener() 
		{
			void onBetSlot3Select()
			{
				SlotSpinnerData sd1 = (SlotSpinnerData) mainSpinner.getSelectedItem();
				SlotSpinnerData sd2 = (SlotSpinnerData) jackpot1Spinner.getSelectedItem();
				SlotSpinnerData sd3 = (SlotSpinnerData) jackpot2Spinner.getSelectedItem();
				int sid1 = (sd1 != null)?sd1.betSlotId:0;
				int sid2 = (sd2 != null)?sd2.betSlotId:0;
				int sid3 = (sd3 != null)?sd3.betSlotId:0;
				if (sid3 <= 0) {
					placeBetButton.setEnabled(false);
					return;
				}
				if (sid1 == sid3) {
					jackpot2Spinner.setSelection(0);
					sid2 = 0;
				}
				if (sid2 == sid3) {
					jackpot1Spinner.setSelection(0);
					sid3 = 0;
				}
				Log.d("GB3P", Integer.toString(sid1)+","+Integer.toString(sid2)+","+Integer.toString(sid3)+",3");
				if (sid1 > 0 && sid2 > 0 && sid3 > 0) {
					placeBetButton.setEnabled(true);
				} else {
					placeBetButton.setEnabled(false);
				}
				onBetSlotSelect(sid1, sid2, sid3);
			}
			
		    @Override
		    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) 
		    {
		    	SlotSpinnerData d = betTimeAdapter.getItem(position);
		    	if (d != null) {
		    		onBetSlot3Select();
		    	}
                //.setText( d.getValue() );
		    }

		    @Override
		    public void onNothingSelected(AdapterView<?> parentView) 
		    {
		    }
		});
	   	
// set up confirm bet screen
	   	confirmBetView = mainContent.findViewById(R.id.confirm_bet_view);	
	   	confirmTitleView = (TextView) mainContent.findViewById(R.id.confirm_bet_title);	
	   	confirmPredictionView = (TextView) mainContent.findViewById(R.id.confirm_bet_prediction);	

	   	Button	confirmBetButton = (Button) mainContent.findViewById(R.id.confirm_bet_button);
		confirmBetButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v)
			{
				if (currentMatch == null || currentMatch.homeTeam == null || currentMatch.awayTeam == null) {
					showError(666, "Internal error .. null match while confirming current bet");
					return;
				}
				showConnecting("Place bet ...");
				Log.d("GB3P", Integer.toString(currentBet.betSlotId1)+","+Integer.toString(currentBet.betSlotId2)+","+Integer.toString(currentBet.betSlotId3)+",!!!");

				server.playParimutuelTicket(
						currentBet.matchId,
						currentBet, currentMatch.predictions);
//					{betSlotId:currentBet.betSlotId,betSlotId2:currentBet.betSlotId2, betSlotId3:currentBet.betSlotId3}
			}
		});
		
		Button	cancelBetButton = (Button) mainContent.findViewById(R.id.cancel_bet_button);
		cancelBetButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v)
			{
				showBetView();
			}
		});
		
// set up data feed screen
	   	trackerView = mainContent.findViewById(R.id.tracker_view);
	   	
	   	trackerScoreView = mainContent.findViewById(R.id.tracker_score);		
	   	trackerTeam1Name=(TextView) mainContent.findViewById(R.id.tracker_team1_name);
		trackerTeam1Score=(TextView) mainContent.findViewById(R.id.tracker_team1_score);
		trackerTeam2Name=(TextView) mainContent.findViewById(R.id.tracker_team2_name);
		trackerTeam2Score=(TextView) mainContent.findViewById(R.id.tracker_team2_score);

		eventFeedPanel = (EventFeedPanel) mainContent.findViewById(R.id.tracker_game_feed);	
		
	   	Button	efAllButton = (Button) mainContent.findViewById(R.id.event_track_all_button);
	   	efAllButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v)
			{
				setGameFeed(null);
			}
		});
	   	Button	efGoalButton = (Button) mainContent.findViewById(R.id.event_track_goal_button);
	   	efGoalButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v)
			{
				setGameFeed(Arrays.asList(FeedEntry.GOAL_EVENT, FeedEntry.CORNER_EVENT, FeedEntry.FREEKICK_EVENT, FeedEntry.SHOT_EVENT, FeedEntry.SAVE_EVENT));
			}
		});
	   	Button	efFoulButton = (Button) mainContent.findViewById(R.id.event_track_foul_button);
	   	efFoulButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v)
			{
				setGameFeed(Arrays.asList(FeedEntry.REDCARD_EVENT, FeedEntry.YELLOWCARD_EVENT, FeedEntry.CORNER_EVENT, FeedEntry.FOUL_EVENT, FeedEntry.PENALTY_EVENT));
			}
		});
// set up fireworks screen
	   	fireworksView = mainContent.findViewById(R.id.fireworks_view);
	   	
// kick start the whole shebang
	   	initialLoginAttempt = true;
	   	makeInitialConnection();
	}

	private void makeInitialConnection()
	{
		showConnecting("Initial Connection ...");
		Log.d("GB3P", "Initial Connect");
		initialConfigReceived = false;
		initialMatchListReceived = false;
		mainContent.setVisibility(View.INVISIBLE);
		mainContent.setDisplayedChild( mainContent.indexOfChild(placeBetView));
		server.getGameProperties("1");
	}

	/**
	 * The activity is about to become visible.
	 */
	@Override
	protected void onStart()
	{
		super.onStart();
	}
	
	/** 
	 * The activity has become visible (it is now "resumed").
	 */
   @Override
   protected void onResume()
   {
		super.onResume();
   }
	
   /**
	*  Another activity is taking focus (this activity is about to be "paused").
	*/
	@Override
	protected void onPause()
	{
		super.onPause();
	}
	
	/**
	 * The activity is no longer visible (it is now "stopped")
	 */
	@Override
	protected void onStop()
	{
		super.onStop();
	}
	
	/**
	 * The activity is about to be destroyed.
	 */
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean onPrepareOptionsMenu (Menu menu)
	{
		MenuItem miResults = menu.findItem(R.id.gb_match_results);
		MenuItem miLogout = menu.findItem(R.id.gb_logout);
		MenuItem miTracker = menu.findItem(R.id.gb_match_tracker);
		MenuItem miBet = menu.findItem(R.id.gb_match_bet);
		MenuItem miSelect = menu.findItem(R.id.gb_select_match);
		MenuItem miAbout = menu.findItem(R.id.gb_about);
		if (mainContent.getCurrentView() == loginView) {
			if (miResults != null) miResults.setVisible(true);
			if (miLogout != null) miLogout.setVisible(false);
			if (miTracker != null) miTracker.setVisible(false);
			if (miBet != null) miBet.setVisible(false);
			if (miSelect != null) miSelect.setVisible(false);
			if (miAbout != null) miAbout.setVisible(true);
		} else if (mainContent.getCurrentView() == placeBetView) {
			if (miResults != null) miResults.setVisible(true);
			if (miLogout != null) miLogout.setVisible(true);
			if (miTracker != null) miTracker.setVisible(true);
			if (miBet != null) miBet.setVisible(false);
			if (miSelect != null) miSelect.setVisible(true);
			if (miAbout != null) miAbout.setVisible(false);
		} else if (mainContent.getCurrentView() == confirmBetView) {
			if (miResults != null) miResults.setVisible(true);
			if (miLogout != null) miLogout.setVisible(false);
			if (miTracker != null) miTracker.setVisible(false);
			if (miBet != null) miBet.setVisible(true);
			if (miSelect != null) miSelect.setVisible(false);
			if (miAbout != null) miAbout.setVisible(false);
		} else if (mainContent.getCurrentView() == trackerView) {
			if (miResults != null) miResults.setVisible(true);
			if (miLogout != null) miLogout.setVisible(true);
			if (miTracker != null) miTracker.setVisible(false);
			if (miBet != null) miBet.setVisible(true);
			if (miSelect != null) miSelect.setVisible(true);
			if (miAbout != null) miAbout.setVisible(false);
		} else if (mainContent.getCurrentView() == resultsView) {			
			if (miResults != null) miResults.setVisible(false);
			if (miLogout != null) miLogout.setVisible(true);
			if (miTracker != null) miTracker.setVisible(true);
			if (miBet != null) miBet.setVisible(true);
			if (miSelect != null) miSelect.setVisible(false);
			if (miAbout != null) miAbout.setVisible(false);
		} else {
			if (miResults != null) miResults.setVisible(false);
			if (miLogout != null) miLogout.setVisible(true);
			if (miTracker != null) miTracker.setVisible(false);
			if (miBet != null) miBet.setVisible(true);
			if (miSelect != null) miSelect.setVisible(true);
			if (miAbout != null) miAbout.setVisible(false);
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId()) {
			case R.id.gb_match_results: {
				//resultsViewSelectedMatch = null;
				//resultsViewSelectedMatchId = -1;
				if (currentMatchId() != -1 && resultsViewSelectedMatchId == -1) {
					resultsViewSelectedMatchId = currentMatchId();
					displayResultsMatchId(resultsViewSelectedMatchId);
					forceResultSpinnerId(resultsViewSelectedMatchId);
				}
				showResultsView();
				return true;
			}
			case R.id.gb_match_bet: {
				showBetView();
				return true;
			}
			case R.id.gb_match_tracker: {
				showTracker();
				return true;
			}
			case R.id.gb_select_match: {
				showSelect();
				return true;
			}
			case R.id.gb_help: {
				showHelp();
				return true;
			}
			case R.id.gb_about: {
				showAbout();
				return true;
			}
			case R.id.gb_preferences: {
				showPreferences();
				return true;
			}
			case R.id.gb_logout: {
				if (server != null) {
					currentName = null;
					currentPassword = null;
					clearCurrentLogin();
					server.logout();
				}
				return true;
			}
			default: {
				Log.d("option", Integer.toString(item.getItemId()));
			}
		}
		return false;
	}

	public boolean retrieveCurrentLogin()
	{
		SharedPreferences settings = getPreferences(MODE_PRIVATE);
		String name = settings.getString("login_name", null);
		String password = settings.getString("login_password", null);
		if (name == null || password == null) {
			return false;
		}
		currentName = name;
		currentPassword = password;
		return true;
	}
	
	public void storeCurrentLogin(String name, String password)
	{
		SharedPreferences settings = getPreferences(MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("login_name", name);
		editor.putString("login_password", password);
		editor.commit();
	}
	
	public void clearCurrentLogin()
	{
		SharedPreferences settings = getPreferences(MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.remove("login_name");
		editor.remove("login_password");
		editor.commit();
	}

/***************************************
 * ANIMATION LISTENER IMPLEMENTATIONS
 ***************************************/
	public void onAnimationEnd(Animation animation)
	{
 //   	if (animation == animSlideIn) {
			viewProgress.setVisibility(View.VISIBLE);
 //   	}
	}

	public void onAnimationRepeat(Animation animation)
	{
	}

	public void onAnimationStart(Animation animation)
	{
	}
	
/***************************************
 * SERVER RESULT HANDLERS
 ***************************************/
	protected Handler gameServerHandler = new Handler()
	{
		public void handleMessage(Message msg)
		{
			switch (msg.what) {
			case GameServerConnection.SERVER_ACCOUNT_EVENT: {
				showAccountBalance();
				break;
			}
			case GameServerConnection.SERVER_CONFIG_EVENT: {
				Log.d("GB3P", "Initial Config");
				if (!initialConfigReceived) {
					showStatus("Received initial configuration");
					onInitialConfig();
				}
				hideConnecting();
				break;
			}
			case GameServerConnection.SERVER_UPDATE_JACKPOT_EVENT: {
				//trace("got jackpot update");
				//gameResultsPanel.updateJackpots(server.jackpotList());
				break;
			}
			case GameServerConnection.SERVER_5R_JACKPOT_EVENT: {
				//trace("got 5r jackpot update");
				//gameResultsPanel.updateJackpots(server.jackpotList());
				break;
			}
			case GameServerConnection.SERVER_PLAY_TICKET_EVENT: {
				onPlayTicketResponse();
				break;
			}
			case GameServerConnection.SERVER_LIST_PREDICTIONS_EVENT: {
				onListPredictionsReceived();
				break;
			}
			case GameServerConnection.SERVER_LAST_PAYOUT_EVENT: {
				//trace("got last payout");
				//gameResultsPanel.updateJackpots(server.jackpotList());
				break;
			}
			case GameServerConnection.SERVER_LIST_MATCHES_EVENT: {
				showStatus("Match List Received");
				if (!initialMatchListReceived) {
					onInitialMatchListReceived();
				} else {
					onMatchListReceived();
				}
				hideConnecting();
				break;
			}
			case GameServerConnection.SERVER_MATCH_RESULT_EVENT: {
				onMatchResultReceived();
				break;
			}
			case GameServerConnection.SERVER_MATCH_STATUS_EVENT: {
				onMatchStatusReceived();
				showMatchPot();
				break;
			}
			case GameServerConnection.SERVER_MATCH_POT_EVENT: {
				showMatchPot();
				break;
			}
			case GameServerConnection.SERVER_ERROR_EVENT: {
				onServerError();
				break;
			}
			case GameServerConnection.SERVER_LOGIN_EVENT: {
				onServerLogin();
				break;
			}
			case GameServerConnection.SERVER_LOGOUT_EVENT: {
				onServerLogout();
				break;
			}
			case GameServerConnection.SERVER_GAME_FEED_EVENT: {
				onServerGameFeed();
				break;
			}
			case GameServerConnection.SERVER_TEAM_DETAILS_EVENT: {
				onServerTeamDetails();
				break;
			}
			case GameServerConnection.SERVER_LIST_ALL_RESULTS_EVENT: {
				onServerListAllResults();
				break;
			}
			case GameServerConnection.SERVER_TICKET_DETAILS_EVENT: {
				Prediction3Goal p = (Prediction3Goal) server.ticketDetailsPrediction;
				Match m;
				if (p != null) {
					m = server.findActiveMatch(p.matchId);
					if (m == null) m = server.findResultsMatch(p.matchId);
					if (m != null) resultsDetailPanel.setPrediction(m, p);
				}
				break;
			}
			case GameServerConnection.SERVER_LIST_ALL_TEAMS_EVENT: {
				onServerListAllTeams();
				break;
			}
			default: {
				showError(668, "Unknown message from server!");
			}
			}
		}


	};

	protected Boolean hasFullTeamList = false;
	protected Boolean requestingPredictionResults = false;
	protected int lprLastListSize = 0;
	protected Date rfd = null;
	protected Date rtd = null;

	public Boolean listAllPredictionResults(
			Date resultsFromDate,
			Date resultsToDate)
	{
		if (server == null) {
			return false;
		}
		if (server.allResultsList == null) {
			lprLastListSize = 0;
		} else {
			lprLastListSize = server.allResultsList.size();
		}
		requestingPredictionResults = true;
		if (!hasFullTeamList) {
			rfd = resultsFromDate;
			rtd = resultsToDate;
			server.listAllTeams();
			return true;
		}
		server.listAllPredictionResults(predictionFactory, resultsFromDate, resultsToDate);
		return true;
	}

	public Boolean listAllPredictionResults()
	{
		return listAllPredictionResults(null, null);
	}
	
	protected void showMatchPot()
	{
		if (server.matchPotId == currentMatchId()) {
			infoBar.setVisibility(View.VISIBLE);
			viewPot.setText(formatCurrency(server.matchPot, server.matchPotCurrency, true, 2));
		}
	}

	private void onServerListAllTeams()
	{
//		dispatchEvent(e);
		hasFullTeamList = true;
		if (requestingPredictionResults) {
			server.listAllPredictionResults(predictionFactory, rfd, rtd);
		}
	}

	protected void onServerListAllResults()
	{
		Log.d("GB3P", "on list all res");
		requestingPredictionResults = false;
		int lprCurrentListSize = ((server.allResultsList == null)?0:server.allResultsList.size());
		int currentResMatchId = -1;
		MatchSpinnerData msd = null;
		int ind = resultsSelectSpinner.getSelectedItemPosition(); // rebuilding index clear selection ...
		if (ind >= 0 && ind <allMatchAdapter.getCount()) {
			msd = allMatchAdapter.getItem(ind);
			currentResMatchId = msd.matchId;
		}
		
		if (lprCurrentListSize <= lprLastListSize || lprLastListSize == 0) {
			if (currentResMatchId == -1 && ind > 0 && allMatchAdapter.getCount() >= 2) {
				ind = allMatchAdapter.getCount()-2;
				msd = allMatchAdapter.getItem(ind);
				currentResMatchId = msd.matchId;
				forceResultSpinner(ind);
			}
		} else {		
			Log.d("GB3P", "got some new ones ind "+Integer.toString(ind)+" cls "+Integer.toString(lprCurrentListSize)+" cls "+Integer.toString(lprLastListSize));
			setupResultsMatchSelect(-1);
			if (allMatchAdapter.getCount() > 1 && ind > allMatchAdapter.getCount()-2) {
				ind = allMatchAdapter.getCount()-2;
			} else if (ind > allMatchAdapter.getCount()-1) {
				ind = allMatchAdapter.getCount()-1;
			}
			if (ind >= 0 && ind < allMatchAdapter.getCount()) {
				msd = allMatchAdapter.getItem(ind);
				currentResMatchId = msd.matchId;
				forceResultSpinner(ind);
			}

/*
			if (ind >= 0 && ind >= resultsSelectSpinner.getCount()-1) {
				if (ind >= 0 && ind < resultsSelectSpinner.getCount()-1) {
					ind++;
					msd = allMatchAdapter.getItem(ind);
					currentResMatchId = msd.matchId;
					forceResultSpinner(ind);
				} else {
					setResultsMatchTo(currentResMatchId);				
				}
			} else {
				setupResultsMatchSelect(currentResMatchId);
				displayResultsMatchId(currentResMatchId);
			}
*/
		}
		displayResultsMatchId(currentResMatchId);
		lprLastListSize = lprCurrentListSize;
/* original
		for (Match m: server.allResultsList) {
			resultsViewPanel.addPredictions(m.predictions, m.name);
		}
*/
	}
	
	protected void onServerLogin()
	{
		storeCurrentLogin(currentName, currentPassword);
		makeInitialConnection();
	}

	protected void onServerLogout()
	{
		initialMatchListReceived = false;
		initialConfigReceived = false;
		showLogin("");
	}

	protected void onServerGameFeed()
	{
//		if (eventSlider != null) eventSlider.clearFeed();
		if (eventFeedPanel != null) eventFeedPanel.clear();
//		if (m != undefined && m!=null) {
//			window.alert('displayEventFeed('+m+','+m.feed+')');
//		}
		
		setGameFeed(currentFilter);
		
//		if (eventSlider != null) eventSlider.addFeed(m.feed);
//		if (m.status != null) {
//			if (useMatchStatusGoals || m.feed.nGoals() < m.status.nGoals()) {
//				addEventSliderGoals(m.status.goalList());
//			}
//		}
	}
	
	protected List<Integer> currentFilter = null;
	public void setGameFeed(List<Integer> filter)
	{
		currentFilter = filter;
		Match m = server.feedDetailsMatch;
		if (m == null || m.feed == null) {
			return;
		}
		if (eventFeedPanel != null) {
			eventFeedPanel.clear();
			for (FeedEntry f: m.feed) {
				if (filter == null || filter.contains(f.kind)) {
					eventFeedPanel.addItem(f, -1);
				}
			}
		}
	
	}
	
	Boolean teamDetailsAvailable = true; // assume there is
	protected void onServerTeamDetails()
	{
		if (server.teamDetailsMatch != null && currentMatch != null && server.teamDetailsMatch.matchId == currentMatch.matchId) {
			displayTeamDetails(server.teamDetailsMatch);
			
			if (server.teamDetailsMatch.homeTeam.players == null || server.teamDetailsMatch.awayTeam.players == null) {
				teamDetailsAvailable = false; // we asked, and there isn't any
			}
			if (needGameFeed) {
				startGameFeed(currentMatch);
			}
		}
	}
	
	private void displayTeamDetails(Match teamDetailsMatch)
	{
		if (teamDetailsMatch == null) {
			return;
		}
//		if (teamStatsPanel != null) teamStatsPanel.setMatch(m);
	}

	public Boolean startGameFeed(final Match m)
	{
		stopGameFeed();
		if (m == null || m.homeTeam == null || m.awayTeam == null) { needGameFeed = false; return false; }
		needGameFeed = true;
		if (m.homeTeam.players == null || m.awayTeam.players == null && teamDetailsAvailable) {
			Log.d("feed", "trying to get the frikking players still");
			server.getTeamDetails(m.matchId);
			return true;
		}
		server.getGameEventFeed(m.matchId);
		if (gameFeedUpdateRate > 0) {
			gameFeedTimer = new Timer();
			gameFeedTimer.schedule(
				new TimerTask() {
					public void run()
					{
						Log.d("feed", "trying to get game feed");
						server.getGameEventFeed(m.matchId);
					}
				}, gameFeedUpdateRate*1000, gameFeedUpdateRate*1000);
		}
//		window.status = "started game feed update";
		return true;
	}
	
	public void  stopGameFeed()
	{
//		window.status = "stopping game feed";
		if (gameFeedTimer != null) {
			gameFeedTimer.cancel();
			gameFeedTimer = null;
		}
	}
	
	
	
	protected void onServerError()
	{
		MatchTime bett=null;
		int slno=0;
		if (initialConnectionTimer != null) {
			initialConnectionTimer.cancel();
			initialConnectionTimer = null;
		}
		Log.d("GB3P", "error "+Integer.toString(server.lastErrorCode)+" ... "+server.lastErrorMessage);
		viewProgress.setVisibility(View.INVISIBLE);
		if (server.lastErrorCode == GameServerConnection.TS_NO_SESSION) {
			if (retrieveCurrentLogin()) {
				initialLoginAttempt = true;
				server.login(currentName, currentPassword);
				return;
			}
			String m = "";
			if (!initialLoginAttempt) {
				m = server.lastErrorMessage;
			}
			initialLoginAttempt = false;
			liveBetEnable(false);
			showLogin(m);
			return;
		} else if (server.lastErrorCode == GameServerConnection.LOGIN_ERROR) {
			String m = server.lastErrorMessage;
			initialLoginAttempt = false;
			liveBetEnable(false);
			showLogin(m);
			return;
		}

		if (server.lastErrorCode == GameServerConnection.BET_IN_THE_PAST &&
				!(currentBet == null) && !bettingSelectedSlots) {
			bett = new MatchTime(currentBetTime);
			if (bett.equals(currentBet.time())) {
				bett.min++;
			}
			slno = time2SlotIdx(bett);
			if (slno == currentBet.timeSlotIdx) {
				++slno;
				if (slno >= 10) {
					slno = -1;
					bett = null;
				} else {
					bett = slotIdx2Time(slno);
					currentBet.timeSlotIdx = slno;
					currentBet.minute = bett.min;
				}
			}
			if (slno >= 0 && !(bett == null)) {
				reBet(currentBet.betSlotId1 + 2, bett, currentBet.time());
			}else {
				showError(server.lastErrorCode, server.lastErrorMessage);
			}
		}else {
			bettingSelectedSlots = false;
			if (!initialConfigReceived) {
				liveBetEnable(false);
//				futureBetEnable(false);
//				showFutureBetPanel();
				showError(server.lastErrorCode, server.lastErrorMessage, ErrorLevel.FATAL);
			}else {
				showError(server.lastErrorCode, server.lastErrorMessage, ErrorLevel.ERROR);
			}
		}
		return;
	}
	
	protected void onPlayTicketResponse()
	{
		if (bettingSelectedSlots) {
			if (playNextSelected()) {
				return;
			}
		}
		hideConnecting();
		showStatus("Bet Successful");
		betControlClearBet();
		server.getPlayerAccount(freePlayMode);
		showBetView();
		if ((selectedBets != null) && selectedBets.size() > 0) {
			clearSelection();
		}
		if (server.currentPrediction == null) {
			Log.d("GB3P", "null server prediction in play ticket return");
			return;
		}
		if (server.currentPredictionList == null) {
			Log.d("GB3P", "null server prediction list in play ticket return");
			return;
		}
		Prediction3Goal p = (Prediction3Goal)server.currentPrediction;
		Match m = server.findActiveMatch(p.matchId);
		if (m == null) {
			Log.d("GB3P", "null match in play ticket return");
			return;
		}
		displayPredictionList(m, server.currentPredictionList);
		currentBet = new Prediction3Goal();
		currentBet.matchId = currentMatchId();
		return;
	}

	private void futureBetEnable(boolean b)
	{
	}

	private void showLiveBetPanel()
	{
		if (liveViewEnabled) {
			showBetView();
		}
	}

	private void showFutureBetPanel() {
	}

	protected void onMatchResultReceived()
	{
		Jackpot j=null;
		Match m=getActiveMatch(currentMatchId());
		Log.d("results", Integer.toString(currentMatchId()));
		if (m == null) {
			return;
		}
		m.setLiveStatus(-1);
		ArrayList<Jackpot> ja=new ArrayList<Jackpot>();
		for (Jackpot jo: server.matchResultJackpots) {
			if (jo.processed) {
				j = server.jackpot4Id(jo.id);
				if (j != null) {
					jo.name = j.name;
					jo.currency = j.currency;
					ja.add(jo);
				}
			}
		}

		showFireworks(
				m, server.matchResultStartTime, server.matchResultWinnings,
				server.matchResultEstimate,
				freePlayMode ? m.status.freePot : m.status.pot,
				server.playerCurrency, server.finalResults(), ja);

		return;
	}

	protected void onInitialConfig()
	{
		showConnecting("Fetching Matches and Account ...");
		server.getPlayerAccount(freePlayMode);
		server.getMatches();
		stakeMinimum = server.minimumBet;
		stakeMaximum = server.maximumBet;
		stakeMultiple = server.minimumBet;
		initialConfigReceived = true;
	}
	
	protected void onInitialMatchListReceived()
	{
		if (initialConnectionTimer != null) {
			initialConnectionTimer.cancel();
			initialConnectionTimer = null;
		}
		/*
		if (biaModeMatchId > 0) {
			trace("in biaMode", biaModeMatchId);
			if (server.reqMatchList() == null || server.reqMatchList().length == 0) {
				futureBetEnable(false);
				trace("no next matches disabling tab");
				if (server.liveMatchList() == null || server.liveMatchList().length == 0) {
					liveBetEnable(false);
					trace("no live matches disabling tab");
				}
			} else {
				selectedFutureMatchId = server.reqMatch(0).matchId;
			}
			liveBetEnable(false);
			hideConnectingView();
			showFutureBetPanel();
		}else {
		}*/
		if (server.nextMatchList() == null || server.nextMatchList().size() == 0) {
			//futureBetEnable(false);
		}else {
			selectedFutureMatchId = server.nextMatch(0).matchId;
		}
		if (server.liveMatchList() == null || server.liveMatchList().size() == 0) {
			liveBetEnable(false);
			setNextMatchStartCheck();
			hideConnecting();
			showFutureBetPanel();
		} else {
			setMatch(server.liveMatch(0));
			liveBetEnable(true);
			hideConnecting();
			showLiveBetPanel();
		}
		initialMatchListReceived = true;
		setupResultsMatchSelect(resultsViewSelectedMatchId);
	}

	protected void onMatchListReceived()
	{

//		trace("got a new match list");
		if (server.nextMatchList() == null || server.nextMatchList().size() == 0) {
			futureBetEnable(false);
			selectedFutureMatchId = -1;
			showLiveBetPanel();
		}
		if (server.liveMatchList() == null || server.liveMatchList().size() == 0) {
			liveBetEnable(false);
			if (!showingFinalWhistle) {
				showFutureBetPanel();
				setNextMatchStartCheck();
			}
		} else {
			liveBetEnable(true);
			if (currentMatchId() < 0 || server.findActiveMatch(currentMatchId()) == null) {
				if (!showingFinalWhistle) {
					setMatch(server.liveMatch(0));
				}
			}
		}
		setupResultsMatchSelect(resultsViewSelectedMatchId);
	}

	private void onListPredictionsReceived()
	{
//		trace("got a new prediction list ... checking");
		if (server.listPredictionResultsMatch == null) {
			return;
		}
		Match m=server.listPredictionResultsMatch;
		checkPredictionsFor(m);
		Log.d("GB3P", "got prediction list for "+m.name);
		displayPredictionList(m, m.predictions);
		return;
	}

	private void onMatchStatusReceived()
	{
		Match m=null;
		int fuseT=0;
		stopMatchStatusTimeoutTimer();
		MatchStatus ms=server.matchStatus;
		if (ms == null) {
//			trace("match status event is null");
			return;
		}
//		trace("match status time", ms.time.toShortString(), "score", ms.t1Score, ms.t2Score, "timestamp", ms.timestamp, "current time", ServerConnection.getTimeStamp());
		if (ms.status.equals(MatchState.ABANDONED) || ms.status.equals(MatchState.CANCELLED) || ms.status.equals(MatchState.DELETED)) {
			ms.time.setTo(1, 0, 0);
		} else if (ms.status.equals(MatchState.OPEN)) {
			ms.time.setTo(1, 0, 0);
		} else if (ms.status.equals(MatchState.FINISHED)) {
			m = getActiveMatch(ms.id);
			if (m != null) {
				checkPredictionsFor(m);
				displayPredictionList(m, m.predictions);
			}
		} else if (ms.status.equals(MatchState.INTERVAL)) {
				if (ms.time.inAddedTime()) {
					ms.time.min = 0;
					ms.time.half++;
				}
		} else if (ms.status.equals(MatchState.PAUSED)) {
		} else if (ms.status.equals(MatchState.PLAYING)) {
		} else {
//				trace("unhandled match state", ms.status);
		}
//		trace("match status adjusted time to", ms.time.toShortString());
		int mtState=updateLiveMatchStatus(
				ms.id, ms.time,
				ms.t1Score, ms.t2Score, ms.status,
				ms.estimate, ms.goalList());
//		trace("prediction check in match status", predictionCheckLooper);
		if (!predictionCheckLooper) {
			livePanelPredictionCheck();
		}
		if (mtState == MatchTimerState.RUNNING) {
//			trace("starting timer update at 60s and match status at 300s");
			startClockUpdateTimer(60 - ms.time.sec);
			if (!waitingForTheHalfHack) {
				fuseT = ms.time.min * 60 + ms.time.sec;
				fuseT = fuseT % matchStatusUpdateRate;
				fuseT = matchStatusUpdateRate - fuseT;
				if (fuseT <= 0) {
					fuseT = matchStatusUpdateRate;
				}
//				trace("timer update in", 60 - ms.time.sec, "secs", "match in", fuseT, "secs");
				startMatchStatusUpdateTimer(fuseT);
			}
		} else if (mtState == MatchTimerState.PAUSED) {
//			trace("starting match status update timer at 60s");
			startMatchStatusUpdateTimer(60);
		} else if (mtState == MatchTimerState.FINISHED) {
//			trace("stopping timer, and requesting new match list");
			if (clockUpdateTimer != null) {
				clockUpdateTimer.cancel();
				clockUpdateTimer = null;
			}
			showingFinalWhistle = true;
// this will block display of received match list until the
// the final whistle display is called in response to the getMatchResults
// postMatchResultsMatchList timer is triggered ... which will also
// set off a getMatch and a jackpot update.
			server.getMatchResults(ms.id, predictionFactory, server.playerID);
			startPostResultsMatchListUpdateTimer();
			server.getPlayerAccount(freePlayMode);
		} else {
//			trace("stopping all timers");
			if (clockUpdateTimer != null) {
				clockUpdateTimer.cancel();
				clockUpdateTimer = null;
			}
		}
	}
	
/*********************************************
 * UTILITIES AND WRAPPERS
 *********************************************/
	void betControlClearBet()
	{
		currentBet.betSlotId1 = 0;
		currentBet.betSlotId2 = 0;
		currentBet.betSlotId3 = 0;
		mainSpinner.setSelection(0);
		jackpot1Spinner.setSelection(0);
		jackpot2Spinner.setSelection(0);
		placeBetButton.setEnabled(false);
	}
	
	void onBetSlotSelect(int sid1, int sid2, int sid3)
	{
		if (currentMatch == null) {
			showError(999, "No current match, while selecting interval", ErrorLevel.FATAL);
			return;
		}
 		if (currentBet != null) {
 			if (sid1 >= 0) {
				currentBet.betSlotId1 = sid1;
				int ix = (sid1-1)/2;
				if (ix >= 10) ix -= 10;
				currentBet.period = (sid1<=20?1:2);
				currentBet.minute = ix*5;
				currentBet.timeSlotIdx = ix;
//				currentBet.teamName = (((sid1%2) == 1)? currentMatch.homeTeam.name:currentMatch.awayTeam.name);
				currentBet.teamId = (((sid1%2) == 1)? currentMatch.homeTeam.teamId:currentMatch.awayTeam.teamId);
 			}
			
			if (sid2 >= 0) {
				currentBet.betSlotId2 = sid2;
				int ix = (sid2-1)/2;
				if (ix >= 10) ix -= 10;
				currentBet.period2 = (sid2<=20?1:2);
				currentBet.minute2 = ix*5;
//				currentBet.timeSlotIdx2 = ix;
				currentBet.teamId2 = (((sid2%2) == 1)? currentMatch.homeTeam.teamId:currentMatch.awayTeam.teamId);
			}
			
			if (sid3 >= 0) {
				currentBet.betSlotId3 = sid3;	
				int ix = (sid3-1)/2;
				if (ix >= 10) ix -= 10;
				currentBet.period3 = (sid3<=20?1:2);
				currentBet.minute3 = ix*5;
//				currentBet.timeSlotIdx3 = ix;
				currentBet.teamId3 = (((sid3%2) == 1)? currentMatch.homeTeam.teamId:currentMatch.awayTeam.teamId);
			}
		}
	}
	
	void displayConfirmMultipleBets(Match m, ArrayList<Prediction> selectedBets, String title)
	{
	}
	
	int selectRandInt(int first,int last, ArrayList<Integer> excl)
	{
		if (first == last) {
			return first;
		} else if (first > last) {
			int n = first;
			first = last;
			last = n;
		}
		if (excl != null && excl.size() >= last-first) {
			return first-1;
		}
		int trial;
		boolean found = false;
		do {
			trial = (first +  (int)Math.floor(Math.random()*(last-first+1)));
			if (excl != null) {
				found = (excl.indexOf(new Integer(trial)) < 0);
			} else {
				found = true;
			}
		} while (!found);
		return trial;
	}

	protected boolean setSelectorToValue(Spinner ssel, int n)
	{
		if (ssel == null) {
			return false;
		}
		ssel.setSelection(0);
	   	SlotSpinnerData s = null;
	   	int i=0;
	   	for (i=0; i<betTimeAdapter.getCount(); i++) {
		   	s = betTimeAdapter.getItem(i);
			if (s.betSlotId == n) {
				ssel.setSelection(i);
				return true;
			}
		}
		return false;
	}

	void generateLuckyDip(Prediction3Goal bet)
	{
		placeBetButton.setEnabled(false);
//		setSelectorToValue('slot1Select', 0)
//		setSelectorToValue('slot2Select', 0)
//		setSelectorToValue('slot3Select', 0)
		
		int cbsn = currentBetSlotNo;
		int firstSlot = ((cbsn <= 0)?1:2*cbsn+1);
		if (firstSlot >= 37) {
//			showError('Lucky dip bet not allowed this far into the game',
//					  999,
//					  ErrorLevel.FATAL);
			return;
		}
		int n1 = selectRandInt(firstSlot,40, null);
		if (n1 <= firstSlot-1 || n1 <= 0) {
			showError(999,
					  "Error choosing first lucky dip number "+firstSlot,
					  ErrorLevel.FATAL);
			return;
		}
		if (!setSelectorToValue(mainSpinner, n1)) {
			showError(999,
					  "Error setting selection to first lucky dip number",
					  ErrorLevel.FATAL);
			return;
		}
		ArrayList<Integer> al = new ArrayList<Integer>();
		al.add(new Integer(n1));
		int n2 = selectRandInt(firstSlot,40, al);
		if (n2 <= firstSlot-1 || n2 <= 0) {
			setSelectorToValue(jackpot1Spinner, 0);
			showError(999,
					  "Error choosing second lucky dip number "+firstSlot,
					  ErrorLevel.FATAL);
			return;
		}
		al.add(new Integer(n2));
		if (!setSelectorToValue(jackpot1Spinner, n2)) {
			showError(999,
					  "Error setting selection to second lucky dip number",
					  ErrorLevel.FATAL);
			return;
		}
		int n3 = selectRandInt(firstSlot,40, al);
		if (n3 <= firstSlot-1 || n3 <= 0) {
			showError(999,
					  "Error choosing third lucky dip number "+firstSlot,
					  ErrorLevel.FATAL);
			setSelectorToValue(jackpot2Spinner, 0);
			return;
		}
		if (!setSelectorToValue(jackpot2Spinner, n3)) {
			showError(999,
					  "Error setting selection to third lucky dip number",
					  ErrorLevel.FATAL);
			return;
		}
		placeBetButton.setEnabled(true);
/*
		var ev = new Event(SELECTSLOT_EVENT);
		if (n1 >= 0) {
			ev.betSlotId = n1;
		}
		if (n2 >= 0) {
			ev.betSlotId2 = n2;
		}
		if (n3 >= 0) {
			ev.betSlotId3 = n3;
		}
		dispatchEvent(ev);
*/
	}
	
	void displayConfirmSingleBet(Prediction3Goal bet)
	{
		displayConfirmSingleBet(currentMatch, bet, null);
	}
	
	void displayConfirmSingleBet(Match m, Prediction3Goal bet, String title)
	{
		if (bet == null) {
			showError(999, "Internal error. null bet passed to confirm3PickBet", ErrorLevel.FATAL);
			return;
		}
		if (bet.betSlotId1 <= 0) {
			showError(999, "Internal error. invalid slot id to confirm3PickBet", ErrorLevel.FATAL);
			return;
		}
		int slid = bet.betSlotId1;
		if (title == null) {
			title = "BET CONFIRMATION";
		}
		if (bet.period < 0) {
			showError(999, "Internal error. invalid bet, missing period field", ErrorLevel.FATAL);
			return;
		}
		if (bet.minute < 0) {
			showError(999, "Internal error. invalid bet, missing minute field",  ErrorLevel.FATAL);
			return;
		}
		if (bet.betSlotId1 <= 0) {
			showError(999, "Internal error. invalid bet, missing betSlotId field",  ErrorLevel.FATAL);
			return;
		}
		if (bet.betSlotId2 <= 0) {
			showError(999, "Internal error. invalid bet, missing betSlotId2 field", ErrorLevel.FATAL);
			return;
		}
		if (bet.betSlotId3 <= 0) {
			showError(999, "Internal error. invalid bet, missing betSlotId3 field", ErrorLevel.FATAL);
			return;
		}
		if (bet.timeSlotIdx < 0) {
			bet.timeSlotIdx = time2SlotIdx(new MatchTime(bet.period, bet.minute));
		}
		String timeSlot = MatchTime.halfName(bet.period) + ", " + Integer.toString(bet.timeSlotIdx * 5) + ":00";
		if (bet.timeSlotIdx < 9) {
			timeSlot = timeSlot + "-" + Integer.toString(bet.timeSlotIdx * 5 + 4) + ":59";
		} else {
			timeSlot = timeSlot + "+";
		}
		timeSlot = timeSlot + " min";
		
		if (confirmTitleView != null) {
			confirmTitleView.setText(title);
		}
		if (confirmPredictionView != null) {
			Team tm1 = getTeam(bet.teamId);;
			Team tm2 = getTeam(bet.teamId2);;
			Team tm3 = getTeam(bet.teamId3);;

			MatchTime t1 = new MatchTime(bet.period, bet.minute, 0);
			MatchTime t2 = new MatchTime(bet.period2, bet.minute2, 0);
			MatchTime t3 = new MatchTime(bet.period3, bet.minute3, 0);
			String tm1nm = (tm1!=null?tm1.abbr:("TM# "+bet.teamId));
			String bs1nm = ((bet.betSlotId1 >0)?(", slot "+bet.betSlotId1):"");
			String tm2nm = (tm2!=null?tm2.abbr:("TM# "+bet.teamId2));
			String bs2nm = ((bet.betSlotId2 >0)?(", slot "+bet.betSlotId2):"");
			String tm3nm = (tm3!=null?tm3.abbr:("TM# "+bet.teamId3));
			String bs3nm = ((bet.betSlotId3 >0)?(", slot "+bet.betSlotId3):"");
			String g1s = "One Goal: "
							+tm1nm+", "
							+time2SlotString(t1)
							+bs1nm;
			String j1s = "3 Goal (1): "
							+tm1nm+", "
							+time2SlotString(t1)
							+bs1nm;
			String j2s = "3 Goal (2): "
							+tm2nm+", "
							+time2SlotString(t2)
							+bs2nm;
			String j3s = "3 Goal (3): "
							+tm3nm+", "
							+time2SlotString(t3)
							+bs3nm;
			confirmPredictionView.setText(
					Html.fromHtml(
						"<font size=\"+1\">"+g1s+"</font>"+"<br/>"+"<br/>"+
						"<font size=\"-1\">"+j1s+"</font>"+"<br/>"+
						"<font size=\"-1\">"+j2s+"</font>"+"<br/>"+
						"<font size=\"-1\">"+j3s+"</font>")
				);
		}
		
		showConfirmView();
	}

	public void setupBetSlotDisplay(int currentBetSlotNo, Match m)
	{
		String t1n=""; 
		String t2n="";
		String t1a=""; 
		String t2a="";
		if (m != null && m.homeTeam != null && m.awayTeam != null) {
			t1n = m.homeTeam.name;
			t2n = m.awayTeam.name;
			t1a = m.homeTeam.abbr;
			t2a = m.awayTeam.abbr;
		}
		
//		window.alert(t1n+' '+t2n);
//		window.alert('cbsn '+currentBetSlotNo);
		setBetSlotOptions(m, currentBetSlotNo, t1n, t2n, t1a, t2a);
		mainSpinner.setSelection(0);
		jackpot1Spinner.setSelection(0);
		jackpot2Spinner.setSelection(0);
		/*
		if (betSlotControls != null) {
			var sbc=null;
			var sbct = null;
			var i=0;
			for (i=0; i<betSlotControls.length; i++) {
				sbc = betSlotControls[i];
				if (sbc != null) {
					sbct = betSlotId2Time(sbc.slotId);
					if (sbct.ge(currentBetTime)) {
						sbc.setEnabled(true);
					} else {
						if (sbc.currentState == SlotBetControl.SELECTED) {
							sbc.makeSelection(false);
						}
						sbc.setEnabled(false);
					}
				}
			}
		}*/
	}
	protected void setBetSlotOptions(Match m, int curBetSlotNo, String t1n, String t2n, String t1a, String t2a)
	{
//		for (i = ssel.length - 1; i>=0; i--) {
//	    	ssel.remove(i);
//		}
		betTimeAdapter.clear();
		betTimeAdapter.add(new SlotSpinnerData("", 0, -1, -1));
		if (curBetSlotNo >= 0) {
			for (int i=curBetSlotNo; i<20; i++) {
				String tt = slotIdx2SlotString(i)+" ["+Integer.toString(2*i+1)+"]";
				if (t1a != null && !t1a.equals("")) {
					tt = t1a + ": " + tt;
				}
				SlotSpinnerData ssd = new SlotSpinnerData(tt, m.matchId, m.homeTeam.teamId, curBetSlotNo);
				ssd.betSlotId = (2*i+1);
				betTimeAdapter.add(ssd);
			}

			for (int i=curBetSlotNo; i<20; i++) {
				String tt = slotIdx2SlotString(i)+" ["+Integer.toString(2*i+2)+"]";
				if (t2a != null && !t2a.equals("")) {
					tt = t2a + ": " + tt;
				}
				SlotSpinnerData ssd = new SlotSpinnerData(tt, m.matchId, m.awayTeam.teamId, curBetSlotNo);
				ssd.betSlotId = (2*i+2);
				betTimeAdapter.add(ssd);
			}
			
		}
	}

	protected void liveBetEnable(boolean b)
	{
		liveViewEnabled = true;
	}

	public void setTime(MatchTime gT, MatchTime bT)
	{
		if (gT == null) {
			time = new MatchTime(0, 0, 0);
		}else {
			time = gT;
		}
		if (bT == null) {
			currentBetTime = new MatchTime(0, 0, 0);
		}else {
			currentBetTime = bT;
		}
		showCurrentTime();
		return;
	}

	protected int currentMatchId()
	{
		if (currentMatch != null) return currentMatch.matchId;
		return -1;
	}

	public Match getActiveMatch(int id)
	{
		if (server == null) {
			return null;
		}
		return server.findActiveMatch(id);
	}
	
	public Match getResultsMatch(int id)
	{
		if (server == null) {
			return null;
		}
		return server.findResultsMatch(id);
	}
	
	public static MatchTime betSlotId2Time(int slno)
	{
		if (slno % 2 != 0) {
			slno += 1;
		}
		slno /= 2;
		return (slno > 10)?new MatchTime(2, (slno-11) * 5, 0):new MatchTime(1,(slno-1)*5,0);
	}

	public static int time2BetSlotId(MatchTime t, boolean tm)
	{
		int tind = 2*((int)(Math.floor(t.min / 5)));
		if (tind >= 18) tind = 18;
		tind += (tm?1:2);
		if (t.half >= 2) {
			return 20 + tind;
		}
		return tind;
	}
	
	public static MatchTime slotIdx2Time(int slno)
	{
		return (slno < 10)? new MatchTime(1,slno*5,0):new MatchTime(2, (slno-10) * 5, 0);
	}

	public static int time2SlotIdx(MatchTime t)
	{
		int slno=(int) Math.floor(t.min / 5);
		if (slno > 9) {
			slno = 9;
		}
		if (t.half >= 2) {
			slno += 10;
		}
		return slno;
	}

	public static String slotIdx2SlotString(int slno)
	{
		if (slno == 9) {
			return "1st Added";
		}
		if (slno >= 19) {
			return "2nd Added";
		}
		MatchTime t1 = slotIdx2Time(slno);
		MatchTime t2 = new MatchTime(t1.half, t1.min+4, 59);
//		Log.d("slot idx1", Integer.toString(t1.half)+","+Integer.toString(t1.min)+","+Integer.toString(t1.sec));
//		Log.d("slot idx2", Integer.toString(t2.half)+","+Integer.toString(t2.min)+","+Integer.toString(t2.sec));
		return t1.toString()+"-"+t2.toString();
	}
	
	public static String time2SlotString(MatchTime t)
	{
		return slotIdx2SlotString(time2SlotIdx(t));
	}
	
	public String formatCurrency(float val, String currency, boolean useSymbol, int decimals)
	{
		String valStr;
		if (decimals > 0) {
			valStr = String.format("%.2f", val);
		} else {
			valStr = Integer.toString((int) Math.floor(val));
		}		
		String showCurrency = "";
		
		if (currency != null) {
			showCurrency = currency;
		}
		if (showCurrency.equals("")) {
			return valStr;
		}
		if (useSymbol) {
			if (showCurrency.equals("GBP")) {
				return '£'+valStr;
			}
			if (showCurrency.equals("EUR")) {
				return '€'+valStr;
			}
			if (showCurrency.equals("USD")) {
				return "US$"+valStr;
			}
		}
		return valStr+' '+showCurrency;
	}
	
/********************************
 * TIMERS 
 ********************************/
	protected void startPotUpdateTimer()
	{
		stopPotUpdateTimer();
		potUpdateTimer = new Timer();
		potUpdateTimer.schedule(
			new TimerTask() {
				public void run()
				{
					if (server != null && currentMatchId() != -1) {
						server.getMatchPotUpdate(currentMatchId());
					}
				}
			}, 0, potUpdateRate * 1000);
		return;
	}

	protected void stopPotUpdateTimer()
	{
		if (this.potUpdateTimer != null) {
			potUpdateTimer.cancel();
			potUpdateTimer = null;
		}
		return;
	}

	public void startClockUpdateTimer(final int udsecs)
	{
		stopClockUpdateTimer();
		clockUpdateTimer = new Timer();
		clockUpdateTimer.schedule(
			new TimerTask() {
				protected int tickCount=0;
				public void run()
				{
					tickCount++;
					if (tickCount > ((udsecs > clockUpdateRate)?clockUpdateRate:udsecs)) {
						stopClockUpdateTimer();
						clockUpdateTimerComplete();
					} else {
						updateTimerTick(false);
					}
				}
			}, 1000, 1000);
		return;
	}

	public void updateTimerTick(boolean isMinTick)
	{
//		trace("LiveBetPanel::updateTimerTick()", isMinTick, timerStopped);
		if (!timerStopped) {
			if (isMinTick) {
				time.min++;
				time.sec = 0;
			}else {
				time.incS(1);
			}
			if (time.sec == 0) {
				currentBetTime = adjustedBetTime(time, MatchState.PLAYING);
			}
			viewHandler.obtainMessage(GUIMSG_SHOW_CURRENT_TIME).sendToTarget();
		}
		return;
	}
	
	protected void clockUpdateTimerComplete()
	{
		if (!predictionCheckLooper) {
			livePanelPredictionCheck();
		}
		Match m=getActiveMatch(currentMatchId());
		if (m == null) {
			return;
		}
		MatchStatus ms= (m != null? m.status : null);
		if (!(ms == null) && ms.time.plusS(60).inAddedTime()) {
			startMatchStatusUpdateTimer(60);
			waitingForTheHalfHack = true;
		}else {
			waitingForTheHalfHack = false;
		}
		startClockUpdateTimer(clockUpdateRate);
		return;
	}
	
	public void stopClockUpdateTimer()
	{
		if (clockUpdateTimer != null) {
			clockUpdateTimer.cancel();
			clockUpdateTimer = null;
		}
		return;
	}

	protected void startMatchStatusTimeoutTimer()
	{
		stopMatchStatusTimeoutTimer();
		statusTimeoutTimer = new Timer();
		statusTimeoutTimer.schedule(
			new TimerTask() {
				public void run()
				{
					if (currentMatchId() > 0) {
						server.getMatchStatus(currentMatchId());
						startMatchStatusTimeoutTimer();
					}
				}

			}, statusTimeout * 1000);
	}

	protected void startPostResultsMatchListUpdateTimer()
	{
		if (postResultMatchListUpdateTimer == null) {
			postResultMatchListUpdateTimer = new Timer();
			postResultMatchListUpdateTimer.schedule(
				new TimerTask() {
					public void run()
					{
						showingFinalWhistle = false;
						postResultMatchListUpdateTimer.cancel();
						postResultMatchListUpdateTimer = null;
						server.getMatches(biaModeMatchId);
						server.updateJackpots();
					}

				}, 1000 * 60 * 2);
		}
		return;
	}
	
	protected void stopMatchStatusTimeoutTimer()
	{
		if (statusTimeoutTimer != null) {
			statusTimeoutTimer.cancel();
			statusTimeoutTimer = null;
		}
		return;
	}
	
	public void startMatchStatusUpdateTimer(int tupds)
	{
		if (tupds == 0) {
			if (matchStatusUpdateTimer != null) {
				matchStatusUpdateTimer.cancel();
				matchStatusUpdateTimer = null;
			}
			return;
		}
		if (matchStatusUpdateTimer != null) {
			if (matchStatusUpdateTimerDelay == tupds * 1000) {
				return;
			}
			matchStatusUpdateTimer.cancel();
			matchStatusUpdateTimer = null;
		}
		matchStatusUpdateTimer = new Timer();
		matchStatusUpdateTimerDelay = tupds * 1000;
		matchStatusUpdateTimer.schedule(
			new TimerTask() {
				public void run()
				{
					if (currentMatchId() > 0) {
						startMatchStatusUpdateTimer(matchStatusUpdateRate);
						server.getMatchStatus(currentMatchId());
						startMatchStatusTimeoutTimer();
					}
				}
			},
			matchStatusUpdateTimerDelay, matchStatusUpdateTimerDelay);
		return;
	}

	protected void setNextMatchStartCheck()
	{
		ArrayList<Match> lma=server.liveMatchList();
		ArrayList<Match> nma=server.nextMatchList();
		if ((lma == null) || lma.size() == 0) {
			liveBetEnable(false);
		}
		if (nma == null || nma.size() == 0) {
			return;
		}
		Match m=nma.get(0);
		long nmms=m.startDate.getTime();
		long nowms=new Date().getTime();
		long wms=nmms - nowms;
		wms = wms / 1000;
		if (wms < 60) {
			wms = 60;
		}
		if (wms > 24 * 60 * 60) {
			wms = 24 * 60 * 60;
		}
		
		if (lookForLiveTimer != null) {
			lookForLiveTimer.cancel();
			lookForLiveTimer = null;
		}
		lookForLiveTimer = new Timer();
		lookForLiveTimer.schedule(
			new TimerTask() {
				public void run()
				{
					if (server != null) {
						server.getMatches();
					}
				}
			}, wms * 1000);
		
		return;
	}

/*******************************************
 * WRAPPERS AND MAIN PROCESSING ROUTINES
 *******************************************/
	public void setMatchDetails(Match m)
	{
		Log.d("select", "set match "+m.name);
		if (needGameFeed) startGameFeed(m);
		currentMatch = m;
		currentBetSlotNo = -1;
		betControlClearBet();
		String t = "";
		if (currentMatch != null) {
			currentBet.matchId = currentMatch.matchId;
			t = currentMatch.matchName();
		}
		if (placeBetTitle != null) {
			placeBetTitle.setText(t);
		}
		if (currentMatch == null || currentMatch.status == null || currentMatch.homeTeam == null || currentMatch.awayTeam == null) {
			trackerScoreView.setVisibility(View.INVISIBLE);
		} else {
			trackerScoreView.setVisibility(View.VISIBLE);
			if (trackerTeam1Name != null) trackerTeam1Name.setText(currentMatch.homeTeam.name);
			if (trackerTeam1Score != null) trackerTeam1Score.setText(Integer.toString(currentMatch.status.t1Score));
			if (trackerTeam2Name != null) trackerTeam2Name.setText(currentMatch.awayTeam.name);
			if (trackerTeam2Score != null) trackerTeam2Score.setText(Integer.toString(currentMatch.status.t2Score));
		}
		if (currentMatch == null) {
			if (team1Details != null) team1Details.setTo("", 0, "");
			if (team2Details != null) team2Details.setTo("", 0, "");
//			eventSlider.setTeamIDs(-1, -1);
//			eventSlider.clearFeed();
			setTime(new MatchTime(0, 0, 0), new MatchTime(0, 0, 0));
		} else {
			if (team1Details != null) team1Details.setTo(currentMatch.homeTeam.name, currentMatch.homeTeam.homeColor, currentMatch.homeTeam.icon);
			if (team2Details != null) team2Details.setTo(currentMatch.awayTeam.name, currentMatch.awayTeam.awayColor, currentMatch.awayTeam.icon);
//			eventSlider.setTeamIDs(currentMatch.homeTeam.teamId, currentMatch.awayTeam.teamId);
//			eventSlider.clearFeed();
//			eventSlider.addFeed(currentMatch.feed);
			if (currentMatch.status == null) {
				setTime(new MatchTime(0, 0, 0), new MatchTime(0, 0, 0));
			} else {
				Log.d("set match", "setting time to "+currentMatch.status.time.toString());
				setTime(currentMatch.status.time, adjustedBetTime(currentMatch.status.time, currentMatch.status.status));
			}
			displayPredictionList(currentMatch, currentMatch.predictions);
		}
		return;
	}

	public MatchTime adjustedBetTime(MatchTime gT, String state)
	{
		MatchTime abT=null;
		if (state.equals(MatchState.INTERVAL) || state.equals(MatchState.OPEN)) {
				abT = gT;
//				trace("adjusted ", state, " bet time is unadjusted", abT.toString());
		} else if (state.equals(MatchState.ABANDONED) || state.equals(MatchState.CANCELLED)
					|| state.equals(MatchState.DELETED) || state.equals(MatchState.FINISHED)) {
				abT = new MatchTime(4, 45, 0);
//				trace("adjusted ", state, " bet time is set to wrongness", abT.toString());
		} else {
			abT = new MatchTime(gT);
			abT.incS(liveBetAdvanceFactor);
			if (liveBetAdvanceMinimum) {
				if (abT.sec > 0) {
					abT.sec = 0;
					abT.min++;
				}
			} else {
				abT.sec = 0;
			}
			if (abT.min > 45 || abT.half > 2 && abT.min > 15) {
				abT.min = 0;
				abT.sec = 0;
				abT.half++;
			}else {
				abT.min = (int) (5 * Math.floor((abT.min + 5) / 5));
				abT.sec = 0;
//				trace("setting to normal advance playing", (abT.min + 5) / 5, Math.floor((abT.min + 5) / 5));
			}
//			trace("adjusted ", state, " bet time for", gT.toString(), "is set to bet t", abT.toString());
		}
		return abT;
	}

	public boolean clearSelection()
	{
		if (selectedBets == null) {
			selectedBets = new ArrayList<Prediction>();
		}
		while (selectedBets.size() > 0) {
			selectedBets.remove(0);
		}
		livePanelDisableBetSelection();
		livePanelClearSlotSelections();
		return false;
	}

	private void livePanelClearSlotSelections()
	{
	}

	private void livePanelDisableBetSelection()
	{
	}

	protected boolean playNextSelected()
	{
		if (selectedBets.size() <= 0) {
			livePanelDisableBetSelection();
			return false;
		}
		currentBet = (Prediction3Goal) selectedBets.remove(0);
		if (currentBet == null) {
			return false;
		}
		Match m = getActiveMatch(currentBet.matchId);
		if (m != null) {
			server.playParimutuelTicket(currentBet.matchId, currentBet, m.predictions);
		}

		return true;
	}

	protected void setMatch(Match m)
	{
		setMatchDetails(m);
		//clearSelection();
		if (m != null) {
			server.getMatchStatus(m.matchId);
			startMatchStatusTimeoutTimer();
			server.listPredictionResults(m, predictionFactory);
			if (m.status.status != MatchState.PLAYING) {
				stopClockUpdateTimer();
			} else {
				startClockUpdateTimer(clockUpdateRate);
			}
		}
//		liveBetPanel.teamStatsPanel.setMatch(currentMatch);
//		dataFeedPanel.addFeed(currentFeed);
	}

	public void displayPredictionList(Match m, ArrayList<Prediction> pa)
	{
		if (m.matchId == currentMatchId()) {
		/*
		SlotBetControl sbc2=null;
		for (SlotBetControl sbc: team1BetControls) {
			sbc.setCurrentState(SlotBetControl.NORMAL);
		}
		for (SlotBetControl sbc: team2BetControls){
			sbc.setCurrentState(SlotBetControl.NORMAL);
		}*/
		/*
		if ((pa != null) && (this.currentMatch != null)) {
			sbc2 = null;
//			trace("prediction list, size", pa.length);
			for (Prediction p: pa) {
				if (p != null) {
//					trace("checking prediction ticket", p);
					sbc2 = betControlFor(p);
					if (sbc2 != null) {
						if (p.result != "W") {
							if (p.result != "L") {
								sbc2.setCurrentState(SlotBetControl.PENDING);
							}else {
								sbc2.setCurrentState(SlotBetControl.LOST);
							}
						}else {
							sbc2.setCurrentState(SlotBetControl.WON);
						}
					} else {
//						trace("no corresponding bet control for prediction", p);
					}
				}
			}
		}*/
		}
		if (m.matchId == resultsViewSelectedMatchId) {
			resultsViewPanel.displayResultsMatch(resultsViewSelectedMatch, true, false, null);	
		}
		return;
	}

	/**
	 * 
	 * @param _matchId
	 * @param t
	 * @param t1Score
	 * @param t2Score
	 * @param _matchState
	 * @param _estimate
	 * @param goals
	 * @return
	 */
	public int updateLiveMatchStatus(
			int _matchId, MatchTime t, int t1Score, int t2Score,
			String _matchState, Number _estimate, ArrayList<Goal> goals)
	{
		int timerState=0;
		if (currentMatchId() < 0) {
//			trace("update match status negative match id in live tab");
			return MatchTimerState.STOPPED;
		}
		if (currentMatchId() != _matchId) {
//			trace("update match status wrong match");
			return MatchTimerState.STOPPED;
		}
		if (_matchState.equals(MatchState.FINISHED)) {
			timerStopped = true;
			timerState = MatchTimerState.FINISHED;
			showStatus("FINAL WHISTLE");
		} else {
			hideFireworks();
			if (_matchState.equals(MatchState.INTERVAL)) {
				if (t.half != 1) {
					if (t.half != 2) {
						if (t.half != 3) {
							if (t.half != 4) {
								showStatus("INTERVAL ");
							} else {
								showStatus("EXTRA TIME INTERVAL");
							}
						}else {
							showStatus("BEFORE EXTRA TIME");
						}
					}else {
						showStatus("HALF TIME");
					}
				} else if ((currentMatch != null) &&
						   (currentMatch.startDate != null) &&
						   (!currentMatch.startDate.equals(""))) {
					showStatus("KICKOFF AT " + MatchTime.timeFormat(currentMatch.startDate));
				} else {
					showStatus("WAITING TO START");
				}
				time = t;
				currentBetTime = adjustedBetTime(t, _matchState);
				timerStopped = true;
				timerState = MatchTimerState.PAUSED;
			} else if (_matchState.equals(MatchState.OPEN)) {
				showStatus("GAME OPEN");
				time = t;
				currentBetTime = adjustedBetTime(t, _matchState);
				timerStopped = true;
				timerState = MatchTimerState.PAUSED;
			} else if (_matchState.equals(MatchState.PAUSED)) {
				time = t;
				currentBetTime = adjustedBetTime(t, _matchState);
				if (time.half != 1){
					showStatus("GAME PAUSED - SECOND HALF");
				}else {
					showStatus("GAME PAUSED - FIRST HALF");
				}
				timerStopped = true;
				timerState = MatchTimerState.PAUSED;
			} else {
				time = t;
				currentBetTime = adjustedBetTime(t, _matchState);
				if (time.half != 1) {
					showStatus("LIVE BET - SECOND HALF");
				}else {
					showStatus("LIVE BET - FIRST HALF");
				}
				timerState = MatchTimerState.RUNNING;
				timerStopped = false;
			}
		}
		displayScore(t1Score, t2Score);
		
		/*
		eventSlider.clearFeed();
		for (Goal g: goals) {
			eventSlider.addItem(FeedEntry.GOAL_EVENT,
				new MatchTime(g.half, g.min, g.sec), g.teamId,
				"Goal!!!!",
				"Goal to " + (g.teamId != currentMatch.homeTeam.teamId ? currentMatch.awayTeam.name : currentMatch.homeTeam.name) + "!");
		}*/
		showCurrentTime();
		return timerState;
	}


	protected void displayScore(int t1Score, int t2Score)
	{
		if (trackerTeam1Score != null) trackerTeam1Score.setText(Integer.toString(t1Score));
		if (trackerTeam2Score != null) trackerTeam2Score.setText(Integer.toString(t2Score));
		if (team1Details != null) {
			team1Details.setScore(t1Score);
		}
		if (team2Details != null) {
			team2Details.setScore(t2Score);
		}
	}

	protected void livePanelPredictionCheck()
	{
		if (server == null) {
			return;
		}
		Match m=getActiveMatch(currentMatchId());
		if (m == null) {
			return;
		}
		if (m.predictions == null || m.predictions.size() == 0) {
			return;
		}
		if (hasLivePredictionsToCheck(m)) {
//			trace("match has live predictions!");
			server.listPredictionResults(m, predictionFactory);
		}else {
//			trace("no live predictions in current live match");
		}
		return;
	}

	protected void checkPredictionsFor(Match m)
	{
		MatchTime t=null;
		boolean chk=false;
		if (m == null || m.predictions == null) {
			return;
		}
//		trace("checking prediction results for match", m.name, m.liveStatus());
		if (m.liveStatus() != 0) {
			if (m.liveStatus() > 0) {
				for (Prediction p: m.predictions) {
					if (p.result.equals(Prediction.Result.WON)) {
//						trace("winning result in future match, wtf!");
						continue;
					}
					p.result = "U";
				}
				return;
			}
			for (Prediction p: m.predictions) {
				if (p.result.equals(Prediction.Result.WON)) {
					continue;
				}
				p.result = "L";
			}
			return;
		}else {
			t = new MatchTime(m.status.time);
//			trace("match is live, checking, comparison time is", t, time);
			if (m.matchId == currentMatchId()) {
				if (time.gt(t)) {
					t.setTo(time);
				}
			}
//			trace("and now comparison time is", t.half, t.min);
			for (Prediction p: m.predictions) {
				Prediction3Goal pg = (Prediction3Goal)p;
				if (!pg.result.equals("W")) {
					chk = false;
					if (pg.period < t.half) {
						chk = true;
					} else if (pg.period != t.half) {
						chk = false;
					} else if (t.min >= pg.minute + BET_INTERVAL_LEN_MINS) {
						chk = true;
					}
					if (chk) {
						p.result = "L";
					} else {
						p.result = "U";
					}
				}
			}
		}
		return;
	}

	public Boolean hasLivePredictionsToCheck(Match m)
	{
		if (m == null || currentMatch == null ||
			 (m.matchId != currentMatch.matchId)) {
			return false;
		}
		if (m.predictions == null || m.predictions.size() == 0) {
			return false;
		}
		for (Prediction p: m.predictions) {
			Prediction3Goal pg = (Prediction3Goal)p;
			if (pg.result.equals("U")) {
				if (pg.period == time.half && time.min >= pg.minute) {
					return true;
				}
				if ((pg.period < time.half)) {
					return true;
				}
			}
		}
		return false;
	}
	
/*******************************************
 * DISPLAY HOOKS
 *******************************************/
	public static final int GUIMSG_SHOW_CURRENT_TIME = 1;
	
	public Handler viewHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case GUIMSG_SHOW_CURRENT_TIME: {
				showCurrentTime();
				break;
			}
			}
		}
	};
	
	public void showCurrentTime()
	{
//			eventSlider.setGameTime(time);
//			eventSlider.setBetTime(currentBetTime);
//			gameClock.setGameTime(time);
		
		/*
		for (SlotBetControl sbc: team1BetControls) {
			if (sbc.time().ge(currentBetTime)) {
				sbc.setFinished(false);
			} else {
				if (sbc.getCurrentState() == SlotBetControl.SELECTED) {
					sbc.makeSelection(false);
				}
				sbc.setFinished(true);
			}
		}
		for (SlotBetControl sbc: team2BetControls) {
			if (sbc.time().ge(currentBetTime)) {
				sbc.setFinished(false);
			} else {
				if (sbc.getCurrentState() == SlotBetControl.SELECTED) {
					sbc.makeSelection(false);
				}
				sbc.setFinished(true);
			}
		}*/
		
		int newBetSlotNo=1;
		for (int i=0; i<20; i++) {
			MatchTime t=slotIdx2Time(i);
			if (t.ge(currentBetTime)) {
				newBetSlotNo = i;
				break;
			}
		}
//			window.alert('show current time '+gameTime.toString());
//			showStatus("show current time "+currentBetSlotNo+", "+newBetSlotNo);
		if (newBetSlotNo != currentBetSlotNo) {
			currentBetSlotNo = newBetSlotNo;
			setupBetSlotDisplay(currentBetSlotNo, currentMatch);
		}
		return;
	}

	public void resetErrDisplay()
	{
	}
		
		
	public void showError(int code, String msg)
	{
		showError(code, msg, ErrorLevel.ERROR);
	}
	
	public void showError(int code, String msg, ErrorLevel error)
	{
		viewProgress.setVisibility(View.GONE);
		viewStatus.setVisibility(View.VISIBLE);
		viewStatusBar.setVisibility(View.VISIBLE);
		showStatus(msg);
	}
	
	protected void showPreferences()
	{
//		viewStatusBar.setVisibility(View.GONE);
	}
	
	protected void showLogin(String lastErrorMessage)
	{
		viewStatus.setVisibility(View.INVISIBLE);
		viewStatusBar.setVisibility(View.INVISIBLE);
		infoBar.setVisibility(View.GONE);
		mainContent.setVisibility(View.VISIBLE);
		mainContent.setDisplayedChild( mainContent.indexOfChild(loginView));
		if (lastErrorMessage != null && !lastErrorMessage.equals("")) {
			showError(999, lastErrorMessage);
		}
	}
	
	protected void showTracker()
	{
		viewStatus.setVisibility(View.GONE);
		viewStatusBar.setVisibility(View.GONE);
		mainContent.setVisibility(View.VISIBLE);
		mainContent.setDisplayedChild( mainContent.indexOfChild(trackerView));
		if (currentMatch == null || currentMatch.status == null || currentMatch.homeTeam == null || currentMatch.awayTeam == null) {
			trackerScoreView.setVisibility(View.INVISIBLE);
		} else {
			trackerScoreView.setVisibility(View.VISIBLE);
			startGameFeed(currentMatch);
		}
	}
	
	protected void showResultsView()
	{
		showStatus(null);
		viewStatus.setVisibility(View.GONE);
		viewStatusBar.setVisibility(View.GONE);
		mainContent.setVisibility(View.VISIBLE);
		mainContent.setDisplayedChild( mainContent.indexOfChild(resultsView));
	}
	
	protected void showBetView()
	{
		viewStatus.setVisibility(View.VISIBLE);
		viewStatusBar.setVisibility(View.VISIBLE);
		mainContent.setVisibility(View.VISIBLE);
		mainContent.setDisplayedChild( mainContent.indexOfChild(placeBetView));
	}

	protected void showConfirmView()
	{
		viewStatus.setVisibility(View.VISIBLE);
		viewStatusBar.setVisibility(View.VISIBLE);
		mainContent.setVisibility(View.VISIBLE);
		mainContent.setDisplayedChild( mainContent.indexOfChild(confirmBetView));
	}

	protected void showSelect()
	{
		if (server == null) return;
		ArrayList<Match> lml = server.liveMatchList();
		if (lml == null || lml.size() == 0) return;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setIcon(R.drawable.app_icon);
		builder.setTitle("Select Live Match");
		builder.setPositiveButton(android.R.string.cancel, new Dialog.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.dismiss();
			}
		});
		
		
		
		ListView modeList = new ListView(this);
		final String[] matchNames = new String[lml.size()];
		final Match[] menuMatches = new Match[lml.size()];
		int i = 0;
		for (Match m:lml) {
			menuMatches[i] = m;
			matchNames[i++] = m.name;
		}
		ArrayAdapter<String> modeAdapter = new ArrayAdapter<String>(
				this, android.R.layout.simple_list_item_1, android.R.id.text1, matchNames);
		modeList.setAdapter(modeAdapter);

		selectMatchDialog = builder.create();
		selectMatchDialog = builder.setView(modeList).setCancelable(true).show();

		modeList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id)
			{
				Log.d("menu item", "select match "+menuMatches[position].name);
				setMatch(menuMatches[position]);
				selectMatchDialog.dismiss();
				if (mainContent != null && mainContent.getCurrentView() == fireworksView) {
					showBetView();
				}
//				  Log.d("item click","got"+view.toString()+" "+view.getId()+" "+Integer.toString(position));
			}
		});
		
	}
	
	protected void showHelp()
	{
		View messageView = getLayoutInflater().inflate(R.layout.help, null, false);
		// When linking text, force to always use default color. This works
		// around a pressed color state bug.
		TextView textView = (TextView) messageView.findViewById(R.id.help_more);
		int defaultColor = textView.getTextColors().getDefaultColor();
		textView.setTextColor(defaultColor);

		textView = (TextView) messageView.findViewById(R.id.help_version);
		textView.setText(fullVersionString);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setIcon(R.drawable.app_icon);
		builder.setTitle(getString(R.string.app_name)+" Help");
		builder.setView(messageView);
		builder.setPositiveButton(android.R.string.ok, new Dialog.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.dismiss();
			}
		});
		builder.create();
		builder.show();
	}
	
	protected void showAbout()
	{
		// Inflate the about message contents
		View messageView = getLayoutInflater().inflate(R.layout.about, null, false);

		// When linking text, force to always use default color. This works
		// around a pressed color state bug.
		TextView textView = (TextView) messageView.findViewById(R.id.about_credits);
		int defaultColor = textView.getTextColors().getDefaultColor();
		textView.setTextColor(defaultColor);
		
		textView = (TextView) messageView.findViewById(R.id.about_version);
		textView.setText(fullVersionString);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setIcon(R.drawable.app_icon);
		builder.setTitle("About "+getString(R.string.app_name));
		builder.setView(messageView);
		builder.setPositiveButton(android.R.string.ok, new Dialog.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.dismiss();
			}
		});
		builder.create();
		builder.show();

	}
	
	protected void showAccountBalance()
	{
		if (server != null) {
			infoBar.setVisibility(View.VISIBLE);
			viewAccount.setText(formatCurrency(server.accountBalance, server.accountCurrency, true, 2));
		}
	}

	protected void showStatus(String string)
	{
		if (string == null) {
			viewStatus.setVisibility(View.GONE);
			viewStatusBar.setVisibility(View.GONE);
			viewStatus.setText("");
		} else {
			viewStatus.setText(string);	
//			viewStatusBar.setVisibility(View.VISIBLE);
		}
	}
	
	/**
	 * show the view bits indicating an internet connection in progress
	 */
	protected void showConnecting()
	{
		showConnecting(null);
	}
	
	protected void showConnecting(String title)
	{
		if (title != null) {
			viewStatus.setText(title);
		}
		viewStatus.setVisibility(View.VISIBLE);
		viewStatusBar.setVisibility(View.VISIBLE);
		viewProgress.setVisibility(View.VISIBLE);
		viewStatusBar.startAnimation(animSlideIn);
	}
   	
	/**
	 * hide the view bits indicating an internet connection in progress
	 */
	protected void hideConnecting()
	{
		viewStatusBar.startAnimation(animSlideOut);
		viewProgress.setVisibility(View.INVISIBLE);
	}

	public void reBet(int bsid, MatchTime bt, MatchTime slt)
	{
		currentBet.betSlotId1 = bsid;
		currentBetTime = new MatchTime(bt);
		currentBet.minute = currentBetTime.min;
		currentBet.period = currentBetTime.half;
		showCurrentTime();
		displayConfirmSingleBet(currentMatch, currentBet, "SLOT AT " + slt.toString() + "MIN IS OVER. CONFIRM NEW BET");
	}

	public boolean confirmSelection()
	{
		if (!(selectedBets == null) && selectedBets.size() > 0) {
			displayConfirmMultipleBets(currentMatch, selectedBets, null);
			return true;
		}
		return false;
	}

	public void hideFireworks()
	{
	}
	
	public void showFireworks(
			Match m,
			Date start,
			float winnings,
			float estimate,
			float pot,
			String currency,
			ArrayList<Prediction> pred,
			ArrayList<Jackpot> jackpots)
	{
		if (m == null) {
//			trace("game panel:: fireworks() null match");
			return;
		}
		if (m.matchId != currentMatchId()) {
//			trace("game panel:: fireworks() bad match id");
			return;
		}
		displayPredictionList(m, pred);
		
		showStatus(null);
		mainContent.setDisplayedChild( mainContent.indexOfChild(fireworksView));

//		displayPredictionList(pred);
		int nPreds=0;
		int nWins=0;
		int i=0;
		for (Prediction p: pred) {
			++nPreds;
//			Log.d("fireworks", "pred "+Integer.toString(nPreds)+" "+p.result);
			if (p.result.equals("W")) {
				++nWins;
			}
		}
		TextView heading = (TextView) fireworksView.findViewById(R.id.fireworks_title);
		if (heading != null) {
			heading.setText("Final Whistle Results");
		}
		TextView subhead = (TextView) fireworksView.findViewById(R.id.fireworks_teams);
		if (subhead != null) {
				subhead.setText(m.homeTeam.name+" (" +
					Integer.toString(m.status.t1Score) +
					")"+"   v   "+ m.awayTeam.name+
					"("+Integer.toString(m.status.t2Score)+")");
		}
		String mInfo = "";
		Log.d("fireworks", Integer.toString(nWins));
		if (nWins > 0) {
			if (nWins != 1) {
				mInfo += "Your " +
					Integer.toString(nWins) + " winning bets paid: " +
					formatCurrency(winnings, currency, true, 2)+"\n";
			}else {
				mInfo += "Your 1 winning bet paid: " + formatCurrency(winnings, currency, true, 2)+"<br/>";
			}
			mInfo += "TOP GOAL PAYOUT FOR A " +
				formatCurrency(10, currency, true, 2) + " STAKE WAS: " +
				formatCurrency(estimate, currency, true, 2)+"\n";
			
			for (Jackpot jp:jackpots) {
				if (jp.amount > 0) {
					String jmsg = "Congratulations! You have won " +
						formatCurrency(jp.amount, jp.currency, true, 2) +
						"in the " + jp.name + " Jackpot!"+"\n";
					mInfo += jmsg;
				}
				
			}
			String pInfo = "";
			for (Prediction p:pred) {
				Prediction3Goal p3 = (Prediction3Goal) p;
				if (p3.result.equals("W")) {
					String tnm = m.teamName(p3.teamId);
					if (tnm == null || tnm.equals("")) {
						tnm = getTeamName(p3.teamId);
					}
					MatchTime t = new MatchTime(p3.period, p3.minute, 0);
					String pmsg = "Your bet on " + tnm + " at " + t.toMinString() + " won!";
					pInfo += pmsg+"\n";
				}
			}
			TextView predfield = (TextView) fireworksView.findViewById(R.id.fireworks_predictions);
			if (predfield != null) predfield.setText(pInfo);
		} else {
			if (nPreds > 0) {
				mInfo = "You had no winning bets on this game";
			} else {
				mInfo = "You had no bets on this game";
			}
		}
		TextView infofld = (TextView) fireworksView.findViewById(R.id.fireworks_info);
		if (infofld != null) infofld.setText(mInfo);
//		betViews.selectedChild = fireworksPanel;
		return;
	}


	Date resultsToDate = new Date();
	Date resultsFromDate = new Date();
	Match resultsViewSelectedMatch = null;
	int resultsViewSelectedMatchId = -1;
	int resultsCountNextMatches = 0;
	int resultsCountLiveMatches = 0;
	
	protected void displayResultsMatchId(int id)
	{
		boolean resultsAreLiveMatch = false;
		boolean isChecking = false;
		if (id >= 0) {
			resultsViewSelectedMatchId = id;
			if ((resultsViewSelectedMatch = server.findActiveMatch(id)) == null) {
				resultsViewSelectedMatch = server.findResultsMatch(id);
				resultsAreLiveMatch = false;
				isChecking = false;
			} else {
				resultsAreLiveMatch = true;
				isChecking = true;
				server.listPredictionResults(resultsViewSelectedMatch, predictionFactory);
			}
//			if (resultsViewSelectedMatch == null) return;
		}
		setResultsSubTitle(resultsViewSelectedMatch);
		
		displayResultsPredictionDetail(null);
		resultsViewPanel.displayResultsMatch(resultsViewSelectedMatch, resultsAreLiveMatch, isChecking, null);	
	}
	
	protected void displayResultsPredictionDetail(Prediction3Goal p)
	{
		if (p != null) {
			resultsDetailPanel.setPrediction(resultsViewSelectedMatch, p);
			resultsContent.setDisplayedChild( resultsContent.indexOfChild(resultsDetailPanel));
		} else {
			resultsContent.setDisplayedChild( resultsContent.indexOfChild(resultsViewPanel));
		}
	}
	
	protected void setResultsSubTitle(Match m)
	{
		String title = "";
		if (m != null) {
			title = m.name;
			if (m.startDate != null) {
				title = title + ", "+Integer.toString(m.startDate.getDate())+"/"+
							Integer.toString(m.startDate.getMonth()+1)+"/"+
							Integer.toString(m.startDate.getYear());
			}
		}
		resultsSelectedName.setText(title);
	}

	protected int rvCheck(int sel)
	{
//		Log.d("GB3P", "rvCheck "+Integer.toString(sel)+" .. "+Integer.toString(allMatchAdapter.getCount()));
		if (sel < 0) sel = 0;
		if (sel >= allMatchAdapter.getCount()-1) {
			if (requestingPredictionResults) {
				sel = allMatchAdapter.getCount()-2;
				if (sel < 0) sel = 0;
			} else {
//				Log.d("GB3P", "backtracking");
				resultsToDate = new Date(resultsFromDate.getTime());
				resultsFromDate.setDate(resultsFromDate.getDate()-7);
				resultsViewTitle.setText("Results since "+MatchTime.dateFormat(resultsFromDate));
				listAllPredictionResults(resultsFromDate, resultsToDate);
	//			sel = allMatchAdapter.getCount()-2;
				return sel;
			}
		}
		if (sel >= 0) {
	    	MatchSpinnerData d = allMatchAdapter.getItem(sel);
	    	if (d != null) {
				displayResultsMatchId(d.matchId);
	    	}
			/*
			var resultsAreLiveMatch = false;
			if (mid >= 0) {
				resultsViewSelectedMatchId = mid;
				var m = null;
				if ((m = sv.findMatch(mid)) != null) {
					resultsViewSelectedMatch = m;
					resultsAreLiveMatch = true;
				} else if ((m=sv.findResultsMatch(mid)) != null) {
					resultsViewSelectedMatch = m;
				} else {
					resultsViewSelectedMatch = null;
				}
			}
			document.resultsViewFullList.displayResultsMatch(
				resultsViewSelectedMatch, resultsAreLiveMatch);
			*/
		}
		return sel;
	}
	
	
	protected boolean addMatchOption(Match m)
	{
		int j=0;
		MatchSpinnerData msd = null;
		for (j=0; j<allMatchAdapter.getCount(); j++) {
			msd = allMatchAdapter.getItem(j);
			if (msd.matchId == m.matchId) {
				return false;
			}
		}
		String text = m.name+" ("+MatchTime.dateFormat(m.startDate)+")";
		msd = new MatchSpinnerData(text, m.matchId);
		allMatchAdapter.add(msd);
		return true;
	}

	//	rebuild the match selector
	protected void setupResultsMatchSelect(int cmid)
	{
		Log.d("GB3P", "setupResultsMatchSelect("+Integer.toString(cmid)+")");
		
		ArrayList<Match> ma = server.allResultsList;
		allMatchAdapter.clear();
		int selected = -1;
		int si = 0;
		int i=0;
		Match m = null;
		ArrayList<Match> maa = new ArrayList<Match>();
		
		i = 0;
		/*
		while ((m=server.nextMatch(i)) != null) {
			maa.add(m);
			i++;
		}
		Collections.sort(maa, new Comparator<Match>() {
			@Override
			public int compare(Match arg0, Match arg1) {
				return (int) (arg0.startDate.getTime()-arg1.startDate.getTime());
			}
		});
		*/
		
		si = 0;
		for (i=0; i<maa.size(); i++) {
			m = maa.get(i);
			if (addMatchOption(m)) {
				if (cmid > 0 && m.matchId == cmid) {
					selected = si;
				}
				si++;
			}
		}
		resultsCountNextMatches = i;
		
		i = 0;
		while ((m=server.liveMatch(i)) != null) {
//			Log.d("GB3P", "adding "+Integer.toString(i)+", "+m.toString());
			maa.add(m);
			i++;
		}
		Collections.sort(maa, new Comparator<Match>() {
			@Override
			public int compare(Match arg0, Match arg1) {
				return (int) (arg1.startDate.getTime()-arg0.startDate.getTime());
			}
		});
		
		si = 0;
		for (i=0; i<maa.size(); i++) {
			m = maa.get(i);
			if (addMatchOption(m)) {
				if (cmid > 0 && m.matchId == cmid) {
					selected = si;
				}
				si++;
			}
		}
		resultsCountLiveMatches = i;
		
//		Log.d("GB3P", "and here "+Integer.toString(cmid)+", "+resultsCountLiveMatches);
		
		if (ma != null) {
			for (Match arm: ma) {
				if (addMatchOption(arm)) {
					if (cmid > 0 && arm.matchId == cmid) {
						selected = si;
					}
					si++;
				}
			}
		}
		
    	MatchSpinnerData d = new MatchSpinnerData("Search for earlier results", -1);
		allMatchAdapter.add(d);
		if (selected >= 0) {
			forceResultSpinner(selected);
			resultsViewSelectedMatchId = cmid;
		}
	}
	
	protected void setResultsMatchTo(int id)
	{
		resultsViewSelectedMatchId = id;
		if (id < 0) {
			int si = resultsSelectSpinner.getSelectedItemPosition();
			int ind = si-resultsCountNextMatches-resultsCountLiveMatches;
			if (server.allResultsList != null && server.allResultsList.size() > 0) {
				if (ind < 0)
					ind = 0;
				else if (ind >= server.allResultsList.size())
					ind = server.allResultsList.size()-1;
				resultsViewSelectedMatchId = id = server.allResultsList.get(ind).matchId;
			}
		}
		
		displayResultsMatchId(id);
		forceResultSpinnerId(id);
	}
	
	protected boolean forceResultSpinnerId(int id)
	{
		for (int i=0; i< allMatchAdapter.getCount(); i++) {
			MatchSpinnerData d = allMatchAdapter.getItem(i);
			if (d.matchId == id) {
				forceResultSpinner(i);
				return true;
			}
		}
		return false;
	}
	
	protected void showResultsScreen(int matchId)
	{
/*
		if (currentMatch != null) {
			nativeControls.showTabBarItems('help', 'bet', 'tracker', 'select');
		} else {
			nativeControls.showTabBarItems('help', 'bet', 'select');
		}
		nativeControls.showTabBar();
		window.scrollTo(0,0);
		var ce = document.activeElement;
		if (ce != null) {
			ce.blur();
		}
		$('#errorViewPanel').hide();
		
		$('#teamScoreTable').hide();
		$('#matchTrackerPanel').hide();
		$('#betPanel').hide();
		$('#betViewPanel').hide();
		$('#helpPanel').hide();
		$('#resultsPanel').show();
*/		

		if (matchId > 0) {
			setupResultsMatchSelect(matchId);
			setResultsMatchTo(matchId);
		} else {
			if (server.allResultsList == null
			       || server.allResultsList.size() == 0) {
				resultsToDate = new Date();
				resultsFromDate = new Date();
				resultsFromDate.setDate(resultsFromDate.getDate()-7);
				listAllPredictionResults(resultsFromDate, resultsToDate);
			} else {
				setupResultsMatchSelect(resultsViewSelectedMatchId);
				setResultsMatchTo(resultsViewSelectedMatchId);
			}
		}
		resetErrDisplay();
	}

	public Team getTeam(int teamId)
	{
		if (server == null) return null;
		return server.findTeam(teamId);
	}
	
	public String getTeamName(int teamId)
	{
		Team t1 = getTeam(teamId);
		return t1 != null? t1.name:("Team "+Integer.toString(teamId));
	}
}