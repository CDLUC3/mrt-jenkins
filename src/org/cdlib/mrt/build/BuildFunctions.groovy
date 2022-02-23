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

def init_build() {
  script {
    if (params.containsKey("branch")) {
      sh("echo 'Building branch ${params.branch}' > build.current.txt")
    } else if (params.containsKey("tagname")) {
      sh("echo 'Building tag ${params.tagname}' > build.current.txt")
    }
    sh("date >> build.current.txt")
    sh("echo '' >> build.current.txt")
    sh("echo 'Purge ${env.M2DIR}: ${params.remove_local_m2}'")
    if (params.remove_local_m2.toBoolean()) {
      sh("rm -rf ${env.M2DIR}")
    }
  }
}
  
def build_library(repo, branch, mvnparams){
  script {
    git branch: branch, url: repo
    sh("git remote get-url origin >> ../build.current.txt")
    sh("git symbolic-ref -q --short HEAD >> ../build.current.txt || git describe --tags --exact-match >> ../build.current.txt")
    sh("git log --pretty=full -n 1 >> ../build.current.txt")
    sh("mvn -Dmaven.repo.local=${env.M2DIR} -s ${MAVEN_HOME}/conf/settings.xml clean install ${mvnparams}")
  }
}

def build_war(repo) {
  script {   
    git branch: env.DEF_BRANCH, url: repo
    sh "git remote get-url origin >> ../build.current.txt"
    if (params.containsKey("branch")) {
      checkout([
        $class: 'GitSCM',
        branches: [[name: "${params.branch}"]],
      ])
      sh "git symbolic-ref -q --short HEAD >> ../build.current.txt || git describe --tags >> ../build.current.txt"
    } else if (params.containsKey("tagname")) {
      checkout([
        $class: 'GitSCM',
        branches: [[name: "refs/tags/${params.tagname}"]],
      ])
      sh "git symbolic-ref -q --short HEAD >> ../build.current.txt || git describe --tags --exact-match >> ../build.current.txt"
    }
    sh "git log --pretty=medium -n 1 >> ../build.current.txt"
    sh "mvn -Dmaven.repo.local=${env.M2DIR} -s ${MAVEN_HOME}/conf/settings.xml clean install"
  }
}

def save_artifacts(path, prefix){
  script {
    def twar = "${prefix}.war"
    def tlabel = ""
    if (params.containsKey("branch")) {
      tlabel = branch.replaceFirst(/origin\//, '')
    } else {
      tlabel = tagname
    }
    twar = "${prefix}-${tlabel}.war"
    sh "cp build.current.txt ${tlabel}"
    sh "mkdir -p WEB-INF"
    sh "cp build.current.txt WEB-INF"
    sh "cp ${path} ${twar}"
    sh "jar uf ${twar} WEB-INF/build.current.txt"
    archiveArtifacts \
      artifacts: "${tlabel}, build.current.txt, ${twar}"
      onlyIfSuccessful: true
    } 
  }
}
