-- MySQL dump 10.13  Distrib 8.0.21, for Win64 (x86_64)
--
-- Host: localhost    Database: funnyranks_stats
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
-- Table structure for table `history`
--

DROP TABLE IF EXISTS `history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `history` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `player_id` int unsigned NOT NULL,
  `old_rank_id` int unsigned DEFAULT NULL,
  `new_rank_id` int unsigned DEFAULT NULL,
  `reg_datetime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `history_player_id_idx` (`player_id`),
  KEY `history_old_rank_id_idx` (`old_rank_id`),
  KEY `history_new_rank_id_idx` (`new_rank_id`),
  CONSTRAINT `history_new_rank_id_fk` FOREIGN KEY (`new_rank_id`) REFERENCES `rank` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `history_old_rank_id_fk` FOREIGN KEY (`old_rank_id`) REFERENCES `rank` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `history_player_id_fk` FOREIGN KEY (`player_id`) REFERENCES `player` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=COMPRESSED KEY_BLOCK_SIZE=2;
/*!40101 SET character_set_client = @saved_cs_client */;

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
  `rank_id` int unsigned DEFAULT NULL,
  `lastseen_datetime` datetime DEFAULT NULL,
  `last_server_name` varchar(31) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `player_rank_id_idx` (`rank_id`),
  CONSTRAINT `player_rank_id_fk` FOREIGN KEY (`rank_id`) REFERENCES `rank` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
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
/*!50032 DROP TRIGGER IF EXISTS player_BEFORE_INSERT */;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`%`*/ /*!50003 TRIGGER `player_BEFORE_INSERT` BEFORE INSERT ON `player` FOR EACH ROW BEGIN
	set NEW.rank_id = calculate_rank_id(NEW.kills, NEW.deaths, NEW.time_secs);
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
/*!50032 DROP TRIGGER IF EXISTS player_BEFORE_UPDATE */;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`%`*/ /*!50003 TRIGGER `player_BEFORE_UPDATE` BEFORE UPDATE ON `player` FOR EACH ROW BEGIN
	IF (!(OLD.kills <=> NEW.kills)
     or !(OLD.deaths <=> NEW.deaths)
     or !(OLD.time_secs <=> NEW.time_secs)) THEN
		set NEW.rank_id = calculate_rank_id(NEW.kills, NEW.deaths, NEW.time_secs);
        
		if (!(OLD.rank_id <=> NEW.rank_id)) then
			insert into history (player_id, old_rank_id, new_rank_id)
			values (NEW.id, OLD.rank_id, NEW.rank_id);
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
  `ip4` varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci GENERATED ALWAYS AS (inet_ntoa(`ip`)) VIRTUAL COMMENT 'Auto-generated IP format v4 - AAA.BBB.CCC.DDD',
  `country_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `country_emoji` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `reg_datetime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `player_ip_player_id_idx` (`player_id`),
  KEY `player_ip_ip_idx` (`ip`),
  KEY `player_ip_ip4_idx` (`ip4`),
  CONSTRAINT `player_ip_player_id_fk` FOREIGN KEY (`player_id`) REFERENCES `player` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
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
/*!50032 DROP TRIGGER IF EXISTS player_ip_BEFORE_INSERT */;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`%`*/ /*!50003 TRIGGER `player_ip_BEFORE_INSERT` BEFORE INSERT ON `player_ip` FOR EACH ROW BEGIN
	declare error_msg text;
	declare existed_id int unsigned;
    
    select id into existed_id from `player_ip` 
		where player_id = NEW.player_id 
        and ip = NEW.ip limit 1;
    
	if(existed_id is not null) then
		set error_msg = concat('Unable to insert ip=', NEW.ip, ' to player_ip, due for player_id=', NEW.player_id, ' already existed id=', existed_id);
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
/*!50032 DROP TRIGGER IF EXISTS player_ip_BEFORE_UPDATE */;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`%`*/ /*!50003 TRIGGER `player_ip_BEFORE_UPDATE` BEFORE UPDATE ON `player_ip` FOR EACH ROW BEGIN
	declare error_msg text;
	declare existed_id int unsigned;
    
    select id into existed_id from `player_ip` 
		where player_id = NEW.player_id 
        and ip = NEW.ip 
        and id != OLD.id limit 1;
    
	if(existed_id is not null) then
		set error_msg = concat('Unable to update player_ip from ', OLD.ip, ' to ', NEW.ip, ', due for player_id=', NEW.player_id, ' already existed id=', existed_id);
		SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = error_msg;
	end if;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;

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
  KEY `player_name_player_id_idx` (`player_id`),
  KEY `player_name_name_idx` (`name`),
  CONSTRAINT `player_name_player_id_fk` FOREIGN KEY (`player_id`) REFERENCES `player` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
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
/*!50032 DROP TRIGGER IF EXISTS player_name_BEFORE_INSERT */;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`%`*/ /*!50003 TRIGGER `player_name_BEFORE_INSERT` BEFORE INSERT ON `player_name` FOR EACH ROW BEGIN
	declare error_msg text;
	declare existed_id int unsigned;
    
    select id into existed_id from `player_name` 
		where player_id = NEW.player_id 
        and name = NEW.name limit 1;
    
	if(existed_id is not null) then
		set error_msg = concat('Unable to insert name=', NEW.name, ' to player_name, due for player_id=', NEW.player_id, ' already existed id=', existed_id);
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
/*!50032 DROP TRIGGER IF EXISTS player_name_BEFORE_UPDATE */;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`%`*/ /*!50003 TRIGGER `player_name_BEFORE_UPDATE` BEFORE UPDATE ON `player_name` FOR EACH ROW BEGIN
	declare error_msg text;
	declare existed_id int unsigned;
    
    select id into existed_id from `player_name` 
		where player_id = NEW.player_id 
        and name = NEW.name 
        and id != OLD.id limit 1;
    
	if(existed_id is not null) then
		set error_msg = concat('Unable to update player_name from ', OLD.name, ' to ', NEW.name, ', due for player_id=', NEW.player_id, ' already existed id=', existed_id);
		SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = error_msg;
	end if;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;

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
  `steamid2` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci GENERATED ALWAYS AS (concat((`steamid64` % 2),_utf8mb4':',truncate((((`steamid64` - 76561197960265728) - (`steamid64` % 2)) / 2),0))) VIRTUAL COMMENT 'Auto-generated SteamID format v2 - STEAM_0:X:YYYYYYYYYY - https://developer.valvesoftware.com/wiki/SteamID',
  `steamid3` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci GENERATED ALWAYS AS (concat(1,_utf8mb4':',(`steamid64` - 76561197960265728))) VIRTUAL COMMENT 'Auto-generated SteamID format v3 - [U:X:YYYYYYYYYY] - https://developer.valvesoftware.com/wiki/SteamID',
  `reg_datetime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `player_steamid_player_id_idx` (`player_id`),
  KEY `player_steamid_steamid64_idx` (`steamid64`),
  KEY `player_steamid_steamid2_idx` (`steamid2`),
  KEY `player_steamid_steamid3_idx` (`steamid3`),
  CONSTRAINT `player_steamid_player_id_fk` FOREIGN KEY (`player_id`) REFERENCES `player` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
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
/*!50032 DROP TRIGGER IF EXISTS player_steamid_BEFORE_INSERT */;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`%`*/ /*!50003 TRIGGER `player_steamid_BEFORE_INSERT` BEFORE INSERT ON `player_steamid` FOR EACH ROW BEGIN
	declare error_msg text;
	declare existed_id int unsigned;
    
    if(NEW.steamid64 < 76561197960265729 or NEW.steamid64 > 76561202255233023) then
		set error_msg = concat('Invalid steamid64=', NEW.steamid64);
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = error_msg;
		end if;
    
    select id into existed_id from `player_steamid` 
		where player_id = NEW.player_id 
        and steamid64 = NEW.steamid64 limit 1;
    
		if(existed_id is not null) then
			set error_msg = concat('Unable to insert steamid64=', NEW.steamid64, ' to player_steamid, due for player_id=', NEW.player_id, ' already existed id=', existed_id);
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
/*!50032 DROP TRIGGER IF EXISTS player_steamid_BEFORE_UPDATE */;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`%`*/ /*!50003 TRIGGER `player_steamid_BEFORE_UPDATE` BEFORE UPDATE ON `player_steamid` FOR EACH ROW BEGIN
	declare error_msg text;
	declare existed_id int unsigned;
    
    if(NEW.steamid64 < 76561197960265729 or NEW.steamid64 > 76561202255233023) then
		set error_msg = concat('Invalid steamid64=', NEW.steamid64);
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = error_msg;
		end if;
    
    select id into existed_id from `player_steamid` 
		where player_id = NEW.player_id 
        and steamid64 = NEW.steamid64
        and id != OLD.id limit 1;
    
	if(existed_id is not null) then
		set error_msg = concat('Unable to update player_steamid from ', OLD.steamid64, ' to ', NEW.steamid64, ', due for player_id=', NEW.player_id, ' already existed id=', existed_id);
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
INSERT INTO `rank` VALUES (1,1,'¯\\_(ツ)_/¯','Сынок','Son'),(2,2,'¯\\_(ツ)_/¯','Тюфяк','Mattress'),(3,3,'¯\\_(ツ)_/¯','Овощ','Vegetable'),(4,4,'¯\\_(ツ)_/¯','Кабан','Boar'),(5,5,'ˁ°ᴥ°ˀ','Силач','Strongman'),(6,6,'ˁ°ᴥ°ˀ','Шароеб','Spray shooter'),(7,7,'ˁ°ᴥ°ˀ','Пацан','Kid'),(8,8,'ˁ°ᴥ°ˀ','Смертник','Bomber'),(9,9,'(°‿°)','Везунчик','Lucky'),(10,10,'(°‿°)','Жульбан','Zhulban'),(11,11,'(°‿°)','Гопник','Chav'),(12,12,'(°‿°)','Кэмпер','Camper'),(13,13,'ᕙ(°ʖ°)ᕗ','Помощник','Assistant'),(14,14,'ᕙ(°ʖ°)ᕗ','Вуйко','Vuiko'),(15,15,'ᕙ(°ʖ°)ᕗ','Донышко','Bottom'),(16,16,'ᕙ(°ʖ°)ᕗ','Профан','Profane'),(17,17,'ᕦ(°_°)ᕤ','Титушка','Titushka'),(18,18,'ᕦ(°_°)ᕤ','Боцман','Boatswain'),(19,19,'ᕦ(°_°)ᕤ','Школьник','Schoolboy'),(20,20,'ᕦ(°_°)ᕤ','Мусор','Rubbish'),(21,21,'龴ↀ‿ↀ龴','Отбой','Hang up'),(22,22,'龴ↀ‿ↀ龴','ПТУ-шник','Vocational school'),(23,23,'龴ↀ‿ↀ龴','Зек','Snakes'),(24,24,'龴ↀ‿ↀ龴','Бывалый','Experienced'),(25,25,'(ಥ﹏ಥ)','Прораб','Foreman'),(26,26,'(ಥ﹏ಥ)','Жестянщик','Tinsmith'),(27,27,'(ಥ﹏ಥ)','Пахан','Pahan'),(28,28,'(ಥ﹏ಥ)','Директор','Director'),(29,29,'(ง°ل͜°)ง','Гастролер','Guest performer'),(30,30,'(ง°ل͜°)ง','Мордоворот','Mordovorot'),(31,31,'(ง°ل͜°)ง','Геймер','Gamer'),(32,32,'(ง°ل͜°)ง','Отважный','Brave'),(33,33,'(づ° ³°)づ','Убийца','Killer'),(34,34,'(づ° ³°)づ','Халявщик','Freeloader'),(35,35,'(づ° ³°)づ','Псих','Crazy'),(36,36,'(づ° ³°)づ','Йовбак','Yowback'),(37,37,'(ﾉ°ヮ°)ﾉ*:･ﾟ✧','Громила','Brute'),(38,38,'(ﾉ°ヮ°)ﾉ*:･ﾟ✧','Мужик','Man'),(39,39,'(ﾉ°ヮ°)ﾉ*:･ﾟ✧','Дезертир','Deserter'),(40,40,'(ﾉ°ヮ°)ﾉ*:･ﾟ✧','Боец','Fighter'),(41,41,'( ° ͜ʖ °)','Софт','Cheater'),(42,42,'( ° ͜ʖ °)','Громила-здоровяк','Big brute'),(43,43,'( ° ͜ʖ °)','Партизан','Partisan'),(44,44,'( ° ͜ʖ °)','Сенсей','Sensei'),(45,45,'t(ಠ益ಠt)','Рыцарь','Knight'),(46,46,'t(ಠ益ಠt)','Спецназовец','Spetsnaz'),(47,47,'t(ಠ益ಠt)','Тащит всю команду','Drags the whole team'),(48,48,'t(ಠ益ಠt)','Олдфаг','Oldfag'),(49,49,'(ノಠ益ಠ)ノ彡','Каратель','The Punisher'),(50,50,'(ノಠ益ಠ)ノ彡','Здоровяк','Big man'),(51,51,'(ノಠ益ಠ)ノ彡','Аим','Aim'),(52,52,'(ノಠ益ಠ)ノ彡','Фраер','Fraer'),(53,53,'ლ(ಠ益ಠლ)','Штурмовой','Assault'),(54,54,'ლ(ಠ益ಠლ)','Boss','Boss'),(55,55,'（︶︿︶）','Super Old School','Super Old School'),(56,56,'（︶︿︶）','Непобедимый','Invincible');
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
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
/*!50032 DROP TRIGGER IF EXISTS rank_AFTER_INSERT */;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`%`*/ /*!50003 TRIGGER `rank_AFTER_INSERT` AFTER INSERT ON `rank` FOR EACH ROW BEGIN
	update player p set p.rank_id = calculate_rank_id(p.kills, p.deaths, p.time_secs);
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
/*!50032 DROP TRIGGER IF EXISTS rank_AFTER_UPDATE */;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`%`*/ /*!50003 TRIGGER `rank_AFTER_UPDATE` AFTER UPDATE ON `rank` FOR EACH ROW BEGIN
	if(!(OLD.level <=> NEW.level)) then
		update player p set p.rank_id = calculate_rank_id(p.kills, p.deaths, p.time_secs);
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
/*!50032 DROP TRIGGER IF EXISTS rank_AFTER_DELETE */;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`%`*/ /*!50003 TRIGGER `rank_AFTER_DELETE` AFTER DELETE ON `rank` FOR EACH ROW BEGIN
	update player p set p.rank_id = calculate_rank_id(p.kills, p.deaths, p.time_secs);
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;

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
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
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
/*!50003 DROP FUNCTION IF EXISTS `build_stars` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` FUNCTION `build_stars`(level int unsigned, ranks_total int unsigned) RETURNS varchar(6) CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci
    DETERMINISTIC
BEGIN
	declare black_stars int unsigned default greatest(1, truncate(level * 6 / ranks_total, 0));
	declare white_stars int unsigned default 6 - black_stars;
	
-- 	return concat(repeat("★", black_stars), repeat("☆", white_stars)); // HLDS 90 not supported stars characters in hud/chat
	return concat(repeat("彡", black_stars), repeat("ノ", white_stars));
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP FUNCTION IF EXISTS `calculate_rank_id` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` FUNCTION `calculate_rank_id`(kills int unsigned, deaths int unsigned, time_secs int unsigned) RETURNS int unsigned
    READS SQL DATA
    DETERMINISTIC
BEGIN
    declare ranks_count int unsigned default (select count(*) from `rank`);
    
    declare hero_days int unsigned default 30;
    declare hero_kills int unsigned default 133/*frags per day*/ * hero_days;
    declare hero_skill int unsigned default calculate_skill(hero_kills, hero_kills * 0.4/* 40% */ );
    declare hero_time int unsigned default (hero_days/*days*/ * 24/*hours*/ * 60/*mins*/ * 60/*secs*/ );
    
    declare skill double default calculate_skill(kills, deaths);
    
    declare kills_pos int unsigned default greatest(1, least(ranks_count, ceil((skill * ranks_count) / hero_skill)));
    declare time_secs_pos int unsigned default greatest(1, least(ranks_count, floor((time_secs * ranks_count) / hero_time)));
	declare rank_num int unsigned default floor((kills_pos + time_secs_pos) / 2);
    
    declare new_rank_id int unsigned;
    
	if ranks_count > 0 then
		with cte as (select (row_number() over()) as num, id from `rank` order by level asc)
			select id from cte where num = rank_num into new_rank_id;
            
        return ifnull(new_rank_id, (select id from `rank` order by level asc limit 1));
	end if;
    
    return null;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP FUNCTION IF EXISTS `calculate_skill` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` FUNCTION `calculate_skill`(kills bigint, deaths bigint) RETURNS int
    DETERMINISTIC
BEGIN
	declare weight double default ((kills - deaths) / (kills / (kills + deaths)));
	declare killsWeight double default weight / 100.0;
	return round(100 * ((kills / (kills + deaths)) * killsWeight), 0);
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
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` FUNCTION `declension`(value int, opt1 varchar(32), opt2 varchar(32), opt3 varchar(32)) RETURNS varchar(32) CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci
    DETERMINISTIC
