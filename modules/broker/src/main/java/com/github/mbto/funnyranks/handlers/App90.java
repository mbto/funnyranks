package com.github.mbto.funnyranks.handlers;

import com.github.mbto.funnyranks.common.dto.Message;
import com.github.mbto.funnyranks.common.dto.PortData;
import com.github.mbto.funnyranks.common.dto.session.Session;
import com.github.mbto.funnyranks.common.dto.session.Storage;
import com.github.mbto.funnyranks.common.model.funnyranks.tables.pojos.Port;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jooq.types.UInteger;
import org.jooq.types.UShort;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.mbto.funnyranks.common.Constants.MMDDYYYY_HHMMSS_PATTERN;
import static com.github.mbto.funnyranks.common.FlushEvent.NEW_GAME_MAP;
import static com.github.mbto.funnyranks.common.FlushEvent.SHUTDOWN_GAME_SERVER;
import static com.github.mbto.funnyranks.common.dto.session.StorageFetchMode.DONT_CREATE;
import static com.github.mbto.funnyranks.handlers.App90.Patterns.*;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * 90	Half-Life Dedicated Server
 */
@Slf4j
public class App90 extends MessageHandler {
    public static final UInteger appId = UInteger.valueOf(90);

    @Override
    public UInteger getAppId() {
        return appId;
    }

    @Override
    public boolean validate(UShort portValue, byte[] data) {
        // [-1, -1, -1, -1, 108, 111, 103, 32, 76, ...]
        if (data.length < 9 || data[4] != 'l' || data[5] != 'o' || data[6] != 'g' || data[7] != ' ' || data[8] != 'L') {
            if (log.isDebugEnabled())
                log.debug(portValue + " Invalid data: '" + new String(data, 0, data.length, UTF_8) + "', raw: " + Arrays.toString(data));
            return false;
        }
        return true;
    }

    @Override
    public String convert(UShort portValue, byte[] data) {
        return new String(data, 8, data.length - 8, UTF_8).trim();
    }

