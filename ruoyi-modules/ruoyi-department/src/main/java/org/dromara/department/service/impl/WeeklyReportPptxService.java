package org.dromara.department.service.impl;

import lombok.RequiredArgsConstructor;
import org.apache.poi.sl.usermodel.TextParagraph.TextAlign;
import org.apache.poi.sl.usermodel.VerticalAlignment;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFAutoShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTable;
import org.apache.poi.xslf.usermodel.XSLFTableCell;
import org.apache.poi.xslf.usermodel.XSLFTableRow;
import org.apache.poi.xslf.usermodel.XSLFTextParagraph;
import org.apache.poi.xslf.usermodel.XSLFTextRun;
import org.apache.poi.xslf.usermodel.XSLFTextShape;
import org.apache.poi.sl.usermodel.ShapeType;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.department.domain.vo.WeeklyReportSummaryVo;
import org.dromara.department.domain.vo.OperationSummaryVo;
import org.dromara.department.domain.vo.WorkOrderSummaryVo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 周报 PPT 生成器。存在模板路径时复用模板封面，否则使用内置的可编辑 PPT 结构生成。
 */
@Service
@RequiredArgsConstructor
public class WeeklyReportPptxService {

    private static final Color NAVY = new Color(31, 55, 102);
    private static final Color BLUE = new Color(34, 105, 184);
    private static final Color GOLD = new Color(245, 183, 32);
    private static final Color LIGHT = new Color(241, 245, 249);
    private static final Color TEXT = new Color(31, 41, 55);

    @Value("${department.weekly-report.template-path:classpath:templates/department/weekly-report-template.pptx}")
    private String templatePath;

    public byte[] generate(WeeklyReportSummaryVo summary, String title) throws IOException {
        try (XMLSlideShow presentation = openPresentation()) {
            if (presentation.getSlides().isEmpty()) {
                createCover(presentation, summary, title);
            } else {
                updateTemplateCover(presentation.getSlides().get(0), summary, title);
                while (presentation.getSlides().size() > 1) {
                    presentation.removeSlide(presentation.getSlides().size() - 1);
                }
            }
            createMetricSlide(presentation, summary);
            createWorkOrderSlide(presentation, summary);
            createWorkSlide(presentation, summary);
            createPlanSlide(presentation, summary);
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            presentation.write(output);
            return output.toByteArray();
        }
    }

    private XMLSlideShow openPresentation() throws IOException {
        if (StringUtils.isNotBlank(templatePath)) {
            Resource resource = new DefaultResourceLoader().getResource(templatePath);
            if (resource.exists()) {
                try (InputStream input = resource.getInputStream()) {
                    return new XMLSlideShow(input);
                }
            }
        }
        if (StringUtils.isNotBlank(templatePath)
            && !templatePath.startsWith("classpath:")
            && !templatePath.startsWith("file:")
            && Files.isRegularFile(Path.of(templatePath))) {
            try (InputStream input = Files.newInputStream(Path.of(templatePath))) {
                return new XMLSlideShow(input);
            }
        }
        XMLSlideShow presentation = new XMLSlideShow();
        presentation.setPageSize(new Dimension(1280, 720));
        return presentation;
    }

    private void updateTemplateCover(XSLFSlide slide, WeeklyReportSummaryVo summary, String title) {
        String range = summary.getWeekStart() + "-" + summary.getWeekEnd();
        String generatedDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日"));
        for (var shape : slide.getShapes()) {
            if (!(shape instanceof XSLFTextShape textShape)) {
                continue;
            }
            String text = textShape.getText();
            if (text == null) {
                continue;
            }
            if (text.matches(".*\\d{4}\\.\\d{1,2}\\.\\d{1,2}-\\d{4}\\.\\d{1,2}\\.\\d{1,2}.*")) {
                replaceText(textShape, range);
            } else if (text.contains("年") && text.length() < 30) {
                replaceText(textShape, generatedDate);
            } else if (text.contains("周仕超")) {
                replaceText(textShape, "日报汇总");
            } else if (text.contains("物流系统科工作汇报") && StringUtils.isNotBlank(title)) {
                replaceText(textShape, shorten(title, 12));
            }
        }
    }

