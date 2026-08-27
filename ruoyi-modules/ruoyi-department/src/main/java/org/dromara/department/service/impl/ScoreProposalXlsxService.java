package org.dromara.department.service.impl;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFPicture;
import org.apache.poi.xssf.usermodel.XSSFPictureData;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFShape;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.department.domain.vo.PersonUserOptionVo;
import org.dromara.department.domain.vo.ScoreProposalVo;
import org.dromara.department.mapper.PersonProfileMapper;
import org.dromara.system.domain.bo.SysDeptBo;
import org.dromara.system.domain.vo.SysDeptVo;
import org.dromara.system.domain.vo.SysOssVo;
import org.dromara.system.service.ISysDictDataService;
import org.dromara.system.service.ISysDeptService;
import org.dromara.system.service.ISysOssService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.util.CellRangeAddress;

/** 根据SCORE模板生成XLSX。 */
@Service
@RequiredArgsConstructor
public class ScoreProposalXlsxService {

    /** 模板要求固定显示的企业参与人员双语标题。 */
    private static final String FIXED_EIT_TEAM_HEADER = "Nama Anggota Tim EIT Departemen\n部门EIT小组成员名称";
    private static final String DEFAULT_COMPANY_TEXT = "PT. Tsingyao Electric Indonesia\n印尼青耀电气有限公司";

    private final ISysOssService ossService;
    private final ISysDictDataService dictDataService;
    private final PersonProfileMapper personProfileMapper;
    private final ISysDeptService sysDeptService;

    @Value("${department.score-proposal.template-path:classpath:templates/department/score-proposal-template.xlsx}")
    private String templatePath;

    public void export(ScoreProposalVo record, HttpServletResponse response) throws IOException {
        GeneratedXlsx generated = generate(record);
        byte[] bytes = generated.bytes();
        String filename = generated.fileName();
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''"
            + URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20"));
        response.setContentLength(bytes.length);
        response.getOutputStream().write(bytes);
    }

    /** 生成不可变的提案文件内容，提交审核和下载共用同一份生成逻辑。 */
    public GeneratedXlsx generate(ScoreProposalVo record) throws IOException {
        if (record == null) throw new ServiceException("SCORE提案不存在");
        try (XSSFWorkbook workbook = openTemplate()) {
            XSSFSheet sheet = workbook.getSheet("EIP企业改进提案表");
            if (sheet == null) throw new ServiceException("SCORE模板缺少EIP企业改进提案表工作表");
            fillSheet(workbook, sheet, record);
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            workbook.write(output);
            return new GeneratedXlsx(output.toByteArray(), "SCORE提案-" + text(record.getProposerName(), "未命名")
                + "-" + dateText(record.getStartDate()) + "-V" + (record.getRevisionNo() == null ? 1 : record.getRevisionNo()) + ".xlsx");
        }
    }

    public record GeneratedXlsx(byte[] bytes, String fileName) {
    }

    public String preview(ScoreProposalVo record) throws IOException {
        GeneratedXlsx generated = generate(record);
        return preview(generated.bytes(), generated.fileName());
    }

