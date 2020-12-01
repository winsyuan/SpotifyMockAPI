package com.csc301.profilemicroservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.csc301.profilemicroservice.Utils;
import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/")
public class ProfileController {
	public static final String KEY_USER_NAME = "userName";
	public static final String KEY_USER_FULLNAME = "fullName";
	public static final String KEY_USER_PASSWORD = "password";

	@Autowired
	private final ProfileDriverImpl profileDriver;

	@Autowired
	private final PlaylistDriverImpl playlistDriver;

	OkHttpClient client = new OkHttpClient();

	public ProfileController(ProfileDriverImpl profileDriver, PlaylistDriverImpl playlistDriver) {
		this.profileDriver = profileDriver;
		this.playlistDriver = playlistDriver;
	}

//	figure out how to properly return the response
//	fix the messages in response
	@RequestMapping(value = "/profile", method = RequestMethod.POST)
	public @ResponseBody Map<String, Object> addProfile(@RequestParam Map<String, String> params,
			HttpServletRequest request) {

		Map<String, Object> response = new HashMap<String, Object>();
		response.put("path", String.format("POST %s", Utils.getUrl(request)));
		
		DbQueryStatus status;
		if(!(params.containsKey(KEY_USER_NAME) && params.containsKey(KEY_USER_FULLNAME) && params.containsKey(KEY_USER_PASSWORD))) {
			status = new DbQueryStatus("Missing parameters", DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
			response = Utils.setResponseStatus(response, status.getdbQueryExecResult(), status.getData());
			return response;
		}
		String userName = params.get(KEY_USER_NAME);
		String fullName = params.get(KEY_USER_FULLNAME);
		String password = params.get(KEY_USER_PASSWORD);
		
		status = profileDriver.createUserProfile(userName, fullName, password);
		response = Utils.setResponseStatus(response, status.getdbQueryExecResult(), status.getData());
		return response;
	}

	@RequestMapping(value = "/followFriend/{userName}/{friendUserName}", method = RequestMethod.PUT)
	public @ResponseBody Map<String, Object> followFriend(@PathVariable("userName") String userName,
			@PathVariable("friendUserName") String friendUserName, HttpServletRequest request) {

		Map<String, Object> response = new HashMap<String, Object>();
		response.put("path", String.format("PUT %s", Utils.getUrl(request)));
		
		DbQueryStatus status;
		if(userName.equals(null) || friendUserName.equals(null)) {
			status = new DbQueryStatus("Missing parameters", DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
			response = Utils.setResponseStatus(response, status.getdbQueryExecResult(), status.getData());
			return response;
		}
		status = profileDriver.followFriend(userName, friendUserName);
		response = Utils.setResponseStatus(response, status.getdbQueryExecResult(), status.getData());
		return response;
	}

	@RequestMapping(value = "/getAllFriendFavouriteSongTitles/{userName}", method = RequestMethod.GET)
	public @ResponseBody Map<String, Object> getAllFriendFavouriteSongTitles(@PathVariable("userName") String userName,
			HttpServletRequest request) {

		Map<String, Object> response = new HashMap<String, Object>();
		response.put("path", String.format("PUT %s", Utils.getUrl(request)));
//		format the data and actually get the songName from mongodb using the mongoid's

		return null;
	}


	@RequestMapping(value = "/unfollowFriend/{userName}/{friendUserName}", method = RequestMethod.PUT)
	public @ResponseBody Map<String, Object> unfollowFriend(@PathVariable("userName") String userName,
			@PathVariable("friendUserName") String friendUserName, HttpServletRequest request) {

		Map<String, Object> response = new HashMap<String, Object>();
		response.put("path", String.format("PUT %s", Utils.getUrl(request)));
		
		DbQueryStatus status;
		if(userName.equals(null) || friendUserName.equals(null)) {
			status = new DbQueryStatus("Missing parameters", DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
			response = Utils.setResponseStatus(response, status.getdbQueryExecResult(), status.getData());
			return response;
		}
		status = profileDriver.unfollowFriend(userName, friendUserName);
		response = Utils.setResponseStatus(response, status.getdbQueryExecResult(), status.getData());
		return response;
	}

	@RequestMapping(value = "/likeSong/{userName}/{songId}", method = RequestMethod.PUT)
	public @ResponseBody Map<String, Object> likeSong(@PathVariable("userName") String userName,
			@PathVariable("songId") String songId, HttpServletRequest request) {

		Map<String, Object> response = new HashMap<String, Object>();
		response.put("path", String.format("PUT %s", Utils.getUrl(request)));

//		check if songId is valid in localhost 3001?
		DbQueryStatus status;
		if(userName.equals(null) || songId.equals(null)) {
			status = new DbQueryStatus("Missing parameters", DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
			response = Utils.setResponseStatus(response, status.getdbQueryExecResult(), status.getData());
			return response;
		}
		Request req = new Request.Builder().url("http://localhost:3001/getSongById/" + songId).get().build();
		try {
			Response res = client.newCall(req).execute();
			
			if(res.body().string().contains("Song not found")) {
				throw new Exception(); 
			}
		} catch (Exception  e) {
			status = new DbQueryStatus("Song node not found in MongoDB", DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
			response = Utils.setResponseStatus(response, status.getdbQueryExecResult(), status.getData());
			return response;
		}
		status = playlistDriver.likeSong(userName, songId);
		response = Utils.setResponseStatus(response, status.getdbQueryExecResult(), status.getData());
		return response;
	}

	@RequestMapping(value = "/unlikeSong/{userName}/{songId}", method = RequestMethod.PUT)
	public @ResponseBody Map<String, Object> unlikeSong(@PathVariable("userName") String userName,
			@PathVariable("songId") String songId, HttpServletRequest request) {

		Map<String, Object> response = new HashMap<String, Object>();
		response.put("path", String.format("PUT %s", Utils.getUrl(request)));

		DbQueryStatus status;
		if(userName.equals(null) || songId.equals(null)) {
			status = new DbQueryStatus("Missing parameters", DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
			response = Utils.setResponseStatus(response, status.getdbQueryExecResult(), status.getData());
			return response;
		}
		Request req = new Request.Builder().url("http://localhost:3001/getSongById/" + songId).get().build();
		try {
			Response res = client.newCall(req).execute();
			if(res.body().string().contains("Song not found")) {
				throw new Exception(); 
			}
		} catch (Exception  e) {
			status = new DbQueryStatus("Song node not found in MongoDB", DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
			response = Utils.setResponseStatus(response, status.getdbQueryExecResult(), status.getData());
			return response;
		}

		status = playlistDriver.unlikeSong(userName, songId);

		response = Utils.setResponseStatus(response, status.getdbQueryExecResult(), status.getData());
		return response;
	}

	@RequestMapping(value = "/deleteAllSongsFromDb/{songId}", method = RequestMethod.PUT)
	public @ResponseBody Map<String, Object> deleteAllSongsFromDb(@PathVariable("songId") String songId,
			HttpServletRequest request) {

		Map<String, Object> response = new HashMap<String, Object>();
		response.put("path", String.format("PUT %s", Utils.getUrl(request)));
//		check if songId is valid in localhost 3001?
		DbQueryStatus status;
		if(songId.equals(null)) {
			status = new DbQueryStatus("Missing parameters", DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
			response = Utils.setResponseStatus(response, status.getdbQueryExecResult(), status.getData());
			return response;
		}
		Request req = new Request.Builder().url("http://localhost:3001/getSongById/" + songId).get().build();
		try {
			Response res = client.newCall(req).execute();
			if(res.body().string().contains("Song not found")) {
				throw new Exception(); 
			}
		} catch (Exception  e) {
			status = new DbQueryStatus("Song node not found in MongoDB", DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
			response = Utils.setResponseStatus(response, status.getdbQueryExecResult(), status.getData());
			return response;
		}
		status = playlistDriver.deleteSongFromDb(songId);

		response = Utils.setResponseStatus(response, status.getdbQueryExecResult(), status.getData());
		return response;
	}
}