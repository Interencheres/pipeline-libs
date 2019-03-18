#!/usr/bin/groovy
package io.indb;

def buildVersionName(Map vars) {
    if ( "${vars.build_type}" == 'release' ) {
        return "v${vars.version.major}.${vars.version.minor}.${vars.version.patch}-RELEASE"
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

def getNexusGroup(String build_type) {
    if ( "${build_type}" == 'release' ) {
        return "${env.GROUP}.releases"
    }
    else {
        if (env.BRANCH_NAME == 'master') {
            return "${env.GROUP}.master"
        }
        else {
            return "${env.GROUP}.branches"
        }
    }
}

def getNexusRepo(String build_type) {
    if ( "${build_type}" == 'release' ) {
        return env.REPO_RELEASES
    }
    else {
        return env.REPO_SNAPSHOTS
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
    credentialsId: env.NEXUS_CRED_ID,
    groupId: getNexusGroup(vars.build_type),
    nexusUrl: env.NEXUS_URL,
    nexusVersion: env.NEXUS_VERSION,
    protocol: env.NEXUS_PROTO,
    repository: getNexusRepo(vars.build_type),
    version: buildVersionName(vars)
}

def getAppName() {
    def app_name = env.JOB_NAME.split('/')[1]
    println "app_name: ${app_name}"
    return app_name
}

def moveArchiveInProjet(Map vars) {
    sh "mv /tmp/${vars.name}-${vars.commit}-${vars.build_id}.tar.gz ."
}

def createArtifacts() {
    archiveArtifacts artifacts: "*", fingerprint: true
}

def createArchive(String name, String commit, String build_id, String options) {
    def default_options = './.git ./.gitignore'
    options = options.concat(default_options).split(' ').join(' --exclude=')
    sh "tar czf /tmp/${name}-${commit}-${build_id}.tar.gz -C . --exclude=$options ."
}

def packageAndUpload(Map vars, String options) {
    def app_name = getAppName()
    createArtifacts()
    createArchive(app_name, vars.app_commit, env.BUILD_ID, options)
    moveArchiveInProjet([
        name: app_name,
        commit: vars.app_commit,
        build_id: env.BUILD_ID
    ])
    sendToNexus([
        name: app_name,
        commit: vars.app_commit,
        build_id: env.BUILD_ID,
        build_type: vars.build_type,
        version: [
            major: vars.major,
            minor: vars.minor,
            patch: vars.patch
        ]
    ])
}

def clean(String[] folders = []) {
    for(int i=0; i < folders.size(); i++) {
        sh "rm -Rf ${folders[i]} || true"
    }
}
