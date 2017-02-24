#!/usr/bin/env groovy
import uk.ac.ox.ndm.jenkins.Utils

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.stream.Collectors

// ws = pwd() as String
Map getGrailsIntegrationTestJobs(String gradle, String grails, String ws) {
    Map jobs = [:]
    Path workspace = Paths.get(ws)

    echo "Workspace: ${workspace}"

    def itPaths = Files.walk(workspace).filter({path ->
        Files.isDirectory(path)
    }).collect(Collectors.toList())
/*
.filter({path ->
        echo "Path: ${path}"
        Files.exists(path.resolve('src/integration-test'))
    })
 */
    echo "itPaths: ${itPaths}"
/*
    itPaths.each {project ->
        String dirName = project.fileName.toString()
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
                    junit allowEmptyResults: true, testResults: '**/build/test-results/*.xml'
                }
            }


        }
    }
    */
    jobs.failFast = true
    jobs
}