    @Override
    public void handle(Message<?> message) {
        if (message.getPayload() == null)
            return;
        Matcher msgMatcher = LOG.pattern.matcher(message.getPayload());
        if (!msgMatcher.find())
            return;
        PortData portData = (PortData) message.getPojo();
        Port port = portData.getPort();
        UShort portValue = port.getValue();

        LocalDateTime dateTime = LocalDateTime.parse(msgMatcher.group("date"), MMDDYYYY_HHMMSS_PATTERN);
        portData.setLastTouchDateTime(dateTime);

        // Extract "msg" from "L 01/08/2021 - 20:50:08: msg"
        String rawMsg = msgMatcher.group("msg");
        Matcher actionMatcher = TWO.pattern.matcher(rawMsg);

        if (actionMatcher.find()) {
            String eventName = actionMatcher.group(2);
            /* L 01/08/2021 - 13:15:02: "Name1<5><STEAM_ID_LAN><CT>" killed "Name2<6><STEAM_ID_LAN><T>" with "m4a1" */
            if (eventName.equals("killed")) {
                String sourceRaw = actionMatcher.group(1);
                String targetRaw = actionMatcher.group(3);

                Matcher sourceMatcher = PLAYER.pattern.matcher(sourceRaw);
                Matcher targetMatcher = PLAYER.pattern.matcher(targetRaw);

                if (sourceMatcher.find() && targetMatcher.find()) {
                    String killerAuth = sourceMatcher.group("auth");
                    String victimAuth = targetMatcher.group("auth");
                    if(isAuthHLTV(killerAuth) || isAuthHLTV(victimAuth)) {
                        if (log.isDebugEnabled()) {
                            log.debug(portValue + " Skip HLTV frag: " + sourceRaw + " or " + targetRaw);
                        }
                        return;
                    }
                    if (port.getIgnoreBots()) {
                        if (isAuthBOT(killerAuth) || isAuthBOT(victimAuth)) {
                            if (log.isDebugEnabled()) {
                                log.debug(portValue + " Skip BOT frag: " + sourceRaw + " or " + targetRaw);
                            }
                            return;
                        }
                    }
                    String killerName = sourceMatcher.group("name");
                    String victimName = targetMatcher.group("name");

                    if (port.getFfa()) {
                        countFrag(port, dateTime, killerName, killerAuth, victimName, victimAuth);
                    } else {
                        String killerTeam = sourceMatcher.group("team");
                        String victimTeam = targetMatcher.group("team");

                        if (StringUtils.isNotBlank(killerTeam)
                                && StringUtils.isNotBlank(victimTeam)
                                && !StringUtils.equalsIgnoreCase(killerTeam, victimTeam)
                        ) {
                            countFrag(port, dateTime, killerName, killerAuth, victimName, victimAuth);
                        }
                    }
                    return;
                }
                return;
            }
            return;
        }
        Matcher eventMatcher = THREE.pattern.matcher(rawMsg);
        if (eventMatcher.find()) {
            String eventName = eventMatcher.group(2);
            /* L 01/08/2021 - 13:15:00: "Name1<5><STEAM_ID_LAN><>" connected, address "12.12.12.12:27005" */
            if (eventName.equals("connected, address")) { // for players + some bots
                String sourceRaw = eventMatcher.group(1);
                Matcher sourceMatcher = PLAYER.pattern.matcher(sourceRaw);
                if (sourceMatcher.find()) {
                    String sourceAuth = sourceMatcher.group("auth");
                    if(isAuthHLTV(sourceAuth)) {
                        return;
                    }
                    if (port.getIgnoreBots()) {
                        // some bots generate event "connected, address"
                        if (isAuthBOT(sourceAuth)) {
                            return;
                        }
                    }
                    String sourceName = sourceMatcher.group("name");
                    String sourceIp = eventMatcher.group(3); // Possible values: "loopback:27005", "12.12.12.12:27005", "none"
                    Storage storage = allocateStorage(port, sourceName, sourceAuth, dateTime);
                    Session session = storage.getSession(true);

                    session.setIp(sourceIp);
                    if (port.getStartSessionOnAction()) {
                        return;
                    }
                    session.setStarted(dateTime); // activate session
                    return;
                }
                return;
            }
            /* L 01/08/2021 - 21:19:26: "Currv<29><BOT><CT>" committed suicide with "grenade" */
            if (eventName.equals("committed suicide with")) {
                String sourceRaw = eventMatcher.group(1);
                Matcher sourceMatcher = PLAYER.pattern.matcher(sourceRaw);
                if (sourceMatcher.find()) {
                    String sourceAuth = sourceMatcher.group("auth");
                    if(isAuthHLTV(sourceAuth)) {
                        if (log.isDebugEnabled()) {
                            log.debug(portValue + " Skip HLTV suicide: " + sourceRaw);
                        }
                        return;
                    }
                    if (port.getIgnoreBots()) {
                        if (isAuthBOT(sourceAuth)) {
                            if (log.isDebugEnabled()) {
                                log.debug(portValue + " Skip BOT suicide: " + sourceRaw);
                            }
                            return;
                        }
                    }
                    String sourceName = sourceMatcher.group("name");
                    Storage storage = allocateStorage(port, sourceName, sourceAuth, dateTime);
                    storage.getSession(dateTime).upDeaths();
                    return;
                }
                return;
            }
            /* L 01/08/2021 - 13:15:08: "Name5<5><STEAM_0:0:123456><CT>" changed name to "Name9" */
            if (eventName.equals("changed name to")) {
                String sourceRaw = eventMatcher.group(1);
                Matcher sourceMatcher = PLAYER.pattern.matcher(sourceRaw);
                if (sourceMatcher.find()) {
                    String sourceAuth = sourceMatcher.group("auth");
                    if(isAuthHLTV(sourceAuth)) {
                        if (log.isDebugEnabled()) {
                            log.debug(portValue + " Skip HLTV changed name: " + sourceRaw);
                        }
                        return;
                    }
                    if (port.getIgnoreBots()) {
                        if (isAuthBOT(sourceAuth)) {
                            if (log.isDebugEnabled()) {
                                log.debug(portValue + " Skip BOT changed name: " + sourceRaw);
                            }
                            return;
                        }
                    }
                    String sourceName = sourceMatcher.group("name");
                    String sourceNewName = eventMatcher.group(3);
                    allocateStorage(port, sourceName, sourceNewName, sourceAuth, dateTime); // activate session
                    return;
                }
                return;
            }
            return;
        }
        eventMatcher = FOUR.pattern.matcher(rawMsg);
        if (eventMatcher.find()) {
            String eventName = eventMatcher.group(2);
            /* L 01/08/2021 - 20:50:20: "timoxatw<3><BOT><>" entered the game */
            if (eventName.equals("entered the game")) { // for players + all bots
                String sourceRaw = eventMatcher.group(1);
                Matcher sourceMatcher = PLAYER.pattern.matcher(sourceRaw);
                if (sourceMatcher.find()) {
                    String sourceAuth = sourceMatcher.group("auth");
                    if(isAuthHLTV(sourceAuth)) {
                        return;
                    }
                    if (port.getIgnoreBots()) {
                        if (isAuthBOT(sourceAuth)) {
                            return;
                        }
                    }
                    String sourceName = sourceMatcher.group("name");
                    Storage storage = allocateStorage(port, sourceName, sourceAuth, dateTime);
                    if (port.getStartSessionOnAction()) {
                        return;
                    }
                    storage.getSession(dateTime); // activate session
                    return;
                }
                return;
            }
            /* L 01/08/2021 - 20:52:10: "timoxatw<3><BOT><TERRORIST>" disconnected */
            if (eventName.equals("disconnected")) {
                String sourceRaw = eventMatcher.group(1);
                Matcher sourceMatcher = PLAYER.pattern.matcher(sourceRaw);
                if (sourceMatcher.find()) {
                    String sourceName = sourceMatcher.group("name");
                    Map<String, Storage> storageByName = allocateStorageByNameContainer(portValue, DONT_CREATE);
                    if (storageByName != null) {
                        Storage storage = storageByName.get(sourceName);
                        if (storage != null) {
                            storage.onDisconnected(dateTime);
                        }
                    }
                    return;
                }
                return;
            }
            return;
        }
        eventMatcher = SIX.pattern.matcher(rawMsg);
        if (eventMatcher.find()) {
            String eventName = eventMatcher.group(1);
            /* L 01/08/2021 - 20:50:08: Started map "de_dust2" (CRC "1159425449") */
            if (eventName.equals("Started map")) {
                flushSessions(portData, dateTime, NEW_GAME_MAP);
                return;
            }
            return;
        }
        // Did not match patterns:
        /* L 01/08/2021 - 20:52:15: Server shutdown */
        if (rawMsg.equals("Server shutdown")) {
            flushSessions(portData, dateTime, SHUTDOWN_GAME_SERVER);
            return;
        }
    }

