// #define ON_DEBUG

#pragma dynamic 32768

#include <amxmodx>
#include <amxmisc>
#include <fakemeta>
#include <sqlx>

#if !defined cm
	#define cm(%1) (sizeof(%1)-1)
#endif

#define SHOWHUD_TASKID 3500
#define SHOWHUD_DELAY 2.0

new pcvar_funnyranks_db_host,
	pcvar_funnyranks_db_username,
	pcvar_funnyranks_db_password,
	pcvar_funnyranks_db_schema,
	pcvar_funnyranks_merge_type,
	pcvar_funnyranks_language
new pcvar_mp_freeforall
new hudMsgId
new Handle:sqlHandler

public plugin_init() {
	register_plugin("Funnyranks", "1.1", "mbto", "github.com/mbto/funnyranks", "Show in HUD player ranks from MySQL")
	
	pcvar_mp_freeforall = get_cvar_pointer("mp_freeforall")

	pcvar_funnyranks_db_host = register_cvar("funnyranks_db_host", "127.0.0.1:3306", FCVAR_PROTECTED)
	pcvar_funnyranks_db_username = register_cvar("funnyranks_db_username", "funnyranks_stats_project1", FCVAR_PROTECTED)
	pcvar_funnyranks_db_password = register_cvar("funnyranks_db_password", "funnyranks_stats_project1", FCVAR_PROTECTED)
	pcvar_funnyranks_db_schema = register_cvar("funnyranks_db_schema", "funnyranks_stats_project1", FCVAR_PROTECTED)
	
	// nick/ip/steamid. This value should be the one you set in the project settings on the frontend
	pcvar_funnyranks_merge_type = register_cvar("funnyranks_merge_type", "nick")
	
	// supports only 'en' and 'ru'
	pcvar_funnyranks_language = register_cvar("funnyranks_language", "en")

	register_clcmd("say /top10", "showTop", ADMIN_ALL, "Shows top players")
	register_clcmd("say /top15", "showTop", ADMIN_ALL, "Shows top players")

	hudMsgId = CreateHudSyncObj()
}

public plugin_cfg() {
	new cfg_dir[128]
	get_configsdir(cfg_dir, cm(cfg_dir))
	server_cmd("exec ^"%s/funnyranks.cfg^"", cfg_dir)
	server_exec()

	new dbmsData[4][64]
	get_pcvar_string(pcvar_funnyranks_db_host, dbmsData[0], cm(dbmsData[]))
	get_pcvar_string(pcvar_funnyranks_db_username, dbmsData[1], cm(dbmsData[]))
	get_pcvar_string(pcvar_funnyranks_db_password, dbmsData[2], cm(dbmsData[]))
	get_pcvar_string(pcvar_funnyranks_db_schema, dbmsData[3], cm(dbmsData[]))
	SQL_SetAffinity("mysql")
	log_amx("Creating DbTuple to %s/%s Username:%s", dbmsData[0], dbmsData[3], dbmsData[1])
	sqlHandler = SQL_MakeDbTuple(dbmsData[0], dbmsData[1], dbmsData[2], dbmsData[3])
	if(sqlHandler == Empty_Handle)
		set_fail_state("Failed creation DbTuple on %s/%s Username:%s", dbmsData[0], dbmsData[3], dbmsData[1])
	else {
		SQL_SetCharset(sqlHandler, "utf8mb4")
		register_event("StatusValue", "showRankUnderCrosshair", "bef", "1=2", "2!0")
		register_event("StatusValue", "hideRankUnderCrosshair", "bef", "1=1", "2=0")
		set_task(SHOWHUD_DELAY, "taskShowRankUnderRadar", SHOWHUD_TASKID, .flags = "b")
	}
}

public plugin_end() {
	remove_task(SHOWHUD_TASKID);
	if(sqlHandler != Empty_Handle) {
		SQL_FreeHandle(sqlHandler);
		sqlHandler = Empty_Handle;
	}
}

enum _:PlayerData {
	bool:PD_has_data,
	PD_place,
	PD_identity[32],
	PD_kills,
	PD_deaths,
	PD_gaming_time[64],
	PD_stars[64],
	PD_level,
	PD_rank_name[64],
	PD_kaomoji[64],
	PD_session,
}
new playerData[33][PlayerData]

public clearPlayerData(id) {
	playerData[id][PD_has_data] = false
	playerData[id][PD_place] = 0
	playerData[id][PD_identity][0] = EOS
	playerData[id][PD_kills] = 0
	playerData[id][PD_deaths] = 0
	playerData[id][PD_gaming_time][0] = EOS
	playerData[id][PD_stars][0] = EOS
	playerData[id][PD_level] = 0
	playerData[id][PD_rank_name][0] = EOS
	playerData[id][PD_kaomoji][0] = EOS
	playerData[id][PD_session] = 0
}

