#!/usr/bin/groovy
package io.indb;

def getShell() {
    new Shell()
}

def getCommitHash(String commit = "HEAD", boolean full = false) {
    if (full) {
        return getShell().pipe("git rev-parse ${commit}")
    } else {
        return getShell().pipe("git rev-parse ${commit}").substring(0,6)
    }
}

def getCurrentBranch(boolean sanitize = false) {
    if (sanitize) {
        return getShell().pipe('git rev-parse --abbrev-ref HEAD').replaceAll("[^A-Za-z0-9]", "-")
    } else {
        return getShell().pipe('git rev-parse --abbrev-ref HEAD')
    }
    
}

return this