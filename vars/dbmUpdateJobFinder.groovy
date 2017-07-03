import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 * @since 03/07/2017
 */

Map call(gradle, pgPort) {

    Map jobs = [failFast: true]
    File workspace = new File(pwd() as String)
    println "Workspace: ${workspace}"

    List<File> files = workspace.listFiles()

    for (int i = 0; i < files.size(); i++) {
        File file = files[i]
        Path props = Paths.get(file.path).resolve('gradle.properties')
        if (Files.exists(props)) {
            def lines = props.readLines()
            def res = false
            for (int j = 0; j < lines.size(); j++) {
                if (lines[j].startsWith('dataSource')) {
                    res = true
                }
            }
            if (res) {
                println "${file.name} gradle properties found with datasource"
                jobs["${file.name}"] = {
                    node {
                        stage('DB Update Checkout') {
                            checkout scm
                        }
                        stage('DB Update') {
                            dir(file.name) {
                                sh "${gradle} -Ddatabase.port=${pgPort} dbmUpdate"
                            }
                        }
                    }
                }
            }
        }
    }



    /*

    Path workspace = Paths.get(pwd() as String)
        Files.walk(workspace)
                .filter({path -> Files.exists(path.resolve('gradle.properties'))})
                .filter({path ->
            Files.readAllLines(path.resolve('gradle.properties')).any {line ->
                line.startsWith('dataSource')
            }
        }).each {project ->
            String dirName = project.fileName.toString()
            jobs["${dirName}"] = {
                dir("${dirName}") {
                    sh "${gradle} -Ddatabase.port=${pgPort} dbmUpdate"
                }
            }
        }
        */
    jobs
}