package tech.ypsilon.bbbot.discord.command;

import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.components.Button;
import tech.ypsilon.bbbot.ButterBrot;
import tech.ypsilon.bbbot.config.ProfileSubconfig;
import tech.ypsilon.bbbot.util.ActionRowUtil;
import tech.ypsilon.bbbot.util.EmbedUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ProfileCommand extends SlashCommand {

    public static final String BTN_DATA_CREATE_MENU = "createProfileSelectionMenu";
    public static final String BTN_DATA_CREATE_YEAR = "createYearSelectionMenu";
    public static final String BTN_DATA_CREATE_DEGREE = "createTypeSelectionMenu";
    public static final String BTN_DATA_PRODUCTIVE = "productiveRoleToggle";

    private static final String BTN_DATA_RETURN_MAIN = "returnToProfilSelectionMenu";

    private static final String CUSTOM_DELIMITER = "_";

    private static final String BTN_DATA_CATEGORY_PREFIX = "courseCategory";
    private static final String BTN_DATA_COURSE_PREFIX = "course";
    private static final String BTN_DATA_DEGREE_PREFIX = "degree";
    private static final String BTN_DATA_YEAR_PREFIX = "year";

    private final Map<Long, String> roleIdCourseIdMap;
    private final Map<String, Role> courseRoleMap;
    private final Role productivityRole;

    public ProfileCommand(ButterBrot parent) {
        super(parent);

        roleIdCourseIdMap = new ConcurrentHashMap<>();
        fillCourseMap();

        courseRoleMap = new ConcurrentHashMap<>();
        fillRoleMap();

        productivityRole = parent.getDiscordController().getHome()
                .getRoleById(parent.getConfig().getProfile().getProductiveRoleId());
    }

    private void fillCourseMap() {
        getParent().getConfig().getProfile().getCourseCategories().forEach((categoryId, category) -> // for all categories
                category.getCourses().forEach((courseId, course) -> // for each course in category
                        roleIdCourseIdMap.put(course.getDiscordRoleId(), categoryId + CUSTOM_DELIMITER + courseId) // add category to map
                )
        );
    }

    private void fillRoleMap() {
        getParent().getConfig().getProfile().getCourseCategories().forEach((categoryId, category) -> // for all categories
                category.getCourses().forEach((courseId, course) -> // for each course in category
                        courseRoleMap.put(
                                courseId,
                                getParent().getDiscordController().getHome().getRoleById(course.getDiscordRoleId())
                        )
                )
        );
    }

    /**
     * JDA Command Data information used to register
     * and search commands.
     *
     * @return command data
     */
    @Override
    public CommandData commandData() {
        return new CommandData("profile", "Bearbeite dein Server-Rollen-Profil");
    }

    /**
     * Execute is called by the onSlashCommand event when
     * this command should be executed
     *
     * @param event original event
     */
    @Override
    public void execute(SlashCommandEvent event) {
        createMenu(event);
    }

    @Override
    public void handleButtonInteraction(ButtonClickEvent event, String data) {
        if (data == null || data.isBlank() || event.getMember() == null) {
            event.getInteraction().replyEmbeds(EmbedUtil.createErrorEmbed().build()).setEphemeral(true).queue();
            return;
        }

        switch (data.split(CUSTOM_DELIMITER)[0]) {
            case BTN_DATA_CREATE_MENU:
                // initial button clicked
                createMenu(event);
                break;
            case BTN_DATA_RETURN_MAIN:
                // return to main menu
                event.editMessage(createMainMenu(event.getMember())).queue();
                break;
            case BTN_DATA_CREATE_DEGREE:
                // open degree menu
                event.reply(createExpectedDegreeMenu(event.getMember())).setEphemeral(true).queue();
                break;
            case BTN_DATA_CREATE_YEAR:
                event.reply(createStartYearMenu(event.getMember())).setEphemeral(true).queue();
                break;
            case BTN_DATA_PRODUCTIVE:
                handleProductivityToggle(event);
                break;
            case BTN_DATA_CATEGORY_PREFIX:
                // category selected
                String category = data.replaceFirst(BTN_DATA_CATEGORY_PREFIX, "")
                        .replace(CUSTOM_DELIMITER, "");
                event.editMessage(createCategoryMenu(event.getMember(), category)).queue();
                break;
            case BTN_DATA_COURSE_PREFIX:
                // course clicked
                handleCourseButton(event, data);
                break;
            case BTN_DATA_DEGREE_PREFIX:
                // degree button clicked
                handleSingleRoleButtonList(event, data, getParent().getConfig().getProfile().getDegreeRoles(),
                        this::createExpectedDegreeMenu);
                break;
            case BTN_DATA_YEAR_PREFIX:
                // year button clicked
                handleSingleRoleButtonList(event, data, getParent().getConfig().getProfile().getYearRoles(),
                        this::createStartYearMenu);
                break;
            default:
                event.reply("Action not found, please report this Error!").setEphemeral(true).queue();
                break;
        }
    }

    private void handleProductivityToggle(ButtonClickEvent event) {
        Member member = Objects.requireNonNull(event.getMember());
        Guild guild = Objects.requireNonNull(event.getGuild());

        if (member.getRoles().contains(productivityRole)) {
            guild.removeRoleFromMember(member, productivityRole).queue();
            event.reply("Freizeit-Kanäle werden wieder angezeigt.").setEphemeral(true).queue();
        } else {
            guild.addRoleToMember(member, productivityRole).queue();
            event.reply("Freizeit-Kanäle werden **nicht** angezeigt.").setEphemeral(true).queue();
        }
    }

    private void handleCourseButton(ButtonClickEvent event, String data) {
        Member member = Objects.requireNonNull(event.getMember());
        Guild guild = Objects.requireNonNull(event.getGuild());

        event.deferEdit().queue();

        // role selected to add/remove
        String categoryCourseIDs = data.replaceFirst(BTN_DATA_COURSE_PREFIX + CUSTOM_DELIMITER, "");

        // This is to avoid cache problems, the Member#getRoles() List is not always up to date!
        List<Role> roles = new ArrayList<>(member.getRoles());

        // add or remove role
        Role role = courseRoleMap.get(categoryCourseIDs.split(CUSTOM_DELIMITER)[1]);
        if (event.getMember().getRoles().contains(role)) {
            guild.removeRoleFromMember(event.getMember(), role).queue();
            roles.remove(role);
        } else {
            guild.addRoleToMember(event.getMember(), role).queue();
            roles.add(role);
        }

        event.getHook().editOriginal(createCategoryMenu(categoryCourseIDs.split(CUSTOM_DELIMITER)[0], roles)).queue();
    }

    private void handleSingleRoleButtonList(ButtonClickEvent event, String data,
                                            List<ProfileSubconfig.NameEmojiRole> selectionList,
                                            Function<List<Role>, Message> messageSupplier) {
        Guild guild = Objects.requireNonNull(event.getGuild());
        Member member = Objects.requireNonNull(event.getMember());

        event.deferEdit().queue();

        String roleId = data.split(CUSTOM_DELIMITER)[1];
        Role selectedRole = Objects.requireNonNull(guild.getRoleById(roleId));

        List<Role> copyRoleList = new ArrayList<>(member.getRoles());

        if (member.getRoles().contains(selectedRole)) {
            guild.removeRoleFromMember(member, selectedRole).queue();
            copyRoleList.remove(selectedRole);
        } else {
            for (Role memberRole : member.getRoles()) {
                for (ProfileSubconfig.NameEmojiRole degree : selectionList) {
                    if (degree.getDiscordRoleId() == memberRole.getIdLong()) {
                        // remove other roles
                        Role otherDegreeRole = Objects.requireNonNull(guild.getRoleById(degree.getDiscordRoleId()));
                        guild.removeRoleFromMember(member, otherDegreeRole).queue();
                        copyRoleList.remove(otherDegreeRole);
                    }
                }
            }

            guild.addRoleToMember(member, selectedRole).queue();
            copyRoleList.add(selectedRole);
        }

        event.getHook().editOriginal(messageSupplier.apply(copyRoleList)).queue();
    }

    private void createMenu(GenericInteractionCreateEvent event) {
        if (event.getMember() == null) {
            return;
        }

        event.deferReply().setEphemeral(true).queue();
        event.getHook().editOriginal(createMainMenu(event.getMember())).queue();
    }

    @Override
    public void handleSelectionMenu(SelectionMenuEvent event, String data) {
        super.handleSelectionMenu(event, data);
    }

    /**
     * Returns a string to be displayed in the help-command
     *
     * @return the help-string
     */
    @Override
    public String getHelpDescription() {
        return "Mit diesem Befehl kann das Menü zur Rollenauswahl angezeigt werden";
    }

    private Message createMainMenu(Member initiator) {
        MessageBuilder messageBuilder = new MessageBuilder("Hier kannst du deinen Studiengang bearbeiten!\n")
                .append("Um einen Studiengang hinzuzufügen oder zu entfernen, klicke auf eine der Kategorien.\n\n")
                .append("__Aktuell ausgewählte Studiengänge:__\n");

        Set<String> selectedCategories = new HashSet<>();
        // Set<String> selectedCourses = new HashSet<>();

        for (Role role : initiator.getRoles()) {
            if (roleIdCourseIdMap.containsKey(role.getIdLong())) {
                String categoryCourseId = roleIdCourseIdMap.get(role.getIdLong());
                String courseName = getParent().getConfig().getProfile().getCourseCategories()
                        .get(categoryCourseId.split(CUSTOM_DELIMITER)[0]).getCourses()
                        .get(categoryCourseId.split(CUSTOM_DELIMITER)[1]).getName();

                messageBuilder.append("- ").append(courseName).append("\n");

                selectedCategories.add(categoryCourseId.split(CUSTOM_DELIMITER)[0]);
                // selectedCourses.add(categoryCourseId.split(CUSTOM_DELIMITER)[1]);
            }
        }

        messageBuilder.append("\nKlicke einen Knopf um dir die Rolle zuzuweisen.");

        List<Button> categoryButtons = getParent().getConfig().getProfile()
                .getCourseCategories()
                .entrySet()
                .stream()
                .map(entry -> {
                    if (selectedCategories.contains(entry.getKey())) {
                        return Button.success(
                                createButtonId(BTN_DATA_CATEGORY_PREFIX + CUSTOM_DELIMITER + entry.getKey()),
                                entry.getValue().getUnicodeEmoji() + " " + entry.getValue().getName()
                        );
                    } else {
                        return Button.primary(
                                createButtonId(BTN_DATA_CATEGORY_PREFIX + CUSTOM_DELIMITER + entry.getKey()),
                                entry.getValue().getUnicodeEmoji() + " " + entry.getValue().getName()
                        );
                    }
                }).collect(Collectors.toList());

        messageBuilder.setActionRows(ActionRowUtil.fillButtons(categoryButtons));

        return messageBuilder.build();
    }

    private Message createCategoryMenu(Member initiator, String categoryId) {
        return createCategoryMenu(categoryId, initiator.getRoles());
    }

    private Message createCategoryMenu(String categoryId, List<Role> roleList) {
        MessageBuilder messageBuilder = new MessageBuilder("Hier kannst du dein Menü bearbeiten!\n")
                .append("Um einen Studiengang hinzuzufügen oder zu entfernen, klicke auf eine der Kategorien.\n\n")
                .append("__Aktuell ausgewählte Studiengänge:__\n");

        // Set<String> selectedCategories = new HashSet<>();
        Set<String> selectedCourses = new HashSet<>();

        for (Role role : roleList) {
            if (roleIdCourseIdMap.containsKey(role.getIdLong())) {
                String categoryCourseId = roleIdCourseIdMap.get(role.getIdLong());
                String courseName = getParent().getConfig().getProfile().getCourseCategories()
                        .get(categoryCourseId.split(CUSTOM_DELIMITER)[0]).getCourses()
                        .get(categoryCourseId.split(CUSTOM_DELIMITER)[1]).getName();

                messageBuilder.append("- ").append(courseName).append("\n");

                // selectedCategories.add(categoryCourseId.split(CUSTOM_DELIMITER)[0]);
                selectedCourses.add(categoryCourseId.split(CUSTOM_DELIMITER)[1]);
            }
        }

        messageBuilder.append("\nKlicke einen Knopf um dir die Rolle zuzuweisen.");

        List<Button> categoryButtons = getParent().getConfig().getProfile()
                .getCourseCategories()
                .get(categoryId)
                .getCourses()
                .entrySet()
                .stream()
                .map(entry -> {
                    if (selectedCourses.contains(entry.getKey())) {
                        return Button.success(
                                createButtonId(BTN_DATA_COURSE_PREFIX + CUSTOM_DELIMITER
                                        + categoryId + CUSTOM_DELIMITER + entry.getKey()),
                                entry.getValue().getUnicodeEmoji() + " " + entry.getValue().getName()
                        );
                    } else {
                        return Button.primary(
                                createButtonId(BTN_DATA_COURSE_PREFIX + CUSTOM_DELIMITER
                                        + categoryId + CUSTOM_DELIMITER + entry.getKey()),
                                entry.getValue().getUnicodeEmoji() + " " + entry.getValue().getName()
                        );
                    }
                }).collect(Collectors.toList());

        categoryButtons.add(Button.danger(createButtonId(BTN_DATA_RETURN_MAIN), "\uD83D\uDD19 Zurück"));

        messageBuilder.setActionRows(ActionRowUtil.fillButtons(categoryButtons));

        return messageBuilder.build();
    }

    private Message createExpectedDegreeMenu(Member initiator) {
        return createExpectedDegreeMenu(initiator.getRoles());
    }

    private Message createExpectedDegreeMenu(List<Role> roleList) {
        MessageBuilder messageBuilder = new MessageBuilder("Hier kannst du auswählen welchen Abschluss ")
                .append("du aktuell anstrebst. Du kannst maximal eine Rolle auswählen.");

        List<Button> buttonList = new ArrayList<>();

        for (ProfileSubconfig.NameEmojiRole degreeRole : getParent().getConfig().getProfile().getDegreeRoles()) {
            buildSingleSelectButtons(roleList, buttonList, degreeRole, BTN_DATA_DEGREE_PREFIX);
        }

        messageBuilder.setActionRows(ActionRowUtil.fillButtons(buttonList));
        return messageBuilder.build();
    }

    private Message createStartYearMenu(Member initiator) {
        return createStartYearMenu(initiator.getRoles());
    }

    private Message createStartYearMenu(List<Role> roleList) {
        MessageBuilder messageBuilder = new MessageBuilder("Hier kannst du auswählen wann du dein Studium ")
                .append("begonnen hast. Du kannst maximal eine Rolle auswählen.");

        List<Button> buttonList = new ArrayList<>();

        for (ProfileSubconfig.NameEmojiRole degreeRole : getParent().getConfig().getProfile().getYearRoles()) {
            buildSingleSelectButtons(roleList, buttonList, degreeRole, BTN_DATA_YEAR_PREFIX);
        }

        messageBuilder.setActionRows(ActionRowUtil.fillButtons(buttonList));
        return messageBuilder.build();
    }

    /**
     * Generated from IntelliJ "extract from duplicates" feature
     */
    private void buildSingleSelectButtons(List<Role> roleList, List<Button> buttonList,
                                          ProfileSubconfig.NameEmojiRole degreeRole, String dataPrefix) {
        if (roleList.stream().anyMatch(role -> role.getIdLong() == degreeRole.getDiscordRoleId())) {
            buttonList.add(Button.success(
                    createButtonId(dataPrefix + CUSTOM_DELIMITER + degreeRole.getDiscordRoleId()),
                    degreeRole.getUnicodeEmoji() + " " + degreeRole.getName()
            ));
        } else {
            buttonList.add(Button.primary(
                    createButtonId(dataPrefix + CUSTOM_DELIMITER + degreeRole.getDiscordRoleId()),
                    degreeRole.getUnicodeEmoji() + " " + degreeRole.getName()
            ));
        }
    }

}
