package rainbowplayer;

import static java.awt.Desktop.getDesktop;
import java.io.File;
import java.io.IOException;
import rainbowplayer.Classes.Track;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TabPane;
import javafx.scene.input.MouseEvent;
import rainbowplayer.Classes.Duration;
import rainbowplayer.Classes.Playlist;
import rainbowplayer.Classes.PlaylistEntry;
import rainbowplayer.Core.SongPlayer;
import rainbowplayer.db.PlaylistFetcher;
import rainbowplayer.db.TrackFetcher;
import rainbowplayer.db.TrackRemoval;
import rainbowplayer.io.TrackImport;

public class FXMLDocumentController implements Initializable {
    
    private SongPlayer songPlayer;
    
    //Track List Data
    private ArrayList<Track> trackList = new ArrayList<>();
    private int trackCount;
    private boolean trackListLoadError;
    private boolean emptyTrackListWarningEmitted = false;
    
    //Playlist List Data
    private ArrayList<Playlist> playlistList = new ArrayList<>();
    private int playlistCount;
    private boolean playlistLoadError = false;
    private boolean emptyPlaylistListWarningEmitted = false;
    
    //Tracklist item deletion 
    private boolean deleteMode = false;
    
    private HashMap<String, String> playerData = null;
    
   
    @FXML
    private Label ChildTitleLabel;
    @FXML
    private Label ChildAuthorLabel;
    @FXML 
    private Label ChildAlbumLabel;
  
    @FXML
    private Label ChildRemainTimeLabel;
    @FXML
    private Label ChildTotalTimeLabel;
    @FXML
    private Label ChildCurrentTimeLabel;
    @FXML
    private Label playlistLabel;
    
    @FXML
    private Label ChildPlaylistLabel;
    @FXML
    private Label ChildTrackNrTracklistLabel;
    @FXML
    private Button ChildDeleteTracklistButton;
    @FXML
    private TabPane listTabs;
    
    @FXML
    private ListView ChildQueueList;
    @FXML
    private ListView ChildPlaylistList;
    @FXML
    private ListView ChildTracklistList;
    
    @FXML
    private void handleButtonAction(ActionEvent event) {
        ArrayList<PlaylistEntry> tQueueNotebook = new ArrayList<>();
        tQueueNotebook.add(new PlaylistEntry(new Track("C:\\Users\\Tim\\Music\\01 - we came to gangbang.mp3", "we came to gangbang", "goreshit")));
        tQueueNotebook.add(new PlaylistEntry(new Track("C:\\Users\\Tim\\Music\\05 - Awg.mp3", "Awg", "Farin Urlaub Racing Team")));
        Playlist notebook = new Playlist("Tims Notebook-Playlist", tQueueNotebook);
        
        ArrayList<PlaylistEntry> tQueueDesktop = new ArrayList<>();
        tQueueDesktop.add(new PlaylistEntry(new Track("D:\\Musik\\Artists\\Crusher-P\\Echo\\echo.mp3", "Echo", "Crusher-P")));
        tQueueDesktop.add(new PlaylistEntry(new Track("D:\\Musik\\Artists\\Fleetwood Mac\\Fleetwood Mac Greatest Hits\\MP3\\12 Fleetwood Mac - Little Lies.mp3", "Little Lies", "Fleetwood Mac")));
        tQueueDesktop.add(new PlaylistEntry(new Track("D:\\Musik\\Artists\\Rammstein\\Mutter\\05 Feuer frei.mp3", "Feuer Frei", "Rammstein")));
        tQueueDesktop.add(new PlaylistEntry(new Track("D:\\Musik\\Artists\\twenty one pilots\\Blurryface\\02 - Stressed Out.mp3", "Stressed Out", "twenty one pilots")));
        Playlist desktop = new Playlist("Tims Desktop-Playlist", tQueueDesktop);
        desktop.setDescription("Das ist eine Beschreibung.");
        desktop.setTags("Pop, Rock, Electro");
        
        // PlaylistExporter pe = (PlaylistExporter) FeatureManager.getInstance().useFeature("PlaylistExporter");
        // pe.savePlaylistFile(desktop, "C:\\Users\\Tim.WEISSHOME\\Desktop\\desktop.rbpls");
        
        // PlaylistImporter pi = (PlaylistImporter) FeatureManager.getInstance().useFeature("PlaylistImporter");
        // Playlist loaded = pi.loadPlaylist("C:\\Users\\Tim.WEISSHOME\\Desktop\\desktop.rbpls");
        
        songPlayer.playPlaylist(desktop);
        startTimer();
    }
    
