#!/usr/bin/groovy
package io.indb;

def pipe(command){
    sh(script: command, returnStdout: true)
}

return this