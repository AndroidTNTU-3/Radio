package com.example.radio;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

import com.example.radio.StationLoader.ParserCallBack;
import com.example.radio.entity.ResponseInfo;
import com.example.radio.entity.Station;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.widget.ArrayAdapter;

public class ListActivity extends Activity implements ParserCallBack{
		
	private LinkedList<String> mListItems;
	private PullToRefreshListView mPullRefreshListView;
	private ArrayAdapter<String> mAdapter;
	
	
	private ArrayList<String> stationList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list);
		
		StationLoader stationLoader = new StationLoader();
		stationLoader.setParserCallBack(this);
		
		Intent intent = getIntent();		
		String token = intent.getExtras().getString(MainActivity.KEY_TOKEN);
		String url = "http://android-course.comli.com/radios.php?token=" + token;
		
		stationLoader.execute(url);
		
		mPullRefreshListView = (PullToRefreshListView) findViewById(R.id.pull_refresh_list);				

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.list, menu);
		return true;
	}

	@Override
	public void setInfo(ArrayList<Station> array) {
		
		
		stationList = new ArrayList<String>();
		
		for (Station station: array){
			stationList.add(station.getName());
		}

		mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, stationList);
		mPullRefreshListView.setAdapter(mAdapter);
	}
	
	@Override
	public void setResponse(ResponseInfo response){
		AlertDialog dialog = DialogScreen.getDialog(ListActivity.this, DialogScreen.IDD_TOKEN, response.getMessage());
        dialog.show();
	}

}
