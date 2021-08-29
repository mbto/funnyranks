#include <amxmodx>
#include <amxmisc>
#include <fakemeta>
#include <sqlx>

// #define DEBUG
#define SHOWHUD_TASKID 3500
#define SHOWHUD_DELAY 2.0

new dbHostCvar, dbUsernameCvar, dbPasswordCvar, dbSchemaCvar, ffaCvar, mergeTypeCvar, languageCvar
new hudMsgId
new Handle:sqlHandler

public plugin_init() {
	register_plugin("Funnyranks", "1.0", "mbto", "github.com/mbto/funnyranks", "Show in HUD player ranks from MySQL")
	
	dbHostCvar = register_cvar("funnyranks_db_host", "127.0.0.1:3306", FCVAR_PROTECTED)
	dbUsernameCvar = register_cvar("funnyranks_db_username", "funnyranks_stats_project1", FCVAR_PROTECTED)
	dbPasswordCvar = register_cvar("funnyranks_db_password", "funnyranks_stats_project1", FCVAR_PROTECTED)
	dbSchemaCvar = register_cvar("funnyranks_db_schema", "funnyranks_stats_project1", FCVAR_PROTECTED)
	ffaCvar = register_cvar("funnyranks_ffa", "0") // 0 - no ffa, not shows for enemy, 1 - ffa (free-for-all mode, example: CS-DeathMath), shows on all players
	mergeTypeCvar = register_cvar("funnyranks_merge_type", "1") // 1 - nick, 2 - IP, 3 - SteamID
	languageCvar = register_cvar("funnyranks_language", "en") // supports only 'en' and 'ru'

	register_clcmd("say /top10", "showTop", ADMIN_ALL, "Shows top players")
	register_clcmd("say /top15", "showTop", ADMIN_ALL, "Shows top players")

	hudMsgId = CreateHudSyncObj()
}

public plugin_cfg() {
	new dbmsData[4][64]
	get_pcvar_string(dbHostCvar, dbmsData[0], 63)
	get_pcvar_string(dbUsernameCvar, dbmsData[1], 63)
	get_pcvar_string(dbPasswordCvar, dbmsData[2], 63)
	get_pcvar_string(dbSchemaCvar, dbmsData[3], 63)
	SQL_SetAffinity("mysql")
	log_amx("Creating DbTuple to %s/%s Username:%s", dbmsData[0], dbmsData[3], dbmsData[1])
	sqlHandler = SQL_MakeDbTuple(dbmsData[0], dbmsData[1], dbmsData[2], dbmsData[3])
	if(sqlHandler == Empty_Handle)
		set_fail_state("Failed creation DbTuple on %s/%s Username:%s", dbmsData[0], dbmsData[3], dbmsData[1])
	else {
		SQL_SetCharset(sqlHandler, "utf8mb4")
		register_event("StatusValue", "showRankUnderCrosshair", "bef", "1=2", "2!0")
		register_event("StatusValue", "hideRankUnderCrosshair", "bef", "1=1", "2=0")
		set_task(SHOWHUD_DELAY, "showRankUnderRadar", SHOWHUD_TASKID, .flags="b")
	}
}

public plugin_end() {
	remove_task(SHOWHUD_TASKID);
	if(sqlHandler != Empty_Handle) {
		SQL_FreeHandle(sqlHandler);
		sqlHandler = Empty_Handle;
	}
}

enum _:PlayerDataEnum {
	session,
	bool:hasData,
	playerName[32],
	kills,
	deaths,
	gamingTime[64],
	rankName[61 * 4], // utf8mb4
	stars[7 * 4], // utf8mb4
	kaomoji[61 * 4] // utf8mb4
}
new playerData[33][PlayerDataEnum]

public clearPlayerData(id) {
	playerData[id][session] = 0;
	playerData[id][hasData] = false;
	playerData[id][playerName][0] = EOS;
	playerData[id][kills] = 0;
	playerData[id][deaths] = 0;
	playerData[id][gamingTime][0] = EOS;
	playerData[id][rankName][0] = EOS;
	playerData[id][stars][0] = EOS;
	playerData[id][kaomoji][0] = EOS;
}

public showTop(id, level, cid) {
	if(sqlHandler == Empty_Handle) {
		client_print(id, print_chat, "[Funnyranks]: Top players not available");
		return PLUGIN_CONTINUE;
	}

	new language[6], sqlQuery[256]
	get_pcvar_string(languageCvar, language, charsmax(language));
	formatex(sqlQuery, charsmax(sqlQuery), "call Top('10', '%s')", language)
	new data[2]
	data[0] = id
	data[1] = playerData[id][session] = random(0)
	SQL_ThreadQuery(sqlHandler, "onTopComing", sqlQuery, data, sizeof data)
	return PLUGIN_CONTINUE
}