    private void startTimer(){
        UiWorkerThread myThread = new UiWorkerThread(playerData, 0, songPlayer, this);
        myThread.start();
    }
    
    /**
     * Replace content of ListView with supplied string ArrayList
     * @param lV
     * @param list 
     */
    public void setListContent(ListView lV, ArrayList<String> list){
	ObservableList oL = FXCollections.observableArrayList(list);
	lV.setItems(oL);
    }
    
    /**
     * Builds a string containing ArrayList content separated by given separator string
     * @param array
     * @param separator
     * @return concatenated string
     */
    public String concatenateArrayListToString(ArrayList<String> array, String separator){
        StringBuilder stringBuilder = new StringBuilder();
        for(String s : array){
            stringBuilder.append(s);
            stringBuilder.append(separator);
        }
        return stringBuilder.toString();
    }
    
    /**
     * Refresh/Populate Track List ListView with tracks retrieved from database
     */
    private void populateTrackList(){
        /*
            Populate TrackList
        */
        
        TrackFetcher trackListFetcher = new TrackFetcher();
        ArrayList<String> trackTitles = new ArrayList<>();
        trackList = new ArrayList<>();
        trackCount = 0;
        
        switch(trackListFetcher.retrieveAllTracks()){
            case "success":
                trackListLoadError = false;
                for(Track t : trackListFetcher.getAllTracks()){
                    ArrayList<String> trackInformation = new ArrayList<>();
                    
                    trackInformation.add((trackCount + 1) + ". " + t.getFormattedTitle());
                    trackInformation.add(t.getAlbumName() + " ("+ t.getReleaseYear() +")");
                    
                    trackList.add(t);
                    trackCount++;
                    trackTitles.add(concatenateArrayListToString(trackInformation, "\n"));
                }
                setListContent(ChildTracklistList,trackTitles);
                ChildTrackNrTracklistLabel.setText(trackCount + " Tracks in Tracklist");
                break;
            case "no_tracks_found":
                trackListLoadError = false;
                setListContent(ChildTracklistList, trackTitles);
                ChildTrackNrTracklistLabel.setText("0 Tracks in Tracklist");
                break;
            case "error":
            default:
                trackListLoadError = true;
                showAlert(AlertType.ERROR, "Could not load tracks", "Oops. An error occurred.", "The track loading process failed somehow, do you want to try it again?");
                setListContent(ChildTracklistList,trackTitles);
                break;
        }
    }
    
    /**
     * Refresh/Populate Playlist ListView with data retrieved from database
     */
    private void populatePlaylistList(){
        /*
            Populate Playlist List
        */
        
        PlaylistFetcher playListFetcher = new PlaylistFetcher();
        ArrayList<String> playlistInfo = new ArrayList<>();
        
        playlistList = new ArrayList<>();
        playlistCount = 0;
        
        switch(playListFetcher.retrieveAllPlaylists()){
            case "success":
                playlistLoadError = false;
                
                for(Playlist p : playListFetcher.getAllPlaylists()){
                    
                    ArrayList<String> playlistShortInformation = new ArrayList<>();
                    int playlistEntryCount = 0;
                    
                    for(PlaylistEntry e : p.getEntries()){
                        playlistEntryCount++;
                    }
                    
                    playlistShortInformation.add(p.getName() + " (" + playlistEntryCount +" Tracks)");
                    playlistShortInformation.add(p.getDescription() + " ("+ p.getTags() +")");
                    
                    playlistList.add(p);
                    playlistCount++;
                    playlistInfo.add(concatenateArrayListToString(playlistShortInformation, "\n"));
                }
                
                setListContent(ChildPlaylistList,playlistInfo);
                ChildPlaylistLabel.setText(playlistCount + " Playlists");
                
                break;
            case "no_playlists_found":
                playlistLoadError = false;
                setListContent(ChildPlaylistList, playlistInfo);
                ChildPlaylistLabel.setText("0 Playlists");
                break;
            case "error":
            default:
                playlistLoadError = true;
                showAlert(AlertType.ERROR, "Could not load playlists", "Oops. An error occurred.", "RainbowPlayer could not load your playlists, do you want to try again?");
                setListContent(ChildPlaylistList,playlistInfo);
                break;
        }
    }
    
