#!/bin/bash
javac -cp ".:lib/*" src/**/*.java
java -cp ".:lib/*:src" ui.StudentGradeManagementSystem
