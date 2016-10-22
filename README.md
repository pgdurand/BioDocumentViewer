#BioDocumentViewer

[![License](https://img.shields.io/badge/license-Affero%20GPL%203.0-blue.svg)](https://www.gnu.org/licenses/agpl-3.0.txt)

##Introduction

BioDocumentViewer is a software enabling to query, browse, view and retrieve sequences from NCBI and EBI public servers.

The software has been made to facilitate the use of public web services, [NCBI Entrez eUtils services](https://www.ncbi.nlm.nih.gov/books/NBK25499/) (NCBI, Bethesda, USA) and [EB-eye Search services](https://www.ebi.ac.uk/ebisearch/) (EBI, Hinxton, UK), so that it is easy to query databanks and retrieve set of sequences in batch mode. 

##Main features

The following screenshot illustrates BioDocumentViewer main features:

* The **QueryBuilder** on the top left side; here, we use NCBI as data provider;
* the **DocumentNavigator** on the left side; **a batch sequence retrieval job** is running to get more than 100K sequences, respecting NCBI recommendations;
* the **3D Strucrure Viewer** on the right side, showing a glycosidase PDB structure;
* the **Sequence Viewer**, displaying a fully annotated Refseq gene

![BioDocumentViewer](doc/bio-document-viewer-query-ncbi-all-s.png)

##Requirements

Use a [Java Virtual Machine](http://www.oracle.com/technetwork/java/javase/downloads/index.html) 1.7 (or above) from Oracle. 

As a result of using Java, BioDocumentViewer is available for Linux, Mac OSX and Windows systems.

*Not tested with any other JVM providers but Oracle... so there is no guarantee that the software will work as expected if not using Oracle's JVM.*

##Working with the binary release

The most easy way to use BioDocViewer Tool consists in downloading an official release. It is made of a single Java binary archive file:

    biodocviewer-x.y.z.jar

It is available from this Github page: check [Releases] tab and download the latest release.

We invite all users to start working with that "jar" file, as explained in the next section. 

*Notice for developers:* this unique JAR file is absolutely not intended to be embedded within any other applications since it contains all the dependencies (third-party libraries) into a single large file. So, if you want to include BioDocViewer into some other softwares, start from its source code (git clone).

##User manual

###Start the graphical interface

After retrieving an official release of the BioDocumentViewer (BDV) software (see above section), you can start it quite easily from a Terminal (Linux, Mac OSX and Windows):

    java -jar biodocviewer-x.y.z.jar

Windows, Mac OSX systems: you can also double click on the "jar" file to start the software.

By default, BDV uses [NCBI Entrez eUtils services](https://www.ncbi.nlm.nih.gov/books/NBK25499/) (NCBI, Bethesda, USA) to let you query and retrieve protein and nucleotide sequences, 3D structures and taxonomy information. 

However, you can also ask BDV to use [EB-eye Search services](https://www.ebi.ac.uk/ebisearch/) (EBI, Hinxton, UK) as follows:

    java -DV_PROVIDER=EBI -jar biodocviewer-x.y.z.jar

JVM argument "V_PROVIDER" accepts one of: NCBI (default) or EBI.

###Make and run a query

Whatever the data provider you use, NCBI or EBI, the software is quite easy to use:

* select a bank to query
* click on the [Go...] button
* setup the criteria in the query dialogue box
* click on the [Ok] button... and wait a few seconds to get an answer from the provider

![BioDocumentViewer](doc/bio-doc-viewer-query-ncbi-proteins.png)

The DocumentNavigator is displayed as soon as you get a valid answer from the bank provider, i.e. your query matches some entries.

Use the DocumentNavigator:

* to navigate through entries; use [<] [>] buttons located at the top right corner
* to display an entry in the Sequence or 3D Structure viewer
* to fetch entries as Fasta or plain format (e.g. Genbank, EMBL, Uniprot, etc.)

![BioDocumentViewer](doc/bio-doc-viewer-ncbi-protein-results.png)

###NCBI/EBI recommendations

**BDV strictly respects the recommendations of NCBI and EBI for the use or their public resources**, especially when retrieving sequences in batch mode.

These receommendations are:

* keep in mind that you are using resources that are shared by many users around the world 
* do not overload NCBI/EBI remote servers
* retrieve sequences in batch mode using timers
* provide your email address: NCBI or EBI can get in touch with you in case they consider your are querying too much their remote services. 

More about these recommendations: [here for NCBI](https://www.ncbi.nlm.nih.gov/home/about/policies.shtml#scripting), [here for EBI](http://www.ebi.ac.uk/Tools/webservices/help/faq).

###Batch retrieval of sequences

Here is an example of DocumentNavigator retrieving more than 100K sequences from NCBI using a batch mode following NCBI recommendations: 

![BioDocumentViewer](doc/bio-doc-viewer-batch-retrieval.png)

###Extended configuration

BDV accepts the following optional JVM arguments on the command line:

* V_DEBUG. Accepts one of: true, false. Default is false.
* V_PROVIDER. Accepts one of: NCBI, EBI. Default is NCBI.
* V_EMAIL. Your email address; see "NCBI/EBI recommendations" section, above. Default is none, so BDV will ask you that information at runtime, the first time you use the software.

You use all these arguments as follows:

    java -DV_PROVIDER=EBI -DV_DEBUG=true ... -jar biodocviewer-x.y.z.jar

###Log viewer

BDV uses a logging framework to monitor the software.. This can be quite useful when you experience NCBI/EBI connections failure. 

To access the log messages, simply click on the little icon located at the top right corner of BDV main frame, next to [Documents] pulldown menu.

By default, BDV uses "info" log level, which does not provide much messages. You can switch from "info" to "debug" level using BDV log viewer (see screenshot, below). In such a way, you can view more messages but you won't have full debug traces including classes/methods information. To enable that feature, you are advised to start BDV with "-DV_DEBUG=true" to get full log messages. 

It is worth noting that log messages are localized in your country; e.g. below screenshot is made on a French localized Mac OSX system:

![BioDocumentViewer](doc/bio-doc-viewer-log.png)


##License and dependencies

BioDocViewer itself is released under the GNU Affero General Public License, Version 3.0. [AGPL](https://www.gnu.org/licenses/agpl-3.0.txt)

BioDocViewer depends on several thrid-party libraries as stated in the NOTICE.txt file provided with this project.

--
(c) 2006-2016 - Patrick G. Durand
