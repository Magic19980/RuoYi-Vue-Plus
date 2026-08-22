package org.dromara.department.service.impl;

import lombok.Data;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.contentstream.PDFGraphicsStreamEngine;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.graphics.image.PDImage;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.PDFTextStripperByArea;
import org.apache.pdfbox.text.TextPosition;
import org.dromara.common.core.exception.ServiceException;
import org.springframework.stereotype.Component;

import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 解析信息部人工单/工程量统计明细 PDF。
 *
 * <p>当前模板是可复制文本的横向表格，优先读取 PDF 内实际绘制的表格边界，再按边界
 * 读取单元格，避免普通文本提取把空的“工程量/中国人工/印尼人工”列挤在一起。只有
 * 少数没有表格矢量边界的兼容文件才使用文本位置兜底。扫描件没有可复制文本时返回
 * 明确异常，后续可在此处增加 OCR 适配器，不把 OCR 结果直接当成已确认工单。</p>
 */
@Component
public class WorkOrderPdfParser {

    private static final Pattern YEAR_MONTH = Pattern.compile("(20\\d{2})年\\s*(\\d{1,2})月");
    private static final Pattern PERIOD_RANGE = Pattern.compile("(\\d{1,2})月\\s*(\\d{1,2})日\\s*[-—至到]+\\s*(\\d{1,2})月\\s*(\\d{1,2})日");
    private static final Pattern NUMBER = Pattern.compile("[-+]?\\d+(?:\\.\\d+)?");
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");
    /**
     * 没有表格绘图信息时的兼容兜底；正常解析会从 PDF 矢量表格边框动态读取边界。
     */
    private static final float REFERENCE_ROW_X = 47.2f;
    private static final int[][] FALLBACK_COLUMN_BOUNDS = {
        {40, 57}, {57, 125}, {125, 201}, {201, 237}, {237, 313}, {313, 388},
        {388, 424}, {424, 461}, {461, 498}, {498, 535}, {535, 598}, {598, 661}, {661, 802}
    };

    public ParseResult parse(byte[] content, String fileName) {
        if (content == null || content.length == 0) {
            throw new ServiceException("PDF文件不能为空");
        }
        try (PDDocument document = Loader.loadPDF(content)) {
            String documentText = extractDocumentText(document);
            if (documentText.isBlank()) {
                throw new ServiceException("PDF没有可复制文本，当前版本暂不支持扫描件，请先转成文字版PDF");
            }
            Period period = inferPeriod(documentText, fileName);
            List<ParsedRow> rows = new ArrayList<>();
            for (int pageIndex = 0; pageIndex < document.getNumberOfPages(); pageIndex++) {
                rows.addAll(parsePage(document, pageIndex, period));
            }
            if (rows.isEmpty()) {
                throw new ServiceException("未识别到工单明细行，请确认PDF使用了信息部人工单模板");
            }
            ParseResult result = new ParseResult();
            result.setPageCount(document.getNumberOfPages());
            result.setRows(rows);
            result.setPeriodStart(period.start());
            result.setPeriodEnd(period.end());
            return result;
        } catch (IOException ex) {
            throw new ServiceException("读取PDF失败：" + ex.getMessage());
        }
    }

    private String extractDocumentText(PDDocument document) throws IOException {
        PDFTextStripper stripper = new PDFTextStripper();
        stripper.setSortByPosition(true);
        return stripper.getText(document);
    }

    private List<ParsedRow> parsePage(PDDocument document, int pageIndex, Period period) throws IOException {
        PDPage page = document.getPage(pageIndex);
        PositionCollector collector = new PositionCollector();
        collector.setSortByPosition(true);
        collector.setStartPage(pageIndex + 1);
        collector.setEndPage(pageIndex + 1);
        collector.getText(document);

        TableGeometryCollector geometry = new TableGeometryCollector(page);
        geometry.processPage(page);
        List<Float> columnBoundaries = geometry.findColumnBoundaries(page.getMediaBox().getWidth(), page.getMediaBox().getHeight());
        List<Float> horizontalBoundaries = geometry.findHorizontalBoundaries(page.getMediaBox().getWidth(), page.getMediaBox().getHeight());
        if (columnBoundaries.size() >= FALLBACK_COLUMN_BOUNDS.length && horizontalBoundaries.size() >= 2) {
            List<ParsedRow> rows = new ArrayList<>();
            for (int index = 0; index + 1 < horizontalBoundaries.size(); index++) {
                float top = horizontalBoundaries.get(index) + 1;
                float bottom = horizontalBoundaries.get(index + 1) - 1;
                rows.addAll(parseRow(page, pageIndex, period, top, bottom, readColumns(page, top, bottom, columnBoundaries)));
            }
            return rows;
        }
        return parseRowsByTextPosition(page, pageIndex, period, collector);
    }

