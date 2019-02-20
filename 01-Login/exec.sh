#!/usr/bin/env bash
docker build -t auth0-samples/auth0-spring-security-mvc-01-login .
docker run -p 3001:3001 -it auth0-samples/auth0-spring-security-mvc-01-login
