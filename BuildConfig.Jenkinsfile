@Library('merritt-build-library')
import org.cdlib.mrt.build.BuildFunctions;

// See https://github.com/CDLUC3/mrt-jenkins/blob/main/src/org/cdlib/mrt/build/BuildFunctions.groovy

pipeline {
    /*
     * Params:
     *   branch
     *   build-config
     *   maven-profile
     */
    environment {      
      //working vars
      M2DIR = "${HOME}/.m2-buildall"
      BUILDDIR = "$WORKSPACE"
    }
    agent any

    tools {
        // Install the Maven version 3.8.4 and add it to the path.
        maven 'maven384'
    }

    stages {
        stage('Purge Local') {
            steps {
                script {
                  new BuildFunctions().init_build();
                }
            }
        }
        stage('Get Code') {
            steps {
                script {
                    git branch: 'main', url: "https://github.com/CDLUC3/merritt-docker.git"
                    sh("bin/fresh_build.sh ${params.branch} ${params.build_conifig} ${params.maven_profile}")
                }
            }
        }
    }
}
