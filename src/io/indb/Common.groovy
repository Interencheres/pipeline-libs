#!/usr/bin/groovy
package io.indb;

def getShell() {
    new Shell()
}

def getVars(Map args) {
    filename = 'Jenkinsfile.json'
    if (args.filename) {
        filename = args.filename
    }

    def input_file = readFile(filename)
    def config = new groovy.json.JsonSlurperClassic().parseText(input_file)

    if (args.debug) {
        println "config ==> ${config}"
    }

    return config
}

def getGroups(String branch, Map vars) {
    if ("${branch}" == 'master') {
        return "${vars.app.group}.master"
    }
    else {
        return "${vars.app.group}.branches"
    }
}

def getNexusBranchName(String branch) {
    return "${branch}".replaceAll("[^A-Za-z0-9]", "-")
}

def getHashCommit() {
    // Retrieve current commit hash
    // @see https://issues.jenkins-ci.org/browse/JENKINS-26100
    sh 'git rev-parse --short HEAD > commit'
    return readFile('commit').trim()
}

def sendToNexus(Map vars) {
    nexusArtifactUploader artifacts: [
        [
        artifactId: vars.name,
        classifier: 'sources',
        file: "${vars.name}-${vars.commit}-${vars.build_id}.tar.gz",
        type: 'tar.gz'
        ]
    ],
    credentialsId: 'nexus',
    groupId: vars.group,
    nexusUrl: vars.nexus,
    nexusVersion: 'nexus2',
    protocol: 'http',
    repository: vars.repo,
    version: "${vars.branch_display}-SNAPSHOT"
}

def moveArchiveInProjet(Map vars) {
    sh "mv /tmp/${vars.name}-${vars.commit}-${vars.build_id}.tar.gz ."
}

def createArtifacts() {
    archiveArtifacts artifacts: "*", fingerprint: true
}

def createArchive(String name, String commit, String build_id, String options='') {
    sh "tar czf /tmp/${name}-${commit}-${build_id}.tar.gz -C . . $options"
}

def clean(String[] folders = []) {
    for(int i=0; i < folders.size(); i++) {
        sh "rm -Rf ${folders[i]} || true"
    }
}