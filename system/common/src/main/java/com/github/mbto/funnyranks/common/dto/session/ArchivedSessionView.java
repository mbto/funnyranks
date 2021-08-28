package com.github.mbto.funnyranks.common.dto.session;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ArchivedSessionView {
    private String name;
    private Session archivedSession;
}