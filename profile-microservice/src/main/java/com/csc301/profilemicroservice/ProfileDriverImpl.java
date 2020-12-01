package com.csc301.profilemicroservice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;

import org.springframework.stereotype.Repository;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.Value;
import org.neo4j.driver.v1.Values;
import org.neo4j.driver.v1.util.Pair;

@Repository
public class ProfileDriverImpl implements ProfileDriver {

////	CHANGE THIS AFTER DONE TESTING
//	public static String dbUri = "bolt://localhost:7687";
//    public static Driver driver = GraphDatabase.driver(dbUri, AuthTokens.basic("neo4j","1234"));
//    
	Driver driver = ProfileMicroserviceApplication.driver;

	public static void InitProfileDb() {
		String queryStr;

		try (Session session = ProfileMicroserviceApplication.driver.session()) {
			try (Transaction trans = session.beginTransaction()) {
				queryStr = "CREATE CONSTRAINT ON (nProfile:profile) ASSERT exists(nProfile.userName)";
				trans.run(queryStr);

				queryStr = "CREATE CONSTRAINT ON (nProfile:profile) ASSERT exists(nProfile.password)";
				trans.run(queryStr);

				queryStr = "CREATE CONSTRAINT ON (nProfile:profile) ASSERT nProfile.userName IS UNIQUE";
				trans.run(queryStr);

				trans.success();
			}
			session.close();
		}
	}
	
	@Override
	public DbQueryStatus createUserProfile(String userName, String fullName, String password) {
//		need to check if user is already in database
		DbQueryStatus exit;
		try (Session session = driver.session()) {
			try (Transaction trans = session.beginTransaction()) {
				String favList = userName + "-" + "favorites";
				String queryStr = "CREATE (p:profile {userName: $x, fullName: $y, password: $z}) CREATE (w:playlist {plName : $s}) MERGE (p)-[relation:created]->(w) RETURN p";
				StatementResult result = trans.run(queryStr, Values.parameters("x", userName, "y", fullName, "z", password, "s", favList));
				if(result.hasNext()) {
					exit = new DbQueryStatus("OK", DbQueryExecResult.QUERY_OK);

				} else {
					exit = new DbQueryStatus("Error in creating profile", DbQueryExecResult.QUERY_ERROR_GENERIC);
				}
				trans.success();
			} 
			session.close();
		}
		return exit;
	}

	@Override
	public DbQueryStatus followFriend(String userName, String frndUserName) {
		DbQueryStatus exit;
		try (Session session = driver.session()) {
			try (Transaction trans = session.beginTransaction()) {
//				checks if first user is in db
				StatementResult first = trans.run("MATCH (a:profile {userName: $x}) RETURN a", Values.parameters("x", userName));
				if(!first.hasNext()) {
					exit = new DbQueryStatus("Username not found", DbQueryExecResult.QUERY_ERROR_GENERIC);
					return exit;
				}
//				checks if friend user is in db
				StatementResult second = trans.run("MATCH (a:profile {userName: $y}) RETURN a", Values.parameters("y", frndUserName));
				if(!second.hasNext()) {
					exit = new DbQueryStatus("Friend username not found", DbQueryExecResult.QUERY_ERROR_GENERIC);
					return exit;
				}
				String queryStr = "MATCH (a:profile {userName: $x}), (b:profile {userName: $y}) MERGE (a)-[relation:follows]->(b)";
				trans.run(queryStr, Values.parameters("x", userName, "y", frndUserName));
				trans.success();
			}
			session.close();
		}
		exit = new DbQueryStatus("OK", DbQueryExecResult.QUERY_OK);
		return exit;
	}

	@Override
	public DbQueryStatus unfollowFriend(String userName, String frndUserName) {
		DbQueryStatus exit;

		try (Session session = driver.session()) {
			try (Transaction trans = session.beginTransaction()) {
//				checks if first user is in db
				StatementResult first = trans.run("MATCH (a:profile {userName: $x}) RETURN a", Values.parameters("x", userName));
				if(!first.hasNext()) {
					exit = new DbQueryStatus("Username not found", DbQueryExecResult.QUERY_ERROR_GENERIC);
					return exit;
				}
//				checks if friend user is in db
				StatementResult second = trans.run("MATCH (a:profile {userName: $y}) RETURN a", Values.parameters("y", frndUserName));
				if(!second.hasNext()) {
					exit = new DbQueryStatus("Friend username not found", DbQueryExecResult.QUERY_ERROR_GENERIC);
					return exit;
				}
				String queryStr = "MATCH ((:profile {userName: $x})-[r:follows]->(:profile {userName: $y})) DELETE r";
				trans.run(queryStr, Values.parameters("x", userName, "y", frndUserName));
				trans.success();
			}
			session.close();
		}
		exit = new DbQueryStatus("OK", DbQueryExecResult.QUERY_OK);
		return exit;
	}

	@Override
	public DbQueryStatus getAllSongFriendsLike(String userName) {
		DbQueryStatus exit;
		try (Session session = driver.session()) {
			try (Transaction trans = session.beginTransaction()) {
				StatementResult first = trans.run("MATCH (a:profile {userName: $x}) RETURN a", Values.parameters("x", userName));
				if(!first.hasNext()) {
					exit = new DbQueryStatus("Username not found", DbQueryExecResult.QUERY_ERROR_GENERIC);
					return exit;
				}
				String queryStr = "MATCH (p:profile {userName : $x})-[relation:follows]->(b:profile) RETURN (b)";
				StatementResult second = trans.run(queryStr, Values.parameters("x", userName));
			    JSONObject newobject = new JSONObject();
				while(second.hasNext()) {
					Record record = second.next();
				    List<Pair<String, Value>> values = record.fields();
				    String friendPlaylist = values.get(0).value().get("userName").asString() + "-favorites";
//				    find their liked songs
					String queryFriend = "MATCH ((:playlist {plName: $x})-[relation:includes]->(s:song)) RETURN s";
					StatementResult res = trans.run(queryFriend, Values.parameters("x", friendPlaylist));
					ArrayList<String> ids = new ArrayList<>();
					while(res.hasNext()) {
						Record songName = res.next();
					    List<Pair<String, Value>> val = songName.fields();
					    String songMongoId = val.get(0).value().get("songId").asString();
					    ids.add(songMongoId);
					}
					newobject.put(values.get(0).value().get("userName").asString(), ids);
				}
//				System.out.println(newobject);
				exit = new DbQueryStatus("OK", DbQueryExecResult.QUERY_OK);
				exit.setData(newobject);
			}
		}
		return exit;
	}
	
//	public static void main(String[] args) {
//		ProfileDriverImpl test = new ProfileDriverImpl();
//
//		DbQueryStatus status = test.getAllSongFriendsLike("emily");
//
//	}
}
