
Below is the command history of building/updating the docker volumes. 


  347  docker pull jetty
  348  docker
  349  sudo apt install docker.io
  350  sudo bash
  351  git status
  352  git diff
  353  cd src/colletta/
  354  ls
  355  ls -l
  356  tar cfvz jetty-base-20210603.tgz jetty-base
  357  jdk8
  358  java -version
  359  cd jetty-base/
  360  ls
  361  ls start.d/
  362  java -jar /opt/jetty-distribution-9.3.29.v20201019/start.jar 
  363  ls
  364  vi webapps/booking.colletta.it.xml 
  365  vi webapps/info.colletta.it.xml 
  366  java -jar /opt/jetty-distribution-9.3.29.v20201019/start.jar 
  367  docker images
  368  docker ps
  369  docker run -it --rm jetty:latest
  370  docker run -it --rm jetty:latest bash
  371  ls start.d/
  372  ls
  373  ls /opt
  374  cd ..
  375  ls
  376  mv jetty-base jetty-base-20210603
  377  mkdir jetty-base
  378  cd jetty-base
  379  ls
  380  jdk11
  381  java -jar /opt/jetty-distribution-9.4.42.v20210604/start.jar --create-startd
  382  java -jar /opt/jetty-distribution-9.4.42.v20210604/start.jar --add-to-start=deploy,http,jsp,jstl,requestlog,server
  383  ls
  384  mv ../jetty-base-20210603/webapps/* webapps/
  385  ls
  386  java -jar /opt/jetty-distribution-9.4.42.v20210604/start.jar 
  387  java -jar /opt/jetty-distribution-9.4.42.v20210604/start.jar --add-to-start=deploy,http,jsp,jstl,requestlog,server,servlets
  388  java -jar /opt/jetty-distribution-9.4.42.v20210604/start.jar 
  389  docker run -it --rm jetty:latest bash
  390  docker run -it --rm -p 8080:8080 jetty:latest 
  391  pwd
  392  docker run -it --rm -p 8080:8080 --mount type=bind,src=/home/gregw/src/colletta/jetty-base,dst=/var/lib/jetty jetty:latest bash
  393  docker run -it --rm -p 8080:8080 --mount type=bind,src=/home/gregw/src/colletta/jetty-base,dst=/var/lib/jetty jetty:latest
  394  docker run -it --rm -p 8080:8080 --mount type=bind,src=/home/gregw/src/colletta/jetty-base,dst=/var/lib/jetty jetty:latest bash
  395  docker run -it --rm -p 8080:8080 -v /home/gregw/src/colletta/jetty-base:/var/lib/jetty jetty:latest bash
  396  docker volume create colletta-vol
  397  docker volume ls
  398  docker run -it --rm -p 8080:8080 -v /home/gregw/src/colletta/jetty-base:/var/lib/jettyOld -mount type=volume,src=colletta-vol,dst=/var/lib/jettyNew  jetty:latest bash
  399  docker run -it --rm -p 8080:8080 -v /home/gregw/src/colletta/jetty-base:/var/lib/jettyOld --mount type=volume,src=colletta-vol,dst=/var/lib/jettyNew  jetty:latest bash
  400  docker run -it --rm -p 8080:8080 -v /home/gregw/src/colletta/jetty-base:/var/lib/jettyOld --mount type=volume,src=colletta-vol,dst=/var/lib/jettyNew --user 0 jetty:latest bash
  401  docker run -it --rm -p 8080:8080 -v /home/gregw/src/colletta/jetty-base:/var/lib/jettyOld --mount type=volume,src=colletta-vol,dst=/var/lib/jettyNew jetty:latest bash
  402  docker run -it --rm -p 8080:8080 --mount type=volume,src=colletta-vol,dst=/var/lib/jetty-base jetty:latest
  403  docker run -it --rm -p 8080:8080 --mount type=volume,src=colletta-vol,dst=/var/lib/jetty-base jetty:latest bash
  404  docker run -it --rm -p 8080:8080 --mount type=volume,src=colletta-vol,dst=/var/lib/jetty-base debian:latest bash
  405  docker run -it --rm -p 8080:8080 --mount type=volume,src=colletta-vol,dst=/var/lib/jetty-base jetty:latest bash
  406  docker run -it --rm -p 8080:8080 --mount type=volume,src=colletta-vol,dst=/var/lib/jetty-base --user 0 jetty:latest bash
  407  docker images
  408  docker rmi f37ef30ec330
  409  docker pull jetty:9.4.42
  410  docker images
  411  docker run -it --rm -p 8080:8080 --mount type=volume,src=colletta-vol,dst=/var/lib/jetty-base --user 0 jetty:9.4.42 bash
  412  docker run -it --rm -p 8080:8080 --mount type=volume,src=colletta-vol,dst=/var/lib/jetty-base jetty:9.4.42 bash
  413  docker run -it --rm -p 8080:8080 --mount type=volume,src=colletta-vol,dst=/var/lib/jetty-base jetty:9.4.42 
  414  docker run -it --rm -p 8080:8080 --mount type=volume,src=colletta-vol,dst=/var/lib/jetty-base jetty:9.4.42 bash
  415  docker run -it --rm -p 8080:8080 --mount type=volume,src=colletta-vol,dst=/var/lib/jetty jetty:9.4.42 bash
  416  docker run -it --rm -p 8080:8080 --mount type=volume,src=colletta-vol,dst=/var/lib/jetty jetty:9.4.42
  417  docker run -it --rm -p 8080:8080 --mount type=volume,src=colletta-vol,dst=/var/lib/jetty ubuntu bash
  418  docker run -it --rm --mount type=volume,src=colletta-vol,dst=/var/lib/jetty -v $(pwd):/backup ubuntu cvzf /backup/colletta-20210618.tgz /var/lib/jetty
  419  docker run -it --rm --mount type=volume,src=colletta-vol,dst=/var/lib/jetty -v $(pwd):/backup ubuntu tar cvzf /backup/colletta-20210618.tgz /var/lib/jetty
  420  ls
  421  docker volume create test
  422  docker run -it --rm --mount type=volume,src=test,dst=/var/lib/jetty -v $(pwd):/backup ubuntu bash -c "tar xvzf /backup/colletta-20210618.tgz /var/lib/jetty
  423  docker run -it --rm --mount type=volume,src=test,dst=/var/lib/jetty -v $(pwd):/backup ubuntu bash -c "cd /var/lib/jetty && tar xvzf /backup/colletta-20210618.tgz --strip 3"
  424  docker run -it --rm -p 8080:8080 --mount type=volume,src=test,dst=/var/lib/jetty ubuntu bash
  425  ls
  426  mv colletta-20210618.tgz ..
  427  history
  428  cd ..
  429  ls
  430  ls -l
  431  git status
  432  ls
  433  cd colletta/
  434  git version
  435  git remote -v
  436  cd src/colletta/
  437  ls
  438  ssh greg_colletta@37.9.227.245
  439  ls
  440  rcp colletta-20210618.tgz greg_colletta@37.9.227.245:colletta-20210618.tgz
  441  ssh greg_colletta@37.9.227.245
  442  rcp colletta-20210618.tgz greg_colletta@37.9.227.245:colletta-20210618.tgz
  443  ftp 37.9.227.245
  444  history
  445  docker volume list
  446  docker run -it --rm -p 8080:8080 --mount type=volume,src=test,dst=/var/lib/jetty jetty:9.4.42
  447  cd 
  448  vi myc.asp
  449  telnet 125.209.132.31 8000
  450  curl http://localhost:8080/WEB-INF/web.xml
  451  sudo apt install curl
  452  curl http://localhost:8080/WEB-INF/web.xml
  453  curl http://localhost:8080/%u002e/WEB-INF/web.xml
  454  curl 'http://localhost:8080/%u002e/WEB-INF/web.xml'
  455  curl http://localhost:8080/%u002e/WEB-INF/web.xml
  456  telnet localhost 8080
  457  curl http://localhost:8080/test/%u002e/WEB-INF/web.xml
  458  curl http://localhost:8080/test/dump/%u002e/WEB-INF/web.xml
  459  curl http://localhost:8080/test/dump/%u002e/WEB-INF/web.xml | egrep PathInfo
  460  curl -q http://localhost:8080/test/dump/%u002e/WEB-INF/web.xml | egrep PathInfo
  461  curl --quit http://localhost:8080/test/dump/%u002e/WEB-INF/web.xml | egrep PathInfo
  462  curl --quiet http://localhost:8080/test/dump/%u002e/WEB-INF/web.xml | egrep PathInfo
  463  curl http://localhost:8080/test/dump/%u002e/WEB-INF/web.xml 2>/dev/null | egrep PathInfo
  464  curl http://localhost:8080/test/dump/x/WEB-INF/web.xml 2>/dev/null | egrep PathInfo
  465  curl http://localhost:8080/test/dump/a/WEB-INF/web.xml 2>/dev/null | egrep PathInfo
  466  curl http://localhost:8080/test/dump/%u002e/WEB-INF/web.xml 2>/dev/null | egrep PathInfo
  467  curl http://localhost:8080/test/dump/%u002e/%u002e/WEB-INF/web.xml 2>/dev/null | egrep PathInfo
  468  curl http://localhost:8080/test/%u002e/%u002e/WEB-INF/web.xml 
  469  curl http://localhost:8080/test/%u002e/WEB-INF/web.xml 
  470  curl http://localhost:8080/test/dump/%u002e/WEB-INF/web.xml 
  471  curl http://localhost:8080/test/dump/%u002e/%u002e/WEB-INF/web.xml 2>/dev/null | egrep PathInfo
  472  curl http://localhost:8080/test/dump/%u002e%u002e/WEB-INF/web.xml 2>/dev/null | egrep PathInfo
  473  man unicode
  474  curl http://localhost:8080/test/dump/%u002e%u002e/WEB-INF/web.xml 2>/dev/null | egrep PathInfo
  475  curl http://localhost:8080/test/%u002e%u002e/WEB-INF/web.xml 2>/dev/null | egrep PathInfo
  476  curl http://localhost:8080/test/%u002e%u002e/WEB-INF/web.xml 
  477  telnet localhost 8080
  478  curl http://localhost:8080/test/foo/%u002e/bar/%u002e/%u002e/WEB-INF/web.xml 
  479  curl http://localhost:8080/test/foo/%u002e/bar/%u002e%u002e/%u002e%u002e/WEB-INF/web.xml 
  480  ls
  481  cd src/colletta/
  482  ls
  483  cd jetty-base/webapps/booking.colletta.it/WEB-INF/
  484  ls
  485  vi config.properties 
  486  ping authsmtp.colletta.it
  487  docker run -it --rm -p 8080:8080 --mount type=volume,src=test,dst=/var/lib/jetty ubuntu ping authsmtp.colletta.it
  488  docker run -it --rm -p 8080:8080 --mount type=volume,src=test,dst=/var/lib/jetty ubuntu bash -c "ping authsmtp.colletta.it"
  489  docker run -it --rm -p 8080:8080 --mount type=volume,src=test,dst=/var/lib/jetty ubuntu bash 
  490  docker run -it --rm -p 8080:8080 ubuntu bash -c "apt update && apt install -y traceroute && traceroute authsmtp.colletta.it"
  491  traceroute authsmtp.colletta.it
  492  sudo apt install traceroute
  493  traceroute authsmtp.colletta.it
  494  ls bin
  495  cd
  496  ls
  497  telnet localhost 8080
  498  man ascii
  499  cd src/jetty-9.4.x
  500  git push
  501  man ascii
  502  ls
  503  cd .
  504  m clean
  505  m install
  506  git pull
  507  git status
  508  git checkout -- documentation/jetty-documentation/src/main/asciidoc/operations-guide/features.adoc
  509  git status
  510  git pull
  511  cd documentation
  512  m clean install
  513  cd ../jetty-server/
  514  m install
  515  cd ..
  516  m install
  517  m clean install
  518  git push
  519  git checkout jetty-10.0.x-releaseIssue 
  520  git checkout jetty-10.0.x
  521  git pull --prune
  522  git status
  523  git checkout -- VERSION.txt
  524  git pull --prune
  525  git fetch unicode
  526  git checkout jetty-10.0.x-cve28164Bypass
  527  m clean install
  528  git remote -v
  529  git fetch unicode
  530  history
  531  git show-branch jetty-10.0.x-cve28164Bypass
  532  git show-branch -v jetty-10.0.x-cve28164Bypass
  533  git remote -v
  534  git push
  535  git pull
  536  git status
  537  git push
  538  git pull
  539  mvn install
  540  git push
  541  mvn install
  542  mvn clean install
  543  git push
  544  git status
  545  git push 
  546  git checkout jetty-10.0.x-cve28164Bypass
  547  git checkout jetty-10.0.x-getservletpath-empty-string
  548  git checkout jetty-10.0.x-cve28164Bypass
  549  git push
  550  cd jetty-util
  551  java -cp \$JETTY_HOME/lib/jetty-util-10.0.6-SNAPSHOT.jar org.eclipse.jetty.util.security.Password
  552  cd ../jetty-home/target/jetty-home
  553  mkdir ttt
  554  cd ttt
  555  export JETTY_HOME=~/src/jetty-10.0.x/jetty-home/target/jetty-home
  556  java -jar \$JETTY_HOME/start.jar 
  557  pwd
  558  export JETTY_HOME=/home/gregw/src/jetty-10.0.x/jetty-home/target/jetty-home
  559  java -jar \$JETTY_HOME/start.jar 
  560  java -jar $JETTY_HOME/start.jar 
  561  ls $JETTY_HOME 
  562  ls $JETTY_HOME/lib
  563  ls $JETTY_HOME/lib/logging
  564  git checkout jetty-10.0.x
  565  git pull
  566  git checkout jetty-10.0.x-compliance-doco 
  567  git pull
  568  ls ../modules/
  569  cd ../..
  570  cd ..
  571  m clean install
  572  git checkout jetty-10.0.x
  573  git status
  574  git push
  575  git checkout jetty-10.0.x
  576  git checkout jetty-10.0.x-6447-utf16Encodings
  577  git push
  578  git checkout jetty-10.0.x
  579  git pull
  580  cd ../jetty-11.0.x/
  581  git pull
  582  git merge --no-commit origin/jetty-10.0.x
  583  git status
  584  git status | cut -d: -f2
  585  git status | egrep : | cut -d: -f2 
  586  git status | egrep : | cut -d: -f2 | xargs egrep javax
  587  git commit -s
  588  git push
  589  cd ../jetty-10.0.x/
  590  git pull
  591  git remote -v
  592  vi ~/.bash_gregw 
  593  . ~/.bash_gregw 
  594  git pull
  595  git checkout jetty-10.0.x-6473-normalize-only-once
  596  git pull
  597  git checkout jetty-10.0.x
  598  git status
  599  git diff
  600  git checkout -f jetty-10.0.x
  601  git pull
  602  git checkout jetty-9.4.x
  603  git checkout -track origin/jetty-9.4.x
  604  git checkout --track origin/jetty-9.4.x
  605  git pull
  606  git checkout -b jetty9-squish
  607  git log
  608  git checkout jetty-10.0.x
  609  git pull
  610  git branch -D jetty9-squish
  611  cd src/colletta/
  612  ls
  613  rsh gregw@192.168.0.160
  614  rcp colletta-20210618.tgz gregw@192.168.0.160:/tmp/
  615  ls
  616  cd colletta/
  617  git pull
  618  ls
  619  cd ..
  620  tar tgz colletta-20210618.tgz 
  621  tar tfz colletta-20210618.tgz 
  622  ls
  623  ls jetty-base
  624  tar xvf colletta-20210618.tgz
  625  ls
  626  mv var/lib/jetty jetty-base-20210618
  627  tree var
  628  rm -fr var
  629  cd colletta/src/main/webapp/
  630  ls
  631  rm -fr *
  632  cp -R ~/src/colletta/jetty-base-20210618/webapps/booking.colletta.it/* .
  633  git status
  634  rm -fr WEB-INF/reservations/*
  635  git status
  636  rm -fr WEB-INF/classes WEB-INF/lib WEB-INF/reservations* WEB-INF/users.properties.sav 
  637  git add renting/view/p1/
  638  git add renting/view/cf/apt0.jpg 
  639  git add renting/view/OLD
  640  git status
  641  git add -A
  642  git status
  643  git commit -m 'Updated content from website'
  644  git push
  645  man bash
  646  vi
  647  vi /tmp/First.Splunk.alert.-.July.5.csv 
  648  git checkout -b jetty-10.0.x-6473-canonical-from-94
  649  git apply /tmp/patch
  650  git status
  651  patch -p1 < /tmp/patch 
  652  git status
  653  git checkout -f jetty-10.0.x
  654  git branch -D jetty-10.0.x-6473-canonical-from-94
  655  git pull
  656  git checkout jetty-10.0.x-6473-normalize-only-once
  657  git pull
  658  git push
  659  git checkout jetty-10.0.x
  660  git pull
  661  git checkout -b jetty-10.0.x-6493-delayed-headerCache
  662  git push -u origin jetty-10.0.x-6493-delayed-headerCache
  663  git puh
  664  git push
  665  git checkout jetty-10.0.x
  666  git checkout -b jetty-10.0.x-reservedThreadExcecutor-cleanup
  667  git status
  668  git checkout jetty-10.0.x
  669  git pull
  670  git checkout jetty-10.0.x-6493-delayed-headerCache 
  671  git pull
  672  git push
  673  git checkout jetty-10.0.x-reservedThreadExcecutor-cleanup
  674  git checkout -b jetty-10.0.x-6496-ReservedThreadExecutor
  675  ftp 37.9.227.245 
  676  cd src/colletta/
  677  ls
  678  cd colletta/
  679  git tag 20210618
  680  git pull
  681  jdk8
  682  ls
  683  mvn install
  684  ls
  685  ls target/
  686  ls target/colletta-website-2.0.0-SNAPSHOT
  687  diff -R target/colletta-website-2.0.0-SNAPSHOT ../jetty-base-20210618/
  688  diff -r target/colletta-website-2.0.0-SNAPSHOT ../jetty-base-20210618/webapps/booking.colletta.it
  689  git status
  690  git diff 20210618 
  691  git diff -q 20210618 
  692  git diff --name-only 20210618 
  693  cd src/main/webapp/
  694  git diff --name-only 20210618 
  695  git diff --name-only 20210618 -- .
  696  vi /tmp/ttt
  697  cd ../../..
  698  cd target/colletta-website-2.0.0-SNAPSHOT/
  699  ls
  700  ls WEB-INF/lib
  701  cd ../..
  702  mvn clean
  703  mvn install
  704  vi pom.xml 
  705  ls target/colletta-website-2.0.0-SNAPSHOT/WEB-INF/lib
  706  ls target/colletta-website-2.0.0-SNAPSHOT/WEB-INF/classes/
  707  ls target/colletta-website-2.0.0-SNAPSHOT/WEB-INF/classes
  708  cd target/colletta-website-2.0.0-SNAPSHOT/
  709  cat /tmp/ttt
  710  tar cfvz ~/src/colletta/update-20210708.tgz WEB-INF/lib WEB-INF/classes $(cat /tmp/ttt)
  711  vi /tmp/ttt
  712  tar cfvz ~/src/colletta/update-20210708.tgz WEB-INF/lib WEB-INF/classes $(cat /tmp/ttt)
  713  cd ../../..
  714  ls
  715  docker ls
  716  docker volume list
  717  docker volume clone --help
  718  docker volume create --help
  719  docker volumne rm test
  720  docker volume rm test
  721  docker run -it --rm --mount type=volume,src=colletta-vol,dst=/var/lib/jetty -v $(pwd):/backup ubuntu bash -c "cd /var/lib/jetty && ls"
  722  docker run -it --rm --mount type=volume,src=colletta-vol,dst=/var/lib/jetty -v $(pwd):/backup ubuntu bash -c "cd /var/lib/jetty && ls && ls /backup"
  723  docker run -it --rm --mount type=volume,src=colletta-vol,dst=/var/lib/jetty -v $(pwd):/backup ubuntu bash -c "cd /var/lib/jetty && ls && ls /backup/update-20210708.tgz"
  724  docker run -it --rm --mount type=volume,src=colletta-vol,dst=/var/lib/jetty -v $(pwd):/backup ubuntu bash -c "cd /var/lib/jetty/webapps/booking.colletta.it && ls && ls /backup/update-20210708.tgz"
  725  docker volume-create colletta-vol-backup
  726  docker volume create colletta-vol-backup
  727  docker run -it --rm --mount type=volume,src=colletta-vol,dst=/var/lib/jetty --mount type=volume,src=colletta-vol-backup,dst=/backup ubuntu bash -c "mkdir -p /backup/var/lib && cp -r /var/lib/jetty /backup/var/lib"
  728  docker run -it --rm --mount type=volume,src=colletta-vol,dst=/var/lib/jetty -v $(pwd):/backup ubuntu bash -c "tar cfvz /backup/colletta-backup-20210708.tgz /var/lib/jetty"
  729  ls
  730  docker run -it --rm --mount type=volume,src=colletta-vol,dst=/var/lib/jetty -c "cd /var/lib/jetty/webapps/booking.colletta.it && ls"
  731  docker run -it --rm --mount type=volume,src=colletta-vol,dst=/var/lib/jetty ubuntu bash -c "cd /var/lib/jetty/webapps/booking.colletta.it && ls"
  732  docker run -it --rm --mount type=volume,src=colletta-vol,dst=/var/lib/jetty ubuntu bash -c "cd /var/lib/jetty/webapps/booking.colletta.it && rm -r WEBINF/lib WEB-INF/classes renting/view/OLD/ah-mimosa"
  733  docker run -it --rm --mount type=volume,src=colletta-vol,dst=/var/lib/jetty ubuntu bash -c "cd /var/lib/jetty/webapps/booking.colletta.it && rm -r WEB-INF/lib WEB-INF/classes renting/view/OLD/ah-mimosa"
  734  docker run -it --rm --mount type=volume,src=colletta-vol,dst=/var/lib/jetty -v $(pwd):/backup ubuntu bash -c "cd /var/lib/jetty/webapps/booking.colletta.it && tar xfvz /backup/update-20210708.tgz"
  735  docker run -it --rm -p 8080:8080 --mount type=volume,src=colletta-vol,dst=/var/lib/jetty jetty:9.4.42
  736  docker run -it --rm --mount type=volume,src=colletta-vol,dst=/var/lib/jetty ubuntu bash -c "cd /var/lib/jetty/webapps/booking.colletta.it && chown -R jetty.jetty ."
  737  docker run -it --rm --mount type=volume,src=colletta-vol,dst=/var/lib/jetty ubuntu bash -c "cd /var/lib/jetty/webapps/booking.colletta.it && chown -R jetty ."
  738  docker run -it --rm --mount type=volume,src=colletta-vol,dst=/var/lib/jetty ubuntu bash -c "cd /var/lib/jetty/webapps/booking.colletta.it && ls -la"
  739  docker run -it --rm --mount type=volume,src=colletta-vol,dst=/var/lib/jetty ubuntu bash -c "cd /var/lib/jetty/webapps/booking.colletta.it && ls -la renting/view"
  740  docker run -it --rm --mount type=volume,src=colletta-vol,dst=/var/lib/jetty ubuntu bash -c "cd /var/lib/jetty/webapps/booking.colletta.it && chown -R 999 ."
  741  docker run -it --rm -p 8080:8080 --mount type=volume,src=colletta-vol,dst=/var/lib/jetty jetty:9.4.42
  742  docker run -it --rm --mount type=volume,src=colletta-vol,dst=/var/lib/jetty ubuntu bash 
  743  cd colletta/
  744  ls
  745  git pull
  746  mvn clean install
  747  cd target/colletta-website-2.0.0-SNAPSHOT/
  748  tar cfvz ~/src/colletta/update-20210708.tgz WEB-INF/lib WEB-INF/classes $(cat /tmp/ttt)
  749  cd ../../..
  750  docker run -it --rm --mount type=volume,src=colletta-vol,dst=/var/lib/jetty ubuntu bash -c "cd /var/lib/jetty/webapps/booking.colletta.it && rm -r WEB-INF/lib WEB-INF/classes renting/view/OLD/ah-mimosa"
  751  docker run -it --rm --mount type=volume,src=colletta-vol,dst=/var/lib/jetty -v $(pwd):/backup ubuntu bash -c "cd /var/lib/jetty/webapps/booking.colletta.it && tar xfvz /backup/update-20210708.tgz"
  752  docker run -it --rm --mount type=volume,src=colletta-vol,dst=/var/lib/jetty ubuntu bash -c "cd /var/lib/jetty/webapps/booking.colletta.it && chown -R 999 ."
  753  docker run -it --rm -p 8080:8080 --mount type=volume,src=colletta-vol,dst=/var/lib/jetty jetty:9.4.42
  754  docker run -it --rm -p 8080:8080 --mount type=volume,src=colletta-vol,dst=/var/lib/jetty jetty:9.4.43
  755  docker pull jetty:9.4.43
  756  ftp -?
  757  ftp --help
  758  man ftp
  759  ftp 37.9.227.245
  760  history
  761  history > colletta/README.txt
