vfs2-file-explorer
==================

# Description
**vfs2-file-explorer** is a liferay (tested on 6.1.1) portlet developed with vaadin and based on apache vfs 2 component


# Build

mvn vaadin:update-widgetset clean install

# Configuration

**File access**

windows : file:///C:/Temp

unix: file:///home/someuser/somedir


----------


**FTP access**

ftp://hostname[: port]

plus username and password


----------


**SFTP access**

sftp://hostname[: port][ absolute-path]

plus username and password

and / or 

path to keyfile like /home/user/keyfile/id_rsa



----------

 - It is possible to configure if the portlet allows to upload and delete files
 - It is possible to specify liferay roles which are allowed to upload an delete files
 - It is possible to filter the result set with a wildcard search (e.g. *abc*, *.txt)
 - Multiple file upload with browsers like firefox , chrome (does not work from IE) via drag and drop from desktop

