/*
 * This file is generated by jOOQ.
 */
package com.github.mbto.funnyranks.common.model.funnyranks_stats;


import com.github.mbto.funnyranks.common.model.funnyranks_stats.routines.BuildHumanTime;
import com.github.mbto.funnyranks.common.model.funnyranks_stats.routines.BuildStars;
import com.github.mbto.funnyranks.common.model.funnyranks_stats.routines.BuildStars2;
import com.github.mbto.funnyranks.common.model.funnyranks_stats.routines.CalculateRankId;
import com.github.mbto.funnyranks.common.model.funnyranks_stats.routines.CalculateSkill;
import com.github.mbto.funnyranks.common.model.funnyranks_stats.routines.Declension;
import com.github.mbto.funnyranks.common.model.funnyranks_stats.routines.Playerbyip;
import com.github.mbto.funnyranks.common.model.funnyranks_stats.routines.Playerbyname;
import com.github.mbto.funnyranks.common.model.funnyranks_stats.routines.Playerbysteamid2;
import com.github.mbto.funnyranks.common.model.funnyranks_stats.routines.Playerbysteamid64;
import com.github.mbto.funnyranks.common.model.funnyranks_stats.routines.Top;

import org.jooq.Configuration;
import org.jooq.Field;
import org.jooq.types.UInteger;


