package com.sb.gb3pick;

import java.util.Date;

import com.sb.MatchTime;
import com.sb.Prediction;

public class Prediction3Goal extends Prediction
{
    public int matchId;
    
    public String type;
    public float jackpot;
    
	public int teamId;
	public int period;
	public int minute;
	
	public int timeSlotIdx;
	
	public int betSlotId1;
	public int betSlotId2;
	public int betSlotId3;
	
	public int teamId2;
	public int period2;
	public int minute2;
	
	public int teamId3;
	public int period3;
	public int minute3;

	public Prediction3Goal()
	{
		this(-1, -1,-1,0, 0, 0, "FRE");
	}
	
	public Prediction3Goal(int _pmId, int _matchId, int _teamId, int _period, int _minute,
			float _bet, String _currency)
	{
		this(_pmId, _matchId, _teamId, _period, _minute, _bet, _currency, null, Prediction.Result.OPEN, 0);
	}

	public Prediction3Goal(int _pmId, int _matchId, int _teamId, int _period, int _minute,
			float _bet, String _currency,
			Date _timestamp, String _result, float _jackp)
	{
		super(_pmId, "", _bet, _currency, _timestamp, _result);
		
		type = "G";

		matchId = _matchId;
		teamId = _teamId;
		period = _period;
		minute = _minute;
		
		teamId2 = teamId3 = 0;
		period2 = period3 = 0;
		minute2 = minute3 = 0;
	}
	
	public MatchTime time()
	{
		return new MatchTime(period, minute, 0);
	}
	
	@Override
	public String toString() 
	{
	    return "[" + "Match " + Integer.toString(matchId) 
	    		+ ", " + "Team " + Integer.toString(teamId) + ", " 
	    		+ "Time " + Integer.toString(period) + ":" 
	    		+ Integer.toString(minute) + ", " + "]=>" + result;
	}

	@Override
	public String ticketInputString() {
		return Integer.toString(matchId) + ","
			+ Integer.toString(teamId) + ","
			+ Integer.toString(period) + ","
			+ Integer.toString(minute) + ","
			+ type+","
			+ Integer.toString(teamId2) + ","
			+ Integer.toString(period2) + ","
			+ Integer.toString(minute2) + ","
			+ "G"+","
			+ Integer.toString(teamId3) + ","
			+ Integer.toString(period3) + ","
			+ Integer.toString(minute3) + ","
			+ "G"
			;
	}
	
	@Override
	public Prediction clone()
	{
		Prediction3Goal p = new Prediction3Goal(
				pmId, matchId, teamId, period, minute,
				bet, currency,
				timestamp, result, jackpot);
		p.ticketId = ticketId;
		p.teamId2 = teamId2;
		p.period2 = period2;
		p.minute2 = minute2;
		
		p.teamId3 = teamId3;
		p.period3 = period3;
		p.minute3 = minute3;
		
		return p;
	}

	
	@Override
	public Prediction fromTicketInput(
			int _pmId, String _ticketId,
			float _bet, String _currency,
			Date _timestamp, String _result, String selStr,
			Object... extras)
	{
		String[] selArr = selStr.split(",");
		if (selArr.length < 5) {
			return null;
		}
// <i><s>36,34,2,05,G,36,2,10,G,36,2,00,G</s></i></pml>
// 5, 6, 7
// 9, 10, 11
		int mid = Integer.parseInt(selArr[0]);

		Prediction3Goal pg = new Prediction3Goal(
				mid, mid,
				Integer.parseInt(selArr[1]),
				Integer.parseInt(selArr[2]),
				Integer.parseInt(selArr[3]),
//				selArr[4], // this is now hard wired for this prediction subclass
				_bet,
				_currency,
				_timestamp,
				_result,
				0);
		pg.ticketId = _ticketId;
		if (selArr.length >= 12) {
			pg.teamId2 = Integer.parseInt(selArr[5]);
			pg.teamId3 = Integer.parseInt(selArr[9]);
			pg.period2 = Integer.parseInt(selArr[6]);
			pg.period3 = Integer.parseInt(selArr[10]);
			pg.minute2 = Integer.parseInt(selArr[7]);
			pg.minute3 = Integer.parseInt(selArr[11]);
		}
		if (extras != null && extras.length >= 1) {
			if (extras[0].getClass().getName() == "Float") {
				Float f = (Float) extras[0];
				pg.jackpot = f;
			}
		}
		return pg;
	}
}
