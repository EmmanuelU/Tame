#!/bin/sh
#DO NOT EDIT FILE - AUTO GENERATED
latestversion=2.1
latestversioncode=12
latestdateliteral='August 16 2015'
latestDL=https://raw.githubusercontent.com/EmmanuelU/Tame/master/releases/Tame.apk
input="$1"
if [[ -z "$input" ]]
	then
	exit 1
fi

if [[ "$input" == "latestversion" ]]
	then
	echo $latestversion

elif [[ "$input" == "latestversioncode" ]]
	then
	echo $latestversioncode

elif [[ "$input" == "latestDL" ]]
	then
	echo $latestDL
elif [[ "$input" == "latestdateliteral" ]]
	then
	echo $latestdateliteral
fi

# echo ${!input} Apparently not all roms have bash installed ._.