/**
 * Convenience access to all stored procedures and functions in funnyranks_stats
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Routines {

    /**
     * Call <code>funnyranks_stats.build_human_time</code>
     */
    public static String buildHumanTime(Configuration configuration, UInteger timeSecs, String lang) {
        BuildHumanTime f = new BuildHumanTime();
        f.setTimeSecs(timeSecs);
        f.setLang(lang);

        f.execute(configuration);
        return f.getReturnValue();
    }

    /**
     * Get <code>funnyranks_stats.build_human_time</code> as a field.
     */
    public static Field<String> buildHumanTime(UInteger timeSecs, String lang) {
        BuildHumanTime f = new BuildHumanTime();
        f.setTimeSecs(timeSecs);
        f.setLang(lang);

        return f.asField();
    }

    /**
     * Get <code>funnyranks_stats.build_human_time</code> as a field.
     */
    public static Field<String> buildHumanTime(Field<UInteger> timeSecs, Field<String> lang) {
        BuildHumanTime f = new BuildHumanTime();
        f.setTimeSecs(timeSecs);
        f.setLang(lang);

        return f.asField();
    }

    /**
     * Call <code>funnyranks_stats.build_stars</code>
     */
    public static String buildStars(Configuration configuration, UInteger level, UInteger ranksTotal) {
        BuildStars f = new BuildStars();
        f.setLevel(level);
        f.setRanksTotal(ranksTotal);

        f.execute(configuration);
        return f.getReturnValue();
    }

    /**
     * Get <code>funnyranks_stats.build_stars</code> as a field.
     */
    public static Field<String> buildStars(UInteger level, UInteger ranksTotal) {
        BuildStars f = new BuildStars();
        f.setLevel(level);
        f.setRanksTotal(ranksTotal);

        return f.asField();
    }

    /**
     * Get <code>funnyranks_stats.build_stars</code> as a field.
     */
    public static Field<String> buildStars(Field<UInteger> level, Field<UInteger> ranksTotal) {
        BuildStars f = new BuildStars();
        f.setLevel(level);
        f.setRanksTotal(ranksTotal);

        return f.asField();
    }

    /**
     * Call <code>funnyranks_stats.build_stars2</code>
     */
    public static String buildStars2(Configuration configuration, UInteger level, UInteger ranksTotal) {
        BuildStars2 f = new BuildStars2();
        f.setLevel(level);
        f.setRanksTotal(ranksTotal);

        f.execute(configuration);
        return f.getReturnValue();
    }

    /**
     * Get <code>funnyranks_stats.build_stars2</code> as a field.
     */
    public static Field<String> buildStars2(UInteger level, UInteger ranksTotal) {
        BuildStars2 f = new BuildStars2();
        f.setLevel(level);
        f.setRanksTotal(ranksTotal);

        return f.asField();
    }

    /**
     * Get <code>funnyranks_stats.build_stars2</code> as a field.
     */
    public static Field<String> buildStars2(Field<UInteger> level, Field<UInteger> ranksTotal) {
        BuildStars2 f = new BuildStars2();
        f.setLevel(level);
        f.setRanksTotal(ranksTotal);

        return f.asField();
    }

    /**
     * Call <code>funnyranks_stats.calculate_rank_id</code>
     */
    public static UInteger calculateRankId(Configuration configuration, UInteger kills, UInteger deaths, UInteger timeSecs) {
        CalculateRankId f = new CalculateRankId();
        f.setKills(kills);
        f.setDeaths(deaths);
        f.setTimeSecs(timeSecs);

        f.execute(configuration);
        return f.getReturnValue();
    }

    /**
     * Get <code>funnyranks_stats.calculate_rank_id</code> as a field.
     */
    public static Field<UInteger> calculateRankId(UInteger kills, UInteger deaths, UInteger timeSecs) {
        CalculateRankId f = new CalculateRankId();
        f.setKills(kills);
        f.setDeaths(deaths);
        f.setTimeSecs(timeSecs);

        return f.asField();
    }

    /**
     * Get <code>funnyranks_stats.calculate_rank_id</code> as a field.
     */
    public static Field<UInteger> calculateRankId(Field<UInteger> kills, Field<UInteger> deaths, Field<UInteger> timeSecs) {
        CalculateRankId f = new CalculateRankId();
        f.setKills(kills);
        f.setDeaths(deaths);
        f.setTimeSecs(timeSecs);

        return f.asField();
    }

    /**
     * Call <code>funnyranks_stats.calculate_skill</code>
     */
    public static Integer calculateSkill(Configuration configuration, Long kills, Long deaths) {
        CalculateSkill f = new CalculateSkill();
        f.setKills(kills);
        f.setDeaths(deaths);

        f.execute(configuration);
        return f.getReturnValue();
    }

    /**
     * Get <code>funnyranks_stats.calculate_skill</code> as a field.
     */
    public static Field<Integer> calculateSkill(Long kills, Long deaths) {
        CalculateSkill f = new CalculateSkill();
        f.setKills(kills);
        f.setDeaths(deaths);

        return f.asField();
    }

    /**
     * Get <code>funnyranks_stats.calculate_skill</code> as a field.
     */
    public static Field<Integer> calculateSkill(Field<Long> kills, Field<Long> deaths) {
        CalculateSkill f = new CalculateSkill();
        f.setKills(kills);
        f.setDeaths(deaths);

        return f.asField();
    }

    /**
     * Call <code>funnyranks_stats.declension</code>
     */
    public static String declension(Configuration configuration, Integer value, String opt1, String opt2, String opt3) {
        Declension f = new Declension();
        f.setValue(value);
        f.setOpt1(opt1);
        f.setOpt2(opt2);
        f.setOpt3(opt3);

        f.execute(configuration);
        return f.getReturnValue();
    }

    /**
     * Get <code>funnyranks_stats.declension</code> as a field.
     */
    public static Field<String> declension(Integer value, String opt1, String opt2, String opt3) {
        Declension f = new Declension();
        f.setValue(value);
        f.setOpt1(opt1);
        f.setOpt2(opt2);
        f.setOpt3(opt3);

        return f.asField();
    }

    /**
     * Get <code>funnyranks_stats.declension</code> as a field.
     */
    public static Field<String> declension(Field<Integer> value, Field<String> opt1, Field<String> opt2, Field<String> opt3) {
        Declension f = new Declension();
        f.setValue(value);
        f.setOpt1(opt1);
        f.setOpt2(opt2);
        f.setOpt3(opt3);

        return f.asField();
    }

    /**
     * Call <code>funnyranks_stats.PlayerByIp</code>
     */
    public static void playerbyip(Configuration configuration, UInteger ip, UInteger ranksTotal, String lang) {
        Playerbyip p = new Playerbyip();
        p.setIp(ip);
        p.setRanksTotal(ranksTotal);
        p.setLang(lang);

        p.execute(configuration);
    }

    /**
     * Call <code>funnyranks_stats.PlayerByName</code>
     */
    public static void playerbyname(Configuration configuration, String name, UInteger ranksTotal, String lang) {
        Playerbyname p = new Playerbyname();
        p.setName_(name);
        p.setRanksTotal(ranksTotal);
        p.setLang(lang);

        p.execute(configuration);
    }

    /**
     * Call <code>funnyranks_stats.PlayerBySteamId2</code>
     */
    public static void playerbysteamid2(Configuration configuration, String steamid2, UInteger ranksTotal, String lang) {
        Playerbysteamid2 p = new Playerbysteamid2();
        p.setSteamid2(steamid2);
        p.setRanksTotal(ranksTotal);
        p.setLang(lang);

        p.execute(configuration);
    }

    /**
     * Call <code>funnyranks_stats.PlayerBySteamId64</code>
     */
    public static void playerbysteamid64(Configuration configuration, Long steamid64, UInteger ranksTotal, String lang) {
        Playerbysteamid64 p = new Playerbysteamid64();
        p.setSteamid64(steamid64);
        p.setRanksTotal(ranksTotal);
        p.setLang(lang);

        p.execute(configuration);
    }

    /**
     * Call <code>funnyranks_stats.Top</code>
     */
    public static void top(Configuration configuration, UInteger rowsCount, String lang) {
        Top p = new Top();
        p.setRowsCount(rowsCount);
        p.setLang(lang);

        p.execute(configuration);
    }
}
