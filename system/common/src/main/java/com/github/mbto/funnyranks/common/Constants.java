package com.github.mbto.funnyranks.common;

import com.github.mbto.funnyranks.common.dto.session.Storage;
import org.springframework.util.LinkedCaseInsensitiveMap;

import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public interface Constants {
    DateTimeFormatter YYYYMMDD_HHMMSS_PATTERN = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    DateTimeFormatter MMDDYYYY_HHMMSS_PATTERN = DateTimeFormatter.ofPattern("MM/dd/yyyy - HH:mm:ss");
    Pattern IPADDRESS_PATTERN = Pattern.compile("(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)");
//    Pattern IPADDRESS_PORT_PATTERN = Pattern.compile("^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?):(6553[0-5]|655[0-2][0-9]|65[0-4][0-9][0-9]|6[0-4][0-9][0-9][0-9][0-9]|[1-5](\\d){4}|[1-9](\\d){0,3})$");
    Pattern STEAMID2_PATTERN = Pattern.compile("^STEAM_[0-1]:([0-1]):([0-9]{1,10})$");
//    Pattern STEAMID3_PATTERN = Pattern.compile("^\\[U:([0-1]):([0-9])+\\]+$");
    Pattern MYSQL_NAMING_PATTERN = Pattern.compile("^[^\\\\/?%*:|\"<>.]{1,64}$");
    // from org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder.BCRYPT_PATTERN
    Pattern BCRYPT_PATTERN = Pattern.compile("\\A\\$2a?\\$\\d\\d\\$[./0-9A-Za-z]{53}");
    int SERVER_DATA_MESSAGES_MAX = 100;

    Supplier<Map<String, Storage>> storageByNameContainerSupplier = LinkedCaseInsensitiveMap::new;

    /* to prevent dublicate array creating at .values() */
//    ProjectDatabaseServerTimezone[] PROJECT_DATABASE_SERVER_TIMEZONES = ProjectDatabaseServerTimezone.values();
}