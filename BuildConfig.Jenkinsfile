@Library('merritt-build-library')
import org.cdlib.mrt.build.BuildFunctions;

// See https://github.com/CDLUC3/mrt-jenkins/blob/main/src/org/cdlib/mrt/build/BuildFunctions.groovy

pipeline {
    /*
     * Params:
     *   branch
     *   build_config
     *   maven_profile
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
        stage('Run Build Script') {
            steps {
                script {
                    sh("mvn --version")
                    sh("pip3 install pyyaml")
                    git branch: 'main', url: "https://github.com/CDLUC3/merritt-docker.git"
                    sh("bin/fresh_build.sh ${params.branch} ${params.build_config} ${params.maven_profile}")
                    archiveArtifacts \
                      artifacts: "build-log.summary.txt, build-log.git.txt, build-log.docker.txt, \
                        build-log.trivy-scan.txt, build-log.trivy-scan-fixed.txt, build-log.maven.txt"
                }
            }
        }
    }
}
