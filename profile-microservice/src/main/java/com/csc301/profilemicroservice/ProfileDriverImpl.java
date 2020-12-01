package com.csc301.profilemicroservice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;

import org.springframework.stereotype.Repository;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.Value;
import org.neo4j.driver.v1.Values;

@Repository
public class ProfileDriverImpl implements ProfileDriver {

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
//				figure this out
				String queryStr = "";
				StatementResult second = trans.run(queryStr, Values.parameters("x", userName));
				
			    JSONObject newobject = new JSONObject();

				exit = new DbQueryStatus("OK", DbQueryExecResult.QUERY_OK);
				exit.setData(null);
			}
		}
		return exit;
	}
}
