#!/bin/bash
nuxt generate

if [ -x "inotifywait"  ]; then
  echo "inotifywait is not available, will try to install assuming ubuntu / debian"
  sudo apt-get install -y inotify-tools
fi

while true; do

inotifywait -e modify,create,delete -r ./ --exclude '^\./\.'  && \
nuxt generate

done
