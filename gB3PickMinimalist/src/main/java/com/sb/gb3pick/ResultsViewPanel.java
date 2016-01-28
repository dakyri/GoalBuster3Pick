package com.sb.gb3pick;

import java.util.ArrayList;
import java.util.Date;

import android.content.Context;
import android.text.Html;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sb.FeedEntry;
import com.sb.Match;
import com.sb.MatchTime;
import com.sb.Prediction;
import com.sb.Team;
import com.sb.gb3pick.EventFeedPanel.EventFeedAdapter;
import com.sb.widget.PredictionViewerList;

public class ResultsViewPanel extends PredictionViewerList {

	protected class ResultsViewAdapter extends ArrayAdapter<Prediction>
	{
		LayoutInflater vi = null;
		public ResultsViewAdapter(Context context, int textViewResourceId)
		{
			super(context, textViewResourceId);
			vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		
		private static final int TYPE_ITEM = 0;
		private static final int TYPE_TITLE = 1;
		private static final int TYPE_MAX_COUNT = TYPE_TITLE + 1;
		
		@Override
        public int getItemViewType(int position)
		{
			if (position == 0) return TYPE_TITLE;
			Prediction3Goal p = (Prediction3Goal) getItem(position);
			if (p.type.equals("")) return TYPE_TITLE;
            return TYPE_ITEM;
        }
 
        @Override
        public int getViewTypeCount() {
            return TYPE_MAX_COUNT;
        }
        
        @Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			final Prediction3Goal p = (Prediction3Goal) getItem(position);
//			Log.d("pred", Integer.toString(position)+" "+p.type+" "+Integer.toString(p.matchId)+" "+Integer.toString(p.teamId));
			 // If the View is not cached .. i think it won't matter .. should confirm this somehow,
			// but it will be confusing with 2 view types to use a cached convvertView
			GB3PickMinimalistActivity gb3pa = (GB3PickMinimalistActivity)getContext();
			Match m=null;			

			if (p.type.equals("")) {
				//if (convertView == null) 
				convertView = vi.inflate(R.layout.results_list_subhead_item, null);
				TextView tv = (TextView) convertView;

				if (tv != null) {
					tv.setText(p.result);
				}
			} else {
//				Log.d("GB3P", p.result);
				m = gb3pa.getActiveMatch(p.matchId);
				if (m == null) m = gb3pa.getResultsMatch(p.matchId);
				
				Team tm1 = null;
				Team tm2 = null;
				Team tm3 = null;
				if (gb3pa != null) {
					tm1 = gb3pa.getTeam(p.teamId);
					tm2 = gb3pa.getTeam(p.teamId2);
					tm3 = gb3pa.getTeam(p.teamId3);
				}

				MatchTime t1 = new MatchTime(p.period, p.minute, 0);
				MatchTime t2 = new MatchTime(p.period2, p.minute2, 0);
				MatchTime t3 = new MatchTime(p.period3, p.minute3, 0);
//				if (m != null) {
//					if (m.homeTeam == null) Log.d("GB3P", "null hm"); else Log.d("GB3P", "hm "+m.homeTeam.toString());
//					if (m.awayTeam == null) Log.d("GB3P", "null aw"); else Log.d("GB3P", "aw "+m.awayTeam.toString());
//				} else {
//					Log.d("GB3P", "No Match");
//				}
//				Log.d("GB3P", Integer.toString(p.teamId));
				if (p.betSlotId1 <= 0) {
					p.betSlotId1 = 0;
					if (m != null && m.homeTeam != null && m.awayTeam != null) {
						if (p.teamId == m.homeTeam.teamId) {
							p.betSlotId1 = GB3PickMinimalistActivity.time2BetSlotId(t1, true);
						} else if (p.teamId == m.awayTeam.teamId) {
							p.betSlotId1 = GB3PickMinimalistActivity.time2BetSlotId(t1, false);
						}
					}
				}
				if (p.betSlotId2 <= 0) {
					p.betSlotId2 = 0;
					if (m != null && m.homeTeam != null && m.awayTeam != null) {
						if (p.teamId == m.homeTeam.teamId) {
							p.betSlotId2 = GB3PickMinimalistActivity.time2BetSlotId(t2, true);
						} else if (p.teamId == m.awayTeam.teamId) {
							p.betSlotId2 = GB3PickMinimalistActivity.time2BetSlotId(t2, false);
						}
					}
				}
				if (p.betSlotId3 <= 0) {
					p.betSlotId3 = 0;
					if (m != null && m.homeTeam != null && m.awayTeam != null) {
						if (p.teamId == m.homeTeam.teamId) {
							p.betSlotId3 = GB3PickMinimalistActivity.time2BetSlotId(t3, true);
						} else if (p.teamId == m.awayTeam.teamId) {
							p.betSlotId3 = GB3PickMinimalistActivity.time2BetSlotId(t3, false);
						}
					}
				}

				String resstr="";
				int imgsrc = R.drawable.event_marker_ball;
				if (p.result.equals("W")) {
					resstr="Won";
					imgsrc = R.drawable.event_marker_ball;
				} else if (p.result.equals("L")) {
					resstr="Lost";
					imgsrc = R.drawable.event_marker_cornerflag;
				} else if (p.result.equals("U")) {
					resstr="Open";
					imgsrc = R.drawable.event_marker_cornerflag;
				}
				/*
				predict._prediction = p;
				predView.appendChild(predict);
				*/
				//if (convertView == null)
				String tm1nm = (tm1!=null?tm1.abbr:("TM# "+p.teamId));
				String bs1nm = ((p.betSlotId1 >0)?(", slot "+p.betSlotId1):"");
				String tm2nm = (tm2!=null?tm2.abbr:("TM# "+p.teamId2));
				String bs2nm = ((p.betSlotId2 >0)?(", slot "+p.betSlotId2):"");
				String tm3nm = (tm3!=null?tm3.abbr:("TM# "+p.teamId3));
				String bs3nm = ((p.betSlotId3 >0)?(", slot "+p.betSlotId3):"");
				convertView = vi.inflate(R.layout.results_list_item, null);	
				ImageView iv = (ImageView) convertView.findViewById(R.id.bet_state_logo);
				TextView bv = (TextView) convertView.findViewById(R.id.main_bet);
				TextView sv = (TextView) convertView.findViewById(R.id.bet_state);
				TextView jv = (TextView) convertView.findViewById(R.id.jackpot_slots);
				if (sv != null) {
					sv.setText(resstr);
				}
				if (iv != null) {
					iv.setImageResource(imgsrc);
				}
				if (bv != null) {
					bv.setText(
							"One Goal: "
								+tm1nm+", "
								+GB3PickMinimalistActivity.time2SlotString(t1)
								+bs1nm
						);
				}
				if (jv != null) {
					jv.setText(
							"3 Goal (1): "
								+tm1nm+", "
								+GB3PickMinimalistActivity.time2SlotString(t1)
								+bs1nm+"\n"
							+"3 Goal (2): "
								+tm2nm+", "
								+GB3PickMinimalistActivity.time2SlotString(t2)
								+bs2nm+"\n"
							+"3 Goal (3): "
								+tm3nm+", "
								+GB3PickMinimalistActivity.time2SlotString(t3)
								+bs3nm
						);
				}
			}
			return convertView;
		}

	}

