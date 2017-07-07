## qNavigator


#Installation instructions

1. Follow the instructions for setting up Liferay and openBIS as well as the QBiC-specific data model found on our portal (coming soon)
2. Clone qNavigator from the git repository: 'git clone https://github.com/qbicsoftware/qnavigator qnavigator'
3. Adjust the properties defined in the file 'portlet.properties' (this is what links the portlet to the qbic-ext.properties file)
4. For deployment a web application archive (.war) file has to be created. Navigate to the ‘WebContent’ folder of the qNavigator project and type 'jar cvf qnavigator.war .'
5. Copy the generated qnavigator.war to the deploy folder of your Liferay installation 'cp qnavigator.war /home/to/liferay/deploy/'
6. Add qNavigator as a new application in your Liferay instance through the web interface
