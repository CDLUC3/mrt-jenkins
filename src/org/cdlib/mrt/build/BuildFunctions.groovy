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
      sh("echo 'Building branch ${params.branch}' > build.content.txt")
    } else if (params.containsKey("tagname")) {
      sh("echo 'Building tag ${params.tagname}' > build.content.txt")
    }
    sh("date >> build.content.txt")
    sh("echo '' >> build.content.txt")
    sh("echo 'Purge ${env.M2DIR}: ${params.remove_local_m2}'")
    if (params.remove_local_m2.toBoolean()) {
      sh("rm -rf ${env.M2DIR}")
    }
  }
}
  
def build_library(repo, branch, mvnparams){
  script {
    git branch: branch, url: repo
    sh("git remote get-url origin >> ../build.content.txt")
    sh("git symbolic-ref -q --short HEAD >> ../build.content.txt || git describe --tags --exact-match >> ../build.content.txt")
    sh("git log --pretty=full -n 1 >> ../build.content.txt")
    sh("mvn -Dmaven.repo.local=${env.M2DIR} -s ${MAVEN_HOME}/conf/settings.xml clean install ${mvnparams}")
  }
}

def build_war(repo, mvnparams) {
  script {   
    git branch: env.DEF_BRANCH, url: repo
    sh "git remote get-url origin >> ../build.content.txt"
    if (params.containsKey("branch")) {
      checkout([
        $class: 'GitSCM',
        branches: [[name: "${params.branch}"]],
      ])
      sh "git symbolic-ref -q --short HEAD >> ../build.content.txt || git describe --tags >> ../build.content.txt"
    } else if (params.containsKey("tagname")) {
      checkout([
        $class: 'GitSCM',
        branches: [[name: "refs/tags/${params.tagname}"]],
      ])
      sh "git symbolic-ref -q --short HEAD >> ../build.content.txt || git describe --tags --exact-match >> ../build.content.txt"
    }
    sh "git log --pretty=medium -n 1 >> ../build.content.txt"
    sh "mvn -Dmaven.repo.local=${env.M2DIR} -s ${MAVEN_HOME}/conf/settings.xml clean install ${mvnparams}"
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
    sh "mkdir -p WEB-INF"
    sh "cp build.content.txt WEB-INF"
    sh "cp ${path} ${twar}"
    sh "jar uf ${twar} WEB-INF/build.content.txt"
    archiveArtifacts \
      artifacts: "build.content.txt, ${twar}"
      onlyIfSuccessful: true
    if (params.containsKey("tagname")) {
      sh "mkdir -p ${JENKINS_HOME}/userContent/${prefix}"
      sh "cp ${twar} ${JENKINS_HOME}/userContent/${prefix}"
    }
  }
}
