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

    PageResult<OperationRecordVo> queryRecordPageList(OperationRecordQueryBo bo, PageQuery pageQuery);

    List<OperationRecordVo> queryRecordList(OperationRecordQueryBo bo);

    OperationRecordVo queryRecordById(Long id);

    Boolean insertRecord(OperationRecordBo bo);

    Boolean updateRecord(OperationRecordBo bo);

    Boolean deleteRecords(Collection<Long> ids);

    String importRecords(List<OperationRecordImportVo> rows);

    PageResult<OperationSystemVo> querySystemPageList(LocalDate beginDate, LocalDate endDate, String systemName, PageQuery pageQuery);

    List<OperationSystemVo> querySystemList(LocalDate beginDate, LocalDate endDate, String systemName);

    OperationSystemVo querySystemById(Long id);

    Boolean insertSystem(OperationSystemBo bo);

    Boolean updateSystem(OperationSystemBo bo);

    Boolean deleteSystems(Collection<Long> ids);

    String importSystems(List<OperationSystemImportVo> rows);

    OperationSummaryVo buildSummary(LocalDate beginDate, LocalDate endDate);
}
