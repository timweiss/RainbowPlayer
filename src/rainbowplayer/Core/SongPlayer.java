package rainbowplayer.Core;

import rainbowplayer.Classes.Track;
import java.io.File;
import java.util.ArrayList;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.util.Timer;
import java.util.TimerTask;
import javafx.util.Duration;
import rainbowplayer.Classes.LiveTrack;
import rainbowplayer.Classes.Playlist;
import rainbowplayer.Classes.PlaylistEntry;
import rainbowplayer.FXMLDocumentController;

/**
 *
 * @author Tim Weiß
 */
public class SongPlayer {
 
    private MediaPlayer mediaPlayer;
    
    private LiveTrack currentTrack;
    private Track currentTitle;
    
    private boolean isPlaying;
    private boolean isQueued;
    
    private int queuePosition = 0;
    
    private Timer songTimer;
    
    private ArrayList<Track> titleQueue;
    private ArrayList<PlaylistEntry> titleQueue2;
    private Playlist currentPlaylist;
    
    // wee need this to update the interface
    private final FXMLDocumentController pInterface;
    
    public SongPlayer(FXMLDocumentController interf) {
        titleQueue = new ArrayList<>();
        titleQueue2 = new ArrayList<>();
        // songTimer = new Timer();
        pInterface = interf;
    }
    
    public void playPlaylist(Playlist plist){
        currentPlaylist = plist;
        playTitleQueue(currentPlaylist.getEntries());
    }
    
    /**
     * The entry point for playing queued titles like playlists.
     * @param tQueue The ArrayList of titles
     */
    public void playTitleQueue(ArrayList<PlaylistEntry> tQueue) {
        if(tQueue.isEmpty())
            return;
        stopPlayback();
        titleQueue2 = tQueue;
        isQueued = true;
        playTitle(titleQueue2.get(queuePosition));
    }
    
    /**
     * Skips the currently playing track and plays the next in queue.
     */
    private void skipTrack(){
        if(titleQueue2.size() > queuePosition + 1 && queuePosition >= 0) {
            queuePosition++;
            playTitle(titleQueue2.get(queuePosition));
        } else {
            stopPlayback();
        }
    }
    
    /**
     * Stops the currently playing track and reverts to the track before the current in the queue.
     */
    private void reverseTrack(){
        if(queuePosition != 0){
            queuePosition--;
            playTitle(titleQueue2.get(queuePosition));
        } else {
            stopPlayback();
        }
    }
    
    /**
     * Plays a track from the given position in the titleQueue2.
     * @param position 
     */
    private void playFromPosition(int position){
        int usablePosition = position -1;
        if(usablePosition >= 0 && usablePosition < titleQueue2.size()){
            playTitle(titleQueue2.get(usablePosition));
        } else {
            stopPlayback();
        }
    }
    
    /**
     * Plays the title from the Title class.
     * @param pe The designated PlaylistEntry.
     */
    public void playTitle(PlaylistEntry pe) {
        Track title = pe.getTrack();
        
        currentTitle = title;
        currentTrack = new LiveTrack(title.getFilePath(), title.getTitleName(), title.getArtistName());
        
        if(!"".equals(title.getAlbumName()))
            currentTrack.setAlbumName(title.getAlbumName());
        
        Media tMedia = new Media(new File(currentTrack.getFilePath()).toURI().toString());
        
        if(mediaPlayer == null) {
            mediaPlayer = new MediaPlayer(tMedia);
        } else {
            mediaPlayer.stop();
            mediaPlayer = new MediaPlayer(tMedia);
        }
        
        mediaPlayer.play();
        isPlaying = true;
        currentTrack.setIsPaused(false);
        
        // we need to run the timer after the player loaded the song
        mediaPlayer.setOnReady(() -> {
            currentTrack.setDuration((int) tMedia.getDuration().toSeconds());
            currentTrack.setTotalSeconds((int) tMedia.getDuration().toSeconds());
            trackStarted();
            changeVolume(setVol);
        });
    }
    
    /**
     * Starts the timer for continuous playback.
     */
    private void trackStarted() {
        if(songTimer != null) {
            songTimer.cancel();
            songTimer.purge();
            songTimer = null;
            songTimer = new Timer();
        } else {
            songTimer = new Timer();
        }
        
        songTimer.schedule(new TimerTask() {
            @Override
            public void run() {
              playbackTimerTick();
            }
          }, 0, 1000);

        songRemainingSeconds = currentTrack.getDuration();
        pInterface.updateInterface();
    }
    
    private int songRemainingSeconds;
    
    private void playbackTimerTick(){
        if(isPlaying){
            songRemainingSeconds--;
            currentTrack.setRemainingSeconds(songRemainingSeconds);
            if(songRemainingSeconds <= 0){
                skipSong();
            }
        }    
    }
    
    /**
     * Pauses the current title and resumes it when paused.
     */
    public void pausePlayback(){
        if(mediaPlayer != null && isPlaying) {
            mediaPlayer.pause();
            isPlaying = false;
            currentTrack.setIsPaused(!isPlaying);
        } else if (mediaPlayer != null) {
            mediaPlayer.play();
            isPlaying = true;
            currentTrack.setIsPaused(!isPlaying);
        }
        pInterface.updateInterface();
    }
    
    /**
     * Stops the playback and resets the player and queue.
     */
    public void stopPlayback(){
        if(mediaPlayer != null && isPlaying) {
            mediaPlayer.stop();
            isPlaying = false;
            isQueued = false;
            
            currentTrack = null;
            queuePosition = 0;
            pInterface.updateInterface();
        }
    }
    /**
     * Jumps to a position in the queue.
     * @param position Position of the track in queue.
     */
    public void jumpInQueue(int position) {
        playFromPosition(position);
    }
    
    double setVol = 1.0;
    
    /**
     * Changes the playback volume to given value.
     * @param vol Desired volume.
     */
    public void changeVolume(double vol) {
        double volume = vol;
        if(vol > 1.0) {
            volume = 1.0;
        }
        if(vol < 0.0) {
            volume = 0.0;
        }
        
        setVol = volume;
        
        mediaPlayer.setVolume(volume);
    }
    
    /**
     * Retrieves the currently playing title.
     * @return The playing title.
     */
    public Track getCurrentTitle() {
        return currentTitle;
    }
    
    public LiveTrack getPlayingTrack(){
        return currentTrack;
    }
    
    public Playlist getPlaylist(){
        return currentPlaylist;
    }
    
    public ArrayList<PlaylistEntry> getPlaybackQueue() {
        return titleQueue2;
    }
    
    /**
     * Skips the current song and triggers the next title.
     */
    public void skipSong() {
        if(isQueued){
            skipTrack();
        }
    }
    
    /**
     * Plays the song played beforehand.
     */
    public void prevSong() {
        if(isQueued){
            reverseTrack();
        }
    }
    
    public boolean isPlaybackActive(){
        return isPlaying;
    }
    
    public void seekSong(int sec){
        if(mediaPlayer != null && isPlaying){
            mediaPlayer.seek(Duration.seconds(sec));
            
            int remSeconds = currentTrack.getTotalDuration().getTotalSeconds() - (int) mediaPlayer.getCurrentTime().toSeconds();
            songRemainingSeconds = remSeconds;
            currentTrack.setRemainingSeconds(remSeconds);
        }
    }
}