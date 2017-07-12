import java.nio.file.Files
import java.nio.file.Paths

/**
 * @since 03/07/2017
 */

Map call(String workspacePath, String gradle = './gradlew', boolean failFast = false) {

    Map jobs = [:]
    File workspace = new File(workspacePath)
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
        if (Files.exists(Paths.get(file.path).resolve('src/test'))) {
            if (!(file.name in ignore)) {
                echo "Unit tests found for ${file}"
                jobs[file.name] = {
                    node {
                        dir(file.path) {
                            stage('Unit Test') {
                                sh "${gradle} test"
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