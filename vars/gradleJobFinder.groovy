#!/usr/bin/env groovy
import uk.ac.ox.ndm.jenkins.Utils

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths


Map getDbmUpdateJobs(String gradle) {

  //  String s = ''
   // s.readLines().find{it.startsWith('jenkinsPipelineIgnoreIntegrationTests')}

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

// ws = pwd() as String
Map getGrailsIntegrationTestJobs(String gradle, String grails, String ws) {
    Map jobs = [:]
    File workspace = new File(ws)

    List<File> files = workspace.listFiles()

    for(File file : files){
        if (Files.exists(Paths.get(file.path).resolve('src/integration-test'))) {

            String dirName = file.name
            jobs[dirName] = {

                node {
                    stage('IT Checkout') {
                        checkout scm
                    }

                    stage('Integration Test') {

                        def pgPort = Utils.findFreeTcpPort()
                        def rPort = Utils.findFreeTcpPort()

                        echo "Running postgres on port ${pgPort} & rabbit on port ${rPort}"

                        def postgres = docker.build('m_postgres', 'test-utils/src/main/dockerfiles/postgres')
                        def rabbit = docker.build('m_rabbit', 'test-utils/src/main/dockerfiles/rabbitmq')

                        rabbit.withRun("-p ${rPort}:5672") {
                            postgres.withRun("-p ${pgPort}:5432") {
                                dir("${dirName}") {
                                    sh "${gradle} clean dbmUpdate"
                                    sh "${grails} test-app --integration"
                                }
                            }
                        }
                    }

                    stage('Integration Test Results') {
                        junit allowEmptyResults: true, testResults: '**/build/test-results/**/*.xml'
                    }
                }


            }
        }

    }
    jobs.failFast = true
    jobs
}
