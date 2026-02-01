package com.github.mbto.funnyranks.common.utils.geoip;

import com.github.mbto.funnyranks.common.model.funnyranks.enums.ProjectLanguage;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public final class GeoInfo {
    private final Long geonameId;
    private final String countryEmoji;
    private final String locationRu;
    private final String locationEn;
    private final String locationMix;

    public GeoInfo(
            Long geonameId,
            String countryEmoji,
            String locationRu,
            String locationEn,
            String locationMix
    ) {
        this.geonameId = geonameId;
        this.countryEmoji = countryEmoji;
        this.locationRu = locationRu;
        this.locationEn = locationEn;
        this.locationMix = locationMix;
    }

    public String locationByProjectLanguage(ProjectLanguage projectLanguage) {
        if(projectLanguage == ProjectLanguage.ru) {
            if(locationMix != null) {
                return locationMix;
            } else if(locationRu != null){
                return locationRu;
            }
        } else {
            if(locationEn != null) {
                return locationEn;
            } else if(locationMix != null){
                return locationMix;
            }
        }
        return null;
    }
}