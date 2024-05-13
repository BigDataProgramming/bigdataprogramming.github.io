import sys
import datetime

for line in sys.stdin:
    line = line.strip()
    userid, movieid, rating, timestamp = line.split('\t')
    week = datetime.datetime.fromtimestamp(float(timestamp)).isocalendar()[1]
    print('\t'.join([userid, movieid, rating, str(week)]))
