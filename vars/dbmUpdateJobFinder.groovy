import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 * @since 03/07/2017
 */

Map call(String gradle) {

    //  String s = ''
    // s.readLines().find{it.startsWith('jenkinsPipelineIgnoreIntegrationTests')}

    Map jobs = [failFast: true]
    Path workspace = Paths.get(pwd() as String)


    workspace.eachDir { dir ->
        def files = dir.listFiles({p,name -> name == 'gradle.properties'} as FilenameFilter)
        if(files){
            def props = files.first()
            def res = props.readLines().any {line -> line.startsWith('dataSource')}
            if(res){
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