	private ResultsViewAdapter resultsAdapter=null;
	
	protected void setup(Context context)
	{
		setAdapter(resultsAdapter = new ResultsViewAdapter(context, R.layout.results_list_item));
		setDivider(null);
		setDividerHeight(0);
		
		setOnItemClickListener(new OnItemClickListener() {
			Date lastClick = null;
			@Override
			public void onItemClick(AdapterView<?> arg0, View v, int position, long id)
			{
				Date now = new Date();
				if (lastClick != null) {
					if (now.getTime()-lastClick.getTime() < 1000) { // double click
						Prediction3Goal p = null;
						p = (Prediction3Goal) resultsAdapter.getItem(position);
						if (p != null && p.type != "") {
							GB3PickMinimalistActivity gb3pa = (GB3PickMinimalistActivity)getContext();
							Match m=null;
							if (gb3pa != null) {
								gb3pa.displayResultsPredictionDetail(p);
							}
						}
					}
				}
				lastClick = now;
			}
			
		});
	}
	
	/**
	 * @param context
	 * @param attrs
	 */
	public ResultsViewPanel(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		setup(context);
	}

	/**
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public ResultsViewPanel(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		setup(context);
	}
	
	@Override
	public void clear()
	{
		if (resultsAdapter != null) resultsAdapter.clear();
	}

	public void addPredictions(ArrayList<Prediction> feed, String _matchName)
	{
		addPredictions(feed);
	}
	
	@Override
	public void addPredictions(ArrayList<Prediction> feed)
	{
		for (Prediction f: feed) {
			addItem(f, -2);
		}
	}
	
	public void addStringItem(String s)
	{
		Prediction3Goal f3 = new Prediction3Goal();
		f3.type = "";
		f3.matchId = -1;
		f3.result = s;
		addItem(f3, -2);
	}

	@Override
	public void addItem(Prediction f, int pos)
	{
		if (++pos < 0) {
			if (resultsAdapter != null) resultsAdapter.add(f);	
		} else {
			if (resultsAdapter != null) resultsAdapter.insert(f, pos);	
		}
	}
	
	public void displayResultsMatch(
			Match m, boolean isLive, boolean isChecking, int [] slotState)
	{
//		hidePredictionDetails(0);
		int resultCount = 0;
		Log.d("GB3P", "display results "+(m!=null?m.toString():""));
		clear();
		if (m != null && m.predictions != null && m.predictions.size() > 0) {
			for (Prediction p: m.predictions) {
				if (p != null) {
					addItem(p, -1);
					resultCount++;
				}
			}
		} else {
			if (isChecking) {
				addStringItem("Searching for results ...");
			} else {
				addStringItem("No predictions");
			}
		}
	}
}
