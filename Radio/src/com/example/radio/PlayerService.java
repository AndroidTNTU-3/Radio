package com.example.radio;

import java.io.IOException;
import java.util.ArrayList;

import com.example.radio.entity.Station;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.media.AudioTrack.OnPlaybackPositionUpdateListener;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

public class PlayerService extends Service {
	
	
	public static final String STOP = "com.exmple.radio.STOP";
	public static final String PLAY = "com.exmple.radio.PLAY";
	public static final String PAUSE = "com.exmple.radio.PAUSE";
	public static final String SET_STATION = "com.exmple.radio.SET_STATION";
	
	public interface IMediaPlayerServiceCallBack {
		public void onMediaPlayerPrapared();
	}
	
	IMediaPlayerServiceCallBack mpCallBack;
	
	MediaPlayer mediaPlayer;
	MediaPlayerBinder binder = new MediaPlayerBinder();
	NotificationManager nm;
	Player player;
	MPlayer mPlayer;
	String url;
	String nameStation;
	boolean playerIsFirst = false;

	
	private ArrayList<Station> stations;

	@Override
	public IBinder onBind(Intent arg0) {
		return binder;
	}
	
	public void onCreate() {	
		mediaPlayer = new MediaPlayer();	
	}
	
	 public int onStartCommand(Intent intent, int flags, int startId) {
		 
		 if (intent != null) {
			 
				String action = intent.getAction();	
				if (STOP.equals(action)) {
					onStopPlayer();
				} else if (PLAY.equals(action)){
					onStartPlayer();
				} else if (PAUSE.equals(action)){
					onPausePlayer();
				} else if (SET_STATION.equals(action)){
					sendInfoWidget(nameStation);
				}
		 }
		    
		    return super.onStartCommand(intent, flags, startId);
	 }
	
	public class MediaPlayerBinder extends Binder {
        /**
         * Returns the instance of this service for a client to make method calls on it.
         * @return the instance of this service.
         */
        public PlayerService getService() {
            return PlayerService.this;
        }
 
    }
 
    /**
     * Returns the contained StatefulMediaPlayer
     * @return
     */
    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }
	

	 
	 public void onStopPlayer(){
		 Log.d("DEBUG", "In stop");
		 mediaPlayer.stop();
		 mediaPlayer.release();
		 nameStation = "";
		 sendInfoWidget(nameStation);
		 nm.cancel(1);
	 }
	 
	 public void onPausePlayer(){
		 Log.d("DEBUG", "In stop");
		 mediaPlayer.pause();
	 }
	 
	 public void onStartPlayer(){
		 Log.d("DEBUG", "In stop");
		 mediaPlayer.start();
		 sendNotif();
	 }
	 
	 public boolean isPlayerPlaying(){
		 if (mediaPlayer.isPlaying()) return true;
		 else return false;
	 }
	 
	 
	 public void startPlayer(String url, String nameStation){
		 this.url = url;
		 this.nameStation = nameStation;
		 if (!playerIsFirst) {
				
				playerIsFirst = true;
			} else {
				// check if you have selected another radioStation
				mediaPlayer.stop();
				mediaPlayer.reset();
				
			}
		 //player = new Player(this.getApplicationContext(), mediaPlayer);
		 //player.execute(url);
		 
		 mPlayer = new MPlayer(this.getApplicationContext(), mediaPlayer, url);
		    
	 }
	 
	 public void setStations(ArrayList<Station> mStations){
		 stations = mStations;
	 }
	 
	 private class MPlayer implements OnPreparedListener{
		 private MediaPlayer mediaPlayer;
		    Context context;
		    
		    public MPlayer(Context context, MediaPlayer mmediaPlayer, String url) {
		    	this.context = context;   	
		      	mediaPlayer = mmediaPlayer;

		       // progress = new ProgressDialog(context);
		        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		        try {
					mediaPlayer.setDataSource(url);
					mediaPlayer.prepareAsync();
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalStateException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}           
	           
	            mediaPlayer.setOnPreparedListener(this);
		    }

			@Override
			public void onPrepared(MediaPlayer mp) {
				Log.d("DEBUG", "Player is Prepared");
				sendNotif();
		        sendInfoWidget(nameStation);
				mpCallBack.onMediaPlayerPrapared();
				mediaPlayer.start();
				
			}
		 
	 }
	 
	/*private void sendInfoToWidget(){
		 Intent intent = new Intent(PlayerWidget.ACTION_SET_STATION);
		 intent.putExtra("nameStation", nameStation);
		 sendBroadcast(intent);
	 }*/
	 
	 private void sendInfoWidget(String nameStation){
		 
		 AppWidgetManager manager = AppWidgetManager.getInstance(this.getApplicationContext());
		 ComponentName thisWidget = new ComponentName(this.getApplicationContext(), PlayerWidget.class);
		 
		 RemoteViews remoteView = new RemoteViews(getApplicationContext().getPackageName(), R.layout.widget);
		 remoteView.setTextViewText(R.id.tvwStation,nameStation);
		 
		 manager.updateAppWidget(thisWidget, remoteView);
	 }
	 
	 void sendNotif() {
		 nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		    Notification notif = new Notification(R.drawable.ic_launcher, "MediaPlayer", 
		      System.currentTimeMillis());
		    
		    Intent intent = new Intent(this, MyRefreshListActivity.class);
		    intent.putExtra("stations", stations);
		    PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);
		    
		    notif.setLatestEventInfo(this, "RadioPlayer", nameStation, pIntent);
		    
		    notif.flags |= Notification.FLAG_AUTO_CANCEL;
		    
		    nm.notify(1, notif);
		    
		  }
	
	/* private class Player extends AsyncTask<String, Void, Boolean> {
	    private ProgressDialog progress;
	    private MediaPlayer mediaPlayer;
	    Context context;
	    
	    public Player(Context context, MediaPlayer mmediaPlayer) {
	    	this.context = context;   	
	      	mediaPlayer = mmediaPlayer;
	        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
	    }

	    
	    @Override
	    protected void onPreExecute() {
	        // TODO Auto-generated method stub
	        super.onPreExecute();

	    }
	    
	    @Override
	    protected Boolean doInBackground(String... params) {
	        Boolean prepared;
	        try {
	        	
	            mediaPlayer.setDataSource(params[0]);           
	            mediaPlayer.prepare();
	            prepared = true;
	            
	        } catch (IllegalArgumentException e) {
	            // TODO Auto-generated catch block
	            Log.d("IllegarArgument", e.getMessage());
	            prepared = false;
	            e.printStackTrace();
	        } catch (SecurityException e) {
	            // TODO Auto-generated catch block
	            prepared = false;
	            e.printStackTrace();
	        } catch (IllegalStateException e) {
	            // TODO Auto-generated catch block
	            prepared = false;
	            e.printStackTrace();
	        } catch (IOException e) {
	            // TODO Auto-generated catch block
	            prepared = false;
	            e.printStackTrace();
	        }
	        return prepared;
	    }

	    @Override
	    protected void onPostExecute(Boolean result) {
	        // TODO Auto-generated method stub
	        super.onPostExecute(result);	  
	        sendNotif();
	        mediaPlayer.start();

	    }
	    
	   
	}*/

	public void setOnPlayerServiceCallBack(IMediaPlayerServiceCallBack mpCallBack) {		
		this.mpCallBack = mpCallBack;
	}
		 

}
