import uk.ac.ox.ndm.jenkins.Utils

import java.nio.file.Files
import java.nio.file.Paths

/**
 * @since 03/07/2017
 */

void call(String workspacePath, def postgres, def rabbit,
          String gradle = './gradlew', String grails = './grailsw', String grailsVersion = null, Integer dbmUpdateTimeoutMins = 15,
          Integer testTimeoutMins = 15) {

    Map jobs = [:]
    File workspace = new File(workspacePath)

    String ignoreTests = Utils.findProperty(workspacePath, 'gradle.properties', 'jenkinsPipelineIgnoreIntegrationTests')
    List<String> ignore = ignoreTests ? ignoreTests.split(',') : []

    List<File> files = workspace.listFiles()

    if (!files) return

    for (int i = 0; i < files.size(); i++) {

        File file = files[i]
        if (Files.exists(Paths.get(file.path).resolve('src/integration-test'))) {
            if (!(file.name in ignore)) {
                echo "Integation tests found for ${file}"
                serialGrailsIntegrationTestJob(file.name, file.path,
                                               postgres, rabbit,
                                               gradle, grails, grailsVersion,
                                               dbmUpdateTimeoutMins, testTimeoutMins)
            }

        }
    }
}