public client_authorized(id, const authid[]) {
	#if defined ON_DEBUG
	log_amx("funnyranks.amxx client_authorized id=%d authid=%s", id, authid)
	#endif
	if(is_user_hltv(id)) {
		return;
	}
	clearPlayerData(id)
	new name[MAX_NAME_LENGTH]
	get_user_name(id, name, cm(name))
	fetchRank(id, name, authid)
}

public client_infochanged(id) {
	if(!is_user_connected(id)) { // I'm not sure for this case
		return
	}
	new oldname[MAX_NAME_LENGTH], newname[MAX_NAME_LENGTH]
	get_user_name(id, oldname, cm(oldname))
	get_user_info(id, "name", newname, cm(newname))
	if(!equal(oldname, newname)) {
		clearPlayerData(id)
		new authid[MAX_AUTHID_LENGTH];
		get_user_authid(id, authid, cm(authid))
		fetchRank(id, newname, authid)
	}
}

public client_remove(id, bool:drop, const message[]) {
	clearPlayerData(id)
}

public showTop(id, level, cid) {
	if(sqlHandler == Empty_Handle) {
		client_print(id, print_chat, "[Funnyranks]: Top players not available");
		return 0
	}
	new type[16]
	get_pcvar_string(pcvar_funnyranks_merge_type, type, cm(type));
	if(type[0] == EOS) {
		return 0
	}
	new language[6], sqlQuery[256]
	get_pcvar_string(pcvar_funnyranks_language, language, cm(language));
	strtolower(type)
	formatex(sqlQuery, cm(sqlQuery), "call Top('%s', '%s', 0, 15, 1)", type, language)
	new data[2]
	data[0] = id
	data[1] = playerData[id][PD_session] = random(0)
	SQL_ThreadQuery(sqlHandler, "onTopComing", sqlQuery, data, sizeof data)
	return 0
}

