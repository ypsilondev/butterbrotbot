package tech.ypsilon.bbbot.discord.command;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class GBILocationCommand extends Command implements GuildExecuteHandler {
    @Override
    public String[] getAlias() {
        return new String[]{"gbi"};
    }

    @Override
    public String getDescription() {
        return "Liefert den Ort, an dem GBI geschrieben wird (ohne Gewär)!";
    }

    @Override
    public void onExecute(GuildMessageReceivedEvent event, String[] args) {
        Guild guild = event.getGuild();
        TextChannel textChanel = event.getChannel();
        Member member = event.getMember();

        if (args.length != 1) {
            member.getUser().openPrivateChannel().flatMap(channel -> channel.sendMessage("Invalid params!"))
                    .queue();
            return;
        }

        try {
            int matrNbr = Integer.parseInt(args[0]);
            textChanel.sendMessage(this.gbiLocation(matrNbr)).queue();
        } catch (NumberFormatException e) {
            member.getUser().openPrivateChannel().flatMap(
                    channel -> channel.sendMessage("Leider konnte deine Matrikelnummer nicht geparsed werden!"))
                    .queue();
        }
    }

    private String gbiLocation(int matrikelnbr) {
        String location = "";
        String time = "";

        if (1817903 <= matrikelnbr && matrikelnbr <= 2250620) {
            location = "Audimax";
            String data = "Matr.nr. 1817903 bis 1947075 um 8:15 Uhr\n"
                    + "Matr.nr. 1954003 bis 2000184 um 8:20 Uhr\n" + "Matr.nr. 2064964 bis 2113064 um 8:25 Uhr\n"
                    + "Matr.nr. 2118650 bis 2127333 um 8:30 Uhr\n" + "Matr.nr. 2127344 bis 2207676 um 8:35 Uhr\n"
                    + "Matr.nr. 2214591 bis 2228893 um 8:40 Uhr\n" + "Matr.nr. 2236197 bis 2250620 um 8:45 Uhr";
            time = getTime(data, matrikelnbr);
        } else if (2252682 <= matrikelnbr && matrikelnbr <= 2319075) {
            location = "Badnerlandhalle";
            String data = "Matr.nr. 2252682 bis 2278917 um 8:00 Uhr\n"
                    + "Matr.nr. 2279590 bis 2283041 um 8:05 Uhr\n" + "Matr.nr. 2283698 bis 2286846 um 8:10 Uhr\n"
                    + "Matr.nr. 2286857 bis 2289163 um 8:15 Uhr\n" + "Matr.nr. 2289378 bis 2293272 um 8:20 Uhr\n"
                    + "Matr.nr. 2293330 bis 2296395 um 8:25 Uhr\n" + "Matr.nr. 2301497 bis 2302707 um 8:30 Uhr\n"
                    + "Matr.nr. 2304214 bis 2309297 um 8:35 Uhr\n" + "Matr.nr. 2309457 bis 2314376 um 8:40 Uhr\n"
                    + "Matr.nr. 2315619 bis 2319075 um 8:45 Uhr";
            time = getTime(data, matrikelnbr);
        } else if (2319746 <= matrikelnbr && matrikelnbr <= 2328112) {
            location = "Fasanengarten";
            String data = "Matr.nr. 2319746 bis 2322603 um 8:30 Uhr\n"
                    + "Matr.nr. 2322705 bis 2324698 um 8:35 Uhr\n" + "Matr.nr. 2324734 bis 2325919 um 8:40 Uhr\n"
                    + "Matr.nr. 2326412 bis 2328112 um 8:45 Uhr";
            time = getTime(data, matrikelnbr);
        } else if (2328736 <= matrikelnbr && matrikelnbr <= 2355331) {
            location = "Gartenhalle";
            String data = "Matr.nr. 2328736 bis 2351464 um 8:00 Uhr\n"
                    + "Matr.nr. 2351475 bis 2351782 um 8:05 Uhr\n" + "Matr.nr. 2351793 bis 2352230 um 8:10 Uhr\n"
                    + "Matr.nr. 2352274 bis 2352570 um 8:15 Uhr\n" + "Matr.nr. 2352581 bis 2352810 um 8:20 Uhr\n"
                    + "Matr.nr. 2352821 bis 2353039 um 8:25 Uhr\n" + "Matr.nr. 2353051 bis 2353255 um 8:30 Uhr\n"
                    + "Matr.nr. 2353277 bis 2353506 um 8:35 Uhr\n" + "Matr.nr. 2353517 bis 2353959 um 8:40 Uhr\n"
                    + "Matr.nr. 2353960 bis 2355331 um 8:45 Uhr";
            time = getTime(data, matrikelnbr);
        } else if (2355342 <= matrikelnbr && matrikelnbr <= 2369144) {
            location = "großes Zelt beim Forum";
            String data = "Matr.nr. 2355342 bis 2356663 um 8:00 Uhr\n"
                    + "Matr.nr. 2356674 bis 2357871 um 8:05 Uhr\n" + "Matr.nr. 2357882 bis 2359048 um 8:10 Uhr\n"
                    + "Matr.nr. 2359059 bis 2360045 um 8:15 Uhr\n" + "Matr.nr. 2360056 bis 2360501 um 8:20 Uhr\n"
                    + "Matr.nr. 2360523 bis 2363339 um 8:25 Uhr\n" + "Matr.nr. 2363340 bis 2364558 um 8:30 Uhr\n"
                    + "Matr.nr. 2364570 bis 2364843 um 8:35 Uhr\n" + "Matr.nr. 2364854 bis 2367422 um 8:40 Uhr\n"
                    + "Matr.nr. 2367444 bis 2369144 um 8:45 Uhr";
            time = getTime(data, matrikelnbr);
        } else if (2369166 <= matrikelnbr && matrikelnbr <= 2373811) {
            location = "Messtechnik Hörsaal";
            String data = "Matr.nr. 2369166 bis 2369257 um 8:30 Uhr\n"
                    + "Matr.nr. 2369315 bis 2372578 um 8:35 Uhr\n" + "Matr.nr. 2372589 bis 2372932 um 8:40 Uhr\n"
                    + "Matr.nr. 2372943 bis 2373811 um 8:45 Uhr";
            time = getTime(data, matrikelnbr);
        } else if (2373979 <= matrikelnbr && matrikelnbr <= 2391608) {
            location = "Südwerk";
            String data = "Matr.nr. 2373979 bis 2374745 um 8:00 Uhr\n"
                    + "Matr.nr. 2374847 bis 2375055 um 8:05 Uhr\n" + "Matr.nr. 2375077 bis 2375419 um 8:10 Uhr\n"
                    + "Matr.nr. 2375486 bis 2376456 um 8:15 Uhr\n" + "Matr.nr. 2376536 bis 2378690 um 8:20 Uhr\n"
                    + "Matr.nr. 2378703 bis 2380032 um 8:25 Uhr\n" + "Matr.nr. 2380474 bis 2383008 um 8:30 Uhr\n"
                    + "Matr.nr. 2383020 bis 2385333 um 8:35 Uhr\n" + "Matr.nr. 2385344 bis 2387602 um 8:40 Uhr\n"
                    + "Matr.nr. 2387782 bis 2391608 um 8:45 Uhr";
            time = getTime(data, matrikelnbr);
        } else {
            location = "kleines Zelt";
            String data = "Matr.nr. 1115751 bis 2209945 um 8:00 Uhr\n"
                    + "Matr.nr. 2210328 bis 2278133 um 8:05 Uhr\n" + "Matr.nr. 2278508 bis 2308125 um 8:10 Uhr\n"
                    + "Matr.nr. 2309173 bis 2320027 um 8:15 Uhr\n" + "Matr.nr. 2322067 bis 2345575 um 8:20 Uhr\n"
                    + "Matr.nr. 2346023 bis 2347559 um 8:25 Uhr\n" + "Matr.nr. 2347571 bis 2352263 um 8:30 Uhr\n"
                    + "Matr.nr. 2355897 bis 2357199 um 8:35 Uhr\n" + "Matr.nr. 2358512 bis 2364489 um 8:40 Uhr\n"
                    + "Matr.nr. 2365664 bis 2375340 um 8:45 Uhr";
            time = getTime(data, matrikelnbr);
        }


        return String.format("Du (%d) schreibst GBI in %s, Einlass um %s", matrikelnbr, location, time);
    }

    private String getTime(String data, int matrikelNbr) {
        for (String entry : data.split("\n")) {
            String from = entry.split(" ")[1];
            String to = entry.split(" ")[3];
            String time = entry.split(" ")[5];
            // System.out.println(from + "_" + to + "_" + time);
            if (Integer.parseInt(from) <= matrikelNbr && matrikelNbr <= Integer.parseInt(to)) {
                return time;
            }
        }
        return "??:??";
    }

}
