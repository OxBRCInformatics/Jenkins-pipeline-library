import java.nio.file.Files
import java.nio.file.Paths

/**
 * @since 03/07/2017
 */

Map call(gradle, codebase) {

    Map jobs = [:]
    File workspace = new File(pwd() as String)
    println "Workspace: ${workspace}"
    List<String> ignore = []

    List<String> lines = Paths.get(workspace.path).resolve('gradle.properties').readLines()
    for (int i = 0; i < lines.size(); i++) {
        if(lines[i].startsWith('jenkinsPipelineIgnoreTests')){
            ignore = lines[i].replaceFirst(/jenkinsPipelineIgnoreTests=/,'').split(',')
        }
    }

    List<File> files = workspace.listFiles()

    for (int i = 0; i < files.size(); i++) {

        File file = files[i]
        if (Files.exists(Paths.get(file.path).resolve('src/test'))) {
            println "Unit tests found for ${file}"
            String dirName = file.name
            if(!(dirName in ignore)) {
                jobs[dirName] = {
                    node {
                        stage('Unit Test Checkout') {
                            unstash codebase
                        }

                        stage('Unit Test') {
                            dir("${dirName}") {
                                sh "${gradle} test"
                            }
                        }

                        stage('Unit Test Results') {
                            dir("${dirName}") {
                                junit allowEmptyResults: true, testResults: 'build/test-results/**/*.xml'
                            }
                        }
                    }
                }
            }
        }

    }
    jobs
}