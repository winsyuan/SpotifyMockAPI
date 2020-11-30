package com.csc301.profilemicroservice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
		try (Session session = driver.session()) {
			try (Transaction trans = session.beginTransaction()) {
				String favList = userName + "-" + "favorites";
				String queryStr = "CREATE (p:profile {userName: $x, fullName: $y, password: $z}) CREATE (w:playlist {plName : $s}) MERGE (p)-[relation:created]->(w)";
				trans.run(queryStr, Values.parameters("x", userName, "y", fullName, "z", password, "s", favList));
				trans.success();
			} 
			session.close();
		}
		DbQueryStatus result = new DbQueryStatus("OK", DbQueryExecResult.QUERY_OK);
		return result;
	}

	@Override
	public DbQueryStatus followFriend(String userName, String frndUserName) {
		try (Session session = driver.session()) {
			try (Transaction trans = session.beginTransaction()) {
				String queryStr = "MATCH (a:profile {userName: $x}), (b:profile {userName: $y}) MERGE (a)-[relation:follows]->(b)";
				trans.run(queryStr, Values.parameters("x", userName, "y", frndUserName));
				trans.success();
			}
			session.close();
		}
		DbQueryStatus result = new DbQueryStatus("OK", DbQueryExecResult.QUERY_OK);
		return result;
	}

	@Override
	public DbQueryStatus unfollowFriend(String userName, String frndUserName) {
		try (Session session = driver.session()) {
			try (Transaction trans = session.beginTransaction()) {
				String queryStr = "MATCH ((:profile {userName: $x})-[r:follows]->(:profile {userName: $y})) DELETE r";
				trans.run(queryStr, Values.parameters("x", userName, "y", frndUserName));
				trans.success();
			}
			session.close();
		}
		DbQueryStatus result = new DbQueryStatus("OK", DbQueryExecResult.QUERY_OK);
		return result;
	}

	@Override
	public DbQueryStatus getAllSongFriendsLike(String userName) {
			
		return null;
	}
}
