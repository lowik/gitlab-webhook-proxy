Small groovy http proxy server used to filter gitlab webhooks

Used to avoid re-build a Jenkins job when Jenkins push a commit (package.json updated by npm version)

For now, only works with push webhook and specific commit message


# Run the proxy server

Before running, set the target host and other variables in webhook-proxy.groovy
```sh
$ npm start
```

# Configure gitlab

In your project settings, set the proxy hostname with the path of the final target

i.e: replace http://jenkins/git/notifyCommit?url=git@repo:dev/project.git
by
http://localhost:9090/git/notifyCommit?url=git@repo:dev/project.git


# Configure jenkins

## Working with pipeline

Job PUBLISH depending of success of job TESTS


### Job TESTS

Configure source code
* Set the Git repository
* Add "Check out to specific local branch" with the correct branch name

Configure Archive workspace
* Check "Override Default Ant Excludes"
* And so configure "Files to exclude from cloned workspace" without .git directory
i.e: node_modules/**/*,.gradle/**/*

### Job PUBLISH

Configure source code
* Clone workspace job TESTS

Publish task
* Must push the new version with specific message starting with "[jenkins-release] "

### Encountered problems

npm publish don't work because repository is not clean caused by a gradlew chmod diff (jenkins make gradlew as executable)

```sh
$ git update-index --add --chmod=+x gradlew
```
