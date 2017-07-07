#!/usr/bin/groovy
package io.indb;

def composerInstall(Map args) {
    title = 'Composer package installation'
    stage(title) {
        println title
        cmd = 'composer install'

        if (args.noDev) {
            cmd = cmd + ' --no-dev'
        }

        if (!args.debug) {
            cmd = cmd + ' --no-progress'
        }

        sh cmd
    }
}

def getVersion() {
    title = 'Composer version'
    stage(title) {
        println title
        sh 'composer --version'
    }
}