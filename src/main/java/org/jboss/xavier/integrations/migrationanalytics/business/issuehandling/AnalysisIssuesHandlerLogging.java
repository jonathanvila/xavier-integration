package org.jboss.xavier.integrations.migrationanalytics.business.issuehandling;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AnalysisIssuesHandlerLogging implements AnalysisIssuesHandler {

    @Override
    public void record(String analysisId, String entity, String entityName, String jsonPath, String message) {
        log.warn("Exception on {} [{}] reading value from JSON [{}}] : {}" , entity, entityName, jsonPath, message);
    }
}
