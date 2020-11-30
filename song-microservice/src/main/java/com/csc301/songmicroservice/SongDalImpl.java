package com.csc301.songmicroservice;

import java.util.HashMap;
import java.util.Map;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

@Repository
public class SongDalImpl implements SongDal {

	private final MongoTemplate db;

	@Autowired
	public SongDalImpl(MongoTemplate mongoTemplate) {
		this.db = mongoTemplate;
	}

	@Override
	public DbQueryStatus addSong(Song songToAdd) {
		// TODO Auto-generated method stub
	    Criteria criteira = Criteria.where(Song.KEY_SONG_NAME).is(songToAdd.getSongName())
	        .and(Song.KEY_SONG_ALBUM).is(songToAdd.getSongAlbum())
	        .and(Song.KEY_SONG_ARTIST_FULL_NAME).is(songToAdd.getSongArtistFullName());
		if (this.db.exists(new Query(criteira), Song.class)) {
          return new DbQueryStatus("Song Already Exist", DbQueryExecResult.QUERY_ERROR_GENERIC); 
        }
		Map<String, String> data = new HashMap<>(); 
		this.db.insert(songToAdd);
		data.put(Song.KEY_SONG_NAME, songToAdd.getSongName()); 
		data.put(Song.KEY_SONG_ARTIST_FULL_NAME, songToAdd.getSongArtistFullName()); 
		data.put(Song.KEY_SONG_ALBUM, songToAdd.getSongAlbum()); 
		data.put("songAmountFavourites", String.valueOf(songToAdd.getSongAmountFavourites())); 
		data.put("id", songToAdd.getId()); 
		DbQueryStatus status = new DbQueryStatus("OK", DbQueryExecResult.QUERY_OK);
		status.setData(data);
		return status; 
		
	}

	@Override
	public DbQueryStatus findSongById(String songId) {
		// TODO Auto-generated method stub
	  Criteria criteira = Criteria.where("_id").is(songId);
	  Query query = new Query(criteira); 
      if (!this.db.exists(query, Song.class)) {
        return new DbQueryStatus("Song not found", DbQueryExecResult.QUERY_ERROR_NOT_FOUND); 
      }
      Song foundSong = this.db.findOne(query, Song.class); 
      Map<String, String> data = new HashMap<>(); 
      data.put(Song.KEY_SONG_NAME, foundSong.getSongName()); 
      data.put(Song.KEY_SONG_ARTIST_FULL_NAME, foundSong.getSongArtistFullName()); 
      data.put(Song.KEY_SONG_ALBUM, foundSong.getSongAlbum()); 
      data.put("songAmountFavourites", String.valueOf(foundSong.getSongAmountFavourites())); 
      data.put("id", foundSong.getId()); 
      DbQueryStatus status = new DbQueryStatus("OK", DbQueryExecResult.QUERY_OK);
      status.setData(data);
      return status; 
      
	}

	@Override
	public DbQueryStatus getSongTitleById(String songId) {
		// TODO Auto-generated method stub
	  Criteria criteira = Criteria.where("_id").is(songId);
      Query query = new Query(criteira); 
      if (!this.db.exists(query, Song.class)) {
        return new DbQueryStatus("Song not found", DbQueryExecResult.QUERY_ERROR_NOT_FOUND); 
      }
      Song foundSong = this.db.findOne(query, Song.class);
      String data = foundSong.getSongName(); 
      DbQueryStatus status = new DbQueryStatus("OK", DbQueryExecResult.QUERY_OK);
      status.setData(data);
      return status; 
      
	}

	@Override
	public DbQueryStatus deleteSongById(String songId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DbQueryStatus updateSongFavouritesCount(String songId, boolean shouldDecrement) {
		// TODO Auto-generated method stub
		return null;
	}
}