    /** 将提交时固化的工作簿按 Excel 模板的版式转换为只读 HTML，预览不修改原文件。 */
    public String preview(byte[] bytes, String fileName) throws IOException {
        if (bytes == null || bytes.length == 0) throw new ServiceException("审核文件内容为空");
        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            DataFormatter formatter = new DataFormatter();
            StringBuilder html = new StringBuilder("<!doctype html><html><head><meta charset=\"UTF-8\"><title>")
                .append(escape(fileName == null ? "SCORE提案预览" : fileName))
                .append("</title><style>")
                .append("html,body{margin:0;min-height:100%;background:#eef1f5;color:#1f2937;font-family:Arial,'Microsoft YaHei','宋体',sans-serif}")
                .append("body{padding:28px;box-sizing:border-box}")
                .append(".sheet{width:max-content;min-width:calc(100vw - 56px);box-sizing:border-box;background:#fff;padding:26px 28px 30px;box-shadow:0 2px 12px rgba(31,41,55,.12)}")
                .append(".sheet h2{margin:0 0 20px;font-size:22px;line-height:1.2;font-weight:700;color:#111827}")
                .append(".sheet-scroll{overflow:auto;max-width:calc(100vw - 112px);padding-bottom:4px}")
                .append(".grid{border-collapse:collapse;table-layout:fixed;background:#fff}")
                .append(".grid td{box-sizing:border-box;padding:4px 6px;white-space:pre-wrap;overflow-wrap:anywhere;word-break:break-word}")
                .append(".grid img{display:block;max-width:100%;max-height:100%;object-fit:contain;margin:auto}")
                .append("</style></head><body>");
            for (int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {
                if (workbook.isSheetHidden(sheetIndex) || workbook.isSheetVeryHidden(sheetIndex)) continue;
                XSSFSheet sheet = workbook.getSheetAt(sheetIndex);
                int columnCount = usedColumnCount(sheet);
                // 模板工作表的显示缩放为 85%，POI 5.5.1 没有提供读取该视图属性的 API。
                double zoom = 0.85d;
                Map<String, String> pictures = collectPictures(sheet);
                html.append("<section class=\"sheet\"><h2>").append(escape(sheet.getSheetName()))
                    .append("</h2><div class=\"sheet-scroll\"><table class=\"grid\" style=\"width:")
                    .append(totalWidth(sheet, columnCount, zoom)).append("px\"><colgroup>");
                for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
                    html.append("<col style=\"width:").append(columnWidth(sheet, columnIndex, zoom)).append("px\">");
                }
                html.append("</colgroup>");
                for (int rowIndex = 0; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                    org.apache.poi.ss.usermodel.Row row = sheet.getRow(rowIndex);
                    if (row == null) continue;
                    html.append("<tr style=\"height:").append(rowHeight(row, sheet, zoom)).append("px\">");
                    for (int cellIndex = 0; cellIndex < columnCount; cellIndex++) {
                        CellRangeAddress merged = mergedRangeAt(sheet, rowIndex, cellIndex);
                        if (merged != null && (merged.getFirstRow() != rowIndex || merged.getFirstColumn() != cellIndex)) continue;
                        Cell cell = row.getCell(cellIndex, org.apache.poi.ss.usermodel.Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                        Cell anchorCell = cell == null ? row.getCell(cellIndex, org.apache.poi.ss.usermodel.Row.MissingCellPolicy.CREATE_NULL_AS_BLANK) : cell;
                        html.append("<td style=\"").append(cellStyle(workbook, sheet, anchorCell, merged, zoom)).append("\"");
                        if (merged != null) {
                            html.append(" colspan=\"").append(merged.getLastColumn() - merged.getFirstColumn() + 1).append("\" rowspan=\"")
                                .append(merged.getLastRow() - merged.getFirstRow() + 1).append("\"");
                        }
                        html.append(">");
                        if (cell != null) html.append(escape(formatter.formatCellValue(cell)));
                        String picture = pictures.get(imageKey(rowIndex, cellIndex));
                        if (picture != null) html.append("<img src=\"").append(picture).append("\" alt=\"\">");
                        html.append("</td>");
                    }
                    html.append("</tr>");
                }
                html.append("</table></div></section>");
            }
            return html.append("</body></html>").toString();
        }
    }

    private int usedColumnCount(XSSFSheet sheet) {
        int count = 1;
        for (int rowIndex = 0; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            org.apache.poi.ss.usermodel.Row row = sheet.getRow(rowIndex);
            if (row != null && row.getLastCellNum() > count) count = row.getLastCellNum();
        }
        for (CellRangeAddress range : sheet.getMergedRegions()) count = Math.max(count, range.getLastColumn() + 1);
        return count;
    }

    private int columnWidth(XSSFSheet sheet, int columnIndex, double zoom) {
        return Math.max(30, (int) Math.round(sheet.getColumnWidth(columnIndex) / 256f * 7f * zoom));
    }

