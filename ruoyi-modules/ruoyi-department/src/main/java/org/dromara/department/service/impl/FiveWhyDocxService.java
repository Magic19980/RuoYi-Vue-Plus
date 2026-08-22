package org.dromara.department.service.impl;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.Document;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.department.domain.vo.FiveWhyVo;
import org.dromara.system.domain.vo.SysOssVo;
import org.dromara.system.service.ISysOssService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.http.ResponseEntity;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** 根据5WHY模板生成DOCX。 */
@Service
@RequiredArgsConstructor
public class FiveWhyDocxService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy年M月d日");

    private final ISysOssService ossService;

    @Value("${department.five-why.template-path:classpath:templates/department/five-why-template.docx}")
    private String templatePath;

    public void export(FiveWhyVo record, HttpServletResponse response) throws IOException {
        if (record == null) {
            throw new ServiceException("5WHY分析记录不存在");
        }
        try (XWPFDocument document = openTemplate()) {
            fillDocument(document, record);
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            document.write(output);
            byte[] bytes = output.toByteArray();
            String filename = buildFilename(record);
            response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''"
                + URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20"));
            response.setContentLength(bytes.length);
            response.getOutputStream().write(bytes);
        }
    }

    private XWPFDocument openTemplate() throws IOException {
        Resource resource = new DefaultResourceLoader().getResource(templatePath);
        if (!resource.exists()) {
            throw new ServiceException("5WHY模板不存在：" + templatePath);
        }
        return new XWPFDocument(resource.getInputStream());
    }

    private void fillDocument(XWPFDocument document, FiveWhyVo record) throws IOException {
        List<XWPFTable> tables = document.getTables();
        if (tables.size() < 2 || tables.get(0).getNumberOfRows() < 19 || tables.get(1).getNumberOfRows() < 4) {
            throw new ServiceException("5WHY模板结构不符合约定，请检查模板版本");
        }
        XWPFTable table = tables.get(0);
        setCellText(table.getRow(1).getCell(1), text(record.getCompanyDept(), formatDate(record.getAnalysisDate())));
        setCellText(table.getRow(2).getCell(1), record.getProblemDescription());
        setCellText(table.getRow(3).getCell(1), record.getImpactScope());
        setCellText(table.getRow(4).getCell(1), record.getProblemName());

        List<org.dromara.department.domain.vo.FiveWhyWhyVo> whys = record.getWhys();
        for (int i = 0; i < 5; i++) {
            XWPFTableRow row = table.getRow(6 + i);
            org.dromara.department.domain.vo.FiveWhyWhyVo why = whys != null && whys.size() > i ? whys.get(i) : null;
            setCellText(row.getCell(0), (i + 1) + "WHY");
            setCellText(row.getCell(1), why == null ? "" : why.getQuestion());
            setCellText(row.getCell(2), why == null ? "" : why.getCause());
        }

        List<org.dromara.department.domain.vo.FiveWhyImprovementVo> improvements = record.getImprovements();
        for (int i = 0; i < 4; i++) {
            XWPFTableRow row = table.getRow(12 + i);
            org.dromara.department.domain.vo.FiveWhyImprovementVo item = improvements != null && improvements.size() > i ? improvements.get(i) : null;
            setCellText(row.getCell(0), item == null ? "" : improvementText(item));
            setCellText(row.getCell(1), item == null ? "" : text(item.getResponsible(), "") + dateSuffix(item.getExpectedDate()));
        }

        XWPFTable effectTable = tables.get(1);
        setImageOrText(effectTable.getRow(1).getCell(0), record.getBeforeOssId(), "改善前图片：未上传", "five-why-before");
        setImageOrText(effectTable.getRow(1).getCell(1), record.getAfterOssId(), "改善后图片：未上传", "five-why-after");
        setCellText(effectTable.getRow(3).getCell(0), record.getEffectVerification());

        List<XWPFParagraph> paragraphs = document.getParagraphs();
        if (paragraphs.size() > 6) {
            setParagraphText(paragraphs.get(5), record.getStandardizationPlan());
            setParagraphText(paragraphs.get(6), "");
        }
        if (paragraphs.size() > 10) {
            setParagraphText(paragraphs.get(8), record.getStandardizationExecution());
            setParagraphText(paragraphs.get(9), "");
            setParagraphText(paragraphs.get(10), "");
            setParagraphText(paragraphs.get(11), "注：相关附件可通过系统上传并与本分析记录一并归档。");
        }
    }

    private String improvementText(org.dromara.department.domain.vo.FiveWhyImprovementVo item) {
        String kind = StringUtils.isBlank(item.getKind()) ? "" : "【" + item.getKind().trim() + "】 ";
        return kind + text(item.getMeasure(), "");
    }

    private String buildFilename(FiveWhyVo record) {
        String employeeNo = StringUtils.isBlank(record.getEmployeeNo()) ? "未填写工号" : record.getEmployeeNo();
        return employeeNo + "-" + text(record.getAnalystName(), "分析人") + "-" + text(record.getProblemName(), "5WHY分析")
            + "-5WHY分析表-" + (record.getAnalysisDate() == null ? LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)
            : record.getAnalysisDate().format(DateTimeFormatter.BASIC_ISO_DATE)) + ".docx";
    }

    private void setImageOrText(XWPFTableCell cell, Long ossId, String emptyText, String filename) throws IOException {
        if (ossId == null) {
            setCellText(cell, emptyText);
            return;
        }
        try {
            ResponseEntity<byte[]> response = ossService.download(ossId);
            byte[] bytes = response == null ? null : response.getBody();
            if (bytes == null || bytes.length == 0) {
                setCellText(cell, emptyText);
                return;
            }
            XWPFParagraph paragraph = firstParagraph(cell);
            clearParagraph(paragraph);
            paragraph.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun run = paragraph.createRun();
            SysOssVo oss = ossService.getById(ossId);
            String suffix = oss == null || StringUtils.isBlank(oss.getFileSuffix()) ? "" : oss.getFileSuffix();
            String imageName = filename + suffix;
            run.addPicture(new ByteArrayInputStream(bytes), pictureType(imageName), imageName, Units.toEMU(2.55), Units.toEMU(1.8));
        } catch (Exception ex) {
            setCellText(cell, emptyText);
        }
    }

    private int pictureType(String filename) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return Document.PICTURE_TYPE_JPEG;
        if (lower.endsWith(".gif")) return Document.PICTURE_TYPE_GIF;
        if (lower.endsWith(".bmp")) return Document.PICTURE_TYPE_BMP;
        return Document.PICTURE_TYPE_PNG;
    }

    private void setCellText(XWPFTableCell cell, String value) {
        setParagraphText(firstParagraph(cell), value);
    }

    private XWPFParagraph firstParagraph(XWPFTableCell cell) {
        if (cell.getParagraphs().isEmpty()) return cell.addParagraph();
        return cell.getParagraphs().get(0);
    }

    private void setParagraphText(XWPFParagraph paragraph, String value) {
        clearParagraph(paragraph);
        String text = value == null ? "" : value;
        String[] lines = text.replace("\r\n", "\n").split("\n", -1);
        XWPFRun run = paragraph.createRun();
        run.setText(lines.length == 0 ? "" : lines[0]);
        for (int i = 1; i < lines.length; i++) {
            run.addBreak();
            run.setText(lines[i]);
        }
    }

    private void clearParagraph(XWPFParagraph paragraph) {
        for (int i = paragraph.getRuns().size() - 1; i >= 0; i--) paragraph.removeRun(i);
    }

    private String text(String value, String fallback) {
        return StringUtils.isBlank(value) ? fallback : value.trim();
    }

    private String formatDate(LocalDate date) {
        return date == null ? "" : date.format(DATE_FORMATTER);
    }

    private String dateSuffix(LocalDate date) {
        return date == null ? "" : " " + date.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }
}
