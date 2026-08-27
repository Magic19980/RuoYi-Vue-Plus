package org.dromara.department.service.impl;

import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.department.domain.vo.DailyReportVo;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 科室日报 Excel 导出服务。
 *
 * <p>日报内容允许录入多行文本，通用导出器无法同时控制换行、行高和日期合并，
 * 因此这里使用 POI 生成面向阅读的明细表。</p>
 */
@Service
public class DailyReportXlsxService {

    private static final String EXPORT_SHEET_NAME = "科室日报";
    private static final String TEMPLATE_SHEET_NAME = "日报导入模板";
    private static final String EXPORT_FILE_NAME = "科室日报.xlsx";
    private static final String TEMPLATE_FILE_NAME = "日报导入模板.xlsx";

    private static final String[] EXPORT_HEADERS = {
        "日报日期", "姓名", "部门", "今日工作", "明日计划", "待协调事项/备注"
    };
    private static final String[] TEMPLATE_HEADERS = {
        "日期", "填报人", "今日工作", "明日计划", "待协调项/备注"
    };
    private static final int[] EXPORT_COLUMN_WIDTHS = {14, 14, 24, 42, 36, 34};
    private static final int[] TEMPLATE_COLUMN_WIDTHS = {14, 18, 42, 36, 34};

    private static final float MIN_ROW_HEIGHT = 28F;
    private static final float LINE_HEIGHT = 17F;
    private static final float ROW_PADDING = 8F;
    private static final float MAX_ROW_HEIGHT = 180F;

    /**
     * 导出日报明细。
     */
    public void export(List<DailyReportVo> records, HttpServletResponse response) {
        try {
            GeneratedXlsx generated = generate(records);
            writeResponse(response, generated.bytes(), generated.fileName());
        } catch (IOException exception) {
            throw new ServiceException("日报导出失败", exception);
        }
    }