    private int totalWidth(XSSFSheet sheet, int columnCount, double zoom) {
        int width = 0;
        for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) width += columnWidth(sheet, columnIndex, zoom);
        return width;
    }

    private int rowHeight(org.apache.poi.ss.usermodel.Row row, XSSFSheet sheet, double zoom) {
        float points = row.getHeightInPoints();
        if (points <= 0) points = sheet.getDefaultRowHeightInPoints();
        return Math.max(16, (int) Math.round(points * 96f / 72f * zoom));
    }

    private CellRangeAddress mergedRangeAt(XSSFSheet sheet, int rowIndex, int columnIndex) {
        for (CellRangeAddress range : sheet.getMergedRegions()) {
            if (range.isInRange(rowIndex, columnIndex)) return range;
        }
        return null;
    }

    private String cellStyle(XSSFWorkbook workbook, XSSFSheet sheet, Cell cell, CellRangeAddress merged, double zoom) {
        CellStyle style = cell.getCellStyle();
        Font font = workbook.getFontAt(style.getFontIndex());
        StringBuilder css = new StringBuilder();
        css.append("font-family:").append(cssFont(font.getFontName())).append(';')
            .append("font-size:").append(Math.max(8d, font.getFontHeightInPoints() * zoom)).append("pt;")
            .append("font-weight:").append(font.getBold() ? "700" : "400").append(';')
            .append("font-style:").append(font.getItalic() ? "italic" : "normal").append(';')
            .append("text-align:").append(horizontal(style.getAlignment())).append(';')
            .append("vertical-align:").append(vertical(style.getVerticalAlignment())).append(';')
            .append("white-space:").append(style.getWrapText() ? "pre-wrap" : "pre").append(';')
            .append("padding:").append(Math.max(2, Math.round(4f * (float) zoom))).append("px ")
            .append(Math.max(3, Math.round(6f * (float) zoom))).append("px;");
            if (style.getIndention() > 0) css.append("padding-left:")
                .append(Math.max(4, Math.round((6 + style.getIndention() * 4) * (float) zoom))).append("px;");
        String fontColor = font instanceof XSSFFont xssfFont ? xssfColor(xssfFont.getXSSFColor()) : null;
        if (fontColor != null) css.append("color:").append(fontColor).append(';');
        if (style instanceof XSSFCellStyle xssfStyle && xssfStyle.getFillPattern() == FillPatternType.SOLID_FOREGROUND) {
            String fill = xssfColor(xssfStyle.getFillForegroundColorColor());
            if (fill != null) css.append("background-color:").append(fill).append(';');
        }
        CellStyle top = boundaryStyle(sheet, merged, "top", cell);
        CellStyle right = boundaryStyle(sheet, merged, "right", cell);
        CellStyle bottom = boundaryStyle(sheet, merged, "bottom", cell);
        CellStyle left = boundaryStyle(sheet, merged, "left", cell);
        css.append("border-top:").append(border(top, "top")).append(';')
            .append("border-right:").append(border(right, "right")).append(';')
            .append("border-bottom:").append(border(bottom, "bottom")).append(';')
            .append("border-left:").append(border(left, "left")).append(';');
        return css.toString();
    }

    private CellStyle boundaryStyle(XSSFSheet sheet, CellRangeAddress merged, String side, Cell fallback) {
        if (merged == null) return fallback.getCellStyle();
        int rowIndex = switch (side) {
            case "bottom" -> merged.getLastRow();
            default -> merged.getFirstRow();
        };
        int columnIndex = switch (side) {
            case "right" -> merged.getLastColumn();
            default -> merged.getFirstColumn();
        };
        Cell boundary = sheet.getRow(rowIndex) == null ? null : sheet.getRow(rowIndex).getCell(columnIndex);
        return boundary == null ? fallback.getCellStyle() : boundary.getCellStyle();
    }

    private String border(CellStyle style, String side) {
        BorderStyle borderStyle = switch (side) {
            case "top" -> style.getBorderTop();
            case "right" -> style.getBorderRight();
            case "bottom" -> style.getBorderBottom();
            default -> style.getBorderLeft();
        };
        String color = null;
        if (style instanceof XSSFCellStyle xssfStyle) {
            XSSFColor xssfColor = switch (side) {
                case "top" -> xssfStyle.getTopBorderXSSFColor();
                case "right" -> xssfStyle.getRightBorderXSSFColor();
                case "bottom" -> xssfStyle.getBottomBorderXSSFColor();
                default -> xssfStyle.getLeftBorderXSSFColor();
            };
            color = xssfColor(xssfColor);
        }
        return borderCss(borderStyle, color == null ? "#9aa1aa" : color);
    }

    private String borderCss(BorderStyle style, String color) {
        if (style == null || style == BorderStyle.NONE) return "none";
        String width = switch (style) {
            case MEDIUM, MEDIUM_DASHED, MEDIUM_DASH_DOT, MEDIUM_DASH_DOT_DOT -> "2px";
            case THICK -> "3px";
            case DOUBLE -> "3px";
            default -> "1px";
        };
        String line = style == BorderStyle.DOUBLE ? "double" : (style.name().contains("DASH") ? "dashed" : "solid");
        return width + " " + line + " " + color;
    }

    private String horizontal(HorizontalAlignment alignment) {
        return switch (alignment) {
            case CENTER, CENTER_SELECTION, FILL, DISTRIBUTED, JUSTIFY -> "center";
            case RIGHT -> "right";
            default -> "left";
        };
    }

    private String vertical(VerticalAlignment alignment) {
        return switch (alignment) {
            case CENTER, DISTRIBUTED, JUSTIFY -> "middle";
            case BOTTOM -> "bottom";
            default -> "top";
        };
    }

    private String cssFont(String value) {
        return "'" + (value == null ? "Arial" : value.replace("'", "")) + "'";
    }

    private String xssfColor(XSSFColor color) {
        if (color == null || color.getARGBHex() == null) return null;
        String value = color.getARGBHex();
        return "#" + (value.length() == 8 ? value.substring(2) : value);
    }

    private Map<String, String> collectPictures(XSSFSheet sheet) {
        Map<String, String> pictures = new HashMap<>();
        XSSFDrawing drawing = sheet.getDrawingPatriarch();
        if (drawing == null) return pictures;
        for (XSSFShape shape : drawing.getShapes()) {
            if (!(shape instanceof XSSFPicture picture)) continue;
            XSSFClientAnchor anchor = picture.getPreferredSize();
            XSSFPictureData data = picture.getPictureData();
            if (anchor == null || data == null || data.getData() == null) continue;
            pictures.put(imageKey(anchor.getRow1(), anchor.getCol1()), "data:" + data.getMimeType() + ";base64,"
                + Base64.getEncoder().encodeToString(data.getData()));
        }
        return pictures;
    }

    private String imageKey(int rowIndex, int columnIndex) {
        return rowIndex + ":" + columnIndex;
    }

    private String escape(String value) {
        return value == null ? "" : value.replace("&", "&amp;").replace("<", "&lt;")
            .replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }

    private XSSFWorkbook openTemplate() throws IOException {
        Resource resource = new DefaultResourceLoader().getResource(templatePath);
        if (!resource.exists()) throw new ServiceException("SCORE模板不存在：" + templatePath);
        return new XSSFWorkbook(resource.getInputStream());
    }

    private void fillSheet(XSSFWorkbook workbook, XSSFSheet sheet, ScoreProposalVo record) throws IOException {
        setText(sheet, 2, 5, companyTemplateText(record));
        setText(sheet, 3, 5, teamMembersTemplateText(record));

        setText(sheet, 6, 0, "1");
        setText(sheet, 6, 1, proposerLevelText(record.getProposerLevel()));
        setText(sheet, 6, 2, text(record.getMainCategory(), ""));
        setText(sheet, 6, 3, text(record.getSubCategory(), ""));
        setText(sheet, 6, 4, text(record.getProblemDescription(), ""));
        setText(sheet, 6, 5, text(record.getImprovementMeasure(), ""));
        setText(sheet, 6, 6, proposerTemplateText(record));
        setText(sheet, 6, 7, text(record.getEmployeeNo(), ""));
        setText(sheet, 6, 8, departmentTemplateText(record));
        setText(sheet, 6, 9, implementerSupervisorTemplateText(record));
        setImage(workbook, sheet, record.getBeforeOssId(), 10, 6, "score-before");
        setImage(workbook, sheet, record.getAfterOssId(), 11, 6, "score-after");
        setDate(sheet, 6, 12, record.getStartDate());
        setDate(sheet, 6, 13, record.getPlannedCompletionDate());
        setDate(sheet, 6, 14, record.getActualCompletionDate());
        setText(sheet, 6, 15, text(record.getCompletionStatus(), "进行中"));
        setText(sheet, 6, 16, text(record.getRemark(), ""));
        // 按业务要求不收集固化清单和推广清单，模板对应列保持空白。
        setText(sheet, 6, 17, "");
        setText(sheet, 6, 18, "");

        String summary = "本周提出改进项：1项，完成改进项：" + (record.getActualCompletionDate() == null ? "0" : "1")
            + "项，完成率" + (record.getActualCompletionDate() == null ? "0%" : "100%");
        setText(sheet, 7, 4, summary);
    }

    private void setImage(XSSFWorkbook workbook, XSSFSheet sheet, Long ossId, int col, int row, String name) throws IOException {
        // 模板图片单元格可能预置了 DISPIMG 公式，必须设为 BLANK 才能彻底清除公式和值。
        Cell imageCell = sheet.getRow(row).getCell(col, org.apache.poi.ss.usermodel.Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        imageCell.setBlank();
        if (ossId == null) return;
        try {
            ResponseEntity<byte[]> response = ossService.download(ossId);
            byte[] bytes = response == null ? null : response.getBody();
            if (bytes == null || bytes.length == 0) return;
            int pictureType = pictureType(ossService.getById(ossId));
            int pictureId = workbook.addPicture(bytes, pictureType);
            XSSFDrawing drawing = sheet.createDrawingPatriarch();
            XSSFClientAnchor anchor = new XSSFClientAnchor(0, 0, 0, 0, col, row, col + 1, row + 1);
            XSSFPicture picture = drawing.createPicture(anchor, pictureId);
            picture.resize(1.0, 1.0);
        } catch (Exception ignored) {
            // 图片为可选内容，图片服务异常时保留文字表格输出。
        }
    }

    private int pictureType(SysOssVo oss) {
        String suffix = oss == null ? "" : text(oss.getFileSuffix(), "").toLowerCase();
        if (suffix.endsWith("jpg") || suffix.endsWith("jpeg")) return Workbook.PICTURE_TYPE_JPEG;
        // POI 的 XSSF 图片常量不包含 GIF，GIF按 PNG 图片类型写入表格。
        if (suffix.endsWith("bmp")) return Workbook.PICTURE_TYPE_DIB;
        return Workbook.PICTURE_TYPE_PNG;
    }

    private void setDate(XSSFSheet sheet, int row, int col, LocalDate value) {
        Cell cell = sheet.getRow(row).getCell(col, org.apache.poi.ss.usermodel.Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        if (value == null) {
            cell.setBlank();
            return;
        }
        cell.setCellValue(value.getMonthValue() + "月" + value.getDayOfMonth() + "日");
    }

    private void setText(XSSFSheet sheet, int row, int col, String value) {
        Cell cell = sheet.getRow(row).getCell(col, org.apache.poi.ss.usermodel.Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        cell.setCellValue(value == null ? "" : value);
    }

    private String proposerTemplateText(ScoreProposalVo record) {
        PersonUserOptionVo option = record.getProposerUserId() == null
            ? null : personProfileMapper.selectUserOptionById(record.getProposerUserId());
        String chineseName = option == null
            ? text(record.getProposerName(), "")
            : firstNonBlank(option.getNickName(), record.getProposerName(), option.getUserName());
        String indonesianName = option == null ? "" : text(option.getIndonesianName(), "");
        String chineseRole = option == null
            ? text(record.getProposerRole(), "")
            : firstNonBlank(option.getJobTitle(), record.getProposerRole());
        String indonesianRole = option == null ? "" : text(option.getJobTitleIndonesianName(), "");
        return groupedBilingual(List.of(indonesianName, indonesianRole), List.of(chineseName, chineseRole));
    }

    private String companyTemplateText(ScoreProposalVo record) {
        String selectedDepartmentName = text(record.getCompanyName(), "");
        if (selectedDepartmentName.isEmpty()) return DEFAULT_COMPANY_TEXT;

        SysDeptBo query = new SysDeptBo();
        query.setDeptName(selectedDepartmentName);
        SysDeptVo department = sysDeptService.selectDeptList(query).stream()
            .filter(item -> selectedDepartmentName.equals(item.getDeptName()))
            .findFirst()
            .orElse(null);
        if (department == null) return selectedDepartmentName;
        return groupedBilingual(
            List.of(text(department.getIndonesianName(), "")),
            List.of(text(department.getDeptName(), selectedDepartmentName)));
    }

    private String teamMembersTemplateText(ScoreProposalVo record) {
        List<Long> userIds = record.getTeamMemberUserIds();
        if (userIds == null || userIds.isEmpty()) return FIXED_EIT_TEAM_HEADER;

        Map<Long, PersonUserOptionVo> optionsById = new HashMap<>();
        for (PersonUserOptionVo option : personProfileMapper.selectUserOptionsByIds(userIds)) {
            if (option.getUserId() != null) optionsById.put(option.getUserId(), option);
        }
        List<String> chineseNames = new ArrayList<>();
        for (Long userId : userIds) {
            PersonUserOptionVo option = optionsById.get(userId);
            if (option == null) continue;
            addUniqueNonBlank(chineseNames, firstNonBlank(option.getNickName(), option.getUserName()));
        }
        String names = String.join("、", chineseNames);
        return names.isEmpty() ? FIXED_EIT_TEAM_HEADER : FIXED_EIT_TEAM_HEADER + "\n" + names;
    }

    private String departmentTemplateText(ScoreProposalVo record) {
        SysDeptVo department = record.getDeptId() == null ? null : sysDeptService.selectDeptById(record.getDeptId());
        String chineseName = department == null
            ? text(record.getDeptName(), "")
            : firstNonBlank(department.getDeptName(), record.getDeptName());
        String indonesianName = department == null ? "" : text(department.getIndonesianName(), "");
        return bilingual(indonesianName, chineseName);
    }

    private String implementerSupervisorTemplateText(ScoreProposalVo record) {
        List<Long> userIds = record.getImplementerUserIds();
        if (userIds == null || userIds.isEmpty()) return text(record.getImplementerSupervisor(), "");

        Map<Long, PersonUserOptionVo> optionsById = new HashMap<>();
        for (PersonUserOptionVo option : personProfileMapper.selectUserOptionsByIds(userIds)) {
            if (option.getUserId() != null) optionsById.put(option.getUserId(), option);
        }
        List<String> indonesianNames = new ArrayList<>();
        List<String> chineseNames = new ArrayList<>();
        for (Long userId : userIds) {
            PersonUserOptionVo option = optionsById.get(userId);
            if (option == null) continue;
            String chineseName = firstNonBlank(option.getNickName(), option.getUserName());
            String indonesianName = text(option.getIndonesianName(), "");
            addUniqueNonBlank(indonesianNames, indonesianName);
            addUniqueNonBlank(chineseNames, chineseName);
        }
        String groupedNames = groupedBilingual(indonesianNames, chineseNames);
        return groupedNames.isEmpty() ? text(record.getImplementerSupervisor(), "") : groupedNames;
    }

    private String bilingual(String indonesian, String chinese) {
        indonesian = text(indonesian, "");
        chinese = text(chinese, "");
        if (indonesian.isEmpty()) return chinese;
        if (chinese.isEmpty() || indonesian.equalsIgnoreCase(chinese)) return indonesian;
        return indonesian + "\n" + chinese;
    }

    /** 双语字段统一先放印尼语，再放中文；同一内容只保留一次。 */
    private String groupedBilingual(List<String> indonesianLines, List<String> chineseLines) {
        List<String> lines = new ArrayList<>();
        indonesianLines.forEach(value -> addUniqueNonBlank(lines, value));
        chineseLines.forEach(value -> addUniqueNonBlank(lines, value));
        return String.join("\n", lines);
    }

    private void addUniqueNonBlank(List<String> values, String value) {
        if (StringUtils.isBlank(value)) return;
        String normalized = value.trim();
        if (values.stream().noneMatch(item -> item.equalsIgnoreCase(normalized))) values.add(normalized);
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (!StringUtils.isBlank(value)) return value.trim();
        }
        return "";
    }

    private String dateText(LocalDate value) {
        return value == null ? LocalDate.now().toString() : value.toString();
    }

    private String proposerLevelText(String value) {
        String label = StringUtils.isBlank(value) ? "" : dictDataService.selectDictLabel("dm_score_job", value);
        return text(label, text(value, "基层员工"));
    }

    private String text(String value, String fallback) {
        return StringUtils.isBlank(value) ? fallback : value.trim();
    }
}
