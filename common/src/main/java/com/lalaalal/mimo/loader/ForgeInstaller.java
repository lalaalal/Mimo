package com.lalaalal.mimo.loader;

import com.lalaalal.mimo.Mimo;
import com.lalaalal.mimo.exception.MessageComponentException;
import com.lalaalal.mimo.util.HttpHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ForgeInstaller extends ForgeLikeInstaller {
    public static final String VERSIONS_URL = "https://maven.minecraftforge.net/net/minecraftforge/forge/maven-metadata.xml";
    public static final String INSTALLER_DOWNLOAD_URL = "https://maven.minecraftforge.net/net/minecraftforge/forge/%1$s/forge-%1$s-installer.jar";

    public ForgeInstaller() throws IOException {
        super(Loader.Type.FORGE, INSTALLER_DOWNLOAD_URL, "--installServer");
    }

    @Override
    public void loadVersions() throws IOException {
        Mimo.LOGGER.info("Loading Forge versions");
        String data = HttpHelper.sendSimpleHttpRequest(VERSIONS_URL);
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            ByteArrayInputStream inputStream = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8));
            Document document = builder.parse(inputStream);
            document.getDocumentElement().normalize();
            NodeList versions = document.getElementsByTagName("version");
            for (int index = 0; index < versions.getLength(); index++) {
                Node versionNode = versions.item(index);
                String version = versionNode.getTextContent();

                parseMinecraftVersion(version).ifPresent(minecraftVersion -> {
                    getMinecraftVersionSpecificLoaderVersions(minecraftVersion).addFirst(version);
                });
            }
        } catch (ParserConfigurationException | SAXException exception) {
            throw new MessageComponentException("Failed to load Forge versions", exception);
        }
    }
}
