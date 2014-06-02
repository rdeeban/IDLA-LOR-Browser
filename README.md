IDLA-LOR-Browser
================

Author: Deeban Ramalingam

The IDLA LOR Browser allows Idaho Digital Learning Academy (IDLA) Learning Object Repository Administrators (LOR) and Lesson Developers to upload to and download Softchalk Lesson Packages from the IDLA LOR Amazon S3 Bucket, and also allows administrators and developers to browse and view files in the IDLA LOR. Setup an Apache Tomcat Server on an Amazon EC2 Linux instance and uploaded web archive (.war built with Maven) containing the web application to the Tomcat Server. The web application structure is organized using the Spring Model-View-Controller design.The web application utilizes Twitter Bootstrap (HTML, CSS, and Javascript), D3, jQuery, AJAX, and JSON on the front-end and JAVA, JSP, AES Cryptography, and Amazon S3 API calls on the back-end. 

The website that hosts the application can be found at http://54.186.242.176

updated version 1.1.0 (15 October 2013)
* fixed upload (previously not sending data)

updated version 1.2.0 (15 October 2013)
* added progress indicator

updated version 2.0.0 (10 January 2014)
* fixed upload (previously sending corrupt bytes)
* added client-side cache for previous node tree state

updated version 2.1.0 (16 February 2014)
* configured backend
* added user ability to view the top-most level contents of bucket
* modified progress update

updated version 2.2.0 (19 April 2014)
* added oauth with gmail verification