    private void handleListViewEvents(){
        ChildTracklistList.setOnMouseClicked((MouseEvent event) -> {
            try{
                int clickedIndex = ChildTracklistList.getSelectionModel().getSelectedIndex();
                if(clickedIndex <= trackCount){
                    Track clickedTrack = trackList.get(clickedIndex);

                    if(!deleteMode){
                        Alert trackInfoAlert = new Alert(AlertType.INFORMATION);
                        trackInfoAlert.setTitle(clickedTrack.getFormattedTitle());
                        trackInfoAlert.setHeaderText(clickedTrack.getTitleName());
                        
                        ArrayList<String> contentText = new ArrayList<>();
                        
                        contentText.add("by " + clickedTrack.getArtistName() + " (" + clickedTrack.getReleaseYear() +")");
                        contentText.add("Album: " + clickedTrack.getAlbumName());
                        contentText.add("Genre: " + clickedTrack.getGenreName());
                        contentText.add("Track ID: " + clickedTrack.getTrackId());
                        contentText.add("Location: " + clickedTrack.getFilePath());
                        
                        trackInfoAlert.setContentText(concatenateArrayListToString(contentText, "\n"));

                        ButtonType openInFileSystemButton = new ButtonType("Open in Explorer", ButtonData.LEFT);
                        ButtonType closeDialog = new ButtonType("Close", ButtonData.CANCEL_CLOSE);

                        trackInfoAlert.getButtonTypes().setAll(openInFileSystemButton, closeDialog);

                        Optional<ButtonType> result = trackInfoAlert.showAndWait();
                        if (result.get() == openInFileSystemButton){
                            try{
                                File trackFile = new File(clickedTrack.getFilePath());
                                getDesktop().open(trackFile.getParentFile());
                            }catch(IOException e){
                                
                            }
                           
                        } else {
                            //cancel
                        }
                    }else{
                        TrackRemoval tRemoval = new TrackRemoval();
                        if(tRemoval.removeTrack(clickedTrack.getTrackId())){
                            populateTrackList();
                        }else{
                            showAlert(AlertType.ERROR, "Could not remove track", "An error occurred!", "The track you selected could not be deleted. Please try again!");
                        }
                    }
                }
            }catch(ArrayIndexOutOfBoundsException e){
                
            }
            
        });
        
        ChildPlaylistList.setOnMouseClicked((MouseEvent event) -> {
            try{
                int clickedIndex = ChildPlaylistList.getSelectionModel().getSelectedIndex();
                if(clickedIndex <= playlistCount){
                    Playlist clickedPlaylist = playlistList.get(clickedIndex);
                    Alert playlistInfoAlert = new Alert(AlertType.INFORMATION);
                    ArrayList<String> contentText = new ArrayList<>();
                    
                    SimpleDateFormat dateDisplayFormat = new SimpleDateFormat("EEEE dd 'of' yyyy kk:mm");
                    String formattedCreationDate = dateDisplayFormat.format(clickedPlaylist.getCreatedAtDate().getTime());
                    
                    playlistInfoAlert.setTitle(clickedPlaylist.getName());
                    playlistInfoAlert.setHeaderText(clickedPlaylist.getName());
                    
                    contentText.add("Description: " + clickedPlaylist.getDescription());
                    contentText.add("Tags: " + clickedPlaylist.getTags());
                    contentText.add("Created " + formattedCreationDate);

                    playlistInfoAlert.setContentText(concatenateArrayListToString(contentText, "\n"));

                    ButtonType closeDialog = new ButtonType("Close", ButtonData.CANCEL_CLOSE);
                    playlistInfoAlert.getButtonTypes().setAll(closeDialog);
                    Optional<ButtonType> result = playlistInfoAlert.showAndWait();

                    if (result.get() == closeDialog){
                        //cancel
                    }
                }
            }catch(ArrayIndexOutOfBoundsException e){
                
            }
        });
        
        ChildQueueList.setOnMouseClicked((MouseEvent event) -> {
            //to be added
        });
    }
    
