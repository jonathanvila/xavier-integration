package org.jboss.xavier.integrations.migrationanalytics.business.issuehandling;

public interface AnalysisIssuesHandler {
    void record(String analysisId, String entity, String entityName, String jsonPath, String message);
}
