package com.sb.gb3pick;

import java.util.Date;

import com.sb.Prediction;
import com.sb.PredictionFactory;

public class GB3PredictionFactory implements PredictionFactory
{
	Prediction3Goal	gb3p = new Prediction3Goal();

	public GB3PredictionFactory()
	{
		
	}
	
	@Override
	public Prediction prediction(int _pmId, String _ticketId, float _bet,
			String _currency, Date _timestamp, String _result, String _selStr,
			Object... extras)
	{
		Prediction3Goal p3g = (Prediction3Goal) gb3p.fromTicketInput(_pmId, _ticketId, _bet, _currency, _timestamp, _result, _selStr);
		return p3g;
	}

}
