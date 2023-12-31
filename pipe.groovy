tenant="pessoal"
stage="teste"
def node_version = "16"
def git_url = "https://github.com/rafaelportomoura"
def scm_cron = "H/05 * * * *"
def git_branch = 'origin/master'
def env_disable_pipes = false

pipe_folder = """$tenant/$stage"""

def init_log="""echo $tenant
echo $stage
"""
def source_nvm = """set +x
export NVM_DIR="\$HOME/.nvm"
[ -s "\$NVM_DIR/nvm.sh" ] && \\. "\$NVM_DIR/nvm.sh" 
source \$NVM_DIR/nvm.sh --no-use > /dev/null
nvm install $node_version > /dev/null
node -v
npm i yarn -g
set -x
"""
def typescript_build="""yarn install --ignore-engines
find ./node_modules -mtime +10950 -exec touch {} +
yarn dev
yarn compile:server
rm -rf ./node_modules
yarn install --ignore-engines --production=true
yarn prod
"""

folder("""$tenant""") {
    displayName('Pessoal')
}

folder("""$pipe_folder""") {
    displayName('Teste')
}



job("""$pipe_folder/init""") {
    disabled(Boolean.valueOf("""$env_disable_pipes"""))
  parameters {
    
  }
  triggers{
    scm("""$scm_cron""")
  }
  scm {
        git {
            remote {
                url("""$git_url/jenkins-pipelines.git""")
            }
            branch("""$git_branch""")
        }
    }
  wrappers {
        preBuildCleanup {
            // includePattern('**/target/**')
            deleteDirectories()
            cleanupParameter('CLEANUP')
        }
    }
  steps {
    shell("""
$init_log

$source_nvm

$typescript_build

	""")
  }
  publishers {
        cleanWs { // Clean after build
            cleanWhenAborted(true)
            cleanWhenFailure(true)
            cleanWhenNotBuilt(true)
            cleanWhenSuccess(true)
            cleanWhenUnstable(true)
            deleteDirs(true)
            notFailBuild(true)
            // disableDeferredWipeout(true)
            // patterns {
            //     pattern {
            //         type('EXCLUDE')
            //         pattern('.propsfile')
            //     }
            //     pattern {
            //         type('INCLUDE')
            //         pattern('.gitignore')
            //     }
            // }
        }
    }
}