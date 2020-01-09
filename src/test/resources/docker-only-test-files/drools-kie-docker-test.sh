#!/usr/bin/env bash
#
# This file serves as a Docker only test for the Drools+KIE containers in order to test the interaction and deployment
#


# Docker Drools container
drools_ext_port=8080
kie_ext_port=8180
docker run -p $drools_ext_port:8080 -p 8001:8001 -d --name drools-workbench \
       --env KIE_ADMIN_USER=kieserver \
       --env KIE_ADMIN_PWD=kieserver1! jboss/drools-workbench-showcase:7.18.0.Final

sleep 5s
# Docker KIE container
docker run -p $kie_ext_port:8080 -d --name kie-server --link drools-workbench:kie-wb \
	--env KIE_SERVER_ID=analytics-kieserver \
	--env KIE_ADMIN_USER=kieserver \
	--env KIE_ADMIN_PWD=kieserver1! \
	--env KIE_SERVER_MODE=DEVELOPMENT \
        --env MAVEN_REPOS=BC,CENTRAL \
	--env BC_MAVEN_REPO_URL=http://kie-wb:8080/business-central/maven2 \
      	--env BC_MAVEN_REPO_PASSWORD=admin \
        --env BC_MAVEN_REPO_USER=admin \
	--env CENTRAL_MAVEN_REPO_URL=https://repo.maven.apache.org/maven2 \
	--env KIE_SERVER_CONTROLLER=http://kie-wb:8080/business-central/rest/controller \
       	--env KIE_REPOSITORY=https://repository.jboss.org/nexus/content/groups/public-jboss \
      	--env KIE_SERVER_CONTROLLER_PWD=admin \
      	--env KIE_SERVER_CONTROLLER_USER=admin \
      	--env KIE_SERVER_LOCATION=http://kie-server:8080/kie-server/services/rest/server \
      	--env KIE_SERVER_PWD=kieserver1! \
      	--env KIE_SERVER_USER=kieserver \
jboss/kie-server-showcase:7.18.0.Final


#	--env BC_MAVEN_REPO_URL=http://localhost:$drools_ext_port/business-central/maven2 \
#	--env KIE_SERVER_CONTROLLER=http://localhost:$drools_ext_port/business-central/rest/controller \
#      	--env KIE_SERVER_LOCATION=http://localhost:$kie_ext_port/kie-server/services/rest/server \

# Using docker-compose
#drools_ext_port=18080
#kie_ext_port=28080
#docker-compose -f drools-kie-compose.yml up -d --build

sleep 5s

# Create the initial space
curl http://localhost:$drools_ext_port/business-central/rest/spaces -X POST --data '{"name": "MySpace","description": "My new space.","owner": "admin","defaultGroupId": "com.newspace"}' \
     -H "Cache-Control:no-cache" -H "Content-Type:application/json" -H "Authorization:Basic YWRtaW46YWRtaW4=" -v

sleep 5s

# Clone the drools project from Github
curl http://localhost:$drools_ext_port/business-central/rest/spaces/MySpace/git/clone -X POST --data '{"name":"xavier-analytics","description":"project description inside business central.","gitURL":"https://github.com/jonathanvila/xavier-analytics"}' -H "Cache-Control:no-cache" -H "Content-Type:application/json" -H "Authorization:Basic YWRtaW46YWRtaW4=" -v
sleep 5s

# Compile the project
curl http://localhost:$drools_ext_port/business-central/rest/spaces/MySpace/projects/Xavier%20Analytics/maven/compile -X POST -H "Cache-Control:no-cache" -H "Content-Type:application/json" -H "Authorization:Basic YWRtaW46YWRtaW4=" -v
sleep 5s

# Install the project
curl http://localhost:$drools_ext_port/business-central/rest/spaces/MySpace/projects/Xavier%20Analytics/maven/install -X POST -H "Cache-Control:no-cache" -H "Content-Type:application/json" -H "Authorization:Basic YWRtaW46YWRtaW4=" -v
sleep 5s

# Deploy the project
curl http://localhost:$drools_ext_port/business-central/rest/spaces/MySpace/projects/Xavier%20Analytics/maven/deploy -X POST -H "Cache-Control:no-cache" -H "Content-Type:application/json" -H "Authorization:Basic YWRtaW46YWRtaW4=" -v

read -n 1 -s -r -p "Press any key to Create the BC Container"

