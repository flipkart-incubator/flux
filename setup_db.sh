#!/bin/bash
set -e
set -x
echo "drop database if exists flux" | mysql -uroot
echo "create database flux" | mysql -uroot
mvn -pl :persistence-mysql liquibase:update
