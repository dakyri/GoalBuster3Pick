/**
 * 
 */
package com.sb.gb3pick;

import java.util.Date;

import com.sb.Match;
import com.sb.MatchTime;
import com.sb.Team;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

/**
 * @author dak
 *
 */
public class ResultsDetailPanel extends TableLayout {

	/**
	 * @param context
	 */
	public ResultsDetailPanel(Context context)
	{
		super(context);
		setup(context);
	}

	/**
	 * @param context
	 * @param attrs
	 */
	public ResultsDetailPanel(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		setup(context);
	}
	
	protected void setup(Context context)
	{
		setOnClickListener(new OnClickListener() {
			Date lastClick = null;
			@Override
			public void onClick(View arg0) {				
				Date now = new Date();
				if (lastClick != null) {
					if (now.getTime()-lastClick.getTime() < 1000) { // double click
						GB3PickMinimalistActivity gb3pa = (GB3PickMinimalistActivity)getContext();
						if (gb3pa != null) {
							gb3pa.displayResultsPredictionDetail(null);
						}
					}
				}
				lastClick = now;
			}		
		});
	}
	
	protected void addRow(String lbl, String content)
	{
		addRow(lbl, content, null, null);
	}
	
	protected void addRow(String lbl, String c1, String c2, String c3)
	{
		addRow(lbl, c1, c2, c3, R.layout.prediction_detail_row);
	}
	
	protected void addTitleRow(String lbl, String c1, String c2, String c3)
	{
		addRow(lbl, c1, c2, c3, R.layout.prediction_detail_head_row);
	}
	
	protected void addRow(String lbl, String c1, String c2, String c3, int vid)
	{
		ViewGroup v = (ViewGroup) ((Activity) getContext()).getLayoutInflater().inflate(vid, null);
		
		TextView tv = (TextView) v.findViewById(R.id.tr_label);
		if (tv != null) {
			tv.setText(lbl != null? lbl:"");
		}
		int nc = 1;
		tv = (TextView) v.findViewById(R.id.tr_c1);
		if (tv != null) {
			tv.setText(c1 != null? c1:"");
		}
		tv = (TextView) v.findViewById(R.id.tr_c2);
		if (tv != null) {
			if (c2 == null) {
				v.removeView(tv);
				nc++;
			} else {
				tv.setText(c2);
			}
		}
		tv = (TextView) v.findViewById(R.id.tr_c3);
		if (tv != null) {
			if (c3 == null) {
				v.removeView(tv);
				nc++;
			} else {
				tv.setText(c3);
			}
		}
		if (nc > 1) {
			tv = (TextView) v.findViewById(R.id.tr_c1);
			if (tv != null) {
				TableRow.LayoutParams tllp = new TableRow.LayoutParams();
				tllp.span = nc;
				tv.setLayoutParams(tllp);
			}
		}
		addView(v);
	}
	
	public void setPrediction(Match m, Prediction3Goal p)
	{
		if (m == null || p == null) {
			return;
		}
		if (p.details == null) {
			GB3PickMinimalistActivity gb3pa = (GB3PickMinimalistActivity)getContext();
			if (gb3pa != null && gb3pa.server != null) {
				gb3pa.server.getTicketDetails(p);
			}
		}
		removeAllViews();
		Date d = m.startDate;
		String dstr = d.getHours()+":"+d.getMinutes()+", "+d.getDate()+"/"
			+Integer.toString(d.getMonth())+"/"+Integer.toString(d.getYear()+1);
		
		MatchTime t = new MatchTime(p.period, p.minute, 0);
		MatchTime t2 = new MatchTime(p.period2, p.minute2, 0);
		MatchTime t3 = new MatchTime(p.period3, p.minute3, 0);
		GB3PickMinimalistActivity gb3pa = (GB3PickMinimalistActivity)getContext();
		Team tm1 = gb3pa.getTeam(p.teamId);
		Team tm2 = gb3pa.getTeam(p.teamId2);
		Team tm3 = gb3pa.getTeam(p.teamId3);

		addRow("Ticket", p.ticketId);
		addRow("Match", m.name);
		addRow("Match Time", dstr);
		if (p.details != null && p.details.size() >= 1) {
			addRow("Bet Time",
					MatchTime.timeFormat(p.details.get(0).betDate)
				);
		}
		addTitleRow("", "1 Goal/\n3 Goal(1)", "3 Goal(2)", "3 Goal(3)");
		addRow("Team",
			(tm1!=null?tm1.name:("team id "+p.teamId)),
			(tm2!=null?tm2.name:("team id "+p.teamId2)),
			(tm3!=null?tm3.name:("team id "+p.teamId3)));
		if (p.details != null && p.details.size() == 3) {
			addRow("Line Id",
					p.details.get(0).lineId,
					p.details.get(1).lineId,
					p.details.get(2).lineId
				);
		}
		addRow("Bet type", p.type, p.type, p.type);
		addRow("Half", Integer.toString(p.period), Integer.toString(p.period2), Integer.toString(p.period3));
		addRow("Minute", p.minute+":00", p.minute2+":00", p.minute3+":00");
		addRow("Slot",
			(p.betSlotId1>0?Integer.toString(p.betSlotId1):"-"),
			(p.betSlotId2>0?Integer.toString(p.betSlotId2):"-"),
			(p.betSlotId3>0?Integer.toString(p.betSlotId3):"-"));
		if (p.details != null && p.details.size() == 3) {
			addRow("State",
					p.details.get(0).state,
					p.details.get(1).state,
					p.details.get(2).state
				);
		}
	}

}
