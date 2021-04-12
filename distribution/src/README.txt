 WSO2 Micro Integrator @product.version@
======================================================================

Welcome to the WSO2 MI @product.version@ release

WSO2 MI distribution contains the lightweight integration runtime called 'Micro Integrator' that is optimized to
deploy in containers.

WSO2 MI simplifies integration by allowing users to easily configure message routing, mediation, transformation,
logging, task scheduling, load balancing, failover routing, event brokering etc.
Data services and various applications can also be hosted and exposed using WSO2 MI.

Key features of WSO2 MI @product.version@
==================================

See the online WSO2 MI documentation for more information on product features:
https://apim.docs.wso2.com/en/latest/integrate/integration-overview/


Installation & Running
==================================

Running the Integrator
==================================
1. Extract  wso2mi-@product.version@.zip and go to the extracted directory/bin.
2. Run micro-integrator.sh or micro-integrator.bat.


WSO2 MI distribution directory
=============================================

 - bin
	  Contains various scripts (.sh & .bat scripts).

 - dbscripts
	  Contains all the database scripts.

 - lib
	  Used to add external jars(dependencies) to all runtimes.

 - repository
	  The repository where services and modules deployed in WSO2 MI runtime
	  are stored.

 - conf
	  Contains configuration files specific to integrator runtime.

 - logs
	  Contains all log files created during execution of MI.

 - resources
	  Contains additional resources that may be required, including sample
	  configurations and sample resources.

 - tmp
	  Used for storing temporary files, and is pointed to by the
	  java.io.tmpdir System property.

 - LICENSE.txt
	  Apache License 2.0 and the relevant other licenses under which
	  WSO2 MI is distributed.

 - README.txt
	  This document.

 - release-notes.html
	  Release information for WSO2 MI @product.version@

 - patches
	  Used to add patches related for all run time.

 -dropins
	  Used to add external osgi bundles(dependencies) to the runtime.

 -extensions
	  Used to add carbon extensions.

 -wso2/components
	  Contains different components (OSGI bundles, features etc.) that are related to the product.

 -wso2/lib
	  Contains jars that are required/shared by run time.

Known issues of WSO2 MI @product.version@
==================================

     - https://github.com/wso2/micro-integrator/issues

Support
==================================

WSO2 Inc. offers a variety of development and production support
programs, ranging from Web-based support up through normal business
hours, to premium 24x7 phone support.

For additional support information please refer to http://wso2.com/support/

For more information on WSO2 MI, visit the GitHub page (https://github.com/wso2/micro-integrator)

Crypto Notice
==================================

   This distribution includes cryptographic software.  The country in
   which you currently reside may have restrictions on the import,
   possession, use, and/or re-export to another country, of
   encryption software.  BEFORE using any encryption software, please
   check your country's laws, regulations and policies concerning the
   import, possession, or use, and re-export of encryption software, to
   see if this is permitted.  See <http://www.wassenaar.org/> for more
   information.

   The U.S. Government Department of Commerce, Bureau of Industry and
   Security (BIS), has classified this software as Export Commodity
   Control Number (ECCN) 5D002.C.1, which includes information security
   software using or performing cryptographic functions with asymmetric
   algorithms.  The form and manner of this Apache Software Foundation
   distribution makes it eligible for export under the License Exception
   ENC Technology Software Unrestricted (TSU) exception (see the BIS
   Export Administration Regulations, Section 740.13) for both object
   code and source code.

   The following provides more details on the included cryptographic
   software:

   Apache Rampart   : http://ws.apache.org/rampart/
   Apache WSS4J     : http://ws.apache.org/wss4j/
   Apache Santuario : http://santuario.apache.org/
   Bouncycastle     : http://www.bouncycastle.org/

--------------------------------------------------------------------------------
(c) Copyright 2018 WSO2 Inc.




