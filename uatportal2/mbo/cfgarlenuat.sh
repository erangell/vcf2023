#! /bin/sh

for i in `ls -1 *.htm`
do
  cp $i $i.bki
  sed 's#http://localhost/uatportal1#https://arlen.music.udel.edu/uatportal1#g' $i > $i.new
  mv $i.new $i
  echo "$i is done!"
done
