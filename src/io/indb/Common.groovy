#!/usr/bin/groovy
package io.indb;

def buildVersionName() {
    if ("${env.BUILD_TYPE}" == 'release') {
        return "v${MAJOR_VERSION}.${MINOR_VERSION}.${PATCH_VERSION}-RELEASE"
    }
    else {
        def branch_display = getNexusBranchName(env.BRANCH_NAME)
        return "${branch_display}-SNAPSHOT"
    }
}

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

def getNexusGroup() {
    if ("${env.BUILD_TYPE}" == 'release') {
        return "${env.GROUP}.releases"
    }
    else {
        if ("${env.BRANCH_NAME}" == 'master') {
            return "${env.GROUP}.master"
        }
        else {
            return "${env.GROUP}.branches"
        }
    }
}

def getNexusRepo() {
    if ("${env.BUILD_TYPE}" == 'release') {
        return REPO_RELEASES
    }
    else {
        return REPO_SNAPSHOTS
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
    version: vars.version
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