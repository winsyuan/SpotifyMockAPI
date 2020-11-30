package com.csc301.profilemicroservice;

import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.springframework.stereotype.Repository;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.Values;

@Repository
public class PlaylistDriverImpl implements PlaylistDriver {

	Driver driver = ProfileMicroserviceApplication.driver;

	public static void InitPlaylistDb() {
		String queryStr;

		try (Session session = ProfileMicroserviceApplication.driver.session()) {
			try (Transaction trans = session.beginTransaction()) {
				queryStr = "CREATE CONSTRAINT ON (nPlaylist:playlist) ASSERT exists(nPlaylist.plName)";
				trans.run(queryStr);
				trans.success();
			}
			session.close();
		}
	}

	@Override
	public DbQueryStatus likeSong(String userName, String songId) {
//		create a new song node
		try (Session session = driver.session()) {
			try (Transaction trans = session.beginTransaction()) {
				String favList = userName + "-" + "favorites";
//				check if node is already in db *****
				String create = "CREATE (s:song {songId: $x})";
				trans.run(create, Values.parameters("x", songId));

				String queryStr = "MATCH (s:song {songId: $w}), (p:playlist {plName: $y}) MERGE (p)-[relation:includes]->(s)";
				trans.run(queryStr, Values.parameters("w", songId, "y", favList));
				trans.success();
			}
			session.close();
		}
				
//	    add to that playlist the song id `(:playlist)-[:includes]->(:song)`
		DbQueryStatus result = new DbQueryStatus("OK", DbQueryExecResult.QUERY_OK);
		return result;
	}

	@Override
	public DbQueryStatus unlikeSong(String userName, String songId) {
//		find their favorite playlist
//		remove that link to that playlist
		try (Session session = driver.session()) {
			try (Transaction trans = session.beginTransaction()) {
				String favList = userName + "-" + "favorites";
				String queryStr = "MATCH ((:playlist {plName: $x})-[r:includes]->(:song {songId: $y})) DELETE r ";
				trans.run(queryStr, Values.parameters("x", favList, "y", songId));
				trans.success();

			}
			session.close();

			
		}
		DbQueryStatus result = new DbQueryStatus("OK", DbQueryExecResult.QUERY_OK);
		return result;
		
	}

	@Override
	public DbQueryStatus deleteSongFromDb(String songId) {
//		remove song node from db
		try (Session session = driver.session()) {
			try (Transaction trans = session.beginTransaction()) {
				String queryStr = "MATCH (s:song {songId: $x}) DETACH DELETE s";
				trans.run(queryStr, Values.parameters("x", songId));
				trans.success();

			}
			session.close();
		}
		
		DbQueryStatus result = new DbQueryStatus("OK", DbQueryExecResult.QUERY_OK);
		return result;	
	}
}
