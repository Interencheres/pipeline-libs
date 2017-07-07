#!/usr/bin/groovy
package io.indb;

def npmInstall(Map args) {
    title = 'NodeJS package installation'
    stage(title) {
        println title
        cmd = 'npm install --no-progress'

        if (args.production) {
            cmd = cmd + ' --production'
        }

        if (!args.debug) {
            cmd = cmd + ' --silent'
        }

        sh cmd
    }
}

def getVersions() {
    stage('Output versions') {
        println 'NodeJS version'
        sh 'node --version'
        println 'NPM version'
        sh 'npm --version'
    }
}