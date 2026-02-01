-- MySQL dump 10.13  Distrib 8.0.31, for Win64 (x86_64)
--
-- Host: localhost    Database: funnyranks_stats
-- ------------------------------------------------------
-- Server version	8.0.31

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
-- Table structure for table `history`
--

DROP TABLE IF EXISTS `history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `history` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `player_id` int unsigned NOT NULL,
  `old_level` int unsigned DEFAULT NULL,
  `new_level` int unsigned DEFAULT NULL,
  `reg_datetime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `history_old_level_idx` (`old_level`),
  KEY `history_new_level_idx` (`new_level`),
  KEY `history_player_id_id_idx` (`player_id` DESC,`id` DESC) USING BTREE,
  KEY `history_player_id_fk` (`player_id`),
  CONSTRAINT `history_new_level_fk` FOREIGN KEY (`new_level`) REFERENCES `rank` (`level`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `history_old_level_fk` FOREIGN KEY (`old_level`) REFERENCES `rank` (`level`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `history_player_id_fk` FOREIGN KEY (`player_id`) REFERENCES `player` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `history`
--

LOCK TABLES `history` WRITE;
/*!40000 ALTER TABLE `history` DISABLE KEYS */;
set autocommit=0;
/*!40000 ALTER TABLE `history` ENABLE KEYS */;
UNLOCK TABLES;
commit;

--
-- Table structure for table `player`
--

DROP TABLE IF EXISTS `player`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `player` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `kills` int unsigned NOT NULL DEFAULT '0',
  `deaths` int unsigned NOT NULL DEFAULT '0',
  `time_secs` int unsigned NOT NULL DEFAULT '0',
  `level` int unsigned DEFAULT NULL,
  `stars_unicode` varchar(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `stars_compat` varchar(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `lastseen_datetime` datetime DEFAULT NULL,
  `last_server_name` varchar(31) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `top_cursor_cover_idx` (`level` DESC,`time_secs` DESC,`id` DESC,`kills`,`deaths`) USING BTREE,
  KEY `player_rank_level_fk_idx` (`level`),
  KEY `lastseen_datetime_idx` (`lastseen_datetime`),
  CONSTRAINT `player_rank_level_fk_idx` FOREIGN KEY (`level`) REFERENCES `rank` (`level`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `player`
--

LOCK TABLES `player` WRITE;
/*!40000 ALTER TABLE `player` DISABLE KEYS */;
set autocommit=0;
/*!40000 ALTER TABLE `player` ENABLE KEYS */;
UNLOCK TABLES;
commit;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
/*!50032 DROP TRIGGER IF EXISTS player_BEFORE_INSERT */;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`%`*/ /*!50003 TRIGGER `player_BEFORE_INSERT` BEFORE INSERT ON `player` FOR EACH ROW BEGIN
  DECLARE ranks_count INT UNSIGNED DEFAULT 0;
  set ranks_count = (SELECT COUNT(*) FROM `rank`);
	set NEW.level = calculate_level(NEW.kills, NEW.deaths, NEW.time_secs, ranks_count);
	set NEW.stars_unicode = build_stars_unicode(NEW.level, ranks_count);
	set NEW.stars_compat = build_stars_compat(NEW.level, ranks_count);
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
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
/*!50032 DROP TRIGGER IF EXISTS player_BEFORE_UPDATE */;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`%`*/ /*!50003 TRIGGER `player_BEFORE_UPDATE` BEFORE UPDATE ON `player` FOR EACH ROW BEGIN
	DECLARE ranks_count INT UNSIGNED DEFAULT 0;
	IF (!(OLD.kills <=> NEW.kills)
     or !(OLD.deaths <=> NEW.deaths)
     or !(OLD.time_secs <=> NEW.time_secs)) THEN
		set ranks_count = (SELECT COUNT(*) FROM `rank`);
		set NEW.level = calculate_level(NEW.kills, NEW.deaths, NEW.time_secs, ranks_count);
        
		if (!(OLD.level <=> NEW.level)) then
			set NEW.stars_unicode = build_stars_unicode(NEW.level, ranks_count);
			set NEW.stars_compat = build_stars_compat(NEW.level, ranks_count);
			insert into history (player_id, old_level, new_level)
			values (NEW.id, OLD.level, NEW.level);
		end if;
	END IF;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;

--
-- Table structure for table `player_ip`
--

DROP TABLE IF EXISTS `player_ip`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `player_ip` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `player_id` int unsigned NOT NULL,
  `ip` int unsigned NOT NULL,
  `ip4` varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci GENERATED ALWAYS AS (inet_ntoa(`ip`)) STORED COMMENT 'Auto-generated IP format v4 - AAA.BBB.CCC.DDD',
  `maxmind_geoname_id` int unsigned DEFAULT NULL,
  `reg_datetime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uqx_ip` (`player_id`,`ip`),
  KEY `player_ip_find_idx` (`player_id`,`reg_datetime` DESC,`id` DESC,`ip`) USING BTREE,
  KEY `player_ip_ip_idx` (`ip`,`reg_datetime` DESC,`id` DESC,`player_id`) USING BTREE,
  CONSTRAINT `player_ip_player_id_fk` FOREIGN KEY (`player_id`) REFERENCES `player` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `player_ip`
--

LOCK TABLES `player_ip` WRITE;
/*!40000 ALTER TABLE `player_ip` DISABLE KEYS */;
set autocommit=0;
/*!40000 ALTER TABLE `player_ip` ENABLE KEYS */;
UNLOCK TABLES;
commit;

--
-- Table structure for table `player_name`
--

DROP TABLE IF EXISTS `player_name`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `player_name` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `player_id` int unsigned NOT NULL,
  `name` varchar(31) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `reg_datetime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uqx_name` (`player_id`,`name`),
  KEY `player_name_find_idx` (`player_id`,`reg_datetime` DESC,`id` DESC,`name`) USING BTREE,
  KEY `player_name_name_idx` (`name`,`reg_datetime` DESC,`id` DESC,`player_id`) USING BTREE,
  CONSTRAINT `player_name_player_id_fk` FOREIGN KEY (`player_id`) REFERENCES `player` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `player_name`
--

LOCK TABLES `player_name` WRITE;
/*!40000 ALTER TABLE `player_name` DISABLE KEYS */;
set autocommit=0;
/*!40000 ALTER TABLE `player_name` ENABLE KEYS */;
UNLOCK TABLES;
commit;

--
-- Table structure for table `player_steamid`
--

DROP TABLE IF EXISTS `player_steamid`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `player_steamid` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `player_id` int unsigned NOT NULL,
  `steamid64` decimal(17,0) unsigned NOT NULL,
  `steamid2` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci GENERATED ALWAYS AS (concat((`steamid64` % 2),_utf8mb4':',truncate((((`steamid64` - 76561197960265728) - (`steamid64` % 2)) / 2),0))) STORED COMMENT 'Auto-generated SteamID format v2 - STEAM_0:X:YYYYYYYYYY - https://developer.valvesoftware.com/wiki/SteamID',
  `steamid3` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci GENERATED ALWAYS AS (concat(1,_utf8mb4':',(`steamid64` - 76561197960265728))) STORED COMMENT 'Auto-generated SteamID format v3 - [U:X:YYYYYYYYYY] - https://developer.valvesoftware.com/wiki/SteamID',
  `reg_datetime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uqx_steamid` (`player_id`,`steamid64`),
  KEY `player_steamid_find_idx` (`player_id`,`reg_datetime` DESC,`id` DESC,`steamid64`) USING BTREE,
  KEY `player_steamid_steamid64_idx` (`steamid64`,`reg_datetime` DESC,`id` DESC,`player_id`) USING BTREE,
  CONSTRAINT `player_steamid_player_id_fk` FOREIGN KEY (`player_id`) REFERENCES `player` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `player_steamid`
--

LOCK TABLES `player_steamid` WRITE;
/*!40000 ALTER TABLE `player_steamid` DISABLE KEYS */;
set autocommit=0;
/*!40000 ALTER TABLE `player_steamid` ENABLE KEYS */;
UNLOCK TABLES;
commit;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
/*!50032 DROP TRIGGER IF EXISTS player_steamid_BEFORE_INSERT */;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`%`*/ /*!50003 TRIGGER `player_steamid_BEFORE_INSERT` BEFORE INSERT ON `player_steamid` FOR EACH ROW BEGIN
  declare error_msg text;
  
  if(!is_valid_steamid64(NEW.steamid64, false)) then
      set error_msg = concat('Invalid steamid64=', NEW.steamid64);
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
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
/*!50032 DROP TRIGGER IF EXISTS player_steamid_BEFORE_UPDATE */;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`%`*/ /*!50003 TRIGGER `player_steamid_BEFORE_UPDATE` BEFORE UPDATE ON `player_steamid` FOR EACH ROW BEGIN
	declare error_msg text;
  
  if(!is_valid_steamid64(NEW.steamid64, false)) then
      set error_msg = concat('Invalid steamid64=', NEW.steamid64);
      SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = error_msg;
  end if;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;

--
-- Table structure for table `rank`
--

DROP TABLE IF EXISTS `rank`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `rank` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `level` int unsigned NOT NULL,
  `kaomoji` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `name_ru` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `name_en` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `rank_name_ru_UNIQUE` (`name_ru`),
  UNIQUE KEY `rank_level_UNIQUE` (`level`),
  UNIQUE KEY `rank_name_en_UNIQUE` (`name_en`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `rank`
--

LOCK TABLES `rank` WRITE;
/*!40000 ALTER TABLE `rank` DISABLE KEYS */;
set autocommit=0;
INSERT INTO `rank` VALUES (1,1,'¯\\_(ツ)_/¯','Гастролер','Guest'),(2,2,'¯\\_(ツ)_/¯','Тюфяк','Mattress'),(3,3,'¯\\_(ツ)_/¯','Овощ','Vegetable'),(4,4,'¯\\_(ツ)_/¯','Кабан','Boar'),(5,5,'ˁ°ᴥ°ˀ','Силач','Strongman'),(6,6,'ˁ°ᴥ°ˀ','Рандомщик','Spray shooter'),(7,7,'ˁ°ᴥ°ˀ','Пацан','Kid'),(8,8,'ˁ°ᴥ°ˀ','Смертник','Bomber'),(9,9,'(°‿°)','Везунчик','Lucky'),(10,10,'(°‿°)','Жульбан','Rogue'),(11,11,'(°‿°)','Гопник','Chav'),(12,12,'(°‿°)','Кэмпер','Camper'),(13,13,'ᕙ(°ʖ°)ᕗ','Помощник','Assistant'),(14,14,'ᕙ(°ʖ°)ᕗ','Вуйко','Vuiko'),(15,15,'ᕙ(°ʖ°)ᕗ','Донышко','Bottom'),(16,16,'ᕙ(°ʖ°)ᕗ','Профан','Profane'),(17,17,'ᕦ(°_°)ᕤ','Титушка','Instigator'),(18,18,'ᕦ(°_°)ᕤ','Боцман','Boatswain'),(19,19,'ᕦ(°_°)ᕤ','Школьник','Schoolboy'),(20,20,'ᕦ(°_°)ᕤ','Мусор','Rubbish'),(21,21,'龴ↀ‿ↀ龴','Отбой','Hang up'),(22,22,'龴ↀ‿ↀ龴','ПТУ-шник','Vocational school'),(23,23,'龴ↀ‿ↀ龴','Зек','Snakes'),(24,24,'龴ↀ‿ↀ龴','Бывалый','Experienced'),(25,25,'(ಥ﹏ಥ)','Прораб','Foreman'),(26,26,'(ಥ﹏ಥ)','Жестянщик','Tinsmith'),(27,27,'(ಥ﹏ಥ)','Пахан','Head of the gang'),(28,28,'(ಥ﹏ಥ)','Директор','Director'),(29,29,'(ง°ل͜°)ง','Сынок','Son'),(30,30,'(ง°ل͜°)ง','Мордоворот','Jowly'),(31,31,'(ง°ل͜°)ง','Геймер','Gamer'),(32,32,'(ง°ل͜°)ง','Отважный','Brave'),(33,33,'(づ° ³°)づ','Убийца','Killer'),(34,34,'(づ° ³°)づ','Халявщик','Freeloader'),(35,35,'(づ° ³°)づ','Псих','Crazy'),(36,36,'(づ° ³°)づ','Йовбак','Mercenary'),(37,37,'(ﾉ°ヮ°)ﾉ*:･ﾟ✧','Громила','Brute'),(38,38,'(ﾉ°ヮ°)ﾉ*:･ﾟ✧','Мужик','Man'),(39,39,'(ﾉ°ヮ°)ﾉ*:･ﾟ✧','Дезертир','Deserter'),(40,40,'(ﾉ°ヮ°)ﾉ*:･ﾟ✧','Боец','Fighter'),(41,41,'( ° ͜ʖ °)','Софт','Cheater'),(42,42,'( ° ͜ʖ °)','Громила-здоровяк','Big brute'),(43,43,'( ° ͜ʖ °)','Партизан','Partisan'),(44,44,'( ° ͜ʖ °)','Сенсей','Sensei'),(45,45,'t(ಠ益ಠt)','Рыцарь','Knight'),(46,46,'t(ಠ益ಠt)','Спецназовец','Spetsnaz'),(47,47,'t(ಠ益ಠt)','Тащит всю команду','Drags the whole team'),(48,48,'t(ಠ益ಠt)','Олдфаг','Oldfag'),(49,49,'(ノಠ益ಠ)ノ彡','Каратель','The Punisher'),(50,50,'(ノಠ益ಠ)ノ彡','Здоровяк','Big man'),(51,51,'(ノಠ益ಠ)ノ彡','Аим','Aim'),(52,52,'(ノಠ益ಠ)ノ彡','Фраер','Virgin'),(53,53,'ლ(ಠ益ಠლ)','Штурмовой','Assault'),(54,54,'ლ(ಠ益ಠლ)','Босс','Boss'),(55,55,'（︶︿︶）','Старая школа','Old School'),(56,56,'（︶︿︶）','Непобедимый','Invincible');
/*!40000 ALTER TABLE `rank` ENABLE KEYS */;
UNLOCK TABLES;
commit;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
/*!50032 DROP TRIGGER IF EXISTS rank_AFTER_INSERT */;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`%`*/ /*!50003 TRIGGER `rank_AFTER_INSERT` AFTER INSERT ON `rank` FOR EACH ROW BEGIN
  DECLARE ranks_count INT UNSIGNED DEFAULT 0;
  set ranks_count = (SELECT COUNT(*) FROM `rank`);

	update player p set p.level = calculate_level(p.kills, p.deaths, p.time_secs, ranks_count);
	update player p set
    p.stars_unicode = build_stars_unicode(p.level, ranks_count),
    p.stars_compat = build_stars_compat(p.level, ranks_count);
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
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
/*!50032 DROP TRIGGER IF EXISTS rank_AFTER_UPDATE */;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`%`*/ /*!50003 TRIGGER `rank_AFTER_UPDATE` AFTER UPDATE ON `rank` FOR EACH ROW BEGIN
	DECLARE ranks_count INT UNSIGNED DEFAULT 0;
	if(!(OLD.level <=> NEW.level)) then
    set ranks_count = (SELECT COUNT(*) FROM `rank`);
    update player p set p.level = calculate_level(p.kills, p.deaths, p.time_secs, ranks_count);
    update player p set
      p.stars_unicode = build_stars_unicode(p.level, ranks_count),
      p.stars_compat = build_stars_compat(p.level, ranks_count);
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
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
/*!50032 DROP TRIGGER IF EXISTS rank_AFTER_DELETE */;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`%`*/ /*!50003 TRIGGER `rank_AFTER_DELETE` AFTER DELETE ON `rank` FOR EACH ROW BEGIN
  DECLARE ranks_count INT UNSIGNED DEFAULT 0;
  set ranks_count = (SELECT COUNT(*) FROM `rank`);

	update player p set p.level = calculate_level(p.kills, p.deaths, p.time_secs, ranks_count);
	update player p set
    p.stars_unicode = build_stars_unicode(p.level, ranks_count),
    p.stars_compat = build_stars_compat(p.level, ranks_count);
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;

--
-- Dumping events for database 'funnyranks_stats'
--

--
-- Dumping routines for database 'funnyranks_stats'
--
/*!50003 DROP FUNCTION IF EXISTS `build_human_time` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` FUNCTION `build_human_time`(time_secs int unsigned, lang varchar(5)) RETURNS text CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci
    DETERMINISTIC
BEGIN
	declare y int unsigned default (time_secs DIV (60 * 60 * 24 * 30 * 12));
	declare mn int unsigned default (time_secs DIV (60 * 60 * 24 * 30)) % 12;
	declare d int unsigned default (time_secs DIV (60 * 60 * 24)) % 30;
	declare h int unsigned default (time_secs DIV (60 * 60)) % 24;
	declare m int unsigned default (time_secs DIV 60) % 60;
	declare s int unsigned default time_secs % 60;
    declare human_time text default '';
    
    if(lang = 'ru') then
		if(y > 0) then set human_time = concat(human_time,y,declension(y,'год','года','лет')); end if;
		if(mn > 0) then set human_time = concat(human_time,if(y > 0, ' ', ''),mn,'мес'); end if;
		if(d > 0) then set human_time = concat(human_time,if(y > 0 or mn > 0, ' ', ''),d,'дн'); end if;
		if(h > 0) then set human_time = concat(human_time,if(y > 0 or mn > 0 or d > 0, ' ', ''),h,'ч'); end if;
		if(m > 0) then set human_time = concat(human_time,if(y > 0 or mn > 0 or d > 0 or h > 0, ' ', ''),m,'м'); end if;
		if(!(y > 0 or mn > 0 or d > 0) and (((s > 0 and (h > 0 or m > 0))) or (h = 0 and m = 0))) then
			set human_time = concat(human_time,if(y > 0 or mn > 0 or d > 0 or h > 0 or m > 0, ' ', ''),s,'с'); 
		end if;
    else
		if(y > 0) then set human_time = concat(human_time,y,declension(y,'year','years','years')); end if;
		if(mn > 0) then set human_time = concat(human_time,if(y > 0, ' ', ''),mn,'mo'); end if;
		if(d > 0) then set human_time = concat(human_time,if(y > 0 or mn > 0, ' ', ''),d,'d'); end if;
		if(h > 0) then set human_time = concat(human_time,if(y > 0 or mn > 0 or d > 0, ' ', ''),h,'h'); end if;
		if(m > 0) then set human_time = concat(human_time,if(y > 0 or mn > 0 or d > 0 or h > 0, ' ', ''),m,'m'); end if;
		if(!(y > 0 or mn > 0 or d > 0) and (((s > 0 and (h > 0 or m > 0))) or (h = 0 and m = 0))) then
			set human_time = concat(human_time,if(y > 0 or mn > 0 or d > 0 or h > 0 or m > 0, ' ', ''),s,'s'); 
		end if;
    end if;
	RETURN human_time;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP FUNCTION IF EXISTS `build_stars_compat` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` FUNCTION `build_stars_compat`(level int unsigned, ranks_total int unsigned) RETURNS varchar(6) CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci
    DETERMINISTIC
BEGIN
	declare black_stars int unsigned default greatest(1, least(6, truncate(level * 6 / greatest(ranks_total, 1), 0)));
	declare white_stars int unsigned default 6 - black_stars;
	
	return concat(repeat("彡", black_stars), repeat("ノ", white_stars));
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP FUNCTION IF EXISTS `build_stars_unicode` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` FUNCTION `build_stars_unicode`(level int unsigned, ranks_total int unsigned) RETURNS varchar(6) CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci
    DETERMINISTIC
BEGIN
	declare black_stars int unsigned default greatest(1, least(6, truncate(level * 6 / greatest(ranks_total, 1), 0)));
	declare white_stars int unsigned default 6 - black_stars;
	
 	return concat(repeat("★", black_stars), repeat("☆", white_stars));
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP FUNCTION IF EXISTS `calculate_level` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` FUNCTION `calculate_level`(
    kills     INT UNSIGNED,
    deaths    INT UNSIGNED,
    time_secs INT UNSIGNED,
    ranks_count INT UNSIGNED
) RETURNS int unsigned
    READS SQL DATA
    DETERMINISTIC
BEGIN
/*
==========  КАК РЕГУЛИРОВАТЬ СКОРОСТЬ ПРОКАЧКИ  ==========

PTS_PER_LVL = 1.25 – цена уровня (меньше = быстрее)
TIME_WEIGHT = 0.95 – 95 % очков даёт время (kreedz-friendly)
MAX_TIME_PTS = 75 – потолок выше фактических 72 LN-ед.
PTS_PER_HOUR = 0.12 – 0.12 LN-ед./час (600 ч = 72 LN-ед.)

Параметр               Эффект                  Пример изменения
--------------------   ---------------------   -------------------------
PTS_PER_LVL ↑          Медленнее               1.25 → 2.5  (в 2 раза дольше)
PTS_PER_LVL ↓          Быстрее                 1.25 → 0.62 (в 2 раза быстрее)

TIME_WEIGHT ↑          Время важнее            0.7 → 0.95 (kreedz-игрок растёт быстрее)
TIME_WEIGHT ↓          K/D важнее              0.95 → 0.5  (фраги важнее времени)

MAX_TIME_PTS ↑         Выше потолок по времени 60 → 90  (можно «качаться» дольше)
MAX_TIME_PTS ↓         Ниже потолок            75 → 50  (время быстро перестаёт помогать)

PTS_PER_HOUR ↑         Больше очков за час     0.12 → 0.24 (каждый час в 2 раза ценнее)
PTS_PER_HOUR ↓         Меньше очков за час     0.12 → 0.06 (в 2 раза медленнее)
*/
    DECLARE PTS_PER_LVL   DOUBLE DEFAULT 0.55;
    DECLARE MIN_TIME_SEC  INT    DEFAULT 3600;
    DECLARE MIN_KD        DOUBLE DEFAULT 0.30;
    DECLARE TIME_WEIGHT   DOUBLE DEFAULT 0.35;
    DECLARE MAX_TIME_PTS  DOUBLE DEFAULT 120;
    DECLARE PTS_PER_HOUR  DOUBLE DEFAULT 0.11;

    DECLARE skill_pts DOUBLE DEFAULT 0;
    DECLARE time_pts  DOUBLE DEFAULT 0;
    DECLARE total_pts DOUBLE DEFAULT 0;
    DECLARE rating    DOUBLE DEFAULT 0;
    DECLARE result    INT UNSIGNED DEFAULT 1;

    /* ----------  1. Входной порог  ---------- */
    IF time_secs < MIN_TIME_SEC THEN
        RETURN 1;
    END IF;

    /*  K/D ниже минимума  */
    IF deaths = 0 THEN
        IF kills = 0 THEN
            RETURN 1;               -- 0/0
        END IF;
        -- только фраги: считаем K/D = ∞, но skill = LN(1+kills) - LN(MIN_KD)
        SET skill_pts := GREATEST(0, LN(1+kills) - LN(MIN_KD));
    ELSE
        IF kills/deaths < MIN_KD THEN
            RETURN 1;
        END IF;
        SET skill_pts := GREATEST(0, LN(kills) - LN(deaths) - LN(MIN_KD));
    END IF;

    /* ----------  3. Time-очки  ---------- */
    IF time_secs > 0 THEN
        SET time_pts := LEAST(MAX_TIME_PTS, time_secs / 3600 * PTS_PER_HOUR);
    ELSE
        SET time_pts := 0;
    END IF;

    /* ----------  4. Итоговые очки  ---------- */
    SET total_pts := (1 - TIME_WEIGHT) * skill_pts + TIME_WEIGHT * time_pts;
    SET rating    := total_pts / PTS_PER_LVL;

    /* ----------  5. Ближайший уровень 1..ranks_count  ---------- */
    SET result := GREATEST(1, LEAST(ranks_count, FLOOR(rating + 0.5)));

    RETURN result;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP FUNCTION IF EXISTS `convert_steamid2_to_steamid64` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` FUNCTION `convert_steamid2_to_steamid64`(steamid2 varchar(22)) RETURNS decimal(17,0)
    DETERMINISTIC
BEGIN
  if (!is_valid_steamid2(steamid2, true, true)) then
		return null;
  end if;
return 76561197960265728 + CAST(SUBSTRING(steamid2, 9, 1) AS UNSIGNED) + CAST(SUBSTRING(steamid2, 11) * 2 AS UNSIGNED);
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP FUNCTION IF EXISTS `convert_steamid64_to_steamid2` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` FUNCTION `convert_steamid64_to_steamid2`(steamid64 decimal(17,0)) RETURNS text CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci
    DETERMINISTIC
BEGIN
  if (!is_valid_steamid64(steamid64, true)) then
		return null;
end if;
return concat((`steamid64` % 2),':',truncate((((`steamid64` - 76561197960265728) - (`steamid64` % 2)) / 2),0));
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP FUNCTION IF EXISTS `declension` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` FUNCTION `declension`(
    value INT,
    opt1 VARCHAR(32),
    opt2 VARCHAR(32),
    opt3 VARCHAR(32)
) RETURNS varchar(32) CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci
    DETERMINISTIC
BEGIN
    DECLARE n INT UNSIGNED DEFAULT ABS(value);
    DECLARE last_two INT UNSIGNED DEFAULT n % 100;
    DECLARE last_one INT UNSIGNED DEFAULT n % 10;
    IF last_two BETWEEN 11 AND 19 THEN
        RETURN opt3;
    END IF;

    IF last_one = 1 THEN
        RETURN opt1;
    ELSEIF last_one BETWEEN 2 AND 4 THEN
        RETURN opt2;
    ELSE
        RETURN opt3;
    END IF;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP FUNCTION IF EXISTS `get_last_ip4` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` FUNCTION `get_last_ip4`(p_player_id INT UNSIGNED) RETURNS varchar(15) CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci
    READS SQL DATA
    DETERMINISTIC
BEGIN
  DECLARE v_ip VARCHAR(15) DEFAULT NULL;
  SELECT pip.ip4
    INTO v_ip
    FROM player_ip pip
   WHERE pip.player_id = p_player_id
   ORDER BY pip.reg_datetime DESC, pip.id DESC
   LIMIT 1;
  RETURN v_ip;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP FUNCTION IF EXISTS `get_last_name` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` FUNCTION `get_last_name`(p_player_id INT UNSIGNED) RETURNS varchar(31) CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci
    READS SQL DATA
    DETERMINISTIC
BEGIN
  DECLARE v_name VARCHAR(31) DEFAULT NULL;
  SELECT pn.name
    INTO v_name
    FROM player_name pn
   WHERE pn.player_id = p_player_id
   ORDER BY pn.reg_datetime DESC, pn.id DESC
   LIMIT 1;
  RETURN v_name;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP FUNCTION IF EXISTS `get_last_steamid2` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` FUNCTION `get_last_steamid2`(p_player_id INT UNSIGNED) RETURNS varchar(20) CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci
    READS SQL DATA
    DETERMINISTIC
BEGIN
  DECLARE v_steamid2 VARCHAR(20) DEFAULT NULL;
  SELECT ps.steamid2
    INTO v_steamid2
    FROM player_steamid ps
   WHERE ps.player_id = p_player_id
   ORDER BY ps.reg_datetime DESC, ps.id DESC
   LIMIT 1;
  RETURN v_steamid2;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP FUNCTION IF EXISTS `is_valid_ip4` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` FUNCTION `is_valid_ip4`(ip varchar(21), nullable boolean, with_port boolean) RETURNS tinyint unsigned
    DETERMINISTIC
BEGIN
	if(nullable and ip is null) then
		return true;
	end if;
    
    if with_port then
		return (select ip regexp "^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?):\\d{1,5}$") is true;
	end if;
	
	return (select ip regexp "^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$") is true;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP FUNCTION IF EXISTS `is_valid_steamid2` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` FUNCTION `is_valid_steamid2`(steamid2 varchar(22), nullable boolean, only_legal boolean) RETURNS tinyint unsigned
    DETERMINISTIC
BEGIN
  declare steamid64 decimal(17,0) unsigned;
	if(nullable and steamid2 is null) then
		return true;
	end if;
	if(only_legal) then
	  if((select steamid2 regexp "^STEAM_0:[0-1]:\\d+$") is false) then
		  return false;
    end if;
		set steamid64 = 76561197960265728 + CAST(SUBSTRING(steamid2, 9, 1) AS UNSIGNED) + CAST(SUBSTRING(steamid2, 11) * 2 AS UNSIGNED);
		return is_valid_steamid64(steamid64, false);
	end if;
    
    return (select steamid2 regexp "^STEAM_\\d:\\d:\\d+$") is true;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP FUNCTION IF EXISTS `is_valid_steamid64` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` FUNCTION `is_valid_steamid64`(steamid64 decimal(17,0), nullable boolean) RETURNS tinyint unsigned
    DETERMINISTIC
BEGIN
	if(nullable and steamid64 is null) then
		return true;
	end if;
  if(steamid64 is null or steamid64 < 76561197960265729 or steamid64 > 76561202255233023) then
    return false;
  end if;
  return true;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `HistoryByIdOrAll` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` PROCEDURE `HistoryByIdOrAll`(
    IN target_id INT UNSIGNED,        -- NULL → вся история, иначе по игроку
    IN display_type VARCHAR(7),       -- 'nick', 'ip', 'steamid'
    IN lang VARCHAR(5),
    IN is_unicode TINYINT UNSIGNED,
    IN poffset INT UNSIGNED,
    IN plimit INT UNSIGNED
)
    READS SQL DATA
BEGIN
    IF target_id IS NOT NULL THEN
        SELECT
            h.reg_datetime,
            p.id,
            CASE
              WHEN display_type = 'ip' THEN get_last_ip4(p.id)
              WHEN display_type = 'steamid' THEN get_last_steamid2(p.id)
              ELSE get_last_name(p.id)
            END AS identity,
            CASE WHEN is_unicode = 1
                THEN p.stars_unicode
                ELSE p.stars_compat
            END AS stars,
            h.old_level,
            CASE WHEN lang = 'ru'
              THEN r_old.name_ru
              ELSE r_old.name_en
            END AS old_rank_name,
            r_old.kaomoji old_kaomoji,
            h.new_level,
            CASE WHEN lang = 'ru'
              THEN r_new.name_ru
              ELSE r_new.name_en
            END AS new_rank_name,
            r_new.kaomoji new_kaomoji
        FROM history h
        JOIN player p ON p.id = h.player_id
        LEFT JOIN `rank` r_old ON r_old.level = h.old_level
        LEFT JOIN `rank` r_new ON r_new.level = h.new_level
        WHERE h.player_id = target_id
        ORDER BY h.id DESC
        LIMIT poffset, plimit;

    ELSE
        SELECT
            h.reg_datetime,
            p.id,
            CASE
              WHEN display_type = 'ip' THEN get_last_ip4(p.id)
              WHEN display_type = 'steamid' THEN get_last_steamid2(p.id)
              ELSE get_last_name(p.id)
            END AS identity,
            CASE WHEN is_unicode = 1
                THEN p.stars_unicode
                ELSE p.stars_compat
            END AS stars,
            h.old_level,
            CASE WHEN lang = 'ru'
              THEN r_old.name_ru
              ELSE r_old.name_en
            END AS old_rank_name,
            r_old.kaomoji old_kaomoji,
            h.new_level,
            CASE WHEN lang = 'ru'
              THEN r_new.name_ru
              ELSE r_new.name_en
            END AS new_rank_name,
            r_new.kaomoji new_kaomoji
        FROM history h
        JOIN player p ON p.id = h.player_id
        LEFT JOIN `rank` r_old ON r_old.level = h.old_level
        LEFT JOIN `rank` r_new ON r_new.level = h.new_level
        ORDER BY h.id DESC
        LIMIT poffset, plimit;
    END IF;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `HistoryByIdOrAllPrettyRanks` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` PROCEDURE `HistoryByIdOrAllPrettyRanks`(
    IN target_id INT UNSIGNED,        -- NULL → вся история, иначе по игроку
    IN display_type VARCHAR(7),       -- 'nick', 'ip', 'steamid'
    IN lang VARCHAR(5),
    IN is_unicode TINYINT UNSIGNED,
    IN poffset INT UNSIGNED,
    IN plimit INT UNSIGNED
)
    READS SQL DATA
BEGIN
    IF target_id IS NOT NULL THEN
        SELECT
            h.reg_datetime,
            p.id,
            CASE
              WHEN display_type = 'ip' THEN get_last_ip4(p.id)
              WHEN display_type = 'steamid' THEN get_last_steamid2(p.id)
              ELSE get_last_name(p.id)
            END AS identity,
            CASE WHEN is_unicode = 1
                THEN p.stars_unicode
                ELSE p.stars_compat
            END AS stars,
            CASE
                WHEN h.old_level IS NOT NULL THEN
                    CONCAT('[', h.old_level, '] ',
                           CASE WHEN lang = 'ru' THEN r_old.name_ru ELSE r_old.name_en END,
                           ' ', r_old.kaomoji)
                ELSE NULL
            END AS old_rank,
            CASE
                WHEN h.new_level IS NOT NULL THEN
                    CONCAT('[', h.new_level, '] ',
                           CASE WHEN lang = 'ru' THEN r_new.name_ru ELSE r_new.name_en END,
                           ' ', r_new.kaomoji)
                ELSE NULL
            END AS new_rank
        FROM history h
        JOIN player p ON p.id = h.player_id
        LEFT JOIN `rank` r_old ON r_old.level = h.old_level
        LEFT JOIN `rank` r_new ON r_new.level = h.new_level
        WHERE h.player_id = target_id
        ORDER BY h.id DESC
        LIMIT poffset, plimit;
    ELSE
        SELECT
            h.reg_datetime,
            p.id,
            CASE
              WHEN display_type = 'ip' THEN get_last_ip4(p.id)
              WHEN display_type = 'steamid' THEN get_last_steamid2(p.id)
              ELSE get_last_name(p.id)
            END AS identity,
            CASE WHEN is_unicode = 1
                THEN p.stars_unicode
                ELSE p.stars_compat
            END AS stars,
            CASE
                WHEN h.old_level IS NOT NULL THEN
                    CONCAT('[', h.old_level, '] ',
                           CASE WHEN lang = 'ru' THEN r_old.name_ru ELSE r_old.name_en END,
                           ' ', r_old.kaomoji)
                ELSE NULL
            END AS old_rank,
            CASE
                WHEN h.new_level IS NOT NULL THEN
                    CONCAT('[', h.new_level, '] ',
                           CASE WHEN lang = 'ru' THEN r_new.name_ru ELSE r_new.name_en END,
                           ' ', r_new.kaomoji)
                ELSE NULL
            END AS new_rank
        FROM history h
        JOIN player p ON p.id = h.player_id
        LEFT JOIN `rank` r_old ON r_old.level = h.old_level
        LEFT JOIN `rank` r_new ON r_new.level = h.new_level
        ORDER BY h.id DESC
        LIMIT poffset, plimit;
    END IF;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `PlayerById` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` PROCEDURE `PlayerById`(
    IN display_type VARCHAR(7),               -- 'nick', 'ip', 'steamid'
    IN target_id INT UNSIGNED,
    IN lang VARCHAR(5),
    IN is_unicode TINYINT UNSIGNED
)
    READS SQL DATA
BEGIN
    IF target_id IS NOT NULL THEN
        SELECT
            (SELECT COUNT(*) + 1 FROM player p2 
             WHERE (p2.level, p2.time_secs, p2.id) > (p.level, p.time_secs, p.id)
            ) AS place,
            p.id,
            CASE
              WHEN display_type = 'ip' THEN get_last_ip4(p.id)
              WHEN display_type = 'steamid' THEN get_last_steamid2(p.id)
              ELSE get_last_name(p.id)
            END AS identity,
            p.kills,
            p.deaths,
            build_human_time(p.time_secs, lang) AS gaming_time,
            CASE WHEN is_unicode = 1
              THEN p.stars_unicode
              ELSE p.stars_compat
            END AS stars,
            p.level,
            CASE WHEN lang = 'ru'
              THEN r.name_ru
              ELSE r.name_en
            END AS rank_name,
            r.kaomoji,
            p.lastseen_datetime,
            p.last_server_name
        FROM player p
        LEFT JOIN `rank` r ON p.level = r.level
        WHERE p.id = target_id;
    END IF;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `PlayerByIp` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` PROCEDURE `PlayerByIp`(
    IN ip VARCHAR(15),
    IN lang VARCHAR(5),
    IN is_unicode TINYINT UNSIGNED
)
    READS SQL DATA
BEGIN
    DECLARE target_id INT UNSIGNED DEFAULT NULL;
    SELECT p.id INTO target_id
    FROM player p
    JOIN player_ip pip ON p.id = pip.player_id
    WHERE pip.ip = INET_ATON(ip)
    ORDER BY pip.reg_datetime DESC, pip.id DESC
    LIMIT 1;

    IF target_id IS NOT NULL THEN
        SELECT
            (SELECT COUNT(*) + 1 FROM player p2 
             WHERE (p2.level, p2.time_secs, p2.id) > (p.level, p.time_secs, p.id)
            ) AS place,
            p.id,
            ip,
            p.kills,
            p.deaths,
            build_human_time(p.time_secs, lang) AS gaming_time,
            CASE WHEN is_unicode = 1
              THEN p.stars_unicode
              ELSE p.stars_compat
            END AS stars,
            p.level,
            CASE WHEN lang = 'ru'
              THEN r.name_ru
              ELSE r.name_en
            END AS rank_name,
            r.kaomoji,
            p.lastseen_datetime,
            p.last_server_name
        FROM player p
        LEFT JOIN `rank` r ON p.level = r.level
        WHERE p.id = target_id;
    END IF;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `PlayerByName` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` PROCEDURE `PlayerByName`(
    IN name VARCHAR(31),
    IN lang VARCHAR(5),
    IN is_unicode TINYINT UNSIGNED
)
    READS SQL DATA
BEGIN
    DECLARE target_id INT UNSIGNED DEFAULT NULL;
    SELECT p.id INTO target_id
    FROM player p
    JOIN player_name pn ON p.id = pn.player_id
    WHERE pn.name = name
    ORDER BY pn.reg_datetime DESC, pn.id DESC
    LIMIT 1;

    IF target_id IS NOT NULL THEN
        SELECT
            (SELECT COUNT(*) + 1 FROM player p2 
             WHERE (p2.level, p2.time_secs, p2.id) > (p.level, p.time_secs, p.id)
            ) AS place,
            p.id,
            name,
            p.kills,
            p.deaths,
            build_human_time(p.time_secs, lang) AS gaming_time,
            CASE WHEN is_unicode = 1
              THEN p.stars_unicode
              ELSE p.stars_compat
            END AS stars,
            p.level,
            CASE WHEN lang = 'ru'
              THEN r.name_ru
              ELSE r.name_en
            END AS rank_name,
            r.kaomoji,
            p.lastseen_datetime,
            p.last_server_name
        FROM player p
        LEFT JOIN `rank` r ON p.level = r.level
        WHERE p.id = target_id;
    END IF;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `PlayerBySteamId2` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` PROCEDURE `PlayerBySteamId2`(IN steamId2 varchar(22), IN lang varchar(5), IN is_unicode tinyint unsigned)
    READS SQL DATA
BEGIN
call PlayerBySteamId64(convert_steamid2_to_steamid64(steamId2), lang, is_unicode);
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `PlayerBySteamId64` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` PROCEDURE `PlayerBySteamId64`(
    IN steamId64 DECIMAL(17,0),
    IN lang VARCHAR(5),
    IN is_unicode TINYINT UNSIGNED
)
    READS SQL DATA
BEGIN
    DECLARE target_id INT UNSIGNED DEFAULT NULL;
    DECLARE target_steamid2 VARCHAR(20) DEFAULT NULL;
    SELECT p.id,
           ps.steamid2
    INTO   target_id,
           target_steamid2
    FROM player p
    JOIN player_steamid ps ON p.id = ps.player_id
    WHERE ps.steamid64 = steamId64
    ORDER BY ps.reg_datetime DESC, ps.id DESC
    LIMIT 1;

    IF target_id IS NOT NULL THEN
        SELECT
            (SELECT COUNT(*) + 1 FROM player p2 
             WHERE (p2.level, p2.time_secs, p2.id) > (p.level, p.time_secs, p.id)
            ) AS place,
            p.id,
            target_steamid2 steamid2,
            p.kills,
            p.deaths,
            build_human_time(p.time_secs, lang) AS gaming_time,
            CASE WHEN is_unicode = 1
              THEN p.stars_unicode
              ELSE p.stars_compat
            END AS stars,
            p.level,
            CASE WHEN lang = 'ru'
              THEN r.name_ru
              ELSE r.name_en
            END AS rank_name,
            r.kaomoji,
            p.lastseen_datetime,
            p.last_server_name
        FROM player p
        LEFT JOIN `rank` r ON p.level = r.level
        WHERE p.id = target_id;
    END IF;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `Top` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` PROCEDURE `Top`(
    IN display_type VARCHAR(7),               -- 'nick', 'ip', 'steamid'
    IN lang VARCHAR(5),
    IN poffset INT UNSIGNED,
    IN plimit INT UNSIGNED,
    IN is_unicode TINYINT UNSIGNED
)
    READS SQL DATA
BEGIN
/* Use TopCursor for higher poffset */
    SELECT
        ROW_NUMBER() OVER (ORDER BY t.level DESC, t.time_secs DESC, t.id DESC) + poffset AS `place`,
        t.id,
        t.identity,
        t.kills,
        t.deaths,
        t.time_secs,
        t.gaming_time,
        t.stars,
        t.level,
        t.rank_name,
        t.kaomoji
    FROM (
        SELECT
            p.id,
            CASE
              WHEN display_type = 'ip' THEN get_last_ip4(p.id)
              WHEN display_type = 'steamid' THEN get_last_steamid2(p.id)
              ELSE get_last_name(p.id)
            END AS identity,
            p.kills,
            p.deaths,
            p.time_secs,
            build_human_time(p.time_secs, lang) AS gaming_time,
            CASE WHEN is_unicode = 1
              THEN p.stars_unicode
              ELSE p.stars_compat
            END AS stars,
            p.level,
            CASE 
              WHEN lang = 'ru' THEN r.name_ru
              ELSE r.name_en
            END AS rank_name,
            r.kaomoji
        FROM player p
        INNER JOIN `rank` r ON r.level = p.level
        ORDER BY p.level DESC, p.time_secs DESC, p.id DESC
        LIMIT poffset, plimit
    ) t;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `TopCursor` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` PROCEDURE `TopCursor`(
    IN display_type VARCHAR(7), -- 'nick', 'ip', 'steamid'
    IN lang VARCHAR(5),
    IN last_level INT UNSIGNED, -- NULL для первой страницы
    IN last_time_secs INT UNSIGNED, -- NULL для первой страницы
    IN last_id INT UNSIGNED, -- NULL для первой страницы
/*
для 'next' - last_ заполнять данными с последней строки
для 'prev' - last_ заполнять данными с первой строки
*/
    IN cursor_direction VARCHAR(4), -- 'next' или 'prev'
    IN plimit INT UNSIGNED,
    IN is_unicode TINYINT UNSIGNED
)
    READS SQL DATA
BEGIN
    DECLARE v_limit INT DEFAULT plimit + 1;
    IF cursor_direction IS NULL OR cursor_direction NOT IN ('next','prev') THEN
        SET cursor_direction = 'next';
    END IF;
    
    IF last_level IS NULL THEN
        -- Первая страница
        SELECT
            (SELECT COUNT(*) + 1 FROM player p2 
             WHERE (p2.level, p2.time_secs, p2.id) > (t.level, t.time_secs, t.id)
            ) AS place,
            t.id,
            t.identity,
            t.kills,
            t.deaths,
            t.time_secs,
            t.gaming_time,
            t.stars,
            t.level,
            t.rank_name,
            t.kaomoji,
            (SELECT COUNT(*) > plimit
             FROM (
                   SELECT 1
                   FROM player p3
                   JOIN `rank` r3 ON r3.level = p3.level
                   ORDER BY p3.level DESC, p3.time_secs DESC, p3.id DESC
                   LIMIT v_limit
             ) AS _chk) AS has_more_next
        FROM (
            SELECT
                p.id,
                CASE
                  WHEN display_type = 'ip' THEN get_last_ip4(p.id)
                  WHEN display_type = 'steamid' THEN get_last_steamid2(p.id)
                  ELSE get_last_name(p.id)
                END AS identity,
                p.kills,
                p.deaths,
                p.time_secs,
                build_human_time(p.time_secs, lang) AS gaming_time,
                CASE WHEN is_unicode = 1
                  THEN p.stars_unicode
                  ELSE p.stars_compat
                END AS stars,
                p.level,
                CASE WHEN lang = 'ru'
                  THEN r.name_ru
                  ELSE r.name_en END
                AS rank_name,
                r.kaomoji
            FROM player p
            INNER JOIN `rank` r ON r.level = p.level
            ORDER BY p.level DESC, p.time_secs DESC, p.id DESC
            LIMIT plimit
        ) t;

    ELSEIF cursor_direction = 'next' THEN
        -- Следующая страница: ищем слабее последней строки
        SELECT
            (SELECT COUNT(*) + 1 FROM player p2 
             WHERE (p2.level, p2.time_secs, p2.id) > (t.level, t.time_secs, t.id)
            ) AS place,
            t.id,
            t.identity,
            t.kills,
            t.deaths,
            t.time_secs,
            t.gaming_time,
            t.stars,
            t.level,
            t.rank_name,
            t.kaomoji,
            (SELECT COUNT(*) FROM (
              SELECT 1
              FROM player p3
              JOIN `rank` r3 ON r3.level = p3.level
              WHERE (p3.level, p3.time_secs, p3.id) < (last_level, last_time_secs, last_id)
              ORDER BY p3.level DESC, p3.time_secs DESC, p3.id DESC
              LIMIT v_limit
            ) AS _chk) > plimit AS has_more_next
        FROM (
            SELECT
                p.id,
                CASE
                  WHEN display_type = 'ip' THEN get_last_ip4(p.id)
                  WHEN display_type = 'steamid' THEN get_last_steamid2(p.id)
                  ELSE get_last_name(p.id)
                END AS identity,
                p.kills,
                p.deaths,
                p.time_secs,
                build_human_time(p.time_secs, lang) AS gaming_time,
                CASE WHEN is_unicode = 1
                  THEN p.stars_unicode
                  ELSE p.stars_compat
                END AS stars,
                p.level,
                CASE WHEN lang = 'ru'
                  THEN r.name_ru
                  ELSE r.name_en
                END AS rank_name,
                r.kaomoji
            FROM player p
            INNER JOIN `rank` r ON r.level = p.level
            WHERE (p.level, p.time_secs, p.id) < (last_level, last_time_secs, last_id)
            ORDER BY p.level DESC, p.time_secs DESC, p.id DESC
            LIMIT plimit
        ) t
--         ORDER BY t.level DESC, t.time_secs DESC, t.id DESC
        ;
    ELSEIF cursor_direction = 'prev' THEN
        -- Предыдущая страница: ищем сильнее первой строки → обратная сортировка + реверс
        SELECT
            (SELECT COUNT(*) + 1 FROM player p2 
             WHERE (p2.level, p2.time_secs, p2.id) > (t.level, t.time_secs, t.id)
            ) AS place,
            t.id,
            t.identity,
            t.kills,
            t.deaths,
            t.time_secs,
            t.gaming_time,
            t.stars,
            t.level,
            t.rank_name,
            t.kaomoji,
            (SELECT COUNT(*) FROM (
                SELECT 1
                FROM player p3
                JOIN `rank` r3 ON r3.level = p3.level
                WHERE (p3.level, p3.time_secs, p3.id) > (last_level, last_time_secs, last_id)
                ORDER BY p3.level ASC, p3.time_secs ASC, p3.id ASC
                LIMIT v_limit
            ) AS _chk) > plimit AS has_more_prev
        FROM (
            SELECT
                p.id,
                CASE
                  WHEN display_type = 'ip' THEN get_last_ip4(p.id)
                  WHEN display_type = 'steamid' THEN get_last_steamid2(p.id)
                  ELSE get_last_name(p.id)
                END AS identity,
                p.kills,
                p.deaths,
                p.time_secs,
                build_human_time(p.time_secs, lang) AS gaming_time,
                CASE WHEN is_unicode = 1
                  THEN p.stars_unicode
                  ELSE p.stars_compat
                END AS stars,
                p.level,
                CASE WHEN lang = 'ru'
                  THEN r.name_ru
                  ELSE r.name_en
                END AS rank_name,
                r.kaomoji
            FROM player p
            INNER JOIN `rank` r ON r.level = p.level
            WHERE (p.level, p.time_secs, p.id) > (last_level, last_time_secs, last_id)
            ORDER BY p.level ASC, p.time_secs ASC, p.id ASC
            LIMIT plimit
        ) t
        ORDER BY t.level DESC, t.time_secs DESC, t.id DESC -- восстанавливаем порядок
        ;
    END IF;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-02-01 11:11:11
