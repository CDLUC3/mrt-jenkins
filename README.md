# Jenkins Build File for the Merritt System

This repository supports the [Merritt Preservation System](https://github.com/CDLUC3/mrt-doc).

## Purpose

Build file for consistently creating Merritt War files

- [Build module](src/org/cdlib/mrt/build/BuildFunctions.groovy)

## Jenkins Configuration

### Global Pipeline Libraries

- Project Repo: https://github.com/CDLUC3/mrt-jenkins.git
- Shared Library Name: `merritt-build-library`

### Environment Variables
- AWS_ACCOUNT_ID
- AWS_REGION
- ECR_REGISTRY

### Key Plugins

- enable pipelines
- enable groovy pipelines
- enable git
- enable git parameters
