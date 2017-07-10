#!/usr/bin/groovy
package io.indb;

def npmInstall(Map args) {
    println 'NodeJS package installation'
    cmd = 'npm install --no-progress'

    if (args.production) {
        cmd = cmd + ' --production'
    }

    if (!args.debug) {
        cmd = cmd + ' --silent'
    }

    sh cmd
}

def getVersions() {
    println 'NodeJS version'
    sh 'node --version'
    println 'NPM version'
    sh 'npm --version'
}