    /**
     * HL Log Standard Examples Specification Rev. 1.03
     * https://developer.valvesoftware.com/wiki/HL_Log_Standard_Examples#Example_1:_Perl:_Log_parsing_routines
     */
    public enum Patterns {
        // L 01/08/2021 - 13:15:00: "Name1<STEAM_ID_LAN><5><>" connected, address "12.12.12.12:27005"
        LOG("L (?<date>\\d{2}/\\d{2}/\\d{4} - \\d{2}:\\d{2}:\\d{2}): (?<msg>.*)"),

        //    # matches events 057,058,059,066
        //057. Kills
        //"Name<uid><wonid><team>" killed "Name<uid><wonid><team>" with "weapon"
        //058. Injuring
        //This event allows for recording of partial kills and teammate friendly-fire injuries.
        // The suggested damage property3 could be used to show how much health the victim lost.
        // If the injury results in a kill, a Kill message (057) should be logged instead/also.
        //"Name<uid><wonid><team>" attacked "Name<uid><wonid><team>" with "weapon" (damage "damage")
        //059. Player-Player Actions
        //This event allows for logging of a wide range of events where one player performs an action on another player.
        // For example, in TFC this event may cover medic healings and infections, sentry gun destruction,
        // spy uncovering, etc. More detail about the action can be given by appending more properties to the end of the event3.
        //"Name<uid><wonid><team>" triggered "action" against "Name<uid><wonid><team>"
        //066. Private Chat
        //"Name<uid><wonid><team>" tell "Name<uid><wonid><team>" message "message"
        //    $team = "";
        //    $player = $1; # parse out name, uid and team later
        //    $event1 = $2; # event type - "killed", "attacked", etc.
        //    $noun1 = $3; # victim name/objective code, etc.
        //    $event2 = $4; # "with", etc.
        //    $noun2 = $5; # weapon/victim name, etc.
        //    $properties = $6; # parse out keys and values later
        TWO("\"([^\"]+)\" ([^\"\\(]+) \"([^\"]+)\" ([^\"\\(]+) \"([^\"]+)\"(.*)"),

