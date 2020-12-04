package com.csc301.songmicroservice;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/")
public class SongController {

	@Autowired
	private final SongDal songDal;

	private OkHttpClient client = new OkHttpClient();

	
	public SongController(SongDal songDal) {
		this.songDal = songDal;
	}

	/**
	 * Gets the song by specifying its id
	 * @param songId - id for the song to get
	 * @param request - GET request 
	 * @return response with song data and OK status, or error status
	 */
	@RequestMapping(value = "/getSongById/{songId}", method = RequestMethod.GET)
	public @ResponseBody Map<String, Object> getSongById(@PathVariable("songId") String songId,
			HttpServletRequest request) {

		Map<String, Object> response = new HashMap<String, Object>();
		response.put("path", String.format("GET %s", Utils.getUrl(request)));

		//Calls DB operation
		DbQueryStatus dbQueryStatus = songDal.findSongById(songId);

		//Sets response 
		response.put("message", dbQueryStatus.getMessage());
		response = Utils.setResponseStatus(response, dbQueryStatus.getdbQueryExecResult(), dbQueryStatus.getData());

		//Sends response 
		return response;
	}

	/**
	 * Get name of song by specifying its id
	 * @param songId - id for the song to get title for 
	 * @param request - GET request 
	 * @return response with the song's title name and OK status, or error status
	 */
	@RequestMapping(value = "/getSongTitleById/{songId}", method = RequestMethod.GET)
	public @ResponseBody Map<String, Object> getSongTitleById(@PathVariable("songId") String songId,
			HttpServletRequest request) {

		Map<String, Object> response = new HashMap<String, Object>();
		response.put("path", String.format("GET %s", Utils.getUrl(request)));
		
		//Calls DB operation
		DbQueryStatus dbQueryStatus = songDal.getSongTitleById(songId); 
		
		//Sets response
		response = Utils.setResponseStatus(response, dbQueryStatus.getdbQueryExecResult(), dbQueryStatus.getData());
		
		//Sends response
		return response; 

	}

	/**
	 * Deletes song which has the given id
	 * @param songId - id for the song that is requested to be deleted
	 * @param request - DELETE request 
	 * @return OK status if deleted or error status if error occurred 
	 */
	@RequestMapping(value = "/deleteSongById/{songId}", method = RequestMethod.DELETE)
	public @ResponseBody Map<String, Object> deleteSongById(@PathVariable("songId") String songId,
			HttpServletRequest request) {

		Map<String, Object> response = new HashMap<String, Object>();
		response.put("path", String.format("DELETE %s", Utils.getUrl(request)));
		
		//Request to send the profile microservice to delete from users favorite list
		Request req = new Request.Builder().url("http://localhost:3002/deleteAllSongsFromDb/"+songId).put(RequestBody.create("", MediaType.parse("application/json; charset=utf-8"))).build(); 
		DbQueryStatus dbQueryStatus; //Status to send back
		
		try {
		  Response res = client.newCall(req).execute(); 
		  if(!res.isSuccessful()) {
		    throw new Exception(); //Error sending request
		  }
		  JSONObject ret = new JSONObject(res.body().string()); 
		  if(!ret.getString("status").equals("OK")) {
		    throw new Exception(); //Error in profiles service
		  }
		  //Call DB operation to delete
		  dbQueryStatus = songDal.deleteSongById(songId); 
		  
		} catch(Exception e) {
		  //Error status since error occurred
		  dbQueryStatus = new DbQueryStatus("Error in deleting from favourite lists", DbQueryExecResult.QUERY_ERROR_GENERIC);
		}
		//Set response
		response = Utils.setResponseStatus(response, dbQueryStatus.getdbQueryExecResult(), dbQueryStatus.getData());
		//Send response 
		return response;
	}

	/**
	 * Adds the song with the given name, artist and album to database
	 * @param params - Map of query parameters sent with request specifies the songs name, artist, and album
	 * @param request - POST request
	 * @return response with song that was added with its id and OK status or error status if something went wrong 
	 */
	@RequestMapping(value = "/addSong", method = RequestMethod.POST)
	public @ResponseBody Map<String, Object> addSong(@RequestParam Map<String, String> params,
			HttpServletRequest request) {

		Map<String, Object> response = new HashMap<String, Object>();
		response.put("path", String.format("POST %s", Utils.getUrl(request)));
		
		String name = params.get(Song.KEY_SONG_NAME);
		String artist = params.get(Song.KEY_SONG_ARTIST_FULL_NAME); 
		String album = params.get(Song.KEY_SONG_ALBUM);
		DbQueryStatus dbQueryStatus; //Status to send back
		
		//Missing query parameters 
		if(name == null || artist == null || album == null) {
		  dbQueryStatus = new DbQueryStatus("Missing parameters for song", DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
		} else {
		  //Creates song object to store
		  Song songToAdd = new Song(name, artist, album); 
		  //Calls DB operation
		  dbQueryStatus = songDal.addSong(songToAdd); 
		}
		//Sets response 
		response = Utils.setResponseStatus(response, dbQueryStatus.getdbQueryExecResult(), dbQueryStatus.getData());

		return response;
	}

	/**
	 * Updates the song with the given sonfId by incrementing or decrementing like counter by 1 
	 * @param songId - id of the song to have its likes updated
	 * @param shouldDecrement - true specifies to decrement by 1 and false to increment by 1
	 * @param request - PUT request 
	 * @return OK status if successfully updated or error status if error occurred 
	 */
	@RequestMapping(value = "/updateSongFavouritesCount/{songId}", method = RequestMethod.PUT)
	public @ResponseBody Map<String, Object> updateFavouritesCount(@PathVariable("songId") String songId,
			@RequestParam("shouldDecrement") String shouldDecrement, HttpServletRequest request) {

		Map<String, Object> response = new HashMap<String, Object>();
		response.put("data", String.format("PUT %s", Utils.getUrl(request)));
		
		DbQueryStatus dbQueryStatus; //Status to send back
		
		//Checks if query parameters are valid to convert to boolean 
		if(shouldDecrement == null || (!Boolean.parseBoolean(shouldDecrement) && !shouldDecrement.contains("false"))) {
		  dbQueryStatus = new DbQueryStatus("Must be true or false", DbQueryExecResult.QUERY_ERROR_GENERIC); 
		} else {
		  //Calls DB operation 
		  dbQueryStatus = songDal.updateSongFavouritesCount(songId, Boolean.parseBoolean(shouldDecrement)); 
		}
		//Sets response 
        response = Utils.setResponseStatus(response, dbQueryStatus.getdbQueryExecResult(), dbQueryStatus.getData());

        return response;
	}
}