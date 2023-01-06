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
    sh("mkdir -p static")
    def build_txt = 'static/build.content.txt'
    def deps_txt = 'static/dependencies.txt'
    if (params.containsKey("branch")) {
      sh("echo 'Building branch ${params.branch}' > ${build_txt}")
    } else if (params.containsKey("tagname")) {
      sh("echo 'Building tag ${params.tagname}' > ${build_txt}")
    } else {
      sh("echo 'Running specialized build' > ${build_txt}")
    } 
    sh("date >> ${build_txt}")
    sh("echo '' >> ${build_txt}")
    sh("echo 'Purge ${env.M2DIR}: ${params.remove_local_m2}'")
    if (params.remove_local_m2.toBoolean()) {
      sh("rm -rf ${env.M2DIR}")
    }
   
    sh("newgrp docker")
    sh("id")
    sh("echo 'ECR REG: ${env.ECR_REGISTRY}'")
    sh("aws ecr get-login-password --region ${env.AWS_REGION} | docker login --username AWS --password-stdin ${env.ECR_REGISTRY}")
  }
}
  
def build_library(repo, branch, mvnparams){
  script {
    def build_txt = '../static/build.content.txt'
    git branch: branch, url: repo
    sh("git remote get-url origin >> ${build_txt}")
    sh("git symbolic-ref -q --short HEAD >> ${build_txt} || git describe --tags --exact-match >> ${build_txt}")
    sh("git log --pretty=full -n 1 >> ${build_txt}")
    sh("mvn -Dmaven.repo.local=${env.M2DIR} -s ${MAVEN_HOME}/conf/settings.xml clean install ${mvnparams}")
  }
}

def deps_library(repo, branch){
  script {
    def deps_txt = '../static/dependencies.txt'
    git branch: branch, url: repo
    sh("mvn -Dmaven.repo.local=${env.M2DIR} -s ${MAVEN_HOME}/conf/settings.xml clean dependency:analyze-only | egrep 'WARNING|INFO..Building' >> ${deps_txt}")
  }
}

def build_war(repo, mvnparams) {
  script {   
    def build_txt = '../static/build.content.txt'
    git branch: env.DEF_BRANCH, url: repo
    sh "git remote get-url origin >> ${build_txt}"
    if (params.containsKey("branch")) {
      checkout([
        $class: 'GitSCM',
        branches: [[name: "${params.branch}"]],
      ])
      sh "git symbolic-ref -q --short HEAD >> ${build_txt} || git describe --tags >> ${build_txt}"
    } else if (params.containsKey("tagname")) {
      checkout([
        $class: 'GitSCM',
        branches: [[name: "refs/tags/${params.tagname}"]],
      ])
      sh "git symbolic-ref -q --short HEAD >> ${build_txt} || git describe --tags --exact-match >> ${build_txt}"
    }
    sh "git log --pretty=medium -n 1 >> ${build_txt}"
    sh "mvn -Dmaven.repo.local=${env.M2DIR} -s ${MAVEN_HOME}/conf/settings.xml clean install -Ddocker.imagePullPolicy=Always ${mvnparams}"
  }
}

def deps_war(repo, branch) {
  script {   
    def deps_txt = '../static/dependencies.txt'
    git branch: branch, url: repo
    checkout([
      $class: 'GitSCM',
      branches: [[name: branch]],
    ])
    sh "mvn -Dmaven.repo.local=${env.M2DIR} -s ${MAVEN_HOME}/conf/settings.xml clean dependency:analyze-only | egrep 'WARNING|INFO..Building' >> ${deps_txt}"
  }
}

def build_and_test_war(repo, branch, mvnparams) {
  script {   
    def build_txt = '../static/build.content.txt'
    git branch: branch, url: repo
    sh "git remote get-url origin >> ${build_txt}"
    checkout([
      $class: 'GitSCM',
      branches: [[name: branch]],
    ])
    sh "git symbolic-ref -q --short HEAD >> ${build_txt} || git describe --tags >> ${build_txt}"
    sh "git log --pretty=medium -n 1 >> ${build_txt}"
    sh "mvn -Dmaven.repo.local=${env.M2DIR} -s ${MAVEN_HOME}/conf/settings.xml clean install -Ddocker.imagePullPolicy=Always ${mvnparams}"
  }
}

def save_build_info() {
  script {
    def build_txt = 'static/build.content.txt'
    archiveArtifacts \
      artifacts: "${build_txt}"
      onlyIfSuccessful: true
  }
}

def save_deps_info() {
  script {
    def deps_txt = 'static/dependencies.txt'
    archiveArtifacts \
      artifacts: "${deps_txt}"
      onlyIfSuccessful: true
    sh "cat ${deps_txt}"
  }
}

def save_artifacts(path, prefix){
  script {
    def build_txt = 'static/build.content.txt'
    def twar = "${prefix}.war"
    def tlabel = ""
    if (params.containsKey("branch")) {
      tlabel = branch.replaceFirst(/origin\//, '')
    } else {
      tlabel = tagname
    }
    twar = "${prefix}-${tlabel}.war"
    sh "cp ${path} ${twar}"
    sh "jar uf ${twar} ${build_txt}"
    archiveArtifacts \
      artifacts: "${build_txt}, ${twar}"
      onlyIfSuccessful: true
    if (params.containsKey("tagname")) {
      sh "mkdir -p ${JENKINS_HOME}/userContent/${prefix}"
      sh "cp ${twar} ${JENKINS_HOME}/userContent/${prefix}"
    }
  }
}

def save_jars(path, prefix){
  script {
    def build_txt = 'static/build.content.txt'
    def tjar = "${prefix}.jar"
    def tlabel = ""
    if (params.containsKey("branch")) {
      tlabel = branch.replaceFirst(/origin\//, '')
    } else {
      tlabel = tagname
    }
    tjar = "${prefix}-${tlabel}.jar"
    sh "cp ${path} ${tjar}"
    archiveArtifacts \
      artifacts: "${build_txt}, ${tjar}"
      onlyIfSuccessful: true
    if (params.containsKey("tagname")) {
      sh "mkdir -p ${JENKINS_HOME}/userContent/${prefix}"
      sh "cp ${tjar} ${JENKINS_HOME}/userContent/${prefix}"
    }
  }
}