        //    # matches events 050,053,054,055,056,060,063a,063b,068,069
        //050. Connection
        //"Name<uid><wonid><>" connected, address "loopback:27005"
        //"Name<uid><wonid><>" connected, address "12.12.12.12:27005"
        //"Name<uid><wonid><>" connected, address "none"
        //053. Suicides
        //"Name<uid><wonid><team>" committed suicide with "weapon"
        //054. Team Selection
        //"Name<uid><wonid><team>" joined team "team"
        //055. Role Selection
        //This event covers classes in games like TFC, FLF and DOD.
        //"Name<uid><wonid><team>" changed role to "role"
        //056. Change Name
        //"Name<uid><wonid><team>" changed name to "Name"
        //060. Player Objectives/Actions
        //"Name<uid><wonid><team>" triggered "action"
        //063. Chat
        //"Name<uid><wonid><team>" say "message"
        //"Name<uid><wonid><team>" say_team "message"
        //068. Weapon Selection
        //Use this event to show what weapon a player currently has selected.
        //"Name<uid><wonid><team>" selected weapon "weapon"
        //069. Weapon Pickup
        //"Name<uid><wonid><team>" acquired weapon "weapon"
        //    $team = "";
        //    $player = $1;
        //    $event1 = $2;
        //    $noun1 = $3; # weapon/team code/objective code, etc.
        //    $event2 = "";
        //    $noun2 = "";
        //    $properties = $4;
        THREE("\"([^\"]+)\" ([^\"\\(]+) \"([^\"]+)\"(.*)"),

        //    # matches events 050b,051,052
        //050b. Validation
        //"Name<uid><wonid><>" STEAM USERID validated
        //051. Enter Game
        //"Name<uid><wonid><>" entered the game
        //052. Disconnection
        //"Name<uid><wonid><team>" disconnected
        //    $team = "";
        //    $player = $1;
        //    $event1 = $2;
        //    $noun1 = "";
        //    $event2 = "";
        //    $noun2 = "";
        //    $properties = $3;
        FOUR("\"([^\"]+)\" ([^\\(]+)(.*)"),

        //    # matches events 061,064
        //061. Team Objectives/Actions
        //Team "team" triggered "action"
        //064. Team Alliances
        //Team "team" formed alliance with team "team"
        //    $team = $1; # Team code
        //    $player = 0;
        //    $event1 = $2;
        //    $noun1 = $3;
        //    $event2 = "";
        //    $noun2 = "";
        //    $properties = $4;
        FIVE("Team \"([^\"]+)\" ([^\"\\(]+) \"([^\"]+)\"(.*)"),

        //    # matches events 062,003a,003b,005,006
        //062. World Objectives/Actions
        //This event allows logging of anything which does not happen in response to the actions of a player or team.
        // For example a gate opening at the start of a round.
        //World triggered "action"
        //003. Change Map
        //This event replaces the current "Spawning server" message.
        //Loading map "map"
        //This event replaces the current "Map CRC" message. The message should appear AFTER all PackFile messages,
        // to indicate when the game actually commences.
        //Started map "map" (CRC "crc")
        //005. Server Name
        //Server name is "hostname"
        //006. Server Say
        //Server say "message"
        //    $team = "";
        //    $player = 0;
        //    $event1 = $1;
        //    $noun1 = $2;
        //    $event2 = "";
        //    $noun2 = "";
        //    $properties = $3;
        SIX("([^\"\\(]+) \"([^\"]+)\"(.*)"),

        //"time-the-kill.freeeemaN.<15064><STEAM_ID_LAN><TERRORIST>"
        //"time-the-kill.freeeemaN.<-1><STEAM_ID_LAN><TERRORIST>"
        PLAYER("(?<name>.+)<(?<id>.*)><(?<auth>.*)><(?<team>.*)>");

        public final Pattern pattern;

        Patterns(String pattern) {
            this.pattern = Pattern.compile(pattern);
        }
    }
}