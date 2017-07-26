import uk.ac.ox.ndm.jenkins.Utils

/**
 * @since 24/07/2017
 */

void call(String jobName, String dirPath,
          def postgres, def rabbit,
          String gradle = './gradlew', String grails = './grailsw', String grailsVersion = null,
          Integer dbmUpdateTimeoutMins = 10, Integer testTimeoutMins = 15 ){

    if (currentBuild.currentResult == 'SUCCESS') {
        stage("${jobName} Integration Test") {

            def pgPort = Utils.findFreeTcpPort()
            def rPort = Utils.findFreeTcpPort()

            echo "${jobName} running postgres on port ${pgPort} & rabbit on port ${rPort}"

            rabbit.withRun("-p ${rPort}:5672") {
                postgres.withRun("-p ${pgPort}:5432") {
                    dir(dirPath) {
                        if (grailsVersion) {
                            // Add grails version to properties file to get the grails wrapper to work
                            sh "printf '\\ngrailsVersion=${grailsVersion}' >> gradle.properties"
                        }
                        retry(2) {
                            timeout(dbmUpdateTimeoutMins) {
                                sh "${gradle} -Ddatabase.port=${pgPort} -Dorg.gradle.daemon=false dbmUpdate"
                            }
                        }
                        retry(2) {
                            timeout(testTimeoutMins) {
                                sh "${grails} -Ddatabase.port=${pgPort} -Drabbitmq.port=${rPort} -Dorg.gradle.daemon=false " +
                                   "test-app --integration"
                            }
                        }
                        junit allowEmptyResults: true, testResults: 'build/test-results/**/*.xml'
                        publishHTML([
                                allowMissing         : true,
                                alwaysLinkToLastBuild: true,
                                keepAll              : false,
                                reportDir            : 'build/reports/tests',
                                reportFiles          : 'index.html',
                                reportName           : "${jobName} Integration Test Report"
                        ])
                        outputTestResults()
                    }
                }
            }
        }
    }
}