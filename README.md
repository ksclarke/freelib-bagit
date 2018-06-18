# freelib-bagit &nbsp;[![Build Status](https://travis-ci.org/ksclarke/freelib-bagit.svg)](https://travis-ci.org/ksclarke/freelib-bagit) [![Codacy Badge](https://api.codacy.com/project/badge/Coverage/514cebb0b7ae4b63905eedcc3fe45a57)](https://www.codacy.com/app/ksclarke/freelib-bagit?utm_source=github.com&utm_medium=referral&utm_content=ksclarke/freelib-bagit&utm_campaign=Badge_Coverage) [![Known Vulnerabilities](https://snyk.io/test/github/ksclarke/freelib-bagit/badge.svg?targetFile=pom.xml)](https://snyk.io/test/github/ksclarke/freelib-bagit?targetFile=pom.xml) [![Maven](https://img.shields.io/maven-metadata/v/http/central.maven.org/maven2/info/freelibrary/freelib-bagit/maven-metadata.xml.svg?colorB=brightgreen)](http://mvnrepository.com/artifact/info.freelibrary/freelib-bagit) [![Javadocs](http://javadoc.io/badge/info.freelibrary/freelib-bagit.svg)](http://projects.freelibrary.info/freelib-bagit/javadocs.html)

Freelib-bagit is a Java library for working with and creating BagIt packages.
It supports most of the BagIt spec, but does not yet support fetch.txt files.

For more on the BagIt spec, start with: http://en.wikipedia.org/wiki/BagIt

FreeLib-BagIt's development is taking place on github:
    http://github.com/ksclarke/freelib-bagit
    
FreeLib-BagIt's project page (with test results, javadocs, etc.) is at:
    http://freelibrary.info/freelib-bagit/

### How to use FreeLib-BagIt:

From within a Java program, you can create a new bag:

    Bag bag = new Bag("dryad_630");

or create a bag from an existing BagIt directory:

    Bag bag = new Bag("/home/kevin/dryad_630");

or open a pre-existing gz, bzip2, or zip compressed bag:

    Bag bag = new Bag("dryad_630.tar.gz");

You can add metadata to the bag:

    Bag bag = new Bag("dryad_630");
    BagInfo bagInfo = bag.getBagInfo();

    bagInfo.addMetadata(BagInfoTags.CONTACT_NAME, "Kevin S. Clarke");
    bagInfo.addMetadata(BagInfoTags.CONTACT_PHONE, "ksclarke@gmail.com");
    bagInfo.addMetadata("Alt-Email", "thetrashcan@gmail.com");
    
or add metadata via Java Properties:

    Properties metadata = new Properties();
    File properties = new File("my.properties");
    
    try {
        metadata.load(new FileReader(properties));
    }
    catch (IOException details) {
        System.out.println(details.getMessage());
    }

    Bag bag = new Bag("bag_new", metadata);

You can add data to the bag:

    Bag bag = new Bag("dryad_630");
    File dataFile = new File("/home/kevin/data_file.csv");
    File dataDir = new File("/home/kevin/csv_files/");

    bag.addData(dataFile, dataDir);

And you can validate the bag:

    BagValidator validator = new BagValidator();
    Bag bag = new Bag("/home/kevin/dryad_630.tar.gz");

    try {
        ValidBag validBag = validator.validate(bag);
    }
    catch (BagException details) {
        switch (details.getReason()) {
        case BagException.PAYLOAD_MANIFEST_DIFFERS_FROM_DATADIR:
            // do something
            break;
        default:
            System.out.println(details.getMessage());
        }
    }
    
or, simply, check a bag's validity:

    BagValidator validator = new BagValidator();
    Bag bag = new Bag("/home/kevin/dryad_630.tar.gz");

    if (validator.isValid(bag)) {
        System.out.println("Bag is valid");
    }
    else {
        System.out.println("Bag isn't valid");
    }
    
You can also write a valid bag to a compressed file for transport:

    BagValidator validator = new BagValidator();
    Bag bag = new Bag("/home/kevin/dryad_630");

    try {
        ValidBag validBag = validator.validate(bag);
        File bz2Bag = validBag.toTarBZip2();
    }
    catch (BagException details) {
        System.out.println(details.getMessage());
    }
    catch (IOException details) {
        System.out.println(details.getMessage());
    }
    
### Contact

If you have questions about freelib-bagit <a href="mailto:ksclarke@ksclarke.io">feel free to ask</a> or, if you encounter a 
problem, please feel free to [open an issue](https://github.com/ksclarke/freelib-bagit/issues "GitHub Issue Queue") in the 
project's issue queue.
