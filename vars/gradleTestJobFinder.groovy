import uk.ac.ox.ndm.jenkins.Utils

import java.nio.file.Files
import java.nio.file.Paths

/**
 * @since 03/07/2017
 */

Map call(String workspacePath, String gradle = './gradlew', boolean failFast = false) {

    Map jobs = [:]
    File workspace = new File(workspacePath)

    String ignoreTests = Utils.findProperty(workspacePath,'gradle.properties', 'jenkinsPipelineIgnoreTests')
    List<String> ignore = ignoreTests ? ignoreTests.split(',') : []

    List<File> files = workspace.listFiles()

    for (int i = 0; i < files.size(); i++) {

        File file = files[i]
        if (Files.exists(Paths.get(file.path).resolve('src/test'))) {
            if (!(file.name in ignore)) {
                echo "Unit tests found for ${file}"
                jobs[file.name] = {
                    node {
                        dir(file.path) {
                            stage('Unit Test') {
                                sh "${gradle} -Dorg.gradle.daemon=false test"
                                junit allowEmptyResults: true, testResults: 'build/test-results/**/*.xml'
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