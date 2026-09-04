package org.dromara.department.service;

import org.dromara.common.core.domain.PageResult;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.department.domain.bo.OperationRecordBo;
import org.dromara.department.domain.bo.OperationRecordQueryBo;
import org.dromara.department.domain.bo.OperationSystemBo;
import org.dromara.department.domain.vo.OperationRecordImportVo;
import org.dromara.department.domain.vo.OperationRecordVo;
import org.dromara.department.domain.vo.OperationSummaryVo;
import org.dromara.department.domain.vo.OperationSystemImportVo;
import org.dromara.department.domain.vo.OperationSystemVo;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

/**
 * 运维台账业务接口。
 */
public interface IOperationLedgerService {

    /**
     * 分页查询运维工作记录。
     *
     * @param bo        工作记录查询条件
     * @param pageQuery 分页参数
     * @return 分页工作记录
     */
    PageResult<OperationRecordVo> queryRecordPageList(OperationRecordQueryBo bo, PageQuery pageQuery);

    /**
     * 查询运维工作记录列表。
     *
     * @param bo 工作记录查询条件
     * @return 工作记录列表
     */
    List<OperationRecordVo> queryRecordList(OperationRecordQueryBo bo);

    /**
     * 查询运维工作记录详情。
     *
     * @param id 工作记录主键
     * @return 工作记录详情
     */
    OperationRecordVo queryRecordById(Long id);

    /**
     * 新增运维工作记录。
     *
     * @param bo 工作记录新增参数
     * @return 是否新增成功
     */
    Boolean insertRecord(OperationRecordBo bo);

    /**
     * 修改运维工作记录。
     *
     * @param bo 工作记录修改参数
     * @return 是否修改成功
     */
    Boolean updateRecord(OperationRecordBo bo);

    /**
     * 删除运维工作记录。
     *
     * @param ids 工作记录主键集合
     * @return 是否删除成功
     */
    Boolean deleteRecords(Collection<Long> ids);

    /**
     * 导入运维工作记录。
     *
     * @param rows 导入行数据
     * @return 导入结果说明
     */
    String importRecords(List<OperationRecordImportVo> rows);

    /**
     * 分页查询系统在线率记录。
     *
     * @param beginDate 统计开始日期，包含当天
     * @param endDate   统计结束日期，包含当天
     * @param systemName 系统名称关键字
     * @param pageQuery 分页参数
     * @return 分页在线率记录
     */
    PageResult<OperationSystemVo> querySystemPageList(LocalDate beginDate, LocalDate endDate, String systemName, PageQuery pageQuery);

    /**
     * 查询系统在线率记录列表。
     *
     * @param beginDate 统计开始日期，包含当天
     * @param endDate   统计结束日期，包含当天
     * @param systemName 系统名称关键字
     * @return 在线率记录列表
     */
    List<OperationSystemVo> querySystemList(LocalDate beginDate, LocalDate endDate, String systemName);

    /**
     * 查询系统在线率记录详情。
     *
     * @param id 在线率记录主键
     * @return 在线率记录详情
     */
    OperationSystemVo querySystemById(Long id);

    /**
     * 新增系统在线率记录。
     *
     * @param bo 在线率记录新增参数
     * @return 是否新增成功
     */
    Boolean insertSystem(OperationSystemBo bo);

    /**
     * 修改系统在线率记录。
     *
     * @param bo 在线率记录修改参数
     * @return 是否修改成功
     */
    Boolean updateSystem(OperationSystemBo bo);

    /**
     * 删除系统在线率记录。
     *
     * @param ids 在线率记录主键集合
     * @return 是否删除成功
     */
    Boolean deleteSystems(Collection<Long> ids);

    /**
     * 导入系统在线率记录。
     *
     * @param rows 导入行数据
     * @return 导入结果说明
     */
    String importSystems(List<OperationSystemImportVo> rows);

    /**
     * 构建指定日期范围的运维指标汇总。
     *
     * @param beginDate 统计开始日期，包含当天
     * @param endDate   统计结束日期，包含当天
     * @return 运维指标汇总
     */
    OperationSummaryVo buildSummary(LocalDate beginDate, LocalDate endDate);
}
