import java.nio.file.Files
import java.nio.file.Paths

/**
 * @since 03/07/2017
 */

Map call(grails) {

    Map jobs = [failFast: true]
    File workspace = new File(pwd() as String)
    println "Workspace: ${workspace}"

    List<File> files = workspace.listFiles()

    for (int i = 0; i < files.size(); i++) {

        File file = files[i]
        if (Files.exists(Paths.get(file.path).resolve('src/test'))) {
            println "Unit tests found for ${file}"
            String dirName = file.name
            jobs[dirName] = {
                node {
                    stage('Unit Test Checkout') {
                        checkout scm
                    }

                    stage('Unit Test') {
                        dir("${dirName}") {
                            sh "${grails} test-app -unit"
                        }
                    }

                    stage('Unit Test Results') {
                        dir("${dirName}") {
                            junit allowEmptyResults: true, testResults: '**/build/test-results/**/*.xml'
                        }
                    }
                }
            }
        }

    }
    jobs
}