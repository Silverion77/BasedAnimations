for i in $(ls stiffness2)
do
    echo "$i"
    convert stiffness2/$i stiffness5/$i stiffness8/$i +append $i
done

#for i in $(ls stiffnesses8)
#do
#    j=`echo $i | sed "s/export1/export0/g"`
#    echo $j
#    mv stiffnesses8/$i stiffnesses8/$j
#done