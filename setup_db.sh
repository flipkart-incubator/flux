#!/bin/bash
set -e
set -x
echo "drop database if exists flux" | mysql -uroot
echo "create database flux" | mysql -uroot
mvn -pl :persistence-mysql liquibase:update
echo "drop database if exists flux_redriver" | mysql -uroot
echo "create database flux_redriver" | mysql -uroot
mvn -pl :persistence-mysql liquibase:update -Dliquibase-maven-plugin.dbname=flux_redriver
