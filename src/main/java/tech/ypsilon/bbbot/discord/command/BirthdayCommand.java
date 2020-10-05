package tech.ypsilon.bbbot.discord.command;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import tech.ypsilon.bbbot.database.BirthdayMongoDBWrapper;
import tech.ypsilon.bbbot.discord.DiscordController;

public class BirthdayCommand extends Command{

	public static final String COMMAND_PREFIX = "bday";
	
	public static final String SYNTAX = COMMAND_PREFIX + " set <Geburtsdatum> | remove | notifyhere";
	public static final String NOPERM = "Du hast nicht die ausrechenden Berechtigungen, um diesen Befehl '{}' zu benutzen!";
	public static final String DATEFORMAT = "Du musst das Datum im Format DD.MM. oder DD.MM.YYYY angeben.";
	
	
	@Override
	public String[] getAlias() {
		return new String[] {"bday", "birthday"};
	}

	@Override
	public void onExecute(GuildMessageReceivedEvent event, String[] args) {
		Guild guild = event.getGuild();
		TextChannel textChanel = event.getChannel();
		Member member = event.getMember();
		boolean birthdayAdmin = isBirthdayAdmin(member); 
		

		// Mindestens ein Argument angegeben.
		switch (args[0].toLowerCase()) {
		case "set": {
			event.getMessage().delete().queue();
			// Geburtstag soll gesetzt werden.
			if(args.length > 1) {
				if(!args[1].startsWith("<")) {
					// Eigenener Geburtstag.
					String bday = setBirthday(member.getAsMention(), guild, args, 1, event);
					// event.getMessage().delete().queue();
					if(bday != null)
						textChanel.sendMessage("Der Benutzer " + member.getAsMention() + " hat am "+bday+ " Geburtstag.").queue();
				}else {
					// Es wurde jemand getaggt => anderer Geburtstag.
					if(birthdayAdmin) {
						// Geburtstag darf gesetzt werden
						setBirthday(args[1], guild, args, 2, event);
					}else {
						noPerm(member, event);
					}
				}
			}
			break;
		}
		case "remove":{
			event.getMessage().delete().queue();
			setBirthday(member.getAsMention(), guild, new String[]{"0.0.0"}, 0, event);
			break;
		}
		case "get": {
			if(args.length > 1) {
				event.getMessage().delete().queue();
				// System.out.println("test");
				HashMap<String, String> bdays = BirthdayMongoDBWrapper.getBirthdayEntrys();
				for(int i = 1; i<args.length;i++) {
					// System.out.println(i);
					tellBirthday(member, args[i].replace("!", "").trim(), guild, bdays);
				}
			}else {
				explainSyntaxError(member, event);
			}
			break;
		}
		case "notify": {
			event.getMessage().delete().queue();
			if(birthdayAdmin) {
				notifyGuild(guild, textChanel);						
			} else {
				noPerm(member, event);
			}
			break;
		}
		case "notifyhere": {
			if(birthdayAdmin) {
				event.getMessage().delete().queue();
				BirthdayMongoDBWrapper.setDefaultChannel(guild.getId(), textChanel.getId());						
			} else {
				noPerm(member, event);
			}
			break;
		}
		
		default:
			explainSyntaxError(member, event);
			break;
		}
		
		
		
	} 
	

	@Override
	public String getDescription() {
		return "Der Geburtstagsbefehl.";
	}

	
	
