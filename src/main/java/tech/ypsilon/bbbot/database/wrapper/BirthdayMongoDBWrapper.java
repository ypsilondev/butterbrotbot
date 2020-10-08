package tech.ypsilon.bbbot.database.wrapper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import tech.ypsilon.bbbot.database.MongoController;
import tech.ypsilon.bbbot.database.codecs.BirthdayCodec;


public class BirthdayMongoDBWrapper {
	
	public static void addBirthdayEntry(String key, String value) {
		MongoCollection<Document> collection = getCollection();
		Document query = new Document("name", key);
		collection.deleteOne(query);

		//For new system

		try {
			SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
			Long aLong = Long.valueOf(key.substring(21, 39));
			Date parse = format.parse(value);
			BirthdayCodec.newBirthday(aLong, parse);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		//End for new system

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
