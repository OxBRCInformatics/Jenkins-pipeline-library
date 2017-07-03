/**
 * @since 03/07/2017
 */

Map call(gradle, pgPort) {

    //  String s = ''
    // s.readLines().find{it.startsWith('jenkinsPipelineIgnoreIntegrationTests')}

    Map jobs = [failFast:true]

    File workspace = new File(pwd() as String)
    println "Workspace: ${workspace}:${workspace.exists()}"

    List<File> files = workspace.listFiles()

    for (int i = 0; i < files.size(); i++) {
        File lf = files[i]
        if (lf.isDirectory()) {
            def props = null
            List<File> subFiles = lf.listFiles()
            for (int j = 0; j < subFiles.size(); j++) {
                if (subFiles[j].name == 'gradle.properties') props = subFiles[j]
            }

            if (props) {
                def lines = props.readLines()
                def res = false
                for (int j = 0; j < lines.size(); j++) {
                    if (lines[j].startsWith('dataSource')) {
                        res = true
                    }
                }
                if (res) {
                    println "${lf} gradle properties found with datasource"
                    jobs[lf.name] = {
                        dir(lf.name) {
                            sh "${gradle} -Ddatabase.port=${pgPort} dbmUpdate"
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