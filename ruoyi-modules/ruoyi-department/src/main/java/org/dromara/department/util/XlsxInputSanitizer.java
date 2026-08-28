package org.dromara.department.util;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * XLSX 输入文件兼容处理工具。
 *
 * <p>部分 Excel 文件会保留无法被 Java URI 解析的外部超链接关系，例如把
 * 中文说明拼接到 mailto 地址中。Fesod/POI 在打开工作表之前就会解析这些关系，
 * 导致与业务数据无关的导入失败。导入业务不使用超链接，因此这里只移除外部超链接
 * 关系及其工作表引用，保留其他工作簿内容不变。</p>
 */
public final class XlsxInputSanitizer {

    private static final String OFFICE_RELATIONSHIP_NS = "http://schemas.openxmlformats.org/officeDocument/2006/relationships";
    private static final String HYPERLINK_RELATIONSHIP_SUFFIX = "/hyperlink";
    private static final String RELATIONSHIP_MARKER = "/_rels/";

    private XlsxInputSanitizer() {
    }

    /**
     * 清理 XLSX 中的外部超链接关系。
     *
     * @param inputStream 原始 Excel 输入流
     * @return 清理后的 Excel 字节；如果文件不是 ZIP/XLSX，则原样返回
     * @throws IOException Excel 文件读取或重建失败
     */
    public static byte[] removeExternalHyperlinks(InputStream inputStream) throws IOException {
        byte[] source = inputStream.readAllBytes();
        if (!isZip(source)) {
            return source;
        }

        Map<String, byte[]> entries = readEntries(source);
        Map<String, Set<String>> removedRelationshipIds = new HashMap<>();
        boolean changed = false;

        for (Map.Entry<String, byte[]> entry : entries.entrySet()) {
            String entryName = entry.getKey();
            if (!entryName.endsWith(".rels")) {
                continue;
            }
            RelationshipResult result = removeExternalHyperlinkRelationships(entry.getValue());
            if (result.changed()) {
                entry.setValue(result.content());
                removedRelationshipIds.put(sourcePart(entryName), result.removedIds());
                changed = true;
            }
        }

        for (Map.Entry<String, Set<String>> entry : removedRelationshipIds.entrySet()) {
            if (entry.getKey() == null || entry.getValue().isEmpty()) {
                continue;
            }
            byte[] sheetContent = entries.get(entry.getKey());
            if (sheetContent == null) {
                continue;
            }
            XmlResult result = removeWorksheetHyperlinks(sheetContent, entry.getValue());
            if (result.changed()) {
                entries.put(entry.getKey(), result.content());
            }
        }

        if (!changed) {
            return source;
        }
        return writeEntries(entries);
    }

    private static boolean isZip(byte[] source) {
        return source.length >= 4
            && source[0] == 'P'
            && source[1] == 'K'
            && (source[2] == 3 || source[2] == 5 || source[2] == 7)
            && (source[3] == 4 || source[3] == 6 || source[3] == 8);
    }

    private static Map<String, byte[]> readEntries(byte[] source) throws IOException {
        Map<String, byte[]> entries = new LinkedHashMap<>();
        try (ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(source))) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                entries.put(entry.getName(), zipInputStream.readAllBytes());
            }
        }
        return entries;
    }

    private static byte[] writeEntries(Map<String, byte[]> entries) throws IOException {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream();
             ZipOutputStream zipOutputStream = new ZipOutputStream(output)) {
            for (Map.Entry<String, byte[]> entry : entries.entrySet()) {
                zipOutputStream.putNextEntry(new ZipEntry(entry.getKey()));
                zipOutputStream.write(entry.getValue());
                zipOutputStream.closeEntry();
            }
            zipOutputStream.finish();
            return output.toByteArray();
        }
    }

    private static RelationshipResult removeExternalHyperlinkRelationships(byte[] content) throws IOException {
        Document document = parseXml(content);
        NodeList nodes = document.getDocumentElement().getChildNodes();
        Set<String> removedIds = new HashSet<>();
        for (int i = nodes.getLength() - 1; i >= 0; i--) {
            Node node = nodes.item(i);
            if (!(node instanceof Element element) || !"Relationship".equals(element.getLocalName())) {
                continue;
            }
            String type = element.getAttribute("Type");
            String target = element.getAttribute("Target");
            String targetMode = element.getAttribute("TargetMode");
            boolean external = "External".equalsIgnoreCase(targetMode)
                || target.regionMatches(true, 0, "mailto:", 0, "mailto:".length())
                || target.regionMatches(true, 0, "http:", 0, "http:".length())
                || target.regionMatches(true, 0, "https:", 0, "https:".length());
            if (type.endsWith(HYPERLINK_RELATIONSHIP_SUFFIX) && external) {
                String id = element.getAttribute("Id");
                if (!id.isBlank()) {
                    removedIds.add(id);
                }
                document.getDocumentElement().removeChild(element);
            }
        }
        if (removedIds.isEmpty()) {
            return new RelationshipResult(content, Set.of(), false);
        }
        return new RelationshipResult(writeXml(document), removedIds, true);
    }

    private static XmlResult removeWorksheetHyperlinks(byte[] content, Set<String> relationshipIds) throws IOException {
        Document document = parseXml(content);
        NodeList nodes = document.getElementsByTagNameNS("*", "hyperlink");
        boolean changed = false;
        for (int i = nodes.getLength() - 1; i >= 0; i--) {
            Node node = nodes.item(i);
            if (!(node instanceof Element element)) {
                continue;
            }
            String relationshipId = element.getAttributeNS(OFFICE_RELATIONSHIP_NS, "id");
            if (relationshipId.isBlank()) {
                relationshipId = element.getAttribute("r:id");
            }
            if (relationshipIds.contains(relationshipId)) {
                element.getParentNode().removeChild(element);
                changed = true;
            }
        }
        return changed ? new XmlResult(writeXml(document), true) : new XmlResult(content, false);
    }

    private static Document parseXml(byte[] content) throws IOException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);
            return factory.newDocumentBuilder().parse(new ByteArrayInputStream(content));
        } catch (Exception exception) {
            throw new IOException("解析 Excel XML 部件失败", exception);
        }
    }

    private static byte[] writeXml(Document document) throws IOException {
        try {
            TransformerFactory factory = TransformerFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            Transformer transformer = factory.newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
                transformer.transform(new DOMSource(document), new StreamResult(output));
                return output.toByteArray();
            }
        } catch (Exception exception) {
            throw new IOException("重建 Excel XML 部件失败", exception);
        }
    }

    private static String sourcePart(String relationshipPart) {
        int markerIndex = relationshipPart.lastIndexOf(RELATIONSHIP_MARKER);
        if (markerIndex < 0 || !relationshipPart.endsWith(".rels")) {
            return null;
        }
        String base = relationshipPart.substring(0, markerIndex);
        String part = relationshipPart.substring(markerIndex + RELATIONSHIP_MARKER.length(),
            relationshipPart.length() - ".rels".length());
        return base + "/" + part;
    }

    private record RelationshipResult(byte[] content, Set<String> removedIds, boolean changed) {
    }

    private record XmlResult(byte[] content, boolean changed) {
    }
}
