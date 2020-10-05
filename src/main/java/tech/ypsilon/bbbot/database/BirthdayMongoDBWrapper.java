package tech.ypsilon.bbbot.database;

import java.util.ArrayList;
import java.util.HashMap;

import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;


public class BirthdayMongoDBWrapper {
	
	public static void addBirthdayEntry(String key, String value) {
		MongoCollection<Document> collection = getCollection();
		Document query = new Document("name", key);
		collection.deleteOne(query);
				
		// Neu
		Document bday = new Document("name", key).append("bday", value);
		collection.insertOne(bday);
	}
	
	
	// FIXME
	public static HashMap<String, String> getBirthdayEntrys() {
		MongoCollection<Document> collection = getCollection();
		// DBObject query = new BasicDBObject("*", "*");
		MongoCursor<Document> cursor = collection.find().cursor();
		Document usr;
		HashMap<String, String> ret = new HashMap<String, String>();
		while(cursor.hasNext()) {
			usr = (Document) cursor.next();
			System.out.println(usr.get("name") + " -> " + usr.get("bday"));
			ret.put((String)usr.get("name"), (String)usr.get("bday"));
		}
		return ret;
	}
	
	private static MongoCollection<Document> getCollection() {
		MongoCollection<Document> collection = MongoController.getInstance().getCollection("birthdays");
		return collection;
	}
	
	public static void setDefaultChannel(String guildID, String channelID) {
		MongoCollection<Document> collection = MongoController.getInstance().getCollection("defaultChannels");
		Document query = new Document("guild", guildID);
		collection.deleteOne(query);
		// Add Channel
		Document channel = new Document("guild", guildID).append("channel", channelID);
		collection.insertOne(channel);
	}
	
	public static String getDefaultChannel(String guildID) {
		MongoCollection<Document> collection = MongoController.getInstance().getCollection("defaultChannels");
		Document query = new Document("guild", guildID);
		MongoCursor<Document> cursor = collection.find(query).cursor();
		if(cursor.hasNext()) {
			return (String)(cursor.next()).get("channel").toString();
		}
		return null;
	}
	
	public static ArrayList<String> getRegisteredGuildIds() {
		MongoCollection<Document> collection = MongoController.getInstance().getCollection("defaultChannels");
		//DBObject query = new BasicDBObject("guild", guildID);
		MongoCursor<Document> cursor = collection.find().cursor();
		ArrayList<String> guildIDs = new ArrayList<String>();
		while(cursor.hasNext()) {
			guildIDs.add((String)((Document) cursor.next()).get("guild"));
		}
		return guildIDs;
	}
}