    private List<ParsedRow> parseRowsByTextPosition(PDPage page, int pageIndex, Period period, PositionCollector collector) throws IOException {
        List<Float> rowY = collector.findRowY(page.getMediaBox().getHeight());
        List<ParsedRow> rows = new ArrayList<>();
        for (int index = 0; index < rowY.size(); index++) {
            float top = index == 0
                ? Math.max(0, rowY.get(index) - 18)
                : (rowY.get(index - 1) + rowY.get(index)) / 2;
            float bottom = index + 1 < rowY.size()
                ? (rowY.get(index) + rowY.get(index + 1)) / 2
                : Math.min(page.getMediaBox().getHeight() - 40, collector.findFooterY(rowY.get(index), page.getMediaBox().getHeight()) - 8);
            if (bottom <= top) {
                continue;
            }
            float rowOffset = collector.findRowX(rowY.get(index)) - REFERENCE_ROW_X;
            rows.addAll(parseRow(page, pageIndex, period, top, bottom, readFallbackColumns(page, top, bottom, rowOffset)));
        }
        return rows;
    }

    private List<ParsedRow> parseRow(PDPage page, int pageIndex, Period period, float top, float bottom, List<String> columns) {
        List<ParsedRow> rows = new ArrayList<>();
        if (bottom <= top || columns.size() < FALLBACK_COLUMN_BOUNDS.length || isBlankRow(columns)) {
            return rows;
        }
        String sequence = clean(columns.get(0));
        if (!sequence.matches("\\d+")) {
            return rows;
        }
        ParsedRow row = new ParsedRow();
        row.setPage(pageIndex + 1);
        row.setSequence(Integer.parseInt(sequence));
        row.setRequestDept(clean(columns.get(1)));
        row.setSettlementUnit(clean(columns.get(2)));
        row.setProjectOwner(clean(columns.get(3)));
        row.setSystemName(clean(columns.get(4)));
        row.setTitle(clean(columns.get(5)));
        row.setUnit(clean(columns.get(6)));
        String engineeringQuantity = clean(columns.get(7));
        String chineseLabor = clean(columns.get(8));
        String indonesiaLabor = clean(columns.get(9));
        String quantityText = firstNumber(engineeringQuantity, chineseLabor, indonesiaLabor);
        row.setEngineeringQuantity(engineeringQuantity);
        row.setChineseLabor(chineseLabor);
        row.setIndonesiaLabor(indonesiaLabor);
        row.setQuantity(parseDecimal(quantityText));
        row.setInstallDepartment(clean(columns.get(10)));
        row.setInstallTeam(clean(columns.get(11)));
        row.setWorkContent(clean(columns.get(12)));
        row.setPeriodStart(period.start());
        row.setPeriodEnd(period.end());
        row.setParseConfidence(confidence(row));
        row.setParseMessage(parseMessage(engineeringQuantity, chineseLabor, indonesiaLabor, row));
        rows.add(row);
        return rows;
    }

    private List<String> readColumns(PDPage page, float top, float bottom, List<Float> boundaries) throws IOException {
        List<String> columns = new ArrayList<>();
        for (int index = 0; index + 1 < boundaries.size(); index++) {
            PDFTextStripperByArea area = new PDFTextStripperByArea();
            area.setSortByPosition(true);
            int left = Math.round(boundaries.get(index)) + 1;
            int right = Math.round(boundaries.get(index + 1)) - 1;
            area.addRegion("column", new Rectangle(left, Math.round(top), Math.max(1, right - left), Math.max(1, Math.round(bottom - top))));
            area.extractRegions(page);
            columns.add(area.getTextForRegion("column"));
        }
        return columns;
    }

    private List<String> readFallbackColumns(PDPage page, float top, float bottom, float rowOffset) throws IOException {
        List<String> columns = new ArrayList<>();
        for (int[] bound : FALLBACK_COLUMN_BOUNDS) {
            PDFTextStripperByArea area = new PDFTextStripperByArea();
            area.setSortByPosition(true);
            int left = Math.round(bound[0] + rowOffset) + 1;
            int right = Math.round(bound[1] + rowOffset) - 2;
            area.addRegion("column", new Rectangle(left, Math.round(top), Math.max(1, right - left), Math.max(1, Math.round(bottom - top))));
            area.extractRegions(page);
            columns.add(area.getTextForRegion("column"));
        }
        return columns;
    }

    private boolean isBlankRow(List<String> columns) {
        return columns.stream().skip(1).allMatch(value -> clean(value).isBlank());
    }

