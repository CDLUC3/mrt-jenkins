/*
Pre-reqs
 
Build Params
- one of the following should be set
  - branch (for dev builds)
  - tagname (for deploy builds)
- remove_local_m2 (to clear maven cache)

Environment
- BRANCH_XXX - branch to use for library dependencies
  - BRANCH_CORE
  - BRANCH_CLOUD
- DEF_BRANCH - default branch for the service repo (TBD - determine if needed)
- M2DIR - the m2 directory that is unique for each service being built
*/

def static init_build() {
  script {
    if (params.containsKey("branch")) {
      echo 'Building branch ${params.branch}' > build.current.txt
    } else if (params.containsKey("tagname")) {
      echo 'Building tag ${params.tagname}' > build.current.txt
    }
    date >> build.current.txt
    echo '' >> build.current.txt
    echo 'Purge ${env.M2DIR}: ${params.remove_local_m2}'
    if (params.remove_local_m2.toBoolean()) {
      sh("rm -rf ${env.M2DIR}")
    }
  }
}
  
def static build_library(repo, branch, mvnparams){
  script {
    sh("git branch: ${branch}, url: ${repo}")
    sh("git remote get-url origin >> ../build.current.txt")
    sh("git symbolic-ref -q --short HEAD >> ../build.current.txt || git describe --tags --exact-match >> ../build.current.txt")
    sh("git log --pretty=full -n 1 >> ../build.current.txt")
    sh("mvn -Dmaven.repo.local=${env.M2DIR} -s ${MAVEN_HOME}/conf/settings.xml clean install ${mvnparams}")
  }
}

def static build_core() {
  script {   
   BuildFunctions.build_library('https://github.com/CDLUC3/mrt-core2.git', ${env.BRANCH_CORE}, '-DskipTests')
  }
}
