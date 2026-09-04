package org.dromara.ecology.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.ecology.domain.OaImportBusinessConfig;
import org.dromara.ecology.domain.bo.OaImportBusinessConfigBo;
import org.dromara.ecology.domain.vo.OaImportAttachmentColumnVo;
import org.dromara.ecology.domain.vo.OaImportAttachmentFixedCandidateVo;
import org.dromara.ecology.domain.vo.OaImportBusinessConfigVo;
import org.dromara.ecology.domain.vo.OaImportAttachmentTemplateVo;
import org.dromara.ecology.domain.vo.OaBusinessTypeVo;
import org.dromara.ecology.domain.vo.OaImportTemplateFieldVo;
import org.dromara.ecology.domain.vo.OaImportTemplatePreviewVo;
import org.dromara.ecology.mapper.OaImportBusinessConfigMapper;
import org.dromara.ecology.mapper.OaDepartmentApprovalMapper;
import org.dromara.ecology.domain.OaDepartmentApproval;
import org.dromara.ecology.service.impl.OaImportBusinessServiceImpl.FieldDefinition;
import org.dromara.ecology.service.impl.OaImportBusinessServiceImpl.AttachmentConfig;
import org.dromara.ecology.service.impl.OaImportBusinessServiceImpl.FixedCellMapping;
import org.dromara.ecology.service.IOaBusinessTypeService;
import org.dromara.ecology.service.IOaWorkflowConfigService;
import org.dromara.ecology.service.IOaImportBusinessConfigService;
import org.dromara.system.domain.SysOssExt;
import org.dromara.system.domain.vo.SysOssVo;
import org.dromara.system.service.ISysOssService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** 通用导入业务模板配置服务实现。 */
@Service
@RequiredArgsConstructor
public class OaImportBusinessConfigServiceImpl implements IOaImportBusinessConfigService {

    private static final String ENABLED = "ENABLED";

    private final OaImportBusinessConfigMapper mapper;
    private final OaDepartmentApprovalMapper approvalMapper;
    private final IOaBusinessTypeService businessTypeService;
    private final IOaWorkflowConfigService workflowConfigService;
    private final ISysOssService ossService;

    @Override
    public List<OaImportBusinessConfigVo> queryList(String businessType, boolean enabledOnly) {
        return mapper.selectList(Wrappers.<OaImportBusinessConfig>lambdaQuery()
            .eq(StringUtils.isNotBlank(businessType), OaImportBusinessConfig::getBusinessType, StringUtils.trim(businessType))
            .eq(enabledOnly, OaImportBusinessConfig::getStatus, ENABLED)
            .orderByAsc(OaImportBusinessConfig::getBusinessName)
            .orderByAsc(OaImportBusinessConfig::getId))
            .stream().map(this::toVo).toList();
    }

    @Override
    public OaImportBusinessConfigVo queryById(Long id) {
        OaImportBusinessConfig entity = mapper.selectById(id);
        if (entity == null) {
            throw new ServiceException("通用导入业务模板不存在");
        }
        return toVo(entity);
    }

