import java.nio.file.Files
import java.nio.file.Paths

/**
 * @since 03/07/2017
 */

Map call(String workspacePath, pgPort, rPort, int timeoutMins = 15, boolean failFast = false) {

    Map jobs = [:]
    File workspace = new File(workspacePath)
    println "Workspace: ${workspace}"
    List<String> ignore = []

    List<String> lines = Paths.get(workspacePath).resolve('gradle.properties').readLines()
    for (int i = 0; i < lines.size(); i++) {
        if (lines[i].startsWith('jenkinsPipelineIgnoreTests')) {
            ignore = lines[i].replaceFirst(/jenkinsPipelineIgnoreTests=/, '').split(',')
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
                            dir(file.path) {
                                stage('Integration Test') {
                                    sh "grails -Ddatabase.port=${pgPort} -Drabbitmq.port=${rPort} test-app --integration"
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