    /**
     * EventListener of primary TabPane
     */
    private void handleTabPaneEvents(){
        listTabs.getSelectionModel().selectedIndexProperty().addListener((ObservableValue<? extends Number> ov, Number oldTabIndex, Number newTabIndex) -> {
            if(deleteMode){
                deleteMode = false;
                ChildDeleteTracklistButton.setText("Delete Track");
            }
            
            //Playlists Tab
            if(newTabIndex.intValue() == 1){
                if(!playlistLoadError){
                    if(playlistCount == 0){
                        if(!emptyPlaylistListWarningEmitted){
                            showAlert(AlertType.INFORMATION, "Hi there!", "It's empty here!", "Do you want to create some playlists? Just hit the 'Create Playlist' button!");
                            emptyPlaylistListWarningEmitted = true;
                        }
                    }
                }
            }
            //TrackList Tab
            if(newTabIndex.intValue() == 2){
                if(!trackListLoadError){
                    if(trackCount == 0){
                        if(!emptyTrackListWarningEmitted){
                            showAlert(AlertType.INFORMATION, "Hi there!", "It's empty here!", "Do you want to import some tracks? Just hit the 'Import' button!");
                            emptyTrackListWarningEmitted = true;
                        }
                    }
                }
            }
        }); 
    }
    
    /**
     * Display JavaFX Alert
     * @param alertType
     * @param alertTitle
     * @param headerText
     * @param contentText 
     */
    private void showAlert(AlertType alertType, String alertTitle, String headerText, String contentText){
        Alert alert = new Alert(alertType);
        alert.setTitle(alertTitle);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);
        