    /**
     * 生成日报明细文件内容，便于接口输出和离线校验共用同一份格式逻辑。
     */
    public GeneratedXlsx generate(List<DailyReportVo> records) throws IOException {
        List<DailyReportVo> safeRecords = records == null ? List.of() : records;
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            XSSFSheet sheet = workbook.createSheet(EXPORT_SHEET_NAME);
            ExportStyles styles = createStyles(workbook);
            configureSheet(sheet, EXPORT_COLUMN_WIDTHS);
            writeHeader(sheet, EXPORT_HEADERS, styles.header());

            for (int index = 0; index < safeRecords.size(); index++) {
                DailyReportVo record = safeRecords.get(index);
                Row row = sheet.createRow(index + 1);
                writeReportRow(row, record, styles, index);
                row.setHeightInPoints(calculateRowHeight(record));
            }
            mergeSameDates(sheet, safeRecords, 1);
            if (!safeRecords.isEmpty()) {
                sheet.setAutoFilter(new CellRangeAddress(0, safeRecords.size(), 0, EXPORT_HEADERS.length - 1));
            }
            sheet.createFreezePane(0, 1);

            workbook.write(output);
            return new GeneratedXlsx(output.toByteArray(), EXPORT_FILE_NAME);
        }
    }

    /**
     * 导出日报导入模板。模板只改变展示样式，不改变导入字段和表头名称。
     */
    public void exportTemplate(HttpServletResponse response) {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            XSSFSheet sheet = workbook.createSheet(TEMPLATE_SHEET_NAME);
            ExportStyles styles = createStyles(workbook);
            configureSheet(sheet, TEMPLATE_COLUMN_WIDTHS);
            writeHeader(sheet, TEMPLATE_HEADERS, styles.header());
            sheet.createFreezePane(0, 1);
            sheet.setAutoFilter(new CellRangeAddress(0, 0, 0, TEMPLATE_HEADERS.length - 1));

            workbook.write(output);
            writeResponse(response, output.toByteArray(), TEMPLATE_FILE_NAME);
        } catch (IOException exception) {
            throw new ServiceException("日报模板导出失败", exception);
        }
    }

    private ExportStyles createStyles(XSSFWorkbook workbook) {
        Font headerFont = workbook.createFont();
        headerFont.setFontName("Microsoft YaHei");
        headerFont.setFontHeightInPoints((short) 11);
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());

        XSSFCellStyle header = (XSSFCellStyle) workbook.createCellStyle();
        header.setFont(headerFont);
        header.setFillForegroundColor(rgb(47, 117, 181));
        header.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        header.setAlignment(HorizontalAlignment.CENTER);
        header.setVerticalAlignment(VerticalAlignment.CENTER);
        header.setWrapText(true);
        applyBorder(header, IndexedColors.WHITE.getIndex());

        CellStyle center = createBodyStyle(workbook, false, true);
        CellStyle centerAlternate = createBodyStyle(workbook, true, true);
        CellStyle text = createBodyStyle(workbook, false, false);
        CellStyle textAlternate = createBodyStyle(workbook, true, false);
        CellStyle date = createBodyStyle(workbook, false, true);
        date.setDataFormat(workbook.createDataFormat().getFormat("yyyy-mm-dd"));
        CellStyle dateAlternate = createBodyStyle(workbook, true, true);
        dateAlternate.setDataFormat(workbook.createDataFormat().getFormat("yyyy-mm-dd"));
        return new ExportStyles(header, center, centerAlternate, text, textAlternate, date, dateAlternate);
    }

    private CellStyle createBodyStyle(XSSFWorkbook workbook, boolean alternate, boolean centered) {
        XSSFCellStyle style = (XSSFCellStyle) workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontName("Microsoft YaHei");
        font.setFontHeightInPoints((short) 10);
        font.setColor(IndexedColors.GREY_80_PERCENT.getIndex());
        style.setFont(font);
        if (alternate) {
            style.setFillForegroundColor(rgb(247, 250, 252));
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        }
        style.setAlignment(centered ? HorizontalAlignment.CENTER : HorizontalAlignment.LEFT);
        style.setVerticalAlignment(centered ? VerticalAlignment.CENTER : VerticalAlignment.TOP);
        style.setWrapText(true);
        applyBorder(style, IndexedColors.GREY_25_PERCENT.getIndex());
        return style;
    }

    private void writeHeader(XSSFSheet sheet, String[] headers, CellStyle style) {
        Row row = sheet.createRow(0);
        row.setHeightInPoints(30F);
        for (int index = 0; index < headers.length; index++) {
            Cell cell = row.createCell(index);
            cell.setCellValue(headers[index]);
            cell.setCellStyle(style);
        }
    }

    private void writeReportRow(Row row, DailyReportVo record, ExportStyles styles, int index) {
        boolean alternate = index % 2 == 1;
        CellStyle centerStyle = alternate ? styles.centerAlternate() : styles.center();
        CellStyle textStyle = alternate ? styles.textAlternate() : styles.text();
        CellStyle dateStyle = alternate ? styles.dateAlternate() : styles.date();

        Cell dateCell = row.createCell(0);
        if (record.getReportDate() != null) {
            dateCell.setCellValue(toDate(record.getReportDate()));
        }
        dateCell.setCellStyle(dateStyle);

        writeTextCell(row, 1, record.getNickName(), centerStyle);
        writeTextCell(row, 2, record.getDeptName(), centerStyle);
        writeTextCell(row, 3, record.getTodayWork(), textStyle);
        writeTextCell(row, 4, record.getTomorrowPlan(), textStyle);
        writeTextCell(row, 5, record.getCoordinationNote(), textStyle);
    }

    private void writeTextCell(Row row, int column, String value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value == null ? "" : value);
        cell.setCellStyle(style);
    }

    private void mergeSameDates(XSSFSheet sheet, List<DailyReportVo> records, int firstDataRow) {
        int start = 0;
        while (start < records.size()) {
            LocalDate date = records.get(start).getReportDate();
            int end = start;
            while (end + 1 < records.size()
                && date != null
                && Objects.equals(date, records.get(end + 1).getReportDate())) {
                end++;
            }
            if (date != null && end > start) {
                sheet.addMergedRegion(new CellRangeAddress(firstDataRow + start, firstDataRow + end, 0, 0));
            }
            start = end + 1;
        }
    }

    private float calculateRowHeight(DailyReportVo record) {
        int lines = Math.max(visualLines(record.getTodayWork(), 36), visualLines(record.getTomorrowPlan(), 30));
        lines = Math.max(lines, visualLines(record.getCoordinationNote(), 28));
        return Math.min(MAX_ROW_HEIGHT, Math.max(MIN_ROW_HEIGHT, ROW_PADDING + lines * LINE_HEIGHT));
    }

    private int visualLines(String value, int charactersPerLine) {
        if (value == null || value.isEmpty()) return 1;
        String normalized = value.replace("\r\n", "\n").replace('\r', '\n');
        int lines = 0;
        for (String line : normalized.split("\n", -1)) {
            int displayWidth = line.codePoints()
                .map(character -> character <= 255 ? 1 : 2)
                .sum();
            lines += Math.max(1, (displayWidth + charactersPerLine - 1) / charactersPerLine);
        }
        return Math.max(1, lines);
    }

    private void configureSheet(XSSFSheet sheet, int[] widths) {
        sheet.setDisplayGridlines(false);
        sheet.setDefaultRowHeightInPoints(MIN_ROW_HEIGHT);
        sheet.setZoom(90);
        sheet.getPrintSetup().setLandscape(true);
        sheet.getPrintSetup().setFitWidth((short) 1);
        sheet.getPrintSetup().setFitHeight((short) 0);
        sheet.setFitToPage(true);
        for (int index = 0; index < widths.length; index++) {
            sheet.setColumnWidth(index, widths[index] * 256);
        }
    }

    private void applyBorder(CellStyle style, short color) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setTopBorderColor(color);
        style.setBottomBorderColor(color);
        style.setLeftBorderColor(color);
        style.setRightBorderColor(color);
    }

    private XSSFColor rgb(int red, int green, int blue) {
        return new XSSFColor(new byte[]{(byte) red, (byte) green, (byte) blue}, null);
    }

    private Date toDate(LocalDate value) {
        return Date.from(value.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private void writeResponse(HttpServletResponse response, byte[] bytes, String fileName) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''"
            + URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20"));
        response.setContentLength(bytes.length);
        response.getOutputStream().write(bytes);
    }

    public record GeneratedXlsx(byte[] bytes, String fileName) {
    }

    private record ExportStyles(CellStyle header, CellStyle center, CellStyle centerAlternate,
                                CellStyle text, CellStyle textAlternate, CellStyle date, CellStyle dateAlternate) {
    }
}
