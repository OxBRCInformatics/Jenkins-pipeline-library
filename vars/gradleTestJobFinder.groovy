import java.nio.file.Files
import java.nio.file.Paths

/**
 * @since 03/07/2017
 */

Map call(String gradle, String workspacePath) {

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
        if (Files.exists(Paths.get(file.path).resolve('src/test'))) {
            println "Unit tests found for ${file}"
            String dirName = file.name
            if (!(dirName in ignore)) {
                jobs[dirName] = {
                    node {
                        println file.path
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
    jobs.failFast = true
    jobs
}