        alert.showAndWait();
    }
    
    private void timerTick(){}
    
    @FXML
    private void handleStopButtonAction(ActionEvent event) {
        songPlayer.stopPlayback();
    }
    
    @FXML
    private void handlePauseButtonAction(ActionEvent event) {
        songPlayer.pausePlayback();
    }
    
    
    @FXML
    private void handleListButtonAction(ActionEvent event) {
        
    }
    
    @FXML
    private void handleEmptyQueueButtonAction(ActionEvent event) {
        
    }
    
    @FXML
    private void handleLoopButtonAction(ActionEvent event) {
        
    } 
    
    @FXML
    private void handleMixButtonAction(ActionEvent event) {
        
    } 
    
    @FXML
    private void handleLooptButtonAction(ActionEvent event) {
        
    }
    
    @FXML
    private void handleAddToQueueButtonAction(ActionEvent event) {
        
    }
    
    @FXML
    private void handlePlayAllQueueButtonAction(ActionEvent event) {
        
    }
    
    @FXML
    private void handleImportTracklistButtonAction(ActionEvent event) {
        
        TrackImport tImport = new TrackImport();
        String importStatus;
        
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Import Track");
        alert.setHeaderText("Import Track");
        alert.setContentText("You can either import a single track or select multiple tracks to import.");

        ButtonType importSingleTrackButton = new ButtonType("Import Single Track");
        ButtonType importMultiTrackButton = new ButtonType("Import Multiple Tracks");
        ButtonType cancelActionButton = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(importSingleTrackButton, importMultiTrackButton, cancelActionButton);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == importSingleTrackButton){
            //Import single track
            importStatus = tImport.importSingleTrack();
        } else if (result.get() == importMultiTrackButton) {
            //Import multiple tracks
            importStatus = tImport.importMultipleTracks();
        } else {
            //Cancel Import
            importStatus = "cancelled";
        }
        
        switch(importStatus){
            case "success":
                populateTrackList(); //refresh track list
                showAlert(AlertType.INFORMATION, "Import Successful", "Success!", "The track import was successful!");
                break;
            case "no_selection":
                showAlert(AlertType.WARNING, "Import Failed: No Track Selected", "No Track Selected", "Please select a valid track file and try again.");
                break;
            case "invalid_format":
                showAlert(AlertType.WARNING, "Import Failed: Invalid Track Format", "Invalid Track Format", "RainbowPlayer only supports .mp3 and .wav files.");
                break;
            case "cancelled":
                break;
            case "partial_error":
                populateTrackList(); //refresh track list
                showAlert(AlertType.WARNING, "Import Failed: Partial Failure", "Partial Import Failure", "One or more tracks could not be imported successfully. Please try again.");
                break;
            case "error":
            default:
                showAlert(AlertType.ERROR, "Import Failed", "Something went wrong.", "RainbowPlayer could not import your selected track(s) successfully. Please try again.");
                break;
        }
    }
    
    @FXML
    private void handleDeleteTracklistButtonAction(ActionEvent event) {
        if(deleteMode){
            deleteMode = false;
            ChildDeleteTracklistButton.setText("Delete Track");
        }else{
            deleteMode = true;
            ChildDeleteTracklistButton.setText("Exit Delete Mode");
        }
    }
    
    @FXML
    private void handleAddToQueueTracklistButtonAction(ActionEvent event) {
        
    }  
    
    @FXML
    private void handleAddToPlaylistTracklistButtonAction(ActionEvent event) {
        
    }
    
    
    @FXML
    private void handleSkipButtonAction(ActionEvent event) {
        songPlayer.skipSong();
    }
    
    @FXML
    private void handlePrevButtonAction(ActionEvent event) {
        /*prevButton.setGraphic("/uf04a");*/
        songPlayer.prevSong();
    }
    
    @FXML 
    private void handleTracklistMouseClick(MouseEvent event) {
        System.out.println("clicked on " + ChildTracklistList.getSelectionModel().getSelectedItem());
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        songPlayer = new SongPlayer(this);
        playerData = new HashMap<>();
 
        populateTrackList();
        populatePlaylistList();
        handleListViewEvents();
        handleTabPaneEvents();
    }
    
    /**
     * Updates the title and artist in the demo interface.
     */
    public void updateInterface() {
        if(songPlayer.getPlayingTrack() != null) {
            ChildTitleLabel.setText(songPlayer.getPlayingTrack().getTitleName());
            ChildAuthorLabel.setText(songPlayer.getPlayingTrack().getArtistName());
            
            Duration durTotal = songPlayer.getPlayingTrack().getRemainingDuration();
            Duration durRemaining = songPlayer.getPlayingTrack().getRemainingDuration();
            
            ChildRemainTimeLabel.setText(durRemaining.getMinutes() + ":" + durRemaining.getSeconds());
            ChildTotalTimeLabel.setText(durTotal.getMinutes() + ":" + durTotal.getSeconds());
            ChildCurrentTimeLabel.setText(Integer.toString(Integer.parseInt(ChildTotalTimeLabel.getText()) - Integer.parseInt(ChildRemainTimeLabel.getText())));
            
            if(songPlayer.getPlaylist() != null){
                playlistLabel.setText(songPlayer.getPlaylist().getName());
            }
        } else {
            ChildTitleLabel.setText("Title: not available");
            ChildAuthorLabel.setText("Author: not available");
            ChildAlbumLabel.setText("Album: not available");
            ChildRemainTimeLabel.setText("-00:00:00");
            ChildTotalTimeLabel.setText("00:00:00");
            ChildCurrentTimeLabel.setText("00:00:00");
            
        }
    }
}
