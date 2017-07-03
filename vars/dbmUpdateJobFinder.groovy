/**
 * @since 03/07/2017
 */

List call(gradle, pgPort) {

    //  String s = ''
    // s.readLines().find{it.startsWith('jenkinsPipelineIgnoreIntegrationTests')}

    List jobs = []


    File workspace = new File(pwd() as String)
    println "Workspace: ${workspace}:${workspace.exists()}"

    workspace.listFiles().each { lf ->

        if(lf.isDirectory()) {
            def props = null
            lf.listFiles().each {f ->
                if (f.name == 'gradle.properties') props = f
            }

            if (props) {
                def res = props.readLines().any {line -> line.startsWith('dataSource')}
                if (res) {
                    println "${lf} gradle properties found with datasource"
                    jobs.add(lf.name)
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