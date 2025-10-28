#### **Features:**
* Receiving game logs from dedicated game servers via UDP
* Collecting & caching player statistics: kills, deaths, online at server, Steam IDs, IPs with country/city names (provided by [MaxMind GeoLite2 database](https://maxmind.com))
* Merging player statistics into MySQL tables per game project by player name/IP/Steam ID on 'next map', 'shutdown server' events, scheduler, or manually
* Automatic calculation players activity and assignment one of 56 eng/rus ranks, history keeping
* Provides frontend for management broker & projects
* Provides [AMXX plugin](https://github.com/mbto/funnyranks/tree/master/amxx-plugin) for show players rank in a game that supports [AMX Mod X](https://github.com/alliedmodders/amxmodx/)
* This broker can be installed on a game hosting to collect and send player statistics to the project tables from all game servers (only Half-Life Dedicated Server (AppID 90) is available, other handlers are not implemented)
*For developers: You can extends [MessageHandler](https://github.com/mbto/funnyranks/blob/master/src/main/java/com/github/mbto/funnyranks/broker/handlers/MessageHandler.java) class and implement logs handler for another game engine*

![funnyranks Диаграмма](https://github.com/user-attachments/assets/007e13e9-a733-4e87-b457-d38116f82809)

#### **Requirements:**
* Java 25 at [oracle.com/java](https://www.oracle.com/java/technologies/javase-downloads.html)
* MySQL server 8+

#### **Install & launch:**
* https://github.com/mbto/funnyranks/wiki/Install-&-launch

#### **Examples:**
* https://github.com/mbto/funnyranks/wiki/Examples

#### **Downloads:**
* https://github.com/mbto/funnyranks/releases

#### **FAQ:**
* https://github.com/mbto/funnyranks/wiki/Questions

#### **Compile & Build:**
* **Requirements:**
    * `Gradle 8.14.3+`
* **With tests:**
    * Unix: `gradlew build`
    * Windows: `gradlew.bat build`
* **Without tests:**
    * Unix: `gradlew build -x test`
    * Windows: `gradlew.bat build -x test`
