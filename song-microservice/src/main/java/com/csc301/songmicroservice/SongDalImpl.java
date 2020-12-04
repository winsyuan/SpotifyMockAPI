package com.csc301.songmicroservice;

import java.util.HashMap;
import java.util.Map;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import com.mongodb.client.result.*;

@Repository
public class SongDalImpl implements SongDal {

	private final MongoTemplate db;

	@Autowired
	public SongDalImpl(MongoTemplate mongoTemplate) {
		this.db = mongoTemplate;
	}
	/**
	 * Adds song to mongoDB
	 * @param songToAdd - The song to be inserted into mongo database
	 * @return DbQueryStatus object that specifies the status of the operation 
	 * and this object's data field may be set if the operation has response data
	 */
	@Override
	public DbQueryStatus addSong(Song songToAdd) {
		// TODO Auto-generated method stub
        /*
         * Criteria criteira =
         * Criteria.where(Song.KEY_SONG_NAME).is(songToAdd.getSongName())
         * .and(Song.KEY_SONG_ALBUM).is(songToAdd.getSongAlbum())
         * .and(Song.KEY_SONG_ARTIST_FULL_NAME).is(songToAdd.
         * getSongArtistFullName()); if (this.db.exists(new Query(criteira),
         * Song.class)) { return new DbQueryStatus("Song Already Exist",
         * DbQueryExecResult.QUERY_ERROR_GENERIC); }
         */
	  
	    //Holds response data
		Map<String, String> data = new HashMap<>(); 
		//Song to add
		Song insertedSong = this.db.insert(songToAdd);
		//Adds the keys and values to data map
		data.put(Song.KEY_SONG_NAME, songToAdd.getSongName()); 
		data.put(Song.KEY_SONG_ARTIST_FULL_NAME, songToAdd.getSongArtistFullName()); 
		data.put(Song.KEY_SONG_ALBUM, songToAdd.getSongAlbum()); 
		data.put("songAmountFavourites", String.valueOf(songToAdd.getSongAmountFavourites())); 
		data.put("id", insertedSong.getId()); //Gets the id from inserted song
		//Status OK since we added
		DbQueryStatus status = new DbQueryStatus("OK", DbQueryExecResult.QUERY_OK);
		//Set status objects data field to send in response body
		status.setData(data);
		return status; 
		
	}

	/**
	 * Retrieves song that matches songId and gets its property data 
	 * @param songId - the string that represents the hex ID of the target song
	 * @return DbQueryStatus object that specifies the status of the operation 
     * and this object's data field may be set if the operation has response data
	 */
	@Override
	public DbQueryStatus findSongById(String songId) {
		// TODO Auto-generated method stub
	  //Criteria to check if song with the id exist
	  Criteria criteira = Criteria.where("_id").is(songId);
	  Query query = new Query(criteira); 
	  //Actual check to see if song exists or not
      if (!this.db.exists(query, Song.class)) {
        //Song doesn't exist so return with not found error status
        return new DbQueryStatus("Song not found", DbQueryExecResult.QUERY_ERROR_NOT_FOUND); 
      }
      //The song that matches the id
      Song foundSong = this.db.findOne(query, Song.class); 
      //Data that is a map to send with body that contains all song properties 
      Map<String, String> data = new HashMap<>(); 
      data.put(Song.KEY_SONG_NAME, foundSong.getSongName()); 
      data.put(Song.KEY_SONG_ARTIST_FULL_NAME, foundSong.getSongArtistFullName()); 
      data.put(Song.KEY_SONG_ALBUM, foundSong.getSongAlbum()); 
      data.put("songAmountFavourites", String.valueOf(foundSong.getSongAmountFavourites())); 
      data.put("id", foundSong.getId()); 
      //OK status
      DbQueryStatus status = new DbQueryStatus("OK", DbQueryExecResult.QUERY_OK);
      //set the song properties map as status objects data field 
      status.setData(data);
      return status; 
      
	}

