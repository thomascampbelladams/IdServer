for f in $@
do
  echo $f
  sqlite3 $f < query > $f.diffme
  diff $1.diffme $f.diffme
done
rm *.diffme
