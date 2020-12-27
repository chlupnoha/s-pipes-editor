#!/bin/bash 

if [ ! "$#" -eq 1 ]; then
        echo Validate shapes and data within provided directory.  
        echo Usage :  $0 "DIRECTORY" 
        echo Example: $0 01
        exit
fi

DIR=$1

DATA_FILE=$DIR/data.ttl
TMP_FILE=`tempfile`


shacl validate --shapes form.shapes.ttl --data $DIR/data.ttl | tee $DIR/validation-result.txt