    private Period inferPeriod(String text, String fileName) {
        String source = (text == null ? "" : text) + " " + (fileName == null ? "" : fileName);
        Matcher yearMonthMatcher = YEAR_MONTH.matcher(source);
        int year = Year.now().getValue();
        int month = 1;
        if (yearMonthMatcher.find()) {
            year = Integer.parseInt(yearMonthMatcher.group(1));
            month = Integer.parseInt(yearMonthMatcher.group(2));
        }
        Matcher rangeMatcher = PERIOD_RANGE.matcher(source);
        if (rangeMatcher.find()) {
            int startMonth = Integer.parseInt(rangeMatcher.group(1));
            int startDay = Integer.parseInt(rangeMatcher.group(2));
            int endMonth = Integer.parseInt(rangeMatcher.group(3));
            int endDay = Integer.parseInt(rangeMatcher.group(4));
            int endYear = endMonth < startMonth ? year + 1 : year;
            try {
                return new Period(LocalDate.of(year, startMonth, startDay), LocalDate.of(endYear, endMonth, endDay));
            } catch (RuntimeException ex) {
                // 非法日期回退到月份范围，并继续保留导入结果。
            }
        }
        YearMonth yearMonth = YearMonth.of(year, month);
        return new Period(yearMonth.atDay(1), yearMonth.atEndOfMonth());
    }

    private String firstNumber(String... values) {
        for (String value : values) {
            Matcher matcher = NUMBER.matcher(value == null ? "" : value);
            if (matcher.find()) {
                return matcher.group();
            }
        }
        return "";
    }

