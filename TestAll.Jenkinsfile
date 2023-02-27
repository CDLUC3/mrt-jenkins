@Library('merritt-build-library')
import org.cdlib.mrt.build.BuildFunctions;

// See https://github.com/CDLUC3/mrt-jenkins/blob/main/src/org/cdlib/mrt/build/BuildFunctions.groovy

pipeline {
    /*
     * Params:
     *   tagname
     *   branch_core
     *   branch_cloud
     *   branch_zk
     *   branch_mrtzoo
     *   branch_inv
     *   branch_audit
     *   branch_replic
     *   branch_store
     *   branch_ingest
     *   purge_local_m2
     */
    environment {      
      //working vars
      M2DIR = "${HOME}/.m2-replic"
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
        stage('Build Core') {
            steps {
                dir('mrt-core2') {
                  script {
                    new BuildFunctions().build_library(
                      'https://github.com/CDLUC3/mrt-core2.git', 
                      params.branch_core, 
                      ''
                    )
                  }
                }
            }
        }
        stage('Build Cloud') {
            steps {
                dir('mrt-cloud') {
                  script {
                    new BuildFunctions().build_library(
                      'https://github.com/CDLUC3/mrt-cloud.git', 
                      params.branch_cloud, 
                      ''
                    )
                  }
                }
            }
        }
        stage('Build CDL ZK Queue') {
            steps {
                dir('cdl-zk-queue') {
                  script {
                    new BuildFunctions().build_library(
                      'https://github.com/CDLUC3/cdl-zk-queue.git', 
                      params.branch_zk, 
                      ''
                    )
                  }
                }
            }
        }
        stage('Build MRT Zoo') {
            steps {
                dir('mrt-zoo') {
                  script {
                    new BuildFunctions().build_library(
                      'https://github.com/CDLUC3/mrt-zoo.git', 
                      params.branch_mrtzoo, 
                      ''
                    )
                  }
                }
            }
        }
        stage('Build Inventory') {
            steps {
                dir('mrt-inventory'){
                  script {
                    new BuildFunctions().build_and_test_war(
                      'https://github.com/CDLUC3/mrt-inventory.git',
                      params.branch_inv, 
                      ''
                    )
                    new BuildFunctions().save_artifacts(
                      'mrt-inventory/inv-war/target/mrt-invwar-1.0-SNAPSHOT.war',
                      'mrt-inventory'
                    )
                  }
                }
            }
        }
        stage('Build Replic') {
            steps {
                dir('mrt-replic'){
                  script {
                    new BuildFunctions().build_and_test_war(
                      'https://github.com/CDLUC3/mrt-replic.git',
                      params.branch_replic, 
                      ''
                    )
                    new BuildFunctions().save_artifacts(
                      'mrt-replic/replication-war/target/mrt-replicationwar-1.0-SNAPSHOT.war',
                      'mrt-replic'
                    )
                  }
                }
            }
        }
        stage('Build Audit') {
            steps {
                dir('mrt-audit'){
                  script {
                    new BuildFunctions().build_and_test_war(
                      'https://github.com/CDLUC3/mrt-audit.git',
                      params.branch_audit, 
                      ''
                    )
                    new BuildFunctions().save_artifacts(
                      'mrt-audit/audit-war/target/mrt-auditwarpub-1.0-SNAPSHOT.war',
                      'mrt-audit'
                    )
                  }
                }
            }
        }
        stage('Build Store') {
            steps {
                dir('mrt-store'){
                  script {
                    new BuildFunctions().build_and_test_war(
                      'https://github.com/CDLUC3/mrt-store.git',
                      params.branch_store, 
                      ''
                    )
                    new BuildFunctions().save_artifacts(
                      'mrt-store/store-war/target/mrt-storewar-1.0-SNAPSHOT.war',
                      'mrt-store'
                    )
                  }
                }
            }
        }
        stage('Build Ingest') {
            steps {
                dir('mrt-ingest'){
                  script {
                    new BuildFunctions().build_and_test_war(
                      'https://github.com/CDLUC3/mrt-ingest.git',
                      params.branch_ingest, 
                      '-Denforcer.skip=true'
                    )
                    new BuildFunctions().save_artifacts(
                      'mrt-ingest/ingest-war/target/mrt-ingestwar-1.0-SNAPSHOT.war',
                      'mrt-ingest'
                    )
                 }
                }
            }
        }
        stage('Archive Build Info') { // for display purposes
            steps {
                script {
                  new BuildFunctions().save_build_info()
                }
            }
        }
    }
}
