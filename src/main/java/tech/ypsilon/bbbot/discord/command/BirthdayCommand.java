package tech.ypsilon.bbbot.discord.command;

import java.text.SimpleDateFormat;
import java.util.Arrays;
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
import tech.ypsilon.bbbot.ButterBrot;
import tech.ypsilon.bbbot.database.wrapper.BirthdayMongoDBWrapper;
import tech.ypsilon.bbbot.discord.DiscordController;

public class BirthdayCommand extends LegacyCommand {

	public static final boolean NOTIFY_ON_STARTUP = false;

	public static final String COMMAND_PREFIX = "bday";

	public static final String SYNTAX = COMMAND_PREFIX + " set <Geburtsdatum> | remove | notifyhere";
	public static final String NOPERM = "Du hast nicht die ausrechenden Berechtigungen, um diesen Befehl '{}' zu benutzen!";
	public static final String DATEFORMAT = "Du musst das Datum im Format DD.MM. oder DD.MM.YYYY angeben.";

	private static boolean shoutout = false;

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

		try {
			event.getMessage().delete().queue();
		} catch (Exception e) {}

		// Mindestens ein Argument angegeben.
		switch (args[0].toLowerCase()) {
			case "set": {
				// Geburtstag soll gesetzt werden.
				if(args.length > 1) {
					if(!args[1].startsWith("@")) {
						// Eigenener Geburtstag.
						String bday = setBirthday(member.getAsMention(), guild, args, 1, event);
						if(bday != null)
							textChanel.sendMessage("Der Benutzer " + member.getAsMention() + " hat am "+bday+ " Geburtstag.").queue();
					}else {
						// Es wurde jemand getaggt => anderer Geburtstag.
						if(birthdayAdmin) {
							// Geburtstag darf gesetzt werden
							setBirthday("<"+event.getMessage().getContentRaw().split("<")[1].split(">")[0] + ">", guild, args, 2, event);
						}else {
							noPerm(member, event);
						}
					}
				}
				break;
			}
			case "remove":{
				setBirthday(member.getAsMention(), guild, new String[]{"0.0.0"}, 0, event);
				break;
			}
			case "get": {
				if(args.length > 1) {
					String[] usrn = event.getMessage().getContentRaw().split("<");
					HashMap<Long, Date> bdays = BirthdayMongoDBWrapper.getBirthdayEntrys();
					SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.YYYY");
					Arrays.stream(usrn).forEach(name -> {
						name = name.replace(" ", "");
						if(name.startsWith("@")) {
							// By username
							try {
								long userId = Long.parseLong(name.replace("!", "").replace(">", "").replace(" ", "").replace("@", ""));
								// System.out.println(userId);
								String userName = guild.getJDA().retrieveUserById(userId).complete().getAsMention();
								if(bdays.get(userId) != null) {
									String dateString = formatter.format(bdays.get(userId));
									String text = "Der Benutzer " + userName + " hat am " + dateString + " Geburtstag.";
									member.getUser().openPrivateChannel().flatMap(channel -> channel.sendMessage(text)).queue();
								}else {
									member.getUser().openPrivateChannel().flatMap(channel -> channel.sendMessage("Der Benutzer " + userName + " hat keinen Geburtstag angegeben.")).queue();
								}
							}catch (NumberFormatException e) {
								System.err.println("Numberformat-Exception while parsing User-ID: " + name.replace("!", "").replace(">", "").replace(" ", ""));
							}
						} else {
							// By date?
							// FIXME To be implemented...
						}
					});
				} else {
					explainSyntaxError(member, event);
				}
				break;
			}
			case "notify": {
				if(birthdayAdmin) {
					notifyGuild(guild, textChanel);
				} else {
					noPerm(member, event);
				}
				break;
			}
			case "notifyhere": {
				if(birthdayAdmin) {
					BirthdayMongoDBWrapper.setDefaultChannel(guild.getId(), textChanel.getId());
					event.getAuthor().openPrivateChannel().flatMap(channel -> channel.sendMessage("Du hast erfolgreich den BDAY-Broadcast-Kanal festgelegt: " + textChanel.getAsMention())).queue();
				} else {
					noPerm(member, event);
				}
				break;
			}
			case "time": {
				Date date = new Date(System.currentTimeMillis());
				SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss dd.MM.YYYY");
				String dateString = formatter.format(date);
				event.getMember().getUser().openPrivateChannel().flatMap(channel -> channel.sendMessage("Java sees following time: " + dateString)).queue();
				break;
			}
			default:
				explainSyntaxError(member, event);
				break;
		}
	}


	@Override
	public String getDescription() {
		return "Der Geburtstagsbefehl: 'kit bday set <Geburtsdatum>'";
	}



	/**
	 * Starts the NotifierService (automatical broadcast of birthdays)
	 * @param hours amount of hours between auto-notifications
	 */
	@SuppressWarnings("unused")
	public static void startNotifierService(int hours) {
		ButterBrot.LOGGER.info("[Birthday]: Registering the notification-service");

		int notifyTime = 8;

		// Calculate time-offset to 08:00 AM, next day.
		Date date = new Date(System.currentTimeMillis());
		SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
		String dateString = formatter.format(date);
		int hour = Integer.parseInt(dateString.split(":")[0]);
		int min  = Integer.parseInt(dateString.split(":")[1]);
		long delay = notifyTime * 60;
		if(hour < notifyTime) {
			delay = ((notifyTime-1) - hour) * 60 + (60 - min);
		}else {
			delay += (23-hour) * 60 + (60 - min);
		}

		// System.out.println(delay);
		// delay = 1;

		ScheduledExecutorService ses = Executors.newScheduledThreadPool(3);
		ScheduledFuture<?> scheduledFuture = ses.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				try {
					notifyAllGuilds();
				}catch (Exception e) {
					System.err.println("Error while notifying the birthdays :(");
				}
			}
		}, delay, hours * 60, TimeUnit.MINUTES);

		if(NOTIFY_ON_STARTUP) {
			new Thread(() ->  {
				try {
					Thread.sleep(2000);
					notifyAllGuilds();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}).start();
		}

		ButterBrot.LOGGER.info("[Birthday]: Registered the notification-service");

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
		notifyGuild(guild,
				guild.getTextChannelById(
						BirthdayMongoDBWrapper.getDefaultChannel(
								guild.getId())));
	}

	/**
	 * Notifies the given guild in the specified channel
	 * @param guild
	 * @param channel
	 */
	public static void notifyGuild(Guild guild, MessageChannel channel) {
		HashMap<Long, Date> bdays = BirthdayMongoDBWrapper.getBirthdayEntrys();
		shoutout = false;

		bdays.forEach((userId, bDay) -> {
			if(shoutOutBday(userId, bDay, guild, channel))
				shoutout = true;
		});;

		if(!shoutout) {
			channel.sendMessage("Heute gibt es leider keine Geburtstage :(").queue(message -> {
				message.addReaction("U+1F62F").queue();
			});
		}
	}

	/**
	 * Sends a message in the specified channel "channel" of the guild "guild" to tell the user "name" has birthday today.
	 * @param userId
	 * @param bday
	 * @param guild
	 * @param channel
	 * @return
	 */
	public static boolean shoutOutBday(long userId, Date bday, Guild guild, MessageChannel channel) {
		if(hasBirthdayToday(bday)) {
			String userName = guild.getJDA().retrieveUserById(userId).complete().getAsMention();
			int age = 0;
			Date now = new Date(System.currentTimeMillis());
	        SimpleDateFormat formatter = new SimpleDateFormat("YYYY");
	        age = Integer.parseInt(formatter.format(now)) - Integer.parseInt(formatter.format(bday));
	        
			channel.sendMessage(userName + " hat heute Geburtstag und wurde "+age+" Jahre alt!\nHerzlichen Glückwunsch!")
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
	 * @param bday
	 * @return
	 */
	public static boolean hasBirthdayToday(Date bday) {
		Date now = new Date(System.currentTimeMillis());
		SimpleDateFormat formatter = new SimpleDateFormat("dd.MM");
		return formatter.format(now).equals(formatter.format(bday));
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

				event.getAuthor().openPrivateChannel().flatMap(channel -> channel.sendMessage(DATEFORMAT)).queue();
			}
		}else {

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
		BirthdayMongoDBWrapper.addBirthdayEntry(saveName, saveValue);
		return true;
	}

	/**
	 * Sends "member" the message of having no permission.
	 * @param member
	 * @param event
	 */
	private void noPerm(Member member, GuildMessageReceivedEvent event) {
		member.getUser().openPrivateChannel().flatMap(channel -> channel.sendMessage(NOPERM.replace("{}", event.getMessage().getContentDisplay()))).queue();
	}

	/**
	 * Send message about the Syntax.
	 * @param member
	 * @param event
	 */
	private void explainSyntaxError(Member member, GuildMessageReceivedEvent event) {
		member.getUser().openPrivateChannel().flatMap(channel -> channel.sendMessage(SYNTAX)).queue();
	}

	/**
	 * Checks whether the user "member" is in one of the birthday-admin-groups.
	 * @param member
	 * @return
	 */
	public static boolean isBirthdayAdmin(Member member) {
		if(Objects.requireNonNull(member).getRoles().stream().noneMatch(role -> role.getIdLong() == 759072770751201361L
				|| role.getIdLong() == 757718320526000138L) && member.getUser().getIdLong() != 699011153208016926L) {
			return false;
		}
		return true;
	}
}
