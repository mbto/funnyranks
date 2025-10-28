package com.github.mbto.funnyranks.common.utils.geoip;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.record.City;
import com.maxmind.geoip2.record.Continent;
import com.maxmind.geoip2.record.Country;
import com.maxmind.geoip2.record.Subdivision;
import org.jooq.types.UInteger;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GeoIpFormatter {
    public static GeoInfo buildGeoInfoByIp(DatabaseReader dbReader,
                                           UInteger uniqueIp) throws Exception {
        InetAddress address = ipToInetAddress(uniqueIp.longValue());
        CityResponse cr = dbReader.city(address);

        Continent continent = cr.getContinent();
        Country country = cr.getCountry();
        City city = cr.getCity();
        List<Subdivision> subdivisions = cr.getSubdivisions();
        Subdivision sd1 = subdivisions.isEmpty() ? null : subdivisions.get(0);
        Subdivision sd2 = subdivisions.size() > 1 ? subdivisions.get(1) : null;

        Long geonameId = city.getGeoNameId();
        String continentRu = safeGet(continent.getNames(), "ru");
        String continentEn = safeGet(continent.getNames(), "en");
        String countryIso = country.getIsoCode();
        String countryEmoji = getEmojiByCountryIsoCode(countryIso);
        String countryRu = safeGet(country.getNames(), "ru");
        if(countryRu != null) {
            countryRu = countryEmoji + " " + countryRu;
        }
        String countryEn = safeGet(country.getNames(), "en");
        if(countryEn != null) {
            countryEn = countryEmoji + " " + countryEn;
        }
        String cityRu = safeGet(city.getNames(), "ru");
        String cityEn = safeGet(city.getNames(), "en");
        String sd1Ru = sd1 != null ? safeGet(sd1.getNames(), "ru") : null;
        String sd1En = sd1 != null ? safeGet(sd1.getNames(), "en") : null;
        String sd2Ru = sd2 != null ? safeGet(sd2.getNames(), "ru") : null;
        String sd2En = sd2 != null ? safeGet(sd2.getNames(), "en") : null;

        String locationRu = buildLocation(continentRu, countryRu, sd1Ru, sd2Ru, cityRu);
        String locationEn = buildLocation(continentEn, countryEn, sd1En, sd2En, cityEn);
        String locationMix = buildLocation(
                coalesce(continentRu, continentEn),
                coalesce(countryRu, countryEn),
                coalesce(sd1Ru, sd1En),
                coalesce(sd2Ru, sd2En),
                coalesce(cityRu, cityEn));
        return new GeoInfo(geonameId,
                countryEmoji,
                locationRu,
                locationEn,
                locationMix
        );
    }

    private static String safeGet(java.util.Map<String, String> names, String lang) {
        if (names == null) return null;
        String val = names.get(lang);
        return (val != null && !val.isEmpty()) ? val : null;
    }

    private static String coalesce(String... values) {
        for (String v : values) {
            if (v != null && !v.isEmpty()) {
                return v;
            }
        }
        return null;
    }

    private static String buildLocation(String continent, String country, String sd1, String sd2, String city) {
        List<String> parts = new ArrayList<>();
        if (continent != null && !continent.isEmpty()) {
            parts.add(continent);
        }
        if (country != null && !country.isEmpty()) {
            parts.add(country);
        }
        if (sd1 != null && !sd1.isEmpty() &&
            !equalsIgnoreCaseAny(sd1, continent, country, city)) {
            parts.add(sd1);
        }
        if (sd2 != null && !sd2.isEmpty() &&
            !equalsIgnoreCaseAny(sd2, continent, country, city, sd1)) {
            parts.add(sd2);
        }
        if (city != null && !city.isEmpty() &&
            !equalsIgnoreCaseAny(city, continent, country)) {
            parts.add(city);
        }
        return parts.stream()
                .filter(s -> s != null && !s.isEmpty())
                .collect(Collectors.joining(" - "));
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private static boolean equalsIgnoreCaseAny(String value, String... others) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        for (String other : others) {
            if (other != null && !other.isEmpty() && value.equalsIgnoreCase(other)) {
                return true;
            }
        }
        return false;
    }

    public static InetAddress ipToInetAddress(long ip) throws UnknownHostException {
        byte[] ipBytes = {
                (byte) ((ip >>> 24) & 0xFF),
                (byte) ((ip >>> 16) & 0xFF),
                (byte) ((ip >>>  8) & 0xFF),
                (byte) ((ip       ) & 0xFF)
        };
        return Inet4Address.getByAddress(ipBytes);
    }

    private static final Map<String, String> emojiByCountryIsoCode;

	public static String getEmojiByCountryIsoCode(String countryIsoCode) {
		return emojiByCountryIsoCode.getOrDefault(countryIsoCode, "🏳️");
	}

    static {
        final String[][] countryIsoCodeWithEmoji = new String[][] {
                {"AD", "🇦🇩"},
                {"AE", "🇦🇪"},
                {"AF", "🇦🇫"},
                {"AG", "🇦🇬"},
                {"AI", "🇦🇮"},
                {"AL", "🇦🇱"},
                {"AM", "🇦🇲"},
                {"AO", "🇦🇴"},
                {"AQ", "🇦🇶"},
                {"AR", "🇦🇷"},
                {"AS", "🇦🇸"},
                {"AT", "🇦🇹"},
                {"AU", "🇦🇺"},
                {"AW", "🇦🇼"},
                {"AX", "🇦🇽"},
                {"AZ", "🇦🇿"},
                {"BA", "🇧🇦"},
                {"BB", "🇧🇧"},
                {"BD", "🇧🇩"},
                {"BE", "🇧🇪"},
                {"BF", "🇧🇫"},
                {"BG", "🇧🇬"},
                {"BH", "🇧🇭"},
                {"BI", "🇧🇮"},
                {"BJ", "🇧🇯"},
                {"BL", "🇧🇱"},
                {"BM", "🇧🇲"},
                {"BN", "🇧🇳"},
                {"BO", "🇧🇴"},
                {"BQ", "🇧🇶"},
                {"BR", "🇧🇷"},
                {"BS", "🇧🇸"},
                {"BT", "🇧🇹"},
                {"BV", "🇧🇻"},
                {"BW", "🇧🇼"},
                {"BY", "🇧🇾"},
                {"BZ", "🇧🇿"},
                {"CA", "🇨🇦"},
                {"CC", "🇨🇨"},
                {"CD", "🇨🇩"},
                {"CF", "🇨🇫"},
                {"CG", "🇨🇬"},
                {"CH", "🇨🇭"},
                {"CI", "🇨🇮"},
                {"CK", "🇨🇰"},
                {"CL", "🇨🇱"},
                {"CM", "🇨🇲"},
                {"CN", "🇨🇳"},
                {"CO", "🇨🇴"},
                {"CR", "🇨🇷"},
                {"CU", "🇨🇺"},
                {"CV", "🇨🇻"},
                {"CW", "🇨🇼"},
                {"CX", "🇨🇽"},
                {"CY", "🇨🇾"},
                {"CZ", "🇨🇿"},
                {"DE", "🇩🇪"},
                {"DJ", "🇩🇯"},
                {"DK", "🇩🇰"},
                {"DM", "🇩🇲"},
                {"DO", "🇩🇴"},
                {"DZ", "🇩🇿"},
                {"EC", "🇪🇨"},
                {"EE", "🇪🇪"},
                {"EG", "🇪🇬"},
                {"EH", "🇪🇭"},
                {"ER", "🇪🇷"},
                {"ES", "🇪🇸"},
                {"ET", "🇪🇹"},
                {"FI", "🇫🇮"},
                {"FJ", "🇫🇯"},
                {"FK", "🇫🇰"},
                {"FM", "🇫🇲"},
                {"FO", "🇫🇴"},
                {"FR", "🇫🇷"},
                {"GA", "🇬🇦"},
                {"GB", "🇬🇧"},
                {"GD", "🇬🇩"},
                {"GE", "🇬🇪"},
                {"GF", "🇬🇫"},
                {"GG", "🇬🇬"},
                {"GH", "🇬🇭"},
                {"GI", "🇬🇮"},
                {"GL", "🇬🇱"},
                {"GM", "🇬🇲"},
                {"GN", "🇬🇳"},
                {"GP", "🇬🇵"},
                {"GQ", "🇬🇶"},
                {"GR", "🇬🇷"},
                {"GS", "🇬🇸"},
                {"GT", "🇬🇹"},
                {"GU", "🇬🇺"},
                {"GW", "🇬🇼"},
                {"GY", "🇬🇾"},
                {"HK", "🇭🇰"},
                {"HM", "🇭🇲"},
                {"HN", "🇭🇳"},
                {"HR", "🇭🇷"},
                {"HT", "🇭🇹"},
                {"HU", "🇭🇺"},
                {"ID", "🇮🇩"},
                {"IE", "🇮🇪"},
                {"IL", "🇮🇱"},
                {"IM", "🇮🇲"},
                {"IN", "🇮🇳"},
                {"IO", "🇮🇴"},
                {"IQ", "🇮🇶"},
                {"IR", "🇮🇷"},
                {"IS", "🇮🇸"},
                {"IT", "🇮🇹"},
                {"JE", "🇯🇪"},
                {"JM", "🇯🇲"},
                {"JO", "🇯🇴"},
                {"JP", "🇯🇵"},
                {"KE", "🇰🇪"},
                {"KG", "🇰🇬"},
                {"KH", "🇰🇭"},
                {"KI", "🇰🇮"},
                {"KM", "🇰🇲"},
                {"KN", "🇰🇳"},
                {"KP", "🇰🇵"},
                {"KR", "🇰🇷"},
                {"KW", "🇰🇼"},
                {"KY", "🇰🇾"},
                {"KZ", "🇰🇿"},
                {"LA", "🇱🇦"},
                {"LB", "🇱🇧"},
                {"LC", "🇱🇨"},
                {"LI", "🇱🇮"},
                {"LK", "🇱🇰"},
                {"LR", "🇱🇷"},
                {"LS", "🇱🇸"},
                {"LT", "🇱🇹"},
                {"LU", "🇱🇺"},
                {"LV", "🇱🇻"},
                {"LY", "🇱🇾"},
                {"MA", "🇲🇦"},
                {"MC", "🇲🇨"},
                {"MD", "🇲🇩"},
                {"ME", "🇲🇪"},
                {"MF", "🇲🇫"},
                {"MG", "🇲🇬"},
                {"MH", "🇲🇭"},
                {"MK", "🇲🇰"},
                {"ML", "🇲🇱"},
                {"MM", "🇲🇲"},
                {"MN", "🇲🇳"},
                {"MO", "🇲🇴"},
                {"MP", "🇲🇵"},
                {"MQ", "🇲🇶"},
                {"MR", "🇲🇷"},
                {"MS", "🇲🇸"},
                {"MT", "🇲🇹"},
                {"MU", "🇲🇺"},
                {"MV", "🇲🇻"},
                {"MW", "🇲🇼"},
                {"MX", "🇲🇽"},
                {"MY", "🇲🇾"},
                {"MZ", "🇲🇿"},
                {"NA", "🇳🇦"},
                {"NC", "🇳🇨"},
                {"NE", "🇳🇪"},
                {"NF", "🇳🇫"},
                {"NG", "🇳🇬"},
                {"NI", "🇳🇮"},
                {"NL", "🇳🇱"},
                {"NO", "🇳🇴"},
                {"NP", "🇳🇵"},
                {"NR", "🇳🇷"},
                {"NU", "🇳🇺"},
                {"NZ", "🇳🇿"},
                {"OM", "🇴🇲"},
                {"PA", "🇵🇦"},
                {"PE", "🇵🇪"},
                {"PF", "🇵🇫"},
                {"PG", "🇵🇬"},
                {"PH", "🇵🇭"},
                {"PK", "🇵🇰"},
                {"PL", "🇵🇱"},
                {"PM", "🇵🇲"},
                {"PN", "🇵🇳"},
                {"PR", "🇵🇷"},
                {"PS", "🇵🇸"},
                {"PT", "🇵🇹"},
                {"PW", "🇵🇼"},
                {"PY", "🇵🇾"},
                {"QA", "🇶🇦"},
                {"RE", "🇷🇪"},
                {"RO", "🇷🇴"},
                {"RS", "🇷🇸"},
                {"RU", "🇷🇺"},
                {"RW", "🇷🇼"},
                {"SA", "🇸🇦"},
                {"SB", "🇸🇧"},
                {"SC", "🇸🇨"},
                {"SD", "🇸🇩"},
                {"SE", "🇸🇪"},
                {"SG", "🇸🇬"},
                {"SH", "🇸🇭"},
                {"SI", "🇸🇮"},
                {"SJ", "🇸🇯"},
                {"SK", "🇸🇰"},
                {"SL", "🇸🇱"},
                {"SM", "🇸🇲"},
                {"SN", "🇸🇳"},
                {"SO", "🇸🇴"},
                {"SR", "🇸🇷"},
                {"SS", "🇸🇸"},
                {"ST", "🇸🇹"},
                {"SV", "🇸🇻"},
                {"SX", "🇸🇽"},
                {"SY", "🇸🇾"},
                {"SZ", "🇸🇿"},
                {"TC", "🇹🇨"},
                {"TD", "🇹🇩"},
                {"TF", "🇹🇫"},
                {"TG", "🇹🇬"},
                {"TH", "🇹🇭"},
                {"TJ", "🇹🇯"},
                {"TK", "🇹🇰"},
                {"TL", "🇹🇱"},
                {"TM", "🇹🇲"},
                {"TN", "🇹🇳"},
                {"TO", "🇹🇴"},
                {"TR", "🇹🇷"},
                {"TT", "🇹🇹"},
                {"TV", "🇹🇻"},
                {"TW", "🇹🇼"},
                {"TZ", "🇹🇿"},
                {"UA", "🇺🇦"},
                {"UG", "🇺🇬"},
                {"UM", "🇺🇲"},
                {"US", "🇺🇸"},
                {"UY", "🇺🇾"},
                {"UZ", "🇺🇿"},
                {"VA", "🇻🇦"},
                {"VC", "🇻🇨"},
                {"VE", "🇻🇪"},
                {"VG", "🇻🇬"},
                {"VI", "🇻🇮"},
                {"VN", "🇻🇳"},
                {"VU", "🇻🇺"},
                {"WF", "🇼🇫"},
                {"WS", "🇼🇸"},
                {"XK", "🇽🇰"},
                {"YE", "🇾🇪"},
                {"YT", "🇾🇹"},
                {"ZA", "🇿🇦"},
                {"ZM", "🇿🇲"},
                {"ZW", "🇿🇼"},
        };
        emojiByCountryIsoCode = Stream.of(countryIsoCodeWithEmoji)
            .collect(Collectors
                .toUnmodifiableMap(
                    strings -> strings[0],
                    strings -> strings[1]
                )
            );
    }
}