public onTopComing(queryState, Handle:handler, const err[], errid, data[], data_len, Float:queuetime) {
	new id = data[0]
	if(data[1] != playerData[id][PD_session]) {
		#if defined ON_DEBUG
		log_amx("funnyranks.amxx onTopComing invalid session");
		#endif
		return
	}
	new Array:topDataArray = ArrayCreate(PlayerData);
	if(queryState == TQUERY_SUCCESS) {
		if(!SQL_NumResults(handler)) {
			#if defined ON_DEBUG
			log_amx("funnyranks.amxx onTopComing no result");
			#endif
			ArrayDestroy(topDataArray)
			client_print(id, print_chat, "[Funnyranks]: Players statistics not exists");
			return;
		}
		#if defined ON_DEBUG
		log_amx("funnyranks.amxx onTopComing has result");
		#endif
		while(SQL_MoreResults(handler)) {
			new topData[PlayerData]
/*
place	id	identity	kills	deaths	time_secs	gaming_time	stars	level	rank_name	kaomoji
1	19257	KpyTblLLlKa	8168	12942	30263542	11мес 20дн 6ч 32м	★★★★★★	56	Непобедимый	（︶︿︶）
*/
			topData[PD_place] = SQL_ReadResult(handler, SQL_FieldNameToNum(handler, "place"))
			SQL_ReadResult(handler, SQL_FieldNameToNum(handler, "identity"), topData[PD_identity], cm(topData[PD_identity]))
			topData[PD_kills] = SQL_ReadResult(handler, SQL_FieldNameToNum(handler, "kills"))
			topData[PD_deaths] = SQL_ReadResult(handler, SQL_FieldNameToNum(handler, "deaths"))
			SQL_ReadResult(handler, SQL_FieldNameToNum(handler, "gaming_time"), topData[PD_gaming_time], cm(topData[PD_gaming_time]))
			SQL_ReadResult(handler, SQL_FieldNameToNum(handler, "stars"), topData[PD_stars], cm(topData[PD_stars]))
			topData[PD_level] = SQL_ReadResult(handler, SQL_FieldNameToNum(handler, "level"))
			SQL_ReadResult(handler, SQL_FieldNameToNum(handler, "rank_name"), topData[PD_rank_name], cm(topData[PD_rank_name]))
			SQL_ReadResult(handler, SQL_FieldNameToNum(handler, "kaomoji"), topData[PD_kaomoji], cm(topData[PD_kaomoji]))
			
			ArrayPushArray(topDataArray, topData)
			#if defined ON_DEBUG
			log_amx("funnyranks.amxx onTopComing place=%d identity=%s kills=%d deaths=%d gamingTime=%s stars=%s level=%d rankName=%s kaomoji=%s", 
				topData[PD_place],
				topData[PD_identity],
				topData[PD_kills],
				topData[PD_deaths],
				topData[PD_gaming_time],
				topData[PD_stars],
				topData[PD_level],
				topData[PD_rank_name], 
				topData[PD_kaomoji]
			)
			#endif
			SQL_NextRow(handler);
		}
	} else if(errid || queryState == TQUERY_CONNECT_FAILED || queryState == TQUERY_QUERY_FAILED) {
		logFailedQuery(queryState, handler, err, errid, queuetime)
		ArrayDestroy(topDataArray)
		client_print(id, print_chat, "[Funnyranks]: Players statistics not available, error #%d", errid);
		return
	}
	#if defined ON_DEBUG
	log_amx("funnyranks.amxx onTopComing queryState=%d", queryState)
	#endif
	new html[MAX_MOTD_LENGTH]
	new len = formatex(html, cm(html), "\
<html>\
<head>\
<meta http-equiv='content-type' content='text/html;charset=utf-8'/>\
<style>\
body{background:#000;color:#FFB000;font-family:Terminal;font-size:6px;margin:0;padding:0}\
table{border-collapse:collapse;width:100%}\
th,td{padding:0 1px;white-space:nowrap}\
</style>\
</head>\
<body>\
<table>\
<tr><th>#</th><th>Identity</th><th>Kills</th><th>Deaths</th><th>Time</th><th>Stars</th><th>Lvl</th><th>Rank</th><th></th></tr>\
")
	new size = ArraySize(topDataArray)
	for(new i;i<size;i++) {
		new topData[PlayerData]
		ArrayGetArray(topDataArray, i, topData)
		replace_string(topData[PD_identity], cm(topData[PD_identity]), "<", "&lt;")
		// replace_string(topData[PD_identity], cm(topData[PD_identity]), ">", "&gt;")

		new row[256]
		formatex(row, cm(row), "<tr><td>%d</td><td>%s</td><td>%d</td><td>%d</td><td>%s</td><td>%s</td><td>%d</td><td>%s</td><td>%s</td></tr>",
			topData[PD_place],
			topData[PD_identity],
			topData[PD_kills],
			topData[PD_deaths],
			topData[PD_gaming_time],
			topData[PD_stars],
			topData[PD_level],
			topData[PD_rank_name], 
			topData[PD_kaomoji]
		)
		if(strlen(html) + strlen(row) > cm(html)) {
			break
		}
		len += formatex(html[len], cm(html) - len, "%s", row)
	}
	ArrayDestroy(topDataArray)
	show_motd(id, html, "Funnyranks top players")
}

public fetchRank(id, const name[MAX_NAME_LENGTH], const authid[]) {
	if(sqlHandler == Empty_Handle) {
		#if defined ON_DEBUG
		log_amx("funnyranks.amxx fetchRank empty sqlHandler=%d", sqlHandler);
		#endif
		return;
	}
	new language[6]
	get_pcvar_string(pcvar_funnyranks_language, language, cm(language));
	new identity[64], type[16], sqlQuery[256]
	get_pcvar_string(pcvar_funnyranks_merge_type, type, cm(type));
	strtolower(type)
	if(equal(type, "nick")) {
		copy(identity, cm(identity), name);
		replace_all(identity, cm(identity), "'", "''")
		formatex(sqlQuery, cm(sqlQuery), "call PlayerByName('%s', '%s', 0)", identity, language)
	} else if(equal(type, "ip")) {
		get_user_ip(id, identity, cm(identity), 1);
		formatex(sqlQuery, cm(sqlQuery), "call PlayerByIp('%s', '%s', 0)", identity, language)
	} else if(equal(type, "steamid")) {
		formatex(sqlQuery, cm(sqlQuery), "call PlayerBySteamId2('%s', '%s', 0)", authid, language)
	} else {
		return
	}
	#if defined ON_DEBUG
	log_amx("funnyranks.amxx fetchRank sqlQuery=%s", sqlQuery);
	#endif
	new data[2]
	data[0] = id
	data[1] = playerData[id][PD_session] = random(0)
	SQL_ThreadQuery(sqlHandler, "onStatsComing", sqlQuery, data, sizeof data)
}

