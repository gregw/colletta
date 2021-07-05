#!/bin/sh
egrep 'res.history.*Payment' $(find reservations -name 'current.res' ) | sed -e 's/\+/ /g' -e's/%3A/:/g' | cut -c14-21,49- | sed 's/\([^ ]*\) \([^ ]*\) \([^ ]*\) \(.*\)$/\2 \1 \4/' | sort
