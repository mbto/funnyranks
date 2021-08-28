package com.github.mbto.funnyranks;

import com.github.mbto.funnyranks.common.dto.PortData;
import com.github.mbto.funnyranks.common.dto.identity.Identity;
import com.github.mbto.funnyranks.common.dto.session.ArchivedSessionView;
import com.github.mbto.funnyranks.common.dto.session.Storage;
import com.github.mbto.funnyranks.common.model.funnyranks.enums.ProjectMergeType;
import com.github.mbto.funnyranks.dao.FunnyRanksDao;
import com.github.mbto.funnyranks.dao.FunnyRanksStatsDao;
import lombok.extern.slf4j.Slf4j;
import org.jooq.types.UShort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.github.mbto.funnyranks.common.utils.ProjectUtils.buildIdentitiesContainer;
import static com.github.mbto.funnyranks.common.utils.ProjectUtils.declension2;

@Service
@Lazy(false)
@Slf4j
public class SessionsSender {
    @Autowired
    private FunnyRanksDao funnyRanksDao;
    @Autowired
    private FunnyRanksStatsDao funnyRanksStatsDao;

    @Async("senderTE")
    public void aggregateAndMergeAsync(PortData portData, Map<String, Storage> storageByNameCopy) {
        UShort portValue = portData.getPort().getValue();
        ProjectMergeType mergeType = portData.getProject().getMergeType();
        int storagesCount = storageByNameCopy.size();
        int sessionsCount = storageByNameCopy.values()
                .stream()
                .mapToInt(Storage::calcSessionsCount)
                .sum();
        String logMsg = "Converting " + declension2(storagesCount, "storage")
                + " (" + declension2(sessionsCount, "session") + ") to identities by '" + mergeType + "'";
        log.info(portValue + " " + logMsg);
        portData.addMessage(logMsg);

        if (log.isDebugEnabled()) {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, Storage> entry : storageByNameCopy.entrySet()) {
                sb.append(entry.getKey()).append(" ").append(entry.getValue()).append('\n');
            }
            log.debug(portValue + " storageByNameCopy summary:\n" + sb);
        }
        Map<Identity, List<ArchivedSessionView>> archivedSessionViewsByIdentity = buildIdentitiesContainer(portData, storageByNameCopy, false, true);
        storageByNameCopy.clear();
        if (archivedSessionViewsByIdentity.isEmpty()) {
            logMsg = "Skip flush sessions by '" + mergeType + "', due no identities";
            log.info(portValue + " " + logMsg);
            portData.addMessage(logMsg);
            return;
        }
        funnyRanksDao.fillSessionByIdentityContainerWithCounties(portData, archivedSessionViewsByIdentity);
        int identitiesCount = archivedSessionViewsByIdentity.size();
        sessionsCount = archivedSessionViewsByIdentity.values()
                .stream()
                .mapToInt(List::size)
                .sum();
        StringBuilder sb = new StringBuilder("Flushing " + declension2(identitiesCount, "identit", "ies", "y")
                + " (" + declension2(sessionsCount, "session") + "):");
        for (Map.Entry<Identity, List<ArchivedSessionView>> entry : archivedSessionViewsByIdentity.entrySet()) {
            Identity identity = entry.getKey();
            List<ArchivedSessionView> archivedSessionViews = entry.getValue();

            sb.append('\n').append(identity).append('\n')
                    .append(archivedSessionViews.stream()
                            .map(archivedSessionView -> archivedSessionView.getArchivedSession().toString(archivedSessionView.getName())
                            ).collect(Collectors.joining("\n\t", "\t", "")));
        }
        log.info(portValue + " " + sb);
        portData.addMessage(sb.toString());
        try {
            funnyRanksStatsDao.mergeIdentities(portData, archivedSessionViewsByIdentity);
            logMsg = "Successfully merged " + declension2(identitiesCount, "identit", "ies", "y")
                    + " (" + declension2(sessionsCount, "session") + ")";

            log.info(portValue + " " + logMsg);
            portData.addMessage(logMsg);
        } catch (Throwable e) {
            logMsg = "Failed merging " + declension2(identitiesCount, "identit", "ies", "y")
                    + " (" + declension2(sessionsCount, "session") + ")";

            log.warn(portValue + " " + logMsg, e);
            portData.addMessage(logMsg + ". Exception: " + e);
        }
    }
}