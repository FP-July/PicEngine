COM=$1
PRO=$2
ID=$(whoami)
LAB=finalPro

if [[ $COM == "ls" ]]; then
	echo hadoop fs -ls /user/$ID/$LAB/$2
	hadoop fs -ls /user/$ID/$LAB/$2

elif [[ $COM == "cat" ]]; then
    echo hadoop fs -cat /user/$ID/$LAB/$2
    hadoop fs -cat /user/$ID/$LAB/$2

elif [[ $COM == "get" ]]; then
	echo hadoop fs -ls /user/$ID/$LAB/$2
	hadoop fs -get /user/$ID/$LAB/$2 

elif [[ $COM == "rm" ]]; then
	echo hadoop fs -rm -r /user/$ID/$LAB/$PRO
	hadoop fs -rm -r /user/$ID/$LAB/$PRO
  
else
	ant rebuild

	echo +++++++++++++++ PROJECT : $PRO +++++++++++++++++

    hadoop fs -rm -r /user/$ID/$LAB/loc
	hadoop jar build/jar/*.jar $PRO
fi