BEGIN
	declare n int unsigned default abs(value);
	if(n > 10 and n < 20) then return opt3; end if;
	if((n mod 10) > 1 and (n mod 10) < 5) then return opt2; end if;
	if((n mod 10) = 1) then return opt1; end if;
	return opt3;
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
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` PROCEDURE `PlayerByIp`(ip int unsigned, ranks_total int unsigned, lang varchar(5))
BEGIN
select `player`.*,
	`build_human_time`(`player`.`time_secs`, lang) AS `gaming_time`,
    (CASE WHEN lang = 'ru' THEN `rank`.`name_ru`
		  ELSE `rank`.`name_en`
    END) AS `rank_name`,
    `build_stars`(`rank`.`level`, ranks_total) AS `stars`,
		`rank`.`kaomoji` as kaomoji
from `player`
join `player_ip` on `player`.`id` = `player_ip`.`player_id`
left join `rank` ON `player`.`rank_id` = `rank`.`id`
where `player_ip`.`ip` = ip
order by `player_ip`.`reg_datetime` desc
limit 1
;
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
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` PROCEDURE `PlayerByName`(name varchar(31), ranks_total int unsigned, lang varchar(5))
BEGIN
select `player`.*,
	`build_human_time`(`player`.`time_secs`, lang) AS `gaming_time`,
    (CASE WHEN lang = 'ru' THEN `rank`.`name_ru`
		  ELSE `rank`.`name_en`
    END) AS `rank_name`,
    `build_stars`(`rank`.`level`, ranks_total) AS `stars`,
		`rank`.`kaomoji` as kaomoji
