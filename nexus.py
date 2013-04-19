#!/usr/bin/python

import glob
import os
import string
import sys

repo_url = 'http://10.0.3.179:8081/nexus/content/repositories/releases'
repository = 'releases'

def installdir(path):
	installjar('%s/*.jar' % path)

def installjar(path):
	if '*' in path:
		jars = glob.glob(path)
	else:
		jars = [path]

	for jar in jars:
		installonejar(jar)

def installonejar(jar):
	realpath = os.path.realpath(jar)
	tokens = jar.split('/')[-1][0:-4].split('-')
	group = 'local'
	artifact = string.join(tokens[0:-1], '-')
	version = tokens[-1]
	cmd = 'mvn deploy:deploy-file -DgroupId=%s -DartifactId=%s -Dversion=%s ' \
			'-Dpackaging=jar -Dfile=%s -Durl=%s -DrepositoryId=%s '  \
			% (group, artifact, version, realpath, repo_url, repository)
	print cmd
	os.system(cmd)

if __name__ == "__main__":
	command = sys.argv[1]
	path = sys.argv[2]
	if command == 'installdir':
		installdir(path)
	elif command == 'installjar':
		installjar(path)