public onStatsComing(queryState, Handle:handler, const err[], errid, data[], data_len, Float:queuetime) {
	new id = data[0]
	if(data[1] != playerData[id][PD_session]) {
		#if defined ON_DEBUG
		log_amx("funnyranks.amxx onStatsComing invalid session");
		#endif
		return
	}
	new bool:fetched
	if(queryState == TQUERY_SUCCESS) {
		new results = SQL_NumResults(handler)
		if(results <= 0) {
			#if defined ON_DEBUG
			log_amx("funnyranks.amxx onStatsComing results=%d", results);
			#endif
			goto finish;
		}
		#if defined ON_DEBUG
		log_amx("funnyranks.amxx onStatsComing results=%d", results);
		#endif
		SQL_MoreResults(handler)
		fetched = true;
/*
place	id	name	kills	deaths	gaming_time	stars	level	rank_name	kaomoji	lastseen_datetime	last_server_name
3	61170	TSK_3655	10895	29008	7мес 20дн 15ч 2м	彡彡彡彡彡彡	56	Непобедимый	（︶︿︶）	2021-11-02 03:25:56	Ito Sakura
*/
		playerData[id][PD_place] = SQL_ReadResult(handler, SQL_FieldNameToNum(handler, "place"))
		new column_id, type[16]
		get_pcvar_string(pcvar_funnyranks_merge_type, type, cm(type));
		strtolower(type)
		if(equal(type, "nick")) {
			column_id = SQL_FieldNameToNum(handler, "name")
		} else if(equal(type, "ip")) {
			column_id = SQL_FieldNameToNum(handler, "ip")
		} else if(equal(type, "steamid")) {
			column_id = SQL_FieldNameToNum(handler, "steamid2")
		}
		SQL_ReadResult(handler, column_id, playerData[id][PD_identity], cm(playerData[][PD_identity]))
		playerData[id][PD_kills] = SQL_ReadResult(handler, SQL_FieldNameToNum(handler, "kills"))
		playerData[id][PD_deaths] = SQL_ReadResult(handler, SQL_FieldNameToNum(handler, "deaths"))
		SQL_ReadResult(handler, SQL_FieldNameToNum(handler, "gaming_time"), playerData[id][PD_gaming_time], cm(playerData[][PD_gaming_time]))
		SQL_ReadResult(handler, SQL_FieldNameToNum(handler, "stars"), playerData[id][PD_stars], cm(playerData[][PD_stars]))
		playerData[id][PD_level] = SQL_ReadResult(handler, SQL_FieldNameToNum(handler, "level"))
		SQL_ReadResult(handler, SQL_FieldNameToNum(handler, "rank_name"), playerData[id][PD_rank_name], cm(playerData[][PD_rank_name]))
		SQL_ReadResult(handler, SQL_FieldNameToNum(handler, "kaomoji"), playerData[id][PD_kaomoji], cm(playerData[][PD_kaomoji]))

		#if defined ON_DEBUG
		log_amx("funnyranks.amxx onStatsComing place=%d identity=%s kills=%d deaths=%d gamingTime=%s stars=%s level=%d rankName=%s kaomoji=%s", 
			playerData[id][PD_place],
			playerData[id][PD_identity],
			playerData[id][PD_kills],
			playerData[id][PD_deaths],
			playerData[id][PD_gaming_time],
			playerData[id][PD_stars],
			playerData[id][PD_level],
			playerData[id][PD_rank_name], 
			playerData[id][PD_kaomoji]
		);
		#endif
		playerData[id][PD_has_data] = true;
	} else if(errid || queryState == TQUERY_CONNECT_FAILED || queryState == TQUERY_QUERY_FAILED) {
		logFailedQuery(queryState, handler, err, errid, queuetime)
	}
	finish:
	#if defined ON_DEBUG
	log_amx("funnyranks.amxx onStatsComing queryState=%d fetched=%d", queryState, fetched)
	#endif
	if(!fetched) {
		clearPlayerData(id)
	}
}

public showRankUnderCrosshair(id) {
	new targetId = read_data(2)
	if(targetId < 1 || targetId > MaxClients || !playerData[targetId][PD_has_data]) {
		#if defined ON_DEBUG
		log_amx("funnyranks.amxx showRankUnderCrosshair invalid targetId=%d or no data", targetId)
		#endif
		hideRankUnderCrosshair(id)
		return
	}
	new tmp[1]
	if(get_pcvar_num(pcvar_mp_freeforall) != 1 && get_user_team(id, tmp, cm(tmp)) != get_user_team(targetId, tmp, cm(tmp))) {
		#if defined ON_DEBUG
		log_amx("funnyranks.amxx showRankUnderCrosshair different teams")
		#endif
		hideRankUnderCrosshair(id)
		return
	}
	#if defined ON_DEBUG
	log_amx("funnyranks.amxx showRankUnderCrosshair showing...")
	#endif
	set_hudmessage(227, 96, 8, -1.0, 0.60, 0, 0.0, 6.0, 0.0, 0.1, -1)
	ShowSyncHudMsg(id, hudMsgId, "%s^nLevel %d %s^n%s^n%s", 
		playerData[targetId][PD_identity], 
		playerData[targetId][PD_level], 
		playerData[targetId][PD_rank_name], 
		playerData[targetId][PD_stars], 
		playerData[targetId][PD_kaomoji]
	)
}

