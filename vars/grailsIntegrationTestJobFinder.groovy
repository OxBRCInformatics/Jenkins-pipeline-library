import uk.ac.ox.ndm.jenkins.Utils

import java.nio.file.Files
import java.nio.file.Paths

/**
 * @since 03/07/2017
 */

Map call(String workspacePath, int timeoutMins = 15, boolean failFast = false) {

    Map jobs = [:]
    File workspace = new File(workspacePath)
    println "Workspace: ${workspace}"
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
                println "Integation tests found for ${file}"
                jobs[file.name] = {
                    node {
                        timeout(timeoutMins) {

                            stage('Integration Test') {

                                def pgPort = Utils.findFreeTcpPort()
                                def rPort = Utils.findFreeTcpPort()

                                echo "Running postgres on port ${pgPort} & rabbit on port ${rPort}"

                                def postgres = docker.build('m_postgres', 'test-utils/src/main/dockerfiles/postgres')
                                def rabbit = docker.build('m_rabbit', 'test-utils/src/main/dockerfiles/rabbitmq')

                                rabbit.withRun("-p ${rPort}:5672") {
                                    postgres.withRun("-p ${pgPort}:5432") {
                                        dir(file.path) {
                                            sh "${gradle} clean dbmUpdate"
                                            sh "grails -Ddatabase.port=${pgPort} -Drabbitmq.port=${rPort} test-app --integration"
                                        }
                                    }
                                }
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