# Create the Drools container
curl http://localhost:$drools_ext_port/business-central/rest/controller/management/servers/analytics-kieserver/containers/xavier-analytics_0.0.1-SNAPSHOT -X PUT -H "Cache-Control:no-cache" -H "Content-Type:application/xml" -H "Authorization:Basic YWRtaW46YWRtaW4=" --data '<?xml version="1.0" encoding="UTF-8" standalone="yes"?><container-spec-details> <container-id>xavier-analytics_0.0.1-SNAPSHOT</container-id><container-name>xavier-analytics_0.0.1-SNAPSHOT</container-name><release-id><artifact-id>org.jboss.xavier</artifact-id><group-id>xavier-analytics</group-id><version>0.0.1-SNAPSHOT</version></release-id><configs><entry> <key>RULE</key><value xsi:type="ruleConfig" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"><scannerStatus>STARTED</scannerStatus></value></entry><entry><key>PROCESS</key> <value xsi:type="processConfig" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"> <runtimeStrategy>SINGLETON</runtimeStrategy> <kbase></kbase> <ksession></ksession> <mergeMode>MERGE_COLLECTIONS</mergeMode> </value> </entry> </configs> <status>STARTED</status></container-spec-details>' -v

read -n 1 -s -r -p "Press any key to Create the KIE Container "

# Create the KIE container
curl  -X PUT -H "Cache-Control:no-cache" -H "Content-Type:application/json" -H "Authorization:Basic a2llc2VydmVyOmtpZXNlcnZlcjEh" --data '{"container-id" : "xavier-analytics_0.0.1-SNAPSHOT","release-id" : {"group-id" : "org.jboss.xavier","artifact-id" : "xavier-analytics","version" : "0.0.1-SNAPSHOT" } }' http://localhost:$kie_ext_port/kie-server/services/rest/server/containers/xavier-analytics_0.0.1-SNAPSHOT -v

ECHO "###### Installation Done"

##### the above line will give us following error , meaning KIE can not find the Drools Maven repository ####
# {
#  "type" : "FAILURE",
#  "msg" : "Failed to create container xavier-analytics_0.0.1-SNAPSHOT with module org.jboss.xavier:xavier-analytics:0.0.1-SNAPSHOT: java.lang.RuntimeException: Cannot find KieModule:  org.jboss.xavier:xavier-analytics:0.0.1-SNAPSHOT",
#  "result" : null
#* Connection #0 to host localhost left intact
#}%

# If we check on business-central with Admin->Artifacts we won't see any artifact deployed

read -n 1 -s -r -p "Press any key to call KIE to calculate InitialCostSavingsReport "


# Call to execute KIE commands
curl -X POST -H "Content-Type:application/xml" -H "X-KIE-ContentType:xstream"  -H "Authorization:Basic a2llc2VydmVyOmtpZXNlcnZlcjEh" --data '<?xml version="1.0" encoding="UTF-8"?><batch-execution lookup="kiesession0"><insert><org.jboss.xavier.analytics.pojo.input.UploadFormInputDataModel><customerId>99</customerId><fileName>vcenter.v2v.bos.redhat.com.json.zip</fileName><hypervisor>2</hypervisor><totalDiskSpace>563902124032</totalDiskSpace><year1HypervisorPercentage>10.0</year1HypervisorPercentage><year2HypervisorPercentage>10.0</year2HypervisorPercentage><year3HypervisorPercentage>10.0</year3HypervisorPercentage><growthRatePercentage>20.0</growthRatePercentage><dealIndicator>1</dealIndicator><openStackIndicator>1</openStackIndicator><sourceProductIndicator>1</sourceProductIndicator></org.jboss.xavier.analytics.pojo.input.UploadFormInputDataModel></insert><fire-all-rules/><query out-identifier="output" name="get InitialSavingsEstimationReports"/></batch-execution>' http://localhost:$kie_ext_port/kie-server/services/rest/server/containers/instances/xavier-analytics_0.0.1-SNAPSHOT

#This command gives us this error in KIE's log
#17:52:59,094 WARN  [org.appformer.maven.integration.MavenRepository] (default task-2) Unable to resolve artifact: org.jboss.xavier:xavier-analytics:pom:0.0.1-SNAPSHOT
#17:52:59,094 ERROR [org.kie.server.services.impl.KieServerImpl] (default task-2) Error creating container 'xavier-analytics_0.0.1-SNAPSHOT' for module 'org.jboss.xavier:xavier-analytics:0.0.1-SNAPSHOT': java.lang.RuntimeException: Cannot find KieModule: org.jboss.xavier:xavier-analytics:0.0.1-SNAPSHOT

echo "-------- The End, adeu siau -------"

# Useful commands
# To check how a job is going
# curl http://localhost:8080/business-central/rest/jobs/{job_id} -X GET -H "Cache-Control:no-cache" -H "Content-Type:application/json" -H "Authorization:Basic YWRtaW46YWRtaW4=" -v
