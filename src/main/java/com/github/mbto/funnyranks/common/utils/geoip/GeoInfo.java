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
        StringBuilder sb = new StringBuilder();
        if(projectLanguage == ProjectLanguage.ru) {
            if(locationRu != null) {
                sb.append(locationRu);
            } else if(locationMix != null){
                sb.append(locationMix);
            }
        } else {
            if(locationEn != null) {
                sb.append(locationEn);
            } else if(locationMix != null){
                sb.append(locationMix);
            }
        }
        return sb.toString();
    }
}