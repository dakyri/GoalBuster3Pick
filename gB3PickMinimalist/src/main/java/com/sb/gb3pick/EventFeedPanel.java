package com.sb.gb3pick;

import java.util.List;

import com.sb.DataFeed;
import com.sb.FeedEntry;
import com.sb.widget.EventViewer;
import com.sb.widget.EventViewerList;

import android.content.Context;
import android.text.Html;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class EventFeedPanel extends EventViewerList
{
	protected class EventFeedAdapter extends ArrayAdapter<FeedEntry>
	{
		public EventFeedAdapter(Context context, int textViewResourceId)
		{
			super(context, textViewResourceId);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			LayoutInflater vi = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			if (convertView == null) 
				convertView = vi.inflate(R.layout.event_feed_list_item, null);
			TextView tv = (TextView) convertView.findViewById(R.id.event_item_text);
			ImageView iv = (ImageView) convertView.findViewById(R.id.event_item_logo);
	
			FeedEntry f = getItem(position);
			if (tv != null) {
				tv.setText(Html.fromHtml(
						"<font color=\"#ffff00\" size=\"+1\">"+f.t.toString()+"</font>"+" "+
						"<font color=\"#ffff00\" size=\"+1\">"+f.title+"</font>"+" "+
						f.shortTxt
					));
			}
			int imgsrc = R.drawable.event_marker_ball;
			switch (f.kind) {
			case FeedEntry.GAME_EVENT:
			case FeedEntry.GOAL_EVENT:
			case FeedEntry.FOUL_EVENT:
			case FeedEntry.PENALTY_EVENT:
			case FeedEntry.OFFSIDE_EVENT:
			case FeedEntry.SHOT_EVENT:
			case FeedEntry.SAVE_EVENT:
			case FeedEntry.FREEKICK_EVENT:
			case FeedEntry.THROWIN_EVENT:
			case FeedEntry.SUBSTITUTION_EVENT:
			case FeedEntry.SLOT_WIN:
			case FeedEntry.SLOT_LOSE:
			case FeedEntry.SLOT_PENDING:
				imgsrc = R.drawable.event_marker_ball;
				break;
			case FeedEntry.REDCARD_EVENT:
				imgsrc = R.drawable.event_marker_redcard;
				break;
			case FeedEntry.YELLOWCARD_EVENT:
				imgsrc = R.drawable.event_marker_yellowcard;
				break;
			case FeedEntry.CORNER_EVENT:
				imgsrc = R.drawable.event_marker_cornerflag;
				break;
			}
			if (iv != null) {
				iv.setImageResource(imgsrc);
			}
			return convertView;
		}
	}
	
	protected void setup(Context context)
	{
		setAdapter(eventFeedAdapter = new EventFeedAdapter(context, R.layout.event_feed_list_item));
		setDivider(null);
		setDividerHeight(0);
	}
	
	EventFeedAdapter eventFeedAdapter = null;
	public EventFeedPanel(Context context)
	{
		super(context);
		setup(context);
	}
	
	/**
	 * @param context
	 * @param attrs
	 */
	public EventFeedPanel(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		setup(context);
	}

	/**
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public EventFeedPanel(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		setup(context);
	}
	
	public void clear()
	{
		if (eventFeedAdapter != null) eventFeedAdapter.clear();
	}

	public void addItem(FeedEntry f, int pos)
	{
		if (++pos < 0) pos = 0;
		if (eventFeedAdapter != null) eventFeedAdapter.insert(f, pos);
	}
}