    private void createCover(XMLSlideShow presentation, WeeklyReportSummaryVo summary, String title) {
        XSLFSlide slide = presentation.createSlide();
        addBackground(slide, new Color(238, 246, 255));
        addBand(slide, 0, 0, 1280, 18, GOLD);
        addText(slide, title, 95, 180, 1090, 80, 40, NAVY, true, TextAlign.CENTER);
        addText(slide, "科室周报", 95, 275, 1090, 46, 24, BLUE, true, TextAlign.CENTER);
        addText(slide, "汇报周期：" + summary.getWeekStart() + " - " + summary.getWeekEnd(), 330, 380, 620, 42, 20, TEXT, false, TextAlign.CENTER);
        addText(slide, "台账自动汇总", 330, 440, 620, 42, 20, TEXT, false, TextAlign.CENTER);
        addBand(slide, 0, 680, 1280, 40, NAVY);
    }

    private void createMetricSlide(XMLSlideShow presentation, WeeklyReportSummaryVo summary) {
        XSLFSlide slide = presentation.createSlide();
        addPageTitle(slide, "一、指标分析");
        WorkOrderSummaryVo manual = manualSummary(summary);
        OperationSummaryVo operation = operationSummary(summary);
        addMetric(slide, 42, "本周开发投入", formatQuantity(manual), BLUE);
        addMetric(slide, 286, "本周运维总量", String.valueOf(operation.getTotalCount()), new Color(16, 148, 148));
        addMetric(slide, 530, "运维解决率", formatPercent(operation.getResolutionRate()), new Color(43, 160, 86));
        addMetric(slide, 774, "平均处理时长", operation.getAverageProcessingMinutes() + "分钟", new Color(236, 132, 27));
        addMetric(slide, 1018, "系统在线率", formatPercent(operation.getOnlineRate()), new Color(218, 65, 84));

        addBox(slide, 42, 225, 1196, 300, LIGHT, new Color(214, 224, 238));
        addText(slide, "数据口径", 68, 250, 180, 34, 24, NAVY, true, TextAlign.LEFT);
        addText(slide, "人工单指标来自人工单台账；运维总量、运维解决率、平均处理时长、系统在线率及运维工单结构来自运维台账。", 68, 310, 1090, 100, 20, TEXT, false, TextAlign.LEFT);
        addText(slide, "系统在线率按所选周内系统在线率记录的有效值取平均；未维护在线率时显示 0%。", 68, 430, 1090, 45, 16, new Color(100, 116, 139), false, TextAlign.LEFT);
    }

    private void createWorkSlide(XMLSlideShow presentation, WeeklyReportSummaryVo summary) {
        XSLFSlide slide = presentation.createSlide();
        addPageTitle(slide, "三、人工单结构 / 重点项目");
        WorkOrderSummaryVo manual = manualSummary(summary);
        List<List<String>> rows = dimensionRows(manual.getBySystem());
        if (rows.isEmpty()) {
            rows.add(List.of("—", "0", "0%"));
        }
        addTable(slide, 42, 125, 570, 420, List.of("系统/项目", "人工单数", "占比"), rows, new int[]{300, 120, 120});
        addText(slide, "人工单台账用于记录 PDF/手动维护的人工单，开发投入及其系统分布均从这里读取。", 650, 170, 520, 100, 22, TEXT, false, TextAlign.LEFT);
        addBox(slide, 650, 310, 540, 170, LIGHT, new Color(214, 224, 238));
        addText(slide, "本周人工投入", 680, 340, 260, 32, 20, NAVY, true, TextAlign.LEFT);
        addText(slide, String.valueOf(manual.getTotalCount()), 680, 380, 240, 50, 34, BLUE, true, TextAlign.LEFT);
        addText(slide, "数量合计：" + (manual.getTotalQuantity() == null ? "0" : manual.getTotalQuantity().stripTrailingZeros().toPlainString()), 680, 440, 420, 28, 17, new Color(100, 116, 139), false, TextAlign.LEFT);
    }

    private void createPlanSlide(XMLSlideShow presentation, WeeklyReportSummaryVo summary) {
        XSLFSlide slide = presentation.createSlide();
        addPageTitle(slide, "四、运维分类与人工单重点");
        OperationSummaryVo operation = operationSummary(summary);
        WorkOrderSummaryVo manual = manualSummary(summary);
        List<List<String>> methodRows = operationDimensionRows(operation.getByProcessMethod());
        List<List<String>> manualRows = dimensionRows(manual.getByFaultType());
        if (methodRows.isEmpty()) methodRows.add(List.of("—", "0", "0%"));
        if (manualRows.isEmpty()) manualRows.add(List.of("—", "0", "0%"));
        addText(slide, "运维处理方式", 55, 120, 520, 34, 22, NAVY, true, TextAlign.LEFT);
        addTable(slide, 42, 165, 570, 370, List.of("处理方式", "数量", "占比"), methodRows, new int[]{300, 120, 120});
        addText(slide, "人工单类别", 680, 120, 520, 34, 22, NAVY, true, TextAlign.LEFT);
        addTable(slide, 650, 165, 588, 370, List.of("故障/类别", "数量", "占比"), manualRows, new int[]{318, 120, 120});
        addText(slide, "统计数据均来自台账，不再从日报明细生成本页内容。", 55, 590, 1160, 30, 15, new Color(100, 116, 139), false, TextAlign.LEFT);
    }

