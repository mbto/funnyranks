-- MySQL dump 10.13  Distrib 8.0.21, for Win64 (x86_64)
--
-- Host: localhost    Database: funnyranks
-- ------------------------------------------------------
-- Server version	8.0.21

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `broker`
--

DROP TABLE IF EXISTS `broker`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `broker` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'broker name from application.properties',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  `reg_datetime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `broker_name_UNIQUE` (`name`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `driver_property`
--

DROP TABLE IF EXISTS `driver_property`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `driver_property` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `project_id` int unsigned NOT NULL,
  `key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `value` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  KEY `driver_property_project_id_idx` (`project_id`),
  CONSTRAINT `driver_property_project_id_fk` FOREIGN KEY (`project_id`) REFERENCES `project` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Additional JDBC driver connection properties https://dev.mysql.com/doc/connector-j/8.0/en/connector-j-reference-configuration-properties.html';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `game`
--

DROP TABLE IF EXISTS `game`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `game` (
  `app_id` int unsigned NOT NULL COMMENT 'Steam application id for converting/validating UDP datagrams from https://developer.valvesoftware.com/wiki/Steam_Application_IDs#Server_Files',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `listener_port` smallint unsigned NOT NULL COMMENT 'UDP port for consuming game server logs',
  PRIMARY KEY (`app_id`),
  UNIQUE KEY `game_listener_port_UNIQUE` (`listener_port`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `game`
--

LOCK TABLES `game` WRITE;
/*!40000 ALTER TABLE `game` DISABLE KEYS */;
set autocommit=0;
INSERT INTO `game` VALUES (90,'Half-Life Dedicated Server',27499),(310,'Source 2007 Dedicated Server',27498),(510,'Left 4 Dead Dedicated Server',27497),(740,'Counter-Strike: Global Offensive Dedicated Server',27496),(2145,'Dark Messiah Might and Magic Dedicated Server',27495),(2403,'The Ship Dedicated Server',27494),(4240,'Race Dedicated Server',27493),(17505,'Zombie Panic! Source Dedicated Server',27492),(17515,'Age of Chivalry Dedicated Server',27491),(17525,'Synergy Dedicated Server',27490),(17535,'D.I.P.R.I.P. Dedicated Server',27489),(17705,'Insurgency Mod Dedicated Server',27488),(111710,'Nuclear Dawn - Dedicated Server',27487),(222860,'Left 4 Dead 2 Dedicated Server',27486),(232250,'Team Fortress 2 Dedicated Server',27485),(232290,'Day of Defeat: Source Dedicated Server',27484),(232330,'Counter-Strike: Source Dedicated Server',27483),(232370,'Half-Life 2: Deathmatch Dedicated Server',27482),(237410,'Insurgency Dedicated Server',27481),(244310,'Source SDK Base 2013 Dedicated Server',27480),(294420,'7 Days to Die Dedicated Server',27479),(317670,'No More Room in Hell Dedicated Server',27478),(462310,'Day of Infamy Dedicated Server',27477);
/*!40000 ALTER TABLE `game` ENABLE KEYS */;
UNLOCK TABLES;
commit;

--
-- Table structure for table `manager`
--

DROP TABLE IF EXISTS `manager`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `manager` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `username` varchar(31) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `password` char(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT 'https://www.browserling.com/tools/bcrypt',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  `role` enum('broker','project') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'project',
  `active` tinyint unsigned NOT NULL DEFAULT '1',
  `project_ids` json DEFAULT NULL,
  `reg_datetime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `manager_username_UNIQUE` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `manager`
--

LOCK TABLES `manager` WRITE;
/*!40000 ALTER TABLE `manager` DISABLE KEYS */;
set autocommit=0;
INSERT INTO `manager` VALUES (1,'admin','$2a$10$a2BIdFgY.K.hE9wrL29mO.X0RhSy2idgYOt/rVKbid4Rl4fjr9sEu',NULL,'broker',1,NULL,'2021-08-01 00:00:00');
/*!40000 ALTER TABLE `manager` ENABLE KEYS */;
UNLOCK TABLES;
commit;

--
-- Table structure for table `maxmind_db_state`
--

DROP TABLE IF EXISTS `maxmind_db_state`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `maxmind_db_state` (
  `date` date DEFAULT NULL,
  `size` int unsigned DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Leave or remove a link for enable/disable auto-updating GeoLite2 country database:\r\nhttps://github.com/mbto/public_keeper/raw/master/funnyranks/country_en_ru.zip';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
/*!50032 DROP TRIGGER IF EXISTS maxmind_db_state_BEFORE_INSERT */;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`%`*/ /*!50003 TRIGGER `maxmind_db_state_BEFORE_INSERT` BEFORE INSERT ON `maxmind_db_state` FOR EACH ROW BEGIN
	declare error_msg text;
    if(SELECT EXISTS (SELECT 1 FROM `maxmind_db_state`)) then
		set error_msg = concat('Unable to insert more one record to maxmind_db_state');
		SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = error_msg;
    end if;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;

--
-- Table structure for table `port`
--

DROP TABLE IF EXISTS `port`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `port` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `broker_id` int unsigned NOT NULL,
  `project_id` int unsigned NOT NULL,
  `game_app_id` int unsigned NOT NULL,
  `value` smallint unsigned NOT NULL COMMENT 'Local port of game server from which logs will be received',
  `name` varchar(31) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `active` tinyint unsigned NOT NULL DEFAULT '0' COMMENT 'Should receive packets from this port?: 1-receive; 0-UDP packets from this port will be ignored',
  `ffa` tinyint unsigned NOT NULL DEFAULT '0' COMMENT 'game server is FREE-FOR-ALL mode (Example: CS-DeathMatch): 1-true; 0-false',
  `ignore_bots` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '1-ignore statistics, when killer or victim is BOT; 0-don''t ignore (include all player''s)',
  `start_session_on_action` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '1-start player''s session on event "... killed ... with ..." (not for kreedz servers); 0-start player''s session on event "... connected, address ..." or "... entered the game"',
  PRIMARY KEY (`id`),
  KEY `port_broker_id_idx` (`broker_id`),
  KEY `port_value_idx` (`value`),
  KEY `port_project_id_idx` (`project_id`),
  KEY `port_game_app_id_idx` (`game_app_id`),
  CONSTRAINT `port_broker_id_fk` FOREIGN KEY (`broker_id`) REFERENCES `broker` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `port_game_app_id_fk` FOREIGN KEY (`game_app_id`) REFERENCES `game` (`app_id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `port_project_id_fk` FOREIGN KEY (`project_id`) REFERENCES `project` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
/*!50032 DROP TRIGGER IF EXISTS port_BEFORE_INSERT */;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`%`*/ /*!50003 TRIGGER `port_BEFORE_INSERT` BEFORE INSERT ON `port` FOR EACH ROW BEGIN
	declare error_msg text;
    declare existed_id int unsigned;
    
    select id into existed_id from `port` 
		where broker_id = NEW.broker_id
        and `value` = NEW.`value` limit 1;
    
	if(existed_id is not null) then
		set error_msg = concat('Unable to insert value=', NEW.`value`, ' to port, due already existed at broker_id=', NEW.broker_id, ', id=', existed_id);
		SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = error_msg;
	end if;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
/*!50032 DROP TRIGGER IF EXISTS port_BEFORE_UPDATE */;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`%`*/ /*!50003 TRIGGER `port_BEFORE_UPDATE` BEFORE UPDATE ON `port` FOR EACH ROW BEGIN
	declare error_msg text;
    declare existed_id int unsigned;
    
    select id into existed_id from `port` 
		where broker_id = NEW.broker_id
        and `value` = NEW.`value`
        and id != OLD.id limit 1;
    
	if(existed_id is not null) then
		set error_msg = concat('Unable to update port from ', OLD.`value`, ' to ', NEW.`value`, ', due already existed at broker_id=', NEW.broker_id, ', id=', existed_id);
		SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = error_msg;
	end if;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;

--
-- Table structure for table `project`
--

DROP TABLE IF EXISTS `project`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `project` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  `language` enum('en','ru') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'en' COMMENT 'Language affects to country name in player_ip table (from MaxMind GeoLite2 database)',
  `merge_type` enum('Nick','IP','Steam ID') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'Nick',
  `reg_datetime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `database_hostport` varchar(260) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `database_schema` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `database_username` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `database_password` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `database_server_timezone` enum('Africa/Abidjan','Africa/Accra','Africa/Addis_Ababa','Africa/Algiers','Africa/Asmara','Africa/Asmera','Africa/Bamako','Africa/Bangui','Africa/Banjul','Africa/Bissau','Africa/Blantyre','Africa/Brazzaville','Africa/Bujumbura','Africa/Cairo','Africa/Casablanca','Africa/Ceuta','Africa/Conakry','Africa/Dakar','Africa/Dar_es_Salaam','Africa/Djibouti','Africa/Douala','Africa/El_Aaiun','Africa/Freetown','Africa/Gaborone','Africa/Harare','Africa/Johannesburg','Africa/Juba','Africa/Kampala','Africa/Khartoum','Africa/Kigali','Africa/Kinshasa','Africa/Lagos','Africa/Libreville','Africa/Lome','Africa/Luanda','Africa/Lubumbashi','Africa/Lusaka','Africa/Malabo','Africa/Maputo','Africa/Maseru','Africa/Mbabane','Africa/Mogadishu','Africa/Monrovia','Africa/Nairobi','Africa/Ndjamena','Africa/Niamey','Africa/Nouakchott','Africa/Ouagadougou','Africa/Porto-Novo','Africa/Sao_Tome','Africa/Timbuktu','Africa/Tripoli','Africa/Tunis','Africa/Windhoek','America/Adak','America/Anchorage','America/Anguilla','America/Antigua','America/Araguaina','America/Argentina/Buenos_Aires','America/Argentina/Catamarca','America/Argentina/ComodRivadavia','America/Argentina/Cordoba','America/Argentina/Jujuy','America/Argentina/La_Rioja','America/Argentina/Mendoza','America/Argentina/Rio_Gallegos','America/Argentina/Salta','America/Argentina/San_Juan','America/Argentina/San_Luis','America/Argentina/Tucuman','America/Argentina/Ushuaia','America/Aruba','America/Asuncion','America/Atikokan','America/Atka','America/Bahia','America/Bahia_Banderas','America/Barbados','America/Belem','America/Belize','America/Blanc-Sablon','America/Boa_Vista','America/Bogota','America/Boise','America/Buenos_Aires','America/Cambridge_Bay','America/Campo_Grande','America/Cancun','America/Caracas','America/Catamarca','America/Cayenne','America/Cayman','America/Chicago','America/Chihuahua','America/Coral_Harbour','America/Cordoba','America/Costa_Rica','America/Creston','America/Cuiaba','America/Curacao','America/Danmarkshavn','America/Dawson','America/Dawson_Creek','America/Denver','America/Detroit','America/Dominica','America/Edmonton','America/Eirunepe','America/El_Salvador','America/Ensenada','America/Fort_Nelson','America/Fort_Wayne','America/Fortaleza','America/Glace_Bay','America/Godthab','America/Goose_Bay','America/Grand_Turk','America/Grenada','America/Guadeloupe','America/Guatemala','America/Guayaquil','America/Guyana','America/Halifax','America/Havana','America/Hermosillo','America/Indiana/Indianapolis','America/Indiana/Knox','America/Indiana/Marengo','America/Indiana/Petersburg','America/Indiana/Tell_City','America/Indiana/Vevay','America/Indiana/Vincennes','America/Indiana/Winamac','America/Indianapolis','America/Inuvik','America/Iqaluit','America/Jamaica','America/Jujuy','America/Juneau','America/Kentucky/Louisville','America/Kentucky/Monticello','America/Knox_IN','America/Kralendijk','America/La_Paz','America/Lima','America/Los_Angeles','America/Louisville','America/Lower_Princes','America/Maceio','America/Managua','America/Manaus','America/Marigot','America/Martinique','America/Matamoros','America/Mazatlan','America/Mendoza','America/Menominee','America/Merida','America/Metlakatla','America/Mexico_City','America/Miquelon','America/Moncton','America/Monterrey','America/Montevideo','America/Montreal','America/Montserrat','America/Nassau','America/New_York','America/Nipigon','America/Nome','America/Noronha','America/North_Dakota/Beulah','America/North_Dakota/Center','America/North_Dakota/New_Salem','America/Ojinaga','America/Panama','America/Pangnirtung','America/Paramaribo','America/Phoenix','America/Port-au-Prince','America/Port_of_Spain','America/Porto_Acre','America/Porto_Velho','America/Puerto_Rico','America/Punta_Arenas','America/Rainy_River','America/Rankin_Inlet','America/Recife','America/Regina','America/Resolute','America/Rio_Branco','America/Rosario','America/Santa_Isabel','America/Santarem','America/Santiago','America/Santo_Domingo','America/Sao_Paulo','America/Scoresbysund','America/Shiprock','America/Sitka','America/St_Barthelemy','America/St_Johns','America/St_Kitts','America/St_Lucia','America/St_Thomas','America/St_Vincent','America/Swift_Current','America/Tegucigalpa','America/Thule','America/Thunder_Bay','America/Tijuana','America/Toronto','America/Tortola','America/Vancouver','America/Virgin','America/Whitehorse','America/Winnipeg','America/Yakutat','America/Yellowknife','Antarctica/Casey','Antarctica/Davis','Antarctica/DumontDUrville','Antarctica/Macquarie','Antarctica/Mawson','Antarctica/McMurdo','Antarctica/Palmer','Antarctica/Rothera','Antarctica/South_Pole','Antarctica/Syowa','Antarctica/Troll','Antarctica/Vostok','Arctic/Longyearbyen','Asia/Aden','Asia/Almaty','Asia/Amman','Asia/Anadyr','Asia/Aqtau','Asia/Aqtobe','Asia/Ashgabat','Asia/Ashkhabad','Asia/Atyrau','Asia/Baghdad','Asia/Bahrain','Asia/Baku','Asia/Bangkok','Asia/Barnaul','Asia/Beirut','Asia/Bishkek','Asia/Brunei','Asia/Calcutta','Asia/Chita','Asia/Choibalsan','Asia/Chongqing','Asia/Chungking','Asia/Colombo','Asia/Dacca','Asia/Damascus','Asia/Dhaka','Asia/Dili','Asia/Dubai','Asia/Dushanbe','Asia/Famagusta','Asia/Gaza','Asia/Harbin','Asia/Hebron','Asia/Ho_Chi_Minh','Asia/Hong_Kong','Asia/Hovd','Asia/Irkutsk','Asia/Istanbul','Asia/Jakarta','Asia/Jayapura','Asia/Jerusalem','Asia/Kabul','Asia/Kamchatka','Asia/Karachi','Asia/Kashgar','Asia/Kathmandu','Asia/Katmandu','Asia/Khandyga','Asia/Kolkata','Asia/Krasnoyarsk','Asia/Kuala_Lumpur','Asia/Kuching','Asia/Kuwait','Asia/Macao','Asia/Macau','Asia/Magadan','Asia/Makassar','Asia/Manila','Asia/Muscat','Asia/Nicosia','Asia/Novokuznetsk','Asia/Novosibirsk','Asia/Omsk','Asia/Oral','Asia/Phnom_Penh','Asia/Pontianak','Asia/Pyongyang','Asia/Qatar','Asia/Qostanay','Asia/Qyzylorda','Asia/Rangoon','Asia/Riyadh','Asia/Saigon','Asia/Sakhalin','Asia/Samarkand','Asia/Seoul','Asia/Shanghai','Asia/Singapore','Asia/Srednekolymsk','Asia/Taipei','Asia/Tashkent','Asia/Tbilisi','Asia/Tehran','Asia/Tel_Aviv','Asia/Thimbu','Asia/Thimphu','Asia/Tokyo','Asia/Tomsk','Asia/Ujung_Pandang','Asia/Ulaanbaatar','Asia/Ulan_Bator','Asia/Urumqi','Asia/Ust-Nera','Asia/Vientiane','Asia/Vladivostok','Asia/Yakutsk','Asia/Yangon','Asia/Yekaterinburg','Asia/Yerevan','Atlantic/Azores','Atlantic/Bermuda','Atlantic/Canary','Atlantic/Cape_Verde','Atlantic/Faeroe','Atlantic/Faroe','Atlantic/Jan_Mayen','Atlantic/Madeira','Atlantic/Reykjavik','Atlantic/South_Georgia','Atlantic/St_Helena','Atlantic/Stanley','Australia/ACT','Australia/Adelaide','Australia/Brisbane','Australia/Broken_Hill','Australia/Canberra','Australia/Currie','Australia/Darwin','Australia/Eucla','Australia/Hobart','Australia/LHI','Australia/Lindeman','Australia/Lord_Howe','Australia/Melbourne','Australia/North','Australia/NSW','Australia/Perth','Australia/Queensland','Australia/South','Australia/Sydney','Australia/Tasmania','Australia/Victoria','Australia/West','Australia/Yancowinna','Brazil/Acre','Brazil/DeNoronha','Brazil/East','Brazil/West','Canada/Atlantic','Canada/Central','Canada/Eastern','Canada/Mountain','Canada/Newfoundland','Canada/Pacific','Canada/Saskatchewan','Canada/Yukon','Chile/Continental','Chile/EasterIsland','Etc/GMT','Etc/GMT+0','Etc/GMT+1','Etc/GMT+10','Etc/GMT+11','Etc/GMT+12','Etc/GMT+2','Etc/GMT+3','Etc/GMT+4','Etc/GMT+5','Etc/GMT+6','Etc/GMT+7','Etc/GMT+8','Etc/GMT+9','Etc/GMT-0','Etc/GMT-1','Etc/GMT-10','Etc/GMT-11','Etc/GMT-12','Etc/GMT-13','Etc/GMT-14','Etc/GMT-2','Etc/GMT-3','Etc/GMT-4','Etc/GMT-5','Etc/GMT-6','Etc/GMT-7','Etc/GMT-8','Etc/GMT-9','Etc/GMT0','Etc/Greenwich','Etc/UCT','Etc/Universal','Etc/UTC','Etc/Zulu','Europe/Amsterdam','Europe/Andorra','Europe/Astrakhan','Europe/Athens','Europe/Belfast','Europe/Belgrade','Europe/Berlin','Europe/Bratislava','Europe/Brussels','Europe/Bucharest','Europe/Budapest','Europe/Busingen','Europe/Chisinau','Europe/Copenhagen','Europe/Dublin','Europe/Gibraltar','Europe/Guernsey','Europe/Helsinki','Europe/Isle_of_Man','Europe/Istanbul','Europe/Jersey','Europe/Kaliningrad','Europe/Kiev','Europe/Kirov','Europe/Lisbon','Europe/Ljubljana','Europe/London','Europe/Luxembourg','Europe/Madrid','Europe/Malta','Europe/Mariehamn','Europe/Minsk','Europe/Monaco','Europe/Moscow','Europe/Nicosia','Europe/Oslo','Europe/Paris','Europe/Podgorica','Europe/Prague','Europe/Riga','Europe/Rome','Europe/Samara','Europe/San_Marino','Europe/Sarajevo','Europe/Saratov','Europe/Simferopol','Europe/Skopje','Europe/Sofia','Europe/Stockholm','Europe/Tallinn','Europe/Tirane','Europe/Tiraspol','Europe/Ulyanovsk','Europe/Uzhgorod','Europe/Vaduz','Europe/Vatican','Europe/Vienna','Europe/Vilnius','Europe/Volgograd','Europe/Warsaw','Europe/Zagreb','Europe/Zaporozhye','Europe/Zurich','Indian/Antananarivo','Indian/Chagos','Indian/Christmas','Indian/Cocos','Indian/Comoro','Indian/Kerguelen','Indian/Mahe','Indian/Maldives','Indian/Mauritius','Indian/Mayotte','Indian/Reunion','Mexico/BajaNorte','Mexico/BajaSur','Mexico/General','Pacific/Apia','Pacific/Auckland','Pacific/Bougainville','Pacific/Chatham','Pacific/Chuuk','Pacific/Easter','Pacific/Efate','Pacific/Enderbury','Pacific/Fakaofo','Pacific/Fiji','Pacific/Funafuti','Pacific/Galapagos','Pacific/Gambier','Pacific/Guadalcanal','Pacific/Guam','Pacific/Honolulu','Pacific/Johnston','Pacific/Kiritimati','Pacific/Kosrae','Pacific/Kwajalein','Pacific/Majuro','Pacific/Marquesas','Pacific/Midway','Pacific/Nauru','Pacific/Niue','Pacific/Norfolk','Pacific/Noumea','Pacific/Pago_Pago','Pacific/Palau','Pacific/Pitcairn','Pacific/Pohnpei','Pacific/Ponape','Pacific/Port_Moresby','Pacific/Rarotonga','Pacific/Saipan','Pacific/Samoa','Pacific/Tahiti','Pacific/Tarawa','Pacific/Tongatapu','Pacific/Truk','Pacific/Wake','Pacific/Wallis','Pacific/Yap','SystemV/AST4','SystemV/AST4ADT','SystemV/CST6','SystemV/CST6CDT','SystemV/EST5','SystemV/EST5EDT','SystemV/HST10','SystemV/MST7','SystemV/MST7MDT','SystemV/PST8','SystemV/PST8PDT','SystemV/YST9','SystemV/YST9YDT','US/Alaska','US/Aleutian','US/Arizona','US/Central','US/East-Indiana','US/Eastern','US/Hawaii','US/Indiana-Starke','US/Michigan','US/Mountain','US/Pacific','US/Pacific-New','US/Samoa','ACT','AET','AGT','ART','AST','BET','BST','CAT','CET','CNT','CST','CST6CDT','CTT','Cuba','EAT','ECT','EET','Egypt','Eire','EST','EST5EDT','GB','GB-Eire','GMT','GMT0','Greenwich','Hongkong','HST','Iceland','IET','Iran','Israel','IST','Jamaica','Japan','JST','Kwajalein','Libya','MET','MIT','MST','MST7MDT','Navajo','NET','NST','NZ','NZ-CHAT','PLT','PNT','Poland','Portugal','PRC','PRT','PST','PST8PDT','ROK','Singapore','SST','Turkey','UCT','Universal','UTC','VST','W-SU','WET','Zulu') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping routines for database 'funnyranks'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2021-08-01 00:00:00