	/**
	 * Gets the song's name for the song that matches songId
	 * @param songId - the string that represents the hex ID of the target song
	 * @return DbQueryStatus object that specifies the status of the operation 
     * and this object's data field may be set if the operation has response data
	 */
	@Override
	public DbQueryStatus getSongTitleById(String songId) {
		// TODO Auto-generated method stub
	  //Criteria to check if song with the id exist
	  Criteria criteira = Criteria.where("_id").is(songId);
	  //Actual check to see if song exists or not
      Query query = new Query(criteira); 
      if (!this.db.exists(query, Song.class)) {
        //Song doesn't exist so return with not found error status
        return new DbQueryStatus("Song not found", DbQueryExecResult.QUERY_ERROR_NOT_FOUND); 
      }
      //Song that matches the id
      Song foundSong = this.db.findOne(query, Song.class);
      //Data to send back is string containing title
      String data = foundSong.getSongName(); 
      DbQueryStatus status = new DbQueryStatus("OK", DbQueryExecResult.QUERY_OK);
      //Set status objects data field to the data string for name to send in response body
      status.setData(data);
      return status; 
      
	}

	/**
	 * Deletes the song that matches the songId from mongo database
	 * @param songId - the string that represents the hex ID of the target song
	 * @return DbQueryStatus object that specifies the status of the operation 
     * and this object's data field may be set if the operation has response data
	 */
	@Override
	public DbQueryStatus deleteSongById(String songId) {
		// TODO Auto-generated method stub
	  //Criteria to check if song with the id exist
	  Criteria criteira = Criteria.where("_id").is(songId);
      Query query = new Query(criteira); 
      //Actual check to see if song exists or not
      if (!this.db.exists(query, Song.class)) {
        //Song doesn't exist so return with not found error status
        return new DbQueryStatus("Song not found", DbQueryExecResult.QUERY_ERROR_NOT_FOUND); 
      }
      //Result of the delete operation 
      DeleteResult deleteResult = this.db.remove(query, Song.class);
      if(deleteResult.getDeletedCount() <= 0) {
        //Nothing got deleted so return error status
        return new DbQueryStatus("Nothing deleted", DbQueryExecResult.QUERY_ERROR_GENERIC);
      }
      //OK status
      return new DbQueryStatus("OK", DbQueryExecResult.QUERY_OK);
	}

	/**
	 * Updates the song that matches songId by incrementing or decrementing coutner by 1
	 * @param songId - the string that represents the hex ID of the target song
	 * @param shouldDecrement - true to specify decrement by 1; false to increment by 1
	 * @return DbQueryStatus object that specifies the status of the operation 
     * and this object's data field may be set if the operation has response data
	 */
	@Override
	public DbQueryStatus updateSongFavouritesCount(String songId, boolean shouldDecrement) {
		// TODO Auto-generated method stub
	  //Criteria to check if song with the id exist
	  Criteria criteira = Criteria.where("_id").is(songId);
      Query query = new Query(criteira); 
      //Actual check to see if song exists or not
      if (!this.db.exists(query, Song.class)) {
        //Song doesn't exist so return with not found error status
        return new DbQueryStatus("Song not found", DbQueryExecResult.QUERY_ERROR_NOT_FOUND); 
      }
      //Update object to specify how to update song document in database
      Update update = new Update(); 
      if(shouldDecrement) {
        //User wants to reduce likes 
        //First we find song
        DbQueryStatus stat = findSongById(songId);
        //Prevent the like counter to drop below 0, error status is attempt is made
        if(Long.parseLong(((Map<String, String>)stat.getData()).get("songAmountFavourites")) <= 0) {
          return new DbQueryStatus("Can't have negative likes", DbQueryExecResult.QUERY_ERROR_GENERIC);  
        }
        //decrement by 1
        update.inc("songAmountFavourites", -1); 
      } else {
        //Increment by 1
        update.inc("songAmountFavourites");
      }
      //Result of update operation 
      UpdateResult updateResult = this.db.updateFirst(query, update, Song.class); 
      //If nothing updated give error status
      if(updateResult.getModifiedCount() <= 0) {
        return new DbQueryStatus("Nothing updated", DbQueryExecResult.QUERY_ERROR_GENERIC);
      }
      //OK status
      return new DbQueryStatus("OK", DbQueryExecResult.QUERY_OK);
	}
}