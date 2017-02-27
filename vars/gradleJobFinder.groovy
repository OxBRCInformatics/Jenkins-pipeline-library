#!/usr/bin/env groovy
import uk.ac.ox.ndm.jenkins.Utils

import java.nio.file.Path

// ws = pwd() as String
Map getGrailsIntegrationTestJobs(String gradle, String grails, String ws) {
    Map jobs = [:]
    File workspace = new File(ws)

    echo "Workspace: ${workspace}"

    List<Path> dirs = Utils.getGrailsDirectoriesWithIntegrationTestFolders(ws)

    dirs.each {
        echo "Testing ${it}"
        /*
        if (Files.exists(resolve('src/integration-test'))) {

            String dirName = path.fileName.toString()
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
                                    sh "${gradle} dbmUpdate"
                                    sh "${grails} test-app --integration"
                                }
                            }
                        }
                    }

                    stage('Integration Test Results') {
                        junit allowEmptyResults: true, testResults: '** /build/test-results/*.xml'
                    }
                }


            }
        }
        */
    }
    jobs.failFast = true
    jobs
}