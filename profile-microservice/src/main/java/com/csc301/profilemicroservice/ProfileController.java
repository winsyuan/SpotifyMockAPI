package com.csc301.profilemicroservice;

import org.json.JSONArray;
import org.json.JSONObject;
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
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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
		if(userName.equals(friendUserName)) {
			status = new DbQueryStatus("Cannot follow yourself", DbQueryExecResult.QUERY_ERROR_GENERIC);
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
		DbQueryStatus status;
		if(userName.equals(null)) {
			status = new DbQueryStatus("Missing parameters", DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
			response = Utils.setResponseStatus(response, status.getdbQueryExecResult(), status.getData());
			return response;
		}
		status = profileDriver.getAllSongFriendsLike(userName);
		if (status.getdbQueryExecResult().equals(DbQueryExecResult.QUERY_ERROR_NOT_FOUND) || status.getdbQueryExecResult().equals(DbQueryExecResult.QUERY_ERROR_GENERIC)) {
			status = new DbQueryStatus("Error finding friends", DbQueryExecResult.QUERY_ERROR_GENERIC);
			response = Utils.setResponseStatus(response, status.getdbQueryExecResult(), null);
			return response;			
		}
		JSONObject output = (JSONObject) status.getData();
		Map<String, Object> newoutput = new HashMap<>();
		Iterator<String> it = output.keys();
		
		try {
			while(it.hasNext()) {
				String name = it.next();
				ArrayList<String> titles = new ArrayList<>();
				JSONArray ids = (JSONArray) output.get(name);
				for(int i = 0; i < ids.length(); i++) {
					Request req = new Request.Builder().url("http://localhost:3001/getSongTitleById/" + ids.get(i)).get().build();
					try {
						Response res = client.newCall(req).execute();
						JSONObject test = new JSONObject(res.body().string());
						String stat = (String) test.get("status");
						if(stat.equals("OK")) {
							String title = (String) test.get("data");
							titles.add(title);
						} else {
							res.body().close();
							throw new Exception();
						};
						res.body().close();
					} catch (Exception e) {
						status = new DbQueryStatus("Error getting song titles", DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
						response = Utils.setResponseStatus(response, status.getdbQueryExecResult(), null);
						return response;	
					}
				}
				newoutput.put(name, titles);
			}
		} catch (Exception e) {
			status = new DbQueryStatus("Error getting song titles", DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
			response = Utils.setResponseStatus(response, status.getdbQueryExecResult(), null);
			return response;	
		}
		response = Utils.setResponseStatus(response, status.getdbQueryExecResult(), newoutput);
		return response;
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
				res.body().close();
				throw new Exception(); 
			}
			res.body().close();
		} catch (Exception  e) {
			status = new DbQueryStatus("Song node not found in MongoDB", DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
			response = Utils.setResponseStatus(response, status.getdbQueryExecResult(), status.getData());
			return response;
		}
		status = playlistDriver.likeSong(userName, songId);
		if(!status.getdbQueryExecResult().toString().equals("QUERY_ERROR_GENERIC")) {
			Request req1 = new Request.Builder().url("http://localhost:3001/updateSongFavouritesCount/" + songId + "?shouldDecrement=false").put(RequestBody.create("", MediaType.parse("application/json; charset=utf-8"))).build(); 
			try {
				Response res1 = client.newCall(req1).execute();
				JSONObject test = new JSONObject(res1.body().string());
				String stat = (String) test.get("status");
				if(!stat.equals("OK")) {
					res1.body().close();
					throw new Exception();
				};
				res1.body().close();
			} catch (Exception e) {
				status = new DbQueryStatus("Error incrementing favorites in MongoDB", DbQueryExecResult.QUERY_ERROR_GENERIC);
				response = Utils.setResponseStatus(response, status.getdbQueryExecResult(), null);
				return response;	
			}
		}
		response = Utils.setResponseStatus(response, DbQueryExecResult.QUERY_OK, status.getData());
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

		status = playlistDriver.unlikeSong(userName, songId);
		if(status.getdbQueryExecResult().toString().equals("QUERY_ERROR_NOT_FOUND")) {
			status = new DbQueryStatus("Relation not found", DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
			response = Utils.setResponseStatus(response, status.getdbQueryExecResult(), status.getData());
			return response;
		}
		Request req = new Request.Builder().url("http://localhost:3001/getSongById/" + songId).get().build();
		try {
			Response res = client.newCall(req).execute();
			if(res.body().string().contains("Song not found")) {
				res.body().close();
				throw new Exception(); 
			}
			res.body().close();
		} catch (Exception  e) {
			status = new DbQueryStatus("Song node not found in MongoDB", DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
			response = Utils.setResponseStatus(response, status.getdbQueryExecResult(), status.getData());
			return response;
		}

		Request req1 = new Request.Builder().url("http://localhost:3001/updateSongFavouritesCount/" + songId + "?shouldDecrement=true").put(RequestBody.create("", MediaType.parse("application/json; charset=utf-8"))).build(); 
		try {
			Response res1 = client.newCall(req1).execute();
			JSONObject test = new JSONObject(res1.body().string());
			String stat = (String) test.get("status");
			if(!stat.equals("OK")) {
				res1.body().close();
				throw new Exception();
			};
			res1.body().close();
		} catch (Exception e) {
			status = new DbQueryStatus("Error incrementing favorites in MongoDB", DbQueryExecResult.QUERY_ERROR_GENERIC);
			response = Utils.setResponseStatus(response, status.getdbQueryExecResult(), null);
			return response;	
		}
		
		response = Utils.setResponseStatus(response, status.getdbQueryExecResult(), status.getData());
		return response;
	}

	@RequestMapping(value = "/deleteAllSongsFromDb/{songId}", method = RequestMethod.PUT)
	public @ResponseBody Map<String, Object> deleteAllSongsFromDb(@PathVariable("songId") String songId,
			HttpServletRequest request) {

		Map<String, Object> response = new HashMap<String, Object>();
		response.put("path", String.format("PUT %s", Utils.getUrl(request)));
		
//		THIS METHOD IS NOT TESTED ITS ALMOST ALWAYS RETURNING "OK" STATUS SO THAT IT WORKS WITH THE MONGODB CALL
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
				res.body().close();
				throw new Exception(); 
			}
			res.body().close();
		} catch (Exception  e) {
			status = new DbQueryStatus("Song node not found in MongoDB", DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
			response = Utils.setResponseStatus(response, status.getdbQueryExecResult(), status.getData());
			return response;
		}
		status = playlistDriver.deleteSongFromDb(songId);
		System.out.println(status.getdbQueryExecResult().toString().equals("QUERY_ERROR_NOT_FOUND"));
		if(status.getdbQueryExecResult().toString().equals("QUERY_ERROR_NOT_FOUND")) {
			status = new DbQueryStatus("OK", DbQueryExecResult.QUERY_OK);
			response = Utils.setResponseStatus(response, status.getdbQueryExecResult(), status.getData());
			return response;
		} 

		response = Utils.setResponseStatus(response, status.getdbQueryExecResult(), status.getData());
		return response;
	}
}