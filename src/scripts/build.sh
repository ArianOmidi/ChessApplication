#!/bin/bash

build(){
	curdir=`pwd`

	cd /Users/arianomidi/Dropbox/Personal/Code/Java/JChess/Chess
	
	#if [[ -d build ]]; then
	#	rm -r build
	#fi
	
	cp -r src/main/java/ build/
	cp -r resources build/
	
	cd build
	javac JChess.java

	cd $curdir
}

chess(){
	curdir=`pwd`

	cd /Users/arianomidi/Dropbox/Personal/Code/Java/JChess/Chess/build

	java JChess
	
	cd $curdir
}

#build
