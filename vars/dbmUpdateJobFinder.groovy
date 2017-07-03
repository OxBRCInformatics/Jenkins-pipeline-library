/**
 * @since 03/07/2017
 */

Map call(String gradle) {

    //  String s = ''
    // s.readLines().find{it.startsWith('jenkinsPipelineIgnoreIntegrationTests')}

    Map jobs = [failFast: true]


    File workspace = new File(pwd() as String)
    println "Workspace: ${workspace}:${workspace.exists()}"
    workspace.eachDir { dir ->
        println "Dir: ${dir}"
        def files = dir.listFiles({p,name -> name == 'gradle.properties'} as FilenameFilter)
        if(files){
            println "gradle properties found"
            def props = files.first()
            def res = props.readLines().any {line -> line.startsWith('dataSource')}
            if(res){
                println "result"
                String dirName = dir.name
                jobs["${dirName}"] = {
                    dir("${dirName}") {
                        sh "${gradle} -Ddatabase.port=${pgPort} dbmUpdate"
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