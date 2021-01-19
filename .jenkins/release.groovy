import groovy.json.JsonSlurper

node {
    checkout()
    build()
    deploy()
    planInfrastructure()
    cleanWorkspace()
}

def checkout() {
    stage('Checkout') {
        git branch: 'master',
        credentialsId: 'fdb088af-f26a-47e8-b109-b49857442ef3',
        url:'git@github.com:CurrencyFair/xsapi'
    }
}

def build() {
   stage('Build') {
     sh 'npm i'
     sh 'mkdir -p ./dist'
     sh 'npm run build:docs'
   }
}

def deploy() {
    stage('Deploy to s3') {
        sh 'aws s3 sync ./dist s3://cf-production-xsapi-reference-origin'
    }
}

def cleanWorkspace() {
    stage('Cleanup Workspace') {
        cleanWs()
    }
}

def planInfrastructure() {
    stage('Run Terraform Plan') {
        def runid = ""
        getWorkspaceId()
//         buildPayload()
//
//         runid = startPlan()
//
//         waitForPlan(runid)
    }
}

def getWorkspaceId() {
    withCredentials([string(credentialsId: 'jenkins-terraform-cloud', variable: 'JENKINSTERRAFORMCLOUD')]) {
        def response = httpRequest(
            customHeaders: [
                    [ name: "Authorization", value: "Bearer ${JENKINSTERRAFORMCLOUD}"],
                    [ name: "Content-Type", value: "application/vnd.api+json" ]
                ],
            url: "https://app.terraform.io/api/v2/organizations/CurrencyFair/workspaces/engineering"
        )
    }

    def data = new JsonSlurper().parseText(response.content)
    println ("Workspace Id: " + data.data.id)
    return data.data.id
}
