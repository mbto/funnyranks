package com.github.mbto.funnyranks.common.dto;

import com.github.mbto.funnyranks.common.model.funnyranks.tables.pojos.DriverProperty;
import com.github.mbto.funnyranks.common.model.funnyranks.tables.pojos.Game;
import com.github.mbto.funnyranks.common.model.funnyranks.tables.pojos.Port;
import com.github.mbto.funnyranks.common.model.funnyranks.tables.pojos.Project;
import lombok.Getter;
import lombok.Setter;
import org.jooq.types.UInteger;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class FunnyRanksData {
    private Map<UInteger, Game> gameByAppId;
    private List<Port> ports;
    private Map<UInteger, Project> projectByProjectId;
    private Map<UInteger, List<DriverProperty>> driverPropertiesByProjectId;
}