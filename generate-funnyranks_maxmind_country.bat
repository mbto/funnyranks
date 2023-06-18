REM FunnyRanks uses geo tables from https://github.com/mbto/maxmind-geoip2-csv2sql-converter
REM Example build configuration with EN,RU locales and IPv4:
chcp 65001
set JAVA_HOME=C:\Program Files\Java\jdk-11.0.16\
"S:\projects\GeoLite2\maxmind-geoip2-csv2sql-converter1.1\bin\maxmind-geoip2-csv2sql-converter.bat" -od "S:\projects\GeoLite2\maxmind-geoip2-csv2sql-converter1.1\funnyranks" -oa "country_en_ru.zip" -k M8jFjv3vZWrOCaZN -c "C:\Users\%USERNAME%\IdeaProjects\funnyranks\GeoLite2-Country-CSV.mysql.funnyranks.ini" -i 4 -l "en,ru" -dc true -ds true