    private BigDecimal parseDecimal(String value) {
        if (value.isBlank()) {
            return BigDecimal.ONE;
        }
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException ex) {
            return BigDecimal.ONE;
        }
    }

    private BigDecimal confidence(ParsedRow row) {
        int score = 0;
        if (!row.getRequestDept().isBlank()) score += 15;
        if (!row.getSystemName().isBlank()) score += 25;
        if (!row.getTitle().isBlank()) score += 15;
        if (!row.getUnit().isBlank()) score += 10;
        if (!row.getWorkContent().isBlank()) score += 20;
        if (row.getQuantity() != null) score += 15;
        return BigDecimal.valueOf(score);
    }

    private String parseMessage(String engineeringQuantity, String chineseLabor, String indonesiaLabor, ParsedRow row) {
        List<String> messages = new ArrayList<>();
        if (engineeringQuantity.isBlank() && !chineseLabor.isBlank()) {
            messages.add("工程量为空，已使用中国人工列作为工单量");
        } else if (engineeringQuantity.isBlank() && chineseLabor.isBlank() && !indonesiaLabor.isBlank()) {
            messages.add("工程量为空，已使用印尼人工列作为工单量");
        }
        if (row.getPeriodStart() != null && row.getPeriodEnd() != null) {
            messages.add("发生年月已从PDF标题提取为" + row.getPeriodStart().format(MONTH_FORMATTER));
        }
        messages.add("状态、故障类型、处理时长需人工确认");
        return String.join("；", messages);
    }

    private String clean(String value) {
        if (value == null) {
            return "";
        }
        // 表格单元格内的换行通常只是 PDF 文本分片（例如“创\n建”），不能转为空格，否则会改变原字段内容。
        return value.replace('\u00a0', ' ').replaceAll("[\\r\\n]+", "").replaceAll("\\s+", " ").trim();
    }

    /**
     * 读取 PDF 内实际绘制的表格线。表格线随页面缩放、纸张大小变化而变化，使用它们比固定坐标可靠。
     */
    private static final class TableGeometryCollector extends PDFGraphicsStreamEngine {

        private final List<RectangleBox> rectangles = new ArrayList<>();

        private Point2D currentPoint;

        private TableGeometryCollector(PDPage page) {
            super(page);
        }

        @Override
        public void appendRectangle(Point2D p0, Point2D p1, Point2D p2, Point2D p3) {
            float pageHeight = getPage().getMediaBox().getHeight();
            double minX = Math.min(Math.min(p0.getX(), p1.getX()), Math.min(p2.getX(), p3.getX()));
            double maxX = Math.max(Math.max(p0.getX(), p1.getX()), Math.max(p2.getX(), p3.getX()));
            double minY = Math.min(Math.min(p0.getY(), p1.getY()), Math.min(p2.getY(), p3.getY()));
            double maxY = Math.max(Math.max(p0.getY(), p1.getY()), Math.max(p2.getY(), p3.getY()));
            rectangles.add(new RectangleBox((float) minX, (float) maxX, pageHeight - (float) maxY, pageHeight - (float) minY));
        }

        private List<Float> findColumnBoundaries(float pageWidth, float pageHeight) {
            return distinct(rectangles.stream()
                .filter(box -> box.width() <= 2 && box.height() >= pageHeight * 0.25f && box.top() > 20 && box.bottom() < pageHeight * 0.85f)
                .map(RectangleBox::x0)
                .sorted()
                .toList());
        }

        private List<Float> findHorizontalBoundaries(float pageWidth, float pageHeight) {
            return distinct(rectangles.stream()
                .filter(box -> box.width() >= pageWidth * 0.60f && box.height() <= 2 && box.top() > 20 && box.bottom() < pageHeight * 0.85f)
                .map(RectangleBox::top)
                .sorted()
                .toList());
        }

        private List<Float> distinct(List<Float> values) {
            List<Float> result = new ArrayList<>();
            for (Float value : values) {
                if (result.isEmpty() || Math.abs(result.get(result.size() - 1) - value) > 2) {
                    result.add(value);
                }
            }
            return result;
        }

        @Override
        public void drawImage(PDImage pdImage) {
        }

        @Override
        public void clip(int windingRule) {
        }

        @Override
        public void moveTo(float x, float y) {
            currentPoint = new Point2D.Float(x, y);
        }

        @Override
        public void lineTo(float x, float y) {
            currentPoint = new Point2D.Float(x, y);
        }

        @Override
        public void curveTo(float x1, float y1, float x2, float y2, float x3, float y3) {
            currentPoint = new Point2D.Float(x3, y3);
        }

        @Override
        public Point2D getCurrentPoint() {
            return currentPoint;
        }

        @Override
        public void closePath() {
        }

        @Override
        public void endPath() {
        }

        @Override
        public void strokePath() {
        }

        @Override
        public void fillPath(int windingRule) {
        }

        @Override
        public void fillAndStrokePath(int windingRule) {
        }

        @Override
        public void shadingFill(COSName shadingName) {
        }
    }

    private record RectangleBox(float x0, float x1, float top, float bottom) {

        private float width() {
            return x1 - x0;
        }

        private float height() {
            return bottom - top;
        }
    }

    private static final class PositionCollector extends PDFTextStripper {

        private final List<TextPosition> positions = new ArrayList<>();

        private PositionCollector() throws IOException {
            super();
        }

        @Override
        protected void writeString(String text, List<TextPosition> textPositions) {
            positions.addAll(textPositions);
        }

        private List<Float> findRowY(float pageHeight) {
            Set<Integer> yValues = new LinkedHashSet<>();
            for (TextPosition position : positions) {
                String value = position.getUnicode();
                if (value != null && value.matches("\\d") && position.getXDirAdj() < 100 && position.getYDirAdj() > 65 && position.getYDirAdj() < pageHeight - 50) {
                    yValues.add(Math.round(position.getYDirAdj()));
                }
            }
            return yValues.stream().map(Integer::floatValue).sorted(Comparator.naturalOrder()).toList();
        }

        private float findRowX(float rowY) {
            return positions.stream()
                .filter(position -> position.getUnicode() != null
                    && position.getUnicode().matches("\\d")
                    && position.getXDirAdj() < 100
                    && Math.abs(position.getYDirAdj() - rowY) < 2)
                .map(TextPosition::getXDirAdj)
                .min(Float::compareTo)
                .orElse(REFERENCE_ROW_X);
        }

        private float findFooterY(float lastRowY, float pageHeight) {
            return positions.stream()
                .filter(position -> position.getXDirAdj() < 120
                    && position.getYDirAdj() > lastRowY + 12
                    && position.getYDirAdj() < pageHeight - 20
                    && position.getUnicode() != null
                    && !position.getUnicode().matches("\\d"))
                .map(TextPosition::getYDirAdj)
                .min(Float::compareTo)
                .orElse(pageHeight - 40);
        }
    }

    private record Period(LocalDate start, LocalDate end) {
    }

    @Data
    public static class ParsedRow {

        private int page;

        private int sequence;

        private String requestDept = "";

        private String settlementUnit = "";

        private String projectOwner = "";

        private String systemName = "";

        private String title = "";

        private String unit = "";

        private BigDecimal quantity = BigDecimal.ONE;

        private String engineeringQuantity = "";

        private String chineseLabor = "";

        private String indonesiaLabor = "";

        private String installDepartment = "";

        private String installTeam = "";

        private String workContent = "";

        private LocalDate periodStart;

        private LocalDate periodEnd;

        private BigDecimal parseConfidence;

        private String parseMessage;
    }

    @Data
    public static class ParseResult {

        private int pageCount;

        private LocalDate periodStart;

        private LocalDate periodEnd;

        private List<ParsedRow> rows = new ArrayList<>();
    }
}