public onTopComing(queryState, Handle:handler, const err[], errid, data[], data_len, Float:queuetime) {
	new id = data[0]
	if(data[1] != playerData[id][session]) {
		#if defined DEBUG
		log_amx("onTopComing invalid session");
		#endif
		SQL_FreeHandle(handler)
		return
	}
	new Array:topDataArray = ArrayCreate(PlayerDataEnum);
	if(queryState == TQUERY_SUCCESS) {
		if(!SQL_NumResults(handler)) {
			#if defined DEBUG
			log_amx("onTopComing no result");
			#endif
			SQL_FreeHandle(handler)
			client_print(id, print_chat, "[Funnyranks]: Players statistics not exists");
			return;
		}
		#if defined DEBUG
		log_amx("onTopComing has result");
		#endif
		while(SQL_MoreResults(handler)) {
			new topData[PlayerDataEnum]
// name					kills	deaths	gaming_time			rank_name	stars
// 3ey1 qsxh jv4wmzm	9999	1010	8мес 13дн 14ч 3м	Сенсей		★★★★☆☆
			SQL_ReadResult(handler, SQL_FieldNameToNum(handler, "name"), topData[playerName], charsmax(topData[playerName]))
			topData[kills] = SQL_ReadResult(handler, SQL_FieldNameToNum(handler, "kills"))
			topData[deaths] = SQL_ReadResult(handler, SQL_FieldNameToNum(handler, "deaths"))
			SQL_ReadResult(handler, SQL_FieldNameToNum(handler, "gaming_time"), topData[gamingTime], charsmax(topData[gamingTime]))
			SQL_ReadResult(handler, SQL_FieldNameToNum(handler, "rank_name"), topData[rankName], charsmax(topData[rankName]))
			SQL_ReadResult(handler, SQL_FieldNameToNum(handler, "stars"), topData[stars], charsmax(topData[stars]))
			// SQL_ReadResult(handler, SQL_FieldNameToNum(handler, "kaomoji"), topData[kaomoji], charsmax(topData[kaomoji]))
			ArrayPushArray(topDataArray, topData)
			#if defined DEBUG
			log_amx("onTopComing playerName %s kills %d deaths %d gamingTime %s rankName %s stars %s kaomoji %s", 
				topData[playerName],
				topData[kills],
				topData[deaths],
				topData[gamingTime],
				topData[rankName], 
				topData[stars], 
				topData[kaomoji]);
			#endif
			SQL_NextRow(handler);
		}
	} else if(errid || queryState == TQUERY_CONNECT_FAILED || queryState == TQUERY_QUERY_FAILED) {
		logFailedQuery(queryState, handler, err, errid, queuetime)
	}
	#if defined DEBUG
	log_amx("onTopComing queryState %d", queryState)
	#endif
	SQL_FreeHandle(handler)
	new html[2048]
	new len = formatex(html, charsmax(html), "<html><head><meta http-equiv='content-type' content='text/html;charset=utf-8'/>")
	len += formatex(html[len], charsmax(html) - len, "<body bgcolor=#000000 style=^"color:#FFB000;font-family:courier new^">")
	len += formatex(html[len], charsmax(html) - len, "<table><tr><th>#</th><th>Nick</th><th>Kills</th><th>Deaths</th><th>Rank</th><th>Skill</th><th>Gaming time</th></tr>")
	for(new i=0,size=ArraySize(topDataArray);i<size && charsmax(html) - len > 0;i++) {
		new topData[PlayerDataEnum]
		ArrayGetArray(topDataArray, i, topData)
		replace_string(topData[playerName], charsmax(topData[playerName]), "<", "&lt;")
		// replace_string(t_sName, charsmax(t_sName), ">", "&gt;")
		len += formatex(html[len], charsmax(html) - len, "<tr><td>%d</td><td>%s</td><td>%d</td><td>%d</td><td>%s</td><td>%s</td><td>%s</td></tr>", 
			i + 1, 
			topData[playerName], 
			topData[kills], 
			topData[deaths], 
			topData[rankName],
			topData[stars], 
			topData[gamingTime])
	}
	ArrayDestroy(topDataArray)
	show_motd(id, html, "Funnyranks top players")
}

public client_authorized(id, const authid[]) {
	#if defined DEBUG
	log_amx("client_authorized %d %s", id, authid)
	#endif
	if(is_user_hltv(id))
		return;
	clearPlayerData(id)
	new name[32]
	get_user_name(id, name, charsmax(name))
	fetchRank(id, name, authid)
}

public client_infochanged(id) {
	if(!is_user_connected(id)) // I'm not sure for this case
		return
	new oldname[32], newname[32]
	get_user_name(id, oldname, charsmax(oldname))
	get_user_info(id, "name", newname, charsmax(newname))
	if(!equal(oldname, newname)) {
		clearPlayerData(id)
		new authid[21];
		get_user_authid(id, authid, charsmax(authid))
		fetchRank(id, newname, authid)
	}
}

public client_disconnected(id) {
	clearPlayerData(id)
}

public fetchRank(id, const name[], const authid[]) {
	if(sqlHandler == Empty_Handle) {
		#if defined DEBUG
		log_amx("fetchRank empty sqlHandler %d", sqlHandler);
		#endif
		return;
	}
	copy(playerData[id][playerName], charsmax(playerData[][playerName]), name);
	new language[6]
	get_pcvar_string(languageCvar, language, charsmax(language));
	new identity[64], sqlQuery[256]
	switch(get_pcvar_num(mergeTypeCvar)) {
		case 1: {
			copy(identity, charsmax(identity), name);
			replace_all(identity, charsmax(identity), "'", "''")
			formatex(sqlQuery, charsmax(sqlQuery), "call PlayerByName('%s', (select count(*) from `rank`), '%s')", identity, language)
		}
		case 2: {
			get_user_ip(id, identity, charsmax(identity), 1);
			formatex(sqlQuery, charsmax(sqlQuery), "call PlayerByIp(inet_aton('%s'), (select count(*) from `rank`), '%s')", identity, language)
		}
		case 3: {
			new pos = strfind(authid, "STEAM_0:")
			if(pos == -1) {
				#if defined DEBUG
				log_amx("fetchRank Steam ID %s without 'STEAM_0:' not supported", authid);
				#endif
				return
			}
			formatex(sqlQuery, charsmax(sqlQuery), "call PlayerBySteamId2('%s', (select count(*) from `rank`), '%s')", authid[8], language)
		}
		default: {
			return;
		}
	}
	#if defined DEBUG
	log_amx("fetchRank sqlQuery=%s", sqlQuery);
	#endif
	new data[2]
	data[0] = id
	data[1] = playerData[id][session] = random(0)
	SQL_ThreadQuery(sqlHandler, "onStatsComing", sqlQuery, data, sizeof data)
}

public onStatsComing(queryState, Handle:handler, const err[], errid, data[], data_len, Float:queuetime) {
	new id = data[0]
	if(data[1] != playerData[id][session]) {
		#if defined DEBUG
		log_amx("onStatsComing invalid session");
		#endif
		SQL_FreeHandle(handler)
		return
	}
	new bool:fetched
	if(queryState == TQUERY_SUCCESS) {
		if(!SQL_NumResults(handler)) {
			#if defined DEBUG
			log_amx("onStatsComing no result");
			#endif
			goto finish;
		}
		#if defined DEBUG
		log_amx("onStatsComing has result");
		#endif
		SQL_MoreResults(handler)
		fetched = true;
// id	kills	deaths	time_secs	rank_id	lastseen_datetime		last_server_name	gaming_time	rank_name	stars		kaomoji
// 1	0		0		2040		1		2021-01-08 14:05:00		Public Server		34м			Сынок		彡ノノノノノ ¯\_(ツ)_/¯
		SQL_ReadResult(handler, SQL_FieldNameToNum(handler, "gaming_time"), playerData[id][gamingTime], charsmax(playerData[][gamingTime]))
		SQL_ReadResult(handler, SQL_FieldNameToNum(handler, "rank_name"), playerData[id][rankName], charsmax(playerData[][rankName]))
		SQL_ReadResult(handler, SQL_FieldNameToNum(handler, "stars"), playerData[id][stars], charsmax(playerData[][stars]))
		SQL_ReadResult(handler, SQL_FieldNameToNum(handler, "kaomoji"), playerData[id][kaomoji], charsmax(playerData[][kaomoji]))
		#if defined DEBUG
		log_amx("onStatsComing gamingTime %s rankName %s stars %s kaomoji %s", 
			playerData[id][gamingTime],
			playerData[id][rankName], 
			playerData[id][stars], 
			playerData[id][kaomoji]);
		#endif
		playerData[id][hasData] = true;
	} else if(errid || queryState == TQUERY_CONNECT_FAILED || queryState == TQUERY_QUERY_FAILED) {
		logFailedQuery(queryState, handler, err, errid, queuetime)
	}
	finish:
	#if defined DEBUG
	log_amx("onStatsComing queryState %d fetched %d", queryState, fetched)
	#endif
	SQL_FreeHandle(handler)
	if(!fetched) {
		clearPlayerData(id)
	}
}

public showRankUnderCrosshair(id) {
	new targetId = read_data(2)
	if(targetId < 1 || targetId > 32 || !playerData[targetId][hasData]) {
		#if defined DEBUG
		log_amx("showRankUnderCrosshair invalid targetId %d or no data", targetId)
		#endif
		hideRankUnderCrosshair(id)
		return
	}
	new tmp[1]
	if(get_pcvar_num(ffaCvar) != 1 && get_user_team(id, tmp, charsmax(tmp)) != get_user_team(targetId, tmp, charsmax(tmp))) {
		#if defined DEBUG
		log_amx("showRankUnderCrosshair different teams")
		#endif
		hideRankUnderCrosshair(id)
		return
	}
	#if defined DEBUG
	log_amx("showRankUnderCrosshair showing...")
	#endif
	set_hudmessage(227, 96, 8, -1.0, 0.60, 0, 0.0, 6.0, 0.0, 0.1, -1)
	ShowSyncHudMsg(id, hudMsgId, "%s^n%s^nSkill: %s^n%s", 
		playerData[targetId][playerName], 
		playerData[targetId][rankName], 
		playerData[targetId][stars], 
		playerData[targetId][kaomoji])
}

public hideRankUnderCrosshair(id) {
	#if defined DEBUG
	log_amx("hideRankUnderCrosshair clearing...")
	#endif
	ClearSyncHud(id, hudMsgId)
}

public showRankUnderRadar() {
	new players[32], num, message[128]
	new id, targetId
	get_players(players, num, "ch")
	for(new i=0;i<num;i++) {
		id = players[i]
		if(!is_user_alive(id)) {
			targetId = pev(id, pev_iuser2)
			if(targetId < 1 || targetId > 32)
				targetId = id
		} else {
			targetId = id
		}
		if(!playerData[targetId][hasData])
			continue;
		format(message, charsmax(message), "%s^n%s^nSkill: %s^n%s", 
			playerData[targetId][playerName], 
			playerData[targetId][rankName], 
			playerData[targetId][stars], 
			playerData[targetId][kaomoji])
		set_dhudmessage(255, 255, 255, 0.01, 0.19, 0, 0.0, SHOWHUD_DELAY, 0.0, 0.1)
		show_dhudmessage(id, message)
	}
	#if defined DEBUG
	set_dhudmessage(255, 255, 255, 0.23, 0.17, 0, 0.0, SHOWHUD_DELAY, 0.0, 0.1)
	show_dhudmessage(0, "¯\_(ツ)_/¯^nˁ°ᴥ°ˀ^n(°‿°)^nᕙ(°ʖ°)ᕗ^nᕦ(°_°)ᕤ^n")
	set_dhudmessage(255, 255, 255, 0.47, 0.17, 0, 0.0, SHOWHUD_DELAY, 0.0, 0.1)
	show_dhudmessage(0, "龴ↀ‿ↀ龴^n(ಥ﹏ಥ)^n(ง°ل͜°)ง^n(づ° ³°)づ^n(ﾉ°ヮ°)ﾉ*:･ﾟ✧^n")
	set_dhudmessage(255, 255, 255, 0.70, 0.17, 0, 0.0, SHOWHUD_DELAY, 0.0, 0.1)
	show_dhudmessage(0, "( ° ͜ʖ °)^nt(ಠ益ಠt)^n(ノಠ益ಠ)ノ彡^nლ(ಠ益ಠლ)^n（︶︿︶）^n")
	#endif
	return PLUGIN_CONTINUE
}

stock bool:stopLogOnConnectionFailed
stock logFailedQuery(queryState, Handle:dhandler, const err[], errid, Float:queuetime) {
	if(queryState == TQUERY_CONNECT_FAILED && stopLogOnConnectionFailed)
		return
	new errmsg[256], len
	len = copy(errmsg, charsmax(errmsg), "^nDbTuple warning: ")
	if(queryState == TQUERY_CONNECT_FAILED) {
		len += copy(errmsg[len], charsmax(errmsg) -len, "Connection failed^n")
		stopLogOnConnectionFailed = true
	}
	else if(queryState == TQUERY_QUERY_FAILED)
		len += copy(errmsg[len], charsmax(errmsg) -len, "Query failed^n")
	else
		len += copy(errmsg[len], charsmax(errmsg) -len, "Unknown^n")
	
	len += formatex(errmsg[len], charsmax(errmsg) -len, "Qtime: %.3f^n", queuetime)
	len += formatex(errmsg[len], charsmax(errmsg) -len, "Qstate: %d^n", queryState)
	len += formatex(errmsg[len], charsmax(errmsg) -len, "Qhandler: %d^n", dhandler)
	len += formatex(errmsg[len], charsmax(errmsg) -len, "EID: %d^n", errid)
	len += formatex(errmsg[len], charsmax(errmsg) -len, "EMSG: ^"%s^"^n", err)
	log_error(AMX_ERR_GENERAL, "%s", errmsg)
}