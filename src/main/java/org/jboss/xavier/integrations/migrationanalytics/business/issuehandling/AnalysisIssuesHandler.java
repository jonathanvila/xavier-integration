package org.jboss.xavier.integrations.migrationanalytics.business.issuehandling;

public interface AnalysisIssuesHandler {
    void record(String analysisId, String vmName, String jsonPath, String message);
}