    private void createWorkOrderSlide(XMLSlideShow presentation, WeeklyReportSummaryVo summary) {
        XSLFSlide slide = presentation.createSlide();
        addPageTitle(slide, "二、运维工单结构");
        OperationSummaryVo operation = operationSummary(summary);
        List<List<String>> systemRows = operationDimensionRows(operation.getBySystem());
        List<List<String>> faultRows = operationDimensionRows(operation.getByFaultType());
        if (systemRows.isEmpty()) systemRows.add(List.of("—", "0", "0%"));
        if (faultRows.isEmpty()) faultRows.add(List.of("—", "0", "0%"));
        addText(slide, "按系统/客户单位", 55, 140, 520, 34, 22, NAVY, true, TextAlign.LEFT);
        addTable(slide, 42, 180, 570, 360, List.of("系统/客户单位", "记录数", "占比"), systemRows, new int[]{300, 120, 120});
        addText(slide, "按故障类型/处理方式", 680, 140, 520, 34, 22, NAVY, true, TextAlign.LEFT);
        addTable(slide, 650, 180, 588, 360, List.of("故障类型", "记录数", "占比"), faultRows, new int[]{318, 120, 120});
        String note = "统计口径：运维总量、解决率、处理时长和结构均统计所选日期范围内的运维工作记录。";
        addText(slide, note, 55, 600, 1160, 34, 15, new Color(100, 116, 139), false, TextAlign.LEFT);
    }

    private List<List<String>> dimensionRows(List<WorkOrderSummaryVo.DimensionCountVo> dimensions) {
        List<List<String>> rows = new ArrayList<>();
        if (dimensions == null) return rows;
        for (WorkOrderSummaryVo.DimensionCountVo dimension : dimensions) {
            rows.add(List.of(shorten(dimension.getName(), 26), String.valueOf(dimension.getCount()), formatPercent(dimension.getPercentage())));
            if (rows.size() >= 6) break;
        }
        return rows;
    }

    private List<List<String>> operationDimensionRows(List<OperationSummaryVo.DimensionCountVo> dimensions) {
        List<List<String>> rows = new ArrayList<>();
        if (dimensions == null) return rows;
        for (OperationSummaryVo.DimensionCountVo dimension : dimensions) {
            rows.add(List.of(shorten(dimension.getName(), 26), String.valueOf(dimension.getCount()), formatPercent(dimension.getPercentage())));
            if (rows.size() >= 6) break;
        }
        return rows;
    }

    private WorkOrderSummaryVo manualSummary(WeeklyReportSummaryVo summary) {
        return summary.getManualOrderSummary() == null ? new WorkOrderSummaryVo() : summary.getManualOrderSummary();
    }

    private OperationSummaryVo operationSummary(WeeklyReportSummaryVo summary) {
        return summary.getOperationSummary() == null ? new OperationSummaryVo() : summary.getOperationSummary();
    }

    private String formatQuantity(WorkOrderSummaryVo summary) {
        if (summary.getTotalLaborQuantity() != null && summary.getTotalLaborQuantity().compareTo(java.math.BigDecimal.ZERO) > 0) {
            return summary.getTotalLaborQuantity().stripTrailingZeros().toPlainString();
        }
        if (summary.getTotalQuantity() != null && summary.getTotalQuantity().compareTo(java.math.BigDecimal.ZERO) > 0) {
            return summary.getTotalQuantity().stripTrailingZeros().toPlainString();
        }
        return String.valueOf(summary.getTotalCount());
    }

    private String formatPercent(java.math.BigDecimal value) {
        return value == null ? "—" : value.stripTrailingZeros().toPlainString() + "%";
    }

    private void addPageTitle(XSLFSlide slide, String title) {
        addBackground(slide, Color.WHITE);
        addText(slide, title, 70, 22, 720, 52, 32, Color.BLACK, true, TextAlign.LEFT);
        addBand(slide, 0, 84, 1280, 6, BLUE);
        addBand(slide, 900, 84, 380, 6, GOLD);
    }

