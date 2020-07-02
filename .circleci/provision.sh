#!/bin/bash

## Detect which modules have changed
    ## look for git tag to indicate that a re-build is required

    ## infra/stacks/api  -> rebuild api + application module
    ## infra/stacks/frontend -> rebuild frontend module
    ## infra/stacks/data -> rebuild api, application, data.  Perform backup / restore

