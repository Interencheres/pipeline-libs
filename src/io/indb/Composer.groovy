#!/usr/bin/groovy
package io.indb;

def install(Map args) {
    println 'Composer package installation'
    cmd = 'composer install'

    if (args.noDev) {
        cmd = cmd + ' --no-dev'
    }

    if (!args.debug) {
        cmd = cmd + ' --no-progress'
    }

    sh cmd
}

def getVersion() {
    println 'Composer version'
    sh 'composer --version'
}