    private void addMetric(XSLFSlide slide, int left, String label, String value, Color color) {
        addBox(slide, left, 105, 205, 88, LIGHT, new Color(214, 224, 238));
        addBand(slide, left, 105, 7, 88, color);
        addText(slide, value, left + 18, 116, 168, 36, 26, color, true, TextAlign.LEFT);
        addText(slide, label, left + 18, 157, 168, 24, 16, TEXT, true, TextAlign.LEFT);
    }

    private void addTable(XSLFSlide slide, int left, int top, int width, int height, List<String> headers, List<List<String>> rows, int[] columnWidths) {
        XSLFTable table = slide.createTable();
        table.setAnchor(new Rectangle(left, top, width, height));
        XSLFTableRow header = table.addRow();
        header.setHeight(42);
        for (int i = 0; i < headers.size(); i++) {
            XSLFTableCell cell = header.addCell();
            cell.setFillColor(NAVY);
            cell.setText(headers.get(i));
            styleCell(cell, Color.WHITE, 17, true, TextAlign.CENTER);
            if (i < columnWidths.length) {
                table.setColumnWidth(i, columnWidths[i]);
            }
        }
        for (List<String> rowData : rows) {
            XSLFTableRow row = table.addRow();
            row.setHeight(Math.max(34, Math.min(66, height / Math.max(2, rows.size() + 1))));
            for (int i = 0; i < headers.size(); i++) {
                XSLFTableCell cell = row.addCell();
                cell.setFillColor(i % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                cell.setText(i < rowData.size() ? rowData.get(i) : "");
                styleCell(cell, TEXT, 15, false, i == 0 ? TextAlign.CENTER : TextAlign.LEFT);
            }
        }
    }

    private void styleCell(XSLFTableCell cell, Color color, double fontSize, boolean bold, TextAlign align) {
        for (XSLFTextParagraph paragraph : cell.getTextParagraphs()) {
            paragraph.setTextAlign(align);
            for (XSLFTextRun run : paragraph.getTextRuns()) {
                run.setFontFamily("Microsoft YaHei");
                run.setFontSize(fontSize);
                run.setBold(bold);
                run.setFontColor(color);
            }
        }
        cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
    }

    private XSLFTextShape addText(XSLFSlide slide, String value, int left, int top, int width, int height, double fontSize, Color color, boolean bold, TextAlign align) {
        XSLFTextShape shape = slide.createTextBox();
        shape.setAnchor(new Rectangle(left, top, width, height));
        shape.setText(value);
        shape.setVerticalAlignment(VerticalAlignment.MIDDLE);
        for (XSLFTextParagraph paragraph : shape.getTextParagraphs()) {
            paragraph.setTextAlign(align);
            for (XSLFTextRun run : paragraph.getTextRuns()) {
                run.setFontFamily("Microsoft YaHei");
                run.setFontSize(fontSize);
                run.setBold(bold);
                run.setFontColor(color);
            }
        }
        return shape;
    }

    private void addBox(XSLFSlide slide, int left, int top, int width, int height, Color fill, Color line) {
        XSLFAutoShape shape = slide.createAutoShape();
        shape.setShapeType(ShapeType.ROUND_RECT);
        shape.setAnchor(new Rectangle(left, top, width, height));
        shape.setFillColor(fill);
        shape.setLineColor(line);
    }

    private void addBand(XSLFSlide slide, int left, int top, int width, int height, Color color) {
        XSLFAutoShape shape = slide.createAutoShape();
        shape.setShapeType(ShapeType.RECT);
        shape.setAnchor(new Rectangle(left, top, width, height));
        shape.setFillColor(color);
        shape.setLineColor(color);
    }

    private void addBackground(XSLFSlide slide, Color color) {
        slide.getBackground().setFillColor(color);
    }

    private void replaceText(XSLFTextShape shape, String value) {
        List<XSLFTextParagraph> paragraphs = shape.getTextParagraphs();
        if (paragraphs.isEmpty() || paragraphs.get(0).getTextRuns().isEmpty()) {
            shape.setText(value);
            return;
        }
        XSLFTextParagraph paragraph = paragraphs.get(0);
        paragraph.getTextRuns().get(0).setText(value);
        for (int i = 1; i < paragraph.getTextRuns().size(); i++) {
            paragraph.getTextRuns().get(i).setText("");
        }
        for (int i = 1; i < paragraphs.size(); i++) {
            paragraphs.get(i).getTextRuns().forEach(run -> run.setText(""));
        }
    }

    private String shorten(String value, int maxLength) {
        if (value == null) {
            return "";
        }
        String compact = value.replace('\n', ' ').replace('\r', ' ').trim();
        return compact.length() <= maxLength ? compact : compact.substring(0, maxLength - 1) + "…";
    }
}
