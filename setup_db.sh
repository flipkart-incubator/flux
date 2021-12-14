#!/bin/bash
set -e
set -x
echo "drop database if exists flux" | mysql -uroot
echo "create database flux" | mysql -uroot
mvn -pl :persistence-mysql liquibase:update -Dliquibase-maven-plugin.dbname=flux
echo "drop database if exists flux_scheduler" | mysql -uroot
echo "create database flux_scheduler" | mysql -uroot
mvn -pl :persistence-mysql liquibase:update -Dliquibase-maven-plugin.dbname=flux_scheduler
