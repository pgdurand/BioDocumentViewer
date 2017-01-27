##Introduction

This "data/render" folder is only there to prepare "web-renderer.zip" file that is part of the *src/bzh/plealog/bioinfo/docviewer/ui/resources* package. That file is deployed by the application at runtime, within (default) directory:

    ${user.home}/.Document-Viewer/web.

##Create a new "web-renderer.zip"

The "data/render" folder can then be used to build a new "web-renderer.zip". 

For that purpose, you need to get from the web:

* Bootstrap Javascript library; current version used is 3.3.7
* JQuery Javascript library; current version used is 3.1.1
* a copy of all "ens\_var\_\*.png" files from *src/bzh/plealog/bioinfo/docviewer/ui/resources* package

Put all that files in this "data/render" directory within appropriate sub-folders; see content of current "src/bzh/plealog/bioinfo/docviewer/ui/resources/web-renderer.zip" file.

*Important notice:* if you upgrade Bootstrap and/or JQuery, you'll have to upgrade "ensembl\_var\_index.vm" Velocity template.

Then, run the following commands from the command-line:

    # -1- Create new "web-renderer.zip" archive
    zip -r -X web-renderer.zip bootstrap-3.3.7-dist/ images/ jquery/ ensembl_var_index.vm
      # this is a "zip" made on MacOSX; "-X" means: do not include 
      # OSX specific files (such as .DS_Store, etc.)
    
    # -2- Copy new archive to appropriate location
    cp web-renderer.zip ../../src/bzh/plealog/bioinfo/docviewer/ui/resources/
