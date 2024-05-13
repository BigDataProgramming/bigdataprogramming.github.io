#!/bin/bash
mkdir -p /opt/examples/4.3.3.2/old
for F in /opt/examples/4.3.3.2/*.tgz; do
  [[ -e $F ]] && mv "$F" /opt/examples/4.3.3.2/old/;
done
echo $? # Force print 0 if no tgz files exist