public hideRankUnderCrosshair(id) {
	#if defined ON_DEBUG
	log_amx("funnyranks.amxx hideRankUnderCrosshair clearing...")
	#endif
	ClearSyncHud(id, hudMsgId)
}

public taskShowRankUnderRadar() {
	new players[MAX_PLAYERS], num, message[512]
	new id, targetId
	get_players(players, num, "ch")
	for(new i;i<num;i++) {
		id = players[i]
		if(!is_user_alive(id)) {
			targetId = pev(id, pev_iuser2)
			if(targetId < 1 || targetId > MaxClients) {
				targetId = id
			}
		} else {
			targetId = id
		}
		// #if defined ON_DEBUG
		// server_print("message=%s playerData[targetId][PD_has_data]=%d", message, playerData[targetId][PD_has_data])
		// #endif
		if(!playerData[targetId][PD_has_data]) {
			continue;
		}
		formatex(message, cm(message), "%s^nLevel %d %s^n%s^n%s", 
			playerData[targetId][PD_identity], 
			playerData[targetId][PD_level], 
			playerData[targetId][PD_rank_name], 
			playerData[targetId][PD_stars], 
			playerData[targetId][PD_kaomoji]
		)
		set_dhudmessage(255, 255, 255, 0.01, 0.19, 0, 0.0, SHOWHUD_DELAY, 0.0, 0.1)
		show_dhudmessage(id, "%s", message)
	}
	#if defined ON_DEBUG
	set_dhudmessage(255, 255, 255, 0.23, 0.17, 0, 0.0, SHOWHUD_DELAY, 0.0, 0.1)
	show_dhudmessage(0, "¯\_(ツ)_/¯^nˁ°ᴥ°ˀ^n(°‿°)^nᕙ(°ʖ°)ᕗ^nᕦ(°_°)ᕤ^n")
	set_dhudmessage(255, 255, 255, 0.47, 0.17, 0, 0.0, SHOWHUD_DELAY, 0.0, 0.1)
	show_dhudmessage(0, "龴ↀ‿ↀ龴^n(ಥ﹏ಥ)^n(ง°ل͜°)ง^n(づ° ³°)づ^n(ﾉ°ヮ°)ﾉ*:･ﾟ✧^n")
	set_dhudmessage(255, 255, 255, 0.70, 0.17, 0, 0.0, SHOWHUD_DELAY, 0.0, 0.1)
	show_dhudmessage(0, "( ° ͜ʖ °)^nt(ಠ益ಠt)^n(ノಠ益ಠ)ノ彡^nლ(ಠ益ಠლ)^n（︶︿︶）^n")
	#endif
}

stock bool:stopLogOnConnectionFailed
stock logFailedQuery(queryState, Handle:dhandler, const err[], errid, Float:queuetime) {
	if(queryState == TQUERY_CONNECT_FAILED && stopLogOnConnectionFailed) {
		return
	}
	new errmsg[768], len
	len = copy(errmsg, cm(errmsg), "^nDbTuple warning: ")
	if(queryState == TQUERY_CONNECT_FAILED) {
		len += copy(errmsg[len], cm(errmsg) -len, "Connection failed^n")
		stopLogOnConnectionFailed = true
	} else if(queryState == TQUERY_QUERY_FAILED) {
		len += copy(errmsg[len], cm(errmsg) -len, "Query failed^n")
	} else {
		len += copy(errmsg[len], cm(errmsg) -len, "Unknown^n")
	}
	len += formatex(errmsg[len], cm(errmsg) -len, "Qtime: %.3f^n", queuetime)
	len += formatex(errmsg[len], cm(errmsg) -len, "Qstate: %d^n", queryState)
	len += formatex(errmsg[len], cm(errmsg) -len, "Qhandler: %d^n", dhandler)
	len += formatex(errmsg[len], cm(errmsg) -len, "EID: %d^n", errid)
	len += formatex(errmsg[len], cm(errmsg) -len, "EMSG: ^"%s^"", err)
	new query[2048] // 4096 max for log_to_file
	SQL_GetQueryString(dhandler, query, cm(query))
	log_error(AMX_ERR_GENERAL, "%s^nQUERY: %s", errmsg, query)
}