from `player`
join `player_name` on `player`.`id` = `player_name`.`player_id`
left join `rank` ON `player`.`rank_id` = `rank`.`id`
where `player_name`.`name` = name
order by `player_name`.`reg_datetime` desc
limit 1
;
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
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` PROCEDURE `PlayerBySteamId2`(steamId2 varchar(20), ranks_total int unsigned, lang varchar(5))
BEGIN
call PlayerBySteamId64(cast(SUBSTR(steamId2,1,1) as DECIMAL) + ((cast(SUBSTR(steamId2,3) as DECIMAL) * 2 + 76561197960265728)), ranks_total, lang);
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
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` PROCEDURE `PlayerBySteamId64`(steamId64 DECIMAL(17,0), ranks_total int unsigned, lang varchar(5))
BEGIN
select `player`.*,
	`build_human_time`(`player`.`time_secs`, lang) AS `gaming_time`,
    (CASE WHEN lang = 'ru' THEN `rank`.`name_ru`
		  ELSE `rank`.`name_en`
    END) AS `rank_name`,
    `build_stars`(`rank`.`level`, ranks_total) AS `stars`,
		`rank`.`kaomoji` as kaomoji
from `player`
join `player_steamid` on `player`.`id` = `player_steamid`.`player_id`
left join `rank` ON `player`.`rank_id` = `rank`.`id`
where `player_steamid`.`steamid64` = steamId64
order by `player_steamid`.`reg_datetime` desc
limit 1
;
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

-- Dump completed on 2021-08-01 00:00:00