	/**
	 * Starts the NotifierService (automatical broadcast of birthdays)
	 * @param hours amount of hours between auto-notifications
	 */
	@SuppressWarnings("unused")
	public static void startNotifierService(int hours) {
		System.out.println("[Birthday]: Registering the notification-service");
		notifyAllGuilds();
		
		// Calculate time-offset to 08:00 AM, next day.
		Date date = new Date(System.currentTimeMillis());
		SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
		String dateString = formatter.format(date);
		int hour = Integer.parseInt(dateString.split(":")[0]);
		int min  = Integer.parseInt(dateString.split(":")[1]);
		long delay = (23-hour) * 60 * 60 + (60 - min) * 60 + 8 * 60 * 60;
		
		
		ScheduledExecutorService ses = Executors.newScheduledThreadPool(1);
		ScheduledFuture<?> scheduledFuture = ses.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				notifyAllGuilds();
			}
		}, delay, hours * 60 * 60, TimeUnit.SECONDS);
		System.out.println("[Birthday]: registered the notification-service");
	}
	
	
	/**
	 * Notifies all registered guilds about today's birthdays.
	 */
	public static void notifyAllGuilds() {
		BirthdayMongoDBWrapper.getRegisteredGuildIds().forEach(guildId -> notifyGuild(DiscordController.getJDA().getGuildById(guildId)));
	}
	
	/**
	 * Notifies the given guild in the specified "Default channel" (set by notifyhere)
	 * @param guild
	 */
	public static void notifyGuild(Guild guild) {
		notifyGuild(guild, guild.getTextChannelById(BirthdayMongoDBWrapper.getDefaultChannel(guild.getId())));
	}
	
	/**
	 * Notifies the given guild in the specified channel
	 * @param guild
	 * @param channel
	 */
	public static void notifyGuild(Guild guild, MessageChannel channel) {
		HashMap<String, String> bdays = BirthdayMongoDBWrapper.getBirthdayEntrys();
		boolean shoutout = false;
		for(String key : bdays.keySet()) {
			if(bdays.get(key.replace("!", "").trim()) != null) {
				// System.out.println(key + ": " + bdays.get(key));
				if (shoutOutBday(key, guild, channel, bdays))
					shoutout = true;
			}
		}
		if(!shoutout) {
			channel.sendMessage("Heute gibt es leider keine Gebutstage :(").queue();
		}
	}
	
	
	/**
	 * Tells the user "member" when the user "taggedName" has his birthday (specified in the given guild)
	 * @param member
	 * @param taggedName
	 * @param guild
	 * @param bdays
	 */
	private static void tellBirthday(Member member, String taggedName, Guild guild, HashMap<String, String> bdays) {
		if(bdays.get(guild.getId() + "_" + taggedName) != null && !bdays.get(guild.getId() + "_" + taggedName).contains("0.0.")) {
			member.getUser().openPrivateChannel().flatMap(channel -> channel.sendMessage("Der Benutzer " + taggedName + " hat am " + bdays.get(guild.getId() + "_" + taggedName) + " Geburtstag.")).queue();
		}else {
			member.getUser().openPrivateChannel().flatMap(channel -> channel.sendMessage("Der Benutzer " + taggedName + " hat keinen Geburtstag angegeben.")).queue();
		}
	}


	/**
	 * Sends a message in the specified channel "channel" of the guild "guild" to tell the user "name" has birthday today.
	 * @param name
	 * @param guild
	 * @param channel
	 * @param bdays
	 * @return
	 */
	public static boolean shoutOutBday(String name, Guild guild, MessageChannel channel, HashMap<String, String> bdays) {
		if(hasBirthdayToday(bdays.get(name.replace("!", "")).trim())) {
			channel.sendMessage(name.split("_")[1] + " hat heute Geburtstag!\nHerzlichen Glückwunsch!")
			.queue(message -> {
				message.addReaction("U+1F381").queue();
				message.addReaction("U+1F382").queue();
			});
			return true;
		}else {
			// channel.sendMessage(name.split("_")[1] + " hat am " + bdays.get(name.replace("!", "")) + " Geburtstag!").queue();								
		}
		return false;
	}
	
	/**
	 * Returns whether the specified String "dateString" [dd.mm.] equals the current day of the year.
	 * @param dateString
	 * @return
	 */
	public static boolean hasBirthdayToday(String dateString) {
		String[] dateComps = dateString.split("\\."); 
		if(dateComps.length >= 2) {
			int[] d = getDate();
			int day = Integer.parseInt(dateComps[0]);
			int month = Integer.parseInt(dateComps[1]);
			return (d[0] == day && d[1] == month);
		}else {
			return false;
		}
	}
	
	/**
	 * @return Array of int, index 0: current day of the month; index 1: current month of the year.
	 */
	public static int[] getDate() {
		Date date = new Date(System.currentTimeMillis());
		SimpleDateFormat formatter = new SimpleDateFormat("dd");
		int day = Integer.parseInt(formatter.format(date));
		formatter = new SimpleDateFormat("MM");
		int month = Integer.parseInt(formatter.format(date));
		return new int[] {day,month};
	}
	
	
	/**
	 * This functions handles the parsing of the user input after "set". Figures out the date and calls the Database to save the data.
	 * @param asMention
	 * @param guild
	 * @param args
	 * @param start
	 * @param event
	 * @return Success
	 */
	private String setBirthday(String asMention, Guild guild, String[] args, int start, GuildMessageReceivedEvent event) {
		String saveName = guild.getId() + "_" + asMention;
		String saveValue = args[start];
		for(int i = start + 1; i < args.length; i++) {
			saveValue += " " + args[i];
		}
		
		saveValue = saveValue.replaceAll(" ", "");
		String[] dateComponents = saveValue.split("\\.");
		if(dateComponents.length >= 2) {
			try {
				int day = Integer.parseInt(dateComponents[0]);
				int month = Integer.parseInt(dateComponents[1]);
				saveValue = day + "." + month + ".";
				if(dateComponents[2].toCharArray().length > 0) {
					int year = Integer.parseInt(dateComponents[2]);
					saveValue += year;
				}
				if(saveBirthday(saveName, saveValue)) {
					event.getAuthor().openPrivateChannel().flatMap(channel -> channel.sendMessage("Dein Geburtstag wurde gespeichert! :)")).queue();
					return saveValue;
				}else {
					event.getAuthor().openPrivateChannel().flatMap(channel -> channel.sendMessage("Dein Geburtstag konnte nicht gespeichert werden! :(")).queue();
					return null;
				}
			}catch (NumberFormatException e) {
				event.getMessage().delete().queue();
				event.getAuthor().openPrivateChannel().flatMap(channel -> channel.sendMessage(DATEFORMAT)).queue();
			}
		}else {
			// System.out.println("No date");
			event.getMessage().delete().queue();
			event.getAuthor().openPrivateChannel().flatMap(channel -> channel.sendMessage(DATEFORMAT)).queue();
		}
		return null;
	}

	/**
	 * Call the DB to save the birthday.
	 * @param saveName
	 * @param saveValue
	 * @return
	 */
	private boolean saveBirthday(String saveName, String saveValue) {
		saveName = saveName.replace("!", "");
		// System.out.println("SAVING: (" + saveName + "=>" + saveValue + ")");
		BirthdayMongoDBWrapper.addBirthdayEntry(saveName, saveValue);
		return true;
	}

	/**
	 * Sends "member" the message of having no permission.
	 * @param member
	 * @param event
	 */
	private void noPerm(Member member, GuildMessageReceivedEvent event) {
		event.getMessage().delete().queue();
		member.getUser().openPrivateChannel().flatMap(channel -> channel.sendMessage(NOPERM.replace("{}", event.getMessage().getContentDisplay()))).queue();
	}
	
	/**
	 * Send message about the Syntax.
	 * @param member
	 * @param event
	 */
	private void explainSyntaxError(Member member, GuildMessageReceivedEvent event) {
		event.getMessage().delete().queue();
		member.getUser().openPrivateChannel().flatMap(channel -> channel.sendMessage(SYNTAX)).queue();
	}

	/**
	 * Checks whether the user "member" is in one of the birthday-admin-groups.
	 * @param member
	 * @return
	 */
	public static boolean isBirthdayAdmin(Member member) {
		if(Objects.requireNonNull(member).getRoles().stream().noneMatch(role -> role.getIdLong() == 759072770751201361L
                || role.getIdLong() == 757718320526000138L)) {
            return false;
        }
		return true;
	}
	
	
}
