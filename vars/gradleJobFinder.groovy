#!/usr/bin/env groovy

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

Map getDbmUpdateJobs(String gradle) {
    Map jobs = [failFast: true]
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
                sh "${gradle} dbmUpdate"
            }
        }
    }
    jobs
}

Map getIntegrationTestJobs(String gradle) {
    Map jobs = [failFast: true]
    Path workspace = Paths.get(pwd() as String)

    Files.walk(workspace)
            .filter({path -> Files.exists(path.resolve('src/integration-test'))})
            .each {project ->
        String dirName = project.fileName.toString()
        jobs["${dirName}"] = {
            dir("${dirName}") {
                sh "${grails} test-app --integration"
            }
        }
    }
    jobs
}