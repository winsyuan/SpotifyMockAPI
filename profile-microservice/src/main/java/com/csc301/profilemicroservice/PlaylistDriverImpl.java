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
//		check if song is an actual song in mongodb
		try (Session session = driver.session()) {
			try (Transaction trans = session.beginTransaction()) {
				String favList = userName + "-" + "favorites";
				StatementResult first = trans.run("MATCH (s:song {songId: $a}) RETURN s", Values.parameters("a", songId));
				if(!first.hasNext()) {
					String create = "CREATE (s:song {songId: $x})";
					trans.run(create, Values.parameters("x", songId));
				}
				StatementResult second = trans.run("MATCH (p:profile {userName: $a}) RETURN p", Values.parameters("a", userName));
				if(!second.hasNext()) {
//					user doesn't exist
					DbQueryStatus result = new DbQueryStatus("User not found", DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
					return result;
				}
				StatementResult third = trans.run("MATCH (s:song {songId: $w}), (p:playlist {plName: $y}), (p)-[r:includes]->(s) RETURN r", Values.parameters("w", songId, "y", favList));
				if(third.hasNext()) {
					DbQueryStatus result = new DbQueryStatus("Relation already found", DbQueryExecResult.QUERY_ERROR_GENERIC);
					return result;
				}
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
		try (Session session = driver.session()) {
			try (Transaction trans = session.beginTransaction()) {
				String favList = userName + "-" + "favorites";
				StatementResult first = trans.run("MATCH (s:song {songId: $a}) RETURN s", Values.parameters("a", songId));
				if(!first.hasNext()) {
//					song doesn't exit
					DbQueryStatus result = new DbQueryStatus("Song node not found", DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
					return result;
				}
				StatementResult second = trans.run("MATCH (p:profile {userName: $a}) RETURN p", Values.parameters("a", userName));
				if(!second.hasNext()) {
//					user doesn't exist
					DbQueryStatus result = new DbQueryStatus("User not found", DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
					return result;
				}
				StatementResult third = trans.run("MATCH ((:playlist {plName: $x})-[r:includes]->(:song {songId: $y})) RETURN r", Values.parameters("x", favList, "y", songId));
				if(!third.hasNext()) {
//					relation doesnt exist
					DbQueryStatus result = new DbQueryStatus("Relation not found", DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
					return result;
				}
//				playlist must exist if user exist
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
				StatementResult first = trans.run("MATCH (p:song {songId: $a}) RETURN p", Values.parameters("a", songId));
				if(!first.hasNext()) {
//					song doesn't exit
					DbQueryStatus result = new DbQueryStatus("Song node not found", DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
					return result;
				}
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
