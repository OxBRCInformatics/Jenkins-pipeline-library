import uk.ac.ox.ndm.jenkins.Utils

import java.nio.file.Files
import java.nio.file.Paths

/**
 * @since 03/07/2017
 */

List call(String gradle, String workspacePath, postgres, rabbit, int groupSize = 0, int timeoutMins = 15, boolean failFast = false) {

    Map jobs = [:]
    File workspace = new File(workspacePath)
    List<String> ignore = []

    List<String> lines = Paths.get(workspacePath).resolve('gradle.properties').readLines()
    for (int i = 0; i < lines.size(); i++) {
        if (lines[i].startsWith('jenkinsPipelineIgnoreIntegrationTests')) {
            ignore = lines[i].replaceFirst(/jenkinsPipelineIgnoreIntegrationTests=/, '').split(',')
        }
    }

    List<File> files = workspace.listFiles()

    for (int i = 0; i < files.size(); i++) {

        File file = files[i]
        if (Files.exists(Paths.get(file.path).resolve('src/integration-test'))) {
            if (!(file.name in ignore)) {
                echo "Integation tests found for ${file}"
                jobs[file.name] = {
                    node {
                        stage("${file.name} Integration Test") {

                            def pgPort = Utils.findFreeTcpPort()
                            def rPort = Utils.findFreeTcpPort()

                            echo "${file.name} running postgres on port ${pgPort} & rabbit on port ${rPort}"

                            rabbit.withRun("-p ${rPort}:5672") {
                                postgres.withRun("-p ${pgPort}:5432") {
                                    dir(file.path) {
                                        sh "${gradle} -Ddatabase.port=${pgPort} dbmUpdate"
                                        timeout(timeoutMins) {
                                            sh "grails -Ddatabase.port=${pgPort} -Drabbitmq.port=${rPort} test-app --integration"
                                        }
                                        junit allowEmptyResults: true, testResults: 'build/test-results/**/*.xml'
                                        publishHTML([
                                                allowMissing         : true,
                                                alwaysLinkToLastBuild: true,
                                                keepAll              : false,
                                                reportDir            : 'build/reports/tests',
                                                reportFiles          : 'index.html',
                                                reportName           : "${file.name} Integration Test Report"
                                        ])
                                        outputTestResults()
                                    }
                                }
                            }
                        }
                    }
                }
            }

        }
    }
    List groupedJobs = []
    if (groupSize != 0) {
        Set jobset = jobs.keySet()

        for (int i = 0; i < (jobs.size() / groupSize); i++) {
            int key = i * groupSize
            Map sub = [:]
            for (int j = 0; j < groupSize; j++) {
                if (jobset[key + j]) {
                    sub["${jobset[key + j]}"] = jobs[jobset[key + j]]
                }
            }
            sub.failFast = failFast
            groupedJobs.add(sub)
        }
    }
    else {
        jobs.failFast = failFast
        groupedJobs.add(jobs)
    }
    groupedJobs
}