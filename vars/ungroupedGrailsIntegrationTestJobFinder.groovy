import uk.ac.ox.ndm.jenkins.Utils

import java.nio.file.Files
import java.nio.file.Paths

/**
 * @since 03/07/2017
 */

List call(String workspacePath, postgres, rabbit,
          String gradle = './gradlew', String grails = './grailsw', String grailsVersion = null,
          int timeoutMins = 15, boolean failFast = false) {

    Map jobs = [:]
    File workspace = new File(workspacePath)

    String ignoreTests = Utils.findProperty(workspacePath, 'gradle.properties', 'jenkinsPipelineIgnoreIntegrationTests')
    List<String> ignore = ignoreTests ? ignoreTests.split(',') : []

    List<File> files = workspace.listFiles()

    if (!files) return jobs

    for (int i = 0; i < files.size(); i++) {

        File file = files[i]
        if (Files.exists(Paths.get(file.path).resolve('src/integration-test'))) {
            if (!(file.name in ignore)) {
                echo "Integation tests found for ${file}"
                jobs[file.name] =

                    stage("${file.name} Integration Test") {

                        def pgPort = Utils.findFreeTcpPort()
                        def rPort = Utils.findFreeTcpPort()

                        echo "${file.name} running postgres on port ${pgPort} & rabbit on port ${rPort}"

                        rabbit.withRun("-p ${rPort}:5672") {
                            postgres.withRun("-p ${pgPort}:5432") {
                                dir(file.path) {
                                    if (grailsVersion) {
                                        // Add grails version to properties file to get the grails wrapper to work
                                        sh "printf '\\ngrailsVersion=${grailsVersion}' >> gradle.properties"
                                    }
                                    sh "${gradle} -Ddatabase.port=${pgPort} -Dorg.gradle.daemon=false dbmUpdate"
                                    timeout(timeoutMins) {
                                        sh "${grails} -Ddatabase.port=${pgPort} -Drabbitmq.port=${rPort} -Dorg.gradle.daemon=false " +
                                           "test-app --integration"
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

    jobs.failFast = failFast
    jobs
}