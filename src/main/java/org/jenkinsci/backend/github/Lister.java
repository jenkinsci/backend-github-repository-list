package org.jenkinsci.backend.github;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

import java.io.FileNotFoundException;
import java.net.URL;
import java.util.Collections;

/**
 * @author Kohsuke Kawaguchi
 */
public class Lister {
    public static void main(String[] args) throws Exception {
        DocumentFactory factory = new DocumentFactory();
        factory.setXPathNamespaceURIs(Collections.singletonMap("m","http://maven.apache.org/POM/4.0.0"));

        GitHub gh = GitHub.connect();
        GHOrganization org = gh.getOrganization("jenkinsci");

        for (GHRepository r : org.getRepositories().values()) {
            System.out.printf("|[%s|%s]|%s|",
                    r.getName(),r.getUrl(),
                    " "+r.getDescription()); // '||' is interpreted as TH

            try {
                URL pom = new URL(r.getUrl() + "/raw/master/pom.xml");
                Document dom = new SAXReader(factory).read(pom);

                Element groupId    = getElement(dom, "groupId");
                Element artifactId = getElement(dom, "artifactId");

                System.out.printf("%s|%s|", groupId.getTextTrim(), artifactId.getTextTrim());
            } catch (DocumentException e) {
                if (e.getNestedException() instanceof FileNotFoundException) {
                    // no POM
                } else {
                    e.printStackTrace();
                }
            }

            System.out.println();
        }
    }

    private static Element getElement(Document dom, String elementName) {
        Node n = dom.selectSingleNode("/project/"+ elementName);
        if(n==null)
            n = dom.selectSingleNode("/m:project/m:"+elementName);
        if(n==null)
            n = dom.selectSingleNode("/project/parent/"+ elementName);
        if(n==null)
            n = dom.selectSingleNode("/m:project/m:parent/m:"+ elementName);
        return (Element)n;
    }
}