    @Override
    public OaImportTemplatePreviewVo parseTemplate(InputStream inputStream) {
        if (inputStream == null) {
            throw new ServiceException("Excel 模板不能为空");
        }
        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            if (workbook.getNumberOfSheets() == 0) {
                throw new ServiceException("Excel 模板没有工作表");
            }
            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter(Locale.ROOT);
            int headerRowIndex = detectHeaderRow(sheet, formatter);
            Row headerRow = sheet.getRow(headerRowIndex);
            if (headerRow == null || headerRow.getLastCellNum() <= 0) {
                throw new ServiceException("无法识别 Excel 表头，请检查模板");
            }
            List<OaImportTemplateFieldVo> fields = new ArrayList<>();
            Set<String> codes = new HashSet<>();
            int sampleRowCount = Math.max(0, Math.min(sheet.getLastRowNum() - headerRowIndex, 3));
            for (int columnIndex = 0; columnIndex < headerRow.getLastCellNum(); columnIndex++) {
                String header = StringUtils.trim(formatter.formatCellValue(headerRow.getCell(columnIndex)));
                if (StringUtils.isBlank(header)) {
                    continue;
                }
                String code = uniqueCode(fieldCode(header, columnIndex), codes);
                String sample = firstSample(sheet, headerRowIndex + 1, columnIndex, formatter);
                OaImportTemplateFieldVo field = new OaImportTemplateFieldVo();
                field.setCode(code);
                field.setHeader(header);
                field.setSample(sample);
                field.setType(fieldType(sample));
                field.setRequired(isOrganizationHeader(header));
                field.setUniqueKey(false);
                field.setRole(fieldRole(header));
                fields.add(field);
            }
            if (fields.isEmpty()) {
                throw new ServiceException("Excel 模板没有可用表头");
            }
            String deptField = fields.stream().filter(item -> "业务组织".equals(item.getRole()))
                .map(OaImportTemplateFieldVo::getCode).findFirst().orElse(null);
            String companyField = fields.stream().filter(item -> "公司".equals(item.getRole()))
                .map(OaImportTemplateFieldVo::getCode).findFirst().orElse(null);
            List<String> groupBy = deptField == null ? List.of() : List.of(deptField);
            OaImportTemplatePreviewVo preview = new OaImportTemplatePreviewVo();
            preview.setSheetName(sheet.getSheetName());
            preview.setHeaderRow(headerRowIndex);
            preview.setSampleRowCount(sampleRowCount);
            preview.setFields(fields);
            preview.setFieldDefinitionsJson(JsonUtils.toJsonString(fields.stream().map(this::fieldDefinition).toList()));
            preview.setDeptField(deptField);
            preview.setCompanyField(companyField);
            preview.setGroupByJson(JsonUtils.toJsonString(groupBy));
            return preview;
        } catch (ServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ServiceException("解析 Excel 模板失败：" + ex.getMessage());
        }
    }

    @Override
    public OaImportAttachmentTemplateVo uploadAttachmentTemplate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ServiceException("附件模板不能为空");
        }
        String originalName = StringUtils.trim(file.getOriginalFilename());
        if (StringUtils.isBlank(originalName)
            || !(originalName.toLowerCase(Locale.ROOT).endsWith(".xlsx")
            || originalName.toLowerCase(Locale.ROOT).endsWith(".xls"))) {
            throw new ServiceException("附件模板仅支持 .xlsx 或 .xls 文件");
        }
        if (file.getSize() > 20 * 1024 * 1024L) {
            throw new ServiceException("附件模板不能超过 20MB");
        }
        try {
            byte[] content = file.getBytes();
            AttachmentTemplateShape shape = inspectAttachmentTemplate(content);
            SysOssExt ossExt = new SysOssExt();
            ossExt.setBizType("OA_IMPORT_ATTACHMENT_TEMPLATE");
            ossExt.setSource("OA_IMPORT_CONFIG");
            ossExt.setRefType("OA_IMPORT_ATTACHMENT_TEMPLATE");
            ossExt.setIsTemp(false);
            SysOssVo oss = ossService.upload(file, ossExt);

            OaImportAttachmentTemplateVo result = new OaImportAttachmentTemplateVo();
            result.setOssId(oss.getOssId());
            result.setFileName(originalName);
            result.setSheetName(shape.sheetName());
            result.setSheetNames(shape.sheetNames());
            result.setHeaderRow(shape.headerRow());
            result.setDataStartRow(shape.headerRow() + 1);
            result.setHeaders(shape.headers());
            result.setColumns(shape.columns());
            result.setTotalRow(shape.totalRow());
            result.setFixedCandidates(shape.fixedCandidates());
            return result;
        } catch (ServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ServiceException("解析附件模板失败：" + ex.getMessage());
        }
    }

    private AttachmentTemplateShape inspectAttachmentTemplate(byte[] content) {
        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(content))) {
            if (workbook.getNumberOfSheets() == 0) {
                throw new ServiceException("附件模板没有工作表");
            }
            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter(Locale.ROOT);
            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
            int headerRowIndex = detectHeaderRow(sheet, formatter);
            Row headerRow = sheet.getRow(headerRowIndex);
            if (headerRow == null || headerRow.getLastCellNum() <= 0) {
                throw new ServiceException("无法识别附件模板表头");
            }
            Integer totalRow = detectTotalRow(sheet, headerRowIndex, formatter, evaluator);
            Row prototypeRow = sheet.getRow(headerRowIndex + 1);
            int columnCount = headerRow.getLastCellNum();
            if (prototypeRow != null && prototypeRow.getLastCellNum() > columnCount) {
                columnCount = prototypeRow.getLastCellNum();
            }
            List<String> headers = new ArrayList<>();
            List<OaImportAttachmentColumnVo> columns = new ArrayList<>();
            boolean hasHeader = false;
            boolean hasPrototypeContent = false;
            for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
                String header = StringUtils.trim(formatter.formatCellValue(headerRow.getCell(columnIndex)));
                headers.add(header);
                OaImportAttachmentColumnVo column = new OaImportAttachmentColumnVo();
                column.setColumnIndex(columnIndex);
                column.setLabel(header);
                columns.add(column);
                hasHeader |= StringUtils.isNotBlank(header);
                if (prototypeRow != null) {
                    org.apache.poi.ss.usermodel.Cell cell = prototypeRow.getCell(columnIndex);
                    hasPrototypeContent |= cell != null && cell.getCellType() != org.apache.poi.ss.usermodel.CellType.BLANK;
                }
            }
            if (!hasHeader && !hasPrototypeContent) {
                throw new ServiceException("附件模板没有可用表头");
            }
            List<String> sheetNames = new ArrayList<>();
            for (int index = 0; index < workbook.getNumberOfSheets(); index++) {
                sheetNames.add(workbook.getSheetAt(index).getSheetName());
            }
            List<OaImportAttachmentFixedCandidateVo> fixedCandidates = detectFixedCandidates(
                sheet, totalRow, formatter, evaluator);
            return new AttachmentTemplateShape(sheet.getSheetName(), sheetNames, headerRowIndex, headers, columns,
                totalRow, fixedCandidates);
        } catch (ServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ServiceException("附件模板不是有效的 Excel 文件：" + ex.getMessage());
        }
    }

    private record AttachmentTemplateShape(String sheetName, List<String> sheetNames, Integer headerRow,
                                           List<String> headers, List<OaImportAttachmentColumnVo> columns,
                                           Integer totalRow, List<OaImportAttachmentFixedCandidateVo> fixedCandidates) {
    }

    /** 在整行中识别合计标识，兼容合计标签不在第一列且只有公式的模板。 */
    private Integer detectTotalRow(Sheet sheet, int headerRowIndex, DataFormatter formatter,
                                   FormulaEvaluator evaluator) {
        for (Row row : sheet) {
            if (row.getRowNum() <= headerRowIndex) {
                continue;
            }
            boolean summaryFormula = false;
            StringBuilder rowText = new StringBuilder();
            short lastCellNum = row.getLastCellNum();
            for (int columnIndex = 0; lastCellNum > 0 && columnIndex < lastCellNum; columnIndex++) {
                Cell cell = row.getCell(columnIndex);
                if (cell == null) {
                    continue;
                }
                String value = StringUtils.trim(formatter.formatCellValue(cell, evaluator));
                if (StringUtils.isNotBlank(value)) {
                    rowText.append(value);
                }
                if (cell.getCellType() == org.apache.poi.ss.usermodel.CellType.FORMULA
                    && cell.getCellFormula().matches("(?i).*\\b(SUM|SUBTOTAL)\\s*\\(.*")) {
                    summaryFormula = true;
                }
            }
            String text = rowText.toString();
            if (text.contains("合计") || text.contains("总计") || summaryFormula) {
                return row.getRowNum();
            }
        }
        return null;
    }

    /** 仅扫描合计行之后的非空文本单元格，作为待确认候选，不自动写入配置。 */
    private List<OaImportAttachmentFixedCandidateVo> detectFixedCandidates(Sheet sheet, Integer totalRow,
                                                                            DataFormatter formatter,
                                                                            FormulaEvaluator evaluator) {
        if (totalRow == null) {
            return List.of();
        }
        List<OaImportAttachmentFixedCandidateVo> candidates = new ArrayList<>();
        for (Row row : sheet) {
            if (row.getRowNum() <= totalRow || row.getLastCellNum() <= 0) {
                continue;
            }
            for (int columnIndex = 0; columnIndex < row.getLastCellNum(); columnIndex++) {
                Cell cell = row.getCell(columnIndex);
                if (cell == null || cell.getCellType() == org.apache.poi.ss.usermodel.CellType.FORMULA) {
                    continue;
                }
                String content = StringUtils.trim(formatter.formatCellValue(cell, evaluator));
                if (StringUtils.isBlank(content) || !isMergedTopLeftOrUnmerged(sheet, row.getRowNum(), columnIndex)) {
                    continue;
                }
                String label = content;
                String originalValue = "";
                int colonIndex = Math.max(content.indexOf('：'), content.indexOf(':'));
                if (colonIndex >= 0) {
                    label = StringUtils.trim(content.substring(0, colonIndex + 1));
                    originalValue = StringUtils.trim(content.substring(colonIndex + 1));
                }
                OaImportAttachmentFixedCandidateVo candidate = new OaImportAttachmentFixedCandidateVo();
                candidate.setCell(new CellReference(row.getRowNum(), columnIndex).formatAsString());
                candidate.setRow(row.getRowNum());
                candidate.setColumn(columnIndex);
                candidate.setOriginalContent(content);
                candidate.setLabel(label);
                candidate.setOriginalValue(originalValue);
                candidate.setMerged(isMergedCell(sheet, row.getRowNum(), columnIndex));
                candidates.add(candidate);
            }
        }
        return candidates;
    }

    private boolean isMergedTopLeftOrUnmerged(Sheet sheet, int row, int column) {
        for (CellRangeAddress region : sheet.getMergedRegions()) {
            if (region.isInRange(row, column)) {
                return region.getFirstRow() == row && region.getFirstColumn() == column;
            }
        }
        return true;
    }

    private boolean isMergedCell(Sheet sheet, int row, int column) {
        for (CellRangeAddress region : sheet.getMergedRegions()) {
            if (region.isInRange(row, column)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public byte[] buildTemplate(Long id) {
        OaImportBusinessConfig config = mapper.selectById(id);
        if (config == null) {
            throw new ServiceException("通用导入业务模板不存在");
        }
        if (StringUtils.isBlank(config.getFieldDefinitionsJson())) {
            throw new ServiceException("该业务模板尚未解析 Excel 字段");
        }
        List<FieldDefinition> fields = JsonUtils.parseArray(config.getFieldDefinitionsJson(), FieldDefinition.class);
        if (fields.isEmpty()) {
            throw new ServiceException("该业务模板没有可下载的 Excel 字段");
        }
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            String sheetName = safeSheetName(StringUtils.isBlank(config.getSheetName()) ? "数据" : config.getSheetName());
            Sheet sheet = workbook.createSheet(sheetName);
            int headerRowIndex = Math.max(0, config.getHeaderRow() == null ? 0 : config.getHeaderRow());
            Row headerRow = sheet.createRow(headerRowIndex);
            for (int index = 0; index < fields.size(); index++) {
                String header = StringUtils.isBlank(fields.get(index).getHeader())
                    ? fields.get(index).getCode() : fields.get(index).getHeader();
                headerRow.createCell(index).setCellValue(header);
                sheet.setColumnWidth(index, Math.min(60 * 256, Math.max(14 * 256, (header.length() + 4) * 256)));
            }
            sheet.createFreezePane(0, headerRowIndex + 1);
            workbook.write(output);
            return output.toByteArray();
        } catch (Exception ex) {
            throw new ServiceException("生成 Excel 模板失败：" + ex.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean insertByBo(OaImportBusinessConfigBo bo) {
        validate(bo);
        OaImportBusinessConfig entity = new OaImportBusinessConfig();
        copy(bo, entity);
        return mapper.insert(entity) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateByBo(OaImportBusinessConfigBo bo) {
        validate(bo);
        OaImportBusinessConfig entity = mapper.selectById(bo.getId());
        if (entity == null) {
            throw new ServiceException("通用导入业务模板不存在");
        }
        copy(bo, entity);
        return mapper.updateById(entity) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteById(Long id) {
        return mapper.deleteById(id) > 0;
    }

    private void copy(OaImportBusinessConfigBo bo, OaImportBusinessConfig entity) {
        entity.setBusinessType(StringUtils.trim(bo.getBusinessType()));
        entity.setBusinessName(StringUtils.trim(bo.getBusinessName()));
        entity.setSheetName(StringUtils.trim(bo.getSheetName()));
        entity.setHeaderRow(bo.getHeaderRow() == null ? 0 : bo.getHeaderRow());
        entity.setFieldDefinitionsJson(trim(bo.getFieldDefinitionsJson()));
        entity.setParameterDefinitionsJson(trim(bo.getParameterDefinitionsJson()));
        entity.setGroupByJson(trim(bo.getGroupByJson()));
        entity.setDeptField(StringUtils.trim(bo.getDeptField()));
        entity.setCompanyField(StringUtils.trim(bo.getCompanyField()));
        entity.setAggregationJson(trim(bo.getAggregationJson()));
        entity.setFormMappingJson(trim(bo.getFormMappingJson()));
        entity.setAttachmentConfigJson(trim(bo.getAttachmentConfigJson()));
        entity.setRequestNameTemplate(StringUtils.trim(bo.getRequestNameTemplate()));
        entity.setContentTemplate(bo.getContentTemplate());
        entity.setDefaultWorkflowConfigId(bo.getDefaultWorkflowConfigId());
        entity.setDefaultApprovalPlanId(bo.getDefaultApprovalPlanId());
        entity.setDefaultApprovalMode(normalizeApprovalMode(bo.getDefaultApprovalMode()));
        entity.setStatus("DISABLED".equalsIgnoreCase(bo.getStatus()) ? "DISABLED" : ENABLED);
        entity.setRemark(bo.getRemark());
    }

    private String safeSheetName(String value) {
        String result = value.replaceAll("[\\\\/?*\\[\\]:]", "_");
        return result.length() > 31 ? result.substring(0, 31) : result;
    }

    private int detectHeaderRow(Sheet sheet, DataFormatter formatter) {
        int lastRow = Math.min(sheet.getLastRowNum(), 20);
        int bestRow = 0;
        int bestScore = 0;
        for (int rowIndex = 0; rowIndex <= lastRow; rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row == null) {
                continue;
            }
            int score = 0;
            for (int columnIndex = row.getFirstCellNum(); columnIndex >= 0 && columnIndex < row.getLastCellNum(); columnIndex++) {
                if (StringUtils.isNotBlank(formatter.formatCellValue(row.getCell(columnIndex)))) {
                    score++;
                }
            }
            if (score > bestScore) {
                bestScore = score;
                bestRow = rowIndex;
            }
        }
        return bestRow;
    }

    private String firstSample(Sheet sheet, int startRow, int columnIndex, DataFormatter formatter) {
        for (int rowIndex = startRow; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row == null) {
                continue;
            }
            String value = StringUtils.trim(formatter.formatCellValue(row.getCell(columnIndex)));
            if (StringUtils.isNotBlank(value)) {
                return value.length() > 80 ? value.substring(0, 80) : value;
            }
        }
        return "";
    }

    private String uniqueCode(String base, Set<String> codes) {
        String code = base;
        int suffix = 2;
        while (!codes.add(code)) {
            code = base + suffix++;
        }
        return code;
    }

    private String fieldCode(String header, int index) {
        String normalized = normalize(header);
        if (normalized.contains("车牌")) return "plateNo";
        if (normalized.contains("结算部门") || normalized.contains("归属部门") || normalized.equals("部门")
            || normalized.contains("业务部门") || normalized.contains("组织")) return "businessDept";
        if (normalized.contains("公司")) return "companyName";
        if (normalized.contains("金额") || normalized.contains("含税额") || normalized.contains("总额")) return "amount";
        if (normalized.contains("客户") || normalized.contains("供应商")) return "customerName";
        if (normalized.contains("合同")) return "contractNo";
        if (normalized.contains("月份") || normalized.contains("期间") || normalized.contains("日期")) return "period";
        return "field" + (index + 1);
    }

    private String fieldRole(String header) {
        String normalized = normalize(header);
        if (normalized.contains("部门") || normalized.contains("组织")) return "业务组织";
        if (normalized.contains("公司")) return "公司";
        return "业务字段";
    }

    private boolean isOrganizationHeader(String header) {
        String normalized = normalize(header);
        return normalized.contains("部门") || normalized.contains("组织");
    }

    private String fieldType(String sample) {
        if (StringUtils.isBlank(sample)) return "TEXT";
        try {
            new BigDecimal(sample.replace(",", ""));
            return "NUMBER";
        } catch (NumberFormatException ignored) {
            return "TEXT";
        }
    }

    private Map<String, Object> fieldDefinition(OaImportTemplateFieldVo field) {
        Map<String, Object> definition = new LinkedHashMap<>();
        definition.put("code", field.getCode());
        definition.put("header", field.getHeader());
        definition.put("type", field.getType());
        definition.put("required", Boolean.TRUE.equals(field.getRequired()));
        definition.put("uniqueKey", Boolean.TRUE.equals(field.getUniqueKey()));
        return definition;
    }

    private String normalize(String value) {
        return StringUtils.trim(value).replaceAll("[\\s\\u3000_\\-（）()/:：]", "").toLowerCase(Locale.ROOT);
    }

    private void validate(OaImportBusinessConfigBo bo) {
        OaBusinessTypeVo businessType = businessTypeService.requireEnabledConfig(bo.getBusinessType());
        bo.setBusinessType(businessType.getBusinessType());
        // 业务名称以业务类型主数据为准，避免用户手填名称与泛微标识不一致。
        bo.setBusinessName(businessType.getBusinessName());
        String defaultApprovalMode = normalizeApprovalMode(bo.getDefaultApprovalMode());
        if ("PLAN".equals(defaultApprovalMode) && bo.getDefaultApprovalPlanId() == null) {
            throw new ServiceException("默认审批方式为指定方案时，必须选择默认审批方案");
        }
        if (bo.getDefaultWorkflowConfigId() != null) {
            workflowConfigService.requireEnabled(bo.getDefaultWorkflowConfigId(), bo.getBusinessType());
        }
        if (bo.getDefaultApprovalPlanId() != null) {
            OaDepartmentApproval plan = approvalMapper.selectById(bo.getDefaultApprovalPlanId());
            if (plan == null || !"ENABLED".equalsIgnoreCase(plan.getStatus())) {
                throw new ServiceException("默认审批方案不存在或已停用");
            }
            if (bo.getDefaultWorkflowConfigId() != null
                && !bo.getDefaultWorkflowConfigId().equals(plan.getWorkflowConfigId())) {
                throw new ServiceException("默认审批方案与默认泛微流程不匹配");
            }
            if (StringUtils.isNotBlank(plan.getBusinessType())
                && !plan.getBusinessType().equals(StringUtils.trim(bo.getBusinessType()))) {
                throw new ServiceException("默认审批方案与业务类型不匹配");
            }
        }
        if (StringUtils.isNotBlank(bo.getFieldDefinitionsJson()) && !JsonUtils.isJsonArray(bo.getFieldDefinitionsJson())) {
            throw new ServiceException("Excel字段定义必须是 JSON 数组");
        }
        if (StringUtils.isNotBlank(bo.getParameterDefinitionsJson()) && !JsonUtils.isJsonArray(bo.getParameterDefinitionsJson())) {
            throw new ServiceException("参数定义必须是 JSON 数组");
        }
        if (StringUtils.isNotBlank(bo.getGroupByJson()) && !JsonUtils.isJsonArray(bo.getGroupByJson())) {
            throw new ServiceException("分组字段必须是 JSON 数组");
        }
        validateObject(bo.getAggregationJson(), "聚合规则");
        validateObject(bo.getFormMappingJson(), "OA表单映射");
        validateAttachmentConfig(bo.getAttachmentConfigJson());
    }

    private void validateObject(String json, String name) {
        if (StringUtils.isNotBlank(json) && !JsonUtils.isJsonObject(json)) {
            throw new ServiceException(name + "必须是 JSON 对象");
        }
    }

    private void validateAttachmentConfig(String json) {
        if (StringUtils.isBlank(json)) {
            return;
        }
        validateObject(json, "附件配置");
        AttachmentConfig config;
        try {
            config = JsonUtils.parseObject(json, AttachmentConfig.class);
        } catch (Exception ex) {
            throw new ServiceException("附件配置格式不正确：" + ex.getMessage());
        }
        if (config == null || !"GENERATED_TABLE".equalsIgnoreCase(config.getMode())) {
            throw new ServiceException("附件配置必须使用模板生成模式");
        }
        if (config.getTemplateOssId() == null) {
            throw new ServiceException("已启用附件生成，请先上传附件模板");
        }
        SysOssVo oss = ossService.getById(config.getTemplateOssId());
        if (oss == null || StringUtils.isBlank(oss.getOriginalName())) {
            throw new ServiceException("附件模板不存在，请重新上传");
        }
        String fileName = oss.getOriginalName().toLowerCase(Locale.ROOT);
        if (!fileName.endsWith(".xlsx") && !fileName.endsWith(".xls")) {
            throw new ServiceException("附件模板仅支持 .xlsx 或 .xls 文件");
        }
        validateRow(config.getTitleRow(), "附件标题行");
        validateRow(config.getHeaderRow(), "附件表头行");
        validateRow(config.getDataStartRow(), "附件明细起始行");
        validateRow(config.getTotalRow(), "附件合计行");
        if (config.getHeaderRow() != null && config.getDataStartRow() != null
            && config.getDataStartRow() <= config.getHeaderRow()) {
            throw new ServiceException("附件明细起始行必须大于表头行");
        }
        if (config.getTotalRow() != null && config.getDataStartRow() != null
            && config.getTotalRow() <= config.getDataStartRow()) {
            throw new ServiceException("附件合计行必须晚于明细起始行");
        }
        if (config.getHeaders() != null) {
            Set<Integer> columnIndexes = new HashSet<>();
            for (int index = 0; index < config.getHeaders().size(); index++) {
                OaImportBusinessServiceImpl.OutputColumn column = config.getHeaders().get(index);
                if (column == null) {
                    throw new ServiceException("附件列配置不能为空");
                }
                int columnIndex = column.getColumnIndex() == null ? index : column.getColumnIndex();
                if (columnIndex < 0 || !columnIndexes.add(columnIndex)) {
                    throw new ServiceException("附件列位置不能重复且不能小于 0");
                }
            }
        }
        if (config.getFixedMappings() != null) {
            Set<String> cells = new HashSet<>();
            for (FixedCellMapping mapping : config.getFixedMappings()) {
                if (mapping == null || mapping.getRow() == null || mapping.getColumn() == null
                    || mapping.getRow() < 0 || mapping.getColumn() < 0) {
                    throw new ServiceException("固定区域单元格位置不正确");
                }
                String mode = StringUtils.isBlank(mapping.getMode()) ? "KEEP" : mapping.getMode().trim().toUpperCase(Locale.ROOT);
                if (!Set.of("KEEP", "CLEAR", "CURRENT_USER", "CURRENT_DEPT", "SETTLEMENT_DEPT",
                    "SETTLEMENT_OWNER", "CUSTOM", "FIELD").contains(mode)) {
                    throw new ServiceException("固定区域填充方式不支持：" + mapping.getMode());
                }
                String cell = mapping.getRow() + ":" + mapping.getColumn();
                if (!cells.add(cell)) {
                    throw new ServiceException("固定区域单元格不能重复配置");
                }
                if (("CUSTOM".equals(mode) || "FIELD".equals(mode)) && StringUtils.isBlank(mapping.getTemplate())) {
                    throw new ServiceException("自定义或字段填充的固定区域内容不能为空");
                }
                if ("FIELD".equals(mode) && StringUtils.isBlank(mapping.getField())) {
                    throw new ServiceException("字段填充的固定区域必须选择字段");
                }
            }
        }
    }

    private void validateRow(Integer value, String name) {
        if (value != null && value < 0) {
            throw new ServiceException(name + "不能小于 0");
        }
    }

    private String trim(String value) {
        return StringUtils.isBlank(value) ? null : StringUtils.trim(value);
    }

    private String normalizeApprovalMode(String value) {
        if (StringUtils.isBlank(value)) {
            return "AUTO_RULE";
        }
        String mode = value.trim().toUpperCase();
        if (!List.of("AUTO_RULE", "PLAN", "MANUAL").contains(mode)) {
            throw new ServiceException("默认审批方式仅支持自动匹配、选择方案或临时指定");
        }
        return mode;
    }

    private OaImportBusinessConfigVo toVo(OaImportBusinessConfig entity) {
        OaImportBusinessConfigVo vo = new OaImportBusinessConfigVo();
        vo.setId(entity.getId());
        vo.setBusinessType(entity.getBusinessType());
        vo.setBusinessName(entity.getBusinessName());
        vo.setSheetName(entity.getSheetName());
        vo.setHeaderRow(entity.getHeaderRow());
        vo.setFieldDefinitionsJson(entity.getFieldDefinitionsJson());
        vo.setParameterDefinitionsJson(entity.getParameterDefinitionsJson());
        vo.setGroupByJson(entity.getGroupByJson());
        vo.setDeptField(entity.getDeptField());
        vo.setCompanyField(entity.getCompanyField());
        vo.setAggregationJson(entity.getAggregationJson());
        vo.setFormMappingJson(entity.getFormMappingJson());
        vo.setAttachmentConfigJson(entity.getAttachmentConfigJson());
        vo.setRequestNameTemplate(entity.getRequestNameTemplate());
        vo.setContentTemplate(entity.getContentTemplate());
        vo.setDefaultWorkflowConfigId(entity.getDefaultWorkflowConfigId());
        vo.setDefaultApprovalPlanId(entity.getDefaultApprovalPlanId());
        vo.setDefaultApprovalMode(entity.getDefaultApprovalMode());
        vo.setStatus(entity.getStatus());
        vo.setRemark(entity.getRemark());
        return vo;
    }
}
