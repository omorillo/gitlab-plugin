# Migration to plugin version **1.2.x**

Migrating from plugin version **1.1.x** to **1.2.x** requires only a few steps to be explained below.

:warning: Upgrading to plugin version **1.2.x** may cause configuration loss. It is highly recommended to backup existing Jenkins job configurations before continuing with the following steps!

* Upgrade to latest `GitLab plugin` version through Jenkins' plugin manager
* If integration targets a `GitLab` version >= **8.1**, you need to add a post-build step ``Publish build status to GitLab commit (GitLab 8.1+ required)`` to the job in order to enable build status publishing.
* In **1.2.x** the setup for manual paramerized builds, manual triggering has changed. Please follow the instructions in "TODO" on how to configure "EnvInjectPlugin":https://wiki.jenkins-ci.org/display/JENKINS/EnvInject+Plugin for manual triggering.





